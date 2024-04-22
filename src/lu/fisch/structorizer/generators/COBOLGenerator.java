/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Simon Sobisch
 *
 *      Description:    This class generates COBOL code from Nassi-Shneiderman diagrams.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Simon Sobisch           2017.04.14      First Issue for #357
 *      Kay Gürtzig             2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig             2019-10-09      Code for Alternative and Case implemented
 *      Kay Gürtzig             2020-04-16      Issue #739: Enumeration type export prepared
 *      Kay Gürtzig             2020-04-19      Declaration part advanced.
 *      Kay Gürtzig             2020-04-22      Bugfix #854: Deterministic topological order of type definitions ensured
 *      Kay Gürtzig             2021-06-07      Issue #67: lineNumering option made plugin-specific
 *      Kay Gürtzig             2024-04-12      Issue #1148: Special handling of ELSE-IF chains by EVALUATE
 *      Kay Gürtzig             2024-04-13/14   Some efforts for assignments and output instructions
 *      
 ******************************************************************************************************
 *
 *      Comment:  /
 *      2020-04-19 Kay Gürtzig
 *      - A rather tricky aspect are record and array initializer expressions. Neither does exist in
 *        COBOL. A COBOL-typical way would be to declare a data item with the initial values and then
 *        to use MOVE CORRESPONDING TO where it is to be assigned. Unfortunately this will not work if
 *        some of the "initializers" contains variables as element values because their value cannot be
 *        known in a data item definition. So the only general way is to decompose the initializer in
 *        place, i.e. where it is to be assigned. This does not help in case an initializer is used as
 *        argument value for a subroutine call though. There we would need a temporary variable, which
 *        must first be filled an thn be passed (by reference in most cases). Where to get this variable
 *        from it would hav to be declared (with full structure) in advance.
 *      - Another ugly problem is that there is no general concept of an expression in COBOL. For
 *        numerical expressions we can at least use COMPUTE (rather than having to decompose it into
 *        ADD, SUBTRACT, MULTIPLY, DIVIDE, and all that crap. But what to do with mixed expressions
 *        (containing string concatenation, comparisons, conversions etc.)? This will require a lot of
 *        auxiliary data items and enormous efforts to decompose the entire expression into whatever.
 *        Without a syntax tree representation of the Structorizer element content and the possibility
 *        to insert declarations into some data section afterwards this is simply not feasible. So the
 *        instruction element is possibly the most complex task.
 *      - Loop bodies may have to be extracted to some named code section in order to be able to form
 *        legal PERFORM commands, particularly in fixed format, otherwise inline PERFORM can be chose.
 *      
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.StringList;

/**
 * @author Simon Sobisch
 *
 */
public class COBOLGenerator extends Generator {

	public enum CodePart {

		WORKING_STORAGE("WS"), LINKAGE("LI"), PROCEDURE_DIVISION("PD");

		private String abbreviation;

		private CodePart(String abbreviation) {
			this.abbreviation = abbreviation;
		}

		public String getAbreviation() {
			return this.abbreviation;
		}

		public static CodePart getByAbbreviation(String abbreviation) {
			String inputString = abbreviation.toUpperCase();
			for (CodePart cp : CodePart.values()) {
				if (cp.abbreviation.equals(inputString)) {
					return cp;
				}
			}
			return null;
		}

	}

	private int lineNumber = 10;
	private int lineIncrement = 10;
	private final String[] ext = { "cob", "cbl" };
	/** Maps Roots to the names of all routines they call */
	private final HashMap<Root, HashSet<String>> subMap = new HashMap<Root, HashSet<String>>();
	/** Maps variable and type names to the structural type information */
	private HashMap<String, TypeMapEntry> typeMap;
	/** Collects extracted bodies of loops and other structures for procedure generation */
	private final HashMap<String, Subqueue> postponedProcedures = new HashMap<String, Subqueue>();

	// START KGU#686 2019-03-18: Enh. #56
	/**
	 * Subclassable method to specify the degree of availability of a try-catch-finally
	 * construction in the target language.
	 * @return a {@link TryCatchSupportLevel} value
	 */
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_NO_TRY;
	}
	// END KGU#686 2019-03-18

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#getDialogTitle()
	 */
	@Override
	protected String getDialogTitle() {
		return "Export COBOL ...";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#getFileDescription()
	 */
	@Override
	protected String getFileDescription() {
		return "COBOL Source Code";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#getIndent()
	 */
	@Override
	protected String getIndent() {
		// switching the reference format between free-form and fixed-form
		if (!this.optionFixedSourceFormat()) {
			return "\t";
		} else {
			// a tab "\t" would be better but cannot be counted for the line
			// length
			return "   ";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#getFileExtensions()
	 */
	@Override
	protected String[] getFileExtensions() {
		return this.ext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#commentSymbolLeft()
	 */
	@Override
	protected String commentSymbolLeft() {
		return "*>";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	protected String getInputReplacer(boolean withPrompt) {
		if (withPrompt) {
			return ("DISPLAY $1 ACCEPT $2");
		} else {
			return "ACCEPT $1";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer() {
		return "DISPLAY $1";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#breakMatchesCase()
	 */
	@Override
	protected boolean breakMatchesCase() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern() {

		//insertUserIncludes(CodePart.WORKING_STORAGE);
		return this.getLineStart(false) + "COPY %.";
	}

	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		// FIXME: No idea whether subroutine overloading is a sensible concept in COBOl at all.
		return OverloadingLevel.OL_NO_OVERLOADING;
	}
	// END KGU#371 2019-03-07

	/**
	 * get start for COBOL source or comment line with correct length depending
	 * on reference-format and optional line numbering for fixed-form reference
	 * format
	 * 
	 * @return the complete start for the line, identical to _indent in
	 *         free-form reference format
	 * 
	 */
	protected String getLineStart(Boolean isCommentLine) {

		String prefix;
		// switching the reference format between free-form and fixed-form
		if (!this.optionFixedSourceFormat()) {
			prefix = this.getIndent();
		} else {
			if (this.optionCodeLineNumbering()) {
				prefix = String.format("%5d", this.lineNumber) + " ";
				this.lineNumber += this.lineIncrement;
			} else {
				prefix = "      ";
			}
			if (!isCommentLine) {
				prefix += " ";
			}
		}
		if (isCommentLine) {
			prefix += this.commentSymbolLeft() + " ";
		}
		return prefix;
	}

	// include / import / uses config
	/*
	 * function for checking and inserting UserIncludes special case for COBOL
	 * copybooks: IncludePattern setting contains the CodePart where the
	 * inclusion should be done, for example
	 * "WS: data1.cpy, data2.cpy; LS: localdata.copy; PD: helpers.cob"
	 */
	protected void appendUserIncludes(CodePart cp) {
		String includes = this.optionIncludeFiles().trim();
		if (includes == null || includes.isEmpty()) {
			return;
		}
		String pattern = this.getIncludePattern();
		if (pattern == null || !pattern.contains("%")) {
			return;
		}
		String _indent = this.getIndent();

		for (String target : includes.split(";")) {
			target = target.trim();
			if (target.startsWith(cp.abbreviation + ":", 0)) {
				String[] items = target.split(":", 3);
				if (items.length == 2) {
					String copies[] = items[1].split(",");
					for (String copy : copies) {
						copy = copy.trim();
						if (!copy.isEmpty()) {
							code.add(_indent + pattern.replace("%", prepareUserIncludeItem(copy)));
						}
					}
				}
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#addCode(java.lang.String,
	 * java.lang.String, boolean)
	 */
	@Override
	protected void addCode(String text, String _indent, boolean asComment) {
		if (asComment) {
			// Indentation is intentionally put inside the comment (comment
			// encloses entire line)
			appendComment(_indent + text, "");
		// START KGU 2017-05-11 At least in free format we shouldn't ignore the indentation
		} else if (!this.optionFixedSourceFormat()) {
			code.add(this.getLineStart(false) + _indent + text);			
		// END KGU 2017-05-11
		} else {
			code.add(this.getLineStart(false) + text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#insertCode(java.lang.String,
	 * boolean)
	 */
	@Override
	protected void insertCode(String text, int atLine)
	{
		super.insertCode(this.getLineStart(false) + text, atLine);
	}

	// We need an overridden fundamental comment method here to be able to
	// switch between fixed-form and free-from reference format.
	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#appendComment(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	protected void appendComment(String _text, String _indent) {
		String[] lines = _text.split("\n");
		for (String line : lines) {
			code.add(this.getLineStart(true) + line);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lu.fisch.structorizer.generators.Generator#appendBlockComment(lu.fisch.
	 * utils.StringList, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	protected void appendBlockComment(StringList _sl, String _indent, String _start, String _cont, String _end) {
		int oldSize = code.count();
		super.appendBlockComment(_sl, _indent, _start, _cont, _end);
		// Set indent for fixed-form reference-format and optional the line numbers afterwards,
		// the super method wouldn't have done it
		// FIXME: using optionBlockBraceNextLine as a dirty workaround for
		// switching the reference format between free-form and fixed-form
		// optionBlockBraceNextLine = true --> free-form reference-format
		if (!this.optionBlockBraceNextLine()) {
			for (int i = oldSize; i < code.count(); i++) {
				code.set(i, this.getLineStart(true) + code.get(i).substring(_indent.length()-1));
			}
		}
	}

	/* (non-Javadoc)
	 * Additionally maps the _caller to the name of the called routine
	 * @see lu.fisch.structorizer.generators.Generator#registerCalled(lu.fisch.structorizer.elements.Call, lu.fisch.structorizer.elements.Root)
	 */
	@Override
	protected Root registerCalled(Call _call, Root _caller)
	{
		Function _called = _call.getCalledRoutine();
		if (_called != null && _called.isFunction()
				&& !(_called.getName().equalsIgnoreCase(_caller.getMethodName()))) {
			if (!this.subMap.containsKey(_caller)) {
				this.subMap.put(_caller, new HashSet<String>());
			}
			// FIXME: How do we cope with overloading? We might modify the name by appending the argument number
			this.subMap.get(_caller).add(_called.getName());
		}
		return super.registerCalled(_call, _caller);
	}
	
	// START KGU#395 2017-05-11: Enh. #357 - source format option for COBOL export
	/**
	 * Returns the value of the export option whether fixed source file format must
	 * be used.
	 * @return true if fixed file format is to be used.
	 */
	private boolean optionFixedSourceFormat()
	{
		Object optionVal = this.getPluginOption("fixedFormat", false);
		return optionVal instanceof Boolean && ((Boolean)optionVal).booleanValue();
	}
	
	/**
	 * Returns the value of the export option to replace underscores by
	 * hyphens in identifiers (normally there aren't underscores in COBOL, but they could rarely be used).
	 * @return true if names are to converted
	 */
	private boolean optionUnderscores2Hyphens()
	{
		Object optionVal = this.getPluginOption("underscores2hyphens", true);
		return !(optionVal instanceof Boolean) || ((Boolean)optionVal).booleanValue();
	}
	// END KGU#395 2017-05-11
	
	// START KGU#395 2020-04-17: Enh. #357 Two new options
	/** @return the incrementing step for the level numbers of composed data entries */
	private int optionLevelIncrement()
	{
		int levelIncr = 2;	// The default value
		Object optionVal = this.getPluginOption("dataLevelIncrement", levelIncr);
		if (optionVal instanceof Integer) {
			levelIncr = Math.max(1, ((Integer)optionVal).intValue());
		}
		return levelIncr;
	}
	// END KGU#395 2020-04-17
	
	// START KGU#113 2021-06-07: Enh. #67 - Line numbering now as plugin-specific option
	/**
	 * Returns the value of the export option whether to generate line numbers
	 * at the beginning of every single line.
	 * @return true if lines are to start with numbers.
	 */
	private boolean optionCodeLineNumbering() {
		// START KGU 2016-04-04: Issue #151 - Get rid of the inflationary eod threads
		//return (eod.lineNumbersCheckBox.isSelected());
		// START KGU#113 2021-06-07: Enh. #67 Converted to a plugin-specific option
		//return this.generateLineNumbers;
		Object optionVal = this.getPluginOption("lineNumbering", false);
		return optionVal instanceof Boolean && (boolean)optionVal;
		// END KGU 2016-04-04
	}
	// END KGU#113 2015-12-18	
	
	/************ Code Generation **************/
	
	/**
	 * If required by the respective export option, replaces underscores in the given
	 * name by hyphens.
	 * @param _identifier - the name to be transformed
	 * @return transformed identifier
	 */
	private String transformName(String _identifier) {
		if (this.optionUnderscores2Hyphens()) {
			_identifier = _identifier.replace("_", "-");
		}
		return _identifier;
	}

	// START KGU#388/KGU#542 2019-12-04: Enh. #423, #739
	@Override
	protected String transformType(String _type, String _default)
	{
		if (_type == null || _type.equals("???")) {
			_type = _default;
		}
		else if (_type.equalsIgnoreCase("string")) {
			// Could also be "PIC N(...) USAGE NATIONAL"
			_type = "PIC X(" + Integer.toString(this.optionDefaultStringLength()) + ")";
		}
		else if (_type.equalsIgnoreCase("int")) {
			// Could also be "PIC S9(9) USAGE BINARY"
			_type = "USAGE BINARY-LONG";
		}
		else if (_type.equalsIgnoreCase("long")) {
			// Could also be "PIC S9(18) USAGE BINARY"
			_type = "USAGE BINARY-DOUBLE";
		}
		else if (_type.equalsIgnoreCase("float")) {
			// Could also be "USAGE COMP-1"
			_type = "USAGE FLOAT-SHORT";
		}
		else if (_type.equalsIgnoreCase("double")) {
			// Could also be "USAGE COMP-2"
			_type = "USAGE FLOAT-LONG";
		}
		else if (_type.equalsIgnoreCase("bool") || (_type.equalsIgnoreCase("boolean"))) {
			_type = "PIC 9(4) USAGE BINARY";
		}
		// TODO
		return _type;
	}
	
	/**
	 * Adds the type definitions for all types in {@code _root.getTypeInfo()}.
	 * @param _root - originating Root
	 * @param _indent - current indentation level (as String)
	 */
	protected void generateTypeDefs(Root _root, String _indent) {
		for (Entry<String, TypeMapEntry> typeEntry: _root.getTypeInfo(routinePool).entrySet()) {
			String typeKey = typeEntry.getKey();
			if (typeKey.startsWith(":")) {
				generateTypeDef(_root, typeKey.substring(1), typeEntry.getValue(), _indent, false);
			}
		}
	}

	/**
	 * Appends a typedef or struct definition for the type passed in by {@code _typeEnry}
	 * if it hadn't been defined globally or in the preamble before.
	 * @param _root - the originating Root
	 * @param _type - the type map entry the definition for which is requested here
	 * @param _indent - the current indentation
	 * @param _asComment - if the type definition is only to be added as comment (disabled)
	 */
	protected void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
		String typeKey = ":" + _typeName;
		if (this.wasDefHandled(_root, typeKey, true)) {
			return;
		}
		String indentPlus1 = _indent + this.getIndent();
		appendDeclComment(_root, _indent, typeKey);
		if (_type.isRecord()) {
			// FIXME: This doesn't make sense at all for COBOL!
			addCode("struct " + _type.typeName + " {", _indent, _asComment);
			for (Entry<String, TypeMapEntry> compEntry: _type.getComponentInfo(false).entrySet()) {
				addCode(transformTypeFromEntry(compEntry.getValue(), _type, true) + "\t" + compEntry.getKey() + ";",
						indentPlus1, _asComment);
			}
			addCode("};", _indent, _asComment);
		}
		else if (_type.isEnum()) {
			StringList items = _type.getEnumerationInfo();
			appendComment("enum " + _type.typeName, _indent);
			int lastVal = -1;
			for (int i = 0; i < items.count(); i++) {
				// FIXME: We might have to transform the value...
				// START KGU#542 2020-04-16: Enh. #739
				String constName = items.get(i);
				String constVal = _root.getConstValueString(constName);
				if (constVal == null) {
					constVal = Integer.toString(++lastVal);
				}
				else {
					try {
						lastVal = Integer.parseInt(constVal);
					}
					catch (NumberFormatException exc) {
						lastVal++;
					}
				}
				addCode("78 " + constName + " VALUE " + constVal, indentPlus1, _asComment);
				// END KGU#542 2020-04-16
			}
		}
	}
	// END KGU#388/KGU#542 2019-12-04

	// START KGU#375 2017-04-12: Enh. #388 common preparation of constants and variables
	protected void appendDeclaration(Root _root, String _name, String _indent)
	{
		/* FIXME: replace declarations
		 * numeric-only used variables can stay with USAGE binary-long (or smaller)
		 * --> only need a USAGE added, works for "pointer", too
		 * all other need a PICTURE clause
		 * "String" is normally PIC X(max-size-used) 
		 */
		String cobName = this.transformName(_name);
		TypeMapEntry typeInfo = typeMap.get(_name);
		StringList types = null;
		String constValue = _root.getConstValueString(_name);
		String level = "01 ";
		if (typeInfo != null) {
			types = getTransformedTypes(typeInfo, false);
		}
		// START KGU#375 2017-04-12: Enh. #388: Might be an imported constant
		// Enumeration constants will be exported together by generateTypeDef(...);
		else if (constValue != null && !_root.constants.get(_name).contains("€")) {
			String type = Element.identifyExprType(typeMap, constValue, false);
			if (!type.isEmpty()) {
				types = StringList.getNew(transformType(type, "int"));
				// We place a faked workaround entry
				typeMap.put(_name, typeInfo = new TypeMapEntry(type, null, null, _root, 0, true, false));
			}
			/* FIXME If it is a numeric constant to be used with "USAGE"
			 * then "01 " should remain, but "CONSTANT" would have to be inserted
			 */
			level = "78 ";
		}
		// If the type is unambiguous then add the declaration here
		if (types != null && types.count() == 1 && typeInfo != null) {
			// TODO if types.count() > 1 we might think about a REDEFINES clause
			if (typeInfo.isArray()) {
				generateArrayDeclaration(cobName, typeInfo, 1, _indent);
				return;
			}
			else if (typeInfo.isRecord()) {
				generateRecordDeclaration(cobName, typeInfo, 1, _indent);
				return;
			}
			String decl = types.get(0).trim();

//			// START KGU#375 2017-04-12: Enh. #388
			if (constValue != null) {
					decl = " VALUE " + transform(constValue);
			}
			decl = level + cobName + "\t" + decl;
			// END KGU#375 2017-04-12
			addCode(decl + ".", _indent, false);
		}
		else if (types != null && types.isEmpty() && _name.startsWith("unstring_")) {
			// Apparently a generic name from a COBOL import - is assumed to be a string array
			addCode(level + cobName + ".", _indent, false);
			addCode(String.format("%02d ", this.optionLevelIncrement()) + "FILLER\t"
			+ transformType("string", "PIC x(" + this.optionDefaultStringLength() + ")")
			+ " OCCURS " + Integer.toString(this.optionDefaultArraySize()),
			_indent + this.getIndent(), false);
		}
		// there is no type info
		else {
			//appendComment(cobName, _indent);
			addCode(level + cobName + ".", _indent, false);
		}
		// END KGU#261/KGU#332 2017-01-16
	}
	// END KGU#375 2017-04-12
	
	/**
	 * @param _cobName - the COBOL-transformed name for the record variable or component
	 * @param _typeInfo - the {@link TypeMapEntry} describing the record type
	 * @param _level - the structure or declaration level (1 is top, 49 is maximum)
	 * @param _indent - the current indentation level - may depend on the format
	 */
	private void generateRecordDeclaration(String _cobName, TypeMapEntry _typeInfo, int _level, String _indent) {
		// TODO Auto-generated method stub
		addCode(String.format("%02d ", _level) + _cobName + ".", _indent, false);
		String subIndent = _indent;
		int incr = this.optionLevelIncrement();
		if (!this.optionFixedSourceFormat() || _level == 1) {
			subIndent += this.getIndent();
		}
		for (Map.Entry<String, TypeMapEntry> comp: _typeInfo.getComponentInfo(true).entrySet()) {
			String compName = transformName(comp.getKey());
			TypeMapEntry compType = comp.getValue();
			if (compType.isArray()) {
				generateArrayDeclaration(compName, compType, _level + incr, subIndent);
			}
			else if (compType.isRecord()) {
				generateRecordDeclaration(compName, compType, _level + incr, subIndent);
			}
			else if (compType.isEnum()) {
				addCode(String.format("%02d ", _level + 2) + compName + "\tPIC 9(3).", subIndent, false);
			}
			else {
				String canonType = compType.getCanonicalType(true, true);
				addCode(String.format("%02d ", _level + 2) + compName + "\t" + transformType(canonType, "???") + ".", subIndent, false);
			}
		}
	}

	/**
	 * @param _cobName - the COBOl-transformed name for the array variable or component
	 * @param _typeInfo 
	 * @param _level - the structure or declaration level (1 is top, 49 is maximum)
	 * @param _indent - the current indentation level - may depend on the format
	 */
	private void generateArrayDeclaration(String _cobName, TypeMapEntry _typeInfo, int _level, String _indent) {
		String subIndent = _indent;
		int incr = this.optionLevelIncrement();
		StringList types = _typeInfo.getTypes();
		String compName = _cobName;
		if (_level == 1) {
			// The data item must have a name at level 1
			addCode("01 " + _cobName, _indent, false);
			compName = "FILLER";
			_level += incr;
			subIndent += this.getIndent();
		}
		for (int i = 0; i < types.count(); i++) {
			// We look for the first type variant that is definitely an array
			String type = types.get(i);
			if (type.startsWith("@")) {
				int dim = 0;
				while (type.startsWith("@")) {
					String occursClause = " OCCURS ";
					int minIdx = _typeInfo.getMinIndex(dim);
					int maxIdx = _typeInfo.getMaxIndex(dim);
					if (minIdx > 0) {
						occursClause += Integer.toString(minIdx + 1) + " TO ";
					}
					if (maxIdx < 0) {
						maxIdx = Math.max(minIdx, 0) + this.optionDefaultArraySize() - 1;
					}
					occursClause += Integer.toString(maxIdx + 1);

					type = type.substring(1);
					// Check element type
					TypeMapEntry elemType = null;
					if ((type.startsWith("${") && (elemType = typeMap.get(":" + (type = type.substring(2)))) != null 
							|| !type.startsWith("@") && (elemType = typeMap.get(":" + type)) != null)
							&& elemType.isRecord()) {
						// Seems to be a record type
						this.generateRecordDeclaration(compName + occursClause,
								elemType, _level + dim * incr, subIndent);
						type = "";
					}
					else if (type.startsWith("@")) {
						addCode(String.format("%02d ", _level + dim * incr)
								+ compName + occursClause + ".", subIndent, false);
					}
					else {
						if (type.equals("???") && _cobName.startsWith(transformName("unstring_"))) {
							type = "string";
						}
						addCode(String.format("%02d " , _level + dim * incr)
								+ compName + "\t" + transformType(type, "???") + occursClause, subIndent, false);
					}
					if (!this.optionFixedSourceFormat()) {
						subIndent += this.getIndent();
					}
					dim++;
				}
			}
			if (!type.isEmpty()) {
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// First get rid of superfluous spaces
		int pos = -1;
		StringList doubleBlank = StringList.explode(" \n ", "\n");
		while ((pos = tokens.indexOf(doubleBlank, 0, true)) >= 0)
		{
			tokens.delete(pos);	// Get rid of one of the blanks
		}
		// On inserting operator keywords we better make sure them being padded
		// (lest neighbouring identifiers be glued to them on concatenating)
		// The correct way would of course be to add blank tokens where needed
		// but this seemed too expensive here.
		tokens.replaceAll("==", "=");
		// TODO
		tokens.replaceAll("!=", "<>");	// FIXME!!!!!
		tokens.replaceAll("%", " mod ");	// FIXME
		tokens.replaceAll("&&", " AND ");
		tokens.replaceAll("||", " OR ");
		tokens.replaceAll("!", " NOT ");
		tokens.replaceAll("&", " and ");		// FIXME
		tokens.replaceAll("|", " or ");		// FIXME
		tokens.replaceAll("~", " not ");	// FIXME
		tokens.replaceAll("<<", " shl ");	// FIXME
		tokens.replaceAll(">>", " shr ");	// FIXME
		tokens.replaceAll("<-", ":=");	// FIXME
		// START KGU#311 2016-12-26: Enh. #314 - Support for File API
		if (this.usesFileAPI) {
			tokens.replaceAll("fileWrite", "write");
			tokens.replaceAll("fileWriteLine", "writeln");
			tokens.replaceAll("fileEOF", "eof");
			tokens.replaceAll("fileClose", "closeFile");
		}
		// END KGU#311 2016-12-26
		// START KGU#190 2016-04-30: Bugfix #181 - String delimiters must be converted to '
		for (int i = 0; i < tokens.count(); i++)
		{
			String token = tokens.get(i);
			if (token.length() > 1 && token.startsWith("\"") && token.endsWith("\""))
			{
				// Seems to be a string, hence modify it
				// Replace all internal apostrophes by double apostrophes
				token = token.replace("'", "''");
				// Now replace the outer delimiters
				tokens.set(i, "'" + token.substring(1, token.length()-1) + "'");
			}
		}
		// END KGU#190 2016-04-30
		String result = tokens.concatenate();
		// We now shrink superfluous padding - this may affect string literals, though!
		result = result.replace("  ", " ");
		result = result.replace("  ", " ");	// twice to catch odd-numbered space sequences, too
		return result;
	}

	@Override
	protected void generateCode(Instruction _inst, String _indent)
	{
		if (!appendAsComment(_inst, _indent)) {
			boolean isDisabled = _inst.isDisabled(false);
			StringList text = _inst.getUnbrokenText();
			appendComment(_inst, _indent);
			for (int i = 0; i < text.count(); i++) {
				String line = text.get(i);
				if (line.isEmpty() || Instruction.isMereDeclaration(line)) {
					continue;
				}
				boolean lineDone = false;
				StringList tokens = Element.splitLexically(line, true);
				Element.unifyOperators(tokens, false);
				String transfLine = transform(line);
				// Input should work via standard transformation...
				if (Instruction.isAssignment(line)) {
// START KGU#395 2024-04-13: Enh. #357 Extracted to a separate method
//					int posAsgn = tokens.indexOf("<-");
//					// FIXME Somehow we must find out what data type is transferred... -> #800
//					String varName = transform(Instruction.getAssignedVarname(tokens, false));
//					StringList exprTokens = tokens.subSequence(posAsgn+1, tokens.count());
//					boolean isVar = isVariable(exprTokens, true, typeMap);
//					if (varNames.contains(varName)) {
//						String target = Instruction.getAssignedVarname(tokens, true);
//						TypeMapEntry varType = typeMap.get(varName);
//						// FIXME This is all awful without syntax trees (#800)
//						if (varType != null && (varType.isArray() || varType.isRecord())) {
//							if (isVar) {
//								transfLine = "MOVE " + transform(exprTokens.concatenate(null)) + " CORRESPONDING TO " + transform(target);
//							}
//							else if (exprTokens.contains("{") && exprTokens.contains("}")) {
//								// Should be an initializer - so we will have to decompose it
//								// The code below was basically copied from CGenerator
//								int posBrace = exprTokens.indexOf("{");
//								// FIXME This code is incomllete and not ready
//								if (posBrace >= 0 && posBrace <= 1 && exprTokens.get(exprTokens.count()-1).equals("}")) {
//									String transfExpr = null;
//									if (posBrace == 1 && exprTokens.count() >= 3 && Function.testIdentifier(exprTokens.get(0), true, null)) {
//										String typeName = exprTokens.get(0);							
//										TypeMapEntry recType = this.typeMap.get(":"+typeName);
//										if (recType != null && recType.isRecord()) {
//											// transforms the Structorizer record initializer into a C-conform one
//											transfExpr = this.transformRecordInit(exprTokens, recType);
//										}
//									}
//									else if (posBrace == 0) {
//										// Seems to be an array initializer so decompose it to singulary assignments
//										/* (The alternative would have been to fake an initialized array declaration
//										 * in the data section and to  
//										 */
//										StringList items = Element.splitExpressionList(exprTokens.subSequence(1, exprTokens.count()), ",", true);
//										String elemType = null;
//										if (varType.isArray()) {
//											elemType = varType.getCanonicalType(true, false);
//											if (elemType != null && elemType.startsWith("@")) {
//												elemType = elemType.substring(1);
//											}
//											// START KGU #784 2019-12-02: varName is only part of the left side, there may be indices, so reduce the type if so
//											int posIdx = tokens.indexOf(varName)+1;
//											StringList indices = tokens.subSequence(posIdx, posAsgn);
//											while (elemType.startsWith("@") && indices.indexOf("[") == 0) {
//												elemType = elemType.substring(1);
//												StringList indexList = Element.splitExpressionList(indices.subSequence(1, indices.count()), ",", true);
//												indexList.remove(0); // Drop first index expression (has already been handled)
//												// Are there perhaps more indices within the same bracket pair (comma-separated list)?
//												while (indexList.count() > 1 && elemType.startsWith("@")) {
//													indexList.remove(0);
//													elemType = elemType.substring(1);
//												}
//												if (indexList.isEmpty()) {
//													indices.clear();
//												}
//												else if (indexList.get(0).trim().startsWith("]")) {
//													// This should be the tail
//													indices = Element.splitLexically(indexList.get(0).substring(1), true);
//												}
//											}
//											// END KGU #784 2019-12-02
//										}
//										transfExpr = this.transformOrGenerateArrayInit(line, items.subSequence(0, items.count()-1), _indent, isDisabled, elemType);
//										if (transfExpr == null) {
//											break;	// FIXME FIXME FIXME
//										}
//									}
//								}
//								
//							}
//						}
//					}
					lineDone = generateAssignment(tokens, line, _indent, isDisabled);
// END KGU#395 2024-04-13
				}
				// START KGU#395 2024-04-13: Enh. #357
				else if (Instruction.isOutput(line)) {
					String keyOutput = CodeParser.getKeyword("output");
					tokens = Element.splitLexically(line.substring(keyOutput.length()).trim(), true);
					tokens.removeAll(" ");
					StringList exprs = Element.splitExpressionList(tokens, ",", true);
					if (exprs.count() == 1 && exprs.get(0).isBlank()) {
						addCode("DISPLAY \"\"", _indent, isDisabled);
					}
					else {
						String varName = "out-" + Integer.toHexString(_inst.hashCode()) + "-";
						for (int j = 0; j < exprs.count()-1; j++) {
							tokens = Element.splitLexically(exprs.get(j), true);
							if (tokens.count() == 1) {
								addCode("DISPLAY " + tokens.get(0), _indent, isDisabled);
							}
							else {
								// TODO insert a declaration if not done
								StringList asgnmt = tokens.copy();
								asgnmt.insert(varName, 0);
								asgnmt.insert("<-", 1);
								generateAssignment(asgnmt, varName + " <- " + tokens.concatenate(null), _indent, isDisabled);
								addCode("DISPLAY " + varName, _indent, isDisabled);
							}
						}
					}
					lineDone = true;
				}
				// END KGU#395 2024-04-13
				// TODO
				if (!lineDone) {
					addCode("INSTRUCTION STILL NOT IMPLEMENTED!", _indent, isDisabled);
				}
			}
		}
	}
	
	// START KGU#395 2024-04-14: Enh. #357 Extracted from generateCode(Instruction, String)
	/**
	 * Appends the COBOL code lines representing the variable assignment given
	 * by the unified token list {@code tokens} and the original instruction
	 * line {@code line}.
	 * 
	 * @param tokens - the unified token list of the given {@code line}
	 * @param line - the original line
	 * @param _indent - the current indentation as string
	 * @param isDisabled - whether the underlying element is disabled
	 */
	private boolean generateAssignment(StringList tokens, String line, String _indent, boolean isDisabled)
	{
		tokens.removeAll(" ");
		String transfLine = transform(line);
		int posAsgn = tokens.indexOf("<-");
		// FIXME Somehow we must find out what data type is transferred... -> #800
		String varName = transform(Instruction.getAssignedVarname(tokens, false));
		StringList exprTokens = tokens.subSequence(posAsgn+1, tokens.count());
		boolean isVar = isVariable(exprTokens, true, typeMap);
		if (varNames.contains(varName)) {
			String target = Instruction.getAssignedVarname(tokens, true);
			TypeMapEntry varType = typeMap.get(varName);
			// FIXME This is all awful without syntax trees (#800)
			if (varType != null && (varType.isArray() || varType.isRecord())) {
				if (isVar) {
					transfLine = "MOVE " + transform(exprTokens.concatenate(null)) + " CORRESPONDING TO " + transform(target);
				}
				else if (exprTokens.contains("{") && exprTokens.contains("}")) {
					// Should be an initializer - so we will have to decompose it
					// The code below was basically copied from CGenerator
					int posBrace = exprTokens.indexOf("{");
					// FIXME This code is incomllete and not ready
					if (posBrace >= 0 && posBrace <= 1 && exprTokens.get(exprTokens.count()-1).equals("}")) {
						String transfExpr = null;
						if (posBrace == 1 && exprTokens.count() >= 3 && Function.testIdentifier(exprTokens.get(0), true, null)) {
							String typeName = exprTokens.get(0);							
							TypeMapEntry recType = this.typeMap.get(":"+typeName);
							if (recType != null && recType.isRecord()) {
								// transforms the Structorizer record initializer into a COBOL-conform one
								transfExpr = this.transformRecordInit(exprTokens, recType);
							}
						}
						else if (posBrace == 0) {
							// Seems to be an array initializer so decompose it to singulary assignments
							/* (The alternative would have been to fake an initialized array declaration
							 * in the data section and to  
							 */
							StringList items = Element.splitExpressionList(exprTokens.subSequence(1, exprTokens.count()), ",", true);
							String elemType = null;
							if (varType.isArray()) {
								elemType = varType.getCanonicalType(true, false);
								if (elemType != null && elemType.startsWith("@")) {
									elemType = elemType.substring(1);
								}
								// START KGU #784 2019-12-02: varName is only part of the left side, there may be indices, so reduce the type if so
								int posIdx = tokens.indexOf(varName)+1;
								StringList indices = tokens.subSequence(posIdx, posAsgn);
								while (elemType.startsWith("@") && indices.indexOf("[") == 0) {
									elemType = elemType.substring(1);
									StringList indexList = Element.splitExpressionList(indices.subSequence(1, indices.count()), ",", true);
									indexList.remove(0); // Drop first index expression (has already been handled)
									// Are there perhaps more indices within the same bracket pair (comma-separated list)?
									while (indexList.count() > 1 && elemType.startsWith("@")) {
										indexList.remove(0);
										elemType = elemType.substring(1);
									}
									if (indexList.isEmpty()) {
										indices.clear();
									}
									else if (indexList.get(0).trim().startsWith("]")) {
										// This should be the tail
										indices = Element.splitLexically(indexList.get(0).substring(1), true);
									}
								}
								// END KGU #784 2019-12-02
							}
							transfExpr = this.transformOrGenerateArrayInit(line, items.subSequence(0, items.count()-1), _indent, isDisabled, elemType);
							if (transfExpr == null) {
								return false;	// FIXME FIXME FIXME
							}
						}
					}
					// TODO Compose transfLine
				}
			}
			// FIXME The test should better/also analyse the expression structuer
			else if (varType != null && varType.isNumeric()) {
				String transfExpr = transform(exprTokens.concatenate(null)); 
				transfLine = "COMPUTE " + transform(varName) + " = " + transfExpr;
			}
		}
		addCode(transfLine, _indent, isDisabled);
		return true;
	}
	// END KGU#395 2024-04-13
	
	/**
	 * Derives or directly appends the COBOL code for the Array initialisation
	 * given by string {@code line} where {@code arrayItems} is the list of the
	 * element values of the array.
	 * 
	 * @param line - the original instruction line
	 * @param arrayItems - the items of the array initialisation expression
	 * @param _indent - the current code indentation as string
	 * @param isDisabled - whether the containing element is disabled
	 * @param elemType - description of the data type of the array elements
	 * @return either a string that contains the COBOL instruction or {@code null}
	 *    if the COBOL representation was so complex that it had to be appended
	 *    directly to {@link Generator#code}.
	 */
	private String transformOrGenerateArrayInit(String line, StringList arrayItems, String _indent, boolean isDisabled,
			String elemType) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Derives or directly appends the COBOL code for the Array initialisation
	 * given by string {@code line} where {@code arrayItems} is the list of the
	 * element values of the array.
	 * 
	 * @param exprTokens - the tokenized expression
	 * @param recType - {@link TypeMapEntry} for the recod type
	 * @return
	 */
	private String transformRecordInit(StringList exprTokens, TypeMapEntry recType) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void generateCode(Alternative _alt, String _indent)
	{
		boolean isDisabled = _alt.isDisabled(false);

		appendComment(_alt, _indent);

		//String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
		String condition = transform(_alt.getUnbrokenText().getLongString()).trim();
		
		// TODO: check for File API needs
		// START KGU#1145 2024-04-12: Issue #1148 treatment for ELSE-IF chains
		final int EVAL_THRESHOLD = 2; // Min. number of else-if to prefer EVALUATE
		int elseifCount = 0;
		Alternative alt1 = _alt;
		while (elseifCount < EVAL_THRESHOLD
				&& alt1.qFalse.getSize() == 1
				&& alt1.qFalse.getElement(0) instanceof Alternative) {
			elseifCount++;
			alt1 = (Alternative)alt1.qFalse.getElement(0);
		}
		if (elseifCount >= EVAL_THRESHOLD) {
			addCode("EVAUATE TRUE", _indent, isDisabled);
			addCode("WHEN " + condition, _indent, isDisabled);
			generateCode(_alt.qTrue, _indent+this.getIndent());
			Element ele = null;
			// We must cater for the code mapping of the chained sub-alternatives
			Stack<Element> processedAlts = new Stack<Element>();
			Stack<Integer> storedLineNos = new Stack<Integer>();
			while (_alt.qFalse.getSize() == 1
					&& (ele = _alt.qFalse.getElement(0)) instanceof Alternative) {
				_alt = (Alternative)ele;
				// We must care for the code mapping explicitly here since we circumvent generateCode()
				markElementStart(_alt, _indent, processedAlts, storedLineNos);
				appendComment(_alt, _indent);
				condition = transform(_alt.getUnbrokenText().getLongString()).trim();
				addCode("WHEN " + condition, _indent, isDisabled);
				generateCode(_alt.qTrue, _indent+this.getIndent());
			}
			if (_alt.qFalse.getSize() > 0) {
				addCode("WHEN OTHER", _indent, isDisabled);
				generateCode(_alt.qFalse, _indent+this.getIndent());
			}
			addCode("END-EVALUATE", _indent, isDisabled);
			markElementEnds(processedAlts, storedLineNos);
		}
		else {
			// END KGU#1145 2024-04-12
			addCode("IF " + condition, _indent, isDisabled);
			addCode("THEN", _indent, isDisabled);

			generateCode(_alt.qTrue, _indent+this.getIndent());

			if (_alt.qFalse.getSize() > 0) {
				addCode("ELSE", _indent, isDisabled);
				generateCode(_alt.qFalse, _indent+this.getIndent());
			}
			addCode("END-IF", _indent, isDisabled);
			// START KGU#1145 2024-04-12: Issue #1148 treatment for ELSE-IF chains
		}
		// END KGU#1145 2024-04-12
		// code.add(_indent+"");
	}

	@Override
	protected void generateCode(Case _case, String _indent)
	{
		boolean isDisabled = _case.isDisabled(false);
		String indent1 = _indent + this.getIndent();
		String indent2 = indent1 + this.getIndent();

		appendComment(_case, _indent);

		StringList lines = _case.getUnbrokenText();

		String condition = transform(lines.get(0));

		addCode("EVALUATE " + condition, _indent, isDisabled);

		for(int i = 0; i < _case.qs.size(); i++)
		{
			StringList constants = StringList.explode(lines.get(i + 1), ",");
			addCode("WHEN = " + constants.concatenate(" OR IS "), indent1, isDisabled);
			generateCode((Subqueue) _case.qs.get(i), indent2);
		}

		if (!lines.get(_case.qs.size()).trim().equals("%") && _case.qs.get(_case.qs.size()-1).getSize() > 0)
		{
			addCode("WHEN OTHER", indent1, isDisabled);
			generateCode(_case.qs.get(_case.qs.size()-1), indent2);
		}

		addCode("END-EVALUATE " + condition, _indent, isDisabled);
	}

	@Override
	protected void generateCode(For _for, String _indent)
	{
		boolean isDisabled = _for.isDisabled(false);
		String bodyName = "";
		if (this.optionFixedSourceFormat()) {
			bodyName = transformName("body_" + Integer.toHexString(_for.hashCode()));
		}
		appendComment(_for, _indent);
		if (_for.isForInLoop())
		{
			// There aren't many ideas how to implement this here in general,
			// but subclasses may have better chances to do so.
			if (generateForInCode(_for, _indent)) return;
		}
		
		String varName = transformName(_for.getCounterVar());
		String startVal = transform(_for.getStartValue());
		int step = _for.getStepConst();
		String endVal = transform(_for.getEndValue());
		
		addCode("PERFORM " + bodyName + " TEST BEFORE", _indent, isDisabled);
		addCode("VARYING " + varName + " FROM "	+ startVal
				+ " BY " + step
				+ " UNTIL " + varName + (step > 0 ? " > " : " < ") + endVal,
				_indent + this.getIndent(), isDisabled);

		processProcedure(_for.getBody(), bodyName, _indent, isDisabled);
	}

	/**
	 * @param _for
	 * @param _indent
	 * @return
	 */
	private boolean generateForInCode(For _for, String _indent) {
		// TODO
		addCode("FOR-IN STILL NOT IMPLEMENTED!", _indent, true);
		
		return true;
	}
	
	@Override
	protected void generateCode(While _while, String _indent)
	{
		boolean isDisabled = _while.isDisabled(false);
		String bodyName = "";
		if (this.optionFixedSourceFormat()) {
			bodyName = transformName("body_" + Integer.toHexString(_while.hashCode()));
		}
		appendComment(_while, _indent);
		String cond = Element.negateCondition(_while.getUnbrokenText().getLongString());
		addCode("PERFORM " + bodyName + " TEST BEFORE UNTIL " + transform(cond),
				_indent, isDisabled);

		processProcedure(_while.getBody(), bodyName, _indent, isDisabled);
	}

	@Override
	protected void generateCode(Repeat _repeat, String _indent)
	{
		boolean isDisabled = _repeat.isDisabled(false);
		String bodyName = "";
		if (this.optionFixedSourceFormat()) {
			bodyName = transformName("body_" + Integer.toHexString(_repeat.hashCode()));
		}
		appendComment(_repeat, _indent);
		String cond = _repeat.getUnbrokenText().getLongString();
		addCode("PERFORM " + bodyName + " TEST AFTER UNTIL " + transform(cond),
				_indent, isDisabled);

		processProcedure(_repeat.getBody(), bodyName, _indent, isDisabled);
	}

	@Override
	protected void generateCode(Forever _forever, String _indent)
	{
		boolean isDisabled = _forever.isDisabled(false);
		String bodyName = "";
		if (this.optionFixedSourceFormat()) {
			bodyName = transformName("body_" + Integer.toHexString(_forever.hashCode()));
		}
		appendComment(_forever, _indent);
		addCode("PERFORM " + bodyName + " FOREVER", _indent, isDisabled);
		
		processProcedure(_forever.q, bodyName, _indent, isDisabled);
	}

	/**
	 * @param _sq
	 * @param _bodyName
	 * @param _indent
	 * @param _isDisabled
	 */
	private void processProcedure(Subqueue _sq, String _procName, String _indent, boolean _isDisabled) {
		if (_procName.isEmpty()) {
			// In free format we will use the inline format as we may indent
			generateCode(_sq, _indent + this.getIndent());
			addCode("END-PERFORM", _indent, _isDisabled);
		}
		else {
			// In fixed format it may be better to place the body as procedure
			postponedProcedures.put(_procName, _sq);
		}
	}
		
	@Override
	protected void generateCode(Call _call, String _indent)
	{
		/*
		 * Thanks to the decision to confine both procedure and function
		 * calls within Call elements in Structorizer, we seem to be in
		 * the comfortable situation to handle all result passing via an
		 * additional reference parameter without having to distinguish
		 * between numerical and non-numerical result types.
		 * But the devil is in the details. As the user is not forced to
		 * declare a result type in the function header and it is not
		 * necessary to assign a provided function result to a variable
		 * (if the function has side effects the user might just ignore
		 * the returned result such that the Call looks like a procedure
		 * call). In the event we might miss to append the additional
		 * reference parameter with the consequence of a wrong signature.
		 * Moreover it is unclear whether the returning of string result
		 * may be transformed to filling it as a reference parameter.
		 * So the code generated here will not be 100 % adequate in some
		 * cases.
		 * We prepared a set of auxiliary methods isNumericType(String),
		 * isNumericType(TypeMapEntry) and hasNumericResult(Root) for
		 * the potential distinction but they will initially all return
		 * false. The challenge is to ensure their consistency no matter
		 * whether we are handling the Root or a Call.
		 */
		boolean isDisabled = _call.isDisabled(false);
		StringList lines = _call.getUnbrokenText();
		appendComment(_call, _indent);
		Root owningRoot = Element.getRoot(_call);
		
		//addCode("CALL STILL NOT IMPLEMENTED!", _indent, true);
		for (int i = 0; i < lines.count(); i++) {
			String line = lines.get(i);
			Function called = _call.getCalledRoutine(i);
			if (called == null) {
				appendComment(line, _indent);
			}
			else {
				StringList paramTypes = new StringList();
				StringList defaults = new StringList();
				boolean hasNumResult = false;
				/* The following will only be exact if we assume that a function
				 * result will always be assigned i.e. the use doesn't call a
				 * function without being interested in its result (see method
				 * top comment).
				 */
				boolean isFct = Call.isAssignment(line);
				String target = null;
				if (isFct) {
					StringList tokens = Element.splitLexically(line, true);
					tokens.removeAll(" ");
					target = transform(Call.getAssignedVarname(tokens, true));
				}
				if (routinePool != null) {
					java.util.Vector<Root> callCandidates = routinePool.findRoutinesBySignature(called.getName(), called.paramCount(), owningRoot, false);
					if (callCandidates.size() > 0) {
						Root sub = callCandidates.get(0);	// TODO better selection strategy?
						sub.collectParameters(null, paramTypes, defaults);
						hasNumResult = hasNumericResult(sub);
					}
				}
				// Procedure call (or function call without getting the result)
				String transf = "CALL " + this.transformName(called.getName());
				int nArgs = called.paramCount();
				if (nArgs > 0 || paramTypes != null && !paramTypes.isEmpty() || isFct && !hasNumResult) {
					transf += " USING";
					addCode(transf, _indent, isDisabled);
					// Now handle the given arguments
					for (int j = 0; j < called.paramCount(); j++) {
						String arg = called.getParam(j);
						boolean isConst = false;
						boolean isCompound = false;
						String paramType = null;
						transf = "";
						if (paramTypes != null && paramTypes.count() > j && (paramType = paramTypes.get(j)) != null) {
							isConst = paramType.startsWith("const ");
							isCompound = paramType.contains("[") || paramType.toLowerCase().startsWith("array ");
							if (Function.testIdentifier(paramType, true, "") && typeMap.containsKey(":" + paramType)) {
								TypeMapEntry parType = typeMap.get(":" + paramType);
								if (parType != null && (parType.isArray() || parType.isRecord())) {
									isCompound = true;
								}
							}
						}
						if (this.varNames.contains(arg)) {
							TypeMapEntry argType = typeMap.get(arg);
							if (argType != null && (argType.isArray() || argType.isRecord())) {
								isCompound = true;
							}
						}
						if (isCompound && !isConst) {
							transf += "BY REFERENCE ";
						}
						else {
							transf += "BY CONTENT ";
						}
						/* FIXME: We are in trouble if arg is an expression, since only
						 * variables or literals may be passed to routines - so we may
						 * have to assign the value of the expression to a variable in
						 * advance. But then this variable should already have been declared
						 * (or we should always declare a set of dummy variables for this
						 * case).
						 */
						addCode(transf + transform(arg), _indent + this.getIndent(), isDisabled);
					}
					// Now handle the omitted arguments
					for (int j = nArgs; j < paramTypes.count(); j++) {
						String deflt = null;
						if (j < defaults.count() && (deflt = defaults.get(j)) != null && !deflt.isEmpty()) {
							addCode("BY CONTENT " + transform(deflt), _indent + getIndent(), isDisabled);
						}
						else {
							addCode("OMITTED", _indent + getIndent(), isDisabled);
						}
					}
					// Last but not least ensure the result variable is passed by reference
					if (isFct && !hasNumResult) {
						addCode("BY REFERENCE " + target, _indent + getIndent(), isDisabled);
					}
				}
				else {
					addCode(transf, _indent, isDisabled);
				}
				if (isFct && hasNumResult) {
					addCode("RETURNING " + target, _indent + getIndent(), isDisabled);
				}
			}
		}
	}

	/**
	 * Tries to decide whether the function {@code sub} has a simple numeric
	 * result i.e. whether the result may be "RETURNING" or has to be obtained
	 * via additional reference parameter.
	 * @param sub - a {@link Root} of type subRoutine
	 * @return true if the result is primitive-numeric, false otherwise
	 */
	private boolean hasNumericResult(Root sub) {
		boolean isNumeric = false;
		/* For consistency between handling a Root and a Call to the same
		 * Root we must not make use of analysis information that is only
		 * available while we are processing sub as current Root.
		 */
		String resultType = sub.getResultType();
		if (resultType != null && !resultType.isEmpty() && !resultType.equals("???")) {
			TypeMapEntry typeInfo = typeMap.get(":" + resultType);
			if (typeInfo != null) {
				isNumeric = isNumericType(typeInfo);
			}
			else {
				isNumeric = isNumericType(transformType(resultType, "???"));
			}
		}
		return isNumeric;
	}
	
	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		// TODO
		addCode("EXIT STILL NOT IMPLEMENTED!", _indent, true);
		
		// code.add(_indent+"");
	}

	@Override
	protected void generateCode(Parallel _para, String _indent)
	{
		// TODO
		addCode("PARALLEL STILL NOT IMPLEMENTED!", _indent, true);
		
		// code.add(_indent+"");
		for(int i = 0; i < _para.qs.size(); i++)
		{
			// code.add(_indent+"");
			generateCode((Subqueue) _para.qs.get(i), _indent+this.getIndent());
			// code.add(_indent+"");
		}
		// code.add(_indent+"");
	}
		
	protected void generateCode(Try _try, String _indent)
	{
		/* FIXME this should somehow be converted to a "declarative procedure" declaration,
		 * something like:
		 * USE AFTER STANDARD EXCEPTION PROCEDURE ON ??? <PARAGR_NAME>
		 * */
		super.generateCode(_try, _indent);
	}
	
	/**
	 * Composes the heading for the program or function according to the
	 * COBOL language specification - this is actually the Identification DIVISION
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		postponedProcedures.clear();
		if (topLevel && !this.optionFixedSourceFormat()) {
			code.add("       >> SOURCE FORMAT IS FREE");
		}
		if (!topLevel) {
			addCode("", _indent, false);
		}
		this.appendComment(_root.getText(), _indent);
		this.appendComment(_root.getComment(), _indent);
		if (topLevel) {
			this.appendComment("", _indent);
			this.appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
		}
		appendCopyright(_root, _indent, _public);
		// IMPORT diagrams go to storage only (copybook)?
		if (!_root.isInclude()) {
			addCode("IDENTIFICATION DIVISION.", _indent, false);
			// FIXME: in COBOL all mains and subs are programs, functions are special programs that have a RETURNING clause and return something
			addCode((_root.isProgram() ? "PROGRAM-ID. " : "FUNCTION-ID. ") + this.transformName(_root.getMethodName()) + ".", _indent, false);
			addCode("", _indent, false);

			// If we had something to tell then here might be the ENVIRONMENT DIVISION
			// (possibly for declaring the repository?)
			if (this.subMap.containsKey(_root)) {
				addCode("ENVIRONMENT DIVISION.", _indent, false);
				addCode("CONFIGURATION SECTION.", _indent, false);
				// FIXME: in COBOL repository includes all FUNCTIONS that may be called without the FUNCTION keyword, something we may want to do
				addCode("REPOSITORY.", _indent, false);
				for (String subName: this.subMap.get(_root)) {
					addCode("FUNCTION " + this.transformName(subName), _indent + this.getIndent(), false);
				}
				addCode(".", _indent + this.getIndent(), false);
			}
		} else {
			this.appendComment("IMPORT diagram: data declarations to be put in a copybook", _indent);
			this.appendComment("", _indent);
		}
		if (topLevel) {
			// line indices like this must not be manipulated except in topLevel mode!
			this.subroutineInsertionLine = code.count();
		}
		return _indent;
	}

	/**
	 * Generates some preamble (i.e. comments, language declaration section etc.)
	 * and adds it to this.code.<br/>
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param varNames - list of variable names introduced inside the body
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		addCode("", _indent, false);
		addCode("DATA DIVISION.", _indent, false);
		addCode("WORKING-STORAGE SECTION.", _indent, false);
		appendComment("TODO: Check and accomplish variable declarations:", _indent);
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//this.typeMap = (HashMap<String, TypeMapEntry>)_root.getTypeInfo().clone();
		// START KGU#852 2020-04-22: Bugfix #854 - we must ensure topological order on export
		//this.typeMap = (HashMap<String, TypeMapEntry>) _root.getTypeInfo(routinePool).clone();
		this.typeMap = (LinkedHashMap<String, TypeMapEntry>) _root.getTypeInfo(routinePool).clone();
		// END KGU#852 2020-04-22
		// END KGU#676 2019-03-30
		// special treatment of constants
		for (String constName: _root.constants.keySet()) {
			appendDeclaration(_root, constName, _indent);			
		}
		// List the variables to be declared
		for (int v = 0; v < varNames.count(); v++) {
			//insertComment(varNames.get(v), _indent);
			String varName = varNames.get(v);
			if (!_root.constants.containsKey(varName)) {
				appendDeclaration(_root, varName, _indent);
			}
		}
		//generateIOComment(_root, _indent);
		addCode("", _indent, false);
		// Starts the PROCEDURE DIVISION
		String procdiv = "PROCEDURE DIVISION";
		StringList params = _root.getParameterNames();
		boolean hasNumericResult = false;
		if (returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) {
			// We will enforce that the effective result variable will be named "result"
			// But in order to find out the result type we must know which variable to
			// check
			String resultName = "result";
			String resultType = _root.getResultType();
			if (resultType != null) {
				hasNumericResult = isNumericType(resultType);
			}
			else {
				if (isFunctionNameSet && !isResultSet) {
					resultName = _root.getMethodName();
				}
				TypeMapEntry resType = typeMap.get(resultName);
				if (resType != null && isNumericType(resType)) {
					hasNumericResult = true;
				}
			}
			if (!hasNumericResult) {
				params.add("result");
			}
		}
		if (_root.isSubroutine() || params.count() > 0) {
			procdiv += " USING " + params.concatenate(" ");
		}
		if (hasNumericResult) {
			procdiv += " RETURNING result";
		}
		procdiv += ".";
		addCode(procdiv, _indent, false);
		if (_root.isSubroutine() || params.count() > 0) {
			addCode("LINKAGE SECTION.", _indent, false);
			for (int v = 0; v < params.count(); v++) {
				//insertComment(varNames.get(v), _indent);
				String varName = params.get(v);
				if (!_root.constants.containsKey(varName)) {
					appendDeclaration(_root, varName, _indent);
				}
			}
		}
		addCode("", _indent, false);
		
		return _indent;
	}
	
	/**
	 * Tries to decide whether the given type is primitive numeric (i.e.
	 * a type that can be used by a COBOL function to RETURN a value).
	 * @param _typeEntry - a {@link TypeMapEntry} for the type to be
	 * checked
	 * @return true if the given {@code _typeEntry} designates a
	 * primitive numeric type
	 * @see #isNumericType(String)
	 * @see #hasNumericResult(Root)
	 */
	private boolean isNumericType(TypeMapEntry _typeEntry) {
		// TODO Auto-generated method stub
		// What about strings here?
		return !_typeEntry.isArray() && !_typeEntry.isRecord();
	}
	/**
	 * Tries to decide whether the given type is primitive numeric (i.e.
	 * a type that can be used by a COBOL function to RETURN a value).
	 * @param _transfTypeName - a type name transformed to COBOL syntax
	 * @return true if the given {@code _transfTypeName} designates a
	 * primitive numeric type
	 * @see #isNumericType(TypeMapEntry)
	 * @see #hasNumericResult(Root)
	 */
	private boolean isNumericType(String _transfTypeName) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Creates the appropriate code for returning a required result and adds it
	 * (after the algorithm code of the body) to this.code)
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param alwaysReturns - whether all paths of the body already force a return
	 * @param varNames - names of all assigned variables
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		addCode("CODE SECTION.", _indent, false);	// start with any section name (???)
		if (_root.isSubroutine() &&
				(returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
		{
			if (isFunctionNameSet && !isResultSet)
			{
				String resultName = _root.getMethodName();
				String option = "";
				if (!hasNumericResult(_root)) {
					option = "CORRESPONDING ";
				}
				addCode("MOVE " + option + resultName + " TO result", _indent, false);
			}
		}
		addCode("GOBACK", _indent, false);	// return to the caller (which may be the OS)
		addCode(".", _indent, false);	// Ends the current section and the PROCEDURE DIVISION
		return _indent;
	}

	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root 
	 * @param _indent
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// Nothing to do here for IMPORT diagrams.
		if (!_root.isInclude()) {
			addCode("", _indent, false);
			// FIXME: in COBOL all mains and subs are programs, functions are special programs that have a RETURNING clause and return something
			addCode("END " + (_root.isProgram() ? "PROGRAM " : "FUNCTION ") + this.transformName(_root.getMethodName()) + ".", _indent, false);
		}
		
		// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
		if (topLevel) {
			// line indices like this must not be manipulated except in topLevel mode!
			this.subroutineInsertionLine = code.count();
			libraryInsertionLine = code.count();
		}
		// END KGU#815/KGU#824 2020-03-19
	}

}

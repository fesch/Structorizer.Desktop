/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

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
 *      Description:    This class generates COBOL code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Simon Sobisch           2017.04.14      First Issue
 *      Kay Gürtzig             2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      
 ******************************************************************************************************
 *
 *      Comment:  /
 *      
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;
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
	private final HashMap<Root, HashSet<String>> subMap = new HashMap<Root, HashSet<String>>();
	private HashMap<String, TypeMapEntry> typeMap; 

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
			this.subMap.get(_caller).add(_called.getName());
		}
		return super.registerCalled(_call, _caller);
	}
	
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
	
	/************ Code Generation **************/
	
	// START KGU#388/KGU#542 2019-12-04: Enh. #423, #739
	@Override
	protected String transformType(String _type, String _default)
	{
		if (_type == null) {
			_type = _default;
		}
		else if (_type.equals("const")) {
			_type = "78 ";
		}
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
			addCode("struct " + _type.typeName + " {", _indent, _asComment);
			for (Entry<String, TypeMapEntry> compEntry: _type.getComponentInfo(false).entrySet()) {
				addCode(transformTypeFromEntry(compEntry.getValue(), _type) + "\t" + compEntry.getKey() + ";",
						indentPlus1, _asComment);
			}
			addCode("};", _indent, _asComment);
		}
		else if (_type.isEnum()) {
			StringList items = _type.getEnumerationInfo();
			appendComment("enum " + _type.typeName, _indent);
			for (int i = 0; i < items.count(); i++) {
				// FIXME: We might have to transform the value...
				addCode(items.get(i) + (i < items.count() -1 ? "," : ""), indentPlus1, _asComment);
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
		String transfConst = transformType("const", "");
		if (typeInfo != null) {
			 types = getTransformedTypes(typeInfo, false);
		}
		// START KGU#375 2017-04-12: Enh. #388: Might be an imported constant
		else if (constValue != null) {
			String type = Element.identifyExprType(typeMap, constValue, false);
			if (!type.isEmpty()) {
				types = StringList.getNew(transformType(type, "int"));
				// We place a faked workaround entry
				typeMap.put(_name, new TypeMapEntry(type, null, null, _root, 0, true, false));
			}
		}
		// If the type is unambiguous and has no C-style declaration or may not be
		// declared between instructions then add the declaration here
		if (types != null && types.count() == 1 && typeInfo != null) {			
			String decl = types.get(0).trim();
			// START KGU#375 2017-04-12: Enh. #388
			if (decl.equals(transfConst) && constValue != null) {
				// The actual type spec is missing but we try to extract it from the value
				decl += " VALUE " + Element.identifyExprType(typeMap, constValue, false);
				decl = decl.trim();
			}
			// END KGU#375 2017-04-12
			if (decl.startsWith("@")) {
				decl = makeArrayDeclaration(decl, _name, typeInfo);
			}
			else {
				// FIXME: handle records
				decl = "01 " + cobName + "\t" + decl;
			}
			if (_root.constants.containsKey(_name)) {
				if (!decl.contains(transfConst + " ")) {
					decl = transfConst + " " + decl;
				}
				if (constValue != null) {
					decl += " " + transform(constValue);
				}
			}
			// END KGU#375 2017-04-12
			if (decl.contains("???")) {
				appendComment(decl + ".", _indent);
			}
			else {
				addCode(decl + ".", _indent, false);
			}
		}
		// Add a comment if there is no type info
		else if (types == null){
			appendComment(cobName, _indent);
		}
		// END KGU#261/KGU#332 2017-01-16
	}
	// END KGU#375 2017-04-12
	
	// START KGU#332 2017-01-30: Decomposition of generatePreamble to ease sub-classing
	private String makeArrayDeclaration(String _elementType, String _varName, TypeMapEntry typeInfo)
	{
//		int nLevels = _elementType.lastIndexOf('@')+1;
//		_elementType = (_elementType.substring(nLevels) + " " + _varName).trim();
//		for (int i = 0; i < nLevels; i++) {
//			int maxIndex = typeInfo.getMaxIndex(i);
//			_elementType += "[" + (maxIndex >= 0 ? Integer.toString(maxIndex+1) : (i == 0 ? "" : "/*???*/") ) + "]";
//		}
		return _elementType;
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
		appendCopyright(_root, _indent, true);
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
	 * and adds it to this.code.
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
		this.typeMap = (HashMap<String, TypeMapEntry>) _root.getTypeInfo(routinePool).clone();
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
		if (_root.isSubroutine() || params.count() > 0) {
			procdiv += " USING " + params.concatenate(" ");
		}
		if (returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) {
			// Now the good question is how to guess the name of the return variable and what to do
			// if the diagram used return.
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
		addCode("CODE SECTION.", _indent, false);	// start with any section name
		if (_root.isProgram() && !alwaysReturns)
		{
//			code.add(_indent);
//			code.add(_indent + "return 0;");
		}
		else if (_root.isSubroutine() &&
				(returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
		{
//			String result = "0";
//			if (isFunctionNameSet)
//			{
//				result = _root.getMethodName();
//			}
//			else if (isResultSet)
//			{
//				int vx = varNames.indexOf("result", false);
//				result = varNames.get(vx);
//			}
//			code.add(_indent);
//			code.add(_indent + "return " + result + ";");
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

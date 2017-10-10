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
 *      
 ******************************************************************************************************
 *
 *      Comment:  /
 *      
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.HashSet;

import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.executor.Function;

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
	 * @see lu.fisch.structorizer.generators.Generator#getReservedWords()
	 */
	// TODO: move to plugin file (in all Generators), allowing a change without
	// recompiling the jar
	private static final String[] reservedWords = new String[] { "ACCEPT", "ACCESS", "ACTIVE-CLASS", "ADD", "ADDRESS",
			"ADVANCING", "AFTER", "ALIGNED", "ALL", "ALLOCATE", "ALPHABET", "ALPHABETIC", "ALPHABETIC-LOWER",
			"ALPHABETIC-UPPER", "ALPHANUMERIC", "ALPHANUMERIC-EDITED", "ALSO", "ALTER", "ALTERNATE", "AND", "ANY",
			"ANYCASE", "ARE", "AREA", "AREAS", "ARGUMENT-NUMBER", "ARGUMENT-VALUE", "ARITHMETIC", "AS", "ASCENDING",
			"ASCII", "ASSIGN", "AT", "ATTRIBUTE", "AUTO", "AUTOMATIC", "AWAY-FROM-ZERO", "B-AND", "B-NOT", "B-OR",
			"B-XOR", "BACKGROUND-COLOR", "BASED", "BEFORE", "BELL", "BINARY", "BINARY-C-LONG", "BINARY-CHAR",
			"BINARY-DOUBLE", "BINARY-LONG", "BINARY-SHORT", "BIT", "BLANK", "BLINK", "BLOCK", "BOOLEAN", "BOTTOM", "BY",
			"BYTE-LENGTH", "CALL", "CANCEL", "CAPACITY", "CARD-PUNCH", "CARD-READER", "CASSETTE", "CD", "CENTER", "CF",
			"CH", "CHAIN", "CHAINING", "CHARACTER", "CHARACTERS", "CLASS", "CLASS-ID", "CLASSIFICATION", "CLOSE",
			"COBOL", "CODE", "CODE-SET", "COL", "COLLATING", "COLOR", "COLS", "COLUMN", "COLUMNS", "COMMA",
			"COMMAND-LINE", "COMMIT", "COMMON", "COMMUNICATION", "COMP", "COMP-X", "COMP-1", "COMP-2", "COMP-3",
			"COMP-4", "COMP-5", "COMP-6", "COMPUTATIONAL", "COMPUTATIONAL-1", "COMPUTATIONAL-2", "COMPUTATIONAL-3",
			"COMPUTATIONAL-4", "COMPUTATIONAL-5", "COMPUTATIONAL-X", "COMPUTE", "CONDITION", "CONFIGURATION",
			"CONSTANT", "CONTAINS", "CONTENT", "CONTINUE", "CONTROL", "CONTROLS", "CONVERSION", "CONVERTING", "COPY",
			"CORR", "CORRESPONDING", "COUNT", "CRT", "CRT-UNDER", "CURRENCY", "CURSOR", "CYCLE", "DATA", "DATA-POINTER",
			"DATE", "DAY", "DAY-OF-WEEK", "DE", "DEBUGGING", "DECIMAL-POINT", "DECLARATIVES", "DEFAULT", "DELETE",
			"DELIMITED", "DELIMITER", "DEPENDING", "DESCENDING", "DESTINATION", "DETAIL", "DISABLE", "DISC", "DISK",
			"DISPLAY", "DIVIDE", "DIVISION", "DOWN", "DUPLICATES", "DYNAMIC", "EBCDIC", "EC", "ECHO", "EGI", "ELSE",
			"EMI", "ENABLE", "END", "END-ACCEPT", "END-ADD", "END-CALL", "END-CHAIN", "END-COMPUTE", "END-DELETE",
			"END-DISPLAY", "END-DIVIDE", "END-EVALUATE", "END-IF", "END-MULTIPLY", "END-OF-PAGE", "END-PERFORM",
			"END-READ", "END-RECEIVE", "END-RETURN", "END-REWRITE", "END-SEARCH", "END-START", "END-STRING",
			"END-SUBTRACT", "END-UNSTRING", "END-WRITE", "ENTRY", "ENTRY-CONVENTION", "ENVIRONMENT", "ENVIRONMENT-NAME",
			"ENVIRONMENT-VALUE", "EO", "EOL", "EOP", "EOS", "EQUAL", "ERASE", "ERROR", "ESCAPE", "ESI", "EVALUATE",
			"EXCEPTION", "EXCEPTION-OBJECT", "EXCLUSIVE", "EXIT", "EXPANDS", "EXTEND", "EXTERN", "EXTERNAL", "F",
			"FACTORY", "FALSE", "FD", "FILE", "FILE-CONTROL", "FILE-ID", "FILLER", "FINAL", "FIRST", "FIXED",
			"FLOAT-BINARY-128", "FLOAT-BINARY-32", "FLOAT-BINARY-64", "FLOAT-DECIMAL-16", "FLOAT-DECIMAL-34",
			"FLOAT-EXTENDED", "FLOAT-INFINITY", "FLOAT-LONG", "FLOAT-NOT-A-NUMBER", "FLOAT-SHORT", "FOOTING", "FOR",
			"FOREGROUND-COLOR", "FOREVER", "FORMAT", "FREE", "FROM", "FULL", "FUNCTION", "FUNCTION-ID",
			"FUNCTION-POINTER", "GENERATE", "GET", "GIVING", "GLOBAL", "GO", "GOBACK", "GREATER", "GRID", "GROUP",
			"GROUP-USAGE", "HEADING", "HIGH-VALUE", "HIGH-VALUES", "HIGHLIGHT", "I-O", "I-O-CONTROL", "ID",
			"IDENTIFICATION", "IF", "IGNORE", "IGNORING", "IMPLEMENTS", "IN", "INDEX", "INDEXED", "INDICATE",
			"INHERITS", "INITIAL", "INITIALIZE", "INITIALIZED", "INITIATE", "INPUT", "INPUT-OUTPUT", "INSPECT",
			"INTERFACE", "INTERFACE-ID", "INTERMEDIATE", "INTO", "INTRINSIC", "INVALID", "INVOKE", "IS", "JUST",
			"JUSTIFIED", "KEPT", "KEY", "KEYBOARD", "LABEL", "LAST", "LC_ALL", "LC_COLLATE", "LC_CTYPE", "LC_MESSAGES",
			"LC_MONETARY", "LC_NUMERIC", "LC_TIME", "LEADING", "LEFT", "LEFT-JUSTIFY", "LEFTLINE", "LENGTH", "LESS",
			"LIMIT", "LIMITS", "LINAGE", "LINAGE-COUNTER", "LINE", "LINE-COUNTER", "LINES", "LINKAGE", "LOCAL-STORAGE",
			"LOCALE", "LOCK", "LOW-VALUE", "LOW-VALUES", "LOWER", "LOWLIGHT", "MAGNETIC-TAPE", "MANUAL", "MEMORY",
			"MERGE", "MESSAGE", "METHOD", "METHOD-ID", "MINUS", "MODE", "MOVE", "MULTIPLE", "MULTIPLY", "NAME",
			"NATIONAL", "NATIONAL-EDITED", "NATIVE", "NEAREST-AWAY-FROM-ZERO", "NEAREST-EVEN", "NEAREST-TOWARD-ZERO",
			"NEGATIVE", "NESTED", "NEXT", "NO", "NO-ECHO", "NONE", "NORMAL", "NOT", "NOTHING", "NULL", "NULLS",
			"NUMBER", "NUMBERS", "NUMERIC", "NUMERIC-EDITED", "OBJECT", "OBJECT-COMPUTER", "OBJECT-REFERENCE", "OCCURS",
			"OF", "OFF", "OMITTED", "ON", "ONLY", "OPEN", "OPTIONAL", "OPTIONS", "OR", "ORDER", "ORGANIZATION", "OTHER",
			"OUTPUT", "OVERFLOW", "OVERLINE", "OVERRIDE", "PACKED-DECIMAL", "PADDING", "PAGE", "PAGE-COUNTER",
			"PARAGRAPH", "PERFORM", "PF", "PH", "PIC", "PICTURE", "PLUS", "POINTER", "POSITION", "POSITIVE", "PREFIXED",
			"PRESENT", "PREVIOUS", "PRINT", "PRINTER", "PRINTER-1", "PRINTING", "PROCEDURE", "PROCEDURE-POINTER",
			"PROCEDURES", "PROCEED", "PROGRAM", "PROGRAM-ID", "PROGRAM-POINTER", "PROHIBITED", "PROMPT", "PROPERTY",
			"PROTECTED", "PROTOTYPE", "PURGE", "QUEUE", "QUOTE", "QUOTES", "RAISE", "RAISING", "RANDOM", "RD", "READ",
			"RECEIVE", "RECORD", "RECORDING", "RECORDS", "RECURSIVE", "REDEFINES", "REEL", "REFERENCE", "REFERENCES",
			"RELATION", "RELATIVE", "RELEASE", "REMAINDER", "REMOVAL", "RENAMES", "REPLACE", "REPLACING", "REPORT",
			"REPORTING", "REPORTS", "REPOSITORY", "REQUIRED", "RESERVE", "RESET", "RESUME", "RETRY", "RETURN",
			"RETURNING", "REVERSE", "REVERSE-VIDEO", "REVERSED", "REWIND", "REWRITE", "RF", "RH", "RIGHT",
			"RIGHT-JUSTIFY", "ROLLBACK", "ROUNDED", "ROUNDING", "RUN", "S", "SAME", "SCREEN", "SCROLL", "SD", "SEARCH",
			"SECONDS", "SECTION", "SECURE", "SEGMENT", "SEGMENT-LIMIT", "SELECT", "SELF", "SEND", "SENTENCE",
			"SEPARATE", "SEQUENCE", "SEQUENTIAL", "SET", "SHARING", "SHORT", "SIGN", "SIGNED", "SIGNED-INT",
			"SIGNED-LONG", "SIGNED-SHORT", "SIZE", "SORT", "SORT-MERGE", "SOURCE", "SOURCE-COMPUTER", "SOURCES",
			"SPACE", "SPACE-FILL", "SPACES", "SPECIAL-NAMES", "STANDARD", "STANDARD-1", "STANDARD-2", "STANDARD-BINARY",
			"STANDARD-DECIMAL", "START", "STATEMENT", "STATIC", "STATUS", "STDCALL", "STEP", "STOP", "STRING", "STRONG",
			"SUB-QUEUE-1", "SUB-QUEUE-2", "SUB-QUEUE-3", "SUBTRACT", "SUM", "SUPER", "SUPPRESS", "SYMBOL", "SYMBOLIC",
			"SYNC", "SYNCHRONIZED", "SYSTEM-DEFAULT", "SYSTEM-OFFSET", "TAB", "TABLE", "TALLYING", "TAPE", "TERMINAL",
			"TERMINATE", "TEST", "TEXT", "THAN", "THEN", "THROUGH", "THRU", "TIME", "TIME-OUT", "TIMES", "TO", "TOP",
			"TOWARD-GREATER", "TOWARD-LESSER", "TRAILING", "TRAILING-SIGN", "TRANSFORM", "TRUE", "TRUNCATION", "TYPE",
			"TYPEDEF", "U", "UCS-4", "UNBOUNDED", "UNDERLINE", "UNIT", "UNIVERSAL", "UNLOCK", "UNSIGNED",
			"UNSIGNED-INT", "UNSIGNED-LONG", "UNSIGNED-SHORT", "UNSTRING", "UNTIL", "UP", "UPDATE", "UPON", "UPPER",
			"USAGE", "USE", "USER", "USER-DEFAULT", "USING", "UTF-16", "UTF-8", "V", "VAL-STATUS", "VALID", "VALIDATE",
			"VALIDATE-STATUS", "VALUE", "VALUES", "VARIABLE", "VARYING", "WAIT", "WHEN", "WITH", "WORDS",
			"WORKING-STORAGE", "WRITE", "YYYYDDD", "YYYYMMDD", "ZERO", "ZERO-FILL", "ZEROES", "ZEROS" };

	@Override
	public String[] getReservedWords() {
		return reservedWords;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.fisch.structorizer.generators.Generator#isCaseSignificant()
	 */
	@Override
	public boolean isCaseSignificant() {
		return false;
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

	// include / import / uses config
	/*
	 * function for checking and inserting UserIncludes special case for COBOL
	 * copybooks: IncludePattern setting contains the CodePart where the
	 * inclusion should be done, for example
	 * "WS: data1.cpy, data2.cpy; LS: localdata.copy; PD: helpers.cob"
	 */
	protected void insertUserIncludes(CodePart cp) {
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
							code.add(_indent + pattern.replace("%", prepareIncludeItem(copy)));
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
			insertComment(_indent + text, "");
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
	 * @see lu.fisch.structorizer.generators.Generator#insertComment(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	protected void insertComment(String _text, String _indent) {
		String[] lines = _text.split("\n");
		for (String line : lines) {
			code.add(this.getLineStart(true) + line);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lu.fisch.structorizer.generators.Generator#insertBlockComment(lu.fisch.
	 * utils.StringList, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	protected void insertBlockComment(StringList _sl, String _indent, String _start, String _cont, String _end) {
		int oldSize = code.count();
		super.insertBlockComment(_sl, _indent, _start, _cont, _end);
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
	 * Returns the value of the export option to replace undercores by
	 * hyphens in identifiers.
	 * @return true if free file format is to be used.
	 */
	private boolean optionUnderscores2Hyphens()
	{
		Object optionVal = this.getPluginOption("underscores2hyphens", true);
		return !(optionVal instanceof Boolean) || ((Boolean)optionVal).booleanValue();
	}
	// END KGU#395 2017-05-11	
	
	// START KGU#375 2017-04-12: Enh. #388 common preparation of constants and variables
	protected void insertDeclaration(Root _root, String _name, String _indent)
	{

		// FIXME
		TypeMapEntry typeInfo = typeMap.get(_name); 
		StringList types = null;
		String constValue = _root.constants.get(_name);
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
				typeMap.put(_name, new TypeMapEntry(type, null, _root, 0, true, false, true));
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
				decl = "01 " + _name + "\t" + decl;
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
				insertComment(decl + ".", _indent);
			}
			else {
				addCode(decl + ".", _indent, false);
			}
		}
		// Add a comment if there is no type info
		else if (types == null){
			insertComment(_name, _indent);
		}
		// END KGU#261/KGU#332 2017-01-16
	}
	// END KGU#375 2017-04-12
	
	// START KGU#332 2017-01-30: Decomposition of geeratePreamble to ease sub-classing
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

	/************ Code Generation **************/
	
	
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
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		if (topLevel && !this.optionFixedSourceFormat()) {
			code.add("       >> SOURCE FORMAT IS FREE");
		}
		if (!topLevel) {
			addCode("", _indent, false);
		}
		this.insertComment(_root.getText(), _indent);
		this.insertComment(_root.getComment(), _indent);
		if (topLevel) {
			this.insertComment("", _indent);
			this.insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
		}
		insertCopyright(_root, _indent, true);
		addCode("IDENTIFICATION DIVISION.", _indent, false);
		// FIXME: What to do here for IMPORT diagrams?
		addCode((_root.isProgram() ? "PROGRAM-ID. " : "FUNCTION-ID. ") + this.transformName(_root.getMethodName()) + ".", _indent, false);
		addCode("", _indent, false);

		// If we had something to tell then here might be the ENVIRONMENT DIVISION
		// (possibly for declaring the repository?)
		if (this.subMap.containsKey(_root)) {
			addCode("ENVIRONMENT DIVISION.", _indent, false);
			addCode("CONFIGURATION SECTION.", _indent, false);
			addCode("REPOSITORY.", _indent, false);
			for (String subName: this.subMap.get(_root)) {
				addCode("FUNCTION " + this.transformName(subName), _indent + this.getIndent(), false);
			}
			addCode(".", _indent + this.getIndent(), false);
		}
		this.subroutineInsertionLine = code.count();
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
		insertComment("TODO: Check and accomplish variable declarations:", _indent);
		this.typeMap = (HashMap<String, TypeMapEntry>) _root.getTypeInfo().clone();
		// special treatment of constants
		for (String constName: _root.constants.keySet()) {
			insertDeclaration(_root, constName, _indent);			
		}
        // List the variables to be declared
		for (int v = 0; v < varNames.count(); v++) {
			//insertComment(varNames.get(v), _indent);
			String varName = varNames.get(v);
			if (!_root.constants.containsKey(varName)) {
				insertDeclaration(_root, varName, _indent);
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
			procdiv += " RETURNING result.";
		}
		addCode(procdiv, _indent, false);
		
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
		addCode(".", _indent, false);	// Ends the PROCEDURE DIVISION
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
		// FIXME: What do do here for IMPORT diagrams?
		addCode("END " + (_root.isProgram() ? "PROGRAM " : "FUNCTION ") + this.transformName(_root.getMethodName()) + ".", _indent, false);		
		this.subroutineInsertionLine = code.count();
	}

}

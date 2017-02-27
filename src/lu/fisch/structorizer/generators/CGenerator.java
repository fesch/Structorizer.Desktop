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
 *      Author:         Bob Fisch
 *
 *      Description:    This class generates ANSI C code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Bob Fisch               2008.11.17      First Issue
 *      Gunter Schillebeeckx    2009.08.10      Bugfixes (see comment)
 *      Bob Fisch               2009.08.17      Bugfixes (see comment)
 *      Bob Fisch               2010.08-30      Different fixes asked by Kay Gürtzig
 *                                              and Peter Ehrlich
 *      Kay Gürtzig             2010.09.10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.11.06      Support for logical Pascal operators added
 *      Kay Gürtzig             2014.11.16      Bugfixes in operator conversion
 *      Kay Gürtzig             2015.10.18      Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig             2015.10.21      New generator now supports multiple-case branches
 *      Kay Gürtzig             2015.11.01      Language transforming reorganised, FOR loop revision
 *      Kay Gürtzig             2015.11.10      Bugfixes KGU#71 (switch default), KGU#72 (div operators)
 *      Kay Gürtzig             2015.11.10      Code style option optionBlockBraceNextLine() added,
 *                                              bugfix/enhancement #22 (KGU#74 jump and return handling)
 *      Kay Gürtzig             2015.12.13      Bugfix #51 (=KGU#108): Cope with empty input and output
 *      Kay Gürtzig             2015.12.21      Adaptations for Bugfix #41/#68/#69 (=KGU#93)
 *      Kay Gürtzig             2016.01.15      Bugfix #64 (exit instruction was exported without ';')
 *      Kay Gürtzig             2016.01.15      Issue #61/#107: improved handling of typed variables 
 *      Kay Gürtzig             2016.03.16      Enh. #84: Minimum support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig             2016.04.01      Enh. #144: Export option to suppress content conversion 
 *      Kay Gürtzig             2016.04.03      Enh. KGU#150: ord and chr functions converted (raw approach)
 *      Kay Gürtzig             2016.07.20      Enh. #160: Option to involve subroutines implemented (=KGU#178)
 *      Kay Gürtzig             2016.08.10      Issue #227: <stdio.h> and TODOs only included if needed 
 *      Kay Gürtzig             2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2016.09.25      Enh. #253: D7Parser.keywordMap refactored 
 *      Kay Gürtzig             2016.10.14      Enh. 270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016.10.15      Enh. 271: Support for input instructions with prompt
 *      Kay Gürtzig             2016.10.16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig             2016.12.01      Bugfix #301: More sophisticated test for condition enclosing by parentheses
 *      Kay Gürtzig             2016.12.22      Enh. #314: Support for File API
 *      Kay Gürtzig             2017.01.26      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig             2017.01.31      Enh. #113: Array parameter transformation
 *      Kay Gürtzig             2017.02.06      Minor corrections in generateJump(), String delimiter conversion (#343)
 *      Kay Gürtzig             2017.02.27      Enh. #346: Insertion mechanism for user-specific include directives
 *
 ******************************************************************************************************
 *
 *      Comment:
 *
 *      2016.04.01 - Enh. #144 (Kay Gürtzig)
 *      - A new export option suppresses conversion of text content and restricts the export
 *        more or less to the mere control structure generation.
 *        
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-29 - enhancement #23: Sensible handling of Jump elements (break / return / exit)
 *      - return instructions and assignments to variables named "result" or like the function
 *        are registered, such that return instructions may be generated on demand
 *      - "leave" jumps will generate break or goto instructions
 *      - exit instructions are produced as well.
 *      - new methods insertBlockHeading() and insertBlockTail() facilitate code style variation and
 *        subclassing w.r.t. multi-level jump instructions.
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops themselves now provide more reliable loop parameters  
 *      
 *      2015.10.21 - Enhancement KGU#15: Case element with comma-separated constant list per branch
 *      
 *      2015.10.18 - Bugfixes and modificatons (Kay Gürtzig)
 *      - Bugfix: The export option "export instructions as comments" had been ignored before
 *      - An empty Jump element will now be translated into a break; instruction by default.
 *      - Comment method signature simplified
 *      - Indentation mechanism revised
 *      
 *      2014.11.16 - Bugfixes (Kay Gürtzig)
 *      - conversion of comparison and logical operators had still been flawed
 *      - comment generation unified by new inherited generic method insertComment 
 *      
 *      2014.11.06 - Enhancement (Kay Gürtzig)
 *      - logical operators "and", "or", and "not" supported 
 *      
 *      2010.09.10 - Bugfixes (Kay Gürtzig)
 *      - conditions for automatic bracket insertion for "while", "switch", "if" corrected
 *      - case keyword inserted for the branches of "switch"
 *      - function header and return statement for non-program diagram export adjusted
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *      
 *      2010.08.30
 *      - replaced standard I/O by the correct versions for C (not Pascal ;-P))
 *      - comments are put into code as well
 *      - code transformations (copied from Java)
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator convertion
 *
 *      2009.08.10 - Bugfixes
 *      - Mistyping of the keyword "switch" in CASE statement
 *      - Mistyping of brackets in IF statement
 *      - Implementation of FOR loop
 *      - Indent replaced from 2 spaces to TAB-character (TAB configurable in IDE)
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;

public class CGenerator extends Generator {

	/************ Fields ***********************/
	@Override
	protected String getDialogTitle() {
		return "Export ANSI C ...";
	}

	@Override
	protected String getFileDescription() {
		return "ANSI C Source Code";
	}

	@Override
	protected String getIndent() {
		return "\t";
	}

	@Override
	protected String[] getFileExtensions() {
		String[] exts = { "c" };
		return exts;
	}

	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft() {
		// In ANSI C99, line comments are already allowed
		return "//";
	}

	// END KGU 2015-10-18

// START KGU#16 2015-12-18: Moved to Generator.java	and made an ExportOptionDialoge option
//	// START KGU#16 2015-11-29: Code style option for opening brace placement
//	protected boolean optionBlockBraceNextLine() {
//		// (KGU 2015-11-29): Should become an ExportOptionDialoge option
//		return true;
//	}
//	// END KGU#16 2015-11-29
// END KGU#16 2015-12-18
	
	// START KGU#261/#332 2017-01-27: Enh. #259/#335
	protected HashMap<String, TypeMapEntry> typeMap;
	// END KGU#261/#332 2017-01-27
	
	// START KGU#16/KGU#74 2015-11-30: Unification of block generation (configurable)
	/**
	 * This subclassable method is used for insertBlockHeading()
	 * @return Indicates where labels for multi-level loop exit jumps are to be placed
	 * (in C, C++, C# after the loop, in Java at the beginning of the loop). 
	 */
	protected boolean isLabelAtLoopStart()
	{
		return false;
	}
	
	/**
	 * Instruction to be used to leave an outer loop (subclassable)
	 * A label string is supposed to be appended without parentheses.
	 * @return a string containing the respective reserved word
	 */
	protected String getMultiLevelLeaveInstr()
	{
		return "goto";
	}
	
	// See also insertExitInstr(int, String)
	// END KGU#16/KGU#74 2015-11-30

	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean breakMatchesCase()
	{
		return true;
	}
	// END KGU#78 2015-12-18

	// START KGU 2016-08-12: Enh. #231 - information for analyser
    private static final String[] reservedWords = new String[]{
		"auto", "break", "case", "char", "const", "continue",
		"default", "do", "double", "else", "enum", "extern",
		"float", "for", "goto", "if", "int", "long",
		"register", "return",
		"short", "signed", "sizeof", "static", "struct", "switch",
		"typedef", "union", "unsigned", "void", "volatile", "while"};
	public String[] getReservedWords()
	{
		return reservedWords;
	}
	public boolean isCaseSignificant()
	{
		return true;
	}
	// END KGU 2016-08-12

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return "#include %";
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input
	 * instruction) into the target code
	 * @param withPrompt - is a prompt string to be considered?
	 * @return a regex replacement pattern, e.g.
	 *         "$1 = (new Scanner(System.in)).nextLine();"
	 */
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer() {
	//	return "scanf(\"\", &$1)";
	//}
	protected String getInputReplacer(boolean withPrompt) {
		if (withPrompt) {
			return "printf($1); scanf(\"\", &$2)";
		}
		return "scanf(\"\", &$1)";
	}
	// END KGU#281 2016-10-15

	/**
	 * A pattern how to embed the expression (right-hand side of an output
	 * instruction) into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer() {
		return "printf(\"\", $1); printf(\"\\\\n\")";
	}

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * Method preprocesses an include file name for the #include
	 * clause. This version surrounds a string not enclosed in angular
	 * brackets by quotes.
	 * @param _includeFileName a string from the user include configuration
	 * @return the preprocessed string as to be actually inserted
	 */
	protected String prepareIncludeItem(String _includeFileName)
	{
		if (!(_includeFileName.startsWith("<") && _includeFileName.endsWith(">"))) {
			_includeFileName = "\"" + _includeFileName + "\"";
		}
		return _includeFileName;
	}
	// END KGU#351 2017-02-26

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	protected void insertExitInstr(String _exitCode, String _indent, boolean isDisabled)
	{
		// START KGU 2016-01-15: Bugfix #64 (reformulated) semicolon was missing
		//code.add(_indent + "exit(" + _exitCode + ")");
		addCode("exit(" + _exitCode + ");", _indent, isDisabled);
		// END KGU 2016-01-15
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by "="
//	 * 
//	 * @param _interm
//	 *            - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm) {
//		return _interm.replace(" <- ", " = ");
//	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		// START KGU#150 2016-04-03: Handle Pascal ord and chr function
		int pos = - 1;
		while ((pos = tokens.indexOf("ord", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "(int)");
		}
		pos = -1;
		while ((pos = tokens.indexOf("chr", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "(char)");
		}
		// END KGU#150 2016-04-03
		// START KGU#311 2016-12-22: Enh. #314 - Structorizer file API support
		if (this.usesFileAPI) {
			transformFileAPITokens(tokens);
		}
		// END KGU#311 2016-12-22
		// START KGU#342 2017-02-07: Bugfix #343
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			int tokenLen = token.length();
			if (tokenLen >= 2 && (token.startsWith("'") && token.endsWith("\"") || token.startsWith("'") && token.endsWith("\""))) {
				char delim = token.charAt(0);
				String internal = token.substring(1, tokenLen-1);
				// Escape all unescaped double quotes
				pos = -1;
				while ((pos = internal.indexOf("\"", pos+1)) >= 0) {
					if (pos == 0 || internal.charAt(pos-1) != '\\') {
						internal = internal.substring(0, pos) + "\\\"" + internal.substring(pos+1);
						pos++;
					}
				}
				if (!(tokenLen == 3 || tokenLen == 4 && token.charAt(1) == '\\')) {
					delim = '\"';
				}
				tokens.set(i, delim + internal + delim);
			}
		}
		// END KGU#342 2017-02-07
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21

	// END KGU#18/KGU#23 2015-11-01

	// START KGU#311 2016-12-22: Enh. #314 - Structorizer file API support
	/**
	 * Subclassable submethod of transformTokens(), designed to do specific replacements or manipulations
	 * with the subroutine names of the File API. It is called after all other token transformations are done
	 * (immediately before re-concatenation).
	 * This does some C-specific stuff here prefixing fileRead with pointer operators and a dummy type casting,
	 * so subclasses should better overwrite it.
	 * @param tokens
	 */
	protected void transformFileAPITokens(StringList tokens)
	{
		int pos = -1;
		while ((pos = tokens.indexOf("fileRead", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "*(/*type?*/*)fileRead");
		}
	}
	// END KGU#311 2016-12-22
	
// START KGU#18/KGU#23 2015-11-01: Obsolete    
//    public static String transform(String _input)
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		// START KGU#162 2016-04-01: Enh. #144
		if (!this.suppressTransformation)
		{
		// END KGU#162 2016-04-01
			// START KGU#109/KGU#141 2016-01-16: Bugfix #61,#107,#112
			_input = Element.unifyOperators(_input);
			int asgnPos = _input.indexOf("<-");
			if (asgnPos > 0)
			{
				String lval = _input.substring(0, asgnPos).trim();
				String expr = _input.substring(asgnPos + "<-".length()).trim();
				String[] typeNameIndex = this.lValueToTypeNameIndex(lval);
				String index = typeNameIndex[2];
				_input = (typeNameIndex[0] + " " + typeNameIndex[1] + 
						(index.isEmpty() ? "" : "["+index+"]") + 
						" <- " + expr).trim();
			}
			// END KGU#109/KGU#141 2016-01-16
		// START KGU#162 2016-04-01: Enh. #144
		}
		// END KGU#162 2016-04-01
		
		_input = super.transform(_input);

		// START KGU#108 2015-12-13: Bugfix #51: Cope with empty input and output
		_input = _input.replace("scanf(\"\", &)", "getchar()");
		_input = _input.replace("printf(\"\", ); ", "");
		// END KGU#108 2015-12-13

		return _input.trim();
	}

	// START KGU#16 2015-11-29
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		_type = _type.toLowerCase();
		_type = _type.replace("integer", "int");
		_type = _type.replace("real", "double");
		_type = _type.replace("boolean", "int");
		_type = _type.replace("boole", "int");
		_type = _type.replace("character", "char");
		// START KGU#332 2017-01-30: Enh. #335 - more sophisticated type info
		if (this.getClass().getSimpleName().equals("CGenerator")) {
			_type = _type.replace("string", "char*");
		}
		// END KGU#332 2017-01-30
		return _type;
	}
	// END KGU#16 2015-11-29

	// START KGU#140 2017-01-31: Enh. #113: Advanced array transformation
	protected String transformArrayDeclaration(String _typeDescr, String _varName)
	{
		String decl = "";
		if (_typeDescr.toLowerCase().startsWith("array") || _typeDescr.endsWith("]")) {
			// TypeMapEntries are really good at analysing array definitions
			TypeMapEntry typeInfo = new TypeMapEntry(_typeDescr, null, 0, false, false);
			String canonType = typeInfo.getTypes().get(0);
			decl = this.makeArrayDeclaration(canonType, _varName, typeInfo).trim();
		}
		else {
			decl = (_typeDescr + " " + _varName).trim();
		}
		return decl;
	}
	// END KGU#140 2017-01-31
	
	protected void insertBlockHeading(Element elem, String _headingText, String _indent)
	{
		boolean isDisabled = elem.isDisabled();
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem) && this.isLabelAtLoopStart())  
		{
				_headingText = this.labelBaseName + this.jumpTable.get(elem) + ": " + _headingText;
		}
		if (!this.optionBlockBraceNextLine())
		{
			addCode(_headingText + " {", _indent, isDisabled);
		}
		else
		{
			addCode(_headingText, _indent, isDisabled);
			addCode("{", _indent, isDisabled);
		}
	}

	protected void insertBlockTail(Element elem, String _tailText, String _indent)
	{
		boolean isDisabled = elem.isDisabled();
		if (_tailText == null) {
			addCode("}", _indent, isDisabled);
		}
		else {
			addCode("} " + _tailText + ";", _indent, isDisabled);
		}
		
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem) && !this.isLabelAtLoopStart()) {
			addCode(this.labelBaseName + this.jumpTable.get(elem) + ": ;", _indent, isDisabled);
		}
	}
	// END KGU#74 2015-11-30
	
	// START KGU#332 2017-01-27: Enh. #335
	protected boolean isInternalDeclarationAllowed()
	{
		return false;
	}
	// END KGU#332 2017-01-27

	@Override
	protected void generateCode(Instruction _inst, String _indent) {

		if (!insertAsComment(_inst, _indent)) {

			insertComment(_inst, _indent);
			boolean isDisabled = _inst.isDisabled();

			StringList lines = _inst.getText();
			for (int i = 0; i < lines.count(); i++) {
				// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
				//code.add(_indent + transform(lines.get(i)) + ";");
				String line = _inst.getText().get(i);
				// START KGU#261/KGU#332 2017-01-26: Enh. #259/#335
				//String codeLine = transform(line) + ";";
				//addCode(codeLine, _indent, isDisabled);
				String codeLine = null;
				if (!this.suppressTransformation && Instruction.isDeclaration(line)) {
					// Pure declarations without initialization may be igmored here (the
					// declaration will be proposed or inserted in the preamble).
					// If there is an initialization then it must at least be generated
					// as assignment.
					// With declaration styles other than than C-like, this requires
					// cutting out the keywords type specification together with the
					// specific keywords and separators ("var"+":" / "dim"+"as").
					// With C-style initializations, however, it depends on whether
					// code-internal declarations are allowed (C++, C#, Java) or not
					// (pure C): If allowed then we may just convert it as is, otherwise
					// we must cut off the type specification (i.e. all text preceding the
					// variable name).
					if (Instruction.isAssignment(line)) {
						// Things will get easier and more precise with tokenization
						// (which must be done based on the original line)
						StringList tokens = Element.splitLexically(line.trim(), true);
						int posAsgn = tokens.indexOf("<-");
						if (tokens.get(0).equalsIgnoreCase("var") || tokens.get(0).equalsIgnoreCase("dim")) {
							String separator = tokens.get(0).equalsIgnoreCase("var") ? ":" : "as";
							int posColon = tokens.indexOf(separator, 2, false);
							if (posColon > 0) {
								codeLine = transform(
										tokens.subSequence(1, posColon).concatenate().trim() + " " +
										tokens.subSequence(posAsgn, tokens.count()).concatenate().trim());
							}
						}
						else {
							// Must be C-style declaration
							if (!this.isInternalDeclarationAllowed()) {
								// Cut out leading type specification
								String varName = _inst.getAssignedVarname(tokens);
								int posVar = tokens.indexOf(varName);
								tokens = tokens.subSequence(posVar, tokens.count());
							}
							codeLine = transform(tokens.concatenate().trim());						}
					}
				}
				else {
					codeLine = transform(line);
				}
				if (codeLine != null) {
					if (Instruction.isTurtleizerMove(line)) {
						codeLine += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
					}
					addCode(codeLine + ";", _indent, isDisabled);
				}
				// END KGU#261 2017-01-26
				// END KGU#277/KGU#284 2016-10-13
			}

		}
		
	}

	@Override
	protected void generateCode(Alternative _alt, String _indent) {
		
		insertComment(_alt, _indent);
		
		String condition = transform(_alt.getText().getLongString(), false)
				.trim();
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")"))
		if (!isParenthesized(condition))
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		
		insertBlockHeading(_alt, "if " + condition, _indent);
		generateCode(_alt.qTrue, _indent + this.getIndent());
		insertBlockTail(_alt, null, _indent);

		if (_alt.qFalse.getSize() != 0) {
			insertBlockHeading(_alt, "else", _indent);
			generateCode(_alt.qFalse, _indent + this.getIndent());
			insertBlockTail(_alt, null, _indent);
		}
	}

	@Override
	protected void generateCode(Case _case, String _indent) {
		
		boolean isDisabled = _case.isDisabled();
		insertComment(_case, _indent);
		
		StringList lines = _case.getText();
		String condition = transform(lines.get(0), false);
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) {
		if (!isParenthesized(condition)) {
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		}

		insertBlockHeading(_case, "switch " + condition, _indent);

		for (int i = 0; i < _case.qs.size() - 1; i++) {
			// START KGU#15 2015-10-21: Support for multiple constants per
			// branch
			StringList constants = StringList.explode(lines.get(i + 1), ",");
			for (int j = 0; j < constants.count(); j++) {
				code.add(_indent + "case " + constants.get(j).trim() + ":");
			}
			// END KGU#15 2015-10-21
			generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent());
			addCode(this.getIndent() + "break;", _indent, isDisabled);
		}

		if (!lines.get(_case.qs.size()).trim().equals("%")) {
			addCode("default:", _indent, isDisabled);
			Subqueue squeue = (Subqueue) _case.qs.get(_case.qs.size() - 1);
			generateCode(squeue, _indent + this.getIndent());
			// START KGU#71 2015-11-10: For an empty default branch, at least a
			// semicolon is required
			if (squeue.getSize() == 0) {
				addCode(this.getIndent() + ";", _indent, isDisabled);
			}
			// END KGU#71 2015-11-10
		}
		
		insertBlockTail(_case, null, _indent);
	}

	// END KGU#18/#23 2015-10-20

	@Override
	protected void generateCode(For _for, String _indent) {

		insertComment(_for, _indent);
		
		// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
		if (_for.isForInLoop())
		{
			// There aren't many ideas how to implement this here in general,
			// but subclasses may have better chances to do so.
			if (generateForInCode(_for, _indent)) return;
		}
		// END KGU#61 2016-03-22

		String var = _for.getCounterVar();
		int step = _for.getStepConst();
		String compOp = (step > 0) ? " <= " : " >= ";
		String increment = var + " += (" + step + ")";
		insertBlockHeading(_for, "for (" + var + " = "
				+ transform(_for.getStartValue(), false) + "; " + var + compOp
				+ transform(_for.getEndValue(), false) + "; " + increment + ")",
				_indent);

		generateCode(_for.q, _indent + this.getIndent());

		insertBlockTail(_for, null, _indent);

	}
	
	// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
	/**
	 * We try our very best to create a working loop from a FOR-IN construct
	 * This will only work, however, if we can get reliable information about
	 * the size of the value list, which won't be the case if we obtain it e.g.
	 * via a variable.
	 * @param _for - the element to be exported
	 * @param _indent - the current indentation level
	 * @return true iff the method created some loop code (sensible or not)
	 */
	protected boolean generateForInCode(For _for, String _indent)
	{
		boolean done = false;
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		StringList items = this.extractForInListItems(_for);
		if (items != null)
		{
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogenous? We will just try three types: int,
			// double and C-strings, and if none of them match we add a TODO comment.
			int nItems = items.count();
			boolean allInt = true;
			boolean allDouble = true;
			boolean allString = true;
			boolean isDisabled = _for.isDisabled();
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
				if (allInt)
				{
					try {
						Integer.parseInt(item);
					}
					catch (NumberFormatException ex)
					{
						allInt = false;
					}
				}
				if (allDouble)
				{
					try {
						Double.parseDouble(item);
					}
					catch (NumberFormatException ex)
					{
						allDouble = false;
					}
				}
				if (allString)
				{
					allString = item.startsWith("\"") && item.endsWith("\"") &&
							!item.substring(1, item.length()-1).contains("\"");
				}
			}
			String itemType = "";
			if (allInt) itemType = "int";
			else if (allDouble) itemType = "double";
			else if (allString) itemType = "char*";
			String arrayLiteral = "{" + items.concatenate(", ") + "}";
			String arrayName = "array20160322";
			String indexName = "index20160322";

			String indent = _indent + this.getIndent();
			// Start an extra block to encapsulate the additional definitions
			addCode("{", _indent, isDisabled);
			
			if (itemType.isEmpty())
			{
				// We do a dummy type definition
				this.insertComment("TODO: Define a sensible 'ItemType' and/or prepare the elements of the array", indent);
				itemType = "ItemType";
				addCode("typedef void " + itemType + ";", indent, isDisabled);
			}
			// We define a fixed array here
			addCode(itemType + " " + arrayName +  "[" + nItems + "] = "
					+ transform(arrayLiteral, false) + ";", indent, isDisabled);
			// Definition of he loop index variable
			addCode("int " + indexName + ";", indent, isDisabled);

			// Creation of the loop header
			insertBlockHeading(_for, "for (" + indexName + " = 0; " +
					indexName + " < " + nItems + "; " + indexName + "++)",
					indent);
			
			// Assignment of a single item to the given variable
			addCode(this.getIndent() + itemType + " " + var + " = " +
					arrayName + "[" + indexName + "];", indent, isDisabled);

			// Add the loop body as is
			generateCode(_for.q, indent + this.getIndent());

			// Accomplish the loop
			insertBlockTail(_for, null, indent);

			// Close the extra block
			addCode("}", _indent, isDisabled);
			done = true;
		}
		else
		{
			// We have no strategy here, no idea how to find out the number and type of elements,
			// no idea how to iterate the members, so we leave it similar to C# and just add a TODO comment...
			this.insertComment("TODO: Rewrite this loop (there was no way to convert this automatically)", _indent);

			// Creation of the loop header
			insertBlockHeading(_for, "foreach (" + var + " in " + transform(valueList, false) + ")", _indent);
			// Add the loop body as is
			generateCode(_for.q, _indent + this.getIndent());
			// Accomplish the loop
			insertBlockTail(_for, null, _indent);
			
			done = true;
		}
		return done;
	}
	// END KGU#61 2016-03-22

	@Override
	protected void generateCode(While _while, String _indent) {
		
		insertComment(_while, _indent);
		

		String condition = transform(_while.getText().getLongString(), false)
				.trim();
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) {
		if (!isParenthesized(condition)) {
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		}

		insertBlockHeading(_while, "while " + condition, _indent);

		generateCode(_while.q, _indent + this.getIndent());

		insertBlockTail(_while, null, _indent);

	}

	@Override
	protected void generateCode(Repeat _repeat, String _indent) {
		
		insertComment(_repeat, _indent);

		insertBlockHeading(_repeat, "do", _indent);

		generateCode(_repeat.q, _indent + this.getIndent());

		// START KGU#301 2016-12-01: Bugfix #301
		//insertBlockTail(_repeat, "while (!(" 
		//		+ transform(_repeat.getText().getLongString()).trim() + "))", _indent);
		String condition = transform(_repeat.getText().getLongString()).trim();
		if (!isParenthesized(condition)) {
			condition = "(" + condition + ")";
		}
		insertBlockTail(_repeat, "while (!" + condition + ")", _indent);
		// END KGU#301 2016-12-01
	}

	@Override
	protected void generateCode(Forever _forever, String _indent) {
		
		insertComment(_forever, _indent);

		insertBlockHeading(_forever, "while (true)", _indent);

		generateCode(_forever.q, _indent + this.getIndent());

		insertBlockTail(_forever, null, _indent);
	}

	@Override
	protected void generateCode(Call _call, String _indent) {
 
		if (!insertAsComment(_call, _indent)) {

			boolean isDisabled = _call.isDisabled();
			insertComment(_call, _indent);

			StringList lines = _call.getText();
			for (int i = 0; i < lines.count(); i++) {
				// Input or Output should not occur here
				addCode(transform(lines.get(i), false) + ";", _indent, isDisabled);
			}
		}
		
	}

	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		// START KGU 2015-10-18: The "export instructions as comments"
		// configuration had been ignored here
		// insertComment(_jump, _indent);
		// for(int i=0;i<_jump.getText().count();i++)
		// {
		// code.add(_indent+transform(_jump.getText().get(i))+";");
		// }
		if (!insertAsComment(_jump, _indent)) {
			
			boolean isDisabled = _jump.isDisabled();

			insertComment(_jump, _indent);

			// KGU 2015-10-18: In case of an empty text generate a break
			// instruction by default.
			boolean isEmpty = true;
			
			StringList lines = _jump.getText();
			String preReturn = D7Parser.getKeywordOrDefault("preReturn", "return").trim();
			String preExit   = D7Parser.getKeywordOrDefault("preExit", "exit").trim();
			String preLeave  = D7Parser.getKeywordOrDefault("preLeave", "leave").trim();
			String preReturnMatch = Matcher.quoteReplacement(preReturn)+"([\\W].*|$)";
			String preExitMatch   = Matcher.quoteReplacement(preExit)+"([\\W].*|$)";
			String preLeaveMatch  = Matcher.quoteReplacement(preLeave)+"([\\W].*|$)";
			for (int i = 0; isEmpty && i < lines.count(); i++) {
				String line = transform(lines.get(i)).trim();
				if (!line.isEmpty())
				{
					isEmpty = false;
				}
				// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
				//code.add(_indent + line + ";");
				if (line.matches(preReturnMatch))
				{
					addCode("return " + line.substring(preReturn.length()).trim() + ";",
							_indent, isDisabled);
				}
				else if (line.matches(preExitMatch))
				{
					insertExitInstr(line.substring(preExit.length()).trim(), _indent, isDisabled);
				}
				// Has it already been matched with a loop? Then syntax must have been okay...
				else if (this.jumpTable.containsKey(_jump))
				{
					Integer ref = this.jumpTable.get(_jump);
					String label = this.labelBaseName + ref;
					if (ref.intValue() < 0)
					{
						insertComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
						insertComment(line, _indent);
						label = "__ERROR__";
					}
					addCode(this.getMultiLevelLeaveInstr() + " " + label + ";", _indent, isDisabled);
				}
				else if (line.matches(preLeaveMatch))
				{
					// START KGU 2017-02-06: The "funny comment" was irritating and dubious itself
					// Seems to be an ordinary one-level break without need to concoct a jump statement
					// (Are there also strange cases - neither matched nor rejected? And how could this happen?)
					//addCode("break;\t// FIXME: Dubious occurrance of break instruction!", _indent, isDisabled);
					addCode("break;", _indent, isDisabled);
					// END KGU 2017-02-06
				}
				else if (!isEmpty)
				{
					insertComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
					insertComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
			if (isEmpty) {
				addCode("break;", _indent, isDisabled);
			}
			// END KGU 2015-10-18
		}
	}

	// START KGU#47 2015-11-30: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{

		boolean isDisabled = _para.isDisabled();
		insertComment(_para, _indent);

		addCode("", "", isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
		addCode("{", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			insertComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			addCode("{", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("}", _indent + this.getIndent(), isDisabled);
			insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			addCode("", "", isDisabled);
		}

		addCode("}", _indent, isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}
	// END KGU#47 2015-11-30
	


	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
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
		// START KGU#178 2016-07-20: Enh. #160
		if (!topLevel)
		{
			code.add("");					
		}
		// END KGU#178 2016-07-20
		String pr = (_root.isProgram) ? "program" : "function";
		insertComment(pr + " " + _root.getText().get(0), _indent);
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
		// END KGU#178 2016-07-20
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			code.add("");
			// START KGU#236 2016-08-10: Issue #227
			//code.add("#include <stdio.h>");
			//code.add("");
			if (this.hasInput() || this.hasOutput() || this.usesFileAPI)
			{
				code.add("#define _CRT_SECURE_NO_WARNINGS");	// VisualStudio precaution 
				code.add("#include <stdio.h>");
				if (this.usesFileAPI) {
					code.add("#include <stdlib.h>");
					code.add("#include <string.h>");
					code.add("#include <errno.h>");
				}
				// STARTB KGU#351 2017-02-26: Enh. #346
				this.insertUserIncludes("");
				// END KGU#351 2017-02-26
				code.add("");
			}
			// END KGU#236 2016-08-10
		// START KGU#178 2016-07-20: Enh. #160
			subroutineInsertionLine = code.count();
			subroutineIndent = _indent;
			
			// START KGU#311 2016-12-22: Enh. #314 - insert File API routines if necessary
			if (this.usesFileAPI) {
				this.insertFileAPI("c");
			}
			// END KGU#311 2016-12-22
		}
		// END KGU#178 2016-07-20

		insertComment(_root, _indent);
		
		if (_root.isProgram)
			code.add("int main(void)");
		else {
			// Compose the function header
			String fnHeader = transformType(_root.getResultType(),
					((this.returns || this.isResultSet || this.isFunctionNameSet) ? "int" : "void"));
			// START KGU#140 2017-01-31: Enh. #113 - improved type recognition and transformation
			boolean returnsArray = fnHeader.toLowerCase().contains("array") || fnHeader.contains("]");
			if (returnsArray) {
				fnHeader = transformArrayDeclaration(fnHeader, "");
			}
			// END KGU#140 2017-01-31
			fnHeader += " " + _procName + "(";
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				// START KGU#140 2017-01-31: Enh. #113: Proper conversion of array types
				//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
				//		_paramNames.get(p)).trim();
				fnHeader += transformArrayDeclaration(transformType(_paramTypes.get(p), "/*type?*/").trim(), _paramNames.get(p));
				// END KGU#140 2017-01-31
			}
			fnHeader += ")";
			insertComment("TODO: Revise the return type and declare the parameters.", _indent);
			// START KGU#140 2017-01-31: Enh. #113
			if (returnsArray) {
				insertComment("      C does not permit to return arrays - find an other way to pass the result!", _indent);
			}
			// END KGU#140 2017-01-31
			code.add(fnHeader);
		}
		code.add(_indent + "{");
		
		return _indent + this.getIndent();
	}

	/**
	 * Generates some preamble (i.e. comments, language declaration section etc.)
	 * and adds it to this.code.
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param varNames - list of variable names introduced inside the body
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		insertComment("TODO: Check and accomplish variable declarations:", _indent);
        // START KGU#261/KGU#332 2017-01-26: Enh. #259/#335: Insert actual declarations if possible
		this.typeMap = _root.getTypeInfo();
		// END KGU#261/KGU#332 2017-01-16
        // START KGU 2015-11-30: List the variables to be declared
		for (int v = 0; v < varNames.count(); v++) {
	        // START KGU#261/#332 2017-01-26: Enh. #259/#335: Insert actual declarations if possible
			//insertComment(varNames.get(v), _indent);
			String varName = varNames.get(v);
			TypeMapEntry typeInfo = typeMap.get(varName); 
			StringList types = null;
			if (typeInfo != null) {
				 types = getTransformedTypes(typeInfo);
			}
			// If the type is unambiguous and has no C-style declaration or may not be
			// declared between instructions then add the declaration here
			if (types != null && types.count() == 1 && 
					(!typeInfo.isCStyleDeclaredAt(null) || !this.isInternalDeclarationAllowed())) {			
				String decl = types.get(0);
				if (decl.startsWith("@")) {
					decl = makeArrayDeclaration(decl, varName, typeInfo);
				}
				else {
					decl = decl + " " + varName;
				}
				if (decl.contains("???")) {
					insertComment(decl + ";", _indent);
				}
				else {
					code.add(_indent + decl + ";");
				}
			}
			// Add a comment if there is no type info or internal declaration is not allowed
			else if (types == null || !this.isInternalDeclarationAllowed()){
				insertComment(varName + ";", _indent);
			}
			// END KGU#261/KGU#332 2017-01-16
		}
		// END KGU 2015-11-30
		// START KGU#332 2017-01-30: Decomposed to ease sub-classing
		generateIOComment(_root, _indent);
		// END KGU#332 2017-01-30
		code.add(_indent);
		return _indent;
	}
	
	// START KGU#332 2017-01-30: Decomposition of geeratePreamble to ease sub-classing
	protected String makeArrayDeclaration(String _elementType, String _varName, TypeMapEntry typeInfo)
	{
		int nLevels = _elementType.lastIndexOf('@')+1;
		_elementType = (_elementType.substring(nLevels) + " " + _varName).trim();
		for (int i = 0; i < nLevels; i++) {
			int maxIndex = typeInfo.getMaxIndex(i);
			_elementType += "[" + (maxIndex >= 0 ? Integer.toString(maxIndex+1) : (i == 0 ? "" : "/*???*/") ) + "]";
		}
		return _elementType;
	}
	
	protected void generateIOComment(Root _root, String _indent)
	{
		// START KGU#236 2016-08-10: Issue #227 - don't express this information if not needed
		if (this.hasInput(_root)) {
		// END KGU#236 2016-08-10
			code.add(_indent);
			insertComment("TODO:", _indent);
			insertComment(
					"For any input using the 'scanf' function you need to fill the first argument.",
					_indent);
			insertComment(
					"http://en.wikipedia.org/wiki/Scanf#Format_string_specifications",
					_indent);
		// START KGU#236 2016-08-10: Issue #227
		}
		if (this.hasOutput(_root)) {
		// END KGU#236 2016-08-10
		code.add(_indent);
		insertComment("TODO:", _indent);
		insertComment(
				"For any output using the 'printf' function you need to fill the first argument:",
				_indent);
		insertComment(
				"http://en.wikipedia.org/wiki/Printf#printf_format_placeholders",
				_indent);
		// START KGU#236 2016-08-10: Issue #227
		}
		// END KGU#236 2016-08-10	
	}
	// START KGU#332 2017-01-30
	
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
		if (_root.isProgram && !alwaysReturns)
		{
			code.add(_indent);
			code.add(_indent + "return 0;");
		}
		else if ((returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
		{
			String result = "0";
			if (isFunctionNameSet)
			{
				result = _root.getMethodName();
			}
			else if (isResultSet)
			{
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
			}
			code.add(_indent);
			code.add(_indent + "return " + result + ";");
		}
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
		code.add(_indent + "}");		
	}

	
}

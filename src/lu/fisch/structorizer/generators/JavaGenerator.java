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

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class generates Java code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Bob Fisch               2008.11.17      First Issue
 *      Gunter Schillebeeckx    2009.08.10      Java Generator starting from C Generator
 *      Bob Fisch               2009.08.10      Update I/O
 *      Bob Fisch               2009.08.17      Bugfixes (see comment)
 *      Kay Gürtzig             2010.09.10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.10.22      Workarounds and Enhancements (see comment)
 *      Kay Gürtzig             2014.11.16      Several fixes and enhancements (see comment)
 *      Kay Gürtzig             2015.10.18      Comment generation and indentation revised
 *      Kay Gürtzig             2015.11.01      Preprocessing reorganised, FOR loop and CASE enhancements
 *      Kay Gürtzig             2015.11.30      Inheritance changed to CGenerator (KGU#16), specific
 *                                              jump and return handling added (issue #22 = KGU#74)
 *      Kay Gürtzig             2015.12.12      Enh. #54 (KGU#101): Support for output expression lists
 *      Kay Gürtzig             2015.12.15      Bugfix #51 (=KGU#108): Cope with empty input and output
 *      Kay Gürtzig             2015.12.21      Bugfix #41/#68/#69 (= KG#93)
 *      Kay Gürtzig             2016.03.23      Enh. #84: Support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig             2016.04.04      transforTokens() disabled due to missing difference to super 
 *      Kay Gürtzig             2016.07.20      Enh. #160: Option to involve subroutines implemented (=KGU#178) 
 *      Kay Gürtzig             2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2016.09.25      Enh. #253: D7Parser.keywordMap refactoring done 
 *      Kay Gürtzig             2016.10.14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016.10.15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig             2016.12.22      Enh. #314: Support for Structorizer File API
 *      Kay Gürtzig             2017.01.30      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig             2017.02.01      Enh. #113: Array parameter transformation
 *
 ******************************************************************************************************
 *
 *      Comments:
 *      
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU#23)
 *      - Enhancement #10 (KGU#3): FOR loops now provide more reliable loop parameters 
 *      - Enhancement KGU#15: Support for the gathering of several case values in CASE instructions
 *      
 *      2015-10-18 - Bugfix / Code revision
 *      - Indentation increment with +_indent.substring(0,1) worked only for single-character indentation units
 *      - Interface of comment insertion methods modified
 *      
 *      2014.11.16 - Bugfixes
 *      - conversion of comparison and logical operators had still been flawed
 *      - comment generation unified by new inherited generic method insertComment 
 *      
 *      2014.10.22 - Bugfixes / Enhancement
 *      - Replacement for asin(), acos(), atan() hadn't worked.
 *      - Support for logical operators "and", "or", and "not"
 *      
 *      2010.09.10 - Bugfixes
 *      - condition for automatic bracket addition around condition expressions corrected
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator convertion
 *      - pascal function convertion
 *
 *      2009.08.10
 *        - writeln() => System.out.println()
 * 
 ******************************************************************************************************
 */

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


// START KGU#16 2015-11-30: Strong similarities made it sensible to reduce this class to the differences
//public class JavaGenerator extends Generator
public class JavaGenerator extends CGenerator
// END KGU#16 2015-11-30
{

	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export Java ...";
	}

	protected String getFileDescription()
	{
		return "Java Source Code";
	}

	protected String getIndent()
	{
		return "\t";
	}

	protected String[] getFileExtensions()
	{
		String[] exts = {"java"};
		return exts;
	}

	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft()
	{
		return "//";
	}
	// END KGU 2015-10-18

// START KGU#16 2015-12-18: Now inherited and depending on export option	
//	// START KGU#16 2015-11-29: Code style option for opening brace placement
//	protected boolean optionBlockBraceNextLine() {
//		return false;
//	}
//	// END KGU#16 2015-11-29
// END KGU#16 2015-12-18

	// START KGU#16/KGU#47 2015-11-30: Unification of block generation (configurable)
	/**
	 * This subclassable method is used for insertBlockHeading()
	 * @return Indicates where labels for multi-level loop exit jumps are to be placed
	 * (in C, C++, C# after the loop, in Java at the beginning of the loop). 
	 */
	@Override
	protected boolean isLabelAtLoopStart()
	{
		return true;
	}

	/**
	 * Instruction to be used to leave an outer loop (subclassable)
	 * A label string is supposed to be appended without parentheses.
	 * @return a string containing the respective reserved word
	 */
	@Override
	protected String getMultiLevelLeaveInstr()
	{
		return "break";
	}
	// END KGU#16/#47 2015-11-30

	// START KGU 2016-08-12: Enh. #231 - information for analyser
    private static final String[] reservedWords = new String[]{
		"abstract", "assert", "boolean", "break", "byte",
		"case", "catch", "char", "class", "const", "continue",
		"default", "do", "double",
		"else", "enum", "extends",
		"false", "final", "finally", "float", "for", "goto",
		"if", "implements", "import", "instanceof", "int", "interface",
		"long", "native", "new", "null",
		"package", "private", "protected", "public",
		"return", "short", "static", "super", "switch", "synchronised",
		"this", "throw", "throws", "transient", "true", "try",
		"void", "volatile", "while"};
	public String[] getReservedWords()
	{
		return reservedWords;
	}
	// END KGU 2016-08-12

	/************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @param withPrompt - is a prompt string to be considered?
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer()
	//{
	//	return "$1 = (new Scanner(System.in)).nextLine()";
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		// NOTE: If you modify these patterns then you must adapt transform() too!
		if (withPrompt) {
			return "System.out.println($1); $2 = (new Scanner(System.in)).nextLine()";
		}
		return "$1 = (new Scanner(System.in)).nextLine()";
	}
	// END KGU#281 2016-10-15

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "System.out.println($1)";
	}

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	@Override
	protected void insertExitInstr(String _exitCode, String _indent, boolean isDisabled)
	{
		addCode("System.exit(" + _exitCode + ")", _indent, isDisabled);
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by "="
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm)
//	{
//		return _interm.replace(" <- ", " = ");
//	}

	// START KGU#150 2016-04-04: No need to override CGenerator.tranformTokens()
//	/* (non-Javadoc)
//	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
//	 */
//	@Override
//	protected String transformTokens(StringList tokens)
//	{
//		tokens.replaceAll("div", "/");
//		tokens.replaceAll("<-", "=");
//		return tokens.concatenate();
//	}
	// END KGU#150 2016-04-04
	// END KGU#93 2015-12-21

	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#311 2017-01-05: Enh. #314 Don't do what the parent does.
	/* (non-Javadoc)
	 * Does nothing here.
	 * @see lu.fisch.structorizer.generators.CGenerator#transformFileAPITokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected void transformFileAPITokens(StringList tokens)
	{
	}
	// END KGU#311 2017-01-05

	@Override
	protected String transform(String _input)
	{
		// START KGU#101 2015-12-12: Enh. #54 - support lists of expressions
		String outputKey = D7Parser.getKeyword("output").trim(); 
		if (_input.matches("^" + getKeywordPattern(outputKey) + "[ ](.*?)"))
		{
			StringList expressions = 
					Element.splitExpressionList(_input.substring(outputKey.length()), ",");
			// Some of the expressions might be sums, so better put parentheses around them
			_input = outputKey + " (" + expressions.getText().replace("\n", ") + (") + ")";
		}
		// END KGU#101 2015-12-12

		// START KGU#18/KGU#23 2015-11-01: This can now be inherited
		String s = super.transform(_input) /*.replace(" div "," / ")*/;
		// END KGU#18/KGU#23 2015-11-01

		// START KGU#108 2015-12-15: Bugfix #51: Cope with empty input and output
		String inpRepl = getInputReplacer(false).replace("$1", "").trim();
		if (s.startsWith(inpRepl)) {
			s = s.substring(2);
		}
		// END KGU#108 2015-12-15
		// START KGU#281 2016-10-15: Enh. #271 cope with an empty input with prompt
		else if (s.endsWith(";  " + inpRepl)) {
			s = s.substring(0, s.length() - inpRepl.length()-1) + s.substring(s.length() - inpRepl.length()+2);
		}
		//END KGU#281


		// Math function
		s=s.replace("cos(", "Math.cos(");
		s=s.replace("sin(", "Math.sin(");
		s=s.replace("tan(", "Math.tan(");
		// START KGU#17 2014-10-22: After the previous replacements the following 3 strings would never be found!
		//s=s.replace("acos(", "Math.acos(");
		//s=s.replace("asin(", "Math.asin(");
		//s=s.replace("atan(", "Math.atan(");
		// This is just a workaround; A clean approach would require a genuine lexical scanning in advance
		s=s.replace("aMath.cos(", "Math.acos(");
		s=s.replace("aMath.sin(", "Math.asin(");
		s=s.replace("aMath.tan(", "Math.atan(");
		// END KGU#17 2014-10-22
		s=s.replace("abs(", "Math.abs(");
		s=s.replace("round(", "Math.round(");
		s=s.replace("min(", "Math.min(");
		s=s.replace("max(", "Math.max(");
		s=s.replace("ceil(", "Math.ceil(");
		s=s.replace("floor(", "Math.floor(");
		s=s.replace("exp(", "Math.exp(");
		s=s.replace("log(", "Math.log(");
		s=s.replace("sqrt(", "Math.sqrt(");
		s=s.replace("pow(", "Math.pow(");
		s=s.replace("toRadians(", "Math.toRadians(");
		s=s.replace("toDegrees(", "Math.toDegrees(");
		// clean up ... if needed
		s=s.replace("Math.Math.", "Math.");

		return s.trim();
	}

	// START KGU#16 2015-11-29
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformType(java.lang.String, java.lang.String)
	 */
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		_type = _type.toLowerCase();
		_type = _type.replace("short int", "int");
		_type = _type.replace("short", "int");
		_type = _type.replace("unsigned int", "int");
		_type = _type.replace("unsigned long", "long");
		_type = _type.replace("unsigned char", "char");
		_type = _type.replace("unsigned", "int");
		_type = _type.replace("longreal", "double");
		_type = _type.replace("real", "double");
		//_type = _type.replace("boole", "boolean");
		//_type = _type.replace("bool", "boolean");
		if (_type.matches("(^|.*\\W+)bool(\\W+.*|$)")) {
			_type = _type.replaceAll("(^|.*\\W+)bool(\\W+.*|$)", "$1boolean$2");
		}
		_type = _type.replace("character", "Character");
		_type = _type.replace("integer", "Integer");
		_type = _type.replace("string", "String");
		_type = _type.replace("array[ ]?([0-9]*)[ ]of char", "String");	// FIXME (KGU 2016-01-14) doesn't make much sense
		return _type;
	}
	// END KGU#16 2015-11-29

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
		boolean isDisabled = _for.isDisabled();
		// We simply use the range-based loop of Java (as far as possible)
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		StringList items = this.extractForInListItems(_for);
		String indent = _indent;
		String itemType = null;
		if (items != null)
		{
			valueList = "{" + items.concatenate(", ") + "}";
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogenous? We will just try three types: int,
			// double and String, and if none of them match we add a TODO comment.
			int nItems = items.count();
			boolean allInt = true;
			boolean allDouble = true;
			boolean allString = true;
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
			if (allInt) itemType = "int";
			else if (allDouble) itemType = "double";
			else if (allString) itemType = "char*";
			String arrayName = "array20160322";
			
			// Extra block to encapsulate the additional variable declarations
			addCode("{", _indent, isDisabled);
			indent += this.getIndent();
			
			if (itemType == null)
			{
				itemType = "Object";
				this.insertComment("TODO: Select a more sensible item type than Object and/or prepare the elements of the array", indent);
				
			}
			addCode(itemType + "[] " + arrayName + " = " + transform(valueList, false) + ";",
					indent, isDisabled);
			
			valueList = arrayName;
		}
		else
		{
			itemType = "Object";
			this.insertComment("TODO: Select a more sensible item type than Object and/or prepare the elements of the array", indent);
			valueList = transform(valueList, false);
		}

		// Creation of the loop header
		insertBlockHeading(_for, "for (" + itemType + " " + var + " : " +	valueList + ")", indent);

		// Add the loop body as is
		generateCode(_for.q, indent + this.getIndent());

		// Accomplish the loop
		insertBlockTail(_for, null, indent);

		if (items != null)
		{
			addCode("}", _indent, isDisabled);
		}
		
		return true;
	}
	// END KGU#61 2016-03-22
	
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
		if (topLevel)
		{
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			code.add("");
			subroutineInsertionLine = code.count();	// default position for subroutines
			subroutineIndent = _indent;
		}
		else
		{
			code.add("");
		}
		// END KGU#178 2016-07-20
		if (_root.isProgram==true) {
			if (topLevel && this.hasInput()) {
				code.add(_indent + "import java.util.Scanner;");
				code.add("");
			}
			insertBlockComment(_root.getComment(), _indent, "/**", " * ", " */");
			insertBlockHeading(_root, "public class " + _procName, _indent);

			code.add("");
			insertComment("TODO Declare and initialise class variables here", this.getIndent());
			code.add("");
			code.add(_indent+this.getIndent() + "/**");
			code.add(_indent+this.getIndent() + " * @param args");
			code.add(_indent+this.getIndent() + " */");

			insertBlockHeading(_root, "public static void main(String[] args)", _indent+this.getIndent());
		}
		else {
			insertBlockComment(_root.getComment(), _indent+this.getIndent(), "/**", " * ", null);
			insertBlockComment(_paramNames, _indent+this.getIndent(), null, " * @param ", null);
			if (_resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
			{
				code.add(_indent+this.getIndent() + " * @return ");
				_resultType = transformType(_resultType, "int");
				// START KGU#140 2017-02-01: Enh. #113: Proper conversion of array types
				_resultType = this.transformArrayDeclaration(_resultType, "");
				// END KGU#140 2017-02-01
			}
			else {
				_resultType = "void";		        	
			}
			code.add(_indent+this.getIndent() + " */");
			// START KGU#178 2016-07-20: Enh. #160 - insert called subroutines as private
			//String fnHeader = "public static " + _resultType + " " + _procName + "(";
			String fnHeader = (topLevel ? "public" : "private") + " static "
					+ _resultType + " " + _procName + "(";
			// END KGU#178 2016-07-20
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				// START KGU#140 2017-02-01: Enh. #113: Proper conversion of array types
				//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
				//		_paramNames.get(p)).trim();
				fnHeader += transformArrayDeclaration(transformType(_paramTypes.get(p), "/*type?*/").trim(), _paramNames.get(p));
				// END KGU#140 2017-02-01
			}
			fnHeader += ")";
			insertBlockHeading(_root, fnHeader,  _indent + this.getIndent());
		}

		return _indent + this.getIndent() + this.getIndent();
	}

	// START KGU#332 2017-01-30: Method decomposed - no need to override it anymore
//	/**
//	 * Generates some preamble (i.e. comments, language declaration section etc.)
//	 * and adds it to this.code.
//	 * @param _root - the diagram root element
//	 * @param _indent - the current indentation string
//	 * @param varNames - list of variable names introduced inside the body
//	 */
//	@Override
//	protected String generatePreamble(Root _root, String _indent, StringList varNames)
//	{
//		code.add(_indent);
//		insertComment("TODO: Declare and initialise local variables here:", _indent);
//		for (int v = 0; v < varNames.count(); v++) {
//			insertComment(varNames.get(v), _indent);
//		}
//		code.add(_indent);
//		// START KGU#236 2016-12-22: Issue #227
//		if (this.hasInput(_root)) {
//			insertComment("TODO: You may have to modify input instructions,", _indent);			
//			insertComment("      e.g. by replacing nextLine() with a more suitable call", _indent);
//			insertComment("      according to the variable type, say nextInt().", _indent);			
//			code.add(_indent);
//		}
//		// END KGU#236 2016-12-22
//		return _indent;
//	}
	
	@Override
	protected String makeArrayDeclaration(String _elementType, String _varName, TypeMapEntry typeInfo)
	{
		while (_elementType.startsWith("@")) {
			_elementType = _elementType.substring(1) + "[]";
		}
		return _elementType + " " + _varName; 
	}
	@Override
	protected void generateIOComment(Root _root, String _indent)
	{
		// START KGU#236 2016-12-22: Issue #227
		if (this.hasInput(_root)) {
			insertComment("TODO: You may have to modify input instructions,", _indent);			
			insertComment("      e.g. by replacing nextLine() with a more suitable call", _indent);
			insertComment("      according to the variable type, say nextInt().", _indent);			
			code.add(_indent);
		}
		// END KGU#236 2016-12-22
	}
// END KGU#332 2017-01-30

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
		if ((this.returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns) {
			String result = "0";
			if (isFunctionNameSet) {
				result = _root.getMethodName();
			} else if (isResultSet) {
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
			}
			code.add(_indent);
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}

	// START KGU 2015-12-15: Method block must be closed as well
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root 
	 * @param _indent
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// Method block close
		super.generateFooter(_root, _indent + this.getIndent());

		// Don't close class block if we haven't opened any
		if (_root.isProgram)
		{
			// START KGU#178 2016-07-20: Enh. #160
			// Modify the subroutine insertion position
			subroutineInsertionLine = code.count();
			// END KGU#178 2016-07-20
			
			// Close class block
			code.add("");
			code.add(_indent + "}");
		}
		
		// START KGU#311 2016-12-22: Enh. #314 - insert File API here if necessary
		if (topLevel && this.usesFileAPI) {
			this.insertFileAPI("java");
		}
		// END KGU#311 2016-12-22
	}
	// END KGU 2015-12-15

}

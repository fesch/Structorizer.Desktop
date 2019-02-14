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
 *      Author:         Daniel Spittank
 *
 *      Description:    This class generates Java code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Daniel Spittank         2014-02-01      Starting from Java Generator
 *      Kay Gürtzig             2014-11-16      Conversion of C-like logical operators and arcus functions (see comment)
 *      Kay Gürtzig             2014-12-02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig             2015-10-18      Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig             2015-12-12      bugfix #59 (KGU#104) with respect to ER #10
 *      Kay Gürtzig             2015-12-17      Enh. #23 (KGU#78) jump generation revised; Root generation
 *                                              decomposed according to Generator.generateCode(Root, String);
 *                                              Enh. KGU#47: Dummy implementation for Parallel element
 *      Kay Gürtzig             2015-12-21      Bugfix #41/#68/#69 (= KGU#93)
 *      Kay Gürtzig             2015-12-22      Bugfix #51/#54 (= KGU#108) empty input and output expression lists
 *      Kay Gürtzig             2016-01-14      Enh. #84 (= KGU#100) Array init. expr. support
 *      Kay Gürtzig             2016-01-17      Bugfix #61 (= KGU#109) Type names removed from assignments
 *      Kay Gürtzig             2016-03-16      Enh. #84: Support for FOREACH loops (KGU#61) 
 *      Kay Gürtzig             2016-04-01      Enh. #144: Care for new option to suppress content conversion 
 *      Kay Gürtzig             2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178),
 *                                              bugfix for routine calls (superfluous parentheses dropped)
 *      Kay Gürtzig             2016-09-25      Enh. #253: CodeParser.keywordMap refactoring done
 *      Kay Gürtzig             2016-10-14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016-10-15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig             2016-10-16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig             2016-12-01      Bugfix #301: More precise check for parenthesis enclosing of log. conditions
 *      Kay Gürtzig             2016-12-27      Enh. #314: Support for Structorizer File API
 *      Kay Gürtzig             2017-02-19      Enh. #348: Parallel sections translated with threading module
 *      Kay Gürtzig             2017-02-23      Issue #350: getOutputReplacer() and Parallel export revised again
 *      Kay Gürtzig             2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017-03-10      Bugfix #378, #379: charset annotation / wrong inequality operator
 *      Kay Gürtzig             2017-05-16      Bugfix #51: an empty output instruction produced "print(, sep='')"
 *      Kay Gürtzig             2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2017-05-24      Bugfix #412: hash codes may be negative, therefore used in hexadecimal form now
 *      Kay Gürtzig             2017-10-02/03   Enh. #389, #423: Export of globals and mutable record types implemented
 *      Kay Gürtzig             2017-11-02      Issue #447: Line continuation and Case elements supported
 *      Kay Gürtzig             2018-07-20      Enh. #563 - support for simplified record initializers
 *      Kay Gürtzig             2018-10-17      Issue #623: Turtleizer support was defective (moves, color, new routines),
 *                                              bugfix #624 - FOR loop translation into range() fixed
 *      Kay Gürtzig             2019-02-14      Enh. #680: Support for input instructions with several variables
 *
 ******************************************************************************************************
 *
 *      Comments:
 *
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015.10.18 - Bugfixes and modifications (Kay Gürtzig)
 *      - Comment method signature simplified
 *      - Bugfix: The export option "export instructions as comments" had been ignored before
 *      - Bugfix KGU#54: generateCode(Repeat,String) ate the last two lines of the loop body!
 *      - The indentation logic was somehow inconsistent
 *
 *      2014.11.16 - Bugfixes / Enhancement
 *      - Conversion of C-style logical operators to the Python-conform ones added
 *      - assignment operator conversion now preserves or ensures surrounding spaces
 *      - workaround for reverse trigonometric functions added
 *      - Operator != had been converted to !==
 *      - comment export introduced
 *
 *      2014.02.01 - First Version of Python Generator
 *      
 *      2010.09.10 - Bugfixes
 *      - condition for automatic bracket addition around condition expressions corrected
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator conversion
 *      - pascal function conversion
 *
 *      2009.08.10
 *        - writeln() => System.out.println()
 * 
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.turtle.TurtleBox;
import lu.fisch.diagrcontrol.DiagramController;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;


public class PythonGenerator extends Generator 
{
		
	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export Python ...";
	}

	protected String getFileDescription()
	{
		return "Python Source Code";
	}

	protected String getIndent()
	{
		return "    ";
	}

	protected String[] getFileExtensions()
	{
		String[] exts = {"py"};
		return exts;
	}

	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft()
	{
		return "#";
	}
	// END KGU 2015-10-18

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

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//    private static final String[] reservedWords = new String[]{
//		"and", "assert", "break", "class", "continue",
//		"def", "del",
//		"else", "elif", "except", "exec",
//		"finally", "for", "from", "global",
//		"if", "import", "in", "is", "lambda", "not", "or",
//		"pass", "print", "raise", "return", 
//		"try", "while",
//		"Data", "Float", "Int", "Numeric", "Oxphys",
//		"array", "close", "float", "int", "input",
//		"open", "range", "type", "write", "zeros"
//		};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	public boolean isCaseSignificant()
//	{
//		return false;
//	}
//	// END KGU 2016-08-12
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return "import %";
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/

	// START KGU#388 2017-10-02: Enh. #423 - Support for recordtypes
	/** cache for the type map of the current Root */
	private HashMap<String, TypeMapEntry> typeMap = null;
	/** Pattern for type name extraction from a type definition */
	private static final Pattern PTRN_TYPENAME = Pattern.compile("type (\\w+)\\s*=.*");
	private static Matcher mtchTypename = PTRN_TYPENAME.matcher("");
	// END KGU#388 2017-10-02

	// START KGU#598 2018-10-17: Enh. #490 Improved support for Turtleizer export
	/**
	 * Maps light-weight instances of DiagramControllers for API retrieval
	 * to their respective adapter class names
	 */
	protected static HashMap<DiagramController, String> controllerMap = new HashMap<DiagramController, String>();
	static {
		// FIXME: The controllers should be retrieved from controllers.xml
		// FIXME: For a more generic approach, the adapter class names should be fully qualified
		controllerMap.put(new TurtleBox(), "Turtleizer");
	}
	
	/**
	 * Defines the translations of Turtleizer subroutine names to the turtle module equivalents 
	 */
	private static HashMap<String, String> turtleMap = new HashMap<String, String>();
	static {
		turtleMap.put("gotoXY", "goto");
		turtleMap.put("getX", "xcor");
		turtleMap.put("getY", "ycor");
		turtleMap.put("getOrientation", "heading");
		turtleMap.put("setPenColor", "pencolor");
		turtleMap.put("setBackgroundColor", "bgcolor");
	}
	// END KGU#598 2018-10-17

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	// START KGU#281 2016-10-15: Enh. #281
	//protected String getInputReplacer()
	//{
	//	return "$1 = input(\"$1\")";
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "$2 = input($1)";				
		}
		return "$1 = input(\"$1\")";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		// START KGU#108 2015-12-22: Bugfix #51, #54: Parenthesis was rather wrong (produced lists)
		//return "print($1)";
		// START KGU 2017-02-23: for Python 3.5, parentheses ARE necessary, separator is to be suppressed
		//return "print $1";
		return "print($1, sep='')";
		// END KGU 2017-02-23
		// END KGU#108 2015-12-22
	}

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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			if (Function.testIdentifier(token, null)) {
				int j = i;
				// Skip all whitespace
				while (j+2 < tokens.count() && tokens.get(++j).trim().isEmpty());
				// START KGU#480 2018-01-21: Enh. 490 - more precise detection
				//String turtleMethod = null;
				//if (j+1 < tokens.count() && tokens.get(j).equals("(") && (turtleMethod = Turtleizer.checkRoutine(token)) != null) {
				//	tokens.set(i, turtleMethod);
				//	this.usesTurtleizer = true;
				//}
				if (j+1 < tokens.count() && tokens.get(j).equals("(")) {
					int nArgs = Element.splitExpressionList(tokens.subSequence(j+1, tokens.count()), ",", false).count();
					for (Entry<DiagramController, String> entry: controllerMap.entrySet()) {
						String name = entry.getKey().providedRoutine(token, nArgs);
						if (name != null) {
							if (entry.getKey() instanceof TurtleBox) {
								this.usesTurtleizer = true;
								if (turtleMap.containsKey(name)) {
									name = turtleMap.get(name);
								}
								tokens.set(i, "turtle." + name.toLowerCase());
							}
							else {
								tokens.set(i, entry.getValue() + "." +name);
							}
						}
					}
				}
				// END KGU#480 2018-01-21
			}
		}
		// START KGU 2014-11-16: C comparison operator required conversion before logical ones
		// START KGU#367 2017-03-10: Bugfix #379 - this conversion was wrong
		//tokens.replaceAll("!="," <> ");
		// END KGU#368 2017-03-10
		// convert C logical operators
		tokens.replaceAll("&&"," and ");
		tokens.replaceAll("||"," or ");
		tokens.replaceAll("!"," not ");
		tokens.replaceAll("xor","^");            
		// END KGU 2014-11-16
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		// START KGU#388 2017-10-02: Enh. #423 - convert Structorizer record initializers to Python
		transformRecordInitializers(tokens);
		// END KGU#388 2017-10-02
		// START KGU#100 2016-01-14: Enh. #84 - convert C/Java initialisers to lists
		tokens.replaceAll("{", "[");
		tokens.replaceAll("}", "]");
		// END KGU#100 2016-01-14
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21

	// START KGU#388 2017-10-02: Enh. #423 Record support
	/**
	 * Recursively looks for record initializers in the tokens and if there are some replaces
	 * the respective type name token by the entire transformed record initializer as single
	 * string element and hence immune against further token manipulations.
	 * @param tokens - the token list of the split line, will be modified.
	 */
	private void transformRecordInitializers(StringList tokens) {
		int posLBrace = -1;
		while ((posLBrace = tokens.indexOf("{", posLBrace+1)) > 0) {
			String prevToken = "";
			TypeMapEntry typeEntry = null;
			// Go back to the last non-empty token
			int pos = posLBrace - 1;
			while (pos >= 0 && (prevToken = tokens.get(pos).trim()).isEmpty()) pos--;
			if (pos >= 0 && Function.testIdentifier(prevToken, null)
					&& (typeEntry = this.typeMap.get(":" + prevToken)) != null
					// Should be a record type but we better make sure.
					&& typeEntry.isRecord()) {
				// We will now reorder the elements and drop the names
				// START KGU#559 2018-07-20: Enh. #563 - smarter record initialization
				//HashMap<String, String> comps = Instruction.splitRecordInitializer(tokens.concatenate("", posLBrace));
				HashMap<String, String> comps = Instruction.splitRecordInitializer(tokens.concatenate("", posLBrace), typeEntry);
				// END KGU#559 2018-07-20
				LinkedHashMap<String, TypeMapEntry> compDefs = typeEntry.getComponentInfo(true);
				String tail = comps.get("§TAIL§");	// String part beyond the initializer
				String sepa = "(";	// initial "separator" is the opening parenthesis, then it will be comma
				for (String compName: compDefs.keySet()) {
					if (comps.containsKey(compName)) {
						prevToken += sepa + transformTokens(Element.splitLexically(comps.get(compName), true));
					}
					else {
						prevToken += sepa + "None";
					}
					sepa = ", ";
				}
				prevToken += ")";
				tokens.set(pos, prevToken);
				tokens.remove(pos+1, tokens.count());
				if (tail != null) {
					// restore the tokens of the remaining text.
					tokens.add(Element.splitLexically(tail, true));
				}
				posLBrace = pos;
			}
		}
	}
	// END KGU#388 2017-10-02

	// END KGU#18/KGU#23 2015-11-01

	// START KGU#108 2015-12-22: Bugfix #51
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	@Override
	protected String transformInput(String _interm)
	{
		String transformed = super.transformInput(_interm);
		if (transformed.startsWith(" = input("))
		{
			transformed = "generatedDummy" + transformed;
		}
		return transformed;
	}
	// END KGU#108 2015-12-22

	// START KGU#399 2017-05-16: Bugfix #51
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformOutput(java.lang.String)
	 */
	protected String transformOutput(String _interm)
	{
		String transf = super.transformOutput(_interm);
		if (this.getOutputReplacer().replace("$1", "").equals(transf)) {
			transf = "print(\"\")";
		}
		return transf;
	}
	// END KGU#399 2017-05-16		

	// START KGU#18/KGU#23 2015-11-01: Obsolete    
//	private String transform(String _input)
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		// START KGU#162 2016-04-01: Enh. #144 - hands off in "no conversion" mode!
		if (!this.suppressTransformation)
		{
			// END KGU#162 2016-04-01
			// START KGU#109 2016-01-17: Bugfix #61 - Remove type specifiers
			// Could we also do it by replacing all inventable type names by empty strings
			// in transformType()? Rather not.
			_input = Element.unifyOperators(_input);
			int asgnPos = _input.indexOf("<-");	// This might mutilate string literals!
			if (asgnPos > 0)
			{
				String lval = _input.substring(0, asgnPos).trim();
				String expr = _input.substring(asgnPos + "<-".length()).trim();
				String[] typeNameIndex = this.lValueToTypeNameIndexComp(lval);
				String index = typeNameIndex[2];
				if (!index.isEmpty()) index = "[" + index + "]";
				// START KGU#388 2017-09-27: Enh. #423
				//_input = typeNameIndex[1] + index + " <- " + expr;
				_input = typeNameIndex[1] + index + typeNameIndex[3] + " <- " + expr;
				// END KGU#388 2017-09-27: Enh. #423
			}
			// END KGU#109 2016-01-17
			// START KGU#162 2016-04-01: Enh. #144 - hands off in "no conversion" mode!
		}
		// END KGU#162 2016-04-01

		_input = super.transform(_input);

		String s = _input;

//		// START KGU 2014-11-16: C comparison operator required conversion before logical ones
//		_input=BString.replace(_input,"!="," <> ");
//		// convert C logical operators
//		_input=BString.replace(_input," && "," and ");
//		_input=BString.replace(_input," || "," or ");
//		_input=BString.replace(_input," ! "," not ");
//		_input=BString.replace(_input,"&&"," and ");
//		_input=BString.replace(_input,"||"," or ");
//		_input=BString.replace(_input,"!"," not ");
//		_input=BString.replace(_input," xor "," ^ ");            
//		// END KGU 2014-11-16
//
//		// convert Pascal operators
//		_input=BString.replace(_input," div "," / ");
//
//		s = _input;
		// Math function
		s = s.replace("cos(", "math.cos(");
		s = s.replace("sin(", "math.sin(");
		s = s.replace("tan(", "math.tan(");
		// START KGU 2014-11-16: After the previous replacements the following 3 strings would never be found!
		//s=s.replace("acos(", "math.acos(");
		//s=s.replace("asin(", "math.asin(");
		//s=s.replace("atan(", "math.atan(");
		// This is just a workaround; A clean approach would require a genuine lexical scanning in advance
		s = s.replace("amath.cos(", "math.acos(");
		s = s.replace("amath.sin(", "math.asin(");
		s = s.replace("amath.tan(", "math.atan(");
		// END KGU 2014-11-16
		//s=s.replace("abs(", "abs(");
		//s=s.replace("round(", "round(");
		//s=s.replace("min(", "min(");
		//s=s.replace("max(", "max(");
		s = s.replace("ceil(", "math.ceil(");
		s = s.replace("floor(", "math.floor(");
		s = s.replace("exp(", "math.exp(");
		s = s.replace("log(", "math.log(");
		s = s.replace("sqrt(", "math.sqrt(");
		s = s.replace("pow(", "math.pow(");
		s = s.replace("toRadians(", "math.radians(");
		s = s.replace("toDegrees(", "math.degrees(");
		// clean up ... if needed
		//s=s.replace("Math.Math.", "math.");

		return s.trim();
	}

	protected void generateCode(Instruction _inst, String _indent)
	{
		if(!insertAsComment(_inst, _indent)) {
			boolean isDisabled = _inst.isDisabled();
			// START KGU 2014-11-16
			insertComment(_inst, _indent);
			// END KGU 2014-11-16
			StringList lines = _inst.getUnbrokenText();
			String tmpCol = null;
			for(int i = 0; i < lines.count(); i++)
			{
				// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
				//code.add(_indent + transform(_inst.getText().get(i)));
				String line = lines.get(i);
				String codeLine = transform(line);
				boolean done = false;

				// START KGU#653 2019-02-14: Enh. #680 - face input instructions with multiple variables
				StringList inputItems = Instruction.getInputItems(line);
				if (inputItems != null && inputItems.count() > 2) {
					String prompt = inputItems.get(0);
					if (!prompt.isEmpty()) {
						prompt += " ";
					}
					for (int j = 1; j < inputItems.count(); j++) {
						String subLine = CodeParser.getKeyword("input") + " " + prompt + inputItems.get(j);
						addCode(transform(subLine), _indent, isDisabled);
					}
					done = true;
				}
				// END KGU#653 2019-02-14
				else if (Instruction.isTurtleizerMove(line)) {
					// START KGU#599 2018-10-17: Bugfix #623 (turtle moves hadn't been exported)
					//codeLine += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
					//done = true;
					if (tmpCol == null) {
						tmpCol = "col" + Integer.toHexString(_inst.hashCode());
						String hexCol = _inst.getHexColor();
						// White elements induce black pen colour
						if (hexCol.equals("ffffff")) hexCol = "000000";
						addCode(tmpCol + " = turtle.pencolor(); turtle.pencolor(\"#" + hexCol + "\")", _indent, isDisabled);
					}
					// END KGU#599 2018-10-17
				}
				// START KGU#388 2017-10-02: Enh. #423 translate record types into mutable recordtype
				else if (Instruction.isTypeDefinition(line)) {
					mtchTypename.reset(line).matches();
					String typeName = mtchTypename.group(1);
					done = this.generateTypeDef(Element.getRoot(_inst), typeName, null, _indent, isDisabled);
				}
				// END KGU#388 2017-10-02
				if (!done) {
					addCode(codeLine, _indent, isDisabled);
				}
				// END KGU#277/KGU#284 2016-10-13/16
			}
			// START KGU#599 2018-10-17: Bugfix #623 make color effective
			if (tmpCol != null) {
				addCode("turtle.pencolor(" + tmpCol + ")", _indent, isDisabled);
			}
			// END KGU#599 2018-10-17
		}
	}

	protected void generateCode(Alternative _alt, String _indent)
	{
		boolean isDisabled = _alt.isDisabled();

		// START KGU 2014-11-16
		insertComment(_alt, _indent);
		// END KGU 2014-11-16

		String condition = transform(_alt.getUnbrokenText().getLongString()).trim();
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
		if (!isParenthesized(condition)) condition = "(" + condition + ")";
		// END KGU#301 2016-12-01

		addCode("if "+condition+":", _indent, isDisabled);
		generateCode((Subqueue) _alt.qTrue,_indent + this.getIndent());
		if(_alt.qFalse.getSize()!=0)
		{
			addCode("else:", _indent, isDisabled);
			generateCode((Subqueue) _alt.qFalse, _indent + this.getIndent());
		}
		// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
		//code.add("");
		if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
		{
			addCode("", "", isDisabled);
		}
		// END KGU#54 2015-10-19
	}

	protected void generateCode(Case _case, String _indent)
	{
		boolean isDisabled = _case.isDisabled();

		// START KGU 2014-11-16
		insertComment(_case, _indent);
		// END KGU 2014-11-16

		// START KGU#453 2017-11-02: Issue #447
		//StringList lines = _case.getText();
		StringList lines = _case.getUnbrokenText();
		// END KGU#453 2017-11-02
		String condition = transform(lines.get(0));

		for(int i=0; i<_case.qs.size()-1; i++)
		{
			String caseline = _indent + ((i == 0) ? "if" : "elif") + " (";
			// START KGU#15 2015-10-21: Support for multiple constants per branch
			StringList constants = StringList.explode(lines.get(i+1), ",");
			for (int j = 0; j < constants.count(); j++)
			{
				if (j > 0) caseline = caseline + " or ";
				caseline = caseline + "(" + condition + ") == " + constants.get(j).trim();
			}
			// END KGU#15 2015-10-21
			addCode(caseline + ") :", "", isDisabled);
			generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent());
		}

		// START KGU#453 2017-11-02: Issue #447
		//if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		if (!lines.get(_case.qs.size()).trim().equals("%"))
			// END KGU#453 2017-11-02
		{
			addCode("else:", _indent, isDisabled);
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent + this.getIndent());
		}
		// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
		//code.add("");
		if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
		{
			addCode("", "", isDisabled);
		}
		// END KGU#54 2015-10-19
	}

	protected void generateCode(For _for, String _indent)
	{
		boolean isDisabled = _for .isDisabled();

		// START KGU 2014-11-16
		insertComment(_for, _indent);
		// END KGU 2014-11-16

		String counterStr = _for.getCounterVar();
		String valueList = "";
		if (_for.isForInLoop())
		{
			valueList = _for.getValueList();
			StringList items = this.extractForInListItems(_for);
			if (items != null)
			{
				valueList = "[" + transform(items.concatenate(", "), false) + "]";
			}
		}
		else 
		{
			String startValueStr = this.transform(_for.getStartValue());
			String endValueStr = this.transform(_for.getEndValue());
			String stepValueStr = _for.getStepString();
			// START KGU#600 2018-10-17: Bugfix #624 - range implements a half-open interval ...
			if (stepValueStr.startsWith("-")) {
				endValueStr += "-1";
			}
			else {
				endValueStr += "+1";
			}
			// END KGU#600 2018-10-17
			valueList = "range("+startValueStr+", "+endValueStr+", "+stepValueStr+")";
		}
		addCode("for "+counterStr+" in " + valueList + ":", _indent, isDisabled);
		generateCode((Subqueue) _for.q,_indent + this.getIndent());
		// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
		//code.add("");
		if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
		{
			addCode("", "", isDisabled);
		}
		// END KGU#54 2015-10-19
	}

	protected void generateCode(While _while, String _indent)
	{
		boolean isDisabled = _while.isDisabled();

		// START KGU 2014-11-16
		insertComment(_while, _indent);
		// END KGU 2014-11-16

		String condition = transform(_while.getUnbrokenText().getLongString()).trim();
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
		if (!isParenthesized(condition)) condition = "(" + condition + ")";
		// END KGU#301 2016-12-01

		addCode("while "+condition+":", _indent, isDisabled);
		generateCode((Subqueue) _while.q, _indent + this.getIndent());
		// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
		//code.add("");
		if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
		{
			addCode("", "", isDisabled);
		}
		// END KGU#54 2015-10-19
	}

	protected void generateCode(Repeat _repeat, String _indent)
	{
		boolean isDisabled = _repeat.isDisabled();

		// START KGU 2014-11-16
		insertComment(_repeat, _indent);
		// END KGU 2014-11-16
		addCode("while True:", _indent, isDisabled);
		generateCode((Subqueue) _repeat. q,_indent + this.getIndent());
		// START KGU#54 2015-10-19: Why should the last two rows be empty? They aren't! This strange behaviour ate code lines! 
		//code.delete(code.count()-1); // delete empty row
		//code.delete(code.count()-1); // delete empty row
		// END KGU#54 2015-10-19
		addCode("if " + transform(_repeat.getUnbrokenText().getLongString()).trim()+":",
				_indent + this.getIndent(), isDisabled);
		addCode("break", _indent+this.getIndent()+this.getIndent(), isDisabled);
		// START KGU#54 2015-10-19: Add an empty line, but void accumulation of empty lines!
		if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
		{
			addCode("", "", isDisabled);
		}
		// END KGU#54 2015-10-19
	}

	protected void generateCode(Forever _forever, String _indent)
	{
		boolean isDisabled = _forever.isDisabled();

		// START KGU 2014-11-16
		insertComment(_forever, _indent);
		// END KGU 2014-11-16
		addCode("while True:", _indent, isDisabled);
		generateCode((Subqueue) _forever.q, _indent + this.getIndent());
		// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
		//code.add("");
		if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
		{
			addCode("", "", isDisabled);
		}
		// END KGU#54 2015-10-19
	}

	protected void generateCode(Call _call, String _indent)
	{
		if(!insertAsComment(_call, _indent))
		{
			boolean isDisabled = _call.isDisabled();
			// START KGU 2014-11-16
			insertComment(_call, _indent);
			// END KGU 2014-11-16
			StringList lines = _call.getText();
			for(int i=0; i<lines.count(); i++)
			{
				// START KGU 2016-07-20: Bugfix the extra parentheses were nonsense
				//code.add(_indent+transform(_call.getText().get(i))+"()");
				addCode(transform(lines.get(i)), _indent, isDisabled);
				// END KGU 2016-07-20
			}
		}
	}

	protected void generateCode(Jump _jump, String _indent)
	{
		if(!insertAsComment(_jump, _indent))
		{
			boolean isDisabled = _jump.isDisabled();
			// START KGU 2014-11-16
			insertComment(_jump, _indent);
			// END KGU 2014-11-16
			// START KGU#78 2015-12-17: Enh. 38 - translate acceptable Jumps to break instructions
			//for(int i=0;i<_jump.getText().count();i++)
			//{
			//	insertComment(transform(_jump.getText().get(i))+" # FIXME goto instructions not allowed in Python", _indent);
			//}
			// In case of an empty text generate a break instruction by default.
			boolean isEmpty = true;

			StringList lines = _jump.getUnbrokenText();
			String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
			String preLeave  = CodeParser.getKeywordOrDefault("preLeave", "leave");
			String preReturnMatch = Matcher.quoteReplacement(preReturn)+"([\\W].*|$)";
			String preLeaveMatch  = Matcher.quoteReplacement(preLeave)+"([\\W].*|$)";
			for (int i = 0; isEmpty && i < lines.count(); i++) {
				String line = transform(lines.get(i)).trim();
				if (!line.isEmpty())
				{
					isEmpty = false;
				}
				if (line.matches(preReturnMatch))
				{
					addCode("return " + line.substring(preReturn.length()).trim(),
							_indent, isDisabled);
				}
				else if (line.matches(preLeaveMatch))
				{
					// We may only allow one-level breaks, i. e. there must not be an argument
					// or the argument must be 1 and a legal label must be associated.
					String arg = line.substring(preLeave.length()).trim();
					Integer label = this.jumpTable.get(_jump);
					if (label != null && label.intValue() >= 0 &&
							(arg.isEmpty() || Integer.parseInt(arg) == 1))
					{
						addCode("break", _indent, isDisabled);		
					}
					else
					{
						addCode("break # FIXME: Illegal multi-level break attempted!",
								_indent, isDisabled);
					}
				}
				else if (!isEmpty)
				{
					insertComment("FIXME: unsupported jump/exit instruction!", _indent);
					insertComment(line, _indent);
				}
			}
			if (isEmpty) {
				addCode("break", _indent, isDisabled);
			}
			// END KGU#78 2015-12-17
		}
	}

	// START KGU#47 2015-12-17: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		boolean isDisabled = _para.isDisabled();
		Root root = Element.getRoot(_para);
		String suffix = Integer.toHexString(_para.hashCode());

		//String indentPlusOne = _indent + this.getIndent();
		//String indentPlusTwo = indentPlusOne + this.getIndent();
		insertComment(_para, _indent);

		addCode("", _indent, isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		//insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
		//addCode("", indentPlusOne, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			//insertComment("----------------- START THREAD " + i + " -----------------", indentPlusOne);
			// START KGU#348 2017-02-19: Enh. #348 Actual translation
			//generateCode((Subqueue) _para.qs.get(i), indentPlusTwo);
			Subqueue sq = _para.qs.get(i);
			String threadVar = "thr" + suffix + "_" + i;
			String threadFunc = "thread" + suffix + "_" + i;
			StringList used = root.getUsedVarNames(sq, false, false);
			StringList asgnd = root.getVarNames(sq, false);
			for (int v = 0; v < asgnd.count(); v++) {
				used.removeAll(asgnd.get(v));
			}
			String args = used.concatenate(",");
			if (used.count() == 1) {
				args += ",";
			}
			if (sq.getSize() == 1) {
				Element el = sq.getElement(0);
				if (el instanceof Call && ((Call)el).isProcedureCall()) {
					threadFunc = ((Call)el).getCalledRoutine().getName();
				}
			}
			addCode(threadVar + " = Thread(target=" + threadFunc + ", args=(" + args + "))", _indent, isDisabled);
			addCode(threadVar + ".start()", _indent, isDisabled);
			addCode("", _indent, isDisabled);
			// END KGU#348 2017-02-19
			//insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			//addCode("", indentPlusOne, isDisabled);
		}

		for (int i = 0; i < _para.qs.size(); i++) {
			String threadVar = "thr" + suffix + "_" + i;
			addCode(threadVar + ".join()", _indent, isDisabled);
		}
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		addCode("", _indent, isDisabled);
	}
	// END KGU#47 2015-12-17

	// START KGU#47/KGU#348 2017-02-19: Enh. #348
	private void generateParallelThreadFunctions(Root _root, String _indent)
	{
		String indentPlusOne = _indent + this.getIndent();
		int lineBefore = code.count();
		StringList globals = new StringList();
		final LinkedList<Parallel> containedParallels = new LinkedList<Parallel>();
		_root.traverse(new IElementVisitor() {
			@Override
			public boolean visitPreOrder(Element _ele) {
				return true;
			}
			@Override
			public boolean visitPostOrder(Element _ele) {
				if (_ele instanceof Parallel) {
					containedParallels.addLast((Parallel)_ele);
				}
				return true;
			}
		});
		for (Parallel par: containedParallels) {
			boolean isDisabled = par.isDisabled();
			String functNameBase = "thread" + Integer.toHexString(par.hashCode()) + "_";
			int i = 0;
			// We still don't care for synchronisation, mutual exclusion etc.
			for (Subqueue sq: par.qs) {
				Element el = null;
				if (sq.getSize() == 1 && (el = sq.getElement(0)) instanceof Call && ((Call)el).isProcedureCall()) {
					// Don't generate a thread function for single procedure calls
					continue;
				}
				// Variables assigned here will be made global
				StringList setVars = _root.getVarNames(sq, false);
				// Variables used here without being assigned will be made arguments
				StringList usedVars = _root.getUsedVarNames(sq, false, false);
				for (int v = 0; v < setVars.count(); v++) {
					usedVars.removeAll(setVars.get(v));
				}
				addCode("def " + functNameBase + i + "(" + usedVars.concatenate(", ") + "):", _indent, isDisabled);
				for (int v = 0; v < setVars.count(); v++) {
					String varName = setVars.get(v);
					globals.addIfNew((isDisabled ? this.commentSymbolLeft() : "") + varName);
					addCode("global " + varName, indentPlusOne, isDisabled);
				}
				generateCode(sq, indentPlusOne);
				code.add(_indent);
				i++;
			}
		}
		if (globals.count() > 0) {
			code.insert(_indent + "", lineBefore);
			for (int v = 0; v < globals.count(); v++) {
				String varName = globals.get(v);
				String end = varName.startsWith(this.commentSymbolLeft()) ? this.commentSymbolRight() : "";
				code.insert(_indent + varName + " = 0" + end, lineBefore);
			}
			code.insert(_indent + this.commentSymbolLeft() + " TODO: Initialize these variables globally referred to by prallel threads in a sensible way!", lineBefore);
		}
	}
	// END KGU#47/KGU#348 2017-02-19

	// START KGU#388 2017-10-02: Enh. #423 Translate record types to mutable recordtypes
	/**
	 * Inserts a typedef or struct definition for the type passed in by {@code _typeEnry}
	 * if it hadn't been defined globally or in the preamble before.
	 * @param _root - the originating Root
	 * @param _type - the type map entry the definition for which is requested here
	 * @param _indent - the current indentation
	 * @param _asComment - if the type deinition is only to be added as comment (disabled)
	 */
	protected boolean generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
		boolean done = false;
		String typeKey = ":" + _typeName;
		if (_type == null) {
			_type = this.typeMap.get(typeKey);
		}
		if (this.wasDefHandled(_root, typeKey, false)
				|| _type != null && _type.isNamed() && this.wasDefHandled(_root, ":" + _type.typeName, false)) {
			// It was not done now and here again but there's nothing more to do, so it's "done"
			return true;
		}
		else if (_type == null) {
			// We leave it to the caller what to do
			return false;
		}
		setDefHandled(_root.getSignatureString(false), typeKey);
		if (_type.isRecord()) {
			String typeDef = _type.typeName + " = recordtype(\"" + _type.typeName + "\" \"";
			for (String compName: _type.getComponentInfo(false).keySet()) {
				typeDef += compName + " ";
			}
			typeDef = typeDef.trim() + "\")";
			addCode(typeDef, _indent, _asComment);
			done = true;
		}
		return done;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#insertGlobalInitialisations(java.lang.String)
	 */
	protected void insertGlobalInitialisations(String _indent) {
		if (topLevel) {
			int startLine = code.count();
			for (Root incl: this.includedRoots.toArray(new Root[]{})) {
				insertComment("BEGIN (global) code from included diagram \"" + incl.getMethodName() + "\"", _indent);
				typeMap = incl.getTypeInfo();	// This line is the difference to Generator!
				generateCode(incl.children, _indent);
				insertComment("END (global) code from included diagram \"" + incl.getMethodName() + "\"", _indent);
			}
			if (code.count() > startLine) {
				code.add(_indent);
			}
		}
	}

	/**
	 * Declares imported names as global
	 * @param _root - the Root being exported
	 * @param _indent - the current indentation level
	 * @see #insertGlobalInitialisations(String)
	 */
	private void insertGlobalDeclarations(Root _root, String _indent) {
		if (_root.includeList != null) {
			HashSet<String> declared = new HashSet<String>();
			for (Root incl: this.includedRoots) {
				if (_root.includeList.contains(incl.getMethodName())) {
					// Start with the types
					for (String name: incl.getTypeInfo().keySet()) {
						if (name.startsWith(":") && !declared.contains((name = name.substring(1)))) {
							addCode("global " + name, _indent, false);
							declared.add(name);								
						}
					}
					// Now add the variables (including constants)
					StringList names = incl.getVarNames();
					for (int i = 0; i < names.count(); i++)
					{
						String name = names.get(i);
						if (!declared.contains(name)) {
							addCode("global " + name, _indent, false);
							declared.add(name);
						}
					}
				}
			}
			if (!declared.isEmpty()) {
				addCode("", _indent, false);
			}
		}
	}
	// END KGU#388 2017-10-02

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		String indent = "";
		if (topLevel)
		{
			code.add(_indent + "#!/usr/bin/env python");
			// START KGU#366 2017-03-10: Issue #378 the used character set is to be named 
			code.add(_indent + "# -*- coding: " + this.getExportCharset().toLowerCase() + " -*-");
			// END KGU#366 2017-03-10
			insertComment(_root.getText().get(0), _indent);
			insertComment("generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			insertCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			// START KGU#348 2017-02-19: Enh. #348 - Translation of parallel sections
			if (this.hasParallels) {
				code.add(_indent);
				code.add(_indent + "from threading import Thread");
			}
			// END KGU#348 2017-02-19
			// START KGU#607 2018-10-30:Issue #346
			this.generatorIncludes.add("math");		// Will be inserted later
			// END KGU#607 2018-10-30
			// START KGU#351 2017-02-26: Enh. #346
			this.insertUserIncludes(indent);
			// END KGU#351 2017-02-26
			// START KGU#600 2018-10-17: It is too cumbersome to check if math is actually needed
			code.add(_indent + "import math");
			// END KGU#600 2018-10-17
			// START KGU#598 2018-10-17: Enh. #623
			this.includeInsertionLine = code.count();
			// END KGU#598 2018-10-17
			// START KGU#376 2017-10-02: Enh. #389 - insert the code of the includables first
			this.insertGlobalInitialisations(_indent);
			// END KGU#376 2017-10-02
//			if (code.count() == this.includeInsertionLine) {
//				code.add(_indent);
//			}
			subroutineInsertionLine = code.count();
			// START KGU#311 2016-12-27: Enh. #314: File API support
			if (this.usesFileAPI) {
				this.insertFileAPI("py");
			}
			// END KGU#311 2016-12-27
		}
		code.add("");
		// FIXME: How to handle includables here?
		if (!_root.isSubroutine()) {
			insertComment(_root, _indent);
		}
		else {
			indent = _indent + this.getIndent();
			insertComment(_root, _indent);
			code.add(_indent + "def " + _procName +"(" + _paramNames.getText().replace("\n", ", ") +") :");
		}
		// START KGU#388 2017-10-02: Enh. #423 type info will now be needed in deep contexts
		this.typeMap = _root.getTypeInfo();
		// END KGU#388 2017-10-02
		return indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		// START KGU#376 2017-10-03: Enh. #389 - Variables and types of the included diagrams must be marked as global here
		insertGlobalDeclarations(_root, _indent);
		// END KGU#376 2017-10-03
		// START KGU#348 2017-02-19: Enh. #348 - Translation of parallel sections
		generateParallelThreadFunctions(_root, _indent);
		// END KGU#348 2017-02-19
		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateResult(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if (_root.isSubroutine() && (returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
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
			code.add(_indent + "return " + result);
		}
		return _indent;
	}
	// END KGU#78 2015-12-17

	// START KGU#598 2018-10-17: Enh. #  turtle import may have to be inserted
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root 
	 * @param _indent
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// Just pro forma
		super.generateFooter(_root, _indent + this.getIndent());

		// START KGU#598 2018-10-17: Enh. #623
		if (topLevel && this.usesTurtleizer) {
			code.insert("import turtle", this.includeInsertionLine);
			code.insert("turtle.colormode(255)", this.includeInsertionLine+1);
			code.insert("turtle.mode(\"logo\")", this.includeInsertionLine+2);
			this.subroutineInsertionLine += 3;
			addCode("turtle.bye()\t" + this.commentSymbolLeft() + " TODO: re-enable this if you want to close the turtle window.", _indent, true);
		}
		// END KGU#598 2018-10-17
	}
	// END KGU 2015-12-15

}

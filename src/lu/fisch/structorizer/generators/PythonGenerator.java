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
 *      Kay Gürtzig             2019-03-08      Enh. #385: Support for parameter default values
 *      Kay Gürtzig             2019-03-21      Issue #706: Export of Calls with explicit argument assignments enabled
 *      Kay Gürtzig             2019-03-21      Issue #707: Mechanism to adjust the file name proposal
 *      Kay Gürtzig             2019-03-26      Bugfix #716: Assignments were transformed defectively
 *      Kay Gürtzig             2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig             2019-05-28      Issue #725: Smarter export of division operator
 *      Kay Gürtzig             2019-11-08      Bugfix #769: Undercomplex selector list splitting in CASE generation mended
 *      Kay Gürtzig             2019-11-24      Bugfix #782: Declaration of global variables corrected
 *      Kay Gürtzig             2019-12-01      Enh. #739: Support for enumerator types
 *      Kay Gürtzig             2020-02-12      Issue #807: records no longer modeled via `recordtype' but as dictionaries
 *      Kay Gürtzig             2020-02-13      Bugfix #812: Defective solution for #782 (global references) mended
 *      Kay Gürtzig             2020-03-08      Bugfix #831: Obsolete shebang and defective export of CALLs as parallel branch
 *      Kay Gürtzig             2020-04-22      Ensured that appendGlobalInitializations() does not eventually overwrite typeMap
 *      Kay Gürtzig             2021-02-03      Issue #920: Transformation for "Infinity" literal
 *      Kay Gürtzig             2021-02-13      Bugfix #935: NullPointerException in generateCode(For...)
 *      Kay Gürtzig             2021-12-05      Bugfix #1024: Precautions against defective record initializers
 *      Kay Gürtzig	            2022-07-04      Issue #1041: Unnecessary nesting of try blocks with finally clause
 *                                              Bugfix #1042: Wrong syntax for catch clauses with variable
 *      Kay Gürtzig             2022-08-14      Bugfix #1061: Suppression of content conversions #423, #623, #680, #782, #812
 *      Kay Gürtzig             2022-08-23      Issue #1068: transformIndexLists() inserted in transformTokens()
 *      Kay Gürtzig             2023-09-29      Issues #1091, #1092: Alias and array type defs now simply suppressed
 *      Kay Gürtzig             2023-10-04      Bugfix #1093 Undue final return 0 on function diagrams eliminated
 *      Kay Gürtzig             2023-10-12      Issue #980 Code generation for multi-variable and array declarations revised
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
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;


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

	// START KGU#690 2019-03-21: Enh. #707 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#ensureFilenameConformity(java.lang.String)
	 */
	@Override
	protected String ensureFilenameConformity(String proposedFilename) {
		// Python file names must not contain hyphens, since the file name is the module name
		return proposedFilename.replace('-', '_');
	}
	// END KGU#690 2019-03-21

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

	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		return OverloadingLevel.OL_DEFAULT_ARGUMENTS;
	}
	// END KGU#371 2019-03-07

	// START KGU#686 2019-03-18: Enh. #56
	/**
	 * Subclassable method to specify the degree of availability of a try-catch-finally
	 * construction in the target language.
	 * @return a {@link TryCatchSupportLevel} value
	 */
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_TRY_CATCH_FINALLY;
	}
	// END KGU#686 2019-03-18

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

	// START KGU#542 2019-12-01: Enh. #739 - support for enumerator types
	/** The currently exported {@link Root} */
	private Root root = null;
	// END KGU#542 2019-12-01
	// START KGU#388 2017-10-02: Enh. #423 - Support for recordtypes
	/** cache for the type map of the current Root */
	private HashMap<String, TypeMapEntry> typeMap = null;
	/** Pattern for type name extraction from a type definition */
	private static final Pattern PTRN_TYPENAME = Pattern.compile("type (\\w+)\\s*=.*");
	private static Matcher mtchTypename = PTRN_TYPENAME.matcher("");
	// END KGU#388 2017-10-02
	// START KGU#799 2020-02-13: Bugfix #812
	private static final Matcher MTCH_IDENTIFIER = Pattern.compile("([A-Za-z_]\\w*).*").matcher("");
	// END KGU#799 2020-02-13

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
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#1061 2022-08-23: Issue #1068
		transformIndexLists(tokens);
		// END KGU#1061 2022-08-23
		// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
		tokens.replaceAll("Infinity", "float(\"inf\")");
		// END KGU#920 2021-02-03
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			if (Function.testIdentifier(token, false, null)) {
				// START KGU#795 2020-02-12: Issue #807 - we now use directories instead of recordtype lib
				// Check for a preceding dot
				int k = i;
				while (k > 0 && tokens.get(--k).trim().isEmpty());
				boolean isComponent = k >= 0 && tokens.get(k).equals(".");
				// END KGU#795 2020-02-12
				int j = i;
				// Skip all whitespace
				while (j+2 < tokens.count() && tokens.get(++j).trim().isEmpty());
				// Handle DiagramController (more specifically: Turtleizer) routine calls
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
				// START KGU#795 2020-02-12: Issue #807 - we now use directories instead of recordtype lib
				else if (isComponent) {
					tokens.set(k++, "[");
					tokens.set(i, "]");
					tokens.insert("'" + token + "'", i);
					tokens.remove(k, i);
					i += (k - i) + 1;	// This corrects the current index w.r.t. insertions and deletions 
				}
				// END KGU#795 2020-02-12
				// START KGU#542 2019-12-01: Enh. #739 support for enumerators
				else if (this.varNames.contains(token) && this.root != null && this.root.constants.get(token) != null) {
					String constVal = this.root.constants.get(token);
					if (constVal.startsWith(":") && constVal.contains("€")) {
						// Enumerator entry
						tokens.set(i, constVal.substring(1, constVal.indexOf("€"))+ "." + token);
					}
				}
				// END KGU#542 2019-12-01
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
		// START KGU#708 2019-05-28: Issue #725 - special operator symbol for int division
		tokens.replaceAll("div", "//");
		// END KGU#708 2019-05-28
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
	 * Recursively looks for record initializers in the tokens and, if there are some, replaces
	 * the respective type name token by the entire transformed record initializer as single
	 * string element (which is hence immune against further token manipulations).<br/>
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
			if (pos >= 0 && Function.testIdentifier(prevToken, false, null)
					&& (typeEntry = this.typeMap.get(":" + prevToken)) != null
					// Should be a record type but we better make sure.
					&& typeEntry.isRecord()) {
				// We will now reorder the elements and drop the names
				// START KGU#559 2018-07-20: Enh. #563 - smarter record initialization
				//HashMap<String, String> comps = Instruction.splitRecordInitializer(tokens.concatenate("", posLBrace));
				// START KGU#1021 2021-12-05: Bugfix #1024 Instruction might be defective
				//HashMap<String, String> comps = Instruction.splitRecordInitializer(tokens.concatenate("", posLBrace), typeEntry, false);
				String tail = tokens.concatenate("", posLBrace);
				HashMap<String, String> comps = Instruction.splitRecordInitializer(tail, typeEntry, false);
				// END KGU#1021 2021-12-05
				// END KGU#559 2018-07-20
				LinkedHashMap<String, TypeMapEntry> compDefs = typeEntry.getComponentInfo(true);
				// START KGU#1021 2021-12-05: Bugfix #1024 Instruction might be defective
				//String tail = comps.get("§TAIL§");	// String part beyond the initializer
				if (comps != null) {
					tail = comps.get("§TAIL§");	// String part beyond the initializer
				}
				// END KGU#1021 2021-12-05
				// START KGU#795 2020-02-12: Issue #807 - we now use directories instead of recordtype lib
				//String sepa = "(";	// initial "separator" is the opening parenthesis, then it will be comma
				String sepa = "{";	// initial "separator" is the opening brace, then it will be comma
				prevToken = "";
				// END KGU#795 2020-02-12
				for (String compName: compDefs.keySet()) {
					// START KGU#1021 2021-12-05: Bugfix #1024 Instruction might be defective
					//if (comps.containsKey(compName)) {
					if (comps != null && comps.containsKey(compName)) {
					// END KGU#1021 2021-12-05
						// START KGU#795 2020-02-12: Issue #807 - we now use directories instead of recordtype lib
						//prevToken += sepa + transformTokens(Element.splitLexically(comps.get(compName), true));
						prevToken += sepa + "'" + compName + "': " + transformTokens(Element.splitLexically(comps.get(compName), true));
						// END KGU#795 2020-02-12
					}
					// START KGU#795 2020-02-12: Issue #807 - we now use directories instead of recordtype lib
					//else {
					//	prevToken += sepa + "None";
					//}
					// END KGU#795 2020-02-12
					sepa = ", ";
				}
				// START KGU#795 2020-02-12: Issue #807 - we now use directories instead of recordtype lib
				//prevToken += ")";
				prevToken += "}";
				// END KGU#795 2020-02-12
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
			// START KGU#689 2019-03-21: Bugfix #706
			//_input = Element.unifyOperators(_input);
			//int asgnPos = _input.indexOf("<-");	// This might mutilate string literals!
			//if (asgnPos > 0)
			//{
			//	String lval = _input.substring(0, asgnPos).trim();
			//	String expr = _input.substring(asgnPos + "<-".length()).trim();
			if (Instruction.isAssignment(_input))
			{
				StringList tokens = Element.splitLexically(_input, true);
				// START KGU#698 2019-03-26: Bugfix #716
				Element.unifyOperators(tokens, false);
				// END KGU#698 2019-03-26
				tokens = Element.coagulateSubexpressions(tokens);
				int asgnPos = tokens.indexOf("<-");
				String lval = tokens.concatenate("", 0, asgnPos).trim();
				String expr = tokens.concatenate("", asgnPos + 1).trim();
			// END KGU#689 2019-03-21
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

		String s = super.transform(_input);

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
		if(!appendAsComment(_inst, _indent)) {
			boolean isDisabled = _inst.isDisabled(false);
			// START KGU 2014-11-16
			appendComment(_inst, _indent);
			// END KGU 2014-11-16
			StringList lines = _inst.getUnbrokenText();
			String tmpCol = null;
			Root root = Element.getRoot(_inst);
			for(int i = 0; i < lines.count(); i++)
			{
				// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
				//code.add(_indent + transform(_inst.getText().get(i)));
				String line = lines.get(i);
				String codeLine = transform(line);
				boolean done = false;

				// START KGU#1053 2022-08-14: Bugfix #1061 - hands off in "no conversion" mode!
				if (!this.suppressTransformation)
				{
				// END KGU#1053 2022-08-14
					// START KGU#653 2019-02-14: Enh. #680 - face input instructions with multiple variables
					StringList inputItems = Instruction.getInputItems(line);
					// START KGU#799 2020-02-13: Bugfix #812
					if (inputItems != null && root.isInclude()) {
						for (int j = 1; j < inputItems.count(); j++) {
							String var = inputItems.get(j);
							if (!Function.testIdentifier(var, false, null) && MTCH_IDENTIFIER.reset(var).matches()) {
								var = MTCH_IDENTIFIER.group(1);
							}
							if (var != null) {
								this.wasDefHandled(root, var, true, true);	// mark var as defined if it isn't
							}
						}
					}
					// END KGU#799 2020-02-13
					if (inputItems != null && inputItems.count() > 2) {
						String inputKey = CodeParser.getKeyword("input") + " ";
						String prompt = inputItems.get(0);
						if (!prompt.isEmpty()) {
							addCode(transform(CodeParser.getKeyword("output") + " " + prompt), _indent, isDisabled);
						}
						for (int j = 1; j < inputItems.count(); j++) {
							String item = inputItems.get(j);
							addCode(transform(inputKey + "\"" + item + "\" " + item), _indent, isDisabled);
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
						done = this.generateTypeDef(root, typeName, null, _indent, isDisabled);
					}
					// END KGU#388 2017-10-02
					// START KGU#767 2019-11-24: Bugfix #782 We must handle variable declarations as unspecified initialisations
					else if (Instruction.isMereDeclaration(line)) {
						done = generateDeclaration(line, root, _indent, isDisabled);
					}
					// END KGU#767 2019-11-24
					// START KGU#799 2020-02-13: Bugfix #812
					else if (Instruction.isAssignment(line) && root.isInclude()) {
						String var = this.getAssignedVarname(line, true);
						if (var != null) {
							this.wasDefHandled(root, var, true, true);	// mark var as defined if it isn't
						}
					}
					// END KGUU#799 2020-02-13
				// START KGU#1053 2022-08-14: Bugfix #1061 - hands off in "no conversion" mode!
				}
				// END KGU#1053 2022-08-14
				// START KGU#1089 2023-10-17: Issue #980
				if (!done && (line.contains("<-") || line.contains(":="))
						&& this.getAssignedVarname(line, false) == null) {
					this.appendComment("*** ILLEGAL LINE SKIPPED: " + line, _indent);
					done = true;
				}
				// END KGU#1089 2023-10-17
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
		boolean isDisabled = _alt.isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_alt, _indent);
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
		boolean isDisabled = _case.isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_case, _indent);
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
			// START KGU#755 2019-11-08: Bugfix #769 - more precise splitting necessary
			//StringList constants = StringList.explode(lines.get(i+1), ",");
			StringList constants = Element.splitExpressionList(lines.get(i + 1), ",");
			// END KGU#755 2019-11-08
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
		boolean isDisabled = _for .isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_for, _indent);
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
		// START KGU#934 2021-02-13: Bugfix #935 NullPointerException...
		//else
		else if (_for.style == For.ForLoopStyle.COUNTER)
		// END KGU#934 2021-02-13
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
		// START KGU#934 2021-02-13: Bugfix #935 NullPointerException...
		//addCode("for "+counterStr+" in " + valueList + ":", _indent, isDisabled);
		if (_for.style == For.ForLoopStyle.FREETEXT) {
			this.appendComment("TODO: No automatic loop translation found!", _indent);
			addCode(_for.getUnbrokenText().getLongString(), _indent, isDisabled);
		}
		else {
			addCode("for "+counterStr+" in " + valueList + ":", _indent, isDisabled);
		}
		// END KGU#934 2021-02-13
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
		boolean isDisabled = _while.isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_while, _indent);
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
		boolean isDisabled = _repeat.isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_repeat, _indent);
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
		// START KGU#54 2015-10-19: Add an empty line, but avoid accumulation of empty lines!
		if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
		{
			addCode("", "", isDisabled);
		}
		// END KGU#54 2015-10-19
	}

	protected void generateCode(Forever _forever, String _indent)
	{
		boolean isDisabled = _forever.isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_forever, _indent);
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
		if(!appendAsComment(_call, _indent))
		{
			boolean isDisabled = _call.isDisabled(false);
			// START KGU 2014-11-16
			appendComment(_call, _indent);
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
		if(!appendAsComment(_jump, _indent))
		{
			boolean isDisabled = _jump.isDisabled(false);
			// START KGU 2014-11-16
			appendComment(_jump, _indent);
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
			String preThrow  = CodeParser.getKeywordOrDefault("preThrow", "throw");
			for (int i = 0; isEmpty && i < lines.count(); i++) {
				String line = transform(lines.get(i)).trim();
				if (!line.isEmpty())
				{
					isEmpty = false;
				}
				if (Jump.isReturn(line))
				{
					addCode("return " + line.substring(preReturn.length()).trim(),
							_indent, isDisabled);
				}
				else if (Jump.isLeave(line))
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
				// START KGU#686 2019-03-21: Enh. #56
				else if (Jump.isThrow(line)) {
					this.addCode("raise Exception(" + line.substring(preThrow.length()) + ")", _indent, isDisabled);
				}
				// END KGU#686 2019-03-21
				else if (!isEmpty)
				{
					appendComment("FIXME: unsupported jump/exit instruction!", _indent);
					appendComment(line, _indent);
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
		boolean isDisabled = _para.isDisabled(false);
		Root root = Element.getRoot(_para);
		String suffix = Integer.toHexString(_para.hashCode());

		//String indentPlusOne = _indent + this.getIndent();
		//String indentPlusTwo = indentPlusOne + this.getIndent();
		appendComment(_para, _indent);

		addCode("", _indent, isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
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
			if (sq.getSize() == 1) {
				Element el = sq.getElement(0);
				if (el instanceof Call && ((Call)el).isProcedureCall(false)) {
					threadFunc = ((Call)el).getCalledRoutine().getName();
					// START KGU#819 2020-03-08: Bugfix #831 - In case of a call we can (and must) simply copy the arg list.
					String line = ((Call)el).getUnbrokenText().get(0);
					used = Element.splitExpressionList(line.substring(line.indexOf("(")+1), ",");
					for (int j = 0; j < used.count(); j++) {
						used.set(j, transform(used.get(j)));
					}
					// END KGU#819 2020-03-08
				}
			}
			String args = used.concatenate(",");
			if (used.count() == 1) {
				args += ",";
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
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
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
			boolean isDisabled = par.isDisabled(false);
			String functNameBase = "thread" + Integer.toHexString(par.hashCode()) + "_";
			int i = 0;
			// We still don't care for synchronisation, mutual exclusion etc.
			for (Subqueue sq: par.qs) {
				Element el = null;
				if (sq.getSize() == 1 && (el = sq.getElement(0)) instanceof Call && ((Call)el).isProcedureCall(false)) {
					// Don't generate a thread function for single procedure calls
					continue;
				}
				// Variables assigned here will be made global
				StringList setVars = _root.getVarNames(sq, false).copy();
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
				addSepaLine(_indent);
				i++;
			}
		}
		if (globals.count() > 0) {
			insertCode(_indent + "", lineBefore);
			for (int v = 0; v < globals.count(); v++) {
				String varName = globals.get(v);
				String end = varName.startsWith(this.commentSymbolLeft()) ? this.commentSymbolRight() : "";
				insertCode(_indent + varName + " = 0" + end, lineBefore);
			}
			insertCode(_indent + this.commentSymbolLeft() + " TODO: Initialize these variables globally referred to by prallel threads in a sensible way!", lineBefore);
		}
	}
	// END KGU#47/KGU#348 2017-02-19
	
	// START KGU#686 2019-03-21: Enh. #56
	protected void generateCode(Try _try, String _indent)
	{
		boolean isDisabled = _try.isDisabled(false);
		this.appendComment(_try, _indent);

		// START KGU#1034 2022-07-04: Issue #1041 superfluous nesting
		//// Both try-except and try-finally blocks exist, but not in combination... (was wrong!)
		//String indent0 = _indent;
		String indent1 = _indent + this.getIndent();
		// END KGU#1034 2022-07-04
		
		this.addCode("try:", _indent, isDisabled);

		// START KGU#1034 2022-07-04: Issue #1041 superfluous nesting
		//if (_try.qFinally.getSize() > 0) {
		//	indent0 += this.getIndent();
		//	// Inner try instruction
		//	this.addCode("try:", indent0, isDisabled);
		//}
		//String indent1 = indent0 + this.getIndent();
		// END KGU#1034 2022-07-04
		
		this.generateCode(_try.qTry, indent1);

		// START KGU#1034 2022-07-04: Issue #1041 superfluous nesting
		//if (_try.qFinally.getSize() > 0) {
		//	this.addCode("finally:", indent0, isDisabled);
		//	this.generateCode(_try.qFinally, indent1);
		//}
		//indent1 = _indent + this.getIndent();
		// END KGU#1034 2022-07-04

		String exName = _try.getExceptionVarName();
		if (exName != null && !exName.isEmpty()) {
			// START KGU#1034 2022-07-04: Bugfix #1042 wrong syntax
			//this.addCode("except Exception, " + exName + ":", _indent, isDisabled);
			this.addCode("except Exception as " + exName + ":", _indent, isDisabled);
			// END KGU#1034 2022-07-04
		}
		else {
			this.addCode("except Exception:", _indent, isDisabled);
		}
		generateCode(_try.qCatch, indent1);
		// START KGU#1034 2022-07-04: Issue #1041 finally can simply be appeded here
		if (_try.qFinally.getSize() > 0) {
			this.addCode("finally:", _indent, isDisabled);
			this.generateCode(_try.qFinally, indent1);
		}
		// END KGU#1034 2022-07-04
		addCode("", _indent, false);
	}
	// END KGU#686 2019-03-21

	// START KGU#388 2017-10-02: Enh. #423 Translate record types to mutable recordtypes
	/**
	 * Adds a typedef, struct, or enum definition for the type passed in by {@code _typeEnry}
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
		setDefHandled(_root.getSignatureString(false, false), typeKey);
		if (_type.isRecord()) {
			// START KGU#795 2020-02-12: Issue #807 Use dictionaries instead of external library recordtype
			//String typeDef = _type.typeName + " = recordtype(\"" + _type.typeName + "\" \"";
			//for (String compName: _type.getComponentInfo(false).keySet()) {
			//	typeDef += compName + " ";
			//}
			//typeDef = typeDef.trim() + "\")";
			//addCode(typeDef, _indent, _asComment);
			// END KGU#795 2020-02-12
			done = true;
		}
		// START KGU#542 2019-12-01: Enh. #739 - Support for enumeration types (since Python 3.4)
		else if (_type.isEnum()) {
			String indentPlus1 = _indent + this.getIndent();
			StringList enumItems = _type.getEnumerationInfo();
			addCode("class " + _typeName + "(Enum):", _indent, _asComment);
			int offset = 0;
			String lastVal = "";
			for (int i = 0; i < enumItems.count(); i++) {
				String[] itemSpec = enumItems.get(i).split("=", 2);
				if (itemSpec.length > 1) {
					lastVal = itemSpec[1].trim();
					offset = 0;
					try {
						int code = Integer.parseUnsignedInt(lastVal);
						lastVal = "";
						offset = code;
					}
					catch (NumberFormatException ex) {}
				}
				addCode(itemSpec[0] + " = " + transform(lastVal) + (lastVal.isEmpty() ? "" : "+") + offset, indentPlus1, _asComment);
				offset++;
			}
			addCode("", _indent, _asComment);
			done = true;
		}
		// END KGU#542 2019-12-01
		// START KGU#1081/KGU#1082 2023-09-29: Issue #1091, #1092 handle remaining cases
		else {
			// Just ignore type definition
			done = true;
		}
		// END KGU#1081/KGU#1082 2023-09-29
		return done;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#appendGlobalInitialisations(java.lang.String)
	 */
	protected void appendGlobalInitialisations(Root _root, String _indent) {
		if (topLevel) {
			// START KGU#852 2020-04-22: Don't leave a foreign typeMap here
			HashMap<String, TypeMapEntry> oldTypeMap = typeMap;
			// END KGU#852 2020-04-22
			for (Root incl: this.includedRoots.toArray(new Root[]{})) {
				// START KGU#815/KGU#824 2020-03-18: Enh. #828, bugfix #836
				// Don't add initialisation code for an imported module
				if (importedLibRoots != null && importedLibRoots.contains(incl)) {
					continue;
				}
				// END KGU#815/KGU#824 2020-03-18
				appendComment("BEGIN (global) code from included diagram \"" + incl.getMethodName() + "\"", _indent);
				// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
				//typeMap = incl.getTypeInfo();
				typeMap = incl.getTypeInfo(routinePool);	// This line is the difference to Generator!
				// END KGU#676 2019-03-30
				generateCode(incl.children, _indent);
				appendComment("END (global) code from included diagram \"" + incl.getMethodName() + "\"", _indent);
			}
			addSepaLine(_indent);
			// START KGU#852 2020-04-22: Don't leave a foreign typeMap here
			if (oldTypeMap != null) {
				typeMap = oldTypeMap;
			}
			// END KGU#852 2020-04-22
		}
	}

	/**
	 * Declares imported names as global
	 * @param _root - the Root being exported
	 * @param _indent - the current indentation level
	 * @see #appendGlobalInitialisations(Root, String)
	 * @see #generateDeclaration(String, Root, String, boolean)
	 */
	private void appendGlobalDeclarations(Root _root, String _indent) {
		// START KGU#767 2019-11-24: Bugfix #782 Fundamentally revised
//		if (_root.includeList != null) {
//			HashSet<String> declared = new HashSet<String>();
//			for (Root incl: this.includedRoots) {
//				if (_root.includeList.contains(incl.getMethodName())) {
//					// Start with the types
//					// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
//					//for (String name: incl.getTypeInfo().keySet()) {
//					for (String name: incl.getTypeInfo(routinePool).keySet()) {
//					// END KGU#676 2019-03-30
//						if (name.startsWith(":") && !declared.contains((name = name.substring(1)))) {
//							addCode("global " + name, _indent, false);
//							declared.add(name);								
//						}
//					}
//					// Now add the variables (including constants)
//					StringList names = incl.retrieveVarNames();
//					for (int i = 0; i < names.count(); i++)
//					{
//						String name = names.get(i);
//						if (!declared.contains(name)) {
//							addCode("global " + name, _indent, false);
//							declared.add(name);
//						}
//					}
//				}
//			}
//			if (!declared.isEmpty()) {
//				addCode("", _indent, false);
//			}
//		}
		// START KGU#799 2020-02-12: Bugfix #812 A main program does not need to use global declarations
		if (_root.isProgram()) {
			return;
		}
		// END KGU#799 2020-02-12
		for (String name: this.typeMap.keySet()) {
			if (this.wasDefHandled(_root, name, false, true) && !this.wasDefHandled(_root, name, true, false)) {
				if (name.startsWith(":")) {
					// START KGU#795 2020-02-12: Issue #807: There are no named record types anymore on export
					if (this.typeMap.get(name).isRecord()) {
						continue;
					}
					// END KGU#795 2020-02-12
					name = name.substring(1);
				}
				addCode("global " + name, _indent, false);
			}
		}
		// END KGU#767 2019-11-24
	}
	// END KGU#388 2017-10-02

	// START KGU#767 2019-11-24: Bugfix #782 - wrong handling of global / local declarations
	/**
	 * Generates a declaration from the given line and registers it with the given root.
	 * 
	 * @param _line - the original line of the declaration (should start with "var" or "dim")
	 * @param _root - the owning {@link Root} object
	 * @param _indent - current indentation level
	 * @param _isDisabled - whether this element is disabled (i.e. all content is going to
	 *     be a comment)
	 * @return true iff all code generation for the instruction line is done
	 */
	private boolean generateDeclaration(String _line, Root _root, String _indent, boolean _isDisabled) {
		// START KGU#1089 2023-10-12: Issu #980
		//StringList tokens = Element.splitLexically(_line + " <- 0", true);
		//tokens.removeAll(" ");
		//String varName = Instruction.getAssignedVarname(tokens, false);
		//return generateDeclaration(_root, varName, _indent, _isDisabled);
		StringList tokens = Element.splitLexically(_line, true);
		tokens.removeAll(" ");
		if (!tokens.isEmpty()) {
			// First tokens should be "var" or "dim" now.
			tokens.remove(0);
		}
		int posColon = tokens.indexOf(":");
		if (posColon < 0) {
			posColon = tokens.indexOf("as", false);
		}
		if (posColon < 0) {
			// Something's wrong here!
			posColon = tokens.count();
		}
		boolean done = true;
		StringList declItems = Element.splitExpressionList(tokens.subSequence(0, posColon), ",", false);
		for (int i = 0; i < declItems.count(); i++) {
			String declItem = declItems.get(i);
			int posBrack = declItem.indexOf("[");
			if (posBrack >= 0) {
				declItem = declItem.substring(0, posBrack);
			}
			if (Function.testIdentifier(declItem, false, null)) {
				done = generateDeclaration(_root, declItem, _indent, _isDisabled) && done;
			}
			else {
				// FIXME This may cause more trouble then to return always true
				done = false;
			}
		}
		return done;
		// END KGU#1089 2023-10-12
	}
	// END KGU#767 2019-11-24

	// START KGU#1098 2023-10-12: Issue #980 extracted from generateDeclaration(String, Root, String, boolean)
	/**
	 * Generates a declaration for the given variable and registers it with the given root
	 * if is hadn't already been declared.
	 * @param _root - the owning {@link Root} object
	 * @param _varName - the variable for which the declaration is to be generated (from
	 *     {@code root}'s type map)
	 * @param _indent - current indentation level
	 * @param _isDisabled - whether this element is disabled (i.e. all content is going to be a comment)
	 * @return true iff declaration for the given variable was or had been generated
	 */
	private boolean generateDeclaration(Root _root, String _varName, String _indent, boolean _isDisabled) {
		if (this.wasDefHandled(_root, _varName, false)) {
			return true;
		}
		String typeComment = "";
		TypeMapEntry type = this.typeMap.get(_varName);
		// START KGU#1089 2023-10-12: Issue #980 More intelligent declaration for fixed-size arrays
		StringList initializer = new StringList();
		int depth = 0;
		boolean toFill = true;
		// END KGU#1089 2023-10-12
		if (type != null) {
			StringList typeNames = this.getTransformedTypes(type, true);
			if (typeNames != null && !typeNames.isEmpty()) {
				typeComment = "\t" + this.commentSymbolLeft() +
						" meant to be of type " + typeNames.concatenate(" or ") + " " +
						this.commentSymbolRight();
			}
			// START KGU#1089 2023-10-12: Issue #980 More intelligent declaration for fixed-size arrays
			String canonStr = type.getCanonicalType(true, false);
			while (canonStr.substring(depth).startsWith("@")) {
				initializer.insert("]", depth);
				int maxIndex = type.getMaxIndex(depth);
				toFill = false;
				if (maxIndex >= 0) {
					initializer.insert(" for i" + depth + " in range(" + (maxIndex+1) +")", depth);
					toFill = true;
				}
				initializer.insert("[", 0);
				depth++;
			}
			// END KGU#1089 2023-10-12
		}
		// START KGU#1089 2023-10-12: Issue #980 More intelligent declaration for fixed-size arrays
		//addCode(_varName + " = None" + typeComment, _indent, _isDisabled);
		if (toFill) {
			initializer.insert("None", depth);
		}
		addCode(_varName + " = " + initializer.concatenate(null) + typeComment, _indent, _isDisabled);
		// END KGU#1089 2023-10-12
		this.setDefHandled(_root.getSignatureString(false, false), _varName);
		return true;
	}
	// END KGU#1098 2023-10-12

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		String indent = "";
		if (topLevel)
		{
			// START KGU#815 2020-04-07: Enh. #828 group export
			// START KGU#1040 2022-08-01: Bugfix #1047 was ugly in batch export to console
			//if (this.isLibraryModule()) {
			if (this.isLibraryModule() && !this.pureFilename.isEmpty()) {
			// END KGU#1040 2022-08-01
				this.appendScissorLine(true, this.pureFilename + "." + this.getFileExtensions()[0]);
			}
			// END KGU#815 2020-04-07
			// START KGU#819 2020-03-08: Bugfix #831
			//code.add(_indent + "#!/usr/bin/env python");
			code.add(_indent + "#!/usr/bin/python3");
			// END KGU#819 2020-03-08
			// START KGU#366 2017-03-10: Issue #378 the used character set is to be named 
			code.add(_indent + "# -*- coding: " + this.getExportCharset().toLowerCase() + " -*-");
			// END KGU#366 2017-03-10
			appendComment(_root.getText().get(0), _indent);
			appendComment("generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			// START KGU#795 2020-02-12: Issue #807 - no longer needed, now dictionaries are used instead
			//appendComment("You should have installed module recordtype: pip install recordtype", _indent);
			//appendComment(this.getIndent() + "See https://pypi.org/project/recordtype", _indent);
			//code.add(_indent + "from recordtype import recordtype");
			// END KGU#795 2020-02-12
			// START KGU#542 2019-12-01: Enh. #739
			code.add(_indent + "from enum import Enum");
			//this.generatorIncludes.add("enum");
			// END KGU#542 2019-12-01
			// START KGU#348 2017-02-19: Enh. #348 - Translation of parallel sections
			if (this.hasParallels) {
				addSepaLine(_indent);
				code.add(_indent + "from threading import Thread");
			}
			// END KGU#348 2017-02-19
			// START KGU#607 2018-10-30: Issue #346
			this.generatorIncludes.add("math");		// Will be inserted later
			// END KGU#607 2018-10-30
			// START KGU#351 2017-02-26: Enh. #346
			this.appendUserIncludes(indent);
			// END KGU#351 2017-02-26
			// START KGU#600 2018-10-17: It is too cumbersome to check if math is actually needed
			this.appendGeneratorIncludes(_indent, true);
			// END KGU#600 2018-10-17
			// START KGU#598 2018-10-17: Enh. #623
			this.includeInsertionLine = code.count();
			// END KGU#598 2018-10-17
			// START KGU#376 2017-10-02: Enh. #389 - insert the code of the includables first
			this.appendGlobalInitialisations(_root, _indent);
			// END KGU#376 2017-10-02
//			if (code.count() == this.includeInsertionLine) {
//				addSepaLine();
//			}
			subroutineInsertionLine = code.count();
			// START KGU#311 2016-12-27: Enh. #314: File API support
			if (this.usesFileAPI) {
				this.insertFileAPI("py");
			}
			// END KGU#311 2016-12-27
		}
		addSepaLine(_indent);;
		// FIXME: How to handle includables here?
		if (!_root.isSubroutine()) {
			appendComment(_root, _indent);
		}
		else {
			indent = _indent + this.getIndent();
			appendComment(_root, _indent);
			// START KGU#371 2019-03-08: Enh. #385 deal with optional parameters
			//code.add(_indent + "def " + _procName +"(" + _paramNames.getText().replace("\n", ", ") +") :");
			String header = _indent + "def " + _procName + "(";
			int minArgs = _root.getMinParameterCount();
			header += _paramNames.concatenate(", ", 0, minArgs);
			if (minArgs < _paramNames.count()) {
				StringList argDefaults = _root.getParameterDefaults();
				for (int p = minArgs; p < _paramNames.count(); p++) {
					header += (p > 0 ? ", " : "") + _paramNames.get(p) + " = " + transform(argDefaults.get(p));
				}
			}
			code.add(header += ") :");
			// END KGU#371 2019-03-08
		}
		// START KGU#388 2017-10-02: Enh. #423 type info will now be needed in deep contexts
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//this.typeMap = _root.getTypeInfo();
		this.typeMap = _root.getTypeInfo(routinePool);
		// END KGU#676 2019-03-30
		// END KGU#388 2017-10-02
		// START KGU#542 2019-12-01: Enh. #739 - For enumerator transformation, we will also need _root in in deeper contexts
		this.root = _root;
		// END KGU#542 2019-12-01
		return indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		// START KGU#376 2017-10-03: Enh. #389 - Variables and types of the included diagrams must be marked as global here
		// START KGU#767 2019-11-24: Bugfix #782: Disabled, will now be done via generateDeclaration() from generateCode(Instruction...)
		appendGlobalDeclarations(_root, _indent);
		// END KGU#767 2019-11-24
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
			// START KGU#1084 2023-10-04: Bugfix #1093 Don't invent an undue return statement here
			else {
				return _indent;
			}
			// END KGU#1084 2023-10-24
			addSepaLine(_indent);;
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
		
		// START KGU#815 2020-04-07: Enh. #828 - group export
		addSepaLine();
		this.libraryInsertionLine = code.count();
		// END KGU#815 2020-04-07

		// START KGU#598 2018-10-17: Enh. #623
		if (topLevel && this.usesTurtleizer) {
			insertCode("import turtle", this.includeInsertionLine);
			insertCode("turtle.colormode(255)", this.includeInsertionLine);
			insertCode("turtle.mode(\"logo\")", this.includeInsertionLine);
			addCode("turtle.bye()\t" + this.commentSymbolLeft() + " TODO: re-enable this if you want to close the turtle window.", _indent, true);
		}
		// END KGU#598 2018-10-17
	}
	// END KGU 2015-12-15
	
	// START KGU#799 2020-02-13: Auxiliary for bugfix #812
	private String getAssignedVarname(String line, boolean pureBasename)
	{
		StringList tokens = Element.splitLexically(line, true);
		tokens.removeAll(" ");
		Element.unifyOperators(tokens, true);
		String var = Instruction.getAssignedVarname(tokens, false);
		if (var != null && !Function.testIdentifier(var, false, "")) {
			if (MTCH_IDENTIFIER.reset(var).matches()) {
				var = MTCH_IDENTIFIER.group(0);
			}
			else {
				var = null;
			}
		}
		return var;
	}
	// END KGU#799 2020-02-13

}

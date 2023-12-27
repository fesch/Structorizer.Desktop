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
 *      Description:    This class generates Java code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Bob Fisch               2008-11-17      First Issue
 *      Gunter Schillebeeckx    2009-08-10      Java Generator starting from C Generator
 *      Bob Fisch               2009-08-10      Update I/O
 *      Bob Fisch               2009-08-17      Bugfixes (see comment)
 *      Kay Gürtzig             2010-09-10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011-11-07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014-10-22      Workarounds and Enhancements (see comment)
 *      Kay Gürtzig             2014-11-16      Several fixes and enhancements (see comment)
 *      Kay Gürtzig             2015-10-18      Comment generation and indentation revised
 *      Kay Gürtzig             2015-11-01      Preprocessing reorganised, FOR loop and CASE enhancements
 *      Kay Gürtzig             2015-11-30      Inheritance changed to CGenerator (KGU#16), specific
 *                                              jump and return handling added (issue #22 = KGU#74)
 *      Kay Gürtzig             2015-12-12      Enh. #54 (KGU#101): Support for output expression lists
 *      Kay Gürtzig             2015-12-15      Bugfix #51 (=KGU#108): Cope with empty input and output
 *      Kay Gürtzig             2015-12-21      Bugfix #41/#68/#69 (= KG#93)
 *      Kay Gürtzig             2016-03-23      Enh. #84: Support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig             2016-04-04      transforTokens() disabled due to missing difference to super 
 *      Kay Gürtzig             2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178) 
 *      Kay Gürtzig             2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2016-09-25      Enh. #253: CodeParser.keywordMap refactoring done 
 *      Kay Gürtzig             2016-10-14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016-10-15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig             2016-12-22      Enh. #314: Support for Structorizer File API
 *      Kay Gürtzig             2017-01-30      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig             2017-02-01      Enh. #113: Array parameter transformation
 *      Kay Gürtzig             2017-02-24      Enh. #348: Parallel sections translated with java.utils.concurrent.Callable
 *      Kay Gürtzig             2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017-04-12      Issue #335: transformType() revised and isInternalDeclarationAllowed() corrected
 *      Kay Gürtzig             2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2017-05-24      Bugfix: name suffix for Parallel elements now hexadecimal (could otherwise be negative)
 *      Kay Gürtzig             2017-09-22      Bugfix #428 Defective replacement pattern for "short" in transformType(String)
 *      Kay Gürtzig             2017-09-28      Enh. #389, #423: Update for record types and includable diagrams
 *      Kay Gürtzig             2017-10-27      Enh. #441: Direct support for now extractable Turtleizer package
 *      Kay Gürtzig             2018-01-21      Enh. #441/#490: Improved support for TurtleBox routine export. 
 *      Kay Gürtzig             2018-02-22      Bugfix #517: Declarations/initializations from includables weren't handled correctly 
 *      Kay Gürtzig             2018-07-20      Enh. #563: support for simplified record initializers
 *      Kay Gürtzig             2018-07-21/22   Bugfix #564: array initializer trouble mended
 *      Kay Gürtzig             2019-01-22      Bugfix #669: FOR-In loop was incorrect for traversing strings 
 *      Kay Gürtzig             2019-02-14      Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig             2019-03-20      Enh. #56: Export of Try elements and Jump element of throw flavour
 *      Kay Gürtzig             2019-03-30      Issue #696: Type retrieval had to consider an alternative pool
 *      Kay Gürtzig             2019-10-02      Bugfix #755: Defective conversion of For-In loops with explicit array initializer
 *      Kay Gürtzig             2019-10-03      Bugfix #755: Further provisional fixes for nested Array initializers
 *      Kay Gürtzig             2019-10-18      Enh. #739: Support for enum types (debugged on 2019-11-30)
 *      Kay Gürtzig             2020-03-17      Enh. #828: New configuration method prepareGeneratorIncludeItem()
 *      Kay Gürtzig             2020-04-01      Enh. #348: Parallel code generation refined (result mechanism)
 *      Kay Gürtzig             2021-02-03      Issue #920: Transformation for "Infinity" literal
 *      Kay Gürtzig             2021-10-03      Bugfix #993: Wrong handling of constant parameters
 *      Kay Gürtzig             2021-12-05      Bugfix #1024: Precautions against defective record initializers
 *      Kay Gürtzig             2023-09-28      Bugfix #1092: Type alias export flaws mended, at least as comment
 *      Kay Gürtzig             2023-10-04      Bugfix #1093: Undue final return 0 on function diagrams
 *      Kay Gürtzig             2023-10-15      Bugfix #1096: Initialisation for multidimensional arrays fixed
 *      Kay Gürtzig             2023-12-25      Issue #1121: Scanner method should be type-specific where possible
 *      Kay Gürtzig             2023-12-27      Issue #1123: Translation of built-in function random() added.
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
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.turtle.TurtleBox;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;

import lu.fisch.diagrcontrol.DiagramController;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;


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

	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		return OverloadingLevel.OL_DELEGATION;
	}
	// END KGU#371 2019-03-07

	// START KGU#686 2019-03-18: Enh. #56
	/**
	 * Subclassable method to specify the degree of availability of a try-catch-finally
	 * construction in the target language.
	 * @return a {@link TryCatchSupportLevel} value
	 * @see #appendCatchHeading(Try, String)
	 */
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_TRY_CATCH_FINALLY;
	}
	// END KGU#686 2019-03-18

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

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//    private static final String[] reservedWords = new String[]{
//		"abstract", "assert", "boolean", "break", "byte",
//		"case", "catch", "char", "class", "const", "continue",
//		"default", "do", "double",
//		"else", "enum", "extends",
//		"false", "final", "finally", "float", "for", "goto",
//		"if", "implements", "import", "instanceof", "int", "interface",
//		"long", "native", "new", "null",
//		"package", "private", "protected", "public",
//		"return", "short", "static", "super", "switch", "synchronised",
//		"this", "throw", "throws", "transient", "true", "try",
//		"void", "volatile", "while"};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	// END KGU 2016-08-12

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return "import %;";
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/
	
	// START KGU#1109 2023-12-25: Issue #1121 Better type-specific input support
	/** List of Java types with specific {@code next...()} method on class {@link Scanner}. */
	static private final StringList SCANNER_TYPES = StringList.explode("double,float,long,int,short,boolean,byte", ",");
	// END KGU#1109 2023-12-23
	
	// START KGU#542 2019-11-18: Enh. #739 - we need the current root for token transformation
	/** Currently exported {@link Root} object */
	protected Root root = null;
	// END KGU#542 2019-11-18
	
	// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatesClass()
	 */
	@Override
	protected boolean allowsMixedModule()
	{
		return true;
	}
 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#insertPrototype(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, int)
	 */
	@Override
	protected int insertPrototype(Root _root, String _indent, boolean _withComment, int _atLine)
	{
		// We don't need prototypes
		return 0;
	}
	// END KGU#815/KGU#824 2020-03-19
	

	// START KGU#560 2018-07-22 Bugfix #564
	@Override
	protected boolean wantsSizeInArrayType()
	{
		return false;
	}
	// END KGU#560 2018-07-22

	@Override
	protected boolean arrayBracketsAtTypeName()
	{
		return true;
	}

	// START KGU#1112 2023-12-27: Issue #1123: Care for random translation
	@Override
	protected boolean needsRandomClassInstance()
	{
		return true;
	}
	// END KGU#1112 2023-12-27
	
	// START KGU#480 2018-01-21: Enh. #490 Improved support for Turtleizer export
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
	// END KGU#480 2018-01-21

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getInputReplacer(boolean)
	 */
	@Override
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer()
	//{
	//	return "$1 = (new Scanner(System.in)).nextLine()";
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		// NOTE: If you modify these patterns then you must adapt transform() too!
		if (withPrompt) {
			return "System.out.print($1); $2 = (new Scanner(System.in)).nextLine()";
		}
		return "$1 = (new Scanner(System.in)).nextLine()";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		return "System.out.println($1)";
	}

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * Method pre-processes an include file name for the #include
	 * clause. This version does nothing (just passes the argument through).
	 * @param _includeFileName a string from the user include configuration
	 * @return the pre-processed string as to be actually inserted
	 */
	protected String prepareUserIncludeItem(String _includeFileName)
	{
		return _includeFileName;
	}
	// END KGU#351 2017-02-26
	// START KGU#815/KGU#826 2020-03-17: Enh. #828, bugfix #836
	/**
	 * Method converts some generic module name into a generator-specific include file name or
	 * module name for the import / use clause.<br/>
	 * To be used before adding a generic name to {@link #generatorIncludes}.
	 * This version does not do anything. 
	 * @see #getIncludePattern()
	 * @see #appendGeneratorIncludes(String)
	 * @see #prepareUserIncludeItem(String)
	 * @param _includeName a generic (language-independent) string for the generator include configuration
	 * @return the converted string as to be actually added to {@link #generatorIncludes}
	 */
	protected String prepareGeneratorIncludeItem(String _includeName)
	{
		return _includeName;
	}
	// END KGU#815/KGU#826 2020-03-17

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	@Override
	protected void appendExitInstr(String _exitCode, String _indent, boolean isDisabled)
	{
		addCode("System.exit(" + _exitCode + ")", _indent, isDisabled);
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
	// START KGU#446 2017-10-27: Enh. #441 special support for Turtleizer
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
		tokens.replaceAll("Infinity", "Double.POSITIVE_INFINITY");
		// END KGU#920 2021-02-03
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			if (Function.testIdentifier(token, false, null)) {
				// START KGU#542 2019-11-30: Enh. #739 - support for enum types
				String constVal = null;	// Will be needed on enum test
				// END KGU#542 2019-11-30
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
							tokens.set(i, entry.getValue() + "." +name);
							if (entry.getKey() instanceof TurtleBox) {
								this.usesTurtleizer = true;
							}
						}
					}
				}
				// END KGU#480 2018-01-21
				// START KGU#542 2019-11-18: Enh. #739 - support for enum types
				else if (this.root != null && (constVal = this.root.constants.get(token)) != null) {
					int posEu = constVal.indexOf('€');
					if (constVal.startsWith(":") && posEu > 1) {
						// In general, the enum constant names are to be qualified
						// (This is not true in case clause of switch instructions, however...
						tokens.set(i, constVal.substring(1, posEu) + "." + token);
					}
				}
				// END KGU#542 2019-11-18
			}
		}
		// START KGU#1112 2023-12-17: Issue #1123: Convert random(expr) calls
		int pos = -1;
		while ((pos = tokens.indexOf("random", pos+1)) >= 0 && pos+2 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			StringList exprs = Element.splitExpressionList(tokens.subSequence(pos+2, tokens.count()),
					",", true);
			if (exprs.count() == 2 && exprs.get(1).startsWith(")")) {
				tokens.remove(pos, tokens.count());
				tokens.add(Element.splitLexically("(randGen.nextInt() % (" + exprs.get(0) + ")" + exprs.get(1), true));
			}
		}
		// END KGU#1112 2023-12-17
		return super.transformTokens(tokens);
	}
	// END KGU#446 2017-10-27
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
		// START KGU#815 2020-04-03: Enh. #828 group export
		if (generatorIncludes.contains("lu.fisch.structorizer.generators." + FILE_API_CLASS_NAME)) {
			for (int i = 0; i < Executor.fileAPI_names.length; i++) {
				tokens.replaceAll(Executor.fileAPI_names[i], FILE_API_CLASS_NAME + "." + Executor.fileAPI_names[i]);
			}
		}
		// END KGU#815 2020-04-03
	}
	// END KGU#311 2017-01-05

	@Override
	protected String transform(String _input, boolean _doInputOutput)
	{
		// START KGU#101 2015-12-12: Enh. #54 - support lists of expressions
		if (_doInputOutput) {
			String outputKey = CodeParser.getKeyword("output").trim(); 
			if (_input.matches("^" + getKeywordPattern(outputKey) + "[ ](.*?)"))
			{
				StringList expressions = 
						Element.splitExpressionList(_input.substring(outputKey.length()), ",");
				// Some of the expressions might be sums, so better put parentheses around them
				if (expressions.count() > 1) {
					_input = outputKey + " (" + expressions.concatenate(") + (") + ")";
				}
			}
		}
		// END KGU#101 2015-12-12

		// START KGU#18/KGU#23 2015-11-01: This can now be inherited
		String s = super.transform(_input, _doInputOutput) /*.replace(" div "," / ")*/;
		// END KGU#18/KGU#23 2015-11-01

		if (_doInputOutput) {
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
			// START KGU#1109 2023-12-25: Issue #1121 Try a more type-specific input
			else {
				int posRepl = s.indexOf(inpRepl);
				if (posRepl > 0) {
					int posSemi = s.lastIndexOf(";", posRepl);
					String target = s.substring(posSemi + 1, posRepl).trim();
					if (this.varNames.contains(target) && this.typeMap.containsKey(target)) {
						String typename = this.transformType(typeMap.get(target).getCanonicalType(true, true), "???");
						if (SCANNER_TYPES.contains(typename)) {
							s = s.replace("nextLine()", 
									"next" + Character.toUpperCase(typename.charAt(0)) + typename.substring(1) + "()");
						}
					}
				}
			}
			// END KGU#1109 2023-12-25
		}

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
		// START KGU 2017-04-12: We must not generally flatten the case (consider user types!)
//		_type = _type.toLowerCase();
//		_type = _type.replace("short int", "int");
//		_type = _type.replace("short", "int");
//		_type = _type.replace("unsigned int", "int");
//		_type = _type.replace("unsigned long", "long");
//		_type = _type.replace("unsigned char", "char");
//		_type = _type.replace("unsigned", "int");
//		_type = _type.replace("longreal", "double");
//		_type = _type.replace("real", "double");
//		//_type = _type.replace("boole", "boolean");
//		//_type = _type.replace("bool", "boolean");
//		if (_type.matches("(^|.*\\W)bool(\\W.*|$)")) {
//			_type = _type.replaceAll("(^|.*\\W)bool(\\W.*|$)", "$1boolean$2");
//		}
//		_type = _type.replace("character", "Character");
//		_type = _type.replace("integer", "Integer");
//		_type = _type.replace("string", "String");
//		_type = _type.replace("array[ ]?([0-9]*)[ ]of char", "String");	// FIXME (KGU 2016-01-14) doesn't make much sense
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("short int", true) + ")($|\\W.*)", "$1short$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("long int", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("long long", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(S" + BString.breakup("hort", true) + ")($|\\W.*)", "$1short$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned int", true) + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned long", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned char", true) + ")($|\\W.*)", "$1byte$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("signed char", true) + ")($|\\W.*)", "$1byte$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("unsigned", true) + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(I" + BString.breakup("nt", true) + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("integer", true) + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(L" + BString.breakup("ong", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("longint", true) + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(D" + BString.breakup("ouble", true) + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("longreal", true) + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("real", true) + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(F" + BString.breakup("loat", true) + ")($|\\W.*)", "$1float$3");
		_type = _type.replaceAll("(^|.*\\W)(C" + BString.breakup("har", true) + ")($|\\W.*)", "$1char$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("character", true) + ")($|\\W.*)", "$1Character$3");
		_type = _type.replaceAll("(^|.*\\W)(B" + BString.breakup("oolean", true) + ")($|\\W.*)", "$1boolean$3");
		if (_type.matches("(^|.*\\W)(" + BString.breakup("bool", true) + "[eE]?)(\\W.*|$)")) {
			_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("bool", true) + "[eE]?)(\\W.*|$)", "$1boolean$3");
		}
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("string", true) + ")($|\\W.*)", "$1String$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("array", true) + "\\s*?\\[[0-9]*\\]\\s*?" + BString.breakup("of", true) + "\\s+char)(\\W.*)", "$1String$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("const", true) + ")($|\\W.*)", "$1final$3");
		// END KGU 2017-04-12
		return _type;
	}
	// END KGU#16 2015-11-29

	// START KGU#388 2017-09-28: Enh. #423 struct type support
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformRecordInit(java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry)
	 */
	@Override
	protected String transformRecordInit(String constValue, TypeMapEntry typeInfo) {
		// This is practically identical to C#
		// START KGU#559 2018-07-20: Enh. #563 - smarter record initialization
		//HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue);
		HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue, typeInfo, false);
		// END KGU#559 2018-07-20
		LinkedHashMap<String, TypeMapEntry> compInfo = typeInfo.getComponentInfo(true);
		String recordInit = "new " + typeInfo.typeName + "(";
		boolean isFirst = true;
		for (Entry<String, TypeMapEntry> compEntry: compInfo.entrySet()) {
			String compName = compEntry.getKey();
			TypeMapEntry compType = compEntry.getValue();
			// START KGU#1021 2021-12-05: Bugfix #1024 Instruction might be defective
			//String compVal = comps.get(compName);
			String compVal = null;
			if (comps != null) {
				compVal = comps.get(compName);
			}
			// END KGU#1021 2021-12-05
			if (isFirst) {
				isFirst = false;
			}
			else {
				recordInit += ", ";
			}
			if (!compName.startsWith("§")) {
				if (compVal == null) {
					recordInit += "null";
				}
				else if (compType != null && compType.isRecord()) {
					recordInit += transformRecordInit(compVal, compType);
				}
				// START KGU#561 2018-07-21: Bugfix #564
				else if (compType != null && compType.isArray() && compVal.startsWith("{") && compVal.endsWith("}")) {
					String elemType = compType.getCanonicalType(true, false).substring(1);
					recordInit += "new " + this.transformType(elemType, "object") + "[]" + compVal;
				}
				// END KGU#561 2018-07-21
				else {
					recordInit += transform(compVal);
				}
			}
		}
		recordInit += ")";
		return recordInit;
	}

	/**
	 * Generates code that either allows direct assignment or decomposes the record
	 * initializer into separate component assignments
	 * @param _lValue - the left side of the assignment (without modifiers!)
	 * @param _recordValue - the record initializer according to Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is to be commented out
	 * @param _typeEntry - an existing {@link TyeMapEntry} for the assumed record type (or null)
	 */
	protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled, TypeMapEntry _typeEntry)
	{
		// START KGU#559/KGU#560 2018-07-21: Enh. #563, bugfix #564 - radically revised
		// This is practically identical to C#
		if (_typeEntry == null || !_typeEntry.isRecord()) {
			// Just decompose it (requires that the target variable has been initialized before).
			super.generateRecordInit(_lValue, _recordValue, _indent, _isDisabled, _typeEntry);
		}
		else {
			// This way has the particular advantage not to fail with an uninitialized variable (important for Java!). 
			addCode(_lValue + " = " + this.transformRecordInit(_recordValue, _typeEntry) + ";", _indent, _isDisabled);
		}
		// END KGU#559/KGU#560 2018-07-21
	}
	// END KGU#388 2017-09-28

	// START KGU#560 2018-07-21: Bugfux #564 Array initializers have to be decomposed if not occurring in a declaration
	/**
	 * Generates code that decomposes an array initializer into a series of element assignments if there
	 * is no compact translation.
	 * @param _lValue - the left side of the assignment (without modifiers!), i.e. the array name
	 * @param _arrayItems - the {@link StringList} of element expressions to be assigned (in index order)
	 * @param _indent - the current indentation level
	 * @param _isDisabled - whether the code is commented out
	 * @param _elemType - the {@link TypeMapEntry} of the element type if available (null otherwise)
	 * @param _isDecl - if this is part of a declaration (i.e. a true initialization)
	 */
	protected String transformOrGenerateArrayInit(String _lValue, StringList _arrayItems, String _indent, boolean _isDisabled, String _elemType, boolean _isDecl)
	{
		// START KGU#732 2019-10-03: Bugfix #755 - The new operator is always to be used.
		//if (_isDecl) {
		//	return this.transform("{" + _arrayItems.concatenate(", ") + "}");
		//}
		//else if (_elemType != null) {
		//	return "new " + this.transformType(_elemType, "Object") + "[]{" + _arrayItems.concatenate(", ") + "}";
		//}
		//else {
		//	super.generateArrayInit(_lValue, _arrayItems, _indent, _isDisabled, null, false);
		//}
		//return null;
		String initializerC = super.transformOrGenerateArrayInit(_lValue, _arrayItems, _indent, _isDisabled, _elemType, true);
		if (initializerC == null) {
			if (_lValue != null) return _lValue;	// Assignment sequence already generated (???)
		}
		// START KGU#1090 2023-10-15: Bugfix #1096
		int endAt = _elemType.lastIndexOf('@');
		if (endAt >= 0) {
			_elemType = _elemType.substring(endAt+1) + "[]".repeat(endAt+1);
		}
		// END KGU#1090 2023-10-15
		return "new " + this.transformType(_elemType, "Object") + "[]" + initializerC;
		// END KGU#732 2019-10-03
	}
	// END KGU#560 2018-07-21

	// START KGU#332 2017-04-13: Enh. #335
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#isInternalDeclarationAllowed()
	 */
	@Override
	protected boolean isInternalDeclarationAllowed()
	{
		// START KGU#501 2018-02-22: Bugfix #517
		//return true;
		return !isInitializingIncludes();
		// END KGU#501 2018-02-22
	}
	// END KGU#332 2017-04-13

	// START KGU#388 2017-09-28: Enh. #423
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#generateTypeDef(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.structorizer.elements.TypeMapEntry, java.lang.String, boolean)
	 */
	@Override
	protected void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
		String typeKey = ":" + _typeName;
		if (this.wasDefHandled(_root, typeKey, true)) {
			return;
		}
		appendDeclComment(_root, _indent, typeKey);
		if (_type.isRecord()) {
			String indentPlus1 = _indent + this.getIndent();
			String indentPlus2 = indentPlus1 + this.getIndent();
			addCode((_root.isInclude() ? "private " : "") + "class " + _typeName + "{", _indent, _asComment);
			boolean isFirst = true;
			StringBuffer constructor = new StringBuffer();
			StringList constrBody = new StringList();
			constructor.append("public " + _typeName + "(");
			for (Entry<String, TypeMapEntry> compEntry: _type.getComponentInfo(false).entrySet()) {
				String compName = compEntry.getKey();
				String typeStr = transformTypeFromEntry(compEntry.getValue(), null, true);
				addCode("public " + typeStr + "\t" + compName + ";",
						indentPlus1, _asComment);
				if (!isFirst) constructor.append(", ");
				constructor.append(typeStr + " p_" + compName);
				constrBody.add(compName + " = p_" + compName + ";");
				isFirst = false;
			}
			constructor.append(")");
			addCode(constructor.toString(), indentPlus1, _asComment);
			addCode("{", indentPlus1, _asComment);
			for (int i = 0; i < constrBody.count(); i++) {
				addCode(constrBody.get(i), indentPlus2, _asComment);
			}
			addCode("}", indentPlus1, _asComment);
			addCode("};", _indent, _asComment);
		}
		// START KGU#542 2019-11-17: Enh. #739
		else if (_type.isEnum()) {
			String indentPlus1 = subroutineIndent;
			String indentPlus2 = indentPlus1 + this.getIndent();
			StringList items = _type.getEnumerationInfo();
			String itemList = items.concatenate(", ");
			if (itemList.length() > 70) {
				this.insertCode(indentPlus1 + "private enum " + _type.typeName + " {", subroutineInsertionLine);
				for (int i = 0; i < items.count(); i++) {
					// FIXME: We might have to transform the value...
					insertCode(indentPlus2 + items.get(i) + (i < items.count() -1 ? "," : ""), subroutineInsertionLine);
				}
				insertCode(indentPlus1 + "};", subroutineInsertionLine);
			}
			else {
				insertCode(indentPlus1 + "private enum " + _type.typeName + "{" + itemList + "};", subroutineInsertionLine);
			}
			insertSepaLine("", subroutineInsertionLine);
		}
		// END KGU#542 2019-11-17
		else {
			// FIXME: What do we here in Java? Replace this type name all over the code?
			// START KGU#1082 2023-09-28: Bugfix #1092 Sensible handling of alias types
			//addCode("typedef " + this.transformTypeFromEntry(_type, null) + " " + _typeName + ";",
			addCode("typedef " + this.transformTypeFromEntry(_type, null, !_typeName.equals(_type.typeName)) + " " + _typeName + ";",
			// END KGU#1082 2023-09-28
					_indent, true);
		}
	}
	// END KGU#388 2017-09-28

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#enhanceWithColor(java.lang.String, lu.fisch.structorizer.elements.Instruction)
	 */
	@Override
	protected String enhanceWithColor(String _codeLine, Instruction _inst) {
		int posParen2 = _codeLine.lastIndexOf(")");
		if (posParen2 > 0) {
			_codeLine = _codeLine.substring(0, posParen2) + 
					", java.awt.Color.decode(\"0x" + _inst.getHexColor() + "\")" + _codeLine.substring(posParen2) + ";";
		}
		return _codeLine;
	}

	// START KGU#653 2019-02-14: Enh. #680
	/**
	 * Subclassable method possibly to obtain a suited transformed argument list string for the given series of
	 * input items (i.e. expressions designating an input target variable each) to be inserted in the input replacer
	 * returned by {@link #getInputReplacer(boolean)}, this allowing to generate a single input instruction only.<br/>
	 * This instance just returns null (forcing the generate method to produce consecutive lines).
	 * @param _inputVarItems - {@link StringList} of variable descriptions for input
	 * @return either a syntactically converted combined string with suited operator or separator symbols, or null.
	 */
	@Override
	protected String composeInputItems(StringList _inputVarItems)
	{
		return null;
	}
	// END KGU#653 2019-02-14

	// START KGU#815 2020-03-26: Enh. #828 support for library references
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#makeLibCallName(java.lang.String)
	 */
	@Override
	protected String makeLibCallName(String name) {
		return this.libModuleName + "." + name;
	}
	// END KGU#815 2020-03-26

	// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
	/**
	 * We try our very best to create a working loop from a FOR-IN construct.
	 * @param _for - the element to be exported
	 * @param _indent - the current indentation level
	 * @return true iff the method created some loop code (sensible or not)
	 */
	@Override
	protected boolean generateForInCode(For _for, String _indent)
	{
		// We simply use the range-based loop of Java (as far as possible)
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		StringList items = this.extractForInListItems(_for);
		String indent = _indent;
		String itemType = null;
		if (items != null)
		{
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogeneous? We will just try four ways: int,
			// double, String, and derived type name. If none of them match we use
			// Object and add a TODO comment.
			int nItems = items.count();
			boolean allInt = true;
			boolean allDouble = true;
			boolean allString = true;
			// START KGU#388 2017-09-28: Enh. #423
			boolean allCommon = true;
			String commonType = null;
			// END KGU#388 2017-09-28
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
				// START KGU#388 2017-09-28: Enh. #423
				if (allCommon)
				{
					String itType = Element.identifyExprType(this.typeMap, item, true);
					if (i == 0) {
						commonType = itType;
					}
					if (!commonType.equals(itType)) {
						allCommon = false;
					}
				}
				// END KGU#388 2017-09-28
				// START KGU#732 2019-10-02: Bugfix #755 - transformation of the items is necessary
				items.set(i, transform(item));
				// END KGU#732 2019-10-02
			}
			valueList = "{" + items.concatenate(", ") + "}";
			// START KGU#388 2017-09-28: Enh. #423
			//if (allInt) itemType = "int";
			if (allCommon) itemType = commonType;
			else if (allInt) itemType = "int";
			// END KGU#388 2017-09-28
			else if (allDouble) itemType = "double";
			else if (allString) itemType = "char*";
			// START KGU#732 2019-10-02: Bugfix #755 part 1 - there is no need to define a variable
			//String arrayName = "array20160322";
			//
			//addCode("{", _indent, isDisabled);
			//indent += this.getIndent();
			// END KGU#732 2019-10-02
			
			if (itemType == null)
			{
				itemType = "Object";
				this.appendComment("TODO: Select a more sensible item type than Object", indent);
				this.appendComment("      and/or prepare the elements of the array.", indent);
			}
			// START KGU#732 2019-10-02: Bugfix #755 part 2
			//addCode(itemType + "[] " + arrayName + " = new " + itemType + "[]" + transform(valueList, false) + ";",
			//		indent, isDisabled);
			//
			//valueList = arrayName;
			valueList = "new " + itemType + "[]" + valueList;
			// END KGU#732 2019-10-02
		}
		else
		{
			// START KGU#388 2017-09-28 #423
			//itemType = "Object";
			//this.insertComment("TODO: Select a more sensible item type than Object and/or prepare the elements of the array", indent);
			TypeMapEntry listType = this.typeMap.get(valueList);
			if (listType != null && listType.isArray() && (itemType = listType.getCanonicalType(true, false)) != null
					&& itemType.startsWith("@"))
			{
				itemType = this.transformType(itemType.substring(1), "Object");	
			}
			// START KGU#640 2019-01-22: Bugfix #669 - we need more specific handling of strings as value list
			else if (listType != null && listType.getCanonicalType(true, true).equalsIgnoreCase("String")) {
				itemType = "char";
				valueList += ".toCharArray()";
			}
			// END KGU#640 2019-01-22
			else {
				itemType = "Object";
				this.appendComment("TODO: Select a more sensible item type than Object and/or prepare the elements of the array", indent);
			}
			// END KGU#388 2017-09-28
			valueList = transform(valueList, false);
		}

		// Creation of the loop header
		appendBlockHeading(_for, "for (" + itemType + " " + var + " : " +	valueList + ")", indent);

		// Add the loop body as is
		generateCode(_for.q, indent + this.getIndent());

		// Accomplish the loop
		appendBlockTail(_for, null, indent);

		// START KGU#732 2019-10-02: Bugfix #755 part 3 - obsolete code disabled
		//if (items != null)
		//{
		//	addCode("}", _indent, isDisabled);
		//}
		// END KGU#732 2019-10-02
		
		return true;
	}
	// END KGU#61 2016-03-22
	
	// START KGU#348 2017-02-25: Enh. #348 - Offer a C++11 solution with class std::thread
	@Override
	protected void generateCode(Parallel _para, String _indent)
	{

		boolean isDisabled = _para.isDisabled(false);
		Root root = Element.getRoot(_para);
		String indentPlusOne = _indent + this.getIndent();
		int nThreads = _para.qs.size();
		StringList[] asgnd = new StringList[nThreads];	// assigned variables per thread
		String suffix = Integer.toHexString(_para.hashCode());

		appendComment(_para, _indent);

		addCode("", "", isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
		addCode("try {", _indent, isDisabled);
		addCode("ExecutorService pool = Executors.newFixedThreadPool(" + nThreads + ");", indentPlusOne, isDisabled);

		boolean expectResults = false;
		for (int i = 0; i < nThreads; i++) {
			addCode("", _indent, isDisabled);
			appendComment("----------------- START THREAD " + i + " -----------------", indentPlusOne);
			Subqueue sq = _para.qs.get(i);
			String future = "future" + suffix + "_" + i;
			String worker = "Worker" + suffix + "_" + i;
			StringList used = root.getUsedVarNames(sq, false, false).reverse();
			asgnd[i] = root.getVarNames(sq, false, false).reverse();
			if (!asgnd[i].isEmpty()) {expectResults = true;}
			for (int v = 0; v < asgnd[i].count(); v++) {
				used.removeAll(asgnd[i].get(v));
			}
			String args = "(" + used.concatenate(", ").trim() + ")";
			addCode("Future<Object[]> " + future + " = pool.submit( new " + worker + args + " );", indentPlusOne, isDisabled);
		}

		addCode("", _indent, isDisabled);
		String results = "results" + suffix;	// Name of the temporary results array
		if (expectResults) {
			addCode("Object[] " + results +";", indentPlusOne, isDisabled);
		}
		// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
		//HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo();
		HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo(routinePool);
		// END KGU#676 2019-03-30
		for (int i = 0; i < nThreads; i++) {
			appendComment("----------------- AWAIT THREAD " + i + " -----------------", indentPlusOne);
			String future = "future" + suffix + "_" + i;
			addCode((asgnd[i].isEmpty() ? "" : results + " = ") + future + ".get();", indentPlusOne, isDisabled);
			for (int v = 0; v < asgnd[i].count(); v++) {
				String varName = asgnd[i].get(v);
				TypeMapEntry typeEntry = typeMap.get(varName);
				String typeSpec = "/*type?*/";
				if (typeEntry != null) {
					StringList typeSpecs = this.getTransformedTypes(typeEntry, false);
					if (typeSpecs.count() == 1) {
						typeSpec = typeSpecs.get(0);
					}
				}
				addCode(varName + " = (" + typeSpec + ")(" + results + "[" + v + "]);", indentPlusOne, isDisabled);
			}
		}

		// The shutdown call should be redundant here, but you never know...
		addCode("pool.shutdown();", indentPlusOne, isDisabled);
		addCode("}", _indent, isDisabled);
		addCode("catch (Exception ex) { System.err.println(ex.getMessage()); ex.printStackTrace(); }", _indent, isDisabled);
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}

	// Adds class definitions for worker objects to be used by the threads
	private void generateParallelThreadWorkers(Root _root, String _indent)
	{
		String indentPlusOne = _indent + this.getIndent();
		String indentPlusTwo = indentPlusOne + this.getIndent();
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
		if (!containedParallels.isEmpty()) {
			appendComment("=========== START PARALLEL WORKER DEFINITIONS ============", _indent);
		}
		for (Parallel par: containedParallels) {
			boolean isDisabled = par.isDisabled(false);
			String workerNameBase = "Worker" + Integer.toHexString(par.hashCode()) + "_";
			Root root = Element.getRoot(par);
			// START KGU#676 2019-03-30: Enh. #696 special pool in case of batch export
			//HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo();
			HashMap<String, TypeMapEntry> typeMap = root.getTypeInfo(routinePool);
			// END KGU#676 2019-03-30
			int i = 0;
			// We still don't care for synchronisation, mutual exclusion etc.
			for (Subqueue sq: par.qs) {
				// Variables assigned and used here will be made members
				StringList setVars = root.getVarNames(sq, false).reverse();
				// Variables used here (without being assigned) will be made reference arguments
				StringList usedVars = root.getUsedVarNames(sq, false, false).reverse();
				for (int v = 0; v < setVars.count(); v++) {
					String varName = setVars.get(v);
					usedVars.removeAll(varName);
				}
				if (i > 0) {
					addSepaLine();
				}
				addCode("class " + workerNameBase + i + " implements Callable<Object[]> {", _indent, isDisabled);
				// Member variables (all references!)
				StringList argList = this.makeArgList(setVars, typeMap);
				for (int v = 0; v < argList.count(); v++) {
					addCode("private " + argList.get(v) + ";", indentPlusOne, isDisabled);
				}
				argList = this.makeArgList(usedVars, typeMap);
				for (int v = 0; v < argList.count(); v++) {
					addCode("private " + argList.get(v) + ";", indentPlusOne, isDisabled);
				}
				// Constructor
				addCode("public " + workerNameBase + i + "(" + argList.concatenate(", ") + ") {", indentPlusOne, isDisabled);
				for (int v = 0; v < usedVars.count(); v++) {
					String memberName = usedVars.get(v);
					addCode("this." + memberName + " = " + memberName + ";", indentPlusTwo, isDisabled);
				}
				addCode ("}", indentPlusOne, isDisabled);
				// Call method
				addCode("public Object[] call() throws Exception {", indentPlusOne, isDisabled);
				generateCode(sq, indentPlusTwo);
				addCode ("return new Object[]{" + setVars.concatenate(",") + "};", indentPlusTwo, isDisabled);
				addCode("}", indentPlusOne, isDisabled);
				addCode("};", _indent, isDisabled);
				i++;
			}
		}
		if (!containedParallels.isEmpty()) {
			appendComment("============ END PARALLEL WORKER DEFINITIONS =============", _indent);
			addSepaLine();
		}
	}
	
	/**
	 * Generates an argument list for a worker thread routine as branch of a parallel section.
	 * Types for the variable names in {@code varNames} are retrieved from {@code typeMap}. If
	 * no associated type can be identified then a comment {@code "type?"} will be inserted.
	 * @param varNames - list of variable names to be passed in
	 * @param typeMap - maps variable names and type names to type specifications
	 * @return a list of argument declarations
	 */
	private StringList makeArgList(StringList varNames, HashMap<String, TypeMapEntry> typeMap)
	{
		StringList argList = new StringList();
		for (int v = 0; v < varNames.count(); v++) {
			String varName = varNames.get(v);
			TypeMapEntry typeEntry = typeMap.get(varName);
			String typeSpec = "???";
			if (typeEntry != null) {
				StringList typeSpecs = this.getTransformedTypes(typeEntry, false);
				if (typeSpecs.count() == 1) {
					// START KGU#784 2019-12-02
					//typeSpec = typeSpecs.get(0);
					typeSpec = this.transformTypeFromEntry(typeEntry, null, true);
					// END KGU#784 2019-12-02
				}
			}
			argList.add(typeSpec + " " + varName);
		}
		return argList;
	}
	// END KGU#348 2017-02-25

	// START KGU#686 2019-03-18: Enh. #56
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#makeExceptionFrom(java.lang.String)
	 */
	@Override
	protected void generateThrowWith(String _thrown, String _indent, boolean _asComment) {
		// If it isn't a rethrow then fake some text
		if (_thrown.isEmpty()) {
			if (this.caughtException == null) {
				_thrown = "new Exception(\"unspecified error\")";
			}
			else {
				_thrown = this.caughtException;
			}
		}
		addCode (("throw " + _thrown).trim() + ";", _indent, _asComment);
	}

	// END KGU#686 2019-03-18
	// START KGU#686 2019-03-2: Enh. #56
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#appendCatchHeading(lu.fisch.structorizer.elements.Try, java.lang.String)
	 */
	@Override
	protected void appendCatchHeading(Try _try, String _indent) {
		
		boolean isDisabled = _try.isDisabled(false);
		String varName = _try.getExceptionVarName();
		String exName = "ex" + Integer.toHexString(_try.hashCode());;
		String head = "catch (Exception " + exName + ")";
		this.appendBlockHeading(_try, head, _indent);
		if (varName != null && !varName.isEmpty()) {
			this.addCode("String " + varName + " = " + exName + ".getMessage()", _indent + this.getIndent(), isDisabled);
		}
		this.caughtException = exName;
	}
	// END KGU#686 2019-03-20

	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param _paramNames - list of the argument names
	 * @param _paramTypes - list of corresponding type names (possibly null) 
	 * @param _resultType - result type name (possibly null)
	 * @param _public - whether the resulting method is to be public
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		// START KGU#542 2019-11-18: Enh. #739
		this.root = _root;
		// END KGU#542 2019-11-18
		String indentPlus1 = _indent + this.getIndent();
		String indentPlus2 = indentPlus1 + this.getIndent();
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
			// START KGU#815/KGU#824 2020-03-25: Enh. #828, bugfix #836
			if (this.usesFileAPI && (this.isLibraryModule() || this.importedLibRoots != null)) {
				/* In case of a library we will rather work with a copied FileAPI file than
				 * with copied code, so ensure the using clause for the namespace
				 */
				generatorIncludes.addIfNew("lu.fisch.structorizer.generators." + FILE_API_CLASS_NAME);
			}
			// END KGU#815/KGU#824 2020-03-25
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			// START KGU#376 2017-09-28: Enh. #389 - definitions from all included diagrams will follow
			if (!_root.isProgram()) {
				appendGlobalDefinitions(_root, indentPlus1, true);
			}
			// END KGU#376 2017-09-28
			addSepaLine();
			// This is just pro forma, may be overwritten in generateFooter().
			subroutineInsertionLine = code.count();	// default position for subroutines
			subroutineIndent = _indent;
		}
		else
		{
			addSepaLine();
		}
		// END KGU#178 2016-07-20
		// START KGU#852 2020-04-22: Since method appendDeclarations() does not overwrite typeMap anymore, we must set it
		this.typeMap = new LinkedHashMap<String, TypeMapEntry>(_root.getTypeInfo(routinePool));
		// END KGU#852 2020-04-22
		// START KGU#815 2020-04-01: Enh. #828
		//if (_root.isProgram()) {
		if (topLevel && (_root.isProgram() || this.isLibraryModule())) {
		// END KGU#815 2020-04-01
			//if (topLevel) {
				if (this.hasInput()) {
					this.generatorIncludes.add("java.util.Scanner");
				}
				// START KGU#348 2017-02-24: Enh. #348 - support translation of Parallel elements
				if (this.hasParallels) {
					this.generatorIncludes.add("java.util.concurrent.Callable");
					this.generatorIncludes.add("java.util.concurrent.ExecutorService");
					this.generatorIncludes.add("java.util.concurrent.Executors");
					this.generatorIncludes.add("java.util.concurrent.Future");
				}
				if (this.appendGeneratorIncludes(_indent, false) > 0) {
					addSepaLine();
				}
				// END KGU#348 2017-02-24#
				// STARTB KGU#351 2017-02-26: Enh. #346
				this.appendUserIncludes(_indent);
				// END KGU#351 2017-02-26
				// START KGU#446 2017-10-27: Enh. #441
				this.includeInsertionLine = code.count();
				// END KGU#446 2017-10-27
			//}
			appendBlockComment(_root.getComment(), _indent, "/**", " * ", " */");
			appendBlockHeading(_root, "public class " + _procName, _indent);

			addSepaLine();
			// START KGU#815 2020-04-02: Enh. #828
			if (topLevel && this.isLibraryModule() && _root.isInclude()) {
				appendBlockComment(StringList.explode("Flag ensures that initialisation method {@link #" + this.getInitRoutineName(_root) +"()}\n runs just one time.", "\n"),
						indentPlus1, "/**", " * ", " */");
				addCode(this.makeStaticInitFlagDeclaration(_root, true), indentPlus1, false);
				addSepaLine();
			}
			// END KGU#815 2020-04-02: Enh. #828
			// START KGU#376 2017-09-28: Enh. #389 - definitions from all included diagrams will follow
			//insertComment("TODO Declare and initialise class variables here", this.getIndent());
			appendGlobalDefinitions(_root, indentPlus1, true);
			// END KGU#376 2017-09-28
			addSepaLine();
			// START KGU#542 2019-11-17: Enh. #739 - Temporarily we mark this position for enum type insertion
			subroutineInsertionLine = code.count();
			subroutineIndent = indentPlus1;
			// END KGU#542 2019-11-17
			// START KGU#815 2020-04-01: Enh. #828 Group export - the following is only for programs
		}
		if (_root.isProgram()) {
			// END KGU#815 2020-04-01
			code.add(indentPlus1 + "/**");
			code.add(indentPlus1 + " * @param args");
			code.add(indentPlus1 + " */");

			appendBlockHeading(_root, "public static void main(String[] args)", indentPlus1);
		}
		// START KGU#815 2020-04-02: Enh. #828
		else if (topLevel && this.isLibraryModule() && _root.isInclude()) {
			this.includeInsertionLine = code.count();
			appendBlockComment(StringList.explode("Initialisation method for this library class", "\n"), indentPlus1, "/**", " * ", " */");
			appendBlockHeading(_root, "public static void " + this.getInitRoutineName(_root) + "()",  indentPlus1);
		}
		// END KGU#815 2020-04-02
		else {
			// START KGU#446 2018-01-21: Enh. #441
			this.includeInsertionLine = code.count();
			// END KGU#446 2018-01-21
			// START KGU#371 2019-03-07: Enh. #385 - we have to multiply the declaration in case of default values
			int minArgs = _root.getMinParameterCount();
			StringList argDefaults = _root.getParameterDefaults();
			boolean docResult = false;
			if (_resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
			{
				docResult = true;
				_resultType = transformType(_resultType, "int");
				// START KGU#140 2017-02-01: Enh. #113: Proper conversion of array types
				_resultType = this.transformArrayDeclaration(_resultType, "");
				// END KGU#140 2017-02-01
			}
			else {
				_resultType = "void";
			}
			// Now we must generate as many delegation methods as there are optional arguments
			while (minArgs <= _paramNames.count()) {
			// END KGU#371 2019-03-07
				appendBlockComment(_root.getComment(), indentPlus1, "/**", " * ", null);
				// START KGU#371 2019-03-07: Enh. #385 - we have to multiply the declaration in case of default values
				//appendBlockComment(_paramNames, indentPlus1, null, " * @param ", null);
				appendBlockComment(_paramNames.subSequence(0, minArgs), indentPlus1, null, " * @param ", null);
				// END KGU#371 2019-03-07
				if (docResult) {
					code.add(indentPlus1 + " * @return ");
				}
				code.add(indentPlus1 + " */");
				// START KGU#178 2016-07-20: Enh. #160 - insert called subroutines as private
				//String fnHeader = "public static " + _resultType + " " + _procName + "(";
				String fnHeader = ((topLevel || _public) ? "public" : "private") + " static "
						+ _resultType + " " + _procName + "(";
				// END KGU#178 2016-07-20
				// START KGU#371 2019-03-08: Enh. #385 - create the next delegate
				//for (int p = 0; p < _paramNames.count(); p++) {
				for (int p = 0; p < minArgs; p++) {
					// END KGU#371 2019-03-08
					if (p > 0) { fnHeader += ", "; }
					// START KGU#140 2017-02-01: Enh. #113: Proper conversion of array types
					//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
					//		_paramNames.get(p)).trim();
					// START KGU#993 2021-10-03: Bugfix #993 wrong handling of constant parameters
					//fnHeader += transformArrayDeclaration(transformType(_paramTypes.get(p), "???").trim(), _paramNames.get(p));
					String pType = _paramTypes.get(p);
					if (pType != null && pType.startsWith("const ")) {
						fnHeader += "final ";
						pType = pType.substring("const ".length());
					}
					fnHeader += transformArrayDeclaration(transformType(pType, "???").trim(), _paramNames.get(p));
					// END KGU#993 2021-10-03
					// END KGU#140 2017-02-01
				}
				fnHeader += ")";
				appendBlockHeading(_root, fnHeader,  indentPlus1);
			// START KGU#371 2019-03-07: Enh. #385 - we have to multiply the declaration in case of default values
				if (minArgs < _paramNames.count()) {
						addCode("return " + _procName + "(" + _paramNames.concatenate(", ", 0, minArgs) +
								(minArgs > 0 ? ", " : "") + transform(argDefaults.get(minArgs)) + ");", indentPlus2, false);
					code.add(indentPlus1 + "}");
					code.add(indentPlus1);
				}
				minArgs++;
			}
			// END KGU#371 2019-03-97
		}

		// START KGU#376 2017-09-26: Enh. #389 - add the initialization code of the includables
		// START KGU#815 2020-03-27: Enh. #828 for library top level now done in generateBody()
		//appendGlobalInitialisations(indentPlus2);
		if (!(topLevel && this.isLibraryModule() && _root.isInclude())) {
			appendGlobalInitialisations(_root, indentPlus2);
		}
		// END KGU#815 2020-03-27
		// END KGU#376 2017-09-26

		return indentPlus2;
	}

	// START KGU#332 2017-01-30: Method decomposed - no need to override it anymore
//	/**
//	 * Generates some preamble (i.e. comments, language declaration section etc.)
//	 * and adds it to this.code.
//	 * @param _root - the diagram root element
//	 * @param _indent - the current indentation string
//	 * @param varNames - list of variable names introduced inside the body
//	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		// START KGU#348 2017-02-24: Enh. #348
		this.generateParallelThreadWorkers(_root, _indent);
		// END KGU#348 2017-02-24
		return super.generatePreamble(_root, _indent, varNames);
	}
	
	// START KGU#501 2018-02-22: Bugfix #517
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#getModifiers(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected String getModifiers(Root _root, String _name) {
		if (_root.isInclude()) {
			// FIXME In case of a library there might be external references (how can we know for sure?)
			return (this.isLibraryModule() ? "public" : "private") + " static ";
		}
		return "";
	}
	// END KGU#501 2018-02-22

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformRecordTypeRef(java.lang.String, boolean)
	 */
	@Override
	protected String transformRecordTypeRef(String structName, boolean isRecursive) {
		return structName;
	}

	// START KGU#542 2019-11-17: Enh. #739
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformEnumTypeRef(java.lang.String)
	 */
	@Override
	protected String transformEnumTypeRef(String enumName) {
		return enumName;
	}
	// END KGU#542 2019-11-17

	@Override
	protected String makeArrayDeclaration(String _canonType, String _varName, TypeMapEntry _typeInfo)
	{
		while (_canonType.startsWith("@")) {
			_canonType = _canonType.substring(1) + "[]";
		}
		return (_canonType + " " + _varName).trim(); 
	}
	@Override
	protected void generateIOComment(Root _root, String _indent)
	{
		// START KGU#236 2016-12-22: Issue #227
		if (this.hasInput(_root)) {
			addSepaLine();
			appendComment("TODO: You may have to modify input instructions,", _indent);			
			appendComment("      e.g. by replacing nextLine() with a more suitable call", _indent);
			appendComment("      according to the variable type, say nextInt().", _indent);			
		}
		// END KGU#236 2016-12-22
	}
// END KGU#332 2017-01-30

	// START KGU#834 2020-03-26: Mechanism to ensure one-time initialisation
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#makeStaticInitFlagDeclaration(lu.fisch.structorizer.elements.Root)
	 */
	@Override
	protected String makeStaticInitFlagDeclaration(Root incl, boolean inGlobalDecl) {
		if (inGlobalDecl) {
			return "private static boolean " + this.getInitFlagName(incl) + " = false;";
		}
		return null;
	}
	// END KGU#834 2020-03-26

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
			// START KGU#1084 2023-10-04: Bugfix #1093 Don't invent an undue return statement here
			else {
				return _indent;
			}
			// END KGU#1084 2023-10-24
			addSepaLine();
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

		// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
		if (topLevel) {
			libraryInsertionLine = code.count();
		}
		// END KGU#815/KGU#824 2020-03-19
		
		// Don't close class block if we haven't opened any
		// START KGU#815 2020-04-01: Enh. #828 Group export
		//if (_root.isProgram())	// Should automatically be topLevel too
		if (topLevel &&  (_root.isProgram() || this.isLibraryModule()))
		// END KGU#815 2020-04-01
		{
			// START KGU#178 2016-07-20: Enh. #160
			// Modify the subroutine insertion position
			subroutineInsertionLine = code.count();
			subroutineIndent = _indent;
			// END KGU#178 2016-07-20
			
			// Close class block
			addSepaLine();
			code.add(_indent + "}");
		}
		
		// START KGU#311 2016-12-22: Enh. #314 - insert File API here if necessary
		// START KGU#815 2020-03-29: Enh. #828 - in case of an involved library we will share the copied file instead
		//if (topLevel && this.usesFileAPI) {
		if (topLevel && this.usesFileAPI && !this.isLibraryModule() && this.importedLibRoots == null) {
		// END KGU#815 2020-03-29
			this.insertFileAPI("java");
		}
		// END KGU#311 2016-12-22
		// START KGU#446 2017-10-27: Enh. #441
		if (topLevel && this.usesTurtleizer) {
			// START KGU#563 2018-07-26: Issue #566
			//code.insert(this.commentSymbolLeft() + " TODO: Download the turtle package from http://structorizer.fisch.lu and put it into this project", this.includeInsertionLine++);
			insertCode(this.commentSymbolLeft() + " TODO: Download the turtle package from " + Element.E_HOME_PAGE + " and put it into this project", this.includeInsertionLine);
			// END KGU#563 2018-07-26
			insertCode((_root.isSubroutine() ? this.commentSymbolLeft() : "") + "import lu.fisch.turtle.adapters.Turtleizer;", this.includeInsertionLine);
		}
		// END KGU#446 2017-10-27
	}
	// END KGU 2015-12-15

	// START KGU#815 2020-03-29: Enh. #828 - group export, for libraries better copy the file than the content
	/**
	 * Special handling for the global initializations in case these were outsourced to
	 * an external library {@link #libModuleName}. (The inherited method would suggest a
	 * constructor call but then we would have to care for an instantiation, certainly as
	 * singleton, rather than relying on static methods.
	 * @param _indent - current indentation
	 * @see #appendGlobalInitialisations(Root, String)
	 */
	protected void appendGlobalInitialisationsLib(String _indent) {
		// We simply call the global initialisation function of the library
		addCode(this.libModuleName + ".initialize" + this.libModuleName + "();", _indent, false);
	}

	// START KGU#815/KGU#824/KGU#834 2020-03-26: Enh. #828, bugfix #826
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateBody(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected boolean generateBody(Root _root, String _indent)
	{
		String indentBody = _indent;
		if (topLevel && this.isLibraryModule() && _root.isInclude()) {
			// This is the initialization code for the library
			String cond = "if (!initDone_"  + this.pureFilename + ")";
			if (!this.optionBlockBraceNextLine()) {
				addCode(cond + " {", _indent, false);
			}
			else {
				addCode(cond, _indent, false);
				addCode("{", _indent, false);
			}
			indentBody += this.getIndent();			
			// START KGU#376 2017-09-26: Enh. #389 - add the initialization code of the includables
			appendGlobalInitialisations(_root, indentBody);
			// END KGU#376 2017-09-26
		}
		// END KGU#815/KGU#824 2020-03-20
				
		this.generateCode(_root.children, indentBody);
		boolean done = true;
		
		if (!indentBody.equals(_indent)) {
			addCode("initDone_" + this.pureFilename + " = true;", indentBody, false);
			addCode("}", _indent, false);
		}
		return done;
	}
	// END KGU#815/KGU#824/KGU#834 2020-03-26
	
	// 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#copyFileAPIResources(java.lang.String)
	 */
	@Override
	protected boolean copyFileAPIResources(String _filePath)
	{
		/* If importedLibRoots is not null then we had a multi-module export,
		 * this function will only be called if at least one of the modules required
		 * the file API, so all requiring modules will be using "FileAPI.CS".
		 * Now we simply have to make sure it gets provided.
		 */
		if (this.importedLibRoots != null) {
			return copyFileAPIResource("java", FILE_API_CLASS_NAME + ".java", _filePath);
		}
		return true;	// By default, nothing is to be done and that is okay
	}
	// END KGU#815 2020-03-29
}

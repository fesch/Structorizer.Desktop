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
 *      Description:    This class generates PHP code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Bob Fisch       	    2008-11-17      First Issue
 *      Gunter Schillebeeckx    2009-08-10		Bugfixes (see comment)
 *      Bob Fisch               2009-08-17      Bugfixes (see comment)
 *      Bob Fisch               2010.08-30      Different fixes asked by Kay Gürtzig and Peter Ehrlich
 *      Kay Gürtzig             2010-09-10      Bugfixes and cosmetics (see comment)
 *      Rolf Schmidt            2010-09-15      1. Release of PHPGenerator
 *      Bob Fisch               2011-11-07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014-11-11      Fixed some replacement flaws (see comment)
 *      Kay Gürtzig             2014-11-16      Comment generation revised (now inherited)
 *      Kay Gürtzig             2014-12-02      Additional replacement of "<--" by "<-"
 *      Kay Gürtzig             2015-10-18      Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig             2015-11-02      Variable identification added, Case and
 *                                              For mechanisms improved (KGU#15, KGU#3)
 *      Kay Gürtzig             2015-12-19      Variable prefixing revised (KGU#62) in analogy to PerlGenerator
 *      Kay Gürtzig             2015-12-21      Bugfix #41/#68/#69 (= KGU#93)
 *      Kay Gürtzig             2016-03-22      Enh. #84 (= KGU#61) varNames now inherited, FOR-IN loop support
 *      Kay Gürtzig             2016-03-23      Enh. #84: Support for FOREACH loops (KGU#61)
 *      Kay Gürtzig             2016-04-01      Enh. #144: Care for new option to suppress content conversion
 *      Kay Gürtzig             2016-07-19      Bugfix #191 (= KGU#204): Wrong comparison operator in FOR loops 
 *      Kay Gürtzig             2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178)
 *      Kay Gürtzig             2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2016-09-25      Enh. #253: CodeParser.keywordMap refactoring done. 
 *      Kay Gürtzig             2016-10-14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016-10-15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig             2016-10-16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig             2016-12-01      Bugfix #301: More precise check for parenthesis enclosing of log. conditions
 *      Kay Gürtzig             2016-12-30      Issues #22, #23, KGU#62 fixed (see comment)
 *      Kay Gürtzig             2017-01-03      Enh. #314: File API extension, bugfix #320 (CALL elements)
 *      Kay Gürtzig             2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2017-11-02      Issue #447: Line continuation in Case elements supported
 *      Kay Gürtzig             2019.02.14      Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig             2019-03-08      Enh. #385: Support for parameter default values
 *      Kay Gürtzig             2019-03-21      Enh. #56: Export of Try elements and throw-flavour Jumps
 *      Kay Gürtzig             2019-09-27      Enh. #738: Support for code preview map on Root level
 *      Kay Gürtzig             2019-11-08      Bugfix #769: Undercomplex selector list splitting in CASE generation mended
 *      Kay Gürtzig             2020-03-23      Issue #840: Adaptations w.r.t. disabled elements using File API
 *      Kay Gürtzig             2020-04-05      Enh. #828: Preparations for group export (modified include mechanism)
 *      Kay Gürtzig             2020-04-06/07   Bugfixes #843, #844: Global declarations and record/array types
 *      Kay Gürtzig             2020-11-08/09   Issue #882: Correct translation of random function calls,
 *                                              also: randomize -> srand, toDegrees -> rad2deg, toRadians -> deg2rad
 *      Kay Gürtzig             2021-02-03      Issue #920: Transformation for "Infinity" literal
 *      Kay Gürtzig             2021-12-05      Bugfix #1024: Precautions against defective record initializers
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2016.12.30 - Bugfixes #22/#23/KGU#62 (Kay Gürtzig)
 *      - Forgotten result mechanism analysis and conversion introduced (partial decomposition of generateCode(Root))
 *      - Forgotten setting of argument prefixes and revised header transformation 
 *      
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015.11.02 - Enhancements and code refactoring (Kay Gürtzig)
 *      - Great bunch of transform preprocessing delegated to Generator and Elemnt, respectively
 *      - In order to prefix all variables in the class-specific post-transformation, a variable list
 *        is requested from root. All variable names are then replaced by a $-prefixed name.
 *      - Case generation now copes with multiple constants per branch
 *      - For generation benefits from more sophisticated splitting mechanism on the For class itself 
 *
 *      2015.10.18 - Bugfixes and modifications (Kay Gürtzig)
 *      - Comment method signature simplified
 *      - Bugfix: The export option "export instructions as comments" had been ignored before
 *      - The indentation logic was somehow inconsistent
 *
 *      2014.11.11
 *      - Replacement of rather unlikely comparison operator " >== " mended
 *      - Correction of the output instruction replacement ("echo " instead of "printf(...)")
 *       
 *      2010.09.15 - Version 1.1 of the PHPGenerator, based on the CGenerator of Kay Gürtzig
 *
 *      Comment:
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

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;

import java.util.HashMap;
import java.util.Map.Entry;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;

// FIXME (KGU 2014-11-11): Variable names will have to be accomplished by a '$' prefix - this requires
// sound lexic preprocessing (as do a lot more of the rather lavish mechanisms here)

public class PHPGenerator extends Generator 
{
	// START KGU#61 2016-03-22: Enh. #84 - Now inherited
	// (KGU 2015-11-02) We must know all variable names in order to prefix them with '$'.
	//StringList varNames = new StringList();
	// END KGU#61 2016-03-22

    /************ Fields ***********************/
    protected String getDialogTitle()
    {
            return "Export PHP ...";
    }

    protected String getFileDescription()
    {
            return "PHP Source Code";
    }

    protected String getIndent()
    {
            return "\t";
    }

    protected String[] getFileExtensions()
    {
            String[] exts = {"php"};
            return exts;
    }

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "//";
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
//		"abstract", "and", "array", "as", "break",
//		"case", "catch", "class", "clone", "const", "continue",
//		"declare", "default", "do",
//		"echo", "else", "elseif", "enddeclare", "endfor", "endforeach",
//		"endif", "endswith", "endwhile", "exit", "extends",
//		"final", "finally", "for", "foreach", "function",
//		"global", "goto",
//		"if", "implements", "include", "instanceof", "insteadof",
//		"interface", "namespace", "new", "or",
//		"print", "private", "protected", "public",
//		"require", "return", "static", "switch", "throw", "trait", "try",
//		"use", "var", "while", "xor", "yield"
//		};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	public boolean isCaseSignificant()
//	{
//		return true;
//	}
//	// END KGU 2016-08-12

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		// START KGU#815 2020-04-05: Enh. #828 It makes more sense to use "require_once", particularly for libraries with initialisation
		//return "include '%';";
		return "require_once '%';";
		// END KGU#815 2020-04-05
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/

	// START KGU#839/KGU#840 2020-04-06: Bugfixes #843 (#389, #782), #844 (#423)
	/** Line number for the insertion of 'global' declarations */
	private int declarationInsertionLine = 0;
	/** Gathered type information for the currently exported {@link Root} */
	private HashMap<String, TypeMapEntry> typeMap = null;
	// END KGU#839/KGU#840 2010-04-06
	
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer()
	//{
	//	// This is rather nonsense but ought to help to sort this out somehow
	//	return "$1 = \\$_GET['$1'];	// TODO form a sensible input opportunity";
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "$2 = \\$_REQUEST[$1];	// TODO form a sensible input opportunity";
		}
		// This is rather nonsense but ought to help to sort this out somehow
		return "$1 = \\$_REQUEST['$1'];	// TODO form a sensible input opportunity";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		return "echo $1";
	}

	// START KGU#93 2015-12-21 Bugfix #41/#68/#69
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
		tokens.replaceAll("Infinity", "INF");
		// END KGU#920 2021-02-03
		// START KGU#840 2020-04-06: Bugfix #844 array and record initializers had not been transformed at all
		if (tokens.contains("{") && tokens.contains("}")) {
			tokens = transformInitializers(tokens);
		}
		// Now convert all qualified names into array access via string key
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			if (Function.testIdentifier(token, false, null)) {
				// Check for a preceding dot
				int k = i;
				while (k > 0 && tokens.get(--k).trim().isEmpty());	// Skip all preceding blanks
				boolean isComponent = k >= 0 && tokens.get(k).equals(".");
				if (isComponent) {
					tokens.set(k++, "[");
					tokens.set(i, "]");
					tokens.insert("'" + token + "'", i);
					tokens.remove(k, i);
					i += (k - i) + 1;	// This corrects the current index w.r.t. insertions and deletions 
				}
				// START KGU#885 2020-11-08: Issue #882 - transform the random function
				else {
					// Check whether it looks like a function
					k = i;	// Skip all following blanks
					while (k+1 < tokens.count() && tokens.get(++k).trim().isEmpty());
					if (k < tokens.count() && tokens.get(k).equals("(")) {
						// It is a function or procedure call, k is the "(" index
						if (token.equals("random")) {
							StringList exprs = Element.splitExpressionList(tokens.subSequence(k+1, tokens.count()), ",", true);
							if (exprs.count() == 2 && exprs.get(1).startsWith(")")) {
								// Syntax seems to be okay, so ...
								tokens.set(i, "rand");	// Replace "random" by "rand", ...
								tokens.remove(k+1, tokens.count());	// clear all that follows the "("
								tokens.add("0");		// ... insert a first argument 0,
								tokens.add(",");		// ... the argument separator, and ...
								tokens.add(" ");
								// ... the argument of random, reduced by 1, ...
								tokens.add(Element.splitLexically("(" + exprs.get(0) + ") - 1", true));
								// ... and finally the re-tokenized tail
								tokens.add(Element.splitLexically(exprs.get(1), true));
							}
						}
						else if (token.equals("randomize")) {
							tokens.set(i, "srand");
						}
						else if (token.equals("toDegrees")) {
							tokens.set(i, "rad2deg");
						}
						else if (token.equals("toRadians")) {
							tokens.set(i, "deg2rad");
						}
					}
				}
				// END KGU#885 2020-11-08
			}
			
		}
		// END KGU#840 2020-04-06
		// START KGU#62 2015-12-19: Bugfix #57 - We must work based on a lexical analysis
		for (int i = 0; i < varNames.count(); i++)
		{
			String varName = varNames.get(i);
			//System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
			//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
			tokens.replaceAll(varName, "$"+varName);
		}
		// END KGU#62 2015-12-19
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		// START KGU#311 2017-01-03: Enh. #314 File API
		//if (this.usesFileAPI) {	// KGU#832 2020-03-23: Issue #840 We should even transform disabled code
		for (int i = 0; i < Executor.fileAPI_names.length; i++) {
			tokens.replaceAll(Executor.fileAPI_names[i], "StructorizerFileAPI::" + Executor.fileAPI_names[i]);
		}
		//}
		// END KGU#311 2017-01-03
		return tokens.concatenate(null);
	}
	// END KGU#93 2015-12-21
	
	// END KGU#18/KGU#23 2015-11-01

	// START KGU#840 2020-04-06: Bugfix #844 (enh. #423) - support for record types and array initializers
	/**
	 * Recursively transforms all record and array initializers within the token sequence
	 * {@code _tokens}
	 * @param tokens - the lexically split instruction line (may be partially transformed)
	 * @return the tokens sequence with transformed initializer expressions
	 */
	private StringList transformInitializers(StringList tokens) {
		StringList newTokens = new StringList();
		int posBraceL = -1;
		while ((posBraceL = tokens.indexOf("{")) >= 0 && tokens.indexOf("}", posBraceL+1) >= 0) {
			int posPrev = posBraceL - 1;
			String prevToken = "";
			// Find the previous non-blank token in order to decide whether it is a record type name
			while (posPrev >= 0 && (prevToken = tokens.get(posPrev--).trim()).isEmpty());
			StringList exprs = Element.splitExpressionList(tokens.subSequence(posBraceL+1, tokens.count()), ",", true);
			String tail = "";
			if (exprs.count() > 0 && (tail = exprs.get(exprs.count()-1).trim()).startsWith("}")) {
				exprs = exprs.subSequence(0, exprs.count()-1);
				// Syntax is principally okay, so decide whether it is a record or array initilaizer
				TypeMapEntry type = this.typeMap.get(":" + prevToken);
				if (!prevToken.isEmpty() && type != null && type.isRecord()) {
					newTokens.add(tokens.subSequence(0, posPrev + 1));
					newTokens.add(this.transformRecordInit(prevToken + "{" + exprs.concatenate(",") + "}", type));
				}
				else {
					newTokens.add(tokens.subSequence(0, posBraceL));
					newTokens.add(this.transformArrayInit(exprs));
				}
				tokens = Element.splitLexically(tail.substring(1), true);
			}
			else {
				// Pass all tokens upto and including the found closing brace
				int posBraceR = tokens.indexOf("}", posBraceL+1);
				newTokens.add(tokens.subSequence(0, posBraceR + 1));
				tokens.remove(0, posBraceR + 1);
			}
		}
		// No more braces found, so append the remaining tokens as are
		newTokens.add(tokens);
		return newTokens;
	}

	/**
	 * Transforms the given list of array element expressions {@code exprs} into a
	 * PHP array value, thereby recursively transforming further initializers possibly
	 * contained in the elements of {@code exprs}
	 * @param exprs a {@link StringList} with each element being a (partially transformed)
	 * expression
	 * @return a token sequence representing the equivalent PHP array initializer
	 */
	private StringList transformArrayInit(StringList exprs) {
		StringList result = new StringList();
		result.add("array(");
		for (int i = 0; i < exprs.count(); i++) {
			StringList tokens = Element.splitLexically(exprs.get(i), true);
			if (tokens.contains("{")) {
				tokens = this.transformInitializers(tokens);
			}
			if (i > 0) {
				result.add(",");
			}
			result.add(tokens);
		}
		result.add(")");
		return result;
	}

	/**
	 * Transforms the record initializer into an adequate PHP code.
	 * @param _recordValue - the record initializer according to Structorizer syntax
	 * @param _typeEntry - used to interpret a simplified record initializer (may be null)
	 * @return a string representing an adequate Perl code for the initialisation. May contain
	 * indentation and newline characters
	 */
	protected StringList transformRecordInit(String _recordValue, TypeMapEntry _typeEntry)
	{
		StringList result = new StringList();
		result.add("array(");
		HashMap<String, String> comps = Instruction.splitRecordInitializer(_recordValue, _typeEntry, false);
		// START KGU#1021 2021-12-05: Bugfix #1024 Instruction might be defective
		if (comps != null) {
		// END KGU#1021 2021-12-05
			for (Entry<String, String> comp: comps.entrySet()) {
				String compName = comp.getKey();
				String compVal = comp.getValue();
				if (!compName.startsWith("§") && compVal != null) {
					result.add("\"" + compName + "\"");
					result.add(" "); result.add("=>"); result.add(" ");
					StringList tokens = Element.splitLexically(compVal, true);
					if (tokens.contains("{")) {
						tokens = transformInitializers(tokens);
					}
					result.add(tokens);
					result.add(",");
				}
			}
			if (!result.isEmpty() && result.get(result.count()-1).equals(",")) {
				result.remove(result.count()-1);
			}
		// START KGU#1021 2021-12-05: Bugfix #1024
		}
		// END KGU#1021 2021-12-05
		result.add(")");
		return result;
	}
	// END KGU#840 2020-04-06
	
	// START KGU#815/#839 2020-04-07: Enh. #828, bugfix #843 group export / global declarations
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#updateLineMarkers(int, int)
	 */
	@Override
	protected void updateLineMarkers(int atLine, int nLines) {
		super.updateLineMarkers(atLine, nLines);
		if (this.declarationInsertionLine >= atLine) {
			this.declarationInsertionLine += nLines;
		}
	}
	
	// END KGU#815/KGU#839 2020-04-07
	
	@Override
	protected void generateCode(Instruction _inst, String _indent)
	{
		// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
		if (!appendAsComment(_inst, _indent)) {
			
			boolean isDisabled = _inst.isDisabled(false);
			Root root = Element.getRoot(_inst);
			StringList declVars = root.getMereDeclarationNames(false);
			
			appendComment(_inst, _indent);

			StringList lines = _inst.getUnbrokenText();
			for (int i=0; i<lines.count(); i++)
			{
				// START KGU#281 2016-10-16: Enh. #271
				//addCode(transform(_inst.getText().get(i))+";",
				//		_indent, isDisabled);
				// START KGU#653 2019-02-14: Enh. #680 - support for multi-var input
				String line = lines.get(i);
				StringList inputItems = Instruction.getInputItems(line);
				if (inputItems != null && inputItems.count() > 1) {
					String prompt = inputItems.get(0);;
					// It doesn't make sense to specify the same key for all variables with same prompt
					if (inputItems.count() > 2 || prompt.isEmpty()) {
						prompt = null;
					}
					for (int j = 1; j < inputItems.count(); j++) {
						// Let the variable name be used as default key for the retrieval
						String key = prompt == null ? "'" + inputItems.get(j) + "'" : prompt;
						String subLine = CodeParser.getKeyword("input") + " " + key + " " + inputItems.get(j);
						addCode(transform(subLine) + ";", _indent, isDisabled);
					}
				}
				// START KGU#840 2020-04-06: Bugfix #844 type definitions had just slipped through
				else if (Instruction.isTypeDefinition(line)) {
					this.appendComment(line, _indent);
				}
				// END KGU#840 2020-04-06
				else {
				// END KGU#653 2019-02-14
					String transf = transform(line) + ";";
					if (transf.startsWith("= $_REQUEST[")) {
						transf = "dummyInputVar " + transf;
					}
					// START KGU#284 2016-10-16: Enh. #274
					else if (Instruction.isTurtleizerMove(line)) {
						transf += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
					}
					// END KGU#284 2016-10-16
					// START KGU#839 2020-04-06: Bugfix #843 (issues #389, #782)
					else if (Instruction.isDeclaration(line)) {
						StringList tokens = Element.splitLexically(transf, true);
						// identify declared variable - the token will start with a dollar
						int posVar = 0;
						boolean mereDecl = Instruction.isMereDeclaration(line);
						while (posVar < tokens.count() && !tokens.get(posVar).startsWith("$")) {
							posVar++;
						}
						if (posVar >= tokens.count() && mereDecl) {
							posVar = 0;
							while (posVar < tokens.count() && !varNames.contains(tokens.get(posVar)) && !declVars.contains(tokens.get(posVar))) {
								posVar++;
							}
						}
						int posEqu = tokens.indexOf("=");
						String varName = null;
						if (posVar < tokens.count() && (posEqu < 0 || posEqu > posVar)) {
							varName = tokens.get(posVar);
							if (varName.startsWith("$")) { varName = varName.substring(1); }
							wasDefHandled(Element.getRoot(_inst), varName, true, false);
						}
						if (mereDecl) {
							TypeMapEntry type = null;
							if (varName != null) {
								type = this.typeMap.get(varName);
							}
							if (type != null) {
								if ((type.isRecord() || type.isArray())) {
									transf = "$" + varName + " = array();";
								}
								else {
									String typeSpec = type.getCanonicalType(true, false);
									if (typeSpec.equalsIgnoreCase("string")) {
										transf = "$" + varName + " = \"\";";
									}
									else if (typeSpec.equals("int") || typeSpec.equals("long")) {
										transf = "$" + varName + " = 0;";
									}
									else if (typeSpec.equals("double") || typeSpec.equals("float")) {
										transf = "$" + varName + " = 0.0;";
									}
									else if (typeSpec.equals("boolean")) {
										transf = "$" + varName + " = False;";
									}
									else {
										// We can't do so much more.
										type = null;
									}
								}
							}
							if (type == null) {
								appendComment(line, _indent);
							}
							else {
								addCode(transf, _indent, isDisabled);
							}
							continue;	// No further action here
						}
						// Now we cut off all remnants of the declaration.
						posEqu -= (posVar);	// Should still be >= 0 as there must be an assignment
						tokens.remove(0, posVar);	// This way we should get rid of "var" or "dim"
						int posColon = tokens.indexOf(":");
						if (posColon < 0 || posColon > posEqu) {
							posColon = tokens.indexOf("as", false);
						}
						if (posColon > 0 && posColon < posEqu) {
							tokens.remove(posColon, posEqu);
							tokens.insert(" ", posColon);
						}
						transf = tokens.concatenate(null);
					}
					// END KGU#839 2020-04-06
					addCode(transf,	_indent, isDisabled);
					// END KGU#281 2016-10-16
				// START KGU#653 2019-02-14: Enh. #680 (part 2)
				}
				// END KGU#653 2019-02-14
			}

		}
		// END KGU 2015-10-18
	}

	@Override
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

		addCode("if "+condition+"", _indent, isDisabled);
		addCode("{", _indent, isDisabled);
		generateCode(_alt.qTrue,_indent+this.getIndent());
		if(_alt.qFalse.getSize()!=0)
		{
			addCode("}", _indent, isDisabled);
			addCode("else", _indent, isDisabled);
			addCode("{", _indent, isDisabled);
			generateCode(_alt.qFalse,_indent+this.getIndent());
		}
		addCode("}", _indent, isDisabled);
	}

	@Override
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
		String condition = transform(_case.getText().get(0));
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
		if (!isParenthesized(condition)) condition = "(" + condition + ")";
		// END KGU#301 2016-12-01

		addCode("switch "+condition+" ", _indent, isDisabled);
		addCode("{", _indent, isDisabled);

		for (int i=0; i<_case.qs.size()-1; i++)
		{
			// START KGU#15 2015-11-02: Support for multiple constants per branch
			//code.add(_indent+this.getIndent()+"case "+_case.getText().get(i+1).trim()+":");
			// START KGU#755 2019-11-08: Bugfix #769 - more precise splitting necessary
			//StringList constants = StringList.explode(lines.get(i+1), ",");
			StringList constants = Element.splitExpressionList(lines.get(i + 1), ",");
			// END KGU#755 2019-11-08
			for (int j = 0; j < constants.count(); j++)
			{
				addCode("case " + constants.get(j).trim() + ":", _indent + this.getIndent(), isDisabled);
			}
			// END KGU#15 2015-11-02
			generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent()+this.getIndent());
			addCode("break;", _indent+this.getIndent()+this.getIndent(), isDisabled);
		}

		// START KGU#453 2017-11-02: Issue #447
		//if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		if(!lines.get(_case.qs.size()).trim().equals("%"))
		// END KGU#453 2017-11-02
		{
			addCode("default:", _indent+this.getIndent(), isDisabled);
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
		}
		addCode("}", _indent, isDisabled);
	}

	@Override
	protected void generateCode(For _for, String _indent)
	{
		boolean isDisabled = _for.isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_for, _indent);
		// END KGU 2014-11-16

		// START KGU#3 2015-11-02: Now we have a more reliable mechanism
		String var = _for.getCounterVar();
		// START KGU#162 2016-04-01: Enh. #144 more tentative mode of operation
		if (!var.startsWith("$"))
		{
			var = "$" + var;
		}
		// END KGU#162 2016-04-01
		// START KGU#61 2016-03-23: Enh. #84 - support for FOREACH loop
		if (_for.isForInLoop())
		{
			String valueList = _for.getValueList();
			StringList items = this.extractForInListItems(_for);
			if (items != null)
			{
				valueList = "array(" + transform(items.concatenate(", "), false) + ")";
			}
			else
			{
				valueList = transform(valueList, false);
			}
			// START KGU#162 2016-04-01: Enh. #144 - var syntax already handled
			//code.add(_indent + "foreach (" + valueList + " as $" + var + ")");
			addCode("foreach (" + valueList + " as " + var + ")", _indent, isDisabled);
			// END KGU#162 2016-04-01

		}
		else
		{
			int step = _for.getStepConst();
			// START KGU#204 2016-07-19: Bugfix #191 - operators confused
			//String compOp = (step > 0) ? " >= " : " <= ";
			String compOp = (step > 0) ? " <= " : " >= ";
			// END KGU#204 2016-07-19
			// START KGU#162 2016-04-01: Enh. #144 - var syntax already handled
//			String increment = "$" + var + " += (" + step + ")";
//			code.add(_indent + "for ($" +
//					var + " = " + transform(_for.getStartValue(), false) + "; $" +
//					var + compOp + transform(_for.getEndValue(), false) + "; " +
//					increment +
//					")");
			String increment = var + " += (" + step + ")";
			addCode("for (" +
					var + " = " + transform(_for.getStartValue(), false) + "; " +
					var + compOp + transform(_for.getEndValue(), false) + "; " +
					increment +
					")", _indent, isDisabled);
			// END KGU#162 2016-04-01
		}
		// END KGU#61 2016-03-23
		// END KGU#3 2015-11-02
		addCode("{", _indent, isDisabled);
		generateCode(_for.q,_indent+this.getIndent());
		addCode("}", _indent, isDisabled);
	}

	@Override
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

		addCode("while "+condition+" ", _indent, isDisabled);
		addCode("{", _indent, isDisabled);
		generateCode(_while.q,_indent+this.getIndent());
		addCode("}", _indent, isDisabled);
	}

	@Override
	protected void generateCode(Repeat _repeat, String _indent)
	{
		boolean isDisabled = _repeat.isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_repeat, _indent);
		// END KGU 2014-11-16

		addCode("do", _indent, isDisabled);
		addCode("{", _indent, isDisabled);
		generateCode(_repeat.q,_indent+this.getIndent());
		// START KGU#162 2016-04-01: Enh. #144 - more tentative approach
		//code.add(_indent+"} while (!("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+"));");
		String condition = transform(_repeat.getUnbrokenText().getLongString()).trim();
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!this.suppressTransformation || !(condition.startsWith("(") && !condition.endsWith(")")))
		if (!this.suppressTransformation || !isParenthesized(condition))
		// END KGU#301 2016-12-01
		{
			condition = "( " + condition + " )";
		}
		addCode("} while (!" + condition + ");", _indent, isDisabled);
		// END KGU#162 2016-04-01
	}

    @Override
    protected void generateCode(Forever _forever, String _indent)
    {
        boolean isDisabled = _forever.isDisabled(false);
        
        // START KGU 2014-11-16
        appendComment(_forever, _indent);
        // END KGU 2014-11-16

        addCode("while (true)", _indent, isDisabled);
        addCode("{", _indent, isDisabled);
        generateCode(_forever.q,_indent+this.getIndent());
        addCode("}", _indent, isDisabled);
    }

	@Override
	protected void generateCode(Call _call, String _indent)
	{
		boolean isDisabled = _call.isDisabled(false);

		// START KGU 2014-11-16
		appendComment(_call, _indent);
		// END KGU 2014-11-16

		StringList lines = _call.getUnbrokenText();
		for(int i=0;i<lines.count();i++)
		{
			// START KGU#319 2017-01-03: Bugfix #320 - Obsolete postfixing removed
			//addCode(transform(_call.getText().get(i))+"();", _indent, isDisabled);
			addCode(transform(lines.get(i))+";", _indent, isDisabled);
			// END KGU#319 2017-01-03
		}
	}

	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		boolean isDisabled = _jump.isDisabled(false);
		
		// START KGU 2014-11-16
		appendComment(_jump, _indent);
		// END KGU 2014-11-16

		// START KGU#78 2015-12-18: Enh. #23 - sensible exit strategy
		//for(int i=0;i<_jump.getText().count();i++)
		//{
		//        code.add(_indent+transform(_jump.getText().get(i))+";");
		//}
		// In case of an empty text generate a continue instruction by default.
		boolean isEmpty = true;
		
		StringList lines = _jump.getUnbrokenText();
		String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
		String preExit   = CodeParser.getKeywordOrDefault("preExit", "exit");
		//String preLeave  = CodeParser.getKeywordOrDefault("preLeave", "leave");
		String preThrow  = CodeParser.getKeywordOrDefault("preThrow", "throw");
		for (int i = 0; isEmpty && i < lines.count(); i++) {
			// FIXME: That the line is transformed prior to the detection is a potential risk
			String line = transform(lines.get(i)).trim();
			if (!line.isEmpty())
			{
				isEmpty = false;
			}
			if (Jump.isReturn(line))
			{
				addCode("return " + line.substring(preReturn.length()).trim() + ";", _indent, isDisabled);
			}
			else if (Jump.isExit(line))
			{
				addCode("exit(" + line.substring(preExit.length()).trim() + ");", _indent, isDisabled);
			}
			// Has it already been matched with a loop? Then syntax must have been okay...
			else if (this.jumpTable.containsKey(_jump))
			{
				// FIXME (KGU 2017-01-02: PHP allows break n - but switch constructs add to the level) 
				Integer ref = this.jumpTable.get(_jump);
				String label = this.labelBaseName + ref;
				if (ref.intValue() < 0)
				{
					appendComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
					appendComment(line, _indent);
					label = "__ERROR__";
				}
				addCode("goto " + label + ";", _indent, isDisabled);
			}
			// START KGU#686 2019-03-21: Enh. #56
			else if (Jump.isThrow(line)) {
				addCode("throw new Exception(" + line.substring(preThrow.length()).trim() + ");", _indent, isDisabled);
			}
			// END KGU#686 2019-03-21
			else if (Jump.isLeave(line))
			{
				// Strange case: neither matched nor rejected - how can this happen?
				// Try with an ordinary break instruction and a funny comment
				addCode("last;\t" + this.commentSymbolLeft() + " FIXME: Dubious occurrence of 'last' instruction!",
						_indent, isDisabled);
			}
			else if (!isEmpty)
			{
				appendComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
				appendComment(line, _indent);
			}
			// END KGU#74/KGU#78 2015-11-30
		}
		if (isEmpty) {
			addCode("last;", _indent, isDisabled);
		}
		// END KGU#78 2015-12-18
	}
	
	// START KGU#686 2019-03-21: Enh. #56
	protected void generateCode(Try _try, String _indent)
	{
		boolean isDisabled = _try.isDisabled(false);
		String indentPlus1 = _indent + this.getIndent();
		String varName = _try.getExceptionVarName();
		String exName = "ex" + Integer.toHexString(_try.hashCode());
		
		this.appendComment(_try, _indent);
		
		this.addCode("try {", _indent, isDisabled);
		this.generateCode(_try.qTry, indentPlus1);
		this.addCode("} catch (Exception $e" + exName + ") {", _indent, isDisabled);
		if (varName != null && !varName.isEmpty()) {
			this.addCode("$" + varName + " = $" + exName + "->getMessage();", indentPlus1, isDisabled);
		}
		this.generateCode(_try.qCatch, indentPlus1);
		if (_try.qFinally.getSize() > 0) {
			this.addCode("} finally {", _indent, isDisabled);
			this.generateCode(_try.qFinally, indentPlus1);
		}
		this.addCode("}", _indent, isDisabled);
	}
	// END KGU#686 2019-03-21

    @Override
    public String generateCode(Root _root, String _indent, boolean _public)
    {
        // START KGU 2015-11-02: First of all, fetch all variable names from the entire diagram
        varNames = _root.retrieveVarNames();
        // END KGU 2015-11-02
        String procName = _root.getMethodName();
        // START KGU#74/KGU#78 2016-12-30: Issues #22/#23: Return mechanisms hadn't been fixed here until now
        boolean alwaysReturns = mapJumps(_root.children);
        this.isResultSet = varNames.contains("result", false);
        this.isFunctionNameSet = varNames.contains(procName);
        // END KGU#74/KGU#78 2016-12-30
        // START KGU#676/KGU#840 2020-04-06: Bugfix #844 (enh. #696) type information, from special pool in case of batch export
        this.typeMap = _root.getTypeInfo(routinePool);
        // END KGU#676/KGU#840 2020-04-06
        
        String pr = "program";
        if (_root.isSubroutine()) {
            pr = "function";
        } else if (_root.isInclude()) {
            pr = "includable";
        }

        // START KGU#705 2019-09-23: Enh. #738
        int line0 = code.count();
        if (codeMap!= null) {
            // register the triple of start line no, end line no, and indentation depth
            // (tab chars count as 1 char for the text positioning!)
            codeMap.put(_root, new int[]{line0, line0, _indent.length()});
        }
        // END KGU#705 2019-09-23

        // START KGU#178 2016-07-20: Enh. #160
        //code.add("<?php");
        //insertComment(pr+" "+_root.getMethodName() + " (generated by Structorizer)", _indent);
        if (topLevel)
        {
            code.add("<?php");
            appendComment(pr+" "+ procName + " (generated by Structorizer " + Element.E_VERSION + ")", _indent);
            // START KGU#363 2017-05-16: Enh. #372
            appendCopyright(_root, _indent, true);
            // END KGU#363 2017-05-16
            // START KGU#815 2020-04-08: Enh. #828 group export may require to share the entire module - so better copy the file
            //if (this.usesFileAPI) {
            if (this.usesFileAPI && (this.isLibraryModule() || this.importedLibRoots != null)) {
                generatorIncludes.add("StructorizerFileAPI");
            }
            // END KGU#815 2020-04-08
            // START KGU#815 2020-04-05: Enh. #828: For group export generatoir includes are essential
            this.appendGeneratorIncludes("", false);
            // END KGU#815 2020-04-05: 
            // START KGU#351 2017-02-26: Enh. #346
            this.appendUserIncludes("");
            // END KGU#351 2017-02-26
            addSepaLine();
            // START KGU#767 2020-04-05: Issue #782
            this.appendGlobalDefinitions(_root, _indent, false);
            if (_root.isProgram() || this.isLibraryModule()) {
                this.appendGlobalInitialisations(_root, _indent);
            }
            // END KGU#767 2020-04-05
            subroutineInsertionLine = code.count();
            // START KGU#311 2017-01-03: Enh. #314 File API support
            // START KGU#815 2020-04-08: Enh. #828 group export may require to share the entire module - so better copy the file
            //if (this.usesFileAPI) {
            if (this.usesFileAPI && !(this.isLibraryModule() || this.importedLibRoots != null)) {
            // END KGU#815 2020-04-08
                this.insertFileAPI("php");
            }
            // END KGU#311 2017-01-03
        }
        addSepaLine();
        if (!topLevel || !subroutines.isEmpty())
        {
            appendComment(pr + " " + procName, _indent);
        }
        // END KGU#178 2016-07-20
        // START KGU 2014-11-16
        appendComment(_root, "");
        // END KGU 2014-11-16
        // START KGU#815 2020-04-06: Enh. #828 group export
        //if (_root.isProgram() == true)
        if (_root.isProgram() || topLevel && _root.isInclude())
        // END KGU#815 2020-04-06
        {
            //addSepaLine();
            //appendComment("TODO declare your variables here if necessary", _indent);
            addSepaLine();
            if (this.hasInput(_root)) {
                appendComment("TODO Establish sensible web formulars to get the $_GET input working.", _indent);
                addSepaLine();
            }
            generateCode(_root.children, _indent);
        }
        else
        {
            String fnHeader = _root.getText().get(0).trim();
            // START #62 2016-12-30: Function header revision had been forgotten completely
            //if (fnHeader.indexOf('(')==-1 || !fnHeader.endsWith(")")) fnHeader=fnHeader+"()";
            if (this.suppressTransformation) {
                if (fnHeader.indexOf('(')==-1 || !fnHeader.endsWith(")")) fnHeader=fnHeader+"()";
            }
            else {
                fnHeader = procName + "(";
                StringList argNames = _root.getParameterNames();
                // START KGU#371 2019-3-08: Enh. #385 support for optional arguments
                int minArgs = _root.getMinParameterCount();
                StringList argDefaults = _root.getParameterDefaults();
                // END KGU#371 2019-03-08
                for (int i = 0; i < argNames.count(); i++) {
                	String argName = argNames.get(i);
                	if (!argName.startsWith("$")) {
                		argName = "$" + argName;
                	}
                	if (i > 0) {
                		fnHeader += ", " + argName;
                	}
                	else {
                		fnHeader += argName;
                	}
                	// START KGU#371 2019-3-08: Enh. #385 support for optional arguments
                	if (i >= minArgs) {
                		fnHeader += " = " + transform(argDefaults.get(i));
                	}
                	// END KGU#371 2019-03-08
                }
                fnHeader += ")";
            }
            // END KGU#62 2016-12-30
            code.add("function " + fnHeader);
            code.add("{");
            //appendComment("TODO declare your variables here if necessary", _indent + this.getIndent());
            // START KGU#839 2020-04-06: Bugfix #843 (#389, #782)
            this.declarationInsertionLine = code.count();
            // END KGU#839 2020-04-06
            addSepaLine();
            appendComment("TODO Establish sensible web formulars to get the $_GET input working.", _indent + this.getIndent());
            addSepaLine();
            generateCode(_root.children, _indent + this.getIndent());
            // START KGU#74/KGU#78 2016-12-30: Issues #22/#23: Return mechanisms hadn't been fixed here until now
            if (!this.suppressTransformation) {
            	this.generateResult(_root, _indent + this.getIndent(), alwaysReturns, varNames);
            }
            // END KGU#74/KGU#78 2016-12-30
            code.add("}");
            // START KGU#839 2020-04-06: Bugfix #843 (#389, #782)
            this.insertGlobalClause(_root, _indent + this.getIndent());
            // END KGU#839 2020-04-06

        }

        // START KGU#178 2016-07-20: Enh. #160
        //code.add("?>");
        if (topLevel)
        {
            // START KGU#815 2020-04-05: Enh. #828: Group export
            this.libraryInsertionLine = code.count();
            addSepaLine();
            // END KGU#815 2020-04-05
            code.add("?>");
        }
        // END KGU#178 2016-07-20

        // START KGU#705 2019-09-23: Enh. #738
        if (codeMap != null) {
            // Update the end line no relative to the start line no
            codeMap.get(_root)[1] += (code.count() - line0);
        }
        // END KGU#705 2019-09-23

        return code.getText();
    }

	private void insertGlobalClause(Root _root, String _indent) {
		if (_root.includeList != null) {
			int nVarsPerLine = 5;
			// Gather global variables
			StringList globVars = new StringList();
			for (Root incl: this.includedRoots) {
				if (_root.includeList.contains(incl.getMethodName())) {
					globVars.addIfNew(incl.getVarNames());
					globVars.addIfNew(incl.getMereDeclarationNames(false));
				}
			}
			for (int i = globVars.count() - 1; i >= 0; i--) {
				if (wasDefHandled(_root, globVars.get(i), false, false)) {
					// Variable was locally overridden, it seems, so drop it
					globVars.remove(i);
				}
			}
			// Now we insert global declarations 5 by 5
			int start = 0;
			while (start+1 < globVars.count()) {
				insertCode(_indent + "global $"
						+ globVars.concatenate(", $", start, Math.min(start+nVarsPerLine, globVars.count())) + ";",
						declarationInsertionLine);
				start += nVarsPerLine;
			}
		}
	}

	// START KGU#839 2020-04-06: Enh. #389 + issue #782
	protected void appendDefinitions(Root _root, String _indent, StringList _varNames, boolean _force) {
		if (topLevel) {
			for (int i = 0; i < _varNames.count(); i++) {
				String varName = _varNames.get(i);
				if (_root.constants.containsKey(varName)) {
					// This should also solve the enumerator type problem - does it?
					code.add(_indent + "define('" + varName + "', " + this.transform(_root.getConstValueString(varName))+ ")");
				}
				// Simply declare it formally
				this.wasDefHandled(_root, varName, true);
			}
		}
	}
	// END KGU#483 2018-01-02

	// START KGU#74/KGU#78 2016-12-30: Issues #22/#23 hadn't been solved for PHP...
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
		if (_root.isSubroutine() && (returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
		{
			String result = "0";
			if (isFunctionNameSet)
			{
				result = "$" + _root.getMethodName();
			}
			else if (isResultSet)
			{
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
				if (!result.startsWith("$")) {
					result = "$" + result;
				}
			}
			addSepaLine();
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}
	// END KGU#74/KGU#78 2016-12-30

	// START KGU#815 2020-03-26: Enh. #828 - group export, for libraries better copy the FileAPI file than the content
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
			return copyFileAPIResource("php",  "StructorizerFileAPI.php", _filePath);
		}
		return true;	// By default, nothing is to be done and that is okay
	}
	// END KGU#815 2020-03-26
}

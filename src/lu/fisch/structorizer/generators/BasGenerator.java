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
 *      Description:    This class generates Basic code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author              Date            Description
 *      ------              ----            -----------
 *      Jacek Dzieniewicz   2013-03-02      First Issue
 *      Kay Gürtzig         2015-10-18      Comment generation revised
 *      Kay Gürtzig         2015-11-02      Case generation was defective (KGU#58), comments exported,
 *                                          transformation reorganised, FOR loop mended (KGU#3)
 *      Kay Gürtzig         2015-12-18      Enh. #9 (KGU#2) Call mechanisms had to be refined,
 *                                          Enh. #23 (KGU#78) Jump mechanism implemented
 *                                          Root generation decomposed and fundamentally revised
 *                                          Enh. #67 (KGU#113) Line number generation considered
 *      Kay Gürtzig         2015-12-19      Bugfix #51 (KGU#108) empty input instruction
 *                                          Enh. #54 (KGU#101) multiple expressions on output
 *      Kay Gürtzig         2015-12-21      Bugfix #41/#68/#69 (= KGU#93)
 *      Kay Gürtzig         2016-01-22      Bugfix/Enh. #84 (= KGU#100): Array initialisation
 *      Kay Gürtzig         2016-03-31      Enh. #144 - content conversion may be switched off
 *      Kay Gürtzig         2016-04-04      Enh. #150 - Pascal functions ord and chr translated
 *      Kay Gürtzig         2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178),
 *                                          though this is only provisional for the line numbering mode
 *      Kay Gürtzig         2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig         2016-09-25      Enh. #253: CodeParser.keywordMap refactoring done
 *      Kay Gürtzig         2016-10-13      Enh. #270: Handling of disabled elements added.
 *      Kay Gürtzig         2016-10-15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig         2016-10-16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig         2016-11-20      KGU#293: Some forgotten traditional keywords added to reservedWords (#231)
 *      Kay Gürtzig         2017-02-27      Enh. #346: Formal adaptation
 *      Kay Gürtzig         2017-03-15      Bugfix #382: FOR-IN loop value list items hadn't been transformed 
 *      Kay Gürtzig         2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig         2017-11-02      Issues #416, #447: Line continuation in Case elements supported
 *      Kay Gürtzig         2019-03-08      Enh. #385: Support for parameter default values
 *      Kay Gürtzig         2019-03-13      Enh. #696: All references to Arranger replaced by routinePool
 *      Kay Gürtzig         2019-03-08      Enh. #385: Support for parameter default values
 *      Kay Gürtzig         2019-03-18      Enh. #56: Export of Try blocks implemented
 *      Kay Gürtzig         2019-03-28      Enh. #657: Retrieval for subroutines now with group filter
 *      Kay Gürtzig         2019-12-01      Bugfix #789 (function calls); keyword case modified for non-vintage BASIC,
 *                                          bugfix #790 line continuation according to #416 hadn't been considered,
 *                                          array initializers and record types hadn't been handled correctly
 *      Kay Gürtzig         2020-04-08      Issue #828 modifications supporting group export
 *      Kay Gürtzig         2020-10-16      Bugfix #874: Nullpointer exception on Calls with non-ASCII letters in name
 *      Kay Gürtzig         2021-02-03      Issue #920: Attempt to transform "Infinity" literal
 *      Kay Gürtzig         2021-06-07      Issue #67: lineNumering option made plugin-specific
 *      Kay Gürtzig         2021-10-03/04   Bugfix #993: Wrong handling of constant parameters, array types, and mere declarations
 *      Kay Gürtzig         2021-12-05      Bugfix #1024: Precautions against defective record initializers
 *      Kay Gürtzig         2023-10-04      Bugfix #1093 Undue final return 0 on function diagrams
 *      Kay Gürtzig         2024-03-19      Issue #1148: Special indentation for "if else if" chains
 *      Kay Gürtzig         2025-02-06      Bugfix #1188: Array declarations in C style weren't correctly
 *                                          processed. Array declarations for vintage BASIC no longer suppressed
 *      Kay Gürtzig         2025-02-07/08   Bugfix #1190: Includables now fully involved. Declaration placement
 *                                          fundamentally revised, record and array inits now recursive.
 *      Kay Gürtzig         2025-02-16      Bugfix #1192: Return keywords in Instruction elements weren't transformed
 *
 ******************************************************************************************************
 *
 *      Comments:
 *
 *      2025-02-06/07 - Bugfixes #1188, #1190 (Kay Gürtzig)
 *      - Severe deficiencies were found w.r.t. type definitions, declarations and includes as well as
 *        nested initialisation expressions. Apparently the includes have never been considered here. The
 *        variable declarations in the preamble were incomplete (particularly for vintage mode none were
 *        produced). No record type definitions were produced for modern BASIC mode (no line numbering).
 *        Otherwise the variable declarations in the preamble preceded their type definitions. So this has
 *        to be fundamentally revised and updated.
 *        It was considered to add all definitions and declarations where they occur in the diagrams rather
 *        than to concentrate them in the preamble (This would not of course apply to definitions from
 *        involved Includables). But this turned out to produce nonsense e.g. for an iterative array filling
 *        within loops where the target array variable wasn't mentioned before the loop.
 *        
 *
 *      2015-12-21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU#23)
 *      - Enhancement #10 (KGU#3): FOR loops now provide more reliable loop parameters 
 *      - Enhancement KGU#15: Support for the gathering of several case values in CASE instructions
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Loop;
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


public class BasGenerator extends Generator 
{

	// START KGU#74 2015-12-18: Bugfix #22: Needed for one of the return mechanisms
	// The method name of root
	protected String procName = "";
	
	protected int lineNumber = 10;
	protected int lineIncrement = 10;
	protected int[] labelMap;
	// END KGU#74 2015-12-18
	
	/************ Fields ***********************/
    @Override
    protected String getDialogTitle()
    {
            return "Export Basic Code ...";
    }

    @Override
    protected String getFileDescription()
    {
            return "Basic Code";
    }

    @Override
    protected String getIndent()
    {
            return "  ";
    }

    @Override
    protected String[] getFileExtensions()
    {
            String[] exts = {"bas"};
            return exts;
    }

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return transformKeyword("REM");
    }
    // END KGU 2015-10-18

    // START KGU#371 2019-03-07: Enh. #385
    /**
     * @return The level of subroutine overloading support in the target language
     */
    @Override
    protected OverloadingLevel getOverloadingLevel() {
        if(this.optionCodeLineNumbering()) {
            // Well, with GOSUB n, this is not even an issue...
            return OverloadingLevel.OL_NO_OVERLOADING;
        }
        else {
            return OverloadingLevel.OL_DEFAULT_ARGUMENTS;
        }
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
		return (this.optionCodeLineNumbering() ? TryCatchSupportLevel.TC_NO_TRY : TryCatchSupportLevel.TC_TRY_CATCH_FINALLY);
	}
	// END KGU#686 2019-03-18
	
	//	// START KGU 2016-08-12: Enh. #231 - information for analyser
//    private static final String[] reservedWords = new String[]{
//		"FUNCTION", "SUB",
//		"REM", "LET", "AS", "DIM",
//		"IF", "THEN", "ELSE", "END",
//		"SELECT", "CASE",
//		"FOR", "TO", "STEP", "NEXT",
//		"DO", "WHILE", "UNTIL", "LOOP",
//		"CALL", "RETURN", "GOTO", "GOSUB", "STOP",
//		// START KGU#293 2016-11-20
//		"INPUT", "PRINT", "READ", "DATA", "RESTORE",
//		// END KGU#293 2016-11-20
//		"AND", "OR", "NOT"};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	public boolean isCaseSignificant()
//	{
//		return false;
//	}
//	// END KGU 2016-08-12

	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean breakMatchesCase()
	{
		return false;
	}
	// END KGU#78 2015-12-18
	
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return null;
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	// START KGU#281-10-15: Enh. #271 (support for input with prompt)
	//protected String getInputReplacer(boolean withPrompt)
	//{
	//	return "INPUT $1";
	//}
	@Override
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "PRINT $1; : INPUT $2";
		}
		return "INPUT $1";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		return "PRINT $1";
	}

	// START KGU#113 2021-06-07: Enh. #67 - Line numbering now as plugin-specific option
	/**
	 * Returns the value of the export option whether to generate line numbers
	 * at the beginning of every single line.
	 * @return true if lines are to start with numbers.
	 */
	protected boolean optionCodeLineNumbering() {
		// START KGU 2016-04-04: Issue #151 - Get rid of the inflationary eod threads
		//return (eod.lineNumbersCheckBox.isSelected());
		// START KGU#113 2021-06-07: Enh. #67 Converted to a plugin-specific option
		//return this.generateLineNumbers;
		Object optionVal = this.getPluginOption("lineNumbering", false);
		return optionVal instanceof Boolean && (Boolean)optionVal;
		// END KGU 2016-04-04
	}
	// END KGU#113 2015-12-18	
	
	// START KGU 2019-12-01 - Use different case styles for Vintage BASIC and modern BASIC
	private String transformKeyword(String keyword)
	{
		if (this.optionCodeLineNumbering()) {
			keyword = keyword.toUpperCase();
		}
		// START KGU#1176 2025-02-07: Bugfix #1190
		else if (keyword.equalsIgnoreCase("TYPE")) {
			keyword = "Structure";	// The VisualBasic equivalent for TYPE
		}
		// END KGU#1176 2025-02-07
		else {
			String[] parts = keyword.split(" ", -2);
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				if (!part.isEmpty()) {
					parts[i] = Character.toUpperCase(part.charAt(0)) + part.substring(1).toLowerCase();
				}
			}
			keyword = (new StringList(parts)).concatenate(" ");
		}
		return keyword;
	}
	// END KGU 2019-12-01
	
	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		tokens.replaceAll("==", "=");
		tokens.replaceAll("!=", "<>");
		tokens.replaceAll("&&", "AND");
		tokens.replaceAll("||", "OR");
		tokens.replaceAll("!", "NOT");
		tokens.replaceAll("[", "(");
		tokens.replaceAll("]", ")");
		tokens.replaceAll("div", "/");
		// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
		// FIXME: Will only work with VisualBasic, others might try with 1.0/0.0
		if (!this.optionCodeLineNumbering())
		{
			tokens.replaceAll("Infinity", "Double.PositiveInfinity");
		}
		// END KGU#920 2021-02-03
		// START KGU#150 2016-04-04: Handle Pascal ord and chr function
		int pos = - 1;
		while ((pos = tokens.indexOf("ord", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "Asc");
		}
		pos = -1;
		while ((pos = tokens.indexOf("chr", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			if (this.optionCodeLineNumbering())
			{
				tokens.set(pos, "Chr$");
			}
			else
			{
				tokens.set(pos,  "Chr");
			}
		}
		// END KGU#150 2016-04-04
		// START KGU 2019-12-01 In vintage BASIC there is no "const"
		if (this.optionCodeLineNumbering()) {
			tokens.removeAll("const");
		}
		// END KGU 2019-12-01
		if (tokens.contains("<-") && this.optionCodeLineNumbering())
		{
			// Insert a "LET" keyword but ensure a separating blank between it and the variable name
			if (!tokens.get(0).equals(" "))	tokens.insert(" ", 0);
			tokens.insert(transformKeyword("LET"), 0);
		}
		// START KGU#100 2016-01-22: Enh #84 - Array initialisation for Visual/modern BASIC
		if (!this.optionCodeLineNumbering())
		{
			tokens.replaceAll("{", "Array(");
			tokens.replaceAll("}", ")");
		}
		// END KGU#100 2016-01-22
		tokens.replaceAll("<-", "=");
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21
	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#113 2015-12-18: Enh. #67: 
	/**
	 * Provides a current line number string plus blank as line prefix if required.</br>
	 * NOTE: This call increments the line number by {@link #lineIncrement} in line
	 * numbering mode! You may inspect field {@link #lineNumber} instead if you don't
	 * want to manipulate its value.
	 */
	protected String getLineNumber()
	{
		String prefix = "";
		if (this.optionCodeLineNumbering())
		{
			prefix = this.lineNumber + " ";
			this.lineNumber += this.lineIncrement;
		}
		return prefix;
	}

	protected void placeJumpTarget(Loop _loop, String _indent)
	{
		if (this.jumpTable.containsKey(_loop))
		{
			if (this.optionCodeLineNumbering())
			{
				// Associate label number with line number of the following dummy comment 
				this.labelMap[this.jumpTable.get(_loop).intValue()] = this.lineNumber;
				appendComment("Exit point from above loop.", _indent);
			}
			else
			{
				// START KGU#277 2016-10-13: Enh. #270
				//code.add(_indent + this.labelBaseName + this.jumpTable.get(_loop).toString() + ": " +
				//		this.commentSymbolLeft() + " Exit point from above loop.");
				addCode(this.labelBaseName + this.jumpTable.get(_loop).toString() + ": " +
						this.commentSymbolLeft() + " Exit point from above loop.",
						_indent, _loop.isDisabled(false));
				// END KGU#277 2016-10-13
			}
		}

	}
	
	// We need an overridden fundamental comment method here to be able to insert line numbers.
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#appendComment(java.lang.String, java.lang.String)
	 */
	@Override
	protected void appendComment(String _text, String _indent)
	{
		String[] lines = _text.split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			code.add(this.getLineNumber() + _indent + commentSymbolLeft() + " " + lines[i] + " " + commentSymbolRight());
		}
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#appendBlockComment(lu.fisch.utils.StringList, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void appendBlockComment(StringList _sl, String _indent, String _start, String _cont, String _end)
	{
		int oldSize = code.count();
		super.appendBlockComment(_sl, _indent, _start, _cont, _end);
		// Set the line numbers afterwards, the super method wouldn't have done it
		if (this.optionCodeLineNumbering())
		{
			for (int i = oldSize; i < code.count(); i++)
			{
				code.set(i, this.getLineNumber() + " " + code.get(i));
			}
		}
	}
	// END KGU#113 2015-12-18

	// START KGU#18/KGU#23 2015-11-02: Method properly sub-classed
	//    private String transform(String _input)
	@Override
	protected String transform(String _input)
	{
		// START KGU#101 2015-12-19: Enh. #54 - support lists of output expressions
		if (_input.matches("^" + getKeywordPattern(CodeParser.getKeyword("output").trim()) + "[ ](.*?)"))
		{
			// Replace commas by semicolons to avoid tabulation
			StringList expressions = 
					Element.splitExpressionList(_input.substring(CodeParser.getKeyword("output").trim().length()), ",");
			_input = CodeParser.getKeyword("output").trim() + " " + expressions.getText().replace("\n", "; ");
		}
		// END KGU#101 2015-12-19

		String interm = super.transform(_input);
		
		// START KGU#993 2021-10-04: Issue #993
		if (interm.startsWith("const ")) {
			interm = this.transformKeyword("CONST") + interm.substring("const".length());
		}
		// END KGU#993 2021-10-04
		
		// Operator translations; KGU#93: now in transformTokens() 
//		interm = interm.replace(" == ", " = ");
//		interm = interm.replace(" != ", " <> ");
//		interm = interm.replace(" && ", " AND ");
//		interm = interm.replace(" || ", " OR ");
//		interm = interm.replace(" ! ", " NOT ");
//		// START KGU 2015-12-19: In BASIC, array indices are usually encöosed by parentheses rather than brackets
//		interm = interm.replace("[", "(");
//		interm = interm.replace("]", ")");
		// END KGU 2015-12-19

		// START KGU#108 2015-12-19: Bugfix #51/Enh. #271: Cope with empty input
		if (interm.trim().equals("INPUT") || interm.endsWith(": INPUT"))
		{
			interm = interm.replace("INPUT", transformKeyword("SLEEP"));	// waits for key hit (according to https://en.wikibooks.org/wiki/BASIC_Programming/Beginning_BASIC/User_Input)
		}
		// END KGU#108 2015-12-19

		return interm.trim();
	}
	// END KGU#18/KGU#23 2015-11-02
	
	// START KGU#16 2015-12-19
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformType(java.lang.String, java.lang.String)
	 */
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		else {
			_type = _type.trim();
			if (_type.equalsIgnoreCase("int")) _type = "Integer";
			else if (_type.equalsIgnoreCase("string") || _type.equals("char[]")) _type = "String";
			// To be continued if required...
			// START KGU#993 2021-10-03: Bugfix #993: Need an array translation for arglists
			else if (_type.startsWith("array ") || _type.startsWith("array[")) {
				_type = _type.substring("array".length());
				int posOf = _type.indexOf(" of ");
				String elemType = _default;
				if (posOf >= 0) {
					elemType = transformType(_type.substring(posOf + " of ".length()), _default);
					_type = _type.substring(0, posOf);
				}
				// Intermediate step: make _type the index range or size
				_type = _type.trim();
				if (_type.startsWith("[") && _type.endsWith("]")) {
					// Cut the brackets off and transform a potential range
					_type = _type.substring(1, _type.length()-1).replace("..", this.transformKeyword(" TO "));
				}
				else {
					int size = this.optionDefaultArraySize();
					if (size <= 0) {
						_type ="";
					}
					_type = "" + size;
				}
				// Combine the index ranges if elemType is an array itself
				int posPar = elemType.indexOf('(');
				if (posPar > 0) {
					_type = elemType.substring(0, posPar+1)
							+ _type
							+ "," + elemType.substring(posPar + 1);
				}
				else {
					_type = elemType + "(" + _type + ")";
				}
			}
			// END KGU#993 2021-10-03
		}
		return _type;
	}
	// END KGU#1 2015-12-19	

	// START KGU#277 2016-10-13: Enh. #270
	@Override
	protected void addCode(String text, String _indent, boolean isDisabled)
	{
		if (isDisabled)
		{
			appendComment(_indent + text, "");
		}
		else
		{
			code.add(this.getLineNumber() + _indent + text);
		}
	}
	// END KGU#277 2016-10-13

	@Override
	protected void generateCode(Instruction _inst, String _indent)
	{

		if(!appendAsComment(_inst, _indent)) {
			// START KGU#277 2016-10-13: Enh. #270
			boolean disabled = _inst.isDisabled(false);
			// END KGU#277 2016-10-13
			// START KGU 2014-11-16
			appendComment(_inst, _indent);
			// END KGU 2014-11-16
			// START KGU#779 2019-12-01: Bugfix #790 (enh. #416)
			//for(int i=0; i<_inst.getText().count(); i++)
			Root root = Element.getRoot(_inst);
			StringList lines = _inst.getUnbrokenText();
			for (int i = 0; i < lines.count(); i++)
			// END KGU#779 2019-12-01
			{
				// START KGU#779 2019-12-01: Bugfix #790 (enh. #416)
				String line = lines.get(i);
				StringList tokens = Element.splitLexically(line, true);
				Element.unifyOperators(tokens, false);
				boolean done = false;
				// END KGU#779 2019-12-01
				//if (this.optionBasicLineNumbering())
				if (!this.suppressTransformation /*&& this.optionCodeLineNumbering()*/)
				// END KGU#171 2016-03-31 / KGU#779 2019-12-01
				{
					// START KGU#779 2019-12-01
					if (Instruction.isTypeDefinition(line)) {
						// In most cases this won't do anything because it's already generated
						generateTypeDef(tokens, root, _indent, disabled);
						continue;
					}
					// The crux is: we don't know the index range!
					// So we'll invent an index base variable easy to be modified in code
					//code.add(_indent + transform(line) + ";");
					int asgnPos = tokens.indexOf("<-");
					if (asgnPos >= 0)
					{
						StringList leftSide = tokens.subSequence(0, asgnPos);
						leftSide.removeAll(" ");
						String varName = Instruction.getAssignedVarname(leftSide, false);
						// START KGU#1089 2023-10-17: Issue #980 Suppress defective initialisations
						if (varName == null) {
							this.appendComment("***ILLEGAL LINE SKIPPED: " + line, _indent);
							continue;
						}
						// END KGU#1089 2023-10-17: Issue #980
						declareVariable(varName, root, _indent);
						StringList exprTokens = tokens.subSequence(asgnPos+1, tokens.count()).trim();
						// START KGU#780 2019-12-01 - trouble with complicated left sides fixed
						int posColon = leftSide.indexOf(":");
						int posAs = leftSide.indexOf("as", false);
						if (posColon > 0) {
							// FIXME #1188 extract the element type and dimensions for the DIM statement!
							leftSide = leftSide.subSequence(0, posColon);
						}
						if (posAs > 0 && posAs < leftSide.count()) {
							// FIXME #1188 extract the element type and dimensions for the DIM statement!
							leftSide = leftSide.subSequence(0, posAs);
						}
						// START KGU#1173 2025-02-06: Bugfix #1188 With a declaration, remove dimensions
						int posVar = leftSide.indexOf(varName);
						boolean isCDecl = posVar > 0;
						if (isCDecl && posVar+1 < leftSide.count() && leftSide.get(posVar+1).equals("[")) {
							leftSide.remove(posVar+1, leftSide.count());
						}
						// END KGU#1173 2025-02-06
						String target = leftSide.concatenate(null);
						varName = transform(target.substring(target.indexOf(varName)));
						// START KGU#1175 2025-02-08: EXTRACTED to generateAssignment(...)
//						boolean isArrayInit = !exprTokens.isEmpty() && exprTokens.get(0).equals("{") && exprTokens.get(exprTokens.count()-1).equals("}");
//						// END KGU#780 2019-12-01
//						if (isArrayInit)
//						{
//							generateArrayInit(varName, exprTokens, _indent, disabled);
//						}
//						StringList compList = null;
//						boolean isRecordInit = !isArrayInit && exprTokens.count() > 2 && Function.testIdentifier(exprTokens.get(0), false, null)
//								&& exprTokens.get(exprTokens.count()-1).equals("}")
//								&& !(compList = exprTokens.subSequence(1, exprTokens.count()).trim()).isEmpty()
//								&& compList.get(0).equals("{");
//						if (isRecordInit) {
//							TypeMapEntry type = root.getTypeInfo(routinePool).get(":" + exprTokens.get(0));
//							// START KGU#1175 2025-02-07: Bugfx #1190 Duplicate declaration should be avoided
//							//if (type != null && Function.testIdentifier(varName, false, null) && !this.wasDefHandled(root, varName, true)) {
//							if (type != null && Function.testIdentifier(varName, false, null) && !this.wasDefHandled(root, varName, true, true)) {
//							// END KGU#1175 2025-02-07
//								addCode(transformKeyword("DIM ") + varName + transformKeyword(" AS ") + type.typeName, _indent, disabled);
//							}
//							this.generateRecordInit(varName, exprTokens.concatenate(), _indent, disabled, type);
//						}
						generateAssignment(varName, exprTokens, root, _indent, disabled);
						done = true;
						// END KGU#1175 2025-02-08
					}
				}
				if (!done)
				{
				// END KGU#100 2016-01-22
					// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
					//code.add(this.getLineNumber() + _indent + transform(_inst.getText().get(i)));
					String codeLine = transform(line);
					if (Instruction.isTurtleizerMove(line)) {
						codeLine += " : " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
					}
					// START KGU#1177 2025-02-16: Bugfix #1192 Return instruction keywords weren't transformed
					else if (Jump.isReturn(line)) {
						codeLine = (transformKeyword("RETURN ") + transform(tokens.concatenate("", 1))).trim();
					}
					// END KGU#1177 2025-02-16
					// START KGU#993 2021-10-04: Bugfix #993 We suppress unused declarations
					//addCode(codeLine, _indent, disabled);
					if (!Instruction.isMereDeclaration(line)) {
						addCode(codeLine, _indent, disabled);
					}
					// START KGU#1175 2025-02-07: Bugfix #1190 We should not suppress within Includables
					else if (root.isInclude()) {
						StringList declNames = Instruction.getDeclaredVariables(tokens);
						for (int j = 0; j < declNames.count(); j++) {
							this.declareVariable(declNames.get(j), root, _indent);
						}
					}
					// END KGU#1175 2025-02-07
					// END KGU#993 2021-10-04
					// END KGU#277/KGU#284 2016-10-13/16
				// START KGU#100 2016-01-22: Enh. #84 (continued)
				}
				// END KGU#100 2016-01-22				
			}
		}
	}

	// START KGU#1175 2025-02-08: Bugfix #1190
	/**
	 * Generates the code for an assignment of the value represented by the token
	 * list {@code _exprTokens} and may be a complex expression (e.g. an array or
	 * record initialiser expression) to variable {@code _varName} (which might be
	 * e.g. a qualified record component path).
	 * 
	 * @param _varName - the access path for the target variable (might e.g. be a
	 *     record variable component)
	 * @param _exprTokens - the tokenized value expression (operator-unified but not
	 *     yet transformed).
	 * @param _root - the {@link Root} the instruction originates from
	 * @param _indent - the indentation string
	 * @param _disabled - whether the instruction was enabled (such that the code
	 *     will have to be "outcommented".
	 */
	private void generateAssignment(String _varName, StringList _exprTokens, Root _root, String _indent, boolean _disabled)
	{
		boolean isArrayInit = !_exprTokens.isEmpty() && _exprTokens.get(0).equals("{") && _exprTokens.get(_exprTokens.count()-1).equals("}");
		// END KGU#780 2019-12-01
		if (isArrayInit)
		{
			generateArrayInit(_varName, _exprTokens, _indent, _disabled);
		}
		StringList compList = null;
		boolean isRecordInit = !isArrayInit && _exprTokens.count() > 2 && Function.testIdentifier(_exprTokens.get(0), false, null)
				&& _exprTokens.get(_exprTokens.count()-1).equals("}")
				&& !(compList = _exprTokens.subSequence(1, _exprTokens.count()).trim()).isEmpty()
				&& compList.get(0).equals("{");
		if (isRecordInit) {
			this.generateRecordInit(_varName, _exprTokens, _root, _indent, _disabled);
		}
		if (!isArrayInit && !isRecordInit) {
			String keyword = "";
			if (this.optionCodeLineNumbering()) {
				keyword = transformKeyword("Let ");
			}
			addCode(keyword + _varName + " = " + transform(_exprTokens.concatenate(null)), _indent, _disabled);
		}
	}
	
	/**
	 * Adds code lines to fill the array {@code _targetVar} with the values
	 * extracted from the initialiser expression {@code _exprTokens}.
	 * 
	 * @param _targetVar - name (or access expression) of the target array
	 *    variable
	 * @param _exprTokens - the initialiser expression as token list
	 * @param _indent - the current indentation (as string)
	 * @param _disabled - whether the originating element is disabled (will
	 *    lead to insertion as comment)
	 */
	private void generateArrayInit(String _targetVar, StringList _exprTokens, String _indent, boolean _disabled) {
		StringList elements = Element.splitExpressionList(
				_exprTokens.concatenate(null, 1), ",");
		if (this.optionCodeLineNumbering()) {
			// In order to be consistent with possible index access
			// at other positions in code, we use the standard Java
			// index range here (though in Pascal indexing usually 
			// starts with 1 but may vary widely). We solve the problem
			// by providing a configurable start index variable 
			//appendComment("TODO: Check indexBase value (automatically generated)", _indent);
			// START KGU#277 2016-10-13: Enh. #270 | 2025-02-06 Obsolete
			//code.add(this.getLineNumber() + _indent + "LET indexBase = 0");
			//addCode(transformKeyword("LET") + " indexBase = 0", _indent, _disabled);
			// END KGU#277 2016-10-13
			for (int el = 0; el < elements.count(); el++)
			{
				// START KGU#277 2016-10-13: Enh. #270
				//code.add(this.getLineNumber() + _indent + "LET " + varName + 
				//		"(indexBase + " + el + ") = " + 
				//		transform(elements.get(el)));
				// START KGU#1175 2025-02-06: Bugfix #1190 indexBase is obsolete
				//addCode(transformKeyword("LET ") + _targetVar + "(indexBase + " + el + ") = " + 
				addCode(transformKeyword("LET ") + _targetVar + "(" + el + ") = " +
				// END KGU1175 2025-02-06
								transform(elements.get(el)), _indent, _disabled);
				// END KGU#277 2016-10-13
			}
		}
		else {
			addCode(_targetVar + " = " + 
					transform(_exprTokens.concatenate(null)), _indent, _disabled);
		}
	}

	// START KGU#388 2019-12-01: Enh. #423
	/**
	 * Generates code that decomposes a record initializer into separate component
	 * assignments if necessary or converts it into the appropriate target language.
	 * 
	 * @param _lValue - the left side of the assignment (without modifiers!)
	 * @param _recordValue - the record initializer according to Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is o be commented out
	 * @param _typeEntry - used to interpret a simplified record initializer (may be null)
	 */
	// START KGU#559 2018-07-20: Enh. #563
	//protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled) {
	//	HashMap<String, String> comps = Instruction.splitRecordInitializer(_recordValue, null);
	protected void generateRecordInit(String _lValue, StringList _exprTokens, Root _root, String _indent, boolean _isDisabled)
	{
		TypeMapEntry type = _root.getTypeInfo(routinePool).get(":" + _exprTokens.get(0));
		String recordValue = _exprTokens.concatenate();
		// START KGU#771 2019-11-24: Bugfix #783 In case of an unknown record type we should at least write the original content
		if (type == null) {
			addCode(transformKeyword("LET ") + transform(_lValue) + " = " + recordValue + "\t" + this.commentSymbolLeft() + " FIXME: missing type information for struct! " + this.commentSymbolRight(),
					_indent, false);
			return;
		}
		HashMap<String, String> comps = Instruction.splitRecordInitializer(recordValue, type, false);
		// START KGU#1021 2021-12-05: Bugfix #1024 Instruction might be defective
		if (comps == null) {
			appendComment("ERROR: defective record initializer in diagram:", _indent);
			appendComment(recordValue, _indent);
			return;
		}
		// END KGU#1021 2021-12-05
		for (Entry<String, String> comp: comps.entrySet()) {
			String compName = comp.getKey();
			String compVal = comp.getValue();
			if (!compName.startsWith("§") && compVal != null) {
				// FIXME (KGU#560) 2018-07-21: how can we fix recursive initializers? See CGenerator
				//addCode(transformKeyword("LET ") + transform(_lValue) + "." + compName + " = " + transform(compVal), _indent, _isDisabled);
				generateAssignment(transform(_lValue) + "." + compName, Element.splitLexically(compVal, _isDisabled), _root, _indent, _isDisabled);
			}
		}
	}
	// END KGU#388 2019-12-01

	// START KGU#779 2019-12-01: Issue #790 Type (record/enum) definitions hadn't been handled at all
	/**
	 * Generates a type definition (as well as possible) from the given token
	 * list, depending on the export preferences (vintage BASIC / modern BASIC).
	 * <br/>
	 * This method tends to get obsolete. It became hardly more than a wrapper
	 * for {@link #generateTypeDef(String, TypeMapEntry, Root, String, boolean)}
	 * now. 
	 * 
	 * @param _tokens - the lexically split type definition line
	 * @param _root - the owning Root
	 * @param _indent - the current indentation level
	 * @param _disabled - whether the element is disabled
	 * @return {@code false} if the type could not be found in the type map,
	 *     {@code true} otherwise (no matter whether the type definition was
	 *     generated or redundant.
	 * 
	 * @see #generateTypeDef(String, TypeMapEntry, Root, String, boolean)T
	 */
	private boolean generateTypeDef(StringList _tokens, Root _root, String _indent, boolean _disabled) {
		_tokens.removeAll(" ");
		String typeName = _tokens.get(1);
		String typeKey = ":" + typeName;
		TypeMapEntry type = _root.getTypeInfo(routinePool).get(typeKey);
		if (type != null && type.isEnum() && this.optionCodeLineNumbering()) {
			appendComment(_tokens.concatenate(null).replace("==", "="), _indent);
		}
		return generateTypeDef(typeKey, type, _root, _indent, _disabled);
	}
	// END KGU#779 2019-12-01

    @Override
    protected void generateCode(Alternative _alt, String _indent)
    {

    	String condition = transform(_alt.getUnbrokenText().getLongString()).trim();
    	String indentPlusOne = _indent + this.getIndent();

    	// START KGU 2015-11-02
    	appendComment(_alt, _indent);
    	// END KGU 2015-11-02

    	// START KGU#277 2016-10-13: Enh. #270
    	//code.add(this.getLineNumber() + _indent + "IF " + condition + " THEN");
    	boolean disabled = _alt.isDisabled(false);
    	addCode(transformKeyword("IF ") + condition + " " + transformKeyword("THEN"), _indent, disabled);
    	// END KGU#277 2016-10-13
    	generateCode(_alt.qTrue, indentPlusOne);
    	
    	// START KGU#1137 2024-03-19: Issue #1148 We ought to make use of the ELSIF if possible
    	Element ele = null;
    	// We must cater for the code mapping of the chained sub-alternatives
    	Stack<Element> processedAlts = new Stack<Element>();
    	Stack<Integer> storedLineNos = new Stack<Integer>();
    	while (_alt.qFalse.getSize() == 1 
    			&& (ele = _alt.qFalse.getElement(0)) instanceof Alternative
    			&& !this.optionCodeLineNumbering()) {
    		_alt = (Alternative)ele;
    		condition = transform(_alt.getUnbrokenText().getLongString()).trim();
    		// We must care for the code mapping explicitly here since we circumvent generateCode()
    		markElementStart(_alt, _indent, processedAlts, storedLineNos);
    		appendComment(_alt, _indent);
    		addCode(transformKeyword("ELSEIF ") + condition + " " + transformKeyword("THEN"),
    				_indent, _alt.isDisabled(false));
    		generateCode(_alt.qTrue, _indent+this.getIndent());
    	}
    	// END KGU#1137 2024-03-19
    	
    	if(_alt.qFalse.getSize() > 0)
    	{
    		// START KGU#277 2016-10-13: Enh. #270
    		//code.add(this.getLineNumber() + _indent + "ELSE");
    		addCode(transformKeyword("ELSE"), _indent, _alt.isDisabled(false));
    		// END KGU#277 2016-10-13
    		generateCode(_alt.qFalse, indentPlusOne);
    	}
    	// START KGU#277 2016-10-13: Enh. #270
    	//code.add(this.getLineNumber() + _indent + "END IF");
    	addCode(transformKeyword("END IF"), _indent, disabled);
    	// END KGU#277 2016-10-13
    	
    	// START KGU#1137 2024-03-19: Issue #1148 Accomplish the code map for the processed child alternatives
    	markElementEnds(processedAlts, storedLineNos);
    	// END KGU#1137 2024-03-19
    }

    @Override
    protected void generateCode(Case _case, String _indent)
    {
        // START KGU#453 2017-11-02: Issue #447 - consider line continuation now
        //String discriminator = transform(_case.getText().get(0));
        StringList unbrokenText = _case.getUnbrokenText();
        String discriminator = transform(unbrokenText.get(0));
        // END KGU #453 2017-11-02
        String indentPlusOne = _indent + this.getIndent();
        String indentPlusTwo = indentPlusOne + this.getIndent();

        // START KGU 2015-11-02
        appendComment(_case, _indent);
        // END KGU 2015-11-02

        // START KGU#277 2016-10-13: Enh. #270
        //code.add(this.getLineNumber() + _indent + "SELECT CASE " + selection);
        boolean disabled =_case.isDisabled(false); 
        addCode(transformKeyword("SELECT CASE ") + discriminator, _indent, disabled);
        // END KGU#277 2016-10-13

        for (int i=0; i<_case.qs.size()-1; i++)
        {
        	// START KGU#277 2016-10-13: Enh. #270
        	//code.add(this.getLineNumber() + indentPlusOne + "CASE " + _case.getText().get(i+1).trim());
        	// START KGU#453 2017-11-02: Issue #447
        	//addCode("CASE " + _case.getText().get(i+1).trim(), indentPlusOne, disabled);
        	addCode(transformKeyword("CASE ") + unbrokenText.get(i+1).trim(), indentPlusOne, disabled);
        	// END KGU#453 2017-11-02
        	// END KGU#277 2016-10-13
        	//    code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1));
        	generateCode((Subqueue) _case.qs.get(i), indentPlusTwo);
        	//    code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1));
        }

        // START KGU#453 2017-11-02: Issue #447
        //if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
        if (!unbrokenText.get(_case.qs.size()).trim().equals("%"))
        // END KGU#453 2017-11-02
        {
        	// START KGU#277 2016-10-13: Enh. #270
        	//code.add(this.getLineNumber() + indentPlusOne + "CASE ELSE");
        	addCode(transformKeyword("CASE ELSE"), indentPlusOne, disabled);
        	// END KGU#277 2016-10-13
        	generateCode((Subqueue)_case.qs.get(_case.qs.size()-1), indentPlusTwo);
        }
        // START KGU#277 2016-10-13: Enh. #270
        //code.add(this.getLineNumber() + _indent + "END SELECT");
        addCode(transformKeyword("END SELECT"), _indent, disabled);
        // END KGU#277 2016-10-13
    }

    @Override
    protected void generateCode(For _for, String _indent)
    {
    	// START KGU#3 2015-11-02: Sensible handling of FOR loops
        //code.add(_indent+"FOR "+BString.replace(transform(_for.getText().getText()),"\n","").trim()+"");
    	appendComment(_for, _indent);
    	
    	// START KGU#61 2016-03-23: Enh. 84
    	if (_for.isForInLoop() && generateForInCode(_for, _indent))
    	{
    		// All done
    		return;
    	}
    	// END KGU#61 2016-03-23

    	String[] parts = _for.splitForClause();
    	String increment = "";
    	if (!parts[3].trim().equals("1")) increment = transformKeyword(" STEP ") + parts[3];
    	// START KGU#277 2016-10-13: Enh. #270
    	//code.add(this.getLineNumber() + _indent + "FOR " +
    	//		parts[0] + " = " + transform(parts[1], false) +
    	//		" TO " + transform(parts[2], false) + increment);
    	boolean disabled = _for.isDisabled(false);
    	addCode(transformKeyword("FOR ") + parts[0] + " = " + transform(parts[1], false) +
    			transformKeyword(" TO ") + transform(parts[2], false) + increment, _indent, disabled);
    	// END KGU#277 2016-10-13
    	// END KGU 2015-11-02
    	generateCode(_for.getBody(), _indent + this.getIndent());
    	// START KGU#277 2016-10-13: Enh. #270
    	//code.add(this.getLineNumber() + _indent + "NEXT " + parts[0]);
    	addCode(transformKeyword("NEXT ") + parts[0], _indent, disabled);
    	// END KGU#277 2016-10-13
    	
    	// START KGU#78 2015-12-18: Enh. #23
    	this.placeJumpTarget(_for, _indent);
    	// END KGU#78 2915-12-18
    }

	// START KGU#61 2016-03-23: Enh. #84 - Support for FOR-IN loops
	/**
	 * We try our very best to create a working loop from a FOR-IN construct
	 * This will only work, however, if we can get reliable information about
	 * the size of the value list, which won't be the case if we obtain it e.g.
	 * via a variable.
	 * (Here, we will just apply Visual Basic syntax until someone complains.)
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
		// START KGU#277 2016-10-13: Enh. #270
		boolean disabled = _for.isDisabled(false);
		// END KGU#277 2016-10-13
		// START KGU#171 2016-03-31: Enh. #144
		//if (items != null)
		if (!this.suppressTransformation && items != null)
		// END KGU#171 2016-03-31
		{
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogenous? We will just try four types: boolean,
			// integer, real and string, where we can only test literals.
			// If none of them match then we add a TODO comment.
			int nItems = items.count();
			boolean allBoolean = true;
			boolean allInt = true;
			boolean allReal = true;
			boolean allString = true;
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
				if (allBoolean)
				{
					if (!item.equalsIgnoreCase("true") && !item.equalsIgnoreCase("false"))
					{
						allBoolean = false;
					}
				}
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
				if (allReal)
				{
					try {
						Double.parseDouble(item);
					}
					catch (NumberFormatException ex)
					{
						allReal = false;
					}
				}
				if (allString)
				{
					allString = item.startsWith("\"") && item.endsWith("\"") &&
							!item.substring(1, item.length()-1).contains("\"");
				}
				// START KGU#368 2017-03-15: Bugfix #382
				items.set(i, transform(item));
				// END KGU#368 2017-03-15
			}
			
			// Create some generic and unique variable names
			String postfix = Integer.toHexString(_for.hashCode());
			String arrayName = "array" + postfix;
			//String indexName = "index" + postfix;

			String itemType = "";
			if (allBoolean) itemType = "Boolean";
			else if (allInt) itemType = "Integer";
			else if (allReal) itemType = "Real";
			else if (allString) itemType = "String";
			else {
				itemType = "FIXME_" + postfix;
				// We do a dummy type definition
				this.appendComment("TODO: Specify an appropriate element type for the array!", _indent);
			}

			// Insert the array declaration and initialisation
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(this.getLineNumber() + _indent + "DIM " + arrayName + "() AS " + itemType + " = {" + 
			//		items.concatenate(", ") + "}");
			addCode(transformKeyword("DIM ") + arrayName + "() " + transformKeyword("AS ") + itemType + " = {" + 
					items.concatenate(", ") + "}", _indent, disabled);
			// END KGU#277 2016-10-13
			valueList = arrayName;
		}
			
		// Creation of the loop header
		// START KGU#277 2016-10-13: Enh. #270
		//code.add(this.getLineNumber() + _indent + "FOR EACH " + var + " IN " + valueList);
		addCode(transformKeyword("FOR EACH ") + var + transformKeyword(" IN ") + valueList, _indent, disabled);
		// END KGU#277 2016-10-13

		// Creation of the loop body
    	generateCode(_for.getBody(), _indent + this.getIndent());
		// START KGU#277 2016-10-13: Enh. #270
    	//code.add(this.getLineNumber() + _indent + "NEXT " + var);
    	addCode(transformKeyword("NEXT ") + var, _indent, disabled);
		// END KGU#277 2016-10-13
    	
		this.placeJumpTarget(_for, _indent);	// Enh. #23: Takes care for correct jumps

		done = true;
		return done;
	}
	// END KGU#61 2016-03-23

    @Override
    protected void generateCode(While _while, String _indent)
    {
            // START KGU#779 2019-12-01: Bugfix #790 (enh. #416)
            //String condition = transform(_while.getText().getLongString(), false).trim();
            String condition = transform(_while.getUnbrokenText().getLongString(), false).trim();
            // END KGU#779 2019-12-01

            // START KGU 2015-11-02
            appendComment(_while, _indent);
            // END KGU 2015-11-02

            // The traditional BASIC while loop looks like WHILE condition ... WEND
            // START KGU#2772 2016-10-13: Enh. #270
            //code.add(this.getLineNumber() + _indent + "DO WHILE " + condition);
            boolean disabled = _while.isDisabled(false);
            addCode(transformKeyword("DO WHILE ") + condition, _indent, disabled);
            // END KGU#277 2016-10-13
            generateCode(_while.getBody(), _indent+this.getIndent());
            // START KGU#2772 2016-10-13: Enh. #270
            //code.add(this.getLineNumber() + _indent + "LOOP");
            addCode(transformKeyword("LOOP"), _indent, disabled);
            // END KGU#277 2016-10-13

            // START KGU#78 2015-12-18: Enh. #23
            this.placeJumpTarget(_while, _indent);
            // END KGU#78 2915-12-18
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent)
    {
            // START KGU#779 2019-12-01: Bugfix #790 (enh. #416)
            //String condition = transform(_repeat.getText().getLongString()).trim();
            String condition = transform(_repeat.getUnbrokenText().getLongString(), false).trim();
            // END KGU#779 2019-12-01

            // START KGU 2015-11-02
            appendComment(_repeat, _indent);
            // END KGU 2015-11-02

            // START KGU#277 2016-10-13: Enh. #270
            //code.add(this.getLineNumber() + _indent + "DO");
            boolean disabled = _repeat.isDisabled(false);
            addCode(transformKeyword("DO"), _indent, disabled);
            // END KGU#277 2016-10-13
            generateCode(_repeat.getBody(), _indent + this.getIndent());
            // START KGU#277 2016-10-13: Enh. #270
            //code.add(this.getLineNumber() + _indent + "LOOP UNTIL " + condition);
            addCode(transformKeyword("LOOP UNTIL ") + condition, _indent, disabled);
            // END KGU#277 2016-10-13

            // START KGU#78 2015-12-18: Enh. #23
            this.placeJumpTarget(_repeat, _indent);
            // END KGU#78 2915-12-18
    }

    @Override
    protected void generateCode(Forever _forever, String _indent)
    {
        // START KGU 2015-11-02
        appendComment(_forever, _indent);
        // END KGU 2015-11-02

        // START KGU#277 2016-10-13: Enh. #270
        //code.add(this.getLineNumber() + _indent + "DO");
        boolean disabled = _forever.isDisabled(false);
        addCode(transformKeyword("DO"), _indent, disabled);
        // END KGU#277 2016-10-13
        generateCode(_forever.getBody(), _indent+this.getIndent());
        // START KGU#277 2016-10-13: Enh. #270
        //code.add(this.getLineNumber() + _indent + "LOOP");
        addCode(transformKeyword("LOOP"), _indent, disabled);
        // END KGU#277 2016-10-13

        // START KGU#78 2015-12-18: Enh. #23
        this.placeJumpTarget(_forever, _indent);
        // END KGU#78 2915-12-18
    }
	
	@Override
	protected void generateCode(Call _call, String _indent)
	{
		if(!appendAsComment(_call, _indent)) {
			// START KGU 2014-11-16
			appendComment(_call, _indent);
			// END KGU 2014-11-16
			Root owningRoot = Element.getRoot(_call);
			StringList lines = _call.getUnbrokenText();
			for (int i=0; i < lines.count(); i++)
			{
				// START KGU#2 2015-12-18: Enh. #9 This may require a CALL command prefix
				//code.add(_indent+transform(_call.getText().get(i)));
				// START KGU#371 2019-03-08: Enh. #385 Support for declared optional arguments
				//String line = transform(_call.getText().get(i));
				String line = lines.get(i);
				boolean isAsgnmt = Instruction.isAssignment(line);
				if (i == 0 && this.getOverloadingLevel() == OverloadingLevel.OL_NO_OVERLOADING && (routinePool != null)
						&& line.endsWith(")")) {
					Function call = _call.getCalledRoutine();
					// START KGU#877 2020-10-16: Bugfix #874 name extraction may fail (e.g. non-ASCII letters)
					if (call != null) {
					// END KGU#877 2020-10-16
						java.util.Vector<Root> callCandidates = 
								routinePool.findRoutinesBySignature(call.getName(), call.paramCount(), owningRoot, false);
						if (!callCandidates.isEmpty()) {
							// FIXME We'll just fetch the very first one for now...
							Root called = callCandidates.get(0);
							StringList defaults = called.getParameterDefaults();
							if (defaults.count() > call.paramCount()) {
								// We just insert the list of default values for the missing arguments
								line = line.substring(0, line.length()-1) + (call.paramCount() > 0 ? ", " : "") + 
										defaults.subSequence(call.paramCount(), defaults.count()).concatenate(", ") + ")";
							}
						}
					// START KGU#877 2020-10-16: Bugfix #874 name extraction may fail (e.g. non-ASCII letters)
					}
					// END KGU#877 2020-10-16
				}
				// END KGU#371 2019-03-08
				// START KGU#778 2019-12-01: Bugfix #789 - The detection of function calls was corrupted by patch #385
				//if (!line.startsWith("LET") || line.indexOf(" = ") < 0)
				if (!isAsgnmt)
				// END KGU#778 2019-12-01
				{
					line = transformKeyword("CALL ") + line;
				}
				// START KGU#277 2016-10-13: Enh. #270
				//code.add(this.getLineNumber() + _indent + line);
				addCode(transform(line), _indent, _call.isDisabled(false));
				// END KGU#277 2016-10-13
				// END KGU#2 2015-12-18
			}
		}
	}

	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		if(!appendAsComment(_jump, _indent)) {
			// START #277 2016-10-13: Enh. #270
			boolean disabled = _jump.isDisabled(false);
			// END KGU#277 2016-10-13
			// START KGU 2014-11-16
			appendComment(_jump, _indent);
			// END KGU 2014-11-16

			// START KGU#78 2015-12-18: Enh. #23 Generate sensible goto instructions
			//for(int i=0;i<_jump.getText().count();i++)
			//{
			//	code.add(_indent+transform(_jump.getText().get(i)));
			//}
			boolean isEmpty = true;

			// START KGU#779 2019-12-01: Bugfix #790 (enh. #416)
			//StringList lines = _jump.getText();
			StringList lines = _jump.getUnbrokenText();
			// END KGU#779 2019-12-01
			String preReturn  = CodeParser.getKeywordOrDefault("preReturn", "return");
			String preThrow   = CodeParser.getKeywordOrDefault("preThrow", "throw");
			for (int i = 0; isEmpty && i < lines.count(); i++) {
				String line = transform(lines.get(i)).trim();
				if (!line.isEmpty())
				{
					isEmpty = false;
				}
				// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
				//code.add(_indent + line + ";");
				if (Jump.isReturn(line))
				{
					String argument = line.substring(preReturn.length()).trim();
					if (!argument.isEmpty())
					{
						//code.add(_indent + this.getLineNumber() + this.procName + " = " + argument + " : END");
						// START KGU#277 2016-10-13: Enh. #270
						//code.add(this.getLineNumber() + _indent + "RETURN " + argument);
						addCode(transformKeyword("RETURN ") + argument, _indent, disabled);
						// END KGU#277 2016-10-13
					}
				}
				else if (Jump.isExit(line))
				{
					// START KGU#277 2016-10-13: Enh. #270
					//code.add(this.getLineNumber() + _indent + "STOP");
					addCode(transformKeyword("STOP"), _indent, disabled);
					// END KGU#277 2016-10-13
				}
				// START KGU#686 2019-03-18: Enh. #56
				else if (Jump.isThrow(line)) {
					// START KGU#1102 2023-11-08: Bugfix #1109 Rethrow not correctly handled
					String arg = line.substring(preThrow.length()).trim();
					// END KGU#1102 2023-11-08
					if (this.optionCodeLineNumbering()) {
						appendComment("FIXME: Only a number is allowed as parameter:", _indent);
						// START KGU#1102 2023-11-08: Bugfix #1109 Rethrow not correctly handled
						//addCode("ERROR " + line.substring(preThrow.length()).trim(), _indent, disabled);
						addCode ("ERROR " + arg, _indent, disabled);
						// END KGU#1102 2023-11-08
					}
					else {
						// START KGU#1102 2023-11-08: Bugfix #1109 Rethrow not correctly handled
						//addCode("Throw New Exception(" + line.substring(preThrow.length()).trim() + ")", _indent, disabled);
						if (arg.isEmpty()) {
							// Could be a rethrow - look for a catch context
							if (Try.findEnclosingTry(_jump, true) == null) {
								arg = "New Exception(\"FIXME - missing argument!\")";
							}
						}
						else {
							arg = "New Exception(" + arg + ")";
						}
						addCode("Throw " + arg, _indent, disabled);
						// END KGU#1102 2023-11-08
					}
				}
				// END KGU#686 2019-03-18
				// Has it already been matched with a loop? Then syntax must have been okay...
				else if (this.jumpTable.containsKey(_jump))
				{
					Integer ref = this.jumpTable.get(_jump);
					String label = this.labelBaseName + ref;
					if (ref.intValue() < 0)
					{
						appendComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
						appendComment(line, _indent);
						label = "__ERROR__";
					}
					// START KGU#277 2016-10-13: Enh. #270
					//code.add(this.getLineNumber() + _indent + "GOTO " + label);
					addCode(transformKeyword("GOTO ") + label, _indent, disabled);
					// END KGU#277 2016-10-13
					isEmpty = false;	// Leave the above loop now 
				}
				else if (!isEmpty)
				{
					appendComment("FIXME: Structorizer detected the following illegal jump attempt:", _indent);
					appendComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
			if (isEmpty && this.jumpTable.containsKey(_jump))
			{
				Integer ref = this.jumpTable.get(_jump);
				String label = this.labelBaseName + ref;
				if (ref.intValue() < 0)
				{
					appendComment("FIXME: Structorizer detected illegal jump attempt here!", _indent);
					label = "__ERROR__";
				}
				// START KGU#277 2016-10-13: Enh. #270
				//code.add(this.getLineNumber() + _indent + "GOTO " + label);
				addCode(transformKeyword("GOTO ") + label, _indent, disabled);
				// END KGU#277 2016-10-13
				isEmpty = false;	// Leave the above loop now 
			}
			// END KGU#78 2015-12-18
		}
	}

	// START KGU#47 2015-12-18: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		String indentPlusOne = _indent + this.getIndent();
		appendComment(_para, _indent);

		// START KGU#277 2016-10-13: Enh. #270
		//code.add(this.getLineNumber());
		boolean disabled = _para.isDisabled(false);
		addCode("", "", disabled);
		// END KGU#277 2016-10-13
		appendComment("==========================================================", _indent);
		appendComment("================= START PARALLEL SECTION =================", _indent);
		appendComment("==========================================================", _indent);
		appendComment("TODO: add the necessary code to run the threads concurrently", _indent);
		//code.add(this.getLineNumber() + _indent + "PARALLEL");

		for (int i = 0; i < _para.qs.size(); i++) {
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(this.getLineNumber());
			addCode("", "", disabled);
			// END KGU#277 2016-10-13
			appendComment("----------------- START THREAD " + i + " -----------------", indentPlusOne);
			//code.add(this.getLineNumber() + indentPlusOne + "THREAD");
			generateCode((Subqueue) _para.qs.get(i), indentPlusOne + this.getIndent());
			//code.add(this.getLineNumber() + indentPlusOne + "END THREAD");
			appendComment("------------------ END THREAD " + i + " ------------------", indentPlusOne);
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(this.getLineNumber());
			addCode("", "", disabled);
			// END KGU#277 2016-10-13
		}

		//code.add(this.getLineNumber() + _indent + "END PARALLEL");
		appendComment("==========================================================", _indent);
		appendComment("================== END PARALLEL SECTION ==================", _indent);
		appendComment("==========================================================", _indent);
		// START KGU#277 2016-10-13: Enh. #270
		//code.add(this.getLineNumber());
		addCode("", "", disabled);
		// END KGU#277 2016-10-13
	}
	// END KGU#47 2015-12-18

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateCode(lu.fisch.structorizer.elements.Try, java.lang.String)
	 */
	@Override
	protected void generateCode(Try _try, String _indent)
	{

		boolean isDisabled = _try.isDisabled(false);
		appendComment(_try, _indent);

		String indent1 = _indent + this.getIndent();
		String varName = _try.getExceptionVarName();
		boolean supportsTry = this.getTryCatchLevel() != TryCatchSupportLevel.TC_NO_TRY;
		if (supportsTry) {
			// Modern code (~ VisualBasic)
			this.addCode("Try", _indent, isDisabled);

			generateCode(_try.qTry, indent1);

			if (varName != null && !varName.isEmpty()) {
				String exName = "ex" + Integer.toHexString(_try.hashCode());
				this.addCode("Catch " + exName + " As Exception", _indent, isDisabled);
				this.addCode("Dim " + varName + " As String = " + exName + ".ToString()", indent1, isDisabled);
			}
			else {
				this.addCode("Catch", _indent, isDisabled);
			}

			generateCode(_try.qCatch, indent1);

			if (_try.qFinally.getSize() > 0) {
				this.addCode("Finally", _indent, isDisabled);

				generateCode(_try.qFinally, indent1);
			}
			
			this.addCode("End Try", _indent, isDisabled);
		}
		else {
			// Vintage Basic

			// There is at least the ON ERROR GOTO construct...
			int ixOnGoto = this.code.count();
			this.addCode("ON ERROR GOTO §§§", _indent, isDisabled);

			generateCode(_try.qTry, _indent);

			int ixGotoFinal = this.code.count();
			this.addCode("GOTO §§§", _indent, isDisabled);
			this.appendComment("Start of error handler, FIXME: variable '" + varName + "' should conatain error info ...", _indent);
			this.code.set(ixOnGoto, this.code.get(ixOnGoto).replace("§§§", Integer.toString(this.lineNumber)));
		
			generateCode(_try.qCatch, indent1);
		
			this.appendComment("End of error handler, resume here ...", _indent);
			this.code.set(ixGotoFinal, this.code.get(ixGotoFinal).replace("§§§", Integer.toString(this.lineNumber)));
		
			this.addCode("ON ERROR GOTO 0", _indent, isDisabled);
			if (_try.qFinally.getSize() > 0) {

				generateCode(_try.qFinally, _indent);

			}
		}
	}


// START KGU#74 2015-12-18: Decomposed and fine-tuned 
	/**
	 * Composes the heading for the program or function according to the
	 * syntactic rules of the target language and adds it to this.code.
	 * @param _root - The diagram root element
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType, boolean _public)
	{
		String furtherIndent = _indent;
		this.labelMap = new int[this.labelCount];
		String pr = this.commentSymbolLeft() + " program";
		this.procName = _procName;	// Needed for value return mechanisms

		// START KGU#178 206-07-20: Enh. #160 - option to involve called subroutines
		//insertComment(_root, _indent);
		//insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
		if (topLevel)
		{
			appendComment(_root, _indent);
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16

			// START KGU#1175 2025-02-07: Bugfix #1190 Includables had been forgotten
			if (_root.isSubroutine()) {
				appendIncludeCode(_indent);
			}
			// END KGU#1175 2025-02-07
			
			subroutineInsertionLine = code.count();	// (this will be revised in line numbering mode)
			appendComment("", _indent);
		}
		else
		{
			appendComment("", _indent);
			appendComment(_root, _indent);
			// START KGU#815 2020-03-26: Enh. #828
			if (_public) {
				appendCopyright(_root, _indent, false);
			}
			// END KGU#815 2020-03-26
		}
		// END KGU#178 2016-07-20

		String signature = _root.getMethodName();
		// START KGU#815 2020-04-08: Enh. #828
		//if (_root.isSubroutine()) {
		if (_root.isSubroutine() || this.isLibraryModule() && topLevel && _root.isInclude()) {
		// END KGU#815 2020-04-08
			boolean isFunction = _resultType != null || this.returns || this.isResultSet || this.isFunctionNameSet; 
			pr = transformKeyword(isFunction ? "FUNCTION" : "SUB");
			
			// START KGU#815 2020-04-08: Enh. #828 - group export
			if (this.isLibraryModule() && _public && !this.optionCodeLineNumbering()) {
				pr = "Public " + pr;
			}
			// END KGU#815 2020-04-08

			// Compose the function header
			signature += "(";
			if (this.optionCodeLineNumbering())
			{
				appendComment("TODO: Add type-specific suffixes where necessary!", _indent);
			}
			else
			{
				appendComment("TODO: Check (and specify if needed) the argument and result types!", _indent);        		
			}
			// START KGU#371 2019-03-08: Enh. #385 Deal with optional arguments#
			int minArgs = _root.getMinParameterCount();
			StringList argDefaults = _root.getParameterDefaults();
			// END KGU#371 2019-03-08
			for (int p = 0; p < _paramNames.count(); p++) {
				signature += ((p > 0) ? ", " : "");
				// START KGU#993 2021-10-03: Bugfix #993 Wrong handling of const and array parameters
				String pType = null;
				if (_paramTypes != null) {
					pType = _paramTypes.get(p);
					if (pType != null && pType.startsWith("const ")) {
						signature += transformKeyword("CONST ");
						pType = pType.substring("const ".length());
					}
				}
				// END KGU#993 2021-10-03
				// START KGU#1175 2025-02-08: Bugfix #1190 We better register params as declared
				//signature += (_paramNames.get(p)).trim();
				String paramName = _paramNames.get(p).trim();
				signature += paramName;
				this.wasDefHandled(_root, paramName, true);
				// END KGU#2025-02-08
				if (_paramTypes != null)
				{
					// START KGU#993 2021-10-03: Bugfix #993 Wrong handling of const and array parameters
					//String type = this.transformType(_paramTypes.get(p), "");
					String type = "";
					if (pType != null) {
						type = this.transformType(pType, "");
					}
					// END KGU#993 2021-10-03
					if (!type.isEmpty())
					{
						signature += transformKeyword(" AS ") + type;
					}
				}
				// START KGU#371 2019-03-08: Enh. #385 Deal with optional arguments#
				if (p >= minArgs && this.getOverloadingLevel() == OverloadingLevel.OL_DEFAULT_ARGUMENTS) {
					signature += " = " + transform(argDefaults.get(p));
				}
				// END KGU#371 2019-03-08
			}
			signature += ")";
			if (_resultType != null)
			{
				signature += transformKeyword(" AS ") + transformType(_resultType, "Real");
			}
			// START KGU#1175 2025-02-07: Bugfix #1190
			//furtherIndent += this.getIndent();
			if (_root.isSubroutine()) {
				furtherIndent += this.getIndent();
			}
			// END KGU#1175 2025-02-07
		}

		code.add(this.getLineNumber() + _indent + pr + " " + signature);

		return furtherIndent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		// Old BASIC dialects with line numbers usually don't support declarations
		if (!this.optionCodeLineNumbering())
		{
			appendComment("TODO: Check and accomplish your variable declarations here:", _indent );
		// START KGU#1175 2025-02-06: Bugfix #1188
		}
		else
		{
			appendComment("TODO: add the respective type suffixes to your variable names if required", _indent );
		}
		// END KGU##1175 2025-02-06
		// START KGU#1175 2025-02-07: Bugfix #1190 Includables had been forgotten
		if (topLevel) {
			// With a top-level subroutine, it will have been done in the header
			if (!_root.isSubroutine()) {
				appendIncludeCode(_indent);
			}
		}
		else {
			if (code.count() == subroutineInsertionLine) {
				appendComment("", _indent);	// Make sure the subroutines are inserted before library entry points
			}
			libraryInsertionLine = code.count();
			appendComment("", _indent);
		}
		// END KGU#1175 2025-02-07
// START KGU#1173/KGU#1175 2025-02-07: Bugfix #1188, #1190 Disabled
//		for (int v = 0; v < _varNames.count(); v++) {
//			// START KGU#542 2019-12-01: Enh. #739 Don't do this for enumeration constants here
//			//appendComment(transformKeyword("DIM ") + _varNames.get(v) + transformKeyword(" AS") + " <type>", indentPlusOne);
//			declareVariable(_varNames.get(v), _root, _indent);
//			// END KGU#542 2019-12-01
//		}
		// First: generate the constants
		for (Entry<String, String> entry: _root.constants.entrySet()) {
			// Constant routine parameters will usually have value null
			// KGU#542 2019-12-01: Enh. #739 Don't do this for enumeration constants here
			if (entry.getValue() != null && !entry.getValue().startsWith(":")) {
				declareVariable(entry.getKey(), _root, _indent);
			}
		}
		// Second: generate the type definitions
		for (Entry<String, TypeMapEntry> entry: _root.getTypeInfo(routinePool).entrySet()) {
			String typeKey = entry.getKey();
			if (typeKey.startsWith(":")) {
				this.generateTypeDef(typeKey, entry.getValue(), _root, _indent, false);
			}
		}
		// Third: generate the remaining variable declarations
		for (int v = 0; v < _varNames.count(); v++) {
			String varName = _varNames.get(v);
			if (_root.constants.get(varName) == null) {
				declareVariable(varName, _root, _indent);
			}
		}
// END KGU#1173/KGU#1175 2025-02-07
		appendComment("", _indent);
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
			code.add(this.getLineNumber() + _indent + transformKeyword("RETURN ") + result);
		}
		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateFooter(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		String endPhrase = "END";
		if (_root.isSubroutine())
		{
			if (_root.getResultType() != null || this.returns || this.isResultSet || this.isFunctionNameSet)
			{
				endPhrase += " FUNCTION";
			}
			else
			{
				endPhrase += " SUB";
			}
		}
		code.add(_indent + this.getLineNumber() + transformKeyword(endPhrase));
		
		if (this.optionCodeLineNumbering())
		{
			// Okay now, in line numbering mode, we will have to replace the generic labels by line numbers
			for (int i = 0; i < code.count(); i++)
			{
				String line = code.get(i);
				int labelPos = line.indexOf(this.labelBaseName);
				if (labelPos >= 0)
				{
					// Supposed to be a jump instruction:
					// Identify the label number, look for the corresponding line number and replace the label by the latter
					int posLN = labelPos + this.labelBaseName.length();		// position of the label number
					String labelNoStr = line.substring(posLN);
					int labelNo = Integer.parseInt(labelNoStr);
					int lineNo = this.labelMap[labelNo];
					code.set(i, line.replace(this.labelBaseName + labelNoStr, Integer.toString(lineNo)));
				}
			}
		}
		// FIXME: We will have to find a way to renumber code lines!
		// In line numbering mode we may not insert the subroutines further up because this would break the number ordering
		if (topLevel)
		{
			subroutineInsertionLine = code.count();
			// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
			libraryInsertionLine = code.count();
			// END KGU#815/KGU#824 2020-03-19
		}
	}
	// END KGU#74 2015-12-18
	
	// START KGU#1175 2025-02-07: Bugfix #1190 We must care for the includables
	/**
	 * Appends the generated code for all included stuff in topological order
	 * Intended to be used at top level before the subroutine header or before
	 * all local declarations occur. Likely to add at least one separation line
	 * if the include queue isn't entirely empty.
	 * 
	 * @param _indent - current indentation level
	 */
	private void appendIncludeCode(String _indent) {
		Root[] includes = new Root[] {};
		includes = this.includedRoots.toArray(includes);
		if (includes.length > 0) {
			addSepaLine();
		}
		int nLines = this.code.count();
		// Enh. #389 - code of includes is to be produced here
		for (Root incl: includes) {
			generateCode(incl.children, _indent);
		}
		if (code.count() > nLines) {
			addSepaLine();
		}
	}
	// END KGU#1175 2025-02-07

	// START KGU#1173/KGU#1175 2025-02-06: Bugfix #1188, #1190 extracted from preamble
	/**
	 * Appends a variable declaration for variable {@code _varName} if possible.
	 * 
	 * @param _varName - name of the variable to be declared
	 * @param _root - the owning {@link Root}
	 * @param _indent - th current indentation
	 */
	private void declareVariable(String _varName, Root _root, String _indent) {
		String constVal = null;
		if (((constVal = _root.constants.get(_varName)) == null || !constVal.startsWith(":"))
				&& !this.wasDefHandled(_root, _varName, true, true)) {
			TypeMapEntry type = _root.getTypeInfo().get(_varName);
			String typeName = "???";
			if (type != null) {
				StringList typeNames = this.getTransformedTypes(type, true);
				if (typeNames.count() == 1) {
					typeName = typeNames.get(0);
				}
				// START KGU#1175 2025-02-06: Bugfix #1188 We should add sizes if possible
				int level = 0;
				// END KGU#1175 2025-02-06
				while (typeName.startsWith("@")) {
					// START KGU#1175 2025-02-06: Bugfix #1188 produce sensible type info
					//varName += "()";
					int maxIx = type.getMaxIndex(level++);
					_varName += "(";
					if (maxIx >= 0) {
						_varName += maxIx;
					}
					_varName += ")";
					// END KGU#1175 2025-02-06
					typeName = typeName.substring(1);
				}
				// START KGU#993 2021-10-04: Issue #993
				_varName = _varName.replace(")(", ",");
				// END KGU#993 2021-10-04
			}
			// START KGU#1175 2025-02-06: Bugfix #1188 Deoending on mode
			//addCode(transformKeyword("DIM ") + varName + transformKeyword(" AS ") + typeName, _indent, false);
			if (!this.optionCodeLineNumbering() || type != null && (type.isArray() || type.isRecord())) {
				addCode(transformKeyword("DIM ") + _varName
						+ transformKeyword(" AS ") + typeName, _indent, false);
			}
			// END KGU#1175 2025-02-06
		}
	}
	// END KGU#1173/KGU#1175 2025-02-06

	// START KGU#1175 2025-02-07: Bugfix #1190 We must place type definitions in the preamble when variables refer to them
	/**
	 * Generates a BASIC type definition for the type with name {@code _typeName}
	 * represented by {@code _type} Enumeration
	 *  
	 * @param _typeKey - type map key of the type (name preceded by ':')
	 * @param _type - the {@link TypeMapEntry} for the type
	 * @param _root - the owning (defining) {@link Root}
	 * @param _indent - the current indentation
	 * @param _isDisabled - whether the originating element is disabled
	 * @return {@code false} if the type could not be found in the type map,
	 *     {@code true} otherwise (no matter whether the type definition was
	 *     generated or redundant.
	 */
	private boolean generateTypeDef(String _typeKey, TypeMapEntry _type, Root _root, String _indent, boolean _isDisabled) {
		String indentPlus1 = _indent + this.getIndent();
		if (_type == null) {
			return false;
		} else if (wasDefHandled(_root, _typeKey, false, true)) {
			return true;
		}
		String typeName = _typeKey.substring(1);
		if (_type.isRecord()) {
			Map<String, TypeMapEntry> compSpecs = _type.getComponentInfo(true);
			// START KGU#1176 2025-02-07: Bugfix #1190 We need a definition for modern Basic too
			//if (this.optionCodeLineNumbering()) {
			// END KGU#1176 2025-02-07
			// We apply a QBASIC construct (see https://en.wikibooks.org/wiki/QBasic/Arrays_and_Types)
			addCode(transformKeyword("TYPE") + " " + typeName, _indent, _isDisabled);
			for (Entry<String, TypeMapEntry> entry: compSpecs.entrySet()) {
				String compKeyword = "";
				if (!this.optionCodeLineNumbering()) {
					compKeyword = "Dim ";
				}
				String compTypeName = "???";
				String varSpec = entry.getKey();
				TypeMapEntry compType = entry.getValue();
				if (compType != null) {
					compTypeName = entry.getValue().getCanonicalType(true, true);
					StringList dims = new StringList();
					while (compTypeName.startsWith("@")) {
						int maxIndex = compType.getMaxIndex(dims.count());
						if (maxIndex < 0) {
							dims.add("");
						}
						else {
							dims.add(Integer.toString(maxIndex));
						}
						compTypeName = compTypeName.substring(1);
					}
					if (!dims.isEmpty()) {
						varSpec += "(" + dims.concatenate(", ") + ")";
						compKeyword = "Dim ";
					}
				}
				// START KGU#1176 2025-02-07: Bugfix #1190 We need a definition for modern Basic too
				//addCode(entry.getKey() + transformKeyword(" AS ") + transformType(compTypeName, "???"), indentPlus1, _disabled);
				addCode(compKeyword + varSpec + transformKeyword(" AS ") +
						transformType(compTypeName, "???"), indentPlus1, _isDisabled);
				// END KGU#1176 2025-02-07
			}
			addCode(transformKeyword("END ") + transformKeyword("TYPE"), _indent, _isDisabled);
			// START KGU#1176 2025-02-07: Bugfix #1190 We need a definition for modern Basic too
			//}
			// END KGU#1176 2025-02-07
		}
		else if (_type.isEnum()) {
			StringList enumItems = _type.getEnumerationInfo();
			if (this.optionCodeLineNumbering()) {
				if (!_isDisabled) {
					this.appendComment("enum " + typeName + "(" + enumItems.concatenate(", ") + ")", _indent);
				}
				// In vintage BASIC, we will just generate separate constant definitions
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
					addCode(transformKeyword("CONST ") + itemSpec[0] + " = " + transform(lastVal) + (lastVal.isEmpty() ? "" : "+") + offset, _indent, _isDisabled);
					offset++;
				}
				appendComment("end type "+ typeName, _indent);
			}
			else {
				// We use a VisualBasic feature here (see https://docs.microsoft.com/de-de/dotnet/visual-basic/language-reference/statements/enum-statement)
				addCode(transformKeyword("ENUM ") + typeName, _indent, _isDisabled);
				for (int i = 0; i < enumItems.count(); i++) {
					addCode(transform(enumItems.get(i)), indentPlus1, _isDisabled);
				}
				addCode(transformKeyword("END ENUM"), _indent, _isDisabled);
			}
		}
		this.setDefHandled(_root.getSignatureString(false, false), _typeKey);
		return true;
	}

}

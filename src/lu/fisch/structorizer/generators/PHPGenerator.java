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
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Executor;
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
		return "include '%';";
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/

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
		if (this.usesFileAPI) {
			for (int i = 0; i < Executor.fileAPI_names.length; i++) {
				tokens.replaceAll(Executor.fileAPI_names[i], "StructorizerFileAPI::" + Executor.fileAPI_names[i]);
			}
		}
		// END KGU#311 2017-01-03
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21
	
	// END KGU#18/KGU#23 2015-11-01

    @Override
    protected void generateCode(Instruction _inst, String _indent)
    {
    	// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
		if (!appendAsComment(_inst, _indent)) {
			
			boolean isDisabled = _inst.isDisabled();
			
			appendComment(_inst, _indent);

			StringList lines = _inst.getUnbrokenText();
			for (int i=0; i<lines.count(); i++)
			{
				// START KGU#281 2016-10-16: Enh. #271
				//addCode(transform(_inst.getText().get(i))+";",
				//		_indent, isDisabled);
				// START KGU#653 2019-02-14: Enh. #680 - support fr multi-var input
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
				else {
				// END KGU#653 2019-02-14
					String transf = transform(line) + ";";
					if (transf.startsWith("= $_REQUEST[")) {
						transf = "dummyInputVar " + transf;
					}
					// START KGU#284 2016-10-16: Enh. #274
					else if (Instruction.isTurtleizerMove(lines.get(i))) {
						transf += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
					}
					// END KGU#284 2016-10-16
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
    	boolean isDisabled = _alt.isDisabled();
    	
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
    	boolean isDisabled = _case.isDisabled();
    	
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
    		StringList constants = StringList.explode(lines.get(i+1), ",");
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
    	boolean isDisabled = _for.isDisabled();
    	
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
//    		String increment = "$" + var + " += (" + step + ")";
//    		code.add(_indent + "for ($" +
//    				var + " = " + transform(_for.getStartValue(), false) + "; $" +
//    				var + compOp + transform(_for.getEndValue(), false) + "; " +
//    				increment +
//    				")");
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
    	boolean isDisabled = _while.isDisabled();

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
        boolean isDisabled = _repeat.isDisabled();
        
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
        boolean isDisabled = _forever.isDisabled();
        
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
    	boolean isDisabled = _call.isDisabled();
    	
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
		boolean isDisabled = _jump.isDisabled();
		
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
		boolean isDisabled = _try.isDisabled();
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
    public String generateCode(Root _root, String _indent)
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
            // START KGU#351 2017-02-26: Enh. #346
            this.appendUserIncludes("");
            // END KGU#351 2017-02-26
            subroutineInsertionLine = code.count();
            // START KGU#311 2017-01-03: Enh. #314 File API support
            if (this.usesFileAPI) {
                this.insertFileAPI("php");
            }
            // END KGU#311 2017-01-03
        }
        code.add("");
        if (!topLevel || !subroutines.isEmpty())
        {
            appendComment(pr + " " + procName, _indent);
        }
        // END KGU#178 2016-07-20
        // START KGU 2014-11-16
        appendComment(_root, "");
        // END KGU 2014-11-16
        if (_root.isProgram() == true)
        {
            code.add("");
            appendComment("TODO declare your variables here if necessary", _indent);
            code.add("");
            appendComment("TODO Establish sensible web formulars to get the $_GET input working.", _indent);
            code.add("");
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
            appendComment("TODO declare your variables here if necessary", _indent + this.getIndent());
            code.add(_indent+"");
            appendComment("TODO Establish sensible web formulars to get the $_GET input working.", _indent + this.getIndent());
            code.add("");
            generateCode(_root.children, _indent + this.getIndent());
            // START KGU#74/KGU#78 2016-12-30: Issues #22/#23: Return mechanisms hadn't been fixed here until now
            if (!this.suppressTransformation) {
            	this.generateResult(_root, _indent + this.getIndent(), alwaysReturns, varNames);
            }
            // END KGU#74/KGU#78 2016-12-30
            code.add("}");
        }

        // START KGU#178 2016-07-20: Enh. #160
        //code.add("?>");
        if (topLevel)
        {
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
			code.add(_indent);
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}
	// END KGU#74/KGU#78 2016-12-30

}

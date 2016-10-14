/*
    This file is part of Structorizer.

    Structorizer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structorizer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 ***********************************************************************

    BASH Source Code Generator

    Copyright (C) 2008 Markus Grundner

    This file has been released under the terms of the GNU Lesser General
    Public License as published by the Free Software Foundation.

    http://www.gnu.org/licenses/lgpl.html

 */

package lu.fisch.structorizer.generators;

/*
 ******************************************************************************************************
 *
 *      Author:         Markus Grundner
 *
 *      Description:    BASH Source Code Generator
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author				Date			Description
 *      ------				----			-----------
 *      Markus Grundner     2008.06.01		First Issue based on KSHGenerator from Jan Peter Kippel
 *      Bob Fisch           2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig         2014.11.16      Bugfixes in operator conversion and enhancements (see comments)
 *      Kay Gürtzig         2015.10.18      Indentation logic and comment insertion revised
 *                                          generateCode(For, String) and generateCode(Root, String) modified
 *      Kay Gürtzig         2015.11.02      transform methods re-organised (KGU#18/KGU#23) using subclassing,
 *                                          Pattern list syntax in Case Elements corrected (KGU#15).
 *                                          Bugfix KGU#60 (Repeat loop was incorrectly translated).
 *      Kay Gürtzig         2015.12.19      Enh. #23 (KGU#78): Jump translation implemented
 *      Kay Gürtzig         2015.12.21      Bugfix #41/#68/#69 (= KGU#93): String literals were spoiled
 *      Kay Gürtzig         2015.12.22      Bugfix #71 (= KGU#114): Text transformation didn't work
 *      Kay Gürtzig         2016.01.08      Bugfix #96 (= KGU#129): Variable names handled properly,
 *                                          KGU#132: Logical expressions (conditions) put into ((  )).
 *      Kay Gürtzig         2016.03.22      Enh. #84/#135 (= KGU#61): Support for FOR-IN loops
 *      Kay Gürtzig         2016.03.24      Bugfix #92/#135 (= KGU#161) Input variables were prefixed
 *      Kay Gürtzig         2016.03.29      KGU#164: Bugfix #138 Function call expression revised (in transformTokens())
 *                                          #135 Array and expression support improved (with thanks to R. Schmidt)
 *      Kay Gürtzig         2016.03.31      Enh. #144 - content conversion may be switched off
 *      Kay Gürtzig         2016.04.05      Enh. #153 - Export of Parallel elements had been missing
 *      Kay Gürtzig         2016.04.05      KGU#150 - provisional support for chr and ord function
 *      Kay Gürtzig         2016.07.20      Enh. #160: Option to involve subroutines implemented (=KGU#178) 
 *      Kay Gürtzig         2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (identifier collisions)
 *      Kay Gürtzig         2016.09.01      Issue #234: ord and chr function code generated only if needed and allowed
 *      Kay Gürtzig         2016.09.21      Bugfix #247: Forever loops were exported with a defective condition.
 *      Kay Gürtzig         2016.10.14      Enh. 270: Handling of disabled elements (code.add(...) --> addCode(..))
 *
 ******************************************************************************************************
 *
 *      Comment:		LGPL license (http://www.gnu.org/licenses/lgpl.html).
 *      
 *      2016.04.05 - Enhancement #153 (Kay Gürtzig / Rolf Schmidt)
 *      - Parallel elements hat just been ignored by previous versions Now an easy way could be
 *        implemented. It's working rather well, provided that the commands within the
 *        branches are convertible. Delivered with version 3.24-06. 
 *      
 *      2016.03.21/22 - Enhancement #84/#135 (Kay Gürtzig / Rolf Schmidt)
 *      - Besides the working (but rather rarely used) C-like "three-expression" FOR loop, FOR-IN loops
 *        were to be enabled in a consistent way, i.e. the syntax must also be accepted by Editors and
 *        Executor as well as other code generators.
 *      - This generator copes with value lists of the following types:
 *        {item1, item2, item3} --> item1 item2 item3
 *        item1, item2, item3 -->   item1 item2 item3
 *        {val1..val2} and {val1..val2..step} would be left as is but not explicitly created
 *      
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-02 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops now provide themselves more reliable loop parameters  
 *      - Case enabled to combine several constants/patterns in one branch (KGU#15)
 *      - The Repeat loop had been implememed in an incorrect way  
 *      
 *      2015.10.18 - Bugfixes (KGU#53, KGU#30)
 *      - Conversion of functions improved by producing headers according to BASH syntax
 *      - Conversion of For loops slightly improved (not robust, may still fail with complex expressions as loop parameters
 *      
 *      2014.11.16 - Bugfixes / Enhancement
 *      - conversion of Pascal-like logical operators "and", "or", and "not" supported 
 *      - conversion of comparison and operators accomplished
 *      - comment export introduced 
 *
 ******************************************************************************************************
 */


import java.util.regex.Matcher;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;


public class BASHGenerator extends Generator {
	
	// START KGU#61 2016-03-22: Now provided by Generator class
	// Bugfix #96 (KGU#129, 2015-01-08): We must know all variable names to prefix them with '$'.
	//StringList varNames = new StringList();
	// END KGU#6 2016-03-22

	/************ Fields ***********************/
	@Override
	protected String getDialogTitle()
	{
		return "Export BASH Code ...";
	}
	
	@Override
	protected String getFileDescription()
	{
		return "BASH Source Code";
	}
	
	@Override
	protected String getIndent()
	{
		return " ";
	}
	
	@Override
	protected String[] getFileExtensions()
	{
		String[] exts = {"sh"};
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
		return false;
	}
	// END KGU#78 2015-12-18
	
	// START KGU#241 2016-09-01: Issue #234: names of certain occurring functions detected by checkElementInformation()
	private StringList occurringFunctions = new StringList();
	// END KGU#241 2015-09-01

	// START KGU 2016-08-12: Enh. #231 - information for analyser
    private static final String[] reservedWords = new String[]{
		"if", "then", "else", "elif", "fi",
		"select", "case", "in", "esac",
		"for", "do", "done",
		"while", "until",
		"function", "return"};
	public String[] getReservedWords()
	{
		return reservedWords;
	}
	public boolean isCaseSignificant()
	{
		return true;
	}
	// END KGU 2016-08-12
	
	/************ Code Generation **************/
	
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected String getInputReplacer()
	{
		return "read $1";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "echo $1";
	}

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by "="
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	protected String transformAssignment(String _interm)
//	{
//		return _interm.replace(" <- ", "=");
//	}

	// START KGU#150/KGU#241 2016-09-01: Issue #234 - smarter handling of ord and chr functions
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#checkElementInformation(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	protected boolean checkElementInformation(Element _ele)
	{
		StringList tokens = Element.splitLexically(_ele.getText().getText(), true);
		String[] functionNames = {"ord", "chr"};
		for (int i = 0; i < functionNames.length && !occurringFunctions.contains(functionNames[i]); i++)
		{
			int pos = -1;
			while ((pos = tokens.indexOf(functionNames[i], pos+1)) >= 0 &&
					pos+1 < tokens.count() &&
					tokens.get(pos+1).equals("("))
			{
				occurringFunctions.add(functionNames[i]);
				break;	
			}
		}
		
		return super.checkElementInformation(_ele);
	}
	// END KGU#150/KGU#241 2016-09-01

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#129 2016-01-08: Bugfix #96 - variable name processing
		// We must of course identify variable names and prefix them with $ unless being an lvalue
		int posAsgnOpr = tokens.indexOf("<-");
		// START KGU#161 2016-03-24: Bugfix #135/#92 - variables in read instructions must not be prefixed!
		if (tokens.contains(D7Parser.keywordMap.get("input")))
		{
			// Hide the text from the replacement, except for occurrences as index
			posAsgnOpr = tokens.count();
		}
		// END KGU#161 2016-03-24
		// START KGU#61 2016-03-21: Enh. #84/#135
		if (posAsgnOpr < 0 && !D7Parser.keywordMap.get("postForIn").trim().isEmpty()) posAsgnOpr = tokens.indexOf(D7Parser.keywordMap.get("postForIn"));
		// END KGU#61 2016-03-21
		// If there is an array variable (which doesn't exist in shell) left of the assignment symbol, check the index 
		int posBracket1 = tokens.indexOf("[");
		int posBracket2 = -1;
		if (posBracket1 >= 0 && posBracket1 < posAsgnOpr) posBracket2 = tokens.lastIndexOf("]", posAsgnOpr-1);
    	for (int i = 0; i < varNames.count(); i++)
    	{
    		String varName = varNames.get(i);
    		//System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
    		//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
    		transformVariableAccess(varName, tokens, posAsgnOpr+1, tokens.count());
    		transformVariableAccess(varName, tokens, posBracket1+1, posBracket2+1);
    	}

		// Now we remove spaces around the assignment operator
    	posAsgnOpr = tokens.indexOf("<-");
    	if (posAsgnOpr >= 0)
    	{
    		int pos = posAsgnOpr - 1;
    		// Eliminate blanks preceding the assignment operator
    		while (pos >= 0 && tokens.get(pos).equals(" "))
    		{
    			tokens.delete(pos--);
    			posAsgnOpr--;
    		}
    		// Eliminate blanks following the assignment operator
    		pos = posAsgnOpr + 1;
    		while (pos < tokens.count() && tokens.get(pos).equals(" "))
    		{
    			tokens.delete(pos);
    		}
    	}
		// END KGU#96 2016-01-08
		// FIXME (KGU): Function calls, math expressions etc. will have to be put into brackets etc. pp.
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		tokens.replaceAllCi("false", "0");	// FIXME: Is this correct?
		tokens.replaceAllCi("true", "1");	// FIXME: Is this correct?
		// START KGU#131 2015-01-08: Prepared for old-style test expressions, but disabled again
//		tokens.replaceAll("<", " -lt ");
//		tokens.replaceAll(">", " -gt ");
//		tokens.replaceAll("==", " -eq ");
//		tokens.replaceAll("!=", " -ne ");
//		tokens.replaceAll("<=", " -le ");
//		tokens.replaceAll(">=", " -ge ");
		// END KGU#131 2016-01-08
		// START KGU#164 2016-03-29: Bugfix #138 - function calls weren't handled
		//return tokens.concatenate();
		String lval = "";
		if (posAsgnOpr > 0)
		{
			// Separate lval and assignment operator from the expression tokens
			lval += tokens.concatenate("", 0, posAsgnOpr + 1);
			tokens = tokens.subSequence(posAsgnOpr+1, tokens.count());
		}
		else if (tokens.count() > 0)
		{
			String[] keywords = D7Parser.getAllProperties();
			boolean startsWithKeyword = false;
			String firstToken = tokens.get(0);
			for (int kwi = 0; !startsWithKeyword && kwi < keywords.length; kwi++)
			{
				if (firstToken.equals(keywords[kwi]))
				{
					lval = firstToken + " ";
					tokens.delete(0);
					startsWithKeyword = true;
				}
			}
		}
		// Re-combine the rval expression to a string 
		String expr = tokens.concatenate().trim();
		// If the expression is a function call, then convert it to shell syntax
		// (i.e. drop the parentheses and dissolve the argument list)
		Function fct = new Function(expr);
		if (fct.isFunction())
		{
			expr = fct.getName();
			for (int p = 0; p < fct.paramCount(); p++)
			{
				String param = fct.getParam(p);
				if (param.matches("(.*?)(-|[+*/%])(.*?)"))
				{
					param = "$(( " + param + " ))";
				}
				else if (param.contains(" "))
				{
					param = "\"" + param + "\"";
				}
				expr += (" " + param);
			}
			if (posAsgnOpr > 0)
			{
				expr = "`" + expr + "`";
			}
		}
		else if (expr.startsWith("{") && expr.endsWith("}") && posAsgnOpr > 0)
		{
			lval = "declare -a " + lval;
			StringList items = Element.splitExpressionList(expr.substring(1, expr.length()-1), ",");
			expr = "(" + items.getLongString() + ")";
		}
		// The following is a very rough and vague heuristics to support arithmetic expressions 
		else if ( !(expr.startsWith("(") && expr.endsWith(")")
				|| expr.startsWith("`") && expr.endsWith("`")
				|| expr.startsWith("'") && expr.endsWith("'")
				|| expr.startsWith("\"") && expr.endsWith("\"")
				|| expr.startsWith("[[") && expr.endsWith("]]")))
		{
			if (expr.matches("(.*?)(-|[+*/%])(.*?)"))
			{
				expr = "(( " + expr + " ))";
				if (posAsgnOpr > 0)
				{
					expr = "$" + expr;
				}
			}
			// START KGU 2016-03-31: Issue #135+#144 - quoting wasn't actually helpful
//			else if (expr.contains(" "))
//			{
//				expr = "\"" + expr + "\"";
//			}
			// END KGU 2016-03-31
		}
		return lval + expr;
		// END KGU#164 2016-03-29
	}
	// END KGU#93 2015-12-21

	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#167 2016-03-30: Enh. #135 Array support
	protected void transformVariableAccess(String _varName, StringList _tokens, int _start, int _end)
	{
		int pos = _start-1;
		while ((pos = _tokens.indexOf(_varName, pos+1)) >= 0 && pos < _end)
		{
			int posNext = pos+1;
			while (posNext < _end && _tokens.get(posNext).trim().isEmpty()) posNext++;
			if (_tokens.get(posNext).equals("["))
			{
				_tokens.set(pos, "${" + _varName);
				// index brackets follow, so remove the blanks
				for (int i = 0; i < posNext - pos-1; i++)
				{
					_tokens.delete(pos+1);
					_end--;
				}
				// find the corresponding closing bracket
				int depth = 1;
				for (posNext = pos+2; depth > 0 && posNext < _end; posNext++)
				{
					String token = _tokens.get(posNext);
					if (token.equals("["))
					{
						depth++;
					}
					else if (token.equals("]"))
					{
						if (--depth <= 0)
						{
							_tokens.set(posNext, "]}");
						}
					}
				}
			}
			else
			{
				_tokens.set(pos, "${" + _varName + "}");
			}
		}
	}

	// END KGU#167 2016-03-30

	// START KGU#101 2015-12-22: Enh. #54 - handling of multiple expressions
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	@Override
	protected String transformOutput(String _interm)
	{
		String output = D7Parser.keywordMap.get("output").trim();
		if (_interm.matches("^" + output + "[ ](.*?)"))
		{
			StringList expressions = 
					Element.splitExpressionList(_interm.substring(output.length()), ",");
			expressions.removeAll(" ");
			_interm = output + " " + expressions.getLongString();
		}
		
		String transformed = super.transformOutput(_interm);
		if (transformed.startsWith("print , "))
		{
			transformed = transformed.replace("print , ", "print ");
		}
		return transformed;
	}
	// END KGU#101 2015-12-22
	
	// START KGU#18/KGU#23 2015-11-02: Most of the stuff became obsolete by subclassing
	protected String transform(String _input)
	{
		String intermed = super.transform(_input);
		
		// START KGU#162 2016-03-31: Enh. #144
		if (!this.suppressTransformation)
		{
		// END KGU#162 2016-03-31
		
			// START KGU 2014-11-16 Support for Pascal-style operators		
			intermed = BString.replace(intermed, " div ", " / ");
			// END KGU 2014-11-06

			// START KGU#78 2015-12-19: Enh. #23: We only have to ensure the correct keywords
			String preLeave = D7Parser.keywordMap.getOrDefault("preLeave","").trim();
			String preReturn = D7Parser.keywordMap.getOrDefault("preReturn","").trim();
			String preExit = D7Parser.keywordMap.getOrDefault("preExit","").trim();
			if (intermed.matches("^" + Matcher.quoteReplacement(preLeave) + "(\\W.*|$)"))
			{
				intermed = "break " + intermed.substring(preLeave.length());
			}
			else if (intermed.matches("^" + Matcher.quoteReplacement(preReturn) + "(\\W.*|$)"))
			{
				intermed = "return " + intermed.substring(preReturn.length());
			}
			else if (intermed.matches("^" + Matcher.quoteReplacement(preExit) + "(\\W.*|$)"))
			{
				intermed = "exit " + intermed.substring(preExit.length());
			} 
			// END KGU#78 2015-12-19
			
		// START KGU#162 2016-03-31: Enh. #144
		}
		// END KGU#162 2016-03-31
		

        // START KGU#114 2015-12-22: Bugfix #71
        //return _input.trim();
        return intermed.trim();
        // END KGU#114 2015-12-22
	}
	
	protected void generateCode(Instruction _inst, String _indent) {
		
		if(!insertAsComment(_inst, _indent)) {
			// START KGU 2014-11-16
			insertComment(_inst, _indent);
			boolean disabled = _inst.isDisabled();
			// END KGU 2014-11-16
			for(int i=0; i<_inst.getText().count(); i++)
			{
				// START KGU#277 2016-10-13: Enh. #270
				//code.add(_indent + transform(_inst.getText().get(i)));
				addCode(transform(_inst.getText().get(i)), _indent, disabled);
				// END KGU#277 2016-10-13
			}
		}

	}

	protected void generateCode(Alternative _alt, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_alt, _indent);
		// END KGU 2014-11-16
		// START KGU#132 2016-01-08: Bugfix #96 - approach with C-like syntax
		//code.add(_indent+"if "+BString.replace(transform(_alt.getText().getText()),"\n","").trim());
		// START KGU#132 2016-03-24: Bugfix #96/#135 second approach with [[ ]] instead of (( ))
		//code.add(_indent+"if (( "+BString.replace(transform(_alt.getText().getText()),"\n","").trim() + " ))");
		String condition = transform(_alt.getText().getLongString()).trim();
		if (!this.suppressTransformation && !(condition.startsWith("((") && condition.endsWith("))")))
		{
			condition = "[[ " + condition + " ]]";
		}
		// START KGU#277 2016-10-13: Enh. #270
		//code.add(_indent + "if " + condition);
		boolean disabled = _alt.isDisabled(); 
		addCode("if " + condition, _indent, disabled);
		// END KGU#277 2016-10-13
		// END KGU#132 2016-03-24
		// END KGU#131 2016-01-08
		// START KGU#277 2016-10-13: Enh. #270
		//code.add(_indent+"then");
		addCode("then", _indent, disabled);
		// END KGU#277 2016-10-13
		generateCode(_alt.qTrue,_indent+this.getIndent());
		
		if(_alt.qFalse.getSize()!=0) {
			
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(_indent+"");
			//code.add(_indent+"else");			
			addCode("", _indent, disabled);
			addCode("else", _indent, disabled);			
			// END KGU#277 2016-10-13
			generateCode(_alt.qFalse,_indent+this.getIndent());
			
		}
		
		// START KGU#277 2016-10-13: Enh. #270
		//code.add(_indent+"fi");
		//code.add("");
		addCode("fi", _indent, disabled);
		addCode("", "", disabled);
		// END KGU#277 2016-10-13
		
	}
	
	protected void generateCode(Case _case, String _indent) {
		
		boolean disabled = _case.isDisabled();
		code.add("");
		// START KGU 2014-11-16
		insertComment(_case, _indent);
		// END KGU 2014-11-16
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent+"case "+transform(_case.getText().get(0))+" in");
		addCode("case "+transform(_case.getText().get(0))+" in", _indent, disabled);
		// END KGU#277 2016-10-14
		
		for(int i=0;i<_case.qs.size()-1;i++)
		{
			// START KGU#277 2016-10-14: Enh. #270
			//code.add("");
			//code.add(_indent + this.getIndent() + _case.getText().get(i+1).trim().replace(",", "|") + ")");
			addCode("", "", disabled);
			addCode(this.getIndent() + _case.getText().get(i+1).trim().replace(",", "|") + ")", _indent, disabled);
			// END KGU#277 2016-10-14
			// START KGU#15 2015-11-02
			generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent()+this.getIndent()+this.getIndent());
			code.add(_indent+this.getIndent()+";;");
		}
		
		if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		{
			code.add("");
			code.add(_indent+this.getIndent()+"*)");
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
			code.add(_indent+this.getIndent()+";;");
		}
		code.add(_indent+"esac");
		code.add("");
		
	}
	
	
	protected void generateCode(For _for, String _indent) {

		code.add("");
		// START KGU 2014-11-16
		insertComment(_for, _indent);
		// END KGU 2014-11-16
		// START KGU#277 2016-10-13: Enh. #270
		boolean disabled = _for.isDisabled(); 
		// END KGU#277 2016-10-13
		// START KGU#30 2015-10-18: This resulted in nonsense if the algorithm was a real counting loop
		// We now use C-like syntax  for ((var = sval; var < eval; var=var+incr)) ...
		// START KGU#3 2015-11-02: And now we have a competent splitting mechanism...
		String counterStr = _for.getCounterVar();
		//START KGU#61 2016-03-21: Enh. #84/#135 - FOR-IN support
		if (_for.isForInLoop())
		{
			String valueList = _for.getValueList();
			if (!this.suppressTransformation)
			{
				StringList items = null;
				// Convert an array initializer to a space-separated sequence
				if (valueList.startsWith("{") && valueList.endsWith("}") &&
						!valueList.contains(".."))	// Preserve ranges like {3..18} or {1..200..2}
				{
					items = Element.splitExpressionList(
							valueList.substring(1, valueList.length()-1), ",");
				}
				// Convert a comma-separated list to a space-separated sequence
				else if (valueList.contains(","))
				{
					items = Element.splitExpressionList(valueList, ",");				
				}
				if (items != null)
				{
					valueList = transform(items.getLongString());
				}
				else if (varNames.contains(valueList))
				{
					// Must be an array variable
					valueList = "${" + valueList + "[@]}";
				}
				else
				{
					valueList = transform(valueList);
				}
			}
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(_indent + "for " + counterStr + " in " + valueList);
			addCode("for " + counterStr + " in " + valueList, _indent, disabled);
			// END KGU#277 2016-10-13
		}
		else // traditional COUNTER loop
		{
		// END KGU#61 2016-03-21
			// START KGU#129 2016-01-08: Bugfix #96: Expressions must be transformed
			//String startValueStr = _for.getStartValue();
			//String endValueStr = _for.getEndValue();
			String startValueStr = transform(_for.getStartValue());
			String endValueStr = transform(_for.getEndValue());
			// END KGU#129 2016-01-08
			int stepValue = _for.getStepConst();
			String incrStr = counterStr + "++";
			if (stepValue == -1) {
				incrStr = counterStr + "--";
			}
			else if (stepValue != 1) {
				// START KGU#129 2016-01-08: Bugfix #96 - prefix variables
				incrStr = "(( " + counterStr + "=$" + counterStr + "+(" + stepValue + ") ))";
			}
			// END KGU#3 2015-11-02
			// START KGU#277 2016-10-13: Enh. #270
			//code.add(_indent+"for (( "+counterStr+"="+startValueStr+"; "+
			//		counterStr + ((stepValue > 0) ? "<=" : ">=") + endValueStr + "; " +
			//		incrStr + " ))");
			addCode("for (( "+counterStr+"="+startValueStr+"; "+
					counterStr + ((stepValue > 0) ? "<=" : ">=") + endValueStr + "; " +
					incrStr + " ))", _indent, disabled);
			// END KGU#277 2016-10-13
			// END KGU#30 2015-10-18
		// START KGU#61 2016-03-21: Enh. #84/#135 (continued)
		}
		// END KGU#61 2016-03-21
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent+"do");
		//generateCode(_for.q,_indent+this.getIndent());
		//code.add(_indent+"done");	
		//code.add("");
		addCode("do", _indent, disabled);
		generateCode(_for.q,_indent+this.getIndent());
		addCode("done", _indent, disabled);	
		addCode("", "", disabled);
		// END KGU#277 2016-10-14

	}
	protected void generateCode(While _while, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_while, _indent);
		// END KGU 2014-11-16
		// START KGU#277 2016-10-14: Enh. #270
		boolean disabled = _while.isDisabled();
		// END KGU#277 2016-10-14
		// START KGU#132 2016-01-08: Bugfix #96 first approach with C-like syntax (( ))
		//code.add(_indent+"while " + transform(_while.getText().getLongString()));
		// START KGU#132 2016-03-24: Bugfix #96/#135 second approach with [[ ]] instead of (( ))
		//code.add(_indent+"while (( " + transform(_while.getText().getLongString()) + " ))");
		// START KGU#132/KGU#162 2016-03-31: Bugfix #96 + Enh. #144
		//code.add(_indent+"while [[ " + transform(_while.getText().getLongString()).trim() + " ]]");
		String condition = transform(_while.getText().getLongString()).trim();
		if (!this.suppressTransformation && !(condition.startsWith("((") && condition.endsWith("))")))
		{
			condition = "[[ " + condition + " ]]";
		}
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent + "while " + condition);
		addCode("while " + condition, _indent, disabled);
		// END KGU#277 2016-10-14
		// END KGU#132/KGU#144 2016-03-31
		// END KGU#132 2016-03-24
		// END KGU#132 2016-01-08
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent+"do");
		//generateCode(_while.q,_indent+this.getIndent());
		//code.add(_indent+"done");
		//code.add("");
		addCode("do", _indent, disabled);
		generateCode(_while.q,_indent+this.getIndent());
		addCode("done", _indent, disabled);
		addCode("", "", disabled);
		// END KGU#277 2016-10-14
		
	}
	
	protected void generateCode(Repeat _repeat, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_repeat, _indent);
		// END KGU 2014-11-16
		// START KGU#277 2016-10-14: Enh. #270
		boolean disabled = _repeat.isDisabled();
		// END KGU#277 2016-10-14
		// START KGU#60 2015-11-02: The do-until loop is not equivalent to a Repeat element: We must
		// generate the loop body twice to preserve semantics!
		insertComment("NOTE: This is an automatically inserted copy of the loop body below.", _indent);
		generateCode(_repeat.q, _indent);		
		// END KGU#60 2015-11-02
		// START KGU#131 2016-01-08: Bugfix #96 first approach with C-like syntax
		//code.add(_indent + "until " + transform(_repeat.getText().getLongString()).trim());
		// START KGU#132 2016-03-24: Bugfix #96/#135 second approach with [[ ]] instead of (( ))
		//code.add(_indent + "until (( " + transform(_repeat.getText().getLongString()).trim() + " ))");
		// START KGU#132/KGU#162 2016-03-31: Bugfix #96 + Enh. #144
		//code.add(_indent + "until [[ " + transform(_repeat.getText().getLongString()).trim() + " ]]");
		String condition = transform(_repeat.getText().getLongString()).trim();
		if (!this.suppressTransformation && !(condition.startsWith("((") && condition.endsWith("))")))
		{
			condition = "[[ " + condition + " ]]";
		}
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent + "while " + condition);
		addCode("while " + condition, _indent, disabled);
		// END KGU#277 2016-10-14
		// END KGU#132/KGU#144 2016-03-31
		// END KGU#132 2016-03-24
		// END KGU#131 2016-01-08
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent + "do");
		//generateCode(_repeat.q, _indent + this.getIndent());
		//code.add(_indent + "done");
		//code.add("");
		addCode("do", _indent, disabled);
		generateCode(_repeat.q, _indent + this.getIndent());
		addCode("done", _indent, disabled);
		addCode("", "", disabled);
		// END KGU#277 2016-10-14
		
	}
	protected void generateCode(Forever _forever, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_forever, _indent);
		// END KGU 2014-11-16
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent + "while [ 1 ]");
		//code.add(_indent + "do");
		//generateCode(_forever.q, _indent + this.getIndent());
		//code.add(_indent + "done");
		//code.add("");
		boolean disabled = _forever.isDisabled();
		addCode("while [ 1 ]", _indent, disabled);
		addCode("do", _indent, disabled);
		generateCode(_forever.q, _indent + this.getIndent());
		addCode("done", _indent, disabled);
		addCode("", "", disabled);
		// END KGU#277 2016-10-14
		
	}
	
	protected void generateCode(Call _call, String _indent) {
		if(!insertAsComment(_call, _indent)) {
			// START KGU 2014-11-16
			insertComment(_call, _indent);
			// END KGU 2014-11-16
			// START KGU#277 2016-10-14: Enh. #270
			boolean disabled = _call.isDisabled();
			// END KGU#277 2016-10-14
			for(int i=0;i<_call.getText().count();i++)
			{
				// START KGU#277 2016-10-14: Enh. #270
				//code.add(_indent+transform(_call.getText().get(i)));
				addCode(transform(_call.getText().get(i)), _indent, disabled);
				// END KGU#277 2016-10-14
			}
		}
	}
	
	protected void generateCode(Jump _jump, String _indent) {
		if(!insertAsComment(_jump, _indent)) {
			// START KGU 2014-11-16
			insertComment(_jump, _indent);
			// END KGU 2014-11-16
			// START KGU#277 2016-10-14: Enh. #270
			boolean disabled = _jump.isDisabled();
			// END KGU#277 2016-10-14
			for(int i=0;i<_jump.getText().count();i++)
			{
				// FIXME (KGU 2016-03-25): Handle the kinds of exiting jumps!
				// START KGU#277 2016-10-14: Enh. #270
				//code.add(_indent+transform(_jump.getText().get(i)));
				addCode(transform(_jump.getText().get(i)), _indent, disabled);
				// END KGU#277 2016-10-14
			}
		}
	}
	
	// START KGU#174 2016-04-05: Issue #153 - export had been missing
	protected void generateCode(Parallel _para, String _indent)
	{
		// START KGU#277 2016-10-14: Enh. #270
		boolean disabled = _para.isDisabled();
		// END KGU#277 2016-10-14
		insertComment(_para, _indent);
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		String indent1 = _indent + this.getIndent();
		String varName = "pids" + Integer.toHexString(_para.hashCode());
		// START KGU#277 2016-10-14: Enh. #270
		//code.add(_indent + varName + "=\"\"");
		addCode(varName + "=\"\"", _indent , disabled);
		// END KGU#277 2016-10-14
		for (Subqueue q : _para.qs)
		{
			// START KGU#277 2016-10-14: Enh. #270
			//code.add(_indent + "(");
			//generateCode(q, indent1);
			//code.add(_indent + ") &");
			//code.add(_indent + varName + "=\"${" + varName + "} $!\"");
			addCode("(", _indent, disabled);
			generateCode(q, indent1);
			addCode(") &", _indent, disabled);
			addCode(varName + "=\"${" + varName + "} $!\"", _indent, disabled);
			// END KGU#277 2016-10-14
		}
		// START KGU#277 2016-10-14: Enh. #270
		addCode("wait ${" + varName + "}", _indent, disabled);
		// END KGU#277 2016-10-14
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
	}
	// END KGU#174 2016-04-05

	// TODO: Decompose this - Result mechanism is missing!
	public String generateCode(Root _root, String _indent) {
		
		if (topLevel)
		{
			code.add("#!/bin/bash");
		}
		code.add("");

		// START KGU 2014-11-16
		insertComment(_root, _indent);
		// END KGU 2014-11-16
		String indent = _indent;
		if (topLevel)
		{
			insertComment("(generated by Structorizer " + Element.E_VERSION + ")", indent);

			// START KGU#150 2016-04-05: Provisional support for chr and ord functions
			// START KGU#241 2016-09-01: Issue #234 - Mechanism to introduce these definitions on demand only
//			code.add(indent);
//			insertComment("chr() - converts decimal value to its ASCII character representation", indent);
//			code.add(indent + "chr() {");
//			code.add(indent + this.getIndent() + "printf \\\\$(printf '%03o' $1)");
//			code.add(indent + "}");
//			insertComment("ord() - converts ASCII character to its decimal value", indent);
//			code.add(indent + "ord() {");
//			code.add(indent + this.getIndent() + "printf '%d' \"'$1\"");
//			code.add(indent + "}");
//			code.add(indent);
			if (!this.suppressTransformation)
			{
				boolean builtInAdded = false;
				if (occurringFunctions.contains("chr"))
				{
					code.add(indent);
					insertComment("chr() - converts decimal value to its ASCII character representation", indent);
					code.add(indent + "chr() {");
					code.add(indent + this.getIndent() + "printf \\\\$(printf '%03o' $1)");
					code.add(indent + "}");
					builtInAdded = true;
				}
				if (occurringFunctions.contains("ord"))
				{
					code.add(indent);
					insertComment("ord() - converts ASCII character to its decimal value", indent);
					code.add(indent + "ord() {");
					code.add(indent + this.getIndent() + "printf '%d' \"'$1\"");
					code.add(indent + "}");
					builtInAdded = true;
				}
				if (builtInAdded) code.add(indent);
			}
			// END KGU#241 2016-09-01
			// END KGU#150 2016-04-05
			
			subroutineInsertionLine = code.count();
		}
		
		if( ! _root.isProgram ) {
			// START KGU#53 2015-10-18: Shell functions get their arguments via $1, $2 etc.
			//code.add(_root.getText().get(0)+" () {");
			String header = _root.getMethodName() + "()";
			code.add(header + " {");
			indent = indent + this.getIndent();
			StringList paraNames = _root.getParameterNames();
			for (int i = 0; i < paraNames.count(); i++)
			{
				code.add(indent + paraNames.get(i) + "=$" + (i+1));
			}
			// END KGU#53 2015-10-18
		} else {				
			code.add("");
		}
		
		code.add("");
		// START KGU#129 2016-01-08: Bugfix #96 - Now fetch all variable names from the entire diagram
		varNames = _root.getVarNames();
		insertComment("TODO: Check and revise the syntax of all expressions!", _indent);
		code.add("");
		// END KGU#129 2016-01-08
		generateCode(_root.children, indent);
		
		if( ! _root.isProgram ) {
			code.add("}");
		}
		
		return code.getText();
		
	}
	
}



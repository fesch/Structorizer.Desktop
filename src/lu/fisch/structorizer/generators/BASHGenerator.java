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

/******************************************************************************************************
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
 *      Kay Gürtzig         2015.11.02      transform methods re-organised (KGU#18/KGU23) using subclassing,
 *                                          Pattern list syntax in Case Elements corrected (KGU#15).
 *                                          Bugfix KGU#60 (Repeat loop was incorrectly translated).
 *      Kay Gürtzig         2015.12.19      Enh. #23 (KGU#78): Jump translation implemented
 *      Kay Gürtzig         2015.12.21      Bugfix #41/#68/#69 (= KG#93): String literals were spoiled
 *      Kay Gürtzig         2015.12.22      Bugfix #71 (= KG#114): Text transformation didn't work
 *      Kay Gürtzig         2016.01.08      Bugfix #96 (= KG#129): Variable names handled properly,
 *                                          Logical expressions (conditions) put into ((  )).
 *
 ******************************************************************************************************
 *
 *      Comment:		LGPL license (http://www.gnu.org/licenses/lgpl.html).
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
 ******************************************************************************************************///


import java.util.regex.Matcher;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;


public class BASHGenerator extends Generator {
	
	// START KGU#129 2016-01-08: Bugfix #96 We must know all variable names to prefix them with '$'.
	StringList varNames = new StringList();
	// END KGU#129 2015-01-08

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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#129 2016-01-08: Bugfix #96 - variable name processing
		// We must of course identify variable names and prefix them with $ unless being an lvalue
		int posAsgnOpr = tokens.indexOf("<-");
		// If there is an array variable (which doesn't exist in shell) left of the assignment symbol, check the index 
		int posBracket1 = tokens.indexOf("[");
		int posBracket2 = -1;
		if (posBracket1 >= 0 && posBracket1 < posAsgnOpr) posBracket2 = Math.min(posAsgnOpr, tokens.indexOf("]"));
    	for (int i = 0; i < varNames.count(); i++)
    	{
    		String varName = varNames.get(i);
    		//System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
    		//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
    		tokens.replaceAllBetween(varName, "$"+varName, true, posAsgnOpr+1, tokens.count());
    		tokens.replaceAllBetween(varName, "$"+varName, true, posBracket1+1, posBracket2);
    	}
    	// Now we remove spaces around the assignment operator
    	if (posAsgnOpr >= 0)
    	{
    		int pos = posAsgnOpr - 1;
    		while (pos >= 0 && tokens.get(pos).equals(" "))
    		{
    			tokens.delete(pos--);
    			posAsgnOpr--;
    		}
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
		// START KGU#131 2015-01-08: Prepared for old-style test expressions, but disabled again
//		tokens.replaceAll("<", " -lt ");
//		tokens.replaceAll(">", " -gt ");
//		tokens.replaceAll("==", " -eq ");
//		tokens.replaceAll("!=", " -ne ");
//		tokens.replaceAll("<=", " -le ");
//		tokens.replaceAll(">=", " -ge ");
		// END KGU#131 2016-01-08
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21

	// END KGU#18/KGU#23 2015-11-01

	// START KGU#101 2015-12-22: Enh. #54 - handling of multiple expressions
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	@Override
	protected String transformOutput(String _interm)
	{
		if (_interm.matches("^" + D7Parser.output.trim() + "[ ](.*?)"))
		{
			StringList expressions = 
					Element.splitExpressionList(_interm.substring(D7Parser.output.trim().length()), ",");
			expressions.removeAll(" ");
			_interm = D7Parser.output.trim() + " " + expressions.getLongString();
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
		
        // START KGU 2014-11-16 Support for Pascal-style operators		
        intermed = BString.replace(intermed, " div ", " / ");
        // END KGU 2014-11-06
        
        // START KGU#78 2015-12-19: Enh. #23: We only have to ensure the correct keywords
        if (intermed.matches("^" + Matcher.quoteReplacement(D7Parser.preLeave.trim()) + "(\\W.*|$)"))
        {
        	intermed = "break " + intermed.substring(D7Parser.preLeave.trim().length());
        }
        else if (intermed.matches("^" + Matcher.quoteReplacement(D7Parser.preReturn.trim()) + "(\\W.*|$)"))
        {
        	intermed = "return " + intermed.substring(D7Parser.preReturn.trim().length());
        }
        else if (intermed.matches("^" + Matcher.quoteReplacement(D7Parser.preExit.trim()) + "(\\W.*|$)"))
        {
        	intermed = "exit " + intermed.substring(D7Parser.preExit.trim().length());
        } 
        // END KGU#78 2015-12-19

        // START KGU#114 2015-12-22: Bugfix #71
        //return _input.trim();
        return intermed.trim();
        // END KGU#114 2015-12-22
	}
	
	protected void generateCode(Instruction _inst, String _indent) {
		
		if(!insertAsComment(_inst, _indent)) {
			// START KGU 2014-11-16
			insertComment(_inst, _indent);
			// END KGU 2014-11-16
			for(int i=0; i<_inst.getText().count(); i++)
			{
				code.add(_indent + transform(_inst.getText().get(i)));
			}
		}

	}

	protected void generateCode(Alternative _alt, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_alt, _indent);
		// END KGU 2014-11-16
		// START KGU#131 2016-01-08: Bugfix #96 - approach with C-like syntax
		//code.add(_indent+"if "+BString.replace(transform(_alt.getText().getText()),"\n","").trim());
		code.add(_indent+"if (( "+BString.replace(transform(_alt.getText().getText()),"\n","").trim() + " ))");
		// END KGU#131 2016-01-08
		code.add(_indent+"then");
		generateCode(_alt.qTrue,_indent+this.getIndent());
		
		if(_alt.qFalse.getSize()!=0) {
			
			code.add(_indent+"");
			code.add(_indent+"else");			
			generateCode(_alt.qFalse,_indent+this.getIndent());
			
		}
		
		code.add(_indent+"fi");
		code.add("");
		
	}
	
	protected void generateCode(Case _case, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_case, _indent);
		// END KGU 2014-11-16
		code.add(_indent+"case "+transform(_case.getText().get(0))+" in");
		
		for(int i=0;i<_case.qs.size()-1;i++)
		{
			code.add("");
			// START KGU#15 2015-11-02: Several patterns are to be separated by '|', not by ','
			//code.add(_indent + this.getIndent() + _case.getText().get(i+1).trim() + ")");
			code.add(_indent + this.getIndent() + _case.getText().get(i+1).trim().replace(",", "|") + ")");
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
		// START KGU#30 2015-10-18: This resulted in nonsense if the algorithm was a real counting loop
		// We now use C-like syntax  for ((var = sval; var < eval; var=var+incr)) ...
		// START KGU#3 2015-11-02: And now we have a competent splitting mechanism...
		String counterStr = _for.getCounterVar();
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
		code.add(_indent+"for (("+counterStr+"="+startValueStr+"; "+
				counterStr + ((stepValue > 0) ? "<=" : "<=") + endValueStr + "; " +
				incrStr + " ))");
		// END KGU#30 2015-10-18
		code.add(_indent+"do");
		generateCode(_for.q,_indent+this.getIndent());
		code.add(_indent+"done");	
		code.add("");

	}
	protected void generateCode(While _while, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_while, _indent);
		// END KGU 2014-11-16
		// START KGU#131 2016-01-08: Bugfix #96 first approach with C-like syntax
		//code.add(_indent+"while " + transform(_while.getText().getLongString()));
		code.add(_indent+"while (( " + transform(_while.getText().getLongString()) + " ))");
		// END KGU#131 2016-01-08
		code.add(_indent+"do");
		generateCode(_while.q,_indent+this.getIndent());
		code.add(_indent+"done");
		code.add("");
		
	}
	
	protected void generateCode(Repeat _repeat, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_repeat, _indent);
		// END KGU 2014-11-16
		// START KGU#60 2015-11-02: The do-until loop is not equivalent to a Repeat element: We must
		// generate the loop body twice to preserve semantics!
		insertComment("NOTE: This is an automatically inserted copy of the loop body below.", _indent);
		generateCode(_repeat.q, _indent);		
		// END KGU#60 2015-11-02
		// START KGU#131 2016-01-08: Bugfix #96 first approach with C-like syntax
		//code.add(_indent + "until " + transform(_repeat.getText().getLongString()).trim());
		code.add(_indent + "until (( " + transform(_repeat.getText().getLongString()).trim() + " ))");
		// END KGU#131 2016-01-08
		code.add(_indent + "do");
		generateCode(_repeat.q, _indent + this.getIndent());
		code.add(_indent + "done");
		code.add("");
		
	}
	protected void generateCode(Forever _forever, String _indent) {
		
		code.add("");
		// START KGU 2014-11-16
		insertComment(_forever, _indent);
		// END KGU 2014-11-16
		code.add(_indent + "while [1]");
		code.add(_indent + "do");
		generateCode(_forever.q, _indent + this.getIndent());
		code.add(_indent + "done");
		code.add("");
		
	}
	
	protected void generateCode(Call _call, String _indent) {
		if(!insertAsComment(_call, _indent)) {
			// START KGU 2014-11-16
			insertComment(_call, _indent);
			// END KGU 2014-11-16
			for(int i=0;i<_call.getText().count();i++)
			{
				code.add(_indent+transform(_call.getText().get(i))+";");
			}
		}
	}
	
	protected void generateCode(Jump _jump, String _indent) {
		if(!insertAsComment(_jump, _indent)) {
			// START KGU 2014-11-16
			insertComment(_jump, _indent);
			// END KGU 2014-11-16
			for(int i=0;i<_jump.getText().count();i++)
			{
				code.add(_indent+transform(_jump.getText().get(i))+";");
			}
		}
	}

	public String generateCode(Root _root, String _indent) {
		
		code.add("#!/bin/bash");
		code.add("");

		// START KGU 2014-11-16
		insertComment(_root, _indent);
		// END KGU 2014-11-16
		String indent = _indent;
		insertComment("(generated by Structorizer " + Element.E_VERSION + ")", indent);
		
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



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
 *      Author                  Date			Description
 *      ------                  ----			-----------
 *      Bob Fisch       	    2008.11.17      First Issue
 *      Gunter Schillebeeckx    2009.08.10		Bugfixes (see comment)
 *      Bob Fisch               2009.08.17      Bugfixes (see comment)
 *      Bob Fisch               2010.08-30      Different fixes asked by Kay Gürtzig and Peter Ehrlich
 *      Kay Gürtzig             2010.09.10      Bugfixes and cosmetics (see comment)
 *      Rolf Schmidt            2010.09.15      1. Release of PHPGenerator
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.11.11      Fixed some replacement flaws (see comment)
 *      Kay Gürtzig             2014.11.16      Comment generation revised (now inherited)
 *      Kay Gürtzig             2014.12.02      Additional replacement of "<--" by "<-"
 *      Kay Gürtzig             2015.10.18      Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig             2015.11.02      Variable identification added, Case and
 *                                              For mechanisms improved (KGU#15, KGU#3)
 *      Kay Gürtzig             2015.12.19      Variable prefixing revised (KGU#62) in analogy to PerlGenerator
 *      Kay Gürtzig             2015.12.21      Bugfix #41/#68/#69 (= KG#93)
 *
 ******************************************************************************************************
 *
 *      Comment:
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

import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;

// FIXME (KGU 2014-11-11): Variable names will have to be accomplished by a '$' prefix - this requires
// sound lexic preprocessing (as do a lot more of the rather lavish mechanisms here)

public class PHPGenerator extends Generator 
{
	// START KGU 2015-11-02: We must know alle variable names in order to prefix the with '$'.
	StringList varNames = new StringList();
	// END KGU 2015-11-02

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
	protected boolean supportsSimpleBreak()
	{
		return true;
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
		// This is rather nonsense but ought to help to sort this out somehow
		return "$1 = \\$_GET['$1'];	// TODO form a sensible input opportunity";
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

	// START KGU#93 2015-12-21 Bugfix #41/#68/#69
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
		// START KGU#62 2015-12-19: We must work based on a lexical analysis
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
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21
    
	// END KGU#18/KGU#23 2015-11-01

//	// START KGU 2015-11-02: Had to be converted to a member method
//    //public static String transform(String _input)
//    protected String transform(String _input)
//    {
//    	_input = super.transform(_input);
//
//    	_input=BString.replace(_input," div "," / ");
//
//    	// START KGU#62 2015-11-02: Identify and adapt variable names (revised KGU 2015-12-19)
//		StringList tokens = Element.splitLexically(_input, true);
//    	for (int i = 0; i < varNames.count(); i++)
//    	{
//    		String varName = varNames.get(i);
//    		//System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
//    		//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
//    		tokens.replaceAll(varName, "$"+varName);
//    	}
//    	_input = tokens.getText().replace("\n", "");
//    	// END KGU#62 2015-11-02
//
//    	return _input.trim();
//    }
// END KGU#93 2015-12-21

    @Override
    protected void generateCode(Instruction _inst, String _indent)
    {
    	// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
//		insertComment(_inst, _indent);
//		for(int i=0;i<_inst.getText().count();i++)
//		{
//			code.add(_indent+transform(_inst.getText().get(i))+";");
//		}
		if (!insertAsComment(_inst, _indent)) {
			
			insertComment(_inst, _indent);

			for (int i=0; i<_inst.getText().count(); i++)
			{
				code.add(_indent+transform(_inst.getText().get(i))+";");
			}

		}
		// END KGU 2015-10-18
    }

    @Override
    protected void generateCode(Alternative _alt, String _indent)
    {
		// START KGU 2014-11-16
		insertComment(_alt, _indent);
		// END KGU 2014-11-16

        String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";

        code.add(_indent+"if "+condition+"");
        code.add(_indent+"{");
        generateCode(_alt.qTrue,_indent+this.getIndent());
        if(_alt.qFalse.getSize()!=0)
        {
                code.add(_indent+"}");
                code.add(_indent+"else");
                code.add(_indent+"{");
                generateCode(_alt.qFalse,_indent+this.getIndent());
        }
        code.add(_indent+"}");
    }

    @Override
    protected void generateCode(Case _case, String _indent)
    {
		// START KGU 2014-11-16
		insertComment(_case, _indent);
		// END KGU 2014-11-16

        StringList lines = _case.getText();
        String condition = transform(_case.getText().get(0));
        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";

        code.add(_indent+"switch "+condition+" ");
        code.add(_indent+"{");

        for(int i=0;i<_case.qs.size()-1;i++)
        {
        	// START KGU#15 2015-11-02: Support for multiple constants per branch
        	//code.add(_indent+this.getIndent()+"case "+_case.getText().get(i+1).trim()+":");
        	StringList constants = StringList.explode(lines.get(i+1), ",");
        	for (int j = 0; j < constants.count(); j++)
        	{
        		code.add(_indent + this.getIndent() + "case " + constants.get(j).trim() + ":");
        	}
        	// END KGU#15 2015-11-02
        	generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent()+this.getIndent());
        	code.add(_indent+this.getIndent()+this.getIndent()+"break;");
        }

        if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
        {
                code.add(_indent+this.getIndent()+"default:");
                generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
        }
        code.add(_indent+"}");
    }

    @Override
    protected void generateCode(For _for, String _indent)
    {
		// START KGU 2014-11-16
		insertComment(_for, _indent);
		// END KGU 2014-11-16

		// START KGU#3 2015-11-02: Now we have a more reliable mechanism
    	String var = _for.getCounterVar();
    	int step = _for.getStepConst();
    	String compOp = (step > 0) ? " >= " : " <= ";
    	String increment = var + " += (" + step + ")";
    	code.add(_indent + "for (" +
    			var + " = " + transform(_for.getStartValue(), false) + "; " +
    			var + compOp + transform(_for.getEndValue(), false) + "; " +
    			increment +
    			")");
		// END KGU#3 2015-11-02
        code.add(_indent+"{");
        generateCode(_for.q,_indent+this.getIndent());
        code.add(_indent+"}");
    }

    @Override
    protected void generateCode(While _while, String _indent)
    {
		// START KGU 2014-11-16
		insertComment(_while, _indent);
		// END KGU 2014-11-16

        String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";

        code.add(_indent+"while "+condition+" ");
        code.add(_indent+"{");
        generateCode(_while.q,_indent+this.getIndent());
        code.add(_indent+"}");
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent)
    {
		// START KGU 2014-11-16
		insertComment(_repeat, _indent);
		// END KGU 2014-11-16

        code.add(_indent+"do");
        code.add(_indent+"{");
        generateCode(_repeat.q,_indent+this.getIndent());
        code.add(_indent+"} while (!("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+"));");
    }

    @Override
    protected void generateCode(Forever _forever, String _indent)
    {
		// START KGU 2014-11-16
		insertComment(_forever, _indent);
		// END KGU 2014-11-16

        code.add(_indent+"while (true)");
        code.add(_indent+"{");
        generateCode(_forever.q,_indent+this.getIndent());
        code.add(_indent+"}");
    }

    @Override
    protected void generateCode(Call _call, String _indent)
    {
		// START KGU 2014-11-16
		insertComment(_call, _indent);
		// END KGU 2014-11-16

        for(int i=0;i<_call.getText().count();i++)
        {
                code.add(_indent+transform(_call.getText().get(i))+"();");
        }
    }

    @Override
    protected void generateCode(Jump _jump, String _indent)
    {
		// START KGU 2014-11-16
		insertComment(_jump, _indent);
		// END KGU 2014-11-16

		// START KGU#78 2015-12-18: Enh. #23 - sensible exit strategy
        //for(int i=0;i<_jump.getText().count();i++)
        //{
        //        code.add(_indent+transform(_jump.getText().get(i))+";");
        //}
		// In case of an empty text generate a continue instruction by default.
		boolean isEmpty = true;
		
		StringList lines = _jump.getText();
		for (int i = 0; isEmpty && i < lines.count(); i++) {
			String line = transform(lines.get(i)).trim();
			if (!line.isEmpty())
			{
				isEmpty = false;
			}
			if (line.matches(Matcher.quoteReplacement(D7Parser.preReturn)+"([\\W].*|$)"))
			{
				code.add(_indent + "return " + line.substring(D7Parser.preReturn.length()).trim() + ";");
			}
			else if (line.matches(Matcher.quoteReplacement(D7Parser.preExit)+"([\\W].*|$)"))
			{
				code.add(_indent + "exit(" + line.substring(D7Parser.preExit.length()).trim() + ");");
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
				code.add(_indent + "goto " + label + ";");
			}
			else if (line.matches(Matcher.quoteReplacement(D7Parser.preLeave)+"([\\W].*|$)"))
			{
				// Strange case: neither matched nor rejected - how can this happen?
				// Try with an ordinary break instruction and a funny comment
				code.add(_indent + "last;\t" + this.commentSymbolLeft() + " FIXME: Dubious occurrance of 'last' instruction!");
			}
			else if (!isEmpty)
			{
				insertComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
				insertComment(line, _indent);
			}
			// END KGU#74/KGU#78 2015-11-30
		}
		if (isEmpty) {
			code.add(_indent + "last;");
		}
		// END KGU#78 2015-12-18
    }

    @Override
    public String generateCode(Root _root, String _indent)
    {
		// START KGU 2015-11-02: First of all, fetch all variable names from the entire diagram
		varNames = _root.getVarNames();
		// END KGU 2015-11-02
    	
        code.add("<?php");
        String pr = "program";
        if(_root.isProgram == false) {pr="function";}
        insertComment(pr+" "+_root.getMethodName() + " (generated by Structorizer)", _indent);
		// START KGU 2014-11-16
		insertComment(_root, "");
		// END KGU 2014-11-16
        if (_root.isProgram == true)
        {
        	code.add("");
            insertComment("TODO declare your variables here if necessary", _indent);
            code.add("");
            insertComment("TODO Establish sensible web formulars to get the $_GET input working.", _indent);
            code.add("");
            generateCode(_root.children, _indent);
        }
        else
        {
            String fnHeader = _root.getText().get(0).trim();
            if(fnHeader.indexOf('(')==-1 || !fnHeader.endsWith(")")) fnHeader=fnHeader+"()";
                code.add("function " + fnHeader);
            code.add("{");
            insertComment("TODO declare your variables here if necessary", _indent + this.getIndent());
            code.add(_indent+"");
            insertComment("TODO Establish sensible web formulars to get the $_GET input working.", _indent + this.getIndent());
            code.add("");
            generateCode(_root.children, _indent + this.getIndent());
            code.add("}");
        }

        code.add("?>");

        return code.getText();
    }


}

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

    OBERON Source Code Generator

    Copyright (C) 2008 Klaus-Peter Reimers

    This file has been released under the terms of the GNU General
    Public License as published by the Free Software Foundation.

 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Klaus-Peter Reimers
 *
 *      Description:    This class generates Oberon code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author					Date			Description
 *      ------                      		----			-----------
 *      Klaus-Peter Reimers                     2008.01.08              First Issue
 *      Bob Fisch				2008.01.08		Modified "private String transform(String _input)"
 *      Bob Fisch				2008.04.12		Added "Fields" section for generator to be used as plugin
 *      Bob Fisch				2008.08.14		Added declaration output. A comment line in the root element
 *												with a "#" is ignored. All other lines are written to the code.
 *      Bob Fisch				2011.11.07		Fixed an issue while doing replacements
 *      Kay Gürtzig				2014.11.10		Operator conversion modified (see comment)
 *      Kay Gürtzig				2014.11.16		Operator conversion corrected (see comment)
 *      Kay Gürtzig				2014.12.02		Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig				2015.10.18		Indentation issue fixed and comment generation revised
 *
 ******************************************************************************************************
 *
 *      Comment:		Based on "PasGenerator.java" from Bob Fisch
 *      
 *      2015.10.18 - Bugfix / Code revision (Kay Gürtzig)
 *      - Indentation had worked in an exponential way (duplicated every level: _indent+_indent)
 *      - Interface of comment insertion methods modified
 *      
 *      2014.11.16 - Bugfix / Enhancements
 *      - operator conversion had to be adjusted to comply with Oberon2 syntax
 *      - case structure wasn't properly exported
 *      - comment export inserted
 *
 *      2014.11.10 - Enhancement
 *      - Conversion of C-style logical operators to the Pascal-like ones added
 *      - assignment operator conversion now preserves or ensures surrounding spaces
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;

public class OberonGenerator extends Generator {
	
	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export Oberon Code ...";
	}
	
	protected String getFileDescription()
	{
		return "Oberon Source Code";
	}
	
	protected String getIndent()
	{
		return "  ";
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"Mod"};
		return exts;
	}

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "(*";
    }

    @Override
    protected String commentSymbolRight()
    {
    	return "*)";
    }
    // END KGU 2015-10-18
	
	/************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected String getInputReplacer()
	{
		return "In.TYPE($1);";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "Out.TYPE($1);";
	}

	/**
	 * Transforms assignments in the given intermediate-language code line.
	 * Replaces "<-" by "="
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed string
	 */
	protected String transformAssignment(String _interm)
	{
		return _interm.replace(" <- ", " := ");
	}
	// END KGU#18/KGU#23 2015-11-01
    
	protected String transform(String _input)
	{
		// START KGU#18/KGU#23 2015-11-02
		_input = super.transform(_input);
		// END KGU#18/KGU#23 2015-11-02
		// et => and
		// ou => or
		// lire => readln()
		// écrire => writeln()
		// tant que => ""
		// pour => ""
		// jusqu'à => ""
		// à => "to"
	
        // START KGU 2014-12-02: To achieve consistency with operator highlighting
        //nput=BString.replace(_input,"<--", "<-");
        // END KGU 2014-12-02
		//_input=BString.replace(_input," <- "," := ");
		//_input=BString.replace(_input,"<- "," := ");
		//_input=BString.replace(_input," <-"," := ");
		//_input=BString.replace(_input,"<-"," := ");
		// START KGU 2014-11-16: Comparison operator had to be converted properly first
        _input=BString.replace(_input," == "," = ");
        _input=BString.replace(_input," != "," # ");
        _input=BString.replace(_input," <> "," # ");
        // C and Pascal division operators
        _input=BString.replace(_input," div "," DIV ");
        //_input=BString.replace(_input," mod "," MOD ");
        _input=BString.replace(_input," % "," MOD ");
        //_input=BString.replace(_input,"%"," MOD ");
        // logical operators required transformation, too
        //_input=BString.replace(_input," and "," & ");
        _input=BString.replace(_input," && "," & ");
        //_input=BString.replace(_input," or "," OR ");
        _input=BString.replace(_input," || "," OR ");
        //_input=BString.replace(_input," not "," ~ ");
        _input=BString.replace(_input," ! "," ~ ");
        //_input=BString.replace(_input,"&&"," & ");
        //_input=BString.replace(_input,"||"," OR ");
        _input=BString.replace(_input,"!"," ~ ");
        // END KGU 2014-11-16

//            // BEGIN: Added 2011.11.07 by Bob Fisch
//            StringList empty = new StringList();
//            empty.addByLength(D7Parser.preAlt);
//            empty.addByLength(D7Parser.postAlt);
//            empty.addByLength(D7Parser.preCase);
//            empty.addByLength(D7Parser.postCase);
//            empty.addByLength(D7Parser.preFor);
//            empty.addByLength(D7Parser.postFor);
//            empty.addByLength(D7Parser.preWhile);
//            empty.addByLength(D7Parser.postWhile);
//            empty.addByLength(D7Parser.postRepeat);
//            empty.addByLength(D7Parser.preRepeat);
//            //System.out.println(empty);
//            for(int i=0;i<empty.count();i++)
//            {
//                _input=BString.replace(_input,empty.get(i),"");
//                //System.out.println(i);
//            }
//            if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"TO");}

            
/*		
                
		// BEGIN: Added 2008.01.07 by Bob Fisch
		if(!D7Parser.preAlt.equals("")){_input=BString.replace(_input,D7Parser.preAlt,"");}
		if(!D7Parser.postAlt.equals("")){_input=BString.replace(_input,D7Parser.postAlt,"");}
		if(!D7Parser.preCase.equals("")){_input=BString.replace(_input,D7Parser.preCase,"");}
		if(!D7Parser.postCase.equals("")){_input=BString.replace(_input,D7Parser.postCase,"");}
		if(!D7Parser.preFor.equals("")){_input=BString.replace(_input,D7Parser.preFor,"");}
		if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"TO");}
		if(!D7Parser.preWhile.equals("")){_input=BString.replace(_input,D7Parser.preWhile,"");}
		if(!D7Parser.postWhile.equals("")){_input=BString.replace(_input,D7Parser.postWhile,"");}
		if(!D7Parser.preRepeat.equals("")){_input=BString.replace(_input,D7Parser.preRepeat,"");}
		if(!D7Parser.postRepeat.equals("")){_input=BString.replace(_input,D7Parser.postRepeat,"");}
		// END: Added 2008.01.07 by Bob Fisch
*/
            
		return _input.trim();
	}
	
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
	
	protected void generateCode(Alternative _alt, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_alt, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"IF "+ transform(_alt.getText().getLongString()) + " THEN");
		generateCode(_alt.qTrue, _indent+this.getIndent());
		if (_alt.qFalse.getSize()!=0)
		{
			code.add(_indent+"END");
			code.add(_indent+"ELSE");
			generateCode(_alt.qFalse,_indent+this.getIndent());
		}
		code.add(_indent+"END;");
	}
	
	protected void generateCode(Case _case, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_case, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"CASE "+transform(_case.getText().get(0))+" OF");
		
		for(int i=0;i<_case.qs.size()-1;i++)
		{
			code.add(_indent+this.getIndent()+_case.getText().get(i+1).trim()+":");
			generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent());
			// START KGU 2014-11-16: Wrong case separator replaced
			//code.add(_indent+"END;");
			code.add(_indent+"|");
			// END KGU 2014-11-16
		}
		
		if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		{
			code.add(_indent+this.getIndent()+"ELSE");
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
		}
		// START KGU 2014-11-16: Wrong indentation mended
		//code.add(_indent+this.getIndent()+"END;");
		code.add(_indent+"END;");
		// END KGU 2014-11-16
	}
	
	protected void generateCode(For _for, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_for, _indent);
        // END KGU 2014-11-16
        // START KGU#3 2015-11-02: New reliable loop parameter mechanism
		//code.add(_indent+"FOR "+BString.replace(transform(_for.getText().getText()),"\n","")+" DO");
        int step = _for.getStepConst();
        String incr = (step == 1) ? "" : " BY "+ step;
		code.add(_indent + "FOR " + _for.getCounterVar() + " := " + transform(_for.getStartValue(), false) +
				" TO " + transform(_for.getEndValue(), false) + incr +" DO");
		// END KGU#3 2015-11-02
		generateCode(_for.q,_indent+this.getIndent());
		code.add(_indent+"END;");
	}
	
	protected void generateCode(While _while, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_while, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"WHILE "+BString.replace(transform(_while.getText().getText()),"\n","")+" DO");
		generateCode(_while.q, _indent + this.getIndent());
		code.add(_indent+"END;");
	}
	
	protected void generateCode(Repeat _repeat, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_repeat, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"REPEAT");
		generateCode(_repeat.q,_indent+this.getIndent());
		code.add(_indent+"UNTIL "+BString.replace(transform(_repeat.getText().getText()),"\n","")+";");
	}
	
	protected void generateCode(Forever _forever, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_forever, _indent);
        // END KGU 2014-11-16
		code.add(_indent+"LOOP");
		generateCode(_forever.q,_indent+this.getIndent());
		code.add(_indent+"END;");
	}
	
	protected void generateCode(Call _call, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_call, _indent);
        // END KGU 2014-11-16
		for(int i=0;i<_call.getText().count();i++)
		{
			code.add(_indent+transform(_call.getText().get(i))+";");
		}
	}
	
	protected void generateCode(Jump _jump, String _indent)
	{
        // START KGU 2014-11-16
        insertComment(_jump, _indent);
        // END KGU 2014-11-16
		for(int i=0;i<_jump.getText().count();i++)
		{
			code.add(_indent+transform(_jump.getText().get(i))+";");
		}
	}
	
	protected void generateCode(Subqueue _subqueue, String _indent)
	{
		// code.add(_indent+"");
		for(int i=0;i<_subqueue.children.size();i++)
		{
			generateCode((Element) _subqueue.children.get(i),_indent);
		}
		// code.add(_indent+"");
	}
	
	public String generateCode(Root _root, String _indent)
	{
		String pr = "MODULE";
		String modname = _root.getText().get(0);
		if(_root.isProgram==false) {pr="PROCEDURE";}
		
		code.add(pr+" "+modname+";");
		code.add("");

		// Add comments and/or declarations to the program (Bob)
		for(int i=0;i<_root.getComment().count();i++)
		{
			if(!_root.getComment().get(i).startsWith("#"))
			{
				code.add(_root.getComment().get(i));
			}
	        // START KGU 2014-11-16: Don't get the comments get lost
			else {
				insertComment(_root.getComment().get(i).substring(1), "");
			}
	        // END KGU 2014-11-16
			
		}
		
		//code.add("// declare your variables here");
		code.add("");
		code.add("BEGIN");
		generateCode(_root.children,_indent+this.getIndent());
		code.add("END "+modname+".");
		
		return code.getText();
	}
	
	
}

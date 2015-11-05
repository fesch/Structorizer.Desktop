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

    KSH Source Code Generator

    Copyright (C) 2008 Jan Peter Klippel

    This file has been released under the terms of the GNU Lesser General
    Public License as published by the Free Software Foundation.

    http://www.gnu.org/licenses/lgpl.html

 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Jan Peter Klippel
 *
 *      Description:    KSH Source Code Generator
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author					Date			Description
 *      ------                  ----			-----------
 *      Jan Peter Klippel       2008.04.11              First Issue
 *      Bob Fisch               2008.04.12              Added "Fields" section for generator to be used as plugin
 *      Bob Fisch               2011.11.07              Fixed an issue while doing replacements
 *      Kay Gürtzig             2015.11.02              Inheritance changed (because the code was nearly
 *                                                      identical to BASHGenerator - so why do it twice?)
 *                                                      Function argument handling improved
 *
 ******************************************************************************************************
 *
 *      Comment:		LGPL license (http://www.gnu.org/licenses/lgpl.html).
 *
 *      2015-11-02 <Kay Gürtzig>
 *      - Inheritance changed from Generator to BASHGenerator - hope that doesn't spoil the plugin idea
 *      - Implemented a way to pass function arguments into the named parameters
 *      
 ******************************************************************************************************///


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


public class KSHGenerator extends BASHGenerator {

	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export KSH Code ...";
	}
	
	protected String getFileDescription()
	{
		return "KSH Source Code";
	}
	
	protected String getIndent()
	{
		return " ";
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"ksh", "sh"};
		return exts;
	}
	
    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "#";
    }
    // END KGU 2015-10-18

    /************ Code Generation **************/

//	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
//	/**
//	 * A pattern how to embed the variable (right-hand side of an input instruction)
//	 * into the target code
//	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
//	 */
//	protected String getInputReplacer()
//	{
//		return "read $1";
//	}
//
//	/**
//	 * A pattern how to embed the expression (right-hand side of an output instruction)
//	 * into the target code
//	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
//	 */
//	protected String getOutputReplacer()
//	{
//		return "echo $1";
//	}
//
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
//	// END KGU#18/KGU#23 2015-11-01
//
//	private String transform(String _input)
//	{
//	
//		_input=BString.replace(_input, " <- ", "=");
//		_input=BString.replace(_input, "<- ", "=");
//		_input=BString.replace(_input, " <-", "=");
//		_input=BString.replace(_input, "<-", "=");
//                
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
//            if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"to");}
//
//            
///*		
//		if(!D7Parser.preAlt.equals("")){_input=BString.replace(_input,D7Parser.preAlt,"");}
//		if(!D7Parser.postAlt.equals("")){_input=BString.replace(_input,D7Parser.postAlt,"");}
//		if(!D7Parser.preCase.equals("")){_input=BString.replace(_input,D7Parser.preCase,"");}
//		if(!D7Parser.postCase.equals("")){_input=BString.replace(_input,D7Parser.postCase,"");}
//		if(!D7Parser.preFor.equals("")){_input=BString.replace(_input,D7Parser.preFor,"");}
//		if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"");}
//		if(!D7Parser.preWhile.equals("")){_input=BString.replace(_input,D7Parser.preWhile,"");}
//		if(!D7Parser.postWhile.equals("")){_input=BString.replace(_input,D7Parser.postWhile,"");}
//		if(!D7Parser.preRepeat.equals("")){_input=BString.replace(_input,D7Parser.preRepeat,"");}
//		if(!D7Parser.postRepeat.equals("")){_input=BString.replace(_input,D7Parser.postRepeat,"");}
//*/			
//		if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input+" ")>=0){_input=BString.replace(_input,D7Parser.input+" ","read ");}
//		if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output+" ")>=0){_input=BString.replace(_input,D7Parser.output+" ","echo ");}
//		if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input)>=0){_input=BString.replace(_input,D7Parser.input,"read ");}
//		if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output)>=0){_input=BString.replace(_input,D7Parser.output,"echo ");}
//		return _input.trim();
//	}
//	
//	protected void generateCode(Instruction _inst, String _indent) {
//		if(!insertAsComment(_inst, _indent))
//			// START KGU 2015-11-02: Comments added
//			insertComment(_inst, _indent);
//			// END KGU 2015-11-02
//			for(int i=0;i<_inst.getText().count();i++)
//			{
//				code.add(_indent+transform(_inst.getText().get(i))+";");
//			}
//
//	}
//	
//	protected void generateCode(Alternative _alt, String _indent) {
//		
//		code.add("");
//		// START KGU 2015-11-02: Comments added
//		insertComment(_alt, _indent);
//		// END KGU 2015-11-02
//		code.add(_indent + "if " + transform(_alt.getText().getLongString()).trim());
//		code.add(_indent+"then");
//		generateCode(_alt.qTrue,_indent+this.getIndent());
//		
//		if(_alt.qFalse.getSize()!=0) {
//			
//			code.add(_indent+"");
//			code.add(_indent+"else");			
//			generateCode(_alt.qFalse,_indent+this.getIndent());
//			
//		}
//		
//		code.add(_indent+"fi");
//		code.add("");
//		
//	}
//	
//	protected void generateCode(Case _case, String _indent) {
//		
//		code.add("");
//		// START KGU 2015-11-02: Comments added
//		insertComment(_case, _indent);
//		// END KGU 2015-11-02
//		code.add(_indent+"case "+transform(_case.getText().get(0))+" in");
//		
//		for(int i=0;i<_case.qs.size()-1;i++)
//		{
//			code.add("");
//			// START KGU#15 2015-11-02: Case patterns must be separated by '|'
//			//code.add(_indent+this.getIndent()+_case.getText().get(i+1).trim()+")");
//			code.add(_indent + this.getIndent() + _case.getText().get(i+1).trim().replace(",", "|") + ")");
//			// END KGU#15 2015-11-02
//			generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent()+this.getIndent()+this.getIndent());
//			code.add(_indent+this.getIndent()+";;");
//		}
//		
//		if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
//		{
//			code.add("");
//			code.add(_indent+this.getIndent()+"*)");
//			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
//			code.add(_indent+this.getIndent()+";;");
//		}
//		code.add(_indent+"esac");
//		code.add("");
//		
//	}
//	
//	
//	protected void generateCode(For _for, String _indent) {
//		
//		code.add("");
//		// START KGU 2015-11-02: Comments added
//		insertComment(_for, _indent);
//		// END KGU 2015-11-02
//		// FIXME (KGU 2015-11-02): This replacement doesn't actually make sense to replace the assignment operator by "in"...
//		code.add(_indent + "for " + BString.replace(transform(_for.getText().getLongString()), "=", " in ").trim());
//		code.add(_indent+"do");
//		generateCode(_for.q,_indent+this.getIndent());
//		code.add(_indent+"done");	
//		code.add("");
//		
//	}
//
//	protected void generateCode(While _while, String _indent) {
//		
//		code.add("");
//		// START KGU 2015-11-02: Comments added
//		insertComment(_while, _indent);
//		// END KGU 2015-11-02
//		code.add(_indent+"while "+BString.replace(transform(_while.getText().getText()),"\n","").trim());
//		code.add(_indent+"do");
//		generateCode(_while.q,_indent+this.getIndent());
//		code.add(_indent+"done");
//		code.add("");
//		
//	}
//	
//	protected void generateCode(Repeat _repeat, String _indent) {
//		
//		code.add("");
//		// START KGU 2015-11-02: Comments added
//		insertComment(_repeat, _indent);
//		// END KGU 2015-11-02
//		// FIXME (KGU 2015-11-02): This head-controlled loop is NOT equivalent to a Repeat loop!
//		code.add(_indent+"until "+BString.replace(transform(_repeat.getText().getText()),"\n","").trim());
//		code.add(_indent+"do");
//		generateCode(_repeat.q,_indent+this.getIndent());
//		code.add(_indent+"done");
//		code.add("");
//		
//	}
//    
//	protected void generateCode(Forever _forever, String _indent) {
//		
//		code.add("");
//		// START KGU 2015-11-02: Comments added
//		insertComment(_forever, _indent);
//		// END KGU 2015-11-02
//		code.add(_indent+"while [1]");
//		code.add(_indent+"do");
//		generateCode(_forever.q,_indent+this.getIndent());
//		code.add(_indent+"done");
//		code.add("");
//		
//	}
//	
//	protected void generateCode(Call _call, String _indent) {
//		if(!insertAsComment(_call, _indent))
//			// START KGU 2015-11-02: Comments added
//			insertComment(_call, _indent);
//			// END KGU 2015-11-02
//			for(int i=0;i<_call.getText().count();i++)
//			{
//				code.add(_indent+transform(_call.getText().get(i))+";");
//			}
//
//	}
//	
//	protected void generateCode(Jump _jump, String _indent) {
//		if(!insertAsComment(_jump, _indent))	
//			// START KGU 2015-11-02: Comments added
//			insertComment(_jump, _indent);
//			// END KGU 2015-11-02
//			for(int i=0;i<_jump.getText().count();i++)
//			{
//				code.add(_indent+transform(_jump.getText().get(i))+";");
//			}
//
//	}
//	
//	protected void generateCode(Subqueue _subqueue, String _indent) {
//		
//		// code.add(_indent+"");
//		for(int i=0;i<_subqueue.children.size();i++)
//		{
//			generateCode((Element) _subqueue.children.get(i),_indent);
//		}
//		// code.add(_indent+"");
//		
//	}
	
	public String generateCode(Root _root, String _indent) {

		String indent = _indent;
		if( ! _root.isProgram ) {
			// START KGU#53 2015-11-02: Shell functions obtain their arguments via $1, $2 etc.
			//code.add(_root.getText().get(0)+" () {");
			String header = _root.getMethodName() + "()";
			code.add(header + " {");
			indent = indent + this.getIndent();
			StringList paraNames = _root.getParameterNames();
			for (int i = 0; i < paraNames.count(); i++)
			{
				code.add(indent + paraNames.get(i) + "=$" + (i+1));
			}
			// END KGU#53 2015-11-02
		} else {
				
			code.add("#!/usr/bin/ksh");
			code.add("");
			
		}
		
		// START KGU 2015-11-02: Comments added
		insertComment(_root, _indent);
		// END KGU 2015-11-02
		insertComment("generated by Structorizer", _indent);
		code.add("");
		insertComment("TODO declare your variables here", _indent);
		code.add("");
		generateCode(_root.children, _root.isProgram ? _indent : _indent + this.getIndent());
		
		if( ! _root.isProgram ) {
			code.add("}");
		}
		
		return code.getText();
		
	}
	
}



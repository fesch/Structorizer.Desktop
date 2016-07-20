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
 *      Jan Peter Klippel       2008.04.11      First Issue
 *      Bob Fisch               2008.04.12      Added "Fields" section for generator to be used as plugin
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2015.11.02      Inheritance changed (because the code was nearly
 *                                              identical to BASHGenerator - so why do it twice?)
 *                                              Function argument handling improved
 *      Kay Gürtzig             2016.01.08      Bugfix #96 (= KG#129): Variable names fetched
 *      Kay Gürtzig             2016-07-20      Enh. #160 (option to involve referred subroutines)
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


import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
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
	
//	// START KGU 2016-01-08: Possible replacement (portable shell code) for the inherited modern BASH code
//	@Override
//	protected void generateCode(For _for, String _indent) {
//
//		code.add("");
//		insertComment(_for, _indent);
//		String counterStr = _for.getCounterVar();
//		String startValueStr = transform(_for.getStartValue());
//		String endValueStr = transform(_for.getEndValue());
//		int stepValue = _for.getStepConst();
//		String compOpr = " -le ";
//		if (stepValue < 0) {
//			compOpr = " -ge ";
//		}
//		code.add(_indent + counterStr + "=" + startValueStr);
//		code.add(_indent+"while [[ $" + counterStr + compOpr + endValueStr + " ]]");
//		// END KGU#30 2015-10-18
//		code.add(_indent+"do");
//		generateCode(_for.q, _indent + this.getIndent());
//		code.add(_indent + this.getIndent() + "let " + counterStr + "=" + counterStr + ((stepValue >= 0) ? "+" : "") + stepValue);
//		code.add(_indent+"done");	
//		code.add("");
//
//	}
//	// END KGU 2016-01-08

	public String generateCode(Root _root, String _indent) {

		String indent = _indent;
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
			code.add("#!/usr/bin/ksh");
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			subroutineInsertionLine = code.count();
			code.add("");
		}
		else
		{
			code.add("");
		}
		// END KGU#178 2016-07-20

		// START KGU 2015-11-02: Comments added
		insertComment(_root, _indent);
		// END KGU 2015-11-02
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
		}
		
		// START KGU#129 2016-01-08: Bugfix #96 - Now fetch all variable names from the entire diagram
		varNames = _root.getVarNames();
		insertComment("TODO: Check and revise the syntax of all expressions!", _indent);
		// END KGU#129 2016-01-08
		code.add("");
		//insertComment("TODO declare your variables here", _indent);
		//code.add("");
		generateCode(_root.children, _root.isProgram ? _indent : _indent + this.getIndent());
		
		if( ! _root.isProgram ) {
			code.add("}");
		}
		
		return code.getText();
		
	}
	
}



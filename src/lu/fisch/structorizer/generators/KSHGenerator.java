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
 *      Kay Gürtzig             2016-01-08      Bugfix #96 (= KG#129): Variable names fetched
 *      Kay Gürtzig             2016-07-20      Enh. #160 (option to involve referred subroutines)
 *      Kay Gürtzig             2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2017-01-05      Enh. #314: File API TODO comments added, issue #234 chr/ord support
 *      Kay Gürtzig             2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2019-03-08      Enh. #385: Optional function arguments with defaults
 *      Kay Gürtzig             2019-09-27      Enh. #738: Support for code preview map on Root level
 *      Kay Gürtzig             2019-10-15      Bugfix #765: Field typeMap had to be initialized, e.g. for transformTokens()
 *      Kay Gürtzig             2019-12-01      Enh. #739: At least minimum support for enum types, array declarations mended
 *      Kay Gürtzig             2020-02-16      Issue #816: Function calls and value return mechanism revised
 *      Kay Gürtzig             2020-02-18      Enh. #388: Support for constants
 *      Kay Gürtzig             2020-02-24      Issues #816,#821: generateCode(Root) decomposed
 *      Kay Gürtzig             2020-03-18      Bugfix #839 - sticky returns flag mended
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

import java.util.Map.Entry;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.TypeMapEntry;


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
	
	// START KGU#542 2019-12-01: Enh. #739 enumeration type support - configuration for subclasses
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.BASHGenerator#getEnumDeclarator()
	 */
	@Override
	protected String getEnumDeclarator()
	{
		return "";	// ksh doesn't know a declare command (as far as we know)
	}

	// On this occasion, we also repair the array declaration, which differes from bash
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.BASHGenerator#getArrayDeclarator()
	 */
	@Override
	protected String getArrayDeclarator(boolean isConst)
	{
		return "set -A ";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.BASHGenerator#getAssocDeclarator()
	 */
	@Override
	protected String getAssocDeclarator(boolean isConst)
	{
		return isConst ? "typeset -A -r " : "typeset -A ";
	}
	// END KGU#542 2019-12-01
	
	// START KGU#803/KGU#806 2020-02-18: Issues #388, #816
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.BASHGenerator#getConstDeclarator()
	 */
	@Override
	protected String getConstDeclarator()
	{
		return "typeset -r ";
	}

	@Override
	protected String getNameRefDeclarator(boolean isConst)
	{
		if (isConst) {
			return "typeset -nr ";
		}
		return "typeset -n ";
	}
	
	@Override
	protected String getLocalDeclarator(boolean isConst, TypeMapEntry type)
	{
		if (type != null) {
			if (type.isRecord()) {
				return this.getAssocDeclarator(isConst);
			}
			else if (type.isArray()) {
				return this.getArrayDeclarator(isConst);
			}
			String typeName = type.getCanonicalType(true, true);
			if (typeName.equals("int")) {
				return "typeset -i" + (isConst ? "r" : "") + " ";
			}
			else if (typeName.equals("double")) {
				return "typeset -E" + (isConst ? "r" : "") + " ";
			}
		}
		if (isConst) {
			return getConstDeclarator();
		}
		return "typeset ";
	}
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.BASHGenerator#makeArrayCopy(java.lang.String, java.lang.String)
	 */
	@Override
	protected String makeArrayCopyAssignment(String tgtVar, String srcVar, boolean getKeys, boolean asConstant, boolean asGlobal)
	{
		String prefix = "";
		String postfix = "";
		if (tgtVar != null) {
			prefix = this.getArrayDeclarator(asConstant);
			if (asGlobal) {
				postfix += "; export " + tgtVar;
			}
			prefix += tgtVar + this.getArrayInitOperator();
		}
		return prefix + "\"${" + (getKeys ? "!" : "") + srcVar + "[@]}\"" + postfix;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.BASHGenerator#getArrayInitOperator()
	 */
	@Override
	protected String getArrayInitOperator()
	{
		return " ";
	}
	// END KGU#803/KGU#806 2020-02-18

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

	public String generateCode(Root _root, String _indent, boolean _public) {

		String indent = _indent;
		root = _root;
		// START KGU#753 2019-10-15: Bugfix #765 - superclass methods need an initialized typeMap
		typeMap = _root.getTypeInfo(routinePool);
		// END KGU#753 2019-10-15
		
		// START KGU#803 2020-02-16: Issue #816
		// START KGU#828 2020-03-18: Bugfix #839: Some fields had been forgotten to reset
		this.returns = false;
		// END KGU#828 2020-03-18
		boolean alwaysReturns = mapJumps(_root.children);
		// END KGU#803 2020-02-16
		
		// START KGU#178 2016-07-20: Enh. #160
		
		// START KGU#705 2019-09-23: Enh. #738
		int line0 = code.count();
		if (codeMap!= null) {
			// register the triple of start line no, end line no, and indentation depth
			// (tab chars count as 1 char for the text positioning!)
			codeMap.put(_root, new int[]{line0, line0, _indent.length()});
		}
		// END KGU#705 2019-09-23
		
		if (topLevel)
		{
			code.add("#!/usr/bin/ksh");
			appendComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			appendCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			// START KGU#351 2017-02-26: Enh. #346
			this.appendUserIncludes("");
			// END KGU#351 2017-02-26
			subroutineInsertionLine = code.count();
			addSepaLine();
			appendAuxiliaryCode(_indent);
		}
		else
		{
			addSepaLine();
		}
		// END KGU#178 2016-07-20

		// START KGU 2015-11-02: Comments added
		appendComment(_root, _indent);
		// END KGU 2015-11-02
		if( _root.isSubroutine() ) {
			// START KGU#53 2015-11-02: Shell functions obtain their arguments via $1, $2 etc.
			//code.add(_root.getText().get(0)+" () {");
			// START KGU#803 2020-02-18: Issue #816 - make sure declarations make variables local
			//String header = _root.getMethodName() + "()";
			//code.add(header + " {");
			addCode("function " + _root.getMethodName() + " {", "", false);
			// END KGU#803 2020-02-18
			indent = indent + this.getIndent();
			// START KGU#803 2020-02-18: Issue #816 - outsourced to enhanced method
			//StringList paraNames = _root.getParameterNames();
			//// START KGU#371 2019-03-08: Enh. #385 support optional arguments
			////for (int i = 0; i < paraNames.count(); i++)
			////{
			////	code.add(indent + paraNames.get(i) + "=$" + (i+1));
			////}
			//int minArgs = _root.getMinParameterCount();
			//StringList argDefaults = _root.getParameterDefaults();
			//for (int i = 0; i < minArgs; i++)
			//{
			//	code.add(indent + paraNames.get(i) + "=$" + (i+1));
			//}
			//for (int i = minArgs; i < paraNames.count(); i++)
			//{
			//	code.add(indent + "if [ ${#} -lt " + (i+1) + " ]");
			//	code.add(indent + "then");
			//	code.add(indent + this.getIndent() + paraNames.get(i) + "=" + transform(argDefaults.get(i)));
			//	code.add(indent + "else");
			//	code.add(indent + this.getIndent() + paraNames.get(i) + "=$" + (i+1));
			//	code.add(indent + "fi");
			//}
			//// END KGU#371 2019-03-08
			this.generateArgAssignments(_root, indent);
			// END KGU#803 2020-02-18
			// END KGU#53 2015-11-02
		}
		
		// START KGU#542 2019-12-01: Enh. #739 - support for enumeration types
		for (Entry<String, TypeMapEntry> typeEntry: typeMap.entrySet()) {
			TypeMapEntry type = typeEntry.getValue();
			if (typeEntry.getKey().startsWith(":") && type != null && type.isEnum()) {
				appendEnumeratorDef(type, _indent);
			}
		}
		// END KGU#542 2019-12-01
		// START KGU#129 2016-01-08: Bugfix #96 - Now fetch all variable names from the entire diagram
		varNames = _root.retrieveVarNames();
		appendComment("TODO: Check and revise the syntax of all expressions!", _indent);
		// END KGU#129 2016-01-08
		addSepaLine();
		//insertComment("TODO declare your variables here", _indent);
		//addSepaLine();
		// START KGU#389/KGU#803/KGU#806 2020-02-21: Enh. #423, #816, #821 declare records as associative arrays
		generateDeclarations(indent);
		// END KGU#389/KGU#803/KGU#806
		// START KGU#803 2020-02-24: Issue #816
		if (_root.isSubroutine()) {
			this.isResultSet = varNames.contains("result", false);
			this.isFunctionNameSet = varNames.contains(_root.getMethodName());
		}
		// END KGU#803 2020-02-24
		generateCode(_root.children, indent);
		
		// START KGU#803 2020-02-16: Issue #816
		generateResult(_root, indent, alwaysReturns, varNames);
		// END KGU#803 2020-02-16

		if( _root.isSubroutine() ) {
			code.add("}");
		}
		
		// START KGU#815/KGU#824 2020-03-19: Enh. #828, bugfix #836
		if (topLevel) {
			libraryInsertionLine = code.count();
		}
		// END KGU#815/KGU#824 2020-03-19

		// START KGU#705 2019-09-23: Enh. #738
		if (codeMap != null) {
			// Update the end line no relative to the start line no
			codeMap.get(_root)[1] += (code.count() - line0);
		}
		// END KGU#705 2019-09-23

		return code.getText();
		
	}

}



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
 *      Description:    This class generates C# code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author         		Date			Description
 *      ------			----                	-----------
 *      Bob Fisch       	2008.11.17              First Issue
 *      Gunter Schillebeeckx    2010.08.07              C# Generator starting from C Generator & Java Generator
 *      Kay Gürtzig             2010.09.10              Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011.11.07              Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.11.06              Support for logical Pascal operators added
 *      Kay Gürtzig             2014.11.16              Bugfixes and enhancements (see comment)
 *      Kay Gürtzig             2014.12.02              Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig             2015.10.18              Indentation fixed, comment insertion interface modified
 *      Kay Gürtzig             2015.11.01              Inheritance changed and unnecessary overridings disabled
 *      Kay Gürtzig             2015.11.30              Sensible handling of return and exit/break instructions
 *                                                      (issue #22 = KGU#47)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2015-11-30 - Bugfix / enhancement #22 (KGU#47) <Kay Gürtzig>
 *      - The generator now checks in advance mechanisms of value return and premature exits in order
 *        to generate appropriate instructions
 *      - Also the analysis of routine arguments and return types was improved
 *      
 *      2015-11-01 - Code revision / enhancements <Kay Gürtzig>
 *      - Inheritance changed to CGenerator because most of the stuff is very similar.
 *      - Enhancement #10 (KGU#3): FOR loops now provide themselves more reliable loop parameters 
 *      - Enhancement KGU#15: Support for the gathering of several case values in CASE instructions
 *
 *      2015.10.18 - Bugfix
 *      - Indentation wasn't done properly (_indent+this.getIndent() works only for single-character indents)
 *      
 *      2014.11.16 - Bugfixes / Enhancements
 *      - conversion of comparison and logical operators had still been flawed
 *      - element comment export added
 *      
 *      2014.11.06 - Enhancement (Kay Gürtzig)
 *      - Pascal-style logical operators "and", "or", and "not" supported 
 *      
 *      2010.09.10 - Bugfixes
 *      - Code generator for the Case structure (switch) had missed to add the case keywords
 *      - Comparison and assignment operator conversion was incomplete
 *      - Missing parentheses around negated condition of "do while" added
 *      - logical flaw in the automatic addition of brackets for "if", "while", and "switch" mended
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *      		
 *      2010.08.07 - Bugfixes
 *      - none
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


public class CSharpGenerator extends CGenerator 
{

	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export C# ...";
	}

	protected String getFileDescription()
	{
		return "C# Source Code";
	}

	protected String[] getFileExtensions()
	{
		String[] exts = {"cs"};
		return exts;
	}

	// TODO
	/************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected String getInputReplacer()
	{
		return "Console.ReadLine($1)";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "Console.WriteLine($1)";
	}


	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	protected void insertExitInstr(String _exitCode, String _indent)
	{
		Jump dummy = new Jump();
		insertBlockHeading(dummy, "if (System.Windows.Forms.Application.MessageLoop)", _indent); 
		insertComment("WinForms app", _indent + this.getIndent());
		code.add(_indent + this.getIndent() + "System.Windows.Forms.Application.Exit();");
		insertBlockTail(dummy, null, _indent);

		insertBlockHeading(dummy, "else", _indent); 
		insertComment("Console app", _indent + this.getIndent());
		code.add(_indent + this.getIndent() + "System.Environment.Exit(" + _exitCode + ");");
		insertBlockTail(dummy, null, _indent);
	}
	// END KGU#16/#47 2015-11-30

// KGU#47 2015-11-30: Now inherited - exactly same behaviour as in C		
//        // JUMP
//        protected void generateCode(Jump _jump, String _indent)
//        {
//                // START KGU 2014-11-16
//                insertComment(_jump, _indent);
//                // END KGU 2014-11-16
//                for(int i=0;i<_jump.getText().count();i++)
//                {
//                        code.add(_indent+"goto "+transform(_jump.getText().get(i))+"; // jump-instruction not recommended");
//
//                }
//        }


// KGU#16/KGU#74 Now only override the new decomposed methods below. 
//        public String generateCode(Root _root, String _indent)
//        {
//			// START KGU#74 2015-11-30: Prepare the label associations
//    		String brace = optionBlockBraceNextLine() ? "" : " {";
//			String fnName = _root.getMethodName();
//			this.alwaysReturns = this.mapJumps(_root.children);
//			// Get all local variable names
//			StringList varNames = _root.getVarNames(_root, false, true);
//			// END KGU#74 2015-11-30
//        	if(_root.isProgram==true) {
//        		code.add(_indent + "using System;");
//        		code.add(_indent + "");
//        		// START KGU 2015-10-18
//        		insertBlockComment(_root.getComment(), _indent, "/**", " * ", " */");
//        		// END KGU 2015-10-18
//        		code.add(_indent + "public class "+ fnName +" {");
//        		code.add(_indent);
//        		insertComment("TODO Declare and initialise global variables here", _indent + this.getIndent());
//        		code.add(_indent);
//        		code.add(_indent + this.getIndent()+"/**");
//        		code.add(_indent + this.getIndent()+" * @param args");
//        		code.add(_indent + this.getIndent()+" */");
//        		code.add(_indent + this.getIndent()+"public static void Main(string[] args)" + brace);
//    	    	if (optionBlockBraceNextLine())	code.add(_indent + this.getIndent() + "{");
//    	    	code.add("");
//        		insertComment("TODO Declare and initialise local variables here:", _indent + this.getIndent()+this.getIndent());
//        		// START KGU 2015-11-30
//        		for (int v = 0; v < varNames.count(); v++)
//        		{
//        			insertComment(varNames.get(v), this.getIndent() + this.getIndent());
//        		}
//        		// END KGU 2015-11-30
//        		code.add(_indent + this.getIndent()+this.getIndent());
//        		generateCode(_root.children, _indent + this.getIndent()+this.getIndent());
//        		code.add(_indent + this.getIndent()+"}");
//        		code.add(_indent);
//        		code.add(_indent + "}");
//        	}
//        	else {
//        		// START KGU 2015-11-30: More precise header information
//        		//insertBlockComment(_root.getComment(), _indent + this.getIndent(), "/**", " * ", null);
//        		//insertBlockComment(paramNames, _indent+this.getIndent(), null, " * @param ", " */");
//        		//code.add(_indent+this.getIndent() + "private static void " + _root.getText().get(0) + " {");
//        		StringList argNames = new StringList();
//        		StringList argTypes = new StringList();
//        		_root.collectParameters(argNames, argTypes);
//        		String resultType = _root.getResultType();
//        		insertBlockComment(_root.getComment(), _indent+this.getIndent(), "/**", " * ", null);
//        		if (resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
//        		{
//        			insertBlockComment(argNames, _indent+this.getIndent(), null, " * @param ", null);
//        			code.add(_indent+this.getIndent() + " * @return ");
//        			code.add(_indent+this.getIndent() + " */");
//        			resultType = transformType(resultType, "int");
//        		}
//        		else
//        		{
//        			insertBlockComment(argNames, _indent+this.getIndent(), null, " * @param ", " */");
//        			resultType = "void";
//        		}
//        		String fnHeader = "public static " + resultType + " " + fnName + "(";
//        		for (int p = 0; p < argNames.count(); p++) {
//        			if (p > 0)
//        				fnHeader += ", ";
//        			fnHeader += (transformType(argTypes.get(p), "/*TODO*/") + " " + argNames
//        					.get(p)).trim();
//        		}
//        		fnHeader += ")";
//        		code.add(_indent+this.getIndent() + fnHeader + brace);
//    	    	if (optionBlockBraceNextLine())	code.add(_indent + this.getIndent() + "{");
//    	    	
//    	    	// Variable declaration proposals (now with all used variables listed)
//        		// START KGU 2015-10-18
//        		insertComment("TODO: Declare local variables here:", _indent+this.getIndent() + this.getIndent());
//        		// START KGU 2015-11-30
//        		for (int v = 0; v < varNames.count(); v++)
//        		{
//        			insertComment(varNames.get(v), _indent+this.getIndent() + this.getIndent());
//        		}
//        		// END KGU 2015-11-30
//        		
//        		// Routine body
//        		code.add(_indent);
//        		generateCode(_root.children, _indent+this.getIndent() + this.getIndent());
//        		code.add(_indent);
//        		
//        		// Result production
//        		// START KGU#47 2015-11-30: If the result mechanism is insecure we we try to ensure a value return
//        		if ((this.returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns) {
//        			String result = "0";
//        			if (isFunctionNameSet) {
//        				result = fnName;
//        			} else if (isResultSet) {
//        				int vx = varNames.indexOf("result", false);
//        				result = varNames.get(vx);
//        			}
//        			code.add(this.getIndent());
//        			code.add(_indent+this.getIndent()+this.getIndent() + "return " + result + ";");
//        		}
//        		// END KGU#47 2015-11-30
//        		code.add(_indent+this.getIndent() + "}");
//        	}
//
//        	return code.getText();
//        }

	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		if (_root.isProgram==true) {
			code.add(_indent + "using System;");
			code.add(_indent + "");
			// START KGU 2015-10-18
			insertBlockComment(_root.getComment(), _indent, "/**", " * ", " */");
			// END KGU 2015-10-18

			insertBlockHeading(_root, "public class "+ _procName, _indent);
			code.add(_indent);
			insertComment("TODO: Declare and initialise global variables here", _indent + this.getIndent());
			code.add(_indent);
			code.add(_indent + this.getIndent()+"/**");
			code.add(_indent + this.getIndent()+" * @param args");
			code.add(_indent + this.getIndent()+" */");

			insertBlockHeading(_root, "public static void Main(string[] args)", _indent + this.getIndent());
			code.add("");
		}
		else {
			insertBlockComment(_root.getComment(), _indent+this.getIndent(), "/**", " * ", null);
			if (_resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
			{
				insertBlockComment(_paramNames, _indent + this.getIndent(), null, " * @param ", null);
				code.add(_indent+this.getIndent() + " * @return ");
				code.add(_indent+this.getIndent() + " */");
				_resultType = transformType(_resultType, "int");
			}
			else
			{
				insertBlockComment(_paramNames, _indent+this.getIndent(), null, " * @param ", " */");
				_resultType = "void";
			}
			String fnHeader = "public static " + _resultType + " " + _procName + "(";
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0)
					fnHeader += ", ";
				fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
						_paramNames.get(p)).trim();
			}
			fnHeader += ")";
			insertBlockHeading(_root, fnHeader, _indent+this.getIndent());
		}

		return _indent + this.getIndent() + this.getIndent();
	}

	/**
	 * Generates some preamble (i.e. comments, language declaration section etc.)
	 * and adds it to this.code.
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param varNames - list of variable names introduced inside the body
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		code.add("");
		// Variable declaration proposals (now with all used variables listed)
		insertComment("TODO: Declare local variables here:", _indent);
		for (int v = 0; v < varNames.count(); v++)
		{
			insertComment(varNames.get(v), _indent);
		}
		code.add("");
		return _indent;
	}

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
		if ((returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
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
			code.add(_indent);
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}

	// START KGU 2015-12-15: Method block must be closed as well
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root - the diagram root element 
	 * @param _indent - the current indentation string
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// Method block close
		code.add(_indent + this.getIndent() + "}");

		super.generateFooter(_root, _indent);
	}
	// END KGU 2015-12-15
    	
}

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

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class generates C# code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author         			Date			Description
 *      ------					----            -----------
 *      Bob Fisch               2008.11.17      First Issue
 *      Gunter Schillebeeckx    2010.08.07      C# Generator starting from C Generator & Java Generator
 *      Kay Gürtzig             2010.09.10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.11.06      Support for logical Pascal operators added
 *      Kay Gürtzig             2014.11.16      Bugfixes and enhancements (see comment)
 *      Kay Gürtzig             2014.12.02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig             2015.10.18      Indentation fixed, comment insertion interface modified
 *      Kay Gürtzig             2015.11.01      Inheritance changed and unnecessary overridings disabled
 *      Kay Gürtzig             2015.11.30      Sensible handling of return and exit/break instructions
 *                                              (issue #22 = KGU#47)
 *      Kay Gürtzig             2016.03.23      Enh. #84: Support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig             2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178),
 *                                              brace balance in non-program files fixed  
 *      Kay Gürtzig             2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions) 
 *      Kay Gürtzig             2016.10.14      Enh. 270: Handling of disabled elements (code.add(...) --> addCode(..))
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
 ******************************************************************************************************
 */

import lu.fisch.utils.*;
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

	// START KGU 2016-08-12: Enh. #231 - information for analyser
    private static final String[] reservedWords = new String[]{
		"abstract", "as", "base", "bool", "break", "byte",
		"case", "catch", "char", "checked", "class", "const", "continue",
		"decimal", "default", "delegate", "do", "double",
		"else", "enum", "event", "explicit", "extern",
		"false", "finally", "fixed", "float", "for", "foreach", "goto",
		"if", "implicit", "in", "int", "interface", "internal", "is",
		"lock", "long", "namespace", "new", "null",
		"object", "operator", "out", "override", "params", "private", "public",
		"readonly", "ref", "return", "sbyte", "sealed", "short", "sizeof",
		"stackalloc", "static", "string", "struct", "switch",
		"this", "throw", "true", "try", "typeof",
		"uint", "ulong", "unchecked", "unsafe", "ushort", "using",
		"virtual", "void", "volatile", "while"};
	public String[] getReservedWords()
	{
		return reservedWords;
	}
	// END KGU 2016-08-12

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
	@Override
	protected void insertExitInstr(String _exitCode, String _indent, boolean isDisabled)
	{
		Jump dummy = new Jump();
		insertBlockHeading(dummy, "if (System.Windows.Forms.Application.MessageLoop)", _indent); 
		insertComment("WinForms app", _indent + this.getIndent());
		addCode(this.getIndent() + "System.Windows.Forms.Application.Exit();", _indent, isDisabled);
		insertBlockTail(dummy, null, _indent);

		insertBlockHeading(dummy, "else", _indent); 
		insertComment("Console app", _indent + this.getIndent());
		addCode(this.getIndent() + "System.Environment.Exit(" + _exitCode + ");", _indent, isDisabled);
		insertBlockTail(dummy, null, _indent);
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
	/**
	 * We try our very best to create a working loop from a FOR-IN construct
	 * This will only work, however, if we can get reliable information about
	 * the size of the value list, which won't be the case if we obtain it e.g.
	 * via a variable.
	 * @param _for - the element to be exported
	 * @param _indent - the current indentation level
	 * @return true iff the method created some loop code (sensible or not)
	 */
	protected boolean generateForInCode(For _for, String _indent)
	{
		boolean isDisabled = _for.isDisabled();
		// We simply use the range-based loop of Java (as far as possible)
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		StringList items = this.extractForInListItems(_for);
		String indent = _indent;
		String itemType = null;
		if (items != null)
		{
			valueList = "{" + items.concatenate(", ") + "}";
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogenous? We will just try three types: int,
			// double and String, and if none of them match we add a TODO comment.
			int nItems = items.count();
			boolean allInt = true;
			boolean allDouble = true;
			boolean allString = true;
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
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
				if (allDouble)
				{
					try {
						Double.parseDouble(item);
					}
					catch (NumberFormatException ex)
					{
						allDouble = false;
					}
				}
				if (allString)
				{
					allString = item.startsWith("\"") && item.endsWith("\"") &&
							!item.substring(1, item.length()-1).contains("\"");
				}
			}
			if (allInt) itemType = "int";
			else if (allDouble) itemType = "double";
			else if (allString) itemType = "char*";
			String arrayName = "array20160322";
			
			// Extra block to encapsulate the additional variable declarations
			addCode("{", _indent , isDisabled);
			indent += this.getIndent();
			
			if (itemType == null)
			{
				itemType = "object";
				this.insertComment("TODO: Find a more specific item type than object and/or prepare the elements of the array", indent);
				
			}
			addCode(itemType + "[] " + arrayName + " = " + transform(valueList, false) + ";", indent, isDisabled);
			
			valueList = arrayName;
		}
		else
		{
			itemType = "object";
			this.insertComment("TODO: Find a more specific item type than object and/or prepare the elements of the array", indent);
			valueList = transform(valueList, false);
		}

		// Creation of the loop header
		insertBlockHeading(_for, "foreach (" + itemType + " " + var + " in " +	valueList + ")", indent);

		// Add the loop body as is
		generateCode(_for.q, indent + this.getIndent());

		// Accomplish the loop
		insertBlockTail(_for, null, indent);

		if (items != null)
		{
			addCode("}", _indent, isDisabled);
		}
		
		return true;
	}
	// END KGU#61 2016-03-22

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
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			code.add("");
			subroutineInsertionLine = code.count();	// default position for subroutines
			subroutineIndent = _indent;
		}
		else
		{
			code.add("");
		}
		// END KGU#178 2016-07-20
		
		if (_root.isProgram==true) {
			code.add(_indent + "using System;");
			code.add(_indent + "");
			// START KGU 2015-10-18
			insertBlockComment(_root.getComment(), _indent, "/**", " * ", " */");
			// END KGU 2015-10-18

			insertBlockHeading(_root, "public class "+ _procName, _indent);
			code.add(_indent);
			insertComment("TODO: Declare and initialise class and member variables here", _indent + this.getIndent());
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
			// START KGU#178 2016-07-20: Enh. #160 - insert called subroutines as private
			//String fnHeader = "public static " + _resultType + " " + _procName + "(";
			String fnHeader = (topLevel ? "public" : "private") + " static "
					+ _resultType + " " + _procName + "(";
			// END KGU#178 2016-07-20
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
		super.generateFooter(_root, _indent + this.getIndent());

		if (_root.isProgram)
		{
			// START KGU#178 2016-07-20: Enh. #160
			// Modify the subroutine insertion position
			subroutineInsertionLine = code.count();
			// END KGU#178 2016-07-20
			
			// Close class block
			code.add("");
			code.add(_indent + "}");
		}
	}
	// END KGU 2015-12-15
    	
}

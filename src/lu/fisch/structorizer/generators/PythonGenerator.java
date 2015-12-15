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
 *      Author:         Daniel Spittank
 *
 *      Description:    This class generates Java code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                     Date		   Description
 *      ------			   ----		   -----------
 *      Bob Fisch                  2008.11.17      First Issue
 *      Gunter Schillebeeckx       2009.08.10      Java Generator starting from C Generator
 *      Bob Fisch                  2009.08.10      Update I/O
 *      Bob Fisch                  2009.08.17      Bugfixes (see comment)
 *      Kay Gürtzig                2010.09.10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch                  2011.11.07      Fixed an issue while doing replacements
 *      Daniel Spittank            2014.02.01      Python Generator starting from Java Generator
 *      Kay Gürtzig                2014.11.16      Conversion of C-like logical operators and arcus functions (see comment)
 *      Kay Gürtzig                2014.12.02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig                2015.10.18      Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig                2015.12.12      bugfix #59 (KGU#104) with respect to ER #10
 *
 ******************************************************************************************************
 *
 *      Comments:
 *
 *      2015.10.18 - Bugfixes and modifications (Kay Gürtzig)
 *      - Comment method signature simplified
 *      - Bugfix: The export option "export instructions as comments" had been ignored before
 *      - Bugfix KGU#54: generateCode(Repeat,String) ate the last two lines of the loop body!
 *      - The indentation logic was somehow inconsistent
 *
 *      2014.11.16 - Bugfixes / Enhancement
 *      - Conversion of C-style logical operators to the Python-conform ones added
 *      - assignment operator conversion now preserves or ensures surrounding spaces
 *      - workaround for reverse trigonometric functions added
 *      - Operator != had been converted to !==
 *      - comment export introduced

 *      2014.02.01 - First Version of Python Generator
 *      
 *      2010.09.10 - Bugfixes
 *      - condition for automatic bracket addition around condition expressions corrected
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator conversion
 *      - pascal function conversion
 *
 *      2009.08.10
 *        - writeln() => System.out.println()
 * 
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


public class PythonGenerator extends Generator 
	{
		
		/************ Fields ***********************/
		protected String getDialogTitle()
		{
			return "Export Python ...";
		}
		
		protected String getFileDescription()
		{
			return "Python Source Code";
		}
		
		protected String getIndent()
		{
			return "    ";
		}
		
		protected String[] getFileExtensions()
		{
			String[] exts = {"py"};
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
	    
		// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
		/**
		 * A pattern how to embed the variable (right-hand side of an input instruction)
		 * into the target code
		 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
		 */
		protected String getInputReplacer()
		{
			return "$1 = input(\"$1\")";
		}

		/**
		 * A pattern how to embed the expression (right-hand side of an output instruction)
		 * into the target code
		 * @return a regex replacement pattern, e.g. "System.out.println($1);"
		 */
		protected String getOutputReplacer()
		{
			return "print($1)";
		}

		/**
		 * Transforms assignments in the given intermediate-language code line.
		 * Replaces "<-" by "="
		 * @param _interm - a code line in intermediate syntax
		 * @return transformed string
		 */
		protected String transformAssignment(String _interm)
		{
			return _interm.replace(" <- ", " = ");
		}
		// END KGU#18/KGU#23 2015-11-01
	    
	// START KGU#18/KGU#23 2015-11-01: Obsolete    
//	    private String transform(String _input)
		/* (non-Javadoc)
		 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
		 */
		@Override
		protected String transform(String _input)
		{

			_input = super.transform(_input);

			String s = _input;

			// START KGU 2014-11-16: C comparison operator required conversion before logical ones
			_input=BString.replace(_input,"!="," <> ");
			// convert C logical operators
			_input=BString.replace(_input," && "," and ");
			_input=BString.replace(_input," || "," or ");
			_input=BString.replace(_input," ! "," not ");
			_input=BString.replace(_input,"&&"," and ");
			_input=BString.replace(_input,"||"," or ");
			_input=BString.replace(_input,"!"," not ");
			_input=BString.replace(_input,"!"," not ");
			_input=BString.replace(_input," xor "," ^ ");            
			// END KGU 2014-11-16

            // convert Pascal operators
			_input=BString.replace(_input," div "," / ");

			s = _input;
			// Math function
			s=s.replace("cos(", "math.cos(");
			s=s.replace("sin(", "math.sin(");
			s=s.replace("tan(", "math.tan(");
			// START KGU 2014-11-16: After the previous replacements the following 3 strings would never be found!
			//s=s.replace("acos(", "math.acos(");
			//s=s.replace("asin(", "math.asin(");
			//s=s.replace("atan(", "math.atan(");
			// This is just a workaround; A clean approach would require a genuine lexical scanning in advance
			s=s.replace("amath.cos(", "math.acos(");
			s=s.replace("amath.sin(", "math.asin(");
			s=s.replace("amath.tan(", "math.atan(");
			// END KGU 2014-11-16
			s=s.replace("abs(", "abs(");
			s=s.replace("round(", "round(");
			s=s.replace("min(", "min(");
			s=s.replace("max(", "max(");
			s=s.replace("ceil(", "math.ceil(");
			s=s.replace("floor(", "math.floor(");
			s=s.replace("exp(", "math.exp(");
			s=s.replace("log(", "math.log(");
			s=s.replace("sqrt(", "math.sqrt(");
			s=s.replace("pow(", "math.pow(");
			s=s.replace("toRadians(", "math.radians(");
			s=s.replace("toDegrees(", "math.degrees(");
			// clean up ... if needed
			//s=s.replace("Math.Math.", "math.");

			return s.trim();
		}
		
		protected void generateCode(Instruction _inst, String _indent)
		{
			if(!insertAsComment(_inst, _indent)) {
				// START KGU 2014-11-16
				insertComment(_inst, _indent);
				// END KGU 2014-11-16
				for(int i=0;i<_inst.getText().count();i++)
				{
					code.add(_indent+transform(_inst.getText().get(i)));
				}
			}
		}
		
		protected void generateCode(Alternative _alt, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_alt, _indent);
			// END KGU 2014-11-16

			String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
			if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";

			code.add(_indent+"if "+condition+":");
			generateCode((Subqueue) _alt.qTrue,_indent + this.getIndent());
			if(_alt.qFalse.getSize()!=0)
			{
				code.add(_indent+"else:");
				generateCode((Subqueue) _alt.qFalse, _indent + this.getIndent());
			}
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				code.add("");
			}
			// END KGU#54 2015-10-19
		}
		
		protected void generateCode(Case _case, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_case, _indent);
			// END KGU 2014-11-16

			StringList lines = _case.getText();
			String condition = transform(lines.get(0));

			for(int i=0; i<_case.qs.size()-1; i++)
			{
				String caseline = _indent + ((i == 0) ? "if" : "elif") + " (";
	    		// START KGU#15 2015-10-21: Support for multiple constants per branch
	    		StringList constants = StringList.explode(lines.get(i+1), ",");
	    		for (int j = 0; j < constants.count(); j++)
	    		{
	    			if (j > 0) caseline = caseline + " or ";
	    			caseline = caseline + "(" + condition + ") == " + constants.get(j).trim();
	    		}
	    		// END KGU#15 2015-10-21
				code.add(caseline + ") :");
				generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent());
			}
			
			if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
			{
				code.add(_indent + "else:");
				generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent + this.getIndent());
			}
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				code.add("");
			}
			// END KGU#54 2015-10-19
		}
		
		protected void generateCode(For _for, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_for, _indent);
			// END KGU 2014-11-16

			// START KGU#3/KGU#104 2015-12-12: ER #10 / Bugfix #59 - This was forgotten to fix
			//String startValueStr="";
			//String endValueStr="";
			//String stepValueStr="";
			//
			//String editStr = BString.replace(transform(_for.getText().getText()),"\n","").trim();
			//String[] word = editStr.split(" ");
			//int nbrWords = word.length;
			//String counterStr = word[0];
			//if ((nbrWords-1) >= 2) startValueStr = word[2];
			//if ((nbrWords-1) >= 4) endValueStr = word[4];
			//if ((nbrWords-1) >= 6) {
			//	stepValueStr = word[6]; 
			//}
			//else {
			//	stepValueStr = "1";
			//}
			String counterStr = _for.getCounterVar();
			String startValueStr = this.transform(_for.getStartValue());
			String endValueStr = this.transform(_for.getEndValue());
			String stepValueStr = _for.getStepString();
			// END KGU#3/KGU#104 2015-12-12
			code.add(_indent+"for "+counterStr+" in range("+startValueStr+", "+endValueStr+", "+stepValueStr+"):");
			generateCode((Subqueue) _for.q,_indent + this.getIndent());
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				code.add("");
			}
			// END KGU#54 2015-10-19
		}
		
		protected void generateCode(While _while, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_while, _indent);
			// END KGU 2014-11-16
			
			String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
			if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
			
			code.add(_indent+"while "+condition+":");
			generateCode((Subqueue) _while.q, _indent + this.getIndent());
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				code.add("");
			}
			// END KGU#54 2015-10-19
		}

        protected void generateCode(Repeat _repeat, String _indent)
        {
			// START KGU 2014-11-16
			insertComment(_repeat, _indent);
			// END KGU 2014-11-16
            code.add(_indent+"while True:");
            generateCode((Subqueue) _repeat. q,_indent + this.getIndent());
            // START KGU#54 2015-10-19: Why should the last two rows be empty? They aren't! This strange behaviour ate code lines! 
            //code.delete(code.count()-1); // delete empty row
            //code.delete(code.count()-1); // delete empty row
            // END KGU#54 2015-10-19
            code.add(_indent+this.getIndent()+"if "+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+":");
            code.add(_indent+this.getIndent()+this.getIndent()+"break");
			// START KGU#54 2015-10-19: Add an empty line, but void accumulation of empty lines!
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				code.add("");
			}
			// END KGU#54 2015-10-19
        }
		
		protected void generateCode(Forever _forever, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_forever, _indent);
			// END KGU 2014-11-16
			code.add(_indent+"while True:");
			generateCode((Subqueue) _forever.q, _indent + this.getIndent());
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				code.add("");
			}
			// END KGU#54 2015-10-19
		}
		
		protected void generateCode(Call _call, String _indent)
		{
			if(!insertAsComment(_call, _indent))
			{
				// START KGU 2014-11-16
				insertComment(_call, _indent);
				// END KGU 2014-11-16
				for(int i=0;i<_call.getText().count();i++)
				{
					code.add(_indent+transform(_call.getText().get(i))+"()");
				}
			}
		}
		
		protected void generateCode(Jump _jump, String _indent)
		{
			if(!insertAsComment(_jump, _indent))
			{
				// START KGU 2014-11-16
				insertComment(_jump, _indent);
				// END KGU 2014-11-16
				for(int i=0;i<_jump.getText().count();i++)
				{
					insertComment(transform(_jump.getText().get(i))+" # FIXME goto instructions not allowed in Python", _indent);
				}
			}
		}
		
		// START KGU#18/KGU#23 2015-11-02: Use inherited method
//		protected void generateCode(Subqueue _subqueue, String _indent)
//		{
//			// code.add(_indent+"");
//			for(int i=0;i<_subqueue.getSize();i++)
//			{
//				generateCode((Element) _subqueue.getElement(i),_indent);
//			}
//			// code.add(_indent+"");
//		}
		// END KGU#18/KGU#23 2015-11-02
		
		public String generateCode(Root _root, String _indent)
		{
			if(_root.isProgram==true) {
				code.add("#!/usr/bin/env python");
				insertComment(_root.getText().get(0), _indent);
				code.add("");
		        // START KGU 2015-10-18
				//code.add("\"\"\"This script ...\"\"\"");
		        insertComment(_root, "");
		        // END KGU 2015-10-18
				code.add("");
					
				Subqueue _subqueue = _root.children;
				for(int i=0;i<_subqueue.getSize();i++) {
					generateCode((Element) _subqueue.getElement(i),"");
				}
				
				code.add("");
			}
			else {
				code.add("def "+_root.getText().get(0)+"() :");
		        // START KGU 2014-11-16
				//code.add(this.getIndent()+"\"\"\"This method ...\"\"\"");
		        insertComment(_root, this.getIndent());
		        // END KGU 2014-11-16

				generateCode(_root.children,"");
				code.add("");
			}
			
			return code.getText();
		}
		
	}

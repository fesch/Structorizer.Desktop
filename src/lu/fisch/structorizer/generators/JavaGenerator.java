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
 *      Kay Gürtzig                2014.10.22      Workarounds and Enhancements (see comment)
 *      Kay Gürtzig                2014.11.16      Several fixes and enhancements (see comment)
 *      Kay Gürtzig                2015.10.18      Comment generation and indentation revised
 *      Kay Gürtzig                2015.11.01      Preprocessing reorganised, FOR loop and CASE enhancements
 *      Kay Gürtzig                2015.12.12      Enh. #54 (KGU#101): Support for output expression lists
*
 ******************************************************************************************************
 *
 *      Comments:
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU#23)
 *      - Enhancement #10 (KGU#3): FOR loops now provide more reliable loop parameters 
 *      - Enhancement KGU#15: Support for the gathering of several case values in CASE instructions
 *      
 *      2015-10-18 - Bugfix / Code revision
 *      - Indentation increment with +_indent.substring(0,1) worked only for single-character indentation units
 *      - Interface of comment insertion methods modified
 *      
 *      2014.11.16 - Bugfixes
 *      - conversion of comparison and logical operators had still been flawed
 *      - comment generation unified by new inherited generic method insertComment 
 *      
 *      2014.10.22 - Bugfixes / Enhancement
 *      - Replacement for asin(), acos(), atan() hadn't worked.
 *      - Support for logical operators "and", "or", and "not"
 *      
 *      2010.09.10 - Bugfixes
 *      - condition for automatic bracket addition around condition expressions corrected
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator convertion
 *      - pascal function convertion
 *
 *      2009.08.10
 *        - writeln() => System.out.println()
 * 
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


public class JavaGenerator extends Generator 
	{
		
		/************ Fields ***********************/
		protected String getDialogTitle()
		{
			return "Export Java ...";
		}
		
		protected String getFileDescription()
		{
			return "Java Source Code";
		}
		
		protected String getIndent()
		{
			return "\t";
		}
		
		protected String[] getFileExtensions()
		{
			String[] exts = {"java"};
			return exts;
		}
		
	    // START KGU 2015-10-18: New pseudo field
	    @Override
	    protected String commentSymbolLeft()
	    {
	    	return "//";
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
			return "$1 = (new Scanner(System.in)).nextLine()";
		}

		/**
		 * A pattern how to embed the expression (right-hand side of an output instruction)
		 * into the target code
		 * @return a regex replacement pattern, e.g. "System.out.println($1);"
		 */
		protected String getOutputReplacer()
		{
			return "System.out.println($1)";
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
		
	    @Override
	    protected String transform(String _input)
	    {
			// START KGU#101 2015-12-12: Enh. #54 - support lists of expressions
			if (_input.matches("^" + D7Parser.output.trim() + "[ ](.*?)"))
			{
				StringList expressions = 
						Element.splitExpressionList(_input.substring(D7Parser.output.trim().length()), ",");
				// Some of the expressions might be sums, so better put parentheses around them
				_input = D7Parser.output.trim() + " (" + expressions.getText().replace("\n", ") + (") + ")";
			}
			// END KGU#101 2015-12-12

			// START KGU#18/KGU#23 2015-11-01: This can now be inherited
			String s = super.transform(_input).replace(" div "," / ");
			// END KGU#18/KGU#23 2015-11-01
                        
			// Math function
			s=s.replace("cos(", "Math.cos(");
			s=s.replace("sin(", "Math.sin(");
			s=s.replace("tan(", "Math.tan(");
			// START KGU 2014-10-22: After the previous replacements the following 3 strings would never be found!
			//s=s.replace("acos(", "Math.acos(");
			//s=s.replace("asin(", "Math.asin(");
			//s=s.replace("atan(", "Math.atan(");
			// This is just a workaround; A clean approach would require a genuine lexical scanning in advance
			s=s.replace("aMath.cos(", "Math.acos(");
			s=s.replace("aMath.sin(", "Math.asin(");
			s=s.replace("aMath.tan(", "Math.atan(");
			// END KGU 2014-10-22:
			s=s.replace("abs(", "Math.abs(");
			s=s.replace("round(", "Math.round(");
			s=s.replace("min(", "Math.min(");
			s=s.replace("max(", "Math.max(");
			s=s.replace("ceil(", "Math.ceil(");
			s=s.replace("floor(", "Math.floor(");
			s=s.replace("exp(", "Math.exp(");
			s=s.replace("log(", "Math.log(");
			s=s.replace("sqrt(", "Math.sqrt(");
			s=s.replace("pow(", "Math.pow(");
			s=s.replace("toRadians(", "Math.toRadians(");
			s=s.replace("toDegrees(", "Math.toDegrees(");
			// clean up ... if needed
			s=s.replace("Math.Math.", "Math.");

			return s.trim();
	    }
		
		
		protected void generateCode(Instruction _inst, String _indent)
		{
	    	// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
//			insertComment(_inst, _indent);
//			for(int i=0;i<_inst.getText().count();i++)
//			{
//				code.add(_indent+transform(_inst.getText().get(i))+";");
//			}
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
	        
	        String condition = transform(_alt.getText().getLongString()).trim();
	        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
	        
	        code.add(_indent+"if " + condition + " {");
			generateCode(_alt.qTrue, _indent+this.getIndent());
			if(_alt.qFalse.getSize()!=0)
			{
				code.add(_indent + "}");
				code.add(_indent + "else {");
				generateCode(_alt.qFalse, _indent+this.getIndent());
			}
			code.add(_indent+"}");
		}
		
		protected void generateCode(Case _case, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_case, _indent);
	        // END KGU 2014-11-16

	        StringList lines = _case.getText();
	        String condition = transform(lines.get(0));
	        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";

	        code.add(_indent+"switch "+condition+" {");
			
			for(int i=0;i<_case.qs.size()-1;i++)
			{
	    		// START KGU#15 2015-10-21: Support for multiple constants per branch
	    		StringList constants = StringList.explode(lines.get(i+1), ",");
	    		for (int j = 0; j < constants.count(); j++)
	    		{
	    			code.add(_indent + "case " + constants.get(j).trim() + ":");
	    		}
	    		// END KGU#15 2015-10-21
				generateCode((Subqueue) _case.qs.get(i),_indent + this.getIndent());
				code.add(_indent + this.getIndent() + "break;\n");
			}
			
			if(!lines.get(_case.qs.size()).trim().equals("%"))
			{
				code.add(_indent + "default:");
				generateCode((Subqueue) _case.qs.get(_case.qs.size()-1), _indent + this.getIndent());
				code.add(_indent + this.getIndent() + "break;");
			}
			code.add(_indent + "}");
		}
		
		protected void generateCode(For _for, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_for, _indent);
	        // END KGU 2014-11-16

			// START KGU#3 2015-11-01: The For element itself provides us with reliable splitting
	    	String var = _for.getCounterVar();
	    	int step = _for.getStepConst();
	    	String compOp = (step > 0) ? " <= " : " >= ";
	    	String increment = var + " += (" + step + ")";
	    	code.add(_indent + "for (" +
	    			var + " = " + transform(_for.getStartValue(), false) + "; " +
	    			var + compOp + transform(_for.getEndValue(), false) + "; " +
	    			increment +
	    			") {");
	    	// END KGU#3 2015-11-01
	    	generateCode(_for.q, _indent + this.getIndent());
	    	code.add(_indent + "}");
		}
		
		protected void generateCode(While _while, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_while, _indent);
	        // END KGU 2014-11-16

	        String condition = transform(_while.getText().getLongString(), false).trim();
	        if (!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
	        
	        code.add(_indent+"while " + condition + " {");
			generateCode(_while.q, _indent+this.getIndent());
			code.add(_indent+"}");
		}
		
		@Override
		// version of Kay Gürtzig 
		protected void generateCode(Repeat _repeat, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_repeat, _indent);
			// END KGU 2014-11-16

			code.add(_indent + "do {");
			generateCode(_repeat.q, _indent + this.getIndent());
			code.add(_indent + "} while (!(" + transform(_repeat.getText().getLongString(), false).trim() + "));");
		}
		
		protected void generateCode(Forever _forever, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_forever, _indent);
	        // END KGU 2014-11-16

	        code.add(_indent + "while (true) {");
			generateCode(_forever.q, _indent+this.getIndent());
			code.add(_indent + "}");
		}
		
		protected void generateCode(Call _call, String _indent)
		{
	    	// START KGU 2015-11-01: The "export instructions as comments" configuration had been ignored here
//			insertComment(_call, _indent);
//			for(int i=0;i<_call.getText().count();i++)
//			{
//				code.add(_indent+transform(_call.getText().get(i))+"();");
//			}
			if (!insertAsComment(_call, _indent)) {
				
				insertComment(_call, _indent);

				StringList lines = _call.getText();
				for (int i = 0; i < lines.count(); i++)
				{
					String line = _call.getText().get(i);
					// KGU 2015-11-01 It was of little use, always to append a parenthesis pair
					if (!line.endsWith(")")) line = line + "()";
					// Input or Output should not occur here
					code.add(_indent+transform(line, false) + ";");
				}

			}
			// END KGU 2015-10-01
		}
		
		protected void generateCode(Jump _jump, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_jump, _indent);
	        // END KGU 2014-11-16

			// KGU 2015-10-18: In case of an empty text generate a break instruction by default.
			boolean isEmpty = true;
			StringList lines = _jump.getText();
			for (int i = 0; i < lines.count(); i++)
			{
				String line = transform(lines.get(i), false);
				if (!line.trim().isEmpty()) isEmpty = false;
				code.add(_indent + line + ";\t// FIXME goto instructions not allowed in Java");
			}
			if (isEmpty)
			{
				code.add(_indent + "break;");
			}
		}
		
		public String generateCode(Root _root, String _indent)
		{
			if(_root.isProgram==true) {
				code.add("import java.util.Scanner;");
				code.add("");
				// START KGU 2015-10-18
				insertBlockComment(_root.getComment(), "", "/**", " * ", " */");
				// END KGU 2014-10-18
				code.add("public class "+_root.getText().get(0)+" {");
				code.add("");
				insertComment("TODO Declare and initialise class variables here", this.getIndent());
				code.add("");
				code.add(this.getIndent() + "/**");
				code.add(this.getIndent() + " * @param args");
				code.add(this.getIndent() + " */");
				code.add(this.getIndent() + "public static void main(String[] args) {");
				insertComment("TODO Declare local variables here", this.getIndent() + this.getIndent());
				code.add(this.getIndent() + this.getIndent() + "");
				insertComment("TODO Initialise local variables here", this.getIndent() + this.getIndent());
				code.add(this.getIndent()+ this.getIndent() +"");
				generateCode(_root.children, this.getIndent()+this.getIndent());
				code.add(this.getIndent()+"}");
				code.add("");
				code.add("}");
			}
			else {
		        // START KGU 2014-10-18
		        insertBlockComment(_root.getComment(), _indent+this.getIndent(), "/**", " * ", null);
		        insertBlockComment(_root.getParameterNames(), _indent+this.getIndent(), null, " * @param ", " */");
				code.add(_indent+this.getIndent()+"public static void "+_root.getText().get(0)+" {");
				insertComment("TODO declare parameters and local variables!", _indent+this.getIndent()+this.getIndent());
				code.add("");
				generateCode(_root.children,_indent+this.getIndent()+this.getIndent());
				code.add("");
				code.add(_indent+this.getIndent()+"}");
			}
			
			return code.getText();
		}
		
	}

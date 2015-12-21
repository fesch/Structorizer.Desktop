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

package lu.fisch.structorizer.generators._old;

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
 *
 ******************************************************************************************************
 *
 *      Comment:		
 *      2010.08.07 - Bugfixes
 *      - none
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.generators.Generator;


public class CSharpGenerator extends Generator 
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
		
		protected String getIndent()
		{
			return "\t";
		}
		
		protected String[] getFileExtensions()
		{
			String[] exts = {"cs"};
			return exts;
		}

	    // START KGU 2015-10-18: New pseudo field
	    protected String commentSymbolLeft()
	    {
	    	return "//";
	    }
	    // END KGU 2015-10-18

		// START KGU#78 2015-12-18: Enh. #23 (added for compilability, not actually used)
		/* (non-Javadoc)
		 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
		 */
		@Override
		protected boolean supportsSimpleBreak()
		{
			return true;
		}
		// END KGU#78 2015-12-18

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
			return "Console.ReadLine($1);";
		}

		/**
		 * A pattern how to embed the expression (right-hand side of an output instruction)
		 * into the target code
		 * @return a regex replacement pattern, e.g. "System.out.println($1);"
		 */
		protected String getOutputReplacer()
		{
			return "Console.WriteLine($1);";
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

		protected String transform(String _input)
		{
			// et => and
			// ou => or
			// lire => readln()
			// écrire => Console.WriteLine()
			// tant que => ""
			// pour => ""
			// jusqu'à => ""
			// à => "to"
			
			// variable assignment
			_input=BString.replace(_input," <- "," = ");
			_input=BString.replace(_input,"<- "," = ");
			_input=BString.replace(_input," <-"," = ");
			_input=BString.replace(_input,"<-"," = ");
			
                    // convert Pascal operators
                    _input=BString.replace(_input," mod "," % ");
                    _input=BString.replace(_input," div "," / ");

                    if(!D7Parser.preAlt.equals("")){_input=BString.replace(_input,D7Parser.preAlt,"");}
                    if(!D7Parser.postAlt.equals("")){_input=BString.replace(_input,D7Parser.postAlt,"");}
                    if(!D7Parser.preCase.equals("")){_input=BString.replace(_input,D7Parser.preCase,"");}
                    if(!D7Parser.postCase.equals("")){_input=BString.replace(_input,D7Parser.postCase,"");}
                    if(!D7Parser.preFor.equals("")){_input=BString.replace(_input,D7Parser.preFor,"");}
                    if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"to");}
                    if(!D7Parser.preWhile.equals("")){_input=BString.replace(_input,D7Parser.preWhile,"");}
                    if(!D7Parser.postWhile.equals("")){_input=BString.replace(_input,D7Parser.postWhile,"");}
                    if(!D7Parser.preRepeat.equals("")){_input=BString.replace(_input,D7Parser.preRepeat,"");}
                    if(!D7Parser.postRepeat.equals("")){_input=BString.replace(_input,D7Parser.postRepeat,"");}

                    /*Regex r;
                     r = new Regex(BString.breakup(D7Parser.input)+"[ ](.*?)","readln($1)"); _input=r.replaceAll(_input);
                     r = new Regex(BString.breakup(D7Parser.output)+"[ ](.*?)","writeln($1)"); _input=r.replaceAll(_input);
                     r = new Regex(BString.breakup(D7Parser.input)+"(.*?)","readln($1)"); _input=r.replaceAll(_input);
                     r = new Regex(BString.breakup(D7Parser.output)+"(.*?)","writeln($1)"); _input=r.replaceAll(_input);*/

                    // TODO syntax to be verified!!!
                    if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input+" ")>=0){_input=BString.replace(_input,D7Parser.input+" ","Console.ReadLine(")+")";}
                    if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output+" ")>=0){_input=BString.replace(_input,D7Parser.output+" ","Console.WriteLine(")+")";}
                    if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input)>=0){_input=BString.replace(_input,D7Parser.input,"Console.ReadLine(")+")";}
                    if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output)>=0){_input=BString.replace(_input,D7Parser.output,"Console.WriteLine(")+")";}

                    return _input.trim();
		}
		
		// Instruction
		protected void generateCode(Instruction _inst, String _indent)
		{
			for(int i=0;i<_inst.getText().count();i++)
			{
				code.add(_indent+transform(_inst.getText().get(i))+";");
			}
		}
		
		// IF statement
		protected void generateCode(Alternative _alt, String _indent)
		{
                        String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
                        if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

			code.add(_indent+"if "+condition+"");
			code.add(_indent+"{");
			generateCode(_alt.qTrue,_indent+_indent.substring(0,1));
			if(_alt.qFalse.getSize()!=0)
			{
				code.add(_indent+"}");
				code.add(_indent+"else");
				code.add(_indent+"{");
				generateCode(_alt.qFalse,_indent+_indent.substring(0,1));
			}
			code.add(_indent+"}");
		}
		
		// CASE statement
		protected void generateCode(Case _case, String _indent)
		{
                        String condition = transform(_case.getText().get(0));
                        if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

                        code.add(_indent+"switch "+condition+" ");
			code.add(_indent+"{");
			
			for(int i=0;i<_case.qs.size()-1;i++)
			{
				code.add(_indent+_indent.substring(0,1)+_case.getText().get(i+1).trim()+":");
				generateCode((Subqueue) _case.qs.get(i),_indent+_indent.substring(0,1)+_indent.substring(0,1)+_indent.substring(0,1));
				code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1)+"break;");
			}
			
			if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
			{
				code.add(_indent+_indent.substring(0,1)+"default:");
				generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+_indent.substring(0,1)+_indent.substring(0,1));
				code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1)+"break;");
			}
			code.add(_indent+_indent.substring(0,1)+"}");
		}
		
		// TODO FOREACH loop
		// Same diagram layout as FOR loop, but different control structure
		// NSD-string: typeStr itemStr IN setStr // Java, C#
		// NSD-string: itemStr IN setStr // Perl
		// Java: for (type item: set) {...}
		// C#: foreach (type item in set) {...}
		// Perl: foreach $item ($set) {...}
		// C++: doesn't exist
		// C: doesn't exist
		// preparation of 2010-08-07 Gunter Schillebeeckx
//		protected void generateCode(Foreach _foreach, String _indent)
//		{
//			String editStr = BString.replace(transform(_foreach.getText().getText()),"\n","").trim();
//			String[] word = editStr.split(" ");
//			int nbrWords = word.length;
//			if (nbrWords > 3) { // Java, C#
//				String typeStr = word[0];
//				String itemStr= word[1]; 
//				String setStr = word[3]; 
//			}
//			else { // Perl
//				String itemStr= word[0]; 
//				String setStr = word[2]; 
//			}
//		
//			// Java only
//			code.add(_indent+"for ("+typeStr+" "+itemStr+": "+setStr+") {");
//			// C# only
//			code.add(_indent+"foreach ("+typeStr+" "+itemStr+" in "+setStr+") {");
//			// Perl only
//			code.add(_indent+"foreach $"+itemStr+" ($"+setStr+") {");
//		
//			generateCode(_foreach.q,_indent+_indent.substring(0,1));
//			code.add(_indent+"}");
//		}

		// FOR loop
		// NSD-string: counterStr <- startValueStr TO endValueStr STEP stepValueStr
		protected void generateCode(For _for, String _indent)
		{
			String startValueStr="";
			String endValueStr="";
			String stepValueStr="";
			String editStr = BString.replace(transform(_for.getText().getText()),"\n","").trim();
			String[] word = editStr.split(" ");
			int nbrWords = word.length;
			String counterStr = word[0];
			if ((nbrWords-1) >= 2) startValueStr = word[2];
			if ((nbrWords-1) >= 4) endValueStr = word[4];
			if ((nbrWords-1) >= 6) {
				stepValueStr = word[6]; 
			}
			else {
				stepValueStr = "1";
			}
			code.add(_indent+"for ("+
					counterStr+" = "+startValueStr+"; "+counterStr+" <= "+endValueStr+"; "+counterStr+" = "+counterStr+" + ("+stepValueStr+") "+
					") {");
			generateCode(_for.q,_indent+_indent.substring(0,1));
			code.add(_indent+"}");
		}
		
		// WHILE loop
		protected void generateCode(While _while, String _indent)
		{
                        String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
                        if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

			code.add(_indent+"while "+condition+" ");
			code.add(_indent+"{");
			generateCode(_while.q,_indent+_indent.substring(0,1));
			code.add(_indent+"}");
		}
		
		// REPEAT loop
		protected void generateCode(Repeat _repeat, String _indent)
		{
			code.add(_indent+"do");
			code.add(_indent+"{");
			generateCode(_repeat.q,_indent+_indent.substring(0,1));
			code.add(_indent+"}");
			code.add(_indent+"while !("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+");");
		}
		
		// ENDLESS loop
		protected void generateCode(Forever _forever, String _indent)
		{
			code.add(_indent+"while (true)");
			code.add(_indent+"{");
			generateCode(_forever.q,_indent+_indent.substring(0,1));
			code.add(_indent+"}");
		}
		
		// CALL
		protected void generateCode(Call _call, String _indent)
		{
			for(int i=0;i<_call.getText().count();i++)
			{
				code.add(_indent+transform(_call.getText().get(i))+";");
			}
		}
		
		// JUMP
		protected void generateCode(Jump _jump, String _indent)
		{
			for(int i=0;i<_jump.getText().count();i++)
			{
				code.add(_indent+"goto "+transform(_jump.getText().get(i))+"; // jump-instruction not recommended");

			}
		}
		
		// nested structure 
		protected void generateCode(Subqueue _subqueue, String _indent)
		{
			// code.add(_indent+"");
			for(int i=0;i<_subqueue.getSize();i++)
			{
				generateCode((Element) _subqueue.getElement(i),_indent);
			}
			// code.add(_indent+"");
		}
		
		public String generateCode(Root _root, String _indent)
		{
			if(_root.isProgram==true) {
                code.add("using System;");
                code.add("");
				code.add("public class "+_root.getText().get(0)+" {");
				code.add("");
				code.add(_indent+"// Declare and initialise global variables here");
				code.add("");
				code.add(_indent+"/**");
				code.add(_indent+" * @param args");
				code.add(_indent+" */");
				code.add(_indent+"public static void Main(string[] args) {");
				code.add(_indent+_indent+"// Declare local variables here");
				code.add(_indent+_indent+"");
				code.add(_indent+_indent+"// Initialise local variables here");
				code.add(_indent+_indent+"");
				generateCode(_root.children,_indent+_indent);
				code.add(_indent+"}");
				code.add("");
				code.add("}");
				}
			else {
				code.add(_indent+"/**");
				code.add(_indent+" * This method ...");
				code.add(_indent+" */");
				code.add(_indent+"private static void "+_root.getText().get(0)+"() {");
				code.add(_indent+_indent+"// Declare local variables here");
				code.add("");
				generateCode(_root.children,_indent+_indent);
				code.add("");
				code.add(_indent+"}");
			}
			
			return code.getText();
		}
		
		
	}

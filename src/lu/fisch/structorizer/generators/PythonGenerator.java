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
 *
 ******************************************************************************************************
 *
 *      Comments:
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
		
		/************ Code Generation **************/
		private String transform(String _input)
		{
			// et => and
			// ou => or
			// lire => readln()
			// écrire => writeln()
			// tant que => ""
			// pour => ""
			// jusqu'à => ""
			// à => "to"

                        String s = _input;
                        
                        // variable assignment
                        // START KGU 2014-12-02: To achieve consistency with operator highlighting
                        s=s.replace("<--", "<-");
                        // END KGU 2014-12-02
                        s=s.replace(":=", "<-");
                        
                        // testing
                        // START KGU 2014-11-16: Otherwise this would end as "!=="
                		s=s.replace("!=", "<>");
                		// END 2014-11-16
                        s=s.replace("=", "==");
                        s=s.replace("<==", "<=");
                        s=s.replace(">==", ">=");
                        s=s.replace("<>", "!=");
                        _input=s;

                        // variable assignment
			_input=BString.replace(_input," <- "," = ");
			_input=BString.replace(_input,"<- "," = ");
			_input=BString.replace(_input," <-"," = ");
			_input=BString.replace(_input,"<-"," = ");

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
                        _input=BString.replace(_input," mod "," % ");
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

                        _input = s;

            StringList empty = new StringList();
            empty.addByLength(D7Parser.preAlt);
            empty.addByLength(D7Parser.postAlt);
            empty.addByLength(D7Parser.preCase);
            empty.addByLength(D7Parser.postCase);
            empty.addByLength(D7Parser.preFor);
            empty.addByLength(D7Parser.postFor);
            empty.addByLength(D7Parser.preWhile);
            empty.addByLength(D7Parser.postWhile);
            empty.addByLength(D7Parser.postRepeat);
            empty.addByLength(D7Parser.preRepeat);
            //System.out.println(empty);
            for(int i=0;i<empty.count();i++)
            {
                _input=BString.replace(_input,empty.get(i),"");
                //System.out.println(i);
            }
            if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"to");}

            
/*
			if(!D7Parser.preAlt.equals("")){_input=BString.replace(_input,D7Parser.preAlt,"");}
			if(!D7Parser.postAlt.equals("")){_input=BString.replace(_input,D7Parser.postAlt,"");}
			if(!D7Parser.preCase.equals("")){_input=BString.replace(_input,D7Parser.preCase,"");}
			if(!D7Parser.postCase.equals("")){_input=BString.replace(_input,D7Parser.postCase,"");}
			if(!D7Parser.preFor.equals("")){_input=BString.replace(_input,D7Parser.preFor,"");}
//			if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"to");}
			if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"");}
			if(!D7Parser.preWhile.equals("")){_input=BString.replace(_input,D7Parser.preWhile,"");}
			if(!D7Parser.postWhile.equals("")){_input=BString.replace(_input,D7Parser.postWhile,"");}
			if(!D7Parser.preRepeat.equals("")){_input=BString.replace(_input,D7Parser.preRepeat,"");}
			if(!D7Parser.postRepeat.equals("")){_input=BString.replace(_input,D7Parser.postRepeat,"");}
*/			
			/*Regex r;
			 r = new Regex(BString.breakup(D7Parser.input)+"[ ](.*?)","readln($1)"); _input=r.replaceAll(_input);
			 r = new Regex(BString.breakup(D7Parser.output)+"[ ](.*?)","writeln($1)"); _input=r.replaceAll(_input);
			 r = new Regex(BString.breakup(D7Parser.input)+"(.*?)","readln($1)"); _input=r.replaceAll(_input);
			 r = new Regex(BString.breakup(D7Parser.output)+"(.*?)","writeln($1)"); _input=r.replaceAll(_input);*/
			
			
			//if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input+" ")>=0){_input=BString.replace(_input,D7Parser.input+" ","")+" = input()";}
			//if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output+" ")>=0){_input=BString.replace(_input,D7Parser.output+" ","print(")+")";}
			if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input)>=0){_input=BString.replace(_input,D7Parser.input,"")+" = input('Eingabe: ')";}
			if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output)>=0){_input=BString.replace(_input,D7Parser.output,"print(")+")";}
			
			return _input.trim();
		}
		
		protected void generateCode(Instruction _inst, String _indent)
		{
			if(!insertAsComment(_inst, _indent, "#")) {
				// START KGU 2014-11-16
				insertComment(_inst, _indent, "# ");
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
			insertComment(_alt, _indent, "# ");
			// END KGU 2014-11-16

			String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
			if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";

			code.add(_indent+"if "+condition+":");
			generateCode((Subqueue) _alt.qTrue,_indent);
			if(_alt.qFalse.getSize()!=0)
			{
				code.add(_indent+"else:");
				generateCode((Subqueue) _alt.qFalse, _indent);
			}
			code.add("");
		}
		
		protected void generateCode(Case _case, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_case, _indent, "# ");
			// END KGU 2014-11-16

			String condition = transform(_case.getText().get(0));

			code.add(_indent+"if ("+condition+" == "+_case.getText().get(1).trim()+"):");
			generateCode((Subqueue) _case.qs.get(0),_indent);
			
			for(int i=1;i<_case.qs.size()-1;i++)
			{ 
				code.add(_indent+"elif ("+condition+" == "+_case.getText().get(i+1).trim()+"):");
				generateCode((Subqueue) _case.qs.get(i),_indent);
			}
			
			if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
			{
				code.add(_indent+"else:");
				generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent);
			}
			code.add("");
		}
		
		protected void generateCode(For _for, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_for, _indent, "# ");
			// END KGU 2014-11-16

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
			code.add(_indent+"for "+counterStr+" in range("+startValueStr+", "+endValueStr+", "+stepValueStr+"):");
			generateCode((Subqueue) _for.q,_indent);
			code.add("");
		}
		
		protected void generateCode(While _while, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_while, _indent, "# ");
			// END KGU 2014-11-16
			
			String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
			if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
			
			code.add(_indent+"while "+condition+":");
			generateCode((Subqueue) _while.q,_indent);
			code.add("");
		}

        protected void generateCode(Repeat _repeat, String _indent)
        {
			// START KGU 2014-11-16
			insertComment(_repeat, _indent, "# ");
			// END KGU 2014-11-16
            code.add(_indent+"while True:");
            generateCode((Subqueue) _repeat.q,_indent);
            code.delete(code.count()-1); // delete empty row
            code.delete(code.count()-1); // delete empty row
            code.add(_indent+this.getIndent()+"if "+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+":");
            code.add(_indent+this.getIndent()+this.getIndent()+"break");
        }
		
		protected void generateCode(Forever _forever, String _indent)
		{
			// START KGU 2014-11-16
			insertComment(_forever, _indent, "# ");
			// END KGU 2014-11-16
			code.add(_indent+"while True:");
			generateCode((Subqueue) _forever.q,_indent);
			code.add("");
		}
		
		protected void generateCode(Call _call, String _indent)
		{
			if(!insertAsComment(_call, _indent, "#"))
			{
				// START KGU 2014-11-16
				insertComment(_call, _indent, "# ");
				// END KGU 2014-11-16
				for(int i=0;i<_call.getText().count();i++)
				{
					code.add(_indent+transform(_call.getText().get(i))+"()");
				}
			}
		}
		
		protected void generateCode(Jump _jump, String _indent)
		{
			if(!insertAsComment(_jump, _indent, "#"))
			{
				// START KGU 2014-11-16
				insertComment(_jump, _indent, "# ");
				// END KGU 2014-11-16
				for(int i=0;i<_jump.getText().count();i++)
				{
					code.add(_indent+"# "+transform(_jump.getText().get(i))+" # goto-instruction not allowed in Python");
				}
			}
		}
		
		protected void generateCode(Subqueue _subqueue, String _indent)
		{
			// code.add(_indent+"");
			for(int i=0;i<_subqueue.children.size();i++)
			{
				generateCode((Element) _subqueue.children.get(i),_indent+this.getIndent());
			}
			// code.add(_indent+"");
		}
		
		public String generateCode(Root _root, String _indent)
		{
			if(_root.isProgram==true) {
				code.add("#!/usr/bin/env python");
				code.add("# "+_root.getText().get(0));
				code.add("");
		        // START KGU 2014-11-16
				//code.add("\"\"\"This script ...\"\"\"");
		        insertComment(_root, "", "# ");
		        // END KGU 2014-11-16
				code.add("");
					
				Subqueue _subqueue = _root.children;
				for(int i=0;i<_subqueue.children.size();i++) {
					generateCode((Element) _subqueue.children.get(i),"");
				}
				
				code.add("");
			}
			else {
				code.add("def "+_root.getText().get(0)+"() :");
		        // START KGU 2014-11-16
				//code.add(this.getIndent()+"\"\"\"This method ...\"\"\"");
		        insertComment(_root, this.getIndent(), "# ");
		        // END KGU 2014-11-16

				generateCode(_root.children,"");
				code.add("");
			}
			
			return code.getText();
		}
		
	}

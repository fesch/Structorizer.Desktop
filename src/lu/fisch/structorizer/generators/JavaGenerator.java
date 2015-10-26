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
 *
 ******************************************************************************************************
 *
 *      Comments:
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
                        s=s.replace("==", "=");
                        // START KGU 2014-11-16: Otherwise this would end as "!=="
                		s=s.replace("!=", "<>");
                		// END KGU 2014-11-16
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

                        // convert Pascal operators
                        _input=BString.replace(_input," mod "," % ");
                        _input=BString.replace(_input," div "," / ");
                        // START KGU 2014-10-22: And these, too...
                        _input=BString.replace(_input," and "," && ");
                        _input=BString.replace(_input," or "," || ");
                        _input=BString.replace(_input," not "," !");
                        // START KGU 2014-11-16: Was too simple in the first place, but now it's clumsy...
                        _input=BString.replace(_input,"(not ", "(!");
                        _input=BString.replace(_input," not(", " !(");
                        _input=BString.replace(_input,"(not(", "(!(");
                       	if (_input.startsWith("not ") || _input.startsWith("not(")) {
                       		_input = "!" + _input.substring(3);
                       	}
                        _input=BString.replace(_input," xor "," ^ ");	// Might cause some operator preference trouble
                        // END KGU 2014-11-16
                        // END KGU 2014-10-22

                        s = _input;
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
			
			
			if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input+" ")>=0){_input=BString.replace(_input,D7Parser.input+" ","")+" = (new Scanner(System.in)).nextLine()";}
			if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output+" ")>=0){_input=BString.replace(_input,D7Parser.output+" ","System.out.println(")+")";}
			if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input)>=0){_input=BString.replace(_input,D7Parser.input,"")+" = (new Scanner(System.in)).nextLine()";}
			if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output)>=0){_input=BString.replace(_input,D7Parser.output,"System.out.println(")+")";}
			
			return _input.trim();
		}
		
		protected void generateCode(Instruction _inst, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_inst, _indent, "// ");
	        // END KGU 2014-11-16
			for(int i=0;i<_inst.getText().count();i++)
			{
				code.add(_indent+transform(_inst.getText().get(i))+";");
			}
		}
		
		protected void generateCode(Alternative _alt, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_alt, _indent, "// ");
	        // END KGU 2014-11-16
	        
	        String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
	        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
	        
	        code.add(_indent+"if "+condition+" {");
			generateCode(_alt.qTrue,_indent+_indent.substring(0,1));
			if(_alt.qFalse.getSize()!=0)
			{
				code.add(_indent+"}");
				code.add(_indent+"else {");
				generateCode(_alt.qFalse,_indent+_indent.substring(0,1));
			}
			code.add(_indent+"}");
		}
		
		protected void generateCode(Case _case, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_case, _indent, "// ");
	        // END KGU 2014-11-16

	        String condition = transform(_case.getText().get(0));
	        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";

	        code.add(_indent+"switch "+condition+" {");
			
			for(int i=0;i<_case.qs.size()-1;i++)
			{
				code.add(_indent+_indent.substring(0,1)+"case "+_case.getText().get(i+1).trim()+":");
				generateCode((Subqueue) _case.qs.get(i),_indent+_indent.substring(0,1)+_indent.substring(0,1)+_indent.substring(0,1));
				code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1)+"break;\n");
			}
			
			if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
			{
				code.add(_indent+_indent.substring(0,1)+"default:");
				generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+_indent.substring(0,1)+_indent.substring(0,1));
				code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1)+"break;");
			}
			code.add(_indent+"}");
		}
		
		protected void generateCode(For _for, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_for, _indent, "// ");
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
			code.add(_indent+"for ("+
					counterStr+" = "+startValueStr+"; "+counterStr+" <= "+endValueStr+"; "+counterStr+" = "+counterStr+" + ("+stepValueStr+") "+
					") {");
			generateCode(_for.q,_indent+_indent.substring(0,1));
			code.add(_indent+"}");
		}
		
		protected void generateCode(While _while, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_while, _indent, "// ");
	        // END KGU 2014-11-16

	        String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
	        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
	        
	        code.add(_indent+"while "+condition+" {");
			generateCode(_while.q,_indent+_indent.substring(0,1));
			code.add(_indent+"}");
		}
		
		/*30/08/2010 by Bob
                 protected void generateCode(Repeat _repeat, String _indent)
		{
			code.add(_indent+"do {");
			generateCode(_repeat.q,_indent+_indent.substring(0,1));
			code.add(_indent+"} while !("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+");");
		}*/

    @Override
    // version of Kay Gürtzig 
    protected void generateCode(Repeat _repeat, String _indent)
    {
        // START KGU 2014-11-16
        insertComment(_repeat, _indent, "// ");
        // END KGU 2014-11-16

        code.add(_indent+"do {");
        generateCode(_repeat.q,_indent+_indent.substring(0,1));
        code.add(_indent+"} while (!("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+"));");
    }
		
		protected void generateCode(Forever _forever, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_forever, _indent, "// ");
	        // END KGU 2014-11-16

	        code.add(_indent+"while (true) {");
			generateCode(_forever.q,_indent+_indent.substring(0,1));
			code.add(_indent+"}");
		}
		
		protected void generateCode(Call _call, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_call, _indent, "// ");
	        // END KGU 2014-11-16

	        for(int i=0;i<_call.getText().count();i++)
			{
				code.add(_indent+transform(_call.getText().get(i))+"();");
			}
		}
		
		protected void generateCode(Jump _jump, String _indent)
		{
	        // START KGU 2014-11-16
	        insertComment(_jump, _indent, "// ");
	        // END KGU 2014-11-16

	        for(int i=0;i<_jump.getText().count();i++)
			{
				code.add(_indent+"// "+transform(_jump.getText().get(i))+" // goto-instruction not allowed in Java");
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
			if(_root.isProgram==true) {
				code.add("import java.util.Scanner;");
				code.add("");
				// START KGU 2014-11-16
				code.add("/**");
				insertComment(_root, "", " * ");
				code.add(" */");
				// END KGU 2014-11-16
				code.add("public class "+_root.getText().get(0)+" {");
				code.add("");
				code.add(_indent+"// Declare and initialise global variables here");
				code.add("");
				code.add(_indent+"/**");
				code.add(_indent+" * @param args");
				code.add(_indent+" */");
				code.add(_indent+"public static void main(String[] args) {");
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
		        // START KGU 2014-11-16
				//code.add(_indent+" * This method ...");
		        insertComment(_root, _indent, " * ");
		        // END KGU 2014-11-16
				code.add(_indent+" */");
				code.add(_indent+"private static void "+_root.getText().get(0)+"() {");
				code.add(_indent+_indent+"// declare local variables here");
				code.add("");
				generateCode(_root.children,_indent+_indent);
				code.add("");
				code.add(_indent+"}");
			}
			
			return code.getText();
		}
		
	}

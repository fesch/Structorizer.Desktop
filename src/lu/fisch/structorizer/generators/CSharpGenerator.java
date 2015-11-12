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
 *
 ******************************************************************************************************
 *
 *      Comment:
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

//        protected String getIndent()
//        {
//                return "\t";
//        }

        protected String[] getFileExtensions()
        {
                String[] exts = {"cs"};
                return exts;
        }

//        // START KGU 2015-10-18: New pseudo field
//        @Override
//        protected String commentSymbolLeft()
//        {
//        	return "//";
//        }
//        // END KGU 2015-10-18

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

//		/**
//		 * Transforms assignments in the given intermediate-language code line.
//		 * Replaces "<-" by "="
//		 * @param _interm - a code line in intermediate syntax
//		 * @return transformed string
//		 */
//		protected String transformAssignment(String _interm)
//		{
//			return _interm.replace(" <- ", " = ");
//		}
		// END KGU#18/KGU#23 2015-11-01

// START KGU#18/KGU#23 2015-11-01: The inherited method does exactly this.        
//        public static String transform(String _input)
//        {
//                // et => and
//                // ou => or
//                // lire => readln()
//                // écrire => Console.WriteLine()
//                // tant que => ""
//                // pour => ""
//                // jusqu'à => ""
//                // à => "to"
//
//        	String s = _input;
//        	// variable assignment
//            // START KGU 2014-12-02: To achieve consistency with operator highlighting
//            s=s.replace("<--", "<-");
//            // END KGU 2014-12-02
//        	s=s.replace(":=", "<-");
//        	// testing
//        	s=s.replace("==", "=");
//        	// START KGU 2014-11-16: Otherwise this would end as "!=="
//        	s=s.replace("!=", "<>");
//        	// END 2014-11-16
//        	s=s.replace("=", "==");
//        	s=s.replace("<==", "<=");
//        	s=s.replace(">==", ">=");
//        	s=s.replace("<>", "!=");
//        	_input=s;
//
//                // variable assignment
//                _input=BString.replace(_input," <- "," = ");
//                _input=BString.replace(_input,"<- "," = ");
//                _input=BString.replace(_input," <-"," = ");
//                _input=BString.replace(_input,"<-"," = ");
//
//            // convert Pascal operators
//            _input=BString.replace(_input," mod "," % ");
//            _input=BString.replace(_input," div "," / ");
//            // START KGU 2014-11-06: Support logical Pascal operators as well
//            _input=BString.replace(_input," and "," && ");
//            _input=BString.replace(_input," or "," || ");
//            _input=BString.replace(_input," not "," !");
//            // START KGU 2014-11-16: Was too simple in the first place, but now it's clumsy...
//            _input=BString.replace(_input,"(not ", "(!");
//            _input=BString.replace(_input," not(", " !(");
//            _input=BString.replace(_input,"(not(", "(!(");
//           	if (_input.startsWith("not ") || _input.startsWith("not(")) {
//           		_input = "!" + _input.substring(3);
//           	}
//            _input=BString.replace(_input," xor "," ^ ");	// Might cause some operator preference trouble
//           	// END KGU 2014-11-16
//            // END KGU 2014-11-06
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
//            if(!D7Parser.preAlt.equals("")){_input=BString.replace(_input,D7Parser.preAlt,"");}
//            if(!D7Parser.postAlt.equals("")){_input=BString.replace(_input,D7Parser.postAlt,"");}
//            if(!D7Parser.preCase.equals("")){_input=BString.replace(_input,D7Parser.preCase,"");}
//            if(!D7Parser.postCase.equals("")){_input=BString.replace(_input,D7Parser.postCase,"");}
//            if(!D7Parser.preFor.equals("")){_input=BString.replace(_input,D7Parser.preFor,"");}
//            if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"to");}
//            if(!D7Parser.preWhile.equals("")){_input=BString.replace(_input,D7Parser.preWhile,"");}
//            if(!D7Parser.postWhile.equals("")){_input=BString.replace(_input,D7Parser.postWhile,"");}
//            if(!D7Parser.preRepeat.equals("")){_input=BString.replace(_input,D7Parser.preRepeat,"");}
//            if(!D7Parser.postRepeat.equals("")){_input=BString.replace(_input,D7Parser.postRepeat,"");}
//*/
//            
//            /*Regex r;
//             r = new Regex(BString.breakup(D7Parser.input)+"[ ](.*?)","readln($1)"); _input=r.replaceAll(_input);
//             r = new Regex(BString.breakup(D7Parser.output)+"[ ](.*?)","writeln($1)"); _input=r.replaceAll(_input);
//             r = new Regex(BString.breakup(D7Parser.input)+"(.*?)","readln($1)"); _input=r.replaceAll(_input);
//             r = new Regex(BString.breakup(D7Parser.output)+"(.*?)","writeln($1)"); _input=r.replaceAll(_input);*/
//
//            // TODO syntax to be verified!!!
//            if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input+" ")>=0){_input=BString.replace(_input,D7Parser.input+" ","Console.ReadLine(")+")";}
//            if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output+" ")>=0){_input=BString.replace(_input,D7Parser.output+" ","Console.WriteLine(")+")";}
//            if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input)>=0){_input=BString.replace(_input,D7Parser.input,"Console.ReadLine(")+")";}
//            if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output)>=0){_input=BString.replace(_input,D7Parser.output,"Console.WriteLine(")+")";}
//
//            return _input.trim();
//        }
// END KGU#18/KGU#23 2015-11-01

//		// Instruction
//        protected void generateCode(Instruction _inst, String _indent)
//        {
//        	// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
//    		//insertComment(_inst, _indent);
//    		//for(int i=0;i<_inst.getText().count();i++)
//    		//{
//    		//	code.add(_indent+transform(_inst.getText().get(i))+";");
//    		//}
//    		if (!insertAsComment(_inst, _indent)) {
//    			
//    			insertComment(_inst, _indent);
//
//    			StringList lines = _inst.getText();
//    			for (int i = 0; i < lines.count(); i++)
//    			{
//    				code.add(_indent + transform(lines.get(i)) + ";");
//    			}
//
//    		}
//    		// END KGU 2015-10-18
//        }

//        // IF statement
//        protected void generateCode(Alternative _alt, String _indent)
//        {
//                // START KGU 2014-11-16:
//                insertComment(_alt, _indent);
//                // END KGU 2014-11-16
//                
//                String condition = transform(_alt.getText().getLongString(), false).trim();
//                if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
//
//                code.add(_indent+"if "+condition+"");
//                code.add(_indent+"{");
//                generateCode(_alt.qTrue,_indent+this.getIndent());
//                if(_alt.qFalse.getSize()!=0)
//                {
//                        code.add(_indent+"}");
//                        code.add(_indent+"else");
//                        code.add(_indent+"{");
//                        generateCode(_alt.qFalse,_indent+this.getIndent());
//                }
//                code.add(_indent+"}");
//        }

//        // CASE statement
//        protected void generateCode(Case _case, String _indent)
//        {
//                // START KGU 2014-11-16:
//                insertComment(_case, _indent);
//                // END KGU 2014-11-16
//                
//                String condition = transform(_case.getText().get(0));
//                if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
//
//                code.add(_indent+"switch "+condition+" ");
//                code.add(_indent+"{");
//
//                for(int i=0;i<_case.qs.size()-1;i++)
//                {
//                        code.add(_indent+this.getIndent()+"case "+_case.getText().get(i+1).trim()+":");
//                        generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent()+this.getIndent());
//                        code.add(_indent+this.getIndent()+this.getIndent()+"break;");
//                }
//
//                if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
//                {
//                        code.add(_indent+this.getIndent()+"default:");
//                        generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
//                        code.add(_indent+this.getIndent()+this.getIndent()+"break;");
//                }
//                code.add(_indent+"}");
//        }

        // TODO FOREACH loop
//        // Same diagram layout as FOR loop, but different control structure
//        // NSD-string: typeStr itemStr IN setStr // Java, C#
//        // NSD-string: itemStr IN setStr // Perl
//        // Java: for (type item: set) {...}
//        // C#: foreach (type item in set) {...}
//        // Perl: foreach $item ($set) {...}
//        // C++11: range-based for: for (auto item : set)
//        // C: doesn't exist
//        // preparation of 2010-08-07 Gunter Schillebeeckx
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
//			generateCode(_foreach.q,_indent+this.getIndent());
//			code.add(_indent+"}");
//		}

        // FOR loop
//        // NSD-string: counterStr <- startValueStr TO endValueStr STEP stepValueStr
//        protected void generateCode(For _for, String _indent)
//        {
//                // START KGU 2014-11-16
//                insertComment(_for, _indent);
//                // END KGU 2014-11-16
//                
//                String startValueStr="";
//                String endValueStr="";
//                String stepValueStr="";
//                String editStr = BString.replace(transform(_for.getText().getText()),"\n","").trim();
//                String[] word = editStr.split(" ");
//                int nbrWords = word.length;
//                String counterStr = word[0];
//                if ((nbrWords-1) >= 2) startValueStr = word[2];
//                if ((nbrWords-1) >= 4) endValueStr = word[4];
//                if ((nbrWords-1) >= 6) {
//                        stepValueStr = word[6];
//                }
//                else {
//                        stepValueStr = "1";
//                }
//                code.add(_indent+"for ("+
//                                counterStr+" = "+startValueStr+"; "+counterStr+" <= "+endValueStr+"; "+counterStr+" = "+counterStr+" + ("+stepValueStr+") "+
//                                ")");
//                code.add(_indent+"{");
//                generateCode(_for.q,_indent+this.getIndent());
//                code.add(_indent+"}");
//        }

//        // WHILE loop
//        protected void generateCode(While _while, String _indent)
//        {
//                // START KGU 2014-11-16
//                insertComment(_while, _indent);
//                // END KGU 2014-11-16
//                
//                String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
//                if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
//
//                code.add(_indent+"while "+condition+" ");
//                code.add(_indent+"{");
//                generateCode(_while.q,_indent+this.getIndent());
//                code.add(_indent+"}");
//        }

//        // REPEAT loop
//        protected void generateCode(Repeat _repeat, String _indent)
//        {
//                // START KGU 2014-11-16
//                insertComment(_repeat, _indent);
//                // END KGU 2014-11-16
//                code.add(_indent+"do");
//                code.add(_indent+"{");
//                generateCode(_repeat.q,_indent+this.getIndent());
//                code.add(_indent+"} while (!("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+"));");
//        }

//        // ENDLESS loop
//        protected void generateCode(Forever _forever, String _indent)
//        {
//                // START KGU 2014-11-16
//                insertComment(_forever, _indent);
//                // END KGU 2014-11-16
//                code.add(_indent+"while (true)");
//                code.add(_indent+"{");
//                generateCode(_forever.q,_indent+this.getIndent());
//                code.add(_indent+"}");
//        }

//        // CALL
//        protected void generateCode(Call _call, String _indent)
//        {
//                // START KGU 2014-11-16
//                insertComment(_call, _indent);
//                // END KGU 2014-11-16
//                for(int i=0;i<_call.getText().count();i++)
//                {
//                        code.add(_indent+transform(_call.getText().get(i))+";");
//                }
//        }

        // JUMP
        protected void generateCode(Jump _jump, String _indent)
        {
                // START KGU 2014-11-16
                insertComment(_jump, _indent);
                // END KGU 2014-11-16
                for(int i=0;i<_jump.getText().count();i++)
                {
                        code.add(_indent+"goto "+transform(_jump.getText().get(i))+"; // jump-instruction not recommended");

                }
        }

//        // nested structure
//        protected void generateCode(Subqueue _subqueue, String _indent)
//        {
//                // START KGU 2014-11-16
//                insertComment(_subqueue, _indent);
//                // END KGU 2014-11-16
//                // code.add(_indent+"");
//                for(int i=0;i<_subqueue.children.size();i++)
//                {
//                        generateCode((Element) _subqueue.children.get(i),_indent);
//                }
//                // code.add(_indent+"");
//        }

        public String generateCode(Root _root, String _indent)
        {
        	StringList paramNames = _root.getParameterNames();
        	if(_root.isProgram==true) {
                        code.add(_indent + "using System;");
                        code.add(_indent + "");
                        // START KGU 2015-10-18
                        insertBlockComment(_root.getComment(), _indent, "/**", " * ", " */");
                        // END KGU 2015-10-18
                        code.add(_indent + "public class "+_root.getText().get(0)+" {");
                        code.add(_indent);
                        insertComment("TODO Declare and initialise global variables here", _indent + this.getIndent());
                        code.add(_indent);
                        code.add(_indent + this.getIndent()+"/**");
                        code.add(_indent + this.getIndent()+" * @param args");
                        code.add(_indent + this.getIndent()+" */");
                        code.add(_indent + this.getIndent()+"public static void Main(string[] args) {");
                        insertComment("TODO Declare local variables here", _indent + this.getIndent()+this.getIndent());
                        code.add(_indent + this.getIndent()+this.getIndent());
                        insertComment("TODO Initialise local variables here", _indent + this.getIndent()+this.getIndent());
                        code.add(_indent + this.getIndent()+this.getIndent());
                        generateCode(_root.children, _indent + this.getIndent()+this.getIndent());
                        code.add(_indent + this.getIndent()+"}");
                        code.add(_indent);
                        code.add(_indent + "}");
                        }
                else {
                        // START KGU 2015-10-18
                        insertBlockComment(_root.getComment(), _indent + this.getIndent(), "/**", " * ", null);
                        insertBlockComment(paramNames, _indent+this.getIndent(), null, " * @param ", " */");
                        // END KGU 2014-10-18
                        code.add(_indent+this.getIndent() + "private static void " + _root.getText().get(0) + " {");
                        insertComment("TODO Declare local variables here", _indent+this.getIndent() + this.getIndent());
                        code.add(_indent);
                        generateCode(_root.children, _indent+this.getIndent() + this.getIndent());
                        code.add(_indent);
                        code.add(_indent+this.getIndent() + "}");
                }

                return code.getText();
        }


}

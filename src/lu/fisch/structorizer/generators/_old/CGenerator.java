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
 *      Description:    This class generates ANSI C code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          		Date			Description
 *      ------				----			-----------
 *      Bob Fisch       		2008.11.17              First Issue
 *      Gunter Schillebeeckx            2009.08.10		Bugfixes (see comment)
 *      Bob Fisch                       2009.08.17              Bugfixes (see comment)
 *      Bob Fisch                       2010.08-30              Differents fixes asked by Kay Gürtzig¨
 *                                                              and Peter Ehrlich
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2010.08.30
 *      - replaced stnard I/O by the correct versions for C (not Pascal ;-P))
 *      - comments are put into code as well
 *      - code transformations (copied from Java)
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator convertion
 *
 *      2009.08.10 - Bugfixes
 *      - Mistyping of the keyword "switch" in CASE statement
 *      - Mistyping of brackets in IF statement
 *      - Implementation of FOR loop
 *      - Indent replaced from 2 spaces to TAB-character (TAB configurable in IDE)
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.generators.Generator;


public class CGenerator extends Generator 
{

    /************ Fields ***********************/
    protected String getDialogTitle()
    {
            return "Export ANSI C ...";
    }

    protected String getFileDescription()
    {
            return "ANSI C Source Code";
    }

    protected String getIndent()
    {
            return "\t";
    }

    protected String[] getFileExtensions()
    {
            String[] exts = {"c"};
            return exts;
    }

    // START KGU 2015-10-18: New pseudo field
    protected String commentSymbolLeft()
    {
    	return "//";
    }
    // END KGU 2015-10-18

    /************ Code Generation **************/
    public static String transform(String _input)
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
            s=s.replace(":=", "<-");
            s=s.replace("==", "=");
            // testing
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


            if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input+" ")>=0){_input=BString.replace(_input,D7Parser.input+" ","scanf(\"\",&")+")";}
            if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input)>=0){_input=BString.replace(_input,D7Parser.input,"scanf(\"\",&")+")";}

            if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output+" ")>=0){_input=BString.replace(_input,D7Parser.output+" ","printf(\"\",")+"); printf(\"\\n\")";}
            if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output)>=0){_input=BString.replace(_input,D7Parser.output,"printf(\"\",")+"); printf(\"\\n\")";}

            return _input.trim();
    }

    @Override
    protected void generateCode(Instruction _inst, String _indent)
    {
        // add comment
        for(int i=0;i<_inst.getComment().count();i++)
        {
            code.add(_indent+"// "+_inst.getComment().get(i));
        }

        for(int i=0;i<_inst.getText().count();i++)
        {
                code.add(_indent+transform(_inst.getText().get(i))+";");
        }
    }

    @Override
    protected void generateCode(Alternative _alt, String _indent)
    {
        // add comment
        for(int i=0;i<_alt.getComment().count();i++)
        {
            code.add(_indent+"// "+_alt.getComment().get(i));
        }

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

    @Override
    protected void generateCode(Case _case, String _indent)
    {
        // add comment
        for(int i=0;i<_case.getComment().count();i++)
        {
            code.add(_indent+"// "+_case.getComment().get(i));
        }

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
        }
        code.add(_indent+_indent.substring(0,1)+"}");
    }

    @Override
    protected void generateCode(For _for, String _indent)
    {
        // add comment
        for(int i=0;i<_for.getComment().count();i++)
        {
            code.add(_indent+"// "+_for.getComment().get(i));
        }

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
    /* Version 2009.01.18 by Bob Fisch
    protected void generateCode(For _for, String _indent)
    {
            String str = _for.getText().getText();
            // cut of the start of the expression
            if(!D7Parser.preFor.equals("")){str=BString.replace(str,D7Parser.preFor,"");}
            // trim blanks
            str=str.trim();
            // modify the later word
            if(!D7Parser.postFor.equals("")){str=BString.replace(str,D7Parser.postFor,"<=");}
            // do other transformations
            str=transform(str);
            String counter = str.substring(0,str.indexOf("="));
            // insert the middle
            str=BString.replace(str,"<=",";"+counter+"<=");
            // complete
            str="for("+str+";"+counter+"++)";


            code.add(_indent+str);
            // needs some work here!
            code.add(_indent+"{");
            generateCode(_for.q,_indent+_indent.substring(0,1));
            code.add(_indent+"}");
    }/**/

    @Override
    protected void generateCode(While _while, String _indent)
    {
        // add comment
        for(int i=0;i<_while.getComment().count();i++)
        {
            code.add(_indent+"// "+_while.getComment().get(i));
        }

        String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
        if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

        code.add(_indent+"while "+condition+" ");
        code.add(_indent+"{");
        generateCode(_while.q,_indent+_indent.substring(0,1));
        code.add(_indent+"}");
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent)
    {
        // add comment
        for(int i=0;i<_repeat.getComment().count();i++)
        {
            code.add(_indent+"// "+_repeat.getComment().get(i));
        }

        code.add(_indent+"do");
        code.add(_indent+"{");
        generateCode(_repeat.q,_indent+_indent.substring(0,1));
        code.add(_indent+"}");
        code.add(_indent+"while (!("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+"));");
    }

    @Override
    protected void generateCode(Forever _forever, String _indent)
    {
        // add comment
        for(int i=0;i<_forever.getComment().count();i++)
        {
            code.add(_indent+"// "+_forever.getComment().get(i));
        }

        code.add(_indent+"while (true)");
        code.add(_indent+"{");
        generateCode(_forever.q,_indent+_indent.substring(0,1));
        code.add(_indent+"}");
    }

    @Override
    protected void generateCode(Call _call, String _indent)
    {
        // add comment
        for(int i=0;i<_call.getComment().count();i++)
        {
            code.add(_indent+"// "+_call.getComment().get(i));
        }

        for(int i=0;i<_call.getText().count();i++)
        {
                code.add(_indent+transform(_call.getText().get(i))+";");
        }
    }

    @Override
    protected void generateCode(Jump _jump, String _indent)
    {
        // add comment
        for(int i=0;i<_jump.getComment().count();i++)
        {
            code.add(_indent+"// "+_jump.getComment().get(i));
        }

        for(int i=0;i<_jump.getText().count();i++)
        {
                code.add(_indent+transform(_jump.getText().get(i))+";");
        }
    }

    @Override
    protected void generateCode(Subqueue _subqueue, String _indent)
    {
        // add comment
        for(int i=0;i<_subqueue.getComment().count();i++)
        {
            code.add(_indent+"// "+_subqueue.getComment().get(i));
        }

        // code.add(_indent+"");
        for(int i=0;i<_subqueue.children.size();i++)
        {
                generateCode((Element) _subqueue.children.get(i),_indent);
        }
        // code.add(_indent+"");
    }

    @Override
    public String generateCode(Root _root, String _indent)
    {
        // add comment
        for(int i=0;i<_root.getComment().count();i++)
        {
            code.add(_indent+"// "+_root.getComment().get(i));
        }

        String pr = "program";
        if(_root.isProgram==false) {pr="function";}

        //code.add(pr+" "+_root.getText().get(0)+";");
        code.add("// "+pr+" "+_root.getText().get(0)+";");
        code.add("#include <stdio.h>");
        code.add("");
        code.add("int main(void)");
        code.add("{");
        code.add(_indent+"// declare your variables here");
        code.add(_indent);
        code.add(_indent+"// For any input using the 'scanf function you need to fill the first parameter.");
        code.add(_indent+"// http://en.wikipedia.org/wiki/Scanf#Format_string_specifications");
        code.add(_indent);
        code.add(_indent+"// For any output using the 'printf' function you need to fill the first parameter.");
        code.add(_indent+"// http://en.wikipedia.org/wiki/Printf#printf_format_placeholders");
        code.add(_indent);

        code.add(_indent+"");
        generateCode(_root.children,_indent);
        code.add(_indent+"");
        code.add(_indent+"return 0;");
        code.add("}");

        return code.getText();
    }


}

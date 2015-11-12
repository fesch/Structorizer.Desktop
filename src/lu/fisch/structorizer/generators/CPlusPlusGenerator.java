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
*      Author:         Kay G�rtzig
*
*      Description:    This class generates C++ code (mainly based on ANSI C code except for IO).
*
******************************************************************************************************
*
*      Revision List
*
*      Author          		Date			Description
*      ------				----			-----------
*      Kay Gürtzig       	2010.08.31              First Issue
*      Kay Gürtzig          2015.11.01              Adaptations to new decomposed preprocessing
*
******************************************************************************************************
*
*      Comment:
*      2010.08.31 Initial revision
*      - root handling overridden - still too much copied code w.r.t. CGenerator, should be
*        parameterized
*
******************************************************************************************************///

import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;

public class CPlusPlusGenerator extends CGenerator {

    /************ Fields ***********************/
    protected String getDialogTitle()
    {
            return "Export C++ ...";
    }

    protected String getFileDescription()
    {
            return "C++ Source Code";
    }

    protected String[] getFileExtensions()
    {
            String[] exts = {"cpp"};
            return exts;
    }

    /************ Code Generation **************/
	// START KGU#18/KGU#23 2015-11-01 Transformation cecomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine()"
	 */
	protected String getInputReplacer()
	{
		return "std::cin >> $1";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1)"
	 */
	protected String getOutputReplacer()
	{
		return "std::cout << $1 << std::endl";
	}

//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by "="
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	protected String transformAssignment(String _interm)
//	{
//		return _interm.replace(" <- ", " = ");
//	}
	// END KGU#18/KGU#23 2015-11-01
    
//    protected String transform(String _input)
//    {
//            // et => and
//            // ou => or
//            // lire => cin>>
//            // écrire => cout<<
//            // tant que => ""
//            // pour => ""
//            // jusqu'à => ""
//            // à => "to"
//
//            String s = _input;
//            // variable assignment
//            // START KGU 2014-12-02: To achieve consistency with operator highlighting
//            s=s.replace("<--", "<-");
//            // END KGU 2014-12-02
//            s=s.replace(":=", "<-");
//            // testing
//            s=s.replace("==", "=");
//            // START KGU 2014-11-16: Otherwise this would end as "!=="
//    		s=s.replace("!=", "<>");
//    		// END 2014-11-16
//            s=s.replace("=", "==");
//            s=s.replace("<==", "<=");
//            s=s.replace(">==", ">=");
//            s=s.replace("<>", "!=");
//            _input=s;
//
//            // variable assignment
//            _input=BString.replace(_input," <- "," = ");
//            _input=BString.replace(_input,"<- "," = ");
//            _input=BString.replace(_input," <-"," = ");
//            _input=BString.replace(_input,"<-"," = ");
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
//
//            /*Regex r;
//             r = new Regex(BString.breakup(D7Parser.input)+"[ ](.*?)","readln($1)"); _input=r.replaceAll(_input);
//             r = new Regex(BString.breakup(D7Parser.output)+"[ ](.*?)","writeln($1)"); _input=r.replaceAll(_input);
//             r = new Regex(BString.breakup(D7Parser.input)+"(.*?)","readln($1)"); _input=r.replaceAll(_input);
//             r = new Regex(BString.breakup(D7Parser.output)+"(.*?)","writeln($1)"); _input=r.replaceAll(_input);*/
//
//
//            if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input+" ")>=0){_input=BString.replace(_input,D7Parser.input+" ","std::cin >> ");}
//            if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input)>=0){_input=BString.replace(_input,D7Parser.input,"std::cin >> ");}
//
//            if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output+" ")>=0){_input=BString.replace(_input,D7Parser.output+" ","std::cout << (")+") << std::endl";}
//            if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output)>=0){_input=BString.replace(_input,D7Parser.output,"std::cout << (") + ") << std::endl";}
//
//            return _input.trim();
//    }

    @Override
    public String generateCode(Root _root, String _indent)
    {
        // add comment
    	insertComment(_root, "");

        String pr = _root.isProgram ? "program" : "function";

        insertComment(pr+" "+_root.getText().get(0), "");
        code.add("#include <iostream>");
        code.add("");
        
        // START KGU 2010-08-31 This is a somewhat raw first approach to consider possible
        // subroutine arguments (though this requires an explicit extension of the diagram
        // editor to recognize subroutine parameters for the following variable detection
        // properly.
        // Of course, this should be handled the same way in CGenerator.
        //code.add("int main(void)");
        if (_root.isProgram)
        	code.add("int main(void)");
        else {
            insertComment("TODO Revise the return type and declare the parameters!", "");
            String fnHeader = _root.getText().get(0).trim();
            if(fnHeader.indexOf('(')==-1 && !fnHeader.endsWith(")")) fnHeader=fnHeader+"(void)";
        	code.add("int " + fnHeader);
        }
        // END KGU 2010-08-31
        code.add("{");
        insertComment("TODO Don't forget to declare your variables!", _indent);
        code.add(this.getIndent());

        code.add(this.getIndent());
        generateCode(_root.children, this.getIndent());
        code.add(this.getIndent());
        code.add(this.getIndent() + "return 0;");
        code.add("}");

        return code.getText();
    }


}

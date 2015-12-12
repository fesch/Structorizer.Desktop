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
*      Author:         Kay Gürtzig
*
*      Description:    This class generates C++ code (mainly based on ANSI C code except for IO).
*
******************************************************************************************************
*
*      Revision List
*
*      Author          	Date			Description
*      ------			----			-----------
*      Kay Gürtzig      2010.08.31      First Issue
*      Kay Gürtzig      2015.11.01      Adaptations to new decomposed preprocessing
*      Kay Gürtzig      2015.12.11      Enh. #54 (KGU#101): Support for output expression lists
*
******************************************************************************************************
*
*      Comment:
*      2010.08.31 Initial revision
*      - root handling overridden - still too much copied code w.r.t. CGenerator, should be
*        parameterized
*
******************************************************************************************************///

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

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
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
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


	// START KGU#101 2015-12-11: Enhancement #54: Cope with output expression lists
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		// START KGU#101 2015-12-11: Enh. #54 - support lists of expressions
		if (_input.matches("^" + D7Parser.output.trim() + "[ ](.*?)"))
		{
			StringList expressions = 
					Element.splitExpressionList(_input.substring(D7Parser.output.trim().length()), ",");
			_input = D7Parser.output.trim() + " " + expressions.getText().replace("\n", " << ");
		}
		// END KGU#101 2015-12-11
		
		_input = super.transform(_input);
		
		return _input.trim();
	}
	// END KGU#101 2015-12-11
	
    @Override
    public String generateCode(Root _root, String _indent)
    {
        // add comment
    	insertComment(_root, "");

        String pr = _root.isProgram ? "program" : "function";
        insertComment(pr + " " + _root.getMethodName(), "");
        code.add("#include <iostream>");
        code.add("");
        
        if (_root.isProgram)
        	code.add("int main(void)");
        else {
            insertComment("TODO Revise the return type and declare the parameters!", _indent);
            String fnHeader = _root.getMethodName();
            
            //if(fnHeader.indexOf('(')==-1 && !fnHeader.endsWith(")")) fnHeader=fnHeader+"(void)";
            StringList paramNames = _root.getParameterNames();
            if (paramNames.count() > 0) {
            	fnHeader += "(" + paramNames.getText().replace("\n", ", ") + ")";
            } else {
            	fnHeader += "(void)";             	
            }
        	code.add("int " + fnHeader);
        }
        // END KGU 2010-08-31
        code.add("{");
        insertComment("TODO Don't forget to declare your variables!", this.getIndent());
        code.add(this.getIndent());

        code.add(this.getIndent());
        generateCode(_root.children, this.getIndent());
        code.add(this.getIndent());
        code.add(this.getIndent() + "return 0;");
        code.add("}");

        return code.getText();
    }


}

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

        protected String[] getFileExtensions()
        {
                String[] exts = {"cs"};
                return exts;
        }

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

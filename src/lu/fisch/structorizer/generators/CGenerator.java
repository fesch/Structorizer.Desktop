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
 *      Description:    This class generates ANSI C code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          		Date			Description
 *      ------					----			-----------
 *      Bob Fisch       	    2008.11.17		First Issue
 *      Gunter Schillebeeckx    2009.08.10		Bugfixes (see comment)
 *      Bob Fisch               2009.08.17		Bugfixes (see comment)
 *      Bob Fisch               2010.08-30		Different fixes asked by Kay Gürtzig
 *                                        		and Peter Ehrlich
 *      Kay Gürtzig             2010.09.10		Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011.11.07		Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.11.06		Support for logical Pascal operators added
 *      Kay Gürtzig             2014.11.16		Bugfixes in operator conversion
 *      Kay Gürtzig             2015.10.18		Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig             2015.10.21		New generator now supports multiple-case branches
 *      Kay Gürtzig             2015.11.01		Language transforming reorganised, FOR loop revision
 *      Kay Gürtzig             2015.11.10		Bugfixes KGU#71, KGU#72
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops now provide themselves more reliable loop parameters  
 *      
 *      2015.10.21 - Enhancement KGU#15: Case element with comma-separated constant list per branch
 *      
 *      2015.10.18 - Bugfixes and modificatons (Kay Gürtzig)
 *      - Bugfix: The export option "export instructions as comments" had been ignored before
 *      - An empty Jump element will now be translated into a break; instruction by default.
 *      - Comment method signature simplified
 *      - Indentation mechanism revised
 *      
 *      2014.11.16 - Bugfixes (Kay Gürtzig)
 *      - conversion of comparison and logical operators had still been flawed
 *      - comment generation unified by new inherited generic method insertComment 
 *      
 *      2014.11.06 - Enhancement (Kay Gürtzig)
 *      - logical operators "and", "or", and "not" supported 
 *      
 *      2010.09.10 - Bugfixes (Kay Gürtzig)
 *      - conditions for automatic bracket insertion for "while", "switch", "if" corrected
 *      - case keyword inserted for the branches of "switch"
 *      - function header and return statement for non-program diagram export adjusted
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *      
 *      2010.08.30
 *      - replaced standard I/O by the correct versions for C (not Pascal ;-P))
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


public class CGenerator extends Generator 
{

    /************ Fields ***********************/
    @Override
    protected String getDialogTitle()
    {
            return "Export ANSI C ...";
    }

    @Override
    protected String getFileDescription()
    {
            return "ANSI C Source Code";
    }

    @Override
    protected String getIndent()
    {
            return "\t";
    }

    @Override
    protected String[] getFileExtensions()
    {
            String[] exts = {"c"};
            return exts;
    }

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	// In ANSI C99, line comments are already allowed
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
		return "scanf(\"\", &$1)";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "printf(\"\", $1); printf(\"\\\\n\")";
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
//    public static String transform(String _input)
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		_input = super.transform(_input);

		// START KGU#72 2015-11-10: Replacement was done but ineffective
		//_input.replace(" div ", " / ");
		_input = _input.replace(" div ", " / ");
		// END KGU#72 2015-11-10
		
		return _input.trim();
	}
    
    
    @Override
    protected void generateCode(Instruction _inst, String _indent)
    {
    	// START KGU#18/KGU#23 2015-10-18: The "export instructions as comments" configuration had been ignored here
//		insertComment(_inst, _indent);
//		for(int i=0;i<_inst.getText().count();i++)
//		{
//			code.add(_indent+transform(_inst.getText().get(i))+";");
//		}
		if (!insertAsComment(_inst, _indent)) {
			
			insertComment(_inst, _indent);

			StringList lines = _inst.getText();
			for (int i = 0; i < lines.count(); i++)
			{
				code.add(_indent + transform(lines.get(i)) + ";");
			}

		}
		// END KGU 2015-10-18
    }

    @Override
    protected void generateCode(Alternative _alt, String _indent)
    {
    	// START KGU 2014-11-16
    	insertComment(_alt, _indent);
    	// END KGU 2014-11-16

    	String condition = transform(_alt.getText().getLongString(), false).trim();
    	if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";

    	code.add(_indent + "if " + condition);
    	code.add(_indent + "{");
    	generateCode(_alt.qTrue, _indent + this.getIndent());
    	if(_alt.qFalse.getSize() != 0)
    	{
    		code.add(_indent+"}");
    		code.add(_indent+"else");
    		code.add(_indent+"{");
    		generateCode(_alt.qFalse, _indent + this.getIndent());
    	}
    	code.add(_indent+"}");
    }

    @Override
    protected void generateCode(Case _case, String _indent)
    {
        // START KGU 2014-11-16
        insertComment(_case, _indent);
        // END KGU 2014-11-16

        StringList lines = _case.getText();
        String condition = transform(lines.get(0), false);
        if(!condition.startsWith("(") || !condition.endsWith(")"))
        {
        	condition= "(" + condition + ")";
        }

        code.add(_indent + "switch " + condition + " ");
    	code.add(_indent + "{");

    	for(int i = 0; i < _case.qs.size()-1; i++)
    	{
    		// START KGU#15 2015-10-21: Support for multiple constants per branch
    		StringList constants = StringList.explode(lines.get(i+1), ",");
    		for (int j = 0; j < constants.count(); j++)
    		{
    			code.add(_indent + "case " + constants.get(j).trim() + ":");
    		}
    		// END KGU#15 2015-10-21
    		generateCode((Subqueue)_case.qs.get(i), _indent + this.getIndent());
    		code.add(_indent + this.getIndent() + "break;");
    	}

    	if (!lines.get(_case.qs.size()).trim().equals("%"))
    	{
    		code.add(_indent + "default:");
    		Subqueue squeue = (Subqueue)_case.qs.get(_case.qs.size()-1);
    		generateCode(squeue, _indent+this.getIndent());
    		// START KGU#71 2015-11-10: For an empty default branch, at least a semicolon is required
    		if (squeue.children.size() == 0) {
    			code.add(_indent + this.getIndent() + ";");
    		}
    		// END KGU#71 2015-11-10
    	}
    	code.add(_indent+"}");
    }
    // END KGU#18/#23 2015-10-20
    

    @Override
    protected void generateCode(For _for, String _indent)
    {
    	insertComment(_for, _indent);

    	String var = _for.getCounterVar();
    	int step = _for.getStepConst();
    	String compOp = (step > 0) ? " <= " : " >= ";
    	String increment = var + " += (" + step + ")";
    	code.add(_indent + "for (" +
    			var + " = " + transform(_for.getStartValue(), false) + "; " +
    			var + compOp + transform(_for.getEndValue(), false) + "; " +
    			increment +
    			")");
    	code.add(_indent + "{");
    	generateCode(_for.q, _indent + this.getIndent());
    	code.add(_indent + "}");
    }


    @Override
    protected void generateCode(While _while, String _indent)
    {
        // START KGU 2014-11-16
        insertComment(_while, _indent);
        // END KGU 2014-11-16

        String condition = transform(_while.getText().getLongString(), false).trim();
        if(!condition.startsWith("(") || !condition.endsWith(")"))
        {
        	condition = "(" + condition + ")";
        }

        code.add(_indent + "while " + condition + " ");
        code.add(_indent + "{");
        generateCode(_while.q, _indent+this.getIndent());
        code.add(_indent + "}");
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent)
    {
        // START KGU 2014-11-16
        insertComment(_repeat, _indent);
        // END KGU 2014-11-16

        code.add(_indent + "do");
        code.add(_indent + "{");
        generateCode(_repeat.q, _indent + this.getIndent());
        code.add(_indent + "} while (!(" + transform(_repeat.getText().getLongString()).trim() + "));");
    }

    @Override
    protected void generateCode(Forever _forever, String _indent)
    {
        // START KGU 2014-11-16
        insertComment(_forever, _indent);
        // END KGU 2014-11-16

        code.add(_indent + "while (true)");
        code.add(_indent + "{");
        generateCode(_forever.q, _indent + this.getIndent());
        code.add(_indent + "}");
    }

    @Override
    protected void generateCode(Call _call, String _indent)
    {
    	// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
//		insertComment(_call, _indent);
//		for(int i=0;i<_call.getText().count();i++)
//		{
//			code.add(_indent+transform(_call.getText().get(i))+";");
//		}
		if (!insertAsComment(_call, _indent)) {
			
			insertComment(_call, _indent);

			StringList lines = _call.getText();
			for (int i = 0; i < lines.count(); i++)
			{
				// Input or Output should not occur here
				code.add(_indent + transform(lines.get(i), false) + ";");
			}
		}
		// END KGU 2015-10-18
    }

    @Override
    protected void generateCode(Jump _jump, String _indent)
    {
    	// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
//		insertComment(_jump, _indent);
//		for(int i=0;i<_jump.getText().count();i++)
//		{
//			code.add(_indent+transform(_jump.getText().get(i))+";");
//		}
		if (!insertAsComment(_jump, _indent)) {
			
			insertComment(_jump, _indent);

			// KGU 2015-10-18: In case of an empty text generate a break instruction by default.
			boolean isEmpty = true;
			StringList lines = _jump.getText();
			for (int i = 0; i < lines.count(); i++)
			{
				String line = transform(lines.get(i));
				if (!line.trim().isEmpty()) isEmpty = false;
				code.add(_indent + line + ";");
			}
			if (isEmpty)
			{
				code.add(_indent + "break;");
			}

		}
		// END KGU 2015-10-18
    }

    // START KGU 2015-11-01: This is exactly was the inherited method does...
//    @Override
//    protected void generateCode(Subqueue _subqueue, String _indent)
//    {
//        // code.add(_indent+"");
//        for(int i=0; i<_subqueue.children.size(); i++)
//        {
//                generateCode((Element) _subqueue.children.get(i), _indent);
//        }
//        // code.add(_indent+"");
//    }
    // END KGU 2015-11-01

    @Override
    public String generateCode(Root _root, String _indent)
    {
        String pr = (_root.isProgram) ? "program" : "function";

        //code.add(pr+" "+_root.getText().get(0)+";");
        insertComment(pr + " " + _root.getText().get(0), "");
        code.add("#include <stdio.h>");
        code.add("");
        // START KGU 2014-11-16
        insertComment(_root, "");
        // END KGU 2014-11-16

        // START Kay Gürtzig 2010-09-10
        //code.add("int main(void)");
        if (_root.isProgram)
        	code.add("int main(void)");
        else {
            String fnHeader = _root.getText().get(0).trim();
            if(fnHeader.indexOf('(')==-1 || !fnHeader.endsWith(")")) fnHeader=fnHeader+"(void)";
            // START KGU 2015-10-18: Hint to accomplish the function signature
            insertComment("TODO Revise the return type and declare the parameters.", "");
            // END KGU 2015-10-18
        	code.add("int " + fnHeader);
        }
        // END Kay Gürtzig 2010-09-10
        code.add("{");
        insertComment("TODO declare your variables here", this.getIndent());
        code.add(this.getIndent());
        insertComment("TODO", this.getIndent());
        insertComment("For any input using the 'scanf' function you need to fill the first argument.", this.getIndent());
        insertComment("http://en.wikipedia.org/wiki/Scanf#Format_string_specifications", this.getIndent());
        code.add(this.getIndent());
        insertComment("TODO", this.getIndent());
        insertComment("For any output using the 'printf' function you need to fill the first argument:", this.getIndent());
        insertComment("http://en.wikipedia.org/wiki/Printf#printf_format_placeholders", this.getIndent());
        code.add(this.getIndent());

        code.add(this.getIndent());
        generateCode(_root.children, this.getIndent());
        // Kay Gürtzig 2010.09.10: A function will already have got a return statement (if it needs one)
        if (_root.isProgram)
        {
        	code.add(this.getIndent());
        	code.add(this.getIndent()+"return 0;");
        }
        code.add("}");

        return code.getText();
    }


}

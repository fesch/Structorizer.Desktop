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
 *      Description:    This class generates Basic code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author              Date            Description
 *      ------              ----            -----------
 *      Jacek Dzieniewicz   2013.03.02      First Issue
 *      Kay Gürtzig         2015.10.18      Comment generation revised
 *      Kay Gürtzig         2015.11.02      Case generation was defective (KGU#58), comments exported,
 *                                          transformation reorganised, FOR loop mended (KGU#3)
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
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


public class BasGenerator extends Generator 
{
	
	
    /************ Fields ***********************/
    @Override
    protected String getDialogTitle()
    {
            return "Export Basic Code ...";
    }

    @Override
    protected String getFileDescription()
    {
            return "Basic Code";
    }

    @Override
    protected String getIndent()
    {
            return "  ";
    }

    @Override
    protected String[] getFileExtensions()
    {
            String[] exts = {"bas"};
            return exts;
    }

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "REM";
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
		return "INPUT $1";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "PRINT $1";
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

	// START KGU#18/KGU#23 2015-11-02: Method properly sub-classed
	//    private String transform(String _input)
	@Override
	protected String transform(String _input)
	{
		String interm = super.transform(_input);
		// Operator translations
		interm.replace(" == ", " = ");
		interm.replace(" != ", " <> ");
		interm.replace(" && ", " AND ");
		interm.replace(" || ", " OR ");
		interm.replace(" ! ", " NOT ");

//    {
//            // et => and
//            // ou => or
//            // lire => readln()
//            // écrire => writeln()
//            // tant que => ""
//            // pour => ""
//            // jusqu'à => ""
//            // à => "to"
//
//            _input=BString.replace(_input," <- ","=");
//            _input=BString.replace(_input,"<- ","=");
//            _input=BString.replace(_input," <-","=");
//            _input=BString.replace(_input,"<-","=");
//            
//            StringList empty = new StringList();
//            empty.addByLength(D7Parser.preAlt);
//            empty.addByLength(D7Parser.postAlt);
//            empty.addByLength(D7Parser.preCase);
//            empty.addByLength(D7Parser.postCase);
//            empty.addByLength(D7Parser.preFor);
//            empty.addByLength(D7Parser.preWhile);
//            empty.addByLength(D7Parser.postWhile);
//            empty.addByLength(D7Parser.postRepeat);
//            empty.addByLength(D7Parser.preRepeat);
//            //System.out.println(empty);
//            for(int i=0;i<empty.count();i++)
//            {
//                _input=BString.replace(_input,empty.get(i),"");
//                //System.out.println(_input);
//                //System.out.println(i);
//            }
//            if(!D7Parser.postFor.equals("")){_input=BString.replace(_input,D7Parser.postFor,"TO");}
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
//            r = new Regex(BString.breakup(D7Parser.input)+"[ ](.*?)","readln($1)"); _input=r.replaceAll(_input);
//            r = new Regex(BString.breakup(D7Parser.output)+"[ ](.*?)","writeln($1)"); _input=r.replaceAll(_input);
//            r = new Regex(BString.breakup(D7Parser.input)+"(.*?)","readln($1)"); _input=r.replaceAll(_input);
//            r = new Regex(BString.breakup(D7Parser.output)+"(.*?)","writeln($1)"); _input=r.replaceAll(_input);*/
//
//
//            if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input+" ")>=0){_input=BString.replace(_input,D7Parser.input+" ","INPUT ");}
//            if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output+" ")>=0){_input=BString.replace(_input,D7Parser.output+" ","PRINT ");}
//            if(!D7Parser.input.equals("")&&_input.indexOf(D7Parser.input)>=0){_input=BString.replace(_input,D7Parser.input,"INPUT");}
//            if(!D7Parser.output.equals("")&&_input.indexOf(D7Parser.output)>=0){_input=BString.replace(_input,D7Parser.output,"PRINT");}
//
//            return _input.trim();
		return interm.trim();
    }
	// END KGU#18/KGU#23 2015-11-02
	

    @Override
    protected void generateCode(Instruction _inst, String _indent)
    {

		if(!insertAsComment(_inst, _indent)) {
			// START KGU 2014-11-16
			insertComment(_inst, _indent);
			// END KGU 2014-11-16
			for(int i=0;i<_inst.getText().count();i++)
			{
				code.add(_indent + transform(_inst.getText().get(i)));
			}
		}
    }

    @Override
    protected void generateCode(Alternative _alt, String _indent)
    {

            String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();

        	// START KGU 2015-11-02
        	insertComment(_alt, _indent);
        	// END KGU 2015-11-02

            code.add(_indent+"IF "+condition+" THEN");
            generateCode(_alt.qTrue,_indent+this.getIndent());
            if(_alt.qFalse.getSize()!=0)
            {
                    code.add(_indent+"ELSE");
                    generateCode(_alt.qFalse,_indent+this.getIndent());
            }
            code.add(_indent+"END IF");
    }

    @Override
    protected void generateCode(Case _case, String _indent)
    {

    	String condition = transform(_case.getText().get(0));

    	// START KGU 2015-11-02
    	insertComment(_case, _indent);
    	// END KGU 2015-11-02

    	code.add(_indent+"SELECT CASE "+condition);

    	for(int i=0; i<_case.qs.size()-1; i++)
    	{
    		// START KGU#58 2015-11-02: CASE key words have been missing. KGU#15 already works  
    		//code.add(_indent+this.getIndent()+_case.getText().get(i+1).trim());
    		code.add(_indent + this.getIndent() + "CASE " + _case.getText().get(i+1).trim());
    		// END KGU#58 2015-11-02
    		//    code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1));
    		generateCode((Subqueue) _case.qs.get(i), _indent+this.getIndent()+this.getIndent());
    		//    code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1));
    	}

    	if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
    	{
    		code.add(_indent+this.getIndent()+"CASE ELSE");
    		generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
    	}
    	code.add(_indent+"END SELECT");
    }

    @Override
    protected void generateCode(For _for, String _indent)
    {
    	// START KGU#3 2015-11-02: Sensible handling of FOR loops
        //code.add(_indent+"FOR "+BString.replace(transform(_for.getText().getText()),"\n","").trim()+"");
    	insertComment(_for, _indent);

    	String[] parts = _for.splitForClause();
    	String increment = "";
    	if (parts[3].trim().equals("1")) increment = " STEP " + parts[3];
    	code.add(_indent + "FOR " +
    			parts[0] + " = " + transform(parts[1], false) +
    			" TO " + transform(parts[2], false) + increment);
    	// END KGU 2015-11-02
    	generateCode(_for.q, _indent + this.getIndent());
    	code.add(_indent+"NEXT");
    }

    @Override
    protected void generateCode(While _while, String _indent)
    {

            String condition = transform(_while.getText().getLongString(), false).trim();

        	// START KGU 2015-11-02
        	insertComment(_while, _indent);
        	// END KGU 2015-11-02

            code.add(_indent+"DO WHILE "+condition+"");
            generateCode(_while.q,_indent+this.getIndent());
            code.add(_indent+"LOOP");
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent)
    {

            String condition = transform(_repeat.getText().getLongString()).trim();

        	// START KGU 2015-11-02
        	insertComment(_repeat, _indent);
        	// END KGU 2015-11-02

        	code.add(_indent+"DO");
            generateCode(_repeat.q,_indent+this.getIndent());
            code.add(_indent+"LOOP UNTIL " + condition);
    }

    @Override
    protected void generateCode(Forever _forever, String _indent)
    {
    	// START KGU 2015-11-02
    	insertComment(_forever, _indent);
    	// END KGU 2015-11-02

    	code.add(_indent+"DO");
    	generateCode(_forever.q,_indent+this.getIndent());
    	code.add(_indent+"LOOP");
    }
	
    @Override
    protected void generateCode(Call _call, String _indent)
    {
		if(!insertAsComment(_call, _indent)) {
			// START KGU 2014-11-16
			insertComment(_call, _indent);
			// END KGU 2014-11-16
			for(int i=0;i<_call.getText().count();i++)
			{
				// FIXME (KGU 2015-11-02): This might require a CALL command prefix
				code.add(_indent+transform(_call.getText().get(i)));
			}
		}
    }

    @Override
    protected void generateCode(Jump _jump, String _indent)
    {
    	if(!insertAsComment(_jump, _indent)) {
    		// START KGU 2014-11-16
    		insertComment(_jump, _indent);
    		// END KGU 2014-11-16
    		for(int i=0;i<_jump.getText().count();i++)
    		{
    			code.add(_indent+transform(_jump.getText().get(i)));
    		}
    	}
    }

    @Override
    protected void generateCode(Subqueue _subqueue, String _indent)
    {

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

            String pr = "REM program";
            String indent = _indent;
            if(_root.isProgram==false)
            {
            	pr="FUNCTION";
            	indent = indent + this.getIndent();	// Within function declarations do indent (not within programs)
            }
            code.add(pr+" "+_root.getText().get(0));
    		// START KGU 2015-11-02
    		insertComment(_root, indent);
    		// END KGU 2015-11-02
            insertComment("TODO declare variables here: DIM AS type variable1, variable2, ...", indent);
            code.add("");
            generateCode(_root.children, indent);
            code.add("");
            if(_root.isProgram==false) {code.add(indent + "END FUNCTION");} else {code.add(indent + "REM END");}

            return code.getText();
    }
	
}

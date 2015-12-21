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
 *      Kay Gürtzig         2015.12.18      Enh. #9 (KGU#2) Call mechanisms had to be refined,
 *                                          Enh. #23 (KGU#78) Jump mechanism implemented
 *                                          Root generation decomposed and fundamentally revised
 *                                          Enh. #67 (KGU#113) Line number generation considered
 *      Kay Gürtzig         2015.12.19      Bugfix #51 (KGU#108) empty input instruction
 *                                          Enh. #54 (KGU#101) multiple expressions on output
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

import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


public class BasGenerator extends Generator 
{

	// START KGU#74 2015-12-18: Bugfix #22: Needed for one of the return mechanisms
	// The method name of root
	protected String procName = "";
	
	protected int lineNumber = 10;
	protected int lineIncrement = 10;
	protected int[] labelMap;
	// END KGU#74 2015-12-18
	
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

	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean supportsSimpleBreak()
	{
		return false;
	}
	// END KGU#78 2015-12-18

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
		String prefix = "";
		if (_interm.indexOf(" <- ") >= 0 && this.optionBasicLineNumbering())	// Old-style Basic? Then better insert "LET "
		{
			prefix = "LET ";
		}
		return prefix + _interm.replace(" <- ", " = ");
	}
	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#113 2015-12-18: Enh. #67: Provide a current line number if required
	protected String getLineNumber()
	{
		String prefix = "";
		if (this.optionBasicLineNumbering())
		{
			prefix += this.lineNumber + " ";
			this.lineNumber += this.lineIncrement;
		}
		return prefix;
	}

	protected void placeJumpTarget(ILoop _loop, String _indent)
	{
        if (this.jumpTable.containsKey(_loop))
        {
        	if (this.optionBasicLineNumbering())
        	{
        		// Associate label number with line number of the following dummy comment 
        		this.labelMap[this.jumpTable.get(_loop).intValue()] = this.lineNumber;
        		insertComment("Exit point from above loop.", _indent);
        	}
        	else
        	{
        		code.add(_indent + this.labelBaseName + this.jumpTable.get(_loop).toString() + ": " +
        				this.commentSymbolLeft() + " Exit point from above loop.");
        	}
        }
		
	}
	
	// We need an overridden fundamental comment method here to be able to insert line numbers.
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#insertComment(java.lang.String, java.lang.String)
	 */
	@Override
	protected void insertComment(String _text, String _indent)
	{
		String[] lines = _text.split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			code.add(this.getLineNumber() + _indent + commentSymbolLeft() + " " + lines[i] + " " + commentSymbolRight());
		}
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#insertBlockComment(lu.fisch.utils.StringList, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected void insertBlockComment(StringList _sl, String _indent, String _start, String _cont, String _end)
	{
		int oldSize = code.count();
		super.insertBlockComment(_sl, _indent, _start, _cont, _end);
		// Set the line numbers afterwards, the super method wouldn't have done it
		if (this.optionBasicLineNumbering())
		{
			for (int i = oldSize; i < code.count(); i++)
			{
				code.set(i, this.getLineNumber() + " " + code.get(i));
			}
		}
	}
	// END KGU#113 2015-12-18

	// START KGU#18/KGU#23 2015-11-02: Method properly sub-classed
	//    private String transform(String _input)
	@Override
	protected String transform(String _input)
	{
		// START KGU#101 2015-12-19: Enh. #54 - support lists of output expressions
		if (_input.matches("^" + D7Parser.output.trim() + "[ ](.*?)"))
		{
			// Replace commas by semicolons to avoid tabulation
			StringList expressions = 
					Element.splitExpressionList(_input.substring(D7Parser.output.trim().length()), ",");
			_input = D7Parser.output.trim() + " " + expressions.getText().replace("\n", "; ");
		}
		// END KGU#101 2015-12-19

		String interm = super.transform(_input);
		
		// Operator translations
		interm = interm.replace(" == ", " = ");
		interm = interm.replace(" != ", " <> ");
		interm = interm.replace(" && ", " AND ");
		interm = interm.replace(" || ", " OR ");
		interm = interm.replace(" ! ", " NOT ");
		// START KGU 2015-12-19: In BASIC, array indices are usually encöosed by parentheses rather than brackets
		interm = interm.replace("[", "(");
		interm = interm.replace("]", ")");
		// END KGU 2015-12-19

		// START KGU#108 2015-12-19: Bugfix #51: Cope with empty input
		if (interm.trim().equals("INPUT"))
		{
			interm = "SLEEP";	// waits for key hit (according to https://en.wikibooks.org/wiki/BASIC_Programming/Beginning_BASIC/User_Input)
		}
		// END KGU#108 2015-12-19

		return interm.trim();
    }
	// END KGU#18/KGU#23 2015-11-02
	
	// START KGU#16 2015-12-19
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformType(java.lang.String, java.lang.String)
	 */
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		else {
			_type = _type.trim();
			if (_type.equals("int")) _type = "Integer";
			else if (_type.equals("string") || _type.equals("char[]")) _type = "String";
			// To be continued if required...
		}
		return _type;
	}
	// END KGU#1 2015-12-19	

    @Override
    protected void generateCode(Instruction _inst, String _indent)
    {

		if(!insertAsComment(_inst, _indent)) {
			// START KGU 2014-11-16
			insertComment(_inst, _indent);
			// END KGU 2014-11-16
			for(int i=0; i<_inst.getText().count(); i++)
			{
				code.add(this.getLineNumber() + _indent + transform(_inst.getText().get(i)));
			}
		}
    }

    @Override
    protected void generateCode(Alternative _alt, String _indent)
    {

            String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
            String indentPlusOne = _indent + this.getIndent();

        	// START KGU 2015-11-02
        	insertComment(_alt, _indent);
        	// END KGU 2015-11-02

            code.add(this.getLineNumber() + _indent + "IF " + condition + " THEN");
            generateCode(_alt.qTrue, indentPlusOne);
            if(_alt.qFalse.getSize() > 0)
            {
                    code.add(this.getLineNumber() + _indent + "ELSE");
                    generateCode(_alt.qFalse, indentPlusOne);
            }
            code.add(this.getLineNumber() + _indent + "END IF");
    }

    @Override
    protected void generateCode(Case _case, String _indent)
    {

    	String selection = transform(_case.getText().get(0));
        String indentPlusOne = _indent + this.getIndent();
        String indentPlusTwo = indentPlusOne + this.getIndent();

    	// START KGU 2015-11-02
    	insertComment(_case, _indent);
    	// END KGU 2015-11-02

    	code.add(this.getLineNumber() + _indent + "SELECT CASE " + selection);

    	for(int i=0; i<_case.qs.size()-1; i++)
    	{
    		// START KGU#58 2015-11-02: CASE key words have been missing. KGU#15 already works  
    		//code.add(_indent+this.getIndent()+_case.getText().get(i+1).trim());
    		code.add(this.getLineNumber() + indentPlusOne + "CASE " + _case.getText().get(i+1).trim());
    		// END KGU#58 2015-11-02
    		//    code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1));
    		generateCode((Subqueue) _case.qs.get(i), indentPlusTwo);
    		//    code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1));
    	}

    	if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
    	{
    		code.add(this.getLineNumber() + indentPlusOne + "CASE ELSE");
    		generateCode((Subqueue)_case.qs.get(_case.qs.size()-1), indentPlusTwo);
    	}
    	code.add(this.getLineNumber() + _indent + "END SELECT");
    }

    @Override
    protected void generateCode(For _for, String _indent)
    {
    	// START KGU#3 2015-11-02: Sensible handling of FOR loops
        //code.add(_indent+"FOR "+BString.replace(transform(_for.getText().getText()),"\n","").trim()+"");
    	insertComment(_for, _indent);

    	String[] parts = _for.splitForClause();
    	String increment = "";
    	if (!parts[3].trim().equals("1")) increment = " STEP " + parts[3];
    	code.add(this.getLineNumber() + _indent + "FOR " +
    			parts[0] + " = " + transform(parts[1], false) +
    			" TO " + transform(parts[2], false) + increment);
    	// END KGU 2015-11-02
    	generateCode(_for.q, _indent + this.getIndent());
    	code.add(this.getLineNumber() + _indent + "NEXT " + parts[0]);
    	
    	// START KGU#78 2015-12-18: Enh. #23
    	this.placeJumpTarget(_for, _indent);
    	// END KGU#78 2915-12-18
    }

    @Override
    protected void generateCode(While _while, String _indent)
    {

            String condition = transform(_while.getText().getLongString(), false).trim();

        	// START KGU 2015-11-02
        	insertComment(_while, _indent);
        	// END KGU 2015-11-02

        	// The traditional BASIC while loop looks like WHILE condition ... WEND
            code.add(this.getLineNumber() + _indent + "DO WHILE " + condition);
            generateCode(_while.q, _indent+this.getIndent());
            code.add(this.getLineNumber() + _indent + "LOOP");
            
        	// START KGU#78 2015-12-18: Enh. #23
        	this.placeJumpTarget(_while, _indent);
        	// END KGU#78 2915-12-18
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent)
    {

            String condition = transform(_repeat.getText().getLongString()).trim();

        	// START KGU 2015-11-02
        	insertComment(_repeat, _indent);
        	// END KGU 2015-11-02

        	code.add(this.getLineNumber() + _indent + "DO");
            generateCode(_repeat.q, _indent + this.getIndent());
            code.add(this.getLineNumber() + _indent + "LOOP UNTIL " + condition);

            // START KGU#78 2015-12-18: Enh. #23
        	this.placeJumpTarget(_repeat, _indent);
        	// END KGU#78 2915-12-18
    }

    @Override
    protected void generateCode(Forever _forever, String _indent)
    {
    	// START KGU 2015-11-02
    	insertComment(_forever, _indent);
    	// END KGU 2015-11-02

    	code.add(this.getLineNumber() + _indent + "DO");
    	generateCode(_forever.q, _indent+this.getIndent());
    	code.add(this.getLineNumber() + _indent + "LOOP");

    	// START KGU#78 2015-12-18: Enh. #23
    	this.placeJumpTarget(_forever, _indent);
    	// END KGU#78 2915-12-18
    }
	
    @Override
    protected void generateCode(Call _call, String _indent)
    {
		if(!insertAsComment(_call, _indent)) {
			// START KGU 2014-11-16
			insertComment(_call, _indent);
			// END KGU 2014-11-16
			for(int i=0; i<_call.getText().count(); i++)
			{
				// START KGU#2 2015-12-18: Enh. #9 This may require a CALL command prefix
				//code.add(_indent+transform(_call.getText().get(i)));
				String line = transform(_call.getText().get(i));
				if (!line.startsWith("LET") || line.indexOf(" = ") < 0)
				{
					line = "CALL " + line;
				}
				code.add(this.getLineNumber() + _indent + line);
				// END KGU#2 2015-12-18
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
    		
    		// START KGU#78 2015-12-18: Enh. #23 Generate sensible goto instructions
    		//for(int i=0;i<_jump.getText().count();i++)
    		//{
    		//	code.add(_indent+transform(_jump.getText().get(i)));
    		//}
			boolean isEmpty = true;
			
			StringList lines = _jump.getText();
			for (int i = 0; isEmpty && i < lines.count(); i++) {
				String line = transform(lines.get(i)).trim();
				if (!line.isEmpty())
				{
					isEmpty = false;
				}
				// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
				//code.add(_indent + line + ";");
				if (line.matches(Matcher.quoteReplacement(D7Parser.preReturn)+"([\\W].*|$)"))
				{
					String argument = line.substring(D7Parser.preReturn.length()).trim();
					if (!argument.isEmpty())
					{
						//code.add(_indent + this.getLineNumber() + this.procName + " = " + argument + " : END"); 
						code.add(this.getLineNumber() + _indent + "RETURN " + argument); 
					}
				}
				else if (line.matches(Matcher.quoteReplacement(D7Parser.preExit)+"([\\W].*|$)"))
				{
					code.add(this.getLineNumber() + _indent + "STOP");
				}
				// Has it already been matched with a loop? Then syntax must have been okay...
				else if (this.jumpTable.containsKey(_jump))
				{
					Integer ref = this.jumpTable.get(_jump);
					String label = this.labelBaseName + ref;
					if (ref.intValue() < 0)
					{
						insertComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
						insertComment(line, _indent);
						label = "__ERROR__";
					}
					code.add(this.getLineNumber() + _indent + "GOTO " + label);
					isEmpty = false;	// Leave the above loop now 
				}
				else if (!isEmpty)
				{
					insertComment("FIXME: Structorizer detected the following illegal jump attempt:", _indent);
					insertComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
			if (isEmpty && this.jumpTable.containsKey(_jump))
			{
				Integer ref = this.jumpTable.get(_jump);
				String label = this.labelBaseName + ref;
				if (ref.intValue() < 0)
				{
					insertComment("FIXME: Structorizer detected illegal jump attempt here!", _indent);
					label = "__ERROR__";
				}
				code.add(this.getLineNumber() + _indent + "GOTO " + label);
				isEmpty = false;	// Leave the above loop now 
			}
			// END KGU#78 2015-12-18
    	}
    }

	// START KGU#47 2015-12-18: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		String indentPlusOne = _indent + this.getIndent();
		insertComment(_para, _indent);

		code.add(this.getLineNumber());
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
		//code.add(this.getLineNumber() + _indent + "PARALLEL");

		for (int i = 0; i < _para.qs.size(); i++) {
			code.add(this.getLineNumber());
			insertComment("----------------- START THREAD " + i + " -----------------", indentPlusOne);
			//code.add(this.getLineNumber() + indentPlusOne + "THREAD");
			generateCode((Subqueue) _para.qs.get(i), indentPlusOne + this.getIndent());
			//code.add(this.getLineNumber() + indentPlusOne + "END THREAD");
			insertComment("------------------ END THREAD " + i + " ------------------", indentPlusOne);
			code.add(this.getLineNumber());
		}

		//code.add(this.getLineNumber() + _indent + "END PARALLEL");
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		code.add(this.getLineNumber());
	}
	// END KGU#47 2015-12-18
    
    
//    @Override
//    protected void generateCode(Subqueue _subqueue, String _indent)
//    {
//
//            // code.add(_indent+"");
//            for(int i=0; i<_subqueue.getSize(); i++)
//            {
//                    generateCode((Element) _subqueue.getElement(i), _indent);
//            }
//            // code.add(_indent+"");
//    }
	
// START KGU#74 2015-12-18: Decomposed and fine-tuned 
//    @Override
//    public String generateCode(Root _root, String _indent)
//    {
//
//            String pr = "REM program";
//            String indent = _indent;
//            if(_root.isProgram==false)
//            {
//            	pr="FUNCTION";
//            	indent = indent + this.getIndent();	// Within function declarations do indent (not within programs)
//            }
//            code.add(pr+" "+_root.getText().get(0));
//    		// START KGU 2015-11-02
//    		insertComment(_root, indent);
//    		// END KGU 2015-11-02
//            insertComment("TODO declare variables here: DIM AS type variable1, variable2, ...", indent);
//            code.add("");
//            generateCode(_root.children, indent);
//            code.add("");
//            if(_root.isProgram==false) {code.add(indent + "END FUNCTION");} else {code.add(indent + "REM END");}
//
//            return code.getText();
//    }
	/**
	 * Composes the heading for the program or function according to the
	 * syntactic rules of the target language and adds it to this.code.
	 * @param _root - The diagram root element
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		String furtherIndent = _indent;
		this.labelMap = new int[this.labelCount];
        String pr = this.commentSymbolLeft() + " program";
        this.procName = _procName;	// Needed for value return mechanisms

        insertComment(_root, _indent);
        insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);            
        
        String signature = _root.getMethodName();
        if (!_root.isProgram) {
        	boolean isFunction = _resultType != null || this.returns || this.isResultSet || this.isFunctionNameSet; 
        	pr = isFunction ? "FUNCTION" : "SUB";
        		
			// Compose the function header
        	signature += "(";
        	if (this.optionBasicLineNumbering())
        	{
        		insertComment("TODO: Add type-specific suffixes where necessary!", _indent);
        	}
        	else
        	{
        		insertComment("TODO: Check (and specify if needed) the argument and result types!", _indent);        		
        	}
			for (int p = 0; p < _paramNames.count(); p++) {
				signature += ((p > 0) ? ", " : "");
				signature += (_paramNames.get(p)).trim();
				if (_paramTypes != null)
				{
					String type = this.transformType(_paramTypes.get(p), "");
					if (!type.isEmpty())
					{
						signature += " AS " + type;
					}
				}
			}
			signature += ")";
			if (_resultType != null)
			{
				signature += " AS " + transformType(_resultType, "Real");
			}
			furtherIndent += this.getIndent();
        }
        code.add(this.getLineNumber() + _indent + pr + " " + signature);
       
		return furtherIndent;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		// Old BASIC dialocts with line numbers usually don't support declarations
		if (!this.optionBasicLineNumbering())
		{
			String indentPlusOne = _indent + this.getIndent();
			insertComment("TODO: declare your variables here:", _indent );
			for (int v = 0; v < _varNames.count(); v++) {
				insertComment("DIM " + _varNames.get(v) + " AS <type>", indentPlusOne);
			}
			insertComment("", _indent);
		}
		else
		{
			insertComment("TODO: add the respective type suffixes to your variable names if required", _indent );
		}
		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateResult(lu.fisch.structorizer.elements.Root, java.lang.String, boolean, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if (!_root.isProgram && (returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
		{
			String result = "0";
			if (isFunctionNameSet)
			{
				result = _root.getMethodName();
			}
			else if (isResultSet)
			{
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
			}
			code.add(this.getLineNumber() + _indent + "RETURN " + result);
		}
		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateFooter(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		String endPhrase = "END";
        if (!_root.isProgram)
        {
        	if (_root.getResultType() != null || this.returns || this.isResultSet || this.isFunctionNameSet)
        	{
        		endPhrase += " FUNCTION";
        	}
        	else
        	{
        		endPhrase += " SUB";
        	}
        }
		code.add(_indent + this.getLineNumber() + endPhrase);
		
		if (this.optionBasicLineNumbering())
		{
			// Okay now, in line numbering mode, we will have to replace the generic labels by line numbers
			for (int i = 0; i < code.count(); i++)
			{
				String line = code.get(i);
				int labelPos = line.indexOf(this.labelBaseName);
				if (labelPos >= 0)
				{
					// Supposed to be a jump instruction:
					// Identify the label number, look for the corresponding line number and replace the label by the latter
					int posLN = labelPos + this.labelBaseName.length();		// position of the label number
					String labelNoStr = line.substring(posLN);
					int labelNo = Integer.parseInt(labelNoStr);
					int lineNo = this.labelMap[labelNo];
					code.set(i, line.replace(this.labelBaseName + labelNoStr, Integer.toString(lineNo)));
				}
			}
		}
	}
	// END KGU#74 2015-12-18
	
}

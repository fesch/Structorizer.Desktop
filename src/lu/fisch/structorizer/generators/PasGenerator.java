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
 *      Description:    This class generates PAscal code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author              Date            Description
 *      ------              ----            -----------
 *      Bob Fisch           2007.12.27      First Issue
 *      Bob Fisch           2008.04.12      Added "Fields" section for generator to be used as plugin
 *      Bob Fisch           2008.11.17      Added Freepascal extensions
 *      Bob Fisch           2009.08.17      Bugfixes (see comment)
 *      Bob Fisch           2011.11.07      Fixed an issue while doing replacements
 *      Dirk Wilhelmi       2012.10.11      Added comments export
 *      Kay Gürtzig         2014.11.10      Conversion of C-like logical operators
 *      Kay Gürtzig         2014.11.16      Conversion of C-like comparison operator, comment export
 *      Kay Gürtzig         2014.12.02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig         2015.10.18      Comment generation and indentation revised
 *      Kay Gürtzig         2015.11.30      Enh. #23: Jump generation modified, KGU#47: Parallel generation
 *                                          added, Root generation fundamentally redesigned (decomposed)  
 *      Bob Fisch           2015.12.10      Bugfix #50 --> grep & export function parameter types
 *      Kay Gürtzig         2015.12.20      Bugfix #22 (KGU#74): Correct return mechanisms even with
 *                                          return instructions not placed in Jump elements
 *      Kay Gürtzig         2015.12.21      Bugfix #41/#68/#69 (= KG#93)
 *      Kay Gürtzig         2016.01.14      Enh. #84: array initialisation expressions decomposed (= KG#100)
 *      Kay Gürtzig         2016.01.17      Bugfix #61/#112 - handling of type names in assignments (KGU#109/KGU#141)
 *                                          KGU#142: Bugfix for enh. #23 - empty Jumps weren't translated
 *      Kay Gürtzig         2016.03.16      Enh. #84: Minimum support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig         2016-03-31      Enh. #144 - content conversion may be switched off
 *
 ******************************************************************************************************
 *
 *      Comments:
 *      
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops now provide more reliable loop parameters detection  
 *
 *      2015.10.18
 *      - Indentation increment with +_indent.substring(0,1) worked only for single-character indentation units
 *      - Interface of comment insertion methods modified
 *
 *      2014.11.16 - Bugfix / Enhancement
 *      - Conversion of C-style unequality operator had to be added
 *      - Comments are now exported, too
 *       
 *      2014.11.10 - Enhancement
 *      - Conversion of C-style logical operators to the Pascal-like ones added
 *      - assignment operator conversion now preserves or ensures surrounding spaces
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch", "repeat" & "if"
 *
 ******************************************************************************************************///

import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


public class PasGenerator extends Generator 
{
	
	// The method name of root
	protected String procName = "";
	
    /************ Fields ***********************/
    @Override
    protected String getDialogTitle()
    {
            return "Export Pascal Code ...";
    }

    @Override
    protected String getFileDescription()
    {
            return "Pascal / Delphi Source Code";
    }

    @Override
    protected String getIndent()
    {
            return "  ";
    }

    @Override
    protected String[] getFileExtensions()
    {
            String[] exts = {"pas", "dpr", "pp", "lpr"};
            return exts;
    }

    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "{";
    }

    @Override
    protected String commentSymbolRight()
    {
    	return "}";
    }
    // END KGU 2015-10-18

	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean breakMatchesCase()
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
		return "readln($1)";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "writeln($1)";
	}

	// START KGU#16 2015-11-30
	/**
	 * Transforms type identifier into the target language (as far as possible)
	 * @param _type - a string potentially meaning a datatype (or null)
	 * @param _default - a default string returned if _type happens to be null
	 * @return a type identifier (or the unchanged _type value if matching failed)
	 */
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		else {
			_type = _type.trim();
			if (_type.equalsIgnoreCase("long")) _type = "Longint";
			else if (_type.equalsIgnoreCase("int")) _type = "Longint";
			else if (_type.equalsIgnoreCase("integer")) _type = "Longint";
			else if (_type.equalsIgnoreCase("float")) _type = "Single";
			else if (_type.equalsIgnoreCase("real")) _type = "Single";
			else if (_type.equalsIgnoreCase("double")) _type = "Double";
			else if (_type.equalsIgnoreCase("longreal")) _type = "Double";
			else if (_type.equalsIgnoreCase("unsigned short")) _type = "Word";
			else if (_type.equalsIgnoreCase("short")) _type = "Smallint";
			else if (_type.equalsIgnoreCase("shortint")) _type = "Smallint";
			else if (_type.equalsIgnoreCase("unsigned int")) _type = "Cardinal";
			else if (_type.equalsIgnoreCase("unsigned long")) _type = "Cardinal";
			else if (_type.equalsIgnoreCase("bool")) _type = "Boolean";
			// To be continued if required...
		}
		return _type;
	}
	// END KGU#16 2015-11-30	

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by ":=" here
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm)
//	{
//		return _interm.replace(" <- ", " := ");
//	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// First get rid of superfluous spaces
		int pos = -1;
		StringList doubleBlank = StringList.explode(" \n ", "\n");
		while ((pos = tokens.indexOf(doubleBlank, 0, true)) >= 0)
		{
			tokens.delete(pos);	// Get rid of one of the blanks
		}
		// On inserting operator keywords we better make sure them being padded
		// (lest neighbouring identifiers be glued to them on concatenating)
		// The correct way would of course be to add blank tokens where needed
		// but this seemed too expensive here.
        tokens.replaceAll("==", "=");
        tokens.replaceAll("!=","<>");
        tokens.replaceAll("%"," mod ");
        tokens.replaceAll("&&"," and");
        tokens.replaceAll("||"," or ");
        tokens.replaceAll("!"," not ");
        tokens.replaceAll("&"," and");
        tokens.replaceAll("|"," or ");
        tokens.replaceAll("~"," not ");
        tokens.replaceAll("<<"," shl ");
        tokens.replaceAll(">>"," shr ");
		tokens.replaceAll("<-", ":=");
		String result = tokens.concatenate();
		// We now shrink superfluous padding - this may affect string literals, though!
		result = result.replace("  ", " ");
		result = result.replace("  ", " ");	// twice to catch odd-numbered space sequences, too
		return result;
	}
	// END KGU#93 2015-12-21
	// END KGU#18/KGU#23 2015-11-01
    

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		String transline = super.transform(_input);

		// START KGU#93 2015-12-21: Bugfix #41/#68/#69 - overriding no longer needed		
//            // START KGU 2014-11-16: C comparison operator required transformation, too
//            _input=BString.replace(_input," != "," <> ");
//            _input=BString.replace(_input," == "," = ");
//            // END KGU 2014-11-16
//            // START KGU 2014-11-10: logical operators required transformation, too
//            _input=BString.replace(_input," && "," and ");
//            _input=BString.replace(_input," || "," or ");
//            _input=BString.replace(_input," ! "," not ");
//            // END KGU 2014-11-10
//            // START KGU 2014-11-16: C bit operators required transformation, too
//            _input=BString.replace(_input," ~ "," not ");
//            _input=BString.replace(_input," & "," and ");
//            _input=BString.replace(_input," | "," or ");
//            _input=BString.replace(_input,"~"," not ");
//            _input=BString.replace(_input,"&"," and ");
//            _input=BString.replace(_input,"|"," or ");
//            _input=BString.replace(_input," << "," shl ");
//            _input=BString.replace(_input," >> "," shr ");
//            _input=BString.replace(_input,"<<"," shl ");
//            _input=BString.replace(_input,">>"," shr ");
//            // END KGU 2014-11-16
//            // START KGU 2015-11-02
//            _input = _input.replace(" % ", " mod ");
//            // END KGU 2015-11-02
//
//            _input.replace("  ", " ");
//            _input.replace("  ", " ");
//            return _input.trim();
// END KGU#93 2015-12-21

		// START KGU#109/KGU#141 2016-01-16: Bugfix #61,#112 - suppress type specifications
		// (This must work both in Instruction and Call elements)
		int asgnPos = transline.indexOf(":=");
		if (asgnPos > 0)
		{
			String varName = transline.substring(0, asgnPos);
			String expr = transline.substring(asgnPos + ":=".length());
			String[] typeNameIndex = this.lValueToTypeNameIndex(varName);
			varName = typeNameIndex[1];
			String index = typeNameIndex[2];
			if (!index.isEmpty())
			{
				varName = varName + "["+index+"]";
			}
			transline = varName + " := " + expr;
		}
		// END KGU#109/KGU#141 2016-01-16
		
		return transline.trim(); 
    }
	
	// START KGU#61 2016-03-23: New for enh. #84
	private void insertDeclaration(String _category, String text, int _maxIndent)
	{
		int posDecl = -1;
		String seekIndent = "";
		while (posDecl < 0 && seekIndent.length() < _maxIndent)
		{
			posDecl = code.indexOf(seekIndent + _category);
			seekIndent += this.getIndent();
		}
		code.insert(seekIndent + text, posDecl + 1);
	}
	// END KGU#61 2016-03-23

    @Override
    protected void generateCode(Instruction _inst, String _indent)
    {
    	// START KGU 2015-10-18: The "export instructions as comments" configuration had been ignored here
//		insertComment(_inst, _indent);
//		for(int i=0;i<_inst.getText().count();i++)
//		{
//			code.add(_indent+transform(_inst.getText().get(i))+";");
//		}
		if (!insertAsComment(_inst, _indent)) {
			
			insertComment(_inst, _indent);

			for (int i=0; i<_inst.getText().count(); i++)
			{
				// START KGU#74 2015-12-20: Bug #22 There might be a return outside of a Jump element, handle it!
				//code.add(_indent+transform(_inst.getText().get(i))+";");
				String line = _inst.getText().get(i).trim();
				if (line.matches(getKeywordPattern(D7Parser.preReturn)+"([\\W].*|$)"))
				{
					String argument = line.substring(D7Parser.preReturn.length()).trim();
					if (!argument.isEmpty())
					{
						code.add(_indent + this.procName + " := " + transform(argument) + ";"); 
					}
					Subqueue sq = (_inst.parent == null) ? null : (Subqueue)_inst.parent;
					if (sq == null || !(sq.parent instanceof Root) || sq.getIndexOf(_inst) != sq.getSize()-1 ||
							i+1 < _inst.getText().count())
					{
						code.add(_indent + "exit;");
					}
				}
				else	// no return
				{
					// START KGU#100 2016-01-14: Enh. #84 - resolve array initialisation
					// The crux is: we don't know the index range!
					// So we'll invent an index base variable easy to be modified in code
					//code.add(_indent + transform(line) + ";");
					String transline = transform(line);
					int asgnPos = transline.indexOf(":=");
					boolean isArrayInit = false;
					if (asgnPos >= 0 && transline.contains("{") && transline.contains("}"))
					{
						String varName = transline.substring(0, asgnPos).trim();
						String expr = transline.substring(asgnPos+2).trim();
						isArrayInit = expr.startsWith("{") && expr.endsWith("}");
						if (isArrayInit)
						{
							StringList elements = Element.splitExpressionList(
									expr.substring(1, expr.length()-1), ",");
							// In order to be consistent with possible index access
							// at other positions in code, we use the standard Java
							// index range here (though in Pascal indexing usually 
							// starts with 1 but may vary widely). We solve the problem
							// by providing a configurable start index variable 
							insertComment("TODO: Check indexBase value (automatically generated)", _indent);
							insertDeclaration("var", "indexBase_" + varName + ": Integer = 0;",
									_indent.length());
							for (int el = 0; el < elements.count(); el++)
							{
								code.add(_indent + varName + "[indexBase_" + varName + " + " + el + "] := " + 
										elements.get(el) + ";");
							}
						}
						
					}
					if (!isArrayInit)
					{
						code.add(_indent + transline + ";");
					}
					// END KGU#100 2016-01-14
				}
				// END KGU#74 2015-12-20
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
            
            String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
            if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

            code.add(_indent+"if "+condition+" then");
            code.add(_indent+"begin");
            generateCode(_alt.qTrue,_indent+this.getIndent());
            if(_alt.qFalse.getSize()!=0)
            {
                    code.add(_indent+"end");
                    code.add(_indent+"else");
                    code.add(_indent+"begin");
                    generateCode(_alt.qFalse,_indent+this.getIndent());
            }
            code.add(_indent+"end;");
    }

    @Override
    protected void generateCode(Case _case, String _indent)
    {
            // START KGU 2014-11-16
            insertComment(_case, _indent);
            // END KGU 2014-11-16

            String condition = transform(_case.getText().get(0));
            if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

            code.add(_indent+"case "+condition+" of");

            for(int i=0;i<_case.qs.size()-1;i++)
            {
                    code.add(_indent+this.getIndent()+_case.getText().get(i+1).trim()+":");
                    code.add(_indent+this.getIndent()+this.getIndent()+"begin");
                    generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent()+this.getIndent()+this.getIndent());
                    code.add(_indent+this.getIndent()+this.getIndent()+"end;");
            }

            if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
            {
                    code.add(_indent+this.getIndent()+"else");
                    generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+this.getIndent()+this.getIndent());
            }
            code.add(_indent+"end;");
    }

    @Override
    protected void generateCode(For _for, String _indent)
    {
            // START KGU 2014-11-16
            insertComment(_for, _indent);
            // END KGU 2014-11-16

        	// START KGU#61 2016-03-23: Enh. 84
        	if (_for.isForInLoop() && generateForInCode(_for, _indent))
        	{
        		// All done
        		return;
        	}
        	// END KGU#61 2016-03-23

        	// START KGU#3 2015-11-02: New reliable loop parameter mechanism
    		//code.add(_indent+"for "+BString.replace(transform(_for.getText().getText()),"\n","").trim()+" do");
            //code.add(_indent + "begin");
            //generateCode(_for.q, _indent+this.getIndent());
        	String counter = _for.getCounterVar();
            int step = _for.getStepConst();
            if (Math.abs(step) == 1)
            {
            	// We may employ a For loop
                String incr = (step == 1) ? " to " : " downto ";
        		code.add(_indent + "for " + counter + " := " + transform(_for.getStartValue(), false) +
        				incr + transform(_for.getEndValue(), false) + " do");
            }
            else
            {
            	// While loop required
            	code.add(_indent + counter + " := " + transform(_for.getStartValue(), false));
            	code.add(_indent + "while " + counter + ((step > 0) ? " <= " : " >= ") + transform(_for.getEndValue(), false) + " do");
            }
            code.add(_indent + "begin");
            generateCode(_for.q, _indent+this.getIndent());
            if (Math.abs(step) != 1)
            {
            	code.add(_indent + this.getIndent() + counter + " := " + counter + ((step > 0) ? " + " : " ") + step ); 
            }
            // END KGU#3 2015-11-02
            code.add(_indent + "end;");
            
            // START KGU#74 2015-11-30: The following instruction is goto target
            if (this.jumpTable.containsKey(_for))
            {
            	code.add(_indent + "StructorizerLabel_" + this.jumpTable.get(_for).intValue() + ": ;");
            }
            // END KGU 2015-11-30
    }

	// START KGU#61 2016-03-23: Enh. #84 - Support for FOR-IN loops
	/**
	 * We try our very best to create a working loop from a FOR-IN construct
	 * This will only work, however, if we can get reliable information about
	 * the size of the value list, which won't be the case if we obtain it e.g.
	 * via a variable.
	 * @param _for - the element to be exported
	 * @param _indent - the current indentation level
	 * @return true iff the method created some loop code (sensible or not)
	 */
	protected boolean generateForInCode(For _for, String _indent)
	{
		boolean done = false;
		String var = _for.getCounterVar();
		StringList items = this.extractForInListItems(_for);
		if (items != null)
		{
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogenous? We will just try four types: boolean,
			// integer, real and string, where we can only test literals.
			// If none of them match then we add a TODO comment.
			int nItems = items.count();
			boolean allBoolean = true;
			boolean allInt = true;
			boolean allReal = true;
			boolean allString = true;
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
				if (allBoolean)
				{
					if (!item.equalsIgnoreCase("true") && !item.equalsIgnoreCase("false"))
					{
						allBoolean = false;
					}
				}
				if (allInt)
				{
					try {
						Integer.parseInt(item);
					}
					catch (NumberFormatException ex)
					{
						allInt = false;
					}
				}
				if (allReal)
				{
					try {
						Double.parseDouble(item);
					}
					catch (NumberFormatException ex)
					{
						allReal = false;
					}
				}
				if (allString)
				{
					allString = item.startsWith("\"") && item.endsWith("\"") &&
							!item.substring(1, item.length()-1).contains("\"") ||
							item.startsWith("\'") && item.endsWith("\'") &&
							!item.substring(1, item.length()-1).contains("\'");
				}
			}
			
			// Create some generic and unique variable names
			String postfix = Integer.toHexString(_for.hashCode());
			String arrayName = "array" + postfix;
			String indexName = "index" + postfix;

			String itemType = "";
			if (allBoolean) itemType = "boolean";
			else if (allInt) itemType = "integer";
			else if (allReal) itemType = "real";
			else if (allString) itemType = "string";
			else {
				itemType = "FIXME_" + postfix;
				// We do a dummy type definition
				this.insertComment("TODO: Specify an appropriate element type for the array!", _indent);
			}

			// Insert the array and index declarations
			String range = "1..." + items.count();
			insertDeclaration("var", arrayName + ": " + "array [" + 
					range + "] of " + itemType + ";", _indent.length());
			insertDeclaration("var", indexName + ": " + range + ";",
					_indent.length());

			// Now we create code to fill the array with the enumerated values
			for (int i = 0; i < nItems; i++)
			{
				code.add(_indent + arrayName + "[" + (i+1) + "] := " + items.get(i) + ";");
			}
			
			// Creation of the loop header
    		code.add(_indent + "for " + indexName + " := 1 to " + nItems + " do");

    		// Creation of the loop body
            code.add(_indent + "begin");
            code.add(_indent+this.getIndent() + var + " := " + arrayName + "[" + indexName + "];");
            generateCode(_for.q, _indent+this.getIndent());
            code.add(_indent + "end;");

            done = true;
		}
		else
		{
			String valueList = _for.getValueList();
			// We have no strategy here, no idea how to find out the number and type of elements,
			// no idea how to iterate the members, so we leave it similar to Delphi and just add a TODO comment...
			this.insertComment("TODO: Rewrite this loop (there was no way to convert this automatically)", _indent);

			// Creation of the loop header
			code.add(_indent + "for " + var + " in " + transform(valueList, false) + " do");
			// Add the loop body as is
            code.add(_indent + "begin");
			generateCode(_for.q, _indent + this.getIndent());
            code.add(_indent + "end;");
			
			done = true;
		}
		return done;
	}
	// END KGU#61 2016-03-23

	@Override
    protected void generateCode(While _while, String _indent)
    {
            // START KGU 2014-11-16
            insertComment(_while, _indent);
            // END KGU 2014-11-16

            String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
            if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

            code.add(_indent+"while "+condition+" do");
            code.add(_indent+"begin");
            generateCode(_while.q,_indent+this.getIndent());
            code.add(_indent+"end;");

            // START KGU#74 2015-11-30: The following instruction is goto target
            if (this.jumpTable.containsKey(_while))
            {
            	code.add(_indent + "StructorizerLabel_" + this.jumpTable.get(_while).intValue() + ": ;");
            }
            // END KGU 2015-11-30
    }

    @Override
    protected void generateCode(Repeat _repeat, String _indent)
    {
            // START KGU 2014-11-16
            insertComment(_repeat, _indent);
            // END KGU 2014-11-16

            String condition = BString.replace(transform(_repeat.getText().getText()),"\n","").trim();
            if(!condition.startsWith("(") && !condition.endsWith(")")) condition="("+condition+")";

            code.add(_indent+"repeat");
            generateCode(_repeat.q,_indent+this.getIndent());
            code.add(_indent+"until "+condition+";");

            // START KGU#74 2015-11-30: The following instruction is goto target
            if (this.jumpTable.containsKey(_repeat))
            {
            	code.add(_indent + "StructorizerLabel_" + this.jumpTable.get(_repeat).intValue() + ": ;");
            }
            // END KGU 2015-11-30
    }

    @Override
    protected void generateCode(Forever _forever, String _indent)
    {
            // START KGU 2014-11-16
            insertComment(_forever, _indent);
            // END KGU 2014-11-16

            code.add(_indent+"while (true) do");
            code.add(_indent+"begin");
            generateCode(_forever.q,_indent+this.getIndent());
            code.add(_indent+"end;");

            // START KGU#74 2015-11-30: The following instruction is goto target
            if (this.jumpTable.containsKey(_forever))
            {
            	code.add(_indent + "StructorizerLabel_" + this.jumpTable.get(_forever).intValue() + ": ;");
            }
            // END KGU 2015-11-30
    }
	
    @Override
    protected void generateCode(Call _call, String _indent)
    {
            // START KGU 2014-11-16
            insertComment(_call, _indent);
            // END KGU 2014-11-16

            for(int i=0;i<_call.getText().count();i++)
            {
                    code.add(_indent+transform(_call.getText().get(i))+";");
            }
    }

    @Override
    protected void generateCode(Jump _jump, String _indent)
    {
		if (!insertAsComment(_jump, _indent)) {

			insertComment(_jump, _indent);

			// KGU 2015-11-30: In Pascal, there is no break and no goto,
			// so empty Jumps won't be allowed
			// We will just have to translate exit into halt and return into exit
			boolean isEmpty = true;
			
			StringList lines = _jump.getText();
			// START KGU#142 2016-01-17: fixes Enh. #23 The following code had been
			// misplaced inside the text line loop, it belongs to the top (no further
			// analysis required):
			// Has it already been matched with a loop? Then syntax must have been okay...
			if (this.jumpTable.containsKey(_jump))
			{
				Integer ref = this.jumpTable.get(_jump);
				String label = "StructorizerLabel_" + ref;
				if (ref.intValue() < 0)
				{
					insertComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
					insertComment(lines.getLongString(), _indent);
					label = "__ERROR__";
				}
				else
				{
					insertComment("WARNING: Most Pascal compilers don't support jump instructions!", _indent);					
				}
				code.add(_indent + "goto" + " " + label + ";");
			}
			else
			{
			// END KGU#142 2016-01-17
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
							code.add(_indent + this.procName + " := " + argument + ";"); 
						}
						// START KGU 2016-01-17: Omit the exit if this is the last instruction of the diagram
						//code.add(_indent + "exit;");
						if (i < lines.count()-1 || !((_jump.parent).parent instanceof Root))
						{
							code.add(_indent + "exit;");
						}
						// END KGU 2016-01-17
					}
					else if (line.matches(Matcher.quoteReplacement(D7Parser.preExit)+"([\\W].*|$)"))
					{
						String argument = line.substring(D7Parser.preExit.length()).trim();
						if (!argument.isEmpty()) { argument = "(" + argument + ")"; }
						code.add(_indent + "halt" + argument + ";");
					}
					// START KGU#142 2016-01-17: This belonged to the top (no further analysis required)
					//				else if (this.jumpTable.containsKey(_jump))
					//				{
					//					Integer ref = this.jumpTable.get(_jump);
					//					String label = "StructorizerLabel_" + ref;
					//					if (ref.intValue() < 0)
					//					{
					//						insertComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
					//						insertComment(line, _indent);
					//						label = "__ERROR__";
					//					}
					//					else
					//					{
					//						insertComment("WARNING: Most Pascal compilers don't support jump instructions!", _indent);					
					//					}
					//					code.add(_indent + "goto" + " " + label + ";");
					//				}
					// END KGU#142 2016-01-17
					else if (!isEmpty)
					{
						insertComment("FIXME: Structorizer detected the following illegal jump attempt:", _indent);
						insertComment(line, _indent);
					}
					// END KGU#74/KGU#78 2015-11-30
				}
				if (isEmpty) {
					insertComment("FIXME: An empty jump was found here! Cannot be translated to " +
							this.getFileDescription(), _indent);
				}
			// START KGU#142 2016-01-17: Bugfix for enh. #23 (continued)
			}
			// END KGU#142 2016-01-17
		}
    }

//    @Override
//    protected void generateCode(Subqueue _subqueue, String _indent)
//    {
//            // code.add(_indent+"");
//            for(int i=0;i<_subqueue.getSize();i++)
//            {
//                    generateCode((Element) _subqueue.getElement(i),_indent);
//            }
//            // code.add(_indent+"");
//    }

	// START KGU#47 2015-11-30: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		// START KGU 2014-11-16
		insertComment(_para, _indent);
		// END KGU 2014-11-16

		code.add("");
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
		code.add(_indent + "begin");

		for (int i = 0; i < _para.qs.size(); i++) {
			code.add("");
			insertComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			code.add(_indent + this.getIndent() + "begin");
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			code.add(_indent + this.getIndent() + "end;");
			insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			code.add("");
		}

		code.add(_indent + "end;");
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		code.add("");
	}
	// END KGU#47 2015-11-30

// KGU#74 2015-11-30: Now using the decomposed sub-methods     
//    @Override
//    public String generateCode(Root _root, String _indent)
//    {
//            String pr = "program";
//            if(_root.isProgram==false) {pr="function";}
//
//            // START KGU 2014-11-16
//            insertComment(_root, _indent);
//            // END KGU 2014-11-16
//            // START KGU 2015-10-18
//            insertComment("(Generated by Structorizer)", _indent);            
//            // END KGU 2015-10-18
//            
//            // START KGU 2015-10-18
//            //code.add(_indent + pr+" "+_root.getText().get(0)+";");
//            String signature = _root.getMethodName();
//            if (!_root.isProgram) {
//            	insertComment("TODO declare the parameters and specify the result type!", _indent);
//                StringList paraNames = _root.getParameterNames();
//                signature = signature + "(" + BString.replace(paraNames.getText(), "\n", "; ") + ")";
//            }
//            code.add(_indent + pr + " " + signature + ";");
//            // END KGU 2015-10-18
//            code.add("");
//            // START KGU 2014-11-16: comment syntax corrected
//            //code.add("// declare your variables here");
//            insertComment("TODO declare your variables here", _indent);
//            // END KGU 2014-11-16
//            code.add("");
//            code.add(_indent + "begin");
//            generateCode(_root.children, _indent + this.getIndent());
//            code.add(_indent + "end.");
//
//            return code.getText();
//    }
	
	// START KGU#74 2015-11-30 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
        String pr = "program";
        this.procName = _procName;	// Needed for value return mechanisms

        insertComment(_root, _indent);
        insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);            
        
        String signature = _root.getMethodName();
        if (!_root.isProgram) {
        	pr = "function";
			// Compose the function header
        	signature += "(";
        	insertComment("TODO: declare the parameters and specify the result type!", _indent);
			for (int p = 0; p < _paramNames.count(); p++) {
				signature += ((p > 0) ? "; " : "");
				signature += (_paramNames.get(p) + ": " + transformType(_paramTypes.get(p), "{type?}")).trim();
			}
			signature += ")";
			if (_resultType != null || this.returns || this.isResultSet || this.isFunctionNameSet)
			{
				_resultType = transformType(_root.getResultType(), "Integer");
				signature += ": " + _resultType;
			}
			else 
			{
				pr = "procedure";
			}
        }
        code.add(_indent + pr + " " + signature + ";");
        
        if (this.labelCount > 0)
        {
        	// Declare the used labels
        	code.add(_indent);
        	code.add(_indent + "label");
        	for (int lb = 0; lb < this.labelCount; lb++)
        	{
        			code.add(_indent + this.getIndent() + "StructorizerLabel_" + lb + ";");
        	}
        }
        code.add("");
        code.add(_indent + "var");
        
		return _indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
        insertComment("TODO: declare your variables here", _indent + this.getIndent());
		for (int v = 0; v < _varNames.count(); v++) {
			insertComment(_varNames.get(v), _indent + this.getIndent());
		}
        code.add("");
        code.add(_indent + "begin");

		return _indent + this.getIndent();
	}

	// START KGU#74 2015-12-20: Enh. #22: We must achieve a correct value assignment to the function name
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if (!_root.isProgram)
		{
			String varName = "";
			if (isResultSet && !isFunctionNameSet && !alwaysReturns)
			{
				int vx = varNames.indexOf("result", false);
				varName = varNames.get(vx);
				code.add(_indent);
				code.add(_indent + this.getIndent() + _root.getMethodName() + " := " + varName + ";");
			}
		}
		return _indent;
	}
	// END KGU#74 2015-12-20

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateFooter(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		code.add(_indent + "end.");
	}
	// END KGU#74 2015-11-30
	
}

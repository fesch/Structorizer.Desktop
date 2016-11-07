/*
    This file is part of Structorizer.

    Structorizer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structorizer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 ***********************************************************************

    Perl Source Code Generator

    Copyright (C) 2008 Jan Peter Klippel

    This file has been released under the terms of the GNU Lesser General
    Public License as published by the Free Software Foundation.

    http://www.gnu.org/licenses/lgpl.html

 */

package lu.fisch.structorizer.generators;

/*
 ******************************************************************************************************
 *
 *      Author:         Jan Peter Klippel
 *
 *      Description:    Perl Source Code Generator
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Jan Peter Klippel 2008.04.11    First Issue
 *      Bob Fisch       2008.04.12		Added "Fields" section for generator to be used as plugin
 *      Bob Fisch       2009.01.18		Corrected the FOR-loop
 *      Bob Fisch       2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig     2014.12.02      Additional replacement of operator "<--" by "<-"
 *      Kay Gürtzig     2015.10.18      Indentation and comment insertion revised
 *      Kay Gürtzig     2015.11.02      Reorganisation of the transformation, input/output corrected
 *      Kay Gürtzig     2015.11.02      Variable detection and renaming introduced (KGU#62)
 *                                      Code generation for Case elements (KGU#15) and For
 *                                      loops (KGU#3) revised
 *      Kay Gürtzig     2015.12.12      Bugfix #57 (KGU#103) endless loops / flaws on variable prefixing
 *      Kay Gürtzig     2015.12.17      Enh. #23 (KGU#78) jump generation revised; Root generation
 *                                      decomposed according to Generator.generateCode(Root, String);
 *                                      Enh. KGU#47: Dummy implementation for Parallel element
 *                                      Fixes in FOR and REPEAT export
 *      Kay Gürtzig     2015.12.21      Bugfix #41/#68/#69 (= KGU#93)
 *      Kay Gürtzig     2015.12.21      Bugfix #51 (= KGU#108) Didn't cope with empty input / output
 *      Kay Gürtzig     2016.03.22      Enh. #84 (= KGU#61) varNames now inherited, FOR-IN loop support
 *      Kay Gürtzig     2016.03.23      Enh. #84: Support for FOREACH loops (KGU#61)
 *      Kay Gürtzig     2016-04-01      Enh. #144: Care for the new export option suppressing content conversion
 *      Kay Gürtzig     2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178) 
 *      Kay Gürtzig     2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig     2016.09.25      Enh. #253: D7Parser.keywordMap refactoring done. 
 *      Kay Gürtzig     2016.10.14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig     2016.10.15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig     2016.10.16      Enh. #274: Colour info for Turtleizer procedures added
 *
 ******************************************************************************************************
 *
 *      Comment:		LGPL license (http://www.gnu.org/licenses/lgpl.html).
 *
 ******************************************************************************************************
 */


import java.util.regex.Matcher;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.ILoop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

public class PerlGenerator extends Generator {
	
	// START KGU#61 2016-03-22: Now provided by Generator class
	// (KGU 2015-11-02) We must know all variable names in order to prefix them with '$'.
	//StringList varNames = new StringList();
	// END KGU#61 2016-03-22

	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export Perl Code ...";
	}
	
	protected String getFileDescription()
	{
		return "Perl Source Code";
	}
	
	protected String getIndent()
	{
		return "    ";	// KGU 2015-12-17: Changed from " " to "    " (www.tutorialspoint.com/perl/perl_coding_standard.html)
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"pl", "pm"};
		return exts;
	}
	
    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "#";
    }
    // END KGU 2015-10-18

	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean breakMatchesCase()
	{
		return true;
	}
	// END KGU#78 2015-12-18
	
	// START KGU 2016-08-12: Enh. #231 - information for analyser
    private static final String[] reservedWords = new String[]{
		"and", "cmp", "continue", "do",
		"else", "elsif", "eq", "exp",
		"for", "foreach", "ge", "gt",
		"if", "le", "lock", "lt", "ne", "no", "or",
		"package", "qq", "qr", "qw", "qx", 
		"sub", "tr", "unless", "until", "while", "xor"
		};
	public String[] getReservedWords()
	{
		return reservedWords;
	}
	public boolean isCaseSignificant()
	{
		return false;
	}
	// END KGU 2016-08-12
	
	
	/************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @param withPrompt - is a prompt string to be considered?
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer()
	//{
	//	// START KGU#108 2015-12-22: Bugfix #51
	//	//return "$1 = <STDIN>";
	//	return "$1 = <STDIN>; chomp $1";
	//	// END KGU#108 2015-12-22
	//}
	protected String getInputReplacer(boolean withPrompt)
	{
		if (withPrompt) {
			return "print $1; $2 = <STDIN>; chomp $2";
		}
		return "$1 = <STDIN>; chomp $1";
	}
	// END KGU#281 2016-10-15

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		// START KGU#103 2015-12-12: Bugfix #57 - Too few backslashes - were consumed by the regex replacement 
		//return "print $1, \"\\n\"";
		return "print $1, \"\\\\n\"";
		// END KGU#103 2015-12-12
	}

	// START KGU#93 2015-12-21 Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by "="
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm)
//	{
//		return _interm.replace(" <- ", " = ");
//	}
    
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#62/KGU#103 2015-12-12: Bugfix #57 - We must work based on a lexical analysis
    	for (int i = 0; i < varNames.count(); i++)
    	{
    		String varName = varNames.get(i);
    		//System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
    		//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
    		tokens.replaceAll(varName, "$"+varName);
    	}
		// END KGU#62/KGU#103 2015-12-12
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		// START KGU#61 2016-03-23: Enh. #84 - prepare array literals
		tokens.replaceAll("{", "(");
		tokens.replaceAll("}", ")");
		// END KGU#61 2016-03-23
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21
	
	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#61 2016-03-23: Enh. #84 (Array/List support)
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		// Set array variable name prefix in assignments with array initialiser
		// START KGU#162 2016-04-01: Enh. #144 - hands off in "no conversion" mode
		if (!this.suppressTransformation)
		{
		// END KGU#162 2016-04-01
			_input = Element.unifyOperators(_input);
			int asgnPos = _input.indexOf("<-");	// This might mutilate string literals!
			if (asgnPos > 0)
			{
				String lval = _input.substring(0, asgnPos).trim();
				String expr = _input.substring(asgnPos + "<-".length()).trim();
				if (expr.startsWith("{") && expr.endsWith("}") && this.varNames.contains(lval))
				{
					_input = "@" + lval + " <- " + expr;				
				}
			}
		// START KGU#162 2016-04-01: Enh. #144 - hands off in "no conversion" mode
		}
		// END KGU#162 2016-04-01

		return super.transform(_input).trim();
	}
	// END KGU#61 2016-03-23
	
	// START KGU#108 2015-12-22: Bugfix #51
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	@Override
	protected String transformInput(String _interm)
	{
		String transformed = super.transformInput(_interm);
		if (transformed.startsWith(" = <STDIN>"))
		{
			transformed = "my $dummy = <STDIN>";
		}
		// START KGU#281 2016-10-16: Enh. #271
		else
		{
			transformed = transformed.replace(";  = <STDIN>; chomp", "; my $dummy = <STDIN>");
		}
		// END KGU#281 2016-10-16
		return transformed;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformInput(java.lang.String)
	 */
	@Override
	protected String transformOutput(String _interm)
	{
		String transformed = super.transformOutput(_interm);
		if (transformed.startsWith("print , "))
		{
			transformed = transformed.replace("print , ", "print ");
		}
		return transformed;
	}
	// END KGU#108 2015-12-22
	
    // START KGU#78 2015-12-17: Enh. #23 (jump translation)
    // Places a label with empty instruction into the code if elem is an exited loop
	protected void appendLabel(Element elem, String _indent)
	{
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem)) {
			addCode(this.labelBaseName + this.jumpTable.get(elem) + ": ;",
					_indent, elem.isDisabled());
		}
	}
	// END KGU#78 2015-12-17

	protected void generateCode(Instruction _inst, String _indent) {

		if (!insertAsComment(_inst, _indent))
		{
			boolean isDisabled = _inst.isDisabled();
	    	insertComment(_inst, _indent);

			for(int i=0;i<_inst.getText().count();i++)
			{
				String text = transform(_inst.getText().get(i));
				if (!text.endsWith(";")) { text += ";"; }
				// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
				//code.add(_indent + text);
				if (Instruction.isTurtleizerMove(_inst.getText().get(i))) {
					text += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
				}
				addCode(text, _indent, isDisabled);
				// END KGU#277/KGU#284 2016-10-13
			}
		}

	}
	
	protected void generateCode(Alternative _alt, String _indent) {
		
		boolean isDisabled = _alt.isDisabled();
		
		addCode("", "", isDisabled);

		insertComment(_alt, _indent);

		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		//code.add(_indent+"if ( "+BString.replace(transform(_alt.getText().getText()),"\n","").trim()+" ) {");
		String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
		if (!this.suppressTransformation || !(condition.startsWith("(") && condition.endsWith(")")))
		{
			condition = "( " + condition + " )";
		}
		addCode("if " + condition + " {", _indent, isDisabled);
		generateCode(_alt.qTrue,_indent+this.getIndent());
		
		if(_alt.qFalse.getSize()!=0) {
			
			addCode("}", _indent, isDisabled);
			addCode("else {", _indent, isDisabled);			
			generateCode(_alt.qFalse,_indent+this.getIndent());
			
		}
		
		addCode("}", _indent, isDisabled);
		addCode("", "", isDisabled);
		
	}
	
	protected void generateCode(Case _case, String _indent) {
		
		boolean isDisabled = _case.isDisabled();
		
		addCode("", "", isDisabled);

		insertComment(_case, _indent);

		// Since Perl release 5.8.0, switch is a standard module...
		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		//code.add(_indent+"switch ( "+transform(_case.getText().get(0))+" ) {");
		String selector = transform(_case.getText().get(0));
		if (!this.suppressTransformation || !(selector.startsWith("(") && selector.endsWith(")")))
		{
			selector = "( " + selector + " )";			
		}
		addCode("switch " + selector + " {", _indent, isDisabled);
		// END KGU#162 2016-04-01
		
		for (int i=0; i<_case.qs.size()-1; i++)
		{
			addCode("", "", isDisabled);
			// START KGU#15 2015-11-02: Support multiple constants per branch
			//code.add(_indent+this.getIndent()+"case ("+_case.getText().get(i+1).trim()+") {");
			String conds = _case.getText().get(i+1).trim();
			if (Element.splitExpressionList(conds, ",").count() > 1)	// Is it an enumeration of values? 
			{
				conds = "[" + conds + "]";
			}
			else
			{
				conds = "(" + conds + ")";
			}
			addCode("case " + conds +" {", _indent + this.getIndent(), isDisabled);
			// END KGU#15 2015-11-02
			//code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1)+"begin");
			generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("}", _indent + this.getIndent(), isDisabled);
		}
		
		if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		{

			addCode("", "", isDisabled);
			addCode("else {", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1), _indent + this.getIndent() + this.getIndent());
			addCode("}", _indent + this.getIndent(), isDisabled);
		}
		addCode("}", _indent, isDisabled);
		addCode("", "", isDisabled);
		
	}
	
	
	protected void generateCode(For _for, String _indent) {
		
		boolean isDisabled = _for.isDisabled();
		
		addCode("", "", isDisabled);
		
    	insertComment(_for, _indent);

    	String var = _for.getCounterVar();
		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		if (!var.startsWith("$"))
		{
			var = "$" + var;
		}
		// END KGU#162 2016-04-01
    	// START KGU#61 2016-03-23: Enh. 84 - FOREACH support
    	if (_for.isForInLoop())
    	{
    		String valueList = _for.getValueList();
    		StringList items = this.extractForInListItems(_for);
    		if (items != null)
    		{
        		valueList = "@array20160323";
    			addCode(valueList + " = (" + transform(items.concatenate(", "), false) + ")",
    					_indent, isDisabled);
    		}
    		else
    		{
    			valueList = transform(valueList, false);
    			if (!this.suppressTransformation && valueList.startsWith("$"))
    			{
    				valueList = "@" + valueList.substring(1);
    			}
    		}
    		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
    		//code.add(_indent + "foreach $"+ var + " (" + valueList + ") {");
    		addCode("foreach "+ var + " (" + valueList + ") {", _indent, isDisabled);
    		// END KGU#162
    	}
    	else
    	{
    	// END KGU#61 2016-03-23
    		int step = _for.getStepConst();
    		String compOp = (step > 0) ? " <= " : " >= ";
    		// START KGU#162 2016-04-01: Enh. #144 var syntax already handled 
    		//String increment = "$" + var + " += (" + step + ")";
    		//code.add(_indent + "for ($" +
    		//	var + " = " + transform(_for.getStartValue(), false) + "; $" +
    		//		var + compOp + transform(_for.getEndValue(), false) + "; " +
    		//		increment +
    		//		") {");
    		String increment = var + " += (" + step + ")";
    		addCode("for (" +
    				var + " = " + transform(_for.getStartValue(), false) + "; " +
    				var + compOp + transform(_for.getEndValue(), false) + "; " +
    				increment +
    				") {", _indent, isDisabled);
    		// END KGU#162 2016-04-01
    	// START KGU#61 2016-03-23: Enh. 84 - FOREACH support (part 2)
    	}
    	// END KGU#61 2016-03-23
		// END KGU#3 2015-11-02
		generateCode(_for.q, _indent+this.getIndent());
		addCode("}", _indent, isDisabled);
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_for, _indent);
		// END KGU#78 2015-12-17
		addCode("", "", isDisabled);
	
	}
	
	protected void generateCode(While _while, String _indent) {
		
		boolean isDisabled = _while.isDisabled();
		
		addCode("", "", isDisabled);
    	insertComment(_while, _indent);
		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		//code.add(_indent+"while ("+BString.replace(transform(_while.getText().getText()),"\n","").trim()+") {");
    	String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
    	if (!this.suppressTransformation || !(condition.startsWith("(") && condition.endsWith(")")))
    	{
    		condition = "( " + condition + " )";
    	}
    	addCode("while " + condition + " {", _indent, isDisabled);
    	// END KGU#162 2016-04-01
		generateCode(_while.q, _indent+this.getIndent());
		addCode("}", _indent, isDisabled);
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_while, _indent);
		// END KGU#78 2015-12-17
		addCode("", "", isDisabled);
		
	}

	
	protected void generateCode(Repeat _repeat, String _indent) {
		
		boolean isDisabled = _repeat.isDisabled();
		
		addCode("", "", isDisabled);

		insertComment(_repeat, _indent);

		addCode("do {", _indent, isDisabled);
		generateCode(_repeat.q,_indent+this.getIndent());
		// START KGU#162 2016-04-01: Enh. #144 new restrictive export mode
		//code.add(_indent+"} while (!("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+")) {");
    	String condition = BString.replace(transform(_repeat.getText().getText()),"\n","").trim();
    	if (!this.suppressTransformation || !(condition.startsWith("(") && condition.endsWith(")")))
    	{
    		condition = "( " + condition + " )";
    	}
    	addCode("} while (!" + condition + ");", _indent, isDisabled);
    	// END KGU#162 2016-04-01
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_repeat, _indent);
		// END KGU#78 2015-12-17
		addCode("", "", isDisabled);
		
	}

	protected void generateCode(Forever _forever, String _indent) {
		
		boolean isDisabled = _forever.isDisabled();
		
		addCode("", "", isDisabled);

		insertComment(_forever, _indent);

		addCode("while (1) {", _indent, isDisabled);		
		generateCode(_forever.q, _indent+this.getIndent());
		addCode("}", _indent, isDisabled);
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_forever, _indent);
		// END KGU#78 2015-12-17
		addCode("", "", isDisabled);
		
	}
	
	protected void generateCode(Call _call, String _indent) {
		if(!insertAsComment(_call, _indent))
		{
			boolean isDisabled = _call.isDisabled();

			insertComment(_call, _indent);

			for (int i=0; i<_call.getText().count(); i++)
			{
				addCode(transform(_call.getText().get(i)) + ";", _indent, isDisabled);
			}
		}
	}
	
	protected void generateCode(Jump _jump, String _indent) {
		if(!insertAsComment(_jump, _indent))
// START KGU#78 2015-12-17: Block braces had been missing! Enh. #23 - jump support
//			insertComment(_jump, _indent);
//			for(int i=0;i<_jump.getText().count();i++)
//			{
//				code.add(_indent+transform(_jump.getText().get(i))+";");
//			}
		{
			boolean isDisabled = _jump.isDisabled();
			// In case of an empty text generate a break instruction by default.
			boolean isEmpty = true;
			
			StringList lines = _jump.getText();
			String preReturn = D7Parser.getKeywordOrDefault("preReturn", "return");
			String preExit   = D7Parser.getKeywordOrDefault("preExit", "exit");
			String preLeave  = D7Parser.getKeywordOrDefault("preLeave", "leave");
			for (int i = 0; isEmpty && i < lines.count(); i++) {
				String line = transform(lines.get(i)).trim();
				if (!line.isEmpty())
				{
					isEmpty = false;
				}
				// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
				//code.add(_indent + line + ";");
				if (line.matches(Matcher.quoteReplacement(preReturn)+"([\\W].*|$)"))
				{
					addCode("return " + line.substring(preReturn.length()).trim() + ";",
							_indent, isDisabled);
				}
				else if (line.matches(Matcher.quoteReplacement(preExit)+"([\\W].*|$)"))
				{
					addCode("exit(" + line.substring(preExit.length()).trim() + ");",
							_indent, isDisabled);
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
					addCode("goto " + label + ";", _indent, isDisabled);
				}
				else if (line.matches(Matcher.quoteReplacement(preLeave)+"([\\W].*|$)"))
				{
					// Strange case: neither matched nor rejected - how can this happen?
					// Try with an ordinary break instruction and a funny comment
					addCode("last;\t" + this.commentSymbolLeft() + " FIXME: Dubious occurrance of 'last' instruction!",
							_indent, isDisabled);
				}
				else if (!isEmpty)
				{
					insertComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
					insertComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
			if (isEmpty) {
				addCode("last;", _indent, isDisabled);
			}
			
		}
// END KGU#78 2015-12-17
			
	}

	// START KGU#47 2015-12-17: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{
		boolean isDisabled = _para.isDisabled();
		
		// START KGU 2014-11-16
		insertComment(_para, _indent);
		// END KGU 2014-11-16

		addCode("", "", isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
		addCode("{", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			insertComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			addCode("{", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("}", _indent + this.getIndent(), isDisabled);
			insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			addCode("", "", isDisabled);
		}

		addCode("}", _indent, isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}
	// END KGU#47 2015-12-17
	
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		String indent = _indent;
		// START KGU#178 2016-07-20: Enh. #160 - don't add this if it's not at top level
		//code.add(_indent + "#!/usr/bin/perl");
		//insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
		//insertComment("", _indent);
		if (topLevel)
		{
			code.add(_indent + "#!/usr/bin/perl");
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			insertComment("", _indent);
			subroutineInsertionLine = code.count();
		}
		else
		{
			code.add("");
		}
		// END KGU#178 2016-07-20
		insertComment(_root, _indent);
		if( ! _root.isProgram ) {
			code.add(_indent + "sub " + _procName + " {");
			indent = _indent + this.getIndent();
			for (int p = 0; p < _paramNames.count(); p++) {
				code.add(indent + "my $" + _paramNames.get(p).trim() + " = $_[" + p + "];");
			}
		} else {
			code.add("");
			code.add(_indent + "use strict;");
			code.add(_indent + "use warnings;");
		}
	
		code.add("");
		
		return indent;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		// Ensure all variables be private
		if (!_root.isProgram) {
			for (int v = 0; v < _varNames.count(); v++) {
				code.add(_indent + "my $" + _varNames.get(v) + ";");	// FIXME (KGU) What about lists?
			}
		}
		code.add(_indent);
		// START KGU 2015-11-02: Now fetch all variable names from the entire diagram
		varNames = _root.getVarNames(); // We need more variables than just the ones retrieved by super.
		// END KGU 2015-11-02
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
			code.add(_indent);
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateFooter(lu.fisch.structorizer.elements.Root, java.lang.String)
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		if (!_root.isProgram) code.add(_indent + "}");		
	}
	// END KGU#78 2015-12-17
	
}

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

/******************************************************************************************************
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
 *
 ******************************************************************************************************
 *
 *      Comment:		LGPL license (http://www.gnu.org/licenses/lgpl.html).
 *
 ******************************************************************************************************///


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
	
	// START KGU 2015-11-02: We must know alle variable names in order to prefix the with '$'.
	StringList varNames = new StringList();
	// END KGU 2015-11-02

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
	protected boolean supportsSimpleBreak()
	{
		return true;
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
		// START KGU#108 2015-12-22: Bugfix #51
		//return "$1 = <STDIN>";
		return "$1 = <STDIN>; chomp $1";
		// END KGU#108 2015-12-22
	}

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
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21
	
	// END KGU#18/KGU#23 2015-11-01
	
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

//	// KGU 2015-11-02 Most of this now obsolete (delegated to Generator, Element), something was wrong anyway
//    protected String transform(String _input)
//	{
//    	_input = super.transform(_input);
//    	
//    	// START KGU#62 2015-11-02: Identify and adapt variable names
//		//System.out.println("Perl - text to be transformed: \"" + _input + "\"");
//		// START KGU#103 2015-12-12: We must do a lexical analysis instead
////    	for (int i = 0; i < varNames.count(); i++)
////    	{
////    		String varName = varNames.get(i);	// FIXME (KGU): Remove after Test!
////    		System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
////    		//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
////    		int pos = _input.indexOf(varName);
////    		while (pos >= 0)
////    		{
////    			int posBehind = pos + varName.length();
////    			// START KGU#103 2015-12-12: Bugfix #57 Endless loop possible
////    			//if ((pos == 0 || !Character.isJavaIdentifierPart(_input.charAt(pos-1))) && (posBehind >= varName.length() || !Character.isJavaIdentifierPart(_input.charAt(posBehind))))
////    	  		if ((pos == 0 || !Character.isJavaIdentifierPart(_input.charAt(pos-1)) && _input.charAt(pos-1) != '\\') &&
////    	  				(posBehind >= _input.length() || !Character.isJavaIdentifierPart(_input.charAt(posBehind))))
////    	  		// END KGU#103 2015-12-12
////    			{
////    				if (pos == 0 || _input.charAt(pos-1) != '$')
////    				{
////    					_input = _input.substring(0, pos) + "$" + _input.substring(pos);
////            	  		// START KGU#103 2015-12-12: Bugfix #57 Endless loop possible
////        				posBehind++;
////        				// END KGU#103 2015-12-12
////    				}
////        	  		// START KGU#103 2015-12-12: Bugfix #57 Endless loop possible
////    				//pos = _input.indexOf(varName, posBehind);
////    				// END KGU#103 2015-12-12
////    			}
////    	  		// START KGU#103 2015-12-12: Bugfix #57 Endless loop possible
////    	  		if (posBehind < _input.length() - varName.length())
////    	  		{
////    	  			pos = _input.indexOf(varName, posBehind);
////    	  		}
////    	  		else
////    	  		{
////    	  			pos = -1;
////    	  		}
////				// END KGU#103 2015-12-12
////    		}
////    		System.out.println("Perl - after replacement: \"" + _input + "\""); 	// FIXME (KGU): Remove after Test!
////    	}
//		StringList tokens = Element.splitLexically(_input, true);
//    	for (int i = 0; i < varNames.count(); i++)
//    	{
//    		String varName = varNames.get(i);
//    		//System.out.println("Looking for " + varName + "...");	// FIXME (KGU): Remove after Test!
//    		//_input = _input.replaceAll("(.*?[^\\$])" + varName + "([\\W$].*?)", "$1" + "\\$" + varName + "$2");
//    		tokens.replaceAll(varName, "$"+varName);
//    	}
//    	_input = tokens.getText().replace("\n", "");
//		//System.out.println("Perl - after replacement: \"" + _input + "\""); 	// FIXME (KGU): Remove after Test!
//		// END KGU#103 2015-12-12
//    	// END KGU#62 2015-11-02
//
//		return _input.trim();
//	}
	// END KGU#93 2015-12-21
	
    // START KGU#78 2015-12-17: Enh. #23 (jump translation)
    // Places a label with empty instruction into the code if elem is an exited loop
	protected void appendLabel(Element elem, String _indent)
	{
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem)) {
			code.add(_indent + this.labelBaseName + this.jumpTable.get(elem) + ": ;");
		}
	}
	// ED KGU#78 2015-12-17

	protected void generateCode(Instruction _inst, String _indent) {

		if(!insertAsComment(_inst, _indent))
		{
	    	insertComment(_inst, _indent);

			for(int i=0;i<_inst.getText().count();i++)
			{

				code.add(_indent + transform(_inst.getText().get(i))+";");

			}
		}

	}
	
	protected void generateCode(Alternative _alt, String _indent) {
		
		code.add("");

		insertComment(_alt, _indent);

		code.add(_indent+"if ( "+BString.replace(transform(_alt.getText().getText()),"\n","").trim()+" ) {");
		generateCode(_alt.qTrue,_indent+this.getIndent());
		
		if(_alt.qFalse.getSize()!=0) {
			
			code.add(_indent+"}");
			code.add(_indent+"else {");			
			generateCode(_alt.qFalse,_indent+this.getIndent());
			
		}
		
		code.add(_indent+"}");
		code.add("");
		
	}
	
	protected void generateCode(Case _case, String _indent) {
		
		code.add("");

		insertComment(_case, _indent);

		// Since Perl release 5.8.0, switch is a standard module...
		code.add(_indent+"switch ( "+transform(_case.getText().get(0))+" ) {");
		
		for(int i=0;i<_case.qs.size()-1;i++)
		{
			code.add("");
			// START KGU#15 2015-11-02: Support multiple constants per branch
			//code.add(_indent+this.getIndent()+"case ("+_case.getText().get(i+1).trim()+") {");
			String conds = _case.getText().get(i+1).trim();
			if (conds.indexOf(',') >= 0)	// Is it an enumeration of values? 
			{
				conds = "[" + conds + "]";
			}
			else
			{
				conds = "(" + conds + ")";
			}
			code.add(_indent + this.getIndent() + "case " + conds +" {");
			// END KGU#15 2015-11-02
			//code.add(_indent+_indent.substring(0,1)+_indent.substring(0,1)+"begin");
			generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent() + this.getIndent());
			code.add(_indent + this.getIndent()+"}");
		}
		
		if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
		{

			code.add("");
			code.add(_indent + this.getIndent() + "else {");
			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1), _indent + this.getIndent() + this.getIndent());
			code.add(_indent + this.getIndent()+"}");
		}
		code.add(_indent+"}");
		code.add("");
		
	}
	
	
	protected void generateCode(For _for, String _indent) {
		
		code.add("");
		
		// STRT KGU#3 2015-11-02: Competent splitting of For clause
//		String str = _for.getText().getText();
//		// cut of the start of the expression
//		if(!D7Parser.preFor.equals("")){str=BString.replace(str,D7Parser.preFor,"");}
//		// trim blanks
//		str=str.trim();
//		// modify the later word
//		if(!D7Parser.postFor.equals("")){str=BString.replace(str,D7Parser.postFor,"<=");}
//		// do other transformations 
//		str=transform(str);
//		String counter = str.substring(0,str.indexOf("="));
//		// insert the middle
//		str=BString.replace(str,"<=",";"+counter+"<=");
//		// complete
//		str="for("+str+";"+counter+"++)";
//		code.add(_indent+str+" {");
//		//code.add(_indent+"for ("+BString.replace(transform(_for.getText().getText()),"\n","").trim()+") {");

    	insertComment(_for, _indent);

    	String var = _for.getCounterVar();
    	int step = _for.getStepConst();
    	String compOp = (step > 0) ? " <= " : " >= ";
    	String increment = "$" + var + " += (" + step + ")";
    	code.add(_indent + "for ($" +
    			var + " = " + transform(_for.getStartValue(), false) + "; $" +
    			var + compOp + transform(_for.getEndValue(), false) + "; " +
    			increment +
    			") {");
		// END KGU#3 2015-11-02
		generateCode(_for.q,_indent+this.getIndent());
		code.add(_indent+"}");
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_for, _indent);
		// END KGU#78 2015-12-17
		code.add("");
	
	}
	
	protected void generateCode(While _while, String _indent) {
		
		code.add("");
    	insertComment(_while, _indent);
		code.add(_indent+"while ("+BString.replace(transform(_while.getText().getText()),"\n","").trim()+") {");		
		generateCode(_while.q,_indent+this.getIndent());
		code.add(_indent+"}");
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_while, _indent);
		// END KGU#78 2015-12-17
		code.add("");
		
	}

	
	protected void generateCode(Repeat _repeat, String _indent) {
		
		code.add("");

		insertComment(_repeat, _indent);

		code.add(_indent+"do {");
		generateCode(_repeat.q,_indent+this.getIndent());
		code.add(_indent+"} while (!("+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+"));");
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_repeat, _indent);
		// END KGU#78 2015-12-17
		code.add("");
		
	}

	protected void generateCode(Forever _forever, String _indent) {
		
		code.add("");

		insertComment(_forever, _indent);

		code.add(_indent+"while (1) {");		
		generateCode(_forever.q,_indent+this.getIndent());
		code.add(_indent+"}");
		// START KGU#78 2015-12-17: Enh. #23 Put a trailing label if this is a jump target
		appendLabel(_forever, _indent);
		// END KGU#78 2015-12-17
		code.add("");
		
	}
	
	protected void generateCode(Call _call, String _indent) {
		if(!insertAsComment(_call, _indent))

			insertComment(_call, _indent);

			for(int i=0;i<_call.getText().count();i++)
			{
				code.add(_indent+transform(_call.getText().get(i))+";");
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
			// In case of an empty text generate a break instruction by default.
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
					code.add(_indent + "return " + line.substring(D7Parser.preReturn.length()).trim() + ";");
				}
				else if (line.matches(Matcher.quoteReplacement(D7Parser.preExit)+"([\\W].*|$)"))
				{
					code.add(_indent + "exit(" + line.substring(D7Parser.preExit.length()).trim() + ");");
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
					code.add(_indent + "goto " + label + ";");
				}
				else if (line.matches(Matcher.quoteReplacement(D7Parser.preLeave)+"([\\W].*|$)"))
				{
					// Strange case: neither matched nor rejected - how can this happen?
					// Try with an ordinary break instruction and a funny comment
					code.add(_indent + "last;\t" + this.commentSymbolLeft() + " FIXME: Dubious occurrance of 'last' instruction!");
				}
				else if (!isEmpty)
				{
					insertComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
					insertComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
			if (isEmpty) {
				code.add(_indent + "last;");
			}
			
		}
// END KGU#78 2015-12-17
			
	}

	// START KGU#47 2015-12-17: Offer at least a sequential execution (which is one legal execution order)
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
		code.add(_indent + "{");

		for (int i = 0; i < _para.qs.size(); i++) {
			code.add("");
			insertComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			code.add(_indent + this.getIndent() + "{");
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			code.add(_indent + this.getIndent() + "}");
			insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			code.add("");
		}

		code.add(_indent + "}");
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		code.add("");
	}
	// END KGU#47 2015-12-17
	
	
	// START KGU 2015-11-02: Already inherited
//	protected void generateCode(Subqueue _subqueue, String _indent) {
//		
//		// code.add(_indent+"");
//		for(int i=0;i<_subqueue.getSize();i++)
//		{
//			generateCode((Element) _subqueue.getElement(i),_indent);
//		}
//		// code.add(_indent+"");
//		
//	}
	// END KGU 2015-11-02
	
// START KGU#78 2015-12-17: Enh. #23 Root generation decomposed
//	public String generateCode(Root _root, String _indent) {
//		
//		// START KGU 2015-11-02: First of all, fetch all variable names from the entire diagram
//		varNames = _root.getVarNames();
//		// END KGU 2015-11-02
//		
//		if( ! _root.isProgram ) {
//			code.add("sub "+_root.getText().get(0)+" {");
//		} else {
//			
//			code.add("#!/usr/bin/perl");
//			code.add("");
//			code.add("use strict;");
//			code.add("use warnings;");
//			
//		}
//		
//		insertComment("generated by Structorizer", _indent);
//		code.add("");
//		insertComment("TODO declare your variables here", _indent);
//		code.add("");
//		generateCode(_root.children, _indent + this.getIndent());
//		
//		if( ! _root.isProgram ) {
//			code.add("}");
//		}
//		
//		return code.getText();
//		
//	}
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		String indent = _indent;
		code.add(_indent + "#!/usr/bin/perl");
		insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
		insertComment("", _indent);
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

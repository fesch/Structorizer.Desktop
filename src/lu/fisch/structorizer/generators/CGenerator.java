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
 *      Kay Gürtzig             2015.11.10		Bugfixes KGU#71 (switch default), KGU#72 (div operators)
 *      Kay Gürtzig             2015.11.10      Code style option optionBlockBraceNextLine() added,
 *                                              bugfix/enhancement #22 (KGU#74 jump and return handling)
 *      Kay Gürtzig             2015.12.13		Bugfix #51 (=KGU#108): Cope with empty input and output
 *      Kay Gürtzig             2015.12.21		Adaptations for Bugfix #41/#68/#69 (=KGU#93)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-29 - enhancement #23: Sensible handling of Jump elements (break / return / exit)
 *      - return instructions and assignments to variables named "result" or like the function
 *        are registered, such that return instructions may be generated on demand
 *      - "leave" jumps will generate break or goto instructions
 *      - exit instructions are produced as well.
 *      - new methods insertBlockHeading() and insertBlockTail() facilitate code style variation and
 *        subclassing w.r.t. multi-level jump instructions.
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops themselves now provide more reliable loop parameters  
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
 ******************************************************************************************************/
//

import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;

public class CGenerator extends Generator {

	/************ Fields ***********************/
	@Override
	protected String getDialogTitle() {
		return "Export ANSI C ...";
	}

	@Override
	protected String getFileDescription() {
		return "ANSI C Source Code";
	}

	@Override
	protected String getIndent() {
		return "\t";
	}

	@Override
	protected String[] getFileExtensions() {
		String[] exts = { "c" };
		return exts;
	}

	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft() {
		// In ANSI C99, line comments are already allowed
		return "//";
	}

	// END KGU 2015-10-18

// START KGU#16 2015-12-18: Moved to Generator.java	and made an ExportOptionDialoge option
//	// START KGU#16 2015-11-29: Code style option for opening brace placement
//	protected boolean optionBlockBraceNextLine() {
//		// (KGU 2015-11-29): Should become an ExportOptionDialoge option
//		return true;
//	}
//	// END KGU#16 2015-11-29
// END KGU#16 2015-12-18
	
	// START KGU#16/KGU#74 2015-11-30: Unification of block generation (configurable)
	/**
	 * This subclassable method is used for insertBlockHeading()
	 * @return Indicates where labels for multi-level loop exit jumps are to be placed
	 * (in C, C++, C# after the loop, in Java at the beginning of the loop). 
	 */
	protected boolean isLabelAtLoopStart()
	{
		return false;
	}
	
	/**
	 * Instruction to be used to leave an outer loop (subclassable)
	 * A label string is supposed to be appended without parentheses.
	 * @return a string containing the respective reserved word
	 */
	protected String getMultiLevelLeaveInstr()
	{
		return "goto";
	}
	
	// See also insertExitInstr(int, String)
	// END KGU#16/KGU#74 2015-11-30

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
	 * A pattern how to embed the variable (right-hand side of an input
	 * instruction) into the target code
	 * 
	 * @return a regex replacement pattern, e.g.
	 *         "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected String getInputReplacer() {
		return "scanf(\"\", &$1)";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output
	 * instruction) into the target code
	 * 
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer() {
		return "printf(\"\", $1); printf(\"\\\\n\")";
	}

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	protected void insertExitInstr(String _exitCode, String _indent)
	{
		code.add(_indent + "exit(" + _exitCode + ")");
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by "="
//	 * 
//	 * @param _interm
//	 *            - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm) {
//		return _interm.replace(" <- ", " = ");
//	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		return tokens.concatenate();
	}
	// END KGU#93 2015-12-21

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
		//_input = _input.replace(" div ", " / ");
		// END KGU#72 2015-11-10
		// START KGU#108 2015-12-13: Bugfix #51: Cope with empty input and output
		_input = _input.replace("scanf(\"\", &)", "getchar()");
		_input = _input.replace("printf(\"\", ); ", "");
		// END KGU#108 2015-12-13
		
		return _input.trim();
	}

	// START KGU#16 2015-11-29
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		_type = _type.toLowerCase();
		_type = _type.replace("integer", "int");
		_type = _type.replace("real", "double");
		_type = _type.replace("boolean", "int");
		_type = _type.replace("boole", "int");
		_type = _type.replace("character", "char");
		return _type;
	}
	// END KGU#1 2015-11-29

	
	protected void insertBlockHeading(Element elem, String _headingText, String _indent)
	{
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem) && this.isLabelAtLoopStart())  
		{
				_headingText = this.labelBaseName + this.jumpTable.get(elem) + ": " + _headingText;
		}
		if (!this.optionBlockBraceNextLine())
		{
			code.add(_indent + _headingText + " {");
		}
		else
		{
			code.add(_indent + _headingText);
			code.add(_indent + "{");
		}
	}

	protected void insertBlockTail(Element elem, String _tailText, String _indent)
	{
		if (_tailText == null) {
			code.add(_indent + "}");
		}
		else {
			code.add(_indent + "} " + _tailText + ";");
		}
		
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem) && !this.isLabelAtLoopStart()) {
			code.add(_indent + this.labelBaseName + this.jumpTable.get(elem) + ": ;");
		}
	}
	// END KGU#74 2015-11-30

	@Override
	protected void generateCode(Instruction _inst, String _indent) {
		// START KGU#18/KGU#23 2015-10-18: The "export instructions as comments"
		// configuration had been ignored here
		// insertComment(_inst, _indent);
		// for(int i=0;i<_inst.getText().count();i++)
		// {
		// code.add(_indent+transform(_inst.getText().get(i))+";");
		// }
		if (!insertAsComment(_inst, _indent)) {

			insertComment(_inst, _indent);

			StringList lines = _inst.getText();
			for (int i = 0; i < lines.count(); i++) {
				code.add(_indent + transform(lines.get(i)) + ";");
			}

		}
		// END KGU 2015-10-18
	}

	@Override
	protected void generateCode(Alternative _alt, String _indent) {
		// START KGU 2014-11-16
		insertComment(_alt, _indent);
		// END KGU 2014-11-16

		String condition = transform(_alt.getText().getLongString(), false)
				.trim();
		if (!condition.startsWith("(") || !condition.endsWith(")"))
			condition = "(" + condition + ")";
		
		insertBlockHeading(_alt, "if " + condition, _indent);
		generateCode(_alt.qTrue, _indent + this.getIndent());
		insertBlockTail(_alt, null, _indent);

		if (_alt.qFalse.getSize() != 0) {
			insertBlockHeading(_alt, "else", _indent);
			generateCode(_alt.qFalse, _indent + this.getIndent());
			insertBlockTail(_alt, null, _indent);
		}
	}

	@Override
	protected void generateCode(Case _case, String _indent) {
		// START KGU 2014-11-16
		insertComment(_case, _indent);
		// END KGU 2014-11-16

		StringList lines = _case.getText();
		String condition = transform(lines.get(0), false);
		if (!condition.startsWith("(") || !condition.endsWith(")")) {
			condition = "(" + condition + ")";
		}

		insertBlockHeading(_case, "switch " + condition, _indent);

		for (int i = 0; i < _case.qs.size() - 1; i++) {
			// START KGU#15 2015-10-21: Support for multiple constants per
			// branch
			StringList constants = StringList.explode(lines.get(i + 1), ",");
			for (int j = 0; j < constants.count(); j++) {
				code.add(_indent + "case " + constants.get(j).trim() + ":");
			}
			// END KGU#15 2015-10-21
			generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent());
			code.add(_indent + this.getIndent() + "break;");
		}

		if (!lines.get(_case.qs.size()).trim().equals("%")) {
			code.add(_indent + "default:");
			Subqueue squeue = (Subqueue) _case.qs.get(_case.qs.size() - 1);
			generateCode(squeue, _indent + this.getIndent());
			// START KGU#71 2015-11-10: For an empty default branch, at least a
			// semicolon is required
			if (squeue.getSize() == 0) {
				code.add(_indent + this.getIndent() + ";");
			}
			// END KGU#71 2015-11-10
		}
		
		insertBlockTail(_case, null, _indent);
	}

	// END KGU#18/#23 2015-10-20

	@Override
	protected void generateCode(For _for, String _indent) {
		insertComment(_for, _indent);
		
		String var = _for.getCounterVar();
		int step = _for.getStepConst();
		String compOp = (step > 0) ? " <= " : " >= ";
		String increment = var + " += (" + step + ")";
		insertBlockHeading(_for, "for (" + var + " = "
				+ transform(_for.getStartValue(), false) + "; " + var + compOp
				+ transform(_for.getEndValue(), false) + "; " + increment + ")",
				_indent);

		generateCode(_for.q, _indent + this.getIndent());

		insertBlockTail(_for, null, _indent);
	}

	@Override
	protected void generateCode(While _while, String _indent) {
		// START KGU 2014-11-16
		insertComment(_while, _indent);
		// END KGU 2014-11-16

		String condition = transform(_while.getText().getLongString(), false)
				.trim();
		if (!condition.startsWith("(") || !condition.endsWith(")")) {
			condition = "(" + condition + ")";
		}

		insertBlockHeading(_while, "while " + condition, _indent);

		generateCode(_while.q, _indent + this.getIndent());

		insertBlockTail(_while, null, _indent);

	}

	@Override
	protected void generateCode(Repeat _repeat, String _indent) {
		// START KGU 2014-11-16
		insertComment(_repeat, _indent);
		// END KGU 2014-11-16

		insertBlockHeading(_repeat, "do", _indent);

		generateCode(_repeat.q, _indent + this.getIndent());

		insertBlockTail(_repeat, "while (!(" 
				+ transform(_repeat.getText().getLongString()).trim() + "))", _indent);
	}

	@Override
	protected void generateCode(Forever _forever, String _indent) {
		// START KGU 2014-11-16
		insertComment(_forever, _indent);
		// END KGU 2014-11-16

		insertBlockHeading(_forever, "while (true)", _indent);

		generateCode(_forever.q, _indent + this.getIndent());

		insertBlockTail(_forever, null, _indent);
	}

	@Override
	protected void generateCode(Call _call, String _indent) {
		// START KGU 2015-10-18: The "export instructions as comments"
		// configuration had been ignored here
		// insertComment(_call, _indent);
		// for(int i=0;i<_call.getText().count();i++)
		// {
		// code.add(_indent+transform(_call.getText().get(i))+";");
		// }
		if (!insertAsComment(_call, _indent)) {

			insertComment(_call, _indent);

			StringList lines = _call.getText();
			for (int i = 0; i < lines.count(); i++) {
				// Input or Output should not occur here
				code.add(_indent + transform(lines.get(i), false) + ";");
			}
		}
		// END KGU 2015-10-18
	}

	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		// START KGU 2015-10-18: The "export instructions as comments"
		// configuration had been ignored here
		// insertComment(_jump, _indent);
		// for(int i=0;i<_jump.getText().count();i++)
		// {
		// code.add(_indent+transform(_jump.getText().get(i))+";");
		// }
		if (!insertAsComment(_jump, _indent)) {

			insertComment(_jump, _indent);

			// KGU 2015-10-18: In case of an empty text generate a break
			// instruction by default.
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
					insertExitInstr(line.substring(D7Parser.preExit.length()).trim(), _indent);
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
					code.add(_indent + this.getMultiLevelLeaveInstr() + " " + label + ";");
				}
				else if (line.matches(Matcher.quoteReplacement(D7Parser.preLeave)+"([\\W].*|$)"))
				{
					// Strange case: neither matched nor rejected - how can this happen?
					// Try with an ordinary break instruction and a funny comment
					code.add(_indent + "break;\t// FIXME: Dubious occurrance of break instruction!");
				}
				else if (!isEmpty)
				{
					insertComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
					insertComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
			if (isEmpty) {
				code.add(_indent + "break;");
			}
		}
		// END KGU 2015-10-18
	}

	// START KGU#47 2015-11-29: Offer at least a sequential execution (which is one legal execution order)
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
	// END KGU#47 2015-11-30
	

//	@Override
//	public String generateCode(Root _root, String _indent) {
//		// START KGU#74 2015-11-30: Prepare the label associations
//		this.alwaysReturns = this.mapJumps(_root.children);
//		// END KGU#74 2015-11-30
//
//		String fnName = _root.getMethodName();
//		String pr = (_root.isProgram) ? "program" : "function";
//		// START KGU 2015-11-29: More informed generation attempts
//		// Get all local variable names
//		StringList varNames = _root.getVarNames(_root, false, true);
//		// code.add(pr+" "+_root.getText().get(0)+";");
//		insertComment(pr + " " + _root.getText().get(0), "");
//		code.add("#include <stdio.h>");
//		code.add("");
//		// START KGU 2014-11-16
//		insertComment(_root, "");
//		// END KGU 2014-11-16
//
//		// START Kay Gürtzig 2010-09-10
//		// code.add("int main(void)");
//		if (_root.isProgram)
//			code.add("int main(void)");
//		else {
//			// START KGU 2015-11-29: We may get more informed information
//			// String fnHeader = _root.getText().get(0).trim();
//			// if(fnHeader.indexOf('(')==-1 || !fnHeader.endsWith(")"))
//			// fnHeader=fnHeader+"(void)";
//			this.isResultSet = varNames.contains("result", false);
//			this.isFunctionNameSet = varNames.contains(fnName);
//			String fnHeader = transformType(_root.getResultType(),
//					((returns || isResultSet || isFunctionNameSet) ? "int" : "void"));
//			fnHeader += " " + fnName + "(";
//			StringList paramNames = new StringList();
//			StringList paramTypes = new StringList();
//			_root.collectParameters(paramNames, paramTypes);
//			for (int p = 0; p < paramNames.count(); p++) {
//				if (p > 0)
//					fnHeader += ", ";
//				fnHeader += (transformType(paramTypes.get(p), "/*type?*/") + " " + paramNames
//						.get(p)).trim();
//			}
//			fnHeader += ")";
//			// END KGU 2015-11-29
//
//			// START KGU 2015-10-18: Hint to accomplish the function signature
//			insertComment(
//					"TODO Revise the return type and declare the parameters.",
//					"");
//			// END KGU 2015-10-18
//			code.add(fnHeader);
//		}
//		// END Kay Gürtzig 2010-09-10
//		code.add("{");
//		insertComment("TODO declare your variables here:", this.getIndent());
//        // START KGU 2015-11-30: List the variables to be declared
//		for (int v = 0; v < varNames.count(); v++) {
//			insertComment(varNames.get(v), this.getIndent());
//		}
//		// END KGU 2015-11-30
//		code.add(this.getIndent());
//		insertComment("TODO", this.getIndent());
//		insertComment(
//				"For any input using the 'scanf' function you need to fill the first argument.",
//				this.getIndent());
//		insertComment(
//				"http://en.wikipedia.org/wiki/Scanf#Format_string_specifications",
//				this.getIndent());
//		code.add(this.getIndent());
//		insertComment("TODO", this.getIndent());
//		insertComment(
//				"For any output using the 'printf' function you need to fill the first argument:",
//				this.getIndent());
//		insertComment(
//				"http://en.wikipedia.org/wiki/Printf#printf_format_placeholders",
//				this.getIndent());
//		code.add(this.getIndent());
//
//		code.add(this.getIndent());
//		generateCode(_root.children, this.getIndent());
//		// Kay Gürtzig 2010.09.10: A function will already have got a return
//		// statement (if it needs one)
//		if (_root.isProgram)
//		{
//			code.add(this.getIndent());
//			code.add(this.getIndent() + "return 0;");
//		}
//		else if ((returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
//		{
//			String result = "0";
//			if (isFunctionNameSet)
//			{
//				result = _root.getMethodName();
//			}
//			else if (isResultSet)
//			{
//				int vx = varNames.indexOf("result", false);
//				result = varNames.get(vx);
//			}
//			code.add(this.getIndent());
//			code.add(this.getIndent() + "return " + result + ";");
//		}
//		code.add("}");
//
//		return code.getText();
//	}

	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		String pr = (_root.isProgram) ? "program" : "function";
		insertComment(pr + " " + _root.getText().get(0), _indent);
		insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
		code.add("");
		code.add("#include <stdio.h>");

		code.add("");		
		insertComment(_root, _indent);
		
		if (_root.isProgram)
			code.add("int main(void)");
		else {
			// Compose the function header
			String fnHeader = transformType(_root.getResultType(),
					((this.returns || this.isResultSet || this.isFunctionNameSet) ? "int" : "void"));
			fnHeader += " " + _procName + "(";
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0)
					fnHeader += ", ";
				fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
						_paramNames.get(p)).trim();
			}
			fnHeader += ")";
			insertComment("TODO: Revise the return type and declare the parameters.", _indent);
			code.add(fnHeader);
		}
		code.add(_indent + "{");
		
		return _indent + this.getIndent();
	}

	/**
	 * Generates some preamble (i.e. comments, language declaration section etc.)
	 * and adds it to this.code.
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param varNames - list of variable names introduced inside the body
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		insertComment("TODO: declare your variables here:", _indent);
        // START KGU 2015-11-30: List the variables to be declared
		for (int v = 0; v < varNames.count(); v++) {
			insertComment(varNames.get(v), _indent);
		}
		// END KGU 2015-11-30
		code.add(_indent);
		insertComment("TODO:", _indent);
		insertComment(
				"For any input using the 'scanf' function you need to fill the first argument.",
				_indent);
		insertComment(
				"http://en.wikipedia.org/wiki/Scanf#Format_string_specifications",
				_indent);
		code.add(_indent);
		insertComment("TODO:", _indent);
		insertComment(
				"For any output using the 'printf' function you need to fill the first argument:",
				_indent);
		insertComment(
				"http://en.wikipedia.org/wiki/Printf#printf_format_placeholders",
				_indent);
		code.add(_indent);
		return _indent;
	}
	
	/**
	 * Creates the appropriate code for returning a required result and adds it
	 * (after the algorithm code of the body) to this.code)
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param alwaysReturns - whether all paths of the body already force a return
	 * @param varNames - names of all assigned variables
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if (_root.isProgram && !alwaysReturns)
		{
			code.add(_indent);
			code.add(_indent + "return 0;");
		}
		else if ((returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
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
	
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root 
	 * @param _indent
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		code.add(_indent + "}");		
	}

	
}

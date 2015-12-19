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
 *      Description:    This class generates Java code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                     Date            Description
 *      ------                     ----            -----------
 *      Bob Fisch                  2008.11.17      First Issue
 *      Gunter Schillebeeckx       2009.08.10      Java Generator starting from C Generator
 *      Bob Fisch                  2009.08.10      Update I/O
 *      Bob Fisch                  2009.08.17      Bugfixes (see comment)
 *      Kay Gürtzig                2010.09.10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch                  2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig                2014.10.22      Workarounds and Enhancements (see comment)
 *      Kay Gürtzig                2014.11.16      Several fixes and enhancements (see comment)
 *      Kay Gürtzig                2015.10.18      Comment generation and indentation revised
 *      Kay Gürtzig                2015.11.01      Preprocessing reorganised, FOR loop and CASE enhancements
 *      Kay Gürtzig                2015.11.30      Inheritance changed to CGenerator (KGU#16), specific
 *                                                 jump and return handling added (issue #22 = KGU#74)
 *      Kay Gürtzig                2015.12.12      Enh. #54 (KGU#101): Support for output expression lists
 *      Kay Gürtzig                2015.12.15      Bugfix #51 (=KGU#108): Cope with empty input and output
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
 *      2015-10-18 - Bugfix / Code revision
 *      - Indentation increment with +_indent.substring(0,1) worked only for single-character indentation units
 *      - Interface of comment insertion methods modified
 *      
 *      2014.11.16 - Bugfixes
 *      - conversion of comparison and logical operators had still been flawed
 *      - comment generation unified by new inherited generic method insertComment 
 *      
 *      2014.10.22 - Bugfixes / Enhancement
 *      - Replacement for asin(), acos(), atan() hadn't worked.
 *      - Support for logical operators "and", "or", and "not"
 *      
 *      2010.09.10 - Bugfixes
 *      - condition for automatic bracket addition around condition expressions corrected
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator convertion
 *      - pascal function convertion
 *
 *      2009.08.10
 *        - writeln() => System.out.println()
 * 
 ******************************************************************************************************///

import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


// START KGU#16 2015-11-30: Strong similarities made it sensible to reduce this class to the differences
//public class JavaGenerator extends Generator
public class JavaGenerator extends CGenerator
// END KGU#16 2015-11-30
{

	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export Java ...";
	}

	protected String getFileDescription()
	{
		return "Java Source Code";
	}

	protected String getIndent()
	{
		return "\t";
	}

	protected String[] getFileExtensions()
	{
		String[] exts = {"java"};
		return exts;
	}

	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft()
	{
		return "//";
	}
	// END KGU 2015-10-18

// START KGU#16 2015-12-18: Now inherited and depending on export option	
//	// START KGU#16 2015-11-29: Code style option for opening brace placement
//	protected boolean optionBlockBraceNextLine() {
//		return false;
//	}
//	// END KGU#16 2015-11-29
// END KGU#16 2015-12-18

	// START KGU#16/KGU#47 2015-11-30: Unification of block generation (configurable)
	/**
	 * This subclassable method is used for insertBlockHeading()
	 * @return Indicates where labels for multi-level loop exit jumps are to be placed
	 * (in C, C++, C# after the loop, in Java at the beginning of the loop). 
	 */
	@Override
	protected boolean isLabelAtLoopStart()
	{
		return true;
	}

	/**
	 * Instruction to be used to leave an outer loop (subclassable)
	 * A label string is supposed to be appended without parentheses.
	 * @return a string containing the respective reserved word
	 */
	@Override
	protected String getMultiLevelLeaveInstr()
	{
		return "break";
	}

	// END KGU#16/#47 2015-11-30

	/************ Code Generation **************/

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
	protected String getInputReplacer()
	{
		return "$1 = (new Scanner(System.in)).nextLine()";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "System.out.println($1)";
	}

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	protected void insertExitInstr(String _exitCode, String _indent)
	{
		code.add(_indent + "System.exit(" + _exitCode + ")");
	}
	// END KGU#16/#47 2015-11-30

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

	@Override
	protected String transform(String _input)
	{
		// START KGU#101 2015-12-12: Enh. #54 - support lists of expressions
		if (_input.matches("^" + D7Parser.output.trim() + "[ ](.*?)"))
		{
			StringList expressions = 
					Element.splitExpressionList(_input.substring(D7Parser.output.trim().length()), ",");
			// Some of the expressions might be sums, so better put parentheses around them
			_input = D7Parser.output.trim() + " (" + expressions.getText().replace("\n", ") + (") + ")";
		}
		// END KGU#101 2015-12-12

		// START KGU#18/KGU#23 2015-11-01: This can now be inherited
		String s = super.transform(_input).replace(" div "," / ");
		// END KGU#18/KGU#23 2015-11-01

		// START KGU#108 2015-12-15: Bugfix #51: Cope with empty input and output
		if (s.startsWith("= (new Scanner(System.in)).nextLine()")) s = s.substring(2);
		// END KGU#108 2015-12-15


		// Math function
		s=s.replace("cos(", "Math.cos(");
		s=s.replace("sin(", "Math.sin(");
		s=s.replace("tan(", "Math.tan(");
		// START KGU 2014-10-22: After the previous replacements the following 3 strings would never be found!
		//s=s.replace("acos(", "Math.acos(");
		//s=s.replace("asin(", "Math.asin(");
		//s=s.replace("atan(", "Math.atan(");
		// This is just a workaround; A clean approach would require a genuine lexical scanning in advance
		s=s.replace("aMath.cos(", "Math.acos(");
		s=s.replace("aMath.sin(", "Math.asin(");
		s=s.replace("aMath.tan(", "Math.atan(");
		// END KGU 2014-10-22:
		s=s.replace("abs(", "Math.abs(");
		s=s.replace("round(", "Math.round(");
		s=s.replace("min(", "Math.min(");
		s=s.replace("max(", "Math.max(");
		s=s.replace("ceil(", "Math.ceil(");
		s=s.replace("floor(", "Math.floor(");
		s=s.replace("exp(", "Math.exp(");
		s=s.replace("log(", "Math.log(");
		s=s.replace("sqrt(", "Math.sqrt(");
		s=s.replace("pow(", "Math.pow(");
		s=s.replace("toRadians(", "Math.toRadians(");
		s=s.replace("toDegrees(", "Math.toDegrees(");
		// clean up ... if needed
		s=s.replace("Math.Math.", "Math.");

		return s.trim();
	}

	// START KGU#16 2015-11-29
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.CGenerator#transformType(java.lang.String, java.lang.String)
	 */
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		_type = _type.toLowerCase();
		_type = _type.replace("short int", "int");
		_type = _type.replace("short", "int");
		_type = _type.replace("unsigned int", "int");
		_type = _type.replace("unsigned long", "long");
		_type = _type.replace("unsigned char", "char");
		_type = _type.replace("unsigned", "int");
		_type = _type.replace("real", "double");
		_type = _type.replace("bool", "boolean");
		_type = _type.replace("boole", "boolean");
		_type = _type.replace("character", "Character");
		_type = _type.replace("integer", "Integer");
		_type = _type.replace("string", "String");
		return _type;
	}
	// END KGU#16 2015-11-29

// KGU#16 2015-11-30: Now inherited from CGenerator
//		protected void generateCode(Instruction _inst, String _indent)
//		{
//			if (!insertAsComment(_inst, _indent)) {
//
//				insertComment(_inst, _indent);
//
//				for (int i=0; i<_inst.getText().count(); i++)
//				{
//					code.add(_indent+transform(_inst.getText().get(i))+";");
//				}
//
//			}
//		}
		
// KGU#16 2015-11-30: Now inherited from CGenerator
//		protected void generateCode(Alternative _alt, String _indent)
//		{
//	        insertComment(_alt, _indent);
//	        
//	        String condition = transform(_alt.getText().getLongString()).trim();
//	        if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
//	        
//	        code.add(_indent+"if " + condition + " {");
//			generateCode(_alt.qTrue, _indent+this.getIndent());
//			if(_alt.qFalse.getSize()!=0)
//			{
//				code.add(_indent + "}");
//				code.add(_indent + "else {");
//				generateCode(_alt.qFalse, _indent+this.getIndent());
//			}
//			code.add(_indent+"}");
//		}
		
// KGU#16 2015-11-30: Now inherited from CGenerator
//		protected void generateCode(Case _case, String _indent)
//		{
//			insertComment(_case, _indent);
//
//			StringList lines = _case.getText();
//			String condition = transform(lines.get(0));
//			if(!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
//
//			code.add(_indent+"switch "+condition+" {");
//			
//			for(int i=0;i<_case.qs.size()-1;i++)
//			{
//				// START KGU#15 2015-10-21: Support for multiple constants per branch
//				StringList constants = StringList.explode(lines.get(i+1), ",");
//				for (int j = 0; j < constants.count(); j++)
//				{
//					code.add(_indent + "case " + constants.get(j).trim() + ":");
//				}
//				// END KGU#15 2015-10-21
//				generateCode((Subqueue) _case.qs.get(i),_indent + this.getIndent());
//				code.add(_indent + this.getIndent() + "break;\n");
//			}
//			
//			if(!lines.get(_case.qs.size()).trim().equals("%"))
//			{
//				code.add(_indent + "default:");
//				generateCode((Subqueue) _case.qs.get(_case.qs.size()-1), _indent + this.getIndent());
//				code.add(_indent + this.getIndent() + "break;");
//			}
//			code.add(_indent + "}");
//		}

// KGU#16 2015-11-30: Now inherited	from CGenerator			
//		protected void generateCode(For _for, String _indent)
//		{
//	        // START KGU 2014-11-16
//	        insertComment(_for, _indent);
//	        // END KGU 2014-11-16
//
//			// START KGU#3 2015-11-01: The For element itself provides us with reliable splitting
//	    	String var = _for.getCounterVar();
//	    	int step = _for.getStepConst();
//	    	String compOp = (step > 0) ? " <= " : " >= ";
//	    	String increment = var + " += (" + step + ")";
//	    	// START KGU#74 2015-11-30: More sophisticated jump handling
//	    	//code.add(_indent + "for (" +
//	    	String label = jumpTable.get(_for);
//			String brace = optionBlockBraceNextLine() ? "" : " {";
//	    	code.add(_indent + ((label == null) ? "" : (label + ": ")) + "for (" +
//	    	// END KGU#74 2015-11-30
//	    			var + " = " + transform(_for.getStartValue(), false) + "; " +
//	    			var + compOp + transform(_for.getEndValue(), false) + "; " +
//	    			increment +
//	    			")" + brace);
//	    	// END KGU#3 2015-11-01
//	    	if (optionBlockBraceNextLine())	code.add(_indent + "{");
//	    	generateCode(_for.q, _indent + this.getIndent());
//	    	code.add(_indent + "}");
//		}
		
// KGU#16 2015-11-30: Now inherited	from CGenerator	
//		protected void generateCode(While _while, String _indent)
//		{
//	        // START KGU 2014-11-16
//	        insertComment(_while, _indent);
//	        // END KGU 2014-11-16
//
//	        String condition = transform(_while.getText().getLongString(), false).trim();
//	        if (!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
//	        
//	    	// START KGU#78 2015-11-30: More sophisticated jump handling
//	        //code.add(_indent+"while " + condition + " {");
//	    	String label = jumpTable.get(_while);
//			String brace = optionBlockBraceNextLine() ? "" : " {";
//	    	code.add(_indent + ((label == null) ? "" : (label + ": ")) + "while " + condition + brace);
//	    	if (optionBlockBraceNextLine())	code.add(_indent + "{");
//	    	// END KGU#78 2015-11-30
//			generateCode(_while.q, _indent+this.getIndent());
//			code.add(_indent+"}");
//		}
		
// KGU#16 2015-11-30: Now inherited	from CGenerator	
//		@Override
//		protected void generateCode(Repeat _repeat, String _indent)
//		{
//			// START KGU 2014-11-16
//			insertComment(_repeat, _indent);
//			// END KGU 2014-11-16
//
//			// START KGU#78 2015-11-30: More sophisticated jump handling
//			//code.add(_indent + "do {");
//			String label = jumpTable.get(_repeat);
//			String brace = optionBlockBraceNextLine() ? "" : " {";
//			code.add(_indent + ((label == null) ? "" : (label + ": ")) + "do" + brace);
//			if (optionBlockBraceNextLine())	code.add(_indent + "{");
//			// END KGU#78 2015-11-30
//			generateCode(_repeat.q, _indent + this.getIndent());
//			code.add(_indent + "} while (!(" + transform(_repeat.getText().getLongString(), false).trim() + "));");
//		}
		
// KGU#16 2015-11-30: Now inherited	from CGenerator	
//		protected void generateCode(Forever _forever, String _indent)
//		{
//	        // START KGU 2014-11-16
//	        insertComment(_forever, _indent);
//	        // END KGU 2014-11-16
//
//	    	// START KGU#78 2015-11-30: More sophisticated jump handling
//	        //code.add(_indent + "while (true) {");
//	    	String label = jumpTable.get(_forever);
//			String brace = optionBlockBraceNextLine() ? "" : " {";
//	    	code.add(_indent + ((label == null) ? "" : (label + ": ")) + "while (true)" + brace);
//	    	if (optionBlockBraceNextLine())	code.add(_indent + "{");
//	    	// END KGU#78 2015-11-30
//			generateCode(_forever.q, _indent+this.getIndent());
//			code.add(_indent + "}");
//		}

		
// KGU#16 2015-11-30: Now inherited	from CGenerator	
//		protected void generateCode(Call _call, String _indent)
//		{
//			if (!insertAsComment(_call, _indent)) {
//				
//				insertComment(_call, _indent);
//
//				StringList lines = _call.getText();
//				for (int i = 0; i < lines.count(); i++)
//				{
//					String line = _call.getText().get(i);
//					// KGU 2015-11-01 It was of little use, always to append a parenthesis pair
//					if (!line.endsWith(")")) line = line + "()";
//					// Input or Output should not occur here
//					code.add(_indent+transform(line, false) + ";");
//				}
//
//			}
//		}

// KGU#74 2015-11-30: Now inherited from CGenerator
//		protected void generateCode(Jump _jump, String _indent)
//		{
//	        // START KGU 2014-11-16
//	        insertComment(_jump, _indent);
//	        // END KGU 2014-11-16
//
//			// KGU 2015-10-18: In case of an empty text generate a break instruction by default.
//			boolean isEmpty = true;
//			StringList lines = _jump.getText();
//			for (int i = 0; i < lines.count() && isEmpty; i++)
//			{
//				String line = transform(lines.get(i), false);
//				if (!line.trim().isEmpty()) isEmpty = false;
//				// START KGU#78 2015-11-30: Mor sophisticated jump handling
//				//code.add(_indent + line + ";\t// FIXME goto instructions not allowed in Java");
//				if (line.matches(Matcher.quoteReplacement(D7Parser.preReturn)+"([\\W].*|$)"))
//				{
//					code.add(_indent + line + ";");
//				}
//				else if (line.matches(Matcher.quoteReplacement(D7Parser.preExit)+"([\\W].*|$)"))
//				{
//					code.add(_indent + "System.exit(" + line.substring(D7Parser.preExit.length()).trim() + ");");
//				}
//				else if (this.jumpTable.containsKey(_jump))
//				{
//					String label = this.jumpTable.get(_jump);
//					code.add(_indent + "break " + label + ";");
//				}
//				else if (line.matches(Matcher.quoteReplacement(D7Parser.preLeave)+"([\\W].*|$)"))
//				{
//					// An ordinary break instruction seems to suffice
//					isEmpty = true;
//				}
//				else
//				{
//					code.add(_indent + line + ";\t// FIXME goto instructions not allowed in Java!");
//				}
//				// END KGU#78 2015-11-30
//			}
//			if (isEmpty)
//			{
//				code.add(_indent + "break;");
//			}
//		}
		

// KGU#16 (2015-11-30): Now we only override the decomposed methods below
//		public String generateCode(Root _root, String _indent)
//		{
//			// START KGU#74 2015-11-30: Prepare the label associations
//			String fnName = _root.getMethodName();
//			this.alwaysReturns = this.mapJumps(_root.children);
//			// Get all local variable names
//			StringList varNames = _root.getVarNames(_root, false, true);
//			String brace = optionBlockBraceNextLine() ? "" : " {";
//			// END KGU#74 2015-11-30
//			if(_root.isProgram==true) {
//				code.add("import java.util.Scanner;");
//				code.add("");
//				// START KGU 2015-10-18
//				insertBlockComment(_root.getComment(), "", "/**", " * ", " */");
//				// END KGU 2014-10-18
//				code.add("public class " + fnName + " {");
//				code.add("");
//				insertComment("TODO Declare and initialise class variables here", this.getIndent());
//				code.add("");
//				code.add(this.getIndent() + "/**");
//				code.add(this.getIndent() + " * @param args");
//				code.add(this.getIndent() + " */");
//				code.add(this.getIndent() + "public static void main(String[] args)" + brace);
//				if (optionBlockBraceNextLine())	code.add(this.getIndent() + "{");
//
//				insertComment("TODO Declare and initialise local variables here:", this.getIndent() + this.getIndent());
//				// START KGU 2015-11-30
//				for (int v = 0; v < varNames.count(); v++)
//				{
//					insertComment(varNames.get(v), this.getIndent() + this.getIndent());
//				}
//				// END KGU 2015-11-30
//				code.add(this.getIndent()+ this.getIndent() +"");
//				generateCode(_root.children, this.getIndent()+this.getIndent());
//				code.add(this.getIndent()+"}");
//				code.add("");
//				code.add("}");
//			}
//			else {
//		        // START KGU 2014-10-18
//				// START KGU 2015-11-30: More precise header information
//				StringList argNames = new StringList();
//				StringList argTypes = new StringList();
//				_root.collectParameters(argNames, argTypes);
//				String resultType = _root.getResultType();
//		        insertBlockComment(_root.getComment(), _indent+this.getIndent(), "/**", " * ", null);
//		        if (resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
//		        {
//		        	insertBlockComment(argNames, _indent+this.getIndent(), null, " * @param ", null);
//		        	code.add(_indent+this.getIndent() + " * @return ");
//		        	code.add(_indent+this.getIndent() + " */");
//		        	resultType = transformType(resultType, "int");
//		        }
//		        else
//		        {
//		        	insertBlockComment(argNames, this.getIndent(), null, " * @param ", " */");
//		        	resultType = "void";
//		        }
//		        String fnHeader = "public static " + resultType + " " + fnName + "(";
//				for (int p = 0; p < argNames.count(); p++) {
//					if (p > 0)
//						fnHeader += ", ";
//					fnHeader += (transformType(argTypes.get(p), "/*TODO*/") + " " + argNames
//							.get(p)).trim();
//				}
//				fnHeader += ")";
//				code.add(this.getIndent() + fnHeader + brace);
//				insertComment("TODO declare parameters and local variables:", this.getIndent()+this.getIndent());
//				// START KGU 2015-11-30
//				for (int v = 0; v < varNames.count(); v++)
//				{
//					insertComment(varNames.get(v), this.getIndent() + this.getIndent());
//				}
//				// END KGU 2015-11-30
//				
//				// Method body
//				code.add("");
//				generateCode(_root.children,_indent+this.getIndent()+this.getIndent());
//				code.add("");
//				
//				// Ensuring result
//				this.isResultSet = varNames.contains("result", false);
//				this.isFunctionNameSet = varNames.contains(fnName);
//				if ((this.returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns) {
//					String result = "0";
//					if (isFunctionNameSet) {
//						result = fnName;
//					} else if (isResultSet) {
//						int vx = varNames.indexOf("result", false);
//						result = varNames.get(vx);
//					}
//					code.add(this.getIndent());
//					code.add(this.getIndent()+this.getIndent() + "return " + result + ";");
//				}
//				code.add(this.getIndent()+"}");
//			}
//			
//			return code.getText();
//		}

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
		if (_root.isProgram==true) {
			code.add(_indent + "import java.util.Scanner;");
			code.add("");
			insertBlockComment(_root.getComment(), _indent, "/**", " * ", " */");
			insertBlockHeading(_root, "public class " + _procName, _indent);

			code.add("");
			insertComment("TODO Declare and initialise class variables here", this.getIndent());
			code.add("");
			code.add(_indent+this.getIndent() + "/**");
			code.add(_indent+this.getIndent() + " * @param args");
			code.add(_indent+this.getIndent() + " */");

			insertBlockHeading(_root, "public static void main(String[] args)", _indent+this.getIndent());
		}
		else {
			insertBlockComment(_root.getComment(), _indent+this.getIndent(), "/**", " * ", null);
			insertBlockComment(_paramNames, _indent+this.getIndent(), null, " * @param ", null);
			if (_resultType != null || this.returns || this.isFunctionNameSet || this.isResultSet)
			{
				code.add(_indent+this.getIndent() + " * @return ");
				_resultType = transformType(_resultType, "int");
			}
			else {
				_resultType = "void";		        	
			}
			code.add(_indent+this.getIndent() + " */");
			String fnHeader = "public static " + _resultType + " " + _procName + "(";
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0)
					fnHeader += ", ";
				fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " +
						_paramNames.get(p)).trim();
			}
			fnHeader += ")";
			insertBlockHeading(_root, fnHeader,  _indent + this.getIndent());
		}

		return _indent + this.getIndent() + this.getIndent();
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
		code.add(_indent);
		insertComment("TODO: Declare and initialise local variables here:", _indent);
		for (int v = 0; v < varNames.count(); v++) {
			insertComment(varNames.get(v), _indent);
		}
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
		if ((this.returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns) {
			String result = "0";
			if (isFunctionNameSet) {
				result = _root.getMethodName();
			} else if (isResultSet) {
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
			}
			code.add(_indent);
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}

	// START KGU 2015-12-15: Method block must be closed as well
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root 
	 * @param _indent
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		// Method block close
		code.add(_indent + this.getIndent() + "}");

		super.generateFooter(_root, _indent);
	}
	// END KGU 2015-12-15

}

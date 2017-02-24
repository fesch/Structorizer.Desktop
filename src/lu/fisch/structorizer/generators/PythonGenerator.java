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
 *      Author:         Daniel Spittank
 *
 *      Description:    This class generates Java code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------					----			-----------
 *      Bob Fisch               2008.11.17      First Issue
 *      Gunter Schillebeeckx    2009.08.10      Java Generator starting from C Generator
 *      Bob Fisch               2009.08.10      Update I/O
 *      Bob Fisch               2009.08.17      Bugfixes (see comment)
 *      Kay Gürtzig             2010.09.10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Daniel Spittank         2014.02.01      Python Generator starting from Java Generator
 *      Kay Gürtzig             2014.11.16      Conversion of C-like logical operators and arcus functions (see comment)
 *      Kay Gürtzig             2014.12.02      Additional replacement of long assignment operator "<--" by "<-"
 *      Kay Gürtzig             2015.10.18      Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig             2015.12.12      bugfix #59 (KGU#104) with respect to ER #10
 *      Kay Gürtzig             2015.12.17      Enh. #23 (KGU#78) jump generation revised; Root generation
 *                                              decomposed according to Generator.generateCode(Root, String);
 *                                              Enh. KGU#47: Dummy implementation for Parallel element
 *      Kay Gürtzig             2015.12.21      Bugfix #41/#68/#69 (= KGU#93)
 *      Kay Gürtzig             2015.12.22      Bugfix #51/#54 (= KGU#108) empty input and output expression lists
 *      Kay Gürtzig             2016.01.14      Enh. #84 (= KGU#100) Array init. expr. support
 *      Kay Gürtzig             2016.01.17      Bugfix #61 (= KGU#109) Type names removed from assignments
 *      Kay Gürtzig             2016.03.16      Enh. #84: Support for FOREACH loops (KGU#61) 
 *      Kay Gürtzig             2016.04.01      Enh. #144: Care for new option to suppress content conversion 
 *      Kay Gürtzig             2016-07-20      Enh. #160: Option to involve subroutines implemented (=KGU#178),
 *                                              bugfix for routine calls (superfluous parentheses dropped)
 *      Kay Gürtzig             2016.09.25      Enh. #253: D7Parser.keywordMap refactoring done
 *      Kay Gürtzig             2016.10.14      Enh. #270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016.10.15      Enh. #271: Support for input instructions with prompt
 *      Kay Gürtzig             2016.10.16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig             2016.12.01      Bugfix #301: More precise check for parenthesis enclosing of log. conditions
 *      Kay Gürtzig             2016.12.27      Enh. #314: Support for Structorizer File API
 *      Kay Gürtzig             2017.02.19      Enh. #348: Parallel sections translated with threading module
 *      Kay Gürtzig             2017.02.23      Issue #350: getOutputReplacer() and Parallel export revised again
 *
 ******************************************************************************************************
 *
 *      Comments:
 *
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015.10.18 - Bugfixes and modifications (Kay Gürtzig)
 *      - Comment method signature simplified
 *      - Bugfix: The export option "export instructions as comments" had been ignored before
 *      - Bugfix KGU#54: generateCode(Repeat,String) ate the last two lines of the loop body!
 *      - The indentation logic was somehow inconsistent
 *
 *      2014.11.16 - Bugfixes / Enhancement
 *      - Conversion of C-style logical operators to the Python-conform ones added
 *      - assignment operator conversion now preserves or ensures surrounding spaces
 *      - workaround for reverse trigonometric functions added
 *      - Operator != had been converted to !==
 *      - comment export introduced
 *
 *      2014.02.01 - First Version of Python Generator
 *      
 *      2010.09.10 - Bugfixes
 *      - condition for automatic bracket addition around condition expressions corrected
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator conversion
 *      - pascal function conversion
 *
 *      2009.08.10
 *        - writeln() => System.out.println()
 * 
 ******************************************************************************************************///

import java.util.LinkedList;
import java.util.regex.Matcher;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;


public class PythonGenerator extends Generator 
	{
		
		/************ Fields ***********************/
		protected String getDialogTitle()
		{
			return "Export Python ...";
		}
		
		protected String getFileDescription()
		{
			return "Python Source Code";
		}
		
		protected String getIndent()
		{
			return "    ";
		}
		
		protected String[] getFileExtensions()
		{
			String[] exts = {"py"};
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
			"and", "assert", "break", "class", "continue",
			"def", "del",
			"else", "elif", "except", "exec",
			"finally", "for", "from", "global",
			"if", "import", "in", "is", "lambda", "not", "or",
			"pass", "print", "raise", "return", 
			"try", "while",
			"Data", "Float", "Int", "Numeric", "Oxphys",
			"array", "close", "float", "int", "input",
			"open", "range", "type", "write", "zeros"
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
		// START KGU#281 2016-10-15: Enh. #281
		//protected String getInputReplacer()
		//{
		//	return "$1 = input(\"$1\")";
		//}
		protected String getInputReplacer(boolean withPrompt)
		{
			if (withPrompt) {
				return "$2 = input($1)";				
			}
			return "$1 = input(\"$1\")";
		}
		// END KGU#281 2016-10-15

		/**
		 * A pattern how to embed the expression (right-hand side of an output instruction)
		 * into the target code
		 * @return a regex replacement pattern, e.g. "System.out.println($1);"
		 */
		protected String getOutputReplacer()
		{
			// START KGU#108 2015-12-22: Bugfix #51, #54: Parenthesis was rather wrong (produced lists)
			//return "print($1)";
			// START KGU 2017-02-23: for Python 3.5, parentheses ARE necessary, separator is to be suppressed
			//return "print $1";
			return "print($1, sep='')";
			// END KGU 2017-02-23
			// END KGU#108 2015-12-22
		}

		// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//		/**
//		 * Transforms assignments in the given intermediate-language code line.
//		 * Replaces "<-" by "="
//		 * @param _interm - a code line in intermediate syntax
//		 * @return transformed string
//		 */
//		@Deprecated
//		protected String transformAssignment(String _interm)
//		{
//			return _interm.replace(" <- ", " = ");
//		}
	    
		/* (non-Javadoc)
		 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
		 */
		@Override
		protected String transformTokens(StringList tokens)
		{
			// START KGU 2014-11-16: C comparison operator required conversion before logical ones
			tokens.replaceAll("!="," <> ");
			// convert C logical operators
			tokens.replaceAll("&&"," and ");
			tokens.replaceAll("||"," or ");
			tokens.replaceAll("!"," not ");
			tokens.replaceAll("xor","^");            
			// END KGU 2014-11-16
			tokens.replaceAll("div", "/");
			tokens.replaceAll("<-", "=");
			// START KGU#100 2016-01-14: Enh. #84 - convert C/Java initialisers to lists
			tokens.replaceAll("{", "[");
			tokens.replaceAll("}", "]");
			// END KGU#100 2016-01-14
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
			if (transformed.startsWith(" = input("))
			{
				transformed = "generatedDummy" + transformed;
			}
			return transformed;
		}
		// END KGU#108 2015-12-22
		
		// START KGU#18/KGU#23 2015-11-01: Obsolete    
//	    private String transform(String _input)
		/* (non-Javadoc)
		 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
		 */
		@Override
		protected String transform(String _input)
		{
			// START KGU#162 2016-04-01: Enh. #144 - hands off in "no conversion" mode!
			if (!this.suppressTransformation)
			{
			// END KGU#162 2016-04-01
				// START KGU#109 2016-01-17: Bugfix #61 - Remove type specifiers
				// Could we also do it by replacing all inventable type names by empty strings
				// in transformType()? Rather not.
				_input = Element.unifyOperators(_input);
				int asgnPos = _input.indexOf("<-");	// This might mutilate string literals!
				if (asgnPos > 0)
				{
					String lval = _input.substring(0, asgnPos).trim();
					String expr = _input.substring(asgnPos + "<-".length()).trim();
					String[] typeNameIndex = this.lValueToTypeNameIndex(lval);
					String index = typeNameIndex[2];
					if (!index.isEmpty()) index = "[" + index + "]";
					_input = typeNameIndex[1] + index + " <- " + expr;
				}
				// END KGU#109 2016-01-17
			// START KGU#162 2016-04-01: Enh. #144 - hands off in "no conversion" mode!
			}
			// END KGU#162 2016-04-01

			_input = super.transform(_input);

			String s = _input;

//			// START KGU 2014-11-16: C comparison operator required conversion before logical ones
//			_input=BString.replace(_input,"!="," <> ");
//			// convert C logical operators
//			_input=BString.replace(_input," && "," and ");
//			_input=BString.replace(_input," || "," or ");
//			_input=BString.replace(_input," ! "," not ");
//			_input=BString.replace(_input,"&&"," and ");
//			_input=BString.replace(_input,"||"," or ");
//			_input=BString.replace(_input,"!"," not ");
//			_input=BString.replace(_input," xor "," ^ ");            
//			// END KGU 2014-11-16
//
//            // convert Pascal operators
//			_input=BString.replace(_input," div "," / ");
//
//			s = _input;
			// Math function
			s=s.replace("cos(", "math.cos(");
			s=s.replace("sin(", "math.sin(");
			s=s.replace("tan(", "math.tan(");
			// START KGU 2014-11-16: After the previous replacements the following 3 strings would never be found!
			//s=s.replace("acos(", "math.acos(");
			//s=s.replace("asin(", "math.asin(");
			//s=s.replace("atan(", "math.atan(");
			// This is just a workaround; A clean approach would require a genuine lexical scanning in advance
			s=s.replace("amath.cos(", "math.acos(");
			s=s.replace("amath.sin(", "math.asin(");
			s=s.replace("amath.tan(", "math.atan(");
			// END KGU 2014-11-16
			//s=s.replace("abs(", "abs(");
			//s=s.replace("round(", "round(");
			//s=s.replace("min(", "min(");
			//s=s.replace("max(", "max(");
			s=s.replace("ceil(", "math.ceil(");
			s=s.replace("floor(", "math.floor(");
			s=s.replace("exp(", "math.exp(");
			s=s.replace("log(", "math.log(");
			s=s.replace("sqrt(", "math.sqrt(");
			s=s.replace("pow(", "math.pow(");
			s=s.replace("toRadians(", "math.radians(");
			s=s.replace("toDegrees(", "math.degrees(");
			// clean up ... if needed
			//s=s.replace("Math.Math.", "math.");

			return s.trim();
		}
		
		protected void generateCode(Instruction _inst, String _indent)
		{
			if(!insertAsComment(_inst, _indent)) {
				boolean isDisabled = _inst.isDisabled();
				// START KGU 2014-11-16
				insertComment(_inst, _indent);
				// END KGU 2014-11-16
				for(int i=0;i<_inst.getText().count();i++)
				{
					// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
					//code.add(_indent + transform(_inst.getText().get(i)));
					String line = _inst.getText().get(i);
					String codeLine = transform(line);
					if (Instruction.isTurtleizerMove(line)) {
						codeLine += " " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
					}
					addCode(codeLine, _indent, isDisabled);
					// END KGU#277/KGU#284 2016-10-13/16
				}
			}
		}
		
		protected void generateCode(Alternative _alt, String _indent)
		{
			boolean isDisabled = _alt.isDisabled();
			
			// START KGU 2014-11-16
			insertComment(_alt, _indent);
			// END KGU 2014-11-16

			String condition = BString.replace(transform(_alt.getText().getText()),"\n","").trim();
			// START KGU#301 2016-12-01: Bugfix #301
			//if (!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
			if (!isParenthesized(condition)) condition = "(" + condition + ")";
			// END KGU#301 2016-12-01

			addCode("if "+condition+":", _indent, isDisabled);
			generateCode((Subqueue) _alt.qTrue,_indent + this.getIndent());
			if(_alt.qFalse.getSize()!=0)
			{
				addCode("else:", _indent, isDisabled);
				generateCode((Subqueue) _alt.qFalse, _indent + this.getIndent());
			}
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				addCode("", "", isDisabled);
			}
			// END KGU#54 2015-10-19
		}
		
		protected void generateCode(Case _case, String _indent)
		{
			boolean isDisabled = _case.isDisabled();
			
			// START KGU 2014-11-16
			insertComment(_case, _indent);
			// END KGU 2014-11-16

			StringList lines = _case.getText();
			String condition = transform(lines.get(0));

			for(int i=0; i<_case.qs.size()-1; i++)
			{
				String caseline = _indent + ((i == 0) ? "if" : "elif") + " (";
	    		// START KGU#15 2015-10-21: Support for multiple constants per branch
	    		StringList constants = StringList.explode(lines.get(i+1), ",");
	    		for (int j = 0; j < constants.count(); j++)
	    		{
	    			if (j > 0) caseline = caseline + " or ";
	    			caseline = caseline + "(" + condition + ") == " + constants.get(j).trim();
	    		}
	    		// END KGU#15 2015-10-21
				addCode(caseline + ") :", "", isDisabled);
				generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent());
			}
			
			if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
			{
				addCode("else:", _indent, isDisabled);
				generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent + this.getIndent());
			}
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				addCode("", "", isDisabled);
			}
			// END KGU#54 2015-10-19
		}
		
		protected void generateCode(For _for, String _indent)
		{
			boolean isDisabled = _for .isDisabled();
			
			// START KGU 2014-11-16
			insertComment(_for, _indent);
			// END KGU 2014-11-16

			String counterStr = _for.getCounterVar();
			String valueList = "";
			if (_for.isForInLoop())
			{
				valueList = _for.getValueList();
				StringList items = this.extractForInListItems(_for);
				if (items != null)
				{
					valueList = "[" + transform(items.concatenate(", "), false) + "]";
				}
			}
			else 
			{
				String startValueStr = this.transform(_for.getStartValue());
				String endValueStr = this.transform(_for.getEndValue());
				String stepValueStr = _for.getStepString();
				valueList = "range("+startValueStr+", "+endValueStr+", "+stepValueStr+")";
			}
			addCode("for "+counterStr+" in " + valueList + ":", _indent, isDisabled);
			generateCode((Subqueue) _for.q,_indent + this.getIndent());
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				addCode("", "", isDisabled);
			}
			// END KGU#54 2015-10-19
		}
		
		protected void generateCode(While _while, String _indent)
		{
			boolean isDisabled = _while.isDisabled();
			
			// START KGU 2014-11-16
			insertComment(_while, _indent);
			// END KGU 2014-11-16
			
			String condition = BString.replace(transform(_while.getText().getText()),"\n","").trim();
			// START KGU#301 2016-12-01: Bugfix #301
			//if (!condition.startsWith("(") || !condition.endsWith(")")) condition="("+condition+")";
			if (!isParenthesized(condition)) condition = "(" + condition + ")";
			// END KGU#301 2016-12-01
			
			addCode("while "+condition+":", _indent, isDisabled);
			generateCode((Subqueue) _while.q, _indent + this.getIndent());
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				addCode("", "", isDisabled);
			}
			// END KGU#54 2015-10-19
		}

        protected void generateCode(Repeat _repeat, String _indent)
        {
        	boolean isDisabled = _repeat.isDisabled();
        	
			// START KGU 2014-11-16
			insertComment(_repeat, _indent);
			// END KGU 2014-11-16
            addCode("while True:", _indent, isDisabled);
            generateCode((Subqueue) _repeat. q,_indent + this.getIndent());
            // START KGU#54 2015-10-19: Why should the last two rows be empty? They aren't! This strange behaviour ate code lines! 
            //code.delete(code.count()-1); // delete empty row
            //code.delete(code.count()-1); // delete empty row
            // END KGU#54 2015-10-19
            addCode("if "+BString.replace(transform(_repeat.getText().getText()),"\n","").trim()+":",
            		_indent+this.getIndent(), isDisabled);
            addCode("break", _indent+this.getIndent()+this.getIndent(), isDisabled);
			// START KGU#54 2015-10-19: Add an empty line, but void accumulation of empty lines!
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				addCode("", "", isDisabled);
			}
			// END KGU#54 2015-10-19
        }
		
		protected void generateCode(Forever _forever, String _indent)
		{
			boolean isDisabled = _forever.isDisabled();
			
			// START KGU 2014-11-16
			insertComment(_forever, _indent);
			// END KGU 2014-11-16
			addCode("while True:", _indent, isDisabled);
			generateCode((Subqueue) _forever.q, _indent + this.getIndent());
			// START KGU#54 2015-10-19: Avoid accumulation of empty lines!
			//code.add("");
			if (code.count() > 0 && !code.get(code.count()-1).isEmpty())
			{
				addCode("", "", isDisabled);
			}
			// END KGU#54 2015-10-19
		}
		
		protected void generateCode(Call _call, String _indent)
		{
			if(!insertAsComment(_call, _indent))
			{
				boolean isDisabled = _call.isDisabled();
				// START KGU 2014-11-16
				insertComment(_call, _indent);
				// END KGU 2014-11-16
				for(int i=0; i<_call.getText().count(); i++)
				{
					// START KGU 2016-07-20: Bugfix the extra parentheses were nonsense
					//code.add(_indent+transform(_call.getText().get(i))+"()");
					addCode(transform(_call.getText().get(i)), _indent, isDisabled);
					// END KGU 2016-07-20
				}
			}
		}
		
		protected void generateCode(Jump _jump, String _indent)
		{
			if(!insertAsComment(_jump, _indent))
			{
				boolean isDisabled = _jump.isDisabled();
				// START KGU 2014-11-16
				insertComment(_jump, _indent);
				// END KGU 2014-11-16
				// START KGU#78 2015-12-17: Enh. 38 - translate acceptable Jumps to break instructions
				//for(int i=0;i<_jump.getText().count();i++)
				//{
				//	insertComment(transform(_jump.getText().get(i))+" # FIXME goto instructions not allowed in Python", _indent);
				//}
				// In case of an empty text generate a break instruction by default.
				boolean isEmpty = true;

				StringList lines = _jump.getText();
				String preReturn = D7Parser.getKeywordOrDefault("preReturn", "return");
				String preLeave  = D7Parser.getKeywordOrDefault("preLeave", "leave");
				String preReturnMatch = Matcher.quoteReplacement(preReturn)+"([\\W].*|$)";
				String preLeaveMatch  = Matcher.quoteReplacement(preLeave)+"([\\W].*|$)";
				for (int i = 0; isEmpty && i < lines.count(); i++) {
					String line = transform(lines.get(i)).trim();
					if (!line.isEmpty())
					{
						isEmpty = false;
					}
					if (line.matches(preReturnMatch))
					{
						addCode("return " + line.substring(preReturn.length()).trim(),
								_indent, isDisabled);
					}
					else if (line.matches(preLeaveMatch))
					{
						// We may only allow one-level breaks, i. e. there must not be an argument
						// or the argument must be 1 and a legal label must be associated.
						String arg = line.substring(preLeave.length()).trim();
						Integer label = this.jumpTable.get(_jump);
						if (label != null && label.intValue() >= 0 &&
								(arg.isEmpty() || Integer.parseInt(arg) == 1))
						{
							addCode("break", _indent, isDisabled);		
						}
						else
						{
							addCode("break # FIXME: Illegal multi-level break attempted!",
									_indent, isDisabled);
						}
					}
					else if (!isEmpty)
					{
						insertComment("FIXME: unsupported jump/exit instruction!", _indent);
						insertComment(line, _indent);
					}
				}
				if (isEmpty) {
					addCode("break", _indent, isDisabled);
				}
				// END KGU#78 2015-12-17
			}
		}
		
		// START KGU#47 2015-12-17: Offer at least a sequential execution (which is one legal execution order)
		protected void generateCode(Parallel _para, String _indent)
		{
			boolean isDisabled = _para.isDisabled();
			Root root = Element.getRoot(_para);
			
			//String indentPlusOne = _indent + this.getIndent();
			//String indentPlusTwo = indentPlusOne + this.getIndent();
			insertComment(_para, _indent);

			addCode("", _indent, isDisabled);
			//insertComment("==========================================================", _indent);
			//insertComment("================= START PARALLEL SECTION =================", _indent);
			//insertComment("==========================================================", _indent);
			//insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
			//addCode("", indentPlusOne, isDisabled);

			for (int i = 0; i < _para.qs.size(); i++) {
				//insertComment("----------------- START THREAD " + i + " -----------------", indentPlusOne);
				// START KGU#348 2017-02-19: Enh. #348 Actual translation
				//generateCode((Subqueue) _para.qs.get(i), indentPlusTwo);
				Subqueue sq = _para.qs.get(i);
				String threadVar = "thr" + _para.hashCode() + "_" + i;
				String threadFunc = "thread" + _para.hashCode() + "_" + i;
				StringList used = root.getUsedVarNames(sq, false, false);
				StringList asgnd = root.getVarNames(sq, false);
				for (int v = 0; v < asgnd.count(); v++) {
					used.removeAll(asgnd.get(v));
				}
				String args = used.concatenate(",");
				if (used.count() == 1) {
					args += ",";
				}
				if (sq.getSize() == 1) {
					Element el = sq.getElement(0);
					if (el instanceof Call && ((Call)el).isProcedureCall()) {
						threadFunc = ((Call)el).getCalledRoutine().getName();
					}
				}
				addCode(threadVar + " = Thread(target=" + threadFunc + ", args=(" + args + "))", _indent, isDisabled);
				addCode(threadVar + ".start()", _indent, isDisabled);
				addCode("", _indent, isDisabled);
				// END KGU#348 2017-02-19
				//insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
				//addCode("", indentPlusOne, isDisabled);
			}

			for (int i = 0; i < _para.qs.size(); i++) {
				String threadVar = "thr" + _para.hashCode() + "_" + i;
				addCode(threadVar + ".join()", _indent, isDisabled);
			}
			//insertComment("==========================================================", _indent);
			//insertComment("================== END PARALLEL SECTION ==================", _indent);
			//insertComment("==========================================================", _indent);
			addCode("", _indent, isDisabled);
		}
		// END KGU#47 2015-12-17

		// START KGU#47/KGU#348 2017-02-19: Enh. #348
		private void generateParallelThreadFunctions(Root _root, String _indent)
		{
			String indentPlusOne = _indent + this.getIndent();
			int lineBefore = code.count();
			StringList globals = new StringList();
			final LinkedList<Parallel> containedParallels = new LinkedList<Parallel>();
			_root.traverse(new IElementVisitor() {
				@Override
				public boolean visitPreOrder(Element _ele) {
					return true;
				}
				@Override
				public boolean visitPostOrder(Element _ele) {
					if (_ele instanceof Parallel) {
						containedParallels.addLast((Parallel)_ele);
					}
					return true;
				}
			});
			for (Parallel par: containedParallels) {
				boolean isDisabled = par.isDisabled();
				String functNameBase = "thread" + par.hashCode() + "_";
				int i = 0;
				// We still don't care for synchronisation, mutual exclusion etc.
				for (Subqueue sq: par.qs) {
					Element el = null;
					if (sq.getSize() == 1 && (el = sq.getElement(0)) instanceof Call && ((Call)el).isProcedureCall()) {
						// Don't generate a thread function for single procedure calls
						continue;
					}
					// Variables assigned here will be made global
					StringList setVars = _root.getVarNames(sq, false);
					// Variables used here without being assigned will be made arguments
					StringList usedVars = _root.getUsedVarNames(sq, false, false);
					for (int v = 0; v < setVars.count(); v++) {
						usedVars.removeAll(setVars.get(v));
					}
					addCode("def " + functNameBase + i + "(" + usedVars.concatenate(", ") + "):", _indent, isDisabled);
					for (int v = 0; v < setVars.count(); v++) {
						String varName = setVars.get(v);
						globals.addIfNew((isDisabled ? this.commentSymbolLeft() : "") + varName);
						addCode("global " + varName, indentPlusOne, isDisabled);
					}
					generateCode(sq, indentPlusOne);
					code.add(_indent);
					i++;
				}
			}
			if (globals.count() > 0) {
				code.insert(_indent + "", lineBefore);
				for (int v = 0; v < globals.count(); v++) {
					String varName = globals.get(v);
					String end = varName.startsWith(this.commentSymbolLeft()) ? this.commentSymbolRight() : "";
					code.insert(_indent + varName + " = 0" + end, lineBefore);
				}
				code.insert(_indent + this.commentSymbolLeft() + " TODO: Initialize these variables globally referred to by prallel threads in a sensible way!", lineBefore);
			}
		}
		// END KGU#47/KGU#348 2017-02-19

		/* (non-Javadoc)
		 * @see lu.fisch.structorizer.generators.Generator#generateHeader(lu.fisch.structorizer.elements.Root, java.lang.String, java.lang.String, lu.fisch.utils.StringList, lu.fisch.utils.StringList, java.lang.String)
		 */
		@Override
		protected String generateHeader(Root _root, String _indent, String _procName,
				StringList _paramNames, StringList _paramTypes, String _resultType)
		{
			String indent = "";
			// START KGU#178 2016-07-20: Enh. #160 -option to involve called functions
//			if (_root.isProgram) {
//				code.add(_indent + "#!/usr/bin/env python");
//				insertComment(_root.getText().get(0), _indent);
//				// START KGU 2016-01-14: Enhanced, as a help for bugfixing etc.
//				insertComment("generated by Structorizer " + Element.E_VERSION, _indent);
//				// END KGU 2016-01-14
//				code.add("");
//				insertComment(_root, _indent);
//			}
			if (topLevel)
			{
				code.add(_indent + "#!/usr/bin/env python");
				insertComment(_root.getText().get(0), _indent);
				insertComment("generated by Structorizer " + Element.E_VERSION, _indent);
				// START KGU#348 2017-02-19: Enh. #348 - Translation of parallel sections
				if (this.hasParallels) {
					code.add(_indent);
					code.add(_indent + "from threading import Thread");
				}
				// END KGU#348 2017-02-19
				subroutineInsertionLine = code.count();
				// START KGU#311 2016-12-27: Enh. #314: File API support
				if (this.usesFileAPI) {
					this.insertFileAPI("py");
				}
				// END KGU#311 2016-12-27
			}
			code.add("");
			if (_root.isProgram) {
				insertComment(_root, _indent);
			}
			// END KGU#178 2016-07-20
			else {
				indent = _indent + this.getIndent();
				insertComment(_root, _indent);
				code.add(_indent + "def " + _procName +"(" + _paramNames.getText().replace("\n", ", ") +") :");
			}
			return indent;
		}

		/* (non-Javadoc)
		 * @see lu.fisch.structorizer.generators.Generator#generatePreamble(lu.fisch.structorizer.elements.Root, java.lang.String, lu.fisch.utils.StringList)
		 */
		@Override
		protected String generatePreamble(Root _root, String _indent, StringList _varNames)
		{
			// START KGU#348 2017-02-19: Enh. #348 - Translation of parallel sections
			generateParallelThreadFunctions(_root, _indent);
			// END KGU#348 2017-02-19
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
				code.add(_indent + "return " + result);
			}
			return _indent;
		}
		// END KGU#78 2015-12-17

	}

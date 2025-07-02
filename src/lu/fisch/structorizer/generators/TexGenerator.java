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
 *      Description:    This class generates LaTeX code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-27      First Issue
 *      Bob Fisch       2008-04-12      Added "Fields" section for generator to be used as plugin
 *      Kay Gürtzig     2015-10-18      Adaptations to updated Generator structure and interface
 *      Kay Gürtzig     2015-11-01      KGU#18/KGU#23 Transformation decomposed
 *      Kay Gürtzig     2015-12-18/19   KGU#2/KGU#47/KGU#78 Fixes for Call, Jump, and Parallel elements
 *      Kay Gürtzig     2016-07-20      Enh. #160 adapted (option to integrate subroutines = KGU#178)
 *      Kay Gürtzig     2016-09-25      Enh. #253: CodeParser.keywordMap refactoring done.
 *      Kay Gürtzig     2016-10-14      Enh. #270: Disabled elements are skipped here now
 *      Kay Gürtzig     2017-05-16      Enh. #372: Export of copyright information
 *      Kay Gürtzig     2017-12-30/31   Bugfix #497: Text export had been defective, Parallel export was useless
 *      Kay Gürtzig     2018-01-02      Issue #497: FOR-IN loop list conversion fixed, height arg reduced, includedRoots involved
 *      Kay Gürtzig     2019-09-27      Enh. #738: Support for code preview map on Root level
 *      Kay Gürtzig     2020-04-03      Enh. #828: Configuration for group export
 *      Kay Gürtzig     2020-10-19      Bugfix #877: Division by zero exception on batch export (Alternative)
 *      Kay Gürtzig     2021-02-03      Issue #920: Transformation for "Infinity" literal
 *      Kay Gürtzig     2021-05-12      Bugfix #975: Backslashes (and tilde symbols) hadn't been escaped in text literals
 *      Kay Gürtzig     2021-06-06      Bugfix #975: ^ within tokens replaced by \textasciicircum rather than \hat{}
 *                                      method transformText extracted for the token-internal substitution
 *      Kay Gürtzig     2022-08-23      Structorizer version inserted as LaTeX comment
 *      Kay Gürtzig     2025-02-16      Bugfix #1192: Export instructions with (tail) return statements as exit structure
 *      Kay Gürtzig     2025-07-03      Bugfix #1195: disabled check unified (--> isDisabled(true))
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.Generator.TryCatchSupportLevel;
import lu.fisch.structorizer.parsers.CodeParser;

public class TexGenerator extends Generator {
	
	/** Conversion factor from pixels to millimeters (assuming 72 dpi) */
	private final double PIXEL_TO_MM = 25.4 / 72.0;
	/** Mirror of Element.E_PADDING */
	private final int E_PADDING = 20;
	
	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export StrukTeX Code ...";
	}
	
	protected String getFileDescription()
	{
		return "StrukTeX Source Code";
	}
	
	protected String getIndent()
	{
		return "  ";
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"tex"};
		return exts;
	}
	
	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft()
	{
		return "%";
	}
	// END KGU 2015-10-18
	
	// START KGU#78 2015-12-18: Enh. #23 - Irrelevant here (?) but necessary now
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean breakMatchesCase()
	{
		return true;
	}
	// END KGU#78 2015-12-18

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return "\\usepackage{%}";
	}
	// END KGU#351 2017-02-26

	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	@Override
	protected OverloadingLevel getOverloadingLevel() {
		// Simply pass the stuff as is...
		return OverloadingLevel.OL_DEFAULT_ARGUMENTS;
	}
	// END KGU#371 2019-03-07

	// START KGU#686 2019-03-18: Enh. #56
	/**
	 * Subclassable method to specify the degree of availability of a try-catch-finally
	 * construction in the target language.
	 * @return a {@link TryCatchSupportLevel} value
	 */
	protected TryCatchSupportLevel getTryCatchLevel()
	{
		return TryCatchSupportLevel.TC_NO_TRY;
	}
	// END KGU#686 2019-03-18

	/************ Code Generation **************/
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	@Override
	protected String getInputReplacer(boolean withPrompt)
	{
		// Will not be used
		return "scanf(\"\", &$1);";
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer()
	{
		// Will not be used
		return "printf(\"\\n\");";
	}

	// START KGU#483 2017-12-30: Bugfix #497 - we need a more sophisticated structure here
	///**
	// * Transforms assignments in the given intermediate-language code line.
	// * Replaces "<-" by "\gets" here
	// * @param _interm - a code line in intermediate syntax
	// * @return transformed string
	// */
	//protected String transformAssignment(String _interm)
	//{
	//	return _interm.replace("<-", "\\gets");
	//}
	/** Temporary list of virtual Roots created for complex threads in Parallel elements */
	private LinkedList<Root> tasks = new LinkedList<Root>();
	private int taskNo = 0;
	/**
	 * Transforms operators and other tokens from the given intermediate
	 * language into tokens of the target language and returns the result
	 * as string.<br/>
	 * This method is called by {@link #transform(String, boolean)} but may
	 * also be used elsewhere for a specific token list.
	 * @see #transform(String, boolean)
	 * @see #transformInput(String)
	 * @see #transformOutput(String)
	 * @see #transformType(String, String)
	 * @see #suppressTransformation
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed string
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		// START KGU#920 2021-02-03: Issue #920 Handle Infinity literal
		tokens.replaceAll("Infinity", "\\infty");
		// END KGU#920 2021-02-03
		tokens.replaceAll("{", "\\{");
		tokens.replaceAll("}", "\\}");
		tokens.replaceAll("%", "\\bmod");
		tokens.replaceAll("&&", "\\wedge");
		tokens.replaceAll("||", "\\vee");
		tokens.replaceAll("==", "=");
		tokens.replaceAll("!=", "\\neq");
		tokens.replaceAll("<=", "\\leq");
		tokens.replaceAll(">=", "\\geq");
		// START KGU#974 2021-05-12: Bugfix #975 Precaution against further critical characters
		tokens.replaceAll("\\", "\\backslash{}");
		tokens.replaceAll("~", "\\~{}");
		tokens.replaceAll("&", "\\&");
		tokens.replaceAll("^", "\\^{}");
		// END KGU#974 2021-05-12
		String[] keywords = CodeParser.getAllProperties();
		HashSet<String> keys = new HashSet<String>(keywords.length);
		for (String keyword: keywords) {
			keys.add(keyword);
		}
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			int len = token.length();
			if (token.equals("<-") || token.equals(":=")) {
				token = "\\gets";
				if (i+1 < tokens.count() && !tokens.get(i+1).trim().isEmpty()) {
					token += " ";
				}
				tokens.set(i,  token);
			}
			// Cut strings out of inline math mode and disarm quotes
			// START KGU#974 2021-05-12: Bugfix #975 we must disarm backslashes as well
			//else if (len >= 2 && token.charAt(0) == '"' && token.charAt(len-1) == '"') {
			//	tokens.set(i, "\\)" + token.replace("\"", "\"{}").replace("'", "'{}").replace("^", "\\^{}") + "\\(");
			//}
			//else if (len >= 2 && token.charAt(0) == '\'' && token.charAt(len-1) == '\'') {
			//	tokens.set(i, "\\)" + token.replace("\"", "\"{}").replace("'", "'{}").replace("^", "\\^{}") + "\\(");
			//}
			else if (len >= 2
					&& (token.charAt(0) == '"' && token.charAt(len-1) == '"' 
					|| token.charAt(0) == '\'' && token.charAt(len-1) == '\'')) {
				tokens.set(i, "\\)"
					// Replacements within string literals
					+ transformStringContent(token)
					// There may be more symbols to be escaped...
					+ "\\(");
			}
			// END KGU#974 2021-05-12
			else if (keys.contains(token)) {
				tokens.set(i, "\\)\\pKey{" + token + "}\\(");
			}
			else if (token.contains("^")) {
				tokens.set(i, token.replace("^", "\\textasciicircum{}"));
			}
		}
		return tokens.concatenate();
	}
	// END KGU#483 2017-12-30
	// END KGU#18/KGU#23 2015-11-01

	protected String transform(String _input)
	{
		// das Zuweisungssymbol
		//_input=BString.replace(_input,"<-","\\gets");
		// START KGU#483 2017-12-30: Bugfix #497 - now done by transformTokens()
		//_input = transformAssignment(_input);
		_input = super.transform(_input, false);
		_input = _input.replace("_", "\\_");
		// END KGU#483 2017-12-30
		
		// Leerzeichen
		_input = _input.replace(" ","\\ ");
		
		// Umlaute (UTF-8 -> LaTeX)
		_input = _input.replace("\u00F6","\"o");
		_input = _input.replace("\u00D6","\"O");
		_input = _input.replace("\u00E4","\"a");
		_input = _input.replace("\u00C4","\"A");
		_input = _input.replace("\u00FC","\"u");
		_input = _input.replace("\u00DC","\"U");
		_input = _input.replace("\u00E9","\"e");
		_input = _input.replace("\u00CB","\"E");
		
		// scharfes "S"
		_input = _input.replace("\u00DF","\\ss{}");

		return _input;
	}

	/**
	 * Transforms mere text (or string literal contents) to LaTeX syntax
	 * @param text - the source string
	 * @return the LaTeX-compatible text
	 */
	private String transformStringContent(String text)
	{
		return text.replace("{", "‽{")
		.replace("}", "‽}")
		.replace("\\", "\\textbackslash{}")
		.replace("|", "\\textbar{}")
		.replace("\"", "\"{}")
		.replace("'", "'{}")
		.replace("´", "\\textasciiacute{}")
		.replace("`", "\\textasciigrave{}")
		.replace("^", "\\textasciicircum{}")
		.replace("~", "\\textasciitilde{}")
		.replace("<", "\\textless")
		.replace(">", "\\textgreater")
		.replace("&", "\\&")
		.replace("#", "\\#")
		.replace("°", "\\textdegree")
		.replace("%", "\\%")
		.replace("$", "\\$")
		.replace("‽{", "\\{")
		.replace("‽}", "\\}");
	}

	private String transformText(String _input)
	{
		_input = transformStringContent(_input);
		_input = _input
				// Escape underscores and blanks
				.replace("_", "\\_")
				// Special German characters (UTF-8 -> LaTeX)
				.replace("\u00F6","\"o")
				.replace("\u00D6","\"O")
				.replace("\u00E4","\"a")
				.replace("\u00C4","\"A")
				.replace("\u00FC","\"u")
				.replace("\u00DC","\"U")
				.replace("\u00E9","\"e")
				.replace("\u00CB","\"E")
				.replace("\u00DF","\\ss{}");
		return _input;
	}
	
	@Override
	protected void generateCode(Instruction _inst, String _indent)
	{
		if (!_inst.isDisabled(true)) {
			StringList lines = _inst.getUnbrokenText();
			for (int i=0; i<lines.count(); i++)
			{
				// START KGU#483 2017-12-30: Enh. #497
				//code.add(_indent+"\\assign{\\("+transform(lines.get(i))+"\\)}");
				String line = lines.get(i);
				if (Instruction.isTypeDefinition(line)) {
					code.add(_indent+"\\assign{%");
					code.add(_indent+this.getIndent() + "\\begin{declaration}[type:]");
					// get the type name
					StringList tokens = Element.splitLexically(line, true);
					tokens.removeAll(" ");
					String typeName = tokens.get(1);
					code.add(_indent+this.getIndent()+this.getIndent() + "\\description{" + typeName + "}{"
							+ transform(tokens.concatenate(" ", 3)) + "}");
					code.add(_indent+this.getIndent() + "\\end{declaration}");
					code.add(_indent + "}");
				}
				// START KGU#1177 2025-02-16: Bugfix #1192: display return statement as exit
				else if (Jump.isReturn(line)) {
					code.add(_indent+ "\\exit{\\("+transform(line)+"\\)}");
				}
				// END KGU#1177 2025-02-16
				else if (!Instruction.isAssignment(line) && Instruction.isDeclaration(line)) {
					code.add(_indent+"\\assign{%");
					code.add(_indent+this.getIndent() + "\\begin{declaration}[variable:]");
					// get the variable name
					StringList tokens = Element.splitLexically(line + "<-", true);
					tokens.removeAll(" ");
					String varName = Instruction.getAssignedVarname(tokens, false);
					code.add(_indent+this.getIndent()+this.getIndent() + "\\description{" + varName + "}{"
							+ transform(line) + "}");
					code.add(_indent+this.getIndent() + "\\end{declaration}");
					code.add(_indent + "}");
				}
				else {
					code.add(_indent+"\\assign{\\("+transform(lines.get(i))+"\\)}");
				}
				// END KGU#483 2017-12-30
			}
		}
	}

	@Override
	protected void generateCode(Alternative _alt, String _indent)
	{
/*
		s.add(makeIndent(_indent)+' \ifthenelse{'+inttostr(max(1,8-2*self.text.Count))+'}{'+inttostr(max(1,8-2*self.text.Count))+'}{\('+ss+'\)}{'+NSDUtils.txtTrue+'}{'+NSDUtils.txtFalse+'}');
		s.AddStrings(qTrue.getTeX(_indent+E_INDENT));
		if (qFalse.children.Count>0) then
			begin
			s.add(makeIndent(_indent)+'\change');
		s.AddStrings(qFalse.getTeX(_indent+E_INDENT));
		end; 
		s.add(makeIndent(_indent)+'\ifend');
*/
		
		if (!_alt.isDisabled(true)) {
			// START KGU#453 2017-11-02: Issue #447 - line continuation support was inconsistent
			//code.add(_indent + "\\ifthenelse{"+Math.max(1, 8-2*_alt.getText().count()) + "}{" + Math.max(1, 8-2*_alt.getText().count()) + "}{\\(" + transform(_alt.getUnbrokenText().getLongString()) + "\\)}{" + Element.preAltT + "}{" + Element.preAltF + "}");
			String indentPlus1 = _indent + this.getIndent();
			StringList condLines = _alt.getCuteText();
			int nCondLines = condLines.count();
			int gradient = Math.max(1, 8 - 2 * nCondLines);
			// START KGU#483 2017-12-31: Issue #497 - this was too simple and inadequate - we should estimate the actual width ratio
			//code.add(_indent + "\\ifthenelse{" + gradient + "}{" + gradient + "}{\\(" + transform(condLines.getLongString()) + "\\)}{" + Element.preAltT + "}{" + Element.preAltF + "}");
			// START KGU#881 2020-10-19: Bugfix #877 - the new mechanism fails in batch mode as it is based on drawing
			if (_alt.getRect().right == 0) {
				code.add(_indent + "\\ifthenelse{" + gradient + "}{" + gradient + "}{\\("
						+ transform(condLines.getLongString()) + "\\)}{"
						+ Element.preAltT + "}{" + Element.preAltF + "}");
			}
			else {
			// END KGU#881 2020-10-19
				// This works only in interactive mode when elements have been drawn
				int depth = Element.getNestingDepth(_alt);
				gradient = Math.max(1, gradient / Math.max(1, depth));
				int lWidth = _alt.qTrue.getRect().getRectangle().width;
				int rWidth = _alt.qFalse.getRect().getRectangle().width;
				int lRatio = Math.max(1, 2 * gradient * lWidth / (lWidth + rWidth));
				int rRatio = 2 * gradient - lRatio;
				if (gradient == 6 || depth > 2) {
					code.add(_indent + "% Reduce the ratio arguments or insert an optional height argument if the head got too flat, e.g.: \\ifthenelse{3}{3}... or \\ifthenelse[10]{" + lRatio + "}{" + rRatio +"}...");
				}
				code.add(_indent + "\\ifthenelse{" + lRatio + "}{" + rRatio + "}{\\(" + transform(condLines.getLongString()) + "\\)}{" + Element.preAltT + "}{" + Element.preAltF + "}");
			// START KGU#881 2020-10-19: Bugfix #877
			}
			// END KGU#881 2020-10-19
			// END KGU#483 2017-12-31
			// END KGU#453 2017-11-02
			generateCode(_alt.qTrue, indentPlus1);
			if(_alt.qFalse.getSize() > 0)
			{
				code.add(_indent+"\\change");
				generateCode(_alt.qFalse, indentPlus1);
			}
			else
			{
				code.add(_indent + "\\change");
			}
			code.add(_indent + "\\ifend");
		}
	}

	@Override
	protected void generateCode(Case _case, String _indent)
	{
		if (!_case.isDisabled(true)) {
			// START KGU#483 2017-12-31: Issue #497 - we need a trick to activate the default branch
			//code.add(_indent+"\\case{6}{"+_case.qs.size()+"}{\\("+transform(_case.getText().get(0))+"\\)}{"+transform(_case.getText().get(1))+"}");
			String indentPlus1 = _indent + this.getIndent();
			String indentPlus2 = indentPlus1 + this.getIndent();
			StringList caseText = _case.getUnbrokenText();
			int nBranches = _case.qs.size();
			boolean hasDefaultBranch = !caseText.get(nBranches).trim().equals("%");
			String macro = "\\case{6}{"+nBranches+"}";
			if (hasDefaultBranch) {
				macro = "\\case["+(nBranches * 5)+"]{5}{"+nBranches+"}";
			}
			// The first branch is integrated in the macro
			code.add(_indent + macro + "{\\("+transform(caseText.get(0))+"\\)}{"+transform(caseText.get(1))+"}");
			// END KGU#483 2017-12-31
			generateCode((Subqueue) _case.qs.get(0), indentPlus2);
			// The further branches have to be added witch switch clauses
			for(int i=1; i < nBranches-1; i++)
			{
				code.add(indentPlus1 + "\\switch{" + transform(caseText.get(i+1).trim()) + "}");
				generateCode((Subqueue) _case.qs.get(i), indentPlus2);
			}

			if (hasDefaultBranch)
			{
				// This selector is to be right-aligned (therefore the "[r]" argument)
				code.add(indentPlus1 + "\\switch[r]{" + transform(caseText.get(nBranches).trim()) + "}");
				generateCode((Subqueue) _case.qs.get(nBranches-1), indentPlus2);
			}
			code.add(_indent+_indent.substring(0,1)+"\\caseend");
		}
	}

	@Override
	protected void generateCode(For _for, String _indent)
	{
		if (!_for.isDisabled(true)) {
			// START KGU#483 2017-12-30: Bugfix #497 - we should at least mark the keywords
			//code.add(_indent + "\\while{\\(" + transform(_for.getUnbrokenText().getLongString()) + "\\)}");
			String content = "";
			if (_for.isForInLoop()) {
				StringList items = this.extractForInListItems(_for);
				String valueList = "";
				if (items == null) {
					valueList = _for.getValueList();
				}
				else {
					valueList = items.concatenate(", ");
					if (items.count() != 1 || !isStringLiteral(items.get(0)) && !Function.testIdentifier(items.get(0), false, null)) {
						valueList = "\\{" + transform(valueList) + "\\}";
					}
					else {
						valueList = "\\ " + transform(valueList);
					}
				}
				content = "\\(\\forall " + transform(_for.getCounterVar()) +
						"\\in " + valueList + "\\)";				
			}
			else if (_for.style == For.ForLoopStyle.COUNTER) {
				content = "\\pKey{" + CodeParser.getKeyword("preFor") + "}\\(" +
						transform(_for.getCounterVar()) +
						"\\ \\gets\\ " +
						transform(_for.getStartValue()) +
						"\\)\\pKey{" + CodeParser.getKeyword("postFor") + "}\\(" +
						transform(_for.getEndValue()) + "\\)";
				if (_for.getStepConst() != 1) {
					content += "\\pKey{" + CodeParser.getKeyword("stepFor") + "}\\(" +
							transform(_for.getStepString()) + "\\)";
				}
			}
			else {
				content = "\\(" + transform(_for.getUnbrokenText().getLongString()) + "\\)";
			}
			code.add(_indent + "\\while{" + content + "}");
			// END KGU#483 2017-12-30
			generateCode(_for.getBody(), _indent + this.getIndent());
			code.add(_indent + "\\whileend");
		}
	}
	
	/** tests whether the given value list item {@code _item} is a string literal */
	private boolean isStringLiteral(String _item) {
		return _item.length() >= 2 && _item.charAt(0) == '"' && _item.charAt(_item.length()-1) == '"';
	}

	@Override
	protected void generateCode(While _while, String _indent)
	{
		if (!_while.isDisabled(true)) {
			code.add(_indent + "\\while{\\(" + transform(_while.getUnbrokenText().getLongString()) + "\\)}");
			generateCode(_while.getBody(), _indent + this.getIndent());
			code.add(_indent + "\\whileend");
		}
	}
	
	@Override
	protected void generateCode(Repeat _repeat, String _indent)
	{
		if (!_repeat.isDisabled(true)) {
			code.add(_indent + "\\until{\\(" + transform(_repeat.getUnbrokenText().getLongString()) + "\\)}");
			generateCode(_repeat.getBody(), _indent + this.getIndent());
			code.add(_indent + "\\untilend");
		}
	}
	
	@Override
	protected void generateCode(Forever _forever, String _indent)
	{
		if (!_forever.isDisabled(true)) {
			code.add(_indent+"\\forever");
			generateCode(_forever.getBody(), _indent + this.getIndent());
			code.add(_indent+"\\foreverend");
		}
	}

	protected void generateCode(Call _call, String _indent)
	{
		if (!_call.isDisabled(false)) {
			StringList lines = _call.getUnbrokenText();
			for (int i = 0; !_call.isDisabled(true) && i < lines.count(); i++)
			{
				// START KGU#2 2015-12-19: Wrong command, should be \sub
				//code.add(_indent+"\\assign{\\("+transform(_call.getText().get(i))+"\\)}");
				code.add(_indent + "\\sub{\\("+transform(lines.get(i))+"\\)}");
				// END KGU#2 2015-12-19
			}
		}
	}
	
	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		if (!_jump.isDisabled(true)) {
			StringList lines = _jump.getUnbrokenText();
			// START KGU#78 2015-12-19: Enh. #23: We now distinguish exit and return boxes
			//code.add(_indent+"\\assign{\\("+transform(_jump.getText().get(i))+"\\)}");
			if (lines.count() == 0 || lines.getText().trim().isEmpty())
			{
				// START KGU#483 2017-12-30: Bugfix #497 - should contain a keyword
				//code.add(_indent+ "\\exit{}");
				code.add(_indent+ "\\exit{\\(" + transform(CodeParser.getKeywordOrDefault("preLeave", "leave")) +"\\)}");
				// END KGU#483 2017-12-30
			}
			else
				// END KGU#78 2015-12-19
			{
				//String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
				for(int i=0; i<lines.count(); i++)
				{
					// START KGU#78 2015-12-19: Enh. #23: We now distinguish exit and return boxes
					//code.add(_indent+"\\assign{\\("+transform(_jump.getText().get(i))+"\\)}");
					String line = lines.get(i);
					String command = "exit";	// Just the default
					String padding = "";	// StrukTeX handles the text orientation differently in exit and return macros
					// START KGU#483 2017-12-31: Issue #497 - distinction between return and exit repaired but then withdrawn
					// (is not compatible with DIN 66261 and indentation needs a workaround too...)
					//if (line.startsWith(preReturn))
					//{
					//	command = "return";
					//	padding = "    ";	// The return macro does no left padding in contrast to the exit macro
					//}
					// END KGU#483 2017-12-31
					code.add(_indent+ "\\" + command + "{\\("+transform(padding + line)+"\\)}");
					// END KGU#78 2015-12-19
				}
			}
		}
	}
	
	// START KGU#47 2015-12-19: Hadn't been generated at all - Trouble is: structure must not be recursive!
	@Override
	protected void generateCode(Parallel _para, String _indent)
	{
		// Ignore it if there are no threads or if the element is disabled
		if (!_para.qs.isEmpty() && !_para.isDisabled(true))
		{
			// Since substructure is not allowed (at least a call would have been sensible!),
			// we transfer all non-atomic threads into virtual Roots
			code.add(_indent + "\\inparallel{" + _para.qs.size() + "} {" +
					// START KGU#483 2017-12-30: Bugfix 497
					//transform(_para.qs.get(0).getFullText(false).getLongString()) + "}");
					makeTaskDescr(_para.qs.get(0)) + "}");
					// END KGU#483 2017-12-30
			for (int q = 1; q < _para.qs.size(); q++)
			{
				// START KGU#483 2017-12-30: Bugfix 497
				//code.add(_indent + "\\task{" + transform(_para.qs.get(q).getFullText(false).getLongString()) + "}");
				code.add(_indent + "\\task{" + makeTaskDescr(_para.qs.get(q)) + "}");
				// END KGU#483 2017-12-30
			}
			code.add(_indent + "\\inparallelend");		
		}
	}
	
	// START KGU#483 2017-12-30: Enh. #497
	/**
	 * Returns a single-line description for the task represented by {@link Subqueue} {@code _sq}
	 * and creates a virtual {@link Root} with its content if not atomic to be exported afterwards
	 * as separate structogram. 
	 * @param _sq - the Element (sequence) of the task
	 * @return the describing string to be put into the {@code inparallel} macro.
	 */
	private String makeTaskDescr(Subqueue _sq) {
		Element el;
		if (_sq.getSize() == 1 && (el = _sq.getElement(0)) instanceof Instruction && el.getUnbrokenText().count() == 1) {
			return transform(el.getUnbrokenText().get(0));
		}
		String name = "Task" + ++this.taskNo;
		Root task = new Root();
		task.setText(name);
		for (int i = 0; i < _sq.getSize(); i++) {
			task.children.addElement(_sq.getElement(i).copy());
		}
		task.width = _sq.getRect().getRectangle().width;
		task.height = _sq.getRect().getRectangle().height;
		this.tasks.addLast(task);
		return name;
	}
	// END KGU#483 2017-12-19

	// END KGU#47 2015-12-19
	
//	protected void generateCode(Subqueue _subqueue, String _indent)
//	{
//		for(int i=0;i<_subqueue.getSize();i++)
//		{
//			generateCode((Element) _subqueue.getElement(i),_indent);
//		}
//	}
	
	public String generateCode(Root _root, String _indent, boolean _public)
	{
		/*
		s.add(makeIndent(_indent)+'\begin{struktogramm}('+inttostr(round(self.height/72*25.4))+','+inttostr(round(self.width/72*25.4))+')['+ss+']');
		s.AddStrings(children.getTeX(_indent+E_INDENT));
		s.add(makeIndent(_indent)+'\end{struktogramm}');
		*/

		// START KGU#705 2019-09-23: Enh. #738
		int line0 = code.count();
		if (codeMap!= null) {
			// register the triple of start line no, end line no, and indentation depth
			// (tab chars count as 1 char for the text positioning!)
			codeMap.put(_root, new int[]{line0, line0, _indent.length()});
		}
		// END KGU#705 2019-09-23

		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
		// END KGU#178 2016-07-20
			code.add("\\documentclass[a4paper,10pt]{article}");
			code.add("");
			code.add("\\usepackage{struktex}");
			code.add("\\usepackage{ngerman}");
			// START KGU#483 2017-12-31: Issue #497 - there might also be usepackage additions
			this.appendUserIncludes("");
			// END KGU#483 2017-12-31
			code.add("");
			// START KGU#483 2017-12-31: Issue #497
			//code.add("\\title{Structorizer StrukTeX Export}");
			File file = _root.getFile();
			code.add("\\title{Structorizer StrukTeX Export"
					+ (file != null ? " of " + transformText(file.getName()) : "") + "}");
			// END KGU#483 2017-12-31
			// START KGU#363 2017-05-16: Enh. #372
			//code.add("\\author{Structorizer "+Element.E_VERSION+"}");
			if (this.optionExportLicenseInfo()) {
				code.add("% Structorizer version " + Element.E_VERSION);
				code.add("\\author{" + transformText(_root.getAuthor()) + "}");
			} else {
				code.add("\\author{Structorizer " + Element.E_VERSION + "}");
			}
			// END KGU#363 2017-05-16
			// START KGU#483 2017-12-31: Issue #497
			code.add("\\date{" + DateFormat.getDateInstance().format(new Date()) + "}");
			// END KGU#483 2017-12-31
			code.add("");
			code.add("\\begin{document}");
		// START KGU#178 2016-07-20: Enh. #160
			subroutineInsertionLine = code.count();
		}
		// END KGU#178 2016-07-20
		code.add("");
		// START KGU#483 2017-12-30: Bugfix #497 - we must escape underscores in the name
		//code.add("\\begin{struktogramm}("+Math.round(_root.width/72.0*25.4)+","+Math.round(_root.height/75.0*25.4)+")["+transform(_root.getText().get(0))+"]");
		code.add("% TODO: Tune the width and height argument if necessary!");
		code.add("\\begin{struktogramm}("
				+ Math.round((_root.width - 2 * E_PADDING) * PIXEL_TO_MM) + ","
				+ Math.round(_root.height * PIXEL_TO_MM / 2) + ")["
				+ transformText(_root.getMethodName()) + "]");
		generateParameterDecl(_root);
		// END KGU#483 2017-12-30
		generateCode(_root.children, this.getIndent());
		code.add("\\end{struktogramm}");
		code.add("");
		// START KGU#483 2017-12-30: Bugfix #497
		while (!this.tasks.isEmpty()) {
			Root task = tasks.removeFirst();
			code.add("% TODO: Tune the width and height argument if necessary!");
			code.add("\\begin{struktogramm}(" + Math.round(task.width * PIXEL_TO_MM) + ","
					+ Math.round(task.height * PIXEL_TO_MM / 2) + ")["
					+transformText(task.getText().get(0)) + "]");
			generateCode(task.children, this.getIndent());
			code.add("\\end{struktogramm}");			
		}
		// END KGU#483 2017-12-30
		// START KGU#178 2016-07-20: Enh. #160
		//code.add("\\end{document}");
		if (topLevel)
		{
			// START KGU#483 2018-01-02: Enh. 389, issue #497
			if (this.optionExportSubroutines()) {
				while (!this.includedRoots.isEmpty()) {
					Root incl = this.includedRoots.remove();
					// START KGU#815/KGU#824 20202-03-18: Enh. #828, bugfix #836
					//if (incl != _root) {
					if (incl != _root && (importedLibRoots == null || !importedLibRoots.contains(incl))) {
					// END KGU#815/KGU#824 2020-03-18
						this.appendDefinitions(incl, _indent, null, true);
					}
				}
			}
			// END KGU#483
			
			// START KGU#815 2020-04-03: Enh. #828 group export
			this.libraryInsertionLine = code.count();
			addSepaLine();
			// END KGU#815 2020-04-03
			
			code.add("\\end{document}");
		}
		// END KGU#178 2016-07-20
		
		// START KGU#705 2019-09-23: Enh. #738
		if (codeMap != null) {
			// Update the end line no relative to the start line no
			codeMap.get(_root)[1] += (code.count() - line0);
		}
		// END KGU#705 2019-09-23

		return code.getText();
	}

	// START KGU#483 2017-12-30: Bugfix #497
	/**
	 * Creates a dummy element declaring all arguments in case of a function diagram
	 * @param _root
	 */
	private void generateParameterDecl(Root _root) {
		boolean hasIncludes = _root.includeList != null && _root.includeList.count() > 0;
		if (_root.isSubroutine() || hasIncludes) {
			String indent1 = this.getIndent();
			String indent2 = indent1 + indent1;
			String indent3 = indent2 + indent1;
			String resType = _root.getResultType();
			ArrayList<Param> params = _root.getParams();
			if (!params.isEmpty() || resType != null || hasIncludes) {
				code.add(indent1 + "\\assign{%");
				if (!params.isEmpty()) {
					code.add(indent2 + "\\begin{declaration}[Parameters:]");
					for (Param param: params) {
						code.add(indent3 + "\\description{\\pVar{"+transform(param.getName())+
								"}}{type: \\("+ transform(param.getType(true)) +"\\)}");
					}
					code.add(indent2 + "\\end{declaration}");
				}
				if (resType != null) {
					code.add(indent2 + "\\begin{declaration}[Result type:]");
					code.add(indent3 + "\\description{" + resType + "}{}");
					code.add(indent2 + "\\end{declaration}");
				}
				if (hasIncludes) {
					code.add(indent2 + "\\begin{declaration}[Requires:]");
					code.add(indent3 + "\\description{" + _root.includeList.concatenate(", ") + "}{}");
					code.add(indent2 + "\\end{declaration}");					
				}
				code.add(this.getIndent() + "}");
			}
		}
	}
	// END KGU#483 2017-12-30

	// START KGU#483 2018-01-02: Enh. #389 + issue #497
	protected void appendDefinitions(Root _root, String _indent, StringList _varNames, boolean _force) {
		// Just generate the entire diagram...
		boolean wasTopLevel = topLevel;
		try {
			topLevel = false;
			generateCode(_root, _indent, false);
		}
		finally {
			topLevel = wasTopLevel;
		}
	}
	// END KGU#483 2018-01-02

	// START KGU#815 2020-04-03: Enh. #828
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#generatesClass()
	 */
	@Override
	protected boolean allowsMixedModule()
	{
		return true;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#max1MainPerModule()
	 */
	@Override
	protected boolean max1MainPerModule()
	{
		return false;
	}
	// END KGU#815 2020-04-03
	
}

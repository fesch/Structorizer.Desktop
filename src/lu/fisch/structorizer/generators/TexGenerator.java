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
 *      Bob Fisch       2007.12.27      First Issue
 *      Bob Fisch       2008.04.12      Added "Fields" section for generator to be used as plugin
 *      Kay Gürtzig     2015.10.18      Adaptations to updated Generator structure and interface
 *      Kay Gürtzig     2015.11.01      KGU#18/KGU#23 Transformation decomposed
 *      Kay Gürtzig     2015.12.18/19   KGU#2/KGU#47/KGU#78 Fixes for Call, Jump, and Parallel elements
 *      Kay Gürtzig     2016.07.20      Enh. #160 adapted (option to integrate subroutines = KGU#178)
 *      Kay Gürtzig     2016.09.25      Enh. #253: CodeParser.keywordMap refactoring done.
 *      Kay Gürtzig     2016.10.14      Enh. #270: Disabled elements are skipped here now
 *      Kay Gürtzig     2017.05.16      Enh. #372: Export of copyright information
 *      Kay Gürtzig     2017.12.30/31   Bugfix #497: Text export had been defective, Parallel export was useless
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.parsers.CodeParser;

public class TexGenerator extends Generator {
	
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
		return null;
	}
	// END KGU#351 2017-02-26

    /************ Code Generation **************/
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
    @Override
	protected String getInputReplacer(boolean withPrompt)
	{
    	// Will not be used
		return "scanf(\"\", &$1);";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
    @Override
	protected String getOutputReplacer()
	{
    	// Will not be used
		return "printf(\"\\n\", &$1);";
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
		tokens.replaceAll("{", "\\{");
		tokens.replaceAll("}", "\\}");
		tokens.replaceAll("%", "\\)\\pKey{mod}\\(");
		tokens.replaceAll("&&", "\\wedge");
		tokens.replaceAll("||", "\\vee");
		tokens.replaceAll("==", "=");
		tokens.replaceAll("!=", "\\neq");
		tokens.replaceAll("<=", "\\leq");
		tokens.replaceAll(">=", "\\geq");
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
			// Cut strings out of math mode and disarm quotes
			else if (len >= 2 && token.charAt(0) == '"' && token.charAt(len-1) == '"') {
				tokens.set(i, "\\)" + token.replace("\"", "\"{}").replace("'", "'{}").replace("^", "\\^{}") + "\\(");
			}
			else if (len >= 2 && token.charAt(0) == '\'' && token.charAt(len-1) == '\'') {
				tokens.set(i, "\\)" + token.replace("\"", "\"{}").replace("'", "'{}").replace("^", "\\^{}") + "\\(");
			}
			else if (keys.contains(token)) {
				tokens.set(i, "\\)\\pKey{" + token + "}\\(");
			}
			else if (token.contains("^")) {
				tokens.set(i, token.replace("^", "\\hat{}"));
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
	
    @Override
	protected void generateCode(Instruction _inst, String _indent)
	{
    	if (!_inst.disabled) {
    		StringList lines = _inst.getUnbrokenText();
    		for (int i=0; i<lines.count(); i++)
    		{
    			// START KGU#483 2017-12-30: Enh. #497
    			//code.add(_indent+"\\assign{\\("+transform(lines.get(i))+"\\)}");
    			String line = lines.get(i);
    			
    			if (!Instruction.isAssignment(line) && Instruction.isDeclaration(line)) {
    				code.add(_indent+"\\assign{%");
    				code.add(_indent+this.getIndent() + "\\begin{declaration}[variable:]");
    				// get the variable name
    				StringList tokens = Element.splitLexically(line + "<-", true);
    				tokens.removeAll(" ");
    				String varName = _inst.getAssignedVarname(tokens);
    				code.add(_indent+this.getIndent()+this.getIndent() + "\\description{" + varName + "}{"
    						+ transform(line) + "}");
    				code.add(_indent+this.getIndent() + "\\begin{declaration}");
    				
    			}
    			code.add(_indent+"\\assign{\\("+transform(lines.get(i))+"\\)}");
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
		
    	if (!_alt.disabled) {
    		// START KGU#453 2017-11-02: Issue #447 - line continuation support was inconsistent
    		//code.add(_indent + "\\ifthenelse{"+Math.max(1, 8-2*_alt.getText().count()) + "}{" + Math.max(1, 8-2*_alt.getText().count()) + "}{\\(" + transform(_alt.getUnbrokenText().getLongString()) + "\\)}{" + Element.preAltT + "}{" + Element.preAltF + "}");
    		StringList condLines = _alt.getCuteText();
    		int nCondLines = condLines.count();
    		int gradient = Math.max(1, 8 - 2 * nCondLines);
    		code.add(_indent + "\\ifthenelse{" + gradient + "}{" + gradient + "}{\\(" + transform(condLines.getLongString()) + "\\)}{" + Element.preAltT + "}{" + Element.preAltF + "}");
    		// END KGU#453 2017-11-02
    		generateCode(_alt.qTrue,_indent+_indent.substring(0,1));
    		if(_alt.qFalse.getSize() > 0)
    		{
    			code.add(_indent+"\\change");
    			generateCode(_alt.qFalse,_indent+_indent.substring(0,1));
    		}
    		else
    		{
    			code.add(_indent+"\\change");
    		}
    		code.add(_indent+"\\ifend");
    	}
	}
	
    @Override
	protected void generateCode(Case _case, String _indent)
	{
    	if (!_case.disabled) {
    		code.add(_indent+"\\case{6}{"+_case.qs.size()+"}{\\("+transform(_case.getText().get(0))+"\\)}{"+transform(_case.getText().get(1))+"}");
    		generateCode((Subqueue) _case.qs.get(0),_indent+_indent.substring(0,1)+_indent.substring(0,1)+_indent.substring(0,1));

    		for(int i=1;i<_case.qs.size()-1;i++)
    		{
    			code.add(_indent+_indent.substring(0,1)+"\\switch{"+transform(_case.getText().get(i+1).trim())+"}");
    			generateCode((Subqueue) _case.qs.get(i),_indent+_indent.substring(0,1)+_indent.substring(0,1)+_indent.substring(0,1));
    		}

    		if(!_case.getText().get(_case.qs.size()).trim().equals("%"))
    		{
    			code.add(_indent+_indent.substring(0,1)+"\\switch[r]{"+transform(_case.getText().get(_case.qs.size()).trim())+"}");
    			generateCode((Subqueue) _case.qs.get(_case.qs.size()-1),_indent+_indent.substring(0,1)+_indent.substring(0,1));
    		}
    		code.add(_indent+_indent.substring(0,1)+"\\caseend");
    	}
	}
	
	protected void generateCode(For _for, String _indent)
	{
		if (!_for.disabled) {
			// START KGU#483 2017-12-30: Bugfix #497 - we should at least mark the keywords
			//code.add(_indent + "\\while{\\(" + transform(_for.getUnbrokenText().getLongString()) + "\\)}");
			String content = "";
			if (_for.isForInLoop()) {
				content = "\\(\\forall " + transform(_for.getCounterVar()) +
						"\\in " + transform(_for.getValueList()) + "\\)";				
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
			generateCode(_for.q, _indent + _indent.substring(0,1));
			code.add(_indent + "\\whileend");
		}
	}
	
	protected void generateCode(While _while, String _indent)
	{
		if (!_while.disabled) {
			code.add(_indent + "\\while{\\(" + transform(_while.getUnbrokenText().getLongString()) + "\\)}");
			generateCode(_while.q, _indent + _indent.substring(0,1));
			code.add(_indent + "\\whileend");
		}
	}
	
	protected void generateCode(Repeat _repeat, String _indent)
	{
		if (!_repeat.disabled) {
			code.add(_indent + "\\until{\\(" + transform(_repeat.getUnbrokenText().getLongString()) + "\\)}");
			generateCode(_repeat.q, _indent + _indent.substring(0,1));
			code.add(_indent + "\\untilend");
		}
	}
	
	protected void generateCode(Forever _forever, String _indent)
	{
		if (!_forever.isDisabled()) {
			code.add(_indent+"\\forever");
			generateCode(_forever.q,_indent+_indent.substring(0,1));
			code.add(_indent+"\\foreverend");
		}
	}

	protected void generateCode(Call _call, String _indent)
	{
		StringList lines = _call.getUnbrokenText();
		for(int i=0; !_call.disabled && i<lines.count(); i++)
		{
			// START KGU#2 2015-12-19: Wrong command, should be \sub
			//code.add(_indent+"\\assign{\\("+transform(_call.getText().get(i))+"\\)}");
			code.add(_indent+"\\sub{\\("+transform(lines.get(i))+"\\)}");
			// END KGU#2 2015-12-19
		}
	}
	
	protected void generateCode(Jump _jump, String _indent)
	{
		if (!_jump.disabled) {
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
				String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
				for(int i=0; i<lines.count(); i++)
				{
					// START KGU#78 2015-12-19: Enh. #23: We now distinguish exit and return boxes
					//code.add(_indent+"\\assign{\\("+transform(_jump.getText().get(i))+"\\)}");
					String command = "exit";	// Just the default
					if (_indent.startsWith(preReturn))
					{
						command = "return";
					}
					code.add(_indent+ "\\" + command + "{\\("+transform(_jump.getText().get(i))+"\\)}");
					// END KGU#78 2015-12-19
				}
			}
		}
	}
	
	// START KGU#47 2015-12-19: Hadn't been generated at all - Trouble is: structure must not be recursive!
	protected void generateCode(Parallel _para, String _indent)
	{
		// Ignore it if there are no threads or if the element is disabled
		if (!_para.qs.isEmpty() && !_para.disabled)
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
		task.width = _sq.getRect().getRectangle().width + 40;
		task.height = _sq.getRect().getRectangle().height + 60;
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
	
	public String generateCode(Root _root, String _indent)
	{
		/*
		s.add(makeIndent(_indent)+'\begin{struktogramm}('+inttostr(round(self.height/72*25.4))+','+inttostr(round(self.width/72*25.4))+')['+ss+']');
		s.AddStrings(children.getTeX(_indent+E_INDENT));
		s.add(makeIndent(_indent)+'\end{struktogramm}');
		*/
		
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
		// END KGU#178 2016-07-20
			code.add("\\documentclass[a4paper,10pt]{article}");
			code.add("");
			code.add("\\usepackage{struktex}");
			code.add("\\usepackage{german}");
			code.add("");
			code.add("\\title{Structorizer StrukTeX Export}");
			// START KGU#363 2017-05-16: Enh. #372
			code.add("\\author{Structorizer "+Element.E_VERSION+"}");
			if (this.optionExportLicenseInfo()) {
				code.add("\\author{" + _root.getAuthor() + "}");
			} else {
				code.add("\\author{Structorizer "+Element.E_VERSION+"}");
			}
			// END KGU#363 2017-05-16
			code.add("");
			code.add("\\begin{document}");
		// START KGU#178 2016-07-20: Enh. #160
			subroutineInsertionLine = code.count();
		}
		// END KGU#178 2016-07-20
		code.add("");
		// START KGU#483 2017-12-30: Bugfix #497 - we must escape underscores in the name
		//code.add("\\begin{struktogramm}("+Math.round(_root.width/72.0*25.4)+","+Math.round(_root.height/75.0*25.4)+")["+transform(_root.getText().get(0))+"]");
		code.add("\\begin{struktogramm}("+Math.round(_root.width/72.0*25.4)+","+Math.round(_root.height/75.0*25.4)+")["+transform(_root.getMethodName())+"]");
		generateParameterDecl(_root);
		// END KGU#483 2017-12-30
		generateCode(_root.children, this.getIndent());
		code.add("\\end{struktogramm}");
		code.add("");
		// START KGU#483 2017-12-30: Bugfix #497
		while (!this.tasks.isEmpty()) {
			Root task = tasks.removeFirst();
			code.add("\\begin{struktogramm}("+Math.round(task.width/72.0*25.4)+","+Math.round(task.height/75.0*25.4)+")["+transform(task.getText().get(0))+"]");
			generateCode(task.children, this.getIndent());
			code.add("\\end{struktogramm}");			
		}
		// END KGU#483 2017-12-30
		// START KGU#178 2016-07-20: Enh. #160
		//code.add("\\end{document}");
		if (topLevel)
		{
			code.add("\\end{document}");
		}
		// END KGU#178 2016-07-20
		
		return code.getText();
	}

	// START KGU#483 2017-12-30: Bugfix #497
	/**
	 * Creates a dummy element declaring all arguments in case of a function diagram
	 * @param _root
	 */
	private void generateParameterDecl(Root _root) {
		if (_root.isSubroutine()) {
			String indent1 = this.getIndent();
			String indent2 = indent1 + indent1;
			String indent3 = indent2 + indent1;
			String resType = _root.getResultType();
			ArrayList<Param> params = _root.getParams();
			if (!params.isEmpty() || resType != null) {
				code.add(indent1 + "\\assign{%");
				code.add(indent2 + "\\begin{declaration}[Parameters:]");
				for (Param param: params) {
					code.add(indent3 + "\\description{\\pVar{"+transform(param.getName())+
							"}}{type: \\("+ transform(param.getType()) +"\\)}");
				}
				code.add(indent2 + "\\end{declaration}");
				if (resType != null) {
					code.add(indent2 + "\\begin{declaration}[Result type:]");
					code.add(indent3 + "\\description{" + resType + "}{}");
					code.add(indent2 + "\\end{declaration}");
				}
				code.add(this.getIndent() + "}");
			}
		}
	}
	// END KGU#483 2017-12-30

//	@Override - obsolete since 3.27
//	public String[] getReservedWords() {
//		return null;
//	}
//
//	@Override
//	public boolean isCaseSignificant() {
//		return false;
//	}
	
	
}

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
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.parsers.D7Parser;

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

    /************ Code Generation **************/
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "$1 = (new Scanner(System.in)).nextLine();"
	 */
    @Override
	protected String getInputReplacer()
	{
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
		return "printf(\"\\n\", &$1);";
	}

	/**
	 * Transforms assignments in the given intermediate-language code line.
	 * Replaces "<-" by "\gets" here
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed string
	 */
	protected String transformAssignment(String _interm)
	{
		return _interm.replace("<-", "\\gets");
	}
	// END KGU#18/KGU#23 2015-11-01

	protected String transform(String _input)
	{
		// das Zuweisungssymbol
		//_input=BString.replace(_input,"<-","\\gets");
		_input = transformAssignment(_input);
		
		// Leerzeichen
		_input=BString.replace(_input," ","\\ ");
		
		// Umlaute (UTF-8 -> LaTeX)
		_input=BString.replace(_input,"\u00F6","\"o");
		_input=BString.replace(_input,"\u00D6","\"O");
		_input=BString.replace(_input,"\u00E4","\"a");
		_input=BString.replace(_input,"\u00C4","\"A");
		_input=BString.replace(_input,"\u00FC","\"u");
		_input=BString.replace(_input,"\u00DC","\"U");
		_input=BString.replace(_input,"\u00E9","\"e");
		_input=BString.replace(_input,"\u00CB","\"E");
		
		// scharfes "S"
		_input=BString.replace(_input,"\u00DF","\\ss{}");

		return _input;
	}
	
    @Override
	protected void generateCode(Instruction _inst, String _indent)
	{
		for(int i=0;i<_inst.getText().count();i++)
		{
			code.add(_indent+"\\assign{\\("+transform(_inst.getText().get(i))+"\\)}");
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
		
		code.add(_indent+"\\ifthenelse{"+Math.max(1,8-2*_alt.getText().count())+"}{"+Math.max(1,8-2*_alt.getText().count())+"}{\\("+BString.replace(transform(_alt.getText().getText()),"\n","")+"\\)}{"+Element.preAltT+"}{"+Element.preAltF+"}");
		generateCode(_alt.qTrue,_indent+_indent.substring(0,1));
		if(_alt.qFalse.getSize()!=0)
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
	
    @Override
	protected void generateCode(Case _case, String _indent)
	{
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
	
	protected void generateCode(For _for, String _indent)
	{
		code.add(_indent+"\\while{\\("+BString.replace(transform(_for.getText().getText()),"\n","")+"\\)}");
		generateCode(_for.q,_indent+_indent.substring(0,1));
		code.add(_indent+"\\whileend");

/*		code.add(_indent+"\\forever");
		generateCode(_for.q,_indent+_indent.substring(0,1));
		code.add(_indent+"\\foreverend");*/
	}
	
	protected void generateCode(While _while, String _indent)
	{
		code.add(_indent+"\\while{\\("+BString.replace(transform(_while.getText().getText()),"\n","")+"\\)}");
		generateCode(_while.q,_indent+_indent.substring(0,1));
		code.add(_indent+"\\whileend");
	}
	
	protected void generateCode(Repeat _repeat, String _indent)
	{
		code.add(_indent+"\\until{\\("+BString.replace(transform(_repeat.getText().getText()),"\n","")+"\\)}");
		generateCode(_repeat.q,_indent+_indent.substring(0,1));
		code.add(_indent+"\\untilend");
	}
	
	protected void generateCode(Forever _forever, String _indent)
	{
		 code.add(_indent+"\\forever");
		 generateCode(_forever.q,_indent+_indent.substring(0,1));
		 code.add(_indent+"\\foreverend");
	}

	protected void generateCode(Call _call, String _indent)
	{
		for(int i=0;i<_call.getText().count();i++)
		{
			// START KGU#2 2015-12-19: Wrong command, should be \sub
			//code.add(_indent+"\\assign{\\("+transform(_call.getText().get(i))+"\\)}");
			code.add(_indent+"\\sub{\\("+transform(_call.getText().get(i))+"\\)}");
			// END KGU#2 2015-12-19
		}
	}
	
	protected void generateCode(Jump _jump, String _indent)
	{
		// START KGU#78 2015-12-19: Enh. #23: We now distinguish exit and return boxes
		//code.add(_indent+"\\assign{\\("+transform(_jump.getText().get(i))+"\\)}");
		if (_jump.getText().count() == 0 || _jump.getText().getText().trim().isEmpty())
		{
			code.add(_indent+ "\\exit{}");
		}
		else
		// END KGU#78 2015-12-19
			// FIXME (KGU 2015-12-19): This should not be split into several blocks
			for(int i=0; i<_jump.getText().count(); i++)
			{
				// START KGU#78 2015-12-19: Enh. #23: We now distinguish exit and return boxes
				//code.add(_indent+"\\assign{\\("+transform(_jump.getText().get(i))+"\\)}");
				String command = "exit";	// Just the default
				if (_indent.startsWith(D7Parser.preReturn))
				{
					command = "return";
				}
				code.add(_indent+ "\\" + command + "{\\("+transform(_jump.getText().get(i))+"\\)}");
				// END KGU#78 2015-12-19
			}
	}
	
	// START KGU#47 2015-12-19: Hadn't been generated at all - Trouble is: structure must not be recursive!
	protected void generateCode(Parallel _para, String _indent)
	{
		// Ignore it if there no threads
		if (!_para.qs.isEmpty())
		{
			// Since substructure is not allowed (at least a call would have been sensible!),
			// we transform all thread contents into single long strings...
			code.add(_indent + "\\inparallel{" + _para.qs.size() + "} {" + 
					transform(_para.qs.get(0).getFullText(false).getLongString()) + "}");
			for (int q = 1; q < _para.qs.size(); q++)
			{
				code.add(_indent + "\\task{" + transform(_para.qs.get(q).getFullText(false).getLongString()) + "}");
			}
			code.add(_indent + "\\inparallelend");		
		}
	}
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
		
		code.add("\\documentclass[a4paper,10pt]{article}");
		code.add("");
		code.add("\\usepackage{struktex}");
		code.add("\\usepackage{german}");
		code.add("");
		code.add("\\title{Structorizer StrukTeX Export}");
		code.add("\\author{Structorizer "+Element.E_VERSION+"}");
		code.add("");
		code.add("\\begin{document}");
		code.add("");
		code.add("\\begin{struktogramm}("+Math.round(_root.width/72.0*25.4)+","+Math.round(_root.height/75.0*25.4)+")["+transform(_root.getText().get(0))+"]");
		generateCode(_root.children, this.getIndent());
		code.add("\\end{struktogramm}");
		code.add("");
		code.add("\\end{document}");
		
		return code.getText();
	}
	
	
}

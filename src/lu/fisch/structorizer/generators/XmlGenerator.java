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
 *      Description:    This class generates XML code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.27      First Issue
 *      Bob Fisch       2008.04.12      Added "Fields" section for generator to be used as plugin
 *      Kay Gürtzig     2015.11.08      Additional information with FOR loops (KGU#3)
 *      Kay Gürtzig     2015.12.18      Formal adaptation to Enh. #23 (KGU#78) related to break mechanism
 *      Kay Gürtzig     2015.12.21      Formal adaptation to Bugfix #41/#68/#69 (KGU#93)
 *      Kay Gürtzig     2015.12.31      Bugfix #82 (KGU#118) Inconsistent FOR loops used to obstruct saving
 *      Kay Gürtzig     2016.01.08      Bugfix #99 (KGU#134) mends mis-spelling due to fix #82
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;

public class XmlGenerator extends Generator {

	// START KGU#118 2015-12-31: Support for bugfix #82
	// START KGU#134 2016-01-08: Bugfix #99: mis-spelled attribute name
	//private static String[] forLoopAttributes = {"counterVar", "StartValue", "endValue", "stepConst"};
	private static String[] forLoopAttributes = {"counterVar", "startValue", "endValue", "stepConst"};
	// END KGU#134 2016-01-08
	// END KGU#118 2015-12-31
	
	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "XML Code ...";
	}
	
	protected String getFileDescription()
	{
		return "XML Code";
	}
	
	protected String getIndent()
	{
		return "\t";
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"xml","nsd"};
		return exts;
	}
	
    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "<!--";
    }

    @Override
    protected String commentSymbolRight()
    {
    	return "-->";
    }
    // END KGU 2015-10-18
    
	// START KGU#78 2015-12-18: Enh. #23 - Irrelevant here but necessary now
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
	protected String getInputReplacer()
	{
		return "";
	}

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code
	 * @return a regex replacement pattern, e.g. "System.out.println($1);"
	 */
	protected String getOutputReplacer()
	{
		return "";
	}

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69 - no longer needed
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	protected String transformAssignment(String _interm)
//	{
//		return _interm;
//	}
	// END KGU#93 2015-12-21
	// END KGU#18/KGU#23 2015-11-01
    
    
    @Override
	protected void generateCode(Instruction _inst, String _indent)
	{
		String r = "0";
		//if(_inst.rotated==true) {r="1";}
		code.add(_indent+"<instruction text=\""+BString.encodeToHtml(_inst.getText().getCommaText())+"\" comment=\""+
												BString.encodeToHtml(_inst.getComment().getCommaText())+"\" color=\""+
												_inst.getHexColor()+"\" rotated=\""+r+"\"></instruction>");
	}
	
    @Override
	protected void generateCode(Alternative _alt, String _indent)
	{
		code.add(_indent+"<alternative text=\""+BString.encodeToHtml(_alt.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_alt.getComment().getCommaText())+"\" color=\""+
				 _alt.getHexColor()+"\">");
		code.add(_indent+this.getIndent()+"<qTrue>");
		generateCode(_alt.qTrue,_indent+this.getIndent()+this.getIndent());
		code.add(_indent+this.getIndent()+"</qTrue>");
		code.add(_indent+this.getIndent()+"<qFalse>");
		generateCode(_alt.qFalse,_indent+this.getIndent()+this.getIndent());
		code.add(_indent+this.getIndent()+"</qFalse>");
		code.add(_indent+"</alternative>");
	}
	
    @Override
	protected void generateCode(Case _case, String _indent)
	{
		code.add(_indent+"<case text=\""+BString.encodeToHtml(_case.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_case.getComment().getCommaText())+"\" color=\""+
				 _case.getHexColor()+"\">");
		for(int i=0;i<_case.qs.size();i++)
		{
			code.add(_indent+this.getIndent()+"<qCase>");
			generateCode((Subqueue) _case.qs.get(i),_indent+this.getIndent()+this.getIndent());
			code.add(_indent+this.getIndent()+"</qCase>");
		}
		code.add(_indent+"</case>");
	}

    @Override
    	protected void generateCode(Parallel _para, String _indent)
	{
		code.add(_indent+"<parallel text=\""+BString.encodeToHtml(_para.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_para.getComment().getCommaText())+"\" color=\""+
				 _para.getHexColor()+"\">");
		for(int i=0;i<_para.qs.size();i++)
		{
			code.add(_indent+this.getIndent()+"<qPara>");
			generateCode((Subqueue) _para.qs.get(i),_indent+this.getIndent()+this.getIndent());
			code.add(_indent+this.getIndent()+"</qPara>");
		}
		code.add(_indent+"</parallel>");
	}

    @Override
	protected void generateCode(For _for, String _indent)
	{
    	// START KGU#118 2015-12-31: Bugfix 82: "free-style" FOR loops used to obstruct saving
    	// We need some pre-processing to enhance robustness: If some of the specific fields
    	// cannot be retrieved then just omit them, they aren't strictly needed on loading.
//		code.add(_indent+"<for text=\""+BString.encodeToHtml(_for.getText().getCommaText()) +
//				"\" comment=\"" + BString.encodeToHtml(_for.getComment().getCommaText()) +
//				// START KGU#3 2015-10-28: Insert new dedicated information fields
//				"\" counterVar=\"" + BString.encodeToHtml(_for.getCounterVar()) +
//				"\" startValue=\"" + BString.encodeToHtml(_for.getStartValue()) +
//				"\" endValue=\"" + BString.encodeToHtml(_for.getEndValue()) +
//				"\" stepConst=\"" + BString.encodeToHtml(_for.getStepString()) +
//				// END KGU#3 2015-10-28
//				// START KGU#3 2015-11-08: The reliability of the structured fields must be stored, too.
//				"\" reliable=\"" + BString.encodeToHtml(_for.checkConsistency() ? "true" : "false") +
//				// END KGU#3 2015-11-08
//				"\" color=\"" + _for.getHexColor()+"\">");
    	String[] specificInfo = _for.splitForClause();
    	String specificAttributes = "";
    	for (int i = 0; i < forLoopAttributes.length; i++)
    	{
    		if (specificInfo[i] != null)
    		{
    			specificAttributes += "\" " + forLoopAttributes[i] + "=\"" + BString.encodeToHtml(specificInfo[i]);
    		}
    	}
    	code.add(_indent+"<for text=\""+BString.encodeToHtml(_for.getText().getCommaText()) +
    			"\" comment=\"" + BString.encodeToHtml(_for.getComment().getCommaText()) +
    			// START KGU#3 2015-10-28: Insert new dedicated information fields
    			specificAttributes +
    			// END KGU#3 2015-10-28
    			// START KGU#3 2015-11-08: The reliability of the structured fields must be stored, too.
    			"\" reliable=\"" + BString.encodeToHtml(_for.checkConsistency() ? "true" : "false") +
    			// END KGU#3 2015-11-08
    			"\" color=\"" + _for.getHexColor()+"\">");
    	// END KGU#118 2015-12-31
		code.add(_indent+this.getIndent()+"<qFor>");
		generateCode(_for.q,_indent+this.getIndent()+this.getIndent());
		code.add(_indent+this.getIndent()+"</qFor>");
		code.add(_indent+"</for>");
	}
	
    @Override
	protected void generateCode(While _while, String _indent)
	{
		code.add(_indent+"<while text=\""+BString.encodeToHtml(_while.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_while.getComment().getCommaText())+"\" color=\""+
				 _while.getHexColor()+"\">");
		code.add(_indent+this.getIndent()+"<qWhile>");
		generateCode(_while.q,_indent+this.getIndent()+this.getIndent());
		code.add(_indent+this.getIndent()+"</qWhile>");
		code.add(_indent+"</while>");
	}
	
    @Override
	protected void generateCode(Repeat _repeat, String _indent)
	{
		code.add(_indent+"<repeat text=\""+BString.encodeToHtml(_repeat.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_repeat.getComment().getCommaText())+"\" color=\""+
				 _repeat.getHexColor()+"\">");
		code.add(_indent+this.getIndent()+"<qRepeat>");
		generateCode(_repeat.q,_indent+this.getIndent()+this.getIndent());
		code.add(_indent+this.getIndent()+"</qRepeat>");
		code.add(_indent+"</repeat>");
	}
	
    @Override
	protected void generateCode(Forever _forever, String _indent)
	{
		code.add(_indent+"<forever text=\""+BString.encodeToHtml(_forever.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_forever.getComment().getCommaText())+"\" color=\""+
				 _forever.getHexColor()+"\">");
		code.add(_indent+this.getIndent()+"<qForever>");
		generateCode(_forever.q,_indent+this.getIndent()+this.getIndent());
		code.add(_indent+this.getIndent()+"</qForever>");
		code.add(_indent+"</forever>");
	}
	
    @Override
	protected void generateCode(Call _call, String _indent)
	{
		code.add(_indent+"<call text=\""+BString.encodeToHtml(_call.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_call.getComment().getCommaText())+"\" color=\""+
				 _call.getHexColor()+"\"></call>");
	}
	
    @Override
	protected void generateCode(Jump _jump, String _indent)
	{
		code.add(_indent+"<jump text=\""+BString.encodeToHtml(_jump.getText().getCommaText())+"\" comment=\""+
				 BString.encodeToHtml(_jump.getComment().getCommaText())+"\" color=\""+
				 _jump.getHexColor()+"\"></jump>");
	}
		
//    @Override
//	protected void generateCode(Subqueue _subqueue, String _indent)
//	{
//		// code.add(_indent+"");
//		for(int i=0;i<_subqueue.getSize();i++)
//		{
//			generateCode((Element) _subqueue.getElement(i),_indent);
//		}
//		// code.add(_indent+"");
//	}
	
    @Override
	public String generateCode(Root _root, String _indent)
	{
		String pr = "program";
		if(_root.isProgram==false) {pr="sub";}
		String ni = "nice";
		if(_root.isNice==false) {ni="abbr";}

		code.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		//code.add("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		// START KGU 2015-12-04: Might not be so bad an idea to write the product version into the file
		//code.add("<root xmlns:nsd=\"http://structorizer.fisch.lu/\" text=\""+BString.encodeToHtml(_root.getText().getCommaText())+"\" comment=\""+
		code.add("<root xmlns:nsd=\"http://structorizer.fisch.lu/\" version=\"" + Element.E_VERSION + "\" text=\"" + 
								BString.encodeToHtml(_root.getText().getCommaText()) + "\" comment=\"" +
		// END KGU 2015-12-04
								BString.encodeToHtml(_root.getComment().getCommaText())+"\" color=\""+
								_root.getHexColor()+"\" type=\""+pr+"\" style=\""+ni+"\">");
		code.add(_indent+"<children>");
		generateCode(_root.children,_indent+this.getIndent());
		code.add(_indent+"</children>");
		code.add("</root>");
		
		return code.getText();
	}
	

}

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

package lu.fisch.structorizer.parsers;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    XML input parser to generate a structogramm frm it savefile.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------		----			-----------
 *      Bob Fisch       2007.12.13              First Issue
 *      Kay Gürtzig     2015.10.29              Enhancement on For loop (new attributes KGU#3)
 *      Kay Gürtzig     2015.10.29              Modification on For loop (new attribute KGU#3)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.Stack;

import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;

public class NSDParser extends DefaultHandler {

	private Root root = null;
	
	private Stack stack = new Stack();
	private Stack ifStack = new Stack();
	private Stack qStack = new Stack();
	private Stack cStack = new Stack();
	private Stack pStack = new Stack();
	
	private boolean multi = false;
	
	private Subqueue lastQ = null;
	private Element lastE = null;

        @Override
	public void startElement(String namespaceUri, String localName, String qualifiedName, Attributes attributes) throws SAXException 
	{
		// --- ELEMENTS ---
		if (qualifiedName.equals("root"))
		{
			if(multi=true)
			{
				/*
				root:=BRoot.create;
				RootList.Add(root);
				*/
			}
			
			// START KGU 2015-12-04: Prepared for future use...
//			String version = Element.E_VERSION;
//			if (attributes.getIndex("version") != -1) { version = attributes.getValue("version"); }
//			// So we might react to some incompatibility... 
//			if (version.indexOf("dev") != 0)
//			{
//				// Unstable version ...
//			}
			// END KGU 2015-12-04
			
			// read attributes
			root.isProgram = true;
			if(attributes.getIndex("type")!=-1)  {if (attributes.getValue("type").equals("sub")) {root.isProgram=false;}}
			root.isNice=true;
			if(attributes.getIndex("style")!=-1)  {if (!attributes.getValue("style").equals("nice")) {root.isNice=false;}}
			// START KGU 2015-12-04: The following line was nonsense
			//if(attributes.getIndex("style")!=-1)  {if (attributes.getValue("type").equals(" ")) {root.isNice=true;}}
			// END KGU 2015-12-04
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {root.setColor(root.getColor().decode("0x"+attributes.getValue("color")));}}
			if(attributes.getIndex("text")!=-1)  {root.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {root.getComment().setCommaText(attributes.getValue("comment"));}
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
			
			// place stack
			lastE = root;
			stack.push(root);
		}
		else if (qualifiedName.equals("instruction"))
		{
			// create element
			Instruction ele = new Instruction(StringList.getNew("???"));
			
			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}
			//if(attributes.getIndex("rotated")!=-1)  {if (attributes.getValue("rotated").equals("1")) {ele.rotated=true;}}
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
			
			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("jump"))
		{
			// create element
			Jump ele = new Jump(StringList.getNew("???"));
			
			// read attributes
			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
						
			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("call"))
		{
			// create element
			Call ele = new Call(StringList.getNew("???"));
			
			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
			
			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("alternative"))
		{
			// create element
			Alternative ele = new Alternative(StringList.getNew("???"));
			
			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
			
			// set children
			ele.qTrue.setColor(ele.getColor());
			ele.qFalse.setColor(ele.getColor());
			
			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("while"))
		{
			// create element
			While ele = new While(StringList.getNew("???"));
			
			
			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
			
			// set children
			ele.q.setColor(ele.getColor());
			
			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("for"))
		{
			// create element
			For ele = new For(StringList.getNew("???"));
			
			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}
			// START KGU#3 2015-10-29: New attributes for cleaner loop parameter analysis
			int got = 0;
			if(attributes.getIndex("counterVar")!=-1)  {ele.setCounterVar(attributes.getValue("counterVar")); got++;}
			if(attributes.getIndex("startValue")!=-1)  {ele.setStartValue(attributes.getValue("startValue")); got++;}
			if(attributes.getIndex("endValue")!=-1)  {ele.setEndValue(attributes.getValue("endValue")); got++;}
			if(attributes.getIndex("stepConst")!=-1)  {ele.setStepConst(attributes.getValue("stepConst")); got++;}
			//ele.isConsistent = ele.checkConsistency();
			// END KGU#3 2015-10-29
			// START KGU#3 2015-11-08: Better management of reliability of structured fields
			if (got == 0)	// Seems to be an older diagram file, so try to split the text
			{
				ele.setCounterVar(ele.getCounterVar());
				ele.setStartValue(ele.getStartValue());
				ele.setEndValue(ele.getEndValue());
				ele.setStepConst(ele.getStepConst());
			}
			boolean reliable = attributes.getIndex("reliable")!=-1 && attributes.getValue("reliable").equals("true");
			ele.isConsistent = (reliable || ele.checkConsistency());
			// END KGU#3 2015-11-08
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
			
			// set children
			ele.q.setColor(ele.getColor());
			
			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("forever"))
		{
			// create element
			Forever ele = new Forever(StringList.getNew("???"));
			
			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
			
			// set children
			ele.q.setColor(ele.getColor());
			
			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("repeat"))
		{
			// create element
			Repeat ele = new Repeat(StringList.getNew("???"));
			
			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}
			
			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}
					
			// set children
			ele.q.setColor(ele.getColor());
			
			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
		}
		else if (qualifiedName.equals("case"))
		{
			// create element
			Case ele = new Case(StringList.getNew("???"));

			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text")); /*System.out.println(attributes.getValue("text"));*/}
			ele.qs.clear();
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}

			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}

			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
			cStack.push(ele);
		}
		// --- QUEUES ---
		else if (qualifiedName.equals("qCase"))
		{
			// create new queue
			lastQ = new Subqueue();
			// setup queue
			lastQ.parent=((Case) cStack.peek());
			lastQ.setColor(lastQ.parent.getColor());
			// handle stacks
			((Case) cStack.peek()).qs.addElement(lastQ);
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("parallel"))
		{
			// create element
			Parallel ele = new Parallel(StringList.getNew("1"));

			// read attributes
			if(attributes.getIndex("text")!=-1)  {ele.getText().setCommaText(attributes.getValue("text"));}
			ele.qs.clear();
			if(attributes.getIndex("comment")!=-1)  {ele.getComment().setCommaText(attributes.getValue("comment"));}
			if(attributes.getIndex("color")!=-1)  {if (!attributes.getValue("color").equals("")) {ele.setColor(ele.getColor().decode("0x"+attributes.getValue("color")));}}

			// set system attribute - NO!
			// if(attributes.getIndex("comment")!=-1)  {Element.E_SHOWCOMMENTS = Element.E_SHOWCOMMENTS || !attributes.getValue("comment").trim().equals("");}

			// place stack
			lastE=ele;
			stack.push(ele);
			lastQ.addElement(ele);
			pStack.push(ele);
		}
		// --- QUEUES ---
		else if (qualifiedName.equals("qPara"))
		{
			// create new queue
			lastQ = new Subqueue();
			// setup queue
			lastQ.parent=((Parallel) pStack.peek());
			lastQ.setColor(lastQ.parent.getColor());
			// handle stacks
			((Parallel) pStack.peek()).qs.addElement(lastQ);
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("children"))
		{
			// handle stacks
			lastQ = root.children;
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qTrue"))
		{
			// create new queue
			lastQ = ((Alternative) lastE).qTrue;
			ifStack.push(((Alternative) lastE).qFalse);
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qFalse"))
		{
			// handle stacks
			lastQ = (Subqueue) ifStack.pop();
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qFor"))
		{
			// handle stacks
			lastQ = ((For) lastE).q;
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qForever"))
		{
			// handle stacks
			lastQ = ((Forever) lastE).q;
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qWhile"))
		{
			// handle stacks
			lastQ = ((While) lastE).q;
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("qRepeat"))
		{
			// handle stacks
			lastQ = ((Repeat) lastE).q;
			qStack.push(lastQ);
		}
	}
	
    @Override
	public void endElement(String namespaceUri, String localName, String qualifiedName) throws SAXException 
	{
		// --- STRUCTURES ---
		if(qualifiedName.equals("root") ||
		   qualifiedName.equals("call") ||
		   qualifiedName.equals("jump") ||
		   qualifiedName.equals("instruction") ||
		   qualifiedName.equals("while") ||
		   qualifiedName.equals("repeat") ||
		   qualifiedName.equals("forever") ||
		   qualifiedName.equals("for") 
		   )
		{
			lastE=(Element) stack.pop();
		}
		else if (qualifiedName.equals("parallel"))
		{
			lastE=(Element) stack.pop();
			pStack.pop();
		}
		else if (qualifiedName.equals("case"))
		{
			lastE=(Element) stack.pop();
			cStack.pop();
		}
		// -- QUEUES ---
		else if(qualifiedName.equals("qCase") ||
				qualifiedName.equals("qPara") ||
				qualifiedName.equals("qFor") ||
				qualifiedName.equals("qForever") ||
				qualifiedName.equals("qWhile") ||
				qualifiedName.equals("qRepeat") ||
				qualifiedName.equals("qTrue") ||
				qualifiedName.equals("qFalse")
				)
		{
			lastQ=(Subqueue) qStack.pop();
			lastQ=(Subqueue) qStack.peek();
		}
		else if(qualifiedName.equals("children"))
		{
			lastQ=(Subqueue) qStack.pop();
		}
		else if(qualifiedName.equals("qTrue"))
		{
			lastQ=(Subqueue) qStack.pop();
		}
	}
	
    @Override
	public void characters(char[] chars, int startIndex, int endIndex) 
	{
		//String dataString =	new String(chars, startIndex, endIndex).trim();
	}
	
	public Root parse(String _filename)
	{
		// setup a new root
		root=new Root();
		
		// clear stacks
		stack.clear();		
		ifStack.clear();		
		qStack.clear();
		cStack.clear();
		pStack.clear();
				
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try		
		{
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(_filename,this);
		} 
		catch(Exception e) 
		{
			String errorMessage = "Error parsing " + _filename + ": " + e;
			System.err.println(errorMessage);
			e.printStackTrace();
		}
		
		root.hasChanged=false;
		
		return root;
	}
}

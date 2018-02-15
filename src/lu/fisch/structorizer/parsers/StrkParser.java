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
 *      Author:         kay
 *
 *      Description:    XML input parser to generate a structogram from a Struktogrammeditor save file.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.04.25      First Issue
 *      Kay Gürtzig     2017.05.22      Enh. #372: New attribute "origin" supported 
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

import lu.fisch.utils.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.elements.For.ForLoopStyle;
import lu.fisch.structorizer.io.Ini;

public class StrkParser extends DefaultHandler implements INSDImporter
{

	private Root root = null;

	private Stack<Element>  stack   = new Stack<Element>();
	private Stack<Subqueue> ifStack = new Stack<Subqueue>();
	private Stack<Subqueue> qStack  = new Stack<Subqueue>();
	private Stack<Case>     cStack  = new Stack<Case>();
	private Stack<Parallel> pStack  = new Stack<Parallel>();
	private boolean textExpected = false;

	private Subqueue lastQ = null;
	private Element lastE = null;
	
	//---------------------- File Filter configuration ---------------------------
	
	public String getDialogTitle() {
		return "Struktogrammeditor";
	}

	public String getFileDescription() {
		return "Struktogrammeditor files";
	}

	public String[] getFileExtensions() {
		final String[] exts = { "strk", "xml" };
		return exts;
	}

	private static final String getExtension(File f) 
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		
		return ext;
	}
	
	private static final String getExtension(String s) 
	{
		String ext = null;
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	/**
	 * Internal check for acceptable input files. The default implementation just
	 * compares the filename extension with the extensions configured in and
	 * provided by {@link #getFileExtensions()}. Helper method for method 
	 * {@link #accept(File)}.
	 * @param _filename
	 * @return true if the import file is formally welcome. 
	 */
	protected final boolean isOK(String _filename)
	{
		boolean res = false;
		String ext = getExtension(_filename); 
		if (ext != null)
		{
			for (int i =0; i<getFileExtensions().length; i++)
			{
				res = res || (ext.equalsIgnoreCase(getFileExtensions()[i]));
			}
		}
		return res;
	}
	
	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new javax.swing.filechooser.FileFilter() {

			/* (non-Javadoc)
			 * @see javax.swing.filechooser.FileFilter#getDescription()
			 */
			@Override
			public final String getDescription() 
			{
		        return getFileDescription();
		    }
			
			/* (non-Javadoc)
			 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
			 */
			@Override
		    public final boolean accept(File f) 
			{
		        if (f.isDirectory()) 
				{
		            return true;
		        }
				
		        String extension = getExtension(f);
		        if (extension != null) 
				{
		            return isOK(f.getName());
				}
				
		        return false;
		    }
			
		};
	}

	@Override
	public void startElement(String namespaceUri, String localName, String qualifiedName, Attributes attributes) throws SAXException 
	{
		// --- ELEMENTS ---
		if (qualifiedName.equals("struktogramm"))
		{
			// read attributes
			root.setProgram(true);
			root.isBoxed = true;

			// place stack
			lastE = root;
			stack.push(root);
			lastQ = root.children;
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("strelem")) {
			Element ele = null;
			int elemType = -1;
			if (attributes.getIndex("typ") != -1) {
				elemType = Integer.parseInt(attributes.getValue("typ"));
			}
			// create element
			switch (elemType) {
			case 0: /* instruction */
				ele = new Instruction("???");
				break;
			case 7: /*jump*/
				ele = new Jump("???");
				break;
			case 8: /*call*/
				ele = new Call("???");
				break;
			case 1: /*alternative*/
				ele = new Alternative("???");
				break;
			case 4: /*while*/
				ele = new While("???");
				break;
			case 3: /*for*/
				ele = new For("???");
				((For)ele).style = ForLoopStyle.FREETEXT;
				break;
			case 6: /*forever*/
				ele = new Forever("");
				break;
			case 5: /*repeat*/
				ele = new Repeat("???");
				ele.setComment("TODO: The condition is likely to be negated");
				break;
			case 2: /*case*/
				ele = new Case();
				cStack.push((Case)ele);
			break;
			}
			if (ele != null) {
				if (attributes.getIndex("bgcolor") !=-1) {
					String colorStr = attributes.getValue("bgcolor").trim();
					if (!colorStr.isEmpty() && !colorStr.equals("-1")) {
						ele.setColor(Color.decode(colorStr));
					}
				}
				stack.push(ele);
				lastQ.addElement(ele);
			}
			// place stack
			lastE=ele;
		}
		// --- QUEUES ---
		else if (qualifiedName.equals("fall")) {
			// Is it a Case case?
			if (stack.peek() instanceof Case) {
				// create new queue
				lastQ = new Subqueue();
				// setup queue
				lastQ.parent = cStack.peek();	// the Case element
				if (attributes.getIndex("fallname") !=-1) {
					String selector = this.decodeText(attributes.getValue("fallname").trim()).concatenate(",");
					lastQ.parent.getText().add(selector);
				}
				else {
					lastQ.parent.getText().add("???");
				}
				cStack.peek().qs.addElement(lastQ);
				qStack.push(lastQ);
			}
			else if (stack.peek() instanceof Alternative) {
				// We might guess something from the "fallname" attribute but we simply
				// assume that the first branch be always the TRUE branch
				if (lastE instanceof Alternative) {
					// create new queue
					lastQ = ((Alternative) lastE).qTrue;
					ifStack.push(((Alternative) lastE).qFalse);
					qStack.push(lastQ);
				}
				// False branch
				else if (!ifStack.isEmpty() && ifStack.peek() == ((Alternative)stack.peek()).qFalse) {
					// handle stacks
					lastQ = ifStack.pop();
					// START KGU 2106-12-21: Bugfix #317
					qStack.push(lastQ);
				}
				
			}
		}
		else if (qualifiedName.equals("schleifeninhalt"))
		{
			// handle stacks
			lastQ = ((ILoop) lastE).getBody();
			qStack.push(lastQ);
		}
		else if (qualifiedName.equals("text")) {
			textExpected = true;
		}
	}

	@Override
	public void endElement(String namespaceUri, String localName, String qualifiedName) throws SAXException 
	{
		// --- STRUCTURES ---
		if(qualifiedName.equals("struktogramm") ||
				qualifiedName.equals("strelem"))
		{
			if (lastE != null) {
				lastE = stack.pop();
				if (lastE instanceof Case) {
					// Now we have to drop the first two (dummy) branches
					Case ele = (Case)lastE;
					// (at least two branches must remain, though)
					if (ele.qs.size() > 3) {
						ele.qs.remove(0);
						ele.qs.remove(0);
						ele.getText().remove(1, 3);
					}
					cStack.pop();
				}
				else if (qualifiedName.equals("struktogramm")) {
					lastQ = qStack.pop();				
				}
			}
			else if (!stack.isEmpty()) {
				lastE = stack.peek();
			}
		}
		// -- QUEUES ---
		else if(qualifiedName.equals("schleifeninhalt") ||
				qualifiedName.equals("fall"))
		{
			qStack.pop();
			lastQ = qStack.peek();
		}
		else if (qualifiedName.equals("text")) {
			textExpected = false;
		}
	}

	@Override
	public void characters(char[] chars, int startIndex, int endIndex) 
	{
		if (textExpected) {
			String dataString =	new String(chars, startIndex, endIndex).trim();
			if (!dataString.isEmpty() && lastE != null) {
				StringList text = this.decodeText(dataString);
				if (lastE instanceof Forever) {
					lastE.setComment(text);
				}
				else {
					lastE.setText(text);
				}
			}
		}
	}
	
	private StringList decodeText(String _encoded) {
		String[] asciiCodes = _encoded.split(";");
		byte[] codes = new byte[asciiCodes.length];
		StringList decoded = new StringList();
		for (int i = 0; i < asciiCodes.length; i++) {
			short code = Short.parseShort(asciiCodes[i]);
			if (code > 127) {
				code -= 256;
			}
			codes[i] = (byte)code;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(codes);
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(bis, "ISO-8859-1"));
			String line = null;
			while ((line = br.readLine()) != null) {
				decoded.add(line);
			}
			br.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decoded;
	}

	public Root parse(String _filename) throws SAXException, IOException
	{
		// setup a new root
		root=new Root();

		// clear stacks
		stack.clear();
		ifStack.clear();
		qStack.clear();
		cStack.clear();
		pStack.clear();

		// START KGU#258 2016-09-26: Enhancement #253
		Ini ini = Ini.getInstance();
		ini.load();

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
			// START KGU#111 2015-12-16: Bugfix #63 re-throw the exception!
			if (e instanceof SAXException)
			{
				throw (SAXException)e;
			}
			else if (e instanceof IOException)
			{
				throw (IOException)e;
			}
			// END KGU#111 2015-12-16
		}
		
		String progName = (new File(_filename)).getName();
		if (progName.contains(".")) {
			progName = progName.substring(0, progName.lastIndexOf(".")).replace(".", "_");
		}
		progName = progName.replace(" ", "_");
		root.setText(progName);
		// START KGU#363 2017-05-22: Enh. #372
		root.origin += " / " + this.getClass().getSimpleName() + ": \"" + _filename + "\""; 
		// END KGU#363 2017-05-22

		return root;
	}

	// START KGU#177 2016-04-14: Enh. 158 - we need an opportunity to parse an XML string as well
	// (FIXME: This is just a copy-and-paste clone of Root parse(String _filename))
	public Root parse(InputStream _is) throws SAXException, IOException
	{
		// setup a new root
		root=new Root();

		// clear stacks
		stack.clear();
		ifStack.clear();
		qStack.clear();
		cStack.clear();
		pStack.clear();

		// START KGU#258 2016-09-26: Enh. #253
		Ini ini = Ini.getInstance();
		ini.load();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try		
		{
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(_is, this);
		} 
		catch(Exception e) 
		{
			String errorMessage = "Error parsing NSD: " + e;
			System.err.println(errorMessage);
			e.printStackTrace();
			// START KGU#111 2015-12-16: Bugfix #63 re-throw the exception!
			if (e instanceof SAXException)
			{
				throw (SAXException)e;
			}
			else if (e instanceof IOException)
			{
				throw (IOException)e;
			}
			// END KGU#111 2015-12-16
		}
		return root;
	}
	// END KGU#177 2016-04-14

}

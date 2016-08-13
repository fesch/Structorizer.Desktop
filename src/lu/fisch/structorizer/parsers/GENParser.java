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
 *      Description:    Parse plugin-file
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2008.04.12      First Issue
 *      Kay Gürtzig     2016.04.01      Type of field plugin specialized
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import lu.fisch.utils.*;
import lu.fisch.structorizer.helpers.*;
import lu.fisch.structorizer.elements.*;

public class GENParser extends DefaultHandler {
	
	private Vector<GENPlugin> plugins = new Vector<GENPlugin>();
	
	public void startElement(String namespaceUri, String localName, String qualifiedName, Attributes attributes) throws SAXException 
	{
		// --- PLUGINS ---
		if (qualifiedName.equals("plugin"))
		{
			GENPlugin plugin = new GENPlugin();
			
			if(attributes.getIndex("class")!=-1)  {plugin.className=attributes.getValue("class");}
			if(attributes.getIndex("title")!=-1)  {plugin.title=attributes.getValue("title");}
			if(attributes.getIndex("icon")!=-1)  {plugin.icon=attributes.getValue("icon");}
			
			plugins.add(plugin);
		}
	}	
	public void endElement(String namespaceUri, String localName, String qualifiedName) throws SAXException 
	{
	}
	
	public void characters(char[] chars, int startIndex, int endIndex) 
	{
	}
	
	public Vector<GENPlugin> parse(BufferedInputStream _is)
	{
		plugins = new Vector<GENPlugin>();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try		
		{
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(_is,this);
		} 
		catch(Exception e) 
		{
			String errorMessage = "Error parsing input bugger: " + e;
			System.err.println(errorMessage);
			e.printStackTrace();
		}
		
		return plugins;
	}
}

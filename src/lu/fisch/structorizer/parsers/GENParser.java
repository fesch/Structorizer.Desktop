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
 *      Kay Gürtzig     2016.04.01      Type of field plugins specialized
 *      Kay Gürtzig     2017.04.23      Enh. #231: reserved words configuration moved to plugin file
 *      Kay Gürtzig     2017.06.20      Enh. #354,#357: Option retrieval added, #404: test with schema file (failed)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import lu.fisch.structorizer.helpers.*;
import lu.fisch.utils.StringList;

public class GENParser extends DefaultHandler {
	
	private static Schema pluginSchema = null;
	
	private Vector<GENPlugin> plugins = new Vector<GENPlugin>();
	// START KGU#416 2017-06-20: Enh. #354, #357
	private StringList lastOptionItems = null;
	/**
	 * In case of an error (may be a SAX parser error) the error message will be
	 * available in this field.
	 */
	public String errorMessage = null;
	// END KGU#416 2017-06-20
	
	// START KGU#400 2017-06-20: Issue #404
	public boolean validationError = false;  
	public SAXParseException saxParseException = null; 
	public void error(SAXParseException exception) throws SAXException
	{
//		System.err.println(exception);
		validationError = true;
		saxParseException = exception;
		if (errorMessage == null) {
			errorMessage = "";
		}
		errorMessage += exception.toString() + "\n";
	}

	public void fatalError(SAXParseException exception) throws SAXException
	{
//		System.err.println("FATAL: " + exception);
		validationError = true;	    
		saxParseException = exception;
		if (errorMessage == null) {
			errorMessage = "";
		}
		errorMessage += exception.toString() + "\n";
	}		    
	// END KGU#400 2017-06-20

	public void startElement(String namespaceUri, String localName, String qualifiedName, Attributes attributes) throws SAXException 
	{
		// --- PLUGINS ---
		if (qualifiedName.equals("plugin"))
		{
			GENPlugin plugin = new GENPlugin();
			
			if(attributes.getIndex("class")!=-1)  {plugin.className=attributes.getValue("class");}
			if(attributes.getIndex("title")!=-1)  {plugin.title=attributes.getValue("title");}
			if(attributes.getIndex("icon")!=-1)  {plugin.icon=attributes.getValue("icon");}
			// START KGU#386 2017-04-28: Enh.
			if(attributes.getIndex("info")!=-1)  {plugin.info=attributes.getValue("info");}
			// END KGU#386 2017-04-28
			// START KGU#239 2017-04-23: Enh. #231
			if(attributes.getIndex("case_matters")!=-1)  {plugin.caseMatters=!attributes.getValue("case_matters").equals("0");}
			if(attributes.getIndex("reserved_words") != -1) {
				plugin.reservedWords = attributes.getValue("reserved_words").split(",");
			}
			// END KGU#239 2017-04-23
			
			plugins.add(plugin);
		}
		// START KGU#416 2017-06-20: Enh. #354, #357
		else if (qualifiedName.equals("option"))
		{
			// This is supposed to be an option for the last plugin
			if (!plugins.isEmpty()) {
				GENPlugin plugin = plugins.lastElement();
				HashMap<String, String> option = new HashMap<String, String>();
				if (attributes.getIndex("name") != -1)  {
					option.put("name", attributes.getValue("name"));
				}
				if (attributes.getIndex("type") != -1) {
					String type = attributes.getValue("type");
					option.put("type", attributes.getValue("type"));
					if (type.equalsIgnoreCase("enum")
							|| type.equalsIgnoreCase("enumeration")
							|| type.equalsIgnoreCase("list")) {
						lastOptionItems = new StringList();
					}
				}
				else {
					// Without explicitly given type name it is supposed to be a list
					lastOptionItems = new StringList();			
				}
				if (attributes.getIndex("title") != -1) {
					option.put("title",  attributes.getValue("title"));
				}
				if (attributes.getIndex("help") != -1) {
					option.put("help", attributes.getValue("help"));
				}
				plugin.options.add(option);
			}
		}
		else if (qualifiedName.equals("item")) {
			if (!plugins.isEmpty() && lastOptionItems != null
					&& attributes.getIndex("value") != -1) {
				lastOptionItems.add(attributes.getValue("value"));
			}
		}
		// END KGU#416 2017-06-20
	}	

	public void endElement(String namespaceUri, String localName, String qualifiedName) throws SAXException 
	{
		// START KGU#416 2017-06-20: Enh. #354, #357
		if (qualifiedName.equals("plugin")) {
			lastOptionItems = null;
		}
		else if (qualifiedName.equals("option") && lastOptionItems != null) {
			GENPlugin plugin = plugins.lastElement();
			plugin.options.lastElement().put("items", 
					"{" + lastOptionItems.concatenate(";") + "}");
			lastOptionItems = null;
		}
		// END KGU#416 2017-06-20
	}
	
	public void characters(char[] chars, int startIndex, int endIndex) 
	{
	}
	
	public Vector<GENPlugin> parse(BufferedInputStream _is)
	{
		plugins = new Vector<GENPlugin>();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		// START KGU#400 2017-06-20: Issue #404
		if (pluginSchema == null) {
			URL schemaLocal = this.getClass().getResource("plugin.xsd");
			SchemaFactory sFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				pluginSchema = sFactory.newSchema(schemaLocal);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//factory.setNamespaceAware(true);
		factory.setValidating(true);
		factory.setSchema(pluginSchema);
		// END KGU#400 2017-06-20: Issue #404
		try		
		{
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(_is,this);
		} 
		catch(Exception e) 
		{
			// START KGU#416 2017-06-20: Enh. #354, #357 - error must be obtainable
			//String errorMessage = "Error parsing input bugger: " + e;
			if (errorMessage == null) {
				errorMessage = "";
			}
			errorMessage += "Error parsing plugin file: " + e + "\n";
			// END KGU#416 2017-06-20
			System.err.println(errorMessage);
			e.printStackTrace();
		}
		
		return plugins;
	}
}

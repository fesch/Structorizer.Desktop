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
 *      Author:         Kay Gürtzig
 *
 *      Description:    Abstract Parser class for all code import (except Pascal/Delphi).
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.04      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import goldengine.java.GOLDParser;
import goldengine.java.Reduction;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.io.Ini;


/**
 * @author kay
 *
 */
public abstract class CodeParser extends javax.swing.filechooser.FileFilter
{
	/************ Common fields *************/
	public String error;
	protected GOLDParser parser;
	protected Root root = null;
	// We may obtain a collection of Roots (unit or program with subroutines)!
	protected List<Root> subRoots = new LinkedList<Root>();

	/************ Abstract Methods *************/
	public abstract String getDialogTitle();
	protected abstract String getFileDescription();
	protected abstract String[] getFileExtensions();
	/**
	 * Parses the source code from file _textToParse, which is supposed to be encoded
	 * with the charset _encoding, and returns a list of structograms - one for each function
	 * or program contained in the source file.
	 * Field `error' will either contain an empty string or an error message afterwards.
	 * @param _textToParse - file name of the C source.
	 * @param _encoding - name of the charset to be used for decoding
	 * @return A list containing composed diagrams (if successful, otherwise field error will contain an error description) 
	 */
	public abstract List<Root> parse(String textToParse, String _encoding);

	/******* FileFilter Extension *********/
	protected boolean isOK(String _filename)
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
	
	private static String getExtension(String s) 
	{
		String ext = null;
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	private static String getExtension(File f) 
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
	
	@Override
	public String getDescription() 
	{
        return getFileDescription();
    }
	
	@Override
    public boolean accept(File f) 
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

	/**
	 * This is the entry point for the Nassi-Shneiderman diagram construction
	 * from the successful
	 * @param _reduction
	 */
	protected void buildNSD(Reduction _reduction)
	{
		root.isProgram = true;
		buildNSD_R(_reduction, root.children);
	}
	
	/**
	 * Recursively constructs the Nassi-Shneiderman diagram into the _parentNode
	 * from the given reduction subtree 
	 * @param _reduction - the current reduction subtree to be converted
	 * @param _parentNode - the Subqueue the emerging elements are to be added to.
	 */
	protected abstract void buildNSD_R(Reduction _reduction, Subqueue _parentNode);
	
	/**
	 * Composes the parsed non-terminal _reduction to a Structorizer-compatible
	 * terminal string, combines it with the given _content string and returns the
	 * result
	 * @param _reduction - a reduction sub-tree
	 * @param _content - partial translation result to be combined with the _reduction
	 * @return the combined translated string
	 */
	protected abstract String getContent_R(Reduction _reduction, String _content);
	
	/************************
	 * static things
	 ************************/
	
	// START KGU#165 2016-03-25: Once and for all: It should be a transparent choice, ...
	/**
	 * whether or not the keywords are to be handled in a case-independent way
	 */
	public static boolean ignoreCase = true;
	// END KGU#165 2016-03-25
	
	// START KGU#288 2016-11-06: Issue #279: Access limited to private, compensated by new methods
	//public static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
	private static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
	// END KGU#288 2016-11-06
	static {
		keywordMap.put("preAlt",     "");
		keywordMap.put("postAlt",    "");
		keywordMap.put("preCase",    "");
		keywordMap.put("postCase",   "");
		keywordMap.put("preFor",     "for");
		keywordMap.put("postFor",    "to");
		keywordMap.put("stepFor",    "step");
		keywordMap.put("preForIn",   "foreach");
		keywordMap.put("postForIn",  "in");
		keywordMap.put("preWhile",   "while");
		keywordMap.put("postWhile",  "");
		keywordMap.put("preRepeat",  "until");
		keywordMap.put("postRepeat", "");
		keywordMap.put("preLeave",   "leave");
		keywordMap.put("preReturn",  "return");
		keywordMap.put("preExit",    "exit");
		keywordMap.put("input",      "read");
		keywordMap.put("output",     "write");
	}
	
	public static void loadFromINI()
	{
		final HashMap<String, String> defaultKeys = new HashMap<String, String>();
		// START KGU 2017-01-06: Issue #327: Defaults changed to English
		defaultKeys.put("ParserPreFor", "for");
		defaultKeys.put("ParserPostFor", "to");
		defaultKeys.put("ParserStepFor", "by");
		defaultKeys.put("ParserPreForIn", "foreach");
		defaultKeys.put("ParserPostForIn", "in");
		defaultKeys.put("ParserPreWhile", "while ");
		defaultKeys.put("ParserPreRepeat", "until ");
		defaultKeys.put("ParserPreLeave", "leave");
		defaultKeys.put("ParserPreReturn", "return");
		defaultKeys.put("ParserPreExit", "exit");
		defaultKeys.put("ParserInput", "INPUT");
		defaultKeys.put("ParserOutput", "OUTPUT");
		// END KGU 2017-01-06 #327
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();

			// elements
			// START KGU#258 2016-09-25: Code redesign for enh. #253
//			preAlt = ini.getProperty("ParserPreAlt","");
//			postAlt = ini.getProperty("ParserPostAlt","");
//			preCase = ini.getProperty("ParserPreCase","");
//			postCase = ini.getProperty("ParserPostCase","");
//			preFor = ini.getProperty("ParserPreFor","pour ");
//			postFor = ini.getProperty("ParserPostFor","\u00E0");
//			// START KGU#3 2015-11-08: Enhancement #10
//			stepFor = ini.getProperty("ParserStepFor", ", pas = ");
//			// END KGU#3 2015-11-08
//			// START KGU#61 2016-03-20: Enh. #84/#135 - support and distinguish FOR-IN loops
//			preForIn = ini.getProperty("ParserPreForIn","pour ");
//			postForIn = ini.getProperty("ParserPostForIn"," en ");
//			// END KGU#61 2016-03-20
//			preWhile = ini.getProperty("ParserPreWhile","tant que ");
//			postWhile = ini.getProperty("ParserPostWhile","");
//			preRepeat = ini.getProperty("ParserPreRepeat","jusqu'\u00E0 ");
//			postRepeat = ini.getProperty("ParserPostRepeat","");
//    		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
//			preLeave = ini.getProperty("ParserPreLeave", "leave");
//			preReturn = ini.getProperty("ParserPreReturn", "return");
//			preExit = ini.getProperty("ParserPreExit", "exit");
//    		// END KGU#78 2016-03-25
//			input = ini.getProperty("ParserInput","lire");
//			output = ini.getProperty("ParserOutput","\u00E9crire");
			for (String key: keywordMap.keySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
                                if(defaultKeys.containsKey(propertyName))
                                {
                                    keywordMap.put(key, ini.getProperty(propertyName, defaultKeys.get(propertyName)));
                                }
                                else
                                {
                                    keywordMap.put(key, ini.getProperty(propertyName, ""));
                                }
			}
			
			// END KGU#258 2016-09-25
			// START KGU#165 2016-03-25: Enhancement configurable case awareness
			ignoreCase = ini.getProperty("ParserIgnoreCase", "true").equalsIgnoreCase("true");
			// END KGU#3 2016-03-25
			
		}
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}
	
	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();			// elements
			// START KGU#258 2016-09-25: Code redesign for enh. #253			
//			ini.setProperty("ParserPreAlt",preAlt);
//			ini.setProperty("ParserPostAlt",postAlt);
//			ini.setProperty("ParserPreCase",preCase);
//			ini.setProperty("ParserPostCase",postCase);
//			ini.setProperty("ParserPreFor",preFor);
//			ini.setProperty("ParserPostFor",postFor);
//			// START KGU#3 2015-11-08: Enhancement #10
//			ini.setProperty("ParserStepFor",stepFor);
//			// END KGU#3 2015-11-08
//			// START KGU#61 2016-03-20: Enh. #84/#135 - support and distinguish FOR-IN loops
//			ini.setProperty("ParserPreForIn",preForIn);
//			ini.setProperty("ParserPostForIn",postForIn);
//			// END KGU#61 2016-03-20
//			ini.setProperty("ParserPreWhile",preWhile);
//			ini.setProperty("ParserPostWhile",postWhile);
//			ini.setProperty("ParserPreRepeat",preRepeat);
//			ini.setProperty("ParserPostRepeat",postRepeat);
//    		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
//			ini.setProperty("ParserPreLeave", preLeave);
//			ini.setProperty("ParserPreReturn", preReturn);
//			ini.setProperty("ParserPreExit", preExit);
//    		// END KGU#78 2016-03-25
//			
//			ini.setProperty("ParserInput",input);
//			ini.setProperty("ParserOutput",output);
//			// START KGU#165 2016-03-25: Enhancement 
//			ini.setProperty("ParserIgnoreCase",Boolean.toString(ignoreCase));
//			// END KGU#3 2016-03-25
			for (Map.Entry<String, String> entry: getPropertyMap(true).entrySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);
				ini.setProperty(propertyName, entry.getValue());
			}
			// END KGU#258 2016-09-25
			
			ini.save();
		}
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}
	
	// START KGU#163 2016-03-25: For syntax analysis purposes
	/**
	 * Returns the complete set of configurable parser keywords for Elements 
	 * @return array of current keyword strings
	 */
	public static String[] getAllProperties()
	{
		String[] props = new String[]{};
		return keywordMap.values().toArray(props);
	}
	// END KGU#163 2016-03-25
	
	// START KGU#258 2016-09-25: Enh. #253 (temporary workaround for the needed Hashmap)
	/**
	 * Returns a Hashmap mapping parser preference labels like "preAlt" to the
	 * configured parser preference keywords.
	 * @param includeAuxiliary - whether or not non-keyword settings (like "ignoreCase") are to be included
	 * @return the hash table with the current settings
	 */
	public static final HashMap<String, String> getPropertyMap(boolean includeAuxiliary)
	{
		HashMap<String, String> keywords = keywordMap;
		if (includeAuxiliary)
		{
			keywords = new HashMap<String,String>(keywordMap);
			// The following information may be important for a correct search
			keywords.put("ignoreCase",  Boolean.toString(ignoreCase));
		}
		return keywords;
	}
	// END KGU#258 2016-09-25
	
	// START KGU#288 2016-11-06: New methods to facilitate bugfix #278, #279
	/**
	 * Returns the set of the parser preference names
	 * @return
	 */
	public static Set<String> keywordSet()
	{
		return keywordMap.keySet();
	}
	
	/**
	 * Returns the cached keyword for parser preference _key or null
	 * @param _key - the name of the requested parser preference
	 * @return the cached keyword or null
	 */
	public static String getKeyword(String _key)
	{
		return keywordMap.get(_key);
	}
	
	/**
	 * Returns the cached keyword for parser preference _key or the given _defaultVal if no
	 * entry or only an empty entry is found for _key.
	 * @param _key - the name of the requested parser preference
	 * @param _defaultVal - a default keyword to be returned if there is no non-empty cached value
	 * @return the cached or default keyword
	 */
	public static String getKeywordOrDefault(String _key, String _defaultVal)
	{
		// This method circumvents the use of the Java 8 method:
		//return keywordMap.getOrDefault(_key, _defaultVal);
		String keyword = keywordMap.get(_key);
		if (keyword == null || keyword.isEmpty()) {
			keyword = _defaultVal;
		}
		return keyword;
	}
	
	/**
	 * Replaces the cached parser preference _key with the new keyword _keyword for this session.
	 * Note:
	 * 1. This does NOT influence the Ini file!
	 * 2. Only for existing keys a new mapping may be set 
	 * @param _key - name of the parser preference
	 * @param _keyword - new value of the parser preference or null
	 */
	public static void setKeyword(String _key, String _keyword)
	{
		if (_keyword == null) {
			_keyword = "";
		}
		// Bugfix #281/#282
		if (keywordMap.containsKey(_key)) {
			keywordMap.put(_key, _keyword);
		}
	}
	// END KGU#288 2016-11-06
	
}

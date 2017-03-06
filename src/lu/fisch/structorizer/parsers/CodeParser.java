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
 * Abstract base class for all code importing classes using the GOLDParser to
 * parse the source code based on a compiled grammar.
 * A compiled grammar file (version 1.0, with extension cgt) given, the respectie
 * subclass can be generated with the GOLDprog.exe tool using ParserTemplate.pgt
 * as template file, e.g.:
 * {@code GOLDprog.exe Ada.cgt ParserTemplate.pgt AdaParser.java} 
 * The generated subclass woud be able to parse code but must manually be accomplished
 * in order to build a structogram from the obtained parse tree. Override the methods
 * {@link #buildNSD_R(Reduction, Subqueue)} and {@link #getContent_R(Reduction, String)}
 * for that purpose.
 * This is where the
 * real challenge is lurking...
 * @author Kay Gürtzig
 */
public abstract class CodeParser extends javax.swing.filechooser.FileFilter
{
	/************ Common fields *************/
	public String error;
	protected GOLDParser parser;
	protected Root root = null;
	// We may obtain a collection of Roots (unit or program with subroutines)!
	protected List<Root> subRoots = new LinkedList<Root>();
	// START KGU#358 2017-03-06: Enh. #368 - new import option
	protected boolean optionImportVarDecl = false;

	/************ Abstract Methods *************/
	
	/**
	 * Is to return a replacement for the FileChooser title. Ideally its just
	 * the name of the source language imported by this parser.
	 * @return Source language name to be inserted in the file open dialog title. 
	 */
	public abstract String getDialogTitle();
	
	/**
	 * Is to return a short description of the source file type, ideally in English.
	 * @see #getFileExtensions()
	 * @return File type description, e.g. "Ada source files"
	 */
	protected abstract String getFileDescription();
	
	/**
	 * Return a string array with file name extensions to be recognized and accepted
	 * as source files of the input language of this parser. The extensions must not
	 * start with a dot.
	 * Correct: { "cpp", "cc" }, WRONG: { ".cpp", ".cc" } 
	 * @see #getFileDescription()
	 * @return the array of associated file name extensions
	 */
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
	
	
	/******* Diagram Synthesis *********/
	
	/**
	 * This is the entry point for the Nassi-Shneiderman diagram construction
	 * from the successfully established parse tree.
	 * Retrieves the import options, sets the initial diagram type (to program)
	 * and calls {@link #buildNSD_R(Reduction, Subqueue)}.
	 * NOTE: If your subclass needs to to some specific initialization then
	 * override {@link #initializeBuildNSD()}.
	 * @see #buildNSD_R(Reduction, Subqueue)
	 * @see #getContent_R(Reduction, String)
	 * @param _reduction the top Reduction of the parse tree.
	 */
	protected final void buildNSD(Reduction _reduction)
	{
		// START KGU#358 2017-03-06: Enh. #368 - consider import options!
		this.optionImportVarDecl = Ini.getInstance().getProperty("impVarDeclarations", "false").equals("true");
		// END KGU#358 2017-03-06
		root.isProgram = true;
		this.initializeBuildNSD();
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
	
	/**
	 * Overridable method to do target-language-specific initialization before
	 * the recursive method {@link #buildNSD_R(Reduction, Subqueue)} will be called.
	 * Method is called in {@link #buildNSD(Reduction)}.
	 */
	protected void initializeBuildNSD()
	{
	}
	
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
			for (Map.Entry<String, String> entry: getPropertyMap(true).entrySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);
				ini.setProperty(propertyName, entry.getValue());
			}
			
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

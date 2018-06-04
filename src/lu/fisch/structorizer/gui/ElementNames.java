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
package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Lightweight class providing locale- and user-specified names for all Element classes.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.12.14      First Issue on behalf of enhancement request #492
 *      Kay Gürtzig     2018.03.21      All direct console output replaced with logging
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.Component;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.io.Ini;

/**
 * Lightweight class holding both user-specific GUI designations for Element types
 * and locale-specific default Element names available for message resolution.
 * Provides basic string processing methods to replace element name placeholders by
 * current external element names. 
 * Specific placeholders {@code "@[a-p]"} in message patterns mark by what Element
 * caption this is required to be substituted where the letter stands for:<br/>
 * a - Instruction<br/>
 * b - Alternative<br/>
 * c - Case<br/>
 * d - For (flavour-unspecific)<br/>
 * e - For (counting flavour)<br/>
 * f - For (traversing flavour)
 * g - While<br/>
 * h - Repeat<br/>
 * i - Forever<br/>
 * j - Call<br/>
 * k - Jump<br/>
 * l - Parallel<br/>
 * m - Root (type-indifferent)<br/>
 * n - Main (Root type)<br/>
 * o - Sub (Root type)<br/>
 * p - Includable (Root type)<br/>
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class ElementNames extends Component {
	
	// START#484 KGU 2018-03-22: Issue #463
	public static final Logger logger = Logger.getLogger(ElementNames.class.getName());
	// END KGU#484 2018-03-22

	private static ElementNames instance = null;
	
	private static HashMap<String, Integer> classNameLookUp = new HashMap<String, Integer>();
	static {
		classNameLookUp.put("Instruction", 0);
		classNameLookUp.put("Alternative", 1);
		classNameLookUp.put("Case", 2);
		classNameLookUp.put("For", 3);
		classNameLookUp.put("While", 6);
		classNameLookUp.put("Repeat", 7);
		classNameLookUp.put("Forever", 8);
		classNameLookUp.put("Call", 9);
		classNameLookUp.put("Jump", 10);
		classNameLookUp.put("Parallel", 11);
		classNameLookUp.put("Root", 12);
		classNameLookUp.put("For.COUNTER", 4);
		classNameLookUp.put("For.TRAVERSAL", 5);
		classNameLookUp.put("Root.DT_MAIN", 13);
		classNameLookUp.put("Root.DT_SUB", 14);
		classNameLookUp.put("Root.DT_INCL", 15);
	}
	public static boolean useConfiguredNames = true;
	/**
	 * Array of user-defined names for the different element types and flavours.<br/>
	 * Indices must correspond with those of {@link #localizedNames}.
	 */
	public static String[] configuredNames = {
			null,	// Instruction
			null,	// Alternative
			null,	// Case
			null,	// For 
			null,	// For-To (counting For)
			null,	// Foreach (traversing For)
			null,	// While
			null,	// Repeat
			null,	// Forever
			null,	// Call
			null,	// Jump
			null,	// Parallel
			null,	// Root
			null,	// Main (Root)
			null,	// Sub (Root)
			null,	// Includable (Root)
	};
	/**
	 * Array of locale-defined names for element types and flavours.<br/>
	 * Indices must correspond with those of {@link #configuredNames}.
	 */
	public static LangTextHolder[] localizedNames = {
			new LangTextHolder("Instruction"),	
			new LangTextHolder("IF"),	
			new LangTextHolder("CASE"),	
			new LangTextHolder("FOR"),	// General name 
			new LangTextHolder("FOR-TO"),
			new LangTextHolder("FOR-IN"),	
			new LangTextHolder("WHILE"),	
			new LangTextHolder("REPEAT"),	
			new LangTextHolder("FOREVER"),	
			new LangTextHolder("CALL"),	
			new LangTextHolder("EXIT"),	
			new LangTextHolder("PARALLEL"),	
			new LangTextHolder("Diagram"),	
			new LangTextHolder("Main program"),
			new LangTextHolder("Sub-routine"),	
			new LangTextHolder("Includable"),	
	};

	public static ElementNames getInstance()
	{
		if (instance == null) {
			instance = new ElementNames();
		}
		return instance;
	}
	
	/**
	 * Returns an external name for the element class or flavour indexed by {@code _index},
	 * where user configuration has priority over locale default, unless {@code _defaultOnly}
	 * is true.
	 * @param _index - the index into the name tables
	 * @param _defaultOnly - if true then user-configured names otherwise prioritized will
	 * be ignored.
	 * @return the element name for GUI purposes if available, null otherwise.
	 */
	public static String getElementName(int _index, boolean _defaultOnly)
	{
		String name = null;
		if (!_defaultOnly && useConfiguredNames && _index >= 0 && _index < configuredNames.length) {
			name = configuredNames[_index].trim();
			if (name.isEmpty()) {
				name = null;
			}
		}
		if (name == null && _index >= 0 && _index < localizedNames.length) {
			name = localizedNames[_index].getText();
		}
		return name;
	}
	
	/**
	 * Returns an external name for the element class or flavor encoded by placeholder
	 * letter {@code _formatCode}, where user configuration has always priority over
	 * locale default.
	 * @param __formatCode - a letter in [a-p] according to the mapping listed in the 
	 * {@link ElementNames} comment.
	 * @param _defaultOnly - if true then user-configured names otherwise prioritized will
	 * be ignored.
	 * @return the element name for GUI purposes if available, null otherwise.
	 */
	public static String getElementName(char _formatCode)
	{
		return getElementName((int)Character.toLowerCase(_formatCode) - (int)'a', false);
	}
	
	/**
	 * Derives an external name for the class of given element {@code _element}, where
	 * user configuration has priority over locale default, unless {@code _defaultOnly}
	 * is true.
	 * @param _element - a Structorizer {@link Element} instance
	 * @param _defaultOnly - if true then user-configurations otherwise prioritized will
	 * be ignored.
	 * @return the element name for GUI purposes if available, null otherwise.
	 * @see #getElementName(char)
	 * @see #getElementName(int, boolean)
	 * @see #resolveElementNames(String)
	 */
	public static String getElementName(Element _element, boolean _defaultOnly)
	{
		int index = classNameLookUp.get(_element.getClass().getSimpleName());
		if (_element instanceof For) {
			switch (((For)_element).style) {
			case TRAVERSAL:
				index++;
			case COUNTER:
				index++;
				break;
			default:
				break;
			}
		}
		else if (_element instanceof Root) {
			if (((Root)_element).isProgram()) {
				index++;
			}
			else if (((Root)_element).isInclude()) {
				index += 3;
			}
			else {
				// Subroutine
				index += 2;
			}
		}
		return getElementName(index, _defaultOnly);
	}
	
	/**
	 * Replaces all occurrences of placeholders {@code "@[a-p]"} in {@code _rawString}
	 * with the respective Element type names according to the current locale and user
	 * configuration (where user configuration overrides locale default).<br/>
	 * See {@link ElementNames} class comment for the mapping of letters to
	 * element classes or flavours.
	 * @param _rawString - the string with possibly unresolved placeholders
	 * @return the string with resolved element names.
	 * @see #getElementName(char)
	 * @see #getElementName(int, boolean)
	 * @see #getElementName(Element, boolean)
	 */
	public static String resolveElementNames(String _rawString)
	{
		if (!_rawString.contains("@")) {
			return _rawString;
		}
		String[] parts = _rawString.split("@", -1);
		StringBuilder result = new StringBuilder(parts[0]);
		for (int i = 1; i < parts.length; i++) {
			String part = parts[i];
			if (!part.isEmpty()) {
				String elName = null;
				char first = part.charAt(0);
				if (first == '{') {
					int posBrace2 = part.indexOf('}');
					Integer index = null;
					if (posBrace2 > 0 && (index = classNameLookUp.get(part.substring(1, posBrace2))) != null) {
						elName = getElementName(index, false);
						part = part.substring(posBrace2);
					}
				}
				else if (Character.isAlphabetic(first)) {
					elName = getElementName(part.charAt(0));
				}
				if (elName != null) {
					// Insert the found name
					result.append(elName + part.substring(1));
				}
				else {
					// Leave it as is
					result.append("@" + part);
				}
			}
		}
		return result.toString();
	}
	/**
	 * Synchronises {@link ElementNames#configuredNames} settings from {@code ini} properties.  
	 * @param ini - the loaded INI file contents.
	 */
	public static void getFromIni(Ini ini) {
		for (int i = 0; i < configuredNames.length; i++) {
			configuredNames[i] = ini.getProperty("ElementNames." + i, "");
		}
		useConfiguredNames = ini.getProperty("ElementNames", "0").equals("1");
	}
	/**
	 * Synchronises {@link ElementNames#configuredNames} settings to {@code ini} properties.  
	 * @param ini - the loaded INI file contents.
	 */
	public static void putToIni(Ini ini) {
		for (int i = 0; i < configuredNames.length; i++) {
			ini.setProperty("ElementNames." + i, configuredNames[i]);
		}
		ini.setProperty("ElementNames", useConfiguredNames ? "1" : "0");
	}
	/**
	 * Saves {@link ElementNames#configuredNames} settings to the Ini file.  
	 */
	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			putToIni(ini);
			ini.save();
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "Ini", e);
		}
	}
}

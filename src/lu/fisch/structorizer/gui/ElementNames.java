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
 *      Kay Gürtzig     2017-12-14      First Issue on behalf of enhancement request #492
 *      Kay Gürtzig     2018-03-21      All direct console output replaced with logging
 *      Kay Gürtzig     2019-03-17      Enh. #56 New entries for new Element class Try added
 *      Kay Gürtzig     2019-06-07      Issue #726: array ELEMENT_KEYS introduced
 *      Kay Gürtzig     2019-06-10      Issue #726: All resolution methods equipped with an optional translations argument
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
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.locales.Locale;

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
 * q - Try<br/>
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class ElementNames extends Component {
	
	// START#484 KGU 2018-03-22: Issue #463
	public static final Logger logger = Logger.getLogger(ElementNames.class.getName());
	// END KGU#484 2018-03-22
	
	// START KGU#709 2019-06-07: Issue #726 We needed an official list of key names for the Translator usability
	/**
	 * Keys used for element name place holders in messages
	 */
	public static final String[] ELEMENT_KEYS = {
			"Instruction",
			"Alternative",
			"Case",
			"For",
			"While",
			"Repeat",
			"Forever",
			"Call",
			"Jump",
			"Parallel",
			"Root",
			"For.COUNTER",
			"For.TRAVERSAL",
			"Root.DT_MAIN",
			"Root.DT_SUB",
			"Root.DT_INCL",
			// START KGU#686 2019-03-17: Enh. #86
			"Try"
			// END KGU#686 2019-03-17
	};
	// END KGU#709 2019-06-07

	private static ElementNames instance = null;
	
	/** Maps the Element class names (more correctly, the place holder keys) to their index */
	private static HashMap<String, Integer> classNameLookUp = new HashMap<String, Integer>();
	static {
		for (int i = 0; i < ELEMENT_KEYS.length; i++) {
			classNameLookUp.put(ELEMENT_KEYS[i], i);
		}
	}
	/** Flag indicating whether {@link #configuredNames} are to be used for place holder resolution */
	public static boolean useConfiguredNames = true;
	/**
	 * Array of user-defined names for the different element types and flavours. Will only be
	 * considered for place holder resolution if {@link #useConfiguredNames} is enabled.
	 * Configuration via GUI dialog {@link ElementNamePreferences}.<br/>
	 * Indices must correspond to those of @{@link ELEMENT_KEYS} and {@link #localizedNames}.
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
			// START KGU#686 2019-03-17: Enh. #86
			null,	// Try
			// END KGU#686 2019-03-17
	};
	/**
	 * Array of locale-defined names for element types and flavours.
	 * Subject to locale-switching.<br/>
	 * Indices must correspond to thoseof of @{@link ELEMENT_KEYS} and {@link #configuredNames}.
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
			// START KGU#686 2019-03-17: Enh. #86
			new LangTextHolder("TRY"),
			// END KGU#686 2019-03-17
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
	 * @param _translations - Array of translated element names.
	 * @return the element name for GUI purposes if available, null otherwise.
	 */
	public static String getElementName(int _index, boolean _defaultOnly, String[] _translations)
	{
		String name = null;
		if (!_defaultOnly && useConfiguredNames && _index >= 0 && _index < configuredNames.length) {
			name = configuredNames[_index].trim();
			if (name.isEmpty()) {
				name = null;
			}
		}
		if (name == null && _index >= 0 && _index < localizedNames.length) {
			// START KGU#709 2019-06-08: Issue #726
			//name = localizedNames[_index].getText();
			if (_translations != null) {
				name = _translations[_index];
				if (name != null && name.trim().isEmpty()) {
					name = null;
				}
			}
			else {
				name = localizedNames[_index].getText();
			}
			// END KGU#709 2019-06-08
		}
		return name;
	}
	
	/**
	 * Returns an external name for the element class or flavor encoded by placeholder
	 * letter {@code _formatCode}, where user configuration has priority over locale
	 * default, unless {@code _defaultOnly} is true..
	 * @param _formatCode - a letter in [a-p] according to the mapping listed in the 
	 * {@link ElementNames} comment.
	 * @param _defaultOnly - if true then user-configured names otherwise prioritized will
	 * be ignored.
	 * @param _translations - possibly an array of localised elements names (may be null).
	 * @return the element name for GUI purposes if available, null otherwise.
	 */
	public static String getElementName(char _formatCode, boolean _defaultOnly, String[] _translations)
	{
		return getElementName((int)Character.toLowerCase(_formatCode) - (int)'a', false, _translations);
	}
	
	/**
	 * Derives an external name for the class of given element {@code _element}, where
	 * user configuration has priority over locale default, unless {@code _defaultOnly}
	 * is true.
	 * @param _element - a Structorizer {@link Element} instance
	 * @param _defaultOnly - if true then user-configurations otherwise prioritized will
	 * be ignored.
	 * @param _translations - a possible array of localized element names (may be null).
	 * @return the element name for GUI purposes if available, null otherwise.
	 * @see #getElementName(char, boolean, Locale)
	 * @see #getElementName(int, boolean, Locale)
	 * @see #resolveElementNames(String, Locale)
	 */
	public static String getElementName(Element _element, boolean _defaultOnly, String[] _translations)
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
		return getElementName(index, _defaultOnly, _translations);
	}
	
	/**
	 * Replaces all occurrences of placeholders {@code "@[a-p]"} in {@code _rawString}
	 * with the respective Element type names according to the given _translations or the
	 * current locale and the user configuration (where user configuration overrides the
	 * current locale default unless {@code _translations} are explicitly given).<br/>
	 * See {@link ElementNames} class comment for the mapping of letters to
	 * element classes or flavours.
	 * @param _rawString - the string with possibly unresolved placeholders
	 * @param _translations - possible array of localised Element names (may be null).
	 * @return the string with resolved element names.
	 * @see #getElementName(char, boolean, Locale)
	 * @see #getElementName(int, boolean, Locale)
	 * @see #getElementName(Element, boolean, Locale)
	 */
	public static String resolveElementNames(String _rawString, String[] _translations)
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
						elName = getElementName(index, _translations != null, _translations);
						part = part.substring(posBrace2);
					}
				}
				else if (Character.isAlphabetic(first)) {
					elName = getElementName(part.charAt(0), _translations != null, _translations);
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

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

package lu.fisch.structorizer.helpers;

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
 *      ------			----            -----------
 *      Bob Fisch       2008-04-12      First Issue
 *      Kay Gürtzig     2017-04-23      Enh. #231 configuration of reserved words in the target language
 *      Kay Gürtzig     2017-05-11      Enh. #354/#357: field for class-specific options added
 *      Kay Gürtzig     2017-06-20      Enh. #354/#357: Structure of options field modified, method getKey() added
 *      Kay Gürtzig     2021-11-12      Enh. #967: New field syntaxChecks
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.Vector;

public class GENPlugin 
{
	public String className = null;
	public String icon = null;
	public String title = null;
	// START KGU#386 2017-04-28
	public String info = null;
	// END KGU#386 2017-04-28
	// START KGU#239 2017-04-23: Enh. #231
	public String[] reservedWords = null;
	public boolean caseMatters = true;
	// END KGU#239 2017-04-23
	// START KGU#354/KGU#395 2017-05-11: Enh. #534 Allow configurable options
	/**
	 * Vector of option specifications forming hash tables of strings each:
	 * <ul>
	 * <li>name: option key (mandatory)</li>
	 * <li>type: a simple value class name (Boolean, Character, Integer, Unsigned, Double, String)</li>
	 * <li>items: a string of the form "{item1; item2; ... itemN}"</li>
	 * <li>title: external caption (English)</li>
	 * <li>help: a tooltip help</li>
	 * <li>default: a possible default value</li>
	 * </ul>
	 */
	public Vector<HashMap<String, String>> options = new Vector<HashMap<String, String>>();
	// END KGU#354/KGU#395 2017-05-11
	
	// START KGU#968 2021-11-12: Enh. #967
	/**
	 * Optional syntax check specification (suited for Analyser and the plugin itself)
	 * @author Kay Gürtzig
	 */
	public static class SyntaxCheck {
		/**
		 * Source data type for the syntax check
		 */
		public enum Source {
			/** check of an unbroken Element line with method {@code checkSyntax(String, Element, int)} */
			STRING,
			/** check of a Line syntax tree with method {@code checkSyntax(Line, Element, int)} */
			TREE
			};
		/** Fully qualified class name for a plugin-specific syntax checker */
		public String className = null;
		/** Source for the syntax check (may be STRING or TREE) */
		public Source source = Source.STRING;
		/** Default title e.g. for the Analyser preferences */
		public String title = null;
		/** Default message for syntax errors complaints (e.g. replacing "error.syntax") */
		public String message = null;
	};
	/**
	 * Specification for an optional plugin-specific syntax check, containing a class name,
	 * a method type selection and possible default messages
	 */
	public Vector<SyntaxCheck> syntaxChecks = null;
	// END KGU#968 2021-11-12
	
	// START KGU#416 2017-06-20
	/**
	 * Returns the simplified class name of the associated class
	 * 
	 * @return Just the pure class name without package
	 */
	public String getKey()
	{
		return this.className.substring(this.className.lastIndexOf('.')+1);
	}
	// END KGU#416 2017-06-20
}

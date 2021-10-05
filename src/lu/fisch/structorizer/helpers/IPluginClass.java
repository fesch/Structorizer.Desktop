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

import java.util.HashMap;
import java.util.Vector;

import lu.fisch.structorizer.io.Ini;
import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Abstract interface for plugin-configurable classes like generators, parser etc.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-06-20      First Issue
 *      Kay Gürtzig     2021-06-08      Method setPluginOptionsFromIni() added (extracted from Diagram)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Abstract interface for plugin-configurable classes like generators, parser etc.
 * @author Kay Gürtzig
 */
public interface IPluginClass {

	/**
	 * Returns a class-specific option value if available (otherwise null)
	 * @param _optionName - option key, to be combined with the class name
	 * @param _defaultValue - a value that will be returned if the option isn't set
	 * @return an Object (of the type specified in the plugin) or null
	 */
	public abstract Object getPluginOption(String _optionName, Object _defaultValue);

	/**
	 * Allows to set a plugin-specified option before the actual task of implementing
	 * class starts
	 * @param _optionName - a key string
	 * @param _value - an object according to the type specified in the plugin
	 */
	public abstract void setPluginOption(String _optionName, Object _value);
	
	// START KGU#977 2021-06-08: Enh. #67, #357, #953 Method moved from Diagram
	// Versions/revisions KGU#395 2017-05-11, KGU#416 2017-06-20, KGU#975 2021-06-03
	/**
	 * Retrieves plugin-specific options for this instance (e.g. a generator or
	 * parser) from Ini and fills the option map.
	 * @param _specificOptions - the option specifications of the related plugin
	 * @return a StringList of conversion failures for logging.
	 */
	public default StringList setPluginOptionsFromIni(
			Vector<HashMap<String, String>> _specificOptions)
	{
		StringList problems = new StringList();
		Ini ini = Ini.getInstance();
		// START KGU#975 2021-06-03: Bugfix a fully qualified name is unsuited for propery retrieval
		String className = this.getClass().getSimpleName();
		// END KGU#975 2021-06-03
		for (HashMap<String, String> optionSpec: _specificOptions) {
			String optionKey = optionSpec.get("name");
			String valueStr = ini.getProperty(className + "." + optionKey, "");
			Object value = null;
			String type = optionSpec.get("type");
			String items = optionSpec.get("items");
			// Now convert the option into the specified type
			if (!valueStr.isEmpty() && type != null || items != null) {
				// Better we fail with just a single option than with the entire method
				try {
					if (items != null) {
						value = valueStr;
					}
					else if (type.equalsIgnoreCase("character")) {
						value = valueStr.charAt(0);
					}
					else if (type.equalsIgnoreCase("boolean")) {
						value = Boolean.parseBoolean(valueStr);
					}
					else if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer")) {
						value = Integer.parseInt(valueStr);
					}
					else if (type.equalsIgnoreCase("unsigned")) {
						value = Integer.parseUnsignedInt(valueStr);
					}
					else if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")) {
						value = Double.parseDouble(valueStr);
					}
					else if (type.equalsIgnoreCase("string")) {
						value = valueStr;
					}
				}
				catch (NumberFormatException ex) {
					String message = ex.getMessage();
					if (message == null || message.isEmpty()) message = ex.toString();
					problems.add(String.format("%s: %s on converting \"%s\" to %s for %s",
							className, message, valueStr, type, optionKey));
				}
			}
			if (value != null) {
				this.setPluginOption(optionKey, value);
			}
		}
		return problems;
	}
	// END KGU#977 2021-06-08

}

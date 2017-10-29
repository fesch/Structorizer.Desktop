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
 *      Kay Gürtzig     2017.06.20      First Issue
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

}

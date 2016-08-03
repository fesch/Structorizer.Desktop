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

import lu.fisch.utils.StringList;

/******************************************************************************************************
*
*      Author:         Kay Gürtzig
*
*      Description:    This Interface provides basic API to support language settings
*
******************************************************************************************************
*
*      Revision List
*
*      Author          Date			Description
*      ------          ----         -----------
*      Kay Gürtzig     2016.08.03   First issue
*
******************************************************************************************************
*
*      Comment:		/
*
******************************************************************************************************///
/**
 * @author Kay Gürtzig
 * To implement this interface allows a GUI class to take part in localisation via LangDialog
 * even in tricky, none-generic cases.
 * In the simplest case
 */
public interface ILangDialog {

	/**
	 * Easiest implementation is:
	 * LangDialog.setLang(this, _langfile)
	 * @param _langfile - name of a text file with translation specifications (e.g. "en.txt")
	 */
	public void setLang(String _langfile);
	/**
	 * Easiest implementation is:
	 * LangDialog.setLang(this, _lines)
	 * @param _lines - StringList of translation specifications (e.g. "Class.component.aspect=translation")
	 */
	public void setLang(StringList _lines);
	/**
	 * Opportunity to perform very specific and difficult translations with own components
	 * @param keys - sequence of key strings starting with <class_name>, "class_specific" followed by detailed aspect tags
	 * @param translation - the string to be used as translated text for the specified aspect
	 */
	public void setLangSpecific(StringList keys, String translation);

}

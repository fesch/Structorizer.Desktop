/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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

package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         kay
 *
 *      Description:    Enumeration class for the decision about preferred Case editor use
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2025-08-01      Introduced on behalf of issue #915/#1198
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      Intended to be used in ComboBoxes
 *
 ******************************************************************************************************///

/**
 * Enum class offering the preference options for the use of a dedicated editor for Case elements
 * 
 * @author Kay Gürtzig
 */
public enum CaseEditorChoice {
	NEVER("never"),
	NON_EMPTY("non-empty"),
	ALWAYS("always");
	
	private String text;
	private CaseEditorChoice(String caption) {
		text = caption;
	}
	public String toString()
	{
		return text;
	}
	public void setText(String _caption)
	{
		text = _caption;
	}
}

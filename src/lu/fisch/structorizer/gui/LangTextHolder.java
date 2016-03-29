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
 *      Description:    Light-weight helper class for LangDialog, serves as hlder for translated texts
 *                      to be used e.g. as messages but don't require a Swing class like JLabel
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Kay Gürtzig     2016.03.25      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

/**
 * A simple String wrapper responding to the messages getText() and setText() in order
 * to serve as light-weight translation holder compatible to the language localisation
 * mechanism of LangDialog. Translations for messages and other texts not permanently
 * visible in the GUI should better be stored in an instance of this class than e,g, in a
 * hidden JLabel.
 * @author Kay Gürtzig
 *
 */
public class LangTextHolder
{
	private String text = "";
	public LangTextHolder() {}
	public LangTextHolder(String _text)
	{
		this.text = _text;
	}
	public void setText(String _text)
	{
		this.text = _text;
	}
	public String getText()
	{
		return this.text;
	}
}

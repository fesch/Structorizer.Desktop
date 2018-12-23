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
package lu.fisch.structorizer.locales;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Event class to notify a locale (language) change.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018-10-08      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.EventObject;

/**
 * Event class signaling a locale change in the source object
 * @author Kay Gürtzig
 * @see LangTextHolder#addLangEventListener(LangEventListener)
 */
@SuppressWarnings("serial")
public class LangEvent extends EventObject {

	public LangEvent(LangTextHolder source) {
		super(source);
	}

	/**
	 * Same as {@link #getSource()}{@code .getText()}.
	 * @return the new localized text of the source object 
	 */
	public String getText()
	{
		return ((LangTextHolder)getSource()).getText();
	}
	
}

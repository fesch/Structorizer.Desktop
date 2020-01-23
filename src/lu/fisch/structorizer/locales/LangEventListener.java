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
 *      Description:    Listener interface for receiving locale change events (LangEvents) on e.g. LangTextHolders
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018.10.08      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * The listener interface for receiving Structorizer locale events. The class that is interested in
 * processing a locale event implements this interface, and the object created with that class is
 * registered with a {@link LangTextHolder}, using the component's addLangEventListener method.
 * When the {@link LangEvent} occurs, that object's LangChanged method is invoked.
 * @author Kay Gürtzig
 *
 */
public interface LangEventListener {

	/**
	 * Invoked when the source object of event {@code evt}, usually a {@link LangTextHolder}, was affected
	 * by a Locale change.
	 * @param evt - The language event
	 */
	public void LangChanged(LangEvent evt);
	
}

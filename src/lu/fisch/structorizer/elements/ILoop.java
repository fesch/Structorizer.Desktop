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

package lu.fisch.structorizer.elements;

/*
 ******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This interface is to facilitate unified handling of different loop types.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author           Date            Description
 *      ------           ----            -----------
 *      Kay Gürtzig      2015.11.30      First issue
 *      Kay Gürtzig      2016.10.13      Enh. #270: method isDisabled() added
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      
 *      The interface eases the detection of different loop classes (For, While, Repeat, Forever) and
 *      the access to the incorporated Subqueue without differentiated type casting and code duplication
 *
 ******************************************************************************************************
 */

/**
 * @author Kay Gürtzig
 * Implementors of the interface are Element subclasses representing some kind of loop. This interface
 * presents the common behaviour
 */
public interface ILoop {

	public Subqueue getBody();
	// START KGU#277 2016-10-13: Enh. #270 (needed for a generator access)
	public boolean isDisabled();
	// END KGU#277 2016-10-13
	
}

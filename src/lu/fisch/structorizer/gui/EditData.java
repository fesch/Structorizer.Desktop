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
 *      Author:         Bob Fisch
 *
 *      Description:    This is a simple data class, defining what data the edit dialog
 *						of an element is returning.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.29      First Issue
 *      Kay Gürtzig     2015.10.12      Field for breakpoint control added (KGU#43)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import lu.fisch.utils.*;

public class EditData {

	public String title = new String();

	public StringList text = new StringList();
	public StringList comment = new StringList();
	// START KGU#3 2015-10-25
	public StringList forParts = new StringList();
	public boolean forPartsConsistent = false;
	// END KGU#3 2015-10-25
	
	// START KGU#43 2015-10-12
	public boolean breakpoint = false;
	// END KGU#43 2015-10-12
	
	public boolean result = false;
	
}

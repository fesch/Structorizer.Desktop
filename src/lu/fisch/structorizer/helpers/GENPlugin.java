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
 *      Bob Fisch       2008.04.12      First Issue
 *      Kay Gürtzig     2017.04.23      Enh. #231 configuration of reserved words in the target language
 *      Kay Gürtzig     2017.05.11      Enh. #354/#357: field for class-specific options added
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.HashMap;

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
	 * Maps plugin-specific option names to simple value class names (Boolean, Integer, Double, String)
	 * FIXME: This is still insufficient for setting up corresponding dialog elements 
	 */
	public HashMap<String, String> options = new HashMap<String, String>();
	// END KGU#354/KGU#395 2017-05-11
}

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

package lu.fisch.structorizer.executor;

import bsh.Interpreter;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.utils.StringList;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This is a simple data class, containing the execution context of a program or
 *                      routine having called a subroutine.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author			Date			Description
 *      ------			----			-----------
 *      Kay Gürtzig		2015.11.13		First Issue
 *      Kay Gürtzig		2015.11.26		Extended by loopDepth (needed for the JUMP execution)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

public class ExecutionStackEntry {
	
	public Root root;					// The caller root itself (only necessary to preserve flags?)
	public StringList variables;		// The variable names used up to the suspending call
	public Interpreter interpreter;		// The execution context (containing variable values etc.)
	// START KGU#78 2015-11-25
	public int loopDepth;				// The current nesting level of loops
	// END KGU#78 2015-11-25

	// START KGU#78 2015-11-25
	//public ExecutionStackEntry(Root _root, StringList _variables, Interpreter _interpreter)
	public ExecutionStackEntry(Root _root, StringList _variables, Interpreter _interpreter, int _loopDepth)
	// END KGU#78 2015-11-25
	{
		_root.isCalling = true;
		this.root = _root;
		this.variables = _variables;
		this.interpreter = _interpreter;
		// START KGU#78 2015-11-25
		this.loopDepth = _loopDepth;
		// END KGU#78 2015-11-25
	}

}

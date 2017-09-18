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

import java.util.HashMap;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This class represents the execution context of a program or
 *                      subroutine (to be renamed in ExecutionContext)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author			Date			Description
 *      ------			----			-----------
 *      Kay Gürtzig		2015.11.13		First Issue
 *      Kay Gürtzig		2015.11.26		Extended by loopDepth (needed for the JUMP execution)
 *      Kay Gürtzig     2016.12.12      Issue #307: Extended by forLoopVars
 *      Kay Gürtzig     2017.04.21      Enh. #389: Extensions for import calls, conversion into a context cartridge
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      2017.04.22 / Kay Gürtzig
 *      - Originally, this class was only instantiated on calling subroutines, but it is now used as
 *        context cartridge from the first execution activity on and is only stacked and replaced on
 *        subroutine or import calls. Hence it will be renamed in ExecutionContext
 *
 ******************************************************************************************************///

import bsh.Interpreter;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.utils.StringList;

public class ExecutionStackEntry {
	
	/**
	 * The currently executed Root
	 */
	public Root root;
	/**
	 * List of the names of already assigned variables and defined constants
	 * @see #constants
	 */
	public StringList variables = new StringList();
	// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
	/**
	 * Hierarchy of FOR loop variables within this stack frame
	 */
	public StringList forLoopVars = new StringList();
	// END KGU#307 2016-12-12
	// START KGU#375 2017-04-21: Enh. #388 Support the concept of variables
	/**
	 * Maps constant names to the respective defined values
	 * @see #variables 
	 */
	public HashMap<String, Object> constants = new HashMap<String, Object>();
	// END KGU#375 2017-04-21
	// START KGU#388 2017-09-14: Enh. #423 Support dynamic type declarations
	/**
	 * Maps variable, constant, and type names to type description entries
	 */
	public HashMap<String, TypeMapEntry> dynTypeMap = new HashMap<String, TypeMapEntry>();
	// END KGU#375 2017-04-21
	
	/**
	 * The BeanShell interpreter used to execute instructions and thereby holding the
	 * accumulated context (built-in routines, variable values etc.)
	 */
	public Interpreter interpreter = new Interpreter();
	// START KGU#78 2015-11-25
	/**
	 * The current nesting level of loops
	 */
	public int loopDepth = 0;
	// END KGU#78 2015-11-25
	// START KGU#376 2017-04-21: Enh. #389
	/**
	 * Lists the names of those Root that have directly or indirectly been imported
	 * into the currently executed Root. Needed to update the imported variables
	 * of all relevant imported Roots whenever a context change is going to happen
	 * (i.e. on calling or importing another diagram or when the execution of a Root
	 * terminates.
	 * @see Executor#importMap
	 */
	public StringList importList = new StringList();
	// END KGU#376 2017-04-21
	// START KGU#384 2017-04-22: Redesign of execution context
	/**
	 * Signals whether a return instruction has been carried out 
	 * @see #returnedValue
	 */
	public boolean returned;
	/**
	 * Holds the prepared return value (if any)
	 */
	public Object returnedValue = null;

	public ExecutionStackEntry(Root _root)
	{
		root = _root;
	}
	public ExecutionStackEntry(Root _root, StringList _importList)
	{
		root = _root;
		if (_importList != null) {
			importList = _importList;
		}
	}
	// END KGU 2017-04-22
	
	// START KGU#78 2015-11-25
	//public ExecutionStackEntry(Root _root, StringList _variables, Interpreter _interpreter)
	// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
	//public ExecutionStackEntry(Root _root, StringList _variables, Interpreter _interpreter, int _loopDepth)
	// START KGU#375/KGU#376 2017-04-21: Enh. #388, #389
	//public ExecutionStackEntry(Root _root, StringList _variables, Interpreter _interpreter, int _loopDepth, StringList _forLoopVars)
	@Deprecated
	public ExecutionStackEntry(Root _root, StringList _variables, Interpreter _interpreter, int _loopDepth, StringList _forLoopVars, HashMap<String, Object> _constants, StringList _imported)
	// END KGU#375/KGU#376 2017-04-21
	// END KGU#307 2016-12-12
	// END KGU#78 2015-11-25
	{
		_root.isCalling = true;
		this.root = _root;
		this.variables = _variables;
		this.interpreter = _interpreter;
		// START KGU#78 2015-11-25
		this.loopDepth = _loopDepth;
		// END KGU#78 2015-11-25
		// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
		this.forLoopVars = _forLoopVars; 
		// END KGU#307 2016-12-12
		// START KGU#376 2017-04-21: Enh. #389
		this.constants = _constants;
		this.importList = _imported;
		// END KGU#376 2017-04-21
	}

}

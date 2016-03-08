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

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This interface represents the fundamental capabilities of a subroutine pool.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author           Date            Description
 *      ------           ----            -----------
 *      Kay Gürtzig      2015.11.25      First issue
 *      Kay Gürtzig      2016.03.08      Method clearExecutionStatus added (for Enhancement #77)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      
 *      The interface facilitates the retrieval of callable subroutines for execution mode
 *
 ******************************************************************************************************///

import java.util.Vector;

import lu.fisch.structorizer.elements.Root;

/**
* Implementing classes may provide diagram Routes by routine signature
* @author Kay Gürtzig
*/
public interface IRoutinePool {

	/**
	 * Gathers all subroutine diagrams responding to the name passed in. 
	 * @param rootName - a String the Root objects looked for ought to respond to as method name
	 * @return a collection of Root objects responding to the passed-in name
	 */
	public Vector<Root> findRoutinesByName(String rootName);

	/**
	 * Gathers all subroutine diagrams responding to the name passed in. 
	 * @param rootName - a String the Root objects looked for ought to respond to as method name
	 * @param argCount - number of parameters required
	 * @return a collection of Root objects meeting the specified signature
	 */
	public Vector<Root> findRoutinesBySignature(String rootName, int argCount);

	// START KGU#117 2016-03-08: Introduced on occasion of Enhancement #77
	/**
	 * Clears the execution status of all routines in the pool.
	 */
	public void clearExecutionStatus();
	// END KGU#117 2016-03-08
}
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
 *      Description:    Inteface providing an API for retrieving and executing DiagramContrller functions.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.06.29      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.HashMap;

/**
 * Interface allowing a DiagramController to provide functions returning a result. Since
 * function calls may be nested, it's important to provide a map of provided functions
 * such that relevant calls may be identified within a complex expression and then be
 * pre-interpreted.
 * @author Kay Gürtzig
 */
public interface FunctionProvidingDiagramController extends DiagramController {

	@SuppressWarnings("serial")
	public class FunctionException extends RuntimeException {

		public FunctionException() {
	        super();
	    }

		public FunctionException(Throwable throwable) {
	        super(throwable);
	    }

		public FunctionException(String msg) {
	        super(msg);
	    }

	    public FunctionException(String msg, Throwable throwable) {
	        super(msg, throwable);
	    }
	}
	
    /**
     * Returns a map associating a (lower-case) function name to an array of
     * argument classes and a result class if such a function is implemented.
     * @param name - the function name
     * @return Array containing the boxing classes of all arguments and of the result (or null
     * if no such function is provided)
     * @see #execute(String, Object[])
     */
    public HashMap<String, Class<?>[]> getFunctionMap();
    /**
     * Executes a function registered in the function map (obtainable by {@link #getFunctionMap()})
     * @param name - the function name (lower-case)
     * @param arguments - Array of the argument values
     * @return the obtained result value (as Object) 
     * @throws FunctionException
     * @see #getFunctionMap()
     */
    public Object execute(String name, Object[] arguments) throws FunctionException;
}

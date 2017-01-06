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
 *      Description:    Abstract class for all Elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016.12.16      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * @author Kay Gürtzig
 * Intended to inform the RoutinePoolListener about a change of the set of routines held.
 *
 */
public interface IRoutinePoolListener {
	
	public void routinePoolChanged(IRoutinePool _source);

}

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
 *      Description:    Pseudo listener interface for IRoutinePool changes (set of Roots or selection)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016-12-16      First Issue
 *      Kay Gürtzig     2018-12-21      Interface modified (flag values introduced and new method signature)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * @author Kay Gürtzig
 * Pseudo listener interface for IRoutinePool changes (set of Roots or selection)
 */
public interface IRoutinePoolListener {
	
	// START KGU#624 2018-12-21: Enh. #655
	/** Flag for changes of the set of routines in the pool */
	public static final int RPC_POOL_CHANGED = 0x1;
	/** Flag for changes of the selection in the pool */
	public static final int RPC_SELECTION_CHANGED = 0x2;
	// END KGU#624 2018-12-21
	
	/**
	 * Notification method<br/>
	 * Intended to inform the RoutinePoolListener about a change of the set of routines held or
	 * or of a selection change.
	 * @param _source - The notifying routine pool (implementer of {@link IRoutinePool})
	 * @param _flags - any combination of {@link #RPC_POOL_CHANGED} or {@link #RPC_SELECTION_CHANGED}
	 */
	// START KGU#624 2018-12-21: Enh. #655
	//public void routinePoolChanged(IRoutinePool _source);
	public void routinePoolChanged(IRoutinePool _source, int _flags);
	// END KGU#624 2018-12-21

}

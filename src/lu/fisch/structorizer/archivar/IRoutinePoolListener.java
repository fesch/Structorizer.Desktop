/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
package lu.fisch.structorizer.archivar;

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
 *      Kay Gürtzig     2019-01-04      Enh. #657: new flag for changed positions (to be reflected in groups)
 *      Kay Gürtzig     2019-01-12      Enh. #662/2: new flag for group colour or visibility change
 *      Kay Gürtzig     2019-03-13      Enh. #698: Moved from executor to archivar package
 *      Kay Gürtzig     2021-02-28      Enh. #410: Two minor-impact change flags added in order to reduce
 *                                      RPC_POOL_CHANGED use
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
	// START KGU#626 2019-01-04: Enh. 657 - for a low-impact refresh of arranger indices
	/** Flag for changes of the diagram positions on the Arranger surface */
	public static final int RPC_POSITIONS_CHANGED = 0x4;
	// END KGU#626 2019-01-04
	// START KGU#630 2019-01-12: Enh. #662/2 - Notification of color or visibility changes of a group
	/** Flag for mere group colour or visibility changes */
	public static final int RPC_GROUP_COLOR_CHANGED = 0x8;
	// END KGU#630 2019-01-12
	// START KGU#408 2021-02-28: Enh. #410 - we need more relevance differentiation
	/** Flag for diagram or group field changes of minor impact */
	public static final int RPC_STATUS_CHANGED = 0x10;
	/** Flag signalling that possibly the order of groups or diagrams is affected */
	public static final int RPC_NAME_CHANGED = 0x20;
	// END KGU#408 2021-02-28
	
	/**
	 * Notification method<br/>
	 * Intended to inform the RoutinePoolListener about a change of the set of held routines or
	 * of a selection (or possibly an arrangement) change.
	 * @param _source - The notifying routine pool (implementer of {@link IRoutinePool})
	 * @param _flags - any combination of {@link #RPC_POOL_CHANGED}, {@link #RPC_SELECTION_CHANGED},
	 * {@link #RPC_POSITIONS_CHANGED}, or {@link #RPC_GROUP_COLOR_CHANGED}, {@link #RPC_NAME_CHANGED},
	 * {@link #RPC_STATUS_CHANGED}.
	 */
	// START KGU#624 2018-12-21: Enh. #655
	//public void routinePoolChanged(IRoutinePool _source);
	public void routinePoolChanged(IRoutinePool _source, int _flags);
	// END KGU#624 2018-12-21

}

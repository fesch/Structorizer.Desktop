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

package lu.fisch.structorizer.generators;

/******************************************************************************************************
*
*      Author:         Kay Gürtzig
*
*      Description:    Entry class for topological sorting of called subroutines in Generator
*
******************************************************************************************************
*
*      Revision List
*
*      Author          Date            Description
*      ------          ----            -----------
*      Kay Gürtzig     2016-07-19      First issue (for enh. #160)
*      Kay Gürtzig     2016-08-10      Modification for bugfix #228 (KGU#237)
*      Kay Gürtzig     2019-12-03      Issue #766: Sorted caller set to achieve deterministic routine order
*      Kay Gürtzig     2020-03-03      Fix for defective bugfix #228
*
******************************************************************************************************
*
*      Comment:	
*      
******************************************************************************************************///

import java.util.TreeSet;

import lu.fisch.structorizer.elements.Root;

/**
 * Entry class for a topological map of called subroutines or required includables in {@link Generator}.
 * Contains references to dependents and a reference counter (i.e. the number of Roots this routine
 * depends on).
 */
final class SubTopoSortEntry {
	// START KGU#754 2019-12-03: Issue #766 - we want deterministic routine orders
	//public Set<Root> callers = new HashSet<Root>();
	public TreeSet<Root> callers = new TreeSet<Root>(Root.SIGNATURE_ORDER);
	// END KGU#754 2019-12-03
	public int nReferingTo = 0;	// number of different(!) routines being called
	
	SubTopoSortEntry(Root _caller)
	{
		// START KGU#237 2020-03-03: Bugfix #228 - apparently forgotten precaution (caused NullPointerException)
		//callers.add(_caller);
		addCaller(_caller);
		// END KGU#237 2020-03-03
	}
	
	public void addCaller(Root _caller)
	{
		// START KGU#237 2016-08-10: Bugfix #228 preparation
		//callers.add(_caller);
		if (_caller != null)
		{
			callers.add(_caller);
		}
		// END KGU#237 2016-08-10
	}
	
}

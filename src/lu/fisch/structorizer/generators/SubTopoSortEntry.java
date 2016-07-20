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
*      Author          Date			Description
*      ------          ----			-----------
*      Kay Gürtzig     2016.07.19      First issue (for enh. #160)
*
******************************************************************************************************
*
*      Comment:	
*      
******************************************************************************************************///

import java.util.HashSet;
import java.util.Set;

import lu.fisch.structorizer.elements.Root;

final class SubTopoSortEntry {
	public Set<Root> callers = new HashSet<Root>();
	public int nReferingTo = 0;	// number of different(!) routines being called
	
	SubTopoSortEntry(Root _caller)
	{
		callers.add(_caller);
	}
	
	public void addCaller(Root _caller)
	{
		callers.add(_caller);
	}
	
}

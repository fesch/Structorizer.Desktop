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
package lu.fisch.structorizer.elements;

import lu.fisch.graphics.Rect;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This interface is to facilitate unified handling of different forking element types.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.02.08      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Implementors of the interface are {@link Element} subclasses representing some
 * kind of forking (i.e. Alternative or Selection).
 * This interface presents the common behaviour.
 * @author Kay Gürtzig
 */
public interface IFork {
	
	/**
	 * Returns a copy of the (relocatable i. e. 0-bound) extension rectangle
	 * of the head section (condition / selection expression and branch labels). 
	 * @return a rectangle starting at (0,0) and spanning to (width, head height) 
	 */
	public Rect getHeadRect();

}

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
 *      Kay Gürtzig     2017-02-08      First Issue
 *      Kay Gürtzig     2025-07-31      New methods for Enh. #1197 (branch head colouring)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.Color;
import lu.fisch.graphics.Rect;

/**
 * Implementors of the interface are {@link Element} subclasses representing some
 * kind of forking (i.e. Alternative or Multi-Selection).
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

	// START KGU#1182 2025-07-31: Enh. #1197 Allow to subselect branch headers
	/**
	 * @return the actual number of branches (including empty ones)
	 */
	public int getBranchCount();
	
	/**
	 * Decides whether the given relative coordinates (with respect to the
	 * top left corner of the element hits a branch selector head (triangle
	 * or trapezium) and registers/switches its selection in this case.
	 * Otherwise a possible existing branch head selection will be lifted.
	 * 
	 * @param _relX - horizontal mouse distance from left element border
	 * @param _relY - vertical mouse distance from upper element border
	 * @return {@code true} if a branch header was modified, {@code false}
	 *     otherwise.
	 *     
	 * @see #getSelectedBranchHead()
	 */
	public boolean selectBranchHead(int _relX, int _relY);
	
	/**
	 * @return the index of a selected branch head (if there is one, otherwise
	 *     -1.
	 *     
	 * @see #selectBranchHead(int, int)
	 */
	public int getSelectedBranchHead();
	
	/**
	 * Checks whether the specified branch header has an associated colour
	 * and returns it if so. Otherwise returns {@code null}.
	 * 
	 * @param _branchIndex - the index of the interesting branch (starting
	 *     with 0 from the left-most one)
	 * @return either a {@link Color} or {@code null}
	 * 
	 * @see #setBranchHeadColor(int, Color)
	 * @see #getHexBranchColorList()
	 */
	public Color getBranchHeadColor(int _branchIndex);
	
	/**
	 * Attempts to associate the {@code _branchIndex}'s branch header with
	 * the given colour {@code _branchColor}. If {@code _brachColor} is
	 * {@code null} then the respective branch head colour will be cleared.
	 * 
	 * @param _branchIndex - number of the branch
	 * @param _branchColor - the colour to be used.
	 * @return whether the operation succeeded (will fail if the index is
	 *    out of bounds)
	 * 
	 * @see #getBranchHeadColor(int)
	 */
	public boolean setBranchHeadColor(int _branchIndex, Color _branchColor);
	
	/**
	 * Returns a string containing a comma-separated hexadecimal RGB value
	 * sequence of the associated branch head colours. If colours are missing
	 * then the respective entry will be empty, e.g. "AD3F0A,FFFFFF,,7F7F00".
	 * 
	 * @return the composed string
	 * 
	 * @see #getBranchHeadColor(int)
	 */
	public String getHexBranchColorList();
	// END KGU#1182

}

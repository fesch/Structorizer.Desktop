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

//import lu.fisch.structorizer.gui.SelectedSequence;

/******************************************************************************************************
*
*      Author:         Kay Guertzig
*
*      Description:    This interface allows sequences of Elements to be handled in a unified way.
*
******************************************************************************************************
*
*      Revision List
*
*      Author           Date            Description
*      ------           ----            -----------
*      Kay Gürtzig      2015.11.23      First issue (KGU#87).
*
******************************************************************************************************
*
*      Comment:		/
*
******************************************************************************************************///

/**
 * @author Kay Gürtzig
 *
 */
public interface IElementSequence {

	/**
	 * Returns the number of Elements held
	 * @return number of elements
	 */
	public abstract int getSize();

	public abstract int getIndexOf(Element _ele);
	
	public abstract Element getElement(int _index);
	
	/**
	 * Appends the given _element to the already held elements
	 * @param _element
	 */
	public abstract void addElement(Element _element);

	/**
	 * Inserts the given _element before child no. _where (if 0 <= _where <= this.getSize()).
	 * If _element is another IElementContainer, however, all children of _element will be
	 * inserted before the child _where, instead.
	 * @param _element - an Element to be inserted (or the children of which are to be inserted here)
	 * @param _where - index of the child, which _element (or _element's children) is to inserted before  
	 */
	public abstract void insertElementAt(Element _element, int _where);

	/**
	 * Clears this of all elements (maybe just element references) 
	 */
	public abstract void clear();

	/**
	 * Removes all elements (by default similar to clear()) 
	 */
	public abstract void removeElements();
	
	public abstract void removeElement(Element _element);
	
	public abstract void removeElement(int _index);
	
}

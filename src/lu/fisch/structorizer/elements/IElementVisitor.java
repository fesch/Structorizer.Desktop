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
 *      Description:    Abstract class for all Elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Kay Gürtzig     11.08.2016      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************/

/**
 * @author kay
 * Interface for visitor objects on diagram traversal, offering a visit in preOrder
 * and another in postOrder e.g. for information gathering, status changes or the like.
 * For access to private attributes the visitor may need a privileged partner object.
 */
public interface IElementVisitor {
	/**
	 * Method is called on tree traversal BEFORE substructure is traversed.
	 * @param _ele - the diagram element being entered
	 * @return false if the traversal is to be exited after the call
	 */
	public boolean visitPreOrder(Element _ele);
	/**
	 * Method is called on tree traversal AFTER substructure is traversed.
	 * @param _ele - the diagram element being left
	 * @return false if the traversal is to be exited after the call
	 */
	public boolean visitPostOrder(Element _ele);
}

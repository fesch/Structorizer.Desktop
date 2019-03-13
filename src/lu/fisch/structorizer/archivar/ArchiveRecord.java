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

import java.awt.Point;

import lu.fisch.structorizer.elements.Root;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Class representing the essential information for arrangement archive items
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-03-09      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Represents the essential information for arrangement archive items
 * @author Kay Gürtzig
 */
public class ArchiveRecord
{
	/** The diagram (should not be null, usually) */
	public Root root = null;
	/** The graphical top-left location of the diagram */
	public Point point = null;

	/**
	 * Creates an Archive entry for diagram {@code _root} without position
	 * @param _root - the {@link Root} object of the archive entry
	 */
	public ArchiveRecord(Root _root) {
		this.root = _root;
	}

	/**
	 * Creates an Archive entry for diagram {@code _root} without position
	 * @param _root - the {@link Root} object of the archive entry
	 * @param _point - the location of the diagram
	 */
	public ArchiveRecord(Root _root, Point _point) {
		this.root = _root;
		this.point = _point;
	}
	
	/**
	 * Creates a new archive record from {@code other} sharing the {@link Root} but
	 * copying the {@link Point}.
	 * @param other - another ArchiveRecord
	 */
	public ArchiveRecord(ArchiveRecord other) {
		this(other.root, new Point(other.point));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "(" + this.point + ", " + this.root + ")";
	}

}

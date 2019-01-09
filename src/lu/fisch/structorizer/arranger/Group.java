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
package lu.fisch.structorizer.arranger;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Group of Diagram objects in Arranger.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018-12-23      First Issue (on behalf of enh. #657)
 *      Kay Gürtzig     2019-01-05      Substantial tuning for enh. #657
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lu.fisch.structorizer.elements.Root;

/**
 * Group of Diagram objects in Arranger
 * @author Kay Gürtzig
 */
public class Group {

	public static final String DEFAULT_GROUP_NAME = "%%%";
	
	/**
	 * The Name of the group
	 */
	private String name = null;
	
	/**
	 * The path of the arranger list file this group came from or was recently
	 * saved to.<br/>
	 * Is null if the group has no file representation. The path may be a
	 * virtual path into an arrz file.
	 */
	private String filePath = null;
	
	/**
	 * The member diagrams of the group. This membership is not exclusive, i.e. there might be
	 * other groups sharing some diagrams.
	 */
	private final Set<Diagram> diagrams = new HashSet<Diagram>();
	
	/**
	 * True if the set of diagrams has changed. Does not reflect location modifications.
	 * @see #hasChanged()
	 */
	public boolean membersChanged = false;
	/**
	 * True as soon as a movement of some member diagram is detected. Is to be cleared when
	 * this group is saved.
	 * @see #hasChanged()
	 */
	public boolean membersMoved = false;

	/**
	 * Cache for the sorted list of referenced {@link Root}s
	 */
	// This redundant w.r.t. diagrams but improves performance of the Arranger index
	private final Vector<Root> routines = new Vector<Root>();

	public static final Comparator<Group> NAME_ORDER =
			new Comparator<Group>() {
		public int compare(Group group1, Group group2)
		{
			return group1.getName().compareToIgnoreCase(group2.getName());
		}
	};
	
	/**
	 * Creates a new empty group with the given name
	 * @param _name - group name
	 */
	public Group(String _name) {
		this.name = _name;
	}
	
	/**
	 * Creates a new empty group with the given name and file reference
	 * @param _name - group name
	 * @param _arrPath - path to the .arr file of the source arrangement (if loaded with arrangement)
	 */
	public Group(String _name, String _arrPath) {
		this.name = _name;
		this.filePath = _arrPath;
	}
	
	/**
	 * Creates a new empty group with the given {@code _name} name and containing
	 * the given {@code _diagrams}.
	 * @param _name - group name
	 * @param _diagrams - the (initial) set of {@link Diagram}s
	 */
	public Group(String _name, Collection<Diagram> _diagrams) {
		this.name = _name;
		this.diagrams.addAll(_diagrams);
		if (!_diagrams.isEmpty()) {
			this.membersChanged = true;
		}
	}
	
	/**
	 * Creates a new group with the given {@code _name} name and containing
	 * the given {@code _diagrams}.
	 * @param _name - group name
	 * @param _diagrams - the (initial) set of {@link Diagram}s
	 * @param _provenance - the {@link File} object associated with the original file
	 * (may be an .arr or an .arrz file)
	 */
	public Group(String _name, Set<Diagram> _diagrams, File _provenance) {
		this.name = _name;
		this.diagrams.addAll(_diagrams);
		this.filePath = _provenance.getAbsolutePath();
	}
	
	/**
	 * @return the name of this group
	 * @see #proposeFileName()
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * @return true if the name this group equals {@link #DEFAULT_GROUP_NAME}.
	 */
	public boolean isDefaultGroup()
	{
		return this.name.equals(DEFAULT_GROUP_NAME);
	}
	
	/**
	 * @return a variant of the name that is well suited e.g. as file name
	 * @see #getName()
	 */
	public String proposeFileName()
	{
		char[] canonical = this.name.toCharArray();
		for (int i = 0; i <- canonical.length; i++) {
			char ch = canonical[i];
			if (!Character.isAlphabetic(ch) && !Character.isDigit(ch) && ch != '.') {
				canonical[i++] = '_';
			}
		}
		return String.copyValueOf(canonical);
	}
	
	/**
	 * @return a {@link File} object with the associated absolute path of the
	 * Arranger list file this group is associated to (stemming from or last saved to)
	 * or null if the group had never been associated to an arrangement file. 
	 */
	public File getFile()
	{
		if (this.filePath != null) {
			return new File(this.filePath);
		}
		return null;
	}

	/**
	 * Returns a {@link File} object for the assumed enveloping compressed
	 * Arranger archive this group's Arranger list file and diagram files
	 * are residing in. Returns null if the group is not residing in an
	 * arrz file.
	 * @return either a {@link File} object for an arrz file or null
	 */
	public File getArrzFile()
	{
		File file = this.getFile();
		if (file != null && !file.exists() && this.filePath.toLowerCase().contains(".arrz")) {
			file = file.getParentFile();
			if (!file.isFile() || !file.getName().toLowerCase().endsWith(".arrz")) {
				file = null;
			}
		}
		else {
			file = null;
		}
		return file;
	}
	
	/**
	 * Overwrites the associated file path with the arr file path derived from
	 * {@code arrFile} and {@code arrzFile}.<br/>
	 * If {@code arrzFile} is null the path will be that of {@code arrFile}, otherwise
	 * a virtual path composed from the {@code arrzFile} path and the last path element
	 * of the{@code arrFile} path is used.<br/>
	 * As a side-effect resets the modification flag.
	 * @param arrFile - {@link File} object referring to the arrangement list file
	 * @param arrzFile - {@link File} object referring to the arrangement archive or null
	 */
	protected void setFile(File arrFile, File arrzFile)
	{
		String arrPath = arrFile.getAbsolutePath();
		if (arrzFile != null) {
			arrPath = arrzFile.getAbsolutePath() + File.separator + arrFile.getName();
		}
		this.filePath = arrPath;
		this.membersChanged = false;
		// This is the (only) chance to reset the membersMoved flag
		this.membersMoved = this.positionsChanged();
	}
	
	/**
	 * Adds the given {@link Diagram} {@code _diagram} if it hadn't already been
	 * a member.
	 * @param _diagram - the diagram to be added
	 * @return true if the diagram has been added and the group has changed 
	 */
	public boolean addDiagram(Diagram _diagram)
	{
		boolean added = this.diagrams.add(_diagram);
		added = _diagram.addToGroup(this) || added;
		if (added) {
			routines.add(_diagram.root);
			this.updateSortedRoots(false);
			this.membersChanged = true;
		}
		return added;
	}

	/**
	 * Removes the given {@link Diagram} {@code _diagram} if it was a member
	 * @param _diagram - the potential member to be removed
	 * @return true if the diagram had been a member and the group has changed 
	 */
	public boolean removeDiagram(Diagram _diagram)
	{
		boolean removed = this.diagrams.remove(_diagram);
		removed = _diagram.removeFromGroup(this) || removed;
		if (removed) {
			routines.remove(_diagram.root);
			this.membersChanged = true;
		}
		return removed;
	}
	
	/**
	 * Forms the union of the already owned diagrams and the ones given in the
	 * parameter collection _diagrams.
	 * @param _diagrams - a collection of {@link Diagram} objects
	 * @return if the group has changed
	 */
	public boolean addAllDiagrams(Collection<? extends Diagram> _diagrams)
	{
		boolean added = this.diagrams.addAll(_diagrams);
		if (added) {
			for (Diagram diagr: _diagrams) {
				diagr.addToGroup(this);
			}
			this.membersChanged = true;
			this.updateSortedRoots(true);
		}
		return added;
	}

	/**
	 * Removes all member {@link Diagram}s without changing the path information etc.
	 * @return true if all diagrams have been removd. 
	 */
	public boolean clear()
	{
		boolean done = true;
		Diagram[] members = this.diagrams.toArray(new Diagram[this.diagrams.size()]);
		for (int i = 0; i < members.length; i++) {
			done = removeDiagram(members[i]) && done;
		}
		return done;
	}
	
	/**
	 * Returns true if this group contains the given @{@link Diagram} {@code _diagram}.
	 * @param _diagram - an interesting {@link Diagram} object
	 * @return true if the group contains the specified diagram
	 */
	public boolean containsDiagram(Diagram _diagram)
	{
		return this.diagrams.contains(_diagram);
	}
	
	/**
	 * @return a copy (!) of the set of member diagrams
	 */
	public Set<Diagram> getDiagrams()
	{
		return new HashSet<Diagram>(this.diagrams);
	}

	/**
	 * @return true if this group contains no diagrams.
	 */
	public boolean isEmpty()
	{
		return this.diagrams.isEmpty();
	}
	
	/**
	 * @return the number of diagrams in this group (its cardinality)
	 */
	public int size()
	{
		return this.diagrams.size();
	}
	
	/**
	 * Returns a sorted list of {@link Roots} held by Arranger.
	 * @return vector of {@link Roots}, sorted by {@link Root#SIGNATURE_ORDER}
	 */
	public Vector<Root> getSortedRoots()
	{
		if (routines.size() != diagrams.size()) {
			// There is an obvious discrepancy, so set up the sorted list again from scratch
			updateSortedRoots(true);
		}
		return routines;
	}

	/**
	 * 
	 */
	protected void updateSortedRoots(boolean completely) {
		if (completely) {
			routines.clear();
			for (Diagram diagram: this.diagrams) {
				routines.add(diagram.root);
			}
		}
		Collections.sort(routines, Root.SIGNATURE_ORDER);
	}
	
	private boolean positionsChanged()
	{
		for (Diagram diagr: this.diagrams) {
			if (diagr.wasMoved) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the set of member diagrams has changed or some of the member diagrams have moved since
	 * loading or last save.
	 * @return true if there wer some kind of changes, false otherwise 
	 * @see #membersChanged
	 */
	public boolean hasChanged()
	{
		if (!this.membersMoved && positionsChanged()) {
			this.membersMoved = true;
		}
		return membersChanged || this.membersMoved;
	}
	
	@Override
	public String toString()
	{
		String prefix = "";
		if (this.hasChanged()) {
			prefix = "*";
		}
		return prefix + this.getName() + ": " + this.size();
	}

}

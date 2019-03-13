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
 *      Kay Gürtzig     2019-01-09      Enhancements for issue #662/2 (drawing capability prepared)
 *      Kay Gürtzig     2019-01-25      Issue #668: More intelligent file name proposal for default group
 *      Kay Gürtzig     2019-02-04      Colour icon revised (now double thin border like in Arranger).
 *      Kay Gürtzig     2019-03-01      Enh. #691 Method rename() added (does only parts of what is to be done)
 *      Kay Gürtzig     2019-03-11      Modification in addDiagram() for bugfix #699
 *      Kay Gürtzig     2019-03-19      Issues #518, #544, #557: Drawing depends on visible rect now.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.AlphaComposite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;

import lu.fisch.graphics.Canvas;
import lu.fisch.graphics.Rect;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.gui.IconLoader;

/**
 * Group of Diagram objects in Arranger
 * @author Kay Gürtzig
 */
public class Group {

	public static final String DEFAULT_GROUP_NAME = "!!!";
	private static final int BUFFER = 3;
	
	// START KGU#630 2019-01-07: Enh. #622
	public static final Color[] groupColors = {
			Color.BLUE,
			Color.RED,
			Color.decode("0x008000"),	// dark green
			Color.ORANGE,
			Color.decode("0x8000FF"),	// violet
			Color.DARK_GRAY
	};
	/** Is used in a modulo way to assign every new group another color */
	private static int nextColor = 0;

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
	// This is redundant w.r.t. diagrams but improves performance of the Arranger index
	private final Vector<Root> routines = new Vector<Root>();

	/**
	 * Flag for drawing the {@link #bounds} according to enh. #662/2
	 * @see #color
	 * @see #draw(Canvas, Rectangle)
	 */
	private boolean visible = true;
	
	/**
	 * The drawing color for the {@link #bounds} according to enh. #662/2
	 * @see #visible
	 * @see #draw(Canvas, Rectangle)
	 * 
	 */
	private Color color = Color.BLUE;
	
	/**
	 * Contains the expanded icon (with color), lazy initialization.
	 */
	private ImageIcon iconColor = null;
	
	/**
	 * The cached bounding box for easier localisation
	 * @see #visible
	 * @see #color
	 */
	protected Rectangle bounds = null;
	
	/**
	 * Comparator method for case-ignorant lexicographic sorting
	 */
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
		synchronized (Group.class) {
			this.color = groupColors[nextColor++];
			if (nextColor >= groupColors.length) nextColor = 0;
		}
		this.name = _name;
	}
	
	/**
	 * Creates a new empty group with the given name and file reference
	 * @param _name - group name
	 * @param _arrPath - path to the .arr file of the source arrangement (if loaded with arrangement)
	 */
	public Group(String _name, String _arrPath) {
		synchronized (Group.class) {
			this.color = groupColors[nextColor++];
			if (nextColor >= groupColors.length) nextColor = 0;
		}
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
		synchronized (Group.class) {
			this.color = groupColors[nextColor++];
			if (nextColor >= groupColors.length) nextColor = 0;
		}
		this.name = _name;
		this.diagrams.addAll(_diagrams);
		if (!_diagrams.isEmpty()) {
			// If this group isn't temporary then register the group name with the diagrams
			if (!_name.isEmpty()) {
				for (Diagram diagr: _diagrams) {
					diagr.addToGroup(this);
				}
			}
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
		synchronized (Group.class) {
			this.color = groupColors[nextColor++];
			if (nextColor >= groupColors.length) nextColor = 0;
		}
		this.name = _name;
		this.diagrams.addAll(_diagrams);
		// If this group isn't temporary register the group name with the diagrams
		if (!_name.isEmpty()) {
			for (Diagram diagr: _diagrams) {
				diagr.addToGroup(this);
			}
		}
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
	
	// START KGU#669 2019-03-01: Enh. #691 Partial support for group renaming (needs external cooperation)
	/**
	 * Renames this group and updates the group association of the members (if the name differs).<br/>
	 * NOTE: This method does NOT:<ul>
	 * <li>check for name collisions with other groups</li>
	 * <li>modify the file path</li>
	 * <li>update external name references</li>
	 * </ul> (All this must all be done by the initiator.)
	 * @param _newName - the new group name
	 * @return true if the {@code _newName} differes from previous name.
	 * @see #getName()
	 * @see #setFile(File, File, boolean)
	 */
	protected boolean rename(String _newName)
	{
		boolean renamed = !_newName.equals(this.name);
		if (renamed) {
			for (Diagram diagr: this.diagrams) {
				diagr.removeFromGroup(this);
			}
			this.name = _newName;
			for (Diagram diagr: this.diagrams) {
				diagr.addToGroup(this);
			}
		}
		return renamed;
	}
	// END KGU#669 2019-03-01
	
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
		// For the default group, if it contains a unique main diagram, provide its name 
		if (this.isDefaultGroup()) {
			String progName = null;
			for (Diagram diagr: this.diagrams) {
				if (diagr.root.isProgram()) {
					if (progName == null) {
						// The first program diagram, retain the name for the case it remains the only one
						progName = diagr.root.getMethodName();
					}
					else {
						// There are more than one program diagrams, so give up
						progName = null;
						break;
					}
				}
			}
			if (progName != null) {
				canonical = progName.toCharArray();
			}
		}
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
	 * @see #getArrzFile()
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
	 * @see #getFile()
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
	 * @param adaptDiagrams - if true and {@code arrzFile} isn't null then the virtual file
	 * paths of all diagrams matching the previous arrz path will be updated to {@code arrzFile}.
	 * @see #getFile()
	 * @see #getArrzFile()
	 * @see Surface#renameGroup(Group, String, java.awt.Component)
	 */
	protected void setFile(File arrFile, File arrzFile, boolean adaptDiagrams)
	{
		String arrPath = arrFile.getAbsolutePath();
		if (arrzFile != null) {
			String newArrzPath = arrzFile.getAbsolutePath();
			arrPath = newArrzPath + File.separator + arrFile.getName();
			// START KGU#669 2019-0301: Enh. #691 - Adapt the diagram file paths if they have gone stale
			if (adaptDiagrams) {
				File file = this.getFile();
				if (file != null && !file.exists() && this.filePath.toLowerCase().contains(".arrz")) {
					file = file.getParentFile();
					if (file.getName().toLowerCase().endsWith(".arrz") && !file.exists()) {
						String oldArrzPath = file.getAbsolutePath();
						int pathLen = oldArrzPath.length();
						for (Root root: this.getSortedRoots()) {
							if (root.filename != null && root.filename.startsWith(oldArrzPath)) {
								root.filename = newArrzPath + root.filename.substring(pathLen);
							}
						}
					}
				}
			}
			// END KGU#669 2019-03-01
		}
		this.filePath = arrPath;
		this.membersChanged = false;
		// This is the (only) chance to reset the membersMoved flag
		this.membersMoved = this.positionsChanged();
		this.iconColor = null;
	}
	
	/**
	 * @return the bounds and background colour for this Group instance
	 * @see #setColor(Color)
	 * @see #setVisible(boolean)
	 * @see #isVisible()
	 */
	public Color getColor()
	{
		return this.color;
	}
	
	/**
	 * Sets the group bounds and background colour to the given {@code _color} 
	 * @param _color - the new Color
	 * @see #getColor()
	 * @see #setVisible(boolean)
	 * @see #isVisible()
	 */
	public void setColor(Color _color)
	{
		this.color = _color;
		this.iconColor = null;
	}
	
	/**
	 * @return true if this individual group is enabled to draw its bounds
	 * @see #setVisible(boolean)
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	public boolean isVisible()
	{
		return this.visible;
	}
	
	/**
	 * Enables or disables this individual group to draw it's bounds
	 * @param show - true enables, false disables drawing
	 * @see #isVisible()
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	public void setVisible(boolean show)
	{
		this.visible = show;
		this.iconColor = null;
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
		// START KGU#680 2019-03-11: Bugfix #699
		//added = _diagram.addToGroup(this) || added;
		// If this group isn't temporary then register the group name with the diagrams
		if (!name.isEmpty()) {
			added = _diagram.addToGroup(this) || added;
		}
		// END KGU#680 2019-03-11
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
	 * @return true if all diagrams have been removed. 
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
	 * Updates the sorted list of {@link Root}s that can be retrieved with
	 * {@link #getSortedRoots()}.
	 * @param completely - if true then {@link #routines} will be computed from scratch,
	 * otherwise it is only re-sorted.
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

	/**
	 * Checks whether some of the associated {@link Diagram}s has changed its position
	 * (e.g. since last saving or loading}
	 * @return true if any position modification had been detected.
	 */
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
	
	public void draw(Graphics _g, Rectangle _viewport)
	{
		draw(_g, 0, 0, _viewport);
	}
	
	protected void draw(Graphics _g, int _offsetX, int _offsetY, Rectangle _viewport)
	{
		Canvas canvas = new Canvas((Graphics2D) _g);
		if (this.visible) {
			getBounds(true);
			if (bounds != null) {
				Rectangle drawBounds = new Rectangle(bounds);
				if (_offsetX != 0 || _offsetY != 0) {
					drawBounds.translate(-_offsetX, -_offsetY);
				}
				
				Rect outer = new Rect(drawBounds.x - BUFFER, drawBounds.y - BUFFER,
						drawBounds.x + drawBounds.width + BUFFER, drawBounds.y + drawBounds.height + BUFFER);

				// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
				if (_viewport != null && !_viewport.intersects(outer.getRectangle()))
				{
					// Outside the visible area
					return; 
				}
				// END KGU#502/KGU#524/KGU#553 2019-03-13

				Rect inner = new Rect(drawBounds);
				canvas.setColor(color);
				canvas.drawRect(outer);
				canvas.drawRect(inner);
				((Graphics2D)_g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0625f));
				canvas.fillRect(inner);
				((Graphics2D)_g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

			}
		}
	}

	/**
	 * Returns the bounds rectangle of this group. If there hadn't been a cached bounds rectangle or {@code forceUpdate}
	 * is true then composes the group bounds from the member diagrams and caches the result. 
	 * @param forceUpdate - if true then a cached bound rectangle is ignored and overwritten
	 * @return a bounding box {@link Rectangle} in true (unzoomed) diagram coordinates
	 */
	protected Rectangle getBounds(boolean forceUpdate) {
		if (forceUpdate) {
			this.bounds = null;
		}
		if (this.bounds == null) {
			for (Diagram diagr: this.diagrams) {
				Rectangle rect = diagr.root.getRect(diagr.point).getRectangle();
				if (bounds == null) {
					bounds = rect;
				}
				else {
					bounds = bounds.union(rect);
				}
			}
		}
		return this.bounds;
	}
	
	/**
	 * Returns a group icon reflecting the file relation (none/list/archive). If {@code withColor} is true
	 * then the icon will be augmented with a symbolic bounding box icon in the group color.
	 * @param withColor - whether the icon is to contain a color-indicating extra symbol
	 * @return - the {@link ImageIcon} of the group
	 */
	public ImageIcon getIcon(boolean withColor)
	{
		int iconNo = 94;
		if (getArrzFile() != null) {
			iconNo = 96;
		}
		else if (getFile() != null) {
			iconNo = 95;
		}
		ImageIcon icon = IconLoader.getIcon(iconNo);
		if (withColor && visible) {
			if (iconColor == null) {
				int size = icon.getIconHeight();
				BufferedImage image = new BufferedImage(2 * size, size, BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = (Graphics2D) image.getGraphics();
				graphics.drawImage(icon.getImage(), 0, 0, size, size, null);
				int margin = 1 * size / 16;
				int offset = 4 * size / 16;
				graphics.setColor(color);
				graphics.fillRect(size + offset + margin , margin, size - offset - 2*margin, size - 2*margin);
				graphics.setColor(Color.WHITE);
				graphics.fillRect(size + offset + 1 + margin, 1 + margin, size - offset - 2 - 2*margin, size - 2 - 2*margin);
				graphics.setColor(color);
				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0625f));
				graphics.fillRect(size + offset + 1 + margin, 1 + margin, size - offset - 2 - 2*margin, size - 2 - 2*margin);
				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				graphics.drawRect(size + offset + 2 + margin, 2 + margin, size - offset - 5 - 2*margin, size - 5 - 2*margin);
				graphics.dispose();
				iconColor = new ImageIcon(image);
			}
			icon = iconColor;
		}
		return icon;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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

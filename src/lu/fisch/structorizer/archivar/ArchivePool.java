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
 *      Description:    Basic routine pool with lazy extraction of archives
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-03-12      First issue for Enh. Requ. #696, #697, #698
 *      Kay Gürtzig     2024-10-09      Enh. #1171: New method getPositionOf(Root) to support batch
 *                                      picture export
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.Point;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import lu.fisch.structorizer.archivar.Archivar.ArchiveIndex;
import lu.fisch.structorizer.archivar.Archivar.ArchiveIndexEntry;
import lu.fisch.structorizer.elements.Root;

/**
 * @author kay
 *
 */
public class ArchivePool implements IRoutinePool {
	
	private String name = null;

	private Archivar archivar = new Archivar();
	
	private Logger logger = Logger.getLogger(ArchivePool.class.getSimpleName());
	
	private HashSet<IRoutinePoolListener> poolListeners = new HashSet<IRoutinePoolListener>();
	
	/**
	 * Maps diagram names to all {@link ArchiveIndexEntry} elements in the ArchiveIndices
	 */
	private HashMap<String, ArchiveIndex> nameMap = new HashMap<String, ArchiveIndex>();
	
	/**
	 * Creates an empty ArchivePool with name {@code name}
	 * @param name - the archive name
	 * @see #ArchivePool(lu.fisch.structorizer.archivar.Archivar.ArchiveIndex)
	 * @see #addArchive(File, boolean)
	 */
	public ArchivePool(String name) {
		this.name = name;
	}

	/**
	 * Creates an ArchivePool from a given {@link ArchiveIndex}.
	 * The archive name will be derived from the arrangement file in
	 * {@code archiveIndex}.
	 */
	public ArchivePool(Archivar.ArchiveIndex archiveIndex) {
		if (archiveIndex.arrFile != null) {
			this.name = archiveIndex.arrFile.getName();
		}
		for (Iterator<ArchiveIndexEntry> iter = archiveIndex.iterator(); iter.hasNext();) {
			putToNameMap(iter.next());
		}
	}

	/**
	 * Returns the name of his archive pool
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#addDiagram(lu.fisch.structorizer.elements.Root)
	 */
	@Override
	public void addDiagram(Root root) {
		String name = root.getMethodName();
		ArchiveIndex index = this.nameMap.get(name);
		if (index == null) {
			this.nameMap.put(name, index = archivar.makeEmptyIndex());
		}
		if (index.addEntryFor(root, null)) {
			this.notifyPoolListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		}
	}

	@Override
	public boolean addArchive(File arrangementArchive, boolean lazy) {
		boolean added = false;
		if (this.name == null) {
			this.name = arrangementArchive.getName();
		}
		ArchiveIndex newIndex = null;
		File targetDir = Archivar.makeTempDir(arrangementArchive.getName().replace(".arrz", ".unzip"));
		if (lazy) {
			newIndex = archivar.getArrangementArchiveContent(arrangementArchive, targetDir);
		}
		else {
			newIndex = archivar.unzipArrangementArchive(arrangementArchive, targetDir);
		}
		if (newIndex != null) {
			for (Iterator<ArchiveIndexEntry> iter = newIndex.iterator(); iter.hasNext();) {
				added = putToNameMap(iter.next()) || added;
			}
		}
		if (added) {
			this.notifyPoolListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		}
		return added;
	}

	private boolean putToNameMap(ArchiveIndexEntry entry) {
		boolean added = false;
		if (entry.name == null) {
			try {
				entry.getRoot(archivar);
			} catch (Exception e) {
				System.err.println(this.getClass().getName() + " / " + entry + ": " + e);
				logger.log(Level.WARNING, "Stale " + entry, e);
			}
		}
		if (entry.name != null) {
			ArchiveIndex index = this.nameMap.get(entry.name);
			if (index == null) {
				this.nameMap.put(entry.name, index = archivar.makeEmptyIndex());
			}
			added = index.add(entry, true);
		}
		return added;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#findDiagramsByName(java.lang.String)
	 */
	@Override
	public Vector<Root> findDiagramsByName(String rootName) {
		Vector<Root> roots = new Vector<Root>();
		ArchiveIndex index = this.nameMap.get(rootName);
		if (index != null && !index.isEmpty()) {
			for (Iterator<ArchiveIndexEntry> iter = index.iterator(); iter.hasNext();) {
				ArchiveIndexEntry entry = iter.next();
				Root root = null;
				try {
					root = entry.getRoot(archivar);
				} catch (Exception e) {
					System.err.println(this.getClass().getName() + " / " + entry + ": " + e);
					logger.log(Level.WARNING, "Stale " + entry, e);
				}
				if (root != null) {
					roots.add(root);
				}
			}
		}
		return roots;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#findIncludesByName(java.lang.String)
	 */
	@Override
	public Vector<Root> findIncludesByName(String rootName, Root includer, boolean filterByClosestPath) {
		Vector<Root> roots = new Vector<Root>();
		ArchiveIndex index = this.nameMap.get(rootName);
		if (index != null && !index.isEmpty()) {
			for (Iterator<ArchiveIndexEntry> iter = index.iterator(); iter.hasNext();) {
				ArchiveIndexEntry entry = iter.next();
				Root root = entry.getRoot();
				if (root == null && entry.minArgs < 0) {
					try {
						root = entry.getRoot(archivar);
					} catch (Exception e) {
						System.err.println(this.getClass().getName() + " / " + entry + ": " + e);
						logger.log(Level.WARNING, "Stale " + entry, e);
					}
				}
				if (root != null && root.isInclude()) {
					roots.add(root);
				}
			}
		}
		return roots;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#findRoutinesBySignature(java.lang.String, int)
	 */
	@Override
	public Vector<Root> findRoutinesBySignature(String rootName, int argCount, Root caller, boolean filterByClosestPath) {
		Vector<Root> roots = new Vector<Root>();
		ArchiveIndex index = this.nameMap.get(rootName);
		if (index != null && !index.isEmpty()) {
			int maxArgs = Integer.MAX_VALUE;
			for (Iterator<ArchiveIndexEntry> iter = index.iterator(); iter.hasNext();) {
				ArchiveIndexEntry entry = iter.next();
				Root root = entry.getRoot();
				if (root == null && entry.minArgs >= -1 && (entry.maxArgs == -1 || entry.maxArgs >= argCount) && entry.maxArgs <= maxArgs) {
					try {
						root = entry.getRoot(archivar);
					} catch (Exception e) {
						System.err.println(this.getClass().getName() + ": " + e);
						logger.log(Level.SEVERE, entry.getSignature() + " couldn't be retrieved", e);
					}
				}
				if (root != null && root.isSubroutine() && entry.minArgs >= 0 && entry.minArgs <= argCount && entry.maxArgs >= argCount) {
					if (entry.maxArgs < maxArgs) {
						roots.clear();
						maxArgs = entry.maxArgs;
					}
					roots.add(root);
				}
			}
		}
		return roots;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#getAllRoots()
	 */
	@Override
	public Set<Root> getAllRoots() {
		Set<Root> roots = new HashSet<Root>();
		for (ArchiveIndex index: this.nameMap.values()) {
			for (Iterator<ArchiveIndexEntry> iter = index.iterator(); iter.hasNext();) {
				ArchiveIndexEntry entry = iter.next();
				Root root = null;
				try {
					root = entry.getRoot(archivar);
				} catch (Exception e) {
					System.err.println(this.getClass().getName() + ": " + e);
					logger.log(Level.SEVERE, entry.getSignature() + " couldn't be retrieved", e);
				}
				if (root != null) {
					roots.add(root);
				}
			}
		}
		return roots;
	}
	
	// START KGU#1157 2024-10-09: Enh. #1171 Allow to retrieve arrangement positions
	/**
	 * Tries to find the given Root in the archive index and returns its position
	 * if there is an associated point.
	 * 
	 * @param _root - A diagram assumed in the archive
	 * @return either a {@link Point} holding the upper left position or {@code null}
	 */
	public Point getPositionOf(Root _root) {
		Point position = null;
		ArchiveIndex index = this.nameMap.get(_root.getMethodName());
		if (index != null && !index.isEmpty()) {
			for (Iterator<ArchiveIndexEntry> iter = index.iterator(); iter.hasNext();) {
				ArchiveIndexEntry entry = iter.next();
				Root root = entry.getRoot();
				if (root == _root) {
					position = entry.point;
				}
			}
		}
		return position;
	}
	// END KGU#1157 2024-10-09

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#clearExecutionStatus()
	 */
	@Override
	public void clearExecutionStatus() {
		// Usually not relevant here
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#addChangeListener(lu.fisch.structorizer.executor.IRoutinePoolListener)
	 */
	@Override
	public void addChangeListener(IRoutinePoolListener _listener) {
		this.poolListeners.add(_listener);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#removeChangeListener(lu.fisch.structorizer.executor.IRoutinePoolListener)
	 */
	@Override
	public void removeChangeListener(IRoutinePoolListener _listener) {
		this.poolListeners.remove(_listener);
	}

	private void notifyPoolListeners(int _flags) {
		for (IRoutinePoolListener listener: this.poolListeners) {
			listener.routinePoolChanged(this, _flags);
		}
	}

}

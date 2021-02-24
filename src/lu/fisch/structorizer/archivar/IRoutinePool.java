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

/*
 ******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This interface represents the fundamental capabilities of a subroutine pool.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2015-11-25      First issue
 *      Kay Gürtzig     2016-03-08      Method clearExecutionStatus added (for Enhancement #77)
 *      Kay Gürtzig     2016-09-26:     Enh. #253: New public method getAllRoots() added.
 *      Kay Gürtzig     2016-04-11      Enh. #389: Method signature change: findRoutinesByName -> findDiagramsByName,
 *                                      new method findProgramsByName
 *      Kay Gürtzig     2016-05-16      Enh. #389: Method signature change: findProgramsByName -> findIncludesByName
 *      Kay Gürtzig     2019-03-12      Enh. #698: Methods addDiagram and getName added.
 *      Kay Gürtzig     2019-03-13      Enh. #698: Moved from executor to archivar package
 *      Kay Gürtzig     2019-03-28      Enh. #657: Argument added to findIncludesByName() and findRoitinesBySignature()
 *      Kay Gürtzig     2019-03-30      Issue #720: Method findIncludingRoots(String, boolean) added.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2019-03-28 / KGU
 *      
 *      The interface facilitates the retrieval of callable subroutines for execution mode
 *
 ******************************************************************************************************
 */

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lu.fisch.structorizer.elements.Root;
import lu.fisch.utils.StringList;

/**
 * The interface facilitates the collection and retrieval of diagrams for e.g. execution,
 * import or export purposes.
 * Implementing classes provide diagram {@link Root}s by name or routine signature
 * @author Kay Gürtzig
 */
public interface IRoutinePool {

	// START KGU#679 2019-03-12: Enh. #698
	/**
	 * @return the name of this pool if it has got one, otherwise null
	 */
	public String getName();
	
	/**
	 * Adds the given diagram {@code root} to the routine pool.
	 * @param root - a {@link Root}
	 */
	public void addDiagram(Root root);
	
	/**
	 * Adds the content of the given archive to the pool. If {@code lazy} is true and the
	 * pool supports lazy extraction then a full extraction will be avoided and diagrams
	 * may be extracted on demand, provided that the arrangement list contains signature
	 * information (from version 3.29-05 on).
	 * @param arrangementArchive - an arrangement archive file
	 * @param lazy - if true, then only the contents will be extracted immediately if the
	 * the pool supports lazy extraction and the arrangement list conveys signature info. 
	 * @return true if something was actually added
	 */
	public boolean addArchive(File arrangementArchive, boolean lazy);
	// END KGU#679 2019-03-12
	
	/**
	 * Gathers all diagrams responding to the name passed in. 
	 * @param rootName - a String the {@link Root} objects looked for ought to respond to as method name
	 * @return a collection of {@link Root} objects having the passed-in name as diagram or routine name.
	 * @see #findIncludesByName(String, Root, boolean)
	 * @see #findRoutinesBySignature(String, int, Root, boolean)
	 * @see #getAllRoots()
	 */
	public Vector<Root> findDiagramsByName(String rootName);

	/**
	 * Gathers all includable diagrams responding to the name passed in. The search result may be disambiguated
	 * in an implementor-specific way according to some relation with {@code includer} if given.
	 * @param rootName - a String the {@link Root} objects looked for ought to respond to as diagram name
	 * @param includer - the interested {@link Root} object (potentially restricting search), may be null
	 * @param filterByClosestPath - if {@code true} and {@code includer} is given and has an associated
	 *        {@link Root#getNamespace()} then only includables with maximum namespace similarity (longest
	 *        common prefix) will be returned.
	 * @return a collection of {@link Root} objects of type Includable having the passed-in name.
	 * @see #findDiagramsByName(String)
	 * @see #findRoutinesBySignature(String, int, Root, boolean)
	 * @see #findIncludingRoots(String, boolean)
	 */
	public Vector<Root> findIncludesByName(String rootName, Root includer, boolean filterByClosestPath);

	/**
	 * Gathers all subroutine diagrams responding to the name passed in and accepting most
	 * closely the number of arguments given by {@code argCount}. The search may be disambiguated
	 * in an implementor-specific way according to some relation with {@code caller} if given.
	 * @param rootName - a String the {@link Root} objects looked for ought to respond to as method name
	 * @param argCount - number of parameters required
	 * @param caller - the interested {@link Root} object (potentially restricting search), may be null
	 * @param filterByClosestPath if {@code true} and {@code caller} is given and has an associated
	 *        {@link Root#getNamespace()} then only subroutines with maximum namespace similarity
	 *        (longest common prefix) will be returned.
	 * @return a collection of Root objects meeting the specified signature.
	 * @see #findDiagramsByName(String)
	 * @see #findIncludesByName(String, Root, boolean)
	 * @see #findIncludingRoots(String, boolean)
	 */
	public Vector<Root> findRoutinesBySignature(String rootName, int argCount, Root caller, boolean filterByClosestPath);
	
	// START KGU#703 2019-03-30: Issue #720 - Needed a convenient retrieval for Roots referring to an Includable
	/**
	 * Gathers all {@link Root} objects of the pool that refer to an Includable with
	 * name {@code includableName}. If {@code recursively} is false then the result
	 * will only contain diagrams directly listing the Includable, otherwise indirect
	 * references will also be contained.
	 * @param includableName - name of an includable diagram (existence is not checked)
	 * @param recursively - whether to involve indirect references
	 * @return the vector of found roots
	 * @see #findIncludesByName(String, Root, boolean)
	 * @see #findIncludingRoots(Root, boolean)
	 */
	public default Set<Root> findIncludingRoots(String includableName, boolean recursively)
	{
		Set<Root> references = new HashSet<Root>();
		Set<Root> roots = this.getAllRoots();
		StringList includeNames = StringList.getNew(includableName);
		int iStart = 0, iEnd = 0;
		do {
			iEnd = includeNames.count();
			for (Root root: roots) {
				for (int i = iStart; i < iEnd; i++) {
					if (root.includeList != null && root.includeList.contains(includeNames.get(i))) {
						references.add(root);
						if (recursively && root.isInclude()) {
							includeNames.addIfNew(root.getMethodName());
						}
					}
				}
			}
			iStart = iEnd;
		} while (iEnd < includeNames.count());
		return references;
	}
	// END KGU#703 2019-03-30
	
	// START KGU#258 2016-09-26: Enh. #253: We need to traverse all roots for refactoring
	/**
	 * Retrieves a set of all {@link Root} objects parked in this diagram pool.
	 * @return the {@link Root} set
	 * @see #findDiagramsByName(String)
	 * @see #findIncludingRoots(String, boolean)
	 */
	public Set<Root> getAllRoots();
	// END KGU#258 2016-09-26

	// START KGU#117 2016-03-08: Introduced on occasion of Enhancement #77
	/**
	 * Clears the execution status of all {@link Root}s (routine diagrams) in the pool.
	 */
	public void clearExecutionStatus();
	// END KGU#117 2016-03-08
	
	// START KGU#305 2016-12-16: Added to establish a clear observer mechanism
	/**
	 * Adds {@code _listener} to the set of {@link IRoutinePoolListeners}
	 * @param _listener - an {@link IRoutinePoolListener}
	 * @see #removeChangeListener(IRoutinePoolListener)
	 */
	public void addChangeListener(IRoutinePoolListener _listener);
	
	/**
	 * Removes {@code _listener} from the set of {@link IRoutinePoolListener}s
	 * @param _listener - an {@link IRoutinePoolListener}
	 * @see #addChangeListener(IRoutinePoolListener)
	 */
	public void removeChangeListener(IRoutinePoolListener _listener);
	
}
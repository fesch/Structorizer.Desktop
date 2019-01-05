/*
    Structorizer :: Arranger
    A little tool which you can use to arrange Nassi-Shneiderman Diagrams (NSD)

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

/*
 *****************************************************************************************************
 *
 *      Author: Bob Fisch
 *
 *      Description: This class is just a holder for diagrams, their owners, and positions within Arranger
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date        Description
 *      ------          ----        -----------
 *      Bob Fisch       2009-08-18  First Issue
 *      Kay Gürtzig     2015-11-24  Pinning flag added (issue #35, KGU#88)
 *      Kay Gürtzig     2016-03-08  Bugfix #97: Method resetDrawingInfo added (KGU#155)
 *      Kay Gürtzig     2017-01-13  Issue #305 (KGU#330) additional information added to trigger notification
 *      Kay Gürtzig     2018-12-26  Enh. #655 method getName() introduced
 *
 ******************************************************************************************************
 *
 * Comment:	/
 *
 *****************************************************************************************************
 *///

import java.awt.Point;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.gui.Mainform;
import lu.fisch.utils.StringList;

/**
 *
 * @author robertfisch
 */
public class Diagram
{
    Root root = null;
    Point point = null;
    Mainform mainform = null;
    // START KGU#88 2015-11-24
    boolean isPinned = false;
    // END KGU#88 2015-11-24
    // START KGU#330 2017-01-13: Enh. #305 We keep redundant information to be able to trigger change notifications
    private String signature = null;
    // END KGU#330 2017-01-13
    // START KGU#626 2018-12-28: Enh. #657 - group management
    private final StringList groupNames = new StringList();
    boolean wasMoved = false;
    // END KGU#626 2018-12-28

    public Diagram(Root root, Point point)
    {
        this.root = root;
        this.point = point;
        // START KGU#330 2017-01-13: Enh. #305 We keep redundant information to be able to trigger change notifications
        signature = root.getSignatureString(true);
        // END KGU#330 2017-01-13
    }

    // START KGU#155 2016-03-08: Bugfix #97 extension
	/**
	 * Invalidates the cached prepareDraw info of all diagrams residing here
	 * (to be called on events with heavy impact on the size or shape of some
	 * Elements)
	 * @param _exceptDiagr the hash code of a lu.fisch.structorizer.gui.Diagram
	 * that is not to be invoked (to avoid recursion)
	 */
	public void resetDrawingInfo(int _exceptDiagr)
	{
		if (root != null)
		{
			root.resetDrawingInfoDown();
		}
		// The following seems too much, actually
//		if (mainform != null && mainform.diagram != null &&
//				mainform.diagram.hashCode() != _exceptDiagr)
//		{
//			mainform.diagram.resetDrawingInfo(false);
//		}
	}
	// END KGU#155 2016-03-08
	
    // START KGU#330 2017-01-13: Enh. #305
	/**
	 * Identifies notification-relevant changes (and updates the cached info)
	 * @return true iff the signature string for the Arranger index has changed
	 */
	public boolean checkSignatureChange()
	{
		String oldSignature = this.signature;
		this.signature = this.root.getSignatureString(true);
		return !this.signature.equals(oldSignature);
	}
    // END KGU#330 2017-01-13
	
	// START KGU#624 2018-12-26: Enh. #655
	/** @return the pure diagram name (extracted from the cached signature) */
	public String getName()
	{
		String name = this.signature;
		if (name != null) {
			int pos1 = name.indexOf(':');
			if (pos1 > 0) {
				name = name.substring(0, pos1);
			}
			if ((pos1 = name.indexOf('(')) > 0) {
				name = name.substring(0, pos1);
			}
		}
		return name;
	}
	// END KGU#624 2018-12-26

    // START KGU#626 2018-12-28: Enh. #657 - group management
	/**
	 * NOTE: This method should not be called directly but from {@link Group#addDiagram(Diagram)}.
	 * @param _group - the {@link Group} this diagram is being added to
	 * @return true if the group had not been registered with this diagram before, false otherwise
	 */
    protected boolean addToGroup(Group _group)
    {
    	if (_group == null) return false;
    	return this.groupNames.addIfNew(_group.getName());
    }

    /**
	 * NOTE: This method should not be called directly but from {@link Group#removeDiagram(Diagram)}.
	 * @param _group - the {@link Group} this diagram is being added to
	 * @return true if the group had indeed been registered with this diagram before, false otherwise
	 */
    protected boolean removeFromGroup(Group _group)
    {
    	if (_group == null) return false;
    	return this.groupNames.removeAll(_group.getName()) > 0;
    }
    // END KGU#626 2018-12-28
    
    // START KGU#626 2018-12-30: Enh. #657
    /**
     * @return the array of the group names this diagran is member of.
     * @see #addToGroup(Group)
     * @see #removeFromGroup(Group)
     */
    public String[] getGroupNames()
    {
    	return this.groupNames.toArray();
    }
    
    /**
     * Moves the diagram's reference point to the new coordinates and registers the movement
     * @param newX
     * @param newY
     */
    public void setLocation(int newX, int newY)
    {
    	this.point.setLocation(newX, newY);
    	this.wasMoved = true;
    }
    // END KGU#626 2018-12-30
	
}

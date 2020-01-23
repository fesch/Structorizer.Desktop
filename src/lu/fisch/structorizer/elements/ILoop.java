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

/*
 ******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This interface is to facilitate unified handling of different loop types.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author           Date            Description
 *      ------           ----            -----------
 *      Kay Gürtzig      2015.11.30      First issue
 *      Kay Gürtzig      2016.10.13      Enh. #270: method isDisabled() added
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      
 *      The interface eases the detection of different loop classes (For, While, Repeat, Forever) and
 *      the access to the incorporated Subqueue without differentiated type casting and code duplication
 *
 ******************************************************************************************************
 */

/**
 * @author Kay Gürtzig
 * Implementors of the interface are Element subclasses representing some kind of loop. This interface
 * presents the common behaviour
 */
public interface ILoop {
	
	/**
	 * Visitor class looking for a reachable LEAVE instruction
	 * @author kay
	 *
	 */
	public final class LeaveDetector implements IElementVisitor
	{
		boolean exactly;
		int loopLevel = 1;
		public boolean isLeavable = false;
		
		public LeaveDetector(boolean _exactly)
		{
			exactly = _exactly;
		}

		@Override
		public boolean visitPreOrder(Element _ele) {
			if (_ele instanceof Jump && !_ele.disabled && (!exactly || ((Jump)_ele).isLeave())) {
				if (_ele.parent instanceof Subqueue && ((Subqueue)_ele.parent).isReachable(_ele, true)) {
					int up = ((Jump)_ele).getLevelsUp();
					isLeavable = exactly && up == loopLevel || up >= loopLevel;
				}
			}
			else if (_ele instanceof ILoop) {
				// We are entering a loop
				loopLevel++;
			}
			return !isLeavable;
		}

		@Override
		public boolean visitPostOrder(Element _ele) {
			if (_ele instanceof ILoop) {
				// We are leaving a loop
				loopLevel--;
			}
			return !isLeavable;
		}	
	}

	/** @return this loop as Element (mere type bridging) */
	public Element getLoop();
	/** @return the {@link Subqueue} representing the body of this loop */
	public Subqueue getBody();
	// START KGU#277 2016-10-13: Enh. #270 (needed for a generator access)
	/**
	 * Checks whether this element or one of its ancestors is disabled 
	 * @return true if directly or indirectly disabled
	 */
	public boolean isDisabled();
	// END KGU#277 2016-10-13
	
	// START KGU 2017-10-21
	/**
	 * Checks whether there is a reachable {@link Jump} element targeting outside this loop.
	 * @param _exactly - if true then only a LEAVE jump with the exact unwinding level counts
	 * @return true iff there is at least one {@link Jump} element inside meeting the requirements
	 */
	public default boolean hasReachableLeave(boolean _exactly)
	{
		LeaveDetector finder = new LeaveDetector(_exactly);
		this.getBody().traverse(finder);
		return finder.isLeavable;
	}
	// END KGU 2017-10-21
	
}

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

import java.awt.Point;

/*
 ******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This common superclass of While, Repeat, For, and Forever is to facilitate
 *                      unified handling of the different loop types.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author           Date            Description
 *      ------           ----            -----------
 *      Kay Gürtzig      2015-11-30      First issue
 *      Kay Gürtzig      2016-10-13      Enh. #270: method isDisabled() added
 *      Kay Gürtzig      2024-04-17      Bugfix #1161: LeaveDetector mended.
 *      Kay Gürtzig      2024-04-22      Renamed to Loop and converted to an abstract subclass of Element
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

import lu.fisch.utils.StringList;

/**
 * Inheritants of this {@link Element} subclass are the classes representing some
 * kind of loop. This class provides common behaviour for simplicity.
 * @author Kay Gürtzig
 */
public abstract class Loop extends Element {
	
	/** The loop body as {@link Subqueue} */
	protected Subqueue q = new Subqueue();
	
	// START KGU#136 2016-02-27: Bugfix #97 - replaced by local variable in prepareDraw()
	//private Rect r = new Rect();
	// END KGU#136 2016-02-27
	// START KGU#136 2016-03-01: Bugfix #97
	protected Point pt0Body = new Point(0,0);
	// END KGU#136 2016-03-01

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
		
		// START KGU#1151 2024-04-17: Bugfix #1161 Specify the reachability context
		private Element context = null;
		public LeaveDetector(boolean _exactly, Element _loop)
		{
			exactly = _exactly;
			context = _loop;
		}
		// END KGU#1151 2024-04-17

		public LeaveDetector(boolean _exactly)
		{
			exactly = _exactly;
		}

		@Override
		public boolean visitPreOrder(Element _ele) {
			if (_ele instanceof Jump && !_ele.isDisabled(true) && (!exactly || ((Jump)_ele).isLeave())) {
				// START KGU#1151 2024-04-17: Bugfix #1161 Specify the reachability context
				//if (_ele.parent instanceof Subqueue && ((Subqueue)_ele.parent).isReachable(_ele, true)) {
				if (_ele.parent instanceof Subqueue && ((Subqueue)_ele.parent).isReachable(_ele, true, context)) {
				// END KGU#1151 2024-04-17
					int up = ((Jump)_ele).getLevelsUp();
					isLeavable = exactly && up == loopLevel || up >= loopLevel;
				}
			}
			else if (_ele instanceof Loop) {
				// We are entering a loop
				loopLevel++;
			}
			return !isLeavable;
		}

		@Override
		public boolean visitPostOrder(Element _ele) {
			if (_ele instanceof Loop) {
				// We are leaving a loop
				loopLevel--;
			}
			return !isLeavable;
		}	
	}

	protected Loop()
	{
		super();
		q.parent = this;
	}
	protected Loop(String _string)
	{
		super(_string);
		q.parent = this;
	}
	
	protected Loop(StringList _strings)
	{
		super(_strings);
		q.parent = this;
	}
	
	/** @return the {@link Subqueue} representing the body of this loop */
	public Subqueue getBody() {
		return this.q;
	}
	/**
	 * Replaces the body by the given {@link Subqueue} {@code _newBody).
	 * @param _newBody - a {@link Subqueue} to replace the former body
	 */
	public void setBody(Subqueue _newBody) {
		this.q = _newBody;
		this.q.parent = this;
	}
//	// START KGU#277 2016-10-13: Enh. #270 (needed for a generator access)
//	/**
//	 * Checks whether this element or one of its ancestors is disabled 
//	 * @param individually - if {@code true} then only the individual setting will be reported
//	 * @return true if directly or indirectly disabled
//	 */
//	public boolean isDisabled(boolean individually);
//	// END KGU#277 2016-10-13
	
	// START KGU 2017-10-21
	/**
	 * Checks whether there is a reachable {@link Jump} element targeting
	 * outside this loop.
	 * 
	 * @param _exactly - if {@code true} then only a LEAVE jump with the exact
	 *    unwinding level counts
	 * @return {@code true} iff there is at least one {@link Jump} element
	 *    inside meeting the requirements
	 */
	public boolean hasReachableLeave(boolean _exactly)
	{
		LeaveDetector finder = new LeaveDetector(_exactly, (Element)this);
		this.getBody().traverse(finder);
		return finder.isLeavable;
	}
	// END KGU 2017-10-21
	
}

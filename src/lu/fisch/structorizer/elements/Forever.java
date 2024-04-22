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
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents a "FOREVER loop" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2008-02-06      First Issue
 *      Kay Gürtzig     2015-10-11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015-10-12      Breakpoint support prepared
 *      Kay Gürtzig     2015-11-14      Bugfixes #31 (= KGU#82) and #32 (= KGU#83) in method copy() 
 *      Kay Gürtzig     2015-11-30      Inheritance changed: implements Loop
 *		Kay Gürtzig     2015-12-02      Bugfix #39 (KGU#91) -> getText(false) on drawing, constructors
 *                                      and methods setText() now ensure field text being empty
 *      Kay Gürtzig     2016-01-02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016-01-03      Bugfix #87 (KGU#121): Correction in getElementByCoord(), geIcon()
 *      Kay Gürtzig     2016-02-27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016-03-01      Bugfix #97 (KGU#136): Translation-neutral selection
 *      Kay Gürtzig     2016-03-06      Enh. #77 (KGU#117): Method for test coverage tracking added
 *      Kay Gürtzig     2016-03-12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016-04-24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016-07-21      KGU#207: Slight performance improvement in getElementByCoord()
 *      Kay Gürtzig     2016-07-30      Enh. #128: New mode "comments plus text" supported, drawing code delegated
 *      Kay Gürtzig     2018-04-04      Issue #529: Critical section in prepareDraw() reduced.
 *      Kay Gürtzig     2018-10-26      Enh. #619: Method getMaxLineLength() implemented
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-03-17      Issue #56: Accordng to the user guide, Forever may not have a breakpoint
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************
 */


import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.gui.FindAndReplace;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.*;

/**
 * This Structorizer class represents an eternal (uncontrolled) loop in a diagram.
 * 
 * @author Bob Fisch
 */
public class Forever extends Element implements Loop {
	
	public Subqueue q = new Subqueue();
	
	// START KGU#136 2016-02-27: Bugfix #97 - replaced by local variable in prepareDraw()
	//private Rect r = new Rect();
	// END KGU#136 2016-02-27
	// START KGU#136 2016-03-01: Bugfix #97
	private Point pt0Body = new Point(0,0);
	// END KGU#136 2016-03-01

	
	public Forever()
	{
		super();
		q.parent=this;
	}
	
	public Forever(String _strings)
	{
		super();	// Forever elements aren't supposed to have text
		q.parent=this;
		//setText(_strings);
	}
	
	public Forever(StringList _strings)
	{
		super();	// Forever elements aren't supposed to have text
		q.parent=this;
		//setText(_strings);
	}
	
	// START KGU#91 2015-12-02
    @Override
    public void setText(String _text)
    {
    	text.clear();
    }
	
    @Override
    public void setText(StringList _text)
    {
    	text.clear();
    }
    // END KGU#91 2015-12-02
	
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRect0UpToDate) return rect0;
		// END KGU#136 2016-03-01
		
		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		if (isCollapsed(true)) 
		{
			rect0 = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
			// START KGU#136 2016-03-01: Bugfix #97
			isRect0UpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}
		
		// START KGU#227 2016-07-30: Enh. #128 Just delegate the basics to Instruction
		// START KGU#516 2018-04-04: Issue #529 - Directly to work on field rect0 was not so good an idea for re-entrance
		//rect0 = Instruction.prepareDraw(_canvas, this.getText(false), this);
		Rect rect0 = Instruction.prepareDraw(_canvas, this.getText(false), this);
		Point pt0Body = new Point();
		// END KGU#516 2018-04-04
		// END KGU#227 2016-07-30: Just delegate the basics to Instruction

		// START KGU#136 2016-03-01: Bugfix #97
		pt0Body.x = 2*E_PADDING/2 - 1;	// FIXME: Fine tuning!
		pt0Body.y = rect0.bottom - 1;	// FIXME: Fine tuning!
		// END KGU#136 2016-03-01
		
		// START KGU#136 2016-02-27: Bugfix #97 - field replaced by local variable
		//r = q.prepareDraw(_canvas);
		//rect.right = Math.max(rect0.right, r.right+E_PADDING);
		//rect.bottom += r.bottom+E_PADDING;		
		Rect rectBody = q.prepareDraw(_canvas);
		rect0.right = Math.max(rect0.right, rectBody.right+E_PADDING);
		rect0.bottom += rectBody.bottom+E_PADDING;
		// END KGU#136 2016-02-27
		
		// START KGU#516 2018-04-04: Issue #529 - reduced critical section
		this.rect0 = rect0;
		this.pt0Body = pt0Body;
		// END KGU#516 2018-04-04
		// START KGU#227 2016-07-30: Has not been done by Instruction
		isRect0UpToDate = true;
		// END KGU#227 2016-07-30
		
		return rect0;
	}
	
	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention)
	{
		// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
		if (!checkVisibility(_viewport, _top_left)) { return; }
		// END KGU#502/KGU#524/KGU#553 2019-03-13

		if(isCollapsed(true)) 
		{
			Instruction.draw(_canvas, _top_left, getCollapsedText(), this, _inContention);
			// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
			wasDrawn = true;
			// END KGU#502/KGU#524/KGU#553 2019-03-14
			return;
		}

		// START KGU#227 2016-07-30: Enh. #128 - on this occasion delegate as much as possible to Instruction
		Instruction.draw(_canvas, _top_left, this.getText(false), this, _inContention);
		// END KGU#227 2016-07-30
		
		// draw children
		Rect myrect = _top_left.copy();
		// START KGU#227 2016-07-30: Enh. #128 + code revision
		//myrect.left += Element.E_PADDING-1;
		//myrect.top += headerHeight-1;
		myrect.left += pt0Body.x;
		myrect.top += pt0Body.y;
		// END KGU#227 2016-07-30
		myrect.bottom -= E_PADDING/*+1*/;	// KGU 2016-04-24: +1 led to line of double thickness
		q.draw(_canvas, myrect, _viewport, _inContention);
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
		wasDrawn = true;
		// END KGU#502/KGU#524/KGU#553 2019-03-14
	}
	
	// START KGU#122 2016-01-03: Enh. #87 - Collapsed elements may be marked with an element-specific icon
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getIcon()
	 */
	@Override
	public ImageIcon getIcon()
	{
		return IconLoader.getIcon(61);
	}
	// END KGU#122 2016-01-03
	
	// START KGU#535 2018-06-28
	/**
	 * @return the (somewhat smaller) element-type-specific icon image intended to be used in
	 * the {@link FindAndReplace} dialog.
	 * @see #getIcon()
	 */
	@Override
	public ImageIcon getMiniIcon()
	{
		return IconLoader.getIcon(14);
	}
	// END KGU#535 2018-06-28

	// START KGU 2015-10-11: Merged with selectElementByCoord
	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x, _y, _forSelection);
		// START KGU#121 2016-01-03: Bugfix #87 - A collapsed element has no visible substructure!
    	// START KGU#207 2016-07-21: If this element isn't hit then there is no use searching the substructure
		//if (!this.isCollapsed())
		if (!this.isCollapsed(true) && (selMe != null || _forSelection))
		// START KGU#207 2016-07-21
		{
		// END KGU#121 2016-01-03
			// START KGU#136 2016-03-01: Bugfix #97 - use local coordinates
			//Element sel = q.getElementByCoord(_x, _y, _forSelection);
			Element sel = q.getElementByCoord(_x-pt0Body.x, _y-pt0Body.y, _forSelection);
			// END KGU#136 2016-03-01
			if (sel != null) 
			{
				if (_forSelection) selected=false;
				selMe = sel;
			}
		// START KGU#121 2016-01-03: Bugfix #87 (continued)
		}
		// END KGU#121 2016-01-03
		
		return selMe;
	}
	// END KGU 2015-10-11
	
	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	public Element findSelected()
	{
		Element sel = selected ? this : null;
		if (sel == null)
		{
			sel = q.findSelected();
		}
		return sel;
	}
	// END KGU#183 2016-04-24
	
	public Element copy()
	{
		Forever ele = new Forever();
		copyDetails(ele, false);
		ele.q=(Subqueue) this.q.copy();
		ele.q.parent=ele;
		return ele;
	}
	
	// START KGU#119 2016-01-02: Bugfix #78
	/**
	 * Returns true iff _another is of same class, all persistent attributes are equal, and
	 * all substructure of _another recursively equals the substructure of this. 
	 * @param another - the Element to be compared
	 * @return true on recursive structural equality, false else
	 */
	@Override
	public boolean equals(Element _another)
	{
		return super.equals(_another) && this.q.equals(((Forever)_another).q);
	}
	// END KGU#119 2016-01-02
	
	// START KGU#43/KGU#686 2019-03-17: Issue #56 Breakpoint support denied
	@Override
	public void toggleBreakpoint()
	{
		// Forever itself may never have a breakpoint!
		breakpoint = false;
	}

	// START KGU#117 2016-03-07: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#combineCoverage(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	public boolean combineRuntimeData(Element _cloneOfMine)
	{
		return super.combineRuntimeData(_cloneOfMine) &&
				this.getBody().combineRuntimeData(((Loop)_cloneOfMine).getBody());
	}
	// END KGU#117 2016-03-07

	// START KGU#156 2016-03-13: Enh. #124
	protected String getRuntimeInfoString()
	{
		String info = this.getExecCount() + " / ";
		String stepInfo = null;
		switch (E_RUNTIMEDATAPRESENTMODE)
		{
		case TOTALSTEPS_LIN:
		case TOTALSTEPS_LOG:
			stepInfo = Integer.toString(this.getExecStepCount(true));
			if (!this.isCollapsed(true)) {
				stepInfo = "(" + stepInfo + ")";
			}
			break;
		default:
			stepInfo = Integer.toString(this.getExecStepCount(this.isCollapsed(true)));
		}
		return info + stepInfo;
	}
	// END KGU#156 2016-03-11

	// START KGU#117 2016-03-10: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isTestCovered(boolean)
	 */
	public boolean isTestCovered(boolean _deeply)
	{
		return this.getBody().isTestCovered(_deeply);
	}
	// END KGU#117 2016-03-10

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
	protected void addFullText(StringList _lines, boolean _instructionsOnly)
	{
		if (!this.isDisabled(false)) {
			this.q.addFullText(_lines, _instructionsOnly);
		}
	}
	// END KGU 2015-10-16

	// START KGU 2015-11-30
	@Override
	public Subqueue getBody() {
		return this.q;
	}
	// END KGU 2015-11-30
	@Override
	public Element getLoop() {
		return this;
	}

	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 */
	@Override
	public void convertToCalls(StringList _signatures)
	{
		getBody().convertToCalls(_signatures);
	}
	// END KGU#199 2016-07-07
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#traverse(lu.fisch.structorizer.elements.IElementVisitor)
	 */
	@Override
	public boolean traverse(IElementVisitor _visitor) {
		boolean proceed = _visitor.visitPreOrder(this);
		if (proceed)
		{
			proceed = this.getBody().traverse(_visitor);
		}
		if (proceed)
		{
			proceed = _visitor.visitPostOrder(this);
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRelevantParserKeys()
	 */
	@Override
	protected String[] getRelevantParserKeys() {
		// There is nothing to refactor
		return null;
	}

	// START KGU 2017-10-21
	@Override
	public boolean mayPassControl()
	{
		// This may only pass control if being disabled or containing a reachable leave jump
		// to exactly this level
		return disabled || this.hasReachableLeave(true);
	}
	// END KGU 2017-10-21

	// START KGU#602 2018-10-25: Issue #419 - Mechanism to detect and handle long lines
	/**
	 * Detects the maximum text line length either on this very element 
	 * @param _includeSubstructure - whether (in case of a complex element) the substructure
	 * is to be involved
	 * @return the maximum line length
	 */
	public int getMaxLineLength(boolean _includeSubstructure)
	{
		int maxLen = 0;
		if (_includeSubstructure) {
			maxLen = Math.max(maxLen, this.q.getMaxLineLength(true));
		}
		return maxLen;
	}
	// END KGU#602 2018-10-25
}

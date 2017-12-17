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
 *      Description:    This class represents a "REPEAT loop" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.12      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015.11.14      Bugfix #31 (= KGU#82) in method copy()
 *      Kay Gürtzig     2015.11.30      Inheritance changed: implements ILoop
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (= KGU#91) in draw methods (--> getText(false))
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016.01.03      Bugfix #87 (KGU#121): Correction in getElementByCoord(),
 *                                      Enh. #87 (KGU#122): Modification of collapsed text, getIcon()
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136): Translation-neutral selection
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Method for test coverage tracking added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.21      KGU#207: Slight performance improvement in getElementByCoord()
 *      Kay Gürtzig     2016.10.13      Enh. #270: Hatched overlay texture in draw() if disabled
 *      Kay Gürtzig     2016.12.12      Bugfix #308 in haveOuterRectDrawn() - must be drawn in collapsed mode
 *      Kay Gürtzig     2017.11.01      Bugfix #447: End-standing backslashes suppressed for display and analysis
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************/

import java.awt.Color;
import java.awt.Point;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.*;

public class Repeat extends Element implements ILoop {
	
	public Subqueue q = new Subqueue();
	
	// START KGU#136 206-02-27: Bugfix #97 - replaced by local variable in prepareDraw()
	//private Rect r = new Rect();
	// END KGU#136 2016-02-27
	// START KGU#136 2016-03-01: Bugfix #97
	private Point pt0Body = new Point(0,0);
	// END KGU#136 2016-03-01

	// START KGU#258 2016-09-26: Enh. #253
	private static final String[] relevantParserKeys = {"preRepeat", "postRepeat"};
	// END KGU#258 2016-09-25
		
	public Repeat()
	{
		super();
		q.parent=this;
	}
	
	public Repeat(String _strings)
	{
		super(_strings);
		q.parent=this;
		setText(_strings);
	}
	
	public Repeat(StringList _strings)
	{
		super(_strings);
		q.parent=this;
		setText(_strings);
	}
	
	// START KGU#122 2016-01-04: It makes more sense to revert the line order for Repeat (first ellipse, then condition)
	public StringList getCollapsedText()
	{
		return super.getCollapsedText().reverse();
	}
	// END KGU#122 2016-01-04

	// START KGU#227 2016-07-30: Enh. #128
	protected boolean haveOuterRectDrawn()
	{
		// START KGU#308 2016-12-12: Bugfix #308
		//return false;
		return this.isCollapsed(true);
		// END KGU#308 2016-12-12
	}
	// END KGU#227 2016-07-30

	/**
	 * Draws the marker bar on the top side of the given _rect
	 * @param _canvas - the canvas to be drawn in
	 * @param _rect - the surrounding rectangle of the Element (or relevant part of it)
	 */
	protected void drawBreakpointMark(Canvas _canvas, Rect _rect)
	{
		Rect bpRect = _rect.copy();
		bpRect.left += pt0Body.x;
		super.drawBreakpointMark(_canvas, bpRect);
	}

	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		// END KGU#136 2016-03-01

		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		if(isCollapsed(true)) 
		{
			rect0 = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
			// START KGU#136 2016-03-01: Bugfix #97
			isRectUpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}
		
		// Just delegate the basics to Instruction
		// START KGU#453 2017-11-01: Bugfix #447 - no need to show possible backslashes at end
		//rect0 = Instruction.prepareDraw(_canvas, this.getText(false), this);
		rect0 = Instruction.prepareDraw(_canvas, this.getCuteText(false), this);
		// END KGU#453 2017-11-01
		this.pt0Body.y = rect0.bottom;	// height of the footer

		
		// START KGU#136 2016-02-27: Bugfix #97 - field replaced by local variable
		//r=q.prepareDraw(_canvas);
		//rect.right=Math.max(rect.right,r.right+E_PADDING);
		//rect.bottom+=r.bottom;		
		//return rect;
		Rect rectBody = q.prepareDraw(_canvas);
		rect0.right = Math.max(rect0.right, rectBody.right+ 2*E_PADDING/2);
		rect0.bottom += rectBody.bottom;		
		// START KGU#136 2016-03-01: Bugfix #97 - Preparation for local coordinate detection
		this.pt0Body.x = 2*E_PADDING/2 - 1;		// FIXME: Fine tuning!
		// END KGU#136 2016-03-01

		// START KGU#136 2016-03-01: Bugfix #97
		isRectUpToDate = true;
		// END KGU#136 2016-03-01
		return rect0;
		// END KGU#136 2016-02-27
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{
		if(isCollapsed(true)) 
		{
			Instruction.draw(_canvas, _top_left, getCollapsedText(), this);
			return;
		}

		// START KGU#227 2016-07-30: Enh. #128 Delegate the drawing of the text area to Instruction

		Rect myrect = _top_left.copy();

		Color drawColor = getFillColor();
		_canvas.setBackground(drawColor);
		_canvas.setColor(drawColor);
		_canvas.fillRect(myrect);
		// START KGU#277 2016-10-13: Enh. #270
		if (this.disabled) {
			_canvas.hatchRect(myrect, 5, 10);
		}
		// END KGU#277 2016-10-13
		
		myrect.top = myrect.bottom - pt0Body.y;
		// START KGU#453 2017-11-01: Bugfix #447 - no need to show possible backslashes at end
		//Instruction.draw(_canvas, myrect, this.getText(false), this);
		Instruction.draw(_canvas, myrect, this.getCuteText(false), this);
		// END KGU#453 2017-11-01
		// START KGU#277 2016-10-13: Enh. #270
		if (this.disabled) {
			_canvas.hatchRect(myrect, 5, 10);
		}
		// END KGU#277 2016-10-13
		// Now we must correct the origin information (was set up by draw under wrong assumptions)
		this.rect.bottom = _top_left.bottom - _top_left.top;
		this.topLeft.y = _top_left.top - this.getDrawPoint().y;

		_canvas.setColor(Color.BLACK);
		myrect = _top_left.copy();
		// redraw comment indicator
		if (Element.E_SHOWCOMMENTS && !this.getComment(false).getText().trim().equals(""))
		{
			this.drawCommentMark(_canvas, myrect);
		}
		// draw outer box
		_canvas.setColor(Color.BLACK);
		_canvas.drawRect(myrect);
		
		// draw children
		myrect.left += pt0Body.x;
		myrect.bottom -= pt0Body.y;
		// END KGU#227 2016-07-30
				
		q.draw(_canvas,myrect);
		
	}
	
	// START KGU#122 2016-01-03: Enh. #87 - Collapsed elements may be marked with an element-specific icon
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getIcon()
	 */
	@Override
	public ImageIcon getIcon()
	{
		return IconLoader.ico063;
	}
	// END KGU#122 2016-01-03

	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x, _y, _forSelection);
		// START KGU#121 2016-01-03: A collapsed element has no visible substructure!
    	// START KGU#207 2016-07-21: If this element isn't hit then there is no use searching the substructure
		//if (!this.isCollapsed())
		if (!this.isCollapsed(true) && (selMe != null || _forSelection))
		// START KGU#207 2016-07-21
		{
		// END KGU#121 2016-01-03
			Element sel = q.getElementByCoord(_x - pt0Body.x, _y, _forSelection);
			if (sel!=null) 
			{
				if (_forSelection) selected=false;
				selMe = sel;
			}
		// START KGU#121 2016-01-03: A collapsed element has no visible substructure!
		}
		// END KGU#121 2016-01-03
		
		return selMe;
	}
	
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
		Element ele = new Repeat(this.getText().copy());
		copyDetails(ele, false);
		((Repeat) ele).q=(Subqueue) this.q.copy();
		((Repeat) ele).q.parent=ele;
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
		return super.equals(_another) && this.q.equals(((Repeat)_another).q);
	}
	// END KGU#119 2016-01-02
	
    /*@Override
    public void setColor(Color _color) 
    {
        super.setColor(_color);
        q.setColor(_color);
    }*/

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
		if (!this.isDisabled()) {
			// The own text contains just a condition (i.e. a logical expression), not an instruction
			if (!_instructionsOnly)
			{
				// START KGU#413 2017-06-09: Enh. #416: Cope with user-inserted line breaks
				//_lines.add(this.getText());
				_lines.add(this.getUnbrokenText().getLongString());
				// END KGU#413 2017-06-09
			}
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

	// START KGU#258 2016-09-26: Enh. #253
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRelevantParserKeys()
	 */
	@Override
	protected String[] getRelevantParserKeys() {
		return relevantParserKeys;
	}
	// END KGU#258 2016-09-26

}

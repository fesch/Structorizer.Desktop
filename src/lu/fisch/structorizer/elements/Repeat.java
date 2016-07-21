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

/******************************************************************************************************
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
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.FontMetrics;
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
	
	// START KGU#64 2015-11-03: Is to improve drawing performance
	/**
	 * Recursively clears all drawing info this subtree down
	 * (To be overridden by structured sub-classes!)
	 */
	@Override
	public void resetDrawingInfoDown()
	{
		this.resetDrawingInfo();
		this.q.resetDrawingInfoDown();
	}
	// END KGU#64 2015-11-03

	// START KGU#122 2016-01-04: It makes more sense to revert the line order for Repeat (first ellipse, then condition)
	public StringList getCollapsedText()
	{
		return super.getCollapsedText().reverse();
	}
	// END KGU#122 2016-01-04

	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		// END KGU#136 2016-03-01

		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		if(isCollapsed()) 
		{
			rect0 = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
			// START KGU#136 2016-03-01: Bugfix #97
			isRectUpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}
            
		rect0.top = 0;
		rect0.left = 0;
		
		rect0.right = 2*(E_PADDING/2);
		
		FontMetrics fm = _canvas.getFontMetrics(font);
		
		for (int i=0; i<getText(false).count(); i++)
		{
			int width = getWidthOutVariables(_canvas, getText(false).get(i), this) + 2*(E_PADDING/2);
			if (rect0.right < width)
			{
				rect0.right = width;
			}
		}
		
		rect0.bottom = 2*(E_PADDING/2) + getText(false).count()*fm.getHeight();
		
		// START KGU#136 2016-02-27: Bugfix #97 - field replaced by local variable
		//r=q.prepareDraw(_canvas);
		//rect.right=Math.max(rect.right,r.right+E_PADDING);
		//rect.bottom+=r.bottom;		
		//return rect;
		// START KGU#136 2016-03-01: Bugfix #97 - Preparation for local coordinate detection
		this.pt0Body.x = E_PADDING - 2;		// FIXME: Fine tuning!
		this.pt0Body.y = 0;
		// END KGU#136 2016-03-01
		Rect rectBody = q.prepareDraw(_canvas);
		rect0.right = Math.max(rect0.right, rectBody.right+E_PADDING);
		rect0.bottom += rectBody.bottom;		

		// START KGU#136 2016-03-01: Bugfix #97
		isRectUpToDate = true;
		// END KGU#136 2016-03-01
		return rect0;
		// END KGU#136 2016-02-27
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{
                if(isCollapsed()) 
                {
                    Instruction.draw(_canvas, _top_left, getCollapsedText(), this);
                    return;
                }
                
		Rect myrect = new Rect();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = getColor();
		Color drawColor = getFillColor();
		// END KGU 2015-10-13
		FontMetrics fm = _canvas.getFontMetrics(font);
//		int p;
//		int w;
		
		// START KGU 2015-10-13: Became obsolete by new method getFillColor() applied above now
//		if (selected==true)
//		{
//			if(waited==true) { drawColor=Element.E_WAITCOLOR; }
//			else { drawColor=Element.E_DRAWCOLOR; }
//		}
		// END KGU 2015-10-13
		
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		Point ref = this.getDrawPoint();
		this.topLeft.x = _top_left.left - ref.x;
		this.topLeft.y = _top_left.top - ref.y;

		// END KGU#136 2016-03-01
		
		int footerHeight = fm.getHeight() * getText(false).count() + 2*(E_PADDING / 2);
		
		// draw shape
		Rect cprect = _top_left.copy();
		canvas.setColor(Color.BLACK);
		cprect.bottom = _top_left.bottom;
		cprect.top = cprect.bottom - footerHeight;
		canvas.drawRect(cprect);
		
		myrect = _top_left.copy();
		myrect.right=myrect.left+Element.E_PADDING;
		canvas.drawRect(myrect);

		cprect.top += 1;
		cprect.left = myrect.right;
		
		// fill shape
		canvas.setColor(drawColor);
		myrect.left += 1;
		myrect.top += 1;
		//myrect.bottom = myrect.bottom;
		//myrect.right = myrect.right;
		canvas.fillRect(myrect);
		
		myrect = _top_left.copy();
		myrect.bottom = _top_left.bottom;
		myrect.top = myrect.bottom - footerHeight;
		myrect.left += 1;
		myrect.top += 1;
		//myrect.bottom=myrect.bottom;
		//myrect.right=myrect.right;
		canvas.fillRect(myrect);
		
		// draw comment
		if (Element.E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
		{
			// START KGU 2015-10-11: Use an inherited helper method now
//			canvas.setBackground(E_COMMENTCOLOR);
//			canvas.setColor(E_COMMENTCOLOR);
//			
//			Rect someRect = _top_left.copy();
//			
//			someRect.left+=2;
//			someRect.top+=2;
//			someRect.right=someRect.left+4;
//			someRect.bottom-=1;
//			
//			canvas.fillRect(someRect);
			this.drawCommentMark(canvas, _top_left);
			// END KGU 2015-10-11
		}
		// START KGU 2015-10-11
		// draw breakpoint bar if necessary
		this.drawBreakpointMark(canvas, cprect);
		// END KGU 2015-10-11
		
		
		// draw text
		for (int i=0; i<getText(false).count(); i++)
		{
			String text = this.getText(false).get(i);
			
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
							  _top_left.left + (E_PADDING / 2),
							myrect.top + (E_PADDING / 2) + (i+1)*fm.getHeight(),
							text, this
							);  	
		}
		
		// START KGU#156 2016-03-11: Enh. #124
		// write the run-time info if enabled
		this.writeOutRuntimeInfo(canvas, myrect.right - (Element.E_PADDING / 2), myrect.top);
		// END KGU#156 2016-03-11
				
		// draw children
		myrect.bottom = myrect.top;
		myrect.top = _top_left.top + pt0Body.y;
		myrect.left += pt0Body.x;
		myrect.right = _top_left.right;
		q.draw(_canvas,myrect);
		
	}
	
	// START KGU#122 2016-01-03: Enh. #87 - Collapsed elements may be marked with an element-specific icon
	@Override
	protected ImageIcon getIcon()
	{
		return IconLoader.ico063;
	}
	// END KGU#122 2016-01-03

	// START KGU 2015-10-11: Merged with getElementByCoord, which had to be overridden as well for proper Comment popping
//	public Element selectElementByCoord(int _x, int _y)
//	{
//		Element selMe = super.selectElementByCoord(_x,_y);
//		Element sel = q.selectElementByCoord(_x,_y);
//		if(sel!=null) 
//		{
//			selected=false;
//			selMe = sel;
//		}
//		
//		return selMe;
//	}
	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x, _y, _forSelection);
		// START KGU#121 2016-01-03: A collapsed element has no visible substructure!
    	// START KGU#207 2016-07-21: If this element isn't hit then there is no use searching the substructure
		//if (!this.isCollapsed())
		if (!this.isCollapsed() && (selMe != null || _forSelection))
		// START KGU#207 2016-07-21
		{
		// END KGU#121 2016-01-03
			Element sel = q.getElementByCoord(_x, _y, _forSelection);
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
	// END KGU 2015-10-11
	
//	public void setSelected(boolean _sel)
//	{
//		selected=_sel;
//		//q.setSelected(_sel);
//	}
	
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
		ele.setComment(this.getComment().copy());
		ele.setColor(this.getColor());
		((Repeat) ele).q=(Subqueue) this.q.copy();
		((Repeat) ele).q.parent=ele;
		// START KGU#82 (bug #31) 2015-11-14
		ele.breakpoint = this.breakpoint;
		// END KGU#82 (bug #31) 2015-11-14
		// START KGU#117 2016-03-07: Enh. #77
		ele.deeplyCovered = Element.E_COLLECTRUNTIMEDATA && this.deeplyCovered;
		// END KGU#117 2016-03-07
		// START KGU#183 2016-04-24: Issue #169
		ele.selected = this.selected;
		// END KGU#183 2016-04-24
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

	// START KGU#43 2015-10-12
	@Override
	public void clearBreakpoints()
	{
		super.clearBreakpoints();
		this.q.clearBreakpoints();
	}
	// END KGU#43 2015-10-12

	// START KGU#43 2015-11-09
	@Override
	public void clearExecutionStatus()
	{
		super.clearExecutionStatus();
		this.q.clearExecutionStatus();
	}
	// END KGU#43 2015-11-09
	
	// START KGU#117 2016-03-06: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#clearTestCoverage()
	 */
	@Override
	public void clearRuntimeData()
	{
		super.clearRuntimeData();
		this.getBody().clearRuntimeData();
	}
	// END KGU#117 2016-03-06

	// START KGU#156 2016-03-13: Enh. #124
	protected String getRuntimeInfoString()
	{
		String info = this.execCount + " / ";
		String stepInfo = null;
		switch (E_RUNTIMEDATAPRESENTMODE)
		{
		case TOTALSTEPS_LIN:
		case TOTALSTEPS_LOG:
			stepInfo = Integer.toString(this.getExecStepCount(true));
			if (!this.isCollapsed()) {
				stepInfo = "(" + stepInfo + ")";
			}
			break;
		default:
			stepInfo = Integer.toString(this.getExecStepCount(this.isCollapsed()));
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
		// The own text contains just a condition (i.e. a logical expression), not an instruction
		if (!_instructionsOnly)
		{
			_lines.add(this.getText());
		}
		this.q.addFullText(_lines, _instructionsOnly);
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
}

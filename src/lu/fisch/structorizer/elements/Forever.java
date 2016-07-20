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
 *      Description:    This class represents a "FOREVER loop" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008.02.06      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015.10.12      Breakpoint support prepared
 *      Kay Gürtzig     2015.11.14      Bugfixes #31 (= KGU#82) and #32 (= KGU#83) in method copy() 
 *      Kay Gürtzig     2015.11.30      Inheritance changed: implements ILoop
 *		Kay Gürtzig     2015.12.02      Bugfix #39 (KGU#91) -> getText(false) on drawing, constructors
 *                                      and methods setText() now ensure field text being empty
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016.01.03      Bugfix #87 (KGU#121): Correction in getElementByCoord(), geIcon()
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136): Translation-neutral selection
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Method for test coverage tracking added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
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

public class Forever extends Element implements ILoop {
	
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
	
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		// END KGU#136 2016-03-01
		
		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		if (isCollapsed()) 
		{
			rect0 = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
			// START KGU#136 2016-03-01: Bugfix #97
			isRectUpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}
            
		rect0.top = 0;
		rect0.left = 0;
		
		rect0.right = 2 * (E_PADDING/2);

		
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		
		for (int i = 0; i < getText(false).count(); i++)
		{
			int lineWidth = getWidthOutVariables(_canvas, getText(false).get(i), this) + 2*(E_PADDING/2);
			if (rect0.right < lineWidth)
			{
				rect0.right = lineWidth;
			}
		}
		
		rect0.bottom = getText(false).count() * fm.getHeight() + 2*(E_PADDING/2);
		// START KGU#136 2016-03-01: Bugfix #97
		pt0Body.x = E_PADDING/2 - 1;	// FIXME: Fine tuning!
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
		return rect0;
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
//			drawColor=Element.E_DRAWCOLOR;
//		}
		// END KGU 2015-10-13
		
		int headerHeight = getText(false).count() * fm.getHeight() + 2*(Element.E_PADDING / 2);
		
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		// draw background
		myrect = _top_left.copy();
		canvas.fillRect(myrect);
		
		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		Point ref = this.getDrawPoint();
		this.topLeft.x = _top_left.left - ref.x;
		this.topLeft.y = _top_left.top - ref.y;
		// END KGU#136 2016-03-01
		
		// draw shape
		canvas.setColor(Color.BLACK);
		canvas.drawRect(_top_left);
		
		myrect = _top_left.copy();
		myrect.bottom = _top_left.top + headerHeight;
		canvas.drawRect(myrect);
		
		myrect.bottom = _top_left.bottom;
		myrect.top = myrect.bottom - E_PADDING;
		canvas.drawRect(myrect);
		
		myrect = _top_left.copy();
		myrect.right = myrect.left + E_PADDING;
		canvas.drawRect(myrect);
		
		// fill shape
		canvas.setColor(drawColor);
		myrect.left += 1;
		myrect.top += 1;
		//myrect.bottom = myrect.bottom;
		myrect.right -= 1;
		canvas.fillRect(myrect);
		
		myrect = _top_left.copy();
		myrect.bottom = _top_left.top + headerHeight;
		myrect.left += 1;
		myrect.top += 1;
		//myrect.bottom = myrect.bottom;
		myrect.right -= 1;
		canvas.fillRect(myrect);
		
		myrect.bottom = _top_left.bottom;
		myrect.top = myrect.bottom - Element.E_PADDING;
		myrect.left += 1;
		myrect.top += 1;
		//myrect.bottom = myrect.bottom;
		//myrect.right = myrect.right;
		canvas.fillRect(myrect);
		
		// draw comment
		if(Element.E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
		{
			this.drawCommentMark(canvas, _top_left);
		}
		// START KGU 2015-10-11
		// draw breakpoint bar if necessary
		this.drawBreakpointMark(canvas, _top_left);
		// END KGU 2015-10-11
		
		
		// draw text
		for(int i = 0; i < getText(false).count(); i++)
		{
			String text = this.getText(false).get(i);
			text = BString.replace(text, "<--","<-");
			
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
							  _top_left.left + (E_PADDING / 2),
							  _top_left.top + (E_PADDING / 2) + (i+1)*fm.getHeight(),
							  text,this
							  );  	
		}
		
		// draw children
		myrect = _top_left.copy();
		myrect.left += Element.E_PADDING-1;
		myrect.top += headerHeight-1;
		myrect.bottom -= E_PADDING/*+1*/;	// KGU 2016-04-24: +1 led to line of double thickness
		q.draw(_canvas,myrect);
	}
	
	// START KGU#122 2016-01-03: Enh. #87 - Collapsed elements may be marked with an element-specific icon
	@Override
	protected ImageIcon getIcon()
	{
		return IconLoader.ico061;
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
		// START KGU#121 2016-01-03: Bugfix #87 - A collapsed element has no visible substructure!
		if (!this.isCollapsed())
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
		// Bugfix KGU#83 (bug #32) 2015-11-14: Instead of a Forever loop, a For loop had been created!
		Forever ele = new Forever();
		ele.setComment(this.getComment().copy());
		ele.setColor(this.getColor());
		ele.q=(Subqueue) this.q.copy();
		ele.q.parent=ele;
		// START KGU#82 (bug #31) 2015-11-14
		ele.breakpoint = this.breakpoint;
		// END KGU#82 (bug #31) 2015-11-14
		// START KGU#117 2016-03-07: Enh. #77
        if (Element.E_COLLECTRUNTIMEDATA)
        {
        	// We share this object (important for recursion!)
        	ele.deeplyCovered = this.deeplyCovered;
        }
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
		return super.equals(_another) && this.q.equals(((Forever)_another).q);
	}
	// END KGU#119 2016-01-02
	
	// START KGU#117 2016-03-07: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#combineCoverage(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	public boolean combineRuntimeData(Element _cloneOfMine)
	{
		return super.combineRuntimeData(_cloneOfMine) &&
				this.getBody().combineRuntimeData(((ILoop)_cloneOfMine).getBody());
	}
	// END KGU#117 2016-03-07

	// START KGU#43 2015-10-12
	@Override
	public void clearBreakpoints()
	{
		super.clearBreakpoints();
		this.q.clearBreakpoints();
	}
	// END KGU#43 2015-10-12
	
	// START KGU#43 2015-10-13
	@Override
	public void clearExecutionStatus()
	{
		super.clearExecutionStatus();
		this.q.clearExecutionStatus();
	}
	// END KGU#43 2015-10-13
	
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

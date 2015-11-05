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
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.FontMetrics;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;

public class Forever extends Element{
	
	public Subqueue q = new Subqueue();
	
	private Rect r = new Rect();
	
	public Forever()
	{
		super();
		q.parent=this;
	}
	
	public Forever(String _strings)
	{
		super(_strings);
		q.parent=this;
		setText(_strings);
	}
	
	public Forever(StringList _strings)
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
	
	public Rect prepareDraw(Canvas _canvas)
	{
                if(isCollapsed()) 
                {
                    rect = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
                    return rect;
                }
            
		rect.top=0;
		rect.left=0;
		
		rect.right=2*Math.round(E_PADDING/2);
		
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		
		rect.right=Math.round(2*(Element.E_PADDING/2));
		for(int i=0;i<getText().count();i++)
		{
			if(rect.right<getWidthOutVariables(_canvas,getText().get(i),this)+2*Math.round(E_PADDING/2))
			{
				rect.right=getWidthOutVariables(_canvas,getText().get(i),this)+2*Math.round(E_PADDING/2);
			}
		}
		
		rect.bottom=2*Math.round(E_PADDING/2)+getText().count()*fm.getHeight();
		
		r=q.prepareDraw(_canvas);
		
		rect.right=Math.max(rect.right,r.right+E_PADDING);
		rect.bottom+=r.bottom+E_PADDING;		
		return rect;
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
		
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		// draw background
		myrect=_top_left.copy();
		canvas.fillRect(myrect);
		
		// draw shape
		rect=_top_left.copy();
		canvas.setColor(Color.BLACK);
		canvas.drawRect(_top_left);
		
		myrect=_top_left.copy();
		myrect.bottom=_top_left.top+fm.getHeight()*getText().count()+2*Math.round(Element.E_PADDING / 2);
		canvas.drawRect(myrect);
		
		myrect.bottom=_top_left.bottom;
		myrect.top=myrect.bottom-E_PADDING;
		canvas.drawRect(myrect);
		
		myrect=_top_left.copy();
		myrect.right=myrect.left+E_PADDING;
		canvas.drawRect(myrect);
		
		// fill shape
		canvas.setColor(drawColor);
		myrect.left=myrect.left+1;
		myrect.top=myrect.top+1;
		myrect.bottom=myrect.bottom;
		myrect.right=myrect.right-1;
		canvas.fillRect(myrect);
		
		myrect=_top_left.copy();
		myrect.bottom=_top_left.top+fm.getHeight()*getText().count()+2*Math.round(E_PADDING / 2);
		myrect.left=myrect.left+1;
		myrect.top=myrect.top+1;
		myrect.bottom=myrect.bottom;
		myrect.right=myrect.right-1;
		canvas.fillRect(myrect);
		
		myrect.bottom=_top_left.bottom;
		myrect.top=myrect.bottom-Element.E_PADDING;
		myrect.left=myrect.left+1;
		myrect.top=myrect.top+1;
		myrect.bottom=myrect.bottom;
		myrect.right=myrect.right;
		canvas.fillRect(myrect);
		
		// draw comment
		if(Element.E_SHOWCOMMENTS==true && !comment.getText().trim().equals(""))
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
		this.drawBreakpointMark(canvas, _top_left);
		// END KGU 2015-10-11
		
		
		// draw text
		for(int i=0;i<getText().count();i++)
		{
			String text = this.getText().get(i);
			text = BString.replace(text, "<--","<-");
			
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
							  _top_left.left+Math.round(E_PADDING / 2),
							  _top_left.top+Math.round(E_PADDING / 2)+(i+1)*fm.getHeight(),
							  text,this
							  );  	
		}
		
		// draw children
		myrect=_top_left.copy();
		myrect.left=myrect.left+Element.E_PADDING-1;
		myrect.top=_top_left.top+fm.getHeight()*getText().count()+2*Math.round(E_PADDING / 2)-1;
		myrect.bottom=myrect.bottom-E_PADDING+1;
		q.draw(_canvas,myrect);
	}
	
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
		Element sel = q.getElementByCoord(_x, _y, _forSelection);
		if(sel!=null) 
		{
			if (_forSelection) selected=false;
			selMe = sel;
		}
		
		return selMe;
	}
	// END KGU 2015-10-11
	
	public void setSelected(boolean _sel)
	{
		selected=_sel;
		//q.setSelected(_sel);
	}
	
	public Element copy()
	{
		Element ele = new For(this.getText().copy());
		ele.setComment(this.getComment().copy());
		ele.setColor(this.getColor());
		((For) ele).q=(Subqueue) this.q.copy();
		((For) ele).q.parent=ele;
		return ele;
	}
	
	
	// START KGU 2015-11-12
	@Override
	public void clearBreakpoints()
	{
		super.clearBreakpoints();
		this.q.clearBreakpoints();
	}
	// END KGU 2015-10-12
	
	// START KGU 2015-11-13
	@Override
	public void clearExecutionStatus()
	{
		super.clearExecutionStatus();
		this.q.clearExecutionStatus();
	}
	// END KGU 2015-10-12
	
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
	
}

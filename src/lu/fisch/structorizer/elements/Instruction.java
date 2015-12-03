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
 *      Description:    This class represents an "instruction" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2015.10.11/13   Comment drawing unified, breakpoints supported, colouring modified
 *      Kay Gürtzig     2015.11.14      Bugfix #31 (= KGU#82) in method copy
 *		Kay Gürtzig     2015.12.01      Bugfix #39 (KGU#91) -> getText(false) on drawing
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

public class Instruction extends Element {

	public Instruction()
	{
		super();
	}
	
	public Instruction(String _strings)
	{
		super(_strings);
		setText(_strings);	// FIXME (KGU 2015-10-13): What is this good for? This has already been done by the super constructor!
	}
	
	public Instruction(StringList _strings)
	{
		super(_strings);
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
	}
	// END KGU#64 2015-11-03
	

	public static Rect prepareDraw(Canvas _canvas, StringList _text, Element _element)
	{
		Rect rect = new Rect(0,0,0,0);

		FontMetrics fm = _canvas.getFontMetrics(Element.font);

		rect.right = 2*(Element.E_PADDING/2);
		
		for(int i=0;i<_text.count();i++)
		{
			int lineWidth = getWidthOutVariables(_canvas, _text.get(i), _element) + Element.E_PADDING;
			if (rect.right < lineWidth)
			{
				rect.right = lineWidth;
			}
		}
		rect.bottom = 2*(Element.E_PADDING/2) + _text.count() * fm.getHeight();

		return rect;
	}
        
	public Rect prepareDraw(Canvas _canvas)
	{
		/*rect.top=0;
		rect.left=0;
		rect.right=0;
		rect.bottom=0;
		
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		
                rect.right=Math.round(2*(Element.E_PADDING/2));
                for(int i=0;i<text.count();i++)
                {
                        if(rect.right<getWidthOutVariables(_canvas,text.get(i),this)+1*Element.E_PADDING)
                        {
                                rect.right=getWidthOutVariables(_canvas,text.get(i),this)+1*Element.E_PADDING;
                        }
                }
                rect.bottom=2*Math.round(Element.E_PADDING/2)+text.count()*fm.getHeight();
		
		return rect;*/
                rect = prepareDraw(_canvas, getText(false), this);
                return rect;
	}

	public static void draw(Canvas _canvas, Rect _top_left, StringList _text, Element _element)
	{
		Rect myrect = new Rect();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = _element.getColor();
		Color drawColor = _element.getFillColor();
		// END KGU 2015-10-13
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
			
		// START KGU 2015-10-13: Became obsolete by new method getFillColor() applied above now
//		if (_element.isCollapsed())
//		{
//			drawColor=Element.E_COLLAPSEDCOLOR;
//		}
//		if (_element.selected==true)
//		{
//			drawColor=Element.E_DRAWCOLOR;
//		}
		// END KGU 2015-10-13
		
		_element.rect=_top_left.copy();
		
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		myrect=_top_left.copy();
		
		canvas.fillRect(myrect);
				
		// draw comment
		if(Element.E_SHOWCOMMENTS==true && !_element.getComment(false).getText().trim().equals(""))
		{
			// START KGU 2015-10-11
//			canvas.setBackground(E_COMMENTCOLOR);
//			canvas.setColor(E_COMMENTCOLOR);
//			
//			myrect.left+=2;
//			myrect.top+=2;
//			myrect.right=myrect.left+4;
//			myrect.bottom-=1;
//			
//			canvas.fillRect(myrect);
			_element.drawCommentMark(canvas, myrect);
			// END KGU 2015-10-11
		}
		
		// START KGU 2015-10-11: If _element is a breakpoint, mark it
		_element.drawBreakpointMark(canvas, _top_left);
		// END KGU 2015-10-11
		
		for (int i = 0; i < _text.count(); i++)
		{
			String text = _text.get(i);
			text = BString.replace(text, "<--","<-");
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
					_top_left.left + (Element.E_PADDING / 2),
					_top_left.top + (Element.E_PADDING / 2) + (i+1)*fm.getHeight(),
					text,
					_element
					);  	

		}
		canvas.setColor(Color.BLACK);
		canvas.drawRect(_top_left);
	}
                
	public void draw(Canvas _canvas, Rect _top_left)
	{
		// Now delegates all stuff to the static method above, which may also
		// be called from Elements of different types when those are collapsed
		
		draw(_canvas, _top_left, getText(false), this);
	}
	
	public Element copy()
	{
		Instruction ele = new Instruction(this.getText().copy());
		ele.setComment(this.getComment().copy());
		ele.setColor(this.getColor());
		// START KGU#82 (bug #31) 2015-11-14
		ele.breakpoint = this.breakpoint;
		// END KGU#82 (bug #31) 2015-11-14
		return ele;
	}

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
   		_lines.add(this.getText());
    }
    // END KGU 2015-10-16

}

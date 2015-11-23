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
 *      Description:    This class represents a sequence of simple and structured elements.
 *						A subqueue can contain other elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,true)
 *      Kay Gürtzig     2015.10.12      Comment drawing centralized and breakpoint mechanism prepared.
 *      Kay Gürtzig     2015.11.22      New and modified methods to support operations on non-empty Subqueues (KGU#87).
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.Vector;
import java.awt.Color;
import java.awt.FontMetrics;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;

public class Subqueue extends Element{

	public Subqueue()
	{
		super("");
	}
	
	public Subqueue(StringList _strings)
	{
		super(_strings);
	}
	
	public Vector<Element> children = new Vector<Element>();
	
	// START KGU#64 2015-11-03: Is to improve drawing performance
	/**
	 * Recursively clears all drawing info this subtree down
	 * (To be overridden by structured sub-classes!)
	 */
	@Override
	public void resetDrawingInfoDown()
	{
		this.resetDrawingInfo();
		if (this.children != null)
		{
			for (int i = 0; i < this.children.size(); i++)
			{
				this.children.get(i).resetDrawingInfoDown();
			}
		}
	}
	// END KGU#64 2015-11-03
	
	public Rect prepareDraw(Canvas _canvas)
	{
		Rect subrect = new Rect();
		
		rect.top=0;
		rect.left=0;
		rect.right=0;
		rect.bottom=0;
		
		if(children.size()>0) 
		{
			for(int i=0;i<children.size() ;i++)
			{
				subrect = ((Element) children.get(i)).prepareDraw(_canvas);
				rect.right=Math.max(rect.right,subrect.right);
				rect.bottom+=subrect.bottom;
			}
		}
		else
		{
			rect.right=2*Element.E_PADDING;
			FontMetrics fm = _canvas.getFontMetrics(Element.font);
			rect.bottom = fm.getHeight() + 2* Math.round(Element.E_PADDING/2);

		}
		
		return rect;
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{
		Rect myrect;
		Rect subrect;
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = getColor();
		Color drawColor = getFillColor();
		// END KGU 2015-10-13
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		Canvas canvas = _canvas;		
		// START KGU 2015-10-13: Became obsolete by new method getFillColor() applied above now
//		if (selected==true)
//		{
//			drawColor=Element.E_DRAWCOLOR;
//		}
		// END KGU 2015-10-13
		
		rect = _top_left.copy();
		
		myrect = _top_left.copy();
		myrect.bottom = myrect.top;
		
		if (children.size() > 0)
		{
			// draw children
			for(int i=0; i<children.size(); i++)
			{
				subrect = ((Element) children.get(i)).prepareDraw(_canvas);
				myrect.bottom+=subrect.bottom;
				if(i==children.size()-1)
				{
					myrect.bottom=_top_left.bottom;
				}
				((Element) children.get(i)).draw(_canvas,myrect);

				//myrect.bottom-=1;
				myrect.top+=subrect.bottom;
			}
		}
		else
		{
			// draw empty set symbol
			rect=_top_left.copy();
			
			canvas.setBackground(drawColor);
			canvas.setColor(drawColor);
			
			myrect=_top_left.copy();
			
			canvas.fillRect(myrect);
			
			canvas.setColor(Color.BLACK);
			canvas.writeOut(_top_left.left+((_top_left.right-_top_left.left) / 2) - (_canvas.stringWidth("\u2205") / 2),
							_top_left.top +((_top_left.bottom-_top_left.top) / 2) + (fm.getHeight() / 2),
							"\u2205"
							);  	

			canvas.drawRect(_top_left);
		}
	}
	
	public int getSize()
	{
		return children.size();
	}
	
	public int getIndexOf(Element _ele)
	{
		return children.indexOf(_ele);
	}
	
	public Element getElement(int _index)
	{
		return (Element) children.get(_index);
	}
	
	public void addElement(Element _element)
	{
		// START KGU#87 2015-11-22: We must make sure a Subqueue as _element is properly appended
//		children.add(_element);
//		_element.parent=this;
		insertElementAt(_element, children.size());
		// END KGU#87 2015-11-22
	}

	// START KGU#87 2015-11-22: Allow the insertion of all children of another Subqueue
	/**
	 * Inserts the given _element before child no. _where (if 0 <= _where <= this.getSize()).
	 * If _element is another Suqueue, however, all children of _element will be inserted before
	 * the child _where, instead.
	 * @param _element - an Element to be inserted (or the children of which are to be inserted here)
	 * @param _where - index of the child, which _element (or _element's children) is to inserted before  
	 */
	public void insertElementAt(Element _element, int _where)
	{
		if (_element instanceof Subqueue)
		{
			for (int i = 0; i < ((Subqueue)_element).getSize(); i++)
			{
				insertElementAt(((Subqueue)_element).getElement(i), _where + i);
			}
		}
		else
		{
			children.insertElementAt(_element, _where);
			_element.parent=this;
		}
	}

	public void clear()
	{
		children.clear();
	}
	// END KGU#87 2015-11-22
	
	
	public void removeElement(Element _element)
	{
		children.removeElement(_element);
	}
	
	public void removeElement(int _index)
	{
		// START KGU 2015-11-22: Why search if we got the index?
		//children.removeElement(children.get(_index));
		children.removeElementAt(_index);
		// END KGU 2015-11-22
	}
	
	// START KGU 2015-10-15: Methods merged to Element getElementByCoord(int _x, int _y, _boolean _forSelection)
//	@Override
//	public Element selectElementByCoord(int _x, int _y)
//	{
//		Element res = super.selectElementByCoord(_x,_y);
//		Element sel = null;
//		for(int i=0;i<children.size();i++)
//		{
//			sel=((Element) children.get(i)).selectElementByCoord(_x, _y);
//			if (sel != null)
//			{
//				selected=false;
//				res = sel;
//			}
//		}
//		return res;
//	}
//
//	@Override
//	public Element getElementByCoord(int _x, int _y)
//	{
//		Element res = super.getElementByCoord(_x,_y);
//		Element sel = null;
//		for(int i=0;i<children.size();i++)
//		{
//			sel=((Element) children.get(i)).getElementByCoord(_x, _y);
//			if (sel != null)
//			{
//				res = sel;
//			}
//		}
//		return res;
//	}

	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element res = super.getElementByCoord(_x, _y, _forSelection);
		Element sel = null;
		for (int i = 0; i < children.size(); i++)
		{
			sel = ((Element) children.get(i)).getElementByCoord(_x, _y, _forSelection);
			if (sel != null)
			{
				if (_forSelection) selected = false;
				res = sel;
			}
		}
		//System.out.println(this + ".getElementByCoord("+_x + ", " + _y + ") returning " + (res == null ? "null" : res));
		return res;
	}
	// END KGU 2015-10-11
	
	public Element copy()
	{
		Element ele = new Subqueue();
		ele.setColor(this.getColor());
		for(int i = 0; i < children.size(); i++)
		{
			((Subqueue) ele).addElement(((Element) children.get(i)).copy());
		}
		return ele;
	}
        
    // START KGU#87 2015-11-22: Re-enabled for multiple selection (selected non-empty subqueues)    
    @Override
    public void setColor(Color _color) 
    {
        super.setColor(_color);
        for(int i=0; i<children.size(); i++)
        {      
            children.get(i).setColor(_color);
        }
    }
	// END KGU#87 2015-11-22

	// START KGU 2015-11-12
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#clearBreakpoints()
	 */
	@Override
	public void clearBreakpoints()
	{
		super.clearBreakpoints();
        for(int i = 0; i < children.size(); i++)
        {      
            children.get(i).clearBreakpoints();
        }
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#clearExecutionStatus()
	 */
	@Override
	public void clearExecutionStatus()
	{
        for(int i = 0; i < children.size(); i++)
        {      
        	super.clearExecutionStatus();	// FIXME: Is this necessary at all?
            children.get(i).clearExecutionStatus();
        }
	}
	// END KGU 2015-10-12

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
		// No own text is to be considered here
        for(int i = 0; i < children.size(); i++)
        {      
            children.get(i).addFullText(_lines, _instructionsOnly);
        }
    }
    // END KGU 2015-10-16

	// START KGU#87 2015-11-22: Allow the selection flagging of all immediate children
	@Override
	public void setSelected(boolean _sel)
	{
		selected=_sel;
		for (int i = 0; i < getSize(); i++)
		{
			// This must not be recursive!
			children.get(i).selected = _sel;
		}
	}
	// END KGU#87 2015-11-22
	
}

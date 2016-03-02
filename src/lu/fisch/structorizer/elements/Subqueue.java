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
 *      Kay Gürtzig     2015.11.23      Inheritance extended to IElementSequence (KGU#87), children now private.
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016-01-03      Enh. #87: Collapsing mechanism for selected Subqueue (KGU#123)
 *      Kay Gürtzig     2016-01-22      Bugfix #114: Method isExecuted() added (KGU#143)
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.gui.SelectedSequence;
import lu.fisch.utils.*;

public class Subqueue extends Element implements IElementSequence {

	public Subqueue()
	{
		// START KGU#91 2015-12-01: A Subqueue has no own text, not even an empty line
		//super("");
		super();
		// END KGU#91 2015-12-01
	}
	
	public Subqueue(StringList _strings)
	{
		// START KGU#91 2015-12-01: A Subqueue has no own text, not even an empty line
		//super(_strings);
		super();
		// END KGU#91 2015-12-01
	}
	
	private Vector<Element> children = new Vector<Element>();
	// START KGU#136 2016-03-01: Bugfix #97
	private Vector<Integer> y0Children = new Vector<Integer>();
	// END KGU#136 2016-03-01
	
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
		// START KGU#136 2016-01-03: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		this.y0Children.clear();
		// END KGU#136 2016-01-03

		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		Rect subrect = new Rect();
		
		rect0.top = 0;
		rect0.left = 0;
		rect0.right = 0;
		rect0.bottom = 0;
		
		if (children.size() > 0) 
		{
			for(int i = 0; i < children.size(); i++)
			{
				// START KGU#136 2016-03-01: Bugfix #97
				this.y0Children.addElement(rect0.bottom);
				// END KGU#136 2016-03-01
				subrect = ((Element) children.get(i)).prepareDraw(_canvas);
				rect0.right = Math.max(rect0.right, subrect.right);
				rect0.bottom += subrect.bottom;
			}
		}
		else
		{
			// START KGU#136 2016-03-01: Bugfix #97
			this.y0Children.addElement(rect0.bottom);
			// END KGU#136 2016-03-01
			rect0.right = 2*Element.E_PADDING;
			FontMetrics fm = _canvas.getFontMetrics(Element.font);
			rect0.bottom = fm.getHeight() + 2*(Element.E_PADDING/2);

		}
		
		// START KGU#136 2016-03-01: Bugfix #97
		isRectUpToDate = true;
		// END KGU#136 2016-03-01
		return rect0;
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
		
		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		Point dP = this.getDrawPoint();
		this.topLeft.x = _top_left.left - dP.x;
		this.topLeft.y = _top_left.top - dP.y;
		// END KGU#136 2016-03-01
		
		myrect = _top_left.copy();
		myrect.bottom = myrect.top;
		
		if (children.size() > 0)
		{
			// draw children
			for(int i=0; i<children.size(); i++)
			{
				subrect = ((Element) children.get(i)).prepareDraw(_canvas);
				myrect.bottom += subrect.bottom;
				if (i==children.size()-1)
				{
					myrect.bottom = _top_left.bottom;
				}
				((Element) children.get(i)).draw(_canvas, myrect);

				//myrect.bottom-=1;
				myrect.top += subrect.bottom;
			}
		}
		else
		{
			// draw empty set symbol
			canvas.setBackground(drawColor);
			canvas.setColor(drawColor);
			
			myrect = _top_left.copy();
			
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
	 * If _element is another implementor of IElementSequence, however, all children of _element
	 * will be inserted before the child _where, instead.
	 * @param _element - an Element to be inserted (or the children of which are to be inserted here)
	 * @param _where - index of the child, which _element (or _element's children) is to inserted before  
	 */
	public void insertElementAt(Element _element, int _where)
	{
		if (_element instanceof IElementSequence)
		{
			for (int i = 0; i < ((IElementSequence)_element).getSize(); i++)
			{
				insertElementAt(((IElementSequence)_element).getElement(i), _where + i);
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
	
	// START KGU#136 2016-03-02: New method to facilitate bugfix #97
	public boolean moveElement(int _from, int _to)
	{
		boolean done = 0 <= _from && _from < children.size() && 0 <= _to && _to < children.size();
		if (done)
		{
			Element ele = children.get(_from);
			children.removeElementAt(_from);
			children.insertElementAt(ele, _to);
			this.resetDrawingInfo();	// Element start points must be re-computed
		}
		return done;
	}
	// END KGU#136 2016-03-02
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.IElementContainer#removeElements()
	 */
	@Override
	public void removeElements() {
		clear();
	}
	
	public Iterator<Element> getIterator()
	{
		return children.iterator();
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
			// START KGU#136 2016-03-01: Bugfix #97
			//sel = ((Element) children.get(i)).getElementByCoord(_x, _y, _forSelection);
			if (i < this.y0Children.size())
			{
				int yOff = this.y0Children.get(i);
				sel = children.get(i).getElementByCoord(_x, _y-yOff, _forSelection);
			}
			// END KGU#136 2016-03-01
			if (sel != null)
			{
				if (_forSelection) selected = false;
				res = sel;
			}
		}
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
		boolean isEqual = super.equals(_another) && this.children.size() == ((Subqueue)_another).getSize();
		for (int i = 0; isEqual && i < children.size(); i++)
		{
			isEqual = children.get(i).equals(((Subqueue)_another).getElement(i));
		}
		return isEqual;
	}
	// END KGU#119 2016-01-02

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

	// START KGU#43 2016-01-22: Method to control the breakpoint property of the sub-elements
	@Override
	public void toggleBreakpoint()
	{
		for (int i = 0; i < this.getSize(); i++)
		{
			this.getElement(i).toggleBreakpoint();
		}
	}
	// END KGU#43 2016-01-22

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

	// START KGU#143 2016-01-22: Bugfix #114 - we need a method to decide execution involvement
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isExecuted()
	 */
	@Override
	public boolean isExecuted()
	{
		boolean involved = false;
		for (int index = 0; !involved && index < this.getSize(); index++)
		{
			if (children.get(index).isExecuted())
			{
				involved = true;
			}
		}
		return involved;
	}
	// END KGU#143 2016-01-22

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

	// START KGU#123 2016-01-03: We need a collective collapsing/expansion now
	@Override
    public void setCollapsed(boolean collapsed) {
        super.setCollapsed(false);	// the Subqueue itself will never be collapsed
        Iterator<Element> iter = getIterator();
        while (iter.hasNext())
        {
        	iter.next().setCollapsed(collapsed);
        }
    }
	// END KGU#123 2016-01-03
	
}

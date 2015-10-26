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
 *      Description:    This class represents a "subqueue" of another element.
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
			// draw nothing
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
		children.add(_element);
		_element.parent=this;
	}
	
	public void removeElement(Element _element)
	{
		children.removeElement(_element);
	}
	
	public void removeElement(int _index)
	{
		children.removeElement(children.get(_index));
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
        
        
    /*@Override
    public void setColor(Color _color) 
    {
        super.setColor(_color);
        for(int i=0;i<children.size();i++)
        {      
            children.get(i).setColor(_color);
        }
    }*/

	// START KGU 2015-11-12
	@Override
	public void clearBreakpoints()
	{
		super.clearBreakpoints();
        for(int i = 0; i < children.size(); i++)
        {      
            children.get(i).clearBreakpoints();
        }
	}

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
		
}

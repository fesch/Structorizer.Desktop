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
 *      Description:    This class represents an "WHILE loop" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.12      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.Vector;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;


public class While extends Element {

	public Subqueue q = new Subqueue();
	
	private Rect r = new Rect();
	
	public While()
	{
		super();
		q.parent=this;
	}
	
	public While(String _strings)
	{
		super(_strings);
		q.parent=this;
		setText(_strings);
	}
	
	public While(StringList _strings)
	{
		super(_strings);
		q.parent=this;
		setText(_strings);
	}
	
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
		
		FontMetrics fm = _canvas.getFontMetrics(font);
		
		rect.right=Math.round(2*(E_PADDING/2));
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
		rect.bottom+=r.bottom;		
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
		Color drawColor = getColor();
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		int p;
		int w;
		
		if (selected==true)
		{
                if(waited==true) { drawColor=Element.E_WAITCOLOR; }
                else { drawColor=Element.E_DRAWCOLOR; }
		}
		
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		rect=_top_left.copy();
		
		// draw shape
		myrect=_top_left.copy();
		canvas.setColor(Color.BLACK);
		myrect.bottom=_top_left.top+fm.getHeight()*getText().count()+2*Math.round(E_PADDING / 2);
		canvas.drawRect(myrect);
		
		myrect=_top_left.copy();
		myrect.right=myrect.left+Element.E_PADDING;
		canvas.drawRect(myrect);
		
		// fill shape
		canvas.setColor(drawColor);
		myrect.left=myrect.left+1;
		myrect.top=myrect.top+1;
		myrect.bottom=myrect.bottom;
		myrect.right=myrect.right;
		canvas.fillRect(myrect);
	
		myrect=_top_left.copy();
		myrect.bottom=_top_left.top+fm.getHeight()*getText().count()+2*Math.round(E_PADDING / 2);
		myrect.left=myrect.left+1;
		myrect.top=myrect.top+1;
		myrect.bottom=myrect.bottom;
		myrect.right=myrect.right;
		canvas.fillRect(myrect);
		
		// draw comment
		if(Element.E_SHOWCOMMENTS==true && !comment.getText().trim().equals(""))
		{
			canvas.setBackground(E_COMMENTCOLOR);
			canvas.setColor(E_COMMENTCOLOR);
			
			Rect someRect = _top_left.copy();
			
			someRect.left+=2;
			someRect.top+=2;
			someRect.right=someRect.left+4;
			someRect.bottom-=1;
			
			canvas.fillRect(someRect);
		}
		
		
		myrect=_top_left.copy();
		// draw text
		for(int i=0;i<getText().count();i++)
		{
			String text = this.getText().get(i);
			
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
		q.draw(_canvas,myrect);
		
	}
	
	public Element selectElementByCoord(int _x, int _y)
	{
		Element selMe = super.selectElementByCoord(_x,_y);
		Element sel = q.selectElementByCoord(_x,_y);
		if(sel!=null) 
		{
			selected=false;
			selMe = sel;
		}
		
		return selMe;
	}
	
	public void setSelected(boolean _sel)
	{
		selected=_sel;
		//q.setSelected(_sel);
	}
	
	public Element copy()
	{
		Element ele = new While(this.getText().copy());
		ele.setComment(this.getComment().copy());
		ele.setColor(this.getColor());
		((While) ele).q=(Subqueue) this.q.copy();
		((While) ele).q.parent=ele;
		return ele;
	}
	
    /*@Override
    public void setColor(Color _color) 
    {
        super.setColor(_color);
        q.setColor(_color);
    }*/
	

}

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
 *      Description:    This class represents an "call" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.13      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015.10.12      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015.11.14      Bugfix #31 (= KGU#82) in method copy
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
import lu.fisch.structorizer.elements.*;

public class Call extends Instruction {
	
	public Call()
	{
		super();
	}
	
	public Call(String _strings)
	{
		super(_strings);
		setText(_strings);	// FIXME (KGU 2015-10-13): What is this good for? This has already been done by both the super and its super constructor!
	}
	
	public Call(StringList _strings)
	{
		super(_strings);
		setText(_strings);
	}
	
	
	public Rect prepareDraw(Canvas _canvas)
	{
		rect.top=0;
		rect.left=0;
		rect.right=0;
		rect.bottom=0;
		
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		
		rect.right=Math.round(2*(E_PADDING/2));
		
		for(int i=0;i<getText().count();i++)
		{
			if(rect.right<getWidthOutVariables(_canvas,getText().get(i),this)+4*E_PADDING)
			{
				rect.right=getWidthOutVariables(_canvas,getText().get(i),this)+4*E_PADDING;
			}
		}
		rect.bottom=2*Math.round(E_PADDING/2)+getText().count()*fm.getHeight();

		return rect;
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{
		Rect myrect = new Rect();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = getColor();
		Color drawColor = getFillColor();
		// END KGU 2015-10-13
		FontMetrics fm = _canvas.getFontMetrics(Element.font);

		// START KGU 2015-10-13: Became obsolete by new method getFillColor() applied above now
//		if (selected==true)
//		{
//			drawColor=E_DRAWCOLOR;
//		}
		// END KGU 2015-10-13
		
		rect=_top_left.copy();
		
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		myrect=_top_left.copy();
		
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
		
		
		for(int i=0;i<getText().count();i++)
		{
			String text = this.getText().get(i);
			text = BString.replace(text, "<--","<-");
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
							  _top_left.left+2*Math.round(E_PADDING / 2),
							_top_left.top+Math.round(E_PADDING / 2)+(i+1)*fm.getHeight(),
							text,this
							);  	
		}
		
		canvas.moveTo(_top_left.left+Math.round(E_PADDING / 2),_top_left.top);
		canvas.lineTo(_top_left.left+Math.round(E_PADDING / 2),_top_left.bottom);
		canvas.moveTo(_top_left.right-Math.round(E_PADDING / 2),_top_left.top);
		canvas.lineTo(_top_left.right-Math.round(E_PADDING / 2),_top_left.bottom);
		
		canvas.setColor(Color.BLACK);
		canvas.drawRect(_top_left);
	}
	
	public Element copy()
	{
		Element ele = new Call(this.getText().copy());
		ele.setComment(this.getComment().copy());
		ele.setColor(this.getColor());
		// START KGU#82 (bug #31) 2015-11-14
		ele.breakpoint = this.breakpoint;
		// END KGU#82 (bug #31) 2015-11-14
		return ele;
	}
	

	
}

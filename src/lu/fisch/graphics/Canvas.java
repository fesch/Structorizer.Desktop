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

package lu.fisch.graphics;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Class to represent a drawing canvas.
 *						Aims to work like a "TCanvas" in Delphi
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.10      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************/


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;


import lu.fisch.utils.*;

public class Canvas  {
	
	protected Graphics2D canvas = null;
	private int x;
	private int y;
	
	public Canvas(Graphics2D _canvas)
	{
		canvas=_canvas;
                if(canvas!=null) canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	public void draw(Image _img, int _x, int _y)
	{
		canvas.drawImage(_img,_x,_y,null);
	}
	
	public FontMetrics getFontMetrics(Font _font)
	{
		return canvas.getFontMetrics(_font);
	}
	
	public int stringWidth(String _string)
        {
		Rectangle2D bounds = canvas.getFont().getStringBounds(_string,canvas.getFontRenderContext());
		return new Double(bounds.getWidth()).intValue();
	}


	
	public int stringHeight(String _string)
	{
		Rectangle2D bounds = canvas.getFont().getStringBounds(_string,canvas.getFontRenderContext());
		return new Double(bounds.getHeight()).intValue();
	}
	
	public Font getFont()
	{
		return canvas.getFont();
	}
	
	public void setFont(Font _font)
	{
		canvas.setFont(_font);
	}
	
	public void setColor(Color _color)
	{
		canvas.setColor(_color);
	}
	
	public void setBackground(Color _color)
	{
		canvas.setBackground(_color);
	}
	
	public void drawRect(Rect _rect)
	{
		canvas.drawRect(_rect.left, _rect.top, _rect.right-_rect.left, _rect.bottom-_rect.top);
	}	
	
	public void roundRect(Rect _rect)
	{
		canvas.drawRoundRect(_rect.left, _rect.top, _rect.right-_rect.left, _rect.bottom-_rect.top,30,30);
	}	
	
	public void fillRect(Rect _rect)
	{
		canvas.fillRect(_rect.left, _rect.top, _rect.right-_rect.left, _rect.bottom-_rect.top);
	}	
	
	public void writeOut(int _x, int _y, String _text)
	{
		String display = new String(_text);
		
		display = BString.replace(display, "<--","<-");
		display = BString.replace(display, "<-","\u2190");
		
		canvas.drawString(display, _x, _y);
	}
	
	public void moveTo(int _x, int _y)
	{
		x=_x;
		y=_y;
	}
	
	public void lineTo(int _x, int _y)
	{
		canvas.drawLine(x,y,_x,_y);
		moveTo(_x,_y);
	}
	
}

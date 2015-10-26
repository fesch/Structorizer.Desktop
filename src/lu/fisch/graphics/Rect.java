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
 *      Description:    Class to represent a rectangle zone on the screen.
 *						Works like a "TRect" in Delphi
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************/


public class Rect{

	public int top;
	public int left;
	public int right;
	public int bottom;
	
	public Rect(int _left, int _top, int _right, int _bottom)
	{
		this.left=_left;
		this.top=_top;
		this.bottom=_bottom;
		this.right=_right;
	}
	
	public Rect()
	{		
		this.left=0;
		this.top=0;
		this.bottom=0;
		this.right=0;
	}
	
	public Rect copy()
	{
		Rect rect= new Rect();
		
		rect.left=this.left;
		rect.top=this.top;
		rect.bottom=this.bottom;
		rect.right=this.right;
		
		return rect;
	}

        public String toString()
        {
            return "Rect = ["+left+","+top+","+right+","+bottom+"]";
        }
}

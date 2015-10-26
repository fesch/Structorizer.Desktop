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
 *      Description:    This class represents an "CASE statement" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.12      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015.10.11      Comment drawing centralized and breakpoint mechanism prepared
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


public class Case extends Element
{
	
    public Vector<Subqueue> qs = new Vector<Subqueue>();

    private Rect r = new Rect();
    private int fullWidth = 0;
    private int maxHeight = 0;
	
    @Override
    public void setText(String _text)
    {

            Subqueue s = null;

            getText().setText(_text);

            if(qs==null)
            {
                    qs = new Vector();
            }

            if(getText().count()>1)
            {
                    while(getText().count()-1>qs.size())
                    {
                            s=new Subqueue();
                            s.parent=this;
                            qs.add(s);
                    }
                    while(getText().count()-1<qs.size())
                    {
                            qs.removeElementAt(qs.size()-1);
                    }/**/
                    /*
                    for(int i=0;i<text.count()-1;i++)
                    {
                            s=new Subqueue();
                            s.parent=this;
                            qs.add(s);
                    }
                    /**/
            }

    }

    @Override
    public void setText(StringList _textList)
    {
            Subqueue s = null;

            text=_textList;

            if(qs==null)
            {
                    qs = new Vector();
            }

            if(getText().count()>1)
            {
                    while(getText().count()-1>qs.size())
                    {
                            s=new Subqueue();
                            s.parent=this;
                            qs.add(s);
                    }
                    while(getText().count()-1<qs.size())
                    {
                            qs.removeElementAt(qs.size()-1);
                    }/**/
                    /*
                     for(int i=0;i<text.count()-1;i++)
                     {
                     s=new Subqueue();
                     s.parent=this;
                     qs.add(s);
                     }
                     /**/
            }

    }

    public Case()
    {
            super();
    }

    public Case(String _strings)
    {
            super(_strings);
            setText(_strings);
    }

    public Case(StringList _strings)
    {
            super(_strings);
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

            FontMetrics fm = _canvas.getFontMetrics(font);

            rect.right=Math.round(2*(E_PADDING/2));

            for(int i=0;i<getText().count();i++)
            {
                    if(rect.right<getWidthOutVariables(_canvas,getText().get(i),this)+2*Math.round(E_PADDING/2))
                    {
                            rect.right=getWidthOutVariables(_canvas,getText().get(i),this)+2*Math.round(E_PADDING/2);
                    }
            }

            rect.bottom=4*Math.round(E_PADDING/2)+2*fm.getHeight();

            int count = 0;
            Rect rtt = null;

            fullWidth=0;
            maxHeight=0;

            if (qs.size()!=0)
            {
                    count=getText().count()-1;
                    if(((String)getText().get(count)).equals("%"))
                    {
                       count-=1;
                    }

                    for (int i=0;i<count;i++)
                    {
                       rtt=((Subqueue) qs.get(i)).prepareDraw(_canvas);
                       fullWidth=fullWidth+Math.max(rtt.right,getWidthOutVariables(_canvas,getText().get(i+1),this)+Math.round(E_PADDING / 2));
                       if(maxHeight<rtt.bottom)
                       {
                            maxHeight=rtt.bottom;
                       }
                    }
            }

            rect.right=Math.max(rect.right,fullWidth);
            rect.bottom=rect.bottom+maxHeight;

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
            FontMetrics fm = _canvas.getFontMetrics(Element.font);
//            int p;
//            int w;

            // START KGU 2015-10-13: Already done by new method getFillColor() now
//            if (selected==true)
//            {
//                if(waited==true) { drawColor=Element.E_WAITCOLOR; }
//                else { drawColor=Element.E_DRAWCOLOR; }
//            }
            // END KGU 2015-10-13

            Canvas canvas = _canvas;
            canvas.setBackground(drawColor);
            canvas.setColor(drawColor);

            rect=_top_left.copy();

            // fill shape
            canvas.setColor(drawColor);
            myrect=_top_left.copy();
            myrect.left+=1;
            myrect.top+=1;
            myrect.bottom-=1;
            myrect.bottom=_top_left.top+2*fm.getHeight()+4*Math.round(E_PADDING / 2);
            //myrect.right-=1;
            canvas.fillRect(myrect);

            // draw shape
            myrect=_top_left.copy();
            myrect.bottom=_top_left.top+2*fm.getHeight()+4*Math.round(E_PADDING / 2);

            int y=myrect.top+E_PADDING;
            int a=myrect.left+Math.round((myrect.right-myrect.left) / 2);
            int b=myrect.top;
            int c=myrect.left+fullWidth-1;
            int d=myrect.bottom-1;
            int x=Math.round(((y-b)*(c-a)+a*(d-b))/(d-b));

            // draw text
            for(int i=0;i<1;i++)
            {
                    String text = this.getText().get(i);
                    canvas.setColor(Color.BLACK);
                    if(((String) this.getText().get(this.getText().count()-1)).equals("%"))
                    {
                            writeOutVariables(canvas,
                                                              x-Math.round(getWidthOutVariables(canvas,text,this) / this.getText().count()),
                                                            myrect.top+Math.round(E_PADDING / 3)+(i+1)*fm.getHeight(),
                                                            text,this
                                                            );
                    }
                    else
                    {
                            writeOutVariables(canvas,
                                                              x-Math.round(getWidthOutVariables(_canvas,text,this) /2),
                                                            myrect.top+Math.round(E_PADDING / 3)+(i+1)*fm.getHeight(),
                                                            text,this
                                                            );
                    }
            }


            // draw comment
            if(Element.E_SHOWCOMMENTS==true && !comment.getText().trim().equals(""))
            {
                // START KGU 2015-10-11: Use an inherited helper method now
//                    canvas.setBackground(E_COMMENTCOLOR);
//                    canvas.setColor(E_COMMENTCOLOR);
//
//                    Rect someRect = myrect.copy();
//
//                    someRect.left+=2;
//                    someRect.top+=2;
//                    someRect.right=someRect.left+4;
//                    someRect.bottom-=2;
//
//                    canvas.fillRect(someRect);
    			this.drawCommentMark(canvas, myrect);
        		// END KGU 2015-10-11
    		}
            // START KGU 2015-10-11
    		// draw breakpoint bar if necessary
    		this.drawBreakpointMark(canvas, myrect);
    		// END KGU 2015-10-11


            // draw lines
            canvas.setColor(Color.BLACK);
            int lineWidth=0;
            // if the last line is '%', do not draw an else part
            int count=getText().count()-2;
            Rect rtt = null;

            for(int i = 0; i<count;i++)
            {
                    rtt=((Subqueue) qs.get(i)).prepareDraw(_canvas);
                    lineWidth=lineWidth+Math.max(rtt.right,getWidthOutVariables(_canvas,getText().get(i+1),this)+Math.round(E_PADDING / 2));
            }

            if( ((String) getText().get(getText().count()-1)).equals("%"))
            {
                    lineWidth=_top_left.right;
            }

            int ax=myrect.left;
            int ay=myrect.top;
            int bx=myrect.left+lineWidth;
            int by=myrect.bottom-1-fm.getHeight()-Math.round(E_PADDING / 2);

            if(  ((String) getText().get(getText().count()-1)).equals("%") )
            {
                    bx=myrect.right;
            }

            canvas.moveTo(ax,ay);
            canvas.lineTo(bx,by);

            if( ! ((String) text.get(text.count()-1)).equals("%") )
            {
                    canvas.lineTo(bx,myrect.bottom-1);
                    canvas.lineTo(bx,by);
                    canvas.lineTo(myrect.right,myrect.top);
            }


            // draw children
            myrect=_top_left.copy();
            myrect.top=_top_left.top+fm.getHeight()*2+4*Math.round(E_PADDING / 2)-1;

            if (qs.size()!=0)
            {

                    // if the last line is '%', do not draw an else part
                    count=qs.size()-1;
                    if( ((String) getText().get(getText().count()-1)).equals("%"))
                    {
                            count-=1;
                    }

                    for(int i = 0;i <=count ; i++)
                    {
                            rtt=((Subqueue) qs.get(i)).prepareDraw(_canvas);

                            if(i==count)
                            {
                                    myrect.right=_top_left.right;
                            }
/*
                            else if((i!=qs.size()-1) || (!(this.parent.parent.getClass().getCanonicalName().equals("Root"))))
                            {
                                    myrect.right=myrect.left+Math.max(rtt.right,_canvas.stringWidth(text.get(i+1)+Math.round(E_PADDING / 2)));
                            }
*/
                            else
                            {
                                    myrect.right=myrect.left+Math.max(rtt.right,getWidthOutVariables(_canvas,getText().get(i+1),this)+Math.round(E_PADDING / 2))+1;
                            }

                            // draw child
                            ((Subqueue) qs.get(i)).draw(_canvas,myrect);

                            // draw text
                            writeOutVariables(canvas,
                                                              myrect.right+Math.round((myrect.left-myrect.right) / 2)-Math.round(getWidthOutVariables(_canvas,getText().get(i+1),this) / 2),
                                                            myrect.top-Math.round(E_PADDING / 4), //+fm.getHeight(),
                                                            getText().get(i+1),this);

                            // draw bottom up line
                            if((i!=qs.size()-2)&&(i!=count))
                            {
                                    canvas.moveTo(myrect.right-1,myrect.top);
                                    int mx=myrect.right-1;
                                    int my=myrect.top-fm.getHeight();
                                    int sx=mx;
                                    int sy=Math.round((sx*(by-ay)-ax*by+ay*bx)/(bx-ax));
                                    canvas.lineTo(sx,sy+1);
                            }

                            myrect.left=myrect.right-1;

                    }
            }

            canvas.setColor(Color.BLACK);
            canvas.drawRect(_top_left);
    }

	// START KGU 2015-10-09: On moving the cursor, substructures had been eclipsed
	// by their containing box wrt. comment popping etc. This correction, however,
	// might significantly slow down the mouse tracking on enabled comment popping.
    // Just give it a try... 
//    public Element selectElementByCoord(int _x, int _y)
//    {
//            Element selMe = super.selectElementByCoord(_x,_y);
//            Element selCh = null;
//
//            for(int i = 0;i<qs.size();i++)
//            {
//                    Element pre = ((Subqueue) qs.get(i)).selectElementByCoord(_x,_y);
//                    if(pre!=null)
//                    {
//                            selCh = pre;
//                    }
//            }
//
//            if(selCh!=null)
//            {
//                    selected=false;
//                    selMe = selCh;
//            }
//
//            return selMe;
//    }

    @Override
    public Element getElementByCoord(int _x, int _y, boolean _forSelection)
    {
    	Element selMe = super.getElementByCoord(_x, _y, _forSelection);
    	Element selCh = null;

    	for(int i = 0; i<qs.size(); i++)
    	{
    		Element pre = ((Subqueue) qs.get(i)).getElementByCoord(_x,_y, _forSelection);
    		if(pre!=null)
    		{
    			selCh = pre;
    		}
    	}

    	if(selCh!=null)
    	{
    		if (_forSelection) selected = false;
    		selMe = selCh;
    	}

    	return selMe;
    }
    // END KGU 2015-10-09
    
    public void setSelected(boolean _sel)
    {
            selected=_sel;
            /* Quatsch !
            for(int i = 0;i<qs.size();i++)
            {
                    ((Subqueue) qs.get(i)).setSelected(_sel);
            }
            */
    }

    public Element copy() // Problem here???
    {
            Element ele = new Case(this.getText().getText());
            //ele.setText(this.getText().copy());
            ele.setComment(this.getComment().copy());
            ele.setColor(this.getColor());
            ((Case) ele).qs.clear();
            for(int i=0;i<qs.size();i++)
            {
                    Subqueue ss = (Subqueue) ((Subqueue) this.qs.get(i)).copy();
                    ss.parent=ele;
                    ((Case) ele).qs.add(ss);
            }

            return ele;
    }

    // START KGU 2015-10-12
    @Override
    public void clearBreakpoints()
    {
    	super.clearBreakpoints();
    	if (qs!= null)
    	{
    		for (int i = 0; i < qs.size(); i++)
    		{
    			qs.get(i).clearBreakpoints();
    		}
    	}
    }
    // END KGU 2015-10-12

    // START KGU 2015-10-13
    @Override
    public void clearExecutionStatus()
    {
    	super.clearExecutionStatus();
    	if (qs!= null)
    	{
    		for (int i = 0; i < qs.size(); i++)
    		{
    			qs.get(i).clearExecutionStatus();
    		}
    	}
    }
    // END KGU 2015-10-13


}

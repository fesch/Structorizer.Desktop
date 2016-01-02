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
 *      Description:    This class represents a "parallel statement" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------		----			-----------
 *      Bob Fisch       2010.11.26      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015.10.11      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015.10.16      Method clearExecutionStatus() duly overridden.
 *      Kay Gürtzig     2015.11.14      Bugfix #31 (= KGU#82) in method copy()
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (= KGU#91) in draw methods (--> getText(false))
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): New method equals(Element)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.Vector;
import java.awt.Color;
import java.awt.FontMetrics;

import javax.swing.JOptionPane;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;


public class Parallel extends Element
{
	
    public Vector<Subqueue> qs = new Vector<Subqueue>();

    private Rect r = new Rect();
    private int fullWidth = 0;
    private int maxHeight = 0;
	
    // START KGU#91 2015-12-01: Bugfix #39 - Parallel may NEVER EVER interchange text and comment!
	/**
	 * Returns the content of the text field Full stop. No swapping here!
	 * @return the text StringList
	 */
    @Override
	public StringList getText(boolean _ignored)
	{
		return getText();
	}

	/**
	 * Returns the content of the comment field. Full stop. No swapping here!
	 * @return the comment StringList
	 */
    @Override
	public StringList getComment(boolean _ignored)
	{
		return getComment();
	}
    // END KGU#91 2015-12-01

    @Override
    public void setText(String _text)
    {

// START KGU#91 2015-12-01: Bugfix #39, D.R.Y. - employ setText(StringList) for the rest
    	text.setText(_text);
    	this.setText(text);
            
//            Subqueue s = null;
//
//            getText().setText(_text);
//
//            if(qs==null)
//            {
//                    qs = new Vector();
//            }
//
//            // we need at least one line
//            if(getText().count()>0)
//            {
//                int count = 10;
//                try
//                {
//                    // retrieve the number of parallel tasks
//                    count = Integer.valueOf(getText().get(0).trim());
//                }
//                catch (java.lang.NumberFormatException e)
//                {
//                    JOptionPane.showMessageDialog(null, "Unknown number <"+getText().get(0).trim()+">.\nSetting number of tasks to 10!", "Error", JOptionPane.ERROR_MESSAGE);
//                    setText(new StringList());
//                    getText().add("10");
//                    count = 10;
//                }
//
//                // add subqueues
//                while(count>qs.size())
//                {
//                        s=new Subqueue();
//                        s.parent=this;
//                        qs.add(s);
//                }
//                // remove subqueues
//                while(count<qs.size())
//                {
//                        qs.removeElementAt(qs.size()-1);
//                }
//            }
// END KGU#91 2015-12-01            

    }

    @Override
    public void setText(StringList _textList)
    {
            Subqueue s = null;

            //setText(_textList);
            text=_textList;

            if(qs==null)
            {
                    qs = new Vector<Subqueue>();
            }

            // we need at least one line
            if(text.count()>0)
            {
                int count = 10;
                try
                {
                    // retrieve the number of parallel tasks
                    count = Integer.valueOf(text.get(0).trim());
                }
                catch (java.lang.NumberFormatException e)
                {
                    count = 10;
                    JOptionPane.showMessageDialog(null,
                    		"Unknown number <" + text.get(0).trim() +
                    		">.\nSetting number of tasks to " + count + "!",
                    		"Error", JOptionPane.ERROR_MESSAGE);
                    setText(new StringList());
                    text.add(Integer.toString(count));
                }

                    // add subqueues
                    while(count>qs.size())
                    {
                            s=new Subqueue();
                            s.parent=this;
                            qs.add(s);
                    }
                    // remove subqueues
                    while(count<qs.size())
                    {
                            qs.removeElementAt(qs.size()-1);
                    }
            }

    }

    public Parallel()
    {
            super();
    }

    public Parallel(String _string)
    {
            super(_string);
            //setText(_string);	// Already done by super
    }

    public Parallel(StringList _strings)
    {
            super(_strings);
            //setText(_strings);	// Already done by super
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
		if (this.qs != null)
		{
			for (int i = 0; i < this.qs.size(); i++)
			{
				this.qs.get(i).resetDrawingInfoDown();
			}
		}
	}
	// END KGU#64 2015-11-03
    
    public Rect prepareDraw(Canvas _canvas)
    {
            if(isCollapsed()) 
            {
                rect = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
                return rect;
            }


            rect.top = 0;
            rect.left = 0;

            FontMetrics fm = _canvas.getFontMetrics(font);

            rect.right  = 3 * (E_PADDING/2);
            rect.bottom = 4 * (E_PADDING/2);

            // retrieve the number of parallel tasks
            int tasks = Integer.valueOf(getText().get(0));

            Rect rtt = null;

            fullWidth=0;
            maxHeight=0;

            if (qs.size()!=0)
            {
                    for (int i=0;i<tasks;i++)
                    {
                       rtt = qs.get(i).prepareDraw(_canvas);
                       fullWidth += Math.max(rtt.right, getWidthOutVariables(_canvas, getText(false).get(i+1), this) + (E_PADDING / 2));
                       if (maxHeight < rtt.bottom)
                       {
                            maxHeight = rtt.bottom;
                       }
                    }
            }

            rect.right = Math.max(rect.right, fullWidth)+1;
            rect.bottom = rect.bottom+maxHeight;

            return rect;
    }

    public void draw(Canvas _canvas, Rect _top_left)
    {
            if(isCollapsed()) 
            {
                Instruction.draw(_canvas, _top_left, getCollapsedText(), this);
                return;
            }
                
            // retrieve the number of parallel tasks
            int tasks = Integer.valueOf(getText().get(0));

            Rect myrect = new Rect();
    		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
    		//Color drawColor = getColor();
    		Color drawColor = getFillColor();
    		// END KGU 2015-10-13
            FontMetrics fm = _canvas.getFontMetrics(Element.font);
//            int p;
//            int w;

    		// START KGU 2015-10-13: Became obsolete by new method getFillColor() applied above now
//    		if (selected==true)
//    		{
//    			if(waited==true) { drawColor=Element.E_WAITCOLOR; }
//    			else { drawColor=Element.E_DRAWCOLOR; }
//    		}
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
            //myrect.right-=1;
            canvas.fillRect(myrect);

            // draw shape
            myrect=_top_left.copy();
            myrect.bottom = _top_left.top + 2*fm.getHeight() + 4*(E_PADDING / 2);

            int y = myrect.top+E_PADDING;
            int a = myrect.left + (myrect.right-myrect.left) / 2;
            int b = myrect.top;
            int c = myrect.left+fullWidth-1;
            int d = myrect.bottom-1;
            int x = ((y-b)*(c-a) + a*(d-b)) / (d-b);

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
            Rect rtt = null;

            for(int i = 0; i<tasks;i++)
            {
                    rtt=((Subqueue) qs.get(i)).prepareDraw(_canvas);
                    lineWidth += Math.max(rtt.right, getWidthOutVariables(_canvas,getText(false).get(i+1),this) + (E_PADDING / 2));
            }

            // corners
            myrect = _top_left.copy();

            canvas.moveTo(myrect.left, myrect.bottom - 2*(E_PADDING/2));
            canvas.lineTo(myrect.left + 2*(E_PADDING/2), myrect.bottom);

            canvas.moveTo(myrect.left, myrect.top + 2*(E_PADDING/2));
            canvas.lineTo(myrect.left + 2*(E_PADDING/2), myrect.top);

            canvas.moveTo(myrect.right - 2*(E_PADDING/2), myrect.top);
            canvas.lineTo(myrect.right, myrect.top + 2*(E_PADDING/2));

            canvas.moveTo(myrect.right - 2*(E_PADDING/2), myrect.bottom);
            canvas.lineTo(myrect.right, myrect.bottom - 2*(E_PADDING/2));

            // horizontal lines
            canvas.moveTo(myrect.left, myrect.top + 2*(E_PADDING/2));
            canvas.lineTo(myrect.right, myrect.top + 2*(E_PADDING/2));

            canvas.moveTo(myrect.left, myrect.bottom - 2*(E_PADDING/2));
            canvas.lineTo(myrect.right, myrect.bottom - 2*(E_PADDING/2));

            // draw children
            myrect = _top_left.copy();
            myrect.top = _top_left.top + 2*(E_PADDING/2);
            myrect.bottom = _top_left.bottom - 2*(E_PADDING/2);

            if (qs.size() != 0)
            {

                    for(int i = 0; i <tasks; i++)
                    {
                            rtt = qs.get(i).prepareDraw(_canvas);

                            if (i==tasks-1)
                            {
                                    myrect.right = _top_left.right;
                            }
/*
                            else if((i!=qs.size()-1) || (!(this.parent.parent.getClass().getCanonicalName().equals("Root"))))
                            {
                                    myrect.right=myrect.left+Math.max(rtt.right,_canvas.stringWidth(text.get(i+1)+Math.round(E_PADDING / 2)));
                            }
*/
                            else
                            {
                                    myrect.right = myrect.left + Math.max(rtt.right,getWidthOutVariables(_canvas,getText(false).get(i+1),this)+Math.round(E_PADDING / 2))+1;
                            }

                            // draw child
                            qs.get(i).draw(_canvas,myrect);

                            // draw bottom up line
                            /*
                            if((i!=qs.size()-2)&&(i!=tasks))
                            {
                                    canvas.moveTo(myrect.right-1,myrect.top);
                                    int mx=myrect.right-1;
                                    int my=myrect.top-fm.getHeight();
                                    int sx=mx;
                                    int sy=Math.round((sx*(by-ay)-ax*by+ay*bx)/(bx-ax));
                                    canvas.lineTo(sx,sy+1);
                            }
                             *
                             */

                            myrect.left = myrect.right-1;

                    }
            }

            canvas.setColor(Color.BLACK);
            canvas.drawRect(_top_left);
    }

    // START KGU 2015-10-11: Merged with getElementByCoord, which had to be overridden as well for proper Comment popping
//    @Override
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

            for(int i = 0; i < qs.size(); i++)
            {
                    Element pre = qs.get(i).getElementByCoord(_x, _y, _forSelection);
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
    // END KGU 2015-10-11
    
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
            Parallel ele = new Parallel(this.getText().getText());
            //ele.setText(this.getText().copy());
            ele.setComment(this.getComment().copy());
            ele.setColor(this.getColor());
            ele.qs.clear();
            for(int i=0; i<qs.size(); i++)
            {
                    Subqueue sq = (Subqueue) ((Subqueue) this.qs.get(i)).copy();
                    sq.parent = ele;
                    ele.qs.add(sq);
            }
    		// START KGU#82 (bug #31) 2015-11-14
    		ele.breakpoint = this.breakpoint;
    		// END KGU#82 (bug #31) 2015-11-14

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
		boolean isEqual = super.equals(_another) && this.qs.size() == ((Parallel)_another).qs.size();
		for (int i = 0; isEqual && i < this.qs.size(); i++)
		{
			isEqual = this.qs.get(i).equals(((Parallel)_another).qs.get(i));
		}
		return isEqual;
	}
	// END KGU#119 2016-01-02

    // START KGU 2015-10-12
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.elements.Element#clearBreakpoints()
     */
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
    
    // START KGU 2015-10-16
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.elements.Element#clearExecutionStatus()
     */
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
    // END KGU 2015-10-16
    
	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
		// Under no circumstances, the text may contain an instruction or even variable declaration (it's just the number of threads) 
//		if (!_instructionsOnly)
//		{
//			_lines.add(this.getText());
//		}
    	if (qs!= null)
    	{
    		for (int i = 0; i < qs.size(); i++)
    		{
    			qs.get(i).addFullText(_lines, _instructionsOnly);
    		}
    	}		
    }
    // END KGU 2015-10-16
    
    
}

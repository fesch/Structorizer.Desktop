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

/*
 ******************************************************************************************************
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
 *      Kay Gürtzig     2016.01.03      Bugfix #87 (KGU#121): Correction in getElementByCoord(),
 *                                      method getCollapsedText() overridden for more clarity, getIcon()
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136): Translation-neutral selection;
 *                                      KGU#151: nonsense removed from prepareDraw() and draw().
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Method for test coverage tracking added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.01      Issue #145 (KGU#162): Comment is yet to be shown in switchText mode
 *      Kay Gürtzig     2016.04.05      Issue #145 solution improved and setText() stabilized
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.21      KGU#207: Slight performance improvement in getElementByCoord()
 *      Kay Gürtzig     2016.07.31      Enh. #128: New mode "comments plus text" supported, drawing code delegated
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.Vector;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.*;


public class Parallel extends Element
{
	
    public Vector<Subqueue> qs = new Vector<Subqueue>();

    private int fullWidth = 0;
    private int maxHeight = 0;
    // START KGU#136 2016-03-01: Bugfix #97 - cache the upper left corners of all branches
    private Vector<Integer> x0Branches = new Vector<Integer>();
    private int y0Branches = 0;
    // END KGU#136 2016-03-01
	
    // START KGU#91 2015-12-01: Bugfix #39 - Parallel may NEVER EVER interchange text and comment!
    // START KGU#227 2016-07-31: Enh. #128 - obsolete code, argument no longer ignored -> super
//	/**
//	 * Returns the content of the text field Full stop. No swapping here!
//	 * @return the text StringList
//	 */
//    @Override
//	public StringList getText(boolean _ignored)
//	{
//		return getText();
//	}
    // END KGU#227 2016-07-31

	/**
	 * Returns the content of the comment field unless _alwaysTrueComment is false and
	 * mode isSwitchedTextAndComment is active, in which case the colapsedText is
	 * returned. 
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
    @Override
	public StringList getComment(boolean _trueComment)
	{
    	// START KGU#172 2016-04.01: Issue #145
		//return getComment();
    	// START KGU#227 2016-07-31: Enh. #128
//		if (!_alwaysTrueComment && this.isSwitchTextCommentMode())
//		{
//			return this.getCollapsedText();
//		}
    	if (!_trueComment)
    	{
    		if (this.isCollapsed())
    		{
    			return StringList.getNew(this.getGenericText());
    		}
    		else if (!this.isSwitchTextCommentMode())
    		{
    			return this.getComment();
    		}
    		else
    		{
    			return new StringList();
    		}
    	}
		// END KGU#227 2016-07-31
		else
		{
			return this.getComment();
		}
		// END KGU#172 2016-04-01
	}
    // END KGU#91 2015-12-01
    
    // START KGU#227 2016-07-31: Enh. #128 new helper method
    private String getGenericText()
    {
    	return getClass().getSimpleName() + "(" + this.text.get(0) + ")";
    }
    // END KGU#227 2016-07-31
    
    // START KGU#122 2016-01-04: Add the Class name (or some localized text?) to the number of threads
    @Override
	public StringList getCollapsedText()
	{
		// START KGU#227 2016-07-30: Enh. #128 - This is getting rickier now - must must distinguish modes
		//StringList sl = super.getCollapsedText();
		//sl.set(0, getClass().getSimpleName() + "(" + sl.get(0) + ")");
		StringList sl = StringList.getNew(this.getGenericText());
		sl.add(COLLAPSED);
    	if (this.isSwitchTextCommentMode() && !this.getComment().getText().trim().isEmpty())
    	{
    		sl.set(0, this.getGenericText() + " - " + this.getComment().get(0));
    	}
		// END KGU#227 2016-07-30
		return sl;
	}
    // END KGU#122 2016-01-04

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
    			text = StringList.getNew(Integer.toString(count));
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

//	// START KGU#64 2015-11-03: Is to improve drawing performance
//	/**
//	 * Recursively clears all drawing info this subtree down
//	 * (To be overridden by structured sub-classes!)
//	 */
//	@Override
//	public void resetDrawingInfoDown()
//	{
//		this.resetDrawingInfo();
//		if (this.qs != null)
//		{
//			for (int i = 0; i < this.qs.size(); i++)
//			{
//				this.qs.get(i).resetDrawingInfoDown();
//			}
//		}
//	}
//	// END KGU#64 2015-11-03
    
	// START KGU#227 2016-07-30: Enh. #128
	/**
	 * Provides a subclassable left offset for drawing the text
	 */
	protected int getTextDrawingOffset()
	{
		return this.isCollapsed() ? 0 : (Element.E_PADDING/2);
	}
	// END KGU#227 2016-07-30

   public Rect prepareDraw(Canvas _canvas)
    {
            // START KGU#136 2016-03-01: Bugfix #97
            if (this.isRectUpToDate) return rect0;
            
            this.x0Branches.clear();
            this.y0Branches = 0;
            // END KGU#136 2016-03-01

            // KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
            if (isCollapsed()) 
            {
                rect0 = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
        		// START KGU#136 2016-03-01: Bugfix #97
        		isRectUpToDate = true;
        		// END KGU#136 2016-03-01
                return rect0;
            }


            rect0.top = 0;
            rect0.left = 0;

            rect0.right  = 3 * (E_PADDING/2);		// Minimum total width	(without comments, without thread area)
            rect0.bottom = 4 * (E_PADDING/2);		// Minimum total height (without thread area)
            // START KGU#136 2016-03-01: Bugfix #97
            this.y0Branches = 2 * (E_PADDING/2);	// Y coordinate below which the branches are drawn
            // END KGU#136 2016-03-01

            // START KGU#227 2016-07-30: Issues #128, #145: New mode "comments plus text" required modification
//            // START KGU#172 2016-04-01: Issue #145 Show comment in switch text/comment mode
//            if (this.isSwitchTextCommentMode() && !this.comment.getText().trim().isEmpty())
//            {
//                FontMetrics fm = _canvas.getFontMetrics(Element.font);
//            	for (int ci = 0; ci < this.comment.count(); ci++)
//            	{
//            		rect0.right = Math.max(rect0.right, getWidthOutVariables(_canvas, this.comment.get(ci), this) + 2 * E_PADDING);
//            	}
//            	int extraHeight = this.comment.count() * fm.getHeight();
//            	rect0.bottom += extraHeight;
//            	this.y0Branches += extraHeight;
//            }
//            // END KGU#172 2016-04-01
            
            // Unless some of the comment modes requires this, the upper stripe remains empty
            if ((Element.E_COMMENTSPLUSTEXT || this.isSwitchTextCommentMode()) && !this.comment.getText().trim().isEmpty())
            {
            	// In mode"comments plus text" there is no actual text, the comment is to be inserted in lower font
            	// Otherwise ("switch text/comments") the comment will be added as text in normal font.
                StringList headerText = new StringList();	// No text in general
                if (!Element.E_COMMENTSPLUSTEXT)
                {
                	headerText = this.getComment();			// It must be "switch text/comments" mode.
                }
               Rect textRect = Instruction.prepareDraw(_canvas, headerText, this);
               rect0.right = Math.max(rect0.right, textRect.right + 2 * (E_PADDING/2));
               rect0.bottom = Math.max(rect0.bottom, textRect.bottom + 2*(E_PADDING/2));
               this.y0Branches = Math.max(this.y0Branches, textRect.bottom);
            }
            // END KGU#227 2016-07-30
            
            // retrieve the number of parallel tasks
            int nTasks = Integer.valueOf(getText().get(0));

            fullWidth = 0;
            maxHeight = 0;

            if (qs.size() != 0)
            {
            	for (int i = 0; i < nTasks; i++)
            	{
            		// START KGU#136 2016-03-01: Bugfix #97
            		x0Branches.addElement(fullWidth);
            		// END KGU#136 2016-03-01
            		Rect rtt = qs.get(i).prepareDraw(_canvas);
                	// START KGU#151 2016-03-01: Additional text lines should not influence the thread width!
            		//fullWidth += Math.max(rtt.right, getWidthOutVariables(_canvas, getText(false).get(i+1), this) + (E_PADDING / 2));
            		fullWidth += Math.max(rtt.right, E_PADDING / 2);
            		// END KGU#151 2016-03-01
            		if (maxHeight < rtt.bottom)
            		{
            			maxHeight = rtt.bottom;
            		}
            	}
            }

            rect0.right = Math.max(rect0.right, fullWidth)+1;
            rect0.bottom = rect0.bottom + maxHeight;

    		// START KGU#136 2016-03-01: Bugfix #97
    		isRectUpToDate = true;
    		// END KGU#136 2016-03-01
            return rect0;
    }

    public void draw(Canvas _canvas, Rect _top_left)
    {
            if(isCollapsed()) 
            {
                Instruction.draw(_canvas, _top_left, getCollapsedText(), this);
                return;
            }
                
            // retrieve the number of parallel tasks
            int nTasks = Integer.valueOf(getText().get(0));

            // START KGU#227 2016-07-30: Enh. #128 - delegate as much as possible to Instruction
//            Rect myrect = new Rect();
//    		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
//    		//Color drawColor = getColor();
//    		Color drawColor = getFillColor();
//    		// END KGU 2015-10-13
//            FontMetrics fm = _canvas.getFontMetrics(Element.font);
//
//            Canvas canvas = _canvas;
//            canvas.setBackground(drawColor);
//            canvas.setColor(drawColor);
//
//    		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
//    		//rect = _top_left.copy();
//    		rect = new Rect(0, 0, 
//    				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
//    		Point ref = this.getDrawPoint();
//    		this.topLeft.x = _top_left.left - ref.x;
//    		this.topLeft.y = _top_left.top - ref.y;
//    		// END KGU#136 2016-03-01
//    		
//            // fill shape
//            canvas.setColor(drawColor);
//            myrect = _top_left.copy();
//            myrect.left += 1;
//            myrect.top += 1;
//            //myrect.right -= 1;
//            canvas.fillRect(myrect);
            StringList headerText = new StringList();
            if (!Element.E_COMMENTSPLUSTEXT && this.isSwitchTextCommentMode())
            {
            	headerText = this.getComment();
            }
            Instruction.draw(_canvas, _top_left, headerText, this);
            // END KGU227 2016-07-30
            
            // draw shape
            Rect myrect = _top_left.copy();
            // START KGU#227 2016-07-30: Enh. #128 - All delegated to Instruction.draw(...) above
//            myrect.bottom = _top_left.top + 2*fm.getHeight() + 4*(E_PADDING / 2);
//            
//            // draw comment
//            // START KGU#172 2016-04-01: Issue #145
//            //if (Element.E_SHOWCOMMENTS==true && !comment.getText().trim().isEmpty())
//            if (Element.E_SHOWCOMMENTS==true && !getComment(false).getText().trim().isEmpty())
//            // END KGU#172 2016-04-01
//            {
//    			this.drawCommentMark(canvas, _top_left);
//    		}
//            // START KGU 2015-10-11
//    		// draw breakpoint bar if necessary
//    		this.drawBreakpointMark(canvas, myrect);
//    		// END KGU 2015-10-11
//
//
//            // draw lines
//            canvas.setColor(Color.BLACK);
//            
//            // corners
//            myrect = _top_left.copy();
//            
//            // START KGU#172 2016-04-01: Issue #145 - we shall show the comment in switch mode
//            int headerHeight = 2*(E_PADDING/2);
//            int footerHeight = 2*(E_PADDING/2);
//            if (this.isSwitchTextCommentMode() && !this.comment.getText().trim().isEmpty())
//            {
//            	headerHeight += this.comment.count() * fm.getHeight();
//            }
//            // END KGU#172 2016-04-01
            int headerHeight = this.y0Branches;
            int footerHeight = 2*(E_PADDING/2);
            // END KGU#227 2016-07-30
            
            _canvas.moveTo(myrect.left, myrect.bottom - 2*(E_PADDING/2));
            _canvas.lineTo(myrect.left + 2*(E_PADDING/2), myrect.bottom);

            // START KGU#172 2016-04-01: Bugfix #145
            //canvas.moveTo(myrect.left, myrect.top + 2*(E_PADDING/2));
            _canvas.moveTo(myrect.left, myrect.top + headerHeight);
            // END KGU#172 2016-04-01
            _canvas.lineTo(myrect.left + 2*(E_PADDING/2), myrect.top);

            _canvas.moveTo(myrect.right - 2*(E_PADDING/2), myrect.top);
            // START KGU#172 2016-04-01: Bugfix #145
            //canvas.lineTo(myrect.right, myrect.top + 2*(E_PADDING/2));
            _canvas.lineTo(myrect.right, myrect.top + headerHeight);
            // END KGU#172 2016-04-01

            _canvas.moveTo(myrect.right - 2*(E_PADDING/2), myrect.bottom);
            // START KGU#172 2016-04-01: Bugfix #145
            //canvas.lineTo(myrect.right, myrect.bottom - 2*(E_PADDING/2));
            _canvas.lineTo(myrect.right, myrect.bottom - footerHeight);
            // END KGU#172 2016-04-01

            // horizontal lines
            // START KGU#172 2016-04-01: Bugfix #145
            //canvas.moveTo(myrect.left, myrect.top + 2*(E_PADDING/2));
            //canvas.lineTo(myrect.right, myrect.top + 2*(E_PADDING/2));
            _canvas.moveTo(myrect.left, myrect.top + headerHeight);
            _canvas.lineTo(myrect.right, myrect.top + headerHeight);
            // END KGU#172 2016-04-01

            //canvas.lineTo(myrect.right, myrect.bottom - 2*(E_PADDING/2));
            //canvas.moveTo(myrect.left, myrect.bottom - 2*(E_PADDING/2));
            //canvas.lineTo(myrect.right, myrect.bottom - 2*(E_PADDING/2));
            _canvas.moveTo(myrect.left, myrect.bottom - footerHeight);
            _canvas.lineTo(myrect.right, myrect.bottom - footerHeight);
            // END KGU#172 2016-04-01
            
            // START KGU#227 2016-07-30: Enh. #128 - Obsolete code
//            // START KGU#172 2016-04.01: Issue #145
//            // draw the comment in switch Text / Comment mode
//            if (this.isSwitchTextCommentMode())
//            {
//            	for (int ci = 0; ci < this.comment.count(); ci++)
//            	{
//            		writeOutVariables(_canvas, myrect.left + 2*(E_PADDING/2), 
//            				myrect.top + E_PADDING/2 + (ci + 1) * fm.getHeight(),
//            				this.comment.get(ci), this);
//            	}
//            }
//            // END KGU#172 2016-04-01
//
//    		// START KGU#156 2016-03-11: Enh. #124
//    		// write the run-time info if enabled
//    		this.writeOutRuntimeInfo(canvas, myrect.right - (Element.E_PADDING * 2), myrect.top);
//    		// END KGU#156 2016-03-11				
            // END KGU#227 2016-07-30
            
            // draw children
            myrect = _top_left.copy();
            // START KGU#172 2016-04-01: Issue #145 - consider the possible comment area
            //myrect.top = _top_left.top + 2*(E_PADDING/2);
            //myrect.bottom = _top_left.bottom - 2*(E_PADDING/2);
            myrect.top = _top_left.top + headerHeight;
            myrect.bottom = _top_left.bottom - footerHeight;
            // END KGU#172 2016-04-01
            
            if (qs.size() != 0)
            {

                    for (int i = 0; i < nTasks; i++)
                    {
                            Rect rtt = qs.get(i).prepareDraw(_canvas);

                            if (i == nTasks-1)
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
                            	// START KGU#151 2016-03-01: Additional text lines should not influence the thread width!
                                //myrect.right = myrect.left + Math.max(rtt.right,getWidthOutVariables(_canvas,getText(false).get(i+1),this) + (E_PADDING / 2)) + 1;
                                myrect.right = myrect.left + Math.max(rtt.right, E_PADDING / 2) + 1;
                            	// END KGU#151 2016-03-01
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

            _canvas.setColor(Color.BLACK);
            _canvas.drawRect(_top_left);
    }

	// START KGU 2016-07-30: Adapt the runtime info position
	/**
	 * Writes the selected runtime information in half-size font to the lower
	 * left of position (_right, _top).
	 * @param _canvas - the Canvas to write to
	 * @param _right - right border x coordinate
	 * @param _top - upper border y coordinate
	 */
	protected void writeOutRuntimeInfo(Canvas _canvas, int _right, int _top)
	{
		super.writeOutRuntimeInfo(_canvas, _right - (Element.E_PADDING/2), _top);
	}
	// END KGU 2016-07-30

	// START KGU#122 2016-01-03: Collapsed elements may be marked with an element-specific icon
    @Override
    protected ImageIcon getIcon()
    {
    	return IconLoader.ico091;
    }
    // END KGU#122 2016-01-03

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
    		// START KGU#121 2016-01-03: A collapsed element has no visible substructure!
        	// START KGU#207 2016-07-21: If this element isn't hit then there is no use searching the substructure
    		//if (!this.isCollapsed())
    		if (!this.isCollapsed() && (selMe != null || _forSelection))
    		// START KGU#207 2016-07-21
    		{
    		// END KGU#121 2016-01-03
    			Element selCh = null;
    			Element pre = null;
    			for(int i = 0; i < qs.size(); i++)
    			{
    				// START KGU#136 2016-03-01: Bugfix #97
    				//Element pre = qs.get(i).getElementByCoord(_x, _y, _forSelection);
    				if (i < x0Branches.size()) {
    					int xOff = x0Branches.get(i);
        				pre = qs.get(i).getElementByCoord(_x - xOff, _y - y0Branches, _forSelection);
    				}
    				// END KGU#136 2016-03-01
    				if (pre != null)
    				{
    					selCh = pre;
    				}
    			}

    			if (selCh != null)
    			{
    				if (_forSelection) selected = false;
    				selMe = selCh;
    			}
    		// START KGU#121 2016-01-03: Bugfix #87 (continued)
    		}
    		// END KGU#121 2016-01-03

            return selMe;
    }
    // END KGU 2015-10-11
    
//    public void setSelected(boolean _sel)
//    {
//            selected=_sel;
//            /* Quatsch !
//            for(int i = 0;i<qs.size();i++)
//            {
//                    ((Subqueue) qs.get(i)).setSelected(_sel);
//            }
//            */
//    }

	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	public Element findSelected()
	{
		Element sel = selected ? this : null;
		for (int i = 0; sel == null && i < this.qs.size(); i++)
		{
			sel = qs.elementAt(i).findSelected();
		}
		return sel;
	}
	// END KGU#183 2016-04-24
	    
    public Element copy() // Problem here???
    {
            Parallel ele = new Parallel(this.getText().getText());
            copyDetails(ele, false);
            ele.qs.clear();
            for(int i=0; i<qs.size(); i++)
            {
                    Subqueue sq = (Subqueue) ((Subqueue) this.qs.get(i)).copy();
                    sq.parent = ele;
                    ele.qs.add(sq);
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
		boolean isEqual = super.equals(_another) && this.qs.size() == ((Parallel)_another).qs.size();
		for (int i = 0; isEqual && i < this.qs.size(); i++)
		{
			isEqual = this.qs.get(i).equals(((Parallel)_another).qs.get(i));
		}
		return isEqual;
	}
	// END KGU#119 2016-01-02

	// START KGU#117 2016-03-07: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#combineCoverage(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	public boolean combineRuntimeData(Element _cloneOfMine)
	{
		boolean isEqual = super.combineRuntimeData(_cloneOfMine);
		for (int i = 0; isEqual && i < this.qs.size(); i++)
		{
			isEqual = this.qs.get(i).combineRuntimeData(((Parallel)_cloneOfMine).qs.get(i));
		}
		return isEqual;
	}
	// END KGU#117 2016-03-07

//    // START KGU 2015-10-12
//    /* (non-Javadoc)
//     * @see lu.fisch.structorizer.elements.Element#clearBreakpoints()
//     */
//    @Override
//    public void clearBreakpoints()
//    {
//    	super.clearBreakpoints();
//    	if (qs!= null)
//    	{
//    		for (int i = 0; i < qs.size(); i++)
//    		{
//    			qs.get(i).clearBreakpoints();
//    		}
//    	}
//    }
//    // END KGU 2015-10-12
//    
//    // START KGU 2015-10-16
//    /* (non-Javadoc)
//     * @see lu.fisch.structorizer.elements.Element#clearExecutionStatus()
//     */
//    @Override
//    public void clearExecutionStatus()
//    {
//    	super.clearExecutionStatus();
//    	if (qs!= null)
//    	{
//    		for (int i = 0; i < qs.size(); i++)
//    		{
//    			qs.get(i).clearExecutionStatus();
//    		}
//    	}
//    }
//    // END KGU 2015-10-16
//    
//	// START KGU#117 2016-03-06: Enh. #77
//    /* (non-Javadoc)
//     * @see lu.fisch.structorizer.elements.Element#clearExecutionStatus()
//     */
//    @Override
//    public void clearRuntimeData()
//    {
//    	super.clearRuntimeData();
//    	if (qs!= null)
//    	{
//    		for (int i = 0; i < qs.size(); i++)
//    		{
//    			qs.get(i).clearRuntimeData();
//    		}
//    	}
//    }

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isTestCovered(boolean)
	 */
	public boolean isTestCovered(boolean _deeply)
	{
		boolean covered = true;
    	if (qs!= null)
    	{
    		for (int i = 0; covered && i < qs.size(); i++)
    		{
    			covered = qs.get(i).isTestCovered(_deeply);
    		}
    	}		
		return covered;
	}
	// END KGU#117 2016-03-06

	// START KGU#156 2016-03-13: Enh. #124
	protected String getRuntimeInfoString()
	{
		String info = this.getExecCount() + " / ";
		String stepInfo = null;
		switch (E_RUNTIMEDATAPRESENTMODE)
		{
		case TOTALSTEPS_LIN:
		case TOTALSTEPS_LOG:
			stepInfo = Integer.toString(this.getExecStepCount(true));
			if (!this.isCollapsed()) {
				stepInfo = "(" + stepInfo + ")";
			}
			break;
		default:
			stepInfo = Integer.toString(this.getExecStepCount(this.isCollapsed()));
		}
		return info + stepInfo;
	}
	// END KGU#156 2016-03-11

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
    
	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 */
	@Override
	public void convertToCalls(StringList _signatures) {
    	if (qs!= null)
    	{
    		for (int i = 0; i < qs.size(); i++)
    		{
    			qs.get(i).convertToCalls(_signatures);
    		}
    	}
	}
	// END KGU#199 2016-07-07
    
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#traverse(lu.fisch.structorizer.elements.IElementVisitor)
	 */
	@Override
	public boolean traverse(IElementVisitor _visitor) {
		boolean proceed = _visitor.visitPreOrder(this);
		if (qs != null) {
			for (int i = 0; proceed && i < qs.size(); i++)
			{
				proceed = qs.get(i).traverse(_visitor);
			}
		}
		if (proceed)
		{
			proceed = _visitor.visitPostOrder(this);
		}
		return proceed;
	}

	@Override
	protected String[] getRelevantParserKeys() {
		// There is nothing to return
		return null;
	}

}

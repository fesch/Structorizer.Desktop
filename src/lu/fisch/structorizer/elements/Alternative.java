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
 *      Description:    This class represents an "IF statement" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.10      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015.10.11      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015.11.14      Bugfix #31 (= KGU#82) in method copy
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (= KGU#91) in drawing methods
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016.01.03      Bugfix #87 (KGU#121): Correction in getElementByCoord()
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136): Translation-neutral selection
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Methods for test coverage tracking added
 *      Kay Gürtzig     2016.03.07      Bugfix #122 (KGU#136): Selection was not aware of option altPadRight 
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Stack;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.*;

public class Alternative extends Element {

	public Subqueue qFalse = new Subqueue();
	public Subqueue qTrue = new Subqueue();
	
	private Rect rTrue = new Rect();
	private Rect rFalse = new Rect();
	
	// START KGU#136 2016-03-07: Bugfix #97
	private Point pt0Parting = new Point();
	// END KGU#136 2016-03-07

	public Alternative()
	{
		super();
		qFalse.parent=this;
		qTrue.parent=this;
	}
	
	public Alternative(String _strings)
	{
		super(_strings);
		qFalse.parent=this;
		qTrue.parent=this;
		setText(_strings);
	}
	
	public Alternative(StringList _strings)
	{
		super(_strings);
		qFalse.parent=this;
		qTrue.parent=this;
		setText(_strings);
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
		qFalse.resetDrawingInfoDown();
		qTrue.resetDrawingInfoDown();
	}
	// END KGU#64 2015-11-03

	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		// END KGU#136 2016-03-01
		//  KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		if(isCollapsed()) 
		{
			rect0 = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
			// START KGU#136 2016-03-01: Bugfix #97
			isRectUpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}

		int nLines = getText(false).count();
		rect0.top = 0;
		rect0.left = 0;

		rect0.right = 2 * (E_PADDING/2);

		FontMetrics fm = _canvas.getFontMetrics(Element.font);

		rect0.right = 2 * E_PADDING;

		// prepare the sub-queues
		rFalse = qFalse.prepareDraw(_canvas);
		rTrue = qTrue.prepareDraw(_canvas);

		// the upper left point of the corner
		double cx = 0;
		double cy = nLines*fm.getHeight() + 4*(E_PADDING/2);
		// the lowest point of the triangle
		double ax =  rTrue.right - rTrue.left;
		//System.out.println("AX : "+ax);
		double ay =  0;
		// coefficient of the left droite
		double coeffleft = (cy-ay)/(cx-ax);


		// init
		//int choice = -1;
		double lowest = 100000;	// dummy bound

		for (int i = 0; i < nLines; i++)
		{

			/* old code
                        if(rect.right<_canvas.stringWidth((String) text.get(i))+4*Math.round(E_PADDING))
			{
				rect.right=_canvas.stringWidth((String) text.get(i))+4*Math.round(E_PADDING);
			}
			 */

			// bottom line of the text
			double by = 4*(E_PADDING/2) - (E_PADDING/3) + (nLines-i-1) * fm.getHeight();
			// part on the left side
			double leftside = by/coeffleft + ax - ay/coeffleft;
			// the bottom right point of this text line
			int textWidth = getWidthOutVariables(_canvas, getText(false).get(i), this);
			double bx = textWidth + 2*(E_PADDING/2) + leftside;
			//System.out.println("LS : "+leftside);

			// check if this is the one we need to do calculations
			double coeff = (by-ay)/(bx-ax);

			// the point height we need
			double y = nLines * fm.getHeight() + 4*(E_PADDING/2);
			double x = y/coeff + ax - ay/coeff;
			//System.out.println(i+" => "+coeff+" --> "+String.valueOf(x));

			if (coeff<lowest && coeff>0)
			{
				// remember it
				lowest = coeff;
				//choice = i;
			}
		}
		if (lowest!=100000)
		{
			// the point height we need
			double y = nLines * fm.getHeight() + 4*(E_PADDING/2);
			double x = y/lowest + ax - ay/lowest;
			rect0.right = (int) Math.round(x);
			//System.out.println("C => "+lowest+" ---> "+rect.right);
		}
		else
		{
			rect0.right = 4*(E_PADDING/2);
		}

		rect0.bottom = 4*(E_PADDING/2) + nLines*fm.getHeight();
		pt0Parting.y = rect0.bottom;
		
		rect0.right = Math.max(rect0.right, rTrue.right + rFalse.right);
		rect0.bottom += Math.max(rTrue.bottom, rFalse.bottom);
		pt0Parting.x = rTrue.right;

		// START KGU#136 2016-03-01: Bugfix #97
		isRectUpToDate = true;
		// END KGU#136 2016-03-01

		return rect0;
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{
		//System.out.println("ALT("+this.getText().getLongString()+") draw at ("+_top_left.left+", "+_top_left.top+")");
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
		int a;
		int b;
		int c;
		int d;
		int x;
		int y;
		int wmax;
		int p;
		int w;

		// START KGU 2015-10-13: Already done by new method getFillColor() now
//		if (selected==true)
//		{
//                if(waited==true) { drawColor=Element.E_WAITCOLOR; }
//                else { drawColor=Element.E_DRAWCOLOR; }
//		}
		// END KGU 2015-10-13
	
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		int nLines = getText(false).count();
		myrect = _top_left.copy();
		myrect.bottom -= 1;
		canvas.fillRect(myrect);

		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		Point ref = this.getDrawPoint();
		this.topLeft.x = _top_left.left - ref.x;
		this.topLeft.y = _top_left.top - ref.y;
		// END KGU#136 2016-03-01
		
		myrect.bottom = _top_left.top + nLines*fm.getHeight() + 4*(E_PADDING/2);
		y = myrect.top + E_PADDING;
		a = myrect.left + ((myrect.right-myrect.left) / 2);
		b = myrect.top;
		c = myrect.left + rTrue.right - 1;
		d = myrect.bottom - 1;
		x = Math.round(((y-b)*(c-a) + a*(d-b))/(d-b));
/*
		wmax=0;
		for(int i=0;i<text.count();i++)
		{
			if (wmax<_canvas.stringWidth(text.get(i)))
			{
				wmax = _canvas.stringWidth(text.get(i));
			}
		}
*/
                int remain = (_top_left.right - _top_left.left)
                             -(rTrue.right - rTrue.left)
                             -(rFalse.right - rFalse.left);
                if (Element.altPadRight == false) remain=0;
                // START KGU#136 2016-03-07: Bugfix #122 - we must correct the else start point
                this.pt0Parting.x = this.rTrue.right - rTrue.left + remain; 
                // END KGU#136 2016-03-07
                
                // the upper left point of the corner
                double cx = 0;
                double cy = nLines*fm.getHeight() + 4*(E_PADDING/2);
                // upper right corner
                double dx = _top_left.right - _top_left.left;
                double dy = cy;
                // the the lowest point of the triangle
                double ax = rTrue.right - rTrue.left + remain;
                double ay = 0;
                // coefficient of the left droite
                double coeffleft = (cy-ay)/(cx-ax);
                double coeffright = (dy-ay)/(dx-ax);

                // draw text
		for (int i=0; i < nLines; i++)
		{
			String mytext = this.getText(false).get(i);

                        // bottom line of the text
                        double by = 4*(E_PADDING/2) - (E_PADDING/3) + (nLines-i-1)*fm.getHeight();
                        // part on the left side
                        double leftside = by/coeffleft + ax - ay/coeffleft;
                        // the the bottom right point of this text line
                        double bx = by/coeffright + ax - ay/coeffright;
                        /* dbugging output
                        canvas.setColor(Color.RED);
                        canvas.fillRect(new Rect(
                                myrect.left+(int) cx-2, myrect.bottom-(int) cy-2,
                                myrect.left+(int) cx+2, myrect.bottom-(int) cy+2)
                        );
                        canvas.moveTo(myrect.left+(int) leftside, myrect.bottom-(int) by);
                        canvas.lineTo(myrect.left+(int) bx, myrect.bottom-(int) by);
                        */
                        int boxWidth = (int) (bx-leftside);
                        int textWidth = getWidthOutVariables(_canvas,getText(false).get(i),this);

                        canvas.setColor(Color.BLACK);
                        writeOutVariables(canvas,
                            _top_left.left + (E_PADDING/2) + (int) leftside + (int) (boxWidth - textWidth)/2,
                            _top_left.top + (E_PADDING / 3) + (i+1)*fm.getHeight(),
                            mytext,this
                        );

                        /*
			if(rotated==false)
			{
				canvas.setColor(Color.BLACK);
				writeOutVariables(canvas,
								  x-Math.round(_canvas.stringWidth(text)/2),
								_top_left.top+Math.round(E_PADDING / 3)+(i+1)*fm.getHeight(),
								text
								);  	
			}
			else
			{
				// draw rotated
				
				// coloredTextOut(text[i],vars,colors,_canvas,_top_left.Left+(E_PADDING div 2)+i*_canvas.TextHeight(text[i]),
				// _top_left.bottom-(E_PADDING div 2),rotated);
				
			}
                         */
		}
		
		// draw symbols
		canvas.writeOut(myrect.left + (E_PADDING / 2),
						myrect.bottom - (E_PADDING / 2), preAltT);
		canvas.writeOut(myrect.right - (E_PADDING / 2) -_canvas.stringWidth(preAltF),
						myrect.bottom - (E_PADDING / 2), preAltF);
		
		
		
		// draw comment
		if (Element.E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
		{
			this.drawCommentMark(canvas, myrect);
		}
		// START KGU 2015-10-11
		// draw breakpoint bar if necessary
		this.drawBreakpointMark(canvas, myrect);
		// END KGU 2015-10-11
		
                // draw triangle
		canvas.setColor(Color.BLACK);
		canvas.moveTo(myrect.left, myrect.top);
		canvas.lineTo(myrect.left + rTrue.right-1 + remain, myrect.bottom-1);
		canvas.lineTo(myrect.right, myrect.top);
		
		// draw children
		myrect = _top_left.copy();
                
		myrect.top = _top_left.top + fm.getHeight()*nLines + 4*(E_PADDING / 2)-1;
		myrect.right = myrect.left + rTrue.right-1 + remain;
		
		qTrue.draw(_canvas,myrect);
		
		myrect.left = myrect.right;
		myrect.right = _top_left.right;
		qFalse.draw(_canvas,myrect);
		
		
		myrect = _top_left.copy();
		canvas.setColor(Color.BLACK);
		canvas.drawRect(myrect);
	}
	
	// START KGU#122 2016-01-03: Collapsed elements may be marked with an element-specific icon
	@Override
	protected ImageIcon getIcon()
	{
		return IconLoader.ico060;
	}
	// END KGU#122 2016-01-03
	
	// START KGU 2015-10-09: On moving the cursor, substructures had been eclipsed
	// by their containing box w.r.t. comment popping etc. This correction, however,
	// might significantly slow down the mouse tracking on enabled comment popping.
    // Just give it a try... 
	//public Element selectElementByCoord(int _x, int _y)
	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x,_y, _forSelection);
		// START KGU#121 2016-01-03: Bugfix #87 - A collapsed element has no visible substructure!
		if (!this.isCollapsed())
		{
		// END KGU#121 2016-01-03
			// START KGU#136 2016-03-01: Bugfix #97 - we use local coordinates now
			//Element selT = qTrue.getElementByCoord(_x,_y, _forSelection);
			//Element selF = qFalse.getElementByCoord(_x,_y, _forSelection);
			Element selT = qTrue.getElementByCoord(_x, _y-pt0Parting.y, _forSelection);
			Element selF = qFalse.getElementByCoord(_x-pt0Parting.x, _y-pt0Parting.y, _forSelection);
			// END KGU#136 2016-03-01
			if (selT != null) 
			{
				//selected=false;
				if (_forSelection) selected = false;
				selMe = selT;
			}
			else if (selF != null)
			{
				//selected=false
				if (_forSelection) selected = false;
				selMe = selF;
			}
		// START KGU#121 2016-01-03: Bugfix #87 (continued)
		}
		// END KGU#121 2016-01-03

		return selMe;
	}
	// END KGU 2015.10.09
	
	public Element copy()
	{
		Alternative ele = new Alternative(this.getText().copy());
		ele.setComment(this.getComment().copy());
		ele.setColor(this.getColor());
		ele.qTrue=(Subqueue) this.qTrue.copy();
		ele.qFalse=(Subqueue) this.qFalse.copy();
		ele.qTrue.parent=ele;
		ele.qFalse.parent=ele;
		// START KGU#82 (bug #31) 2015-11-14
		ele.breakpoint = this.breakpoint;
		// END KGU#82 (bug #31) 2015-11-14
		// START KGU#117 2016-03-07: Enh. #77
		ele.tested = Element.E_TESTCOVERAGEMODE && this.tested;
		// END KGU#117 2016-03-07
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
		boolean isEqual = super.equals(_another);
		if (isEqual)
		{
			isEqual = this.qTrue.equals(((Alternative)_another).qTrue) &&
					this.qFalse.equals(((Alternative)_another).qFalse);
		}
		return isEqual;
	}
	// END KGU#119 2016-01-02

	// START KGU#117 2016-03-07: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#combineCoverage(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	public boolean combineCoverage(Element _another)
	{
		boolean isEqual = super.combineCoverage(_another);
		if (isEqual)
		{
			isEqual = this.qTrue.combineCoverage(((Alternative)_another).qTrue) &&
					this.qFalse.combineCoverage(((Alternative)_another).qFalse);			
		}
		return isEqual;
	}
	// END KGU#117 2016-03-07

	/*@Override
    public void setColor(Color _color) 
    {
        super.setColor(_color);
        qFalse.setColor(_color);
        qTrue.setColor(_color);
    }*/
	
	
	// START KGU 2015-11-12
	@Override
	public void clearBreakpoints()
	{
		super.clearBreakpoints();
		this.qFalse.clearBreakpoints();
		this.qTrue.clearBreakpoints();
	}
	// END KGU 2015-10-12
	
	// START KGU 2015-10-13
	// Recursively clears all execution flags in this branch
	public void clearExecutionStatus()
	{
		super.clearExecutionStatus();
		this.qFalse.clearExecutionStatus();
		this.qTrue.clearExecutionStatus();
	}
	// END KGU 2015-10-13

	// START KGU#117 2016-03-07: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#clearTestCoverage()
	 */
	public void clearTestCoverage()
	{
		super.clearTestCoverage();
		this.qFalse.clearTestCoverage();
		this.qTrue.clearTestCoverage();
	}
	// END KGU#117 2016-03-07

	// START KGU#117 2016-03-06: Enh. #77
	public boolean isTestCovered()
	{
		return this.qTrue.isTestCovered() && this.qFalse.isTestCovered();
	}
	// END KGU#117 2016-03-06

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
    	if (!_instructionsOnly) {
    		_lines.add(this.getText());	// Text of the condition
    	}
    	this.qTrue.addFullText(_lines, _instructionsOnly);
    	this.qFalse.addFullText(_lines, _instructionsOnly);
    }
    // END KGU 2015-10-16
	
	
}

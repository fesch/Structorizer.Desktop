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
 *      Description:    This class represents an "FOR loop" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.07      First Issue
 *      Bob Fisch       2008.02.06      Modified for DIN / not DIN
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015.10.12      Comment drawing centralized and breakpoint mechanism prepared.
 *      Kay Gürtzig     2015.11.04      New mechanism to split and compose the FOR clause into/from dedicated fields
 *      Kay Gürtzig     2015.11.14      Bugfixes (#28 = KGU#80 and #31 = KGU#82) in Method copy
 *      Kay Gürtzig     2015.11.30      Inheritance changed: implements ILoop
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (=KGU#91) -> getText(false), prepareDraw() optimised
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016.01.03      Bugfix #87 (KGU#121): Correction in getElementByCoord(), getIcon()
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136): Translation-neutral selection
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Fields for test coverage tracking added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.03.20      Enh. #84/#135 (KGU#61): enum type and methods introduced/modified
 *                                      to distinguish and handle FOR-IN loops 
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.05.02      Bugfix #184: constructor For(String) now supports code import (KGU#192)
 *      Kay Gürtzig     2016.07.21      KGU#207: Slight performance improvement in getElementByCoord()
 *      Kay Gürtzig     2016.07.30      Enh. #128: New mode "comments plus text" supported, drawing code delegated
 *      Kay Gürtzig     2016.09.24      Enh. #250: Adaptations to make the new editor design work
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.awt.Point;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.*;


public class For extends Element implements ILoop {

	// START KGU#61 2016-03-20: Enh. #84/#135
	public enum ForLoopStyle {
		FREETEXT,
		COUNTER,
		TRAVERSAL
	}
	// END KGU#61 2016-03-26

	public Subqueue q = new Subqueue();
	
	// START KGU#136 2016-02-27: Bugfix #97 - replaced by local variable in prepareDraw()
	//private Rect r = new Rect();
	// END KGU#136 2016-02-27
	// START KGU#136 2016-03-01: Bugfix #97
	private Point pt0Body = new Point(0,0);
	// END KGU#136 2016-03-01

	
	// START KGU#3 2015-10-24
	private static String forSeparatorPre = "§FOR§";
	private static String forSeparatorTo = "§TO§";
	private static String forSeparatorBy = "§BY§";
	// START KGU#61 2016-03-20: Enh. #84/#135 - support FOR-IN loops
	private static String forInSeparatorPre = "§FOREACH§";
	private static String forInSeparatorIn = "§IN§";
	// END KGU#61 2016-03-20
	// START KGU#254 2016-09-23: Enh. #250 - Specific key for the case preFor and preForIn are equal
	private static String commonSeparatorPre = "§FORCOMMON§";
	// END KGU#254 2016-09-23
	// The following fields are dedicated for unambiguous semantics representation. If and only if the
	// structured information of these fields is consistent field isConsistent shall be true.
	private String counterVar = "";			// name of the counter variable
	private String startValue = "1";		// expression determining the start value of the loop
	private String endValue = "";			// expression determining the end value of the loop
	private int stepConst = 1;				// an integer value defining the increment/decrement
	//@Deprecated
	//public boolean isConsistent = false;	// flag determining whether the semantics is consistently defined by the dedicated fields
	// END KGU#3 2015-10-24
	// START KGU#61 2016-03-20: Enh. #84/#135 - now we have to distinguish three styles
	private String valueList = null;		// expression specifying the set (array) of values
	public ForLoopStyle style = ForLoopStyle.FREETEXT;
	// END KGU#61 2016-03-20
	
	/**
	 * Standard constructor producing an empty element.
	 */
	public For()
	{
		super();
		q.parent=this;
	}

	/**
	 * This constructor version is intended to be used by the code import. It automatically
	 * fills in the dedicated parameter fields by analysing the passed-in header string _string. 
	 * @param _string - the header string
	 */
	public For(String _string)
	{
		super(_string);
		q.parent=this;
		setText(_string);
		// START KGU#192 2016-05-02: Bugfix #184 - Support for code import (Pascal)
		this.updateFromForClause();
		// END KGU#192 2016-05-02
	}
	
	/**
	 * This is a more low-level constructor e.g. to be used on copying. It simply adopts
	 * the given StringList _strings as text content and does NOT attempt to fill in the
	 * FOR-loop-specific parameter fields (this is supposed to be done explicitly afterwards)
	 * @param _strings
	 */
	public For(StringList _strings)
	{
		super(_strings);
		q.parent=this;
		setText(_strings);
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
//		this.q.resetDrawingInfoDown();
//	}
//	// END KGU#64 2015-11-03
	
	
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97
		if (this.isRectUpToDate) return rect0;
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

		// START KGU#227 2016-07-30: Enh. #128 - Just delegate the basics to Instruction
//		rect0.top = 0;
//		rect0.left=0;
//
//		int padding = 2*(E_PADDING/2); 
//		rect0.right = padding;
//
//		FontMetrics fm = _canvas.getFontMetrics(Element.font);
//
//		int nLines = getText(false).count();
//		for (int i = 0; i < nLines; i++)
//		{
//			int lineWidth = getWidthOutVariables(_canvas, getText(false).get(i), this) + padding;
//			if (rect0.right < lineWidth)
//			{
//				rect0.right = lineWidth;
//			}
//		}
//		rect0.bottom = padding + nLines * fm.getHeight();
		rect0 = Instruction.prepareDraw(_canvas, this.getText(false), this);
		int padding = 2*(E_PADDING/2); 
		// END KGU#227 2016-07-30: Enh. #128 Just delegate the basics to Instruction


		// START KGU#136 2016-03-01: Bugfix #97 - Preparation for local coordinate detection
		this.pt0Body.x = padding - 1;		// FIXME: Fine tuning!
		this.pt0Body.y = rect0.bottom - 1;	// FIXME: Fine tuning!
		// END KGU#136 2016-03-01

		// START KGU#136 2016-02-27: Bugfix #97 - field replaced by local variable
		//r = q.prepareDraw(_canvas);
		//rect.right = Math.max(rect.right, r.right + E_PADDING);
		Rect rectBody = q.prepareDraw(_canvas);
		rect0.right = Math.max(rect0.right, rectBody.right + E_PADDING);
		// END KGU#136 2016-02-27

		rect0.bottom += rectBody.bottom;		
		if (Element.E_DIN==false)
		{
			rect0.bottom += E_PADDING;
		}

		// START KGU#136 2016-03-01: Bugfix #97
		isRectUpToDate = true;
		// END KGU#136 2016-03-01
		return rect0;
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{

		if (isCollapsed()) 
		{
			Instruction.draw(_canvas, _top_left, getCollapsedText(), this);
			return;
		}

		// START KGU#227 2016-07-30: Enh. #128 - on this occasion delegate as much as possible
//		// START KGU 2015-10-12: Common beginning of both styles
//		Rect myrect = new Rect();
//		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
//		//Color drawColor = getColor();
//		Color drawColor = getFillColor();
//		// END KGU 2015-10-13
//		FontMetrics fm = _canvas.getFontMetrics(font);
//		// END KGU 2015-10-13
//
//		Canvas canvas = _canvas;
//		canvas.setBackground(drawColor);
//		canvas.setColor(drawColor);
//
//
//		int headerHeight = fm.getHeight() * getText(false).count() + 2 * (Element.E_PADDING / 2);
//
//		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
//		//rect = _top_left.copy();
//		rect = new Rect(0, 0, 
//				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
//		Point ref = this.getDrawPoint();
//		this.topLeft.x = _top_left.left - ref.x;
//		this.topLeft.y = _top_left.top - ref.y;
//		// END KGU#136 2016-03-01
//
//		// FIXME: What's this nonsense good for?
//		if(Element.E_DIN==false)
//		{
//			// draw background
//			myrect = _top_left.copy();
//			canvas.fillRect(myrect);
//			
//			// draw shape
//			canvas.setColor(Color.BLACK);
//			canvas.drawRect(_top_left);
//			
//			myrect = _top_left.copy();
//			myrect.bottom = _top_left.top + headerHeight;
//			canvas.drawRect(myrect);
//			
//			myrect.bottom = _top_left.bottom;
//			myrect.top = myrect.bottom-E_PADDING;
//			canvas.drawRect(myrect);
//			
//			myrect = _top_left.copy();
//			myrect.right = myrect.left+E_PADDING;
//			canvas.drawRect(myrect);
//			
//			// fill shape
//			canvas.setColor(drawColor);
//			myrect.left = myrect.left+1;
//			myrect.top = myrect.top+1;
//			myrect.bottom = myrect.bottom;
//			myrect.right = myrect.right-1;
//			canvas.fillRect(myrect);
//			
//			myrect = _top_left.copy();
//			myrect.bottom = _top_left.top + headerHeight;
//			myrect.left = myrect.left+1;
//			myrect.top = myrect.top+1;
//			myrect.bottom = myrect.bottom;
//			myrect.right = myrect.right-1;
//			canvas.fillRect(myrect);
//			
//			myrect.bottom = _top_left.bottom;
//			myrect.top = myrect.bottom-Element.E_PADDING;
//			myrect.left = myrect.left+1;
//			myrect.top = myrect.top+1;
//			myrect.bottom = myrect.bottom;
//			myrect.right = myrect.right;
//			canvas.fillRect(myrect);
//		}
//		else
//		{
//			// draw shape
//			myrect = _top_left.copy();
//			canvas.setColor(Color.BLACK);
//			myrect.bottom = _top_left.top + headerHeight;
//			canvas.drawRect(myrect);
//			
//			myrect=_top_left.copy();
//			myrect.right = myrect.left+Element.E_PADDING;
//			canvas.drawRect(myrect);
//			
//			// fill shape
//			canvas.setColor(drawColor);
//			myrect.left = myrect.left+1;
//			myrect.top = myrect.top+1;
//			myrect.bottom = myrect.bottom;
//			myrect.right = myrect.right;
//			canvas.fillRect(myrect);
//			
//			myrect = _top_left.copy();
//			myrect.bottom = _top_left.top + headerHeight;
//			myrect.left = myrect.left+1;
//			myrect.top = myrect.top+1;
//			myrect.bottom = myrect.bottom;
//			myrect.right = myrect.right;
//			canvas.fillRect(myrect);
//		}
//		
//		// START KGU 2015-10-12: D.R.Y. - common tail of both branches re-united here
//		if(Element.E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
//		{
//			this.drawCommentMark(canvas, _top_left);
//		}
//		// draw breakpoint bar if necessary
//		this.drawBreakpointMark(canvas, _top_left);
//
//		// draw text
//		for (int i=0; i<getText(false).count(); i++)
//		{
//			String text = this.getText(false).get(i);
//			text = BString.replace(text, "<--","<-");
//			
//			canvas.setColor(Color.BLACK);
//			writeOutVariables(canvas,
//							  _top_left.left + (E_PADDING / 2),
//							  _top_left.top + (E_PADDING / 2) + (i+1)*fm.getHeight(),
//							  text, this
//							  );  	
//		}
//		
//		// START KGU#156 2016-03-11: Enh. #124
//		// write the run-time info if enabled
//		this.writeOutRuntimeInfo(canvas, _top_left.left + rect.right - (Element.E_PADDING / 2), _top_left.top);
//		// END KGU#156 2016-03-11
		Instruction.draw(_canvas, _top_left, this.getText(false), this);
		// END KGU#227 2016-07-30: Enh. #128 - on this occasion delegate as much as possible
				
		// draw children
		Rect myrect = _top_left.copy();
		// START KGU#227 2016-07-30: Enh. #128 + code revision
		//myrect.left = myrect.left+Element.E_PADDING-1;
		//myrect.top = _top_left.top + headerHeight-1;
		myrect.left += pt0Body.x;
		myrect.top += pt0Body.y;
		// END KGU#227 2016-07-30
		if (Element.E_DIN == false)
		{
			myrect.bottom = myrect.bottom-E_PADDING+1;
		}
		q.draw(_canvas,myrect);
		// END KGU 2015-10-12
	}
	
	// START KGU#122 2016-01-03: Enh. #87 - Collapsed elements may be marked with an element-specific icon
	@Override
	protected ImageIcon getIcon()
	{
		if (Element.E_DIN)
		{
			return IconLoader.ico062;
		}
		return IconLoader.ico061;
	}
	// END KGU#122 2016-01-03

	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x, _y, _forSelection);
		// START KGU#121 2016-01-03: Bugfix #87 - A collapsed element has no visible substructure!
    	// START KGU#207 2016-07-21: If this element isn't hit then there is no use searching the substructure
		//if (!this.isCollapsed())
		if (!this.isCollapsed() && (selMe != null || _forSelection))
		// START KGU#207 2016-07-21
		{
		// END KGU#121 2016-01-03
			// START KGU#136 2016-03-01: Bugfix #97
			//Element sel = q.getElementByCoord(_x, _y, _forSelection);
			Element sel = q.getElementByCoord(_x-pt0Body.x, _y-pt0Body.y, _forSelection);
			// END KGU#136 2016-03-01
			if(sel!=null) 
			{
				if (_forSelection) selected=false;
				selMe = sel;
			}
		// START KGU#121 2016-01-03: Bugfix #87 (continued)
		}
		// END KGU#121 2016-01-03
		
		return selMe;
	}

//	public void setSelected(boolean _sel)
//	{
//		selected=_sel;
//		//q.setSelected(_sel);
//	}
	
	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	public Element findSelected()
	{
		Element sel = selected ? this : null;
		if (sel == null)
		{
			sel = q.findSelected();
		}
		return sel;
	}
	// END KGU#183 2016-04-24
	    
	public Element copy()
	{
		For ele = new For(this.getText().copy());
		copyDetails(ele, false);
		// START KGU#81 (bug #28) 2015-11-14: New fields must be copied, too!
		ele.counterVar = this.counterVar + "";
		ele.startValue = this.startValue + "";
		ele.endValue = this.endValue + "";
		ele.stepConst = this.stepConst;
		//ele.isConsistent = this.isConsistent;
		// END KGU#81 (bug #28) 2015-11-14
		// START KGU#61 2016-03-20: Enh. #84/#135
		ele.style = this.style;
		ele.valueList = this.valueList;
		// END KGU#61 2016-03-20
		ele.q=(Subqueue) this.q.copy();
		ele.q.parent=ele;
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
		return super.equals(_another) && this.q.equals(((For)_another).q) &&
				//this.isConsistent == ((For)_another).isConsistent &&
				this.style == ((For)_another).style;
	}
	// END KGU#119 2016-01-02
	
	// START KGU#117 2016-03-07: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#combineCoverage(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	public boolean combineRuntimeData(Element _cloneOfMine)
	{
		return super.combineRuntimeData(_cloneOfMine) &&
				this.getBody().combineRuntimeData(((ILoop)_cloneOfMine).getBody());
	}
	// END KGU#117 2016-03-07

//	// START KGU#43 2015-10-12
//	@Override
//	public void clearBreakpoints()
//	{
//		super.clearBreakpoints();
//		this.q.clearBreakpoints();
//	}
//	// END KGU#43 2015-10-12
//
//	// START KGU#43 2015-10-13
//	@Override
//	public void clearExecutionStatus()
//	{
//		super.clearExecutionStatus();
//		this.q.clearExecutionStatus();
//	}
//	// END KGU#43 2015-10-13
//
//	// START KGU#117 2016-03-06: Enh. #77
//	/* (non-Javadoc)
//	 * @see lu.fisch.structorizer.elements.Element#clearTestCoverage()
//	 */
//	@Override
//	public void clearRuntimeData()
//	{
//		super.clearRuntimeData();
//		this.getBody().clearRuntimeData();
//	}
//	// END KGU#117 2016-03-06

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

	// START KGU#117 2016-03-10: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isTestCovered(boolean)
	 */
	public boolean isTestCovered(boolean _deeply)
	{
		return this.getBody().isTestCovered(_deeply);
	}
	// END KGU#117 2016-03-10

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
		// START KGU#3 2015-11-30: Fine tuning
		//_lines.add(this.getText());
		if (!_instructionsOnly)
		{
			_lines.add(this.getText());
		}
		// END KGU#3 2015-11-30
		this.q.addFullText(_lines, _instructionsOnly);
    }
    // END KGU 2015-10-16

	// START KGU#3 2015-10-24
	
	/**
	 * Retrieves the counter variable name either from stored value or from text
	 * @return name of the counter variable
	 */
	public String getCounterVar()
	{
		// START KGU#61 2016-03-20: Enh. #84/#135
		//if (this.isConsistent)
		if (this.style != ForLoopStyle.FREETEXT)
		// END KGU#61 206-03-20
		{
			return this.counterVar;
		}
		return this.splitForClause()[0];
	}
	
	/**
	 * Retrieves the start value expression either from stored value or from text
	 * @return expression to compute the start value
	 */
	public String getStartValue()
	{
		// START KGU#61 2016-03-20: Enh. #84/#135
		//if (this.isConsistent)
		if (this.style == ForLoopStyle.COUNTER)
		// END KGU#61 206-03-20
		{
			return this.startValue;
		}
		return this.splitForClause()[1];
	}
	
	/**
	 * Retrieves the end value expression either from stored value or from text
	 * @return expression to compute the end value
	 */
	public String getEndValue()
	{
		// START KGU#61 2016-03-20: Enh. #84/#135
		//if (this.isConsistent)
		if (this.style == ForLoopStyle.COUNTER)
		// END KGU#61 206-03-20
		{
			return this.endValue;
		}
		return this.splitForClause()[2];
	}
	
	/**
	 * Retrieves the counter increment either from stored value or from text
	 * @return the constant increment (or decrement)
	 */
	public int getStepConst()
	{
		int step = 1;
		// START KGU#61 2016-03-20: Enh. #84/#135
		//if (this.isConsistent)
		if (this.style == ForLoopStyle.COUNTER)
		// END KGU#61 206-03-20
		{
			step = this.stepConst;
		}
		else
		{
			String stepStr = this.splitForClause()[3]; 
			step = Integer.valueOf(stepStr);
		}
		return step;
	}
	
	/**
	 * Retrieves the counter increment either from stored value or from text
	 * @return string representing the constant increment (or decrement)
	 */
	public String getStepString()
	{
		// START KGU#61 2016-03-20: Enh. #84/#135
		//if (this.isConsistent)
		if (this.style == ForLoopStyle.COUNTER)
		// END KGU#61 206-03-20
		{
			return Integer.toString(this.stepConst);
		}
		else
		{
			return this.splitForClause()[3]; // Or should we provide this.splitClause()[4]?
		}
	}
	
	// START KGU#61 2016-03-22: Enh. #84/#135
	/**
	 * Retrieves the set or list of values to be traversed (For-In style)
	 * @return string representing the array variable or literal
	 */
	public String getValueList()
	{
		//if (this.isConsistent)
		String valList = null;
		if (this.style == ForLoopStyle.TRAVERSAL && (valList = this.valueList) == null ||
				this.style == ForLoopStyle.FREETEXT)
		{
			// START KGU#254 2016-09-24: A get method shouldn't modify the element
			//this.setValueList(valList = this.splitForClause()[5]);
			valList = this.splitForClause()[5];
			// END KGU#254 2016-09-24
		}
		return valList;
	}
	// END KGU#61 2016-03-22
	
    
	/**
	 * @param counterVar the counterVar to set
	 */
	public void setCounterVar(String counterVar) {
		this.counterVar = counterVar;
	}

	/**
	 * @param startValue the startValue to set
	 */
	public void setStartValue(String startValue) {
		this.startValue = startValue;
	}

	/**
	 * @param endValue the endValue to set
	 */
	public void setEndValue(String endValue) {
		this.endValue = endValue;
	}

	/**
	 * @param stepConst the stepConst to set
	 */
	public void setStepConst(int stepConst) {
		this.stepConst = stepConst;
	}

	public void setStepConst(String stepConst) {
		if (stepConst == null || stepConst.isEmpty())
		{
			this.stepConst = 1;
		}
		else 
		{
			try
			{
				this.stepConst = Integer.valueOf(stepConst);
			}
			catch (Exception ex) {}
		}
	}
	// END KGU#3 2015-10-24

	// START KGU#61 2016-03-22: Enh. #84/#135 - needed for FOR-IN loops
	/**
	 * @param valueList the String representing the values to be traversed
	 */
	public void setValueList(String valueList) {
		this.valueList = valueList;
		if (this.getText().getLongString().trim().equals(this.composeForInClause()))
		{
			this.style = ForLoopStyle.TRAVERSAL;
		}
	}
	// END KGU#61 2016-03-22

	// START KGU#3 2015-11-04: We need a transformation to a common intermediate language
	private static StringList disambiguateForClause(String _text)
	{
		// Pad the string to ease the key word detection
		//String interm = " " + _text + " ";
		StringList tokens = Element.splitLexically(_text, true);

		// START KGU#61 2016-03-20: Enh. #84/#135
		// First collect the placemarkers of the for loop header ...
		//String[] forMarkers = {D7Parser.preFor, D7Parser.postFor, D7Parser.stepFor};
		// ... and their replacements (in same order!)
		//String[] forSeparators = {forSeparatorPre, forSeparatorTo, forSeparatorBy};
		// First collect the placemarkers of the for loop header ...
		String[] forMarkers = {D7Parser.preFor, D7Parser.postFor, D7Parser.stepFor,
				(D7Parser.preForIn.trim().isEmpty() ? D7Parser.preFor : D7Parser.preForIn), D7Parser.postForIn};
		// ... and their replacements (in same order!)
		String[] forSeparators = {forSeparatorPre, forSeparatorTo, forSeparatorBy,
				forInSeparatorPre, forInSeparatorIn};
		// END KGU#61 2016-03-20
		// START KGU#61 2016-09-23: Enh. #84/#135/#250 improvement
		if (forMarkers[0].equals(forMarkers[3]))
		{
			forSeparators[0] = commonSeparatorPre;
		}
		// END KGU#61 2016-09-23

		// The configured markers for the For loop are not at all redundant but the only sensible
		// hint how to split the line into the counter variable, the initial and the final value (and possibly the step).
		// FIXME The composition of the regular expressions here conveys some risk since we may not know
		// what the user might have configured (see above)
		for (int i = 0; i < forMarkers.length; i++)
		{
			String marker = forMarkers[i];
			if (!marker.isEmpty())
			{
				StringList markerTokens = Element.splitLexically(marker, false);
				int markerLen = markerTokens.count();
				int pos = -1;
				while ((pos = tokens.indexOf(markerTokens, pos+1, !D7Parser.ignoreCase)) >= 0)
				{
					// Replace the first token of the parser keyword by the separator 
					tokens.set(pos, forSeparators[i]);
					// ... and remove the remaining ones (they will all pass through pos+1)
					for (int d = 1; d < markerLen; d++)
					{
						tokens.delete(pos+1);
					}
				}
			}
		}

		//return interm;
		return tokens;

	}
	

	/**
	 * Splits the contained text (after operator unification, see unifyOperators for details)
	 * into an array consisting of six strings meant to have following meaning:
	 * 0. counter variable name 
	 * 1. expression representing the initial value
	 * 2. expression representing the final value
	 * 3. Integer literal representing the increment value ("1" if the substring can't be parsed)
	 * 4. Substring for increment section as found on splitting (no integer coercion done)
	 * 5. Substring representing the set of values to be traversed (FOR-IN loop) or null
	 * 
	 * @param _text the FOR clause to be split (something like "for i <- 1 to n")
	 * @return String array consisting of the four parts explained above
	 */
	public String[] splitForClause()
	{
		return splitForClause(this.getText().getText());
	}
	
	/**
	 * Splits a potential FOR clause (after operator unification, see unifyOperators for details)
	 * into an array consisting of six strings meant to have following meaning:
	 * 0. counter variable name 
	 * 1. expression representing the initial value
	 * 2. expression representing the final value
	 * 3. Integer literal representing the increment value ("1" if the substring can't be parsed)
	 * 4. Substring for increment section as found on splitting (no integer coercion done)
	 * 5. Substring representing the set of values to be traversed (FOR-IN loop) or null
	 * 
	 * @param _text the FOR clause to be split (something like "for i <- 1 to n")
	 * @return String array consisting of the four parts explained above
	 */
	public static String[] splitForClause(String _text)
	{
		// START KGU#61 2016-03-20: Enh. #84/#135 - consider FOR-IN loops
		// Components are: loop variable, start value, end value, step int, step string, value set
		//String[] forParts = { "dummy_counter", "1", null, "1", ""};
		// Set some defaults
		//String init = "";	// Initialisation instruction
		// END KGU#61 2016-03-20
		
		// Do some pre-processing to disambiguate the key words
		StringList tokens = disambiguateForClause(_text);		
		//System.out.println("Disambiguated For clause: \"" + _intermediate + "\"");
		
		tokens.replaceAll("\n", " "); // Concatenate the lines
		int posFor = tokens.indexOf(forSeparatorPre);
		//int lenFor = forSeparatorPre.length();
		int posTo = tokens.indexOf(forSeparatorTo);
		//int lenTo = forSeparatorTo.length();
		int posBy = tokens.indexOf(forSeparatorBy);
		//int lenBy = forSeparatorBy.length();
		int posForIn = tokens.indexOf(forInSeparatorPre);
		int posIn = tokens.indexOf(forInSeparatorIn);
		// START KGU#61 2016-03-20: Enh. #84/#135 - must go different ways now
		// If both forInSeparatorIn and forSeparatorTo occur then a traditional loop is assumed
		// START KGU#61 2016-09-23: Enh. #250  More criteria combinations (if for and forin keywords are equal then
		// posForIn will always be -1.
		//if (posIn > 0 && posTo < 0)
		//{
		//	return splitForTraversal(tokens, posFor, posForIn, posIn);
		//}
		if (posFor < 0 && posForIn < 0)
		{
			posFor = posForIn = tokens.indexOf(commonSeparatorPre);
		}
		if (posIn > 0 && posTo < 0 || posIn > 0 && posForIn >= 0 || posForIn >= 0 && posTo < 0)
		{
			return splitForTraversal(tokens, posForIn, posIn);
		}
		// END KGU#61 2016-09-23
		else {
			return splitForCounter(tokens, posFor, posTo, posBy);
		}
		// END KGU#61 2016-03-20
	}
	
	// START KGU#61 2016-03-20: Enh. #84/#135 - outsourced from splitForClause(String)
	private static String[] splitForCounter(StringList _tokens, int _posFor, int _posTo, int _posBy)
	{
		String[] forParts = { "dummy_counter", "1", null, "1", "", null};
		int endInit = (_posTo >= 0) ? _posTo : _tokens.count();
		if (_posBy >= 0 && _posBy < _posTo) endInit = _posBy;
		StringList init = _tokens.subSequence(_posFor+1, endInit);
		//System.out.println("FOR --> \"" + init + "\"");
		if (_posTo >= 0)
		{
			int endTo = (_posBy > _posTo) ? _posBy : _tokens.count();
			forParts[2] = _tokens.subSequence(_posTo + 1, endTo).concatenate().trim();
			//System.out.println("TO --> \"" + forParts[2] + "\"");
		}
		if (_posBy >= 0)
		{
			int endBy = (_posTo > _posBy) ? _posTo : _tokens.count();
			forParts[4] = _tokens.subSequence(_posBy + 1, endBy).concatenate().trim();
			//System.out.println("BY --> \"" + forParts[4] + "\"");
		}
		if (forParts[4].isEmpty())
		{
			forParts[3] = "1";
		}
		else
		{
			try
			{
				forParts[3] = Integer.valueOf(forParts[4]).toString();
			}
			catch (NumberFormatException ex)
			{
				forParts[3] = "1";
			}
		}
		unifyOperators(init, true);
		int posAsgnOpr = init.indexOf("<-");
		if (posAsgnOpr > 0)
		{
			forParts[0] = init.subSequence(0, posAsgnOpr).concatenate().trim();
		}
		forParts[1] = init.subSequence(posAsgnOpr + 1, init.count()).concatenate().trim();
		
		return forParts;		
	}

	// START KGU#61 2016-09-23: Enh, #250 - Signature reduced by one parameter
	//private static String[] splitForTraversal(StringList _tokens, int _posFor, int _posForIn, int _posIn)
	private static String[] splitForTraversal(StringList _tokens, int _posForIn, int _posIn)
	// END KGU#61 2016-09-23
	{
		String[] forParts = { "dummy_iterator", "", null, "", "", "{}"};
		// START KGU#61 2016-09-23: Enh. #250 - Dropped		
		//if (_posForIn < 0)
		//{
		//	_posForIn = _posFor;
		//}
		// END KGU#61 2016-09-23
		forParts[0] = _tokens.subSequence(_posForIn + 1, _posIn).concatenate().trim();
		forParts[5] = _tokens.subSequence(_posIn + 1, _tokens.count()).concatenate().trim();
		return forParts;
		
	}
	
	// START KGU#192 2016-05-02: Bugfix #184 - New method to support code import (from Pascal)
	/**
	 * Splits the For clause and updates the structured parameter fields accordingly,
	 * classifies the loop style, sets and returns the style code.
	 * Intended to be used in the constructor with String argument.
	 * @return the identified style of the loop (counting, traversing, or "freestyle")
	 */
	private ForLoopStyle updateFromForClause()
	{
		String[] forParts = this.splitForClause();
		this.setCounterVar(forParts[0]);
		this.setStartValue(forParts[1]);
		this.setEndValue(forParts[2]);
		this.setStepConst(forParts[3]);
		this.setValueList(forParts[5]);
		return (this.style = this.classifyStyle());
	}
	// END KGU#192 2016-05-02
	
	/**
	 * Returns the FOR loop header resulting from the stored structured fields,
	 * not forcing a step section
	 * @return the composed For loop header
	 */
	public String composeForClause()
	{
		return composeForClause(false);
	}
	
	/**
	 * Returns the FOR loop header resulting from the stored structured fields
	 * @param _forceStep - if a step section is to be produced even in case step==1
	 * @return the composed For loop header
	 */
	public String composeForClause(boolean _forceStep)
	{
		return composeForClause(this.counterVar, this.startValue, this.endValue, this.stepConst, _forceStep);
	}
	
	// START KGU#61 2016-03-20: Enh. #84/#135
	/**
	 * Returns the FOR loop header resulting from the given arguments
	 * @param _counter - name of the loop variable
	 * @param _start - start value expression
	 * @param _end - end value expression
	 * @param _step - increment literal (integer constant)
	 * @param _forceStep - if a step section is to be produced even in case step==1
	 * @return the composed For loop header
	 */
	public static String composeForClause(String _counter, String _start, String _end, String _step, boolean _forceStep)
	{
		int step = 1;
		try
		{
			step = Integer.valueOf(_step);
		}
		catch (Exception ex)
		{}
		return composeForClause(_counter, _start, _end, step, _forceStep);
	}
	
	/**
	 * Returns the FOR loop header resulting from the given arguments
	 * @param _counter - name of the loop variable
	 * @param _start - start value expression
	 * @param _end - end value expression
	 * @param _step - increment integer constant
	 * @param _forceStep - if a step section is to be produced even in case step==1
	 * @return the composed For loop header
	 */
	public static String composeForClause(String _counter, String _start, String _end, int _step, boolean _forceStep)
	{
		String asgnmtOpr = " <- ";	// default assignment operator
		// If the preset text prefers the Pascal assignment operator then we will use this instead
		if (Element.preFor.indexOf("<-") < 0 && Element.preFor.indexOf(":=") >= 0)
		{
			asgnmtOpr = " := ";
		}
		String forClause = D7Parser.preFor.trim() + " " + _counter + asgnmtOpr + _start + " " +
				D7Parser.postFor.trim() + " " + _end;
		if (_step != 1 || _forceStep)
		{
			forClause = forClause + " " + D7Parser.stepFor.trim() + " " + Integer.toString(_step);
		}
		// Now get rid of multiple blanks
		forClause = BString.replace(forClause, "  ", " ");
		forClause = BString.replace(forClause, "  ", " ");
		return forClause;
	}
	
	public boolean checkConsistency()
	{
		//String string1 = this.getText().getLongString();
		//String string2 = this.composeForClause();
		// START KGU#61 2016-03-20: Enh. #84/#135 - there are two styles now
		//return this.getText().getLongString().equals(this.composeForClause());
		return this.classifyStyle() != ForLoopStyle.FREETEXT;
		// END KGU#61 2016-03-20
	}
	// END KGU#3 2015-11-04

	// START KGU#61 2016-03-20: Enh. #84/#135
	public String composeForInClause()
	{
		String valueList = this.valueList;
		// START KGU#254 2016-09-24: #250 - There should be a chance for FREETEXT loops to be recognised
		if (valueList == null) valueList = this.getValueList();
		// END KGU#254 2016-09-24
		return composeForInClause(this.counterVar, valueList);
	}

	public static String composeForInClause(String _iterator, String _valueList)
	{
		String preForIn = D7Parser.preForIn.trim();
		if (preForIn.isEmpty()) { preForIn = D7Parser.preFor.trim(); }
		String forClause = preForIn + " " + _iterator + " " +
				D7Parser.postForIn.trim() + " " + _valueList;
		return forClause;
	}
	
	/**
	 * Classifies the loop style based only on the congruence of the stored
	 * text with the generated textes of the styles COUNTER and TRAVERSAL
	 * (the latter being the code for FOR-IN loops).
	 * You might also consider testing this.style (which jus returns an cached
	 * earlier classification) and this.isForInLoop(), which first checks the
	 * cached classification and if this is FREETEXT also calls this method
	 * in order to find out whether this complies with FOR-IN syntax.
	 * @return One of the style codes COUNTER, TRAVERSAL, and FREETEXT
	 */
	public ForLoopStyle classifyStyle()
	{
		ForLoopStyle style = ForLoopStyle.FREETEXT;
		String thisText = this.getText().getLongString().trim();
		//System.out.println(thisText + " <-> " + this.composeForClause() + " <-> " + this.composeForInClause());
		
		if (D7Parser.ignoreCase)
		{
			if (thisText.equalsIgnoreCase(this.composeForClause()) ||
					thisText.equalsIgnoreCase(this.composeForClause(true)))
			{
				style = ForLoopStyle.COUNTER;
			}
			else if (thisText.equalsIgnoreCase(this.composeForInClause()))
			{
				style = ForLoopStyle.TRAVERSAL;
			}			
		}
		else
		{
			if (thisText.equals(this.composeForClause()) ||
					thisText.equals(this.composeForClause(true)))
			{
				style = ForLoopStyle.COUNTER;
			}
			else if (thisText.equals(this.composeForInClause()))
			{
				style = ForLoopStyle.TRAVERSAL;
			}
		}
		return style;
	}
	
	/**
	 * Convenience method to find out whether this FOR loop has FOR-IN
	 * style, because it has checks it in two ways:
	 * 1. by the stored attribute style
	 * 2. if 1 results in FREETEXT also compares the text against the
	 * split and composed stored information 
	 */
	public boolean isForInLoop()
	{
		boolean isForIn = this.style == ForLoopStyle.TRAVERSAL;
		if (!isForIn && this.style == ForLoopStyle.FREETEXT)
		{
			isForIn = this.classifyStyle() == ForLoopStyle.TRAVERSAL;
		}
		return isForIn;
	}
	// END KGU#61 2016-03-20

	// START KGU 2015-11-30
	@Override
	public Subqueue getBody() {
		return this.q;
	}
	// END KGU 2015-11-30
	
//    public static void main(String[] args)
//	{
//    	Vector<For> forElems = new Vector<For>();
//		forElems.add(new For("for doof <- 42 to 56"));
//		forElems.add(new For("for troll in {34, 252, 21}"));
//		forElems.add(new For("for int mumpitz in 18 20 22 23 24 27 passe"));
//		forElems.add(new For("irgendein Schwachsinn hier!"));
//		for (int f = 0; f < forElems.size(); f++)
//		{
//			String[] components = forElems.get(f).splitForClause();
//			for (int i = 0; i < components.length; i++) System.out.println(i + ": " + components[i]);
//			if (components[5] == null)
//			{
//				forElems.get(f).setCounterVar(components[0]);
//				forElems.get(f).setStartValue(components[1]);
//				forElems.get(f).setEndValue(components[2]);
//				forElems.get(f).setStepConst(components[3]);
//			}
//			else
//			{
//				forElems.get(f).setCounterVar(components[0]);
//				forElems.get(f).setValueList(components[5]);				
//			}
//			System.out.println(forElems.get(f).classifyStyle());
//		}		
//	}

	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 */
	@Override
	public void convertToCalls(StringList _signatures)
	{
		getBody().convertToCalls(_signatures);
	}
	// END KGU#199 2016-07-07

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#traverse(lu.fisch.structorizer.elements.IElementVisitor)
	 */
	@Override
	public boolean traverse(IElementVisitor _visitor) {
		boolean proceed = _visitor.visitPreOrder(this);
		if (proceed)
		{
			proceed = this.getBody().traverse(_visitor);
		}
		if (proceed)
		{
			proceed = _visitor.visitPostOrder(this);
		}
		return proceed;
	}


}

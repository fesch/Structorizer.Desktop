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
 *      Description:    This class represents an "FOR loop" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007-12-07      First Issue
 *      Bob Fisch       2008-02-06      Modified for DIN / not DIN
 *      Kay Gürtzig     2015-10-11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015-10-12      Comment drawing centralized and breakpoint mechanism prepared.
 *      Kay Gürtzig     2015-11-04      New mechanism to split and compose the FOR clause into/from dedicated fields
 *      Kay Gürtzig     2015-11-14      Bugfixes (#28 = KGU#80 and #31 = KGU#82) in Method copy
 *      Kay Gürtzig     2015-11-30      Inheritance changed: implements ILoop
 *      Kay Gürtzig     2015-12-01      Bugfix #39 (=KGU#91) -> getText(false), prepareDraw() optimised
 *      Kay Gürtzig     2016-01-02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016-01-03      Bugfix #87 (KGU#121): Correction in getElementByCoord(), getIcon()
 *      Kay Gürtzig     2016-02-27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016-03-01      Bugfix #97 (KGU#136): Translation-neutral selection
 *      Kay Gürtzig     2016-03-06      Enh. #77 (KGU#117): Fields for test coverage tracking added
 *      Kay Gürtzig     2016-03-12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016-03-20      Enh. #84/#135 (KGU#61): enum type and methods introduced/modified
 *                                      to distinguish and handle FOR-IN loops 
 *      Kay Gürtzig     2016-04-24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016-05-02      Bugfix #184: constructor For(String) now supports code import (KGU#192)
 *      Kay Gürtzig     2016-07-21      KGU#207: Slight performance improvement in getElementByCoord()
 *      Kay Gürtzig     2016-07-30      Enh. #128: New mode "comments plus text" supported, drawing code delegated
 *      Kay Gürtzig     2016-09-24      Enh. #250: Adaptations to make the new editor design work
 *      Kay Gürtzig     2016-09-25      Issue #252: ':=' and '<-' equivalence in consistency check
 *                                      Enh. #253: CodeParser.keywordMap refactored
 *      Kay Gürtzig     2016-10-04      Enh. #253: Refactoring configuration revised
 *      Kay Gürtzig     2017-01-26      Enh. #259: Type retrieval support added (for counting loops)
 *      Kay Gürtzig     2017-04-14      Enh. #259: Approach to guess FOR-IN loop variable type too
 *      Kay Gürtzig     2017-04-30      Enh. #354: New structured constructors
 *      Kay Gürtzig     2017-11-02      Issue #447: Precaution against line-continuating backslashes 
 *      Kay Gürtzig     2018-02-12:     Issue #4: Separate icons for FOR loops introduced
 *      Kay Gürtzig     2018-04-04      Issue #529: Critical section in prepareDraw() reduced.
 *      Kay Gürtzig     2018-07-12      Separator bug in For(String,String,String,int) fixed.
 *      Kay Gürtzig     2018-10-26      Enh. #619: Method getMaxLineLength() implemented
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-11-21      Enh. #739 Enum types considered in type compatibility check for FOR-IN lists 
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************
 */


import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.gui.FindAndReplace;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.parsers.CodeParser;
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

	// START KGU#258 2016-09-26: Enh. #253
	private static final String[] relevantParserKeysCount = {"preFor", "postFor", "stepFor"};
	private static final String[] relevantParserKeysTrav = {"preForIn", "postForIn"};
	// END KGU#258 2016-09-25
	
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
	
	// START KGU#354 2017-04-30: Enh. #354 Further facilitation of import
	/**
	 * This is a high-level structured constructor version and produces a fully classified
	 * counting loop. 
	 * @param varName - the counter variable name
	 * @param startValStr - the expression for the initial counting value
	 * @param endValStr - the expression for the final counting value
	 * @param stepVal - increment or decrement constant
	 */
	public For(String varName, String startValStr, String endValStr, int stepVal)
	{
		this(CodeParser.getKeywordOrDefault("preFor", "for") + " " + varName
				+ " <- " + startValStr + " "
				+ CodeParser.getKeywordOrDefault("postFor", "to") + " " + endValStr
				+ (stepVal != 1 ? (" " + CodeParser.getKeywordOrDefault("stepFor", "by") + " " + stepVal) : ""));
	}
	
	/**
	 * This is a high-level structured constructor version and produces a fully classified
	 * traversing loop. 
	 * @param varName - the loop variable name
	 * @param startValStr - the expression representing the value list
	 */
	public For(String varName, String valueList)
	{
		this(CodeParser.getKeywordOrDefault("preForIn", "foreach") + " " + varName + " "
				+ CodeParser.getKeywordOrDefault("postForIn", "in") + " " + valueList);
	}
	// END KGU#354 2017-04-30
	
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
		if (this.isRect0UpToDate) return rect0;
		// END KGU#136 2016-03-01
		
		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		if (isCollapsed(true)) 
		{
			rect0 = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
			// START KGU#136 2016-03-01: Bugfix #97
			isRect0UpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}

		// START KGU#227 2016-07-30: Enh. #128 - Just delegate the basics to Instruction
		// START KGU#516 2018-04-04: Issue #529 - Directly to work on field rect0 was not so good an idea for re-entrance
		//rect0 = Instruction.prepareDraw(_canvas, this.getText(false), this);
		Rect rect0 = Instruction.prepareDraw(_canvas, this.getText(false), this);
		Point pt0Body = new Point();
		// END KGU#516 2018-04-04
		int padding = 2*(E_PADDING/2); 
		// END KGU#227 2016-07-30: Enh. #128 Just delegate the basics to Instruction


		// START KGU#136 2016-03-01: Bugfix #97 - Preparation for local coordinate detection
		pt0Body.x = padding - 1;		// FIXME: Fine tuning!
		pt0Body.y = rect0.bottom - 1;	// FIXME: Fine tuning!
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

		// START KGU#516 2018-04-04: Issue #529 - reduced critical section
		this.rect0 = rect0;
		this.pt0Body = pt0Body;
		// END KGU#516 2018-04-04
		// START KGU#136 2016-03-01: Bugfix #97
		isRect0UpToDate = true;
		// END KGU#136 2016-03-01
		return rect0;
	}
	
	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention)
	{
		// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
		if (!checkVisibility(_viewport, _top_left)) { return; }
		// END KGU#502/KGU#524/KGU#553 2019-03-13

		if (isCollapsed(true)) 
		{
			Instruction.draw(_canvas, _top_left, getCollapsedText(), this, _inContention);
			// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
			wasDrawn = true;
			// END KGU#502/KGU#524/KGU#553 2019-03-14
			return;
		}

		// START KGU#227 2016-07-30: Enh. #128 - on this occasion delegate as much as possible
		Instruction.draw(_canvas, _top_left, this.getText(false), this, _inContention);
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
		q.draw(_canvas, myrect, _viewport, _inContention);
		// END KGU 2015-10-12
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
		wasDrawn = true;
		// END KGU#502/KGU#524/KGU#553 2019-03-14
	}
	
	// START KGU#122 2016-01-03: Enh. #87 - Collapsed elements may be marked with an element-specific icon
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getIcon()
	 */
	@Override
	public ImageIcon getIcon()
	{
		if (Element.E_DIN)
		{
			// START KGU#493 2018-02-12: Issue # - separate icons now
			//return IconLoader.getIcon(62);
			return IconLoader.getIcon(74);
			// END KGU#493 2018-02-12
		}
		// START KGU#493 2018-02-12: Issue # - separate icons now
		//return IconLoader.getIcon(61);
		return IconLoader.getIcon(53);
		// END KGU#493 2018-02-12
	}
	// END KGU#122 2016-01-03

	// START KGU#535 2018-06-28
	/**
	 * @return the (somewhat smaller) element-type-specific icon image intended to be used in
	 * the {@link FindAndReplace} dialog.
	 * @see #getIcon()
	 */
	@Override
	public ImageIcon getMiniIcon()
	{
		if (Element.E_DIN) {
			return IconLoader.getIcon(49);
		}
		return IconLoader.getIcon(50);
	}
	// END KGU#535 2018-06-28

	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x, _y, _forSelection);
		// START KGU#121 2016-01-03: Bugfix #87 - A collapsed element has no visible substructure!
    	// START KGU#207 2016-07-21: If this element isn't hit then there is no use searching the substructure
		//if (!this.isCollapsed())
		if (!this.isCollapsed(true) && (selMe != null || _forSelection))
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
			if (!this.isCollapsed(true)) {
				stepInfo = "(" + stepInfo + ")";
			}
			break;
		default:
			stepInfo = Integer.toString(this.getExecStepCount(this.isCollapsed(true)));
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
		if (!this.isDisabled()) {
			// START KGU#3 2015-11-30: Fine tuning
			//_lines.add(this.getText());
			if (!_instructionsOnly)
			{
				// START KGU#453 2017-11-02: Issue #447 - Consider deliberate line continuation
				//_lines.add(this.getText());
				_lines.add(this.getUnbrokenText());
				// END KGU#453 2017-11-02
			}
			// END KGU#3 2015-11-30
			this.q.addFullText(_lines, _instructionsOnly);
		}
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
			return this.splitForClause()[3]; // Or should we provide this.splitForClause()[4]?
		}
	}
	
	// START KGU#61 2016-03-22: Enh. #84/#135
	/**
	 * Retrieves the string representing the set or list of values to be traversed (For-In style)
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
	
	// START KGU 2017-04-14
	/**
	 * Tries to identify  the string representing the set or list of values to be traversed (For-In style)
	 * @return a StringList containing string representations of the items of the value list - or null
	 */
	public StringList getValueListItems()
	{
		StringList valueItems = null;
		String valueListString = this.getValueList();
		if (valueListString != null) {
			valueListString = valueListString.trim();
			boolean hadBraces = valueListString.startsWith("{") && valueListString.endsWith("}");
			StringList valueListTokens = splitLexically(valueListString, true);
			// There are no built-in functions returning an array and external function calls
			// aren't allowed at this position, hence it's relatively safe to conclude
			// an item enumeration from the occurrence of a comma.
			if (valueListTokens.contains(",")) {
				if (hadBraces)
				{
					valueListTokens = valueListTokens.subSequence(1, valueListTokens.count()-1);
				}
				valueItems = splitExpressionList(valueListTokens, ",", false);
			}
			else if (valueListTokens.contains(" ")) {
				valueItems = splitExpressionList(valueListTokens, " ", false);
			}
			
			if (valueItems != null && valueItems.count() == 1 && !hadBraces && Function.testIdentifier(valueItems.get(0), false, ".")) {
				// Now we get into trouble: It ought to be an array variable, which we cannot evaluate here
				// So what do we return?
				// We just return null to avoid misunderstandings
				valueItems = null;
			}
		}
		return valueItems;
	}
	// END KGU 2017-04-14
    
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
		// START KGU#453 2017-11-02: Issue #447 - consider line continuation backslashes
		//if (this.getText().getLongString().trim().equals(this.composeForInClause()))
		if (this.getUnbrokenText().getLongString().trim().equals(this.composeForInClause()))
		// END KGU#453 2017-11-02
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
		//String[] forMarkers = {CodeParser.preFor, CodeParser.postFor, CodeParser.stepFor};
		// ... and their replacements (in same order!)
		//String[] forSeparators = {forSeparatorPre, forSeparatorTo, forSeparatorBy};
		// First collect the placemarkers of the for loop header ...
		String[] forMarkers = {
				CodeParser.getKeyword("preFor"),
				CodeParser.getKeyword("postFor"),
				CodeParser.getKeyword("stepFor"),
				(CodeParser.getKeyword("preForIn").trim().isEmpty() ? CodeParser.getKeyword("preFor") : CodeParser.getKeyword("preForIn")),
				CodeParser.getKeyword("postForIn")
				};
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
				while ((pos = tokens.indexOf(markerTokens, pos+1, !CodeParser.ignoreCase)) >= 0)
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
	 * into an array consisting of six strings meant to have following meaning:<br/>
	 * 0. counter variable name<br/>
	 * 1. expression representing the initial value<br/>
	 * 2. expression representing the final value<br/>
	 * 3. Integer literal representing the increment value ("1" if the substring can't be parsed)<br/>
	 * 4. Substring for increment section as found on splitting (no integer coercion done)<br/>
	 * 5. Substring representing the set of values to be traversed (FOR-IN loop) or null<br/>
	 * @return String array consisting of the four parts explained above
	 */
	public String[] splitForClause()
	{
		// START KGU#453 2017-11-02: Issue #447 - eliminate line continuation backslashes 
		//return splitForClause(this.getText().getText());
		return splitForClause(this.getUnbrokenText().getLongString());
		// END KGU#453 2017-11-02
	}
	
	/**
	 * Splits a potential FOR clause (after operator unification, see unifyOperators for details)
	 * into an array consisting of six strings meant to have following meaning:<br/>
	 * 0. counter variable name<br/>
	 * 1. expression representing the initial value<br/>
	 * 2. expression representing the final value<br/>
	 * 3. Integer literal representing the increment value ("1" if the substring can't be parsed)<br/>
	 * 4. Substring for increment section as found on splitting (no integer coercion done)<br/>
	 * 5. Substring representing the set of values to be traversed (FOR-IN loop) or null<br/>
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
	public ForLoopStyle updateFromForClause()
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
	 * @see #composeForClause(boolean)
	 * @see #composeForInClause()
	 */
	public String composeForClause()
	{
		return composeForClause(false);
	}
	
	/**
	 * Returns the FOR loop header resulting from the stored structured fields
	 * @param _forceStep - if a step section is to be produced even in case step==1
	 * @return the composed For loop header
	 * @see #composeForClause()
	 * @see #composeForClause(String, String, String, int, boolean)
	 * @see #composeForClause(String, String, String, String, boolean)
	 * @see #composeForInClause()
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
	 * @see #composeForClause(String, String, String, int, boolean)
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
	 * @see #composeForClause(String, String, String, String, boolean)
	 */
	public static String composeForClause(String _counter, String _start, String _end, int _step, boolean _forceStep)
	{
		String asgnmtOpr = " <- ";	// default assignment operator
		// If the preset text prefers the Pascal assignment operator then we will use this instead
		if (Element.preFor.indexOf("<-") < 0 && Element.preFor.indexOf(":=") >= 0)
		{
			asgnmtOpr = " := ";
		}
		String forClause = CodeParser.getKeyword("preFor").trim() + " " +
				_counter + asgnmtOpr + _start + " " +
				CodeParser.getKeyword("postFor").trim() + " " + _end;
		if (_step != 1 || _forceStep)
		{
			forClause = forClause + " " + CodeParser.getKeyword("stepFor").trim() + " " +
					Integer.toString(_step);
		}
		// Now get rid of multiple blanks
		forClause = forClause.replace("  ", " ");
		forClause = forClause.replace("  ", " ");
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
	/**
	 * Returns the FOR-IN loop header resulting from the stored structured fields
	 * @return the composed For-In loop header
	 * @see #composeForClause()
	 * @see #composeForInClause(String, String)
	 */
	public String composeForInClause()
	{
		String valueList = this.valueList;
		// START KGU#254 2016-09-24: #250 - There should be a chance for FREETEXT loops to be recognised
		if (valueList == null) valueList = this.getValueList();
		// END KGU#254 2016-09-24
		return composeForInClause(this.counterVar, valueList);
	}

	/**
	 * Returns the FOR-IN loop header resulting from the given arguments.
	 * @param _iterator - the variable name for the iterating variable
	 * @param _valueList - an expression describing the list of things to be iterated over
	 * @return the composed For-In loop header
	 * @see #composeForClause()
	 * @see #composeForInClause()
	 */
	public static String composeForInClause(String _iterator, String _valueList)
	{
		String preForIn = CodeParser.getKeyword("preForIn").trim();
		if (preForIn.isEmpty()) { preForIn = CodeParser.getKeyword("preFor").trim(); }
		String forClause = preForIn + " " + _iterator + " " +
				CodeParser.getKeyword("postForIn").trim() + " " + _valueList;
		return forClause;
	}
	
	/**
	 * Classifies the loop style based only on the congruence of the stored
	 * text with the generated textes of the styles COUNTER and TRAVERSAL
	 * (the latter being the code for FOR-IN loops).
	 * You might also consider testing this.style (which just returns a cached
	 * earlier classification) and this.isForInLoop(), which first checks the
	 * cached classification and if this is FREETEXT also calls this method
	 * in order to find out whether this complies with FOR-IN syntax.
	 * Note that assignment operator differences will be tolerated.
	 * @return One of the style codes COUNTER, TRAVERSAL, and FREETEXT
	 */
	public ForLoopStyle classifyStyle()
	{
		ForLoopStyle style = ForLoopStyle.FREETEXT;
		// START KGU#256 2016-09-25: Bugfix #252 - we will level all assignment symbols here
		//String thisText = this.getText().getLongString().trim();
		// START KGU#453 2017-11-02: Issue #447 - get rid of end-standing backslashes
		//String thisText = this.getText().getLongString().trim().replace(":=", "<-");
		String thisText = this.getUnbrokenText().getLongString().trim().replace(":=", "<-");
		// END KGU#453 2017-11-02
		// END KGU#256 2016-09-25
		//System.out.println(thisText + " <-> " + this.composeForClause() + " <-> " + this.composeForInClause());
		
		if (CodeParser.ignoreCase)
		{
			// START KGU#256 2016-09-25: Bugfix #252 - we will level all assignment symbols here
			//if (thisText.equalsIgnoreCase(this.composeForClause()) ||
			//		thisText.equalsIgnoreCase(this.composeForClause(true)))
			if (thisText.equalsIgnoreCase(this.composeForClause().replace(":=", "<-")) ||
					thisText.equalsIgnoreCase(this.composeForClause(true).replace(":=", "<-")))
			// END KGU#256 2016-09-25
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
			// START KGU#256 2016-09-25: Bugfix #252 - we will level all assignment symbols here
			//if (thisText.equals(this.composeForClause()) ||
			//		thisText.equals(this.composeForClause(true)))
			if (thisText.equals(this.composeForClause().replace(":=", "<-")) ||
					thisText.equals(this.composeForClause(true).replace(":=", "<-")))
			// END KGU#256 2016-09-25
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
	@Override
	public Element getLoop() {
		return this;
	}

	
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

	// START KGU#258 2016-09-26: Enh. #253
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRelevantParserKeys()
	 */
	@Override
	protected String[] getRelevantParserKeys() {
		switch (this.style)
		{
		case COUNTER:
			return relevantParserKeysCount;
		case TRAVERSAL:
			return relevantParserKeysTrav;
		default:
			{
				Vector<String> keys = new Vector<String>();
				for (String key: relevantParserKeysCount)
				{
					keys.add(key);
				}
				for (String key: relevantParserKeysTrav)
				{
					keys.add(key);
				}
				return keys.toArray(relevantParserKeysCount);
			}
		}
	}
	// END KGU#258 2016-09-26
	
	// START KGU#261 2017-01-26: Enh. #259 (type map)
	/**
	 * Adds own variable declarations (only this element, no substructure!) to the given
	 * map (varname -> typeinfo).
	 * @param typeMap
	 */
	@Override
	public void updateTypeMap(HashMap<String, TypeMapEntry> typeMap)
	{
		if (!this.isForInLoop()) {
			// This may be regarded as an explicit type declaration
			this.addToTypeMap(typeMap, this.getCounterVar(), "int", 0, true, true);
		}
		// START KGU#261 2017-04-14: Enh. #259 Try to make as much sense of the value list as possible
		else {
			StringList valueItems = this.getValueListItems();
			String typeSpec = "";
			if (valueItems != null) {
				// Try to identify the element type(s)
				for (int i = 0; !typeSpec.contains("???") && i < valueItems.count(); i++) {
					String itemType = identifyExprType(typeMap, valueItems.get(i), true);
					if (typeSpec.isEmpty()) {
						typeSpec = itemType;
					}
					else if (!itemType.isEmpty() && !typeSpec.equalsIgnoreCase(itemType)) {
						// START KGU#542 2019-11-21: Enh. #739 try a resolution if an enumerator type collides with int
						//typeSpec = TypeMapEntry.combineTypes(itemType, typeSpec, true);
						TypeMapEntry type1 = typeMap.get(":" + itemType);
						TypeMapEntry type2 = typeMap.get(":" + typeSpec);
						boolean isEnum1 = type1 != null && type1.isEnum();
						boolean isEnum2 = type2 != null && type2.isEnum();
						if (isEnum1 && typeSpec.equals("int")
								|| isEnum2 && itemType.equals("int")
								|| isEnum1 && isEnum2 && !type1.typeName.equals(type2.typeName)) {
							typeSpec = "int";
						}
						else {
							typeSpec = TypeMapEntry.combineTypes(itemType, typeSpec, true);
						}
						// END KGU#542 2019-11-21
					}
				}
				if (!typeSpec.isEmpty() && !typeSpec.equals("???")) {
					this.addToTypeMap(typeMap, this.getCounterVar(), typeSpec, 0, true, false);
				}
			}
			else {
				String valueListString = this.getValueList();
				if (valueListString != null) {
					// Try to derive the type from the expression
					typeSpec = identifyExprType(typeMap, valueListString, false);
					if (!typeSpec.isEmpty() && typeSpec.startsWith("@")) {
						// nibble one array level off as the loop variable is of the element type
						this.addToTypeMap(typeMap, this.getCounterVar(), typeSpec.substring(1), 0, true, false);
					}
				}
			}
		}
		// END KGU#261 2017-04-14
	}
	// END KGU#261 2017-01-26

	// START KGU#602 2018-10-25: Issue #419 - Mechanism to detect and handle long lines
	/**
	 * Detects the maximum text line length either on this very element 
	 * @param _includeSubstructure - whether (in case of a complex element) the substructure
	 * is to be involved
	 * @return the maximum line length
	 */
	public int getMaxLineLength(boolean _includeSubstructure)
	{
		int maxLen = super.getMaxLineLength(false);
		if (_includeSubstructure) {
			maxLen = Math.max(maxLen, this.q.getMaxLineLength(true));
		}
		return maxLen;
	}
	// END KGU#602 2018-10-25

}

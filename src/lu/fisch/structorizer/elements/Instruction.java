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
 *      Description:    This class represents an "instruction" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2015.10.11/13   Comment drawing unified, breakpoints supported, colouring modified
 *      Kay Gürtzig     2015.11.14      Bugfix #31 (= KGU#82) in method copy
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *      Kay Gürtzig     2016-01-03      Bugfix #87 (KGU#124) collapsing of larger instruction elements,
 *                                      Enh. #87 (KGU#122) marking of collapsed elements with icon
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136): fix accomplished
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Fields for test coverage tracking added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.06      Enh. #188: New classification methods isAssignment() etc.,
 *                                      new copy constructor to support conversion (KGU#199)
 *      Kay Gürtzig     2016.07.30      Enh. #128: New mode "comments plus text" supported
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.parsers.D7Parser;
//import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.*;

public class Instruction extends Element {
	
	public Instruction()
	{
		super();
	}
	
	public Instruction(String _strings)
	{
		super(_strings);
		setText(_strings);	// FIXME (KGU 2015-10-13): What is this good for? This has already been done by the super constructor!
	}
	
	public Instruction(StringList _strings)
	{
		super(_strings);
		setText(_strings);
	}
	
	// START KGU#199 2016-07-07: Enh. #188 - also serves subclasses for "up-casting"
	public Instruction(Instruction instr)
	{
		super(instr.text.copy());
		instr.copyDetails(this, true, true);
	}
	// END KGU#199 2016-07-07
	
	// START KGU#64 2015-11-03: Is to improve drawing performance
	/**
	 * Recursively clears all drawing info this subtree down
	 * (To be overridden by structured sub-classes!)
	 */
	@Override
	public void resetDrawingInfoDown()
	{
		this.resetDrawingInfo();
	}
	// END KGU#64 2015-11-03

	public static Rect prepareDraw(Canvas _canvas, StringList _text, Element _element)
	{
		Rect rect = new Rect(0, 0, 2*(Element.E_PADDING/2), 0);
		// START KGU#227 2016-07-30: Enh. #128
		int commentHeight = 0;
		// END KGU#227 2016-07-30

		FontMetrics fm = _canvas.getFontMetrics(Element.font);

		// START KGU#227 2016-07-30: Enh. #128
		if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed())
		{
			Rect commentRect = _element.writeOutCommentLines(_canvas,
					0, 0, false);
			rect.right = Math.max(rect.right, commentRect.right + Element.E_PADDING);
			commentHeight = commentRect.bottom;
		}
		// END KGU#227 2016-07-30
		
		for(int i=0;i<_text.count();i++)
		{
			int lineWidth = getWidthOutVariables(_canvas, _text.get(i), _element) + Element.E_PADDING;
			if (rect.right < lineWidth)
			{
				rect.right = lineWidth;
			}
		}
		rect.bottom = 2*(Element.E_PADDING/2) + _text.count() * fm.getHeight();
		// START KGU#227 2016-07-30: Enh. #128
		rect.bottom += commentHeight;
		// END KGU#227 2016-07-30

		return rect;
	}
        
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		// END KGU#136 2016-03-01

		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		
		// START KGU#124 2016-01-03: Large instructions should also be actually collapsed
        //rect = prepareDraw(_canvas, getText(false), this);
		StringList text = getText(false);
        if (isCollapsed() && text.count() > 2) 
        {
        	text = getCollapsedText();
        }
        rect0 = prepareDraw(_canvas, text, this);
        // END KGU#124 2016-01-03
        
		// START KGU#136 2016-03-01: Bugfix #97
		isRectUpToDate = true;
		// END KGU#136 2016-03-01
        return rect0;
	}

	public static void draw(Canvas _canvas, Rect _top_left, StringList _text, Element _element)
	{
		Rect myrect = new Rect();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = _element.getColor();
		Color drawColor = _element.getFillColor();
		// END KGU 2015-10-13
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
			
		// START KGU 2015-10-13: Became obsolete by new method getFillColor() applied above now
//		if (_element.isCollapsed())
//		{
//			drawColor=Element.E_COLLAPSEDCOLOR;
//		}
//		if (_element.selected==true)
//		{
//			drawColor=Element.E_DRAWCOLOR;
//		}
		// END KGU 2015-10-13
		
		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//_element.rect = _top_left.copy();
		_element.rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		Point ref = _element.getDrawPoint();
		_element.topLeft.x = _top_left.left - ref.x;
		_element.topLeft.y = _top_left.top - ref.y;
		// END KGU#136 2016-03-01
		
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		myrect = _top_left.copy();
		
		canvas.fillRect(myrect);
				
		// draw comment indicator
		if (Element.E_SHOWCOMMENTS && !_element.getComment(false).getText().trim().equals(""))
		{
			_element.drawCommentMark(canvas, myrect);
		}
		
		// START KGU 2015-10-11: If _element is a breakpoint, mark it
		_element.drawBreakpointMark(canvas, _top_left);
		// END KGU 2015-10-11
		
		// START KGU#227 2016-07-30: Enh. #128
		int commentHeight = 0;
		if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed())
		{
			Rect commentRect = _element.writeOutCommentLines(canvas,
					_top_left.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
					_top_left.top + (Element.E_PADDING / 2),
					true);
			commentHeight = commentRect.bottom - commentRect.top;
		}
		int yTextline = _top_left.top + (Element.E_PADDING / 2) + commentHeight/* + fm.getHeight()*/;
		// END KGU#227 2016-07-30
		
		for (int i = 0; i < _text.count(); i++)
		{
			String text = _text.get(i);
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
					_top_left.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
					// START KGU#227 2016-07-30: Enh. #128
					//_top_left.top + (Element.E_PADDING / 2) + (i+1)*fm.getHeight(),
					yTextline += fm.getHeight(),
					// END KGU#227 2016-07-30
					text,
					_element
					);  	

		}
		// END KGU#227 2016-07-30

		// START KGU#156 2016-03-11: Enh. #124
		// write the run-time info if enabled
		_element.writeOutRuntimeInfo(_canvas, _top_left.left + _element.rect.right - (Element.E_PADDING / 2), _top_left.top);
		// END KGU#156 2016-03-11
				
		canvas.setColor(Color.BLACK);
		if (_element.haveOuterRectDrawn())
		{
			canvas.drawRect(_top_left);
		}
		// START KGU#122 2016-01-03: Enh. #87 - A collapsed element is to be marked by the type-specific symbol,
		// unless it's an Instruction offspring in which case it will keep its original style, anyway.
		if (_element.isCollapsed() && !(_element instanceof Instruction))
		{
			canvas.draw(_element.getIcon().getImage(), _top_left.left, _top_left.top);
		}
		// END KGU#122 2016-01-03
	}
                
	public void draw(Canvas _canvas, Rect _top_left)
	{
		// Now delegates all stuff to the static method above, which may also
		// be called from Elements of different types when those are collapsed
		
		// START KGU#124 2016-01-03: Large instructions should also be actually collapsed
        //draw(_canvas, _top_left, getText(false), this);
        if (isCollapsed() && getText(false).count() > 2) 
        {
        	draw(_canvas, _top_left, getCollapsedText(), this);
        }
        else
        {
            draw(_canvas, _top_left, getText(false), this);
        }
        // END KGU#124 2016-01-03
	}
	
	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	public Element findSelected()
	{
		return selected ? this : null;
	}
	// END KGU#183 2016-04-24

	public Element copy()
	{
		Element ele = new Instruction(this.getText().copy());
		// START KGU#199 2016-07-06: Enh. #188 specific conversions enabled
		return copyDetails(ele, false, false);
	}
	
	// START KGU#225 2016-07-29: Bugfix #210 - argument added
	//protected Element copyDetails(Element _ele, boolean _forConversion)
	protected Element copyDetails(Element _ele, boolean _forConversion, boolean _simplyCoveredToo)
	// END KGU#225 2016-07-29
	{
		// END KGU#199 2016-07-06
		_ele.setComment(this.getComment().copy());
		_ele.setColor(this.getColor());
		// START KGU#82 (bug #31) 2015-11-14
		_ele.breakpoint = this.breakpoint;
		// END KGU#82 (bug #31) 2015-11-14
		// START KGU#117 2016-03-07: Enh. #77
        if (Element.E_COLLECTRUNTIMEDATA)
        {
        	// START KGU#225 2016-07-28: Bugfix #210
        	this.copyRuntimeData(_ele, _simplyCoveredToo);
        	// END KGU#225 2016-07-28
        	// We share this object (important for recursion!)
        	//_ele.deeplyCovered = this.deeplyCovered;
        	// START KGU#199 2016-07-06: Enh. #188
        	if (_forConversion)	// This distinction wasn't clear here: why?
        	{
            	// START KGU#225 2016-07-28: Bugfix #210 - some parts put to copyRuntimeData
        		//_ele.simplyCovered = this.simplyCovered;
        		//_ele.execCount = this.execCount;
            	// END KGU#225 2016-07-28
        		_ele.execStepCount = this.execStepCount;
        		_ele.execSubCount = this.execSubCount;
        	}
        	// END KGU#199 2016-07-06
        }
		// END KGU#117 2016-03-07
		// START KGU#183 2016-04-24: Issue #169
		_ele.selected = this.selected;
		// END KGU#183 2016-04-24
		return _ele;
	}

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
   		_lines.add(this.getText());
    }
    // END KGU 2015-10-16

	// START KGU#117 2016-03-10: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#checkTestCoverage(boolean)
	 */
	@Override
	public void checkTestCoverage(boolean _propagateUpwards)
	{
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			this.simplyCovered = true;
			this.deeplyCovered = true;
			if (_propagateUpwards)
			{
				Element parent = this.parent;
				while (parent != null)
				{
					parent.checkTestCoverage(false);
					parent = parent.parent;
				}
			}
		}
	}
	// END KGU#117 2016-03-10
	
	// START KGU#199 2016-07-06: Enh. #188 - new classication methods.
	// There is always a pair of a static and an instance method, the former for
	// a single line, the latter for the element as a whole.
	public static boolean isAssignment(String line)
	{
    	StringList tokens = Element.splitLexically(line, true);
    	unifyOperators(tokens, true);
		return tokens.contains("<-");
	}
	public boolean isAssignment()
	{
		return this.text.count() == 1 && Instruction.isAssignment(this.text.get(0));
	}
	
	public static boolean isJump(String line)
	{
    	StringList tokens = Element.splitLexically(line, true);
		return (tokens.indexOf(D7Parser.preReturn, !D7Parser.ignoreCase) == 0 ||
				tokens.indexOf(D7Parser.preLeave, !D7Parser.ignoreCase) == 0 ||
				tokens.indexOf(D7Parser.preExit, !D7Parser.ignoreCase) == 0
				);
	}
	public boolean isJump()
	{
		return this.text.count() == 0 || this.text.count() == 1 && Instruction.isJump(this.text.get(0));
	}
	
	public static boolean isProcedureCall(String line)
	{
		// Be aware that this method is also used for the isFunctionCall check
		Function fct = new Function(line);
		return fct.isFunction();
	}
	public boolean isProcedureCall()
	{
		return this.text.count() == 1 && Instruction.isProcedureCall(this.text.get(0));		
	}
	
	public static boolean isFunctionCall(String line)
	{
		boolean isFunc = false;
    	StringList tokens = Element.splitLexically(line, true);
    	unifyOperators(tokens, true);
		int asgnPos = tokens.indexOf("<-");
		if (asgnPos > 0)
		{
			// This looks somewhat misleading. But we do a mere syntax check
			isFunc = isProcedureCall(tokens.concatenate("", asgnPos+1));
		}
		return isFunc;
	}
	public boolean isFunctionCall()
	{
		return this.text.count() == 1 && Instruction.isFunctionCall(this.text.get(0));
	}
	// END KGU#199 2016-07-06

	// START KGU#178 2016-07-19: Support for enh. #160 (export of called subroutines)
	// (This method is plaed here instead of in class Call because it is needed
	// to decide whether an Instruction element complies to the Call syntax and
	// may be transmuted.)
	/**
	 * Returns a Function object describing the signature of the called routine
	 * if the text complies to the call syntax described in the user guide
	 * or null otherwise.
	 * @return Function object or null.
	 */
	public Function getCalledRoutine()
	{
		Function called = null;
		if (this.text.count() == 1)
		{
			String potentialCall = this.text.get(0);
			StringList tokens = Element.splitLexically(potentialCall, true);
			unifyOperators(tokens, true);
			int asgnPos = tokens.indexOf("<-");
			if (asgnPos > 0)
			{
				potentialCall = tokens.concatenate("", tokens.indexOf("<-")+1);		
			}
			called = new Function(potentialCall);
			if (!called.isFunction())
			{
				called = null;
			}
		}
		return called;
	}
	// END KGU#178 2016-07-19

	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	public boolean isCallOfOneOf(StringList _signatures)
	{
		Function fct = this.getCalledRoutine();
		return fct != null && _signatures.contains(fct.getName() + "#" + fct.paramCount());
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 * Doesn't do anything - it's the task of SubQueues
	 */
	@Override
	public void convertToCalls(StringList _signatures)
	{}
	// END KGU#199 2016-07-07
}

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
 *      Kay Gürtzig     2015.12.08      //Temporary modification in addFullText() as workaround for bug #46
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.FontMetrics;
import java.util.regex.Matcher;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.generators.Generator;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.*;

public class For extends Element implements ILoop {

	public Subqueue q = new Subqueue();
	
	private Rect r = new Rect();
	
	// START KGU#3 2015-10-24
	private static String forSeparatorPre = "§FOR§";
	private static String forSeparatorTo = "§TO§";
	private static String forSeparatorBy = "§BY§";
	// The following fields are dedicated for unambiguous semantics representation. If and only if the
	// structured information of these fields is consistent field isConsistent shall be true.
	private String counterVar = "";			// name of the counter variable
	private String startValue = "1";		// expression determining the start value of the loop
	private String endValue = "";			// expression determining the end value of the loop
	private int stepConst = 1;				// an integer value defining the increment/decrement
	public boolean isConsistent = false;	// flag determining whether the semantics is consistently defined by the dedicated fields
	// END KGU#3 2015-10-24

	public For()
	{
		super();
		q.parent=this;
	}

	public For(String _strings)
	{
		super(_strings);
		q.parent=this;
		setText(_strings);
	}
	
	public For(StringList _strings)
	{
		super(_strings);
		q.parent=this;
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
		this.q.resetDrawingInfoDown();
	}
	// END KGU#64 2015-11-03
	
	
	public Rect prepareDraw(Canvas _canvas)
	{
		if(isCollapsed()) 
		{
			rect = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
			return rect;
		}

        // START KGU 2015-12-02: Obviously redundant stuff merged        
		rect.top=0;
		rect.left=0;

		int padding = 2*(E_PADDING/2); 
		rect.right = padding;

		FontMetrics fm = _canvas.getFontMetrics(Element.font);

		for (int i=0; i<getText(false).count(); i++)
		{
			int lineWidth = getWidthOutVariables(_canvas, getText(false).get(i), this) + padding;
			if (rect.right < lineWidth)
			{
				rect.right = lineWidth;
			}
		}

		rect.bottom = padding + getText(false).count() * fm.getHeight();

		r = q.prepareDraw(_canvas);

		rect.right = Math.max(rect.right, r.right + E_PADDING);

		if(Element.E_DIN==false)
		{
//			rect.top=0;
//			rect.left=0;
//			
//			rect.right=2*Math.round(E_PADDING/2);
//			
//			FontMetrics fm = _canvas.getFontMetrics(Element.font);
//			
//			rect.right=Math.round(2*(Element.E_PADDING/2));
//			for(int i=0;i<getText(false).count();i++)
//			{
//				int lineWidth = getWidthOutVariables(_canvas,getText(false).get(i),this)+2*Math.round(E_PADDING/2);
//				if(rect.right < lineWidth)
//				{
//					rect.right = lineWidth;
//				}
//			}
//			
//			rect.bottom = 2 * (E_PADDING/2) + getText(false).count() * fm.getHeight();
//			
//			r=q.prepareDraw(_canvas);
//			
//			rect.right=Math.max(rect.right,r.right+E_PADDING);
			rect.bottom += r.bottom + E_PADDING;
//			return rect;
		}
		else
		{
//			rect.top=0;
//			rect.left=0;
//			
//			rect.right=2*Math.round(E_PADDING/2);
//			
//			FontMetrics fm = _canvas.getFontMetrics(font);
//			
//			rect.right=Math.round(2*(E_PADDING/2));
//			for(int i=0;i<getText(false).count();i++)
//			{
//				int lineWidth = getWidthOutVariables(_canvas,getText(false).get(i),this)+2*Math.round(E_PADDING/2);
//				if(rect.right < lineWidth)
//				{
//					rect.right = lineWidth;
//				}
//			}
//			
//			rect.bottom= 2 * (E_PADDING/2) + getText(false).count() * fm.getHeight();
//			
//			r=q.prepareDraw(_canvas);
//			
//			rect.right=Math.max(rect.right,r.right+E_PADDING);
			rect.bottom += r.bottom;		
//			return rect;
		}
		return rect;
		// END KGU 2015-12-02
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{

		if(isCollapsed()) 
		{
			Instruction.draw(_canvas, _top_left, getCollapsedText(), this);
			return;
		}

		// START KGU 2015-10-12: Common beginning of both styles
		Rect myrect = new Rect();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = getColor();
		Color drawColor = getFillColor();
		// END KGU 2015-10-13
		FontMetrics fm = _canvas.getFontMetrics(font);
//		int p;
//		int w;

		// START KGU 2015-10-13: Became obsolete by new method getFillColor() applied above now
//		if (selected==true)
//		{
//			if(waited==true) { drawColor=Element.E_WAITCOLOR; }
//			else { drawColor=Element.E_DRAWCOLOR; }
//		}
		// END KGU 2015-10-13

		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);


		int headerHeight = fm.getHeight() * getText(false).count() + 2 * (Element.E_PADDING / 2);

		if(Element.E_DIN==false)
		{

			// draw background
			myrect=_top_left.copy();
			canvas.fillRect(myrect);
			
			// draw shape
			rect=_top_left.copy();
			canvas.setColor(Color.BLACK);
			canvas.drawRect(_top_left);
			
			myrect=_top_left.copy();
			myrect.bottom=_top_left.top + headerHeight;
			canvas.drawRect(myrect);
			
			myrect.bottom=_top_left.bottom;
			myrect.top=myrect.bottom-E_PADDING;
			canvas.drawRect(myrect);
			
			myrect=_top_left.copy();
			myrect.right=myrect.left+E_PADDING;
			canvas.drawRect(myrect);
			
			// fill shape
			canvas.setColor(drawColor);
			myrect.left=myrect.left+1;
			myrect.top=myrect.top+1;
			myrect.bottom=myrect.bottom;
			myrect.right=myrect.right-1;
			canvas.fillRect(myrect);
			
			myrect=_top_left.copy();
			myrect.bottom=_top_left.top + headerHeight;
			myrect.left=myrect.left+1;
			myrect.top=myrect.top+1;
			myrect.bottom=myrect.bottom;
			myrect.right=myrect.right-1;
			canvas.fillRect(myrect);
			
			myrect.bottom=_top_left.bottom;
			myrect.top=myrect.bottom-Element.E_PADDING;
			myrect.left=myrect.left+1;
			myrect.top=myrect.top+1;
			myrect.bottom=myrect.bottom;
			myrect.right=myrect.right;
			canvas.fillRect(myrect);
	
		}
		else
		{
			
			rect=_top_left.copy();
			
			// draw shape
			myrect=_top_left.copy();
			canvas.setColor(Color.BLACK);
			myrect.bottom=_top_left.top + headerHeight;
			canvas.drawRect(myrect);
			
			myrect=_top_left.copy();
			myrect.right=myrect.left+Element.E_PADDING;
			canvas.drawRect(myrect);
			
			// fill shape
			canvas.setColor(drawColor);
			myrect.left=myrect.left+1;
			myrect.top=myrect.top+1;
			myrect.bottom=myrect.bottom;
			myrect.right=myrect.right;
			canvas.fillRect(myrect);
			
			myrect=_top_left.copy();
			myrect.bottom=_top_left.top + headerHeight;
			myrect.left=myrect.left+1;
			myrect.top=myrect.top+1;
			myrect.bottom=myrect.bottom;
			myrect.right=myrect.right;
			canvas.fillRect(myrect);
			
		}
		
		// START KGU 2015-10-12: D.R.Y. - common tail of both branches re-united here
		if(Element.E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
		{
			this.drawCommentMark(canvas, _top_left);
		}
		// draw breakpoint bar if necessary
		this.drawBreakpointMark(canvas, _top_left);

		// draw text
		for(int i=0;i<getText(false).count();i++)
		{
			String text = this.getText(false).get(i);
			text = BString.replace(text, "<--","<-");
			
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
							  _top_left.left + Math.round(E_PADDING / 2),
							  _top_left.top + Math.round(E_PADDING / 2) + (i+1)*fm.getHeight(),
							  text, this
							  );  	
		}
		
		// draw children
		myrect=_top_left.copy();
		myrect.left=myrect.left+Element.E_PADDING-1;
		myrect.top=_top_left.top + headerHeight-1;
		if (Element.E_DIN == false)
		{
			myrect.bottom=myrect.bottom-E_PADDING+1;
		}
		q.draw(_canvas,myrect);
		// END KGU 2015-10-12
	}
	
	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x, _y, _forSelection);
		Element sel = q.getElementByCoord(_x, _y, _forSelection);
		if(sel!=null) 
		{
			if (_forSelection) selected=false;
			selMe = sel;
		}
		
		return selMe;
	}

	public void setSelected(boolean _sel)
	{
		selected=_sel;
		//q.setSelected(_sel);
	}
	
	public Element copy()
	{
		For ele = new For(this.getText().copy());
		ele.setComment(this.getComment().copy());
		// START KGU#81 (bug #28) 2015-11-14: New fields must be copied, too!
		ele.counterVar = this.counterVar + "";
		ele.startValue = this.startValue + "";
		ele.endValue = this.endValue + "";
		ele.stepConst = this.stepConst;
		ele.isConsistent = this.isConsistent;
		// END KGU#81 (bug #28) 2015-11-14
		ele.setColor(this.getColor());
		ele.q=(Subqueue) this.q.copy();
		ele.q.parent=ele;
		// START KGU#82 (bug #31) 2015-11-14
		ele.breakpoint = this.breakpoint;
		// END KGU#82 (bug #31) 2015-11-14
		return ele;
	}
	
	
	// START KGU#43 2015-10-12
	@Override
	public void clearBreakpoints()
	{
		super.clearBreakpoints();
		this.q.clearBreakpoints();
	}
	// END KGU#43 2015-10-12

	// START KGU#43 2015-10-13
	@Override
	public void clearExecutionStatus()
	{
		super.clearExecutionStatus();
		this.q.clearExecutionStatus();
	}
	// END KGU#43 2015-10-13

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
		if (this.isConsistent)
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
		if (this.isConsistent)
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
		if (this.isConsistent)
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
		if (this.isConsistent)
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
		if (this.isConsistent)
		{
			return Integer.toString(this.stepConst);
		}
		else
		{
			return this.splitForClause()[3]; // Or should we provide this.splitClause()[4]?
		}
	}
	
    
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

	// START KGU#3 2015-11-04 We need a transformation to a common intermediate language
	private static String disambiguateForClause(String _text)
	{
		// Pad the string to ease the key word detection
		String interm = " " + _text + " ";

		// First collect the placemarkers of the for loop header ...
		String[] forMarkers = {D7Parser.preFor, D7Parser.postFor, D7Parser.stepFor};
		// ... and their replacements (in same order!)
		String[] forSeparators = {forSeparatorPre, forSeparatorTo, forSeparatorBy};

		// The configured markers for the For loop are not at all redundant but the only sensible
		// hint how to split the line into the counter variable, the initial and the final value (and possibly the step).
		// FIXME The composition of the regular expressions here conveys some risk since we may not know
		// what the user might have configured (see above)
		for (int i = 0; i < forMarkers.length; i++)
		{
			//String marker = forMarkers[i];
			String marker = Matcher.quoteReplacement(forMarkers[i]);
			String separator = forSeparators[i];
			if (!marker.isEmpty())
			{
				String pattern = "(.*?)" + marker + "(.*)";
				// If it is not padded, then ensure it is properly isolated
				if (marker.equals(marker.trim()))
				{
					pattern = "(.*?\\W)" + marker + "(\\W.*)";
				}
				interm = interm.replaceFirst(pattern, "$1 " + separator + " $2");
				// Eliminate possibly remaining occurrences if padded (preserve name substrings!)
				interm = interm.replaceAll(pattern, "$1 " + separator + " $2");
			}
			// eliminate multiple blanks
			interm = BString.replace(interm, "  ", " ");
		}

		return interm;

	}
	

	public String[] splitForClause()
	{
		return splitForClause(this.getText().getText());
	}
	
	/**
	 * Splits a potential FOR clause (after operator unification, see unifyOperators for details)
	 * into an array consisting of five strings meant to have following meaning:
	 * 1. counter variable name 
	 * 2. expression representing the initial value
	 * 3. expression representing the final value
	 * 4. Integer literal representing the increment value ("1" if the substring can't be parsed)
	 * 5. Substring for increment section as found on splitting (no integer coercion done)
	 * 
	 * @param _text the FOR clause to be split (something like "for i <- 1 to n")
	 * @return String array consisting of the four parts explained above
	 */
	public static String[] splitForClause(String _text)
	{
		String[] forParts = { "dummy_counter", "1", null, "1", ""};
		// Set some defaults
		String init = "";	// Initialisation instruction
		
		// Do some pre-processing to disambiguate the key words
		String _intermediate = disambiguateForClause(_text);		
		//System.out.println("Disambiguated For clause: \"" + _intermediate + "\"");
		
		_intermediate = _intermediate.replace('\n', ' '); // Concatenate the lines
		int posFor = _intermediate.indexOf(forSeparatorPre);
		int lenFor = forSeparatorPre.length();
		int posTo = _intermediate.indexOf(forSeparatorTo);
		int lenTo = forSeparatorTo.length();
		int posBy = _intermediate.indexOf(forSeparatorBy);
		int lenBy = forSeparatorBy.length();
		if (posFor < 0) { posFor = -lenFor; }	// Fictitious position such that posFor+lenFor becomes 0
		int posIni = posFor + lenFor;
		int[] positions = { posFor, posTo, posBy };
		int pastIni = _intermediate.length();
		int pastTo = pastIni, pastBy = pastIni;
		for (int i = 0; i < positions.length; i++) 
		{
			if (i > 0 && positions[i] >= posIni && positions[i] < pastIni) pastIni = positions[i];
			if (positions[i] >= posTo+lenTo && positions[i] < pastTo) pastTo = positions[i];
			if (positions[i] >= posBy+lenBy && positions[i] < pastBy) pastBy = positions[i];
		}
		//System.out.println("FOR section from " + posIni + " to " + pastIni + "...");
		init = _intermediate.substring(posIni, pastIni).trim();
		//System.out.println("FOR --> \"" + init + "\"");
		if (posTo >= 0)
		{
			//System.out.println("TO section from " + (posTo + lenTo) + " to " + pastTo + "...");
			forParts[2] = _intermediate.substring(posTo + lenTo, pastTo).trim();
			//System.out.println("TO --> \"" + forParts[2] + "\"");
		}
		if (posBy >= 0)
		{
			//System.out.println("BY section from " + (posBy + lenBy) + " to " + pastBy + "...");
			forParts[4] = _intermediate.substring(posBy + lenBy, pastBy).trim();
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
		init = unifyOperators(init);	// 
		String[] initParts = init.split(" <- ");
		if (initParts.length < 2)
		{
			forParts[1] = initParts[0].trim();
		}
		else
		{
			forParts[0] = initParts[0].trim();
			forParts[1] = initParts[1].trim();
		}
		return forParts;
	}
	
	public String composeForClause()
	{
		return composeForClause(this.counterVar, this.startValue, this.endValue, this.stepConst);
	}
	
	public static String composeForClause(String _counter, String _start, String _end, String _step)
	{
		int step = 1;
		try
		{
			step = Integer.valueOf(_step);
		}
		catch (Exception ex)
		{}
		return composeForClause(_counter, _start, _end, step);
	}
	
	public static String composeForClause(String _counter, String _start, String _end, int _step)
	{
		String asgnmtOpr = " <- ";	// default assignment operator
		// If the preset text prefers the Pascal assignment operator then we will use this instead
		if (Element.preFor.indexOf("<-") < 0 && Element.preFor.indexOf(":=") >= 0)
		{
			asgnmtOpr = " := ";
		}
		String forClause = D7Parser.preFor.trim() + " " + _counter + asgnmtOpr + _start + " " +
				D7Parser.postFor.trim() + " " + _end;
		if (_step != 1)
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
		return this.getText().getLongString().equals(this.composeForClause());
	}
	// END KGU#3 2015-11-04

	// START KGU 2015-11-30
	@Override
	public Subqueue getBody() {
		return this.q;
	}
	// END KGU 2015-11-30
}

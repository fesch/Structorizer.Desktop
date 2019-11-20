/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
 *      Author:         Kay Gürtzig
 *
 *      Description:    This class represents a "TRY CATCH block" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-03-15      First Issue (implementing an idea of Bob Fisch)
 *      Kay Gürtzig     2019-09-17      Bugfix #749: Width for FINALLY section wasn't properly reserved
 *      Kay Gürtzig     2019-09-24      Bugfix #749: Text content and width in collapsed mode fixed
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import lu.fisch.graphics.Canvas;
import lu.fisch.graphics.Rect;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.gui.FindAndReplace;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.StringList;

/**
 * Elemment class representing a try / catch / finally construct after a proposal by Bob Fisch (#56)
 * @author Kay Gürtzig
 */
public class Try extends Element {

	public Subqueue qTry = new Subqueue();
	public Subqueue qCatch = new Subqueue();
	public Subqueue qFinally = new Subqueue();
	
	private Rect r0Try = new Rect();
	private Rect r0Catch = new Rect();
	private Rect r0Finally = new Rect();
	
	/**
	 * Creates an empty 
	 */
	public Try() {
		super();
		qTry.parent = this;
		qCatch.parent = this;
		qFinally.parent = this;
	}

	/**
	 * Creates a new Try element with the given {@link String} as element text (should be the name of the exception variable)
	 * @param _string - element text (will be split by newlines)
	 */
	public Try(String _string) {
		super(_string);
		qTry.parent = this;
		qCatch.parent = this;
		qFinally.parent = this;
	}

	/**
	 * Creates a new Try element with the given {@link StringList} as element text (should be the name of the exception variable)
	 * @param _strings - element text
	 */
	public Try(StringList _strings) {
		super(_strings);
		qTry.parent = this;
		qCatch.parent = this;
		qFinally.parent = this;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#prepareDraw(lu.fisch.graphics.Canvas)
	 */
	@Override
	public Rect prepareDraw(Canvas _canvas) {
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRect0UpToDate) return rect0;
		// END KGU#136 2016-03-01

		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		if(isCollapsed(true)) 
		{
			StringList collapsedText = getCollapsedText();
			rect0 = Instruction.prepareDraw(_canvas, collapsedText, this);
			// START KGU#136 2016-03-01: Bugfix #97
			isRect0UpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}
		
		int fontHeight = getFontHeight(_canvas.getFontMetrics(Element.font));
		
		Rect rTry = qTry.prepareDraw(_canvas).copy();
		Rect rCatch = qCatch.prepareDraw(_canvas).copy();
		Rect rFinally = qFinally.prepareDraw(_canvas).copy();
		Rect rTop = Instruction.prepareDraw(_canvas, StringList.getNew(preTry), this);
		rTry.left = E_PADDING; rTry.right += E_PADDING;
		rCatch.left = 2 * E_PADDING; rCatch.right += 2 * E_PADDING;
		rFinally.left = E_PADDING; rFinally.right += E_PADDING;

		StringList textCatch = this.getCuteText(false);
		String lineCatch0 = "";
		if (!textCatch.isEmpty()) {
			lineCatch0 = textCatch.get(0);
		}
		int[] widths = {
				rTop.right,
				rTry.right + E_PADDING,
				E_PADDING/2 + getWidthOutVariables(_canvas, preCatch + " " + lineCatch0, this) + E_PADDING,
				rCatch.right + E_PADDING,
				E_PADDING/2 + getWidthOutVariables(_canvas, preFinally, this) + E_PADDING,
				// START KGU#728 2019-09-17: Bugfix #749
				//rFinally.right
				rFinally.right + E_PADDING
				// END KGU#728 2019-09-17
				};
		int width = 0;
		for (int i = 0; i < widths.length; i++) {
			if (widths[i] > width) width = widths[i];
		}
		for (int i = 1; i < textCatch.count(); i++) {
			width = Math.max(width, E_PADDING + E_PADDING/2 + getWidthOutVariables(_canvas, textCatch.get(i), this)) + E_PADDING;
		}
		//int height = rTop.bottom + rTry.bottom + 2 * (E_PADDING/2) + fontHeight + rCatch.bottom + rFinally.bottom + E_PADDING;
		int height = rTop.bottom;
		rTry.top = height; rTry.bottom += height;
		
		height = rTry.bottom + 2 * (E_PADDING/2) + fontHeight;
		
		rCatch.top = height; rCatch.bottom += height;
		if (textCatch.count() > 1) {
			height += (textCatch.count() - 1) * fontHeight;
		}
		height = Math.max(height, rCatch.bottom) + E_PADDING;
		if (!preFinally.trim().isEmpty()) {
			height += fontHeight;
		}
		rFinally.top = height; rFinally.bottom += height;
		height = rFinally.bottom + E_PADDING;
		r0Try = rTry;
		r0Catch = rCatch;
		r0Finally = rFinally;
		rect0 = new Rect(0, 0, width, height);
		isRect0UpToDate = true;
		return rect0;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#draw(lu.fisch.graphics.Canvas, lu.fisch.graphics.Rect, java.awt.Rectangle, boolean)
	 */
	@Override
	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention) {
		// START KGU#502/KGU#524/KGU#553: Issues #518, #544, #557 New approach to reduce drawing contention
		if (!checkVisibility(_viewport, _top_left)) { return; }
		// END KGU#502/KGU#524/KGU#553
		
		if (isCollapsed(true)) 
		{
			StringList collapsedText = getCollapsedText();
			Instruction.draw(_canvas, _top_left, collapsedText, this, _inContention);
			wasDrawn = true;
			return;
		}
		
		Instruction.draw(_canvas, _top_left, StringList.getNew(preTry), this, _inContention);

		int fontHeight = getFontHeight(_canvas.getFontMetrics(Element.font));
		
		Rect subRect = new Rect(_top_left.left + r0Try.left, _top_left.top + r0Try.top, _top_left.right - E_PADDING, _top_left.top + r0Try.bottom);
		qTry.draw(_canvas, subRect, _viewport, _inContention);
		StringList textCatch = this.getCuteText(false);
		String lineCatch0 = "";
		if (!textCatch.isEmpty()) {
			lineCatch0 = textCatch.get(0);
		}
		writeOutVariables(_canvas, _top_left.left + E_PADDING/2, subRect.bottom + E_PADDING/2 + fontHeight,
				(preCatch + " " + lineCatch0).trim(), this, _inContention);
		for (int i = 1; i < textCatch.count(); i++) {
			writeOutVariables(_canvas, _top_left.left + E_PADDING + E_PADDING/2, subRect.bottom + E_PADDING/2 + i * fontHeight,
					textCatch.get(i), this, _inContention);			
		}
		subRect = new Rect(_top_left.left + r0Catch.left, _top_left.top + r0Catch.top, _top_left.right - E_PADDING, _top_left.top + r0Catch.bottom);		
		qCatch.draw(_canvas, subRect, _viewport, _inContention);
		if (!preFinally.trim().isEmpty()) {
			writeOutVariables(_canvas, _top_left.left + E_PADDING/2, subRect.bottom + E_PADDING/2 + fontHeight,
					preFinally, this, _inContention);
		}
		subRect = new Rect(_top_left.left + r0Finally.left, _top_left.top + r0Finally.top, _top_left.right - E_PADDING, _top_left.top + r0Finally.bottom);
		qFinally.draw(_canvas, subRect, _viewport, _inContention);
		
		_canvas.setColor(Color.BLACK);	// With an empty text, the decoration often was invisible.
		_canvas.moveTo(_top_left.left + (E_PADDING / 2), _top_left.top + r0Catch.top);
		_canvas.lineTo(_top_left.left + E_PADDING + E_PADDING/2, _top_left.top + r0Catch.bottom + ((r0Catch.top-r0Catch.bottom) / 2));
		_canvas.lineTo(_top_left.left + (E_PADDING / 2), _top_left.top + r0Catch.bottom);

		rect = new Rect(0, 0, _top_left.right - _top_left.left,  _top_left.bottom - _top_left.top);
		wasDrawn = true;
	}

	// START #728 2019-09-24: Bugfix #749
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getCollapsedText()
	 */
	@Override
	public StringList getCollapsedText()
	{
		StringList collapsedText = null;
		String prefix = preTry;
		if (qTry.getSize() > 0) {
			collapsedText = qTry.getElement(0).getCollapsedText();
		}
		if (collapsedText == null || collapsedText.isEmpty()) {
			collapsedText = super.getCollapsedText();
			if (!collapsedText.isEmpty()) {
				prefix = preCatch;
			}
		}
		if (collapsedText.isEmpty()) {
			collapsedText.add(prefix);
		}
		else {
			collapsedText.set(0, prefix + " " + collapsedText.get(0));
		}
		return collapsedText;
	}
	// END KGU#728 2019-09-24
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getIcon()
	 */
	@Override
	public ImageIcon getIcon()
	{
		return IconLoader.getIcon(120);
	}
	
	/**
	 * @return the (somewhat smaller) element-type-specific icon image intended to be used in
	 * the {@link FindAndReplace} dialog.
	 * @see #getIcon()
	 */
	@Override
	public ImageIcon getMiniIcon()
	{
		return IconLoader.getIcon(121);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#copy()
	 */
	@Override
	public lu.fisch.structorizer.elements.Element copy() {
		Try ele = new Try(this.getText().copy());
		copyDetails(ele, true);
		ele.qTry     = (Subqueue)this.qTry.copy();
		ele.qCatch   = (Subqueue)this.qCatch.copy();
		ele.qFinally = (Subqueue)this.qFinally.copy();
		ele.qTry.parent     = ele;
		ele.qCatch.parent   = ele;
		ele.qFinally.parent = ele;
		return ele;
	}

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
			isEqual = this.qTry.equals(((Try)_another).qTry) &&
					this.qCatch.equals(((Try)_another).qCatch) &&
					this.qFinally.equals(((Try)_another).qFinally);
		}
		return isEqual;
	}

	@Override
	public void toggleBreakpoint()
	{
		// Try itself may never have a breakpoint!
		breakpoint = false;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#combineCoverage(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	public boolean combineRuntimeData(Element _cloneOfMine)
	{
		boolean isEqual = super.combineRuntimeData(_cloneOfMine);
		if (isEqual)
		{
			isEqual = this.qTry.combineRuntimeData(((Try)_cloneOfMine).qTry) &&
					this.qCatch.combineRuntimeData(((Try)_cloneOfMine).qCatch) &&
					this.qFinally.combineRuntimeData(((Try)_cloneOfMine).qFinally);			
		}
		return isEqual;
	}
	
	@Override
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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isTestCovered(boolean)
	 */
	public boolean isTestCovered(boolean _deeply)
	{
		return this.qTry.isTestCovered(_deeply) && this.qCatch.isTestCovered(_deeply) && this.qFinally.isTestCovered(_deeply);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#traverse(lu.fisch.structorizer.elements.IElementVisitor)
	 */
	@Override
	public boolean traverse(IElementVisitor _visitor) {
		boolean proceed = _visitor.visitPreOrder(this);
		if (proceed)
		{
			proceed = qTry.traverse(_visitor) && qCatch.traverse(_visitor) && qFinally.traverse(_visitor);
		}
		if (proceed)
		{
			proceed = _visitor.visitPostOrder(this);
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	@Override
	public lu.fisch.structorizer.elements.Element findSelected() {
		Element sel = selected ? this : null;
		if (sel == null && (sel = qTry.findSelected()) == null && (sel = qCatch.findSelected()) == null)
		{
			sel = qFinally.findSelected();
		}
		return sel;
	}

	// KGU 2015-10-09: On moving the cursor, substructures had been eclipsed
	// by their containing box w.r.t. comment popping etc. This correction, however,
	// might significantly slow down the mouse tracking on enabled comment popping.
	// Just give it a try... 
	//public Element selectElementByCoord(int _x, int _y)
	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x, _y, _forSelection);
		if ((selMe != null || _forSelection) && !this.isCollapsed(true))
		{
			// Bugfix #97 - we use local coordinates now
			Element selTry = qTry.getElementByCoord(_x - r0Try.left, _y - r0Try.top, _forSelection);
			Element selCat = qCatch.getElementByCoord(_x - r0Catch.left, _y - r0Catch.top, _forSelection);
			Element selFin = qFinally.getElementByCoord(_x - r0Finally.left, _y - r0Finally.top, _forSelection);
			if (selTry != null) 
			{
				if (_forSelection) selected = false;
				selMe = selTry;
			}
			else if (selCat != null)
			{
				if (_forSelection) selected = false;
				selMe = selCat;
			}
			else if (selFin != null)
			{
				if (_forSelection) selected = false;
				selMe = selFin;
			}
		}

		return selMe;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 */
	@Override
	public void convertToCalls(StringList _signatures) {
		this.qTry.convertToCalls(_signatures);
		this.qCatch.convertToCalls(_signatures);
		this.qFinally.convertToCalls(_signatures);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
	protected void addFullText(StringList _lines, boolean _instructionsOnly) {
		if (!this.isDisabled()) {
			this.qTry.addFullText(_lines, _instructionsOnly);
			// FIXME the contents of the catch block may not be wanted
			if (!_instructionsOnly) {
				this.qCatch.addFullText(_lines, _instructionsOnly);
			}
			this.qFinally.addFullText(_lines, _instructionsOnly);
		}
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRelevantParserKeys()
	 */
	@Override
	protected String[] getRelevantParserKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#mayPassControl()
	 */
	public boolean mayPassControl()
	{
		// An alternative may only pass control if being disabled or containing at least one
		// passable branch. We don't check whether the condition is satisfiable.
		return disabled || this.qTry.mayPassControl() && this.qFinally.mayPassControl();
	}

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
			maxLen = Math.max(maxLen, this.qTry.getMaxLineLength(true));
			maxLen = Math.max(maxLen, this.qCatch.getMaxLineLength(true));
			maxLen = Math.max(maxLen, this.qFinally.getMaxLineLength(true));
		}
		return maxLen;
	}
	
	/**
	 * Adds own variable declarations (only this element, no substructure!) to the given
	 * map (varname -> typeinfo).
	 * @param typeMap
	 */
	@Override
	public void updateTypeMap(HashMap<String, TypeMapEntry> typeMap)
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//for (int i = 0; i < this.getText().count(); i++) {
		//	updateTypeMapFromLine(typeMap, this.getText().get(i), i);
		//}
		String excName = this.getExceptionVarName();
		if (excName != null) {
			addToTypeMap(typeMap, excName, "Exception", 0, true, true);
		}
	}
	
	@Override
	protected Set<String> getVariableSetFor(Element _child) {
		Set<String> varNames;
		if (this.parent == null) {
			varNames = new HashSet<String>();
		}
		else {
			varNames = this.parent.getVariableSetFor(this);
		}
		if (_child == this || _child == this.qCatch) {
			varNames.add(this.getExceptionVarName());
		}
		return varNames;
	}
	
	/**
	 * @return the most likely variable name extracted from the text (assuming it
	 * to be either a pure name or a declaration).
	 */
	public String getExceptionVarName()
	{
		return Instruction.getAssignedVarname(
				Element.splitLexically(this.getUnbrokenText().getLongString(), true)
				);
	}

}

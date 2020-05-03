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

package lu.fisch.structorizer.gui;

/*
 ******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This class is a kind of proxy for a selected subsequence of a Subqueue.
 *                      It meets some editing purposes and contains mere references.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Kay Gürtzig     2015.11.23      First issue (KGU#87).
 *      Kay Gürtzig     2016.01.22      Bugfix #114 for Enh. #38 (addressing moveUp/moveDown, KGU#143 + KGU#144).
 *      Kay Gürtzig     2016.03.01/02   Bugfix #79 (KGU#136) for reliable selection.
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.06      Bugfix in method removeElement() for enh. #188 (element conversion)
 *      Kay Gürtzig     2016.07.21      Bugfix #197 (selection moves by cursor keys); KGU#207 (getElementByCoord() revised)
 *      Kay Gürtzig     2016.10.13      Enh. #277: Method setDisabled(boolean) implemented
 *      Kay Gürtzig     2016.11.17      Bugfix #114: isExecuted() revised (signatures too)
 *      Kay Gürtzig     2017.03.26      Enh. #380: Methods addElement() and insertElementAt() now substantially implemented
 *      Kay Gürtzig     2018.10.26      Enh. #619: New method getMaxLineLength(boolean) implemented
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2020-05-02      Issue #866: Additional fields and methods to support revised selection expansion
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************
 */

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;

import lu.fisch.graphics.Canvas;
import lu.fisch.graphics.Rect;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.IElementSequence;
import lu.fisch.structorizer.elements.IElementVisitor;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 * Class represents a selected subsequence of a {@link Subqueue}
 */
public class SelectedSequence extends Element implements IElementSequence {

	// Positions of the first and last selected element within the parent Subqueue
	private int firstIndex = 0, lastIndex = -1;
	// START KGU#866 2020-05-02: Issue #866 New fields to support revised expansion strategy
	// Selection seed index offset w.r.t. firstIndex
	private int anchorOffset = 0;
	// Where the most recent expansion had taken place w.r.t. the anchorIndex
	private boolean lastActionBelow = true;
	// END KGU#866 2020-05-02
	
	// START KGU#136 2016-03-01: Bugfix #97 We must retain the base y coordinate to avoid flickering
	private Vector<Integer> y0Children = new Vector<Integer>();
	// END KGU#136 2016-03-01

	/**
	 * If this is used to replace (expand/reduce) a previous selection then the old
	 * selection must be {@code _child1}.
	 * @param _child1 - a selected {@link Element}, marking one end of the sequence,
	 * may be a {@link SelectedSequence}
	 * @param _child2 - a selected {@link Element}, marking the other end of  sequence,
	 * must not be a {@link SelectedSequence}
	 */
	public SelectedSequence(Element _child1, Element _child2)
	{
		super("");
		// _child1 and _child2 should have got a common parent (in particular, a Subqueue)
		this.parent = _child1.parent;
		assert parent == _child2.parent;
		
		int i1 = ((Subqueue)this.parent).getIndexOf(_child1);
		int i2 = ((Subqueue)this.parent).getIndexOf(_child2);
		if (i1 < 0)	{
			if (_child1 instanceof SelectedSequence) {
				// START KGU#866 2020-05-02: Issue #866 - improved selection expansion / reduction
				//this.firstIndex = Math.min(((SelectedSequence)_child1).firstIndex, i2);
				//this.lastIndex = Math.max(((SelectedSequence)_child1).lastIndex, i2);
				SelectedSequence sel = (SelectedSequence)_child1;
				int ixAnchor = sel.firstIndex + sel.anchorOffset;
				if (i2 < ixAnchor || i2 == ixAnchor && !sel.lastActionBelow) {
					this.firstIndex = i2;
					this.lastIndex = sel.lastIndex;
					this.lastActionBelow = false;
				}
				else {
					// i2 > ixAnchor || i2 == ixAnchor && sel.lastActionBelow
					this.firstIndex = sel.firstIndex;
					this.lastIndex = i2;
				}
				this.anchorOffset = ixAnchor - this.firstIndex;
				// END KGU#866 2020-05-02
			}
			else if (i2 >= 0)
			{
				this.firstIndex = this.lastIndex = i2;
			}
		}
		else if (i2 < 0) {
			this.firstIndex = this.lastIndex = i1;
		}
		else if (i1 <= i2) {
			this.firstIndex = i1;
			this.lastIndex = i2;
		}
		else {
			this.firstIndex = i2;
			this.lastIndex = i1;
			// START KGU#866 2020-05-02: Issue #866 - improved selection expansion / reduction
			this.anchorOffset = i1 - i2;
			this.lastActionBelow = false;
			// END KGU#866 2020-05-02
		}
	}

	/**
	 * Establishes new selection span with respect to the given {@code _owner}
	 * @param _owner - {@link Subqueue} this is to represent a subsequence of
	 * @param _index1 - Index of the first Element of the sequence within {@code _owner}
	 * @param _index2 - Index of the last Element of the sequence within {@code _owner}
	 */
	public SelectedSequence(Subqueue _owner, int _index1, int _index2) {
		this(_owner, _index1, _index2, 0, true);
	}

	/**
	 * Establishes new selection span with respect to the given {@code _owner}, where
	 * element at {@code _index0} is retained as selection anchor for expansion/reduction.
	 * @param _owner - {@link Subqueue} this is to represent a subsequence of
	 * @param _index1 - Index of the Element marking the begin of the sequence
	 * @param _index2 - Index of the Element marking the end of the sequence
	 * @param _offset0 - Index offset of the selection anchor (w.r.t. {@code _index0} for expansion or reduction)
	 * @param _modifiedBelow - true if last expanded or reduced at or below anchor (false otherwise)
	 */
	public SelectedSequence(Subqueue _owner, int _index1, int _index2, int _offset0, boolean _modifiedBelow) {
		super("");
		this.parent = _owner;
		this.firstIndex = Math.max(0, _index1);
		this.lastIndex = Math.min(_owner.getSize()-1, _index2);
		this.anchorOffset = Math.max(0, Math.min(this.lastIndex - this.firstIndex, _offset0));
		this.lastActionBelow = _modifiedBelow;
	}

	/**
	 * @param _strings
	 */
	public SelectedSequence(String _string) {
		super(_string);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param _strings
	 */
	public SelectedSequence(StringList _strings) {
		super(_strings);
		// TODO Auto-generated constructor stub
	}

//	/* (non-Javadoc)
//	 * @see lu.fisch.structorizer.elements.Element#resetDrawingInfoDown()
//	 */
//	@Override
//	public void resetDrawingInfoDown() {
//		this.isRectUpToDate = false;
//	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#prepareDraw(lu.fisch.graphics.Canvas)
	 */
	@Override
	public Rect prepareDraw(Canvas _canvas) {
		// START KGU#136 2016-03-01: Bugfix #97
		if (this.isRect0UpToDate) return rect0;
		// END KGU#136 2016-03-01
		
		rect0.left = rect0.right = rect0.top = rect0.bottom = 0;
		Rect subrect = new Rect(0, 0, 0, 0);
		this.y0Children.clear();
		
		if (firstIndex <= lastIndex) 
		{
			for(int i = firstIndex; i <= lastIndex ;i++)
			{
				y0Children.addElement(rect0.bottom);
				subrect = ((Subqueue)parent).getElement(i).prepareDraw(_canvas);
				rect0.right = Math.max(rect0.right, subrect.right);
				rect0.bottom += subrect.bottom;
			}
		}
		else
		{
			rect0.right = 2 * Element.E_PADDING;
			FontMetrics fm = _canvas.getFontMetrics(Element.font);
			rect0.bottom = fm.getHeight() + 2* (Element.E_PADDING/2);
		}
		
		// START KGU#136 2016-03-01: Bugfix #97
		isRect0UpToDate = true;
		// END KGU#136 2016-03-01
		return rect0;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#draw(lu.fisch.graphics.Canvas, lu.fisch.graphics.Rect)
	 */
	@Override
	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention) {
		// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
		if (!checkVisibility(_viewport, _top_left)) { return; }
		// END KGU#502/KGU#524/KGU#553 2019-03-13
		Rect myrect;
		Rect subrect;
		Color drawColor = getFillColor();
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		Canvas canvas = _canvas;		
		
		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		this.topLeft.x = _top_left.left;
		this.topLeft.y = _top_left.top;
		// END KGU#136 2016-03-01
				
		myrect = _top_left.copy();
		myrect.bottom = myrect.top;
		
		if (firstIndex <= lastIndex)
		{
			// draw children
			for(int i = firstIndex; i <= lastIndex; i++)
			{
				subrect = ((Subqueue) parent).getElement(i).prepareDraw(_canvas);
				myrect.bottom += subrect.bottom;
				if (i == lastIndex)
				{
					myrect.bottom = _top_left.bottom;
				}
				((Subqueue) parent).getElement(i).draw(_canvas, myrect, null, _inContention);

				//myrect.bottom-=1;
				myrect.top += subrect.bottom;
			}
		}
		else
		{
			// draw empty set symbol
			
			canvas.setBackground(drawColor);
			canvas.setColor(drawColor);
			
			myrect = _top_left.copy();
			
			canvas.fillRect(myrect);
			
			canvas.setColor(Color.BLACK);
			canvas.writeOut(_top_left.left+((_top_left.right-_top_left.left) / 2) - (_canvas.stringWidth("\u2205") / 2),
							_top_left.top +((_top_left.bottom-_top_left.top) / 2) + (fm.getHeight() / 2),
							"\u2205"
							);  	

			canvas.drawRect(_top_left);
		}
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
		wasDrawn = true;
		// END KGU#502/KGU#524/KGU#553 2019-03-14
	}

	// START KGU#206 2016-07-21: Bugfix #197 for enh. #158 (cursor move didn't work due to wrong coordinates)
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRect()
	 */
	@Override
	public Rect getRect()
	{
		Rect first = this.getElement(0).getRect();
		int extraHeight = 0;
		for (int i = 1; i < this.getSize(); i++)
		{
			Rect next = this.getElement(i).getRect();
			extraHeight += next.bottom - next.top;
		}
		return new Rect(first.left, first.top, first.right, first.bottom + extraHeight);
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRect(java.awt.Point)
	 */
	@Override
	public Rect getRect(Point relativeTo)
	{
		Rect combined = getRect();
		return new Rect(combined.left + relativeTo.x, combined.top + relativeTo.y,
				combined.right + relativeTo.x, combined.bottom + relativeTo.y);		
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRectOffDrawPoint()
	 */
	@Override
	public Rect getRectOffDrawPoint()
	{
		// First find out the topLeft coordinate of the first element
		Element elem0 = this.getElement(0);
		Rect rect0DP = elem0.getRectOffDrawPoint();
		Rect rect0 = elem0.getRect();
		// Now compute the rectangle
		return getRect(new Point(rect0DP.left - rect0.left, rect0DP.top - rect0.top));
	}
	// END KGU#206 2016-07-21

	/**
	 * The copy will be related to a new {@link Subqueue} only consisting of copies of
	 * my referenced elements
	 * @see lu.fisch.structorizer.elements.Element#copy()
	 */
	@Override
	public Element copy() {
		Subqueue newParent = new Subqueue();
		newParent.parent = parent.parent;	// Is this sensible?
		for (int i = 0; i < getSize(); i++)
		{
			newParent.addElement(getElement(i).copy());
		}
		return new SelectedSequence(newParent, 0, getSize());
	}

	@Override
	public int getSize()
	{
		return lastIndex >= 0 ? lastIndex - firstIndex + 1 : 0;
	}
	
	@Override
	public int getIndexOf(Element _ele)
	{
		int index = ((Subqueue)parent).getIndexOf(_ele);
		if (index >= firstIndex && index <= lastIndex)
		{
			index -= firstIndex;
		}
		else
		{
			index = -1;
		}
		return index;
	}
	
	@Override
	public Element getElement(int _index)
	{
		Element ele = null;
		if (_index >= 0 && _index <= lastIndex - firstIndex)
		{
			ele = ((Subqueue)parent).getElement(_index + firstIndex); 
		}
		return ele;
	}
	
	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		// START KGU#207 2016-07-21: Method will hardly ever be used. But if so then it should at least work
		if (!this.isRect0UpToDate)
		{
			rect = this.getRect();
			this.isRect0UpToDate = true;
		}
		// END KGU#207 2016-07-21
		Element res = super.getElementByCoord(_x, _y, _forSelection);		
		Element sel = null;
		for (int i = firstIndex; i <= lastIndex; i++)
		{
			sel = ((Element)((Subqueue)parent).getElement(i)).getElementByCoord(_x, _y, _forSelection);
			if (sel != null)
			{
				if (_forSelection) selected = false;
				res = sel;
			}
		}
		//System.out.println(this + ".getElementByCoord("+_x + ", " + _y + ") returning " + res);
		return res;
	}
	
	/**
	 * Removes the given element from its owner and drops the virtual reference here
	 * (if it was referenced here)
	 * @param _element - Element t be removed
	 */
	@Override
	public void removeElement(Element _element)
	{
		// Is _element within my range?
		int index = getIndexOf(_element);
		if (index >= 0)	// Yes, so drop it
		{
			((Subqueue)parent).removeElement(_element);
			lastIndex--;
			// START KGU#136 2016-03-01: Bugfix #97
			this.resetDrawingInfo();
			// END KGU#136 2016-03-01
		}
	}
	
	/**
	 * Removes the element referenced by _index from its owning Subqueue and
	 * drops the virtual reference here.
	 * @param _index
	 */
	@Override
	public void removeElement(int _index)
	{
		// START KGU#199 2016-07-07: Bugfix (on occasion of Enh. #188)
		//if (_index >= 0 && _index < lastIndex - firstIndex)
		if (_index >= 0 && _index <= lastIndex - firstIndex)
		// END KGU#199 2016-07-07
		{
			((Subqueue)parent).removeElement(_index + firstIndex);
			lastIndex--;
			// START KGU#136 2016-03-01: Bugfix #97
			this.resetDrawingInfo();
			// END KGU#136 2016-03-01
		}
	}
	
	// START KGU#143 2016-01-22: Bugfix #114 - we need a method to decide execution involvement
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isExecuted()
	 */
	@Override
	public boolean isExecuted(boolean ignored)
	{
		// START KGU#143 2016-11-17: Bugfix #114 - We must assume involvement if parent is in waited element
		boolean involved = false;
		for (int index = this.firstIndex; !involved && index <= this.lastIndex; index++)
		{
			if (((Subqueue)this.parent).getElement(index).isExecuted())
			{
				involved = true;
			}
		}
		return involved;
	}
	// END KGU#143 2016-01-22

	// START KGU#144 2016-01-22: Bugfix #38, #114 - moveDown and moveUp hadn't been implemented
	@Override
	public boolean canMoveDown()
	{
		boolean canMove = this.lastIndex + 1 < ((Subqueue)this.parent).getSize();
		// None of the affected elements must be under execution! (Issue #114)
		for (int index = this.firstIndex; canMove && index <= this.lastIndex + 1; index++)
		{
			if (((Subqueue)this.parent).getElement(index).executed)
			{
				canMove = false;
			}
		}
		return canMove;
	}
	
	@Override
	public boolean canMoveUp()
	{
		boolean canMove = this.firstIndex > 0;
		// None of the affected elements must be under execution! (Issue #114)
		for (int index = this.firstIndex - 1; canMove && index <= this.lastIndex; index++)
		{
			if (((Subqueue)this.parent).getElement(index).executed)
			{
				canMove = false;
			}
		}
		return canMove;
	}
	
	/**
	 * Moves all {@link Element}s representing this selection one position downwards
	 * within the owner {@link Subqueue} if possible
	 * @return true if the selected {@link Element}s could be moved, false otherwise
	 * @see #canMoveDown()
	 */
	public boolean moveDown()
	{
		boolean feasible = this.canMoveDown();
		if (feasible) {
			// START KGU#136 2016-03-02: Bugfix #97
			//Element successor = ((Subqueue)this.parent).getElement(this.lastIndex + 1);
			//((Subqueue)this.parent).removeElement(this.lastIndex + 1);
			//((Subqueue)this.parent).insertElementAt(successor, this.firstIndex++);
			//this.lastIndex++;
			((Subqueue)this.parent).moveElement(++this.lastIndex, this.firstIndex++);
			this.resetDrawingInfo();
			// END KGU#136 2016-03-02

		}
		return feasible;
	}

	/**
	 * Moves all {@link Element}s representing this selection one position upwards
	 * within the owner {@link Subqueue} if possible
	 * @return true if the selected {@link Element}s could be moved, false otherwise
	 * @see #canMoveUp()
	 */
	public boolean moveUp()
	{
		boolean feasible = this.canMoveUp();
		if (feasible) {
			// START KGU#136 2016-03-02: Bugfix #97
			//Element predecessor = ((Subqueue)this.parent).getElement(this.firstIndex - 1);
			//((Subqueue)this.parent).removeElement(this.firstIndex - 1);
			//this.firstIndex--;
			//((Subqueue)this.parent).insertElementAt(predecessor, this.lastIndex--);
			((Subqueue)this.parent).moveElement(--this.firstIndex, this.lastIndex--);
			this.resetDrawingInfo();
			// END KGU#136 2016-03-02
		}
		return feasible;
	}
	// END KGU#144 2016-01-22
	
	/**
	 * Clears the element references held by this (without removing them from the owning
	 * Subqueue (if they are to be removed from the owning Subqueue, use removeElements())
	 */
	public void clear()
	{
		firstIndex = 0;
		lastIndex = -1;
	}

	/**
	 * Removes the referenced elements from the owning Subqueue and clears this 
	 */
	public void removeElements()
	{
		for (int i = firstIndex; i <= lastIndex; i++)
		{
			((Subqueue)parent).removeElement(firstIndex);
		}
		clear();
	}

	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
	protected void addFullText(StringList _lines, boolean _instructionsOnly) {
		// This class will hardly be object of code generation
	}

	@Override
	public void setColor(Color _color) 
	{
		for(int i = firstIndex; i <= lastIndex; i++)
		{      
			((Subqueue)parent).getElement(i).setColor(_color);
		}
	}

	/**
	 * Sets this element sequence as a whole and also all individual members to selected
	 * (if {@code _sel} is true) or unselected (otherwise).
	 */
	@Override
	public Element setSelected(boolean _sel)
	{
		//System.out.println(this + ".setSelected(" + _sel + ")");
		selected = _sel;
		for (int i = firstIndex; i <= lastIndex; i++)
		{
			// This must not be recursive!
			((Subqueue)parent).getElement(i).selected = _sel;
		}
		return _sel ? this : null;
	}

	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	public Element findSelected()
	{
		// Shouldn't it always return this - being a SELECTEDSequence?
		return selected ? this : null;
	}
	// END KGU#183 2016-04-24
	    
	// START KGU#123 2016-01-03: We need a collective collapsing/expansion now
	/**
	 * Sets all members individually collapsed (true) or expanded (false). 
	 */
	@Override
    public void setCollapsed(boolean collapsed) {
        super.setCollapsed(false);	// the Subqueue itself will never be collapsed
		for (int i = firstIndex; i <= lastIndex; i++)
        {
			((Subqueue)parent).getElement(i).setCollapsed(collapsed);
        }
    }
	// END KGU#123 2016-01-03

	@Override
	public void addElement(Element _element) {
		// START KGU#365 2017-03-26: Enh. #380 - we accomplish this implementation
		((Subqueue)this.parent).insertElementAt(_element, ++this.lastIndex);
		this.resetDrawingInfo();
		// END KGU#365 2017-03-26
	}

	@Override
	public void insertElementAt(Element _element, int _where) {
		// START KGU#365 2017-03-26: Enh. #380 - we accomplish this implementation
		if (_where <= this.getSize()) {
			((Subqueue)this.parent).insertElementAt(_element, this.firstIndex + _where);
			this.lastIndex++;
			this.resetDrawingInfo();
		}
		// END KGU#365 2017-03-26
	}
	
	// START KGU#43 2016-01-22: Method to control the breakpoint property of the sub-elements
	@Override
	public void toggleBreakpoint()
	{
		for (int i = 0; i < this.getSize(); i++)
		{
			this.getElement(i).toggleBreakpoint();
		}
	}
	// END KGU#43 2016-01-22

	// START KGU#156 2016-03-11: Enh. #124
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getExecStepCount(boolean)
	 */
	public int getExecStepCount(boolean _combined)
	{
		this.execStepCount = ((Subqueue)parent).getExecStepCount(false);
		if (_combined && this.getSize() > 0)
		{
			this.execSubCount = 0;
			for (int i = 0; i < this.getSize(); i++)
			{
				this.execSubCount += this.getElement(i).getExecStepCount(true);
			}
		}
		return super.getExecStepCount(_combined);
	}
	// END KGU#156 2016-03-12

	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 */
	@Override
	public void convertToCalls(StringList _signatures) {
		this.parent.convertToCalls(_signatures);
	}
	// END KGU#199 2016-07-07

	@Override
	public boolean traverse(IElementVisitor _visitor) {
		boolean proceed = true;
		// FIXME: It's not quite clear whether we better should do nothing at all here
		for (int i = 0; proceed && i < this.getSize(); i++)
		{
			proceed = this.getElement(i).traverse(_visitor);
		}
		return proceed;
	}

	@Override
	protected String[] getRelevantParserKeys() {
		// Nothing to refactor
		return null;
	}

	// START KGU#277 2016-10-13: Enh. #270
	@Override
	public void setDisabled(boolean disable) {
		for (int i = 0; i < this.getSize(); i++)
		{
			this.getElement(i).disabled = disable;
		}
	}
	// END KGU#277 2016-10-13

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.IElementSequence#getSubqueue()
	 */
	@Override
	public Subqueue getSubqueue() {
		if (this.getSize() > 0) {
			return (Subqueue)this.getElement(0).parent;
		}
		return null;
	}

	// START KGU#602 2018-10-25: Issue #419 - Mechanism to detect and handle long lines
	/**
	 * Detects the maximum text line length either on this very element 
	 * @param _includeSubstructure - whether (in case of a complex element) the substructure
	 * is to be involved
	 * @return the maximum line length
	 */
	public int getMaxLineLength(boolean _includeSubstructure)
	{
		int maxLen = 0;
		/* If this gets called with _includeSubstructure = false then it must have
		 * been selected on the top level. So the immediate children will have been
		 * meant
		 */ 
		for (int i = 0; i < this.getSize(); i++)
		{
			maxLen = Math.max(maxLen, this.getElement(i).getMaxLineLength(_includeSubstructure));
		}
		return maxLen;
	}
	// END KGU#602 2018-10-25
	
	// START KGU#866 2020-05-02: Issue #866 - modified expansion / reduction strategy
	/**
	 * @return the index offset of the first selected element within the parenting {@link Subqueue}
	 */
	public int getStartOffset()
	{
		return this.firstIndex;
	}

	/**
	 * @return the index offset of the first selected element within the parenting {@link Subqueue}
	 */
	public int getEndOffset()
	{
		return this.lastIndex;
	}
	
	/**
	 * @return the index offset of the first selected element within the parenting {@link Subqueue}
	 */
	public int getAnchorOffset()
	{
		return this.anchorOffset;
	}
	
	/**
	 * @return true if last expansion/reduction was below anchor position (or if no
	 * expansion / reduction had been done recently), false otherwise.
	 */
	public boolean wasModifiedBelowAnchor()
	{
		return this.lastActionBelow;
	}
	// END KGU#866 2020-05-02
}

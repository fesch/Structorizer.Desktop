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

/******************************************************************************************************
 *
 *      Author:         Kay Guertzig
 *
 *      Description:    This class is a kind of proxy for a selected subsequence of a Subqueue.
 *						It meets some editing purposes and contains mere references.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Kay Gürtzig     2015.11.23      First issue (KGU#87).
 *      Kay Gürtzig     2016.01.22      Bugfix #114 for Enh. #38 (addressing moveUp/moveDown, KGU#143 + KGU#144).
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.Iterator;

import lu.fisch.graphics.Canvas;
import lu.fisch.graphics.Rect;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.IElementSequence;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 * Class represents a selected subsequence of a Subqueue
 */
public class SelectedSequence extends Element implements IElementSequence {

	private int firstIndex = 0, lastIndex = -1;	// The positions of the first and last selected element within the parent Subqueue
	
	/**
	 * @param _child1 - a selected Element, marking one end of the sequence
	 * @param _child2 - a selected Element, marking the other end of  sequence
	 */
	public SelectedSequence(Element _child1, Element _child2) {
		super("");
		// _child1 and _child2 should have got a common parent (in particular, a Subqueue)
		this.parent = _child1.parent;
		int i1 = ((Subqueue)this.parent).getIndexOf(_child1);
		int i2 = ((Subqueue)this.parent).getIndexOf(_child2);
		if (i1 < 0)
		{
			if (_child1 instanceof SelectedSequence)
			{
				this.firstIndex = Math.min(((SelectedSequence)_child1).firstIndex, i2);
				this.lastIndex = Math.max(((SelectedSequence)_child1).lastIndex, i2);
			}
			else if (i2 >= 0)
			{
				this.firstIndex = this.lastIndex = i2;
			}
		}
		else if (i2 < 0)
		{
			this.firstIndex = this.lastIndex = i1;
		}
		else if (i1 <= i2)
		{
			this.firstIndex = i1;
			this.lastIndex = i2;
		}
		else
		{
			this.firstIndex = i2;
			this.lastIndex = i1;			
		}
	}

	/**
	 * @param _owner - Subqueue this is to represent a subsequence of
	 * @param _index1 - Index of the Element marking the begin of the sequence
	 * @param _index2 - Index of the Element marking the end of the sequence
	 */
	public SelectedSequence(Subqueue _owner, int _index1, int _index2) {
		super("");
		// _child1 and _child2 should have got a common parent (in particualr, a Subqueue
		this.parent = _owner;
		this.firstIndex = Math.max(0, _index1);
		this.lastIndex = Math.min(_owner.getSize()-1, _index2);
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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#resetDrawingInfoDown()
	 */
	@Override
	public void resetDrawingInfoDown() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#prepareDraw(lu.fisch.graphics.Canvas)
	 */
	@Override
	public Rect prepareDraw(Canvas _canvas) {
		Rect subrect = new Rect(0, 0, 0, 0);
		
		if (firstIndex <= lastIndex) 
		{
			for(int i = firstIndex; i <= lastIndex ;i++)
			{
				subrect = ((Subqueue)parent).getElement(i).prepareDraw(_canvas);
				rect.right = Math.max(rect.right, subrect.right);
				rect.bottom += subrect.bottom;
			}
		}
		else
		{
			rect.right = 2 * Element.E_PADDING;
			FontMetrics fm = _canvas.getFontMetrics(Element.font);
			rect.bottom = fm.getHeight() + 2* Math.round(Element.E_PADDING/2);
		}
		
		return rect;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#draw(lu.fisch.graphics.Canvas, lu.fisch.graphics.Rect)
	 */
	@Override
	public void draw(Canvas _canvas, Rect _top_left) {
		Rect myrect;
		Rect subrect;
		Color drawColor = getFillColor();
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		Canvas canvas = _canvas;		
		
		rect = _top_left.copy();
		
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
				((Subqueue) parent).getElement(i).draw(_canvas, myrect);

				//myrect.bottom-=1;
				myrect.top += subrect.bottom;
			}
		}
		else
		{
			// draw empty set symbol
			rect = _top_left.copy();
			
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
	}

	/* (non-Javadoc)
	 * The copy will be related to a new Suqueue only consisting of copies of
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

	public int getSize()
	{
		return lastIndex >= 0 ? lastIndex - firstIndex + 1 : 0;
	}
	
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
		//System.out.println(this + ".getElementByCoord("+_x + ", " + _y + ") returning " + (res == null ? "null" : res));
		return res;
	}
	
	/**
	 * Removes the given element from its owner and drops the virtual reference here
	 * (if it was referenced here)
	 * @param _element - Element t be removed
	 */
	public void removeElement(Element _element)
	{
		// Is _element within my range?
		int index = getIndexOf(_element);
		if (index >= 0)	// Yes, so drop it
		{
			((Subqueue)parent).removeElement(_element);
			lastIndex--;
		}
	}
	
	/**
	 * Removes the element referenced by _index from its owning Subqueue and
	 * drops the virtual reference here.
	 * @param _index
	 */
	public void removeElement(int _index)
	{
		if (_index >= 0 && _index < lastIndex - firstIndex)
		{
			((Subqueue)parent).removeElement(_index + firstIndex);
			lastIndex--;
		}
	}
	
	// START KGU#143 2016-01-22: Bugfix #114 - we need a method to decide execution involvement
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isExecuted()
	 */
	@Override
	public boolean isExecuted()
	{
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
	
	public boolean moveDown()
	{
		boolean feasible = this.canMoveDown();
		if (feasible) {
			Element successor = ((Subqueue)this.parent).getElement(this.lastIndex + 1);
			((Subqueue)this.parent).removeElement(this.lastIndex + 1);
			((Subqueue)this.parent).insertElementAt(successor, this.firstIndex++);
			this.lastIndex++;
		}
		return feasible;
	}

	public boolean moveUp()
	{
		boolean feasible = this.canMoveUp();
		if (feasible) {
			Element predecessor = ((Subqueue)this.parent).getElement(this.firstIndex - 1);
			((Subqueue)this.parent).removeElement(this.firstIndex - 1);
			this.firstIndex--;
			((Subqueue)this.parent).insertElementAt(predecessor, this.lastIndex--);
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
	
	@Override
	public void setSelected(boolean _sel)
	{
		//System.out.println(this + ".setSelected(" + _sel + ")");
		selected=_sel;
		for (int i = firstIndex; i <= lastIndex; i++)
		{
			// This must not be recursive!
			((Subqueue)parent).getElement(i).selected = _sel;
		}
	}

	// START KGU#123 2016-01-03: We need a collective collapsing/expansion now
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertElementAt(Element _element, int _where) {
		// TODO Auto-generated method stub
		
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

	
}

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
 *      Description:    This class represents a sequence of simple and structured elements.
 *						A subqueue can contain other elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,true)
 *      Kay Gürtzig     2015.10.12      Comment drawing centralized and breakpoint mechanism prepared.
 *      Kay Gürtzig     2015.11.22      New and modified methods to support operations on non-empty Subqueues (KGU#87).
 *      Kay Gürtzig     2015.11.23      Inheritance extended to IElementSequence (KGU#87), children now private.
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016-01-03      Enh. #87: Collapsing mechanism for selected Subqueue (KGU#123)
 *      Kay Gürtzig     2016-01-22      Bugfix #114: Method isExecuted() added (KGU#143)
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016.03.02      Bugfix #97 (KGU#136) accomplished (translation-independent selection)
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Method for test coverage tracking added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.06      Bugfix: changes of the element set now force drawing info invalidation
 *      Kay Gürtzig     2016.07.07      Enh. #185 + #188: Mechanism to convert Instructions to Calls
 *      Kay Gürtzig     2016.10.13      Enh. #277: setDisabled() added.
 *      Kay Gürtzig     2016.11.17      Bugfix #114: isExecuted() revised (signature too)
 *      Kay Gürtzig     2016.11.25      Issue #294: Method isTestCovered adapted to refined CASE coverage rules
 *      Kay Gürtzig     2016.12.20      Bugfix KGU#315: Flawed selection and cursor navigation after element shifts
 *      Kay Gürtzig     2016.04.18      Bugfix #386: New method isNoOP().
 *      Kay Gürtzig     2017.05.21      Enh. #372: Additional field for RootAttributes to be cached on undoing/redoing
 *      Kay Gürtzig     2017.07.01      Enh. #389: Additional field for caching the includeList on undoing/redoing 
 *      Kay Gürtzig     2018.04.04      Issue #529: Critical section in prepareDraw() reduced.
 *      Kay Gürtzig     2018.09.11      Issue #508: Font height retrieval concentrated to one method on Element
 *      Kay Gürtzig     2018.10.26      Enh. #619: Method getMaxLineLength() implemented
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2021-01-06      Enh. #905: draw() method enhanced to ensure markers during tutorials be shown
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.Date;
import java.util.Vector;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;

/**
 * This Structorizer class represents an sequence in a diagram and is used
 * to form the entire algorithm of the diagram "root", the body of loops, a
 * branch of an alternative or multi-selecion, a thread of a parallel section,
 * etc.<br/>
 * Subqueues are the glue in the element hierarchy of any diagram, i.e.,
 * vertically there is always a Subqueue between an element and its logical
 * parent.
 * 
 * @author Bob Fisch
 */
public class Subqueue extends Element implements IElementSequence {

	public Subqueue()
	{
		// START KGU#91 2015-12-01: A Subqueue has no own text, not even an empty line
		//super("");
		super();
		// END KGU#91 2015-12-01
	}
	
	public Subqueue(StringList _strings)
	{
		// START KGU#91 2015-12-01: A Subqueue has no own text, not even an empty line
		//super(_strings);
		super();
		// END KGU#91 2015-12-01
	}
	
	private Vector<Element> children = new Vector<Element>();
	// START KGU#136 2016-03-01: Bugfix #97
	private Vector<Integer> y0Children = new Vector<Integer>();
	// END KGU#136 2016-03-01
	// START KGU#363 2017-05-21: Enh. #372 - for the undo/redo list we need to cache Root attributes
	public RootAttributes rootAttributes = null;
	// END KGU#363 2017-05-21
	// START KGU#363 2018-09-12: Enh. #372 - for the undo/redo list we need to cache former modification date
	public Date modified = null;
	// END KGU#363 2017-09-12
	// START KGU#376 2017-07-01: Enh. #389: comma-separated diagram names
	public String diagramRefs = null;
	// END KGU#376 2017-07-01
	
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRect0UpToDate) return rect0;
		// START KGU#516 2018-04-04: Directly to work on fields was not so good an idea for re-entrance
		//this.y0Children.clear();
		// END KGU#516 2018-04-04
		// END KGU#136 2016-03-01

		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		Rect subrect = new Rect();
		
		// START KGU#516 2018-04-04: Issue #529 - Directly to work on field rect0 was not so good an idea for re-entrance
		//rect0.top = 0;
		//rect0.left = 0;
		//rect0.right = 0;
		//rect0.bottom = 0;
		Rect rect0 = new Rect();
		Vector<Integer> y0Children = new Vector<Integer>();
		// END KGU#516 2018-04-04
		
		if (children.size() > 0) 
		{
			for(int i = 0; i < children.size(); i++)
			{
				//System.out.println(children.get(i) + ".prepareDraw()");
				// START KGU#136 2016-03-01: Bugfix #97
				y0Children.addElement(rect0.bottom);
				// END KGU#136 2016-03-01
				subrect = children.get(i).prepareDraw(_canvas);
				rect0.right = Math.max(rect0.right, subrect.right);
				rect0.bottom += subrect.bottom;
			}
		}
		else
		{
			// START KGU#136 2016-03-01: Bugfix #97
			y0Children.addElement(rect0.bottom);
			// END KGU#136 2016-03-01
			rect0.right = 2*Element.E_PADDING;
			// START KGU#494 2018-09-11: Issue #508 Retrieval concentrated for easier maintenance
			//FontMetrics fm = _canvas.getFontMetrics(Element.font);
			int fontHeight = getFontHeight(_canvas.getFontMetrics(Element.font));
			// END KGU#494 2018-09-11
			rect0.bottom = fontHeight + 2*(Element.E_PADDING/2);

		}
		
		// START KGU#516 2018-04-04: Issue #529 - reduced critical section
		this.rect0 = rect0;
		this.y0Children = y0Children;
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

		Rect myrect;
		Rect subrect;
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = getColor();
		Color drawColor = getFillColor();
		// END KGU 2015-10-13
		// START KGU#494 2018-09-11: Issue #508 Retrieval concentrated for easier maintenance
		//FontMetrics fm = _canvas.getFontMetrics(Element.font);
		int fontHeight = getFontHeight(_canvas.getFontMetrics(Element.font));
		// END KGU#494 2018-09-11
		Canvas canvas = _canvas;		
		
		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		Point dP = this.getDrawPoint();
		this.topLeft.x = _top_left.left - dP.x;
		this.topLeft.y = _top_left.top - dP.y;
		// END KGU#136 2016-03-01
		
		myrect = _top_left.copy();
		myrect.bottom = myrect.top;
		
		if (children.size() > 0)
		{
			// draw children
			for(int i=0; i<children.size(); i++)
			{
				//System.out.println(children.get(i) + "prepareraw()");
				subrect = children.get(i).prepareDraw(_canvas);
				myrect.bottom += subrect.bottom;
				if (i==children.size()-1)
				{
					myrect.bottom = _top_left.bottom;
				}
				((Element) children.get(i)).draw(_canvas, myrect, _viewport, _inContention);

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
							_top_left.top +((_top_left.bottom-_top_left.top) / 2) + (fontHeight / 2),
							"\u2205"
							);  	

			// START KGU#906 2021-01-06: Enh. #905 This will chiefly be used for tutorial hints
			this.drawWarningSignOnError(canvas, _top_left);
			// END KGU#906 2021-01-06

			// START KGU#156 2016-03-11: Enh. #124
			// write the run-time info if enabled
			this.writeOutRuntimeInfo(canvas, _top_left.right - (Element.E_PADDING / 2), _top_left.top);
			// END KGU#156 2016-03-11
					
			canvas.drawRect(_top_left);
		}
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
		wasDrawn = true;
		// END KGU#502/KGU#524/KGU#553 2019-03-14
	}
	
	public int getSize()
	{
		return children.size();
	}
	
	public int getIndexOf(Element _ele)
	{
		return children.indexOf(_ele);
	}
	
	public Element getElement(int _index)
	{
		return (Element) children.get(_index);
	}
	
	public void addElement(Element _element)
	{
		// START KGU#87 2015-11-22: We must make sure a Subqueue as _element is properly appended
//		children.add(_element);
//		_element.parent=this;
		insertElementAt(_element, children.size());
		// END KGU#87 2015-11-22
	}

	// START KGU#87 2015-11-22: Allow the insertion of all children of another Subqueue
	/**
	 * Inserts the given _element before child no. _where (if 0 <= _where <= this.getSize()).
	 * If _element is another implementor of IElementSequence, however, all children of _element
	 * will be inserted before the child _where, instead.
	 * @param _element - an Element to be inserted (or the children of which are to be inserted here)
	 * @param _where - index of the child, which _element (or _element's children) is to inserted before  
	 */
	public void insertElementAt(Element _element, int _where)
	{
		if (_element instanceof IElementSequence)
		{
			for (int i = 0; i < ((IElementSequence)_element).getSize(); i++)
			{
				insertElementAt(((IElementSequence)_element).getElement(i), _where + i);
			}
		}
		else
		{
			children.insertElementAt(_element, _where);
			_element.parent=this;
		}
		// START KGU#136 2016-07-06: Bugfix #97
		this.resetDrawingInfoUp();
		// END KGU#136 2016-07-06
	}

	public void clear()
	{
		children.clear();
		// START KGU#136 2016-07-06: Bugfix #97
		this.resetDrawingInfoUp();
		// END KGU#136 2016-07-06
	}
	// END KGU#87 2015-11-22
	
	
	public void removeElement(Element _element)
	{
		children.removeElement(_element);
		// START KGU#136 2016-07-06: Bugfix #97
		this.resetDrawingInfoUp();
		// END KGU#136 2016-07-06
	}
	
	public void removeElement(int _index)
	{
		// START KGU 2015-11-22: Why search if we got the index?
		//children.removeElement(children.get(_index));
		children.removeElementAt(_index);
		// END KGU 2015-11-22
		// START KGU#136 2016-07-06: Bugfix #97
		this.resetDrawingInfoUp();
		// END KGU#136 2016-07-06
	}
	
	// START KGU#136 2016-03-02: New method to facilitate bugfix #97
	public boolean moveElement(int _from, int _to)
	{
		boolean done = 0 <= _from && _from < children.size() && 0 <= _to && _to < children.size();
		if (done)
		{
			Element ele = children.get(_from);
			children.removeElementAt(_from);
			children.insertElementAt(ele, _to);
			// START KGU#315 2016-12-19: Bugfix: If we don't escalate then prepareDraw won't be done here 
			//this.resetDrawingInfo();	// Element start points must be re-computed
			this.resetDrawingInfoUp();	// Element start points must be re-computed
			// END KGU#315 2016-12-19
		}
		return done;
	}
	// END KGU#136 2016-03-02
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.IElementContainer#removeElements()
	 */
	@Override
	public void removeElements() {
		clear();
	}
	
	public java.util.Iterator<Element> getIterator()
	{
		return children.iterator();
	}
	

	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element res = super.getElementByCoord(_x, _y, _forSelection);
		// If this element isn't hit then there is no use searching the substructure
		if (res != null || _forSelection)
		{
			Element sel = null;
			for (int i = 0; i < children.size(); i++)
			{
				// START KGU#136 2016-03-01: Bugfix #97
				//sel = ((Element) children.get(i)).getElementByCoord(_x, _y, _forSelection);
				if (i < this.y0Children.size())
				{
					int yOff = this.y0Children.get(i);
					sel = children.get(i).getElementByCoord(_x, _y-yOff, _forSelection);
				}
				// END KGU#136 2016-03-01
				if (sel != null)
				{
					if (_forSelection) selected = false;
					res = sel;
				}
			}
		}
		return res;
	}
	
	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	public Element findSelected()
	{
		Element sel = selected ? this : null;
		// Now look for a selected subsequence
		if (sel == null)
		{
			int from = children.size(), to = -1;
			boolean done = false;
			for (int i = 0; !done && i < this.children.size(); i++)
			{
				if (children.elementAt(i).getSelected())
				{
					if (from > i)
					{
						from = i;
					}
					else
					{
						to = i;
					}
				}
				else
				{
					done = from < i;
				}
			}
			if (to >= 0)
			{
				sel = new lu.fisch.structorizer.gui.SelectedSequence(this, from, to);
			}
		}
		// If neither this nor a subsequence is selected then look into the deep
		for (int i = 0; sel == null && i < this.children.size(); i++)
		{
			sel = children.elementAt(i).findSelected();
		}
		return sel;
	}
	// END KGU#183 2016-04-24

	@Override
	public Element copy()
	{
		Element ele = new Subqueue();
		ele.setColor(this.getColor());
		for(int i = 0; i < children.size(); i++)
		{
			((Subqueue) ele).addElement(((Element) children.get(i)).copy());
		}
		// START KGU#117 2016-03-07: Enh. #77
		// START KGU#156/KGU#225 2016-07-28: Bugfix #210
		//ele.deeplyCovered = Element.E_COLLECTRUNTIMEDATA && this.deeplyCovered;
		this.copyRuntimeData(ele, false);
		// END KGU#156/KGU#225 2016-07-28
		// END KGU#117 2016-03-07
		// START KGU#183 2016-04-24: Issue #169
		ele.selected = this.selected;
		// END KGU#183 2016-04-24
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
		boolean isEqual = super.equals(_another) && this.children.size() == ((Subqueue)_another).getSize();
		for (int i = 0; isEqual && i < children.size(); i++)
		{
			isEqual = children.get(i).equals(((Subqueue)_another).getElement(i));
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
		for (int i = 0; isEqual && i < children.size(); i++)
		{
			isEqual = children.get(i).combineRuntimeData(((Subqueue)_cloneOfMine).getElement(i));
		}
		return isEqual;
	}
	// END KGU#117 2016-03-07

	// START KGU#87 2015-11-22: Re-enabled for multiple selection (selected non-empty subqueues)    
	@Override
	public void setColor(Color _color) 
	{
		super.setColor(_color);
		for(int i=0; i<children.size(); i++)
		{
			children.get(i).setColor(_color);
		}
	}
	// END KGU#87 2015-11-22

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

	// START KGU#143 2016-01-22: Bugfix #114 - we need a method to decide execution involvement
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isExecuted()
	 */
	@Override
	public boolean isExecuted(boolean ignored)
	{
		boolean involved = false;
		// START KGU#143 2016-11-17: Issue #114 - If the parent isn't in waited state then there may not be execution here
		if (parent == null || parent.waited)
		{
		// END KGU#143 2016-11-17
			for (int index = 0; !involved && index < this.getSize(); index++)
			{
				// START KGU#143 2016-11-17: Issue #114 - Don't risk cyclic recursion!
				//if (children.get(index).isExecuted())
				if (children.get(index).isExecuted(false))
				// END KGU#143 2016-11-17
				{
					involved = true;
				}
			}
		}
		return involved;
	}
	// END KGU#143 2016-01-22

	// START KGU#117 2016-03-06: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isTestCovered(boolean)
	 */
	public boolean isTestCovered(boolean _deeply)
	{
		// START KGU#296 2016-11-25: Issue #294 - hidden default CASE branch required modification
//		// An empty sequence must at least once have been passed in order to count as covered
//		if (children.isEmpty())
//		{
//			return super.isTestCovered(_deeply);
//		}
//		// ... otherwise all instructions must be covered
//		boolean covered = true;
		boolean covered = super.isTestCovered(_deeply);
		if (covered || children.isEmpty()) {
			return covered;
		}
		covered = true;
		// END KGU#296 2016-11-25
		for(int i = 0; covered && i < children.size(); i++)
		{
			// START KGU#345 2017-02-07: Bugfix #342 - disabled elements must be ignored for test coverage
			//covered = children.get(i).isTestCovered(_deeply);
			if (!children.get(i).isDisabled(true)) {
				covered = children.get(i).isTestCovered(_deeply);
			}
			// END KGU#345 2017-02-07
		}
		return covered;
	}
	// END KGU#117 2016-03-06

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
	protected void addFullText(StringList _lines, boolean _instructionsOnly)
	{
		// No own text is to be considered here
		for(int i = 0; i < children.size(); i++)
		{
			children.get(i).addFullText(_lines, _instructionsOnly);
		}
	}
	// END KGU 2015-10-16

	// START KGU#87 2015-11-22: Allow the selection flagging of all immediate children
	@Override
	public Element setSelected(boolean _sel)
	{
		selected = _sel;
		for (int i = 0; i < getSize(); i++)
		{
			// This must not be recursive!
			children.get(i).selected = _sel;
		}
		return _sel ? this : null;
	}
	// END KGU#87 2015-11-22

	// START KGU#123 2016-01-03: We need a collective collapsing/expansion now
	@Override
	public void setCollapsed(boolean collapsed) {
		super.setCollapsed(false);	// the Subqueue itself will never be collapsed
		java.util.Iterator<Element> iter = getIterator();
		while (iter.hasNext())
		{
			iter.next().setCollapsed(collapsed);
		}
	}
	// END KGU#123 2016-01-03
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 */
	@Override
	public void convertToCalls(StringList _signatures)
	{
		boolean somethingChanged = false;
		for (int i = 0; i < this.children.size(); i++)
		{
			Element ele = this.children.get(i);
			if (ele instanceof Instruction && !(ele instanceof Call) && ((Instruction)ele).isCallOfOneOf(_signatures))
			{
				Element newEle = new Call((Instruction)ele);
				this.children.setElementAt(newEle, i);
				newEle.parent = this;
				somethingChanged = true;
			}
			else
			{
				ele.convertToCalls(_signatures);
			}
		}
		if (somethingChanged)
		{
			this.resetDrawingInfoUp();
		}
	}
	// END KGU#199 2016-07-07

	@Override
	public boolean traverse(IElementVisitor _visitor) {
		boolean proceed = _visitor.visitPreOrder(this);
		for (int i = 0; proceed && i < children.size(); i++)
		{
			proceed = children.get(i).traverse(_visitor);
		}
		if (proceed)
		{
			proceed = _visitor.visitPostOrder(this);
		}
		return proceed;
	}

	@Override
	protected String[] getRelevantParserKeys() {
		return null;
	}

	@Override
	public void setDisabled(boolean disable) {
		for (int i = 0; i < this.getSize(); i++)
		{
			this.getElement(i).setDisabled(disable);
		}
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.IElementSequence#getSubqueue()
	 */
	@Override
	public Subqueue getSubqueue() {
		return this;
	}
	
	// START KGU#383 2017-04-18: Bugfix #386
	/**
	 * Returns true if this contains only insructions with empty text (e.g. mere
	 * comments without implementation). This may be important to know for code
	 * export.
	 * @return true if no substantial instruction is contained.
	 */
	public boolean isNoOp()
	{
		for (int i = 0; i < this.getSize(); i++) {
			Element ele = this.getElement(i);
			if (!ele.isDisabled(false) && (
					!(ele instanceof Instruction)
					|| (ele instanceof Jump)
					|| !ele.getText().getLongString().trim().isEmpty())
					) {
				return false;
			}
		}
		return true;
	}
	// END KGU#383 2017-04-18

	// START KGU#3401 2017-05-17: Issue #405
	public void setRotated(boolean _rotated) {
		super.setRotated(_rotated);
		for (int i = 0; i < this.getSize(); i++) {
			this.getElement(i).rotated = _rotated;
		}
	}
	// END KGU#401 2017-05-17
	
	// START KGU 2017-10-21
	/**
	 * Checks whether {@link Element} {@code _ele} is member of this and may be reached i.e.
	 * is neither directly nor indirectly preceded by a {@link Jump} element. If {@code _deepCheck}
	 * is true then preceding structured elements are also checked if they may be left.  
	 * @param _ele - the {@link Element} the reachability of which is to be tested
	 * @param _deepCheck - whether preceding structured elements are to be checked particularly 
	 * @return true if {@code _ele} is potentially reachable within this.
	 */
	public boolean isReachable(Element _ele, boolean _deepCheck)
	{
		return isReachable(children.indexOf(_ele), _deepCheck); 
	}

	/**
	 * Checks whether {@link Element} with index {@code _index} exists may be reached i.e.
	 * is neither directly nor indirectly preceded by a {@link Jump} element. If {@code _deepCheck}
	 * is true then preceding structured elements are also checked if they may be left.  
	 * @param _ele - the {@link Element} the reachability of which is to be tested
	 * @param _deepCheck - whether preceding structured elements are to be checked particularly 
	 * @return true if {@code _ele} is potentially reachable within this.
	 */
	public boolean isReachable(int _index, boolean _deepCheck) {
		boolean reachable = _index >= 0 && _index < this.children.size();
		while (_index >= 0 && reachable) {
			Element ele = children.get(_index--);
			reachable = !_deepCheck && (ele.isDisabled(true) || !(ele instanceof Jump)) || ele.mayPassControl();
		}
		return reachable;
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#mayPassControl()
	 */
	public boolean mayPassControl()
	{
		int size = this.children.size();
		return size == 0 || this.children.get(size-1).mayPassControl() && this.isReachable(size-1, true);
	}
	// END KGU 2017-10-21

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
		for (Element el: this.children) {
			maxLen = Math.max(maxLen, el.getMaxLineLength(_includeSubstructure));
		}
		return maxLen;
	}
	// END KGU#602 2018-10-25
	
	// START KGU#695 2021-01-23: Enh. #714 Also return true if all elements are disabled
	/**
	 * @return {@code true} if there is at least one member not being individually disabled
	 */
	public boolean hasEnabledElements()
	{
		for (int i = 0; i < this.children.size(); i++) {
			// Inherited disabling is of no interest here
			if (!this.children.get(i).isDisabled(true)) {
				return true;
			}
		}
		return false;
	}
	// END KGU#695 2021-01-23
}

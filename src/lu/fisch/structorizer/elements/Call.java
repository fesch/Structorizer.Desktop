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
 *      Description:    This class represents an "call" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.13      First Issue
 *      Kay Gürtzig     2015.10.11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015.10.12      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015.11.14      Bugfix #31 (= KGU#82) in method copy
 *		Kay Gürtzig     2015.12.01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *		Kay Gürtzig     2015.01.03      Enh. #87 (KGU#122) -> getIcon()
 *		Kay Gürtzig     2015.03.01      Bugfix #97 (KGU#136) Steady selection mechanism
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      Until 2015, this class had not had any specific effect on execution and code export. This was
 *      changed by enhancement requests #9.
 *      Though chiefly the Executor (and perhaps some code generators) is concerned, this class file
 *      seems to be a good place to state the general ideas behind the Call element as now being handled.
 *      1. In order to get a Call working, it must refer to a function defined by another Nassi-
 *         Shneiderman diagram or just the diagram itself (recursive routine).
 *      2. The called diagram is required to be a function diagram and must match the "method name"
 *         (case-sensitive!) and parameter count of the call.
 *      3. To keep things simple, the call text must consist of a single instruction line,
 *         either being a procedure call:
 *             <proc_name> ( <value1>, <value2>, ... , <value_n> )
 *         or a variable assignment with a single function call as expression:
 *             <var_name> <- <func_name> ( <value1>, <value2>, ... , <value_n> )
 *      4. A direct output instruction is not supported like in:
 *             OUT foreign(something).
 *         Hence to use the results of a foreign call, first assign the value to a variable within
 *         a Call element, then use the variable as part of some expression in an ordinary
 *         Instruction element.
 *      5. Nested or multiple subroutine calls as in the following examples are not allowed
 *             foreign(x, foreign(y, a))
 *             result <- foreign(a) + foreign(b)
 *         Workaround: analogous to 4.)
 *      6. The called diagram must be opened and held in a container accessible by the Structorizer
 *         (e.g. Arranger surface or a tab list of the Structorizer itself) in order to make the call
 *         work on execution.
 *      7. Whether a returned value is required and in this case of what type will only dynamically be
 *         relevant on execution (interpreted code). There is no check in advance.
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.gui.IconLoader;

public class Call extends Instruction {
	
	public Call()
	{
		super();
	}
	
	public Call(String _strings)
	{
		super(_strings);
		setText(_strings);	// FIXME (KGU 2015-10-13): What is this good for? This has already been done by both the super and its super constructor!
	}
	
	public Call(StringList _strings)
	{
		super(_strings);
		setText(_strings);
	}
	
	
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		// END KGU#136 2016-03-01
        // KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		rect0.top=0;
		rect0.left=0;
		rect0.right=0;
		rect0.bottom=0;
		
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		
		// START KGU#91 2015-12-02: The minimum width must allow to show both vertical lines
		//rect.right = 2*(E_PADDING/2);
		rect0.right = 8*(E_PADDING/2);
		// END KGU#91 2015-12-02
		
		for (int i=0; i<getText(false).count(); i++)
		{
			int lineWidth = getWidthOutVariables(_canvas,getText(false).get(i),this)+4*E_PADDING;
			if (rect0.right < lineWidth)
			{
				rect0.right = lineWidth;
			}
		}
		rect0.bottom = 2 * (E_PADDING/2) + getText(false).count() * fm.getHeight();

		// START KGU#136 2016-03-01: Bugfix #97
		isRectUpToDate = true;
		// END KGU#136 2016-03-01
		return rect0;
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{
		Rect myrect = new Rect();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = getColor();
		Color drawColor = getFillColor();
		// END KGU 2015-10-13
		FontMetrics fm = _canvas.getFontMetrics(Element.font);

		// START KGU 2015-10-13: Became obsolete by new method getFillColor() applied above now
//		if (selected==true)
//		{
//			drawColor=E_DRAWCOLOR;
//		}
		// END KGU 2015-10-13
		
		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		Point ref = this.getDrawPoint();
		this.topLeft.x = _top_left.left - ref.x;
		this.topLeft.y = _top_left.top - ref.y;
		// END KGU#136 2016-03-01
		
		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		myrect=_top_left.copy();
		
		canvas.fillRect(myrect);
		
		// draw comment
		if(Element.E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
		{
			this.drawCommentMark(canvas, _top_left);
		}
		// START KGU 2015-10-11
		// draw breakpoint bar if necessary
		this.drawBreakpointMark(canvas, _top_left);
		// END KGU 2015-10-11
		
		// START KGU#156 2016-03-11: Enh. #124
		// write the run-time info if enabled
		this.writeOutRuntimeInfo(canvas, _top_left.left + rect.right - (Element.E_PADDING), _top_left.top);
		// END KGU#156 2016-03-11
				
		
		for(int i=0;i<getText(false).count();i++)
		{
			String text = this.getText(false).get(i);
			text = BString.replace(text, "<--","<-");
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
					_top_left.left + 2 * (E_PADDING / 2),
					_top_left.top + (E_PADDING / 2) + (i+1) * fm.getHeight(),
					text,this
					);  	
		}
		
		canvas.setColor(Color.BLACK);
		canvas.moveTo(_top_left.left  + (E_PADDING / 2), _top_left.top);
		canvas.lineTo(_top_left.left  + (E_PADDING / 2), _top_left.bottom);
		canvas.moveTo(_top_left.right - (E_PADDING / 2), _top_left.top);
		canvas.lineTo(_top_left.right - (E_PADDING / 2), _top_left.bottom);
		
		canvas.setColor(Color.BLACK);
		canvas.drawRect(_top_left);
	}
	
	// START KGU#122 2016-01-03: Enh. #87 - Collapsed elements may be marked with an element-specific icon
	@Override
	protected ImageIcon getIcon()
	{
		return IconLoader.ico058;
	}
	// END KGU#122 2016-01-03

	public Element copy()
	{
		Element ele = new Call(this.getText().copy());
		ele.setComment(this.getComment().copy());
		ele.setColor(this.getColor());
		// START KGU#82 (bug #31) 2015-11-14
		ele.breakpoint = this.breakpoint;
		// END KGU#82 (bug #31) 2015-11-14
		// START KGU#117 2016-03-07: Enh. #77
		ele.simplyCovered = Element.E_COLLECTRUNTIMEDATA && this.simplyCovered;
		ele.deeplyCovered = Element.E_COLLECTRUNTIMEDATA && this.deeplyCovered;
		// END KGU#117 2016-03-07
		return ele;
	}

	// START KGU#117 2016-03-07: Enh. #77
	/**
	 * In test coverage mode, sets the local tested flag if element is fully covered,
	 * which - if E_TESTCOVERAGERECURSIVE is set - must include the called subroutine(s)
	 */
	@Override
	public void checkTestCoverage(boolean _propagateUpwards)
	{
		// Replace super implementation by the original Element implementation again
		if (Element.E_COLLECTRUNTIMEDATA && (this.isTestCovered(false) || this.isTestCovered(true)))
		{
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
	// END KGU#117 2016-03-07

}

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
 *      Description:    This class represents a "jump" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.13      First Issue
 *      Kay Gürtzig     2015.10.12      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015.11.14      Bugfix #31 = KGU#82 in method copy()
 *		Kay Gürtzig     2015.12.01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *		Kay Gürtzig     2016.01.03      Enh. #87 (KGU#122) -> getIcon()
 *		Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136) Drawing/dragging/selection consolidated
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.07      Enh. #188: New copy constructor to support conversion (KGU#199)
 *
 ******************************************************************************************************
 *
 *      Comment:	Kay Gürtzig	/ 2015.11.27
 *      Until 2015, this class had not had any specific effect on execution and code export. This was
 *      changed by enhancement requests #23 and #22, respectively.
 *      Though chiefly the Executor and the code generators are concerned, this class file seems to be
 *      a good place to state the general ideas behind the Jump element as being handled here.
 *      First of all, any kind of jump severely compromises the concept of structured programming. So
 *      jumps ought to be avoided. Full stop.
 *      On the other hand, the DIN 66261 standard includes this kind of element (titled "termination")
 *      without specifying into detail its semantics. Roughly, it means a jump to the end of an
 *      enclosing construct.
 *      The following cases of enclosing constructs obviously make sense to terminate:
 *      - loop of any type (leave, break)
 *      - routine (return, with or without result)
 *      - program (exit, possibly with status value)
 *      The following cases of enclosing constructs clearly don't make sense to terminate:
 *      - sequence: an unconditioned termination would make all subsequent instructions useless, a
 *           conditional termination of a sequence could easily be avoided by inverting the condition
 *           and putting the subsequent elements into the conditional branch instead.
 *      - alternative: In order to get to the end of an alternative just don't add more instructions
 *           to the branch. Alternatives must be "transparent" for break/leave, return, and exit,
 *           otherwise "conditional termination" would be a meaningless concept.
 *      - case switch: see alternative. It might be confusing, though, that in C-like languages a
 *           break instruction is needed to end a case branch. In a Nassi-Shneiderman diagram, however,
 *           there is obviously no need for such a workaround, the branch ends where it ends.
 *           Hence, a selection element ought to be transparent for termination as well.
 *      - parallel section: No single thread may steal off the flock or even stop the entire show.
 *           Only to exit the entire process may be allowed, not even a return out of a parallel branch
 *           seems tolerable. In no case a loop enclosing the parallel element may be terminated from
 *           within one of the concurrent branches. So, a parallel section is opaque and impenetrable
 *           for leave attempts and will only end when the last of its threads has reached the barrier.
 *      So this is the design specification derived from the above analysis:
 *      1. Jumps may terminate:
 *         a) the innermost enclosing loop - standard behaviour of an empty Jump element, a keyword
 *            is optional (e.g. "break" or "leave");
 *         b) the current (sub-)routine - requires a keyword (e.g. "return"), possibly with a return value;
 *         c) the process - requires a keyword (e.g. "exit"), possibly with an integral exit code.
 *      2. Alternatives and Case elements are transparent for termination.
 *      3. Parallel sections are impermeable for termination except exit.
 *      4. Routines are impermeable for terminations of type a).
 *      5. Multi-level loop termination is a particularly critical breach of structured programming,
 *         but might be granted here by specifying the number of loop levels to leave as Jump text,
 *         optionally prefixed by a keyword (preferably "leave" rather than "break").
 *      6. Any attempt to leave more levels than the current depth of nested loops is a syntax error
 *         and immediately aborts execution.
 *      7. An attempt to leave or return from the inside of a parallel section is regarded as syntax
 *         error but will raise a warning on execution and continue after having killed just the causing
 *         thread.
 *      8. Structorizer will NOT allow any kind of goto to a label.
 *      
 *      Notes on code export
 *      1. It is to be dealt with languages lacking support for jumps, premature leave or return.
 *      2. Multi-level termination is hardly supported by most programming languages but may perhaps
 *         be translated to a goto statement with a generated target label immediately behind the loop
 *         to be left - if goto is available like in C. In Java, however, a labeled break statement
 *         might do the job but requires the code generator to know in advance that such a break
 *         statement will occur within the nested substructure (because the label is to be placed at
 *         the beginning of the complex instruction to be left).
 *      3. A Jump element inside a Case instruction actually means a two-level break in C-like languages
 *         and hence requires a goto or a labeled break instruction.
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.gui.IconLoader;

public class Jump extends Instruction {

	public Jump()
	{
		super();
	}
	
	public Jump(String _strings)
	{
		super(_strings);
		setText(_strings);
	}
	
	public Jump(StringList _strings)
	{
		super(_strings);
		setText(_strings);
	}
	
	// START KGU#199 2016-07-07: New for enh. #188
	public Jump(Instruction instr)
	{
		super(instr);
	}
	// END KGU#199 2016-07-07	
	
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
		
		// FIXME (KGU): What is the rounding of an integer division result good for?
		rect0.right = 2 * (E_PADDING/2);
		for (int i=0; i<getText(false).count(); i++)
		{
			// FIXME (KGU): The width parameters differ from the ones in draw()!
			int lineWidth = getWidthOutVariables(_canvas, getText(false).get(i), this) + 3*E_PADDING;
			if (rect0.right < lineWidth)
			{
				rect0.right = lineWidth;
			}
		}
		// FIXME (KGU): What is the rounding of an integer division result good for?
		rect0.bottom = 2*(Element.E_PADDING/2) + getText(false).count()*fm.getHeight();
		
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
//			drawColor=Element.E_DRAWCOLOR;
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
		this.writeOutRuntimeInfo(canvas, myrect.right - (Element.E_PADDING / 2), myrect.top);
		// END KGU#156 2016-03-11
				
		
		for(int i=0;i<getText(false).count();i++)
		{
			String text = this.getText(false).get(i);
			text = BString.replace(text, "<--", "<-");
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
					_top_left.left + 2 * (E_PADDING / 2),
					_top_left.top + (E_PADDING / 2) + (i+1)*fm.getHeight(),
					text, this
					);  	
		}

		canvas.setColor(Color.BLACK);	// With an empty text, the decoration often was invisible.
		canvas.moveTo(_top_left.left + (E_PADDING / 2), _top_left.top);
		canvas.lineTo(_top_left.left, _top_left.bottom + ((_top_left.top-_top_left.bottom) / 2));
		canvas.lineTo(_top_left.left + (E_PADDING / 2), _top_left.bottom);
		
		canvas.setColor(Color.BLACK);
		canvas.drawRect(_top_left);
	}

	// START KGU#122 2016-01-03: Collapsed elements may be marked with an element-specific icon
	@Override
	protected ImageIcon getIcon()
	{
		return IconLoader.ico059;
	}
	// END KGU#122 2016-01-03

	public Element copy()
	{
		Element ele = new Jump(this.getText().copy());
// START KGU#199 2016-07-07: Enh. #188, D.R.Y.
//		ele.setComment(this.getComment().copy());
//		ele.setColor(this.getColor());
//		// START KGU#82 (bug #31) 2015-11-14
//		ele.breakpoint = this.breakpoint;
//		// END KGU#82 (bug #31) 2015-11-14
//		// START KGU#117 2016-03-07: Enh. #77
//        if (Element.E_COLLECTRUNTIMEDATA)
//        {
//        	// We share this object (important for recursion!)
//        	ele.deeplyCovered = this.deeplyCovered;
//        }
//		// END KGU#117 2016-03-07
//		// START KGU#183 2016-04-24: Issue #169
//		ele.selected = this.selected;
//		// END KGU#183 2016-04-24
//		return ele;
//	}
		return copyDetails(ele, false);
	}
// END KGU#199 2016-07-07
	
	
	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * Only adds anything if _instructionsOnly is set false (because no new variables ought to occur here).
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
		// In a jump instruction no variables ought to be introduced - so we ignore this text on _instructionsOnly
		if (!_instructionsOnly)
		{
			_lines.add(this.getText());
		}
    }
    // END KGU 2015-10-16
	
	
}

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
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *      Kay Gürtzig     2016.01.03      Enh. #87 (KGU#122) -> getIcon()
 *      Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136) Drawing/dragging/selection consolidated
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.07      Enh. #188: New copy constructor to support conversion (KGU#199)
 *      Kay Gürtzig     2016.07.30      Enh. #128: New mode "comments plus text" supported, drawing code delegated
 *      Kay Gürtzig     2017.03.03      Enh. #354: New classification methods isLeave(), isReturn(), isExit()
 *      Kay Gürtzig     2017.04.14      Issues #23,#380,#394: new jump analysis helper methods
 *      Kay Gürtzig     2017.06.09      Enh. #416: Adaptations for execution line continuation
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.parsers.CodeParser;

public class Jump extends Instruction {

	// START KGU#258 2016-09-26: Enh. #253
	private static final String[] relevantParserKeys = {"preLeave", "preExit", "preReturn"};
	// END KGU#258 2016-09-25
	
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
	
	// START KGU#227 2016-07-30: Enh. #128
	/**
	 * Provides a subclassable left offset for drawing the text
	 */
	protected int getTextDrawingOffset()
	{
		return (Element.E_PADDING/2);
	}
	// END KGU#227 2016-07-30

	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		// END KGU#136 2016-03-01

		// START KGU#227 2016-07-30: Enh. #128 - on this occasion, we just enlarge the instruction rect width
		super.prepareDraw(_canvas);
		rect0.right += (E_PADDING/2);		
		// END KGU#227 2016-07-30
		return rect0;
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{
		// START KGU 2016-07-30: Just delegate the basics to super
		super.draw(_canvas, _top_left);
		// END KGU 2016-07-30: Just delegate the basics to super

		_canvas.setColor(Color.BLACK);	// With an empty text, the decoration often was invisible.
		_canvas.moveTo(_top_left.left + (E_PADDING / 2), _top_left.top);
		_canvas.lineTo(_top_left.left, _top_left.bottom + ((_top_left.top-_top_left.bottom) / 2));
		_canvas.lineTo(_top_left.left + (E_PADDING / 2), _top_left.bottom);
		
		// START KGU 2016-07-30: Just delegate the basics to super
//		_canvas.setColor(Color.BLACK);
//		_canvas.drawRect(_top_left);
		// END KGU 2016-07-30: Just delegate the basics to super
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
		return copyDetails(ele, false, false);
		// END KGU#199 2016-07-07
	}
	
	
	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * Only adds anything if _instructionsOnly is set false (because no new variables ought to occur here).
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
	protected void addFullText(StringList _lines, boolean _instructionsOnly, HashSet<Root> implicatedRoots)
	{
		// In a jump instruction no variables ought to be introduced - so we ignore this text on _instructionsOnly
		if (!this.isDisabled() && !_instructionsOnly)
		{
			// START KGU#413 2017-06-09: Enh. #416: Cope with user-inserted line breaks
			//_lines.add(this.getText());
			_lines.add(this.getUnbrokenText());
			// END KGU#413 2017-06-09
		}
	}
	// END KGU 2015-10-16
	
	// START KGU#258 2016-09-26: Enh. #253
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRelevantParserKeys()
	 */
	@Override
	protected String[] getRelevantParserKeys() {
		// There is nothing to refactor
		return relevantParserKeys;
	}
	// END KGU#258 2016-09-25
	
	// START KGU#354 2017-03-03: Enh. #354 More consistent support for generators etc.
	/**
	 * Checks whether the given line contains a return statement
	 * @param line the text line to be analysed
	 * @return true if this has one line and matches the return syntax 
	 */
	public static boolean isReturn(String line)
	{
    	StringList tokens = Element.splitLexically(line, true);
		return (tokens.indexOf(CodeParser.getKeyword("preReturn"), !CodeParser.ignoreCase) == 0);
	}
	/**
	 * Checks whether this element contains a return statement
	 * @return true if this has one line and matches the leave syntax 
	 */
	public boolean isReturn()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && isReturn(this.text.get(0).trim());
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && isReturn(lines.get(0));
		// END KGU#413 2017-06-09
	}
	/**
	 * Checks whether the given line contains a leave statement
	 * @param line the text line to be analysed
	 * @return true if line matches the leave syntax 
	 */
	public static boolean isLeave(String line)
	{
    	StringList tokens = Element.splitLexically(line, true);
		return (tokens.indexOf(CodeParser.getKeyword("preLeave"), !CodeParser.ignoreCase) == 0);
	}
	/**
	 * Checks whether this element contains a leave statement
	 * @return true if this has one line and matches the leave syntax 
	 */
	public boolean isLeave()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.getLongString().trim().isEmpty() || this.text.count() == 1 && isLeave(this.text.get(0).trim());
		StringList lines = this.getUnbrokenText();
		return lines.getLongString().trim().isEmpty() || lines.count() == 1 && isLeave(lines.get(0).trim());
		// END KGU#413 2017-06-09
	}
	/**
	 * Checks whether this line contains an exit statement
	 * @param line the text line to be analysed
	 * @return true if the given line matches the exit syntax 
	 */
	public static boolean isExit(String line)
	{
    	StringList tokens = Element.splitLexically(line, true);
		return (tokens.indexOf(CodeParser.getKeyword("preExit"), !CodeParser.ignoreCase) == 0);
	}
	/**
	 * Checks whether this element contains an exit statement
	 * @return true if this has one line and matches the exit syntax 
	 */
	public boolean isExit()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && isExit(this.text.get(0).trim());
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && isExit(lines.get(0));
		// END KGU#413 2017-06-09
	}
	// END KGU#354 2017-03-03
	
	// START KGU#78/KGU#365 3017-04-14: Enh. #23, #380: leave analyses unified here
	/**
	 * In case of a leave jump returns the specified number of loop levels to leave,
	 * otherwise 0.
	 * If the returned level specification is not a positive integer value then a
	 * negative value will be returned.  
	 * @return number of loop levels > 0 or 0 (wrong JUmp type) or negative (wrong specification)
	 */
	public int getLevelsUp()
	{
		int levelsUp = 0;
		if (this.isLeave()) {
			// START KGU#413 2017-06-09: Enh. #416 - cope with user-broken lines
			//StringList tokens = Element.splitLexically(getText().get(0), true);
			StringList tokens = Element.splitLexically(getUnbrokenText().get(0), true);
			// END KGU#413 2017-06-09
			if (tokens.count() > 0) {
				tokens.remove(0);
			}
			String expr = tokens.concatenate().trim();
			if (expr.isEmpty()) {
				levelsUp = 1;
			}
			else {
				try {
					if ((levelsUp = Integer.parseInt(expr)) == 0) {
						levelsUp = -1;
					}
				}
				catch (NumberFormatException ex) {
					levelsUp = -1;
				}
			}
		}
		return levelsUp;
	}
	
	/**
	 * Returns the outermost loop this leave Jump intends to leave or null if it
	 * is no leave Jump or if the loop level specification is wrong.
	 * @param _scope
	 * @return either the outermost one of the left loops or null
	 * @see #isLeave()
	 * @see #getLevelsUp()
	 * @see #getLeftStructures(Subqueue, boolean, boolean)
	 */
	public Element getLeftLoop(Subqueue _scope)
	{
		Element leftLoop = null;
		List<Element> leftLoopChain = getLeftStructures(_scope, false, false);
		if (leftLoopChain != null && !leftLoopChain.isEmpty()) {
			int levelsUp = this.getLevelsUp();
			if (levelsUp > 0 && leftLoopChain.size() >= levelsUp) {
				leftLoop = leftLoopChain.get(levelsUp-1);
			}
		}
		return leftLoop;
	}
	
	/**
	 * Identifies the chain of relevant structured elements (loops, actually, i.e. elements
	 * implementing the {@link ILoop} interface) to be left by this Jump.
	 * The result will be null if this is not a Jump of leave flavour.<br/>
	 * The result may contain less loop elements than specified if the actual nesting
	 * depth falls short of the specified number of if the given {@code _scope} or a
	 * {@link Parallel} element limit the reachable hierarchy.<br/>
	 * If {@code _includeCase} is true then {@link Case} structures along the path are
	 * also included in the resulting list, which may cause that the length of the
	 * result may be greater than the specified number of levels to leave.<br/>
	 * Likewise, if {@code _addLimitingParallel} is true and the reach is limited by
	 * an enclosing {@link #Parallel} element then this stopper will be appended to the
	 * result instead of further loops. 
	 * @param _scope - a {@link Subqueue} limiting the hierarchy path or null
	 * @param _includeCase - specifies whether enclosing {@link Case} structures are
	 * also to be inserted into the list.
	 * @param _addLimitingParallel - specifies whether a {@link Parallel} structure
	 * blocking the upper hierarchy to be left is to be appended to the list (which
	 * will not contain as many loops as specified then).
	 * @return a list of loops (and possibly {@link Case} and {@link Parallel} elements)
	 * or {@code null}.
	 * @see #isLeave()
	 * @see #getLevelsUp()
	 * @see #getLeftLoop(Subqueue)
	 */
	public List<Element> getLeftStructures(Subqueue _scope, boolean _includeCase, boolean _addLimitingParallel)
	{
		List<Element> structuresLeft = null;
		if (this.isLeave()) {
			int levelsUp = this.getLevelsUp();
			structuresLeft = new LinkedList<Element>();
			Element parent = this.parent;
			while (levelsUp > 0 && parent != null && !(parent instanceof Root)
					&& !(parent instanceof Parallel)
					&& (_scope == null || parent != _scope))
			{
				if (parent instanceof ILoop)
				{
					structuresLeft.add(parent);
					levelsUp--;
				}
				else if (_includeCase && parent instanceof Case)
				{
					structuresLeft.add(parent);
				}
				parent = parent.parent;
			}
			if (_addLimitingParallel && parent != null && parent instanceof Parallel) {
				structuresLeft.add(parent);
			}
		}		
		return structuresLeft;
	}
	// END KGU#78/KGU#365 2017-04-14
	
}

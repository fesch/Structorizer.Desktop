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
 *      Bob Fisch       2007-12-09      First Issue
 *      Kay Gürtzig     2015-10-11/13   Comment drawing unified, breakpoints supported, colouring modified
 *      Kay Gürtzig     2015-11-14      Bugfix #31 (= KGU#82) in method copy
 *      Kay Gürtzig     2015-12-01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *      Kay Gürtzig     2016-01-03      Bugfix #87 (KGU#124) collapsing of larger instruction elements,
 *                                      Enh. #87 (KGU#122) marking of collapsed elements with icon
 *      Kay Gürtzig     2016-02-27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016-03-01      Bugfix #97 (KGU#136): fix accomplished
 *      Kay Gürtzig     2016-03-06      Enh. #77 (KGU#117): Fields for test coverage tracking added
 *      Kay Gürtzig     2016-03-12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016-04-24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016-07-06      Enh. #188: New classification methods isAssignment() etc.,
 *                                      new copy constructor to support conversion (KGU#199)
 *      Kay Gürtzig     2016-07-30      Enh. #128: New mode "comments plus text" supported
 *      Kay Gürtzig     2016-08-10      Issue #227: New classification methods for Input/Output
 *      Kay Gürtzig     2016-09-25      Enh. #253: D7Parser.keywordMap refactored
 *      Kay Gürtzig     2016-10-13      Enh. #270: Hatched overlay texture in draw() if disabled
 *      Kay Gürtzig     2016-10-15      Enh. #271: method isEmptyInput() had to consider prompt strings now.
 *      Kay Gürtzig     2016-11-22      Bugfix #296: Wrong transmutation of return and output statements
 *      Kay Gürtzig     2017-01-26      Enh. #259: First retrieval approach for variable types
 *      Kay Gürtzig     2017-01-30      Enh. #335: More sophisticated type and declaration support    
 *      Kay Gürtzig     2017-02-20      Enh. #259: Retrieval of result types of called functions enabled (q&d)
 *      Kay Gürtzig     2017-04-11      Enh. #389: Methods isImportCall() introduced (2017-07-01 undone)
 *      Kay Gürtzig     2017-06-09      Enh. #416: drawing support for broken lines and is...() method adaptation
 *      Kay Gürtzig     2017-07-03      Enh. #423: Type definition concept for record/struct types begun
 *      Kay Gürtzig     2017-09-15-28   Enh. #423: Record type definition concept nearly accomplished
 *      Kay Gürtzig     2017-12-06      Enh. #487: Drawing supports hiding of declaration sequences 
 *      Kay Gürtzig     2017-12-10/11   Enh. #487: Run data support for new display mode "Hide declarations"
 *      Kay Gürtzig     2018-01-21      Enh. #490: Replacement of DiagramController aliases on drawing
 *      Kay Gürtzig     2018-02-15      Issue #508: Workaround for large-scaled collapse symbols eclipsing the text
 *      Kay Gürtzig     2018-07-12      Bugfix #557: potential endless loop in isDeclaration(String)
 *      Bob Fisch       2018-09-08      Issue #508: Reducing top padding from E_PADDING/2 to E_PADDING/3
 *      Kay Gürtzig     2018-09-11      Issue #508: Font height retrieval concentrated to one method on Element
 *      Kay Gürtzig     2019-02-14      Enh. #680: Improved support for processing of input instructions
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-03-18      Enh. #56: "preThrow" keyword handling
 *      Kay Gürtzig     2019-11-17      Enh. #739: Support for enum type definitions
 *      Kay Gürtzig     2021-01-02      Enh. #905: Mechanism to draw a warning symbol on related DetectedError
 *      Kay Gürtzig     2021-02-04      Enh. #905, #926: Warning symbol for hidden declarations, support for backlink
 *      Kay Gürtzig     2021-02-26      Bugfix #946: Endless loop in getAssignedVarname()
 *      Kay Gürtzig     2021-11-02      Bugfix #1014: Detection of java array declarations was flawed
 *      Kay Gürtzig     2021-09-28      Issue #1091: Type definition detection mended (aliases and array types)
 *      Kay Gürtzig     2023-10-10/13   Issue #980: Declaration-related stuff revised
 *      Kay Gürtzig     2023-10-15      Bugfix #1096: More precise type and C-style declaration handling
 *      Kay Gürtzig     2024-03-15      Bugfix #1140: Function syntax check ignored the 'qualified' argument
 *      Kay Gürtzig     2024-03-22      Issue #1154: Drawing of the hatch delegated to the disabled elements
 *      Kay Gürtzig     2024-04-02      Bugfix #1156: getAssignedVarName used to return null on typed constant definitions
 *      Kay Gürtzig     2024-04-16      Bugfix #1160: Drawing of rotated elements fixed,
 *                                      issues #161, #1161: Method mayPassControl() overridden
 *      Kay Gürtzig     2025-01-17      Bugfix #1183: updateTypeMap was caught in an eternal loop by assignment
 *                                      lines like "m[i][j] <- something"
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.gui.SelectedSequence;
import lu.fisch.structorizer.parsers.CodeParser;
//import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.*;

/**
 * This Structorizer class represents a simple activity (instruction element) in a diagram.
 * Special cases are <b>input</b> and <b>output</b> instructions, which are distinguished
 * by specific initial keywords in the text.
 * 
 * @author Bob Fisch
 */
public class Instruction extends Element {
	
	// START KGU#258 2016-09-26: Enh. #253
	private static final String[] relevantParserKeys = {"input", "output", "preReturn"};
	// END KGU#258 2016-09-25
	// START KGU#413 2017-06-09: Enh. #416
	//protected static final String indentPattern = "(\\s*)(.*)";
	protected static final Pattern INDENT_PATTERN = Pattern.compile("(\\s*)(.*)");
	// END KGU#413 2017-06-09
	// START KGU#388 2017-07-03: Enh. #423
	//protected static final String TYPE_DEF_PATTERN = "^[tT][yY][pP][eE]\\s+\\w+\\s*=\\s*\\S*$";
	// END KGU#413 2017-07-03
	private static final StringList TURTLEIZER_MOVERS = StringList.explode("forward,backward,fd,bk", ",");
	
	
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
	
	public static Rect prepareDraw(Canvas _canvas, StringList _text, Element _element)
	{
		// Within the method we may reuse the matcher, as it is local
		Matcher indentMatcher = INDENT_PATTERN.matcher("");
		// START KGU#494 2018-02-15: Enh. #408
		int leftPadding = Element.E_PADDING/2;
		if (_element.isCollapsed(true)) {
			leftPadding += _element.getIcon().getIconWidth();
		}
		// END KGU#494 2018-02-15
		Rect rect = new Rect(0, 0, leftPadding + Element.E_PADDING/2, 0);
		// START KGU#227 2016-07-30: Enh. #128
		int commentHeight = 0;
		// END KGU#227 2016-07-30

		// START KGU#494 2018-09-11: Issue #508 Retrieval concentrated for easier maintenance
		//FontMetrics fm = _canvas.getFontMetrics(Element.font);
		int fontHeight = getFontHeight(_canvas.getFontMetrics(Element.font));
		// END KGU#494 2018-09-11

		// START KGU#227 2016-07-30: Enh. #128
		// START KGU#477 2017-12-06: Enh. #487
		//if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed())
		if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed(true))
		// END KGU#477 2017-12-06
		{
			Rect commentRect = _element.writeOutCommentLines(_canvas,
					0, 0, false);
			rect.right = Math.max(rect.right, commentRect.right + Element.E_PADDING);
			commentHeight = commentRect.bottom;
		}
		// END KGU#227 2016-07-30
		
		// START KGU#480 2018-01-21: Enh. #490
		if (Element.E_APPLY_ALIASES && !isSwitchTextCommentMode()) {
			_text = StringList.explode(Element.replaceControllerAliases(_text.getText(), true, false), "\n");
		}
		// END KGU#480 2018-01-21
		// START KGU#413 2017-06-09: Enh. #416
		boolean isContinuation = false;
		// END KGU#413 2017-06-09
		for(int i = 0; i < _text.count(); i++)
		{
			// START KGU#413 2017-06-09: Enh. #416
			//int lineWidth = getWidthOutVariables(_canvas, _text.get(i), _element) + Element.E_PADDING;
			String line = _text.get(i);
			if (isContinuation && indentMatcher.reset(line).matches()) {
				//String indent = line.replaceAll(indentPattern, "$1");
				//String rest = line.replaceAll(indentPattern, "$2");
				String indent = indentMatcher.group(1);
				String rest = indentMatcher.group(2);
				if (indent.length() < Element.E_INDENT) {
					line = String.format("%1$" + Element.E_INDENT + "s%2$s", indent, rest);
				}
			}
			isContinuation = line.trim().endsWith("\\");
			int lineWidth = getWidthOutVariables(_canvas, line, _element) + leftPadding + Element.E_PADDING/2;
			// END KGU#413 2017-06-09
			if (rect.right < lineWidth)
			{
				rect.right = lineWidth;
			}
		}
		rect.bottom = 2*(Element.E_PADDING/2) + _text.count() * fontHeight;
		// START KGU#227 2016-07-30: Enh. #128
		rect.bottom += commentHeight;
		// END KGU#227 2016-07-30

		return rect;
	}
	
	@Override
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRect0UpToDate) return rect0;
		// END KGU#136 2016-03-01

		// START KGU#477 2017-12-06: Enh. #487 - if being a hidden declaration, don't show
		if (this != this.getDrawingSurrogate(false)) {
			rect0 = new Rect(0, 0, 0, 0);
			// START KGU#136 2016-03-01: Bugfix #97
			isRect0UpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}
		// END KGU#477 2017-12-06

		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0

		// START KGU#124 2016-01-03: Large instructions should also be actually collapsed
		//rect = prepareDraw(_canvas, getText(false), this);
		StringList text = getText(false);
		// START KGU#477 2017-12-06: Enh. #487 - if being a hidden declaration, don't show
		//if (isCollapsed() && text.count() > 2) 
		if (isCollapsed(true) && text.count() > 2) 
		// END KGU#477 2017-12-06
		{
			text = getCollapsedText();
		}
		rect0 = prepareDraw(_canvas, text, this);
		// END KGU#124 2016-01-03

		// START KGU#136 2016-03-01: Bugfix #97
		isRect0UpToDate = true;
		// END KGU#136 2016-03-01
		return rect0;
	}

	/**
	 * Draws {@code _element} like an Instruction in the bounds of @{@link Rect} {@code _top_left}
	 * with the given {@code _text} on the {@code _canvas}.<br/>
	 * Sets {@link Element#rect} and {@link Element#topLeft} on {@code _element}, obeys the flags
	 * {@link Element#rotated} and {@code Element#disabled}.
	 * @param _canvas - The drawing {@link Canvas}
	 * @param _top_left - the given shape and position
	 * @param _text - the text to be written into the element area
	 * @param _element - the originating {@link Element}.
	 * @param _inContention - whether this drawing is done under heavy contention
	 */
	public static void draw(Canvas _canvas, Rect _top_left, StringList _text, Element _element, boolean _inContention)
	{
		// Within the method we may reuse the matcher, as it is local
		Matcher indentMatcher = INDENT_PATTERN.matcher("");
		Rect myrect = new Rect();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = _element.getColor();
		Color drawColor = _element.getFillColor();
		// END KGU 2015-10-13
		// START KGU#494 2018-09-11: Issue #508 Retrieval concentrated for easier maintenance
		//FontMetrics fm = _canvas.getFontMetrics(Element.font);
		int fontHeight = getFontHeight(_canvas.getFontMetrics(Element.font));
		// END KGU#494 2018-09-11
			
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

		// START KGU#1150 2024-04-16: Bugfix #1160 This must be drawn before rotation
		canvas.fillRect(_top_left);
		// START KGU 2015-10-11: If _element is a breakpoint, mark it
		_element.drawBreakpointMark(canvas, _top_left);
		// END KGU 2015-10-11
		
		// START KGU#906 2021-01-02: Enh. #905
		_element.drawWarningSignOnError(canvas, _top_left);
		// END KGU#906 2021-01-02
		// END KGU#1150 2024-04-16

		myrect = _top_left.copy();
		
		AffineTransform oldTrans = null;
		if (_element.rotated) {
			oldTrans = _canvas.rotateLeftAround(myrect.left, myrect.top);
			myrect.left -= (_top_left.bottom - _top_left.top);
			myrect.right -= (_top_left.right - _top_left.left);
			myrect.bottom = myrect.top + _top_left.right - _top_left.left;
		}
		
		// START KGU#1150 2024-04-16: Bugfix #1160 This should be done before rotation
		//canvas.fillRect(myrect);
		// END KGU#1150 2024-04-16
				
		// draw comment indicator
		if (Element.E_SHOWCOMMENTS && !_element.getComment(false).getText().trim().equals(""))
		{
			_element.drawCommentMark(canvas, myrect);
		}
		
//		// START KGU 2015-10-11: If _element is a breakpoint, mark it
//		_element.drawBreakpointMark(canvas, _top_left);
//		// END KGU 2015-10-11
//		
//		// START KGU#906 2021-01-02: Enh. #905
//		_element.drawWarningSignOnError(canvas, _top_left);
//		// END KGU#906 2021-01-02

		// START KGU#227 2016-07-30: Enh. #128
		int commentHeight = 0;
		//if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed())
		if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed(true))
		// END KGU#477 2017-12-06
		{
			Rect commentRect = _element.writeOutCommentLines(canvas,
//					_top_left.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
//					_top_left.top + (Element.E_PADDING / 2),
					// START KGU#1150 2024-04-16: Bugfix #1160 consider rotation
					//myrect.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
					//myrect.top + (Element.E_PADDING / 2),
					myrect.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffsetX(),
					myrect.top + (Element.E_PADDING / 2) + _element.getTextDrawingOffsetY(),
					// END KGU#1150 2024-04-16
					true);
			commentHeight = commentRect.bottom - commentRect.top;
		}
		// START BOB## 2018-09-08: Issue  #508
		//int yTextline = _top_left.top + (Element.E_PADDING / 3) + commentHeight/* + fontHeight*/;
		// START KGU#1150 2024-04-16: Bugfix #1160 consider rotation
		//int yTextline = _top_left.top + (Element.E_PADDING / 2) + commentHeight/* + fontHeight*/;
		int yTextline = _top_left.top + (Element.E_PADDING / 2) + commentHeight
				+ _element.getTextDrawingOffsetY();
		// END KGU#1150 2024-04-16
		// END BOB## 2018-09-08
		// END KGU#227 2016-07-30
		
		// START KGU#480 2018-01-21: Enh. #490
		if (Element.E_APPLY_ALIASES && !isSwitchTextCommentMode()) {
			_text = StringList.explode(Element.replaceControllerAliases(_text.getText(), true, Element.E_VARHIGHLIGHT), "\n");
		}
		// END KGU#480 2018-01-21
		// START KGU#494 2018-02-15: Enh. #408
		int leftPadding = Element.E_PADDING/2;
		if (_element.isCollapsed(true)) {
			leftPadding += _element.getIcon().getIconWidth();
		}
		// END KGU#494 2018-02-15
		// START KGU#413 2017-06-09: Enh. #416
		boolean isContinuation = false;
		// END KGU#413 2017-06-09
		for (int i = 0; i < _text.count(); i++)
		{
			String text = _text.get(i);
			// START KGU#413 2017-06-09: Enh. #416
			if (isContinuation && indentMatcher.reset(text).matches()) {
				//String indent = text.replaceAll(indentPattern, "$1");
				//String rest = text.replaceAll(indentPattern, "$2");
				String indent = indentMatcher.group(1);
				String rest = indentMatcher.group(2);
				if (indent.length() < Element.E_INDENT) {
					text = String.format("%1$" + Element.E_INDENT + "s%2$s", indent, rest);
				}
			}
			isContinuation = text.trim().endsWith("\\");
			// END KGU#413 2017-06-09
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
//					_top_left.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
					// START KGU#1150 2024-04-16: Bugfix #1160 consider rotation
					//myrect.left + leftPadding + _element.getTextDrawingOffset(),
					myrect.left + leftPadding + _element.getTextDrawingOffsetX(),
					// END KGU#1150 2024-04-16
					// START KGU#227 2016-07-30: Enh. #128
					//_top_left.top + (Element.E_PADDING / 2) + (i+1)*fontHeight,
					yTextline += fontHeight,
					// END KGU#227 2016-07-30
					text,
					_element,
					_inContention
					);  	

		}
		// END KGU#227 2016-07-30

		// START KGU#156 2016-03-11: Enh. #124
		// write the run-time info if enabled
//		_element.writeOutRuntimeInfo(_canvas, _top_left.left + _element.rect.right - (Element.E_PADDING / 2), _top_left.top);
		int width = (_element.rotated ? _element.rect.bottom : _element.rect.right);
		_element.writeOutRuntimeInfo(_canvas, myrect.left + width - (Element.E_PADDING / 2), _top_left.top);
		// END KGU#156 2016-03-11
				
		canvas.setColor(Color.BLACK);
		if (_element.haveOuterRectDrawn())
		{
//			canvas.drawRect(_top_left);
			canvas.drawRect(myrect);
			// START KGU#277 2016-10-13: Enh. #270
			if (_element.isDisabled(true)) {
//				canvas.hatchRect(_top_left, 5, 10);
				// START KGU#1142 2024-03-22: Issue #1154 Allow element-specific adaptation
				//canvas.hatchRect(myrect, 5, 10);
				_element.drawHatched(myrect, canvas);
				// END KGU#1142 2024-03-22
			}
			// END KGU#277 2016-10-13
		}
		// START KGU#122 2016-01-03: Enh. #87 - A collapsed element is to be marked by the type-specific symbol,
		// unless it's an Instruction offspring in which case it will keep its original style, anyway.
		// START KGU#477 2017-12-06: Enh. #487 - option to hide mere declarations
		//if (_element.isCollapsed() && !(_element instanceof Instruction))
		if (_element.isCollapsed(true) && (!(_element instanceof Instruction) || ((Instruction)_element).eclipsesDeclarations(false)))
		// END KGU#477 2017-12-06
		{
//			canvas.draw(_element.getIcon().getImage(), _top_left.left, _top_left.top);
			canvas.draw(_element.getIcon().getImage(), myrect.left, myrect.top);
		}
		// END KGU#122 2016-01-03
		
		if (oldTrans != null) {
			canvas.setTransform(oldTrans);
		}
	}

	@Override
	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention)
	{
		// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
		if (!checkVisibility(_viewport, _top_left)) { return; }
		// END KGU#502/KGU#524/KGU#553 2019-03-13

		// Now delegates all stuff to the static method above, which may also
		// be called from Elements of different types when those are collapsed
		
		// START KGU#477 2017-12-06: Enh. #487: Don't draw at all if there is a drawing surrogate
		if (E_HIDE_DECL && this != this.getDrawingSurrogate(false)) {
			rect = new Rect(0,0,0,0);
			// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
			wasDrawn = true;
			// END KGU#502/KGU#524/KGU#553 2019-03-14
			return;
		}
		// END KGU#477 2017-12-06
		
		// START KGU#124 2016-01-03: Large instructions should also be actually collapsed
		//draw(_canvas, _top_left, getText(false), this);
		// START KGU#477 2017-12-06: Enh. #487
		//if (isCollapsed() && getText(false).count() > 2)
		if (isCollapsed(true) && getText(false).count() > 2)
			// END KGU#477 2017-12-06
		{
			draw(_canvas, _top_left, getCollapsedText(), this, _inContention);
		}
		else
		{
			draw(_canvas, _top_left, getText(false), this, _inContention);
		}
		// END KGU#124 2016-01-03
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
		wasDrawn = true;
		// END KGU#502/KGU#524/KGU#553 2019-03-14
	}
	
	// START KGU#477 2017-12-06: Enh. #487
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#setSelected(boolean)
	 */
	@Override
	public Element setSelected(boolean _sel)
	{
		if (this.isMereDeclaratory()) {
			SelectedSequence flock = (SelectedSequence)this.getEclipsedDeclarations(false);
			if (flock != null) {
				return flock.setSelected(_sel);
			}
		}
		return super.setSelected(_sel);
	}
	// END KGU#477 2017-12-06

	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	@Override
	public Element findSelected()
	{
		// START KGU#477 2017-12-06: Enh. #487
		if (this.eclipsesDeclarations(false)) {
			return (SelectedSequence)this.getEclipsedDeclarations(false);
		}
		// END KGU#477 2017-12-06
		return selected ? this : null;
	}
	// END KGU#183 2016-04-24

	@Override
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
		super.copyDetails(_ele, _simplyCoveredToo);
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			if (_forConversion)	// This distinction wasn't clear here: why?
			{
				_ele.execStepCount = this.execStepCount;
				_ele.execSubCount = this.execSubCount;
			}
		}
		return _ele;
	}

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
	protected void addFullText(StringList _lines, boolean _instructionsOnly)
	{
		if (!this.isDisabled(false)) {
			// START KGU#413 2017-06-09: Enh. #416 cope with user-inserted line breaks
			//_lines.add(this.getText());
			// START KGU#388 2017-09-13: Enh. #423: We must not add type definition lines
			//_lines.add(this.getUnbrokenText());
			StringList myLines = this.getUnbrokenText();
			for (int i = 0; i < myLines.count(); i++) {
				String line = myLines.get(i);
				if (!isTypeDefinition(line, null)) {
					// START KGU#1090 2023-10-15: Bugfix #1096
					// In case of a C-type declaration remove the type specification
					line = removeCDeclType(line);
					// END KGU#1090 2023-10-15
					_lines.add(line);
				}
				// START KGU#542 2019-11-17: Enh. #739 special treatment for enum type definitions
				else if (isEnumTypeDefinition(line)) {
					Root myRoot = getRoot(this);
					HashMap<String, String> constVals = myRoot.extractEnumerationConstants(line);
					if (constVals != null) {
						// FIXME If this interferes then we might generate singular constant definition lines
						//this.constants.putAll(constVals);
						for (Entry<String, String> enumItem: constVals.entrySet()) {
							_lines.add("const " + enumItem.getKey() + " <- " + enumItem.getValue());
						}
					}
				}
				// END KGU#542 2019-11-17
			}
			// END KGU#388 2017-09-13
			// END KGU#413 2017-06-09
		}
	}
	// END KGU 2015-10-16

	// START KGU#1090 2023-10-15: Bugfix #1096 publicly provided for consistency, e.g. in Root.getVarNames()
	/**
	 * Removes the type specification from the line if it's a C or Java style declaration.
	 * This is to help extract assigned variable names in syntactically complicated
	 * cases like "{@code elem_type[size1] var_name [size2] <- array_initialiser}" or
	 * "{@code int one, two, three <- 4}".<br/>
	 * 
	 * @param line - the instruction line
	 * @return a mutilated line (in the example: "{@code var_name <- array_initialiser}")
	 */
	public static String removeCDeclType(String line) {
		if (isDeclaration(line)
				&& !line.toLowerCase().startsWith("var ")
				&& !line.toLowerCase().startsWith("dim ")) {
			// Must be C-type declaration, so drop all confusing type prefix
			StringList tokens = Element.splitLexically(line, true);
			tokens.removeAll(" ");
			String varName = getAssignedVarname(tokens, false);
			int posVar = tokens.indexOf(varName);
			// FIXME: What to do in case of varName = null?
			if (posVar > 0) {
				int posAsgn = tokens.indexOf("<-");
				if (posAsgn > posVar) {
					tokens.remove(posVar+1, posAsgn);
				}
				line = tokens.concatenate(null, posVar).trim();
			}
			else if (varName == null) {
				// Ambiguous initialisation? Wipe all off.
				line = "";
			}
		}
		return line;
	}
	// END KGU#1090 2023-10-15

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
	
	// START KGU#199 2016-07-06: Enh. #188 - new classification methods.
	// There is always a pair of a static and an instance method, the former for
	// a single line, the latter for the element as a whole.
	/** @return true iff the given line contains an assignment symbol */
	public static boolean isAssignment(String line)
	{
		StringList tokens = Element.splitLexically(line, true);
		unifyOperators(tokens, true);
		// START KGU#689 2019-03-21: Issue #706 we should better cope with named parameter assignment
		//return tokens.contains("<-");
		boolean isAsgnmt = tokens.contains("<-");
		if (isAsgnmt) {
			// First eliminate all index expressions, function arguments etc.
			tokens = coagulateSubexpressions(tokens);
			// Now try again
			isAsgnmt = tokens.contains("<-");
		}
		return isAsgnmt;
		// END KGU#689 2019-03-21
	}
	
	/** @return true if this element consists of exactly one instruction line and the line complies to {@link #isAssignment(String)} */
	public boolean isAssignment()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && Instruction.isAssignment(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && Instruction.isAssignment(lines.get(0));
		// END KGU#413 2017-06-09
	}
	
	/** @return true iff the given {@code line} starts with one of the configured EXIT keywords */
	public static boolean isJump(String line)
	{
		StringList tokens = Element.splitLexically(line, true);
		// FIXME: These tests might be too simple if the keywords don't comply with identifier syntax
		return (tokens.indexOf(CodeParser.getKeyword("preReturn"), !CodeParser.ignoreCase) == 0 ||
				tokens.indexOf(CodeParser.getKeyword("preLeave"), !CodeParser.ignoreCase) == 0 ||
				// START KGU#686 2019-03-18: Enh. #56 new flavour, for try/catch
				tokens.indexOf(CodeParser.getKeyword("preThrow"), !CodeParser.ignoreCase) == 0 ||
				// END KGU#686 2019-03-18
				tokens.indexOf(CodeParser.getKeyword("preExit"), !CodeParser.ignoreCase) == 0
				);
	}
	/** @return true if this element is empty or consists of exactly one line and the line complies to {@link #isJump(String)} */
	public boolean isJump()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 0 || this.text.count() == 1 && Instruction.isJump(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		return lines.isEmpty() || lines.count() == 1 && Instruction.isJump(lines.get(0));
		// END KGU#413 2017-06-09
	}
	
	/**
	 * Checks whether the given {@code line} consists of a procedure call (or method
	 * invocation without result assignment).
	 * @param withQualifiers - whether a qualified procedure name is also to be accepted.
	 * @return true iff the given {@code line} has the syntax of a procedure invocation */
	public static boolean isProcedureCall(String line, boolean withQualifiers)
	{
		// START KGU#298 2016-11-22: Bugfix #296 - unawareness had led to wrong transmutations
		//Function fct = new Function(line);
		//return fct.isFunction();
		// START KGU#959 2021-03-05: Bugfix #961 allow method checks
		//return !isJump(line) && !isOutput(line) && Function.isFunction(line);
		return !isJump(line) && !isOutput(line) && Function.isFunction(line, withQualifiers);
		// END KGU#959 2021-03-05
		// END KGU#298 2016-11-22
	}
	/**
	 * Checks whether this element logically consists a single procedure call line.
	 * @param withQualifiers - whether qualified names are also to be accepted.
	 * @return true iff {@code this} has exactly one instruction line and the line complies
	 *  to {@link #isProcedureCall(String, boolean)} */
	public boolean isProcedureCall(boolean withQualifiers)
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && Instruction.isProcedureCall(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		// START KGU#959 2021-03-05: Bugfix #961 allow method checks
		//return lines.count() == 1 && Instruction.isProcedureCall(lines.get(0));
		return lines.count() == 1 && Instruction.isProcedureCall(lines.get(0), withQualifiers);
		// END KGU#959 2021-03-05
		// END KGU#413 2017-06-09
	}

	// START #274 2016-10-16 (KGU): Improved support for Code export
	/** @return true iff the given {@code line} contains a {@code forward}, {@code backward}, {@code fd}, or {@code bk} procedure call */
	public static boolean isTurtleizerMove(String line)
	{
		Function fct = new Function(line);
		return fct.isFunction() && TURTLEIZER_MOVERS.contains(fct.getName(), false) && fct.paramCount() == 1;
	}
	// END #274 2016-10-16
	
	/**
	 * Checks whether the given {@code line} is an assignment with a function or method
	 * invocation as expression.
	 * @param withQualifiers - if {@code true} then qualified function names will also be
	 * accepted (otherwise not)
	 * @return {@code true} iff the given {@code line} is an assignment with a function
	 * invocation as expression */
	public static boolean isFunctionCall(String line, boolean withQualifiers)
	{
		boolean isFunc = false;
		StringList tokens = Element.splitLexically(line, true);
		unifyOperators(tokens, true);
		int asgnPos = tokens.indexOf("<-");
		if (asgnPos > 0)
		{
			// START KGU#959 2021-03-05: Bugfix #961 allow method checks
			//isFunc = Function.isFunction(tokens.concatenate("", asgnPos+1));
			isFunc = Function.isFunction(tokens.concatenate("", asgnPos+1), withQualifiers);
			// END KGU#959 2021-03-05
		}
		return isFunc;
	}
	/**
	 * Checks whether this element contains exactly one line and that assigns the value
	 * of a function (or method) call to the target variable.
	 * 
	 * @param withQualifiers - whether qualified names are also to be accepted.
	 * @return true iff {@code this} consists of exactly one instruction line and this
	 * line complies to {@link #isFunctionCall(String, boolean)} */
	public boolean isFunctionCall(boolean withQualifiers)
	{
		StringList lines = this.getUnbrokenText();
		// START KGU#1126 2024-03-15: Bugfix #1140 wrong argument passing
		//return lines.count() == 1 && Instruction.isFunctionCall(lines.get(0), false);
		return lines.count() == 1 && Instruction.isFunctionCall(lines.get(0), withQualifiers);
		//END KGU#1126 2024-03-15
	}
	// END KGU#199 2016-07-06
	
	// START KGU#236 2016-08-10: Issue #227: New classification for input and output
	/** @return true iff the given {@code line} represents an output instruction */
	public static boolean isOutput(String line)
	{
		StringList tokens = Element.splitLexically(line, true);
		return (tokens.indexOf(CodeParser.getKeyword("output"), !CodeParser.ignoreCase) == 0);
	}
	/** @return true if at least one of the instruction lines of {@code this} complies to {@link #isOutput(String)} */
	public boolean isOutput()
	{
		StringList lines = this.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++)
		{
			if (isOutput(lines.get(i)))
			{
				return true;
			}
		}
		return false;
	}
	
	// START KGU#653 2019-02-14: Enh. #680 - better support for input instructions
	/**
	 * Checks whether the given instruction line is an input instruction. If so, decomposes
	 * it into the specified input prompt (may be empty) and the expressions identifying the
	 * target variables (the first variable description will be at index 1, the resulting
	 * StringList of an empty input instruction will have length 1). Otherwise the result
	 * will be null.
	 * @param line - the instruction line (assumed to have been trimmed)
	 * @return a {@link StringList} containing the input items (prompt + variables) or null
	 * @see #isInput(String)
	 * @see #isEmptyInput(String)
	 */
	public static StringList getInputItems(String line)
	{
		StringList inputItems = null;
		StringList tokens = Element.splitLexically(line, true);
		StringList keyTokens = Element.splitLexically(CodeParser.getKeyword("input"), false);
		if (tokens.indexOf(keyTokens, 0, !CodeParser.ignoreCase) == 0) {
			// It is an input instruction
			inputItems = new StringList();
			tokens.remove(0, keyTokens.count());	// Remove the keyword
			tokens.removeAll(" ");					// remove whitespace
			// Identify the prompt if any
			if (tokens.isEmpty()) {
				inputItems.add(""); 
			}
			else {
				String token0 = tokens.get(0);
				if (token0.length() > 1 &&
						(token0.startsWith("\"") && token0.endsWith("\"") || token0.startsWith("'") && token0.endsWith("'"))) {
					inputItems.add(token0);
					tokens.remove(0);
					if (tokens.count() > 0 && tokens.get(0).equals(",")) {
						tokens.remove(0);
					}
				}
				else {
					// No prompt string
					inputItems.add("");
				}
			}
			// Now extract the target variables
			StringList exprs = Element.splitExpressionList(tokens, ",", false);
			exprs.removeAll("");
			inputItems.add(exprs);
		}
		return inputItems;
	}
	
	/** @return true iff the given {@code line} is an input instruction */
	public static boolean isInput(String line)
	{
		return getInputItems(line) != null;
	}
	/** @return true if at least one instruction line of {@code this} complies to {@link #isInput(String)} */
	public boolean isInput()
	{
		StringList lines = this.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++)
		{
			if (isInput(lines.get(i)))
			{
				return true;
			}
		}
		return false;
	}

	/** @return true iff the given instruction {@code line} is an input instruction without target variables */
	public static boolean isEmptyInput(String line)
	{
		StringList tokens = getInputItems(line);
		return tokens != null && tokens.count() <= 1;
	}
	/** @return true if at least on of the instruction lines of {@code this} complies to {@link #isEmptyInput(String)} */
	public boolean isEmptyInput()
	{
		StringList lines = this.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++)
		{
			if (isEmptyInput(lines.get(i)))
			{
				return true;
			}
		}
		return false;
	}	
	// END KGU#236 2016-08-10
	
	// START KGU#322 2016-07-06: Enh. #335
	/**
	 * Returns true if the current line of code is a variable declaration of one of
	 * the following types:<br/>
	 * a) var &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt; [&lt;- &lt;expr&gt;]<br/>
	 * b) dim &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt; [&lt;- &lt;expr&gt;]<br/>
	 * c) &lt;type&gt; &lt;id&gt; &lt;- &lt;expr&gt;
	 * 
	 * @param line - String comprising one line of code
	 * @return true iff line is of one of the forms a), b), c)
	 * 
	 * @see #isDeclaration(String, boolean)
	 */
	public static boolean isDeclaration(String line)
	// START KGU#1143 2024-04-02: Issue #1156 An extended check for typed constants was useful
	{
		return isDeclaration(line, false);
	}
	/**
	 * Returns true if the current line of code is a variable declaration of one of
	 * the following types, where d) is only considered if {@code constantToo} is
	 * {@code true}:<br/>
	 * a) var &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt; [&lt;- &lt;expr&gt;]<br/>
	 * b) dim &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt; [&lt;- &lt;expr&gt;]<br/>
	 * c) &lt;type&gt; &lt;id&gt; &lt;- &lt;expr&gt;<br/>
	 * d) const &lt;id&gt; : &lt;type&gt; &lt;- &lt;expr&gt;
	 * 
	 * @param line - String comprising one line of code
	 * @param constantToo - whether type d) shall also lead to a {@code true} result
	 * @return true iff line is of one of the forms a), b), c) - or d) if
	 *    {@code constantToo} is {@code true}.
	 */
	public static boolean isDeclaration(String line, boolean constantToo)
	// END KGU#1143 2024-04-02
	{
		StringList tokens = Element.splitLexically(line, true);
		unifyOperators(tokens, true);
		// START KGU#1089 2023-10-12: Bugfix #980 Accept uppercase, too
		//boolean typeA = tokens.indexOf("var") == 0 && tokens.indexOf(":") > 1;
		//boolean typeB = tokens.indexOf("dim") == 0 && tokens.indexOf("as") > 1;
		boolean typeA = tokens.indexOf("var", false) == 0 && tokens.indexOf(":") > 1;
		boolean typeB = tokens.indexOf("dim", false) == 0 && tokens.indexOf("as", false) > 1;
		// END KGU#1089 2023-10-13
		int posAsgn = tokens.indexOf("<-");
		boolean typeC = false;
		// START KGU#1143 2024-04-02: Issue #1156 extended support for typed constants
		boolean typeD = false;
		// END KGU#1143 2024-04-02
		if (posAsgn > 1) {
			tokens = tokens.subSequence(0, posAsgn);
			tokens.removeAll(" ");
			typeC = !Instruction.getDeclaredVariables(tokens).isEmpty();
			// START KGU#1143 2024-04-02: Issue #1156 extended support for typed constants
			typeD = constantToo
					&& tokens.indexOf("const", false) == 0
					&& tokens.indexOf(":") == 2
					&& Function.testIdentifier(tokens.get(1), false, null);
			// END KGU#1143 2024-04-02
		}
		// START KGU#1143 2024-04-02: Issue #1156 extended support for typed constants
		//return typeA || typeB || typeC;
		return typeA || typeB || typeC || typeD;
		// END KGU#1143 2024-04-02
	}
	/**
	 * @return {@code true} if all non-empty lines are declarations
	 * 
	 * @see #isDeclaration(String)
	 * @see #isDeclaration(String, boolean)
	 */
	public boolean isDeclaration()
	{
		boolean isDecl = true;
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//for (int i = 0; isDecl && i < this.text.count(); i++) {
		//	String line = this.text.get(i).trim();
		//	isDecl = line.isEmpty() || isDeclaration(line);
		//}
		StringList lines = this.getUnbrokenText();
		for (int i = 0; isDecl && i < lines.count(); i++) {
			String line = lines.get(i);
			isDecl = line.isEmpty() || isDeclaration(line);
		}
		// END KGU#413 2017-06-09
		return isDecl;
	}
	/**
	 * @return {@code true}  if at least one line of {@code this} complies to
	 * {@link #isDeclaration(String)}
	 * 
	 * @see #isDeclaration(String)
	 * @see #isDeclaration(String, boolean)
	 */
	public boolean hasDeclarations()
	{
		boolean hasDecl = false;
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//for (int i = 0; !hasDecl && i < this.text.count(); i++) {
		//	String line = this.text.get(i).trim();
		//	hasDecl = !line.isEmpty() && isDeclaration(line);
		//}
		StringList lines = this.getUnbrokenText();
		for (int i = 0; !hasDecl && i < lines.count(); i++) {
			String line = lines.get(i);
			hasDecl = !line.isEmpty() && isDeclaration(line);
		}
		// END KGU#413 2017-06-09
		return hasDecl;
	}
	// END KGU#332 2017-01-27
	
	// START KGU#1089 2203-10-16: Issue #980
	/**
	 * Returns the list of variable names potentially declared in the line
	 * represented by the token list {@code _tokens}. This is either the list
	 * of names between a {@code var}/{@code :} or {@code dim}/{@code as}
	 * pair (Pascal/BASIC style) or the names of variables in a potential
	 * C/Java-style declaration.<br/>
	 * In a C/Java-style declaration, the type specification is assumed to
	 * start the line. It may consist of one or more names (id syntax),
	 * followed by zero or more index brackets. Type constructions like
	 * "record{...}", "struct{...}", "enum{...}", or "array..." are not
	 * tolerated.<br/>
	 * The syntax of a declared variable assumes exactly one (unqualified)
	 * name, possibly followed by one or more index brackets. Pointer symbols
	 * like '*' or '^' or the like are not tolerated. Several variable
	 * specifications might be listed with comma separation but will not
	 * be effective since a C/Java-style declaration is only accepted with
	 * an initialisation, which excludes multiple declared variables per
	 * line.<br/>
	 * An initialisation part has no effect on the result, however, even
	 * if it makes a multi-variable declaration invalid.
	 *  
	 * @param _tokens - the lexically split instruction line potentially being
	 *     a declaration, <b>must be blank-free!</b>
	 * @return a list of variable names, possibly empty.
	 */
	public static StringList getDeclaredVariables(StringList _tokens)
	{
		StringList declVars = new StringList();
		if (_tokens.indexOf("var", false) == 0 || _tokens.indexOf("dim", false) == 0) {
			int posColon = _tokens.indexOf(":", 2);
			if (posColon > 1 || (posColon = _tokens.indexOf("as", false)) > 1) {
				declVars = Element.splitExpressionList(_tokens.subSequence(1, posColon), ",", true);
				int nVars = declVars.count()-1;
				if (!declVars.get(nVars).isEmpty()) {
					// Syntax error
					declVars.clear();
					nVars = 0;
				}
				else {
					declVars.remove(nVars);
				}
				for (int i = nVars - 1; i >= 0; i--) {
					if (!Function.testIdentifier(declVars.get(i), false, null)) {
						declVars.remove(i);
						nVars--;
					}
				}
			}
		}
		else {
			int posAsgn = _tokens.indexOf("<-");
			if (posAsgn < 0 && (posAsgn = _tokens.indexOf(":=")) < 0) {
				posAsgn = _tokens.count();
			}
			// It takes at least two tokens to form a C/Java-style declaration
			if (posAsgn > 1) {
				_tokens = Element.coagulateSubexpressions(_tokens.subSequence(0, posAsgn));
				int i = _tokens.count()-1;
				while (i > 0) {
					// Possible declared variable, something like an id, possibly
					// followed by index specifications
					String token = _tokens.get(i);
					// Skip all end-standing index specifiers until we may reach an identifier
					while (i > 1 && token.startsWith("[")) {
						// It is a coagulated index access - skip it
						token = _tokens.get(--i);
					}
					if (Function.testIdentifier(token, false, null)) {
						// Potential variable name
						String preToken = _tokens.get(i-1);
						if (i >= 2 && _tokens.get(i-1).equals(",")) {
							// Another variable declaration might precede, not a type specification
							declVars.insert(token, 0);
							i -= 2;
						}
						else if (i > 1 && preToken.startsWith("[")
								|| i > 0 && Function.testIdentifier(preToken, false, ".")) {
							// An array type specification might precede
							declVars.insert(token, 0);
							i--;
							break;
						}
						else {
							// Any other kind of preceding stuff causes trouble
							declVars.clear();
							break;
						}
					}
					else  {
						// Anything else may hardly precede a declared variable
						declVars.clear();
						i = -1;
					}
				}
				// Now check type syntax
				while (i > 0 && !declVars.isEmpty() && _tokens.get(i).startsWith("[")) {
					// It is a coagulated index access - skip it
					i--;
				}
				// At last, there must be at least one identifier (other than const)
				int idCount = 0;
				while (i >= 0 && !declVars.isEmpty()) {
					String token = _tokens.get(i--);
					if (!Function.testIdentifier(token, false, ".")) {
						declVars.clear();
					}
					// We might exclude more here, e.g. "input", record but...
					else if (!token.equalsIgnoreCase("const")) {
						idCount++;
					}
				}
				if (idCount == 0) {
					declVars.clear();
				}
			}
		}
		return declVars;
	}
	// END KGU#1089 2023-10-16

	// START KGU#388 2017-07-03: Enh. #423
	/**
	 * Returns true if the given {@code line} of code is a type definition of one of the following forms:<br>
	 * a) type &lt;id&gt; = record{ &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt; {; &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt;} };<br>
	 * b) type &lt;id&gt; = record{ &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt; {; &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt;} };<br>
	 * c) type &lt;id&gt; = record{ &lt;type&gt; &lt;id&gt; {, &lt;id&gt;}; {; &lt;type&gt; &lt;id&gt; {, &lt;id&gt;}} };<br>
	 * d)...f) same as a)...c) but with struct instead of record;<br/>
	 * g) type &lt;id&gt; = enum{ &lt;id&gt [ = &lt;value&gt; ] {, &lt;id&gt [ = &lt;value&gt; ]} };<br/>
	 * h) type &lt;id&gt; = &lt;type&gt;;<br/>
	 * i) type &lt;id&gt; = array [...] of &lt;type&gt;;<br/>
	 * j) type &lt;id&gt; = &lt;typeid&gt[...].<br/>
	 * @param line - String comprising one line of code
	 * @return true iff line is of one of the forms a) through e)
	 * @see #isTypeDefinition(String, HashMap)
	 * @see #isTypeDefinition()
	 * @see #isEnumTypeDefinition(String)
	 */
	public static boolean isTypeDefinition(String line)
	{
		return isTypeDefinition(line, null);
	}
	/**
	 * Returns true if the given {@code line} of code is a type definition (with possibly registered type, see argument {@code typeMap})
	 * of one of the following forms:<br>
	 * a) type &lt;id&gt; = record{ &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt; {; &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt;} };<br/>
	 * b) type &lt;id&gt; = record{ &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt; {; &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt;} };<br/>
	 * c) type &lt;id&gt; = record{ &lt;type&gt; &lt;id&gt; {, &lt;id&gt;}; {; &lt;type&gt; &lt;id&gt; {, &lt;id&gt;}} };<br/>
	 * d)...f) same as a)...c) but with struct instead of record;<br/>
	 * g) type &lt;id&gt; = enum{ &lt;id&gt [ = &lt;value&gt; ] {, &lt;id&gt [ = &lt;value&gt; ]} };<br/>
	 * h) type &lt;id&gt; = &lt;typeid&gt;;<br/>
	 * i) type &lt;id&gt; = array [...] of &lt;type&gt;;<br/>
	 * j) type &lt;id&gt; = &lt;typeid&gt[...].<br/>
	 * Type names and descriptions &lt;type&gt; are checked against existing types in {@code typeMap} if given.
	 * 
	 * @param line - String comprising one line of code
	 * @param typeMap - if given then the type name must have been registered in typeMap in order to be accepted (otherwise
	 * an appropriate syntax is sufficient).
	 * @return true iff line is of one of the forms a) through e)
	 * 
	 * @see #isTypeDefinition(String)
	 * @see #isTypeDefinition(HashMap, boolean)
	 * @see #isTypeDefinition()
	 */
	public static boolean isTypeDefinition(String line, HashMap<String, TypeMapEntry> typeMap)
	{
		StringList tokens = Element.splitLexically(line.trim(), true);
		// START KGU#1090 2023-10-15: Bugfix #1096
		tokens.removeAll(" ");
		// END KGU#1090 2023-10-15
		if (tokens.isEmpty() || !tokens.get(0).equalsIgnoreCase("type")) {
			return false;
		}
		unifyOperators(tokens, true);
		int posDef = tokens.indexOf("=");
		// START KGU#1090 2023-10-15: Bugfix #1096
		//if (posDef < 2 || posDef == tokens.count()-1) {
		// The second condition checks that a type specification still follows
		if (posDef != 2 || posDef == tokens.count()-1) {
		// END KGU#1090 2023-10-15
			return false;
		}
		// START KGU#1090 2023-10-15: Bugfix #1096
		String typename = tokens.get(1);
		// END KGU#1090 2023-10-15
		tokens = tokens.subSequence(posDef+1, tokens.count());
		// START KGU#1081 2023-09-28: Enh. #1091 Accept array type definitions
		String typeDescr = tokens.concatenate().trim();
		boolean isArray = typeDescr.matches("\\w+\\s*\\[.*\\].*")
				|| typeDescr.equalsIgnoreCase("array")
				|| typeDescr.matches("^" + BString.breakup("array", false) + "((\\s*(\\[.*?\\]\\s*)+)|\\s+)" + BString.breakup("of", false) + "\\W.*");
		// END KGU#1081 2023-09-28
		// START KGU#1090 2023-10-15: Bugfix #1096 Now done at the beginning
		//tokens.removeAll(" ");
		// END KGU#1090 2023-10-15
		// START KGU#1081 2023-09-28: Enh. #1091 The type existence test is not case-ignorant
		//String tag = tokens.get(0).toLowerCase();
		String tag = tokens.get(0);
		// END KGU#1081 2023-09-28
		return Function.testIdentifier(typename, false, null) &&
				// START KGU#542 2019-11-17: Enh. #739 - also consider enumeration types
				//((tag.equals("record") || tag.equals("struct")) && tokens.get(1).equals("{") && tokens.get(tokens.count()-1).equals("}")
				((tag.equalsIgnoreCase("record") || tag.equalsIgnoreCase("struct") || tag.equalsIgnoreCase("enum"))
						&& tokens.get(1).equals("{") && tokens.get(tokens.count()-1).equals("}")
				// END KGU#542 2019-11-17
				// START KGU#1081 2023-09-28 Enh. #1091 also accept array type definitions
				//|| tokens.count() == 1 && (typeMap != null && typeMap.containsKey(":" + tag) || typeMap == null && Function.testIdentifier(tag, false, null)));
				|| isArray
				|| tokens.count() == 1 && (typeMap != null && (typeMap.containsKey(":" + tag) || TypeMapEntry.isStandardType(tag)) || typeMap == null && Function.testIdentifier(tag, false, null)));
				// END KGU#1081 2023-09-28
	}
	/** @return true if all non-empty lines comply to {@link #isTypeDefinition(String)} */
	public boolean isTypeDefinition()
	{
		return isTypeDefinition(null, true);
	}

	/**
	 * Determines if this element contains valid type definitions
	 * @param typeMap - a type map for verification of types
	 * @param allLines - if the result should only be true if all lines are type definitions
	 * @return true if this element contains type definitions
	 * @see #isTypeDefinition(String, HashMap)
	 */
	public boolean isTypeDefinition(HashMap<String, TypeMapEntry> typeMap, boolean allLines)
	{
		boolean isTypeDef = false;
		StringList lines = this.getUnbrokenText();
		if (allLines) {
			isTypeDef = true;
			for (int i = 0; isTypeDef && i < lines.count(); i++) {
				String line = lines.get(i).trim();
				isTypeDef = line.isEmpty() || isTypeDefinition(line, typeMap);
			}
		}
		else {
			for (int i = 0; !isTypeDef && i < lines.count(); i++) {
				String line = lines.get(i);
				isTypeDef = !line.isEmpty() && isTypeDefinition(line, typeMap);
			}
		}
		return isTypeDef;
	}
	/** @return true if at least one line complies to {@link #isTypeDefinition(String)} */
	public boolean hasTypeDefinitions()
	{
		return isTypeDefinition(null, false);
	}
	// END KGU#388 2017-07-03
	// START KGU#542 2019-11-17: Enh. #739 - identify enum type line
	/**
	 * Returns true if the given {@code line} of code is a type definition of the following form:<br>
	 * type &lt;id&gt; = enum{ &lt;id&gt [ = &lt;value&gt; ] {, &lt;id&gt [ = &lt;value&gt; ]} }.<br/>
	 * @param line - String comprising one line of code
	 * @return true iff line is of one of the forms a) through e)
	 * @see #isTypeDefinition(String, HashMap)
	 * @see #isTypeDefinition()
	 */
	public static boolean isEnumTypeDefinition(String line)
	{
		boolean isEnum = isTypeDefinition(line);
		if (isEnum) {
			int posEq = line.indexOf('=');
			isEnum = posEq > 0 && TypeMapEntry.MATCHER_ENUM.reset(line.substring(posEq+1).trim()).matches();
		}
		return isEnum;
	}
	// END KGU#542 2019-11-17
	
	// START KGU#47 2017-12-06: Enh. #487 - compound check for hidable content
	/**
	 * @return true iff this Instruction contains nothing but type definitions and
	 * (uninitialized) variable declarations, i.e. stuff that can be hidden.
	 * @see #isMereDeclaration(String) 
	 */
	public boolean isMereDeclaratory()
	{
		boolean isHideable = true;
		StringList lines = this.getUnbrokenText();
		for (int i = 0; isHideable && i < lines.count(); i++) {
			String line = lines.get(i);
			isHideable = line.isEmpty() || isMereDeclaration(line);
		}
		return isHideable;
	}
	// END KGU#477 2017-12-06
	// START KGU#772 2019-11-24: We want to be able to suppress expression of code for mere declarations
	/**
	 * Checks whether the given {@code _line} of code is either a type definition or
	 * a variable declaration without initialization.
	 * 
	 * @param line - instruction line
	 * @return true if the line is a mere declaration
	 * @see #isTypeDefinition(String)
	 * @see #isDeclaration(String)
	 * @see #isAssignment(String)
	 */
	public static boolean isMereDeclaration(String line)
	{
		return isTypeDefinition(line) || (isDeclaration(line) && !isAssignment(line));
	}
	// END KGU#772 2019-11-24

	// START KGU#178 2016-07-19: Support for enh. #160 (export of called subroutines)
	// (This method is plaed here instead of in class Call because it is needed
	// to decide whether an Instruction element complies to the Call syntax and
	// may be transmuted.)
	/**
	 * Returns a {@link Function} object describing the signature of the called
	 * routine if the text complies to the call syntax described in the user guide,
	 * or {@code null} otherwise.
	 * @return Function object or {@code null}.
	 * @see #isFunctionCall(boolean)
	 * @see #isProcedureCall(boolean)
	 */
	public Function getCalledRoutine() {
		if (this.getUnbrokenText().count() == 1) {
			return getCalledRoutine(0);
		}
		return null;
	}
	
	// Undocumented version of getCalledRoutine() coping with multiple call lines per element
	public Function getCalledRoutine(int lineNo)
	{
		Function called = null;
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//StringList lines = this.text;
		StringList lines = this.getUnbrokenText();
		// END KGU#413 2017-06-09
		if (lines.count() > 0 && lineNo < lines.count())
		{
			String potentialCall = lines.get(lineNo);
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
	/**
	 * Checks if this element contains a function or procedure call matching one of the given
	 * subroutine signatures.
	 * @param _signatures - a {@link StringList} of symbolic subroutine signatures {@code "<name>#<argcount>"}
	 * @return true if this contains a call matching one of the signatures given.
	 * @see #getCalledRoutine()
	 * @see #isFunctionCall(boolean)
	 * @see #isProcedureCall(boolean)
	 */
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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#traverse(lu.fisch.structorizer.elements.IElementVisitor)
	 */
	@Override
	public boolean traverse(IElementVisitor _visitor) {
		return _visitor.visitPreOrder(this) && _visitor.visitPostOrder(this);
	}

	// START KGU#258 2016-09-26: Enh. #253
	@Override
	protected String[] getRelevantParserKeys() {
		// TODO Auto-generated method stub
		return relevantParserKeys;
	}
	// END KGU#258 2016-09-26
	
	// START KGU#261 2017-01-26: Enh. #259 (type map)
	@Override
	public void updateTypeMap(HashMap<String, TypeMapEntry> typeMap)
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//for (int i = 0; i < this.getText().count(); i++) {
		//	updateTypeMapFromLine(typeMap, this.getText().get(i), i);
		//}
		StringList lines = this.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++) {
			updateTypeMapFromLine(typeMap, lines.get(i), i);
		}
		// END KGU#413 2017-06-09
	}
	
	/**
	 * Adds type map entries for the variable declarations contained in the given
	 * line to the passed-in type map (varname -> typeinfo).
	 * 
	 * @param typeMap - the type map to be used and extended
	 * @param line - the (unbroken) line to be processed
	 * @param lineNo - number of the (unbroken) {@code line} within the element text
	 */
	public void updateTypeMapFromLine(HashMap<String, TypeMapEntry> typeMap, String line, int lineNo)
	{
		StringList tokens = Element.splitLexically(line, true);
		String varName = null;
		String typeSpec = "";
		boolean isAssigned = false;
		boolean isDeclared = true;
		unifyOperators(tokens, true);
		tokens.removeAll(" ");
		if (tokens.isEmpty()) {
			return;
		}
		String token0 = tokens.get(0).toLowerCase();
		int posColon = tokens.indexOf(token0.equals("dim") ? "as" : ":", false);
		int posAsgnmt = tokens.indexOf("<-");
		// First we try to extract a type description from a Pascal-style variable declaration
		if (tokens.count() > 3 && (token0.equals("var") || token0.equals("dim") || token0.equals("const")) && posColon >= 2) {
			isAssigned = posAsgnmt > posColon;
			typeSpec = tokens.concatenate(" ", posColon+1, (isAssigned ? posAsgnmt : tokens.count()));
			// There may be more than one variable name between "var" and ':' if there is no assignment
			// START KGU#1089 2023-10-12: Issue #980 - This was for the discarded idea of array specifiers
			//StringList varTokens = tokens.subSequence(1, posColon);
			//varTokens.removeAll(" ");
			//for (int i = 0; i < varTokens.count(); i++)
			//{
			//	if (Function.testIdentifier(varTokens.get(i), false, null) && (i + 1 >= varTokens.count() || varTokens.get(i+1).equals(","))) {
			//		addToTypeMap(typeMap, varTokens.get(i), typeSpec, lineNo, isAssigned, true);
			//	}
			//}
			StringList varList = Element.splitExpressionList(tokens.subSequence(1, posColon), ",", false);
			if (!isAssigned || varList.count() == 1) {
				for (int i = 0; i < varList.count(); i++) {
					String var = varList.get(i).trim();
					// The following part addressed a mixed list of scalar and array declarations
					//StringList dims = new StringList();
					//int posBrack = var.indexOf('[');
					//if (posBrack >= 0) {
					//	String tail = var.substring(posBrack);
					//	var = var.substring(0, posBrack).trim();
					//	while (tail.startsWith("[")) {
					//		StringList ranges = Element.splitExpressionList(tail.substring(1), ",", true);
					//		dims.add(ranges.subSequence(0, ranges.count()-1));
					//		tail = ranges.get(ranges.count()-1);
					//		if (tail.length() > 1) {
					//			tail = tail.substring(1);
					//		}
					//	}
					//}
					if (Function.testIdentifier(var, false, null)) {
						String typeSpec1 = typeSpec;
						// The following part addressed a mixed list of scalar and array declarations
						//if (!dims.isEmpty()) {
						//	if (dims.count() == 1 && dims.get(0).isBlank()) {
						//		typeSpec1 = "array of " + typeSpec1;
						//	}
						//	else {
						//		typeSpec1 = "array [" + dims.concatenate(",") + "] of " + typeSpec1;
						//	}
						//}
						addToTypeMap(typeMap, var, typeSpec1, lineNo, isAssigned, true);
					}
				}
			}
			// END KGU#1089 2023-10-12
		}
		// Next we try to extract type information from an initial assignment (without "var"/"dim" keyword)
		else if (posAsgnmt > 0 && !token0.equals("var") && !token0.equals("dim")) {
			// Type information might be found left of the variable name or be derivable from the initial value
			// START KGU#375 2017-09-20: Enh. #388 - a "const" keyword might be here, drop it
			if (token0.equals("const")) {
				tokens.remove(0);
				posAsgnmt--;
			}
			// EMD KGU#375 2017-09-20
			StringList leftSide = tokens.subSequence(0, posAsgnmt);
			StringList rightSide = tokens.subSequence(posAsgnmt+1, tokens.count());
			isAssigned = !rightSide.isEmpty();
			// Isolate the variable name from the left-hand side of the assignment
			varName = getAssignedVarname(leftSide, false);
			// START KGU#1089 2023-10-16: Issue #980
//			// Without const, var, or dim, a declaration must be a C-style declaration
//			boolean isCStyleDecl = Instruction.isDeclaration(line);
//			// If the target is a record component we won't add a type specification.
//			if (varName != null && !varName.contains(".")) {
//				int pos = leftSide.indexOf(varName);
//				// C-style type declaration left of the variable name?
//				typeSpec = leftSide.concatenate(null, 0, pos);
//				// Check for array declaration (or array element access)
//				// START KGU#1090 2023-10-15: Bugfix we must not compromise the typeSpec part
//				//while (!typeSpec.isEmpty() && (pos = leftSide.indexOf("[")) > 1) {
//				//	typeSpec += leftSide.concatenate(null, pos, leftSide.indexOf("]")+1);
//				//	leftSide.remove(pos, leftSide.indexOf("]")+1);
//				//}
//				leftSide.remove(0, pos);
//				if (!typeSpec.isEmpty() && (pos = leftSide.indexOf("[")) > 0) {
//					// Left side should end with a closing bracket now
//					typeSpec = "array " + leftSide.concatenate(null, pos) + " of " + typeSpec;
//					leftSide.remove(pos, leftSide.count());
//				}
//				// END KGU#1090 2023-10-15
//				// No explicit type specification but new variable?
//				if (typeSpec.isEmpty() && !typeMap.containsKey(varName)) {
//					//String expr = rightSide.concatenate(" ");
//					typeSpec = getTypeFromAssignedValue(rightSide, typeMap);
//					isDeclared = false;
//					if (typeSpec.isEmpty()) {
//						typeSpec = "???";
//					}
//					// Maybe it's a multidimensional array, then reformulate it as "array of [array of ...]"
//					while (!typeSpec.isEmpty() && (pos = leftSide.indexOf("[")) == 1) {
//						typeSpec = "array of " + typeSpec;
//						leftSide.remove(pos, leftSide.indexOf("]")+1);
//					}
//				}
//			}
//			addToTypeMap(typeMap, varName, typeSpec, lineNo, isAssigned, isDeclared || isCStyleDecl);

			/* Without const, var, or dim, a declaration must be a C-style declaration
			 * Without a type it must be a simple assignment
			 * We put all cases in a loop to avoid duplicate code. If varName isn't
			 * null then it may be an initialisation (declVars should have one element)
			 * or an assignment to a single variable (declVars should be empty then).
			 * If declVars has more than one element then varName will be null.
			 */
			StringList declVars = Instruction.getDeclaredVariables(tokens);
			boolean isCStyleDecl = !declVars.isEmpty();
			/* In case of a multi-variable declaration we will associate the type but
			 * ignore the assignment.
			 */
			if (varName != null && declVars.isEmpty()) {
				declVars.add(varName);
			}
			if (varName == null && !declVars.isEmpty()) {
				varName = declVars.get(0);
				rightSide.clear(); // ignore the value - it is not intended to be assigned
			}
			// If the target is a record component we won't add a type specification.
			if (varName != null && !varName.contains(".")) {
				int pos = leftSide.indexOf(varName);
				// C-style type specification left of the (first) variable name?
				typeSpec = leftSide.concatenate(null, 0, pos);
				// Check for array declaration (or array element access)
				leftSide.remove(0, pos);
				// If there are several comma-separated zones (should be if declVars.count() > 1)
				StringList declZones = Element.splitExpressionList(leftSide, ",", true);
				for (int i = 0; i < declVars.count(); i++) {
					isDeclared = true;
					String typeSpec1 = typeSpec;
					String declVar = declVars.get(i);
					if (!typeSpec.isEmpty() && i < declZones.count() && (pos = declZones.get(i).indexOf("[")) > 0) {
						// Left side should end with a closing bracket now
						int posRBrace = declZones.get(i).lastIndexOf(']') + 1;
						if (posRBrace < 1) {
							posRBrace = declZones.get(i).length();
						}
						typeSpec1 = "array " + declZones.get(i).substring(pos, posRBrace) + " of " + typeSpec;
					}
					if (typeSpec1.isEmpty() && !typeMap.containsKey(declVar) && !rightSide.isEmpty()) {
						// Doesn't seem to be a declaration but an assignment
						//String expr = rightSide.concatenate(" ");
						typeSpec1 = getTypeFromAssignedValue(rightSide, typeMap);
						isDeclared = false;
						if (typeSpec1.isEmpty()) {
							typeSpec1 = "???";
						}
						StringList declZone = Element.splitLexically(declZones.get(i), true);
						// Maybe it's a multidimensional array, then reformulate it as "array of [array of ...]"
						// Don't mistake an index as a size, so better don't specify size
						while ((pos = declZone.indexOf("[")) == 1) {
							// There might be more than one index in the bracket
							// START KGU#1168 2025-10-17: Bugfix #1183 endless loop in case of "[i][j]"
							//StringList indices = Element.splitExpressionList(declZone.subSequence(2, declZone.count()), ",", true);
							declZone = declZone.subSequence(2, declZone.count());
							StringList indices = Element.splitExpressionList(declZone, ",", true);
							// END KGU#1168 2025-01-17
							if (indices.get(indices.count()-1).startsWith("]")) {
								declZone.remove(0, declZone.indexOf("]"));
							}
							else {
								declZone.clear();
							}
							typeSpec1 = "array of ".repeat(indices.count()-1) + typeSpec1;
						}
					}
					if (declVar != null) {
						addToTypeMap(typeMap, declVar, typeSpec1, lineNo, isAssigned, isDeclared || isCStyleDecl);
					}
				}
				//END KGU#1089 2023-10-16
			}
		}
		// START KU#388 2017-08-07: Enh. #423
		else if (isTypeDefinition(line, typeMap)) {
			// START KGU#542 2019-11-17: Enh. #739
			boolean isEnum = tokens.get(3).equalsIgnoreCase("enum");
			// END KGU#542 2019-11-17
			// FIXME: In future, array type definitions will also have to be handled...
			String typename = tokens.get(1);
			// Because of possible C-style declarations we must not glue the tokens together with "".
			typeSpec = tokens.concatenate(null, 3, tokens.count()).trim();
			int posBrace = typeSpec.indexOf("{");
			if (posBrace > 0 && tokens.get(tokens.count()-1).equals("}")) {
				// START KGU#542 2019-11-17: Enh. #739 Handle enumeration tapes
				if (isEnum) {
					// first make sure the syntax is okay
					if (TypeMapEntry.MATCHER_ENUM.reset(typeSpec).matches() ) {
						Root root = getRoot(this);
						if (root != null) {
							TypeMapEntry enumType = new TypeMapEntry(typeSpec, typename, typeMap, this, lineNo, false, false);
							typeMap.put(":" + typename, enumType);
							HashMap<String, String> enumItems = root.extractEnumerationConstants(line);
							if (enumItems != null) {
								for (String constName: enumItems.keySet()) {
									typeMap.put(constName, enumType);
								}
							}
						}

					}
				}
				else {
					// END KGU#542 2019-11-17
					StringList compNames = new StringList();
					StringList compTypes = new StringList();
					extractDeclarationsFromList(typeSpec.substring(posBrace+1, typeSpec.length()-1), compNames, compTypes, null);
					addRecordTypeToTypeMap(typeMap, typename, typeSpec, compNames, compTypes, lineNo);
					// START KGU#542 2019-11-17: Enh. #739
				}
				// END KGU#542 2019-1-17
			}
			// START KGU#1081 2023-09-28: Enh. #1091 Accept array type definitions
			else if (typeSpec.equalsIgnoreCase("array")
					|| TypeMapEntry.MATCHER_ARRAY.reset(typeSpec).matches()) {
				typeMap.put(":" + typename, new TypeMapEntry(typeSpec, typename, typeMap, this, lineNo, false, true));
			}
			// END KGU#1081 2023-09-28
			else {
				// According to isTypeDefinition() this must now be an alias for an existing type
				// START KGU#1081 2023-09-28: Issue #1091 Aliases for standard types didn't work
				//typeMap.put(":" + typename, typeMap.get(":" + tokens.get(3)));
				TypeMapEntry refType = typeMap.get(":" + tokens.get(3));
				if (refType == null) {
					refType = new TypeMapEntry(tokens.get(3), typename, typeMap, this, lineNo, false, true);
				}
				typeMap.put(":" + typename, refType);
				// END KGU#1081 2023-09-28
			}
		}
		// END KGU#388 2017-08-07
	}
	
	/**
	 * Extracts the target variable (or constant) name (or the the entire variable
	 * expression, see argument {@code entireTarget} out of the given blank-free token
	 * sequence which may comprise the entire line of an assignment or just its left
	 * part.<br/>
	 * The variable name may be qualified, i.e. be a sequence of identifiers separated
	 * by dots. Possible end-standing indices will not be part of the returned string,
	 * e.g. the result for {@code foo.bar[i][j]} will be "foo.bar", whereas for a mixed
	 * expression {@code foo[i].bar[j]}
	 * the result would be just "foo".<br/>
	 * In case of a multi-variable declaration, the result will be {@code null} as a
	 * potential initialisation would be rejected.
	 * 
	 * @param tokens - unified tokens of an assignment instruction without whitespace (otherwise
	 *     the result may be nonsense)
	 * @param entireTarget - if this is {@code true} then index expressions etc. will remain in
	 *    the result (so it is no longer the pure name)
	 * @return the extracted variable name/specification or {@code null}
	 */
	// KGU#686 2019-03-17: Enh. #56 - made static to facilitate implementation of Try
	public static String getAssignedVarname(StringList tokens, boolean entireTarget) {
		String varName = null;
		// START KGU#689 2019-03-21: Issue #706 - get along with named parameter calls
		tokens = coagulateSubexpressions(tokens.copy());		
		// END KGU689 2019-03-21
		int posAsgn = tokens.indexOf("<-");
		if (posAsgn > 0) {
			tokens = tokens.subSequence(0, posAsgn);
		}
		// START KGU#689 2019-03-21: Issue #706 can no longer happen in this form due to coagulation
		//int posLBracket = tokens.indexOf("[");
		//if (posLBracket > 0 && tokens.lastIndexOf("]") > posLBracket) {
		//	// If it's an array element access then cut off the index expression
		//	tokens = tokens.subSequence(0, posLBracket);
		//}
		// END KGU#689 2019-03-21
		// START KGU#388 2017-09-15: Enh. #423 avoid accidental return of type information
		// START KGU#1098 2023-10-13: Issue #980 in case of declarations be careful
		boolean isDecl = tokens.indexOf("type", false) == 0;
		// END KGU#1098 2023-10-13
		int posColon = tokens.indexOf(":");
		if (posColon > 0 || (posColon = tokens.indexOf("as", false)) > 0) {
			// It contains a declaration part
			tokens = tokens.subSequence(0, posColon);
			// START KGU#1089 2023-10-13: Issue #980 Don't return a name in a multi-var declaration
			isDecl = true;
			// END KGU#1089 2023-10-13
		}
		// END KGU#388 2017-09-15
		// START KGU#1089 2023-10-13: Issue #980 Don't return a name in a multi-var declaration
		// START KGU#1143 2024-04-02: Bugfix #1156 Typed constants caused null result
		//if (!tokens.isEmpty() && (tokens.get(0).equalsIgnoreCase("var") || tokens.get(0).equalsIgnoreCase("dim"))) {
		if (!tokens.isEmpty()
				&& (tokens.get(0).equalsIgnoreCase("var")
						|| tokens.get(0).equalsIgnoreCase("dim")
						|| tokens.get(0).equalsIgnoreCase("const"))) {
		// END KGU#1143 2024-04-02
			// This should be the case, otherwise we had a syntax violation here
			tokens.remove(0);
			isDecl = true;
		}
		if (isDecl && tokens.count() != 1) {
			// Too few or too many variables
			return null;
		}
		// Now we have handled multi-variable declarations (where an initialisation attempt is ignored)
		// END KGU#1089 2023-10-13

		// The last sequence of dot-separated identifiers should be the target variable designator
		if (tokens.count() > 0) {
			int i = tokens.count()-1;
			varName = tokens.get(i);
			// START KGU#780 2019-12-01: Bugfix - endstanding index access was erroneously returned
			// FIXME But it might be even more complicated, e.g. foo[i].bar[j]!
			// FIXME And face something like: int[2] mix[3] <- {{1,2,3},{4,5,6}}
			while (varName.startsWith("[") && varName.endsWith("]") && i > 0) {
//				if (posColon >= 0) {
//					// Something is wrong here - there should not be both a declaration and a bracket(?)
//					return null;
//				}
				// It is a coagulated index access - skip it
				varName = tokens.get(--i);
			}
			// END KGU#780 2019-12-01
			// START KGU#388 2017-09-14: Enh. #423
			// START KGU#780 2019-12-01: In cases like foo[i].bar[j] we want to return rather "foo" than "bar"
			//while (i > 1 && tokens.get(i-1).equals(".") && Function.testIdentifier(tokens.get(i-2), null)) {
			//	varName = tokens.get(i-2) + "." + varName;
			//	i -= 2;
			//}
			// START KGU#944 2021-02-26: Bugfix #946
			boolean forgetVarname = false;
			// END KGU#944 2021-02-26
			while (i > 1 && varName != null && tokens.get(i-1).equals(".")) {
				String preDotToken = tokens.get(i-2);
				if (Function.testIdentifier(preDotToken, false, null)) {
					varName = preDotToken + "." + varName;
					i -= 2;
				}
				else {
					// We may expect either an index expression or an identifier (variable or component name)
					while (i > 1 && preDotToken.startsWith("[") && preDotToken.endsWith("]")) {
						// Skip index expressions (invalidate the name, it was a component of an array element)
						varName = null;
						preDotToken = tokens.get(--i - 2);
					}
					if (varName == null && Function.testIdentifier(preDotToken, false, null)) {
						varName = preDotToken;	// Start again with the identifier prior to the indices
						i -= 2;	// this ought to be the token index of varName
					}
					// START KGU#944 2021-02-26: Bugfix #946 This could run into an endless loop
					else if (preDotToken.startsWith("(") && preDotToken.endsWith(")") && entireTarget) {
						// Might be a function or method call - then we might give up, e.g.
						// alternatives.get(nAlts - 1).qFalse <- caseElem.qs.get(lineNo - 1)
						if (i - 2 > 0 && Function.testIdentifier(tokens.get(i-3), false, null)) {
							// okay, seems to be a function or method call, indeed
							varName = null;
							i -= 3; // This is now the position of the function name
							// Check if it is a method, in this case the show might go on...
							if (i > 1 && tokens.get(i-1).equals(".")
									&& Function.testIdentifier(preDotToken = tokens.get(i-2), false, null)) {
								varName = preDotToken;
								i -= 2;
								forgetVarname = true;
							}
						}
						else {
							/* TODO --> issue #800
							 * The content of the parenthesis might be a casted expression or
							 * whatsoever. Without a detailed syntax analysis we are lost here
							 * It can't be sensible to proceed for now. But on the other hand
							 * it is already unlikely that a declaration is involved, and we
							 * are to deliver the correct start index i ...
							 */
							i = 0;
							varName = null;
						}
					}
					else {
						// Get the hell outa here! The preceding dot makes this varName wrong
						varName = null;
					}
					// END KGU#944 2021-02-26
				}
			}
			// END KGU#780 2019-12-01
			// END KGU#388 2017-09-14
			// START KGU#784 2019-12-02
			// START KGU#1090 2023-10-15: Bugfix #1096
			if (i > 0 && varName != null) {
				String preNameToken = tokens.get(i-1);
				if (Function.testIdentifier(preNameToken, false, null)
						|| preNameToken.startsWith("[") && preNameToken.endsWith("]")) {
					/* If another identifier or a bracket precedes the variable name then this
					 * must be a C declaration
					 */
					isDecl = true;
				}
				else if (preNameToken.equals(",")) {
					// Seems to be a list of variables
					varName = null;
				}
			}
			// END KGU#1090 2023-10-15
			if (entireTarget && !isDecl) {
				varName = tokens.concatenate(null, i);
			}
			// START KGU#944 2021-02-26: Bugfix #946
			else if (forgetVarname) {
				varName = null;
			};
			// END KGU#944 2021-02-26
			// END KGU#784 2019-12-02
		}
		return varName;
	}
	// END KGU#261 2017-01-26
	
	// START KGU#261 2017-02-20: Enh. #259 Allow CALL elements to override this...
	/**
	 * Tries to extract type information from the right side of an assignment.
	 * 
	 * @param rightSide - tokens of the assigned expression
	 * @param knownTypes - the typeMap as filled so far (won't be changed here)
	 * @return a type specification, an empty string (no clue), or "???" (ambiguous)
	 */
	protected String getTypeFromAssignedValue(StringList rightSide, HashMap<String, TypeMapEntry> knownTypes)
	{
		String typeSpec = "";
		// Check for array initializer expression
		if (rightSide.count() >= 2 && rightSide.get(0).equals("{") && rightSide.get(rightSide.count()-1).equals("}")) {
			StringList items = Element.splitExpressionList(rightSide.subSequence(1, rightSide.count()-1), ",", false);
			// Try to identify the element type(s)
			for (int i = 0; !typeSpec.contains("???") && i < items.count(); i++) {
				String itemType = identifyExprType(knownTypes, items.get(i), false);
				if (typeSpec.isEmpty()) {
					typeSpec = itemType;
				}
				else if (!itemType.isEmpty() && !typeSpec.equalsIgnoreCase(itemType)) {
					typeSpec = "???";
				}
			}
			if (typeSpec.isEmpty()) {
				typeSpec = "???";
			}
			typeSpec += "[" + items.count() + "]";
		}
		else {
			// START KGU#542 2019-11-17: Enh. #739 Check for enumerator constant
			if (rightSide.count() == 1 && Function.testIdentifier(rightSide.get(0), false, null)) {
				Root root = getRoot(this);
				if (root != null) {
					String constVal = root.constants.get(rightSide.get(0));
					if (constVal != null && constVal.startsWith(":") && constVal.contains("€")) {
						return constVal.substring(1, constVal.indexOf('€'));	// This is the type name
					}
				}
			}
			// END KGU#542 2019-11-17
			// Try to derive the type from the expression
			typeSpec = identifyExprType(knownTypes, rightSide.concatenate(" "), false);
		}
		return typeSpec;
	}
	// END KGU#261 2017-02-20

	// START KGU#388 2017-08-07: Enh. #423 type definition support for record 
	/**
	 * Tries to extract type information from the right side of an assignment.
	 * @param typeDef - tokens of the type definition
	 * @param knownTypes - the typeMap as filled so far (won't be changed here)
	 * @return a type specification, an empty string (no clue), or "???" (ambiguous)
	 */
	protected String getTypeFromTypeDefinition(StringList rightSide, HashMap<String, TypeMapEntry> knownTypes)
	{
		String typeSpec = "";
		
		// Try to derive the type from the expression
		typeSpec = identifyExprType(knownTypes, rightSide.concatenate(" "), false);
		return typeSpec;
	}
	// END KGU#388 2017-08-07

	// START KGU#477 2017-12-06: Enh. #487 - new mode to hide declarations
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getIcon()
	 */
	@Override
	public ImageIcon getIcon()
	{
		if (E_HIDE_DECL && this.isMereDeclaratory() && this == this.getDrawingSurrogate(false)) {
			return IconLoader.getIcon(85);
		}
		return super.getIcon();
	}
	/**
	 * In active mode {@link Element#E_HIDE_DECL} detects whether this is a mere declaration element and
	 * would be the first of a contiguous sequence of such elements (no matter if actually other declarations
	 * follow). In all other cases returns false 
	 * @param _modeIndependent - if true then an existing declaration sequence will also be detected if mode
	 * {@link Element#E_HIDE_DECL} is off
	 * @return true if declarations are to be hidden and this is the drawing surrogate of declaration sequence
	 */
	public boolean eclipsesDeclarations(boolean _modeIndependent)
	{
		return (_modeIndependent || E_HIDE_DECL) && this.isMereDeclaratory() && this == this.getDrawingSurrogate(_modeIndependent);
	}
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isCollapsed(boolean)
	 */
	@Override
	public boolean isCollapsed(boolean _orHidingOthers) {
		return super.isCollapsed(_orHidingOthers) || _orHidingOthers && eclipsesDeclarations(false);
	}
	/**
	 * @param _modeIndependent TODO
	 * @return either this or a preceding declaration if mode {@link Element#E_HIDE_DECL}
	 * is active and this is a mere declaration but not the first one in a series.
	 */
	public final Instruction getDrawingSurrogate(boolean _modeIndependent)
	{
		Instruction surrogate = this;
		if ((_modeIndependent || E_HIDE_DECL) && this.isMereDeclaratory()) {
			Subqueue myParent = (Subqueue)this.parent;
			int myIndex = (myParent).getIndexOf(this);
			Element pred = null;
			while (myIndex > 0
					// START KGU#408 2021-02-26
					//&& (pred = myParent.getElement(myIndex-1)).getClass().getSimpleName().equals("Instruction")
					&& (pred = myParent.getElement(myIndex-1)) instanceof Instruction
					// END KGU#408 2021-02-26
					&& ((Instruction)pred).isMereDeclaratory()) {
				surrogate = (Instruction)pred;
				myIndex--;
			}
		}
		return surrogate;
	}
	/**
	 * If this is part of a sequence of mere declaration elements and display
	 * mode {@link Element#E_HIDE_DECL} is active or {@code _force} is {@code true}
	 * then the complete declaration sequence virtually amalgamated under its
	 * first member will be returned, otherwise null.
	 * 
	 * @param _modeIndependent - if {@code true} then an existing declaration sequence
	 *    will also be returned if mode {@link Element#E_HIDE_DECL} is off.
	 * @return the sequence of hidden declaration elements including its surrogate
	 *    element or {@code null}.
	 */
	public IElementSequence getEclipsedDeclarations(boolean _modeIndependent)
	{
		IElementSequence hidden = null;
		Instruction surrogate = this.getDrawingSurrogate(_modeIndependent);
		if (this.eclipsesDeclarations(_modeIndependent) || this != surrogate) {
			Subqueue myParent = (Subqueue)this.parent;
			int firstIndex = (myParent).getIndexOf(surrogate);
			int index = firstIndex+1;
			Element succ = null;
			while (index < myParent.getSize()
					// START KGU#408 2021-02-26: Enh. #410 Mofied Call behaviour
					//&& (succ = myParent.getElement(index)).getClass().getSimpleName().equals("Instruction")
					&& (succ = myParent.getElement(index)) instanceof Instruction
					// END KGU#408 2021-02-26
					&& ((Instruction)succ).isMereDeclaratory()) {
				index++;
			}
			hidden = new SelectedSequence(myParent, firstIndex, index-1);
		}
		return hidden;
	}
	// END KGU#477 2017-12-06
	
	// START KGU#477 2017-12-10: Enh. #487 - We may have to aggregate information of hidden declarations
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRuntimeInfoString()
	 */
	@Override
	protected String getRuntimeInfoString()
	{
		// Only if display mode "Hide mere declarations?" is active we will have to aggregate flock data
		if (!this.eclipsesDeclarations(false)) {
			return super.getRuntimeInfoString();
		}
		// Now we need the minimum execution count and the total step count of the entire declaration flock
		// Since we must override this method anyway in order to enclose the combined step count, we do it
		// in a compound loop rather than by calling this.getExecCount() and this.getExecStepCount(_combined)
		IElementSequence flock = this.getEclipsedDeclarations(false);
		int nExec = this.getExecCount();
		int nSteps = this.getExecStepCount(false);
		for (int i = 1; i < flock.getSize(); i++) {
			Element decl = flock.getElement(i);
			nExec = Math.min(nExec, decl.getExecCount());
			nSteps += decl.getExecStepCount(false);
		}
		return nExec + " / (" + nSteps + ")";
	}
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getScaleColorForRTDPM()
	 */
	@Override
	protected Color getScaleColorForRTDPM()
	{
		int maxValue = 0;
		int value = 0;
		boolean logarithmic = false;
		switch (Element.E_RUNTIMEDATAPRESENTMODE) {
		case EXECCOUNTS:
			maxValue = Element.maxExecCount;
			value = this.getMinExecCount();
			break;
		case EXECSTEPS_LOG:
			logarithmic = true;
		case EXECSTEPS_LIN:
			maxValue = Element.maxExecStepCount;
			if (Element.E_HIDE_DECL && Element.maxExecStepsEclCount > Element.maxExecStepCount) {
				maxValue = Element.maxExecStepsEclCount;
			}
			value = this.execStepCount;
			if (this.eclipsesDeclarations(false)) {
				value += this.execSubCount;
			}
			break;
		case TOTALSTEPS_LOG:
			logarithmic = true;
		case TOTALSTEPS_LIN:
			maxValue = Element.maxExecTotalCount;
			if (Element.E_HIDE_DECL && Element.maxExecStepsEclCount > Element.maxExecTotalCount) {
				maxValue = Element.maxExecStepsEclCount;
			}
			value = this.execStepCount;
			if (this.eclipsesDeclarations(false)) {
				value += this.execSubCount;
			}
			break;
		default:
				;
		}
		if (logarithmic) {
			// We scale the logarithm a little up lest there should be too few
			// discrete possible values
			if (maxValue > 0) maxValue = (int) Math.round(25 * Math.log(maxValue));
			if (value > 0) value = (int) Math.round(25 * Math.log(value));
		}
		return getScaleColor(value, maxValue);
	}
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addToExecTotalCount(int, boolean)
	 */
	@Override
	public void addToExecTotalCount(int _growth, boolean _directly)
	{
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			if (!_directly && this.eclipsesDeclarations(true))
			{
				this.execSubCount += _growth;
				Element.maxExecStepsEclCount = 
						Math.max(this.execStepCount + this.execSubCount,
								Element.maxExecStepsEclCount);			
			}
			else {
				super.addToExecTotalCount(_growth, _directly);
			}
		}
	}
	private int getMinExecCount()
	{
		// Only if display mode "Hide mere declarations?" is active we will have to aggregate flock data
		if (!this.eclipsesDeclarations(false)) {
			return super.getExecCount();
		}
		// Now we need the minimum execution count and the total step count of the entire declaration flock
		// Since we must override this method anyway in order to enclose the combined step count, we do it
		// in a compound loop rather than by calling this.getExecCount() and this.getExecStepCount(_combined)
		IElementSequence flock = this.getEclipsedDeclarations(false);
		int nExec = this.getExecCount();
		for (int i = 1; i < flock.getSize(); i++) {
			Element decl = flock.getElement(i);
			nExec = Math.min(nExec, decl.getExecCount());
		}
		return nExec;
		
	}
	/**
	 * Returns the summed up number of execution steps of this element and - if
	 * {@code _combined} is {@code true} and this is not an eclipsing declaration -
	 * all its substructure.
	 * 
	 * @param _combined - ignored
	 * @return the requested step count
	 */
	@Override
	public int getExecStepCount(boolean _combined)
	{
		if (this.eclipsesDeclarations(true)) {
			return this.execStepCount;
		}
		else {
			return super.getExecStepCount(_combined);
		}
	}
	// END KGU#477 2017-12-10
	
	// START KGU#906/KGU#926 2021-02-04: Enh. #905, #926
	@Override
	public LinkedHashMap<Element, Vector<DetectedError>> getRelatedErrors(boolean getAll)
	{
		LinkedHashMap<Element, Vector<DetectedError>> errorMap = super.getRelatedErrors(getAll);
		IElementSequence hiddenDecls = this.getEclipsedDeclarations(false);
		if (hiddenDecls != null) {
			for (int i = 0; i < hiddenDecls.getSize() && (getAll || errorMap.isEmpty()); i++) {
				Element decl = hiddenDecls.getElement(i);
				if (decl != this) {
					decl.addRelatedErrors(getAll, errorMap);
				}
			}
		}
		return errorMap;
	}
	// END KGU#906/KGU#926 2021-02-04

	// START KGU#1151 2024-04-16: Issues #161, #1161#
	@Override
	public boolean mayPassControl()
	{
		if (!disabled) {
			StringList unbroken = getUnbrokenText();
			for (int i = 0; i < unbroken.count(); i++) {
				String line = unbroken.get(i);
				if (!line.isBlank() 
						&& (Jump.isLeave(line)
								|| Jump.isReturn(line)
								|| Jump.isExit(line)
								|| Jump.isThrow(line))
						) {
					return false;
				}
			}
		}
		return true;
	}
	// END KGU#1151 2024-04-16
}

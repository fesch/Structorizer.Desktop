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

		myrect = _top_left.copy();
		
		AffineTransform oldTrans = null;
		if (_element.rotated) {
			oldTrans = _canvas.rotateLeftAround(myrect.left, myrect.top);
			myrect.left -= (_top_left.bottom - _top_left.top);
			myrect.right -= (_top_left.right - _top_left.left);
			myrect.bottom = myrect.top + _top_left.right - _top_left.left;
		}
		
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
		//if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed())
		if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed(true))
		// END KGU#477 2017-12-06
		{
			Rect commentRect = _element.writeOutCommentLines(canvas,
//					_top_left.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
//					_top_left.top + (Element.E_PADDING / 2),
					myrect.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
					myrect.top + (Element.E_PADDING / 2),
					true);
			commentHeight = commentRect.bottom - commentRect.top;
		}
		// START BOB## 2018-09-08: Issue  #508
		//int yTextline = _top_left.top + (Element.E_PADDING / 3) + commentHeight/* + fontHeight*/;
		int yTextline = _top_left.top + (Element.E_PADDING / 2) + commentHeight/* + fontHeight*/;
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
					myrect.left + leftPadding + _element.getTextDrawingOffset(),
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
			if (_element.disabled) {
//				canvas.hatchRect(_top_left, 5, 10);
				canvas.hatchRect(myrect, 5, 10);
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
		if (!this.isDisabled()) {
			// START KGU#413 2017-06-09: Enh. #416 cope with user-inserted line breaks
			//_lines.add(this.getText());
			// START KGU#388 2017-09-13: Enh. #423: We must not add type definition lines
			//_lines.add(this.getUnbrokenText());
			StringList myLines = this.getUnbrokenText();
			for (int i = 0; i < myLines.count(); i++) {
				String line = myLines.get(i);
				if (!isTypeDefinition(line, null)) {
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
	
	/** @return true iff the given {@code line} has the syntax of a procedure invocation */
	public static boolean isProcedureCall(String line)
	{
		// START KGU#298 2016-11-22: Bugfix #296 - unawareness of had led to wrong transmutations
		//Function fct = new Function(line);
		//return fct.isFunction();
		return !isJump(line) && !isOutput(line) && Function.isFunction(line);
		// END KGU#298 2016-11-22
	}
	/** @return true iff {@code this} has exactly one instruction line and the line complies to {@link #isProcedureCall(String)} */
	public boolean isProcedureCall()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && Instruction.isProcedureCall(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && Instruction.isProcedureCall(lines.get(0));
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
	
	/** @return true iff the given {@code line} is an assignment with a function invocation as expression */
	public static boolean isFunctionCall(String line)
	{
		boolean isFunc = false;
		StringList tokens = Element.splitLexically(line, true);
		unifyOperators(tokens, true);
		int asgnPos = tokens.indexOf("<-");
		if (asgnPos > 0)
		{
			isFunc = Function.isFunction(tokens.concatenate("", asgnPos+1));
		}
		return isFunc;
	}
	/** @return true iff {@code this} consists of exactly one instruction line and the line complies to {@link #isFunctionCall(String)} */
	public boolean isFunctionCall()
	{
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && Instruction.isFunctionCall(lines.get(0));
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
	 * into the specified input prompt (may be empty) and the expressions identifying the
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
			tokens.remove(0, keyTokens.count());
			tokens.removeAll(" ");
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
	 * Returns true if the current line of code is a variable declaration of one of the following types:<br/>
	 * a) var &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt; [&lt;- &lt;expr&gt;]<br/>
	 * b) dim &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt; [&lt;- &lt;expr&gt;]<br/>
	 * c) &lt;type&gt; &lt;id&gt; &lt;- &lt;expr&gt;
	 * @param line - String comprising one line of code
	 * @return true iff line is of one of the forms a), b), c)
	 */
	public static boolean isDeclaration(String line)
	{
		StringList tokens = Element.splitLexically(line, true);
		unifyOperators(tokens, true);
		boolean typeA = tokens.indexOf("var") == 0 && tokens.indexOf(":") > 1;
		boolean typeB = tokens.indexOf("dim") == 0 && tokens.indexOf("as") > 1;
		int posAsgn = tokens.indexOf("<-");
		boolean typeC = false;
		if (posAsgn > 1) {
			tokens = tokens.subSequence(0, posAsgn);
			int posLBrack = tokens.indexOf("[");
			if (posLBrack > 0 && posLBrack < tokens.lastIndexOf("]")) {
				tokens = tokens.subSequence(0, posLBrack);
			}
			tokens.removeAll(" ");
			// START KGU#388 2017-09-27: Enh. #423 there might be a qualified name
			if (tokens.contains(".")) {
				int i = 1;
				// FIXME (KGU#553): The exact idea here isn't so clear anymore
				while (i < tokens.count() - 1) {
					if (tokens.get(i).equals(".") && Function.testIdentifier(tokens.get(i-1), null) && Function.testIdentifier(tokens.get(i+1), null)) {
						tokens.remove(i, i+2);
					}
					// START KGU#553 2018-07-12: Bugfix #557 We could get stuck in an endless loop here
					else {
						break;
					}
					// END KGU#553 2018-07-12
				}
			}
			// END KGU#388 2017-09-27
			typeC = tokens.count() > 1;
		}
		return typeA || typeB || typeC;
	}
	/** @return true if all non-empty lines are declarations
	 * @see #isDeclaration(String) */
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
	/** @return true if at least one line of {@code this} complies to {@link #isDeclaration(String)}
	 * @see #isDeclaration(String) */
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

	// START KGU#388 2017-07-03: Enh. #423
	/**
	 * Returns true if the given {@code line} of code is a type definition of one of the following forms:<br>
	 * a) type &lt;id&gt; = record{ &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt; {; &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt;} };<br>
	 * b) type &lt;id&gt; = record{ &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt; {; &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt;} };<br>
	 * c) type &lt;id&gt; = record{ &lt;type&gt; &lt;id&gt; {, &lt;id&gt;}; {; &lt;type&gt; &lt;id&gt; {, &lt;id&gt;}} };<br>
	 * d)...f) same as a)...c) but with struct instead of record;<br/>
	 * g) type &lt;id&gt; = enum{ &lt;id&gt [ = &lt;value&gt; ] {, &lt;id&gt [ = &lt;value&gt; ]} };<br/>
	 * h) type &lt;id&gt; = &lt;type&gt.<br/>
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
	 * h) type &lt;id&gt; = &lt;type&gt.<br/>
	 * Type names and descriptions &lt;type&gt; are checked against existing types in {@code typeMap} if given.
	 * @param line - String comprising one line of code
	 * @param typeMap - if given then the type name must have been registered in typeMap in order to be accepted (otherwise
	 * an appropriate syntax is okay.
	 * @return true iff line is of one of the forms a) through e)
	 * @see #isTypeDefinition(String)
	 * @see #isTypeDefinition(HashMap, boolean)
	 * @see #isTypeDefinition()
	 */
	public static boolean isTypeDefinition(String line, HashMap<String, TypeMapEntry> typeMap)
	{
		StringList tokens = Element.splitLexically(line.trim(), true);
		if (tokens.count() == 0 || !tokens.get(0).equalsIgnoreCase("type")) {
			return false;
		}
		unifyOperators(tokens, true);
		int posDef = tokens.indexOf("=");
		if (posDef < 2 || posDef == tokens.count()-1) {
			return false;
		}
		// FIXME why would we allow to define multi-word type names?
		String typename = tokens.concatenate("", 1, posDef).trim();
		tokens = tokens.subSequence(posDef+1, tokens.count());
		tokens.removeAll(" ");
		String tag = tokens.get(0).toLowerCase();
		return Function.testIdentifier(typename, null) &&
				// START KGU#542 2019-11-17: Enh. #739 - also consider enumeration types
				//((tag.equals("record") || tag.equals("struct")) && tokens.get(1).equals("{") && tokens.get(tokens.count()-1).equals("}")
				((tag.equals("record") || tag.equals("struct") || tag.equals("enum")) && tokens.get(1).equals("{") && tokens.get(tokens.count()-1).equals("}")
				// END KGU#542 2019-11-17
				|| tokens.count() == 1 && (typeMap != null && typeMap.containsKey(":" + tag) || typeMap == null && Function.testIdentifier(tag, null)));
		
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
	/** @return true iff this Instruction contains nothing but type definitions and
	 * (uninitialized) variable declarations, i.e. hidable stuff. 
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
	 * Returns a Function object describing the signature of the called routine
	 * if the text complies to the call syntax described in the user guide,
	 * or null otherwise.
	 * @return Function object or null.
	 * @see #isFunctionCall()
	 * @see #isProcedureCall()
	 */
	public Function getCalledRoutine()
	{
		Function called = null;
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//StringList lines = this.text;
		StringList lines = this.getUnbrokenText();
		// END KGU#413 2017-06-09
		if (lines.count() == 1)
		{
			String potentialCall = lines.get(0);
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
	 * @see #isFunctionCall()
	 * @see #isProcedureCall()
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
		StringList lines = this.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++) {
			updateTypeMapFromLine(typeMap, lines.get(i), i);
		}
		// END KGU#413 2017-06-09
	}
	
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
			// There may be one or more variable names between "var" and ':' if there is no assignment
			StringList varTokens = tokens.subSequence(1, posColon);
			varTokens.removeAll(" ");
			for (int i = 0; i < varTokens.count(); i++)
			{
				if (Function.testIdentifier(varTokens.get(i), null) && (i + 1 >= varTokens.count() || varTokens.get(i+1).equals(","))) {
					addToTypeMap(typeMap, varTokens.get(i), typeSpec, lineNo, isAssigned, true);
				}
			}
		}
		// Next we try to extract type information from an initial assignment (without "var" keyword)
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
			varName = getAssignedVarname(leftSide);
			// Without const, var, or dim a declaration must be a C-style declaration
			boolean isCStyleDecl = Instruction.isDeclaration(line);
			// If the target is a record component we won't add a type specification.
			if (varName != null && !varName.contains(".")) {
				int pos = leftSide.indexOf(varName);
				// C-style type declaration left of the variable name?
				typeSpec = leftSide.concatenate(null, 0, pos);
				// Check for array declaration (or array element access)
				while (!typeSpec.isEmpty() && (pos = leftSide.indexOf("[")) > 1) {
					typeSpec += leftSide.concatenate(null, pos, leftSide.indexOf("]")+1);
					leftSide.remove(pos, leftSide.indexOf("]")+1);
				}
				// No explicit type specification but new variable?
				if (typeSpec.isEmpty() && !typeMap.containsKey(varName)) {
					//String expr = rightSide.concatenate(" ");
					typeSpec = getTypeFromAssignedValue(rightSide, typeMap);
					isDeclared = false;
					if (typeSpec.isEmpty()) {
						typeSpec = "???";
					}
					// Maybe it's a multidimensional array, then reformulate it as "array of [array of ...]"
					while (!typeSpec.isEmpty() && (pos = leftSide.indexOf("[")) == 1) {
						typeSpec = "array of " + typeSpec;
						leftSide.remove(pos, leftSide.indexOf("]")+1);
					}
				}
			}
			addToTypeMap(typeMap, varName, typeSpec, lineNo, isAssigned, isDeclared || isCStyleDecl);
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
					this.extractDeclarationsFromList(typeSpec.substring(posBrace+1, typeSpec.length()-1), compNames, compTypes, null);
					addRecordTypeToTypeMap(typeMap, typename, typeSpec, compNames, compTypes, lineNo);
				// START KGU#542 2019-11-17: Enh. #739
				}
				// END KGU#542 2019-1-17
			}
			else {
				// According to isTypeefinition() this must now be an alias for an existing type
				typeMap.put(":" + typename, typeMap.get(":" + tokens.get(3)));
			}
		}
		// END KGU#388 2017-08-07
	}
	
	/**
	 * Extracts the target variable name out of the given blank-free token sequence which may comprise
	 * the entire line of an assignment or just its left part.
	 * The variable name may be qualified, i.e. be a sequence of identifiers separated by dots.
	 * @param tokens - unified tokens of an assignment instruction without whitespace (otherwise the result may be nonsense)
	 * @return the extracted variable name or null
	 */
	// KGU#686 2019-03-17: Enh. #56 - made static to facilitate implementation of Try
	public static String getAssignedVarname(StringList tokens) {
		String varName = null;
		// START KGU#689 2019-03-21: Issue #706 - get along with named parameter calls
		tokens = coagulateSubexpressions(tokens);		
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
		int posColon = tokens.indexOf(":");
		if (posColon > 0 || (posColon = tokens.indexOf("as", false)) > 0) {
			// It contains a declaration part
			tokens = tokens.subSequence(0, posColon);
		}
		// END KGU#388 2017-09-15
		// The last sequence of dot.separated ids should be the variable name
		if (tokens.count() > 0) {
			int i = tokens.count()-1;
			varName = tokens.get(i);
			// START KGU#388 2017-09-14: Enh. #423
			while (i > 1 && tokens.get(i-1).equals(".") && Function.testIdentifier(tokens.get(i-2), null)) {
				varName = tokens.get(i-2) + "." + varName;
				i -= 2;
			}
			// END KGU#388 2017-09-14
		}
		return varName;
	}
	// END KGU#261 2017-01-26

	// START KGU#261 2017-02-20: Enh. #259 Allow CALL elements to override this...
	/**
	 * Tries to extract type information from the right side of an assignment.
	 * @param rightSide - tokens of the assigned expression
	 * @param knownTypes - the typeMap as filled so far (won't be changed here)
	 * @return - A type specification or an empty string (no clue) or "???" (ambiguous)
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
			if (rightSide.count() == 1 && Function.testIdentifier(rightSide.get(0), null)) {
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
	 * @return - A type specification or an empty string (no clue) or "???" (ambiguous)
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
    public boolean isCollapsed(boolean _orHidden) {
        return super.isCollapsed(_orHidden) || _orHidden && eclipsesDeclarations(false);
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
					&& (pred = myParent.getElement(myIndex-1)).getClass().getSimpleName().equals("Instruction")
					&& ((Instruction)pred).isMereDeclaratory()) {
				surrogate = (Instruction)pred;
				myIndex--;
			}
		}
		return surrogate;
	}
	/**
	 * If this is part of a sequence of mere declaration elements and display mode {@link Element#E_HIDE_DECL}
	 * is active or {@code _force} is true then the complete declaration sequence virtually amalgamated under
	 * its first member will be returned, otherwise null.
	 * @param _modeIndependent - if true then an existing declaration sequence will also be returned if mode
	 * {@link Element#E_HIDE_DECL} is off.
	 * @return the sequence of hidden declaration elements including its surrogate element or null.
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
					&& (succ = myParent.getElement(index)).getClass().getSimpleName().equals("Instruction")
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
	 * Returns the summed up execution steps of this element and - if {@code _combined} is true and
	 * this is not an eclipsing declaration - all its substructure.
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

}

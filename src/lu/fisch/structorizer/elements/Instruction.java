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
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2015.10.11/13   Comment drawing unified, breakpoints supported, colouring modified
 *      Kay Gürtzig     2015.11.14      Bugfix #31 (= KGU#82) in method copy
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *      Kay Gürtzig     2016-01-03      Bugfix #87 (KGU#124) collapsing of larger instruction elements,
 *                                      Enh. #87 (KGU#122) marking of collapsed elements with icon
 *      Kay Gürtzig     2016.02.27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016.03.01      Bugfix #97 (KGU#136): fix accomplished
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Fields for test coverage tracking added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.06      Enh. #188: New classification methods isAssignment() etc.,
 *                                      new copy constructor to support conversion (KGU#199)
 *      Kay Gürtzig     2016.07.30      Enh. #128: New mode "comments plus text" supported
 *      Kay Gürtzig     2016.08.10      Issue #227: New classification methods for Input/Output
 *      Kay Gürtzig     2016.09.25      Enh. #253: D7Parser.keywordMap refactored
 *      Kay Gürtzig     2016.10.13      Enh. #270: Hatched overlay texture in draw() if disabled
 *      Kay Gürtzig     2016.10.15      Enh. #271: method isEmptyInput() had to consider prompt strings now.
 *      Kay Gürtzig     2016.11.22      Bugfix #296: Wrong transmutation of return and output statements
 *      Kay Gürtzig     2017.01.26      Enh. #259: First retrieval approach for variable types
 *      Kay Gürtzig     2017.01.30      Enh. #335: More sophisticated type and declaration support    
 *      Kay Gürtzig     2017.02.20      Enh. #259: Retrieval of result types of called functions enabled (q&d)
 *      Kay Gürtzig     2017.04.11      Enh. #289: Methods isImportCall() introduced
 *      Kay Gürtzig     2017.06.09      Enh. #416: drawing support for broken lines and is...() method adaptation
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.parsers.CodeParser;
//import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.*;

public class Instruction extends Element {
	
	// START KGU#258 2016-09-26: Enh. #253
	private static final String[] relevantParserKeys = {"input", "output", "preReturn"};
	// END KGU#258 2016-09-25
	// START KGU#413 2017-06-09: Enh. #416
	protected static final String indentPattern = "(\\s*)(.*)";
	// END KGU#413 2017-06-09
	
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
		Rect rect = new Rect(0, 0, 2*(Element.E_PADDING/2), 0);
		// START KGU#227 2016-07-30: Enh. #128
		int commentHeight = 0;
		// END KGU#227 2016-07-30

		FontMetrics fm = _canvas.getFontMetrics(Element.font);

		// START KGU#227 2016-07-30: Enh. #128
		if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed())
		{
			Rect commentRect = _element.writeOutCommentLines(_canvas,
					0, 0, false);
			rect.right = Math.max(rect.right, commentRect.right + Element.E_PADDING);
			commentHeight = commentRect.bottom;
		}
		// END KGU#227 2016-07-30
		
		// START KGU#413 2017-06-09: Enh. #416
		boolean isContinuation = false;
		// END KGU#413 2017-06-09
		for(int i = 0; i < _text.count(); i++)
		{
			// START KGU#413 2017-06-09: Enh. #416
			//int lineWidth = getWidthOutVariables(_canvas, _text.get(i), _element) + Element.E_PADDING;
			String line = _text.get(i);
			if (isContinuation) {
				String indent = line.replaceAll(indentPattern, "$1");
				String rest = line.replaceAll(indentPattern, "$2");
				if (indent.length() < Element.E_INDENT) {
					line = String.format("%1$" + Element.E_INDENT + "s%2$s", indent, rest);
				}
			}
			isContinuation = line.trim().endsWith("\\");
			int lineWidth = getWidthOutVariables(_canvas, line, _element) + Element.E_PADDING;
			// END KGU#413 2017-06-09
			if (rect.right < lineWidth)
			{
				rect.right = lineWidth;
			}
		}
		rect.bottom = 2*(Element.E_PADDING/2) + _text.count() * fm.getHeight();
		// START KGU#227 2016-07-30: Enh. #128
		rect.bottom += commentHeight;
		// END KGU#227 2016-07-30

		return rect;
	}
        
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRectUpToDate) return rect0;
		// END KGU#136 2016-03-01

		// KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		
		// START KGU#124 2016-01-03: Large instructions should also be actually collapsed
        //rect = prepareDraw(_canvas, getText(false), this);
		StringList text = getText(false);
        if (isCollapsed() && text.count() > 2) 
        {
        	text = getCollapsedText();
        }
        rect0 = prepareDraw(_canvas, text, this);
        // END KGU#124 2016-01-03
        
		// START KGU#136 2016-03-01: Bugfix #97
		isRectUpToDate = true;
		// END KGU#136 2016-03-01
        return rect0;
	}

	public static void draw(Canvas _canvas, Rect _top_left, StringList _text, Element _element)
	{
		Rect myrect = new Rect();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = _element.getColor();
		Color drawColor = _element.getFillColor();
		// END KGU 2015-10-13
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
			
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
		if (Element.E_COMMENTSPLUSTEXT && !_element.isCollapsed())
		{
			Rect commentRect = _element.writeOutCommentLines(canvas,
//					_top_left.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
//					_top_left.top + (Element.E_PADDING / 2),
					myrect.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
					myrect.top + (Element.E_PADDING / 2),
					true);
			commentHeight = commentRect.bottom - commentRect.top;
		}
		int yTextline = _top_left.top + (Element.E_PADDING / 2) + commentHeight/* + fm.getHeight()*/;
		// END KGU#227 2016-07-30
		
		// START KGU#413 2017-06-09: Enh. #416
		boolean isContinuation = false;
		// END KGU#413 2017-06-09
		for (int i = 0; i < _text.count(); i++)
		{
			String text = _text.get(i);
			// START KGU#413 2017-06-09: Enh. #416
			if (isContinuation) {
				String indent = text.replaceAll(indentPattern, "$1");
				String rest = text.replaceAll(indentPattern, "$2");
				if (indent.length() < Element.E_INDENT) {
					text = String.format("%1$" + Element.E_INDENT + "s%2$s", indent, rest);
				}
			}
			isContinuation = text.trim().endsWith("\\");
			// END KGU#413 2017-06-09
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
//					_top_left.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
					myrect.left + (Element.E_PADDING / 2) + _element.getTextDrawingOffset(),
					// START KGU#227 2016-07-30: Enh. #128
					//_top_left.top + (Element.E_PADDING / 2) + (i+1)*fm.getHeight(),
					yTextline += fm.getHeight(),
					// END KGU#227 2016-07-30
					text,
					_element
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
		if (_element.isCollapsed() && !(_element instanceof Instruction))
		{
//			canvas.draw(_element.getIcon().getImage(), _top_left.left, _top_left.top);
			canvas.draw(_element.getIcon().getImage(), myrect.left, myrect.top);
		}
		// END KGU#122 2016-01-03
		
		if (oldTrans != null) {
			canvas.setTransform(oldTrans);
		}
	}
                
	public void draw(Canvas _canvas, Rect _top_left)
	{
		// Now delegates all stuff to the static method above, which may also
		// be called from Elements of different types when those are collapsed
		
		// START KGU#124 2016-01-03: Large instructions should also be actually collapsed
        //draw(_canvas, _top_left, getText(false), this);
        if (isCollapsed() && getText(false).count() > 2) 
        {
        	draw(_canvas, _top_left, getCollapsedText(), this);
        }
        else
        {
            draw(_canvas, _top_left, getText(false), this);
        }
        // END KGU#124 2016-01-03
	}
	
	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	public Element findSelected()
	{
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
    protected void addFullText(StringList _lines, boolean _instructionsOnly, HashSet<Root> implicatedRoots)
    {
		if (!this.isDisabled()) {
			// START KGU#413 2017-06-09: Enh. #416 cope with user-inserted line breaks
			//_lines.add(this.getText());
			_lines.add(this.getUnbrokenText());
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
	public static boolean isAssignment(String line)
	{
    	StringList tokens = Element.splitLexically(line, true);
    	unifyOperators(tokens, true);
		return tokens.contains("<-");
	}
	public boolean isAssignment()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && Instruction.isAssignment(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && Instruction.isAssignment(lines.get(0));
		// END KGU#413 2017-06-09
	}
	
	public static boolean isJump(String line)
	{
    	StringList tokens = Element.splitLexically(line, true);
		return (tokens.indexOf(CodeParser.getKeyword("preReturn"), !CodeParser.ignoreCase) == 0 ||
				tokens.indexOf(CodeParser.getKeyword("preLeave"), !CodeParser.ignoreCase) == 0 ||
				tokens.indexOf(CodeParser.getKeyword("preExit"), !CodeParser.ignoreCase) == 0
				);
	}
	public boolean isJump()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 0 || this.text.count() == 1 && Instruction.isJump(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		return lines.count() == 0 || lines.count() == 1 && Instruction.isJump(lines.get(0));
		// END KGU#413 2017-06-09
	}
	
	public static boolean isProcedureCall(String line)
	{
    	// START KGU#298 2016-11-22: Bugfix #296 - unawareness of had led to wrong transmutations
		//Function fct = new Function(line);
		//return fct.isFunction();
		return !isJump(line) && !isOutput(line) && Function.isFunction(line);
    	// END KGU#298 2016-11-22
	}
	public boolean isProcedureCall()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && Instruction.isProcedureCall(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && Instruction.isProcedureCall(lines.get(0));
		// END KGU#413 2017-06-09
	}

	// START #274 2016-10-16 (KGU): Improved support for Code export
	public static boolean isTurtleizerMove(String line)
	{
		final StringList turtleizerMovers = StringList.explode("forward,backward,fd,bk", ",");
		Function fct = new Function(line);
		return fct.isFunction() && turtleizerMovers.contains(fct.getName()) && fct.paramCount() == 1;
	}
	// END #274 2016-10-16
	
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
	public boolean isFunctionCall()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && Instruction.isFunctionCall(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && Instruction.isFunctionCall(lines.get(0));
		// END KGU#413 2017-06-09
	}
	// END KGU#199 2016-07-06
	
	// START KGU#376 2017-04-11: Enh. #389
	public static boolean isImportCall(String line)
	{
		final String importKey = CodeParser.getKeywordOrDefault("preImport", "import").trim() + " ";
		return line.startsWith( importKey ) &&
				Function.testIdentifier(line.substring( importKey.length() ).trim(), null);
	}
	public boolean isImportCall()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//return this.text.count() == 1 && Instruction.isImportCall(this.text.get(0));
		StringList lines = this.getUnbrokenText();
		return lines.count() == 1 && Instruction.isImportCall(lines.get(0));
		// END KGU#413 2017-06-09
	}
	// END KGU#376 2017-04-11
	
	// START KGU#236 2016-08-10: Issue #227: New classification for input and output
	public static boolean isOutput(String line)
	{
		StringList tokens = Element.splitLexically(line, true);
		return (tokens.indexOf(CodeParser.getKeyword("output"), !CodeParser.ignoreCase) == 0);
	}
	public boolean isOutput()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//for (int i = 0; i < this.getText().count(); i++)
		//{
		//	if (isOutput(this.getText().get(i).trim()))
		//	{
		//		return true;
		//	}
		//}
		StringList lines = this.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++)
		{
			if (isOutput(lines.get(i)))
			{
				return true;
			}
		}
		// END KGU#413 2017-06-09
		return false;
	}
	
	public static boolean isInput(String line)
	{
		StringList tokens = Element.splitLexically(line, true);
		return (tokens.indexOf(CodeParser.getKeyword("input"), !CodeParser.ignoreCase) == 0);
	}
	public boolean isInput()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//for (int i = 0; i < this.getText().count(); i++)
		//{
		//	if (isInput(this.getText().get(i).trim()))
		//	{
		//		return true;
		//	}
		//}
		StringList lines = this.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++)
		{
			if (isInput(lines.get(i)))
			{
				return true;
			}
		}
		// END KGU#413 2017-06-09
		return false;
	}

	
	public static boolean isEmptyInput(String line)
	{
		StringList tokens = Element.splitLexically(line, true);
		// START KGU#281 2016-10-15: Enh. #271 - had turned out to be too simple.
		//return (tokens.count() == 1 && tokens.indexOf(CodeParser.keywordMap.get("input"), !CodeParser.ignoreCase) == 0);
		boolean isEmptyInp = false;
		StringList keyTokens = Element.splitLexically(CodeParser.getKeyword("input"), false);
		if (tokens.indexOf(keyTokens, 0, !CodeParser.ignoreCase) == 0) {
			tokens = tokens.subSequence(keyTokens.count(), tokens.count());
			tokens.removeAll(" ");
			isEmptyInp = tokens.count() == 0 || tokens.count() == 1 && (tokens.get(0).startsWith("\"") || tokens.get(0).startsWith("'"));
		}
		return isEmptyInp;
		// END KGU#281 2016-10-15
	}
	public boolean isEmptyInput()
	{
		// START KGU#413 2017-06-09: Enh. #416 cope with user-defined line breaks
		//for (int i = 0; i < this.getText().count(); i++)
		//{
		//	if (isEmptyInput(this.getText().get(i).trim()))
		//	{
		//		return true;
		//	}
		//}
		StringList lines = this.getUnbrokenText();
		for (int i = 0; i < lines.count(); i++)
		{
			if (isEmptyInput(lines.get(i)))
			{
				return true;
			}
		}
		// END KGU#413 2017-06-09
		return false;
	}	
	// END KGU#236 2016-08-10
	
	// START KGU#322 2016-07-06: Enh. #335
	/**
	 * Returns true if the current line of code is a declarationof one of the following types:
	 * a) var &lt;id&gt; {, &lt;id&gt;} : &lt;type&gt; [&lt;- &lt;expr&gt;]
	 * b) dim &lt;id&gt; {, &lt;id&gt;} as &lt;type&gt; [&lt;- &lt;expr&gt;]
	 * c) &lt;type&gt; &lt;id&gt; &lt;- &lt;- &lt;expr&gt;
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
    		typeC = tokens.count() > 1;
    	}
		return typeA || typeB || typeC;
	}
	/** @return true if all non-empty lines are declarations */
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
	/** @return true if at least one line is a declaration */
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

	// START KGU#178 2016-07-19: Support for enh. #160 (export of called subroutines)
	// (This method is plaed here instead of in class Call because it is needed
	// to decide whether an Instruction element complies to the Call syntax and
	// may be transmuted.)
	/**
	 * Returns a Function object describing the signature of the called routine
	 * if the text complies to the call syntax described in the user guide
	 * or null otherwise.
	 * @return Function object or null.
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
		unifyOperators(tokens, true);
		tokens.removeAll(" ");
		if (tokens.count() == 0) {
			return;
		}
		int posColon = tokens.indexOf(tokens.get(0).equals("dim") ? "as" : ":");
		int posAsgnmt = tokens.indexOf("<-");
		// First we try to extract a type description from a Pascal-style variable declaration
		if (tokens.count() > 3 && (tokens.get(0).equals("var") || tokens.get(0).equals("dim")) && posColon >= 2) {
			isAssigned = posAsgnmt > posColon;
			typeSpec = tokens.concatenate(" ", posColon+1, (isAssigned ? posAsgnmt : tokens.count()));
			// There may be one or more variable names between "var" and ':' if there is no assignment
			for (int i = 1; i < posColon; i++)
			{
				if (Function.testIdentifier(tokens.get(i), null)) {
					addToTypeMap(typeMap, tokens.get(i), typeSpec, lineNo, isAssigned, false);
				}
			}
		}
		// Next we try to extract type information from an initial assignment (without "var" keyword)
		else if (posAsgnmt > 0 && !tokens.contains("var") && !tokens.contains("dim")) {
			// Type information might be found left of the variable name or be derivable from the initial value
			StringList leftSide = tokens.subSequence(0, posAsgnmt);
			StringList rightSide = tokens.subSequence(posAsgnmt+1, tokens.count());
			isAssigned = rightSide.count() > 0;
			// Isolate the variable name from the left-hand side of the assignment
			varName = getAssignedVarname(leftSide);
			boolean isCStyleDecl = Instruction.isDeclaration(line);
			if (varName != null) {
				int pos = leftSide.indexOf(varName);
				// C-style type declaration left of the variable name?
				typeSpec = leftSide.concatenate(" ", 0, pos);
				// Check for array declaration (or array element access)
				while (!typeSpec.isEmpty() && (pos = leftSide.indexOf("[")) > 1) {
					typeSpec += leftSide.concatenate("", pos, leftSide.indexOf("]")+1);
					leftSide.remove(pos, leftSide.indexOf("]")+1);
				}
				// No explicit type specification but new variable?
				if (typeSpec.isEmpty() && !typeMap.containsKey(varName)) {
					//String expr = rightSide.concatenate(" ");
					typeSpec = getTypeFromAssignedValue(rightSide, typeMap);
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
			addToTypeMap(typeMap, varName, typeSpec, lineNo, isAssigned, isCStyleDecl);
		}
	}
	
	/**
	 * Extracts the target variable name out of the given blank-free token sequence which may comprise
	 * the entire line of an assignment or just its left part.
	 * @param tokens - unified tokens of an assignment instruction without whitespace (otherwise the result may be nonsense)
	 * @return the extracted variable name or null
	 */
	public String getAssignedVarname(StringList tokens) {
		String varName = null;
		int posAsgn = tokens.indexOf("<-");
		if (posAsgn > 0) {
			tokens = tokens.subSequence(0, posAsgn);
		}
		int posLBracket = tokens.indexOf("[");
		if (posLBracket > 0 && tokens.lastIndexOf("]") > posLBracket) {
			// If it's an array element access then cut of the index expression
			tokens = tokens.subSequence(0, posLBracket);
		}
		// The last token should be the variable name
		if (tokens.count() > 0) {
			varName = tokens.get(tokens.count()-1);
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
			StringList items = Element.splitExpressionList(rightSide.concatenate("", 1, rightSide.count()-1), ",");
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
			// Try to derive the type from the expression
			typeSpec = identifyExprType(knownTypes, rightSide.concatenate(" "), false);
		}
		return typeSpec;
	}
	// END KGU#261 2017-02-20

}

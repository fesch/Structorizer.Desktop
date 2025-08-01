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
 *      Description:    This class represents an "IF statement" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-10      First Issue
 *      Kay Gürtzig     2015-10-11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015-10-11      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015-11-14      Bugfix #31 (= KGU#82) in method copy
 *      Kay Gürtzig     2015-12-01      Bugfix #39 (= KGU#91) in drawing methods
 *      Kay Gürtzig     2016-01-02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016-01-03      Bugfix #87 (KGU#121): Correction in getElementByCoord()
 *      Kay Gürtzig     2016-02-27      Bugfix #97 (KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016-03-01      Bugfix #97 (KGU#136): Translation-neutral selection
 *      Kay Gürtzig     2016-03-06      Enh. #77 (KGU#117): Methods for test coverage tracking added
 *      Kay Gürtzig     2016-03-07      Bugfix #122 (KGU#136): Selection was not aware of option altPadRight 
 *      Kay Gürtzig     2016-03-12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016-04-24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016-07-21      Bugfix #198: Inconsistency between methods prepareDraw() and draw()
 *      Kay Gürtzig     2016-07-31      Enh. #128: New mode "comments plus text" supported, drawing code delegated
 *                                      Bugfix #212 (inverted logic of option altPadRight = "enlarge FALSE")
 *      Kay Gürtzig     2016-10-13      Enh. #270: Hatched overlay texture in draw() if disabled
 *      Kay Gürtzig     2017-02-08      Bugfix #198 (KGU#346) rightward cursor navigation was flawed,
 *                                      Inheritance changed (IFork added)
 *      Kay Gürtzig     2017-10-22      Enh. #128: Design for mode "comments plus text" revised to save space
 *      Kay Gürtzig     2017-11-01      Bugfix #447: End-standing backslashes suppressed for display and analysis
 *      Kay Gürtzig     2018-01-21      Enh. #490: Replacement of DiagramController aliases on drawing
 *      Kay Gürtzig     2018-02-09      Bugfix #507: Element size and layout must depend on branch labels
 *      Kay Gürtzig     2018-04-04      Issue #529: Critical section in prepareDraw() reduced.
 *      Bob Fisch       2018-09-08      Issue #508: Font height reduction for better vertical centering
 *      Kay Gürtzig     2018-09-11      Issue #508: Font height retrieval concentrated to one method on Element
 *      Kay Gürtzig     2018-10-26      Enh. #619: Method getMaxLineLength() implemented
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-03-28      Enh. #128: comment block height slightly enlarged
 *      Kay Gürtzig     2021-01-02      Enh. #905: Mechanism to draw a warning symbol on related DetectedError
 *      Kay Gürtzig     2022-07-31      Bugfix #1054: Element width did not always respect comment width
 *      Kay Gürtzig     2025-07-02      Bugfix #1195: Element is also to be hatched if indirectly disabled,
 *                                      missing Override annotations added.
 *      Kay Gürtzig     2025-07-31      Enh. #1197: Branch selector colouring enabled
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.structorizer.gui.FindAndReplace;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.utils.*;

/**
 * This Structorizer class represents an "IF statement" in a diagram.
 * 
 * @author Bob Fisch
 */
public class Alternative extends Element implements IFork {

	public Subqueue qFalse = new Subqueue();
	public Subqueue qTrue = new Subqueue();
	
	private Rect rTrue = new Rect();
	private Rect rFalse = new Rect();
	
	// START KGU#136 2016-03-07: Bugfix #97
	private Point pt0Parting = new Point();
	// END KGU#136 2016-03-07
	// START KGU#227 2016-07-31: Enh. #128
	private Rect commentRect = new Rect();
	// END KGU#227 2016-07-31

	// START KGU#258 2016-09-26: Enh. #253
	private static final String[] relevantParserKeys = {"preAlt", "postAlt"};
	// END KGU#258 2016-09-25
	
	// START KGU#1182 2025-07-31: Enh. #1197 Allow to subselect headers in IFork
	/**
	 * Index of the currently selected branch head (0 -> T, 1 -> F, -1 - none).
	 */
	private int selectedBranchHead = -1;
	
	/**
	 * Possible chosen colours for the existing branch selector head polygons
	 */
	private Color branchHeadColors[] = new Color[] {null, null};
	// END KGU#1182 2025-07-31
	
	public Alternative()
	{
		super();
		qFalse.parent=this;
		qTrue.parent=this;
	}
	
	public Alternative(String _strings)
	{
		super(_strings);
		qFalse.parent=this;
		qTrue.parent=this;
		//setText(_strings);	// Already done
	}
	
	public Alternative(StringList _strings)
	{
		super(_strings);
		qFalse.parent=this;
		qTrue.parent=this;
		//setText(_strings);	// Already done
	}
	
	@Override
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRect0UpToDate) return rect0;
		// END KGU#136 2016-03-01
		//  KGU#136 2016-02-27: Bugfix #97 - all rect references replaced by rect0
		if(isCollapsed(true)) 
		{
			rect0 = Instruction.prepareDraw(_canvas, getCollapsedText(), this);
			// START KGU#136 2016-03-01: Bugfix 97
			isRect0UpToDate = true;
			// END KGU#136 2016-03-01
			return rect0;
		}

		// START KGU#453 2017-11-01: Bugfix #447 - don't show end-standing backslashes
		//int nLines = getText(false).count();
		StringList myText = getCuteText(false);
		// START KGU#480 2018-01-21: Enh. #490
		if (Element.E_APPLY_ALIASES && !isSwitchTextCommentMode()) {
			myText = StringList.explode(Element.replaceControllerAliases(myText.getText(), true, false), "\n");
		}
		// END KGU#480 2018-01-21
		int nLines = myText.count();
		// END KGU#453 2017-11-01
		
		// START KGU#516 2018-04-04: Issue #529 - Directly to work on field rect0 was not so good an idea for re-entrance
		//rect0.top = 0;
		//rect0.left = 0;
		Rect rect0 = new Rect();
		Rect commentRect;
		Rect rFalse, rTrue;
		Point pt0Parting = new Point();
		// END KGU#516 2018-04-04

		// START KGU#494 2018-09-11: Issue #508 Retrieval concentrated for easier maintenance
		//FontMetrics fm = _canvas.getFontMetrics(Element.font);
		int fontHeight = getFontHeight(_canvas.getFontMetrics(Element.font));
		// END KGU#494 2018-09-11

		rect0.right = 2 * E_PADDING;

		// START KGU#227 2016-07-31: Enh. #128
		commentRect = new Rect();
		if (Element.E_COMMENTSPLUSTEXT)
		{
			// Get the space needed for the 1st comment line
			// START KGU#435 2017-10-22: Issue #128 revised
			//commentRect = this.writeOutCommentLines(_canvas, 0, 0, false, false);
			commentRect = this.writeOutCommentLines(_canvas, 0, 0, false);
			if (commentRect.right != 0) {
				commentRect.bottom += E_PADDING/3;
				commentRect.right += 2 * (E_PADDING/2);
			}
			// END KGU#435 2017-10-22
		}
		// END KHU#227 2016-07-31
		// prepare the sub-queues
		rFalse = qFalse.prepareDraw(_canvas);
		rTrue = qTrue.prepareDraw(_canvas);
		
		// START KGU#491 2018-02-09: Bugfix #507 - we must find out the label widths
		int lWidthT = _canvas.stringWidth(preAltT);
		int lWidthF = _canvas.stringWidth(preAltF);
		// As a rough estimate of the angles we add a half of it and the padding
		// (This is pretty sound since the label usually occupies the lower third of its triangle
		// height. For proportionality reasons, the triangle width at top of the label must be
		// about 2/3 of the entire branch width, such that we may assume the branch width minimum
		// to be 1.5 * label width. With this simple formula we stay on the safe side even for
		// nLines > 1 where 1 + 1/(nLines+1) would suffice.)
		lWidthT += E_PADDING + lWidthT/2;	// or += E_PADDING + (lWidhtT/(nLines+1))
		lWidthF += E_PADDING + lWidthF/2;	// or += E_PADDING + (lWidhtF/(nLines+1))
		if (rTrue.right - rTrue.left < lWidthT) {
			rTrue.right = rTrue.left + lWidthT;
		}
		if (rFalse.right - rFalse.left < lWidthF) {
			rFalse.right = rFalse.left + lWidthF;
		}
		// END KGU#491 2018-02-09

		// Compute the left traverse line (y coordinates reversed) as if the triangle were always at top
		// the upper left corner
		double cx = 0;
		// START KGU#227 2016-07-31: Enh. #128 - we need additional space for the comment
		double cy = nLines*fontHeight + 4*(E_PADDING/2);
		//double cy = nLines*fontHeight + 4*(E_PADDING/2) + commentRect.bottom;
		// END KGU#227 2016-07-31
		// the lowest point of the triangle
		double ax =  rTrue.right - rTrue.left;
		//logger.debug("AX : "+ax);
		double ay =  0;
		// gradient coefficient of the left traverse line
//		if (cx - ax == 0) {
//			System.err.println(this + "prepareDraw(212): cx-ax = 0");
//		}
//		if (cy - ay == 0) {
//			System.err.println(this + "prepareDraw(215): cy-ay = 0");
//		}
		double coeffleft = (cy-ay)/(cx-ax);


		// init
		//int choice = -1;
		double lowest = 100000;	// dummy bound

		// START KGU#227 2016-07-31: Enh. #128 - withdrawn KGU#435 2017-10-22
//		int yOffset = 4*(E_PADDING/2) - (E_PADDING/3) + commentRect.bottom;
//		if (commentRect.bottom > 0)
//		{
//			// This code is derived from the FOR loop body below. yOffset had to be reduced by the bias fontHeight.
//			// part on the left side
//			double by = yOffset - fontHeight - commentRect.bottom;
//			double leftside = by/coeffleft + ax - ay/coeffleft;
//			double bx = commentRect.right + 2*(E_PADDING/2) + leftside;
//			double coeff = (by-ay)/(bx-ax);
//			if (coeff<lowest && coeff>0)
//			{
//				lowest = coeff;
//			}			
//		}
		// END KGU#227 2016-07-31

		for (int i = 0; i < nLines; i++)
		{

			/* old code
			if(rect.right<_canvas.stringWidth((String) text.get(i))+4*Math.round(E_PADDING))
			{
				rect.right=_canvas.stringWidth((String) text.get(i))+4*Math.round(E_PADDING);
			}
			 */

			// bottom line of the text
			// START KGU#227 2016-07-31: Enh. #128 - withdrawn KGU#435 2017-10-22
			double by = 4*(E_PADDING/2) - (E_PADDING/3) + (nLines-i-1) * fontHeight;
			//double by = yOffset + (nLines-i-1) * fontHeight;
			// END KGU#227 2016-07-31
			// part on the left side
			double leftside = by/coeffleft + ax - ay/coeffleft;
			// the bottom right point of this text line
			// START KGU#453 2017-11-01: Bugfix #447
			//int textWidth = getWidthOutVariables(_canvas, getText(false).get(i), this);
			int textWidth = getWidthOutVariables(_canvas, myText.get(i), this);
			// END KGU#453 2017-11-01
			double bx = textWidth + 2*(E_PADDING/2) + leftside;
			//logger.debug("LS : "+leftside);

			// check if this is the one we need to do calculations
//			if (bx - ax == 0) {
//				System.err.println(this + "prepareDraw(268): bx-ax = 0");
//			}
//			if (by - ay == 0) {
//				System.err.println(this + "prepareDraw(271): by-ay = 0");
//			}
			double coeff = (by-ay)/(bx-ax);

			// the point height we need
			//double y = nLines * fontHeight + 4*(E_PADDING/2);
			//double x = y/coeff + ax - ay/coeff;
			//logger.debug(i+" => "+coeff+" --> "+String.valueOf(x));

			if (coeff < lowest && coeff > 0)
			{
				// remember it
				lowest = coeff;
				//choice = i;
			}
		}
		if (lowest != 100000)
		{
			// the point height we need
			// START KGU#227 2016-07-31: Enh. #128 - withdrawn KGU#435 2017-10-22
			double y = nLines * fontHeight + 4*(E_PADDING/2);
			//double y = nLines * fontHeight + 4*(E_PADDING/2) + commentRect.bottom;
			// END KGU#227 2016-07-31
			double x = y/lowest + ax - ay/lowest;
			// START KGU#435 2017-10-22: Enh. #128 revised
			//rect0.right = (int) Math.round(x);
			rect0.right = Math.max((int) Math.round(x), commentRect.right);
			// END KGU#435 2017-10-22
			//logger.debug("C => "+lowest+" ---> "+rect.right);
		}
		else
		{
			rect0.right = 4*(E_PADDING/2);
		}

		// START KGU#207 2016-07-21: Bugfix #198 - Inconsistency with draw() mended 
		//rect0.bottom = 4*(E_PADDING/2) + nLines*fontHeight;
		// START KGU#227 2016-07-31: Enh. #128
		//rect0.bottom = 4*(E_PADDING/2) + nLines*fontHeight - 1;
		rect0.bottom = 4*(E_PADDING/2) + nLines*fontHeight - 1 + commentRect.bottom;
		// END KGU#227 2016-07-31
		// END KGU#207 2016-07-21
		pt0Parting.y = rect0.bottom;
		
		rect0.right = Math.max(rect0.right, rTrue.right + rFalse.right);
		rect0.bottom += Math.max(rTrue.bottom, rFalse.bottom);
		pt0Parting.x = rTrue.right;
		// START KGU#1047 2022-07-31: Bugfix #1054 Comment exceeded the element width
		if (commentRect.right > rect0.right) {
			rect0.right = commentRect.right;
		}
		// END KGU#1047 2022-07-31

		// START KGU#516 2018-04-04: Issue #529 - reduced critical section
		//rect0.top = 0;
		//rect0.left = 0;
		this.rect0 = rect0;
		this.commentRect = commentRect;
		this.rFalse = rFalse;
		this.rTrue = rTrue;
		this.pt0Parting = pt0Parting;	// Why don't we simply move rFalse by pt0Parting to right?
		// END KGU#516 2018-04-04
		// START KGU#136 2016-03-01: Bugfix #97
		isRect0UpToDate = true;
		// END KGU#136 2016-03-01

		return rect0;
	}
	
	@Override
	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention)
	{
		// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
		if (!checkVisibility(_viewport, _top_left)) { return; }
		// END KGU#502/KGU#524/KGU#553 2019-03-13
		//logger.debug("ALT("+this.getText().getLongString()+") draw at ("+_top_left.left+", "+_top_left.top+")");
		if(isCollapsed(true)) 
		{
			Instruction.draw(_canvas, _top_left, getCollapsedText(), this, _inContention);
			// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
			wasDrawn = true;
			// END KGU#502/KGU#524/KGU#553 2019-03-14
			return;
		}
		
		Rect myrect = _top_left.copy();
		// START KGU 2015-10-13: All highlighting rules now encapsulated by this new method
		//Color drawColor = getColor();
		Color drawColor = getFillColor();
		// END KGU 2015-10-13
		// START KGU#494 2018-09-11: Issue #508 Retrieval concentrated for easier maintenance
		//FontMetrics fm = _canvas.getFontMetrics(Element.font);
		int fontHeight = getFontHeight(_canvas.getFontMetrics(Element.font));
		// END KGU#494 2018-09-11

		Canvas canvas = _canvas;
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		
		// START KGU#453 2017-11-01: Bugfix #447 - don't show end-standing backslashes
		//int nLines = getText(false).count();
		StringList myText = getCuteText(false);
		// START KGU#480 2018-01-21: Enh. #490
		if (Element.E_APPLY_ALIASES && !isSwitchTextCommentMode()) {
			myText = StringList.explode(Element.replaceControllerAliases(myText.getText(), true, Element.E_VARHIGHLIGHT), "\n");
		}
		// END KGU#480 2018-01-21
		int nLines = myText.count();
		// END KGU#453 2017-11-01

		myrect.bottom -= 1;
		canvas.fillRect(myrect);

		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		Point ref = this.getDrawPoint();
		this.topLeft.x = _top_left.left - ref.x;
		this.topLeft.y = _top_left.top - ref.y;
		// END KGU#136 2016-03-01
		
		// START KGU#227 2016-07-31: Enh. #128
		//myrect.bottom = _top_left.top + nLines*fontHeight + 4*(E_PADDING/2);
		myrect.bottom = _top_left.top + pt0Parting.y;
		// END KGU#227 2016-07-31

		int remain = (_top_left.right - _top_left.left)
				- (rTrue.right - rTrue.left)
				- (rFalse.right - rFalse.left);
		// START KGU#228 2016-07-31: Bugfix #212 - Condition was logically inverted
		//if (Element.altPadRight == false) remain=0;
		if (Element.altPadRight) remain=0;
		// END KGU#228 2016-07-31
		// START KGU#136 2016-03-07: Bugfix #122 - we must correct the else start point
		this.pt0Parting.x = rTrue.right - rTrue.left + remain; 
		// END KGU#136 2016-03-07

		// the upper left corner point (with reversed y coordinates)
		double cx = 0;
		// START KGU#227 2016-07-31: Enh. #128 - we rely on the cached value
		//double cy = nLines*fontHeight + 4*(E_PADDING/2);
		// START KGU#435 2017-10-22: Enh. #128 revised
		//double cy = pt0Parting.y + 1;
		double cy = pt0Parting.y + 1 - commentRect.bottom;
		// END KGU#435 2017-10-22
		// END KGU#227 2016-07-31
		// upper right corner
		double dx = _top_left.right - _top_left.left;
		double dy = cy;
		// the lowest point of the upper triangle
		double ax = pt0Parting.x;
		double ay = 0;
		// gradient coefficient of the left oblique line
		double coeffleft = (cy-ay)/(cx-ax);
		// coefficient of the right oblique line
		double coeffright = (dy-ay)/(dx-ax);

		// START KGU#227 2016-07-31: Enh. #128 - revised KGU#435 2017-10-22
		//int yOffset = 4*(E_PADDING/2) - (E_PADDING/3);
		// draw comment if required
		if (commentRect.bottom > 0)
		{			
			writeOutCommentLines(_canvas,
					_top_left.left + (E_PADDING/2),
					_top_left.top + E_PADDING/2, true);
		}
		// END KGU#227/KGU#435 2016-07-31 / 2017-10-22
		
		// START KGU#1182 2025-07-31: Enh. #1197 Allow to colourize branch headers
		if (!E_COLLECTRUNTIMEDATA ||
				E_RUNTIMEDATAPRESENTMODE == RuntimeDataPresentMode.NONE) {
			for (int i = 0; i < 2; i++) {
				if (this.selectedBranchHead == i || branchHeadColors[i] != null) {
					int top = _top_left.top + commentRect.bottom;
					Polygon tri;
					// Draw the respective triangle in the specified colour
					if (i == 0) {
						tri = new Polygon(
								new int[] {_top_left.left, _top_left.left + pt0Parting.x, _top_left.left},
								new int[] {top, _top_left.top + pt0Parting.y, _top_left.top + pt0Parting.y},
								3);
					}
					else {
						tri = new Polygon(
								new int[] {_top_left.left + pt0Parting.x, _top_left.right, _top_left.right},
								new int[] {_top_left.top + pt0Parting.y, top, _top_left.top + pt0Parting.y},
								3);
					}
					if (this.selectedBranchHead == i) {
						canvas.setColor(Element.E_DRAWCOLOR);
					}
					else {
						canvas.setColor(branchHeadColors[i]);
					}
					canvas.fillPoly(tri);
					canvas.setColor(drawColor);
				}
			}
		}
		// END KGU#1182 2025-07-31
		
		// draw text
		for (int i=0; i < nLines; i++)
		{
			// START KGU#453 2017-11-01: Bugfix #447 - don't show end-standing backslashes
			//String myLine = this.getText(false).get(i);
			String myLine = myText.get(i);
			// END KGU#453 2017-11-01

			// bottom line of the text
			// START KGU#435 2017-10-22: Enh. #128 revised
			//double by = yOffset + (nLines-i-1)*fontHeight;
			double by = 4*(E_PADDING/2) - (E_PADDING/3) + (nLines-i-1)*fontHeight;
			// END KGU#435 2017-10-22
			// part on the left side
			double leftside = by/coeffleft + ax - ay/coeffleft;
			// the bottom right point of this text line
			double bx = by/coeffright + ax - ay/coeffright;
			/* debugging output
                        canvas.setColor(Color.RED);
                        canvas.fillRect(new Rect(
                                myrect.left+(int) cx-2, myrect.bottom-(int) cy-2,
                                myrect.left+(int) cx+2, myrect.bottom-(int) cy+2)
                        );
                        canvas.moveTo(myrect.left+(int) leftside, myrect.bottom-(int) by);
                        canvas.lineTo(myrect.left+(int) bx, myrect.bottom-(int) by);
			 */
			int boxWidth = (int) (bx-leftside);
			int textWidth = getWidthOutVariables(_canvas, myLine, this);

			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
					_top_left.left + (E_PADDING/2) + (int) leftside + (int) (boxWidth - textWidth)/2,
					// START KGU#227 2016-07-31: Enh. #128
					//_top_left.top + (E_PADDING / 3) + (i+1)*fontHeight,
					_top_left.top + (E_PADDING/3) + commentRect.bottom + (i+1)*fontHeight,
					// END KGU#227 2016-07-31
					myLine, this, _inContention
					);

			/*
			if(rotated==false)
			{
				canvas.setColor(Color.BLACK);
				writeOutVariables(canvas,
								  x-Math.round(_canvas.stringWidth(text)/2),
								_top_left.top+Math.round(E_PADDING / 3)+(i+1)*fontHeight,
								text
								);  	
			}
			else
			{
				// draw rotated
				
				// coloredTextOut(text[i],vars,colors,_canvas,_top_left.Left+(E_PADDING div 2)+i*_canvas.TextHeight(text[i]),
				// _top_left.bottom-(E_PADDING div 2),rotated);
				
			}
			 */
		}
		
		// draw branch labels
		canvas.writeOut(myrect.left + (E_PADDING / 2),
						myrect.bottom - (E_PADDING / 2), preAltT);
		canvas.writeOut(myrect.right - (E_PADDING / 2) -_canvas.stringWidth(preAltF),
						myrect.bottom - (E_PADDING / 2), preAltF);
		
		// draw comment
		if (Element.E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
		{
			this.drawCommentMark(canvas, myrect);
		}
		// START KGU 2015-10-11
		// draw breakpoint bar if necessary
		this.drawBreakpointMark(canvas, myrect);
		// END KGU 2015-10-11
		
		// START KGU#906 2021-01-02: Enh. #905
		this.drawWarningSignOnError(canvas, myrect);
		// END KGU#906 2021-01-02

		// START KGU#156 2016-03-11: Enh. #124
		// write the run-time info if enabled
		// START KGU#435 2017-10-22: Enh. #128 revised
		//this.writeOutRuntimeInfo(_canvas, 
		//		_top_left.left + rect.right - (int)Math.round(fontHeight / coeffright),
		//		_top_left.top);
		int rightOffset = (int)Math.round(fontHeight / coeffright);
		if (commentRect.right > 0) rightOffset = E_PADDING/2;
		this.writeOutRuntimeInfo(_canvas, 
				_top_left.left + rect.right - rightOffset,
				_top_left.top);
		// END KGU#435 2017-10-22
		// END KGU#156 2016-03-11
				
		// draw triangle
		canvas.setColor(Color.BLACK);
		// START KGU#435 2017-10-22: Enh. #128 revised
		//canvas.moveTo(myrect.left, myrect.top);
		//canvas.lineTo(myrect.left + rTrue.right-1 + remain, myrect.bottom-1);
		//canvas.lineTo(myrect.right, myrect.top);
		canvas.moveTo(myrect.left, myrect.top + commentRect.bottom);
		canvas.lineTo(myrect.left + rTrue.right-1 + remain, myrect.bottom-1);
		canvas.lineTo(myrect.right, myrect.top + commentRect.bottom);
		// END KGU#435 2017-10-22
		
		// START KGU#277 2016-10-13: Enh. #270
		// START KGU#1080 2025-07-02: Bugfix #1195 Should also be hatched if indirectly disabled
		//if (this.disabled) {
		if (this.isDisabled(false)) {
		// END KGU#1080 2025-07-02
			canvas.hatchRect(myrect, 5, 10);
		}
		// END KGU#277 2016-10-13

			// draw children
		myrect = _top_left.copy();

		// START KGU#207 2016-07-21: Bugfix #198 - this offset difference to pt0Parting.y spoiled selection traversal 
		//myrect.top = _top_left.top + fontHeight*nLines + 4*(E_PADDING / 2)-1;
		myrect.top = _top_left.top + this.pt0Parting.y;
		// END KGU#207 2016-07-21
		myrect.right = myrect.left + rTrue.right-1 + remain;
		
		qTrue.draw(_canvas, myrect, _viewport, _inContention);
		
		myrect.left = myrect.right;
		myrect.right = _top_left.right;
		qFalse.draw(_canvas, myrect, _viewport, _inContention);
		
		
		myrect = _top_left.copy();
		canvas.setColor(Color.BLACK);
		canvas.drawRect(myrect);
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
		wasDrawn = true;
		// END KGU#502/KGU#524/KGU#553 2019-03-14
	}
	
	// START KGU#122 2016-01-03: Collapsed elements may be marked with an element-specific icon
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getIcon()
	 */
	@Override
	public ImageIcon getIcon()
	{
		return IconLoader.getIcon(60);
	}
	// END KGU#122 2016-01-03
	
	// START KGU#535 2018-06-28
	/**
	 * @return the (somewhat smaller) element-type-specific icon image intended to be used in
	 * the {@link FindAndReplace} dialog.
	 * @see #getIcon()
	 */
	@Override
	public ImageIcon getMiniIcon()
	{
		return IconLoader.getIcon(13);
	}
	// END KGU 2018-06-28
	
	// START KGU 2015-10-09: On moving the cursor, substructures had been eclipsed
	// by their containing box w.r.t. comment popping etc. This correction, however,
	// might significantly slow down the mouse tracking on enabled comment popping.
    // Just give it a try... 
	//public Element selectElementByCoord(int _x, int _y)
	@Override
	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		Element selMe = super.getElementByCoord(_x,_y, _forSelection);
		// START KGU#1182 2025-07-31: Enh. #1197 Clear branch header selection too
		if (selMe == null && _forSelection) {
			selectedBranchHead = -1;
		}
		// END KGU#1182 2025-07-31
		// START KGU#121 2016-01-03: Bugfix #87 - A collapsed element has no visible substructure!
		// START KGU#207 2016-07-21: Bugfix #198 - If this is not hit then there is no need to check the children
		//if (!this.isCollapsed())
		if ((selMe != null || _forSelection) && !this.isCollapsed(true))
		// END KGU#207 2016-07-21
		{
		// END KGU#121 2016-01-03
			// START KGU#136 2016-03-01: Bugfix #97 - we use local coordinates now
			//Element selT = qTrue.getElementByCoord(_x,_y, _forSelection);
			//Element selF = qFalse.getElementByCoord(_x,_y, _forSelection);
			Element selT = qTrue.getElementByCoord(_x, _y-pt0Parting.y, _forSelection);
			// START KGU#346 2017-02-08: Bugfix #198 (flawed horizontal navigation)
			//Element selF = qFalse.getElementByCoord(_x-pt0Parting.x, _y-pt0Parting.y, _forSelection);
			Element selF = qFalse.getElementByCoord(_x-pt0Parting.x+1, _y-pt0Parting.y, _forSelection);
			// END KGU#346 2017-02-08
			// END KGU#136 2016-03-01
			if (selT != null) 
			{
				// START KGU#1182 2025-07-31: Enh. #1197 Clear branch header selection too
				//if (_forSelection) selected = false;
				if (_forSelection) {
					selected = false;
					selectedBranchHead = -1;
				}
				// END KGU#1182 2025-07-31
				selMe = selT;
			}
			else if (selF != null)
			{
				// START KGU#1182 2025-07-31: Enh. #1197 Clear branch header selection too
				//if (_forSelection) selected = false;
				if (_forSelection) {
					selected = false;
					selectedBranchHead = -1;
				}
				// END KGU#1182 2025-07-31
				selMe = selF;
			}
		// START KGU#121 2016-01-03: Bugfix #87 (continued)
		}
		// END KGU#121 2016-01-03

		return selMe;
	}
	// END KGU 2015.10.09
	
	// START KGU#346 2017-02-08: Issue #198 Provide a relative rect for the head
	/**
	 * Returns a copy of the (relocatable i. e. 0-bound) extension rectangle
	 * of the head partition (condition and branch labels). 
	 * @return a rectangle starting at (0,0) and spanning to (width, head height) 
	 */
	@Override
	public Rect getHeadRect()
	{
		return new Rect(rect.left, rect.top, rect.right, this.pt0Parting.y);
	}
	// END KGU#346 2017-02-08

	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	@Override
	public Element findSelected()
	{
		Element sel = selected ? this : null;
		if (sel == null && (sel = qTrue.findSelected()) == null)
		{
			sel = qFalse.findSelected();
		}
		return sel;
	}
	// END KGU#183 2016-04-24
	
	@Override
	public Element copy()
	{
		Alternative ele = new Alternative(this.getText().copy());
		copyDetails(ele, true);
		ele.qTrue  = (Subqueue)this.qTrue.copy();
		ele.qFalse = (Subqueue)this.qFalse.copy();
		ele.qTrue.parent  = ele;
		ele.qFalse.parent = ele;
		// START KGU#1182 2025-07-31: Enh. #1197 Colours for branch heads
		for (int i = 0; i < this.branchHeadColors.length; i++) {
			ele.branchHeadColors[i] = this.branchHeadColors[i]; 
		}
		// END KGU#1182 2025-07-31
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
		boolean isEqual = super.equals(_another);
		if (isEqual)
		{
			isEqual = this.qTrue.equals(((Alternative)_another).qTrue) &&
					this.qFalse.equals(((Alternative)_another).qFalse);
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
		if (isEqual)
		{
			isEqual = this.qTrue.combineRuntimeData(((Alternative)_cloneOfMine).qTrue) &&
					this.qFalse.combineRuntimeData(((Alternative)_cloneOfMine).qFalse);			
		}
		return isEqual;
	}
	// END KGU#117 2016-03-07

	// START KGU#156 2016-03-13: Enh. #124
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
	// END KGU#156 2016-03-11

	// START KGU#117 2016-03-10: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isTestCovered(boolean)
	 */
	@Override
	public boolean isTestCovered(boolean _deeply)
	{
		return this.qTrue.isTestCovered(_deeply) && this.qFalse.isTestCovered(_deeply);
	}
	// END KGU#117 2016-03-10

	// START KGU 2015-10-16
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
	protected void addFullText(StringList _lines, boolean _instructionsOnly)
	{
		if (!this.isDisabled(false)) {
			if (!_instructionsOnly) {
				// START KGU#453 2017-11-01: Bugfix 447 Someone might have placed line continuation backslashes...
				//_lines.add(this.getText());	// Text of the condition
				_lines.add(this.getUnbrokenText().getLongString());	// Text of the condition as a single line
				// END KGU#453 2017-11-01
			}
			this.qTrue.addFullText(_lines, _instructionsOnly);
			this.qFalse.addFullText(_lines, _instructionsOnly);
		}
	}
	// END KGU 2015-10-16
	
	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 */
	@Override
	public void convertToCalls(StringList _signatures) {
		this.qTrue.convertToCalls(_signatures);
		this.qFalse.convertToCalls(_signatures);
	}
	// END KGU#199 2016-07-07

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#traverse(lu.fisch.structorizer.elements.IElementVisitor)
	 */
	@Override
	public boolean traverse(IElementVisitor _visitor) {
		boolean proceed = _visitor.visitPreOrder(this);
		if (proceed)
		{
			proceed = qTrue.traverse(_visitor) && qFalse.traverse(_visitor);
		}
		if (proceed)
		{
			proceed = _visitor.visitPostOrder(this);
		}
		return proceed;
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRelevantParserKeys()
	 */
	@Override
	protected String[] getRelevantParserKeys() {
		return relevantParserKeys;
	}
	
	// START KGU 2017-10-21
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#mayPassControl()
	 */
	@Override
	public boolean mayPassControl()
	{
		// An alternative may only pass control if being disabled or containing at least one
		// passable branch. We don't check whether the condition is satisfiable.
		return disabled || this.qTrue.mayPassControl() || this.qFalse.mayPassControl();
	}
	// END KGU 2017-10-21
	
	// START KGU#602 2018-10-25: Issue #419 - Mechanism to detect and handle long lines
	/**
	 * Detects the maximum text line length either on this very element 
	 * @param _includeSubstructure - whether (in case of a complex element) the substructure
	 * is to be involved
	 * @return the maximum line length
	 */
	@Override
	public int getMaxLineLength(boolean _includeSubstructure)
	{
		int maxLen = super.getMaxLineLength(false);
		if (_includeSubstructure) {
			maxLen = Math.max(maxLen, this.qTrue.getMaxLineLength(true));
			maxLen = Math.max(maxLen, this.qFalse.getMaxLineLength(true));
		}
		return maxLen;
	}
	// END KGU#602 2018-10-25

	// START KGU#1182 2025-07-31: Enh. #1197 Allow to subselect branch headers
	@Override
	public int getBranchCount() {
		return 2;
	}

	@Override
	public boolean selectBranchHead(int _relX, int _relY) {
		int oldSel = this.selectedBranchHead;
		if (this.wasDrawn && this.isRect0UpToDate && !isCollapsed(true)) {
			/* The TRUE header triangle is selected if _relX is between 0 and
			 * pt0Parting.x and _relY is above the oblique line from origin to
			 * pt0Parting.
			 * We use the determinant of matrix {{selX, selY, 1}, {0,0,1},
			 * {pt0Parting.x, pt0Parting.y, 1}} to decide this (must be > 0)
			 */
			if (_relX > 0 && _relX < pt0Parting.x) {
				double det = _relY * pt0Parting.x - _relX * pt0Parting.y;
				if (det > 0) {
					this.selectedBranchHead = 0;
				}
			}
			/* The FALSE header triangle is selected if _relX is between
			 * pt0Parting.x and rect0.right and _relY is above the oblique
			 * line from pt0Parting to upper right corner of rect0.
			 * We use the determinant of matrix {{selX, selY, 1},
			 * {pt0Parting.x, pt0Parting.y, 1}, {rect.right,0,1},} to decide
			 * this (must be > 0)
			 */
			else if (_relX > pt0Parting.x && _relX < rect0.right) {
				double det = _relX * pt0Parting.y + _relY * rect.right
						- _relY * pt0Parting.x - rect.right * pt0Parting.y;
				if (det > 0) {
					this.selectedBranchHead = 1;
				}
			}
			else {
				this.selectedBranchHead = -1;
			}
		}
		return oldSel != this.selectedBranchHead;
	}
	
	@Override
	public int getSelectedBranchHead()
	{
		return this.selectedBranchHead;
	}

	@Override
	public Color getBranchHeadColor(int _branchIndex) {
		if (_branchIndex < 0 || _branchIndex > 1) {
			return null;
		}
		return this.branchHeadColors[_branchIndex];
	}
	
	@Override
	public String getHexBranchColorList()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.getBranchCount(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(getHexColor(this.getBranchHeadColor(i)));
		}
		return sb.toString();
	}

	@Override
	public boolean setBranchHeadColor(int _branchIndex, Color _branchColor) {
		if (_branchIndex < 0 || _branchIndex > 1) {
			return false;
		}
		this.branchHeadColors[_branchIndex] = _branchColor;
		return true;
	}
	
	@Override
	public Element setSelected(boolean _sel)
	{
		this.selectedBranchHead = -1;
		return super.setSelected(_sel);
	}
	
	@Override
	public void setColor(Color _color)
	{
		if (this.selectedBranchHead >= 0) {
			this.branchHeadColors[this.selectedBranchHead] = _color;
		}
		else if (_color == null) {
			// Wipe all branch head colours (if any)
			for (int i = 0; i < 2; i++) {
				this.branchHeadColors[i] = null;
			}
		}
		else {
			super.setColor(_color);
		}
	}
	
	@Override
	protected Color getFillColor(DrawingContext drawingContext)
	{
		Color fillColor = super.getFillColor(drawingContext);
		// Check if the colour is the designated selection colour
		if (this.getSelected(drawingContext) && this.selectedBranchHead != -1
				&& fillColor == Color.YELLOW) {
			// In case a branch head is selected, use the element background colour
			fillColor = this.getColor();
		}
		return fillColor;
	}
	// END KGU#1182 2025-07-31

}

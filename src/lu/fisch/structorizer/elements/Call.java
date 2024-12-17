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
 *      Bob Fisch       2007-12-13      First Issue
 *      Kay Gürtzig     2015-10-11      Method selectElementByCoord(int,int) replaced by getElementByCoord(int,int,boolean)
 *      Kay Gürtzig     2015-10-12      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015-11-14      Bugfix #31 (= KGU#82) in method copy
 *      Kay Gürtzig     2015-12-01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *      Kay Gürtzig     2015-01-03      Enh. #87 (KGU#122) -> getIcon()
 *      Kay Gürtzig     2015-03-01      Bugfix #97 (KGU#136) Steady selection mechanism
 *      Kay Gürtzig     2016-03-12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016-04-24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016-07-07      Enh. #188: New copy constructor to support conversion (KGU#199)
 *      Kay Gürtzig     2016-07-19      Enh. #160: New method getSignatureString()
 *      Kay Gürtzig     2016-07-30      Enh. #128: New mode "comments plus text" supported, drawing code delegated
 *      Kay Gürtzig     2017-02-20      Enh. #259: Retrieval of result types of called functions enabled (q&d)
 *      Kay Gürtzig     2017-04-11      Enh. #389: Support for "import" flavour. Withdrawn 2017-ß07-01 
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-03-28      Enh. #657: Retrieval for called subroutine now with group filter
 *      Kay Gürtzig     2019-03-30      Enh. #696: subroutine retrieval now possible from an alternative pool
 *      Kay Gürtzig     2021-01-04      Enh. #906: New field pauseAfterCall to support the debug mode "step over"
 *      Kay Gürtzig     2021-02-26      Enh. #410: New field isMethodDeclaration and method derivates for
 *                                      the representation of imported methods (OOP approach)
 *      Kay Gürtzig     2024-03-22      Issue #1154: Modified drawing of CALLs diverted for method declarations
 *      Kay Gürtzig     2024-04-17      Bugfix #1160: Rectification of rotated drawing
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      Until 2015, this class had not had any specific effect on execution and code export. This was
 *      changed by enhancement requests #9.
 *      Though chiefly the Executor (and perhaps some code generators) is concerned, this class file
 *      seems to be a good place to state the general ideas behind the Call element as now being handled.
 *      1. In order to get a Call working, it must refer to a another Nassi-Shneiderman diagram or
 *         just the diagram itself (recursive routine).
 *         There are thre flavours of a Call:
 *         a) procedure call: <proc_name>(<param_list>)
 *         b) function call: <var_name> <- <func_name>(<param_list>)
 *         c) code import: import <prog_name>
 *      2. The called diagram is required to be
 *         a, b) a function diagram and must match the "method name" (case-sensitive!) and parameter
 *               count of the call.
 *         c)    a program diagram
 *      3. To keep things simple, the call text must consist of a single instruction line,
 *         being
 *         a) a procedure call:
 *             <proc_name> ( <value1>, <value2>, ... , <value_n> )
 *         b) a variable assignment with a single function call as expression:
 *             <var_name> <- <func_name> ( <value1>, <value2>, ... , <value_n> )
 *         c) an import instruction (aiming in a kind of macro execution within the current root's context):
 *             import <prog_name>
 *      4. A direct output instruction with a function result is not supported like in:
 *             OUT foreign(something).
 *         Hence to use the results of a foreign call, first assign the value to a variable within
 *         a Call element, then use the variable as part of some expression in an ordinary
 *         Instruction element.
 *      5. Nested or multiple subroutine calls as in the following examples are not supported
 *             foreign(x, foreign(y, a))
 *             result <- foreign(a) + foreign(b)
 *         Workaround: analogous to 4.)
 *      6. The called diagram must be opened and held in a container accessible by the Structorizer
 *         (e.g. Arranger surface or a tab list of the Structorizer itself) in order to make the call
 *         work on execution.
 *      7. Whether a returned value is required and in this case of what type will only dynamically be
 *         relevant on execution (interpreted code). There is no check in advance.
 *
 *****************************************************************************************************///

import java.awt.Color;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.gui.FindAndReplace;
import lu.fisch.structorizer.gui.IconLoader;

/**
 * This Structorizer class represents a procedure or function Call in a diagram.
 * 
 * @author Bob Fisch
 */
public class Call extends Instruction {
	
	// START KGU#907 2021-01-04: Enh. #906 - special break mechanism for stepping through Calls
	/** One-time flag for pausing after execution */
	public boolean pauseAfterCall = false;
	// END KGU#907 2021-01-04
	// START KGU#408 2021-02-26: Enh. #410
	/** Is this a method declaration in a class-representing Includable rather than a call? */
	public boolean isMethodDeclaration = false;
	// END KGU#408 2021-02-26

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
		setText(_strings);	// FIXME (KGU 2016-07-07): What is this good for (see above)? 
	}
	
	// START KGU#199 2016-07-07: New for enh. #188
	public Call(Instruction instr)
	{
		super(instr);
	}
	// END KGU#199 2016-07-07
	
	// START KGU#227 2016-07-30: Enh. #128
	// START KGU#1150 2024-04-16: Bugfix #1160 For rotation we need X and Y
	///**
	// * Provides a subclassable left offset for drawing the text
	// */
	//@Override
	//protected int getTextDrawingOffset()
	//{
	//	return (Element.E_PADDING/2);
	//}
	@Override
	protected int getTextDrawingOffsetX()
	{
		return rotated ? 0 : (Element.E_PADDING/2);
	}
	@Override
	protected int getTextDrawingOffsetY()
	{
		return rotated ? (Element.E_PADDING/2) : 0;
	}
	// END KGU#1150 2024-04-16
	// END KGU#227 2016-07-30

	@Override
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRect0UpToDate) return rect0;
		// END KGU#136 2016-03-01

		// START KGU#227 2016-07-30: Enh. #128 - on this occasion, we just enlarge the instruction rect width
		super.prepareDraw(_canvas);
		// START KGU#1150 2024-04-16: Bugfix #1160 We must consider rotation
		//rect0.right += 2*(E_PADDING/2);
		if (rotated) {
			// The border bars will virtually be drawn above and below the tetxt
			rect0.bottom += 2*(E_PADDING/2); 
		}
		else {
			rect0.right += 2*(E_PADDING/2);			
		}
		// END KGU#1150 2024-04-16
		// END KGU#227 2016-07-30
		return rect0;
	}
	
	@Override
	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention)
	{
		// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
		if (!checkVisibility(_viewport, _top_left)) { return; }
		// END KGU#502/KGU#524/KGU#553 2019-03-13
		// START KGU 2016-07-30: Just delegate the basics to super
		super.draw(_canvas, _top_left, _viewport, _inContention);
		// END KGU 2016-07-30: Just delegate the basics to super
		
		// Now draw the Call-specific lines
		// START KGU#408 2021-02-26: Enh. #410 - textColor will reflect the isMethodDeclaration
		//_canvas.setColor(Color.BLACK);
		_canvas.setColor(this.getTextColor());
		// END KGU#408 2021-02-26
		_canvas.moveTo(_top_left.left  + (E_PADDING / 2), _top_left.top);
		_canvas.lineTo(_top_left.left  + (E_PADDING / 2), _top_left.bottom);
		_canvas.moveTo(_top_left.right - (E_PADDING / 2), _top_left.top);
		_canvas.lineTo(_top_left.right - (E_PADDING / 2), _top_left.bottom);
		
		// START KGU 2016-07-30: Just delegate the basics to super
//		_canvas.setColor(Color.BLACK);
//		_canvas.drawRect(_top_left);
		// END KGU 2016-07-30: Just delegate the basics to super
	}
	
	// START KGU#1142 2024-03-22: Issue #1154 Intended to be subclassed for special purposes
	/**
	 * Draws a dark grey hatch pattern into the given rectangle {@code rect} on the
	 * {@link Canvas} {@code canvas}.
	 * 
	 * @param myrect - the rectangle to be (partially) hatched
	 * @param canvas - the target canvas
	 */
	@Override
	protected void drawHatched(Rect rect, Canvas canvas) {
		if (this.isMethodDeclaration) {
			super.drawHatched(new Rect(rect.left, rect.top, rect.left + (E_PADDING / 2), rect.bottom), canvas);
			super.drawHatched(new Rect(rect.right - (E_PADDING / 2), rect.top, rect.right, rect.bottom), canvas);
		}
		else {
			super.drawHatched(rect, canvas);
		}
	}
	// END KGU#1142 2024-03-22

	// START KGU 2016-07-30: Adapt the runtime info position
	/**
	 * Writes the selected runtime information in half-size font to the lower
	 * left of position (_right, _top).
	 * @param _canvas - the Canvas to write to
	 * @param _right - right border x coordinate
	 * @param _top - upper border y coordinate
	 */
	@Override
	protected void writeOutRuntimeInfo(Canvas _canvas, int _right, int _top)
	{
		super.writeOutRuntimeInfo(_canvas, _right - (Element.E_PADDING/2), _top);
	}
	// END KGU 2016-07-30
	
	// START KGU#122 2016-01-03: Enh. #87 - Collapsed elements may be marked with an element-specific icon
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Instruction#getIcon()
	 */
	@Override
	public ImageIcon getIcon()
	{
		// START KGU#408 2021-02-27: Enh. #410 Now same mechanism as in Instruction needed
		if (E_HIDE_DECL && this.isMereDeclaratory() && this == this.getDrawingSurrogate(false)) {
			return IconLoader.getIcon(85);
		}
		// END KGU#408 2021-02-27
		return IconLoader.getIcon(58);
	}
	// END KGU#122 2016-01-03
	
	// START KGU 2018-06-28
	/**
	 * @return the (somewhat smaller) element-type-specific icon image intended to be used in
	 * the {@link FindAndReplace} dialog.
	 * @see #getIcon()
	 */
	@Override
	public ImageIcon getMiniIcon()
	{
		return IconLoader.getIcon(11);
	}
	// END KGU 2018-06-28

	@Override
	public Element copy()
	{
		Element ele = new Call(this.getText().copy());
		// START KGU#408 2021-02-26: Enh. #410 May be used as mere reference
		((Call)ele).isMethodDeclaration = this.isMethodDeclaration;
		// END KGU#408 2021-02-26
		// START KGU#199 2016-07-07: Enh. #188, D.R.Y.
		return copyDetails(ele, false, true);
		// END KGU#199 2016-07-07
	}
	
	// START KGU#408 2021-02-26: Enh. #410
	@Override
	public boolean isDisabled(boolean individually)
	{
		return super.isDisabled(individually) || this.isMethodDeclaration;
	}
	
	@Override
	protected Color getTextColor()
	{
		Color declColor = Color.DARK_GRAY;
		if (!this.isMethodDeclaration || declColor.equals(getFillColor())) {
			return super.getTextColor();
		}
		return declColor;
	}
	// END KGU#408 2021-02-26
	
//	// START KGU#376 2017-04-11: Enh. #389 - secific support for import calls / KGU#376 2017-07-01 withdrawn
//	/* (non-Javadoc)
//	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
//	 */
//	@Override
//    protected void addFullText(StringList _lines, boolean _instructionsOnly, HashSet<Root> _implicatedRoots)
//    {
//		if (!this.isDisabled()) {
//			if (this.isImportCall()) {
//				// Get all lines of the called routine
//				String name = this.getSignatureString();
//				if (Arranger.hasInstance()) {
//					Vector<Root> roots = Arranger.getInstance().findIncludesByName(name);
//					if (roots.size() == 1) {
//						roots.get(0).addFullText(_lines, _instructionsOnly, _implicatedRoots);
//					}
//				}		
//			}
//			else {
//				super.addFullText(_lines, _instructionsOnly, _implicatedRoots);
//			}
//		}
//    }
//    // END KGU#376 2017-04-11

	
	// START #178 2016-07-19: Enh. #160
	/**
	 * Returns a string of form "&lt;function_name&gt;(&lt;parameter_count&gt;)"
	 * describing the signature of the called routine if the text is conform to
	 * a procedure or function call syntax as described in the user guide.
	 * Otherwise {@code null} will be returned.
	 * @return signature string, e.g. "factorial(1)", or null
	 */
	public String getSignatureString()
	{
		String signature = null;
		Function fct = this.getCalledRoutine();
		if (fct != null)
		{
			// START KGU#261 2017-02-20: Unified with Root.getSignatureString(false)
			//signature = fct.getName() + "#" + fct.paramCount();
			signature = fct.getSignatureString();
			// END KGU#261 2017-02-20
		}
		return signature;
	}
	// END #178 2016-07-19
	
//	// START KGU#1036 2024-11-22: Workaround for bug #1180 (alternative approach, withdrawn 2024-11-24)
//	/**
//	 * Detects shallow or deep test coverage of this element according to the
//	 * argument _deeply
//	 * @param _deeply if exhaustive coverage (including subroutines is requested)
//	 * @return true iff element and all its sub-structure is test-covered
//	 */
//	@Override
//	public boolean isTestCovered(boolean _deeply)
//	{
//		if (_deeply && this.simplyCovered && !this.deeplyCovered && Arranger.hasInstance()) {
//			// We have to check if called routine(s) may have turned completely covered meanwhile
//			StringList sl = this.getUnbrokenText();
//			if (sl.count() > 1) {
//				Root myRoot = getRoot(this);
//				for (int i = 0; i < sl.count(); i++) {
//					Function called = getCalledRoutine(i);
//					if (called != null) {
//						// The retrieval is restricted to the Arranger pool, which may not be consistent with Executor
//						Vector<Root> candidates = Arranger.getInstance()
//								.findRoutinesBySignature(called.getName(), called.paramCount(), myRoot, true);
//						// If unique then update the deeplyCovered attribute accordingly
//						if (candidates.size() != 1 || !candidates.get(0).isTestCovered(true)) {
//							return false;	// No further check needed
//						}
//					}
//				}
//				this.deeplyCovered = true;
//				if (this.parent != null) {
//					this.parent.checkTestCoverage(true);
//				}
//			}
//		}
//		// The following ought to be identical with super
//		return _deeply ? this.deeplyCovered : this.simplyCovered;
//	}
//	// END KGU#1036 2024-11-22
	
	// START KGU#408 2021-02-26: Enh. #410 Differing behaviour in case of a method reference
	/**
	 * Returns a {@link Function} object describing the signature of
	 * <ul>
	 * <li> the referred method declaration (in case this obect represents a method reference),</li>
	 * <li> the called routine if the text complies to the call syntax described in the user guide,</li>
	 * <li> {@code null} otherwise. (Note that this may be the case if the element consists of more than
	 *     one line!)</li>
	 * </ul>
	 * @return Function object or {@code null}.
	 * 
	 * @see #isFunctionCall(boolean)
	 * @see #isProcedureCall(boolean)
	 */
	@Override
	public Function getCalledRoutine()
	{
		// START KGU#408 2021-02-26: Enh. #410 Different kind of retrieval for method declaration
		if (this.isMethodDeclaration) {
			String decl = this.getText().getLongString();
			StringList paramNames = new StringList();
			boolean hasParamList = Root.extractMethodParamDecls(decl, paramNames, null, null);
			String methodName = Root.getMethodName(decl, Root.DiagramType.DT_SUB, true);
			if (hasParamList) {
				return new Function(methodName + "(" + paramNames.concatenate(", ") + ")");
			}
			return null;
		}
		// END KGU#408 2021-02-26
		return super.getCalledRoutine();
	}
	
	/**
	 * @return true iff this Call contains a method declaration reference.
	 * @see #isMereDeclaration(String) 
	 */
	public boolean isMereDeclaratory()
	{
		return isMethodDeclaration || super.isMereDeclaratory();
	}
	
	public void updateTypeMapFromLine(HashMap<String, TypeMapEntry> typeMap, String line, int lineNo)
	{
		// Don't do anything if it is a method declaration reference
		if (!isMethodDeclaration) {
			super.updateTypeMapFromLine(typeMap, line, lineNo);
		}
	}
	// END KGU#408 2021-02-26
	
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

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRelevantParserKeys()
	 */
	@Override
	protected String[] getRelevantParserKeys() {
		// START KGU#376 2017-04-11: Enh. #389 Now there is...
		return null;
		//return relevantParserKeys;
		// END KGU#376 2017-04-11
	}

	// START KGU#261 2017-02-20: Enh. #259 - Allow to retrieve return type info
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Instruction#getTypeFromAssignedValue(lu.fisch.utils.StringList, java.util.HashMap)
	 */
	@Override
	protected String getTypeFromAssignedValue(StringList rightSide, HashMap<String, TypeMapEntry> knownTypes)
	{
		String typeSpec = "";
		Function called = this.getCalledRoutine();
		if (called != null) {
			String signature = called.getSignatureString();
			Root myRoot = Element.getRoot(this);
			if (myRoot.getSignatureString(false, false).equals(signature)) {
				typeSpec = myRoot.getResultType();
			}
			// START KGU#676 2019-03-31: Issue #696 batch export
			//else if (Arranger.hasInstance()) {
			//	Vector<Root> routines = Arranger.getInstance().findRoutinesBySignature(called.getName(), called.paramCount(), myRoot);
			else if (myRoot.specialRoutinePool != null || Arranger.hasInstance()) {
				IRoutinePool pool = myRoot.specialRoutinePool;
				if (pool == null) { pool = Arranger.getInstance(); }
				Vector<Root> routines = pool.findRoutinesBySignature(called.getName(), called.paramCount(), myRoot, true);
			// END KGU#676 2019-03-31
				if (routines.size() == 1) {
					typeSpec = routines.get(0).getResultType();
				}
			}
			if (typeSpec == null) {
				typeSpec = "";
			}
		}
		return typeSpec;
	}
	// END KGU#261 2017-02-20
	
}

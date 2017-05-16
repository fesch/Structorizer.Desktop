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

/*
 ******************************************************************************************************
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
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *      Kay Gürtzig     2015.01.03      Enh. #87 (KGU#122) -> getIcon()
 *      Kay Gürtzig     2015.03.01      Bugfix #97 (KGU#136) Steady selection mechanism
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.07      Enh. #188: New copy constructor to support conversion (KGU#199)
 *      Kay Gürtzig     2016.07.19      Enh. #160: New method getSignatureString()
 *      Kay Gürtzig     2016.07.30      Enh. #128: New mode "comments plus text" supported, drawing code delegated
 *      Kay Gürtzig     2017.02.20      Enh. #259: Retrieval of result types of called functions enabled (q&d)
 *      Kay Gürtzig     2017.04.11      Enh. #389: Support for "import" flavour.
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
 ******************************************************************************************************
 */

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.ImageIcon;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.parsers.CodeParser;

public class Call extends Instruction {
	
	// START KGU#376 2017-04-11: Enh. #389
	private static final String[] relevantParserKeys = {"preImport"};
	// END KGU#376 2017-04-11

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
		rect0.right += 2*(E_PADDING/2);
		// END KGU#227 2016-07-30
		return rect0;
	}
	
	public void draw(Canvas _canvas, Rect _top_left)
	{
		// START KGU 2016-07-30: Just delegate the basics to super
		super.draw(_canvas, _top_left);
		// END KGU 2016-07-30: Just delegate the basics to super
		
		// Now draw the Call-specific lines
		_canvas.setColor(Color.BLACK);
		_canvas.moveTo(_top_left.left  + (E_PADDING / 2), _top_left.top);
		_canvas.lineTo(_top_left.left  + (E_PADDING / 2), _top_left.bottom);
		_canvas.moveTo(_top_left.right - (E_PADDING / 2), _top_left.top);
		_canvas.lineTo(_top_left.right - (E_PADDING / 2), _top_left.bottom);
		
		// START KGU 2016-07-30: Just delegate the basics to super
//		_canvas.setColor(Color.BLACK);
//		_canvas.drawRect(_top_left);
		// END KGU 2016-07-30: Just delegate the basics to super
	}
	
	// START KGU 2016-07-30: Adapt the runtime info position
	/**
	 * Writes the selected runtime information in half-size font to the lower
	 * left of position (_right, _top).
	 * @param _canvas - the Canvas to write to
	 * @param _right - right border x coordinate
	 * @param _top - upper border y coordinate
	 */
	protected void writeOutRuntimeInfo(Canvas _canvas, int _right, int _top)
	{
		super.writeOutRuntimeInfo(_canvas, _right - (Element.E_PADDING/2), _top);
	}
	// END KGU 2016-07-30
	
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
		// START KGU#199 2016-07-07: Enh. #188, D.R.Y.
		return copyDetails(ele, false, true);
		// END KGU#199 2016-07-07
	}
	
	// START KGU#376 2017-04-11: Enh. #389 - secific suport for import calls
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
	 */
	@Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly, HashSet<Root> _implicatedRoots)
    {
		if (!this.isDisabled()) {
			if (this.isImportCall()) {
				// Get all lines of the called routine
				String name = this.getSignatureString();
				if (Arranger.hasInstance()) {
					Vector<Root> roots = Arranger.getInstance().findIncludesByName(name);
					if (roots.size() == 1) {
						roots.get(0).addFullText(_lines, _instructionsOnly, _implicatedRoots);
					}
				}		
			}
			else {
				super.addFullText(_lines, _instructionsOnly, _implicatedRoots);
			}
		}
    }
    // END KGU#376 2017-04-11

	
	// START #178 2016-07-19: Enh. #160
	/**
	 * Returns a string of form "&lt;function_name&gt;(&lt;parameter_count&gt;)"
	 * describing the signature of the called routine if the text is conform to
	 * a procedure or function call syntax as described in the user guide. If the
	 * call text matches the syntax of an import call then the returned signature
	 * will just be a program name. Otherwise null will be returned.
	 * @return signature string, e.g. "factorial(1)", "globalDefs", or null
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
		// START KGU#376 2017-04-11: Enh. #389 import mechanism
		else if (this.isImportCall()) {
			String importKey = CodeParser.getKeywordOrDefault("preImport", "import").trim();
			signature = this.getText().getText().substring(importKey.length()+1).trim();
		}
		// END KGU#376 2017-04-11
		return signature;
	}
	// END #178 2016-07-19
	
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
		//return null;
		return relevantParserKeys;
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
			if (myRoot.getSignatureString(false).equals(signature)) {
				typeSpec = myRoot.getResultType();
			}
			else if (Arranger.hasInstance()) {
				Vector<Root> routines = Arranger.getInstance().findRoutinesBySignature(called.getName(), called.paramCount());
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

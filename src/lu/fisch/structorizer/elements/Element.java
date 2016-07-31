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
 *      Description:    Abstract class for all Elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2014.11.11      Operator highlighting modified (sse comment)
 *      Kay Gürtzig     2015.10.09      Methods selectElementByCoord(x,y) and getElementByCoord() merged
 *      Kay Gürtzig     2015.10.11      Comment drawing centralized and breakpoint mechanism prepared
 *      Kay Gürtzig     2015.10.13      Execution state separated from selected state
 *      Kay Gürtzig     2015.11.01      operator unification and intermediate syntax transformation ready
 *      Kay Gürtzig     2015.11.12      Issue #25 (= KGU#80) fixed in unifyOperators, highlighting corrected
 *      Kay Gürtzig     2015.12.01      Bugfixes #39 (= KGU#91) and #41 (= KGU#92)
 *      Kay Gürtzig     2015.12.11      Enhancement #54 (KGU#101): Method splitExpressionList added
 *      Kay Gürtzig     2015.12.21      Bugfix #41/#68/#69 (KGU#93): Method transformIntermediate revised
 *      Kay Gürtzig     2015.12.23      Bugfix #74 (KGU#115): Pascal operators accidently disabled
 *                                      Enh. #75 (KGU#116): Highlighting of jump keywords (orange)
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): New method equals(Element)
 *      Kay Gürtzig     2016.01.03/04   Enh. #87 for collapsing/expanding (KGU#122/KGU#123)
 *      Kay Gürtzig     2016.01.12      Bugfix #105: flaw in string literal tokenization (KGU#139)
 *      Kay Gürtzig     2016.01.12      Bugfix #104: transform caused index errors
 *      Kay Gürtzig     2016.01.14      Enh. #84: Added "{" and "}" to the token separator list (KGU#100)
 *      Kay Gürtzig     2016.01.15      Enh. #61,#107: Highlighting for "as" added (KGU#109)
 *      Kay Gürtzig     2016.01.16      Changes having got lost on a Nov. 2014 merge re-inserted
 *      Kay Gürtzig     2016.01.22      Bugfix for Enh. #38 (addressing moveUp/moveDown, KGU#144).
 *      Kay Gürtzig     2016.03.02      Bugfix #97: steady selection on dragging (see comment, KGU#136),
 *                                      Element self-description improved (method toString(), KGU#152)
 *      Kay Gürtzig     2016.03.06      Enh. #77 (KGU#117): Fields for test coverage tracking added
 *      Kay Gürtzig     2016.03.10      Enh. #124 (KGU#156): Counter fields for histographic tracking added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Runtime data collection accomplished
 *      Kay Gürtzig     2016.03.26      KGU#165: New option D7Parser.ignoreCase introduced
 *      Kay Gürtzig     2016.04.24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016.07.07      Enh. #188: Modification of getText(boolean) to cope with transmutation,
 *                                      Enh. #185: new abstract method convertToCalls() for code import
 *      Kay Gürtzig     2016.07.25      Bugfix #205: Alternative comment bar colour if fill colour equals (KGU#215)
 *      Kay Gürtzig     2016.07.28      Bugfix #210: Execution counting mechanism fundamentally revised
 *      Kay Gürtzig     2016.07.29      Issue #211: Modification in writeOutVariables() for E_TOGGLETC mode.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2016-07-28: Bugfix #210 (KGU#225)
 *      - Before this fix the execution count values were held locally in the Elements. Without recursion,
 *        this wasn't a problem. But for recursive algorithms, particularly for spawning recursion as
 *        in Fibonacci, QuickSort, or binary search trees, all attempts to combine the counts from the
 *        various copies of an algorithm failed in the end. So now the counters are placed in a static
 *        vector on base class Element, the actual element instances hold only indices into this table.
 *        Hence, cloning of elements is no longer a problem, all copies of an element (recursion copies
 *        the diagram!) look via the shared index into the same vector slot and increment it when executed,
 *        at what call level ever. Still, the differences in run data copying between the element classes
 *        must still be put under scrutinous analysis. Not all differences seem plausible.
 *      2016-03-06 / 2016-03-12 Enhancements #77, #124 (KGU#117/KGU#156)
 *      - According to an ER by [elemhsb], first a mechanism optionally to visualise code coverage (for
 *        white-box test completeness) was implemented. A green background colour was proposed and used
 *        to highlight covered Element. It soon became clear that with respect to subroutines a dis-
 *        tinction among loose (shallow) and strict (deep) coverage was necessary, particularly when
 *        recursion comes in. So the coverage tracking could be switched between shallow mode (where
 *        subroutines were automatically regarded as proven to have been covered previously, such the
 *        first CALL to a routine it was automatically marked as covered as well) and deep mode where
 *        a CALL was only marked after the subroutine (regarded as brand-new and never analyzed) had
 *        fully been covered at runtime.
 *      - When this obviously worked, I wanted to get more out of the new mechanism. Instead of
 *        deciding first which coverage tracking to do and having to do another run to see the effect
 *        of the complementary option, always both kinds of analysis were done at once, and the user
 *        could arbitrarily switch between the two possible coverage results.
 *      - And then I had a really great idea: Why not add some more runtime data collection, once data
 *        are collected? And so I added an execution counter for every very element, such that after
 *        a run one might easily see, how often a certain operation was executed. And a kind of
 *        histographic analysis seemed also sensible, i.e. to show how the load is distributed over
 *        the elements (particularly the structured ones) and how many instruction steps were needed
 *        in total to run the algorithm for certain data. This is practically an empirical abstract
 *        time estimation. Both count numbers (execution counter / instruction load) are now written
 *        to the upper right corner of any element, and additionally a scaled colouring from deep
 *        blue to hot red is used to visualize the hot spots and the lonesome places.
 *      2016-02-25 / 2016-03-02 Bugfix #97 (KGU#136)
 *      - Methods prepareDraw() and draw() used the same field rect for temporary calculations but in
 *        a slightly different way: draw() left a bounding rec related to the Root coordinates whereas
 *        prepareDraw() always produced a (0,0)-bound rectangle i. e. with (0,0) as upper left corner.
 *      - getElementByCoord(), however compared the cursor coordinates with rect expecting it to contain
 *        the real drawing coordinates
 *      - getElementByCoord() was not ensured to be called after a draw() invocation but could follow a
 *        prepareDraw() call in which case the coordinate comparison led to wrong results
 *      - So a new field rect0 was introduced for prepareDraw() - in combination with field isRectUpToDate
 *        it even allows to avoid unnecessary re-calculation.
 *      - Field rect was also converted to a (0,0)-bound and hence position-independent bounds rectangle
 *        (in contrast to rect0 representing actual context-sensitive drawing extension (important for
 *        selection).
 *      2015.12.01 (KGU#91/KGU#92)
 *      - Methods setText() were inconsistent and caused nasty effects including data losses (bug #39).
 *      - Operator unification enhanced (issue #41)
 *      2015.11.03 (KGU#18/KGU#23/KGU#63)
 *      - Methods writeOutVariables() and getWidthOutVariables re-merged, lexical splitter extracted from
 *        them.
 *      2015.11.01 (KGU#18/KGU#23)
 *      - Methods unifyOperators(), transformIntermediate() and getIntermediateText() now support different
 *        activities like code generation and execution in a unique way.
 *      2015.10.11/13 (KGU#41 + KGU#43)
 *      - New fields added to distinguish states of selection from those of current execution, this way
 *        inducing more stable colouring and execution path tracking
 *      - a field and several methods introduced to support the setting of breakpoints for execution (it had
 *        always been extremely annoying that for the investigation of some issues near the end of the diagram
 *        either the entire execution had to be started in step more or you had to be utterly quick to pause
 *        in the right moment. Now breakpoints allow to catch the execution wherever necessary.
 *      2015.10.09
 *      - In E_SHOWCOMMENTS mode, substructures had been eclipsed by the top-level elements popping their
 *        comments. This was due to an incomplete subclassing of method getElementByCoord (in contrast
 *        to the nearly identical method selectElementByCoord), both methods were merged by means of a
 *        discriminating additional parameter to identifyElementByCoord(_x, _y, _forSelection)
 *      2014.10.18 / 2014.11.11
 *      - Additions for highlighting of logical operators (both C and Pascal style) in methods
 *        writeOutVariables() and getWidthOutVariables(),
 *      - minor code revision respecting 2- and 3-character operator symbols
 *
 ******************************************************************************************************///


import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import lu.fisch.utils.*;
import lu.fisch.graphics.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.io.*;

import com.stevesoft.pat.*;  //http://www.javaregex.com/

import java.awt.Point;
import java.util.Stack;
import java.util.Vector;

import javax.swing.ImageIcon;

public abstract class Element {
	// Program CONSTANTS
	public static String E_VERSION = "3.25";
	public static String E_THANKS =
	"Developed and maintained by\n"+
	" - Robert Fisch <robert.fisch@education.lu>\n"+
	"\n"+
	"Having also put his fingers into the code\n"+
	" - Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	"\n"+
	"Export classes written and maintained by\n"+
	" - Oberon: Klaus-Peter Reimers <k_p_r@freenet.de>\n"+
	" - Perl: Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - KSH: Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - BASH: Markus Grundner <markus@praised-land.de>\n"+
	" - Java: Gunter Schillebeeckx <gunter.schillebeeckx@tsmmechelen.be>\n"+
	" - C: Gunter Schillebeeckx <gunter.schillebeeckx@tsmmechelen.be>\n"+
	" - C#: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - C++: Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	" - PHP: Rolf Schmidt <rolf.frogs@t-online.de>\n"+
	" - Python: Daniel Spittank <kontakt@daniel.spittank.net>\n"+
	"\n"+
	"License setup and checking done by\n"+
	" - Marcus Radisch <radischm@googlemail.com>\n"+
	" - Stephan <clauwn@freenet.de>\n"+
	"\n"+
	"User manual edited by\n"+
	" - David Morais <narutodc@hotmail.com>\n"+
	" - Praveen Kumar <praveen_sonal@yahoo.com>\n"+
	" - Jan Ollmann <bkgmjo@gmx.net>\n"+
	" - Kay Gürtzig <kay.guertzig@fh-erfurt.de>\n"+
	"\n"+
	"Translations realised by\n"+
	" - NL: Jerone <jeronevw@hotmail.com>\n"+
	" - DE: Klaus-Peter Reimers <k_p_r@freenet.de>\n"+
	" - LU: Laurent Zender <laurent.zender@hotmail.de>\n"+
	" - ES: Andres Cabrera <andrescabrera20@gmail.com>\n"+
	" - PT/BR: Theldo Cruz <cruz@pucminas.br>\n"+
	" - IT: Andrea Maiani <andreamaiani@gmail.com>\n"+
	" - CHS: Wang Lei <wanglei@hollysys.com>\n"+
	" - CHT: Joe Chem <hueyan_chen@yahoo.com.tw>\n"+
	" - CZ: Vladimír Vaščák <vascak@spszl.cz>\n"+
	" - RU: Юра Лебедев <elita.alegator@gmail.com>\n"+
	"\n"+
	"Different good ideas and improvements provided by\n"+
	" - Serge Marelli <serge.marelli@education.lu>\n"+
	" - T1IF1 2006/2007\n"+
	" - Gil Belling <gil.belling@education.lu>\n"+
	" - Guy Loesch <guy.loesch@education.lu>\n"+
	" - Claude Sibenaler <claude.sibenaler@education.lu>\n"+
	" - Tom Van Houdenhove <tom@vanhoudenhove.be>\n"+
	" - Sylvain Piren <sylvain.piren@education.lu>\n"+
	" - Bernhard Wiesner <bernhard.wiesner@informatik.uni-erlangen.de>\n"+
	" - Christian Fandel <christian_fandel@web.de>\n"+
	" - Sascha Meyer <harlequin2@gmx.de>\n"+
	" - Andreas Jenet <ajenet@gmx.de>\n"+
	" - Jan Peter Klippel <structorizer@xtux.org>\n"+
	" - David Tremain <DTremain@omnisource.com>\n"+
	
	"\n"+
	"File dropper class by\n"+
	" - Robert W. Harder <robertharder@mac.com>\n"+
	"\n"+
	"Pascal parser (GOLDParser) engine by\n"+
	" - Matthew Hawkins <hawkini@barclays.net>\n"+
	"\n"+
	"Delphi grammar by\n"+
	" - Rob F.M. van den Brink <R.F.M.vandenBrink@hccnet.nl>\n"+
	"\n"+
	"Regular expression engine by\n"+
	" - Steven R. Brandt, <sbrandt@javaregex.com>\n"+
	"\n"+
	"Vector graphics export by\n"+
	" - FreeHEP Team <http://java.freehep.org/vectorgraphics>\n"+
	"\n"+
	"Command interpreter provided by\n"+
	" - Pat Niemeyer <pat@pat.net>\n"+
	"\n"+
	"Turtle icon designed by\n"+
	" - rainie_billybear@yahoo.com <rainiew@cass.net>\n"+
	"";
	public final static String E_CHANGELOG = "";

	// some static constants
	protected static int E_PADDING = 20;
	public static int E_INDENT = 2;
	public static Color E_DRAWCOLOR = Color.YELLOW;	// Actually, the background colour for selected elements
	public static Color E_COLLAPSEDCOLOR = Color.LIGHT_GRAY;
	// START KGU#41 2015-10-13: Executing status now independent from selection
	public static Color E_RUNNINGCOLOR = Color.ORANGE;		// used for Elements currently (to be) executed 
	// END KGU#41 2015-10-13
	public static Color E_WAITCOLOR = new Color(255,255,210);	// used for Elements with pending execution
	public static Color E_COMMENTCOLOR = Color.LIGHT_GRAY;
	// START KGU#43 2015-10-11: New fix color for breakpoint marking
	public static Color E_BREAKPOINTCOLOR = Color.RED;			// Colour of the breakpoint bar at element top
	// END KGU#43 2015-10-11
	// START KGU#117 2016-03-06: Test coverage colour and mode for Enh. #77
	public static Color E_TESTCOVEREDCOLOR = Color.GREEN;
	public static boolean E_COLLECTRUNTIMEDATA = false;
	public static RuntimeDataPresentMode E_RUNTIMEDATAPRESENTMODE = RuntimeDataPresentMode.NONE;	// FIXME: To be replaced by an enumeration type
	// END KGU#117 2016-03-06
	// START KGU#156 2016-03-10; Enh. #124
	protected static int maxExecCount = 0;			// Maximum number of executions of any element while runEventTracking has been on
	protected static int maxExecStepCount = 0;		// Maximum number of instructions carried out directly per element
	protected static int maxExecTotalCount = 0;		// Maximum combined number of directly and indirectly performed instructions
	// END KGU156 2016-03-10
	// START KGU#225 2016-07-28: Bugfix #210
	protected static Vector<Integer> execCounts = new Vector<Integer>();
	// END KGU#225 2016-07-28

	public static boolean E_VARHIGHLIGHT = false;	// Highlight variables, operators, string literals, and certain keywords? 
	public static boolean E_SHOWCOMMENTS = true;	// Enable comment bars and comment popups? 
	public static boolean E_TOGGLETC = false;		// Swap text and comment on displaying?
	// START KGU#227 2016-07-29: Enh. #128
	public static boolean E_COMMENTSPLUSTEXT = true;
	// END KGU#227 2016-07-29
	public static boolean E_DIN = false;			// Show FOR loops according to DIN 66261?
	public static boolean E_ANALYSER = true;		// Analyser enabled?
	// START KGU#123 2016-01-04: New toggle for Enh. #87
	public static boolean E_WHEELCOLLAPSE = false;	// Is collapsing by mouse wheel rotation enabled?
	// END KGU#123 2016-01-04

	// some colors
	public static Color color0 = Color.decode("0xFFFFFF");
	public static Color color1 = Color.decode("0xFF8080");
	public static Color color2 = Color.decode("0xFFFF80");
	public static Color color3 = Color.decode("0x80FF80");
	public static Color color4 = Color.decode("0x80FFFF");
	public static Color color5 = Color.decode("0x0080FF");
	public static Color color6 = Color.decode("0xFF80C0");
	public static Color color7 = Color.decode("0xC0C0C0");
	public static Color color8 = Color.decode("0xFF8000");
	public static Color color9 = Color.decode("0x8080FF");

	// text "constants"
	public static String preAlt = "(?)";
	public static String preAltT = "T";
	public static String preAltF = "F";
	public static String preCase = "(?)\n?\n?\nelse";
	public static String preFor = "for ? <- ? to ?";
	public static String preWhile = "while (?)";
	public static String preRepeat = "until (?)";
	
	// used font
	protected static Font font = new Font("Helvetica", Font.PLAIN, 12);

	public static final String COLLAPSED =  "...";
	public static boolean altPadRight = true;

	// element attributes
	protected StringList text = new StringList();
	public StringList comment = new StringList();
        
	public boolean rotated = false;

	public Element parent = null;
	public boolean selected = false;
	// START KGU#41 2015-10-13: Execution mark had to be separated from selection
	public boolean executed = false;	// Is set while being executed
	// END KGU#41 2015-10-13
	public boolean waited = false;		// Is set while a substructure Element is under execution
	// START KGU#117 2016-03-06: Enh. #77 - for test coverage mode
	public boolean simplyCovered = false;	// Flag indicates shallow test coverage
	public boolean deeplyCovered = false;	// Flag indicates full test coverage
	// END KGU#117 2016-03-06
	// START KGU#156 2016-03-10; Enh. #124
	//protected int execCount = 0;		// Number of times this was executed while runEventTracking has been on
	protected int execStepCount = 0;	// Number of instructions carried out directly by this element
	protected int execSubCount;			// Number of instructions carried out by substructures of this element
	// END KGU#156 2016-03-11
	// START KGU#225 2016-07-28: Bugfix #210
	protected int execCountIndex = -1;
	// END KGU#225 2016-07-28

	// END KGU156 2016-03-10
	
	private Color color = Color.WHITE;

	private boolean collapsed = false;
	
	// START KGU 2015-10-11: States whether the element serves as breakpoint for execution (stop before!)
	protected boolean breakpoint = false;
	// END KGU 2015-10-11

	// used for drawing
	// START KGU#136 2016-02-25: Bugfix #97 - New separate 0-based Rect for prepareDraw()
	protected Rect rect = new Rect();			// bounds aligned to fit in the context, no longer public
	protected Rect rect0 = new Rect();			// minimum bounds for stand-alone representation
	protected Point topLeft = new Point(0, 0);	// upper left corner coordinate offset wrt drawPoint
	// END KGU#136 2016-03-01
	// START KGU#64 2015-11-03: Is to improve drawing performance
	protected boolean isRectUpToDate = false;		// Will be set and used by prepareDraw() - to be reset on changes
	private static StringList specialSigns = null;	// Strings to be highlighted in the text (lazy initialisation)


	public Element()
	{
	}

	public Element(String _string)
	{
		setText(_string);
	}

	public Element(StringList _strings)
	{
		setText(_strings);
	}

	
	/**
	 * Resets my cached drawing info
	 */
	protected final void resetDrawingInfo()
	{
		this.isRectUpToDate = false;
	}
	/**
	 * Resets my drawing info and that of all of my ancestors
	 */
	public final void resetDrawingInfoUp()
	{
		// If this element is touched then all ancestry information must be invalidated
		Element ancestor = this;
		do {
			ancestor.resetDrawingInfo();
		} while ((ancestor = ancestor.parent) != null);
	}
	/**
	 * Recursively clears all drawing info this subtree down
	 * (To be overridden by structured sub-classes!)
	 */
	public abstract void resetDrawingInfoDown();
	// END KGU#64 2015-11-03

	// abstract things
	/**
	 * Recursively computes the drawing extensions of the element and stores
	 * them in the 0-based rect0 attribute, which is also returned
	 * @param _canvas - the drawing canvas for which the drawing is to be prepared
	 * @return the origin-based extension record.
	 */
	public abstract Rect prepareDraw(Canvas _canvas);

	/**
	 * Actually draws this element within the given canvas, using _top_left
	 * for the placement of the upper left corner. Uses attribute rect0 as
	 * prepared by prepareDraw() to determine the expected extensions and
	 * stores the the actually drawn bounds in attribute rect.
	 * @param _canvas - the drawing canvas where the drawing is to be done in 
	 * @param _top_left - conveyes the upper-left corner for the placement
	 */
	public abstract void draw(Canvas _canvas, Rect _top_left);
	
	public abstract Element copy();
	
	// START KGU#156 2016-03-11: Enh. #124
	/**
	 * Copies the runtime data tha is to be cloned - Usually this comprises the deep
	 * coverage status and the execution counter index. Only for certain kinds of elements
	 * the shallow coverage status is to be copied as well - therefore the argument.
	 * @param _target - target element of the copy operation
	 * @param _simply_too - whether the shallow coverage status is to be copied, too
	 */
	protected void copyRuntimeData(Element _target, boolean _simply_too)
	{
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			if (_simply_too)	// This distinction is important
			{
				_target.simplyCovered = this.simplyCovered;
			}
			_target.deeplyCovered = this.deeplyCovered;
			// START KGU#225 2016-07-28: Bugfix #210
			//_target.execCount = this.execCount;
			this.makeExecutionCount();
			_target.execCountIndex = this.execCountIndex;
			// END KGU#225 2016-07-28
		}
	}
	// END KGU#156 2016-03-11
	
	// START KGU#119 2016-01-02 Bugfix #78
	/**
	 * Returns true iff another is of same class, all persistent attributes are equal, and
	 * all substructure of another recursively equals the substructure of this. 
	 * @param _another - the Element to be compared
	 * @return true on recursive structural equality, false else
	 */
	public boolean equals(Element _another)
	{
		boolean isEqual = this.getClass() == _another.getClass();
		if (isEqual) isEqual = this.getText().getText().equals(_another.getText().getText());
		if (isEqual) isEqual = this.getComment().getText().equals(_another.getComment().getText());
		// START KGU#156 2016-03-12: Colour had to be disabled due to races
		//if (isEqual) isEqual = this.getColor().equals(_another.getColor());
		// END KGU#156 2016-03-12
		return isEqual;
	}
	// END KGU#119 2016-01-02

	// START KGU#117 2016-03-07: Enh. #77
	/**
	 * Disjunctively combines the test coverage status and the execution counts
	 * of _cloneOfMine (which is supposed to a clone of this) with this own
	 * runtime data (coverage status, execution and step counts)
	 * (Important for recursive tests)
	 * @param _cloneOfMine - the Element to be combined (must be equal to this)
	 * @return true on recursive structural equality, false else
	 */
	public boolean combineRuntimeData(Element _cloneOfMine)
	{
		if (this.equals(_cloneOfMine))	// This is rather paranoia
		{
			this.simplyCovered = this.simplyCovered || _cloneOfMine.simplyCovered;
			this.deeplyCovered = this.deeplyCovered || _cloneOfMine.deeplyCovered;
			// START KGU#225 2016-07-28: Bugfix #210 - no longer needed, seemed wrong
			//this.execCount += _cloneOfMine.execCount;
			// END KGU#225 2016-07-28
			//this.execStepCount += _cloneOfMine.execStepCount;
			// In case of (direct or indirect) recursion the substructure steps will
			// gathered on a different way! We must not do it twice
//			if (!this.getClass().getSimpleName().equals("Root"))
//			{
//				this.execSubCount += _cloneOfMine.execSubCount;
//			}
			return true;
		}
		System.err.println("CombineRuntimeData for " + this + " FAILED!");
		return false;
	}
	// END KGU#117 2016-03-07

	// draw point
	Point drawPoint = new Point(0,0);

	public Point getDrawPoint()
	{
		Element ele = this;
		while(ele.parent!=null) ele=ele.parent;
		return ele.drawPoint;
	}

	public void setDrawPoint(Point point)
	{
		Element ele = this;
		while(ele.parent!=null) ele=ele.parent;
		ele.drawPoint=point;
	}

	// START KGU#227 2016-07-30: Enh. #128
	/**
	 * Provides a subclassable left offset for drawing the text
	 */
	protected int getTextDrawingOffset()
	{
		return 0;
	}
	// END KGU#227 2016-07-30

	public void setText(String _text)
	{
		// START KGU#91 2015-12-01: Should never set in swapped mode!
		//getText().setText(_text);
		text.setText(_text);
		// END KGU#91 2015-12-01
	}

	public void setText(StringList _text)
	{
		text = _text;
	}

	// START KGU#91 2015-12-01: We need a way to get the true value
	/**
	 * Returns the content of the text field no matter if mode isSwitchedTextAndComment
	 * is active, use getText(false) for a mode-sensitive effect.
	 * @return the text StringList (in normal mode) the comment StringList otherwise
	 */
	public StringList getText()
	{
		return text;
	}
	/**
	 * Returns the content of the text field unless _alwaysTrueText is false and
	 * mode isSwitchedTextAndComment is active, in which case the comment field
	 * is returned instead 
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
	public StringList getText(boolean _alwaysTrueText)
	// END KGU#91 2015-12-01
	{
        if (!_alwaysTrueText && this.isSwitchTextCommentMode())
        {
        	// START KGU#199 2016-07-07: Enh. #188
        	// Had to be altered since the combination of instructions may produce
        	// multi-line string elements which would compromise drawing
        	//return comment;
        	return StringList.explode(comment, "\n");
        	// END KGU#199 2016-07-07
        }
        else
        {
        	return text;
        }
	}

	public StringList getCollapsedText()
	{
		StringList sl = new StringList();
		// START KGU#91 2015-12-01: Bugfix #39: This is for drawing, so use switch-sensitive methods
		//if(getText().count()>0) sl.add(getText().get(0));
		if (getText(false).count()>0) sl.add(getText(false).get(0));
		// END KGU#91 2015-12-01
		sl.add(COLLAPSED);
		return sl;
	}

	public void setComment(String _comment)
	{
		comment.setText(_comment);
	}

	public void setComment(StringList _comment)
	{
		comment = _comment;
	}

	// START KGU#91 2015-12-01: We need a way to get the true value
	/**
	 * Returns the content of the comment field no matter if mode isSwitchedTextAndComment
	 * is active, use getComment(false) for a mode-sensitive effect.
	 * @return the text StringList (in normal mode) the comment StringList otherwise
	 */
	public StringList getComment()
	{
		return comment;
	}

	/**
	 * Returns the content of the comment field unless _alwaysTrueComment is false and
	 * mode isSwitchedTextAndComment is active, in which case the text field
	 * content is returned instead 
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
	public StringList getComment(boolean _alwaysTrueComment)
	// END KGU#91 2015-12-01
	{
		// START KGU#227 2016-07-30: Enh. #128 - Comments plus text mode ovverrides all
		//if (!_alwaysTrueComment && this.isSwitchTextCommentMode())
		if (!_alwaysTrueComment && !Element.E_COMMENTSPLUSTEXT && this.isSwitchTextCommentMode())
		// END KGU#227 2016-07-30
		{
			return text;
		}
		else
		{
			return comment;
		}
	}
	
	// START KGU#172 2016-04-01: Issue #145: Make it easier to obtain this information
	/**
	 * Checks whether texts and comments are to be swapped for display.
	 * @return true iff a Root is associated and its swichTextAndComments flag is on
	 */
	protected boolean isSwitchTextCommentMode()
	{
		Root root = getRoot(this);
		return (root != null && root.isSwitchTextAndComments());
	}
	// END KGU#172 2916-04-01

	public boolean getSelected()
	{
		return selected;
	}

	public void setSelected(boolean _sel)
	{
		selected=_sel;
	}

	// START KGU#183 2016-04-24: Issue #169 
	/**
	 * Recursively searches the subtree for the currently selected Element or Element
	 * sequence and returns it
	 * @return selected Element (null if none was found)
	 */
	public abstract Element findSelected();
	// END KGU#183 2016-04-24
	
	// START KGU 2016-04-24: replaces Root.checkChild(this, _ancestor)
    /**
     * Checks if this is a descendant of _ancestor in the tree
     * @param _parent - Element to be verified as ancestor of _child
     * @return true iff this is a descendant of _ancestor
     */
    public boolean isDescendantOf(Element _ancestor)
    {
            Element tmp = this.parent;
            boolean res = false;
            while ((tmp != null) && !(res = tmp == _ancestor))
            {
            	tmp = tmp.parent;
            }
            return res;
    }
    // END KGU 2016-04-24
    
    // START KGU#143 2016-01-22: Bugfix #114 - we need a method to decide execution involvement
	/**
	 * Checks execution involvement.
	 * @return true iff this or some substructure of this is currently executed. 
	 */
	public boolean isExecuted()
	{
		return this.executed || this.waited;
	}
	// END KGU#143 2016-01-22
	
	// START KGU#156 2016-03-11: Enh. #124 - We need a consistent execution step counting
	/**
	 * Resets all element execution counts and the derived maximum execution count 
	 */
	public static void resetMaxExecCount()
	{
		Element.maxExecTotalCount = Element.maxExecStepCount = Element.maxExecCount = 0;
		// START KGU#225 2016-07-28: Bugfix #210
		Element.execCounts.clear();
		// END KGU#225 2016-07-28
	}

	// START KGU#225 2016-07-28: Bugfix #210
	/**
	 * Resets the execution count value of this element and all its clones
	 */
	protected void resetExecCount()
	{
		if (this.execCountIndex >= 0)
		{
			if (this.execCountIndex < Element.execCounts.size())
			{
				Element.execCounts.set(this.execCountIndex, 0);
			}
			else
			{
				this.execCountIndex = -1;
			}
		}
	}
	
	/*
	 * Ensures an entry in the static execution count array. If there
	 * hadn't been an entry, then its value will be 0 and its index is
	 * stored in this.execCountEntry
	 */
	protected void makeExecutionCount()
	{
		if (this.execCountIndex < 0 || this.execCountIndex >= Element.execCounts.size())
		{
			this.execCountIndex = Element.execCounts.size();
			Element.execCounts.add(0);
		}
	}
	
	/**
	 * Retrieves the associated execution count and returns it.
	 * @return current execution count for this element (and all its clones)
	 */
	protected int getExecCount()
	{
		int execCount = 0;
		if (this.execCountIndex >= 0)
		{
			if (this.execCountIndex < Element.execCounts.size())
			{
				execCount = Element.execCounts.get(this.execCountIndex);
			}
			else
			{
				System.err.println("**** Illegal execCountIndex " + this.execCountIndex + " on " + this);
			}
		}
		return execCount;
	}
	// END KGU#225 2016-07-28

	/**
	 * Computes the summed up execution steps of this and all its substructure
	 * This method is just for setting the cached value execTotalCount, so don't
	 * call it unless you must (better 
	 * @param _directly TODO
	 * @return
	 */
	public int getExecStepCount(boolean _combined)
	{
		return this.execStepCount + (_combined ? this.execSubCount : 0);
	}

	/**
	 * Increments the execution counter.
	 */
	public final void countExecution()
	{
		// Element execution is always counting 1, no matter whether element is structured or not
		// START KGU#225 2016-07-28: Bugfix #210
		//if (Element.E_COLLECTRUNTIMEDATA && ++this.execCount > Element.maxExecCount)
		//{
		//	Element.maxExecCount = this.execCount;
		//}
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			this.makeExecutionCount();
			int execCount = this.getExecCount() + 1;
			Element.execCounts.set(this.execCountIndex, execCount);
			if (execCount > Element.maxExecCount)
			{
				Element.maxExecCount = execCount;
			}
		}
		// END KGU#225 2016-07-28
	}
	
	/**
	 * Updates the own or substructure instruction counter by adding the growth value
	 * @param _growth - the amount by which the counter is to be increased
	 * @param _directly - whether is to be counted as own instruction or the substructure's
	 */
	public void addToExecTotalCount(int _growth, boolean _directly)
	{
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			if (_directly)
			{
				this.execStepCount += _growth;
				if (this.execStepCount > Element.maxExecStepCount)
				{
					Element.maxExecStepCount = this.execStepCount;
				}
			}
			else
			{
				this.execSubCount += _growth;
				Element.maxExecTotalCount = 
						Math.max(this.getExecStepCount(true),
								Element.maxExecTotalCount);			
			}
		}
	}
	// END KGU#156 2016-03-11
	
	// START KGU#117 2016-03-10: Enh. #77
	/**
	 * In test coverage mode, sets the local tested flag if element is fully covered
	 * and then recursively checks test coverage upwards all ancestry if
	 * _propagateUpwards is true (otherwise it would be postponed to the termination
	 * of the superstructure).
	 * @param _propagateUpwards if true then the change is immediately propagated  
	 */
	public void checkTestCoverage(boolean _propagateUpwards)
	{
		//System.out.print("Checking coverage of " + this + " --> ");
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			boolean hasChanged = false;
			if (!this.simplyCovered && this.isTestCovered(false))
			{
				hasChanged = true;
				this.simplyCovered = true;
			}
			if (!this.deeplyCovered && this.isTestCovered(true))
			{
				hasChanged = true;
				this.deeplyCovered = true;
			}
			if (hasChanged && _propagateUpwards)
			{
				Element parent = this.parent;
				while (parent != null)
				{
					parent.checkTestCoverage(false);
					parent = parent.parent;
				}
			}
		}
		//System.out.println(this.tested ? "SET" : "unset");
	}
	
	/**
	 * Detects shallow or deep test coverage of this element according to the
	 * argument _deeply
	 * @param _deeply if exhaustive coverage (including subroutines is requested)
	 * @return true iff element and all its sub-structure is test-covered
	 */
	public boolean isTestCovered(boolean _deeply)
	{
		return _deeply ? this.deeplyCovered : this.simplyCovered;
	}
	// END KGU#117 2016-03-10

	// START KGU#144 2016-01-22: Bugfix for #38 - Element knows best whether it can be moved up or down
	/**
	 * Checks whether this has a successor within the parenting Subqueue
	 * @return true iff this is element of a Subqueue and has a successor
	 */
	public boolean canMoveDown()
	{
		boolean canMove = false;
		if (parent != null && parent.getClass().getSimpleName().equals("Subqueue"))
		{
			int i = ((Subqueue)parent).getIndexOf(this);
			canMove = (i+1 < ((Subqueue)parent).getSize()) && !this.isExecuted() && !((Subqueue)parent).getElement(i+1).isExecuted();
		}
		return canMove;
	}

	/**
	 * Checks whether this has a predecessor within the parenting Subqueue
	 * @return true iff this is element of a Subqueue and has a predecessor
	 */
	public boolean canMoveUp()
	{
		boolean canMove = false;
		if (parent != null && parent.getClass().getSimpleName().equals("Subqueue"))
		{
			int  i = ((Subqueue)parent).getIndexOf(this);
			canMove = (i > 0) && !this.isExecuted() && !((Subqueue)parent).getElement(i-1).isExecuted();
		}
		return canMove;
	}
	//	END KGU#144 2016-01-22
	
	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	/**
	 * Recursively identifies Instruction elements with call syntax matching one
	 * the given subroutine signatures and converts respective elements to Call
	 * elements.
	 * @param signatures - strings of the form "&lt;routinename&gt;#&lt;arity&gt;"
	 */
	public abstract void convertToCalls(StringList _signatures);
	// END KGU#199 2016-07-07

	public Color getColor()
	{
		return color;
	}

	public String getHexColor()
	{
		String rgb = Integer.toHexString(color.getRGB());
		return rgb.substring(2, rgb.length());
	}

	public static String getHexColor(Color _color)
	{
		String rgb = Integer.toHexString(_color.getRGB());
		return rgb.substring(2, rgb.length());
	}

	public void setColor(Color _color)
	{
		color = _color;
	}
	
	// START KGU#41 2015-10-13: The highlighting rules are getting complex
	// but are more ore less the same for all kinds of elements
	protected Color getFillColor()
	{
		// This priority might be arguable but represents more or less what was found in the draw methods before
		if (this.waited) {
			// FIXME (KGU#117): Need a combined colour for waited + tested
			return Element.E_WAITCOLOR; 
		}
		else if (this.executed) {
			return Element.E_RUNNINGCOLOR;
		}
		else if (this.selected) {
			return Element.E_DRAWCOLOR;
		}
		// START KGU#117/KGU#156 2016-03-06: Enh. #77 + #124 Specific colouring for test coverage tracking
		else if (E_COLLECTRUNTIMEDATA &&
				E_RUNTIMEDATAPRESENTMODE != RuntimeDataPresentMode.NONE) {
			switch (E_RUNTIMEDATAPRESENTMODE) {
			case SHALLOWCOVERAGE:
			case DEEPCOVERAGE:
				if (this.isTestCovered(Element.E_RUNTIMEDATAPRESENTMODE == RuntimeDataPresentMode.DEEPCOVERAGE)) {
					return Element.E_TESTCOVEREDCOLOR;
				}
				break;
			default:
				return getScaleColorForRTDPM();
			}
		}
		// END KGU#117 2016-03-06
		else if (this.collapsed) {
			// NOTE: If the backround colour for collapsed elements should once be discarded, then
			// for Instruction subclasses the icon is to be activated in Instruction.draw() 
			return Element.E_COLLAPSEDCOLOR;
		}
		return getColor();
	}
	// END KGU#41 2015-10-13
	
	// START KGU#156 2016-03-12: Enh. #124 (Runtime data visualisation)
	protected Color getScaleColorForRTDPM()
	{
		int maxValue = 0;
		int value = 0;
		boolean logarithmic = false;
		switch (Element.E_RUNTIMEDATAPRESENTMODE) {
		case EXECCOUNTS:
			maxValue = Element.maxExecCount;
			value = this.getExecCount();
			break;
		case EXECSTEPS_LOG:
			logarithmic = true;
		case EXECSTEPS_LIN:
			maxValue = Element.maxExecStepCount;
			value = this.getExecStepCount(false);
			break;
		case TOTALSTEPS_LOG:
			logarithmic = true;
		case TOTALSTEPS_LIN:
			maxValue = Element.maxExecTotalCount;
			value = this.getExecStepCount(true);
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
	
	/**
	 * Converts the value in the range 0 ... maxValue in the a colour
	 * from deep blue to hot red.
	 * @param value	- a count value in the range 0 (blue) ... maxValue (red)
	 * @param maxValue - the end of the scale 
	 * @return the corresponding spectral colour.
	 */
	private Color getScaleColor(int value, int maxValue)
	{
		// Actually we split the range in four equally sized sections
		// In section 0, blue is at the brightness limit, red sticks to 0,
		// and green is linearly rising from 0 to he brightness limit.
		// In section 1, green is at the brightness limit and blue is linearly
		// falling from the brightness limit down to 0.
		// In section 2, green is at the brightness limit and red in linearly
		// rising from 0 to he brightness limit.
		// In section 1, red is at the brightness limit and green in linearly
		// decreasing from the brightness limit down to 0.
		
		// Lest the background colour should get too dark for the text to remain
		// legible, we shift the scale (e.g. by a twelfth) and this way reduce
		// the effective spectral range such that the latter starts with a less
		// deep blue...
		int rangeOffset = maxValue/12;
		value += rangeOffset;
		maxValue += rangeOffset;
		
		if (maxValue == 0)
		{
			return Color.WHITE;
		}
		int maxBrightness = 255;
		int blue = 0, green = 0, red = 0;
		int value4 = 4 * value * maxBrightness;
		int maxValueB = maxValue * maxBrightness;
		if (value4 < maxValueB)
		{
			blue = maxBrightness;
			green = (int)Math.round(value4 * 1.0 / maxValue);
		}
		else if (value4 < 2 * maxValueB)
		{
			green = maxBrightness;
			blue = Math.max((int)Math.round((maxValueB * 2.0 - value4) / maxValue), 0);
		}
		else if (value4 < 3 * maxValueB)
		{
			green = maxBrightness;
			red = Math.max((int)Math.round((value4 - 2.0 * maxValueB) / maxValue), 0);
		}
		else
		{
			red = maxBrightness;
			green = Math.max((int)Math.round((maxValueB * 3.0 - value4) / maxValue), 0);
		}
		return new Color(red, green, blue);
	}
	// END KGU#156 2016-03-12
	
	// START KGU#43 2015-10-12: Methods to control the new breakpoint property
	public void toggleBreakpoint()
	{
		this.breakpoint = !this.breakpoint;
	}
	
	// Returns whether this Element works as breakpoint on execution
	public boolean isBreakpoint()
	{
		return this.breakpoint;
	}
	
	// 
	/**
	 * Recursively clears all breakpoints in this branch
	 * (To be overridden by structured sub-classes!)
	 */
	public void clearBreakpoints()
	{
		this.breakpoint = false;
	}
	// END KGU#43 2015-10-12

	// START KGU#41 2015-10-13
	/**
	 * Recursively clears all execution flags in this branch
	 * (To be overridden by structured sub-classes!)
	 */
	public void clearExecutionStatus()
	{
		this.executed = false;
		this.waited = false;
		// START KGU#117 2016-03-06: Enh. #77 - extra functionality in test coverage mode
		if (!E_COLLECTRUNTIMEDATA)
		{
			this.deeplyCovered = this.simplyCovered = false;;
			// START KGU#156 2016-03-10: Enh. #124
			// START KGU#225 2016-07-28: Bugfix #210
			//this.execCount = this.execStepCount = this.execSubCount = 0;
			this.execStepCount = this.execSubCount = 0;
			this.execCountIndex = -1;
			// END KGU#225 2016-07-28
			// END KGU#156 2016-03-10
		}
		// KGU#117 2016-03-06
	}
	// END KGU#41 2015-10-13

	// START KGU#117 2016-03-07: Enh. #77
	/** 
	 * Recursively clears test coverage flags and execution counts in this branch
	 * (To be overridden by structured sub-classes!)
	 */
	public void clearRuntimeData()
	{
		this.deeplyCovered = this.simplyCovered = false;;
		// START KGU#156 2016-03-11: Enh. #124
		// START KGU#225 2016-07-28: Bugfix #210
		//this.execCount = this.execStepCount = this.execSubCount = 0;
		this.execStepCount = this.execSubCount = 0;
		if (this.execCountIndex >= Element.execCounts.size())
		{
			this.execCountIndex = -1;
		}
		else if (this.execCountIndex >= 0)
		{
			Element.execCounts.set(this.execCountIndex, 0);
		}
		// END KGU#225 2016-07-28
		// END KGU#156 2016-03-11
	}
	// END KGU#117 2016-03-07

	// START KGU 2015-10-09 Methods selectElementByCoord(int, int) and getElementByCoord(int, int) merged
	/**
	 * Retrieves the smallest (deepest) Element containing coordinate (_x, _y) and flags it as selected
	 * @param _x
	 * @param _y
	 * @return the selected Element (if any)
	 */
	public Element selectElementByCoord(int _x, int _y)
	{
//            Point pt=getDrawPoint();
//
//            if ((rect.left-pt.x<_x)&&(_x<rect.right-pt.x)&&
//                    (rect.top-pt.y<_y)&&(_y<rect.bottom-pt.y))
//            {
//                    return this;
//            }
//            else
//            {
//                    selected=false;
//                    return null;
//            }
		return this.getElementByCoord(_x, _y, true);
	}

	// 
	/**
	 * Retrieves the smallest (deepest) Element containing coordinate (_x, _y)
	 * @param _x
	 * @param _y
	 * @return the (sub-)Element at the given coordinate (if there is none, returns null)
	 */
	public Element getElementByCoord(int _x, int _y)
	{
//            Point pt=getDrawPoint();
//
//            if ((rect.left-pt.x<_x)&&(_x<rect.right-pt.x)&&
//                    (rect.top-pt.y<_y)&&(_y<rect.bottom-pt.y))
//            {
//                    return this;
//            }
//            else
//            {
//                    return null;
//            }
		return this.getElementByCoord(_x, _y, false);
	}

	public Element getElementByCoord(int _x, int _y, boolean _forSelection)
	{
		// START KGU#136 2016-03-01: Bugfix #97 - we will now have origin-bound rects and coords
		//Point pt=getDrawPoint();
		// END KGU#136 2016-03-01

		// START KGU#136 2016-03-01: Bugfix #97: Now all coords will be "local"
//		if ((rect.left-pt.x < _x) && (_x < rect.right-pt.x) &&
//				(rect.top-pt.y < _y) && (_y < rect.bottom-pt.y))
		if ((rect.left <= _x) && (_x <= rect.right) &&
				(rect.top <= _y) && (_y <= rect.bottom))
		// END KGU#136 2016-03-01
		{
			//System.out.println("YES");
			return this;         
		}
		else 
		{
			//System.out.println("NO");
			if (_forSelection)	
			{
				selected = false;	
			}
			return null;    
		}
	}
	// END KGU 2015-10-09
	
	// START KGU 2015-10-11: Helper methods for all Element types' drawing
	
	/**
	 * Draws the marker bar on the left-hand side of the given _rect 
	 * @param _canvas - the canvas to be drawn in
	 * @param _rect - supposed to be the Element's surrounding rectangle
	 */
	protected void drawCommentMark(Canvas _canvas, Rect _rect)
	{
		// START KGU#215 2015-07-25: Bugfix # 205 - If fill colour is the same use an alternative colour
		//_canvas.setBackground(E_COMMENTCOLOR);
		//_canvas.setColor(E_COMMENTCOLOR);
		Color commentColor = E_COMMENTCOLOR;
		if (commentColor.equals(this.getFillColor()))
		{
			commentColor = Color.WHITE;
		}
		_canvas.setBackground(commentColor);
		_canvas.setColor(commentColor);
		// END KGU#215 2015-07-25
		
		Rect markerRect = new Rect(_rect.left + 2, _rect.top + 2,
				_rect.left + 4, _rect.bottom - 2);
		
		if (breakpoint)
		{
			// spare the area of the breakpoint bar
			markerRect.top += 5;
		}
		
		_canvas.fillRect(markerRect);
	}
 
	/**
	 * Draws the marker bar on the top side of the given _rect
	 * @param _canvas - the canvas to be drawn in
	 * @param _rect - the surrounding rectangle of the Element (or relevant part of it)
	 */
	protected void drawBreakpointMark(Canvas _canvas, Rect _rect)
	{
		if (breakpoint) {
			_canvas.setBackground(E_BREAKPOINTCOLOR);
			_canvas.setColor(E_BREAKPOINTCOLOR);

			Rect markerRect = _rect.copy();

			markerRect.left += 2;
			markerRect.top += 2;
			markerRect.right -= 2;
			markerRect.bottom = markerRect.top+4;

			_canvas.fillRect(markerRect);
		}
	}
	// END KGU 2015-10-11

	/**
	 * Returns a copy of the (relocatable i. e. 0-bound) extension rectangle 
	 * @return a rectangle starting at (0,0) and spanning to (width, height) 
	 */
	public Rect getRect()
	{
		return new Rect(rect.left, rect.top, rect.right, rect.bottom);
	}

	// START KGU#136 2016-03-01: Bugfix #97
	/**
	 * Returns the bounding rectangle translated to point relativeTo 
	 * @return a rectangle starting at relativeTo 
	 */
	public Rect getRect(Point relativeTo)
	{
		return new Rect(rect.left + relativeTo.x, rect.top + relativeTo.y,
				rect.right + relativeTo.x, rect.bottom + relativeTo.y);		
	}

	/**
	 * Returns the bounding rectangle translated relative to the drawingPoint 
	 * @return a rectangle starting at relativeTo 
	 */
	public Rect getRectOffDrawPoint()
	{
		return getRect(this.topLeft);		
	}
	// END KGU#136 2016-03-01
	
	public static Font getFont()
	{
		return font;
	}

	public static void setFont(Font _font)
	{
		font=_font;
	}

	/************************
	 * static things
	 ************************/

	public static void loadFromINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// elements
			preAltT=ini.getProperty("IfTrue","V");
			preAltF=ini.getProperty("IfFalse","F");
			preAlt=ini.getProperty("If","()");
			// START KGU 2016-07-31: Bugfix #212 - After corrected effect the default is also turned
			//altPadRight = Boolean.valueOf(ini.getProperty("altPadRight", "true"));
			altPadRight = Boolean.valueOf(ini.getProperty("altPadRight", "false"));
			// END KGU#228 2016-07-31
			StringList sl = new StringList();
			sl.setCommaText(ini.getProperty("Case","\"?\",\"?\",\"?\",\"sinon\""));
			preCase=sl.getText();
			preFor=ini.getProperty("For","pour ? <- ? \u00E0 ?");
			preWhile=ini.getProperty("While","tant que ()");
			preRepeat=ini.getProperty("Repeat","jusqu'\u00E0 ()");
			// font
			setFont(new Font(ini.getProperty("Name","Dialog"), Font.PLAIN,Integer.valueOf(ini.getProperty("Size","12")).intValue()));
			// colors
			color0=Color.decode("0x"+ini.getProperty("color0","FFFFFF"));
			color1=Color.decode("0x"+ini.getProperty("color1","FF8080"));
			color2=Color.decode("0x"+ini.getProperty("color2","FFFF80"));
			color3=Color.decode("0x"+ini.getProperty("color3","80FF80"));
			color4=Color.decode("0x"+ini.getProperty("color4","80FFFF"));
			color5=Color.decode("0x"+ini.getProperty("color5","0080FF"));
			color6=Color.decode("0x"+ini.getProperty("color6","FF80C0"));
			color7=Color.decode("0x"+ini.getProperty("color7","C0C0C0"));
			color8=Color.decode("0x"+ini.getProperty("color8","FF8000"));
			color9=Color.decode("0x"+ini.getProperty("color9","8080FF"));
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// elements
			ini.setProperty("IfTrue",preAltT);
			ini.setProperty("IfFalse",preAltF);
			ini.setProperty("If",preAlt);
			// START KGU 2016-01-16: Stuff having got lost by a Nov. 2014 merge
			ini.setProperty("altPadRight", String.valueOf(altPadRight));
			// END KGU 2016-01-16
			StringList sl = new StringList();
			sl.setText(preCase);
			ini.setProperty("Case",sl.getCommaText());
			ini.setProperty("For",preFor);
			ini.setProperty("While",preWhile);
			ini.setProperty("Repeat",preRepeat);
			// font
			ini.setProperty("Name",getFont().getFamily());
			ini.setProperty("Size",Integer.toString(getFont().getSize()));
			// colors
			ini.setProperty("color0", getHexColor(color0));
			ini.setProperty("color1", getHexColor(color1));
			ini.setProperty("color2", getHexColor(color2));
			ini.setProperty("color3", getHexColor(color3));
			ini.setProperty("color4", getHexColor(color4));
			ini.setProperty("color5", getHexColor(color5));
			ini.setProperty("color6", getHexColor(color6));
			ini.setProperty("color7", getHexColor(color7));
			ini.setProperty("color8", getHexColor(color8));
			ini.setProperty("color9", getHexColor(color9));

			ini.save();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	public static Root getRoot(Element _element)
	{
		while (_element.parent != null)
		{
			_element = _element.parent;
		}
		if (_element instanceof Root)
			return (Root) _element;
		else
			return null;
	}

//	@Deprecated
//	private String cutOut(String _s, String _by)
//	{
//		//System.out.print(_s+" -> ");
//		Regex rep = new Regex("(.*?)"+BString.breakup(_by)+"(.*?)","$1\",\""+_by+"\",\"$2");
//		_s=rep.replaceAll(_s);
//		//System.out.println(_s);
//		return _s;
//	}
	
	// START KGU#18/KGU#23 2015-11-04: Lexical splitter extracted from writeOutVariables
	/**
	 * Splits the given _text into lexical morphemes (lexemes). This will possibly overdo
	 * somewhat (e. g. split float literal 123.45 into "123", ".", "45").
	 * By setting _restoreStrings true, at least string literals can be reassambled again,
	 * consuming more time, of course. 
	 * @param _text - String to be exploded into lexical units
	 * @param _restoreLiterals - if true then accidently split numeric and string literals will be reassembled 
	 * @return StringList consisting ofvthe separated lexemes including isolated spaces etc.
	 */
	public static StringList splitLexically(String _text, boolean _restoreStrings)
	{
		StringList parts = new StringList();
		parts.add(_text);
		
		// split
		parts=StringList.explodeWithDelimiter(parts," ");	// FIXME: Should we omit the delimiters here? 
		parts=StringList.explodeWithDelimiter(parts,"\t");
		parts=StringList.explodeWithDelimiter(parts,"\n");
		parts=StringList.explodeWithDelimiter(parts,".");
		parts=StringList.explodeWithDelimiter(parts,",");
		parts=StringList.explodeWithDelimiter(parts,";");
		parts=StringList.explodeWithDelimiter(parts,"(");
		parts=StringList.explodeWithDelimiter(parts,")");
		parts=StringList.explodeWithDelimiter(parts,"[");
		parts=StringList.explodeWithDelimiter(parts,"]");
		// START KGU#100 2016-01-14: We must also catch the initialiser delimiters
		parts=StringList.explodeWithDelimiter(parts,"{");
		parts=StringList.explodeWithDelimiter(parts,"}");
		// END KGU#100 2016-01-14
		parts=StringList.explodeWithDelimiter(parts,"-");
		parts=StringList.explodeWithDelimiter(parts,"+");
		parts=StringList.explodeWithDelimiter(parts,"/");
		parts=StringList.explodeWithDelimiter(parts,"*");
		parts=StringList.explodeWithDelimiter(parts,">");
		parts=StringList.explodeWithDelimiter(parts,"<");
		parts=StringList.explodeWithDelimiter(parts,"=");
		parts=StringList.explodeWithDelimiter(parts,":");
		parts=StringList.explodeWithDelimiter(parts,"!");
		parts=StringList.explodeWithDelimiter(parts,"'");
		parts=StringList.explodeWithDelimiter(parts,"\"");

		parts=StringList.explodeWithDelimiter(parts,"\\");
		parts=StringList.explodeWithDelimiter(parts,"%");

		// reassamble symbols
		int i = 0;
		while (i < parts.count())
		{
			if (i < parts.count()-1)
			{
				if (parts.get(i).equals("<") && parts.get(i+1).equals("-"))
				{
					parts.set(i,"<-");
					parts.delete(i+1);
					// START KGU 2014-10-18 potential three-character assignment symbol?
					if (i < parts.count()-1 && parts.get(i+1).equals("-"))
					{
						parts.delete(i+1);
					}
					// END KGU 2014-10-18
				}
				else if (parts.get(i).equals(":") && parts.get(i+1).equals("="))
				{
					parts.set(i,":=");
					parts.delete(i+1);
				}
				else if (parts.get(i).equals("!") && parts.get(i+1).equals("="))
				{
					parts.set(i,"!=");
					parts.delete(i+1);
				}
				// START KGU 2015-11-04
				else if (parts.get(i).equals("=") && parts.get(i+1).equals("="))
				{
					parts.set(i,"==");
					parts.delete(i+1);
				}
				// END KGU 2015-11-04
				else if (parts.get(i).equals("<"))
				{
					if (parts.get(i+1).equals(">"))
					{
						parts.set(i,"<>");
						parts.delete(i+1);
					}
					else if (parts.get(i+1).equals("="))
					{
						parts.set(i,"<=");
						parts.delete(i+1);
					}
					// START KGU#92 2015-12-01: Bugfix #41
					else if (parts.get(i+1).equals("<"))
					{
						parts.set(i,"<<");
						parts.delete(i+1);
					}					
					// END KGU#92 2015-12-01
				}
				else if (parts.get(i).equals(">"))
				{
					if (parts.get(i+1).equals("="))
					{
						parts.set(i,">=");
						parts.delete(i+1);
					}
					// START KGU#92 2015-12-01: Bugfix #41
					else if (parts.get(i+1).equals(">"))
					{
						parts.set(i,">>");
						parts.delete(i+1);
					}					
					// END KGU#92 2015-12-01
				}
				// START KGU#24 2014-10-18: Logical two-character operators should be detected, too ...
				else if (parts.get(i).equals("&") && parts.get(i+1).equals("&"))
				{
					parts.set(i,"&&");
					parts.delete(i+1);
				}
				else if (parts.get(i).equals("|") && parts.get(i+1).equals("|"))
				{
					parts.set(i,"||");
					parts.delete(i+1);
				}
				// END KGU#24 2014-10-18
				// START KGU#26 2015-11-04: Find escaped quotes
				else if (parts.get(i).equals("\\"))
				{
					if (parts.get(i+1).equals("\""))
					{
						parts.set(i, "\\\"");
						parts.delete(i+1);					}
					else if (parts.get(i+1).equals("\\"))
					{
						parts.set(i, "\\\\");
						parts.delete(i+1);					}
				}
				// END KGU#26 2015-11-04
			}
			i++;
		}
		
		if (_restoreStrings)
		{
			String[] delimiters = {"\"", "'"};
			// START KGU#139 2016-01-12: Bugfix #105 - apparently incomplete strings got lost
			// We mustn't eat seemingly incomplete strings, instead we re-feed them
			StringList parkedTokens = new StringList();
			// END KGU#139 2016-01-12
			for (int d = 0; d < delimiters.length; d++)
			{
				boolean withinString = false;
				String composed = "";
				i = 0;
				while (i < parts.count())
				{
					String lexeme = parts.get(i);
					if (withinString)
					{
						composed = composed + lexeme;
						if (lexeme.equals(delimiters[d]))
						{
							// START KGU#139 2016-01-12: Bugfix #105
							parkedTokens.clear();
							// END KGU#139 2016-01-12
							parts.set(i, composed+"");
							composed = "";
							withinString = false;
							i++;
						}
						else
						{
							// START KGU#139 2016-01-12: Bugfix #105
							parkedTokens.add(lexeme);
							// END KGU#139 2016-01-12
							parts.delete(i);
						}
					}
					else if (lexeme.equals(delimiters[d]))
					{
						// START KGU#139 2016-01-12: Bugfix #105
						parkedTokens.add(lexeme);
						// END KGU#139 2016-01-12
						withinString = true;
						composed = lexeme+"";
						parts.delete(i);
					}
					else
					{
						i++;
					}
				}
			}
			// START KGU#139 2916-01-12: Bugfix #105
			if (parkedTokens.count() > 0)
			{
				parts.add(parkedTokens);
			}
			// END KGU#139 2016-01-12
		}
		return parts;
	}
	// END KGU#18/KGU#23
	
	// START KGU#101 2015-12-11: Enhancement #54: We need to split expression lists (might go to a helper class)
	/**
	 * Splits the _text supposed to represent a list of expressions separated by _listSeparator
	 * into strings representing one of the listed expressions each.
	 * This does not mean mere string splitting but is aware of string literals, argument lists
	 * of function calls etc. These must not be broken.
	 * The analysis stops as soon as there is a level underflow (i.e. an unmatched closing parenthesis,
	 * bracket, or the like).
	 * The remaining string from the unsatisfied closing parenthesis, bracket, or brace on will
	 * be ignored!
	 * @param _text - string containing one or more expressions
	 * @param _listSeparator - a character sequence serving as separator among the expressions (default: ",") 
	 * @return a StringList, each element of which contains one of the separated expressions (order preserved)
	 */
	public static StringList splitExpressionList(String _text, String _listSeparator)
	// START KGU#93 2015-12-21 Bugfix #41/#68/#69
	{
		return splitExpressionList(_text, _listSeparator, false);
	}
	
	/**
	 * Splits the _text supposed to represent a list of expressions separated by _listSeparator
	 * into strings representing one of the listed expressions each.
	 * This does not mean mere string splitting but is aware of string literals, argument lists
	 * of function calls etc. These must not be broken.
	 * The analysis stops as soon as there is a level underflow (i.e. an unmatched closing parenthesis,
	 * bracket, or the like).
	 * The remaining string from the unsatisfied closing parenthesis, bracket, or brace on will
	 * be added as last element to the result if _appendRemainder is true - otherwise there is no
	 * difference to method splitExpressionList(String _text, String _listSeparator)!
	 * If the last result element is empty then the expression list was syntactically "clean".
	 * @param _text - string containing one or more expressions
	 * @param _listSeparator - a character sequence serving as separator among the expressions (default: ",") 
	 * @param _appendTail - if the remaining part of _text from the first unaccepted character on is to be added 
	 * @return a StringList consisting of the separated expressions (and the tail if _appendTail was true).
	 */
	public static StringList splitExpressionList(String _text, String _listSeparator, boolean _appendTail)
	// END KU#93 2015-12-21
	{
		StringList expressionList = new StringList();
		if (_listSeparator == null) _listSeparator = ",";
		StringList tokens = Element.splitLexically(_text, true);
		
		int parenthDepth = 0;
		boolean isWellFormed = true;
		Stack<String> enclosings = new Stack<String>();
		int tokenCount = tokens.count();
		String currExpr = "";
		for (int i = 0; isWellFormed && parenthDepth >= 0 && i < tokenCount; i++)
		{
			String token = tokens.get(i);
			if (token.equals(_listSeparator) && enclosings.isEmpty())
			{
				// store the current expression and start a new one
				expressionList.add(currExpr.trim());
				currExpr = new String();
			}
			else
			{ 
				if (token.equals("("))
				{
					enclosings.push(")");
					parenthDepth++;
				}
				else if (token.equals("["))
				{
					enclosings.push("]");
					parenthDepth++;
				}
				else if (token.equals("{"))
				{
					enclosings.push("}");
					parenthDepth++;
				}
				else if ((token.equals(")") || token.equals("]") || token.equals("}")))
				{
					isWellFormed = parenthDepth > 0 && token.equals(enclosings.pop());
					parenthDepth--;
				}
				if (isWellFormed)
				{
					currExpr += token;
				}
				else if (_appendTail)
				{
					expressionList.add(currExpr.trim());
					currExpr = tokens.concatenate("", i);
				}
			}
		}
		// add the last expression if it's not empty
		if (!currExpr.trim().isEmpty() || _appendTail)
		{
			expressionList.add(currExpr.trim());
		}
		return expressionList;
	}
	// END KGU#101 2015-12-11

	// START KGU#63 2015-11-03: getWidthOutVariables and writeOutVariables were nearly identical (and had to be!)
	// Now it's two wrappers and a common algorithm -> ought to avoid duplicate work and prevents from divergence
	public static int getWidthOutVariables(Canvas _canvas, String _text, Element _this)
	{
		return writeOutVariables(_canvas, 0, 0, _text, _this, false);
	}

	public static void writeOutVariables(Canvas _canvas, int _x, int _y, String _text, Element _this)
	{
		writeOutVariables(_canvas, _x, _y, _text, _this, true);
	}
	
	private static int writeOutVariables(Canvas _canvas, int _x, int _y, String _text, Element _this, boolean _actuallyDraw)
	// END KGU#63 2015-11-03
	{
		// init total
		int total = 0;

		Root root = getRoot(_this);

		if (root != null)
		{
			// START KGU#226 2016-07-29: Issue #211: No syntax highlighting in comments
			//if (root.hightlightVars==true)
			if (root.hightlightVars==true && !root.isSwitchTextCommentMode())
			// END KGU#226 2016-07-29
			{
				StringList parts = Element.splitLexically(_text, true);

				// bold font
				Font boldFont = new Font(Element.font.getName(),Font.BOLD,Element.font.getSize());
				// backup the original font
				Font backupFont = _canvas.getFont();

				// START KGU#64 2015-11-03: Not to be done again and again. Private static field now!
				//StringList specialSigns = new StringList();
				if (specialSigns == null)	// lazy initialisation
				{
					specialSigns = new StringList();
					// ENDU KGU#64 2015-11-03
					specialSigns.add(".");
					specialSigns.add("[");
					specialSigns.add("]");
					specialSigns.add("\u2190");
					specialSigns.add(":=");

					specialSigns.add("+");
					specialSigns.add("/");
					// START KGU 2015-11-03: This operator had been missing
					specialSigns.add("%");
					// END KGU 2015-11-03
					specialSigns.add("*");
					specialSigns.add("-");
					specialSigns.add("var");
					specialSigns.add("mod");
					specialSigns.add("div");
					specialSigns.add("<=");
					specialSigns.add(">=");
					specialSigns.add("<>");
					specialSigns.add("<<");
					specialSigns.add(">>");
					specialSigns.add("<");
					specialSigns.add(">");
					specialSigns.add("==");
					specialSigns.add("!=");
					specialSigns.add("=");
					specialSigns.add("!");
					// START KGU#24 2014-10-18
					specialSigns.add("&&");
					specialSigns.add("||");
					specialSigns.add("and");
					specialSigns.add("or");
					specialSigns.add("xor");
					specialSigns.add("not");
					// END KGU#24 2014-10-18
					// START KGU#115 2015-12-23: Issue #74 - These Pascal operators hadn't been supported
					specialSigns.add("shl");
					specialSigns.add("shr");
					// END KGU#115 2015-12-23
					// START KGU#109 2016-01-15: Issues #61, #107 highlight the BASIC declarator keyword, too
					specialSigns.add("as");
					// END KGU#109 2016-01-15
					
					// START KGU#100 2016-01-16: Enh. #84: Also highlight the initialiser delimiters
					specialSigns.add("{");
					specialSigns.add("}");
					// END KGU#100 2016-01-16

					// The quotes will only occur as tokens if they are unpaired!
					specialSigns.add("'");
					specialSigns.add("\"");
				// START KGU#64 2015-11-03: See above
				}
				// END KGU#64 2015-11-03

				// These markers might have changed by configuration, so don't cache them
				StringList ioSigns = new StringList();
				ioSigns.add(D7Parser.input.trim());
				ioSigns.add(D7Parser.output.trim());
				// START KGU#116 2015-12-23: Enh. #75 - highlight jump keywords
				StringList jumpSigns = new StringList();
				jumpSigns.add(D7Parser.preLeave.trim());
				jumpSigns.add(D7Parser.preReturn.trim());
				jumpSigns.add(D7Parser.preExit.trim());
				// END KGU#116 2015-12-23
				
				for(int i=0; i < parts.count(); i++)
				{
					String display = parts.get(i);

					display = BString.replace(display, "<-","\u2190");

					if(!display.equals(""))
					{
						// if this part has to be colored
						if(root.variables.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x000099"));
							// set font
							_canvas.setFont(boldFont);
						}
						// if this part has to be colored with special color
						else if(specialSigns.contains(display))
						{
							// set color
							_canvas.setColor(Color.decode("0x990000"));
							// set font
							_canvas.setFont(boldFont);
						}
						// if this part has to be colored with io color
						// START KGU#165 2016-03-25: consider the new option
						//else if(ioSigns.contains(display))
						else if(ioSigns.contains(display, !D7Parser.ignoreCase))
							// END KGU#165 2016-03-25
						{
							// set color
							_canvas.setColor(Color.decode("0x007700"));
							// set font
							_canvas.setFont(boldFont);
						}
						// START KGU 2015-11-12
						// START KGU#116 2015-12-23: Enh. #75
						// START KGU#165 2016-03-25: cosider the new option
						//else if(jumpSigns.contains(display))
						else if(jumpSigns.contains(display, !D7Parser.ignoreCase))
							// END KGU#165 2016-03-25
						{
							// set color
							_canvas.setColor(Color.decode("0xff5511"));
							// set font
							_canvas.setFont(boldFont);
						}
						// END KGU#116 2015-12-23
						// if it's a String or Character literal then mark it as such
						else if (display.startsWith("\"") && display.endsWith("\"") ||
								display.startsWith("'") && display.endsWith("'"))
						{
							// set colour
							_canvas.setColor(Color.decode("0x770077"));
						}
						// END KGU 2015-11-12
					}

					if (_actuallyDraw)
					{
						// write out text
						_canvas.writeOut(_x + total, _y, display);
					}

					// add to the total
					total += _canvas.stringWidth(display);

					// reset color
					_canvas.setColor(Color.BLACK);
					// reset font
					_canvas.setFont(backupFont);

				}
				//System.out.println(parts.getCommaText());
			}
			else
			{
				if (_actuallyDraw)
				{
					_canvas.writeOut(_x + total, _y, _text);
				}

                // add to the total
                total += _canvas.stringWidth(_text);

			}
		}
		
		return total;
	}
	
	// START KGU#227 2016-07-29: Enh. #128
	/**
	 * Writes the non-empty comment lines at position _x, _y to _canvas with 2/3 font height and in dark gray
	 * @param _canvas - the drawing canvas
	 * @param _x - left text anchor coordinate for the text area
	 * @param _y - top text anchor coordinate for the text area
	 * @param _actuallyDraw - if the text is actually to be written (otherwise we just return the bonds)
	 * @return - bounding box of the text
	 */
	protected Rect writeOutCommentLines(Canvas _canvas, int _x, int _y, boolean _actuallyDraw)
	{
		return writeOutCommentLines(_canvas, _x, _y, _actuallyDraw, true);
	}

	protected Rect writeOutCommentLines(Canvas _canvas, int _x, int _y, boolean _actuallyDraw, boolean _allLines)
	{
		int height = 0;
		int width = 0;
		// smaller font
		Font smallFont = new Font(Element.font.getName(), Font.PLAIN, Element.font.getSize() * 2 / 3);
		FontMetrics fm = _canvas.getFontMetrics(smallFont);
		int fontHeight = fm.getHeight();
		int extraHeight = this.isBreakpoint() ? fontHeight/2 : 0;
		// backup the original font
		Font backupFont = _canvas.getFont();
		_canvas.setFont(smallFont);
		_canvas.setColor(Color.DARK_GRAY);
		int nLines = this.getComment().count();
		String appendix = "";
		if (nLines > 1 && !_allLines)
		{
			nLines = 1;
			appendix = "...";
		}
		for (int i = 0; i < nLines; i++)
		{
			String line = this.getComment().get(i).trim();
			if (!line.isEmpty())
			{
				height += fontHeight;
				width = Math.max(width, _canvas.stringWidth(line + appendix));
				if (_actuallyDraw)
				{
					_canvas.writeOut(_x, _y + height + extraHeight, line + appendix);
				}
			}
		}
		
		_canvas.setFont(backupFont);
		_canvas.setColor(Color.BLACK);
		if (height > 0)
		{
			height += fontHeight/2;
		}
		return new Rect(_x, _y, _x+width, _y+height);
	}
	
	protected boolean haveOuterRectDrawn()
	{
		return true;
	}
	// END KGU#227 2016-07-29

	// START KGU#156 2016-03-11: Enh. #124 - helper routines to display run-time info
	/**
	 * Writes the selected runtime information in half-size font to the lower
	 * left of position (_right, _top).
	 * @param _canvas - the Canvas to write to
	 * @param _right - right border x coordinate
	 * @param _top - upper border y coordinate
	 */
	protected void writeOutRuntimeInfo(Canvas _canvas, int _right, int _top)
	{
		if (Element.E_COLLECTRUNTIMEDATA)
		{
			// smaller font
			Font smallFont = new Font(Element.font.getName(), Font.PLAIN, Element.font.getSize()*2/3);
			FontMetrics fm = _canvas.getFontMetrics(smallFont);
			// backup the original font
			Font backupFont = _canvas.getFont();
			String info = this.getRuntimeInfoString();
			int yOffs = this.isBreakpoint() ? 4 : 0; 
			_canvas.setFont(smallFont);
			_canvas.setColor(Color.BLACK);
			int width = _canvas.stringWidth(info);
			_canvas.writeOut(_right - width, _top + yOffs + fm.getHeight() , info);
			_canvas.setFont(backupFont);
		}
	}
	
	/**
	 * Returns a runtime counter string, composed from execution count
	 * and a mode-dependent number of steps (pure or aggregated, with or
	 * without parenthesis). 
	 * @return the decoration string for runtime data visualisation
	 */
	protected String getRuntimeInfoString()
	{
		return this.getExecCount() + " / " + this.getExecStepCount(this.isCollapsed());
	}
	// END KGU#156 2016-03-11
	


    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    	// START KGU#136 2016-03-01: Bugfix #97
    	this.resetDrawingInfoUp();
    	// END KGU#136 2016-03-01
    }
    
    // START KGU#122 2016-01-03: Collapsed elements may be marked with an element-specific icon
    protected ImageIcon getIcon()
    {
    	return IconLoader.ico057;
    }
    // END KGU#122 2016-01-03

    // START KGU 2015-10-16: Some Root stuff properly delegated to the Element subclasses
    // (The obvious disadvantage is slightly reduced performance, of course)
    /**
     * Returns the serialised texts held within this element and its substructure.
     * The argument _instructionsOnly controls whether mere expressions like logical conditions or
     * even call statements are included. As a rule, no lines that may not potentially introduce new
     * variables are added if true (which not only reduces time and space requirements but also avoids
     * "false positives" in variable detection). 
     * Uses addFullText() - so possibly better override that method if necessary.
     * @param _instructionsOnly - if true then only the texts of Instruction elements are included
     * @return the composed StringList
     */
    public StringList getFullText(boolean _instructionsOnly)
    {
    	// The default...
    	StringList sl = new StringList();
    	this.addFullText(sl, _instructionsOnly);
    	return sl;
    }
    
    /**
     * Appends all the texts held within this element and its substructure to the given StringList.
     * The argument _instructionsOnly controls whether mere expressions like logical conditions or
     * even call statements are included. As a rule, no lines that may not potentially introduce new
     * variables are added if true (which not only reduces time and space requirements but also avoids
     * "false positives" in variable detection). 
     * (To be overridden by structured subclasses)
     * @param _lines - the StringList to append to 
     * @param _instructionsOnly - if true then texts not possibly containing variable declarations are omitted
     */
    protected abstract void addFullText(StringList _lines, boolean _instructionsOnly);
    // END KGU 2015-10-16
    
    // START KGU#18/KGU#23 2015-10-24 intermediate transformation added and decomposed
    /**
     * Converts the operator symbols accepted by Structorizer into padded Java operators
     * (note the surrounding spaces - no double spaces will exist):
     * - Assignment:		" <- "
     * - Comparison:		" == ", " < ", " > ", " <= ", " >= ", " != "
     * - Logic:				" && ", " || ", " ! ", " ^ "
     * - Arithmetics:		" div " and usual Java operators with or without padding
     * @param _expression an Element's text in practically unknown syntax
     * @return an equivalent of the _expression String with replaced operators
     */
    public static String unifyOperators(String _expression)
    {
    	// START KGU#93 2015-12-21: Bugfix #41/#68/#69 Avoid operator padding
    	//return unifyOperators(_expression, false);
    	StringList tokens = Element.splitLexically(_expression, true);
    	unifyOperators(tokens, false);
    	return tokens.concatenate();
    	// END KGU#93 2015-12-21
    }
    
//    // START KGU#18/KGU#23 2015-10-24 intermediate transformation added and decomposed
//    /**
//     * Converts the operator symbols accepted by Structorizer into padded Java operators
//     * (note the surrounding spaces - no double spaces will exist):
//     * - Assignment:		" <- "
//     * - Comparison*:		" == ", " < ", " > ", " <= ", " >= ", " != "
//     * - Logic*:			" && ", " || ", " ! ", " ^ "
//     * - Arithmetics*:		" div " and usual Java operators without padding (e. g. " mod " -> " % ")
//     * @param _expression an Element's text in practically unknown syntax
//     * @param _assignmentOnly if true then only assignment operator will be unified
//     * @return an equivalent of the _expression String with replaced operators
//     */
//    @Deprecated
//    public static String unifyOperators(String _expression, boolean _assignmentOnly)
//    {
//    	
//        String interm = _expression.trim();	// KGU#54
//        // variable assignment
//        interm = interm.replace("<--", " §ASGN§ ");
//        interm = interm.replace("<-", " §ASGN§ ");
//        interm = interm.replace(":=", " §ASGN§ ");
//        
//        if (!_assignmentOnly)
//        {
//        	// testing
//        	interm = interm.replace("!=", " §UNEQ§ ");
//        	interm = interm.replace("==", " §EQU§ ");
//        	interm = interm.replace("<=", " §LE§ ");
//        	interm = interm.replace(">=", " §GE§ ");
//        	interm = interm.replace("<>", " §UNEQ§ ");
//        	// START KGU#92 2015-12-01: Bugfix #41
//        	interm = interm.replace("<<", " §SHL§ ");
//        	interm = interm.replace(">>", " §SHR§ ");
//        	// END KGU#92 2015-12-01
//        	interm = interm.replace("<", " < ");
//        	interm = interm.replace(">", " > ");
//        	interm = interm.replace("=", " §EQU§ ");
//
//        	// Parenthesis/bracket padding as preparation for the following replacements
//        	interm = interm.replace(")", " ) ");
//        	interm = interm.replace("(", "( ");
//        	interm = interm.replace("]", "] ");	// Do NOT pad '[' (would spoil the array detection)
//        	// arithmetics and signs
//        	interm = interm.replace("+", " +");	// Fortunately, ++ isn't accepted as working operator by the Structorizer
//        	interm = interm.replace("-", " -");	// Fortunately, -- isn't accepted as working operator by the Structorizer
//        	//interm = interm.replace(" div "," / ");	// We must still distinguish integer division
//        	interm = interm.replace(" mod ", " % ");
//        	interm = interm.replace(" MOD ", " % ");
//        	interm = interm.replace(" mod(", " % (");
//        	interm = interm.replace(" MOD(", " % (");
//        	interm = interm.replace(" div(", " div (");
//        	interm = interm.replace(" DIV ", " div ");
//        	interm = interm.replace(" DIV(", " div (");
//        	// START KGU#92 2015-12-01: Bugfix #41
//        	interm = interm.replace(" shl ", " §SHL§ ");
//        	interm = interm.replace(" shr ", " §SHR§ ");
//        	interm = interm.replace(" SHL ", " §SHL§ ");
//        	interm = interm.replace(" SHR ", " §SHR§ ");
//        	// END KGU#92 2015-12-01
//        	// Logic
//        	interm = interm.replace( "&&", " && ");
//        	interm = interm.replace( "||", " || ");
//        	interm = interm.replace( " and ", " && ");
//        	interm = interm.replace( " AND ", " && ");
//        	interm = interm.replace( " and(", " && (");
//        	interm = interm.replace( " AND(", " && (");
//        	interm = interm.replace( " or ", " || ");
//        	interm = interm.replace( " OR ", " || ");
//        	interm = interm.replace( " or(", " || (");
//        	interm = interm.replace( " OR(", " || (");
//        	interm = interm.replace( " not ", " §NOT§ ");
//        	interm = interm.replace( " NOT ", " §NOT§ ");
//        	interm = interm.replace( " not(", " §NOT§ (");
//        	interm = interm.replace( " NOT(", " §NOT§ (");
//        	String lower = interm.toLowerCase();
//        	if (lower.startsWith("not ") || lower.startsWith("not(")) {
//        		interm = " §NOT§ " + interm.substring(3);
//        	}
//        	interm = interm.replace( "!", " §NOT§ ");
//        	interm = interm.replace( " xor ", " ^ ");	// Might cause some operator preference trouble
//        	interm = interm.replace( " XOR ", " ^ ");	// Might cause some operator preference trouble
//        }
//
//        String unified = interm.replace(" §ASGN§ ", " <- ");
//        if (!_assignmentOnly)
//        {
//        	unified = unified.replace(" §EQU§ ", " == ");
//        	unified = unified.replace(" §UNEQ§ ", " != ");
//        	unified = unified.replace(" §LE§ ", " <= ");
//        	unified = unified.replace(" §GE§ ", " >= ");
//        	unified = unified.replace(" §NOT§ ", " ! ");
//        	// START KGU#92 2015-12-01: Bugfix #41
//        	unified = unified.replace(" §SHL§ ", " << ");
//        	unified = unified.replace(" §SHR§ ", " >> ");
//        	// END KGU#92 2015-12-01
//        }
//        unified = BString.replace(unified, "  ", " ");	// shrink multiple blanks
//        unified = BString.replace(unified, "  ", " ");	// do it again to catch odd-numbered blanks as well
//        
//        return unified;
//    }

	// START KGU#92 2015-12-01: Bugfix #41 Okay now, here is the new approach (still a sketch)
    /**
     * Converts the operator symbols accepted by Structorizer into intermediate operators
     * (mostly Java operators), mostly padded:
     * - Assignment:		" <- "
     * - Comparison*:		" == ", " < ", " > ", " <= ", " >= ", " != "
     * - Logic*:			" && ", " || ", " ! ", " ^ "
     * - Arithmetics*:		" div " and usual Java operators (e. g. " mod " -> " % ")
     * @param _tokens a tokenised line of an Element's text (in practically unknown syntax)
     * @param _assignmentOnly if true then only assignment operator will be unified
     * @return total number of deletions / replacements
     */
    public static int unifyOperators(StringList _tokens, boolean _assignmentOnly)
    {
    	int count = 0;
        count += _tokens.replaceAll(":=", "<-");
        // START KGU#115 2015-12-23: Bugfix #74 - logical inversion
        //if (_assignmentOnly)
        if (!_assignmentOnly)
        // END KGU#115 2015-12-23
        {
        	//count += _tokens.replaceAll("=", " == ");
        	count += _tokens.replaceAll("=", "==");
        	//count += _tokens.replaceAll("<", " < ");
        	//count += _tokens.replaceAll(">", " > ");
        	//count += _tokens.replaceAll("<=", " <= ");
        	//count += _tokens.replaceAll(">=", " >= ");
        	//count += _tokens.replaceAll("<>", " != ");
        	count += _tokens.replaceAll("<>", "!=");
        	//count += _tokens.replaceAll("%", " % ");
        	//count += _tokens.replaceAllCi("mod", " % ");
        	count += _tokens.replaceAllCi("mod", "%");
        	//count += _tokens.replaceAllCi("div", " div ");
        	count += _tokens.replaceAllCi("shl", "<<");
        	count += _tokens.replaceAllCi("shr", ">>");
        	count += _tokens.replaceAllCi("and", "&&");
        	count += _tokens.replaceAllCi("or", "||");
        	count += _tokens.replaceAllCi("not", "!");
        	count += _tokens.replaceAllCi("xor", "^");
        }
    	return count;
    }
	// END KGU#92 2015-12-01

    /**
     * Returns a (hopefully) lossless representation of the stored text as a
     * StringList in a common intermediate language (code generation phase 1).
     * This allows the language-specific Generator subclasses to concentrate on the translation
     * into their respective target languages (code generation phase 2).
     * Conventions of the intermediate language:
     * Operators (note the surrounding spaces - no double spaces will exist):
     * - Assignment:		" <- "
     * - Comparison:		" = ", " < ", " > ", " <= ", " >= ", " <> "
     * - Logic:				" && ", " || ", " §NOT§ ", " ^ "
     * - Arithmetics:		usual Java operators without padding
     * - Control key words:
     * -	If, Case:		none (wiped off)
     * -	While, Repeat:	none (wiped off)
     * -	For:			unchanged
     * -	Forever:		none (wiped off)
     * 
     * @return a padded intermediate language equivalent of the stored text
     */
    
    public StringList getIntermediateText()
    {
    	StringList interSl = new StringList();
    	for (int i = 0; i < text.count(); i++)
    	{
    		interSl.add(transformIntermediate(text.get(i)));
    	}
    	return interSl;
    }
    
    /**
     * Creates a (hopefully) lossless representation of the _text String as a
     * tokens list of a common intermediate language (code generation phase 1).
     * This allows the language-specific Generator subclasses to concentrate
     * on the translation into their target language (code generation phase 2).
     * Conventions of the intermediate language:
     * Operators (note the surrounding spaces - no double spaces will exist):
     * - Assignment:		"<-"
     * - Comparison:		"=", "<", ">", "<=", ">=", "<>"
     * - Logic:				"&&", "||", "!", "^"
     * - Arithmetics:		usual Java operators
     * - Control key words:
     * -	If, Case:		none (wiped off)
     * -	While, Repeat:	none (wiped off)
     * -	For:			unchanged
     * -	Forever:		none (wiped off)
     * 
     * @param _text - a line of the Structorizer element
     * //@return a padded intermediate language equivalent of the stored text
     * @return a StringList consisting of tokens translated into a unified intermediate language
     */
    // START KGU#93 2015-12-21: Bugfix #41/#68/#69
    //public static String transformIntermediate(String _text)
    public static StringList transformIntermediate(String _text)
    {
    	//final String regexMatchers = ".?*+[](){}\\^$";
    	
//    	// Collect redundant placemarkers to be deleted from the text
//        StringList redundantMarkers = new StringList();
//        redundantMarkers.addByLength(D7Parser.preAlt);
//        redundantMarkers.addByLength(D7Parser.preCase);
//        //redundantMarkers.addByLength(D7Parser.preFor);	// will be handled separately
//        redundantMarkers.addByLength(D7Parser.preWhile);
//        redundantMarkers.addByLength(D7Parser.preRepeat);
//
//        redundantMarkers.addByLength(D7Parser.postAlt);
//        redundantMarkers.addByLength(D7Parser.postCase);
//        //redundantMarkers.addByLength(D7Parser.postFor);	// will be handled separately
//        //redundantMarkers.addByLength(D7Parser.stepFor);	// will be handled separately
//        redundantMarkers.addByLength(D7Parser.postWhile);
//        redundantMarkers.addByLength(D7Parser.postRepeat);
       
        String interm = " " + _text + " ";
//
//        //System.out.println(interm);
//        // Now, we eliminate redundant keywords according to the Parser configuration
//        // Unfortunately, regular expressions are of little use here, because the prefix and infix keywords may
//        // consist of or contain Regex matchers like '?' and hence aren't suitable as part of the pattern
//        // The harmful characters to be inhibited or masked are: .?*+[](){}\^$
//        //System.out.println(interm);
//        for (int i=0; i < redundantMarkers.count(); i++)
//        {
//        	String marker = redundantMarkers.get(i);
//        	if (!marker.isEmpty())
//        	{
//        		// If the marker has not been padded then we must care for proper isolation
//        		if (marker.equals(marker.trim()))
//        		{
//        			int len = marker.length();
//        			int pos = 0;
//        			// START KGU 2016-01-13: Bugfix #104: position fault
//        			//while ((pos = interm.indexOf(marker, pos)) >= 0)
//        			while ((pos = interm.indexOf(marker, pos)) > 0)
//        			// END KGU 2016-01-13
//        			{
//        				if (!Character.isJavaIdentifierPart(interm.charAt(pos-1)) &&
//        						(pos + len) < interm.length() &&
//        						!Character.isJavaIdentifierPart(interm.charAt(pos + len)))
//        				{
//        					interm = interm.substring(0, pos) + interm.substring(pos + len);
//        				}
//        			}
//        		}
//        		else
//        		{
//        			// Already padded, so just replace it everywhere
//        			// START KGU 2016-01-13: Bugfix #104 - padding might go away here
//        			//interm = interm.replace( marker, ""); 
//        			interm = interm.replace( marker, " "); 
//        			// END KGU 2016-01-13
//        		}
//        		//interm = " " + interm + " ";	// Ensure the string being padded for easier matching
//                interm = interm.replace("  ", " ");		// Reduce multiple spaces (may also spoil string literals!)
//                // START KGU 2016-01-13: Bugfix #104 - should have been done after the loop only
//                //interm = interm.trim();
//                // END KGU 2016-01-13
//        		//System.out.println("transformIntermediate: " + interm);	// FIXME (KGU): Remove or deactivate after test!
//        	}
//        }
        // START KGU 2016-01-13: Bugfix #104 - should have been done after the loop only
        interm = interm.trim();
        // END KGU 2016-01-13
        
        // START KGU#93 2015-12-21 Bugfix #41/#68/#69 Get rid of padding defects and string damages
        //interm = unifyOperators(interm);
        // END KGU#93 2015-12-21
        
		// START KGU 2015-11-30: Adopted from Root.getVarNames(): 
        // pascal: convert "inc" and "dec" procedures
        // (Of course we could omit it for Pascal, and for C offsprings there are more efficient translations, but this
        // works for all, and so we avoid trouble. 
        Regex r;
        r = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 + $2"); interm = r.replaceAll(interm);
        r = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)","$1 <- $1 + 1"); interm = r.replaceAll(interm);
        r = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 - $2"); interm = r.replaceAll(interm);
        r = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)","$1 <- $1 - 1"); interm = r.replaceAll(interm);
        // END KGU 2015-11-30

        // START KGU#93 2015-12-21 Bugfix #41/#68/#69 Get rid of padding defects and string damages
        // Reduce multiple space characters
        //interm = interm.replace("  ", " ");
        //interm = interm.replace("  ", " ");	// By repetition we eliminate the remnants of odd-number space sequences
        //return interm/*.trim()*/;

        StringList tokens = Element.splitLexically(interm, true);
        
        // START KGU#165 2016-03-26: Now keyword search with/without case
//        for (int i = 0; i < redundantMarkers.count(); i++)
//        {
//        	String marker = redundantMarkers.get(i);
//        	if (!marker.trim().isEmpty())
//        	{
//        		StringList markerTokens = Element.splitLexically(marker, false);
//        		int markerLen = markerTokens.count();
//        		int pos = -1;
//        		while ((pos = tokens.indexOf(markerTokens, 0, !D7Parser.ignoreCase)) >= 0)
//        		{
//        			for (int j = 0; j < markerLen; j++)
//        			{
//        				tokens.delete(pos);
//        			}
//        		}
//        	}
//        }
        cutOutRedundantMarkers(tokens);
        // END KGU#165 2016-03-26
        
//        // START KGU 2016-01-13: Bugfix #104 - planned new approach to overcome that nasty keyword/string problem
//        // It is also too simple, e.g. in cases like  jusqu'à test = 'o'  where a false string recognition would
//        // avert the keyvword recognition. So both will have to be done simultaneously...
//        for (int i=0; i < redundantMarkers.count(); i++)
//        {
//        	StringList markerTokens = Element.splitLexically(redundantMarkers.get(i), true);
//        	int pos = 0;
//        	while ((pos = tokens.indexOf(markerTokens, pos, true)) >= 0)
//        	{
//        		for (int j = 0; j < markerTokens.count(); j++)
//        		{
//        			tokens.delete(pos);
//        		}
//        	}
//        }
//        // END KGU 2016-01-13
        
        unifyOperators(tokens, false);
        
        return tokens;
        // END KGU#93 2015-12-21

    }
    // END KGU#18/KGU#23 2015-10-24
    
    // START KGU#162 2016-03-31: Enh. #144 - undispensible part of transformIntermediate
    public static void cutOutRedundantMarkers(StringList _tokens)
    {
    	// Collect redundant placemarkers to be deleted from the text
        StringList redundantMarkers = new StringList();
        redundantMarkers.addByLength(D7Parser.preAlt);
        redundantMarkers.addByLength(D7Parser.preCase);
        //redundantMarkers.addByLength(D7Parser.preFor);	// will be handled separately
        redundantMarkers.addByLength(D7Parser.preWhile);
        redundantMarkers.addByLength(D7Parser.preRepeat);

        redundantMarkers.addByLength(D7Parser.postAlt);
        redundantMarkers.addByLength(D7Parser.postCase);
        //redundantMarkers.addByLength(D7Parser.postFor);	// will be handled separately
        //redundantMarkers.addByLength(D7Parser.stepFor);	// will be handled separately
        redundantMarkers.addByLength(D7Parser.postWhile);
        redundantMarkers.addByLength(D7Parser.postRepeat);
        
        for (int i = 0; i < redundantMarkers.count(); i++)
        {
        	String marker = redundantMarkers.get(i);
        	if (!marker.trim().isEmpty())
        	{
        		StringList markerTokens = Element.splitLexically(marker, false);
        		int markerLen = markerTokens.count();
        		int pos = -1;
        		while ((pos = _tokens.indexOf(markerTokens, 0, !D7Parser.ignoreCase)) >= 0)
        		{
        			for (int j = 0; j < markerLen; j++)
        			{
        				_tokens.delete(pos);
        			}
        		}
        	}
        }
    }
    // END KGU#162 2016-03-31
    
    // START KGU#152 2016-03-02: Better self-description of Elements
    public String toString()
    {
    	return getClass().getSimpleName() + '@' + Integer.toHexString(hashCode()) +
    			"(" + (this.getText().count() > 0 ? this.getText().get(0) : "") + ")";
    }
    // END KGU#152 2016-03-02
    
}

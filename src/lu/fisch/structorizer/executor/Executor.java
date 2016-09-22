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

package lu.fisch.structorizer.executor;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class controls the execution of a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch                       First Issue
 *      Kay Gürtzig     2015.10.11      Method execute() now ensures that all elements get unselected
 *      Kay Gürtzig     2015.10.13      Method step decomposed into separate subroutines, missing
 *                                      support for Forever loops and Parallel sections added;
 *                                      delay mechanism reorganised in order to integrate breakpoint
 *                                      handling in a sound way
 *      Kay Gürtzig     2015.10.15      stepParallel() revised (see comment)
 *      Kay Gürtzig     2015.10.17/18   First preparations for a subroutine retrieval via Arranger
 *      Kay Gürtzig     2015.10.21      Support for multiple constants per CASE branch added
 *      Kay Gürtzig     2015.10.26/27   Language conversion and FOR loop parameter analysis delegated to the elements
 *      Kay Gürtzig     2015.11.04      Bugfix in stepInstruction() w.r.t. input/output (KGU#65)
 *      Kay Gürtzig     2015.11.05      Enhancement allowing to adopt edited values from Control (KGU#68)
 *      Kay Gürtzig     2015.11.08      Array assignments and variable setting deeply revised (KGU#69)
 *      Kay Gürtzig     2015.11.09      Bugfix: div operator had gone, wrong exit condition in stepRepeat (KGU#70),
 *                                      wrong equality operator in stepCase().
 *      Kay Gürtzig     2015.11.11      Issue #21 KGU#77 fixed: return instructions didn't terminate the execution.
 *      Kay Gürtzig     2015.11.12      Bugfix KGU#79: WHILE condition wasn't effectively converted.
 *      Kay Gürtzig     2015.11.13/14   Enhancement #9 (KGU#2) to allow the execution of subroutine calls
 *      Kay Gürtzig     2015.11.20      Bugfix KGU#86: Interpreter was improperly set up for functions sqr, sqrt;
 *                                      Message types for output and return value information corrected
 *      Kay Gürtzig     2015.11.23      Enhancement #36 (KGU#84) allowing to pause from input and output dialogs.
 *      Kay Gürtzig     2015.11.24/25   Enhancement #9 (KGU#2) enabling the execution of calls accomplished.
 *      Kay Gürtzig     2015.11.25/27   Enhancement #23 (KGU#78) to handle Jump elements properly.
 *      Kay Gürtzig     2015.12.10      Bugfix #49 (KGU#99): wrapper objects in variables obstructed comparison,
 *                                      ER #48 (KGU#97) w.r.t. delay control of diagramControllers
 *      Kay Gürtzig     2015.12.11      Enhancement #54 (KGU#101): List of output expressions
 *      Kay Gürtzig     2015.12.13      Enhancement #51 (KGU#107): Handling of empty input and output
 *      Kay Gürtzig     2015.12.15/26   Bugfix #61 (KGU#109): Precautions against type specifiers
 *      Kay Gürtzig     2016.01.05      Bugfix #90 (KGU#125): Arranger updating for executed subroutines fixed
 *      Kay Gürtzig     2016.01.07      Bugfix #91 (KGU#126): Reliable execution of empty Jump elements,
 *                                      Bugfix #92 (KGU#128): Function names were replaced within string literals
 *      Kay Gürtzig     2016.01.08      Bugfix #95 (KGU#130): div operator conversion accidently dropped
 *      Kay Gürtzig     2016.01.09      KGU#133: Quick fix to show returned arrays in a list view rather than a message box
 *      Kay Gürtzig     2016.01.14      KGU#100: Array initialisation in assignments enabled (Enh. #84)
 *      Kay Gürtzig     2016.01.15      KGU#109: More precaution against typed variables (issues #61, #107)
 *      Kay Gürtzig     2016.01.16      Bugfix #112: Several flaws in index evaluation mended (KGU#141)
 *      Kay Gürtzig     2016-01-29      Bugfix #115, enh. #84: Result arrays now always presented as list
 *                                      (with "Pause" button if not already in step mode; KGU#133, KGU#147).
 *      Kay Gürtzig     2016.03.13      Enh. #77, #124: runtime data collection implemented (KGU#117, KGU#156)
 *      Kay Gürtzig     2016.03.16      Bugfix #131: Precautions against reopening, take-over, and loss of control (KGU#157)
 *      Kay Gürtzig     2016.03.17      Enh. #133: Stacktrace now permanently maintained, not only on errors (KGU#159)
 *      Kay Gürtzig     2016.03.18      KGU#89: Language localization support slightly improved
 *      Kay Gürtzig     2016.03.21      Enh. #84 (KGU#61): Support for FOR-IN loops
 *      Kay Gürtzig     2016.03.29      Bugfix #139 (KGU#166) in getIndexValue() - nested index access failed
 *      Kay Gürtzig     2016.04.03      KGU#150: Support for Pascal functions chr and ord
 *                                      KGU#165: Case awareness consistency for keywords improved.
 *      Kay Gürtzig     2016-04-12      Enh. #137 (KGU#160): Additional or exclusive output to text window
 *      Kay Gürtzig     2016-04-25      Issue #30 (KGU#76): String comparison substantially improved,
 *                                      Enh. #174 (KGU#184): Input now accepts array initialisation expressions
 *      Kay Gürtzig     2016-04-26      KGU#150: ord implementation revised,
 *                                      Enh. #137 (KGU#160): Arguments and results added to text window output
 *      Kay Gürtzig     2016.05.05      KGU#197: Further (forgotten) texts put under language support
 *      Kay Gürtzig     2016.05.25      KGU#198: top-level function results weren't logged in the window output
 *      Kay Gürtzig     2016.06.07      KGU#200: While loops showed wrong colour if their body raised an error
 *      Kay Gürtzig     2016.07.25      Issue #201: Look-and-Feel update, Strack trace level numbers (KGU#210)
 *      Kay Gürtzig     2016-07-27      KGU#197: Further (chiefly error) messages put under language support
 *                                      Enh. #137: Error messages now also written to text window output
 *      Kay Gürtzig     2016-09-17      Bugfix #246 (Boolean expressions) and issue #243 (more translations)
 *      Kay Gürtzig     2016.09.22      Issue #248: Workaround for Java 7 in Linux systems (parseUnsignedInt)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2016-03-17 Enh. #133 (KGU#159)
 *      - Previously, a Call stack trace was only shown in cse of an execution error or manual abort.
 *        Now a Call stack trace may always be requested while execution hasn't ended. Only prerequisite
 *        is that the execution be paused. Then a double-click on the stext item showing the subroutine
 *        depth is sufficient. Moreover, the stacktrace will always be presented as list view (before a
 *        simple message box was used if the number of call levels didn't exceed 10.   
 *      2016-03-16/18 Bugfix #131 (KGU#157)
 *      - When the "run" (or "make") button was pressed while an execution was already running or stood
 *        paused then the Executor CALL stack, the event queues, and the connection between Diagram and
 *        Root often got compromised or even corrupted. The same used to happen when a Structorizer
 *        instance replaced its Root while this was involved in some execution. So these actions had to
 *        be prevented if an execution is going on. Hence, the parameterized version of getInstance() 
 *        now checks the running flag and raises a user dialog in order either to ignore the interfering
 *        action or to abort the running execution.
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
 *        blue to hot red is used to visualize the hot spots and the lost places.
 *      2015.12.10 (KGU#97, KGU#99)
 *          Bug/ER #48: An attached diagramController (usually the TurtleBox) had not immediately been
 *            informed about a delay change, such that e.g. the Turtleizer still crept in slow motion
 *            while the Executor had no delay anymore. Now a suitable diagramController will be informed.
 *          Bug 49: Equality test had failed between variables, particularly between array elements,
 *            because they presented Wrapper objects (e. g. Integer) rather than primitive values. 
 *            For scalar variables, values are now assigned as primitive type if possible (via
 *            interpreter.eval()). For array elements, in contrast, the comparison expression  will be
 *            converted, such that == and != will be replaced by .equals() calls.
 *      2015.11.23 (KGU#84) Pausing from input and output dialogs enabled (Enhancement issue #36)
 *          On cancelling input now first a warning box opens and after having quit the execution is in pause
 *          mode such that the user may edit values, abort or continue in either run oder step mode.
 *          Output and result message dialogs now provide a Pause button to allow to pause mode (see above).
 *      2015.11.13 (KGU#2) Subroutine call mechanisms introduced
 *          Recursively callable submethod of execute(Root) added plus new call-handling method executeCall()
 *          Error handling in some subroutine level still neither prepared nor tested
 *      2015.11.04 (KGU#65) Input/output execution mended
 *          The configured input / output parser settings triggered input or output action also if found
 *          deep in a line, even within a string literal. This was mended.
 *      2015.10.26/27 (KGU#3) Language conversion (in method convert) partially delegated to Element
 *          The aim was to share this functionality with generators
 *          Analysis of FOR loop parameters also delegated to the For class instance.
 *      2015.10.21 (KGU#15) Common branch for multiple constants in Case structure enabled
 *          A modification in stepCase() now allows to test against a comma-separated list of case constants
 *          (though it would fail with complex expressions, accidently containing commas but this would anyway
 *          produce nonsense on code export)
 *      2015.10.17/18 (KGU#2) Two successful (though somewhat makeshift) subroutine retrieval attempts
 *          in stepInstruction() via Arranger and by means of Bob's Function class.
 *          We can be glad that Executor is already a Singleton - on the one hand...
 *          Towards an actually working approach several challenges must therefore be addressed:
 *          1. a Stack with tuples of root, variable values, return value, and the like.
 *          2. Reentrance of the Elements or replication of entire Element hierarchies.
 *          3. Recursion on the user algorithm level (see above) - if deep copies of the diagrams are
 *             temporarily created and pushed into the Arranger then either an additional "busy" flag
 *             will be necessary on Root or a second, volatile diagram vector (not be searched!) on
 *             Surface. By design, volatile subroutine copies should never be associated with a Mainform,
 *             not even on double-clicking! By design, they should partially overlap on the Surface
 *             (in the stack order i.e. top on top).
 *          4. The trouble is going to get really nasty with Parallel elements involved, particularly if
 *             their threads use identical subroutines.   
 *      2015.10.15 (KGU#47) Improved simulation of Parallel execution
 *          Instead of running entire "threads" of the parallel section in just random order, the "threads"
 *          will now only progress by one instruction when randomly chosen, so they alternate in an
 *          unpredictable way)
 *         
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.ILoop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Updater;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.gui.Diagram;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;
import bsh.EvalError;
import bsh.Interpreter;

import com.stevesoft.pat.Regex;

/**
 * Singleton class controlling the execution of a Nassi-Shneiderman diagram.
 * Method sed as runnable thread.
 * @author robertfisch
 */
public class Executor implements Runnable
{

	private static Executor mySelf = null;

	public static Executor getInstance()
	{
		return mySelf;
	}

	public static Executor getInstance(Diagram diagram,
			DiagramController diagramController)
	{
		if (mySelf == null)
		{
			mySelf = new Executor(diagram, diagramController);
		}
		if (diagramController != null)
		{
			mySelf.diagramController = diagramController;
		}
		// START KGU#157 2016-03-16: Bugfix #131 - Don't init if there is a running thread
		//if (diagram != null)
		//{
		//	mySelf.diagram = diagram;
		//}
		//mySelf.control.init();
		//mySelf.control.setLocationRelativeTo(diagram);
		boolean doInitialise = true;
		mySelf.reopenFor = null;
		if (mySelf.diagram != null && mySelf.running)
		{
			doInitialise = false;
			Root root = mySelf.diagram.getRoot();
			String errText = mySelf.control.lbStopRunningProc.getText();
			errText = errText.replace("\\n", "\n");
			if (root != null)
			{
				errText = errText.replace("?", " (\"" + root.getMethodName() + "\")?");
			}
			int res = JOptionPane.showOptionDialog(diagram,
					   errText,
					   mySelf.control.msgTitleQuestion.getText(),
					   JOptionPane.YES_NO_OPTION,
					   JOptionPane.QUESTION_MESSAGE,
					   null,null,null);
			if (res == 0)
			{
				mySelf.setStop(true);
				mySelf.reopenFor = diagram;
			}
		}
		if (doInitialise)
		{
			if (diagram != null)
			{
				mySelf.diagram = diagram;
			}
			mySelf.control.init();
			mySelf.control.setLocationRelativeTo(diagram);
		}
		// END KGU#157 2016-03-16: Bugfix #131
		mySelf.control.validate();
		mySelf.control.setVisible(true);
		mySelf.control.repaint();

		return mySelf;
	}

	private Control control = new Control();

	// START KGU#160 2016-04-12: Enh. #137 - Option for text window output
	private OutputConsole console = new OutputConsole();
	private boolean isConsoleEnabled = false; 
	// END KGU#160 2016-04-12

	private int delay = 50;

	private Diagram diagram = null;
	
	// START KGU#2 (#9) 2015-11-13: We need a stack of calling parents
	private Stack<ExecutionStackEntry> callers = new Stack<ExecutionStackEntry>();
	private Object returnedValue = null;
	private Vector<IRoutinePool> routinePools = new Vector<IRoutinePool>();
	// END KGU#2 (#9) 2015-11-13

	private DiagramController diagramController = null;
	private Interpreter interpreter;

	private boolean paus = false;
	private boolean returned = false;
	private boolean running = false;
	private boolean step = false;
	private boolean stop = false;
	// START KGU#78 2015-11-25: JUMP enhancement (#35)
	private int loopDepth = 0;	// Level of nested loops
	private int leave = 0;		// Number of loop levels to unwind
	// END KGU#78 2015-11-25
	private StringList variables = new StringList();
	// START KGU#2 2015-11-24: It is crucial to know whether an error had been reported on a lower level
	private boolean isErrorReported = false;
	private StringList stackTrace = new StringList();
	// END KGU#2 2015-11-22
	// START KGU#157 2016-03-16: Bugfix #131 - Precaution against a reopen attempts by different Structorizer instances
	private Diagram reopenFor = null;	// A Structorizer instance that tried to open Control while still running
	// END KGU#2 2016-03-16

	private Executor(Diagram diagram, DiagramController diagramController)
	{
		this.diagram = diagram;
		this.diagramController = diagramController;
	}

	// START KGU#210/KGU#234 2016-08-08: Issue #201 - Ensure GUI consistency
	public static void updateLookAndFeel()
	{
		if (mySelf != null)
		{
			mySelf.control.updateLookAndFeel();
		}
	}
	// END KGU#210/KGU#234 2016-08-08
	
	// METHOD MODIFIED BY GENNARO DONNARUMMA

	private String convert(String s)
	{
		return convert(s, true);
	}
	
	private String convert(String s, boolean convertComparisons)
	{
		Regex r;

		// START KGU#128 2016-01-07: Bugfix #92 - Effort via tokens to avoid replacements within string literals
//		s = Element.unifyOperators(s);
//		s = s.replace(" div ", " / ");		// FIXME: Operands should be coerced to integer...
//
//		// Convert built-in mathematical functions
//		s = s.replace("cos(", "Math.cos(");
//		s = s.replace("sin(", "Math.sin(");
//		s = s.replace("tan(", "Math.tan(");
//        // START KGU 2014-10-22: After the previous replacements the following 3 strings would never be found!
//        //s=s.replace("acos(", "Math.acos(");
//        //s=s.replace("asin(", "Math.asin(");
//        //s=s.replace("atan(", "Math.atan(");
//        // This is just a workaround; A clean approach would require a genuine lexical scanning in advance
//        s=s.replace("aMath.cos(", "Math.acos(");
//        s=s.replace("aMath.sin(", "Math.asin(");
//        s=s.replace("aMath.tan(", "Math.atan(");
//        // END KGU 2014-10-22:
//		s = s.replace("abs(", "Math.abs(");
//		s = s.replace("round(", "Math.round(");
//		s = s.replace("min(", "Math.min(");
//		s = s.replace("max(", "Math.max(");
//		s = s.replace("ceil(", "Math.ceil(");
//		s = s.replace("floor(", "Math.floor(");
//		s = s.replace("exp(", "Math.exp(");
//		s = s.replace("log(", "Math.log(");
//		s = s.replace("sqrt(", "Math.sqrt(");
//		s = s.replace("pow(", "Math.pow(");
//		s = s.replace("toRadians(", "Math.toRadians(");
//		s = s.replace("toDegrees(", "Math.toDegrees(");
//		// s=s.replace("random(", "Math.random(");
		StringList tokens = Element.splitLexically(s, true);
		Element.unifyOperators(tokens, false);
		// START KGU#130 2015-01-08: Bugfix #95 - Conversion of div operator had been forgotten...
		tokens.replaceAll("div", "/");		// FIXME: Operands should better be coerced to integer...
		// END KGU#130 2015-01-08
		// Function names to be prefixed with "Math."
		final String[] mathFunctions = {
				"cos", "sin", "tan", "acos", "asin", "atan", "toRadians", "toDegrees",
				"abs", "round", "min", "max", "ceil", "floor", "exp", "log", "sqrt", "pow"
				};
		StringList fn = new StringList();
		fn.add("DUMMY");
		fn.add("(");
		for (int f = 0; f < mathFunctions.length; f++)
		{
			int pos = 0;
			fn.set(0, mathFunctions[f]);
			while ((pos = tokens.indexOf(fn, pos, true)) >= 0)
			{
				tokens.set(pos, "Math." + mathFunctions[f]);
			}
		}
		s = tokens.concatenate();
		// END KGU#128 2016-01-07

		// pascal notation to access a character inside a string
		//r = new Regex("(.*)\\[(.*)\\](.*)", "$1.charAt($2-1)$3");
		//r = new Regex("(.*)\\[(.*)\\](.*)", "$1.substring($2-1,$2)$3");
		// MODIFIED BY GENNARO DONNARUMMA, NEXT LINE COMMENTED -->
		// NO REPLACE ANY MORE! CHARAT AND SUBSTRING MUST BE CALLED MANUALLY
		// s = r.replaceAll(s);
		// pascal: delete
		r = new Regex("delete\\((.*),(.*),(.*)\\)", "$1=delete($1,$2,$3)");
		s = r.replaceAll(s);
		// pascal: insert
		r = new Regex("insert\\((.*),(.*),(.*)\\)", "$2=insert($1,$2,$3)");
		s = r.replaceAll(s);
		// pascal: quotes
		r = new Regex("([^']*?)'(([^']|'')*)'", "$1\"$2\"");
		//r = new Regex("([^']*?)'(([^']|''){2,})'", "$1\"$2\"");
		s = r.replaceAll(s);
		// START KGU 2015-11-29: Adopted from Root.getVarNames() - can hardly be done in initialiseInterpreter() 
        // pascal: convert "inc" and "dec" procedures
        r = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 + $2"); s = r.replaceAll(s);
        r = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)","$1 <- $1 + 1"); s = r.replaceAll(s);
        r = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 - $2"); s = r.replaceAll(s);
        r = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)","$1 <- $1 - 1"); s = r.replaceAll(s);
        // END KGU 2015-11-29
		
		s = s.replace("''", "'");	// FIXME (KGU 2015-11-29): Looks like an unwanted relic!
		// pascal: randomize
		s = s.replace("randomize()", "randomize");
		s = s.replace("randomize", "randomize()");

		// clean up ... if needed
		s = s.replace("Math.Math.", "Math.");

		if (convertComparisons)
		{
			s = convertStringComparison(s);
		}

		// System.out.println(s);
		return s;
	}
	
	// START KGU#57 2015-11-07
	private String convertStringComparison(String str)
	{
//		Character chA = 'a';
//		Character chB = 'a';
//		System.out.println("Zeichen sind " + ((chA == chB) ? "" : "NICHT ") + "identisch!");
//		System.out.println("Zeichen sind " + ((chA.equals(chB)) ? "" : "NICHT ") + "gleich!");
		// Is there any equality test at all?
		// START KGU#76 2016-04-25: Issue #30 - convert all string comparisons
		//if (str.indexOf(" == ") >= 0 || str.indexOf(" != ") >= 0)
		String[] compOps = {"==", "!=", "<=", ">=", "<", ">"};
		boolean containsComparison = false;
		for (int op = 0; !containsComparison && op < compOps.length; op++)
		{
			containsComparison = str.indexOf(compOps[op]) >= 0;
		}
		if (containsComparison)
		// END KGU#76 2016-04-25
		{
			// We are looking for || operators and split the expression by them (if present) 
			StringList exprs = StringList.explodeWithDelimiter(str, " \\|\\| ");	// '|' is a regex metasymbol!
			// Now we do the same with && operators
			exprs = StringList.explodeWithDelimiter(exprs, " && ");
			// Now we should have some atomic assertions, among them comparisons
			boolean replaced = false;
			for (int i = 0; i < exprs.count(); i++)
			{
				String s = exprs.get(i);
				// START KGU#76 2016-04-25: Issue #30 - convert all string comparisons
				//String[] eqOps = {"==", "!="};
				//for (int op = 0; op < eqOps.length; op++)
				StringList tokens = Element.splitLexically(s.trim(), true);
				for (int op = 0; op < compOps.length; op++)
				// END KGU#76 2016-04-25
				{
					// START KGU#76 2016-04-25: Issue #30
					//Regex r = null;
					// We can no longer expect operators to be padded, better use tokens
					//if (!s.equals(" " + eqOps[op] + " ") && s.indexOf(eqOps[op]) >= 0)
					int opPos = -1;		// Operator position
					if ((opPos = tokens.indexOf(compOps[op])) >= 0)
					{
						String leftParenth = "";
						String rightParenth = "";
						// Get the left operand expression
						// START KGU#76 2016-04-25: Issue #30
						//r = new Regex("(.*)"+eqOps[op]+"(.*)", "$1");
						//String left = r.replaceAll(s).trim();	// All? Really? And what is the result supposed to be then?
						String left = tokens.concatenate("", 0, opPos).trim();
						// END KGU#76 2016-04-25
						// Re-balance parentheses
						while (Function.countChar(left, '(') > Function.countChar(left, ')') &&
								left.startsWith("("))
						{
							leftParenth = leftParenth + "(";
							left = left.substring(1).trim();
						}
						// Get the right operand expression
						// START KGU#76 2016-04-25: Issue #30
						//r = new Regex("(.*)"+eqOps[op]+"(.*)", "$2");
						//String right = r.replaceAll(s).trim();
						String right = tokens.concatenate("", opPos+1).trim();
						// END KGU#76 2016-04-25
						// Re-balance parentheses
						while (Function.countChar(right, ')') > Function.countChar(right, '(') &&
								right.endsWith(")"))
						{
							rightParenth = rightParenth + ")";
							right = right.substring(0, right.length()-1).trim();
						}
						// ---- thanks to autoboxing, we can always use the "equals" method
						// ---- to compare things ...
						// addendum: sorry, doesn't always work.
						try
						{
							int pos = -1;	// some character position
							Object leftO = interpreter.eval(left);
							Object rightO = interpreter.eval(right);
							String neg = (op > 0) ? "!" : "";
							// First the obvious case: two String expressions
							if ((leftO instanceof String) && (rightO instanceof String))
							{
								// START KGU#76 2016-04-25: Issue #30 support all string comparison
								//exprs.set(i, leftParenth + neg + left + ".equals(" + right + ")" + rightParenth);
								exprs.set(i, leftParenth + left + ".compareTo(" + right + ") " + compOps[op] + " 0" + rightParenth);
								// END KGU#76 2016-04-25
								replaced = true;
							}
							// We must make single-char strings comparable with characters, since it
							// doesn't work automatically and several conversions have been performed 
							else if ((leftO instanceof String) && (rightO instanceof Character))
							{
								// START KGU#76 2016-04-25: Issue #30 support all string comparison
								//exprs.set(i, leftParenth + neg + left + ".equals(\"" + (Character)rightO + "\")" + rightParenth);
								exprs.set(i, leftParenth + left + ".compareTo(\"" + (Character)rightO + "\") " + compOps[op] + " 0" + rightParenth);
								// END KGU#76 2016-04-25
								replaced = true;								
							}
							else if ((leftO instanceof Character) && (rightO instanceof String))
							{
								// START KGU#76 2016-04-25: Issue #30 support all string comparison
								//exprs.set(i, leftParenth + neg + right + ".equals(\"" + (Character)leftO + "\")" + rightParenth);
								exprs.set(i, leftParenth + "\"" + (Character)leftO + "\".compareTo(" + right + ") " + compOps[op] + " 0" + rightParenth);
								// END KGU#76 2016-04-25
								replaced = true;								
							}
							// START KGU#99 2015-12-10: Bugfix #49 (also replace if both operands are array elements (objects!)
							// START KGU#76 2016-04-25: Issue #30 - this makes only sense for "==" and "!="
							//else if ((pos = left.indexOf('[')) > -1 && left.indexOf(']', pos) > -1 && 
							else if (op < 2 &&
									(pos = left.indexOf('[')) > -1 && left.indexOf(']', pos) > -1 && 
							// END KGU#76 2016-04-25
									(pos = right.indexOf('[')) > -1 && right.indexOf(']', pos) > -1)
							{
								exprs.set(i, leftParenth + neg + left + ".equals(" + right + ")" + rightParenth);
								replaced = true;								
							}
							// END KGU#99 2015-12-10
						} catch (EvalError ex)
						{
							System.err.println(ex.getMessage());
						}
					} // if (!s.equals(" " + eqOps[op] + " ") && (s.indexOf(eqOps[op]) >= 0))
				} // for (int op = 0; op < eqOps.length; op++)
				if (replaced)
				{
					// Compose the partial expressions and undo the regex escaping for the initial split
					str = BString.replace(exprs.getLongString(), " \\|\\| ", " || ");
					str.replace("  ", " ");	// Get rid of multiple spaces
				}
			}
		}
		return str;
	}
	// END KGU#57 2015-11-07

	private void delay()
	{
		if (delay != 0)
		{
			diagram.redraw();
			try
			{
				Thread.sleep(delay);
			} catch (InterruptedException e)
			{
				System.err.println(e.getMessage());
			}
		}
		waitForNext();
	}

	/**
	 * @param aStep
	 *            the step to set
	 */
	public void doStep()
	{
		synchronized (this)
		{
			paus = false;
			step = true;
			this.notify();
		}
	}
	
	// START KGU#117 2016-03-08: Enh. #77
	/**
	 * Clears the execution status of all routines held by known
	 * subroutine pools  
	 */
	public void clearPoolExecutionStatus()
	{
		Iterator<IRoutinePool> iter = this.routinePools.iterator();
		while (iter.hasNext())
		{
			iter.next().clearExecutionStatus();
		}
		this.diagram.clearExecutionStatus();
		// START KGU#156 2016-03-10: Enh. #124
		if (!Element.E_COLLECTRUNTIMEDATA)
		{
			Element.resetMaxExecCount();
		}
		// END KGU#156 2016-03-10
	}
	// END KGU#117 2016-03-08

	// METHOD MODIFIED BY GENNARO DONNARUMMA

	public void execute()
	// START KGU#2 (#9) 2015-11-13: We need a recursively applicable version
	{
		this.callers.clear();
		this.stackTrace.clear();
		this.routinePools.clear();
		if (diagram.isArrangerOpen)
		{
			this.routinePools.addElement(Arranger.getInstance());
			// START KGU#117 2016-03-08: Enh. #77
			Arranger.getInstance().clearExecutionStatus();
			// END KGU#117 2016-03-08
		}
		this.isErrorReported = false;
		this.diagram.getRoot().isCalling = false;
		// START KGU#160 2016-04-12: Enh. #137 - Address the console window 
		this.console.clear();
		SimpleDateFormat sdf = new SimpleDateFormat();
		this.console.writeln("*** STARTED \"" + this.diagram.getRoot().getText().getLongString() +
				"\" at " + sdf.format(System.currentTimeMillis()) + " ***", Color.GRAY);
		if (this.isConsoleEnabled) this.console.setVisible(true);
		// END KGU#160 2016-04-12
		/////////////////////////////////////////////////////////
		this.execute(null);	// The actual top-level execution
		/////////////////////////////////////////////////////////
		this.callers.clear();
		this.stackTrace.clear();
		// START KGU#160 2016-04-12: Enh. #137 - Address the console window 
		this.console.writeln("*** TERMINATED \"" + this.diagram.getRoot().getText().getLongString() +
				"\" at " + sdf.format(System.currentTimeMillis()) + " ***", Color.GRAY);
		if (this.isConsoleEnabled) this.console.setVisible(true);
		// END KGU#160 2016-04-12
		//System.out.println("stackTrace size: " + stackTrace.count());
	}
	
	/**
	 * Executes the current diagram held by this.diagram, applicable for main or sub routines 
	 * @param arguments - list of interpreted argument values or null (if main program)
	 * @return the result value of the algorithm (if not being a program)
	 */
	private boolean execute(Object[] arguments)
	{
		boolean successful = true;
	// END KGU#2 (#9) 2015-11-13
		
		Root root = diagram.getRoot();

		// START KGU#159 2016-03-17: Now we permanently maintain the stacktrace, not only in case of error
		addToStackTrace(root, arguments);
		// END KGU#159 2016-03-17
		
		// START KGU#2 (#9) 2015-11-14
		Iterator<Updater> iter = root.getUpdateIterator();
		while (iter.hasNext())
		{
			Updater pool = iter.next();
			if (pool instanceof IRoutinePool && !this.routinePools.contains(pool))
			{
				this.routinePools.addElement((IRoutinePool)pool);
			}
		}
		// END KGU#2 (#9) 2015-11-14

		boolean analyserState = diagram.getAnalyser();
		diagram.setAnalyser(false);
		// START KGU 2015-10-11/13:
		// Unselect all elements before start!
		//diagram.unselectAll();	// KGU 2016-03-08: There is no need anymore
		// Reset all execution state remnants (just for sure)
		diagram.clearExecutionStatus();
		// END KGU 2015-10-11/13
		initInterpreter();
		String result = "";
		returned = false;
		// START KGU#78 2015-11-25
		loopDepth = 0;
		leave = 0;
		// END KGU#78 2015-11-25

		// START KGU#39 2015-10-16 (1/2): It made absolutely no sense to look for parameters if root is a program
		if (!root.isProgram)
		{
		// END KGU#39 2015-10-16 (1/2)
			StringList params = root.getParameterNames();
			//System.out.println("Having: "+params.getCommaText());
			// START KGU#2 2015-12-05: New mechanism of getParameterNames() made reverting wrong
			//params=params.reverse();
			// END KGU#2 2015-12-05
			//System.out.println("Having: "+params.getCommaText());
			// START KGU#2 2015-11-24
			boolean noArguments = arguments == null;
			if (noArguments) arguments = new Object[params.count()];
			// END KGU#2 2015-11-24
			for (int i = 0; i < params.count(); i++)
			{
				String in = params.get(i);
				
				// START KGU#2 (#9) 2015-11-13: If root was not called then ask the user for values
				if (noArguments)
				{
				// END KGU#2 (#9) 2015-11-13
					// START KGU#89 2016-03-18: More language support 
					//String str = JOptionPane.showInputDialog(null,
					//		"Please enter a value for <" + in + ">", null);
					String msg = control.lbInputValue.getText();
					msg = msg.replace("%", in);
					String str = JOptionPane.showInputDialog(null, msg, null);
					// END KGU#89 2016-03-18
					if (str == null)
					{
						//i = params.count();	// leave the loop
						// START KGU#197 2016-07-27: Enhanced localization
						//result = "Manual break!";
						result = control.msgManualBreak.getText();
						// END KGU#197 2016-07-27
						break;
					}
					try
					{
						// START KGU#69 2015-11-08 What we got here is to be regarded as raw input
						setVarRaw(in, str);
						// END KGU#69 2015-11-08
						// START KGU#2 2015-11-24: We might need the values for a stacktrace
						arguments[i] = interpreter.get(in);
						// END KGU#2 2015-11-24
						// START KGU#160 2016-04-26: Issue #137 - document the arguments
						this.console.writeln("*** Argument <" + in + "> = " + this.prepareValueForDisplay(arguments[i]), Color.CYAN);
						// END KGU#160 2016-04-26
					} catch (EvalError ex)
					{
						result = ex.getLocalizedMessage();
						if (result == null) result = ex.getMessage();
						break;
					}
				// START KGU#2 (#9) 2015-11-13: If root was called then just assign the arguments
				}
				else
				{
					try
					{
						setVar(in, arguments[i]);
					}
					catch (EvalError ex)
					{
						result = ex.getLocalizedMessage();
						if (result == null) result = ex.getMessage();
						break;
					}
				}
				// END KGU#2 (#9) 2015-11-13
			}
		// START KGU#39 2015-10-16
		}
		// END KGU#39 2015-10-16

		if (result.equals(""))
		{
			/////////////////////////////////////////////////////
			// Actual start of execution 
			/////////////////////////////////////////////////////
			result = step(root);
			
			if (result.equals("") && (stop == true))
			{
				// START KGU#197 2016-07-27: Enhanced localization
				//result = "Manual break!";
				result = control.msgManualBreak.getText();
				// END KGU#197 2016-07-27
			}
		}

		diagram.redraw();
		if (!result.equals(""))
		{
			// START KGU#2 (#9) 2015-11-13
			successful = false;
			// END KGU#2 (#9) 2015-11-13
			
			// MODIFIED BY GENNARO DONNARUMMA, ADDED ARRAY ERROR MSG
			
			String modifiedResult = result;
			// FIXME (KGU): If the interpreter happens to provide localized messages then this won't work anymore!
			if (result.contains("Not an array"))
			{
				modifiedResult = modifiedResult.concat(" or the index "
						+ modifiedResult.substring(
								modifiedResult.indexOf("[") + 1,
								modifiedResult.indexOf("]"))
						+ " is out of bounds (invalid index)");
				result = modifiedResult;
			}

			// START KGU#2 2015-11-22: If we are on a subroutine level, then we must stop the show
			//JOptionPane.showMessageDialog(diagram, result, "Error",
			//		JOptionPane.ERROR_MESSAGE);
			if (!isErrorReported)
			{
				JOptionPane.showMessageDialog(diagram, result, control.msgTitleError.getText(),
						JOptionPane.ERROR_MESSAGE);
				// START KGU#160 2016-07-27: Issue #137 - also log the result to the console
				this.console.writeln("*** " + result, Color.RED);
				// END KGU#160 2016-07-27
				isErrorReported = true;
			}
			if (!this.callers.isEmpty())
			{
				stop = true;
				paus = false;
				step = false;
			}
			else if (isErrorReported && stackTrace.count() > 1)
			{
				// START KGU#159 2016-03-17: Now we permanently maintain the stacktrace, so there is no need anymore
				//addToStackTrace(root, arguments);
				// END KGU#159 2016-03-17
				showStackTrace();
			}
			// END KGU#2 2015-11-24	
		} else
		{
			if ((root.isProgram == false) && (returned == false))
			{
				StringList posres = new StringList();
				posres.add(root.getMethodName());
				posres.add("result");
				posres.add("RESULT");
				posres.add("Result");

				try
				{
					int i = 0;
					while ((i < posres.count()) && (!returned))
					{
						Object resObj = interpreter.get(posres.get(i));
						if (resObj != null)
						{
							// START KGU#2 (#9) 2015-11-13: Only tell the user if this wasn't called
							//JOptionPane.showMessageDialog(diagram, n,
							//		"Returned result", 0);
							this.returnedValue = resObj;
							if (this.callers.isEmpty())
							{
								// START KGU#197 2016-05-25: Translate the headline!
								String header = control.lbReturnedResult.getText();
								// END KGU#197 2016-05-25
								// START KGU#133 2016-01-09: Show large arrays in a listview
								//JOptionPane.showMessageDialog(diagram, n,
								//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
								// KGU#133 2016-01-29: Arrays now always shown as listview (independent of size)
								if (resObj instanceof Object[] /*&& ((Object[])resObj).length > 20*/)
								{
									// START KGU#147 2016-01-29: Enh. #84 - interface changed for more flexibility
									//showArray((Object[])resObj, "Returned result");
									showArray((Object[])resObj, header, !step);
									// END KGU#147 2016-01-29
								}
								// START KGU#84 2015-11-23: Enhancement to give a chance to pause (though of little use here)
								//else
								//{
									//JOptionPane.showMessageDialog(diagram, resObj,
									//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
								//}
								else if (step)
								{
									// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
									this.console.writeln("*** " + header + ": " + this.prepareValueForDisplay(resObj), Color.CYAN);
									// END KGU#160 2016-04-26
									JOptionPane.showMessageDialog(diagram, resObj,
											header, JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									// START KGU#198 2016-05-25: Issue #137 - also log the result to the console
									this.console.writeln("*** " + header + ": " + this.prepareValueForDisplay(resObj), Color.CYAN);
									// END KGU#198 2016-05-25
									Object[] options = {"OK", "Pause"};		// FIXME: Provide a translation
									int pressed = JOptionPane.showOptionDialog(diagram, resObj, header,
											JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
									if (pressed == 1)
									{
										paus = true;
										step = true;
										control.setButtonsForPause();
									}
								}
								// END KGU#84 2015-11-23
								// END KGU#133 2016-01-09
							}
							// START KGU#148 2016-01-29: Pause now here, particularly for subroutines
							delay();
							// END KGU#148 2016-01-29							
							// END KGU#2 (#9) 2015-11-13
							returned = true;
						}
						i++;
					}
				} catch (EvalError ex)
				{
					Logger.getLogger(Executor.class.getName()).log(
							Level.SEVERE, null, ex);
				}

			}

		}
		// START KGU 2015-10-13: Unsets all execution flags in the diagram
		diagram.clearExecutionStatus();
		// END KGU 2015-10-13
		diagram.setAnalyser(analyserState);

		if (successful)
		{
			dropFromStackTrace();
		}
		
		// START KGU#2 (#9) 2015-11-13: Need the status
		return successful;
		// END KGU# (#9) 2015-11-13
	}
	
	// START KGU#133 2016-01-09: New method for presenting result arrays as scrollable list
	// START KGU#147 2016-01-29: Enh. #84 - interface enhanced, pause button added
	//private void showArray(Object[] _array, String _title)
	private void showArray(Object[] _array, String _title, boolean withPauseButton)
	// END KGU#147 2016-01-29
	{	
		JDialog arrayView = new JDialog();
		arrayView.setTitle(_title);
		arrayView.setIconImage(IconLoader.ico004.getImage());
		arrayView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// START KGU#147 2016-01-29: Enh. #84 (continued)
		JButton btnPause = new JButton("Pause");
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) 
			{
				step = true; paus = true; control.setButtonsForPause();
				if (event.getSource() instanceof JButton)
				{
					Container parent = ((JButton)(event.getSource())).getParent();
					while (parent != null && !(parent instanceof JDialog))
					{
						parent = parent.getParent();
					}
					if (parent != null) {
						((JDialog)parent).dispose();
					}
				}
			}
		});
		arrayView.getContentPane().add(btnPause, BorderLayout.NORTH);
		btnPause.setVisible(withPauseButton);
		// END KGU#147 2016-01-29
		// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
		this.console.writeln("*** " + _title + ":", Color.CYAN);
		// END KGU#160 2016-04-26
		List arrayContent = new List(10);
		for (int i = 0; i < _array.length; i++)
		{
			// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
			String valLine = "[" + i + "]  " + prepareValueForDisplay(_array[i]);
			this.console.writeln("\t" + valLine, Color.CYAN);
			// END KGU#160 2016-04-26
			arrayContent.add(valLine);
		}
		arrayView.getContentPane().add(arrayContent, BorderLayout.CENTER);
		arrayView.setSize(300, 300);
		arrayView.setLocationRelativeTo(control);
		arrayView.setModalityType(ModalityType.APPLICATION_MODAL);
		arrayView.setVisible(true);
	}
	// END KGU#133 2016-01-09
	
	// START KGU#2 (#9) 2015-11-13: New method to execute a called subroutine
	// START KGU#156 2016-03-12: Enh. #124 - signature enhanced to overcome some nasty hacks
	//private Object executeCall(Root subRoot, Object[] arguments)
	private Object executeCall(Root subRoot, Object[] arguments, Call caller)
	// END KGU#156 2016-03-12
	{
		boolean cloned = false;
		Root root = subRoot;
		Object resultObject = null;
		Root oldRoot = this.diagram.getRoot();
		ExecutionStackEntry entry = new ExecutionStackEntry(
				oldRoot,
				this.variables, 
				this.interpreter,
				// START KGU#78 2015-11-25
				this.loopDepth
				// END KGU#78 2015-11-25
				);
		this.callers.push(entry);
		this.interpreter = new Interpreter();
		this.initInterpreter();
		this.variables = new StringList();
		
		// If the found subroutine is already an active caller, then we need a new instance of it
		if (root.isCalling)
		{
			root = (Root)root.copy();
			root.isCalling = false;
			// Remaining initialisations will be done by this.execute(...).
			cloned = true;
		}
		
		this.diagram.setRoot(root, true);
		
		// START KGU#156 2016-03-11: Enh. #124 - detect execution counter diff.
		int countBefore = root.getExecStepCount(true);
		// END KGU#156 2016-03-11
		/*boolean done =*/ this.execute(arguments);
		// START KGU#156 2016-03-11; Enh. #124
		caller.addToExecTotalCount(root.getExecStepCount(true) - countBefore, true);
		if (cloned || root.isTestCovered(true))	
		{
			caller.deeplyCovered = true;
		}
		// END KGU#156 2016-03-11

		// START KGU#2 2015-11-24
//		if (!done || stop)
//		{
//			addToStackTrace(root, arguments);
//		}
		// END KGU#2 2015-11-24
		
		// START KGU#117 2016-03-07: Enh. #77
		// For recursive calls the coverage must be combined
		if (cloned && Element.E_COLLECTRUNTIMEDATA)
		{
			subRoot.combineRuntimeData(root);
		}
		// END KG#117 2016-03-07
		
		// START KGU#117 2016-03-07: Enh. #77
		// For recursive calls the coverage must be combined
		if (cloned && Element.E_COLLECTRUNTIMEDATA)
		{
			subRoot.combineRuntimeData(root);
		}
		// END KG#117 2016-03-07
		
		this.callers.pop();	// Should be the entry still held by variable entry
					
		this.variables = entry.variables;
		this.interpreter = entry.interpreter;
		// START KGU#78 2015-11-25
		this.loopDepth = entry.loopDepth;
		// END KGU#78 2015-11-25
		this.diagram.setRoot(entry.root);
		entry.root.isCalling = false;

		// The called subroutine will certainly have returned a value...
		resultObject = this.returnedValue;
		// ... but definitively not THIS calling routine!
		this.returned = false;
		this.returnedValue = null;
		
		try 
		{
			updateVariableDisplay();
		}
		catch (EvalError ex) {}
		
		return resultObject;
	}
	
	// START KGU#2 2015-11-24: Stack trace support for execution errors
	private void addToStackTrace(Root _root, Object[] _arguments)
	{
		String argumentString = "";
		if (_arguments != null)
		{
			for (int i = 0; i < _arguments.length; i++)
			{
				argumentString = argumentString + (i>0 ? ", " : "") + prepareValueForDisplay(_arguments[i]);					
			}
			argumentString = "(" + argumentString + ")";
		}
		this.stackTrace.add(_root.getMethodName() + argumentString);
	}
	
	// START KGU#159 2016-03-17: Stacktrace should always be available on demand, not only on error
	private void dropFromStackTrace()
	{
		int size = this.stackTrace.count();
		if (size > 0)
		{
			this.stackTrace.delete(size-1);
		}
	}
	// END KGU#159 2016-03-17

	/**
	 * Pops up a dialog displaying the call trace with argument values
	 */
	public void showStackTrace()
	{
// START KGU#159 2016-03-17: A listview is always the better choice
// (Think of large arrays as arguments!)
//		if (stackTrace.count() <= 20)
//		{
//			// Okay, keep it simple
//			JOptionPane.showMessageDialog(diagram, this.stackTrace.getText(), "Stack trace",
//					JOptionPane.INFORMATION_MESSAGE);
//		}
//		else
//		{
// END KGU#159 2016-03-17
			JDialog stackView = new JDialog();
			stackView.setTitle("Stack trace");
			stackView.setIconImage(IconLoader.ico004.getImage());
			List stackContent = new List(10);
			int depth = stackTrace.count();
			for (int i = 0; i < depth; i++)
			{
				// START KGU#201 2016-07-25: Issue #201 - level indices added
				//stackContent.add(stackTrace.get(depth - i - 1));
				stackContent.add(depth-i-1 + ": " + stackTrace.get(depth - i - 1));
				// END KGU#201 2016-07-25
			}
			stackView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    stackView.getContentPane().add(stackContent, BorderLayout.CENTER);
		    stackView.setSize(300, 300);
		    stackView.setLocationRelativeTo(control);
		    stackView.setModalityType(ModalityType.APPLICATION_MODAL);
		    stackView.setVisible(true);
// START KGU#159 2016-03-17: A listview is always the better choice
//		}		
// END KGU#159 2016-03-17
	}
	// END KGU#2 2015-11-24
	
    /**
     * Searches all known pools for subroutines with a signature compatible to name(arg1, arg2, ..., arg_nArgs) 
     * @param name - function name
     * @param nArgs - number of parameters of the requested function
     * @return a Root that matches the specification if uniquely found, null otherwise
     */
    public Root findSubroutineWithSignature(String name, int nArgs)
    {
    	Root subroutine = null;
    	// First test whether the current root calls itself recursively
    	Root root = diagram.getRoot();
    	if (name.equals(root.getMethodName()) && nArgs == root.getParameterNames().count())
    	{
    		subroutine = root;
    	}
    	Iterator<IRoutinePool> iter = this.routinePools.iterator();
    	while (subroutine == null && iter.hasNext())
    	{
    		IRoutinePool pool = iter.next();
    		Vector<Root> candidates = pool.findRoutinesBySignature(name, nArgs);
    		for (int c = 0; subroutine == null && c < candidates.size(); c++)
    		{
    	    	// TODO Check for ambiguity (multiple matches) and raise e.g. an exception in that case
    			subroutine = candidates.get(c);
    			// START KGU#125 2016-01-05: Is to force updating of the diagram status
    			if (pool instanceof Updater)
    			{
    				subroutine.addUpdater((Updater)pool);
    			}
    			diagram.adoptArrangedOrphanNSD(subroutine);
    			// END KGU#125 2016-01-05
    		}
    	}
    	return subroutine;
    }
	// END KGU#2 (#9) 2015-11-13

	public String getExec(String cmd)
	{
		String result = "";
		if (diagramController != null)
		{
			result = diagramController.execute(cmd);
		} else
		{
			delay();
		}
		if (delay != 0)
		{
			diagram.redraw();
			try
			{
				Thread.sleep(delay);
			} catch (InterruptedException e)
			{
				System.err.println(e.getMessage());
			}
		}
		return result;
	}

	public String getExec(String cmd, Color color)
	{
		String result = "";
		if (diagramController != null)
		{
			result = diagramController.execute(cmd, color);
		} else
		{
			delay();
		}
		if (delay != 0)
		{
			diagram.redraw();
			try
			{
				Thread.sleep(delay);
			} catch (InterruptedException e)
			{
				System.err.println(e.getMessage());
			}
		}
		return result;
	}

	public boolean getPaus()
	{
		synchronized (this)
		{
			return paus;
		}
	}

	private void initInterpreter()
	{
		try
		{
			interpreter = new Interpreter();
			String pascalFunction;
			// random
			pascalFunction = "public int random(int max) { return (int) (Math.random()*max); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public void randomize() {  }";
			interpreter.eval(pascalFunction);
			// square
			pascalFunction = "public double sqr(double d) { return d * d; }";
			interpreter.eval(pascalFunction);
			// square root
			pascalFunction = "public double sqrt(double d) { return Math.sqrt(d); }";
			interpreter.eval(pascalFunction);
			// length of a string
			pascalFunction = "public int length(String s) { return s.length(); }";
			interpreter.eval(pascalFunction);
			// position of a substring inside another string
			pascalFunction = "public int pos(String subs, String s) { return s.indexOf(subs)+1; }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public int pos(Character subs, String s) { return s.indexOf(subs)+1; }";
			interpreter.eval(pascalFunction);
			// return a substring of a string
			pascalFunction = "public String copy(String s, int start, int count) { return s.substring(start-1,start-1+count); }";
			interpreter.eval(pascalFunction);
			// delete a part of a string
			pascalFunction = "public String delete(String s, int start, int count) { return s.substring(0,start-1)+s.substring(start+count-1,s.length()); }";
			interpreter.eval(pascalFunction);
			// insert a string into anoter one
			pascalFunction = "public String insert(String what, String s, int start) { return s.substring(0,start-1)+what+s.substring(start-1,s.length()); }";
			interpreter.eval(pascalFunction);
			// string transformation
			pascalFunction = "public String lowercase(String s) { return s.toLowerCase(); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public String uppercase(String s) { return s.toUpperCase(); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public String trim(String s) { return s.trim(); }";
			interpreter.eval(pascalFunction);
			// START KGU#57 2015-11-07: More interoperability for characters and Strings
			// char transformation
			pascalFunction = "public Character lowercase(Character ch) { return (Character)Character.toLowerCase(ch); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public Character uppercase(Character ch) { return (Character)Character.toUpperCase(ch); }";
			interpreter.eval(pascalFunction);
			// START KGU#150 2016-04-03
			pascalFunction = "public int ord(Character ch) { return (int)ch; }";
			interpreter.eval(pascalFunction);
			// START KGU 2016-04-26: It is conform to many languages just to use the first character
			//pascalFunction = "public int ord(String s) throws Exception { if (s.length() == 1) return (int)s.charAt(0); else throw new Exception(); }";
			pascalFunction = "public int ord(String s) { return (int)s.charAt(0); }";
			// END KGU 2016-04-26
			interpreter.eval(pascalFunction);
			pascalFunction = "public char chr(int code) { return (char)code; }";
			interpreter.eval(pascalFunction);
			// END KGU#150 2016-04-03
			// END KGU#57 2015-11-07
		} catch (EvalError ex)
		{
			System.err.println(ex.getMessage());
		}
	}

	public boolean isNumeric(String input)
	{
		try
		{
			Double.parseDouble(input);
			return true;
		} catch (Exception e)
		{
			return false;
		}
	}

	public boolean isRunning()
	{
		return running;
	}

	public void run()
	{
		execute();
		running = false;
		// START KGU#117/KGU#156 2016-03-13: Enh. #77 + #124
		// It is utterly annoying when in run data mode the control always 
		// closes after execution.
		control.setVisible(false);
		// START KGU#157 2016-03-16: Bugfix #131 - postponed Control start?
		boolean reopen = false;
		if (this.reopenFor != null)
		{
			this.diagram = this.reopenFor;
			this.reopenFor = null;
			reopen = true;
		}
		// START KGU#117/KGU#156 2016-03-13: Enh. #77 + #124
		// It is utterly annoying when in run data mode the control always 
		// closes after execution.
		if (reopen || Element.E_COLLECTRUNTIMEDATA)
		{
			control.init();
			control.validate();
			control.setVisible(true);
			control.repaint();
		}
		// END KGU#117/KGU#156
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(int aDelay)
	{
		// START KGU#97 2015-12-20: Enh.Req. #48: Only inform if it's worth
		boolean delayChanged = aDelay != delay;
		// END KGU#97 2015-12-10
		delay = aDelay;
		// START KGU#97 2015-12-10: Enh.Req. #48: Inform a delay-aware DiaramController A.S.A.P.
		if (delayChanged && 
				diagramController != null &&
				diagramController instanceof DelayableDiagramController)
		{
			((DelayableDiagramController) diagramController).setAnimationDelay(aDelay);
		}
		// END KGU#97 2015-12-20
	}

	/*
	 * ORIGINAL VERSION, NOT MODIFIED BY gdonnarumma
	 */

	/*
	 * private void setVar(String name, Object content) throws EvalError {
	 * //interpreter.set(name,content);
	 * 
	 * if(content instanceof String) { if(!isNumeric((String) content)) {
	 * content = "\""+ ((String) content) + "\""; } }
	 * 
	 * interpreter.set(name,content); interpreter.eval(name+" = "+content);
	 * variables.addIfNew(name);
	 * 
	 * if(delay!=0) { Vector<Vector> vars = new Vector<Vector>(); for(int
	 * i=0;i<variables.count();i++) { Vector myVar = new Vector();
	 * myVar.add(variables.get(i));
	 * myVar.add(interpreter.get(variables.get(i))); vars.add(myVar); }
	 * control.updateVars(vars); }
	 * 
	 * }
	 */

	/**
	 * @param aPaus
	 *            the step to set
	 */
	public void setPaus(boolean aPaus)
	{
		// START KGU 2015-10-13: In "turbo" mode, too, we want to see were the algorithm is hovering.
		if (delay == 0)
		{
			diagram.redraw();
			try {
				updateVariableDisplay();
			}
			catch (EvalError e)
			{
			}
		}
		// END KGU 2015-10-13
		synchronized (this)
		{
			paus = aPaus;
			if (paus == false)
			{
				step = false;
			}
			this.notify();
		}
	}

	/**
	 * @param aStop
	 *            the stop to set
	 */
	public void setStop(boolean aStop)
	{
		diagram.clearExecutionStatus();
		synchronized (this)
		{
			stop = aStop;
			paus = false;
			step = false;
			this.notify();
		}
	}

	
	// START KGU#67/KGU#68/KGU#69 2015-11-08: We must distinguish between raw input and evaluated objects
	private void setVarRaw(String name, String rawInput) throws EvalError
	{
		// first add as string (lest we should end with nothing at all...)
		// START KGU#109 2015-12-15: Bugfix #61: Previously declared (typed) variables caused errors here
		//setVar(name, rawInput);
		try {
			setVar(name, rawInput);
		}
		catch (EvalError ex)
		{
			System.out.println(rawInput + " as raw string " + ex.getMessage());			
		}
		// END KGU#109 2015-12-15
		// Try some refinement if possible
		if (rawInput != null && !isNumeric(rawInput) )
		{
			try
			{
				String strInput = rawInput.trim();
				// Maybe the string or character is already quoted, then get the content
				if (strInput.startsWith("\"") && strInput.endsWith("\"") ||
						strInput.startsWith("'") && strInput.endsWith("'"))
				{
					this.interpreter.eval(name + " = " + rawInput);
				}
				// try adding as char (only if it's not a digit)
				else if (rawInput.length() == 1)
				{
					Character charInput = rawInput.charAt(0);
					setVar(name, charInput);
				}
				// START KGU#184 2016-04-25: Enh. #174 - accept array initialisations on input
				else if (rawInput.startsWith("{") && rawInput.endsWith("}"))
				{
					String asgnmt = "Object[] " + name + " = " + rawInput;
					// Nested initializers won't work here!
					interpreter.eval(asgnmt);
					setVar(name, interpreter.get(name));
				}
				// END KGU#184 2016-04-25
			}
			catch (Exception ex)
			{
				System.out.println(rawInput + " as string/char: " + ex.getMessage());
			}
		}
		// try adding as double
		try
		{
			double dblInput = Double.parseDouble(rawInput);
			setVar(name, dblInput);
		} catch (Exception ex)
		{
			//System.out.println(rawInput + " as double: " + ex.getMessage());
		}
		// finally try adding as integer
		try
		{
			int intInput = Integer.parseInt(rawInput);
			setVar(name, intInput);
		} catch (Exception ex)
		{
			//System.out.println(rawInput + " as int: " + ex.getMessage());
		}
	}
	
	// METHOD MODIFIED BY GENNARO DONNARUMMA and revised by Kay Gürtzig
	private void setVar(String name, Object content) throws EvalError

	{
		// START KGU#69 2015-11-09: This is only a good idea in case of raw input
		//if (content instanceof String)
		//{
		//	if (!isNumeric((String) content))
		//	{
		//		content = "\"" + (String) content + "\"";
		//	}
		//}
		// END KGU#69 2015-11-08

		// MODIFIED BY GENNARO DONNARUMMA

		// START KGU#141 2016-01-16: Bugfix #112 - this spoiled many index expressions!
		// No idea what this might have been intended for - enclosing parentheses after input?
		//if ((name != null) && (name.contains("(")))
		//{
		//	name = name.replace("(", "");
		//}
		//if ((name != null) && (name.contains(")")))
		//{
		//	name = name.replace(")", "");
		//}
		// END KGU#141 2016-01-16

		// MODIFIED BY GENNARO DONNARUMMA, ARRAY SUPPORT ADDED
		// Fundamentally revised by Kay Gürtzig 2015-11-08

		String arrayname = null;
		if ((name.contains("[")) && (name.contains("]")))
		{
			arrayname = name.substring(0, name.indexOf("["));
			// START KGU#109 2015-12-16: Bugfix #61: Several strings suggest type specifiers
			String[] nameParts = arrayname.split(" ");
			arrayname = nameParts[nameParts.length-1];
			// END KGU#109 2015-12-15
			boolean arrayFound = this.variables.contains(arrayname);
			int index = this.getIndexValue(name);
			Object[] objectArray = null;
			int oldSize = 0;
			if (arrayFound)
			{
				try {
					// If it hasn't been an array then we'll get an error here
					objectArray = (Object[]) this.interpreter.get(arrayname);
					oldSize = objectArray.length;
				}
				catch (Exception ex)
				{
					// Produce a meaningful EvalError instead
					this.interpreter.eval(arrayname + "[" + index + "] = " + prepareValueForDisplay(content));
				}
			}
			if (index > oldSize - 1) // This includes the case of oldSize = 0
			{
				Object[] oldObjectArray = objectArray;
				objectArray = new Object[index + 1];
				for (int i = 0; i < oldSize; i++)
				{
					objectArray[i] = oldObjectArray[i];
				}
				for (int i = oldSize; i < index; i++)
				{
					objectArray[i] = new Integer(0);
				}
			}
			objectArray[index] = content;
			this.interpreter.set(arrayname, objectArray);
			this.variables.addIfNew(arrayname);
		} else // if ((name.contains("[")) && (name.contains("]")))
		{
			// START KGU#109 2015-12-16: Bugfix #61: Several strings suggest type specifiers
			// START KGU#109 2016-01-15: Bugfix #61,#107: There might also be a colon...
			int colonPos = name.indexOf(":");	// Check Pascal and BASIC style as well
			if (colonPos > 0 || (colonPos = name.indexOf(" as ")) > 0)
			{
				name = name.substring(0, colonPos).trim();
			}
			// END KGU#109 2016-01-15
			String[] nameParts = name.split(" ");
			name = nameParts[nameParts.length-1];
			// END KGU#109 2015-12-15
			this.interpreter.set(name, content);

			// MODIFIED BY GENNARO DONNARUMMA
			// PREVENTING DAMAGED STRING AND CHARS
			// FIXME (KGU): Seems superfluous or even dangerous (Addendum 2015-12-10: Now the aim became clear by issue #49)
//			if (content != null)
//			{
//				if (content instanceof String)
//				{
//					content = ((String) content).replaceAll("\"\"", "\"");
//				}
//				else if (content instanceof Character)
//				{
//					content = new String("'" + content + "'");
//				}
//			}
//			this.interpreter.eval(name + " = " + content);	// What the heck is this good for, now?
			// START KGU#99 2015-12-10: Bugfix #49 - for later comparison etc. we try to replace wrapper objects by simple values
			if (! (content instanceof String || content instanceof Character || content instanceof Object[]))
			{
				try {
					this.interpreter.eval(name + " = " + content);	// Avoid the variable content to be an object
				}
				catch (EvalError ex)	// Just ignore an error (if we may rely on the previously set content to survive)
				{}
			}
			// END KGU#99 2015-12-10
			this.variables.addIfNew(name);
		}
		
		// START KGU#20 2015-10-13: In step mode, variable display should be updated even if delay is set to 0
//		if (this.delay != 0)
//		{
//			Vector<Vector> vars = new Vector();
//			for (int i = 0; i < this.variables.count(); i++)
//
//			{
//				Vector myVar = new Vector();
//				myVar.add(this.variables.get(i));
//				myVar.add(this.interpreter.get(this.variables.get(i)));
//				vars.add(myVar);
//			}
//			this.control.updateVars(vars);
//		}
		
		if (this.delay != 0 || step)
		{
			updateVariableDisplay();
		}
		// END KGU#20 2015-10-13
	}

	// START KGU#20 2015-10-13: Code from above moved hitherto and formed to a method
	/**
	 * Prepares an editable variable table and has the Control update the display
	 * of variables with it
	 */
	private void updateVariableDisplay() throws EvalError
	{
		Vector<Vector<Object>> vars = new Vector<Vector<Object>>();
		for (int i = 0; i < this.variables.count(); i++)
		{
			Vector<Object> myVar = new Vector<Object>();
			myVar.add(this.variables.get(i));	// Variable name
			// START KGU#67 2015-11-08: We had to find a solution for displaying arrays in a sensible way
			//myVar.add(this.interpreter.get(this.variables.get(i)));
			Object val = this.interpreter.get(this.variables.get(i));
			String valStr = prepareValueForDisplay(val);
			myVar.add(valStr);					// Variable value as string
			// END KGU#67 2015-11-08
			vars.add(myVar);
		}
		this.control.updateVars(vars);
		// START KGU#2 (#9) 2015-11-14
		this.control.updateCallLevel(this.callers.size());
		// END#2 (#9) KGU 2015-11-14
	}
	// END KGU#20 2015-10-13
	
	// START KGU#67/KGU#68 2015-11-08: We have to present values in an editable way (recursively!)
	private String prepareValueForDisplay(Object val)
	{
		String valStr = "";
		if (val != null)
		{
			valStr = val.toString();
			if (val.getClass().getSimpleName().equals("Object[]"))
			{
				valStr = "{";
				Object[] valArray = (Object[]) val;
				for (int j = 0; j < valArray.length; j++)
				{
					String elementStr = prepareValueForDisplay(valArray[j]);
					valStr = valStr + ((j > 0) ? ", " : "") + elementStr;
				}
				valStr = valStr + "}";
			}
			else if (val instanceof String)
			{
				valStr = "\"" + valStr + "\"";
			}
			else if (val instanceof Character)
			{
				valStr = "'" + valStr + "'";
			}
		}
		return valStr;
	}
	// END KGU#67/KGU#68 2015-11-08
	
	// START KGU#68 2015-11-06
	public void adoptVarChanges(Object[] newValues)
	{
		String tmplManuallySet = control.lbManuallySet.getText();	// The message template
		for (int i = 0; i < newValues.length; i++)
		{
			if (newValues[i] != null)
			{
				try {
					String varName = this.variables.get(i);
					Object oldValue = interpreter.get(varName);
					// START KGU#160 2016-04-12: Enh. #137 - text window output
					// START KGU#197 2016-05-05: Language support extended
					//this.console.writeln("*** Manually set: " + varName + " <- " + newValues[i] + " ***", Color.RED);
					this.console.writeln(tmplManuallySet.replace("%1", varName).replace("%2", newValues[i].toString()), Color.RED);
					// END KGU#197 2016-05-05
					if (isConsoleEnabled)
					{
						this.console.setVisible(true);
					}
					// END KGU#160 2016-04-12
					
					if (oldValue != null && oldValue.getClass().getSimpleName().equals("Object[]"))
					{
						// In this case an initialisation expression ("{ ..., ..., ...}") is expected
						String asgnmt = "Object[] " + varName + " = " + newValues[i];
						//System.out.println(asgnmt);	// FIXME (KGU) Remove this debug info after test
						// FIXME: Nested initializers (as produced for nested arrays before) won't work here!
						interpreter.eval(asgnmt);
//						// Okay, but now we have to sort out some un-boxed strings
//						Object[] objectArray = (Object[]) interpreter.get(varName);
//						for (int j = 0; j < objectArray.length; j++)
//						{
//							Object content = objectArray[j];
//							if (content != null)
//							{
//								System.out.println("Updating " + varName + "[" + j + "] = " + content.toString());
//								this.interpreter.set("structorizer_temp", content);
//								this.interpreter.eval(varName + "[" + j + "] = structorizer_temp");
//							}
//						}
						
					}
					else
					{
						//System.out.println(varName + " = " + (String)newValues[i]);	// FIXME(KGU) Remove this debug info after test
						setVarRaw(varName, (String)newValues[i]);
					}
				}
				catch (EvalError err) {
					System.err.println(err.getMessage());
				}
			}
		}
	}
	// END KGU#68 2015-11-06

	public void start(boolean useSteps)
	{
		paus = useSteps;
		step = useSteps;
		stop = false;
		variables = new StringList();
		control.updateVars(new Vector<Vector<Object>>());
		
		running = true;
		Thread runner = new Thread(this, "Player");
		runner.start();
	}
	
	// START KGU#43 2015-10-12 New method for breakpoint support
	private boolean checkBreakpoint(Element element)
	{
		// START KGU#213 2016-08-01: Enh. #215
		//boolean atBreakpoint = element.isBreakpoint();
		boolean atBreakpoint = element.triggersBreakNow();
		// END KGU#213 2016-08-01
		if (atBreakpoint) {
			control.setButtonsForPause();
			this.setPaus(true);
		}
		return atBreakpoint;
	}
	// END KGU#43 2015-10-12

	// START KGU 2015-10-13: Decomposed this "monster" method into Element-type-specific subroutines
	private String step(Element element)
	{
		String result = new String();
		element.executed = true;
		if (delay != 0 || step)
		{
			diagram.redraw();
		}
		// START KGU#143 2016-01-21: Bugfix #114 - make sure no compromising editing is done
		diagram.doButtons();
		// END KGU#143 2016-01-21
		// START KGU#43 2015-10-12: If there is a breakpoint switch to step mode before delay
		checkBreakpoint(element);
		// END KGU#43 2015-10-12
		
		// The Root element and the REPEAT loop won't be delayed or halted in the beginning except by their members
		if (element instanceof Root)
		{
			result = stepRoot((Root)element);
		} else if (element instanceof Repeat)
		{
			result = stepRepeat((Repeat)element);
		}
		else 
		{
			// Delay or wait (in case of step mode or breakpoint) before
			delay();	// does the delaying or waits in case of step mode or breakpoint
			
			// START KGU#2 2015-11-14: Separate execution for CALL elements to keep things clearer
			//if (element instanceof Instruction)
			if (element instanceof Call)
			{
				result = stepCall((Call)element);
			}
			// START KGU#78 2015-11-25: Separate handling of JUMP instructions
			else if (element instanceof Jump)
			{
				result = stepJump((Jump)element);
			}
			// END KGU#78 2015-11-25
			else if (element instanceof Instruction)
			// END KGU#2 2015-11-14
			{
				result = stepInstruction((Instruction)element);
			} else if (element instanceof Case)
			{
				result = stepCase((Case)element);
			} else if (element instanceof Alternative)
			{
				result = stepAlternative((Alternative)element);
			} else if (element instanceof While)
			{
				result = stepWhile(element, false);
			} else if (element instanceof For)
			{
				result = stepFor((For)element);
			}
			// START KGU#44/KGU#47 2015-10-13: Obviously, Forever loops and Parallel sections had been forgotten
			else if (element instanceof Forever)
			{
				result = stepWhile(element, true);
			}
			else if (element instanceof Parallel)
			{
				result = stepParallel((Parallel)element);
			}
			// END KGU#44/KGU#47 2015-10-13
		}
		if (result.equals("")) {
			element.executed = false;
			// START KGU#117 2016-03-07: Enh. #77
			element.checkTestCoverage(false);
			// END KGU#117 2016-03-07
			// START KGU#156 2016-03-11: Enh. #124
			// Increment the execution counters
			element.countExecution();
			// END KGU#156 2016-03-11
		}
		return result;
	}

	private String stepRoot(Root element)
	{
		// KGU 2015-11-25: Was very annoying to wait here in step mode
		// and we MUST NOT re-initialise the Turtleizer on a subroutine!
		if ((diagramController != null || !step) && callers.isEmpty())
		{
			getExec("init(" + delay + ")");
		}

		element.waited = true;

		// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
//		String result = new String();
//		int i = 0;
//		// START KGU#77 2015-11-11: Leave if a return statement has been executed
//		//while ((i < element.children.children.size())
//		//		&& result.equals("") && (stop == false))
//		while ((i < element.children.getSize())
//				&& result.equals("") && (stop == false) && !returned)
//		// END KGU#77 2015-11-11
//		{
//			result = step(element.children.getElement(i));
//			i++;
//		}
		String result = stepSubqueue(element.children, false);
		// END KGU#117 2016-03-07

		// START KGU#148 2016-01-29: Moved to execute
//		delay(); // FIXME Specific pause for root after the last instruction of the program/function
//		if (result.equals(""))
//		{
//			element.clearExecutionStatus();
//		}
		// END KGU#148 2016-01-29
		return result;
	}

	private String stepInstruction(Instruction element)
	{
		String result = new String();

		StringList sl = element.getText();
		int i = 0;

		// START KGU#77/KGU#78 2015-11-25: Leave if some kind of leave statement has been executed
		//while ((i < sl.count()) && result.equals("") && (stop == false))
		while ((i < sl.count()) && result.equals("") && (stop == false) &&
				!returned && leave == 0)
		// END KGU#77/KGU#78 2015-11-25
		{
			String cmd = sl.get(i);
			// cmd=cmd.replace(":=", "<-");
			cmd = convert(cmd).trim();
			try
			{
				// START KGU 2015-10-12: Allow to step within an instruction block (but no breakpoint here!) 
				if (i > 0)
				{
					delay();
				}
				// END KGU 2015-10-12
				
				// assignment
				if (cmd.indexOf("<-") >= 0)
				{
					result = tryAssignment(cmd, element);
				}
				// input
				// START KGU#65 2015-11-04: Input keyword should only trigger this if positioned at line start
				//else if (cmd.indexOf(D7Parser.input) >= 0)
				else if (cmd.matches(
						this.getKeywordPattern(D7Parser.input) + "([\\W].*|$)"))
				// END KGU#65 2015-11-04
				{
					result = tryInput(cmd);
				}
				// output
				// START KGU#65 2015-11-04: Output keyword should only trigger this if positioned at line start
				//else if (cmd.indexOf(D7Parser.output) >= 0)
				else if (cmd.matches(
						this.getKeywordPattern(D7Parser.output) + "([\\W].*|$)"))
				// END KGU#65 2015-11-04
				{
					result = tryOutput(cmd);
				}
				// return statement
				// START KGU 2015-11-28: The "return" keyword ought to be the first word of the instruction,
				// comparison should not be case-sensitive while D7Parser.preReturn isn't fully configurable,
				// but a separator would be fine...
				//else if (cmd.indexOf("return") >= 0)
				else if (cmd.matches(
						this.getKeywordPattern(D7Parser.preReturn) + "([\\W].*|$)"))
				// END KGU 2015-11-11
				{		 
					result = tryReturn(cmd.trim());
				}
				else
				{
					result = trySubroutine(cmd, element);
				}
				// START KGU#156 2016-03-11: Enh. #124
				element.addToExecTotalCount(1, true);	// For the instruction line
				//END KGU#156 2016-03-11
			} catch (EvalError ex)
			{
				result = ex.getLocalizedMessage();
				if (result == null) result = ex.getMessage();
			}
			i++;
			// Among the lines of a single instruction element there is no further breakpoint check!
		}
		if (result.equals(""))
		{
			element.executed = false;
		}
		return result;
	}
	
	// START KGU#2 2015-11-14: Separate dedicated implementation for "foreign calls"
	private String stepCall(Call element)
	{
		String result = new String();

		StringList sl = element.getText();
		int i = 0;

		// START KGU#117 2016-03-10: Enh. #77
		boolean wasSimplyCovered = element.simplyCovered;
		boolean wasDeeplyCovered = element.deeplyCovered;
		boolean allSubroutinesCovered = true;
		// END KGU#117 2016-03-10

		// START KGU#77 2015-11-11: Leave if a return statement has been executed
		//while ((i < sl.count()) && result.equals("") && (stop == false))
		while ((i < sl.count()) && result.equals("") && (stop == false) && !returned)
		// END KGU#77 2015-11-11
		{
			String cmd = sl.get(i);
			// cmd=cmd.replace(":=", "<-");
			cmd = convert(cmd);

			try
			{
				// START KGU#117 2016-03-08: Enh. #77
				element.deeplyCovered = false;
				// END KGU#117 2016-03-08

				// START KGU 2015-10-12: Allow to step within an instruction block (but no breakpoint here!) 
				if (i > 0)
				{
					delay();
				}
				// END KGU 2015-10-12
				
				// assignment
				if (cmd.indexOf("<-") >= 0)
				{
					result = tryAssignment(cmd, element);
				}
				else
				{
					result = trySubroutine(cmd, element);
				}
				
				// START KGU#117 2016-03-08: Enh. #77
				allSubroutinesCovered = allSubroutinesCovered && element.deeplyCovered;
				// END KGU#117 2016-03-08
			} catch (EvalError ex)
			{
				result = ex.getLocalizedMessage();
				if (result == null) result = ex.getMessage();
			}

			i++;
			// Among the lines of a single instruction element there is no further breakpoint check!
		}
		if (result.equals(""))
		{
			element.executed = false;
			// START KGU#117 2016-03-08: Enh. #77
			element.simplyCovered = true;	// (Should already have been set)
			element.deeplyCovered = wasDeeplyCovered || allSubroutinesCovered;
			if (!wasDeeplyCovered && allSubroutinesCovered ||
					!wasSimplyCovered)
			{
				element.checkTestCoverage(true);
			}
			// END KGU#117 2016-03-08
		}
		return result;
	}
	// END KGU#2 2015-11-14

	// START KGU#78 2015-11-25: Separate dedicated implementation for JUMPS
	private String stepJump(Jump element)
	{
		String result = new String();

		StringList sl = element.getText();
		int i = 0;
		boolean done = false;

		// START KGU#127 2016-01-07: Bugfix #91 - without a single line the exit didn't work
		if (sl.count() == 0)
		{
			done = true;
			this.leave++;
		}
		// END KGU#127 2016-01-07
		while ((i < sl.count()) && !done && result.equals("") && (stop == false) && !returned)
		{
			String cmd = sl.get(i).trim();
			StringList tokens = Element.splitLexically(cmd.toLowerCase(), true);
			tokens.removeAll(" ");
			try
			{
				boolean startsWithLeave = tokens.indexOf(D7Parser.preLeave, !D7Parser.ignoreCase) == 0;
				// Single-level break? (An empty Jump is also a break!)
				if (startsWithLeave && tokens.count() == 1 ||
						cmd.isEmpty() && i == sl.count() - 1)
				{
					this.leave++;
					done = true;
				}
				// Multi-level leave?
				else if (startsWithLeave)
				{
					int nLevels = 1;
					if (tokens.count() > 1)
					{
						// START KGU#252 2016-09-22: Issue #248 - Java 7 workaround
						String errorMessage = null;
						// END KGU#252 2016-09-22
						try {
							// START KGU#252 2016-09-22: Issue #248 - Java 7 workaround
							//nLevels = Integer.parseUnsignedInt(tokens.get(1));
							nLevels = Integer.parseInt(tokens.get(1));
							if (nLevels <= 0)
							{
								errorMessage = tokens.get(1) + " < 1";
							}
							// END KGU#252 2016-09-22
						}
						catch (NumberFormatException ex)
						{
							// START KGU#197 2016-07-27: Localization support (updated 2016-09-17)
							//result = "Illegal leave argument: " + ex.getMessage();
							errorMessage = ex.getLocalizedMessage();
							if (errorMessage == null) errorMessage = ex.getMessage();
							errorMessage = ex.getClass().getSimpleName() + " " + errorMessage;
							// START KGU#252 2016-09-22: Issue #248: Java 7 workaround
							//result = control.msgIllegalLeave.getText().replace("%1", errorMessage);
							// END KGU#252 2016-09-22
							// END KGU#197 2016-07-27
						}
						// START KGU#252 2016-09-22: Issue #248: Java 7 workaround
						if (errorMessage != null) {
							result = control.msgIllegalLeave.getText().replace("%1", errorMessage);
						}
						// END KGU#252 2016-09-22
					}
					this.leave += nLevels;
					done = true;
				}
				// Unstructured return from the routine?
				else if (tokens.indexOf(D7Parser.preReturn, !D7Parser.ignoreCase) == 0)
				{
					result = tryReturn(convert(sl.get(i)));
					done = true;
				}
				// Exit from the entire program - simply handled like an error here.
				else if (tokens.indexOf(D7Parser.preExit, !D7Parser.ignoreCase) == 0)
				{
					int exitValue = 0;
					try {
						
						Object n = interpreter.eval(tokens.get(1));
						if (n instanceof Integer)
						{
							exitValue = ((Integer) n).intValue();
						}
						else
						{
							// START KGU#197 2016-07-27: More localization support
							//result = "Inappropriate exit value: <" + (n == null ? tokens.get(1) : n.toString()) + ">";
							result = control.msgWrongExit.getText().replace("%1",
									"<" + (n == null ? tokens.get(1) : n.toString()) + ">");
							// END KGU#197 2016-07-27
						}
					}
					catch (EvalError ex)
					{
						// START KGU#197 2016-07-27: More localization support (Updated 32016-09-17)
						//result = "Wrong exit value: " + ex.getMessage();
						String exMessage = ex.getLocalizedMessage();
						if (exMessage == null) exMessage = ex.getMessage();
						result = control.msgWrongExit.getText().replace("%1", exMessage);
						// END KGU#197 2016-07-27
					}
					if (result.isEmpty())
					{
						// START KGU#197 2016-07-27: More localization support
						//result = "Program exited with code " + exitValue + "!";
						result = control.msgExitCode.getText().replace("%1",
								Integer.toString(exitValue));
						// END KGU#197 2016-07-27
						// START KGU#117 2016-03-07: Enh. #77
						element.checkTestCoverage(true);
						// END KGU#117 2016-03-07
					}
					done = true;
				}
				// Anything else is an error
				else if (!cmd.isEmpty())
				{
					// START KGU#197 2016-07-27: More localization support
					//result = "Illegal content of a Jump (i.e. exit) instruction: <" + cmd + ">!";
					result = control.msgIllegalJump.getText().replace("%1", cmd);
					// END KGU#197 2016-07-27
				}
			} catch (Exception ex)
			{
				result = ex.getLocalizedMessage();
				if (result == null) result = ex.getMessage();
			}
			i++;
		}
		if (done && leave > loopDepth)
		{
			// START KGU#197 2016-07-27: More localization support
			result = "Too many levels to leave (actual depth: " + loopDepth + " / specified: " + leave + ")!";
			result = control.msgTooManyLevels.getText().
					replace("%1", Integer.toString(loopDepth)).
					replace("%2", Integer.toString(leave));
			// END KGU#197 2016-07-27
		}			
		if (result.equals(""))
		{
			// START KGU#156 2016-03-11: Enh. #124
			element.addToExecTotalCount(1, true);	// For the jump
			//END KGU#156 2016-03-11
			element.executed = false;
		}
		return result;
	}
	// END KGU#78 2015-11-25
	
	// START KGU 2015-11-11: Equivalent decomposition of method stepInstruction
	// Submethod of stepInstruction(Instruction element), handling an assignment
	private String tryAssignment(String cmd, Instruction instr) throws EvalError
	{
		String result = "";
		Object value = null;
		// KGU#2: In case of a Call element, we allow an assignment with just the subroutine call on the
		// right-hand side. This makes it relatively easy to detect and prepare the very subroutine call,
		// in contrast to possible occurrences of such foreign function calls at arbitrary expression depths,
		// combined, nested etc.
		String varName = cmd.substring(0, cmd.indexOf("<-")).trim();
		String expression = cmd.substring(
				cmd.indexOf("<-") + 2, cmd.length()).trim();
		// START KGU#2 2015-10-18: cross-NSD subroutine execution?
		if (instr instanceof Call)
		{
			Function f = new Function(expression);
			if (f.isFunction())
			{
				//System.out.println("Looking for SUBROUTINE NSD:");
				//System.out.println("--> " + f.getName() + " (" + f.paramCount() + " parameters)");
				Root sub = this.findSubroutineWithSignature(f.getName(), f.paramCount());
				if (sub != null)
				{
					Object[] args = new Object[f.paramCount()];
					for (int p = 0; p < f.paramCount(); p++)
					{
						args[p] = interpreter.eval(f.getParam(p));
					}
					value = executeCall(sub, args, (Call)instr);
					// START KGU#117 2016-03-10: Enh. #77
					if (Element.E_COLLECTRUNTIMEDATA)
					{
						instr.simplyCovered = true;
					}
					// END KGU#117 2016-03-10
				}
				else
				{
					// START KGU#197 2016-07-27: Now translatable
					//result = "A function diagram " + f.getName() + " (" + f.paramCount() + 
					//		" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";
					result = control.msgNoSubroutine.getText().
							replace("%1", f.getName()).
							replace("%2", Integer.toString(f.paramCount())).
							replace("%0", "\n");
					// END KGU#197 2016-07-27
				}
			}
			else
			{
				// START KGU#197 2016-07-27: Now translatable
				//result = "<" + expression + "> is not a correct function!";
				result = control.msgIllFunction.getText().replace("%1", expression);
				// END KGU#197 2016-07-27
			}
		}
		// END KGU#2 2015-10-17
		// evaluate the expression
		// START KGU#100 2016-01-14: Enh. #84 - accept array assignments with syntax array <- {val1, val2, ..., valN}
		else if (expression.startsWith("{") && expression.endsWith("}"))
		{
			interpreter.eval("Object[] tmp20160114kgu = " + expression);
			value = interpreter.get("tmp20160114kgu");
			interpreter.unset("tmp20160114kgu");
		}
		// END KGU#100 2016-01-14
		else		
		{
			//cmd = cmd.replace("<-", "=");
		
			value = interpreter.eval(expression);
		}
		
		if (value != null)
		{
			setVar(varName, value);
		}
		// START KGU#2 2015-11-24: In case of an already detected problem don't fill the result
		//else if (result.isEmpty())
		else if (result.isEmpty() && !stop)
		// END KGU#2 2015-11-24
		{
			// START KGU#197 2016-07-27: Localization support
			//result = "<"
			//		+ expression
			//		+ "> is not a correct or existing expression.";
			result = control.msgInvalidExpr.getText().replace("%1", expression);
			// END KGU#197 2016-07-27
		}

		return result;
		
	}
	
	// Submethod of stepInstruction(Instruction element), handling an input instruction
	private String tryInput(String cmd) throws EvalError
	{
		String result = "";
		String in = cmd.substring(D7Parser.input.trim().length()).trim();
		// START KGU#107 2015-12-13: Enh-/bug #51: Handle empty input instruction
		if (in.isEmpty())
		{
			// In run mode, give the user a chance to intervene
			Object[] options = {"OK", "Pause"};	// FIXME: Provide a translation
			int pressed = JOptionPane.showOptionDialog(diagram, control.lbAcknowledge.getText(), control.lbInput.getText(),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
			if (pressed == 1)
			{
				paus = true;
				step = true;
				control.setButtonsForPause();
			}
		}
		else
		{
		// END KGU#107 2015-12-13
			// START KGU#141 2016-01-16: Bugfix #112 - setVar won't eliminate enclosing paranetheses anymore
			while (in.startsWith("(") && in.endsWith(")"))
			{
				in = in.substring(1, in.length()-1).trim();
			}
			// END KGU#141 2016-01-16
			// START KGU#33 2014-12-05: We ought to show the index value
			// if the variable is indeed an array element
			if (in.contains("[") && in.contains("]")) {
				try {
					// Try to replace the index expression by its current value
					int index = getIndexValue(in);
					in = in.substring(0, in.indexOf('[')+1) + index
							+ in.substring(in.indexOf(']'));
				}
				catch (Exception e)
				{
					// START KGU#141 2016-01-16: We MUST raise the error here.
					result = e.getLocalizedMessage();
					if (result == null) result = e.getMessage();
					// END KGU#141 2016-01-16
				}
			}
			// END KGU#33 2014-12-05
			// START KGU#141 2016-01-16: Bugfix #112 - nothing more to do than exiting
			if (!result.isEmpty())
			{
				return result;
			}
			// END KGU#141 2016-01-16
			// START KGU#89 2016-03-18: More language support 
			//String str = JOptionPane.showInputDialog(null,
			//		"Please enter a value for <" + in + ">", null);
			String msg = control.lbInputValue.getText();
			msg = msg.replace("%", in);
			// START KGU#160 2016-04-12: Enh. #137 - text window output
			this.console.write(msg + ": ", Color.YELLOW);
			if (isConsoleEnabled)
			{
				this.console.setVisible(true);
			}
			// END KGU#160 2016-04-12
			String str = JOptionPane.showInputDialog(null, msg, null);
			// END KGU#89 2016-03-18
			// START KGU#84 2015-11-23: ER #36 - Allow a controlled continuation on cancelled input
			//setVarRaw(in, str);
			if (str == null)
			{
				// Switch to step mode such that the user may enter the variable in the display and go on
				// START KGU#197 2016-05-05: Issue #89
				//JOptionPane.showMessageDialog(diagram, "Execution paused - you may enter the value in the variable display.",
				//		"Input cancelled", JOptionPane.WARNING_MESSAGE);
				JOptionPane.showMessageDialog(diagram, control.lbInputPaused.getText(),
						control.lbInputCancelled.getText(), JOptionPane.WARNING_MESSAGE);
				// START KGU#197 2016-05-05
				paus = true;
				step = true;
				this.control.setButtonsForPause();
				if (!variables.contains(in))
				{
					// If the variable hasn't been used before, we must create it now
					setVar(in, null);
				}
			}
			else
			{
				// START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
				this.console.writeln(str, Color.GREEN);
				if (isConsoleEnabled)
				{
					this.console.setVisible(true);
				}
				// END KGU#160 2016-04-12
				// START KGU#69 2015-11-08: Use specific method for raw input
				setVarRaw(in, str);
				// END KGU#69 2015-11-08
			}
			// END KGU#84 2015-11-23
		// START KGU#107 2015-12-13: Enh./bug #51 part 2
		}
		// END KGU#107 2015-12-13
		
		return result;
	}

	// Submethod of stepInstruction(Instruction element), handling an output instruction
	private String tryOutput(String cmd) throws EvalError
	{
		String result = "";
		// KGU 2015-12-11: Instruction is supposed to start with the output keyword!
		String out = cmd.substring(/*cmd.indexOf(D7Parser.output) +*/
						D7Parser.output.trim().length()).trim();
		String str = "";
		// START KGU#107 2015-12-13: Enh-/bug #51: Handle empty output instruction
		if (!out.isEmpty())
		{
		// END KGU#107 2015-12-13
		// START KGU#101 2015-12-11: Fix #54 - Allow several expressions to be output in a line
			StringList outExpressions = Element.splitExpressionList(out, ",");
			for (int i = 0; i < outExpressions.count() && result.isEmpty(); i++)
			{
				out = outExpressions.get(i);
		// END KGU#101 2015-12-11
				Object n = interpreter.eval(out);
				if (n == null)
				{
					result = control.lbNoCorrectExpr.getText().replace("%", out);
				} else
				{
		// START KGU#101 2015-12-11: Fix #54 (continued)
					//	String s = unconvert(n.toString());
					str += n.toString();
				}
			}
		// START KGU#107 2015-12-13: Enh-/bug #51: Handle empty output instruction
		}
//		else {
//			str = "(empty line)";
//		}
		// END KGU#107 2015-12-13
		if (result.isEmpty())
		{
			String s = unconvert(str.trim());	// FIXME (KGU): What the heck is this good for?
		// END KGU#101 2015-12-11
			// START KGU#84 2015-11-23: Enhancement #36 to give a chance to pause
			//JOptionPane.showMessageDialog(diagram, s, "Output",
			//		0);
			//System.out.println("running/step/paus/stop: " +
			//		running + " / " + step + " / " + paus + " / " + " / " + stop);

			// START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
			//if (step)
			this.console.writeln(s);
			// START KGU#107 2016-05-05: For the message dialog we must show something
			if (s.isEmpty())
			{
				s = "(" + control.lbEmptyLine.getText() + ")";
			}
			// END KGU#107 2016-05-05
			if (isConsoleEnabled)
			{
				this.console.setVisible(true);
			}
			else if (step)
			// END KGU#160 2016-04-12
			{
				// In step mode, there is no use to offer pausing
				JOptionPane.showMessageDialog(diagram, s, control.lbOutput.getText(),
						JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				// In run mode, give the user a chance to intervene
				Object[] options = {"OK", "Pause"};	// FIXME: Provide a translation
				int pressed = JOptionPane.showOptionDialog(diagram, s, control.lbOutput.getText(),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
				if (pressed == 1)
				{
					paus = true;
					step = true;
					control.setButtonsForPause();
				}
			}
			// END KGU#84 2015-11-23
		}
		return result;
	}

	// Submethod of stepInstruction(Instruction element), handling a return instruction
	private String tryReturn(String cmd) throws EvalError
	{
		String result = "";
		String header = control.lbReturnedResult.getText();
		String out = cmd.substring(D7Parser.preReturn.length()).trim();
		// START KGU#77 (#21) 2015-11-13: We out to allow an empty return
		//Object n = interpreter.eval(out);
		//if (n == null)
		//{
		//	result = "<"
		//			+ out
		//			+ "> is not a correct or existing expression.";
		//} else
		//{
		//	String s = unconvert(n.toString());
		//	JOptionPane.showMessageDialog(diagram, s,
		//			"Returned result", 0);
		//}
		Object resObj = null;
		if (!out.isEmpty())
		{
			resObj = interpreter.eval(out);
			// If this diagram is executed at top level then show the return value
			if (this.callers.empty())
			{
				if (resObj == null)
				{
					result = control.lbNoCorrectExpr.getText().replace("%", out);
				// START KGU#133 2016-01-29: Arrays should be presented as scrollable list
				} else if (resObj instanceof Object[])
				{
					showArray((Object[])resObj, header, !step);
				} else if (step)
				{
					// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
					this.console.writeln("*** " + header + ": " + this.prepareValueForDisplay(resObj), Color.CYAN);
					// END KGU#160 2016-04-26
					// START KGU#147 2016-01-29: This "uncoverting" copied from tryOutput() didn't make sense...
					//String s = unconvert(resObj.toString());
					//JOptionPane.showMessageDialog(diagram, s,
					//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
					JOptionPane.showMessageDialog(diagram, resObj,
							header, JOptionPane.INFORMATION_MESSAGE);
					// END KGU#147 2016-01-29					
				// END KGU#133 2016-01-29
				} else
				{
					// START KGU#198 2016-05-25: Issue #137 - also log the result to the console
					this.console.writeln("*** " + header + ": " + this.prepareValueForDisplay(resObj), Color.CYAN);
					// END KGU#198 2016-05-25
					// START KGU#84 2015-11-23: Enhancement to give a chance to pause (though of little use here)
					Object[] options = {"OK", "Pause"};		// FIXME: Provide a translation
					int pressed = JOptionPane.showOptionDialog(diagram, resObj, header,
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
					if (pressed == 1)
					{
						paus = true;
						step = true;
						control.setButtonsForPause();
					}
					// END KGU#84 2015-11-23
				}
			}
		}
		
		this.returnedValue = resObj;
		// END KGU#77 (#21) 2015-11-13
		returned = true;
		return result;
	}

	// Submethod of stepInstruction(Instruction element), handling a function call
	private String trySubroutine(String cmd, Instruction element) throws EvalError
	{
		String result = "";
		Function f = new Function(cmd);
		if (f.isFunction())
		{
			String params = new String();
			Object[] args = new Object[f.paramCount()];
			for (int p = 0; p < f.paramCount(); p++)
			{
				try
				{
					args[p] = interpreter.eval(f.getParam(p));
					if (args[p] == null)
					{
						if (!result.isEmpty())
						{
							result = result + "\n";
						}
						// START KGU#197 2016-07-27: Localization support
						//result = result + "PARAM " + (p+1) + ": <"
						//		+ f.getParam(p)
						//		+ "> is not a correct or existing expression.";
						result = result + "PARAM " + (p+1) + ": "
								+ control.msgInvalidExpr.getText().replace("%1", f.getParam(p));
						// END KGU#197 2016-07-27
					} else
					{
						params += "," + args[p].toString();
					}
				} catch (EvalError ex)
				{
					String exMessage = ex.getLocalizedMessage();
					if (exMessage == null) exMessage = ex.getMessage();
					result = result + (!result.isEmpty() ? "\n" : "") +
							"PARAM " + (p+1) + ": " + exMessage;
				}
			}
			// If this element is of class Call and the extracted function name
			// corresponds to one of the NSD diagrams currently opened then try
			// a sub-execution of that diagram.
			// START KGU#2 2015-10-17: Check foreign call
			if (result.isEmpty() && element instanceof Call)
			{
				// FIXME: Disable the output instructions for the release version
				//System.out.println("Looking for SUBROUTINE NSD:");
				//System.out.println("--> " + f.getName() + " (" + f.paramCount() + " parameters)");
				Root sub = this.findSubroutineWithSignature(f.getName(), f.paramCount());
				if (sub != null)
				{
					executeCall(sub, args, (Call)element);
					// START KGU#117 2016-03-10: Enh. #77
					if (Element.E_COLLECTRUNTIMEDATA)
					{
						element.simplyCovered = true;
					}
					// END KGU#117 2016-03-10
				}
				else
				{
					// START KGU#197 2016-07-27: Now translatable
					//result = "A subroutine diagram " + f.getName() + " (" + f.paramCount() + 
					//		" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";
					result = control.msgNoSubroutine.getText().
							replace("%1", f.getName()).
							replace("%2", Integer.toString(f.paramCount())).
							replace("%0", "\n");
					// END KGU#197 2016-07-27
				}
			}
			// END KGU#2 2015-10-17
			else if (result.isEmpty())
			{
				if (diagramController != null)
				{
					if (f.paramCount() > 0)
					{
						params = params.substring(1);
					}
					cmd = f.getName().toLowerCase() + "(" + params + ")";
					result = getExec(cmd, element.getColor());
				} else
				{
					interpreter.eval(cmd);
				}
			}
		} else
		{
			result = "<" + cmd + "> is not a correct function!";
		}
		return result;
	}
	// END KGU 2015-11-11

	private String stepCase(Case element)
	{
		String result = new String();
		try
		{
			StringList text = element.getText();
			// START KGU 2015-11-09 New unified conversion strategy ahead, so use Structorizer syntax
			//String expression = text.get(0) + "==";
			String expression = text.get(0) + " = ";
			// END KGU 2015-11-09
			boolean done = false;
			int last = text.count() - 1;
			if (text.get(last).trim().equals("%"))
			{
				last--;
			}
			// START KGU#156 2016-03-11: Enh. #124
			element.addToExecTotalCount(1, true);	// For the condition test (as if were just one comparison)
			//END KGU#156 2016-03-11
			
			for (int q = 1; (q <= last) && (done == false); q++)
			{
				// START KGU#15 2015-10-21: Support for multiple constants per branch
				//String test = convert(expression + text.get(q));
				String[] constants = text.get(q).split(",");
				// END KGU#15 2015-10-21
				boolean go = false;
				if ((q == last)
						&& !text.get(text.count() - 1).trim().equals("%"))
				{
					go = true;
				}
				if (go == false)
				{
					// START KGU#15 2015-10-21: Test against a list of constants now
					//Object n = interpreter.eval(test);
					//go = n.toString().equals("true");
					for (int c = 0; !go && c < constants.length; c++)
					{
						String test = convert(expression + constants[c]);
						Object n = interpreter.eval(test);
						go = n.toString().equals("true");
					}
					// END KGU#15 2015-10-21
				}
				if (go)
				{
					done = true;
					element.waited = true;
					// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
//					int i = 0;
//					// START KGU#78 2015-11-25: Leave if a loop exit is open
//					// START KGU#77 2015-11-11: Leave if a return statement has been executed
//					//while ((i < element.qs.get(q - 1).children.size())
//					//		&& result.equals("") && (stop == false))
//					while ((i < element.qs.get(q - 1).getSize())
//							&& result.equals("") && (stop == false) && !returned)
//					// END KGU#77 2015-11-11
//					{
//						result = step(element.qs.get(q - 1).getElement(i));
//						i++;
//					}
					if (result.isEmpty())
					{
						result = stepSubqueue(element.qs.get(q - 1), false);
					}
					// END KGU#117 2016-03-07
					if (result.equals(""))
					{
						element.waited = false;
					}
				}

			}
			if (result.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
		} catch (EvalError ex)
		{
			result = ex.getLocalizedMessage();
			if (result == null) result = ex.getMessage();
		}
		
		return result;
	}
	
	private String stepAlternative(Alternative element)
	{
		String result = new String();
		try
		{
			String s = element.getText().getText();
			// START KGU#150 2016-04-03: More precise processing
//			if (!D7Parser.preAlt.equals(""))
//			{
//				// FIXME: might damage variable names
//				s = BString.replace(s, D7Parser.preAlt, "");
//			}
//			if (!D7Parser.postAlt.equals(""))
//			{
//				// FIXME: might damage variable names
//				s = BString.replace(s, D7Parser.postAlt, "");
//			}
//
//			s = convert(s);
			StringList tokens = Element.splitLexically(s, true);
			for (String key : new String[]{D7Parser.preAlt, D7Parser.postAlt})
			{
				if (!key.trim().isEmpty())
				{
					tokens.removeAll(Element.splitLexically(key, false), !D7Parser.ignoreCase);
				}		
			}
			s = convert(tokens.concatenate());
			// END KGU#150 2016-04-03

			//System.out.println("C=  " + interpreter.get("C"));
			//System.out.println("IF: " + s);
			Object cond = interpreter.eval(s);
			//System.out.println("Res= " + n);
			if (cond == null || !(cond instanceof Boolean))
			{
				// START KGU#197 2016-07-27: Localization support
				//result = "<" + s
				//		+ "> is not a correct or existing expression.";
				result = control.msgInvalidBool.getText().replace("%1", s);
				// END KGU#197 2016-07-27
			}
			// if(getExec(s).equals("OK"))
			else 
			{
				// START KGU#156 2016-03-11: Enh. #124
				element.addToExecTotalCount(1, true);	// For the condition test
				//END KGU#156 2016-03-11
				
				Subqueue branch;
				if (cond.toString().equals("true"))
				{
					branch = element.qTrue;
				}
				else
				{
					branch = element.qFalse;
				}
				element.executed = false;
				element.waited = true;

				// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
//				int i = 0;
//				// START KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
//				//while ((i < branch.children.size())
//				//		&& result.equals("") && (stop == false))
//				while ((i < branch.getSize())
//						&& result.equals("") && (stop == false) && !returned && leave == 0)
//				// END KGU#78 2015-11-25
//				{
//					result = step(branch.getElement(i));
//					i++;
//				}
				if (result.isEmpty())
				{
					result = stepSubqueue(branch, true);
				}
				// END KGU#117 2016-03-07
				if (result.equals(""))
				{
					element.waited = false;
				}
			}
			if (result.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
		} catch (EvalError ex)
		{
			result = ex.getLocalizedMessage();
			if (result == null) result = ex.getMessage();
		}
		return result;
	}
	
	// This executes While and Forever loops
	private String stepWhile(Element element, boolean eternal)
	{
		String result = new String();
		try
		{
			String condStr = "true";	// Condition expression
			if (!eternal) {
				condStr = ((While) element).getText().getText();
				// START KGU#150 2016-04-03: More precise processing
//				if (!D7Parser.preWhile.equals(""))
//				{
//					// FIXME: might damage variable names
//					condStr = BString.replace(condStr, D7Parser.preWhile, "");
//				}
//				if (!D7Parser.postWhile.equals(""))
//				{
//					// FIXME: might damage variable names
//					condStr = BString.replace(condStr, D7Parser.postWhile, "");
//				}
//				// START KGU#79 2015-11-12: Forgotten zu write back the result!
//				//convert(condStr, false);
//				condStr = convert(condStr, false);
//				// END KGU#79 2015-11-12
//				// System.out.println("WHILE: "+condStr);
				StringList tokens = Element.splitLexically(condStr, true);
				for (String key : new String[]{D7Parser.preWhile, D7Parser.postWhile})
				{
					if (!key.trim().isEmpty())
					{
						tokens.removeAll(Element.splitLexically(key, false), !D7Parser.ignoreCase);
					}		
				}
				condStr = convert(tokens.concatenate());
				// END KGU#150 2016-04-03
			}

			//int cw = 0;
			Object cond = interpreter.eval(convertStringComparison(condStr));

			if (cond == null || !(cond instanceof Boolean))
			{
				// START KGU#197 2016-07-27: Localization support
				//result = "<" + condStr
				//		+ "> is not a correct or existing expression.";
				result = control.msgInvalidBool.getText().replace("%1", condStr);
				// END KGU#197 2016-07-27
			} else
			{
				// START KGU#156 2016-03-11: Enh. #124
				element.addToExecTotalCount(1, true);	// For the condition evaluation
				//END KGU#156 2016-03-11
				
				// START KGU#77/KGU#78 2015-11-25: Leave if any kind of Jump statement has been executed
				//while (cond.toString().equals("true") && result.equals("")
				//		&& (stop == false))
				loopDepth++;
				while (cond.toString().equals("true") && result.equals("")
						&& (stop == false) && !returned && leave == 0)
				// END KGU#77/KGU#78 2015-11-25
				{

					element.executed = false;
					element.waited = true;

					// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
//					int i = 0;
//					Subqueue body;
//					if (eternal)
//					{
//						body = ((Forever)element).q;
//					}
//					else
//					{
//						body = ((While) element).q;
//					}
//					// START KGU#77/KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
//					//while ((i < body.children.size())
//					//		&& result.equals("") && (stop == false))
//					while ((i < body.getSize())
//							&& result.equals("") && (stop == false) && !returned && leave == 0)
//					// END KGU#77/KGU#78 2015-11-25
//					{
//						result = step(body.getElement(i));
//						i++;
//					}
					if (result.isEmpty())
					{
						result = stepSubqueue(((ILoop)element).getBody(), true);						
					}
					// END KGU#117 2016-03-07

					// START KGU#200 2016-06-07: Body is only done if there was no error
					//element.executed = true;
					//element.waited = false;
					// END KGU#200 2016-06-07
					if (result.equals(""))
					{
						// START KGU#200 2016-06-07: If body is done without error then show loop as active again
						element.executed = true;
						element.waited = false;
						// END KGU#200 2016-06-07
						//cw++;
						// START KGU 2015-10-13: Symbolizes the loop condition check 
						checkBreakpoint(element);
						delay();
						// END KGU 2015-10-13
					}
					cond = interpreter.eval(convertStringComparison(condStr));
					if (cond == null)
					{
						// START KGU#197 2016-07-27: Localization support
						//result = "<"
						//		+ condStr
						//		+ "> is not a correct or existing expression.";
						result = control.msgInvalidExpr.getText().replace("%1", condStr);
						// END KGU#197 2016-07-27
					}
					// START KGU#156 2016-03-11: Enh. #124
					else
					{
						element.addToExecTotalCount(1, true);	// For the condition evaluation
					}
					//END KGU#156 2016-03-11			
						
				}
				// START KGU#78 2015-11-25: If there are open leave requests then nibble one off
				if (leave > 0)
				{
					leave--;
				}
				loopDepth--;
				// END KGU#78 2015-11-25
			}
			if (result.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
			/*
			 * if (cw > 1000000) { element.selected = true; result =
			 * "Your loop ran a million times. I think there is a problem!";
			 * }
			 */
		} catch (EvalError ex)
		{
			result = ex.getMessage();
		}
		return result;
	}
	
	private String stepRepeat(Repeat element)
	{
		String result = new String();
		try
		{
			element.waited = true;
			if (delay != 0 || step)
			{
				diagram.redraw();
			}

			// The exit condition is converted and parsed once in advance!
			// Hence, syntactic errors will be reported before the loop has been started at all.
			// And, of course, variables only introduced within the loop won't be recognised--
			// which is sound with scope rules in C or Java.
			String condStr = element.getText().getText();
			// STRT KGU#150 2016-04-03: More pecise processing
//			if (!D7Parser.preRepeat.equals(""))
//			{
//				// FIXME: might damage variable names
//				condStr = BString.replace(condStr, D7Parser.preRepeat, "");
//			}
//			if (!D7Parser.postRepeat.equals(""))
//			{
//				// FIXME: might damage variable names
//				condStr = BString.replace(condStr, D7Parser.postRepeat, "");
//			}
//			condStr = convert(condStr, false);
			StringList tokens = Element.splitLexically(condStr, true);
			for (String key : new String[]{D7Parser.preRepeat, D7Parser.postRepeat})
			{
				if (!key.trim().isEmpty())
				{
					tokens.removeAll(Element.splitLexically(key, false), !D7Parser.ignoreCase);
				}		
			}
			condStr = convert(tokens.concatenate());
			// END KGU#150 2016-04-03

			//int cw = 0;
			Object cond = interpreter.eval(condStr);
			if (cond == null)
			{
				// START KGU#197 2016-07-27: Localization support
				//result = "<" + condStr
				//		+ "> is not a correct or existing expression.";
				result = control.msgInvalidExpr.getText().replace("%1", condStr);
				// END KGU#197 2016-07-27
			} else
			{
				// START KGU#78 2015-11-25: In order to handle exits we must know the nesting depth
				loopDepth++;
				// END KGU#78
				do
				{
					// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
//					int i = 0;
//					// START KGU#77/KGU#78 2015-11-25: Leave if some Jump statement has been executed
//					//while ((i < element.q.children.size())
//					//		&& result.equals("") && (stop == false))
//					while ((i < element.q.getSize())
//							&& result.equals("") && (stop == false) && !returned && leave == 0)
//					// END KGU#77/KGU#78 2015-11-25
//					{
//						result = step(element.q.getElement(i));
//						i++;
//					}
					if (result.isEmpty())
					{
						result = stepSubqueue(element.getBody(), true);
					}
					// END KGU#117 2016-03-07

					if (result.equals(""))
					{
						//cw++;
						element.executed = true;
					}
					cond = interpreter.eval(convertStringComparison(condStr));
					if (cond == null || !(cond instanceof Boolean))
					{
						// START KGU#197 2016-07-27: Localization support
						//result = "<"
						//		+ condStr
						//		+ "> is not a correct or existing expression.";
						result = control.msgInvalidBool.getText().replace("%1", condStr);
						// END KGU#197 2016-07-27
					}

					// delay this element
					// START KGU 2015-10-12: This remains an important breakpoint position
					checkBreakpoint(element);
					// END KGU 2015-10-12
					element.waited = false;
					delay();	// Symbolizes the loop condition check time
					element.waited = true;

					// START KGU#156 2016-03-11: Enh. #124
					element.addToExecTotalCount(1, true);		// For the condition evaluation
					//END KGU#156 2016-03-11
					
				// START KGU#70 2015-11-09: Condition logically incorrect - execution often got stuck here 
				//} while (!(n.toString().equals("true") && result.equals("") && (stop == false)));
				// START KGU#77/KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
				//} while (!(n.toString().equals("true")) && result.equals("") && (stop == false))
				} while (!(cond.toString().equals("true")) && result.equals("") && (stop == false) &&
						!returned && leave == 0);
				// END KGU#77/KGU#78 2015-11-25
				// END KGU#70 2015-11-09
				// START KGU#78 2015-11-25: If there are open leave requests then nibble one off
				if (leave > 0)
				{
					leave--;
				}
				loopDepth--;
				// END KGU#78 2015-11-25
			}

			if (result.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
			/*
			 * if (cw > 100) { element.selected = true; result = "Problem!";
			 * }
			 */
		} catch (EvalError ex)
		{
			result = ex.getMessage();
		}
		return result;
	}
	
	private String stepFor(For element)
	{
		// START KGU#61 2016-03-21: Enh. #84
		if (element.isForInLoop())
		{
			return stepForIn(element);
		}
		// END KGU#61 2016-03-21
		String result = new String();
		try
		{
			// START KGU#3 2015-10-31: Now it's time for the new intrinsic mechanism
//			String str = element.getText().getText();
//            
//			String pas = "1";
//			if(str.contains(", pas ="))	// FIXME: Ought to be replaced by a properly configurable string
//			{
//				String[] pieces = str.split(", pas =");
//				str=pieces[0];
//				pas = pieces[1].trim();
//			}
//			// START KGU 2015-10-13: The above mechanism has/had several flaws:
//			// 1. The parsing works only for the hard-coded french keyword (ought to be a preference).
//			// 2. the while condition didn't work for negative pas values.
//			// 3. the pas value was parsed again and again in every loop.
//			// 4. It's certainly not consistent with code export
//			// To solve 2 and 3 we provide the Integer conversion once in advance
			int sval = element.getStepConst();
			// END KGU#3 2015-10-31
                            
			// START KGU#3 2015-10-27: Now replaced by For-intrinsic mechanisms
//			// cut off the start of the expression
//			if (!D7Parser.preFor.equals(""))
//			{
//				str = BString.replace(str, D7Parser.preFor, "");
//			}
//			// trim blanks
//			str = str.trim();
//			// modify the later word
//			if (!D7Parser.postFor.equals(""))
//			{
//				str = BString.replace(str, D7Parser.postFor, "<=");
//			}
//			// do other transformations
//			str = CGenerator.transform(str);
//			String counter = str.substring(0, str.indexOf("="));
			String counter = element.getCounterVar();
			// END KGU#3 2015-10-27
			// complete

			// START KGU#3 2015-10-27: Now replaced by For-intrinsic mechanisms
//			String s = str.substring(str.indexOf("=") + 1,
//					str.indexOf("<=")).trim();
			String s = element.getStartValue(); 
			// END KGU#3 2015-10-27
			s = convert(s);
			Object n = interpreter.eval(s);
			if (n == null)
			{
				// START KGU#197 2016-07-27: Localization support
				//result = "<"+s+"> is not a correct or existing expression.";
				result = control.msgInvalidExpr.getText().replace("%1", s);
				// END KGU#197 2016-07-27
			}
			int ival = 0;
			if (n instanceof Integer)
			{
				ival = (Integer) n;
			}
			else if (n instanceof Long)
			{
				ival = ((Long) n).intValue();
			}
			else if (n instanceof Float)
			{
				ival = ((Float) n).intValue();
			}
			else if (n instanceof Double)
			{
				ival = ((Double) n).intValue();
			}

			// START KGU#3 2015-10-27: Now replaced by For-intrinsic mechanisms
//			s = str.substring(str.indexOf("<=") + 2, str.length()).trim();
			s = element.getEndValue();
			// END KGU#3 2015-10-27
			s = convert(s);
			
			n = interpreter.eval(s);
			if (n == null)
			{
				// START KGU#197 2016-07-27: Localization support
				//result = "<"+s+"> is not a correct or existing expression.";
				result = control.msgInvalidExpr.getText().replace("%1", s);
				// END KGU#197 2016-07-27
			}
			int fval = 0;
			if (n instanceof Integer)
			{
				fval = (Integer) n;
			}
			else if (n instanceof Long)
			{
				fval = ((Long) n).intValue();
			}
			else if (n instanceof Float)
			{
				fval = ((Float) n).intValue();
			}
			else if (n instanceof Double)
			{
				fval = ((Double) n).intValue();
			}

			// START KGU#156 2016-03-11: Enh. #124
			element.addToExecTotalCount(1, true);	// For the initialisation and first test
			//END KGU#156 2016-03-11
			
			int cw = ival;
			// START KGU#77/KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
			//while (((sval >= 0) ? (cw <= fval) : (cw >= fval)) && result.equals("") && (stop == false))
			loopDepth++;
			while (((sval >= 0) ? (cw <= fval) : (cw >= fval)) && result.equals("") &&
					(stop == false) && !returned && leave == 0)
			// END KGU#77/KGU#78 2015-11-25
			{
				setVar(counter, cw);
				element.waited = true;


				// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
//				int i = 0;
//				// START KGU#77/KGU#78 2015-11-25: Leave if a return statement has been executed
//				//while ((i < element.q.children.size())
//				//		&& result.equals("") && (stop == false))
//				while ((i < element.q.getSize())
//						&& result.equals("") && (stop == false) && !returned && leave == 0)
//				// END KGU#77/KGU#78 2015-11-25
//				{
//					result = step(element.q.getElement(i));
//					i++;
//				}
				if (result.isEmpty())
				{
					result = stepSubqueue(element.getBody(), true);
				}
				// END KGU#117 2016-03-07
                
				// START KGU#156 2016-03-11: Enh. #124
				element.addToExecTotalCount(1, true);	// For the condition test and increment
				//END KGU#156 2016-03-11
				
				// At this point, we symbolize the time for the incrementing and condition checking
				element.waited = false;
				element.executed = true;
				if (delay != 0 || step)
				{
					diagram.redraw();
				}
				checkBreakpoint(element);
				delay();
				element.executed = false;
				element.waited = true;
				
				// START KGU 2015-10-13: The step value is now calculated in advance
//				try
//				{
//					cw+=Integer.valueOf(pas);
//				}
//				catch(Exception e)
//				{
//					cw++;
//				}
				cw += sval;
				// END KGU 2015-10-13
			}
			// START KGU#78 2015-11-25
			if (leave > 0)
			{
				leave--;
			}
			loopDepth--;
			// END KGU#78 2015-11-25
			if (result.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
		} catch (EvalError ex)
		{
			result = ex.getMessage();
		}
		return result;
	}
	
	// START KGU#61 2016-03-21: Enh. #84
	// This executes FOR-IN loops
	private String stepForIn(For element)
	{
		String result = new String();
		String valueListString = element.getValueList();
		String iterVar = element.getCounterVar();
		Object[] valueList = null;
		String problem = "";	// Gathers exception descriptions for analysis purposes
		Object value = null;
		if (valueListString.startsWith("{") && valueListString.endsWith("}"))
		{
			try
			{
				interpreter.eval("Object[] tmp20160321kgu = " + valueListString);
				value = interpreter.get("tmp20160321kgu");
				interpreter.unset("tmp20160321kgu");
			}
			catch (EvalError ex)
			{
				problem = ex.getMessage();
			}
		}
		if (value == null && valueListString.contains(","))
		{
			try
			{
				interpreter.eval("Object[] tmp20160321kgu = {" + valueListString + "}");
				value = interpreter.get("tmp20160321kgu");
				interpreter.unset("tmp20160321kgu");
			}
			catch (EvalError ex)
			{
				problem = ex.getMessage();
			}
		}
		// Might be a function or variable otherwise evaluable
		if (value == null)
		{
			try
			{
				value = interpreter.eval(valueListString);
			}
			catch (EvalError ex)
			{
				problem = ex.getMessage();
			}
		}
		if (value == null && valueListString.contains(" "))
		{
			// Rather desparate attempt to compose an array from loose strings (like in shell scripts)
			StringList tokens = Element.splitExpressionList(valueListString, " ");
			try
			{
				interpreter.eval("Object[] tmp20160321kgu = {" + tokens.concatenate(",") + "}");
				value = interpreter.get("tmp20160321kgu");
				interpreter.unset("tmp20160321kgu");
			}
			catch (EvalError ex)
			{
				problem = ex.getMessage();
			}
		}
		if (value != null)
		{
			if (value instanceof Object[])
			{
				valueList = (Object[]) value;
			}
			else
			{
				valueList = new Object[1];
				valueList[0] = value;
			}
		}

		if (valueList == null)
		{
			result = "<" + valueListString
					+ "> cannot be interpreted as value list.";
			// START KGU 2016-07-06: Privide the gathered information
			if (!problem.isEmpty())
			{
				result += "\nDetails: " + problem;
			}
			// END KGU 2016-07-06
		}
		else
		{
				element.addToExecTotalCount(1, true);	// For the condition evaluation

				// Leave if any kind of Jump statement has been executed
				loopDepth++;
				int cw = 0;

				while (cw < valueList.length && result.equals("")
						&& (stop == false) && !returned && leave == 0)
				{
					try
					{
						setVar(iterVar, valueList[cw]);
						element.executed = false;
						element.waited = true;

						if (result.isEmpty())
						{
							result = stepSubqueue(((ILoop)element).getBody(), true);						
						}

						element.executed = true;
						element.waited = false;
						if (result.equals(""))
						{
							cw++;
							// Symbolizes the loop condition check 
							checkBreakpoint(element);
							delay();
						}
						element.addToExecTotalCount(1, true);	// For the condition evaluation
					} catch (EvalError ex)
					{
						result = ex.getMessage();
					}
				}
				// If there are open leave requests then nibble one off
				if (leave > 0)
				{
					leave--;
				}
				loopDepth--;
		}
		if (result.equals(""))
		{
			element.executed = false;
			element.waited = false;
		}
		/*
		 * if (cw > 1000000) { element.selected = true; result =
		 * "Your loop ran a million times. I think there is a problem!";
		 * }
		 */
		return result;
	}
	// END KGU#61 2016-03-21
	
	private String stepParallel(Parallel element)
	{
		String result = new String();
		try
		{
			int outerLoopDepth = this.loopDepth;
			int nThreads = element.qs.size();
			// For each of the parallel "threads" fetch a subqueue's Element iterator...
			Vector<Iterator<Element> > undoneThreads = new Vector<Iterator<Element>>();
			for (int thr = 0; thr < nThreads; thr++)
			{
				undoneThreads.add(element.qs.get(thr).getIterator());
			}

			element.waited = true;
			// Since we can hardly really execute this in parallel here,
			// the workaround is to run all the "threads" in a randomly chosen order...
			Random rdmGenerator = new Random(System.currentTimeMillis());

			// The first condition holds if there is at least one unexhausted "thread"
			// START KGU#77/KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
			//while (!undoneThreads.isEmpty() && result.equals("") && (stop == false))
			loopDepth = 0;	// Loop exits may not penetrate the Parallel section
			while (!undoneThreads.isEmpty() && result.equals("") && (stop == false) &&
					!returned && leave == 0)
			// END KGU#77/KGU#78 2015-11-25
			{
				// Pick one of the "threads" by chance
				int threadNr = rdmGenerator.nextInt(undoneThreads.size());
				Iterator<Element> iter = undoneThreads.get(threadNr);
				if (!iter.hasNext())
				{
					// Thread is exhausted - drop it
					undoneThreads.remove(threadNr);
				}
				else 
				{
					// Run the next instruction of the chosen thread
					Element instr = iter.next();
					int oldExecCount = instr.getExecStepCount(true);
					// END KGU#156 2016-03-11
					result = step(instr);
					// START KGU#117 2016-03-12: Enh. #77
					element.addToExecTotalCount(instr.getExecStepCount(true) - oldExecCount, false);
					// END KGU#117 2016-03-12
					// In order to allow better tracking we put the executed instructions into `waited´ state...
					instr.waited = true;
					// START KGU#78 2015-11-25: Parallel sections are impermeable for leave requests!
					if (result == "" && leave > 0)
					{
						// This should never happen (the leave instruction should have failed already)
						// At least we will kill the causing thread...
						undoneThreads.remove(threadNr);
						// ...and then of course wipe the remaining requested levels
						leave = 0;
						// As it is not only a user syntax error but also a flaw in the Structorizer mechanisms we better report it
						// START KGU#247 2016-09-17: Issue #243
						//JOptionPane.showMessageDialog(diagram, "Uncaught attempt to jump out of a parallel thread:\n\n" + 
						//		instr.getText().getText().replace("\n",  "\n\t") + "\n\nThread killed!",
						//		"Parallel Execution Problem", JOptionPane.WARNING_MESSAGE);
						JOptionPane.showMessageDialog(diagram, control.msgJumpOutParallel.getText().replace("%", "\n\n" + 
								instr.getText().getText().replace("\n",  "\n\t") + "\n\n"),
								control.msgTitleParallel.getText(), JOptionPane.WARNING_MESSAGE);
						// END KGU#247 2016-09-17
					}
					// END KGU#78 2015-11-25
				}                
			}
			this.loopDepth = outerLoopDepth;	// Restore the original context
			if (result.equals(""))
			{
				// Recursively reset all `waited´ flags of the subqueues now finished
				element.clearExecutionStatus();
			}
		} catch (Error ex)
		{
			result = ex.getMessage();
		}
		return result;
	}

	// START KGU#117 2016-03-07: Enh. #77 - to track test coverage a consistent subqueue handling is necessary
	String stepSubqueue(Subqueue sq, boolean checkLeave)
	{
		String result = "";
		
		int i = 0;
		while ((i < sq.getSize())
				&& result.equals("") && (stop == false) && !returned
				&& (!checkLeave || leave == 0))
		{
			// START KGU#156 2016-03-11: Enh. #124
			//result = step(sq.getElement(i));
			Element ele = sq.getElement(i);
			int oldExecCount = ele.getExecStepCount(true);
			result = step(ele);
			sq.parent.addToExecTotalCount(ele.getExecStepCount(true) - oldExecCount, false);
			// END KGU#156 2016-03-11
			i++;
		}
		if (sq.getSize() == 0)
		{
			sq.deeplyCovered = sq.simplyCovered = true;
			// START KGU#156 2016-03-11: Enh. #124
			sq.countExecution();
			//END KGU#156 2016-03-11
		}
		return result;
	}

	private String unconvert(String s)
	{
		s = s.replace("==", "=");
		return s;
	}

	private void waitForNext()
	{
		synchronized (this)
		{
			while (paus == true)
			{
				try
				{
					wait();
				} catch (Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
		/*
		 * int i = 0; while(paus==true) { System.out.println(i);
		 * 
		 * try { Thread.sleep(100); } catch (InterruptedException e) {
		 * System.out.println(e.getMessage());} i++; }
		 */

		if (step == true)
		{
			paus = true;
		}
	}
	
	// START KGU#33/KGU#34 2014-12-05
	// Method tries to extract the index value from an expression formed like
	// a array element access, i.e. "<arrayname>[<expression>]"
	private int getIndexValue(String varname) throws EvalError
	{
		// START KGU#141 2016-01-16: Bugfix #112
		String message = "Illegal (negative) index";
		// END KGU#141 2016-01-16
		String ind = varname.substring(varname.indexOf("[") + 1,
				// START KGU#166 2016-03-29: Bugfix #139 (nested index expressions failed)
				//varname.indexOf("]"));
				varname.lastIndexOf("]"));
				// END KGU#166 2016-03-29

		int index = -1;

		try
		{
			//index = Integer.parseInt(ind);		// KGU: This was nonsense - usually no literal here
			index = (Integer) this.interpreter.eval(ind);
		}
		catch (Exception e)
		{
			//index = (Integer) this.interpreter.get(ind);	// KGU: This didn't work for expressions
			// START KGU#141 2016-01-16: Bugfix #112 - this led to silent errors and incapacitation of executor
			//System.out.println(e.getMessage() + " on " + varname + " in Executor.getIndexValue()");
			message = e.getMessage();	// We will rethrow it later
			// END KGU#141 2016-01-16
		}
		// START KGU#141 2016-01-16: Bugfix #112 - We may not allow negative indices
		if (index < 0)
		{
			throw new EvalError(message + " on index evaluation in: " + varname, null, null);
		}
		// END KGU#141 2016-01-16
		return index;
	}
	// END KGU#33/KGU#34 2014-12-05
	
	// START KGU#165 2016-04-03: Support keyword case sensitivity
	/**
	 * Returns an appropriate match string for the given parser preference string
	 * (where D7Parser.ignoreCase is paid attention to)
	 * @param keyword - parser preference string
	 * @return match pattern
	 */
	private String getKeywordPattern(String keyword)
	{
		String pattern = Matcher.quoteReplacement(keyword);
		if (D7Parser.ignoreCase)
		{
			pattern = BString.breakup(pattern);
		}
		return pattern;
	}
	// END KGU#165 2016-04-03
	
	// START KGU#156 2016-03-10: An interface for an external update trigger was needed
	public void redraw()
	{
		diagram.repaint();
	}
	// END KGU#156 2016-03-10
	
    // START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
	public void setOutputWindowEnabled(boolean _enabled)
	{
		this.isConsoleEnabled = _enabled;
		this.console.setVisible(_enabled);
	}
	// END KGU#160 2016-04-12

}

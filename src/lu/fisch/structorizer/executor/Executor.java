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
 *      Kay Gürtzig     2016.09.25      Bugfix #251: Console window wasn't involved in look and feel update
 *      Kay Gürtzig     2016.09.25      Bugfix #254: parser keywords for CASE elements had been ignored
 *                                      Enh. #253: CodeParser.keywordMap refactoring done
 *      Kay Gürtzig     2016.10.06      Bugfix #261: Stop didn't work immediately within multi-line instructions
 *      Kay Gürtzig     2016.10.07      Some synchronized sections added to reduce inconsistency exception likelihood
 *      Kay Gürtzig     2016.10.09      Bugfix #266: Built-in Pascal functions copy, delete, insert defectively implemented;
 *                                      Issue #269: Attempts to scroll the diagram to currently executed elements (ineffective)
 *      Kay Gürtzig     2016.10.12      Issue #271: Systematic support for user-defined input prompts
 *      Kay Gürtzig     2016.10.13      Enh. #270: Elements may be disabled for execution ("outcommented")
 *      Kay Gürtzig     2016.10.16      Enh. #273: Input strings "true" and "false" now accepted as boolean values
 *                                      Bugfix #276: Raw string conversion and string display mended, undue replacements
 *                                      of ' into " in method convert() eliminated
 *      Kay Gürtzig     2016.11.19      Issue #269: Scrolling problem eventually solved. 
 *      Kay Gürtzig     2016.11.22      Bugfix #293: input and output boxes no longer popped up at odd places on screen.
 *      Kay Gürtzig     2016.11.22/25   Issue #294: Test coverage rules for CASE elements without default branch refined
 *      Kay Gürtzig     2016.12.12      Issue #307: Attempts to manipulate FOR loop variables now cause an error
 *      Kay Gürtzig     2016.12.22      Enh. #314: Support for File API
 *      Kay Gürtzig     2016.12.29      Enh. #267/#315 (KGU#317): Execution abort on ambiguous CALLs
 *      Kay Gürtzig     2017.01.06      Bugfix #324: Trouble with replacing an array by a scalar value on input
 *                                      Enh. #325: built-in type test functions added.
 *      Kay Gürtzig     2017.01.17      Enh. #335: Toleration of Pascal variable declarations in stepInstruction()
 *      Kay Gürtzig     2017.01.27      Enh. #335: Toleration of BASIC variable declarations in stepInstruction()
 *      Kay Gürtzig     2017.02.08      Issue #343: Unescaped internal string delimiters escaped on string literal conversion
 *      Kay Gürtzig     2017.02.17      KGU#159: Stacktrace now also shows the arguments of top-level subroutine calls
 *      Kay Gürtzig     2017.03.06      Bugfix #369: Interpretation of C-style array initializations (decl.) fixed.
 *      Kay Gürtzig     2017.03.27      Issue #356: Sensible reaction to the close button ('X') implemented
 *      Kay Gürtzig     2017.03.30      Enh. #388: Concept of constants implemented
 *      Kay Gürtzig     2017.04.11      Enh. #389: Implementation of import calls (without context change)
 *      Kay Gürtzig     2017.04.12      Bugfix #391: Control button activation fixed for step mode
 *      Kay Gürtzig     2017.04.14      Issue #380/#394: Jump execution code revised on occasion of these bugfixes
 *      Kay Gürtzig     2017.04.22      Code revision KGU#384: execution context bundled into Executor.context
 *      Kay Gürtzig     2017.05.07      Enh. #398: New built-in functions sgn (int result) and signum (float result)
 *      Kay Gürtzig     2017.05.22      Issue #354: converts binary literals ("0b[01]+") into decimal literals 
 *      Kay Gürtzig     2017.05.23      Bugfix #411: converts certain unicode escape sequences to octal ones
 *      Kay Gürtzig     2017.05.24      Enh. #413: New function split(string, string) built in
 *      Kay Gürtzig     2017.06.09      Enh. #416: Support for execution line continuation by trailing backslash
 *      Kay Gürtzig     2017.06.30      Enh. #424: Turtleizer functions enabled (evaluateDiagramControllerFunctions())
 *      Kay Gürtzig     2017.07.01      Enh. #413: Special check for built-in split function in stepForIn()
 *      Kay Gürtzig     2017.07.02      Enh. #389: Include (import) mechanism redesigned (no longer CALL-based)
 *      Kay Gürtzig     2017.09.09      Bugfix #411 revised (issue #426)
 *      Kay Gürtzig     2017.09.17      Enh. #423: First draft implementation of records.
 *      Kay Gürtzig     2017.09.18/27   Enh. #423: Corrections on handling typed constants and for-in loops with records
 *      Kay Gürtzig     2017.09.30      Bugfix #429: Initializer evaluation made available in return statements
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2017-04-22 Code revision (KGU#384)
 *      - The execution context as to be pushed to call stack had been distributed over numerous attributes
 *        and were bundled to an ExecutionStackEntry held in attribute context (the class ExecutionStackEntry
 *        is likely to be renamed to ExecutionContext). This was to simplify the call mechanisms and regain
 *        overview and control.
 *      2016-03-17 Enh. #133 (KGU#159)
 *      - Previously, a Call stack trace was only shown in case of an execution error or manual abort.
 *        Now a Call stack trace may always be requested while execution hasn't ended. Only prerequisite
 *        is that the execution be paused. Then a click on the button "Stacktrace" will do.
 *        Moreover, the stacktrace will always be presented as list view (before, a simple message box had
 *        been used unless the number of call levels exceeded 10).   
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
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
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.elements.Updater;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.gui.Diagram;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.parsers.CodeParser;
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
	// START KGU#376 2017-04-20: Enh. #389
	/**
	 * Context record for an imported Root in order to fetch the defined constants
	 * and global/static variables as well as defined or declared types.
	 * This prototype version just contains the BeanShell interpreter used at the
	 * initial execution and hence bearing values etc. and a list of variable names.
	 * Supports enhancement #389
	 * @author Kay Gürtzig
	 * @see Executor#importMap
	 */
	private class ImportInfo {
		public final Interpreter interpreter;
		public final StringList variableNames;
		// START KGU#388 2017-09-18: Enh. 423
		public final HashMap<String, TypeMapEntry> typeDefinitions;
		// END KGU#388 2017-09-18
		public ImportInfo(Interpreter _interpr, StringList _varNames) {
			interpreter = _interpr;
			variableNames = _varNames;
			// START KGU#388 2017-09-18: Enh. 423
			typeDefinitions = new HashMap<String, TypeMapEntry>();
			// END KGU#388 2017-09-18
		}
		// START KGU#388 2017-09-18: Enh. 423
		public ImportInfo(Interpreter _interpr, StringList _varNames, HashMap<String, TypeMapEntry> _typeMap) {
			interpreter = _interpr;
			variableNames = _varNames;
			typeDefinitions = new HashMap<String, TypeMapEntry>(_typeMap);
		}
		// END KGU#388 2017-09-18
	};
	// END KGU#376 2017-04-20

	private static Executor mySelf = null;
	// START KGU#311 2016-12-22: Enh. #314 - fileAPI index
	public static final String[] fileAPI_names = {
		"fileOpen", "fileCreate", "fileAppend",
		"fileClose",
		"fileRead", "fileReadChar", "fileReadInt", "fileReadDouble", "fileReadLine",
		"fileEOF",
		"fileWrite", "fileWriteLine"
	};
	// END KGU#311 2016-12-22

	/**
	 * Returns the singleton instance if there is one
	 * @return the existing instance or null.
	 */
	public static Executor getInstance()
	{
		return mySelf;
	}

	/**
	 * Ensures there is a (singleton) instance and returns it
	 * @param diagram - the Diagram instance requesting the instance (also used for conflict detection)
	 * @param diagramController - possibly an additional effector for execution 
	 * @return the sole instance of this class.
	 */
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
	
	// START KGU#376 2017-04-20: Enh. #389 - we need info about all imported Roots
	/**
	 * Maps all Roots ever called as import during current execution to their
	 * execution results, represented by an ImportInfo object, such that whenever
	 * the same Root will be requested for import again, we may just retrieve its
	 * results here.
	 * @see ExecutionStackEntry#importList 
	 */
	private final HashMap<Root, ImportInfo> importMap = new HashMap<Root, ImportInfo>();
	//private StringList importList = new StringList();	// KGU#384 2017-04-22: -> context
	// END KGU#376 2017-04-20
	// START KGU#384 2017-04-22: Redesign of the execution context
	/**
	 * Execution context cartridge containing all context to be pushed to callers stack on calls
	 */
	private ExecutionStackEntry context;
	// END KGU#376 2017-04-20
	// START KGU#2 (#9) 2015-11-13: We need a stack of calling parents
	private Stack<ExecutionStackEntry> callers = new Stack<ExecutionStackEntry>();
	//private Object returnedValue = null;	// KGU#384 2017-04-22 -> context
	private Vector<IRoutinePool> routinePools = new Vector<IRoutinePool>();
	// END KGU#2 (#9) 2015-11-13
	// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
	//private StringList forLoopVars = new StringList();	// KGU#384 2017-04-22 -> context
	// END KGU#307 2016-12-12

	private DiagramController diagramController = null;
	// START KGU#384 2017-04-22: Context redesign -> this.context
	//private Interpreter interpreter;
	//private boolean returned = false;
	// END KGU#384 2017-04-22

	private boolean paus = false;
	private boolean running = false;
	private boolean step = false;
	private boolean stop = false;
	// START KGU#78 2015-11-25: JUMP enhancement (#35)
	//private int loopDepth = 0;	// Level of nested loops KGU#384 207-04-22 -> context
	private int leave = 0;		// Number of loop levels to unwind
	// END KGU#78 2015-11-25
	//private StringList variables = new StringList();	// KGU#384 2017-04-22 -> context
	// START KGU#375 2017-03-30: Enh. #388 Support the concept of variables
	//private HashMap<String, Object> constants = new HashMap<String, Object>();	// KGU#384 2017-04-22 -> context
	// END KGU#375 2017-03-30
	// START KGU#2 2015-11-24: It is crucial to know whether an error had been reported on a lower level
	private boolean isErrorReported = false;
	private StringList stackTrace = new StringList();
	// END KGU#2 2015-11-22
	// START KGU#157 2016-03-16: Bugfix #131 - Precaution against a reopen attempts by different Structorizer instances
	private Diagram reopenFor = null;	// A Structorizer instance that tried to open Control while still running
	// END KGU#2 2016-03-16
	// START KGU 2016-12-18: Enh. #314: Stream table for Simple file API
	private final Vector<Closeable> openFiles = new Vector<Closeable>();
	// END KGU 2016-12-18

	private Executor(Diagram diagram, DiagramController diagramController)
	{
		this.diagram = diagram;
		this.diagramController = diagramController;
		// START KGU#372 2017-03-27: Enh. #356: Show at least an information on closing attempt
		this.control.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent evt) {}

			@Override
			public void windowClosed(WindowEvent evt) {}

			@Override
			public void windowClosing(WindowEvent evt) {
				if (evt.getSource() == control) {	// should be the only possible source but...
					if (running) {
						JOptionPane.showMessageDialog(null, Control.msgUseStopButton.getText(),
								mySelf.getClass().getSimpleName() + ": " + mySelf.diagram.getRoot().getSignatureString(false),
								JOptionPane.WARNING_MESSAGE);
					}
					else {
						control.clickStopButton();
					}
				}
			}

			@Override
			public void windowDeactivated(WindowEvent evt) {}

			@Override
			public void windowDeiconified(WindowEvent evt) {}

			@Override
			public void windowIconified(WindowEvent evt) {}

			@Override
			public void windowOpened(WindowEvent evt) {}
			
		});
		// END KGU#372 2017-03-27
	}

	// START KGU#210/KGU#234 2016-08-08: Issue #201 - Ensure GUI consistency
	public static void updateLookAndFeel()
	{
		if (mySelf != null)
		{
			mySelf.control.updateLookAndFeel();
			// START KGU#255 2016-09-25: Bugfix #251
			SwingUtilities.updateComponentTreeUI(mySelf.console);
			// END KGU#255 2016-09-25
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
		StringList tokens = Element.splitLexically(s, true);
		Element.unifyOperators(tokens, false);
		// START KGU#130 2015-01-08: Bugfix #95 - Conversion of div operator had been forgotten...
		tokens.replaceAll("div", "/");		// FIXME: Operands should better be coerced to integer...
		// END KGU#130 2015-01-08
		// START KGU#285 2016-10-16: Bugfix #276
		// pascal: quotes
		for (int i = 0; i < tokens.count(); i++)
		{
			String token = tokens.get(i);
			// START KGU#342 2017-01-08: Issue #343 We must also escape all internal quotes
			//if (token.length() != 3 && token.startsWith("'") && token.endsWith("'"))
			//{
			//	tokens.set(i, "\"" + token.substring(1, token.length()-1) + "\"");
			//}
			int tokenLen = token.length();
			if (tokenLen >= 2 && (token.startsWith("'") && token.endsWith("'") || token.startsWith("\"") && token.endsWith("\"")))
			{
				char delim = token.charAt(0);
				String internal = token.substring(1, tokenLen-1);
				// Escape all unescaped double quotes
				int pos = -1;
				while ((pos = internal.indexOf("\"", pos+1)) >= 0) {
					if (pos == 0 || internal.charAt(pos-1) != '\\') {
						internal = internal.substring(0, pos) + "\\" + internal.substring(pos);
						pos++;
					}
				}
				// START KGU 2017-04-22 unescaping of double single quotes - no, doesn't make sense
				//if (token.startsWith("'") && internal.length() > 2) {
				//	int intLen = internal.length();
				//	internal = internal.replace("''", "'");
				//	tokenLen -= (intLen - internal.length());
				//}
				// END KGU 2017-04-22
				// START KGU#406/KGU#420 2017-05-23/2017-09-09: Bugfix #411, #426 (arose with COBOL import)
				// The interpreter doesn't cope with unicode escape sequences "\\u000a", "\\u000d", "\\u0022", and "\\u005c"
				internal = internal.replaceAll("(.*)\\\\u000[aA](.*)", "$1\\\\012$2").
						replaceAll("(.*?)\\\\u000[dD](.*?)", "$1\\\\015$2").
						replaceAll("(.*?)\\\\u0022(.*?)", "$1\\\\042$2").
						replaceAll("(.*?)\\\\u005[cC](.*?)", "$1\\\\134$2");
				// END KGU#406/KGU#420 2017-05-23/2017-09-09
				if (!(tokenLen == 3 || tokenLen == 4 && token.charAt(1) == '\\')) {
					delim = '\"';
				}
				tokens.set(i, delim + internal + delim);
			}
			// END KGU#342 2017-01-08
			// START KGU#354 2017-05-22: Unfortunately theinterpreter doesn't cope with binary integer literals, so convert them
			else if (token.matches("0b[01]+")) {
				tokens.set(i, "" + Integer.parseInt(token.substring(2), 2));
			}
			// END KGU#354 2017-05-22
		}
		// END KGU#285 2016-10-16
		// Function names to be prefixed with "Math."
		final String[] mathFunctions = {
				// START KGU#391 2017-05-07: Enh. #398 We needed a sign function for facilitating COBOL rounding import
				"signum",
				// END KGU#391 2017-05-07
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
		// START KGU#275 2016-10-09: Bugfix #266 obsolete replacement obstructed assignment recognition
		//r = new Regex("delete\\((.*),(.*),(.*)\\)", "$1=delete($1,$2,$3)");
		r = new Regex("delete\\((.*),(.*),(.*)\\)", "$1 <- delete($1,$2,$3)");
		// END KGU#275 2016-10-09
		s = r.replaceAll(s);
		// pascal: insert
		// START KGU#275 2016-10-09: Bugfix #266 obsolete replacement obstructed assignment recognition
		//r = new Regex("insert\\((.*),(.*),(.*)\\)", "$2=insert($1,$2,$3)");
		r = new Regex("insert\\((.*),(.*),(.*)\\)", "$2 <- insert($1,$2,$3)");
		// END KGU#275 2016-10-09
		s = r.replaceAll(s);
		// START KGU#285 2016-10-16: Bugfix #276 - this spoiled apostrophes because misplaced here
//		// pascal: quotes
//		r = new Regex("([^']*?)'(([^']|'')*)'", "$1\"$2\"");
//		//r = new Regex("([^']*?)'(([^']|''){2,})'", "$1\"$2\"");
//		s = r.replaceAll(s);
		// END KGU#285 2016-10-16
		// START KGU 2015-11-29: Adopted from Root.getVarNames() - can hardly be done in initInterpreter() 
        // pascal: convert "inc" and "dec" procedures
        r = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 + $2"); s = r.replaceAll(s);
        r = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)","$1 <- $1 + 1"); s = r.replaceAll(s);
        r = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 - $2"); s = r.replaceAll(s);
        r = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)","$1 <- $1 - 1"); s = r.replaceAll(s);
        // END KGU 2015-11-29
		
        // START KGU 2017-04-22: now done above in the string token conversion
		//s = s.replace("''", "'");	// (KGU 2015-11-29): Looks like an unwanted relic!
        // END KGU 2017-04-22
		// pascal: randomize
		s = s.replace("randomize()", "randomize");
		s = s.replace("randomize", "randomize()");

		// clean up ... if needed
		s = s.replace("Math.Math.", "Math.");

		if (convertComparisons)
		{
			// FIXME: This should only be applied to an expression in s, not to an entire instruction line!
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
							Object leftO = this.evaluateExpression(left, false);
							Object rightO = this.evaluateExpression(right, false);
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
								// START KGU#342 2017-02-09: Bugfix #343 - be aware of characters to be escaped
								//exprs.set(i, leftParenth + left + ".compareTo(\"" + (Character)rightO + "\") " + compOps[op] + " 0" + rightParenth);
								exprs.set(i, leftParenth + left + ".compareTo(\"" + this.literalFromChar((Character)rightO) + "\") " + compOps[op] + " 0" + rightParenth);
								// END KGU#342 2017-02-09
								// END KGU#76 2016-04-25
								replaced = true;								
							}
							else if ((leftO instanceof Character) && (rightO instanceof String))
							{
								// START KGU#76 2016-04-25: Issue #30 support all string comparison
								//exprs.set(i, leftParenth + neg + right + ".equals(\"" + (Character)leftO + "\")" + rightParenth);
								// START KGU#342 2017-02-09: Bugfix #343 - be aware of characters to be escaped
								//exprs.set(i, leftParenth + "\"" + (Character)leftO + "\".compareTo(" + right + ") " + compOps[op] + " 0" + rightParenth);
								exprs.set(i, leftParenth + "\"" + this.literalFromChar((Character)leftO) + "\".compareTo(" + right + ") " + compOps[op] + " 0" + rightParenth);
								// END KGU#342 2017-02-09
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
						}
						catch (EvalError ex)
						{
							System.err.println("Executor.convertStringComparison(\"" + str + "\"): " + ex.getMessage());
						}
						catch (Exception ex)
						{
							System.err.println("Executor.convertStringComparison(\"" + str + "\"): " + ex.getMessage());
						}
					} // if (!s.equals(" " + eqOps[op] + " ") && (s.indexOf(eqOps[op]) >= 0))
				} // for (int op = 0; op < eqOps.length; op++)
				if (replaced)
				{
					// Compose the partial expressions and undo the regex escaping for the initial split
					str = exprs.getLongString().replace(" \\|\\| ", " || ");
					str.replace("  ", " ");	// Get rid of multiple spaces
				}
			}
		}
		return str;
	}
	// END KGU#57 2015-11-07
	
	// START KGU#342 2017-02-09: Bugfix #343
	private String literalFromChar(char ch) {
		String literal = Character.toString(ch);
		if ("\"\'\\\b\f\n\r\t".indexOf(ch) >= 0) {
			literal = "\\" + literal;
		}
		return literal;
	}
	// END KGU#342 2017-02-09

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
				System.err.println("Executor.delay(): " + e.getMessage());
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
		Root root = this.diagram.getRoot();
		this.callers.clear();
		this.stackTrace.clear();
		this.routinePools.clear();
		// START KGU#376 2017-04-22: Enh. #389
		this.importMap.clear();
		// END KGU#376 2017-04-22
		// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
		//this.forLoopVars.clear();	// KGU#384 2017-04-22 -> new context
		// END KGU#307 2016-12-12
		// START KGU#375 2017-03-30: Enh. #388: Keep track of constants
		//this.constants.clear();	// KGU#384 2017-04-22 -> new context
		// END KGU#375 2017-03-30
		// START KGU 2016-12-18: Enh. #314
		for (Closeable file: this.openFiles) {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					System.err.println("Executor.execute(); openFiles -> " + e.getLocalizedMessage());
				}
			}
		}
		this.openFiles.clear();
		// END KGU 2016-12-18

		if (Arranger.hasInstance())
		{
			this.routinePools.addElement(Arranger.getInstance());
			// START KGU#117 2016-03-08: Enh. #77
			Arranger.getInstance().clearExecutionStatus();
			// END KGU#117 2016-03-08
		}
		this.isErrorReported = false;
		root.isCalling = false;
		// START KGU#160 2016-04-12: Enh. #137 - Address the console window 
		this.console.clear();
		SimpleDateFormat sdf = new SimpleDateFormat();
		this.console.writeln("*** STARTED \"" + root.getText().getLongString() +
				"\" at " + sdf.format(System.currentTimeMillis()) + " ***", Color.GRAY);
		if (this.isConsoleEnabled) this.console.setVisible(true);
		// END KGU#160 2016-04-12
		// START KGU#384 2017-04-22
		this.context = new ExecutionStackEntry(root, null);
		initInterpreter();
		// END KGU#384 2017-04-22
		/////////////////////////////////////////////////////////
		this.execute(null);	// The actual top-level execution
		/////////////////////////////////////////////////////////
		this.callers.clear();
		this.stackTrace.clear();
		// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
		this.context.forLoopVars.clear();
		// END KGU#307 2016-12-12
		// START KGU 2016-12-18: Enh. #314
		for (Closeable file: this.openFiles) {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					System.err.println("Executor.execute(); openFiles -> " + e.getLocalizedMessage());
				}
			}
		}
		this.openFiles.clear();
		// END KGU 2016-12-18
		// START KGU#160 2016-04-12: Enh. #137 - Address the console window 
		this.console.writeln("*** TERMINATED \"" + root.getText().getLongString() +
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
		
		// START KGU#384 2017-04-22: execution context redesign
		//Root root = diagram.getRoot();
		Root root = context.root;
		// END 2017-04-22

		// START KGU#159 2016-03-17: Now we permanently maintain the stacktrace, not only in case of error
		//addToStackTrace(root, arguments);	// KGU 2017-02-17 moved downwards, after the argument request
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
		// START KGU#376 2017-04-11: Enh. #389 - Must no longer done here but in execute() and executeCall()
		//initInterpreter();
		// END KGU#376 2017-04-11
		String result = "";
		// START KGU#384 2017-04-22: Holding all execution context in this.context now
		//returned = false;
		// START KGU#78 2015-11-25
		//loopDepth = 0;
		leave = 0;
		// END KGU#78 2015-11-25
		// END KGU#384 207-04-22
		
		// START KGU#376 2017-07-01: Enh. #389 - perform all specified includes
		result = importSpecifiedIncludables(root);
		// END KGU#376 2017-07-01

		// START KGU#39 2015-10-16 (1/2): It made absolutely no sense to look for parameters if root is a program
		if (root.isSubroutine() && result.isEmpty())
		{
		// END KGU#39 2015-10-16 (1/2)
			StringList params = root.getParameterNames();
			//System.out.println("Having: "+params.getCommaText());
			// START KGU#375 2017-03-30: Enh. #388 - support a constant concept
			StringList pTypes = root.getParameterTypes();
			// END KGU#375 2017-03-30
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
				// START KGU#375 2017-03-30: Enh. #388 - support a constant concept
				String type = pTypes.get(i);
				boolean isConstant = type != null && (type.toLowerCase() + " ").startsWith("const ");
				// END KGU#375 2017-03-30
				// START KGU#388 2017-09-18: Enh. #423 Track at least record types
				if (type != null) {
					StringList typeTokens = Element.splitLexically(type, true);
					typeTokens.removeAll(" ");
					if (isConstant) {
						typeTokens.remove(0);
					}
					if (typeTokens.count() == 1 && context.dynTypeMap.containsKey(":" + (type = typeTokens.get(0)))) {
						context.dynTypeMap.put(in, context.dynTypeMap.get(":" + type));
					}
				}
				// END KGU#388 2017-09-18
				
				// START KGU#2 (#9) 2015-11-13: If root was not called then ask the user for values
				if (noArguments)
				{
				// END KGU#2 (#9) 2015-11-13
					// START KGU#89 2016-03-18: More language support 
					//String str = JOptionPane.showInputDialog(null,
					//		"Please enter a value for <" + in + ">", null);
					String msg = control.lbInputValue.getText();
					msg = msg.replace("%", in);
					String str = JOptionPane.showInputDialog(diagram.getParent(), msg, null);
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
						// START KGU#375 2017-03-30: Enh. 388: Support a constant concept
						//setVarRaw(in, str);
						if (isConstant) {
							setVarRaw("const " + in, str);
						}
						else {
							setVarRaw(in, str);
						}
						// END KGU#375 2017-03-30
						// END KGU#69 2015-11-08
						// START KGU#2 2015-11-24: We might need the values for a stacktrace
						arguments[i] = context.interpreter.get(in);
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
						// START KGU#375 2017-03-30: Enh. 388: Support a constant concept
						//setVar(in, arguments[i]);
						if (isConstant) {
							setVar("const " + in, arguments[i]);
						}
						else {
							setVar(in, arguments[i]);
						}
						// END KGU#375 2017-03-30
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
		// START KGU#376 2017-04-22: Enh. #389 - without arguments, we must also show the new context 
		try {
			this.updateVariableDisplay();
		} catch (EvalError ex) {}
		// END KGU#376 2017-04-22

		// START KGU#159 2017-02-17: Now we permanently maintain the stacktrace, not only in case of error
		addToStackTrace(root, arguments);
		// END KGU#159 2017-03-17
	
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
				JOptionPane.showMessageDialog(diagram.getParent(), result, control.msgTitleError.getText(),
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
			if (root.isSubroutine() && (context.returned == false))
			{
				// Possible result variable names
				StringList posres = new StringList();
				posres.add(root.getMethodName());
				posres.add("result");
				posres.add("RESULT");
				posres.add("Result");

				try
				{
					int i = 0;
					while ((i < posres.count()) && (!context.returned))
					{
						Object resObj = context.interpreter.get(posres.get(i));
						if (resObj != null)
						{
							// START KGU#2 (#9) 2015-11-13: Only tell the user if this wasn't called
							//JOptionPane.showMessageDialog(diagram, n,
							//		"Returned result", 0);
							context.returnedValue = resObj;
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
									JOptionPane.showMessageDialog(diagram.getParent(), resObj,
											header, JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									// START KGU#198 2016-05-25: Issue #137 - also log the result to the console
									this.console.writeln("*** " + header + ": " + this.prepareValueForDisplay(resObj), Color.CYAN);
									// END KGU#198 2016-05-25
									Object[] options = {
											control.lbOk.getText(),
											control.lbPause.getText()
											};
									int pressed = JOptionPane.showOptionDialog(diagram.getParent(), resObj, header,
											JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
									if (pressed == 1)
									{
										paus = true;
										step = true;
										// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
										//control.setButtonsForPause();
										control.setButtonsForPause(false);	// This avoids interference with the pause button
										// END KGU#379 2017-04-12
									}
								}
								// END KGU#84 2015-11-23
								// END KGU#133 2016-01-09
							}
							// START KGU#148 2016-01-29: Pause now here, particularly for subroutines
							delay();
							// END KGU#148 2016-01-29							
							// END KGU#2 (#9) 2015-11-13
							context.returned = true;
						}
						i++;
					}
				} catch (EvalError ex)
				{
					Logger.getLogger(Executor.class.getName()).log(
							Level.SEVERE, null, ex);
				}

			}
			// START KGU#299 2016-11-23: Enh. #297 In step mode, this offers a last pause to inspect variables etc.
			if (this.callers.isEmpty() && !context.returned) {
				delay();
			}
			// END KGU 2016-11-23

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
	
	// START KGU#376 2017-07-01: Enh. #389 - perform all specified includes
	private String importSpecifiedIncludables(Root root) {
		String errorString = "";
		if (root.includeList != null) {
			root.waited = true;
			root.isIncluding = true;
			for (int i = 0; errorString.isEmpty() && i < root.includeList.count(); i++) {
				delay();
				Root imp = null;
				String diagrName = root.includeList.get(i);
				try {
					imp = this.findIncludableWithName(diagrName);
				} catch (Exception ex) {
					return ex.getMessage();	// Ambiguous call!
				}
				if (imp != null)
				{
					// START KGU#376 2017-04-21: Enh. #389
					// Has this import already been executed -then just adopt the results
					if (this.importMap.containsKey(imp)) {
						ImportInfo impInfo = this.importMap.get(imp);
						this.copyInterpreterContents(impInfo.interpreter, context.interpreter,
								imp.variables, imp.constants.keySet(), false);
						// START KGU#388 2017-09-18: Enh. #423
						// Adopt the imported typedefs if any
						for (Entry<String, TypeMapEntry> typeEntry: impInfo.typeDefinitions.entrySet()) {
							TypeMapEntry oldEntry = context.dynTypeMap.putIfAbsent(typeEntry.getKey(), typeEntry.getValue());
							if (oldEntry != null) {
								System.err.println("Conflicting type entry " + typeEntry.getKey() + " from Includable " + diagrName);
							}
						}
						// END KGU#388 2017-09-18
						context.variables.addIfNew(impInfo.variableNames);
						for (String constName: imp.constants.keySet()) {
							// FIXME: Is it okay just to ignore conflicting constants?
							if (!context.constants.containsKey(constName)) {
								try {
									context.constants.put(constName, impInfo.interpreter.get(constName));
								} catch (EvalError e) {
									if (!errorString.isEmpty()) {
										errorString += "\n";
									}
									errorString += e.getMessage();
								}
							}
						}
						try 
						{
							updateVariableDisplay();
						}
						catch (EvalError ex) {}
					}
					else {
						// END KGU#376 2017-04-21
						executeCall(imp, null, null);
					}
					context.importList.addIfNew(diagrName);
				}
				else
				{
					// START KGU#197 2016-07-27: Now translatable message
					//result = "A subroutine diagram " + f.getName() + " (" + f.paramCount() + 
					//		" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";
					errorString = control.msgNoInclDiagram.getText().
							replace("%", diagrName);
					// END KGU#197 2016-07-27
				}
			}
			if (errorString.isEmpty()) {
				root.waited = false;
				root.isIncluding = false;
			}
		}
		return errorString;
	}
	// END KGU#376 2017-07-01

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
				step = true; paus = true; control.setButtonsForPause(true);
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
	// KGU#376 2017-07-01: Enh.#389 - caller may now be null if an include is performed
	//private Object executeCall(Root subRoot, Object[] arguments)
	private Object executeCall(Root subRoot, Object[] arguments, Call caller)
	// END KGU#156 2016-03-12
	{
		boolean cloned = false;
		Root root = subRoot;
		Object resultObject = null;
		// START KGU#384 2017-04-22: Replaced by the ExecutionContext cartridge
//		Root oldRoot = this.diagram.getRoot();
//		ExecutionStackEntry entry = new ExecutionStackEntry(
//				oldRoot,
//				this.variables, 
//				this.interpreter,
//				// START KGU#78 2015-11-25
//				this.loopDepth,
//				// END KGU#78 2015-11-25
//				// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
//				this.forLoopVars,
//				// END KGU#307 2016-12-12
//				// START KGU#375/KGU#376 2017-04-21: Enh. #388, #389
//				this.constants,
//				this.importList
//				// END KGU#375, KGU#376 2017-04-21
//				);
		this.callers.push(this.context);
		// END KGU#384 2017-04-22
		// START KGU#376 2017-04-21: Update all current imports before sub execution
		for (int i = 0; i < context.importList.count(); i++) {
			String impName = context.importList.get(i);
			// FIXME This retrieval is a little awkward - maybe the importList should be a set of Root
			for (Root impRoot: this.importMap.keySet()) {
				if (impRoot.getMethodName().equals(impName)) {
					ImportInfo info = this.importMap.get(impRoot);
					this.copyInterpreterContents(context.interpreter, info.interpreter, info.variableNames, impRoot.constants.keySet(), true);
				}
			}
		}
		// START KGU#384 2017-04-22 Is done below now, when setting up the new context
//		if (!subRoot.isProgram) {
//			// It's not an import, so start with a new importList 
//			this.importList = new StringList();
//		}
		// END KGU#384 2017-04-22
		// END KGU#376 2017-04-21
		// START KGU#384 2017-04-22: Now delegated to execute(Object[])
//		this.initInterpreter();
//		this.variables = new StringList();	// FIXME -> Map<String, Set<Interpreter>>
//		// START KGU#375 2017-04-21: Enh. #388: Need also a new constants enviroment
//		this.constants = new HashMap<String, Object>();
//		// END KGU#375 2017-04-21
//		// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
//		this.forLoopVars = new StringList(); 
//		// END KGU#307 2016-12-12
//		// loopDepth will be set 0 by the execut(arguments) call below
		// END KGU#384 2017-04-22
		
		// If the found subroutine is already an active caller, then we need a new instance of it
		if (root.isCalling)
		{
			root = (Root)root.copy();
			root.isCalling = false;
			// Remaining initialisations will be done by this.execute(...).
			cloned = true;
		}
		// START KGU#384 2017-04-22: Execution context redesign
		if (root.isInclude()) {
			// For an import Call continue the importList recursively
			this.context = new ExecutionStackEntry(root, this.context.importList);
		}
		else {
			// For a subroutine call, start with a new import list
			this.context = new ExecutionStackEntry(root);
		}
		initInterpreter();
		// END KGU#384 2017-04-22
		
		this.diagram.setRoot(root, !Element.E_AUTO_SAVE_ON_EXECUTE);
		
		// START KGU#156 2016-03-11: Enh. #124 - detect execution counter diff.
		int countBefore = root.getExecStepCount(true);
		// END KGU#156 2016-03-11
		
		/////////////////////////////////////////////////////////
		this.execute(arguments);	// Actual execution of the subroutine or import
		/////////////////////////////////////////////////////////
		
		// START KGU#156 2016-03-11: Enh. #124 / KGU#376 2017-07-01: Enh. #389 - caller may be null
		if (caller != null) {
			caller.addToExecTotalCount(root.getExecStepCount(true) - countBefore, true);
			if (cloned || root.isTestCovered(true))	
			{
				caller.deeplyCovered = true;
			}
		}
		// END KGU#156 2016-03-11 / KGU#376 2017-07-01

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
		
		ExecutionStackEntry entry = this.callers.pop();	// former context
		
//		// START KGU#376 2017-04-21: Enh. #389 don't restore after an import call
		// FIXME: Restore but cache the Interpreter with all variables and copy contents before
		if (subRoot.isInclude()) {
			// It was an import Call, so we have to import the definitions and values 
			// FIXME: Derive a sensible type StringList from subRoot.getTypeInfo() KGU 2017-09-18: what for?
			this.copyInterpreterContents(context.interpreter, entry.interpreter,
					this.context.variables, entry.root.constants.keySet(), false);
			// START KGU#388 2017-09-18: Enh. #423
			// Adopt the imported typedefs if any
			for (Entry<String, TypeMapEntry> typeEntry: context.dynTypeMap.entrySet()) {
				TypeMapEntry oldEntry = entry.dynTypeMap.putIfAbsent(typeEntry.getKey(), typeEntry.getValue());
				if (oldEntry != null) {
					System.err.println("Conflicting type entry " + typeEntry.getKey() + " from Includable " + subRoot.getMethodName());
				}
			}
			// END KGU#388 2017-09-18
			entry.variables.addIfNew(context.variables);
			for (Entry<String, Object> constEntry: context.constants.entrySet()) {
				if (!entry.constants.containsKey(constEntry.getKey())) {
					entry.constants.put(constEntry.getKey(), constEntry.getValue());
				}
			}	
			this.importMap.put(subRoot, new ImportInfo(this.context.interpreter, this.context.variables, this.context.dynTypeMap));
			context.importList.addIfNew(subRoot.getMethodName());
			// TODO: Check this for necessity and soundness!
			for (Entry<String, String> constEntry: subRoot.constants.entrySet()) {
				if (!entry.root.constants.containsKey(constEntry.getKey())) {
					entry.root.constants.put(constEntry.getKey(), constEntry.getValue());
				}
			}
		}
		else {
			// Subroutines may have updated definitions from import diagrams - we must get aware of these changes 
			for (int i = 0; i < context.importList.count(); i++) {
				String impName = context.importList.get(i);
				// FIXME This retrieval is a little awkward - maybe the importList should be a set of Root
				for (Root impRoot: this.importMap.keySet()) {
					if (impRoot.getMethodName().equals(impName)) {
						ImportInfo info = this.importMap.get(impRoot);
						if (this.copyInterpreterContents(context.interpreter, info.interpreter, info.variableNames, impRoot.constants.keySet(), true)
								&& entry.importList.contains(impName)) {
							this.copyInterpreterContents(info.interpreter, entry.interpreter, info.variableNames, impRoot.constants.keySet(), true);
						}
					}
				}
			}
		}
//		// END KGU#376 2017-04-21
		// START KGU#384 2017-04-22: Now done at once with the entire context cartridge
//		this.variables = entry.variables;
//		// START KGU#375 2017-04-21: Enh. #388: Need also a new constants enviroment
//		this.constants = entry.constants;
//		// END KGU#375 2017-04-21
//		this.interpreter = entry.interpreter;
//		// START KGU#78 2015-11-25
//		this.loopDepth = entry.loopDepth;
//		// END KGU#78 2015-11-25
//		this.forLoopVars = entry.forLoopVars;
		// END KGU#384 2017-08-22
		
		this.diagram.setRoot(entry.root, !Element.E_AUTO_SAVE_ON_EXECUTE);
		entry.root.isCalling = false;

		// START KGU#376 2017-04-21: Enh. #389
		// The called subroutine will certainly have returned a value...
		resultObject = this.context.returnedValue;
		// ... but definitively not THIS calling routine!
		// FIXME: Shouldn't we have cached the previous values in entry?
		
		// START KGU#384 2017-04-22: Now done at once with the entire context cartridge
		//this.returned = false; 
		//this.returnedValue = null;
		this.context = entry;
		// END KGU#384 2017-08-22
		
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
     * Searches all known pools for a unique includable diagram with given name 
     * @param name - diagram name
     * @return a Root of type INCLUDABLE with given name if uniquely found, null otherwise
     * @throws Exception
     */
	public Root findIncludableWithName(String name) throws Exception
	{
		return findDiagramWithSignature(name, -1);
	}
	
    /**
     * Searches all known pools for subroutines with a signature compatible to name(arg1, arg2, ..., arg_nArgs) 
     * @param name - function name
     * @param nArgs - number of parameters of the requested function
     * @return a Root that matches the specification if uniquely found, null otherwise
     * @throws Exception 
     */
    public Root findSubroutineWithSignature(String name, int nArgs) throws Exception
    {
    	Root subroutine = null;
    	// First test whether the current root calls itself recursively
    	Root root = diagram.getRoot();
    	if (name.equals(root.getMethodName()) && nArgs == root.getParameterNames().count())
    	{
    		subroutine = root;
    	}
    	if (subroutine == null) {
    		subroutine = findDiagramWithSignature(name, nArgs);
    	}
    	return subroutine;
    }
    
    private Root findDiagramWithSignature(String name, int nArgs) throws Exception
    {
    	Root diagr = null;
    	Iterator<IRoutinePool> iter = this.routinePools.iterator();
    	while (diagr == null && iter.hasNext())
    	{
    		IRoutinePool pool = iter.next();
    		Vector<Root> candidates = null;
    		if (nArgs >= 0) {
    			candidates = pool.findRoutinesBySignature(name, nArgs);
    		}
    		else {
    			candidates = new Vector<Root>();
    			for (Root cand: pool.findIncludesByName(name)) {
    				candidates.add(cand);
    			}
    		}
    		// START KGU#317 2016-12-29: Now the execution will be aborted on ambiguous calls
    		//for (int c = 0; subroutine == null && c < candidates.size(); c++)
    		for (int c = 0; c < candidates.size(); c++)
    		// END KGU#317 2016-12-29
    		{
    	    	// START KGU#317 2016-12-29: Check for ambiguity (multiple matches) and raise e.g. an exception in that case
    			//subroutine = candidates.get(c);
    			if (diagr == null) {
    				diagr = candidates.get(c);
    			}
    			else {
    				Root cand = candidates.get(c);
    				int similarity = diagr.compareTo(cand); 
    				if (similarity > 2 && similarity != 4) {
    					throw new Exception(control.msgAmbiguousCall.getText().replace("%1", name).replace("%2", (nArgs < 0 ? "--" : Integer.toString(nArgs))));
    				}
    			}
    			// END KGU#317 2016-12-29
    			// START KGU#125 2016-01-05: Is to force updating of the diagram status
    			if (pool instanceof Updater)
    			{
    				diagr.addUpdater((Updater)pool);
    			}
    			diagram.adoptArrangedOrphanNSD(diagr);
    			// END KGU#125 2016-01-05
    		}
    	}
    	return diagr;
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
				System.err.println("Executor.getExec(\"" + cmd + "\"): " + e.getMessage());
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
				System.err.println("Executor.getExec(\"" + cmd + "\", " + color + "): " + e.getMessage());
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
			// STRT KGU#384 2017-04-22: Redesign of execution context
			//interpreter = new Interpreter();
			Interpreter interpreter = this.context.interpreter;
			// END KGU#384 2017-04-22
			String pascalFunction;
			// random
			pascalFunction = "public int random(int max) { return (int) (Math.random()*max); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public void randomize() {  }";
			interpreter.eval(pascalFunction);
			// START KGU#391 2017-05-07: Enh. #398 - we need a sign function to ease the rounding support for COBOL import
			pascalFunction = "public int sgn(int i) { return (i == 0 ? 0 : (i > 0 ? 1 : -1)); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public int sgn(double d) { return (d == 0 ? 0 : (d > 0 ? 1 : -1)); }";
			interpreter.eval(pascalFunction);
			// END KGU#391 2017-05-07
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
			// START KGU#275 2016-10-09: Bugfix #266: length tolerance of copy function had to be considered
			//pascalFunction = "public String copy(String s, int start, int count) { return s.substring(start-1,start-1+count); }";
			pascalFunction = "public String copy(String s, int start, int count) { int end = Math.min(start-1+count, s.length()); return s.substring(start-1,end); }";
			// END KGU#275 2016-10-09
			interpreter.eval(pascalFunction);
			// delete a part of a string
			pascalFunction = "public String delete(String s, int start, int count) { return s.substring(0,start-1)+s.substring(start+count-1); }";
			interpreter.eval(pascalFunction);
			// insert a string into another one
			pascalFunction = "public String insert(String what, String s, int start) { return s.substring(0,start-1)+what+s.substring(start-1); }";
			interpreter.eval(pascalFunction);
			// string transformation
			pascalFunction = "public String lowercase(String s) { return s.toLowerCase(); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public String uppercase(String s) { return s.toUpperCase(); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public String trim(String s) { return s.trim(); }";
			interpreter.eval(pascalFunction);
			// START KGU#410 2017-05-24: Enh. #413: Introduced to facilitate COBOL import but generally useful
			// If we passed the result of String.split() directly then we would obtain a String[] object the 
			// Executor cannot display.
			pascalFunction = "public Object[] split(String s, String p)"
					+ "{ p = java.util.regex.Pattern.quote(p);"
					+ " String[] parts = s.split(p, -1);"
					+ "Object[] results = new Object[parts.length];"
					+ " for (int i = 0; i < parts.length; i++) {"
					+ "		results[i] = parts[i];"
					+ "}"
					+ "return results; }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public Object[] split(String s, char c)"
					+ "{ return split(s, \"\" + c); }";
			interpreter.eval(pascalFunction);
			// END KGU#410 2017-05-24
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
			// START KGU#322 2017-01-06: Enh. #325 - reflection functions
			pascalFunction = "public boolean isArray(Object obj) { return (obj instanceof Object[]); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public boolean isString(Object obj) { return (obj instanceof String); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public boolean isChar(Object obj) { return (obj instanceof Character); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public boolean isBool(Object obj) { return (obj instanceof Boolean); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public boolean isNumber(Object obj) { return (obj instanceof Integer) || (obj instanceof Double); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public int length(Object[] arr) { return arr.length; }";
			interpreter.eval(pascalFunction);
			// END KGU#322 2017-01-06
			// START KGU 2016-12-18: #314: Support for simple text file API
			interpreter.set("executorFileMap", this.openFiles);
			interpreter.set("executorCurrentDirectory", 
					(diagram.currentDirectory.isDirectory() ? diagram.currentDirectory : diagram.currentDirectory.getParentFile()).getAbsolutePath());
			pascalFunction = "public int fileOpen(String filePath) { "
					+ "int fileNo = 0; "
					+ "java.io.File file = new java.io.File(filePath); "
					+ "if (!file.isAbsolute()) { "
					+ "file = new java.io.File(executorCurrentDirectory + java.io.File.separator + filePath); "
					+ "} "
					+ "try { java.io.FileInputStream fis = new java.io.FileInputStream(file); "
					+ "java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis, \"UTF-8\")); "
					+ "fileNo = executorFileMap.size() + 1; "
					+ "executorFileMap.add(new java.util.Scanner(reader)); "
					+ "} "
					+ "catch (SecurityException e) { fileNo = -3; } "
					+ "catch (java.io.FileNotFoundException e) { fileNo = -2; } "
					+ "catch (java.io.IOException e) { fileNo = -1; } "
					+ "return fileNo; }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public int fileCreate(String filePath) { "
					+ "int fileNo = 0; "
					+ "java.io.File file = new java.io.File(filePath); "
					+ "if (!file.isAbsolute()) { "
					+ "file = new java.io.File(executorCurrentDirectory + java.io.File.separator + filePath); "
					+ "} "
					+ "try { java.io.FileOutputStream fos = new java.io.FileOutputStream(file); "
					+ "java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, \"UTF-8\")); "
					+ "fileNo = executorFileMap.size() + 1; "
					+ "executorFileMap.add(writer); "
					+ "} "
					+ "catch (SecurityException e) { fileNo = -3; } "
					+ "catch (java.io.FileNotFoundException e) { fileNo = -2; } "
					+ "catch (java.io.IOException e) { fileNo = -1; } "
					+ "return fileNo; }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public int fileAppend(String filePath) { "
					+ "int fileNo = 0; "
					+ "java.io.File file = new java.io.File(filePath); "
					+ "if (!file.isAbsolute()) { "
					+ "file = new java.io.File(executorCurrentDirectory + java.io.File.separator + filePath); "
					+ "} "
					+ "try { java.io.FileOutputStream fos = new java.io.FileOutputStream(file, true); "
					+ "java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, \"UTF-8\")); "
					+ "fileNo = executorFileMap.size() + 1; "
					+ "executorFileMap.add(writer); "
					+ "} "
					+ "catch (SecurityException e) { fileNo = -3; } "
					+ "catch (java.io.FileNotFoundException e) { fileNo = -2; } "
					+ "catch (java.io.IOException e) { fileNo = -1; } "
					+ "return fileNo; "
					+ "}";
			interpreter.eval(pascalFunction);
			pascalFunction = "public void fileClose(int fileNo) { "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable file = executorFileMap.get(fileNo - 1); "
					+ "if (file != null) { "
					+ "try { file.close(); } "
					+ "catch (java.io.IOException e) {} "
					+ "executorFileMap.set(fileNo - 1, null); } "
					+ "}"
					+ "}";
			interpreter.eval(pascalFunction);
			pascalFunction = "public boolean fileEOF(int fileNo) {"
					+ "	boolean isEOF = true; "
					+ "	if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "		java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "		if (reader instanceof java.util.Scanner) { "
					+ "			try { "
					+ "				isEOF = !((java.util.Scanner)reader).hasNext();"
					+ "			} catch (IOException e) {}"
					+ "		}"
					+ "	}"
					+ "	else { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "	return isEOF;"
					+"}";
			interpreter.eval(pascalFunction);
			// The following is just a helper method...
			pascalFunction = "public Object structorizerGetScannedObject(java.util.Scanner sc) {"
					+ "Object result = null; "
					+ "sc.useLocale(java.util.Locale.UK); "
					+ "if (sc.hasNextInt()) { result = sc.nextInt(); } "
					+ "else if (sc.hasNextDouble()) { result = sc.nextDouble(); } "
					+ "else if (sc.hasNext(\"\\\\\\\".*?\\\\\\\"\")) { "
					+ "String str = sc.next(\"\\\\\\\".*?\\\\\\\"\"); "
					+ "result = str.substring(1, str.length() - 1); "
					+ "} "
					+ "else if (sc.hasNext(\"'.*?'\")) { "
					+ "String str = sc.next(\"'.*?'\"); "
					+ "result = str.substring(1, str.length() - 1); "
					+ "} "
					+ "else if (sc.hasNext(\"\\\\{.*?\\\\}\")) { "
					+ "String token = sc.next(); "
					+ "result = new Object[]{token.substring(1, token.length()-1)}; "
					+ "} " 
					+ "else if (sc.hasNext(\"\\\\\\\".*\")) { "
					+ "String str = sc.next(); "
					+ "while (sc.hasNext() && !sc.hasNext(\".*\\\\\\\"\")) { "
					+ "str += \" \" + sc.next(); "
					+ "} "
					+ "if (sc.hasNext()) { str += \" \" + sc.next(); } "
					+ "result = str.substring(1, str.length() - 1); "
					+ "} "
					+ "else if (sc.hasNext(\"'.*\")) { "
					+ "String str = sc.next(); "
					+ "while (sc.hasNext() && !sc.hasNext(\".*'\")) { "
					+ "str += \" \" + sc.next(); "
					+ "} "
					+ "if (sc.hasNext()) { str += \" \" + sc.next(); } "
					+ "result = str.substring(1, str.length() - 1); "
					+ "} "
					+ "else if (sc.hasNext(\"\\\\{.*\")) { "
					+ "java.util.regex.Pattern oldDelim = sc.delimiter(); "
					+ "sc.useDelimiter(\"\\\\}\"); "
					+ "String content = sc.next().trim().substring(1); "
					+ "sc.useDelimiter(oldDelim); "
					+ "if (sc.hasNext(\"\\\\}\")) { sc.next(); } "
					+ "String[] elements = {}; "
					+ "if (!content.isEmpty()) { "
					+ "elements = content.split(\"\\\\p{javaWhitespace}*,\\\\p{javaWhitespace}*\"); "
					+ "} "
					+ "Object[] objects = new Object[elements.length]; "
					+ "for (int i = 0; i < elements.length; i++) { "
					+ "java.util.Scanner sc0 = new java.util.Scanner(elements[i]); "
					+ "objects[i] = structorizerGetScannedObject(sc0); "
					+ "sc0.close(); "
					+ "} "
					+ "result = objects;"
					+ "}"
					+ "else { result = sc.next(); } "
					+ "return result; }";
			interpreter.eval(pascalFunction);
// KGU: The following outcommented code is for debugging the local equivalent of the function above
//			Scanner sc = new Scanner("test 3.4 4,6 \"Alles großer Murks \" hier. {tfzz64} \"aha\" {6, 89,8, 2,DFj}\n{666}");
//			Object obj = null;
//			do {
//				try {
//				obj = this.structorizerGetScannedObject(sc);
//				if (obj instanceof Object[]) {
//					System.out.print("Array: ");
//					for (int i = 0; i < ((Object[])obj).length; i++) {
//						System.out.print(" | " + ((Object[])obj)[i]);
//					}
//					System.out.println("");
//				}
//				else System.out.println(obj.getClass().getName() + ": " + obj);
//				}
//				catch (java.util.NoSuchElementException ex) { obj = null; }
//			} while (obj != null);
//			sc.close();
// KGU: The preceding outcommented code is for debugging the local equivalent of the function above
			pascalFunction = "public Object fileRead(int fileNo) { "
					+ "Object result = null; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "result = structorizerGetScannedObject((java.util.Scanner)reader); "
					+ "ok = true;"
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return result; }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public Character fileReadChar(int fileNo) { "
					+ "Character result = '\0'; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "java.util.Scanner sc = (java.util.Scanner)reader; "
					+ "java.util.regex.Pattern oldDelim = sc.delimiter(); "
					+ "sc.useDelimiter(\"\"); "
					+ "try { "
					+ "if (!sc.hasNext(\".\") && sc.hasNextLine()) { sc.nextLine(); result = '\\n'; }"
					+ "else { result = sc.next(\".\").charAt(0); } "
					+ "}"
					+ "finally { sc.useDelimiter(oldDelim); } "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return result; }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public Integer fileReadInt(int fileNo) { "
					+ "Integer result = null; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "result = ((java.util.Scanner)reader).nextInt(); "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return result; }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public Double fileReadDouble(int fileNo) { "
					+ "Double result = null; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "result = ((java.util.Scanner)reader).nextDouble(); "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return result; }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public String fileReadLine(int fileNo) { "
					+ "String line = null; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "line = ((java.util.Scanner)reader).nextLine(); "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return line;	}";
			interpreter.eval(pascalFunction);
			pascalFunction = "public void fileWrite(int fileNo, java.lang.Object data) { "
					+ "	boolean ok = false; "
					+ "	if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "		java.io.Closeable writer = executorFileMap.get(fileNo - 1); "
					+ "		if (writer instanceof java.io.BufferedWriter) { "
					+ "			((java.io.BufferedWriter)writer).write(data.toString()); "
					+ "		ok = true;"
					+ "	}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberWrite.getText() + "\"); } "
					+ "}";
			interpreter.eval(pascalFunction);
			pascalFunction = "public void fileWriteLine(int fileNo, java.lang.Object data) { "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable file = executorFileMap.get(fileNo - 1); "
					+ "if (file instanceof java.io.BufferedWriter) { "
					+ "((java.io.BufferedWriter)file).write(data.toString()); "
					+ "((java.io.BufferedWriter)file).newLine(); "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberWrite.getText() + "\"); } "
					+ "}";
			interpreter.eval(pascalFunction);
			// END KGU 2016-12-18
			// START KGU#375 2017-03-30: Enh. #388 Workaround for missing support of Object[].clone() in bsh-2.0b4.jar
			pascalFunction = "public Object[] copyArray(Object[] sourceArray) {"
					+ "Object[] targetArray = new Object[sourceArray.length];"
					+ "for (int i = 0; i < sourceArray.length; i++) {"
					+ "targetArray[i] = sourceArray[i];"
					+ "}"
					+ "return targetArray;"
					+ "}";
			interpreter.eval(pascalFunction);
			// END KGU#375 2017-03-30
			// START KGU#388 2017-09-13: Enh. #423 Workaround for missing support of HashMap<?,?>.clone() in bsh-2.0b4.jar
			pascalFunction = "public HashMap copyRecord(HashMap sourceRecord) {"
					+ "HashMap targetRecord = new HashMap();"
					+ "for (java.util.Map.Entry entry: sourceRecord.entrySet()) {"
					+ "targetRecord.put(entry.getKey(), entry.getValue());"
					+ "}"
					+ "return targetRecord;"
					+ "}";
			interpreter.eval(pascalFunction);
			// END KGU#388 2017-09-13
			// START TEST fileAppend
//			int handle = fileAppend("AppendTest.txt");
//			System.out.println("fileAppend: " + handle);
//			interpreter.eval("fileWriteLine("+handle+", \"Bandwurm\")");
//			interpreter.eval("fileWriteLine("+handle+", 4711)");
//			interpreter.eval("fileClose("+handle+")");
			// END TEST
			// START TEST fileRead
//			int handle = fileOpen("D:/SW-Produkte/Structorizer/tests/Issue314/StructorizerFileAPI.cpp");
//			if (handle > 0) {
//				try {
//					while (!fileEOF(handle)) {
//						Object value = fileRead(handle);
//						System.out.println(value);
//					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				try {
//					fileClose(handle);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			// END TEST
		} catch (EvalError ex)
		{
			//java.io.IOException
			System.err.println("Executor.initInterpreter(): " + ex.getMessage());
		}
	}
	
	// Test for Interpreter routines
//	public Object structorizerGetScannedObject(java.util.Scanner sc) {
//		Object result = null; 
//		sc.useLocale(java.util.Locale.UK); 
//		if (sc.hasNextInt()) { result = sc.nextInt(); } 
//		else if (sc.hasNextDouble()) { result = sc.nextDouble(); } 
//		else if (sc.hasNext("\\\".*?\\\"")) { result = sc.next("\\\".*?\\\""); } 
//		else if (sc.hasNext("\\{.*?\\}")) {
//			String token = sc.next();
//			result = new Object[]{token.substring(1, token.length()-1)};
//		} 
//		else if (sc.hasNext("\\\".*")) { 
//			String str = sc.next(); 
//			while (sc.hasNext() && !sc.hasNext(".*\\\"")) { 
//				str += " " + sc.next();
//			}
//			if (sc.hasNext()) { str += " " + sc.next(); }
//			result = str;
//		}
//		else if (sc.hasNext("\\{.*")) { 
//			java.util.regex.Pattern oldDelim = sc.delimiter();
//			//sc.useDelimiter("(\\p{javaWhitespace}*,\\p{javaWhitespace}*|\\})");
//			sc.useDelimiter("\\}");
//			String expr = sc.next().trim().substring(1);
//			sc.useDelimiter(oldDelim);
//			String[] elements = {};
//			if (!expr.isEmpty()) {
//				elements = expr.split("\\p{javaWhitespace}*,\\p{javaWhitespace}*");
//			}
//			if (sc.hasNext("\\}")) { sc.next(); }
//			Object[] objects = new Object[elements.length];
//			for (int i = 0; i < elements.length; i++) { 
//				java.util.Scanner sc0 = new java.util.Scanner(elements[i]);
//				objects[i] = structorizerGetScannedObject(sc0);
//				sc0.close();
//			}
//			result = objects;
//		}
//		else { result = sc.next(); }
//		return result;
//	}

//	public int fileOpen(String filePath)
//	{
//		int fileNo = 0; 
//		java.io.File file = new java.io.File(filePath);
//		try {
//			java.io.FileInputStream fis = new java.io.FileInputStream(file);
//			java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis, "UTF-8"));
//			fileNo = this.openFiles.size() + 1;
//			this.openFiles.add(new java.util.Scanner(reader));
//		}
//		catch (SecurityException e) { fileNo = -3; }
//		catch (java.io.FileNotFoundException e) { fileNo = -2; }
//		catch (java.io.IOException e) { fileNo = -1; }
//		return fileNo;
//	}

//	public int fileAppend(String filePath)
//	{
//		int fileNo = 0;
//		java.io.File file = new java.io.File(filePath);
//		if (!file.isAbsolute()) {
//			file = diagram.currentDirectory;
//			if (!file.isDirectory()) { file = file.getParentFile(); }
//			file = new java.io.File(file.getAbsolutePath() + java.io.File.separator + filePath);
//			filePath = file.getAbsolutePath();
//		}
//		java.io.BufferedWriter writer = null;
//		System.out.println(file.getName());
//		try {
//			if (file.exists()) {
//				java.io.File tmpFile = java.io.File.createTempFile("structorizer_"+file.getName(), null);
//				if (tmpFile.exists()) { tmpFile.delete(); }
//				if (file.renameTo(tmpFile)) {
//					java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath); 
//					java.io.FileInputStream fis = new java.io.FileInputStream(tmpFile.getAbsolutePath()); 
//					writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, "UTF-8")); 
//					java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis, "UTF-8"));
//					String line = null; 
//					while ((line = reader.readLine()) != null) {
//						writer.write(line); writer.newLine();
//					} 
//					reader.close();
//					tmpFile.delete();
//				}
//				else {
//					fileNo = -4;
//				}
//			} 
//			else { 
//				java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath); 
//				writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, "UTF-8")); 				
//			} 
//			fileNo = this.openFiles.size() + 1;
//			this.openFiles.add(writer);  
//		} 
//		catch (SecurityException e) { fileNo = -3; } 
//		catch (java.io.FileNotFoundException e) { fileNo = -2; }
//		catch (java.io.IOException e) { fileNo = -1; }
//		return fileNo;
//	}
	
//	public void fileClose(int fileNo) throws java.io.IOException
//	{
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable file = this.openFiles.get(fileNo - 1);
//			if (file != null) {
//				try { file.close(); }
//				catch (java.io.IOException e) {}
//				this.openFiles.set(fileNo - 1, null); }
//		}
//		else { throw new java.io.IOException("fileClose: §INVALID_HANDLE_READ§"); }
//	}

//	public boolean fileEOF(int fileNo) throws java.io.IOException
//	{
//		boolean isEOF = true;
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable reader = this.openFiles.get(fileNo - 1);
//			if (reader instanceof java.util.Scanner) {
//				//try {
//					isEOF = !((java.util.Scanner)reader).hasNext();
//				//} catch (java.io.IOException e) {}
//			}
//		}
//		else { throw new java.io.IOException("fileEOF: §INVALID_HANDLE_READ§"); }
//		return isEOF;
//	}

//	public Object fileRead(int fileNo) throws java.io.IOException
//	{
//		Object result = null;
//		boolean ok = false;
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable reader = this.openFiles.get(fileNo - 1);
//			if (reader instanceof java.util.Scanner) {
//				result = structorizerGetScannedObject((java.util.Scanner)reader);
//				ok = true;
//			}
//		}
//		if (!ok) { throw new java.io.IOException("fileRead: §INVALID_HANDLE_READ§"); }
//		return result;
//	}

//	public String fileReadLine(int fileNo) throws java.io.IOException
//	{
//		String line = null;
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable file = this.openFiles.get(fileNo - 1);
//			if (file instanceof java.io.BufferedReader) {
//				line = ((java.io.BufferedReader)file).readLine();
//			}
//		}
//		return line;
//	}

//	public void fileWrite(int fileNo, String line)
//	{
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable file = this.openFiles.get(fileNo - 1);
//			if (file instanceof java.io.BufferedWriter) {
//				((java.io.BufferedWriter)file).write(line);
//				((java.io.BufferedWriter)file).newLine();
//			}
//		}
//	}

	// START KGU#376 2017-04-20: Enh. #389 - we need to copy interpreter contents 
	/**
	 * Copies the constants specified by <code>_constNames</code> and the values of the variables
	 * specified by <code>_varNames</code> from the <code>_source</code> interpreter context to the
	 * <code>_target</code> interpreter context.
	 * @param _source - the source interpreter
	 * @param _target - the target interpreter
	 * @param _varNames - names of the variables to be considered
	 * @param _constNames - names of the constants to be included
	 * @param _overwrite - whereas defined constants are never overwritten, for variables this argument
	 * may aloow to update the values of already existing values (default is false)
	 * @return true if there was at least one copied entity
	 */
	private boolean copyInterpreterContents(Interpreter _source, Interpreter _target, StringList _varNames, Set<String> _constNames, boolean _overwrite)
	{
		boolean somethingCopied = false;
		for (int i = 0; i < _varNames.count(); i++) {
			String varName = _varNames.get(i);
			try {
				if (!_constNames.contains(varName) && _overwrite || _target.get(varName) == null) {
					Object val = _source.get(_varNames.get(i));
					// Here we try to avoid hat all specific values are boxed to
					// Object.
					if (val instanceof Boolean) {
						_target.set(varName, ((Boolean)val).booleanValue());
						somethingCopied = true;
					}
					else if (val instanceof Integer) {
						_target.set(varName, ((Integer)val).intValue());
						somethingCopied = true;
					}
					else if (val instanceof Long) {
						_target.set(varName, ((Long)val).longValue());
						somethingCopied = true;
					}
					else if (val instanceof Float) {
						_target.set(varName, ((Float)val).floatValue());
						somethingCopied = true;
					}
					else if (val instanceof Double) {
						_target.set(varName, ((Double)val).doubleValue());
						somethingCopied = true;
					}
					else {
						_target.set(varName, val);
						somethingCopied = true;
					}
				}
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return somethingCopied;
	}
	// END KGU#376 2017-04-20

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
	/**
	 * Interprets and evaluates the user input string {@code rawInput} and assigns the result to the given
	 * variable extracted from the "lvalue" {@code target} via {@link #setVar(String, Object)}.
	 * @param target - an assignment lvalue, may contain modifiers, type info and access specifiers
	 * @param rawInput - the raw input string to be interpreted
	 * @throws EvalError if the interpretation of {@code rawInput} fails, if the {@code target} or the resulting
	 * value is inappropriate, if both don't match or if a loop variable violation is detected.
	 * @see #setVar(String, Object) 
	 */
	private void setVarRaw(String target, String rawInput) throws EvalError
	{
		// first add as string (lest we should end with nothing at all...)
		// START KGU#109 2015-12-15: Bugfix #61: Previously declared (typed) variables caused errors here
		//setVar(name, rawInput);
		try {
			setVar(target, rawInput);
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
					this.evaluateExpression(target + " = " + rawInput, false);
					setVar(target, context.interpreter.get(target));
				}
				// START KGU#285 2016-10-16: Bugfix #276
				else if (rawInput.contains("\\"))
				{
					// Obviously it isn't enclosed by quotes (otherwise the previous test would have caught it
					this.evaluateExpression(target + " = \"" + rawInput + "\"", false);
					setVar(target, context.interpreter.get(target));					
				}
				// END KGU#285 2016-10-16
				// try adding as char (only if it's not a digit)
				else if (rawInput.length() == 1)
				{
					Character charInput = rawInput.charAt(0);
					setVar(target, charInput);
				}
				// START KGU#184 2016-04-25: Enh. #174 - accept array initialisations on input
				else if (strInput.startsWith("{") && rawInput.endsWith("}"))
				{
					String asgnmt = "Object[] " + target + " = " + rawInput;
					// Nested initializers won't work here!
					this.evaluateExpression(asgnmt, false);
					setVar(target, context.interpreter.get(target));
				}
				// END KGU#184 2016-04-25
				// START KGU#388 2017-09-18: Enh. #423
				else if (strInput.indexOf("{") > 0 && strInput.endsWith("}")
						&& Function.testIdentifier(strInput.substring(0, strInput.indexOf("{")), null)) {
					String asgnmt = "HashMap " + target + " = new HashMap()";
					this.evaluateExpression(asgnmt, false);
					HashMap<String, String> components = Element.splitRecordInitializer(strInput);
					for (Entry<String, String> comp: components.entrySet()) {
						String value = comp.getValue();
						if (comp.getKey().startsWith("§")) {
							value = "\"" + value + "\"";
						}
						asgnmt = target + ".put(\"" + comp.getKey() + "\", " + value + ")";
						this.evaluateExpression(asgnmt, false);
					}
					setVar(target, context.interpreter.get(target));
				}
				// END KGU#388 2017-09-18
				// START KGU#283 2016-10-16: Enh. #273
				else if (strInput.equals("true") || strInput.equals("false"))
				{
					setVar(target, Boolean.valueOf(strInput));
				}
				// END KGU#283 2016-10-16
			}
			catch (Exception ex)
			{
				System.out.println(rawInput + " as string/char: " + ex.getMessage());
				// START KGU#388 2017-09-18: These explicit errors should get raised
				throw ex;
				// END KGU#388 2017-09-18
			}
		}
		// try adding as double
		try
		{
			double dblInput = Double.parseDouble(rawInput);
			setVar(target, dblInput);
		} catch (Exception ex)
		{
			//System.out.println(rawInput + " as double: " + ex.getMessage());
		}
		// finally try adding as integer
		try
		{
			int intInput = Integer.parseInt(rawInput);
			setVar(target, intInput);
		} catch (Exception ex)
		{
			//System.out.println(rawInput + " as int: " + ex.getMessage());
		}
	}

	// METHOD MODIFIED BY GENNARO DONNARUMMA and revised by Kay Gürtzig
	/**
	 * Assigns the computed value {@code content} to the given variable extracted from the "lvalue"
	 * {@code target}. Analyses and handles possibly given extra information in order to register and
	 * declare the target variable or constant.<br/>
	 * Also ensures that no loop variable manipulation is performed (the entire loop stack is checked,
	 * so use {@link #setVar(String, Object, int)} for a regular loop variable update).<br/>
	 * There are the following sensible cases w.r.t. {@code target} here (unquoted brackets enclose optional parts):<br/>
	 * a) {@code [const] <id>}<br/>
	 * b) {@code <id>'['<expr>']'}<br/>
	 * c) {@code [const] <typespec1> <id>}<br/>
	 * d) {@code [const] <typespec1> <id>'['[<expr>]']'}  - implicit C-style array declaration (questionable)<br/>
	 * e) {@code [const|var] <id> : <typespec2>}<br/>
	 * f) {@code [const|dim] <id> as <typespec2>}<br/>
	 * g) {@code <id>(.<id>['['<expr>']'])+}<br/>
	 * h) {@code <id>'['<expr>']'(.<id>)+}<br/>
	 * ILLEGAL (NOT supported here):<br/>
	 * w) {@code const <id>'['<expr>']'} - single elements can't be const<br/>
	 * x) {@code [const] <id>'['']'}  - C-style array declaration: redundant if array value is assigned, wrong otherwise<br/>
	 * y) {@code <id>'['<expr>']'('['<expr>']')}+<br/>
	 * Meta symbol legend (as far as not obvious):<br/>
	 * {@code <typespec1> ::=}<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;<code>{modifier} &lt;typeid&gt; |</code><br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;<code>{modifier} &lt;typeid&gt; ('['']')+</code> - Java-style array type (questionable)<br/>
	 * {@code <typespec2> ::=}<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <typeid> |}<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@code array ['['<range>']'] of <typespec>}<br/>
	 * {@code <range> ::= <id> | <intliteral> .. <intliteral>}<br/>
	 * @param target - an assignment lvalue, may contain modifiers, type info and access specifiers
	 * @param content - the value to be assigned
	 * @throws EvalError if the {@code target} or the {@code content} is inappropriate or if both don't
	 * match or if a loop variable violation is detected.
	 * @see #setVarRaw(String, Object)
	 * @see #setVar(String, Object, int) 
	 */
	private void setVar(String target, Object content) throws EvalError
	// START KGU#307 2016-12-12: Enh. #307 - check FOR loop variable manipulation
	{
		setVar(target, content, context.forLoopVars.count()-1);
	}

	/**
	 * Assigns the computed value {@code content} to the given variable extracted from the "lvalue"
	 * {@code target}. Analyses and handles possibly given extra information in order to register and
	 * declare the target variable or constant.<br/>
	 * Also ensures that no loop variable manipulation is performed. 
	 * @param target - an assignment lvalue, may contain modifiers, type info and access specifiers
	 * @param content - the value to be assigned
	 * @param ignoreLoopStackLevel - the loop nesting level beyond which loop variables aren't critical.
	 * @throws EvalError if the {@code target} or the {@code content} is inappropriate or if both don't
	 * match or if a loop variable violation is detected.
	 * @see #setVarRaw(String, Object)
	 * @see #setVar(String, Object)
	 */
	private void setVar(String target, Object content, int ignoreLoopStackLevel) throws EvalError
	// END KGU#307 2016-12-12
	{
		// START KGU#375 2017-03-30: Enh. #388 - Perform a clear case analysis instead of some heuristic poking
		// We refer to the cases listed in the javadoc of method setVar(target, content).
		boolean isConstant = false;
		String recordName = null;
		TypeMapEntry compType = null;
		StringList typeDescr = null;
		String indexStr = null;

		// ======== PHASE 1: Analysis of the target structure ===========

		StringList tokens = Element.splitLexically(target, true);
		tokens.removeAll(" ");
		int nTokens = tokens.count();
		String token0 = tokens.get(0).toLowerCase();
		if ((isConstant = token0.equals("const")) || token0.equals("var") || token0.equals("dim")) {
			// a), c), d), e), f) ?
			tokens.remove(0);
			// Extract type information
			int posColon = tokens.indexOf(":");
			if (posColon < 0 && !token0.equals("var")) posColon = tokens.indexOf("as", false);
			if (posColon >= 0) {
				typeDescr = tokens.subSequence(posColon+1, nTokens);
				tokens = tokens.subSequence(0, posColon);
				// In case of an explicit and Pascal- or BASIC-style declaration the target must be an unqualified identifier
				if (tokens.contains(".")) {
					throw new EvalError(control.msgConstantRecordComponent.getText().replace("%", target), null, null);
				}
				if (tokens.contains("[")) {
					throw new EvalError(control.msgConstantArrayElement.getText().replace("%", target), null, null);
				}
			}
			nTokens = tokens.count();
			target = tokens.get(nTokens-1);
			// START KGU#388 2017-09-18: Enh. #423 - Register the declared type
			associateType(target, typeDescr);
			// END KGU#388 2017-09-18
		}
		// Now it must be some C or Java style declaration or just a plain variable (possibly indexed or qualified or both)
		// START KGU#388 2017-09-14: Enh. #423 - We try recursively to track cases g) and h) down
//		else if (tokens.get(nTokens-1).equals("]")) {
//			// b) indexed variable or d) a C-style array declaration or g) or h)?
//			int posLBrack = tokens.indexOf("[");
//			if (posLBrack < 1) {
//				throw new EvalError(control.msgInvalidExpr.getText().replace("%1", tokens.concatenate(" ")), null, null);
//			}
//			else {
//				name = tokens.get(posLBrack-1);
//				if (posLBrack == 1) {
//					indexStr = tokens.concatenate(" ");
//					if (isConstant) {
//						throw new EvalError(control.msgConstantArrayElement.getText().replace("%", indexStr), null, null);
//					}
//				}
//			}
//		}
		// qualified or indexed or both?
		else if (tokens.indexOf(".") == 1 || tokens.get(nTokens-1).equals("]")) {
			// FIXME: Face a mixed encapsulation of arrays and records
			// In case of a record component access there must not be modifiers
			if (tokens.indexOf(".") == 1) {
				TypeMapEntry recordType = null;
				// The base variable name should be the last identifier in the series
				target = tokens.get(0);
				recordType = this.identifyRecordType(target, false);	// This will only differ from null if it's a record type
				recordName = target;
				// Now check recursively for record component names 
				while (recordType != null && nTokens >= 3 && tokens.get(1).equals(".") && Function.testIdentifier(tokens.get(2), null)) {
					LinkedHashMap<String, TypeMapEntry> comps = recordType.getComponentInfo(false);
					String compName = tokens.get(2);
					if (comps.containsKey(compName)) {
						// If this is in turn a record type, it may be going on recursively...
						target += "." + compName;
						compType = comps.get(compName);
						tokens.set(0, target);
						tokens.remove(1, 3);
						nTokens -= 2;
					}
					else {
						throw new EvalError(control.msgInvalidExpr.getText().replace("%1", target + "." + compName), null, null);
					}
				}
				if (isConstant) {
					throw new EvalError(control.msgConstantRecordComponent.getText().replace("%", target), null, null);
				}
				if (this.isConstant(recordName)) {
					throw new EvalError(control.msgConstantRedefinition.getText().replace("%", recordName), null, null);
				}
			}
			if (tokens.get(nTokens-1).equals("]")) {
				// b) indexed variable or d) a C-style array declaration?
				int posLBrack = tokens.indexOf("[");
				if (posLBrack < 1 || recordName != null && posLBrack > 1) {
					throw new EvalError(control.msgInvalidExpr.getText().replace("%1", tokens.concatenate(" ")), null, null);
				}
				else {
					target = tokens.get(posLBrack-1);
					if (posLBrack == 1) {
						indexStr = tokens.concatenate(" ");
						if (isConstant) {
							throw new EvalError(control.msgConstantArrayElement.getText().replace("%", indexStr), null, null);
						}
					}
				}
			}
		}
		// END KGU#388 2017-09-14
		else {
			// The standard case: a) or c)
			// START KGU#388 2017-09-18: Register the declared type if it's a defined type name
			if (nTokens == 2) {
				typeDescr = tokens.subSequence(0, nTokens - 1);
				associateType(tokens.get(1), typeDescr);
			}
			// END KGU#388 2017-09-18
			target = tokens.get(nTokens-1);
		}
		
		// ======== PHASE 2: Check of loop variable violations ===========
		
		// FIXME: target may still contain type and other modifiers, so this check might fail!
		// START KGU#307 2016-12-12: Enh. #307 - check FOR loop variable manipulation
		if (context.forLoopVars.lastIndexOf(target, ignoreLoopStackLevel) >= 0)
		{
			throw new EvalError(control.msgForLoopManipulation.getText().replace("%", target), null, null);
		}
		// END KGU#307 2016-12-12
		
		// ======== PHASE 3: Precautions against violation of constants ===========
		// START KGU#375 2017-03-30: Enh. #388 - check redefinition of constant
		if (this.isConstant(target)) {
			throw new EvalError(control.msgConstantRedefinition.getText().replace("%", target), null, null);
		}
		
		// Avoid sharing an array if the target is a constant (while the source may not be) 
		if (isConstant && content instanceof Object[]) {
			content = ((Object[])content).clone();
		}
		// END KGU#375 2017-03-30
		// START KGU#388 2017-09-14: Enh. #423
		if (isConstant && content instanceof HashMap<?,?>) {
			// FIXME: This is only a shallow copy, we might have to clone all values as well
			content = new HashMap<String, Object>((HashMap<String, ?>)content);
		}
		// END KGU#388 2017-09-14
		
		// MODIFIED BY GENNARO DONNARUMMA, ARRAY SUPPORT ADDED
		// Fundamentally revised by Kay Gürtzig 2015-11-08

		// START KGU#375 2017-03-30: Enh. #388 - replaced by preparing code above
//		String arrayname = null;
//		if ((name.contains("[")) && (name.contains("]")))
//		{
//			arrayname = name.substring(0, name.indexOf("["));
//			// START KGU#109 2015-12-16: Bugfix #61: Several strings suggest type specifiers
//			String[] nameParts = arrayname.split(" ");
//			arrayname = nameParts[nameParts.length-1];
//			// END KGU#109 2015-12-15
//		// START KGU#359 2017-03-06: Bugfix #369 for typed array initialisation like int a[3] <- {4, 9, 2}
//			if (nameParts.length > 1) {
//				// This is rather a C-style array declaration (initialized) than an array
//				// element assignment. The important question is now, whether the
//				// expression represents an array. Then we would drop the "index"
//				// (which is indeed a size) or check it against the array size.
//				name = arrayname;
//				arrayname = null;
//			}
//		}
//		if (arrayname != null) {
//		// Now all is fine here...
//		// END KGU#359 2017-03-06 
//			boolean arrayFound = this.variables.contains(arrayname);
//			int index = this.getIndexValue(name);
		
		// ======== PHASE 4: Structure-aware value assignment ===========

		// -------- Step 4 a: Array element assignment ----------------------- 
		if (indexStr != null) {
		// END KGU#375 2017-03-30
			boolean arrayFound = context.variables.contains(target);
			boolean componentArrayFound = compType != null && context.variables.contains(recordName) && compType.isArray();
			int index = this.getIndexValue(indexStr);
			Object[] objectArray = null;
			int oldSize = 0;
			if (arrayFound)
			{
				try {
					// If it hasn't been an array then we'll get an error here
					//objectArray = (Object[]) this.interpreter.get(arrayname);
					objectArray = (Object[]) context.interpreter.get(target);
					oldSize = objectArray.length;
				}
				catch (Exception ex)
				{
					// Produce a meaningful EvalError instead
					//this.interpreter.eval(arrayname + "[" + index + "] = " + prepareValueForDisplay(content));
					this.evaluateExpression(target + "[" + index + "] = " + prepareValueForDisplay(content), false);
				}
			}
			else if (componentArrayFound)
			{
				try {
					// If it hasn't been an array then we'll get an error here
					//objectArray = (Object[]) this.interpreter.get(arrayname);
					StringList path = StringList.explode(target, "\\.");
					Object comp = context.interpreter.get(path.get(0));
					for (int i = 0; i < path.count(); i++) {
						if (comp == null && i == path.count() - 2) {
							comp = this.createEmptyRecord(path, 0);
						}
						if (!(comp instanceof HashMap<?,?>)) {
							throw new EvalError(control.msgInvalidComponent.getText().replace("%1", path.get(i+1)).replace("%2", path.concatenate(".", 0, i)), null, null);
						}
						comp = ((HashMap<?, ?>)comp).get(path.get(i));
					}
					objectArray = (Object[])comp;
					oldSize = objectArray.length;
				}
				catch (Exception ex)
				{
					// Produce a meaningful EvalError instead
					//this.interpreter.eval(arrayname + "[" + index + "] = " + prepareValueForDisplay(content));
					this.evaluateExpression(target + "[" + index + "] = " + prepareValueForDisplay(content), false);
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
			//this.interpreter.set(arrayname, objectArray);
			//this.variables.addIfNew(arrayname);
			if (componentArrayFound) {
				try {
					StringList path = StringList.explode(target, "\\.");
					Object record = context.interpreter.get(recordName);
					Object comp = record;
					for (int i = 0; i < path.count()-1; i++) {
						if (comp == null && i == path.count() - 2) {
							comp = this.createEmptyRecord(path, i);
							if (i == 0) {
								record = comp;
							}
						}
						if (!(comp instanceof HashMap<?,?>)) {
							throw new EvalError(control.msgInvalidComponent.getText().
									replace("%1", path.get(i+1)).
									replace("%2", path.concatenate(".", 0, i+1)), null, null);
						}
						if (i < path.count()-2) {
							comp = ((HashMap)comp).get(path.get(i+1));
						}
					}
					((HashMap)comp).put(path.get(path.count()-1), objectArray);
					context.interpreter.set(recordName, record);
				}
				catch (Exception ex)
				{
					// Produce a meaningful EvalError instead
					//this.interpreter.eval(arrayname + "[" + index + "] = " + prepareValueForDisplay(content));
					this.evaluateExpression(target + "[" + index + "] = " + prepareValueForDisplay(content), false);
				}
				
			}
			else {
				context.interpreter.set(target, objectArray);
				context.variables.addIfNew(target);
			}
		}
		// START KGU#388 2017-09-14: Enh. #423 Special treatment for record components
		// -------- Step 4 b: Record component assignment -------------------- 
		else if (recordName != null) {
			StringList path = StringList.explode(target, "\\.");
			try {
				Object record = context.interpreter.get(recordName);
				if (record == null && path.count() == 2) {
					record = createEmptyRecord(path, 0);
				}
				Object comp = record;
				for (int i = 1; i < path.count()-1; i++) {
					Object subComp = ((HashMap<?, ?>)comp).get(path.get(i));
					if (subComp == null && i == path.count()-2) {
						// We tolerate that the penultimate level is unset...
						subComp = this.createEmptyRecord(path, i);
						((HashMap<String, Object>)comp).put(path.get(i), subComp);
					}
					else if (!(subComp instanceof HashMap<?,?>)) {
						throw new EvalError(control.msgInvalidComponent.getText().replace("%1", path.get(i-1)).replace("%2", path.concatenate(".",0,i-1)), null, null);
					}
					comp = subComp;
				}
				((HashMap<String, Object>)comp).put(path.get(path.count()-1), content);
				context.interpreter.set(recordName, record);
			}
			catch (EvalError ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new EvalError(ex.toString(), null, null);
			}
		}
		// END KGU#388 2017-09-14
		// -------- Step 4 c: assignment to a plain variable ----------------------- 
		else // indexString == null && recordName == null
		{
			// START KGU#375 2017-03-30: Enh. #388 - this has all been done already now
//			// START KGU#109 2015-12-16: Bugfix #61: Several strings suggest type specifiers
//			// START KGU#109 2016-01-15: Bugfix #61,#107: There might also be a colon...
//			int colonPos = name.indexOf(":");	// Check Pascal and BASIC style as well
//			if (colonPos > 0 || (colonPos = name.indexOf(" as ")) > 0)
//			{
//				name = name.substring(0, colonPos).trim();
//			}
//			// END KGU#109 2016-01-15
//			String[] nameParts = name.split(" ");
//			name = nameParts[nameParts.length-1];
//			// END KGU#109 2015-12-15
			// END KGU#375 2017-03-30

			// START KGU#388 2017-09-14: Enh. #423
			// Throw an error if a record is assigned to a used but undeclared, non-record or wrong-record-type variable
			// or vice versa 
			if (content instanceof HashMap<?,?>) {
				String typeName = ((HashMap<?, ?>)content).get("§TYPENAME§").toString();
				if ((context.variables.contains(target) || typeDescr != null)
						&& (!context.dynTypeMap.containsKey(target) || (compType = context.dynTypeMap.get(target)) == null || !compType.isRecord()
						|| !compType.typeName.equals(typeName))) {
					String compTypeStr = "???";
					if (compType != null) {
						compTypeStr = compType.getCanonicalType(true, true).replace("@", "array of ");
					}
					throw new EvalError(control.msgTypeMismatch.getText().
							replace("%1", ((HashMap<?, ?>)content).get("§TYPENAME§").toString()).
							replace("%2", compTypeStr).
							replace("%3", target), null, null);
				}
			}
			else if (content != null && (context.dynTypeMap.containsKey(target) && (compType = context.dynTypeMap.get(target)) != null
				|| typeDescr != null && typeDescr.count() == 1 && (compType = context.dynTypeMap.get("%" + typeDescr.get(0))) != null)
					&& compType.isRecord() ) {
				throw new EvalError(control.msgTypeMismatch.getText().
						replace("%1", content.toString()).
						replace("%2", compType.typeName).
						replace("%3", target), null, null);
			}

			// START KGU#322 2017-01-06: Bugfix #324 - an array assigned on input hindered scalar re-assignment
			//this.interpreter.set(name, content);
			try {
				context.interpreter.set(target, content);
			}
			catch (EvalError ex) {
				if (ex.getMessage().matches(".*Can't assign.*to java\\.lang\\.Object \\[\\].*")) {
					// Stored array type is an obstacle for re-assignment, so drop it
					context.interpreter.unset(target);
					// Now try again
					context.interpreter.set(target, content);
				}
				else {
					// Something different, so rethrow
					throw ex;
				}
			}
			// END KGU#322 2017-01-06
			
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
			if (! (content instanceof String || content instanceof Character || content instanceof Object[] || content instanceof HashMap<?,?>))
			{
				try {
					this.evaluateExpression(target + " = " + content, false);	// Avoid the variable content to be an object
				}
				catch (EvalError ex)	// Just ignore an error (if we may rely on the previously set content to survive)
				{}
			}
			// END KGU#99 2015-12-10
			context.variables.addIfNew(target);
			// START KGU#375 2017-03-30: Enh. #388
			if (isConstant) {
				context.constants.put(target, context.interpreter.get(target));
			}
			// END KGU#375 2017-03-30
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

	/**
	 * Detects whether the StringList {@code _typeDescr} specifies a defined type and if so
	 * associates the latter to the given variable or constant name {@code target} in {@code this.context.dynTypeMap}.
	 * @param target - a variable or constant identifier
	 * @param typeDescr - a {@link StringList} comprising a found type description
	 */
	private void associateType(String target, StringList typeDescr) {
		String typeName = null;
		if (typeDescr != null && typeDescr.count() == 1 && Function.testIdentifier(typeName = typeDescr.get(0), null)
				&& context.dynTypeMap.containsKey(":" + typeName)) {
			context.dynTypeMap.put(target, context.dynTypeMap.get(":" + typeName));
		}
		// In other cases we cannot create a new TypeMapEntry because we are lacking element and line information here.
		// So it is up to the calling method...
	}
	
	private HashMap<String, Object> createEmptyRecord(StringList path, int depth) {
		HashMap<String, Object> record = new HashMap<String, Object>();
		TypeMapEntry recordType = this.identifyRecordType(path.get(0), false);
		for (int i = 1; i <= depth; i++) {
			recordType = recordType.getComponentInfo(true).get(path.get(i));
		}
		return createEmptyRecord(recordType);
	}
	private HashMap<String, Object> createEmptyRecord(TypeMapEntry recordType) {
		HashMap<String, Object> record = new HashMap<String, Object>();
		for (String compName: recordType.getComponentInfo(true).keySet()) {
			record.put(compName, null);
		}
		record.put("§TYPENAME§", recordType.typeName);
		return record;
	}

	/**
	 * Checks if the name described by {@code varName} represents a record and if so
	 * returns the respective TypeMapEntry, otherwise null.
	 * @param typeOrVarName - a string sequence of modifiers, ids, and possible selectors 
	 * @param isTypeName TODO
	 * @return a TypeMapEntry for a record type or null
	 */
	private TypeMapEntry identifyRecordType(String typeOrVarName, boolean isTypeName)
	{
		TypeMapEntry recordType = context.dynTypeMap.get((isTypeName ? ":" : "") + typeOrVarName);
		
		if (recordType != null && !recordType.isRecord()) {
				recordType = null;
		}
		
		return recordType;
	}

	// START KGU#20 2015-10-13: Code from above moved hitherto and formed to a method
	/**
	 * Prepares an editable variable table and has the Control update the display
	 * of variables with it
	 */
	private void updateVariableDisplay() throws EvalError
	{
		Vector<Vector<Object>> vars = new Vector<Vector<Object>>();
		for (int i = 0; i < context.variables.count(); i++)
		{
			Vector<Object> myVar = new Vector<Object>();
			myVar.add(context.variables.get(i));	// Variable name
			// START KGU#67 2015-11-08: We had to find a solution for displaying arrays in a sensible way
			//myVar.add(this.interpreter.get(this.variables.get(i)));
			Object val = context.interpreter.get(context.variables.get(i));
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
			// START KGU#388 2017-09-14: Enh. #423
			if (val.getClass().getSimpleName().equals("HashMap"))
			{
				valStr = ((HashMap)val).get("§TYPENAME§") + "{";
				int j = 0;
				for (Object entry: ((HashMap)val).entrySet())
				{
					String key = (String)((Entry)entry).getKey();
					if (!key.startsWith("§")) {
						String elementStr = prepareValueForDisplay(((Entry)entry).getValue());
						valStr = valStr + ((j++ > 0) ? ", " : "") + key + ": " + elementStr;
					}
				}
				valStr = valStr + "}";
			}
			// END KGU#388 2017-09-14
			else if (val instanceof String)
			{
				// START KGU#285 2016-10-16: Bugfix #276
				valStr = valStr.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
				// END KGU#285 2016-10-16
				valStr = "\"" + valStr + "\"";
			}
			else if (val instanceof Character)
			{
				// START KGU#285 2016-10-16: Bugfix #276
				valStr = valStr.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
				// END KGU#285 2016-10-16
				valStr = "'" + valStr + "'";
			}
		}
		return valStr;
	}
	// END KGU#67/KGU#68 2015-11-08
	
	// START KGU#68 2015-11-06 - modified 2016-10-07 for improved thread-safety
	public void adoptVarChanges(HashMap<String,Object> newValues)
	{
		String tmplManuallySet = control.lbManuallySet.getText();	// The message template
		for (HashMap.Entry<String, Object> entry: newValues.entrySet())
		{
			try {
				String varName = entry.getKey();
				Object oldValue = context.interpreter.get(varName);
				Object newValue = entry.getValue();
				// START KGU#160 2016-04-12: Enh. #137 - text window output
				// START KGU#197 2016-05-05: Language support extended
				//this.console.writeln("*** Manually set: " + varName + " <- " + newValues[i] + " ***", Color.RED);
				this.console.writeln(tmplManuallySet.replace("%1", varName).replace("%2", newValue.toString()), Color.RED);
				// END KGU#197 2016-05-05
				if (isConsoleEnabled)
				{
					this.console.setVisible(true);
				}
				// END KGU#160 2016-04-12

				if (oldValue != null && oldValue.getClass().getSimpleName().equals("Object[]"))
				{
					// In this case an initialisation expression ("{ ..., ..., ...}") is expected
					String asgnmt = "Object[] " + varName + " = " + newValue;
					// FIXME: Nested initializers (as produced for nested arrays before) won't work here!
					this.evaluateExpression(asgnmt, false);
//					// Okay, but now we have to sort out some un-boxed strings
//					Object[] objectArray = (Object[]) interpreter.get(varName);
//					for (int j = 0; j < objectArray.length; j++)
//					{
//						Object content = objectArray[j];
//						if (content != null)
//						{
//							System.out.println("Updating " + varName + "[" + j + "] = " + content.toString());
//							this.interpreter.set("structorizer_temp", content);
//							this.interpreter.eval(varName + "[" + j + "] = structorizer_temp");
//						}
//					}
				}
				else
				{
					setVarRaw(varName, (String)newValue);
				}
			}
			catch (EvalError err) {
				System.err.println("Executor.adoptVarChanges(" + newValues + "): " + err.getMessage());
			}
		}
	}
	// END KGU#68 2015-11-06
	
	// START KGU#375 2017-03-30: Auxiliary callback for Control
	public boolean isConstant(String varName)
	{
		return context.constants.containsKey(varName.trim());
	}
	// END KGU#375 2017-03-30

	public void start(boolean useSteps)
	{
		paus = useSteps;
		step = useSteps;
		stop = false;
		// START KGU#384 2017-04-22: execution context redesign - no longer an attribute
		//this.variables = new StringList();
		// END KGU#384 2017-04-22
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
		// START KGU#276 2016-11-19: Issue #267: in paused mode we should move the focus to the current element
		if (delay > 0 || step || atBreakpoint) {
			diagram.redraw(element);
		}
		// END KGU#276 2016-11-19
		if (atBreakpoint) {
			// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
			//control.setButtonsForPause();
			control.setButtonsForPause(false);	// This avoids interference with the pause button
			// END KGU#379 2017-04-12
			this.setPaus(true);
		}
		return atBreakpoint;
	}
	// END KGU#43 2015-10-12

	// START KGU 2015-10-13: Decomposed this "monster" method into Element-type-specific subroutines
	private String step(Element element)
	{
		String result = new String();
		// START KGU#277 2016-10-13: Enh. #270: skip the element if disabled
		if (element.disabled) {
			return result;
		}
		// END KGU#277 2016-10-13
		
		element.executed = true;
		// START KGU#276 2016-10-09: Issue #269: Now done in checkBreakpoint()
//		if (delay != 0 || step)
//		{
//			diagram.redraw();
//		}
//		if (step) {
//			diagram.redraw(element);	// Doesn't work properly...
//		}
//		else if (delay != 0) {
//			diagram.redraw();
//		}
		// END KGU#276 2016-10-09
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
//		delay();
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

		// START KGU#413 2017-06-09: Enh. #416 allow user-defined line concatenation
		//StringList sl = element.getText();
		StringList sl = element.getUnbrokenText();
		// END KGU#413 2017-06-09
		int i = 0;

		// START KGU#77/KGU#78 2015-11-25: Leave if some kind of leave statement has been executed
		//while ((i < sl.count()) && result.equals("") && (stop == false))
		while ((i < sl.count()) && result.equals("") && (stop == false) &&
				!context.returned && leave == 0)
		// END KGU#77/KGU#78 2015-11-25
		{
			String cmd = sl.get(i);
			// START KGU#388 2017-09-13: Enh. #423 We shouldn't do this for type definitions
			//cmd = convert(cmd).trim();
			// END KGU#388 2017-09-13
			try
			{
				// START KGU#271: 2016-10-06: Bugfix #269 - this was mis-placed here and had to go to the loop body end 
//				if (i > 0)
//				{
//					delay();
//				}
				// END KGU#271 2016-10-06
				
				// START KGU#388 2017-09-13: Enh. #423 We shouldn't do this for type definitions
				if (!Instruction.isTypeDefinition(cmd)) {
					cmd = convert(cmd).trim();
				// END KGU#388 2017-09-13

					// START KGU#417 2017-06-30: Enh. #424 (Turtleizer functions introduced) 
					cmd = this.evaluateDiagramControllerFunctions(cmd);
					// END KGU#417 2017-06-30

					// assignment?
					// START KGU#377 2017-03-30: Bugfix
					//if (cmd.indexOf("<-") >= 0)
					if (Element.splitLexically(cmd, true).contains("<-"))
						// END KGU#377 2017-03-30: Bugfix
					{
						result = tryAssignment(cmd, element, i);
					}
					// input
					// START KGU#65 2015-11-04: Input keyword should only trigger this if positioned at line start
					//else if (cmd.indexOf(CodeParser.input) >= 0)
					else if (cmd.matches(
							this.getKeywordPattern(CodeParser.getKeyword("input")) + "([\\W].*|$)"))
						// END KGU#65 2015-11-04
					{
						result = tryInput(cmd);
					}
					// output
					// START KGU#65 2015-11-04: Output keyword should only trigger this if positioned at line start
					//else if (cmd.indexOf(CodeParser.output) >= 0)
					else if (cmd.matches(
							this.getKeywordPattern(CodeParser.getKeyword("output")) + "([\\W].*|$)"))
						// END KGU#65 2015-11-04
					{
						result = tryOutput(cmd);
					}
					// return statement
					// START KGU 2015-11-28: The "return" keyword ought to be the first word of the instruction,
					// comparison should not be case-sensitive while CodeParser.preReturn isn't fully configurable,
					// but a separator would be fine...
					//else if (cmd.indexOf("return") >= 0)
					else if (cmd.matches(
							this.getKeywordPattern(CodeParser.getKeywordOrDefault("preReturn", "return")) + "([\\W].*|$)"))
						// END KGU 2015-11-11
					{		 
						result = tryReturn(cmd.trim());
					}
					// START KGU#332 2017-01-17/19: Enh. #335 - tolerate a Pascal variable declaration
					else if (cmd.matches("^var.*:.*")) {
						// START KGU#388 2017-09-14: Enh. #423
						element.updateTypeMapFromLine(this.context.dynTypeMap, cmd, i);
						// END KGU#388 2017-09-14
						StringList varNames = StringList.explode(cmd.substring("var".length(), cmd.indexOf(":")), ",");
						for (int j = 0; j < varNames.count(); j++) {
							setVar(varNames.get(j).trim(), null);
						}
					}
					else if (cmd.matches("^dim.* as .*")) {
						// START KGU#388 2017-09-14: Enh. #423
						element.updateTypeMapFromLine(this.context.dynTypeMap, cmd, i);
						// END KGU#388 2017-09-14
						StringList varNames = StringList.explode(cmd.substring("dim".length(), cmd.indexOf(" as ")), ",");
						for (int j = 0; j < varNames.count(); j++) {
							setVar(varNames.get(j), null);
						}
					}
					// END KGU#332 2017-01-17/19
					else
					{
						result = trySubroutine(cmd, element);
					}
					// START KGU#156 2016-03-11: Enh. #124
					element.addToExecTotalCount(1, true);	// For the instruction line
					//END KGU#156 2016-03-11
					
				// START KGU#388 2017-09-13: Enh. #423
				}
				else {
					element.updateTypeMapFromLine(this.context.dynTypeMap, cmd, i);
				}
				// END KGU#388 2017-09-13
				// START KGU#271: 2016-10-06: Bugfix #261: Allow to step and stop within an instruction block (but no breakpoint here!) 
				if ((i+1 < sl.count()) && result.equals("") && (stop == false)
						&& !context.returned && leave == 0)
				{
					delay();
				}
				// END KGU#271 2016-10-06
			} catch (EvalError ex)
			{
				result = ex.getLocalizedMessage();
				if (result == null) result = ex.getMessage();
				if (result.endsWith("TargetError")) {
					String errorText = ex.getErrorText();
					int leftParPos = errorText.indexOf('(');
					int rightParPos = errorText.lastIndexOf(')');
					if (errorText.startsWith("throw") && leftParPos >= 0 && rightParPos > leftParPos) {
						errorText = errorText.substring(leftParPos+1,  rightParPos).trim();
					}
					result = result.replace("TargetError", errorText);
				}
			}
			catch (Exception ex)
			{
				result = ex.getLocalizedMessage();
				if (result == null) result = ex.getMessage();
			}
			i++;
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

		// START KGU#413 2017-06-09: Enh. #416 allow user-defined line concatenation
		//StringList sl = element.getText();
		StringList sl = element.getUnbrokenText();
		// END KGU#413 2017-06-09
		int i = 0;

		// START KGU#117 2016-03-10: Enh. #77
		boolean wasSimplyCovered = element.simplyCovered;
		boolean wasDeeplyCovered = element.deeplyCovered;
		boolean allSubroutinesCovered = true;
		// END KGU#117 2016-03-10

		// START KGU#77 2015-11-11: Leave if a return statement has been executed
		//while ((i < sl.count()) && result.equals("") && (stop == false))
		while ((i < sl.count()) && result.equals("") && (stop == false) && !context.returned)
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

				// START KGU#417 2017-06-30: Enh. #424
				cmd = this.evaluateDiagramControllerFunctions(cmd);
				// END KGU#417 2017-06-30

				// assignment?
				// START KGU#377 2017-03-30: Bugfix
				//if (cmd.indexOf("<-") >= 0)
				if (Element.splitLexically(cmd, true).contains("<-"))
				// END KGU#377 2017-03-30: Bugfix
				{
					result = tryAssignment(cmd, element, i);
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

	// START KGU#78 2015-11-25: Separate dedicated implementation for JUMPs
	private String stepJump(Jump element)
	{
		String result = new String();

		// START KGU#413 2017-06-09: Enh. #416 allow user-defined line concatenation
		//StringList sl = element.getText();
		StringList sl = element.getUnbrokenText();
		// END KGU#413 2017-06-09
		boolean done = false;

		// START KGU#380 2017-04-14: #394 Radically rewritten and simplified (no multi-line evaluation anymore)
		// Leave?
		if (element.isLeave()) {
			int nLevels = element.getLevelsUp();
			if (nLevels < 1) {
				String argument = sl.get(0).trim().substring(CodeParser.getKeyword("preLeave").length()).trim();
				result = control.msgIllegalLeave.getText().replace("%1", argument);				
			}
			else {
				this.leave += nLevels;
				done = true;
			}
		}
		// Unstructured return?
		else if (element.isReturn()) {
			try {
				// START KGU#417 2017-06-30: Enh. #424
				//result = tryReturn(convert(sl.get(0)));
				String cmd = convert(sl.get(0));
				cmd = this.evaluateDiagramControllerFunctions(cmd);
				result = tryReturn(cmd);
				// END KGU#417 2017-06-30
				done = true;			
			}
			catch (Exception ex)
			{
				result = ex.getLocalizedMessage();
				if (result == null) result = ex.getMessage();
			}
		}
		// Exit from entire program?
		else if (element.isExit()) {
			StringList tokens = Element.splitLexically(sl.get(0).trim(), true);
			// START KGU#365/KGU#380 2017-04-14: Issues #380, #394 Allow arbitrary integer expressions now
			//tokens.removeAll("");
			tokens.remove(0);	// Get rid of the keyword...
			String expr = tokens.concatenate();
			// END KGU#380 2017-04-14
			// Get exit value
			int exitValue = 0;
			try {
				// START KGU 2017-04-14: #394 Allow arbitrary integer expressions now
				//Object n = interpreter.eval(tokens.get(1));
				// START KGU#417 2017-06-30: Enh. #424
				expr = this.evaluateDiagramControllerFunctions(expr);
				// END KGU#417 2017-06-30
				Object n = this.evaluateExpression(expr, false);
				// END KGU 2017-04-14
				if (n instanceof Integer)
				{
					exitValue = ((Integer) n).intValue();
				}
				else
				{
					// START KGU#197 2016-07-27: More localization support
					//result = "Inappropriate exit value: <" + (n == null ? tokens.get(1) : n.toString()) + ">";
					result = control.msgWrongExit.getText().replace("%1",
							"<" + (n == null ? expr : n.toString()) + ">");
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
		else
		{
			// START KGU#197 2016-07-27: More localization support
			//result = "Illegal content of a Jump (i.e. exit) instruction: <" + cmd + ">!";
			result = control.msgIllegalJump.getText().replace("%1", sl.concatenate(" <nl> "));
			// END KGU#197 2016-07-27
		}
		// END KGU#380 2017-04-14
			
		if (done && leave > context.loopDepth)
		{
			// START KGU#197 2016-07-27: More localization support
			result = "Too many levels to leave (actual depth: " + context.loopDepth + " / specified: " + leave + ")!";
			result = control.msgTooManyLevels.getText().
					replace("%1", Integer.toString(context.loopDepth)).
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
	
	// START KGU#417 2017-06-29: Enh. #424 New mechanism to pre-evaluate Turtleizer functions
	private String evaluateDiagramControllerFunctions(String expression) throws EvalError
	{
		if (diagramController != null && diagramController instanceof FunctionProvidingDiagramController) {
			// Now, several ones of the functions offered by diagramController might
			// occur at different nesting depths in the expression. So we must find
			// and evaluate them from innermost to outermost.
			// We advance from right to left, this way we will evaluate deeper nested
			// functions first.
			// Begin with collecting all possible occurrence positions
			StringList tokens = Element.splitLexically(expression, true);
			LinkedList<Integer> positions = new LinkedList<Integer>();
			HashMap<String, Class<?>[]> funcMap = ((FunctionProvidingDiagramController)diagramController).getFunctionMap();
			for (Entry<String, Class<?>[]> entry: funcMap.entrySet()) {
				int pos = -1;
				while ((pos = tokens.indexOf(entry.getKey(), pos+1, false)) >= 0) {
					positions.add(pos);
				}
			}
			// 
			positions.sort(java.util.Collections.reverseOrder());
			Iterator<Integer> iter = positions.iterator();
			try {
				while (iter.hasNext()) {
					int pos = iter.next();
					String fName = tokens.get(pos).toLowerCase();
					String exprTail = tokens.concatenate("", pos + 1).trim();
					if (exprTail.startsWith("(")) {
						StringList args = Element.splitExpressionList(exprTail.substring(1), ",");
						Class<?>[] signature = funcMap.get(fName);
						int nArgs = args.count();
						if (nArgs == signature.length - 1) {
							String tail = "";
							StringList parts = Element.splitExpressionList(exprTail.substring(1), ",", true);
							if (parts.count() > nArgs) {
								tail = parts.get(parts.count()-1).trim();
							}
							Object argVals[] = new Object[nArgs];
							for (int i = 0; i < nArgs; i++) {
								Object val = this.evaluateExpression(args.get(i), false);
								try {
									signature[i].cast(val);
								}
								catch (Exception ex) {
									EvalError err = new EvalError("Function <" + fName + "> argument "
											+ (i+1) + ": <" + args.get(i) + "> could not be converted to "
											+ signature[i].getSimpleName(), null, null);
									err.setStackTrace(ex.getStackTrace());
									throw err;
								}
							}
							// Passed till here, we try to execute the function - this may throw a FunctionException
							Object result = ((FunctionProvidingDiagramController)diagramController).execute(fName, argVals);
							tokens.remove(pos, tokens.count());
							tokens.add(signature[signature.length-1].cast(result).toString());
							if (!tail.isEmpty()) {
								tokens.add(Element.splitLexically(tail.substring(1), true));
							}
						}
					}
				}
			}
			catch (EvalError ex) {
				throw ex;
			}
			catch (Exception ex) {
				// Convert other errors into EvalError
				EvalError err = new EvalError(ex.toString(), null, null);
				err.setStackTrace(ex.getStackTrace());
				throw err;
			}

			expression = tokens.concatenate();
		}
		return expression;
	}
	// END KGU#417 2017-06-29
	
	// START KGU 2015-11-11: Equivalent decomposition of method stepInstruction
	/**
	 * Submethod of stepInstruction(Instruction element), handling an assignment.
	 * Also updates the dynamic type map. 
	 * @param cmd - the (assignment) instruction line, may also contain declarative parts
	 * @param instr - the Instruction element
	 * @param lineNo - the line number of the current assignment (for the type resgistration)
	 * @return a possible error message (for errors not thrown as EvalError)
	 * @throws EvalError
	 */
	private String tryAssignment(String cmd, Instruction instr, int lineNo) throws EvalError
	{
		String result = "";
		Object value = null;
		// KGU#2: In case of a Call element, we allow an assignment with just the subroutine call on the
		// right-hand side. This makes it relatively easy to detect and prepare the very subroutine call,
		// in contrast to possible occurrences of such foreign function calls at arbitrary expression depths,
		// combined, nested etc.
		// START KGU#375 2017-03-30: Enh. #388 - be constant-aware (clone constant arrays in the expression)
//		String varName = cmd.substring(0, cmd.indexOf("<-")).trim();
//		String expression = cmd.substring(
//				cmd.indexOf("<-") + 2, cmd.length()).trim();
		StringList tokens = Element.splitLexically(cmd, true);
		int posAsgnOpr = tokens.indexOf("<-");
		String leftSide = tokens.subSequence(0, posAsgnOpr).concatenate().trim();
		tokens.remove(0, posAsgnOpr+1);
		// START KGU#388 2017-09-13: Enh. #423 support records
		while (tokens.count() > 0 && tokens.get(0).trim().isEmpty()) {
			tokens.remove(0);
		}
		TypeMapEntry recordType = null;
		// END KGU#388 2017-09-13
		// Watch out for constant arrays or records
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			Object constVal = context.constants.get(token);
			if (constVal instanceof Object[]) {
				// Let a constant array be replaced by its clone, so we avoid structure
				// sharing, which would break the assurance of constancy.
				tokens.set(i, "copyArray(" + token + ")");
			}
			// START KGU#388 2017-09-13: Enh. #423 support records, too
			else if (constVal instanceof HashMap<?, ?>) {
				// Let a constant record be replaced by its clone, so we avoid structure
				// sharing, which would break the assurance of constancy.
				tokens.set(i, "copyRecord(" + token + ")");
			}
			// END KGU#388 2017-09-13
		}
		String expression = tokens.concatenate().trim();
		// END KGU#375 2017-03-30
		// START KGU#2 2015-10-18: cross-NSD subroutine execution?
		if (instr instanceof Call)
		{
			Function f = new Function(expression);
			if (f.isFunction())
			{
				//System.out.println("Looking for SUBROUTINE NSD:");
				//System.out.println("--> " + f.getName() + " (" + f.paramCount() + " parameters)");
				// START KGU#317 2016-12-29
				//Root sub = this.findSubroutineWithSignature(f.getName(), f.paramCount());
				Root sub = null;
				try {
					sub = this.findSubroutineWithSignature(f.getName(), f.paramCount());
				} catch (Exception ex) {
					return ex.getMessage();	// Ambiguous call!
				}
				// END KGU#317 2016-12-29
				if (sub != null)
				{
					Object[] args = new Object[f.paramCount()];
					for (int p = 0; p < f.paramCount(); p++)
					{
						args[p] = this.evaluateExpression(f.getParam(p), false);
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
							replace("\\n", "\n");
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
		// Now evaluate the expression
		// START KGU#426 2017-09-30: Enh. #48, #423, bugfix #429 we need this code e.g in tryReturn, too
//		// FIXME: This should be recursive!
//		// START KGU#100 2016-01-14: Enh. #84 - accept array assignments with syntax array <- {val1, val2, ..., valN}
//		else if (expression.startsWith("{") && expression.endsWith("}"))
//		{
//			// FIXME: We might have to evaluate those element values in advance, which are initializers themselves...
//			this.evaluateExpression("Object[] tmp20160114kgu = " + expression, false);
//			value = context.interpreter.get("tmp20160114kgu");
//			context.interpreter.unset("tmp20160114kgu");
//		}
//		// END KGU#100 2016-01-14
//		// START KGU#388 2017-09-13: Enh. #423 - accept record assignments with syntax recordVar <- typename{comp1: val1, comp2: val2, ..., compN: valN}
//		else if (tokens.indexOf("{") == 1 && expression.endsWith("}")
//				&& (recordType = identifyRecordType(tokens.get(0), true)) != null)
//		{
//			this.evaluateExpression("HashMap tmp20170913kgu = new HashMap()", false);
//			HashMap<String, String> components = Element.splitRecordInitializer(expression);
//			if (components == null || components.containsKey("§TAIL§")) {
//				result = control.msgInvalidExpr.getText().replace("%1", expression);
//			}
//			else {
//				components.remove("§TYPENAME§");
//				LinkedHashMap<String, TypeMapEntry> compDefs = recordType.getComponentInfo(false);
//				for (Entry<String, String> comp: components.entrySet()) {
//					// FIXME: We might have to evaluate the component value in advance if it is an initializer itself...
//					if (compDefs.containsKey(comp.getKey())) {
//						context.interpreter.eval("tmp20170913kgu.put(\"" + comp.getKey() + "\", " + comp.getValue() + ");");
//					}
//					else {
//						result = control.msgInvalidComponent.getText().replace("%1", comp.getKey()).replace("%2", recordType.typeName);
//						break;
//					}
//				}
//				value = context.interpreter.get("tmp20170913kgu");
//				if (value instanceof HashMap<?,?>) {
//					((HashMap<String, Object>)value).put("§TYPENAME§", recordType.typeName);
//				}
//				context.interpreter.unset("tmp20170913kgu");
//			}
//		}
//		// END KGU#388 2017-09-13
//		else
//		{
//			//cmd = cmd.replace("<-", "=");
//		
//			value = this.evaluateExpression(expression);
//		}
		else
		{
			value = this.evaluateExpression(expression, true);
		}
		// END KGU#426 2017-09-30
		
		if (value != null)
		{
			// Assign the value and handle provided declaration
			setVar(leftSide, value);
			// START KGU#388 2017-09-14. Enh. #423
			// FIXME: This is poorly done, particularly we must handle cases of record assignment 
			//instr.updateTypeMapFromLine(context.dynTypeMap, cmd, lineNo);
			if (!leftSide.contains(".") && !leftSide.contains("[")) {
				TypeMapEntry oldEntry = null;
				String target = instr.getAssignedVarname(Element.splitLexically(leftSide, true)) + "";
				if (!context.dynTypeMap.containsKey(target) || !(oldEntry = context.dynTypeMap.get(target)).isDeclared) {
					String typeDescr = Instruction.identifyExprType(context.dynTypeMap, expression, true);
					if (oldEntry == null) {
						TypeMapEntry typeEntry = null;
						if (typeDescr != null && (typeEntry = context.dynTypeMap.get(":" + typeDescr)) == null) {
							typeEntry = new TypeMapEntry(typeDescr, null, instr, lineNo, true, false, false);
						}
						context.dynTypeMap.put(target, typeEntry);
					}
					else {
						oldEntry.addDeclaration(typeDescr, instr, lineNo, true, false);
					}
				}
			}
			// END KGU#388 2017-09-14
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
		String in = cmd.substring(CodeParser.getKeyword("input").trim().length()).trim();
		// START KGU#281: Enh. #271: Input prompt handling
		String prompt = null;
		if (in.startsWith("\"") || in.startsWith("\'")) {
			StringList tokens = Element.splitLexically(in, true);
			String delim = tokens.get(0).substring(0,1);
			if (tokens.get(0).endsWith(delim))
			{
				prompt = tokens.get(0).substring(1, tokens.get(0).length()-1);
				// START KGU#285 2016-10-16: Bugfix #276 - We should interpret contained escape sequences...
				try {
					String dummyVar = "prompt" + this.hashCode();
					this.evaluateExpression(dummyVar + "=\"" + prompt + "\"", false);
					Object res = context.interpreter.get(dummyVar);
					if (res != null) {
						prompt = res.toString();
					}
					context.interpreter.unset(dummyVar);
				}
				catch (EvalError ex) {}
				// END KGU#285 2016-10-16
    			// START KGU#281 2016-12-23: Enh. #271 - ignore comma between prompt and variable name
				if (tokens.count() > 1 && tokens.get(1).equals(",")) {
					tokens.remove(1);
				}
				// END KGU#281 2016-12-23
				in = tokens.concatenate("", 1).trim();
			}
		}
		// END KGU#281 2016-10-12
		// START KGU#107 2015-12-13: Enh-/bug #51: Handle empty input instruction
		if (in.isEmpty())
		{
			// In run mode, give the user a chance to intervene
			Object[] options = {
					control.lbOk.getText(),
					control.lbPause.getText()
					};
			int pressed = JOptionPane.showOptionDialog(diagram.getParent(), control.lbAcknowledge.getText(), control.lbInput.getText(),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
			if (pressed == 1)
			{
				synchronized(this)
				{
					paus = true;
					step = true;
				}
				// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
				//control.setButtonsForPause();
				control.setButtonsForPause(false);	// This avoids interference with the pause button
				// END KGU#379 2017-04-12
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
			// START KGU#375 2017-03-30: Enh. #388 - support of constants
			if (this.isConstant(in)) {
				result = control.msgConstantRedefinition.getText().replaceAll("%", in);
			}
			// END KGU#375 2017-03-30
			// START KGU#141 2016-01-16: Bugfix #112 - nothing more to do than exiting
			if (!result.isEmpty())
			{
				return result;
			}
			// END KGU#141 2016-01-16
			// START KGU#89 2016-03-18: More language support 
			//String str = JOptionPane.showInputDialog(null,
			//		"Please enter a value for <" + in + ">", null);
			// START KGU#281 2016-10-12: Enh. #271
			//String msg = control.lbInputValue.getText();
			//msg = msg.replace("%", in);
			if (prompt == null) {
				prompt = control.lbInputValue.getText();				
				prompt = prompt.replace("%", in);
			}
			// END KGU#281 2016-10-12
			// START KGU#160 2016-04-12: Enh. #137 - text window output
			this.console.write(prompt + (prompt.trim().endsWith(":") ? " " : ": "), Color.YELLOW);
			if (isConsoleEnabled)
			{
				this.console.setVisible(true);
			}
			// END KGU#160 2016-04-12
			String str = JOptionPane.showInputDialog(diagram.getParent(), prompt, null);
			// END KGU#89 2016-03-18
			// START KGU#84 2015-11-23: ER #36 - Allow a controlled continuation on cancelled input
			//setVarRaw(in, str);
			if (str == null)
			{
				// Switch to step mode such that the user may enter the variable in the display and go on
				// START KGU#197 2016-05-05: Issue #89
				//JOptionPane.showMessageDialog(diagram, "Execution paused - you may enter the value in the variable display.",
				//		"Input cancelled", JOptionPane.WARNING_MESSAGE);
				JOptionPane.showMessageDialog(control, control.lbInputPaused.getText(),
						control.lbInputCancelled.getText(), JOptionPane.WARNING_MESSAGE);
				// START KGU#197 2016-05-05
				synchronized(this)
				{
					paus = true;
					step = true;
				}
				// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
				//control.setButtonsForPause();
				control.setButtonsForPause(false);	// This avoids interference with the pause button
				// END KGU#379 2017-04-12
				if (!context.variables.contains(in))
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
		String out = cmd.substring(/*cmd.indexOf(CodeParser.output) +*/
						CodeParser.getKeyword("output").trim().length()).trim();
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
				Object n = this.evaluateExpression(out, false);
				if (n == null)
				{
					result = control.msgInvalidExpr.getText().replace("%1", out);
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
				// diagram is a bad anchor component since its extension is the Root rectangle (may be huge!)
				JOptionPane.showMessageDialog(diagram.getParent(), s, control.lbOutput.getText(),
						JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				// In run mode, give the user a chance to intervene
				Object[] options = {
						control.lbOk.getText(),
						control.lbPause.getText()
						};
				// diagram is a bad anchor component since its extension is the Root rectangle (may be huge!)
				int pressed = JOptionPane.showOptionDialog(diagram.getParent(), s, control.lbOutput.getText(),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
				if (pressed == 1)
				{
					synchronized(this)
					{
						paus = true;
						step = true;
					}
					// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
					//control.setButtonsForPause();
					control.setButtonsForPause(false);	// This avoids interference with the pause button
					// END KGU#379 2017-04-12
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
		String out = cmd.substring(CodeParser.getKeywordOrDefault("preReturn", "return").length()).trim();
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
			// START KGU#426 2017-09-30: Bugfix #429
			//resObj = this.evaluateExpression(out);
			resObj = this.evaluateExpression(out, true);
			// END KGU#426 2017-09-30
			// If this diagram is executed at top level then show the return value
			if (this.callers.empty())
			{
				if (resObj == null)
				{
					result = control.msgInvalidExpr.getText().replace("%1", out);
				// START KGU#133 2016-01-29: Arrays should be presented as scrollable list
				} else if (resObj instanceof Object[])
				{
					showArray((Object[])resObj, header, !step);
				} else if (step)
				{
					// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
					this.console.writeln("*** " + header + ": " + this.prepareValueForDisplay(resObj), Color.CYAN);
					// END KGU#160 2016-04-26
					// START KGU#147 2016-01-29: This "unconverting" copied from tryOutput() didn't make sense...
					//String s = unconvert(resObj.toString());
					//JOptionPane.showMessageDialog(diagram, s,
					//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
					JOptionPane.showMessageDialog(diagram.getParent(), resObj,
							header, JOptionPane.INFORMATION_MESSAGE);
					// END KGU#147 2016-01-29					
				// END KGU#133 2016-01-29
				} else
				{
					// START KGU#198 2016-05-25: Issue #137 - also log the result to the console
					this.console.writeln("*** " + header + ": " + this.prepareValueForDisplay(resObj), Color.CYAN);
					// END KGU#198 2016-05-25
					// START KGU#84 2015-11-23: Enhancement to give a chance to pause (though of little use here)
					Object[] options = {
							control.lbOk.getText(),
							control.lbPause.getText()
							};
					int pressed = JOptionPane.showOptionDialog(diagram.getParent(), resObj, header,
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
					if (pressed == 1)
					{
						synchronized(this) {
							paus = true;
							step = true;
						}
						// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
						//control.setButtonsForPause();
						control.setButtonsForPause(false);	// This avoids interference with the pause button
						// END KGU#379 2017-04-12
					}
					// END KGU#84 2015-11-23
				}
			}
		}
		
		context.returnedValue = resObj;
		// END KGU#77 (#21) 2015-11-13
		context.returned = true;
		return result;
	}

	// Submethod of stepInstruction(Instruction element), handling a function call
	private String trySubroutine(String cmd, Instruction element) throws EvalError
	{
		String result = "";
		Function f = new Function(cmd);
		if (f.isFunction())
		{
			String procName = f.getName();
			String params = new String();	// List of evaluated arguments
			Object[] args = new Object[f.paramCount()];
			for (int p = 0; p < f.paramCount(); p++)
			{
				try
				{
					args[p] = this.evaluateExpression(f.getParam(p), false);
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
				// START KGU#317 2016-12-29: Abort execution on ambiguous calls
				//Root sub = this.findSubroutineWithSignature(f.getName(), f.paramCount());
				Root sub = null;
				try {
					sub = this.findSubroutineWithSignature(procName, f.paramCount());
				} catch (Exception ex) {
					return ex.getMessage();	// Ambiguous call!
				}
				// END KGU#317 2016-12-29
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
					// START KGU#197 2016-07-27: Now translatable message
					//result = "A subroutine diagram " + f.getName() + " (" + f.paramCount() + 
					//		" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";
					result = control.msgNoSubroutine.getText().
							replace("%1", procName).
							replace("%2", Integer.toString(f.paramCount())).
							replace("\\n", "\n");
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
						// Cut off the leading comma from the list of evaluated arguments
						params = params.substring(1);
					}
					cmd = procName.toLowerCase() + "(" + params + ")";
					result = getExec(cmd, element.getColor());
				} 
//				else if (CodeParser.ignoreCase) {
//					// Try as built-in subroutine with aligned case
//					// FIXME: This does not recursively adapt the names! (So what for at all?)
//					interpreter.eval(f.getInvokation(true));
//				}
				else	
				{
					// Try as built-in subroutine as is
					this.evaluateExpression(cmd, false);
				}
			}
		}
		// START KGU#376 2017-04-11: Enh. #389 - withdrawn 2017-07-01
//		else if (element instanceof Call && element.isImportCall()) {
//			Root imp = null;
//			String diagrName = ((Call)element).getSignatureString();
//			try {
//				imp = this.findProgramWithName(diagrName);
//			} catch (Exception ex) {
//				return ex.getMessage();	// Ambiguous call!
//			}
//			// END KGU#317 2016-12-29
//			if (imp != null)
//			{
//				// START KGU#376 207-04-21: Enh. ä389
//				// Has this import already been executed -then just adopt the results
//				if (this.importMap.containsKey(imp)) {
//					ImportInfo impInfo = this.importMap.get(imp);
//					this.copyInterpreterContents(impInfo.interpreter, context.interpreter,
//							imp.variables, null, imp.constants.keySet(), false);
//					// FIXME adopt the imported typedefs if any
//					context.variables.addIfNew(impInfo.variableNames);
//					for (String constName: imp.constants.keySet()) {
//						if (!context.constants.containsKey(constName)) {
//							context.constants.put(constName, impInfo.interpreter.get(constName));
//						}
//					}
//					// START KGU#117 2017-04-29: Enh. #77
//					if (Element.E_COLLECTRUNTIMEDATA)
//					{
//						element.simplyCovered = true;
//						if (imp.isTestCovered(true)) {
//							element.deeplyCovered = true;
//						}
//					}
//					// END KGU#117 2017-04-29
//					try 
//					{
//						updateVariableDisplay();
//					}
//					catch (EvalError ex) {}
//				}
//				else {
//				// END KGU#376 2017-04-21
//					executeCall(imp, null, (Call)element);
//					// START KGU#117 2016-03-10: Enh. #77
//					if (Element.E_COLLECTRUNTIMEDATA)
//					{
//						element.simplyCovered = true;
//					}
//					// END KGU#117 2016-03-10
//				// START KGU#376 207-04-21: Enh. ä389
//				}
//				context.importList.addIfNew(diagrName);
//				// END KGU#376 2017-04-21
//			}
//			else
//			{
//				// START KGU#197 2016-07-27: Now translatable message
//				//result = "A subroutine diagram " + f.getName() + " (" + f.paramCount() + 
//				//		" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";
//				result = control.msgNoProgDiagram.getText().
//						replace("%", diagrName);
//				// END KGU#197 2016-07-27
//			}
//			
//		}
		// END KGU#376 2017-04-11
		else {
			// START KGU#197 2017-06-06: Now translatable
			//result = "<" + cmd + "> is not a correct function!";
			result = control.msgIllFunction.getText().replace("%1", cmd);
			// END KGU#197 2017-06-06
		}
		return result;
	}
	// END KGU 2015-11-11

	private String stepCase(Case element)
	{
		// START KGU 2016-09-25: Bugfix #254
		String[] parserKeys = new String[]{
				CodeParser.getKeyword("preCase"),
				CodeParser.getKeyword("postCase")
				};
		// END KGU 2016-09-25
		String result = new String();
		try
		{
			StringList text = element.getText();
			// START KGU#259 2016-09-25: Bugfix #254
			//String expression = text.get(0) + " = ";
			StringList tokens = Element.splitLexically(text.get(0), true);
			for (String key : parserKeys)
			{
				if (!key.trim().isEmpty())
				{
					tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
				}		
			}
			// START KGU#417 2017-06-30: Enh. #424
			//String expression = tokens.concatenate() + " = ";
			String expression = this.evaluateDiagramControllerFunctions(tokens.concatenate()) + " = ";
			// END KGU#417 2017-06-30
			// END KGU#259 2016-09-25
			boolean done = false;
			int last = text.count() - 1;
			boolean hasDefaultBranch = !text.get(last).trim().equals("%");
			if (!hasDefaultBranch)
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
				if ((q == last) && hasDefaultBranch)
				{
					// default branch
					go = true;
				}
				if (go == false)
				{
					// START KGU#15 2015-10-21: Test against a list of constants now
					//Object n = interpreter.eval(test);
					//go = n.toString().equals("true");
					for (int c = 0; !go && c < constants.length; c++)
					{
						// START KGU#259 2016-09-25: Bugfix #254
						//String test = convert(expression + constants[c]);
						tokens = Element.splitLexically(constants[c], true);
						for (String key : parserKeys)
						{
							if (!key.trim().isEmpty())
							{
								tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
							}		
						}
						String test = convert(expression + tokens.concatenate());
						// END KGU#259 2016-09-25
						Object n = this.evaluateExpression(test, false);
						go = n.toString().equals("true");
					}
					// END KGU#15 2015-10-21
				}
				if (go)
				{
					done = true;
					element.waited = true;
					if (result.isEmpty())
					{
						result = stepSubqueue(element.qs.get(q - 1), false);
					}
					if (result.equals(""))
					{
						element.waited = false;
					}
				}
			}
			if (result.equals(""))
			{
				// START KGU#296 2016-11-25: Issue #294 - special coverage treatment for default-less CASE
				if (!done && !hasDefaultBranch) {
					// In run data tracking mode it is required that the suppressed default branch
					// has been passed at least once to achieve deep test coverage
					element.qs.get(last).deeplyCovered = true;
				}
				// END KGU#296 2016-11-25
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
//			if (!CodeParser.preAlt.equals(""))
//			{
//				// FIXME: might damage variable names
//				s = BString.replace(s, CodeParser.preAlt, "");
//			}
//			if (!CodeParser.postAlt.equals(""))
//			{
//				// FIXME: might damage variable names
//				s = BString.replace(s, CodeParser.postAlt, "");
//			}
//
//			s = convert(s);
			StringList tokens = Element.splitLexically(s, true);
			for (String key : new String[]{
					CodeParser.getKeyword("preAlt"),
					CodeParser.getKeyword("postAlt")})
			{
				if (!key.trim().isEmpty())
				{
					tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
				}		
			}
			s = convert(tokens.concatenate());
			// END KGU#150 2016-04-03

			// START KGU#417 2017-06-30: Enh. #424
			s = this.evaluateDiagramControllerFunctions(s);
			// END KGU#417 2017-06-30

			//System.out.println("C=  " + interpreter.get("C"));
			//System.out.println("IF: " + s);
			Object cond = this.evaluateExpression(s, false);
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
				// START KGU#413 2017-06-09: Enh. #416: Cope with user-inserted line breaks
				//condStr = element.getText().getText();
				condStr = element.getUnbrokenText().getText();
				// END KGU#413 2017-06-09
				// START KGU#150 2016-04-03: More precise processing
//				if (!CodeParser.preWhile.equals(""))
//				{
//					// FIXME: might damage variable names
//					condStr = BString.replace(condStr, CodeParser.preWhile, "");
//				}
//				if (!CodeParser.postWhile.equals(""))
//				{
//					// FIXME: might damage variable names
//					condStr = BString.replace(condStr, CodeParser.postWhile, "");
//				}
//				// START KGU#79 2015-11-12: Forgotten zu write back the result!
//				//convert(condStr, false);
//				condStr = convert(condStr, false);
//				// END KGU#79 2015-11-12
//				// System.out.println("WHILE: "+condStr);
				StringList tokens = Element.splitLexically(condStr, true);
				for (String key : new String[]{
						CodeParser.getKeyword("preWhile"),
						CodeParser.getKeyword("postWhile")})
				{
					if (!key.trim().isEmpty())
					{
						tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
					}		
				}
				condStr = convert(tokens.concatenate());
				// END KGU#150 2016-04-03
			}

			//int cw = 0;
			// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
			//Object cond = context.interpreter.eval(convertStringComparison(condStr));
			String tempCondStr = this.evaluateDiagramControllerFunctions(condStr);
			Object cond = this.evaluateExpression(convertStringComparison(tempCondStr), false);
			// END KGU#417 2017-06-30

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
				context.loopDepth++;
				while (cond.toString().equals("true") && result.equals("")
						&& (stop == false) && !context.returned && leave == 0)
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
					// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
					//cond = context.interpreter.eval(convertStringComparison(condStr));
					tempCondStr = this.evaluateDiagramControllerFunctions(condStr);
					cond = this.evaluateExpression(convertStringComparison(tempCondStr), false);
					// END KGU#417 2017-06-30
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
				context.loopDepth--;
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
			// START KGU#413 2017-06-09: Enh. #416: Cope with user-inserted line breaks
			//String condStr = element.getText().getText();
			String condStr = element.getUnbrokenText().getText();
			// END KGU#413 2017-06-09
			// STRT KGU#150 2016-04-03: More pecise processing
//			if (!CodeParser.preRepeat.equals(""))
//			{
//				// FIXME: might damage variable names
//				condStr = BString.replace(condStr, CodeParser.preRepeat, "");
//			}
//			if (!CodeParser.postRepeat.equals(""))
//			{
//				// FIXME: might damage variable names
//				condStr = BString.replace(condStr, CodeParser.postRepeat, "");
//			}
//			condStr = convert(condStr, false);
			StringList tokens = Element.splitLexically(condStr, true);
			for (String key : new String[]{
					CodeParser.getKeyword("preRepeat"),
					CodeParser.getKeyword("postRepeat")})
			{
				if (!key.trim().isEmpty())
				{
					tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
				}		
			}
			condStr = convert(tokens.concatenate());
			// END KGU#150 2016-04-03

			//int cw = 0;
			// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
			//Object cond = context.interpreter.eval(convertStringComparison(condStr));
			String tempCondStr = this.evaluateDiagramControllerFunctions(condStr);
			Object cond = this.evaluateExpression(convertStringComparison(tempCondStr), false);
			// END KGU#417 2017-06-30
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
				context.loopDepth++;
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
					// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
					//cond = context.interpreter.eval(convertStringComparison(condStr));
					//Object cond = context.interpreter.eval(condStr);
					tempCondStr = this.evaluateDiagramControllerFunctions(condStr);
					cond = this.evaluateExpression(convertStringComparison(tempCondStr), false);
					// END KGU#417 2017-06-30
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
						!context.returned && leave == 0);
				// END KGU#77/KGU#78 2015-11-25
				// END KGU#70 2015-11-09
				// START KGU#78 2015-11-25: If there are open leave requests then nibble one off
				if (leave > 0)
				{
					leave--;
				}
				context.loopDepth--;
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
		// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
		int forLoopLevel = context.forLoopVars.count();
		// END KGU#307 2016-12-12
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
//			if (!CodeParser.preFor.equals(""))
//			{
//				str = BString.replace(str, CodeParser.preFor, "");
//			}
//			// trim blanks
//			str = str.trim();
//			// modify the later word
//			if (!CodeParser.postFor.equals(""))
//			{
//				str = BString.replace(str, CodeParser.postFor, "<=");
//			}
//			// do other transformations
//			str = CGenerator.transform(str);
//			String counter = str.substring(0, str.indexOf("="));
			String counter = element.getCounterVar();
			// END KGU#3 2015-10-27
			// complete
			
			// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
			context.forLoopVars.add(counter);
			// END KGU#307 2016-12-12

			// START KGU#3 2015-10-27: Now replaced by For-intrinsic mechanisms
//			String s = str.substring(str.indexOf("=") + 1,
//					str.indexOf("<=")).trim();
			String s = element.getStartValue(); 
			// END KGU#3 2015-10-27
			s = convert(s);
			// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated
			s = this.evaluateDiagramControllerFunctions(s);
			// END KGU#417 2017-06-30
			Object n = this.evaluateExpression(s, false);
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
			// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated
			s = this.evaluateDiagramControllerFunctions(s);
			// END KGU#417 2017-06-30
			
			n = this.evaluateExpression(s, false);
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
			context.loopDepth++;
			while (((sval >= 0) ? (cw <= fval) : (cw >= fval)) && result.equals("") &&
					(stop == false) && !context.returned && leave == 0)
			// END KGU#77/KGU#78 2015-11-25
			{
				// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
				//setVar(counter, cw);
				setVar(counter, cw, forLoopLevel-1);
				// END KGU#307 2016-12-12
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
			context.loopDepth--;
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
		// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
		while (forLoopLevel < context.forLoopVars.count()) {
			context.forLoopVars.remove(forLoopLevel);
		}
		// END KGU#307 2016-12-12
		return result;
	}
	
	// START KGU#61 2016-03-21: Enh. #84
	// This executes FOR-IN loops
	private String stepForIn(For element)
	{
		String result = new String();
		// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
		int forLoopLevel = context.forLoopVars.count();
		// END KGU#307 2016-12-12
		String valueListString = element.getValueList();
		String iterVar = element.getCounterVar();
		Object[] valueList = null;
		String problem = "";	// Gathers exception descriptions for analysis purposes
		Object value = null;
		// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
		try {
			valueListString = this.evaluateDiagramControllerFunctions(valueListString).trim();
		}
		catch (EvalError ex)
		{
			problem += "\n" + ex.getMessage();
		}
		// END KGU#417 2017-06-30
		// START KGU#410 2017-07-01: Enh. #413 - there is an array-returning built-in function now!
		//if (valueListString.startsWith("{") && valueListString.endsWith("}"))
		if (valueListString.startsWith("{") && valueListString.endsWith("}")
			|| valueListString.matches("^split\\(.*?[,].*?\\)$"))
		// END KGU#410 2017-07-01
		{
			try
			{
				this.evaluateExpression("Object[] tmp20160321kgu = " + valueListString, false);
				value = context.interpreter.get("tmp20160321kgu");
				context.interpreter.unset("tmp20160321kgu");
			}
			catch (EvalError ex)
			{
				problem += "\n" + ex.getMessage();
			}
		}
		// There are no other built-in functions returning an array and external function calls
		// aren't allowed at this position, hence it's relatively safe to conclude
		// an item enumeration from the occurrence of a comma. (If the comma IS an argument
		// separator of a function call then the function result will be an element of the
		// value list, such that it must be put in braces.)
		if (value == null && valueListString.contains(","))
		{
			try
			{
				this.evaluateExpression("Object[] tmp20160321kgu = {" + valueListString + "}", false);
				value = context.interpreter.get("tmp20160321kgu");
				context.interpreter.unset("tmp20160321kgu");
			}
			catch (EvalError ex)
			{
				problem += "\n" + ex.getMessage();
			}
		}
		// Might be a function or variable otherwise evaluable
		if (value == null)
		{
			try
			{
				value = this.evaluateExpression(valueListString, false);
			}
			catch (EvalError ex)
			{
				problem += "\n" + ex.getMessage();
			}
		}
		if (value == null && valueListString.contains(" "))
		{
			// Rather desperate attempt to compose an array from loose strings (like in shell scripts)
			StringList tokens = Element.splitExpressionList(valueListString, " ");
			try
			{
				this.evaluateExpression("Object[] tmp20160321kgu = {" + tokens.concatenate(",") + "}", false);
				value = context.interpreter.get("tmp20160321kgu");
				context.interpreter.unset("tmp20160321kgu");
			}
			catch (EvalError ex)
			{
				problem += "\n" + ex.getMessage();
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
				// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
				context.forLoopVars.add(iterVar);
				// END KGU#307 2016-12-12

				// Leave if any kind of Jump statement has been executed
				context.loopDepth++;
				int cw = 0;

				while (cw < valueList.length && result.equals("")
						&& (stop == false) && !context.returned && leave == 0)
				{
					try
					{
						Object iterVal = valueList[cw];
						// START KGU#388 2017-09-27: Enh. #423 declare or un-declare the loop variable dynamically
						TypeMapEntry iterType = null;
						if (iterVal instanceof HashMap<?,?>) {
							Object typeName = ((HashMap<?, ?>)iterVal).get("§TYPENAME§");
							if (typeName instanceof String && (iterType = context.dynTypeMap.get(":" + typeName)) != null) {
								context.dynTypeMap.put(iterVar, iterType);
							}
						}
						else if (iterVal != null && (iterType = context.dynTypeMap.get(iterVar)) != null && iterType.isRecord()) {
							context.dynTypeMap.remove(iterVar);
						}
						// END KGU#388 2017-09-27
						// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
						//setVar(iterVar, valueList[cw]);
						setVar(iterVar, iterVal, forLoopLevel-1);
						// END KGU#307 2016-12-12
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
				context.loopDepth--;
				// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
				while (forLoopLevel < context.forLoopVars.count()) {
					context.forLoopVars.remove(forLoopLevel);
				}
				// END KGU#307 2016-12-12
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
			int outerLoopDepth = context.loopDepth;
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
			context.loopDepth = 0;	// Loop exits may not penetrate the Parallel section
			while (!undoneThreads.isEmpty() && result.equals("") && (stop == false) &&
					!context.returned && leave == 0)
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
						JOptionPane.showMessageDialog(diagram.getParent(), control.msgJumpOutParallel.getText().replace("%", "\n\n" + 
								instr.getText().getText().replace("\n",  "\n\t") + "\n\n"),
								control.msgTitleParallel.getText(), JOptionPane.WARNING_MESSAGE);
						// END KGU#247 2016-09-17
					}
					// END KGU#78 2015-11-25
				}                
			}
			context.loopDepth = outerLoopDepth;	// Restore the original context
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
				&& result.equals("") && (stop == false) && !context.returned
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
	
	// START KGU#388 2017-09-16: Enh. #423 We must prepare expressions with record component access
	/**
	 * Resolves qualified names (record access) where contained and - if allowed by setting
	 * {@code _withInitializers} - array or record initializers and has the interpreter evaluate
	 * the prepared expression.<br/>
	 * This preparation work might perhaps also have been done by the convert function but requires
	 * current evaluation context. So it was rather located here.<br/>
	 * Note: Argument {@code _withInitializers} (and the associated mechanism) was added via
	 * refactoring afterwards with a default value of {@code false} in order to avoid unwanted
	 * impact. If there happens to be some place in code where it seems helpful to activate this
	 * mechanism just go ahead and try.   
	 * @param _expr -the converted expression to be evaluated
	 * @param _withInitializers - whether an array or record initializer is to be managed here
	 * @return the evaluated result if successful 
	 * @throws EvalError an exception if something went wrong (may be raised by the interpreter
	 * or this method itself)
	 */
	private Object evaluateExpression(String _expr, boolean _withInitializers) throws EvalError
	{
		Object value = null;
		StringList tokens = Element.splitLexically(_expr, true);
		int i = 0;
		// FIXME: Special treatment for inc() and dec functions - no need if convert was applied before
		while ((i = tokens.indexOf(".", i+1)) > 0) {
			if (i+1 < tokens.count() && Function.testIdentifier(tokens.get(i+1), null) && (i+2 == tokens.count() || !tokens.get(i+2).equals("("))) {
				tokens.set(i, ".get(\"" + tokens.get(i+1) + "\")");
				tokens.remove(i+1);
			}
		}
		// START KGU#100/KGU#388 2017-09-29: Enh. #84, #423 Make this avaialable at more places
		TypeMapEntry recordType = null;
		// FIXME: This should be recursive!
		if (tokens.get(tokens.count()-1).equals("}") && _withInitializers) {
			// START KGU#100 2016-01-14: Enh. #84 - accept array assignments with syntax array <- {val1, val2, ..., valN}
			if (tokens.get(0).equals("{"))
			{
				// FIXME: We might have to evaluate those element values in advance, which are initializers themselves...
				this.evaluateExpression("Object[] tmp20160114kgu = " + tokens.concatenate(), false);
				value = context.interpreter.get("tmp20160114kgu");
				context.interpreter.unset("tmp20160114kgu");
			}
			// END KGU#100 2016-01-14
			// START KGU#388 2017-09-13: Enh. #423 - accept record assignments with syntax recordVar <- typename{comp1: val1, comp2: val2, ..., compN: valN}
			else if (tokens.get(1).equals("{") && (recordType = identifyRecordType(tokens.get(0), true)) != null)
			{
				this.evaluateExpression("HashMap tmp20170913kgu = new HashMap()", false);
				HashMap<String, String> components = Element.splitRecordInitializer(_expr);
				if (components == null || components.containsKey("§TAIL§")) {
					throw new EvalError(control.msgInvalidExpr.getText().replace("%1", _expr), null, null);
				}
				else {
					components.remove("§TYPENAME§");
					LinkedHashMap<String, TypeMapEntry> compDefs = recordType.getComponentInfo(false);
					for (Entry<String, String> comp: components.entrySet()) {
						// FIXME: We might have to evaluate the component value in advance if it is an initializer itself...
						if (compDefs.containsKey(comp.getKey())) {
							context.interpreter.eval("tmp20170913kgu.put(\"" + comp.getKey() + "\", " + comp.getValue() + ");");
						}
						else {
							throw new EvalError(control.msgInvalidComponent.getText().replace("%1", comp.getKey()).replace("%2", recordType.typeName), null, null);
						}
					}
					value = context.interpreter.get("tmp20170913kgu");
					if (value instanceof HashMap<?,?>) {
						((HashMap<String, Object>)value).put("§TYPENAME§", recordType.typeName);
					}
					context.interpreter.unset("tmp20170913kgu");
				}
			}
			// END KGU#388 2017-09-13
		}
		else
		{
			value = context.interpreter.eval(tokens.concatenate());
		}
		return value;
	}
	// END KGU#388 2017-09-16
	
	private String unconvert(String s)
	{
		s = s.replace("==", "=");
		return s;
	}

	private void waitForNext()
	{
		// START KGU#379 2017-04-12: Bugfix #391: This is the proper place to prepare the buttons for pause mode
		// Well, maybe it is better put into the synchronized block?
		if (getPaus()) {
			control.setButtonsForPause(true);
		}
		// END KGU#379 2017-04-12
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
			index = (Integer) this.evaluateExpression(ind, false);
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
	 * (where CodeParser.ignoreCase is paid attention to)
	 * @param keyword - parser preference string
	 * @return match pattern
	 */
	private String getKeywordPattern(String keyword)
	{
		String pattern = Matcher.quoteReplacement(keyword);
		if (CodeParser.ignoreCase)
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

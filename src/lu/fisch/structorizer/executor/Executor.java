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
 *
 ******************************************************************************************************
 *
 *      Comment:
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
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

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
import lu.fisch.structorizer.generators.CGenerator;
import lu.fisch.structorizer.gui.Diagram;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.gui.LangDialog;
import lu.fisch.structorizer.parsers.D7Parser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;
import bsh.EvalError;
import bsh.Interpreter;

import com.stevesoft.pat.Regex;

/**
 * 
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
		if (diagram != null)
		{
			mySelf.diagram = diagram;
		}
		mySelf.control.init();
		mySelf.control.setLocationRelativeTo(diagram);
		mySelf.control.validate();
		// START KGU#89 2015-11-25: Language support (we don't force the existence of all languages)
		try {
			LangDialog.setLang(mySelf.control, mySelf.diagram.getLang());
		}
		catch (Exception ex)
		{
			System.err.println(ex.getMessage());
		}
		// END KGU#89 2015-11-25
		mySelf.control.setVisible(true);
		mySelf.control.repaint();

		return mySelf;
	}

	private Control control = new Control();

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

	private Executor(Diagram diagram, DiagramController diagramController)
	{
		this.diagram = diagram;
		this.diagramController = diagramController;
	}

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
		if (str.indexOf(" == ") >= 0 || str.indexOf(" != ") >= 0)
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
				String[] eqOps = {"==", "!="};
				for (int op = 0; op < eqOps.length; op++)
				{
					Regex r = null;
					// The comparison operators should have been padded within the string by former conversion steps
					if (!s.equals(" " + eqOps[op] + " ") && s.indexOf(eqOps[op]) >= 0)
					{
						String leftParenth = "";
						String rightParenth = "";
						// Get the left operand expression
						r = new Regex("(.*)"+eqOps[op]+"(.*)", "$1");
						String left = r.replaceAll(s).trim();	// All? Really? And what is the result supposed to be then?
						// Re-balance parentheses
						while (Function.countChar(left, '(') > Function.countChar(left, ')') &&
								left.startsWith("("))
						{
							leftParenth = leftParenth + "(";
							left = left.substring(1).trim();
						}
						// Get the right operand expression
						r = new Regex("(.*)"+eqOps[op]+"(.*)", "$2");
						String right = r.replaceAll(s).trim();
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
								exprs.set(i, leftParenth + neg + left + ".equals(" + right + ")" + rightParenth);
								replaced = true;
							}
							// We must make single-char strings comparable with characters, since it
							// doesn't work automatically and several conversions have been performed 
							else if ((leftO instanceof String) && (rightO instanceof Character))
							{
								exprs.set(i, leftParenth + neg + left + ".equals(\"" + (Character)rightO + "\")" + rightParenth);
								replaced = true;								
							}
							else if ((leftO instanceof Character) && (rightO instanceof String))
							{
								exprs.set(i, leftParenth + neg + right + ".equals(\"" + (Character)leftO + "\")" + rightParenth);
								replaced = true;								
							}
							// START KGU#99 2015-12-10: Bugfix #49 (also replace if both operands are array elements (objects!)
							else if ((pos = left.indexOf('[')) > -1 && left.indexOf(']', pos) > -1 && 
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
				System.out.println(e.getMessage());
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
		/////////////////////////////////////////////////////////
		this.execute(null);	// The actual top-level execution
		/////////////////////////////////////////////////////////
		this.callers.clear();
		this.stackTrace.clear();
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
					String str = JOptionPane.showInputDialog(null,
							"Please enter a value for <" + in + ">", null);
					if (str == null)
					{
						//i = params.count();	// leave the loop
						result = "Manual break!";
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
					} catch (EvalError ex)
					{
						result = ex.getMessage();
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
						result = ex.getMessage();
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
				result = "Manual break!";
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
				JOptionPane.showMessageDialog(diagram, result, "Error",
						JOptionPane.ERROR_MESSAGE);
				isErrorReported = true;
			}
			if (!this.callers.isEmpty())
			{
				stop = true;
				paus = false;
				step = false;
			}
			else if (isErrorReported && stackTrace.count() > 0)
			{
				addToStackTrace(root, arguments);
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
								// START KGU#133 2016-01-09: Show large arrays in a listview
								//JOptionPane.showMessageDialog(diagram, n,
								//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
								// KGU#133 2016-01-29: Arrays now always shown as listview (independent of size)
								if (resObj instanceof Object[] /*&& ((Object[])resObj).length > 20*/)
								{
									// START KGU#147 2016-01-29: Enh. #84 - interface changed for more flexibility
									//showArray((Object[])resObj, "Returned result");
									showArray((Object[])resObj, "Returned result", !step);
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
									JOptionPane.showMessageDialog(diagram, resObj,
											"Returned result", JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									Object[] options = {"OK", "Pause"};		// FIXME: Provide a translation
									int pressed = JOptionPane.showOptionDialog(diagram, resObj, "Returned result",
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
		List arrayContent = new List(10);
		for (int i = 0; i < _array.length; i++)
		{
			arrayContent.add("[" + i + "]  " + prepareValueForDisplay(_array[i]));
		}
		arrayView.getContentPane().add(arrayContent, BorderLayout.CENTER);
		arrayView.setSize(300, 300);
		arrayView.setLocationRelativeTo(control);
		arrayView.setModalityType(ModalityType.APPLICATION_MODAL);
		arrayView.setVisible(true);
	}
	// END KGU#133 2016-01-09
	
	// START KGU#2 (#9) 2015-11-13: New method to execute a called subroutine
	private Object executeCall(Root subRoot, Object[] arguments)
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
		
		this.diagram.setRoot(root);
		
		boolean done = this.execute(arguments);

		// START KGU#2 2015-11-24
		if (!done || stop)
		{
			addToStackTrace(root, arguments);
		}
		// END KGU#2 2015-11-24
		
		// START KGU#117 2016-03-07: Enh. #77
		// For recursive calls the coverage must be combined
		if (cloned && Element.E_TESTCOVERAGEMODE)
		{
			subRoot.combineCoverage(root);
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

	/**
	 * Pops up a dialog displaying the call trace with argument values
	 */
	private void showStackTrace()
	{
		if (stackTrace.count() <= 20)
		{
			// Okay, keep it simple
			JOptionPane.showMessageDialog(diagram, this.stackTrace.getText(), "Stack trace",
					JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			JDialog stackView = new JDialog();
			stackView.setTitle("Stack trace");
			stackView.setIconImage(IconLoader.ico004.getImage());
			List stackContent = new List(10);
			for (int i = 0; i < stackTrace.count(); i++)
			{
				stackContent.add(stackTrace.get(i));
			}
			stackView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    stackView.getContentPane().add(stackContent, BorderLayout.CENTER);
		    stackView.setSize(300, 300);
		    stackView.setLocationRelativeTo(control);
		    stackView.setModalityType(ModalityType.APPLICATION_MODAL);
		    stackView.setVisible(true);
		}		
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
				System.out.println(e.getMessage());
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
				System.out.println(e.getMessage());
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
			pascalFunction = "public Character lowercase(Character ch) { return (Character)Character.toLowerCase(ch); }";
			interpreter.eval(pascalFunction);
			pascalFunction = "public Character uppercase(Character ch) { return (Character)Character.toUpperCase(ch); }";
			interpreter.eval(pascalFunction);
			// char transformation
			
			// END KGU#57 2015-11-07
		} catch (EvalError ex)
		{
			System.out.println(ex.getMessage());
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
		control.setVisible(false);
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
			System.out.println(rawInput + " as double: " + ex.getMessage());
		}
		// finally try adding as integer
		try
		{
			int intInput = Integer.parseInt(rawInput);
			setVar(name, intInput);
		} catch (Exception ex)
		{
			System.out.println(rawInput + " as int: " + ex.getMessage());
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
		Vector<Vector> vars = new Vector();
		for (int i = 0; i < this.variables.count(); i++)
		{
			Vector myVar = new Vector();
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
		for (int i = 0; i < newValues.length; i++)
		{
			if (newValues[i] != null)
			{
				try {
					String varName = this.variables.get(i);
					Object oldValue = interpreter.get(varName);
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
		running = true;
		paus = useSteps;
		step = useSteps;
		stop = false;
		variables = new StringList();
		control.updateVars(new Vector<Vector>());
		
		Thread runner = new Thread(this, "Player");
		runner.start();
	}
	
	// START KGU#43 2015-10-12 New method for breakpoint support
	private boolean checkBreakpoint(Element element)
	{
		boolean atBreakpoint = element.isBreakpoint(); 
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
						Matcher.quoteReplacement(D7Parser.input.trim()) + "([\\W].*|$)"))
				// END KGU#65 2015-11-04
				{
					result = tryInput(cmd);
				}
				// output
				// START KGU#65 2015-11-04: Output keyword should only trigger this if positioned at line start
				//else if (cmd.indexOf(D7Parser.output) >= 0)
				else if (cmd.matches(
						Matcher.quoteReplacement(D7Parser.output.trim()) + "([\\W].*|$)"))
				// END KGU#65 2015-11-04
				{
					result = tryOutput(cmd);
				}
				// return statement
				// START KGU 2015-11-28: The "return" keyword ought to be the first word of the instruction,
				// comparison should not be case-sensitive while D7Parser.preReturn isn't fully configurable,
				// but a separator would be fine...
				//else if (cmd.indexOf("return") >= 0)
				else if (cmd.toLowerCase().matches(
						Matcher.quoteReplacement(
								D7Parser.preReturn.toLowerCase()) + "([\\W].*|$)"))
				// END KGU 2015-11-11
				{		 
					result = tryReturn(cmd.trim());
				}
				else
				{
					result = trySubroutine(cmd, element);
				}
			} catch (EvalError ex)
			{
				result = ex.getMessage();
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

		// START KGU#117 2016-03-07: Enh. #77
		boolean allSubroutinesCovered = true;
		// END KGU#117 2016-03-07

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
				// START KGU#117 2016-03-07: Enh. #77
				element.subroutineCovered = false;
				// END KGU#117 2016-03-07

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
				
				// START KGU#117 2016-03-07: Enh. #77
				allSubroutinesCovered = allSubroutinesCovered && element.subroutineCovered;
				// END KGU#117 2016-03-07
				
			} catch (EvalError ex)
			{
				result = ex.getMessage();
			}

			i++;
			// Among the lines of a single instruction element there is no further breakpoint check!
		}
		if (result.equals(""))
		{
			element.executed = false;
			element.subroutineCovered = allSubroutinesCovered;
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
				// Single-level break? (An empty Jump is also a break!)
				if (tokens.indexOf(D7Parser.preLeave) == 0 && tokens.count() == 1 ||
						cmd.isEmpty() && i == sl.count() - 1)
				{
					this.leave++;
					done = true;
				}
				// Multi-level leave?
				else if (tokens.indexOf(D7Parser.preLeave) == 0)
				{
					int nLevels = 1;
					if (tokens.count() > 1)
					{
						try {
							nLevels = Integer.parseUnsignedInt(tokens.get(1));
						}
						catch (NumberFormatException ex)
						{
							result = "Illegal leave argument: " + ex.getMessage();
						}
					}
					this.leave += nLevels;
					done = true;
				}
				// Unstructured return from the routine?
				else if (tokens.indexOf(D7Parser.preReturn) == 0)
				{
					result = tryReturn(convert(sl.get(i)));
					done = true;
				}
				// Exit from the entire program - simply handled like an error here.
				else if (tokens.indexOf(D7Parser.preExit) == 0)
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
							result = "Inappropriate exit value: <" + (n == null ? tokens.get(1) : n.toString()) + ">";
						}
					}
					catch (EvalError ex)
					{
						result = "Wrong exit value: " + ex.getMessage();
					}
					if (result.isEmpty())
					{
						result = "Program exited with code " + exitValue + "!";
						// START KGU#117 2016-03-07: Enh. #77
						element.checkTestCoverage(true);
						// END KGU#117 2016-03-07
					}
					done = true;
				}
				// Anything else is an error
				else if (!cmd.isEmpty())
				{
					result = "Illegal content of a Jump (i.e. exit) instruction: <" + cmd + ">!";
				}
			} catch (Exception ex)
			{
				result = ex.getMessage();
			}
			i++;
		}
		if (done && leave > loopDepth)
		{
			result = "Too many levels to leave (actual depth: " + loopDepth + " / specified: " + leave + ")!";
		}			
		if (result.equals(""))
		{
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
					// START KGU#117 2016-03-08: Enh. #77
					((Call)instr).isRecursive = 
							sub.isCalling || sub.equals(this.diagram.getRoot());
					// END KGU#117 2016-03-08
					value = executeCall(sub, args);
					// START KGU#117 2016-03-07: Enh. #77
					((Call)instr).subroutineCovered = sub.isTestCovered();
					if (((Call)instr).isRecursive) // FIXME: Still needed?
					{
						instr.checkTestCoverage(true);
					}
					// END KGU#117 2016-03-07
				}
				else
				{
					result = "A function diagram " + f.getName() + " (" + f.paramCount() + 
							" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first."; 
				}
			}
			else
			{
				result = "<" + expression + "> is not a correct function!";
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
			result = "<"
					+ expression
					+ "> is not a correct or existing expression.";
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
			int pressed = JOptionPane.showOptionDialog(diagram, "Please acknowledge.", "Input",
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
					result = e.getMessage();
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
			String str = JOptionPane.showInputDialog(null,
					"Please enter a value for <" + in + ">", null);
			// START KGU#84 2015-11-23: ER #36 - Allow a controlled continuation on cancelled input
			//setVarRaw(in, str);
			if (str == null)
			{
				// Switch to step mode such that the user may enter the variable in the display and go on
				JOptionPane.showMessageDialog(diagram, "Execution paused - you may enter the value in the variable display.",
						"Input cancelled", JOptionPane.WARNING_MESSAGE);
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
					result = "<"
							+ out
							+ "> is not a correct or existing expression.";
				} else
				{
		// START KGU#101 2015-12-11: Fix #54 (continued)
					//	String s = unconvert(n.toString());
					str += n.toString();
				}
			}
		// START KGU#107 2015-12-13: Enh-/bug #51: Handle empty output instruction
		}
		else {
			str = "(empty line)";
		}
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
			if (step)
			{
				// In step mode, there is no use to offer pausing
				JOptionPane.showMessageDialog(diagram, s, "Output",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				// In run mode, give the user a chance to intervene
				Object[] options = {"OK", "Pause"};	// FIXME: Provide a translation
				int pressed = JOptionPane.showOptionDialog(diagram, s, "Output",
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
					result = "<"
							+ out
							+ "> is not a correct or existing expression.";
				// START KGU#133 2016-01-29: Arrays should be presented as scrollable list
				} else if (resObj instanceof Object[])
				{
					showArray((Object[])resObj, "Returned result", !step);
				} else if (step)
				{
					// START KGU#147 2016-01-29: This "uncoverting" copied from tryOutput() didn't make sense...
					//String s = unconvert(resObj.toString());
					//JOptionPane.showMessageDialog(diagram, s,
					//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
					JOptionPane.showMessageDialog(diagram, resObj,
							"Returned result", JOptionPane.INFORMATION_MESSAGE);
					// END KGU#147 2016-01-29					
				// END KGU#133 2016-01-29
				} else
				{
					// START KGU#84 2015-11-23: Enhancement to give a chance to pause (though of little use here)
					Object[] options = {"OK", "Pause"};		// FIXME: Provide a translation
					int pressed = JOptionPane.showOptionDialog(diagram, resObj, "Returned result",
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

	// Submethod of stepInstruction(Instruction element), handling an output instruction
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
						result = result + "PARAM " + (p+1) + ": <"
								+ f.getParam(p)
								+ "> is not a correct or existing expression.";
					} else
					{
						params += "," + args[p].toString();
					}
				} catch (EvalError ex)
				{
					result = result + (!result.isEmpty() ? "\n" : "") +
							"PARAM " + (p+1) + ": " + ex.getMessage();
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
					// START KGU#117 2016-03-08: Enh. #77
					((Call)element).isRecursive = sub.isCalling || sub.equals(this.diagram.getRoot());
					// END KGU#117 2016-03-08
					executeCall(sub, args);
					// START KGU#117 2016-03-08: Enh. #77
					((Call)element).subroutineCovered = sub.isTestCovered();
					if (((Call)element).isRecursive)	// FIXME: Still necessary?
					{
						element.checkTestCoverage(true);
					}
					// END KGU#117 2016-03-08
				}
				else
				{
					result = "A subroutine diagram " + f.getName() + " (" + f.paramCount() + 
							" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";					
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
			result = ex.getMessage();
		}
		
		return result;
	}
	
	private String stepAlternative(Alternative element)
	{
		String result = new String();
		try
		{
			String s = element.getText().getText();
			if (!D7Parser.preAlt.equals(""))
			{
				// FIXME: might damage variable names
				s = BString.replace(s, D7Parser.preAlt, "");
			}
			if (!D7Parser.postAlt.equals(""))
			{
				// FIXME: might damage variable names
				s = BString.replace(s, D7Parser.postAlt, "");
			}

			s = convert(s);

			//System.out.println("C=  " + interpreter.get("C"));
			//System.out.println("IF: " + s);
			Object n = interpreter.eval(s);
			//System.out.println("Res= " + n);
			if (n == null)
			{
				result = "<" + s
						+ "> is not a correct or existing expression.";
			}
			// if(getExec(s).equals("OK"))
			else 
			{
				Subqueue branch;
				if (n.toString().equals("true"))
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
			result = ex.getMessage();
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
				if (!D7Parser.preWhile.equals(""))
				{
					// FIXME: might damage variable names
					condStr = BString.replace(condStr, D7Parser.preWhile, "");
				}
				if (!D7Parser.postWhile.equals(""))
				{
					// FIXME: might damage variable names
					condStr = BString.replace(condStr, D7Parser.postWhile, "");
				}
				// START KGU#79 2015-11-12: Forgotten zu write back the result!
				//convert(condStr, false);
				condStr = convert(condStr, false);
				// END KGU#79 2015-11-12
				// System.out.println("WHILE: "+condStr);
			}

			//int cw = 0;
			Object cond = interpreter.eval(convertStringComparison(condStr));

			if (cond == null)
			{
				result = "<" + condStr
						+ "> is not a correct or existing expression.";
			} else
			{
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

					element.executed = true;
					element.waited = false;
					if (result.equals(""))
					{
						//cw++;
						// START KGU 2015-10-13: Symbolizes the loop condition check 
						checkBreakpoint(element);
						delay();
						// END KGU 2015-10-13
					}
					cond = interpreter.eval(convertStringComparison(condStr));
					if (cond == null)
					{
						result = "<"
								+ condStr
								+ "> is not a correct or existing expression.";
					}
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
			if (!D7Parser.preRepeat.equals(""))
			{
				// FIXME: might damage variable names
				condStr = BString.replace(condStr, D7Parser.preRepeat, "");
			}
			if (!D7Parser.postRepeat.equals(""))
			{
				// FIXME: might damage variable names
				condStr = BString.replace(condStr, D7Parser.postRepeat, "");
			}
			// s=s.replace("==", "=");
			// s=s.replace("=", "==");
			// s=s.replace("<==", "<=");
			// s=s.replace(">==", ">=");
			condStr = convert(condStr, false);
			// System.out.println("REPEAT: "+s

			//int cw = 0;
			Object n = interpreter.eval(condStr);
			if (n == null)
			{
				result = "<" + condStr
						+ "> is not a correct or existing expression.";
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
					n = interpreter.eval(convertStringComparison(condStr));
					if (n == null)
					{
						result = "<"
								+ condStr
								+ "> is not a correct or existing expression.";
					}

					// delay this element
					// START KGU 2015-10-12: This remains an important breakpoint position
					checkBreakpoint(element);
					// END KGU 2015-10-12
					element.waited = false;
					delay();	// Symbolizes the loop condition check time
					element.waited = true;

				// START KGU#70 2015-11-09: Condition logically incorrect - execution often got stuck here 
				//} while (!(n.toString().equals("true") && result.equals("") && (stop == false)));
				// START KGU#77/KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
				//} while (!(n.toString().equals("true")) && result.equals("") && (stop == false))
				} while (!(n.toString().equals("true")) && result.equals("") && (stop == false) &&
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
				result = "<"+s+"> is not a correct or existing expression.";
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
				result = "<"+s+ "> is not a correct or existing expression.";
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
					result = step(instr);
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
						JOptionPane.showMessageDialog(diagram, "Uncaught attempt to jump out of a parallel thread:\n\n" + 
								instr.getText().getText().replace("\n",  "\n\t") + "\n\nThread killed!",
								"Parallel Execution Problem", JOptionPane.WARNING_MESSAGE);
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
			result = step(sq.getElement(i));
			i++;
		}
		if (sq.getSize() == 0) sq.tested = true;
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
				varname.indexOf("]"));

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
	
}

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
*      Kay Gürtzig     2015.11.20      Bugfix KGU#86: Interpreter was improperly set up for functions sqr, sqrt;
*                                      Message types for output and return value information corrected
*
******************************************************************************************************
*
*      Comment:
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
*      2015.10.15 (KGU#47) Improved simulation of Parallel execution
*          Instead of running entire "threads" of the parallel section in just random order, the "threads"
*          will now only progress by one instruction when randomly chosen, so they alternate in an
*          unpredictable way)
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
*         
******************************************************************************************************///

import java.awt.Color;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import javax.swing.JOptionPane;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.generators.CGenerator;
import lu.fisch.structorizer.gui.Diagram;
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
		mySelf.control.setVisible(true);
		mySelf.control.repaint();

		return mySelf;
	}

	private Control control = new Control();

	private int delay = 50;

	private Diagram diagram = null;

	private DiagramController diagramController = null;
	private Interpreter interpreter;

	private boolean paus = false;
	boolean returned = false;
	private boolean running = false;
	private boolean step = false;
	private boolean stop = false;
	private StringList variables = new StringList();

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

		// START KGU#18/KGU#23 2015-10-26: Replaced by new unifying method on Element class
//		// variable assignment
//		// START KGU 2014-12-02: To achieve consistency with operator highlighting
//		s = s.replace("<--", "<-");
//		// END KGU 2014-12-02
//		s = s.replace(":=", "<-");
//
//		// testing
//		s = s.replace("==", "=");
//		s = s.replace("!=", "<>");
//		s = s.replace("=", "==");
//		s = s.replace("<==", "<=");
//		s = s.replace(">==", ">=");
//		s = s.replace("<>", "!=");
//
//		s = s.replace(" mod ", " % ");
//		s = s.replace(" div ", " / ");
//        // START KGU 2014-11-14: Logical operators, too
//        s=s.replace(" and ", " && ");
//        s=s.replace(" or ", " || ");
//        s=s.replace(" not ", " !");
//        s=s.replace("(not ", "(!");
//        s=s.replace(" not(", " !(");
//        s=s.replace("(not(", "(!(");
//       	if (s.startsWith("not ")) {
//       		s = "!" + s.substring(4);
//       	}
//       	if (s.startsWith("not(")) {
//       		s = "!(" + s.substring(4);
//       	}
//        s=s.replace(" xor ", " ^ "); // This might cause some operator preference trouble, though       
//        // END KGU 2014-11-14
		s = Element.unifyOperators(s);
		s = s.replace(" div ", " / ");		// FIXME: Operands should be coerced to integer...
		// END KGU#18/KGU#23 2015-10-26

		// Convert built-in mathematical functions
		s = s.replace("cos(", "Math.cos(");
		s = s.replace("sin(", "Math.sin(");
		s = s.replace("tan(", "Math.tan(");
        // START KGU 2014-10-22: After the previous replacements the following 3 strings would never be found!
        //s=s.replace("acos(", "Math.acos(");
        //s=s.replace("asin(", "Math.asin(");
        //s=s.replace("atan(", "Math.atan(");
        // This is just a workaround; A clean approach would require a genuine lexical scanning in advance
        s=s.replace("aMath.cos(", "Math.acos(");
        s=s.replace("aMath.sin(", "Math.asin(");
        s=s.replace("aMath.tan(", "Math.atan(");
        // END KGU 2014-10-22:
		s = s.replace("abs(", "Math.abs(");
		s = s.replace("round(", "Math.round(");
		s = s.replace("min(", "Math.min(");
		s = s.replace("max(", "Math.max(");
		s = s.replace("ceil(", "Math.ceil(");
		s = s.replace("floor(", "Math.floor(");
		s = s.replace("exp(", "Math.exp(");
		s = s.replace("log(", "Math.log(");
		s = s.replace("sqrt(", "Math.sqrt(");
		s = s.replace("pow(", "Math.pow(");
		s = s.replace("toRadians(", "Math.toRadians(");
		s = s.replace("toDegrees(", "Math.toDegrees(");
		// s=s.replace("random(", "Math.random(");

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
		s = s.replace("''", "'");
		// pascal: randomize
		s = s.replace("randomize()", "randomize");
		s = s.replace("randomize", "randomize()");

		// clean up ... if needed
		s = s.replace("Math.Math.", "Math.");

		// FIXME (KGU#57 2015-10-27): The following mechanism doesn't work in composed expressions like
		//       answer == "J" || answer == "j"
//		if (s.indexOf("==") >= 0)
//		{
//			r = new Regex("(.*)==(.*)", "$1");
//			String left = r.replaceAll(s).trim();
//			while (Function.countChar(left, '(') > Function
//					.countChar(left, ')'))
//			{
//				left += ')';
//			}
//			r = new Regex("(.*)==(.*)", "$2");
//			String right = r.replaceAll(s).trim();
//			while (Function.countChar(right, ')') > Function.countChar(right,
//					'('))
//			{
//				right = '(' + right;
//			}
//			// ---- thanks to autoboxing, we can always use the "equals" method
//			// ---- to compare things ...
//			// addendum: sorry, doesn't always work.
//			try
//			{
//				Object leftO = interpreter.eval(left);
//				Object rightO = interpreter.eval(right);
//				if ((leftO instanceof String) || (rightO instanceof String))
//				{
//					s = left + ".equals(" + right + ")";
//				}
//			} catch (EvalError ex)
//			{
//				System.err.println(ex.getMessage());
//			}
//		}
//		if (s.indexOf("!=") >= 0)
//		{
//			r = new Regex("(.*)!=(.*)", "$1");
//			String left = r.replaceAll(s).trim();
//			while (Function.countChar(left, '(') > Function
//					.countChar(left, ')'))
//			{
//				left += ')';
//			}
//			r = new Regex("(.*)!=(.*)", "$2");
//			String right = r.replaceAll(s).trim();
//			while (Function.countChar(right, ')') > Function.countChar(right,
//					'('))
//			{
//				right = '(' + right;
//			}
//			// ---- thanks to autoboxing, we can always use the "equals" method
//			// ---- to compare things ...
//			// addendum: sorry, doesn't always work.
//			try
//			{
//				Object leftO = interpreter.eval(left);
//				Object rightO = interpreter.eval(right);
//				if ((leftO instanceof String) || (rightO instanceof String))
//				{
//					s = "!" + left + ".equals(" + right + ")";
//				}
//			} catch (EvalError ex)
//			{
//				System.err.println(ex.getMessage());
//			}
//		}
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
			StringList exprs = StringList.explodeWithDelimiter(str, " \\|\\| ");	// '|' is a regex metasymbol!
			exprs = StringList.explodeWithDelimiter(exprs, " && ");
			boolean replaced = false;
			for (int i = 0; i < exprs.count(); i++)
			{
				String s = exprs.get(i);
				String[] eqOps = {"==", "!="};
				for (int op = 0; op < eqOps.length; op++)
				{
					Regex r = null;
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

	// METHOD MODIFIED BY GENNARO DONNARUMMA

	public void execute()
	{
		Root root = diagram.getRoot();

		boolean analyserState = diagram.getAnalyser();
		diagram.setAnalyser(false);
		// START KGU 2015-10-11/13:
		// Unselect all elements before start!
		diagram.unselectAll();
		// ...and reset all execution state remnants (just for sure)
		diagram.clearExecutionStatus();
		// END KGU 2015-10-11/13
		initInterpreter();
		String result = "";
		returned = false;

		// START KGU#39 2015-10-16 (1/2): It made absolutely no sense to look for parameters if root is a program
		if (!root.isProgram)
		{
		// END KGU#39 2015-10-16 (1/2)
			StringList params = root.getParameterNames();
			//System.out.println("Having: "+params.getCommaText());
			params=params.reverse();
			//System.out.println("Having: "+params.getCommaText());
			for (int i = 0; i < params.count(); i++)
			{
				String in = params.get(i);
				String str = JOptionPane.showInputDialog(null,
						"Please enter a value for <" + in + ">", null);
				if (str == null)
				{
					i = params.count();
					result = "Manual break!";
					break;
				}
				try
				{
					// START KGU#69 2015-11-08 What we got here is to be regarded as raw input
//					// first add as string
//					setVar(in, str);
//					// try adding as char: FIXME Spoils comparison
//					try
//					{
//						if (str.length() == 1)
//						{
//							Character strc = str.charAt(0);
//							setVar(in, strc);
//						}
//					} catch (Exception e)
//					{
//					}
//					// try adding as double
//					try
//					{
//						double strd = Double.parseDouble(str);
//						setVar(in, strd);
//					} catch (Exception e)
//					{
//					}
//					// finally try adding as integer
//					try
//					{
//						int stri = Integer.parseInt(str);
//						setVar(in, stri);
//					} catch (Exception e)
//					{
//					}
					setVarRaw(in, str);
					// END KGU#69 2015-11-08
				} catch (EvalError ex)
				{
					result = ex.getMessage();
				}
			}
		// START KGU#39 2015-10-16
		}
		// END KGU#39 2015-10-16

		if (result.equals(""))
		{
			// Actual start of execution 
			result = step(root);
			
			if (result.equals("") && (stop == true))
			{
				result = "Manual break!";
			}
		}

		diagram.redraw();
		if (!result.equals(""))
		{
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

			JOptionPane.showMessageDialog(diagram, result, "Error",
					JOptionPane.ERROR_MESSAGE);
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
					while ((i < posres.count()) && (returned == false))
					{
						Object n = interpreter.get(posres.get(i));
						if (n != null)
						{
							JOptionPane.showMessageDialog(diagram, n,
									"Returned result", JOptionPane.INFORMATION_MESSAGE);
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
	}

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
			pascalFunction = "public double sqr(double d) { return (d) * (d); }";
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
		delay = aDelay;
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
		setVar(name, rawInput);
		// Try some refinement if possible
		if (rawInput instanceof String && !isNumeric(rawInput) )
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
			}
		}
		// try adding as double
		try
		{
			double dblInput = Double.parseDouble(rawInput);
			setVar(name, dblInput);
		} catch (Exception e)
		{
		}
		// finally try adding as integer
		try
		{
			int intInput = Integer.parseInt(rawInput);
			setVar(name, intInput);
		} catch (Exception e)
		{
		}
	}
	
	// METHOD MODIFIED BY GENNARO DONNARUMMA and revised by Kay Gürtzig
	private void setVar(String name, Object content) throws EvalError

	{
		// START KGU#69 2015-11-09: This is only a god idea in case of raw input
		//if (content instanceof String)
		//{
		//	if (!isNumeric((String) content))
		//	{
		//		content = "\"" + (String) content + "\"";
		//	}
		//}
		// END KGU#69 2015-11-08

		// MODIFIED BY GENNARO DONNARUMMA

		if ((name != null) && (name.contains("(")))
		{
			name = name.replace("(", "");
		}
		if ((name != null) && (name.contains(")")))
		{
			name = name.replace(")", "");
		}

		// MODIFIED BY GENNARO DONNARUMMA, ARRAY SUPPORT ADDED
		// Fundamentally revised by Kay Gürtzig 2015-11-08

		String arrayname = null;
		if ((name.contains("[")) && (name.contains("]")))
		{
			arrayname = name.substring(0, name.indexOf("["));
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
			this.interpreter.set(name, content);

			// MODIFIED BY GENNARO DONNARUMMA
			// PREVENTING DAMAGED STRING AND CHARS
			// FIXME (KGU): Seems superfluous or even dangerous
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
					if (oldValue.getClass().getSimpleName().equals("Object[]"))
					{
						// In this case an initialisation expression ("{ ..., ..., ...}") is expected
						String asgnmt = "Object[] " + varName + " = " + newValues[i];
						System.out.println(asgnmt);	// FIXME (KGU) Remove this debug info after test
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
		
		// FIXME (KGU 2015-11-07) Should we replace the interpreter in order to avoid the frequently
		// observed "freezing" after some severe syntax errors in a previous run attempt?

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
		// START KGU#43 2015-10-12: If there is a breakpoint switch to step mode before delay
		checkBreakpoint(element);
		// END KGU#43 2015-10-12
		
		// The Root,  element and the REPEAT loop won't be delayed or halted in the beginning except by their members
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
			
			if (element instanceof Instruction)
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
		}
		return result;
	}

	private String stepRoot(Root element)
	{
		String result = new String();

		int i = 0;
		getExec("init(" + delay + ")");

		element.waited = true;

		// START KGU#77 2015-11-11: Leave if a return statement has been executed
		//while ((i < element.children.children.size())
		//		&& result.equals("") && (stop == false))
		while ((i < element.children.children.size())
				&& result.equals("") && (stop == false) && !returned)
		// END KGU#77 2015-11-11
		{
			result = step(element.children.getElement(i));
			i++;
		}

		delay(); // FIXME Specific pause for root after the last instruction of the program/function
		if (result.equals(""))
		{
			element.clearExecutionStatus();
		}
		return result;
	}

	private String stepInstruction(Instruction element)
	{
		String result = new String();

		StringList sl = element.getText();
		int i = 0;

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
				// START KGU 2015-10-12: Allow to step within an instruction block (but no breakpoint here!) 
				if (i > 0)
				{
					delay();
				}
				// END KGU 2015-10-12
				
				// assignment
				if (cmd.indexOf("<-") >= 0)
				{
					result = tryAssignment(cmd, element instanceof Call && sl.count() == 1);
				}
				// input
				// START KGU#65 2015-11-04: Input keyword should only trigger this if positioned at line start
				//else if (cmd.indexOf(D7Parser.input) >= 0)
				else if (cmd.trim().startsWith(D7Parser.input.trim()))
				// END KGU#65 2015-11-04
				{
					result = tryInput(cmd);
				}
				// output
				// START KGU#65 2015-11-04: Output keyword should only trigger this if positioned at line start
				//else if (cmd.indexOf(D7Parser.output) >= 0)
				else if (cmd.trim().startsWith(D7Parser.output.trim()))
				// END KGU#65 2015-11-04
				{
					result = tryOutput(cmd);
				}
				// return statement
				// START KGU 2015-11-11: "return" ought to be the first word of the instruction,
				// comparison should not be case-sensitive, but a separator would be fine
				//else if (cmd.indexOf("return") >= 0)
				else if (cmd.trim().toLowerCase().matches("return([\\W].*|$)"))
				// END KGU 2015-11-11
				{
					result = tryReturn(cmd);
				} else
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
	
	// START KGU 2015-11-11: Aquivalent decomposition of method stepInstruction
	// Submethod of stepInstruction(Instruction element), handling an assignment
	private String tryAssignment(String cmd, boolean isCall) throws EvalError
	{
		String result = "";
		// TODO KGU#2: In case of a Call element, do we just allow a procedure call or an assignment with just the
		// subroute call on the right-hand side? In a way this makes sense. Then it would be relatively easy to
		// detect and prepare the very subroutine call, in contrast to the occurrence of such a function call to
		// another NSD being allowed at any expression depth?
		String varName = cmd.substring(0, cmd.indexOf("<-"))
				.trim();
		String expression = cmd.substring(
				cmd.indexOf("<-") + 2, cmd.length()).trim();
		// START KGU#2 2015-10-18: Just a preliminary check for the applicability of a cross-NSD subroutine execution!
		if (isCall)
		{
			Function f = new Function(expression);
			if (f.isFunction())
			{
				System.out.println("Looking for SUBROUTINE NSD:");
				System.out.println("--> " + f.getName() + " (" + f.paramCount() + " parameters)");
				Root sub = this.diagram.getRoot().findSubroutineWithSignature(f.getName(), f.paramCount());
				if (sub != null)
				{
					System.out.println("HEUREKA: Matching sub-NSD found for SUBROUTINE CALL!");
					System.out.println("--> " + varName + " <- " + sub.getMethodName() + "(" + sub.getParameterNames().getCommaText() + ")");
				}
			}
		}
		// END KGU#2 2015-10-17

		cmd = cmd.replace("<-", "=");
		// evaluate the expression
		Object n = interpreter.eval(expression);
		if (n == null)
		{
			result = "<"
					+ expression
					+ "> is not a correct or existing expression.";
		} else
		{
			// FIXME: Here setVar is used with already interpreted object...
			setVar(varName, n);
		}

		return result;
		
	}
	
	// Submethod of stepInstruction(Instruction element), handling an input instruction
	private String tryInput(String cmd) throws EvalError
	{
		String result = "";
		String in = cmd.substring(
				cmd.indexOf(D7Parser.input)
						+ D7Parser.input.length()).trim();
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
				// Is bound to fail anyway!
			}
		}
		// END KGU33 2014-12-05
		String str = JOptionPane.showInputDialog(null,
				"Please enter a value for <" + in + ">", null);
		// START KGU#69 2015-11-08: Use specific method for raw input
		// (obsolete code lines removed)
		setVarRaw(in, str);
		// END KGU#69 2015-11-08
		return result;
	}

	// Submethod of stepInstruction(Instruction element), handling an output instruction
	private String tryOutput(String cmd) throws EvalError
	{
		String result = "";
		String out = cmd.substring(
				cmd.indexOf(D7Parser.output)
						+ D7Parser.output.length()).trim();
		Object n = interpreter.eval(out);
		if (n == null)
		{
			result = "<"
					+ out
					+ "> is not a correct or existing expression.";
		} else
		{
			String s = unconvert(n.toString());
			JOptionPane.showMessageDialog(diagram, s, "Output",
					JOptionPane.INFORMATION_MESSAGE);
		}
		return result;
	}

	// Submethod of stepInstruction(Instruction element), handling an output instruction
	private String tryReturn(String cmd) throws EvalError
	{
		String result = "";
		String out = cmd.substring(cmd.indexOf("return") + 6)
				.trim();
		Object n = interpreter.eval(out);
		if (n == null)
		{
			result = "<"
					+ out
					+ "> is not a correct or existing expression.";
		} else
		{
			String s = unconvert(n.toString());
			JOptionPane.showMessageDialog(diagram, s,
					"Returned result", JOptionPane.INFORMATION_MESSAGE);
		}
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
			// TODO KGU 2015-10-13 for the future case that either the Arranger or the Editor itself may
			// administer a set of diagrams: If this element is of class Call and the extracted function name
			// corresponds to one of the NSD diagrams currently opened then try a sub-execution of that diagram.
			// Parts of the parsing code for diagramController will apply for this project as well.
			// But it seems to get more tricky. A function CALL might hide in any expression - so we may have
			// to check interpreter.eval() or write an adapter.
			// START KGU#2 2015-10-17: Just a preliminary check for the applicability of a cross-NSD subroutine execution!
			if (element instanceof Call)
			{
				System.out.println("Looking for SUBROUTINE NSD:");
				System.out.println("--> " + f.getName() + " (" + f.paramCount() + " parameters)");
				Root sub = this.diagram.getRoot().findSubroutineWithSignature(f.getName(), f.paramCount());
				if (sub != null)
				{
					System.out.println("HEUREKA: Matching sub-NSD found for SUBROUTINE CALL!");
					System.out.println("--> " + sub.getMethodName() + "(" + sub.getParameterNames().getCommaText() + ")");
				}
			}
			// END KGU#2 2015-10-17
			if (diagramController != null)
			{
				String params = new String();
				for (int p = 0; p < f.paramCount(); p++)
				{
					try
					{
						Object n = interpreter.eval(f
								.getParam(p));
						if (n == null)
						{
							result = "<"
									+ f.getParam(p)
									+ "> is not a correct or existing expression.";
						} else
						{
							params += "," + n.toString();
						}
					} catch (EvalError ex)
					{
						System.out.println("PARAM: "
								+ f.getParam(p));
						result = ex.getMessage();
					}
				}
				if (result.equals(""))
				{
					if (f.paramCount() > 0)
					{
						params = params.substring(1);
					}
					cmd = f.getName() + "(" + params + ")";
					result = getExec(cmd, element.getColor());
				}
				//delay();
			} else
			{
				interpreter.eval(cmd);
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
					int i = 0;
					// START KGU#77 2015-11-11: Leave if a return statement has been executed
					//while ((i < element.qs.get(q - 1).children.size())
					//		&& result.equals("") && (stop == false))
					while ((i < element.qs.get(q - 1).children.size())
							&& result.equals("") && (stop == false) && !returned)
					// END KGU#77 2015-11-11
					{
						result = step(element.qs.get(q - 1).getElement(i));
						i++;
					}
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
				int i = 0;
				// START KGU#77 2015-11-11: Leave if a return statement has been executed
				//while ((i < branch.children.size())
				//		&& result.equals("") && (stop == false))
				while ((i < branch.children.size())
						&& result.equals("") && (stop == false) && !returned)
				// END KGU#77 2015-11-11
				{
					result = step(branch.getElement(i));
					i++;
				}
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

			int cw = 0;
			Object cond = interpreter.eval(convertStringComparison(condStr));

			if (cond == null)
			{
				result = "<" + condStr
						+ "> is not a correct or existing expression.";
			} else
			{
				// START KGU#77 2015-11-11: Leave if a return statement has been executed
				//while (cond.toString().equals("true") && result.equals("")
				//		&& (stop == false))
				while (cond.toString().equals("true") && result.equals("")
						&& (stop == false) && !returned)
				// END KGU#77 2015-11-11
				{

					element.executed = false;
					element.waited = true;

					int i = 0;
					Subqueue body;
					if (eternal)
					{
						body = ((Forever)element).q;
					}
					else
					{
						body = ((While) element).q;
					}
					// START KGU#77 2015-11-11: Leave if a return statement has been executed
					//while ((i < body.children.size())
					//		&& result.equals("") && (stop == false))
					while ((i < body.children.size())
							&& result.equals("") && (stop == false) && !returned)
					// END KGU#77 2015-11-11
					{
						result = step(body.getElement(i));
						i++;
					}

					element.executed = true;
					element.waited = false;
					if (result.equals(""))
					{
						cw++;
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

			int cw = 0;
			Object n = interpreter.eval(condStr);
			if (n == null)
			{
				result = "<" + condStr
						+ "> is not a correct or existing expression.";
			} else
			{
				do
				{
					int i = 0;
					// START KGU#77 2015-11-11: Leave if a return statement has been executed
					//while ((i < element.q.children.size())
					//		&& result.equals("") && (stop == false))
					while ((i < element.q.children.size())
							&& result.equals("") && (stop == false) && !returned)
					// END KGU#77 2015-11-11
					{
						result = step(element.q.getElement(i));
						i++;
					}

					if (result.equals(""))
					{
						cw++;
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
				// START KGU#77 2015-11-11: Leave if a return statement has been executed
				//} while (!(n.toString().equals("true")) && result.equals("") && (stop == false))
				} while (!(n.toString().equals("true")) && result.equals("") && (stop == false) && !returned);
				// END KGU#77 2015-11-11
				// END KGU#70 2015-11-09
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
			// START KGU#77 2015-11-11: Leave if a return statement has been executed
			//while (((sval >= 0) ? (cw <= fval) : (cw >= fval)) && result.equals("") && (stop == false))
			while (((sval >= 0) ? (cw <= fval) : (cw >= fval)) && result.equals("") && (stop == false) && !returned)
			// END KGU#77 2015-11-11
			{
				setVar(counter, cw);
				element.waited = true;

				int i = 0;
				// START KGU#77 2015-11-11: Leave if a return statement has been executed
				//while ((i < element.q.children.size())
				//		&& result.equals("") && (stop == false))
				while ((i < element.q.children.size())
						&& result.equals("") && (stop == false) && !returned)
				// END KGU#77 2015-11-11
				{
					result = step(element.q.getElement(i));
					i++;
				}
                
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
			int nThreads = element.qs.size();
			// For each of the parallel "threads" fetch a subqueue's Element iterator...
			Vector<Iterator<Element> > undoneThreads = new Vector<Iterator<Element>>();
			for (int thr = 0; thr < nThreads; thr++)
			{
				undoneThreads.add(element.qs.get(thr).children.iterator());
			}

			element.waited = true;
			// Since we can hardly really execute this in parallel here,
			// the workaround is to run all the "threads" in a randomly chosen order...
			Random rdmGenerator = new Random(System.currentTimeMillis());

			// The first condition holds if there is at least one unexhausted "thread"
			// START KGU#77 2015-11-11: Leave if a return statement has been executed
			//while (!undoneThreads.isEmpty() && result.equals("") && (stop == false))
			while (!undoneThreads.isEmpty() && result.equals("") && (stop == false) && !returned)
			// END KGU#77 2015-11-11
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
				}                
			}
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
			System.out.println(e.getMessage() + " on " + varname + " in Executor.getIndexValue()");
		}
		return index;
	}
	// END KGU#33/KGU#34 2014-12-05
	
}

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

import java.awt.Color;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.While;
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
		Regex r;

		// variable assignment
		// START KGU 2014-12-02: To achieve consistency with operator highlighting
		s = s.replace("<--", "<-");
		// END KGU 2014-12-02
		s = s.replace(":=", "<-");

		// testing
		s = s.replace("==", "=");
		s = s.replace("!=", "<>");
		s = s.replace("=", "==");
		s = s.replace("<==", "<=");
		s = s.replace(">==", ">=");
		s = s.replace("<>", "!=");

		s = s.replace(" mod ", " % ");
		s = s.replace(" div ", " / ");
        // START KGU 2014-11-14: Logical operators, too
        s=s.replace(" and ", " && ");
        s=s.replace(" or ", " || ");
        s=s.replace(" not ", " !");
        s=s.replace("(not ", "(!");
        s=s.replace(" not(", " !(");
        s=s.replace("(not(", "(!(");
       	if (s.startsWith("not ")) {
       		s = "!" + s.substring(4);
       	}
       	if (s.startsWith("not(")) {
       		s = "!(" + s.substring(4);
       	}
        s=s.replace(" xor ", " ^ "); // This might cause some operator preference trouble, though       
        // END KGU 2014-11-14

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
		r = new Regex("(.*)\\[(.*)\\](.*)", "$1.charAt($2-1)$3");
		r = new Regex("(.*)\\[(.*)\\](.*)", "$1.substring($2-1,$2)$3");
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
		s = r.replaceAll(s);
		s = s.replace("''", "'");
		// pascal: randomize
		s = s.replace("randomize()", "randomize");
		s = s.replace("randomize", "randomize()");

		// clean up ... if needed
		s = s.replace("Math.Math.", "Math.");

		if (s.indexOf("==") >= 0)
		{
			r = new Regex("(.*)==(.*)", "$1");
			String left = r.replaceAll(s).trim();
			while (Function.countChar(left, '(') > Function
					.countChar(left, ')'))
			{
				left += ')';
			}
			r = new Regex("(.*)==(.*)", "$2");
			String right = r.replaceAll(s).trim();
			while (Function.countChar(right, ')') > Function.countChar(right,
					'('))
			{
				right = '(' + right;
			}
			// ---- thanks to autoboxing, we can alway use the "equals" method
			// ---- to compare things ...
			// addendum: sorry, doesn't always work.
			try
			{
				Object leftO = interpreter.eval(left);
				Object rightO = interpreter.eval(right);
				if ((leftO instanceof String) || (rightO instanceof String))
				{
					s = left + ".equals(" + right + ")";
				}
			} catch (EvalError ex)
			{
				System.err.println(ex.getMessage());
			}
		}
		if (s.indexOf("!=") >= 0)
		{
			r = new Regex("(.*)!=(.*)", "$1");
			String left = r.replaceAll(s).trim();
			while (Function.countChar(left, '(') > Function
					.countChar(left, ')'))
			{
				left += ')';
			}
			r = new Regex("(.*)!=(.*)", "$2");
			String right = r.replaceAll(s).trim();
			while (Function.countChar(right, ')') > Function.countChar(right,
					'('))
			{
				right = '(' + right;
			}
			// ---- thanks to autoboxing, we can always use the "equals" method
			// ---- to compare things ...
			// addendum: sorry, doesn't always work.
			try
			{
				Object leftO = interpreter.eval(left);
				Object rightO = interpreter.eval(right);
				if ((leftO instanceof String) || (rightO instanceof String))
				{
					s = "!" + left + ".equals(" + right + ")";
				}
			} catch (EvalError ex)
			{
				System.err.println(ex.getMessage());
			}
		}

		// System.out.println(s);
		return s;
	}

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
		Root root = diagram.root;

		boolean analyserState = diagram.getAnalyser();
		diagram.setAnalyser(false);
		initInterpreter();
		String result = "";
		returned = false;

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
				// first add as string
				setVar(in, str);
				// try adding as char
				try
				{
					if (str.length() == 1)
					{
						Character strc = str.charAt(0);
						setVar(in, strc);
					}
				} catch (Exception e)
				{
				}
				// try adding as double
				try
				{
					double strd = Double.parseDouble(str);
					setVar(in, strd);
				} catch (Exception e)
				{
				}
				// finally try adding as integer
				try
				{
					int stri = Integer.parseInt(str);
					setVar(in, stri);
				} catch (Exception e)
				{
				}
			} catch (EvalError ex)
			{
				result = ex.getMessage();
			}
		}

		if (result.equals(""))
		{
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
			if ((result != null) && result.contains("Not an array"))
			{
				modifiedResult = modifiedResult.concat(" or the index "
						+ modifiedResult.substring(
								modifiedResult.indexOf("[") + 1,
								modifiedResult.indexOf("]"))
						+ " is out of bounds (invalid index)");
				result = modifiedResult;
			}

			JOptionPane.showMessageDialog(diagram, result, "Error", 0);
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
									"Returned result", 0);
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
			pascalFunction = "public double sqr(Double d) { return Math.pow(d,2); }";
			interpreter.eval(pascalFunction);
			// square root
			pascalFunction = "public double sqrt(Double d) { return Math.sqrt(d); }";
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
		} catch (EvalError ex)
		{
			System.out.println(ex.getMessage());
		}
	}

	public boolean isNumneric(String input)
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
	 * if(content instanceof String) { if(!isNumneric((String) content)) {
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
		synchronized (this)
		{
			stop = aStop;
			paus = false;
			step = false;
			this.notify();
		}
	}

	// METHOD MODIFIED BY GENNARO DONNARUMMA

	private void setVar(String name, Object content) throws EvalError

	{
		if ((content instanceof String))
		{
			if (!isNumneric((String) content))
			{
				content = "\"" + (String) content + "\"";
			}
		}

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

		String arraynname = null;
		if ((name.contains("[")) && (name.contains("]")))
		{
			arraynname = name.substring(0, name.indexOf("["));
			boolean arrayFound = false;
			for (int i = 0; i < this.variables.count(); i++)

			{
				String s = this.variables.get(i);
				if ((s != null) && (s.equals(arraynname)))

				{
					arrayFound = true;
					Object[] objectArray = (Object[]) this.interpreter.get(s);

					// START KGU 2014-12-05: Delegated to a new method (bug fixed)
//					String ind = name.substring(name.indexOf("[") + 1,
//							name.indexOf("]"));
//
//					int index = -1;
//
//					try
//					{
//						index = Integer.parseInt(ind);
//					} catch (Exception e)
//					{
//						index = (Integer) this.interpreter.get(ind);
//					}
					int index = this.getIndexValue(name);
					// END KGU 2014-12-05

					if (index < objectArray.length)

					{
						this.interpreter.set(arraynname, objectArray);
						this.interpreter.set("temp", content);
						this.interpreter.eval(arraynname + "[" + index
								+ "] = temp");
					} else
					{
						Object[] objectArrayTemp = new Object[index + 1];

						this.interpreter.set(arraynname, objectArrayTemp);
						for (int j = 0; j < objectArrayTemp.length; j++)
						{
							if (j < objectArray.length)

							{
								this.interpreter.set("temp", objectArray[j]);
								this.interpreter.eval(arraynname + "[" + j
										+ "] = temp");
							} else if (j < index)

							{
								this.interpreter.set("temp", new Integer(0));
								this.interpreter.eval(arraynname + "[" + j
										+ "] = temp");
							} else
							{
								this.interpreter.set("temp", content);
								this.interpreter.eval(arraynname + "[" + j
										+ "] = temp");
							}
						}
					}
				}
			}
			if (!arrayFound)

			{
				String indexInArrayAssign = name.substring(
						name.indexOf("[") + 1, name.indexOf("]"));
				// START KGU 2014-12-05: Delegated to a new method (bug fixed)
//
//				int index = -1;
//				try
//				{
//					index = Integer.parseInt(indexInArrayAssign);
//				} catch (Exception e)
//				{
//					index = (Integer) this.interpreter.get(indexInArrayAssign);
//				}
				int index = getIndexValue(name);
				// END KGU 2014-12-05

				Object[] arrayNew = new Object[index + 1];

				this.interpreter.set(arraynname, arrayNew);
				this.interpreter.set("temp", content);

				int indexInArrayAssignAsInt = index;
				for (int i = 0; i < (indexInArrayAssignAsInt - 1); i++)
				{
					this.interpreter.eval(arraynname + "[" + i + "] = 0");
				}
				// KGU 2014-12-05: Why isn't just the already extracted index value used here?
				this.interpreter.eval(arraynname + "[" + indexInArrayAssign
						+ "] = temp");
			}
			this.variables.addIfNew(arraynname);
		} else
		{
			this.interpreter.set(name, content);

			// MODIFIED BY GENNARO DONNARUMMA
			// PREVENTING DAMAGED STRING AND CHARS
			if ((content != null) && (content instanceof String))
			{
				content = ((String) content).replaceAll("\"\"", "\"");
			}
			if ((content != null) && (content instanceof Character))
			{
				content = new String("'" + content + "'");
			}
			this.interpreter.eval(name + " = " + content);
			this.variables.addIfNew(name);
		}
		if (this.delay != 0)

		{
			Vector<Vector> vars = new Vector();
			for (int i = 0; i < this.variables.count(); i++)

			{
				Vector myVar = new Vector();
				myVar.add(this.variables.get(i));
				myVar.add(this.interpreter.get(this.variables.get(i)));
				vars.add(myVar);
			}
			this.control.updateVars(vars);
		}
	}

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

	private String step(Element element)
	{
		String result = new String();
		if (element instanceof Root)
		{
			element.selected = true;

			int i = 0;
			getExec("init(" + delay + ")");

			element.waited = true;

			while ((i < ((Root) element).children.children.size())
					&& result.equals("") && (stop == false))
			{
				result = step(((Root) element).children.getElement(i));
				i++;
			}

			if (result.equals(""))
			{
				element.selected = false;
				element.waited = false;
			}
		} else if (element instanceof Instruction)
		{
			element.selected = true;
			if (delay != 0)
			{
				diagram.redraw();
			}

			StringList sl = ((Instruction) element).getText();
			int i = 0;

			while ((i < sl.count()) && result.equals("") && (stop == false))
			{
				String cmd = sl.get(i);
				// cmd=cmd.replace(":=", "<-");
				cmd = convert(cmd);
				try
				{
					// assignment
					if (cmd.indexOf("<-") >= 0)
					{
						String varName = cmd.substring(0, cmd.indexOf("<-"))
								.trim();
						String expression = cmd.substring(
								cmd.indexOf("<-") + 2, cmd.length()).trim();
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
							setVar(varName, n);
						}
						delay();
					}
					// input
					else if (cmd.indexOf(D7Parser.input) >= 0)
					{
						String in = cmd.substring(
								cmd.indexOf(D7Parser.input)
										+ D7Parser.input.length()).trim();
						// START KGU 2014-12-05: We ought to show the index value
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
						// END KGU 2014-12-05
						String str = JOptionPane.showInputDialog(null,
								"Please enter a value for <" + in + ">", null);
						// first add as string
						setVar(in, str);
						// try adding as char
						try
						{
							if (str.length() == 1)
							{
								Character strc = str.charAt(0);
								setVar(in, strc);
							}
						} catch (Exception e)
						{
						}
						// try adding as double
						try
						{
							double strd = Double.parseDouble(str);
							setVar(in, strd);
						} catch (Exception e)
						{
						}
						// finally try adding as integer
						try
						{
							int stri = Integer.parseInt(str);
							setVar(in, stri);
						} catch (Exception e)
						{
						}
					}
					// output
					else if (cmd.indexOf(D7Parser.output) >= 0)
					{
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
									0);
						}
					}
					// return statement
					else if (cmd.indexOf("return") >= 0)
					{
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
									"Returned result", 0);
						}
						returned = true;
					} else
					{
						Function f = new Function(cmd);
						if (f.isFunction())
						{
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
								delay();
							} else
							{
								interpreter.eval(cmd);
							}
						} else
						{
							result = "<" + cmd + "> is not a correct function!";
						}
					}
				} catch (EvalError ex)
				{
					result = ex.getMessage();
				}
				i++;
			}
			if (result.equals(""))
			{
				element.selected = false;
			}
		} else if (element instanceof Case)
		{
			try
			{
				// select the element
				element.selected = true;
				if (delay != 0)
				{
					diagram.redraw();
				}
				// delay for this element!
				element.waited = false;
				delay();

				Case c = (Case) element;
				StringList text = c.getText();
				String expression = text.get(0) + "==";
				boolean done = false;
				int last = text.count() - 1;
				if (text.get(last).trim().equals("%"))
				{
					last--;
				}
				for (int q = 1; (q <= last) && (done == false); q++)
				{
					String test = convert(expression + text.get(q));
					boolean go = false;
					if ((q == last)
							&& !text.get(text.count() - 1).trim().equals("%"))
					{
						go = true;
					}
					if (go == false)
					{
						Object n = interpreter.eval(test);
						go = n.toString().equals("true");
					}
					if (go)
					{
						done = true;
						element.waited = true;
						int i = 0;
						while ((i < c.qs.get(q - 1).children.size())
								&& result.equals("") && (stop == false))
						{
							result = step(c.qs.get(q - 1).getElement(i));
							i++;
						}
						if (result.equals(""))
						{
							element.selected = false;
						}
					}

				}
				if (result.equals(""))
				{
					element.selected = false;
					element.waited = false;
				}
			} catch (EvalError ex)
			{
				result = ex.getMessage();
			}
		} else if (element instanceof Alternative)
		{
			try
			{
				element.selected = true;
				if (delay != 0)
				{
					diagram.redraw();
				}
				// delay for this element!
				element.waited = false;
				delay();

				String s = ((Alternative) element).getText().getText();
				if (!D7Parser.preAlt.equals(""))
				{
					s = BString.replace(s, D7Parser.preAlt, "");
				}
				if (!D7Parser.postAlt.equals(""))
				{
					s = BString.replace(s, D7Parser.postAlt, "");
				}
				// s=s.replace("==", "=");
				// s=s.replace("=", "==");
				// s=s.replace("<==", "<=");
				// s=s.replace(">==", ">=");
				s = convert(s);

				System.out.println("C=  " + interpreter.get("C"));
				System.out.println("IF: " + s);
				Object n = interpreter.eval(s);
				System.out.println("Res= " + n);
				if (n == null)
				{
					result = "<" + s
							+ "> is not a correct or existing expression.";
				}
				// if(getExec(s).equals("OK"))
				else if (n.toString().equals("true"))
				{
					element.waited = true;
					int i = 0;
					while ((i < ((Alternative) element).qTrue.children.size())
							&& result.equals("") && (stop == false))
					{
						result = step(((Alternative) element).qTrue
								.getElement(i));
						i++;
					}
					if (result.equals(""))
					{
						element.selected = false;
					}
				} else
				{
					element.waited = true;
					int i = 0;
					while ((i < ((Alternative) element).qFalse.children.size())
							&& result.equals("") && (stop == false))
					{
						result = step(((Alternative) element).qFalse
								.getElement(i));
						i++;
					}
					if (result.equals(""))
					{
						element.selected = false;
					}
				}
				if (result.equals(""))
				{
					element.selected = false;
					element.waited = false;
				}
			} catch (EvalError ex)
			{
				result = ex.getMessage();
			}
		} else if (element instanceof While)
		{
			try
			{
				element.selected = true;
				if (delay != 0)
				{
					diagram.redraw();
				}

				String s = ((While) element).getText().getText();
				if (!D7Parser.preWhile.equals(""))
				{
					s = BString.replace(s, D7Parser.preWhile, "");
				}
				if (!D7Parser.postWhile.equals(""))
				{
					s = BString.replace(s, D7Parser.postWhile, "");
				}
				// s=s.replace("==", "=");
				// s=s.replace("=", "==");
				// s=s.replace("<==", "<=");
				// s=s.replace(">==", ">=");
				s = convert(s);
				// System.out.println("WHILE: "+s);

				int cw = 0;
				Object n = interpreter.eval(s);
				if (n == null)
				{
					result = "<" + s
							+ "> is not a correct or existing expression.";
				} else
				{
					while (n.toString().equals("true") && result.equals("")
							&& (stop == false))
					{

						// delay this element
						element.waited = false;
						delay();
						element.waited = true;

						int i = 0;
						// START KGU 2010-09-14 The limitation of cw CAUSED
						// eternal loops (rather then preventing them)
						// while (i < ((While) element).q.children.size() &&
						// result.equals("") && stop == false && cw < 100)
						while ((i < ((While) element).q.children.size())
								&& result.equals("") && (stop == false))
						// END KGU 2010-09-14
						{
							result = step(((While) element).q.getElement(i));
							i++;
						}
						if (result.equals(""))
						{
							cw++;
							element.selected = true;
						}
						n = interpreter.eval(s);
						if (n == null)
						{
							result = "<"
									+ s
									+ "> is not a correct or existing expression.";
						}
					}
				}
				if (result.equals(""))
				{
					element.selected = false;
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
		} else if (element instanceof Repeat)
		{
			try
			{
				element.selected = true;
				element.waited = true;
				if (delay != 0)
				{
					diagram.redraw();
				}

				String s = ((Repeat) element).getText().getText();
				if (!D7Parser.preRepeat.equals(""))
				{
					s = BString.replace(s, D7Parser.preRepeat, "");
				}
				if (!D7Parser.postRepeat.equals(""))
				{
					s = BString.replace(s, D7Parser.postRepeat, "");
				}
				// s=s.replace("==", "=");
				// s=s.replace("=", "==");
				// s=s.replace("<==", "<=");
				// s=s.replace(">==", ">=");
				s = convert(s);
				// System.out.println("REPEAT: "+s

				int cw = 0;
				Object n = interpreter.eval(s);
				if (n == null)
				{
					result = "<" + s
							+ "> is not a correct or existing expression.";
				} else
				{
					do
					{
						int i = 0;
						// START KGU 2010-09-14 The limitation of cw CAUSED
						// eternal loops (rather then preventing them)
						// while (i < ((Repeat) element).q.children.size() &&
						// result.equals("") && stop == false && cw < 100)
						while ((i < ((Repeat) element).q.children.size())
								&& result.equals("") && (stop == false))
						// END KGU 2010-09-14
						{
							result = step(((Repeat) element).q.getElement(i));
							i++;
						}

						if (result.equals(""))
						{
							cw++;
							element.selected = true;
						}
						n = interpreter.eval(s);
						if (n == null)
						{
							result = "<"
									+ s
									+ "> is not a correct or existing expression.";
						}

						// delay this element
						element.waited = false;
						delay();
						element.waited = true;

					} while (!(n.toString().equals("true") && result.equals("") && (stop == false)));
				}

				if (result.equals(""))
				{
					element.selected = false;
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
		} else if (element instanceof For)
		{
			try
			{
				element.selected = true;
				if (delay != 0)
				{
					diagram.redraw();
				}

				String str = ((For) element).getText().getText();
                                
                                String pas = "1";
                                if(str.contains(", pas ="))
                                {
                                    String[] pieces = str.split(", pas =");
                                    str=pieces[0];
                                    pas = pieces[1].trim();
                                }
                                
				// cut of the start of the expression
				if (!D7Parser.preFor.equals(""))
				{
					str = BString.replace(str, D7Parser.preFor, "");
				}
				// trim blanks
				str = str.trim();
				// modify the later word
				if (!D7Parser.postFor.equals(""))
				{
					str = BString.replace(str, D7Parser.postFor, "<=");
				}
				// do other transformations
				str = CGenerator.transform(str);
				String counter = str.substring(0, str.indexOf("="));
				// complete

				String s = str.substring(str.indexOf("=") + 1,
						str.indexOf("<=")).trim();
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
				if (n instanceof Long)
				{
					ival = ((Long) n).intValue();
				}
				if (n instanceof Float)
				{
					ival = ((Float) n).intValue();
				}
				if (n instanceof Double)
				{
					ival = ((Double) n).intValue();
				}

				s = str.substring(str.indexOf("<=") + 2, str.length()).trim();
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
				if (n instanceof Long)
				{
					fval = ((Long) n).intValue();
				}
				if (n instanceof Float)
				{
					fval = ((Float) n).intValue();
				}
				if (n instanceof Double)
				{
					fval = ((Double) n).intValue();
				}

				int cw = ival;
				while ((cw <= fval) && result.equals("") && (stop == false))
				{
					setVar(counter, cw);
					// delay for this element!
					element.waited = false;
					delay();
					element.waited = true;

					int i = 0;
					// START KGU 2010-09-14 The limitation of cw CAUSED eternal
					// loops (rather then preventing them)
					// while (i < ((For) element).q.children.size() &&
					// result.equals("") && stop == false && cw < 100)
					while ((i < ((For) element).q.children.size())
							&& result.equals("") && (stop == false))
					// END KGU 2010-09-14
					{
						result = step(((For) element).q.getElement(i));
						i++;
					}
                                        
                                        try
                                        {
                                            cw+=Integer.valueOf(pas);
                                        }
                                        catch(Exception e)
                                        {
                                            cw++;
                                        }
				}
				if (result.equals(""))
				{
					element.selected = false;
					element.waited = false;
				}
			} catch (EvalError ex)
			{
				result = ex.getMessage();
			}
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
	
	// START KGU 2014-12-05
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
	// END KGU 2014-12-05
}

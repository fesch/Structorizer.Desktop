/*
    Structorizer
    A not so little tool anymore, which you can use to create Nassi-Schneiderman Diagrams (NSD)

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

package lu.fisch.diagrcontrol;

/******************************************************************************************************
 *
 *      Author:         Robert Fisch
 *
 *      Description:    Interface providing an API for modules than may be controlled e.g. by Structorizer
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-06-29      Sub-interface FunctionProvidingDiagramControl introduced (for enh. #424))
 *      Kay Gürtzig     2017-10-28      Sub-interface FunctionProvidingDiagramControl integrated and enhanced )
 *      Kay Gürtzig     2018-01-21      Enh. #443, #490: Additional method for retrieval of Java adapter class
 *      Kay Gürtzig     2018-03-21      Issue #463: console output replaced by standard JDK4 (= j.u.l.) logging
 *      Kay Gürtzig     2018-10-12      Issue #622: Logging of API calls introduced (level CONFIG)
 *      Kay Gürtzig     2019-03-02      Issue #366: New methods isFocused() and requestFocus() in analogy to Window
 *      Kay Gürtzig     2020-12-11      Enh. #443: deprecated methods removed
 *      Kay Gürtzig     2020-12-14      References to lu.fisch.util.StringList and java.awt.Color eliminated
 *      Kay Gürtzig     2021-01-09/11   Enh. #910: New methods getEnumerators(), restart()
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      This interface allows to plug in an implementing class in Structorizer in order to control
 *      something via the implementing class.
 *      The methods yielding a look-up table of supported functions or to check whether a given
 *      signature matches with an offered method are necessary because function calls can be nested
 *      such that their identification within a complex expression is required in order to evaluate
 *      the arguments in advance.
 *
 ******************************************************************************************************///

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface for classes that provide an API for being controlled e.g. by executed Structorizer
 * diagrams.<br/>
 * Offers methods to retrieve the set of supported procedures and functions or to check whether
 * a given signature matches with one the offered methods.<br/>
 * NOTE: The standard constructor of implementing classes should produce a light-weight instance
 * for API retrieval (see {@link #getFunctionMap()}, {@link #getProcedureMap()},
 * {@link #providedRoutine(String, int)}, {@link #providesRoutine(String, Object[], boolean)}).
 * Constructors with arguments may result in fully functional heavy-weight singletons or the
 * like. If a light-weight instance is asked to {@link #execute(String, Object[])} then it
 * should convert to a fully functional controller instance if it hadn't already been.
 * @author robertfisch
 * @author Kay Gürtzig
 */
public interface DiagramController
{
	// START KGU#448 2017-10-28: Enh. #443 FunctionProvidingDiagramControl integrated
	@SuppressWarnings("serial")
	public class FunctionException extends RuntimeException {

		public FunctionException() {
			super();
		}

		public FunctionException(Throwable throwable) {
			super(throwable);
		}

		public FunctionException(String msg) {
			super(msg);
		}

		public FunctionException(String msg, Throwable throwable) {
			super(msg, throwable);
		}
	}

	/**
	 * Returns a title for this controller. (The default implementation just
	 * returns the simple class name. Override this if needed.)
	 * 
	 * @return the title string
	 * 
	 * @see #setName(String)
	 */
	public default String getName()
	{
		return this.getClass().getSimpleName();
	}
	
	/**
	 * May allow to set the title or name for this controller (e.g. from plugin).
	 * The default implementation ignores the given {@code title} (i.e. does not
	 * influence the result of inherited {@link #getName()}, so override both if
	 * needed).
	 * 
	 * @param title - the proposed new title string
	 * 
	 * @see #getName()
	 */
	public default void setName(String title)
	{
		
	}
	
	// START KGU#911 2021-01-11: Enh. #910
	/**
	 * Possibly subclassable method for execution startup - must be repeatedly
	 * applicable to the instance.<br/>
	 * The default implementation does not do anything.
	 * 
	 * @param args - array of arguments
	 */
	public default void restart(Object ... args)
	{
	}
	// END KGU#911 2021-01-11
	
	/**
	 * Returns a map associating Strings of format {@code"<function_name>#<arg_count>"}
	 * with a {@link Method} object each for every supported function. The argument
	 * classes and the result class can be obtained from the associated {@link Method}
	 * object. The {@code <function_name>} is supposed to be in lower-case.
	 * 
	 * @return the function map
	 * 
	 * @see #getProcedureMap()
	 * @see #execute(String, Object[])
	 */
	public HashMap<String, Method> getFunctionMap();

	/**
	 * Returns a map associating Strings of format {@code"<procedure_name>#<arg_count>"}
	 * with a {@link Method} object each for every supported procedure. The argument
	 * classes can be obtained from the associated {@link Method} object.
	 * The {@code <procedure_name>} is supposed to be in lower-case.
	 * 
	 * @return the procedure map
	 * 
	 * @see #getFunctionMap()
	 * @see #execute(String, Object[])
	 */
	public HashMap<String, Method> getProcedureMap();

	// START KGU#911 2021-01-09: Enh. #910 This shall facilitate the creation of new controllers
	/**
	 * May provide the Java definition texts of enumerator types to by made
	 * available via Analyser and Executor for argument (de-)coding and possibly
	 * be used by code generators of Structorizer.<br/>
	 * The default implementation returns {@code null}.
	 * 
	 * @return a String array of definitions like "enum TrafficLights {GREEN, YELLOW, RED}",
	 * or {@code null}
	 */
	public default String[] getEnumerators()
	{
		return null;
	}
	// END KGU#911 2021-01-09
	
	// START KGU#446/KGU#448 2018-01-21: Enh. #441, #443 - More generic support for code export
	/**
	 * Checks whether there may be a procedure or function with name {@code name} and
	 * {@code nArguments} arguments where letter case and the category of the routine
	 * are ignored.<br/>
	 * If there is a matching routine then its actual name is returned.<br/>
	 * For a more precise test see {@link #providesRoutine(String, Object[], boolean)}.
	 * 
	 * @param name - case-ignorant routine name
	 * @param nArguments - number of arguments
	 * @return An exact routine name if there is a routine with the roughly given
	 *    signature, {@code null} otherwise.
	 * 
	 * @see #providesRoutine(String, Object[], boolean)
	 */
	public default String providedRoutine(String name, int nArguments)
	{
		String routineName = null;
		String key = name.toLowerCase() + "#" + nArguments;
		Method method = this.getProcedureMap().get(key);
		if (method == null) {
			method = this.getFunctionMap().get(key);
		}
		if (method != null) {
			routineName = method.getName();
		}
		return routineName;
	}
	// END KGU#446/KGU#448 2018-01-21

	/**
	 * Checks whether there is a either a function or procedure exposed for
	 * this API the declared parameters for which match the given arguments
	 * in type.
	 * 
	 * @param name - the function name (lower-case)
	 * @param arguments - Array of the argument values
	 * @param isFunction - true if we need a result value.
	 * @return true if a matching method is provided
	 * 
	 * @see #getFunctionMap()
	 * @see #getProcedureMap()
	 * @see #execute(String, Object[])
	 * @see #providedRoutine(String, int)
	 */
	public default boolean providesRoutine(String name, Object[] arguments, boolean isFunction)
	{
		boolean ok = true;
		String key = name + "#" + arguments.length;
		Method method = this.getProcedureMap().get(key);
		if (method == null || isFunction) {
			method = this.getFunctionMap().get(key);
		}
		if (method != null) {
			Class<?>[] argClasses = method.getParameterTypes();
			for (int i = 0; ok && i < arguments.length; i++) {
				try {
					castArgument(arguments[i], argClasses[i]);
				}
				catch (Exception ex) {
					ok = false;
				}
			}
		}
		else {
			ok = false;
		}
		return ok;
	}

	/**
	 * Executes a function or procedure registered in either the function map
	 * (obtainable by {@link #getFunctionMap()}) or the procedure map
	 * (obtainable via {@link #getProcedureMap()}).
	 * Delay isn't applied here.
	 * 
	 * @param name - the function name (lower-case)
	 * @param arguments - Array of the argument values
	 * @return the obtained result value (as Object)
	 * 
	 * @throws FunctionException
	 * 
	 * @see #getFunctionMap()
	 * @see #getProcedureMap()
	 */
	public default Object execute(String name, Object[] arguments) throws FunctionException
	{
		Object result = null;
		String key = name + "#" + arguments.length;
		String category = "Procedure";
		Method method = this.getProcedureMap().get(key);
		if (method == null) {
			category = "Function";
			method = this.getFunctionMap().get(key);
		}
		if (method != null) {
			Class<?>[] argClasses = method.getParameterTypes();
			for (int i = 0; i < arguments.length; i++) {
				try {
					arguments[i] = castArgument(arguments[i], argClasses[i]);
				}
				catch (Exception ex) {
					FunctionException err = new FunctionException(
							this.getClass().getSimpleName() + ": "
									+ category + " <" + name + "> argument " + (i+1)
									+ ": <" + arguments[i] + "> could not be converted to "
									+ argClasses[i].getSimpleName());
					err.setStackTrace(ex.getStackTrace());
					throw err;
				}
			}
			Logger logger = Logger.getLogger(getClass().getName());
			try {
				// START KGU#597 2018-10-12: Issue #622 - better monitoring of controller activity
				if (logger.isLoggable(Level.CONFIG)) {
					// START KGU#591/KGU#685 2020-12-14: Issues #622, #704: StringList replaced
					//StringList argStrings = new StringList();
					//for (Object arg: arguments) {
					//	argStrings.add(String.valueOf(arg));
					//}
					//logger.config("Executing " + name + "(" + argStrings.concatenate(",") + ")");
					StringBuilder sb = new StringBuilder();
					sb.append("Executing ");
					sb.append(name);
					sb.append("(");
					for (int i = 0; i < arguments.length; i++) {
						if (i > 0) {
							sb.append(",");
						}
						sb.append(String.valueOf(arguments[i]));
					}
					sb.append(")");
					logger.config(sb.toString());
					// END KGU#597/KGU#685 2020-12-14
				}
				// END KGU#597 2018-10-12
				result = method.invoke(this, arguments);
			} catch (Exception e) {
				// START KGU#484 2018-03-21: Issue #463
				//System.err.println("Defective DiagramControl class " + method + ": " + e.toString());
				//e.printStackTrace();
				//Logger logger = Logger.getLogger(getClass().getName());
				logger.log(Level.SEVERE, "Defective DiagramControl class " + method + ": " + e.toString(), e);
				// END KGU#484 2018-03-21
			}
		}
		else {
			throw new FunctionException(
					this.getClass().getSimpleName() + ": No " + category.toLowerCase()
					+ " <" + name + "> with " + arguments.length + " arguments defined.");
		}
		return result;
	}

	/**
	 * Helper method trying to convert the argument type to the declared
	 * parameter type (particularly in case of numbers, since casting between
	 * different Number classes - though assignment-compatible as primitive
	 * types - will fail).
	 * Override this if some more tricky conversions are necessary.
	 * 
	 * @param argument - an evaluated argument object
	 * @param argClass - the declared argument class
	 */
	public default Object castArgument(Object argument, Class<?> argClass) throws ClassCastException
	{
		if (!argClass.isInstance(argument)) {
			if (argument instanceof Number) {
				String clName = argClass.getSimpleName();
				if (clName.equals("Integer")) {
					argument = ((Number)argument).intValue();
				}
				else if (clName.equals("Double")) {
					argument = ((Number)argument).doubleValue();
				}
				else if (clName.equals("Float")) {
					argument = ((Number) argument).floatValue();
				}
				else if (clName.equals("Short")) {
					argument = ((Number) argument).shortValue();
				}
				else if (clName.equals("Long")) {
					argument = ((Number) argument).longValue();
				}
				else if (clName.equals("Byte")) {
					argument = ((Number) argument).byteValue();
				}
			}
		}
		return argClass.cast(argument);
	}
	// END KGU#448 2017-10-28
	
	// START KGU#356 2019-03-02: Issue #366 - Allow focus control of he DiagramController copes with it
	/**
	 * Returns whether this DiagramController is or has a focusable window and
	 *    if this window is focused.
	 * @return whether this DiagramController is focused (default: false)
	 */
	public default boolean isFocused()
	{
		return false;
	}
	
	/**
	 * If this DiagramController is or has a focusable window then it will have
	 * this window request the focus. (Default: doesn't do anything.)
	 */
	public default void requestFocus() {}
	// END KGU#356 2019-03-02
	
// START KGU#448/KGU#673 2020-12-11: Enh. #443, eventually deleted
//	@Deprecated
//	public String execute(String message);
//	@Deprecated
//	public String execute(String message, Color color);
// END KGU#448/KGU#673 2020-12-11
}

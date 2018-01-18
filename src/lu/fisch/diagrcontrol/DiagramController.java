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
 *      Kay Gürtzig     2017.06.29      Sub-interface FunctionProvidingDiagramControl introduced (for enh. #424))
 *      Kay Gürtzig     2017.10.28      Sub-interface FunctionProvidingDiagramControl integrated and enhanced )
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      This interface allows to plugin an implementing class in Structorizer in order to control
 *      something via the implementing class.
 *      The methods yielding a look-up table of supported functions or to check whether a given
 *      signature matches with an offered method are necessary because function calls can be nested
 *      such that their identification within a complex expression is required in order to evaluate
 *      the arguments in advance.
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Interface for classes that provide an API for being controlled e.g. by executed Structorizer
 * diagrams.
 * Offers methods to retrieve the set of supported procedures and functions or to check whether
 * a given signature matches with one the offered methods.
 * @author robertfisch
 * @author Kay Gürtzig
 */
public interface DiagramController
{
	// START KGU# 2017-10-28: Enh. #443 FunctionProvidingDiagramControl integrated
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
	 * Returns a title for this controller
	 * (The default implementation just returns the simple class name. Override this if needed) 
	 * @return the title string
	 * @see #setName(String)
	 */
	public default String getName()
	{
		return this.getClass().getSimpleName();
	}
	
	/**
	 * May allow to set the title or name for this controller (e.g. from plugin)
	 * The default implementation ignores the given {@code title} (i.e. does not influence
	 * the result of {@link #getName()}, so override both if needed).
	 * @param title - the proposed new title string
	 * @see #getName()
	 */
	public default void setName(String title)
	{
		
	}
	
    /**
     * Returns a map associating Strings of format {@code"<function_name>#<arg_count>"}
     * with a {@link Method} object each for every supported function. The argument classes
     * and the result class can be obtained from the associated {@link Method} object.
     * @return the function map
     * @see #getProcedureMap()
     * @see #execute(String, Object[])
     */
    public HashMap<String, Method> getFunctionMap();

    /**
     * Returns a map associating Strings of format {@code"<procedure_name>#<arg_count>"}
     * with a {@link Method} object each for every supported procedure. The argument classes
     * can be obtained from the associated {@link Method} object.
     * @return the procedure map
     * @see #getFunctionMap()
     * @see #execute(String, Object[])
     */
    public HashMap<String, Method> getProcedureMap();
    
    /**
     * Checks whether there is a either a function or procedure exposed for this API the
     * declared parameters for which match the given arguments in type.
     * @param name - the function name (lower-case)
     * @param arguments - Array of the argument values
     * @param isFunction - true if we need a result value.
     * @return true if a matching method is provided
     * @see #getFunctionMap()
     * @see #getProcedureMap()
     * @see #execute(String, Object[])
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
     * Executes a function or procedure registered in either the function map (obtainable
     * by {@link #getFunctionMap()}) or the procedure map (obtainable via {@link #getProcedureMap()})
     * Delay isn't applied here.
     * @param name - the function name (lower-case)
     * @param arguments - Array of the argument values
     * @return the obtained result value (as Object) 
     * @throws FunctionException
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
    		try {
				result = method.invoke(this, arguments);
			} catch (Exception e) {
				System.err.println("Defective DiagramControl class " + method + ": " + e.toString());;
				e.printStackTrace();
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
	 * Helper method trying to convert the argument type to the declared parameter type
	 * (particularly in case of numbers, since casting between different Number classes
	 * - though assignment-compatible as primitive types - will fail).
	 * Override this if some more tricky conversions are necessary. 
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
    // END KGU 2017-10-28
	
    @Deprecated
    public String execute(String message);
    @Deprecated
    public String execute(String message, Color color);
}

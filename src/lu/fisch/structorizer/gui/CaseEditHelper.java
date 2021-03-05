/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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

package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Facade class for retrieval and checking of Case options
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-02-05      First Issue for #915
 *      Kay Gürtzig     2021-02-10      Method checkValues now does the conversion of broken lines
 *                                      Method evaluateExpression now tries evaluation with Math. prefix
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import bsh.EvalError;
import bsh.Interpreter;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.utils.StringList;

/**
 * Helper class for InputBoxCase
 * @author Kay Gürtzig
 *
 */
public class CaseEditHelper {
	
	private Root root;
	private Interpreter interpreter;
	private boolean initialized = false;
	
	public CaseEditHelper(Root aRoot)
	{
		root = aRoot;
		interpreter = new Interpreter();
	}
	
	/**
	 * Tries to identify the type of the given expression. Returns {@code true}
	 * if this succeeds and the type represents an array or record.
	 * @param expr - the expression to be checked as string
	 * @return {@code true} in case of unambiguous structured type, {@code false}
	 * otherwise
	 */
	public boolean isStructured(String expr)
	{
		String typeName = Root.identifyExprType(root.getTypeInfo(), expr, true);
		if (typeName.startsWith("@") || typeName.startsWith("$")) {
			return true;
		}
		TypeMapEntry type = root.getTypeInfo().get(":" + typeName);
		if (type != null) {
			return type.isArray() || type.isRecord();
		}
		return false;
	}
	
	/**
	 * For the supposed enumerator variable {@code name} (might be a qualified name),
	 * returns either {@code null} if no enumerator type association can be found) or
	 * a map of enumerator constant names and the associated code (integer value).
	 * @param name - (possibly qualified) name of an assumed variable
	 * @return - either {@code null} or a name-value map for the enumerator constants
	 */
	public HashMap<String, Integer> getEnumeratorInfo(String name)
	{
		HashMap<String, Integer> enumValues = null;
		String typeName = Root.identifyExprType(root.getTypeInfo(), name, true);
		TypeMapEntry type = root.getTypeInfo().get(":" + typeName);
		if (type != null && type.isEnum()) {
			StringList enumNames = type.getEnumerationInfo();
			if (enumNames != null && !enumNames.isEmpty()) {
				this.ensureInterpreterInitialization();
				enumValues = new LinkedHashMap<String, Integer>();
				for (int i = 0; i < enumNames.count(); i++) {
					String constName = enumNames.get(i);
					try {
						Object val = interpreter.get(constName);
						if (val instanceof Integer) {
							enumValues.put(constName, (Integer)val);
						}
					} catch (EvalError exc) {
						// TODO Auto-generated catch block
						exc.printStackTrace();
					}
				}
			}
		}
		return enumValues;
	}
	
	/**
	 * For the given expression tries to compute the value if it is a constant expression.
	 * @param expr - an expression
	 * @return {@code null} if the evaluation failed or the resulting (constant) value
	 */
	public Object evaluateExpression(String expr)
	{
		Object value = null;
		if (Function.testIdentifier(expr, false, null)) {
			try {
				value = interpreter.get(expr);
			}
			catch (EvalError exc) {}
			if (value == null) {
				try {
					value = interpreter.eval(root.getConstValueString(expr) + ";");
				}
				catch (EvalError exc) {}
			}
		}
		else if (Function.isFunction(expr, false)) {
			try {
				// Might help - if not: there is a next try
				value = interpreter.eval("Math." + expr + ";");
			}
			catch (EvalError exc) {}
		}
		if (value == null) {
			try {
				value = interpreter.eval(expr + ";");
			}
			catch (EvalError exc) {}
		}
		return value;
	}
	
	/**
	 * For the given list {@code valueLists} of expression lists, checks what values are
	 * among them and assigns them the indices of the expression lists they occur in if
	 * they occur several times.
	 * @param valueLists - The list of comma-separated value lists (where the "values"
	 *       might be represented by (constant) expressions). Contained soft line break
	 *       place holders "\n" will be replaced by spaces.
	 * @param conflictsOnly - if {@code true} then only conflicting entries will contained
	 *       in the result.
	 * @return a lookup table mapping each found value to the line indices of its occurrence
	 *       (in case {@code conflictsOnly} is {@code true} only entries with conflicts
	 *       - either between several lines or between expressions of a single line - will
	 *       be contained.
	 */
	public HashMap<String, ArrayList<Integer>> checkValues(StringList valueLists, boolean conflictsOnly)
	{
		HashMap<String, ArrayList<Integer>> valueMap = new HashMap<String, ArrayList<Integer>>();
		if (!valueLists.isEmpty()) {
			this.ensureInterpreterInitialization();
		}
		for (int i = 0; i < valueLists.count(); i++) {
			/* We cannot directly replace the pseudo newlines by blank because
			 * this might modify possible string or character literals - as
			 * unlikely their occurrence might be here. So we use tango step
			 * via tokenization.
			 */
			String line = valueLists.get(i).replace("\\n", "\n");
			StringList tokens = Element.splitLexically(line, true);
			tokens.replaceAll("\n", " ");
			line = tokens.concatenate().replace("\n", "\\n");
			StringList items = Element.splitExpressionList(line, ",");
			for (int j = 0; j < items.count(); j++) {
				String valStr = items.get(j);
				Object value = evaluateExpression(valStr);
				if (value != null) {
					valStr = string4Value(value);
				}
				ArrayList<Integer> occurrences = valueMap.get(valStr);
				if (occurrences == null) {
					occurrences = new ArrayList<Integer>();
					valueMap.put(valStr, occurrences);
				}
				// This way, line-internal duplications will also be detected
				occurrences.add(i);
			}
		}
		if (conflictsOnly)
		{
			Object[] values = valueMap.keySet().toArray();
			for (Object value: values) {
				if (valueMap.get(value).size() == 1) {
					valueMap.remove(value);
				}
			}
		}
		return valueMap;
	}

	/**
	 * Converts the given value into a sensible describing string
	 * @param value - some evaluated object
	 * @return a String representation for {@code Value}
	 */
	private String string4Value(Object value) {
		String str = "null";
		if (value instanceof String) {
			str = "\"" + value + "\"";
		}
		else if (value instanceof Character) {
			str = "'" + value + "'";
		}
		else {
			str = String.valueOf(value);
		}
		return str;
	}

	/**
	 * Ensures an initialised Interpreter instance.
	 * @return {@code false} if some evaluation error occurred
	 */
	private boolean ensureInterpreterInitialization()
	{
		boolean clean = true;
		if (!initialized) {
			try {
				interpreter.eval("Infinity = Double.POSITIVE_INFINITY;");
			} catch (EvalError exc1) {
				clean = false;
				// Just ignore it in future
				exc1.printStackTrace();
			}
			for (String constName: root.constants.keySet()) {
				String valStr = root.getConstValueString(constName);
				try {
					interpreter.eval(constName + " = " + valStr);
				} catch (EvalError exc) {
					clean = false;
					// Just ignore it in future
					exc.printStackTrace();
				}
			}
		}
		return clean;
	}

}

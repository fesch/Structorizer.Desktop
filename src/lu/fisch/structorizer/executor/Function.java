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
 *      Description:    Auxiliary class to check function syntax and to separate signature parts.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch                       First Issue
 *      Kay Gürtzig     2015-10-27      For performance reasons, now stores name and parsed parameters
 *      Kay Gürtzig     2015-11-13      KGU#2 (Enhancement #9): No longer automatically renames to lowercase
 *      Kay Gürtzig     2015-12-12      KGU#106: Parameter splitting mended (using enhancement #54 = KGU#101)
 *      Kay Gürtzig     2016-12-22      KGU#311: New auxiliary method getSourceLength()
 *      Kay Gürtzig     2017-01-29      Enh. #335: Enhancements for better type analysis
 *      Kay Gürtzig     2020-10-16      Bugfix #874: Too strict identifier check
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.Map;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.utils.StringList;

/**
 * Represents and analyzes an expression or declaration looking like a subprogram call.
 * May confirm function syntax resemblance and report name and parameter strings.
 * @author robertfisch
 */
public class Function
{
	// START KGU#332 2017-01-29: Enh. #335 - result type forecast
	private static final Map<String, String> knownResultTypes;
	static {
		knownResultTypes = new HashMap<String,String>();
		//knownResultTypes.put("abs#1", "numeric");
		//knownResultTypes.put("max#2", "numeric");
		//knownResultTypes.put("min#2", "numeric");
		knownResultTypes.put("round#1", "int");
		knownResultTypes.put("ceil#1", "double");
		knownResultTypes.put("floor#1", "double");
		knownResultTypes.put("sqr#1", "double");
		knownResultTypes.put("sqrt#1", "double");
		knownResultTypes.put("exp#1", "double");
		knownResultTypes.put("log#1", "double");
		knownResultTypes.put("pow#2", "double");
		knownResultTypes.put("cos#1", "double");
		knownResultTypes.put("sin#1", "double");
		knownResultTypes.put("tan#1", "double");
		knownResultTypes.put("acos#1", "double");
		knownResultTypes.put("asin#1", "double");
		knownResultTypes.put("atan#1", "double");
		knownResultTypes.put("toRadians#1", "double");
		knownResultTypes.put("toDegrees#1", "double");
		knownResultTypes.put("random#1", "int");
		knownResultTypes.put("length#1", "int");
		knownResultTypes.put("lowercase#1", "String");
		knownResultTypes.put("uppercase#1", "String");
		knownResultTypes.put("pos#2", "int");
		knownResultTypes.put("copy#3", "string");
		knownResultTypes.put("trim#1", "String");
		knownResultTypes.put("ord#1", "int");
		knownResultTypes.put("chr#1", "char");
		knownResultTypes.put("isArray#1", "boolean");
		knownResultTypes.put("isChar#1", "boolean");
		knownResultTypes.put("isBool#1", "boolean");
		knownResultTypes.put("inc#2", "void");
		knownResultTypes.put("dec#2", "void");
		knownResultTypes.put("randomize#0", "void");
		knownResultTypes.put("insert#3", "void");
		knownResultTypes.put("delete#3", "void");
		knownResultTypes.put("fileOpen#1", "int");
		knownResultTypes.put("fileCreate#1", "int");
		knownResultTypes.put("fileAppend#1", "int");
		knownResultTypes.put("fileEOF#1", "boolean");
		//knownResultTypes.put("fileRead", "Object");
		knownResultTypes.put("fileReadChar#1", "char");
		knownResultTypes.put("fileReadInt#1", "int");
		knownResultTypes.put("fileReadDouble#1", "double");
		knownResultTypes.put("fileReadLine#1", "String");
		knownResultTypes.put("fileWrite#2", "void");
		knownResultTypes.put("fileWriteLine#2", "void");
		knownResultTypes.put("fileClose#1", "void");
	}
	// END KGU#332 2017-01-29
    private String str = new String();		// The original string this is derived from
    // START KGU#56 2015-10-27: Performance improvement approach (bug fixed 2015-11-09)
    private StringList parameters = null;	// parameter strings as split by commas
    private boolean isFunc = false;			// basic syntactic plausibility check result
    private String name = null;				// The string before the opening parenthesis
    // END KGU#56 2015-10-27
//    // START KGU#341 2017-02-06: Cache the case-apdapted name if there is a matching built-in function
//    private String caseAlignedName = null;
//    // END KGU#341 2017-02-06

    public Function(String exp)
    {
        this.str = exp.trim();
        // START KGU#56 2015-10-27
        // START KGU#332 2017-01-29: Enh. #335 We need a more precise test
        //int posLP = str.indexOf("(");
        //this.isFunc =
        //		posLP < str.indexOf(")") && posLP >=0 &&
        //        countChar(str,'(') == countChar(str,')') &&
        //        str.endsWith(")");
        this.isFunc = isFunction(this.str);
        // END KGU#332 2017-01-29
        if (this.isFunc)
        {
            int posLP = str.indexOf("(");
        	// START KGU#2 (#9) 2015-11-13: In general, we don't want to flatten the case!
        	//this.name = str.substring(0, posLP).trim().toLowerCase();
        	this.name = str.substring(0, posLP).trim();
        	// END KGU#2 (#9) 2015-11-13
        	String params = str.substring(posLP+1, str.length()-1).trim();
        	if (!params.equals(""))
        	{
        		// START KGU#106 2015-12-12: Face nested function calls with comma-separated arguments!
        		//this.parameters = StringList.explode(params, ",");
        		this.parameters = Element.splitExpressionList(params, ",");
        		// END KGU#106 2015-12-12
            }
//            // START KGU#341 2017-02-06
//            String nPars = Integer.toString(this.parameters.count());
//            for (String key: knownResultTypes.keySet()) {
//                String[] parts = key.split("#");
//                if (name.equalsIgnoreCase(parts[0]) && parts[1].equals(nPars)) {
//                    this.caseAlignedName = parts[0];
//                    break;
//                }
//            }
//            // END KGU#341 2017-02-06
        }
        // END KGU#56 2015-10-27
    }

    // This is just a very general string helper function 
    public static int countChar(String s, char c)
    {
        int res = 0;
        for (int i=0; i<s.length(); i++)
        {
            if (s.charAt(i)==c)
            {
                res++;
            }
        }
        return res;
    }

    public boolean isFunction()
    {
    	// START KGU#56 2015-10-27 Analysis now already done by constructor
//        return str.indexOf("(")<str.indexOf(")") &&
//               str.indexOf("(")>=0 &&
//               countChar(str,'(')==countChar(str,')') &&
//               str.endsWith(")");
		// START KGU#61 2016-03-22: We must not accept names containing e.g. blanks
		// (though dots as in method invocations should be accepted)
    	//return this.isFunc;
    	return this.isFunc && testIdentifier(this.name, ".");
		
		// END KGU#61 2016-03-22
    	// END KGU#56 2015-10-27
    }

    // START KGU#332 2017-01-29: Enh. #335
    /**
     * Tests whether the passed-in expression expr may represent a subroutine call
     * i.e. consists of an identifier followed by a parenthesized comma-separated
     * list of argument expressions
     * @param expr - an expression
     * @return true if the expression has got function call syntax
     */
    public static boolean isFunction(String expr)
    {
    	expr = expr.trim();
        int posLP = expr.indexOf("(");
        // START KGU#560 2018-07-22: Bugfix #564 - Parenthesis may not be on index 0
        //boolean isFunc = posLP < expr.indexOf(")") && posLP >=0 &&
        boolean isFunc = posLP < expr.indexOf(")") && posLP >0 &&
        // END KGU#560 2018-07
        		countChar(expr,'(') == countChar(expr,')') &&
        		expr.endsWith(")");
        // The test above is way too easy, it would also hold for e.g. "(a+b)*(c+d)";
        // So we restrict the result in the following
        if (isFunc) {
        	isFunc = testIdentifier(expr.substring(0, posLP), null);
        	// Tokenize string between the outer parentheses 
        	StringList tokens = Element.splitLexically(expr.substring(posLP+1, expr.length()-1), true);
        	int parLevel = 0;	// parenthesis level, must never get < 0
        	for (int i = 0; isFunc && i < tokens.count(); i++) {
        		String token = tokens.get(i);
        		if (token.equals("(")) parLevel++;
        		else if (token.equals(")")) {
        			isFunc = --parLevel >= 0;
        		}
        	}
        }
        return isFunc;
    }
    // END KGU#332 2017-01-29
    
    // START KGU 2017-02-21: signature string uniform with Root.getSignatureString(false)
    /**
     * Returns a string composed of the function name and the number of arguments in parentheses,
     * e.g. {@code "sub0815(4)"}.
     * @return the signature string
     */
    public String getSignatureString()
    {
    	String sigStr = null;
    	if (this.isFunc) {
    		sigStr = this.getName() + "(" + this.paramCount() + ")";
    	}
    	return sigStr;
    }
    // END KGU 2017-02-21
    
    public String getName()
    {
    	// START KGU#56 2015-10-27: Analysis now already done by the constructor
        //if (isFunction())
        //    return str.trim().substring(0,str.trim().indexOf("(")).trim().toLowerCase();
        //else
        //    return null;
        // END KGU#56 2015-10-27
        return this.name;
    }
    
//    // START KGU 2017-02-06: Support for case-ignorant mode
//    /**
//     * Returns the subroutine name unitized in case to a built-in subroutine
//     * with otherwise matching signature if ignoreCase is true (otherwise as is)
//     * @param ignoreCase - whether the name is to be adapted to a built-in one
//     * @return Subroutine name adapted in case
//     */
//    public String getName(boolean ignoreCase)
//    {
//        return ignoreCase ? this.caseAlignedName : this.name;
//    }
//    
//    /**
//     * Returns the originating command with a function name unitized in case to a
//     * built-in function if ignoreCase is true and the signature only differs in case. 
//     * @param ignoreCase - whether the name is to be adapted to a built-in one
//     * @return Subroutine call with adapted subroutine name
//     */
//    public String getInvokation(boolean ignoreCase)
//    {
//        String invok = this.str;
//        if (this.isFunc && ignoreCase) {
//    	    invok = this.getName(ignoreCase) + this.str.substring(this.name.length());
//        }
//        return invok;
//    }
//    // END KGU 2017-02-06

    public int paramCount()
    {
    	// START KGU#56 2015-10-27: Performance improvements
//        if (isFunction())
//        {
//            String params = str.trim().substring(str.trim().indexOf("(")+1,str.length()-1).trim();
//            if(!params.equals(""))
//            {
//                StringList sl = StringList.explode(params,",");
//                return sl.count();
//            }
//            else return 0;
//        }
    	if (this.parameters != null)
    	{
    		return this.parameters.count();
    	}
        // END KGU#56 2015-10-27
        else return 0;
    }

    public String getParam(int count)
    {
    	// START KGU#56 2015-10-27: Analysis now only done once by the constructor
//        if (isFunction())
//        {
//            String params = str.trim().substring(str.trim().indexOf("(")+1,str.length()-1).trim();
//            if(!params.equals(""))
//            {
//                StringList sl = StringList.explode(params,",");
//                return sl.get(count);
//            }
//            else return null;
//        }
    	if (this.parameters != null)
    	{
    		return this.parameters.get(count);
    	}
        // END KGU#56 2015-10-27
        else return null;
    }
    
    // START KGU#332 2017-01-29: Enh. #335 - type map
    /**
     * Returns the name of the result type of this subroutine call if known as
     * built-in function with unambiguous type.
     * If this is known as built-in procedure then it returns "void".
     * If unknown then returns the given defaultType
     * @param defaultType - null or some default type name for unsuccessful retrieval
     * @return name of the result type (Java type name)
     */
    public String getResultType(String defaultType)
    {
//    	return getResultType(false, defaultType);
    	String type = knownResultTypes.get(this.name + "#" + this.paramCount());
    	if (type == null) {
    		type = defaultType;
    	}
    	return type;
    }
    
//    /**
//     * Returns the name of the result type of this subroutine call if known as
//     * built-in function with unambiguous type.
//     * If this is known as built-in procedure then it returns "void".
//     * If unknown then returns the given defaultType
//     * @param defaultType - null or some default type name for unsuccessful retrieval
//     * @return name of the result type (Java type name)
//     */
//    public String getResultType(boolean ignoreCase, String defaultType)
//    {
//        String type = knownResultTypes.get(this.getName(ignoreCase) + "#" + this.paramCount());
//        if (type == null) {
//            type = defaultType;
//        }
//        return type;
//    }
    // END KGU#332 2017-01-29

    // START KGU#61 2016-03-22: Moved hitherto from Root (was a private member method there)
    /**
     * Checks identifier syntax (i.e. ASCII letters, digits, underscores, and possibly dots)
     * @param _str - the identifier candidate
     * @param _alsoAllowedChars - a String containing additionally accepted characters (e.g. ".") or null
     * @return true iff _str complies with the strict identifier syntax convention (plus allowed characters)
     */
    public static boolean testIdentifier(String _str, String _alsoAllowedChars)
    {
    	_str = _str.trim().toLowerCase();
    	// START KGU#877 2020-10-16: Bugfix #874 - we should tolerate non-ascii letters
    	//boolean isIdent = !_str.isEmpty() &&
    	//		('a' <= _str.charAt(0) && 'z' >= _str.charAt(0) || _str.charAt(0) == '_');
    	boolean isIdent = false;
    	if (!_str.isEmpty()) {
    		char firstChar = _str.charAt(0);
    		isIdent = Character.isLetter(firstChar) || firstChar == '_';
    	}
    	// END KGU#877 2020-10-16
    	if (_alsoAllowedChars == null)
    	{
    		_alsoAllowedChars = "";
    	}
    	for (int i = 1; isIdent && i < _str.length(); i++)
    	{
    		char currChar = _str.charAt(i);
    		if (!(
    				// START KGU#877 2020-10-16: Bugfix #874 - we should tolerate non-ascii letters
    				//('a' <= currChar && currChar <= 'z')
    				Character.isLetterOrDigit(currChar)
    				// END KGU#877 2020-10-16
    				||
    				('0' <= currChar && currChar <= '9')
    				||
    				(currChar == '_')
    				||
    				_alsoAllowedChars.indexOf(currChar) >= 0
    				))
    			// END KGU 2015-11-25
    		{
    			isIdent = false;
    		}
    	}
    	return isIdent;
    }
    // END KGU#61 2016-03-22
    
    // START KGU 2016-10-16: More informative self-description
    public String toString()
    {
    	String paramNames = this.parameters == null ? "" : this.parameters.concatenate(", ");
    	return getClass().getSimpleName() + '@' + Integer.toHexString(hashCode()) +
    			": " + this.getName() + "(" + paramNames + ")";
    }
    // END KGU# 2016-10-16

    // START KGU#311 2016-12-22: Enh. #314 - We need the expression length for replacements
    /**
     * Returns the length of the original String that was parsed to this Function
     * @return length of the code source snippet representing this function call
     */
    public int getSourceLength()
    {
    	return str.length();
    }
    // END KGU#311 2016-12-22
    
}

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
 *      Kay Gürtzig     2015.10.27      For performance reasons, now stores name and parsed parameters
 *      Kay Gürtzig     2015.11.13      KGU#2 (Enhacement #9): No longer automatically renames to lowercase
 *      Kay Gürtzig     2015.12.12      KGU#106: Parameter splitting mended (using enhancement #54 = KGU#101)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import lu.fisch.structorizer.elements.Element;
import lu.fisch.utils.StringList;

/**
 * Represents and analyzes an expression or declaration looking like a subprogram call.
 * May confirm function syntax resemblance and report name and parameter strings.
 * @author robertfisch
 */
public class Function
{
    private String str = new String();
    // START KGU#56 2015-10-27: Performance improvement approach (bug fixed 2015-11-09)
    private StringList parameters = null;	// parameter strings as split by commas
    private boolean isFunc = false;			// basic syntactic plausibility check result
    private String name = null;				// The string before the opening parenthesis
    // END KGU#56 2015-10-27

    public Function(String exp)
    {
        this.str = exp.trim();
        // START KGU#56 2015-10-27
        int posLP = str.indexOf("(");
        this.isFunc =
        		posLP < str.indexOf(")") && posLP >=0 &&
                countChar(str,'(') == countChar(str,')') &&
                str.endsWith(")");
        if (this.isFunc)
        {
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
        }
        // END KGU 2015-10-27
    }

    // This is just a very general string helper function 
    public static int countChar(String s, char c)
    {
        int res = 0;
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i)==c)
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

    // START KGU#61 2016-03-22: Moved hitherto from Root (was a private member method there)
    /**
     * Checks identifier syntax (i.e. ASCII letters, digits, underscores, and possibly dots)
     * @param _str - the identifier candidate
     * @param _refuseDots - whether dots like in qualified method names are allowed 
     * @return true iff _str complies with the strict identifier syntax convention
     */
    public static boolean testIdentifier(String _str, String _alsoAllowedChars)
    {
    	_str = _str.trim().toLowerCase();
    	boolean isIdent = !_str.isEmpty() &&
    			('a' <= _str.charAt(0) && 'z' >= _str.charAt(0) || _str.charAt(0) == '_');
    	if (_alsoAllowedChars == null)
    	{
    		_alsoAllowedChars = "";
    	}
    	for (int i = 1; isIdent && i < _str.length(); i++)
    	{
    		char currChar = _str.charAt(i);
    		if (!(
    				('a' <= currChar && currChar <= 'z')
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
    
}

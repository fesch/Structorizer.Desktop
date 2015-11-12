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

import lu.fisch.utils.StringList;

/**
 * Represents and analyzes an expression or declaration looking like a subprogram call.
 * May confirm function syntax resemblance and report name and parameter strings.
 * @author robertfisch
 */
// TODO KGU 2015-10-13: For performance reasons, this class should hold the name and the list of parsed parameters as fields
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
        	this.name = str.substring(0, posLP).trim().toLowerCase();
        	String params = str.substring(posLP+1, str.length()-1).trim();
        	if (!params.equals(""))
        	{
        		this.parameters = StringList.explode(params, ",");
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
    	return this.isFunc;
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

}

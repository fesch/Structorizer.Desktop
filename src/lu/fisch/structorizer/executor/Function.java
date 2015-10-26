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
 *
 * @author robertfisch
 */
public class Function
{
    private String str = new String();

    public Function(String exp)
    {
        this.str=exp.trim();
    }

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
        return str.indexOf("(")<str.indexOf(")") &&
               str.indexOf("(")>=0 &&
               countChar(str,'(')==countChar(str,')') &&
               str.endsWith(")");
    }

    public String getName()
    {
        if (isFunction())
            return str.trim().substring(0,str.trim().indexOf("(")).trim().toLowerCase();
        else
            return null;
    }

    public int paramCount()
    {
        if (isFunction())
        {
            String params = str.trim().substring(str.trim().indexOf("(")+1,str.length()-1).trim();
            if(!params.equals(""))
            {
                StringList sl = StringList.explode(params,",");
                return sl.count();
            }
            else return 0;
        }
        else return 0;
    }

    public String getParam(int count)
    {
        if (isFunction())
        {
            String params = str.trim().substring(str.trim().indexOf("(")+1,str.length()-1).trim();
            if(!params.equals(""))
            {
                StringList sl = StringList.explode(params,",");
                return sl.get(count);
            }
            else return null;
        }
        else return null;
    }

}

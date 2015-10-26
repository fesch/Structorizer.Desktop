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

package lu.fisch.utils;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Stimple utility class to work with strings.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2003.05.10      First Issue
 *		Bob Fisch		2007.12.09		Moved to another package and adapted for Structorizer
 *
 ******************************************************************************************************
 *
 *      Comment:		This is one of the first utility classes I wrote while learning Java!
 *
 ******************************************************************************************************/


import java.util.*;

public abstract class BString
	{
		
		/**
		 * Encodes some caracters to HTML-encoded symbols
		 *@return The encoded string
		 *@param str The string to encode
		 */
		public static String encodeToHtml(String str)
		{
			str=BString.replace(str,"&","&amp;");
//			str=BString.replace(str,"<","&lt;");
			str=BString.replace(str,"<","&#60;");
//			str=BString.replace(str,">","&gt;");
			str=BString.replace(str,">","&#62;");
//			str=BString.replace(str,"\"","&quot;");
			str=BString.replace(str,"\"","&#34;");
			//str=BString.replace(str," ","&nbsp;");
			
			String res = null;
			try {
				byte[] utf8 = str.getBytes("UTF-8");
				res = new String(utf8, "UTF-8");
			} 
			catch (Exception e) 
			{
			}
			
			return str;
		}
		
		/**
		 * Transform caracters behind spaces into uppercase ones
		 *@return The encoded string
		 *@param myS The string to encode
		 */
		public static String phrase(String myS)
		{
			String ret = new String();
			String tmp1, tmp2;
			myS=BString.replace(myS,"."," ");
			myS=BString.replace(myS,"_"," ");
			ret=myS.substring(0,1).toUpperCase();
			for(int i=1;i<myS.length();i++)
			{
				tmp1= new String(myS.substring(i-1,i));
				tmp2= new String(myS.substring(i,i+1));
				if(tmp1.equals(" ")&&!tmp2.equals(" "))
				{
					ret=ret+myS.substring(i,i+1).toUpperCase();
				}
				else
				{
					ret=ret+myS.substring(i,i+1);
				}
			}
			return ret;
		}
		
		/**
		 * Encodes an entrire STRING-Vector to HTML-encoded STRING-Vector
		 *@return The encoded vector
		 *@param vec The vector to encode
		 */
		public static Vector enocodeVectorToHtml(Vector vec)
		{
			Vector myvec=new Vector();
			for (int i = 0;i<vec.size();i++)
			{
				myvec.add(encodeToHtml((String) vec.get(i)));
			}
			return myvec;
		}
		
		/**
		 * Cuts blanks at the end and at the beginning of the string. [trim]
		 *@return The outcuted string
		 *@param str The string to cut out
		 */
		public static String cutOut(String str)
		{
			return str.trim();
		}
		
		/**
		 * Checks wheater a string contains any non-blank caracters
		 *@return The result
		 *@param str The string to check
		 */
		public static boolean containsSomething(String str)
		{
			boolean result = false;
			Character chr;
			
			for (int i=0;i<str.length();i++)
			{
				if (Character.isWhitespace(str.charAt(i))==false)
				{
					result = true;
				}
			}
			
			return result;
		}
		
		/**
		 * Replaces all substrings with another substring
		 *@return The replaced string
		 *@param str The original string
		 *@param substr The substring to be replaced
		 *@param with The substring to put in
		 */
		public static String replace(String str,String substr, String with)
		{
			String outi = new String("");
			int width = str.length();
			int count = 0;
			do
			{
				if (str.indexOf(substr,count)!=-1)
				{
					outi=outi+str.substring(count,str.indexOf(substr,count))+with;
					count=str.indexOf(substr,count)+substr.length();
				}
				else
				{
					outi=outi+str.substring(count,str.length());
					count=str.length();
				}
			}
			while (count<width);
			return outi;
		}
		
		/**
		 * Replaces all substrings with another substring
		 *@return The replaced string
		 *@param str The original string
		 *@param substr The substring to be replaced
		 *@param with The substring to put in
		 */
		public static String replaceInsensitive(String str,String substr, String with)
		{
			String outi = new String("");
			int width = str.length();
			int count = 0;
			do
			{
				if (str.toLowerCase().indexOf(substr.toLowerCase(),count)!=-1)
				{
					outi=outi+str.substring(count,str.toLowerCase().indexOf(substr.toLowerCase(),count))+with;
					count=str.toLowerCase().indexOf(substr.toLowerCase(),count)+substr.length();
				}
				else
				{
					outi=outi+str.substring(count,str.length());
					count=str.length();
				}
			}
			while (count<width);
			return outi;
		}

		public static boolean croissantStrict(String s)
		{
			boolean ret = true;
			for(int i=0;i<s.length()-1;i++)
			{
				if(s.charAt(i)>s.charAt(i+1))
				{
					ret = false;
				}
			}
			if(s.charAt(0)>=s.charAt(s.length()-1))
			{
				ret = false;
			}
			if(s.length()==1)
			{
				ret=true;
			}
			return ret;
		}
		
		public static boolean isPrefixOf(String pre, String str)
		{
			boolean ret = false;
			if(pre.length()>str.length())
			{
				ret=true;
			}
			else
			{
				ret = str.substring(0,pre.length()).equals(pre);
			}
			return ret;
		}
		
		public static StringList explode(String _source, String _by)
		{
			StringList sl = new StringList();
			
			while(!_source.equals(""))
			{
				if (_source.indexOf(_by)>=0)
				{
					sl.add(_source.substring(0,_source.indexOf(_by)-1));
					_source=_source.substring(_source.indexOf(_by)+_by.length(), _source.length());
				}
				else
				{
					sl.add(_source);
					_source="";
				}
			}
			return sl;
		}
		
		public static StringList explodeWithDelimiter(String _source, String _by)
		{
			StringList sl = new StringList();
			
			while(!_source.equals(""))
			{
				if (_source.indexOf(_by)>=0)
				{
					sl.add(_source.substring(0,_source.indexOf(_by)));
					sl.add(_by);
					_source=_source.substring(_source.indexOf(_by)+_by.length(), _source.length());
				}
				else
				{
					sl.add(_source);
					_source="";
				}
			}
			return sl;
		}
		
		public static String breakup(String _replace)
		{
			_replace=_replace.toLowerCase();
			String upper = _replace.toUpperCase();
			String result = new String();
			
			for(int i=0;i<_replace.length();i++)
			{
				result+="["+_replace.charAt(i)+upper.charAt(i)+"]";
			}
			
			return result;
		}
		
	}

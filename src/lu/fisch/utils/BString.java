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
 *      Description:    Simple utility class to work with strings.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2003.05.10      First Issue
 *		Bob Fisch		2007.12.09		Moved to another package and adapted for Structorizer
 *		Kay Gürtzig		2015.10.31		Performance improvements
 *      Kay Gürtzig     2017.03.13      New method pair encodeToXML and decodeFromXML added for enh. #372,
 *                                      Some code revisions where it ached too much looking at.
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
		 * Encodes some characters to HTML-encoded symbols
		 * @return The encoded string
		 * @param str The string to encode
		 * @see #enocodeVectorToHtml(Vector)
		 * @see #encodeToXML(String)
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
		
		// START KGU#363 2017-03-13: Enh. #372 - workaround for XML coding of potentially very long texts
		/**
		 * Returns a string from text where all characters with special meaning in XML and all non-
		 * ASCII characters are converted into XML escapes. 
		 * @param text - the source string 
		 * @return the XML-escaped string
		 * @see #decodeFromXML(String)
		 * @see #encodeToHtml(String)
		 */
		public static String encodeToXML(String text) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < text.length(); i++){
				char ch = text.charAt(i);
				switch(ch){
				case '<': sb.append("&#60;"); break;
				case '>': sb.append("&#62;"); break;
				case '\"': sb.append("&#34;"); break;
				case '&': sb.append("&#38;"); break;
				case '\'': sb.append("&#39;"); break;
				default:
					if(ch < 0x20 || ch > 0x7e) {
						sb.append("&#"+((int)ch)+";");
					}else {
						sb.append(ch);
					}
				}
			}
			return sb.toString();
		}
		
		/**
		 * Returns a string from {@code text} where all XML-escaped character sequences of kind &amp;#&lt;int&gt;;
		 * are converted back to the original characters with that code.
		 * This method is less likely to be needed than {@link #encodeToXML(String)} (because usually an XML
		 * framework will be usd to parse XML files. But well, for symmetry reasons, it's provided here.)
		 * @see #encodeToXML(String)
		 * @param text - the XML-escaped string 
		 * @return the decoded original string
		 */
		public static String decodeFromXML(String text) {
			int state = 0;
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < text.length(); i++){
				int code = 0;
				char ch = text.charAt(i);
				switch (state) {
				case 0:
					if (ch == '&') {
						state++;
					}
					else {
						sb.append(ch);
					}
					break;
				case 1:
					if (ch == '#') {
						state++;
					}
					else {
						// possibly some specific escape names, but we will ignore them
						state = 0;
						sb.append("&"+ch);
					}
					break;
				case 2:
					if (Character.isDigit(ch)) {
						code = code * 10 + (ch - '0');
					}
					else if (ch == ';') {
						sb.append((char)code);
						code = 0;
						state = 0;
					}
					else {
						sb.append("@#" + code + ch);
						code = 0;
						state = 0;
					}
				}
			}
			return sb.toString();
		}
		// END KGU#363 2017-03-13
		
		/**
		 * Replaces '.' and '_' throughout the string with spaces and then turns all characters
		 * behind spaces into upper-case ones
		 *@return The encoded string
		 *@param myS The string to encode
		 */
		public static String phrase(String myS)
		{
			String ret = new String();
			myS = BString.replace(myS,"."," ");
			myS = BString.replace(myS,"_"," ");
			ret = myS.substring(0,1).toUpperCase();
			for(int i=1; i<myS.length(); i++)
			{
				char tmp1 = myS.charAt(i-1);
				char tmp2 = myS.charAt(i);
				if (tmp1 == ' ' && tmp2 != ' ')
				{
					tmp2 = Character.toUpperCase(tmp2);
				}
				ret += tmp2;
			}
			return ret;
		}
		
		/**
		 * Encodes an entire STRING-Vector to HTML-encoded STRING-Vector
		 * @return The encoded vector
		 * @param vec The vector to encode
		 * @see #encodeToHtml(String)
		 * @see #encodeToXML(String)
		 */
		public static Vector<String> enocodeVectorToHtml(Vector<String> vec)
		{
			Vector<String> myvec=new Vector<String>();
			for (int i = 0; i<vec.size() ;i++)
			{
				myvec.add(encodeToHtml(vec.get(i)));
			}
			return myvec;
		}
		
		/**
		 * Cuts blanks at the end and at the beginning of the string. [trim]
		 *@return The trimmed string
		 *@param str The string to cut out
		 */
		public static String cutOut(String str)
		{
			return str.trim();
		}
		
		/**
		 * Checks whether a string contains any non-blank characters
		 *@return true iff there is at least one non-blank character
		 *@param str The string to check
		 */
		public static boolean containsSomething(String str)
		{
			boolean result = false;
			
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
		public static String replaceInsensitive(String str, String substr, String with)
		{
			String outi = new String("");
			String strLower = str.toLowerCase();
			String substrLower = substr.toLowerCase();
			int width = str.length();
			int count = 0;
			do
			{
				int index = strLower.indexOf(substrLower,count); 
				if (index != -1)
				{
					outi += str.substring(count,index) + with;
					count = index + substr.length();
				}
				else
				{
					outi += str.substring(count,str.length());
					count = str.length();
				}
			} while (count<width);
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
//			boolean ret = false;
//			if(pre.length()>str.length())
//			{
//				ret=true;	// This is wrong!
//			}
//			else
//			{
//				ret = str.substring(0,pre.length()).equals(pre);
//			}
//			return ret;
			return str.startsWith(pre);
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

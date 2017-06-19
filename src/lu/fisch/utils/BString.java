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
 *      Kay Gürtzig     2017.06.18      Method breakup refined to cope with metasymbols in the string to
 *                                      be broken up for regex matching. Code revision, seeral rdundant methods
 *                                      declared deprecated
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
			
			// FIXME (KGU): The following code is irrelevant for the result! Should we return res (if not null)?
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
		 * Cuts blanks at the end and at the beginning of the string. [trim]<br/>
		 * NOTE: You should better use {@code str.trim()} instead.  
		 *@param str - The string to be trimmed
		 *@return The trimmed string
		 */
		@Deprecated
		public static String cutOut(String str)
		{
			return str.trim();
		}
		
		/**
		 * Checks whether a string contains any non-blank characters<br/>
		 * NOTE: You may use {@code !str.trim().isEmpty()} instead
		 *@param str The string to check
		 *@return true iff there is at least one non-blank character
		 */
		@Deprecated
		public static boolean containsSomething(String str)
		{
			boolean result = false;
			
			for (int i = 0; i < str.length(); i++)
			{
				if (!Character.isWhitespace(str.charAt(i)))
				{
					result = true;
					break;
				}
			}
			
			return result;
		}
		
		/**
		 * Replaces all substrings with another substring <br/>
		 * NOTE: You should better use {@code str.replace(substr, with)} instead.
		 *@param str The original string
		 *@param substr The substring to be replaced
		 *@param with The substring to put in
		 *@return The replaced string
		 */
		@Deprecated
		public static String replace(String str, String substr, String with)
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
			} while (count<width);
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

		/**
		 * Checks that the character codes of string s are strictly monotonous,
		 * i.e. i &le; j --&gt; s[i] &le; s[j], but not all equal (if there are
		 * at least two characters).
		 * @param s - the string to be analysed
		 * @return true iff the monotony described above holds
		 */
		public static boolean croissantStrict(String s)
		{
			boolean ret = true;
			for(int i = 0; i < s.length()-1; i++)
			{
				if (s.charAt(i) > s.charAt(i+1))
				{
					ret = false;
					break;
				}
			}
			if (s.charAt(0) >= s.charAt(s.length()-1))
			{
				ret = false;
			}
			if(s.length()==1)
			{
				ret=true;
			}
			return ret;
		}
		
		/**
		 * Use {@code str.startsWith(pre)} instead.
		 * @param pre - the prefix to be confirmed
		 * @param str - the analysed string
		 * @return true iff {@code str} starts with prefix {@code pre}
		 */
		@Deprecated
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
		
		/**
		 * Splits the string {@code _source} around occurrences of delimiter string {@code _by}
		 * and returns a StringList consisting of the split parts (without the separating
		 * delimiters) in order of occurrence.<br/>
		 * NOTE: Use method {@code explode(String, String)} on {@link #StringList} instead.
		 * @param _source - the string to be split
		 * @param _by - the separating string
		 * @return the split result
		 */
		@Deprecated
		public static StringList explode(String _source, String _by)
		{
			// START KGU 2017-06-18: Delegated to StringList.explode() where it belongs
//			StringList sl = new StringList();
//			
//			while(!_source.equals(""))
//			{
//				if (_source.indexOf(_by)>=0)
//				{
//					sl.add(_source.substring(0,_source.indexOf(_by)-1));
//					_source=_source.substring(_source.indexOf(_by)+_by.length(), _source.length());
//				}
//				else
//				{
//					sl.add(_source);
//					_source="";
//				}
//			}
//			return sl;
			return StringList.explode(_source, _by);
			// END KGU 2017-06-18
		}

		/**
		 * Splits the string {@code _source} around occurrences of delimiter string {@code _by}
		 * and returns a StringList consisting of the split parts and the separating
		 * delimiters in order of occurrence.<br/>
		 * NOTE: Use method {@code explodeWithDElimiter(String, String)} on {@link #StringList} instead.
		 * @param _source - the string to be split
		 * @param _by - the separating string
		 * @return the split result
		 */
		@Deprecated
		public static StringList explodeWithDelimiter(String _source, String _by)
		{
			// START KGU 2017-06-18: Delegated to StringList.explode() where it belongs
//			StringList sl = new StringList();
//			int lenBy = _by.length();
//			while(!_source.equals(""))
//			{
//				int pos = _source.indexOf(_by); 
//				if (pos >= 0)
//				{
//					sl.add(_source.substring(0, pos));
//					sl.add(_by);
//					_source = _source.substring(pos + lenBy, _source.length());
//				}
//				else
//				{
//					sl.add(_source);
//					_source = "";
//				}
//			}
//			return sl;
			return StringList.explodeWithDelimiter(_source, _by);
			// END KGU 2017-06-18
		}
		
		/**
		 * Produces a regular expression allowing to match the given string in a case-insensitive way.
		 * All letters 'x' are replaced by "[xX]", meta symbols like '[', ']', '^', '$' are escaped or
		 * quoted, others (like '(', ')') just enclosed in brackets. 
		 * @param _replace - the string (not supposed to be regular expression)!
		 * @return a regular expression string
		 */
		public static String breakup(String _replace)
		{
			// START KGU#324 2017-06-18: Bugfix on occ. #415
			//_replace = _replace.toLowerCase();
			String lower = _replace.toLowerCase();
			// END KGU#324 2017-06-18
			String upper = _replace.toUpperCase();
			String result = new String();
			
			for(int i=0; i < _replace.length(); i++)
			{
				// START KGU#324 2017-06-18: We must escape characters like brackets and shouldn't duplicate characters
				//result+="["+_replace.charAt(i)+upper.charAt(i)+"]";
				char ch1 = lower.charAt(i);
				char ch2 = upper.charAt(i);
				if (ch1 != ch2) {
					result += "[" + ch1 + ch2 + "]";
				}
				else if ("[]^$".contains(ch1+"")) {
					result += "\\" + ch1;
					// The character following to a backslash is to be adopted as is
					if (ch1 == '\\' && i < _replace.length() - 1) {
						result += _replace.charAt(++i);
					}
				}
				else if (ch1 == '-') {
					result += ch1;
				}
				else {
					result += "[" + ch1 + "]";
				}
				// END KGU#324 2017-06-18
			}
			
			return result;
		}
		
		
	}

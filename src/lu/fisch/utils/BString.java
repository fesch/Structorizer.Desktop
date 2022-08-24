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
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2003-05-10      First Issue
 *      Bob Fisch       2007-12-09      Moved to another package and adapted for Structorizer
 *      Kay Gürtzig     2015-10-31      Performance improvements
 *      Kay Gürtzig     2017-03-13      New method pair encodeToXML and decodeFromXML added for enh. #372,
 *                                      Some code revisions where it ached too much looking at.
 *      Kay Gürtzig     2017-06-18      Method breakup refined to cope with meta symbols in the string to
 *                                      be broken up for regex matching. Code revision, several redundant
 *                                      methods declared as deprecated
 *      Kay Gürtzig     2017-11-03      Bugfix #448: Method breakup(String) revised again
 *      Kay Gürtzig     2018-09-12      Method name typo refactored: enocodeVectorToHtml() --> encodeVectorToHtml()
 *      Kay Gürtzig     2019-11-22      Dead code in encodeToHtml() disabled, bugfix in explode()
 *      Kay Gürtzig     2020-04-21      Bugfix #852: method breakup completely rewritten, signature changed
 *      Kay Gürtzig     2022-08-18      replaceInsensitive() rewritten, croissantStrict() hardened against empty strings
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
		 * @see #encodeVectorToHtml(Vector)
		 * @see #encodeToXML(String)
		 */
		public static String encodeToHtml(String str)
		{
			str = str.replace("&","&amp;");
//			str=BString.replace(str,"<","&lt;");
			str = str.replace("<","&#60;");
//			str=BString.replace(str,">","&gt;");
			str = str.replace(">","&#62;");
//			str=BString.replace(str,"\"","&quot;");
			str = str.replace("\"","&#34;");
			//str=BString.replace(str," ","&nbsp;");
			
			// START KGU 2019-11-22: The following code was irrelevant for the result! Should we have returned res (if not null)?
			//String res = null;
			//try {
			//	byte[] utf8 = str.getBytes("UTF-8");
			//	res = new String(utf8, "UTF-8");
			//} 
			//catch (Exception e) 
			//{
			//}
			// END KGU 2019-11-22
			
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
			myS = myS.replace("."," ");
			myS = myS.replace("_"," ");
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
		public static Vector<String> encodeVectorToHtml(Vector<String> vec)
		{
			Vector<String> myvec=new Vector<String>();
			for (int i = 0; i<vec.size() ;i++)
			{
				myvec.add(encodeToHtml(vec.get(i)));
			}
			return myvec;
		}
		
		/**
		 * Cuts blanks at the end and at the beginning of the string.
		 *@param str - The string to be trimmed
		 *@return The trimmed string
		 *@deprecated Use {@link String#trim()} instead.
		 */
		public static String cutOut(String str)
		{
			return str.trim();
		}
		
		/**
		 * Checks whether a string contains any non-blank characters.
		 *@param str - The string to check
		 *@return true iff there is at least one non-blank character
		 *@deprecated Use {@code !str.trim().isEmpty()} instead
		 */
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
		 * Replaces all substrings with another substring
		 *@param str The original string
		 *@param substr The substring to be replaced
		 *@param with The substring to put in
		 *@return The replaced string
		 *@deprecated Use {@link String#replace(CharSequence, CharSequence)} instead.
		 */
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
		 * Case-insensitively replaces all substrings {@code substr} in string {@code str}
		 * with another substring {@code with}.
		 * 
		 *@param str - The original string
		 *@param substr - The substring case-insensitively to be replaced
		 *@param with - The substring to put in
		 *@return The resulting string of the substitutions
		 */
		public static String replaceInsensitive(String str, String substr, String with)
		{
			StringBuilder sb = new StringBuilder();
			String strLower = str.toLowerCase();
			String substrLower = substr.toLowerCase();
			int substrLen = substr.length();
			int from = 0;
			int index = -1;
			while ((index = strLower.indexOf(substrLower, from)) >= 0)
			{
				sb.append(str.substring(from, index));
				sb.append(with);
				from = index + substrLen;
			}
			sb.append(str.substring(from, str.length()));
			return sb.toString();
		}

		/**
		 * Checks that the character codes of string {@code s} are strictly monotonous,
		 * i.e. i &le; j --&gt; s[i] &le; s[j], but not all equal (if there are
		 * at least two characters).
		 * @param s - the string to be analysed
		 * @return {@code true} iff the monotony described above holds
		 */
		public static boolean croissantStrict(String s)
		{
			if (s.length() <= 1) {
				return true;
			}
			for(int i = 0; i < s.length()-1; i++)
			{
				if (s.charAt(i) > s.charAt(i+1))
				{
					return false;
				}
			}
			if (s.charAt(0) >= s.charAt(s.length()-1))
			{
				return false;
			}
			return true;
		}
		
		/**
		 * Checks whether {@code str} starts with {@code pre}.
		 * @param pre - the prefix to be confirmed
		 * @param str - the analysed string
		 * @return true iff {@code str} starts with prefix {@code pre}
		 * @see String#startsWith(String)
		 * @deprecated Use {@code str.startsWith(pre)} instead.
		 */
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
		 * and returns a {@link StringList} consisting of the split parts (without the separating
		 * delimiters) in order of occurrence.<br/>
		 * @param _source - the string to be split
		 * @param _by - the separating string (not interpreted as regular expression!)
		 * @return the split result
		 * @see StringList#explode(String, String)
		 * @see String#explodeWithDelimiter(String, String)
		 */
		public static StringList explode(String _source, String _by)
		{
			StringList sl = new StringList();
			
			while (!_source.isEmpty())
			{
				int posBy = _source.indexOf(_by);
				if (posBy >= 0)
				{
					// START KGU 2019-11-22 Wrong cut position
					//sl.add(_source.substring(0,_source.indexOf(_by)-1));
					sl.add(_source.substring(0, posBy));
					// END KGU 2019-11-22
					_source=_source.substring(posBy + _by.length());
				}
				else
				{
					sl.add(_source);
					_source = "";
				}
			}
			return sl;
		}

		/**
		 * Splits the string {@code _source} around occurrences of delimiter string {@code _by}
		 * and returns a StringList consisting of the split parts and the separating
		 * delimiters in order of occurrence.
		 * @param _source - the string to be split
		 * @param _by - the separating string
		 * @return the split result
		 * @deprecated Use method {@link StringList#explodeWithDelimiter(String, String)} instead.
		 */
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
		 * quoted, others (like '(', ')') just enclosed in brackets.<br/>
		 * NOTE:<br/>
		 * Decide whether the resulting pattern may contain capturing groups (i.e. parentheses as meta
		 * symbols). With {@code _noGroups = true} the result is guaranteed not to contain capturing
		 * groups (this may be important if the pattern is to be used as part of a regex replacement
		 * with several groups following, as it will compromise the group counting). The disadvantage
		 * is that possible matches may be missed for letters the lowercase or uppercase representation
		 * of which would require more than one character as is the case with 'ß' (uppercase --> "SS").
		 * @param _searched - the string (not supposed to be regular expression)!
		 * @param _noGroups - if true then capturing groups are to be avoided by all means (see above).
		 * @return a regular expression pattern as string
		 */
// START KGU#850 2020-04-21: Bugfix #852: lower and upper may have different length (e.g. 'ß' -> "SS")
//		public static String breakup(String _replace, boolean _noGroups)
//		{
//			// START KGU#324 2017-06-18: Bugfix on occ. #415
//			//_replace = _replace.toLowerCase();
//			String lower = _replace.toLowerCase();
//			// END KGU#324 2017-06-18
//			String upper = _replace.toUpperCase();
//			String result = new String();
//			
//			for(int i=0; i < _replace.length(); i++)
//			{
//				// START KGU#324 2017-06-18: We must escape characters like brackets and shouldn't duplicate characters
//				//result+="["+_replace.charAt(i)+upper.charAt(i)+"]";
//				char ch1 = lower.charAt(i);
//				char ch2 = upper.charAt(i);
//				if (ch1 != ch2) {
//					result += "[" + ch1 + ch2 + "]";
//				}
//				// START KGU#454 2017-11-03: Bugfix #448 regex syntax errors occurred e.g. with breakup("\\\\n+")
//				//else if ("[]^$".contains(ch1+"")) {
//				else if ("\\[]^$".contains(ch1+"")) {
//				// END KGU#4545 2017-11-03
//					result += "\\" + ch1;
//					// The character following to a backslash is to be adopted as is
//					// START KGU#454 2017-11-03: Bugfix #448 ... unless it is a backslash itself!
//					//if (ch1 == '\\' && i < _replace.length() - 1) {
//					if (ch1 == '\\' && i < _replace.length() - 1 && _replace.charAt(i+1) != '\\') {
//					// END KGU#4545 2017-11-03
//						result += _replace.charAt(++i);
//					}
//				}
//				else if (ch1 == '-') {
//					result += ch1;
//				}
//				else {
//					result += "[" + ch1 + "]";
//				}
//				// END KGU#324 2017-06-18
//			}
//			
//			return result;
		public static String breakup(String _searched, boolean _noGroups)
		{
			StringBuilder result = new StringBuilder();
			
			for (int i = 0; i < _searched.length(); i++) {
				char ch = _searched.charAt(i);
				String stCh = Character.toString(ch);
				if ("\\[]^$".contains(stCh)) {
					result.append('\\');
					result.append(ch);
					/* The character following to a backslash is to be adopted as is ...
					 * ... unless it is a backslash itself! */
					if (ch == '\\' && i < _searched.length() - 1 && _searched.charAt(i+1) != '\\') {
						result.append(_searched.charAt(++i));
					}
				}
				else if (Character.isLetter(ch)) {
					String loCh = stCh.toLowerCase();
					String upCh = stCh.toUpperCase();
					if (loCh.length() != upCh.length()) {
						if (_noGroups) {
							// We will just use the original character as we must not group
							result.append('[');
							result.append(ch);
							result.append(']');
						}
						else {
							// We form a group if we are allowed to
							result.append('(');
							result.append(loCh);
							result.append('|');
							result.append(upCh);
							result.append(')');
						}
					}
					else {
						result.append('[');
						result.append(loCh);
						result.append(upCh);
						result.append(']');
					}
				}
				else if (ch == '-') {
					result.append(ch);
				}
				else {
					result.append('[');
					result.append(ch);
					result.append(']');
				}
			}
			return result.toString();
// END KGU#850 2020-04-21
		}
		
	}

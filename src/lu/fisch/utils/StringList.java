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
 *      Description:    A dynamic list of strings.
 *						Copies the behaviour of a "TStringList" in Delphi.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2015.11.04      Methods indexOf added.
 *      Kay Gürtzig     2015.11.24      Method clear added.
 *      Kay Gürtzig     2015.12.01      Methods replaceAll, replaceAllCi added.
 *      Kay Gürtzig     2015.12.01      Methods concatenate(...) added; getText() etc. reduced to them.
 *      Kay Gürtzig     2016.01.08      Method replaceAllBetween() added, replaceAll etc. reduced to it.
 *      Kay Gürtzig     2016.03.26      Method subSequence() added.
 *      Kay Gürtzig     2016.04.03      Method int removeAll(StringList, int, boolean) added
 *      Bob Fisch       2016.08.01      added method "toArray()" and "remove(int)" (which is a synonym to delete(int))
 *      Kay Gürtzig     2017.01.31      Method remove(int,int) added. 
 *      Kay Gürtzig     2017.03.31      Methods addOrderedIfNew and addByLengthIfNew revised (now with return value)
 *      Kay Gürtzig     2017.06.18      Methods explodeWithDelimiter() revised (don't mistake '_by' for a regex anymore)
 *      Kay Gürtzig     2017.10.02      New functional variant with null separator for methods concatenate(...)
 *      Kay Gürtzig     2017.10.28      Method trim() added.
 *      Kay Gürtzig     2019-02-15      Method isEmpty() added
 *      Kay Gürtzig     2019-03-03      Bugfix in method explodeFirstOnly(String, String)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************/


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Vector;

public class StringList {

	private Vector<String> strings = new Vector<String>();

	/**
	 * Constructs this as empty StringList
	 * @param _strings
	 * @see #StringList(String[])
	 * @see #getNew(String)
	 * @see #explode(String, String)
	 */
	public StringList()
	{}
	
	// START KGU 2017-06-18: New constructor as pendant to toArray()
	/**
	 * Constructs this from the given String array
	 * @param _strings
	 * @see #StringList()
	 * @see #getNew(String)
	 * @see #explode(String, String)
	 */
	public StringList(String[] _strings)
	{
		for (String str: _strings) {
			strings.add(str);
		}
	}
	// END KGU 2017-06-18
	
	/**
	 * Returns a new StringList containing just the given {@code _string} 
	 * @param _string - the single element
	 * @return the created StringList
	 * @see #StringList(String[])
	 * @see #explode(String, String)
	 */
	public static StringList getNew(String _string)
	{
		StringList sl = new StringList();
		sl.add(_string);
		return sl;
	}
	
	/**
	 * Splits string {@code _source} around matches of the given REGULAR EXPRESSION(!) {@code _by}.
	 * Trailing empty strings are not included in the resulting StringList.
	 * The string "boo:and:foo", for example, yields the following results with
	 * these expressions:<br/>
	 * <table>
	 * <tbody>
	 * <tr><td>Regex</td><td>Result</td></tr>
	 * <tr><td>":"</td><td>{ "boo", "and", "foo" }</td></tr>
	 * <tr><td>"o"</td><td>{ "b", "", ":and:f" }</td></tr>
	 * </tbody>
	 * </table>
	 * @see #explodeFirstOnly(String, String)
	 * @see #explode(StringList, String)
	 * @see #explodeWithDelimiter(String, String)
	 * @param _source - the string to be split
	 * @param _by - the splitting regular expression
	 * @return the StringLits containing all splitting shards
	 */
	public static StringList explode(String _source, String _by)
	{
		String[] multi = _source.split(_by);
		StringList sl = new StringList();

		for(int i=0;i<multi.length;i++)
		{
			sl.add(multi[i]);
		}

		return sl;
	}

	/**
	 * Splits string {@code _source} around the first match of the given REGULAR EXPRESSION(!) {@code _by}.
	 * A trailing empty string is not included in the resulting StringList.
	 * The string "boo:and:foo", for example, yields the following results with
	 * these expressions:<br/>
	 * <table>
	 * <tbody>
	 * <tr><td>Regex</td><td>Result</td></tr>
	 * <tr><td>":"</td><td>{ "boo", "and:foo" }</td></tr>
	 * <tr><td>"o"</td><td>{ "b", "o:and:f" }</td></tr>
	 * </tbody>
	 * </table>
	 * @see #explode(String, String)
	 * @see #explode(StringList, String)
	 * @see #explodeWithDelimiter(String, String)
	 * @param _source - the string to be split
	 * @param _by - the splitting regular expression
	 * @return the StringLits containing all splitting shards
	 */
	public static StringList explodeFirstOnly(String _source, String _by)
	{
		// START KGU 2019-03-03: Bugfix explodeFirstOnly("test=, pas =", "=") returned {"test", ", pas "} instead of {"test", ", pas ="}
		//String[] multi = _source.split(_by);
		//StringList sl = new StringList();
		//
		//String first = multi[0];
		//sl.add(first);
		//
		//if(multi.length>1)
		//{
		//	String second = multi[1];
		//	for(int i=2;i<multi.length;i++)
		//	{
		//		second+="="+multi[i];
		//	}
		//	sl.add(second);
		//}
		StringList sl = new StringList(_source.split(_by, 2));
		// END KGU 2019-03-03
		return sl;
	}

	/**
	 * Splits all strings contained in {@code _source} around matches of the given REGULAR EXPRESSION(!) {@code _by}
	 * and returns a single StringList containing all split results of all element strngs in sequential order.
	 * Trailing empty strings are not included in the resulting StringList.
	 * @see #explode(String, String)
	 * @see #explodeFirstOnly(String, String)
	 * @see #explodeWithDelimiter(StringList, String)
	 * @param _source - the StringList further to be split
	 * @param _by - the separator (delimiter) pattern (regex!)
	 * @return The split results as StringList
	 */
	public static StringList explode(StringList _source, String _by)
	{
		StringList sl = new StringList();

		for(int s=0; s<_source.count(); s++)
		{
			// FIXME KGU 2017-06-18: I suggest that all but the last string be split with second argument -1
			String[] multi = _source.get(s).split(_by);
			for(int i=0; i<multi.length; i++)
			{
				sl.add(multi[i]);
			}
		}

		return sl;
	}

	/**
	 * Splits the string {@code _source} around occurrences of delimiter string {@code _by}
	 * and returns a new StringList consisting of the split parts and the separating
	 * delimiters in order of occurrence.<br/>
	 * Note that the resulting StringList may be empty!
	 * @param _source - the string to be split
	 * @param _by - the separating string (plain string, no regex!)
	 * @return the split result
	 */
	public static StringList explodeWithDelimiter(String _source, String _by)
	{
		// START KGU 2017-06-18: Bugfix - this (unused) version was defective ("ate" delimiters)
//		//String[] multi = _source.split(_by);
//		String[] multi = _source.split(Pattern.quote(_by), -1);	// We must not suppress empty parts!
//		// END KGU 2017-06-18
//		StringList sl = new StringList();
//
//		for(int i=0; i < multi.length; i++)
//		{
//			if (i != 0)
//			{
//				sl.add(_by);
//			}
//			sl.add(multi[i]);
//		}
//
//		return sl;
		// TODO: performance should be measured and compared between these two solutions!
		// The following is the (optimized) alternative solution copied from BString  
		StringList sl = new StringList();
		int lenBy = _by.length();
		while (!_source.isEmpty())
		{
			int pos = _source.indexOf(_by);
			if (pos >= 0)
			{
				sl.add(_source.substring(0,pos));
				sl.add(_by);
				_source=_source.substring(pos + lenBy, _source.length());
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
	 * Splits the elements of StringList {@code _source} around occurrences of delimiter string {@code _by}
	 * and returns a new StringList consisting of all the split parts and the separating
	 * delimiters in order of occurrence.<br/>
	 * @param _source - the string to be split
	 * @param _by - the separating string (plain string, no regex!)
	 * @return the split result
	 */
	public static StringList explodeWithDelimiter(StringList _source, String _by)
	{
		StringList sl = new StringList();

		for(int s=0;s<_source.count();s++)
		{
			// START KGU 2017-06-18: We should rely on our own method
			//StringList multi = BString.explodeWithDelimiter(_source.get(s),_by);
			StringList multi = explodeWithDelimiter(_source.get(s),_by);
			// END KGU 2017-06-18
			sl.add(multi);
		}

		return sl;
	}
	
	/**
	 * Creates a copy of this StringList via a representation in CSV format.
	 * @return an equivalent StringList
	 */
	public StringList copy()
	{
		// FIXME (KGU) Why this complicated detour?
		StringList sl = new StringList();
		//sl.add("TEXT");
		sl.setCommaText(this.getCommaText()+"");
		return sl;
	}
	
	// START KGU 2016-03-26
	/**
	 * Returns a StringList consisting of the elements with position {@code _start} through (but not including) {@code _end} of this.
	 * If {@code _start} is less than 0 then the result starts at element 0, if {@code _end} is greater than {@link #count()} then
	 * the result simply contains copies of all remaining elements.
	 * @param _start - position (index) of the first element to be copied.
	 * @param _end - position (index) beyond the last element to be copied.
	 * @return The partial copy of this.
	 */
	public StringList subSequence(int _start, int _end)
	{
		StringList sl = new StringList();
		if (_start < 0) _start = 0;
		if (_end > this.count()) _end = this.count();
		for (int i = _start; i < _end; i++)
		{
			sl.add(this.get(i) + "");
		}
		return sl;
	}

	/**
	 * Appends the given String {@code _string} at end.
	 * @param _string - a string
	 */
	public void add(String _string)
	{
		strings.add(_string);
	}

	/**
	 * Inserts the given String {@code _string} at the appropriate place assuming that this is
	 * a sorted StringList. Sorting criterion for the strings is lexicographic order according
	 * to {@code String.compareTo(String)}.
	 * Multiple String values may occur i.e. if you add a string that has already been member then
	 * another copy of the string will be inserted.
	 * @param _string
	 * @see #add(String)
	 * @see #addOrderedIfNew(String)
	 * @see #addIfNew(String)
	 * @see #addByLength(String)
	 */
	public void addOrdered(String _string)
	{
		addOrdered(_string, false);
	}
	
	private boolean addOrdered(String _string, boolean _onlyIfNew)
	{
		for (int i=0; i < strings.size(); i++)
		{
			int comp = (strings.get(i)).compareTo(_string);
			if (comp == 0 && _onlyIfNew) {
				return false;
			}
			else if (comp >= 0) {
				strings.insertElementAt(_string, i);
				return true;
			}
		}

		add(_string);
		return true;
	}

	/**
	 * Inserts _string such that the elements be ordered by decreasing length
	 * (longest ones first!). If _string is empty then it won't be added at all.
	 * Elements of same length occur in order of insertion.<br/>
	 * (Only works if the already contained elements represent the order described
	 * above.)
	 * Multiple String values may occur i.e. if you add a string that has already been member then
	 * another copy of the string will be inserted.
	 * @param _string the string to be inserted
	 * @see #add(String)
	 * @see #addIfNew(String)
	 * @see #addOrdered(String)
	 * @see #addOrderedIfNew(String)
	 * @see #addByLengthIfNew(String)
	 */
	public void addByLength(String _string)
	{
		boolean inserted = false;
		if (!_string.equals(""))
		{
			for(int i=0;i<strings.size();i++)
			{
				// FIXME: Shouldn't strings of the same length be ordered lexicographically?
				if ((strings.get(i)).length()<_string.length())
				{
					strings.insertElementAt(_string,i);
					inserted = true;
					break;
				}
			}

			if (inserted==false)
			{
				add(_string);
			}
		}
	}

	/**
	 * Inserts the string _string if it had not been
	 * contained in this StringList. 
	 * @param _string - The string to be added
	 * @return true if the string was new
	 * @see #add(String)
	 * @see #addOrdered(String)
	 * @see #addOrderedIfNew(String)
	 * @see #addByLength(String)
	 * @see #addByLengthIfNew(String)
	 */
	public boolean addIfNew(String _string)
	{
		if (!strings.contains(_string))
		{
			add(_string);
			return true;
		}
		return false;
	}

	/**
	 * Inserts the string _string (in lexicographic order) if it had not been
	 * contained in this StringList. 
	 * @param _string - The string to be added
	 * @return true if the string was new
	 * @see #add(String)
	 * @see #addIfNew(String)
	 * @see #addOrdered(String)
	 * @see #addByLength(String)
	 * @see #addByLengthIfNew(String)
	 */
	public boolean addOrderedIfNew(String _string)
	{
		return addOrdered(_string, true);
	}

	/**
	 * Inserts _string such that the elements be ordered by decreasing length
	 * (longest ones first!). If _string is empty then it won't be added at all.
	 * Elements of same length occur in order of insertion.
	 * (Only works if the already contained elements represent the order described
	 * above.
	 * @param _string the string to be inserted
	 * @see #add(String)
	 * @see #addIfNew(String)
	 * @see #addOrdered(String)
	 * @see #addOrderedIfNew(String)
	 * @see #addByLength(String)
	 */
	public boolean addByLengthIfNew(String _string)
	{
		boolean found = strings.contains(_string);
		if (!found)
		{
			addByLength(_string);
		}
		return !found;
	}

	/**
	 * Appends a copy of each element of {@code _stringList} to this StringList
	 * no matter whether there might already be an equal string element in this.
	 * @param _string - The string to be added
	 * @see #add(String)
	 * @see #addIfNew(String)
	 * @see #addIfNew(StringList)
	 * @see #addOrdered(String)
	 * @see #addOrderedIfNew(String)
	 * @see #addByLength(String)
	 * @see #addByLengthIfNew(String)
	 */
	public void add(StringList _stringList)
	{
		for (int i=0; i<_stringList.count(); i++)
		{
			strings.add(_stringList.get(i));
		}
	}

	/**
	 * Appends each elements of _stringList that had not been
	 * contained in this StringList.
	 * @param _string - The string to be added
	 * @return true if some of the strings of _stringList was added
	 * @see #add(String)
	 * @see #addIfNew(String)
	 * @see #add(StringList)
	 * @see #addOrdered(String)
	 * @see #addOrderedIfNew(String)
	 * @see #addByLength(String)
	 * @see #addByLengthIfNew(String)
	 */
	public boolean addIfNew(StringList _stringList)
	{
		boolean someInserted = false;
		for (int i=0; i<_stringList.count(); i++)
		{
			if (!strings.contains(_stringList.get(i)))
			{
				strings.add(_stringList.get(i));
				someInserted = true;
			}
		}
		return someInserted;
	}

	// START KGU 2015-11-04: New, more performant and informative searchers
	/**
	 * Returns the last element position of a string element exactly equal to {@code _string} or -1
	 * if there is no such element in this.
	 * @param _string - the search string (plain string, no regex!)
	 * @return element index or -1
	 * @see #indexOf(String)
	 * @see #indexOf(String, boolean)
	 * @see #indexOf(String, int)
	 * @see #indexOf(String, int, boolean)
	 * @see #lastIndexOf(String, boolean)
	 * @see #lastIndexOf(String, int)
	 * @see #lastIndexOf(String, int, boolean)
	 */
	public int lastIndexOf(String _string)
	{
		return this.strings.lastIndexOf(_string);
	}

	/**
	 * Returns the last element position of a string element exactly equal to {@code _string} or -1
	 * if there is no such element in this.
	 * @param _string - the search string (plain string, no regex!)
	 * @param _backwardFrom - the index of the element from which (including!) {@code _string} is looked for backwards.  
	 * @return element index or -1
	 * @see #indexOf(String)
	 * @see #indexOf(String, boolean)
	 * @see #indexOf(String, int)
	 * @see #indexOf(String, int, boolean)
	 * @see #lastIndexOf(String, boolean)
	 * @see #lastIndexOf(String, int, boolean)
	 */
	public int lastIndexOf(String _string, int _backwardFrom)
	{
		return this.strings.lastIndexOf(_string, _backwardFrom);
	}

	public int lastIndexOf(String _string, boolean _matchCase)
	{
		return lastIndexOf(_string, 0, _matchCase);
	}
	
	public int lastIndexOf(String _string, int _backwardFrom, boolean _matchCase)
	{
		if (_matchCase)
			return this.strings.lastIndexOf(_string, _backwardFrom);

		_string = _string.toLowerCase();
		for (int i=_backwardFrom; i > 0; i--)
		{
			if ((strings.get(i)).toLowerCase().equals(_string))
			{
				return i;
			}
		}
		return -1;
	}

	public int indexOf(String _string)
	{
		return this.strings.indexOf(_string);
	}

	public int indexOf(String _string, int _from)
	{
		return this.strings.indexOf(_string, _from);
	}

	public int indexOf(String _string, boolean _matchCase)
	{
		return indexOf(_string, 0, _matchCase);
	}
	
	public int indexOf(String _string, int _from, boolean _matchCase)
	{
		if (_matchCase)
			return this.strings.indexOf(_string, _from);

		_string = _string.toLowerCase();
		for (int i=_from; i<strings.size(); i++)
		{
			if ((strings.get(i)).toLowerCase().equals(_string))
			{
				return i;
			}
		}
		return -1;
	}

	public int indexOf(StringList _subList, int _from, boolean _matchCase)
	{
		int foundAt = -1;
		int foundFirst = -1;
		while (foundAt < 0 && (foundFirst = indexOf(_subList.get(0), _from, _matchCase)) >= 0 && foundFirst + _subList.count() <= this.count())
		{
			for (int i = 1; foundFirst >= 0 && i < _subList.count(); i++)
			{
				String str1 = _subList.get(i);
				String str2 = this.strings.get(foundFirst + i);
				if (!_matchCase) {
					str1 = str1.toLowerCase();
					str2 = str2.toLowerCase();
				}
				if (!(str1.equals(str2)))
				{
					_from = foundFirst + 1;
					foundFirst = -1;
				}
			}
			if (foundFirst >= 0)
			{
				foundAt = foundFirst;
			}
		}
		return foundAt;
	}

	public boolean contains(String _string)
	{
		// START KGU 2015-11-04: Just use the more performant and informative find method 
//		boolean found = false;
//		for(int i=0;i<strings.size();i++)
//		{
//			if(((String) strings.get(i)).equals(_string))
//			{
//				found=true;
//			}
//		}
//		return found;
		return indexOf(_string) != -1;
		// END KGU 2015-11-04
	}

	public boolean contains(String _string, boolean _matchCase)
	{
		// START KGU 2015-11-04: Just use the more performant and informative find method 
//		boolean found = false;
//		for(int i=0;i<strings.size();i++)
//		{
//			if(_matchCase==false)
//			{
//				if(((String) strings.get(i)).toLowerCase().equals(_string.toLowerCase()))
//				{
//					found=true;
//				}
//			}
//			else
//			{
//				if(((String) strings.get(i)).equals(_string))
//				{
//					found=true;
//				}
//			}
//		}
//		return found;
		return indexOf(_string, _matchCase) != -1;
		// END KGU 2015-11-04
	}

	/**
	 * @return a reverse copy of this StringList, does not alter this.
	 */
	public StringList reverse()
	{
		StringList sl = new StringList();

		for (int i=0; i<strings.size(); i++)
		{
			sl.add(get(count()-i-1));
		}

		return sl;
	}

	
	/**
	 * Replaces the element at position {@code _index} with string {@code _s} if {@code 0 <= _index < this.count()},
	 * doesn't do anything otherwise.
	 * @param _index - the index of the element to be replaced
	 * @param _s - the replacing string
	 */
	public void set(int _index, String _s)
	{
		if (_index < strings.size() && _index >= 0)
		{
			strings.remove(_index);
			strings.insertElementAt(_s,_index);
		}
	}

	/**
	 * Returns the String element at position {@code _index}<br/>
	 * NOTE: In case the given index is invalid (i.e. {@code _index < 0}
	 * or {@code _index >= this.count()} neither an exception is raised nor
	 * {@code null} is returned but an empty String!
	 * @param _index - the number of the element
	 * @return the requested string or an empty string (!), see text.
	 */
	public String get(int _index)
	{
		if (_index < strings.size() && _index >= 0)
		{
			return strings.get(_index);
		}
		else
		{
			return "";
		}
	}

	public void delete(int _index)
	{
		strings.removeElementAt(_index);
	}

	public void insert(String _string, int _index)
	{
		strings.insertElementAt(_string,_index);
	}

	public void setText(String _text)
	{
		String[] lines = _text.split ("\n");
		strings.clear();
		for (int i=0; i < lines.length; i++)
		{
			strings.add(lines[i]);
		}
	}

	// START KGU 2015-12-21: More flexibility with reduced redundancy
	/**
	 * Concatenates all elements, putting the _separator string between them.<br/>
	 * NEW: If {@code _separator} is {@code null} then an empty separator is
	 * used unless a preceding string ending with an identifier character
	 * would meet a beginning identifier character of the current string in
	 * which case a single space would be inserted, thus preserving a lexical
	 * gap.
	 * @param _separator - a string placed between the elements of this 
	 * @return the concatenated string
	 */
	public String concatenate(String _separator)
	{
		return concatenate(_separator, 0, this.count());
	}
	
	/**
	 * Concatenates all elements, putting the _separator string between them.<br/>
	 * NEW: If {@code _separator} is {@code null} then an empty separator is
	 * used unless a preceding string ending with an identifier character
	 * would meet a beginning identifier character of the current string in
	 * which case a single space would be inserted, thus preserving a lexical
	 * gap.
	 * @param _separator - a string placed between the elements of this
	 * @param _start - index of the first element to be included
	 * @param _end - index beyond the last element to be included 
	 * @return the concatenated string
	 */
	public String concatenate(String _separator, int _start, int _end)
	{
		// START KGU#425 2017-09-29
		//String text = "";
		StringBuffer text = new StringBuffer();
		boolean lastEndedLikeId = false;
		// END KGU#425 2017-09-29
		boolean isFirst = true;
        for(int i = Math.min(_start, count()); i < Math.min(_end, count()); i++)
		{
        	String thisString = strings.get(i);
			if (isFirst)
			{
				//text = strings.get(i);
				isFirst = false;
				// START KGU#425 2019-10-02
				lastEndedLikeId = !thisString.isEmpty() && Character.isJavaIdentifierPart(thisString.charAt(thisString.length()-1));
				// END KGU#425 2019-10-02
			}
			// START KGU#425 2019-10-02
			//else
			else if (_separator != null)
			// END KGU#425 2019-10-02
			{
				//text += _separator + thisString;
				text.append(_separator);
			}
			// START KGU#425 2019-10-02
			else if (!thisString.isEmpty()) {
				if (lastEndedLikeId && Character.isJavaIdentifierPart(thisString.charAt(0))) {
					text.append(" ");
				}
				lastEndedLikeId = Character.isJavaIdentifierPart(thisString.charAt(thisString.length()-1));
			}
			// END KGU#425 2019-10-02
			text.append(thisString);
		}
		//return text;
        return text.toString();
	}
	
	public String concatenate(String _separator, int _start)
	{
		return concatenate(_separator, _start, this.count());
	}
	
	/**
	 * Concatenates the elements without separating string (exact reconstruction
	 * of a string having produced this StringList by explodeWithDelimiter())
	 * @return a continuous string composed of all elements
	 */
	public String concatenate()
	{
		return concatenate("");
	}

	/**
	 * Multi-line text formed from the list elements as lines (actually the
	 * same as this.concatenate("\n"))
	 * @return multi-line string, each element being copied to a line
	 */
	public String getText()
	{
		return concatenate("\n");
	}

	/**
	 * Concatenates elements with blanks between them.
	 * @return the concatenation of all elements separated by single blanks
	 */
	public String getLongString()
	{
		return concatenate(" ");
	}
	// END KGU 2015-12-21

	/**
	 * @return the number of elements
	 */
	public int count()
	{
		return strings.size();
	}
	
	/**
	 * Tests if this StringList has no components
	 * @return {@code true} if and only if this StringList has no elements.
	 */
	public boolean isEmpty()
	{
		return strings.isEmpty();
	}

	public void setCommaText(String _input)
	{
		String input = _input+"";

		// if not CSV, make it CSV
		if (input.length()>0)
		{
			String first = Character.toString(input.charAt(0));
			if (!first.equals("\""))
			{
				input = "\"" + input;
			}
			first = Character.toString(input.charAt(input.length()-1));
			if (!first.equals("\""))
			{
				input += "\"";
			}
		}

		strings.clear();

		String tmp = new String();
		boolean isOpen = false;

		for(int i=0; i<input.length(); i++)
		{
			String chr = Character.toString(input.charAt(i));
			if (chr.equals("\""))
			{
				if (i+1<input.length())
				{
					if (!isOpen)
					{
						isOpen = true;
					}
					else
					{
						String next = Character.toString(input.charAt(i+1));
						if (next.equals("\""))
						{
							tmp += "\"";
							i++;
						}
						else
						{
							//if(!((strings.size()==0)&&(tmp.trim().equals(""))))
							{
								strings.add(tmp);
							}
							tmp = new String();
							isOpen = false;
						}
					}
				}
				else
				{
					if (!((strings.size()==0) && (tmp.trim().isEmpty())))
					{
						strings.add(tmp);
					}
					tmp = new String();
					isOpen = false;
				}
			}
			else
			{
				if (isOpen)
				{
					tmp += chr;
				}
			}
		}
		if (!(tmp.trim().isEmpty()))
		{
			strings.add(tmp);
		}
	}

	public String getCommaText()
	{
		String res = new String();

		for (int i = 0; i<strings.size(); i++)
		{
			if (i==0)
			{
				res+= "\"" + get(i).replace("\"", "\"\"") + "\"";
			}
			else
			{
				res+= ",\"" + get(i).replace("\"", "\"\"") + "\"";
			}
		}

		return res;
	}

	public void loadFromFile(String _filename)
	{
		try
		{
			StringBuffer buffer = new StringBuffer();
			InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(_filename)),"UTF-8");
			Reader in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1)
			{
				buffer.append((char)ch);
			}
			in.close();

			strings.clear();
			add(StringList.explode(buffer.toString(),"\n"));
		}
		catch(IOException ex){}

/*		try
		{
			BTextfile inp = new BTextfile(_filename);
			inp.reset();
			strings.clear();
			while(!inp.eof())
			{
				String s = inp.readln();
				strings.add(s);
			}
			inp.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
*/
	}

    public void saveToFile(String _filename)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(_filename);
            Writer out = new OutputStreamWriter(fos, "UTF-8");
            out.write(this.getText());
            out.close();
            /*
            BTextfile inp = new BTextfile(_filename);
            inp.rewrite();
            inp.write(this.getText());
            inp.close();
             */
        }
        catch (IOException ex)
        {
            System.err.println("StringListsaveToFile(): " + ex.getMessage());
        }
    }

    public String copyFrom(int beginLine, int beginIndex, int endLine, int endIndex)
    {
        String ret = "";
        for (int i=beginLine; i<=endLine; i++)
        {
            String line = get(i);
            //System.err.println(i+") "+line);
            if (i == beginLine)
            {
                if((line.length()>beginIndex) && (beginIndex>=0)) ret+=line.substring(beginIndex);
            }
            else if (i == endLine)
            {
                ret += "\n" + line.substring(0, Math.min(endIndex, line.length()));
            }
            else
            {
                ret += "\n" + line;
            }
        }/**/
        //System.err.println("Res = "+ret);
        return ret;
    }

    // START KGU 2015-11-25
    /**
     * Returns a multi-line {@link String} composed of the sub-StringList from element
     * {@code _start} to element {@code _end} (excluded)
     * @param _start - index of first element to include
     * @param _end - index after last element to include
     * @return a string with newlines as separator
     */
    public String getText(int _start, int _end)
    {
//        String ret = "";
//        for(int i = Math.min(_start, count()); i < Math.min(_end, count()); i++)
//        {
//            String line = get(i);
//            //System.err.println(i+") "+line);
//            ret += "\n" + line;
//        }
//        //System.err.println("Res = "+ret);
//        return ret;
    	return concatenate("\n", _start, _end);
    }
    
    /**
     * Returns a multi-line String composed of the sub-StringList from element
     * _start to the end
     * @param _start - index of first element to include
     * @return a string with newlines as separator
     */
    public String getText(int _start)
    {
        return getText(_start, count());
    }
    
    /**
     * Removes all elements being equal to the given string _string
     * @param _string - the searched string
     * @return number of deletions
     */
    public int removeAll(String _string)
    // START KGU#375 2017-04-04: For regularity, new method to remove case-independently
//    {
//    	int nRemoved = 0;
//    	int i = 0;
//    	while (i < count())
//    	{
//    		if (strings.get(i).equals(_string))
//    		{
//    			strings.removeElementAt(i);
//    			nRemoved++;
//    		}
//    		else
//    		{
//        		i++;    			
//    		}
//    	}
//    	return nRemoved;
//    }
    {
        return removeAll(_string, true);
    }
    
    /**
     * Removes all elements being exactly or case-insensitively equal to the given string _string
     * @param _string - the searched string
     * @param _matchCase - if the string is be compared exactly (or case-ignoringly)
     * @return number of deletions
     */
    public int removeAll(String _string, boolean _matchCase)
    {
        int nRemoved = 0;
        int i = 0;
        while (i < count())
        {
            if (_matchCase && strings.get(i).equals(_string) || strings.get(i).equalsIgnoreCase(_string))
            {
                strings.removeElementAt(i);
                nRemoved++;
            }
            else
            {
                i++;    			
            }
        }
        return nRemoved;
    }
    // END KGU#375 2017-04-04
    // END KGU 2015-11-25
    
    // START KGU 2016-04-03: New methods to ease case-independent manipulations
    /**
     * Removes all subsequences being equal to _subList, either case-independently
     * or not, according to the _matchCase argument
     * @param _subList - The subsequence to cut out
     * @param _matchCase - if false then case is ignored
     * @return the number of removed matches
     */
    public int removeAll(StringList _subList, boolean _matchCase)
    {
        int nRemoved = 0;
        int pos = -1;
        while ((pos = this.indexOf(_subList, pos+1, _matchCase)) >= 0)
        {
            for (int i = 0; i < _subList.count(); i++)
            {
                strings.removeElementAt(pos);
            }
            nRemoved++;
        }
        return nRemoved;
    }
    // END KGU 2016-04-03
    
    // START KGU#92 2015-12-01: New method to facilitate bugfix #41
    /**
     * Replaces all elements being equal to the given string _stringOld by
     * _stringNew
     * @param _stringOld - the searched string
     * @param _stringNew - the string to replace occurrences of _stringOld
     * @return number of replacements
     */
    public int replaceAll(String _stringOld, String _stringNew)
    {
    	// START KGU#129 2016-01-08: Delegated to common submethod
//    	int nReplaced = 0;
//    	int i = 0;
//    	while (i < count())
//    	{
//    		if (strings.get(i).equals(_stringOld))
//    		{
//    			strings.setElementAt(_stringNew, i);
//    			nReplaced++;
//    		}
//    		else
//    		{
//    			i++;    			
//    		}
//    	}
//    	return nReplaced;
    	return replaceAllBetween(_stringOld, _stringNew, true, 0, count());
    	// END KGU#129 2016-01-08
    }

    /**
     * Replaces all elements being case-independently equal to the given string
     * _stringOld by _stringNew
     * @param _stringOld - the searched string
     * @param _stringNew - the string to replace occurrences of _stringOld
     * @return number of replacements
     */
    public int replaceAllCi(String _stringOld, String _stringNew)
    {
    	// START KGU#129 2016-01-08: Delegated to common submethod
//    	int nReplaced = 0;
//    	int i = 0;
//    	while (i < count())
//    	{
//    		if (strings.get(i).equalsIgnoreCase(_stringOld))
//    		{
//    			strings.setElementAt(_stringNew, i);
//    			nReplaced++;
//    		}
//    		else
//    		{
//    			i++;    			
//    		}
//    	}
//    	return nReplaced;
    	return replaceAllBetween(_stringOld, _stringNew, false, 0, count());
    	// END KGU#129 2016-01-08
    }
    // END KGU#92 2015-12-01
    
    // START KGU#129 2016-01-08: Extended interface to facilitate bugfix #96
    /**
     * Replaces all elements being exactly (or case-independently) equal to the given
     * string _stringOld by _stringNew; works only within index range _start and _end
     * (where _end is not included).
     * @param _stringOld - the searched string
     * @param _stringNew - the string to replace occurrences of _stringOld
     * @param _matchCase - whether or not letter case must match exactly
     * @param _fromIndex - index of first element to be affected
     * @param _toIndex - index beyond the last element to be affected
     * @return number of replacements
     */
    public int replaceAllBetween(String _stringOld, String _stringNew, boolean _matchCase, int _fromIndex, int _toIndex)
    {
    	int nReplaced = 0;
    	for (int i = Math.max(0, _fromIndex); i < Math.min(_toIndex, count()); i++)
    	{
    		if (_matchCase && strings.get(i).equals(_stringOld) ||
    				!_matchCase && strings.get(i).equalsIgnoreCase(_stringOld))
    		{
    			strings.setElementAt(_stringNew, i);
    			nReplaced++;
    		}
    	}
    	return nReplaced;
    }
    // END KGU#129 2016-01-08

    @Override
    public String toString()
    {
    	return getCommaText();
    }
    
    // START KGU 2015-11-24
    public void clear()
    {
    	this.strings.clear();
    }
    // END KGU 2015-11-24

    
    // START BOB 2016-08-01
    public String[] toArray()
    {
        String[] array = new String[count()];
        for (int i = 0; i < count(); i++) {
            String get = strings.get(i);
            array[i]=get;
        }
        return array;
    }
    
    /**
     * Removes the element at the given {@code index}.
     * @param index - the index of the element to be removed
     */
    public void remove(int index)
    {
        strings.remove(index);
    } 
    // END BOB 2016-08-01
    
    // START KGU 2017-01-31
    /**
     * Removes all elements from position {@code fromIndex} to
     * position {@code toIndex-1}.
     * @param fromIndex - the beginning index (inclusive)
     * @param toIndex - the ending index (exclusve) 
     */
    public void remove(int fromIndex, int toIndex)
    {
    	for (int count = Math.min(toIndex, this.strings.size())-fromIndex; count > 0; count--) {
    		strings.remove(fromIndex);
    	}
    }
    // END KGU 2017-01-31
    
    // START KGU 2017-10-29
    /**
     * Removes all elements at front and rear that contain only whitespace, such that
     * this.concatenate().trim() and this.trim().concatenate() produce the same result.<br/>
     * Note that the StringList itself is bound to be modified, not a copy of this!
     * @return this StringList after having been trimmed.
     * @see #removeAll(String)
     * @see #removeAll(String, boolean)
     * @see #remove(int)
     * @see #remove(int, int)
     * @see #subSequence(int, int)
     */
    public StringList trim()
    {
    	// Trim at front
    	while (!strings.isEmpty() && strings.get(0).trim().isEmpty()) {
    		strings.remove(0);
    	}
    	// Trim at rear
    	while (!strings.isEmpty() && strings.get(strings.size()-1).trim().isEmpty()) {
    		strings.remove(strings.size()-1);
    	}
    	return this;
    }
    // END KGU 2017-10-29
        
    public static void main(String[] args)
    {
        StringList sl = new StringList();
        sl.setCommaText("\"\",\"1\",\"2\",\"3\",\"sinon\"");
        System.out.println(sl.getText());
        StringList sl1 = sl.copy();
        System.out.println(sl1.getText());
    }
}

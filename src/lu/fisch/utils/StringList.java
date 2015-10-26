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
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
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

	private Vector strings = new Vector();

	public static StringList getNew(String _string)
	{
		StringList sl = new StringList();
		sl.add(_string);
		return sl;
	}

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

	public static StringList explode(StringList _source, String _by)
	{
		StringList sl = new StringList();

		for(int s=0;s<_source.count();s++)
		{
			String[] multi = _source.get(s).split(_by);
			for(int i=0;i<multi.length;i++)
			{
				sl.add(multi[i]);
			}
		}

		return sl;
	}

	public static StringList explodeWithDelimiter(String _source, String _by)
	{
		String[] multi = _source.split(_by);
		StringList sl = new StringList();

		for(int i=0;i<multi.length;i++)
		{
			if(i!=0)
			{
				sl.add(_by);
			}
			sl.add(multi[i]);
		}

		return sl;
	}

	public static StringList explodeWithDelimiter(StringList _source, String _by)
	{
		StringList sl = new StringList();

		for(int s=0;s<_source.count();s++)
		{
			StringList multi = BString.explodeWithDelimiter(_source.get(s),_by);
			sl.add(multi);
		}

		return sl;
	}

	public StringList copy()
	{
		StringList sl = new StringList();
		//sl.add("TEXT");
		sl.setCommaText(this.getCommaText()+"");
		return sl;
	}

	public void add(String _string)
	{
		strings.add(_string);
	}

	public void addOrdered(String _string)
	{
		if(count()==0)
		{
			add(_string);
		}
		else
		{
			boolean inserted = false;
			for(int i=0;i<strings.size();i++)
			{
				if (((String) strings.get(i)).compareTo(_string)>0)
				{
					strings.insertElementAt(_string,i);
					inserted = true;
					break;
				}
			}

			if(inserted==false)
			{
				add(_string);
			}
		}
	}

	public void addByLength(String _string)
	{
            if (!_string.equals(""))
		if(count()==0)
		{
			add(_string);
		}
		else
		{
			boolean inserted = false;
			for(int i=0;i<strings.size();i++)
			{
				if (((String) strings.get(i)).length()<_string.length())
				{
					strings.insertElementAt(_string,i);
					inserted = true;
					break;
				}
			}

			if(inserted==false)
			{
				add(_string);
			}
		}
	}

	public void addIfNew(String _string)
	{
		if(!strings.contains(_string))
		{
			add(_string);
		}
		/*
		boolean found = false;
		for(int i=0;i<strings.size();i++)
		{
			if(((String) strings.get(i)).equals(_string))
			{
				found=true;
			}
		}
		if(found==false)
		{
			add(_string);
		}
		*/
	}

	public void addOrderedIfNew(String _string)
	{
		boolean found = false;
		for(int i=0;i<strings.size();i++)
		{
			if(((String) strings.get(i)).equals(_string))
			{
				found=true;
			}
		}
		if(found==false)
		{
			addOrdered(_string);
		}
	}

	public void addByLengthIfNew(String _string)
	{
		boolean found = false;
		for(int i=0;i<strings.size();i++)
		{
			if(((String) strings.get(i)).equals(_string))
			{
				found=true;
			}
		}
		if(found==false)
		{
			addByLength(_string);
		}
	}

	public void add(StringList _stringList)
	{
		for(int i=0;i<_stringList.count();i++)
		{
			strings.add(_stringList.get(i));
		}
	}

	public void addIfNew(StringList _stringList)
	{
		for(int i=0;i<_stringList.count();i++)
		{
			if(!strings.contains(_stringList.get(i)))
			{
			   strings.add(_stringList.get(i));
			}
		}
	}

	public boolean contains(String _string)
	{
		boolean found = false;
		for(int i=0;i<strings.size();i++)
		{
			if(((String) strings.get(i)).equals(_string))
			{
				found=true;
			}
		}
		return found;
	}

	public boolean contains(String _string, boolean matchCase)
	{
		boolean found = false;
		for(int i=0;i<strings.size();i++)
		{
			if(matchCase==false)
			{
				if(((String) strings.get(i)).toLowerCase().equals(_string.toLowerCase()))
				{
					found=true;
				}
			}
			else
			{
				if(((String) strings.get(i)).equals(_string))
				{
					found=true;
				}
			}
		}
		return found;
	}



	public StringList reverse()
	{
		StringList sl = new StringList();

		for(int i=0;i<strings.size();i++)
		{
			sl.add(get(count()-i-1));
		}

		return sl;
	}

	public void set(int _index, String _s)
	{
		if(_index<strings.size() && _index>=0)
		{
			strings.remove(_index);
			strings.insertElementAt(_s,_index);
		}
	}

	public String get(int _index)
	{
		if(_index<strings.size() && _index>=0)
		{
			return (String) strings.get(_index);
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
		String[] words = _text.split ("\n");
		strings.clear();
		for (int i=0; i < words.length; i++)
		{
			strings.add(words[i]);
		}
	}

	public String getText()
	{
		String text = new String();

		for(int i=0;i<strings.size();i++)
		{
			if(i==0)
			{
				text=(String) strings.get(i);
			}
			else
			{
				text+="\n"+(String) strings.get(i);
			}
		}

		return text;
	}

	public String getLongString()
	{
		String text = new String();

		for(int i=0;i<strings.size();i++)
		{
			if(i==0)
			{
				text=(String) strings.get(i);
			}
			else
			{
				text+=" "+(String) strings.get(i);
			}
		}

		return text;
	}

	public int count()
	{
		return strings.size();
	}

	public void setCommaText(String _input)
	{
		String input = _input+"";

		// if not CSV, make it CSV
		if(input.length()>0)
		{
			String first = Character.toString(input.charAt(0));
			if(!first.equals("\""))
			{
				input="\""+input;
			}
			first = Character.toString(input.charAt(input.length()-1));
			if(!first.equals("\""))
			{
				input=input+"\"";
			}
		}

		strings.clear();

		String tmp = new String();
		boolean open = false;

		for(int i=0;i<input.length();i++)
		{
			String chr = Character.toString(input.charAt(i));
			if(chr.equals("\""))
			{
			   if(i+1<input.length())
			   {
				if(open == false)
				{
					open =true;
				}
				else
				{
					String next = Character.toString(input.charAt(i+1));
					if(next.equals("\""))
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
						open = false;
					}
				}
			   }
			   else
			   {
				   if(!((strings.size()==0)&&(tmp.trim().equals(""))))
				   {
					   strings.add(tmp);
				   }
				   tmp = new String();
				   open = false;
			   }
			}
			else
			{
			   if(open == true)
			   {
				tmp += chr;
			   }
			}
		}
		if(!(tmp.trim().equals("")))
		{
			strings.add(tmp);
		}
	}

	public String getCommaText()
	{
		String res = new String();

		for (int i = 0;i<strings.size();i++)
		{
			if(i==0)
			{
				res+= "\""+BString.replace(get(i),"\"","\"\"")+"\"";
			}
			else
			{
				res+= ",\""+BString.replace(get(i),"\"","\"\"")+"\"";
			}
		}

		return res;
	}

	public void loadFromFile(String _filename)
	{
            try
            {
                StringBuffer buffer = new StringBuffer();
                InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(_filename)),"UTF8");
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

/*        try
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
            Writer out = new OutputStreamWriter(fos, "UTF8");
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
            System.out.println(ex.getMessage());
        }
    }

    public String copyFrom(int beginLine, int beginIndex, int endLine, int endIndex)
    {
        String ret = "";
        for(int i=beginLine;i<=endLine;i++)
        {
            String line = get(i);
            //System.err.println(i+") "+line);
            if(i==beginLine)
            {
                if((line.length()>beginIndex) && (beginIndex>=0)) ret+=line.substring(beginIndex);
            }
            else if (i==endLine)
            {
                ret+="\n"+line.substring(0, Math.min(endIndex, line.length()));
            }
            else
            {
                ret+="\n"+line;
            }
        }/**/
        //System.err.println("Res = "+ret);
        return ret;
    }


    @Override
	public String toString()
	{
		return getCommaText();
	}

        
        public static void main(String[] args)
        {
            StringList sl = new StringList();
            sl.setCommaText("\"\",\"1\",\"2\",\"3\",\"sinon\"");
            System.out.println(sl.getText());
        }
}

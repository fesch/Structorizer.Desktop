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

package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class extends a "JDialog" to support language settings
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008.01.14      First Issue
 *      Kay Gürtzig     2015.10.14      Hook for customizable class-specific translation activities added
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.*;

import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import lu.fisch.utils.*;

public class LangDialog extends JDialog
{
	protected String langFile = null;
	
	private static JFrame dummyFrame = new JFrame();


	public void setLang(String _langfile)
	{
		langFile = _langfile;
	
		setLang(this,_langfile);
	}
	
	public void setLang(StringList _lines)
	{
		setLang(this,_lines);
	}
	
	public static void setLang(Component _com, String _langfile)
	{
		String input = new String();
		try 
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(_com.getClass().getResourceAsStream(_langfile), "UTF-8"));
			//BufferedReader in = new BufferedReader(new InputStreamReader(_com.getClass().getResourceAsStream(_langfile), "ISO-8859-1"));
			String str;
			while ((str = in.readLine()) != null) 
			{
				input+=str+"\n";
			}
			in.close();
		} 
		catch (IOException e) 
		{
			System.out.println("LANG: Error while loading language file => "+e.getMessage());
		}
		
		StringList lines = new StringList();
		lines.setText(input);
		setLang(_com,lines);
	}

	public static void setLang(Component _com, StringList _lines)
	{
		StringList pieces;
		StringList parts;
		
		
		for(int i=0;i<_lines.count();i++)
		{
			parts = StringList.explode(_lines.get(i),"=");
			pieces = StringList.explode(parts.get(0),"\\.");
			
			if (pieces.get(0).toLowerCase().equals(_com.getClass().getSimpleName().toLowerCase()))
			{	
				if(pieces.get(1).toLowerCase().equals("title"))
				{
					if(_com instanceof JDialog)
					{
						((JDialog) _com).setTitle(parts.get(1));
					}
				}
				// START KGU 2015-10-14: Hook for some more sophisticated class-specific stuff added
				else if (pieces.get(1).equals("class_specific") && _com instanceof LangDialog)
				{
					((LangDialog)_com).setLangSpecific(pieces, parts.get(1));
				}
				// END KGU 2015-10-14
				else
				{
					try
					{
						Field field = _com.getClass().getDeclaredField(pieces.get(1));
						
						if(field!=null)
						{
							Class fieldClass = field.getType();
							
							if (pieces.get(2).toLowerCase().equals("text"))
							{
								Method method = fieldClass.getMethod("setText",new Class [] {String.class});
								method.invoke(field.get(_com),new Object [] {parts.get(1)});
							}
							else if (pieces.get(2).toLowerCase().equals("tooltip"))
							{
								Method method = fieldClass.getMethod("setToolTipText",new Class [] {String.class});
								method.invoke(field.get(_com),new Object [] {parts.get(1)});
							}
							else if (pieces.get(2).toLowerCase().equals("border"))
							{
								Method method = fieldClass.getMethod("setBorder",new Class [] {Border.class});
								method.invoke(field.get(_com),new Object [] {new TitledBorder(parts.get(1))});
						 	}
							else if (pieces.get(2).toLowerCase().equals("tab"))
							{
								Method method = fieldClass.getMethod("setTitleAt",new Class [] {int.class,String.class});
								method.invoke(field.get(_com),new Object [] {Integer.valueOf(pieces.get(3)),parts.get(1)});
							}
						}
						else
						{
							System.out.println("LANG: Field not found <"+pieces.get(1)+">");
						}
					}
					catch (Exception e)
					{
						System.out.println("LANG: Error while setting field <"+pieces.get(2)+"> for element <"+pieces.get(1)+">!\n"+e.getMessage());
					}
				}
			}
		}
	}	
	
	
	// START KGU 2015-10-14
	/**
	 * Hook to do some subclass-specific language translation for possibly data-dependent GUI stuff.
	 * This is just a dummy to be overridden if required.
	 * @param keys = a list of identifying strings (like of a domain name), starting with the class name, for matching purposes
	 * @param translation = the text to be used if the ids matched
	 * @return true if the matching succeeded and no further matching attempts with these keys ought to be done
	 */
	protected void setLangSpecific(StringList keys, String translation)
	{
	}
	// END KGU 2015-10-14

	public LangDialog() 
	{
		super(dummyFrame);
		dummyFrame.setIconImage(IconLoader.ico074.getImage());
		this.repaint();
	}

	public LangDialog(Frame owner) 
	{
		super(owner);
	}
	
	public LangDialog(Dialog owner) 
	{
		super(owner);
	}
	
	public LangDialog(Frame owner, boolean modal)
	{
		super(owner, modal);
	}
}

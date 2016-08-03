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
 *      Kay Gürtzig     2016.03.13      KGU#156: Support for JComboBox added on occasion of enhancement #124
 *      Kay Gürtzig     2016.07.03      KGU#203: File conversion to StringList now skips comments and empty lines
 *      Bob Fisch       2016.08.02      Bugfix #218: equality signs in translations mutilated them
 *      Kay Gürtzig     2016.08.03      Inheritance changed (ILangDialog added)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.border.*;

import lu.fisch.utils.*;

/**
 * Extends JDialog to facilitate language localization, also provides static methods
 * applicable to other GUI classes not inheriting from LangDialog.
 * @author Robert Fisch
 *
 */
@SuppressWarnings("serial")
public class LangDialog extends JDialog implements ILangDialog
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
		// START KGU#203 2016-07-03: Skip comment lines (to accelerate matching)
		boolean isComment = false;
		// END KGU#203 2016-07-03
		try 
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(_com.getClass().getResourceAsStream("/lu/fisch/structorizer/locales/"+_langfile), "UTF-8"));
			//BufferedReader in = new BufferedReader(new InputStreamReader(_com.getClass().getResourceAsStream(_langfile), "ISO-8859-1"));
			String str;
			while ((str = in.readLine()) != null) 
			{
				// START KGU#203 2016-07-03: Skip comment lines
				//input+=str+"\n";
				int cePos = -1;
				if (str.startsWith("/*"))
				{
					isComment = true;
				}
				if (isComment && (cePos = str.indexOf("*/")) >= 0)
				{
					str = str.substring(cePos+2);
					isComment = false;
				}
				// START KGU#203 2016-08-02: Also ignore special markers
				//if (!str.isEmpty() && !isComment && !str.startsWith("//"))
				if (!str.isEmpty()
						&& !isComment
						&& !str.startsWith("//")
						&& (str.indexOf("=") > 0)	// Ensure it specifies a translation
						&& !str.startsWith("-----")
						&& !str.startsWith(">>>"))
				// END KGU#203 2016-08-02
				{
					input += str+"\n";
				}
				// END KGU#203 2016-07-03
			}
			in.close();
		} 
		catch (IOException e) 
		{
			System.err.println("LANG: Error while loading language file => " + e.getMessage());
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
			parts = StringList.explodeFirstOnly(_lines.get(i),"=");
			pieces = StringList.explode(parts.get(0),"\\.");
			
			if (pieces.get(0).equalsIgnoreCase(_com.getClass().getSimpleName()) && !parts.get(1).trim().isEmpty())
			{	
				if(pieces.get(1).toLowerCase().equals("title"))
				{
					if(_com instanceof JDialog)
					{
						((JDialog) _com).setTitle(parts.get(1));
					}
				}
				// START KGU 2015-10-14: Hook for some more sophisticated class-specific stuff added
				else if (pieces.get(1).equals("class_specific") && _com instanceof ILangDialog)
				{
					((ILangDialog)_com).setLangSpecific(pieces, parts.get(1));
				}
				// END KGU 2015-10-14
				else
				{
					Field field = null;
					String errorMessage = null;
					try
					{
						// First try on own fields - whatever access level they might have
						field = _com.getClass().getDeclaredField(pieces.get(1));
					// START KGU#3 2015-11-03: Addition to enable the access to inherited fields
					}
					catch (Exception e)
					{
						errorMessage = e.getMessage();
					}
					if (field == null)
					{
						// Now try on inherited PUBLIC fields, too (unfortunately, a retrieval of protected inherited fields seems to be missing)
						try
						{
							field = _com.getClass().getField(pieces.get(1));
							// If this works then cancel the previously obtained error message
							errorMessage = null;
						}
						catch (Exception e)
						{
							errorMessage = e.getMessage();
						}
					}
					if (errorMessage != null)
					{
						System.err.println("LANG: Error accessing element <" + 
								pieces.get(0) + "." + pieces.get(1) + ">!\n" + errorMessage);						
					}
					else try {
					// END KGU#3 2015-11-03

						if(field!=null)
						{
							Class<?> fieldClass = field.getType();
							String piece2 = pieces.get(2).toLowerCase();
							
							if (piece2.equals("text"))
							{
								Method method = fieldClass.getMethod("setText",new Class [] {String.class});
								method.invoke(field.get(_com),new Object [] {parts.get(1)});
							}
							else if (piece2.equals("tooltip"))
							{
								Method method = fieldClass.getMethod("setToolTipText",new Class [] {String.class});
								method.invoke(field.get(_com),new Object [] {parts.get(1)});
							}
							else if (piece2.equals("border"))
							{
								Method method = fieldClass.getMethod("setBorder",new Class [] {Border.class});
								method.invoke(field.get(_com),new Object [] {new TitledBorder(parts.get(1))});
						 	}
							else if (piece2.equals("tab"))
							{
								Method method = fieldClass.getMethod("setTitleAt",new Class [] {int.class,String.class});
								method.invoke(field.get(_com),new Object [] {Integer.valueOf(pieces.get(3)),parts.get(1)});
							}
							// START KGU#183 2016-04-24: Enh. #173 - new support
							else if (piece2.equals("mnemonic"))
							{
								Method method = fieldClass.getMethod("setMnemonic", new Class [] {int.class});
								int keyCode = KeyEvent.getExtendedKeyCodeForChar(parts.get(1).toLowerCase().charAt(0)); 
								if (keyCode != KeyEvent.VK_UNDEFINED)
								{
									method.invoke(field.get(_com),new Object [] {Integer.valueOf(keyCode)});
								}
							}
							// END KGU#183 2016-04-24
							// START KGU#156 2016-03-13: Enh. #124 - intended for JComboBoxes
							else if (piece2.equals("item"))
							{
								// The JCombobox is supposed to be equipped with enum objects providing a setText() method
								// (see lu.fisch.structorizer.elements.RuntimeDataPresentMode and
								// lu.fisch.structorizer.executor.Control for an example).
								Method method = fieldClass.getMethod("getItemAt", new Class [] {int.class});
								Object item = method.invoke(field.get(_com), new Object[] {Integer.valueOf(pieces.get(3))});
								if (item != null)
								{
								Class<?> itemClass = item.getClass();
								method = itemClass.getMethod("setText", new Class[] {String.class});
								method.invoke(item, new Object[] {parts.get(1)});
								}
							}
							// END KGU#156 2016-03-13
						}
						else
						{
							// START KGU 2015-11-03: Better add the class name for more precision
							//System.out.println("LANG: Field not found <"+pieces.get(1)+">");
							System.err.println("LANG: Field not found <" + pieces.get(0) + "." + pieces.get(1) + ">");
							// END KGU 2015-11-03
						}
					}
					catch (Exception e)
					{
						// START KGU 2015-11-03: Better add the class name for more precision
						//System.out.println("LANG: Error while setting field <"+pieces.get(2)+"> for element <"+pieces.get(1)+">!\n"+e.getMessage());
						System.err.println("LANG: Error while setting field <" + pieces.get(2) + "> for element <" + 
								pieces.get(0) + "." + pieces.get(1) + ">!\n" + e.getMessage());
						// END KGU 2015-11-03
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
	public void setLangSpecific(StringList keys, String translation)
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

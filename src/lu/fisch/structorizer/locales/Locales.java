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

package lu.fisch.structorizer.locales;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Fundamental localization manager, hold the locales and performs translations
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2016.08.02      First Issue
 *      Kay Gürtzig     2016.08.12      Mechanism to translate arrays of controls (initially for AnalyserPreferences)
 *      Kay Gürtzig     2016.09.05      Mechanism to translate Hashtables of controls (initially for language preferences)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************/

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import lu.fisch.utils.StringList;

/**
 *
 * @author robertfisch
 */
public class Locales {
    /**
     * LOCALES_LIST of all locales we have and their respective English denomination
     * Locales for actually existing languages MUST have an English language name, whereas
     * pure technical pseudo-locales MUST NOT have a denomination
     * Note: Order matters (preferences menu, Translator etc. will present locales in the order given here) 
     */
    public static final String[][] LOCALES_LIST = {
    	{"en", "English"},
    	{"de", "German"},
    	{"fr", "French"},
    	{"nl", "Hollandish"},
    	{"lu", "Luxemburgish"},
    	{"es", "Spanish"},
    	{"pt_br", "Portuguese (Brazilian)"},
    	{"it", "Italian"},
    	{"zh-cn", "Chinese (simplified)"},
    	{"zh-tw", "Chinese (traditional)"},
    	{"cz", "Czech"},
    	{"ru",	"Russian"},
    	{"pl", "Polish"},
    	// pseudo and auxiliary locales 
    	{"empty", null},
    	{"preview", null},
    	{"external", null}
    	};
    
    // the "default" oder "master" locale
    public static final String DEFAULT_LOCALE = "en";
    
    // structure were all data is being loaded to
    private final HashMap<String,Locale> locales = new HashMap<String,Locale>();
    
    private static Locales instance = null;
    
    private String loadedLocaleName = null;
    private String loadedLocaleFilename = null;
    private final ArrayList<Component> components = new ArrayList<Component>();
    
    public static Locales getInstance()
    {
        if(instance==null) instance=new Locales();
        return instance;
    }
    
    public static boolean hasInstance()
    {
        return (instance!=null);
    }
    
    public static void clearReference()
    {
        instance=null;
        System.gc();
    }
    
    public static void main(String[] args)
    {
        Locales locales = Locales.getInstance();

        System.out.println("Files:");
        String[] names = locales.getNames();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            System.out.println("- "+name);
        }
        
        System.out.println("\nSections:");
        for (int i = 0; i < locales.getSectionNames().size(); i++) {
            String get = locales.getSectionNames().get(i);
            System.out.println("- "+get);
        }
    }

    private Locales() 
    {
        // do not preload all files
        // rather use some "load on demand"
        /*
        for (int i = 0; i < LOCALES_LIST.length; i++) {
            String name = LOCALES_LIST[i];
            locales.put(name, new Locale(name+".txt"));
        }
        */
    }
    
    public Locale getDefaultLocale()
    {
        // get the default locale
        return getLocale(DEFAULT_LOCALE);
    }
    
    /**
     * Retrieve all section names
     * 
     * @return  the LOCALES_LIST with the names of the sections found in all files
     */
    public ArrayList<String> getSectionNames()
    {
        ArrayList<String> sections = new ArrayList<String>();
        String[] localNames = getNames();
        for (int i = 0; i < localNames.length; i++) {
            String localName = localNames[i];
            Locale locale = locales.get(localName);
            String[] sectionNames = locale.getSectionNames();
            for (int s = 0; s < sectionNames.length; s++) {
                String sectionName = sectionNames[s];
                if(!sections.contains(sectionName))
                    sections.add(sectionName);
            }
        }
        return sections;
    }
    
    public String[] getNames() {
        return locales.keySet().toArray(new String[locales.size()]);
    }
    
    public Locale getLocale(String name)
    {
        // try to get the locale
        Locale locale = locales.get(name);
        // if it has not yet been loaded
        if(locale==null)
        {
            // load it now ...
            locale=new Locale(name+".txt");
            // ... and put it into the list
            locales.put(name, locale);
        }
        return locale;
    }
    
    public StringList whoHasKey(String keyName)
    {
        StringList result = new StringList();
        
        for (int i = 0; i < LOCALES_LIST.length; i++) {
            String localeName = LOCALES_LIST[i][0];
            if(getLocale(localeName).hasKey(keyName)) result.add(localeName);
        }
        
        return result;
    }
    
    
    public void register(Component component)
    {
        // register a new component
        components.add(component);

        //System.out.println("Actual number of components: "+components.size());
        
        // set it the actual language, if possible
        //setLang(component);
        // update all components!
        updateComponents();
        
    }
    
    public void unregister(Component component)
    {
        // unregister a component
        components.remove(component);
    }
    
    private void updateComponents()
    {
        // loop through all components
        for (int i = 0; i < components.size(); i++) {
            Component component = components.get(i);
            
            // set it the actual language, if possible
            Locales.this.setLocale(component);
        }
    }
    
    public static boolean isNamedLocale(String localeName)
    {
        boolean found = false;
        for (int i = 0; !found && i < LOCALES_LIST.length; i++)
        {
        	found = LOCALES_LIST[i][0].equals(localeName);
        }
    	return found;
    }
    
    public void setLocale(String localeName)
    {
        loadedLocaleName = localeName.replace(".txt", "");
        
        // if we can't find the name of the loaded locale,
        // we suppose a filepath has been passed
        //if(!Arrays.asList(LOCALES_LIST).contains(loadedLocaleName))
        if (!isNamedLocale(loadedLocaleName))
        {
            // let's check if it is an existing file
            if((new File(localeName)).exists())
            {
                // load the file
                StringList lines = new StringList();
                lines.loadFromFile(localeName);
                // set it
                setExternal(lines, localeName);
            }
            else
            {
                // get the default
                loadedLocaleName="en";
            }
        }
        
        if(!localeName.equals("preview") && !localeName.equals("external"))
            loadedLocaleFilename=loadedLocaleName+".txt";
        // update all registered components
        updateComponents();
    }
    
    public void setLocale(Component component)
    {
        // check if we have a loaded LocaleName
        if(loadedLocaleName!=null) {
            // try to load the corresponding locale
            Locale locale = getLocale(loadedLocaleName);
            if(locale!=null) {
                // set it
                setLocale(component, locale.getBody());
            }
        }
    }
    
    public void setLocale(StringList lines)
    {
        loadedLocaleName="preview";
        Locale locale = getLocale(loadedLocaleName);
        updateComponents();
    }

    public void setLocale(Component component, String localeName)
    {
        localeName = localeName.replace(".txt","");
        Locale locale = getLocale(localeName);
        if(locale!=null)
        {
            Locales.this.setLocale(component, locale.getBody());
        }
    }
    
    private boolean checkConditions(Component component, StringList conditions)
    {
        // suppose the result will be OK
        boolean result = true;
        
        // loop through each condition
        for (int i = 0; i < conditions.count(); i++) {
            String condition = conditions.get(i);
            // split it up
            String[] parts = condition.split(":");
            String fieldName = parts[0];
            String value = parts[1];
            
            String fieldValue = null;
            String errorMessage = null;
            // check to see if we got a method ...
            if(fieldName.endsWith("()"))
            {
                try 
                {
                    Method method = component.getClass().getMethod(fieldValue, new Class[]{});
                    method.invoke(component, new Object[]{});
                }
                catch(Exception e)
                {
                    errorMessage = e.getMessage();
                }
            }
            // ... of a field
            else
            {
                Field field = null;
                try {
                    // first try on own fields
                    field = component.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                } 
                catch (Exception e) 
                {
                    errorMessage = e.getMessage();
                    e.printStackTrace();
                }
                if (field == null) {
                    // Now try on inherited PUBLIC fields
                    try {
                        field = component.getClass().getField(fieldName);
                        field.setAccessible(true);
                        // If this works then cancel the previously obtained error message
                        errorMessage = null;
                    } 
                    catch (Exception e) 
                    {
                        errorMessage = e.getMessage();
                        e.printStackTrace();
                    }
                }
                if(field!=null)
                {
                    try
                    {
                        if(field.get(component)!=null)
                            fieldValue = field.get(component).toString();
                    }
                    catch(Exception e)
                    {
                        errorMessage = e.getMessage();
                        e.printStackTrace();
                    }
                }
            }
            
            if(errorMessage!=null)
                System.err.println("CONDITION ("+fieldName+":"+value+"): "+errorMessage);

            if(fieldValue!=null)
                result &= value.trim().equalsIgnoreCase(fieldValue.trim());
        }
        
        return result;
    }
    
    
    // ----[ ATTENTION ]----
    // As this method might be called before a component is fully initialised,
    // so before all contained components have been put there, we might get
    // null pointers. So deal with it! ;-)
    public void setLocale(Component component, StringList lines) {
        StringList pieces;
        StringList parts;
        
        for (int i = 0; i < lines.count(); i++) {
            parts = StringList.explodeFirstOnly(lines.get(i), "=");
            pieces = StringList.explode(parts.get(0), "\\.");
            
            if (pieces.get(0).equalsIgnoreCase(component.getClass().getSimpleName()) && 
                !parts.get(1).trim().isEmpty()) 
            {
                //
                // We know, that we are now in the right component.
                //
                
                // default the condition to true, even if there is none
                boolean condition = true;

                // check for conditions
                String key = parts.get(0);
                if(!parts.get(1).trim().isEmpty() && key.contains("[") && key.endsWith("]"))
                {
                    // cut of last "]"
                    key=key.substring(0, key.length()-1);
                    // split
                    String[] elements = key.split("\\[");
                    // put back the key
                    parts.set(0, elements[0]);
                    // split up the condition
                    StringList conditions = StringList.explode(elements[1], ",");

                    //System.out.println("Found condition "+elements[1]+" for element "+elements[0]);
                    condition = checkConditions(component, conditions);
                    
                    // re-explode pieces for further processing
                    // because parts.get(0) has changed before
                    pieces = StringList.explode(parts.get(0), "\\.");
                }
                
                if(condition)
                    {

                    if (pieces.get(1).toLowerCase().equals("title")) {
                        if (component instanceof JDialog) {
                            ((JDialog) component).setTitle(parts.get(1));
                        }
                    } 
                    else {
                        Field field = null;
                        String errorMessage = null;
                        try {
                            // First try on own fields - whatever access level they might have
                            field = component.getClass().getDeclaredField(pieces.get(1));
                            field.setAccessible(true);
                            // START KGU#3 2015-11-03: Addition to enable the access to inherited fields
                        } catch (Exception e) {
                            errorMessage = e.getMessage();
                        }
                        if (field == null) {
                            // Now try on inherited PUBLIC fields, too (unfortunately, a retrieval of protected inherited fields seems to be missing)
                            try {
                                field = component.getClass().getField(pieces.get(1));
                                field.setAccessible(true);
                                // If this works then cancel the previously obtained error message
                                errorMessage = null;
                            } catch (Exception e) {
                                errorMessage = e.getMessage();
                            }
                        }
                        if (errorMessage != null) {
                            System.err.println("LANG: Error accessing element <"
                                    + pieces.get(0) + "." + pieces.get(1) + ">!\n" + errorMessage);
                        } else if (field != null) {
                    		// END KGU#3 2015-11-03
                        	try {

                        		Class<?> fieldClass = field.getType();
                        		String piece2 = pieces.get(2).toLowerCase();

                        		Object target = field.get(component);

                        		// START KGU#239 2016-08-12: Opportunity to localize an array of controls
                        		if (fieldClass.isArray() && pieces.count() > 3)
                        		{
                        			int length = Array.getLength(field.get(component));
                        			int index = Integer.parseUnsignedInt(piece2);
                        			if (index < length) {
                        				target = Array.get(field.get(component), index);
                        				fieldClass = target.getClass();
                        				pieces.remove(2);	// Index no longer needed
                        				pieces.set(1, pieces.get(1) + "[" + piece2 + "]");
                        				piece2 = pieces.get(2).toLowerCase();
                        			}

                        		}
                        		// END KGU#239 2016-08-12
                        		// START KGU#242 2016-09-04
                        		else if (fieldClass.getName().equals("java.util.Hashtable") && pieces.count() > 3)
                        		{
                        			String piece1_2 = pieces.get(1) + "[" + piece2 + "]";
                        			Method method = fieldClass.getMethod("get", new Class[]{Object.class});
                        			try {
                        				target = method.invoke(target, piece2);
                            			if (target == null)
                            			{
                            				System.err.println("LANG: No Element <" + pieces.get(0) + "." + piece1_2 + "> found!");
                            			}
                        			}
                        			catch (Exception e) {
                        				// FIXME: No idea why this always goes off just on startup
                        			}
                        			if (target != null)
                        			{
                        				fieldClass = target.getClass();
                        				pieces.remove(2);	// Key no longer needed
                         				pieces.set(1, piece1_2);
                         				piece2 = pieces.get(2).toLowerCase();
                        			}
                        		}
                        		// END KGU#242 2016-09-04

                        		if (piece2.equals("text")) {
                        			Method method = fieldClass.getMethod("setText", new Class[]{String.class});
                        			if(target != null)
                        				method.invoke(target, new Object[]{parts.get(1)});
                        		} else if (piece2.equals("tooltip")) {
                        			Method method = fieldClass.getMethod("setToolTipText", new Class[]{String.class});
                        			if(target != null)
                        				method.invoke(target, new Object[]{parts.get(1)});
                        		} else if (piece2.equals("border")) {
                        			Method method = fieldClass.getMethod("setBorder", new Class[]{Border.class});
                        			if(target != null)
                        				method.invoke(target, new Object[]{new TitledBorder(parts.get(1))});
                        		} else if (piece2.equals("tab")) {
                        			Method method = fieldClass.getMethod("setTitleAt", new Class[]{int.class, String.class});
                        			if(target != null)
                        				method.invoke(target, new Object[]{Integer.valueOf(pieces.get(3)), parts.get(1)});
                        		} else if (piece2.equals("header")) {
                        			Method method = fieldClass.getMethod("setHeaderTitle", new Class[]{int.class, String.class});
                        			if(target != null)
                        				method.invoke(target, new Object[]{Integer.valueOf(pieces.get(3)), parts.get(1)});
                        		} // START KGU#183 2016-04-24: Enh. #173 - new support
                        		else if (piece2.equals("mnemonic")) {
                        			Method method = fieldClass.getMethod("setMnemonic", new Class[]{int.class});
                        			int keyCode = KeyEvent.getExtendedKeyCodeForChar(parts.get(1).toLowerCase().charAt(0));
                        			if (keyCode != KeyEvent.VK_UNDEFINED && target != null) {
                        				method.invoke(target, new Object[]{Integer.valueOf(keyCode)});
                        			}
                        		} // END KGU#183 2016-04-24
                        		// START KGU#156 2016-03-13: Enh. #124 - intended for JComboBoxes
                        		else if (piece2.equals("item")) {
                        			// The JCombobox is supposed to be equipped with enum objects providing a setText() method
                        			// (see lu.fisch.structorizer.elements.RuntimeDataPresentMode and
                        			// lu.fisch.structorizer.executor.Control for an example).
                        			Method method = fieldClass.getMethod("getItemAt", new Class[]{int.class});
                        			if(target != null)
                        			{
                        				Object item = method.invoke(target, new Object[]{Integer.valueOf(pieces.get(3))});
                        				if (item != null) {
                        					Class<?> itemClass = item.getClass();
                        					method = itemClass.getMethod("setText", new Class[]{String.class});
                        					method.invoke(item, new Object[]{parts.get(1)});
                        				}
                        			}
                        		}
                        		// END KGU#156 2016-03-13
                        	} catch (Exception e) {
                        		System.err.println("LANG: Error while setting property <" + pieces.get(2) + "> for element <"
                        				+ pieces.get(0) + "." + pieces.get(1) + ">!\n" + e.getMessage());
                        	}
                        } else {
                        	System.err.println("LANG: Field not found <" + pieces.get(0) + "." + pieces.get(1) + ">");
                        }
                    }
                }
            }
        }
    }
    
    public String getLoadedLocaleName()
    {
        if(loadedLocaleName==null) return "en";
        else return loadedLocaleName;
    }
    
    public String getLoadedLocaleFilename()
    {
        if(loadedLocaleFilename==null) return "en.txt";
        else return loadedLocaleFilename;
}
    
    public void setExternal(StringList lines, String filename)
    {
        getLocale("external").parseStringList(lines);
        setLocale("external");
        loadedLocaleFilename=filename;
    }
}

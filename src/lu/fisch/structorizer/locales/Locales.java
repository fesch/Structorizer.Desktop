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

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Fundamental localization manager, holds the locales and performs translations
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date        Description
 *      ------          ----        -----------
 *      Bob Fisch       2016-08-02  First Issue
 *      Kay Gürtzig     2016-08-12  Mechanism to translate arrays of controls (initially for AnalyserPreferences)
 *      Kay Gürtzig     2016-09-05  Mechanism to translate Hashtables of controls (initially for language preferences)
 *      Kay Gürtzig     2016-09-09  Fix in getSectionNames(), Javadoc accomplished
 *      Kay Gürtzig     2016-09-13  Bugfix #241 in checkConditions() (KGU#246)
 *      Kay Gürtzig     2016-09-22  Issue #248: Workaround for Linux systems with Java 1.7
 *      Kay Gürtzig     2016-09-28  KGU#263: Substrings "\n" in the text part now generally replaced by newline
 *      Kay Gürtzig     2016-12-07  Issue #304: Check for feasibility of mnemonic replacement via Reflection
 *      Kay Gürtzig     2016-02-03  Issue #340: registration without immediate update launch
 *      Kay Gürtzig     2017-02-27  Enh. #346: Mechanism to translate an asterisk at index position to a loop over an array
 *      Kay Gürtzig     2017-10-02  Enh. #415: The title localization wasn't done for JFrame offsprings
 *      Kay Gürtzig     2018-07-02  KGU#245: Substrings "[#]" may be replaced by the actual index in an array target
 *      Kay Gürtzig     2019-01-18  Issue #346: Precaution against uninitialized arrays in setLocale()
 *      Kay Gürtzig     2019-03-03  Enh. #327: New methods removeLocale(String, boolean), removeLocales(boolean)
 *      Kay Gürtzig     2019-06-14  Issue #728: Mechanism for setting mnemonics enhanced
 *      Kay Gürtzig     2019-09-30  KGU#736 Precaution against newlines in tooltips
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************
 */

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import lu.fisch.structorizer.gui.ElementNames;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * Fundamental localization manager (for Structorizer), holds the locales and performs
 * translations
 * @author robertfisch
 */
public class Locales {
    /**
     * LOCALES_LIST of all locales we have and their respective English denomination.<br/>
     * Locales for actually existing languages MUST have an English language name, whereas
     * pure technical pseudo-locales MUST NOT have a denomination.<br/>
     * Note: Order matters (preferences menu, Translator etc. will present locales in the order given here) 
     */
    public static final String[][] LOCALES_LIST = {
        {"en", "English"},
        {"de", "German"},
        {"fr", "French"},
        {"nl", "Dutch"},
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
    
    // START KGU#484 2018-03-22: Issue #463
    public static final Logger logger = Logger.getLogger(Locales.class.getName());
    // END KGU#484 2018-03-22

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

        String[] names = locales.getNames();
        StringBuilder localeNames = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            localeNames.append("\n- " + name);
        }
        logger.log(Level.INFO, "Files:{0}", localeNames.toString());

        ArrayList<String> sections = locales.getSectionNames();
        StringBuilder sectNames = new StringBuilder();
        for (int i = 0; i < sections.size(); i++) {
            String get = sections.get(i);
            sectNames.append("\n- "+get);
        }
        logger.log(Level.INFO, "Sections:{0}", sectNames.toString());
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
    
    /**
     * Returns the locale configured as default locale. If the locale hadn't been
     * loaded before it will be loaded now.
     * @return the default locale
     */
    public Locale getDefaultLocale()
    {
        // get the default locale
        return getLocale(DEFAULT_LOCALE);
    }
    
    /**
     * Retrieve all section names from the locales already loaded. At least the
     * default locale is ensured to be loaded.
     * @return  the names of the sections found in the LOADED locales
     */
    public ArrayList<String> getSectionNames()
    {
        ArrayList<String> sections = new ArrayList<String>();
        String[] localeNames = getNames();
        // START KGU 2016-09-09: Bugfix - We must ensure at least the default locale
        if (localeNames.length == 0)
        {
            getDefaultLocale();
            localeNames = getNames();
        }
        // END KGU 2016-09-09
        for (int i = 0; i < localeNames.length; i++) {
            String localeName = localeNames[i];
            Locale locale = locales.get(localeName);
            String[] sectionNames = locale.getSectionNames();
            for (int s = 0; s < sectionNames.length; s++) {
                String sectionName = sectionNames[s];
                if(!sections.contains(sectionName))
                    sections.add(sectionName);
            }
        }
        return sections;
    }
    
    /**
     * Returns a String array containing the names of all locales currently loaded.
     * This does not necessarily comprise all locales from LOCALES_LIST!
     * @return String list of locale names 
     */
    public String[] getNames() {
        return locales.keySet().toArray(new String[locales.size()]);
    }
    
    /**
     * Returns the locale associated with the given name of the locale (language
     * code) or the locale file. If the locale hadn't been loaded yet then it will
     * be loaded now - this may take time and could raise error message boxes.
     * @param name - language code, pseudo locale name, or locale file name
     * @return - the locale associated with the given name
     */
    public Locale getLocale(String name)
    {
        // try to get the locale
        Locale locale = locales.get(name);
        // if it has not yet been loaded
        if (locale == null)
        {
            // load it now ...
            locale = new Locale(name + ".txt");
            // ... and put it into the list
            locales.put(name, locale);
        }
        return locale;
    }
    
    // START KGU#323 2019-03-03: Enh. #327 There should be an opportunity to dispose no longer needed locales
    /**
     * Removes a the {@link Locale} with name {@code name} if it had been cached and is not the current
     * {@link Locale}, unless it contains unsaved changes and {@code discardChanges} is false
     * @param name - name of the Locale to be get rid of
     * @param discardChanges - whether the Locale is to be removed even if it has unsaved changes
     * @return true if there won't be a cached Locale with the given name thereafter
     */
    public boolean removeLocale(String name, boolean discardChanges)
    {
        // Don't dispose the current locale
        boolean isAbsent = !name.equals(loadedLocaleName);
        if (!discardChanges) {
            Locale locale = locales.get(name);
            if (locale != null && locale.hasUnsavedChanges) {
                isAbsent = false;
            }
        }
        if (isAbsent) {
            locales.remove(name);
        }
        return isAbsent;
    }
    
    /**
     * Will remove all cached {@link Locale}s different from the current {@link Locale} unless
     * they have unsaved changes and {@code discardChanges} is not true.
     * @param discardChanges - whether Locales with unsaved changes are to be removed nevertheless
     * @return true if all but the current Locale have been discarded
     */
    public boolean removeLocales(boolean discardChanges)
    {
        boolean allDone = true;
        java.util.Set<String> localeKeys = locales.keySet();
        String[] localeNames = localeKeys.toArray(new String[localeKeys.size()]);
        for (String localeName: localeNames) {
            boolean done = localeName.equals(loadedLocaleName) || removeLocale(localeName, discardChanges);
            allDone = allDone && done;
        }
        return allDone;
    }
    // END KGU#323 2019-03-03
    
    /**
     * Retrieves all locales providing a line with the given key (no matter whether
     * or not there is a non-empty translation for it)
     * @param keyName - a hierarchical dot-separated key sequence 
     * @return list of locale names (language codes and pseudo-locale names)
     */
    public StringList whoHasKey(String keyName)
    {
        StringList result = new StringList();
        
        for (int i = 0; i < LOCALES_LIST.length; i++) {
            String localeName = LOCALES_LIST[i][0];
            if(getLocale(localeName).hasKey(keyName)) result.add(localeName);
        }
        
        return result;
    }
    
    /**
     * Registers the given {@code component} for translation service on
     * locale change and updates all components with the current Locale
     * (equivalent to {@code register(component, true)})
     * @param component - a translatable GUI {@link Component}
     * @see #register(Component, boolean)
     * @see #unregister(Component)
     */
    public void register(Component component)
    // START KGU#337 2017-02-03: Issue #340 - possibility needed to register without update
    {
    	register(component, true);
    }
    
    /**
     * Registers the given component for translation service on locale change.
     * @param component - a translatable GUI component
     * @param updateImmediately - {@code true} induces an immediate update of all
     *  subcomponents, {@code false} will postpone this to an explicit
     *  {@link #setLocale(Component)} event.
     * @see #register(Component)
     * @see #unregister(Component)
     */
    public void register(Component component, boolean updateImmediately)
    // END KGU#337 2017-02-03
    {
        // register a new component
        components.add(component);

        //System.out.println("Actual number of components: "+components.size());
        
        // set it the actual language, if possible
        //setLang(component);
        // START KGU#337 2017-02-03: Issue #340 - possibility needed to register without update
        //updateComponents();
        if (updateImmediately) {
        // update all components!
            updateComponents();
        }
        // END KGU#337 2017-02-03
        
    }
    
    /**
     * Removes the given component from the set of applicants for translation service 
     * @param component - the GUI component no longer to be translated on locale change
     */
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
    
    /**
     * Checks whether the proposed localeName is listed among LOCALES_LIST
     * @param localeName a supposed locale name
     * @return true iff localeName is among the configured locales
     */
    public static boolean isNamedLocale(String localeName)
    {
        boolean found = false;
        for (int i = 0; !found && i < LOCALES_LIST.length; i++)
        {
            found = LOCALES_LIST[i][0].equals(localeName);
        }
    	return found;
    }
    
    /**
     * Makes the locale named by localeName the current locale
     * and propagates it to all registered components
     * @param localeName - a language code, a pseudo locale name or a locale file name
     */
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
                loadedLocaleName = "en";
            }
        }
        
        if(!localeName.equals("preview") && !localeName.equals("external"))
            loadedLocaleFilename = loadedLocaleName+".txt";
        // update all registered components
        updateComponents();
    }
    
    /**
     * Translates the given component according to the current locale.
     * @param component - a GUI component
     */
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
    
    /**
     * Updates the "preview" locale from the given StringList, sets the locale
     * and propagates it
     * @param lines - the translation lines according to the locale file construction rules
     */
    public void setLocale(StringList lines)
    {
        loadedLocaleName="preview";
        Locale locale = getLocale(loadedLocaleName);
        // START KGU 2016-09-09: This seemed to be missing to make sense
        locale.parseStringList(lines);
        // END KGU 2016-09-09
        updateComponents();
    }

    /**
     * Performs the translation of the given component for the locale named by localeName
     * @param component - a GUI component
     * @param localeName - name of the locale (language code) or the locale file
     */
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
                    // START KGU#246 2016-09-13: Bugfix #241
                    //Method method = component.getClass().getMethod(fieldValue, new Class[]{});
                    //method.invoke(component, new Object[]{});
                    Method method = component.getClass().getMethod(fieldName.substring(0,  fieldName.length()-2), new Class[]{});
                    Object methodResult = method.invoke(component, new Object[]{});
                    if (methodResult instanceof String)
                    {
                    	fieldValue = (String)methodResult;
                    }
                    // END KGU#246 2016-09-13
                }
                catch(Exception e)
                {
                    // START KGU#246 2016-09-13: Bugfix #241 For NullPointerExceptions getMessage() may return null
                    //errorMessage = e.getMessage();
                    if ((errorMessage = e.getMessage()) == null)
                    {
                    	errorMessage = e.getClass().getSimpleName();
                    }
                    // END KGU#246 2016-09-13
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
                    // START KGU#484 2018-04-05: Issue #463
                    //e.printStackTrace();
                    logger.log(Level.WARNING, "Field access to "+ fieldName + " failed.", e);
                    // END KGU#484 2018-04-05
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
                        // START KGU#484 2018-04-05: Issue #463
                        //e.printStackTrace();
                        logger.log(Level.WARNING, "Component access failed.", e);
                        // END KGU#484 2018-04-05
                    }
                }
                if (field!=null)
                {
                    try
                    {
                        if (field.get(component) != null)
                            fieldValue = field.get(component).toString();
                    }
                    catch(Exception e)
                    {
                        errorMessage = e.getMessage();
                        // START KGU#484 2018-04-05: Issue #463
                        //e.printStackTrace();
                        logger.log(Level.WARNING, "Field access to "+ component + " failed.", e);
                        // END KGU#484 2018-04-05
                    }
                }
            }
            
            if(errorMessage!=null)
                logger.log(Level.WARNING, "CONDITION ({0}:{1}): {2}",
                        new Object[]{fieldName, value, errorMessage});

            if(fieldValue!=null)
                result &= value.trim().equalsIgnoreCase(fieldValue.trim());
        }
        
        return result;
    }
    
    
    /**
     * Performs the translation of the given component with the translation lines
     * passed in.
     * @param component - a GUI component
     * @param lines - the translation lines according to the locale file construction rules
     */
    // ----[ ATTENTION ]----
    // As this method might be called before a component is fully initialised,
    // i.e. before all contained components have been put there, we might get
    // null pointers. So deal with it! ;-)
    public void setLocale(Component component, StringList lines) {
//    	if (component instanceof LangMenuBar) {
//    		System.out.println("setLocale("+component+") called!");
//    		try {
//    			throw new Exception("test");
//    		}
//    		catch (Exception e) {
//    			e.printStackTrace();
//    		}
//    	}
        // The parts on both sides of the equality sign (compound key and string value)
        StringList parts;
        // The pieces of the split key (i.e. parts[0])
        StringList pieces;
        
        for (int i = 0; i < lines.count(); i++) {
            parts = StringList.explodeFirstOnly(lines.get(i), "=");
            //System.out.println(parts.get(0));
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
                if (key.contains("[") && key.endsWith("]"))
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
                    // START KGU#263 2016-09-28: Generally replace any found "\n" by a real newline
                    // START #479 2017-12-15: Enh. #492 - replace element names
                    //parts.set(1, parts.get(1).replace("\\n", "\n"));
                    parts.set(1, ElementNames.resolveElementNames(parts.get(1).replace("\\n", "\n"), null));
                    // END KGU#479 2017-12-15
                    // END KGU#263 2016-09-28

                    if (pieces.get(1).toLowerCase().equals("title")) {
                        if (component instanceof JDialog) {
                            ((JDialog) component).setTitle(parts.get(1));
                        }
                        // START KGU#324 2017-10-02: Enh. #415 JFrames should also be able to get the title localized
                        else if (component instanceof JFrame) {
                            ((JFrame) component).setTitle(parts.get(1));
                        }
                        // END KGU#324 2017-10-02
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
                            logger.log(Level.WARNING, "LANG: Error accessing element <{0}.{1}>!\n{}",
                                    new Object[]{pieces.get(0), pieces.get(1), errorMessage});
                        } else if (field != null) {
                            // END KGU#3 2015-11-03
                            try {

                                Class<?> fieldClass = field.getType();
                                String piece2 = pieces.get(2).toLowerCase();

                                Object target = field.get(component);

                                // START KGU#239 2016-08-12: Opportunity to localize an array of controls
                                if (fieldClass.isArray() && pieces.count() > 3)
                                {
                                    // On startup we might be faster here than the initialization of the components, such
                                    // that we must face nasty NullPointerExceptions if we don't prevent
                                    if (target != null) {
                                        int length = Array.getLength(target);
                                        // START KGU#252 2016-09-22: Issue #248 - workaround for Java 7
                                        //int index = Integer.parseUnsignedInt(piece2);
                                        //if (index < length) {
                                        // START KGU#351 2017-02-26
                                        //int index = Integer.parseInt(piece2);
                                        //if (index >= 0 && index < length) {
                                        //// END KGU#252 2016-09-22
                                        //    target = Array.get(target, index);
                                        //    fieldClass = target.getClass();
                                        //    pieces.remove(2);	// Index no longer needed
                                        //    pieces.set(1, pieces.get(1) + "[" + piece2 + "]");
                                        //    piece2 = pieces.get(2).toLowerCase();
                                        //}
                                        int ixStart = 0, ixEnd = 0;
                                        if (piece2.equals("*")) {
                                            // All indices!
                                            ixEnd = length;
                                        }
                                        else {
                                            ixStart = Integer.parseInt(piece2);
                                            ixEnd = ixStart + 1;
                                        }
                                        pieces.remove(2);	// Index no longer needed
                                        pieces.set(1, pieces.get(1) + "[" + piece2 + "]");
                                        piece2 = pieces.get(2).toLowerCase();
                                        if (ixStart >= 0 && ixEnd <= length) {
                                            String piece3 = (pieces.count()>3) ? pieces.get(3) : "0";
                                            for (int index = ixStart; index < ixEnd; index++) {
                                                Object tgt = Array.get(target, index);
                                                // START KGU#245 2018-07-02: New mechanism to insert the index into the text
                                                //this.setFieldProperty(tgt, tgt.getClass(), piece2, piece3, parts.get(1));
                                                this.setFieldProperty(tgt, tgt.getClass(), piece2, piece3, 
                                                        parts.get(1).replace("[#]", Integer.toString(index)));
                                                // END KGU#245 2018-07-02
                                            }
                                            // Target exhausted
                                            target = null;
                                        }
                                        // END KGU#351 2017-02-26
                                        // START KGU#252 2016-09-22: Issue #248 - workaround for Java 7
                                        else
                                        {
                                            logger.log(Level.WARNING,
                                                    "LANG: Error while setting property <{0}> for element <{1}.{2}.{3}>!\n"
                                                            + "Index out of range (0...{4})!",
                                                            new Object[]{pieces.get(3), pieces.get(0), pieces.get(1), piece2, length-1});
                                        }
                                        // END KGU#252 2016-09-22
                                    }
                                }
                                // END KGU#239 2016-08-12
                                // START KGU#242 2016-09-04
                                else if ((fieldClass.getName().equals("java.util.HashMap") || fieldClass.getName().equals("java.util.Hashtable")) && pieces.count() > 3)
                                {
                                    String piece1_2 = pieces.get(1) + "[" + piece2 + "]";
                                    Method method = fieldClass.getMethod("get", new Class[]{Object.class});
                                    // On startup we might be faster here than the initialization of the components, such
                                    // that we must face nasty NullPointerExceptions if we don't prevent
                                    if (target != null) {
                                        try {
                                            target = method.invoke(target, piece2);
                                            if (target == null)
                                            {
                                                logger.log(Level.WARNING, "LANG: No Element <{0}.{1}> found!",
                                                        new Object[]{pieces.get(0), piece1_2});
                                            }
                                        }
                                        catch (Exception e) {
                                            // FIXME: No idea why this always goes off just on startup
                                            logger.log(Level.WARNING, "LANG: Trouble accessing <{0}.{1}>",
                                                    new Object[]{pieces.get(0), piece1_2});
                                        }
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
                                
                                // START KGU#351 2017-02-26: Decomposition to allow index loops
//                                if (piece2.equals("text")) {
//                                    Method method = fieldClass.getMethod("setText", new Class[]{String.class});
//                                    if(target != null)
//                                        method.invoke(target, new Object[]{parts.get(1)});
//                                } else if (piece2.equals("tooltip")) {
//                                    Method method = fieldClass.getMethod("setToolTipText", new Class[]{String.class});
//                                    if(target != null)
//                                        method.invoke(target, new Object[]{parts.get(1)});
//                                } else if (piece2.equals("border")) {
//                                    Method method = fieldClass.getMethod("setBorder", new Class[]{Border.class});
//                                    if(target != null)
//                                        method.invoke(target, new Object[]{new TitledBorder(parts.get(1))});
//                                } else if (piece2.equals("tab")) {
//                                    Method method = fieldClass.getMethod("setTitleAt", new Class[]{int.class, String.class});
//                                    if(target != null)
//                                        method.invoke(target, new Object[]{Integer.valueOf(pieces.get(3)), parts.get(1)});
//                                } else if (piece2.equals("header")) {
//                                    Method method = fieldClass.getMethod("setHeaderTitle", new Class[]{int.class, String.class});
//                                    if(target != null)
//                                        method.invoke(target, new Object[]{Integer.valueOf(pieces.get(3)), parts.get(1)});
//                                } // START KGU#184 2016-04-24: Enh. #173 - new mnemonic support (only works from Java 1.7 on)
//                                else if (piece2.equals("mnemonic")) {
//                                    Method method = fieldClass.getMethod("setMnemonic", new Class[]{int.class});
//                                    // START KGU 2016-12-07: Issue #304 We must check the availability of a Java 1.7 method.
//                                    try {
//                                        int keyCode = KeyEvent.getExtendedKeyCodeForChar(parts.get(1).toLowerCase().charAt(0));
//                                        if (keyCode != KeyEvent.VK_UNDEFINED && target != null) {
//                                            method.invoke(target, new Object[]{Integer.valueOf(keyCode)});
//                                        }
//                                    } catch (NoSuchMethodError ex) {
//                                    	System.out.println("Locales: Mnemonic localization failed due to legacy JavaRE (at least 1.7 required).");
//                                    }
//                                    // END KGU 2016-12-07
//                                } // END KGU#184 2016-04-24
//                                // START KGU#156 2016-03-13: Enh. #124 - intended for JComboBoxes
//                                else if (piece2.equals("item")) {
//                                    // The JCombobox is supposed to be equipped with enum objects providing a setText() method
//                                    // (see lu.fisch.structorizer.elements.RuntimeDataPresentMode and
//                                    // lu.fisch.structorizer.executor.Control for an example).
//                                    Method method = fieldClass.getMethod("getItemAt", new Class[]{int.class});
//                                    if(target != null)
//                                    {
//                                        Object item = method.invoke(target, new Object[]{Integer.valueOf(pieces.get(3))});
//                                        if (item != null) {
//                                            Class<?> itemClass = item.getClass();
//                                            method = itemClass.getMethod("setText", new Class[]{String.class});
//                                            method.invoke(item, new Object[]{parts.get(1)});
//                                        }
//                                    }
//                                }
                                String piece3 = (pieces.count() > 3) ? pieces.get(3) : "0";
                                this.setFieldProperty(target, fieldClass, piece2, piece3, parts.get(1));
                                // END KGU#351 2017-02-26
                                // END KGU#156 2016-03-13
                            } catch (Exception e) {
                                String reason = e.getMessage();
                                if (reason == null) {
                                    reason = e.getClass().getSimpleName();
                                    // START KGU#484 2018-04-05: Issue #463
                                    //e.printStackTrace();
                                    logger.log(Level.WARNING, "", e);	// FIXME: really that important?
                                    // END KGU#484 2018-04-05
                                }
                                logger.log(Level.WARNING, "LANG: Error while setting property <{0}> for element <{1}>!\n",
                                        new Object[]{pieces.get(2), pieces.get(0), pieces.get(1), reason});
                            }
                        } else {
                            logger.log(Level.WARNING, "LANG: Field not found <{0}.{1}>",
                                    new Object[]{pieces.get(0), pieces.get(1)});
                        }
                    }
                }
            }
        }
    }
    
    // START KGU#351 2017-02-26: Outsourced from setLocale(Component, StringList)
    private void setFieldProperty(Object _target, Class<?> _fieldClass, String _property, String _piece3, String _text) throws Exception
    {
        if (_target == null) {
            return;
        }
        if (_property.equals("text")) {
            Method method = _fieldClass.getMethod("setText", new Class[]{String.class});
            method.invoke(_target, new Object[]{_text});
        } else if (_property.equals("tooltip")) {
            Method method = _fieldClass.getMethod("setToolTipText", new Class[]{String.class});
            // START KGU#736 2019-09-29: In case of contained newlines, try to convert the text to html.
            if (_text.contains("\n") && !_text.startsWith("<html>")) {
                _text = "<html>" + BString.encodeToHtml(_text).replace("\n", "<br/>") + "</html>";
            }
            // END KGU#736 2019-09-29
            method.invoke(_target, new Object[]{_text});
        } else if (_property.equals("border")) {
            Method method = _fieldClass.getMethod("setBorder", new Class[]{Border.class});
            method.invoke(_target, new Object[]{new TitledBorder(_text)});
        } else if (_property.equals("tab")) {
            Method method = _fieldClass.getMethod("setTitleAt", new Class[]{int.class, String.class});
            method.invoke(_target, new Object[]{Integer.valueOf(_piece3), _text});
        } else if (_property.equals("header")) {
            Method method = _fieldClass.getMethod("setHeaderTitle", new Class[]{int.class, String.class});
            method.invoke(_target, new Object[]{Integer.valueOf(_piece3), _text});
        } // START KGU#184 2016-04-24: Enh. #173 - new mnemonic support (only works from Java 1.7 on)
        else if (_property.equals("mnemonic")) {
            Method method = _fieldClass.getMethod("setMnemonic", new Class[]{int.class});
            // START KGU 2016-12-07: Issue #304 We must check the availability of a Java 1.7 method.
            try {
                int keyCode = KeyEvent.getExtendedKeyCodeForChar(_text.toLowerCase().charAt(0));
                if (keyCode != KeyEvent.VK_UNDEFINED) {
                    method.invoke(_target, new Object[]{Integer.valueOf(keyCode)});
                    // START KGU#713 2019-06-14: Issue #728 Allow to position the mnemonic in the caption
                    String pos = _text.substring(1);	// an index may follow, e.g in "g12"
                    if (!pos.isEmpty()) {
                        method = _fieldClass.getMethod("setDisplayedMnemonicIndex", new Class[]{int.class});
                        method.invoke(_target, new Object[]{Integer.valueOf(pos)});
                    }
                    // END KGU#713 2019-06-14
                }
            } catch (NoSuchMethodError ex) {
            	logger.warning("Locales: Mnemonic localization failed due to legacy JavaRE (at least 1.7 required).");
            }
            // END KGU 2016-12-07
        } // END KGU#184 2016-04-24
        // START KGU#156 2016-03-13: Enh. #124 - intended for JComboBoxes
        else if (_property.equals("item")) {
            // The JCombobox is supposed to be equipped with enum objects providing a setText() method
            // (see lu.fisch.structorizer.elements.RuntimeDataPresentMode and
            // lu.fisch.structorizer.executor.Control for an example).
            Method method = _fieldClass.getMethod("getItemAt", new Class[]{int.class});
            Object item = method.invoke(_target, new Object[]{Integer.valueOf(_piece3)});
            if (item != null) {
                Class<?> itemClass = item.getClass();
                method = itemClass.getMethod("setText", new Class[]{String.class});
                method.invoke(item, new Object[]{_text});
            }
        }
        // END KGU#156 2016-03-13
    	
    }
    // END KGU#351 2017-02-26
    
    /**
     * Returns the name of the current locale
     * @return language code or pseudo locale name (or default locale name)
     */
    public String getLoadedLocaleName()
    {
        if (loadedLocaleName==null) return DEFAULT_LOCALE;
        else return loadedLocaleName;
    }
    
    /**
     * Returns the file name of locale most recently loaded from file
     * @return a text file name
     */
    public String getLoadedLocaleFilename()
    {
        if(loadedLocaleFilename==null) return DEFAULT_LOCALE + ".txt";
        else return loadedLocaleFilename;
    }
    
    /**
     * Updates the "external" (pseudo) locale with the translation lines passed in,
     * makes it the current locale and propagates it
     * @param lines - translation lines according to the locale file construction rules
     * @param filename - the name (path) of the originating text file
     */
    public void setExternal(StringList lines, String filename)
    {
        getLocale("external").parseStringList(lines);
        setLocale("external");
        loadedLocaleFilename=filename;
    }
}

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

import java.util.ArrayList;
import java.util.HashMap;
import lu.fisch.utils.StringList;

/**
 *
 * @author robertfisch
 */
public class Locales {
    // LOCALES_LIST of all locales we have
    public static final String[] LOCALES_LIST = {"chs","cht","cz","de","en","es","fr","it","lu","nl","pl","pt_br","ru","empty"};
    
    // the "default" oder "master" locale
    public static final String DEFAULT_LOCALE = "en";
    
    // structure were all data is being loaded to
    private final HashMap<String,Locale> locales = new HashMap<String,Locale>();
    
    public static void main(String[] args)
    {
        Locales locales = new Locales();

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

    public Locales() 
    {
        for (int i = 0; i < LOCALES_LIST.length; i++) {
            String name = LOCALES_LIST[i];
            locales.put(name, new Locale(name+".txt"));
        }
    }
    
    public Locale getDefaultLocale()
    {
        return locales.get(DEFAULT_LOCALE);
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
        return locales.get(name);
    }
    
    public StringList whoHasKey(String keyName)
    {
        StringList result = new StringList();
        
        for (int i = 0; i < LOCALES_LIST.length; i++) {
            String localeName = LOCALES_LIST[i];
            if(getLocale(localeName).hasKey(keyName)) result.add(localeName);
        }
        
        return result;
    }
    
}

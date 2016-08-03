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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.JOptionPane;
import lu.fisch.utils.StringList;

/**
 *
 * @author robertfisch
 */
public class Locale {
    public static final String endOfHEader         = ">>>";
    public static final String startOfSection      = "----->";
    public static final String startOfSubSection   = "-----[";
    
    
    private StringList header = new StringList();
    private final LinkedHashMap<String,StringList> sections = new LinkedHashMap<String,StringList>();
    
    private String filename;
    
    public static void main(String[] args)
    {
        Locale locale = new Locale("en.txt");
        System.out.println(locale.getText());
        
        System.out.println("Sections:");
        for (int i = 0; i < locale.getSectionNames().length; i++) {
            String sectionName = locale.getSectionNames()[i];
            System.out.println("- "+sectionName);
        }
    }
    
    public Locale loadCopyFromFile()
    {
        return new Locale(filename);
    }
    
    /**
     * create a locale based on a given file
     * @param _langfile     the file to be loaded
     */
    public Locale(String _langfile)
    {
        filename = _langfile;
        
        // read the file from the compiled application into a string
        String input = new String();
        try 
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/lu/fisch/structorizer/locales/"+_langfile), "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) 
            {
                input+=str+"\n";
            }
            in.close();
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(null,  _langfile+": Error while loading language file\n"+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // convert the data to a stringlist
        StringList lines = new StringList();
        lines.setText(input); 
        
        // extract & remove the header
        while(!lines.get(0).trim().equals(endOfHEader) && lines.count()>0) 
        {
            header.add(lines.get(0));
            lines.remove(0);
        }
        
        if(lines.count()==0)
            JOptionPane.showMessageDialog(null, _langfile+": File is empty after removing the header ...", "Error", JOptionPane.ERROR_MESSAGE);
        
        // remove the endOfHEader marker
        lines.remove(0);
        
        StringList section = null;
        for(int i=0; i<lines.count(); i++)
        {
            String line = lines.get(i).trim();
            
            // we found a section
            if(line.startsWith(startOfSection)) 
            {
                section = new StringList();
                // get the name
                line = line.replace(startOfSection, "").trim();
                // create a new one
                sections.put(line, section);
            }
            // no special case
            else 
            {
                if(section!=null)
                {
                    // add the content
                    section.add(line);
                }
                else if(!line.trim().isEmpty())
                {
                    JOptionPane.showMessageDialog(null, _langfile+": Found a sub-section ("+line+") before a section!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    public ArrayList<String> getKeys(String sectionName)
    {
        ArrayList<String> keys = new ArrayList<String>();
        
        StringList section = sections.get(sectionName);
        
        if(section==null)
        {
            return new ArrayList<String>();
        }
        
        for (int i = 0; i < section.count(); i++) {
            String line = section.get(i);
            StringList parts = StringList.explodeFirstOnly(line.trim(),"=");
            if(line.trim().contains("=") && parts.get(0).contains(".") && !parts.get(0).startsWith("//"))
            {
                keys.add(parts.get(0));
            }
        }
        return keys;
    }
    
    public boolean hasKey(String keyName)
    {
        String[] sectioNames = getSectionNames();
        for (int i = 0; i < sectioNames.length; i++) {
            String sectioName = sectioNames[i];
            if(getKeys(sectioName).contains(keyName)) return true;
        }
        return false;
    }
    
    public boolean hasValuePresent(String keyName)
    {
        String[] sectionNames = getSectionNames();
        
        for (int i = 0; i < sectionNames.length; i++) {
            String sectionName = sectionNames[i];
            
            StringList section = sections.get(sectionName);

            for (int s = 0; s < section.count(); s++) {
                String line = section.get(s);
                StringList parts = StringList.explodeFirstOnly(line.trim(),"=");
                if(line.trim().contains("=") && parts.get(0).contains(".") && !parts.get(0).startsWith("//"))
                {
                    if(parts.get(0).equals(keyName))
                    {
                        if(parts.get(1).trim().isEmpty())
                            return false;
                        else
                            return true;
                    }
                }
            }
        }
        return false;
    }
    
    public ArrayList<String> getKeyValues(String sectionName)
    {
        ArrayList<String> keys = new ArrayList<String>();
        
        StringList section = sections.get(sectionName);
        
        for (int i = 0; i < section.count(); i++) {
            String line = section.get(i);
            StringList parts = StringList.explodeFirstOnly(line.trim(),"=");
            if(
                    (line.trim().contains("=") && parts.get(0).contains(".") && !parts.get(0).startsWith("//"))
                    || line.startsWith(Locale.startOfSubSection)
                    
              )
            {
                keys.add(line);
            }
        }
        return keys;
    }

    public String getValue(String sectionName, String key)
    {
        StringList section = sections.get(sectionName);
        
        if(section==null) return "";
        
        for (int i = 0; i < section.count(); i++) {
            String line = section.get(i);
            StringList parts = StringList.explodeFirstOnly(line.trim(),"=");
            if(line.trim().contains("=") && 
                    parts.get(0).contains(".") && 
                    !parts.get(0).startsWith("//") &&
                    parts.get(0).equals(key)
                    )
            {
                return parts.get(1);
            }
        }
        return "";
    }
    
    public boolean setValue(String sectionName, String key, String value)
    {
        StringList section = sections.get(sectionName);
        
        for (int i = 0; i < section.count(); i++) {
            String line = section.get(i);
            StringList parts = StringList.explodeFirstOnly(line.trim(),"=");
            if(line.trim().contains("=") && 
                    parts.get(0).contains(".") && 
                    !parts.get(0).startsWith("//") &&
                    parts.get(0).equals(key)
                    )
            {
                section.set(i, key+"="+value);
                return true;
            }
        }
        return false;
    }
    
    
    public String getText()
    {
        String data = new String();
        data += header.getText().trim();
        data += "\n"+endOfHEader+"\n";
        String[] sectionNames = getSectionNames();
        for (int i = 0; i < sectionNames.length; i++) {
            String name = sectionNames[i];
            data +=startOfSection+" "+name+"\n";
            data +=getSection(name).getText().trim()+"\n"+"\n";
        }
        return data;
    }

    public StringList getHeader() {
        return header;
    }

    public void setHeader(StringList header) {
        this.header = header;
    }

    public String[] getSectionNames() {
        return sections.keySet().toArray(new String[sections.size()]);
    }
    
    public StringList getSection(String name)
    {
        return sections.get(name);
    }
    
    
}

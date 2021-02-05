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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

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
    
	// START KGU 2018-03-21
	public static final Logger logger = Logger.getLogger(Locale.class.getName());
	// END KGU 2018-03-21
    // START KGU#231 2016-08-04: #220 
    public boolean hasUnsavedChanges = false;
    // END KGU#231 2016-08-04
    // START KGU#231 2016-08-09: #220 
    public StringList cachedHeader = new StringList();
    // END KGU#231 2016-08-09
    // START KGU#244 2016-09-06: Needed for a session with loaded user language file
    public String cachedFilename = null;
    // END KGU#244 2016-09-06
    public final LinkedHashMap<String,LinkedHashMap<String,String>> values = new LinkedHashMap<String,LinkedHashMap<String,String>>();
    
    public static void main(String[] args)
    {
        Locale locale = new Locale("en.txt");
        logger.info(locale.getText());
        
        StringBuilder sectNames = new StringBuilder();
        sectNames.append("Sections:");
        String[] sectionNames = locale.getSectionNames();
        for (int i = 0; i < sectionNames.length; i++) {
            String sectionName = sectionNames[i];
            sectNames.append("\n- " + sectionName);
        }
        logger.info(sectNames.toString());
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
        
        // "preview" and "external" as special cases, so we don't need to load
        // a file then
        if (!_langfile.equals("external.txt") && !_langfile.equals("preview.txt")) 
        {
            logger.info("Loading now locale: "+_langfile);        

            // read the file from the compiled application into a string
            String input = new String();
            try 
            {
            	// START KGU#244 2016-09-06: Allow temporary unregistered locales from arbitrary files
                //BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/lu/fisch/structorizer/locales/"+_langfile), "UTF-8"));
            	File file = new File(_langfile);
            	InputStreamReader isr = null;
            	if (file.isAbsolute() && file.canRead())
            	{
            		isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            	}
            	else
            	{
            		isr = new InputStreamReader(this.getClass().getResourceAsStream("/lu/fisch/structorizer/locales/"+_langfile), "UTF-8");
            	}
            	BufferedReader in = new BufferedReader(isr);
                // END KGU#244 2016-09-06
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

            parseStringList(lines);
        }
    }
    
    public void parseStringList(StringList lines)
    {
       // extract & remove the header
        while (!lines.get(0).trim().equals(endOfHEader) && lines.count() > 0) 
        {
            header.add(lines.get(0));
            lines.remove(0);
        }
        
        if (lines.count() == 0)
            JOptionPane.showMessageDialog(null, filename+": File is empty after removing the header ...", "Error", JOptionPane.ERROR_MESSAGE);
        
        // remove the endOfHEader marker
        lines.remove(0);

        // parse the body
        parseBody(lines);
    }
    
    private void parseBody(StringList lines)
    {
        // clear all sections
        sections.clear();
        
        // go ahead and parse the input
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
                    JOptionPane.showMessageDialog(null, filename+": Found a sub-section ("+line+") before a section!", "Error", JOptionPane.ERROR_MESSAGE);
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
        String[] sectionNames = getSectionNames();
        for (int i = 0; i < sectionNames.length; i++) {
            String sectionName = sectionNames[i];
            if(getKeys(sectionName).contains(keyName)) return true;
        }
        return false;
    }
    
    public boolean hasValuePresent(String keyName)
    {
        String[] sectionNames = getSectionNames();
        
        for (int i = 0; i < sectionNames.length; i++) {
            // START KGU#231 2016-08-08: Issue #220 Code unification
            //String sectionName = sectionNames[i];
            //
            //StringList section = sections.get(sectionName);

            //for (int s = 0; s < section.count(); s++) {
            //    String line = section.get(s);
            //    StringList parts = StringList.explodeFirstOnly(line.trim(),"=");
            //    if(line.trim().contains("=") && parts.get(0).contains(".") && !parts.get(0).startsWith("//"))
            //    {
            //        if(parts.get(0).equals(keyName))
            //        {
            //            if(parts.get(1).trim().isEmpty())
            //                return false;
            //            else
            //                return true;
            //        }
            //    }
            //}
        	String value = getValueIfPresent(sectionNames[i], keyName);
        	if (value != null)
        	{
        		return !(value).trim().isEmpty();
        	}
            // END KGU#231 2016-08-08
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

    // START KGU#231 2016-08-08: Issue #220 - Unifies retrieval
    private String getValueIfPresent(String sectionName, String key)
    {
        StringList section = sections.get(sectionName);
        
        if (section==null) return null;
        
        for (int i = 0; i < section.count(); i++) {
            String line = section.get(i);
            StringList parts = StringList.explodeFirstOnly(line.trim(),"=");
            if (line.trim().contains("=") && 
                    parts.get(0).contains(".") && 
                    !parts.get(0).startsWith("//") &&
                    parts.get(0).equals(key)
                    )
            {
                return parts.get(1);
            }
        }
        return null;
    }
    
    public boolean valueDiffersFrom(String key, String value)
    {
        String[] sectionNames = getSectionNames();
        
        for (int i = 0; i < sectionNames.length; i++) {
        	String val = getValueIfPresent(sectionNames[i], key);
        	if (val != null)
        	{
        		return !(val.equals(value));
        	}
            // END KGU 2016-08-08
        }
        return value != null && !value.isEmpty();
    }
    // END KGU#231 2016-08-08
    
    /**
     * Retrieves the string value for the given {@code key} in section {@code sectionName}.
     * If there is no such key or the value isn't specified then an empty string will be
     * returned.<br/>
     * <b>Note:</b> Possible conditions and index placeholders will <b>not</b> be resolved,
     * i.e. they are interpreted as constant parts of the key here, e.g.
     * {@code "InputBox.title[getInsertionType():insert]"}, not just {@code "InputBox.title"}.
     * @param sectionName - name of the interesting section
     * @param key - the key sequence (with all conditional adapters, as is)
     * @return a string associated to the given key or ""
     */
    public String getValue(String sectionName, String key)
    {
        // START KGU#231 2016-08-08: Issue #220 - Reduced to new internal method
        //StringList section = sections.get(sectionName);
        //
        //if(section==null) return "";
        //
        //for (int i = 0; i < section.count(); i++) {
        //    String line = section.get(i);
        //    StringList parts = StringList.explodeFirstOnly(line.trim(),"=");
        //    if(line.trim().contains("=") && 
        //            parts.get(0).contains(".") && 
        //            !parts.get(0).startsWith("//") &&
        //            parts.get(0).equals(key)
        //            )
        //    {
        //        return parts.get(1);
        //    }
        //}
        String value = getValueIfPresent(sectionName, key);
        if (value != null) return value;
        // END KGU#231 2016-08-08
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

    /*public StringList getBody() {
        return body;
    }/**/

    public StringList getBody() {
        StringList data = new StringList();
        String[] sectionNames = getSectionNames();
        for (int i = 0; i < sectionNames.length; i++) {
            String name = sectionNames[i];
            data.add(startOfSection+" "+name);
            data.add(getSection(name));
        }
        
        return data;
    }
    
    public void setBody(StringList body) {
        parseBody(body);
    }
    
    // START KGU#231 2016-08-09: Issue #220
    public boolean hasCachedChanges()
    {
        return !this.values.isEmpty() || !this.cachedHeader.isEmpty();
    }
    // END KGU#231 2016-08-09
    
    // START KGU#244 2016-09-06: Allow loading from external text files
    public String getFilename()
    {
        return filename + "";
    }
    // END KGU#244 2016-09-06
}

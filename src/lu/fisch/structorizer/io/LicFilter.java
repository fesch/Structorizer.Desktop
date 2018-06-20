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
package lu.fisch.structorizer.io;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    File name filter for license files.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.13      First Issue (for enh.req. #372)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.io.File;
import java.io.FilenameFilter;


/**
 * File name filer for Structorizer license files
 * @author Kay Gürtzig
 */
public class LicFilter implements FilenameFilter {

	public static boolean isLic(String _filename)
	{
		return _filename.startsWith(getNamePrefix()) && acceptedExtension().equals(getExtension(_filename));
	}
	
	public static String getNamePrefix()
	{
		return "lic";
	}
	
	public static String acceptedExtension()
	{
		return "txt";
	}
	
	public static String getExtension(String s) 
	{
		String ext = "";
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	public static String getExtension(File f) 
	{
		return getExtension(f.getName());
	}
	
    public boolean accept(File dir, String fileName) {
        return isLic(fileName);
    }
}

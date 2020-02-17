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

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Inputfilter for structogram files.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.15      First Issue
 *      Kay Gürtzig     2018.06.08      Inheritance changed
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************
 */

import java.io.File;

public class StructogramFilter extends ExtFileFilter {

	public static boolean isNSD(String _filename)
	{
		return getExtension(_filename).equals("nsd");
	}
	
//	public static String getExtension(String s) 
//	{
//		String ext = null;
//		int i = s.lastIndexOf('.');
//		
//		if (i > 0 &&  i < s.length() - 1) 
//		{
//			ext = s.substring(i+1).toLowerCase();
//		}
//		return ext;
//	}
//	
//	public static String getExtension(File f) 
//	{
//		String ext = null;
//		String s = f.getName();
//		int i = s.lastIndexOf('.');
//		
//		if (i > 0 &&  i < s.length() - 1) 
//		{
//			ext = s.substring(i+1).toLowerCase();
//		}
//
//		return ext;
//	}
	
	public String getDescription() 
	{
        return "NSD Files";
    }
	
	public boolean accept(File f) 
	{
		if (f.isDirectory()) 
		{
			return true;
		}
		
//        String extension = getExtension(f);
//        if (extension != null) 
//		{
		return isNSD(f.getName());
//		}
//		
//        return false;
    }
	
}

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
 *      Description:    Input filter for Arranger files.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2015-12-20      First Issue
 *      Kay Gürtzig     2018-06-08      Inheritance changed
 *      Kay Gürtzig     2020-02-16      Description revised
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.io.File;

/**
 * Input filter for arrangement list files (related to {@link Arranger})
 * @author Kay Gürtzig
 * @see ArrZipFilter
 * @see ArrangerFilter
 */
public class ArrFilter extends ExtFileFilter {

	public static boolean isArr(String _filename)
	{
		return (getExtension(_filename).equals("arr"));
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
//		return getExtension(f.getName());
//	}

	public String getDescription() 
	{
		// START KGU#802 2020-02-16: Issue #815
		//return "Arranger Files";
		return "Arrangement List Files";
		// END KGU#802 2020-02-16
	}

	public boolean accept(File f) 
	{
		if (f.isDirectory()) 
		{
			return true;
		}

//		String extension = getExtension(f);
//		if (extension != null) 
//		{
			return isArr(f.getName());
//		}
//
//		return false;
	}

}

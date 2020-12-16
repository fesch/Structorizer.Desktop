/*
    TurtleBox
    A module providing a simple turtle graphics for Java

    Copyright (C) 2009, 2020  Bob Fisch

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

package lu.fisch.turtle.io;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Abstract extended FileFilter class with file extension support
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-12-13      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      getExtension() ensures a non-null return value (in case of a missing extension,
 *      an empty string will be returned)
 *
 ******************************************************************************************************///

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * This is an extended abstract subclass of {@link javax.swing.filechooser.FileFilter}
 * providing two basic static methods for file name extension extraction and implementing
 * default version of {@link #accept(File)} based on the extensions proposed by the new
 * abstract method {@link #getAcceptedExtensions()}.
 * @author Kay Gürtzig
 */
public abstract class ExtFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) 
		{
			return true;
		}
		String ext = getExtension(f);
		String[] accExts = getAcceptedExtensions();
		for (int i = 0; i < accExts.length; i++) {
			if (ext.equals(accExts[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts the file name extension of the given file path {@code s}
	 * @param s - a file path as string
	 * @return the extension as string or {@code ""} if there isn't any.
	 * @see #getExtension(File)
	 */
	public static String getExtension(String s) 
	{
		String ext = "";
		// We must face cases where entire paths might be passed in
		s = (new File(s)).getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	/**
	 * Extracts the file name extension of the given file {@code f}
	 * @param f - a {@link File} object
	 * @return the extension as string or {@code ""} if there isn't any.
	 * @see #getExtension(String)
	 */
	public static String getExtension(File f) 
	{
		return getExtension(f.getName());
	}

	/**
	 * @return an array of accepted file name extensions (without dot and
	 * preferably in lower case)
	 */
	public abstract String[] getAcceptedExtensions();

}

/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
 *      Description:    Input filter for both structogram and arrangement files for Structorizer.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2020-02-16      First version for issue #815
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      It was too inconvenient always having to switch among nsd, arr, and arrz filter
 *
 ******************************************************************************************************///

import java.io.File;

/**
 * Input filter for both structogram and arrangement files for Structorizer
 * @author Kay Gürtzig
 */
public class StructorizerFilter extends ExtFileFilter {

	public static boolean isStructorizerFile(String _filename)
	{
		String ext = getExtension(_filename);
		return ext.equals("nsd") || ext.equals("arr") || ext.equals("arrz");
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) 
		{
			return true;
		}
		return isStructorizerFile(f.getName());
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return "All Structorizer Files (NSD, Arrangements)";
	}

}

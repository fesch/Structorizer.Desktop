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
*      Description:    File filter for XML files of SVG type.
*
******************************************************************************************************
*
*      Revision List
*
*      Author          Date            Description
*      ------          ----            -----------
*      Kay Gürtzig     2020-12-13      First Issue for TurtleBox
*
******************************************************************************************************
*
*      Comment:
*      Inspired by lu.fisch.io.PNGFilter
*
******************************************************************************************************///

/**
* File filter for image files of SVG type
* @author Kay Gürtzig
*/
public class SVGFilter extends ExtFileFilter {

	@Override
	public String[] getAcceptedExtensions() {
		return new String[] {"svg"};
	}

	@Override
	public String getDescription() {
		return "SVG files";
	}

}

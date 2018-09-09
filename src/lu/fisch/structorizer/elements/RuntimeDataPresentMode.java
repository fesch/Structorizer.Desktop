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

package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Enumeration type for runtime data presentation (element highlighting).
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Kay Gürtzig     2016.03.12      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      - / -
 *
 ******************************************************************************************************///

public enum RuntimeDataPresentMode {
	NONE("no coloring"),
	SHALLOWCOVERAGE("shallow test coverage"),
	DEEPCOVERAGE("deep test coverage"),
	EXECCOUNTS("execution counts"),
	EXECSTEPS_LIN("done operations, lin."),
	EXECSTEPS_LOG("done operations, logar."),
	TOTALSTEPS_LIN("total operations, lin."),
	TOTALSTEPS_LOG("total operations, logar.");
	
	private String text;
	private RuntimeDataPresentMode(String _caption)
	{
		text = _caption;
	}
	public String toString()
	{
		return text;
	}
	public void setText(String _caption)
	{
		text = _caption;
	}
}

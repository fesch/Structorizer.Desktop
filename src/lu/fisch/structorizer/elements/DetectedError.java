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
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents an "FOR loop" in a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008.04.16      First Issue
 *		
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

public class DetectedError 
{
		private String error = new String();
		private Element element = null;
		
		// Contructor
		public DetectedError(String _error, Element _ele)
		{
			error=_error;
			element=_ele;
		}
		
		// Getter
		public String getError()
		{
			return error;
		}
		
		public Element getElement()
		{
			return element;
		}
		
		// transformers
		public String toString()
		{
			if(element!=null)
			{
				error=error.replaceAll("«", "\u00AB");
				error=error.replaceAll("»", "\u00BB");
				return error;
				//return element.getClass().getSimpleName()+" «"+element.getText().get(0)+"»: "+error;
			}
			else
			{
				return "No error?";
			}
		}
		
		// tester
		public boolean equals(DetectedError _error)
		{
			return (element==_error.getElement()) && (error.equals(_error.getError()));
		}
}

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

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents an Element-related Analyser issue for the error list.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008.04.16      First Issue
 *		Kay Gürtzig     2016-07-27      Enh. #207: New general substitutions to support warnings introduced
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************
 */

public class DetectedError 
{
		private String error = new String();
		private Element element = null;
		
		// Constructor
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
			// START KGU#220 2016-07-27: Enh. #207 - allow general warnings
			//if(element!=null)
			// END KGU#220 2016-07-27
			{
				error = error.replaceAll("«", "\u00AB");
				error = error.replaceAll("»", "\u00BB");
				// START KGU#220 2016-07-27: Enh. #207 - allow general warnings
				error = error.replace("--->", "\u2192");
				error = error.replace("<-", "\u2190");
				// END KGU#220 2016-07-27
				return error;
				//return element.getClass().getSimpleName()+" «"+element.getText().get(0)+"»: "+error;
			}
			// START KGU#220 2016-07-27: Enh. #207 - allow general warnings
			//else
			//{
			//	return "No error?";
			//}
			// END KGU#220 2016-07-27
		}
		
		// tester
		public boolean equals(DetectedError _error)
		{
			return (element==_error.getElement()) && (error.equals(_error.getError()));
		}
}

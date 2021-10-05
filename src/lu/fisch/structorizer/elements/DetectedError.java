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
 *      Bob Fisch       2008-04-16      First Issue
 *      Kay Gürtzig     2016-07-27      Enh. #207: New general substitutions to support warnings introduced
 *      Kay Gürtzig     2021-01-06/09   Enh. #905: New field to tell hints from errors/warnings, field
 *                                      error renamed to message, new method getMessage() introduced
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************
 */

/**
 * This class represents an Element-related Analyser issue for the error list
 * or warning pop-up.
 * @author Bob Fisch
 */
public class DetectedError 
{
		private String message = new String();
		private Element element = null;
		// START KGU#906 2021-01-06: Enh. #905: Tell tutorial hints from errors and warnings
		private boolean isError = true;
		// END KGU#906 2021-01-06
		
		// Constructor
		/**
		 * Creates a true error or warning entry with the given text {@code _error}
		 * associated to Element {@code _ele}
		 * @param _error - the error or warning message
		 * @param _ele - the affected {@link Element} (or {@code null} for general
		 * warnings)
		 */
		public DetectedError(String _error, Element _ele)
		{
			message = _error;
			element = _ele;
		}

		// START KGU#906 2021-01-06: Enh. #905 - distinguish hints from other stuff
		/**
		 * Creates an entry for an error, a warning or a hint with the given text
		 * {@code _error} associated to Element {@code _ele}
		 * @param _error - the error or warning message
		 * @param _ele - the affected {@link Element} (or {@code null} for general
		 * warnings)
		 * @param _isWarning - whether the entry is a true warning (related to an error
		 * or mode, otherwise it would be treated as a mere tutorial hint)
		 */
		public DetectedError(String _error, Element _ele, boolean _isWarning)
		{
			message = _error;
			element = _ele;
			isError = _isWarning;
		}
		// END KGU#906 2021-01-06

		// Getter
		// START KGU#906 2021-01-09 Enh. #905 Method getError() replaced by getMessage()
		/**
		 * @deprecated Use {@link #getMessage()} instead
		 */
		public String getError()
		{
			return getMessage();
		}
		/**
		 * @return the associated (usually localized) description
		 */
		public String getMessage()
		{
			return message;
		}
		// END KGU#906 2021-01-09
		
		public Element getElement()
		{
			return element;
		}
		
		// START KGU#906 2021-01-06: Enh. #905
		/**
		 * @return {@code true} if this is a regular Analyser complaint, 
		 * {@code false} in case of a tutorial hint
		 */
		public boolean isWarning()
		{
			return isError;
		}
		// END KGU#906 2021-01-06
		
		// transformers
		@Override
		public String toString()
		{
			// START KGU#220 2016-07-27: Enh. #207 - allow general warnings
			//if(element!=null)
			// END KGU#220 2016-07-27
			{
				message = message.replaceAll("«", "\u00AB");
				message = message.replaceAll("»", "\u00BB");
				// START KGU#220 2016-07-27: Enh. #207 - allow general warnings
				message = message.replace("--->", "\u2192");
				message = message.replace("<-", "\u2190");
				// END KGU#220 2016-07-27
				return message;
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
		/**
		 * Compares the messages of {@code this} and {@code _error}.
		 * @param _error - another instance of this class
		 * @return {@code true} iff both messages are equal
		 */
		public boolean equals(DetectedError _error)
		{
			// FIXME: Neither element reference nor warning classification are compared!
			return (element == _error.getElement()) && (message.equals(_error.getMessage()));
		}
}

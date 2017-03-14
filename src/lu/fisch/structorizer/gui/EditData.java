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

package lu.fisch.structorizer.gui;

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This is a simple data class, defining what data the edit dialog
 *						of an element is returning.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.29      First Issue
 *      Kay Gürtzig     2015.10.12      Field for breakpoint control added (KGU#43)
 *      Kay Gürtzig     2015.10.25      Enh. #10: Fields for specific For loop support (KGU#3)
 *      Kay Gürtzig     2016.03.21      Enh. #84: Field modification to support FOR-IN loops (KGU#61)
 *      Kay Gürtzig     2016.08.01      Enh. #215: Breakpoint trigger counters added (KGU#213)
 *      Kay Gürtzig     2016.10.13      Enh. #270: Disabling control added (KGU#277)
 *      Kay Gürtzig     2017.03.14      Enh. #372: Additional Root fields (author, license)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************
 */

import lu.fisch.structorizer.elements.For;
import lu.fisch.utils.*;

public class EditData {

	public String title = new String();

	public StringList text = new StringList();
	public StringList comment = new StringList();
	// START KGU#3 2015-10-25: Enh. #10 - specific FOR loop support
	public StringList forParts = new StringList();
	// START KGU#61 2016-03-21: Enh. #84 - we have to distinguish three styles now
	//public boolean forPartsConsistent = false;
	public For.ForLoopStyle forLoopStyle = For.ForLoopStyle.FREETEXT;
	// END KGU#61 2016-03-21
	// END KGU#3 2015-10-25
	
	// START KGU#43 2015-10-12
	public boolean breakpoint = false;
	// END KGU#43 2015-10-12
	// START KGU#213 2016-08-01: Enh. #215
	public int breakTriggerCount = 0;
	// END KGU#213 2016-08-01
	// START KGU#277 2016-10-13: Enh #270
	public boolean disabled = false;
	// END KGU#277 2016-10-13
	// START KGU#363 2017-03-14: Enh. #372 Author name and license infor for Root
	public String authorName = null;
	public String licenseName = null;
	public String licenseText = null;
	// END KGU#363 2017-03-14
	
	public boolean result = false;
	
}

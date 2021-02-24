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
 *      Description:    Helper class raised from inside class LicenseEditor to support isue #372
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.05.19      First Issue (for Enh. requ. #372)
 *      Kay Gürtzig     2017.05.22      Attribute origin added
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      Objects of this class are used both for editing and in undo mechanism
 *
 ******************************************************************************************************///

/**
 * @author Kay Gürtzig
 * Helper structure for the communication between classes
 * {@link lu.fisch.structorizer.gui.Diagram},
 * {@link lu.fisch.structorizer.gui.InputBoxRoot},
 * {@link lu.fisch.structorizer.gui.AttributeInspector},
 * {@link lu.fisch.structorizer.gui.LicensEditor}, and for undo actions
 */
public class RootAttributes {

	public Root root = null;
	public String authorName = null;
	public String licenseName = null;
	public String licenseText = null;
	// START KGU#363 2017-05-22: Enh. #372
	public String origin = null; 
	// END KGU#363 2017-05-22
	// START KGU#408 2021-02-22: Enh. #410
	public String namespace = null;
	// END KGU#408 2021-02-22


	/**
	 * Creates an instance with empty data (null)
	 */
	public RootAttributes() {
	}

	/**
	 * Creates a instance with the given texts as data 
	 */
	public RootAttributes(Root _root) {
		this.root = _root;
		this.authorName = _root.getAuthor();
		if (_root.licenseName !=null) {
			this.licenseName = _root.licenseName + "";
		}
		if (_root.licenseText != null) {
			this.licenseText = _root.licenseText + "";
		}
		// START KGU#363 2017-05-22: Enh. #372
		this.origin = _root.origin;
		// END KGU#363 2017-05-22
		// START KGU#408 2021-02-22: Enh. #410
		this.namespace = _root.getNamespace();
		// END KGU#408 2021-02-22
	}
	
	/**
	 * Returns a (shallow) copy of this' contents
	 * @return the copied data
	 */
	public RootAttributes copy()
	{
		RootAttributes info = new RootAttributes();
		info.root = this.root;
		info.authorName = this.authorName;
		info.licenseName = this.licenseName;
		info.licenseText = this.licenseText;
		// START KGU#363 2017-05-22: Enh. #372
		info.origin = this.origin;
		// END KGU#363 2017-05-22
		// START KGU#408 2021-02-22: Enh. #410
		info.namespace = this.namespace;
		// END KGU#408 2021-02-22
		return info;
	}

}

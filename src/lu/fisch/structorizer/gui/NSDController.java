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

import javax.swing.JFrame;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This is a special interface I use to refresh all the speedbuttons as
 *						well as different menu entries. The main application class is in charge
 *						of dispatching the "doButtons" method to all subclasses' "doButtonsLocal".
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-28      First Issue
 *      Kay Gürtzig     2020-06-03      Issue #868: New API isRestricted()
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

public interface NSDController
{

	/**
	 * Updates the visibility or accessibility of the buttons held e.g. in the button bar,
	 * menus etc. depending on the current state.
	 */
	public void doButtons();
	/**
	 * Updates the visibility or accessibility of the local buttons, menu items etc.
	 */
	public void doButtonsLocal();

	public void updateColors();

	public void setLookAndFeel(String _laf);
	public String getLookAndFeel();

	public void savePreferences();

	public JFrame getFrame();

	public void loadFromINI();

	// START KGU#868 2020-06-03: Bugfix #868
	/**
	 * @return {@code true} if code export and import are suppressed
	 */
	public boolean isRestricted();
	// END KGU#868 2020-06-03
}

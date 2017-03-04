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

package lu.fisch.structorizer.locales;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    JPanel subclass with Structorizer locale support 
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date        Description
 *      ------          ----        -----------
 *      Bob Fisch       2016.08.02  First Issue
 *      Kay Gürtzig     2016.02.03  Issue #340: registration without immediate update launch
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 *
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class LangPanel extends JPanel {

    public LangPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        // START KGU#337 2017-02-03: Issue #340 - register without immediate update
        //Locales.getInstance().register(this);
        Locales.getInstance().register(this, false);
        // END KGU#337 2017-02-03
    }

    public LangPanel(LayoutManager layout) {
        super(layout);
        // START KGU#337 2017-02-03: Issue #340 - register without immediate update
        //Locales.getInstance().register(this);
        Locales.getInstance().register(this, false);
        // END KGU#337 2017-02-03
    }

    public LangPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        // START KGU#337 2017-02-03: Issue #340 - register without immediate update
        //Locales.getInstance().register(this);
        Locales.getInstance().register(this, false);
        // END KGU#337 2017-02-03
    }

    public LangPanel() {
        // START KGU#337 2017-02-03: Issue #340 - register without immediate update
        //Locales.getInstance().register(this);
        Locales.getInstance().register(this, false);
        // END KGU#337 2017-02-03
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b); 
        Locales.getInstance().setLocale(LangPanel.this);
    }
    
}

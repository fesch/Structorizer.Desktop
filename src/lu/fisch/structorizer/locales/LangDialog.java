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

/*
 ******************************************************************************************************
 *
 *     Author: Bob Fisch
 *
 *     Description: This class extends a "JDialog" to support language settings
 *
 ******************************************************************************************************
 *
 *     Revision List
 *
 *     Author       Date        Description
 *     ------       ----        -----------
 *     Bob Fisch    2008.01.14  First Issue
 *     Bob Fisch    2016.08.02  Fundamentally redesigned
 *     Kay Gürtzig  2016.09.21  API enhanced (initLang(), adjustLangDependentComponents()) to facilitate bugfix #241
 *     Kay Gürtzig  2016.11.11  Issue #81: Method scaleToggleIcon added (DPI awareness workaround)
 *     Kay Gürtzig  2017.01.09  Issue #81/#330: Method scaleToggleButton moved to class GUIScaler 
 *
 ******************************************************************************************************
 *
 * Comment:	/
 *
 *****************************************************************************************************
 */

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import lu.fisch.structorizer.gui.IconLoader;

/**
 * Extends JDialog to facilitate language localization, also provides static
 * methods applicable to other GUI classes not inheriting from LangDialog.
 *
 * @author Robert Fisch
 *
 */
@SuppressWarnings("serial")
public class LangDialog extends JDialog {
    
    private boolean packing = true;

    private static JFrame dummyFrame = new JFrame();

    public LangDialog() {
        super(dummyFrame);
        dummyFrame.setIconImage(IconLoader.getIcon(0).getImage());
        this.repaint();
        initLang();
    }

    public LangDialog(Frame owner) {
        super(owner);
        initLang();
    }

    public LangDialog(Dialog owner) {
        super(owner);
        initLang();
    }

    public LangDialog(Frame owner, boolean modal) {
        super(owner, modal);
        initLang();
    }

    private void initLang() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                Locales.getInstance().setLocale(LangDialog.this);
                // START KGU#246 2016-09-21: Bugfix #241 Needed for subclassable adjustment
                adjustLangDependentComponents();
                // END KGU#246 2016-09-21
                // repack the dialog to possibly extend it to the new strings
                if (LangDialog.this.packing) LangDialog.this.pack();
            }
        });
    }
    
    public boolean isPacking() {
        return packing;
    }

    public void setPacking(boolean doPacking) {
        this.packing = doPacking;
    }

    // START KGU#246 2016-09-21: Enhancement to implement issues like bugfix #241
    /**
     * This method is called on opening after setLocale and before re-packing
     * and allows subclasses to adjust components that may require so after the
     * translation of captions and texts. 
     */
    protected void adjustLangDependentComponents()
    {
    	// MAY BE OVERRIDDEN BY SUBCLASSES
    }
    // END KGU#246 2016-09-21
    
}

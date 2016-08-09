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

/**
 * ****************************************************************************************************
 *
 * Author: Bob Fisch
 *
 * Description: This class extends a "JDialog" to support language settings
 *
 ******************************************************************************************************
 *
 * Revision List
 *
 * Author Date	Description ------	----	----------- Bob Fisch 2008.01.14 First
 * Issue Kay Gürtzig 2015.10.14 Hook for customizable class-specific translation
 * activities added Kay Gürtzig 2016.03.13 KGU#156: Support for JComboBox added
 * on occasion of enhancement #124 Kay Gürtzig 2016.07.03 KGU#203: File
 * conversion to StringList now skips comments and empty lines Bob Fisch
 * 2016.08.02 Bugfix #218: equality signs in translations mutilated them Kay
 * Gürtzig 2016.08.03 Inheritance changed (ILangDialog added)
 *
 ******************************************************************************************************
 *
 * Comment:	/
 *
 *****************************************************************************************************
 *///
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
        dummyFrame.setIconImage(IconLoader.ico074.getImage());
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
                // repack the dialog to possibly extend it to the new strings
                if(LangDialog.this.packing) LangDialog.this.pack();
            }
        });
    }
    
    public boolean isPacking() {
        return packing;
    }

    public void setPacking(boolean doPacking) {
        this.packing = doPacking;
    }

    
}

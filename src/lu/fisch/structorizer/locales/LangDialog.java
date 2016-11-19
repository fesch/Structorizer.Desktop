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
 *     Kay Gürtzig  2016.11.11  Issue #81: Method scaleCheckBoxIcon added (DPI awareness workaround) 
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
import java.awt.image.BufferedImage;

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
                // START KGU#246 2016-09-21: Bugfix #241 Needed for subclassable adjustment
                adjustLangDependentComponents();
                // END KGU#246 2016-09-21
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
    
    // START KGU#287 2016-11-11: Issue #81 (DPI-awareness workaround)
    /**
     * Returns a scaled checkbox icon for JCheckbox checkbox in mode selected
     * @param checkbox - the checkbox the icon is recested for
     * @param selected - the kind of icon (true = selected, false = unselected)
     * @return an ImageIcon scaled to the font size of checkbox
     */
    protected static ImageIcon scaleToggleIcon(JToggleButton checkbox, boolean selected)
    {
        boolean prevState = checkbox.isSelected();
        FontMetrics boxFontMetrics = checkbox.getFontMetrics(checkbox.getFont());
        Icon boxIcon = selected ? checkbox.getSelectedIcon() : checkbox.getIcon();
        if (boxIcon == null) {
            checkbox.setSelected(selected);
            String propertyName = "CheckBox.icon";
            if (checkbox instanceof JRadioButton) {
            	propertyName = "RadioButton.icon";
            }
            boxIcon = UIManager.getIcon(propertyName);
        }
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage boxImage = new BufferedImage(
                boxIcon.getIconWidth(), boxIcon.getIconHeight(), type);
        Graphics2D g2 = boxImage.createGraphics();
        try {
            boxIcon.paintIcon(checkbox, g2, 0, 0);
        }
        finally {
            g2.dispose();
        }
        ImageIcon newBoxIcon = new ImageIcon(boxImage);
        Image finalBoxImage = newBoxIcon.getImage().getScaledInstance(
                boxFontMetrics.getHeight(), boxFontMetrics.getHeight(), Image.SCALE_SMOOTH
                );
        checkbox.setSelected(prevState);
        return new ImageIcon(finalBoxImage);
    }
    // END KGU#287 2016-11-11

}

/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch
    Copyright (C) 2016  Kay Gürtzig

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

/*******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    A special class rendering the cells of a Root list.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016-12-12      First Issue
 *      Kay Gürtzig     2017-01-07      Enh. #319 - "covered" status now shown by the icons instead of the text
 *      Kay Gürtzig     2017-05-16      Enh. #389: new icons for includable roots.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      Introduced for a first implementation of enhancement #305
 *
 *      2019-01-02 Kay Gürtzig
 *      Now replaced by Editor.ArrangerIndexCellRenderer
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;

/**
 * Special {@link ListCellRenderer} subclass rendering the cells of a {@link Root} list.
 * @author Kay Gürtzig
 */

@SuppressWarnings("serial")
class RootListCellRenderer extends JLabel implements ListCellRenderer<Root>{
    private final static ImageIcon mainIcon = IconLoader.getIcon(22);
    private final static ImageIcon subIcon = IconLoader.getIcon(21);
    // START KGU#318 2017-01-07: Enh. #319: Better than text colouring for test-covered diagrams
    private final static ImageIcon subIconCovered = IconLoader.getIcon(30);
    // END KGU#318 2017-01-07
    // START KGU#318/KGU#376 2017-04-29: Enh. #319, #389: show coverage status of (imported) main diagrams
    private final static ImageIcon mainIconCovered = IconLoader.getIcon(70);
    // END KGU#318/KGU#376 2017-04-29
    // START KGU#318/KGU#376 2017-05-16: Enh. #319, #389: show coverage status of (imported) main diagrams
    private final static ImageIcon inclIcon = IconLoader.getIcon(71);
    private final static ImageIcon inclIconCovered = IconLoader.getIcon(72);
    // END KGU#318/KGU#376 2017-05-16
    private final static Color selectedBackgroundNimbus = new Color(57,105,138);

    @Override
    public Component getListCellRendererComponent(JList<? extends Root> list, Root root, int index, boolean isSelected,
            boolean cellHasFocus) {
        String s = root.getSignatureString(true);
        boolean covered = Element.E_COLLECTRUNTIMEDATA && root.deeplyCovered; 
        setText(s);
        // START KGU#318/KGU#376 2017-04-29: Enh. #319, #389: show coverage status of (imported) main diagrams
        //setIcon((root.isProgram) ? mainIcon : (covered ? subIconCovered : subIcon));
        if (root.isProgram()) {
        	setIcon(covered ? mainIconCovered : mainIcon);
        }
        else if (root.isSubroutine()) {
        	setIcon(covered ? subIconCovered : subIcon);
        }
        // START KGU#376 2017-05-16: Enh. #389
        else if (root.isInclude()) {
        	setIcon(covered ? inclIconCovered : inclIcon);
        }
        // END KGU#376 2017-05-16
        // END KGU#318/KGU#376 2017-04-29
        if (isSelected) {
            if (UIManager.getLookAndFeel().getName().equals("Nimbus"))
            {
                // Again, a specific handling for Nimbus was necessary in order to show any difference at all.
                if (list.isFocusOwner()) {
                    setBackground(selectedBackgroundNimbus);
                    setForeground(Color.WHITE);
                }
                else {
                    setBackground(Color.WHITE);	
                    setForeground(selectedBackgroundNimbus);
                }
            }
            else {
                if (list.isFocusOwner()) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                }
                else {
                    // Invert the selection colours
                    setBackground(list.getSelectionForeground());
                    setForeground(list.getSelectionBackground());    				
                }
            }
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }

}

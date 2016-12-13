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

import java.awt.Color;

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
 *      Kay Gürtzig     12.12.2016      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      Introduced for a first implementation of enhancement #305
 *
 ******************************************************************************************************///

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import lu.fisch.structorizer.elements.Root;

/**
 * @author Kay Gürtzig
 *
 */

@SuppressWarnings("serial")
class RootListCellRenderer extends JLabel implements ListCellRenderer<Root>{
    private final static ImageIcon mainIcon = IconLoader.ico022;
    private final static ImageIcon subIcon = IconLoader.ico021;
    private final static Color selectedBackgroundNimbus = new Color(57,105,138);

	@Override
	public Component getListCellRendererComponent(JList<? extends Root> list, Root root, int index, boolean isSelected,
			boolean cellHasFocus) {
        String s = root.getSignatureString(true);
        setText(s);
        setIcon((root.isProgram) ? mainIcon : subIcon);
        if (isSelected) {
    		if (UIManager.getLookAndFeel().getName().equals("Nimbus"))
    		{
    			// Again, a specific handling for Nimbus was necessary in order to show any difference at all.
    			setBackground(selectedBackgroundNimbus);
    			setForeground(Color.WHITE);
    		}
    		else {
                setBackground(list.getSelectionBackground()/*Color.BLUE*/);
                setForeground(list.getSelectionForeground()/*Color.WHITE*/);    			
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

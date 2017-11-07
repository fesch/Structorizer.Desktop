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

import java.awt.Component;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JToolBar;

/**
 *
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class MyToolbar extends JToolBar
{
    private boolean visible = true;
    // START KGU#456 2017-11-05: Enh. #452
    private Vector<Component> expertMembers = new Vector<Component>();
    
    public Component add(Component comp, boolean indispensable)
    {
    	if (!indispensable) {
    		expertMembers.add(comp);
    	}
    	return this.add(comp);
    }
    
    public void setExpertVisibility(boolean visible)
    {
    	for (Component comp: expertMembers) {
    		comp.setVisible(visible);
    	}
    }
    // END KGU#456 2017-11-05

    // FIXME (KGU): Why were the following two methods overridden in the first place?
    /* (non-Javadoc)
     * @see java.awt.Component#isVisible()
     */
    @Override
    public boolean isVisible()
    {
        return visible;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible)
    {
        this.visible = visible;
        // START KGU#456 2017-11-05: Enh. #452
        synchronized (this.getTreeLock()) {
        	for (int i = 0; i < this.getComponentCount(); i++) {
        		this.getComponent(i).setVisible(visible);
        	}
        }
        // END KGU#456 2017-11-05
    }

    
}

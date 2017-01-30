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
        Locales.getInstance().register(this);
    }

    public LangPanel(LayoutManager layout) {
        super(layout);
        Locales.getInstance().register(this);
    }

    public LangPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        Locales.getInstance().register(this);
    }

    public LangPanel() {
        Locales.getInstance().register(this);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b); 
        Locales.getInstance().setLocale(LangPanel.this);
    }
    
}

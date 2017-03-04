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

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 *
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class LangFrame extends JFrame {

    public LangFrame() throws HeadlessException {
        initLang();
    }

    public LangFrame(GraphicsConfiguration gc) {
        super(gc);
        initLang();
    }

    public LangFrame(String title) throws HeadlessException {
        super(title);
        initLang();
    }

    public LangFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        initLang();
    }
    
    
    private void initLang()
    {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                Locales.getInstance().register(LangFrame.this);
            }
        });        
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b); 
        Locales.getInstance().setLocale(LangFrame.this);
    }
    
}

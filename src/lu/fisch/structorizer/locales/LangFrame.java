/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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

/*******************************************************************************************************
*
*      Author:         Bob Fisch
*
*      Description:    A Locale-aware JFrame subclass.
*
******************************************************************************************************
*
*      Revision List
*
*      Author          Date            Description
*      ------          ----            -----------
*      Bob Fisch       2016-08-08      First Issue for the new Locale system (issue #220)
*      Kay Gürtzig     2019-01-08      Issue #664: Method isApplicationMain() introduced (KGU#631).
*
******************************************************************************************************
*
*      Comment:
*
******************************************************************************************************///

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

/**
 * A Locale-aware JFrame subclass registering as windowOpened listener with the {@link Locales}
 * instance and translating all components on being set visible. 
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
    
    // START KGU#631 2019-01-08: Issue #664 We need a handy way to decide whther he application is closing
    /**
     * Subclasses that can represent the main class (and thread) of an application should
     * override (re-implement) this method to return true.<br/>
     * Relevant for the {@link WindowListener#windowClosing()} event.
     * @return true if this object represents the running application. Default is false.
     */
    public boolean isApplicationMain()
    {
        return false;
    }
    // END KGU#631 2019-01-08
    
}

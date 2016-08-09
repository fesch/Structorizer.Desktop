/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

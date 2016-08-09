/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lu.fisch.structorizer.locales;

import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 *
 * @author robertfisch
 */
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

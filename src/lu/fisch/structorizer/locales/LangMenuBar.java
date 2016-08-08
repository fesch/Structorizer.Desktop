/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lu.fisch.structorizer.locales;

import javax.swing.JMenuBar;

/**
 *
 * @author robertfisch
 */
public class LangMenuBar extends JMenuBar {

    public LangMenuBar() {
        super();
        Locales.getInstance().register(this);
    }
    
}

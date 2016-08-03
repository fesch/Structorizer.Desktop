/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lu.fisch.structorizer.locales;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author robertfisch
 */
public class TranslatorTableModel extends DefaultTableModel {

    @Override
    public boolean isCellEditable(int row, int column){  
        if(column==2)
            return true;
        else
            return false;  
    }

}

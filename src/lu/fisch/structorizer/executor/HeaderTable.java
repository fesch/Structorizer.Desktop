/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lu.fisch.structorizer.executor;

import javax.swing.JTable;

/**
 *
 * @author robertfisch
 */
public class HeaderTable extends JTable {
    public void setHeaderTitle(int column, String title)
    {
        this.getColumnModel().getColumn(column).setHeaderValue(title);
        this.getTableHeader().repaint();
    }
}

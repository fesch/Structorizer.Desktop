/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lu.fisch.structorizer.locales;

import java.util.HashSet;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class TranslatorTableModel extends DefaultTableModel {

	// START KGU 2016-08-04: Issue #220
	private final Set<Integer> forbiddenRows = new HashSet<Integer>();
	/**
	 * Disables editing in given row 
	 */
	public void forbidRowEditable(int row)
	{
		forbiddenRows.add(row);
	}
	// END KGU 2016-08-04
    @Override
    public boolean isCellEditable(int row, int column){  
        return (column==2) && !forbiddenRows.contains(row);  
    }

}

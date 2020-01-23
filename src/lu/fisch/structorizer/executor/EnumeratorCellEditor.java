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
package lu.fisch.structorizer.executor;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Cell editor class for JTable cells that may contain a String or a ComboBox.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-11-21      First Issue (for enh. #739)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Specific table cell editor for the value column in the variable display, capable
 * of handling both JComboBoxes for enumerator variables and ordinary strings
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class EnumeratorCellEditor extends DefaultCellEditor {

	private JComboBox<?> combo = null;
	
	/**
	 * @param textField
	 */
	public EnumeratorCellEditor() {
		super(new JTextField());
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param comboBox
	 */
	public EnumeratorCellEditor(JComboBox<?> comboBox) {
		super(comboBox);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Object getCellEditorValue() {
		if (this.combo != null) {
			return this.combo;
		}
		return super.getCellEditorValue();
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (value instanceof JComboBox) {
			this.combo = (JComboBox<?>)value;
			
			if (isSelected) {
				combo.setBackground(table.getSelectionBackground());
			} else {
				combo.setBackground(table.getSelectionForeground());
			}

			return combo;
		}
		else  {
			this.combo = null;
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}
 
}

package lu.fisch.structorizer.executor;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;

/******************************************************************************************************
 *
 *      Author:         kay
 *
 *      Description:    Specific cell editor for pulldown buttons in Control or ValuePresenter
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.10.31      Replaces former Control.ButtonEditor
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

/**
 * Specific table cell editor for the pulldown buttons in the variable display
 * @author Kay Gürtzig
 */
public class PulldownButtonCellEditor extends DefaultCellEditor {
	protected JButton button;
	private JTable table;

	public PulldownButtonCellEditor() {
		super(new javax.swing.JCheckBox());
	}

	public Component getTableCellEditorComponent(JTable _table, Object _value,
			boolean _isSelected, int _row, int _column) {
		if (_value instanceof JButton) {
			table = _table;
			button = (JButton)_value;
//			if (isSelected) {
			// Show it in selected colour as soon as pressed.
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
//			} else {
//				button.setForeground(table.getForeground());
//				button.setBackground(table.getBackground());
//			}
		}
		else {
			button = null;
		}
		return button;
	}

	public Object getCellEditorValue() {
		return button;
	}

	public boolean stopCellEditing() {
		if (button != null && table != null) {
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
		}
		return super.stopCellEditing();
	}

	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}

}

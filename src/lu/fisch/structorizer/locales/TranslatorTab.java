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

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents a Translator tab (for editing of a locale file section).
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2016-08-01      First Issue
 *      Kay Gürtzig     2016-08-04      Issue #220: Subsection header rows shouldn't be editable
 *      Kay Gürtzig     2016-08-08      Issue #220: Detect any substantial modification in a cell
 *      Kay Gürtzig     2016-09-06      KGU#244: Opportunity to reload a saved language file to resume editing it
 *                                      Cell renderer shall highlight also deleted texts as modifications
 *      Kay Gürtzig     2017-12-12      Enh. #491: Tooltip for long master texts (otherwise not completely readable)
 *      Kay Gürtzig     2019-06-06      Enh. #726: Fourth column with pull-down buttons for launching more powerful editor
 *      Kay Gürtzig     2021-05-11      Enh. #972: Row filtering prepared
 *      Kay Gürtzig     2024-04-21      Renamed Tab -> TranslatorTab
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import lu.fisch.structorizer.gui.IconLoader;

/**
 * This class represents tabs for the {@link Translator}, the main component is a
 * three-column {@link JTable} showing key sequences in the first column, English
 * master text in the central column, and the locale-bound translation in the third
 * column.
 * @author Robert Fisch
 */
@SuppressWarnings("serial")
public class TranslatorTab extends javax.swing.JPanel {

    /**
     * Creates new form Tab
     */
    public TranslatorTab() {
        initComponents();
        
        // configure the table
        table.setDefaultRenderer(Object.class, new BoardTableCellRendererTT());
        table.setRowHeight(25);
        
        table.setModel(new TranslatorTableModel());
        DefaultTableModel model = ((DefaultTableModel)table.getModel());
        // START KGU#709 2019-06-06: Issue #726
        //model.setColumnCount(3);
        model.setColumnCount(4);
        TableColumn col3 = table.getColumnModel().getColumn(3);
        col3.setHeaderValue(" ");
        int pulldownWidth = IconLoader.getIcon(80).getIconWidth();
        col3.setCellEditor(new BoardButtonEditorTT());
        table.getColumnModel().getColumn(3).setMaxWidth(pulldownWidth);
        table.getColumnModel().getColumn(3).setPreferredWidth(pulldownWidth);
        // END KGU#709 2019-06-06
        model.setRowCount(0);
        table.getColumnModel().getColumn(0).setHeaderValue("String");
        
        // START KGU#972 2021-05-11: Enh. #972 Prepare filtering
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);	// This is simply to allow the addition of a filter
        // We must not allow manual sorting as this would mix up sections
        for (int col = 0; col < model.getColumnCount(); col++) {
            sorter.setSortable(col, false);
        }
        // END KGU#972 2021-05-11
        table.getColumnModel().getColumn(2).setHeaderValue("Please load a language!");
        table.getTableHeader().repaint();
    }
    
    public JTable getTable()
    {
        return table;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        
        jScrollPane1.setViewportView(table);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}


/**
 * Specific table cell editor for the pulldown buttons in the Translator Tabs
 * @author Kay Gürtzig
 */
// FIXME: This code is identical with PullDownButtonCellEditor
@SuppressWarnings("serial")
class BoardButtonEditorTT extends DefaultCellEditor {
	protected JButton button;
	private JTable table;

	public BoardButtonEditorTT() {
		super(new javax.swing.JCheckBox());
	}

	public Component getTableCellEditorComponent(JTable _table, Object _value,
			boolean _isSelected, int _row, int _column) {
		if (_value instanceof JButton) {
			table = _table;
			button = (JButton)_value;
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
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
// END KGU#709 2019-06-06

class BoardTableCellRendererTT extends DefaultTableCellRenderer {

    Color backgroundColor = getBackground();

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        // START KGU#709 2019-06-06: Issue #726 - Better editing support for long messages
        if (value instanceof JButton) {
            return (JButton)value;
        }
        // END KGU#709 2019-06-06
        TableModel model = table.getModel();
        // START KGU#972 2021-05-11: Enh. #972 Be aware of filtering!
        row = table.convertRowIndexToModel(row);
        // END KGU#972 2021-05-11
        String key = (String) model.getValueAt(row, 0);
        
        if (key!=null && key.startsWith(Locale.startOfSubSection))
        {
            // START KGU 2016-08-04: Issue #220
            if (model instanceof TranslatorTableModel)
            {
                ((TranslatorTableModel) model).forbidRowEditable(row);
            }
            // END KGU 2016-08-04
            if (!isSelected)
                c.setBackground(Color.cyan);
            else
                c.setBackground(Color.blue);
        }
        else if ((value instanceof String && ((String) value).equals("")) || (value==null))
        {
            // START KGU#244 2016-09-06: Show an explicit deletion as well
            boolean isDeleted = col == 2 && Translator.loadedLocale.valueDiffersFrom(key, (String)value);
            // END KGU#244 2016-09-06
            if (!isSelected)
                //c.setBackground(Color.orange);
                c.setBackground(isDeleted ? Color.red : Color.orange);
            else
                c.setBackground(Color.yellow);
        }
        // START KGU#231 2016-08-08: Issue #220 - detect any substantial change
        //else if(col==2 && !Translator.loadedLocale.hasValuePresent(key))
        else if (col==2 && Translator.loadedLocale.valueDiffersFrom(key, (String)value))
        // END KGU#231 2016-08-08
        {
            if(!isSelected)
                c.setBackground(Color.green);
            else
                c.setBackground(Color.green.darker());
        }
        else if (!isSelected) 
        {
            c.setBackground(backgroundColor);
        }
        // START KGU#481 2017-12-12: Enh. #491 - long texts in the central column couldn't read completely
        if (col == 1 && value instanceof String) {
            int length = c.getPreferredSize().width; 
            int width = table.getColumnModel().getColumn(1).getWidth();
            if (length > width) {
                ((JLabel)c).setToolTipText((String)value);
            }
            else {
                ((JLabel)c).setToolTipText(null);
            }
        }
        // END KGU#481 2017-12-12
        return c;
    }
    
}

//@SuppressWarnings("serial")
//class MyRenderer extends DefaultTableCellRenderer {
//
//    Color backgroundColor = getBackground();
//
//    @Override
//    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
//    {
//        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//        DefaultTableModel model = (DefaultTableModel) table.getModel();
//        return c;
//    }
//}
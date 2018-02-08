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
package lu.fisch.structorizer.executor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Abstract class for all Elements.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      kay       31.10.2017      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import bsh.EvalError;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.utils.StringList;

/**
 * Recursive sub-editor for the inspection and modification of components in structured data (arrays and
 * records)
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class ValuePresenter extends JDialog implements ActionListener, WindowListener, PropertyChangeListener {

	javax.swing.JScrollPane pnlTable = new javax.swing.JScrollPane();
	javax.swing.JPanel pnlButtons = new javax.swing.JPanel();    	
	JButton btnCommit = new JButton(Control.lbOk.getText());
	JButton btnDiscard = new JButton(Control.lbDiscard.getText());
	// START KGU#147 2017-10-31: Enh. #84 - force a given pause button if requested
	JButton extButton = null;	// A button passed in from the caller
	// END KGU#147 2017-10-31
	JTable tblFields = new JTable();
	
	/** Ought to be either an {@link ArrayList} or a {@link HashMap} */
	private HashMap<String, Object> record = null;
	private ArrayList<Object> array = null;
	private String[] oldValStrings = null;
	private HashMap<Integer, String> editedLines = new HashMap<Integer, String>();
	private boolean editable = false;
    // START KGU#443 2017-10-31: Enh. #439 Apply this recursively
	private AbstractCellEditor activeBtnEditor; 
    private java.awt.event.ActionListener pulldownActionListener = new java.awt.event.ActionListener(){
    	@Override
    	public void actionPerformed(ActionEvent evt) {
    		btnPullDownActionPerformed(evt);
    	}
    };
    // END KGU#443 2017-10-31

    private class MyCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
            // START KGU#443 2017-10-16: Enh. #439 pulldown button for compound values
            if (value instanceof JButton) {
            	return (JButton)value;
            }
            // END KGU#443 2017-10-16
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    // START KGU#443 2017-10-16: Enh. #439 pulldown button for compound values
    /**
     * Specific table cell editor for the pulldown buttons in the variable display
     * @author Kay Gürtzig
     */
    public class ButtonEditor extends DefaultCellEditor {
    	protected JButton button;
    	private JTable table;

    	public ButtonEditor() {
    		super(new javax.swing.JCheckBox());
    	}

    	public Component getTableCellEditorComponent(JTable _table, Object _value,
    			boolean _isSelected, int _row, int _column) {
    		if (_value instanceof JButton) {
    			table = _table;
    			button = (JButton)_value;
    			//        			if (isSelected) {
    			button.setForeground(table.getSelectionForeground());
    			button.setBackground(table.getSelectionBackground());
    			//        			} else {
    			//        				button.setForeground(table.getForeground());
    			//        				button.setBackground(table.getBackground());
    			//        			}
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

	
	/**
	 * Sets up a recursive ValuePresenter for the compound value {@code _value}.
	 * @param _title - The title string to be presented (will be prefix for substructure titles!)
	 * @param _value - the object to be presented: Should be a {@link HashMap} or an {@link ArrayList}.
	 * @param _editable - whether editing of components or elements is allowed.
	 * @param _addButton - an additional button to be placed (ignored if {@code _editable is true}.
	 */
	@SuppressWarnings("unchecked")
	public ValuePresenter(String _title, Object _value, boolean _editable, JButton _addButton)
	{
		this.setTitle(_title);
		if (_value instanceof ArrayList<?>) {
			this.array = (ArrayList<Object>)_value;
		}
		else if (_value instanceof HashMap<?,?>) {
			this.record = (HashMap<String, Object>)_value;
		}
		this.editable = _editable;
		// START KGU#147 2017-10-31: Enh. #84
		if (!this.editable && _addButton != null) {
			this.extButton = _addButton;
		}
		// END KGU#14 2017-10-31
		initComponents();
	}
	
	private void initComponents()
	{
		tblFields.setGridColor(Color.LIGHT_GRAY);
		tblFields.setShowGrid(true);
		if (this.editable) {
			// Use pencil symbol if editable
			this.setIconImage(IconLoader.getIcon(84).getImage());
		}
		else {
			// Use a magnifying glass if not editable
			this.setIconImage(IconLoader.getIcon(83).getImage());    			
		}
		this.getContentPane().setLayout(new BorderLayout());
		pnlButtons.setLayout(new java.awt.GridLayout(0, 2));
		btnCommit.addActionListener(this);
		btnDiscard.addActionListener(this);
		String header0 = "";
		if (record != null) {
			header0 = Control.ttlCompName.getText();
		}
		else {
			header0 = Control.ttlIndex.getText();
		}
		tblFields.setModel(new javax.swing.table.DefaultTableModel(
				new Object [][] {
				},
				new String [] {
						header0, " ", Control.ttlContent.getText()
				}
				) {
			Class<?>[] types = new Class<?> [] {
				java.lang.String.class, JButton.class, java.lang.Object.class
			};

			public Class<?> getColumnClass(int columnIndex) {
				return types [columnIndex];
			}
			// Disable editing of the first column
			@Override
			public boolean isCellEditable(int row, int column){  
				return (column == 1 || editable && column > 1);  
			}
		});
		DefaultTableModel tm =(DefaultTableModel)tblFields.getModel();
		if (array != null) {
			oldValStrings = new String[array.size()];
			for (int i = 0; i < array.size(); i++)
			{
				Object[] rowData = {"[" + i + "]", null,
						oldValStrings[i] = Executor.prepareValueForDisplay(array.get(i))};
				tm.addRow(rowData);    				
			}
		}
		else if (record != null) {
			oldValStrings = new String[record.size()];	// May be an element too large; better than an exception...
			int i = 0;
			for (Entry<String, Object> entry: record.entrySet())
			{
				if (!entry.getKey().startsWith("§")) {
					Object[] rowData = {entry.getKey(), null,
							oldValStrings[i++] = Executor.prepareValueForDisplay(entry.getValue())};
					tm.addRow(rowData);
				}
			}
		}
        // START KGU#443 2017-10-16: Enh. #439 - pulldown buttons near compound values
		ImageIcon pulldownIcon = IconLoader.getIcon(80);
        for (int i = 0; i < tm.getRowCount(); i++) {
        	String value = (String)tm.getValueAt(i, 2);
            String name = (String)tm.getValueAt(i, 0);
            if (value.endsWith("}")) {
            	JButton pulldown = new JButton();
            	pulldown.setName(name);
            	pulldown.setIcon(pulldownIcon);
            	pulldown.addActionListener(this.pulldownActionListener);
            	tm.setValueAt(pulldown, i, 1);
            }
        }
        // END KGU#443 2017-10-16
        // START KGU#443 2017-10-16: Enh. #439
        int pulldownWidth = IconLoader.getIcon(80).getIconWidth();
        tblFields.getColumnModel().getColumn(1).setCellEditor(new PulldownButtonCellEditor());
        tblFields.getColumnModel().getColumn(1).setMaxWidth(pulldownWidth);
        tblFields.getColumnModel().getColumn(1).setPreferredWidth(pulldownWidth);
        // END KGU#443 2017-10-16
		optimizeColumnWidth(tblFields, 0);
		tblFields.addPropertyChangeListener("tableCellEditor", this);
		tblFields.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		tblFields.setRowHeight((int)(tblFields.getRowHeight() * Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"))));
		pnlTable.setViewportView(tblFields);
		if (this.editable) {
			btnDiscard.setEnabled(false);
			pnlButtons.add(btnDiscard);
		}
		else {
			if (this.extButton != null) {
				// This is not the Discard button but the forced external button.
				pnlButtons.add(extButton);
			}
			else {
				pnlButtons.add(new javax.swing.JLabel(""));
			}
			btnCommit.setText(Control.lbOk.getText());
		}
		pnlButtons.add(btnCommit);
		this.getContentPane().add(pnlTable, BorderLayout.CENTER);
		this.getContentPane().add(pnlButtons, BorderLayout.SOUTH);
		this.addWindowListener(this);
        // We must do this as late as possible, otherwise "Nimbus" tends to ignore this
        tblFields.setDefaultRenderer(Object.class, new MyCellRenderer());
		pack();
	}
	
	// START KGU#443 2017-10-16: Enh. #439 - Reserve the maximum space for last column
	/**
	 * Determines the required maximum rendering width for column {@code _colNo} of
	 * {@link JTable} {@code _table} and fixes it as maximum and preferred width 
	 * @param _table - the {@link JTable} to be optimized
	 * @param _colNo - index of the intersting column (typically 0)
	 * @return the determined width 
	 */
	public static int optimizeColumnWidth(JTable _table, int _colNo) {
		TableColumn tabCol = _table.getTableHeader().getColumnModel().getColumn(_colNo);
		TableCellRenderer renderer = tabCol.getHeaderRenderer();
		if (renderer == null) {
			renderer = _table.getTableHeader().getDefaultRenderer();
		}
		Component comp = renderer.getTableCellRendererComponent(_table,
				tabCol.getHeaderValue(), false, false, -1, _colNo);
		int width0 = comp.getPreferredSize().width;
		for (int row = 0; row < _table.getRowCount(); row++) {
			renderer = _table.getCellRenderer(row, _colNo);
			comp = _table.prepareRenderer(renderer, row, _colNo);
			width0 = Math.max(comp.getPreferredSize().width, width0);
		}
		_table.getColumnModel().getColumn(_colNo).setMaxWidth(width0 + 3);
		_table.getColumnModel().getColumn(_colNo).setPreferredWidth(width0 + 3);
		return width0;
	}
	// END KGU#443 2017-10-16

	protected boolean wasModified()
	{
		return !editedLines.isEmpty();
	}
	
	protected Object getValue()
	{
		if (record != null) {
			return record;
		}
		return array;
	}

	// START KGU#443 2017-10-31: Enh. #439 - Recursive application of this dialog 
	private void btnPullDownActionPerformed(java.awt.event.ActionEvent evt)
	{
		Object evtSource = evt.getSource();
		if (evtSource instanceof JButton) {
			String selector = ((JButton)evtSource).getName();
			int rowNr = tblFields.getSelectedRow();
			DefaultTableModel tm = (DefaultTableModel) tblFields.getModel();
			Object val = tm.getValueAt(rowNr, 2);
			if (val != null)
			{
				String qualName = this.getTitle() + (selector.startsWith("[") ? "" : ".") + selector;
				try {
					val = Executor.getInstance().evaluateExpression((String)val, true, false);
					if (val != null) {
						if (!selector.startsWith("[")) {
							selector = "." + selector;
						}
						val = editCompoundValue(qualName, val,
								this.editable,
								(JButton)evtSource);
						if (val != null) {
							tm.setValueAt(Executor.prepareValueForDisplay(val), rowNr, 2);
						}
					}
				} catch (EvalError er) {
					JOptionPane.showMessageDialog((JButton)evtSource,
							Control.msgStructureCorrupt.getText().replace("%", er.toString()), qualName, JOptionPane.ERROR_MESSAGE);
				}
				//varUpdates.put((String)tm.getValueAt(rowNr, 0), val);
				//System.out.println(tm.getValueAt(rowNr, 0).toString() + " <- " + val.toString());
			}
			if (activeBtnEditor != null) {
				activeBtnEditor.stopCellEditing();
				activeBtnEditor = null;
			}
		}
	}

    /**
     * Opens a dialog with editable JTable for the given complex value {@code val},
     * representing either an array (as {@link ArrayList} or a record (as {@link HashMap}.
     * If something therein was modified, then the modified value will be returned.
     * @param _varName - name of the compound variable 
     * @param _value - either an {@link ArayList}{@code <Object>} or a {@link HashMap}{@code<String, Object>} is expected
     * @param _editable - whether the component values may be edited
     * @param _refComponent - the originating button 
     * @return the modified value if the change was committed.
     */
    private Object editCompoundValue(String _title, Object _value, boolean _editable, Component _refComponent) {
    	ValuePresenter valueEditor = new ValuePresenter(_title, _value, _editable, null);
    	valueEditor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	valueEditor.setLocationRelativeTo(_refComponent);
    	valueEditor.setModal(true);
    	valueEditor.setVisible(true);
    	if (valueEditor.wasModified()) {
    		return valueEditor.getValue();
    	}
    	return null;
    }
    // END KGU#443 2017-10-31

    @Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == btnDiscard) {
			this.editedLines.clear();
			this.dispose();
		}
		else if (evt.getSource() == btnCommit) {
			updateValueFromTable();
			this.dispose();
		}
	}

	private void updateValueFromTable() {
		DefaultTableModel tm = (DefaultTableModel)tblFields.getModel();
		Executor executor = Executor.getInstance();
		StringList errors = new StringList();
		for (Integer lineNo: this.editedLines.keySet()) {
			String keyStr = (String)tm.getValueAt(lineNo, 0);
			//String valueStr = (String)tm.getValueAt(lineNo, 1);	// Often returned null
			String valueStr = this.editedLines.get(lineNo);
			try {
				Object value = executor.evaluateExpression(valueStr, true, false);
				if (array != null) {
					array.set(lineNo, value);
				}
				else if (record != null) {
					record.put(keyStr, value);
				}
			} catch (EvalError err) {
				errors.add(keyStr + ": " + err.toString());
			}
		}
		if (errors.count() > 0) {
			JOptionPane.showInternalMessageDialog(this, 
					Control.msgVarUpdatesFailed.getText().replace("%", errors.getText()),
					Control.msgVarUpdateErrors.getText(),
					JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		this.updateValueFromTable();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == tblFields) {
			Object cellEditor = evt.getNewValue(); 
			if (cellEditor == null) {
				// Editing finished, identify the edited cell
				int rowNo = tblFields.getSelectedRow();
				Object newValue = tblFields.getModel().getValueAt(rowNo, 2);
				if (newValue != null && !oldValStrings[rowNo].equals(newValue)) {
					editedLines.put(rowNo, (String)newValue);
					btnDiscard.setEnabled(true);
					btnCommit.setText(Control.lbCommit.getText());
				}
				btnCommit.setEnabled(true);					
			}
			else {
				if (cellEditor instanceof PulldownButtonCellEditor) {
					activeBtnEditor = (PulldownButtonCellEditor)cellEditor;
				}
				btnCommit.setEnabled(false);
			}
		}
	}

}

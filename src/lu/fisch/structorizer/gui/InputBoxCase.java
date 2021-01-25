/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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

package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This dialog allows editing the properties of CASE elements in a more specific way.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2021-01-24/25   First Issue (on behalf of #915)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.utils.StringList;

/**
 * Enhanced and specialized element editor for CASE elements.<br/>
 * Provides a text field for the discriminator expression, a table for
 * the branch selectors, several buttons to manipulate the lines of the
 * table and checkboxes specifying whether existing branches are to be
 * permuted in the same way as the selector lines and whether a default
 * branch is wanted.
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class InputBoxCase extends InputBox implements ItemListener, PropertyChangeListener, ListSelectionListener {
	
	public static final LangTextHolder msgSelectorMustNotBeEmpty = 
			new LangTextHolder("A selector list must not be empty!");
	
	public int[] branchOrder = null;
	private int maxBranch = 0;
	
	protected JLabel lblDiscriminator;
	protected JLabel lblSelectors;
	protected JTextField txtDiscriminator;
	protected JTextField txtDefaultLabel;
	protected JCheckBox chkMoveBranches;
	protected JCheckBox chkDefaultBranch;
	protected JButton btnAddLine;
	protected JButton btnDelLine;
	protected JButton btnUpLine;
	protected JButton btnDnLine;
	protected JTable tblSelectors;
	
	private JPanel pnlSelectorControl;
	private JScrollPane scrSelectors;
	private int ixEdit;

	/**
	 * Constructs the dedicated editor for CASE elements 
	 * @param owner
	 * @param modal
	 */
	public InputBoxCase(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Subclassable method to add specific stuff to the Panel top
	 * @param _panel - the panel to be enhanced
	 * @param pnPanel0c - the layout constraints
	 * @return number of lines (y cell units) inserted
	 */
	@Override
	protected int createPanelTop(JPanel _panel, GridBagLayout _gb, GridBagConstraints _gbc)
	{
		// START Issue #81 (DPI awareness workaround)
		double scaleFactor = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
		if (scaleFactor < 1) scaleFactor = 1.0;
		// END Issue #81
		
		lblDiscriminator = new JLabel("Choice expression:");
		lblSelectors = new JLabel("Branch selectors:");
		txtDiscriminator = new JTextField();	// FIXME Length should not be hard-coded
		txtDefaultLabel = new JTextField();	// FIXME Length should not be hard-coded
		chkMoveBranches = new JCheckBox("Move associated branches");
		chkDefaultBranch = new JCheckBox("Default branch:");
		btnAddLine = new JButton(IconLoader.getIcon(18));
		btnDelLine = new JButton(IconLoader.getIcon(5));
		btnUpLine = new JButton(IconLoader.getIcon(19));
		btnDnLine = new JButton(IconLoader.getIcon(20));
		tblSelectors = new JTable();
		scrSelectors = new JScrollPane();
		
		pnlSelectorControl = new JPanel();
		
		tblSelectors.setGridColor(Color.LIGHT_GRAY);
		tblSelectors.setShowGrid(true);
		tblSelectors.addPropertyChangeListener("tableCellEditor", this);
		tblSelectors.setModel(new javax.swing.table.DefaultTableModel(5, 2) {
			Class<?>[] types = new Class<?> [] {
				java.lang.String.class, java.lang.String.class
			};
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return types [columnIndex];
			}
			@Override
			public boolean isCellEditable(int row, int column){
				return column == 1;
			}
		});
		tblSelectors.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		tblSelectors.setRowHeight((int)(tblSelectors.getFontMetrics(txtText.getFont()).getHeight()));
		tblSelectors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblSelectors.getSelectionModel().addListSelectionListener(this);
		scrSelectors.setViewportView(tblSelectors);
		scrSelectors.setPreferredSize(new Dimension((int)(400 * scaleFactor), (int)(100 * scaleFactor)));
		tblSelectors.setTableHeader(null);
		
		chkMoveBranches.setEnabled(false);
		chkDefaultBranch.addItemListener(this);
		
		txtDiscriminator.addKeyListener(this);
		txtDefaultLabel.addKeyListener(this);
		btnAddLine.addActionListener(this);
		btnDelLine.addActionListener(this);
		btnUpLine.addActionListener(this);
		btnDnLine.addActionListener(this);
		
		pnlSelectorControl.setLayout(new BoxLayout(pnlSelectorControl, BoxLayout.Y_AXIS));
		lblSelectors.setAlignmentX(Component.LEFT_ALIGNMENT);
		chkMoveBranches.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlSelectorControl.add(lblSelectors);
		pnlSelectorControl.add(chkMoveBranches);
		JPanel pnlSelCtrlButtons = new JPanel();
		pnlSelCtrlButtons.setLayout(new BoxLayout(pnlSelCtrlButtons, BoxLayout.X_AXIS));
		pnlSelCtrlButtons.add(btnAddLine);
		pnlSelCtrlButtons.add(btnDelLine);
		pnlSelCtrlButtons.add(btnUpLine);
		pnlSelCtrlButtons.add(btnDnLine);
		pnlSelCtrlButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlSelectorControl.add(pnlSelCtrlButtons);
		
		// START Issue #284
		scalableComponents.addElement(txtDiscriminator);
		scalableComponents.addElement(txtDefaultLabel);
		scalableComponents.addElement(tblSelectors);
		// END Issue #284
		
		int lineNo = 1;
		
		int border = (int)(5 * scaleFactor);
		_gbc.insets = new Insets(2*border, border, 0, border);
		
		_gbc.gridx = 0;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(lblDiscriminator, _gbc);
		_panel.add(lblDiscriminator);
		
		_gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = GridBagConstraints.REMAINDER;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gbc.anchor = GridBagConstraints.EAST;
		_gb.setConstraints(txtDiscriminator, _gbc);
		_panel.add(txtDiscriminator);
		
		lineNo++;
		
		_gbc.gridx = 0;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.NORTHWEST;
		_gb.setConstraints(pnlSelectorControl, _gbc);
		_panel.add(pnlSelectorControl);
		
		_gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = GridBagConstraints.REMAINDER;
		_gbc.fill = GridBagConstraints.BOTH;
		_gbc.anchor = GridBagConstraints.EAST;
		_gb.setConstraints(scrSelectors, _gbc);
		_panel.add(scrSelectors);
		
		lineNo++;
		
		_gbc.gridx = 0;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 1;
		_gbc.fill = GridBagConstraints.NONE;
		_gbc.anchor = GridBagConstraints.WEST;
		_gb.setConstraints(chkDefaultBranch, _gbc);
		_panel.add(chkDefaultBranch);
		
		_gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = GridBagConstraints.REMAINDER;
		_gbc.fill = GridBagConstraints.HORIZONTAL;
		_gbc.anchor = GridBagConstraints.EAST;
		_gb.setConstraints(txtDefaultLabel, _gbc);
		_panel.add(txtDefaultLabel);
		
		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent evt) {
				resizeTableScrollPane();
			}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}});
		
		return lineNo;
	}

	// listen to actions
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		DefaultTableModel tm = (DefaultTableModel) tblSelectors.getModel();
		int ixSelected = tblSelectors.getSelectedRow();
		if (source == btnAddLine) {
			int nRows = tm.getRowCount();
			tblSelectors.clearSelection();
			tm.addRow(new Object[] {"", "???"});
			adjustTableSize();
			tblSelectors.setRowSelectionInterval(nRows, nRows);
		}
		else if (source == btnDelLine) {
			if (ixSelected >= 0) {
				tblSelectors.clearSelection();
				tm.removeRow(ixSelected);
				adjustTableSize();
			}
		}
		else if (source == btnUpLine) {
			if (ixSelected > 0) {
				Object val0 = tm.getValueAt(ixSelected, 0);
				Object val1 = tm.getValueAt(ixSelected, 1);
				tblSelectors.clearSelection();
				tm.removeRow(ixSelected);
				tm.insertRow(ixSelected-1, new Object[] {val0, val1});
				tblSelectors.setRowSelectionInterval(ixSelected-1, ixSelected-1);
			}
		}
		else if (source == btnDnLine) {
			if (ixSelected >= 0 && ixSelected < tm.getRowCount()-1) {
				Object val0 = tm.getValueAt(ixSelected, 0);
				Object val1 = tm.getValueAt(ixSelected, 1);
				tblSelectors.clearSelection();
				tm.removeRow(ixSelected);
				tm.insertRow(ixSelected+1, new Object[] {val0, val1});
				tblSelectors.setRowSelectionInterval(ixSelected+1, ixSelected+1);
			}
		}
		else {
			if (source == btnOK) {
				StringList text = StringList.explode(txtDiscriminator.getText(), "\\\\n");
				for (int i = 0; i < text.count()-1; i++) {
					String line = text.get(i);
					if (!line.endsWith("\\")) {
						text.set(i, line + "\\");
					}
				}
				String lastLine = text.get(text.count()-1);
				while (lastLine.endsWith("\\")) {
					lastLine = lastLine.substring(0, lastLine.length()-1);
				}
				text.set(text.count()-1, lastLine);
				int nRows = tm.getRowCount();
				this.branchOrder = new int[nRows+1];
				this.branchOrder[nRows] = maxBranch;
				for (int i = 0; i < nRows; i++) {
					branchOrder[i] = 0;
					String branchIx = (String)tm.getValueAt(i, 0);
					if (!branchIx.isEmpty()) {
						branchOrder[i] = Integer.parseInt(branchIx);
					}
					StringList selector = StringList.explode(((String)tm.getValueAt(i, 1)), "\\\\n");
					for (int j = 0; j < selector.count()-1; j++) {
						String line = selector.get(j);
						if (!line.endsWith("\\")) {
							text.add(line + "\\");
						}
						else {
							text.add(line);
						}
					}
					lastLine = selector.get(selector.count()-1);
					while (lastLine.endsWith("\\")) {
						lastLine = lastLine.substring(0, lastLine.length()-1);
					}
					text.add(lastLine);
				}
				if (chkDefaultBranch.isSelected()) {
					String defaultLabel = txtDefaultLabel.getText().trim();
					if (defaultLabel.isEmpty() || defaultLabel.equals("%")) {
						StringList defText = StringList.explode(Element.preCase, "\n");
						defaultLabel = defText.get(defText.count()-1).trim();
						if (defaultLabel.isEmpty() || defaultLabel.equals("%")) {
							defaultLabel = "default";
						}
					}
					text.add(defaultLabel);
				}
				else {
					text.add("%");
				}
				txtText.setText(text.getText());
			}
			super.actionPerformed(event);
		}
	}
	
	@Override
	public void checkConsistency() {
		// First we try to obtain the StringList of unbroken lines...
		StringList text = StringList.explode(txtText.getText(), "\n");
		int i = 0;
		while (i < text.count()-1) {
			if (text.get(i).endsWith("\\")) {
				// We compose the line with the next one but insert a visible "\n"
				text.set(i, text.get(i) + "n" + text.get(i+1));
			}
			else {
				i++;
			}
		}
		int nLines = text.count();
		txtDiscriminator.setText(text.get(0));
		String defaultLabel = "";
		// With exactly one line it might be a TRY element with single catch clause
		// (CASE element texts consist of at least 3 lines!)
		if (nLines > 1 && !(defaultLabel = text.get(nLines-1)).equals("%")) {
			txtDefaultLabel.setText(defaultLabel);
			chkDefaultBranch.setSelected(true);
		}
		// See above
		if (nLines > 1) {
			text.remove(--nLines);
		}
		maxBranch = nLines;	// Retain the default branch number
		DefaultTableModel tm = (DefaultTableModel) tblSelectors.getModel();
		tm.setRowCount(0);
		for (i = 1; i < nLines; i++) {
			tm.addRow(new Object[] {Integer.toString(i), text.get(i)});
		}
		adjustTableSize();
		if (!forInsertion) {
			chkMoveBranches.setEnabled(true);
			chkMoveBranches.setSelected(true);
		}
		tblSelectors.setRowHeight((int)(tblSelectors.getFontMetrics(txtText.getFont()).getHeight()));
		tblSelectors.revalidate();
		
		valueChanged(null);
		
		scrText.setVisible(false);
	}
	
	@Override
	public void itemStateChanged(ItemEvent iev) {
		if (iev.getSource() == chkDefaultBranch)
		{
			txtDefaultLabel.setEnabled(iev.getStateChange() == ItemEvent.SELECTED);
			if (txtDefaultLabel.isEnabled() && txtDefaultLabel.getText().trim().isEmpty()) {
				txtDefaultLabel.setText("default");
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == tblSelectors) {
			Object cellEditor = evt.getNewValue();
			if (cellEditor != null && cellEditor instanceof DefaultCellEditor) {
				if (((DefaultCellEditor)cellEditor).getComponent() != null) {
					((DefaultCellEditor)cellEditor).getComponent().setFont(tblSelectors.getFont());
				}
				ixEdit = tblSelectors.getSelectedRow();
				btnOK.setEnabled(false);
			}
			else {
				adjustTableSize();
				if (ixEdit >= 0 && ixEdit < tblSelectors.getRowCount()) {
					String selector = (String)tblSelectors.getValueAt(ixEdit, 1);
					if (selector == null || selector.trim().isEmpty()) {
						JOptionPane.showMessageDialog(this,
								msgSelectorMustNotBeEmpty .getText(),
								selector, JOptionPane.WARNING_MESSAGE);
					}
				}
				btnOK.setEnabled(true);
			}
		}
	}

	/**
	 * Adjusts the actual tables size according to the number of rows and row widths
	 * Will also ensure a sensible scroll pane width and the horizontal scrollbar
	 * @param _table TODO
	 */
	public void adjustTableSize() {
		Dimension tabSize = getRequiredTableSize(tblSelectors, true);
		tblSelectors.setPreferredSize(tabSize);
		Dimension scrSize = scrSelectors.getPreferredSize();
		if (tabSize.width > scrSize.width) {
			tblSelectors.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		}
		else {
			tblSelectors.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent evt) {
		int ixSel = tblSelectors.getSelectedRow();
		int nRows = tblSelectors.getRowCount();
		btnDelLine.setEnabled(ixSel >= 0 && (nRows > 1 || this.elementType.equalsIgnoreCase("try") && nRows > 0));
		btnUpLine.setEnabled(ixSel > 0);
		btnDnLine.setEnabled(ixSel >= 0 && ixSel < nRows-1);
	}
	
	@Override
	public void fontControl(boolean up)
	{
		super.fontControl(up);
		/*
		 * If we don't do his then the actual drawing canvas of the table might get too
		 * large or too small w.r.t. the table rows.
		 */
		adjustTableSize();
	}

	@Override
	protected void adjustLangDependentComponents()
	{
		super.adjustLangDependentComponents();
		resizeTableScrollPane();
	}

	/**
	 * Adapts the width of the scroll pane associated to {@link #tblSelectors} according
	 * the current width of the left part of the top pane and the dialog width.<br/>
	 * This is a precarious workaround for the lacking auto-resizing of a JTable ScrollPane
	 * within a {@link GridBagLayout}: If the new size is a little to wide then the layout
	 * will collapse, if it is a little too narrow then it will look ugly.
	 */
	private void resizeTableScrollPane() {
		// FIXME Precarious workaround for a layout defect
		double scaleFactor = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
		if (scaleFactor < 1) scaleFactor = 1.0;
		int border = (int)(5 * scaleFactor);
		Dimension dialogSize = getSize();
		Dimension pnlSize = pnlSelectorControl.getPreferredSize();
		int diff = dialogSize.width - pnlSize.width - 4 * border;
		scrSelectors.setPreferredSize(new Dimension((int)(diff - 25 * scaleFactor), (int)(100 * scaleFactor)));
		adjustTableSize();
	}

}

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
 *      Kay Gürtzig     2021-02-06/10   More functionality implemented, resizing behaviour fixed
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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
//import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.locales.Locales;
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
public class InputBoxCase extends InputBox implements ItemListener, PropertyChangeListener, ListSelectionListener, DocumentListener {
	
// For the case the checkLines activity allows to select separate conflicts
// (just in order to be able to modify the background colour of the selected lines)
//	private class MyCellRenderer extends DefaultTableCellRenderer {
//		@Override
//		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
//		{
//			// TODO Set special colour for compromised lines
//			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//		}
//	};
	
	/**
	 * Specific cell editor for the first column, offers a pulldown choice
	 * list with the numbers of orphaned (available) branch numbers.
	 */
	private class NumberCellEditor extends DefaultCellEditor {

		private JComboBox<String> combo = null;

		/**
		 * @param textField
		 */
		public NumberCellEditor() {
			super(new JTextField());
		}

		@Override
		public Object getCellEditorValue() {
			if (this.combo != null) {
				Object val = this.combo.getSelectedItem();
				if (val instanceof String) {
					return ((String)val).trim();
				}
			}
			return super.getCellEditorValue();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			Vector<String> options = getUnusedBranchNumbers(row);
			if (options.size() > 1) {
				if (this.combo == null) {
					this.combo = new JComboBox<String>(options);
				}
				else {
					this.combo.removeAllItems();
					for (String item: options) {
						this.combo.addItem(item);
					}
				}
				return this.combo;
			}
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	};
	
	public static final LangTextHolder msgSelectorMustNotBeEmpty = 
			new LangTextHolder("A selector list must not be empty!");
	// START KGU#927 2021-02-06: Issue #915 update - more functionality
	public static final LangTextHolder msgRedundantRows =
			new LangTextHolder("Redundant rows (unlikely values):");
	public static final LangTextHolder msgConflictsDetected =
			new LangTextHolder("Selector conflicts detected:");
	public static final LangTextHolder msgOrphanedBranches =
			new LangTextHolder("Orphaned branches (doomed on commit):");
	public static final LangTextHolder msgDubiousSelectors =
			new LangTextHolder("Dubious selectors (not evaluable):");
	public static final LangTextHolder msgStructuredDiscriminator =
			new LangTextHolder("A structured choice expression is unsuited!");
	public static final LangTextHolder msgMissingValues =
			new LangTextHolder("Missing enumerator values (and no default):");
	public static final LangTextHolder msgNoProblems =
			new LangTextHolder("No obvious problems detected.");
	public static final LangTextHolder msgRows =
			new LangTextHolder("rows");
	// END KGU#927 2021-02-06
	
	// START KGU#927 2021-02-10: Issue #915 update - better resizing behaviour
	/**
	 * Specifies the nominal initial dimension of {@link #tblSelectors} (to
	 * be scaled, of course)
	 */
	private static final int[] TABLE_SIZE = new int[] {400, 100};
	// comment field was on the brink of being hidden
	private static final int IBC_PREFERRED_HEIGHT = 425;
	// END KGU#927 2021-02-10
	
	/**
	 * Contains the numbers of non-empty branches (starting at 1) in
	 * existing order (on entry) or intended order (on commit). For empty
	 * branches contains 0 in the respective place. (Length is the total
	 * number of branches.)
	 */
	public int[] branchOrder = null;
	/**
	 * Number of the original branches (on entry)
	 */
	private int maxBranch = 0;
	
	// START KGU#927 2021-02-06: Enh. #915 More functionality
	private CaseEditHelper valueHelper = null;
	private Color standardButtonBackground = null;
	// END KGU#927 2021-02-06
	
	protected JLabel lblDiscriminator;
	protected JLabel lblSelectors;
	protected JTextField txtDiscriminator;
	protected JTextField txtDefaultLabel;
	protected JCheckBox chkMoveBranches;
	protected JCheckBox chkDefaultBranch;
	protected JButton btnAddRow;
	protected JButton btnDelRows;
	protected JButton btnUpRow;
	protected JButton btnDnRow;
	// START KGU#927 2021-02-06: Enh. #915
	protected JButton btnMergeRows;
	protected JButton btnSplitRow;
	protected JButton btnEnumAssist;
	protected JButton btnCheckRows;
	// END KGU#927 2021-02-06
	protected JTable tblSelectors;
	
	private JPanel pnlSelectorControl;
	private JScrollPane scrSelectors;
	private int ixEdit = -1;
//	private DefaultCellEditor activeCellEditor = null;
	//private boolean discriminatorModified = false;
	
	// FIXME temporary field
	private boolean tempHintGiven = false;

	/**
	 * Constructs the dedicated editor for CASE elements.
	 * 
	 * @param owner - the owning GUI Frame
	 * @param modal - whether the dialog is to be modal
	 * @param helper - a {@link CaseEditHelper} to be used for branch checks
	 */
	public InputBoxCase(Frame owner, boolean modal, CaseEditHelper helper) {
		super(owner, modal);
		// START KGU#927 2021-02-06: Enh. #915
		valueHelper = helper;
		// END KGU#927 2021-02-06
	}

	// START KGU#927 2021-02-10: Issue #915 update - comment field was on the brink of being hidden
	@Override
	protected void setPreferredSize(double scaleFactor) {
		setSize((int)(PREFERRED_SIZE[0] * scaleFactor), (int)(IBC_PREFERRED_HEIGHT * scaleFactor));
	}
	// END KGU#927 2021-02-10

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
		txtDiscriminator = new JTextField();
		txtDefaultLabel = new JTextField();
		chkMoveBranches = new JCheckBox("Move associated branches");
		chkDefaultBranch = new JCheckBox("Default branch:");
		btnAddRow = new JButton(IconLoader.getIcon(18));
		btnDelRows = new JButton(IconLoader.getIcon(5));
		btnUpRow = new JButton(IconLoader.getIcon(19));
		btnDnRow = new JButton(IconLoader.getIcon(20));
		// START KGU#927 2021-02-06: Enh. #915
		btnMergeRows = new JButton(IconLoader.getIcon(127));
		btnSplitRow = new JButton(IconLoader.getIcon(128));
		btnCheckRows = new JButton(IconLoader.getIcon(83));
		btnEnumAssist = new JButton(IconLoader.getIcon(109));
		standardButtonBackground = btnCheckRows.getBackground();
		// END KGU#927 2021-02-06
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
				if (column == 0) {
					Vector<String> unusedNumbers = getUnusedBranchNumbers(row);
					return unusedNumbers.size() > 1;
				}
				return column == 1;
			}
		});
		tblSelectors.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		// START KGU#927 2021-02-07: Enh. #915
		//tblSelectors.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblSelectors.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		tblSelectors.getColumnModel().getColumn(0).setCellEditor(new NumberCellEditor());
		// END KGU#927 2021-02-07
		tblSelectors.getSelectionModel().addListSelectionListener(this);
		scrSelectors.setViewportView(tblSelectors);
		scrSelectors.setPreferredSize(new Dimension((int)(TABLE_SIZE[0] * scaleFactor), (int)(TABLE_SIZE[1] * scaleFactor)));
		tblSelectors.setTableHeader(null);
		
		chkMoveBranches.setEnabled(false);
		chkDefaultBranch.addItemListener(this);
		
		txtDiscriminator.addKeyListener(this);
		txtDefaultLabel.addKeyListener(this);
		btnAddRow.addActionListener(this);
		btnDelRows.addActionListener(this);
		btnUpRow.addActionListener(this);
		btnDnRow.addActionListener(this);
		// START KGU#393 2021-01-26: Issue #400
		btnAddRow.addKeyListener(this);
		btnDelRows.addKeyListener(this);
		btnUpRow.addKeyListener(this);
		btnDnRow.addKeyListener(this);
		chkMoveBranches.addKeyListener(this);
		chkDefaultBranch.addKeyListener(this);
		// END KGU#393 2021-01-26
		// START KGU#927 2021-02-06: Enh. #915 More functionality
		txtDiscriminator.getDocument().addDocumentListener(this);
//		txtDiscriminator.addFocusListener(new FocusListener() {
//			@Override
//			public void focusGained(FocusEvent e) {
//				discriminatorModified = false;
//			}
//			@Override
//			public void focusLost(FocusEvent e) {
//				if (discriminatorModified) {
//					checkLines(null, false);
//					discriminatorModified = false;
//				}
//			}});
		btnMergeRows.addActionListener(this);
		btnSplitRow.addActionListener(this);
		btnEnumAssist.addActionListener(this);
		btnCheckRows.addActionListener(this);
		btnMergeRows.addKeyListener(this);
		btnSplitRow.addKeyListener(this);
		btnEnumAssist.addKeyListener(this);
		btnCheckRows.addKeyListener(this);
		String lafName = UIManager.getLookAndFeel().getName();
		btnCheckRows.setOpaque(!"Nimbus".equals(lafName) && !"CDE/Motif".equals(lafName));
		// END KGU#927 2021-02-06
		
		pnlSelectorControl.setLayout(new BoxLayout(pnlSelectorControl, BoxLayout.Y_AXIS));
		lblSelectors.setAlignmentX(Component.LEFT_ALIGNMENT);
		chkMoveBranches.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlSelectorControl.add(lblSelectors);
		pnlSelectorControl.add(chkMoveBranches);
		JPanel pnlSelCtrlButtons = new JPanel();
		pnlSelCtrlButtons.setLayout(new BoxLayout(pnlSelCtrlButtons, BoxLayout.X_AXIS));
		pnlSelCtrlButtons.add(btnAddRow);
		pnlSelCtrlButtons.add(btnDelRows);
		pnlSelCtrlButtons.add(btnUpRow);
		pnlSelCtrlButtons.add(btnDnRow);
		pnlSelCtrlButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlSelectorControl.add(pnlSelCtrlButtons);
		
		// START KGU#927 2021-02-06: Enh. #915
		JPanel pnlSelManipButtons = new JPanel();
		pnlSelManipButtons.setLayout(new BoxLayout(pnlSelManipButtons, BoxLayout.X_AXIS));
		pnlSelManipButtons.add(btnMergeRows);
		pnlSelManipButtons.add(btnSplitRow);
		pnlSelManipButtons.add(btnEnumAssist);
		pnlSelManipButtons.add(btnCheckRows);
		pnlSelManipButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
		pnlSelectorControl.add(pnlSelManipButtons);
		// END KGU#927 2021-02-06
		
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
			public void componentHidden(ComponentEvent e) {}}
		);
		
		return lineNo;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_ESCAPE && ixEdit >= 0) {
			// Don't close the window, this is to end table cell editing
			return;
		}
		super.keyPressed(e);
	}

	// listen to actions
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		DefaultTableModel tm = (DefaultTableModel) tblSelectors.getModel();
		int[] ixSelected = tblSelectors.getSelectedRows();
		int nSelected = ixSelected.length;
		
		if (source == btnAddRow) {
			int nRows = tm.getRowCount();
			tblSelectors.clearSelection();
			tm.addRow(new Object[] {"", "?"});
			adjustTableSize();
			tblSelectors.setRowSelectionInterval(nRows, nRows);
			Rectangle cellRect = tblSelectors.getCellRect(nRows, 1, true);
			tblSelectors.scrollRectToVisible(cellRect);
			// The editor will request its focus itself in this.propertyChanged(e)
			tblSelectors.editCellAt(nRows, 1);
		}
		else if (source == btnDelRows) {
			if (nSelected > 0) {
				deleteRows(tm, ixSelected);
			}
		}
		else if (source == btnUpRow) {
			if (nSelected > 0 && ixSelected[0] > 0) {
				// In fact we move the preceding row down
				int ixSel = ixSelected[0] - 1;
				Object val0 = tm.getValueAt(ixSel, 0);
				Object val1 = tm.getValueAt(ixSel, 1);
				tblSelectors.clearSelection();
				tm.removeRow(ixSel);
				tm.insertRow(ixSel + nSelected, new Object[] {val0, val1});
				tblSelectors.setRowSelectionInterval(ixSel, ixSel + nSelected-1);
			}
		}
		else if (source == btnDnRow) {
			if (nSelected > 0 && ixSelected[nSelected-1] < tm.getRowCount()-1) {
				// In fact we move the subsequent row up
				int ixSel = ixSelected[nSelected-1] + 1;
				Object val0 = tm.getValueAt(ixSel, 0);
				Object val1 = tm.getValueAt(ixSel, 1);
				tblSelectors.clearSelection();
				tm.removeRow(ixSel);
				tm.insertRow(ixSelected[0], new Object[] {val0, val1});
				tblSelectors.setRowSelectionInterval(ixSelected[0]+1, ixSel);
			}
		}
		// START KGU#927 2021-02-06: Enh. #915
		else if (source == btnMergeRows) {
			mergeRows(tm, ixSelected);
		}
		else if (source == btnSplitRow) {
			splitRow(tm, ixSelected);
		}
		else if (source == btnEnumAssist) {
			forceEnumeratorCompleteness(tm);
		}
		else if (source == btnCheckRows) {
			checkRows(tm, true);
		}
		// END KGU#927 2021-02-06
		else {
			if (source == btnOK) {
				StringList text = new StringList();
				addBrokenLine(text, txtDiscriminator.getText());
				int defBranchNo = 0;
				int nRows = tm.getRowCount();
				if (this.branchOrder != null && maxBranch < this.branchOrder.length) {
					defBranchNo = this.branchOrder[maxBranch];
				}
				this.branchOrder = new int[nRows+1];
				this.branchOrder[nRows] = defBranchNo;
				for (int i = 0; i < nRows; i++) {
					branchOrder[i] = 0;
					String branchIx = (String)tm.getValueAt(i, 0);
					if (!branchIx.isEmpty()) {
						branchOrder[i] = Integer.parseInt(branchIx);
					}
					addBrokenLine(text, ((String)tm.getValueAt(i, 1)));
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

	/**
	 * Adds the given {@code line} to the StringList {@code text}, decomposing it
	 * at all {@code "\n"} substrings outside of string or character literals
	 * into "broken lines", i.e. several lines where all but the last one end
	 * with a backslash. All ending backslashes of the last part of the line will
	 * be removed.
	 * 
	 * @param text - the {@link StringList} to be composed
	 * @param line - the text line to be appended (after preparation)
	 */
	private void addBrokenLine(StringList text, String line) {
		/* Lest we should split string literals containing "\n", we must tokenize
		 * the text after having temporarily replaced "\n" by true newlines, such
		 * that newlines within strings should survive when we split the token list.
		 * Afterwards we will re-unite the tokens and undo the replacement.
		 */
		line = line.replace("\\n", "\n");
		
		StringList tokens = Element.splitLexically(line, true);
		int posNl = -1;
		while ((posNl = tokens.indexOf("\n")) >= 0) {
			text.add(tokens.concatenate(null, 0, posNl).replace("\n", "\\n") + "\\");
			tokens.remove(0, posNl+1);
		}
		text.add(tokens.concatenate(null).replace("\n", "\\n"));
		String lastLine = text.get(text.count()-1);
		while (lastLine.endsWith("\\")) {
			lastLine = lastLine.substring(0, lastLine.length()-1);
		}
		text.set(text.count()-1, lastLine);
	}

	/**
	 * Deletes the selected table lines.
	 * 
	 * @param tm - the table model of {@link #tblSelectors}
	 * @param ixSelected - the current selection span
	 */
	private void deleteRows(DefaultTableModel tm, int[] ixSelected) {
		if (tm == null) {
			tm = (DefaultTableModel)tblSelectors.getModel();
		}
		tblSelectors.clearSelection();
		for (int i = ixSelected.length-1; i >= 0; i--) {
			tm.removeRow(ixSelected[i]);
		}
		adjustTableSize();
		checkEnumButton(null);
		checkRows(tm, false);
		if (ixSelected[0] >= tm.getRowCount()) {
			ixSelected[0] = tm.getRowCount()-1;
		}
		if (ixSelected[0] >= 0) {
			tblSelectors.setRowSelectionInterval(ixSelected[0], ixSelected[0]);
		}
	}

	/**
	 * Merges the lines of the selection interval {@code ixSelected}.
	 * 
	 * @param tm - TableModel of {@link #tblSelectors}
	 * @param ixSelected - the index span of selected rows
	 */
	private void mergeRows(DefaultTableModel tm, int[] ixSelected) {
		int nSelected = ixSelected.length;
		int nRows = tm.getRowCount();
		int minRowCount = 2;
		if (this.elementType.equalsIgnoreCase("try")) {
			minRowCount = 1;
		}
		if (nSelected > 1 && nRows - nSelected >= minRowCount-1) {
			/* We don't care for the case of several non-empty branches
			 * here, but rely on the correct enabling control in valueChanged(),
			 * so at most one row may have an existing relevant branch
			 */
			int ixNonEmpty = ixSelected[0];
			StringList lines = new StringList();
			tblSelectors.clearSelection();
			for (int i = 0; i < nSelected; i++) {
				Object val = tm.getValueAt(ixSelected[i], 1);
				if (val instanceof String && !((String) val).trim().isEmpty()) {
					lines.add((String)val);
				}
				val = tm.getValueAt(ixSelected[i], 0);
				if (val instanceof String && !((String) val).trim().isEmpty()) {
					ixNonEmpty = ixSelected[i];
				}
			}
			tm.setValueAt(lines.concatenate(", "), ixNonEmpty, 1);
			// First remove the subsequent lines ...
			for (int ix = ixNonEmpty+1; ix <= ixSelected[nSelected-1]; ix++) {
				tm.removeRow(ixNonEmpty+1);
			}
			// ... the remove the preceding selected lines
			for (int ix = ixSelected[0]; ix < ixNonEmpty; ix++) {
				tm.removeRow(ixSelected[0]);
			}
			adjustTableSize();
			tblSelectors.setRowSelectionInterval(ixSelected[0], ixSelected[0]);
		}
	}

	/**
	 * Performs the splitting of the selected line, i.e. distributes the
	 * comma-separated expressions to as many rows (all but the first one
	 * new), the new rows will be inserted immediately after the selected
	 * one. In the event, all modified or created rows will be selected.<br/>
	 * If the source row was associated to a non-empty branch then all new
	 * rows will also inherit the branch number such that the branch would
	 * be copied on committing.
	 * 
	 * @param tm - the table model of {@link #tblSelectors}
	 * @param ixSelected - the current selection span (if the length differs
	 *    from 1 them nothing will be done here)
	 */
	private void splitRow(DefaultTableModel tm, int[] ixSelected) {
		if (ixSelected.length == 1) {
			int ixSel = ixSelected[0];
			Object branchNo = tm.getValueAt(ixSel, 0);
			Object val = tm.getValueAt(ixSel, 1);
			if (val instanceof String) {
				tblSelectors.clearSelection();
				StringList exprs = Element.splitExpressionList((String)val, ",");
				if (exprs.count() > 1) {
					// Replace the line content by its first element
					tm.setValueAt(exprs.get(0), ixSel, 1);
					// Now insert copies with the subsequent selectors
					for (int i = 1; i < exprs.count(); i++) {
						tm.insertRow(ixSel + i, new Object[] {branchNo, exprs.get(i)});
					}
				}
				adjustTableSize();
				// Restore the selection (i.e. spread it over all affected lines)
				tblSelectors.setRowSelectionInterval(ixSel, ixSel + exprs.count()-1);
			}
		}
	}

	/**
	 * Checks the lines for conflicts and redundancy. Also checks for orphaned
	 * branches. Will pop up a message if {@code interactive} is {@code true},
	 * otherwise only controls the colour of button {@link #btnCheckRows}.
	 * 
	 * @param tm - the table model of {@link #tblSelectors} or {@code null}
	 * @param interactive - whether the result is to be reported as message box
	 */
	private void checkRows(DefaultTableModel tm, boolean interactive) {
		if (tm == null) {
			tm = (DefaultTableModel)tblSelectors.getModel();
		}
		int nRows = tm.getRowCount();
		// First map all values
		StringList lines = new StringList();
		for (int i = 0; i < nRows; i++) {
			Object selectors = tm.getValueAt(i, 1);
			// Might be null during initialisation
			if (selectors != null) {
				lines.add(((String)selectors));
			}
		}
		HashMap<String, ArrayList<Integer>> values = valueHelper.checkValues(lines, false);
		// Check unevaluable selectors (possibly syntax errors)
		StringList dubiousValues = new StringList();
		for (String selector: values.keySet()) {
			if (valueHelper.evaluateExpression(selector) == null) {
				dubiousValues.add(selector);
			}
		}
		// Retrieve conflicts in particular
		HashMap<String, ArrayList<Integer>> conflicts = valueHelper.checkValues(lines, true);
		// Check for redundant rows and missing values in case of an enumerator
		HashMap<String, Integer> enumVals = valueHelper.getEnumeratorInfo(txtDiscriminator.getText());
		long unusedLines = 0L;	// Bitmap
		StringList missingValues = new StringList();
		if (enumVals != null) {
			unusedLines = (1L << nRows) - 1;
			boolean noDefault = !chkDefaultBranch.isSelected();
			for (Map.Entry<String, Integer> entry: enumVals.entrySet()) {
				Integer code = entry.getValue();
				ArrayList<Integer> rowNos = values.get(code.toString());
				if (rowNos != null) {
					for (Integer rowNo: rowNos) {
						// Remove the respective bit
						unusedLines &= ~(1L << rowNo);
					}
				}
				else if (noDefault) {
					missingValues.add(entry.getKey());
				}
			}
		}
		// Check orphaned branches
		Vector<String> orphanedBranches = this.getUnusedBranchNumbers(-1);
		// Check for structured discriminator
		boolean isInappropriate = valueHelper.isStructured(txtDiscriminator.getText());

		// No it is time to act according to the findings
		if (!conflicts.isEmpty() || unusedLines != 0 || orphanedBranches.size() > 1
				|| isInappropriate || !dubiousValues.isEmpty() || !missingValues.isEmpty()) {
			// Decide the severity
			if (!conflicts.isEmpty() || isInappropriate) {
				btnCheckRows.setBackground(Color.RED);
			}
			else {
				btnCheckRows.setBackground(Color.ORANGE);
			}
			if (interactive) {
				// Compose the problem report
				StringBuilder sb = new StringBuilder();
				// 1. Inappropriate discriminator expression type
				if (isInappropriate) {
					sb.append(msgStructuredDiscriminator.getText());
					sb.append("\n\n");
				}
				// 2. Selector conflicts
				if (!conflicts.isEmpty()) {
					sb.append(msgConflictsDetected.getText());
					sb.append("\n");
				}
				for (Map.Entry<String, ArrayList<Integer>> conflict: conflicts.entrySet()) {
					sb.append("    ");
					sb.append(conflict.getKey());
					String sepa = ": " + msgRows.getText() + " ";
					for (Integer index: conflict.getValue()) {
						sb.append(sepa);
						sb.append(index + 1);
						sepa = ", ";
					}
					sb.append("\n");
				}
				// 3. Missing enumerator values
				if (!missingValues.isEmpty()) {
					sb.append(msgMissingValues.getText());
					sb.append("\n    ");
					sb.append(missingValues.concatenate(", "));
					sb.append("\n");
				}
				// 4. Unused rows
				if (unusedLines != 0) {
					sb.append(msgRedundantRows.getText());
					sb.append("\n");
					for (int i = 0; i < nRows; i++) {
						if ((unusedLines & (1L << i)) != 0) {
							sb.append("    ");
							sb.append(i+1);
							sb.append(": ");
							sb.append(tm.getValueAt(i, 1));
							sb.append("\n");
						}
					}
				}
				// 5. Orphaned branches
				if (orphanedBranches.size() > 1) {
					sb.append(msgOrphanedBranches.getText());
					sb.append("\n    ");
					String defBranchNoStr = "";
					if (maxBranch < branchOrder.length) {
						defBranchNoStr = Integer.toString(branchOrder[maxBranch]);
					}
					for (int i = 1; i < orphanedBranches.size(); i++) {
						if (i > 1) sb.append(", ");
						String branchNoStr = orphanedBranches.get(i);
						sb.append(branchNoStr);
						if (branchNoStr.equals(defBranchNoStr)) {
							String defaultLabel = txtDefaultLabel.getText();
							if (defaultLabel.trim().isEmpty()) {
								defaultLabel = "default";
							}
							sb.append(" (= " + defaultLabel + ")");
						}
					}
					sb.append("\n");
				}
				// 6. Dubious selectors (not evaluable expressions)
				if (!dubiousValues.isEmpty()) {
					sb.append(msgDubiousSelectors.getText());
					sb.append("\n    ");
					sb.append(dubiousValues.concatenate("\n    "));
				}
				// Show the composed problem report
				JOptionPane.showMessageDialog(this, 
						sb.toString(),
						ElementNames.getElementName('c', false, null) + " " + txtDiscriminator.getText(),
						JOptionPane.WARNING_MESSAGE);
			}
		}
		else {
			// No problem found
			btnCheckRows.setBackground(standardButtonBackground);
			if (interactive) {
				// Report the absence of detected problems
				JOptionPane.showMessageDialog(this, 
						msgNoProblems.getText(),
						ElementNames.getElementName('c', false, null) + " " + txtDiscriminator.getText(),
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	/**
	 * Inserts new rows for missing enumerator values among the selectors and
	 * replaces code literals in existing lines by the respective enumerator
	 * constant names.
	 * 
	 * @param tm - TableModel of {@link #tblSelectors}
	 */
	private void forceEnumeratorCompleteness(DefaultTableModel tm) {
		int nRows = tm.getRowCount();
		btnEnumAssist.setEnabled(false);
		HashMap<String, Integer> enumVals = valueHelper.getEnumeratorInfo(txtDiscriminator.getText());
		if (enumVals != null) {
			// For efficiency reasons, we set up an inverse map (code replacements)
			HashMap<Integer, String> invMap = new HashMap<Integer, String>();
			for (Map.Entry<String, Integer> enumEntry: enumVals.entrySet()) {
				if (!invMap.containsKey(enumEntry.getValue())) {
					invMap.put(enumEntry.getValue(), enumEntry.getKey());
				}
			}
			
			tblSelectors.clearSelection();
			// Phase 1: Analyse the occurring enumeration values and replace codes
			StringList lines = new StringList();
			for (int i = 0; i < nRows; i++) {
				String line = (String)tm.getValueAt(i, 1);
				lines.add(line);
				// Now replace all code literals with the respective constant name
				StringList exprs = Element.splitExpressionList(line.replace("\\n", " "), ",", true);
				boolean replaced = false;
				for (int j = 0; j < exprs.count(); j++) {
					String expr = exprs.get(j);
					if (expr.isEmpty()) {
						exprs.remove(j--);
						continue;
					}
					try {
						Integer code = Integer.valueOf(expr);
						String name = invMap.get(code);
						if (name != null) {
							exprs.set(j, name);
							replaced = true;
						}
					}
					catch (NumberFormatException exc) {}
				}
				if (replaced) {
					tm.setValueAt(exprs.concatenate(", "), i, 1);
				}
			}
			// Get the mapping of code values to row numbers
			HashMap<String, ArrayList<Integer>> values = valueHelper.checkValues(lines, false);
			long usedRows = 0;	// Bitset
			// Phase 2: Add rows with missing enumeration names
			for (Map.Entry<String, Integer> enumEntry: enumVals.entrySet()) {
				ArrayList<Integer> rowNos = values.get(enumEntry.getValue().toString());
				if (rowNos == null) {
					tm.addRow(new Object[] {"", enumEntry.getKey()});
				}
				else {
					for (Integer lNo: rowNos) {
						usedRows |= 1L << lNo;
					}
				}
			}
			// Phase 3: Remove redundant lines (only sensible among the old lines)
			for (int i = nRows-1; i >= 0; i--) {
				// Don't drop rows with non-empty branch
				if ((usedRows & (1L << i)) == 0 && "".equals(tm.getValueAt(i, 0))) {
					tm.removeRow(i);
				}
			}
			// Phase 4: Deselect the default branch if it is empty
			if (chkDefaultBranch.isSelected()
					&& (branchOrder == null || branchOrder[branchOrder.length-1] == 0)) {
				chkDefaultBranch.setSelected(false);
			}
			adjustTableSize();
			checkRows(tm, false);
			//tblSelectors.setRowSelectionInterval(nRows, nRows);
		}
	}
	
	/**
	 * Controls whether the magic wand button is to be enabled (and does so
	 * in this case).
	 * 
	 * @param tm - TableModel of {@link #tblSelectors} or {@code null}
	 */
	private void checkEnumButton(DefaultTableModel tm) {
		if (tm == null) {
			tm = (DefaultTableModel)tblSelectors.getModel();
		}
		btnEnumAssist.setEnabled(false);
		HashMap<String, Integer> enumVals = valueHelper.getEnumeratorInfo(txtDiscriminator.getText());
		if (enumVals != null) {
			StringList lines = new StringList();
			int nRows = tm.getRowCount();
			for (int i = 0; i < nRows; i++) {
				// FIXME could mutilate string literals containing "\n" --> We must tokenize
				Object value = tm.getValueAt(i, 1);
				// Could be null during initialisation!
				if (value != null) {
					
					lines.add((String)value);
				}
			}
			HashMap<String, ArrayList<Integer>> values = valueHelper.checkValues(lines, false);
			for (Map.Entry<String, Integer> enumEntry: enumVals.entrySet()) {
				if (!values.containsKey(enumEntry.getValue().toString())) {
					btnEnumAssist.setEnabled(true);
					break;
				}
			}
		}
	}

	// This is a subclassed method of InputBox called before being set visible
	@Override
	public void checkConsistency() {
		// First we try to obtain the StringList of unbroken lines...
		StringList text = StringList.explode(txtText.getText(), "\n");
		int i = 0;
		while (i < text.count()-1) {
			if (text.get(i).endsWith("\\")) {
				// We compose the line with the next one but insert a visible "\n"
				text.set(i, text.get(i) + "n" + text.get(i+1));
				text.remove(i+1);
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
		// In general, but for certain TRY elements (see above), remove the default line
		if (nLines > 1) {
			text.remove(--nLines);
		}
		maxBranch = nLines - 1;	// Retain the default branch number
		DefaultTableModel tm = (DefaultTableModel) tblSelectors.getModel();
		tm.setRowCount(0);
		Vector<String> validBranchNumbers = getUnusedBranchNumbers(-1);
		for (i = 1; i < nLines; i++) {
			String branchNoStr = Integer.toString(i);
			if (!validBranchNumbers.contains(branchNoStr)) {
				branchNoStr = "";
			}
			tm.addRow(new Object[] {branchNoStr, text.get(i)});
		}
		adjustTableSize();
		if (!forInsertion) {
			chkMoveBranches.setEnabled(true);
			chkMoveBranches.setSelected(true);
		}
		
		valueChanged(null);
		
		scrText.setVisible(false);
		
		checkEnumButton(tm);
		checkRows(tm, false);
	}
	
	// Gets called on selecting/unselecting the checkbox for the default branch
	@Override
	public void itemStateChanged(ItemEvent iev) {
		if (iev.getSource() == chkDefaultBranch)
		{
			txtDefaultLabel.setEnabled(iev.getStateChange() == ItemEvent.SELECTED);
			if (txtDefaultLabel.isEnabled() && txtDefaultLabel.getText().trim().isEmpty()) {
				txtDefaultLabel.setText("default");
			}
			checkRows(null, false);
		}
	}

	// Typically called when a table cell is worked on.
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == tblSelectors
				&& "tableCellEditor".equals(evt.getPropertyName())) {
			Object cellEditor = evt.getNewValue();
			if (cellEditor != null && cellEditor instanceof DefaultCellEditor) {
				// First ensure acceptable behaviour for scaled mode
				if (((DefaultCellEditor)cellEditor).getComponent() != null) {
					((DefaultCellEditor)cellEditor).getComponent().setFont(tblSelectors.getFont());
					((DefaultCellEditor)cellEditor).getComponent().requestFocusInWindow();
				}
				ixEdit = tblSelectors.getSelectedRow();
				btnOK.setEnabled(false);
				btnAddRow.setEnabled(false);
				btnDelRows.setEnabled(false);
				btnUpRow.setEnabled(false);
				btnDnRow.setEnabled(false);
				btnMergeRows.setEnabled(false);
				btnSplitRow.setEnabled(false);
				btnEnumAssist.setEnabled(false);
			}
			else {
				adjustTableSize();
				checkEnumButton(null);
				if (ixEdit >= 0 && ixEdit < tblSelectors.getRowCount()) {
					String selector = (String)tblSelectors.getValueAt(ixEdit, 1);
					if (selector == null || selector.trim().isEmpty()) {
						JOptionPane.showMessageDialog(this,
								msgSelectorMustNotBeEmpty .getText(),
								selector, JOptionPane.WARNING_MESSAGE);
					}
				}
				ixEdit = -1;	// Editing ended
				doButtons();
				this.checkRows(null, false);
				btnOK.setEnabled(true);
			}
		}
	}

	/**
	 * Adjusts the actual tables size according to the number of rows and row
	 * widths. <br/>
	 * Will also ensure a sensible scroll pane width and the horizontal scrollbar.
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

	// Called e.g. on selection changes on tblSelectors
	@Override
	public void valueChanged(ListSelectionEvent evt) {
		doButtons();
	}

	/**
	 * Cares for enabling/disabling the table row selection related buttons.
	 */
	private void doButtons() {
		int[] ixSelected = tblSelectors.getSelectedRows();
		int nRows = tblSelectors.getRowCount();
		btnAddRow.setEnabled(true);
		btnDelRows.setEnabled(ixSelected.length > 0 && (nRows > 1 || this.elementType.equalsIgnoreCase("try") && nRows > 0));
		btnUpRow.setEnabled(ixSelected.length > 0 && ixSelected[0] > 0);
		btnDnRow.setEnabled(ixSelected.length > 0 && ixSelected[ixSelected.length-1] < nRows-1);
		boolean canMerge = ixSelected.length > 1;
		if (canMerge) {
			// Check branch association
			String branchNo = "";
			for (int i = 0; i < ixSelected.length; i++) {
				Object numStr = tblSelectors.getValueAt(ixSelected[i], 0);
				if (numStr instanceof String && !((String) numStr).trim().isEmpty()) {
					if (!branchNo.isEmpty() && !branchNo.equals(numStr)) {
						// This is the second involved non-empty branch: no way to merge
						canMerge = false;
						break;
					}
					// This is the first involved non-empty branch
					branchNo = (String)numStr;
				}
			}
		}
		btnMergeRows.setEnabled(canMerge);
		boolean canSplit = ixSelected.length == 1;
		if (canSplit) {
			Object val = tblSelectors.getValueAt(ixSelected[0], 1);
			if (val instanceof String) {
				StringList exprs = Element.splitExpressionList((String)val, ",");
				canSplit = exprs.count() > 1;
			}
		}
		btnSplitRow.setEnabled(canSplit);
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
	 * Adapts the width and height of the scroll pane associated to {@link #tblSelectors}
	 * according to the current width of the left part of the top pane and the
	 * dialog width, and regarding the extra height of the dialog wrt to the
	 * preferred height (we ensure that the table scroll pane gets about an 7/8
	 * share of the extra height).<br/>
	 * The width mechanism is a precarious workaround for the lacking auto
	 * resizing of a JTable ScrollPane within a {@link GridBagLayout}: If the
	 * new size is a little to wide then the layout will collapse, if it is a
	 * little too narrow then it will look ugly.
	 */
	private void resizeTableScrollPane() {
		double scaleFactor = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
		if (scaleFactor < 1) scaleFactor = 1.0;
		int border = (int)(5 * scaleFactor);
		Dimension dialogSize = getSize();
		Dimension pnlSize = pnlSelectorControl.getPreferredSize();
		int diff = dialogSize.width - pnlSize.width - 4 * border;
		int extraHeight = Math.max(0, dialogSize.height - (int)(scaleFactor * IBC_PREFERRED_HEIGHT));
		int newTableHeight = (int)(scaleFactor * TABLE_SIZE[1]) + 7 * extraHeight / 8;
		scrSelectors.setPreferredSize(new Dimension((int)(diff - 25 * scaleFactor), (int)(newTableHeight)));
		adjustTableSize();
		// FIXME: Temporary version hint
		String lastHint = Ini.getInstance().getProperty("versionHint", "");
		if (!tempHintGiven && lastHint.compareTo("3.30-15") < 0) {
			tempHintGiven = true;
			String message = ElementNames.resolveElementNames(
					Menu.msgVersionHint_3_30_15.getText()
					.replace("%1", Locales.getValue("Structorizer", "Preferences.lblCaseEditor.text", rootPaneCheckingEnabled))
					.replace("%2", Locales.getValue("Structorizer", "Preferences.title", true)),
					null);
			JOptionPane.showMessageDialog(this.getOwner(),
					message, this.getClass().getSimpleName(),
					JOptionPane.INFORMATION_MESSAGE,
					IconLoader.getIconImage(getClass().getResource("icons/EditorHint_3.30-15.png")));
				
			Ini.getInstance().setProperty("versionHint", "3.30-15");
		}
	}

	// START KGU#927 2021-02-06: Enh. #915
	/** 
	 * Retrieves branches that are not associated to a table line.
	 * 
	 * @param rowNumber - the row index if called from a cell editor, otherwise -1
	 * @return the (sorted) vector of branch numbers of non-empty branches
	 *    without current bond to a selector line.
	 */
	private Vector<String> getUnusedBranchNumbers(int rowNumber)
	{
		Vector<String> numbers = new Vector<String>();
		numbers.add("");
		HashSet<String> usedNumbers = new HashSet<String>();
		for (int j = 0; j < tblSelectors.getRowCount(); j++) {
			// The branch number of the passed row is to be seen as available
			if (j != rowNumber) {
				usedNumbers.add((String)tblSelectors.getValueAt(j, 0));
			}
		}
		if (branchOrder != null) {
			for (int i = 0; i < Math.min(maxBranch, branchOrder.length-1); i++) {
				int branchNo = branchOrder[i];
				String branchNoStr = Integer.toString(branchNo);
				if (branchNo > 0 && !usedNumbers.contains(branchNoStr)) {
					numbers.add(branchNoStr);
				}
			}
			// Check default branch number
			if (maxBranch < branchOrder.length) {
				int branchNo = branchOrder[maxBranch];
				if (branchNo > 0) {
					String branchNoStr = Integer.toString(branchNo);
					if (!chkDefaultBranch.isSelected() 
							&& !usedNumbers.contains(branchNoStr)) {
						numbers.add(branchNoStr);
					}
				}
			}
		}
		return numbers;
	}
	
	// Three document listener methods for txtDiscriminator
	@Override
	public void insertUpdate(DocumentEvent e) {
		checkEnumButton((DefaultTableModel)tblSelectors.getModel());
//		discriminatorModified = true;
		checkRows(null, false);	// We react immediately now, should be fast enough
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		checkEnumButton((DefaultTableModel)tblSelectors.getModel());
//		discriminatorModified = true;
		checkRows(null, false);	// We react immediately now, should be fast enough
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		checkEnumButton((DefaultTableModel)tblSelectors.getModel());
//		discriminatorModified = true;
		checkRows(null, false);	// We react immediately now, should be fast enough
	}
	// END KGU#927 2021-02-06
	
}

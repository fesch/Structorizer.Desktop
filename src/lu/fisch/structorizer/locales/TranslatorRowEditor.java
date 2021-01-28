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
package lu.fisch.structorizer.locales;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Editor for a locale entry, particularly for very long messages
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-06-06      First Issue (addressing issue #726)
 *      Kay Gürtzig     2921-01-27      Issue #914, #919: Undo/Redo management added.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import lu.fisch.structorizer.gui.ElementNames;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.io.Ini;

/**
 * Translator sub-dialog for editing a locale entry, particularly for very long messages
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class TranslatorRowEditor extends JDialog implements ActionListener, ItemListener {
	
	private static final int TEXT_AREA_HEIGHT = 75;
	private static final int N_TEXT_AREAS = 3;
	
	private HashMap<ImageIcon, String> locales = new HashMap<ImageIcon, String>();
	private Translator translator;
	private String key;
	private String section;
	private String subsection;
	private String lang;
	private String text;
	private boolean committed = false;
	private boolean multilineMode = false;
	private static String lastCompLang = null;
	
	private JLabel lblSection;
	private JLabel lblKey;
	private JLabel lblCond;
	private JLabel lblDefault;
	private JLabel lblTarget;
	private JTextField txtSection;
	private JTextField txtKey;
	private JTable tblConditions;
	private JTextArea[] txtAreas = new JTextArea[N_TEXT_AREAS];
	private JComboBox<ImageIcon> cmbLanguage;
	private JCheckBox chkWrapLines;
	private JButton btnElements;
	private JToggleButton btnPreview;
	private JButton btnReset;
	private JButton btnCancel;
	private JButton btnOK;
	/** Unsubstituted texts in preview mode, never with true newlines */
	private String[] cachedTexts = new String[N_TEXT_AREAS];
	// START KGU#915 2021-01-28: Enh. #914: Undo manager
	private UndoManager undoManager = new UndoManager();
	// END KGU#915 202101-28

	/**
	 * @param owner
	 * @param title
	 * @param modal
	 */
	public TranslatorRowEditor(Translator owner, JButton button, String localeName, String text) {
		super(owner, true);
		setIconImage(IconLoader.getIcon(0).getImage());
		String[] details = button.getName().split(":", 2);
		this.translator = owner;
		this.section = details[0];
		this.key = details[1];
		this.subsection = key.split("\\.", 2)[0];
		this.text = text;
		this.lang = localeName;
		setTitle("Structorizer Translator: " + this.subsection);
		this.multilineMode = Ini.getInstance().getProperty("TranslatorWrap", "0").equals("1");
		initComponents();
	}


	void initComponents()
	{
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
		int border = (int)(5 * scaleFactor);
		// =================== Header ===========================
		JPanel pnlHeader = new JPanel();
		GroupLayout lytHeader = new GroupLayout(pnlHeader);
		pnlHeader.setLayout(lytHeader);
		pnlHeader.setBorder(new EmptyBorder(border, border, border, border));

		lblSection = new JLabel("Section");
		lblKey = new JLabel("Key");
		lblCond = new JLabel("Conditions");
		txtSection = new JTextField(this.section);
		txtKey = new JTextField(this.key);
		txtSection.setEnabled(false);
		txtKey.setEnabled(false);
		int posCond = this.key.indexOf('[');
		if (posCond >= 0) {
			txtKey.setText(this.key.substring(0, posCond));
			// FIXME: A malformed key sequence might case harm here
			String txtCond = this.key.substring(posCond+1, this.key.indexOf(']'));
			String[] conds = txtCond.split(",");
			tblConditions = new JTable(0, 2);
			DefaultTableModel model = (DefaultTableModel)tblConditions.getModel();
			for (String cond: conds) {
				model.addRow(cond.split(":", 2));
			}
		}
		else {
			tblConditions = new JTable();
			lblCond.setVisible(false);
			tblConditions.setVisible(false);
		}
		tblConditions.setEnabled(false);
		
		lytHeader.setHorizontalGroup(lytHeader.createSequentialGroup().
				addGroup(lytHeader.createParallelGroup().
						addComponent(lblSection).
						addComponent(lblKey).
						addComponent(lblCond)).
				addGap(5).
				addGroup(lytHeader.createParallelGroup().
						addComponent(txtSection).
						addComponent(txtKey).
						addComponent(tblConditions)));
		lytHeader.setVerticalGroup(lytHeader.createSequentialGroup().
				addGroup(lytHeader.createParallelGroup().
						addComponent(lblSection).
						addComponent(txtSection)).
				addGap(5).
				addGroup(lytHeader.createParallelGroup().
						addComponent(lblKey).
						addComponent(txtKey)).
				addGap(5).
				addGroup(lytHeader.createParallelGroup().
						addComponent(lblCond).
						addComponent(tblConditions)));
		
		// ================= Text Areas ==========================
		JPanel pnlText = new JPanel();
		GroupLayout lytText = new GroupLayout(pnlText);
		pnlText.setLayout(lytText);
		pnlText.setBorder(new EmptyBorder(border, border, border, border));
		
		lblDefault = new JLabel(IconLoader.getLocaleIconImage(Locales.DEFAULT_LOCALE));
		lblTarget = new JLabel(IconLoader.getLocaleIconImage(this.lang));
		JTextArea txtDefault = txtAreas[0] = new JTextArea();
		JTextArea txtTarget = txtAreas[1] = new JTextArea(this.text);
		cmbLanguage = new JComboBox<ImageIcon>();
		JTextArea txtCompar = txtAreas[2] = new JTextArea();
		Locale loc0 = Locales.getInstance().getLocale(Locales.DEFAULT_LOCALE);
		txtDefault.setText(loc0.getValue(this.section, this.key));
		ImageIcon lastCompIcon = null;
		ImageIcon icon = IconLoader.getLocaleIconImage("empty");
		locales.put(icon, "empty");
		cmbLanguage.addItem(icon);
		for (String[] localePair : Locales.LOCALES_LIST) {
			String localeName = localePair[0];
			if (localeName.equals("empty")) {
				break;
			}
			else if (!localeName.equals(Locales.DEFAULT_LOCALE)) {
				icon = IconLoader.getLocaleIconImage(localeName);
				locales.put(icon, localeName);
				cmbLanguage.addItem(icon);
				if (localeName.equals(lastCompLang)) {
					lastCompIcon = icon;
				}
			}
		}
		Dimension prefSize = cmbLanguage.getPreferredSize();
		cmbLanguage.setMaximumSize(prefSize);
		txtDefault.setEditable(false);
		txtCompar.setEditable(false);		
		
		JScrollPane scrDefault = new JScrollPane(txtDefault);
		JScrollPane scrTarget = new JScrollPane(txtTarget);
		JScrollPane scrCompar = new JScrollPane(txtCompar);
		int textAreaHeight = (int)(TEXT_AREA_HEIGHT * scaleFactor);
		scrDefault.setPreferredSize(new Dimension(scrDefault.getPreferredSize().width, textAreaHeight));
		scrTarget.setPreferredSize(new Dimension(scrTarget.getPreferredSize().width, textAreaHeight));
		scrCompar.setPreferredSize(new Dimension(scrCompar.getPreferredSize().width, textAreaHeight));
		
		lytText.setVerticalGroup(lytText.createSequentialGroup().
				addComponent(lblDefault).
				addComponent(scrDefault).
				addGap(5).
				addComponent(lblTarget).
				addComponent(scrTarget).
				addGap(5).
				addComponent(cmbLanguage).
				addComponent(scrCompar));
		lytText.setHorizontalGroup(lytText.createParallelGroup().
				addComponent(lblDefault).
				addComponent(scrDefault).
				addComponent(lblTarget).
				addComponent(scrTarget).
				addComponent(cmbLanguage).
				addComponent(scrCompar)
				);
		
		cmbLanguage.addActionListener(this);
		if (lastCompIcon != null) {
			cmbLanguage.setSelectedItem(lastCompIcon);
		}
		
		// ================= Button Bar =========================
		JPanel buttonBar = new JPanel();
		buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));
		buttonBar.setBorder(new EmptyBorder(border, border, border, border));
		
		chkWrapLines = new JCheckBox("Wrap Lines");
		btnElements = new JButton("Elements");
		btnPreview = new JToggleButton("Preview");
		btnReset = new JButton("Reset");
		btnCancel = new JButton("Cancel");
		btnOK = new JButton("Commit");

		chkWrapLines.addActionListener(this);
		btnElements.addActionListener(this);
		btnPreview.addItemListener(this);
		btnReset.addActionListener(this);
		btnCancel.addActionListener(this);
		btnOK.addActionListener(this);
		
		if (this.multilineMode) {
			chkWrapLines.doClick();
		}
		
		buttonBar.add(chkWrapLines);
		buttonBar.add(btnElements);
		buttonBar.add(btnPreview);
		buttonBar.add(btnReset);
		buttonBar.add(Box.createHorizontalGlue());
		buttonBar.add(btnCancel);
		buttonBar.add(btnOK);
		
		// ================= UndoManager =========================

		// START KGU#915 2021-01-28: Enh. #914
		Document docTarget = txtTarget.getDocument();
		docTarget.addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent evt) {
				if (txtAreas[1].isEditable()) {
					undoManager.addEdit(evt.getEdit());
					btnOK.setEnabled(true);
				}
			}});
		btnOK.setEnabled(false);
		// END KGU#915 2021-01-28
		
		// ================= KeyListener =========================
		KeyListener keyListener = new KeyListener() {

			public void keyPressed(KeyEvent evt) 
			{
				int keyCode = evt.getKeyCode();
				Object src = evt.getSource();
				if(keyCode == KeyEvent.VK_ESCAPE)
				{
					dispose();
				}
				else if(keyCode == KeyEvent.VK_ENTER && (evt.isShiftDown() || evt.isControlDown()))
				{
					// START KGU#915 2021-01-28: Enh. #914
					//committed = true;
					committed = btnOK.isEnabled() || undoManager.canUndo();
					// END KGU#915 2021-01-28
					Ini.getInstance().setProperty("TranslatorWrap", multilineMode ? "1" : "0");
					dispose();
				}
				// START KGU#915 2021-01-28: Enh. #914 We want undo/redo behaviour
				// (it is the only UndoManager, so we don't have to restrict to src)
				else if (/*src == txtAreas[1] &&*/ txtAreas[1].isEditable()) {
					if (keyCode == KeyEvent.VK_Z && evt.isControlDown() && !evt.isShiftDown()) {
					try {
						if (undoManager.canUndo()) {
							undoManager.undo();
						}
						else {
							btnOK.setEnabled(false);
						}
					}
					catch (CannotUndoException ex) {}
					}
					else if (keyCode == KeyEvent.VK_Y && evt.isControlDown() && !evt.isShiftDown()
							|| keyCode == KeyEvent.VK_Z && evt.isControlDown() && evt.isShiftDown()) {
						try {
							if (undoManager.canRedo()) {
								undoManager.redo();
								btnOK.setEnabled(true);
							}
						}
						catch (CannotRedoException ex) {}
					}
				}
				// END KGU#915 2021-0-28
			}

			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			
		};
		for (int i = 0; i < txtAreas.length; i++) {
			txtAreas[i].addKeyListener(keyListener);
		}
		cmbLanguage.addKeyListener(keyListener);
		chkWrapLines.addKeyListener(keyListener);
		btnElements.addKeyListener(keyListener);
		btnPreview.addKeyListener(keyListener);
		btnReset.addKeyListener(keyListener);
		btnCancel.addKeyListener(keyListener);
		btnOK.addKeyListener(keyListener);
		
		// ================= Tooltips ============================
		cmbLanguage.setToolTipText("By selecting a banner you may see the translation into the respective language for comparison (or to copy parts).");
		chkWrapLines.setToolTipText("Preview or enter newline characters directly. If unselected, newlines will be represented as '\\n'.");
		btnElements.setToolTipText("Shows the table of Element type placeholders and their current replacements.");
		btnReset.setToolTipText("Undoes all your changes to this message without closing the dialog.");
		btnPreview.setToolTipText("Shows all texts with resolved element name place holders (@...). Disables editing while selected.");
		btnCancel.setToolTipText("Closes the dialog without committing your changes (so does the <Esc> key).");
		btnOK.setToolTipText("Commits your changes and closes the dialog (so do key combinations <Ctrl><Enter> and <Shift><Enter>.");
		
		// ================= ContentPane =========================
		JPanel pnlContent = new JPanel();
		pnlContent.setLayout(new BorderLayout(5, 5));
		pnlContent.add(pnlHeader, BorderLayout.NORTH);
		pnlContent.add(pnlText, BorderLayout.CENTER);
		pnlContent.add(buttonBar, BorderLayout.SOUTH);
		this.getContentPane().add(pnlContent);
		this.pack();
		txtAreas[1].requestFocusInWindow();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == btnReset) {
			cachedTexts[1] = this.text;
			String content = this.text;
			if (this.multilineMode) {
				txtAreas[1].setText(this.text.replace("\\n", "\n"));
			}
			if (btnPreview.isSelected()) {
				content = ElementNames.resolveElementNames(content, this.translator.provideCurrentElementNames(this.lang, true));
			}
			txtAreas[1].setText(content);
			// START KGU#915 2021-01-28: Enh. #914
			undoManager.discardAllEdits();
			btnOK.setEnabled(false);
			// END KGU#915 2021-01-28
		}
		else if (evt.getSource() == btnElements) {
			this.showElementPlaceholders();
		}
		else if (evt.getSource() == btnOK) {
			// START KGU#915 2021-01-28: Enh. #914
			//committed = true;
			committed = btnOK.isEnabled() || undoManager.canUndo();
			// END KGU#915 2021-01-28
			Ini.getInstance().setProperty("TranslatorWrap", this.multilineMode ? "1" : "0");
			//this.setVisible(false);
			this.dispose();
		}
		else if (evt.getSource() == btnCancel) {
			//this.setVisible(false);
			this.dispose();
		}
		else if (evt.getSource() == chkWrapLines) {
			this.multilineMode = chkWrapLines.isSelected();
			// START KGU#915 2021-01-28: Enh. #914
			// Stop tracking undoable edits
			boolean wasEditable = txtAreas[1].isEditable();
			txtAreas[1].setEditable(false);
			// END KGU#915 2021-01-28
			for (int i = 0; i < txtAreas.length; i++) {
				String content = txtAreas[i].getText();
				if (this.multilineMode) {
					txtAreas[i].setText(content.replace("\\n", "\n"));
				}
				else {
					txtAreas[i].setText(content.replace("\n", "\\n"));
				}
			}
			// START KGU#915 2021-01-28: Enh. #914
			// Restore previous mode
			txtAreas[1].setEditable(wasEditable);
			// END KGU#915 2021-01-28
		}
		else if (evt.getSource() == cmbLanguage) {
			String langName = locales.get(cmbLanguage.getSelectedItem());
			if (langName != null) {
				Locale locale = Locales.getInstance().getLocale(langName);
				String value = null;
				if (locale != null && (value = locale.getValue(section, key)) != null) {
					if (this.multilineMode) {
						txtAreas[2].setText(value.replace("\\n", "\n"));
					}
					else {
						txtAreas[2].setText(value);
					}
				}
				lastCompLang = langName;
			}
		}
	}
	
	private void showElementPlaceholders() {
		String[] localeNames = new String[] {
				Locales.DEFAULT_LOCALE,
				this.lang,
				locales.get(cmbLanguage.getSelectedItem())
		};
		int [] colWidths = new int[] {5, 5, 5, 5, 5};
		String[] header = new String [] {
				"Short key",
				"Long key",
				Locales.DEFAULT_LOCALE,
				this.lang,
				locales.get(cmbLanguage.getSelectedItem())
		};
		// First get all the translations for the element names
		String[][] translations = new String[localeNames.length][];
		for (int j = 0; j < localeNames.length; j++) {
			translations[j] = this.translator.provideCurrentElementNames(localeNames[j], j != 0);
		}
		// Now build the table
		Object[][] content = new Object[ElementNames.ELEMENT_KEYS.length][colWidths.length];
		for (int i = 0; i < ElementNames.ELEMENT_KEYS.length; i++) {
			content[i][0] = "@" + (char)('a' + i);
			content[i][1] = "@{" + ElementNames.ELEMENT_KEYS[i] + "}";
			for (int j = 0; j < localeNames.length; j++) {
				content[i][2+j] = translations[j][i];
			}
		}
		JTable elementTable = new JTable(content, header);
		for (int j = 0; j < colWidths.length; j++) {
			TableColumn tabCol = elementTable.getTableHeader().getColumnModel().getColumn(j);
			TableCellRenderer renderer = tabCol.getHeaderRenderer();
			if (renderer == null) {
				renderer = elementTable.getTableHeader().getDefaultRenderer();
			}
			Component comp = renderer.getTableCellRendererComponent(elementTable,
					tabCol.getHeaderValue(), false, false, -1, j);
			colWidths[j] = Math.max(colWidths[j], comp.getPreferredSize().width);
		}
		for (int i = 0; i < ElementNames.ELEMENT_KEYS.length; i++) {
			for (int j = 0; j < colWidths.length; j++) {
				TableCellRenderer renderer = elementTable.getCellRenderer(i, j);
				Component comp = elementTable.prepareRenderer(renderer, i, j);
				colWidths[j] = Math.max(colWidths[j], comp.getPreferredSize().width);
			}
		}
		for (int j = 0; j < colWidths.length; j++) {
			//System.out.println("width col " + j + ": " + colWidths[j]);
			TableColumn column = elementTable.getColumnModel().getColumn(j);
			column.setMinWidth(colWidths[j]+3);
			column.setPreferredWidth(colWidths[j]+3);
		}
		elementTable.setRowHeight((int)(elementTable.getRowHeight() * Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"))));
		//JScrollPane elementPane = new JScrollPane(elementTable);
		//elementPane.setMaximumSize(elementTable.getPreferredSize());
		JPanel elementPane = new JPanel();
		elementPane.setLayout(new BoxLayout(elementPane, BoxLayout.Y_AXIS));
		elementPane.add(elementTable.getTableHeader());
		elementPane.add(elementTable);
		//elementTable.setEnabled(false);
		elementTable.setDefaultEditor(Object.class, null);
		JOptionPane.showMessageDialog(this, elementPane, "Element place holders", JOptionPane.INFORMATION_MESSAGE);
	}


	/**
	 * @return whether changes have been committed
	 */
	public boolean isCommitted()
	{
		return this.committed;
	}
	
	/**
	 * @return the current translation (make sure the changes have been committed!)
	 * @see #isCommitted()
	 */
	public String getText()
	{
		String translated = this.txtAreas[1].getText();
		if (btnPreview.isSelected() && cachedTexts[1] != null) {
			// The cached text is never in multi-line mode
			translated = cachedTexts[1];
		}
		else if (this.multilineMode) {
			translated = translated.replace("\n", "\\n");
		}
		return translated;
	}


	@Override
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() == btnPreview) {
			String[] localeNames = new String[] {
					Locales.DEFAULT_LOCALE,
					this.lang,
					locales.get(cmbLanguage.getSelectedItem())
			};
			switch (evt.getStateChange()) {
			case ItemEvent.SELECTED:
				txtAreas[1].setToolTipText("While the \"Preview\" mode is active you can't edit this area.");
				txtAreas[1].setEditable(false);
				btnReset.setEnabled(false);
				for (int i = 0; i < N_TEXT_AREAS; i++) {
					String content = this.txtAreas[i].getText();
					this.cachedTexts[i] = content.replace("\n", "\\n");
					this.txtAreas[i].setText(ElementNames.resolveElementNames(
							content, this.translator.provideCurrentElementNames(localeNames[i], i != 0)
							));
				}
				btnReset.setEnabled(true);
				
				break;
			case ItemEvent.DESELECTED:
				btnReset.setEnabled(false);
				for (int i = 0; i < N_TEXT_AREAS; i++) {
					String content = this.cachedTexts[i];
					if (content != null) {
						if (this.multilineMode) {
							content = content.replace("\\n", "\n");
						}
						this.txtAreas[i].setText(content);
					}
				}
				txtAreas[1].setToolTipText(null);
				txtAreas[1].setEditable(true);
				btnReset.setEnabled(true);
				break;
			}
		}
	}
	
}

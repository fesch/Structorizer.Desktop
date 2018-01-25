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
 *      Author:         Kay Gürtzig
 *
 *      Description:    Simple substring search dialog for Translator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.12.11      First Issue (for enh.req. #425)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuListener;

import lu.fisch.structorizer.gui.IconLoader;

/**
 * Little search dialog for the Translator tool
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class TranslatorFindDialog extends JDialog {
	/** pattern history limitation */
	private static final int MAX_RECENT_PATTERNS = 10;
	/** little trick to achieve a sufficient text box width on packing the dialog */
	private static final String PATTERN_PROTOTYPE = "A string defining the preferred combobox width";
	private final JLabel lblFind = new JLabel("Find:");
	private final JLabel lblColumns = new JLabel("Columns:");
	private final JComboBox<String> cbbPattern = new JComboBox<String>();
	private final JButton btnPrev = new JButton();
	private final JButton btnNext = new JButton();
	private final JCheckBox chkWrapAround = new JCheckBox("Wrap around");
	private final JCheckBox chkCaseSensitive = new JCheckBox("Case-sensitive");
	private final JCheckBox chkColumns[] = {
			new JCheckBox("1"),
			new JCheckBox("2"),
			new JCheckBox("3")
	};
	private boolean fillingComboBox = false;
	private KeyListener cmbKeyListener;
	private PopupMenuListener cmbPopupListener;
	private LinkedList<String> searchPatterns = new LinkedList<String>();
	
	TranslatorFindDialog(Frame owner)
	{
		super(owner);
		initComponents();
	}

	private void initComponents() {
		this.setTitle("Find String");
		ActionListener buttonListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt) {
				searchPattern(evt.getSource() == btnNext);				
			}
		};

		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent evt) 
			{
				if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {}
		};
		
		cmbKeyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent evt) 
			{
				if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					// Hide the window if Esc key is pressed without the pulldown list being visible
					// (otherwise just close the pulldown)
					JComboBox<String> box = cbbPattern;
					if (!box.isPopupVisible()) {
						setVisible(false);
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent arg0) {}
		};
		
		ChangeListener chkColListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				boolean anyColumn = false;
				for (JCheckBox box: chkColumns) {
					if (box.isSelected()) {
						anyColumn = true;
						break;
					}
				}
				btnNext.setEnabled(anyColumn);
				btnPrev.setEnabled(anyColumn);
			}
		};

		cbbPattern.setEditable(true);
		cbbPattern.setPrototypeDisplayValue(PATTERN_PROTOTYPE);
		cbbPattern.getEditor().getEditorComponent().addKeyListener(cmbKeyListener);
		cbbPattern.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent evt) {
				patternChanged(evt);
			}
		});
		cbbPattern.addPopupMenuListener(cmbPopupListener);
		btnNext.setIcon(IconLoader.ico114);
		btnPrev.setIcon(IconLoader.ico115);
		btnNext.addActionListener(buttonListener);
		btnPrev.addActionListener(buttonListener);
		btnNext.addKeyListener(keyListener);
		btnPrev.addKeyListener(keyListener);
		chkWrapAround.addKeyListener(keyListener);
		chkCaseSensitive.addKeyListener(keyListener);
		for (JCheckBox box: chkColumns) {
			box.addKeyListener(keyListener);
			box.addChangeListener(chkColListener);
			box.setSelected(true);
		}
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		JPanel pnlFind = new JPanel();
		JPanel pnlCols = new JPanel();
		pnlFind.setLayout(new GridBagLayout());
		pnlCols.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints(
				1, 1,
				1, 1,
				0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0,0,0,0),
				0, 0);
		pnlFind.add(lblFind, gbc);
		gbc.gridx++;
		pnlFind.add(cbbPattern, gbc);
		gbc.gridx++;
		pnlFind.add(btnPrev, gbc);
		gbc.gridx++;
		pnlFind.add(chkWrapAround, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		pnlFind.add(lblColumns, gbc);
		
		GridBagConstraints gbcCols = new GridBagConstraints(
				1, 1,
				1, 1,
				0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0,0,0,0),
				4, 0);
		for (JCheckBox box: chkColumns) {
			gbcCols.gridx++;
			pnlCols.add(box, gbcCols);
		}
		gbc.gridx++;
		pnlFind.add(pnlCols, gbc);
		
		gbc.gridx++;
		pnlFind.add(btnNext, gbc);
		gbc.gridx++;
		pnlFind.add(chkCaseSensitive, gbc);
		
		contentPane.add(pnlFind, BorderLayout.NORTH);
		pack();
	}

	/**
	 * This is the search action method, delegating the actual work to the Translator
	 * instance.
	 * @param _forward - whether the search task is directed forwards (down)
	 */
	protected void searchPattern(boolean _forward) {
		// Now we need to access Translator
		boolean[] colSelection = new boolean[chkColumns.length];
		for (int i = 0; i < chkColumns.length; i++) {
			colSelection[i] = chkColumns[i].isSelected();
		}
		Translator.getInstance().gotoNextMatch(
				(String)cbbPattern.getSelectedItem(),
				_forward,
				chkWrapAround.isSelected(),
				chkCaseSensitive.isSelected(),
				colSelection);
	}

	/**
	 * Item state change listener method for both the pattern combo boxes.
	 * Ensures the new selected item is cached in the history
	 * @param evt - the inducing event
	 */
	protected void patternChanged(ItemEvent evt) {
		Object comp = evt.getSource();
		if (fillingComboBox || !(comp instanceof JComboBox<?>)) return;	// Avoid stack overflow
		
		@SuppressWarnings("unchecked")
		JComboBox<String> box = (JComboBox<String>)comp;
		if (box.isPopupVisible()) {
			// Wait with updates until the popup gets closed. 
			return;
		}
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			updatePatternList(box);
		}
	}

	/**
	 * Updates the pattern history for the originating {@link JComboBox} {@code box}
	 * and resets the find results if {@code box} is the combo box for the search patterns.
	 * @param box - the originating {@link JComboBox} 
	 */
	private void updatePatternList(JComboBox<String> box) {
		String item = (String)box.getEditor().getItem();
		if (searchPatterns.isEmpty() || !item.equals(searchPatterns.getFirst())) {
			ListIterator<String> iter = searchPatterns.listIterator();
			boolean found = false;
			while (!found && iter.hasNext()) {
				found = item.equals(iter.next());
				if (found) {
					iter.remove();
				}
			}
			if (!found && searchPatterns.size() >= MAX_RECENT_PATTERNS) {
				searchPatterns.removeLast();
			}
			searchPatterns.addFirst(item);
			this.refillPatternCombos(box);
			box.setSelectedItem(item);
		}
	}

	/**
	 * Refreshes the choice list of the given {@link JComboBox} {@code box} from
	 * the cached history for that combobox.
	 * @param _box the target combo-box or null if both comboboxes are to be filled.
	 */
	private void refillPatternCombos(JComboBox<String> _box)
	{
		// Recursion protection
		fillingComboBox = true;
		// Cater for the search pattern box
		if (_box == null || _box == cbbPattern) {
			cbbPattern.removeAllItems();
			cbbPattern.addItem("");
			for (String pattern: searchPatterns) {
				if (!pattern.isEmpty()) {
					cbbPattern.addItem(pattern);
				}
			}
		}
		fillingComboBox = false;
	}
	
	/**
	 * Ensures that certain listeners on LaF-specific components don't get lost by a
	 * Look & Feel change.
	 */
	public void adaptToNewLaF()
	{
		if (cmbKeyListener != null) {
			if (cbbPattern != null) {
				cbbPattern.getEditor().getEditorComponent().addKeyListener(cmbKeyListener);
			}
			if (cbbPattern != null) {
				cbbPattern.getEditor().getEditorComponent().addKeyListener(cmbKeyListener);
			}
		}
		pack();
	}
}

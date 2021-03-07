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
package lu.fisch.structorizer.gui;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Dialog class for dynamically retrieved option configuration.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-06-20      First Issue
 *      Kay Gürtzig     2018-01-22      Issue #484: Layout modified such that text fields will get all remaining width
 *                                      Moreover, bug in item list processing fixed.
 *      Kay Gürtzig     2021-03-07      KGU#961: Preparations for option localisation, combined with
 *                                      subtle layout improvements
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - Class is used by ExportOptionDialog and ImportOptionDialog to present plugin options
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.helpers.GENPlugin;

import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.utils.StringList;

/**
 * Dialog class for dynamically retrieved option configuration.
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class PluginOptionDialog extends LangDialog {
	
	private static final int TEXT_FIELD_WIDTH = 20;	// Default initial width of generic text fields
	private GENPlugin plugin;
	public HashMap<String, String> optionVals;
	protected JButton btnOk = new JButton("OK");
	protected JButton btnCancel = new JButton("Cancel");
	protected JCheckBox[] checkboxes = null;
	protected JTextField[] textFields = null;
	protected JLabel[] comboLabels = null;
	protected JComboBox<String>[] comboBoxes = null;
	protected HashMap<String, JComponent> optionComponents = new HashMap<String, JComponent>();
	// START KGU#961 2021-03-07: Complementary approach to allow option translations
	protected HashMap<String, JComponent> optionLabels = new HashMap<String, JComponent>();
	// END KGU#961 2021-03-07
	protected final LangTextHolder msgNoValidInteger = new LangTextHolder("Value for '%' must be an integral number!");
	protected final LangTextHolder msgNoValidUnsigned = new LangTextHolder("Value for '%' must be a cardinal number (unsigned integer)!");
	protected final LangTextHolder msgNoValidCharacter = new LangTextHolder("Value for '%' must be a single character!");
	protected final LangTextHolder msgNoValidDouble = new LangTextHolder("Value for '%' must be a number");
	protected final LangTextHolder msgVerificationError = new LangTextHolder("Input verification failed");
	
	PluginOptionDialog(GENPlugin _plugin, HashMap<String, String> _optionValues)
	{
		plugin = _plugin;
		optionVals = _optionValues;
		initComponents();
		setModal(true);
	}

	private void initComponents() {
		
		InputVerifier charVerifier = new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				boolean verified = ((JTextField)input).getText().length() <= 1;
				if (!verified) {
					String title = "";
					for (HashMap<String, String> optionSpec: plugin.options) {
						if (input == optionComponents.get(optionSpec.get("name"))) {
							title = optionSpec.get("title");
							break;
						}
					}
					JOptionPane.showMessageDialog(input,
							msgNoValidCharacter.getText().replace("%", title),
							msgVerificationError.getText(), 
							JOptionPane.ERROR_MESSAGE);
				}
				return verified;
			}
		};
		InputVerifier intVerifier = new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				boolean verified = false;
				String text = ((JTextField)input).getText();
				if (text.isEmpty()) {
					verified = true;
				}
				else {
					try {
						Integer.parseInt(text);
						verified = true;
					}
					catch (NumberFormatException ex) {
						String title = "";
						for (HashMap<String, String> optionSpec: plugin.options) {
							if (input == optionComponents.get(optionSpec.get("name"))) {
								title = optionSpec.get("title");
								break;
							}
						}
						JOptionPane.showMessageDialog(input,
								msgNoValidInteger.getText().replace("%", title),
								msgVerificationError.getText(), 
								JOptionPane.ERROR_MESSAGE);
					}
				}
				return verified;
			}
		};
		InputVerifier uintVerifier = new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				boolean verified = false;
				String text = ((JTextField)input).getText();
				if (text.isEmpty()) {
					verified = true;
				}
				else {
					try {
						Integer.parseUnsignedInt(text);
						verified = true;
					}
					catch (NumberFormatException ex) {
						String title = "";
						for (HashMap<String, String> optionSpec: plugin.options) {
							if (input == optionComponents.get(optionSpec.get("name"))) {
								title = optionSpec.get("title");
								break;
							}
						}
						JOptionPane.showMessageDialog(input,
								msgNoValidUnsigned.getText().replace("%", title),
								msgVerificationError.getText(), 
								JOptionPane.ERROR_MESSAGE);
					}
				}
				return verified;
			}
		};
		InputVerifier doubleVerifier = new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				boolean verified = false;
				String text = ((JTextField)input).getText();
				if (text.isEmpty()) {
					verified = true;
				}
				else {
					try {
						Double.parseDouble(text);
						verified = true;
					}
					catch (NumberFormatException ex) {
						String title = "";
						for (HashMap<String, String> optionSpec: plugin.options) {
							if (input == optionComponents.get(optionSpec.get("name"))) {
								title = optionSpec.get("title");
								break;
							}
						}
						JOptionPane.showMessageDialog(input,
								msgNoValidDouble.getText().replace("%", title),
								msgVerificationError.getText(), 
								JOptionPane.ERROR_MESSAGE);
					}
				}
				return verified;
			}
		};
		
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent kevt) 
			{
				if(kevt.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				else if(kevt.getKeyCode() == KeyEvent.VK_ENTER && (kevt.isShiftDown() || kevt.isControlDown()))
				{
					commitChanges();
					setVisible(false);
				}
			}
			
			public void keyReleased(KeyEvent kevt) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		
		// ====================== OPTIONS ======================
		JPanel pnlOptions = new JPanel();
		pnlOptions.setBorder(new EmptyBorder(5,5,5,5));
		// START KGU#472 2018-01-22: Issue #484 - We may prefer a GridBagLayout
		//pnlOptions.setLayout(new GridLayout(0, 1));
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		pnlOptions.setLayout(gbl);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.weightx = 1.0;
		// END KGU#472 2018-01-22
		
		for (HashMap<String, String> optionSpec: plugin.options) {
			String key = optionSpec.get("name");
			String type = optionSpec.get("type");
			String items = optionSpec.get("items");
			String caption = optionSpec.get("title");
			String tooltip = optionSpec.get("help");
			String value = this.optionVals.get(key);
			if (items != null && items.startsWith("{") && items.endsWith("}")) {
				// START KGU#472 2018-01-22: Bugfix #484 - wrong string was split
				//StringList itemVals = Element.splitExpressionList(type.substring(1, type.length()-1), ";");
				StringList itemVals = Element.splitExpressionList(items.substring(1, items.length()-1), ";");
				// END KGU#472 2018-01-22
				JComboBox<String> comp = new JComboBox<String>(itemVals.toArray());
				comp.setEditable(false);
				if (tooltip != null && !tooltip.trim().isEmpty()) {
					comp.setToolTipText(tooltip);
				}
				if (itemVals.contains(value)) {
					comp.setSelectedItem(value);
				}
				comp.addKeyListener(keyListener);
				JLabel lbl = new JLabel(caption);
				JPanel pnl = new JPanel();
				pnl.setLayout(new GridLayout(1, 0, 5, 0));
				pnl.setBorder(new EmptyBorder(0, 2, 0, 2));
				pnl.add(lbl);
				pnl.add(comp);
				optionComponents.put(key, comp);
				// START KGU#961 2021-03-07: New approach for plugin-specific translations
				optionLabels.put(key, lbl);
				// END KGU#961 2021-03-07
				// START KGU#472 2018-01-22: Issue #484
				//pnlOptions.add(pnl);
				gbc.gridy++;
				pnlOptions.add(pnl, gbc);
				// END KGU#472 2018-01-22
			}
			else if (type.equalsIgnoreCase("Boolean")) {
				JCheckBox comp = new JCheckBox(caption);
				if (tooltip != null && !tooltip.trim().isEmpty()) {
					comp.setToolTipText(tooltip);
				}
				comp.setSelected(Boolean.parseBoolean(value));
				comp.addKeyListener(keyListener);
				optionComponents.put(key, comp);
				// START KGU#472 2018-01-22: Issue #484
				//pnlOptions.add(comp);
				gbc.gridy++;
				pnlOptions.add(comp, gbc);
				// END KGU#472 2018-01-22
			}
			else {
				JTextField comp = new JTextField(TEXT_FIELD_WIDTH);
				comp.setText(value);
				if (type.equalsIgnoreCase("Integer")) {
					comp.setInputVerifier(intVerifier);
				}
				else if (type.equalsIgnoreCase("Unsigned")) {
					comp.setInputVerifier(uintVerifier);
				}
				else if (type.equalsIgnoreCase("Character")) {
					comp.setInputVerifier(charVerifier);
				}
				else if (type.equalsIgnoreCase("Double")) {
					comp.setInputVerifier(doubleVerifier);
				}
				if (tooltip != null && !tooltip.trim().isEmpty()) {
					comp.setToolTipText(tooltip);
				}
				comp.addKeyListener(keyListener);
				JLabel lbl = new JLabel(caption);
				// START KGU#472 2018-01-22: Issue #484 
//				JPanel pnl = new JPanel();
//				pnl.setLayout(new GridLayout(1, 0));
//				pnl.setBorder(new EmptyBorder(0, 2, 0,2));
//				pnl.add(lbl);
//				pnl.add(comp);
				optionComponents.put(key, comp);
				// START KGU#961 2021-03-07: New approach for plugin-specific translations
				optionLabels.put(key, lbl);
				// END KGU#961 2021-03-07
//				pnlOptions.add(pnl);
				gbc.gridy++;
				gbc.gridwidth = 1;
				gbc.weightx = 0.0;
				gbc.fill = GridBagConstraints.NONE;
				gbc.insets = new Insets(0, 0, 0, 5);
				pnlOptions.add(lbl, gbc);
				
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridwidth = GridBagConstraints.REMAINDER;
				gbc.gridx++;
				gbc.weightx = 1.0;
				gbc.insets.right = 0;
				pnlOptions.add(comp, gbc);
				gbc.gridx = 1;
				// END KGU#472 2018-01-22
			}
		}
		// ====================== BUTTONS ======================
		// START KGU#472 2018-01-22: Issue #484
		//JPanel pnlButtons = new JPanel();
		JPanel pnlButtonBar = new JPanel();
		// END KGU#472 2018-01-22
		{
			// START KGU#472 2018-01-22: Issue #484
			//pnlButtons.setBorder(new EmptyBorder(10, 5, 5, 5));
			pnlButtonBar.setBorder(new EmptyBorder(10, 5, 5, 5));
			pnlButtonBar.setLayout(new GridBagLayout());
			GridBagConstraints gbc1 = new GridBagConstraints();
			gbc1.gridx = 1;
			gbc1.gridy = 1;
			gbc1.gridwidth = 1;
			gbc1.gridheight = 1;
			gbc1.anchor = GridBagConstraints.LINE_START;
			gbc1.fill = GridBagConstraints.HORIZONTAL;
			JPanel pnlButtons = new JPanel();
			// END KGU#472 2018-01-22
			pnlButtons.setLayout(new GridLayout(1, 0));
			btnCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					setVisible(false);
				}});
			btnCancel.addKeyListener(keyListener);
			btnOk.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					commitChanges();
					setVisible(false);
				}});
			btnOk.addKeyListener(keyListener);
			pnlButtons.add(btnCancel);
			pnlButtons.add(btnOk);
			// START KGU#472 2018-01-22: Issue #484
			gbc1.weightx = 1.0;
			pnlButtonBar.add(new JLabel(""), gbc1);
			gbc1.anchor = GridBagConstraints.LINE_END;
			gbc1.fill = GridBagConstraints.NONE;
			gbc1.weightx = 0.0;
			pnlButtonBar.add(pnlButtons, gbc1);
			// END KGU#472 2018-01-22
		}
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(pnlOptions, BorderLayout.NORTH);
		// START KGU#472 2018-01-22: Issue #484
		//contentPane.add(pnlButtons, BorderLayout.SOUTH);
		contentPane.add(pnlButtonBar, BorderLayout.SOUTH);
		// END KGU#472 2018-01-22

		GUIScaler.rescaleComponents(this);

		pack();

	}

	protected void commitChanges() {
		for (Map.Entry<String, JComponent> entry: this.optionComponents.entrySet()) {
			String value = null;
			JComponent comp = entry.getValue();
			if (comp instanceof JComboBox<?>) {
				value = (String) ((JComboBox<?>)comp).getSelectedItem();
			}
			else if (comp instanceof JCheckBox) {
				value = Boolean.toString(((JCheckBox)comp).isSelected());
			}
			else if (comp instanceof JTextField) {
				value = ((JTextField) comp).getText();
			}
			this.optionVals.put(entry.getKey(), value);
		}
	}
	
	// START KGU#961 2021-03-07: Hook for a condition in the locale file
	/**
	 * @return the simplified class name for the plugin-associated Java class, e.g.
	 * "CGenerator" or "JavaParser".
	 */
	public String getPluginKey()
	{
		return plugin.getKey();
	}
	// END KGU#961 2021-03-07

}

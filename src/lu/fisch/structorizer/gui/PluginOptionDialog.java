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
 *      Kay Gürtzig     2017.06.20      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - Class is used by ExportOptionDialog and ImportOptionDialog to present plugin options
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;

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
import lu.fisch.utils.StringList;

/**
 * Dialog class for dynamically retrieved option configuration.
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class PluginOptionDialog extends LangDialog {
	
	private GENPlugin plugin;
	public HashMap<String, String> optionVals;
	protected JButton btnOk = new JButton("OK");
	protected JButton btnCancel = new JButton("Cancel");
	protected JCheckBox[] checkboxes = null;
	protected JTextField[] textFields = null;
	protected JLabel[] comboLabels = null;
	protected JComboBox<String>[] comboBoxes = null;
	protected HashMap<String, JComponent> optionComponents = new HashMap<String, JComponent>();
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
		// FIXME We may prefer a GridBagLayout instead
		pnlOptions.setLayout(new GridLayout(0, 1));
		for (HashMap<String, String> optionSpec: plugin.options) {
			String key = optionSpec.get("name");
			String type = optionSpec.get("type");
			String items = optionSpec.get("items");
			String caption = optionSpec.get("title");
			String tooltip = optionSpec.get("help");
			String value = this.optionVals.get(key);
			if (items != null && items.startsWith("{") && items.endsWith("}")) {
				StringList itemVals = Element.splitExpressionList(type.substring(1, type.length()-1), ";");
				JComboBox<String> comp = new JComboBox<String>(itemVals.toArray());
				comp.setEditable(false);
				if (tooltip != null && !tooltip.trim().isEmpty()) {
					comp.setToolTipText(tooltip);
				}
				if (itemVals.contains(value)) {
					comp.setSelectedItem(value);
				}
				comp.addKeyListener(keyListener);
				JLabel lbl = new JLabel(caption + "  ");
				JPanel pnl = new JPanel();
				pnl.setLayout(new GridLayout(1, 0));
				pnl.setBorder(new EmptyBorder(0, 2, 0,2));
				pnl.add(lbl);
				pnl.add(comp);
				optionComponents.put(key, comp);
				pnlOptions.add(pnl);				
			}
			else if (type.equalsIgnoreCase("Boolean")) {
				JCheckBox comp = new JCheckBox(caption);
				if (tooltip != null && !tooltip.trim().isEmpty()) {
					comp.setToolTipText(tooltip);
				}
				comp.setSelected(Boolean.parseBoolean(value));
				comp.addKeyListener(keyListener);
				optionComponents.put(key, comp);
				pnlOptions.add(comp);
			}
			else {
				JTextField comp = new JTextField(/*TEXT_FIELD_WIDTH*/);
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
				JLabel lbl = new JLabel(caption + "  ");
				JPanel pnl = new JPanel();
				pnl.setLayout(new GridLayout(1, 0));
				pnl.setBorder(new EmptyBorder(0, 2, 0,2));
				pnl.add(lbl);
				pnl.add(comp);
				optionComponents.put(key, comp);
				pnlOptions.add(pnl);
			}
		}
		// ====================== BUTTONS ======================
		JPanel pnlButtons = new JPanel();
		{
			pnlButtons.setBorder(new EmptyBorder(10, 5, 5, 5));
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
		}
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(pnlOptions, BorderLayout.NORTH);
		contentPane.add(pnlButtons, BorderLayout.SOUTH);
		
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

}

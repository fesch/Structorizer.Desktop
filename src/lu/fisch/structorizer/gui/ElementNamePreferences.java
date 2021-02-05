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
 *      Description:    Preferences dialog class to allow users to specify names for all Element types.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-12-14      First Issue for #492
 *      Kay Gürtzig     2018-01-20      Layout improved (row distance reduced)
 *      Kay Gürtzig     2019-06-10/11   Issue #727: The placement of the TRY field had to be tweaked,
 *                                      a new button "English Standards" was introduced.
 *      Kay Gürtzig     2021-01-26      Issue #400: Key listener applied to chkUseConfNames
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
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
import java.awt.GridLayout;
//import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.locales.Locale;
import lu.fisch.structorizer.locales.Locales;

/**
 * Preferences dialog allowing to introduce user-declared (and locale-independent) Element name aliases
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class ElementNamePreferences extends LangDialog {

	public boolean OK = false;
	
	private JLabel lblExplanation;
	private JLabel lblLocalized;
	private JLabel lblIndividual;
	public JCheckBox chkUseConfNames;
	private JLabel[] lblElements;
	public JTextField[] txtElements;
	private JButton btnOK;
	// START KGU#710 2019-06-11: Issue #727
	private JButton btnEnglishStd;
	// END KGU#710 2019-06-11
	
	private JPanel dialogPane;
	private JPanel configPanel;
	private JPanel headBar;
	private JPanel buttonBar;
	
	public ElementNamePreferences(Frame owner)
	{
		super(owner);
		setModal(true);
		initComponents();
	}
	
	private void initComponents()
	{
		setTitle("Element Name Preferences");
		
		dialogPane = new JPanel();
		configPanel = new JPanel();
		headBar = new JPanel();
		buttonBar = new JPanel();
		
		lblExplanation = new JLabel("Re-label the Element types for display");
		chkUseConfNames = new JCheckBox("Enable the configured labels");
		lblLocalized = new JLabel("Localized label");
		lblIndividual = new JLabel("Configured label");
		lblElements = new JLabel[ElementNames.ELEMENT_KEYS.length];
		txtElements = new JTextField[ElementNames.ELEMENT_KEYS.length];
		btnOK = new JButton("OK");
		// START KGU#710 2019-06-11: Issue #727
		btnEnglishStd = new JButton("English Standard");
		// END KGU#710 2019-06-11

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

			dialogPane.setLayout(new BorderLayout());
			
			//======== headBar =============
			{
				chkUseConfNames.setSelected(true);
				headBar.setBorder(new EmptyBorder(0, 0, 10, 0));
				headBar.setLayout(new GridLayout(0, 1, 8, 4));
				headBar.add(lblExplanation);
				headBar.add(chkUseConfNames);
			}
			dialogPane.add(headBar, BorderLayout.NORTH);

			//======== contentPanel ========
			{
				configPanel.setLayout(new GridLayout(0, 2, 8, 0));
				
				configPanel.add(lblLocalized);
				configPanel.add(lblIndividual);

				// Index of the "Diagram" field (we simply know)
				final int IxDIAGRAM = 12;
				for (int i = 0; i < ElementNames.ELEMENT_KEYS.length; i++) {
					int j = i;
					// START KGU#710 2019-06-10: Issue #727 - TRY label without indentation and before diagram
					//String descr = ElementNames.localizedNames[j].getText();
					//if (i == 4 || i == 5 || i > 12) {
					//	descr = "    " + descr;
					//}
					// We do some index re-mapping to give TRY an appropriate place
					if (i == IxDIAGRAM) j = ElementNames.ELEMENT_KEYS.length - 1;
					else if (i > IxDIAGRAM) j = i - 1;
					String descr = ElementNames.localizedNames[j].getText();
					if (ElementNames.ELEMENT_KEYS[j].indexOf('.') >= 0) {
						descr = "    " + descr;
					}
					// END KGU##710 2019-06-10
					lblElements[j] = new JLabel(descr);
					txtElements[j] = new JTextField();
					configPanel.add(lblElements[j]);
					configPanel.add(txtElements[j]);
				}
			}
			dialogPane.add(configPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 5, 0));
				// START KGU#710 2019-06-11: Issue #727
//				buttonBar.setLayout(new GridBagLayout());
//				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 80};
//				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};
				buttonBar.setLayout(new GridLayout(1, 2));

				//---- okEnglishStd ----
				btnEnglishStd.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						Locale enLoc = Locales.getInstance().getDefaultLocale();
						for (int i = 0; i < ElementNames.ELEMENT_KEYS.length; i++) {
							txtElements[i].setText(enLoc.getValue("Elements", "ElementNames.localizedNames." + i + ".text"));
						}
						if (!chkUseConfNames.isSelected()) {
							chkUseConfNames.doClick();
						}
					}
				});
				btnEnglishStd.setToolTipText("Adopt the English standard element names as user-specific names (for all languages).");
				// END KGU#710 2019-06-11
				
				//---- okButton ----
				btnOK.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						 OK = true; setVisible(false);
						 }
					});
				
				// START KGU#710 2019-06-11: Issue #727
				//buttonBar.add(btnOK, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				//	GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				//	new Insets(0, 0, 5, 0), 0, 0));
				
				buttonBar.add(btnEnglishStd);
				buttonBar.add(btnOK);
				// END KGU#710 2019-06-11
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane);
		
		ItemListener checkItemListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent evt) {
				if (evt.getSource() == chkUseConfNames) {
					boolean isSelected = evt.getStateChange() == ItemEvent.SELECTED;
					for (int i = 0; i < txtElements.length; i++) {
						txtElements[i].setEnabled(isSelected);
					}
				}
			}

		};
		chkUseConfNames.addItemListener(checkItemListener);

		// START KGU#479 2017-12-22: Issue #492: GUI scaling hadn't worked properly
		GUIScaler.rescaleComponents(this);
		// END KGU#479 2017-12-22

		pack();
		setLocationRelativeTo(getOwner());
		
		KeyListener keyListener = new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				else if(e.getKeyCode() == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown()))
				{
					setVisible(false);
					OK = true;
				}
			}
			
			@Override
			public void keyReleased(KeyEvent ke) {} 
			@Override
			public void keyTyped(KeyEvent kevt) {}
		};

		for (int i = 0; i < txtElements.length; i++) {
			txtElements[i].addKeyListener(keyListener);
		}
		// START KGU#393 2021-01-26: Issue #400
		chkUseConfNames.addKeyListener(keyListener);
		// END KGU#393 2021-01-26
		// START KGU#710 2019-06-11: Issue #727
		btnEnglishStd.addKeyListener(keyListener);
		// END KGU#710 2019-06-11
		btnOK.addKeyListener(keyListener);
	}
}

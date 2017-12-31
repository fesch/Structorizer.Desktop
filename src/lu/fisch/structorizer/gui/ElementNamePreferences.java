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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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

/******************************************************************************************************
 *
 *      Author:         kay
 *
 *      Description:    Preferences dialog class to allow users to specify names for all Element types.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.12.14      First Issue for #492
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

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
		lblElements = new JLabel[ElementNames.configuredNames.length];
		txtElements = new JTextField[ElementNames.configuredNames.length];
		btnOK = new JButton("OK");

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

			dialogPane.setLayout(new BorderLayout());
			
			//======== headBar =============
			{
				chkUseConfNames.setSelected(true);
				headBar.setLayout(new GridLayout(0, 1, 8, 8));
				headBar.add(lblExplanation);
				headBar.add(chkUseConfNames);
			}
			dialogPane.add(headBar, BorderLayout.NORTH);

			//======== contentPanel ========
			{
				configPanel.setLayout(new GridLayout(0, 2, 8, 8));
				
				configPanel.add(lblLocalized);
				configPanel.add(lblIndividual);

				for (int i = 0; i < ElementNames.configuredNames.length; i++) {
					String descr = ElementNames.localizedNames[i].getText();
					if (i == 4 || i == 5 || i > 12) {
						descr = "    " + descr;
					}
					lblElements[i] = new JLabel(descr);
					txtElements[i] = new JTextField();
					configPanel.add(lblElements[i]);
					configPanel.add(txtElements[i]);
				}
			}
			dialogPane.add(configPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

				//---- okButton ----
				btnOK.setText("OK");
				buttonBar.add(btnOK, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));
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
		btnOK.addKeyListener(keyListener);
		btnOK.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			 OK = true; setVisible(false);
			 }
		}); 
	}
}

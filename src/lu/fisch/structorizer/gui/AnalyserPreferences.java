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
 *      Author:         Bob Fisch
 *
 *      Description:    This is the GUI for the analyser preferences
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008.05.23      First Issue
 *      Kay Gürtzig     2015.11.03      check14 added (enhanced FOR loop support, issue #10 = KGU#3)
 *      Kay Gürtzig     2015.11.25      check15 (issue #9 = KGU#2) and check16 (issue #23 = KGU#78) added
 *      Kay Gürtzig     2015.11.28      check17 (KGU#47) for inconsistency risks in Parallel sections added
 *
 ******************************************************************************************************
 *
 *      Comment:		
 *
 ******************************************************************************************************///

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Bob Fisch
 */
public class AnalyserPreferences extends LangDialog {

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Bob Fisch
	private JPanel dialogPane;
	private JPanel contentPanel;
	public JCheckBox check1;
	public JCheckBox check2;
	public JCheckBox check3;
	public JCheckBox check4;
	public JCheckBox check5;
	public JCheckBox check6;
	public JCheckBox check7;
	public JCheckBox check8;
	public JCheckBox check9;
	public JCheckBox check10;
	public JCheckBox check11;
	public JCheckBox check12;
	public JCheckBox check13;
	// START KGU#3 2015-11-03: Additional FOR loop checks
	public JCheckBox check14;
	// END KGU#3 2015-11-03
	// START KGU#2 2015-11-25: Additional CALL syntax check
	public JCheckBox check15;
	// END KGU#2 2015-11-25
	// START KGU#78 2015-11-25: Additional JUMP syntax check
	public JCheckBox check16;
	// END KGU#78 2015-11-25
	// START KGU#47 2015-11-28: Additional PARALLEL consistency check
	public JCheckBox check17;
	// END KGU#47 2015-11-28
	private JPanel buttonBar;
	protected JButton okButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	/*public AnalyserPreferences()
	{
		super();
		setModal(true);
		initComponents();
	}*/

	public AnalyserPreferences(Frame owner) {
		super(owner);
                setModal(true);
		initComponents();
	}

	/*public AnalyserPreferences(Dialog owner) {
		super(owner);
		initComponents();
	}*/

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Bob Fisch
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		check1 = new JCheckBox();
		check2 = new JCheckBox();
		check3 = new JCheckBox();
		check4 = new JCheckBox();
		check5 = new JCheckBox();
		check6 = new JCheckBox();
		check7 = new JCheckBox();
		check8 = new JCheckBox();
		check9 = new JCheckBox();
		check10 = new JCheckBox();
		check11 = new JCheckBox();
		check12 = new JCheckBox();
		check13 = new JCheckBox();
		// START KGU#3 2015-11-03: Additional For loop checks
		check14 = new JCheckBox();
		// END KGU#3 2015-11-03
		// START KGU#2 2015-11-25: Additional CALL syntax check
		check15 = new JCheckBox();;
		// END KGU#2 2015-11-25
		// START KGU#78 2015-11-25: Additional JUMP syntax check
		check16 = new JCheckBox();;
		// END KGU#78 2015-11-25
		// START KGU#47 2015-11-28: Additional PARALLEL consistency check
		check17 = new JCheckBox();
		// END KGU#47 2015-11-28
		buttonBar = new JPanel();
		okButton = new JButton();

		//======== this ========
		setTitle("Analyser preferences");
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

			// JFormDesigner evaluation mark
			/*dialogPane.setBorder(new javax.swing.border.CompoundBorder(
				new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
					"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
					javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
					java.awt.Color.red), dialogPane.getBorder())); dialogPane.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});
*/
			dialogPane.setLayout(new BorderLayout());
			
			//======== contentPanel ========
			{
				contentPanel.setLayout(new GridLayout(17, 1));

				//---- check1 ----
				check1.setText("Check for modified loop variable.");
				contentPanel.add(check1);

				// START KGU#3 2015-11-03: Additional For loop checks
				//---- check14----
				check14.setText("Check for consistency of FOR loop parameters.");
				contentPanel.add(check14);
				// END KGU#3 2015-11-03

				//---- check2 ----
				check2.setText("Check for endless loop (as far as detectable!).");
				contentPanel.add(check2);

				//---- check3 ----
				check3.setText("Check for non-initialized variables.");
				contentPanel.add(check3);

				//---- check4 ----
				check4.setText("Check for incorrect use of the IF-statement.");
				contentPanel.add(check4);

				//---- check5 ----
				check5.setText("Check for UPPERCASE variable names. (LUX/MEN)");
				contentPanel.add(check5);

				//---- check6 ----
				check6.setText("Check for UPPERCASE program / sub name. (LUX/MEN)");
				contentPanel.add(check6);

				//---- check7 ----
				check7.setText("Check for valid identifiers.");
				contentPanel.add(check7);

				//---- check8 ----
				check8.setText("Check for assignment in conditions.");
				contentPanel.add(check8);

				//---- check9 ----
				check9.setText("Check that the program / sub name is not equal to any other identifier.");
				contentPanel.add(check9);

				//---- check10 ----
				check10.setText("Check for instructions with inputs and outputs.");
				contentPanel.add(check10);

				//---- check11 ----
				check11.setText("Check for assignment errors.");
				contentPanel.add(check11);
				
				//---- check12 ----
				check12.setText("Check for standardized parameter name. (LUX/MEN)");
				contentPanel.add(check12);
				
				//---- check13 ----
				check13.setText("Check if, in case of a function, it returns a result.");
				contentPanel.add(check13);

				// START KGU#2/KGU#78 2015-11-25: Additional checks for CALL and JUMP elements
				//---- check15 ----
				check15.setText("Check for inappropriate subroutine CALLs.");
				contentPanel.add(check15);

				//---- check16 ----
				check16.setText("Check for incorrect JUMP element usage.");
				contentPanel.add(check16);
				// END KGU#2/KGU#78 2015-11-25

				// START KGU#47 2015-11-28: Additional checks for PARALLEL elements
				//---- check17 ----
				check17.setText("Check for inconsistency risks in PARALLEL sections.");
				contentPanel.add(check17);
				// END KGU#47 2015-11-28
				
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
				buttonBar.setLayout(new GridBagLayout());
				((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 80};
				((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};
				
				//---- okButton ----
				okButton.setText("OK");
				buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
															   GridBagConstraints.CENTER, GridBagConstraints.BOTH,
															   new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
			
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// Bob-thinks
		// add the KEY-listeners
		okButton.requestFocus(true);
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				else if(e.getKeyCode() == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown()))
				{
					setVisible(false);
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		okButton.addKeyListener(keyListener);
		
		// add the ACTION-listeners
		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				setVisible(false);
			}
		};
		okButton.addActionListener(actionListener);
	}

}

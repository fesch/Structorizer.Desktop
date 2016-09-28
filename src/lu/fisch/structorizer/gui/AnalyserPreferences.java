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

/*
 ******************************************************************************************************
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
 *      Kay Gürtzig     2016.08.12      Enh. #231: check18 and check19 added (identifier collisions),
 *                                      checkbox management reorganised with arrays for easier maintenance
 *      Kay Gürtzig     2016.09.21      Enh. #249: check20 added (subroutine syntax)
 *      Kay Gürtzig     2016.09.22      checkboxes index mapping modified, duplicate entries removed from
 *                                      checkboxOrder, order of checkboxes modified
 *
 ******************************************************************************************************
 *
 *      Comment:		
 *
 ******************************************************************************************************
 */

import lu.fisch.structorizer.locales.LangDialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Bob Fisch
 */
@SuppressWarnings("serial")
public class AnalyserPreferences extends LangDialog {

	// DO NOT CHANGE THE ORDER OF THE STRINGS HERE - use checkboxOrder to rearrange checks! 
	private static final String[] checkCaptions = {
		"Check for modified loop variable.",						// 1
		"Check for endless loop (as far as detectable!).",			// 2
		"Check for non-initialized variables.",						// 3
		"Check for incorrect use of the IF-statement.",				// 4
		"Check for UPPERCASE variable names. (LUX/MEN)",			// 5
		"Check for UPPERCASE program / sub name. (LUX/MEN)",		// 6
		"Check for valid identifiers.",								// 7
		"Check for assignment in conditions.",						// 8
		"Check that the program / sub name is not equal to any other identifier.",
		"Check for instructions with inputs and outputs.",			// 10
		"Check for assignment errors.",								// 11
		"Check for standardized parameter name. (LUX/MEN)",			// 12
		"Check if, in case of a function, it returns a result.",	// 13
		"Check for consistency of FOR loop parameters.",			// 14
		"Check for inappropriate subroutine CALLs.",				// 15
		"Check for incorrect JUMP element usage.",					// 16
		"Check for inconsistency risks in PARALLEL sections.",		// 17
		"Check that identifiers don't differ only by upper/lower case.",
		"Check if an identifier might collide with reserved words.",// 19
		"Check that a subroutine header has a parameter list."		// 20
		// Just append the descriptions for new check types here and insert their
		// numbers at the appropriate place in array checkboxOrder below.
		// DON'T FORGET to add a new entry to Root.analyserChecks for every
		// text added here (and of course the checking code itself)!
	};
	// The order in which the checks (numbering starts with 1) are to be presented
	private static final int[] checkboxOrder = {
		// instructions, alternatives
		3, 10, 11, 8, 4,
		// loops
		1, 14, 2,
		// functions and calls
		20, 13,	15,
		// jumps and parallel sections
		16, 17,
		// identifiers and naming conventions
		7, 9, 18, 19, /*LUX/MEN*/ 5, 6, 12
	};
			
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Bob Fisch
	private JPanel dialogPane;
	private JPanel contentPanel;
//	public JCheckBox check1;
//	public JCheckBox check2;
//	public JCheckBox check3;
//	public JCheckBox check4;
//	public JCheckBox check5;
//	public JCheckBox check6;
//	public JCheckBox check7;
//	public JCheckBox check8;
//	public JCheckBox check9;
//	public JCheckBox check10;
//	public JCheckBox check11;
//	public JCheckBox check12;
//	public JCheckBox check13;
//	// START KGU#3 2015-11-03: Additional FOR loop checks
//	public JCheckBox check14;
//	// END KGU#3 2015-11-03
//	// START KGU#2 2015-11-25: Additional CALL syntax check
//	public JCheckBox check15;
//	// END KGU#2 2015-11-25
//	// START KGU#78 2015-11-25: Additional JUMP syntax check
//	public JCheckBox check16;
//	// END KGU#78 2015-11-25
//	// START KGU#47 2015-11-28: Additional PARALLEL consistency check
//	public JCheckBox check17;
//	// END KGU#47 2015-11-28
//	// START KGU#239 2016-08-12: New identifier collision checks
//	public JCheckBox check18;
//	public JCheckBox check19;
//	// END KGU#239 2016-08-12
	// START KGU 2016-09-22: Dummy entry at index 0 for more consistent error numbering
	//public JCheckBox[] checkboxes = new JCheckBox[checkCaptions.length];
	public JCheckBox[] checkboxes = new JCheckBox[checkCaptions.length+1];
	// END KGU 2016-09-22
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
//		check1 = new JCheckBox();
//		check2 = new JCheckBox();
//		check3 = new JCheckBox();
//		check4 = new JCheckBox();
//		check5 = new JCheckBox();
//		check6 = new JCheckBox();
//		check7 = new JCheckBox();
//		check8 = new JCheckBox();
//		check9 = new JCheckBox();
//		check10 = new JCheckBox();
//		check11 = new JCheckBox();
//		check12 = new JCheckBox();
//		check13 = new JCheckBox();
//		// START KGU#3 2015-11-03: Additional For loop checks
//		check14 = new JCheckBox();
//		// END KGU#3 2015-11-03
//		// START KGU#2 2015-11-25: Additional CALL syntax check
//		check15 = new JCheckBox();;
//		// END KGU#2 2015-11-25
//		// START KGU#78 2015-11-25: Additional JUMP syntax check
//		check16 = new JCheckBox();;
//		// END KGU#78 2015-11-25
//		// START KGU#47 2015-11-28: Additional PARALLEL consistency check
//		check17 = new JCheckBox();
//		// END KGU#47 2015-11-28
//		// START KGU#239 2016-08-12: New identifier collision checks
//		check18 = new JCheckBox();
//		check19 = new JCheckBox();
//		// END KGU#239 2016-08-12
		// START KGU 2016-09-22: New dummy entry at index position 0
		//for (int i = 0; i < checkboxes.length; i++)
		checkboxes[0] = null;
		for (int i = 1; i < checkboxes.length; i++)
		// END KGU 2016-09-22
		{
			checkboxes[i] = new JCheckBox();
			// START KGU 2016-09-22: New dummy entry at index position 0
			//checkboxes[i].setText(checkCaptions[i]);
			checkboxes[i].setText(checkCaptions[i-1]);
			// END KGU 2016-09-22
		}
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
				// START KGU 2016-09-22: New dummy entry at index position 0
				//contentPanel.setLayout(new GridLayout(checkboxes.length, 1));
				contentPanel.setLayout(new GridLayout(checkboxes.length-1, 1));
				// END KGU 2016-09-22

				for (int i = 0; i < checkboxOrder.length; i++)
				{
					// START KGU 2016-09-22: New dummy entry at index position 0
					//contentPanel.add(checkboxes[checkboxOrder[i]-1]);
					contentPanel.add(checkboxes[checkboxOrder[i]]);
					// END KGU 2016-09-22
				}
				
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

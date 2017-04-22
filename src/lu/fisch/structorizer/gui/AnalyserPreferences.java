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
 *      Kay Gürtzig     2016.08.12      Enh. #231: check18 and check19 added (identifier collisions),
 *                                      checkbox management reorganised with arrays for easier maintenance
 *      Kay Gürtzig     2016.09.21      Enh. #249: check20 added (subroutine syntax)
 *      Kay Gürtzig     2016.09.22      checkboxes index mapping modified, duplicate entries removed from
 *                                      checkboxOrder, order of checkboxes modified
 *      Kay Gürtzig     2016.11.10      Enh. #286: Tabs introduced, configuration array checkboxOrder replaced
 *                                      by map checkboxTabs.
 *      Kay Gürtzig     2016.11.11      Issue #81: DPI-awareness workaround
 *      Kay Gürtzig     2017.01.07      Enh. #329: New Analyser error21 (variable names I, l, O)
 *                                      bugfix #330: Checkbox status visibility in "Nimbus" look & feel
 *      Kay Gürtzig     2017.01.09      Bugfix #330: Scaling stuff outsourced to GUIScaler
 *      Kay Gürtzig     2017.04.04      Enh. #388: New check for constant definitions (no. 22)
 *
 ******************************************************************************************************
 *
 *      Comment:		
 *
 ******************************************************************************************************///

import lu.fisch.structorizer.locales.LangDialog;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Bob Fisch
 */
@SuppressWarnings("serial")
public class AnalyserPreferences extends LangDialog {

	// DO NOT CHANGE THE ORDER OF THE STRINGS HERE - use checkboxOrder to rearrange checks! 
	private static final String[] checkCaptions = {
		/*1*/"Check for modified loop variable.",
		/*2*/"Check for endless loop (as far as detectable!).",
		/*3*/"Check for non-initialized variables.",
		/*4*/"Check for incorrect use of the IF-statement.",
		/*5*/"Check for UPPERCASE variable names. (LUX/MEN)",
		/*6*/"Check for UPPERCASE program / sub name. (LUX/MEN)",
		/*7*/"Check for valid identifiers.",
		/*8*/"Check for assignment in conditions.",
		/*9*/"Check that the program / sub name is not equal to any other identifier.",
		/*10*/"Check for mixed-type multiple-line instructions.",
		/*11*/"Check for assignment errors.",
		/*12*/"Check for standardized parameter name. (LUX/MEN)",
		/*13*/"Check if, in case of a function, it returns a result.",
		/*14*/"Check for consistency of FOR loop parameters.",
		/*15*/"Check for inappropriate subroutine CALLs.",
		/*16*/"Check for incorrect JUMP element usage.",
		/*17*/"Check for inconsistency risks in PARALLEL sections.",
		/*18*/"Check that identifiers don't differ only by upper/lower case.",
		/*19*/"Check if an identifier might collide with reserved words.",
		/*20*/"Check that a subroutine header has a parameter list.",
		/*21*/"Discourage use of mistakable variable names «I», «l», and «O».",
		/*22*/"Check for possible violations of constants.",
		/*23*/"Check for faulty diagram imports"
		// Just append the descriptions for new check types here and insert their
		// numbers at the appropriate place in array checkboxOrder below.
		// DON'T FORGET to add a new entry to Root.analyserChecks for every
		// text added here (and of course the checking code itself)!
	};
	// START KGU#290 2016-11-10: Enh. #286 (grouping and distribution over several tabs)
	// The order in which the checks (numbering starts with 1, index 0 induces an
	// empty line) are to be presented and distributed over several tabs
	private static final LinkedHashMap<String, int[]> checkboxTabs = new LinkedHashMap<String, int[]>();
	static {
		checkboxTabs.put("Algorithmic", new int[]{
				// instructions
				3, 11, 22,
				0,// alternatives
				8, 4,
				0,// loops
				1, 14, 2,
				0,// functions and calls
				20, 13,	15, 23,
				0,// jumps and parallel sections
				16, 17
		});
		checkboxTabs.put("Naming / Conventions", new int[]{
				// identifiers and naming conventions
				7, 9, 18, 19, 21,
				0/*LUX/MEN*/,
				5, 6, 12,
				0,// multiple command types
				10
		});
	}
	// END KGU#290 2016-11-10
			
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Bob Fisch
	private JPanel dialogPane;
	private JTabbedPane contentPanel;
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
		// END KGU#287 2016-11-11
		dialogPane = new JPanel();
		contentPanel = new JTabbedPane();
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
				// START KGU#290 2016-11-10: Enh. #286 (tabs and gaps)
				//contentPanel.setLayout(new GridLayout(checkboxes.length-1, 1));
				//for (int i = 0; i < checkboxOrder.length; i++)
				//{
				//	contentPanel.add(checkboxes[checkboxOrder[i]]);
				//}
				for (Entry<String, int[]> entry: checkboxTabs.entrySet())
				{
					JPanel panel = new JPanel();
					JPanel wrapper = new JPanel();
					int[] checklist = entry.getValue();
					panel.setLayout(new GridLayout(checklist.length, 1));

					for (int i = 0; i < checklist.length; i++)
					{
						int checkIndex = checklist[i];
						if (checkIndex == 0) {
							panel.add(new JLabel(""));
						}
						else {
							panel.add(checkboxes[checkIndex]);
						}
					}
					wrapper.setLayout(new BorderLayout());
					wrapper.add(panel, BorderLayout.NORTH);	// Avoids the checkboxes being spread
					contentPanel.addTab(entry.getKey(), wrapper);
				}
				// END KGU#290 2016-11-10
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
		
		// START KGU#287 2017-01-09: Issues #81, #330
		GUIScaler.rescaleComponents(this);
		// END KGU#287 2017-01-09
		
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

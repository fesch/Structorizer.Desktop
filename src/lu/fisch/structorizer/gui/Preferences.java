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
 *      Description:    This is the general preferences window.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.31      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		I used JFormDesigner to design this window graphically.
 *
 ******************************************************************************************************///

import lu.fisch.structorizer.locales.LangDialog;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
/*
 * Created by JFormDesigner on Mon Dec 31 09:04:29 CET 2007
 */



/**
 * @author Robert Fisch
 */
@SuppressWarnings("serial")
public class Preferences extends LangDialog implements ActionListener, KeyListener
{

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Robert Fisch
	protected JPanel dialogPane;
	protected JPanel contentPanel;
	protected JPanel pnlLeft;
	protected JPanel pnlAlt;
	protected JPanel pnlCases;
	protected JPanel pnlAltLeft;
	protected JLabel lblAltT;
	protected JTextField edtAltT;
	protected JPanel pnlAltRight;
	protected JLabel lblAltF;
	protected JTextField edtAltF;
	protected JPanel pnlContent;
	protected JLabel lblAltContent;
	protected JTextField edtAlt;
	protected JPanel pnlCase;
	protected JLabel lblCase;
	protected JScrollPane scrollPane1;
	protected JTextArea txtCase;
	protected JPanel pnlRight;
	protected JPanel pnlFor;
	protected JLabel lblFor;
	protected JTextField edtFor;
	protected JPanel pnlRepeat;
	protected JLabel lblRepeat;
	protected JTextField edtRepeat;
	protected JPanel pnlWhile;
	protected JLabel lblWhile;
	protected JTextField edtWhile;
	protected JPanel buttonBar;
	protected JButton btnOK;
	protected JCheckBox altPadRight;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	/*public Preferences() {
		super();
		setModal(true);
		initComponents();
	}*/
	
	public Preferences(Frame owner) {
		super(owner);
		setModal(true);
		initComponents();
	}
	
	/*public Preferences(Dialog owner) {
		super(owner);
		initComponents();
	}*/

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Robert Fisch
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		pnlLeft = new JPanel();
		pnlAlt = new JPanel();
		pnlCases = new JPanel();
		pnlAltLeft = new JPanel();
		lblAltT = new JLabel();
		edtAltT = new JTextField();
		pnlAltRight = new JPanel();
		lblAltF = new JLabel();
		edtAltF = new JTextField();
		pnlContent = new JPanel();
		lblAltContent = new JLabel();
		edtAlt = new JTextField();
		pnlCase = new JPanel();
		lblCase = new JLabel();
		scrollPane1 = new JScrollPane();
		txtCase = new JTextArea();
		pnlRight = new JPanel();
		pnlFor = new JPanel();
		lblFor = new JLabel();
		edtFor = new JTextField();
		pnlRepeat = new JPanel();
		lblRepeat = new JLabel();
		edtRepeat = new JTextField();
		pnlWhile = new JPanel();
		lblWhile = new JLabel();
		edtWhile = new JTextField();
		buttonBar = new JPanel();
		btnOK = new JButton();
		altPadRight = new JCheckBox();

		//======== this ========
		setTitle("Structures Preferences");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setPreferredSize(new Dimension(429, 320));
			dialogPane.setRequestFocusEnabled(false);

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
				contentPanel.setLayout(new BorderLayout(5, 5));

				//======== pnlLeft ========
				{
					pnlLeft.setMaximumSize(new Dimension(2147483647, 2147483647));
					pnlLeft.setPreferredSize(new Dimension(200, 185));
					pnlLeft.setFocusCycleRoot(true);
					pnlLeft.setLayout(new BorderLayout(8, 8));

					//======== pnlAlt ========
					{
						pnlAlt.setBorder(new TitledBorder("IF statement"));
						pnlAlt.setLayout(new BorderLayout(8, 8));

						//======== pnlCases ========
						{
							pnlCases.setLayout(new BorderLayout(8, 8));

							//======== pnlAltLeft ========
							{
								pnlAltLeft.setPreferredSize(new Dimension(95, 44));
								pnlAltLeft.setLayout(new BorderLayout(8, 7));

								//---- lblAltTF ----
								lblAltT.setText("Label TRUE");
								pnlAltLeft.add(lblAltT, BorderLayout.NORTH);
								pnlAltLeft.add(edtAltT, BorderLayout.CENTER);
							}
							pnlCases.add(pnlAltLeft, BorderLayout.WEST);

							//======== pnlAltRight ========
							{
								pnlAltRight.setPreferredSize(new Dimension(95, 44));
								pnlAltRight.setLayout(new BorderLayout(8, 8));

								//---- lblAltF ----
								lblAltF.setText("Label FALSE");
								lblAltF.setHorizontalAlignment(SwingConstants.RIGHT);
								pnlAltRight.add(lblAltF, BorderLayout.NORTH);
								pnlAltRight.add(edtAltF, BorderLayout.CENTER);
							}
							pnlCases.add(pnlAltRight, BorderLayout.CENTER);
						}
						pnlAlt.add(pnlCases, BorderLayout.NORTH);

						//======== pnlContent ========
						{
							pnlContent.setLayout(new BorderLayout());

							//---- lblAltContent ----
							lblAltContent.setText("Default content");
							pnlContent.add(lblAltContent, BorderLayout.NORTH);
							pnlContent.add(edtAlt, BorderLayout.CENTER);
							altPadRight.setText("Enlarge FALSE");
							pnlContent.add(altPadRight, BorderLayout.SOUTH);
						}
						pnlAlt.add(pnlContent, BorderLayout.CENTER);
					}
					pnlLeft.add(pnlAlt, BorderLayout.NORTH);

					//======== pnlCase ========
					{
						pnlCase.setBorder(new TitledBorder("CASE statement"));
						pnlCase.setLayout(new BorderLayout());

						//---- lblCase ----
						lblCase.setText("Default content");
						pnlCase.add(lblCase, BorderLayout.NORTH);

						//======== scrollPane1 ========
						{
							scrollPane1.setViewportView(txtCase);
						}
						pnlCase.add(scrollPane1, BorderLayout.CENTER);
					}
					pnlLeft.add(pnlCase, BorderLayout.CENTER);
				}
				contentPanel.add(pnlLeft, BorderLayout.CENTER);

				//======== pnlRight ========
				{
					pnlRight.setMaximumSize(new Dimension(2147483647, 2147483647));
					pnlRight.setPreferredSize(new Dimension(200, 226));
					pnlRight.setLayout(new BorderLayout(8, 8));

					//======== pnlFor ========
					{
						pnlFor.setBorder(new TitledBorder("FOR loop"));
						pnlFor.setLayout(new BorderLayout(8, 8));

						//---- lblFor ----
						lblFor.setText("Default content");
						pnlFor.add(lblFor, BorderLayout.NORTH);
						pnlFor.add(edtFor, BorderLayout.CENTER);
					}
					pnlRight.add(pnlFor, BorderLayout.NORTH);

					//======== pnlRepeat ========
					{
						pnlRepeat.setBorder(new TitledBorder("REPEAT loop"));
						pnlRepeat.setLayout(new BorderLayout(8, 8));

						//---- lblRepeat ----
						lblRepeat.setText("Default content");
						pnlRepeat.add(lblRepeat, BorderLayout.NORTH);
						pnlRepeat.add(edtRepeat, BorderLayout.CENTER);
					}
					pnlRight.add(pnlRepeat, BorderLayout.SOUTH);

					//======== pnlWhile ========
					{
						pnlWhile.setBorder(new TitledBorder("WHILE loop"));
						pnlWhile.setAutoscrolls(true);
						pnlWhile.setLayout(new BorderLayout(8, 8));

						//---- lblWhile ----
						lblWhile.setText("Default content");
						pnlWhile.add(lblWhile, BorderLayout.NORTH);
						pnlWhile.add(edtWhile, BorderLayout.CENTER);
					}
					pnlRight.add(pnlWhile, BorderLayout.CENTER);
				}
				contentPanel.add(pnlRight, BorderLayout.EAST);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

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
					new Insets(0, 0, 0, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// Bob things
		btnOK.addActionListener(this);
		btnOK.addKeyListener(this);
		edtAltT.addKeyListener(this);
		edtAltF.addKeyListener(this);
		edtAlt.addKeyListener(this);
		txtCase.addKeyListener(this);
		edtFor.addKeyListener(this);
		edtWhile.addKeyListener(this);
		edtRepeat.addKeyListener(this);
		addKeyListener(this);
	}

		
	// listen to actions
	public void actionPerformed(ActionEvent event)
	{
		setVisible(false);
	}
	
	public void keyTyped(KeyEvent kevt) 
	{
    }
	
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
	
	public void keyReleased(KeyEvent ke)
	{
	} 
	
}

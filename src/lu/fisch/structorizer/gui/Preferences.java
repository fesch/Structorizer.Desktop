/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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
 *      Description:    This is the structure preferences window.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007-12-31      First Issue
 *      Kay Gürtzig     2016-11-01      Issue #81 (CPI awareness): Proper scaling of all explicit sizes
 *      Kay Gürtzig     2016-11-11      Issue #81: DPI-awareness workaround for checkboxes/radio buttons
 *      Kay Gürtzig     2017-01-07      Bugfix #330 (issue #81): checkbox scaling suppressed for "Nimbus" l&f
 *      Kay Gürtzig     2017-01-09      Issue #81 / bugfix #330: GUI scaling stuff outsourced to class GUIScaler
 *      Kay Gürtzig     2017-05-09      Issue #400: commit field OK introduced, keyListener at all controls
 *      Kay Gürtzig     2017-05-18      Issue #405: New option spnCaseRot introduced
 *      Kay Gürtzig     2017-06-08      Issue #405: dimension tuning for Nimbus L&F
 *      Kay Gürtzig     2019-03-22      Enh. #56: Preferences for Try blocks added, Layout revised
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2019-03-20 Kay Gürtzig
 *      - Inappropriate BorderLayouts replaced by BoxLayout or GridLayout, explicit size control reduced or disabled
 *      2016-11-01 Kay Gürtzig
 *      - Many aspects manually modified, Layout no longer compatible with JFormDesigner
 *      2007-12-31 Bob Fisch
 *      - I used JFormDesigner to design this window graphically.
 *
 ******************************************************************************************************///

import lu.fisch.structorizer.elements.Element;

import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangDialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Structure preferences dialog, allows to configure default contents for the
 * different kinds of {@link Element}.
 * Originally created via JFormDesigner on Mon Dec 31 09:04:29 CET 2007
 * @author Robert Fisch
 */
@SuppressWarnings("serial")
public class Preferences extends LangDialog implements ActionListener, KeyListener
{
	// START KGU#393 2017-05-09: Issue #400 - indicate whether changes are committed
	public boolean OK = false;
	// END KGU#393 2017-05-09
	
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
	// START KGU#376 2017-07-01: Enh. #389
	protected JPanel pnlLowerRight;
	protected JPanel pnlRoot;
	protected JLabel lblRoot;
	protected JTextField edtRoot;
	// END KGU#376 2017-07-01
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
	// START KGU#401 2017-05-18: Issue #405 (width-reduced Case elements by instruction rotation)
	protected JPanel pnlCaseRot;
	protected JLabel lblCaseRot;
	protected JSpinner spnCaseRot;
	// END KGU#401 2017-05-18
	// START KGU#686 2019-03-22: Enh. #56 (Try elements)
	protected JPanel pnlTry;
	protected JLabel lblTry;
	protected JLabel lblCatch;
	protected JLabel lblFinal;
	protected JTextField edtTry;
	protected JTextField edtCatch;
	protected JTextField edtFinal;
	// END KGU#686 2019-03-22
	
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
		// Originally generated using JFormDesigner Evaluation license - Robert Fisch
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
		// START KGU#376 2017-07-01: Enh. #389
		pnlLowerRight = new JPanel();
		pnlRoot = new JPanel();
		lblRoot = new JLabel();
		edtRoot = new JTextField();
		// END KGU#376 2017-07-01
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
		// START KGU#401 2017-05-18: Issue #405 (width-reduced Case elements by instruction rotation)
		pnlCaseRot = new JPanel();
		lblCaseRot = new JLabel();
		spnCaseRot = new JSpinner();
		// END KGU#401 2017-05-18
		// START KGU#686 2019-03-22: Enh. #56 (Try elements)
		pnlTry = new JPanel();
		lblTry = new JLabel();
		lblCatch = new JLabel();
		lblFinal = new JLabel();
		edtTry = new JTextField();
		edtCatch = new JTextField();
		edtFinal = new JTextField();
		// END KGU#686 2019-03-22

		//======== this ========
		setTitle("Structures Preferences");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
			//dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
			int border = (int)(12 * scaleFactor);
			dialogPane.setBorder(new EmptyBorder(border, border, border, border));
			// START KGU#401 2017-06-08: Enh. #405 We need more space for Nimbus L&F
			double nimbusFactor = 1.0;
			if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
				nimbusFactor = 1.25;				
			}
			// END KGU#401 2017-06-08
			// END KGU#287 2016-11-01
			dialogPane.setRequestFocusEnabled(false);

			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				// START KGU#287 2019-03-22: Issue #81 (DPI awareness)
				//contentPanel.setLayout(new BorderLayout(5, 5));
				border = (int)(5 * scaleFactor);
				contentPanel.setLayout(new GridLayout(1, 0, border, 0));
				// END KGU#287 2019-03-22

				//======== pnlLeft ========
				{
					pnlLeft.setMaximumSize(new Dimension(2147483647, 2147483647));
					// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
					border = (int)(8 * scaleFactor);
					// END KGU#287 2016-11-01
					pnlLeft.setFocusCycleRoot(true);
					pnlLeft.setLayout(new BorderLayout(border, border));

					//======== pnlAlt ========
					{
						pnlAlt.setBorder(new TitledBorder("IF statement"));
						pnlAlt.setLayout(new BorderLayout(border, border));

						//======== pnlCases ========
						{
							pnlCases.setLayout(new GridLayout(1, 0, border, border));
							int width = (int)(95 * scaleFactor * nimbusFactor);

							//======== pnlAltLeft ========
							{
								pnlAltLeft.setLayout(new BorderLayout(border, border-1));

								//---- lblAltTF ----
								lblAltT.setText("Label TRUE");
								lblAltT.setHorizontalAlignment(SwingConstants.LEFT);
								pnlAltLeft.add(lblAltT, BorderLayout.NORTH);
								pnlAltLeft.add(edtAltT, BorderLayout.CENTER);
								pnlAltLeft.setPreferredSize(new Dimension(width,
										pnlAltLeft.getPreferredSize().height));
							}
							pnlCases.add(pnlAltLeft);

							//======== pnlAltRight ========
							{
								pnlAltRight.setLayout(new BorderLayout(border, border));

								//---- lblAltF ----
								lblAltF.setText("Label FALSE");
								lblAltF.setHorizontalAlignment(SwingConstants.RIGHT);
								pnlAltRight.add(lblAltF, BorderLayout.NORTH);
								pnlAltRight.add(edtAltF, BorderLayout.CENTER);
								pnlAltRight.setPreferredSize(new Dimension(width,
										pnlAltRight.getPreferredSize().height));
							}
							pnlCases.add(pnlAltRight);
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

						// START KGU#401 2017-05-18: Issue #405 (width-reduced Case elements by instruction rotation)
						pnlCaseRot.setLayout(new BorderLayout(border, border));
						lblCaseRot.setText("Min. branches for rotation");
						spnCaseRot.setModel(new SpinnerNumberModel(0, 0, 20, 1));
						pnlCaseRot.add(lblCaseRot, BorderLayout.WEST);
						pnlCaseRot.add(spnCaseRot, BorderLayout.EAST);
						pnlCase.add(pnlCaseRot, BorderLayout.SOUTH);
						// END KGU#401 2017-05-18
					}
					pnlLeft.add(pnlCase, BorderLayout.CENTER);
				}
				contentPanel.add(pnlLeft/*, BorderLayout.CENTER*/);

				//======== pnlRight ========
				{
					border = (int)(8 * scaleFactor);
					pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));

					// START KGU#376 2017-07-01: Enh. #389
					//======== pnlRoot ========
					{
						pnlRoot.setBorder(new TitledBorder("Diagram header"));
						pnlRoot.setLayout(new GridLayout(0, 1, 1, 1));

						//---- lblFor ----
						lblRoot.setText("Include list caption");
						pnlRoot.add(lblRoot);
						pnlRoot.add(edtRoot);
					}
					pnlRight.add(pnlRoot);
					// END KGU#376 2017-07-01

					pnlRight.add(Box.createVerticalStrut(border));
					
					//======== pnlFor ========
					{
						pnlFor.setBorder(new TitledBorder("FOR loop"));
						pnlFor.setLayout(new GridLayout(0, 1, 1, 1));

						//---- lblFor ----
						lblFor.setText("Default content");
						pnlFor.add(lblFor);
						pnlFor.add(edtFor);
					}
					pnlRight.add(pnlFor);

					pnlRight.add(Box.createVerticalStrut(border));

					//======== pnlRepeat ========
					{
						pnlRepeat.setBorder(new TitledBorder("REPEAT loop"));
						pnlRepeat.setLayout(new GridLayout(0, 1, 1, 1));

						//---- lblRepeat ----
						lblRepeat.setText("Default content");
						pnlRepeat.add(lblRepeat);
						pnlRepeat.add(edtRepeat);
					}
					pnlRight.add(pnlRepeat);

					pnlRight.add(Box.createVerticalStrut(border));

					//======== pnlWhile ========
					{
						pnlWhile.setBorder(new TitledBorder("WHILE loop"));
						pnlWhile.setLayout(new GridLayout(0, 1, 1, 1));

						//---- lblWhile ----
						lblWhile.setText("Default content");
						pnlWhile.add(lblWhile);
						pnlWhile.add(edtWhile);
					}
					pnlRight.add(pnlWhile);

					pnlRight.add(Box.createVerticalStrut(border));

					// START KGU#686 2019-03-22: Enh. #56
					//======== pnlTry ========
					{
						pnlTry.setBorder(new TitledBorder("TRY block labels"));
						pnlTry.setLayout(new GridLayout(2, 3, 1, 1));
						lblTry.setText("Try");
						lblCatch.setText("Catch");
						lblFinal.setText("Finally");
						pnlTry.add(lblTry);
						pnlTry.add(lblCatch);
						pnlTry.add(lblFinal);
						pnlTry.add(edtTry);
						pnlTry.add(edtCatch);
						pnlTry.add(edtFinal);
					}
					pnlRight.add(pnlTry);
					// END KGU#686 2019-03-22
					
				}
				contentPanel.add(pnlRight);
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(new EmptyBorder((int)(12*scaleFactor), 0, 0, 0));
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
			// START KGU#686 2019-03-22: All works way better now without this attempt to control the size
			//dialogPane.setMinimumSize(new Dimension((int)(scaleFactor * 600), (int)(scaleFactor * nimbusFactor * 400)));
			//dialogPane.setPreferredSize(new Dimension((int)(scaleFactor * 600), (int)(scaleFactor * nimbusFactor * 400)));
			//dialogPane.setSize(new Dimension((int)(scaleFactor * 600), (int)(scaleFactor * nimbusFactor * 400)));
			// END KGU#686 2019-03-22
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		
		// START KGU#287 2017-01-09: Issue #81 / bugfix #330: GUI scaling
		GUIScaler.rescaleComponents(this);
		// END KGU#287 2017-01-09
		
		pack();
		setLocationRelativeTo(getOwner());
		
		btnOK.addActionListener(this);
		btnOK.addKeyListener(this);
		edtAltT.addKeyListener(this);
		edtAltF.addKeyListener(this);
		edtAlt.addKeyListener(this);
		txtCase.addKeyListener(this);
		edtFor.addKeyListener(this);
		edtWhile.addKeyListener(this);
		edtRepeat.addKeyListener(this);
		// START KGU#394/KGU#401 2017-11-06: Issue #401, #405
		((JSpinner.DefaultEditor)spnCaseRot.getEditor()).getTextField().addKeyListener(this);
		// END KGU#394/KGU#401 2017-11-06
		// START KGU#394/KGU#376 2017-07-01: Enh. #389, #401
		edtRoot.addKeyListener(this);
		// END KGU#376 2017-07-01
		// START KGU#686 2019-03-22: Enh. #56
		edtTry.addKeyListener(this);
		edtCatch.addKeyListener(this);
		edtFinal.addKeyListener(this);
		// END KGU#686 2019-03-22
		addKeyListener(this);
		
	}

		
	// listen to actions
	public void actionPerformed(ActionEvent event)
	{
		// START KGU#393 2017-05-09: Issue #400 - indicate that changes are committed
		if (event.getSource() == btnOK) {
			OK = true;
		}
		// END KGU#393 2017-05-09
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
			// START KGU#393 2017-05-09: Issue #400 - indicate that changes are committed
			OK = true;
			// END KGU#393 2017-05-09
			setVisible(false);
		}
    }
	
	public void keyReleased(KeyEvent ke)
	{
	} 
	
}

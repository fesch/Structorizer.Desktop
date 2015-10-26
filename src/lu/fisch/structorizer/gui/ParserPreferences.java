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
 *      Description:    This is the parser preferences dialog window.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2008.01.03      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:		I used JFormDesigner to desin this window graphically.
 *
 ******************************************************************************************************///

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
/*
 * Created by JFormDesigner on Thu Jan 03 15:19:25 CET 2008
 */



/**
 * @author Robert Fisch
 */
public class ParserPreferences extends LangDialog {
    
        public boolean OK = false;

	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Robert Fisch
	protected JPanel dialogPane;
	protected JPanel contentPanel;
	protected JLabel lblNothing;
	protected JLabel lblPre;
	protected JLabel lblPost;
	protected JLabel lblNothing2;
	protected JLabel lblNothing3;
	protected JLabel lblInput;
	protected JLabel lblOutput;
	protected JLabel lblAlt;
	protected JTextField edtAltPre;
	protected JTextField edtAltPost;
	protected JTextField edtInput;
	protected JTextField edtOutput;
	protected JLabel lblCase;
	protected JTextField edtCasePre;
	protected JTextField edtCasePost;
	protected JLabel lblFor;
	protected JTextField edtForPre;
	protected JTextField edtForPost;
	protected JLabel lblWhile;
	protected JTextField edtWhilePre;
	protected JTextField edtWhilePost;
	protected JLabel lblRepeat;
	protected JTextField edtRepeatPre;
	protected JTextField edtRepeatPost;
	protected JPanel buttonBar;
	protected JButton btnOK;
        protected JLabel lblErrorSign;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	/*public ParserPreferences()
	{
		super();
		setModal(true);
		initComponents();
	}*/
	
	public ParserPreferences(Frame owner) {
		super(owner);
                setModal(true);
		initComponents();
	}
	
	/*public ParserPreferences(Dialog owner) {
		super(owner);
		initComponents();
	}*/

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Robert Fisch
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		lblNothing = new JLabel();
		lblNothing2 = new JLabel();
		lblNothing3 = new JLabel();
		lblPre = new JLabel();
		lblPost = new JLabel();
		lblInput = new JLabel();
		lblOutput = new JLabel();
		lblAlt = new JLabel();
		edtAltPre = new JTextField();
		edtAltPost = new JTextField();
		lblCase = new JLabel();
		edtCasePre = new JTextField();
		edtCasePost = new JTextField();
		lblFor = new JLabel();
		edtForPre = new JTextField();
		edtForPost = new JTextField();
		lblWhile = new JLabel();
		edtWhilePre = new JTextField();
		edtWhilePost = new JTextField();
		lblRepeat = new JLabel();
		edtRepeatPre = new JTextField();
		edtRepeatPost = new JTextField();
		buttonBar = new JPanel();
		btnOK = new JButton();
		edtInput = new JTextField();
		edtOutput = new JTextField();
                lblErrorSign = new JLabel();
                
                lblErrorSign.setText("Your are not allowed to use the sign ':' in any parser string!");

		//======== this ========
		setModal(true);
		setResizable(false);
		setTitle("Parser Preferences");
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
				contentPanel.setLayout(new GridLayout(8, 3, 8, 8));
				contentPanel.add(lblNothing);

				//---- lblPre ----
				lblPre.setText("Pre");
				contentPanel.add(lblPre);

				//---- lblPost ----
				lblPost.setText("Post");
				contentPanel.add(lblPost);

				//---- lblAlt ----
				lblAlt.setText("IF statement");
				contentPanel.add(lblAlt);
				contentPanel.add(edtAltPre);
				contentPanel.add(edtAltPost);

				//---- lblCase ----
				lblCase.setText("CASE statement");
				contentPanel.add(lblCase);
				contentPanel.add(edtCasePre);
				contentPanel.add(edtCasePost);

				//---- lblFor ----
				lblFor.setText("FOR loop");
				contentPanel.add(lblFor);
				contentPanel.add(edtForPre);
				contentPanel.add(edtForPost);

				//---- lblWhile ----
				lblWhile.setText("WHILE loop");
				contentPanel.add(lblWhile);
				contentPanel.add(edtWhilePre);
				contentPanel.add(edtWhilePost);

				//---- lblRepeat ----
				lblRepeat.setText("REPEAT loop");
				contentPanel.add(lblRepeat);
				contentPanel.add(edtRepeatPre);
				contentPanel.add(edtRepeatPost);
				
				contentPanel.add(lblNothing2);
				lblInput.setText("Input");
				contentPanel.add(lblInput);
				lblOutput.setText("Output");
				contentPanel.add(lblOutput);

				contentPanel.add(lblNothing3);
				contentPanel.add(edtInput);
				contentPanel.add(edtOutput);
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
					new Insets(0, 0, 5, 0), 0, 0));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// BOB thinks
		
		// add the LIST-listeners
		// add the KEY-listeners
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
                                    done();
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		edtAltPre.addKeyListener(keyListener);
		edtAltPost.addKeyListener(keyListener);
		edtCasePre.addKeyListener(keyListener);
		edtCasePost.addKeyListener(keyListener);
		edtForPre.addKeyListener(keyListener);
		edtForPost.addKeyListener(keyListener);
		edtWhilePre.addKeyListener(keyListener);
		edtWhilePost.addKeyListener(keyListener);
		edtRepeatPre.addKeyListener(keyListener);
		edtRepeatPost.addKeyListener(keyListener);
		edtInput.addKeyListener(keyListener);
		edtOutput.addKeyListener(keyListener);
		btnOK.addKeyListener(keyListener);
		
		// add the ACTION-listeners
		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				done();
			}
		};
		btnOK.addActionListener(actionListener);
	}
        
        public void done()
        {
            if(
                    edtAltPre.getText().contains(":") ||
                    edtAltPost.getText().contains(":") ||
                    edtCasePre.getText().contains(":") ||
                    edtCasePost.getText().contains(":") ||
                    edtForPre.getText().contains(":") ||
                    edtForPost.getText().contains(":") ||
                    edtWhilePre.getText().contains(":") ||
                    edtWhilePost.getText().contains(":") ||
                    edtRepeatPre.getText().contains(":") ||
                    edtRepeatPost.getText().contains(":") ||
                    edtInput.getText().contains(":") ||
                    edtOutput.getText().contains(":")
            ) {
                 JOptionPane.showMessageDialog(ParserPreferences.this, lblErrorSign.getText(),"Error", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                setVisible(false);
                OK=true;
            }    
            
        }

}

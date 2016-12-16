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
 *      Description:    Dialog for configuration of diagram saving options (enhancement #310)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016.12.15      First Issue for enh. #310
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import lu.fisch.structorizer.locales.LangDialog;

/**
 * Dialog for the configuration of diagram saving options
 * @author Kay Gürtzig
 *
 */
@SuppressWarnings("serial")
public class SaveOptionDialog extends LangDialog {
	
    public boolean goOn = false;

	public SaveOptionDialog()
	{
	    initComponents();
	    setModal(true);
	}
	
	public SaveOptionDialog(Frame frame)
	{
	    initComponents();
	    setModal(true);
	    setLocationRelativeTo(frame);
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	//@SuppressWarnings("unchecked")
	private void initComponents() {

	    pnlTop = new javax.swing.JPanel();
	    pnlButtons = new javax.swing.JPanel();
	    pnlOptions = new javax.swing.JPanel();
	    pnlWrapper = new javax.swing.JPanel();
	    pnlAutoSave = new javax.swing.JPanel();
	    pnlBackup = new javax.swing.JPanel();
	    lbIntro = new javax.swing.JLabel();
	    btnOk = new javax.swing.JButton();

		chkAutoSaveExecute = new javax.swing.JCheckBox("Auto-save during execution?");
		chkAutoSaveClose  = new javax.swing.JCheckBox("Auto-save when going to be closed?");
		chkBackupFile = new javax.swing.JCheckBox("Create backup file on re-saving?") ;

	    setTitle("Preferences for Saving ...");

	    org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(pnlTop);
	    pnlTop.setLayout(jPanel1Layout);
	    jPanel1Layout.setHorizontalGroup(
	        jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
	        .add(0, 0, Short.MAX_VALUE)
	    );
	    jPanel1Layout.setVerticalGroup(
	        jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
	        .add(0, 0, Short.MAX_VALUE)
	    );


	    lbIntro.setText("Please select the options you want to activate ...");

	    btnOk.setText("OK");
	    btnOk.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent evt) {
	            btnOkActionPerformed(evt);
	        }
	    });

	    Container content = getContentPane();
	    content.setLayout(new BorderLayout());
	    
	    pnlTop.setLayout(new GridLayout(1,1,4,4));
	    pnlTop.setBorder(new EmptyBorder(12,12,0,12));
	    pnlTop.add(lbIntro);
	    
	    pnlAutoSave.setBorder(new TitledBorder("Auto-save options"));
	    pnlAutoSave.setLayout(new GridLayout(0, 1, 0 , 1));
	    pnlAutoSave.add(this.chkAutoSaveExecute);
	    pnlAutoSave.add(this.chkAutoSaveClose);
	    
	    pnlBackup.setBorder(new TitledBorder("Backup options"));
	    pnlBackup.setLayout(new GridLayout(0, 1, 0, 1));
	    pnlBackup.add(this.chkBackupFile);
	    

	    pnlOptions.setLayout(new GridLayout(0,1,4,4));
	    pnlOptions.setBorder(new EmptyBorder(12,12,12,12));
	    pnlOptions.add(pnlAutoSave, BorderLayout.CENTER);
	    pnlOptions.add(pnlBackup, BorderLayout.CENTER);
	    //pnlOptions.add(pnlPreference, BorderLayout.CENTER);
	    
	    pnlButtons.setLayout(new BorderLayout());
	    pnlButtons.setBorder(new EmptyBorder(12,12,12,12));
	    pnlButtons.add(btnOk, BorderLayout.EAST);
	    
	    pnlWrapper.add(pnlOptions);
	    
	    content.add(pnlTop, BorderLayout.NORTH);
	    content.add(pnlWrapper, BorderLayout.CENTER);
	    content.add(pnlButtons, BorderLayout.SOUTH);

	    pack();
	    
	    
	}

    private void btnOkActionPerformed(ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {
        goOn = true;
        this.setVisible(false);
    }

//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[])
//    {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try
//        {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
//            {
//                if ("Nimbus".equals(info.getName()))
//                {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex)
//        {
//            java.util.logging.Logger.getLogger(SaveOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex)
//        {
//            java.util.logging.Logger.getLogger(SaveOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex)
//        {
//            java.util.logging.Logger.getLogger(SaveOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex)
//        {
//            java.util.logging.Logger.getLogger(SaveOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable()
//        {
//
//            public void run()
//            {
//                new SaveOptionDialog().setVisible(true);
//            }
//        });
//    }

    // Variables declaration
	private javax.swing.JPanel pnlTop;
	private javax.swing.JPanel pnlButtons;
	private javax.swing.JPanel pnlOptions;
	private javax.swing.JPanel pnlWrapper;
	public javax.swing.JPanel pnlAutoSave;
	public javax.swing.JPanel pnlBackup;
	public javax.swing.JButton btnOk;
	public javax.swing.JLabel lbIntro;
	public javax.swing.JCheckBox chkAutoSaveExecute;
	public javax.swing.JCheckBox chkAutoSaveClose;
	public javax.swing.JCheckBox chkBackupFile;
	
}


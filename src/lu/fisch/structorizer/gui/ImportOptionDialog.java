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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.Set;

import javax.swing.border.EmptyBorder;

import lu.fisch.structorizer.locales.LangDialog;

/*
 ******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This dialog allows to control certain settings for the file import.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      kay             2016.09.25      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************/
//

/**
 * This Dialog allows to control certain settings for the file import.
 * @author kay
 */
@SuppressWarnings("serial")
public class ImportOptionDialog extends LangDialog {

    public boolean goOn = false;

    /** Creates new form ExportOptionDialogue */
    public ImportOptionDialog()
    {
        initComponents();
        setModal(true);
    }

    public ImportOptionDialog(Frame frame)
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanelCB = new javax.swing.JPanel();
        chkRefactorOnLoading = new javax.swing.JCheckBox();
        lbIntro = new javax.swing.JLabel();
        btnOk = new javax.swing.JButton();
        lbCharset = new javax.swing.JLabel();
        cbCharset = new javax.swing.JComboBox<String>();
        chkCharsetAll = new javax.swing.JCheckBox();
        // END KGU#168 2016-04-04

        setTitle("Import options ...");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        lbCharset.setText("Character Set: ");
        lbCharset.setMinimumSize(
        		new Dimension(lbCharset.getMinimumSize().width, cbCharset.getPreferredSize().height));
        cbCharset.setMaximumSize(
        		new Dimension(150, cbCharset.getPreferredSize().height));
        charsetListChanged(null);
        chkCharsetAll.setText("List all?");
        chkCharsetAll.setMinimumSize(
        		new Dimension(chkCharsetAll.getMinimumSize().width, cbCharset.getPreferredSize().height));
        chkCharsetAll.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		charsetListChanged((String)cbCharset.getSelectedItem());
        	}
        });
        // END KGU#168 2016-04-04

        // START KGU#162 2016-03-31: Enh. #144 - now option to suppress all content transformation
        chkRefactorOnLoading.setText("Replace keywords on loading a diagram (refactoring).");
        chkRefactorOnLoading.setToolTipText("Select this option if all configurable keywords in the daiagram are to be adapted to the current parser preferences.");
        // END KGU#162 2016-03-31

        lbIntro.setText("Please select the options you want to activate ...");

        btnOk.setText("OK");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        
        jPanel1.setLayout(new GridLayout(3,1,4,4));
        jPanel1.setBorder(new EmptyBorder(12,12,0,12));
        jPanel1.add(lbIntro);
        
        jPanel2.setLayout(new GridLayout(1, 3, 8, 8));
        jPanel2.add(lbCharset);
        jPanel2.add(cbCharset);
        jPanel2.add(chkCharsetAll);

        jPanelCB.setLayout(new GridLayout(2,1,4,4));
        jPanelCB.setBorder(new EmptyBorder(12,12,12,12));
        jPanelCB.add(jPanel2);
        jPanelCB.add(chkRefactorOnLoading, BorderLayout.CENTER);
        
        jPanel3.setLayout(new BorderLayout());
        jPanel3.setBorder(new EmptyBorder(12,12,12,12));
        jPanel3.add(btnOk, BorderLayout.EAST);
        
        content.add(jPanel1, BorderLayout.NORTH);
        content.add(jPanelCB, BorderLayout.CENTER);
        content.add(jPanel3, BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        goOn = true;
        this.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    public void charsetListChanged(String favouredCharset)
    {
    	Set<String> availableCharsets = Charset.availableCharsets().keySet();
    	cbCharset.removeAllItems();
        if (chkCharsetAll.isSelected())
        {
        	for (String charsetName : availableCharsets)
        	{
        		cbCharset.addItem(charsetName);
        	}
        }
        else
        {
        	boolean inNewList = false;
        	for (int i = 0; i < standardCharsets.length; i++)
        	{
        		String charsetName = standardCharsets[i];
        		if (availableCharsets.contains(charsetName))
        		{
        			cbCharset.addItem(charsetName);
        			if (favouredCharset != null && favouredCharset.equals(charsetName))
        			{
        				inNewList = true;
        			}
        		}
        	}
        	if (favouredCharset != null && !inNewList && availableCharsets.contains(favouredCharset))
        	{
        		cbCharset.insertItemAt(favouredCharset,  0);
        	}
        }
    	if (favouredCharset != null)
    	{
    		cbCharset.setSelectedItem(favouredCharset);
    	}
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(ExportOptionDialoge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(ExportOptionDialoge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(ExportOptionDialoge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(ExportOptionDialoge.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            public void run()
            {
                new ImportOptionDialog().setVisible(true);
            }
        });
    }
    
    public static String[] standardCharsets = {"ISO-8859-1", "UTF-8", "UTF-16", "windows-1250", "windows-1252", "US-ASCII"};
    
    // Variables declaration
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelCB;
    public javax.swing.JButton btnOk;
    public javax.swing.JLabel lbIntro;
    public javax.swing.JCheckBox chkRefactorOnLoading;
    public javax.swing.JLabel lbCharset;
    public javax.swing.JComboBox<String> cbCharset;
    public javax.swing.JCheckBox chkCharsetAll;
    // End of variables declaration

}

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
 *      Author:         Kay Gürtzig
 *
 *      Description:    This dialog allows to control certain settings for the file import.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date        Description
 *      ------          ----        -----------
 *      kay             2016.09.25  First Issue
 *      Kay Gürtzig     2016.11.11  Issue #81: DPI-awareness workaround for checkboxes
 *      Kay Gürtzig     2017.01.07  Bugfix #330 (issue #81): checkbox scaling suppressed for "Nimbus" l&f
 *      Kay Gürtzig     2017.01.09  Bugfix #330 (issue #81): scaling stuff outsourced to class GUIScaler
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************/
//

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
import javax.swing.border.TitledBorder;

import lu.fisch.structorizer.locales.LangDialog;

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

        pnlTop = new javax.swing.JPanel();
        pnlCharSet = new javax.swing.JPanel();
        pnlButtons = new javax.swing.JPanel();
        pnlOptions = new javax.swing.JPanel();
        pnlWrapper = new javax.swing.JPanel();
        pnlCode = new javax.swing.JPanel();
        pnlNSD = new javax.swing.JPanel();
        pnlPreference = new javax.swing.JPanel();
        chkRefactorOnLoading = new javax.swing.JCheckBox();
        //chkOfferRefactoringIni = new javax.swing.JCheckBox();
        lbIntro = new javax.swing.JLabel();
        btnOk = new javax.swing.JButton();
        lbCharset = new javax.swing.JLabel();
        cbCharset = new javax.swing.JComboBox<String>();
        chkCharsetAll = new javax.swing.JCheckBox();

        setTitle("Import options ...");

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

        lbCharset.setText("Character Set: ");
        lbCharset.setMinimumSize(
        		new Dimension(lbCharset.getMinimumSize().width, cbCharset.getPreferredSize().height));
        cbCharset.setMaximumSize(
        		new Dimension(150, cbCharset.getPreferredSize().height));
        charsetListChanged(null);
        cbCharset.setMaximumSize(cbCharset.getPreferredSize());
        chkCharsetAll.setText("List all?");
        chkCharsetAll.setMinimumSize(
        		new Dimension(chkCharsetAll.getMinimumSize().width, cbCharset.getPreferredSize().height));
        chkCharsetAll.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		charsetListChanged((String)cbCharset.getSelectedItem());
        	}
        });

        chkRefactorOnLoading.setText("Replace keywords on loading a diagram (refactoring).");
        chkRefactorOnLoading.setToolTipText("Select this option if all configurable keywords in the daiagram are to be adapted to the current parser preferences.");
        chkRefactorOnLoading.setAlignmentX(LEFT_ALIGNMENT);

        //chkOfferRefactoringIni.setText("Offer refactoring on loading preferences from file.");
        //chkOfferRefactoringIni.setToolTipText("Select this option if you want to be asked whether to refactor diagrams whenever you load preferences from file.");
        //chkOfferRefactoringIni.setAlignmentX(LEFT_ALIGNMENT);

        lbIntro.setText("Please select the options you want to activate ...");

        btnOk.setText("OK");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        
        pnlTop.setLayout(new GridLayout(1,1,4,4));
        pnlTop.setBorder(new EmptyBorder(12,12,0,12));
        pnlTop.add(lbIntro);
        
        pnlCharSet.setLayout(new GridLayout(1, 3, 8, 8));
        pnlCharSet.add(lbCharset);
        pnlCharSet.add(cbCharset);
        pnlCharSet.add(chkCharsetAll);
        
        pnlCode.setBorder(new TitledBorder("Code Files"));
        pnlCode.setLayout(new GridLayout(0, 1, 0 , 1));
        pnlCode.add(pnlCharSet);
        
        pnlNSD.setBorder(new TitledBorder("NSD Files"));
        pnlNSD.setLayout(new GridLayout(0, 1, 0, 1));
        pnlNSD.add(chkRefactorOnLoading);
        
        pnlPreference.setBorder(new TitledBorder("Preference Files"));
        pnlPreference.setLayout(new GridLayout(0, 1, 0, 1));
        //pnlPreference.add(chkOfferRefactoringIni);

        pnlOptions.setLayout(new GridLayout(0,1,4,4));
        pnlOptions.setBorder(new EmptyBorder(12,12,12,12));
        pnlOptions.add(pnlCode, BorderLayout.CENTER);
        pnlOptions.add(pnlNSD, BorderLayout.CENTER);
        //pnlOptions.add(pnlPreference, BorderLayout.CENTER);
        
        pnlButtons.setLayout(new BorderLayout());
        pnlButtons.setBorder(new EmptyBorder(12,12,12,12));
        pnlButtons.add(btnOk, BorderLayout.EAST);
        
        pnlWrapper.add(pnlOptions);
        
        content.add(pnlTop, BorderLayout.NORTH);
        content.add(pnlWrapper, BorderLayout.CENTER);
        content.add(pnlButtons, BorderLayout.SOUTH);
        
        // START KGU#287 2017-01-09: Issues #81, #330
        GUIScaler.rescaleComponents(this);
        // END KGU#287 2017-01-09

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
            java.util.logging.Logger.getLogger(ImportOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(ImportOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(ImportOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(ImportOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
    private javax.swing.JPanel pnlTop;
    private javax.swing.JPanel pnlCharSet;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlOptions;
    private javax.swing.JPanel pnlWrapper;
    public javax.swing.JPanel pnlNSD;
    public javax.swing.JPanel pnlPreference;
    public javax.swing.JPanel pnlCode;
    public javax.swing.JButton btnOk;
    public javax.swing.JLabel lbIntro;
    public javax.swing.JCheckBox chkRefactorOnLoading;
    // START KGU#266 2016-09-30: Enh. selective loading of preferences
//    public javax.swing.JCheckBox chkPrefFont;
//    public javax.swing.JCheckBox chkPrefColors;
//    public javax.swing.JCheckBox chkPrefStructures;
//    public javax.swing.JCheckBox chkPrefParser;
//    public javax.swing.JCheckBox chkPrefAnalyser;
//    public javax.swing.JCheckBox chkPrefExport;
//    public javax.swing.JCheckBox chkPrefImport;
//    public javax.swing.JCheckBox chkPrefLanguage;
//    public javax.swing.JCheckBox chkPrefLaF;
//    public javax.swing.JCheckBox chkSettings;
    // END KGU#266 2016-09-30
    public javax.swing.JLabel lbCharset;
    public javax.swing.JComboBox<String> cbCharset;
    public javax.swing.JCheckBox chkCharsetAll;
    // End of variables declaration

}

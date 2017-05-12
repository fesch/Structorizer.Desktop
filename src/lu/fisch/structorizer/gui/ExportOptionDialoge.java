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
 *      Description:    This dialog allows to control certain settings for the code export.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date         Description
 *      ------          ----         -----------
 *      Bob Fisch       2012.07.02   First Issue
 *      Kay Gürtzig     2016.04.01   Enh. #144: noConversionCheckBox and cbPrefGenerator added
 *      Kay Gürtzig     2016.04.04   Enh. #149: cbCharset added
 *      Kay Gürtzig     2016.07.20   Enh. #160: new option to involve called subroutines (= KGU#178)
 *      Kay Gürtzig     2016.07.25   Size setting dropped. With the current layout, pack() is fine (KGU#212).
 *      Kay Gürtzig     2016.07.26   Bug #204: Constructor API modified to ensure language translation before pack()
 *      Kay Gürtzig     2016.11.11   Issue #81: DPI-awareness workaround for checkboxes
 *      Kay Gürtzig     2017.01.07   Bugfix #330 (issue #81): checkbox scaling suppressed for "Nimbus" l&f
 *      Kay Gürtzig     2017.01.09   Bugfix #330 (issue #81): Rescaling stuff outsourced to class GUIScaler
 *      Kay Gürtzig     2017.02.27   Enh. #346: New tab for configuration of user-specific include directives
 *      Kay Gürtzig     2017.05.09   Issue #400: keyListener at all controls
 *      Kay Gürtzig     2017.05.11   Enh. #372: New option to export license attributes 
 *
 ******************************************************************************************************
 *
 *      Comment:		I used JFormDesigner to design this window graphically.
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.parsers.GENParser;

/**
 * This dialog allows to control certain settings for the code export.
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class ExportOptionDialoge extends LangDialog
{
    public boolean goOn = false;

    /** Creates new form ExportOptionDialogue */
    public ExportOptionDialoge()
    {
        initComponents();
        setModal(true);
    }

    public ExportOptionDialoge(Frame frame) //, String langFileName)
    {
        initComponents();
        setModal(true);
        setLocationRelativeTo(frame);
    }

    /** 
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

    	// START KGU#351 2017-02-26: Enh. #346
    	tabbedPane = new javax.swing.JTabbedPane();
    	contentPanel0 = new javax.swing.JPanel();
    	contentPanel1 = new javax.swing.JPanel();
    	buttonBar = new javax.swing.JPanel();
    	// END KGU#351 2017-02-26
        //jPanel1 = new javax.swing.JPanel();
        // START KGU#162 2016-03-31: Enh. #144 - now option to suppress all content transformation
        noConversionCheckBox = new javax.swing.JCheckBox();
        // END KGU#162 2016-03-31
        commentsCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        bracesCheckBox = new javax.swing.JCheckBox();
        lineNumbersCheckBox = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        // START KGU#171 2016-04-01: Enh. #144 - new: preferred code export language
        lbVoid = new javax.swing.JLabel();
        lbPrefGenerator = new javax.swing.JLabel();
        cbPrefGenerator = new javax.swing.JComboBox<String>(this.getCodeGeneratorNames());
        // END KGU#171 2016-04-01
        // START KGU#168 2016-04-04: Issue #149
        lbVoid1 = new javax.swing.JLabel();
        lbCharset = new javax.swing.JLabel();
        cbCharset = new javax.swing.JComboBox<String>();
        chkCharsetAll = new javax.swing.JCheckBox();
        // END KGU#168 2016-04-04
        // START KGU#178 2016-07-20: Enh. #160
        chkExportSubroutines = new javax.swing.JCheckBox();
        // END KGU#178 2016-07-20
        // START KGU#363 2017-05-11: Enh. #372
        chkExportLicenseInfo = new javax.swing.JCheckBox();
        // END KGU#363 2017-05-11
        
        setTitle("Export options ...");

        //org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        //jPanel1.setLayout(jPanel1Layout);
        //jPanel1Layout.setHorizontalGroup(
        //    jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        //    .add(0, 0, Short.MAX_VALUE)
        //);
        //jPanel1Layout.setVerticalGroup(
        //    jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        //    .add(0, 0, Short.MAX_VALUE)
        //);

        // START KGU#168 2016-04-04: Issue #149
        lbVoid1.setText(" ");	// FIXME: Can we replace this by insets?
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

        // START KGU#171 2016-04-01: Enh. #144 - new: preferred code export language
        lbVoid.setText(" ");	// FIXME: Can we replace this by insets?
        lbVoid.setMinimumSize(
        		new Dimension(lbVoid.getMinimumSize().width, cbPrefGenerator.getPreferredSize().height));
        lbPrefGenerator.setText("Favorite Code Export:");
        lbPrefGenerator.setMinimumSize(
        		new Dimension(lbPrefGenerator.getMinimumSize().width, cbPrefGenerator.getPreferredSize().height));
        cbPrefGenerator.setMaximumSize(
        		new Dimension(150, cbPrefGenerator.getPreferredSize().height));
        cbPrefGenerator.setMaximumRowCount(cbPrefGenerator.getItemCount());
        cbPrefGenerator.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                preferredGeneratorChanged(evt);
            }
        });
        // END KGU#17 2016-04-01

        // START KGU#162 2016-03-31: Enh. #144 - now option to suppress all content transformation
        noConversionCheckBox.setText("No conversion of the expression/instruction contents.");
        noConversionCheckBox.setToolTipText("Select this option if the text content of your elements already represents target language syntax.");
        // END KGU#162 2016-03-31

        commentsCheckBox.setText("Export instructions as comments.");
//        commentsCheckBox.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                commentsCheckBoxActionPerformed(evt);
//            }
//        });

        jLabel1.setText("Please select the options you want to activate ...");

        bracesCheckBox.setText("Put block-opening brace on same line (C/C++/Java etc.).");
        //bracesCheckBox.setActionCommand("Put block-opening brace on same line (C/C++/Java etc.).");	// ??
        bracesCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bracesCheckBoxActionPerformed(evt);
            }
        });

        lineNumbersCheckBox.setText("Generate line numbers on export to BASIC.");
        lineNumbersCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                lineNumbersCheckBoxActionPerformed(evt);
            }
        });

        // START KGU#178 2016-07-20: Enh. #160
        chkExportSubroutines.setText("Involve called subroutines");
        chkExportSubroutines.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		subroutinesCheckBoxActionPerformed(evt);
        	}
        });
        // END KGU#178 2016-07-20
        
        // START KGU#363 2017-05-11: Enh. #372
        chkExportLicenseInfo.setText("Export author and license attributes");
        chkExportLicenseInfo.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		licenseInfoCheckBoxActionPerformed(evt);
        	}
        });
        // END KGU#363 2017-05-11
        
        jButton1.setText("OK");
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        // START KGU#351 217-02-26: Enh. #346
		//======== contentPanel0 ========
        //org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        //getContentPane().setLayout(layout);
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(contentPanel0);
        contentPanel0.setLayout(layout);
        // END KGU#351 2017-02-26
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    // START KGU#168 2016-04-04: Enh. #149
                    .add(layout.createSequentialGroup()
                    		.add(lbVoid1)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            		.add(lbCharset)
                            		.add(lbPrefGenerator))
                    		.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            		.add(cbCharset)
                            		.add(cbPrefGenerator))
                    		.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            		.add(chkCharsetAll)
                            		.add(lbVoid))
                    		.addContainerGap()
                    		)
                    // END KGU#168 2016-04-04
                    // START KGU#162 2016-03-31: Enh. #144
                    .add(noConversionCheckBox)
                    // END KGU#162 2016-03-31
                    .add(commentsCheckBox)
                    .add(bracesCheckBox)
                    .add(lineNumbersCheckBox)
                    // START KGU#178 2016-07-20: Enh. #160
                    .add(chkExportSubroutines)
                    // END KGU#178 2016-07-20
                    // START KGU#363 2017-05-11: Enh. #372
                    .add(chkExportLicenseInfo)
                	// END KGU#363 2017-05-11: Enh. #372
                    /*.add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jButton1)
                        .addContainerGap())*/))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(22, 22, 22)
                // START KGU#168/KGU#171 2016-04-04: Enh. #149 choice of character set
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                		.add(lbVoid1)
                		.add(layout.createSequentialGroup()
                				.add(lbCharset)
                				.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                				.add(lbPrefGenerator))
                		.add(layout.createSequentialGroup()
                				.add(cbCharset)
                				.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                				.add(cbPrefGenerator))
                		.add(layout.createSequentialGroup()
                				.add(chkCharsetAll)
                				.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                				.add(lbVoid))
                		)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                // END KGU#168/KGU#171 2016-04-04
                // START KGU#162 2016-03-31: Enh. #144
                .add(noConversionCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                // END KGU#162 2016-03-31
                .add(commentsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bracesCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lineNumbersCheckBox)
                // START KGU#178 2016-07-20: Enh. #160
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chkExportSubroutines)
                // END KGU#178 2016-07-20
                // START KGU#363 2017-05-11: Enh. #372
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chkExportLicenseInfo)
            	// END KGU#363 2017-05-11: Enh. #372
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                /*.add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)*/)
        );
        
        // START KGU#351 2017-02-26: Enh. #346
		//======== contentPanel1 ========
		{
			// get generator Names
			Vector<String> generatorNames = this.getCodeGeneratorNames();
			int nGenerators = generatorNames.size();
			
			this.targetLabels = new JLabel[nGenerators];
			this.includeLists = new JTextField[nGenerators];
	        GridBagLayout gbl = new GridBagLayout();
	        GridBagConstraints gbc0 = new GridBagConstraints();
	        GridBagConstraints gbc1 = new GridBagConstraints();
			contentPanel1.setLayout(gbl);
			gbc0.gridx = 1;
			gbc0.gridy = 1;
			gbc0.gridwidth = 1;
			gbc0.gridheight = 1;
			gbc0.fill = GridBagConstraints.BOTH;
			gbc1.gridx = 2;
			gbc1.gridy = 1;
			gbc1.gridwidth = GridBagConstraints.REMAINDER;
			gbc1.gridheight = 1;
			gbc1.fill = GridBagConstraints.BOTH;

			for (int i = 0; i < nGenerators; i++)
			{
				this.targetLabels[i] = new JLabel(generatorNames.get(i));
				contentPanel1.add(targetLabels[i], gbc0);
				this.includeLists[i] = new JTextField(20);
				//this.includeLists[i].setPreferredSize(new Dimension(100, 20));
				this.includeLists[i].setToolTipText("Fill in a comma-separated list of files or modules for which include/import/use clauses are to be inserted");
				contentPanel1.add(includeLists[i], gbc1);
				gbc0.gridy++; gbc1.gridy++;
			}
			
		}

		//======== buttonBar ========
		{
			buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
			buttonBar.setLayout(new GridBagLayout());
			((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 80};
			((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

			//---- btnOK ----
			buttonBar.add(jButton1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.addTab("General", contentPanel0);
        tabbedPane.addTab("Includes", contentPanel1);
        contentPane.add(buttonBar, BorderLayout.SOUTH);
        // END KGU#351 2017-02-26
        
        // START KGU#393 2017-05-09: Issue #400 - GUI consistency - let Esc and ctrl/shift-Enter work
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
					goOn = true;
					setVisible(false);
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		jButton1.addKeyListener(keyListener);
		cbCharset.addKeyListener(keyListener);
		chkCharsetAll.addKeyListener(keyListener);
		noConversionCheckBox.addKeyListener(keyListener);
		commentsCheckBox.addKeyListener(keyListener);
		bracesCheckBox.addKeyListener(keyListener);
		lineNumbersCheckBox.addKeyListener(keyListener);
		chkExportSubroutines.addKeyListener(keyListener);
		for (int i = 0; i < this.includeLists.length; i++) {
			this.includeLists[i].addKeyListener(keyListener);
		}
		tabbedPane.addKeyListener(keyListener);
		// END KGU#393 2017-05-09		

        // START KGU#287 2017-01-09: Issues #81/#330 GUI scaling
        GUIScaler.rescaleComponents(this);
        // END KGU#287 2017-01-09
        
        pack();
    }// </editor-fold>//GEN-END:initComponents

//    private void commentsCheckBoxActionPerformed(ActionEvent evt)//GEN-FIRST:event_commentsCheckBoxActionPerformed
//    {//GEN-HEADEREND:event_commentsCheckBoxActionPerformed
//        // TODO add your handling code here:
//    }//GEN-LAST:event_commentsCheckBoxActionPerformed

    protected void licenseInfoCheckBoxActionPerformed(ActionEvent evt) {
		// TODO Auto-generated method stub
		
	}

	private void jButton1ActionPerformed(ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        goOn = true;
        this.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void bracesCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_bracesCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bracesCheckBoxActionPerformed

    private void lineNumbersCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_lineNumbersCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lineNumbersCheckBoxActionPerformed
    
    // START KGU#171 2016-04-01: Enh. #144
    private void preferredGeneratorChanged(ItemEvent evt) {
    	// TODO inform the Menu? No need, value will be retrieved by Diagram
    }
    // END KGU#171 2016-04-01
    
    // START KGU#178 2016-07-20: Enh. #160
    private void subroutinesCheckBoxActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:    	
    }
    // END KGU#178 2016-07-20
    
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
    
    private Vector<String> getCodeGeneratorNames()
    {
		// read generators from file
		// and add them to the Vector
    	Vector<String> generatorNames = new Vector<String>();
    	// START KGU#351 2017-02-26: Enh. #346 - include/uses configuration
    	this.generatorKeys = new Vector<String>();
    	// END KGU#351 2017-02-26
		BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream("generators.xml"));
		GENParser genp = new GENParser();
		Vector<GENPlugin> plugins = genp.parse(buff);
		for(int i=0;i<plugins.size();i++)
		{
			GENPlugin plugin = (GENPlugin) plugins.get(i);
			generatorNames.add(plugin.title);
			String genKey = plugin.className;
			genKey = genKey.substring(genKey.lastIndexOf(".")+1);
			generatorKeys.add(genKey);
		}
		return generatorNames;
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
                new ExportOptionDialoge().setVisible(true);
            }
        });
    }
    
    // START KGU#168 2016-04-04: Issue #149
    public static String[] standardCharsets = {"ISO-8859-1", "UTF-8", "UTF-16", "windows-1250", "windows-1252", "US-ASCII"};
    // END KGU#168 2016-04-04
    
	// START KGU#351 2017-02-26: Enh. #346
	public javax.swing.JTabbedPane tabbedPane;
	public javax.swing.JPanel contentPanel0;
	public javax.swing.JPanel contentPanel1;
	protected JLabel[] targetLabels;
	protected JTextField[] includeLists = new JTextField[1];
	public javax.swing.JPanel buttonBar;
	public Vector<String> generatorKeys;
	// END KGU#351 2017-02-26
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox bracesCheckBox;
    public javax.swing.JCheckBox commentsCheckBox;
    public javax.swing.JButton jButton1;
    public javax.swing.JLabel jLabel1;
    //private javax.swing.JPanel jPanel1;
    public javax.swing.JCheckBox lineNumbersCheckBox;
    // START KGU#162 2016-03-31: Enh. #144 - new option to suppress all content transformation
    public javax.swing.JCheckBox noConversionCheckBox;
    // END KGU#162 2016-03-31
    // START KGU#171 2016-04-01: Enh. #144 - new: preferred code export language
    public javax.swing.JLabel lbVoid;
    public javax.swing.JLabel lbPrefGenerator;
    public javax.swing.JComboBox<String> cbPrefGenerator;
    // END KGU#171 2016-04-01
    // START KGU#168 2016-04-04: Issue #149
    public javax.swing.JLabel lbVoid1;
    public javax.swing.JLabel lbCharset;
    public javax.swing.JComboBox<String> cbCharset;
    public javax.swing.JCheckBox chkCharsetAll;
    // END KGU#168 2016-04-04
    // START KGU#178 2016-07-20: Enh. #160
    public javax.swing.JCheckBox chkExportSubroutines;
    // END KGU#178 2016-07-20
    // START KGU#363 2017-05-11: Enh. #372
    public javax.swing.JCheckBox chkExportLicenseInfo;
    // END KGU#363 2017-05-11
    // End of variables declaration//GEN-END:variables
}

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
 *      Description:    This dialog allows to control certain settings for the file import.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date        Description
 *      ------          ----        -----------
 *      Kay Gürtzig     2016-09-25  First Issue
 *      Kay Gürtzig     2016-11-11  Issue #81: DPI-awareness workaround for checkboxes
 *      Kay Gürtzig     2017-01-07  Bugfix #330 (issue #81): checkbox scaling suppressed for "Nimbus" l&f
 *      Kay Gürtzig     2017-01-09  Bugfix #330 (issue #81): scaling stuff outsourced to class GUIScaler
 *      Kay Gürtzig     2017-03-06  Enh. #368: New code option to import variable declarations
 *      Kay Gürtzig     2017-04-27  Enh. #354: New option logDir, all layouts fundamentally revised
 *      Kay Gürtzig     2017-05-09  Issue #400: keyListener at all controls 
 *      Kay Gürtzig     2017-06-20  Enh. #354/#357: generator-specific option mechanism implemented
 *      Kay Gürtzig     2018-07-13  Issue #557: New limitation option for the number of imported roots to be displayed
 *      Kay Gürtzig     2018-10-26  Enh. #419: New line length limitation option
 *      Kay Gürtzig     2019-03-29  Issue #557, #718: Limit for the max. number of roots could be enlarged
 *      Kay Gürtzig     2020-03-09  Issue #833: New option for the insertion of optional keywords (e.g. "preAlt")
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.parsers.GENParser;

/**
 * This Dialog allows to control certain settings for the file import.
 * @author kay
 */
@SuppressWarnings("serial")
public class ImportOptionDialog extends LangDialog {
	
	// START KGU#701 2019-03-29: Issues #557, #718
	private static final int MAX_DIAGRAMS = 300;	// was 150 before, no longer needed so strict
	// END KGU#701 2019-03-29

    public boolean goOn = false;

    /** Creates new form ExportOptionDialogue */
    public ImportOptionDialog()
    {
        initComponents();
        setModal(true);
    }

    // START KGU#416 2017-06-20: Enh. #354, #357: Signature changed
    //public ImportOptionDialog(Frame frame)
    //{
    public ImportOptionDialog(Frame _frame, Vector<GENPlugin> _plugins)
    {
    	plugins = _plugins;
    // END KGU#416 2017-06-20
        initComponents();
        setModal(true);
        setLocationRelativeTo(_frame);
    }
    // END KGU416 2017-06-20

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    //@SuppressWarnings("unchecked")
    private void initComponents() {

        pnlTop = new javax.swing.JPanel();
        pnlButtons = new javax.swing.JPanel();
        pnlOptions = new javax.swing.JPanel();
        pnlCode = new javax.swing.JPanel();
        pnlNSD = new javax.swing.JPanel();
        pnlPreference = new javax.swing.JPanel();
        // START KGU#553 2018-07-13: Issue #557 - new option to limit the number of displayed Roots
        javax.swing.JPanel pnlLimit = new javax.swing.JPanel();
        lblLimit = new javax.swing.JLabel();
        spnLimit = new javax.swing.JSpinner();
        // END KGU#553 2018-07-13
        // START KGU#602 2018-10-25: Issue #416 - new option to limit the line length
        lblMaxLen = new javax.swing.JLabel();
        spnMaxLen = new javax.swing.JSpinner();
        // END KGU#602 2018-10-25
        chkRefactorOnLoading = new javax.swing.JCheckBox();
        //chkOfferRefactoringIni = new javax.swing.JCheckBox();
        lbIntro = new javax.swing.JLabel();
        btnOk = new javax.swing.JButton();
        lbCharset = new javax.swing.JLabel();
        cbCharset = new javax.swing.JComboBox<String>();
        chkCharsetAll = new javax.swing.JCheckBox();
        // START KGU#354 2017-04-27: Enh. #354 Specify a log directory
        chkLogDir = new javax.swing.JCheckBox();
        txtLogDir = new javax.swing.JTextField(20);
        btnLogDir = new javax.swing.JButton("<<");
        // END KGU#354 2017-04-27
        // START KGU#358 2017-03-06: Enh. #368
        chkVarDeclarations = new javax.swing.JCheckBox();
        // END KGU#358 2017-03-06
        // START KGU#407 2017-06-22: Enh. #420 - new option to import statement comments
        chkCommentImport = new javax.swing.JCheckBox();
        // END KGU#407 2017-06-22
        // START KGU#821 2020-03-09: Issue #833 - new option to control the insertion of optional keywords
        chkInsertOptKeywords = new javax.swing.JCheckBox();
        // END KGU#821 2020-03-09
        // START KGU#354 2017-03-08: Enh. #354 - new option to save the parse tree
        chkSaveParseTree = new javax.swing.JCheckBox();
        // END KGU#354 2017-03-08
        // START KGU#416 2017-06-20: Enh. #354,#357
        btnPluginOptions = new javax.swing.JButton();
        cbOptionPlugins = new javax.swing.JComboBox<String>(this.getCodeParserNames(true));
        // END KGU#416 2017-06-20

        setTitle("Import options ...");

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
        
        // START KGU#354 2017-04-27: Enh. #354 Specify a log directory
        chkLogDir.setText("Log to folder");
        chkLogDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                doLogButtons();
            }
        });
        btnLogDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jlogDirButtonActionPerformed(evt);
            }
        });
        // END KGU#354 2017-04-27

        // START KGU#358 2017-03-06: Enh. #368
        chkVarDeclarations.setText("Import variable declarations");
        chkVarDeclarations.setToolTipText("With this option enabled, parser will make instruction elements from variable declarations.");
        // END KGU#358 2017-03-06
        // START KGU#407 2017-06-22: Enh. #420
        chkCommentImport.setText("Import source code comments");
        chkCommentImport.setToolTipText("With this option enabled, parser may equip derived elements with comments found closest in the source code.");
        // END KGU#407 2017-06-22
        // START KGU#821 2020-03-09: Issue #833
        chkInsertOptKeywords.setText("Place configured optional keywords around conditions");
        chkInsertOptKeywords.setToolTipText("Allows to decorate imported conditions (e.g. of alternatives) with the redundant pre/post keywords from the parser preferences.");
        // END KGU#407 2017-06-22
        // START KGU#354 2017-03-08: Enh. #354 - new option to save the parse tree
        chkSaveParseTree.setText("Write parse tree to file after import");
        chkSaveParseTree.setToolTipText("After a successful import you may obtain the syntax tree saved to a text file \"*.parsetree.txt\".");
        // END KGU#354 2017-03-08

        // START KGU#553 2018-07-13: Issue #557 - new option to limit the number of displayed Roots
        lblLimit.setText("Maximum number of imported diagrams for direct display:");
        lblLimit.setBorder(new EmptyBorder(0, 0, 0, 5));
        // START KGU#701 2019-03-29: Issues #557, #718
        //SpinnerModel spnModel = new SpinnerNumberModel(20, 5, 150, 5);
        SpinnerModel spnModel = new SpinnerNumberModel(50, 5, MAX_DIAGRAMS, 5);
        // END KGU#701 2019-03-29
        spnLimit.setModel(spnModel);
//        pnlLimit.setBorder(new EmptyBorder(3, 3, 5, 3));
//        pnlLimit.setLayout(new javax.swing.BoxLayout(pnlLimit, javax.swing.BoxLayout.X_AXIS));
//        pnlLimit.add(lblLimit);
//        pnlLimit.add(spnLimit);
        
        // END KGU#553 2018-07-13

        // START KGU#602 2018-10-25: Issue #416 - new option to limit the line length
        lblMaxLen.setText("Maximum line length (for word wrapping):");
        lblMaxLen.setBorder(new EmptyBorder(0, 0, 0, 5));
        SpinnerModel spnModelLen = new SpinnerNumberModel(0, 0, 255, 5);
        spnMaxLen.setModel(spnModelLen);

        GridBagConstraints gbcLimits = new GridBagConstraints();
        gbcLimits.insets = new Insets(0, 5, 5, 5);
        gbcLimits.weightx = 1.0;
        gbcLimits.anchor = GridBagConstraints.LINE_START;

        pnlLimit.setLayout(new GridBagLayout());
        gbcLimits.gridx = 0; gbcLimits.gridy = 0;
        pnlLimit.add(lblMaxLen, gbcLimits);
        gbcLimits.gridx++;
        gbcLimits.gridwidth = GridBagConstraints.REMAINDER;
        gbcLimits.fill = GridBagConstraints.HORIZONTAL;
        pnlLimit.add(spnMaxLen, gbcLimits);

        gbcLimits.gridwidth = 1;
        gbcLimits.gridx = 0; gbcLimits.gridy++;        
        gbcLimits.fill = GridBagConstraints.NONE;
        pnlLimit.add(lblLimit, gbcLimits);
        gbcLimits.gridx++;
        gbcLimits.gridwidth = GridBagConstraints.REMAINDER;
        gbcLimits.fill = GridBagConstraints.HORIZONTAL;
        pnlLimit.add(spnLimit, gbcLimits);
        // END KGU#602 2018-10-25

        // START KGU#416 2017-06-20: Enh. #354,#357
        btnPluginOptions.setText("Language-specific Options");
        btnPluginOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String pluginTitle = (String) cbOptionPlugins.getSelectedItem();
                // Identify the plugin by its title
                int pluginIndex = -1;
                for (int i = 0; pluginIndex < 0 && i < plugins.size(); i++) {
                    GENPlugin plugin = plugins.get(i);
                    if (pluginTitle.equals(plugin.title)) {
                        pluginIndex = i;
                    }
                }
                // If found then we can open the dialog
                if (pluginIndex >= 0) {
                    openSpecificOptionDialog(
                            msgOptionsForPlugin.getText(),
                            plugins.get(pluginIndex),
                            parserOptions.get(pluginIndex));
                }
            }});
        cbOptionPlugins.setMaximumSize(
                new Dimension(cbOptionPlugins.getMaximumSize().width, cbOptionPlugins.getPreferredSize().height));
        if (cbOptionPlugins.getItemCount() == 0) {
            btnPluginOptions.setVisible(false);
            cbOptionPlugins.setVisible(false);
        }
        // END KGU#416 2017-06-20

        chkRefactorOnLoading.setText("Replace keywords on loading a diagram (refactoring).");
        chkRefactorOnLoading.setToolTipText("Select this option if all configurable keywords in the daiagram are to be adapted to the current parser preferences.");
        chkRefactorOnLoading.setAlignmentX(LEFT_ALIGNMENT);

        lbIntro.setText("Please select the options you want to activate ...");

        btnOk.setText("OK");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        
        org.jdesktop.layout.GroupLayout pnlTopLayout = new org.jdesktop.layout.GroupLayout(pnlTop);
        pnlTop.setLayout(pnlTopLayout);
        pnlTopLayout.setHorizontalGroup(
                pnlTopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnlTopLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(pnlTopLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(lbIntro)
                                .addContainerGap()
                                )
                        .add(pnlOptions)
                        )
                );
        pnlTopLayout.setVerticalGroup(
                pnlTopLayout.createSequentialGroup()
                .addContainerGap()
                .add(lbIntro)
                .add(pnlOptions)
                );

        pnlCode.setBorder(new TitledBorder("Code Files"));
        org.jdesktop.layout.GroupLayout pnlCodeLayout = new org.jdesktop.layout.GroupLayout(pnlCode);
        pnlCode.setLayout(pnlCodeLayout);
        pnlCodeLayout.setHorizontalGroup(
                pnlCodeLayout.createParallelGroup()
                .add(pnlCodeLayout.createSequentialGroup()
                        .add(pnlCodeLayout.createParallelGroup()
                                .add(pnlCodeLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .add(lbCharset)
                                        )
                                .add(chkLogDir)
                                // START KGU#416 2017-06-20: Enh. #354, #357
                                .add(btnPluginOptions)
                                // END KGU#416 2017-06-20
                                )
                        .add(pnlCodeLayout.createParallelGroup()
                                .add(pnlCodeLayout.createSequentialGroup()
                                        .add(cbCharset)
                                        .add(chkCharsetAll)
                                        )
                                .add(pnlCodeLayout.createSequentialGroup()
                                        .add(txtLogDir)
                                        .add(btnLogDir)
                                        )
                                // START KGU#416 2017-06-20: Enh. #354, #357
                                .add(cbOptionPlugins)
                                // END KGU#416 2017-06-20
                                )
                        )
                .add(chkVarDeclarations)
                .add(chkCommentImport)
                // START KGU#821 2020-03-09: Issue #833
                .add(chkInsertOptKeywords)
                // END KGU#821 2020-03-09
                .add(chkSaveParseTree)
                // START KGU#553 2018-07-13: Issue #557
                .add(pnlLimit)
                // END KGU#553 2018-07-13
                );
        pnlCodeLayout.setVerticalGroup(
                pnlCodeLayout.createSequentialGroup()
                .add(pnlCodeLayout.createParallelGroup()
                        .add(pnlCodeLayout.createSequentialGroup()
                                .add(lbCharset)
                                .addContainerGap()
                                .add(chkLogDir)
                                )
                        .add(pnlCodeLayout.createSequentialGroup()
                                .add(pnlCodeLayout.createParallelGroup()
                                        .add(cbCharset)
                                        .add(chkCharsetAll)
                                        )
                                .addContainerGap()
                                .add(pnlCodeLayout.createParallelGroup()
                                        .add(txtLogDir)
                                        .add(btnLogDir)
                                        )
                                )
                        )
                .add(chkVarDeclarations)
                .add(chkCommentImport)
                // START KGU#821 2020-03-09: Issue #833
                .add(chkInsertOptKeywords)
                // END KGU#821 2020-03-09
                .add(chkSaveParseTree)
                // START KGU#553 2018-07-13: Issue #557
                .add(pnlLimit)
                // END KGU#553 2018-07-13
                // START KGU#416 2017-06-20: Enh. #354, #357
                .add(pnlCodeLayout.createParallelGroup()
                        .add(btnPluginOptions)
                        .add(cbOptionPlugins)
                        )
                // END KGU#416 2017-06-20
                );

        pnlNSD.setBorder(new TitledBorder("NSD Files"));
//        pnlNSD.setLayout(new GridLayout(0, 1, 0, 1));
        org.jdesktop.layout.GroupLayout pnlNSDLayout = new org.jdesktop.layout.GroupLayout(pnlNSD);
        pnlNSD.setLayout(pnlNSDLayout);
        //pnlNSD.add(chkRefactorOnLoading);
        pnlNSDLayout.setHorizontalGroup(
                pnlNSDLayout.createParallelGroup()
                .add(chkRefactorOnLoading)
                );
        pnlNSDLayout.setVerticalGroup(
                pnlNSDLayout.createSequentialGroup()
                .add(chkRefactorOnLoading)
                );
        
        pnlPreference.setBorder(new TitledBorder("Preference Files"));
        pnlPreference.setLayout(new GridLayout(0, 1, 0, 1));
        //pnlPreference.add(chkOfferRefactoringIni);

        GridBagLayout gbOptions = new GridBagLayout();
        GridBagConstraints gbcOptions = new GridBagConstraints();
        gbcOptions.insets = new Insets(12, 12, 12, 12);
        pnlOptions.setLayout(gbOptions);
        gbcOptions.gridx = 1;
        gbcOptions.gridy = 1;
        gbcOptions.gridwidth = 1;
        gbcOptions.gridheight = 1;
        gbcOptions.fill = GridBagConstraints.BOTH;
        gbcOptions.weightx = 1;
        gbcOptions.weighty = 1;
        gbcOptions.anchor = GridBagConstraints.NORTH;
        gbOptions.setConstraints(pnlCode, gbcOptions);
        pnlOptions.add(pnlCode);
        gbcOptions.gridy = 2;
        gbOptions.setConstraints(pnlNSD, gbcOptions);
        pnlOptions.add(pnlNSD);
        //pnlOptions.add(pnlPreference);
         
        pnlButtons.setLayout(new BorderLayout());
        pnlButtons.setBorder(new EmptyBorder(12,12,12,12));
        pnlButtons.add(btnOk, BorderLayout.EAST);
        
        content.add(pnlTop, BorderLayout.NORTH);
        content.add(pnlButtons, BorderLayout.SOUTH);
        
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
        btnOk.addKeyListener(keyListener);
        cbCharset.addKeyListener(keyListener);
        chkCharsetAll.addKeyListener(keyListener);
        chkLogDir.addKeyListener(keyListener);
        chkSaveParseTree.addKeyListener(keyListener);
        chkVarDeclarations.addKeyListener(keyListener);
        chkCommentImport.addKeyListener(keyListener);
        // START KGU#821 2020-03-09: Issue #833
        chkInsertOptKeywords.addKeyListener(keyListener);
        // END KGU#821 2020-03-09
        chkRefactorOnLoading.addKeyListener(keyListener);
        // END KGU#393 2017-05-09		
        // START KGU#416 2017-06-20: Enh. #354. #357
        btnPluginOptions.addKeyListener(keyListener);
        cbOptionPlugins.addKeyListener(keyListener);
        // END KGU#416 2017-06-20
        // START KGU#602 2018-10-26: Issue #419
        ((JSpinner.DefaultEditor)spnLimit.getEditor()).getTextField().addKeyListener(keyListener);
        ((JSpinner.DefaultEditor)spnMaxLen.getEditor()).getTextField().addKeyListener(keyListener);
        // END KGU#602 2018-10-26

        // START KGU#354 2017-04-27
        doLogButtons();
        // END KGU#354 2017-04-27
        
        // START KGU#287 2017-01-09: Issues #81, #330
        GUIScaler.rescaleComponents(this);
        // END KGU#287 2017-01-09

        pack();
        
        
    }// </editor-fold>//GEN-END:initComponents

    // START KGU#416 2017-06-20: Enh. #354,#357
    protected void openSpecificOptionDialog(String TitleFormat, GENPlugin plugin, HashMap<String, String> optionValues) {
    	PluginOptionDialog pod = new PluginOptionDialog(plugin, optionValues);
    	pod.setTitle(TitleFormat.replace("%", plugin.title));
    	pod.setVisible(true);
    }
    // END KGU#416 2017-06-20

    private void jButton1ActionPerformed(ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        // START KGU#354 2017-04-27: Enh. #354 Specify a log directory
        String logPath = txtLogDir.getText().trim();
        if (chkLogDir.isSelected() && !logPath.isEmpty() && !logPath.equals(".")) {
            File logDir = new File(logPath);
            String errMsg = null;
            if (!logDir.isDirectory()) {
            	errMsg = this.msgDirDoesntExist.getText();
            }
            else {
                File test = new File(logDir, "###test###.txt");
                try {
                    test.createNewFile();
                    test.delete();
                }
                catch (IOException ex) {
                    errMsg = this.msgDirNotWritable.getText();
                }
            }
            if (errMsg != null) {
                JOptionPane.showMessageDialog(this,
                        errMsg.replace("%", logPath),
                        chkLogDir.getText(), JOptionPane.ERROR_MESSAGE);
                txtLogDir.requestFocusInWindow();
                return;
            }
        }
        // END KGU#354 2017-04-27
        goOn = true;
        this.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    // START KGU#354 2017-04-27: Enh. #354
    private void jlogDirButtonActionPerformed(ActionEvent evt)
    {
        String path = txtLogDir.getText();
        JFileChooser logDirChooser = new JFileChooser();
        logDirChooser.setDialogTitle(chkLogDir.getText());
        if (!path.isEmpty()) {
            logDirChooser.setCurrentDirectory(new File(path));
        }
        logDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int answer = logDirChooser.showOpenDialog(this);
        if (answer == JFileChooser.APPROVE_OPTION) {
            txtLogDir.setText(logDirChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    public void doLogButtons()
    {
        boolean isLogEnabled = chkLogDir.isSelected();
        txtLogDir.setEnabled(isLogEnabled);
        btnLogDir.setEnabled(isLogEnabled);    	
    }
    // END KGU#354 2017-04-27

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
     * Returns a vector of the titles of all available Code Parsers. The list may be
     * restricted to those that provide specific options by {@code withOptionsOnly}
     * @param withOptionsOnly - if true then only the titles of parsers with options will be returned
     * @return The parser titles (actually rather language names)
     */
    private Vector<String> getCodeParserNames(boolean withOptionsOnly)
    {
        if (this.plugins == null) {
            // read generators from file
            // and add them to the Vector
            BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream("parsers.xml"));
            GENParser genp = new GENParser();
            this.plugins = genp.parse(buff);
            try { buff.close();	} catch (IOException e) {}
        }
        Vector<String> parserTitles = new Vector<String>();
        for(int i = 0; i < plugins.size(); i++)
        {
            if (!withOptionsOnly || !plugins.get(i).options.isEmpty()) {
                parserTitles.add(plugins.get(i).title);
            }
        }
        return parserTitles;
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
//            java.util.logging.Logger.getLogger(ImportOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex)
//        {
//            java.util.logging.Logger.getLogger(ImportOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex)
//        {
//            java.util.logging.Logger.getLogger(ImportOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex)
//        {
//            java.util.logging.Logger.getLogger(ImportOptionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable()
//        {
//
//            public void run()
//            {
//                new ImportOptionDialog().setVisible(true);
//            }
//        });
//    }
    
    public static String[] standardCharsets = {"ISO-8859-1", "UTF-8", "UTF-16", "windows-1250", "windows-1252", "US-ASCII"};
    
    // Variables declaration
    private javax.swing.JPanel pnlTop;
    // START KGU#354 2017-04-27: Enh. #354 Specify a log directory
    // END KGU#354 2017-04-27
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlOptions;
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
    public final LangTextHolder msgDirDoesntExist = new LangTextHolder("The selected log directory % doesn't exist!"); 
    public final LangTextHolder msgDirNotWritable = new LangTextHolder("The selected log directory % is not writable!"); 
    // START KGU#358 2017-03-06: Enh. #368 - new option to import mere declarations
    public javax.swing.JCheckBox chkVarDeclarations;
    // END KGU#358 2017-03-06
    // START KGU#407 2017-06-22: Enh. #420 - new option to import statement comments
    public javax.swing.JCheckBox chkCommentImport;
    // END KGU#407 2017-06-22
    // START KGU#821 2020-03-09: Issue #833 - new option whether to insert optional keywords from the parser preferences
    public javax.swing.JCheckBox chkInsertOptKeywords;
    // END KGU#821 2020-03-09
    // START KGU#354 2017-03-08: Enh. #354 - new option to save the parse tree
    public javax.swing.JCheckBox chkSaveParseTree;
    // END KGU#354 2017-03-08
    // START KGU#354 2017-04-27: Enh. #354 Specify a log directory
    public javax.swing.JCheckBox chkLogDir;
    public javax.swing.JTextField txtLogDir;
    public javax.swing.JButton btnLogDir;
    // END KGU#354 2017-04-27
    // End of variables declaration
    // START KGU#416 2017-06-20: Enh. #354, #357
    public Vector<GENPlugin> plugins = null;
    // In order of plugins there is an option value map per parser plugin
    public Vector<HashMap<String, String>> parserOptions = new Vector<HashMap<String, String>>();
    public javax.swing.JButton btnPluginOptions;
    public javax.swing.JComboBox<String> cbOptionPlugins;
    public final LangTextHolder msgOptionsForPlugin = new LangTextHolder("Options for % Parser");
    // END KGU#416 2017-06-20
    // START KGU#553 2018-07-12: Issue #557
    public javax.swing.JLabel lblLimit;
    public javax.swing.JSpinner spnLimit;
    // END KGU#553 2018-07-12
    // START KGU#602 2018-10-25: Enh. #419
    public javax.swing.JLabel lblMaxLen;
    public javax.swing.JSpinner spnMaxLen;    
    // END KGU#602 2018-10-25
}

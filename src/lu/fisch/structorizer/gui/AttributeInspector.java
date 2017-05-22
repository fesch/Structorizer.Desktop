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
 *      Description:    Dialog class to present and edit the Root attributes
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.04.28      Created
 *      Kay Gürtzig     2017.05.20      First usable issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.RootAttributes;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.io.LicFilter;
import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 *
 */
@SuppressWarnings("serial")
public class AttributeInspector extends LangDialog implements WindowListener {

	private static final String POOL_SUFFIX = " (pool)";
	/**
	 * The concerned Root 
	 */
	protected Root root;
	private Frame frame = null; 
	
	private boolean committed = false;
	
	private JPanel pnDialogPane;
	private JPanel pnButtonBar;
	protected final JPanel pnStatistics = new JPanel();
	protected final JPanel pnCopyrights = new JPanel();
	protected final JPanel pnKeywordSet = new JPanel();
	protected final JPanel pnButtons = new JPanel();
	
	protected final JLabel lblRoot = new JLabel();
	protected final JLabel lblType = new JLabel();
	protected final JLabel lblCreatedBy = new JLabel("Author");
	protected final JLabel lblCreatedOn = new JLabel("Created on");
	protected final JLabel lblModifiedBy = new JLabel("Last changed by");
	protected final JLabel lblModifiedOn = new JLabel("Last changed on");
	
	protected final JLabel lblFilePath = new JLabel("File path");
	protected final JLabel lblShadowPath = new JLabel("Shadow file path");

	// START KGU#363 2017-05-22: Enh. #372
	protected final JLabel lblOrigin = new JLabel("Origin"); 
	// END KGU#363 2017-05-22

	// Statistics / metrics
	protected final JLabel lblElements = new JLabel("Elements total");
	//protected final JLabel lblPaths = new JLabel();
	protected final JLabel lblInstrs = new JLabel("Instructions");
	protected final JLabel lblAlts = new JLabel("Alternatives");
	protected final JLabel lblCases = new JLabel("Case selections");
	protected final JLabel lblLoops = new JLabel("Loops");
	protected final JLabel lblCalls = new JLabel("Calls");
	protected final JLabel lblJumps = new JLabel("Jumps");
	protected final JLabel lblPars = new JLabel("Parallel sections");
	protected final JLabel lblNoOfElements = new JLabel();
	//protected final JLabel lblNoOfPaths = new JLabel();
	protected final JLabel lblNoOfInstrs = new JLabel();
	protected final JLabel lblNoOfAlts = new JLabel();
	protected final JLabel lblNoOfCases = new JLabel();
	protected final JLabel lblNoOfLoops = new JLabel();
	protected final JLabel lblNoOfCalls = new JLabel();
	protected final JLabel lblNoOfJumps = new JLabel();
	protected final JLabel lblNoOfPars = new JLabel();
	
	protected final JLabel lblPrefsAlt = new JLabel("IF statement");
	protected final JLabel lblPrefsCase = new JLabel("CASE statement");
	protected final JLabel lblPrefsFor = new JLabel("FOR loop");
	protected final JLabel lblPrefsForIn = new JLabel("FOR-IN loop");
	protected final JLabel lblPrefsWhile = new JLabel("WHILE loop");
	protected final JLabel lblPrefsRepeat = new JLabel("REPEAT loop");
	protected final JLabel lblPrefsIO = new JLabel("Input/Output");
	protected final JLabel lblPrefsJump = new JLabel("Jump (Exit)");
	protected final JLabel lblPrefsPre = new JLabel("Pre");
	protected final JLabel lblPrefsPost = new JLabel("Post");
	protected final JLabel lblPrefsMore = new JLabel("(More)");
	protected final JLabel lblPrefsLeave = new JLabel("...loop");
	protected final JLabel lblPrefsReturn = new JLabel("...routine");
	protected final JLabel lblPrefsExit = new JLabel("...program");
	
	
	protected final JTextField txtCreatedBy = new JTextField(20);
	protected final JTextField txtCreatedOn = new JTextField(20);
	protected final JTextField txtModifiedBy = new JTextField(20);
	protected final JTextField txtModifiedOn = new JTextField(20);

	protected final JTextField txtFilePath = new JTextField(20);
	protected final JTextField txtShadowPath = new JTextField(20);

	// START KGU#363 2017-05-22: Enh. #372
	protected final JTextField txtOrigin = new JTextField(15); 
	// END KGU#363 2017-05-22

	protected final JTextField txtAltPre = new JTextField(10);
	protected final JTextField txtAltPost = new JTextField(10);
	protected final JTextField txtCasePre = new JTextField(10);
	protected final JTextField txtCasePost = new JTextField(10);
	protected final JTextField txtForPre = new JTextField(10);
	protected final JTextField txtForPost = new JTextField(10);
	protected final JTextField txtForStep = new JTextField(10);
	protected final JTextField txtForInPre = new JTextField(10);
	protected final JTextField txtForInPost = new JTextField(10);
	protected final JTextField txtWhilePre = new JTextField(10);
	protected final JTextField txtWhilePost = new JTextField(10);
	protected final JTextField txtRepeatPre = new JTextField(10);
	protected final JTextField txtRepeatPost = new JTextField(10);
	protected final JTextField txtInput = new JTextField(10);
	protected final JTextField txtOutput = new JTextField(10);
	protected final JTextField txtJumpLeave = new JTextField(10);
	protected final JTextField txtJumpReturn = new JTextField(10);
	protected final JTextField txtJumpExit = new JTextField(10);
	protected final JTextField txtCallImport = new JTextField(10);

	// START KGU#363 2017-05-22: Enh. #372
	protected final JButton btnClearOrigin = new JButton("Clear"); 
	// END KGU#363 2017-05-22

	protected final JButton btnShowLicense = new JButton("License text");
	protected final JComboBox<String> cbLicense = new JComboBox<String>();
	
	protected final JButton btnParserPrefs = new JButton("Compare parser keys");
	protected final JButton btnOk = new JButton("OK");
	protected final JButton btnCancel = new JButton("Cancel");

	protected JComboBox<String> cbLicenseName;
	protected static final LangTextHolder msgOverrideLicense = new LangTextHolder("Override license text by the selected default license \"%\"?");
	protected static final LangTextHolder msgLicenseChange = new LangTextHolder("License Modification");
	protected static final LangTextHolder msgLicenseLoadError = new LangTextHolder("Error on loading license text for license \"%1\":\n%2!");
	public RootAttributes licenseInfo;
	
	public static final LangTextHolder lblMain = new LangTextHolder("Main program");
	public static final LangTextHolder lblSub = new LangTextHolder("Subroutine");
	public static final LangTextHolder lblIncl = new LangTextHolder("Includable");
	public static final LangTextHolder msgEnterLicenseName = new LangTextHolder("You must give your modified license a name now:\n(Otherwise it will be cleared!)");
	
	private HashMap<String, JTextField> keyFieldMap = new HashMap<String, JTextField>();
	private boolean rootLicTextChanged = false;
	
	/**
	 * @param owner
	 * @param _licenseInfo
	 */
	public AttributeInspector(Frame _owner, RootAttributes _licenseInfo) {
		super(_owner);
		frame = _owner;
		setModal(true);
		root = _licenseInfo.root;
		licenseInfo = _licenseInfo;
		initComponents();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param _licenseInfo
	 */
	public AttributeInspector(RootAttributes _licenseInfo) {
		super();
		setModal(true);
		root = _licenseInfo.root;
		licenseInfo = _licenseInfo;
		initComponents();
	}

	private void initComponents()
	{
		pnDialogPane = new JPanel();
		pnButtonBar = new JPanel();
		
		setTitle("Diagram Attributes");
		
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent evt) 
			{
				if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				else if (evt.getKeyCode() == KeyEvent.VK_ENTER && (evt.isShiftDown() || evt.isControlDown()))
				{
					commitChanges();
					setVisible(false);
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};

        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

		keyFieldMap.put("preAlt", this.txtAltPre);
		keyFieldMap.put("postAlt", this.txtAltPost);
		keyFieldMap.put("preCase", this.txtCasePre);
		keyFieldMap.put("postCase", this.txtCasePost);
		keyFieldMap.put("preFor", this.txtForPre);
		keyFieldMap.put("postFor", this.txtForPost);
		keyFieldMap.put("stepFor", this.txtForStep);
		keyFieldMap.put("preForIn", this.txtForInPre);
		keyFieldMap.put("postForIn", this.txtForInPost);
		keyFieldMap.put("preWhile", this.txtWhilePre);
		keyFieldMap.put("postWhile", this.txtWhilePost);
		keyFieldMap.put("preRepeat", this.txtRepeatPre);
		keyFieldMap.put("postRepeat", this.txtRepeatPost);
		keyFieldMap.put("preLeave", this.txtJumpLeave);
		keyFieldMap.put("preReturn", this.txtJumpReturn);
		keyFieldMap.put("preExit", this.txtJumpExit);
		keyFieldMap.put("input", this.txtInput);
		keyFieldMap.put("output", this.txtOutput);

        lblRoot.setText(root.getSignatureString(false));
        txtFilePath.setText(root.filename == null ? "" : root.filename);
        txtFilePath.setEditable(false);
        txtFilePath.addKeyListener(keyListener);
        txtShadowPath.setText(root.shadowFilepath == null ? "" : root.shadowFilepath);
        txtShadowPath.setEditable(false);
        txtShadowPath.addKeyListener(keyListener);
        
        txtCreatedBy.setText(licenseInfo.authorName);
        txtCreatedBy.addKeyListener(keyListener);
        txtCreatedOn.setText(root.getCreatedString());
        txtCreatedOn.setEditable(false);
        txtCreatedOn.addKeyListener(keyListener);
        txtModifiedBy.setText(root.getModifiedBy());
        txtModifiedBy.setEditable(false);
        txtModifiedBy.addKeyListener(keyListener);
        txtModifiedOn.setText(root.getModifiedString());
        txtModifiedOn.setEditable(false);
        txtModifiedOn.addKeyListener(keyListener);
        
    	// START KGU#363 2017-05-22: Enh. #372
        txtOrigin.setText(licenseInfo.origin != null ? licenseInfo.origin.trim() : "");
        // It may contain a long file path, so make it a tooltip if it isn't empty
        txtOrigin.setToolTipText(licenseInfo.origin);
    	txtOrigin.setEditable(false);
        txtOrigin.addKeyListener(keyListener);
        btnClearOrigin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearOrgButtonActionPerformed(evt);
            }
        });
    	// END KGU#363 2017-05-22

        final JLabel[] statLabels = new JLabel[]{
                lblElements,
                lblInstrs,
                lblAlts,
                lblCases,
                lblLoops,
                lblCalls,
                lblJumps,
                lblPars
        };
        final JLabel[] statNoLabels = new JLabel[]{
                lblNoOfElements,
                lblNoOfInstrs,
                lblNoOfAlts,
                lblNoOfCases,
                lblNoOfLoops,
                lblNoOfCalls,
                lblNoOfJumps,
                lblNoOfPars
        };

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		GridBagLayout gblDialog = new GridBagLayout();
		GridBagConstraints gbcDialog = new GridBagConstraints();
		pnDialogPane.setLayout(gblDialog);
		pnDialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        gbcDialog.gridx = 1;
        gbcDialog.gridy = 1;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gbcDialog.insets = new Insets(0, 5, 5, 0);
        gblDialog.setConstraints(lblType, gbcDialog);
        pnDialogPane.add(lblType);
        
        gbcDialog.gridx = 2;
        gbcDialog.gridwidth = 2;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 2;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(lblRoot, gbcDialog);
        pnDialogPane.add(lblRoot);
        
        gbcDialog.gridy++;

        gbcDialog.gridx = 1;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(lblFilePath, gbcDialog);
        pnDialogPane.add(lblFilePath);
        
        gbcDialog.gridx = 2;
        gbcDialog.gridwidth = 3;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 3;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(txtFilePath, gbcDialog);
        pnDialogPane.add(txtFilePath);
        
        gbcDialog.gridy++;

        gbcDialog.gridx = 1;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(lblShadowPath, gbcDialog);
        pnDialogPane.add(lblShadowPath);
        
        gbcDialog.gridx = 2;
        gbcDialog.gridwidth = 3;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 3;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(txtShadowPath, gbcDialog);
        pnDialogPane.add(txtShadowPath);
        
        // START KGU#363 2017-05-22
        gbcDialog.gridy++;

        gbcDialog.gridx = 1;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(lblOrigin, gbcDialog);
        pnDialogPane.add(lblOrigin);
        
        gbcDialog.gridx = 2;
        gbcDialog.gridwidth = 2;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 2;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(txtOrigin, gbcDialog);
        pnDialogPane.add(txtOrigin);
        
        gbcDialog.gridx = 4;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(btnClearOrigin, gbcDialog);
        pnDialogPane.add(btnClearOrigin);
        
        // END KGU#363 2017-05-22

        gbcDialog.gridy++;

        gbcDialog.gridx = 1;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(lblCreatedBy, gbcDialog);
        pnDialogPane.add(lblCreatedBy);
        
        gbcDialog.gridx = 2;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(txtCreatedBy, gbcDialog);
        pnDialogPane.add(txtCreatedBy);
        
        gbcDialog.gridx = 3;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(lblCreatedOn, gbcDialog);
        pnDialogPane.add(lblCreatedOn);
        
        gbcDialog.gridx = 4;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(txtCreatedOn, gbcDialog);
        pnDialogPane.add(txtCreatedOn);
        
        gbcDialog.gridy++;

        gbcDialog.gridx = 1;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(lblModifiedBy, gbcDialog);
        pnDialogPane.add(lblModifiedBy);
        
        gbcDialog.gridx = 2;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(txtModifiedBy, gbcDialog);
        pnDialogPane.add(txtModifiedBy);
        
        gbcDialog.gridx = 3;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(lblModifiedOn, gbcDialog);
        pnDialogPane.add(lblModifiedOn);
        
        gbcDialog.gridx = 4;
        gbcDialog.gridwidth = 1;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 1;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(txtModifiedOn, gbcDialog);
        pnDialogPane.add(txtModifiedOn);
        
        //================= COPYRIGHTS =================
        
        gbcDialog.gridy++;

        gbcDialog.gridx = 1;
        gbcDialog.gridwidth = 4;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 4;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gbcDialog.insets.left = 0;
        gbcDialog.insets.right = 0;
        gblDialog.setConstraints(pnCopyrights, gbcDialog);
        pnDialogPane.add(pnCopyrights);
        
        {
        	pnCopyrights.setBorder(new TitledBorder("Copyrights"));
        	pnCopyrights.setLayout(new GridLayout(0, 2, 5, 1));
        	
        	this.btnShowLicense.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    licButtonActionPerformed(evt);
                }
            });
    		cbLicenseName = new JComboBox<String>();
    		cbLicenseName.setToolTipText("Select an available license from the personal license pool or the current one to edit it.");
        	this.updateLicenseChoice();
        	    		
    		pnCopyrights.add(btnShowLicense);
    		pnCopyrights.add(cbLicenseName);
        }
        
        //================= STATISTICS =================
        
        gbcDialog.gridy++;

        gbcDialog.gridx = 1;
        gbcDialog.gridwidth = 4;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 4;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(pnStatistics, gbcDialog);
        pnDialogPane.add(pnStatistics);
        
        {
            final short N_COLS = 4;

            Integer[] statistics = root.getElementCounts();
            int nElements = 0;
            for (int i = 0; i < statistics.length; i++) {
            	nElements += statistics[i];
            }
            lblNoOfElements.setText(Integer.toString(nElements));
            for (int i = 0; i < statistics.length; i++) {
            	statNoLabels[i + 1].setText(statistics[i].toString());
            }

            pnStatistics.setBorder(new TitledBorder("Statistics"));
        	pnStatistics.setLayout(new GridLayout(0, N_COLS, 0, 1));

        	for (int i = 0; i < 2 * statLabels.length; i++) {

        		int ix = i % N_COLS + i / (2*N_COLS) * N_COLS;
        		JLabel lbl = ((i / N_COLS % 2 == 0) ? statLabels : statNoLabels)[ix];
        		pnStatistics.add(lbl);
        	}
        	
        	// TODO: Add some software complexity measures!
        }
        
        //================= KEYWORD SET =================
        
        gbcDialog.gridy++;

        gbcDialog.gridx = 1;
        gbcDialog.gridwidth = 4;
        gbcDialog.gridheight = 1;
        gbcDialog.fill = GridBagConstraints.BOTH;
        gbcDialog.weightx = 4;
        gbcDialog.weighty = 1;
        gbcDialog.anchor = GridBagConstraints.NORTH;
        gblDialog.setConstraints(pnKeywordSet, gbcDialog);
        pnDialogPane.add(pnKeywordSet);
        
        {
        	JComponent[] keyComponents = new JComponent[] {
        			this.btnParserPrefs, this.lblPrefsPre,	this.lblPrefsPost,	this.lblPrefsMore,
        			this.lblPrefsAlt,	this.txtAltPre,		this.txtAltPost,	null,
        			this.lblPrefsCase,	this.txtCasePre,	this.txtCasePost,	null,
        			this.lblPrefsFor,	this.txtForPre,		this.txtForPost,	this.txtForStep,
        			this.lblPrefsForIn,	this.txtForInPre,	this.txtForInPost,	null,
        			this.lblPrefsWhile,	this.txtWhilePre,	this.txtWhilePost,	null,
        			this.lblPrefsRepeat,this.txtRepeatPre,	this.txtRepeatPost,	null,
//        			this.lblPrefsJump, 	this.lblPrefsLeave,	this.lblPrefsReturn,	this.lblPrefsExit,
//        			null, 				this.txtJumpLeave,	this.txtJumpReturn,	this.txtJumpExit,
        			this.lblPrefsIO,	this.txtInput,		this.txtOutput,		null
        	};
        	
        	pnKeywordSet.setLayout(new GridLayout(0, 4, 5, 0));
        	pnKeywordSet.setBorder(new TitledBorder("Cached differing keyword set"));

        	this.btnParserPrefs.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    parserPrefsButtonActionPerformed(evt);
                }
            });
        	
        	for (JComponent comp: keyComponents) {
        		if (comp == null) {
        			pnKeywordSet.add(new JLabel(""));
        		}
        		else {
        			if (comp instanceof JTextField) {
        				comp.setEnabled(false);
        				//((JTextField)comp).setEditable(false);
        			}
        			pnKeywordSet.add(comp);
        		}
        	}
            this.updateKeywordDisplay();
        	
        }
        
        //================= BUTTON BAR =================

        pnButtonBar.setLayout(new BorderLayout());
        pnButtonBar.setBorder(new EmptyBorder(12,12,12,12));
        
        pnButtons.add(btnCancel);
        pnButtons.add(btnOk);
        
        pnButtonBar.add(pnButtons, BorderLayout.EAST);
        
        contentPane.add(pnDialogPane, BorderLayout.CENTER);
        contentPane.add(pnButtonBar, BorderLayout.SOUTH);
        
		// START KGU#287 2017-01-09: Issues #81, #330
		GUIScaler.rescaleComponents(this);
		// END KGU#287 2017-01-09
		
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// Bob-thinks
		// add the KEY-listeners
		btnOk.requestFocus(true);
		btnOk.addKeyListener(keyListener);
        
	}

	private void updateKeywordDisplay() {
		if (root.storedParserPrefs != null) {
			// The keys to retrieve the reserved words are defined in CodeParser.keywordMap.
			for (Entry<String, JTextField> entry: keyFieldMap.entrySet()) {
				StringList keyTokens = root.storedParserPrefs.get(entry.getKey());
				String text = "";
				if (keyTokens != null) { text = keyTokens.concatenate(); }
				entry.getValue().setText(text);
				if (!CodeParser.getKeywordOrDefault(entry.getKey(), "").equals(text)) {
					entry.getValue().setForeground(Color.RED);
					entry.getValue().setEditable(false);
					entry.getValue().setEnabled(true);
				}
			}
		}
	}

	protected void parserPrefsButtonActionPerformed(ActionEvent evt) {
		ParserPreferences parserPreferences = new ParserPreferences(frame);
		parserPreferences.setTitle(parserPreferences.getTitle() + " for comparison / to adopt");
		// set fields
		parserPreferences.edtAltPre.setText(CodeParser.getKeyword("preAlt"));
		parserPreferences.edtAltPost.setText(CodeParser.getKeyword("postAlt"));
		parserPreferences.edtCasePre.setText(CodeParser.getKeyword("preCase"));
		parserPreferences.edtCasePost.setText(CodeParser.getKeyword("postCase"));
		parserPreferences.edtForPre.setText(CodeParser.getKeyword("preFor"));
		parserPreferences.edtForPost.setText(CodeParser.getKeyword("postFor"));
		parserPreferences.edtForStep.setText(CodeParser.getKeyword("stepFor"));
		parserPreferences.edtForInPre.setText(CodeParser.getKeyword("preForIn"));
		parserPreferences.edtForInPost.setText(CodeParser.getKeyword("postForIn"));
		parserPreferences.edtWhilePre.setText(CodeParser.getKeyword("preWhile"));
		parserPreferences.edtWhilePost.setText(CodeParser.getKeyword("postWhile"));
		parserPreferences.edtRepeatPre.setText(CodeParser.getKeyword("preRepeat"));
		parserPreferences.edtRepeatPost.setText(CodeParser.getKeyword("postRepeat"));
		parserPreferences.edtJumpLeave.setText(CodeParser.getKeyword("preLeave"));
		parserPreferences.edtJumpReturn.setText(CodeParser.getKeyword("preReturn"));
		parserPreferences.edtJumpExit.setText(CodeParser.getKeyword("preExit"));
		parserPreferences.edtInput.setText(CodeParser.getKeyword("input"));
		parserPreferences.edtOutput.setText(CodeParser.getKeyword("output"));
		parserPreferences.chkIgnoreCase.setSelected(CodeParser.ignoreCase);

		parserPreferences.edtAltPre.setEnabled(false);
		parserPreferences.edtAltPost.setEnabled(false);
		parserPreferences.edtCasePre.setEnabled(false);
		parserPreferences.edtCasePost.setEnabled(false);
		parserPreferences.edtForPre.setEnabled(false);
		parserPreferences.edtForPost.setEnabled(false);
		parserPreferences.edtForStep.setEnabled(false);
		parserPreferences.edtForInPre.setEnabled(false);
		parserPreferences.edtForInPost.setEnabled(false);
		parserPreferences.edtWhilePre.setEnabled(false);
		parserPreferences.edtWhilePost.setEnabled(false);
		parserPreferences.edtRepeatPre.setEnabled(false);
		parserPreferences.edtRepeatPost.setEnabled(false);
		parserPreferences.edtJumpLeave.setEnabled(false);
		parserPreferences.edtJumpReturn.setEnabled(false);
		parserPreferences.edtJumpExit.setEnabled(false);
		parserPreferences.edtInput.setEnabled(false);
		parserPreferences.edtOutput.setEnabled(false);
		parserPreferences.chkIgnoreCase.setEnabled(false);

		parserPreferences.pack();
		parserPreferences.setVisible(true);

	}

	protected void clearOrgButtonActionPerformed(ActionEvent evt) {
		this.txtOrigin.setText("");
	}

	protected void licButtonActionPerformed(ActionEvent evt) {
    	File licFile = null;
    	String licName = (String)this.cbLicenseName.getSelectedItem();
    	if (licName != null && licName.trim().isEmpty()) {
    		licName = null;
    	}
    	if (licName != null && licName.endsWith(POOL_SUFFIX)) {
    		String fileName = LicFilter.getNamePrefix() + licName.substring(0, licName.lastIndexOf(POOL_SUFFIX)) +
    				"." + LicFilter.acceptedExtension();
    		File[] licFiles = Ini.getIniDirectory().listFiles(new LicFilter());
    		for (File file: licFiles) {
    			if (fileName.equals(file.getName())) {
    				licFile = file;
    				break;
    			}
    		}
			licName = null;
    	}
    	String oldText = this.licenseInfo.licenseText;
    	LicenseEditor licEditor = new LicenseEditor(this.frame, licFile, this.licenseInfo, licName);
		licEditor.addWindowListener(this);
    	licEditor.setVisible(true);
    	if (licFile == null && this.licenseInfo.licenseText != null
    			&& (oldText == null || !oldText.equals(this.licenseInfo.licenseText))) {
    		this.rootLicTextChanged = true;
    		if (licName == null) {
    			licName = JOptionPane.showInputDialog(this, msgEnterLicenseName.getText(),
    					this.getTitle(), JOptionPane.WARNING_MESSAGE);
    			if (licName != null && !licName.trim().isEmpty()) {
    				this.licenseInfo.licenseName = licName;
    				this.updateLicenseChoice();
    			}
    			else {
    	    		this.rootLicTextChanged = false;    				
    			}
    		}
    	}
	}

    private String getLicenseTextFromPool(String licenseName) {
		String content = "";
		String error = null;
		BufferedReader br = null;
		File licDir = Ini.getIniDirectory();
		String fileName = licDir.getAbsolutePath() + File.separator + LicFilter.getNamePrefix() +
				licenseName + "." + LicFilter.acceptedExtension();
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				content += line + '\n';
			};
		} catch (Exception ex) {
			error = ex.getMessage();
		}
		if (br != null) {
			
			try {
				br.close();
			} catch (IOException ex) {}
		}
		if (error != null) {
			JOptionPane.showMessageDialog(frame, msgLicenseLoadError.getText().
					replace("%1", licenseName).replace("%2", error),
					"Error " + fileName, JOptionPane.ERROR_MESSAGE);
		}
		return content;	
	}

	private void updateLicenseChoice()
    {
    	String oldSel = licenseInfo.licenseName;
    	int selIx = cbLicenseName.getSelectedIndex();
    	if (selIx >= 0) {
    		oldSel = cbLicenseName.getItemAt(selIx);
    	}
    	selIx = -1;
    	cbLicenseName.setSelectedIndex(selIx);
    	cbLicenseName.removeAllItems();
		File licDir = Ini.getIniDirectory();
		File[] licFiles = licDir.listFiles(new LicFilter());
		String prefix = LicFilter.getNamePrefix();
		String ext = LicFilter.acceptedExtension();
		boolean rootContained = false;
		for (File licFile: licFiles) {
			String fileName = licFile.getName();
			String licName = fileName.substring(prefix.length(), fileName.lastIndexOf("."+ext)) + POOL_SUFFIX;
			cbLicenseName.addItem(licName);
			if (licName.equals(oldSel)) {
				selIx = cbLicenseName.getItemCount() - 1;
			}
			if (licName.equals(licenseInfo.licenseName)) {
				rootContained = true;
			}
		}
		if (!rootContained) {
			String licName = this.licenseInfo.licenseName;
			if (licName == null) {
				licName = " ";
			}
			cbLicenseName.addItem(licName);
			cbLicenseName.setSelectedItem(licName);
		}
		if (selIx >= 0) {
			cbLicenseName.setSelectedIndex(selIx);
		}
    }

	protected void okButtonActionPerformed(ActionEvent evt) {
		if (evt.getSource() == this.btnOk) {
			commitChanges();
		}
		setVisible(false);
	}
	
	private void commitChanges()
	{
		String licName = (String)cbLicenseName.getSelectedItem();
		if (licName == null || licName.trim().isEmpty()) {
			// License was explicitly removed, so this is a change
			committed = licenseInfo.licenseName != null;
			licenseInfo.licenseName = null;
			licenseInfo.licenseText = null;
		}
		else if (licName.endsWith(POOL_SUFFIX) &&
			(licenseInfo.licenseText == null || licenseInfo.licenseText.trim().isEmpty() ||
					JOptionPane.showConfirmDialog(frame, 
							msgOverrideLicense.getText().replace("%", licName),
							msgLicenseChange.getText(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
			licName = licName.substring(0, licName.length()-POOL_SUFFIX.length());
			String licText = getLicenseTextFromPool(licName);
			// License was explicitly replaced if at least one of the data changed 
			committed = !licName.equals(licenseInfo.licenseName) || !licText.equals(licenseInfo.licenseText);
			licenseInfo.licenseText = licText;
			licenseInfo.licenseName = licName;
		}
		else {
			committed = this.rootLicTextChanged;
		}
		if (!this.txtCreatedBy.getText().equals(licenseInfo.authorName)) {
			licenseInfo.authorName = this.txtCreatedBy.getText();
			committed = true;
		}
		if (!this.txtOrigin.getText().equals(licenseInfo.origin)) {
			licenseInfo.origin = this.txtOrigin.getText();
			committed = true;
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent evt) {
//		if (evt.getSource() instanceof LicenseEditor) {
//			this.updateLicenseChoice();
//		}
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent evt) {
		Object source = evt.getSource();
		//System.out.println(source + " opened...");
		if (source instanceof LicenseEditor) {
			LicenseEditor led = (LicenseEditor)source;
			led.setSize(LicenseEditor.PREFERRED_WIDTH, LicenseEditor.PREFERRED_HEIGHT);
		}
		else if (source == this) {
			this.updateLicenseChoice();
		}
	}

	public boolean isCommitted() {
		return committed;
	}
	
	@Override
	public void setVisible(boolean b)
	{
		if (b) {
//			this.updateLicenseChoice();
//			this.updateKeywordDisplay();
			pnKeywordSet.setVisible(root.storedParserPrefs != null);
		}
		super.setVisible(b);
	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.locales.LangDialog#adjustLangDependentComponents()
	 */
	@Override
    protected void adjustLangDependentComponents()
    {
		String type = lblMain.getText();
        if (root.isSubroutine()) {
        	type = lblSub.getText();
        } else if (root.isInclude()) {
        	type = lblIncl.getText();
        }
        lblType.setText(type);
    }


}

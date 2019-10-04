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
 *      Description:    This dialog allows editing the extra properties of Root elements
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-03-13      First Issue (for Enh. requ. #372)
 *      Kay Gürtzig     2017-05-21      Attribute editing now delegated to new class AttributeInspector
 *      Kay Gürtzig     2017-06-30      Enh. #389: Text area for Include list added.
 *      Kay Gürtzig     2018-12-19      Issue #651: Include list editing now delegated to a JOptionPane
 *      Kay Gürtzig     2019-10-04      Bugfix #757: JTextArea size of include list. 
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      In addition to the usual fields (text and comment) this editor also allows access to attributes
 *      and the include list, in future variable and type management are likely to be added. 
 *
 ******************************************************************************************************///

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lu.fisch.structorizer.elements.RootAttributes;
import lu.fisch.structorizer.locales.LangEvent;
import lu.fisch.structorizer.locales.LangEventListener;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.utils.StringList;

/**
 * Enhanced and specialized element editor for diagram Roots
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class InputBoxRoot extends InputBox implements LangEventListener {
	
//	/**
//	 * Helper structure for the communication with classes {@code Diagram}
//	 * and {@code LicenseEditor}
//	 */
//	public class RootLicenseInfo {
//		public String rootName = null;
//		public String licenseName = null;
//		public String licenseText = null;
//	}
	
	// START KGU#363 2017-05-20: Issue #372
//	protected JLabel lblAuthorName;
//	protected JTextField txtAuthorName;
//	protected JButton btnLicenseText;
//	protected JComboBox<String> cbLicenseName;
//	protected static final LangTextHolder msgOverrideLicense = new LangTextHolder("Override license text by the selected default license \"%\"?");
//	protected static final LangTextHolder msgLicenseChange = new LangTextHolder("License Modification");
//	protected static final LangTextHolder msgLicenseLoadError = new LangTextHolder("Error on loading license text for license \"%1\":\n%2!");
	private AttributeInspector attrInspr;
	protected JButton btnAttributes;
	// END KGU#363 2017-05-20
	// START KGU#376 2017-06-30: Enh. #389 - Diagram import now directly from Root 
	// START KGU#620 2018-12-19: Issue #651 - Redesign of the include list editing
	//private JLabel lblIncludeList;
	//private JTextArea txtIncludeList;
    //protected JScrollPane scrIncludeList;
	protected JButton btnIncludeList;
	private static final LangTextHolder lblIncludeList = new LangTextHolder("Diagrams to be included");
	public JTextArea txtIncludeList;		// To be realised in a popup now
    public JScrollPane scrIncludeList;		// To be realised in a popup now
	// END KGU#620 2018-12-19
	// END KGU#376 2017-06-30
	
	public RootAttributes licenseInfo = new RootAttributes();
	private Frame frame;

	/**
	 * @param owner - the responsible frame of the application
	 * @param modal - whether this editor is to be made modal
	 */
	public InputBoxRoot(Frame owner, boolean modal) {
		super(owner, modal);
		this.frame = owner;
//		this.addWindowListener(this);
	}

	// START KGU#376 2017-06-30: Enh. #389
    /**
     * Subclassable method to add specific stuff to the Panel top
     * @param _panel the panel to be enhanced
     * @param pnPanel0c the layout constraints
     * @return number of lines (y cell units) inserted
     */
	protected int createPanelTop(JPanel _panel, GridBagLayout _gb, GridBagConstraints _gbc)
	{
		//double scaleFactor = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
		
		int lineNo = 1;

		// START KGU#620 2018-12-19: Issue #651 - put the include list editing to an additional popup window
//		int border = (int)(5 * scaleFactor);
//		_gbc.insets = new Insets(2*border, border, 0, border);
//		
//		lblIncludeList = new JLabel("Diagrams to be included");
//		txtIncludeList = new JTextArea();
//	    scrIncludeList = new JScrollPane(txtIncludeList);
//
//	    txtIncludeList.addKeyListener(this);
//	    // START KGU 2018-02-16: Make sure the includes area isn't mistaken for comments or signature text
//	    txtIncludeList.setBackground(new Color(255,255,210));
//	    // END KGU 2018-02-16
//		// Issue #163 - tab isn't really needed within the text
//		txtIncludeList.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
//		txtIncludeList.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
//
//		scalableComponents.addElement(txtIncludeList);
//        
//		_gbc.gridx = 1;
//		_gbc.gridy = lineNo;
//		_gbc.gridheight = 1;
//		_gbc.gridwidth = 18;
//		_gbc.fill = GridBagConstraints.BOTH;
//		_gbc.weightx = 1;
//		_gbc.weighty = 0;
//		_gbc.anchor = GridBagConstraints.NORTH;
//		_panel.add(lblIncludeList, _gbc);
//
//		_gbc.gridx = 1;
//		_gbc.gridy = ++lineNo;
//		_gbc.gridheight = 4;
//		_gbc.gridwidth = 18;
//		_gbc.fill = GridBagConstraints.BOTH;
//		_gbc.weightx = 1;
//		_gbc.weighty = 1;
//		_gbc.anchor = GridBagConstraints.NORTH;
//		_panel.add(scrIncludeList, _gbc);
//		int fontHeight = txtIncludeList.getFontMetrics(txtIncludeList.getFont().deriveFont(FONT_SIZE)).getHeight();
//		scrIncludeList.setPreferredSize(new Dimension(getPreferredSize().width, (int)Math.ceil(2 * fontHeight)));
		txtIncludeList = new JTextArea();
	    scrIncludeList = new JScrollPane(txtIncludeList);
		// END KGU#620 2018-12-19

		_gbc.gridx = 1;
		_gbc.gridy = (lineNo += _gbc.gridheight);
		_gbc.gridwidth = 18;
		_gbc.gridheight = 30;
		_gbc.fill = GridBagConstraints.BOTH;
		_gbc.weightx = 1;
		_gbc.weighty = 0;
		_gbc.anchor = GridBagConstraints.NORTH;
		_gb.setConstraints(lblText, _gbc);
		_panel.add(lblText);

		return lineNo + _gbc.gridheight;
	}
	// END KGU#376 2017-06-30
	
	
	/**
     * Adds additional controls to the left of the font button panel.
     * Returns the number of columns created.
     * @param _panel - the panel where the extra controls may be added
     * @param _gbc - a usable GridBagConstraints object 
     * @param _maxGridX - the gridX value InputBox will claim (we must stay left of it)
     * @return the next unused gridx value
     */
	@Override
    protected int createExtrasBottom(JPanel _panel, GridBagConstraints _gbc, int _maxGridX) {
		
		btnAttributes = new JButton("Attributes");
		btnAttributes.addActionListener(this);
		
		int border = _gbc.insets.left;

        _gbc.gridx = 1;
        _gbc.gridwidth = 1;
        _gbc.gridheight = 1;
        _gbc.fill = GridBagConstraints.BOTH;
        _gbc.weightx = 0;
        _gbc.weighty = 0;
        _gbc.anchor = GridBagConstraints.NORTH;
        
        ((GridBagLayout)_panel.getLayout()).setConstraints(btnAttributes, _gbc);
        _panel.add(btnAttributes);
      
        // START KGU#620 2018-12-19: Issue #651 - Include list now editable via a button
        btnIncludeList = new JButton(lblIncludeList.getText());
		btnIncludeList.addActionListener(this);
		
		lblIncludeList.addLangEventListener(this);

        _gbc.insets.left = 0;

        _gbc.gridx = 2;
        _gbc.gridwidth = 1;
        _gbc.gridheight = 1;
        _gbc.fill = GridBagConstraints.BOTH;
        _gbc.weightx = 1;
        _gbc.weighty = 0;
        _gbc.anchor = GridBagConstraints.NORTH;
        ((GridBagLayout)_panel.getLayout()).setConstraints(btnIncludeList, _gbc);
        _panel.add(btnIncludeList);
        // END KGU#620 2018-12-19

        _gbc.gridx = 11;
        
        _gbc.insets.left = border;
        
        return _gbc.gridx + _gbc.gridwidth;
	}

    // listen to actions
    @Override
    public void actionPerformed(ActionEvent event) {
    	Object source = event.getSource(); 
//        if (source == btnLicenseText) {
//        	File licFile = null;
//        	String licName = (String)this.cbLicenseName.getSelectedItem();
//        	if (licName != null && licName.endsWith(" (pool)")) {
//        		String fileName = LicFilter.getNamePrefix() + licName.substring(0, licName.lastIndexOf(" (pool)")) +
//        				"." + LicFilter.acceptedExtension();
//        		File[] licFiles = Ini.getIniDirectory().listFiles(new LicFilter());
//        		for (File file: licFiles) {
//        			if (fileName.equals(file.getName())) {
//        				licFile = file;
//        				break;
//        			}
//        		}
//				licName = null;
//        	}
//        	LicenseEditor licEditor = new LicenseEditor(this.frame, licFile, this.licenseInfo, licName);
//			licEditor.addWindowListener(this);
//        	licEditor.setVisible(true);
//        }
//        else {
//        	if (source == btnOK) {
//        		String licName = (String)cbLicenseName.getSelectedItem();
//        		if (licName != null && licName.endsWith(" (pool)") &&
//        			(licenseInfo.licenseText == null || licenseInfo.licenseText.trim().isEmpty() ||
//        					JOptionPane.showConfirmDialog(frame, 
//        							msgOverrideLicense.getText().replace("%", licName),
//        							msgLicenseChange.getText(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)) {
//        			licenseInfo.licenseText = getLicenseTextFromPool(licName.substring(0, licName.lastIndexOf(" (pool)")));
//        		}
//        	}
//        	super.actionPerformed(event);
//        }
    	if (source == btnAttributes) {
    		RootAttributes oldLicInfo = licenseInfo.copy();
    		attrInspr = new AttributeInspector(frame, licenseInfo);
    		attrInspr.setVisible(true);
    		if (!attrInspr.isCommitted()) {
    			licenseInfo = oldLicInfo;
    		}
    	}
        // START KGU#620 2018-12-19: Issue #651 - Include list now editable via a button
    	else if (source == btnIncludeList) {
    		String oldList = txtIncludeList.getText();
        	txtIncludeList.setFont(txtText.getFont());
        	// START KGU#741 2019-10-04: Bugfix #757
    		//int fontHeight = txtIncludeList.getFontMetrics(txtIncludeList.getFont().deriveFont(FONT_SIZE)).getHeight();
    		//scrIncludeList.setPreferredSize(new Dimension(scrIncludeList.getPreferredSize().width, (int)Math.ceil(10 * fontHeight)));
        	txtIncludeList.setColumns(30);
        	txtIncludeList.setRows(10);
        	// END KGU#741 2019-10-04
    		int answer = JOptionPane.showConfirmDialog(frame,
    				scrIncludeList, lblIncludeList.getText(),
    				JOptionPane.OK_CANCEL_OPTION,
    				JOptionPane.PLAIN_MESSAGE,
    				IconLoader.getIcon(71)	// Symbol for includables
    				);
    		if (answer != JOptionPane.OK_OPTION) {
    			txtIncludeList.setText(oldList);
    		}
    		else {
        		StringList includes = this.getIncludeList();
        		int nIncludes = 0;
        		if (includes != null) {
        			nIncludes = includes.count();
        			btnIncludeList.setToolTipText(includes.concatenate(", "));
        		}
        		else {
        			btnIncludeList.setToolTipText(null);
        		}
        		btnIncludeList.setText(lblIncludeList.getText() + " (" + nIncludes + ")");
    		}
    	}
    	// END KGU#620 2018-12-19
    	else {
    		super.actionPerformed(event);
    	}
    }
    
    // START KGU#376 2017-07-01: Enh. #389 display-width-aware text setting
    /**
     * Fills the Include List text area with the names of includable diagrams given
     * as {@code includeNames}
     * @param includeNames - a StringList holding a diagram name per line or null.
     */
    public void setIncludeList(StringList includeNames)
    {
    	// START KGU#620 2018-12-19: Issue #651 - This gets way easier now
//    	if (includeNames == null) {
//    		return;
//    	}
//    	FontMetrics fm = txtIncludeList.getFontMetrics(txtIncludeList.getFont());
//    	int width = txtIncludeList.getWidth();	// Either this width is wrong or the font metrics result is
//    	StringList lines = new StringList();
//    	String line = "";
//    	for (int i = 0; i < includeNames.count(); i++) {
//    		String name = includeNames.get(i);
//    		if (line.isEmpty() || fm.stringWidth(line + name) < width) {
//    			line += ", " + name;
//    		}
//    		else {
//    			lines.add(line.substring(2).trim() + (i + 1 < includeNames.count() ? "," : ""));
//    			line = ", " + name;
//    		}
//    	}
//    	if (!line.isEmpty()) {
//    		lines.add(line.substring(2));
//    	}
//    	txtIncludeList.setText(lines.getText());
    	if (includeNames == null) {
    		includeNames = new StringList();
    		btnIncludeList.setToolTipText(null);
    	}
    	else {
    		btnIncludeList.setToolTipText(includeNames.concatenate(", "));
    	}
    	txtIncludeList.setText(includeNames.getText());
    	btnIncludeList.setText(lblIncludeList.getText() + " (" + includeNames.count() + ")");
    }
    
    /**
     * Extracts the items (names of includable diagrams) out of the multi-line
     * comma-separated text of this.txtIncludeList and returns them as a StringList.  
     * @return a StringList with a name per element or null
     */
    public StringList getIncludeList()
    {
    	StringList names = null;
    	String content = txtIncludeList.getText().trim();
    	if (!content.isEmpty()) {
    		names = StringList.explode(txtIncludeList.getText(), "\n");
    		names = StringList.explode(names, ",");
    		names.removeAll("");
    		for (int i = 0; i < names.count(); i++) {
    			names.set(i, names.get(i).trim());
    		}
    	}
    	return names;
    }
    // END KGU#376 2017-07-01

    // START KGU#620 2018-12-20: Issue #651 - Ensure the Include List button has proper caption on start
	@Override
	public void LangChanged(LangEvent evt) {
		StringList includeNames = this.getIncludeList();
		int nIncludes = 0;
		if (includeNames != null) { nIncludes = includeNames.count(); }
    	btnIncludeList.setText(lblIncludeList.getText() + " (" + nIncludes + ")");
    	/* It should only once be triggered - on start. Remove listener now lest all instances of this
    	 * class should stay in the listener list of the static variable forever 
    	 */
    	lblIncludeList.removeLangEventListener(this);
	}
	// END KGU#620 2018-12-20
}

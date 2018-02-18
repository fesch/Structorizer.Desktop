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
 *      Kay Gürtzig     2017.03.13      First Issue (for Enh. requ. #372)
 *      Kay Gürtzig     2017.05.21      Attribute editing now delegated to new class AttributeInspector
 *      Kay Gürtzig     2017.06.30      Enh. #389: Text area for Include list added.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      In addition to the usual fields (text and comment) this editor also handles author name and
 *      license aspects, in future varaibel and type management are likely to be added. 
 *
 ******************************************************************************************************///

import java.awt.Color;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lu.fisch.structorizer.elements.RootAttributes;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.utils.StringList;

/**
 * Enhanced and specialized element editor for diagram Roots
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class InputBoxRoot extends InputBox /*implements WindowListener*/ {
	
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
	private JLabel lblIncludeList;
	private JTextArea txtIncludeList;
    protected JScrollPane scrIncludeList;
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

//		int border = (int)(5 * scaleFactor);
//		_gbc.insets = new Insets(2*border, border, 0, border);
		
		lblIncludeList = new JLabel("Diagrams to be included");
		txtIncludeList = new JTextArea();
	    scrIncludeList = new JScrollPane(txtIncludeList);

	    txtIncludeList.addKeyListener(this);
	    // START KGU 2018-02-16: Make sure the includes area isn't mistaken for comments or signature text
	    txtIncludeList.setBackground(new Color(255,255,210));
	    // END KGU 2018-02-16
        // Issue #163 - tab isn't really needed within the text
        txtIncludeList.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        txtIncludeList.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

        scalableComponents.addElement(txtIncludeList);
        
        _gbc.gridx = 1;
		_gbc.gridy = lineNo;
		_gbc.gridheight = 1;
		_gbc.gridwidth = 18;
        _gbc.fill = GridBagConstraints.BOTH;
        _gbc.weightx = 1;
        _gbc.weighty = 0;
        _gbc.anchor = GridBagConstraints.NORTH;
		_panel.add(lblIncludeList, _gbc);

		_gbc.gridx = 1;
		_gbc.gridy = ++lineNo;
		_gbc.gridheight = 4;
		_gbc.gridwidth = 18;
		_gbc.fill = GridBagConstraints.BOTH;
		_gbc.weightx = 1;
		_gbc.weighty = 1;
		_gbc.anchor = GridBagConstraints.NORTH;
		_panel.add(scrIncludeList, _gbc);

		int fontHeight = txtIncludeList.getFontMetrics(txtIncludeList.getFont().deriveFont(FONT_SIZE)).getHeight();
		scrIncludeList.setPreferredSize(new Dimension(getPreferredSize().width, (int)Math.ceil(2 * fontHeight)));

		_gbc.gridx = 1;
		_gbc.gridy = (lineNo += _gbc.gridheight);
		_gbc.gridwidth = 18;
		_gbc.gridheight = 1;
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
		
//		lblAuthorName = new JLabel("Author");
//		txtAuthorName = new JTextField(40);
//		btnLicenseText = new JButton("License text");
//		btnLicenseText.addActionListener(this);
//		cbLicenseName = new JComboBox<String>();
//		cbLicenseName.setToolTipText("Select an available license from the personal license pool or the current one to edit it.");
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
//        ((GridBagLayout)_panel.getLayout()).setConstraints(lblAuthorName, _gbc);
//        _panel.add(lblAuthorName);
//
//        _gbc.insets.left = 0;
//        
//        _gbc.gridx = 2;
//        _gbc.gridwidth = 3;
//        _gbc.gridheight = 1;
//        _gbc.fill = GridBagConstraints.BOTH;
//        _gbc.weightx = 5;
//        _gbc.weighty = 0;
//        _gbc.anchor = GridBagConstraints.NORTH;
//        ((GridBagLayout)_panel.getLayout()).setConstraints(txtAuthorName, _gbc);
//        _panel.add(txtAuthorName);
//        
//        _gbc.insets.left = border;
//
//        _gbc.gridx = 7;
//        _gbc.gridwidth = 1;
//        _gbc.gridheight = 1;
//        _gbc.fill = GridBagConstraints.BOTH;
//        _gbc.weightx = 0;
//        _gbc.weighty = 0;
//        _gbc.anchor = GridBagConstraints.NORTH;
//        ((GridBagLayout)_panel.getLayout()).setConstraints(btnLicenseText, _gbc);
//        _panel.add(btnLicenseText);
//        
//        _gbc.insets.left = 0;
//        
//        _gbc.gridx = 11;
//        _gbc.gridwidth = 1;
//        _gbc.gridheight = 1;
//        _gbc.fill = GridBagConstraints.BOTH;
//        _gbc.weightx = 0;
//        _gbc.weighty = 0;
//        _gbc.anchor = GridBagConstraints.NORTH;
//        ((GridBagLayout)_panel.getLayout()).setConstraints(cbLicenseName, _gbc);
//        _panel.add(cbLicenseName);
        ((GridBagLayout)_panel.getLayout()).setConstraints(btnAttributes, _gbc);
        _panel.add(btnAttributes);
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
    	else {
    		super.actionPerformed(event);
    	}
    }
    
//    private String getLicenseTextFromPool(String licenseName) {
//		String content = "";
//		String error = null;
//		BufferedReader br = null;
//		File licDir = Ini.getIniDirectory();
//		String fileName = licDir.getAbsolutePath() + File.separator + LicFilter.getNamePrefix() +
//				licenseName + "." + LicFilter.acceptedExtension();
//		try {
//			InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
//			br = new BufferedReader(isr);
//			String line = null;
//			while ((line = br.readLine()) != null) {
//				content += line + '\n';
//			};
//		} catch (Exception ex) {
//			error = ex.getMessage();
//		}
//		if (br != null) {
//			
//			try {
//				br.close();
//			} catch (IOException ex) {}
//		}
//		if (error != null) {
//			JOptionPane.showMessageDialog(frame, msgLicenseLoadError.getText().
//					replace("%1", licenseName).replace("%2", error),
//					"Error " + fileName, JOptionPane.ERROR_MESSAGE);
//		}
//		return content;	
//	}
//
//	private void updateLicenseChoice()
//    {
//    	String oldSel = licenseInfo.licenseName;
//    	int selIx = cbLicenseName.getSelectedIndex();
//    	if (selIx >= 0) {
//    		oldSel = cbLicenseName.getItemAt(selIx);
//    	}
//    	selIx = -1;
//    	cbLicenseName.setSelectedIndex(selIx);
//    	cbLicenseName.removeAllItems();
//		File licDir = Ini.getIniDirectory();
//		File[] licFiles = licDir.listFiles(new LicFilter());
//		String prefix = LicFilter.getNamePrefix();
//		String ext = LicFilter.acceptedExtension();
//		boolean rootContained = false;
//		for (File licFile: licFiles) {
//			String fileName = licFile.getName();
//			String licName = fileName.substring(prefix.length(), fileName.lastIndexOf("."+ext)) + " (pool)";
//			cbLicenseName.addItem(licName);
//			if (licName.equals(oldSel)) {
//				selIx = cbLicenseName.getItemCount() - 1;
//			}
//			if (licName.equals(licenseInfo.licenseName)) {
//				rootContained = true;
//			}
//		}
//		if (!rootContained && this.licenseInfo.licenseName != null && !this.licenseInfo.licenseName.isEmpty()) {
//			cbLicenseName.addItem(this.licenseInfo.licenseName);
//			cbLicenseName.setSelectedItem(this.licenseInfo.licenseName);
//		}
//		if (selIx >= 0) {
//			cbLicenseName.setSelectedIndex(selIx);
//		}
//    }

//	@Override
//	public void windowActivated(WindowEvent arg0) {
//		// TODO Auto-generated method stub
//		//System.out.println(arg0.getSource() + " activated...");
//		
//	}
//
//	@Override
//	public void windowClosed(WindowEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void windowClosing(WindowEvent evt) {
//		if (evt.getSource() instanceof LicenseEditor) {
//			this.updateLicenseChoice();
//		}
//	}
//
//	@Override
//	public void windowDeactivated(WindowEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void windowDeiconified(WindowEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void windowIconified(WindowEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void windowOpened(WindowEvent evt) {
//		Object source = evt.getSource();
//		//System.out.println(source + " opened...");
//		if (source instanceof LicenseEditor) {
//			LicenseEditor led = (LicenseEditor)source;
//			led.setSize(LicenseEditor.PREFERRED_WIDTH, LicenseEditor.PREFERRED_HEIGHT);
//		}
//		else if (source == this) {
//			this.updateLicenseChoice();
//		}
//	}
    
    // START KGU#376 2017-07-01: Enh. #389 display-width-aware text setting
    /**
     * Fills the Include List text area with the names of includable diagrams given
     * as {@code includeNames}
     * @param includeNames - a StringList holding a diagram name per line or null.
     */
    public void setIncludeList(StringList includeNames)
    {
    	if (includeNames == null) {
    		return;
    	}
    	FontMetrics fm = txtIncludeList.getFontMetrics(txtIncludeList.getFont());
    	int width = txtIncludeList.getWidth();	// FIXME: Either this width is wrong or the font metrics result
    	StringList lines = new StringList();
    	String line = "";
    	for (int i = 0; i < includeNames.count(); i++) {
    		String name = includeNames.get(i);
    		if (line.isEmpty() || fm.stringWidth(line + name) < width) {
    			line += ", " + name;
    		}
    		else {
    			lines.add(line.substring(2).trim() + (i + 1 < includeNames.count() ? "," : ""));
    			line = ", " + name;
    		}
    	}
    	if (!line.isEmpty()) {
    		lines.add(line.substring(2));
    	}
    	txtIncludeList.setText(lines.getText());
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
}

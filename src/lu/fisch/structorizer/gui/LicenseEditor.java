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
 *      Description:    Simple text file editor for License files according to issue #372.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.13      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.io.LicFilter;
import lu.fisch.structorizer.locales.LangDialog;

/**
 * Simple frame to allow users to edit their license file(s) as plain text.
 *      For this editor, there are at least 6 scenarios:
 *      1. Called from SaveOptionDialog with a selected license file
 *      2. Called from SaveOptionDialog with a new license name - an empty license file should then
 *         already have been created (hence it's more or less like case 1)
 *      3. Called from InputBoxRoot for a certain diagram with assigned license information selected
 *      4. Called from InputBoxRoot for a certain (unsaved) Root without license information but a selected
 *             file from pool
 *      5. Called from InputBoxRoot for a Root with assigned license info but a selected license from pool.
 *      6. Called from InputBoxRoot for a Root with assigned license name but no own text while the pool
 *         may contain a license text with this name (a: license available, b: not available).
 *      What is to be shown/done in these cases?
 *      1. Content of the license file, allowed to modify, copy, rename, delete; title: name of the
 *         license (possibly with " (pool)" appended?)
 *      2. like 1.
 *      3. Content of the license stored in Root, allow to modify (in-place), copy (to pool), [replace
 *         (from pool)?]; title with Root name and license name
 *      4. Content of the license file in the pool, not allowed to modify?; title rather without root
 *         but the license name
 *      5. like 4.
 *      6. Content of the license file in the pool, different text colour, not allowed to modify but to
 *         adopt as copy -> case 3; title like 4 but with root name in parentheses?
 */
@SuppressWarnings("serial")
public class LicenseEditor extends LangDialog implements ActionListener, UndoableEditListener {

	static private final int MIN_FONT_SIZE = 6;
	static protected final int PREFERRED_WIDTH = 500;
	static protected final int PREFERRED_HEIGHT = 500;

	private Frame frame;
	private InputBoxRoot.RootLicenseInfo root = null;			// The associated diagram if any
	private JPanel panel;
	private JTextPane textPane;
	private StyledDocument doc = null;
	public JMenu menuFile;
	public JMenuItem menuFileCommit;
	public JMenuItem menuFileSave;
	public JMenuItem menuFileSaveAs;
	public JMenuItem menuFileRename;
	public JMenuItem menuFileReload;
	public JMenuItem menuFileDelete;
	public JMenuItem menuFileQuit;
	public JMenu menuEdit;
	public JMenuItem menuEditCopy;
	public JMenuItem menuEditCut;
	public JMenuItem menuEditPaste;
	public JMenuItem menuEditClear;
	public JMenuItem menuEditUndo;
	public JMenuItem menuEditRedo;
	public JMenu menuProp;
	public JMenuItem menuPropFont;
	public JMenuItem menuPropFontUp;
	public JMenuItem menuPropFontDown;
	private UndoManager undoMan = new UndoManager();
	private File licenseFile = null;
	private boolean licenseFromPool = false;

	public static LangTextHolder titleString = new LangTextHolder("Structorizer License Editor: %");
	public static LangTextHolder msgNewLicName = new LangTextHolder("New license name");
	public static LangTextHolder msgCouldntWriteLicense = new LangTextHolder("Could not write license \"%1\" to file!\nReason: %2.");
	public static LangTextHolder msgCouldntRenameLicense = new LangTextHolder("Could not rename license \"%1\" to \"%2\"!\nA license with the new name may already exist.");
	public static LangTextHolder msgSureToDelete = new LangTextHolder("Are you sure to delete license \"%\"? This is not undoable!");
	public static LangTextHolder msgSureToDiscard = new LangTextHolder("Are you sure to discard all unsaved changes for license \"%\"?");
	public static LangTextHolder msgPendingChanges = new LangTextHolder("There are unsaved changes for license \"%\".\nShall they be saved before you leave?");
	public static LangTextHolder msgOverwriteExisting = new LangTextHolder("License \"%\" already exists in the pool.\nContinue to overwrite it?");

	/**
	 * Constructor for the inspection of the configured set of licenses in the pool (cases 1, 2).
	 * @param _frame - owning frame
	 * @param _licenseFile - the selected license file
	 */
	public LicenseEditor(Frame _frame, File _licenseFile)
	{
		frame = _frame;
		licenseFile = _licenseFile;

		initComponents();
		setModal(true);

		String error = load();
		this.undoMan.discardAllEdits();

		if (error != null) {
			JOptionPane.showMessageDialog(null,
					error, _licenseFile.getAbsolutePath(),
					JOptionPane.ERROR_MESSAGE);
		}
		this.doButtons();
	}

	/**
	 * Constructor for the Root editor, i.e. for a specific diagram (cases 3 ...6).
	 * @param _frame - owning frame
	 * @param _licenseFile - a selected license file (cases 4, 5, 6a) or null (cases 3, 6b)
	 * @param _licInfo - license info of the diagram for which license settings are to be managed
	 * @param _licenseName - the license name as set by root (case 3, 6)
	 */
	public LicenseEditor(Frame _frame, File _licenseFile, InputBoxRoot.RootLicenseInfo _licInfo, String _licenseName)
	{
		frame = _frame;
		root = _licInfo;
		licenseFile = _licenseFile;
		if (licenseFile == null) {
			// case 3 or 6
			licenseFile = Ini.getIniDirectory();
		}

		initComponents();
		setModal(true);

		String errors = null;
		// case 3 or 6?
		if (licenseFile.isDirectory()) {
			if (root.licenseText != null && !root.licenseText.trim().isEmpty())
			try {
				doc.insertString(0, root.licenseText, null);
			} catch (BadLocationException e) {
				errors = e.getMessage();
			}
		}
		else if (_licenseName != null && !_licenseName.trim().isEmpty()) {
			File licFile = this.identifyLicenseFile(_licenseName);
			if (licFile != null) {
				this.licenseFile = licFile;
			}
		}
		
		if (!this.licenseFile.isDirectory()) {
			errors = load();
		}
		if (errors != null) {
			JOptionPane.showMessageDialog(_frame, errors, "Error on loading license file", JOptionPane.ERROR_MESSAGE);
		}
		this.undoMan.discardAllEdits();
		if (this.licenseFile.exists() && !this.licenseFile.isDirectory()) {
			this.textPane.setEditable(false);
		}
		doButtons();
	}

	// Helper method for the constructor
	private File identifyLicenseFile(String _licenseName) {
		String fileName = LicFilter.getNamePrefix() +
				_licenseName.substring(0, _licenseName.indexOf(" (pool)")) +
				"." + LicFilter.acceptedExtension();
		for (File licFile: this.licenseFile.listFiles(new LicFilter())) {
			if (licFile.getName().equals(fileName)) {
				return licFile;
			}
		}
		return null;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 */
	//@SuppressWarnings("unchecked")
	private void initComponents() {

		panel = new JPanel();

//		String licName = this.getLicenseName();
//		this.setTitle(titleString.getText().replace("%", licName));

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setIconImage(IconLoader.ico065.getImage());	// Paragraph sign

		menuFile = new JMenu("File");
		menuEdit = new JMenu("Edit");
		menuProp = new JMenu("Properties");

		menuFileCommit = new JMenuItem("Commit", IconLoader.ico003);
		menuFileCommit.addActionListener(this);
		menuFileCommit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileSave = new JMenuItem("Save to pool", IconLoader.ico003);
		menuFileSave.addActionListener(this);
		menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileSaveAs = new JMenuItem("Save as ...", IconLoader.ico003);
		menuFileSaveAs.addActionListener(this);
		menuFileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuFileRename = new JMenuItem("Rename ...");
		menuFileRename.addActionListener(this);
		menuFileRename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileReload = new JMenuItem("Reload/Revert", IconLoader.ico025);
		menuFileReload.addActionListener(this);
		menuFileReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		menuFileDelete = new JMenuItem("Delete", IconLoader.ico066);
		menuFileDelete.addActionListener(this);
		menuFileDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileQuit = new JMenuItem("Quit");
		menuFileQuit.addActionListener(this);
		menuFileQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menuEditUndo = new JMenuItem("Undo",IconLoader.ico039);
		menuEditUndo.addActionListener(this);
		menuEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuEditRedo = new JMenuItem("Redo",IconLoader.ico038);
		menuEditRedo.addActionListener(this);
		menuEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//    	menuEditCut = new JMenuItem("Cut",IconLoader.ico044);
//    	menuEditCut.addActionListener(this);
//    	menuEditCopy = new JMenuItem("Copy",IconLoader.ico042);
//    	menuEditCopy.addActionListener(this);
//    	menuEditPaste = new JMenuItem("Paste",IconLoader.ico043);
//    	menuEditPaste.addActionListener(this);
		menuEditClear = new JMenuItem("Clear",IconLoader.ico045);
		menuEditClear.addActionListener(this);
		menuEditClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menuPropFont = new JMenuItem("Font ...",IconLoader.ico023);
		menuPropFont.addActionListener(this);
		menuPropFontUp = new JMenuItem("Enlarge font", IconLoader.ico033);
		menuPropFontUp.addActionListener(this);
		menuPropFontUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK));
		menuPropFontDown = new JMenuItem("Diminish font", IconLoader.ico034);
		menuPropFontDown.addActionListener(this);
		menuPropFontDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK));

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFile);
		menuFile.setMnemonic(KeyEvent.VK_F);
		if (this.root != null) {
			menuFile.add(menuFileCommit);
		}
		menuFile.add(menuFileSave);
		menuFile.add(menuFileSaveAs);
		menuFile.add(menuFileRename);
		menuFile.add(menuFileReload);
		if (this.root == null) {
			menuFile.add(menuFileDelete);
		}
		menuFile.add(menuFileQuit);
		
		menuBar.add(menuEdit);
		menuEdit.setMnemonic(KeyEvent.VK_E);
		menuEdit.add(menuEditUndo);
		menuEdit.add(menuEditRedo);
//		menuEdit.add(menuEditCut);
//		menuEdit.add(menuEditCopy);
//		menuEdit.add(menuEditPaste);
		menuEdit.add(menuEditClear);
		
		menuBar.add(menuProp);
		menuProp.setMnemonic(KeyEvent.VK_P);
		menuProp.add(menuPropFont);
		menuProp.add(menuPropFontUp);
		menuProp.add(menuPropFontDown);
		setJMenuBar(menuBar);
		
		textPane = new JTextPane();
		// START KGU#255 2016-09-25: Bugfix #251 background setting diddn't work with Nimbus
		//textPane.setBackground(Color.BLACK);
		Color bgColor = Color.WHITE;
		UIDefaults defaults = new UIDefaults();
		defaults.put("TextPane[Enabled].backgroundPainter", bgColor);
		textPane.putClientProperty("Nimbus.Overrides", defaults);
		textPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		textPane.setBackground(bgColor);
		// END KGU#255 2016-09-25
		textPane.setForeground(Color.BLACK);
		JScrollPane scrText = new JScrollPane(textPane);
		doc = textPane.getStyledDocument();
		Style defStyle = doc.getStyle("default");
		// The standard font size (11) wasn't in the FontChooser choice list 
		defStyle.addAttribute(StyleConstants.FontSize, 12);
		// START KGU#279 2016-10-11: Bugfix #268
		textPane.setEditable(true);
		// END KGU#279 2016-10-11
		doc.addUndoableEditListener(this);
		
		// START KGU#393 2017-05-09: Issue #400
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent e) 
			{
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					dispose();
				}
				else if(e.getKeyCode() == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown()))
				{
					dispose(true);
				}
			}
			
			public void keyReleased(KeyEvent ke) {} 
			public void keyTyped(KeyEvent kevt) {}
		};
		textPane.addKeyListener(keyListener);
		// END KGU#393 2017-05-09		

		
		panel.setLayout(new BorderLayout());
		panel.add(scrText, BorderLayout.CENTER);
		this.add(panel, null);
		this.pack();
		this.doButtons();
		this.setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
	}

	/**
	 * Returns the name of the currently edited license 
	 * @return
	 */
	public String getLicenseName()
	{
		return getLicenseName(false);
	}
	
	public String getLicenseName(boolean withoutMarkers)
	{
		if (this.licenseFile.isDirectory()) {
			if (root != null && root.licenseText != null && root.licenseName != null && !root.licenseName.trim().isEmpty()) {
				return root.licenseName;
			}
			else {
				return "???";
			}
		}
		String fileName = this.licenseFile.getName();
		String licName = fileName.substring(LicFilter.getNamePrefix().length(),
				fileName.lastIndexOf("." + LicFilter.acceptedExtension()));
		if (this.root != null && !withoutMarkers) {
			licName += " (pool)";
		}
		return licName;
	}

	private void clear()
	{
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	public int getFontSize()
	{
		MutableAttributeSet attrs = doc.getStyle("default");
		return StyleConstants.getFontSize(attrs);
	}

	public void setFontSize(int newFontSize)
	{
		// Modify the default style
		doc.getStyle("default").addAttribute(StyleConstants.FontSize, newFontSize);
		// Modify the font of the already written text
		MutableAttributeSet attrs = new javax.swing.text.SimpleAttributeSet();
		StyleConstants.setFontSize(attrs, newFontSize);
		doc.setCharacterAttributes(0, doc.getLength(), attrs, false);
	}

	public void selectFont()
	{
		FontChooser fontChooser = new FontChooser(frame);
		// set fields
		MutableAttributeSet stAttrs = doc.getStyle("default");
		fontChooser.setFont(doc.getFont(stAttrs));
		fontChooser.setVisible(true);
		// Get the user selection
		String fontName = fontChooser.getCurrentFont().getName();
		int fontSize = fontChooser.getCurrentFont().getSize();

		// Modify the default style
		Style style = doc.getStyle("default");
		style.addAttribute(StyleConstants.FontFamily, fontName);
		style.addAttribute(StyleConstants.FontSize, fontSize);

		// Modify the font of the already written text
		MutableAttributeSet attrs = new javax.swing.text.SimpleAttributeSet();
		StyleConstants.setFontFamily(attrs, fontName);
		StyleConstants.setFontSize(attrs, fontSize);
		doc.setCharacterAttributes(0, doc.getLength(), attrs, false);
	}

	/**
	 * Raises the font size by 2
	 */
	public void fontUp()
	{
		setFontSize(2*((getFontSize()+1)/2) + 2);
	}

	/**
	 * Reduces the font size by 2 unless MIN_FONT_SIZE would be undershot
	 */
	public void fontDown()
	{
		setFontSize(Math.max(MIN_FONT_SIZE, 2*(getFontSize()/2) - 2));
	}
	
	/**
	 * (Re-)Loads the current license text from file this.licenceFile (supposed to be set).
	 * @return an error string if something went wrong, null otherwise
	 */
	private String load()
	{
		int oldHeight = this.getHeight();
		int oldWidth = this.getWidth();
		String content = "";
		String error = null;
		BufferedReader br = null;
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(this.licenseFile), "UTF-8");
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				content += line + '\n';
			};
			this.setSize(oldWidth, oldHeight);
		} catch (UnsupportedEncodingException e) {
			error = e.getMessage();
		} catch (FileNotFoundException e) {
			error = e.getMessage();
		} catch (IOException e) {
			error = e.getMessage();
		}
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {}
		}
		try {
			doc.insertString(0, content, null);
			this.licenseFromPool = true;
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			error = e.getMessage();
		}
		return error;	
	}
	
	/**
	 * Saves the current license text under the given name to the pool. In case
	 * this editor is associated to a Root object and the Root's license text is
	 * to be updated, use update() instead.
	 * @return true if the action succeeded
	 */
	private boolean save(boolean override)
	{
		String error = null;
		if (!override && this.licenseFile.exists()) {
			int answer = JOptionPane.showConfirmDialog(frame,
					msgOverwriteExisting.getText().replace("%", this.getLicenseName(true)),
					this.licenseFile.getAbsolutePath(),
					JOptionPane.QUESTION_MESSAGE);
			if (answer != JOptionPane.OK_OPTION) {
				return false;
			}
		}
		BufferedWriter bw = null;
		try {
			FileOutputStream fos = new FileOutputStream(this.licenseFile);
			OutputStreamWriter osr = new OutputStreamWriter(fos, "UTF-8");
			bw = new BufferedWriter(osr);
			bw.write(doc.getText(0, doc.getLength()));
		} catch (FileNotFoundException e) {
			error = e.getMessage();
		} catch (IOException e) {
			error = e.getMessage();
		} catch (BadLocationException e) {
			error = e.getMessage();
			e.printStackTrace();
		}
		if (bw != null) {
			try {
				bw.close();
			}
			catch (IOException e)
			{
				error = e.getMessage();
			}
		}
		if (error != null) {
			JOptionPane.showMessageDialog(this,
					msgCouldntWriteLicense.getText().replace("%1", getLicenseName()).replace("%2", error),
					licenseFile.getAbsolutePath(), JOptionPane.ERROR_MESSAGE);
		}
		else {
			this.undoMan.discardAllEdits();
			this.licenseFromPool = true;
		}
		return error == null;
	}
	
	private void saveAs()
	{
		String newLicName = JOptionPane.showInputDialog(this,
				msgNewLicName.getText(), this.getLicenseName());
		if (!newLicName.equalsIgnoreCase(this.getLicenseName(true))) {
			File oldFile = this.licenseFile;
			String fileName = LicFilter.getNamePrefix() + newLicName + "." + LicFilter.acceptedExtension();
			this.licenseFile = new File(Ini.getIniDirectory().getAbsolutePath() + File.separator + fileName);
			if (!save(false)) {
				this.licenseFile = oldFile;
			}
		}
	}
	
	private void update()
	{
		try {
			root.licenseText = doc.getText(0, doc.getLength());
			this.undoMan.discardAllEdits();
			licenseFromPool = false;
		} catch (BadLocationException e) {	}
	}
	
	private void rename()
	{
		String newLicName = JOptionPane.showInputDialog(this,
				msgNewLicName.getText(), this.getLicenseName());
		if (!newLicName.equalsIgnoreCase(this.getLicenseName(true))) {
			String fileName = LicFilter.getNamePrefix() + newLicName + "." + LicFilter.acceptedExtension();
			File newLicFile = new File(this.licenseFile.getParent() + File.separator + fileName);
			if (!newLicFile.exists() && this.licenseFile.renameTo(newLicFile)) {
				this.licenseFile = newLicFile;
			}
			else {
				JOptionPane.showMessageDialog(this,
						msgCouldntRenameLicense.getText().replace("%1", getLicenseName()).replace("%2", newLicName),
						licenseFile.getAbsolutePath(), JOptionPane.ERROR_MESSAGE);				
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent actev) {
		Object src = actev.getSource();
		if (src == menuPropFont) {
			selectFont();
		}
		else if (src == menuPropFontUp) {
			fontUp();
		}
		else if (src == menuPropFontDown)
		{
			fontDown();
		}
		else if (src == menuFileCommit) {
			update();
		}
		else if (src == menuFileSave) {
			if (this.licenseFile.isDirectory()) {
				saveAs();
			}
			else {
				save(this.root == null);
			}
		}
		else if (src == menuFileSaveAs) {
			saveAs();
		}
		else if (src == menuFileRename) {
			// FIXME should we offer to save? In theory no need, file isn't open
			if (!this.licenseFile.isDirectory()) {
				this.rename();
			}
		}
		else if (src == menuFileReload) {
			if (!this.undoMan.canUndoOrRedo()) {
				// no need to reload
				return;
			}
			else if (root != null && !licenseFromPool && root.licenseText != null) {
				try {
					doc.remove(0, doc.getLength());
					doc.insertString(0, root.licenseText, null);
					this.undoMan.discardAllEdits();
				} catch (BadLocationException e) {
					System.err.println("LicenseEditor.actionPerformed(Reload): " + e.getLocalizedMessage());
				}
			}
			else {
				int answer = JOptionPane.showConfirmDialog(this, 
						msgSureToDiscard.getText().replace("%", getLicenseName()),
						this.licenseFile.getAbsolutePath(), JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					this.load();
				}
			}
		}
		else if (src == menuFileDelete) {
			int answer = JOptionPane.showConfirmDialog(this, 
					msgSureToDelete.getText().replace("%", getLicenseName()),
					this.licenseFile.getAbsolutePath(), JOptionPane.OK_CANCEL_OPTION);
			if (answer == JOptionPane.OK_OPTION) {
				this.licenseFile.delete();
				this.licenseFile = this.licenseFile.getParentFile();
			}
		}
		else if (src == menuFileQuit) {
			this.dispose();
		}
		else if (src == menuEditUndo && this.undoMan.canUndo()) {
			this.undoMan.undo();
		}
		else if (src == menuEditRedo && this.undoMan.canRedo()) {
			this.undoMan.redo();
		}
		else if (src == menuEditClear) {
			this.clear();
		}
		this.doButtons();
	}
	
	public void dispose()
	{
		dispose(false);
	}
	
	public void dispose(boolean _saveAnyway)
	{
		// Ask whether to save
		if (this.undoMan.canUndoOrRedo()) {
			int answer = (_saveAnyway ? JOptionPane.OK_OPTION : JOptionPane.DEFAULT_OPTION);
			if (!_saveAnyway) {
				answer = JOptionPane.showConfirmDialog(this, 
					msgPendingChanges.getText().replace("%", getLicenseName()),
					this.licenseFile.getAbsolutePath(), JOptionPane.YES_NO_CANCEL_OPTION);
			}
			if (answer == JOptionPane.OK_OPTION) {
				if (this.root != null) {
					this.update();
				}
				else if (this.licenseFile.isDirectory()) {
					this.saveAs();
				}
				else {
					this.save(true);
				}
			}
			else if (answer == JOptionPane.CANCEL_OPTION) {
				// backed off
				return;
			}
		}
		super.dispose();
	}
	
	private void doButtons()
	{
		boolean validFile = this.licenseFile.exists() && !this.licenseFile.isDirectory();
		this.menuFileCommit.setEnabled(root != null && this.undoMan.canUndoOrRedo());
		this.menuFileSave.setEnabled((root == null) || validFile && this.undoMan.canUndoOrRedo());
		this.menuFileSaveAs.setEnabled(doc.getLength() > 0);
		this.menuFileReload.setEnabled(((root == null && validFile) || (root != null && !this.licenseFromPool)) && this.undoMan.canUndoOrRedo());
		this.menuFileRename.setEnabled(root == null && validFile);
		this.menuFileDelete.setEnabled(root == null && validFile);
		this.menuEditUndo.setEnabled(this.undoMan.canUndo());
		this.menuEditRedo.setEnabled(this.undoMan.canRedo());
		this.menuEditClear.setEnabled(textPane.isEditable());
		String title = (this.root != null ? this.root.rootName + " * " : "") +
				this.getLicenseName();
		if (!textPane.isEditable()) {
			title = "[" + title + "]";
		}
		this.setTitle(titleString.getText().replace("%", title));
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent evt) {
		UndoableEdit udoEd = evt.getEdit();
		undoMan.addEdit(udoEd);
		this.doButtons();
	}

}

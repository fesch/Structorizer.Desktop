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

package lu.fisch.structorizer.executor;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    This class represents the Output text area for executor console mode.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------			----			-----------
 *      Kay Gürtzig     2016-04-08      First Issue (implementing enhancement request #137 / KGU#160)
 *      Kay Gürtzig     2016-04-12      Functionality accomplished
 *      Kay Gürtzig     2016-04-25      Scrolling to last line ensured
 *      Kay Gürtzig     2016-04-26      Converted to JTextPane in order to allow styled output
 *      Kay Gürtzig     2016-09-25      Bugfix #251 averting Nimbus disrespect of panel settings 
 *      Kay Gürtzig     2016-10-11      Enh. #268: Inheritance changed, font selecting opportunities added
 *      Kay Gürtzig     2016-10-17      Issue #268: Font setting source and target corrected (doc's default style)
 *      Kay Gürtzig     2016-11-22      Enh.#284: Font resizing accelerators modified (CTRL_DOWN_MASK added)
 *      Kay Gürtzig     2018-03-13      Enh. #519: Font resizing via ctrl + mouse wheel (newboerg's proposal)
 *      Kay Gürtzig     2018-08-03      Enh. #577: New checkbox menu items "menuLogMeta" and "menuLogCalls"
 *      Kay Gürtzig     2018-08-09      Issue #577: New menu item and accelerator for saving the log
 *
 ******************************************************************************************************
 *
 *      Comment:  /
 *         
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.gui.FontChooser;
import lu.fisch.structorizer.gui.GUIScaler;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.io.LogFilter;
import lu.fisch.structorizer.locales.LangFrame;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.utils.BTextfile;

@SuppressWarnings("serial")
// START KGU#279 2016-10-11: Enh. #268 - inheritance change was necessary to add a menu
// KGU#503 2018-03-13: Enh. #519 - having it implement MouseWheelListener
public class OutputConsole extends LangFrame implements ActionListener, MouseWheelListener {

	static private final int MIN_FONT_SIZE = 6;
	static private final Color[] colours = {Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY,
		Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};

	private JPanel panel;
	private JTextPane textPane;
	private StyledDocument doc = null;
	// START KGU#279 2016-10-11: Enh. #268 Font change opportunity
	public JMenu menuProp;
	public JMenuItem menuPropFont;
	public JMenuItem menuPropFontUp;
	public JMenuItem menuPropFontDown;
	// END KGU#279 2016-10-11
	// START KGU#569 2018-08-03: Enh. #577
	public JMenu menuContent;
	public JCheckBoxMenuItem menuContentLogMeta;
	public JCheckBoxMenuItem menuContentLogCalls;
	public JMenuItem menuContentSave;
	// END KGU#569 2018-08-03
	// START KGU#569 2018-08-04: Enh. #577 - more precise scrolling control
	/** cached height of the {@link #textPane} for detection of the need to scroll */
	private int textHeight = 0;
	/** Last saved log file in this session */
	private File lastSaved = null;
	public static final LangTextHolder msgOverwriteFile = new LangTextHolder("Overwrite existing file?");
	public static final LangTextHolder msgErrorFileSave = new LangTextHolder("Error on saving the file: %!");
	public static final LangTextHolder msgTitleError = new LangTextHolder("Error");
	// END KGU#569 2018-08-04
	
	public OutputConsole()
	{
		initComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 */
	//@SuppressWarnings("unchecked")
	private void initComponents() {

		panel = new JPanel();

		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setTitle("Structorizer Output Console");
		this.setIconImage(IconLoader.getIcon(4).getImage());

		// START KGU#279 2016-10-11: Enh. #268: Font selection opportunity
		menuProp = new JMenu("Properties");
		menuPropFont = new JMenuItem("Font ...",IconLoader.getIcon(23));
		menuPropFont.addActionListener(this);
		menuPropFontUp = new JMenuItem("Enlarge font", IconLoader.getIcon(33));
		menuPropFontUp.addActionListener(this);
		menuPropFontUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK));
		menuPropFontDown = new JMenuItem("Diminish font", IconLoader.getIcon(34));
		menuPropFontDown.addActionListener(this);
		menuPropFontDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK));

		// START KGU#569 2018-08-03: Enh. #577
		menuContent = new JMenu("Contents");
		menuContentLogMeta = new JCheckBoxMenuItem("Log meta-info");
		menuContentLogMeta.setSelected(true);
		menuContentLogCalls = new JCheckBoxMenuItem("Log calls");
		menuContentSave = new JMenuItem("Save log ...", IconLoader.getIcon(3));
		menuContentSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				save();
			}});
		menuContentSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		// END KGU#569 2018-08-03

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuProp);
		menuProp.add(menuPropFont);
		menuProp.add(menuPropFontUp);
		menuProp.add(menuPropFontDown);
		// START KGU#569 2018-08-03: Enh. #577
		menuBar.add(menuContent);
		menuContent.add(menuContentLogMeta);
		menuContent.add(menuContentLogCalls);
		menuContent.addSeparator();
		menuContent.add(menuContentSave);
		// END KGU#569 2018-08-03
		setJMenuBar(menuBar);
		// END KGU#279 2016-10-11

		textPane = new JTextPane();
		// START KGU#255 2016-09-25: Bugfix #251 background setting didn't work with Nimbus
		//textPane.setBackground(Color.BLACK);
		Color bgColor = Color.BLACK;
		UIDefaults defaults = new UIDefaults();
		defaults.put("TextPane[Enabled].backgroundPainter", bgColor);
		textPane.putClientProperty("Nimbus.Overrides", defaults);
		textPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		textPane.setBackground(bgColor);
		// END KGU#255 2016-09-25
		textPane.setForeground(Color.WHITE);
		JScrollPane scrText = new JScrollPane(textPane);
		doc = textPane.getStyledDocument();
		Style defStyle = doc.getStyle("default");
		// The standard font size (11) wasn't in the FontChooser choice list 
		defStyle.addAttribute(StyleConstants.FontSize, 12);
		for (Color colour : colours)
		{
			Style style = doc.addStyle(colour.toString(), null);
			style.addAttribute(StyleConstants.Foreground, colour);
		}
		// START KGU#279 2016-10-11: Bugfix #268
		textPane.setEditable(false);
		// END KGU#279 2016-10-11

		panel.setLayout(new BorderLayout());
		panel.add(scrText, BorderLayout.CENTER);
		this.add(panel, null);
		this.setSize(500, 250);
		// START KGU#503 2018-03-13: Enh. #519 - Allow ctrl + mouse wheel to "zoom"
		scrText.addMouseWheelListener(this);
		// END KGU#503 2018-03-13
		// START KGU#569 2018-08-04: Enh. #577 - more precise scrolling control
		textHeight = textPane.getHeight();
		// END KGU#569 2018-08-04
	}

	public void clear()
	{
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException ex) {
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Trouble clearing the content.", ex);
			// END KGU#484 2018-04-05
		}
		// START KGU#569 2018-08-04: Enh. #577 - more precise scrolling control
		textHeight = textPane.getHeight();
		// END KGU#569 2018-08-04
	}

	/**
	 * Appends string _text to the textArea as is, i.e. without additional newline.
	 * @param _text - a string
	 */
	public void write(String _text)
	{
		write(_text, textPane.getForeground());
	}

	/**
	 * Appends string _text in the specified (foreground) colour to the textArea
	 * as is, i.e. without additional newline.
	 * @param _text - a string
	 * @param _colour - the text colour to use
	 */
	public void write(String _text, Color _colour)
	{
		try {
			this.doc.insertString(doc.getLength(), _text, doc.getStyle(_colour.toString()));
		} catch (BadLocationException e) {
			// START KGU#484 2018-04-05: Issue #463 
			//e.printStackTrace();
			int docLen = this.doc.getLength();
			int txtLen = _text.length();
			if (docLen + txtLen >= Integer.MAX_VALUE && txtLen < docLen) {
				// Remove as many lines from start as necessary to get the required space (tends to be very slow)
				try {
					this.doc.remove(0, txtLen);
					// Another try...
					this.doc.insertString(doc.getLength(), _text, doc.getStyle(_colour.toString()));
				}
				catch (BadLocationException ex) {
					Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to shorten content between 0 and " + txtLen + "(at " + e.offsetRequested() + ").", ex);    				
				}
			}
			else {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Inconsistent content with offset " + e.offsetRequested() + ".", e);
			}
			// END KGU#484 2018-04-05
		}
		// Scroll to end (if there is an easier way, I just didn't find it).
		// START KGU#569 2018-08-04: Enh. #577 - safer and more precise scrolling control
		//Rectangle rect = textPane.getBounds();
		//rect.y = rect.height - 1;
		//rect.height = 1;
		//textPane.scrollRectToVisible(rect);
		int newHeight = textPane.getHeight();
		if (newHeight != textHeight) {
			textHeight = newHeight;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Rectangle rect = textPane.getBounds();
					rect.y = rect.height - 1;
					rect.height = 1;

					try {
						textPane.scrollRectToVisible(rect);
					}
					catch (ArrayIndexOutOfBoundsException ex) {
						Logger.getLogger(getClass().getName()).log(Level.WARNING, "Output console ran out of bounds.", ex);
						System.err.println("Doc length: " + doc.getLength());
						System.err.println("View count: " + textPane.getUI().getRootView(textPane).getViewCount());
						System.err.println("View 0 count: " + textPane.getUI().getRootView(textPane).getView(0).getViewCount());
						// TODO Shall we remove some doc lines? Or just refocus the textPane?
//						try {
//						View rootView = textPane.getUI().getRootView( textPane );
//						View boxView = rootView.getView(0);
//						// The following is likely to be based on some misconceptions! 
//						for( int i = 0; i < boxView.getViewCount()-3000; i++ ) {
//							int line = boxView.getViewIndex( i, Bias.Forward );
//							View paragrView = boxView.getView(line);
//							System.out.println(i + ": doc.remove("+paragrView.getStartOffset()+","+paragrView.getEndOffset()+") out of " + doc.getLength());
//							doc.remove( paragrView.getStartOffset(), paragrView.getEndOffset() );
//						}
//					} catch( BadLocationException e1 ) {
//						e1.printStackTrace();
//					}
					}
					catch (NullPointerException ex) {
						Logger.getLogger(getClass().getName()).log(Level.INFO, "Text pane scrolling trouble (apparently race hazard):", ex);
					}
				}
			});
		}
		// END KGU#569 2018-08-04
	}

	/**
	 * Appends string _text to the textArea with additional newline.
	 * @param _text - a string
	 */
	public void writeln(String _text)
	{
		this.write(_text + "\n");
	}

	/**
	 * Appends string _text in the specified (foreground) colour to the textArea
	 * with additional newline.
	 * @param _text - a string
	 * @param _colour - the text colour to use
	 */
	public void writeln(String _text, Color _colour)
	{
		this.write(_text + "\n", _colour);
	}

	// START KGU#569 2018-08-09: Issue #577
	/**
	 * Callback method for menu action "Save log ...".
	 * @return whether saving has worked.
	 */
	public boolean save()
	{
		boolean done = false;
		JFileChooser dlgSave = new JFileChooser();
		GUIScaler.rescaleComponents(dlgSave);
		dlgSave.setDialogTitle(menuContentSave.getText());
		if (lastSaved != null) {
			dlgSave.setCurrentDirectory(lastSaved);
			dlgSave.setSelectedFile(lastSaved);
		}
		else {
			dlgSave.setSelectedFile(new File("OutputConsole.log"));
		}
		LogFilter filter = new LogFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		int answer = dlgSave.showSaveDialog(this);
		if (answer == JFileChooser.APPROVE_OPTION) {
			File outFile = dlgSave.getSelectedFile();
			// In case of a missing extension append ".log", otherwise leave name as is
			if (LogFilter.getExtension(outFile).isEmpty()) {
				outFile = new File(outFile.getAbsolutePath() + ".log");
			}
			// Do existence check and allow the user to cancel
			if (outFile.exists() && (JOptionPane.showConfirmDialog(this,
					msgOverwriteFile.getText(),
					menuContentSave.getText(),
					JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)) {
				return false;
			}
			lastSaved = outFile;
			String troubles = saveToFile(outFile);
			if (!troubles.isEmpty()) {
				JOptionPane.showMessageDialog(this, msgErrorFileSave.getText().replace("%", troubles),
						msgTitleError.getText(),
						JOptionPane.ERROR_MESSAGE);
			}
			else {
				done = true;
			}
		}
		return done;
	}

	/**
	 * Saves the the {@link #doc} content to file {@code targetFile}.
	 * @param targetFile - file object specifying the saving target. 
	 * @return in case of errors, a string describing them.
	 */
	private String saveToFile(File targetFile) {
		String problems = "";
		BTextfile outp = new BTextfile(targetFile.getAbsolutePath());
		try {
			outp.rewrite("UTF-8");
			int nleft = doc.getLength();
			Segment text = new Segment();
			int offs = 0;
			text.setPartialReturn(true);
			while (nleft > 0) {
				doc.getText(offs, nleft, text);
				outp.write(text.toString());
				nleft -= text.count;
				offs += text.count;
			}
		}
		catch (Exception ex) {
			problems = ex.toString();
		}
		finally {
			try {
				outp.close();
			} catch (IOException e) {
				problems += e.toString();
			}
		}
		return problems;
	}
	// END KGU#569 2018-08-09

	// START KGU#279 2016-10-11: Enh. #268 - allow to control the font size
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
		FontChooser fontChooser = new FontChooser(this);
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

	public void fontUp()
	{
		setFontSize(2*((getFontSize()+1)/2) + 2);
	}

	public void fontDown()
	{
		setFontSize(Math.max(MIN_FONT_SIZE, 2*(getFontSize()/2) - 2));
	}

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
	}
	// END KGU#279 2016-10-11

	// START KGU#503 2018-03-13: Enh. #519 - "zooming" via font size control with ctrl + mouse wheel
	@Override
	public void mouseWheelMoved(MouseWheelEvent mwEvt) {
		if (mwEvt.isControlDown()) {
			int rotation = mwEvt.getWheelRotation();
			if (Element.E_WHEEL_REVERSE_ZOOM) {
				rotation *= -1;
			}
			if (rotation >= 1) {
				mwEvt.consume();
				this.fontDown();
			}
			else if (rotation <= -1) {
				mwEvt.consume();
				this.fontUp();
			}
		}
	}
	// END KGU#503 2018-03-13

	// START KGU#569 2018-08-03: Enh. #577 - convenience methods for the check of log options
	/** @return true if the logging of calls (in/out) is enabled */
	public boolean logCalls() {
		return (menuContentLogCalls != null && menuContentLogCalls.isSelected());
	}
	
	/** @return true if the logging of meta information is enabled */
	public boolean logMeta() {
		return (menuContentLogMeta != null && menuContentLogMeta.isSelected());
	}
	// END KGU#569 2018-08-03

//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new OutputConsole().setVisible(true);
//            }
//        });
//    }

}

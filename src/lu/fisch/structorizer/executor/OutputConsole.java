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
 *      Kay Gürtzig     2016.04.08      First Issue (implementing enhancement request #137 / KGU#160)
 *      Kay Gürtzig     2016.04.12      Functionality accomplished
 *      Kay Gürtzig     2016.04.25      Scrolling to last line ensured
 *      Kay Gürtzig     2016.04.26      Converted to JTextPane in order to allow styled output
 *      Kay Gürtzig     2016.09.25      Bugfix #251 averting Nimbus disrespect of panel settings 
 *      Kay Gürtzig     2016.10.11      Enh. #268: Inheritance changed, font selecting opportunities added
 *      Kay Gürtzig     2016.10.17      Issue #268: Font setting source and target corrected (doc's default style)
 *      Kay Gürtzig     2016.11.22      Enh.#284: Font resizing accelerators modified (CTRL_DOWN_MASK added)
 *
 ******************************************************************************************************
 *
 *      Comment:  /
 *         
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import lu.fisch.structorizer.gui.FontChooser;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.locales.LangFrame;

@SuppressWarnings("serial")
// START KGU#279 2016-10-11: Enh. #268 - inheritance change was necessary to add a menu
public class OutputConsole extends LangFrame implements ActionListener {

	static private final int MIN_FONT_SIZE = 6;
	static private final Color[] colours = {Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY,
		Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};

	private JPanel panel;
	private JTextPane textPane;
	private StyledDocument doc = null;
	// START KGU#279 2016-10-11: Enh. #268 Font change opportunity
	public JMenu menu;
	public JMenuItem menuFont;
	public JMenuItem menuFontUp;
	public JMenuItem menuFontDown;
	// END KGU#279 2016-10-11
	
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
    	this.setIconImage(IconLoader.ico004.getImage());
    	
    	// START KGU#279 2016-10-11: Enh. #268: Font selection opportunity
    	menu = new JMenu("Properties");
    	menuFont = new JMenuItem("Font ...",IconLoader.ico023);
    	menuFont.addActionListener(this);
    	menuFontUp = new JMenuItem("Enlarge font", IconLoader.ico033);
    	menuFontUp.addActionListener(this);
    	menuFontUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK));
    	menuFontDown = new JMenuItem("Diminish font", IconLoader.ico034);
    	menuFontDown.addActionListener(this);
    	menuFontDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK));
    	
    	JMenuBar menuBar = new JMenuBar();
    	menuBar.add(menu);
    	menu.add(menuFont);
    	menu.add(menuFontUp);
    	menu.add(menuFontDown);
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
    }
    
    public void clear()
    {
    	try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
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
     * TODO Remark: Color attribute doesn't work in the current version
     * @param _text - a string
     * @param _colour - the text colour to use
     */
    public void write(String _text, Color _colour)
    {
    	try {
    		this.doc.insertString(doc.getLength(), _text, doc.getStyle(_colour.toString()));
    	} catch (BadLocationException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	// Scroll to end (if there is an easier way, I just didn't find it).
    	Rectangle rect = this.textPane.getBounds();
    	rect.y = rect.height - 1;
    	rect.height = 1;
    	this.textPane.scrollRectToVisible(rect);
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
     * TODO Remark: Color attribute doesn't work in the current version
     * @param _text - a string
     * @param _colour - the text colour to use
     */
    public void writeln(String _text, Color _colour)
    {
    	this.write(_text + "\n", _colour);
    }

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
		if (src == menuFont) {
			selectFont();
		}
		else if (src == menuFontUp) {
			fontUp();
		}
		else if (src == menuFontDown)
		{
			fontDown();
		}
	}
	// END KGU#279 2016-10-11
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OutputConsole().setVisible(true);
            }
        });
    }

}

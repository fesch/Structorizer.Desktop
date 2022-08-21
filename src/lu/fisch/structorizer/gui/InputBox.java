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
 *     Author: Bob Fisch
 *
 *     Description: This is the dialog that allows editing the properties of any element.
 *
 ******************************************************************************************************
 *
 *     Revision List
 *
 *      Author          Date        Description
 *      ------          ----        -----------
 *      Bob Fisch       2007.12.23  First Issue
 *      Kay Gürtzig     2015.10.12  A checkbox added for breakpoint control (KGU#43)
 *      Kay Gürtzig     2015.10.14  Element-class-specific language support (KGU#42)
 *      Kay Gürtzig     2015.10.25  Hook for subclassing added to method create() (KGU#3)
 *      Kay Gürtzig     2016.04.26  Issue #165: Focus transfer reset to Tab and Shift-Tab
 *      Kay Gürtzig     2016.07.14  Enh. #180: Initial focus dependent on switchTextComment mode (KGU#169)
 *      Kay Gürtzig     2016.08.02  Enh. #215: Breakpoint trigger counts partially implemented
 *      Kay Gürtzig     2016.09.13  Bugfix #241: Obsolete mechanisms removed (remnants of KGU#42)
 *      Kay Gürtzig     2016.09.22  Bugfix #241 revised by help of a LangDialog API modification
 *      Kay Gürtzig     2016.10.13  Enh. #270: New checkbox chkDisabled
 *      Kay Gürtzig     2016.11.02  Issue #81: Workaround for lacking DPI awareness
 *      Kay Gürtzig     2016.11.09  Issue #81: Scale factor no longer rounded but ensured to be >= 1
 *      Kay Gürtzig     2016.11.11  Issue #81: DPI-awareness workaround for checkboxes
 *      Kay Gürtzig     2016.11.21  Issue #284: Opportunity to scale up/down the TextField fonts by Ctrl-Numpad+/-
 *      Kay Gürtzig     2017.01.07  Bugfix #330 (issue #81): checkbox scaling suppressed for "Nimbus" l&f
 *      Kay Gürtzig     2017.01.09  Bugfix #330 (issue #81): Basic scaling outsourced to class GUIScaler
 *      Kay Gürtzig     2017.03.14  Enh. #372: Additional hook for subclass InputBoxRoot.
 *      Kay Gürtzig     2017.10.06  Enh. #430: The scaled TextField font size (#284) is now kept during the session
 *      Kay Gürtzig     2020-10-15  Bugfix #885 Focus rule was flawed (ignored suppression of switch text/comments mode)
 *      Kay Gürtzig     2021-01-22  Enh. #714 New checkbox for TRY elements
 *      Kay Gürtzig     2021-01-04  Enh. #914 UndoManagers added to text and comment field.
 *      Kay Gürtzig     2021-01-25  Enh. #915 New supporting methods for JTables
 *      Kay Gürtzig     2021-01-26  Issue #400: Some Components had not reacted to Esc and Shift/Ctrl-Enter
 *      Kay Gürtzig     2021-02-10  Bugfix #931: Font resizing: JTextArea font was spread to other components,
 *                                  JTables are to be involved on init already, scaleFactor to be considered
 *      Kay Gürtzig     2022-08-18  Enh. #1066: First draft of a simple text auto-completion mechanism
 *      Kay Gürtzig     2022-08-21  Enh. #1066: New text suggestion approach with pulldown (based on LogicBig
 *                                  SuggestionDropDownDecorator) and keyword inclusion
 *
 ******************************************************************************************************
 *
 *     Comment:	/
 *
 ******************************************************************************************************
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import com.logicbig.uicommon.SuggestionClient;
import com.logicbig.uicommon.SuggestionDropDownDecorator;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.StringList;

@SuppressWarnings("serial")
public class InputBox extends LangDialog implements ActionListener, KeyListener {
	
    // START KGU#428 2017-10-06: Issue #430 Modified editor font size should be maintained
    /** font size for the text fields, 0 = default, may be overridden by keys or from ini */
    public static float FONT_SIZE = 0;	// Default value
    // END KGU#428 2017-10-06
    // START KGU#1057 2022-08-19: Enh. #1066 Auto-text-completion with dropdown
    public static int MIN_SUGG_PREFIX = 3;
    private static final HashMap<String, StringList> KEYWORD_SUGGESTIONS = new HashMap<String, StringList>();
    static {
        KEYWORD_SUGGESTIONS.put("Instruction", StringList.explode("input,output", ","));
        KEYWORD_SUGGESTIONS.put("Jump", StringList.explode("preReturn,preLeave,preExit,preThrow", ","));
        KEYWORD_SUGGESTIONS.put("Alternative", StringList.getNew("preAlt"));
        KEYWORD_SUGGESTIONS.put("While", StringList.getNew("preWhile"));
        KEYWORD_SUGGESTIONS.put("Repeat", StringList.getNew("preRepeat"));
        KEYWORD_SUGGESTIONS.put("For", StringList.explode("preFor,preForIn", ","));
    }
    // END KGU#1057 2022-08-19
    
    protected static int[] PREFERRED_SIZE = new int[] {500, 400};

    public boolean OK = false;

    // KGU#3 2015-11-03: Some of the controls had to be made public in order to allow language support for subclasses 
    // Buttons
    public JButton btnOK = new JButton("OK");    
    public JButton btnCancel = new JButton("Cancel");

    // Labels
    public JLabel lblText = new JLabel("Please enter a text");
    public JLabel lblComment = new JLabel("Comment");

    // Textarea
    public JTextArea txtText = new JTextArea();
    public JTextArea txtComment = new JTextArea();
    // START KGU#915 2021-01-24: Enh. #914
    private UndoManager umText = new UndoManager();
    private UndoManager umComment = new UndoManager();
    // END KGU#915 2021-01-24
    
    // START KGU#1057 2022-08-21: Enh. #1066
    public JPanel pnlSuggest = new JPanel();
    private JLabel lblSuggest = new JLabel("Suggestion threshold");
    private JSpinner spnSuggest = new JSpinner();
    // END KGU#1057 2022-08-21

    // Scrollpanes
    protected JScrollPane scrText = new JScrollPane(txtText);
    protected JScrollPane scrComment = new JScrollPane(txtComment);

    // Checkboxes
    // START KGU#277 2016-10-13: #270
    public JCheckBox chkDisabled = new JCheckBox("Execution and export disabled");
    // END KGU#277 2016-10-13
    // START KGU#695 2021-01-22: Enh. #714 New checkbox to force the Finally block in Try element
    public JCheckBox chkShowFinally = new JCheckBox("Show the FINALLY block even if empty");
    // END KGU#695 2021-01-22
    // START KGU#43 2015-10-12: Additional possibility to control the breakpoint setting
    public JCheckBox chkBreakpoint = new JCheckBox("Breakpoint");
    // END KGU#43 2015-10-12
    // START KGU#213 2016-08-01: Enh. #215
    //private int prevBreakTrigger = 0;
    public JLabel lblBreakTriggerText = new JLabel("Break at execution count: %");
    public LangTextHolder lblBreakTrigger = new LangTextHolder("0");
    //public JTextField txtBreakTrigger = new JTextField();
    // END KGU#213 2016-08-01
    
    // START KGU#294 2016-11-22: Enh. #284
    protected final JButton btnFontUp = new JButton(IconLoader.getIcon(33)); 
    protected final JButton btnFontDown = new JButton(IconLoader.getIcon(34));
    // END KGI#294 2016-11-22

    // START KGU 2015-10-14: Additional information for data-specific title translation
    public String elementType = new String();	// The class name of the element type to be edited here
    public boolean forInsertion = false;		// If this dialog is used to setup a new element (in contrast to updating an existing element)
    // END KGU 2015-10-14

    // START KGU#1057 2022-08-19: Enh. #1066 Auto-text-completion with dropdown
    /**
     * A case-insensitively sorted list of suggestable words (e.g. variable names etc.)
     */
    public ArrayList<String> words = null;
    public HashMap<String, TypeMapEntry> typeMap = null;
    
    /**
     * Specific text suggestion client for the LogicBig.com SuggestionDropDownDecorator
     * Cares for sensible name insertion proposals (variables, routines, components).
     * 
     * @author Kay Gürtzig
     */
    private class InputSuggestionClient implements SuggestionClient<JTextComponent> {
        
        private Logger logger = Logger.getLogger(InputSuggestionClient.class.getName());

        @Override
        public Point getPopupLocation(JTextComponent invoker) {
            int caretPosition = invoker.getCaretPosition();
            try {
                Rectangle2D rectangle2D = invoker.modelToView2D(caretPosition);
                return new Point((int) rectangle2D.getX(), (int) (rectangle2D.getY() + rectangle2D.getHeight()));
            } catch (BadLocationException ex) {
                logger.log(Level.FINE, ex.toString());
            }
            return null;
        }

        @Override
        public void setSelectedText(JTextComponent tc, String selectedValue) {
            int cp = tc.getCaretPosition();
            int posOpen = -1;
            if (selectedValue.endsWith(")") && (posOpen = selectedValue.lastIndexOf("(")) >= 0) {
                int nArgs = Integer.parseInt(selectedValue.substring(posOpen+1, selectedValue.length()-1));
                selectedValue = selectedValue.substring(0, posOpen+1)
                        + (nArgs > 0 ? "?" + ",?".repeat(nArgs-1) : "") + ")";
            }
            try {
                if (cp == 0 || tc.getText(cp - 1, 1).trim().isEmpty()) {
                    tc.getDocument().insertString(cp, selectedValue, null);
                } else {
                    int previousWordIndex = this.findWordStart(tc, cp - 1);
                    String text = tc.getText(previousWordIndex, cp - previousWordIndex);
                    if (selectedValue.startsWith(text)) {
                        tc.getDocument().insertString(cp, selectedValue.substring(text.length()), null);
                    } else if (selectedValue.toLowerCase().startsWith(text.toLowerCase())) {
                        tc.setSelectionStart(previousWordIndex);
                        tc.setSelectionEnd(cp);
                        tc.replaceSelection(selectedValue);
                    } else {
                        // In case of a mismatch just append the selectedValue (???)
                        tc.getDocument().insertString(cp, selectedValue, null);
                        previousWordIndex = cp;
                    }
                    if (posOpen > 0 && selectedValue.contains("?")) {
                        // Routine with at least on argument - select the first '?'
                        cp = previousWordIndex + posOpen + 1;
                        tc.setCaretPosition(cp+1);
                        tc.moveCaretPosition(cp);
                    }
                }
            } catch (BadLocationException ex) {
                logger.log(Level.FINE, ex.toString());
            }
        }

        @Override
        public List<String> getSuggestions(JTextComponent tc) {
            if (words == null || MIN_SUGG_PREFIX <= 0) {
                return null;
            }
            int pos = tc.getCaretPosition();
            int w = this.findWordStart(tc, pos - 1);
            String content = null;
            try {
                content = tc.getText(0, pos);
            } catch (BadLocationException ex) {
                logger.log(Level.FINE, ex.toString());
            }
            
            ArrayList<String> proposals = words;
            // Now check whether a dot precedes - in which case we have to provide component names
            if (w > 1 && pos >= w && content.charAt(w-1) == '.' && typeMap != null) {
                if ((proposals = retrieveComponentNames(content.substring(0, w-1))) == null) {
                    // No record information available -> don't provide suggestions
                    return null;
                }
            }
            else if (pos - w < MIN_SUGG_PREFIX) {
                // Too few chars
                return null;
            }
            ArrayList<String> suggestions = new ArrayList<String>();
            String prefix = content.substring(w);
            if ((w == 0 || content.charAt(w-1) == '\n')) {
                StringList keys = KEYWORD_SUGGESTIONS.get(elementType);
                if (keys != null) {
                    for (int i = 0; i < keys.count(); i++) {
                        String keyword = CodeParser.getKeyword(keys.get(i));
                        if (keyword != null
                                && keyword.toLowerCase().startsWith(prefix.toLowerCase())) {
                            suggestions.add(keyword);
                        }
                    }
                }
            }
            int n = Collections.binarySearch(proposals, prefix, String.CASE_INSENSITIVE_ORDER);
            if (n < 0) {
                n = -n - 1;
            }
            prefix = prefix.toLowerCase();
            String match = null;
            while (n < proposals.size()
                    && (match = proposals.get(n)).toLowerCase().startsWith(prefix)) {
                suggestions.add(match);
                n++;
            }
            return suggestions;
        }
        
        /**
         * Goes backwards through the text preceding position {@code pos}, searching
         * for a character that is not part of an identifier
         * 
         * @param invoker - the {@link JTextComponent} to operate within
         * @param pos - current {@code invoker} position WITHIN the supposed identifier
         * @return the start position of the identifier
         */
        private int findWordStart(JTextComponent invoker, int pos) {
            String content = "";
            try {
                content = invoker.getText(0, pos + 1);
            } catch (BadLocationException ex) {
                logger.log(Level.FINE, ex.toString());
                return pos;
            }
            // Find where the word starts
            int w;
            for (w = pos; w >= 0; w--) {
                if (!Character.isUnicodeIdentifierPart(content.charAt(w))) {
                    break;
                }
            }
            return w + 1;
        }
        
        /**
         * Analyses the text {@code content} preceding a dot in backwards direction for
         * record structure information.<br/>
         * if the pretext describes an object with record structure then returns the sorted
         * list of the component names, otherwise the result will {@code null}.
         * 
         * @param _content - the text content up to (but not including) the triggering dot
         * @return either the new proposals (component names in the latter case) or {@code null}
         */
        private ArrayList<String> retrieveComponentNames(String _content) {
            ArrayList<String> proposals = null;
            StringList lines = StringList.explode(_content, "\n");
            String prevLine = null;
            while (lines.count() > 1 && (prevLine = lines.get(lines.count()-2)).endsWith("\\")) {
                lines.set(lines.count()-2,
                        prevLine.substring(0, prevLine.length()-1) + lines.get(lines.count()-1));
                lines.remove(lines.count()-1);
            }
            StringList tokens = Element.splitLexically(lines.get(lines.count()-1), true);
            proposals = Element.retrieveComponentNames(tokens, typeMap, null);
            if (proposals != null) {
                Collections.sort(proposals, String.CASE_INSENSITIVE_ORDER);
            }
            return proposals;
        }
    }
    // END KGU#1057 2022-08-19
    
    // START KGU#294 2016-11-21: Issue #284
    // Components with fonts to be scaled independently 
    protected Vector<JComponent> scalableComponents = new Vector<JComponent>();
    // END KGU#294 2016-11-21
    
    // START KGU#287 2016-11-02: Enh. #180, Issue #81 (DPI awareness workaround)
    protected void setPreferredSize(double scaleFactor) {
        setSize((int)(PREFERRED_SIZE[0] * scaleFactor), (int)(PREFERRED_SIZE[1] * scaleFactor));
    }
    // END KGU#287 2016-11-02
    
    private void create() {
        // set window title
        setTitle("Content");
        // set layout (OS default)
        setLayout(null);
            // START KGU#169 2016-07-14: Enh. #180: Now done after pack() and subclassable
        // set windows size
        //setSize(500, 400);
        // END KGU#169 2016-07-14
        
        // START KGU#287 2016-11-02: Issue #81 (DPI awareness workaround)
        double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
        // END KGU#287 2016-11-11

        // show form
        setVisible(false);
        // set action to perform if closed
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // set listeners
        // START KGU#294 2016-11-22: Issue #284
        btnFontUp.addActionListener(this);
        btnFontDown.addActionListener(this);
        // END KGU#294 2016-11-22
        btnOK.addActionListener(this);
        btnCancel.addActionListener(this);
        
        btnOK.addKeyListener(this);
        btnCancel.addKeyListener(this);
        // START KGU#393 2021-01-26: Issue #400
        btnFontUp.addKeyListener(this);
        btnFontDown.addKeyListener(this);
        chkDisabled.addKeyListener(this);
        chkBreakpoint.addKeyListener(this);
        chkShowFinally.addKeyListener(this);
        // END KGU#393 2021-01-26
        txtText.addKeyListener(this);
        // START KGU#186 2016-04-26: Issue #163 - tab isn't really needed within the text
        txtText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        txtText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        // END KGU#186 2016-04-26
        txtComment.addKeyListener(this);
        // START KGU#186 2016-04-26: Issue #163 - tab isn't really needed within the text
        txtComment.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        txtComment.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        // END KGU#186 2016-04-26
        // START KGU#213 2016-08-01: Enh. #215
        //txtBreakTrigger.addKeyListener(this);
        // END KGU#213 2016-08-01
        addKeyListener(this);
        // START KGU#408 2021-02-23: Enh. #410 Introduced on behalf of InputBoxRoot.txtNamespace
        // Suppress input validation
        btnCancel.setVerifyInputWhenFocusTarget(false);
        // END KGU#408 2021-03-23
        
        int border = (int)(4 * scaleFactor);
        Border emptyBorder = BorderFactory.createEmptyBorder(border, border, border, border);
        txtText.setBorder(emptyBorder);
        txtComment.setBorder(emptyBorder);
        
        // START KGU#915 2021-01-24: Enh. #914
        Document docText = txtText.getDocument();
        Document docComment = txtComment.getDocument();
        docText.addUndoableEditListener(umText);
        docComment.addUndoableEditListener(umComment);
        // END KGU#915 2021-01-24
        
        // START KGU#1057 2022-08-19: Enh. #1066 first auto-completion approach
        SuggestionDropDownDecorator.decorate(txtText, new InputSuggestionClient());
        spnSuggest.setModel(new SpinnerNumberModel(0, 0, 5, 1));
        // END KGU#1057 2022-08-19
        
        // START KGU#294 2016-11-21: Issue #284
        scalableComponents.addElement(txtText);
        scalableComponents.addElement(txtComment);
        // END KGU#294 2016-11-21
        
        JPanel pnPanel0 = new JPanel();
        GridBagLayout gbPanel0 = new GridBagLayout();
        GridBagConstraints gbcPanel0 = new GridBagConstraints();
        border = (int)(10 * scaleFactor);
        gbcPanel0.insets = new Insets(border, border, 0, border);
        pnPanel0.setLayout(gbPanel0);

        // START KGU#3 2015-10-24: Open opportunities for subclasses
        createPanelTop(pnPanel0, gbPanel0, gbcPanel0);
        
        //// START KGU#428 2017-10-06: Enh. #430 - 2021-02-10 now done after GUI scaling
        //if (FONT_SIZE > 0) {
        //    for (JComponent comp: scalableComponents) {
        //        Font font = comp.getFont();
        //        comp.setFont(font.deriveFont(FONT_SIZE));
        //    }
        //}
        // END KGU#428 2017-10-06
 
        JPanel pnPanel1 = new JPanel();
        GridBagLayout gbPanel1 = new GridBagLayout();
        GridBagConstraints gbcPanel1 = new GridBagConstraints();
        gbcPanel1.insets = new Insets(border, border, 0, border);
        pnPanel1.setLayout(gbPanel1);
        // END KGU#3 2015-10-24
        
        gbcPanel1.gridx = 1;
        gbcPanel1.gridy = 2;
        gbcPanel1.gridwidth = 18;
        gbcPanel1.gridheight = 7;
        gbcPanel1.fill = GridBagConstraints.BOTH;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 1;
        gbcPanel1.anchor = GridBagConstraints.NORTH;
        gbPanel1.setConstraints(scrText, gbcPanel1);
        pnPanel1.add(scrText);
        
        gbcPanel1.gridx = 1;
        gbcPanel1.gridy = 12;
        gbcPanel1.gridwidth = 18;
        gbcPanel1.gridheight = 4;
        gbcPanel1.fill = GridBagConstraints.BOTH;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 1;
        gbcPanel1.anchor = GridBagConstraints.NORTH;
        gbPanel1.setConstraints(scrComment, gbcPanel1);
        pnPanel1.add(scrComment);

        gbcPanel1.gridx = 1;
        gbcPanel1.gridy = 10;
        gbcPanel1.gridwidth = 18;
        gbcPanel1.gridheight = 1;
        gbcPanel1.fill = GridBagConstraints.BOTH;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 0;
        gbcPanel1.anchor = GridBagConstraints.NORTH;
        gbPanel1.setConstraints(lblComment, gbcPanel1);
        pnPanel1.add(lblComment);

        // START KGU#363 2017-03-13: Enh. #372 new hook for InputBoxRoot
        gbcPanel1.gridy = 17;
        gbcPanel1.gridx = this.createExtrasBottom(pnPanel1, gbcPanel1, 12);
        // END KGU#363 2017-03-13
        
        // START KGU#294 2016-11-22: Issue #284 - visible font-resizing buttons
        JPanel fontPanel = new JPanel();
        fontPanel.setLayout(new GridLayout(0,2));
        fontPanel.add(btnFontUp);
        fontPanel.add(btnFontDown);
        
        //gbcPanel1.gridx = 12;
        gbcPanel1.gridy = 17;
        gbcPanel1.gridwidth = 7;
        gbcPanel1.gridheight = 1;
        gbcPanel1.fill = GridBagConstraints.WEST;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 0;
        gbcPanel1.anchor = GridBagConstraints.EAST;
        gbPanel1.setConstraints(fontPanel, gbcPanel1);
        pnPanel1.add(fontPanel);
        // END KGU#294 2016-11-22

        // START KGU 2021-02-22: Enh. #714 - special checkbox for Try elements
        gbcPanel1.gridx = 1;
        gbcPanel1.gridy++;
        gbcPanel1.gridwidth = 7;
        gbcPanel1.gridheight = 1;
        gbcPanel1.fill = GridBagConstraints.BOTH;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 0;
        gbcPanel1.anchor = GridBagConstraints.NORTH;
        gbPanel1.setConstraints(chkShowFinally, gbcPanel1);
        pnPanel1.add(chkShowFinally);
        chkShowFinally.setVisible(false);	// Usually not visible
        // END KGU 2021-02-22
        
        gbcPanel1.gridx = 1;
        // START KGU#277 2016-10-13: Enh. #270
        //gbcPanel1.gridy = 17;
        gbcPanel1.gridy++;
        // END KGU#277 2016-10-13
        gbcPanel1.gridwidth = 7;
        gbcPanel1.gridheight = 1;
        gbcPanel1.fill = GridBagConstraints.BOTH;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 0;
        gbcPanel1.anchor = GridBagConstraints.NORTH;
        gbPanel1.setConstraints(chkBreakpoint, gbcPanel1);
        pnPanel1.add(chkBreakpoint);

        // START KGU#213 2016-08-01: Enh. #215 - conditional breakpoints
        gbcPanel1.gridx = 12;
        // START KGU#277 2016-10-13: Enh. #270
        //gbcPanel1.gridy = 17;
        // END KGU#277 2016-10-13
        gbcPanel1.gridwidth = 7;
        gbcPanel1.gridheight = 1;
        gbcPanel1.fill = GridBagConstraints.BOTH;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 0;
        gbcPanel1.anchor = GridBagConstraints.NORTH;
        gbPanel1.setConstraints(lblBreakTriggerText, gbcPanel1);
        pnPanel1.add(lblBreakTriggerText);
        // END KGU#213 2106-08-01

//        // START KGU#213/KGU#245 2016-09-13: Enh. #215 + bugfix #241
//        gbcPanel1.gridx = 13;
//        gbcPanel1.gridy = 17;
//        gbcPanel1.gridwidth = 1;
//        gbcPanel1.gridheight = 1;
//        gbcPanel1.fill = GridBagConstraints.RELATIVE;
//        gbcPanel1.weightx = 1;
//        gbcPanel1.weighty = 0;
//        gbcPanel1.anchor = GridBagConstraints.CENTER;
//        gbPanel1.setConstraints( lblBreakTrigger, gbcPanel1 );
//        pnPanel1.add(lblBreakTrigger);
//        // END KGU#246 2106-09-13
        gbcPanel1.insets = new Insets(border, border, border, border);

        //createExitButtons(gridbase)
        gbcPanel1.gridx = 1;
        // START KGU#277 2016-10-13: Enh. #270
        //gbcPanel1.gridy = 18;
        gbcPanel1.gridy++;
        // END KGU#277 2016-10-13
        gbcPanel1.gridwidth = 7;
        gbcPanel1.gridheight = 1;
        gbcPanel1.fill = GridBagConstraints.BOTH;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 0;
        gbcPanel1.anchor = GridBagConstraints.NORTH;
        gbPanel1.setConstraints(btnCancel, gbcPanel1);
        pnPanel1.add(btnCancel);

        // START KGU#3 2015-10-31: The new gridx causes no difference here but fits better for InputBoxFor
        gbcPanel1.gridx = 11;
            //gbcPanel1.gridx = 8;
        // END KGU#3 2015-10-31
        // START KGU#277 2016-10-13: Enh. #270
        //gbcPanel1.gridy = 18;
        // END KGU#277 2016-10-13
        // START KGU#3 2015-10-31: The new gridwidth causes no difference here but fits better for InputBoxFor
        gbcPanel1.gridwidth = 8;
        //gbcPanel1.gridwidth = GridBagConstraints.REMAINDER;
        // END KGU#3 2015-10-31
        gbcPanel1.gridheight = 1;
        gbcPanel1.fill = GridBagConstraints.BOTH;
        gbcPanel1.weightx = 1;
        gbcPanel1.weighty = 0;
        gbcPanel1.anchor = GridBagConstraints.NORTH;
        gbPanel1.setConstraints(btnOK, gbcPanel1);
        pnPanel1.add(btnOK);
        
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(pnPanel0, BorderLayout.NORTH);
        container.add(pnPanel1, BorderLayout.CENTER);

        // START KGU#287 2017-01-09: Bugfix #330  - scaling stuff outsourced to class GUIScaler
        GUIScaler.rescaleComponents(this);
        // END KGU#287 2017-01-09

        // START KGU#428 2017-10-06: Enh. #430
        // START KGU#916/KGU#931 2021-02-10 moved hitherto and replaced by method
        //if (FONT_SIZE > 0) {
        //    for (JComponent comp: scalableComponents) {
        //        Font font = comp.getFont();
        //        comp.setFont(font.deriveFont(FONT_SIZE));
        //    }
        //}
        applyFontScale();
        // END KGU#916/KGU#931 2021-02-10
        // END KGU#428 2017-10-06

        // START KGU#91+KGU#169 2016-07-14: Enh. #180 (also see #39 and #142)
        this.pack();	// This makes focus control possible but must precede the size setting
        setPreferredSize(scaleFactor);
        // START KGU#887 2020-12-15: Bugfix #885 Don't put the focus to comment if TC mode is suppressed
        //if (Element.E_TOGGLETC) {
        if (Element.isSwitchTextCommentMode()) {
        // END KGU#887 2020-12-15
            txtComment.requestFocusInWindow();
        } else {
            txtText.requestFocusInWindow();
        }
        // END KGU#91+KGU#169 2016-07-14
    }

	// START KGU#3 2015-10-24: Hook for subclasses
    /**
     * Subclassable method to add specific stuff to the Panel top
     *
     * @param _panel the panel to be enhanced
     * @param _gb a usable GridBagLayout object
     * @param _gbc the layout constraints
     * @return number of lines (y units) inserted
     */
    protected int createPanelTop(JPanel _panel, GridBagLayout _gb, GridBagConstraints _gbc) {
        _gbc.gridx = 1;
        _gbc.gridy = 1;
        _gbc.gridwidth = 1;
        _gbc.gridheight = 1;
        _gbc.fill = GridBagConstraints.BOTH;
        _gbc.weightx = 1;
        _gbc.weighty = 0;
        _gbc.anchor = GridBagConstraints.NORTH;
        //_gb.setConstraints(lblText, _gbc);
        _panel.add(lblText, _gbc);
        
        // START KGU#1057 2022-08-21: Enh. #1066
        pnlSuggest.add(lblSuggest);
        pnlSuggest.add(spnSuggest);
        _gbc.gridx = 2;
        _gbc.weightx = 0;
        _gbc.fill = GridBagConstraints.NONE;
        _panel.add(pnlSuggest, _gbc);
        // Disable it by default
        pnlSuggest.setVisible(false);
        spnSuggest.setValue(MIN_SUGG_PREFIX);
        ((JSpinner.DefaultEditor)spnSuggest.getEditor()).getTextField().addKeyListener(this);
        spnSuggest.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ev) {
                InputBox.MIN_SUGG_PREFIX = (Integer)spnSuggest.getValue();
            }});
        lblSuggest.setToolTipText("Minimum number of typed characters to instigate word suggestions (0 to switch assistance off).");
        // END KGU#1057 2022-08-21
        // Return the number of used grid lines such that the calling method may go on there
        return 1;
    }
    // END KGU#3 2015-10-24

    /**
     * Allows a subclass to add additional controls to the left of the font button
     * panel. Must return the number of columns created.
     * @param _panel - the panel where the extra controls may be added
     * @param _gbc - the layout constraints
     * @param _maxGridX - the gridX value InputBox will claim (we must stay left of it)
     * @return the next unused gridx value
     */
    protected int createExtrasBottom(JPanel _panel, GridBagConstraints _gbc, int _maxGridX) {
        // START KGU#277 2016-10-13: Enh. #270
        _gbc.gridx = 1;
        _gbc.gridwidth = 7;
        _gbc.gridheight = 1;
        _gbc.fill = GridBagConstraints.BOTH;
        _gbc.weightx = 1;
        _gbc.weighty = 0;
        _gbc.anchor = GridBagConstraints.NORTH;
        ((GridBagLayout)_panel.getLayout()).setConstraints(chkDisabled, _gbc);
        _panel.add(chkDisabled);
        // END KGU#277 2016-10-13
        // START KGU#393 2021-01-26: Issue #400
        chkDisabled.addKeyListener(this);
        // END KGU#393 2021-01-26
        
        return _gbc.gridx + _gbc.gridwidth;
    }

    // listen to actions
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        
        if (source == btnOK) {
            OK = true;
        } else if (source == btnCancel) {
            OK = false;
        }
        // START KGU#294 2016-11-22: Issue #284
        else if (source == btnFontUp || source == btnFontDown) {
            fontControl(source == btnFontUp);
            return;
        }
        // END KGU#294 2016-11-22
        setVisible(false);
    }
    
    @Override
    public void keyTyped(KeyEvent kevt) {
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) {
            OK = false;
            setVisible(false);
        } else if (keyCode == KeyEvent.VK_ENTER && (e.isShiftDown() || e.isControlDown())) {
            OK = true;
            setVisible(false);
        }
        // START KGU#294 2016-11-21: Issue #284 - Opportunity to modify JTextField font size
        else if ((keyCode == KeyEvent.VK_ADD || keyCode == KeyEvent.VK_SUBTRACT) && (e.isControlDown())) {
            fontControl(keyCode == KeyEvent.VK_ADD);
        }
        // END KGU#294 2016-11-21
        // START KGU#915 2021-01-24: Enh. #914
        else if (keyCode == KeyEvent.VK_Z && e.isControlDown() && !e.isShiftDown()) {
            Object src = e.getSource();
            try {
                if (src == txtText && umText.canUndo()) {
                    umText.undo();
                }
                else if (src == txtComment && umComment.canUndo()) {
                    umComment.undo();
                }
            }
            catch (CannotUndoException ex) {}
        }
        else if (keyCode == KeyEvent.VK_Y && e.isControlDown() && !e.isShiftDown()
              || keyCode == KeyEvent.VK_Z && e.isControlDown() && e.isShiftDown()) {
            Object src = e.getSource();
            try {
                if (src == txtText && umText.canRedo()) {
                    umText.redo();
                }
                else if (src == txtComment && umComment.canUndo()) {
                    umComment.redo();
                }
            }
            catch (CannotRedoException ex) {}
        }
        // END KGU#915 2021-01-24
    }
    
    @Override
    public void keyReleased(KeyEvent ke) {
//    	// START KGU#213 2016-08-01: Enh. #215
//    	Object source = ke.getSource();
//    	if (source == txtBreakTrigger)
//    	{
//    		int cnt = 0;
//    		String triggerStr = txtBreakTrigger.getText();
//    		if (triggerStr != null)
//    		{
//    			try{
//    				cnt = Integer.parseUnsignedInt(triggerStr);
//    				this.prevBreakTrigger = cnt;
//    			}
//    			catch (Exception ex)
//    			{
//    				txtBreakTrigger.setText(Integer.toString(this.prevBreakTrigger));
//    			}
//    		}
//    	}
    }

    // constructors
    public InputBox(Frame owner, boolean modal) {
        super(owner, modal);
        setPacking(false);
        create();
    }

    public String getInsertionType()
    {
        return (forInsertion ? "insert" : "update");
    }

    // START KGU#61 2016-03-21: Enh. #84 - Addition to facilitate specific handling
    /**
     * May check and ensure consistency between inserted data, control behaviour
     * etc.
     */
    public void checkConsistency() {
    	// Basic implementation doesn't do anything
    }
    // END KGU#61 2016-03-21

    // START KGU#246 2016-09-21: Enhancement to implement issues like bugfix #241
    /**
     * This method is called on opening after setLocale and before re-packing.
     * Replaces markers in translated texts.
     */
    @Override
    protected void adjustLangDependentComponents()
    {
        this.lblBreakTriggerText.setText(this.lblBreakTriggerText.getText().replace("%", lblBreakTrigger.getText()));
    }
    // END KGU#246 2016-09-21
    
    // START KGU#294 2016-11-22: Issue #284
    public void fontControl(boolean up)
    {
        // START KGU#931 2021-02-10: Issue #931
        if (FONT_SIZE == 0) {
            Font font = txtText.getFont();
            FONT_SIZE = font.getSize();	// Make sure FONT_SIZE is set
        }
        float increment = 2.0f;
        if (!up) {
            increment = FONT_SIZE > 8 ? -2.0f : 0.0f;
        }
        // START KGU#428 2017-10-06: Enh. #430
        //Font newFont = font.deriveFont(font.getSize()+increment);
        FONT_SIZE += increment;
        // START KGU#931 2021-02-10: Bugfix #931 Don't spread the JTextArea font!
        //Font newFont = font.deriveFont(FONT_SIZE);
        // END KGU#931 2021-02-10
        // END KGU#428 2017-10-06
        applyFontScale();	// Outsourced on occasion of #931
        this.revalidate();
    }
    // END KGU#294 2016-11-22

    // START KGU#931 2021-02-10: Issue #931 + code revision
    /**
     * Applies the configured {@link #FONT_SIZE} to all {@link #scalableComponents}
     */
    private void applyFontScale() {
        if (FONT_SIZE < 6) {
            return;
        }
        // START KGU#931 2021-02-10 Bugfix #931 Now we will consider the scaleFactor, too
        //Font newFont = txtText.getFont().deriveFont(FONT_SIZE);
        float scaleFactor = Float.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
        float fontSize = FONT_SIZE * scaleFactor;
        // END KGU#931 2021-02-10
        for (JComponent comp: scalableComponents) {
            // START KGU#931 2021-02-10: Bugfix #931 Don't spread the JTextArea font!
            //comp.setFont(newFont);
            Font newFont = comp.getFont().deriveFont(fontSize);
            comp.setFont(newFont);
            // END KGU#9321 2021-02-10
            // START KGU#916 2021-01-25: Enh. #915
            if (comp instanceof JTable) {
                ((JTable)comp).setRowHeight(((JTable)comp).getFontMetrics(newFont).getHeight());
            }
            // END KGU#915 2021-01-25
        }
    }
    // END KGU#931 2021-02-10

    // START KGU#916 2021-01-24: Enh. #915 Support for tables in subclasses
    /**
     * Determines the required maximum rendering width for column {@code _colNo} of
     * {@link JTable} {@code _table} and fixes it as maximum and preferred width 
     * @param _table - the {@link JTable} to be optimized
     * @param _colNo - index of the interesting column (typically 0)
     * @return the determined width 
     */
    protected static int optimizeColumnWidth(JTable _table, int _colNo)
    {
        int optWidth = 5;
        for (int row = 0; row < _table.getRowCount(); row++) {
            TableCellRenderer renderer = _table.getCellRenderer(row, _colNo);
            Component comp = _table.prepareRenderer(renderer, row, _colNo);
            optWidth = Math.max(comp.getPreferredSize().width, optWidth);
        }
        return optWidth;
    }
    
    /**
     * Determines the required dimension of the given table {@code _table} in
     * order to display all rows and columns. If {@code _optimizeCol0Width} is
     * {@code true} then will adjust the width of the first column.
     * @param _table - The {@link JTable} to be analysed
     * @param _optimizeCol0width - whether the first column width is to be
     *      optimized for the contained values.
     * @return the required size as {@link Dimension}
     */
    protected Dimension getRequiredTableSize(JTable _table, boolean _optimizeCol0width)
    {
        int width0 = optimizeColumnWidth(_table, 0);
        int width1 = optimizeColumnWidth(_table, 1);
        int height = _table.getRowHeight() * _table.getRowCount();
        if (_optimizeCol0width) {
            _table.getColumnModel().getColumn(0).setMaxWidth(width0 + 3);
            _table.getColumnModel().getColumn(0).setPreferredWidth(width0 + 3);
        }
        return new Dimension(width0 + width1 + 10, height);
    }


    // END KGU#916 2021-01-24

}

/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

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
 *      Description:    Find & Replace dialog for Structorizer (according to Enh. Requ. #415)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017-05-30      First Issue (#415)
 *      Kay Gürtzig     2017-06-13      Pattern combo boxes with history
 *      Kay Gürtzig     2017-06-17      JTree for multi-Root retrieval
 *      Kay Gürtzig     2017-06-19      Preview size problem solved, inner-element navigation, matching flaws fixed
 *      Kay Gürtzig     2017-06-22      NullPointerException on replacing due to cleared currentNode fixed
 *      Kay Gürtzig     2017-09-12      Combobox fixes: cursor up/down in pulldown list and Esc key without pulldown
 *      Kay Gürtzig     2017-10-09      Internal consistency of For elements on replacement ensured (KGU#431)
 *      Kay Gürtzig     2017-11-03      Bugfix #448: endless self-replacement averted, performance improved
 *                                      (minimum-invasive revision)
 *      Kay Gürtzig     2018-01-22      Enh. #490: The dialog now works on the controller alias texts if enabled
 *      Kay Gürtzig     2018-04-05      Issue #463: Plain console messages replaced by logging mechanism
 *      Kay Gürtzig     2018-07-02      Bugfix KGU#540 - An element filter change didn't reset the result
 *      Kay Gürtzig     2018-11-21      Bugfix #448: Apparently forgotten part of the fix accomplished
 *      Kay Gürtzig     2018-11-22      Bugfix #637: ArrayIndexOutOfBoundsException in replacePattern(...)
 *      Kay Gürtzig     2019-02-07      Workaround for truncation of node texts with scale factors > 1.0 (KGU#647)
 *      Kay Gürtzig     2019-03-17      Enh. #56: Try elements integrated, element panel layout revised
 *      Kay Gürtzig     2019-06-12      Bugfix #728 - flaws on traversing and replacement tackled
 *      Kay Gürtzig     2019-06-13      Bugfix #728 - IRoutinePoolListener inheritance added, now reacts on diagram changes
 *                                      Retrieval and traversal strategies unified (now tree is always completely shown)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      TODO / FIXME:
 *      - Matching / Replacement with regular expressions ok?
 *      - Place element icons next to the element type checkboxes (and analogously to the Root type checkboxes)?
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.archivar.IRoutinePoolListener;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.IElementSequence;
import lu.fisch.structorizer.elements.IElementSequence.Iterator;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangFrame;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 * A dialog providing the usual tools to search a (sub) diagram for Elements matching certain string
 * patterns with the opportunity to replace the text parts by other patterns
 */
@SuppressWarnings("serial")
public class FindAndReplace extends LangFrame implements IRoutinePoolListener /*implements WindowListener*/ {

	// START KGU#484 2018-04-05: Info #463
	public static final Logger logger = Logger.getLogger(FindAndReplace.class.getName());
	// END KGU#484 2018-04-05

	/** pattern history limitation */
	private static final int MAX_RECENT_PATTERNS = 10;
	/** maximum height of the text and the comment preview text areas */
	private static final int MAX_PREVIEW_HEIGHT = 75;
	/** little trick to achieve a sufficient text box width on packing the dialog */
	private static final String patternPrototype = "This is just a string long enough to establish a sufficient width for the entire dialog";
	// START KGU#647 2019-02-07: Part of the #675 workaround
	/** Matcher to identify Windows look&feel classes */
	private static final Matcher WINDOWS_LaF_MATCHER = Pattern.compile(".*windows.*", Pattern.CASE_INSENSITIVE).matcher("");
	/** GUI scale factor */
	private static double scaleFactor = 1.0;
	// END KGU#647 2019-02-07
	/** Search pattern history */
	private final LinkedList<String> searchPatterns = new LinkedList<String>();
	/** Replacement pattern history */
	private final LinkedList<String> replacePatterns = new LinkedList<String>();
	/** recursion-stopping flag for the choice list update of pattern comboboxes */
	private boolean fillingComboBox = false;
	// START KGU#684 2019-06-13: Bugfix #728
	/** Replacement action flag (is to avoid result tree wiping during replacement) */
	private boolean replacing = false;
	// END KGU#684 2019-06-13
	/** Currently focused diagram {@link Element} (caches the current position of {@link #treeIterator}) */
	private Element currentElement = null;
	// START KGU#454 2017-11-03: Bugfix #448
	/**
	 * Parts of the split text of the {@link #currentElement}
	 * The matches are at the uneven index positions
	 */
	private final StringList partsText = new StringList();
	/**
	 * Parts of the split comment of the {@link #currentElement}
	 * The matches are at the uneven index positions
	 */
	private final StringList partsComment = new StringList();
	// END KGU#454 2017-11-03
	/** Position of the match within the virtual concatenation of an element's text and comment (as far as being subject) */
	private int currentPosition = -1;
	/** Fixed invisible top node of the tree view, root of the apparent search result forest */
	private final DefaultMutableTreeNode resultTop = new DefaultMutableTreeNode("Search Results");
	/**
	 * currentNode is used to navigate in the pre-retrieved tree of matching elements
	 */
	private DefaultMutableTreeNode currentNode = null;
	private DefaultTreeModel resultModel = null;
	
	// Pre-compiled matchers for word separation
	private static final Pattern PTRN_WORDL = Pattern.compile("(\\n|.)*?\\W");
	private static final Pattern PTRN_WORDR = Pattern.compile("\\W(\\n|.)*?");
	// Caution! Can we be sure these "constants" aren't concurrently used?
	private static Matcher mtchWordL = PTRN_WORDL.matcher("");
	private static Matcher mtchWordR = PTRN_WORDR.matcher("");

	/**
	 * Allows to formulate sets of interesting element types
	 */
	// START KGU#686 2019-03-17: Enh. #56
	//public enum ElementType { ROOT, INSTRUCTION, ALTERNATIVE, CASE, FOR, WHILE, REPEAT, FOREVER, CALL, JUMP, PARALLEL };
	public enum ElementType { ROOT, INSTRUCTION, ALTERNATIVE, CASE, FOR, WHILE, REPEAT, FOREVER, CALL, JUMP, PARALLEL, TRY };
	// END KGU#686 2019-03-15
	
	/**
	 * Specifies the search scope (a selected element sequence, single diagram, or multiple diagrams).<br/>
	 * Text attribute is subject to locale setting. 
	 */
	public enum Scope { CURRENT_SELECTION, CURRENT_DIAGRAM, OPENED_DIAGRAMS;
		private String text = null;
		public String toString()
		{
			if (text != null) return text;
			return super.toString();
		}
		public void setText(String _caption)
		{
			text = _caption;
		}
	};
	
	/** The commanding {@link Diagram} */
	private Diagram diagram;
	
	private class MyTreeCellRenderer extends DefaultTreeCellRenderer {

		public MyTreeCellRenderer() {}

		@Override
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus)
		{
			super.getTreeCellRendererComponent(
					tree, value, sel,
					expanded, leaf, row,
					hasFocus);
			if (value instanceof DefaultMutableTreeNode) {
				value = ((DefaultMutableTreeNode)value).getUserObject();
			}
			if (value instanceof Element) {
				ImageIcon icon = ((Element)value).getMiniIcon();
				if (icon != null) {
					setIcon(icon);
				}
				// START KGU#480 208-01-22: Enh. #490 - Replace aliases if necessary
				//StringList text = ((Element)value).getText();
				String description = "---";
				StringList text = ((Element)value).getAliasText();
				// END KGU#480 2018-01-22
				if (!text.isEmpty()) {
					description = text.get(0);
					if (text.count() > 1) {
						description += " ...";
					}
				}
				this.setText(description);
			}
			return this;
		}
	};
		
	// Needed GUI controls
	protected JLabel lblSearchPattern;
	protected JLabel lblReplacePattern;
	protected JComboBox<String> cmbSearchPattern;
	protected JComboBox<String> cmbReplacePattern;
	protected JRadioButton rbDown;
	protected JRadioButton rbUp;
	protected JCheckBox chkCaseSensitive = new JCheckBox("Case sensitive");
	protected JCheckBox chkWholeWord;	// Not sensible with regex search
	protected JCheckBox chkRegEx;
	protected JCheckBox chkInTexts;
	protected JCheckBox chkInComments;
	protected JCheckBox chkDisabled;
	protected JCheckBox chkElementwise;
	protected JCheckBox[] chkElementTypes;
	protected JCheckBox[] chkRootTypes;		// main, sub, includable (only active if scope is "open diagrams"
	protected JComboBox<Scope> cmbScope;	// current diagram, current selection, open diagrams (or as radio group?)
	protected JButton btnAll;				// selectes all element types
	protected JButton btnNone;				// unselects all element types
	protected JButton btnFind;
	protected JButton btnReplaceFind;
	protected JButton btnReplace;
	protected JButton btnReplaceAll;
	protected JButton btnClose;
	/** Preview area for the current element text, may be editable */
	protected JTextPane txtText;
	/** Copy of the found element text with match markers */
	protected StyledDocument docText = null;	// 
	/** preview area for the current element comment, may be editable */
	protected JTextPane txtComm;
	/** Copy of the found element comment with match markers */
	protected StyledDocument docComm = null;
	/** Presentation of the search results over a diagram forest */
	protected JTree treResults = null;
	protected JScrollPane scrTree;
	
	protected JPanel pnlOptions;
	protected JPanel pnlMode;
	protected JPanel pnlRootTypes;
	protected JPanel pnlWherein;
	protected JPanel pnlScope;
	protected JPanel pnlElements;
	protected JPanel pnlResults;
	protected JPanel pnlPreview;
	/** Key listener for ComboBoxEditors */
	private KeyListener cmbKeyListener;
	
	// START KGU#454 2017-11-03: Bugfix #448
	public static final LangTextHolder msgRegexCorrupt = new LangTextHolder("Regular expression «%1» seems invalid: %2");
	public static final LangTextHolder ttlSearchError = new LangTextHolder("Search Error");
	// END KGU#454 2017-11-03
	
	/**
	 * Creates a new Find & Replace dialog associated with {@code _diagram}.
	 * @param _diagram - commanding {@link Diagram} object (needed for selection retrieval etc.)
	 */
	public FindAndReplace(Diagram _diagram) {
		diagram = _diagram;
		initComponents();
	}

	/**
	 * Creates and initializes the GUI
	 */
	private void initComponents()
	{
		// FIXME: There should rather be buttons FindAll, FindNext, FindPrev, ReplaceNext, ReplaceAll

		this.setIconImage(IconLoader.getIcon(73).getImage());
		Ini ini = Ini.getInstance();
		try {
			ini.load();
		} catch (IOException ex) {
			// Seems ok to ignore this
			// START KGU#484 2018-04-05: Issue #463 
			//ex.printStackTrace();
			logger.log(Level.WARNING, "Failed to re-read preferences.", ex);
			// END KGU#484 2018-04-05
		}
		scaleFactor = Double.valueOf(ini.getProperty("scaleFactor","1"));
		int inset = (int)Math.round(5 * scaleFactor);

		for (int i = 1; i <= MAX_RECENT_PATTERNS; i++) {
			String pattern = ini.getProperty("searchPattern" + i, ""); 
			if (!pattern.isEmpty()) {
				searchPatterns.add(pattern + "");
			}
			pattern = ini.getProperty("replacePattern" + i, ""); 
			if (!pattern.isEmpty()) {
				replacePatterns.add(pattern + "");
			}
		}

		this.setTitle("Find / Replace");
		
		JPanel pnlContent = new JPanel();
		JPanel pnlButtons = new JPanel();
		pnlOptions = new JPanel();
		pnlMode = new JPanel();
		pnlScope = new JPanel();
		pnlRootTypes = new JPanel();
		pnlWherein = new JPanel();
		pnlElements = new JPanel();
		pnlResults = new JPanel();
		pnlPreview = new JPanel();

		Container contentPane = this.getContentPane();
		contentPane.add(pnlContent);
		contentPane.setLayout(new BorderLayout());
		
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent evt) 
			{
				if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				if (evt.getKeyCode() == KeyEvent.VK_ADD)
				{
					selectAllElementTypes(true);
				}
				if (evt.getKeyCode() == KeyEvent.VK_SUBTRACT)
				{
					selectAllElementTypes(false);;
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// No action necessary
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// No action necessary
			}
		};
		
		cmbKeyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent evt) 
			{
				Object comp = evt.getSource();
				if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					// Hide the window if Esc key is pressed without the pulldown list being visible
					// (otherwise just close the pulldown)
					JComboBox<String> box = cmbSearchPattern.getEditor().getEditorComponent() == comp ? cmbSearchPattern : cmbReplacePattern;
					if (!box.isPopupVisible()) {
						setVisible(false);
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// Nothing to do
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// Nothing to do
			}
		};
		
		PopupMenuListener cmbPopupListener = new PopupMenuListener() {

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// Nothing to do
			}

			@SuppressWarnings("unchecked")
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
				Object comp = evt.getSource();
				if (comp instanceof JComboBox<?>) {
					updatePatternList((JComboBox<String>)comp);
				}
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				// Nothing to do
			}
			
		};

		//=================== PATTERNS ========================
		{
			lblSearchPattern = new JLabel("Find:");
			lblReplacePattern = new JLabel("Replace with:");
			cmbSearchPattern = new JComboBox<String>();
			cmbReplacePattern = new JComboBox<String>();
			cmbSearchPattern.setEditable(true);
			cmbSearchPattern.setPrototypeDisplayValue(patternPrototype);
			//cmbSearchPattern.addKeyListener(keyListener);
			cmbSearchPattern.getEditor().getEditorComponent().addKeyListener(cmbKeyListener);
			cmbSearchPattern.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent evt) {
					patternChanged(evt);
				}
			});
			cmbSearchPattern.addPopupMenuListener(cmbPopupListener);
			
			cmbReplacePattern.setEditable(true);
			cmbReplacePattern.setPrototypeDisplayValue(patternPrototype);
			cmbReplacePattern.getEditor().getEditorComponent().addKeyListener(cmbKeyListener);
			cmbReplacePattern.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent evt) {
					patternChanged(evt);
				}
			});
			cmbReplacePattern.addPopupMenuListener(cmbPopupListener);
			
			refillPatternCombos(null);
			
			JPanel pnlPatterns = new JPanel();
			GridBagLayout gblPatterns = new GridBagLayout(); 
			pnlPatterns.setLayout(gblPatterns);
			GridBagConstraints gbcPatterns = new GridBagConstraints();
			
			gbcPatterns.gridx = 1;
			gbcPatterns.gridy = 1;
			gbcPatterns.gridwidth = 1;
			gbcPatterns.gridheight = 1;
			gbcPatterns.weightx = 1;
			gbcPatterns.anchor = GridBagConstraints.WEST;
			gbcPatterns.insets = new Insets(inset, inset, 0, inset);
			gblPatterns.setConstraints(lblSearchPattern, gbcPatterns);
			pnlPatterns.add(lblSearchPattern);

			gbcPatterns.gridx = 2;
			gbcPatterns.gridy = 1;
			gbcPatterns.gridwidth = 4;
			gbcPatterns.gridheight = 1;
			gbcPatterns.weightx = 4;
			gbcPatterns.anchor = GridBagConstraints.WEST;
			gbcPatterns.insets.left = 0;
			gblPatterns.setConstraints(cmbSearchPattern, gbcPatterns);
			pnlPatterns.add(cmbSearchPattern);

			gbcPatterns.gridx = 1;
			gbcPatterns.gridy = 2;
			gbcPatterns.gridwidth = 1;
			gbcPatterns.gridheight = 1;
			gbcPatterns.weightx = 1;
			gbcPatterns.anchor = GridBagConstraints.WEST;
			gbcPatterns.insets = new Insets(inset, inset, inset, inset);
			gblPatterns.setConstraints(lblReplacePattern, gbcPatterns);
			pnlPatterns.add(lblReplacePattern);

			gbcPatterns.gridx = 2;
			gbcPatterns.gridy = 2;
			gbcPatterns.gridwidth = 4;
			gbcPatterns.gridheight = 4;
			gbcPatterns.weightx = 1;
			gbcPatterns.anchor = GridBagConstraints.WEST;
			gbcPatterns.insets.left = 0;
			gblPatterns.setConstraints(cmbReplacePattern, gbcPatterns);
			pnlPatterns.add(cmbReplacePattern);

			contentPane.add(pnlPatterns, BorderLayout.NORTH);

		}

		//=================== OPTIONS ========================
		{

			pnlOptions.setLayout(new BorderLayout());

			JPanel pnlOptionsWest = new JPanel();
			pnlOptionsWest.setLayout(new BoxLayout(pnlOptionsWest, BoxLayout.Y_AXIS));

			// --------------- MODE -------------
			
			pnlMode.setLayout(new GridLayout(0, 1));
			pnlMode.setBorder(new TitledBorder("Mode"));
			
			this.rbDown = new JRadioButton("Down");
			this.rbUp = new JRadioButton("Up");
			ButtonGroup grpDirection = new ButtonGroup();
			grpDirection.add(rbDown);
			grpDirection.add(rbUp);
			
			this.chkCaseSensitive.setMnemonic(java.awt.event.KeyEvent.VK_C);
			this.chkCaseSensitive.addKeyListener(keyListener);
			this.chkCaseSensitive.setSelected(ini.getProperty("findCaseSensitive", "0").equals("1"));
			pnlMode.add(chkCaseSensitive);
			
			this.chkWholeWord = new JCheckBox("Whole word");
			this.chkWholeWord.setMnemonic(java.awt.event.KeyEvent.VK_W);
			this.chkWholeWord.addKeyListener(keyListener);
			this.chkWholeWord.setSelected(ini.getProperty("findWholeWord", "0").equals("1"));
			pnlMode.add(chkWholeWord);

			this.chkRegEx = new JCheckBox("Regular expressions");
			this.chkRegEx.setMnemonic(java.awt.event.KeyEvent.VK_X);
			this.chkRegEx.addKeyListener(keyListener);
			this.chkRegEx.addItemListener(new ItemListener() {
				//@Override
				public void itemStateChanged(ItemEvent evt) {
					Boolean deselected = evt.getStateChange() == ItemEvent.DESELECTED;
					chkCaseSensitive.setEnabled(deselected);
					chkWholeWord.setEnabled(deselected && Function.testIdentifier((String)cmbSearchPattern.getEditor().getItem(), null));
				}
			});
			this.chkRegEx.setSelected(ini.getProperty("findRegEx", "0").equals("1"));
			pnlMode.add(chkRegEx);
			
			JPanel pnlDirection = new JPanel();
			pnlDirection.setLayout(new GridLayout(1, 0));
			this.rbDown.setMnemonic(java.awt.event.KeyEvent.VK_O);
			this.rbDown.addKeyListener(keyListener);
			pnlDirection.add(rbDown);
			this.rbUp.setMnemonic(java.awt.event.KeyEvent.VK_U);
			this.rbUp.addKeyListener(keyListener);
			pnlDirection.add(rbUp);
			if (ini.getProperty("searchDir", "down").equals("up")) {
				rbUp.setSelected(true);
			}
			else {
				rbDown.setSelected(true);
			}
			
			pnlMode.add(pnlDirection);
			
			pnlOptionsWest.add(pnlMode);
			
			// --------------- SCOPE -------------
		
			pnlScope.setBorder(new TitledBorder("Scope"));
			GridBagLayout gblScope = new GridBagLayout();
			GridBagConstraints gbcScope = new GridBagConstraints();
			pnlScope.setLayout(gblScope);
			
			gbcScope.gridx = 1;
			gbcScope.gridy = 1;
			gbcScope.anchor = GridBagConstraints.WEST;
			gbcScope.fill = GridBagConstraints.HORIZONTAL;
			gbcScope.insets = new Insets(inset, inset, inset, inset);
			
			ItemListener scopeListener = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getSource() == cmbScope) {
						doButtonsScope();
					}
					resetResults();
				}				
			};
			
			cmbScope = new JComboBox<Scope>(Scope.values());
			cmbScope.addItemListener(scopeListener);
			cmbScope.addKeyListener(keyListener);
			String lastScopeName = ini.getProperty("searchScope", Scope.CURRENT_DIAGRAM.name());
			Scope lastScope = Scope.valueOf(lastScopeName); 
			if (lastScope != null) {
				cmbScope.setSelectedItem(lastScope);
			}
			pnlScope.add(cmbScope, gbcScope);
			
			pnlRootTypes.setBorder(new TitledBorder("Diagram types"));
			pnlRootTypes.setLayout(new BoxLayout(pnlRootTypes, BoxLayout.Y_AXIS));
			Root.DiagramType[] rootTypes = Root.DiagramType.values();
			chkRootTypes = new JCheckBox[rootTypes.length];
			for (int i = 0; i < rootTypes.length; i++) {
				boolean sel = !ini.getProperty("search" + rootTypes[i].name(), "1").equals("0"); 
				chkRootTypes[i] = new JCheckBox(rootTypes[i].name());
				chkRootTypes[i].setSelected(sel);
				chkRootTypes[i].addItemListener(scopeListener);
				chkRootTypes[i].addKeyListener(keyListener);
				pnlRootTypes.add(chkRootTypes[i]);
			}
			gbcScope.gridy++;
			gbcScope.insets.top = 0;
			pnlScope.add(pnlRootTypes, gbcScope);
			
			pnlWherein.setBorder(BorderFactory.createEtchedBorder());
			pnlWherein.setLayout(new BoxLayout(pnlWherein, BoxLayout.Y_AXIS));
			chkInTexts = new JCheckBox("In texts");
			chkInTexts.setSelected(!ini.getProperty("findInTexts", "1").equals("0"));
			pnlWherein.add(chkInTexts);
			
			chkInComments = new JCheckBox("In comments");
			chkInComments.setSelected(!ini.getProperty("findInComments", "1").equals("0"));
			pnlWherein.add(chkInComments);
			
			ItemListener whereinListener = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					// It doesn't make sense if both checkboxes are unselected
					if (!chkInTexts.isSelected() && !chkInComments.isSelected()) {
						if (evt.getSource() == chkInTexts) {
							chkInComments.setSelected(true);
						}
						else {
							chkInTexts.setSelected(true);
						}
					}
					if (txtText != null) {
			    		// START KGU#480 208-01-22: Enh. #490 - Replace aliases if necessary
						//txtText.setEnabled(chkInTexts.isSelected() && currentElement != null && textMatches(currentElement.getText()) > 0);
						txtText.setEnabled(chkInTexts.isSelected() && currentElement != null && textMatches(currentElement.getAliasText()) > 0);
			    		// END KGU#480 2018-01-22
					}
					if (txtComm != null) {
						txtComm.setEnabled(chkInComments.isSelected() && currentElement != null && textMatches(currentElement.getComment()) > 0);
					}
				}};
			chkInTexts.addItemListener(whereinListener);
			chkInTexts.addKeyListener(keyListener);
			chkInComments.addItemListener(whereinListener);
			chkInComments.addKeyListener(keyListener);
			
			gbcScope.gridy++;
			gbcScope.insets.top = inset;
			//gblScope.setConstraints(pnlWherein, gbcScope);
			pnlScope.add(pnlWherein, gbcScope);
			
			pnlOptionsWest.add(pnlScope);
			
			pnlOptions.add(pnlOptionsWest, BorderLayout.WEST);

			// -------------- ELEMENT TYPES -------------
			
			JPanel pnlOptionsEast = new JPanel();
			pnlOptionsEast.setLayout(new BoxLayout(pnlOptionsEast, BoxLayout.Y_AXIS));

			pnlElements.setBorder(new TitledBorder("Element Types"));
			pnlElements.setLayout(new BoxLayout(pnlElements, BoxLayout.Y_AXIS));
			
			btnAll = new JButton("All");
			btnAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) { selectAllElementTypes(true); }});
			btnAll.addKeyListener(keyListener);
			btnNone = new JButton("None");
			btnNone.addKeyListener(keyListener);
			btnNone.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) { selectAllElementTypes(false); }});
			JPanel pnlSelectButtons = new JPanel();
			pnlSelectButtons.setLayout(new BoxLayout(pnlSelectButtons, BoxLayout.X_AXIS));
			pnlSelectButtons.add(btnAll);
			pnlSelectButtons.add(btnNone);
			pnlSelectButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			pnlElements.add(pnlSelectButtons);
			
			ItemListener elementTypeListener = new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						// FIXME: There might be more influence factors
						btnFind.setEnabled(true);
						btnReplaceFind.setEnabled(true);
						btnReplace.setEnabled(currentElement != null);
						btnReplaceAll.setEnabled(true);
					}
					// START KGU#540 2018-07-02: Bugfix - a filter change didn't reset the selection result
					// FIXME: In case it is a stronger restriction, we might perhaps just refilter the results?
					resetResults();
					// END KGU#540 2018-07-02
				}
			};

			ElementType[] types = ElementType.values();
			chkElementTypes = new JCheckBox[types.length];
			for (int i = 0; i < types.length; i++) {
				boolean sel = !ini.getProperty("find" + types[i].name(), "1").equals("0"); 
				chkElementTypes[i] = new JCheckBox(types[i].name());
				chkElementTypes[i].setSelected(sel);
				chkElementTypes[i].addItemListener(elementTypeListener);
				chkElementTypes[i].addKeyListener(keyListener);
				chkElementTypes[i].setAlignmentX(Component.LEFT_ALIGNMENT);
				pnlElements.add(chkElementTypes[i]);
			}

			pnlElements.add(new JSeparator(SwingConstants.HORIZONTAL));

			chkDisabled = new JCheckBox("Disabled elements");
			chkDisabled.setSelected(ini.getProperty("findDisabledElements", "0").equals("1"));
			chkDisabled.addKeyListener(keyListener);
			chkDisabled.setAlignmentX(Component.LEFT_ALIGNMENT);
			pnlElements.add(chkDisabled);

			pnlOptionsEast.add(pnlElements);

			pnlOptions.add(pnlOptionsEast, BorderLayout.EAST);
			
			// -------------- Text View ---------------
			
			pnlPreview.setLayout(new GridLayout(0, 1));
			pnlPreview.setBorder(BorderFactory.createTitledBorder("Contents"));

			txtText = new JTextPane();
			txtText.setBorder(BorderFactory.createTitledBorder("Text"));
			txtText.setEditable(false);
			txtText.setEnabled(false);
			txtText.addKeyListener(keyListener);
			JScrollPane scrText = new JScrollPane(txtText);
//			txtText.addComponentListener(textScrollListener);
			docText = txtText.getStyledDocument();
			//Style defStyle = doc.getStyle("default");
			Style hilStyle = docText.addStyle("highlight", null);
			hilStyle.addAttribute(StyleConstants.Background, Color.YELLOW);
			hilStyle = docText.addStyle("emphasis", null);
			hilStyle.addAttribute(StyleConstants.Background, Color.ORANGE);

			pnlPreview.add(scrText);

			txtComm = new JTextPane();
			txtComm.setBorder(BorderFactory.createTitledBorder("Comment"));
			txtComm.setEditable(false);
			txtComm.setEnabled(false);
			txtComm.addKeyListener(keyListener);

			JScrollPane scrComm = new JScrollPane(txtComm);
//			txtComm.addComponentListener(textScrollListener);
			docComm = txtComm.getStyledDocument();
			//Style defStyle = doc.getStyle("default");
			hilStyle = docComm.addStyle("highlight", null);
			hilStyle.addAttribute(StyleConstants.Background, Color.YELLOW);
			hilStyle = docComm.addStyle("emphasis", null);
			hilStyle.addAttribute(StyleConstants.Background, Color.ORANGE);

			pnlPreview.add(scrComm);
			pnlOptions.add(pnlPreview, BorderLayout.SOUTH);

			Dimension dim = scrText.getMaximumSize();
			int maxHeight = (int)Math.floor(scaleFactor * MAX_PREVIEW_HEIGHT);
			if (dim != null) {
				scrText.setMaximumSize(new Dimension(dim.width, maxHeight));	// Doesn't actually work
				scrText.setPreferredSize(new Dimension(scrText.getPreferredSize().width, maxHeight));
			}
			dim = scrComm.getMaximumSize();
			if (dim != null) {
				scrComm.setMaximumSize(new Dimension(dim.width, maxHeight));	// Doesn't actually work
				scrComm.setPreferredSize(new Dimension(scrComm.getPreferredSize().width, maxHeight));
			}

			// --------------------------------------
			contentPane.add(pnlOptions, BorderLayout.CENTER);
			
		}
		
		//================ SEARCH RESULTS ====================
		{
			resultModel = new DefaultTreeModel(resultTop);
			this.treResults = new JTree(resultModel);
			this.treResults.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			this.treResults.setExpandsSelectedPaths(true);
			this.treResults.setRootVisible(false);
			// START KGU#647 2019-02-07: Issue #675 Helps to avoid the tree node texts being truncated (replaced by an ellipse)
			if (scaleFactor > 1.0) {
				this.treResults.setLargeModel(true);
			}
			// END KGU#647 2019-02-07
			// Allow to select an element in the tree
			this.treResults.addTreeSelectionListener(new TreeSelectionListener(){
				@Override
				public void valueChanged(TreeSelectionEvent evt) {
					currentNode = (DefaultMutableTreeNode)treResults.getLastSelectedPathComponent();
					if (currentNode != null) {
						Object ele = currentNode.getUserObject();
						if (currentNode.isLeaf() && ele instanceof Element) {
							int pos = -1;
							if (chkElementwise == null || !chkElementwise.isSelected()) {
								if (rbUp != null && rbUp.isSelected()) {
									pos = checkElementMatch((Element)ele) - 1;
								}
								else {
									pos = 0;
								}
							}
							setCurrentElement((Element)ele, pos);
						}
						if (ele instanceof Root && cmbScope.getSelectedItem() == Scope.OPENED_DIAGRAMS) {
							Arranger.scrollToDiagram((Root)ele, true);
						}
					}
				}});
			this.treResults.setCellRenderer(new MyTreeCellRenderer());
			this.treResults.addKeyListener(keyListener);
			this.scrTree = new JScrollPane(this.treResults);

			pnlOptions.add(scrTree, BorderLayout.CENTER);
		}
		
		//=================== BUTTONS ========================
		{
		
			final int BUTTONS_PER_ROW = 4;
			
			pnlButtons.setLayout(new GridLayout(0, BUTTONS_PER_ROW));
			
			btnFind = new JButton("Find");
			btnFind.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					findActionPerformed(evt, false, true);
				}
			});
			btnFind.setMnemonic(java.awt.event.KeyEvent.VK_N);
			btnFind.addKeyListener(keyListener);
			pnlButtons.add(btnFind);
			
			btnReplaceFind = new JButton("Replace/Find");
			btnReplaceFind.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					findActionPerformed(evt, true, true);
				}
			});
			btnReplaceFind.addKeyListener(keyListener);
			pnlButtons.add(btnReplaceFind);

			btnReplace = new JButton("Replace");
			btnReplace.setMnemonic(KeyEvent.VK_R);
			btnReplace.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					findActionPerformed(evt, true, false);
				}
			});
			btnReplace.addKeyListener(keyListener);
			pnlButtons.add(btnReplace);

			btnReplaceAll = new JButton("Replace All");
			btnReplaceAll.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					replaceAllActionPerformed(evt);
				}
			});
			btnReplaceAll.addKeyListener(keyListener);
			pnlButtons.add(btnReplaceAll);
			
			chkElementwise = new JCheckBox("Element-wise");
			chkElementwise.setMnemonic(java.awt.event.KeyEvent.VK_E);
			chkElementwise.addKeyListener(keyListener);
			chkElementwise.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					switch (evt.getStateChange()) {
					case ItemEvent.SELECTED:
						currentPosition = -1;
						break;
					case ItemEvent.DESELECTED:
						if (currentPosition < 0) {
							currentPosition= 0;
						}
						break;
					}
				}});
			pnlButtons.add(chkElementwise);
			
			for (int i = pnlButtons.getComponentCount() + 1; i % BUTTONS_PER_ROW != 0; i++) {
				pnlButtons.add(new JLabel(""));
			}
			
			btnClose = new JButton("Close");
			btnClose.setMnemonic(KeyEvent.VK_ESCAPE);
			btnClose.addKeyListener(keyListener);
			btnClose.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					setVisible(false);
				}
				
			});
			pnlButtons.add(btnClose);

			contentPane.add(pnlButtons, BorderLayout.SOUTH);

		}
		
		GUIScaler.rescaleComponents(this);

		this.pack();
		
		doButtonsScope();
		
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		//this.addWindowListener(this);
	}

	/**
	 * Clears the current element reference (including match position and preview fields),
	 * unselects the element in the diagram (if selected).
	 */
	protected void clearCurrentElement() {
		if (currentElement != null) {
			Element selected = diagram.getSelected();
			if (selected == null ||
					this.cmbScope.getSelectedItem() != Scope.CURRENT_SELECTION ||
					selected instanceof IElementSequence && ((IElementSequence)selected).getIndexOf(currentElement) >= 0 ||
					!(selected instanceof IElementSequence) && currentElement != selected) {
				currentElement.setSelected(false);
				if (Element.getRoot(currentElement) == diagram.getRoot()) {
					diagram.redraw(currentElement);
				}
			}
			currentElement = null;
			// START KGU#454 2017-11-03: Bugfix #448
			this.partsText.clear();
			this.partsComment.clear();
			// EN KGU#454 2017-11-03
			currentPosition = -1;
			if (docText != null && docComm != null) {
				clearDoc(docText);
				clearDoc(docComm);
				txtText.setEnabled(false);
				txtComm.setEnabled(false);
			}
		}
		doButtons();
	}
	
	/**
	 * Clears the given {@link StyledDocument} {@code doc} behind one of the preview text areas. 
	 * @param doc - the document to be emptied
	 */
	private void clearDoc(StyledDocument doc) {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			// START KGU#484 2018-04-05: Issue #463
			//e.printStackTrace();
			logger.log(Level.WARNING, "Trouble clearing the content.", e);
			// END KGU#484 2018-04-05
		}
	}

	/**
	 * Sets {@link Element} {@code ele} as {@link #currentElement} (if it hadn't already been)
	 * and {@code positionInElement} as the current match position inside the texts of {@code ele}.
	 * Then fills the preview text ares accordingly.<br/>
	 * Used as action callback and within the find action.
	 * @param ele - the element to be made the current element.
	 * @param positionInElement - number of the current match 
	 */
	private void setCurrentElement(Element ele, int positionInElement)
	{
		if (ele != currentElement) {
			clearCurrentElement();
			// START KGU#454 2017-11-03: Bugfix #448
			currentElement = ele;
			// START KGU#480 2018-01-22: Enh. #490 
			//this.splitTextToList(ele.getText(), this.partsText);
			this.splitTextToList(ele.getAliasText(), this.partsText);
			// END KGU#480 2018-01-22
			this.splitTextToList(ele.getComment(), this.partsComment);
			// END KGU#454 2017-11-03
		}
		// START KGU#454 2017-11-03: Bugfix #448
		//currentElement = ele;	// Moved into the alternative above (otherwise redundant)
		// END KGU#454 2017-11-03
		currentPosition = positionInElement;
		ele.setSelected(true);
		if (Element.getRoot(ele) == diagram.getRoot()) {
			diagram.redraw(ele);
		}
		//System.out.println(ele);
		int nMatches = 0;
		boolean enable = false;
		if (chkInTexts.isSelected()) {
			// START KGU#454 2017-11-03: Bugfix #448
			//nMatches = this.textMatches(ele.getText());
			nMatches = (this.partsText.count() - 1) / 2;
			// END KGU#454 2017-11-03
			enable = nMatches > 0;
		}
		// START KGU#454 2017-11-03: Bugfix #448
		//fillPreview(ele.getText(), docText, txtText, 0, enable);
		//enable = chkInComments.isSelected() && this.textMatches(ele.getComment()) > 0;
		//fillPreview(ele.getComment(), docComm, txtComm, nMatches, enable);
		fillPreview(partsText, docText, txtText, 0, enable);
		enable = chkInComments.isSelected() && this.partsComment.count() > 1;
		fillPreview(this.partsComment, docComm, txtComm, nMatches, enable);
		// END KGU#454 2017-11-03
		doButtons();
	}
	
	/**
	 * Fills the document {@code doc} associated to {@link JTextPane} {@code txtPane} with the match preview
	 * for the original {@link StringList} {@code txtLines}. 
	 * @param textParts - source text split to matches and surrounding parts
	 * @param doc - the target {@link StyledDocument} 
	 * @param txtPane - the presenting {@link JTextPane}, to be enabled or disabled (according to {@code enable}) 
	 * @param posOffset - the index of the current match
	 * @param enable - whether the {@code txtPane} is to be enabled.
	 * @return the number of matches found within the given source text {@code txtLines}
	 */
	private int fillPreview(StringList textParts, StyledDocument doc, JTextPane txtPane, int posOffset, boolean enable) {
		//int nParts = 0;
		int currPos = currentPosition - posOffset;
		// START KGU#454 2017-11-03: Bugfix #448
		//String text0 = txtLines.getText();
		String text0 = textParts.concatenate();
		// END KGU#454 2017-11-03
		clearDoc(doc);
		txtPane.setEnabled(enable);
		//String pattern = (String)cmbSearchPattern.getEditor().getItem();
		if (!enable) {
			// Simply show the text without highlighting etc.
			try {
				doc.insertString(doc.getLength(), text0, doc.getStyle("default"));
			} catch (BadLocationException e) {
				// START KGU#484 2018-04-05: Issue #463 
				//e.printStackTrace();
				logger.log(Level.WARNING, "Inconsistent content.", e);
				// END KGU#484 2018-04-05
			}
		}
		else {
			// START KGU#454 2017-11-03: Bugfix #448 (obsolete code removed 2019-02-07)
			// Once having the split parts it's extremely simple: Show the text parts
			// alternatingly in normal and highlighted style.
			try {
				int emphPos = -1;
				for (int i = 0; i < textParts.count(); i++) {
					String styleName = "default";
					if (i % 2 != 0) {
						styleName = "highlight";
						if ((i - 1) / 2 == currPos) {
							styleName = "emphasis";
							emphPos = doc.getLength();
						}
					}
					doc.insertString(doc.getLength(), textParts.get(i), doc.getStyle(styleName));
				}
				if (emphPos > -1) {
					txtPane.setCaretPosition(emphPos);
				}
			} catch (BadLocationException e) {
				// START KGU#484 2018-04-05: Issue #463 
				//e.printStackTrace();
				logger.log(Level.WARNING, "Inconsistent content.", e);
				// END KGU#484 2018-04-05
			}
			// END KGU#454 2017-11-03
		}
		// START KGU#454 2017-11-03: Bugfix #448
//		return nParts - 1;
		return (textParts.count() - 1) / 2;
		// END KGU#454 2017-11-03
	}

	// FIXME: We might cache the split results for the currentElement
	/**
	 * Splits the source string {@code text} (may contain newlines) with respect to the
	 * matching {@code pattern}, returns the split results and fills th matching substrings
	 * into {@code realWords} (the name means that it contains the real strings matching
	 * the patterns as needed for the preview highlighting.
	 * @param text - the newline-separated source text as String
	 * @param pattern - the search pattern as string
	 * @param realWords - empty {@link StringList} to be filled with the matching substrings
	 * @return array of splitting results (i.e. the substrings around the matches)
	 */
	private String[] splitText(String text, String pattern, StringList realWords) {
		int lenPattern = pattern.length();
		boolean caseSens = chkCaseSensitive.isSelected();
		boolean isRegex = chkRegEx.isSelected();
		String splitter = pattern + "";
		if (!isRegex) {
			if (caseSens) {
				splitter = Pattern.quote(splitter);
			}
			else {
				splitter = BString.breakup(splitter);
				// We must care for metasymbols lest the regex mechanism should run havoc
			}
		}
		String[] parts = text.split(splitter, -1);
		int nParts = parts.length;
		// Restore all matched substrings.
		String[] matches = new String[nParts-1];
		int start = 0;
		for (int i = 0; i < nParts-1; i++) {
			String part = parts[i];
			int lenPart = part.length();
			if (!isRegex) {
				if (caseSens) {
					matches[i] = pattern;
				}
				else {
					matches[i] = text.substring(start + lenPart, start + lenPart + lenPattern);
				}
			}
			else {
				// In case of regular expressions we cannot deduce the length of the match
				// So we try something vague: We try to match the pattern in the remainder of
				// the text and "replace" it by itself. This way, we my derive the length of
				// the match and copy the respective substring from the original text.
				String remainder = text.substring(start + lenPart);
				// We have to consider that the regular expression may or may not subsume line
				// feed under '.'. Hence we must either check linewise or replace all newlines
				// by something unlikely in program texts but being matched by '.', or as third
				// variant explicitly matching newlines. The last way is what we try.
				// Unfortunately, this may crash in certain cases, we can't even catch it.
				// FIXME: We should first try some possibly incomplete but less dangerous
				// approach, e.g. by seeking the next split part in the text beyond the previous
				// split part. We cannot exclude, however, that this text part might also be
				// a substring of the match, thus suggesting too early a position...
				int pos = remainder.indexOf(parts[i+1]);
				if (pos > -1) {
					matches[i] = remainder.replaceFirst("(" + splitter +")(\\n|.)*", "$1");
				}
				// The splitter might require a non-line start, so we better involve the previous
				// context for the eventual trial...
				remainder = text.substring(start);
				matches[i] = remainder.replaceFirst("(\\n|.)*?(" + splitter + ")(\\n|.)*", "$2");
			}
			start += lenPart + matches[i].length();
		}
		// Finally we may have to re-combine wrong matches if we only may accept whole word matches
		if (chkWholeWord.isSelected()) {
			StringList realParts = new StringList(); 
			String part = parts[0];
			for (int i = 0; i < nParts - 1; i++) {
				String nextPart = parts[i+1];
				if ((part.isEmpty() || mtchWordL.reset(part).matches())
						&& (i+2 == nParts && nextPart.isEmpty() || mtchWordR.reset(nextPart).matches())) {
					realParts.add(part);
					realWords.add(matches[i]);
					part = nextPart;
				}
				else {
					part += matches[i] + nextPart;
				}
			}
			realParts.add(part);
			parts = realParts.toArray();
		}
		else {
			for (String word: matches) {
				realWords.add(word);
			}
		}
		return parts;
	}

	// START KGU#454 2017-11-03: Bugfix #448 - helper to cache the splitting results
	/**
	 * Splits the source string {@code text} (may contain newlines) with respect to the
	 * matching {@code pattern} and fills the {@link StringList} {@code partList} with the
	 * splitting results where the elements with even indices are the parts between the
	 * matches and the elements with uneven indices are the matches themselves.
	 * @param text - the newline-separated source text as String
	 * @param partList - empty {@link StringList} to be filled with the splitting results
	 * @return array of splitting results (i.e. the substrings around the matches)
	 */
	private int splitTextToList(StringList text, StringList partList) {
		String brokenText = text.getText();
		StringList matches = new StringList();
		if (chkInTexts.isSelected()) {
			String pattern = (String)cmbSearchPattern.getEditor().getItem();
			String[] parts = this.splitText(brokenText, pattern, matches);
			for (int i = 0; i < parts.length - 1; i++) {
				partList.add(parts[i]);
				partList.add(matches.get(i));
			}
			partList.add(parts[parts.length-1]);
		}
		else {
			partList.add(brokenText);
		}
		return matches.count();
	}
	// END KGU#454 2017-11-03

	/**
	 * Empties the result tree and unsets all navigation data ({@link #currentElement},
	 * {@link #treeIterator}, {@link #currentNode}, {@link #currentPosition}). (Part of
	 * the ItemListener for the scope choice and the StateChangeListener of the pattern
	 * combo boxes, but may also be called externally.)
	 */
	public void resetResults()
	{
		// START KGU#684 2019-06-13: Bugfix #728 - Should not be triggered during replacement
		if (replacing) {
			return;
		}
		// END KGU#684 2019-06-13
//		treeIterator = null;
		resultTop.removeAllChildren();
		currentNode = null;
		clearCurrentElement();
		if (treResults != null) {
			resultModel.reload();
			treResults.setEnabled(false);
		}
	}
	
	// START KGU#684 2019-06-13: Bugfix #728
	@Override
	public void routinePoolChanged(IRoutinePool _source, int _flags) {
		if ((_flags & IRoutinePoolListener.RPC_POOL_CHANGED) != 0
				&& this.cmbScope.getSelectedItem() == Scope.OPENED_DIAGRAMS) {
			this.resetResults();
		}
	}
	// END KGU#684 2019-06-13

	/**
	 * Enables or disables all element types (selects or unselects the corresponding
	 * checkboxes) and updates button visibility.
	 * @param toBeSelected - whether to select or unselect the checkboxes.
	 */
	protected void selectAllElementTypes(boolean toBeSelected)
	{
		for (int i = 0; i < ElementType.values().length; i++) {
			chkElementTypes[i].setSelected(toBeSelected);
		}
		if (!toBeSelected) {
			btnFind.setEnabled(false);
			btnReplaceFind.setEnabled(false);
			btnReplace.setEnabled(false);
			btnReplaceAll.setEnabled(false);
		}
	}

	/**
	 * Action method for find event {@code evt}. Performs a single action step, i.e.
	 * jumps to the next matching position (if {@code gotoNext} is true) after having
	 * possibly done a single replacement at the current matching position (if {@code replace} is true).
	 * In case there is no {@link #currentNode}, the result tree will be refreshed
	 * according to the current criteria.
	 * @param evt - the triggering {@link ActionEvent}
	 * @param replace - are replacements to be performed?
	 * @param gotoNext - is the position to be moved to the next matching hit?
	 * @return indicates whether a requested replacement could be performed 
	 */
	protected boolean findActionPerformed(ActionEvent evt, boolean replace, boolean gotoNext) {
		boolean replacementsDone = false;
		boolean up = rbUp.isSelected();
		boolean elementwise = chkElementwise.isSelected();
		// START KGU#454 2017-11-03: Bugfix #448 Check regex patterns in advance
		if (this.chkRegEx.isSelected()) {
			String patternString = (String)this.cmbSearchPattern.getEditor().getItem();
			try {
				Pattern.compile(patternString);
			}
			catch (PatternSyntaxException ex) {
				JOptionPane.showMessageDialog(this,
						msgRegexCorrupt.getText().replace("%1", patternString).replace("%2", ex.getMessage()),
						ttlSearchError.getText(),
						JOptionPane.ERROR_MESSAGE);
				this.doButtons();
				return false;
			}
		}
		// END KGU#4545 2017-11-03

		// PHASE 1: Identify and set up the element traversing strategy
		// Previous search exhausted? Then retrieve results
		if (currentNode == null) {
			fillResultTree();
			// START KGU#647 2019-02-07: FIXME - Empirical workaround for issue #675 (truncated node lines)
			if (scaleFactor > 1.0 && !WINDOWS_LaF_MATCHER.reset(UIManager.getLookAndFeel().getName()).matches()) {
				// No idea why exactly, the lines get truncated (ends replaced by ellipse on the first) on the first attempt
				fillResultTree();
			}
			// ÉND KGU#647 2019-02-07
			gotoNext = false;
			replace = false;
		}

		// PHASE 2: Look into the currentElement (if there is one) and care for replacements
		int nMatches = 0;
		if (currentElement != null) {
			// Get the total number of (remaining) matches in the current element's interesting texts
			// START KGU#454 2017-11-03: Bugfix #448 - It must not be done by new matching!
			//nMatches = checkElementMatch(currentElement);
			nMatches = checkElementMatch();
			// END KGU#4545 2017-11-03
		}
		if (replace && nMatches > 0) {
			// START KGU#684 2019-06-13: Bugfix #728 - avoid interference from element modifications
			replacing = true;
			System.out.println("replacing set true...");
			try {
			// END KGU#684 2019-06-13
				// Replace next match according to the current pattern
				Root root = Element.getRoot(currentElement);
				if (root != null) {
					// Every single replacement is to be undoable ...
					root.addUndo();
				}
				// START KGU#480 2018-01-22: Enh. #490
				//StringList text = currentElement.getText();
				StringList text = currentElement.getAliasText();
				// END KGU#480 2018-01-22
				StringList comment = currentElement.getComment();
				// START KGU#454 2017-11-03: Bugfix #448
				//int nMatchesComment = textMatches(comment);
				//int nMatchesText = textMatches(text);
				int nMatchesComment = (partsComment.count() - 1) / 2;
				int nMatchesText = (partsText.count() - 1) / 2;
				// END KGU#454 2017-11-03
				// Start with the element text (prioritized if included)
				if ((elementwise || !replacementsDone) && chkInTexts.isSelected() 
						&& nMatchesText > currentPosition) {
					// START KGU#454 2017-11-03: Bugfix #448
					//text = replacePattern(text, elementwise, currentPosition);
					text = replacePattern(partsText, elementwise, currentPosition);
					// END KGU#454 2017-11-03 
					// START KGU#480 2018-01-22: Enh. #490
					//currentElement.setText(text);
					currentElement.setAliasText(text);
					// END KGU#480 2018-01-22
					// START KGU#431 2017-10-09: We must handle the structured fields of For elements				
					if (currentElement instanceof For) {
						((For)currentElement).updateFromForClause();					
					}
					// END KGU#431 2017-10-09
					currentElement.resetDrawingInfoUp();
					// START KGU#609 2018-11-21: Bugfix #448 accomplished
					//this.fillPreview(text, docText, txtText, 0, true);
					this.fillPreview(partsText, docText, txtText, (replace && up) ? 1 : 0, true);
					// END KGU#609 2018-11-21
					replacementsDone = true;
				}
				// Now cater for the comment if included
				if ((elementwise || !replacementsDone) && chkInComments.isSelected()
						&& nMatchesComment > currentPosition - nMatchesText) {
					// START KGU#454 2017-11-03: Bugfix #448
					//comment = replacePattern(comment, elementwise, currentPosition - nMatchesText);
					comment = replacePattern(partsComment, elementwise, currentPosition - nMatchesText);
					// END KGU#454 2017-11-03 
					currentElement.setComment(comment);
					currentElement.resetDrawingInfoUp();
					// START KGU#609 2018-11-21: Bugfix #448 accomplished
					//this.fillPreview(comment, docComm, txtComm, nMatchesText, true);
					this.fillPreview(partsComment, docComm, txtComm, nMatchesText + ((replace && up) ? 1 : 0), true);
					// END KGU#609 2018-11-21
					replacementsDone = true;
				}
				if (currentNode != null) {
					// We better cache the current node locally lest the reload actions should reset it.
					DefaultMutableTreeNode currNode = currentNode;
					resultModel.reload(currentNode);	// update the element's presentation in the tree view
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currNode.getParent();
					// In case we are working on a Root make sure its representing top node is also updated
					if (parent != null && parent.getUserObject() == currNode.getUserObject()) {
						resultModel.reload(parent);
						// Restore the previously cached selection
						currentNode = currNode;
						treResults.setSelectionPath(new TreePath(currentNode.getPath()));
					}
				}
			}
			finally {
				replacing = false;
				System.out.println("replacing resset to false...");
			}

			diagram.doButtons();
			// Make sure the Structorizer working area is refreshed, too
			diagram.redraw(currentElement);
			if (replacementsDone) {
				if (elementwise) {
					// after an elementwise replacement there can't be matches left (unless the
					// replacement hasn't accidently induced new matches, which should NOT be
					// involved during this cycle)
					nMatches = 0;
				}
				else {
					// One match has gone (it would be an issue, however, if the replacement
					// accidently conjured up a new match like replacing "abc" with "ab" in text
					// "xabccy")
					nMatches--;
					// Avoid a node change while the current node isn't exhausted (note that on
					// downward search the currentPosition must not be incremented after the match
					// having been replaced). On upward search the move will be done in phase 3.
					// START KGU#684 2019-06-12: Bugfix #728
					//if (!up && currentPosition < nMatches || up && --currentPosition >= 0) {
					if (!up && currentPosition < nMatches) {
					// END KGU#684 2019-06-12
						gotoNext = false;
					}
				}
			}
		}
		
		// PHASE 3: traverse to next position (if stlll requested)
		// Is there another matching position within this element? (We might have come here in non-replacing mode!)
		// Remember that "up" in the tree means backward in the text and "down" means forward in the text here
		if (gotoNext && !elementwise && currentPosition >= 0 && (up && currentPosition > 0 || !up && currentPosition < nMatches-1)) {
			// yes: just update the preview
			if (up) {
				currentPosition--;
			}
			else {
				currentPosition++;
			}
			setCurrentElement(currentElement, currentPosition);
		}
		else if (gotoNext && currentNode != null) {
			// go to the next element within in the search result tree
			if (rbUp.isSelected()) {
				currentNode = currentNode.getPreviousLeaf();
			}
			else {
				currentNode = currentNode.getNextLeaf();
			}
			if (currentNode != null) {
				Element ele = (Element)currentNode.getUserObject();
				nMatches = checkElementMatch(ele);
				setCurrentElement(ele, elementwise ? -1 : (up ? nMatches - 1 : 0));
				TreePath path = new TreePath(currentNode.getPath());
				treResults.setSelectionPath(path);
				treResults.scrollPathToVisible(path);
				// START KGU#684 2019-06-12: Bugfix #728 - cannot have been correct -> endless loop
				//replacementsDone = true;
				// END KGU#684 2019-06-12
			}
			else {
				clearCurrentElement();
				treResults.clearSelection();
			}
		}
		return replacementsDone;
	}
	
	/**
	 * Initializes the result tree and sets {@link #currentNode} (if possible)
	 * @see #updateResultTree()
	 */
	private void fillResultTree() {
		resultTop.removeAllChildren();
		resultModel.reload();
		clearCurrentElement();
		DefaultMutableTreeNode lastNode = null;
		// START KGU#712 2019-06-13 CR
		//Vector<Root> roots = Arranger.getSortedRoots();
		Scope scope = (Scope)this.cmbScope.getSelectedItem();
		Vector<Root> roots;
		if (scope == Scope.OPENED_DIAGRAMS) {
			roots = Arranger.getSortedRoots();
		}
		else {
			roots = new Vector<Root>();
		}
		// END KGU#712 2019-06-13
		if (!roots.isEmpty()) {
			Arranger.addToChangeListeners(this);
		}
		if (!roots.contains(diagram.getRoot())) {
			roots.add(0, diagram.getRoot());
		}
		for (Root root: roots) {
			IElementSequence range = null;
			Element selected = diagram.getSelected();
			if (scope == Scope.CURRENT_SELECTION && root == diagram.getRoot()) {
				if (selected == null || selected == root) {
					range = root.children;
				}
				else if (selected instanceof IElementSequence) {
					range = (IElementSequence)selected;
				}
				else {
					range = new SelectedSequence(selected, selected);
				}
			}
			else {
				range = root.children;
			}
			LinkedList<Element> elements = this.findElements(range, true);
			if ((scope != Scope.CURRENT_SELECTION || selected == root) && checkElementMatch(root) > 0) {
				elements.addFirst(root);
			}
			if (!elements.isEmpty()) {
				DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root);
				//this.resultTop.add(rootNode);
				this.resultModel.insertNodeInto(rootNode, this.resultTop, this.resultTop.getChildCount());
				for (Element ele: elements) {
					lastNode = new DefaultMutableTreeNode(ele);
					if (currentNode == null) {
						currentNode = lastNode;
					}
					//rootNode.add(lastNode);
					this.resultModel.insertNodeInto(lastNode, rootNode, rootNode.getChildCount());					
				}
			}
		}
		if (rbUp.isSelected()) {
			currentNode = lastNode;
		}
		if (currentNode != null) {
			TreePath path = new TreePath(currentNode.getPath());
			treResults.scrollPathToVisible(path);
			if (scrTree.getHorizontalScrollBar().isVisible()) {
				scrTree.getHorizontalScrollBar().setValue(0);
			}
			treResults.setSelectionPath(path);
			treResults.setEnabled(true);
		}
		else {
			treResults.clearSelection();
			treResults.setEnabled(false);
		}
	}

	/**
	 * Action listener method for the "Replace All" button. Executes the find cycle
	 * with replacement.
	 * @param evt - the inducing event
	 */
	protected void replaceAllActionPerformed(ActionEvent evt) {
		// START KGU#684 2019-06-12: Bugfix #728
		// If the search hasn't been initialised then find the first match
		if (currentNode == null /*&& treeIterator == null*/) {
			findActionPerformed(evt, false, false);
		}
		// END KGU#684 2019-06-12
		while (findActionPerformed(evt, true, true));
		
	}

	/**
	 * Replaces one (= {@code pos}-th) or all matches within the given {@link StringList} {@code text}
	 * @param splitText - the target text, already split.
	 * @param all - whether all matches are to be replaced (then {@code pos} will be ignored)
	 * @param pos - the number of the target match within the text otherwise
	 * @return the replacement result as {@link StringList} of lines
	 */
	private StringList replacePattern(StringList splitText, boolean all, int pos) {
		// Get the current settings
		String searchPattern = (String)cmbSearchPattern.getEditor().getItem();
		String replacePattern = (String)cmbReplacePattern.getEditor().getItem();
		//boolean caseSensitive = chkCaseSensitive.isSelected();
		boolean isRegex = chkRegEx.isSelected();
		//boolean wholeWord = chkWholeWord.isSelected(); 
		if (all && splitText.count() > 1) {
			if (!isRegex) {
				StringBuilder sb = new StringBuilder(10 * splitText.count());
				for (int i = 0; i < splitText.count(); i++) {
					if (i % 2 == 0) {
						sb.append(splitText.get(i));
					}
					else {
						sb.append(replacePattern);
					}
				}
				splitText.set(0, sb.toString());
			}
			else {
				splitText.set(0, splitText.concatenate().replaceAll(searchPattern, replacePattern));
			}
			splitText.remove(1, splitText.count());
		}
		// START KGU#610 2018-11-22: Bugfix #637 - for comments without matches, negative pos causes harm here
		//else {
		else if (pos >= 0) {
		// END KGU#610 2018-11-22
			// In case of an individual replacement first split the text and count the matches
			// The splitting function will collect the matches and return the parts around the matches
			pos = pos * 2 + 1;
			if (pos+1 < splitText.count()) {
				// At the very position replace the found match
				if (isRegex) {
					replacePattern = splitText.get(pos).replaceFirst(searchPattern, replacePattern);
				}
				// We must now concatenate the replaced part with both its neighbours
				splitText.set(pos - 1, splitText.get(pos-1) + replacePattern + splitText.get(pos+1));
				splitText.remove(pos, pos+2);
			}
		}
		// Split the concatenated result into lines
		return StringList.explode(splitText.concatenate(), "\n");
	}

	/**
	 * Checks whether and how many matches of the configured search criteria are in the
	 * given {@link Element} {@code _ele}.
	 * @param _ele - the Structorizer element to be scrutinized.
	 * @return number of matches within the relevant text fields.
	 */
	private int checkElementMatch(Element _ele)
	{
		int nMatches = 0;
		String elementClass = _ele.getClass().getSimpleName().toUpperCase();
		ElementType type = ElementType.valueOf(elementClass);
		if (chkElementTypes[type.ordinal()].isSelected()) {
			if (chkInTexts.isSelected()) {
				// START KGU#480 2018-01-22: Enh. #490
				//nMatches += textMatches(_ele.getText());
				nMatches += textMatches(_ele.getAliasText());
				// END KGU#480 2018-01-22
			}
			if (chkInComments.isSelected()) {
				nMatches += textMatches(_ele.getComment());
			}
		}
		return nMatches;
	}

	/**
	 * Specific version of {@link #checkElementMatch(Element)} for the {@link #currentElement}
	 * (which must be enabled)
	 * @return numbe of matches in the current element (depending on current target settings)
	 */
	private int checkElementMatch()
	{
		int nMatches = 0;
		if (chkInTexts.isSelected()) {
			nMatches += (partsText.count() - 1) / 2;
		}
		if (chkInComments.isSelected()) {
			nMatches += (partsComment.count() - 1) / 2;
		}
		return nMatches;
	}

	/**
	 * Determines the number of matches in the given {@link StringList} {@code text}
	 * @param text - an element text or comment split into lines.
	 * @return the number of matches
	 */
	private int textMatches(StringList text) {
		int nMatches = 0;
		boolean caseSensi = chkCaseSensitive.isSelected();
		String searchPattern = (String)cmbSearchPattern.getEditor().getItem();
		String brokenText = text.getText();
		if (chkRegEx.isSelected()) {
			//doesMatch = brokenText.matches(searchPattern);
			// START KGU#454 2017-11-03: Bugfix #448 - the pattern might be corrupt!
			//nMatches = brokenText.split(searchPattern, -1).length - 1;
			try {
				nMatches = brokenText.split(searchPattern, -1).length - 1;
			}
			catch (Exception ex) {
				JOptionPane.showMessageDialog(this,
						msgRegexCorrupt.getText().replace("%1", searchPattern).replace("%2", ex.getMessage()),
						ttlSearchError.getText(),
						JOptionPane.ERROR_MESSAGE);
				// TODO: What to do to achieve clean status?
				throw ex;
			}
			// END KGU#454 2017-11-03
		}
		else if (chkWholeWord.isSelected()) {
			// FIXME: Maybe we should rather tokenize the string!?
			String[] words = brokenText.split("\\W+");
			for (String word: words) {
				if (caseSensi && word.equals(searchPattern) || !caseSensi && word.equalsIgnoreCase(searchPattern)) {
					//doesMatch = true;
					//break;
					nMatches++;
				}
			}
		}
		else {
			if (!caseSensi) {
				brokenText = brokenText.toLowerCase();
				searchPattern = searchPattern.toLowerCase();
			}
			//doesMatch = brokenText.contains(searchPattern);
			nMatches = brokenText.split(Pattern.quote(searchPattern), -1).length - 1;
		}
		//return doesMatch;
		return nMatches;
	}

	/**
	 * Item state change listener method for both the pattern combo boxes.
	 * Ensures the new selected item is cached in the history
	 * @param evt - the inducing event
	 */
	protected void patternChanged(ItemEvent evt) {
		Object comp = evt.getSource();
		if (fillingComboBox || !(comp instanceof JComboBox<?>)) return;	// Avoid stack overflow
		
		@SuppressWarnings("unchecked")
		JComboBox<String> box = (JComboBox<String>)comp;
		if (box.isPopupVisible()) {
			// Wait with updates until the popup gets closed. 
			return;
		}
		if (evt.getStateChange() == ItemEvent.SELECTED) {
			updatePatternList(box);
		}
	}

	/**
	 * Updates the respective pattern history for the originating {@link JComboBox} {@code box}
	 * and resets the find results if {@code box} is the combo box for the search patterns.
	 * @param box - the originating {@link JComboBox} 
	 */
	private void updatePatternList(JComboBox<String> box) {
		LinkedList<String> patternList = replacePatterns;
		if (box == cmbSearchPattern) {
			resetResults();
			patternList = searchPatterns;
		}
		String item = (String)box.getEditor().getItem();
		if (box == cmbSearchPattern) {
			chkWholeWord.setEnabled(!chkRegEx.isSelected() && Function.testIdentifier(item, null));
		}
		if (patternList.isEmpty() || !item.equals(patternList.getFirst())) {
			ListIterator<String> iter = patternList.listIterator();
			boolean found = false;
			while (!found && iter.hasNext()) {
				found = item.equals(iter.next());
				if (found) {
					iter.remove();
				}
			}
			if (!found && patternList.size() >= MAX_RECENT_PATTERNS) {
				patternList.removeLast();
			}
			patternList.addFirst(item);
			this.refillPatternCombos(box);
			box.setSelectedItem(item);
		}
	}

	/**
	 * Refreshes the choice list of the given {@link JComboBox} {@code box} from
	 * the cached history for that combobox.
	 * @param _box the target combo-box or null if both comboboxes are to be filled.
	 */
	private void refillPatternCombos(JComboBox<String> _box)
	{
		// Recursion protection
		fillingComboBox = true;
		// Cater for the search pattern box
		if (_box == null || _box == cmbSearchPattern) {
			cmbSearchPattern.removeAllItems();
			cmbSearchPattern.addItem("");
			for (String pattern: searchPatterns) {
				if (!pattern.isEmpty()) {
					cmbSearchPattern.addItem(pattern);
				}
			}
		}
		// Cater for the replace pattern box
		if (_box == null || _box == cmbReplacePattern) {
			cmbReplacePattern.removeAllItems();
			cmbReplacePattern.addItem("");
			for (String pattern: replacePatterns) {
				if (!pattern.isEmpty()) {
					cmbReplacePattern.addItem(pattern);
				}
			}
		}
		fillingComboBox = false;
	}
	
	/**
	 * Updates the visibility or accessibility of the buttons held in the button bar,
	 * depending on the current state.
	 */
	protected void doButtons() {
		boolean anythingSelected = false;
		if (chkElementTypes != null) {
			for (int i = 0; !anythingSelected && i < chkElementTypes.length; i++) {
				anythingSelected = chkElementTypes[i].isSelected();
			}
		}
		if (btnReplace != null)
			btnReplace.setEnabled(currentElement != null);
		if (btnReplaceFind != null)
			btnReplaceFind.setEnabled(currentElement != null);
		if (btnFind != null)
			btnFind.setEnabled(anythingSelected);
		if (btnReplaceAll != null)
			btnReplaceAll.setEnabled(anythingSelected);
	}

	/**
	 * Updates the visibility or accessibility of the search scope ad result controls depending
	 * on the current state
	 */
	protected void doButtonsScope()
	{
		boolean allRoots = cmbScope.getSelectedItem() == Scope.OPENED_DIAGRAMS;
		if (chkRootTypes != null) {
			for (int i = 0; i < chkRootTypes.length; i++) {
				chkRootTypes[i].setEnabled(allRoots);
			}
		}
		if (treResults != null) {
			treResults.setEnabled(allRoots);
		}
	}
	
	/**
	 * Retrieves the matching elements in the given subsequence {@code _scope} and links them to a list.
	 * THe restrieval is restricted to elements of one and the same {@link Root}.
	 * @param _scope - linear subsequence of elements limiting the search scope
	 * @param _deeply - whether the searh is to comprise all substructure (otherwise: flat)
	 * @return ordered list of the elements matching the search criteria
	 */
	private LinkedList<Element> findElements(IElementSequence _scope, boolean _deeply)
	{
		LinkedList<Element> elements = new LinkedList<Element>();
		Iterator iter = _scope.iterator(_deeply);
		while (iter.hasNext()) {
			Element ele = iter.next();
			if (checkElementMatch(ele) > 0) {
				elements.add(ele);
			}
		}
		return elements;
	}

	/**
	 * Has the given {@link Ini} instance {@code ini} save all relevant search criteria and
	 * settings to the associated structorizer.ini file.
	 * @param ini - instance of the {@link Ini} instance
	 */
	public void cacheToIni(Ini ini)
	{
		int i = 1;
		for (String pattern: searchPatterns) {
			ini.setProperty("searchPattern" + i++, pattern);
		}
		i = 1;
		for (String pattern: replacePatterns) {
			ini.setProperty("replacePattern" + i++, pattern);
		}
		
		// Store the mode setting in Ini
		ini.setProperty("findCaseSensitive", chkCaseSensitive.isSelected() ? "1" : "0");
		ini.setProperty("findWholeWord", chkWholeWord.isSelected() ? "1" : "0");
		ini.setProperty("findRegEx", chkRegEx.isSelected() ? "1" : "0");
		ini.setProperty("searchDir", rbUp.isSelected() ? "up" : "down");
		
		// Store the scope settings in Ini
		ini.setProperty("searchScope", ((Scope)cmbScope.getSelectedItem()).name());
		i = 0;
		for (Root.DiagramType type: Root.DiagramType.values()) {
			ini.setProperty("search" + type.name(), chkRootTypes[i++].isSelected() ? "1" : "0"); 
		}
		i = 0;
		for (ElementType type: ElementType.values()) {
			ini.setProperty("find" + type.name(), chkElementTypes[i++].isSelected() ? "1" : "0"); 
		}
		ini.setProperty("findInTexts", chkInTexts.isSelected() ? "1" : "0");
		ini.setProperty("findInComments", chkInTexts.isSelected() ? "1" : "0");
		ini.setProperty("findDisabled", chkDisabled.isSelected() ? "1" : "0");		
	}
	
	/**
	 * Ensures that certain listeners on LaF-specific components don't get lost by a
	 * Look & Feel change.
	 */
	public void adaptToNewLaF()
	{
		if (cmbKeyListener != null) {
			if (cmbSearchPattern != null) {
				cmbSearchPattern.getEditor().getEditorComponent().addKeyListener(cmbKeyListener);
			}
			if (cmbReplacePattern != null) {
				cmbReplacePattern.getEditor().getEditorComponent().addKeyListener(cmbKeyListener);
			}
		}
		pack();
	}

}

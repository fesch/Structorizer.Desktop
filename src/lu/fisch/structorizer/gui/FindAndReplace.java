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
 *      Description:    Find & Replace dialog for Structorizer (according to Enh. Requ. #415)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.05.30      First Issue (#415)
 *      Kay Gürtzig     2017.06.13      Pattern combo boxes with history
 *      Kay Gürtzig     2017.06.17      JTree for multi-Root retrieval
 *      Kay Gürtzig     2017.06.19      Preview size problem solved, inner-element navigation, matching flaws fixed
 *      Kay Gürtzig     2017.06.22      NullPointerException on replacing due to cleared currentNode fixed
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      TODO / FIXME:
 *      - Matching / Replacement with regular expressions ok?
 *      - Update or clear this dialog on heavy changes to the set of available open diagrams
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
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

import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.IElementSequence;
import lu.fisch.structorizer.elements.IElementSequence.Iterator;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangFrame;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 * A dialog providing the usual tools to search a (sub) diagram for Elements matching certain string
 * patterns with the opportunity to replace the text parts by other patterns
 */
@SuppressWarnings("serial")
public class FindAndReplace extends LangFrame /*implements WindowListener*/ {

	private static final int MAX_RECENT_PATTERNS = 10;
	private static final int MAX_PREVIEW_HEIGHT = 75;
	private static final String patternPrototype = "This is just a string long enough to establish a sufficient width for the entire dialog";
	private final LinkedList<String> searchPatterns = new LinkedList<String>();
	private final LinkedList<String> replacePatterns = new LinkedList<String>();
	private boolean fillingComboBox = false;
	private IElementSequence.Iterator treeIterator = null;
	private Element currentElement = null;
	private int currentPosition = -1;		// Position of the match within an element's text or comment
	private final DefaultMutableTreeNode resultTop = new DefaultMutableTreeNode("Search Results");
	private DefaultMutableTreeNode currentNode = null;
	private DefaultTreeModel resultModel = null;

	/**
	 * Allows to formulate sets of interesting element types
	 */
	public enum ElementType { ROOT, INSTRUCTION, ALTERNATIVE, CASE, FOR, WHILE, REPEAT, FOREVER, CALL, JUMP, PARALLEL };
	
	/**
	 * Specifies the search scope
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
	
	private Diagram diagram;
	
	private class MyTreeCellRenderer extends DefaultTreeCellRenderer {

	    public MyTreeCellRenderer() {}

	    public Component getTreeCellRendererComponent(
	                        JTree tree,
	                        Object value,
	                        boolean sel,
	                        boolean expanded,
	                        boolean leaf,
	                        int row,
	                        boolean hasFocus)
	    {
	    	ImageIcon icon = null;
	    	if (value instanceof DefaultMutableTreeNode) {
	    		value = ((DefaultMutableTreeNode)value).getUserObject();
	    	}
	    	Object description = value;
	    	if (value instanceof Element) {
	    		StringList text = ((Element)value).getText();
	    		if (text.count() > 0) {
	    			description = text.get(0);
	    			if (text.count() > 1) {
	    				description += " ...";
	    			}
	    		}
	    		else {
	    			description = "---";
	    		}
	    		icon = ((Element)value).getIcon();
	    	} 
	        super.getTreeCellRendererComponent(
	                        tree, description, sel,
	                        expanded, leaf, row,
	                        hasFocus);
	        if (icon != null) {
	            setIcon(icon);
	        } 

	        return this;
	    }
	};
	// TODO We need:
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
	// FIXME Should there be only one text pane alternatively used for texts or comments? (How to distinguish both?)
	protected JTextPane txtText;			// For the find result / preview, editable
	protected StyledDocument docText = null;	// Copy of the found element text / comment with markers
	protected JTextPane txtComm;			// For the find result / preview, editable
	protected StyledDocument docComm = null;	// Copy of the found element comment with markers
	protected JTree treResults = null;		// Presentation of the search results over a diagram forest
	
	protected JPanel pnlOptions;
	protected JPanel pnlMode;
	protected JPanel pnlRootTypes;
	protected JPanel pnlWherein;
	protected JPanel pnlScope;
	protected JPanel pnlElements;
	protected JPanel pnlResults;
	protected JPanel pnlPreview;
	
	/**
	 * @param owner - commanding Diagram object (needed for selection retrieval etc.)
	 */
	public FindAndReplace(Diagram _diagram) {
		diagram = _diagram;
		initComponents();
	}

	private void initComponents()
	{
		// FIXME: There should rather be buttons FindAll, FindNext, FindPrev, ReplaceNext, ReplaceAll

		this.setIconImage(IconLoader.ico074.getImage());
		Ini ini = Ini.getInstance();
		try {
			ini.load();
		} catch (IOException ex) {
			// Seems ok to ignore this
			ex.printStackTrace();
		}
		double scaleFactor = Double.valueOf(ini.getProperty("scaleFactor","1"));
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
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
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
			cmbSearchPattern.addKeyListener(keyListener);
			cmbSearchPattern.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent evt) {
					patternChanged(evt);
				}
			});
			cmbReplacePattern.setEditable(true);
			cmbReplacePattern.setPrototypeDisplayValue(patternPrototype);
			cmbReplacePattern.addKeyListener(keyListener);
			cmbReplacePattern.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent evt) {
					patternChanged(evt);
				}
			});
			
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
			//gbcPatterns.fill = GridBagConstraints.HORIZONTAL;
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
			
			//this.chkCaseSensitive = new JCheckBox("Case sensitive");
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
			this.rbDown.setMnemonic(java.awt.event.KeyEvent.VK_D);
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
			
			//pnlWherein.setBorder(new TitledBorder("Wherein"));
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
						txtText.setEnabled(chkInTexts.isSelected() && currentElement != null && textMatches(currentElement.getText()) > 0);
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

			pnlElements.setLayout(new BoxLayout(pnlElements, BoxLayout.Y_AXIS));
			pnlElements.setLayout(new GridLayout(0, 1));
			pnlElements.setBorder(new TitledBorder("Element Types"));
			
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
				pnlElements.add(chkElementTypes[i]);
			}
//			pnlOptions.add(pnlElements, BorderLayout.EAST);
			pnlOptionsEast.add(pnlElements);

			JPanel pnlDisabled = new JPanel();
			chkDisabled = new JCheckBox("Disabled elements");
			chkDisabled.setSelected(ini.getProperty("findDisabledElements", "0").equals("1"));
			chkDisabled.addKeyListener(keyListener);
			pnlDisabled.add(chkDisabled);
			pnlOptionsEast.add(pnlDisabled);
			
			pnlOptions.add(pnlOptionsEast, BorderLayout.EAST);
			
			// -------------- Text View ---------------
			
			pnlPreview.setLayout(new GridLayout(0, 1));
			pnlPreview.setBorder(BorderFactory.createTitledBorder("Contents"));

			// This is no good way to enforce maximum height because the layout continues
			// to try to enlarge this on and on...
//			ComponentAdapter textScrollListener = new ComponentAdapter() {
//				@Override
//				public void componentResized(ComponentEvent evt) {
//					Component comp = evt.getComponent();
//					int w = comp.getSize().width;
//					int h = comp.getSize().height;
//					int maxHeight = comp.getMaximumSize().height;
//					System.out.println(comp + " w = " + w + ", h = " + h + ", maxh = " + maxHeight);
//					if (h > maxHeight) {
//						comp.setSize(new Dimension(w, maxHeight));
//						comp.repaint();
//						comp.revalidate();
//					}
//					
//					super.componentResized(evt);
//				}
//			};
			
			txtText = new JTextPane();
			txtText.setBorder(BorderFactory.createTitledBorder("Text"));
			txtText.setEditable(false);
			txtText.setEnabled(false);
			txtText.addKeyListener(keyListener);
	    	JScrollPane scrText = new JScrollPane(txtText);
//	    	txtText.addComponentListener(textScrollListener);
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
//	    	txtComm.addComponentListener(textScrollListener);
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
			// Allow to select an element in the tree
			this.treResults.addTreeSelectionListener(new TreeSelectionListener(){
				@Override
				public void valueChanged(TreeSelectionEvent evt) {
					if (cmbScope.getSelectedItem() == Scope.OPENED_DIAGRAMS) {
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
							if (ele instanceof Root) {
								Arranger.scrollToDiagram((Root)ele, true);
							}
						}
					}
				}});
			this.treResults.setCellRenderer(new MyTreeCellRenderer());
			this.treResults.addKeyListener(keyListener);
			JScrollPane scrTree = new JScrollPane(this.treResults);

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

	protected void clearCurrentElement() {
		Element selected = diagram.getSelected();
		if (currentElement != null) {
			if (selected == null ||
					selected instanceof IElementSequence && ((IElementSequence)selected).getIndexOf(currentElement) >= 0 ||
					!(selected instanceof IElementSequence) && currentElement != selected) {
				currentElement.setSelected(false);
			}
			currentElement = null;
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
	
	private void clearDoc(StyledDocument doc) {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setCurrentElement(Element ele, int positionInElement)
	{
		if (ele != currentElement) {
			clearCurrentElement();
		}
		currentElement = ele;
		currentPosition = positionInElement;
		ele.setSelected(true);
		diagram.redraw(ele);
		//System.out.println(ele);
		int nMatches = 0;
		boolean enable = false;
		if (chkInTexts.isSelected()) {
			nMatches = this.textMatches(ele.getText());
			enable = nMatches > 0;
		}
		fillPreview(ele.getText(), docText, txtText, 0, enable);
		enable = chkInComments.isSelected() && this.textMatches(ele.getComment()) > 0;
		fillPreview(ele.getComment(), docComm, txtComm, nMatches, enable);
		doButtons();
	}
	
	private int fillPreview(StringList stringLst, StyledDocument doc, JTextPane txtPane, int posOffset, boolean enable) {
		int nParts = 0;
		int currPos = currentPosition - posOffset;
		String text0 = stringLst.getText();
		clearDoc(doc);
		txtPane.setEnabled(enable);
		String pattern = (String)cmbSearchPattern.getEditor().getItem();
		if (!enable) {
			// Simply show the text without highlighting etc.
			try {
				doc.insertString(doc.getLength(), text0, doc.getStyle("default"));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		else {
			// In non-regex mode it's relatively simple: We know the exact length
			// of the seeked pattern. The actual match might only differ in case
			// So, having the split parts we can just copy the matches from the
			// resective positions of the original text (text0).
			// For whole word mode its more tricky - we must re-compose the erroneously
			// slit parts first. All this is the task of splitText(). 
			StringList matches = new StringList();
			String[] parts = splitText(text0, pattern, matches);
			nParts = parts.length;
			int emphPos = -1;
			for (int i = 0; i < nParts; i++) {
		    	try {
		    		doc.insertString(doc.getLength(), parts[i], doc.getStyle("default"));
		    		if (i < nParts-1) {
		    			if (i == currPos) {
		    				emphPos = doc.getLength();
		    				doc.insertString(doc.getLength(), matches.get(i), doc.getStyle("emphasis"));
		    			}
		    			else {
		    				if (emphPos < 0) emphPos = doc.getLength();
		    				doc.insertString(doc.getLength(), matches.get(i), doc.getStyle("highlight"));
		    			}
		    		}
		    	} catch (BadLocationException e) {
		    		e.printStackTrace();
		    	}
			}
			if (emphPos > -1) {
				txtPane.setCaretPosition(emphPos);
			}
		}
		return nParts - 1;
	}

	// FIXME: We might cache the split results for the currentElement
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
				if ((part.isEmpty() || part.matches("(\\n|.)*?\\W"))
						&& (i+2 == nParts && nextPart.isEmpty() || nextPart.matches("\\W(\\n|.)*?"))) {
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

	private void resetResults()
	{
		treeIterator = null;
		resultTop.removeAllChildren();
		currentNode = null;
		clearCurrentElement();
		if (treResults != null) {
			resultModel.reload();
			treResults.setEnabled(false);
		}
	}

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

	protected boolean findActionPerformed(ActionEvent evt, boolean replace, boolean gotoNext) {
		boolean done = false;
		boolean up = rbUp.isSelected();
		boolean elementwise = chkElementwise.isSelected();
		Element selected = diagram.getSelected();
		Scope scope = (Scope)cmbScope.getSelectedItem();
		if (scope == Scope.OPENED_DIAGRAMS) {
			if (currentNode == null) {
				fillResultTree();
				gotoNext = false;
				replace = false;
			}
		}
		else if (treeIterator == null) {
			// Reinitialize iterator for incremental search
			if (selected == null || scope == Scope.CURRENT_DIAGRAM) {
				// Now this is a somewhat dirty trick to make sure a matching Root isn't ignored
				int nMatches = checkElementMatch(diagram.getRoot()); 
				if (nMatches > 0) {
					setCurrentElement(diagram.getRoot(), elementwise ? -1 : (up ? nMatches - 1 : 0));
					replace = false;
					gotoNext = false;
				}
				treeIterator = diagram.getRoot().children.iterator(true);
			}
			else if (selected instanceof IElementSequence) {
				treeIterator = ((IElementSequence) selected).iterator(true);
			}
			else if (selected.parent != null) {
				treeIterator = (new SelectedSequence(selected, selected)).iterator(true);
			}
			else if (selected instanceof Root) {
				// Now this is a somewhat dirty trick to make sure a matching Root isn't ignored
				int nMatches = checkElementMatch(selected); 
				if (nMatches > 0) {
					setCurrentElement(selected, elementwise ? -1 : (up ? nMatches - 1 : 0));
					replace = false;
					gotoNext = false;
				}
				treeIterator = ((Root)selected).children.iterator(true);
			}
			// Go to last element if we are to go upwards
			if (treeIterator != null && up) {
				while (treeIterator.hasNext()) {
					treeIterator.next();
				}
			}
		}
		int nMatches = 0;
		if (currentElement != null) {
			nMatches = checkElementMatch(currentElement);
		}
		if (replace && nMatches > 0) {
			// Replace according to the current pattern
			Root root = Element.getRoot(currentElement);
			if (root != null) {
				root.addUndo();
			}
			StringList text = currentElement.getText();
			StringList comment = currentElement.getComment();
//			int matchesSeen = 0;
			int nMatchesComment = textMatches(comment);
			int nMatchesText = textMatches(text);
//			if (up && chkInComments.isSelected()) {
//				if (nMatchesComment >= currentPosition) {
//					comment = replacePattern(comment, elementwise, currentPosition);
//					currentElement.setComment(comment);
//					this.fillPreview(comment, docComm, txtComm, 0);
//					done = true;
//				}
//				matchesSeen += nMatchesComment;
//			}
//			if ((elementwise || !done) && chkInTexts.isSelected()) {
//				if (nMatchesText > currentPosition - matchesSeen) {
//					text = replacePattern(text, elementwise, currentPosition - matchesSeen);
//					currentElement.setText(text);
//					this.fillPreview(text, docText, txtText, matchesSeen);
//					done = true;
//				}
//				matchesSeen += nMatchesText;
//			}
//			if (rbDown.isSelected() && (elementwise || !done) && chkInComments.isSelected()
//					&& nMatchesComment > currentPosition - matchesSeen) {
//				comment = replacePattern(comment, elementwise, currentPosition - matchesSeen);
//				currentElement.setComment(comment);
//				this.fillPreview(comment, docComm, txtComm, matchesSeen);
//				done = true;
//			}
			if ((elementwise || !done) && chkInTexts.isSelected() 
					&& nMatchesText > currentPosition) {
				text = replacePattern(text, elementwise, currentPosition);
				currentElement.setText(text);
				this.fillPreview(text, docText, txtText, 0, true);
				done = true;
			}
			if ((elementwise || !done) && chkInComments.isSelected()
					&& nMatchesComment > currentPosition - nMatchesText) {
				comment = replacePattern(comment, elementwise, currentPosition - nMatchesText);
				currentElement.setComment(comment);
				this.fillPreview(comment, docComm, txtComm, nMatchesText, true);
				done = true;
			}
			if (currentNode != null) {
				// We better cache the current Node locally lest the reload actions should reset it.
				DefaultMutableTreeNode currNode = currentNode;
				resultModel.reload(currentNode);
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currNode.getParent();
				if (parent != null && parent.getUserObject() == currNode.getUserObject()) {
					resultModel.reload(parent);
					currentNode = currNode;
					treResults.setSelectionPath(new TreePath(currentNode.getPath()));
				}
			}
			diagram.doButtons();
			diagram.redraw(currentElement);
			if (done) {
				if (elementwise) {
					nMatches = 0;
				}
				else {
					nMatches--;
					if (!up && currentPosition < nMatches || up && --currentPosition >= 0) {
						gotoNext = false;
					}
				}
			}
		}
		// Is there another matching position within this element?
		if (gotoNext && !elementwise && currentPosition >= 0 && (up && currentPosition > 0 || !up && currentPosition < nMatches-1)) {
			// just update the preview
			if (up) {
				currentPosition--;
			}
			else {
				currentPosition++;
			}
			setCurrentElement(currentElement, currentPosition);
		}
		else if (gotoNext && treeIterator != null) {
			// find the next matching element within the current diagram
			boolean found = false;
			clearCurrentElement();
			if (rbUp.isSelected()) 
				while (!found && treeIterator.hasPrevious()) {
					Element ele = treeIterator.previous();
					nMatches = checkElementMatch(ele);
					if (found = nMatches > 0) {
						setCurrentElement(ele, elementwise ? -1 : (up ? nMatches - 1 : 0));
						updateResultTree();
						done = true;
					}
				}
			else 
				while (!found && treeIterator.hasNext()) {
					Element ele = treeIterator.next();
					nMatches = checkElementMatch(ele);
					if (found = nMatches > 0) {
						setCurrentElement(ele, elementwise ? -1 : (up ? nMatches - 1 : 0));
						updateResultTree();
						done = true;
					}
				}
			if (!found) {
				// Iterator exhausted - drop it
				treeIterator = null;
				updateResultTree();
			}
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
				done = true;
			}
			else {
				clearCurrentElement();
				treResults.clearSelection();
			}
		}
		return done;
	}
	
	private void updateResultTree() {
		if (currentNode == null) {
			this.resultTop.removeAllChildren();
			this.resultModel.reload();
			if (currentElement != null && treeIterator != null) {
				if (treeIterator.hasPrevious()) {
					this.resultModel.insertNodeInto(new DefaultMutableTreeNode("..."), resultTop, resultTop.getChildCount());
				}
				DefaultMutableTreeNode eleNode = new DefaultMutableTreeNode(currentElement); 
				this.resultModel.insertNodeInto(eleNode, resultTop, resultTop.getChildCount());
				if (treeIterator.hasNext()) {
					this.resultModel.insertNodeInto(new DefaultMutableTreeNode("..."), resultTop, resultTop.getChildCount());
				}
				TreePath path = new TreePath(eleNode.getPath());
				treResults.scrollPathToVisible(path);
				treResults.setSelectionPath(path);
				treResults.setEnabled(true);
			}
			else {
				treResults.setEnabled(false);
			}
		}
	}

	/**
	 * Initializes the result tree for scope OPENED_DIAGRAMS
	 * Also sets this.currentNode (if possible)
	 */
	private void fillResultTree() {
		resultTop.removeAllChildren();
		resultModel.reload();
		clearCurrentElement();
		DefaultMutableTreeNode lastNode = null;
		Vector<Root> roots = Arranger.getSortedRoots();
		if (!roots.contains(diagram.getRoot())) {
			roots.add(0, diagram.getRoot());
		}
		for (Root root: roots) {
			LinkedList<Element> elements = this.findElements(root.children, true);
			if (checkElementMatch(root) > 0) {
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
			treResults.setSelectionPath(path);
			treResults.setEnabled(true);
		}
		else {
			treResults.clearSelection();
			treResults.setEnabled(false);
		}
	}

	protected void replaceAllActionPerformed(ActionEvent evt) {
		while (findActionPerformed(evt, true, true));
	}

	private StringList replacePattern(StringList text, boolean all, int pos) {
		String brokenText = text.getText();
		String searchPattern = (String)cmbSearchPattern.getEditor().getItem();
		String replacePattern = (String)cmbReplacePattern.getEditor().getItem();
		String resultText = brokenText;
		boolean caseSensitive = chkCaseSensitive.isSelected();
		boolean isRegex = chkRegEx.isSelected();
		boolean wholeWord = chkWholeWord.isSelected(); 
		if (all) {
			if (!isRegex) {
				if (!caseSensitive) {
					// KGU 2017-06-18: Method breakup now ensures quoting of regex meta symbols
					searchPattern = BString.breakup(searchPattern);
				}
				else {
					searchPattern = Pattern.quote(searchPattern);
				}
				replacePattern = "$1" + Matcher.quoteReplacement(replacePattern) + "$2";
				if (wholeWord) {
					searchPattern = "(^|.*?\\W)" + searchPattern + "($|\\W.*?)";
				}
				else {
					searchPattern = "(.*?)" + searchPattern + "(.*?)";
				}
			}
			resultText = brokenText.replaceAll(searchPattern, replacePattern);
		}
		else {
			//resultText = brokenText.replaceFirst(searchPattern, replacePattern);
			StringList actualMatches = new StringList();
			String[] parts = splitText(brokenText, searchPattern, actualMatches);
			String[] matches = actualMatches.toArray();
			//String[] parts = brokenText.split(searchPattern, -1);
			int nParts = parts.length;
			// Restore all matched substrings.
//			String[] matches = new String[nParts-1];
//			int start = 0;
//			for (int i = 0; i < nParts-1; i++) {
//				String part = parts[i];
//				matches[i] = brokenText.substring(start).replaceFirst("(\\n|.)*?(" + searchPattern + ")(\\n|.)*", "$2");
//				start += part.length() + matches[i].length();
//			}
//			if (wholeWord) {
//				StringList realParts = new StringList();
//				StringList realWords = new StringList();
//				String part = parts[0];
//				for (int i = 0; i < nParts - 1; i++) {
//					String nextPart = parts[i+1];
//					if ((part.isEmpty() || part.matches(".*?\\W"))
//							&& (i+2 == nParts && nextPart.isEmpty() || nextPart.matches("\\W.*?"))) {
//						realParts.add(part);
//						realWords.add(matches[i]);
//						part = nextPart;
//					}
//					else {
//						part += matches[i] + nextPart;
//					}
//				}
//				realParts.add(part);
//				nParts = realParts.count();
//				parts = new String[nParts];
//				matches = new String[realWords.count()];
//				for (int i = 0; i < parts.length; i++) {
//					parts[i] = realParts.get(i);
//				}
//				for (int i = 0; i < matches.length; i++) {
//					matches[i] = realWords.get(i);
//				}
//			}
			// Now we can work properly.
//			if (rbUp.isSelected()) {
//				pos = nParts - 2 - pos;
//			}
			resultText = "";
			for (int i = 0; i < nParts; i++) {
				resultText += parts[i];
				if (i == pos) {
					if (isRegex) {
						resultText += matches[i].replaceFirst(searchPattern, replacePattern);
					}
					else {
						resultText += replacePattern;
					}
				}
				else if (i < nParts - 1) {
					resultText += matches[i];
				}
			}
		}
		return StringList.explode(resultText, "\n");
	}

	private int checkElementMatch(Element _ele)
	{
		int nMatches = 0;
		String elementClass = _ele.getClass().getSimpleName().toUpperCase();
		ElementType type = ElementType.valueOf(elementClass);
		if (chkElementTypes[type.ordinal()].isSelected()) {
			if (chkInTexts.isSelected()) {
				nMatches += textMatches(_ele.getText());
			}
			if (chkInComments.isSelected()) {
				nMatches += textMatches(_ele.getComment());
			}
		}
		return nMatches;
	}

	private int textMatches(StringList text) {
		int nMatches = 0;
		boolean caseSensi = chkCaseSensitive.isSelected();
		String searchPattern = (String)cmbSearchPattern.getEditor().getItem();
		String brokenText = text.getText();
		if (chkRegEx.isSelected()) {
			//doesMatch = brokenText.matches(searchPattern);
			nMatches = brokenText.split(searchPattern, -1).length - 1;
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

	protected void patternChanged(ItemEvent evt) {
		Object comp = evt.getSource();
		if (fillingComboBox || !(comp instanceof JComboBox<?>)) return;	// Avoid stack overflow
		
		@SuppressWarnings("unchecked")
		JComboBox<String> box = (JComboBox<String>)comp;
		LinkedList<String> patternList = replacePatterns;
		if (box == cmbSearchPattern) {
			resetResults();
			patternList = searchPatterns;
		}
		
		if (evt.getStateChange() == ItemEvent.SELECTED) {
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
	}

	private void refillPatternCombos(JComboBox<String> _box)
	{
		fillingComboBox = true;
		if (_box == null || _box == cmbSearchPattern) {
			cmbSearchPattern.removeAllItems();
			cmbSearchPattern.addItem("");
			for (String pattern: searchPatterns) {
				if (!pattern.isEmpty()) {
					cmbSearchPattern.addItem(pattern);
				}
			}
		}
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
	
//	@Override
//	public void windowActivated(WindowEvent arg0) {
//		// TODO Auto-generated method stub
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
//	public void windowClosing(WindowEvent arg0) {
//		// Store the patterns in Ini
//		cacheToIni(Ini.getInstance());
//	}
//
//	@Override
//	public void windowDeactivated(WindowEvent arg0) {
//		// Store the patterns in Ini
//		cacheToIni(Ini.getInstance());
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
//	public void windowOpened(WindowEvent arg0) {
//		// TODO Auto-generated method stub
//		
//	}
	
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

}

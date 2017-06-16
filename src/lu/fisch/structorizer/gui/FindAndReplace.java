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
 *      Description:    Find & Replace dialog for Structorizer
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.05.30      First Issue
 *      Kay Gürtzig     2017.06.13      Pattern combo boxes with history
 *      Kay Gürtzig     2017.06.17      JTree for multi-Root retrieval
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      TODO / FIXME:
 *      - Matching / Replacement with regular expressions
 *      - Find and Replace incrementally within text and comment (via splitting?)
 *      - How to address the alienation effect of the missing result tree in modes CURRENT_SELECTION and
 *        CURRENT_DIAGRAM after having worked in mode OPENED_DIAGRAMS?
 *      - Update or clear this dialog on heavy changes to the set of available open diagrams
 *      - Translation efforts
 *      - Place element icons next to the element type checkboxes (and analogously to the Root type checkboxes)
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JList;
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
import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;

/**
 * @author Kay Gürtzig
 * A diaolog providing the usual tools to search a (sub) diagram for Elements matching certain string
 * patterns with the opportunity to replace the text parts by other patterns
 */
@SuppressWarnings("serial")
public class FindAndReplace extends LangDialog /*implements WindowListener*/ {

	private static final int MAX_RECENT_PATTERNS = 10;
	private static final String patternPrototype = "This is just a string long enough to establish a sufficient width";
	private final LinkedList<String> searchPatterns = new LinkedList<String>();
	private final LinkedList<String> replacePatterns = new LinkedList<String>();
	private boolean fillingComboBox = false;
	private IElementSequence.Iterator treeIterator = null;
	private Element currentElement = null;
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
	public enum Scope { CURRENT_SELECTION, CURRENT_DIAGRAM, OPENED_DIAGRAMS };
	
	private Diagram diagram;
	
	private class MyRenderer extends DefaultTreeCellRenderer {

	    public MyRenderer() {}

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
	protected JCheckBox chkCaseSensitive;
	protected JCheckBox chkWholeWord;	// Not sensible with regex search
	protected JCheckBox chkRegEx;
	protected JCheckBox chkInTexts;
	protected JCheckBox chkInComments;
	protected JCheckBox chkDisabled;
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
	protected JScrollPane scrElements;		// Scroll pane for the search results list lstElements
	protected JList<Element> lstElements;	// Search results, to be represented by icon and first line each
	protected JScrollPane scrRoots;			// Scroll pane for the list lstRoots
	protected JList<Element> lstRoots;		// Owning Roots of the search results if several diagrams are involved
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

//	/**
//	 * @param owner - owning Frame
//	 */
//	public FindAndReplace(Frame owner) {
//		super(owner);
//		initComponents();
//	}
//
//	/**
//	 * @param owner - some kind of Dialog
//	 */
//	public FindAndReplace(Dialog owner) {
//		super(owner);
//		initComponents();
//	}

	/**
	 * @param owner
	 * @param modal
	 */
	public FindAndReplace(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}
	
	private void initComponents()
	{
		// TODO ...

		// BorderLayout:
		// NORTH (GridBagLayout): panel with find and replace pattern
		// CENTER: scrollable BorderLayout with:
		// * NORTH: GridBagLayout for options:
		//   - case-sensitive, whole word, regex
		// * WEST:
		//   - scope (plus three Root types)
		//   - wherein (text/comments)
		// * EAST (GridLayout): 
		//   - element types (EAST?)
		// * SOUTH: Results
		//   - lstRoots (diagrams)
		//   - lstElements
		// SOUTH: GridBagLayout with
		// * txtPane
		// * buttons
		// FIXME: There should rather be buttons FindAll, FindNext, FindPrev, ReplaceNext, ReplaceAll
		
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
		

		//=================== PATTERNS ========================
		{
			lblSearchPattern = new JLabel("Find:");
			lblReplacePattern = new JLabel("Replace with:");
			cmbSearchPattern = new JComboBox<String>();
			cmbReplacePattern = new JComboBox<String>();
			cmbSearchPattern.setEditable(true);
			cmbSearchPattern.setPrototypeDisplayValue(patternPrototype);
			cmbSearchPattern.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent evt) {
					patternChanged(evt);
				}
			});
			cmbReplacePattern.setEditable(true);
			cmbReplacePattern.setPrototypeDisplayValue(patternPrototype);
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
			
			this.chkCaseSensitive = new JCheckBox("Case sensitive");
			this.chkCaseSensitive.setMnemonic(java.awt.event.KeyEvent.VK_C);
			this.chkCaseSensitive.setSelected(ini.getProperty("findCaseSensitive", "0").equals("1"));
			pnlMode.add(chkCaseSensitive);
			
			this.chkWholeWord = new JCheckBox("Whole word");
			this.chkWholeWord.setMnemonic(java.awt.event.KeyEvent.VK_W);
			this.chkWholeWord.setSelected(ini.getProperty("findWholeWord", "0").equals("1"));
			pnlMode.add(chkWholeWord);

			this.chkRegEx = new JCheckBox("Regular expressions");
			this.chkRegEx.setMnemonic(java.awt.event.KeyEvent.VK_X);
			this.chkRegEx.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					Boolean deselected = evt.getStateChange() == ItemEvent.DESELECTED;
					chkCaseSensitive.setEnabled(deselected);
					chkWholeWord.setEnabled(deselected && Function.testIdentifier((String)cmbSearchPattern.getEditor().getItem(), null));
				}});
			this.chkRegEx.setSelected(ini.getProperty("findRegEx", "0").equals("1"));
			pnlMode.add(chkRegEx);
			
			JPanel pnlDirection = new JPanel();
			pnlDirection.setLayout(new GridLayout(1, 0));
			pnlDirection.add(rbDown);
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
			
			//JPanel pnlScopeCombo = new JPanel();
			cmbScope = new JComboBox<Scope>();
			for (Scope item: Scope.values()) {
				cmbScope.addItem(item);
			}
			cmbScope.addItemListener(scopeListener);
			String lastScopeName = ini.getProperty("searchScope", Scope.CURRENT_DIAGRAM.name());
			Scope lastScope = Scope.valueOf(lastScopeName); 
			if (lastScope != null) {
				cmbScope.setSelectedItem(lastScope);
			}
			//pnlScopeCombo.add(cmbScope);
			//pnlScope.add(pnlScopeCombo);
			//gblScope.setConstraints(cmbScope, gbcScope);
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
				pnlRootTypes.add(chkRootTypes[i]);
			}
			gbcScope.gridy++;
			gbcScope.insets.top = 0;
			//gblScope.setConstraints(pnlRootTypes, gbcScope);
			pnlScope.add(pnlRootTypes, gbcScope);
			
			//pnlWherein.setBorder(new TitledBorder("Wherein"));
			pnlWherein.setBorder(BorderFactory.createEtchedBorder());
			pnlWherein.setLayout(new BoxLayout(pnlWherein, BoxLayout.Y_AXIS));
			chkInTexts = new JCheckBox("In texts");
			chkInTexts.setSelected(!ini.getProperty("findInTexts", "1").equals("0"));
			pnlWherein.add(chkInTexts);
//			gbcScope.gridy++;
//			gbcScope.insets.top = inset;
//			gbcScope.insets.bottom = 0;
//			pnlScope.add(chkInTexts, gbcScope);
			
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
				}};
			chkInTexts.addItemListener(whereinListener);
			chkInComments.addItemListener(whereinListener);
			
//			gbcScope.gridy++;
//			gbcScope.insets.top = 0;
//			gbcScope.insets.bottom = inset;
//			pnlScope.add(chkInComments, gbcScope);
			gbcScope.gridy++;
			gbcScope.insets.top = inset;
			//gblScope.setConstraints(pnlWherein, gbcScope);
			pnlScope.add(pnlWherein, gbcScope);
			
//			chkDisabled = new JCheckBox("Disabled elements");
//			chkDisabled.setSelected(ini.getProperty("findDisabledElements", "0").equals("1"));
//			gbcScope.gridy++;
//			gbcScope.insets.top = 0;
//			pnlScope.add(chkDisabled, gbcScope);	
			
			pnlOptionsWest.add(pnlScope);
			
			pnlOptions.add(pnlOptionsWest, BorderLayout.WEST);
//			pnlOptions.add(pnlScope, BorderLayout.WEST);

			// -------------- ELEMENT TYPES -------------
			
			JPanel pnlOptionsEast = new JPanel();
			pnlOptionsEast.setLayout(new BoxLayout(pnlOptionsEast, BoxLayout.Y_AXIS));

//			pnlElements.setLayout(new BoxLayout(pnlElements, BoxLayout.Y_AXIS));
			pnlElements.setLayout(new GridLayout(0, 1));
			pnlElements.setBorder(new TitledBorder("Element Types"));
			
			btnAll = new JButton("All");
			btnAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent arg0) { selectAllElementTypes(true); }});
			btnNone = new JButton("None");
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
				pnlElements.add(chkElementTypes[i]);
			}
//			pnlOptions.add(pnlElements, BorderLayout.EAST);
			pnlOptionsEast.add(pnlElements);

			JPanel pnlDisabled = new JPanel();
			chkDisabled = new JCheckBox("Disabled elements");
			chkDisabled.setSelected(ini.getProperty("findDisabledElements", "0").equals("1"));
			pnlDisabled.add(chkDisabled);
			pnlOptionsEast.add(pnlDisabled);
			
			pnlOptions.add(pnlOptionsEast, BorderLayout.EAST);
			
			// -------------- Text View ---------------
			
			pnlPreview.setLayout(new GridLayout(0, 1));
			pnlPreview.setBorder(BorderFactory.createTitledBorder("Preview"));
			
			txtText = new JTextPane();
			txtText.setBorder(BorderFactory.createTitledBorder("Text"));
	    	JScrollPane scrText = new JScrollPane(txtText);
	    	docText = txtText.getStyledDocument();
	    	//Style defStyle = doc.getStyle("default");
    		Style hilStyle = docText.addStyle("highlight", null);
    		hilStyle.addAttribute(StyleConstants.Background, Color.YELLOW);
    		
    		pnlPreview.add(scrText);

    		txtComm = new JTextPane();
			txtComm.setBorder(BorderFactory.createTitledBorder("Comment"));
	    	JScrollPane scrComm = new JScrollPane(txtComm);
	    	docComm = txtComm.getStyledDocument();
	    	//Style defStyle = doc.getStyle("default");
    		hilStyle = docComm.addStyle("highlight", null);
    		hilStyle.addAttribute(StyleConstants.Background, Color.YELLOW);
    		
    		pnlPreview.add(scrComm);
    		pnlOptions.add(pnlPreview, BorderLayout.SOUTH);
			
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
					currentNode = (DefaultMutableTreeNode)treResults.getLastSelectedPathComponent();
					if (currentNode != null && currentNode.isLeaf()) {
						Object ele = currentNode.getUserObject();
						if (ele instanceof Element) {
							setCurrentElement((Element)ele);
						}
					}
				}});
			this.treResults.setCellRenderer(new MyRenderer());
			JScrollPane scrTree = new JScrollPane(this.treResults);

			pnlOptions.add(scrTree, BorderLayout.CENTER);
		}
		
		//=================== BUTTONS ========================
		{
		
			pnlButtons.setLayout(new GridLayout(0, 4));
			
			btnFind = new JButton("Find");
			btnFind.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					findActionPerformed(evt, false, true);
				}
			});
			pnlButtons.add(btnFind);
			
			btnReplaceFind = new JButton("Replace/Find");
			btnReplaceFind.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					findActionPerformed(evt, true, true);
				}
			});
			pnlButtons.add(btnReplaceFind);

			btnReplace = new JButton("Replace");
			btnReplace.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					findActionPerformed(evt, true, false);
				}
			});
			pnlButtons.add(btnReplace);

			btnReplaceAll = new JButton("Replace All");
			btnReplaceAll.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					replaceAllActionPerformed(evt);
				}
			});
			pnlButtons.add(btnReplaceAll);
			
			btnClose = new JButton("Close");
			btnClose.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent evt) {
					closeActionPerformed(evt);
				}
				
			});
			pnlButtons.add(btnClose);

			contentPane.add(pnlButtons, BorderLayout.SOUTH);

		}
		
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

	private void setCurrentElement(Element ele)
	{
		if (ele != currentElement) {
			clearCurrentElement();
		}
		currentElement = ele;
		ele.setSelected(true);
		diagram.redraw(ele);
		//System.out.println(ele);
		if (chkInTexts.isSelected() && this.textMatches(ele.getText())) {
			fillPreview(ele.getText(), docText, txtText);
		}
		if (chkInComments.isSelected() && this.textMatches(ele.getComment())) {
			fillPreview(ele.getComment(), docComm, txtComm);
		}
		doButtons();
	}
	
	private void fillPreview(StringList stringLst, StyledDocument doc, JTextPane txtPane) {
		String text0 = stringLst.getText();
		clearDoc(doc);
		if (!chkRegEx.isSelected()) {
			String pattern = (String)cmbSearchPattern.getEditor().getItem();
			int patternLgth = pattern.length();
			String splitter = pattern;
			String text = text0;
			if (!chkCaseSensitive.isSelected()) {
				text = text0.toLowerCase();
				splitter = pattern.toLowerCase();
			}
			splitter = Pattern.quote(splitter);
			String[] parts = text.split(splitter, -1);
			int start = 0;
			for (int i = 0; i < parts.length; i++) {
				String part = text0.substring(start, start+parts[i].length());
		    	try {
		    		doc.insertString(doc.getLength(), part, doc.getStyle("default"));
		    		if (i < parts.length-1) {
		    			doc.insertString(doc.getLength(), pattern, doc.getStyle("highlight"));
		    		}
		    	} catch (BadLocationException e) {
		    		// TODO Auto-generated catch block
		    		e.printStackTrace();
		    	}
				start += part.length() + patternLgth;
			}
		}
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
				if (checkElementMatch(diagram.getRoot())) {
					setCurrentElement(diagram.getRoot());
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
				if (checkElementMatch(selected)) {
					setCurrentElement(selected);
					replace = false;
					gotoNext = false;
				}
				treeIterator = ((Root)selected).children.iterator(true);
			}
			if (treeIterator != null && rbUp.isSelected()) {
				while (treeIterator.hasNext()) {
					treeIterator.next();
				}
			}
		}
		if (replace && currentElement != null) {
			// TODO replace according to the current pattern
			Root root = Element.getRoot(currentElement);
			if (checkElementMatch(currentElement)) {
				root.addUndo();
			}
			if (chkInTexts.isSelected()) {
				StringList text = replacePattern(currentElement.getText(), true);
				currentElement.setText(text);
				this.fillPreview(text, docText, txtText);
				done = true;
			}
			if (chkInComments.isSelected()) {
				StringList comment = replacePattern(currentElement.getComment(), true);
				currentElement.setComment(comment);
				this.fillPreview(comment, docComm, txtComm);
				done = true;
			}
			if (currentNode != null) {
				resultModel.reload(currentNode);
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentNode.getParent();
				if (parent != null && parent.getUserObject() == currentNode.getUserObject()) {
					resultModel.reload(parent);
					treResults.setSelectionPath(new TreePath(currentNode.getPath()));
				}
			}
			diagram.doButtons();
			diagram.redraw(currentElement);
		}
		if (gotoNext && treeIterator != null) {
			// find the next matching element within the current diagram
			boolean found = false;
			clearCurrentElement();
			if (rbUp.isSelected()) 
				while (!found && treeIterator.hasPrevious()) {
					Element ele = treeIterator.previous();
					if (found = checkElementMatch(ele)) {
						setCurrentElement(ele);
						done = true;
					}
				}
			else 
				while (!found && treeIterator.hasNext()) {
					Element ele = treeIterator.next();
					if (found = checkElementMatch(ele)) {
						setCurrentElement(ele);
						done = true;
					}
				}
			if (!found) {
				// Iterator exhausted - drop it
				treeIterator = null;
			}
		}
		else if (gotoNext && currentNode != null) {
			// got to the next element within in the search result tree
			if (rbUp.isSelected()) {
				currentNode = currentNode.getPreviousLeaf();
			}
			else {
				currentNode = currentNode.getNextLeaf();
			}
			if (currentNode != null) {
				setCurrentElement((Element)currentNode.getUserObject());
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
			if (checkElementMatch(root)) {
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

	private StringList replacePattern(StringList text, boolean all) {
		String brokenText = text.getText();
		String searchPattern = (String)cmbSearchPattern.getEditor().getItem();
		String replacePattern = (String)cmbReplacePattern.getEditor().getItem();
		String resultText = brokenText;
		if (!chkRegEx.isSelected()) {
			if (!chkCaseSensitive.isSelected()) {
				// FIXME We must solve the case-insensitive replacement!
				// Unfortunately this is not compatible with quoting
				searchPattern = BString.breakup(searchPattern);
			}
			else {
				searchPattern = Pattern.quote(searchPattern);
			}
			replacePattern = "$1" + Matcher.quoteReplacement(replacePattern) + "$2";
			if (chkWholeWord.isSelected()) {
				searchPattern = "(^|.*?\\W)" + searchPattern + "($|\\W.*?)";
			}
			else {
				searchPattern = "(.*?)" + searchPattern + "(.*?)";
			}
		}
		if (all) {
			resultText = brokenText.replaceAll(searchPattern, replacePattern);
		}
		else {
			resultText = brokenText.replaceFirst(searchPattern, replacePattern);
		}
		return StringList.explode(resultText, "\n");
	}

	private boolean checkElementMatch(Element _ele)
	{
		String elementClass = _ele.getClass().getSimpleName().toUpperCase();
		ElementType type = ElementType.valueOf(elementClass);
		if (!chkElementTypes[type.ordinal()].isSelected()) {
			return false;
		}
		return (chkInTexts.isSelected() && textMatches(_ele.getText()) || chkInComments.isSelected() && textMatches(_ele.getComment()));
	}

	private boolean textMatches(StringList text) {
		boolean doesMatch = false;
		boolean caseSensi = chkCaseSensitive.isSelected();
		String searchPattern = (String)cmbSearchPattern.getEditor().getItem();
		String brokenText = text.getText();
		if (chkRegEx.isSelected()) {
			doesMatch = brokenText.matches(searchPattern);
		}
		else if (chkWholeWord.isSelected()) {
			// FIXME: Maybe we should rather tokenize the string!?
			String[] words = brokenText.split("\\W+");
			for (String word: words) {
				if (caseSensi && word.equals(searchPattern) || !caseSensi && word.equalsIgnoreCase(searchPattern)) {
					doesMatch = true;
					break;
				}
			}
		}
		else {
			if (!caseSensi) {
				brokenText = brokenText.toLowerCase();
				searchPattern = searchPattern.toLowerCase();
			}
			doesMatch = brokenText.contains(searchPattern);
		}
		return doesMatch;
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

	protected void closeActionPerformed(ActionEvent evt) {
		this.setVisible(false);
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
		if (!allRoots && treResults != null) {
			treResults.setEnabled(false);
		}
	}
	
	private LinkedList<Element> findElements(IElementSequence _scope, boolean _deeply)
	{
		LinkedList<Element> elements = new LinkedList<Element>();
		Iterator iter = _scope.iterator(_deeply);
		while (iter.hasNext()) {
			Element ele = iter.next();
			if (checkElementMatch(ele)) {
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

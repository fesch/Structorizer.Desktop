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
package lu.fisch.structorizer.arranger;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    A locale-aware JTree subclass handling the bundling the arranger index features.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-01-11      First Issue
 *      Kay Gürtzig     2019-01-12      Group colour update notification
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2019-01-11 (Kay Gürtzig)
 *      - This class comprises the different Arranger-related aspects that had slowly invaded the
 *        lu.fisch.structorizer.gui.Editor class. It is a kind of bridge between the Mainform/Editor/Diagram
 *        and the Arranger/Surface worlds. 
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.executor.IRoutinePoolListener;
import lu.fisch.structorizer.gui.ColorButton;
import lu.fisch.structorizer.gui.Diagram;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.locales.LangTree;
import lu.fisch.utils.StringList;

/**
 * A locale-aware JTree subclass presenting all diagrams group by group in a tree, also offering
 * many info and manipulating opportunities for groups and diagrams.
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class ArrangerIndex extends LangTree implements MouseListener {
	
	private Diagram diagram = null;
	private final DefaultMutableTreeNode arrangerIndexTop;
	//protected final JTree arrangerIndex = new JTree(arrangerIndexTop);
	private final HashSet<DefaultMutableTreeNode> expandedGroupNodes = new HashSet<DefaultMutableTreeNode>();
	/** Original (standard) Arranger index background color  - may get wrong with an L&F change! */
	private Color arrangerIndexBackground = null;
	private static final Color ARRANGER_INDEX_UNFOCUSSED_BACKGROUND = Color.LIGHT_GRAY;

	// START KGU#318 2017-01-05: Enh. #319 - context menu for the Arranger index
	protected final JPopupMenu popupIndex = new JPopupMenu();
	protected final JMenuItem popupIndexGet = new JMenuItem("Get diagram", IconLoader.getIcon(0));
	protected final JMenuItem popupIndexSave = new JMenuItem("Save changes", IconLoader.getIcon(3));
	// START KGU#534 2018-06-27: Enh. #552
	//protected final JMenuItem popupIndexRemove = new JMenuItem("Remove", IconLoader.getIcon(45));
	protected final JMenuItem popupIndexRemove = new JMenuItem("Remove", IconLoader.getIcon(100));
	protected final JMenuItem popupIndexRemoveAll = new JMenuItem("Remove all", IconLoader.getIcon(45));    
	// END KGU#534 2018-06-27
	protected final JMenuItem popupIndexCovered = new JMenuItem("Test-covered on/off", IconLoader.getIcon(46));
	// END KGU#318 2017-01-05
	// START KGU#573 2018-09-13: Enh. #590 - allow to open attribute inspector
	protected final JMenuItem popupIndexAttributes = new JMenuItem("Inspect attributes ...", IconLoader.getIcon(86));
	// END KGU#573 2018-09-13
	// START KGU#626 2019-01-03: Enh. #657
	protected final JMenuItem popupIndexGroup = new JMenuItem("Create group ...", IconLoader.getIcon(94));
	protected final JMenuItem popupIndexExpandGroup = new JMenuItem("Expand group ...", IconLoader.getIcon(117));
	protected final JMenuItem popupIndexDissolve = new JMenuItem("Dissolve group", IconLoader.getIcon(97));
	protected final JMenuItem popupIndexDetach = new JMenuItem("Detach from group", IconLoader.getIcon(98));
	protected final JMenuItem popupIndexAttach = new JMenuItem("Add/move to group ...", IconLoader.getIcon(116));
	protected final JMenuItem popupIndexInfo = new JMenuItem("Diagram info ...", IconLoader.getIcon(118));

	protected final JLabel lblSelectTargetGroup = new JLabel("Select the target group:");
	protected final JComboBox<Group> cmbTargetGroup = new JComboBox<Group>();
	protected final JPanel pnlGroupSelect = new JPanel();

	protected final DefaultMutableTreeNode nodeIndexInfoTop = new DefaultMutableTreeNode();
	protected final JTree indexInfoTree = new JTree(nodeIndexInfoTop);
	protected final JScrollPane scrollInfo = new JScrollPane(indexInfoTree);
	protected final JLabel lblGroups = new JLabel("Containing groups");
	protected final JLabel lblSubroutines = new JLabel("Called subroutines");
	protected final JLabel lblIncludables = new JLabel("Referenced includables");
	protected final JLabel lblStaleReferences = new JLabel("Stale diagram references");
	protected final DefaultMutableTreeNode nodeGroups = new DefaultMutableTreeNode(lblGroups);
	protected final DefaultMutableTreeNode nodeSubroutines = new DefaultMutableTreeNode(lblSubroutines);
	protected final DefaultMutableTreeNode nodeIncludables = new DefaultMutableTreeNode(lblIncludables);
	protected final DefaultMutableTreeNode nodeStaleReferences = new DefaultMutableTreeNode(lblStaleReferences);
	// END KGU#626 2019-01-03
	protected final DefaultMutableTreeNode nodeIndexGroupInfoTop = new DefaultMutableTreeNode();
	protected final JTree indexGroupInfoTree = new JTree(nodeIndexGroupInfoTop);
	protected final JScrollPane scrollGroupInfo = new JScrollPane(indexGroupInfoTree);
	protected final JLabel lblArrangementPath = new JLabel();
	protected final JLabel lblModifications = new JLabel("Modifications");
	protected final JLabel lblCompleteness = new JLabel();
	protected final JLabel lblExternSubroutines = new JLabel("Referenced external subroutines");
	protected final JLabel lblExternIncludables = new JLabel("Referenced external includables");
	protected final JButton[] btnGroupColors = new JButton[Group.groupColors.length];
	protected final JToggleButton btnShowGroup = new JToggleButton(IconLoader.getIcon(17));
	protected final JPanel pnlGroupInfo = new JPanel();
	protected final DefaultMutableTreeNode nodeArrangementPath = new DefaultMutableTreeNode(lblArrangementPath);
	protected final DefaultMutableTreeNode nodeElementNumbers = new DefaultMutableTreeNode();
	protected final DefaultMutableTreeNode nodeModifications = new DefaultMutableTreeNode(lblModifications);
	protected final DefaultMutableTreeNode nodeCompleteness = new DefaultMutableTreeNode(lblCompleteness);
	protected final DefaultMutableTreeNode nodeExternSubroutines = new DefaultMutableTreeNode(lblExternSubroutines);
	protected final DefaultMutableTreeNode nodeExternIncludables = new DefaultMutableTreeNode(lblExternIncludables);
	protected final DefaultMutableTreeNode nodeDeafReferences = new DefaultMutableTreeNode(lblStaleReferences);
	private static final ImageIcon greenIcon = IconLoader.generateIcon(Color.GREEN, 2);
	private static final ImageIcon redIcon = IconLoader.generateIcon(Color.RED, 2);
	protected final ActionListener colorGroupButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			Object button = evt.getSource();
			Object group = nodeIndexGroupInfoTop.getUserObject();
			if (button instanceof ColorButton && group instanceof Group) {
				((Group)group).setColor(((ColorButton)button).getColor());
				indexGroupInfoTree.repaint();
				Arranger.getInstance().routinePoolChanged(null, IRoutinePoolListener.RPC_GROUP_COLOR_CHANGED);
			}
		}
	};
	// END KGU#630 2019-01-07
	
	// START KGU#626 2019-01-01: Enh. #657
	public static final LangTextHolder msgDefaultGroupName = new LangTextHolder("(Default Group)");
	protected static final LangTextHolder msgGroupsAndRootsSelected = new LangTextHolder("Both groups and diagrams selected. Removing on both levels at a time may have unexpected results.");
	protected static final LangTextHolder msgDeleteGroupMembers = new LangTextHolder("You are going to delete % groups.\n\nThose member diagrams of them that are shared by other groups will survive.\nWhat about diagrams not shared by other groups: Remove from Arranger?\n(Otherwise they would be moved to the default group.)");
	protected static final LangTextHolder msgConfirmDeleteRoots = new LangTextHolder("You selected % diagrams to be removed\n\nDo you really intend to remove them from all groups and Arranger?\n(Otherwise they would just be detached from the respective group.)");
	protected static final LangTextHolder[] msgAttachOptions = new LangTextHolder[] {
			new LangTextHolder("Add to group"),
			new LangTextHolder("Move to group"),
			new LangTextHolder("Cancel")
	};
	protected static final LangTextHolder msgNumberOfSharedMembers = new LangTextHolder("% members shared with other groups");
	protected static final LangTextHolder msgMembersIncomplete = new LangTextHolder("Group is incomplete: %1 referenced diagrams outside group, %2 stale references");
	protected static final LangTextHolder msgMembersComplete = new LangTextHolder("Group is complete: No outward references");
	protected static final LangTextHolder msgGroupMembersChanged = new LangTextHolder("The set of member diagrams was modified.");
	protected static final LangTextHolder msgGroupMembersMoved = new LangTextHolder("The coordinates of some member diagrams were changed.");
	
	
	public static class ArrangerIndexCellRenderer extends DefaultTreeCellRenderer {
		private final static ImageIcon mainIcon = IconLoader.getIcon(22);
		private final static ImageIcon subIcon = IconLoader.getIcon(21);
		private final static ImageIcon subIconCovered = IconLoader.getIcon(30);
		private final static ImageIcon mainIconCovered = IconLoader.getIcon(70);
		private final static ImageIcon inclIcon = IconLoader.getIcon(71);
		private final static ImageIcon inclIconCovered = IconLoader.getIcon(72);
		//private final static Color selectedBackgroundNimbus = new Color(57,105,138);

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean isLeaf, int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, isSelected, expanded, isLeaf, row, hasFocus);
			Object content = ((DefaultMutableTreeNode)value).getUserObject();
			if (content instanceof Root) {
				Root root = (Root)content;
				String s = root.getSignatureString(true);
				boolean covered = Element.E_COLLECTRUNTIMEDATA && root.deeplyCovered; 
				setText(s);
				// Enh. #319, #389: show coverage status of (imported) main diagrams
				if (root.isProgram()) {
					setIcon(covered ? mainIconCovered : mainIcon);
				}
				else if (root.isSubroutine()) {
					setIcon(covered ? subIconCovered : subIcon);
				}
				else if (root.isInclude()) {
					setIcon(covered ? inclIconCovered : inclIcon);
				}
			}
			else if (content instanceof Group)
			{
				Group group = (Group)content;
				setText(group.toString().replace(Group.DEFAULT_GROUP_NAME, msgDefaultGroupName.getText()));
				setIcon(group.getIcon(true));
			}
			else if (content instanceof JLabel) {
				setText(((JLabel)content).getText());
				Icon icon = ((JLabel)content).getIcon();
				if (icon != null) {
					setIcon(icon);
				}
			}
			return this;
		}	
	}
	// END KGU#626 2019-01-01

	// START KGU#305 2016-12-15: Enh. #305 - diagramIndex should react to keys
	private class ArrangerIndexAction extends AbstractAction
	{
		
		ArrangerIndexAction(boolean isDoubleClick)
		{
			super(isDoubleClick ? "DOUBLE_CLICK" : "SINGLE_CLICK");	// KGU#564 2018-07-27: Bugfix #568 (mis-spelled action name)
		}
		
		// START KGU#305 2016-12-17: Also allow to remove a diagram from Arranger
		ArrangerIndexAction(String keyString)
		{
			super(keyString);
		}
		// END KGU#305 2016-12-17
		
		@Override
		public void actionPerformed(ActionEvent ev) {
			// TODO - Find an equivalent for JTree
			Object name = getValue(AbstractAction.NAME);
			if (name.equals("SINGLE_CLICK")) {
				// START KGU#626 2019-01-04: Enh. #657 - different handling in JTree than in JList
				//Arranger.scrollToDiagram(diagramIndex.getSelectedValue(), true);
				TreePath[] paths = getSelectionPaths();
				if (paths != null && paths.length == 1) {
					Object selectedObject = ((DefaultMutableTreeNode)paths[0].getLastPathComponent()).getUserObject();
					if (selectedObject instanceof Root) {
						Arranger.scrollToDiagram((Root)selectedObject, true);
					}
					else if (selectedObject instanceof Group) {
						Arranger.scrollToGroup((Group)selectedObject);
					}
				}
				// END KGU#626 2019-01-04
			}
			// START KGU#305 2016-12-17: Also allow to remove a diagram from Arranger
			//else {
			else if (name.equals("DOUBLE_CLICK")) {
			// END KGU#305 2016-12-17
				arrangerIndexGet();
			}
			// START KGU#626 2019-01-04: Enh. #657
			else if (name.equals("ALT_ENTER")) {
				arrangerIndexAttributes();
			}
			else if (name.equals("MAKE_GROUP")) {
				arrangerIndexMakeGroup(false);
			}
			else if (name.equals("MAKE_COMPLETE_GROUP")) {
				arrangerIndexMakeGroup(true);
			}
			// END KGU#626 2019-01-04
			// START KGU#305 2016-12-17: Also allow to remove a diagram from Arranger
			else if (name.equals("DELETE") && Arranger.hasInstance()) {
				arrangerIndexRemove();
			}
			// END KGU#305 2016-12-17
			// START KGU#626 2019-01-05: Enh. #657
			else if (name.equals("SHOW_INFO")) {
				arrangerIndexInfo();
			}
			else if (name.equals("DETACH")) {
				arrangerIndexDetachFromGroup();
			}
			else if (name.equals("ATTACH")) {
				arrangerIndexAttachToGroup();
			}
			else if (name.equals("DISSOLVE")) {
				arrangerIndexDissolveGroup();
			}
			// END KGU#626 2019-01-05
		}
	}
	// END KGGU#305 2016-12-15

	// PopupListener Methods
	class PopupListener extends MouseAdapter 
	{
		@Override
		public void mousePressed(MouseEvent e) 
		{
			showPopup(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) 
		{
			showPopup(e);
		}
		
		private void showPopup(MouseEvent e) 
		{
			if (e.isPopupTrigger()) 
			{
				// START KGU#318 2017-01-05: Enh. #319
				//popup.show(e.getComponent(), e.getX(), e.getY());
				if (e.getComponent() == ArrangerIndex.this) {
					if (isSelectionEmpty()) {
						TreePath path = getClosestPathForLocation(e.getX(), e.getY());
						if (path != null) {
							addSelectionPath(path);
						}
					}
					doButtonsLocal();
					requestFocusInWindow();
					popupIndex.show(e.getComponent(), e.getX(), e.getY());
				}
				// END KGU#318 2017-01-05
			}
		}
	}

	/////////////////// Constructor ///////////////////////
	/**
	 * Sets up an ArrangerIndex instance with reference to the passed-in {@link Diagram} {@code _diagram}.
	 * @param _diagram
	 */
	public ArrangerIndex(Diagram _diagram) {
		super();
		diagram = _diagram;
		arrangerIndexTop = ((DefaultMutableTreeNode)this.getModel().getRoot());
		create();
	}

	/**
	 * Sets up the Arranger index (held in fields {@link Editor#scrollIndex}, {@link Editor#arrangerIndex}).
	 */
	private void create() {
		for (int i = 0; i < Group.groupColors.length; i++) {
			btnGroupColors[i] = new ColorButton(Group.groupColors[i]);
			btnGroupColors[i].addActionListener(colorGroupButtonListener);
		}
		btnShowGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Object group = nodeIndexGroupInfoTop.getUserObject();
				if (evt.getSource() == btnShowGroup && group instanceof Group) {
					((Group)group).setVisible(btnShowGroup.isSelected());
					indexGroupInfoTree.repaint();
					Arranger.getInstance().routinePoolChanged(null, IRoutinePoolListener.RPC_GROUP_COLOR_CHANGED);
				}
			}
		});
		pnlGroupInfo.setLayout(new BorderLayout());
		pnlGroupInfo.add(this.scrollGroupInfo, BorderLayout.CENTER);
		JPanel buttonBar = new JPanel();
		buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));
		buttonBar.add(this.btnShowGroup);
		for (int i = 0; i < btnGroupColors.length; i++) {
			buttonBar.add(btnGroupColors[i]);
		}
		pnlGroupInfo.add(buttonBar, BorderLayout.SOUTH);
		
		arrangerIndexBackground = this.getBackground();
		this.setRootVisible(false);
		this.setCellRenderer(new ArrangerIndexCellRenderer());
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);		
		this.addMouseListener(this);
		this.addMouseListener(new PopupListener());
		this.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				TreePath path = event.getPath();
				// Make sure it is at group level (just in case...)
				if (path.getPathCount() == 2) {
					expandedGroupNodes.add((DefaultMutableTreeNode)path.getLastPathComponent());
				}
			}
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				TreePath path = event.getPath();
				// Make sure it is at group level (just in case...)
				if (path.getPathCount() == 2) {
					expandedGroupNodes.remove((DefaultMutableTreeNode)path.getLastPathComponent());
				}
			}});
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent event) {
				setBackground(arrangerIndexBackground);
			}
			@Override
			public void focusLost(FocusEvent event) {
				setBackground(ARRANGER_INDEX_UNFOCUSSED_BACKGROUND);
			}});
		this.setShowsRootHandles(true);
		// START KGU#305 2016-12-15: Enh. #305 - react to space and enter
		// START KGU#626 2019-01-04: Enh. #657 - didn't work any longer for JTree
		InputMap inpMap = this.getInputMap(WHEN_FOCUSED);
		ActionMap actMap = this.getActionMap();
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "SPACE");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), "ALT_ENTER");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK), "CTRL_G");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "CTRL_SHIFT_G");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), "CTRL_I");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "CTRL_MINUS");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK), "CTRL_PLUS");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN, KeyEvent.CTRL_DOWN_MASK), "CTRL_HATCH");
		actMap.put("SPACE", new ArrangerIndexAction(false));
		actMap.put("ENTER", new ArrangerIndexAction(true));
		actMap.put("ALT_ENTER", new ArrangerIndexAction("ALT_ENTER"));
		actMap.put("DELETE", new ArrangerIndexAction("DELETE"));
		actMap.put("CTRL_G", new ArrangerIndexAction("MAKE_GROUP"));
		actMap.put("CTRL_SHIFT_G", new ArrangerIndexAction("MAKE_COMPLETE_GROUP"));
		actMap.put("CTRL_I", new ArrangerIndexAction("SHOW_INFO"));
		actMap.put("CTRL_MINUS", new ArrangerIndexAction("DETACH"));
		actMap.put("CTRL_PLUS", new ArrangerIndexAction("ATTACH"));
		actMap.put("CTRL_HATCH", new ArrangerIndexAction("DISSOLVE"));
		
		if (!this.isFocusOwner()) {
			this.setBackground(ARRANGER_INDEX_UNFOCUSSED_BACKGROUND);
		}
		// END KGU#305 2016-12-15
		
		scrollInfo.setWheelScrollingEnabled(true);
		
		createPopupMenu();
	}

	/**
	 * Sets up the pop-up menus with all submenus and shortcuts and actions
	 */
	private void createPopupMenu() {
		// START KGU#318 2017-01-05: Enh. #319 - context menu for the Arranger index
		popupIndex.add(popupIndexGet);
		popupIndexGet.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) {	arrangerIndexGet();	} });
		popupIndexGet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

		// START KGU#573 2018-09-13: Enh. #590  - Attribute inspector for selected index entry
		popupIndex.add(popupIndexAttributes);
		popupIndexAttributes.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexAttributes(); } });
		popupIndexAttributes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK));
		// END KGU#573 2018-09-13
		
		// START KGU#626 2019-01-05: Enh. #657  - Info tree for single selection
		popupIndex.add(popupIndexInfo);
		popupIndexInfo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexInfo(); } });
		popupIndexInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK));
		// END KGU#573 2018-09-13
		
		// START KGU#626 2019-01-01: Enh. #657 items above need single Root selection, below multiple selection is okay
		popupIndex.addSeparator();
		// END KGU#626 2019-01-01
		
		popupIndex.add(popupIndexGroup);
		popupIndexGroup.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexMakeGroup(false); } });
		popupIndexGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK));

		popupIndex.add(popupIndexExpandGroup);
		// FIXME: We should associate a different action here if a group is selected: expand it by missing subdiagrams
		popupIndexExpandGroup.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexMakeGroup(true); } });
		popupIndexExpandGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));

		popupIndex.add(popupIndexDissolve);
		popupIndexDissolve.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexDissolveGroup(); } });
		popupIndexDissolve.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN, KeyEvent.CTRL_DOWN_MASK));

		popupIndex.add(popupIndexDetach);
		popupIndexDetach.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexDetachFromGroup(); } });
		popupIndexDetach.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));

		popupIndex.add(popupIndexAttach);
		popupIndexAttach.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexAttachToGroup(); } });
		popupIndexAttach.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));

		popupIndex.add(popupIndexSave);
		popupIndexSave.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexSave(); } });

		popupIndex.add(popupIndexRemove);
		popupIndexRemove.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexRemove(); } });
		popupIndexRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

		popupIndex.add(popupIndexCovered);
		popupIndexCovered.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexToggleCovered(); } });
		// END KGU#318 2017-01-05

		// START KGU#534 2018-06-27: Enh. #552
		popupIndex.addSeparator();
		popupIndex.add(popupIndexRemoveAll);
		popupIndexRemoveAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexRemoveAll(); } });
		// END KGU#534 2018-06-27
		
		// START KGU#626 2019-01-04: Enh. #657 - a panel that may sporadically be needed for the detach action
		pnlGroupSelect.setLayout(new FlowLayout());
		pnlGroupSelect.add(lblSelectTargetGroup);
		pnlGroupSelect.add(cmbTargetGroup);
		// END KGU#626 2019-01-04
		
		// Configure the skeleton for the info tree for diagrams to be popped up with a JOptionPane
		lblGroups.setIcon(IconLoader.getIcon(94));
		lblSubroutines.setIcon(IconLoader.getIcon(21));
		lblIncludables.setIcon(IconLoader.getIcon(71));
		lblStaleReferences.setIcon(IconLoader.getIcon(5));
		
		indexInfoTree.setCellRenderer(new ArrangerIndexCellRenderer());
		// Permanent tree nodes for diagram info
		nodeIndexInfoTop.add(nodeGroups);
		nodeIndexInfoTop.add(nodeSubroutines);
		nodeIndexInfoTop.add(nodeIncludables);
		nodeIndexInfoTop.add(nodeStaleReferences);

		// START KGU#630 2019-01-07: Enh. #662 - now the equivalents for group info
		lblExternSubroutines.setIcon(IconLoader.getIcon(21));
		lblExternIncludables.setIcon(IconLoader.getIcon(71));
		lblArrangementPath.setIcon(IconLoader.getIcon(3));
		indexGroupInfoTree.setCellRenderer(new ArrangerIndexCellRenderer());
		nodeIndexGroupInfoTop.add(nodeArrangementPath);
		nodeIndexGroupInfoTop.add(nodeElementNumbers);
		nodeIndexGroupInfoTop.add(nodeModifications);
		nodeIndexGroupInfoTop.add(nodeCompleteness);
		// END KGU#630 2019-01-07
	}
	
	/**
	 * Rebuilds the Arranger index from scratch according to the group information give
	 * with {@code _groups}.
	 * @param _groups - sorted list of all currently held {@link Group} objects
	 */
	public void update(Vector<Group> _groups)
	{
		// Attempt to maintain expansions - the nodes will be replaced, so identify the associated groups
		HashSet<Group> expandedGroups = new HashSet<Group>();
		for (DefaultMutableTreeNode node: expandedGroupNodes) {
			expandedGroups.add((Group)node.getUserObject());
		}
		expandedGroupNodes.clear();
		
		// Now rebuild the tree from scratch 
		Vector<Integer> rowsToExpand = new Vector<Integer>(expandedGroups.size());
		arrangerIndexTop.removeAllChildren();
		if (_groups != null) {
			for (int i = 0; i < _groups.size(); i++) {
				Group group = _groups.get(i);
				if (expandedGroups.contains(group)) {
					rowsToExpand.add(i);
				}
				DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
				for (Root aRoot: group.getSortedRoots()) {
					groupNode.add(new DefaultMutableTreeNode(aRoot));
				}
				arrangerIndexTop.add(groupNode);
			}
		}
		((DefaultTreeModel)getModel()).reload();
		if (!this.arrangerIndexTop.isLeaf()) {
			/* Try to restore the original expansion (in backward direction, otherwise we
			 * would invalidate the subsequent row numbers) */
			for (int i = rowsToExpand.size() - 1; i >= 0; i--) {
				this.expandRow(rowsToExpand.get(i));
			}
		}
		this.doButtonsLocal();
	}
	// END KGU#626 2019-01-01

	// START KGU#626 2019-01-04: Enh. #657
	/**
	 * Checks and updates the visibility / usability of all Editor-specific buttons, menu items
	 * and other controls
	 */
	public void doButtonsLocal()
	{
		// START KGU#318 2017-01-05: Enh. #319
		boolean indexSelected = !this.isEmpty() && !this.isSelectionEmpty();
		//popupIndexGet.setEnabled(indexSelected && diagramIndex.getSelectedValue() != diagram.getRoot());
		//popupIndexSave.setEnabled(indexSelected && diagramIndex.getSelectedValue().hasChanged());
		popupIndexGet.setEnabled(indexSelected && arrangerIndexSelectsOtherRoot());
		popupIndexSave.setEnabled(indexSelected && arrangerIndexSelectsUnsavedChanges());
		popupIndexRemove.setEnabled(indexSelected);
		//popupIndexCovered.setEnabled(indexSelected && Element.E_COLLECTRUNTIMEDATA && !arrangerIndex.getSelectedValue().isProgram());
		popupIndexCovered.setEnabled(indexSelected && Element.E_COLLECTRUNTIMEDATA && arrangerIndexSelectsNonProgram());
		// END KGU#318 2017-01-05
		// START KGU#573 2018-09-13: Enh. #590
		//popupIndexAttributes.setEnabled(indexSelected);
		popupIndexAttributes.setEnabled(indexSelected && arrangerIndexGetSelectedRoot() != null);
		// END KGU#573 2018-09-13
		// START KGU#626 2019-01-03: Enh. #657
		popupIndexInfo.setEnabled(indexSelected &&
				(arrangerIndexGetSelectedRoot() != null || arrangerIndexGetSelectedGroup() != null));
		popupIndexGroup.setEnabled(this.arrangerIndexGetSelectedRoots(false).size() > 0);
		popupIndexExpandGroup.setEnabled(!this.arrangerIndexGetSelectedRoots(false).isEmpty()
				|| this.arrangerIndexGetSelectedGroup() != null);
		popupIndexDissolve.setEnabled(this.arrangerIndexGetSelectedGroup() != null);
		popupIndexDetach.setEnabled(!this.arrangerIndexGetSelectedRoots(false).isEmpty());
		popupIndexAttach.setEnabled(!this.arrangerIndexGetSelectedRoots(false).isEmpty());
		// END KGU#626 2019-01-03

	}

	private boolean arrangerIndexSelectsOtherRoot() {
		TreePath[] selectedPaths = this.getSelectionPaths();
		if (selectedPaths.length == 1) {
			Object selectedObject = ((DefaultMutableTreeNode)selectedPaths[0].getLastPathComponent()).getUserObject();
			return (selectedObject instanceof Root && selectedObject != diagram.getRoot());
		}
		return false;
	}

	private boolean arrangerIndexSelectsUnsavedChanges() {
		TreePath[] selectedPaths = this.getSelectionPaths();
		for (TreePath path: selectedPaths) {
			Object selectedObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
			if (selectedObject instanceof Root && ((Root)selectedObject).hasChanged()) {
				return true;
			}
			else if (selectedObject instanceof Group && ((Group)selectedObject).hasChanged()) {
				return true;
			}
		}
		return false;
	}

	private boolean arrangerIndexSelectsNonProgram() {
		TreePath[] selectedPaths = this.getSelectionPaths();
		if (selectedPaths.length == 1) {
			Object selectedObject = ((DefaultMutableTreeNode)selectedPaths[0].getLastPathComponent()).getUserObject();
			if (selectedObject instanceof Root) {
				return !((Root)selectedObject).isProgram();
			}
		}
		return false;
	}

	// START KGU#305/KGU#318 2017-01-05: Enh. #305/#319 Arranger index action methods concentrated here
	public void arrangerIndexGet()
	{
		// START KGU#626 2019-01-01: Enh. #657
		//Root selectedRoot = arrangerIndex.getSelectedValue();
		Root selectedRoot = arrangerIndexGetSelectedRoot();
		// END KGU#626 2019-01-01
		if (selectedRoot != null && selectedRoot != diagram.getRoot()) {
			diagram.setRootIfNotRunning(selectedRoot);
		}		
	}

	private Root arrangerIndexGetSelectedRoot() {
		TreePath[] paths = this.getSelectionPaths();
		if (paths != null && paths.length == 1) {
			Object userObject = ((DefaultMutableTreeNode)paths[0].getLastPathComponent()).getUserObject();
			if (userObject instanceof Root) {
				return (Root)userObject;
			}
		}
		return null;
	}

	private Group arrangerIndexGetSelectedGroup() {
		TreePath[] paths = this.getSelectionPaths();
		if (paths != null && paths.length == 1) {
			Object userObject = ((DefaultMutableTreeNode)paths[0].getLastPathComponent()).getUserObject();
			if (userObject instanceof Group) {
				return (Group)userObject;
			}
		}
		return null;
	}

	private Collection<Root> arrangerIndexGetSelectedRoots(boolean groupMembersToo) {
		HashSet<Root> roots = new HashSet<Root>();
		TreePath[] paths = this.getSelectionPaths();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				Object userObject = ((DefaultMutableTreeNode)paths[i].getLastPathComponent()).getUserObject();
				if (userObject instanceof Root) {
					roots.add((Root)userObject);
				}
				else if (userObject instanceof Group && groupMembersToo) {
					roots.addAll(((Group)userObject).getSortedRoots());
				}
			}
		}
		return roots;
	}

	private Collection<Group> arrangerIndexGetSelectedGroups(boolean partiallySelectedGroupsToo) {
		HashSet<Group> groups = new HashSet<Group>();
		TreePath[] paths = this.getSelectionPaths();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
				Object userObject = node.getUserObject();
				if (userObject instanceof Root && partiallySelectedGroupsToo
						&& (userObject = ((DefaultMutableTreeNode)node.getParent()).getUserObject()) instanceof Group) {
					groups.add((Group)userObject);
				}
				else if (userObject instanceof Group) {
					groups.add((Group)userObject);
				}
			}
		}
		return groups;
	}

	protected void arrangerIndexSave()
	{
		// START KGU#626 2019-01-05: Enh. #657
//		Root selectedRoot = arrangerIndex.getSelectedValue();
//		if (selectedRoot != null) {
//			diagram.saveNSD(selectedRoot, false);
//		}

		/*
		 * We must in any case cache the selection because it's likely that arrangerIndex
		 * will be synchronized in between, which may wipe all selection.
		 */
		Collection<Group> selectedGroups = arrangerIndexGetSelectedGroups(false);
		Collection<Root> selectedRoots = arrangerIndexGetSelectedRoots(true);

		// First save groups then save further roots (the latter may have got superfluous then)
		for (Group selectedGroup: selectedGroups) {
			if (selectedGroup.hasChanged()) {
				Group resultGroup = Arranger.getInstance().saveGroup(this, selectedGroup);
				// Now update the list of recent files in case the saving was successful
				if (resultGroup != null) {
					File groupFile = resultGroup.getArrzFile();
					if (groupFile != null || (groupFile = resultGroup.getFile()) != null) {
						this.diagram.addRecentFile(groupFile.getAbsolutePath());
					}
				}
			}
		}

		for (Root selectedRoot: selectedRoots) {
			if (selectedRoot.hasChanged()) {
				diagram.saveNSD(selectedRoot, false);
			}
		}
		// END KGU#626 2019-01-05
	}

	protected void arrangerIndexRemove()
	{
		// START KGU#626 2019-01-01: Enh. #657
		Collection<Group> doomedGroups = arrangerIndexGetSelectedGroups(false);
		Collection<Root> doomedRoots = arrangerIndexGetSelectedRoots(false);
		boolean goAhead = true;
		if (!doomedGroups.isEmpty() && !doomedRoots.isEmpty()) {
			goAhead = JOptionPane.showConfirmDialog(this,
					msgGroupsAndRootsSelected.getText(),
					popupIndexRemove.getText(),
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
		}
		if (goAhead) {
			Diagram.startSerialMode();
			try {
				// First we remove groups then single roots (if still there)
				int decision = JOptionPane.OK_OPTION;
				if (!doomedGroups.isEmpty()) {
					decision = JOptionPane.showConfirmDialog(this,
							msgDeleteGroupMembers.getText().replace("%", Integer.toString(doomedGroups.size())),
							popupIndexRemove.getText(),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (decision == JOptionPane.CANCEL_OPTION || decision == -1) {
						doomedGroups.clear();
					}
				}
				for (Group group: doomedGroups) {
					Arranger.getInstance().removeGroup(group.getName(), decision == JOptionPane.OK_OPTION, this);
				}
				decision = JOptionPane.OK_OPTION;
				if (!doomedRoots.isEmpty()) {
					decision = JOptionPane.showConfirmDialog(this,
							msgConfirmDeleteRoots.getText().replace("%", Integer.toString(doomedRoots.size())),
							popupIndexRemove.getText(),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (decision == JOptionPane.CANCEL_OPTION || decision == -1) {
						doomedRoots.clear();
					}
				}
				if (decision == JOptionPane.OK_OPTION) {
					for (Root root: doomedRoots) {
						Arranger.getInstance().removeDiagram(root);
					}
				}
				else if (!doomedRoots.isEmpty()) {
					arrangerIndexDetachFromGroup();
				}
			}
			finally {
				Diagram.endSerialMode();
			}
		}
		// END KGU#626 2019-01-01
	}
	
	// START KGU#534 2018-06-27: Enh. #552
	protected void arrangerIndexRemoveAll()
	{
		Arranger.getInstance().removeAllDiagrams(this);			
	}
	// END KGU#534 2018-06-27
	
	protected void arrangerIndexToggleCovered()
	{
		// START KGU#626 2019-01-01: Enh. #657
		Collection<Root> roots = arrangerIndexGetSelectedRoots(false);
		if (roots.isEmpty()) {
			roots = arrangerIndexGetSelectedRoots(true);
		}
		for (Root selectedRoot: roots) {
			if (selectedRoot != null && Element.E_COLLECTRUNTIMEDATA) {
				selectedRoot.deeplyCovered = !selectedRoot.deeplyCovered;
				// We must update/refresh both Structorizer and Arranger
				if (selectedRoot == diagram.getRoot()) {
					diagram.redraw();
				}
			}
		}
		if (!roots.isEmpty()) {
			Arranger.getInstance().redraw();
			this.repaint();
		}
		// END KGU#626 2019-01-01
	}
	// END KGU#305/KGU#318 2017-01-05

	// START KGU#573 2018-09-13: Enh. #590
	protected void arrangerIndexAttributes() {
		// START KGU#626 2019-01-01: Enh. #657
		//Root selectedRoot = arrangerIndex.getSelectedValue();
		Root selectedRoot = arrangerIndexGetSelectedRoot();
		// END KGU#626 2019-01-01
		if (selectedRoot != null) {
			diagram.inspectAttributes(selectedRoot);			
			this.repaint();			
		}
	}
	// END KGU#573 2018-09-13

	// START KGU#626 2019-01-05: Enh. #657
	protected void arrangerIndexInfo() {
		// Let's see what is is
		Group selectedGroup = arrangerIndexGetSelectedGroup();
		Root selectedRoot = this.arrangerIndexGetSelectedRoot();
		Object display = null;
		if (selectedRoot != null) {
			this.nodeIndexInfoTop.setUserObject(selectedRoot);
			nodeGroups.removeAllChildren();
			Collection<Group> owners = Arranger.getInstance().getGroupsFromRoot(selectedRoot, false);
			for (Group group: owners) {
				nodeGroups.add(new DefaultMutableTreeNode(group));
			}
			nodeIncludables.removeAllChildren();
			nodeSubroutines.removeAllChildren();
			HashSet<Root> roots = new HashSet<Root>();
			StringList missing = new StringList();
			roots.add(selectedRoot);
			Collection<Root> moreRoots = Arranger.getInstance().accomplishRootSet(roots, null, missing);
			for (Root root: moreRoots) {
				if (!root.equals(selectedRoot)) {
					if (root.isInclude()) {
						nodeIncludables.add(new DefaultMutableTreeNode(root));
					}
					else if (root.isSubroutine()) {
						nodeSubroutines.add(new DefaultMutableTreeNode(root));
					}
				}
			}
			nodeStaleReferences.removeAllChildren();
			for (int i = 0; i < missing.count(); i++) {
				nodeStaleReferences.add(new DefaultMutableTreeNode(missing.get(i)));
			}
			display = this.scrollInfo;
			((DefaultTreeModel)this.indexInfoTree.getModel()).reload();
		}
		else if (selectedGroup != null) {
			// START KGU#630 2019-01-08: Enh. #662 - redesigned into a tree view
			// Show the group itself
			this.nodeIndexGroupInfoTop.setUserObject(selectedGroup);
			
			// Inform about the file path (if any)
			File arrFile = selectedGroup.getFile();
			if (arrFile == null) {
				this.lblArrangementPath.setText("---");
			}
			else {
				this.lblArrangementPath.setText(arrFile.getAbsolutePath());
			}

			// Present numbers of group members and shared members
			this.nodeElementNumbers.removeAllChildren();
			int nShared = 0;
			for (Root root: selectedGroup.getSortedRoots()) {
				Collection<Group> groups = Arranger.getInstance().getGroupsFromRoot(root, false); 
				if (groups.size() > 1) {
					DefaultMutableTreeNode memberNode = new DefaultMutableTreeNode(root);
					for (Group group: groups) {
						if (group != selectedGroup) {
							memberNode.add(new DefaultMutableTreeNode(group));
						}
					}
					this.nodeElementNumbers.add(memberNode);
					nShared++;
				}
			}
			this.nodeElementNumbers.setUserObject(msgNumberOfSharedMembers.getText()
					.replace("%", Integer.toString(nShared)));

			// Inform about registered modifications
			this.nodeModifications.removeAllChildren();
			if (selectedGroup.membersChanged) {
				this.nodeModifications.add(new DefaultMutableTreeNode(msgGroupMembersChanged.getText()));
			}
			if (selectedGroup.membersMoved) {
				this.nodeModifications.add(new DefaultMutableTreeNode(msgGroupMembersMoved.getText()));
			}
			this.lblModifications.setIcon(selectedGroup.hasChanged() ? redIcon : greenIcon);

			// Present information about external and stale references
			this.nodeCompleteness.removeAllChildren();
			this.nodeExternSubroutines.removeAllChildren();
			this.nodeExternIncludables.removeAllChildren();
			this.nodeDeafReferences.removeAllChildren();
			StringList missing = new StringList();
			HashSet<Root> members = new HashSet<Root>(selectedGroup.getSortedRoots());
			Collection<Root> expandedSet = Arranger.getInstance().accomplishRootSet(members, null, missing);
			for (Root root: expandedSet) {
				if (!members.contains(root)) {
					if (root.isSubroutine()) {
						this.nodeExternSubroutines.add(new DefaultMutableTreeNode(root));
					}
					else if (root.isInclude()) {
						this.nodeExternIncludables.add(new DefaultMutableTreeNode(root));
					}
				}
			}
			for (int i = 0; i < missing.count(); i++) {
				this.nodeDeafReferences.add(new DefaultMutableTreeNode(missing.get(i)));				
			}
			if (!this.nodeExternSubroutines.isLeaf()) {
				this.nodeCompleteness.add(this.nodeExternSubroutines);
			}
			if (!this.nodeExternIncludables.isLeaf()) {
				this.nodeCompleteness.add(this.nodeExternIncludables);
			}
			if (!this.nodeDeafReferences.isLeaf()) {
				this.nodeCompleteness.add(this.nodeDeafReferences);
			}
			if (!this.nodeCompleteness.isLeaf()) {
				this.lblCompleteness.setText(msgMembersIncomplete.getText()
				.replace("%1", Integer.toString(expandedSet.size() - members.size()))
				.replace("%2", Integer.toString(missing.count())));
				this.lblCompleteness.setIcon(redIcon);
			}
			else {
				this.lblCompleteness.setText(msgMembersComplete.getText());
				this.lblCompleteness.setIcon(greenIcon);
			}
			
			// Set visibility button status
			btnShowGroup.setSelected(selectedGroup.isVisible());
			
			display = this.pnlGroupInfo;
			((DefaultTreeModel)this.indexGroupInfoTree.getModel()).reload();
			// END KGU#63ß 2019-01-08
		}
		
		if (display != null) {
			JOptionPane.showMessageDialog(this, display,
					popupIndexInfo.getText(), JOptionPane.INFORMATION_MESSAGE);
		}
	}
	// END KGU#626 2019-01-05


	// START KGU#626 2019-01-03: Enh #657
	private boolean arrangerIndexMakeGroup(boolean expand)
	{
		// FIXME: Check for single group selection, then expand the group!
		Group selectedGroup = this.arrangerIndexGetSelectedGroup();
		if (selectedGroup != null) {
			Collection<Root> expandedRootSet = Arranger.getInstance().accomplishRootSet(
					new HashSet<Root>(selectedGroup.getSortedRoots()), this, null);
			for (Root root: expandedRootSet) {
				Arranger.getInstance().attachRootToGroup(selectedGroup, root, null, this);
			}
		}
		return Arranger.getInstance().makeGroup(this.arrangerIndexGetSelectedRoots(false), this, expand);
	}

	/** Dissolves the selected group(s) i.e. detaches all contained diagrams. If a diagram gets
	 * orphaned then it will be attached to the default group instead. The group may be deleted
	 * if it hadn't been associated to a file.
	 */
	private boolean arrangerIndexDissolveGroup() {
		Collection<Group> groups = this.arrangerIndexGetSelectedGroups(false);
		// TODO make a user query (multiple selection)
		boolean done = true;
		for (Group group: groups) {
			done = Arranger.getInstance().dissolveGroup(group.getName(), this) && done;
		}
		return done && !groups.isEmpty();
	}

	/**
	 * Detaches the selected diagrams from the parent group of their respective selection
	 * path. If diagram gets orphaned then it will be attached to the default group instead.
	 * @return true if at least one of the selected detachments worked.
	 */
	private boolean arrangerIndexDetachFromGroup()
	{
		boolean done = false;
		TreePath[] paths = this.getSelectionPaths();
		for (int i = 0; i < paths.length; i++) {
			TreePath path = paths[i];
			if (path.getPathCount() >= 3) {
				Object rootObject = ((DefaultMutableTreeNode)path.getPathComponent(2)).getUserObject();
				Object groupObject = ((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject();
				if (rootObject instanceof Root && groupObject instanceof Group) {
					done = Arranger.getInstance().detachRootFromGroup((Group)groupObject, (Root)rootObject, this) || done;
				}
			}
		}
		return done;
	}
	
	/**
	 * Asks for a target group and attaches the selected diagrams to the chosen group.
	 * Depending on a user decision the diagrams are simply added to the new group (i.e.
	 * shared with their current groups) or moved from the group of their selection path. 
	 * @return true if at least one of the selected attachments worked.
	 */
	private boolean arrangerIndexAttachToGroup()
	{
		boolean done = false;
		/* For a single selected Root, it makes of course sense not to offer its source
		 * groups among the targets, but with distributed selection we must face a situation
		 * that the selected Root objects are members of many different groups - so would cost
		 * too much efforts for a rather unimportant effect - if the user selects the source
		 * group as target group then simply nothing will happen. So we don't bother.
		 * Nevertheless after the target was chosen we will of course take he actual paths
		 * into consideration.
		 */ 
		Collection<Root> roots = this.arrangerIndexGetSelectedRoots(false);
		if (!roots.isEmpty()) {
			int nGroups = this.arrangerIndexTop.getChildCount();
			for (int i = 0; i < nGroups; i++) {
				Object groupObject = ((DefaultMutableTreeNode)arrangerIndexTop.getChildAt(i)).getUserObject();
				if (groupObject instanceof Group && !((Group)groupObject).isDefaultGroup()) {
					this.cmbTargetGroup.addItem((Group)groupObject);
				}
			}
			String[] options = new String[msgAttachOptions.length];
			for (int i = 0; i < options.length; i++) {
				options[i] = msgAttachOptions[i].getText();
			}
			int option = JOptionPane.showOptionDialog(this,
					this.pnlGroupSelect,
					popupIndexAttach.getText(),
					JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, IconLoader.getIcon(117), options, options[0]);
			if (option < options.length-1) {
				Group targetGroup = (Group)cmbTargetGroup.getSelectedItem();
				if (option == 0) {
					// Simply add the roots to the target group
					for (Root root: roots) {
						done = Arranger.getInstance().attachRootToGroup(targetGroup, root, null, this) || done;
					}
				}
				else {
					// We have to move the diagrams, so we must know where they come from
					TreePath[] paths = this.getSelectionPaths();
					for (int i = 0; i < paths.length; i++) {
						TreePath path = paths[i];
						if (path.getPathCount() >= 3) {
							Object rootObject = ((DefaultMutableTreeNode)path.getPathComponent(2)).getUserObject();
							Object groupObject = ((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject();
							if (rootObject instanceof Root && groupObject instanceof Group) {
								done = Arranger.getInstance().attachRootToGroup(targetGroup, (Root)rootObject, (Group)groupObject, this) || done;
							}
						}
					}
				}
			}
		}
		cmbTargetGroup.removeAllItems();
		return done;
	}
	// END KGU#626 2019-01-03
	
	/**
	 * @return true if the index tree is empty i.e. if nor group node is present
	 */
	public boolean isEmpty()
	{
		return this.arrangerIndexTop.isLeaf();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// START KGU#626 2019-01-01: Enh. #657
		//else if (e.getSource() == diagramIndex)
		if (e.getSource() == this && this.getSelectionCount() == 1)
		// END KGU#626 2019-01-01
		{
			if (e.getClickCount() == 1)
			{
				TreePath[] selectedPaths = this.getSelectionPaths();	// Should have cardinality 1
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPaths[0].getLastPathComponent();
				Object selectedObject = selectedNode.getUserObject();
				if (selectedObject instanceof Root) {
					// Should be a Root object
					Arranger.scrollToDiagram((Root)selectedObject, true);
				}
				else if (selectedObject instanceof Group) {
					Arranger.scrollToGroup((Group)selectedObject);
				}
				
			}
			else if (e.getClickCount() == 2) {
				// START KGU#626 2019-01-01: Enh. #657
				//Root selectedRoot = diagramIndex.getSelectedValue();
				//if (selectedRoot != null && selectedRoot != this.root) {
				//	this.setRootIfNotRunning(selectedRoot);
				//}
				this.getParent().getParent().requestFocusInWindow();
				TreePath[] paths = this.getSelectionPaths();
				Object selectedObject = ((DefaultMutableTreeNode)paths[0].getLastPathComponent()).getUserObject();
				if (selectedObject instanceof Root) {
					Root selectedRoot = (Root)selectedObject;
					if (selectedRoot != diagram.getRoot()) {
						diagram.setRootIfNotRunning(selectedRoot);
					}
				}
				else if (selectedObject instanceof Group) {
					// TODO think about some more sensible action than showing the info here...
					// (additionally to the expand/collapse action done by the JTree itself)

					// Grope for the editor instance in the container hierarchy (bad, bad!)
					this.arrangerIndexInfo();
				}
				// END KGU#626 2019-01-01
			}
		}
		// END KGU#305 2016-12-12
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}


}

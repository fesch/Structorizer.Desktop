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
 *      Description:    A locale-aware JTree subclass handling the bundling of arranger index features.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2019-01-11      First Issue
 *      Kay Gürtzig     2019-01-12      Group colour update notification
 *      Kay Gürtzig     2019-01-17      Enhancements (group nodes for external references) and corrections
 *      Kay Gürtzig     2019-01-25      Bugfix #670: Attempt to fix the scaling deficiency w.r.t. to the info trees
 *      Kay Gürtzig     2019-01-28      Issue #670: Update of the info box components on look & feel change
 *      Kay Gürtzig     2019-02-05      Bugfix #674: L&F update of popup menu ensured
 *      Kay Gürtzig     2019-03-01      Enh. #691: Group renaming enabled (new context menu item + accelerator)
 *      Kay Gürtzig     2019-03-30      Enh. #720: tree node for dependent diagrams (includers/callers) added
 *      Kay Gürtzig     2020-03-16      Enh. #828: New popup submenu for code export of a group (or diagram)
 *      Kay Gürtzig     2020-04-01      Enh. #440: Group export to PapDesigner inserted in popup menu
 *      Kay Gürtzig     2020-06-06      Issue #868/#870: Suppression of group export in noExportImport mode
 *      Kay Gürtzig     2020-12-29      Issue #901: Time-consuming actions set WAIT_CURSOR now
 *      Kay Gürtzig     2020-12-31      Bugfix #902: Must regain focus after selecting; cancel add/move action
 *                                      on quitting the pane; confirmation request before dissolving groups
 *      Kay Gürtzig     2021-01-11      Enh. #910: Menu items disabled if only DiagramController proxies are selected
 *      Kay Gürtzig     2021-02-23      Issue #901: WAIT_CURSOR now also set on group saving
 *      Kay Gürtzig     2022-08-17      Issue #1065: Right-click now overrides a previous single selection
 *      Kay Gürtzig     2024-11-25      Issue #1180: Test coverage display inconsistency fixed
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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import lu.fisch.structorizer.archivar.IRoutinePoolListener;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.gui.ColorButton;
import lu.fisch.structorizer.gui.Diagram;
import lu.fisch.structorizer.gui.GUIScaler;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.gui.Menu;
import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.locales.LangEvent;
import lu.fisch.structorizer.locales.LangEventListener;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.locales.LangTree;
import lu.fisch.structorizer.parsers.GENParser;
import lu.fisch.utils.StringList;

/**
 * A locale-aware JTree subclass presenting all diagrams group by group in a tree, also offering
 * many info and manipulating opportunities for groups and diagrams.
 * 
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class ArrangerIndex extends LangTree implements MouseListener, LangEventListener {
	
	private Diagram diagram = null;
	private final DefaultMutableTreeNode arrangerIndexTop;
	//protected final JTree arrangerIndex = new JTree(arrangerIndexTop);
	private final HashSet<DefaultMutableTreeNode> expandedGroupNodes = new HashSet<DefaultMutableTreeNode>();
	/** Original (standard) Arranger index background color  - may get wrong with an L&F change! */
	private Color arrangerIndexBackground = null;
	// START KGU#964 2021-03-10: Issue #966 Consider dark themes
	//private static final Color ARRANGER_INDEX_UNFOCUSSED_BACKGROUND = Color.LIGHT_GRAY;
	private static final Color LIGHT_INDEX_UNFOCUSSED_BACKGROUND = Color.LIGHT_GRAY;
	private static final Color DARK_INDEX_UNFOCUSSED_BACKGROUND = Color.DARK_GRAY;
	private static Color unfocussedBackground = null;
	// END KGU#964 2021-03-10

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
	// START KGU#815 2020-03-16: Enh. #828 group export
	protected final JMenu popupIndexExport = new JMenu("Export diagram/group");
	// END KGU#815 2020-03-16
	// START KGU#396/KGU#815 2020-04-01: Enh. #440, #828 FIXME - to be converted into a plugin mechanism
	protected final JMenu popupIndexExportPap = new JMenu("PapDesigner");
	protected final JMenuItem popupIndexExportPap1966 = new JMenuItem("DIN 66001 / 1966 ...");
	protected final JMenuItem popupIndexExportPap1982 = new JMenuItem("DIN 66001 / 1982 ...");
	// END KGU#396/KGU#815 2020-04-01
	// START KGU#626 2019-01-03: Enh. #657
	protected final JMenuItem popupIndexGroup = new JMenuItem("Create group ...", IconLoader.getIcon(94));
	protected final JMenuItem popupIndexExpandGroup = new JMenuItem("Expand group ...", IconLoader.getIcon(117));
	protected final JMenuItem popupIndexDissolve = new JMenuItem("Dissolve group", IconLoader.getIcon(97));
	protected final JMenuItem popupIndexDetach = new JMenuItem("Detach from group", IconLoader.getIcon(98));
	protected final JMenuItem popupIndexAttach = new JMenuItem("Add/move to group ...", IconLoader.getIcon(116));
	protected final JMenuItem popupIndexInfo = new JMenuItem("Diagram/group info ...", IconLoader.getIcon(118));
	protected final JCheckBoxMenuItem popupIndexDrawGroup = new JCheckBoxMenuItem("Show group", IconLoader.getIcon(17));
	// START KGU#669 2019-03-01: Enh. #691
	protected final JMenuItem popupIndexRenameGroup = new JMenuItem("Rename group ...");
	// END KGU#669 2019-03-01
	// START KGU#408 2021-02-26: Enh. #410 It ought to be possible to hide the qualifiers
	protected final JCheckBoxMenuItem popupIndexShowQualifiers = new JCheckBoxMenuItem("Show qualifiers as prefix");
	// END KGU#408 2021-02-26
	// START KGU#705 2019-10-03: Added on occasion of Enh. #738 (code preview) for regularity
	protected final JMenuItem popupIndexHide = new JMenuItem("Hide Arranger index");
	// END KGU#705 2019-10-03
	// START KGU#646 2019-02-10: Issue #674 - The L&F adaptation from Windows to others was defective if it hadn't been open before
	private boolean wasPopupOpen = false;
	// END KGU#646 2019-02-10
	
	// START KGU#408 2021-02-26: Enh. #410 The display of the namespace prefix ought to be configurable
	// The map is necessary to allow instance-dependent settings
	private static final HashMap<JTree, Boolean> showQualifiers = new HashMap<JTree, Boolean>();
	// END KGU#408 2021-02-26

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
	// START KGU#703 2019-03-30: Issue #720
	protected final JLabel lblDependingDiagrams = new JLabel("Dependent diagrams");
	// END KGU##783 2019-03-30
	protected final DefaultMutableTreeNode nodeGroups = new DefaultMutableTreeNode(lblGroups);
	protected final DefaultMutableTreeNode nodeSubroutines = new DefaultMutableTreeNode(lblSubroutines);
	protected final DefaultMutableTreeNode nodeIncludables = new DefaultMutableTreeNode(lblIncludables);
	protected final DefaultMutableTreeNode nodeStaleReferences = new DefaultMutableTreeNode(lblStaleReferences);
	// START KGU#703 2019-03-30: Issue #720
	protected final DefaultMutableTreeNode nodeDependingDiagrams = new DefaultMutableTreeNode(lblDependingDiagrams);
	// END KGU##783 2019-03-30
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
	
	// START KGU#669 2019-03-01: Enh. #691
	protected static final LangTextHolder msgNewGroupName = new LangTextHolder("New name for the selected group: ");
	// END KGU#669 2019-03-01
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
	// START KGU#900 2020-12-31: Issue #902
	protected static final LangTextHolder msgConfirmDissolve = new LangTextHolder("Sure to dissolve these group(s)?\n%");
	// END KGU#900 2020-12-31
	
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
				// START KGU#408 2021-02-26: Enh. #410 Show the qualified names, e.g. after Java import
				//String s = root.getSignatureString(true);
				boolean withQualifiers = !showQualifiers.containsKey(tree) || showQualifiers.get(tree);
				String s = root.getSignatureString(true, withQualifiers);
				// END KGU#408 2021-02-26
				// START KGU#1036 2024-11-25: Bugfix #1180 We must accept both kinds of markings
				//boolean covered = Element.E_COLLECTRUNTIMEDATA && root.deeplyCovered;
				boolean covered = Element.E_COLLECTRUNTIMEDATA && root.isTestCovered(true);
				// END KGU#1036 2024-11-25
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
				// START KGU#900 2020-12-31: Issue #902 - better ask in this case
				//arrangerIndexDissolveGroup();
				arrangerIndexDissolveGroup(false);
				// END KGU#900 2020-12-31
			}
			// END KGU#626 2019-01-05
			// START KGU#630 2019-01-13: Enh. #662/2
			else if (name.equals("TOGGLE_GROUP_VISIBILITY")) {
				arrangerIndexToggleGroupVis();
			}
			// END KGU#630 2019-01-13
			// START KGU#669 219-03-01: Enh. #691
			else if (name.equals("SHIFT_ALT_R")) {
				arrangerIndexRenameGroup();
			}
			// END KGU#669 2019-03-01
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
					
					// START KGU#1056 2022-08-17: Issue #1065 right click should hit
					//if (isSelectionEmpty()) {
					//	TreePath path = getClosestPathForLocation(e.getX(), e.getY());
					//	if (path != null) {
					//		addSelectionPath(path);
					//	}
					//}
					if (getSelectionModel().getSelectionCount() <= 1) {
						TreePath path = getClosestPathForLocation(e.getX(), e.getY());
						if (path != null) {
							setSelectionPath(path);
						}
					}
					// END KGU#1056 2022-08-17
					doButtonsLocal();
					requestFocusInWindow();
					// START KGU#646 2019-02-10: workaround for issue #674
					if (!wasPopupOpen) {
						javax.swing.SwingUtilities.updateComponentTreeUI(popupIndex);
						wasPopupOpen = true;
					}
					// END KGU#646 4019-02-10
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
		// START KGU#408 2021-02-26: Enh. #410
		showQualifiers.put(this, true);
		// END KGU#408 2021-02-26
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
		// START KGU#964 2021-03-10: Issue #966 Decide whether this is a light or dark theme
		float[] hsb = new float[3];
		Color.RGBtoHSB(arrangerIndexBackground.getRed(), arrangerIndexBackground.getGreen(), arrangerIndexBackground.getBlue(), hsb);
		unfocussedBackground = hsb[2] > 0.5 ? LIGHT_INDEX_UNFOCUSSED_BACKGROUND : DARK_INDEX_UNFOCUSSED_BACKGROUND;
		// END KGU#964 2021-03-10
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
				// START KGU#964 2021-03-10: Issue #966
				//setBackground(ARRANGER_INDEX_UNFOCUSSED_BACKGROUND);
				setBackground(unfocussedBackground);
				// END KGU#964 2021-03-10
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
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), "CTRL_ALT_G");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), "CTRL_I");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "CTRL_MINUS");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK), "CTRL_PLUS");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN, KeyEvent.CTRL_DOWN_MASK), "CTRL_HATCH");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK), "SHIFT_ALT_R");
		actMap.put("SPACE", new ArrangerIndexAction(false));
		actMap.put("ENTER", new ArrangerIndexAction(true));
		actMap.put("ALT_ENTER", new ArrangerIndexAction("ALT_ENTER"));
		actMap.put("DELETE", new ArrangerIndexAction("DELETE"));
		actMap.put("CTRL_G", new ArrangerIndexAction("MAKE_GROUP"));
		actMap.put("CTRL_SHIFT_G", new ArrangerIndexAction("MAKE_COMPLETE_GROUP"));
		actMap.put("CTRL_ALT_G", new ArrangerIndexAction("TOGGLE_GROUP_VISIBILITY"));
		actMap.put("CTRL_I", new ArrangerIndexAction("SHOW_INFO"));
		actMap.put("CTRL_MINUS", new ArrangerIndexAction("DETACH"));
		actMap.put("CTRL_PLUS", new ArrangerIndexAction("ATTACH"));
		actMap.put("CTRL_HATCH", new ArrangerIndexAction("DISSOLVE"));
		actMap.put("SHIFT_ALT_R", new ArrangerIndexAction("RENAME_GROUP"));
		
		if (!this.isFocusOwner()) {
			// START KGU#964 2021-03-10: Issue #966
			//this.setBackground(ARRANGER_INDEX_UNFOCUSSED_BACKGROUND);
			setBackground(unfocussedBackground);
			// END KGU#964 2021-03-10
		}
		// END KGU#305 2016-12-15
		
		scrollInfo.setWheelScrollingEnabled(true);
		
		createPopupMenu();
		
		// START KGU#964 2021-03-10: Issue #966
		UIManager.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("lookAndFeel".equals(evt.getPropertyName())) {
					Color bg = UIManager.getColor("Tree.background");
					if (bg != null) {
						arrangerIndexBackground = bg;
						float[] hsb = new float[3];
						Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), hsb);
						unfocussedBackground = hsb[2] > 0.5 ? LIGHT_INDEX_UNFOCUSSED_BACKGROUND : DARK_INDEX_UNFOCUSSED_BACKGROUND;
					}
				}
				
			}});
		// END KGU#964 2021-03-10
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
		
		popupIndex.add(popupIndexSave);
		popupIndexSave.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexSave(); } });

		popupIndex.add(popupIndexRemove);
		popupIndexRemove.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexRemove(); } });
		popupIndexRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

		// START KGU#815 2020-03-16: Enh. #828 Code export for diagrams or groups
		popupIndex.add(popupIndexExport);
		popupIndexExport.setIcon(IconLoader.getIcon(32));
		{
			Vector<GENPlugin> plugins = Menu.generatorPlugins;
			if (plugins.isEmpty()) {
				// Editor must retrieve the plugins itself, obvioulsy
				String fileName = "generators.xml";
				BufferedInputStream buff = new BufferedInputStream(Menu.class.getResourceAsStream(fileName));
				GENParser genp = new GENParser();
				plugins = genp.parse(buff);
				try { buff.close();	} catch (IOException e) {}
			}
			ImageIcon defaultIcon = IconLoader.getIcon(87);
			ActionListener exportListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object item = e.getSource();
					if (item instanceof JMenuItem) {
						arrangerIndexExportCode(((JMenuItem) item).getName());
					}
				}
			};
			for (GENPlugin plugin: plugins)
			{
				ImageIcon icon = defaultIcon;	// The default icon
				if (plugin.icon != null && !plugin.icon.isEmpty()) {
					try {
						URL iconFile = IconLoader.class.getResource(plugin.icon);
						if (iconFile != null) {
							icon = IconLoader.getIconImage(plugin.icon);
						}
					}
					catch (Exception ex) {}
				}
				JMenuItem pluginItem = new JMenuItem(plugin.title, icon);
				pluginItem.setName(plugin.className);
				popupIndexExport.add(pluginItem);
				pluginItem.addActionListener(exportListener);
			}
			// START KGU#396 2020-04-01: Enh. #440 Add PapDesigner export
			ActionListener exportPapListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object item = e.getSource();
					if (item instanceof JMenuItem) {
						arrangerIndexExportPap(item == popupIndexExportPap1982);
					}
				}
			};
			ImageIcon icon = defaultIcon;	// The default icon
			URL iconFile = IconLoader.class.getResource("icons/editor_pap.png");
			if (iconFile != null) {
				icon = IconLoader.getIconImage("editor_pap.png");
			}
			// FIXME: This should be based on a plugin definition like for Menu.importPluginItems
			popupIndexExportPap.setIcon(icon);
			popupIndexExport.add(popupIndexExportPap);
			popupIndexExportPap.setToolTipText(Menu.msgExportTooltip.getText().replace("%", "https://www.heise.de/download/product/papdesigner-51889"));
			popupIndexExportPap.add(popupIndexExportPap1966);
			popupIndexExportPap.add(popupIndexExportPap1982);
			popupIndexExportPap1966.addActionListener(exportPapListener);
			popupIndexExportPap1982.addActionListener(exportPapListener);
			// END KGU#396 2020-04-01
			// START KGU#396 2020-06-06
			popupIndexExportPap1966.setIcon(icon);
			popupIndexExportPap1982.setIcon(icon);
			// END KGU#396 2020-06-06
			// START KGU#396/KGU#725 2020-04-08: Enh. #440, #746 - for later re-translation if necessary
			Menu.msgExportTooltip.addLangEventListener(this);
			// END KGU#396 2020-04-08
		}
		// END KGU#815 2020-03-16
		
		popupIndex.add(popupIndexCovered);
		popupIndexCovered.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexToggleCovered(); } });
		// END KGU#318 2017-01-05

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
		popupIndexDissolve.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexDissolveGroup(true); } });
		popupIndexDissolve.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN, KeyEvent.CTRL_DOWN_MASK));

		popupIndex.add(popupIndexDetach);
		popupIndexDetach.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexDetachFromGroup(); } });
		popupIndexDetach.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));

		popupIndex.add(popupIndexAttach);
		popupIndexAttach.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexAttachToGroup(); } });
		popupIndexAttach.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));

		// START KGU#630 2019-01-13: Enh. #662/2  - Control group visibility
		popupIndex.add(popupIndexDrawGroup);
		popupIndexDrawGroup.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexSetGroupVis(((JCheckBoxMenuItem)event.getSource()).isSelected()); } });
		popupIndexDrawGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		// END KGU#630 2019-01-13
		
		// START KGU#630 2019-01-13: Enh. #662/2  - Control group visibility
		popupIndex.add(popupIndexRenameGroup);
		popupIndexRenameGroup.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexRenameGroup(); } });
		popupIndexRenameGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		// END KGU#630 2019-01-13
		
		// START KGU#534 2018-06-27: Enh. #552
		popupIndex.addSeparator();
		popupIndex.add(popupIndexRemoveAll);
		popupIndexRemoveAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexRemoveAll(); } });
		// END KGU#534 2018-06-27
		
		popupIndex.addSeparator();
		
		// START KGU#408 2021-02-26: Enh. #410
		popupIndex.add(popupIndexShowQualifiers);
		popupIndexShowQualifiers.setSelected(true);
		popupIndexShowQualifiers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showQualifiers.put(ArrangerIndex.this, popupIndexShowQualifiers.isSelected());
				int nGroups = arrangerIndexTop.getChildCount();
				Vector<Group> groups = new Vector<Group>();
				for (int i = 0; i < nGroups; i++) {
					Object groupObject = ((DefaultMutableTreeNode)arrangerIndexTop.getChildAt(i)).getUserObject();
					if (groupObject instanceof Group) {
						groups.add((Group)groupObject);
					}
				}
				update(groups);
				repaint();
			}
		});
		// END KGU#408 2021-02-21
		
		popupIndex.add(popupIndexHide);
		popupIndexHide.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		popupIndexHide.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				diagram.setArrangerIndex(false);
			}
		});

		
		// START KGU#626 2019-01-04: Enh. #657 - a panel that may sporadically be needed for the detach action
		pnlGroupSelect.setLayout(new FlowLayout());
		pnlGroupSelect.add(lblSelectTargetGroup);
		pnlGroupSelect.add(cmbTargetGroup);
		// END KGU#626 2019-01-04
		
		// Configure the skeleton for the info tree for diagrams to be popped up with a JOptionPane
		lblGroups.setIcon(IconLoader.getIcon(94));
		lblSubroutines.setIcon(IconLoader.getIcon(21));
		lblIncludables.setIcon(IconLoader.getIcon(71));
		lblDependingDiagrams.setIcon(IconLoader.getIcon(22));
		lblStaleReferences.setIcon(IconLoader.getIcon(5));
		
		indexInfoTree.setCellRenderer(new ArrangerIndexCellRenderer());
		// Permanent tree nodes for diagram info
		nodeIndexInfoTop.add(nodeGroups);
		nodeIndexInfoTop.add(nodeSubroutines);
		nodeIndexInfoTop.add(nodeIncludables);
		nodeIndexInfoTop.add(nodeStaleReferences);
		// START KGU#703 2019-03-30: Enh. #720
		nodeIndexInfoTop.add(nodeDependingDiagrams);
		// END KGU#703 2019-03-30

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
		// START KGU#642 2019-01-25: Bugfix #670
		GUIScaler.rescaleComponents(scrollInfo);
		GUIScaler.rescaleComponents(scrollGroupInfo);
		// END KGU#642 2019-01-25
	}
	
	/**
	 * Rebuilds the Arranger index from scratch according to the group information
	 * given with {@code _groups}.
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
			// START KGU#408 2021-02-28: Enh. #410 hierarchical presentation of imported OOP diagrams
			boolean qualifiedNames = popupIndexShowQualifiers == null
					|| popupIndexShowQualifiers.isSelected();
			// END KGU#408 2021-02-28
			for (int i = 0; i < _groups.size(); i++) {
				Group group = _groups.get(i);
				if (expandedGroups.contains(group)) {
					rowsToExpand.add(i);
				}
				DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
				// START KGU#408 2021-02-28: Enh. #410 hierarchical presentation of imported OOP diagrams
				//for (Root aRoot: group.getSortedRoots()) {
				//	groupNode.add(new DefaultMutableTreeNode(aRoot));
				//}
				Vector<Root> roots = group.getSortedRoots();
				if (qualifiedNames) {
					for (Root aRoot: roots) {
						groupNode.add(new DefaultMutableTreeNode(aRoot));
					}
				}
				else {
					rebuildGroupAsTree(groupNode, roots);
				}
				// END KGU#408 2021-02-28
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

	/**
	 * Rebuilds the group node as a multi-level diagram tree, regarding namespace
	 * hierarchy 
	 * @param groupNode - the group node to attach the root node to
	 * @param roots - the sorted vector of member diagrams
	 */
	private void rebuildGroupAsTree(DefaultMutableTreeNode groupNode, Vector<Root> roots) {
		// This algorithm works but needs too much time
		Vector<StringList> qualifiers = new Vector<StringList>();
		int ixSubs = roots.size();	// Index of the first subroutine
		int ixIncl = ixSubs;		// Index of the first includable
		/* According to the sorting strategy (lexicographic by qualified
		 * name), nodes the qualified name of which represents a prefix
		 * of others will precede the latter ones. So we can 
		 */
		// Preparation loop: Fetch and decompose the qualified names
		for (Root aRoot: roots) {
			if (aRoot.isSubroutine()) {
				ixSubs = Math.min(ixSubs, qualifiers.size());
			}
			else if (aRoot.isInclude()) {
				ixIncl = Math.min(ixIncl, qualifiers.size());
			}
			qualifiers.add(StringList.explode(aRoot.getQualifiedName(true), "\\."));
		}
		/* Now try to place the uncontained nodes per category first and attach
		 * them their descendants
		 */
		int[] ranges = new int[] {0, ixSubs, ixSubs, ixIncl, ixIncl, roots.size()};
		for (int k = 0; k < ranges.length - 1; k += 2) {
			for (int j = ranges[k]; j < ranges[k+1]; j++) {
				StringList path = qualifiers.get(j);
				if (path == null) {
					continue;
				}
				boolean placeIt = path.count() <= 1;
				if (!placeIt) {
					// Look for prefixes among the following categories
					boolean isContained = false;
					for (int c = k + 2; c < ranges.length -1; c += 2) {
						isContained = checkContainingNodes(path, qualifiers, ranges[c], ranges[c+1]);
						if (isContained) {
							break;
						}
					}
					placeIt = !isContained;
				}
				if (placeIt) {
					DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(roots.get(j));
					groupNode.add(rootNode);
					qualifiers.set(j, null);	// Mark it as done
					// Now look for direct child nodes
					int[] ranges2 = ranges.clone();
					ranges2[k] = j + 1;	// in the current category only subsequent entries are of interest
					findAndAddChildren(rootNode, path, qualifiers, roots, ranges2);
				}
			}
		}
		// Just to make sure we haven't forgotten any: Gather the remnants
		for (int j = 0; j < roots.size(); j++) {
			if (qualifiers.get(j) != null) {
				groupNode.add(new DefaultMutableTreeNode(roots.get(j)));
			}
		}
	}
	/**
	 * Checks if a prefix of the given path is among the lexicographically sorted
	 * qualified names in index range from ixStart to ixEnd-1.
	 * @param path - a StringList representing the path
	 * @param qualifiers - the vector of all occurring paths in this group, may contain
	 *      {@code null} entries (if the respective node is already placed)
	 * @param ixSubs
	 * @param ixIncl
	 * @return
	 */
	private boolean checkContainingNodes(StringList path, Vector<StringList> qualifiers, int ixStart, int ixEnd) {
		String qualName = path.concatenate(".");
		for (int i = ixStart; i < ixEnd; i++) {
			StringList path2 = qualifiers.get(i);
			if (path2 != null) {
				if (path.indexOf(path2, 0, true) == 0) {
					return true;
				}
				else if (qualName.compareTo(path2.concatenate(".")) > 0) {
					// A containing path may not come anymore
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Finds diagrams the associated qualified name of which is a direct "child" of the given
	 * {@code path}
	 * @param rootNode - the already established node associated to decomposed qualified name {@code path}
	 * @param path - the split qualified name a a StringList
	 * @param qualifiers - the vector of split qualified names according to the order of {@code roots}.
	 * @param roots - the sorted Roots of the currently processed Group
	 * @param ranges - an array of index values where always two consecutive ones define an index
	 *    range.
	 */
	private void findAndAddChildren(DefaultMutableTreeNode rootNode, StringList path, Vector<StringList> qualifiers,
			Vector<Root> roots, int[] ranges) {
		int len = path.count();
		String qualName = path.concatenate(".");
		for (int k = 0; k < ranges.length-1; k+=2) {
			for (int i = ranges[k]; i < ranges[k+1]; i++) {
				StringList path2 = qualifiers.get(i);
				if (path2 != null) {
					if (path2.indexOf(path, 0, true) == 0 && path2.count() == len+1) {
						DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(roots.get(i));
						rootNode.add(subNode);
						qualifiers.set(i, null);
						int[] subranges = ranges.clone();
						subranges[k] = i + 1;
						findAndAddChildren(subNode, path2, qualifiers, roots, subranges);
					}
					else if (qualName.compareTo(path2.concatenate(".")) > 0) {
						// No further matching entries in this range
						break;
					}
				}
			}
		}
	}
	
//	/**
//	 * Rebuilds the group node as a multi-level diagram tree, regarding namespace
//	 * hierarchy (second approach) 
//	 * @param groupNode - the group node to attach the root node to
//	 * @param roots - the sorted vector of member diagrams
//	 */
//	private void rebuildGroupAsTree(DefaultMutableTreeNode groupNode, Vector<Root> roots) {
//		// We build a horizontal name and Root hierarchy, ideally in linear time
//		LinkedHashMap<String, Object> mains = new LinkedHashMap<String, Object>();
//		LinkedHashMap<String, Object> subs = new LinkedHashMap<String, Object>();
//		LinkedHashMap<String, Object> incls = new LinkedHashMap<String, Object>();
//		for (Root aRoot: roots) {
//			LinkedHashMap<String, Object> tree = mains;
//			if (aRoot.isSubroutine()) {
//				tree = subs;
//			}
//			else if (aRoot.isInclude()) {
//				tree = incls;
//			}
//			String qualName = aRoot.getQualifiedName(true);
//			StringList qualifier = StringList.explode(qualName, "\\.");
//			for (int i = 0; i < qualifier.count(); i++) {
//				String part = qualifier.get(i);
//				Object entry = stage.get(qualifier.get(0));
//				if (entry == null) {
//					entry = new LinkedHashMap<String, Object>();
//					stage.put(part, entry);
//				}
//
//			}
//		}
//	}

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
		Group selectedGroup = null;
		Root selectedRoot = null;
		if (indexSelected) {
			selectedGroup = arrangerIndexGetSelectedGroup();
			selectedRoot = arrangerIndexGetSelectedRoot();
		}
		Collection<Root> selectedRoots = this.arrangerIndexGetSelectedRoots(false);
		boolean isMutable = arrangerIndexSelectsMutable();
		
		//popupIndexGet.setEnabled(indexSelected && diagramIndex.getSelectedValue() != diagram.getRoot());
		//popupIndexSave.setEnabled(indexSelected && diagramIndex.getSelectedValue().hasChanged());
		popupIndexGet.setEnabled(indexSelected && arrangerIndexSelectsOtherRoot());
		popupIndexSave.setEnabled(indexSelected && arrangerIndexSelectsUnsavedChanges());
		popupIndexRemove.setEnabled(indexSelected && isMutable);
		//popupIndexCovered.setEnabled(indexSelected && Element.E_COLLECTRUNTIMEDATA && !arrangerIndex.getSelectedValue().isProgram());
		popupIndexCovered.setEnabled(indexSelected && Element.E_COLLECTRUNTIMEDATA && arrangerIndexSelectsNonProgram());
		// END KGU#318 2017-01-05
		// START KGU#573 2018-09-13: Enh. #590
		//popupIndexAttributes.setEnabled(indexSelected);
		popupIndexAttributes.setEnabled(selectedRoot != null);
		// END KGU#573 2018-09-13
		// START KGU#626 2019-01-03: Enh. #657
		popupIndexInfo.setEnabled(indexSelected &&
				(selectedRoot != null || selectedGroup != null));
		popupIndexGroup.setEnabled(selectedRoots.size() > 0);
		popupIndexExpandGroup.setEnabled(isMutable && (!selectedRoots.isEmpty()
				|| selectedGroup != null));
		popupIndexDissolve.setEnabled(selectedGroup != null && isMutable);
		popupIndexDetach.setEnabled(!selectedRoots.isEmpty() && isMutable);
		popupIndexAttach.setEnabled(!selectedRoots.isEmpty() && isMutable);
		// END KGU#626 2019-01-03
		// START KGU#630 2019-01-13: Enh. #662/2
		popupIndexDrawGroup.setEnabled(selectedGroup != null);
		popupIndexDrawGroup.setSelected(selectedGroup != null && selectedGroup.isVisible());
		// END KGU#630 2019-01-13
		// START KGU#669 2019-03-01: Enh. #691
		popupIndexRenameGroup.setEnabled(selectedGroup != null && isMutable);
		// END KGU#630 2019-01-13
		// START KGU#815 2020-03-16: Enh. #828
		popupIndexExport.setEnabled(selectedGroup != null || selectedRoot != null);
		//END KGU#815 2020-03-16
	}

	/** @return {@code true} on single selection of a {@link Root} that is not the currently edited diagram, false otherwise */
	private boolean arrangerIndexSelectsOtherRoot() {
		TreePath[] selectedPaths = this.getSelectionPaths();
		if (selectedPaths != null && selectedPaths.length == 1) {
			Object selectedObject = ((DefaultMutableTreeNode)selectedPaths[0].getLastPathComponent()).getUserObject();
			return (selectedObject instanceof Root && selectedObject != diagram.getRoot());
		}
		return false;
	}

	/** @return {@code true} if any of the selected {@link Root}s or {@link Group}s has unsaved changes, false otherwise */
	private boolean arrangerIndexSelectsUnsavedChanges() {
		TreePath[] selectedPaths = this.getSelectionPaths();
		if (selectedPaths != null) {
			for (TreePath path: selectedPaths) {
				Object selectedObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
				if (selectedObject instanceof Root && ((Root)selectedObject).hasChanged()) {
					return true;
				}
				else if (selectedObject instanceof Group && ((Group)selectedObject).hasChanged()) {
					return true;
				}
			}
		}
		return false;
	}

	/** @return {@code true} if a single non-main {@link Root} is selected */
	private boolean arrangerIndexSelectsNonProgram() {
		TreePath[] selectedPaths = this.getSelectionPaths();
		if (selectedPaths != null && selectedPaths.length == 1) {
			Object selectedObject = ((DefaultMutableTreeNode)selectedPaths[0].getLastPathComponent()).getUserObject();
			if (selectedObject instanceof Root) {
				return !((Root)selectedObject).isProgram();
			}
		}
		return false;
	}
	
	/** @return {@code true} if at least one {@link Root} or {@link Group} not representing
	 * DiagramControllers is among the selection */
	private boolean arrangerIndexSelectsMutable()
	{
		TreePath[] selectedPaths = this.getSelectionPaths();
		if (selectedPaths != null) {
			for (TreePath path: selectedPaths) {
				Object selectedObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
				if (selectedObject instanceof Root && !((Root)selectedObject).isRepresentingDiagramController()) {
					return true;
				}
				else if (selectedObject instanceof Group && !((Group)selectedObject).isDiagramControllerRepresentative()) {
					return true;
				}
			}
		}
		return false;
	}

	// START KGU#305/KGU#318 2017-01-05: Enh. #305/#319 Arranger index action methods concentrated here
	/** Action method for {@link #popupIndexGet}, summons the selected {@link Root} */
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

	/** @return the selected {@link Root} if exactly one {@link Root} is selected, otherwise {@code null} */
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

	/** @return the selected {@link Group} if exactly one {@link Group} is selected, otherwise {@code null} */
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

	/**
	 * @param groupMembersToo - if true then members of selected groups add to the result, otherwise
	 * selected groups will be ignored
	 * @return the selected roots (either ignoring selected groups or adding the roots of selected groups)
	 */
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

	/** @return the set of selected (or touched) {@link Group}s (multiple selection)
	 * @param partiallySelectedGroupsToo - if true then also yields groups when at least one of their
	 * members is selected, otherwise not.
	 */
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

	/** Action method for {@link #popupIndexSave}, saves all selected items with pending changes */
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

		// START KGU#901 2021-02-23: Issue #901 - WAIT_CURSOR on time-consuming actions
		Cursor prevCursor = this.getCursor();
		try {
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		// END KGU#901 2021-02-23
			// First save groups then save further roots (the latter may have got superfluous then)
			for (Group selectedGroup: selectedGroups) {
				if (selectedGroup.hasChanged()) {
					Group resultGroup = Arranger.getInstance().saveGroup(this, selectedGroup);
					// Now update the list of recent files in case the saving was successful
					if (resultGroup != null) {
						File groupFile = resultGroup.getArrzFile(true);
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
		// START KGU#901 2021-02-23: Issue #901 - WAIT_CURSOR on time-consuming actions
		}
		finally {
			this.setCursor(prevCursor);
		}
		// END KGU#901 2021-02-23
		// END KGU#626 2019-01-05
	}

	/** Action method for {@link #popupIndexRemove}, removes all selected {@link Root}s and {@link Group}s */
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
			// START KGU#901 2020-12-29: Issue #901 - WAIT_CURSOR on time-consuming actions
			Cursor origCursor = getCursor();
			// END KGU#901 2020-12-29
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
				// START KGU#901 2020-12-29: Issue #901 - WAIT_CURSOR on time-consuming actions
				this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				// END KGU#901 2020-12-29
				for (Group group: doomedGroups) {
					Arranger.getInstance().removeGroup(group.getName(), decision == JOptionPane.OK_OPTION, this);
				}
				decision = JOptionPane.OK_OPTION;
				if (!doomedRoots.isEmpty()) {
					// START KGU#901 2020-12-29: Issue #901 - WAIT_CURSOR on time-consuming actions
					this.setCursor(origCursor);
					// END KGU#901 2020-12-29
					decision = JOptionPane.showConfirmDialog(this,
							msgConfirmDeleteRoots.getText().replace("%", Integer.toString(doomedRoots.size())),
							popupIndexRemove.getText(),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (decision == JOptionPane.CANCEL_OPTION || decision == -1) {
						doomedRoots.clear();
					}
					// START KGU#901 2020-12-29: Issue #901 - WAIT_CURSOR on time-consuming actions
					else {
						this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					}
					// END KGU#901 2020-12-29
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
				// START KGU#901 2020-12-29: Issue #901 - WAIT_CURSOR on time-consuming actions
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				// END KGU#901 2020-12-29
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

	// START KGU#815 2020-03-16: Enh. #828
	/**
	 * Action method for popup menu items below {@link #popupIndexExport}
	 * @param generatorName - class name of the selected code generator
	 */
	protected void arrangerIndexExportCode(String generatorName) {
		Group selectedGroup = this.arrangerIndexGetSelectedGroup();
		Root selectedRoot = this.arrangerIndexGetSelectedRoot();
		if (selectedGroup != null) {
			diagram.exportGroup(selectedGroup, generatorName, null);
			this.repaint();
		}
		else if (selectedRoot != null) {
			diagram.export(selectedRoot, generatorName, null);
		}
	}
	// END KGU#815 2020-03-16
	
	// START KGU#396/KGU#815 2020-04-01: Enh. #440, #828
	/**
	 * Provisional action method for PapDesigner export menu items
	 * @param generatorName - class name of the selected code generator
	 */
	protected void arrangerIndexExportPap(boolean din66001_1982) {
		Group selectedGroup = this.arrangerIndexGetSelectedGroup();
		Root selectedRoot = this.arrangerIndexGetSelectedRoot();
		if (selectedGroup != null) {
			HashMap<String, Object> options = new HashMap<String, Object>();
			options.put("din66001_1982", din66001_1982);
			diagram.exportGroup(selectedGroup, "lu.fisch.structorizer.generators.PapGenerator", options);
			this.repaint();
		}
		else if (selectedRoot != null) {
			diagram.exportPap(selectedRoot, din66001_1982);
		}
	}
	// END KGU#396/KGU#815 2020-04-01

	// START KGU#626 2019-01-05: Enh. #657
	/**
	 * Produces a dialog with relevant information about the selected group or
	 * diagram as a tree view
	 */
	protected void arrangerIndexInfo() {
		// Let's see what it is
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
			Vector<Root> moreRoots = new Vector<Root>(Arranger.getInstance().accomplishRootSet(roots, null, missing));
			Collections.sort(moreRoots, Root.SIGNATURE_ORDER);
			for (Root root: moreRoots) {
				if (!root.equals(selectedRoot)) {
					if (root.isInclude()) {
						nodeIncludables.add(this.makeNodeWithGroups(root, null));
					}
					else if (root.isSubroutine()) {
						nodeSubroutines.add(this.makeNodeWithGroups(root, null));
					}
				}
			}
			// START KGU#703 2019-03-30: Enh. #720
			nodeDependingDiagrams.removeAllChildren();
			if (selectedRoot.isInclude()) {
				String name = selectedRoot.getMethodName();
				Vector<Root> dependents = new Vector<Root>(Arranger.getInstance().findIncludingRoots(name, false));
				Collections.sort(dependents, Root.SIGNATURE_ORDER);
				for (Root dependent: dependents) {
					if (Arranger.getInstance().findIncludesByName(name, dependent, false).contains(selectedRoot)) {
						nodeDependingDiagrams.add(makeNodeWithGroups(dependent, null));
					}
				}
				
			}
			else if (selectedRoot.isSubroutine()) {
				for (Root dependent: this.retrieveCallers(selectedRoot)) {
					nodeDependingDiagrams.add(makeNodeWithGroups(dependent, null));
				}
			}
			// END KGU#703 2019-03-30
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
			Collection<Root> expandedSet = Arranger.getInstance().accomplishRootSet(new HashSet<Root>(members), null, missing);
			for (Root root: expandedSet) {
				if (!members.contains(root)) {
					DefaultMutableTreeNode node = makeNodeWithGroups(root, selectedGroup);
					if (root.isSubroutine()) {
						this.nodeExternSubroutines.add(node);
					}
					else if (root.isInclude()) {
						this.nodeExternIncludables.add(node);
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

	// START KGU#703 2019-03-30: Enh. #720
	/** @return a sorted vector of diagrams calling the given subroutine (under group aspect) */
	private Vector<Root> retrieveCallers(Root subRoutine) {
		Vector<Root> callers = new Vector<Root>();
		for (Root candidate: Arranger.getSortedRoots()) {
			for (Call call: candidate.collectCalls()) {
				Function fct = call.getCalledRoutine();
				if (fct != null && fct.isFunction()
						&& Arranger.getInstance().findRoutinesBySignature(
								fct.getName(), 
								fct.paramCount(), 
								candidate, false).contains(subRoutine)) {
					callers.add(candidate);
					break;
				}
			}
		}
		Collections.sort(callers, Root.SIGNATURE_ORDER);
		return callers;
	}
	// END KGU#703 2019-03-30

	/** @return a new node for {@code root} with subnodes for every group {@code root} is member of except {@code selectedGroup} */
	private DefaultMutableTreeNode makeNodeWithGroups(Root root, Group selectedGroup) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(root);
		Collection<Group> groups = Arranger.getInstance().getGroupsFromRoot(root, false);
		for (Group group: groups) {
			if (group != selectedGroup) {
				node.add(new DefaultMutableTreeNode(group));
			}
		}
		return node;
	}

	// START KGU#626 2019-01-03: Enh #657
	private boolean arrangerIndexMakeGroup(boolean expand)
	{
		// FIXME: Check for single group selection, then expand the group!
		Group selectedGroup = this.arrangerIndexGetSelectedGroup();
		// START KGU#911 2021-01-11: Enh. #910 don't touch diagram controller representatives
		//if (selectedGroup != null) {
		if (selectedGroup != null && !selectedGroup.isDiagramControllerRepresentative()) {
		// END KGU#911 2021-01-11
			Collection<Root> expandedRootSet = Arranger.getInstance().accomplishRootSet(
					new HashSet<Root>(selectedGroup.getSortedRoots()), this, null);
			for (Root root: expandedRootSet) {
				// START KGU#911 2021-01-11: Enh. #910 don't touch diagram controller representatives
				//Arranger.getInstance().attachRootToGroup(selectedGroup, root, null, this);
				if (!root.isRepresentingDiagramController()) {
					Arranger.getInstance().attachRootToGroup(selectedGroup, root, null, this);
				}
				// END KGU#911 2021-01-11
			}
		}
		// START KGU#900 2020-12-31: Issue #902 focus got lost because of change notifications
		//Arranger.getInstance().makeGroup(this.arrangerIndexGetSelectedRoots(false), this, expand);
		boolean done = Arranger.getInstance().makeGroup(this.arrangerIndexGetSelectedRoots(false), this, expand);
		this.requestFocusInWindow();
		return done;
		// END KGU#900 2020-12-31

	}

	/** Dissolves the selected group(s) i.e. detaches all contained diagrams. If a diagram gets
	 * orphaned then it will be attached to the default group instead. The group may be deleted
	 * if it hadn't been associated to a file.<br/>
	 * In case {@code viaMenu} is {@code false} or more than one group is selected, a confirmation
	 * request will pop up.
	 * @param viaMenu - {@code true} if launched form a menu item, {@code false} otherwise
	 * (via accelerator)
	 */
	private boolean arrangerIndexDissolveGroup(boolean viaMenu) {
		Collection<Group> groups = this.arrangerIndexGetSelectedGroups(false);
		// TODO make a user query (multiple selection)
		// START KGU#900 2020-12-31: Issue #902 better ask for confirmation in certain cases
		if (!viaMenu || groups.size() > 1) {
			StringBuilder groupNames = new StringBuilder();
			for (Group group: groups) {
				// START KGU#911 2021-01-11: Enh. #910 Skip DiagramController group
				//groupNames.append("\n - ");
				//groupNames.append(group.getName());
				if (!group.isDiagramControllerRepresentative()) {
					groupNames.append("\n - ");
					groupNames.append(group.getName());
				}
				// END KGU#911 2021-01-11
			}
			int answer = JOptionPane.showConfirmDialog(this,
					msgConfirmDissolve.getText().replace("%", groupNames.toString()),
					this.popupIndexDissolve.getText(),
					JOptionPane.WARNING_MESSAGE);
			if (answer != JOptionPane.OK_OPTION) {
				return false;
			}
		}
		// END KGU#900 2020-12-31
		boolean done = true;
		for (Group group: groups) {
			// START KGU#911 2021-01-11: Enh. #910 Skip DiagramController group
			//done = Arranger.getInstance().dissolveGroup(group.getName(), this) && done;
			if (!group.isDiagramControllerRepresentative()) {
				done = Arranger.getInstance().dissolveGroup(group.getName(), this) && done;
			}
			// END KGU#911 2021-01-11
		}
		// START KGU#900 2020-12-31: Issue #902 focus got lost because of change notifications
		this.requestFocusInWindow();
		// END KGU#900 2020-12-31
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
				// START KGU#911 2021-01-11: Enh. #910 Skip DiagramController includables
				//if (rootObject instanceof Root && groupObject instanceof Group) {
				if (rootObject instanceof Root && groupObject instanceof Group
						&& !((Root)rootObject).isRepresentingDiagramController()
						&& !((Group)groupObject).isDiagramControllerRepresentative()) {
				// END KGU#911 2021-01-11
					done = Arranger.getInstance().detachRootFromGroup((Group)groupObject, (Root)rootObject, this) || done;
				}
			}
		}
		// START KGU#900 2020-12-31: Issue #902 focus got lost because of change notifications
		this.requestFocusInWindow();
		// END KGU#900 2020-12-31
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
		 * that the selected Root objects are members of many different groups - so it would
		 * cost too much effort for a rather unimportant effect: if the user selects the
		 * source group as target group then simply nothing will happen. Hence we don't bother.
		 * Nevertheless after the target was chosen we will of course take he actual paths
		 * into consideration.
		 */ 
		Collection<Root> roots = this.arrangerIndexGetSelectedRoots(false);
		if (!roots.isEmpty()) {
			int nGroups = this.arrangerIndexTop.getChildCount();
			for (int i = 0; i < nGroups; i++) {
				Object groupObject = ((DefaultMutableTreeNode)arrangerIndexTop.getChildAt(i)).getUserObject();
				// START KGU#911 2021-01-1: Enh. #910 don't offer Diagram Controllers group
				//if (groupObject instanceof Group && !((Group)groupObject).isDefaultGroup()) {
				if (groupObject instanceof Group && !((Group)groupObject).isDefaultGroup()
						&& !((Group)groupObject).isDiagramControllerRepresentative()) {
				// END KGU#911 2021-01-11
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
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					IconLoader.getIcon(117),
					options, options[0]);
			// START KGU#900 2020-12-31: Issue #902 Closing the pane was mis-interpreted as move acted
			//if (option < options.length-1) {
			if (option >= 0 && option < options.length-1) {
			// END KGU#900 2020-12-31
				Group targetGroup = (Group)cmbTargetGroup.getSelectedItem();
				if (option == 0) {
					// Simply add the roots to the target group
					for (Root root: roots) {
						// START KGU#911 2021-01-11: Enh. #910 Skip DiagramController includables
						//done = Arranger.getInstance().attachRootToGroup(
						//		targetGroup, root, null, this) || done;
						if (!root.isRepresentingDiagramController()) {
							done = Arranger.getInstance().attachRootToGroup(
									targetGroup, root, null, this) || done;
						}
						// END KGU#911 2021-01-11
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
							// START KGU#911 2021-01-11: Enh. #910 Skip DiagramController includables
							//if (rootObject instanceof Root && groupObject instanceof Group) {
							if (rootObject instanceof Root && groupObject instanceof Group
									&& !((Root)rootObject).isRepresentingDiagramController()
									&& !((Group)groupObject).isDiagramControllerRepresentative()) {
							// END KGU#911 2021-01-11
								done = Arranger.getInstance().attachRootToGroup(targetGroup, (Root)rootObject, (Group)groupObject, this) || done;
							}
						}
					}
				}
			}
		}
		// Tidy the combo box for later reuse
		cmbTargetGroup.removeAllItems();
		// START KGU#900 2020-12-31: Issue #902 focus got lost because of change notifications
		this.requestFocusInWindow();
		// END KGU#900 2020-12-31
		return done;
	}
	// END KGU#626 2019-01-03
	
	// START KGU#630 3019-01-13: Enh. #662/2
	private void arrangerIndexToggleGroupVis()
	{
		Group group = this.arrangerIndexGetSelectedGroup();
		if (group != null) {
			group.setVisible(!group.isVisible());
			Arranger.getInstance().routinePoolChanged(null, IRoutinePoolListener.RPC_GROUP_COLOR_CHANGED);
			this.requestFocusInWindow();
		}
	}
	private void arrangerIndexSetGroupVis(boolean show)
	{
		Group group = this.arrangerIndexGetSelectedGroup();
		if (group != null) {
			group.setVisible(show);
			Arranger.getInstance().routinePoolChanged(null, IRoutinePoolListener.RPC_GROUP_COLOR_CHANGED);
			// START KGU#900 2020-12-31: Issue #902 focus got lost because of change notifications
			this.requestFocusInWindow();
			// END KGU#900 2020-12-31
		}
	}
	// END KGU#630 2019-01-13
	
	// START KGU#669 2019-03-01: Enh. #691
	private void arrangerIndexRenameGroup()
	{
		Group group = this.arrangerIndexGetSelectedGroup();
		// START KGU#911 2021-01-11: Enh. #910 Skip DiagramController includables
		//if (group != null) {
		if (group != null && !group.isDiagramControllerRepresentative()) {
		// END KGU#911 2021-01-11
			String newName = JOptionPane.showInputDialog(this, msgNewGroupName.getText(), group.getName());
			// Surface does not allow manually to rename a group in "Diagram Controllers"
			Arranger.getInstance().renameGroup(group, newName, this);
			// START KGU#900 2020-12-31: Issue #902 focus got lost because of change notifications
			this.requestFocusInWindow();
			// END KGU#900 2020-12-31
		}
	}
	// END KGU#669 2019-03-01
	
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
				// START KGU#900 2020-12-31: Bugfix #902
				// We must regain the focus that will have been snatched off by notifications
				this.requestFocusInWindow();
				// END KGU#900 2020-12-31
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

	// START KGU#643 2019-01-28: Issues #670, #674 - Update info box and popup menu L&F (aren't members of the component tree)
	@Override
	public void updateUI()
	{
		super.updateUI();
		// Cater for the look and feel update of the info boxes (2019-02-04: #674 - popupIndex added).
		for (Component comp: new Component[] {this.scrollInfo, this.pnlGroupInfo, this.popupIndex}) {
			if (comp != null) {
				try {
					javax.swing.SwingUtilities.updateComponentTreeUI(comp);
				}
				catch (Exception ex) {
					System.out.println("L&F problem with " + comp);
				}
			}
		}
	}
	// END KGU#643 2019-01-28

	// START KGU#396/KGU#815 2020-04-08: Enh. #440, #828 - group export to PapDesigner files
	@Override
	public void LangChanged(LangEvent evt) {
		if (evt.getSource() == Menu.msgExportTooltip) {
			popupIndexExportPap.setToolTipText(Menu.msgExportTooltip.getText().replace("%", "https://www.heise.de/download/product/papdesigner-51889"));
		}		
	}
	// END KGU#396/KGU#815 2020-04-08

	/**
	 * Controls whether GUI elements providing code import or export be
	 * suppressed
	 * @param restricted - true to disable menu items offering export
	 */
	// START KGU#870 2020-06-06: Bugfix #870
	public void hideExportImport()
	{
		popupIndexExport.setVisible(false);
	}
	// END KGU#870 2020-06-06
}

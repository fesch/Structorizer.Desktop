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
 *      Description:    Preferences dialog for configuration of user-specific routine name aliases
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2018-01-08      First Issue (on behalf of enhancement request #490)
 *      Kay Gürtzig     2018-01-22      Accomplished.
 *      Kay Gürtzig     2019-02-26      Placed relative to owner, indentations aligned
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - Andreas Brusinski proposed to add German aliases to the Turtleizer API definition to
 *        facilitate the learning by German pupils, which may not be very familiar with English.
 *        But this approach would sooner or later have meant to add an unlimited number of routine name
 *        aliases for further languages. Moreover, Executor and all Generators would have to translate
 *        all these additional names, too. So an alternative approach was proposed: A new preferences
 *        category allowing users (or teachers) to specify a single user-specific set of alias names
 *        for DiagramController offsprings, which may be accepted on editing the diagram and would be
 *        presented on drawing (and editing) the diagram instead of the "real" names, which would still
 *        be the only stored ones in the diagrams. This dialog would also have to ensure that no
 *        ambiguities arise, i.e. it must check for name collisions with other aliases or built-in routines.
 *        This check may not of course prevent future trouble with new built-in routines or plugins of
 *        the DiagramController type.
 *        But we get a tricky problem here: In order to retrieve the API we must consult an instance of
 *        the respective DiagramController subclass, which may be a heavy-weight object or cause side
 *        effects like TurtleBox (it opens a window on instantiating). So the aim is to avoid this and
 *        also to avoid a redundant mirror API configuration as e.g. in the XML plugin file or in a Java
 *        adapter class (like Turtleizer).
 *        So DiagramController interface now states that the standard constructor is to produce a light-
 *        weight instance suitable for cheap API retrieval. 
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.util.Vector;

import lu.fisch.diagrcontrol.DiagramController;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangDialog;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.utils.StringList;

/**
 * Tabular Dialog allowing to specify alias names for configured routines (functions
 * and procedures) of {@link DiagramController} subclasses.  
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")
public class DiagramControllerAliases extends LangDialog implements PropertyChangeListener {
	
	public boolean OK = false;
	private Vector<GENPlugin> plugins;
	private final ArrayList<ArrayList<String[]>> routineMaps =
			new ArrayList<ArrayList<String[]>>();
	private javax.swing.JTabbedPane tabs;
	private JButton btnOK;
	private Vector<JTable> tables = new Vector<JTable>();
	private JLabel lblHeader;
	private JLabel lblRemark;
	public JCheckBox chkApplyAliases;
	private JPanel pnlHeader;
	private JPanel pnlContent;
	private JPanel pnlButtons;
	public static final LangTextHolder hlpEndEditing = new LangTextHolder("End cell editing by pressing <Enter> or <Esc> or by clicking elsewhere.");
	public static final LangTextHolder msgIdentifierRequired = new LangTextHolder("Your alias contains illegal characters."
			+ "\nA routine name alias must either be empty or an identifier"
			+ "\n(i.e. only consist of letters, digits, and underscores)."
			+ "\n\nThe original text will be restored.");
	public static final LangTextHolder ttlIllegalValues = new LangTextHolder("Illegal alias");
	public static final LangTextHolder msgIn = new LangTextHolder("in");
	public static final LangTextHolder msgConflictsFound = new LangTextHolder("There are signature conflicts, changes can't be committed:"
			+ "\n\n%\n\n"
			+ "You must resolve these conflicts first. (The conflicts are copied to the clipboard.)");

	/** 
	 * Comparator for String triples the third element of which is a symbolic method signature
	 * to be ordered lexicographically.
	 */
	private static final Comparator<String[]> SIGNATURE_ORDER =
			new Comparator<String[]>() {
				public int compare(String[] tuple1, String[] tuple2)
				{
			return tuple1[2].compareTo(tuple2[2]);
		}
	};
		
	/**
	 * This class represents a tab for the {@link DiagramComponentAliases} dialog, the
	 * only component is a two-column {@link JTable} showing routine signatures in the
	 * first column and the user-defined aliases in the second column.
	 * @author Kay Gürtzig
	 */
	public class AliasTab extends javax.swing.JPanel {

		private javax.swing.JScrollPane scrollPane;
		private javax.swing.JTable table;

		/**
		 * Creates new form Tab
		 */
		public AliasTab() {
			initComponents();

			// configure the table
			table.setRowHeight(25);

			DefaultTableModel model = ((DefaultTableModel)table.getModel());
			model.setColumnCount(2);
			model.setRowCount(0);
			table.getColumnModel().getColumn(0).setHeaderValue(Menu.msgTabHdrSignature.getText());
			table.getColumnModel().getColumn(1).setHeaderValue(Menu.msgTabHdrAlias.getText());
			table.getTableHeader().repaint();
		}

		/** @return the {@link JTable} being the essential component of this tab */
		public JTable getTable()
		{
			return table;
		}

		/**
		 * This method is called from within the constructor to initialize the form.
		 */
		private void initComponents() {

			scrollPane = new javax.swing.JScrollPane();
			table = new javax.swing.JTable();

			setLayout(new java.awt.BorderLayout());

			table.setModel(new AliasTableModel());

			scrollPane.setViewportView(table);

			add(scrollPane, java.awt.BorderLayout.CENTER);
		}

	}

	/**
	 * Internal {@link TableModel} class for the alias tables, ensuring that only the second
	 * column is editable. 
	 */
	public class AliasTableModel extends DefaultTableModel {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int row, int column){
			return (column == 1);  
		}

	}
	
	/**
	 * Creates a dialog that allows the definition of alias names for all routines offered
	 * by the Structorizer API of the {@link DiagramController}s described by the given
	 * {@code controllerPlugins}.<br/>
	 * Does not automatically get visible ({@link #setVisible(boolean)}) will have to be
	 * applied explicitly thefore.
	 * @param owner - the {@link Frame} this dialog is to be owned by 
	 * @param controllerPlugins - vector of available controller plugins
	 */
	public DiagramControllerAliases(Frame owner, Vector<GENPlugin> controllerPlugins) {
		super(owner);
		plugins = controllerPlugins;
		setModal(true);
		initComponents();
	}
	
	private void initComponents()
	{
		Ini ini = Ini.getInstance();
		double scaleFactor = Double.valueOf(ini.getProperty("scaleFactor","1"));
		if (scaleFactor < 1) scaleFactor = 1.0;

		setTitle("Controller Routine Aliases");

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		KeyListener keyListener = new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent kevt) 
			{
				if(kevt.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					setVisible(false);
				}
				else if(kevt.getKeyCode() == KeyEvent.VK_ENTER && (kevt.isShiftDown() || kevt.isControlDown()))
				{
					okAction();
				}
			}
			
			public void keyReleased(KeyEvent kevt) {} 
			public void keyTyped(KeyEvent kevt) {}
		};

		//======== header panel ========
		{
			pnlHeader = new JPanel();
			pnlHeader.setBorder(new EmptyBorder(10, 10, 10, 10));
			pnlHeader.setLayout(new GridLayout(0, 1));
			lblHeader = new JLabel("Specify alias names for the routines of controllable add-ons.");
			pnlHeader.add(lblHeader);
			lblRemark = new JLabel("(Note that case is ignored with controller routine names!)");
			pnlHeader.add(lblRemark);
			chkApplyAliases = new JCheckBox("Apply the specified aliases on display etc.");
			chkApplyAliases.setSelected(Element.E_APPLY_ALIASES);
			chkApplyAliases.addKeyListener(keyListener);
			pnlHeader.add(chkApplyAliases);
		}

		//======== content tabs ========
		{
			pnlContent = new JPanel();
			//pnlContent.setBorder(new EmptyBorder(10, 10, 10, 10));
			pnlContent.setLayout(new BorderLayout());
			
			tabs = new javax.swing.JTabbedPane();
			StringList errors = new StringList();
			for (int i = 0; i < plugins.size(); i++) {
				ArrayList<String[]> routineList = new ArrayList<String[]>();
				GENPlugin plugin = plugins.get(i);
				final String className = plugin.className;
				try {
					Class<?> ctrClass = Class.forName(className);
					DiagramController ctrl = (DiagramController)ctrClass.getDeclaredConstructor().newInstance();
					HashMap<String, Method> routineMap = ctrl.getProcedureMap();
					extractSignatures(className, routineList, routineMap, ini, false);
					routineMap = ((DiagramController)ctrClass.getDeclaredConstructor().newInstance()).getFunctionMap();
					extractSignatures(className, routineList, routineMap, ini, true);
					Collections.sort(routineList, SIGNATURE_ORDER);
					routineMaps.add(routineList);

					// create a new tab
					AliasTab tab = new AliasTab();
					// add it to the panel
					tabs.add(plugin.title, tab);

					JTable table = tab.getTable();
					table.setName(plugin.title);
					table.setGridColor(Color.LIGHT_GRAY);
					table.setShowGrid(true);
					table.addKeyListener(keyListener);
					tables.add(table);
					if (scaleFactor > 2.0) {
						table.setRowHeight((int)Math.ceil(table.getRowHeight() * (scaleFactor - 1)*0.75));
					}
					DefaultTableModel model = ((DefaultTableModel)table.getModel());
					for (String[] tuple: routineList) {
						model.addRow(new String[]{tuple[0], tuple[1]});
					}
					table.addPropertyChangeListener("tableCellEditor", this);
				} catch (Exception ex) {
					errors.add(plugin.title + ": " + ex.getLocalizedMessage());
				}
			}
			pnlContent.add(tabs, BorderLayout.CENTER);
		}
		
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//======== button bar ========
		{
			pnlButtons = new JPanel();
			
			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent arg0) {
					okAction();
				}

			});
			btnOK.addKeyListener(keyListener);
			
			pnlButtons.setBorder(new EmptyBorder(10, 10, 10, 10));
			GridBagLayout gblButtons = new GridBagLayout(); 
			pnlButtons.setLayout(gblButtons);
			gblButtons.columnWidths = new int[] {0, 80};
			gblButtons.columnWeights = new double[] {1.0, 0.0};
			
			pnlButtons.add(btnOK, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
														   GridBagConstraints.CENTER, GridBagConstraints.BOTH,
														   new Insets(0, 0, 0, 0), 0, 0));
		}

		contentPane.add(pnlHeader, BorderLayout.NORTH);
		contentPane.add(pnlContent, BorderLayout.CENTER);
		contentPane.add(pnlButtons, BorderLayout.SOUTH);

		GUIScaler.rescaleComponents(this);

		pack();
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Extracts the method signatures from the API specification {@code _routineMap} for
	 * {@link DiagramController} {@code _pluginName} and adds a String tuples consisting
	 * of [0] this signature, [1] the configured routine alias and [2] the API specification
	 * key each to {@code _routineList}. The signatures depend on argument {@code _withResult}
	 * and look slightly Pascal-like:<br/>
	 * {@code <routine_name>(<arg_type1>, <arg_type2>, ...): <result_type>}  
	 * @param _className - qualified class name of the {@link DiagramController}
	 * @param _routineList - the list of String tuples the extracted routine signatures are to be added to
	 * @param _routineMap - the API map for procedures or functions, mapping a signature key to a {@link Method}
	 * @param _ini - an ini file proxy providing available user preferences 
	 * @param _withResult - if result types are to be added to the signatures (use true for function maps)
	 */
	protected void extractSignatures(String _className, ArrayList<String[]> _routineList, HashMap<String, Method> _routineMap, Ini _ini, boolean _withResult)
	{
		for (Entry<String, Method> entry: _routineMap.entrySet()) {
			String keyName = entry.getKey();
			String routineName = entry.getValue().getName();
			StringList paramTypeNames = new StringList();
			for (Class<?> paramType: entry.getValue().getParameterTypes()) {
				paramTypeNames.add(paramType.getSimpleName());
			}
			keyName = keyName.substring(0, keyName.indexOf('#'));
			if (!keyName.equalsIgnoreCase(routineName)) {
				routineName = keyName;
			}
			String signature = routineName + "(" + paramTypeNames.concatenate(",") + ")";
			if (_withResult) {
				signature += ": " + entry.getValue().getReturnType().getSimpleName();
			}
			String routineAlias = _ini.getProperty(_className+"."+entry.getKey(), "");
			_routineList.add(new String[]{signature, routineAlias, entry.getKey()});
		}
	}
	
	/**
	 * Performs the necessary consistency checks and stores the changes if not
	 * conflicting.
	 */
	protected void okAction() {
		StringList conflicts = adoptAliases(false);
		if (conflicts == null) {
			OK = true;
			Element.controllerName2Alias.clear();
			Element.controllerAlias2Name.clear();
			adoptAliases(true);
			setVisible(false);
		}
		else {
			JOptionPane.showMessageDialog(DiagramControllerAliases.this,
					msgConflictsFound.getText().replace("%", conflicts.getText()),
					ttlIllegalValues.getText(),
					JOptionPane.ERROR_MESSAGE);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection toClip = new StringSelection(conflicts.getText());
			clipboard.setContents(toClip, null);									
		}
	}

	/**
	 * Checks the contents of the {@link JTable}s of all tabs and adopts all changes
	 * in the {@link Ini} instance if the user had left the dialog via the OK button.<br/>
	 * Does not save the Ini properties to file, though.
	 * @param commit - whether the aliases are to be committed to the {@link Ini} instance
	 *  (otherwise only a conflict check will be done)
	 * @return a {@link StringList} of signature conflicts if there any, null otherwise.
	 */
	private StringList adoptAliases(boolean commit) {
		boolean conflictFree = true;
		StringList conflicts = null;
		// The keys are the signatures (with '#'), the values list of referencing routines (title + '.' + name(arity))
		HashMap<String, StringList> signatureMap = new HashMap<String, StringList>();
		Ini ini = Ini.getInstance();
		for (int i = 0; i < tables.size(); i++) {
			JTable tab = tables.get(i);
			GENPlugin plugin = plugins.get(i);
			ArrayList<String[]> routineList = routineMaps.get(i);
			String className = plugin.className;
			DefaultTableModel model = (DefaultTableModel)tab.getModel();
			for (int j = 0; j < routineList.size(); j++) {
				String[] tuple = routineList.get(j);
				String[] sign = tuple[2].split("#");
				String name = tuple[0].substring(0,  tuple[0].indexOf("("));
				String origin = plugin.title + "." + name + "(" + sign[1] + ")";
				String signKey = tuple[2];
				// Register the original names (lest there should be some cyclic renaming...)
				if (signatureMap.containsKey(signKey)) {
					signatureMap.get(signKey).add(origin);
					conflictFree = false;
				}
				else {
					signatureMap.put(signKey, StringList.getNew(origin));
				}
				String alias = ((String) model.getValueAt(j, 1)).trim();
				if (!alias.isEmpty() && !alias.equalsIgnoreCase(name)) {
					// Now register the alias signature as well
					signKey = alias.toLowerCase() + "#" + sign[1];
					if (signatureMap.containsKey(signKey)) {
						signatureMap.get(signKey).add(origin);
						conflictFree = false;
					}
					else {
						signatureMap.put(signKey, StringList.getNew(origin));
					}
				}
				if (commit) {
					if (!alias.equals(tuple[1])) {
						ini.setProperty(className + "." + tuple[2], alias);
					}
					if (!alias.isEmpty()) {
						Element.controllerName2Alias.put(tuple[2], alias);
						Element.controllerAlias2Name.put(alias.toLowerCase() + "#" + sign[1], name);
					}
				}
			}
		}
		if (!conflictFree) {
			conflicts = new StringList();
			for (Entry<String, StringList> entry: signatureMap.entrySet()) {
				StringList controllers = entry.getValue();
				if (controllers.count() > 1) {
					String[] sigParts = entry.getKey().split("#");
					conflicts.add(sigParts[0] + "(" + sigParts[1] + ") " + msgIn.getText() + ": "
							+ controllers.concatenate(", "));
				}
			}
		}
		return conflicts;
	}

	/**
	 * Property change listener method reacting to cell editing events of a {@link JTable} in one
	 * of the tabs. 
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ((evt.getSource() instanceof JTable)) {
			JTable table = (JTable)evt.getSource();
			Object cellEditor = evt.getNewValue(); 
			if (cellEditor == null) {
				// Editing finished, identify the edited cell
				int rowNo = table.getSelectedRow();
				String newValue = (String)table.getModel().getValueAt(rowNo, 1);
				if (newValue != null) {
					if (!newValue.isEmpty() && !Function.testIdentifier(newValue, null)) {
						JOptionPane.showMessageDialog(this,
								msgIdentifierRequired.getText(),
								ttlIllegalValues.getText(),
								JOptionPane.WARNING_MESSAGE);
						String pluginName = table.getName();
						for (int i = 0; i < this.plugins.size(); i++) {
							if (plugins.get(i).title.equals(pluginName)) {
								table.getModel().setValueAt(this.routineMaps.get(i).get(rowNo)[1], rowNo, 1);
							}
						}
					}
				}
				btnOK.setEnabled(true);
				btnOK.setToolTipText(null);
			}
			else {
				btnOK.setEnabled(false);
				btnOK.setToolTipText(hlpEndEditing.getText());
			}
			
		}
	}

}

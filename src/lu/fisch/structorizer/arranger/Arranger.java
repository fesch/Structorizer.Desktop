/*
 Structorizer :: Arranger
 A little tool which you can use to arrange Nassi-Shneiderman Diagrams (NSD)

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
 *      Author: Bob Fisch
 *
 *      Description: This class offers an opportunity to graphically arrange several
 *      NSD diagrams within one and the same drawing area. While related to owned
 *      Structorizers, the diagrams will fully and synchronously reflect all status
 *      changes (selection, execution, ...)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date        Description
 *      ------          ----        -----------
 *      Bob Fisch       2009-08-18  First Issue
 *      Kay Gürtzig     2015-10-18  Transient WindowsListener added enabling Surface to have dirty
 *                                  diagrams saved before exit (KGU#49)
 *      Kay Gürtzig     2015-11-17  Remove button added (issue #35 = KGU#85)
 *      Kay Gürtzig     2015-11-19  Converted into a singleton (enhancement request #9 = KGU#2)
 *      Kay Gürtzig     2015-11-24  Pin button added (issue #35, KGU#88)
 *      Kay Gürtzig     2015-11-30  Remove action now also achievable by pressing del button (issue #35, KGU#88)
 *      Kay Gürtzig     2015-12-21  Two new buttons for saving and loading arrangements (issue #62, KGU#110)
 *      Kay Gürtzig     2016-01-05  Icons for saving and loading arrangements replaced by fitting ones
 *      Kay Gürtzig     2016-03-08  Bugfix #97: Methods for drawing info invalidation added (KGU#155)
 *      Kay Gürtzig     2016-03-08  Method clearExecutionStatus and btnSetCovered added (for Enhancement #77)
 *      Kay Gürtzig     2016-03-12  Enh. #124 (KGU#156): Generalized runtime data visualisation hooks
 *      Kay Gürtzig     2016-04-14  Enh. #158 (KGU#177): Keys for copy and paste enabled, closing
 *                                  mechanism modified
 *      Kay Gürtzig     2016-07-03  Dialog message translation mechanism added (KGU#203).
 *      Kay Gürtzig     2016-09-26  Enh. #253: New public method getAllRoots() added.
 *      Kay Gürtzig     2016-11-01  Enh. #81: Scalability of the Icons ensured
 *      Kay Gürtzig     2016-11-15  Enh. #290: New opportunity to load arrangements from Structorizer
 *      Kay Gürtzig     2016-12-12  Enh. #305: Support for diagram list in Structorizer
 *      Kay Gürtzig     2016-12-16  Issue #305: Notification redesign, visibility fix in scrollToDiagram,
 *                                  new method removeDiagram(Root)
 *      Kay Gürtzig     2017-01-04  KGU#49: Arranger now handles windowClosing events itself (instead
 *                                  of a transient WindowAdapter). This allows Mainform to warn Arranger
 *      Kay Gürtzig     2017-03-28  Enh. #386: New method saveAll()
 *      Kay Gürtzig     2018-02-17  Enh. #512: Zoom mechanism implemented (zoom button + key actions)
 *      Kay Gürtzig     2018-06-12  Issue #536: Experimental workaround for Direct3D trouble
 *      Kay Gürtzig     2018-10-06  Issue #552: New method hasUnsavedChanges() for smarter serial action handling
 *      Kay Gürtzig     2018-12-20  Issue #654: Current directory is now passed to the ini file
 *      Kay Gürtzig     2018-12-21  Enh. #655: Status bar introduced, key bindings revised
 *      Kay Gürtzig     2018-12-27  Enh. #655: Set of key bindings accomplished, dialog revision, popup menu
 *      Kay Gürtzig     2019-01-12  Enh. #662/3: Rearrangement by groups
 *      Kay Gürtzig     2019-01-13  Enh. #662/4: Save option to use relative coordinates
 *      Kay Gürtzig     2019-01-16  Enh. #655: keyPressed()/keyReleased() had to be replaced by Keybinding
 *
 ******************************************************************************************************
 *
 * Comment:	/
 *
 ******************************************************************************************************///

import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;

import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.RootAttributes;
import lu.fisch.structorizer.executor.IRoutinePool;
import lu.fisch.structorizer.executor.IRoutinePoolListener;
import lu.fisch.structorizer.gui.AttributeInspector;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.gui.Mainform;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangEvent;
import lu.fisch.structorizer.locales.LangEventListener;
import lu.fisch.structorizer.locales.LangFrame;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.utils.StringList;

/**
 * This class graphically arranges several Nassi-Shneiderman diagrams within
 * one and the same drawing area.
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class Arranger extends LangFrame implements WindowListener, /*KeyListener,*/ IRoutinePool, IRoutinePoolListener, LangEventListener {

	// START KGU#630 2019-01-13: Enh.#662/4
	/** Preference specifying whether group-relative coordinaes are to be stored in an arrangement file (default: absolute coordinates) */
	public static boolean A_STORE_RELATIVE_COORDS = false;
	// END KGU#630 2019-01-13
	
	// START KGU 2018-03-21
	public static final Logger logger = Logger.getLogger(Arranger.class.getName());
	// END KGU 2018-03-21
	
	// START KGU#177 2016-04-14: Enh. #158 - because of pasting opportunity we must take more care
	private boolean isStandalone = false;
	// END KGU#177 2016-04-14
	
	private static final String KEY_SHIFT = "KEY_SHIFT";
	private static final String KEY_DELETE = "KEY_DELETE";
	private static final String KEY_CTRL_DELETE = "KEY_CTRL_DELETE";
	private static final String KEY_COPY = "KEY_COPY";
	private static final String KEY_PASTE = "KEY_PASTE";
	private static final String KEY_CUT = "KEY_CUT";
	private static final String KEY_ALL = "KEY_ALL";
	private static final String KEY_MAKE_GROUP = "KEY_MAKE_GROUP";
	private static final String KEY_EXPAND_GROUP = "KEY_EXPAND_GROUP";
	private static final String KEY_OPEN = "KEY_OPEN";
	private static final String KEY_SAVE = "KEY_SAVE";
	private static final String KEY_HELP = "KEY_HELP";
	private static final String KEY_KEYS = "KEY_KEYS";
	private static final String KEY_ZOOM_IN = "KEY_ZOOM_IN";
	private static final String KEY_ZOOM_OUT = "KEY_ZOOM_OUT";
	private static final String KEY_LEFT = "KEY_LEFT";
	private static final String KEY_LEFT10 = "KEY_LEFT10";
	private static final String KEY_RIGHT = "KEY_RIGHT";
	private static final String KEY_RIGHT10 = "KEY_RIGHT10";
	private static final String MOVE_LEFT = "MOVE_LEFT";
	private static final String MOVE_LEFT10 = "MOVE_LEFT10";
	private static final String MOVE_RIGHT = "MOVE_RIGHT";
	private static final String MOVE_RIGHT10 = "MOVE_RIGHT10";
	private static final String KEY_UP = "KEY_UP";
	private static final String KEY_UP10 = "KEY_UP10";
	private static final String KEY_DOWN = "KEY_DOWN";
	private static final String KEY_DOWN10 = "KEY_DOWN10";
	private static final String MOVE_UP = "MOVE_UP";
	private static final String MOVE_UP10 = "MOVE_UP10";
	private static final String MOVE_DOWN = "MOVE_DOWN";
	private static final String MOVE_DOWN10 = "MOVE_DOWN10";
	private static final String KEY_PAGE_UP = "KEY_PAGE_UP";
	private static final String KEY_PAGE_DOWN = "KEY_PAGE_DOWN";
	private static final String KEY_PAGE_LEFT = "KEY_PAGE_LEFT";
	private static final String KEY_PAGE_RIGHT = "KEY_PAGE_RIGHT";
	private static final String KEY_HOME_V = "KEY_HOME_V";
	private static final String KEY_END_V = "KEY_END_V";
	private static final String KEY_HOME_H = "KEY_HOME_H";
	private static final String KEY_END_H = "KEY_END_H";
	private static final String KEY_ALT_ENTER = "KEY_ALT_ENTER";

	// START KGU#534 2018-06-27: Enh. #552
	public static final LangTextHolder msgConfirmRemoveAll = new LangTextHolder("Do you really want to remove all diagrams from Arranger?");
	public static final LangTextHolder msgTitleWarning = new LangTextHolder("Warning");
	public static final LangTextHolder btnRemoveDiagrams = new LangTextHolder("Drop Diagram");
	public static final LangTextHolder btnRemoveAllDiagrams = new LangTextHolder("Remove All");
	// END KGU#534 2018-06-27
	// START KGU#624 2018-12-21/24: Enh. #655
	public static final LangTextHolder msgTitleIllegal = new LangTextHolder("Illegal Operation!");
	public static final LangTextHolder msgActionDelete = new LangTextHolder("remove");
	public static final LangTextHolder msgActionCut = new LangTextHolder("cut");
	public static final LangTextHolder msgActionCopy = new LangTextHolder("copy");
	public static final LangTextHolder msgConfirmMultiple = new LangTextHolder("You have selected the following %1 diagram(s) (out of %2):\n- %3\n\n%4");
	public static final LangTextHolder msgConfirmRemove = new LangTextHolder("Do you really want to %1 the above diagram(s) from Arranger?");
	public static final LangTextHolder msgReadyToExport = new LangTextHolder("Are you ready to export this sub-arrangement to PNG?");
	public static final LangTextHolder msgCantDoWithMultipleRoots = new LangTextHolder("It is not possible to %1 more than one diagram at a time. You selected %2 diagram(s):\n- %3");
	public static final LangTextHolder msgDiagramsSelected = new LangTextHolder("diagrams: %1, selected: %2");
	public static final LangTextHolder msgBrowseFailed = new LangTextHolder("Failed to show % in browser");
	public static final LangTextHolder msgTitleURLError = new LangTextHolder("URL Error");
	public static final LangTextHolder msgSelectionExpanded = new LangTextHolder("% referenced diagram(s) added to selection.");
	public static final LangTextHolder msgMissingDiagrams = new LangTextHolder("\n\n%1 referenced diagram(s) not found:\n- %2");
	public static final LangTextHolder msgAmbiguousSignatures = new LangTextHolder("\n%1 ambiguous signatures among the selection, i.e. with several matching diagrams:\n- %2");
	// END KGU#624 2018-12-21/26
	// START KGU#626 2019-01-02: Enh. #657
	public static final LangTextHolder msgEmptySelection = new LangTextHolder("Can't % diagrams: nothing selected.");
	public static final LangTextHolder msgActionGroup = new LangTextHolder("group");
	public static final LangTextHolder msgGroupName = new LangTextHolder("%1Name for the group the %2 selected diagrams are to join:");
	public static final LangTextHolder msgGroupNameRejected = new LangTextHolder("Group name \"%\" was rejected.");
	public static final LangTextHolder msgGroupExists = new LangTextHolder("There is already a group with name \"%\". What do want to do?");
	public static final LangTextHolder[] msgGroupingOptions = new LangTextHolder[] {
			new LangTextHolder("Add diagrams"),
			new LangTextHolder("Replace group"),
			new LangTextHolder("Try other group name"),
			new LangTextHolder("Cancel")
	};
	public static final LangTextHolder msgCongruentGroups = new LangTextHolder("The set of the %1 selected diagrams is congruent with %2 existing group(s):\n- %3\n\n%4");
	public static final LangTextHolder msgDoCreateGroup = new LangTextHolder("Do you still insist on creating yet another group?");
	// END KGU#626 2019-01-02
	
	// START KGU#624 2018-12-26: Enh. #655 - (temporary) limit for the listing of selected diagrams in a message box
	protected static final int ROOT_LIST_LIMIT = 20;
	// END KGU#624 2018-12-26

    // START KGU#2 2015-11-19: Enh. #9 - Converted into a singleton class
    //** Creates new form Arranger */
    //public Arranger() {
    //    initComponents();
    //}
    private static Arranger mySelf = null;

    /**
     * Returns the Arranger instance (if it is to be created then it will be as
     * a dependent frame)
     */
    public static Arranger getInstance() {
        return getInstance(false);
    }

    /**
     * Returns the Arranger instance
     *
     * @param standalone - if true then the instance will exit on close
     * otherwise only dispose (works only on actual creation)
     */
    public static Arranger getInstance(boolean standalone) {
        if (mySelf == null) {
            mySelf = new Arranger(standalone);
        }
        return mySelf;
    }

    // START KGU#155 2016-03-08: added for bugfix #97
    /**
     * Allows to find out whether an Arranger instance is created without
     * creating it.
     * @return true iff there is already an Arranger instance
     */
    public static boolean hasInstance() {
        return mySelf != null;
    }
    // END KGU#155 2016-03-08
    
    // START KGU#305 2016-12-12: Enh. #305
    /**
     * Scrolls to the given Root if found and selects it. If setAtTop is true then the diagram
     * will be raised to the top drawing level.
     * @param aRoot - the diagram to be focused
     * @param setAtTop - whether the diagram is to be drawn on top of all
     */
    public static void scrollToDiagram(Root selectedRoot, boolean raiseToTop)
    {
    	if (mySelf != null && selectedRoot != null) {
    		// START KGU#305 2016-12-16: Bugfix #305 - possibly we must wake the instance
    		if (!mySelf.isVisible()) {
    			mySelf.setVisible(true);
    		}
    		// END KGU#305 2016-12-16
    		mySelf.surface.scrollToDiagram(selectedRoot, raiseToTop);
    	}
    }
    // END KGU#305 2016-12-12

    // START KGU#626 2019-01-01: Enh. #657
    /**
     * Scrolls to the given Root if found and selects it. If setAtTop is true then the diagram
     * will be raised to the top drawing level.
     * @param aRoot - the diagram to be focused
     * @param setAtTop - whether the diagram is to be drawn on top of all
     */
    public static void scrollToGroup(Group selectedGroup)
    {
    	if (mySelf != null && selectedGroup != null) {
    		// START KGU#305 2016-12-16: Bugfix #305 - possibly we must wake the instance
    		if (!mySelf.isVisible()) {
    			mySelf.setVisible(true);
    		}
    		// END KGU#305 2016-12-16
    		mySelf.surface.scrollToGroup(selectedGroup);
    	}
    }
    // END KGU#626 2019-01-01

    /**
     * Creates new form Arranger
     *
     * @param standalone - if Arranger is started as application (or only as
     * dependent frame)
     */
    private Arranger(boolean standalone) {
        initComponents();
        // START KGU#177 2016-04-14: Enh. #158
        //setDefaultCloseOperation(standalone ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
        isStandalone = standalone;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // END KGU#177 2016-04-14
        // START KGU#305 2016-12-16
        surface.addChangeListener(this);
        // END KGU#305 2016-12-16
    }

    /**
     * Places the passed-in root at a free space on the Arranger surface if it
     * hasn't been placed there already. Relates the given frame to it if it is
     * a Mainform instance.
     *
     * @param root - The diagram root to be added to the Arranger
     * @param frame - potentially an associable Mainform (Structorizer)
     */
    public void addToPool(Root root, JFrame frame) {
        if (frame instanceof Mainform) {
            surface.addDiagram(root, (Mainform) frame);
        } else {
            surface.addDiagram(root);
        }
    }
    // END KGU#2 2015-11-19

    // START KGU#626 2018-12-28: Enh. #657 - new group management
    /**
     * Places the passed-in root at a free space on the Arranger surface if it
     * hasn't been placed there already. Relates the given frame to it if it is
     * a Mainform instance.
     *
     * @param root - The diagram root to be added to the Arranger
     * @param frame - potentially an associable Mainform (Structorizer)
     * @param groupName - name of the group the file is to belong to
     */
    public void addToPool(Root root, JFrame frame, String groupName) {
        if (frame instanceof Mainform) {
            surface.addDiagramToGroup(root, (Mainform)frame, null, groupName);
        } else {
            surface.addDiagramToGroup(root, null, null, groupName);
        }
    }
    // END KGU#626 2018-12-28

    // START KGU#289 2016-11-15: Enh. #290 (Arrangement files oadable from Structorizer)
    /**
     * Has the file specified by arrFilename (may be an .arr or an .arrz file) loaded as
     * arrangement.
     * 
     * @param frame - potentially an associable Mainform (Structorizer)
     * @param filename - Name of the file to be loaded as arrangement
     * @return error message if something went wrong
     */
    public String loadArrangement(Mainform form, String arrFilename)
    {
    	return surface.loadArrFile(form, arrFilename);
    }
    // END KGU#259 2016-11-15

    // START KGU#155 2016-03-08: Bugfix #97 extension
    /**
     * Invalidates the cached prepareDraw info of all diagrams residing here (to
     * be called on events with heavy impact on the size or shape of some
     * Elements)
     *
     * @param _exceptDiagr the hash code of a lu.fisch.structorizer.gui.Diagram
     * that is not to be invoked (to avoid recursion)
     */
    public void resetDrawingInfo(int _exceptDiagr) {
        surface.resetDrawingInfo(_exceptDiagr);
    }
    // END KGU#155 2016-03-08

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    //@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        btnExportPNG = new javax.swing.JButton();
        btnAddDiagram = new javax.swing.JButton();
        // START KGU#85 2015-11-17
        btnRemoveDiagram = new javax.swing.JButton();
        // END KGU#85 2015-11-17
        // START KGU#88 2015-11-24
        btnPinDiagram = new javax.swing.JButton();
        // END KGU#88 2015-11-24
        // START KGU#110 2015-12-20: Enh. #62
        btnSaveArr = new javax.swing.JButton();
        btnLoadArr = new javax.swing.JButton();
        // END KGU#110 2015-12-20
        // START KGU#117 2016-03-09: Env. #77 - test coverage
        btnSetCovered = new  javax.swing.JButton();
        // END KGU#117 2016-03-09
        // START KGU#497 2018-02-17: Enh. #512 - zoom function
        btnZoom = new javax.swing.JButton();
        // END KGU#497 2018-02-17
        // START KGU#624/KGU#626 2018-12-27: Enh. #655, #657
        // END KGU#624/KGU#626 2018-12-27

        
        surface = new lu.fisch.structorizer.arranger.Surface();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Structorizer Arranger");
        // START KGU#2 2015-11-24: Replace the Java default icon
        try
        {
            // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
            //setIconImage(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/arranger48.png")).getImage());
            setIconImage(IconLoader.getIconImage(getClass().getResource("/lu/fisch/structorizer/gui/icons_48/105_arranger.png")).getImage()); // NOI18N
            // END KGU#287 2016-11-01
        }
        catch (Error error)
        {
        	logger.log(Level.WARNING, error.getMessage());
        }
        // END KGU#2 2015-11-24

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        
        //Border raisedBorder = BorderFactory.createRaisedBevelBorder();

        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnExportPNG.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/032_make_bmp.png"))); // NOI18N
        btnExportPNG.setIcon(IconLoader.getIconImage("093_picture_export.png", ICON_FACTOR)); // NOI18N
        // END KGU#287 2016-11-01
        btnExportPNG.setText("PNG Export");
        btnExportPNG.setFocusable(false);
        btnExportPNG.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExportPNG.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExportPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportPNGActionPerformed(evt);
            }
        });
        //btnExportPNG.setBorder(raisedBorder);
        toolbar.add(btnExportPNG);

        // START KGU#110 2015-12-20: Enh. #62
        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnSaveArr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/SaveFile20x20.png"))); // NOI18N
        btnSaveArr.setIcon(IconLoader.getIconImage("003_Save.png", ICON_FACTOR)); // NOI18N
        // END KGU#287 2016-11-01
        btnSaveArr.setText("Save Arr.");
        btnSaveArr.setFocusable(false);
        btnSaveArr.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSaveArr.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveArr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveArrActionPerformed(evt);
            }
        });
        //btnSaveArr.setBorder(raisedBorder);
        toolbar.add(btnSaveArr);

        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnLoadArr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/OpenFile20x20.png"))); // NOI18N
        btnLoadArr.setIcon(IconLoader.getIconImage("002_Open.png", ICON_FACTOR)); // NOI18N
        // END KGU#287 2016-11-01
        btnLoadArr.setText("Load Arr.");
        btnLoadArr.setFocusable(false);
        btnLoadArr.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLoadArr.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLoadArr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadArrActionPerformed(evt);
            }
        });
        //btnLoadArr.setBorder(raisedBorder);
        toolbar.add(btnLoadArr);
        // END KGU#110 2015-12-20

        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnAddDiagram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/101_diagram_new.png"))); // NOI18N
        btnAddDiagram.setIcon(IconLoader.getIconImage("101_diagram_new.png", ICON_FACTOR)); // NOI18N
        // END KGU#287 2016-11-01
        btnAddDiagram.setText("New Diagram");
        btnAddDiagram.setFocusable(false);
        btnAddDiagram.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddDiagram.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDiagramActionPerformed(evt);
            }
        });
        //btnAddDiagram.setBorder(raisedBorder);
        toolbar.add(btnAddDiagram);

        // START KGU#88 2015-11-24: Protect a diagram against replacement
        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnPinDiagram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/pin_blue_14x20.png"))); // NOI18N
        btnPinDiagram.setIcon(IconLoader.getIconImage("099_pin_blue.png", ICON_FACTOR)); // NOI18N
        // END KGU#287 2016-11-01
        btnPinDiagram.setText("Pin Diagram");
        btnPinDiagram.setToolTipText("Pin the selected diagrams to make them immune against replacement.");
        btnPinDiagram.setFocusable(false);
        btnPinDiagram.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPinDiagram.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPinDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPinDiagramActionPerformed(evt);
            }
        });
        //btnPinDiagram.setBorder(raisedBorder);
        toolbar.add(btnPinDiagram);
        // END KGU#88 2015-11-24

        // START KGU#117 2016-03-09: Enh. #77 - Mark a subroutine as test-covered
        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnSetCovered.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/setCovered20x20.png"))); // NOI18N
        btnSetCovered.setIcon(IconLoader.getIconImage("046_covered.png", ICON_FACTOR)); // NOI18N
        // END KGU#287 2016-11-01
        btnSetCovered.setText("Set Covered");
        btnSetCovered.setToolTipText("Mark the selected routine diagrams as test-covered for subroutine calls to them.");
        btnSetCovered.setFocusable(false);
        btnSetCovered.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSetCovered.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSetCovered.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetCoveredActionPerformed(evt);
            }
        });
        //btnSetCovered.setBorder(raisedBorder);
        toolbar.add(btnSetCovered);
        // END KGU#117 2016-03-09

        // START KGU#85 2015-11-17: New opportunity to drop the selected diagram 
        // START KGU#287 2016-11-01: Issue #81 (DPI awareness)
        //btnRemoveDiagram.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/100_diagram_drop.png"))); // NOI18N
        btnRemoveDiagram.setIcon(IconLoader.getIconImage("100_diagram_drop.png", ICON_FACTOR)); // NOI18N
        // END KGU#287 2016-11-01
        btnRemoveDiagram.setText("Drop Diagram");
        btnRemoveDiagram.setFocusable(false);
        btnRemoveDiagram.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRemoveDiagram.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRemoveDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveDiagramActionPerformed(evt, true);
            }
        });
        //btnRemoveDiagram.setBorder(raisedBorder);
        toolbar.add(btnRemoveDiagram);
        // END KGU#85 2015-11-17

        // START KGU#497 2018-02-17: Enh. #512 - zoom function
        btnZoom.setIcon(IconLoader.getIconImage("007_zoom_out.png", ICON_FACTOR)); // NOI18N
        // END KGU#287 2016-11-01
        btnZoom.setText("Zoom out/in");
        btnZoom.setFocusable(false);
        btnZoom.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoom.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnZoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomActionPerformed(evt);
            }
        });
        toolbar.add(btnZoom);
        // END KGU#497 2018-02-17

        getContentPane().add(toolbar, java.awt.BorderLayout.NORTH);

        surface.setBackground(new java.awt.Color(255, 255, 255));

//        org.jdesktop.layout.GroupLayout surfaceLayout = new org.jdesktop.layout.GroupLayout(surface);
//        surface.setLayout(surfaceLayout);
//        surfaceLayout.setHorizontalGroup(
//            surfaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//            .add(0, 420, Short.MAX_VALUE)
//        );
//        surfaceLayout.setVerticalGroup(
//            surfaceLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//            .add(0, 254, Short.MAX_VALUE)
//        );
        
        // START KGU#85 2015-11-18
        //getContentPane().add(surface, java.awt.BorderLayout.CENTER);
        scrollarea = new JScrollPane(surface);
        //scrollarea.setBackground(Color.LIGHT_GRAY);
        scrollarea.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
        scrollarea.setWheelScrollingEnabled(true);
        scrollarea.setDoubleBuffered(true);
        scrollarea.setBorder(BorderFactory.createEmptyBorder());
        //scrollarea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        //scrollarea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollarea.setViewportView(surface);
        getContentPane().add(scrollarea, java.awt.BorderLayout.CENTER);
        // END KGU#85 2015-11-18
        // START KGU#503 2018-03-13: Enh. #519 - Allow Ctrl  + mouse wheel to zoom
        scrollarea.addMouseWheelListener(surface);
        // END KGU#503 2018-03-13
        
        // START KGU#624 2018-12-21: Enh. #655 - add a statusbar for the multiple selection
        statusbar = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        //statusbar.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.LineBorder(java.awt.Color.DARK_GRAY),
        //        new javax.swing.border.EmptyBorder(0, 4, 0, 4)));
        statusSize = new javax.swing.JLabel();
        statusViewport = new javax.swing.JLabel();
        statusZoom = new javax.swing.JLabel();
        statusSelection = new javax.swing.JLabel(msgDiagramsSelected.getText().replace("%1", "0").replace("%2", "0"));
        statusSize.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
        		javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        statusViewport.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
        		javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        statusZoom.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
        		javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        statusSelection.setBorder(new javax.swing.border.CompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
                new javax.swing.border.EmptyBorder(0, 4, 0, 4)));
        // START KGU#630 2019-01-09: Enh. #622/2
        chkDrawGroups = new javax.swing.JCheckBox("Show groups");
        chkSelectGroups = new javax.swing.JCheckBox("Select groups");
        chkSelectGroups.setEnabled(false);
        ItemListener groupItemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {
                statusGroupsChanged(evt);
            }};
            
        chkDrawGroups.addItemListener(groupItemListener);
        chkSelectGroups.addItemListener(groupItemListener);
        // END KGU#630 2019-01-09
        
        statusbar.add(statusSize);
        statusbar.add(statusViewport);
        statusbar.add(statusZoom);
        statusbar.add(statusSelection);
        // START KGU#630 2019-01-09: Enh. #622/2
        // FIXME: These checkboxes in the statusbar spoil the key listener on Arranger 
        statusbar.add(chkDrawGroups);
        statusbar.add(chkSelectGroups);
        statusbar.setFocusable(false);
        // END KGU#630 2019-01-09

        getContentPane().add(statusbar, java.awt.BorderLayout.SOUTH);
        scrollarea.getViewport().addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                updateStatusSize();
            }
        });
        // END KGU#624 2018-12-21
        
        //this.addKeyListener(this);
        setKeyBindings();
        this.addWindowFocusListener(surface);

        // START KGU#49 2015-10-18: On closing the Arranger window, the dependent Mainforms must get a chance to save their stuff!
        /******************************
         * Set onClose event
         ******************************/
        addWindowListener(this); 
        // END KGU#49 2015-10-18
        
        // START KGU#624 2018-12-25: Enh. #655
        msgDiagramsSelected.addLangEventListener(this);
        // END KGU#624 2018-12-28

        this.initPopupMenu();
        
        // START KGU#117 2016-03-09: New for Enh. #77
        this.doButtons();
        // END KGU#117 2016-03-09
        pack();

    }// </editor-fold>//GEN-END:initComponents

	// START KGU#624/KGU#626 2018-12-27: Enh. #655, #657
    private void initPopupMenu() {
        popupMenu = new javax.swing.JPopupMenu();
        
        popupHitList = new javax.swing.JMenu("Hit diagrams / groups");
        popupHitList.setIcon(IconLoader.getIcon(90));
        
        popupExpandSelection = new javax.swing.JMenuItem("Expand selection", IconLoader.getIcon(79));
        popupMenu.add(popupExpandSelection);
        popupExpandSelection.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		expandRootSetOrSelection(null, Arranger.this, null);
        	}});
        popupExpandSelection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        
        // START KGU#626 2019-01-03: Enh. #657
        popupGroup = new javax.swing.JMenuItem("Group selected diagrams ...", IconLoader.getIcon(94));
        popupMenu.add(popupGroup);
        popupGroup.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		makeGroup(Arranger.this);
        	}});
        popupGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        
        popupExpandGroup = new javax.swing.JMenuItem("Expand and group ...", IconLoader.getIcon(117));
        popupMenu.add(popupExpandGroup);
        popupExpandGroup.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		expandRootSetOrSelection(null, null, null);
        		makeGroup(Arranger.this);
        	}});
        popupExpandGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        // END KGU#626 201-01-03
        
        popupAttributes = new javax.swing.JMenuItem("Inspect attributes ...", IconLoader.getIcon(86));
        popupMenu.add(popupAttributes);
        popupAttributes.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Root root = surface.getSelected1();
        		if (root != null) {
        			inspectAttributes(root);
        		}
        	}});
        popupAttributes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.ALT_DOWN_MASK));

        popupMenu.add(popupHitList);
        
        popupRemove = new javax.swing.JMenuItem("Remove selected diagrams", IconLoader.getIcon(100));
        popupMenu.add(popupRemove);
        popupRemove.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		btnRemoveDiagramActionPerformed(e, false);
        	}});
        popupRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

        popupMenu.addSeparator();

        // START KGU#630 2019-01-12: Enh. #662/3
        popupRearrange = new javax.swing.JMenuItem("Rearrange by groups", IconLoader.getIcon(119));
        popupMenu.add(popupRearrange);
        popupRearrange.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		rearrange();
        	}
        });

        popupMenu.addSeparator();
        // END KGU#630 2019-01-12

        popupRemoveAll = new javax.swing.JMenuItem("Remove all diagrams", IconLoader.getIcon(45));
        popupMenu.add(popupRemoveAll);
        popupRemoveAll.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		removeAllDiagrams(null);
        	}});
        popupRemoveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        
        popupMenu.addSeparator();
        
        popupHelp = new javax.swing.JMenuItem("Arranger help page ...", IconLoader.getIcon(110));
        popupMenu.add(popupHelp);
        popupHelp.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		helpArranger(false);
        	}});
        popupHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        
        popupKeyBindings = new javax.swing.JMenuItem("Show key bindings ...", IconLoader.getIcon(89));
        popupMenu.add(popupKeyBindings);
        popupKeyBindings.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		helpArranger(true);
        	}});
        popupKeyBindings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, java.awt.event.InputEvent.ALT_DOWN_MASK));
        
    }
    // END KGU#624/KGU#626 2018-12-27

	// START KGU#624 2018-12-21: Enh. #655 - new status bar
	protected void updateStatusSize() {
		scrollarea.getLocation(scrollareaOrigin);
		java.awt.Rectangle vRect = scrollarea.getViewport().getViewRect();
		/* To compute the unzoomed sizes from the vRect looks bad due to rounding jitter.
		 * Alternatively we would have to ask for the diagram bounds but these don't
		 * reflect a larger window.
		 */
		statusSize.setText(surface.getWidth() + " x " + surface.getHeight());
		statusViewport.setText(vRect.x + ".." + (vRect.x + vRect.width) + " : " +
				vRect.y + ".." + (vRect.y + vRect.height));
		statusZoom.setText(String.format("%.1f %%", 100 / surface.getZoom()));
	}
	
	protected void updateStatusSelection() {
		Set<Root> sel = surface.getSelected();
		Root sel1 = surface.getSelected1();
		String selText = Integer.toString(sel.size());
		if (sel1 != null) {
			selText = sel1.getSignatureString(false);
		}
		statusSelection.setText(msgDiagramsSelected.getText()
				.replace("%1", Integer.toString(surface.getDiagramCount()))
				.replace("%2", selText));
	}
	// END KGU#624 2018-12-21

	private void btnExportPNGActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnExportPNGActionPerformed
	{//GEN-HEADEREND:event_btnExportPNGActionPerformed
		// START KGU#624 2018-12-23: Enh. #655 On multiple selection better warn
		StringList rootList = surface.listSelectedRoots(false, false);
		int nSelected = rootList.count();
		if (nSelected > 1 && nSelected < surface.getDiagramCount()) {
			if (nSelected > ROOT_LIST_LIMIT) {
				rootList.remove(ROOT_LIST_LIMIT, nSelected);
				rootList.add("...");
			}
			if (JOptionPane.showConfirmDialog(this, 
					msgConfirmMultiple.getText()
					.replace("%1", Integer.toString(nSelected))
					.replace("%2", Integer.toString(surface.getDiagramCount()))
					.replace("%3", rootList.concatenate("\n- "))
					.replace("%4", msgReadyToExport.getText()),
					msgTitleWarning.getText(),
					JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
				return;
			}
		}
		// END KGU#624 2018-12-23
		surface.exportPNG(this);
	}//GEN-LAST:event_btnExportPNGActionPerformed

    private void btnAddDiagramActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnAddDiagramActionPerformed
    {//GEN-HEADEREND:event_btnAddDiagramActionPerformed
        surface.addDiagram(new Root());
    }//GEN-LAST:event_btnAddDiagramActionPerformed

	// START KGU#85 2015-11-17
	private void btnRemoveDiagramActionPerformed(java.awt.event.ActionEvent evt, boolean checkShiftPressed) {
		// START KGU#534 2018-06-27: Enh.#552    	
		//surface.removeDiagram();
		if (checkShiftPressed && this.isShiftPressed) {
			removeAllDiagrams(this);
			// We must make sure that the shift key status is reset - this doesn't work automatically!
			// Seems that the keyReleased event gets lost...
			this.setShiftPressed(false);
		}
		else {
			// START KGU#624 2018-12-23: Enh. #655 On multiple selection better warn
			StringList rootList = surface.listSelectedRoots(false, false);
			int nSelected = rootList.count();
			if (rootList.count() > 1) {
				if (nSelected > ROOT_LIST_LIMIT) {
					rootList.remove(ROOT_LIST_LIMIT, nSelected);
					rootList.add("...");
				}
				if (JOptionPane.showConfirmDialog(this, 
						msgConfirmMultiple.getText()
						.replace("%1", Integer.toString(nSelected))
						.replace("%2", Integer.toString(surface.getDiagramCount()))
						.replace("%3", rootList.concatenate("\n- "))
						.replace("%4", msgConfirmRemove.getText().replace("%1",msgActionDelete.getText())), 
						msgTitleWarning.getText(),
						JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
					return;
				}
			}
			// END KGU#624 2018-12-23
			surface.removeDiagram();
		}
		// END KGU#534 2018-06-27
	}
	// END KGU#85 2015-11-17

    // START KGU#88 2015-11-24
    private void btnPinDiagramActionPerformed(java.awt.event.ActionEvent evt) {
        surface.togglePinned();
    }
    // END KGU#88 2015-11-24

    // START KGU#110 2015-12-20: Enh. #62 Possibility to save and load arrangements
    private void btnSaveArrActionPerformed(java.awt.event.ActionEvent evt) {
        surface.saveArrangement(this, null, false);
    }

    private void btnLoadArrActionPerformed(java.awt.event.ActionEvent evt) {
        surface.loadArrangement(this);
    }
    // END KGU#110 2015-12-20

    // START KGU#117 2016-03-09: Enh. #77
    private void btnSetCoveredActionPerformed(java.awt.event.ActionEvent evt) {
        surface.setCovered(this);
    }
    // END KGU#88 2016-03-09

    // START KGU#497 2018-02-17: Enh. #513
    protected void btnZoomActionPerformed(ActionEvent evt) {
        surface.zoom((evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0);
        if (surface.getZoom() <= 1 && this.isShiftPressed) {
            btnZoom.setEnabled(false);
        }
    }
    // END KGU#497 2018-02-17

	// START KGU#630 2019-01-12: Enh. #662/3
    /**
     * This is the btnPopupRearrangePerformed action
     * Rearranges all diagrams by groups
     */
    public void rearrange() {
    	surface.rearrange();
    }
    // END KGU#630 2019-01-12

    // START KGU#630 2019-01-09: Enh. #622/2
    protected void statusGroupsChanged(ItemEvent evt) {
        if (evt.getSource() == chkDrawGroups) {
            boolean drawingEnabled = chkDrawGroups.isSelected();
            surface.enableGroupDrawing(drawingEnabled);
            chkSelectGroups.setEnabled(drawingEnabled);
            if (!drawingEnabled) {
                surface.enableGroupSelection(false);
            }
            else {
                surface.enableGroupSelection(chkSelectGroups.isSelected());
            }
        }
        else if (evt.getSource() == chkSelectGroups) {
            surface.enableGroupSelection(chkSelectGroups.isSelected());
        }
    }
    // END KGU#630 2019-01-09

    /**
     * Starts the Arranger as application
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // START KGU#521 2018-06-12: Workaround for #536 (corrupted rendering on certain machines) 
        System.setProperty("sun.java2d.noddraw", "true");
        // END KGU#521 2018-06-12
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // START KGU#2 2015-11-19: Converted into a singleton
                //new Arranger().setVisible(true);
                getInstance(true).setVisible(true);
                // END KGU#2 2015-11-19
            }
        });
    }

    // Variables declaration
    private javax.swing.JButton btnAddDiagram;
    // START KGU#85 2015-11-17
    private javax.swing.JButton btnRemoveDiagram;
    // END KGU#85 2015-11-17
    // START KGU#88 2015-11-24
    private javax.swing.JButton btnPinDiagram;
    // END KGU#88 2015-11-24
    private javax.swing.JButton btnExportPNG;
    // START KGU#110 2015-12-20: Enh. #62 a possibility to save and load arrangements was requested
    private javax.swing.JButton btnSaveArr;
    private javax.swing.JButton btnLoadArr;
    // END KGU#110 2015-12-20
    // START KGU#117 2016-03-09: Env. #77 - test coverage
    private javax.swing.JButton btnSetCovered;
    // END KGU#117 2016-03-09
    // START KGU#497 2018-02-17: Enh. #512 - zoomm function
    private javax.swing.JButton btnZoom;
    /** Registers whether shift key had been pressed without having been released again */
    private boolean isShiftPressed = false;
    /** Defines a magnification factor for the toolbar button icons w.r.t. the default size 16x16 px */
    private static final double ICON_FACTOR = 1.5;
    // END KGU#497 2018-02-17
    
    private lu.fisch.structorizer.arranger.Surface surface;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
    // START KGU#624 2018-12-21: Enh. #655
    private javax.swing.JPanel statusbar;
    protected javax.swing.JLabel statusSize;
    protected javax.swing.JLabel statusViewport;
    protected javax.swing.JLabel statusZoom;
    protected javax.swing.JLabel statusSelection;
    protected java.awt.Point scrollareaOrigin = new java.awt.Point();
    // END KGU#624 2018-12-21
    // START KGU#630 2019-01-09: Enh. #662/2
    protected javax.swing.JCheckBox chkDrawGroups;
    protected javax.swing.JCheckBox chkSelectGroups;
    // END KGU#630 2019-01-09
    // START KGU#85 2015-11-18
    private JScrollPane scrollarea;
    // END KGU#85 2015-11-18
    // START KGU#624/KGU#626 2018-12-27: Enh. #655, #657
    protected static javax.swing.JPopupMenu popupMenu = null;
    protected static javax.swing.JMenu popupHitList = null;
    private javax.swing.JMenuItem popupRemove = null;
    private javax.swing.JMenuItem popupRemoveAll = null;
    private javax.swing.JMenuItem popupExpandSelection = null;
    private javax.swing.JMenuItem popupAttributes = null;
    private javax.swing.JMenuItem popupHelp = null;
    private javax.swing.JMenuItem popupKeyBindings = null;
    // END KGU#624 2018-12-27
    // START KGU#626 2019-01-03: Enh. #657
    private javax.swing.JMenuItem popupGroup = null;
    private javax.swing.JMenuItem popupExpandGroup = null;
    // END KGU#626 2019-01-03
    // START KGU#630 2019-01-12: Enh. #622/3
    private javax.swing.JMenuItem popupRearrange = null;
    // END KGU#630 2019-01-12
    // START KGU#305 2016-12-16
    private static final Set<IRoutinePoolListener> listeners = new HashSet<IRoutinePoolListener>();
    private static final Vector<Root> routines = new Vector<Root>();
    // END KGU#305 2016-12-16
    // START KGU#626 2018-12-31: Enh. #657
    private static final Vector<Group> groups = new Vector<Group>();
    // END KGU#626 2018-12-31

    // START KGU#631 2019-01-08: We need a handy way to decide whther he application is closing
    /**
     * Checks whether this Mainform represents the main class (and thread) of the application
     * i.e. if it was started as a stand-alone object.<br/>
     * Relevant for the {@link WindowListener#windowClosing()} event.
     * @return true if this object represents the running application.
     */
    public boolean isApplicationMain()
    {
        return isStandalone;
    }
    // END KGU#631 2019-01-08

    @Override
    public void windowOpened(WindowEvent e) {
    }

    // START KGU#631 2019-01-08: Bugfix #664
    @Override
    public void windowClosing(WindowEvent e)
    {
        windowClosingVetoable(e);
    }
    
    /**
     * Like {@link WindowListener#windowClosing(WindowEvent)} but may return
     * whether the listener had vetoed.
     * @param e - the current {@link WindowEvent}
     * @return true if confirmed, false if vetoed
     */
    public boolean windowClosingVetoable(WindowEvent e) {
    // END KGU#631 2019-01-08
        // START KGU#49 2017-01-04: On closing the Arranger window, the dependent Mainforms must get a chance to save their stuff!
        // START KGU#631 2019-01-08: Decide more precisely if the application is going down
        Object source = e.getSource();
        boolean applicationClosing = (!(source instanceof LangFrame) || ((LangFrame)source).isApplicationMain());
        // END KGU#631 2019-01-08
        // START KGU#626 2019-01-05: Enh. #657
        //if (surface.saveDiagrams(this, null, true, false))
        Component initiator = e.getComponent();
        if (initiator == null) {
            initiator = this;
        }
        if (!applicationClosing || this.saveAll(initiator, applicationClosing))
        // END KGU#626 2019-01-05
        {
            if (isStandalone)
            {
                System.exit(0);
            }
            else
            {
                /* While the singleton reference is still held, a dispose() call won't have any garbage
                 * collection effect, but if there are no diagrams then we may actually induce disposal. */
                if (surface.getDiagramCount() == 0) {
                    mySelf = null;
                }
                dispose();
            }
            // START KGU#631 2019-01-08: Bugfix #664
            return true;
            // END KGU#631 2019-01-08
        }
        // END KGU#49 2017-01-04
        // START KGU#631 2019-01-08: Bugfix #664 - allow a veto
        return false;
        // END KGU#631 2019-01-08
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
        surface.repaint();
    }

    public void windowDeactivated(WindowEvent e) {
    }

	// START KGU#85/KGU#177/KGU#497/KGU#534/KGU#624/KGU#626 2019-01-16: Issues #35, #158, #512, #552, #655, #657
	/**
	 * This class had to replace the KeyListener method {@link Arranger#keyPressed(KeyEvent)} since the two JCheckBoxes
	 * on the {@link Arranger#statusbar} had been introduced and spoiled the key notification mechanism such that we
	 * were forced to use Keybinding rather than the leaner key listening.
	 */
	private class KeyAction extends AbstractAction {

		private boolean shiftDown = false, ctrlDown = false, altDown = false;
		public KeyAction(String actionName)
		{
			putValue(ACTION_COMMAND_KEY, actionName);
		}
		public KeyAction(String actionName, boolean isShiftDown, boolean isCtrlDown, boolean isAltDown)
		{
			putValue(ACTION_COMMAND_KEY, actionName);
			shiftDown = isShiftDown; ctrlDown = isCtrlDown; altDown = isAltDown;
		}
		@Override
		public void actionPerformed(ActionEvent actionEvt) {
			String command = actionEvt.getActionCommand();
			if (command.equals(KEY_DELETE)) {
				StringList rootList = surface.listSelectedRoots(false, false);
				int nSelected = rootList.count();
				if (nSelected == 0) {
					return;
				}
				String verb = msgActionDelete.getText();
				if (nSelected > ROOT_LIST_LIMIT) {
					// Avoid to make the option pane so large that the buttons can't be reached
					rootList.remove(ROOT_LIST_LIMIT, nSelected);
					rootList.add("...");
				}
				String message = msgConfirmMultiple.getText().
						replace("%1", Integer.toString(nSelected)).
						replace("%2", Integer.toString(surface.getDiagramCount())).
						replace("%3", rootList.concatenate("\n- ")).
						replace("%4", msgConfirmRemove.getText().replace("%1", verb));
				if (this.ctrlDown || nSelected == 1 || JOptionPane.showConfirmDialog(Arranger.this, message, msgTitleWarning.getText(),
						JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
					surface.removeDiagram();
				}
			}
			else if (command.equals(KEY_CUT)) {
				StringList rootList = surface.listSelectedRoots(false, false);
				if (checkIllegalMultipleAction(rootList, msgActionCut.getText())) {
					return;
				}
				if (surface.copyDiagram())	// cut means copy first
				{
					surface.removeDiagram();
				}
			}
			else if (command.equals(KEY_PASTE)) {
				surface.pasteDiagram();
			}
			else if (command.equals(KEY_COPY)) {
				StringList rootList = surface.listSelectedRoots(false, false);
				if (checkIllegalMultipleAction(rootList, msgActionCopy.getText())) {
					return;
				}
				surface.copyDiagram();
			}
			else if (command.equals(KEY_SHIFT)) {
				/* The former mechanism to switch toolbar buttons while shift is being held down doesn't work
				 * with keybindings. So we had to use another key (now F2) to toggle the button functins */
				setShiftPressed(!isShiftPressed);
			}
			else if (command.equals(KEY_ZOOM_OUT)) {
				surface.zoom(shiftDown);
			}
			else if (command.equals(KEY_PAGE_DOWN)) {
				int dir = altDown ? -1 : 1;
				if (shiftDown) {
					scrollarea.getHorizontalScrollBar().setValue(
							scrollarea.getHorizontalScrollBar().getValue() + 
							dir * scrollarea.getHorizontalScrollBar().getBlockIncrement(dir));
				}
				else {
					scrollarea.getVerticalScrollBar().setValue(
							scrollarea.getVerticalScrollBar().getValue() + 
							dir * scrollarea.getVerticalScrollBar().getBlockIncrement(dir));
				}
			}
			else if (command.equals(KEY_ALL)) {
				surface.selectAll();
			}
			else if (command.equals(KEY_SAVE)) {
				surface.saveArrangement(Arranger.this, null, false);
			}
			else if (command.equals(KEY_OPEN)) {
				surface.loadArrangement(Arranger.this);
			}
			else if (command.equals(KEY_DOWN) || command.equals(KEY_UP)) {
				int direction = command.equals(KEY_UP) ? -1 : 1;
				int vUnits = scrollarea.getVerticalScrollBar().getUnitIncrement(direction) * direction;
				if (this.shiftDown) {
					vUnits *= 10;
				}
				int newValue = Math.max(scrollarea.getVerticalScrollBar().getValue() + vUnits, 0);
				if (this.ctrlDown) {
					surface.moveSelection(0, vUnits);
					surface.scrollToSelection();
					routinePoolChanged(Arranger.this, IRoutinePoolListener.RPC_POSITIONS_CHANGED);
				}
				else {
					scrollarea.getVerticalScrollBar().setValue(newValue);                		
				}
			}
			else if (command.equals(KEY_LEFT) || command.equals(KEY_RIGHT)) {
				int direction = command.equals(KEY_LEFT) ? -1 : 1;
				int hUnits = scrollarea.getHorizontalScrollBar().getUnitIncrement(direction) * direction;
				if (this.shiftDown) {
					hUnits *= 10;
				}
				int newValue = Math.max(scrollarea.getHorizontalScrollBar().getValue() + hUnits, 0);
				if (this.ctrlDown) {
					surface.moveSelection(hUnits, 0);
					surface.scrollToSelection();
					routinePoolChanged(Arranger.this, IRoutinePoolListener.RPC_POSITIONS_CHANGED);
				}
				else {
					scrollarea.getHorizontalScrollBar().setValue(newValue);
				}
			}
			else if (command.equals(KEY_HELP)) {
				helpArranger(false);
			}
			else if (command.equals(KEY_KEYS)) {
				helpArranger(true);
			}
			else if (command.equals(KEY_HOME_H)) {
				scrollarea.getHorizontalScrollBar().setValue(0);
			}
			else if (command.equals(KEY_END_H)) {
				scrollarea.getHorizontalScrollBar().setValue(surface.getWidth());
			}
			else if (command.equals(KEY_HOME_V)) {
				scrollarea.getVerticalScrollBar().setValue(0);
			}
			else if (command.equals(KEY_END_V)) {
				scrollarea.getVerticalScrollBar().setValue(surface.getHeight());
			}
			else if (command.equals(KEY_ALT_ENTER)) {
				Root selected = surface.getSelected1();
				if (selected != null) {
					inspectAttributes(selected);
				}
			}
			else if (command.equals(KEY_MAKE_GROUP)) {
				makeGroup(Arranger.this);
			}
			else if (command.equals(KEY_EXPAND_GROUP)) {
				expandRootSetOrSelection(null, Arranger.this, null);
			}
		}
	}
	
	private void setKeyBindings()
	{
		ActionMap actionMap = surface.getActionMap();
		InputMap inputMap = surface.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KEY_DELETE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK), KEY_CTRL_DELETE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK), KEY_CUT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), KEY_CUT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), KEY_PASTE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK), KEY_PASTE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK), KEY_COPY);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), KEY_COPY);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), KEY_SHIFT);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KEY_LEFT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KEY_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK), KEY_LEFT10);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK), KEY_RIGHT10);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), MOVE_LEFT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), MOVE_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), MOVE_LEFT10);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), MOVE_RIGHT10);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KEY_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KEY_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), KEY_UP10);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), KEY_DOWN10);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), MOVE_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), MOVE_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), MOVE_UP10);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), MOVE_DOWN10);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), KEY_ZOOM_IN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), KEY_ZOOM_OUT);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), KEY_PAGE_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), KEY_PAGE_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_DOWN_MASK), KEY_PAGE_LEFT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_DOWN_MASK), KEY_PAGE_RIGHT);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), KEY_HOME_H);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), KEY_END_H);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_DOWN_MASK), KEY_HOME_V);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK), KEY_END_V);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK), KEY_ALL);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), KEY_OPEN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), KEY_SAVE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), KEY_HELP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.ALT_DOWN_MASK), KEY_KEYS);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK), KEY_MAKE_GROUP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), KEY_EXPAND_GROUP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), KEY_EXPAND_GROUP);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), KEY_ALT_ENTER);

		actionMap.put(KEY_DELETE, new KeyAction(KEY_DELETE));
		actionMap.put(KEY_CTRL_DELETE, new KeyAction(KEY_DELETE, false, true, false));
		actionMap.put(KEY_CUT, new KeyAction(KEY_CUT));
		actionMap.put(KEY_COPY, new KeyAction(KEY_COPY));
		actionMap.put(KEY_PASTE, new KeyAction(KEY_PASTE));

		actionMap.put(KEY_SHIFT, new KeyAction(KEY_SHIFT));

		actionMap.put(KEY_ZOOM_IN, new KeyAction(KEY_ZOOM_OUT, true, false, false));
		actionMap.put(KEY_ZOOM_OUT, new KeyAction(KEY_ZOOM_OUT));

		actionMap.put(KEY_LEFT, new KeyAction(KEY_LEFT));
		actionMap.put(KEY_LEFT10, new KeyAction(KEY_LEFT, true, false, false));
		actionMap.put(KEY_RIGHT, new KeyAction(KEY_RIGHT));
		actionMap.put(KEY_RIGHT10, new KeyAction(KEY_RIGHT, true, false, false));
		actionMap.put(MOVE_LEFT, new KeyAction(KEY_LEFT, false, true, false));
		actionMap.put(MOVE_LEFT10, new KeyAction(KEY_LEFT, true, true, false));
		actionMap.put(MOVE_RIGHT, new KeyAction(KEY_RIGHT, false, true, false));
		actionMap.put(MOVE_RIGHT10, new KeyAction(KEY_RIGHT, true, true, false));

		actionMap.put(KEY_UP, new KeyAction(KEY_UP));
		actionMap.put(KEY_UP10, new KeyAction(KEY_UP, true, false, false));
		actionMap.put(KEY_DOWN, new KeyAction(KEY_DOWN));
		actionMap.put(KEY_DOWN10, new KeyAction(KEY_DOWN, true, false, false));
		actionMap.put(MOVE_UP, new KeyAction(KEY_UP, false, true, false));
		actionMap.put(MOVE_UP10, new KeyAction(KEY_UP, true, true, false));
		actionMap.put(MOVE_DOWN, new KeyAction(KEY_DOWN, false, true, false));
		actionMap.put(MOVE_DOWN10, new KeyAction(KEY_DOWN, true, true, false));

		actionMap.put(KEY_PAGE_DOWN, new KeyAction(KEY_PAGE_DOWN));
		actionMap.put(KEY_PAGE_UP, new KeyAction(KEY_PAGE_DOWN, false, false, true));
		actionMap.put(KEY_PAGE_RIGHT, new KeyAction(KEY_PAGE_DOWN, true, false, false));
		actionMap.put(KEY_PAGE_LEFT, new KeyAction(KEY_PAGE_DOWN, true, false, true));

		actionMap.put(KEY_HOME_H, new KeyAction(KEY_HOME_H));
		actionMap.put(KEY_END_H, new KeyAction(KEY_END_H));
		actionMap.put(KEY_HOME_V, new KeyAction(KEY_HOME_V));
		actionMap.put(KEY_END_V, new KeyAction(KEY_END_V));
		
		actionMap.put(KEY_ALL, new KeyAction(KEY_ALL));
		
		actionMap.put(KEY_OPEN, new KeyAction(KEY_OPEN));
		actionMap.put(KEY_SAVE, new KeyAction(KEY_SAVE));

		actionMap.put(KEY_HELP, new KeyAction(KEY_HELP));
		actionMap.put(KEY_KEYS, new KeyAction(KEY_KEYS));
		
		actionMap.put(KEY_MAKE_GROUP, new KeyAction(KEY_MAKE_GROUP));
		actionMap.put(KEY_EXPAND_GROUP, new KeyAction(KEY_EXPAND_GROUP));

		actionMap.put(KEY_ALT_ENTER, new KeyAction(KEY_ALT_ENTER));
	}

    // START KGU#85 2015-11-30: Enh. #35 - For convenience, the delete button may also be used to drop a diagram now
//    @Override
//    public void keyPressed(KeyEvent ev) {
//        if (ev.getSource() == this) {
//            switch (ev.getKeyCode()) {
//                case KeyEvent.VK_DELETE:
//                    // START KGU#534 2018-06-27: Enh. #552
//                    // START KGU#624 2018-12-21: Enh. #655 recent ky binding wasn't intuitive
//                    //surface.removeDiagram();
//                    //if (ev.isShiftDown()) {
//                    //    surface.removeDiagram();
//                    //} else if (ev.isControlDown()) {
//                    //    surface.removeAllDiagrams();
//                    //}
//                {
//                    // START KGU624 2018-12-21: Enh. #655 - Face multiple selection
//                    //Root sel = surface.getSelected1();
//                    StringList rootList = surface.listSelectedRoots(false, false);
//                    int nSelected = rootList.count();
//                    if (nSelected == 0) {
//                        break;
//                    }
//                    boolean shift = ev.isShiftDown();
//                    String verb = "";
//                    if (shift) {
//                        verb = msgActionCut.getText();
//                    }
//                    else {
//                        verb = msgActionDelete.getText();
//                    }
//                    if (nSelected > ROOT_LIST_LIMIT) {
//                        // Avoid to make the option pane so large that the buttons can't be reached
//                        rootList.remove(ROOT_LIST_LIMIT, nSelected);
//                        rootList.add("...");
//                    }
//                    if (shift && checkIllegalMultipleAction(rootList, verb)) {
//                        break;
//                    }
//                    String message = msgConfirmMultiple.getText().
//                            replace("%1", Integer.toString(nSelected)).
//                            replace("%2", Integer.toString(surface.getDiagramCount())).
//                            replace("%3", rootList.concatenate("\n- ")).
//                            replace("%4", msgConfirmRemove.getText().replace("%1", verb));
//                    if (ev.isControlDown() || nSelected == 1 || JOptionPane.showConfirmDialog(this, message, msgTitleWarning.getText(),
//                            JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
//                        if (shift) {
//                            surface.copyDiagram();
//                        }
//                        surface.removeDiagram();
//                    }
//                }
//                    // END KGU#624 2018-12-21
//                    // END KGU#534 2018-06-27
//                    break;
//                // START KGU#177 2016-04-14: Enh. #158 - support the insertion from clipboard
//                case KeyEvent.VK_X:
//                    if (ev.isControlDown()) {
//                        // START KGU#624 2018-12-21: Enh. #655 - face multiple selection
//                        //surface.removeDiagram();
//                        StringList rootList = surface.listSelectedRoots(false, false);
//                        if (checkIllegalMultipleAction(rootList, msgActionCut.getText())) {
//                            break;
//                        }
//                        if (surface.copyDiagram())	// cut means copy first
//                        {
//                            surface.removeDiagram();
//                        }
//                        // END KGU#624 2018-12-21
//                    }
//                    break;
//                case KeyEvent.VK_V:
//                    if (ev.isControlDown()) {
//                        surface.pasteDiagram();
//                    }
//                    break;
//                case KeyEvent.VK_INSERT:
//                    if (ev.isShiftDown()) {
//                        surface.pasteDiagram();
//                    } else if (ev.isControlDown()) {
//                        surface.copyDiagram();
//                    }
//                    break;
//                case KeyEvent.VK_C:
//                    if (ev.isControlDown()) {
//                        // START KGU#624 2018-12-21: Enh. #655 - face multiple selection
//                        StringList rootList = surface.listSelectedRoots(false, false);
//                        if (checkIllegalMultipleAction(rootList, msgActionCopy.getText())) {
//                            break;
//                        }
//                        // END KGU#624 2018-12-21
//                        surface.copyDiagram();
//                    }
//                    break;
//                // END KGU#177 2016-04-14
//                // START KGU#497 2018-02-17: Enh. #512 - zooming introduced
//                case KeyEvent.VK_SHIFT:
//                    if (!this.isShiftPressed) {
//                        setShiftPressed(true);
//                    }
//                    break;
//                case KeyEvent.VK_ADD:
//                    surface.zoom(true);
//                    break;
//                case KeyEvent.VK_SUBTRACT:
//                    surface.zoom(false);
//                    break;
//                // END KGU#497 2018-02-17
//                // START KGU#624 2018-12-21: Enh. #655
//                case KeyEvent.VK_PAGE_UP:
//                    if (ev.isShiftDown()) {
//                        scrollarea.getHorizontalScrollBar().setValue(
//                                scrollarea.getHorizontalScrollBar().getValue() - 
//                                scrollarea.getHorizontalScrollBar().getBlockIncrement(-1));
//                    }
//                    else {
//                        scrollarea.getVerticalScrollBar().setValue(
//                                scrollarea.getVerticalScrollBar().getValue() - 
//                                scrollarea.getVerticalScrollBar().getBlockIncrement(-1));
//                    }
//                    break;
//                case KeyEvent.VK_PAGE_DOWN:
//                    if (ev.isShiftDown()) {
//                        scrollarea.getHorizontalScrollBar().setValue(
//                                scrollarea.getHorizontalScrollBar().getValue() + 
//                                scrollarea.getHorizontalScrollBar().getBlockIncrement(1));
//                    }
//                    else {
//                        scrollarea.getVerticalScrollBar().setValue(
//                                scrollarea.getVerticalScrollBar().getValue() + 
//                                scrollarea.getVerticalScrollBar().getBlockIncrement(1));
//                    }
//                    break;
//                case KeyEvent.VK_A:
//                    if (ev.isControlDown()) {
//                        surface.selectAll();
//                    }
//                    break;
//                case KeyEvent.VK_S:
//                    if (ev.isControlDown()) {
//                        surface.saveArrangement(this, null, false);
//                    }
//                    break;
//                case KeyEvent.VK_O:
//                    if (ev.isControlDown()) {
//                        surface.loadArrangement(this);
//                    }
//                // END KGU#624 2018-12-21
//                    // START KGU#624 2018-12-24: Enh. #655
//                case KeyEvent.VK_UP:
//                case KeyEvent.VK_DOWN:
//                {
//                    int direction = ev.getKeyCode() == KeyEvent.VK_UP ? -1 : 1;
//                    int vUnits = scrollarea.getVerticalScrollBar().getUnitIncrement(direction) * direction;
//                    if (ev.isShiftDown()) {
//                        vUnits *= 10;
//                    }
//                    int newValue = Math.max(scrollarea.getVerticalScrollBar().getValue() + vUnits, 0);
//                    if (ev.isControlDown()) {
//                        surface.moveSelection(0, vUnits);
//                        surface.scrollToSelection();
//                        this.routinePoolChanged(this, IRoutinePoolListener.RPC_POSITIONS_CHANGED);
//                    }
//                    else {
//                        scrollarea.getVerticalScrollBar().setValue(newValue);                		
//                    }
//                }
//                break;
//                case KeyEvent.VK_LEFT:
//                case KeyEvent.VK_RIGHT:
//                {
//                    int direction = ev.getKeyCode() == KeyEvent.VK_LEFT ? -1 : 1;
//                    int hUnits = scrollarea.getHorizontalScrollBar().getUnitIncrement(direction) * direction;
//                    if (ev.isShiftDown()) {
//                        hUnits *= 10;
//                    }
//                    int newValue = Math.max(scrollarea.getHorizontalScrollBar().getValue() + hUnits, 0);
//                    if (ev.isControlDown()) {
//                        surface.moveSelection(hUnits, 0);
//                        surface.scrollToSelection();
//                        this.routinePoolChanged(this, IRoutinePoolListener.RPC_POSITIONS_CHANGED);
//                    }
//                    else {
//                        scrollarea.getHorizontalScrollBar().setValue(newValue);
//                    }
//                }
//                break;
//                case KeyEvent.VK_F1:
//                    this.helpArranger(ev.isAltDown());
//                    break;
//                    // END KGU#624 2018-12-24
//                    // START KGU#624 2018-12-26: Enh. #655
//                case KeyEvent.VK_HOME:
//                case KeyEvent.VK_END:
//                {
//                    javax.swing.JScrollBar scrollbar = scrollarea.getHorizontalScrollBar();
//                    int maxValue = surface.getWidth();
//                    if (ev.isControlDown()) {
//                        scrollbar = scrollarea.getVerticalScrollBar();
//                        maxValue = surface.getHeight();
//                    }
//                    scrollbar.setValue(ev.getKeyCode() == KeyEvent.VK_HOME ? 0 : maxValue);
//                }
//                break;
//                case KeyEvent.VK_F11:
//                {
//                    expandRootSetOrSelection(null, this, null);
//                }
//                    break;
//                case KeyEvent.VK_ENTER:
//                    if (ev.isAltDown()) {
//                        Root selected = surface.getSelected1();
//                        if (selected != null) {
//                            this.inspectAttributes(selected);
//                        }
//                    }
//                    break;
//                    // END KGU#624 2018-12-26
//                    // START KGU#626 2019-01-02: Enh. #657
//                case KeyEvent.VK_G:
//                    if (ev.isShiftDown()) {
//                        this.expandRootSetOrSelection(null, null, null);
//                    }
//                    makeGroup(this);
//                    break;
//                    // END KGU#626 2019-01-02
//            }
//        }
//    }
//
//  @Override
//  public void keyReleased(KeyEvent ev) {
//      // START KGU#497 2018-02-17: Change the zoom icon when shift is released
//      if (ev.getSource() == this) {
//          switch (ev.getKeyCode()) {
//              case KeyEvent.VK_SHIFT:
//                  setShiftPressed(false);
//                  break;
//          }
//      }
//      // END KGU#497 2018-02-17
//  }
//
//  @Override
//  public void keyTyped(KeyEvent ev) {
//      // Nothing to do here
//  }
//  // END KGU#2 2015-11-30

	// START KGU#626 2019-01-02: Enh. #657
    /**
     * Makes a new group from currently selected diagrams.
     * @param originator - some initiating component, used e.g. for message box positioning
     * @return true if the method was successful.
     * @see #makeGroup(Collection, Component, boolean)
     */
	public boolean makeGroup(Component originator) {
		boolean done = false;
		if (surface.getSelectionCount() == 0) {
			JOptionPane.showMessageDialog(originator, msgEmptySelection.getText().replace("%", msgActionGroup.getText()));
		}
		else {
			done = makeGroup(null, originator, false);
		}
		return done;
	}
	// END KGU#626 2019-01-02

	/**
	 * Makes a new {@link Group} for the given {@link Root}s. The name will be requested
	 * interactively.
	 * @param roots - the {@link Root} objects the associated diagrams of which ought to
	 * join the new group, if null then the currently selected diagrams are used.
	 * @param originator - some initiating component, used e.g. for message box positioning
	 * @param accomplishSet - whether all reachable diagrams from which the given or selected
	 * {@code roots} depend are to join the group. 
	 * @return true if the method was successful
	 * @see #makeGroup(Component)
	 */
	public boolean makeGroup(Collection<Root> roots, Component originator, boolean accomplishSet) {
		boolean done = false;
		int nSelected = 0;
		// This collection is to gather the groups coinciding with the (expanded) set of Roots.
		Collection<Group> congrGroups = null;
		if (roots != null) {
			if (accomplishSet) {
				roots = this.accomplishRootSet(new HashSet<Root>(roots), originator, null);
			}
			congrGroups = surface.getGroupsFromRoots(roots, true);
			nSelected = roots.size();
		}
		else {
			// Start from current selection in Arranger
			roots = surface.getSelected();
			nSelected = surface.getSelectionCount();
			congrGroups = surface.getGroupsFromSelection(true);
		}
		if (!congrGroups.isEmpty()) {
			StringList groupNames = new StringList();
			for (Group grp: congrGroups) {
				// Remark: the default group may not occur here, so we don't have to replace its default name
				groupNames.add(grp.getName());
			}
			if (JOptionPane.showConfirmDialog(originator,
					msgCongruentGroups.getText()
					.replace("%1", Integer.toString(nSelected))
					.replace("%2", Integer.toString(groupNames.count()))
					.replace("%3", groupNames.concatenate("\n- "))
					.replace("%4", msgDoCreateGroup.getText()),
					msgTitleWarning.getText(),
					JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
				return false;
			}
		}

		if (!roots.isEmpty()) {
			String groupName = this.requestNewGroupName(originator, nSelected);
			if (groupName != null) { // not cancelled
				// the prefix character of groupName signals whether to to override an existing group  or not  
				done = (surface.makeGroup(groupName.substring(1), null, roots, groupName.startsWith("!"), null) != null);
			}
		}
		return done;
	}

	/**
	 * Request the user to enter a name for the new group. Checks against name conflicts and
	 * the like.
	 * @param originator - a component from which the request was initiated
	 * @param nDiagrams - number of diagrams to form a group of
	 * @return null if the user cancelled, a prefixed group name otherwise where prefix "!" means
	 * to override an existing group, prefix " " just to add the diagrasm to the group if already
	 * existing.
	 */
	private String requestNewGroupName(Component originator, int nDiagrams)
	{
		String groupName = null;
		String complaint = "";
		int option = 0;
		String[] options = new String[msgGroupingOptions.length];
		for (int i = 0; i < options.length; i++) {
			options[i] = msgGroupingOptions[i].getText();
		}
		do {
			do {
				// On repeated input request, name the previous problem
				if (groupName != null) {
					complaint = msgGroupNameRejected.getText().replace("%", groupName) + "\n";
				}
				groupName = JOptionPane.showInputDialog(originator, 
						msgGroupName.getText().replace("%1", complaint).replace("%2", Integer.toString(nDiagrams)));
				if (groupName == null) {
					// User has cancelled
					return null;
				}
				groupName = groupName.trim();
			} while (groupName.isEmpty() || groupName.equals(Group.DEFAULT_GROUP_NAME));
			// Check against names of existing groups
			option = 0;
			if (surface.hasGroup(groupName)) {
				option = JOptionPane.showOptionDialog(originator,
						msgGroupExists.getText().replace("%", groupName),
						msgActionGroup.getText(),
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						IconLoader.getIcon(94),
						options, options[option]);
			}
		} while (option == 2);	// try with different name
		if (option == options.length - 1) {	// cancelled
			return null;
		}
		// Insert a marker for overriding or not (to be cut off after identification)
		return (option == 1 ? "!" : " ") + groupName;
	}
	
	/**
	 * Enhances the {@code initialRootSet} with all {@code Root} objects that are directly and
	 * indirectly called or included by some {@link Root} objects from {@code initialRootSet}. 
	 * @param initialRootSet - a set of {@link Root} objects from where to start.
	 * @param initiator - a GUI component meant to be made the owner of message boxes etc., if
	 * being null then the messages won't be raised. 
	 * @param missingSignatures - optional {@link StringList} to gather signatures of missing referred diagrams
	 * @return an accomplished set of {@link Root}s.
	 */
	public Collection<Root> accomplishRootSet(Set<Root> initialRootSet, Component initiator, StringList missingSignatures)
	{
		Collection<Root> neededRoots = new Vector<Root>(initialRootSet);
		Collection<Diagram> addedDiagrams = expandRootSetOrSelection(initialRootSet, initiator, missingSignatures);
		if (addedDiagrams != null) {
			for (Diagram diagr: addedDiagrams) {
				neededRoots.add(diagr.root);
			}
		}
		return neededRoots;
	}
	
	/**
	 * Expands the given {@link Root} set {@code initialRoots} or the current selection by all directly
	 * or indirectly referenced subroutine and includable diagrams that hadn't already been selected.
	 * @param initialRoots - set of {@link Root} objects from which the dependencies are to be retrieved
	 * @param initiator - A responsible GUI component: message boxes will be modal to it; if null
	 * then no messages will be raised.
	 * @param missingSignatures - id a {@link StringList} is given here then it will gather signatures of missing referenced diagrams.
	 * @return the set of added diagrams in case {@link initialRoots} was given 
	 */
	protected Set<Diagram> expandRootSetOrSelection(Set<Root> initialRoots, Component initiator, StringList missingSignatures) {
		StringList missingRoots = missingSignatures;
		StringList duplicateRoots = new StringList();
		Set<Diagram> addedDiagrams = null;
		int nAdded = 0;
		if (initiator != null && missingRoots == null) {
			missingRoots = new StringList();
		}
		if (initialRoots == null) {
			nAdded = surface.expandSelectionRecursively(missingRoots, duplicateRoots);
		}
		else {
			addedDiagrams = surface.expandRootSet(initialRoots, missingRoots, duplicateRoots);
			nAdded = addedDiagrams.size();
		}
		if (initiator != null) {
			String message = msgSelectionExpanded.getText().replace("%", Integer.toString(nAdded));
			if (duplicateRoots.count() > 0) {
				message += msgAmbiguousSignatures.getText()
						.replace("%1", Integer.toString(duplicateRoots.count()))
						.replace("%2", duplicateRoots.concatenate("\n- "));
			}
			if (missingRoots.count() > 0) {
				message += msgMissingDiagrams.getText()
						.replace("%1", Integer.toString(missingRoots.count()))
						.replace("%2", missingRoots.concatenate("\n- "));
			}
			JOptionPane.showMessageDialog(initiator, message);
		}
		return addedDiagrams;
	}

    /**
     * In case {@code rootList} contains more than one element, raises an error message
     * that {@code actionName} is not allowed for multiple selection and returns true,
     * otherwise returns false.
     * @param rootList - {@link StringList} of selected {@link Root} signatures
     * @param actionName - designation of the intended action
     * @return true if illegal multiple selection was detected, false otherwise
     */
    private boolean checkIllegalMultipleAction(StringList rootList, String actionName)
    {
        int nSelected = rootList.count();
        if (nSelected > 1) {
        	if (nSelected > ROOT_LIST_LIMIT) {
        		// Avoid to make the option pane so large that the buttons can't be reached
        		rootList.remove(ROOT_LIST_LIMIT, nSelected);
        		rootList.add("...");
        	}
        	JOptionPane.showMessageDialog(this,
        			msgCantDoWithMultipleRoots.getText().
        			replace("%1", actionName).
        			replace("%2", Integer.toString(nSelected)).
        			replace("%3", rootList.concatenate("\n- ")),
        			msgTitleIllegal.getText(),
        			JOptionPane.ERROR_MESSAGE);
        	return true;
        }
        return false;
    }

    /**
     * Sets or unsets the shift flag and changes the appearance of dependent buttons accordingly
     */
    private void setShiftPressed(boolean isPressed) {
        if (isPressed) {
            this.btnZoom.setIcon(IconLoader.getIconImage("008_zoom_in.png", ICON_FACTOR));
            // START KGU#534 2018-06-27: Enh. #552
            this.btnRemoveDiagram.setIcon(IconLoader.getIconImage("045_remove.png", ICON_FACTOR));
            this.btnRemoveDiagram.setText(btnRemoveAllDiagrams.getText());
            // END KGU#534 2018-06-27
            this.isShiftPressed = true;
            if (surface.getZoom() <= 1) {
                btnZoom.setEnabled(false);
            }
        }
        else {
            this.isShiftPressed = false;
            this.btnZoom.setEnabled(true);
            this.btnZoom.setIcon(IconLoader.getIconImage("007_zoom_out.png", ICON_FACTOR));
            // START KGU#534 2018-06-27: Enh. #552
            this.btnRemoveDiagram.setIcon(IconLoader.getIconImage("100_diagram_drop.png", ICON_FACTOR));
            this.btnRemoveDiagram.setText(btnRemoveDiagrams.getText());
            // END KGU#534 2018-06-27			
        }
    }
    // END KGU#85 2015-11-30

	// START KGU#624 2018-12-24: Enh. #655
	/**
	 * Tries to open the online User Guide with the Arranger page in the browser
	 * @param keyBindings TODO
	 */
	public void helpArranger(boolean keyBindings)
	{
		String query = keyBindings ? "?menu=118&page=#keys_arranger" : "?menu=103";
		String help = Element.E_HELP_PAGE + query;
		boolean isLaunched = false;
		try {
			isLaunched = lu.fisch.utils.Desktop.browse(new URI(help));
		} catch (URISyntaxException ex) {
			logger.log(Level.WARNING, "Can't browse Arranger help URL.", ex);
		}
		// The isLaunched mechanism above does not signal an unavailable help page.
		// With the following code we can find out whether the help page was available...
		// TODO In this case we might offer to download the PDF for offline use,
		// otherwise we could try to open a possibly previously downloaded PDF ...
		URL url;
		HttpsURLConnection con = null;
		try {
			isLaunched = false;
			url = new URL(help);
			con = (HttpsURLConnection)url.openConnection();
			if (con != null) {
				con.connect();
			}
			isLaunched = true;
		} catch (SocketTimeoutException ex) {
			logger.log(Level.WARNING, "Timeout connecting to " + help, ex);
		} catch (MalformedURLException e1) {
			logger.log(Level.SEVERE, "Malformed URL " + help, e1);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed Access to " + help, e);
		}
		finally {
			if (con != null) {
				con.disconnect();
			}
		}
		if (!isLaunched)
		{
			String message = msgBrowseFailed.getText().replace("%", help);
			JOptionPane.showMessageDialog(this,
			message,
			msgTitleURLError.getText(),
			JOptionPane.ERROR_MESSAGE);
			// TODO We might look for a downloaded PDF version and offer to open this instead...
		}
	}
	// END KGU#624 2018-12-24

    // START KGU#2 2015-11-24
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.executor.IRoutinePool#findRoutinesByName(java.lang.String)
     */
    @Override
    public Vector<Root> findDiagramsByName(String rootName) {
        return surface.findDiagramsByName(rootName);
    }

    // START KGU#376 2017-04-11: Enh. #389
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.executor.IRoutinePool#findIncludesByName(java.lang.String)
     */
    @Override
    public Vector<Root> findIncludesByName(String rootName) {
        return surface.findIncludesByName(rootName);
    }
    // END KGU#376 2017-04-11

    /* (non-Javadoc)
     * @see lu.fisch.structorizer.executor.IRoutinePool#findRoutinesBySignature(java.lang.String, int)
     */
    @Override
    public Vector<Root> findRoutinesBySignature(String rootName, int argCount) {
        return surface.findRoutinesBySignature(rootName, argCount);
    }
    // END KGU#2 2015-11-24
    
    // START KGU#258 2016-09-26: Enh. #253: We need to traverse all roots for refactoring
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.executor.IRoutinePool#getAllRoots()
     */
    @Override
    public Set<Root> getAllRoots()
    {
        return surface.getAllRoots();
    }
    // END KGU#258 2016-09-26

    // START KGU#305 2016-12-16: Code revision
    // Shares the sorted list of Root elements held by the Surface object
    /**
     * Returns a sorted list of {@link Root}s held by Arranger.
     * @return vector of {@link Root}s, sorted by {@link Root#SIGNATURE_ORDER}
     */
    public static Vector<Root> getSortedRoots()
    {
        //System.out.println("Group list:");
        //for (Group group: groups) {
        //    System.out.println("\t" + group);
        //}
        return routines;
    }
    // END KGU#305 2016-12-16
    
    // START KGU#626 2018-12-31: Enh. #657
    /**
     * Returns a sorted list of {@link Group}s held by Arranger.
     * @return vector of {@link Group}, sorted by name
     */
    public static Vector<Group> getSortedGroups()
    {
        return groups;
    }
    // END KGU#626 2018-12-31
    

    // START KGU#117 2016-03-08: Introduced on occasion of Enhancement #77
    @Override
    public void clearExecutionStatus() {
        doButtons();
        surface.clearExecutionStatus();
    }

    public void doButtons() {
        btnSetCovered.setEnabled(Element.E_COLLECTRUNTIMEDATA);
        // START KGU#624/KGU#626 2018-12-27: Enh. #655, #657
        if (popupMenu != null && surface != null) {
        	int nSelected = surface.getSelectionCount();
        	this.popupAttributes.setEnabled(nSelected == 1);
        	this.popupExpandSelection.setEnabled(nSelected > 0);
        	this.popupRemove.setEnabled(nSelected > 0);
        	this.popupGroup.setEnabled(nSelected > 0);
        	this.popupExpandGroup.setEnabled(nSelected > 0);
        }
        // END KGU#624/KGU#626 2018-12-27
    }
    // END KGU#117 2016-03-08

    // START KGU#156 2016-03-10: An interface for an external update trigger was needed
    public void redraw() {
        surface.repaint();
    }
    // END KGU#156 2016-03-10

	// START KGU#305 2016-12-12: Enh. #305
	/**
	 * Returns the {@link Root} currently selected in Arranger, if it is a single one.
	 * @return Either a {@link Root} object or null (if none or more than 1 was selected)
	 */
	public Root getSelected() 
	{
		return surface.getSelected1();
	}
	// END KGU#305 2016-12-12

	// START KGU#305 2016-12-16
	/**
	 * Statically adds the given {@code _listener} to the set of {@link IRoutinePoolListener}s
	 * @param _listener - the listener to be registered.
	 * @see #addChangeListener(IRoutinePoolListener)
	 * @see #removeFromChangeListeners(IRoutinePoolListener)
	 */
	public static void addToChangeListeners(IRoutinePoolListener _listener)
	{
		listeners.add(_listener);
	}
	
	/**
	 * Statically removes the given {@code _listener} from the set of {@link IRoutinePoolListener}s
	 * @param _listener - the listener to be unregistered.
	 * @see #removeChangeListener(IRoutinePoolListener)
	 * @see #addToChangeListeners(IRoutinePoolListener)
	 */
	public static void removeFromChangeListeners(IRoutinePoolListener _listener)
	{
		listeners.remove(_listener);
	}
	
	@Override
	public void addChangeListener(IRoutinePoolListener _listener) {
		// Used just as an adaptor to the own static method
		addToChangeListeners(_listener);
	}

	@Override
	public void removeChangeListener(IRoutinePoolListener _listener) {
		// Used just as an adaptor to the own static method
		removeFromChangeListeners(_listener);
	}

	@Override
	public void routinePoolChanged(IRoutinePool _source, int _flags) {
		// START KGU#624 2018-12-21: Enh. #655
		if ((_flags & RPC_POOL_CHANGED) != 0) {
		// END KGU#624 2018-12-21
			routines.clear();
			routines.addAll(_source.getAllRoots());
			Collections.sort(routines, Root.SIGNATURE_ORDER);
			// START KGU#626 2018-12-31: Enh. #657
			groups.clear();
			groups.addAll(surface.getGroups());
			Collections.sort(groups, Group.NAME_ORDER);
			// END KGU#626 2018-12-31
		// START KGU#624 2018-12-21: Enh. #655
		}
		if ((_flags & RPC_SELECTION_CHANGED) != 0) {
			doButtons();
			updateStatusSelection();
		}
		if ((_flags & RPC_GROUP_COLOR_CHANGED) != 0) {
			surface.repaint();
		}
		// END KGU#624 2018-12-21
		for (IRoutinePoolListener listener: listeners) {
			listener.routinePoolChanged(this, _flags);
		}
	}
	// END KGU#305 2016-12-16

	// START KGU#305 2016-12-17: Enh. #305 External removal request (from  Arranger index)
	/**
	 * Removes the diagram given by root from the Arranger surface (if being placed there)
	 * @param _root - the root element of the diagram to remove
	 */
	public void removeDiagram(Root _root) {
		if (surface != null) {
			surface.removeDiagram(_root);
		}
	}
	// END #305 2016-12-17
	
	// START KGU#534 2018-06-27: Enh. #552
	/**
	 * Removes all diagrams from the Arranger surface.
	 * @param initiator -the commanding GUI component (for placement of message boxes etc., may be null)
	 * @return true if this was accomplished
	 */
	public boolean removeAllDiagrams(Component initiator) {
		boolean done = false;
		if (initiator == null) initiator = this;
		if (surface != null && !this.getAllRoots().isEmpty()) {
			if (JOptionPane.showConfirmDialog(initiator == null ? this : initiator, 
					msgConfirmRemoveAll.getText(), 
					msgTitleWarning.getText(),
					JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
				// START KGU#626 2019-01-04: Enh. #657 - Make sure modified groups get notified about
				lu.fisch.structorizer.gui.Diagram.startSerialMode();
				try {
					// Field groups might be modified via notifications, so better work on a copy
					Group[] groupsToCheck = groups.toArray(new Group[groups.size()]);
					for (int i = 0; i < groupsToCheck.length; i++) {
						if (!groupsToCheck[i].isDefaultGroup()) {
							this.dissolveGroup(groupsToCheck[i].getName(), initiator);
						}
					}
				}
				finally {
					lu.fisch.structorizer.gui.Diagram.endSerialMode();
				}
				// END KGU#626 2019-01-04
				done = surface.removeAllDiagrams(initiator);
			}
		}
		return done;
	}
	// END KGU#534 2018-06-27
	
	// START KGU#626 2018-12-31: Enh. #657
	/**
	 * Removes the group with the given {@code name}.
	 * @param name - name of the {@link Group} to be removed
	 * @param withDiagrams - if true then the member diagrams will also be removed if not held by
	 * any other group, otherwise the orphaned diagrams will be handed over to the default group.
	 * @param initiator - the GUI component commanding this action (for association of message boxes etc.)
	 * @return true if the group with given name had existed and has been removed.
	 */
	public boolean removeGroup(String name, boolean withDiagrams, Component initiator)
	{
		return surface.removeGroup(name, withDiagrams, initiator);
	}
	// END KGU#626 218-12-31
	
	// START KGU#626 2019-01-04: Enh. #657
	/**
	 * Removes the group with the given {@code name}.
	 * @param name - name of the {@link Group} to be dissolved
	 * @param initiator - the GUI component commanding this action (for association of message boxes etc.)
	 * @return true if the group with given name had existed and has been removed.
	 */
	public boolean dissolveGroup(String name, Component initiator)
	{
		return surface.dissolveGroup(name, initiator);
	}
	// END KGU#626 2019-01-04

	// START KGU#373 2017-03-28: Enh. #386
	/**
	 * Saves unsaved changes of all held diagrams and groups. Will report the names
	 * of the groups that couldn't be saved if {@code initiator} isn't null.
	 * If the saving of diagrams was interrupted then the saving of groups won't be
	 * tried here.
	 * @param initiator - the originating GUI component
	 * @return true if the saving attempt had been completed, false if vetoed
	 */
	public boolean saveAll(Component initiator) {
		return saveAll(initiator, false);
	}
	// END KGU#373 2017-03-28

	// START KGU#373 2017-03-28: Enh. #386
	/**
	 * Saves unsaved changes of all held diagrams and groups. Will report the names
	 * of the groups that couldn't be saved if {@code initiator} isn't null.
	 * If the saving of diagrams was interrupted then the saving of groups won't be
	 * tried here.
	 * @param initiator - the originating GUI component
	 * @param goingToClose TODO
	 * @return true if the saving attempt had been completed, false if vetoed
	 */
	public boolean saveAll(Component initiator, boolean goingToClose) {
		// START KGU#626 2019-01-06: Enh. #657
		//this.surface.saveDiagrams(initiator, null, false, true);
		if (initiator == null) {
			initiator = this;
		}
		return this.surface.saveDiagrams(initiator, null, goingToClose, goingToClose && Element.E_AUTO_SAVE_ON_CLOSE) &&
				this.surface.saveGroups(initiator, goingToClose);
		// END KGU#626 2019-01-06
	}
	// END KGU#373 2017-03-28
	
	// START KGU#497 2018-02-17: Enh. #512
	/**
	 * Adds or updates Arranger-specific preferences or properties to {@code ini}
	 * such that they may be saved.
	 * @param ini - the instance of the {@link Ini} class. 
	 */
	public void updateProperties(Ini ini)
	{
		ini.setProperty("arrangerZoom", Float.toString(surface.getZoom()));
		// START KGU#623 2018-12-20: Enh. #654
		ini.setProperty("arrangerDirectory", surface.currentDirectory.getAbsolutePath());
		// END KGU#623 2018-12-20
		// START KGU#630 2019-01-13: Enh. #662/4
		ini.setProperty("arrangerRelCoords", (A_STORE_RELATIVE_COORDS ? "1" : "0"));
		// END KGU#630 2019-01-13

	}
	// END KGU#497 2018-02-17

	// START KGU#594 2018-10-06: Issue #552 - help to reduce unnecessary serial approval questions
	/**
	 * Checks if there are {@link Root}s with unsaved changes held by the {@link Surface}. 
	 * The given {@link Root} {@code toBeIgnored} is not checked. 
	 * @param toBeIgnored - null or a {@link Root} the status of which is irrelevant
	 * @return true in case there is a dirty {@link Root} other than {@code toBeIgnored}
	 */
	public boolean hasUnsavedChanges(Root toBeIgnored) {
		for (Root root: surface.getAllRoots()) {
			if (root.hasChanged() && root != toBeIgnored) {
				return true;
			}
		}
		// START KGU#626 2019-01-05: Enh. #657 - there might also be unsaved group changes
		for (Group group: groups) {
			if (group.hasChanged()) {
				return true;
			}
		}
		// END KGU#626 2019-01-05
		return false;
	}
	// END KGU#594 2018-10-06

	// START KGU#624 2018-12-25: Enh. #655
	@Override
	public void LangChanged(LangEvent evt) {
		if (evt.getSource() == msgDiagramsSelected && this.statusbar != null)
		{
			this.updateStatusSelection();
		}
	}
	// END KGU#624 2018-12-25
	
	// START KGU#363/KGU#624 2018-12-27: Enh. #372, #655
	/**
	 * Opens the {@link AttributeInspector} for the specified {@code _root}.
	 * @param _root - a {@link Root} the attributes of which are to be presented
	 */
	public void inspectAttributes(Root _root) {
		RootAttributes licInfo = new RootAttributes(_root);
		AttributeInspector attrInsp = new AttributeInspector(
				this, licInfo);
		attrInsp.setVisible(true);
		if (attrInsp.isCommitted()) {
			_root.addUndo(true);
			_root.adoptAttributes(attrInsp.licenseInfo);
			this.routinePoolChanged(surface, RPC_POOL_CHANGED);
		}
	}
	// END KGU#363/KGU#624 2018-12-27

	/**
	 * Stores the {@link Group} specified by {@code group} or the current selection as diagram
	 * arrangement to either the already associated or a new file.<br/>
	 * Depending on the group type or the choice of the user, this file will either be only a list
	 * of reference points and filenames (this way not being portable) or be a compressed archive
	 * containing the list file as well as the referenced NSD files will be produced such that it
	 * can be ported to a different location and extracted there.
	 * @param initiator TODO
	 * @param group - the {@link Group} to be saved
	 * @return the eventually associated {@link Group} object if the saving succeeded without error,
	 * otherwise null.
	 */
	public Group saveGroup(Component initiator, Group group) {
		return surface.saveArrangement(initiator, group, false);
	}

	/**
	 * Detaches the diagram {@code root} from the given {@code group} (if it
	 * had been a member of).
	 * @param group - {@link Group} {@code root} is suposed to be member of
	 * @param root - the {@link Root} object to be removed from {@code group}
	 * @param initiator - the {@link Component} commanding the action
	 * @return true if the detachment worked and {@code group} actually changed
	 */
	public boolean detachRootFromGroup(Group group, Root root, Component initiator) {
		return surface.removeDiagramFromGroup(group, root, false, initiator);
	}

	/**
	 * Attaches the diagram {@code root} to the given {@code targetGroup} (if it hadn't
	 * already been member of it). If {@code sourceGroup} is given, contains the diagram
	 * represented by {@code root} and differs from {@code targetGroup} then the diagram
	 * {@code root} will be detached from it. 
	 * @param targetGroup - the target {@link Group} for 
	 * @param root - the {@link Root} to be attached to {@code targetGroup}
	 * @param sourceGroup - if given then {@code root} will be detached from that group
	 * (provided it was a member there)
	 * @param initiator - the {@link Component} that initiated the action
	 * @see #detachRootFromGroup(Group, Root, Component)
	 */
	public boolean attachRootToGroup(Group targetGroup, Root root, Group sourceGroup, Component initiator) {
		boolean done = surface.addDiagramToGroup(targetGroup, root);
		// Remove the diagram from its former owner if sourceGroup is given 
		if (done && sourceGroup != null && sourceGroup != targetGroup) {
			surface.removeDiagramFromGroup(sourceGroup, root, false, initiator);
		}
		return done;
	}

	/**
	 * Identifies all groups associated with the diagram given by in {@code interestingDiagrams}
	 * @param root - the {@link Root} object, the group membership of which is to be returned
	 * @param supressDefaultGroup - if true then the default group won't be reported
	 * @return a collection (set) of the identified groups
	 */
	public Collection<Group> getGroupsFromRoot(Root root, boolean suppressDefaultGroup)
	{
		return surface.getGroupsFromRoot(root, suppressDefaultGroup);
	}

}

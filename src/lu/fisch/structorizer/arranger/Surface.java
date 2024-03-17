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
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents the interactive drawing area for arranging several diagrams
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2009-08-18      First Issue
 *      Kay Gürtzig     2015-10-18      Several enhancements to improve Arranger usability (see comments)
 *      Kay Gürtzig     2015-11-14      Parameterized creation of dependent Mainforms (to solve issues #6, #16)
 *      Kay Gürtzig     2015-11-18      Several changes to get scrollbars working (issue #35 = KGU#85)
 *                                      removal mechanism added, selection mechanisms revised
 *      Kay Gürtzig     2015-12-17      Bugfix KGU#111 for Enh. #63, preparations for Enh. #62 (KGU#110)
 *      Kay Gürtzig     2015-12-20      Enh. #62 (KGU#110) 1st approach: Load / save as mere file list.
 *                                      Enh. #35 (KGU#88) Usability improvement (automatic pinning)
 *      Kay Gürtzig     2016-01-02      Bugfix #78 (KGU#119): Avoid reloading of structurally equivalent diagrams 
 *      Kay Gürtzig     2016-01-15      Enh. #110: File open dialog now selects the NSD filter
 *      Kay Gürtzig     2016-03-02      Bugfix #97 (KGU#136): Modifications for stable selection
 *      Kay Gürtzig     2016-03-03      Bugfix #121 (KGU#153): Successful file dropping must not pop up an error message
 *      Kay Gürtzig     2016-03-08		Bugfix #97: Method for drawing info invalidation added (KGU#155) 
 *      Kay Gürtzig     2016-03-09      Enh. #77 (KGU#117): Methods clearExecutionStatus and setCovered added
 *      Kay Gürtzig     2016-03-12      Enh. #124 (KGU#156): Generalized runtime data visualisation (refactoring)
 *      Kay Gürtzig     2016-03-14      Enh. #62 update: currentDirectory adopted from first added diagram.
 *      Kay Gürtzig     2016-03-16      Bugfix #132: Precautions against stale Mainform references (KGU#158)
 *      Kay Gürtzig     2016-04-14      Enh. #158: Methods for copy and paste of diagrams as XML strings (KGU#177)
 *                                      Selection mechanisms mended
 *      Kay Gürtzig     2016-05-09      Issue #185: Chance to store unsaved changes before removal (KGU#194)
 *      Kay Gürtzig     2016-07-01      Enh. #62: Opportunity to save/load zipped arrangement (KGU#110)
 *      Kay Gürtzig     2016-07-03      Dialog message translation mechanism added (KGU#203). 
 *      Kay Gürtzig     2016-07-19      Enh. #192: File name proposals slightly modified (KGU#205)
 *      Kay Gürtzig     2016-09-26      Enh. #253: New public method getAllRoots() added.
 *      Kay Gürtzig     2016-10-11      Enh. #267: New notification changes to the set of diagrams now trigger analyser updates
 *      Kay Gürtzig     2016-11-14      Enh. #289: The dragging-in of arrangement files (.arr, .arrz) enabled.
 *      Kay Gürtzig     2016-11-15      Enh. #290: Further modifications to let a Mainform insert arrangements
 *      Kay Gürtzig     2016-12-12      Enh. #305: New mechanism to update the Arranger indices in the related Mainforms
 *      Kay Gürtzig     2016-12-17      Enh. #305: New method removeDiagram(Root)
 *      Kay Gürtzig     2016-12-28      Enh. #318: Shadow path for Roots unzipped (from an arrz) into a temp dir
 *      Kay Gürtzig     2016-12-29      Enh. #315: More meticulous detection of diagram conflicts
 *      Kay Gürtzig     2017-01-04      Bugfix #321: Make sure Mainforms save the actually iterated Roots
 *      Kay Gürtzig     2017-01-05      Enh. #319: Additional notification on test coverage status change
 *      Kay Gürtzig     2017-01-11      Fix KGU#328 in method replaced()
 *      Kay Gürtzig     2017-01-13      Enh. #305 / Bugfix KGU#330: Arranger index notification on name and dirtiness change
 *      Kay Gürtzig     2017-04-22      Enh. #62, #318: Confirmation before overwriting a file, shadow paths considered
 *      Kay Gürtzig     2017-05-26      Bugfix #414: Too large bounding boxes caused errors and made the GUI irresponsive
 *      Kay Gürtzig     2017-10-23      Issue #417: Linear scrolling unit adaptation to reduce drawing time complexity
 *                                      Enh. #35: Scrolling dimensioning mechanism revised (group layout dropped) 
 *      Kay Gürtzig     2017-11-03      Bugfix #417: division by zero exception in scroll unit adaptation averted
 *      Kay Gürtzig     2018-02-17      Enh. #512: Zoom mechanism implemented
 *      Kay Gürtzig     2018-02-20      Magic numbers replaced, Enh. #515 first steps toward a silhouette allocation
 *      Kay Gürtzig     2018-02-21      Enh. #515: Working first prototype for space-saving area management
 *      Kay Gürtzig     2018-03-13      Enh. #519: enabled to handle Ctrl + mouse wheel as zooming trigger (see comment)
 *      Kay Gürtzig     2018-03-19      Enh. #512: Zoom compensation for PNG export mended (part of background was transparent)
 *      Kay Gürtzig     2018-06-10      Overriding of paint() replaced by paintComponent()
 *      Kay Gürtzig     2018-06-18      Bugfix #544 (KGU#524): zoom adaptation forgotten in adaptLayout() -> unnecessary revalidations
 *      Kay Gürtzig     2018-06-27      Enh. #552: Serial decisions on saveAll allowed, removeAllDiagrams() added
 *      Kay Gürtzig     2018-09-10      Bugfix #508/#512: A diagram loaded into a zoomed Surface first could have too small a shape
 *                                      for the text drawing in Structorizer due to font height/width rounding effects
 *      Kay Gürtzig     2018-09-12      Issue #372: Attribute handling, particularly for arrz file members, improved
 *      Kay Gürtzig     2018-12-20      Issue #654: Current directory is now restored from ini file on first launch
 *      Kay Gürtzig     2018-12-21/22   Enh. #655: Zoom and selection notifications to support status bar,
 *                                      multiple selection enabled and established as base for collective operations
 *      Kay Gürtzig     2018-12-22      Bugfix #656: Loading of referred files residing in an arrz file fixed.
 *      Kay Gürtzig     2018-12-23      Bugfix #512: On dropping/pushing diagrams the scrolling had used wrong coordinates
 *      Kay Gürtzig     2018-12-25      Enh. #655: Dialog revisions
 *      Kay Gürtzig     2018-12-26      Two cross reference maps introduced (rootMap, nameMap), expandSelectionRecursively() impemented
 *      Kay Gürtzig     2018-12-31      Enh. #657: Group management implemented
 *      Kay Gürtzig     2019-01-04      Enh. #657: Group management significantly advanced and improved
 *      Kay Gürtzig     2019-01-09      Bugfix #515: updateSilhouette() revised (KGU#633)
 *      Kay Gürtzig     2019-01-12      Enh. #662/3: New method to rearrange all diagrams by groups
 *      Kay Gürtzig     2019-01-13      Enh. #662/4: enabled to save arrangements with relative coordinates
 *      Kay Gürtzig     2019-01-16      Enh. #662/2: Coloured group name popup
 *      Kay Gürtzig     2019-02-02      Bugfix #672: If the saving was cancelled in FileChooser, the group must not be renamed
 *      Kay Gürtzig     2019-02-03      Issue #673: The dimensions were to be enlarged by a DEFAULT_GAP size
 *      Kay Gürtzig     2019-02-11      Issue #677: Inconveniences on saving arrangement archives mended
 *      Kay Gürtzig     2019-03-01      Enh. #691: Method renameGroup() introduced for exactly this purpose
 *      Kay Gürtzig     2019-03-07      Enh. #385: findRoutinesBySignature(String, int) made aware of default arguments
 *      Kay Gürtzig     2019-03-10      Enh. #698: Refactoring/code revision: basics of loading/ saving delegated to Archivar
 *      Kay Gürtzig     2019-03-11      Bugfix #699: On saving diagrams from an archive to another archive they must be copied
 *      Kay Gürtzig     2019-03-12/13   Enh. #698: New IRoutinePool mmethods added
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Root and Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-03-14      Issues #518, #544, #557: Measures against drawing contention revised and extended.
 *      Kay Gürtzig     2019-03-15      Bugfix #703: isMoved status of diagrams wasn't reset on saving a containing arrangement
 *                                      Measures for bugfix #699 skipped on diagrams that are only members of the saved group.
 *                                      KGU#697: Bugfix in updateSilhouette(), at least partially
 *      Kay Gürtzig     2019-03-27      Issue #717: Configurable base scroll unit (adaptScrollUnits(Rect))
 *      Kay Gürtzig     2019-03-28      Enh. #657: Retrieval for includables/subroutines now with group filter,
 *                                      fixed the fix for the fix #699 again.
 *                                      Further drawing acceleration - clip bounds instead of viewport rect
 *      Kay Gürtzig     2019-03-30      Issues #699, #720: Several fixes and fine-tuning
 *      Kay Gürtzig     2019-05-14      Bugfix #722: Drawing within a BufferedImage (PNG export) failed.
 *      Kay Gürtzig     2019-07-31      Bugfix #731: File renaming failure on Linux made arrz files vanish
 *      Kay Gürtzig     2019-07-31      Bugfix #732: Diagrams cloned had shared the position rather than copied.
 *      Kay Gürtzig     2019-10-05      Bugfix #759: Missing reaction to closed Mainforms impaired functioning
 *      Kay Gürtzig     2019-10-13      Bugfix #763: Handling of stale file connections on saving and loading arrangements
 *      Kay Gürtzig     2019-10-14      Bugfix #764: Updating the .arr file of a modified group used to fail.
 *      Kay Gürtzig     2019-10-15      Bugfix #763: On resaving stale diagrams, the shadow path had to be cleared.
 *      Kay Gürtzig     2019-11-28      Bugfix #788: Offered arrz extraction to user-chosen folder was ignored
 *      Kay Gürtzig     2020-02-16      Issue #815: Combined ArrangerFilter introduced for convenience
 *      Kay Gürtzig     2020-02-17      Bugfix #818: Strong inconsistencies by outdated method replace() mended.
 *      Kay Gürtzig     2020-12-14      Zoom scale reverted, i.e. zomeFactor is no longer inverse: 0.5 means 50% now
 *      Kay Gürtzig     2020-12-23      Enh. #896: Readiness for dragging now indicated by different cursor
 *      Kay Gürtzig     2020-12-29      Issue #901: Time-consuming actions set WAIT_CURSOR now
 *      Kay Gürtzig     2020-12-30      Issue #901: WAIT_CURSOR also applied to saveDiagrams() and saveGroups()
 *      Kay Gürtzig     2021-01-13      Enh. #910: Group visibility now also affects the contained diagrams
 *      Kay Gürtzig     2021-02-24      Enh. #410: Root search methods enhanced by namespace similarity ranking
 *      Kay Gürtzig     2021-03-01      Enh. #410: Temporary pool notification suppression introduced
 *      Kay Gürtzig     2022-06-01      Enh. #1035: New method getGroup(String)
 *      Kay Gürtzig     2024-03-16      Issue #1138: Provide a better and more usable diagram collision information
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2018-06-10 (Kay Gürtzig)
 *      - The change (paint() -> paintComponent()) was made to comply with the Oracle Swing debugging guidelines
 *        (https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/swing002.html#BABHEADA),
 *        in particular section 13.2.8 Custom Painting and Double Buffering:
 *        "Although you can override paint and do custom painting in the override, you should instead
 *         override paintComponent. The JComponent.paint method ensures that painting happens to the
 *         double buffer. If you override paint directly, you may lose double buffering."
 *      2018-03-13 (Kay Gürtzig)
 *      - According to a GUI suggestion by newboerg, surface now also implements MouseWheelListener in
 *        order to let ctrl + mouse wheel forward to zoom out and ctrl + mouse wheel backward to zoom in
 *      - is added as listener to Arranger.scrollarea  
 *      2018-02-21 (Kay Gürtzig)
 *      - Rather than to place added diagrams along the top and left window border, now there is a more
 *        intelligent strategy implemented, which still aligns from top to bottom, left to right but tries
 *        to fill the gaps at a given level before diagrams are put beneath in new "rows". This is done
 *        via constructing the "lower silhouette" from the diagrams placed so far and then to look for the
 *        uppermost breach of sufficient width to accommodate the diagram to be added. See #515 on GitHub
 *        for illustrations. 
 *      2016-03-16 (Kay Gürtzig)
 *      - It still happened that double-clicking on a diagram seemed to have no effect. In these cases
 *        actually there was a stale Mainform reference. The reason was that the windowClosing() trigger
 *        used to compare the Roots instead of the Mainform itself. So wrong reference may have been
 *        removed, e.g. if the Mainform contained some called subroutine it wasn't associated with.
 *        On the other hand, a handling for such a case was missing in the mouseClicked() trigger. 
 *      2016-03-08 (Kay Gürtzig)
 *      - Enh. #77: For Test Coverage Tracking, Arranger in its function of a subroutine pool had to
 *        be enabled to set oder clear coverage flags 
 *      2016-01-02 (Kay Gürtzig)
 *      - Bug #78: On (re)placing diagrams from a Structorizer Mainframe, an identity check had already
 *        duplicate diagram presence, but on file dropping and reloading a saved arrangement (Enhancement #62),
 *        an identity check didn't help, of course. So for these cases, a structural equivalence check had
 *        to be used instead - the bugfix realises this by new method Root.equals(). 
 *      2015-11-18 (Kay Gürtzig)
 *      - In order to achieve scrollability, autoscroll mode (on dragging) had to be enabled, used area has
 *        to be communicated (nothing better than resetting the layout found - see adaptLayout())
 *      - Method removeDiagram() added,
 *      - selection consistency improved (never select or unselect a diagram element other than root, don't
 *        select eclipsed diagrams, don't leave selection flag on diagrams no longer selected).
 *      2015-11-14 (Kay Gürtzig)
 *      - The creation of dependant Mainforms is now done via a parameterized constructor in order to
 *        inform the Mainform that it must not exit on closing but may only dispose.
 *      2015-10-18 (KGU)
 *      - New interface method replaced() implemented that allows to keep track of NSD replacement in a
 *        related Mainform (KGU#48)
 *      - New interface method findSourcesByName() to prepare subroutine execution in a future effort (KGU#2)
 *      - Method saveDiagrams() added, enabling the Mainforms to save dirty diagrams before exit (KGU#49)
 *
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import lu.fisch.graphics.Rect;
import lu.fisch.structorizer.archivar.Archivar;
import lu.fisch.structorizer.archivar.Archivar.ArchiveIndex;
import lu.fisch.structorizer.archivar.ArchiveRecord;
import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.archivar.IRoutinePoolListener;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Updater;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.XmlGenerator;
import lu.fisch.structorizer.gui.Diagram.SerialDecisionAspect;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.gui.Mainform;
import lu.fisch.structorizer.gui.Menu;
import lu.fisch.structorizer.io.ArrFilter;
import lu.fisch.structorizer.io.ArrZipFilter;
import lu.fisch.structorizer.io.ArrangerFilter;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.io.PNGFilter;
import lu.fisch.structorizer.io.StructogramFilter;
import lu.fisch.structorizer.locales.LangPanel;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.parsers.NSDParser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;
import net.iharder.dnd.FileDrop;

/**
 * Class represents the interactive viewport for arranging several Nassi Shneiderman
 * diagrams (as part of a scroll pane).<br/>
 * 2018-03-13: Enhanced the inheritance by {@link MouseWheelListener} to enable ctrl + wheel zooming 
 * @author robertfisch, codemanyak
 */
@SuppressWarnings("serial")
public class Surface extends LangPanel implements MouseListener, MouseMotionListener, WindowListener, Updater, IRoutinePool, ClipboardOwner, MouseWheelListener, WindowFocusListener {

	// START#484 KGU 2018-03-22: Issue #463
	public static final Logger logger = Logger.getLogger(Surface.class.getName());
	// END KGU#484 2018-03-22

	private Vector<Diagram> diagrams = new Vector<Diagram>();
	// START KGU#305 2016-12-16: Code revision
	private final Vector<IRoutinePoolListener> listeners = new Vector<IRoutinePoolListener>();
	// END KGU#305 2016-12-16
	// START KGU#624 2018-12-26: Enh. #655 We need more efficient searching
	private final HashMap<String, Vector<Diagram>> nameMap = new HashMap<String, Vector<Diagram>>();
	private final HashMap<Root, Diagram> rootMap = new HashMap<Root, Diagram>();
	// END KGU#624 2018-12-26
	// START KGU#626 2018-12-23: Enh. #657
	private final HashMap<String, Group> groups = new HashMap<String, Group>();
	// END KGU#626 2018-12-23
	
	/** Default minimum distance between diagrams when allocated */ 
	private static final int DEFAULT_GAP = 10;
	/** Default width for a diagram never drawn before */ 
	private static final int DEFAULT_WIDTH = 120;
	/** Default height for a diagram never drawn before */ 
	private static final int DEFAULT_HEIGHT = 150;
	/** Empirical width estimate for an empty diagram */ 
	private static final int MIN_WIDTH = 80;
	/** Empirical height estimate for an empty diagram */ 
	private static final int MIN_HEIGHT = 118;
	
	/** Symbols for the degree of content difference on diagram comparison for equivalence levels 3..6 */
	private static final String[] SYMBOLS_CONTENT_DIFF = {"*=*", "=", "≠", "?"};
	/** Symbols for the path difference on diagram comparison for equivalence levels 3..6 */
	private static final String[] SYMBOLS_PATH_DIFF = {"=", "≠", "≠", "≠"};

	/** Current/Last actual mouse coordinates on dragging diagrams (null while nothing being dragged) */
	private Point dragPoint = null;
	// START KGU#624 2018-12-21: Enh. #655 Diagram -> Set<Diagram>
	//private Diagram mouseSelected = null;
	/** The {@link Diagram}s currently selected via mouse click */
	private final Set<Diagram> diagramsSelected = new HashSet<Diagram>();
	// END KGU#624 2018-12-21
	// START KGU#630 2019-01-09: Enh. #662/2
	/** The {@link Group}s currently selected via mouse click */
	private final Set<Group> groupsSelected = new HashSet<Group>();
	// END KGU#630 2019-01-09
	// START KGU#624 2018-12-23: Enh. #655 Drag a selection area
	private Rectangle dragArea = null;
	// END KGU#624 2018-12-23
	// START KGU#88 2015-11-24: We may often need the pin icon
	/** Caches the icon used to indicate pinned state in the requested size */
	public static Image pinIcon = null;
	// END KGU#88 2015-11-24
	// START KGU#497 2018-02-17: Enh. #   - new zoom facility
	/** The factor by which the drawing is currently downscaled */
	private float zoomFactor = 0.5f;
	// END KGU#497 2018-02-17
	// START KGU#110 2015-12-21: Enh. #62, also supports PNG export
	public File currentDirectory = new File(System.getProperty("user.home"));
	// END KGU#110 2015-12-21
	// START KGU#630 2019-01-09: Enh. #662/2 central setting to enable/disable drawing of groups
	protected boolean drawGroups = false;
	private boolean selectGroups = false;	// To be ignored while drawGroups == false
	private JPopupMenu pop = new JPopupMenu();
	private JLabel lblPop = new JLabel("",SwingConstants.CENTER);
	// END KGU#630 2019-01-09
	private boolean wasContented = false;
	
	// START KGU#202 2016-07-03
	public final LangTextHolder msgFileLoadError = new LangTextHolder("File Load Error:");
	public final LangTextHolder msgSavePortable = new LangTextHolder("You may save this arrangement\n- either as portable compressed archive (*.arrz)\n- or as mere arrangement list with file paths (*.arr).");
	public final LangTextHolder lblSaveAsArrz = new LangTextHolder("As archive (*.arrz)");
	public final LangTextHolder lblSaveAsArr = new LangTextHolder("As list (*.arr)");
	public final LangTextHolder msgSaveDialogTitle = new LangTextHolder("Save arranged set of diagrams ...");
	public final LangTextHolder msgSaveError = new LangTextHolder("Error on saving the arrangement:");
	public final LangTextHolder msgLoadDialogTitle = new LangTextHolder("Reload a stored arrangement of diagrams ...");
	public final LangTextHolder msgExtractDialogTitle = new LangTextHolder("Extract to a directory (optional)?");
	public final LangTextHolder msgArrLoadError = new LangTextHolder("Error on loading the arrangement:");
	public final LangTextHolder msgExportDialogTitle = new LangTextHolder("Export diagram as PNG ...");
	public final LangTextHolder msgExportError = new LangTextHolder("Error while saving the image!");
	public final LangTextHolder msgParseError = new LangTextHolder("NSD-Parser Error:");
	public final LangTextHolder msgResetCovered = new LangTextHolder("Routine is already marked as test-covered! Reset coverage mark?");
	public final LangTextHolder msgCoverageError = new LangTextHolder("No suitable routine diagram selected, cannot mark anything as covered!");
	public final LangTextHolder msgUnsavedDiagrams = new LangTextHolder("Couldn't save these diagrams:");
	public final LangTextHolder msgUnsavedHint = new LangTextHolder("You might want to double-click and save them via Structorizer first.");
	public final LangTextHolder msgUnsavedContinue = new LangTextHolder("Continue nevertheless?");
	public final LangTextHolder msgNoArrFile = new LangTextHolder("No Arranger file found");
	// END KGU#202 2016-07-03
	// START KGU#289 2016-11-14: Enh. #289
	public final LangTextHolder msgDefectiveArr = new LangTextHolder("Defective Arrangement.");
	public final LangTextHolder msgDefectiveArrz = new LangTextHolder("Defective Portable Arrangement.");
	// END KGU#289 2016-11-14
	// START KGU#312 2016-12-29: Enh. #315 more meticulous equivalence analysis on insertion
	public final LangTextHolder titleDiagramConflict = new LangTextHolder("Diagram conflict");
	// START KGU#1124 2024-03-16: Issue #1138 More sensible conflict information
	//public final LangTextHolder[] msgInsertionConflict = {
	//		new LangTextHolder("There is another version of diagram \"%2\",\nat least one of them has unsaved changes."),
	//		new LangTextHolder("There is an equivalent copy of diagram \"%1\"\nwith different path \"%2\"."),
	//		new LangTextHolder("There is a differing diagram with signature \"%1\"\nand path \"%2\".")
	//};
	public final LangTextHolder msgInsertionConflict = new LangTextHolder("There are differing arranged diagrams for \"%\":");
	public final LangTextHolder titleConflContentDiff = new LangTextHolder("Content");
	public final LangTextHolder titleConflPathDiff = new LangTextHolder("Path");
	public final LangTextHolder titleConflSignature = new LangTextHolder("Fully qualified signature");
	public final LangTextHolder titleConflPath = new LangTextHolder("File path");
	public final LangTextHolder titleConflGroups = new LangTextHolder("Groups");
	public final LangTextHolder msgOk = new LangTextHolder("OK");
	public final LangTextHolder msgNoFurtherConflicts = new LangTextHolder("Don't show further conflicts");
	// END KGU#1124 2024-03-16
	// END KGU#312 2016-12-29
	// START KGU#408 2021-03-01: Enh. ##410 Again monstruous contention
	private boolean notifications_enabled = true;
	private int deferred_notifications = 0;
	// END KGU#408 2021-03-01
	// START KGU#385 2017-04-22: Enh. #62
	public static final LangTextHolder msgOverwriteFile = new LangTextHolder("Overwrite existing file \"%\"?");
	public static final LangTextHolder msgConfirmOverwrite = new LangTextHolder("Confirm Overwrite");
	// END KGU#385 2017-04-22
	// START KGU#626 2018-12-27/2019-01-04: Enh. #657
	public static final LangTextHolder msgTooltipSelectThis = new LangTextHolder("Select %1 (+shift: add it to the selection)%2 and bring it up to top.");
	public static final LangTextHolder msgGroupRemovalError = new LangTextHolder("Error on removing group «%»");
	public static final LangTextHolder msgArrangementAlreadyLoaded = new LangTextHolder("The arrangment file \"%1\" has already been loaded to group «%2».\nLoad it again with a modified group name?");
	public static final LangTextHolder msgArrangementNotLoaded = new LangTextHolder("Arrangement loading cancelled.");
	public static final LangTextHolder msgSaveAsNewGroup = new LangTextHolder("as new group");
	public static final LangTextHolder msgSelectGroup = new LangTextHolder("Please decide whether to update the file of an existing group or to create a new arrangement:");
	public static final LangTextHolder msgConfirmRemoveGroup = new LangTextHolder("Group «%» became empty. Do you want to remove it now?");
	public static final LangTextHolder msgSaveGroupChanges = new LangTextHolder("Group «%» has pending changes.\nDo you want to save these changes now?");
	public static final LangTextHolder msgUnsavedGroups = new LangTextHolder("Couldn't save these groups (arrangements):");
	// END KGU#626 2018-12-27/2019-01-04
	// START KGU#631 2019-01-08: Issue #663
	public static final LangTextHolder msgDiagram = new LangTextHolder("diagram «%»");
	public static final LangTextHolder msgGroup = new LangTextHolder("group «%»");
	// END KGU#631 2019-01-08
	// START KGU#669 2019-03-01: Enh. #691
	public static final LangTextHolder msgGroupExists = new LangTextHolder("Group with name «%» already exists!");
	public static final LangTextHolder titleRenameGroup = new LangTextHolder("Renaming Group «%1» to «%2»");
	public static final LangTextHolder msgRenameArrFile = new LangTextHolder("Rename arrangement file \"%1\"\nto \"%2\"?");
	public static final LangTextHolder msgRenamingFailed = new LangTextHolder("Could not rename arrangement file to \"%\"!\nAction cancelled!");
	// END KGU#669 2019-03-01
	// START KGU#680 2019-03-11: Bugfix #699
	public static final LangTextHolder msgSharedDiagrams = new LangTextHolder("The following diagrams will now be shared with the listed groups\n - %1\n\nBe aware that any modification to them will have an impact on all these groups\nand archive %2, even if the latter isn't loaded anymore!");
	// END KGU#680 2019-03-11
	// START KGU#749 2019-10-13: Bugfix #763
	public static final LangTextHolder msgFileMissing = new LangTextHolder(" missing!");
	// END KGU#749 2019-10-13

	@Override
	public void paintComponent(Graphics g)
	// START KGU#497 2018-03-19: Enh. #512 - The PNG export must compensate the zoom factor
	{
		this.paintComponent(g, false, false, 0, 0);
	}
	
	/**
	 * Specific variant of {@link #paintComponent(Graphics)} with the opportunity to compensate
	 * the imposed {@link #zoomFactor} by filling the enlarged canvas with white colour
	 * and drawing the diagrams in original size.
	 * @param g - a {@link Graphics2D} object as transformable drawing canvas
	 * @param compensateZoom - whether the imposed {@link #zoomFactor} is to be compensated
	 */
	public void paintComponent(Graphics g, boolean compensateZoom)
	// START KGU#624 2018-12-24: Enh. #655
	{
		this.paintComponent(g, compensateZoom, false, 0, 0);		
	}
	
	/**
	 * Specific variant of {@link #paintComponent(Graphics)} with the opportunity to compensate
	 * the imposed {@link #zoomFactor} by filling the enlarged canvas with white colour
	 * and drawing the diagrams in original size.
	 * @param g - a {@link Graphics2D} object as transformable drawing canvas
	 * @param compensateZoom - whether the imposed {@link #zoomFactor} is to be compensated
	 * @param onlySelected - if true then the drawing will be restricted to selected diagrams
	 * @param offsetX - a horizontal offset that is to be compensated (subtracted)
	 * @param offsetY - a vertical offset that is to be compensated (subtracted)
	 */
	public void paintComponent(Graphics g, boolean compensateZoom, boolean onlySelected, int offsetX, int offsetY)
	// END KGU#624 2018-12-24
	// END KGU#497 2018-03-19
	{
		
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Issues #518, #544, #557 - Workaround for heavy drawing contention
		// We will simply tell the Roots about the contention, so they may decide what to do (e.g. switch off highlighting)
		boolean fullRepaint = false;
		if (EventQueue.isDispatchThread()) {
			EventQueue evtqu = Toolkit.getDefaultToolkit().getSystemEventQueue();
			long contention = System.currentTimeMillis() - EventQueue.getMostRecentEventTime();
			//System.out.println("Contention: " + contention);
			if (contention > 500 && evtqu.peekEvent(MouseWheelEvent.MOUSE_WHEEL) != null && !wasContented) {
				fullRepaint = true;
				wasContented = true;
			}
			else if (contention <= 10 && wasContented) {
				fullRepaint = true;
				wasContented = false;
			}
		}
		// END KGU#502/KGU#524/KGU#553 2019-03-13: Issues #518, #544, #557 - reduce drawing contention
		
		// Region occupied by diagrams
		Dimension area = new Dimension(0, 0);
		super.paintComponent(g);
		if (diagrams != null)
		{
			// START KGU#497 2018-02-17: Enh. #512
			Graphics2D g2d = (Graphics2D) g;
			// START KGU#572 2018-09-09: Bugfix #508/#512 - ensure all diagrams have shape without rounding defects
			for (int d = 0; d < diagrams.size(); d++)
			{
				// START KGU#624 2018-12.24: Enh. #655
				//diagrams.get(d).root.prepareDraw(g2d);
				Diagram diagr = diagrams.get(d);
				// START KGU#911 2021-01-13: Enh. #910 New interpretation of group visible
				//if ((!onlySelected || this.diagramsSelected.contains(diagr)) && diagr.root != null) {
				if ((!onlySelected || this.diagramsSelected.contains(diagr))
						&& diagr.root != null
						&& this.isVisible(diagr)) {
				// END KGU#911 2021-01-13
					// If the diagram had already been drawn or prepared this will return immediately
					diagr.root.prepareDraw(g2d);
				}
				// END KGU#624 2018-12-24
			}
			// END KGU#572 2018-09-09
			// START KGU#497 2018-03-19: Enh. #512
			//g2d.scale(zoomFactor, zoomFactor);
			// END KGU#497 2018-02-17
			if (compensateZoom) {
				// In zoom-compensated drawing the background filled by super.paint(g)
				// is too small (virtually scaled down), therefore we must draw a
				// white rectangle covering the enlarged image area
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0,
						// START KGU#624 2018-12-24: Enh. #655
						//Math.round(this.getWidth() / zoomFactor),
						//Math.round(this.getHeight() / zoomFactor)
						Math.round(this.getWidth() / zoomFactor - offsetX),
						Math.round(this.getHeight() / zoomFactor - offsetY)
						// END KGU#624 2018-12-24
						);
			}
			else {
				g2d.scale(zoomFactor, zoomFactor);
			}
			// END KGU#497 2018-03-19

			// START KGU#502/KGU#524/KGU#553 2019-03-13: Issues #518, #544, #557 - reduce drawing contention
			Rectangle visibleRect = null;
			// In need of full repaint, we don't provide the actual visibility bounds
			if (!fullRepaint) {
				// START KGU#502/KGU#524/KGU#553: 2019-03-29: Issues #518, #544, #557
				/* The clip bounds of the graphics object are usually smaller than the entire
				 * viewport, due to window bit blit */
				//Rect visRect = new Rect(((JViewport)getParent()).getViewRect());
				//visRect = visRect.scale(1/zoomFactor);
				//visibleRect = visRect.getRectangle();
				// START KGU#706 2019-05-14: Bugfix #722 This failed with the graphics object of a buffered image
				//Rect clipRect = new Rect(g.getClipBounds());
				//visibleRect = clipRect.getRectangle();
				Rectangle clipBounds = g.getClipBounds();
				if (clipBounds != null) {
					visibleRect = new Rectangle(clipBounds);
				}
				// END KGU#706 2019-05-14
				// END KGU#502/KGU#524/KGU#553: 2019-03-29
			}
			// END KGU#502/KGU#524/KGU#557
				
			// START KGU#630 2019-01-09: Enh. #662/2 - preparations for group drawing
			if (drawGroups) {
				for (Group group: groups.values()) {
					group.draw(g2d, null);
				}
			}
			// END KGU#630 2019-01-19
			
//			System.out.println("Surface.paintComponent()");
			for (int d=0; d < diagrams.size(); d++)
			{
				Diagram diagram = diagrams.get(d);
				// START KGU#624 2018-12-24: Enh. #655
				if (onlySelected && !this.diagramsSelected.contains(diagram)) {
					continue;
				}
				// END KGU#624 2018-12-24
				// START KGU#911 2021-01-13: Enh. #910: New interpretation of diagram visibility
				if (!isVisible(diagram)) {
					continue;
				}
				// END KGU#911 2021-01-13
				
				Root root = diagram.root;
				Point point = diagram.point;

				// START KGU#624 2018-12-24: Enh. #655
				if (offsetX != 0 || offsetY != 0) {
					point = new Point(point.x - offsetX, point.y - offsetY);
				}
				// END KGU#624 2018-12-24
				// START KGU#88 2015-11-24
				//root.draw(g, point, this);
				Rect rect = root.draw(g2d, point, visibleRect, this, Element.DrawingContext.DC_ARRANGER, wasContented);
				if (diagram.isPinned)
				{
					if (pinIcon == null)
					{
						// START KGU#287 2016-11-01: Enh. #81 (DPI awareness)
						//pinIcon = new ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/pin_blue_14x20.png")).getImage();
						// START KGU#486 2018-02-10: Issue #4 - scalable icon, will now be somewhat smller but grow with the scale factor
						//pinIcon = IconLoader.getIconImage(getClass().getResource("/lu/fisch/structorizer/gui/icons/pin_blue_14x20.png")).getImage(); // NOI18N
						pinIcon = IconLoader.getIcon(99).getImage();
						// END KGU#486 2018-02-10
						// END KGU#287 2016-11-01
					}
					int x = rect.right - pinIcon.getWidth(null)*3/4;
					int y = rect.top - pinIcon.getHeight(null)/4;

					g2d.drawImage(pinIcon, x, y, null);
					//((Graphics2D)g).drawOval(rect.right - 15, rect.top + 5, 10, 10);
				}
				// END KGU#88 2015_11-24
				// START KGU#85 2017-10-23: Enh. #35 - take advantage of this opportunity to check scroll dimensions
				if (rect.right > area.width) area.width = rect.right;
				if (rect.bottom > area.height) area.height = rect.bottom;
				// END KGU#85 2017-10-23
			}
			// START KGU#624 2018-12-23: Enh. #655 - draw the dragArea
			if (dragArea != null) {
				g2d.drawRect(dragArea.x, dragArea.y, dragArea.width, dragArea.height);
			}
			// END KGU#624 2018-12-23
			// START KGU#497 2018-03-19: Enh. #512
			if (!compensateZoom) {
				g2d.scale(1/zoomFactor, 1/zoomFactor);
			}
			// END KGU#497 2018-03-19
		}
		// START KGU#645 2019-02-03: Issue #673 - drawing area should exceed the group bounds a little
		area.width += DEFAULT_GAP;
		area.height += DEFAULT_GAP;
		// END KGU#645 2019-02-03S
		// START KGU#85 2017-10-23: Enh. #35 - now make sure the scrolling area is up to date
		area.width = Math.round(Math.min(area.width, Short.MAX_VALUE) * this.zoomFactor);
		area.height = Math.round(Math.min(area.height, Short.MAX_VALUE) * this.zoomFactor);
		Dimension oldArea = this.getPreferredSize();
		// This check isn't only to improve performance but also to avoid endless recursion
		if (area.width != oldArea.width || area.height != oldArea.height) {
			this.setPreferredSize(area);
			this.revalidate();
		}
		// END KGU#85 2017-10-23
	}

	private void create()
	{
		new FileDrop(this, new FileDrop.Listener()
		{
			public void  filesDropped( java.io.File[] files )
			{
				loadFiles(files);
			}
		});
		// START KGU#623 2018-12-20: Enh. #654
		File cd = new File(Ini.getInstance().getProperty("arrangerDirectory", this.currentDirectory.getAbsolutePath()));
		if (cd.exists()) {
			currentDirectory = cd;
		}
		// END KGU#623 2018-12-20

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		// START KGU#85 2015-11-18: Enh. #35
		this.setAutoscrolls(true);
		// END KGU#85 2015-11-18

	}

	// START KGU#110 2015-12-17: Enh. #62 - offer an opportunity to save / load an arrangement
	/**
	 * Tries to load all nsd files contained in the array `files´ such that the diagrams
	 * may be held by this.
	 * @param files - array of File objects associated to NSD file names 
	 * @return number of successfully loaded files.
	 */
	public int loadFiles(File[] files)
	{
		// START KGU#624 2018-12-22: Enh. #655 - Clear the selection before
		this.unselectAll();
		// END KGU#624 2018-12-22
		// We try to load as many files of the list as possible and collect the error messages
		int nLoaded = 0;
		int toBeLoaded = files.length;
		// TODO delegate this to a worker thread with a large number (e.g. > 20) of files
		String troubles = "";
		Cursor origCursor = getCursor();
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			for (int i = 0; i < toBeLoaded; i++) {
				//String filename = files[i].toString();
				String errorMessage = loadFile(files[i]);
				// START KGU#153 2016-03-03: Bugfix #121 - a successful load must not add to the troubles text
				//if (!troubles.isEmpty()) { troubles += "\n"; }
				//troubles += "\"" + filename + "\": " + errorMessage;
				//System.err.println("Arranger failed to load \"" + filename + "\": " + troubles);
				if (!errorMessage.isEmpty()) {
					if (!troubles.isEmpty()) { troubles += "\n"; }
					String trouble = "\"" + files[i].getAbsolutePath() + "\": " + errorMessage;
					troubles += trouble;
					logger.log(Level.INFO, "Arranger failed to load " + trouble);	
				}
				else {
					nLoaded++;
				}
				// END KGU#153 2016-03-03
			}
		}
		finally {
			setCursor(origCursor);
		}
		if (!troubles.isEmpty()) {
			JOptionPane.showMessageDialog(this, troubles, msgFileLoadError.getText(), JOptionPane.ERROR_MESSAGE);
		}
		// START KGU#278 2016-10-11: Enh. #267
		if (nLoaded > 0) {
			notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		}
		// END KGU#278 2016-10-11
		// START KGU#624 2018-12-27: Enh. #655 - Notify about the selection change
		notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
		// END KGU#624 2018-12-27
		return nLoaded;
	}
	
	private String loadFile(File file)
	{
		// START KGU#679 2019-03-12: Enh. #698 several overloaded versions are no longer used.
//		return loadFile(file, null);
//	}

//	private String loadFile(File file, Point point)
//	// START KGU#289 2016-11-15: Enh. #290 (load arrangements from Structorizer)
//	{
//		// START KGU#316 2016-12-28: Enh. #318
//		//return loadFile(null, filename, point);
//		return loadFile(null, file, point, null, null);
//		// END KGU#316 2016-12-28
//	}
//
//	// START KGU#316 2016-12-28: Enh. #318 signature enhanced to keep track of unzipped files 
//	//private String loadFile(Mainform form, String filename, Point point)
//	// START KGU#626 2018-12-28: New argument group
//	//private String loadFile(Mainform form, String filename, Point point, String unzippedFrom)
//	// FIXME - there is no longer a constellation in which this can be called with form, zipFile, or group
//	private String loadFile(Mainform form, File file, Point point, File zipFile, Group group)
//	// END KGU#626 2018-12-28
//	// END KGU#316 2016-12-28
//	// END KGU#289 2016-11-15
//	{
		// FIXME: These variables just temporarily substitute the obsolete arguments
		Mainform form = null;
		Point point = null;
		File zipFile = null;
		Group group= null;
// END KGU#679 2019-03-12
		String errorMessage = "";
		String filename = file.getName();
		if (StructogramFilter.isNSD(filename))
		{
			// open an existing file
			NSDParser parser = new NSDParser();
			// START KGU#111 2015-12-17: Bugfix #63: We must now handle a possible exception
			try {
				// END KGU#111 2015-12-17
				// START KGU#363 2017-05-21: Issue #372 API change
				//Root root = parser.parse(f.toURI().toString());
				// START KGU#363 2018-09-11: Improvement for temporary legacy nsd files extracted from arrz files
				//Root root = parser.parse(f);
				Root root = parser.parse(file, zipFile);
				// END KGU#363 2018-09-11
				// END KGU#363 2017-05-21

				root.filename = file.getAbsolutePath();
// START KGU#679 2019-03-12: Enh. #698 - became dead code
//				// START KGU#316 2016-12-28: Enh. #318 Allow nsd files to "reside" in arrz files
//				if (zipFile != null) {
//					root.filename = zipFile.getAbsolutePath() + File.separator + file.getName();
//					root.shadowFilepath = file.getAbsolutePath();
//				}
//				// END KGU#316 2016-12-28
// END KGU#679 2019-03-12
				// START KGU#382 2017-04-15: Ensure highlighting mode has effect
				//root.highlightVars = Element.E_VARHIGHLIGHT;
				root.retrieveVarNames();	// Initialise the variable table, otherwise the highlighting won't work
				// END KGU#382 2017-04-15
				// START KGU#289 2016-11-15: Enh. #290 (load from Mainform)
				//addDiagram(root, point);
				// FIXME (2019-03-12): three of the four arguments are dead
				addDiagram(root, form, point, group);
				// END KGU#289 2016-11-15
				// START KGU#111 2015-12-17: Bugfix #63: We must now handle a possible exception
			}
			catch (Exception ex) {
				errorMessage = ex.getLocalizedMessage();
			}
			// END KGU#111 2015-12-17
		}
		// START KGU#289 2016-11-14: Enh. #289
		else if (ArrangerFilter.isArr(filename))
		{
			// FIXME KGU#679 2019-03-12: The first argument became superfluous
			errorMessage = loadArrFile(form, file);
		}
		// END KGU#289 2016-11-14
		return errorMessage;
	}

	/**
	 * Stores the current diagram arrangement (new with version 3.28-13: only selected diagrams)
	 * to a file.<br/>
	 * Depending on the choice of the user, this file will either be only a list of reference
	 * points and filenames (this way not being portable) or be a compressed archive containing
	 * the list file as well as the referenced NSD files will be produced such that it can be
	 * ported to a different location and extracted there.
	 *  
	 * @param initiator - the commanding GUI component
	 * @param group - possibly a group defining the set of diagrams to arrange
	 * @param goingToClose - indicates whether this call was initiated because tge application is going to close
	 * @return the resulting {@link Group} object (may not be {@code group}) if saving of the
	 * arrangement succeeded, otherwise null.
	 */
	public Group saveArrangement(Component initiator, Group group, boolean goingToClose)
	{
		boolean done = false;
		// START KGU#110 2016-06-29: Enh. #62
		boolean portable = false;
		String filename = null;
		String extension = "arr";
		// END KGU#110 2016-06-29
		// Ensure the diagrams themselves have been saved
		// START KGU#626 2019-01-02: Enh. #657
		Collection<Diagram> toArrange = this.diagramsSelected;
		String sourceDescription = Integer.toString(this.diagrams.size());
		// A selected group always overrides the selection in Arranger
		if (group != null) {
			toArrange = group.getDiagrams();
			// START KGU#631 2019-01-08: More sensible message content on saving a group
			sourceDescription = msgGroup.getText().replace("%", 
					group.getName().replace(Group.DEFAULT_GROUP_NAME, ArrangerIndex.msgDefaultGroupName.getText()));
			// END KGU#631 2019-01-08
			// If the group was the default group then we will anonymize it
			if (group.isDefaultGroup()) {
				group = null;
			}
		}
		else if (toArrange.isEmpty()) {
			toArrange = this.diagrams;	// save all if there is an empty selection
		}
		/* If group is null then the diagrams were individually selected, so check
		 * for coinciding groups and warn in this case */
		if (group == null) {
			Collection<Group> exactGroups = this.getGroupsFromCollection(toArrange, true);
			if (!exactGroups.isEmpty()) {
				// There are congruent groups, so propose to update one of them instead
				StringList groupDescriptions = new StringList();
				String[] options = new String[exactGroups.size()+2];
				int option = 0;
				for (Group grp: exactGroups) {
					File grpFile = grp.getFile();
					String path = "";
					if (grpFile != null) {
						path = ": " + grpFile.getAbsolutePath();
					}
					groupDescriptions.add(grp.getName() + path);
					options[option++] = grp.getName();
				}
				options[option] = msgSaveAsNewGroup.getText();
				options[option + 1] = Menu.lblCancel.getText();
				option = JOptionPane.showOptionDialog(initiator,
						Arranger.msgCongruentGroups.getText()
						.replace("%1", Integer.toString(toArrange.size()))
						.replace("%2", Integer.toString(exactGroups.size()))
						.replace("%3", groupDescriptions.concatenate("\n- "))
						.replace("%4", msgSelectGroup.getText()),
						this.msgSaveDialogTitle.getText(),
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[option]);
				if (option >= options.length-1 || option == JOptionPane.CLOSED_OPTION) {
					// Cancelled
					return null;
				}
				else if (option < exactGroups.size()) {
					group = groups.get(options[option]);
				}
			}
			if (group == null) {
				group = new Group("", toArrange);
			}
		}
		// We will care for the saving of the diagrams after the user has chosen where and how to save
		//boolean writeNow = this.saveDiagrams(initiator, toArrange, goingToClose, false);
		boolean writeNow = true;
		if (group.getFile() == null) {
			int answer = JOptionPane.CANCEL_OPTION;
			// The group has never been loaded from nor saved to file
		// END KGU#626 2019-01-02
			// START KGU#624 2018-12-22: Enh. #655
			// TODO Provide a choice table with checkboxes for every diagram (selected ones already checked)?
			// Let the user confirm this action for the selected set of diagrams
			StringList rootNames = listCollectedRoots(toArrange, false);
			int nSelected = rootNames.count();
			if (nSelected > Arranger.ROOT_LIST_LIMIT) {
				rootNames.remove(Arranger.ROOT_LIST_LIMIT, nSelected);
				rootNames.add("...");
			}
			String saveMessage = Arranger.msgConfirmMultiple.getText().
					replace("%1", Integer.toString(nSelected)).
					replace("%2", sourceDescription).
					replace("%3", rootNames.concatenate("\n- ")).
					replace("%4", msgSavePortable.getText());
			Object[] options = {lblSaveAsArrz.getText(), lblSaveAsArr.getText(), Menu.lblCancel.getText()};
			// END KGU#624 2018-12-22
			if ((answer = JOptionPane.showOptionDialog(initiator,
					saveMessage, this.msgSaveDialogTitle.getText(),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null,
					options, options[0])) == JOptionPane.CANCEL_OPTION) {
				// Cancel the saving
				return null;
			}
			// Let's select path and name for the list / archive file
			JFileChooser dlgSave = new JFileChooser(currentDirectory);
			dlgSave.setDialogTitle(msgSaveDialogTitle.getText());
			// START KGU#110 2016-06-29: Enh. #62
			//dlgSave.addChoosableFileFilter(new ArrFilter());
			FileFilter filter = null;
			if (answer == JOptionPane.OK_OPTION)
			{
				filter = new ArrZipFilter();
				dlgSave.addChoosableFileFilter(filter);
				dlgSave.setFileFilter(filter);				
				portable = true;
				extension += "z";
			}
			else
			{
				filter = new ArrFilter();
				dlgSave.addChoosableFileFilter(filter);
				dlgSave.setFileFilter(filter);
			}
			// END KGU#110 2016-06-29
			// START KGU#626 2019-01-02: Enh. #657
			//dlgSave.setCurrentDirectory(currentDirectory);
			File proposedFile = null;
			if (!group.getName().isEmpty()) {
				String proposedName = group.proposeFileName();
				proposedFile = new File(proposedName);
				if (!filter.accept(proposedFile)) {
					proposedFile = new File(proposedName += "." + extension);
				}
				dlgSave.setSelectedFile(proposedFile);
			}
			if (proposedFile == null || !proposedFile.isAbsolute()) {
				dlgSave.setCurrentDirectory(currentDirectory);
			}
			// END KGU#626 2019-01-02
			int result = dlgSave.showSaveDialog(initiator);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				currentDirectory = dlgSave.getCurrentDirectory();
				while (currentDirectory != null && !currentDirectory.isDirectory())
				{
					currentDirectory = currentDirectory.getParentFile();
				}
				// correct the filename if necessary
				filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
				// START KGU#110 2016-06-29: Enh. #62
				//if (!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".arr"))
				//{
				//	filename+=".arr";
				//}
				//done = saveArrangement(frame, filename + "");
				if (filename.substring(filename.length()-extension.length()-1).equalsIgnoreCase("."+extension))
				{
					filename = filename.substring(0, filename.length()-extension.length()-1);
				}
				// START KGU#385 2017-04-22: Enh. #62
				//done = saveArrangement(frame, filename, extension, portable);
				writeNow = true;
				File f = new File(filename + "." + extension);
				if (f.exists())
				{
					// Ask the user whether they want to overwrite the already existing file
					int res = JOptionPane.showConfirmDialog(
							initiator,
							msgOverwriteFile.getText().replace("%", f.getAbsolutePath()),
							msgConfirmOverwrite.getText(),
							JOptionPane.YES_NO_OPTION);
					writeNow = (res == JOptionPane.YES_OPTION);
				}
				// START KGU#626 2019-01-02: Enh. #657
				//if (writeNow) {
				//	done = saveArrangement(initiator, filename, extension, portable);
				//}
				// END KGU#626 2019-01-02
				// END KGU#385 2017-04-22
				// END KGU#110 2016-06-29
			}
			// START KGU#644 2019-02-02: Bugfix #672: We must not go on if the FileChooser was cancelled
			else {
				writeNow = false;
			}
			// END KGU#644 2019-02-02
		// START KGU#626 2019-01-02: Enh. #657
		}
		else {
			// The group originates from or has been saved to a file
			File file = group.getFile();
			File arrzFile = group.getArrzFile(false);
			if (arrzFile != null) {
				portable = true;
				extension += "z";
				file = arrzFile;
			}
			filename = file.getAbsolutePath();
			int dotPos = filename.length()-extension.length() - 1;
			if (filename.substring(dotPos).equalsIgnoreCase("."+extension))
			{
				filename = filename.substring(0, dotPos);
			}
			if (group.hasChanged() && writeNow && lu.fisch.structorizer.gui.Diagram.isInSerialMode()) {
				if (!(goingToClose && Element.E_AUTO_SAVE_ON_CLOSE)){
					int answer =lu.fisch.structorizer.gui.Diagram.requestSaveDecision(
							msgSaveGroupChanges.getText().replace("%", group.getName()),
							initiator, SerialDecisionAspect.SERIAL_GROUP_SAVE);
					writeNow = answer == 0 || answer == 2;
				}
			}
		}
		/* Care for the chance to save diagram changes now (without asking for portable archives
		 * because otherwise we might copy an obsolete file into the archive). In case of new
		 * diagrams, the user must be asked, anyway, they will have to commit the proposed file name
		 * and location. For an arrangement list, it's not so important whether or not the content
		 * of the file is up to date, so the user will be left the choice.
		 */
		if (writeNow && this.saveDiagrams(initiator, toArrange, goingToClose, portable, portable)) {
			if (group.hasChanged()) {
				done = saveArrangement(initiator, filename, extension, portable, group);
				if (done && group.getName().isEmpty()) {
					// Replace the temporary group by a permament group
					File groupFile = new File(filename + "." + extension);
					String groupName = (groupFile).getName();
					if (group.getFile() != null) {
						groupFile = group.getFile();
					}
					group = this.makeGroup(groupName, null, group.getSortedRoots(), false, groupFile);
				}
			}
		}
		// END KGU#626 2019-01-02
		// START KGU#679 2019-03-12: Enh. #698 - Inform associated Mainforms about the new file
		File file = group.getArrzFile(false);
		if (done && !goingToClose && (file != null || (file = group.getFile()) != null)) {
			for (Diagram diagr: group.getDiagrams()) {
				if (diagr.mainform != null) {
					diagr.mainform.addRecentFile(file);
				}
			}
		}
		// END KGU#679 2019-03-13
		// START KGU#746 2019-10-05: The status change of the group wasn't shown in Arranger index
		// START KGU#408 2021-03-01: This is a low-level change
		//this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		this.notifyChangeListeners(IRoutinePoolListener.RPC_STATUS_CHANGED);
		// END KGU#408 2021-03-01
		// END KGU#746 2019-10-05
		return done ? group : null;
	}

	/**
	 * Stores the group specified by {@code groupName} (if existent) as diagram arrangement
	 * to either the associated or a new file.<br/>
	 * Depending on the group type or the choice of the user, this file will either be only a
	 * list of reference points and filenames (this way not being portable) or be a compressed
	 * archive containing the list file as well as the referenced NSD files will be produced
	 * such that it can be ported to a different location and extracted there.
	 * @param groupName - name of the group to be saved.
	 * @param initiator - the commanding GUI component
	 * @return status flag (true iff the group was found and the saving succeeded without error) 
	 */
	public boolean saveArrangement(String groupName, Component initiator)
	{
		boolean done = false;
		Group group = groups.get(groupName);
		if (group != null) {
			done = (saveArrangement(initiator, group, false) != null);
		}
		return done;
	}
	
	
	/**
	 * Stores the currently selected diagram arrangement to a file.
	 * If {@code portable} is false, this file will only contain a list of points and filenames.
	 * Otherwise a compressed archive containing the list file as well as the referenced
	 * NSD files will be produced such that it can be ported to a different location and
	 * extracted there.
	 *
	 * @param initiator - the commanding GUI component
	 * @param filename - the base path of the selected file (without extension)
	 * @param extension - the file name extension
	 * @param portable - whether a portable zip file is to be created
	 * @param group - the group this arrangement is to be associated to
	 * @return status flag (true iff the saving succeeded without error) 
	 */
	// START KGU#110 2016-06-29. Enh. #62
	//public boolean saveArrangement(Frame frame, String filename)
	private boolean saveArrangement(Component initiator, String filename, String extension, boolean portable, Group group)
	// END KGU#110 2016-06-29
	{
		boolean done = false;
		String outFilename = filename + "." + extension;		// Name of the actually written file
		String tmpFilename = null;

		// Find a suited temporary directory to store the output file(s) if needed
		File tempDir = findTempDir();
		if (tempDir == null)
		{
			File dir = new File(".");
			if (dir.isFile())
			{
				tempDir = dir.getParentFile();
			}
		}
		LinkedList<Root> savedRoots = null;
		enableNotification(false);
		try
		{
			// Prepare to save the arr file (if portable is false then this is the outfile)
			String arrFilename = outFilename;
			File file = new File(outFilename);
			// START KGU#110 2016-06-29: Enh. #62
			// Check whether the target file already exists
			//boolean fileExisted = file.exits();
			//if (fileExisted)
			if (portable)
			{
				// name for the arr file to be zipped into the target file
				arrFilename = tempDir + File.separator + (new File(filename)).getName() + ".arr";
				// START KGU#650 2019-02-11: Issue #677 save all virgin group members to the temp dir
				savedRoots = saveVirginRootsToTempDir(group, tempDir);
				// END KGU#650 2019-02-11
			}
			else if (file.exists())
			// END KGU#110 2016-06-29
			{
				// name for a temporary arr file
				arrFilename = tempDir + File.separator + "Temp." + extension;
				tmpFilename = arrFilename;
			}
			
			// START KGU#679 2019-03-11: Issue #698 Delegated to Archivar
//			// Now actually save the arr file
//			saveArrFile(group, arrFilename, portable);
//
//			// START KGU#110 2016-06-29: Enh. #62
//			// Now zip all files together if a portable file is requested
//			if (portable)
//			{
//				// Create a zip file from the nsd files and the arr file
//				// (returns the name of the zip file if it was placed in
//				// a temp directory, otherwise null).
//				tmpFilename = zipAllFiles(group, outFilename, arrFilename, tempDir);				
//			}
			// We want to preserve the original drawing order, so we must go the long way...
			Set<Diagram> groupMembers = group.getDiagrams();
			List<ArchiveRecord> itemsToSave = new LinkedList<ArchiveRecord>();
			for (Diagram diagr: this.diagrams)
			{
				if (groupMembers.contains(diagr))
				{
					itemsToSave.add(diagr);
				}
			}
			// Enh. #662/4
			Point offset = null;
			if (Arranger.A_STORE_RELATIVE_COORDS) {
				Rectangle bounds = group.getBounds(true);
				if (bounds != null) {	// Could happen if the group is empty
					offset = new Point(bounds.x - DEFAULT_GAP, bounds.y - DEFAULT_GAP);
				}
			}
			Archivar archivar = new Archivar();
			// START KGU#752 2019-10-14: Bugfix #764 - the update of the arr file if portable==false was averted
			//tmpFilename = archivar.saveArrangement(itemsToSave, arrFilename, (portable ? file : null), (portable ? tempDir : null), offset, null);
			String tmpArrzName = archivar.saveArrangement(itemsToSave, arrFilename, (portable ? file : null), (portable ? tempDir : null), offset, null);
			if (portable) {
				tmpFilename = tmpArrzName;
			}
			// END KGU#752 2019-10-14
			// END KGU#679 2019-03-11

			// START KGU#682 2019-03-15: Bugfix #703 - group change status must get a chance to be reset.
			for (Diagram diagr: groupMembers) {
				diagr.wasMoved = false;
			}
			// END KGU#682 2019-03-15
			// If the target file had existed then replace it by the output file after having created a backup
			if (tmpFilename != null)
			{
				File backUp = new File(outFilename + ".bak");
				if (backUp.exists())
				{
					backUp.delete();
				}
				// START KGU#717 2019-7-31: Bugfix #731 - didn't cope among different file systems (Linux)
				//file.renameTo(backUp);
				boolean moved = Archivar.renameTo(file, backUp);
				// END KGU#717 2019-07-31
				file = new File(outFilename);
				File tmpFile = new File(tmpFilename);
				// START KGU#717 2019-7-31: Bugfix #731 - didn't cope among different file systems (Linux)
				//tmpFile.renameTo(file);
				moved = moved && Archivar.renameTo(tmpFile, file);
				// END KGU#717 2019-07-31
				// START KGU#626 2019-01-02: Enh. #657
				if (portable) {
					group.setFile(new File(arrFilename), file, false);
				}
				else {
					group.setFile(new File(outFilename), null, false);					
				}
				// END KGU#626 2019-01-02
			}
			// START KGU#626 2019-01-02: Enh. #657
			else if (portable) {
				group.setFile(new File(arrFilename), new File(outFilename), false);
			}
			else {
				group.setFile(new File(outFilename), null, false);
			}
			// END KGU#626 2019-01-02
			// START KGU#650 2019-02-11: Issue #677 - let the archived virgin diagrams reside in the archive
			// START KGU#680 2019-03-11: Bugfix #699 - all files in the archive must be ensured their virtual path is set properly 
			//if (savedVirginRoots != null) {
			//	for (Root root: savedVirginRoots) {
			//		File path = root.getFile();
			//		if (path != null) {
			//			root.shadowFilepath = path.getAbsolutePath();
			//			root.filename = outFilename + File.separator + path.getName();
			//		}
			//	}
			//}
			// START KGU#680 2019-03-11: Bugfix #699 The saved diagrams must reflect that they reside in the archive, possibly they must be copied
			if (portable) {
				StringList sharedDiagrams = new StringList();
				for (Diagram diagr: group.getDiagrams().toArray(new Diagram[group.size()])) {
					// START KGU#683 2019-03-29: Bugfix #699 - No measures for diagrams that aren't shared by other groups
					/* Caution: A temporary group (empty name) is not listed among the
					 * group names of a diagram! So a mere group counting approach
					 * goes wrong. Only in case the name of the group to be saved is
					 * the name of the actual and only group of the diagram or if the diagram
					 * is only member of the default group and the group to be saved is a
					 * temporary group (empty name, in this case the diagram will simply be
					 * moved from the default group to the new group in makeGroup()) we may
					 * be sure to deal with the sole owning group.
					 */
					StringList groupNames = new StringList(diagr.getGroupNames());
					if (groupNames.count() <= 1 && (groupNames.contains(group.getName()) ||
							groupNames.contains(Group.DEFAULT_GROUP_NAME) && group.getName().isEmpty())) {
						continue;
					}
					// END KGU#683 2019-03-29
					if (diagr.root.shadowFilepath != null && !diagr.root.getFile().getAbsolutePath().equals(outFilename)) {
						// A diagram residing in another archive must be copied now, it cannot be shared.
						Root copiedRoot = (Root)diagr.root.copy();
						copiedRoot.retrieveVarNames();	// Ensures that syntax highlighting will work
						//diagr.point.translate(2 * DEFAULT_GAP, 2 * DEFAULT_GAP);	// The copy must have the same place (this was the archived one!)
						// START KGU#718 2019-07-31: Bugfix #732: we must not share the point!
						//Diagram diagram = new Diagram(copiedRoot, diagr.point);
						Diagram diagram = new Diagram(copiedRoot, new Point(diagr.point));
						// END KGU#718 2019-07-31
						diagrams.add(diagram);
						rootMap.put(copiedRoot, diagram);
						String rootName = copiedRoot.getMethodName();
						addToNameMap(rootName, diagram);
						//printNameMap(1228);	// FIXME DEBUG
						// In theory, the diagram can't get orphaned here.
						group.removeDiagram(diagr);
						group.addDiagram(diagram);
						copiedRoot.addUpdater(this);
						// FIXME: change selection?
					}
					// In the other cases the diagram already resides in the archive or can be shared
					else if (diagr.root.shadowFilepath == null) {
						sharedDiagrams.addOrdered(diagr.root.getSignatureString(false, false) + ": " +
								groupNames.concatenate(", ").replace(Group.DEFAULT_GROUP_NAME, ArrangerIndex.msgDefaultGroupName.getText()));
						if (savedRoots.contains(diagr.root)) {
							for (String grName: diagr.getGroupNames()) {
								Group gr = this.groups.get(grName);
								if (gr != null && gr.getFile() != null && gr.getArrzFile(false) == null) {
									// The arrangement list file is out of date (new diagram file path).
									gr.membersChanged = true;
								}
							}
						}
					}
				}
				// Ensure all new copies are saved somewhere such that they will get a virtual path later
				this.saveVirginRootsToTempDir(group, tempDir);
				// At this point there should not be any unsaved Roots and no Roots residing in a different archive anymore
				for (Root root: group.getSortedRoots()) {
					String shadowPath = root.shadowFilepath;
					// ... so a Root without shadowPath obviously needs the virtual path inside the archive now
					if (shadowPath == null) {
						root.shadowFilepath = root.filename;
						root.filename = outFilename + File.separator + root.getFile().getName();
					}
				}
				if (sharedDiagrams.count() > 0) {
					JOptionPane.showMessageDialog(initiator,
							msgSharedDiagrams.getText().replace("%1", sharedDiagrams.concatenate("\n - ")).replace("%2", (new File(outFilename).getName())),
							this.msgSaveDialogTitle.getText(), JOptionPane.WARNING_MESSAGE);
				}
				// START KGU#682 2019-03-15: Issue #704 group change status must be reset.
				group.membersChanged = false;
				// END KGU#682 2019-03-15
			}
			// END KGU#680 2019-03-11
			// END KGU#650 2019-02-11
			done = true;
		}
		catch (Exception ex)
		{
			StringList causes = new StringList();
			Throwable cause = ex.getCause();
			while (cause != null) {
				causes.add(cause.getMessage());
				cause = cause.getCause();
			}
			
			JOptionPane.showMessageDialog(initiator, msgSaveError.getText() + " " + ex.getMessage() + "!" + (!causes.isEmpty() ? "\n" + causes.getText() : ""),
					"Error", JOptionPane.ERROR_MESSAGE, null);
		}
		finally {
			enableNotification(true);
		}
		return done;
	}

	// START KGU#650 2019-02-11: Issue #677 - Inconveniences on saving arrangement archives
	/** Tries to save all unsaved group members in the given temporary directory, ensuring
	 * unique names. Raises a message box with the names of all diagrams the saving attempt
	 * for which failed.
	 * @param group - the {@link Group} the diagrams of which are to be saved if not already associated to a file
	 * @param tempDir - the target directory
	 * @return a list of the {@link Root} objects that were successfully saved
	 */
	private LinkedList<Root> saveVirginRootsToTempDir(Group group, File tempDir) {
		LinkedList<Root> savedRoots = new LinkedList<Root>();
		StringList unsaved = new StringList();
		StringList errors = new StringList();
		for (Diagram diagr: group.getDiagrams()) {
			// START KGU#749 2019-10-13: Bugfix #763 - Diagrams with inaccessible files must also be saved
			//if (diagr.root.getFile() == null) {
			File assocFile = diagr.root.getFile();
			// START KGU#749 2019-10-15: Bugfix #763 We must make sure that there is no obsolete shadow file path
			// If there is a valid shadow file path then don't save it with a new file but wipe the file name
			if (assocFile == null && diagr.root.shadowFilepath != null) {
				assocFile = new File(diagr.root.shadowFilepath);
				diagr.root.filename = assocFile.getAbsolutePath();
				diagr.root.shadowFilepath = null;
				if (assocFile.canRead()) {
					// Formally this is a change as if the diagram had been saved again
					savedRoots.add(diagr.root);
				}
			}
			// END KGU#749 2019-10-15
			if (assocFile == null || !assocFile.canRead()) {
			// END KGU#749 2019-10-13
				String filename = tempDir.getAbsolutePath() + File.separator + diagr.root.proposeFileName();
				File file = new File(filename + ".nsd");
				int count = 1;
				while (file.exists()) {
					file = new File(filename + "_" + count++ + ".nsd");
				}
				filename = file.getAbsolutePath();
				Writer out = null;
				try {
					FileOutputStream fos = new FileOutputStream(filename);
					out = new OutputStreamWriter(fos, "UTF-8");
					XmlGenerator xmlgen = new XmlGenerator();
					out.write(xmlgen.generateCode(diagr.root,"\t", true));
					diagr.root.filename = filename;
					diagr.root.rememberSaved();
					savedRoots.add(diagr.root);
					if (diagr.mainform != null) {
						diagr.mainform.doButtons();
					}
				} catch (IOException ex) {
					// START KGU#749 2019-10-13: Bugfix #763
					//unsaved.add(diagr.root.proposeFileName());
					if (assocFile != null) {
						unsaved.add(assocFile.getAbsolutePath());
					}
					else {
						unsaved.add(diagr.root.proposeFileName());
					}
					// END KGU#749 2019-10-13
					errors.add(ex.toString());
				}
				finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {}
					}
				}

			}
		}
		if (!unsaved.isEmpty()) {
			JOptionPane.showMessageDialog(this.getParent(), 
					this.msgUnsavedDiagrams.getText() + "\n" + unsaved.getText() + "\n\n" + errors.getText(),
					this.msgSaveError.getText(), JOptionPane.WARNING_MESSAGE);
		}
		return savedRoots;
	}
	// END KGU#650 2019-02-11

// START KGU#679 2019-03-13: Issue #698 - methods have been replaced by Archivar
//	/**
//	 * Creates the Arranger file with path {@code arrFilename} for all diagrams held
//	 * by {@code group}. If the Arranger file is used for an arrangement archive then
//	 * the paths are to be shortened to the pure names.
//	 * @param group - the {@link Group} holding the diagrams belonging to the arrangement
//	 * @param arrFilename - target path of the Arranger file
//	 * @param pureNames - if true then only the pure file names (instead of the entire
//	 * paths) will be listed in the arr file.
//	 * @throws IOException
//	 */
//	private void saveArrFile(Group group, String arrFilename, boolean pureNames) throws IOException
//	{
//		int offsetX = 0, offsetY = 0;
//		// START KGU#630 2019-01-13: Enh. #662/4
//		if (Arranger.A_STORE_RELATIVE_COORDS) {
//			Rectangle bounds = group.getBounds(true);
//			if (bounds != null) {	// Could happen if the group is empty
//				offsetX = DEFAULT_GAP - bounds.x;	// Will usually be negative
//				offsetY = DEFAULT_GAP - bounds.y;	// Will usually be negative
//			}
//		}
//		// END KGU#630 2019-01-13
//		// We want to preserve the original drawing order, so we must go the long way...
//		Set<Diagram> groupMembers = group.getDiagrams();
//		FileOutputStream fos = new FileOutputStream(arrFilename);
//		Writer out = new OutputStreamWriter(fos, "UTF8");
//		for (Diagram diagr: this.diagrams)
//		{
//			String path = "";
//			// KGU#110 2016-07-01: Bugfix #62 - don't include diagrams without file
//			if (groupMembers.contains(diagr) && !(path = diagr.root.getPath()).isEmpty())
//			{
//				// START KGU#630 2019-01-13: Enh. #662/4
//				out.write(Integer.toString(diagr.point.x + offsetX) + ",");
//				out.write(Integer.toString(diagr.point.y + offsetY) + ",");
//				// END KGU#630 2019-01-13
//				StringList entry = new StringList();
//				if (pureNames)
//				{
//					File nsdFile = new File(path);
//					path = nsdFile.getName();	// Only last part of path
//				}
//				entry.add(path);
//				out.write(entry.getCommaText()+'\n');
//				// START KGU#626 2019-01-05: Enh. #657 - reset the moved flag
//				diagr.wasMoved = false;
//				// END KGU#626 2019-01-05
//			}
//		}
//
//		out.close();
//	}
//
//	/**
//	 * Compresses the diagrams of the given {@link Group} {@code group} and the describing
//	 * arr file ({@code arrFilename}) into file {@code zipFilename} (which is essentially an ordinary
//	 * zip file but named as given).
//	 * @param group - the {@link Group} defining what diagrams are members of the arrangement
//	 * @param zipFilename - path of the arrz file (zip file) to be created
//	 * @param arrFilename - path of the existing arr file holding the positions
//	 * @param tmpDir - a temporary directory for the case the target file already exists
//	 * @return the path of the created temporary file if the target file {@code zipFilename} had
//	 * existed (otherwise null)
//	 */
//	private String zipAllFiles(Group group, String zipFilename, String arrFilename, File tmpDir) throws IOException
//	{
//		final int BUFSIZE = 2048;
//
//		String tmpFilename = zipFilename + "";
//		// set up the file (and check whether it has existed already)
//		File file = new File(zipFilename);
//		boolean fileExisted = file.exists(); 
//		if (fileExisted)
//		{
//			tmpFilename = tmpDir.getAbsolutePath() + File.separator + "Arranger.zip";
//		}
//
//		BufferedInputStream origin = null;
//		FileOutputStream dest = new	FileOutputStream(tmpFilename);
//		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
//		//out.setMethod(ZipOutputStream.DEFLATED);
//		byte buffer[] = new byte[BUFSIZE];
//		StringList filePaths = new StringList();
//		// Add the diagram file names
//		for (Diagram diagr: group.getDiagrams())
//		{
//			// START KGU#316 2017-04-22: Enh. #318: Files might be zipped
//			//String path = diagr.root.getPath();
//			String path = diagr.root.shadowFilepath;
//			if (path == null) {
//				path = diagr.root.getPath();
//			}
//			// END KGU#316 2017-04-22
//			if (!path.isEmpty())
//			{
//				filePaths.add(path);
//			}
//		}
//		filePaths.add(arrFilename);
//
//		int count;
//		for (int i = 0; i < filePaths.count(); i++)
//		{
//			FileInputStream fis = new FileInputStream(filePaths.get(i));
//			origin = new BufferedInputStream(fis, BUFSIZE);
//			ZipEntry entry = new ZipEntry(new File(filePaths.get(i)).getName());
//			// START KGU 2018-09-12: Preserve time attributes if possible
//			Path srcPath = (new File(filePaths.get(i))).toPath();
//			try {
//				BasicFileAttributes attrs = Files.readAttributes(srcPath, BasicFileAttributes.class);
//				FileTime modTime = attrs.lastModifiedTime();
//				FileTime creTime = attrs.creationTime();
//				if (modTime.toMillis() > 0) {
//					entry.setLastModifiedTime(modTime);
//				}
//				if (creTime.toMillis() > 0) {
//					entry.setCreationTime(creTime);
//				}
//			} catch (IOException e) {}
//			// END KGU 2018-09-12
//			out.putNextEntry(entry);
//			while((count = origin.read(buffer, 0, BUFSIZE)) != -1)
//			{
//				out.write(buffer, 0, count);
//			}
//			origin.close();
//		}
//
//		out.close();
//		if (!fileExisted)
//		{
//			tmpFilename = null;
//		}
//		return tmpFilename;
//	}
// END KGU#679 2019-03-12

	/**
	 * Action method for the "Load List" button of the Arranger: Attempts to load the
	 * specified (portable) arrangement file.
	 * @param frame - the owning frame object. 
	 * @return true iff the loading succeeded (will raise an error message else).
	 */
	public boolean loadArrangement(Frame frame)
	{
		boolean done = false;
		// Ensure the previous diagrams themselves are saved
		if (this.saveDiagrams(null))
		{
			// Let's select path and name for the list / archive file
			JFileChooser dlgOpen = new JFileChooser(currentDirectory);
			dlgOpen.setDialogTitle(msgLoadDialogTitle.getText());
			// START KGU#100 2016-01-15: Enh. #62 - select the provided filter
			//dlgOpen.addChoosableFileFilter(new ArrFilter());
			// START KGU#802 2020-02-16: Issue #815
			//ArrFilter filter = new ArrFilter();
			ArrangerFilter filter = new ArrangerFilter();
			// END KGU#802 2020-02-16
			dlgOpen.addChoosableFileFilter(filter);
			dlgOpen.setFileFilter(filter);
			// END KGU#110 2016-01-15: Enh. #62
			// START KGU#802 2020-02-16: Issue #815 No longer the favourite filter
			dlgOpen.addChoosableFileFilter(new ArrFilter());
			// END KGU#802 2020-02-16
			// START KGU#110 2016-07-01: Enh. #62 - Add the zipped filter
			dlgOpen.addChoosableFileFilter(new ArrZipFilter());
			// END KGU#110 2016-07-01

			dlgOpen.setCurrentDirectory(currentDirectory);

			int result = dlgOpen.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				currentDirectory = dlgOpen.getCurrentDirectory();
				while (currentDirectory != null && !currentDirectory.isDirectory())
				{
					currentDirectory = currentDirectory.getParentFile();
				}
				File oldCurrDir = currentDirectory;
				// correct the filename if necessary
				File file = dlgOpen.getSelectedFile();

				// START KGU#316 2016-12-28: Enh. #318
				File unzippedFrom = null;
				// END KGU#316 2016-12-28
				// START KGU#110 2016-07-01: Enh. # 62 - unpack a zip file first
				if (ArrZipFilter.isArr(file.getName()))
				{
					File extractTo = null;
					dlgOpen.setDialogTitle(msgExtractDialogTitle.getText());
					dlgOpen.resetChoosableFileFilters();
					dlgOpen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					dlgOpen.setAcceptAllFileFilterUsed(false);
					if (dlgOpen.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
					{
						//extractTo = dlgOpen.getCurrentDirectory().getAbsolutePath();
						extractTo = dlgOpen.getSelectedFile();
					}
					else {
						unzippedFrom = file;
					}
					file = unzipArrangement(file, extractTo);
					if (file != null)
					{
						currentDirectory = file;
						while (currentDirectory != null && !currentDirectory.isDirectory())
						{
							currentDirectory = currentDirectory.getParentFile();
						}
					}
				}
				// END KGU#110 2016-07-01
				// START KGU#316 2016-12-28: Enh. #318
				//done = loadArrangement(frame, filename);
				done = loadArrangement(frame, file, unzippedFrom);
				// END KGU#316 2016-12-28
				currentDirectory = oldCurrDir;
				// START KGU#679 2019-03-12: Enh. #698 Ensure that loaded arrangements occur in the recent file lists
				if (done) {
					for (Mainform form: this.activeMainforms()) {
						form.addRecentFile(file);
					}
				}
				// END KGU#679 2019-03-12
			}
		}
		return done;
	}

	/**
	 * Restores the diagram arrangement stored in arr file `filename´ if possible
	 * (i.e. given the referred nsd file paths exist and the nsd files may be parsed)
	 * @param frame - owning frame component
	 * @param arrFile - the arrangement list file to be reloaded
	 * @param unzippedFrom - the arrangement archive file if shadowed to temporary directory, null otherwise
	 * @return true if at lest some of the referred diagrams could be arranged again.
	 */
	// START KGU#316 2016-12-28: Enh. #318 API change to support shadow paths in unzipped Roots
	//public boolean loadArrangement(Frame frame, String filename)
	public boolean loadArrangement(Frame frame, File arrFile, File unzippedFrom)
	// END KGU#316 2016-12-28
	{
		boolean done = false;
		// START KGU#278 2016-10-11: Enh. #267
		int nLoaded = 0;
		// END KGU#278 2016-10-11
		
		// START KGU#624 2018-12-22: Enh. #655 clear the selection such that only the loaded files will be selected
		this.unselectAll();
		// END KGU#624 2018-12-22
		
		// START KGU#626 2018-12-28: Enh. #657
		Group group = null;
		// END KGU#626 2018-12-28 

		String errorMessage = null;
		// START KGU#316 2016-12-28: Enh. #318 don't get confused by the loading of some files
		String prevCurDirPath = this.currentDirectory.getAbsolutePath();
		// END KGU#316 2016-12-28
		// START KGU#901 2020-12-29: Issue #901 WAIT_CURSOR on time-consuming actions
		Cursor origCursor = getCursor();
		// END KGU#901 2020-12-29
		try
		{
			// set up the file
			
			// START KGU#626 2018-12-28: Enh. #657 - group management
			String groupName = arrFile.getName();
			if (unzippedFrom != null) {
				groupName = unzippedFrom.getName();
			}
			if (groups.containsKey(groupName)) {
				// Warn and ask for confirmation if this group has already been loaded
				Group oldGroup = groups.get(groupName);
				File oldFile = null;
				if ((oldFile = oldGroup.getArrzFile(false)) != null && unzippedFrom != null && oldFile.compareTo(unzippedFrom) == 0 ||
						(oldFile = oldGroup.getFile()) != null && oldFile.compareTo(arrFile) == 0) {
					if (JOptionPane.showConfirmDialog(frame,
							msgArrangementAlreadyLoaded.getText()
							.replace("%1", oldFile.getAbsolutePath()).replace("%2", groupName),
							this.msgLoadDialogTitle.getText(),
							JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
						// User doesn't confirm reloading, so stop it
						JOptionPane.showMessageDialog(frame, msgArrangementNotLoaded.getText());
						return true;
					};
				}
				int trial = 1;
				// We care for a unique group name by appending a parenthesised number
				while (groups.containsKey(groupName + "(" + trial +")")) trial++;
				groupName = groupName + "(" + trial +")";
			}
			// Now we prepare the group context before the actual diagrams are loaded
			group = new Group(groupName, unzippedFrom == null ? arrFile.getAbsolutePath() : unzippedFrom.getAbsolutePath() + File.separator + arrFile.getName());
			groups.put(groupName, group);
			// END KGU#626 2018-12-28
		
// START KGU#679 2019-03-10: Enh. #698 Decomposition 
//			Scanner in = new Scanner(arrFile, "UTF8");
//			while (in.hasNextLine())
//			{
//				String line = in.nextLine();
//				StringList fields = StringList.explode(line, ",");
//				if (fields.count() >= 3)
//				{
//					Point point = new Point();
//					point.x = Integer.parseInt(fields.get(0));
//					point.y = Integer.parseInt(fields.get(1));
//					String nsdFileName = fields.get(2);
//					if (nsdFileName.startsWith("\""))
//						nsdFileName = nsdFileName.substring(1);
//					if (nsdFileName.endsWith("\""))
//						nsdFileName = nsdFileName.substring(0, nsdFileName.length() - 1);
//					File nsdFile = new File(nsdFileName);
//					if (!nsdFile.exists() && !nsdFile.isAbsolute())
//					{
//						// START KGU#316 2016-12-28: Enh. #318 don't get confused by the loading of some files
//						//nsdFileName = currentDirectory.getAbsolutePath() + File.separator + nsdFileName;
//						nsdFile = new File(prevCurDirPath + File.separator + nsdFileName);
//						// END KGU#316 2016-12-28
//					}
//					// START KGU#289 2016-11-15: Enh. #290 (Arrangements loaded from Mainform)
//					//String trouble = loadFile(nsdFileName, point);
//					String trouble = loadFile((frame instanceof Mainform) ? (Mainform)frame : null,
//							nsdFile, point, unzippedFrom, group);
//					// END KGU#289 2016-11-15
//					// START KGU#625 2018-12-22: Bugfix #656 - It might be that the arr file refers to virtual arrz paths
//					if (!trouble.isEmpty() && !nsdFile.exists() && unzippedFrom == null && nsdFileName.contains(".arrz")) {
//						try {
//							// Might be a path into an arrz file from which the referred diagram had originally been loaded
//							File arrzFile = nsdFile.getParentFile();
//							String pureName = nsdFile.getName();
//							if (arrzFile.exists()) {
//								File extractedArrFile = unzipArrangement(arrzFile, null);
//								if (extractedArrFile != null) {
//									File targetDir = extractedArrFile.getParentFile(); 
//									if (targetDir.exists() && (nsdFile = new File(targetDir.getAbsolutePath() + File.separator + pureName)).exists()) {
//										// Now let's try again
//										String newTrouble = loadFile((frame instanceof Mainform) ? (Mainform)frame : null,
//												nsdFile, point, arrzFile, group);
//										if (newTrouble.isEmpty()) {
//											trouble = "";
//										}
//										else {
//											trouble += "\n   " + newTrouble;
//										}
//									}
//								}
//							}
//						}
//						catch (Exception ex) {
//							trouble += "\n    " + ex.toString();
//						}
//					}
//					// END KGU#625 2018-12-22
//					if (!trouble.isEmpty())
//					{
//						if (errorMessage != null)
//						{
//							errorMessage += "\n" + trouble;
//						}
//						else {
//							errorMessage = trouble;
//						}
//					}
//					// START KGU#278 2016-10-11: Enh. #267
//					else {
//						nLoaded++;
//					}
//					// END KGU#278 2016-10-11
//				}
//			}
//
//			in.close();
			// START KGU#901 2020-12-29: Issue #901 WAIT_CURSOR on time-consuming actions
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			// END KGU#901 2020-12-29
			StringList problems = new StringList();
			List<ArchiveRecord> records = (new Archivar()).loadArrangement(arrFile, unzippedFrom, currentDirectory, problems);
			if (!problems.isEmpty()) {
				errorMessage = problems.getText().replace(" MISSING!", msgFileMissing.getText());
			}
			Mainform form = (frame instanceof Mainform) ? (Mainform)frame : null;
			for (ArchiveRecord record: records) {
				addDiagram(record.root, form, record.point, group);
				nLoaded++;
			}
// END KGU#679 2019-03-10

			done = true;
		}
		catch (Exception ex)
		{
			if (arrFile == null)
			{
				errorMessage = msgNoArrFile.getText();
			}
			else
			{
				errorMessage = ex.getLocalizedMessage();
			}
			if (errorMessage == null)
			{
				errorMessage = ex.toString();
			}
		}
		finally {
			// START KGU#901 2020-12-29: Issue #901 WAIT_CURSOR on time-consuming actions
			setCursor(origCursor);
			// END KGU#901 2020-12-29
			// START KGU#316 2016-12-28: Enh. #318 don't get confused by the loading of some files
			this.currentDirectory = new File(prevCurDirPath);
			// END KGU#316 2016-12-28
			if (group != null) {
				if (group.isEmpty()) {
					groups.remove(group.getName());
				}
				else {
					group.membersChanged = false;
				}
			}
		}
		if (errorMessage != null)
		{
			JOptionPane.showMessageDialog(frame, msgArrLoadError.getText() + "\n" + errorMessage + "!",
					"Error", JOptionPane.ERROR_MESSAGE, null);   		
		}
		// START KGU#278 2016-10-11: Enh. #267
		if (nLoaded > 0)
		{
			notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		}
		// END KGU#278 2016-10-11
		return done;
	}
	// END KGU#110 2015-12-17

	// START KGU#289 2016-11-15: Enh. #289/#290
	/**
	 * Loads an .arr or .arrz file, associating the loaded diagrams with the given
	 * {@link Mainform} {@code form} if possible.
	 * @param form - a commanding Structorizer Mainform
	 * @param filename - path of the arrangement file to be loaded
	 * @return A rough error message if something went wrong.
	 */
	public String loadArrFile(Mainform form, File file)
	{
		String errorMessage = "";
		File oldCurrDir = currentDirectory;
		// START KGU#316 2016-12-28: Enh. #318 Keep track of the original arrz path
		File arrzFile = null;
		// END KGU#316 2016-12-28
		if (ArrZipFilter.isArr(file.getName())) {
			arrzFile = file;
			file = unzipArrangement(arrzFile, null);
		}
		if (file != null)
		{
			try {
				currentDirectory = file;
				while (currentDirectory != null && !currentDirectory.isDirectory())
				{
					currentDirectory = currentDirectory.getParentFile();
				}
				// START KGU#316 2016-12-28: Enh. #318
				//if (!loadArrangement(form, filename)) {
				if (!loadArrangement(form, file, arrzFile)) {
					// END KGU#316 2016-12-28
					errorMessage = msgDefectiveArr.getText();
				}
			}
			finally {
				currentDirectory = oldCurrDir;
			}
		}
		else
		{
			errorMessage = msgDefectiveArrz.getText();
		}
		return errorMessage;
	}
	// END KGU#289 2016-11-15

	// START KGU#110 2016-07-01: Enh. 62
	/**
	 * Extracts the files contained in the zip file given by `filename´ into the
	 * directory `targetDir´ or a temporary directory (if not given).
	 * @param arrzFile - path of the arrz file
	 * @param targetDir - target directory for the unzipping (may be null)
	 * @return the arr file found in the extracted archive (or otherwise null) 
	 */
	private File unzipArrangement(File arrzFile, File targetDir)
	{
		// START KGU#698
//		final int BUFSIZE = 2048;
//		File arrFile = null;
//		if (targetDir == null)
//		{
//			// START KGU#316 2016-12-28: Enh. #318
//			//targetDir = findTempDir();
//			boolean tmpDirCreated = false;
//			try {
//				// We just force the existence of the folder hierarchy in the temp directory
//				File tempFile = File.createTempFile("arr", null);
//				tempFile.delete();	// We don't need the file itself
//				String targetDirPath = tempFile.getParent() + File.separator + arrzFile.getName();
//				if (targetDirPath.endsWith(".arrz")) {
//					targetDirPath = targetDirPath.substring(0, targetDirPath.length()-4) + "unzip";
//				}
//				targetDir = new File(targetDirPath);
//				if (targetDir.isDirectory()) {
//					tmpDirCreated = true;
//				}
//				else {
//					tmpDirCreated = targetDir.mkdirs();
//				}
//			} catch (IOException ex) {
//				logger.log(Level.WARNING, "Failed to unzip the arrangement archive: {0}", ex.getLocalizedMessage());
//			}
//			if (!tmpDirCreated) {
//				targetDir = findTempDir();
//			}
//			// END KGU#316 2016-12-28
//		}
//		try {
//			BufferedOutputStream dest = null;
//			BufferedInputStream bistr = null;
//			ZipEntry entry;
//			ZipFile zipfile = new ZipFile(arrzFile);
//			Enumeration<? extends ZipEntry> entries = zipfile.entries();
//			while(entries.hasMoreElements()) {
//				entry = (ZipEntry) entries.nextElement();
//				File targetFile = new File(targetDir.getAbsolutePath() + File.separator + entry.getName());
//				bistr = new BufferedInputStream
//						(zipfile.getInputStream(entry));
//				int count;
//				byte buffer[] = new byte[BUFSIZE];
//				FileOutputStream fostr = new FileOutputStream(targetFile);
//				dest = new BufferedOutputStream(fostr, BUFSIZE);
//				while ((count = bistr.read(buffer, 0, BUFSIZE))	!= -1)
//				{
//					dest.write(buffer, 0, count);
//				}
//				dest.flush();
//				dest.close();
//				bistr.close();
//				// START KGU 2018-09-12: Preserve at least the modification time if possible
//				Path destPath = targetFile.toPath();
//				try {
//					Files.setLastModifiedTime(destPath, entry.getLastModifiedTime());
//				} catch (IOException e) {}
//				// END KGU 2018-09-12
//				if (ArrFilter.isArr(entry.getName()))
//				{
//					arrFile = targetFile;
//				}
//			}
//			zipfile.close();
//		} catch(Exception ex) {
//			// START KGU#484 2018-04-05: Issue #463
//			//ex.printStackTrace();
//			logger.log(Level.WARNING, "Failed to unzip the arrangement archive " + arrzFile.getAbsolutePath(), ex);
//			// END KGU#484 2018-04-05
//		}
//		return arrFile;
		Archivar archivar = new Archivar();
		// START KGU#775 2019-11-29: Bugfix #788 - the extraction target dir was ignored
		//ArchiveIndex archiveIndex = archivar.unzipArrangementArchive(arrzFile, null);
		ArchiveIndex archiveIndex = archivar.unzipArrangementArchive(arrzFile, targetDir);
		// END KGU#775 2019-11-29
		return archiveIndex.arrFile;
	}

	/**
	 * Finds a directory for temporary files (trying different OS standard environment variables)
	 * @return path of a temp directory
	 */
	private static File findTempDir()
	{
		return Archivar.findTempDir();
	}
	// END KGU#110 2016-07-01

	public void exportPNG(Frame frame)
	{
		JFileChooser dlgSave = new JFileChooser(currentDirectory);
		// propose name
		//String uniName = directoryName.substring(directoryName.lastIndexOf('/')+1).trim();
		//dlgSave.setSelectedFile(new File(uniName));

		dlgSave.addChoosableFileFilter(new PNGFilter());
		dlgSave.setDialogTitle(msgExportDialogTitle.getText());
		int result = dlgSave.showSaveDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			currentDirectory = dlgSave.getCurrentDirectory();
			while (currentDirectory != null && !currentDirectory.isDirectory())
			{
				currentDirectory = currentDirectory.getParentFile();
			}
			// correct the filename, if necessary
			String filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".png"))
			{
				filename+=".png";
			}

			// temporarily deselect any diagram
			if (diagrams != null)
			{
				for (int d=0; d<diagrams.size(); d++)
				{
					diagrams.get(d).root.setSelected(false, Element.DrawingContext.DC_ARRANGER);
				}
				repaint();
			}

			// set up the file
			File file = new File(filename);
			// create the image
			// START KGU#497 2018-03-19: Enh. #512 - consider the (new) zoom factor
			//BufferedImage bi = new BufferedImage(this.getWidth(), this.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
			//paint(bi.getGraphics());
			// START KGU#624 2018-12-24: Enh. #655
			//Rect rect = this.getDrawingRect(null);
			int offsetX = 0, offsetY = 0;
			Rect rect = null;
			if (this.diagramsSelected.isEmpty()) {
				rect = this.getDrawingRect(null);
			}
			else {
				rect = new Rect(getSelectionBounds(true));
				offsetX = rect.left - DEFAULT_GAP;
				offsetY = rect.top - DEFAULT_GAP;
				rect.left = DEFAULT_GAP;
				rect.top = DEFAULT_GAP;
				rect.right -= offsetX;
				rect.bottom -= offsetY;
			}
			int width = this.getWidth() - Math.round(offsetX * zoomFactor);
			int height = this.getHeight() - Math.round(offsetY * zoomFactor);
			// END KGU#624 2018-12-24
			if (logger.isLoggable(Level.CONFIG)) {
				logger.log(Level.CONFIG, "{0} x {1}", new Object[]{width, height});
				logger.log(Level.CONFIG, "Drawing Rect: {0}", rect);
				logger.log(Level.CONFIG, "zoomed: {0} x {1}", new Object[]{width/this.zoomFactor, height/this.zoomFactor});
			}
			BufferedImage bi = new BufferedImage(
					Math.round(width / this.zoomFactor),
					Math.round(height / this.zoomFactor),
					BufferedImage.TYPE_4BYTE_ABGR);
			paintComponent(bi.getGraphics(), true, !this.diagramsSelected.isEmpty(), offsetX, offsetY);
			// END KGU#497 2018-03-19
			// START KGU#624 2018-12-24: Enh. #655 - support multiole selection / restore selection
			for (Diagram diagr: this.diagramsSelected) {
				if (diagr.root != null) {
					diagr.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
				}
			}
			// END KGU624 2018-12-24
			// save the file
			try
			{
				ImageIO.write(bi, "png", file);
			}
			catch(Exception e)
			{
				JOptionPane.showOptionDialog(frame, msgExportError.getText(), "Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
			}
		}
	}

	/**
	 * Determines the union of bounds of all diagrams on this {@link Surface} (in true diagram
	 * coordinates) and updates the lower silhouette line of {@code _silhouette} if it is given.
	 * @param _silhouette - a list of pairs {x,y} representing the lower silhouette line
	 * (where the x coordinate represents a leap position and the y coordinate is the new
	 * level from x to the next leap eastwards) or null
	 * @return the bounding box as {@link Rect} (an empty Rect at (0,0) if there are no diagrams)=
	 */
	public Rect getDrawingRect(LinkedList<Point> _silhouette)
	{
		return getDrawingRect(diagrams, _silhouette);
	}

	/**
	 * Determines the union of bounds of the given {@code _diagrams} in true diagram coordinates
	 * and updates the lower silhouette line if {@code _silhouette} is given.<br/>
	 * At the right-hand and bottom side, a buffer of {@link #DEFAULT_GAP} is added, if the
	 * bounds are not empty (Issue #673).
	 * @param _diagrams - a collection of {@link Diagram} objects.
	 * @param _silhouette - a list of pairs {x,y} representing the lower silhouette line
	 * (where the x coordinate represents a leap position and the y coordinate is the new
	 * level from x to the next leap eastwards) or null
	 * @return the bounding box as {@link Rect} (an empty Rect at (0,0) if there are no diagrams)
	 */
	private Rect getDrawingRect(Collection<? extends Diagram> _diagrams, LinkedList<Point> _silhouette)
	{
		Rect r = new Rect(0,0,0,0);

		if (_diagrams != null && !_diagrams.isEmpty())
		{
			r = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
			//System.out.println("--------getDrawingRect()---------");
			for (Diagram diagram: _diagrams)
			{
				Root root = diagram.root;
				// FIXME (KGU 2015-11-18) This does not necessarily return a Rect within this surface!
				Rect rect = root.getRect();	// 0-bound extension rectangle
				// START KGU#85 2015-11-18: Didn't work properly, hence
				//r.left=Math.min(rect.left,r.left);
				//r.top=Math.min(rect.top,r.top);
				//r.right=Math.max(rect.right,r.right);
				//r.bottom=Math.max(rect.bottom,r.bottom);
				// START KGU#136 2016-03-01: Bugfix #97
				// empirical minimum width of an empty diagram
				//int width = Math.max(rect.right - rect.left, 80);
				int width = Math.max(rect.right, MIN_WIDTH);
				// empirical minimum height of an empty diagram 
				//int height = Math.max(rect.bottom - rect.top, 118);
				int height = Math.max(rect.bottom, MIN_HEIGHT);
				// END KGU#136 2016-03-01
				// DEBUG: disable this output for releases
				//System.out.println(root.getMethodName() + ": (" + (diagram.point.x + rect.left) + ", " + (diagram.point.y + rect.top) + ", " + (diagram.point.x + rect.right) + ", " + (diagram.point.y + rect.bottom) +")");
				r.left = Math.min(diagram.point.x, r.left);
				r.top = Math.min(diagram.point.y, r.top);
				r.right = Math.max(diagram.point.x + width, r.right);
				r.bottom = Math.max(diagram.point.y + height, r.bottom);
				//END KGU#85 2015-11-18
				// START KGU#499 2018-02-20
				if (_silhouette != null) {
					this.updateSilhouette(_silhouette, diagram.point.x, diagram.point.x + width, diagram.point.y + height);
				}
				// END KGU#499 2018-02-20
			}
			// START KGU#645 2019-02-03: Issue #673 - drawing area should exceed the group bounds a little
			r.right += DEFAULT_GAP;
			r.bottom += DEFAULT_GAP;
			// END KGU#645 2019-02-03S
		}
		//System.out.println("drawingRect: (" + r.left + ", " + r.top + ", " + r.right + ", " + r.bottom +")");

		return r;
	}

	// START KGU#499 2018-02-20: Enh. #515 - Helper method for more intelligent area management
	/**
	 * Integrates the shape of a diagram given by its left x coordinate, its width and its
	 * bottom y coordinate into the Point list {@code silhouette} describing the course of the
	 * lower silhouette of the diagrams.<br/>
	 * At the moment, this method tends to consume O(N) time with N diagrams already processed.
	 * @param _silhouette - List of leap points in the silhouette line from left to right
	 * @param left - the left edge x coordinate of the considered diagram
	 * @param right - the right edge x coordinate of the considered diagram
	 * @param bottom - the bottom y coordinate of the considered diagram
	 */
	private void updateSilhouette(LinkedList<Point> _silhouette, int left, int right, int bottom) {
		ListIterator<Point> iter = _silhouette.listIterator();
		Point lastLeap = new Point(0, 0);	// previous leap data
		Point leap = null;			// current leap data
		if (!iter.hasNext()) {
			// Ensure a first leap entry {0, 0}
			iter.add(lastLeap);
		}
		else {
			// Adopt the actual start level
			lastLeap = iter.next();
		}
		// Search for an overlapping between diagram and silhouette
		while (iter.hasNext() && ((leap = iter.next()).x < left || leap.x < right && lastLeap.y >= bottom && leap.y >= bottom)) {
			lastLeap = leap;	// FIXME: clone?
		}
		// Now if we haven't found any leap at all, then just add the two leaps for this diagram
		Point leap1 = new Point(left, bottom);
		Point nextLeap = new Point(right, lastLeap.y);
		if (leap == null || leap.x < left) {
			_silhouette.add(leap1);
			_silhouette.add(nextLeap);
			leap = null;
		}
		// Otherwise there are three fundamental cases:
		else if (leap.x >= right && lastLeap.x <= left && lastLeap.y < bottom) {
			// 1. The current leap is already beyond the diagram, the diagram had protruded the
			// level between lastLeap and leap
			nextLeap.y = lastLeap.y;
			iter.previous();
			if (lastLeap.x < left - DEFAULT_GAP) {
				iter.add(leap1);
			}
			else {
				lastLeap.y = bottom;
			}
			if (leap.x > right + DEFAULT_GAP) {
				iter.add(nextLeap);
			}
		}
		else if (leap.x < right) {
			// We have a leap transition inside the stretch of the diagram. In case this is the last leap
			// ever, we must restore the former leap (in theory, a silhouette as either none or at least two nodes...)
			if (lastLeap == leap && iter.hasPrevious()) {
				lastLeap = iter.previous();
				iter.next();
			}
			if (lastLeap.y >= bottom) {
				// 2. The silhouette had exceeded the diagram but is now receding --> update leap to level bottom
				// START KGU#697 2019-03-26: Bugfix - leap must be cloned!
				//lastLeap = leap;
				lastLeap = (Point)leap.clone();
				// END KGU#697 2019-03-26
				leap.y = bottom;
			}
			else {
				// 3. The silhouette had not exceeded the diagram, so it's the first leap
				// inside the diagram bounds, the level may now protrude or not
				//    --> insert a new leap at position left (or just raise the level of leap)
				if (leap.x > left) {
					if (lastLeap.x == leap1.x) {
						lastLeap.y = leap1.y;
					}
					else {
						iter.previous();
						iter.add(leap1);
						iter.next();
					}
					lastLeap = leap;
				}
				else if (leap.y < bottom) {
					// START KGU#697 2019-03-26: Bugfix - leap must be cloned!
					//lastLeap = leap;
					lastLeap = (Point)leap.clone();
					// END KGU#697 2019-03-26
					leap.y = bottom;
				}
				else {
					lastLeap = leap;
				}
				nextLeap.y = lastLeap.y;
			}
			// Now wipe all leaps eclipsed or exceeded by the diagram
			boolean first = true;
			while (leap != null && leap.x < right) {
				if (!first) {
					lastLeap = leap;	// FIXME! clone?
				}
				else {
					first = false;
				}
				if (leap.x > left) {
					if (leap.y <= bottom) {
						if (nextLeap.y <= bottom) {
							iter.previous();
							iter.remove();
						}
						else if (nextLeap.y > bottom) {
							leap.y = bottom;
						}
					}
				}
				if (iter.hasNext()) {
					leap = iter.next();
				}
				else {
					leap = null;
				}
				nextLeap.y = lastLeap.y;
			}
			// If there is no further leap or the next leap is far, insert the prepared end leap
			// START KGU#633 2019-01-08: Bugfix #515
			if (leap == null || (leap.x > right + DEFAULT_GAP) && (nextLeap.y < bottom)) {
			// END KGU#633 2019-01-08
				if (leap != null && leap.y == nextLeap.y) {
					leap.x = right;
				}
				else {
					if (leap != null && iter.hasPrevious()) {
						iter.previous();
					}
					iter.add(nextLeap);
				}
			}
		}
		// DEBUG: Disable this list printing after debugging
//		iter = _silhouette.listIterator();
//		System.out.println("Current silhouette:");
//		while (iter.hasNext()) {
//			leap1 = iter.next();
//			System.out.println(leap1.x + " --> " + leap1.y);
//		}
	}

	// START KGU#699 2019-03-27: Issue #717 - made protected to allow access from Arranger
	//private Rect adaptLayout()
	protected Rect adaptLayout()
	// END KGU#699 2019-03-17
	{
		Rect rect = getDrawingRect(null);
		// START KGU#85 2017-10-23: Enh. #35 - Add scrollbars
		Dimension oldDim = this.getPreferredSize();
		// START KGU#524 2018-06-18: Bugfix #544 for #512 (forgotten zoom consideration)
//		if (rect.right != oldDim.width || rect.bottom != oldDim.height) {
//			this.setPreferredSize(new Dimension(rect.right, rect.bottom));
//			this.revalidate();
//		}
		Dimension newZoomedDim = new Dimension(Math.round(Math.min(rect.right, Short.MAX_VALUE) * this.zoomFactor),
				Math.round(Math.min(rect.bottom, Short.MAX_VALUE) * this.zoomFactor));
		if (newZoomedDim.width != oldDim.width || newZoomedDim.height != oldDim.height) {
			this.setPreferredSize(newZoomedDim);
			this.revalidate();
		}
		// END KGU#524 2018-06-18
		// END KGU#85 2017-10-23
		// START KGU#444 2017-10-23: Issue #417 - reduce polynomial scrolling time complexity
		adaptScrollUnits(rect);
		// END KGU#444 2017-10-23
		return rect;	// Just in case someone might need it
	}

	// START KGU#444 2017-10-23: Issue #417 - polynomial scrolling time complexity 
	/**
	 * Adapts the scroll units according to the size of the current {@link Root}. With standard scroll unit
	 * of 1, large diagrams would take an eternity to get scrolled over because their redrawing time also
	 * increases with the number of elements, of course, such that it's polynomial (at least square) time growth... 
	 */
	protected void adaptScrollUnits(Rect drawArea) {
		Container parent = this.getParent();
		if (parent != null && (parent = parent.getParent()) instanceof javax.swing.JScrollPane) {
			javax.swing.JScrollPane scroll = (javax.swing.JScrollPane)parent;
			// START KGU#444 2017-11-03: Bugfix #417
			//int unitsVertical = drawArea.bottom / scroll.getHeight() + 1;
			//int unitsHorizontal = drawArea.right / scroll.getWidth() + 1;
			int unitsVertical = 1;
			int unitsHorizontal = 1;
			// FIXME: May we apply the zoomFactor here only?
			if (scroll.getHeight() > 0) {
				unitsVertical = drawArea.bottom / scroll.getHeight() + 1;
			}
			if (scroll.getWidth() > 0) {
				unitsHorizontal = drawArea.right / scroll.getWidth() + 1;
			}
			// END KGU#444 2017-11-03
			//System.out.println("unit factors: " + widthFactor + " / " + heightFactor);
			// START KGU#699 2019-03-27: Issue #717 - make base increment configurable
			//scroll.getHorizontalScrollBar().setUnitIncrement(unitsHorizontal);
			//scroll.getVerticalScrollBar().setUnitIncrement(unitsVertical);
			if (Element.E_WHEEL_SCROLL_UNIT <= 0) {
				// The very first time Structorizer is used, we fetch the original unit increment
				Element.E_WHEEL_SCROLL_UNIT = scroll.getVerticalScrollBar().getUnitIncrement();
			}
			scroll.getHorizontalScrollBar().setUnitIncrement(Element.E_WHEEL_SCROLL_UNIT + unitsHorizontal - 1);
			scroll.getVerticalScrollBar().setUnitIncrement(Element.E_WHEEL_SCROLL_UNIT + unitsVertical - 1);
			// END KGU#699 2019-03-27
		}
	}
	// END KGU#444 2017-10-23

	// START KGU#679 2019-03-12: Enh. #698
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#addArchive(java.io.File, boolean)
	 */
	@Override
	public boolean addArchive(File arrangementArchive, boolean lazy) {
		// TODO we should actually think about lazy mode in order to reduce drawing contention
		String errors = this.loadFile(arrangementArchive);
		return !errors.isEmpty();
	}
	// END KGU#679 2019-03-12

	// START KGU#742 2019-10-04: To solve a problem with concurrent modifications 
	/**
	 * If the given {@link Root} {@code root} is orphaned (i.e. has no owning
	 * {@link Mainform} then it will be assigned to the given {@code mainform}.
	 * @param root a {@link Root} supposed to be in the pool
	 * @param mainform - the adopting {@link Mainform}
	 */
	public void adoptRootIfOrphaned(Root root, Mainform mainform) {
		Diagram owner = rootMap.get(root);
		if (owner != null && owner.mainform == null) {
			owner.mainform = mainform;
			mainform.addWindowListener(this);
		}
		root.addUpdater(this);
	}
	// END KGU#742 2019-10-04
	
	/**
	 * Places the passed-in diagram {@code root} in the drawing area if it hadn't already been
	 * residing here. If a {@link Mainform} {@code form} was given, then it is registered with
	 * the {@code root} (unless there is already another {@link Mainform} associated) and
	 * {@code root} will automatically be pinned.
	 * The diagram will be associated to the default group
	 * @param root - a diagram to be placed here
	 * @see #addDiagramToGroup(Root, Mainform, Point, String)
	 */
	public void addDiagram(Root root)
	// START KGU#2 2015-11-19: Needed a possibility to register a related Mainform
	{
		// START KGU#624 2018-12-23: Enh. #655
		// Only the new diagram shall be selected afterwards
		unselectAll();
		// END KGU#624 2018-12-23
		addDiagram(root, null, null, null);
	}
	/**
	 * Places the passed-in diagram {@code root} in the drawing area if it hadn't already
	 * been residing here. If a {@link Mainform} {@code form} was given, then it is registered
	 * with the {@code root} (unless there is already another {@link Mainform} associated) and
	 * {@code root} will automatically be pinned.
	 * The diagram will be associated to the default group.
	 * @param root - a diagram to be placed here
	 * @param form - the sender of the diagram if it was pushed here from a Structorizer instance
	 * @see #addDiagramToGroup(Root, Mainform, Point, String)
	 */
	public void addDiagram(Root root, Mainform form)
	// START KGU#110 2015-12-20: Enhancement #62 -we want to be able to use predefined positions
	{
		// START KGU#624 2018-12-23: Enh. #655
		// Only the new diagram shall be selected afterwards
		unselectAll();
		// END KGU#624 2018-12-23
		this.addDiagram(root, form, null, null);
	}

	/**
	 * Adds the given {@link Root} {@code root} to the set of diagrams, associating it to the default group.
	 * @param root - the {@link Root} element of the diagram to be added
	 * @param position - the proposed position
	 * @see #addDiagramToGroup(Root, Mainform, Point, String)
	 */
	public void addDiagram(Root root, Point position)
	// START KGU#110 2015-12-20: Enhancement #62 - we want to be able to use predefined positions
	{
		// START KGU#626 2018-12-28 Enh. #657
		//this.addDiagram(root, null, position);
		this.addDiagram(root, null, position, null);
		// END KGU#626 2018-12-28
	}

	// START KGU#626 2018-12-28: Enh. #657 - opportunity to specify the owning group via name
	/**
	 * Places the passed-in diagram {@code root} in the drawing area if it hadn't already been
	 * residing here. If a {@link Mainform} {@code form} was given, then it is registered with
	 * the {@code root} (unless there is already another {@link Mainform} associated) and
	 * {@code root} will automatically be pinned.
	 * If {@code groupName} is given then a matching {@link Group} will be fetched or created and
	 * the diagram will be associated to it. 
	 * If {@code point} is given then the diagram will be placed to that position, otherwise a free
	 * area is looked for.
	 * @param root - the {@link Root} element of the diagram to be added
	 * @param form - the sender of the diagram if it was pushed here from a Structorizer instance or null otherwise
	 * @param point - the proposed position, may be null if a location is to be found automatically
	 * @param groupName - name of an existing group or else a group to be created, which the diagram is to be added to
	 * @see #addDiagramToGroup(Group, Root)
	 */
	protected void addDiagramToGroup(Root root, Mainform form, Point point, String groupName)
	{
		Group group = this.groups.get(groupName);
		if (group == null && groupName != null) {
			group = new Group(groupName);
			this.groups.put(groupName, group);
		}
		this.addDiagram(root, form, point, group);
	}

	/**
	 * Places the passed-in diagram {@code root} in the drawing area if it hadn't already been
	 * residing here. If a {@link Mainform} {@code form} was given, then it is registered with
	 * the {@code root} (unless there is already another {@link Mainform} associated) and
	 * {@code root} will automatically be pinned.<br/>
	 * If {@code point} is given then the diagram will be placed to that position, otherwise a free
	 * area is looked for.<br/>
	 * If {@code owningGroup} is given then the diagram will be associated to if it is not already
	 * a member. Otherwise the diagram will be associated to the default group (if possible).
	 * @param root - the {@link Root} element of the diagram to be added
	 * @param form - the sender of the diagram if it was pushed here from a Structorizer instance
	 * @param point - the proposed position
	 * @param owningGroup - the diagram group {@code root} belongs to or is to belong to
	 */
	private void addDiagram(Root root, Mainform form, Point point, Group owningGroup)
	// END KGU#110 2015-12-20
	// END KGU#2 2015-11-19
	{
		// START KGU#2 2015-11-19: Don't add a diagram that is already held here
		// START KGU#119 2016-01-02: Bugfix #78 - Don't reload a structurally equal diagram from file
		//Diagram diagram = findDiagram(root);
		// START KGU#312 2016-12-29: Enh. #315 - more differentiated equality test
		//Diagram diagram = findDiagram(root, form != null);	// If the Mainform is given, then it's not from file
		Diagram diagram = findDiagram(root, (form != null) ? 2 : 3, true);	// If the Mainform is given, then it's not from file
		// END KGU#312 2016-12-29
		// END KGU#1119 2016-01-02
		if (diagram == null) {
		// END KGU#2 2015-11-19
			boolean pointGiven = point != null && point.x >= 0 && point.y >= 0;
			// START KGU#499 2018-02-22: New packing strategy (silhouette approach)
			//Rect rect = getDrawingRect();
			LinkedList<Point> silhouette = new LinkedList<Point>();
			/* The bounds of the computed silhouette as a first rough approach
			 * (hardly better than getDrawingRect(), the major aim is the computation
			 * of the silhouette */
			Rect rect = getDrawingRect(silhouette);
			// END KGU#499 2018-02-22

			int top = DEFAULT_GAP;
			int left = DEFAULT_GAP;

			top  = Math.max(rect.top, top);
			left = Math.max(rect.right + DEFAULT_GAP, left);

			// START KGU#497 2018-02-17: Enh. #512 - zooming must be considered
			//if (left > this.getWidth())
			if (left > this.getWidth() / this.zoomFactor)
				// END KGU#497 2018-02-17
			{
				// FIXME (KGU 2015-11-19) This isn't really sensible - might find a free space by means of a quadtree?
				top = rect.bottom + DEFAULT_GAP;
				left = rect.left;
			}
			// START KGU#110 2015-12-20
			//Point point = new Point(left,top);
			if (!pointGiven)
			{
				point = new Point(left, top);
			}
			// END KGU#110 2015-12-20
			// START KGU 2016-03-14: Enh. #62
			// If it's the first diagram then adopt the current directory if possible
			if (diagrams.isEmpty() && root.filename != null && !root.filename.isEmpty())
			{
				// START KGU#316 2016-12-29: Enh. #318 - get the directory of the arrz file if unzipped 
				//this.currentDirectory = new File(root.filename);
				this.currentDirectory = new File(root.getPath(true));
				// END KGU#316 2016-12-29
				while (this.currentDirectory != null && !this.currentDirectory.isDirectory())
				{
					this.currentDirectory = this.currentDirectory.getParentFile();
				}
			}
			// END KGU 2016-03-14
			/*Diagram*/ diagram = new Diagram(root,point);
			diagrams.add(diagram);
			// START KGU#624 2018-12-26: Enh. #655 Attempt to make search faster
			rootMap.put(root, diagram);
			String rootName = root.getMethodName();
			addToNameMap(rootName, diagram);
			// END KGU#624 2018-12-26
			//printNameMap(2507);
			// START KGU 2015-11-30
			// START KGU#136 2016-03-01: Bugfix #97 - here we need the actual position
			//Rectangle rec = root.getRect().getRectangle();
			//rec.setLocation(left, top);
			Rect rec = root.getRect(point);
			// END KGU#136 2016-03-01
			// START KGU#627 2019-03-14: Check if the diagram has dimensions
			if (rec.right == rec.left && rec.top == rec.bottom) {
				// Can never have been drawn, so try it now...
				Graphics2D graphics2d = (Graphics2D) this.getGraphics();
				rec = root.prepareDraw(graphics2d);
				// START KGU#627 2019-03-26 Now we must move the record to the proposed position
				rec.add(point);
				// END KGU#627 2019-03-26
			}
			// END KGU#627 2019-03-14
			if (rec.right == rec.left) rec.right += DEFAULT_WIDTH;
			if (rec.bottom == rec.top) rec.bottom += DEFAULT_HEIGHT;
			
			// START KGU#499 2018-02-21: Enh. #515 - better area management
			// Improve the placement if possible (for more compact arrangement)
			if (!pointGiven) {
				point = findPreferredLocation(silhouette, rec.getRectangle());
				diagram.point = point;
				// START KGU 2019-03-11
				rec = root.getRect(point);
				//if (draftRec != null) {
				//	(rec = draftRec.copy()).add(point);
				//}
				//else {
				//	rec = root.getRect(point);
				//}
				// END KGU 2019-03-11
			}
			// END KGU#499 2018-02-21
			// START KGU#85 2015-11-18
			adaptLayout();
			// END KGU#85 2015-11-18
			// START KGU#497 2018-12-23: Bugfix for enh. #512
			rec = rec.scale(this.zoomFactor);
			// END KGU#497 2018-12-23
			this.scrollRectToVisible(rec.getRectangle());
			// START KGU#88 2015-12-20: It ought to be pinned if form wasn't null (KGU#804 2020-02-17: now done in both cases)
			//if (form != null)
			//{
			//	diagram.isPinned = true;
			//}
			// END KGU#88 2015-12-20
			// START KGU#626 2019-01-01: Enh. #657 Moved behind the alternative (to be done in both branches)
//			// START KGU#278 2016-10-11: Enh. #267
//			notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
//			// END KGU#278 2016-10-11
//			// END KGU 2015-11-30
//			// START KGU#624 2018-12-21: Enh. #655 Simply add the new diagram to the selection
//			//// START KGU 2016-12-12: First unselect the selected diagram (if any)
//			//if (mouseSelected != null && mouseSelected.root != null)
//			//{
//			//	mouseSelected.root.setSelected(false);
//			//}
//			//mouseSelected = diagram;
//			diagramsSelected.add(diagram);
//			diagram.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
//			// END KGU 2016-12-12
//			// START KGU#624 2018-12-21: Enh. #655
//			notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
//			// END KGU#624 2018-12-21
//			repaint();
//			//getDrawingRect();	// Desperate but ineffective approach to force scroll area update
			// END KGU#626 2018-01-01
			// START KGU#2 2015-11-19
		}
		// START KGU#119 2016-01-02: Bugfix #78 - if a position is given then move the found diagram
		else if (point != null)
		{
			diagram.setLocation(point.x, point.y);
			// START KGU#626 2019-01-01 Enh. #657 Moved after the alternative (to be done in both branches)
//			// START KGU 2016-12-12: First unselect the selected diagram (if any)
//			// START KGU#624 2018-12-21: Enh. #655 Multiple selection - just add the diagram
//			//if (mouseSelected != null && mouseSelected.root != null)
//			//{
//			//	mouseSelected.root.setSelected(false);
//			//}
//			//mouseSelected = diagram;
//			diagramsSelected.add(diagram);
//			diagram.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
//			// START KGU#624 2018-12-21: Enh. #655
//			notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
//			// END KGU#624 2018-12-21
//			// END KGU 2016-12-12
//			repaint();
			//getDrawingRect();	// Desperate but ineffective approach to force scroll area update
			// END KGU#626 201-01-01
		}
		// END KGU#119 2016-01-02
		// START KGU#88/KGU#804 2020-02-17: Bugfix #818 It ought to be pinned if form wasn't null
		if (form != null)
		{
			diagram.isPinned = true;
		}
		// END KGU#88/KGU#804 2020-02-17
		// START KGU#626 2018-12-28: Enh. #657 Group management
		if (owningGroup == null && diagram.getGroupNames().length == 0) {
			owningGroup = this.groups.get(Group.DEFAULT_GROUP_NAME);	// Default group
			if (owningGroup == null) {
				owningGroup = new Group(Group.DEFAULT_GROUP_NAME);
				this.groups.put(Group.DEFAULT_GROUP_NAME, owningGroup);
			}
		}
		if (owningGroup != null) {
			owningGroup.addDiagram(diagram);
			/* 
			 * Keep the former selection only if the owningGroup is among the selected groups
			 * or if all selected diagrams are members of the owningGroup, otherwise clear the
			 * selection and start with a new selection set.
			 */
			if (!groupsSelected.isEmpty() && !groupsSelected.contains(owningGroup)) {
				this.unselectAll();
			}
			else if (!diagramsSelected.isEmpty() && owningGroup != null) {
				for (Diagram diagr: diagramsSelected) {
					if (!this.getGroups(diagr).contains(owningGroup)) {
						this.unselectAll();
						break;
					}
				}
			}
		}
		diagramsSelected.add(diagram);
		diagram.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
		// END KGU 2016-12-12
		// START KGU#701 2019-03-30: Issue #718
		if (root.isInclude()) {
			for (Root ref: this.findIncludingRoots(root.getMethodName(), true)) {
				ref.clearVarAndTypeInfo(false);
			}
		}
		// END KGU#701 2019-03-30
		// START KGU#624 2018-12-21: Enh. #655
		notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED | IRoutinePoolListener.RPC_SELECTION_CHANGED);
		// END KGU#624 2018-12-21
		repaint();
		// END KGU#626 2018-12-28
		if (form != null)
		{
			// START KGU#125 2016-01-07: We allow adoption but only for orphaned diagrams
			//diagram.mainform = form;
			if (diagram.mainform == null)
			{
				diagram.mainform = form;
				// START KGU#305 2016-12-12: Enh.#305 / 2016-12-16 no longer needed
				//form.updateArrangerIndex();
				// END KGU#305 2016-12-12
				// START KGU#745 2019-10-05: Bugfix #759 - We must be informed about dying Mainforms
				form.addWindowListener(this);
				// END KGU#745 2019-10-05
			}
			// END KGU#125 2016-01-07
			root.addUpdater(this);
		}
		// END KGU#2 2015-11-19
	}

	// START KGU#499 2018-02-21: Enh. #515 - More intelligent area management
	/**
	 * Scans the given silhouette line given by the {@link Point} list {@code silhouette}
	 * for the uppermost breach wide enough to accommodate a diagram of width {@code rec.width}. 
	 * @param silhouette - linked {@link Point} list symbolizing the lower bound of the diagrams
	 * @param rec - the proposed {@link Rectangle} of a diagram (possibly to be relocated)
	 * @return the preferrable new anchor position (top left) for the diagram
	 */
	private Point findPreferredLocation(LinkedList<Point> silhouette, Rectangle rec) {
		ListIterator<Point> iter = silhouette.listIterator();
		LinkedList<Point> candidates = new LinkedList<Point>();
		candidates.add(new Point(0,0));
		Point optimum = null;
		while (iter.hasNext()) {
			Point leap = iter.next();
			if (leap.x > this.getWidth() / this.zoomFactor) {
				break;
			}
			ListIterator<Point> iter1 = candidates.listIterator();
			while (iter1.hasNext()) {
				Point cand = iter1.next();
				// Update the entry if incomplete and needed
				if (leap.x < cand.x + rec.width + 2 * DEFAULT_GAP) {
					if (leap.y > cand.y) {
						cand.y = leap.y;
					}
				}
				else {
					if (optimum == null || cand.y < optimum.y) {
						optimum = cand;
					}
					// A complete entry worse than the optimum isn't needed any longer
					iter1.remove();
				}
			}
			// Here a better entry might start (though leap(0,0) isn't needed, see first candidate) 
			if ((optimum == null || leap.y < optimum.y)/* && !(leap.x == 0 && leap.y == 0)*/) {
				candidates.add(new Point(leap));
			}
		}
		// If we didn't find anything better, then we will just adhere to the bounds approach result
		// But first have a look whether some incompletely analysed breaches (those remaining open at
		// end) are wide enough to be accepted. We will allow a diagram if it fits at least by half.
		float windowWidth = this.getWidth() / this.zoomFactor - rec.width/2; 
		for (Point cand: candidates) {
			if (cand.x < windowWidth && (optimum == null || cand.y < optimum.y)) {
				optimum = cand;
			}
		}
		if (optimum == null) {
			optimum = new Point(rec.x, rec.y); 
		}
		else {
			optimum.x += DEFAULT_GAP;
			optimum.y += DEFAULT_GAP;
		}
		return optimum;
	}
	// END KGU#499 2018-02-21

	// START KGU#85 2015-11-17
	/**
	 * Removes all currently selected diagrams
	 */
	public void removeDiagram()
	{
		// START KGU#624 2018-12-21: Enh. #655 Allow multiple selection
		//if (this.mouseSelected != null)
		//{
		//	removeDiagram(this.mouseSelected);
		//}
		// We must produce a copy of all selected elements because removeDiagram(Diagram) will update the selection
		Diagram[] selectedDiagrams = new Diagram[this.diagramsSelected.size()];
		this.diagramsSelected.toArray(selectedDiagrams);
		for (Diagram diagr: selectedDiagrams) {
			removeDiagram(diagr);
		}
		// END KGU#624 2018-12-21
	}
	// END KGU#85 2015-11-17

	// START KGU#305 2016-12-17: Enh. #305 
	/**
	 * Removes the given {@link Root} {@code _root} from the Arranger surface and all
	 * groups it may belong to after having given the user an opportunity to save pending
	 * changes. 
	 * @param _root - the {@link Root} to be removed from Arranger.
	 */
	public void removeDiagram(Root _root) {
		// START KGU#312 2016-12-29: Enh. #315: More meticulous equality check
		//Diagram diagr = findDiagram(_root, true);
		Diagram diagr = findDiagram(_root, 1);	// Test for object identity
		// END KGU#312 2016-12-29
		if (diagr != null) {
			removeDiagram(diagr);
		}
	}

	/**
	 * Removes the given {@link Diagram} object from the Arranger surface and all referencing
	 * collections after having given the user an opportunity to save pending changes. 
	 * @param diagr - the {@link Diagram} to be eliminated.
	 */
	private void removeDiagram(Diagram diagr)
	{
		boolean ask = true;
		Mainform form = diagr.mainform;
		// START KGU#194 2016-05-09: Bugfix #185 - on importing unsaved roots may linger here
		// START KGU#911 2021-01-10: Enh. #910 A DiagramController includable can't have been changed, but...
		//if (diagr.root.hasChanged())
		if (diagr.root.hasChanged() && !diagr.root.isRepresentingDiagramController())
		// END KGU#911 2021-01-1ß
		{
			if (form == null || form.getRoot() != diagr.root)
			{
				// Create a temporary Mainform
				form = new Mainform(false);
				form.setRoot(diagr.root);
				// Let the user decide to save it or not, or to cancel this removal 
				boolean goOn = form.diagram.saveNSD(true);
				// Destroy the temporary Mainform
				// START KGU#745 2019-10-05: Bugfix #759 - we must also remove it from the listeners (had added itself)
				removeChangeListener(form);
				// END KGU#745 2019-10-05
				form.dispose();
				if (!goOn)
				{
					// User cancelled this - don't remove
					return;
				}
				form = diagr.mainform;
				ask = false;
			}
		}
		// END KGU#194 2016-05-09
		diagr.root.removeUpdater(this);
		// START KGU#624 2018-12-21: Enh. #655 support multiple selection
		//if (diagr == this.mouseSelected) {
		//	this.mouseSelected = null;
		//}
		this.diagramsSelected.remove(diagr);
		// END KGU#624 2018-12-21
		// START KGU#624 2018-12-26: Enh. #655 - Attempt to make search faster
		rootMap.remove(diagr.root, diagr);
		removeFromNameMap(diagr.getName(), diagr);
		// END KGU#624 2018-12-26
		//printNameMap(2813);	// FIXME DEBUG
		// START KGU#626 2018-12-30: Enh. #657
		for (String groupName: diagr.getGroupNames()) {
			Group group = this.groups.get(groupName);
			if (group != null) {
				group.removeDiagram(diagr);
				if (group.isEmpty()) {
					this.groups.remove(groupName);
				}
			}
		}
		// END KGU#626 2018-12-30
		diagrams.remove(diagr);
		// START KGU#701 2019-03-30: Issue #718
		if (diagr.root.isInclude()) {
			for (Root ref: this.findIncludingRoots(diagr.root.getMethodName(), true)) {
				ref.clearVarAndTypeInfo(false);
			}
		}
		// END KGU#701 2019-03-30
		adaptLayout();
		repaint();
		// START KGU#278 2016-10-11: Enh. #267
		notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED | IRoutinePoolListener.RPC_SELECTION_CHANGED);
		Vector<Mainform> mainforms = activeMainforms();
		if (form != null) {
			if (!mainforms.contains(form) && !form.isStandalone() && (!ask || form.diagram.saveNSD(true))) {
				// Form doesn't seem necessary any longer
				form.dispose();
				removeChangeListener(form);
			}
		}
		// END KGU#278 2016-10-11		
	}
	// END KGU#385 2016-12-17
	
	// END KGU#177 2016-04-14: Enh. #158: Allow to copy diagrams via clipboard (as XML)
	/**
	 * Copies the currently selected diagram (single selection provided) to the clipboard.
	 * @return true if the copy was possible (false e.g. in case of an empty or multiple selection)
	 */
	public boolean copyDiagram()
	{
		// START KGU#624 2018-12-21: Enh. #655 - Only a single diagram may be copied at once
		//if (this.mouseSelected !=null)
		boolean okay = this.diagramsSelected.size() == 1;
		if (okay)
		// END KGU#624 2018-12-21
		{
			XmlGenerator xmlgen = new XmlGenerator();
			// START KGU#642 2018-12-21: Enh. #655
			//StringSelection toClip = new StringSelection(xmlgen.generateCode(this.mouseSelected.root,"\t"));
			StringSelection toClip = new StringSelection(
					xmlgen.generateCode(this.diagramsSelected.iterator().next().root,"\t", true)
					);
			// END KGU#624 2018-12-21
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(toClip, this);
		}
		return okay;
	}

	/**
	 * Checks the system clipbard and pasts the content as diagram if it is the text content
	 * of an nsd file (as used to be copied there by {@link #copyDiagram()}).
	 * If successful, then the diagram will be added to the default group. 
	 */
	public void pasteDiagram()
	{
		// See http://www.javapractices.com/topic/TopicAction.do?Id=82
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clip = toolkit.getSystemClipboard();
		Transferable contents = clip.getContents(this);
		String rootXML = "";
		Root root = null;
		if ((contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				rootXML = (String)contents.getTransferData(DataFlavor.stringFlavor);
				InputStream istr = new ByteArrayInputStream(rootXML.getBytes(StandardCharsets.UTF_8));
				NSDParser parser = new NSDParser();
				root = parser.parse(istr);
			}
			catch (Exception ex){
				JOptionPane.showMessageDialog(this, msgParseError.getText() + " " + ex.getMessage(), "Paste Error",
						JOptionPane.ERROR_MESSAGE);

				logger.log(Level.WARNING, msgParseError.getText(), ex);
			}
		}	
		if (root != null)
		{
			addDiagram(root);
			// The content may not have been saved or may come from a different JVM
			root.setChanged(false);
			// START KGU#278 2016-10-11: Enh. #267 / 2016-12-16 Already done in addDiagram()
			//notifyChangeListeners();
			// END KGU#278 2016-10-11    		
		}
	}
	// END KGU#177 2016-04-14

	// START KGU#88 2015-11-24: Provide a possibility to protect diagrams against replacement
	/**
	 * Toggles the "pinned" status of the selected diagrams. If the "pinned" flag
	 * of the selected diagrams differs then all diagrams will be set to pinned.
	 */
	public void togglePinned()
	{
		// START KGU#624 2018-12-21: Enh. #655 - converted t multiple selection 
		//if (this.mouseSelected != null)
		//{
		//	this.mouseSelected.isPinned = !this.mouseSelected.isPinned;
		//	repaint();
		//}
		boolean toPin = false;
		// To flip all diagrams individually may not be what the user expects
		Iterator<Diagram> iter = this.diagramsSelected.iterator();
		// Pin all if at least one unpinned diagram is among the selection 
		while (!toPin && iter.hasNext()) {
			toPin = !iter.next().isPinned;
		}
		for (Diagram diagr: this.diagramsSelected) {
			// This may not be what the user expected
			diagr.isPinned = toPin;
		}
		repaint();
		// END KGU#624 2018-12-21
	}
	// END KGU#88 2015-11-24

	// START KGU#117 2016-03-09: Provide a possibility to mark diagrams as test-covered
	/**
	 * Toggles the "covered" status of the subroutine or includable diagrams among the
	 * selected diagrams.<br/>
	 * If the "covered" flag the selected diagrams differs then all diagrams will be set
	 * "covered". Before unsetting the "covered" flag a confirmation dialog will pop up.
	 * @param frame - the owner for the dialog box.
	 */
	public void setCovered(Frame frame)
	{
		if (this.maySetCovered())
		{
			// START KGU#624 2018-12-21: Enh. #655 - converted to multiple selection
			//if (this.mouseSelected.root.deeplyCovered)
			//{
			//	if (JOptionPane.showConfirmDialog(frame, msgResetCovered.getText()) == JOptionPane.OK_OPTION)
			//	{
			//		this.mouseSelected.root.deeplyCovered = false;
			//	}
			//}
			//else
			//{
			//	this.mouseSelected.root.deeplyCovered = true;
			//	this.mouseSelected.root.setSelected(false);
			//	this.mouseSelected = null;
			//}
			boolean someUncovered = false;
			for (Diagram diagr: this.diagramsSelected) {
				if (diagr.root != null && !diagr.root.isProgram()) {
					if (!diagr.root.deeplyCovered) {
						someUncovered = true;
						break;
					}
				}
			}
			if (someUncovered || JOptionPane.showConfirmDialog(frame, msgResetCovered.getText()) == JOptionPane.OK_OPTION)
			{
				for (Diagram diagr: this.diagramsSelected) {
					if (diagr.root != null && !diagr.root.isProgram()) {
						diagr.root.deeplyCovered = someUncovered;
					}
				}
				// FIXME This is of no good anymore with a multiple selection because selection cannot easily be restored
				if (someUncovered)
				{
					// While the diagram is selected the green color isn't visible - so it might seem to have failed
					// We must clear the selection for consistency reasons therefore.
					this.unselectAll();
				}
			}
			// END KGU#624 2018-12-21
			repaint();
			// START KGU#318 2017-01-05: Enh. #319 Arranger index now reflects test coverage
			// START KGU#408 2021-03-01: Enh. #410 - no need to rebuild the tree
			//this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED | IRoutinePoolListener.RPC_SELECTION_CHANGED);
			this.notifyChangeListeners(IRoutinePoolListener.RPC_STATUS_CHANGED | IRoutinePoolListener.RPC_SELECTION_CHANGED);
			// END KGU#408 2021-03-01
			// END KGU#318 2017-01-05
		}
		else
		{
			JOptionPane.showMessageDialog(frame, msgCoverageError.getText(),
					"Error", JOptionPane.ERROR_MESSAGE, null);   		
		}
	}

	/**
	 * Checks whether the action to toggle the "covered" flag is currently available.
	 * This requires that the run-time analysis mode is switched on and that at least
	 * one subroutine diagram is selected.
	 * @return true if it is sensible to siwtch the "covered" flag of some selected
	 * diagram on or off.
	 */
	public boolean maySetCovered()
	{
		// START KGU#624 2018-12-21: Enh. #655 - For multiple selection, this gets more complicated
		//return Element.E_COLLECTRUNTIMEDATA &&
		//		this.mouseSelected != null &&
		//		this.mouseSelected.root != null &&
		//		!this.mouseSelected.root.isProgram();
		boolean canDo = Element.E_COLLECTRUNTIMEDATA && !this.diagramsSelected.isEmpty();
		if (canDo) {
			canDo = false;
			for (Diagram diagr: this.diagramsSelected) {
				if (diagr.root != null && !diagr.root.isProgram()) {
					canDo = true;
					break;
				}
			}
		}
		return canDo;
		// END KGU#642 2018-12-21
	}
	// END KGU#117 2016-03-09

	/** Creates a new Arranger Surface */
	public Surface()
	{
		initComponents();
		create();
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	//@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		// popup for group names
		JPanel jp = new JPanel();
		jp.setOpaque(true);
		lblPop.setPreferredSize(new Dimension(30,12));
		jp.add(lblPop);
		pop.add(jp);

		this.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		this.setPreferredSize(new Dimension(400, 300));
		// START KGU#497 2018-02-17: Enh. #512
		try {
			// For historical reasons, the zoom scale in ini remains reverse
			this.zoomFactor = 1/Float.parseFloat(Ini.getInstance().getProperty("arrangerZoom", "2.0f"));
		}
		catch (NumberFormatException ex) {
			logger.log(Level.WARNING, "Corrupt zoom factor in ini", ex);
		}
		// END KGU#497 2018-02-17
	}// </editor-fold>//GEN-END:initComponents

	// START KGU#624 2018-12-25: Enh. #655
	/**
	 * @return the number of currently held {@link Diagram}s
	 */
	public int getDiagramCount()
	{
		return this.diagrams.size();
	}
	// END KGU#624 2018-12-25
	
	/**
	 * Returns a vector of all {@link Group}s the given {@link Diagram} is member of
	 * @param _member a {@link Diagram}
	 * @return - the collection of groups containing this diagram
	 */
	public Vector<Group> getGroups(Diagram _member)
	{
		Vector<Group> containingGroups = new Vector<Group>();
		for (Group group: this.groups.values()) {
			if (group.containsDiagram(_member)) {
				containingGroups.add(group);
			}
		}
		return containingGroups;
	}
	
	/**
	 * @return a collection of all available {@link Group}s
	 */
	protected Collection<Group> getGroups()
	{
		return this.groups.values();
	}

	// START KGU#49 2015-10-18: When the window is going to be closed we have to give the diagrams a chance to store their stuff
	// FIXME (KGU): Quick-and-dirty version. More convenient should be a list view with all unsaved diagrams for checkbox selection
	/**
	 * Loops over all administered diagrams and has their respective Mainform (if still alive) saved them in case they are dirty
	 * @param initiator - the component initiating this action
	 * @return Whether saving is complete (or confirmed though being incomplete) 
	 */
	// START KGU#177 2016-04-14: Enh. #158 - report all diagram (file) names without saving possibility
	//public void saveDiagrams()
	public boolean saveDiagrams(Component initiator)
	{
		return saveDiagrams(initiator, this.diagrams, false, false, false);
	}

	/**
	 * Loops over dirty diagrams among {@code diagramsToCheck} or all diagrams (if null) and has their
	 * respective {@link Mainform} (if still alive) saved them. Otherwise uses a temporary {@link Mainform}.
	 * @param initiator - the originating GUI component (where dialogs are to be directed to)
	 * @param diagramsToCheck - collection of the {@link Diagram}s to save if necessary or null (in which
	 * case all diagrams are saved)
	 * @param goingToClose - whether the application is going to close
	 * @param dontAsk - if questions are to be suppressed
	 * @param forArchive - if true then virgin diagrams are saved to a temporary folder, a complete file
	 * chooser won't be offered in such a case.
	 * @return true if all was done or the user has quit the warning message about saving deficiencies.
	 */
	protected boolean saveDiagrams(Component initiator, Collection<Diagram> diagramsToCheck, boolean goingToClose, boolean dontAsk, boolean forArchive)
	// END KGU#177 2016-04-14
	{
		// START KGU#177 2016-04-14: Enh. #158 - a pasted diagram may not have been saved, so warn
		boolean allDone = true;
		StringList unsaved = new StringList();
		// END KGU#177 2016-04-14
		if (diagramsToCheck == null) {
			diagramsToCheck = this.diagrams;
		}
		allDone = saveDiagrams(diagramsToCheck, goingToClose, dontAsk, forArchive, unsaved);
		// START KGU#177 2016-04-14: Enh. #158
		if (!allDone)
		{
			String message = msgUnsavedDiagrams.getText() + "\n" + unsaved.getText();
			int answer = JOptionPane.showConfirmDialog(this, message +
					"\n\n" + msgUnsavedHint.getText() + "\n" +
					msgUnsavedContinue.getText(), "Saving Problem",
					JOptionPane.WARNING_MESSAGE);
			allDone = answer == JOptionPane.YES_OPTION;
		}
		return allDone;
		// END KGU#177 2016-04-14
	}
	// END KGU#49 2015-10-18

	/**
	 * Saves all dirty diagrams among the collection {@code diagrams}
	 * @param diagrams - the collection of {@link Diagram}s to be saved - CAUTION: must not be null!
	 * @param goingToClose - signals whether this method was called because the application is going to shut down 
	 * @param dontAsk - if true then the user won't be asked whether they want to save or not
	 * @param forArchive - if true then diagrams never saved before won't be saved here but later
	 * @param unsaved - a {@link StringList} collecting the signatures of diagrams the saving of which failed
	 * @return true if all affected diagram could be saved or the user accepted the faults
	 */
	private boolean saveDiagrams(Collection<Diagram> diagrams, boolean goingToClose, boolean dontAsk, boolean forArchive, StringList unsaved) {
		boolean allDone = true;
		// START KGU#320 2017-01-04: Bugfix #321
		HashSet<Root> handledRoots = new HashSet<Root>();
		HashSet<Mainform> mainforms = new HashSet<Mainform>();
		// END KGU#320 2017-01-04
		// START KGU#650 2019-02-11: Issue #677
		Mainform tempMainform = null;
		Mainform someMainform = null;
		// END KGU#650 2019-02-11
		// START KGU#534 2018-06-27: Enh. #552
		lu.fisch.structorizer.gui.Diagram.startSerialMode();
		// START KGU#901 2020-12-30: Issue #901
		Cursor origCursor = getCursor();
		// END KGU#901 2020-12-30
		try {
			// END KGU#534 2018-06-27
			Iterator<Diagram> iter = diagrams.iterator();
			while (iter.hasNext())
			{
				Diagram diagram = iter.next();
				Mainform form = diagram.mainform;
				// START KGU#650 2019-02-11: Issue #677 - Ensure a Mainform if we have to archive diagrams
				// START KGU#749 2019-10-13: Bugfix #763 - make sure the file actually exists
				//boolean hasFile = diagram.root.filename != null && !diagram.root.filename.trim().isEmpty();
				boolean hasFile = diagram.root.getFile() != null;
				// END KGU#749 2019-10-13
				if (form == null && hasFile && forArchive && (form = someMainform) == null) {
					form = someMainform = tempMainform = new Mainform(false);
				}
				// START KGU#901 2020-12-30: Issue #901
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				// END KGU#901 2020-12-30
				// Postpone the saving of virgin diagrams here in case of saving to an archive (will be done later)
				//if (form != null)
				if (form != null && (hasFile || !forArchive)) 
				// END KGU#650 2019-02-11
				{
					// START KGU#320 2017-01-04: Bugfix #321 (?) A Mainform may own several diagrams here!
					//form.diagram.saveNSD(!goingToClose || !Element.E_AUTO_SAVE_ON_CLOSE);
					if (!form.diagram.saveNSD(diagram.root, !dontAsk && !(goingToClose && Element.E_AUTO_SAVE_ON_CLOSE))
							&& JOptionPane.showConfirmDialog(form.getFrame(),
									Menu.msgCancelAll.getText(),
									Menu.ttlCodeImport.getText(),
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					{
						break;
					}
					// START KGU#650 2019-02-11: Issue #677
					if (form != tempMainform) {
						mainforms.add(form);
						if (someMainform == null) {
							someMainform = form;
						}
					}
					// END KGU#650 2019-02-11
					handledRoots.add(diagram.root);
					// END KGU#320 2017-01-04
				}
				// START KGU#177 2016-04-14: Enh. #158 - a pasted diagram may not have been saved, so warn
				else if (!hasFile)
				{
					if (!forArchive) {
						unsaved.add("( " + diagram.root.proposeFileName() + " ?)");
						allDone = false;
					}
				}
				else if (diagram.root.hasChanged())
				{
					unsaved.add(diagram.root.filename);
					allDone = false;
				}
				// END KGU#177 2016-04-14
			}
			// START KGU#650 2019-02-11: Issue #677
			if (tempMainform != null) {
				// START KGU#745 2019-10-14: Bugfix #759 - we must also remove it from the listeners (had added itself)
				removeChangeListener(tempMainform);
				// END KGU#745 2019-10-14
				tempMainform.dispose();
			}
			// END KGU#650 2019-02-11
			// START KGU#320 2017-01-04: Bugfix #321
			// In case Arranger is closing give all dependent (and possibly doomed) Mainforms a
			// chance to save their currently maintained Root even if this was not arranged here.
			if (goingToClose) {
				for (Mainform form: mainforms) {
					if (!form.isStandalone() && !handledRoots.contains(form.getRoot())) {
						form.diagram.saveNSD(!(goingToClose && Element.E_AUTO_SAVE_ON_CLOSE));
					}
				}
			}
			// END KGU#320 2017-01-04
			// START KGU#534 2018-06-29: Enh. #552
		}
		finally {
			// START KGU#901 2020-12-30: Issue #901
			setCursor(origCursor);
			// END KGU#901 2020-12-30
			lu.fisch.structorizer.gui.Diagram.endSerialMode();
		}
		// END KGU#534 2018-06-29
		return allDone;
	}

	// START KGU#534 2018-06-27: Enh. #552 - new opportunity to clear the entire Arranger
	/**
	 * Removes all diagrams from the Arranger surface after having asked for changes to
	 * be saved.
	 * @param initiator - the component initiating this action
	 * @return whether this action was complete.
	 */
	public boolean removeAllDiagrams(Component initiator)
	{
		boolean allDone = false;
		if (saveDiagrams(initiator)) {
			try {
				while (!diagrams.isEmpty()) {
					Diagram diagr = diagrams.firstElement();
					diagr.root.removeUpdater(this);
					// START KGU#624 2018-12-21: Enh. #655 no we allow multiple selection
					//if (diagr == this.mouseSelected) {
					//	this.mouseSelected = null;
					//}
					this.diagramsSelected.remove(diagr);
					// END KGU#624 2018-12-21
					// START KGU#624 2018-12-26: Enh. #655
					this.rootMap.remove(diagr.root);
					this.removeFromNameMap(diagr.root.getMethodName(), diagr);
					// END KGU#624 2018-12-26
					diagrams.remove(diagr);
				}
				// START KGU#626 2018-12-31: Enh. #657
				groups.clear();
				// END KGU#626 2018-12-31
			}
			finally {
				// START KGU#626 2018-12-31: Enh. #657
				if (!groups.isEmpty()) {
					cleanupGroups();
				}
				// END KGU#626 2018-12-31
				adaptLayout();
				repaint();
				notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED | IRoutinePoolListener.RPC_SELECTION_CHANGED);
			}
			allDone = true;
		}
		return allDone;
	}
	// END KGU#534 2018-06-27

	// START KGU#626 2018-12-31: Enh. #657
	/**
	 * This removes all stale diagram references from the remaining groups and
	 * all empty groups from the group table.
	 */
	private void cleanupGroups() {
		StringList doomedGroups = new StringList();
		for (Entry<String, Group> groupEntry: groups.entrySet()) {
			// Remove all member diagrams that aren't held in the total diagram vector anymore
			Group group = groupEntry.getValue();
			for (Diagram member: group.getDiagrams()) {
				if (!diagrams.contains(member)) {
					// This is save because group.getDiagrams() returned a copied set.
					group.removeDiagram(member);
				}
			}
			if (group.isEmpty()) {
				doomedGroups.add(groupEntry.getKey());
			}
		}
		for (int i = 0; i < doomedGroups.count(); i++) {
			groups.remove(doomedGroups.get(i));
		}
	}
	// END KGU#626 2018-12-31
	
	// START KGU#630 2019-01-12: Enh. #662/3
	/**
	 * Re-arranges all diagrams group by group.
	 */
	protected void rearrange()
	{
		// Keep track of all diagrams already re-arranged (diagram my be shared among groups)
		Set<Diagram> rearrangedDiagrams = new HashSet<Diagram>();

		// First remove all diagrams from the vector (they should all be held by the groups as well)
		diagrams.clear();

		// Now traverse the groups and start a new level for every group
		int groupOffsetY = 0;	// height offset of the current group
		Group defaultGroup = null;	// Place the default group last
		for (Group group: groups.values()) {
			if (group.isDefaultGroup()) {
				defaultGroup = group;
				continue;
			}
			groupOffsetY = rearrangeGroup(group, rearrangedDiagrams, groupOffsetY);
		}
		if (defaultGroup != null) {
			rearrangeGroup(defaultGroup, rearrangedDiagrams, groupOffsetY);
		}
		this.adaptLayout();
		this.repaint();
		this.notifyChangeListeners(IRoutinePoolListener.RPC_POSITIONS_CHANGED);
	}
	// END KGU#630 2019-01-12

	/**
	 * Rearranges the diagrams of the given {@code group} not contained in set {@code rearrangedDiagrams}
	 * below {@code groupOffsetY}, thereby adds these member diagrams both to field {@link #diagrams} again
	 * (they must have been removed from the vector before or - if shared with an already rearranged group -
	 * have been added to {@code rearrangedDiagrams}) and set {@code rearrangedDiagrams}.
	 * @param group - the {@link Group} to be rearranged
	 * @param rearrangedDiagrams - the set of already rearranged {@link Diagram}s - should contain exactly
	 * the same diagrams as {@link #diagrams} but is more efficient to be searched.
	 * @param groupOffsetY - the y coordinate beneath which the diagrams for this group are to be placed
	 * @return  the bottom coordinate of the group bounds after rearrangement.
	 */
	private int rearrangeGroup(Group group, Set<Diagram> rearrangedDiagrams, int groupOffsetY) {
		for (Diagram diagr: group.getDiagrams()) {
			if (rearrangedDiagrams.contains(diagr)) {
				continue;
			}
			LinkedList<Point> silhouette = new LinkedList<Point>();
			silhouette.add(new Point(0, groupOffsetY));
			silhouette.add(new Point(Integer.MAX_VALUE, 0));
			this.getDrawingRect(silhouette);
			Rect rec = diagr.root.getRect();
			Point newPoint = this.findPreferredLocation(silhouette, rec.getRectangle());
			diagr.setLocation(newPoint.x, newPoint.y);
			diagrams.add(diagr);
			rearrangedDiagrams.add(diagr);
		}
		groupOffsetY = getDrawingRect(null).bottom;
		return groupOffsetY;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		Diagram diagr1 = null;
		if (this.diagramsSelected.size() == 1) {
			diagr1 = this.diagramsSelected.iterator().next();
		}
		// Don't change selection on right click!
		if (e.getButton() != 3) {
			if (e.getClickCount()==2)
			{
				// Double-click action
				if (diagr1 == null) {
					// Don't do anything if no diagram is selected
					return;
				}
				// create editor
				Mainform form = diagr1.mainform;
				// This loop is a precaution against stale Mainform (bugfix #132)
				int nAttempts = 0;
				do {
					try
					{
						// An attached Mainform might refuse to re-adopt the root
						if (form==null || !form.setRoot(diagr1.root))
						{
							// Start a dependent Mainform not willing to kill us
							form = new Mainform(false);
							form.addWindowListener(this);
							form.setRoot(diagr1.root);
						}

						// store Mainform reference in diagram
						diagr1.mainform = form;

						// register this as "updater"
						diagr1.root.addUpdater(this);

						form.setVisible(true);
						this.addChangeListener(form);
					}
					catch (Exception ex)
					{
						// Seems the Mainform was stale (closed without having been cleared)
						form = null;
					}
				} while (form == null && nAttempts++ < 2);

				adaptLayout();
				this.repaint();
			}
			else {
				boolean ctrlDown = e.isControlDown();
				if (!ctrlDown && !e.isShiftDown()) {
					// Without Control button all previous selection is to be cleared 
					this.unselectAll();
				}
				// With zooming we need the virtual mouse coordinates (Enh. #512)
				int mouseX = Math.round(e.getX() / this.zoomFactor);
				int mouseY = Math.round(e.getY() / this.zoomFactor);
				// START KGU#633 2019-01-09: Enh. #662/2
				if (!(drawGroups && selectGroups)) {
					Diagram diagram = getHitDiagram(mouseX, mouseY);
					if (diagram != null)
					{
						Root root = diagram.root;
						if (ctrlDown && diagramsSelected.contains(diagram)) {
							root.setSelected(false, Element.DrawingContext.DC_ARRANGER);
							diagramsSelected.remove(diagram);
						}
						else {
							root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
							diagramsSelected.add(diagram);
						}
					}
				}
				else {
					Set<Group> hitGroups = this.getHitGroups(mouseX, mouseY);
					if (ctrlDown) {
						HashSet<Group> groupsToSelect = new HashSet<Group>(groupsSelected);
						for (Group group: hitGroups) {
							if (groupsSelected.contains(group)) {
								groupsToSelect.remove(group);
							}
							else {
								groupsToSelect.add(group);
							}
						}
						this.unselectAll();
						this.selectGroups(groupsToSelect);
					}
					else {
						this.selectGroups(hitGroups);
					}
				}
				this.notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
				repaint();			
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		// START KGU#896 2020-12-23: Enh. #896 Unambiguous indication of what will happen on dragging
		Point mousePt = e.getPoint();
		int trueX = Math.round(mousePt.x / zoomFactor);
		int trueY = Math.round(mousePt.y / zoomFactor);
		Diagram diagram = getHitDiagram(trueX, trueY);
		if (dragArea == null && (diagram != null && (this.diagramsSelected.isEmpty() || this.diagramsSelected.contains(diagram)))) {
			setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}
		// END KGU#896 2020-12-23
		/* The interesting things happen in mouseClicked() and mouseDragged() */
		// START KGU#626 2018-12-23: Enh. #657
		showPopupMenuIfTriggered(e);
		// END KGU#626 2018-12-23
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		// We must reset the last drag information lest mouseDragged() should run havoc
		if (dragPoint != null) {
			this.notifyChangeListeners(IRoutinePoolListener.RPC_POSITIONS_CHANGED);
		}
		// START KGU#896 2020-12-23: Enh. #896 Unambiguous indication of what will happen on dragging
		if (getCursor().getType() != Cursor.DEFAULT_CURSOR) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		// END KGU#896 2020-12-23
		dragPoint = null;
		if (dragArea != null) {
			// Select all contained diagrams
			Set<Diagram> foundDiagrams = this.getContainedDiagrams(dragArea);
			if (! e.isShiftDown()) {
				this.unselectAll();
			}
			this.selectSet(foundDiagrams);
		}
		dragArea = null;
		
		// START KGU#626 2018-12-23: Enh. #657
		showPopupMenuIfTriggered(e); 
		// END KGU#626 2018-12-23
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		pop.setVisible(false);
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		int mouseX = e.getX();
		int mouseY = e.getY();
		if (mouseX > 0 && mouseY > 0)	// Don't let something be dragged beyond the scrollable area
		{
			// Make sure the viewport moves with the mouse
			Rectangle rect = new Rectangle(mouseX, mouseY, 1, 1);
			scrollRectToVisible(rect);

			// Now we need the virtual coordinates
			mouseX = Math.round(mouseX / zoomFactor);
			mouseY = Math.round(mouseY / zoomFactor);
			
			Diagram diagram = getHitDiagram(mouseX, mouseY);
			Point oldMousePoint = this.dragPoint;
			
			// Don't drag diagrams if the mouse isn't above one of the selected diagrams
			if (dragArea == null && (oldMousePoint != null || diagram != null && (this.diagramsSelected.isEmpty() || this.diagramsSelected.contains(diagram)))) {
				this.dragPoint = new Point(mouseX, mouseY);
				if (this.diagramsSelected.isEmpty()) {
					this.diagramsSelected.add(diagram);
					diagram.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
				}
				// Don't do anything if there is no former dragging position (cleared by mouseReleased())
				if (oldMousePoint != null) {
					int deltaX = mouseX - oldMousePoint.x;
					int deltaY = mouseY - oldMousePoint.y;
					// Now move all selected diagrams synchronously
					moveSelection(deltaX, deltaY);
				}
			}
			else if (this.dragPoint == null) {
				// Draw a selection area
				if (dragArea == null) {
					dragArea = new Rectangle(mouseX, mouseY, 1, 1);
				}
				else {
					dragArea.add(mouseX, mouseY);
				}
				repaint();
			}
		}
	}
	
	
	/**
	 * Moves the selected diagrams by the specified amounts of pixels. Ensures that the
	 * resulting coordinates of the diagrams won't be negative.
	 * @param deltaX - pixels to the right (or left if negative)
	 * @param deltaY - pixels down (or up if negative)
	 */
	protected void moveSelection(int deltaX, int deltaY) {
		for (Diagram diagr: this.diagramsSelected) {
			// No diagram is allowed to be shifted outside the reachable area
			int newX = Math.max(0, diagr.point.x + deltaX);
			int newY = Math.max(0, diagr.point.y + deltaY);
			diagr.setLocation(newX, newY);
		}
		adaptLayout();
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e)
	{
		// START KGU#896 2020-12-23: Enh. #896 Dragging has definitely ended now
		if (getCursor().getType() != Cursor.DEFAULT_CURSOR) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		// END KGU#896 2020-12-23
		if (this.drawGroups) {
			pop.setVisible(false);
			int x = (int)(e.getX() / this.zoomFactor);
			int y = (int)(e.getY() / this.zoomFactor);
			Set<Group> hitGroups = this.getHitGroups(x, y);
			if (!hitGroups.isEmpty()) {
				StringList groupNames = new StringList();
				StringList htmlContent = new StringList();
				for (Group group: hitGroups) {
					String groupName = group.getName().replace(Group.DEFAULT_GROUP_NAME, ArrangerIndex.msgDefaultGroupName.getText());
					groupNames.add(groupName);
					htmlContent.add("<span style=\"color: #" + Integer.toHexString(group.getColor().getRGB()).substring(2) + ";\">"
							+ BString.encodeToHtml(groupName)
							+ "</span>");
				}
				lblPop.setText("<html>" + htmlContent.concatenate(", ") + "</html>");
				lblPop.setPreferredSize(
						new Dimension(
								8 + lblPop.getFontMetrics(lblPop.getFont()).
								stringWidth(groupNames.concatenate(", ")),
								lblPop.getFontMetrics(lblPop.getFont()).getHeight()
								)
						);
				x = ((JComponent) e.getSource()).getLocationOnScreen().getLocation().x;
				y = ((JComponent) e.getSource()).getLocationOnScreen().getLocation().y;
				pop.setLocation(x+e.getX(),
						y+e.getY()+16);
				pop.setVisible(true);
			}
			
		}
	}
	
	// START KGU#626 2018-12-23: Prepared for enh. #657
	private void showPopupMenuIfTriggered(MouseEvent e) 
	{
		if (e.isPopupTrigger()) 
		{
			if (Arranger.popupMenu != null) {
				Arranger.popupHitList.removeAll();
				// START KGU#630 2019-01-09: Enh #662/2
				if (!(drawGroups && selectGroups)) {
				// END KGU#630 2019-01-09
					List<Diagram> hitDiagrs = this.getHitDiagrams(Math.round(e.getX() / zoomFactor), Math.round(e.getY() / zoomFactor));
					for (Diagram diagr: hitDiagrs) {
						String description = diagr.root.getSignatureString(false, false);
						javax.swing.JMenuItem menuItem = new javax.swing.JMenuItem(description, diagr.root.getIcon());
						menuItem.setToolTipText(msgTooltipSelectThis.getText()
								.replace("%1", msgDiagram.getText().replace("%", description))
								.replace("%2", ""));
						menuItem.addActionListener(new java.awt.event.ActionListener() {
							@Override
							public void actionPerformed(ActionEvent evt) {
								if ((evt.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
									unselectAll();
								}
								diagr.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
								diagramsSelected.add(diagr);
								if (diagrams.remove(diagr)) {
									diagrams.add(diagr);
								}
								notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
							}});
						Arranger.popupHitList.add(menuItem);
					}
				// START KGU#630 2019-01-09: Enh #662/2
				}
				if (drawGroups) {
					Set<Group> hitGroups = this.getHitGroups(Math.round(e.getX() / zoomFactor), Math.round(e.getY() / zoomFactor));
					String tooltipText = msgTooltipSelectThis.getText();
					int pos2 = tooltipText.indexOf("%2");
					if (pos2 > 0) {
						tooltipText = tooltipText.substring(0, pos2);
					}
					for (Group group: hitGroups) {
						String description = group.getName().replace(Group.DEFAULT_GROUP_NAME, ArrangerIndex.msgDefaultGroupName.getText());
						javax.swing.JMenuItem menuItem = new javax.swing.JMenuItem(description, group.getIcon(true));
						menuItem.setToolTipText(tooltipText.replace("%1", msgGroup.getText().replace("%", description)));
						menuItem.addActionListener(new java.awt.event.ActionListener() {
							@Override
							public void actionPerformed(ActionEvent evt) {
								if ((evt.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
									unselectAll();
								}
								for (Diagram diagr: group.getDiagrams()) {
									diagr.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
									diagramsSelected.add(diagr);
									if (diagrams.remove(diagr)) {
										diagrams.add(diagr);
									}
								}
								notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
							}});
						Arranger.popupHitList.add(menuItem);
					}
				}
				// END KGU#630 2019-01-09
				pop.setVisible(false);
				Arranger.popupMenu.show(e.getComponent(), e.getX(), e.getY());					
			}
		}
	}
	// END KGU#626 2018-12-23
	
	// START KGU#624 2018-12-22: Enh. #655
	/**
	 * Identifies the top diagram under the mouse cursor (if any). An eclipsed diagram
	 * will never be returned here
	 * @param trueX - the true X coordinate of the diagrams (zooming compensated)
	 * @param trueY - the true Y coordinate of the diagrams (zooming compensated)
	 * @return the only diagram not eclipsed at mouse position (if any, otherwise null)
	 */
	private Diagram getHitDiagram(int trueX, int trueY)
	{
		Diagram hitDiagram = null;
		for (int d = diagrams.size()-1; d >= 0 && hitDiagram == null; d--)
		{
			Diagram diagram = diagrams.get(d);
			// START KGU#911 2021-01-13: Enh. #910: Don't select an invisible diagram
			if (!isVisible(diagram)) {
				continue;
			}
			// END KGU#911 2021-01-13
			Root root = diagram.root;

			Element ele = root.getElementByCoord(
					trueX - diagram.point.x,
					trueY - diagram.point.y);
			if (ele != null)
			{
				hitDiagram = diagram;
			}
		}
		return hitDiagram;
	}

	/**
	 * Returns the list of all diagrams under the mouse cursor (also the eclipsed ones)
	 * from top to bottom.
	 * @param trueX - the true X coordinate of the diagrams (zoom compensated)
	 * @param trueY - the true Y coordinate of the diagrams (zoom compensated)
	 * @return The list of the diagrams enclosing the mouse position.
	 * @see #getHitDiagram(int, int)
	 * @see #getHitGroups(int, int)
	 */
	private List<Diagram> getHitDiagrams(int trueX, int trueY)
	{
		List<Diagram> hitDiagrams = new LinkedList<Diagram>();
		for (int d = diagrams.size()-1; d >= 0; d--)
		{
			Diagram diagram = diagrams.get(d);
			// START KGU#911 2021-01-13: Enh. #910: Don't select an invisible diagram
			if (!isVisible(diagram)) {
				continue;
			}
			// END KGU#911 2021-01-13
			Root root = diagram.root;

			Element ele = root.getElementByCoord(
					trueX - diagram.point.x,
					trueY - diagram.point.y);
			if (ele != null)
			{
				hitDiagrams.add(diagram);
			}
		}
		return hitDiagrams;
	}
	
	/**
	 * Returns the set of all groups under the mouse cursor.
	 * @param trueX - the true X coordinate (zoom compensated)
	 * @param trueY - the true Y coordinate (zoom compensated)
	 * @return The set of the groups enclosing the mouse position.
	 * @see #getHitDiagram(int, int)
	 * @see #getHitDiagrams(int, int)
	 */
	private Set<Group> getHitGroups(int trueX, int trueY)
	{
		Set<Group> hitGroups = new HashSet<Group>();
		for (Group group: this.groups.values())
		{
			if (group.bounds != null && group.bounds.contains(trueX, trueY))
			{
				hitGroups.add(group);
			}
		}
		return hitGroups;
	}
	
	/**
	 * Identifies all diagrams that are contained in the given {@code bounds} 
	 * @param bounds - a {@link Rectangle} in true coordinates (zoom compensated!)
	 * @return the set of diagrams contained in {@code bounds} 
	 */
	private Set<Diagram> getContainedDiagrams(Rectangle bounds)
	{
		Set<Diagram> containedDiagrams = new HashSet<Diagram>();
		
		for (Diagram diagram: diagrams) {
			Root root = diagram.root;
			if (root != null) {
				Rectangle rect = root.getRect(diagram.point).getRectangle();
				if (bounds.contains(rect.x, rect.y,
						(rect.width + 1), (rect.height+1))) {
					containedDiagrams.add(diagram);
				}
			}
		}
		
		return containedDiagrams;
	}
	// END KGU#624 2018-12-22

	// START KGU#624 2018-12-21: Enh. #655 - multiple selection opportunity
	/**
	 * Unselects all available diagrams and repaints
	 */
	public void unselectAll() {
		// START KGU#630 2019-01-09: Enh. #622/2
		this.groupsSelected.clear();
		// END KGU#630 2019-01-09
		this.diagramsSelected.clear();
		for (Diagram diagr: this.diagrams) {
			if (diagr.root != null) {
				diagr.root.setSelected(false, Element.DrawingContext.DC_ARRANGER);
			}
		}
		this.notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED); 
		repaint();
	}
	
	/**
	 * Selects all available groups or diagrams and repaints
	 */
	public void selectAll() {
		// START KGU#630 2019-01-9: Enh. #622/2
		if (this.drawGroups && this.selectGroups) {
			this.groupsSelected.addAll(this.groups.values());
		}
		// END KGU#630 2019-01-09
		this.diagramsSelected.addAll(this.diagrams);
		for (Diagram diagr: this.diagrams) {
			if (diagr.root != null) {
				diagr.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
			}
		}
		notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
		repaint();
	}

	/**
	 * Selects all given diagrams and repaints
	 * @param diagrSet - set of diagrams to be added to the selection
	 */
	public void selectSet(Collection<Diagram> diagrSet) {
		this.diagramsSelected.addAll(diagrSet);
		for (Diagram diagr: diagrSet) {
			if (diagr.root != null) {
				diagr.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
			}
		}
		this.notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
		repaint();
	}
	// END KGU#624 2018-12-21
	
	// START KGU#630 2019-01-09: Enh. #662/2
	/**
	 * Adds all given groups as well as their member diagrams to the respective
	 * selection sets and repaints
	 * @param groupSet
	 */
	public void selectGroups(Collection<Group> groupSet)
	{
		this.groupsSelected.addAll(groupSet);
		for (Group group: groupSet) {
			selectSet(group.getDiagrams());
		}
	}
	// END KGU#630 2019-01-09

	/**
	 * Repaints the given {@link Root} (as reported to have been subject to changes).
	 * Also notifies all registered {@link IRoutinePoolListener}s.
	 * Reorganizes some mappings and cached references if necessary.
	 * @param source - the structogram that reported to have been modified
	 */
	public void update(Root source)
	// START KGU#804 2020-02-17: Bugfix #818
	{
		update(source, false);
	}
	
	/**
	 * Repaints the given {@link Root} (as reported to have been subject to changes).
	 * Also notifies all registered {@link IRoutinePoolListener}s.
	 * Reorganizes some mappings and cached references if necessary.
	 * @param source - the structogram that reported to have been modified
	 * @param replaced - whether it is rather a replacement than a modification
	 * @see #update(Root)
	 */
	private void update(Root source, boolean replaced)
	// END KGU#884 2020-02-17
	{
		// START KGU#85 2015-11-18
		adaptLayout();
		// END KGU#85 2015-11-18
		this.repaint();
		// START KGU#330 2017-01-13: We keep redundant information to be able to trigger change notifications
		// START KGU#624 2018-12-26: Enh. #655 - Attempt to make search faster
		//Diagram diagr = this.findDiagram(source, 1);
		Diagram diagr = rootMap.get(source);
		// END KGU#624 2018-12-26
		// START KGU#624 2018-12-26: Enh. #655
		//if (diagr != null && diagr.checkSignatureChange()) {
		//	this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		//}
		if (diagr != null) {
			String oldRootName = diagr.getName();
			if (diagr.checkSignatureChange()) {
				int changes = IRoutinePoolListener.RPC_POOL_CHANGED
						| IRoutinePoolListener.RPC_STATUS_CHANGED;
				String newRootName = source.getMethodName();
				if (!oldRootName.equals(newRootName)) {
					removeFromNameMap(oldRootName, diagr);
					addToNameMap(newRootName, diagr);
					//printNameMap(3953);	// FIXME DEBUG
					changes |= IRoutinePoolListener.RPC_NAME_CHANGED;
				}
				// START KGU#626 2018-12-31: Update the root lists in the groups
				for (String groupName: diagr.getGroupNames()) {
					Group group = this.groups.get(groupName);
					if (group != null) {
						// START KGU#804 2020-02-17: Bugfix #818
						//group.updateSortedRoots(false);
						group.updateSortedRoots(replaced);
						// END KGU#804 2020-02-17
					}
				}
				// END KGU#626 2018-12-31
				this.notifyChangeListeners(changes);
			}
			// START KGU#650 2019-02-11: Issue #677 Keep track of changed archive members residing outside
			if (source.hasChanged()) {
				for (String groupName: diagr.getGroupNames()) {
					Group group = this.groups.get(groupName);
					File arrzFile = null;
					if (group != null && !group.membersChanged && (arrzFile = group.getArrzFile(false)) != null) {
						if (!source.getPath(true).equals(arrzFile.getAbsolutePath())) {
							group.membersChanged = true;
						}
					}
				}
			}
			// END KGU#650 2019-02-11
		}
		// END KGU#624 2018-12-26
		// END KGU#330 2017-01-13
	}

	// START KGU#155 2016-03-08: Bugfix #97 extension
	/**
	 * Invalidates the cached prepareDraw info of all diagrams residing here
	 * (to be called on events with heavy impact on the size or shape of some
	 * Elements)
	 * @param _exceptDiagr the hash code of a lu.fisch.structorizer.gui.Diagram
	 * that is not to be invoked (to avoid recursion)
	 */
	public void resetDrawingInfo(int _exceptDiagr)
	{
		if (this.diagrams != null)
		{
			for (int d = 0; d < this.diagrams.size(); d++)
			{
				this.diagrams.get(d).resetDrawingInfo(_exceptDiagr);
			}
		}
	}
	// END KGU#155 2016-03-08

	// START KGU#2 2015-11-19: We now need a way to identify a diagram - a root should not be twice here
	// START KGU#119 2016-01-02: Bugfix #78 Under certain circumstances, even the equality has to be avoided
	//private Diagram findDiagram(Root root)
	// START KGU#312 2016-12-29: Enh. #315 - more differentiated equivalence check
	//private Diagram findDiagram(Root root, boolean identityCheck)
	/**
	 * Search among the held {@link Diagram} objects for one the associated {@link Root} of which
	 * closely enough resembles the given {@link Root} {@code root}.
	 * 
	 * @param root - the Nassi-Shneiderman diagram we look for
	 * @param equalityLevel - the level of resemblance - see {@link Root#compareTo(Root)}
	 * @return the first {@link Diagram} that holds a matching {@link Root}.
	 * 
	 * @see #findDiagram(Root, int, boolean)
	 * @see #findDiagramsByName(String)
	 * @see #findIncludesByName(String, Root, boolean)
	 */
	private Diagram findDiagram(Root root, int equalityLevel)
	{
		return findDiagram(root, equalityLevel, false);
	}
	/**
	 * Search among the held {@link Diagram} objects for one the associated {@link Root} of which
	 * closely enough resembles the given {@link Root} {@code root}.
	 * 
	 * @param root - the Nassi-Shneiderman diagram we look for
	 * @param equivalenceLevel - the level of resemblance - see {@link Root#compareTo(Root)}
	 * @param warnLevel2AndAbove - if true then a warning will be raised if the obtained equality level is not 1 
	 * @return the first {@link Diagram} that holds a matching {@link Root}.
	 * 
	 * @see #findDiagram(Root, int)
	 * @see #findDiagramsByName(String)
	 * @see #findIncludesByName(String, Root, boolean)
	 */
	private Diagram findDiagram(Root root, int equivalenceLevel, boolean warnLevel2andAbove)
	// END KGU#312 2016-12-29
	// END KGU#119 2016-01-02
	{
		Diagram owner = null;
		Vector<Diagram> diagramList = this.diagrams;
		// START KGU#624 2018-12-26: Enh. #655 - Attempt to make search faster
		// START KGU#700 2019-03-28: Enh. #657 - This didn't make sense
		/* If we find the root itself then it shouldn't matter whether there are similar ones */
		//if (equalityLevel == 1) {
		//	return this.rootMap.get(root);
		//}
		owner = this.rootMap.get(root);
		if (owner != null || equivalenceLevel == 1) {
			// Identity case
			return owner;
		}
		// END KGU#700 2019-03-28
		else if (equivalenceLevel == 2 || equivalenceLevel == 5) {
			// Either equality or at least signature compatibility required
			// We try to reduce the number of diagrams to be searched  
			diagramList = this.nameMap.get(root.getMethodName());
		}
		// END KGU#624 2018-12-26
		// START KGU#1124 2024-03-15: Issue #1138 large imports could provoke an n² message nuisance
		Vector<String[]> collisionInfo = null;
		// END KGU#1124 2024-03-14
		if (diagramList != null) {
			// START KGU#1124 2024-03-16: Issue #1138 get a complete collision set
			//for(int d = 0; owner == null && d < diagramList.size(); d++)
			for(int d = 0; (owner == null || warnLevel2andAbove) && d < diagramList.size(); d++)
			// END KGU#1124 2024-03-16
			{
				Diagram diagram = diagramList.get(d);
				// START KGU#312 2016-12-29: Enh. #315
				// START KGU#119 2016-01-02: Bugfix #78 When loading diagrams we ought to check for equality only
				//if (diagram.root == root)
				//if (identityCheck && diagram.root == root ||
				//		!identityCheck && diagram.root.equals(root))
				// END KGU#119 2016-01-02
				//{
				//	owner = diagram;	// Will leave the loop
				//}
				int resemblance = diagram.root.compareTo(root);
				if (resemblance > 0) {
					if (resemblance > 2 && warnLevel2andAbove) {
						String fName = diagram.root.filename.toString();
						if (fName.trim().isEmpty()) {
							fName = "[" + diagram.root.proposeFileName() + "]";
						}
						// START KGU#1124 2024-03-13: Issue #1138 Just raise the conflict viewer
						//String message = msgInsertionConflict[resemblance-3].getText().
						//		replace("%1", root.getSignatureString(false, false)).
						//		replace("%2", fName);
						//JOptionPane.showMessageDialog(this.getParent(), message,
						//		this.titleDiagramConflict.getText(),
						//		JOptionPane.WARNING_MESSAGE);
						if (collisionInfo == null) {
							collisionInfo = new Vector<String[]>();
						}
						collisionInfo.add(new String[] {
								Integer.toString(collisionInfo.size() + 1),
								SYMBOLS_CONTENT_DIFF[resemblance-3],
								SYMBOLS_PATH_DIFF[resemblance-3],
								diagram.root.getSignatureString(false, true),
								fName,
								new StringList(diagram.getGroupNames()).concatenate(", ")
						});
						// END KGU#1124 2024-03-13
					}
					if (resemblance <= equivalenceLevel)
					{
						owner = diagram;
					}
				}
				// END KGU#312 2016-12-29
			}
		}
		// START KGU#1124 2024-03-15: Issue #1138
		if (collisionInfo != null
				&& (!lu.fisch.structorizer.gui.Diagram.isInSerialMode()
						|| lu.fisch.structorizer.gui.Diagram.getSerialDecision(
								lu.fisch.structorizer.gui.Diagram.SerialDecisionAspect.SERIAL_ARRANGE) != 
								lu.fisch.structorizer.gui.Diagram.SerialDecisionStatus.NO_TO_ALL)) {
			boolean waived = showArrangementCollisions(root, collisionInfo);
			if (waived) {
				lu.fisch.structorizer.gui.Diagram.setSerialDecision(
						lu.fisch.structorizer.gui.Diagram.SerialDecisionAspect.SERIAL_ARRANGE,
						false);
			}
		}
		// END KGU#1134 2024-03-15
		return owner;
	}
	// END KGU#2 2015-11-19
	
	// START KGU#1124 2024-03-15: Issue #1138 Present colision info in a more useful way
	/**
	 * Presents the passed-in set of collision information for the given {@link}
	 * @param root - the compared Nassi-Shneiderman diagram
	 * @param collisionInfo - a list of string arrays containing information about
	 *    similar diagrams
	 * @return {@code true} if the 
	 */
	private boolean showArrangementCollisions(Root root, Vector<String[]> collisionInfo) {
		JPanel pnColl = new JPanel();
		JTable tblColl = new JTable();
		Object[][] content = collisionInfo.toArray(new Object[collisionInfo.size()][6]);
		tblColl.setModel(new javax.swing.table.DefaultTableModel(
				content,
				new String [] {
						"#",
						"Δ " + titleConflContentDiff.getText(),
						"Δ " + titleConflPathDiff.getText(),
						titleConflSignature.getText(),
						titleConflPath.getText(),
						titleConflGroups.getText()
				}
				) {
			@Override
			public boolean isCellEditable(int row, int column)
			{
				// Make the table non-editable (though it wouldn't cause harm)
				return false;
			}
		});
		tblColl.setGridColor(Color.LIGHT_GRAY);
		tblColl.setShowGrid(true);
		tblColl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//scrTable.setViewportView(tblColl);
		JScrollPane scrTable = new JScrollPane(tblColl);
		//scrTable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnColl.setLayout(new BorderLayout(5, 5));
		pnColl.add(new JLabel(msgInsertionConflict.getText()
				.replace("%", root.getSignatureString(false, true))), BorderLayout.NORTH);
		pnColl.add(scrTable, BorderLayout.CENTER);
		// Work out the column widths
		int colCount = tblColl.getColumnCount();
		int columnWidth[] = new int[colCount];
		for (int i = 0; i < colCount; i++) {
			columnWidth[i] = getMaxColumnWidth(tblColl, i, true, 10);
		}
		// Now set the preferred column widths accordingly
		TableColumnModel tableColumnModel = tblColl.getColumnModel();
		for (int i = 0; i < colCount; i++) {
			TableColumn tableColumn = tableColumnModel.getColumn(i);
			tableColumn.setPreferredWidth(columnWidth[i]);
		}
		int stdHeight = tblColl.getRowHeight();
		double scale = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
		if (scale > 1) {
			tblColl.setRowHeight((int)(stdHeight * scale));
		}
		
		Object[] options;
		if (lu.fisch.structorizer.gui.Diagram.isInSerialMode()) {
			options = new String[] {this.msgOk.getText(), msgNoFurtherConflicts.getText()};
		}
		else {
			options = new String[] {this.msgOk.getText()};
		}
		int answer = JOptionPane.showOptionDialog(this.getParent(),
				pnColl,
				this.titleDiagramConflict.getText(),
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, null);
		return answer > 0;
	}
	
	/**
	 * Determines the required width for the specified column of the given
	 * {@link JTable} {@code aTable} according to the column contents.
	 * (Taken from https://wiki.byte-welt.net/wiki/Breite_von_Tabellenspalten_automatisch_am_Zelleninhalt_anpassen)
	 * 
	 * @param aTable - the JTable to resize the columns on
	 * @param columnNo - the number of the column to be estimated
	 * @param includeColumnHeaderWidth - use the column header width as a minimum width?
	 * @param columnPadding - the number of extra pixels at the end of each column
	 * @returns The column width required to show all rows of this column in full length
	 */
	private static int getMaxColumnWidth(JTable aTable, int columnNo,
			boolean includeColumnHeaderWidth,
			int columnPadding) {
		// FIXME: This ought to be a method on a JTable subclass
		TableColumn column = aTable.getColumnModel().getColumn(columnNo);
		Component comp = null;
		int maxWidth = 0;
		if (includeColumnHeaderWidth) {
			TableCellRenderer headerRenderer = column.getHeaderRenderer();
			if (headerRenderer != null) {
				comp = headerRenderer.getTableCellRendererComponent(aTable, column.getHeaderValue(), false, false, 0, columnNo);
				if (comp instanceof JTextComponent) {
					JTextComponent jtextComp = (JTextComponent) comp;
					String text = jtextComp.getText();
					Font font = jtextComp.getFont();
					FontMetrics fontMetrics = jtextComp.getFontMetrics(font);
					maxWidth = SwingUtilities.computeStringWidth(fontMetrics, text);
				} else {
					maxWidth = comp.getPreferredSize().width;
				}
			} else {
				try {
					String headerText = (String) column.getHeaderValue();
					JLabel defaultLabel = new JLabel(headerText);
					Font font = defaultLabel.getFont();
					FontMetrics fontMetrics = defaultLabel.getFontMetrics(font);
					maxWidth = SwingUtilities.computeStringWidth(fontMetrics, headerText);
				} catch (final ClassCastException ce) {
					// Can't work out the header column width..
					maxWidth = 0;
				}
			}
		}
		TableCellRenderer tableCellRenderer;
		int cellWidth = 0;
		for (int i = 0; i < aTable.getRowCount(); i++) {
			tableCellRenderer = aTable.getCellRenderer(i, columnNo);
			comp = tableCellRenderer.getTableCellRendererComponent(aTable, aTable.getValueAt(i, columnNo), false, false, i, columnNo);
			if (comp instanceof JTextComponent) {
				JTextComponent jtextComp = (JTextComponent) comp;
				String text = jtextComp.getText();
				Font font = jtextComp.getFont();
				FontMetrics fontMetrics = jtextComp.getFontMetrics(font);
				int textWidth = SwingUtilities.computeStringWidth(fontMetrics, text);
				maxWidth = Math.max(maxWidth, textWidth);
			} else {
				cellWidth = comp.getPreferredSize().width;
				maxWidth = Math.max(maxWidth, cellWidth);
			}
		}
		return (maxWidth + columnPadding);
	}
	// END KGU#1124 2024-0315

	// START KGU#48 2015-10-17: As soon as a new NSD was loaded by some Mainform instance, Surface had lost track
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Updater#replace(lu.fisch.structorizer.elements.Root, lu.fisch.structorizer.elements.Root)
	 */
	@Override
	public void replaced(Root oldRoot, Root newRoot)
	{
		// Try to find the appropriate diagram holding oldRoot
		// START KGU#119/KGU#312 2016-01-02/2016-12-29: Bugfix #78 / enh. #315 - we only check for identity here, not for structural equality
		//Diagram owner = findDiagram(oldRoot);
		Diagram owner = findDiagram(oldRoot, 1);	// Check for identity
		// END KGU#119/KGU#312 2016-01-02/2016-12-29
		// START KGU#328 2017-01-11: This caused Comodification errorExceptions, oldRoot will wipe its updaters anyway
		//if (owner != null) {
		//	oldRoot.removeUpdater(this);
		//}
		// END KGU#328 2017-01-11
		// START KGU#88 2015-11-24: Protect the Root if diagram is pinned
		//if (owner != null) {
		if (owner != null && !owner.isPinned)
			// END KGU#88 2015-11-24
		{
			if (owner.mainform != null) {
				owner.root = owner.mainform.getRoot();
				owner.root.addUpdater(this);
			}
			else if (newRoot != null)
			{
				owner.root = newRoot;
				owner.root.addUpdater(this);
			}
			// START KGU#804 2020-02-17: Bugfix #818 - We must adapt all maps now!
			//// START KGU#85 2015-11-18
			//adaptLayout();
			//// END KGU#85 2015-11-18
			//this.repaint();
			this.rootMap.remove(oldRoot);
			this.rootMap.put(owner.root, owner);
			this.update(owner.root, true);
			// END KGU#804 2020-02-17
			// START KGU#278 2016-10-11: Enh. #267
			//notifyChangeListeners();
			// END KGU#278 2016-10-11
		}
		// START KGU#305 2016-10-16: Enh. #305
		notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED | IRoutinePoolListener.RPC_SELECTION_CHANGED);
		// END KGU#305 2016-10-16
	}
	// END KGU#48 2015-10-17

	// START KGU#2 2015-10-17: Prepares the execution of a registered NSD as subroutine
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#findDiagramsByName(java.lang.String)
	 */
	@Override
	public Vector<Root> findDiagramsByName(String rootName)
	{
		Vector<Root> functions = new Vector<Root>();
		// START KGU#624 2018-12-26: Enh. #655 Try to improve search performance
		//if (this.diagrams != null) {
		//	for (int d = 0; d < this.diagrams.size(); d++)
		//	{
		//		Diagram diagram = this.diagrams.get(d);
		//		if (rootName.equals(diagram.root.getMethodName()))
		//		{
		//			functions.add(diagram.root);
		//		}
		//	}
		//}
		Vector<Diagram> diagramList = this.nameMap.get(rootName);
		if (diagramList != null) {
			for (int d = 0; d < diagramList.size(); d++) {
				functions.add(diagramList.get(d).root);
			}
		}
		// END KGU#624 2018-12-26
		return functions;
	}
	// END KGU#2 2015-10-17

	// START KGU#376 2017-04-11: Enh. #389 - Support for import calls
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#findIncludesByName(java.lang.String)
	 */
	@Override
	public Vector<Root> findIncludesByName(String rootName, Root includer, boolean filterByClosestPath)
	{
		/* The most efficient strategy for group-aware retrieval is supposed to
		 * be first to fetch all matching Roots (in most cases the result will
		 * be unique anyway) and only to check group membership if the result
		 * consists of more than one Root.
		 */
		Vector<Root> incls = new Vector<Root>();
		for (Root root: this.findDiagramsByName(rootName)) {
			if (root.isInclude()) {
				incls.add(root);
			}
		}
		// START KGU#700 2019-03-28: Enh. #657 Check for group membership?
		if (incls.size() > 0 && includer != null) {
			incls = filterRootsByGroups(includer, incls, false);
		}
		// END KGU#700 2019-03-28
		
		// START KGU#408 2021-02-24: Enh. #410 Additional check for namespace match
		if (incls.size() > 0 && includer != null && includer.getNamespace() != null) {
			incls = sortRootsByNamespace(includer.getNamespace(), incls, filterByClosestPath);
		}
		// END KGU#408 2021-02-24
		return incls;
	}
	// END KGU#376 2017-04-11

	// START KGU#2 2015-11-24
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#findRoutinesBySignature(java.lang.String, int)
	 */
	@Override
	public Vector<Root> findRoutinesBySignature(String rootName, int argCount, Root caller, boolean filterByClosestPath)
	{
		/* The most efficient strategy for group-aware retrieval is supposed to
		 * be first to fetch all matching Roots (in most cases the result will
		 * be unique anyway) and only to check group membership if the result
		 * consists of more than one Root.
		 */
		Vector<Root> functionsAny = findDiagramsByName(rootName);
		Vector<Root> functions = new Vector<Root>();
		// START KGU#371 2019-03-07: Enh. #385 - In a second attempt look for closest matching routines with defaults
		int minDefaults = Integer.MAX_VALUE;
		// END KGU#371 2019-03-07
		for (int i = 0; i < functionsAny.size(); i++)
		{
			Root root = functionsAny.get(i);
			// START KGU#371 2019-03-07: Enh. #385 - In a second attempt look for closest matching routines with defaults
			//if (root.isSubroutine() && root.getParameterNames().count() == argCount)
			//{
			//	functions.add(root);
			//}
			if (root.isSubroutine()) {
				int nDflts = root.acceptsArgCount(argCount);
				if (nDflts >= 0 && nDflts <= minDefaults) {
					if (nDflts < minDefaults) {
						functions.clear();
						minDefaults = nDflts;
					}
					functions.add(root);
				}
			}
			// END KGU#371 2019-03-07
		}
		// START KGU#700 2019-03-28: Enh. #657 Check for group membership?
		if (functions.size() > 0 && caller != null) {
			functions = filterRootsByGroups(caller, functions, false);
		}
		// END KGU#700 2019-03-28
		
		// START KGU#408 2021-02-24: Enh. #410 Additional check for namespace match
		if (functions.size() > 0 && caller != null && caller.getNamespace() != null) {
			functions = sortRootsByNamespace(caller.getNamespace(), functions, filterByClosestPath);
		}
		// END KGU#408 2021-02-24
		return functions;
	}
	// END KGU#2 2015-11-24

	// START KGU#258 2016-09-26: Enh. #253: We need to traverse all roots for refactoring
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#getAllRoots()
	 */
	@Override
	public Set<Root> getAllRoots()
	{
		Set<Root> roots = new HashSet<Root>();
		if (this.diagrams != null) {
			for (int d = 0; d < this.diagrams.size(); d++)
			{
				Diagram diagram = this.diagrams.get(d);
				roots.add(diagram.root);
			}
		}    
		return roots;
	}
	// END KGU#258 2016-09-26
	
	// START KGU#624 2018-12-22: Enh. #655
	/**
	 * Returns a {@link StringList} with the signatures of all selected diagrams, sorted
	 * first by type and second lexicographically.
	 * @param allIfEmpty - Shall all Roots be reported if none is selected?
	 * @param withPath - Shall the file paths be added to the signature?
	 * @return the StringList.
	 * @see #listCollectedRoots(Collection, boolean)
	 */
	protected StringList listSelectedRoots(boolean allIfEmpty, boolean withPath) {
		Collection<Diagram> selected = this.diagramsSelected;
		if (selected.isEmpty() && allIfEmpty) {
			selected = this.diagrams;
		}
	// START KGU#626 2019-01-05: Enh. #657 - method decomposed
		return listCollectedRoots(selected, withPath);
	}
	/**
	 * Returns a {@link StringList} with the signatures of all collected diagrams, sorted
	 * first by type and second lexicographically.
	 * @param collectedDiagrams - a collection of {@link Diagram} objects
	 * @param withPath - Shall the file paths be added to the signature? 
	 * @return the StringList
	 * @see #listSelectedRoots(boolean, boolean)
	 */
	protected StringList listCollectedRoots(Collection<Diagram> collectedDiagrams, boolean withPath)
	{
	// END KGU#657 2019-01-05
		Vector<Root> selectedRoots = new Vector<Root>(collectedDiagrams.size());
		for (Diagram diagr: collectedDiagrams) {
			if (diagr.root != null) {
				selectedRoots.add(diagr.root);
			}
		}
		Collections.sort(selectedRoots, Root.SIGNATURE_ORDER);
		StringList signatures = new StringList();
		for (Root root: selectedRoots) {
			signatures.add(root.getSignatureString(withPath, false));
		}
		return signatures;
	}
	// END KGU#624 2018-12-22

	// START KGU#117 2016-03-08: Introduced on occasion of Enhancement #77
	/**
	 * Clears the execution status of all routines in the pool.
	 */
	public void clearExecutionStatus()
	{
		if (this.diagrams != null) {
			for (int d = 0; d < this.diagrams.size(); d++)
			{
				Diagram diagram = this.diagrams.get(d);
				if (diagram.root != null)
				{
					diagram.root.clearExecutionStatus();
				}
			}
			this.repaint();
		}
	}
	// END KGU#117 2016-03-08

	// START KGU#700 2019-03-28: Enh. #657
	/**
	 * Returns a {@link Vector} of only those elements of {@code roots} that
	 * share at least one group with {@code discriminator} if {@code discriminator}
	 * is member of at least one group. If {@code strictly} is false, then in case of
	 * an empty intersection the original set of roots will be returned (unfiltered).
	 * @param discriminator - a {@link Root} object the associated groups of which decide membership
	 * @param roots - collection of candidate {@link Root} objects
	 * @param strictly - if {@code true} then an empty vector is returned if none of the
	 *  roots shares a group with {@code discriminator}, otherwise all given roots will
	 *  be returned in case the filtered set became empty
	 * @return the filtered collection of members of {@code roots}
	 */
	private Vector<Root> filterRootsByGroups(Root discriminator, Vector<Root> roots, boolean strictly) {
		Vector<Root> filteredRoots = new Vector<Root>();
		Collection<Group> wantedGroups = this.getGroupsFromRoot(discriminator, false);
		if (!wantedGroups.isEmpty()) {
			for (int i = 0; i < roots.size(); i++) {
				Root root = roots.get(i);
				Diagram diagr = this.rootMap.get(root);
				for (Group grp: wantedGroups) {
					if (grp.containsDiagram(diagr)) {
						filteredRoots.add(root);
						break;
					}
				}
			}
		}
		if (filteredRoots.isEmpty() && !strictly) {
			filteredRoots.addAll(roots);
		}
		return filteredRoots;
	}
	// END KGU#700 2019-03-28
	
	// START KGU#408 2021-02-24: Enh. #410 Additional namespace field on behalf of Java import
	/**
	 * Returns a {@link Vector} of the given elements of {@code roots} that is sorted
	 * from maximum to minimum coincidence of the name space prefixes with given
	 * {@code namespace}. Result set may be restricted to the best-matching partition.
	 * @param namespace - the qualifier to be matched against
	 * @param roots - the candidate {@link Root}s
	 * @param onlyBestMatch - if {@code true} and there are differences in the length
	 *   of the common namespace prefix then only the best-matching category will be
	 *   returned
	 * @return the sorted and possibly filtered collection of members of {@code roots}
	 */
	private Vector<Root> sortRootsByNamespace(String namespace, Vector<Root> roots, boolean onlyBestMatch) {
		Vector<Root> orderedRoots = new Vector<Root>();
		StringList path = StringList.explode(namespace, "\\.");
		Vector<Vector<Root>> partition = new Vector<Vector<Root>>(path.count()+1);
		for (int i = 0; i <= path.count(); i++) {
			partition.add(new Vector<Root>());
		}
		for (int i = 0; i < roots.size(); i++) {
			Root cand = roots.get(i);
			if (cand.getNamespace() == null) {
				partition.get(0).add(cand);
			}
			else {
				StringList pathI = StringList.explode(cand.getNamespace(), "\\.");
				int minLgth = Math.min(path.count(), pathI.count());
				for (int j = 0; j < minLgth; j++) {
					if (!path.get(j).equals(pathI.get(j))) {
						partition.get(j).add(cand);
						break;
					}
					else if (j+1 == minLgth) {
						partition.get(j+1).add(cand);
					}
				}
			}
		}
		for (int i = partition.size() - 1; i >= 0; i--) {
			if (orderedRoots.addAll(partition.get(i)) && onlyBestMatch) {
				break;
			}
		}
		return orderedRoots;
	}
	// END KGU#408 2021-02-24

	// START KGU#305 2016-12-16
	/**
	 * Returns the list of all Mainform objects associated to some diagram held
	 * here.
	 * @return vector of actively associated Mainforms.
	 */
	private Vector<Mainform> activeMainforms()
	{
		Vector<Mainform> mainforms = new Vector<Mainform>();
		if (this.diagrams != null) {
			// It will be a small number of Mainforms, so a Vector is okay
			for (int d = 0; d < this.diagrams.size(); d++) {
				Diagram diagram = this.diagrams.get(d);
				if (diagram.root != null && diagram.mainform != null) {
					if (!mainforms.contains(diagram.mainform)) {
						mainforms.add(diagram.mainform);
					}
				}
			}
		}
		return mainforms;
	}
	// END KGU#305 2016-12-16
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent e)
	{
	}

	/**
	 * Windows listener method for the {@link Mainform}.<br/>
	 * Needed to unregister the updaters.
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent e)
	{
		if (e.getSource() instanceof Mainform)
		{
			Mainform mainform = (Mainform) e.getSource();
			// unregister updater
			mainform.getRoot().removeUpdater(this);
			// remove mainform reference
			if (diagrams!=null)
			{
				for (int d=0; d<diagrams.size(); d++)
				{
					Diagram diagram = diagrams.get(d);
					// START KGU#158 2016-03-16: Bugfix #132 - wrong mainform cleared
					//Root root = diagram.root;
					////Point point = diagram.point;
					//if (mainform.getRoot() == root)
					if (diagram.mainform == mainform)
						// END KGU#158 2016-03-16
					{
						diagram.mainform = null;
					}
				}
			}
		}
	}

	/**
	 * Windows listener method for the {@link Mainform}.<br/>
	 * Needed to remove the {@link Mainforms} as change listeners.
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent e)
	{
		// START KGU#305 2016-12-16
		if (e.getSource() instanceof Mainform) {
			Mainform form = (Mainform)e.getSource();
			removeChangeListener(form);
			// START KGU#745 2019-10-05: Bugfix #759 - We must make sure that stale mainforms get dropped
			if (diagrams != null) {
				for (int i = 0; i < diagrams.size(); i++) {
					Diagram diagr = diagrams.get(i);
					if (diagr.mainform == form) {
						diagr.mainform = null;
					}
				}
			}
			// END KGU#745 2019-10-05
		}
		// END KGU#305 2016-12-16
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	// START KGU#305 2016-12-12: Enh. #305
	/**
	 * Scrolls to the given Root if found and selects it. If setAtTop is true then the diagram
	 * will be raised to the top drawing level.
	 * @param aRoot - the diagram to be focused
	 * @param setAtTop - whether the diagram is to be drawn on top of all
	 */
	public void scrollToDiagram(Root aRoot, boolean setAtTop) {
		// START KGU#312 2016-12-29: Enh. #315 - adaptation to modified signature 
		//Diagram diagr = this.findDiagram(aRoot, true);
		Diagram diagr = this.findDiagram(aRoot, 1);	// Check for identity here
		// END KGU#312 2016-12-29
		if (diagr != null) {
			if (setAtTop) {
				this.diagrams.remove(diagr);
				this.diagrams.add(diagr);
				// START KGU#624 2018-12-21: Enh. #655 - replace the previous selection (really?)
				//if (mouseSelected != null && mouseSelected != diagr && mouseSelected.root != null)
				//{
				//	mouseSelected.root.setSelected(false);
				//}
				//mouseSelected = diagr;
				//diagr.root.setSelected(true);
				unselectAll();
				this.diagramsSelected.add(diagr);
				diagr.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
				// END KGU#624 2018-12-21
				this.repaint();
				// START KGU#624 2018-12-21: Enh. #655
				this.notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
				// END KGU#624 2018-12-21
			}
			Rect rect = aRoot.getRect(diagr.point);
			// START KGU#497 2018-02-17: Enh. #512
			rect = rect.scale(this.zoomFactor);
			// END KGU#497 2018-02-17
			this.scrollRectToVisible(rect.getRectangle());
		}
	}

	// START KGU#305 2016-12-12: Enh. #305
	/**
	 * Scrolls to the given Group if found and selects all members of it.
	 * If {@code showBounds} is true then the bounding box of the group will
	 * be drawn.
	 * @param aGroup - the {@link Group} to be focused
	 */
	public void scrollToGroup(Group aGroup) {
		Collection<Diagram> members = aGroup.getDiagrams();
		Rect rect = this.getDrawingRect(members, null);
		rect = rect.scale(this.zoomFactor);
		unselectAll();
		this.selectSet(members);
		this.scrollRectToVisible(rect.getRectangle());
	}

	// START KGU#624 2018-12-24: Enh. #655
	/**
	 * Scrolls to the current selection.
	 */
	public void scrollToSelection()
	{
		Rectangle bounds = getSelectionBounds(false);
		if (bounds != null) {
			this.scrollRectToVisible(bounds);
		}
	}

	/**
	 * Returns the bounding rectangle for the current selection
	 * @param unzoomed - if the true coordinates are wanted, otherwise the zoom factor will be applied
	 * @return the bounding box of the selected diagrams or null if the selection is empty
	 */
	private Rectangle getSelectionBounds(boolean unzoomed)
	{
		Rectangle bounds = null;
		Rect rect = this.getDrawingRect(this.diagramsSelected, null);
		if (!unzoomed) {
			rect = rect.scale(zoomFactor);
		}
		if (rect.right > rect.left && rect.bottom > rect.top) {
			bounds = rect.getRectangle();
		}
		return bounds;
	}
	// END KGU#624 2018-12-24
	
	/**
	 * @return the number of currently selected diagrams
	 */
	public int getSelectionCount()
	{
		return diagramsSelected.size();
	}
	
	/**
	 * @return the set of {@link Root} objects currently selected in Arranger.
	 */
	// START KGU#624 2018-12-21: Enh. #655
	//public Root getSelected()
	//{
	//	if (mouseSelected != null)
	//	{
	//		return mouseSelected.root;
	//	}
	//	return null;
	//}
	public Set<Root> getSelected() {
		Set<Root> selectedRoots = new HashSet<Root>();
		for (Diagram diagr: this.diagramsSelected) {
			if (diagr.root != null) {
				selectedRoots.add(diagr.root);
			}
		}
		return selectedRoots;
	}
	// END KGU#624 2018-12-21
	// END KGU#305 2016-12-12
	
	// START KGU#624 2018-12-21: Enh. #655
	/**
	 * In case of a single diagram being selected returns the {@link Root} currently
	 * selected in Arranger
	 * @return Either a {@link Root} object or null (if selection was empty or ambiguous)
	 */
	public Root getSelected1() 
	{
		if (diagramsSelected.size() == 1)
		{
			return diagramsSelected.iterator().next().root;
		}
		return null;
	}
	// END KGU#624 2018-12-21
	
	// START KGU#624 2018-12-26: Enh. #655
    protected int expandSelectionRecursively(StringList missingSignatures, StringList duplicateSignatures) {
    	Set<Root> selectedRoots = getSelected();
		Set<Diagram> addedDiagrams = expandRootSet(selectedRoots, missingSignatures, duplicateSignatures);
		diagramsSelected.addAll(addedDiagrams);
		notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
		repaint();
		return addedDiagrams.size();
	}

	/**
	 * Searches for diagrams referenced by some of {@code selectedRoots} but not being member of them.
	 * Proceeds recursively for all added diagrams as well.
	 * @param selectedRoots - the initial set of {@link Root} objects. CAUTION: may be expanded!
	 * @param missingSignatures - a {@link StringList} to gather signatures of missing diagrams
	 * @param duplicateSignatures - a {@link StringList} to gather signatures of ambiguous diagrams
	 * @return the set of added {@link Diagram} objects
	 */
	protected Set<Diagram> expandRootSet(Set<Root> selectedRoots, StringList missingSignatures,
			StringList duplicateSignatures) {
		LinkedList<Root> rootQueue = new LinkedList<Root>(selectedRoots);
		Set<Diagram> addedDiagrams = new HashSet<Diagram>();
		while (!rootQueue.isEmpty()) {
			Root root = rootQueue.removeFirst();
			// First look for called routines
			Vector<Call> calls = root.collectCalls();
			for (Call call: calls) {
				Function fct = call.getCalledRoutine();
				if (fct != null) {
					Vector<Root> candidates = this.findRoutinesBySignature(fct.getName(), fct.paramCount(), root, false);
					handleReferenceCandidates(selectedRoots, missingSignatures, duplicateSignatures, rootQueue, addedDiagrams,
							fct.getSignatureString(), candidates);
				}
			}
			// Then look for referenced includables
			if (root.includeList != null) {
				for (int i = 0; i < root.includeList.count(); i++) {
					String inclName = root.includeList.get(i);
					Vector<Root> candidates = this.findIncludesByName(inclName, root, false);
					handleReferenceCandidates(selectedRoots, missingSignatures, duplicateSignatures, rootQueue, addedDiagrams,
							inclName, candidates);
				}
			}
		}
		return addedDiagrams;
	}

	/**
	 * Checks for each {@link Root} object in {@code candidates} whether it is to be added to
	 * {@code rootSet}, {@code rootQueue}, and {@code addedDiagrams} or if the {@code signature}
	 * is to be registered with {@code missingSignatures} or {@code duplicateSignatures}.
	 * @param rootSet - the set of {@link Root}s to be completed
	 * @param missingSignatures - {@link StringList} gathering signatures of missing diagrams
	 * @param duplicateSignatures - {@link StringList} gathering ambiguous signatures
	 * @param rootQueue - the queue of unprocessed taks (i.e. {@link Root}s
	 * @param addedDiagrams - set of {@link Diagram} objects for the added {@link Root}s
	 * @param signature - the signature (search pattern)
	 * @param candidates - the vector of found {@link Root} objects matching the {@code signature}
	 */
	private void handleReferenceCandidates(Set<Root> rootSet, StringList missingSignatures, StringList duplicateSignatures,
			LinkedList<Root> rootQueue, Set<Diagram> addedDiagrams, String signature, Vector<Root> candidates) {
		/* First check if any of the candidates is already member of the set, then we
		 * can ignore all the others
		 */
		boolean isMember = false;
		for (Root sub: candidates) {
			if (rootSet.contains(sub)) {
				isMember = true;
				break;
			}
		}
		if (!isMember) {
			// For now we will add all of them to be on the safe side
			for (Root sub: candidates) {
				rootSet.add(sub);
				rootQueue.addLast(sub);
				addedDiagrams.add(rootMap.get(sub));	// Should of course be in the map
				// This only an optical selection, does not change the selection set
				sub.setSelected(true, Element.DrawingContext.DC_ARRANGER);
			}
			if (candidates.size() > 1 && duplicateSignatures != null) {
				duplicateSignatures.addIfNew(signature);
			}
		}
		if (candidates.isEmpty() && missingSignatures != null) {
			missingSignatures.addIfNew(signature);
		}
	}

	// END KGU#624 2018-12-26

	// START KGU#305 2016-12-16: Code revision
	@Override
	public void addChangeListener(IRoutinePoolListener _listener) {
		if (_listener instanceof Arranger) {
			listeners.add(_listener);
		}
		else {
			Arranger.addToChangeListeners(_listener);
		}
	}

	@Override
	public void removeChangeListener(IRoutinePoolListener _listener) {
		if (_listener instanceof Arranger) {
			listeners.remove(_listener);
		}
		else {
			Arranger.removeFromChangeListeners(_listener);
		}
	}

	// START KGU#624 2018-12-21: Enh. #655
	//private void notifyChangeListeners()
	private void notifyChangeListeners(int _flags)
	// END KGU#624 2018-12-21
	{
		if (this.notifications_enabled ) {
			for (IRoutinePoolListener listener: listeners) {
				// START KGU#624 2018-12-21: Enh. #655
				//listener.routinePoolChanged(this);
				listener.routinePoolChanged(this, _flags);
				// END KGU#624 2018-12-21
			}
		}
		else {
			deferred_notifications |= _flags;
		}
	}
	// END KGU#305 2016-12-16
	
	// START KGU#408 2021-03-01: Enh. #410 Acceleration attempt 
	@Override
	public void enableNotification(boolean enable)
	{
		this.notifications_enabled = enable;
		if (enable && deferred_notifications != 0) {
			notifyChangeListeners(deferred_notifications);
			deferred_notifications = 0;
		}
		
	}

	@Override
	public boolean isNotificationEnabled()
	{
		return this.notifications_enabled;
	}
	// END KGU#408 2021-03-01

	// START KGU#497 2018-02-17: Enh. #512
	/**
	 * Raises or reduces the current zoom factor according to the argument.
	 * @param zoomIn - if true then z´the zoom factor is reduced otherwise it is raised
	 */
	public void zoom(boolean zoomIn) {
		if (zoomIn) {
			this.zoomFactor = Math.min(this.zoomFactor / 0.9f, 1);
		}
		else {
			this.zoomFactor = Math.max(this.zoomFactor * 0.9f, 0.01f);
		}
		repaint();
		// START KGU#624 2018-12-21: Enh. #655
		this.dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
		// END KGU#624 2018-12-21
	}

	/**
	 * Return the current zoom factor
	 * @return zoom factor (1 = 100%, 0.5 = 50% etc.)
	 */
	public float getZoom() {
		return this.zoomFactor;
	}
	// END KGU#497 2018-02-17

	// START KGU#503 201-03-13: Enh. #519 - ctrl + mouse wheel is to zoom in / zoom out
	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent mwEvt) {
		if (mwEvt.isControlDown()) {
			int rotation = mwEvt.getWheelRotation();
			if (Math.abs(rotation) >= 1) {
				if (Element.E_WHEEL_REVERSE_ZOOM) {
					rotation *= -1;
				}
				mwEvt.consume();
				this.zoom(rotation < 0);
			}
		}
	}
	// END KGU#503 2018-03-13
	
	// START KGU#624 2018-12-26: Enh. #655
	/**
	 * Adds {@code diagr} to the set of diagrams {@code name} maps to.<br/>
	 * (This is only for internal use!)
	 * @param name - the assumed diagram name (method name)
	 * @param diagr - the {@link Diagram} to be associated to {@code name}
	 * @return true if the mapping has really changed 
	 */
	private boolean addToNameMap(String name, Diagram diagr)
	{
		boolean added = false;
		Vector<Diagram> diagrs = this.nameMap.get(name);
		if (diagrs == null) {
			diagrs = new Vector<Diagram>();
			diagrs.add(diagr);		
			this.nameMap.put(name, diagrs);
			added = true;
		}
		else if (!diagrs.contains(diagr)) {
			added = diagrs.add(diagr);
		}
		return added;
	}
	
	/**
	 * Removes the {@link Diagram} {@code diagr} from the diagram set the given {@code name}
	 * maps to.<br/>
	 * (This is only for internal use!)
	 * @param name - a diagram name
	 * @param diagr - the {@link Diagram} to be disassociated from {@code name} 
	 * @return true if {@code diagr} had been mapped from {@code name} and isn't any longer
	 * @see #addToNameMap(String, Diagram)
	 */
	private boolean removeFromNameMap(String name, Diagram diagr)
	{
		boolean removed = false;
		Vector<Diagram> diagrs = this.nameMap.get(name);
		if (diagrs != null) {
			removed = diagrs.removeElement(diagr);
			if (diagrs.isEmpty()) {
				this.nameMap.remove(name);
			}
		}
		return removed;
	}
	// END KGU#624 2018-12-26
	
	// START KGU#1030 2022-06-01: Enh. #1035
	/**
	 * Retrieves the group registered with name {@code groupName} if existent,
	 * otherwise {@code null}
	 * 
	 * @param groupName - name of the questioned group
	 * @return the requested group or {@code null}
	 * 
	 * @see #hasGroup(String)
	 * @see #getGroups()
	 */
	protected Group getGroup(String groupName)
	{
		return groups.get(groupName);
	}
	// END KGU#1030 2022-06-01
	
	// START KGU#626 2019-01-01/05: Enh. #657
	/**
	 * Simply checks whether there is a registered group with name {@code groupName}
	 * 
	 * @param groupName - name of the questioned group
	 * @return true if there is a group with this name
	 * 
	 * @see #getGroup(String)
	 */
	protected boolean hasGroup(String groupName)
	{
		return groups.containsKey(groupName);
	}
	
	/**
	 * Ensures the existence of a group with name {@code groupName}, fills it either with
	 * the {@link Diagram}s associated to the {@link Root}s contained in given collection
	 * {@code roots} (if not null) or the currently selected diagrams.<br/>
	 * If for some of the given {@code roots} there hasn't already been a corresponding {@link Diagram}
	 * object then it will be created.<br/>
	 * If {@code groupName} is not equal to {@link Group#DEFAULT_GROUP_NAME} then all added diagrams
	 * that had been member of the default group will be detached from it.
	 * 
	 * @param groupName - the name for the new group
	 * @param form - possible a commanding {@link Mainform} or null
	 * @param roots - a collection of {@link Root} objects to be associated to the group or null
	 * @param replaceIfExisting - if true and the group had already existed then it will be emptied before.
	 * @param arrFileOrNull - if given, sets the group arranger file path to its absolute path
	 * @return the actually resulting group if it could be created and the specified diagrams could
	 *     be attached to it, {@code null} otherwise. 
	 */
	protected Group makeGroup(String groupName, Mainform form, Collection<Root> roots, boolean replaceIfExisting, File arrFileOrNull)
	{
		boolean done = false;
		Vector<Diagram> diagramsToAdd = new Vector<Diagram>();
		Group group = this.groups.get(groupName);
		// Create the group if there is something to add
		if (group == null) {
			this.groups.put(groupName, (group = new Group(groupName)));
		}
		else if (!groupName.equals(Group.DEFAULT_GROUP_NAME) && replaceIfExisting) {
			group.clear();
		}
		if (roots == null) {
			diagramsToAdd.addAll(diagramsSelected);
		}
		else {
			for (Root root: roots) {
				// START KGU#911 2021-01-11: Enh. #910 We don't allow diagram controller roots
				if (root.isRepresentingDiagramController()) {
					continue;
				}
				// END KGU#911 2021-01-11
				Diagram diagr = rootMap.get(root);
				if (diagr == null) {
					this.addDiagram(root, form, null, group);
				}
				else {
					diagramsToAdd.add(diagr);
				}
			}
		}
		done = group.addAllDiagrams(diagramsToAdd);
		// Now remove the diagrams from the default group as soon as they are members of a named group
		Group defaultGroup = groups.get(Group.DEFAULT_GROUP_NAME);
		if (done && !groupName.equals(Group.DEFAULT_GROUP_NAME) && defaultGroup != null) {
			for (Diagram diagr: diagramsToAdd) {
				defaultGroup.removeDiagram(diagr);
			}
			if (defaultGroup.isEmpty()) {
				groups.remove(Group.DEFAULT_GROUP_NAME);
			}
		}
		if (arrFileOrNull != null) {
			group.setFile(arrFileOrNull, null, false);
		}
		this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		return done ? group : null;
	}

	/**
	 * Removes the group with the given {@code name}.
	 * @param name - the name of the group
	 * @param withDiagrams - if {@code true} then the member diagrams will also be removed
	 * if not held by any other group, otherwise the orphaned diagrams will be handed over
	 * to the default group.
	 * @param initiator - the initiating component, e.g. {@link Arranger} or {@link ArrangerIndex}
	 * @return {@code true} if the group with given name had existed and has been removed.
	 * @see #dissolveGroup(String, Component)
	 */
	protected boolean removeGroup(String name, boolean withDiagrams, Component initiator) {
		return clearGroup(name, withDiagrams, true, initiator);
	}
	
	/**
	 * Dissolves (clears) the group with the given {@code name}.
	 * The member diagrams that are not shared by other groups will be handed over
	 * to the default group.
	 * @param name - the name of the group
	 * @param initiator - the initiating component, e.g. {@link Arranger} or {@link ArrangerIndex}
	 * @return true if the group with given name had existed and has been cleared.
	 * @see #removeGroup(String, boolean, Component)
	 */
	protected boolean dissolveGroup(String name, Component initiator) {
		return clearGroup(name, false, false, initiator);
	}
	
	/**
	 * Clears the group with the given {@code name}.
	 * The member diagrams that are not shared by other groups will be handed over
	 * to the default group.
	 * @param deleteDiagrams - if true then the member diagrams will also be removed if not held by
	 * any other group, otherwise the orphaned diagrams will be handed over to the default group.
	 * @param deleteGroup - if the (empty) group is to be deleted in the event.
	 * @param initiator - the initiating component, e.g. {@link Arranger} or {@link ArrangerIndex}
	 * @return {@code true} if the group with given name had existed and has been cleared.
	 * @see #removeGroup(String, boolean, Component)
	 */
	private boolean clearGroup(String name, boolean deleteDiagrams, boolean deleteGroup, Component initiator) {
		boolean done = false;
		boolean anythingChanged = false;
		Group targetGroup = null;	// Where to add orphaned diagrams
		Vector<Diagram> doomedDiagrams = null;
		if (!deleteDiagrams && !name.equals(Group.DEFAULT_GROUP_NAME)) {
			targetGroup = groups.get(Group.DEFAULT_GROUP_NAME);
			if (targetGroup == null) {
				// Default group is missing, so create it
				targetGroup = new Group(Group.DEFAULT_GROUP_NAME);
				groups.put(Group.DEFAULT_GROUP_NAME, targetGroup);
				anythingChanged = true;
			}
		}
		else {
			// We will postpone the actual removal of diagrams since this may require user interaction
			doomedDiagrams = new Vector<Diagram>();
		}
		Group group = groups.get(name);
		if (group != null) {
			int option = 0;	// means ok to save
			if (deleteGroup && group.hasChanged()) {
				// FIXME: consider serial action
				String question = msgSaveGroupChanges.getText().replace("%", group.getName().
						replace(Group.DEFAULT_GROUP_NAME, ArrangerIndex.msgDefaultGroupName.getText()));
				option = lu.fisch.structorizer.gui.Diagram.requestSaveDecision(question, initiator,
						lu.fisch.structorizer.gui.Diagram.SerialDecisionAspect.SERIAL_GROUP_SAVE);
				if (option == 0) {
					this.saveArrangement(initiator, group, false);	// FIXME
				}
			}
			if (option != -1) {
				for (Diagram diagram: group.getDiagrams()) {
					int nGroups = diagram.getGroupNames().length;
					if (deleteGroup || nGroups > 1 || !group.isDefaultGroup()) {
						anythingChanged = group.removeDiagram(diagram) || anythingChanged;
						// Is the diagram orphaned now?
						if (diagram.getGroupNames().length == 0) {
							if (targetGroup != null) {
								anythingChanged = targetGroup.addDiagram(diagram) || anythingChanged;
							}
							else {
								doomedDiagrams.add(diagram);
							}
						}
					}
				}
				if (doomedDiagrams != null) {
					StringList unsaved = new StringList();
					done = saveDiagrams(doomedDiagrams, false, false, false, unsaved);
					for (Diagram diagram: doomedDiagrams) {
						removeDiagram(diagram);
					}
					anythingChanged = true;
				}
				if (group.isEmpty()) {
					if (group.getFile() == null || deleteGroup
							|| JOptionPane.showConfirmDialog(initiator == null ? this : initiator,
									msgConfirmRemoveGroup.getText()
									.replace("%", name.replace(Group.DEFAULT_GROUP_NAME,
											ArrangerIndex.msgDefaultGroupName.getText()))) == JOptionPane.OK_OPTION) {
						anythingChanged = (this.groups.remove(name) != null) || anythingChanged;
					}
					done = true;
				}
			}
		}
		if (anythingChanged) {
			this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		}
		return done;
	}
	
	/**
	 * Identifies all groups (except the default group) that are associated or congruent
	 * (depending on argument {@code congruent}) with the currently selected subset of diagrams
	 * (or the entire set of diagrams if nothing is selected).
	 * @param congruent - if only groups being congruent with the selection are wanted.
	 * @return a collection (set) of the identified groups
	 * @see #getGroupsFromRoots(Collection, boolean)
	 * @see #getGroupsFromCollection(Collection, boolean)
	 */
	public Collection<Group> getGroupsFromSelection(boolean congruent)
	{
		// START KGU#630 2019-01-09: Enh. #662/2
		if (selectGroups) {
			return groupsSelected;
		}
		// END KGU#630 2019-01-09
		Collection<Diagram> interestingDiagrams = diagramsSelected.isEmpty() ? diagrams : diagramsSelected;
		return getGroupsFromCollection(interestingDiagrams,congruent);
	}
	
	/**
	 * Identifies all groups (except the default group) that are associated or congruent
	 * (depending on argument {@code congruent}) with the diagrams in {@code interestingDiagrams}
	 * @param interestingDiagrams - the set of diagrams to scrutinize
	 * @param congruent - if only groups being congruent with the selection are wanted.
	 * @return a collection (set) of the identified groups
	 * @see #getGroupsFromRoots(Collection, boolean)
	 * @see #getGroupsFromSelection(boolean)
	 */
	protected Collection<Group> getGroupsFromCollection(Collection<Diagram> interestingDiagrams, boolean congruent)
	{
		Set<Group> foundGroups = new HashSet<Group>();
		int nInteresting = interestingDiagrams.size();
		for (Diagram diagr: interestingDiagrams) {
			Set<Diagram> members = null;
			for (String groupName: diagr.getGroupNames()) {
				if (!groupName.equals(Group.DEFAULT_GROUP_NAME)) {
					Group group = groups.get(groupName);
					if (group != null && 
							(!congruent || ((members = group.getDiagrams()).size() == nInteresting && members.containsAll(interestingDiagrams)))) {
						foundGroups.add(group);
					}
				}
			}
			if (congruent) {
				// There may not be congruent groups not containing the first diagram... 
				break;
			}
		}
		return foundGroups;
	}

	/**
	 * Identifies all groups (except the default group) that are associated or congruent
	 * (depending on argument {@code congruent}) with the diagrams in {@code interestingDiagrams}
	 * @param roots - collection of {@link Root} objects to be grouped
	 * @param congruent - if only groups being congruent with the selection are wanted.
	 * @return a collection (set) of the identified groups
	 * @see #getGroupsFromCollection(Collection, boolean)
	 * @see #getGroupsFromSelection(boolean)
	 */
	public Collection<Group> getGroupsFromRoots(Collection<Root> roots, boolean congruent)
	{
		HashSet<Diagram> diagrs = new HashSet<Diagram>();
		for (Root root: roots) {
			Diagram diagr = rootMap.get(root);
			if (diagr != null) {
				diagrs.add(diagr);
			}
		}
		return getGroupsFromCollection(diagrs, congruent);
	}
	
	/**
	 * Identifies all groups associated with the diagram given by in {@code interestingDiagrams}
	 * @param root - the {@link Root} object, the group membership of which is to be returned
	 * @param supressDefaultGroup - if true then the default group won't be reported
	 * @return a collection (set) of the identified groups
	 * @see #getGroupsFromCollection(Collection, boolean)
	 * @see #getGroupsFromSelection(boolean)
	 * @see #getGroupsFromRoots(Collection, boolean)
	 */
	public Collection<Group> getGroupsFromRoot(Root root, boolean suppressDefaultGroup)
	{
		HashSet<Group> owningGroups = new HashSet<Group>();
		Diagram diagr = rootMap.get(root);
		if (diagr != null) {
			String[] groupNames = diagr.getGroupNames();
			for (int i = 0; i < groupNames.length; i++) {
				Group group = groups.get(groupNames[i]);
				if (group != null && (!suppressDefaultGroup || !group.isDefaultGroup())) {
					owningGroups.add(group);
				}
			}
		}
		return owningGroups;
	}
	

	
	/**
	 * Detaches the given {@link Root} {@code root} from the specified {@code group} (if it was a
	 * member of it).</br>
	 * The corresponding {@link Diagram} may remain in Arranger if it is also held by other groups.
	 * If not, however, then {@code removeOrphaned} decides whether the diagram will be eliminated
	 * or moved to thwe default group.<br/>
	 * If {@code group} gets empty then the user may interactively decide whether it is to be deleted.
	 * @param group - the {@link Group} from which {@code root} is to be detached.
	 * @param root - the {@link Root} to be detached
	 * @param removeOrphaned - whether automatically to remove diagrams not belonging to any other group
	 * @param initiator - the component initiating this action or {@code null}
	 * @return whether the removal had an effect
	 */
	public boolean removeDiagramFromGroup(Group group, Root root, boolean removeOrphaned, Component initiator) {
		boolean done = false;
		Diagram diagr = rootMap.get(root);
		if (diagr != null) {
			done = group.removeDiagram(diagr);
			if (done && diagr.getGroupNames().length == 0) {
				if (removeOrphaned) {
					this.removeDiagram(diagr);
				}
				else {
					Group defaultGroup = groups.get(Group.DEFAULT_GROUP_NAME);
					if (defaultGroup == null) {
						defaultGroup = new Group(Group.DEFAULT_GROUP_NAME);
						groups.put(Group.DEFAULT_GROUP_NAME, defaultGroup);
					}
					defaultGroup.addDiagram(diagr);
				}
			}
		}
		if (group.isEmpty() && (group.getFile() == null || JOptionPane.showConfirmDialog(
				initiator == null ? this : initiator,
						msgConfirmRemoveGroup.getText().replace("%", group.getName())) == JOptionPane.OK_OPTION)) {
			groups.remove(group.getName());
		}
		if (done) {
			this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		}
		return done;
	}
	
	/**
	 * Attaches the given {@link Root} {@code root} (which must be present here) to the specified
	 * {@code group} (if it hadn't already been a member of it).</br>
	 * @param group - the {@link Group} to which {@code root} is to be attached (additionally)
	 * @param root - the diagram to be attached to {@code group} 
	 * @return true if this resulted in an actual change
	 * @see #addDiagramToGroup(Root, Mainform, Point, String)
	 */
	protected boolean addDiagramToGroup(Group group, Root root)
	{
		boolean done = false;
		Diagram diagr = this.rootMap.get(root);
		if (diagr != null) {
			done = group.addDiagram(diagr);
		}
		if (done) {
			this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
		}
		return done;
	}
	
	/**
	 * Tries to save all "dirty" groups i.e. those with pending changes as arrangements. The default
	 * group is skipped here, in particular if {@code goingToClose} is true.
	 * @param initiator - the initiating GUI component (as owner of possible dialogs etc.)
	 * @param goingToClose - whether this method was called because the application is going to shut down
	 * @param unsaved - a {@link SringList} where the names of groups that couldn't be saved will be added 
	 * @return true if all was done without serious problems.
	 */
	protected boolean saveGroups(Component initiator, boolean goingToClose)
	{
		boolean allDone = true;
		StringList unsaved = new StringList();
		lu.fisch.structorizer.gui.Diagram.startSerialMode();
		// START KGU#901 2020-12-30: Issue #901
		Cursor origCursor = getCursor();
		// END KGU#901 2020-12-30
		try {
			// Saving an arrangement may modify the map of groups, so we get a copy first
			Vector<Group> groupsToHandle = new Vector<Group>(this.groups.values());
			// START KGU#901 2020-12-30: Issue #901
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			// END KGU#901 2020-12-30
			for (Group group: groupsToHandle) {
				/* It doesn't make sense to save the default group on window closing event
				 * unless it contains some interdependent diagrams not shared anywhere.
				 * (if it exists then it will always be unsaved and it's usually not a
				 * conscious arrangement but just a dump of some diagrams e.g. used as subroutines */
				if (groups.containsValue(group) && group.hasChanged() && (!goingToClose || !(group.isDefaultGroup() && !uniquelyHoldsDependents(group)))) {
					if (this.saveArrangement(initiator, group, goingToClose) == null) {
						allDone = false;
						if (group.isDefaultGroup()) {
							unsaved.addIfNew(ArrangerIndex.msgDefaultGroupName.getText());
						}
						else {
							unsaved.addIfNew(group.getName());
						}
					}
				}
			}
		}
		finally {
			// START KGU#901 2020-12-30: Issue #901
			setCursor(origCursor);
			// END KGU#901 2020-12-30
			lu.fisch.structorizer.gui.Diagram.endSerialMode();
		}
		if (!unsaved.isEmpty() && initiator != null) {
			allDone = JOptionPane.showConfirmDialog(initiator,
					msgUnsavedGroups.getText() + "\n" + unsaved.concatenate(", "), 
					this.msgSaveDialogTitle.getText(),
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
		}
		return allDone;
	}
	// END KGU#626 2019-01-01/05

	// START KGU#631 2019-01-08: Bugfix #623
	/**
	 * Checks whether the given group is the only common group of a set of diagrams with
	 * (mutual or not) dependencies.
	 * @param group - some {@link Group}
	 * @return true if there is at least one pair of contained 
	 */
	private boolean uniquelyHoldsDependents(Group group) {
		Set<Diagram> members = group.getDiagrams();
		for (Diagram diagr: members) {
			StringList groupNames = new StringList(diagr.getGroupNames());
			Vector<Call> containedCalls = diagr.root.collectCalls();
			for (Call call: containedCalls) {
				Function fct = call.getCalledRoutine();
				if (fct != null && fct.isFunction()) {
					Vector<Root> candidates = this.findRoutinesBySignature(fct.getName(), fct.paramCount(), null, false);
					if (containsUnsharedPartner(candidates, diagr, members, group.getName(), groupNames)) {
						return true;
					}
				}
			}
			StringList includeNames = diagr.root.includeList;
			if (includeNames != null) {
				for (int i = 0; i < includeNames.count(); i++) {
					Vector<Root> candidates = this.findIncludesByName(includeNames.get(i), null, false);
					if (containsUnsharedPartner(candidates, diagr, members, group.getName(), groupNames)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	// END KGU#631 2019-01-08

	/**
	 * Checks if a diagram among {@code candidates}, which is not represented by {@code diagr}
	 * but element of {@code members} is member of any group with name contained in {@code groupNames}
	 * but not equal to {@code groupName}.
	 * @param candidates - a set of {@link Root} objects to check
	 * @param diagr - a @{@link Diagram} representing a {@code candidates} member to be ignored  
	 * @param members - set of member diagrams of group {@code groupName}
	 * @param groupName - name of the owning group not counting as match
	 * @param groupNames - names of interesting groups
	 */
	private boolean containsUnsharedPartner(Vector<Root> candidates, Diagram diagr, Set<Diagram> members, String groupName,
			StringList groupNames) {
		for (Root cand: candidates) {
			Diagram called = this.rootMap.get(cand);
			if (called != diagr && members.contains(called)) {
				boolean shared = false;
				for (String gName: called.getGroupNames()) {
					if (groupNames.contains(gName) && !gName.equals(groupName)) {
						shared = true;
						break;
					}
				}
				if (!shared) {
					// So there is at least one pair uniquely residing here
					return true;
				}
			}
		}
		return false;
	}

	protected void enableGroupDrawing(boolean selected) {
		this.drawGroups = selected;
		this.pop.setVisible(selected);
		repaint();
	}

	public void enableGroupSelection(boolean selected) {
		this.selectGroups = selected;
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		pop.setVisible(false);
	}

	/**
	 * Renames the given arrangement {@code group} to {@code newName} including the possibly
	 * associated arrangement [archive] file. Tries interactively to resolve detected problems
	 * like name collision with other groups or existing files.
	 * @param group - the {@link Group} to be renamed;
	 * @param newName - the new name for the group;
	 * @param initiator - the {@link Component} owning the control that initiated the renaming
	 * (to associate dialogs to).
	 * @return true if the renaming worked apparently.
	 */
	public boolean renameGroup(Group group, String newName, Component initiator) {
		String oldName = group.getName();
		// START KGU#911 2021-01-11: Enh. #910 Dirty attempt to avoid usurping a "Diagram Controllers" group
		//if (this.hasGroup(newName)) {
		if (newName.equals(Arranger.DIAGRAM_CONTROLLER_GROUP_NAME) || this.hasGroup(newName)) {
		// END KGU#911 2021-01-11
			JOptionPane.showMessageDialog(initiator,
					msgGroupExists.getText().replace("%", newName),
					titleRenameGroup.getText().replace("%1", group.getName()).replace("%2", newName),
					JOptionPane.ERROR_MESSAGE);
			return false;			
		}
		if (group.getFile() != null) {
			String ext = ".arrz";
			File arrFile = group.getFile();
			File file = group.getArrzFile(true);
			if (file == null) {
				ext = ".arr";
				file = arrFile;
			}
			String newFilename = newName;
			if (!newFilename.endsWith(ext)) {
				newFilename = newName + ext;
			}
			File newFile = new File(file.getParent() + File.separator + newFilename);
			int answer = JOptionPane.showConfirmDialog(initiator,
					msgRenameArrFile.getText().replace("%1", file.getAbsolutePath()).replace("%2", "..." + File.separator + newFilename),
					titleRenameGroup.getText().replace("%1", oldName).replace("%2", newName),
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
				return false;
			}
			else if (answer == JOptionPane.YES_OPTION) {
				if (newFile.exists()) {
					answer = JOptionPane.showConfirmDialog(initiator,
							msgOverwriteFile.getText().replace("%1", newFile.getAbsolutePath()),
							msgConfirmOverwrite.getText(),
							JOptionPane.YES_NO_CANCEL_OPTION);
					if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
						return false;
					}
					else if (answer == JOptionPane.YES_OPTION) {
						newFile.delete();
					}
				}
				if (answer == JOptionPane.YES_OPTION) {
					// KGU#717 - this should always happen within the same folder, so this simple method should suffice
					if (!file.renameTo(newFile)) {
						JOptionPane.showMessageDialog(initiator,
								msgRenamingFailed.getText().replace("%", newFile.getAbsolutePath()),
								titleRenameGroup.getText().replace("%1", group.getName()).replace("%2", newName),
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					if (ext.equals(".arr")) {
						group.setFile(newFile, null, false);
					}
					else {
						group.setFile(arrFile, newFile, true);
					}
				}
			}
		}
		if (group.rename(newName)) {
			this.groups.remove(oldName);
			groups.put(newName, group);
		}
		this.notifyChangeListeners(IRoutinePoolListener.RPC_NAME_CHANGED);
		return true;
	}

	// START KGU#679 2019-03-13: Enh. #698
	/**
	 * @return the name of this pool if it has got one, otherwise null
	 */
	@Override
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
	// END KGU#679 2019-03-13
	
	// START KGU#911 2021-01-13: Enh. #910 Group visibility new interpreted
	private boolean isVisible(Diagram diagr)
	{
		for (Group group: this.getGroups(diagr)) {
			if (group.isVisible()) {
				return true;
			}
		}
		return false;
	}
	// END KGU#911 2021-01-13
	
	// DEBUG
//	private void printNameMap(int lineNo)
//	{
//		System.out.println("--------------------------------- " + lineNo + " -----------------------------------");
//		for (Entry<String, Vector<Diagram>> entry: nameMap.entrySet()) {
//			Vector<Diagram> diagrs = entry.getValue();
//			if (diagrs.size() > 1) {
//				System.out.println(entry.getKey() + ": ");
//				for (Diagram diagr: diagrs) {
//					StringList grpnames = new StringList(diagr.getGroupNames());
//					System.out.println("\t" + diagr.root.getSignatureString(true) + ": " + grpnames.concatenate(","));
//				}
//			}
//			else {
//				Diagram diagr = diagrs.get(0);
//				StringList grpnames = new StringList(diagr.getGroupNames());
//				System.out.println(entry.getKey() + "= " + diagr.root.getSignatureString(true) + ": " + grpnames.concatenate(","));
//			}
//		}
//	}

}

/*
    Structorizer :: Arranger
    A little tool which you can use to arrange Nassi-Schneiderman Diagrams (NSD)

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
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2018-06-10 (Kay Gürtzig)
 *      - The change was made to comply with the Oracle Swing debugging guidelines
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
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
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import lu.fisch.graphics.Rect;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Updater;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.executor.IRoutinePool;
import lu.fisch.structorizer.executor.IRoutinePoolListener;
import lu.fisch.structorizer.generators.XmlGenerator;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.gui.Mainform;
import lu.fisch.structorizer.gui.Menu;
import lu.fisch.structorizer.io.ArrFilter;
import lu.fisch.structorizer.io.ArrZipFilter;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.io.PNGFilter;
import lu.fisch.structorizer.locales.LangPanel;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.parsers.NSDParser;
import lu.fisch.utils.StringList;
import net.iharder.dnd.FileDrop;

/**
 * Class represents the interactive viewport for arranging several Nassi Shneiderman
 * diagrams (as part of a scroll pane).<br/>
 * 2018-03-13: Enhanced the inheritance by {@link MouseWheelListener} to enable ctrl + wheel zooming 
 * @author robertfisch, codemanyak
 */
@SuppressWarnings("serial")
public class Surface extends LangPanel implements MouseListener, MouseMotionListener, WindowListener, Updater, IRoutinePool, ClipboardOwner, MouseWheelListener {

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

	/** Current/Last actual mouse coordinates (i.e. without regarding the zoom-factor) */
	private Point mousePoint = null;
	// START KGU#624 2018-12-21: Enh. #655 Diagram -> Set<Diagram>
	//private Diagram mouseSelected = null;
	/** The {@link Diagram}s currently selected via mouse click */
	private final Set<Diagram> diagramsSelected = new HashSet<Diagram>();
	// END KGU#624 2018-12-21
	// START KGU#624 2018-12-23: Enh. #655 Drag a selection area
	private Rectangle dragArea = null;
	// END KGU#624 2018-12-23
	// START KGU#88 2015-11-24: We may often need the pin icon
	/** Caches the icon used to indicate pinned state in the requested size */
	public static Image pinIcon = null;
	// END KGU#88 2015-11-24
	// START KGU#497 2018-02-17: Enh. #   - new zoom facility
	/** The factor by which the drawing is currently downscaled */
	private float zoomFactor = 2.0f;
	// END KGU#497 2018-02-17
	// START KGU#110 2015-12-21: Enh. #62, also supports PNG export
	public File currentDirectory = new File(System.getProperty("user.home"));
	// END KGU#110 2015-12-21
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
	public final LangTextHolder[] msgInsertionConflict = {
			new LangTextHolder("There is another version of diagram \"%2\",\nat least one of them has unsaved changes."),
			new LangTextHolder("There is an equivalent copy of diagram \"%1\"\nwith different path \"%2\"."),
			new LangTextHolder("There is a differing diagram with signature \"%1\"\nand path \"%2\".")
	};
	// END KGU#312 2016-12-29
	// START KGU#385 2017-04-22: Enh. #62
	public static final LangTextHolder msgOverwriteFile = new LangTextHolder("Overwrite existing file \"%\"?");
	public static final LangTextHolder msgConfirmOverwrite = new LangTextHolder("Confirm Overwrite");
	// END KGU#385 2017-04-22

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
		//System.out.println("Surface: " + System.currentTimeMillis());
		// Region occupied by diagrams
		Dimension area = new Dimension(0, 0);
		super.paintComponent(g);
		if (diagrams != null)
		{
			// START KGU#497 2018-02-17: Enh. #512
			Graphics2D g2d = (Graphics2D) g;
			// START KGU#572 2018-09-09: Bugfix #508/#512 - ensure all diagrams have shape without rounding defects
			for(int d=0; d<diagrams.size(); d++)
			{
				// START KGU#624 2018-12.24: Enh. #655
				//diagrams.get(d).root.prepareDraw(g2d);
				Diagram diagr = diagrams.get(d);
				if ((!onlySelected || this.diagramsSelected.contains(diagr)) && diagr.root != null) {
					// If the diagram had already been drawn or prepared this will return immediately
					diagr.root.prepareDraw(g2d);
				}
				// END KGU#624 2018-12-24
			}
			// END KGU#572 2018-09-09
			// START KGU#497 2018-03-19: Enh. #512
			//g2d.scale(1/zoomFactor, 1/zoomFactor);
			// END KGU#497 2018-02-17
			if (compensateZoom) {
				// In zoom-compensated drawing the background filled by super.paint(g)
				// is too small (virtually scaled don), therefore we must draw a
				// white rectangle covering the enlarged image area
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0,
						// START KGU#624 2018-12-24: Enh. #655
						//Math.round(this.getWidth() * zoomFactor),
						//Math.round(this.getHeight() * zoomFactor)
						Math.round(this.getWidth() * zoomFactor - offsetX),
						Math.round(this.getHeight() * zoomFactor - offsetY)
						// END KGU#624 2018-12-24
						);
			}
			else {
				g2d.scale(1/zoomFactor, 1/zoomFactor);
			}
			// END KGU#497 2018-03-19
//			System.out.println("Surface.paintComponent()");
			for(int d=0; d<diagrams.size(); d++)
			{
				Diagram diagram = diagrams.get(d);
				// START KGU#624 2018-12-24: Enh. #655
				if (onlySelected && !this.diagramsSelected.contains(diagram)) {
					continue;
				}
				// END KGU#624 2018-12-24
				
				Root root = diagram.root;
				Point point = diagram.point;

				// START KGU#624 2018-12-24: Enh. #655
				if (offsetX != 0 || offsetY != 0) {
					point = new Point(point.x - offsetX, point.y - offsetY);
				}
				// END KGU#624 2018-12-24
				// START KGU#88 2015-11-24
				//root.draw(g, point, this);
				Rect rect = root.draw(g2d, point, this, Element.DrawingContext.DC_ARRANGER);
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
				g2d.scale(zoomFactor, zoomFactor);
			}
			// END KGU#497 2018-03-19
		}
		// START KGU#85 2017-10-23: Enh. #35 - now make sure the scrolling area is up to date
		area.width = Math.round(Math.min(area.width, Short.MAX_VALUE) / this.zoomFactor);
		area.height = Math.round(Math.min(area.height, Short.MAX_VALUE) / this.zoomFactor);
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
		new  FileDrop(this, new FileDrop.Listener()
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
	public int loadFiles(java.io.File[] files)
	{
		// START KGU#624 2018-12-22: Enh. #655 - Clear the selection before
		this.unselectAll();
		// END KGU#624 2018-12-22
		// We try to load as many files of the list as possible and collect the error messages
		int nLoaded = 0;
		String troubles = "";
		for (int i = 0; i < files.length; i++)
		{
			String filename = files[i].toString();
			String errorMessage = loadFile(filename);
			// START KGU#153 2016-03-03: Bugfix #121 - a successful load must not add to the troubles text
			//if (!troubles.isEmpty()) { troubles += "\n"; }
			//troubles += "\"" + filename + "\": " + errorMessage;
			//System.err.println("Arranger failed to load \"" + filename + "\": " + troubles);
			if (!errorMessage.isEmpty())
			{
				if (!troubles.isEmpty()) { troubles += "\n"; }
				String trouble = "\"" + filename + "\": " + errorMessage;
				troubles += trouble;
				logger.log(Level.INFO, "Arranger failed to load " + trouble);	
			}
			else
			{
				nLoaded++;
			}
			// END KGU#153 2016-03-03
		}
		if (!troubles.isEmpty())
		{
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
	
	private String loadFile(String filename)
	{
		return loadFile(filename, null);
	}

	private String loadFile(String filename, Point point)
	// START KGU#289 2016-11-15: Enh. #290 (load arrangements from Structorizer)
	{
		// START KGU#316 2016-12-28: Enh. #318
		//return loadFile(null, filename, point);
		return loadFile(null, filename, point, null);
		// END KGU#316 2016-12-28
	}

	// START KGU#316 2016-12-28: Enh. #318 signature enhanced to keep track of unzipped files 
	//private String loadFile(Mainform form, String filename, Point point)
	private String loadFile(Mainform form, String filename, Point point, String unzippedFrom)
	// END KGU#316 2016-12-28
	// END KGU#289 2016-11-15
	{
		// START KGU#363 2018-09-11: Improvement for temporary legacy nsd files extracted from arrz files
		File zipFile = null;
		if (unzippedFrom != null) {
			zipFile = new File(unzippedFrom);
		}
		// END KGU#363 2018-09-11
		String errorMessage = "";
		String ext = filename.substring(Math.max(filename.lastIndexOf("."),0));
		if (ext.equalsIgnoreCase(".nsd"))
		{
			// open an existing file
			NSDParser parser = new NSDParser();
			File f = new File(filename);	// FIXME (KGU) Why don't we just use files[i]?
			// START KGU#111 2015-12-17: Bugfix #63: We must now handle a possible exception
			try {
				// END KGU#111 2015-12-17
				// START KGU#363 2017-05-21: Issue #372 API change
				//Root root = parser.parse(f.toURI().toString());
				// START KGU#363 2018-09-11: Improvement for temporary legacy nsd files extracted from arrz files
				//Root root = parser.parse(f);
				Root root = parser.parse(f, zipFile);
				// END KGU#363 2018-09-11
				// END KGU#363 2017-05-21

				root.filename = filename;
				// START KGU#316 2016-12-28: Enh. #318 Allow nsd files to "reside" in arrz files
				if (unzippedFrom != null) {
					root.filename = unzippedFrom + File.separator + (new File(filename)).getName();
					root.shadowFilepath = filename;
				}
				// END KGU#316 2016-12-28
				// START KGU#382 2017-04-15: Ensure highlighting mode has effect
				//root.highlightVars = Element.E_VARHIGHLIGHT;
				root.getVarNames();	// Initialise the variable table, otherwise the highlighting won't work
				// END KGU#382 2017-04-15
				// START KGU#289 2016-11-15: Enh. #290 (load from Mainform)
				//addDiagram(root, point);
				addDiagram(root, form, point);
				// END KGU#289 2016-11-15
				// START KGU#111 2015-12-17: Bugfix #63: We must now handle a possible exception
			}
			catch (Exception ex) {
				errorMessage = ex.getLocalizedMessage();
			}
			// END KGU#111 2015-12-17
		}
		// START KGU#289 2016-11-14: Enh. #289
		else if (ext.equalsIgnoreCase(".arr") || ext.equalsIgnoreCase(".arrz"))
		{
			errorMessage = loadArrFile(form, filename);
		}
		// END KGU#289 2016-11-14
		return errorMessage;
	}

	/**
	 * Stores the current diagram arrangement (new with version3.28-13: only selected diagrams)
	 * to a file.<br/>
	 * Depending on the choice of the user, this file will either be only a list of reference
	 * points and filenames (this way not being portable) or be a compressed archive containing
	 * the list file as well as the referenced NSD files will be produced such that it can be
	 * ported to a different location and extracted there.
	 *  
	 * @param frame - the commanding GUI component
	 * @return status flag (true iff the saving succeeded without error) 
	 */
	public boolean saveArrangement(Frame frame)
	{
		boolean done = false;
		// START KGU#110 2016-06-29: Enh. #62
		boolean portable = false;
		String extension = "arr";
		// END KGU#110 2016-06-29
		// Ensure the diagrams themselves have been saved
		int answer = JOptionPane.CANCEL_OPTION;
		// START KGU#624 2018-12-22: Enh. #655
		// TODO Provide a choice table with checkboxes for every diagram (selected ones already checked)
		StringList rootNames = listSelectedRoots(true, false);
		int nSelected = rootNames.count();
		if (nSelected > Arranger.ROOT_LIST_LIMIT) {
			rootNames.remove(Arranger.ROOT_LIST_LIMIT, nSelected);
			rootNames.add("...");
		}
		String saveMessage = Arranger.msgConfirmMultiple.getText().
				replace("%1", Integer.toString(nSelected)).
				replace("%2", Integer.toString(this.diagrams.size())).
				replace("%3", rootNames.concatenate("\n- ")).
				replace("%4", msgSavePortable.getText());
		Object[] options = {lblSaveAsArrz.getText(), lblSaveAsArr.getText(), Menu.lblCancel.getText()};
		// END KGU#624 2018-12-22
		if (this.saveDiagrams() && 
				(answer = JOptionPane.showOptionDialog(frame,
						saveMessage, this.msgSaveDialogTitle.getText(),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null,
						options, options[0])) != JOptionPane.CANCEL_OPTION)
		{
			// Let's select path and name for the list / archive file
			JFileChooser dlgSave = new JFileChooser(currentDirectory);
			dlgSave.setDialogTitle(msgSaveDialogTitle.getText());
			// START KGU#110 2016-06-29: Enh. #62
			//dlgSave.addChoosableFileFilter(new ArrFilter());
			if (answer == JOptionPane.OK_OPTION)
			{
				FileFilter filter = new ArrZipFilter();
				dlgSave.addChoosableFileFilter(filter);
				dlgSave.setFileFilter(filter);				
				portable = true;
				extension += "z";
			}
			else
			{
				FileFilter filter = new ArrFilter();
				dlgSave.addChoosableFileFilter(filter);
				dlgSave.setFileFilter(filter);
			}
			// END KGU#110 2016-06-29
			dlgSave.setCurrentDirectory(currentDirectory);
			int result = dlgSave.showSaveDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				currentDirectory = dlgSave.getCurrentDirectory();
				while (currentDirectory != null && !currentDirectory.isDirectory())
				{
					currentDirectory = currentDirectory.getParentFile();
				}
				// correct the filename if necessary
				String filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
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
				boolean writeNow = true;
				File f = new File(filename + "." + extension);
				if (f.exists())
				{
					writeNow = false;
					int res = JOptionPane.showConfirmDialog(
							this,
							msgOverwriteFile.getText().replace("%", f.getAbsolutePath()),
							msgConfirmOverwrite.getText(),
							JOptionPane.YES_NO_OPTION);
					writeNow = (res == JOptionPane.YES_OPTION);
				}
				if (writeNow) {
					done = saveArrangement(frame, filename, extension, portable);
				}
				// END KGU#385 2017-04-22
				// END KGU#110 2016-06-29
			}
		}
		return done;
	}

	/**
	 * Stores the current diagram arrangement to a file.
	 * If `portable´ is false, this file will only contain a list of points and filenames.
	 * Otherwise a compressed archive containing the list file as well as the referenced
	 * NSD files will be produced such that it can be ported to a different location and
	 * extracted there.
	 *  
	 * @param frame - the commanding GUI component
	 * @param filename - the base path of the selected file (without extension)
	 * @param extension - the file extension
	 * @param portable - whether a portable zip file is to be created
	 * @return status flag (true iff the saving succeeded without error) 
	 */
	// START KGU#110 2016-06-29. Enh. #62
	//public boolean saveArrangement(Frame frame, String filename)
	public boolean saveArrangement(Frame frame, String filename, String extension, boolean portable)
	// END KGU#110 2016-06-29
	{
		boolean done = false;
		String outFilename = filename + "." + extension;		// Name of the actually written file
		String tmpFilename = null;

		// Find a suited temporary directory to store the output file(s) if needed
		String tempDir = findTempDir();
		if ((tempDir == null || tempDir.isEmpty()))
		{
			File dir = new File(".");
			if (dir.isFile())
			{
				tempDir = dir.getParent();
			}
			else
			{
				tempDir = dir.getAbsolutePath();
			}
		}

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
			}
			else if (file.exists())
				// END KGU#110 2016-06-29
			{
				// name for a temporary arr file
				arrFilename = tempDir + File.separator + "Temp." + extension;
				tmpFilename = arrFilename;
			}
			// Now actually save the arr file
			saveArrFile(arrFilename, portable);

			// START KGU#110 2016-06-29: Enh. #62
			// Now zip all files together if a portable file is requested
			if (portable)
			{
				// Create a zip file from the nsd files and the arr file
				// (returns the name of the zip file if it was placed in
				// a temp directory, otherwise null).
				tmpFilename = zipAllFiles(outFilename, arrFilename, tempDir);
			}


			// If the target file had existed then replace it by the output file after having created a backup
			if (tmpFilename != null)
			{
				File backUp = new File(outFilename + ".bak");
				if (backUp.exists())
				{
					backUp.delete();
				}
				file.renameTo(backUp);
				file = new File(outFilename);
				File tmpFile = new File(tmpFilename);
				tmpFile.renameTo(file);
			}

			done = true;
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(frame, msgSaveError.getText() + " " + ex.getMessage() + "!",
					"Error", JOptionPane.ERROR_MESSAGE, null);
		}
		return done;
	}


	/**
	 * Creates the Arranger file with path `arrFilename´ from all held diagrams.
	 * @param arrFilename - target path of the Arranger file
	 * @param pureNames - determines whether only the pure file names (or the
	 * entire paths) are to be referred to by the arr file.
	 * @throws IOException
	 */
	private void saveArrFile(String arrFilename, boolean pureNames) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(arrFilename);
		Writer out = new OutputStreamWriter(fos, "UTF8");
		for (int d = 0; d < this.diagrams.size(); d++)
		{
			Diagram diagr = this.diagrams.get(d);
			// START KGU#624 2018-12-22: Enh. #655 - no only selected diagrams!
			if (!this.diagramsSelected.isEmpty() && !this.diagramsSelected.contains(diagr)) {
				continue;
			}
			// END KGU#624 2018-12-22
			String path = diagr.root.getPath();
			// KGU#110 2016-07-01: Bugfix #62 - don't include diagrams without file
			if (!path.isEmpty())
			{
				out.write(Integer.toString(diagr.point.x) + ",");
				out.write(Integer.toString(diagr.point.y) + ",");
				StringList entry = new StringList();
				if (pureNames)
				{
					File nsdFile = new File(path);
					path = nsdFile.getName();	// Only last part of path
				}
				entry.add(path);
				out.write(entry.getCommaText()+'\n');
			}
		}

		out.close();
	}

	/**
	 * Compresses the arranged diagrams and the describing arr file (`arrFilename´)
	 * into file `zipFilename´ (which is essentially an ordinary zip file but named as given).
	 * @param zipFilename - path of the arrz file (zip file) to be created
	 * @param arrFilename - path of the existing arr file holding the positions
	 * @param tmpPath - path of a temporary directory for the case the target
	 * file already exists
	 * @return the path of the created temporary file if the target file `zipFilename´ had
	 * existed (otherwise null)
	 */
	private String zipAllFiles(String zipFilename, String arrFilename, String tmpPath) throws IOException
	{
		final int BUFSIZE = 2048;

		String tmpFilename = zipFilename + "";
		// set up the file (and check whether it has existed already)
		File file = new File(zipFilename);
		boolean fileExisted = file.exists(); 
		if (fileExisted)
		{
			tmpFilename = tmpPath + File.separator + "Arranger.zip";
		}

		BufferedInputStream origin = null;
		FileOutputStream dest = new	FileOutputStream(tmpFilename);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		//out.setMethod(ZipOutputStream.DEFLATED);
		byte buffer[] = new byte[BUFSIZE];
		StringList filePaths = new StringList();
		// Add the diagram file names
		for (int d = 0; d < this.diagrams.size(); d++)
		{
			Diagram diagr = this.diagrams.get(d);
			// START KGU#624 2018-12-22: Enh. #655 - no only selected diagrams!
			if (!this.diagramsSelected.isEmpty() && !this.diagramsSelected.contains(diagr)) {
				continue;
			}
			// END KGU#624 2018-12-22
			// START KGU#316 2017-04-22: Enh. #318: Files might be zipped
			//String path = diagr.root.getPath();
			String path = diagr.root.shadowFilepath;
			if (path == null) {
				path = diagr.root.getPath();
			}
			// END KGU#316 2017-04-22
			if (!path.isEmpty())
			{
				filePaths.add(path);
			}
		}
		filePaths.add(arrFilename);

		int count;
		for (int i = 0; i < filePaths.count(); i++)
		{
			FileInputStream fis = new FileInputStream(filePaths.get(i));
			origin = new BufferedInputStream(fis, BUFSIZE);
			ZipEntry entry = new ZipEntry(new File(filePaths.get(i)).getName());
			// START KGU 2018-09-12: Preserve time attributes if possible
			Path srcPath = (new File(filePaths.get(i))).toPath();
			try {
				BasicFileAttributes attrs = Files.readAttributes(srcPath, BasicFileAttributes.class);
				FileTime modTime = attrs.lastModifiedTime();
				FileTime creTime = attrs.creationTime();
				if (modTime.toMillis() > 0) {
					entry.setLastModifiedTime(modTime);
				}
				if (creTime.toMillis() > 0) {
					entry.setCreationTime(creTime);
				}
			} catch (IOException e) {}
			// END KGU 2018-09-12
			out.putNextEntry(entry);
			while((count = origin.read(buffer, 0, BUFSIZE)) != -1)
			{
				out.write(buffer, 0, count);
			}
			origin.close();
		}

		out.close();
		if (!fileExisted)
		{
			tmpFilename = null;
		}
		return tmpFilename;
	}

	/**
	 * Action method for the "Load List" button of the Arranger: Attempts to load the
	 * specified (portable) arrangement file.
	 * @param frame - the owning frame object. 
	 * @return true iff the saving succeeded (will raise an error message else).
	 */
	public boolean loadArrangement(Frame frame)
	{
		boolean done = false;
		// Ensure the previous diagrams themselves are saved
		if (this.saveDiagrams())
		{
			// Let's select path and name for the list / archive file
			JFileChooser dlgOpen = new JFileChooser(currentDirectory);
			dlgOpen.setDialogTitle(msgLoadDialogTitle.getText());
			// START KGU#100 2016-01-15: Enh. #62 - select the provided filter
			//dlgOpen.addChoosableFileFilter(new ArrFilter());
			// START KGU#110 2016-07-01: Enh. #62 - Add the zipped filter
			dlgOpen.addChoosableFileFilter(new ArrZipFilter());
			// END KGU#110 2016-07-01
			ArrFilter filter = new ArrFilter();
			dlgOpen.addChoosableFileFilter(filter);
			dlgOpen.setFileFilter(filter);
			// END KGU#110 2016-01-15: Enh. #62

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
				String filename = dlgOpen.getSelectedFile().getAbsoluteFile().toString();

				// START KGU#316 2016-12-28: Enh. #318
				String unzippedFrom = null;
				// END KGU#316 2016-12-28
				// START KGU#110 2016-07-01: Enh. # 62 - unpack a zip file first
				if (ArrZipFilter.isArr(filename))
				{
					String extractTo = null;
					dlgOpen.setDialogTitle(msgExtractDialogTitle.getText());
					dlgOpen.resetChoosableFileFilters();
					dlgOpen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					dlgOpen.setAcceptAllFileFilterUsed(false);
					if (dlgOpen.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
					{
						//extractTo = dlgOpen.getCurrentDirectory().getAbsolutePath();
						extractTo = dlgOpen.getSelectedFile().getAbsolutePath();
					}
					else {
						unzippedFrom = filename;
					}
					filename = unzipArrangement(filename, extractTo);
					if (filename != null)
					{
						currentDirectory = new File(filename);
						while (currentDirectory != null && !currentDirectory.isDirectory())
						{
							currentDirectory = currentDirectory.getParentFile();
						}
					}
				}
				// END KGU#110 2016-07-01
				// START KGU#316 2016-12-28: Enh. #318
				//done = loadArrangement(frame, filename);
				done = loadArrangement(frame, filename, unzippedFrom);
				// END KGU#316 2016-12-28
				currentDirectory = oldCurrDir;
			}
		}
		return done;
	}

	/**
	 * Restores the diagram arrangement stored in arr file `filename´ if possible
	 * (i.e. given the referred nsd file paths exist and the nsd files may be parsed)
	 * @param frame - owning frame component
	 * @param filename - path of the arr file to be reloaded
	 * @param unzippedFrom - path of the arrz file if shadowed to temporary directory, null otherwise
	 * @return true if at lest some of the referred diagrams could be arranged again.
	 */
	// START KGU#316 2016-12-28: Enh. #318 API change to support shadow paths in unzipped Roots
	//public boolean loadArrangement(Frame frame, String filename)
	public boolean loadArrangement(Frame frame, String filename, String unzippedFrom)
	// END KGU#316 2016-12-28
	{
		boolean done = false;
		// START KGU#278 2016-10-11: Enh. #267
		int nLoaded = 0;
		// END KGU#278 2016-10-11
		
		// START KGU#624 2018-12-22: Enh. #655 clear the selection such that only the loaded files wil be selected
		this.unselectAll();
		// END KGU#624 2018-12-22

		String errorMessage = null;
		try
		{
			// START KGU#316 2016-12-28: Enh. #318 don't get confused by the loading of some files
			String prevCurDirPath = this.currentDirectory.getAbsolutePath();
			// END KGU#316 2016-12-28
			// set up the file
			File file = new File(filename);
			//Pattern separator = new Pattern(",");
			Scanner in = new Scanner(file, "UTF8");
			while (in.hasNextLine())
			{
				String line = in.nextLine();
				StringList fields = StringList.explode(line, ",");
				if (fields.count() >= 3)
				{
					Point point = new Point();
					point.x = Integer.parseInt(fields.get(0));
					point.y = Integer.parseInt(fields.get(1));
					String nsdFileName = fields.get(2);
					if (nsdFileName.startsWith("\""))
						nsdFileName = nsdFileName.substring(1);
					if (nsdFileName.endsWith("\""))
						nsdFileName = nsdFileName.substring(0, nsdFileName.length() - 1);
					File nsd = new File(nsdFileName);
					if (!nsd.exists() && !nsd.isAbsolute())
					{
						// START KGU#316 2016-12-28: Enh. #318 don't get confused by the loading of some files
						//nsdFileName = currentDirectory.getAbsolutePath() + File.separator + nsdFileName;
						nsdFileName = prevCurDirPath + File.separator + nsdFileName;
						// END KGU#316 2016-12-28
					}
					// START KGU#289 2016-11-15: Enh. #290 (Arrangements loaded from Mainform)
					//String trouble = loadFile(nsdFileName, point);
					String trouble = loadFile((frame instanceof Mainform) ? (Mainform)frame : null, nsdFileName, point, unzippedFrom);
					// END KGU#289 2016-11-15
					// START KGU#625 2018-12-22: Bugfix #656 - It might be that the arr file refers to virtual arrz paths
					if (!trouble.isEmpty() && !nsd.exists() && unzippedFrom == null && nsdFileName.contains(".arrz")) {
						try {
							// Might be a path into an arrz file from which the referred diagram had originally been loaded
							File arrzFile = nsd.getParentFile();
							String pureName = nsd.getName();
							if (arrzFile.exists()) {
								String extractedArrPath = unzipArrangement(arrzFile.getAbsolutePath(), null);
								if (extractedArrPath != null) {
									File targetDir = (new File(extractedArrPath)).getParentFile(); 
									if (targetDir.exists() && (nsd = new File(targetDir.getAbsolutePath() + File.separator + pureName)).exists()) {
										// Now let's try again
										String newTrouble = loadFile((frame instanceof Mainform) ? (Mainform)frame : null, nsd.getAbsolutePath(), point, arrzFile.getAbsolutePath());
										if (newTrouble.isEmpty()) {
											trouble = "";
										}
										else {
											trouble += "\n   " + newTrouble;
										}
									}
								}
							}
						}
						catch (Exception ex) {
							trouble += "\n    " + ex.toString();
						}
					}
					// END KGU#625 2018-12-22
					if (!trouble.isEmpty())
					{
						if (errorMessage != null)
						{
							errorMessage += "\n" + trouble;
						}
						else {
							errorMessage = trouble;
						}
					}
					// START KGU#278 2016-10-11: Enh. #267
					else {
						nLoaded++;
					}
					// END KGU#278 2016-10-11
				}
			}

			in.close();

			done = true;
		}
		catch (Exception ex)
		{
			if (filename == null)
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
	 * Mainform form if possible.
	 * @param form - a commanding Structorizer Mainform
	 * @param filename - path of the arrangement file to be loaded
	 * @return A rough error message if something went wrong.
	 */
	public String loadArrFile(Mainform form, String filename)
	{
		String errorMessage = "";
		File oldCurrDir = currentDirectory;
		// START KGU#316 2016-12-28: Enh. #318 Keep track of the original arrz path
		String arrzPath = null;
		// END KGU#316 2016-12-28
		if (filename.toLowerCase().endsWith(".arrz")) {
			arrzPath = filename;
			filename = unzipArrangement(filename, null);
		}
		if (filename != null)
		{
			try {
				currentDirectory = new File(filename);
				while (currentDirectory != null && !currentDirectory.isDirectory())
				{
					currentDirectory = currentDirectory.getParentFile();
				}
				// START KGU#316 2016-12-28: Enh. #318
				//if (!loadArrangement(form, filename)) {
				if (!loadArrangement(form, filename, arrzPath)) {
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
	 * @param filename - path of the arrz file
	 * @param targetDir - target directory path for the unzipping (may be null)
	 * @return the path of the arr file found in the extracted archive (or otherwise null) 
	 */
	private String unzipArrangement(String filename, String targetDir)
	{
		final int BUFSIZE = 2048;
		String arrFilename = null;
		if (targetDir == null)
		{
			// START KGU#316 2016-12-28: Enh. #318
			//targetDir = findTempDir();
			boolean tmpDirCreated = false;
			try {
				// We just force the existence of the folder hierarchy in the temp directory
				File tempFile = File.createTempFile("arr", null);
				tempFile.delete();	// We don't need the file itself
				targetDir = tempFile.getParent() + File.separator + (new File(filename)).getName();
				tmpDirCreated = (new File(targetDir)).mkdirs();
			} catch (IOException ex) {
				logger.log(Level.WARNING, "Failed to unzip the arrangement archive: {0}", ex.getLocalizedMessage());
			}
			if (!tmpDirCreated) {
				targetDir = findTempDir();
			}
			// END KGU#316 2016-12-28
		}
		try {
			BufferedOutputStream dest = null;
			BufferedInputStream bistr = null;
			ZipEntry entry;
			ZipFile zipfile = new ZipFile(filename);
			Enumeration<? extends ZipEntry> entries = zipfile.entries();
			while(entries.hasMoreElements()) {
				entry = (ZipEntry) entries.nextElement();
				String targetName = targetDir + File.separator + entry.getName();
				bistr = new BufferedInputStream
						(zipfile.getInputStream(entry));
				int count;
				byte buffer[] = new byte[BUFSIZE];
				FileOutputStream fostr = new FileOutputStream(targetName);
				dest = new BufferedOutputStream(fostr, BUFSIZE);
				while ((count = bistr.read(buffer, 0, BUFSIZE))	!= -1)
				{
					dest.write(buffer, 0, count);
				}
				dest.flush();
				dest.close();
				bistr.close();
				// START KGU 2018-09-12: Preserve at least the modification time if possible
				Path destPath = (new File(targetName)).toPath();
				try {
					Files.setLastModifiedTime(destPath, entry.getLastModifiedTime());
				} catch (IOException e) {}
				// END KGU 2018-09-12
				if (ArrFilter.isArr(entry.getName()))
				{
					arrFilename = targetName;
				}
			}
			zipfile.close();
		} catch(Exception ex) {
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			logger.log(Level.WARNING, "Failed to unzip the arrangement archive " + filename, ex);
			// END KGU#484 2018-04-05
		}
		return arrFilename;
	}

	/**
	 * Finds a directory for temporary files (trying different OS standard environment variables)
	 * @return path of a temp directory
	 */
	private static String findTempDir()
	{
		String[] EnvVariablesToCheck = { "TEMP", "TMP", "TMPDIR", "HOME", "HOMEPATH" };
		String tempDir = "";
		for (int i = 0; (tempDir == null || tempDir.isEmpty()) && i < EnvVariablesToCheck.length; i++)
		{
			tempDir = System.getenv(EnvVariablesToCheck[i]);
		}
		return tempDir;
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
				offsetX = rect.left - 10;
				offsetY = rect.top - 10;
				rect.left = 10;
				rect.top = 10;
				rect.right -= offsetX;
				rect.bottom -= offsetY;
			}
			int width = this.getWidth() - Math.round(offsetX / zoomFactor);
			int height = this.getHeight() - Math.round(offsetY / zoomFactor);
			// END KGU#624 2018-12-24
			if (logger.isLoggable(Level.CONFIG)) {
				logger.log(Level.CONFIG, "{0} x {1}", new Object[]{width, height});
				logger.log(Level.CONFIG, "Drawing Rect: {0}", rect);
				logger.log(Level.CONFIG, "zoomed: {0} x {1}", new Object[]{width*this.zoomFactor, height*this.zoomFactor});
			}
			BufferedImage bi = new BufferedImage(
					Math.round(width * this.zoomFactor),
					Math.round(height * this.zoomFactor),
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
	 * Determines the union of bounds of all diagrams on this {@link Surface} and updates
	 * the lower silhouette line if {@code _silhouette} is given.
	 * @param _silhouette - a list of pairs {x,y} representing the lower silhouette line
	 * (where the x coordinate represents a leap position and the y coordinate is the new
	 * level from x to the next leap eastwards) or null
	 * @return the bounding box as {@link Rect} (an empty Rect at (0,0) if there are no diaras
	 */
	public Rect getDrawingRect(LinkedList<Point> _silhouette)
	{
		return getDrawingRect(diagrams, _silhouette);
	}

	/**
	 * Determines the union of bounds of the given {@code _diagrams} in true diagram coordinates
	 * and updates the lower silhouette line if {@code _silhouette} is given.
	 * @param _diagrams - a collection of {@link Diagram} objects.
	 * @param _silhouette - a list of pairs {x,y} representing the lower silhouette line
	 * (where the x coordinate represents a leap position and the y coordinate is the new
	 * level from x to the next leap eastwards) or null
	 * @return the bounding box as {@link Rect} (an empty Rect at (0,0) if there are no diaras
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
				//System.out.println(root.getMethodName() + ": (" + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom +")");
				r.left = Math.min(diagram.point.x, r.left);
				r.top = Math.min(diagram.point.y, r.top);
				r.right = Math.max(diagram.point.x + width, r.right);
				r.bottom = Math.max(diagram.point.y + height, r.bottom);
				//END KGU#85 2015-11-18
				// START KGU#499 2018-02-20
				if (_silhouette != null) {
					this.updateSilhouette(_silhouette, diagram.point.x, width, diagram.point.y + height);
				}
				// END KGU#499 2018-02-20
			}
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
	 * @param left - the left edge x  coordinate of the considered diagram
	 * @param width - the widh of the considered diagram
	 * @param bottom - the bottom value of the considered diagram
	 */
	private void updateSilhouette(LinkedList<Point> _silhouette, int left, int width, int bottom) {
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
		while (iter.hasNext() && ((leap = iter.next()).x < left || lastLeap.y >= bottom)) {
			lastLeap = leap;
		}
		// Now if we haven't found any leap at all, then just add the two leaps for this diagram
		Point nextLeap = new Point(left + width, lastLeap.y);
		if (leap == null) {
			Point leap1 = new Point(left, bottom);
			_silhouette.add(leap1);
			_silhouette.add(nextLeap);
		}
		// There is nothing to do if the last leap is already beyond the diagram
		else if (lastLeap.x < left + width) {
			// otherwise there are two fundamental cases:
			if (lastLeap.y >= bottom && lastLeap.x >= left) {
				// 1. The diagram had already started but the silhouette is receding (at lastLeap)
				//    --> update lastLeap to level bottom
				lastLeap.y = bottom;
			}
			else {
				// 2. The diagram starts here and protrudes over the silhouette
				//    --> insert a new leap at position left (or just raise the level at lastLeap)
				Point leap1 = new Point(left, bottom);
				// Was there another leap beyond the last leap (or had the list been exhausted)?
				if (leap.x != lastLeap.x) {
					// There is another leap farther right
					// If the distance between silhouette leap and diagram edge is small then avoid
					// an additional leap and just move the existing leap to left
					if (leap.x - left <= DEFAULT_GAP && leap.y > bottom && left > lastLeap.x) {
						leap.x = left;		// Just move the leap left
					}
					else {
						iter.previous();	// go back before current leap
						iter.add(leap1);	// insert the new leap before it
						iter.next();		// and go beyond the already read leap again
					}
				}
				else {
					// List had been exhausted
					leap = null;
					iter.add(leap1);	// insert the new leap at end
				}
			}
			// Now wipe all leaps eclipsed exceeded by the diagram
			while (leap != null && leap.x <= left+width) {
				if (leap.y <= bottom) {
					if (nextLeap.y <= bottom) {
						nextLeap.y = leap.y;
						iter.remove();
					}
					else {
						nextLeap.y = leap.y;
						leap.y = bottom;
					}
				}
				if (iter.hasNext()) {
					leap = iter.next();
				}
				else {
					leap = null;
				}
			}
			// If there no further leap or the next leap is far, insert the prepared end leap
			if (leap == null || (leap.x - (left+width) > DEFAULT_GAP)) {
				if (leap != null && iter.hasPrevious()) {
					iter.previous();
				}
				iter.add(nextLeap);    			
			}
		}
		// DEBUG: Disable this list printing after debugging
//		iter = _silhouette.listIterator();
//		System.out.println("Current silhouette:");
//		while (iter.hasNext()) {
//			Point leap1 = iter.next();
//			System.out.println(leap1.x + " --> " + leap1.y);
//		}
	}

	private Rect adaptLayout()
	{
		Rect rect = getDrawingRect(null);
		// START KGU#85 2017-10-23: Enh. #35 - without this superfluous group layout it's all pretty simple
		// Didn't find anything else to effectively inform the scrollbars about current extension
//		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
//		this.setLayout(layout);
//		layout.setHorizontalGroup(
//			layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//				// START KGU#411 2017-05-26: With huge diagrams the bounding box could exceed the Short value range
//				//.add(0, rect.right, Short.MAX_VALUE)
//				.add(0, Math.min(rect.right, Short.MAX_VALUE), Short.MAX_VALUE)
//				// END KGU#411 2017-05-26
//				);
//		layout.setVerticalGroup(
//			layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//				// START KGU#411 2017-05-26: With huge diagrams the bounding box could exceed the Short value range
//				//.add(0, rect.bottom, Short.MAX_VALUE)
//				.add(0, Math.min(rect.bottom, Short.MAX_VALUE), Short.MAX_VALUE)
//				// END KGU#411 2017-05-26
//				);
		//System.out.println(rect);
		Dimension oldDim = this.getPreferredSize();
		// START KGU#524 2018-06-18: Bugfix #544 for #512 (forgotten zoom consideration)
//		if (rect.right != oldDim.width || rect.bottom != oldDim.height) {
//			this.setPreferredSize(new Dimension(rect.right, rect.bottom));
//			this.revalidate();
//		}
		Dimension newZoomedDim = new Dimension(Math.round(Math.min(rect.right, Short.MAX_VALUE) / this.zoomFactor),
				Math.round(Math.min(rect.bottom, Short.MAX_VALUE) / this.zoomFactor));
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
			scroll.getHorizontalScrollBar().setUnitIncrement(unitsHorizontal);
			scroll.getVerticalScrollBar().setUnitIncrement(unitsVertical);
		}
	}
	// END KGU#444 2017-10-23

	/**
	 * Places the passed-in diagram {@code root} in the drawing area if it hadn't already been
	 * residing here. If a {@link Mainform} {@code form} was given, then it is registered with
	 * the {@code root} (unless there is already another {@link Mainform} associated) and
	 * {@code root} will automatically be pinned.
	 * @param root - a diagram to be placed here
	 */
	public void addDiagram(Root root)
	// START KGU#2 2015-11-19: Needed a possibility to register a related Mainform
	{
		// START KGU#624 2018-12-23: Enh. #655
		// Only the new diagram shall be selected afterwards
		unselectAll();
		// END KGU#624 2018-12-23
		addDiagram(root, null, null);
	}
	/**
	 * Places the passed-in diagram {@code root} in the drawing area if it hadn't already
	 * been residing here. If a {@link Mainform} {@code form} was given, then it is registered
	 * with the {@code root} (unless there is already another {@link Mainform} associated) and
	 * {@code root} will automatically be pinned.
	 * @param root - a diagram to be placed here
	 * @param form - the sender of the diagram if it was pushed here from a Structorizer instance
	 */
	public void addDiagram(Root root, Mainform form)
	// START KGU#110 2015-12-20: Enhancement #62 -we want to be able to use predefined positions
	{
		// START KGU#624 2018-12-23: Enh. #655
		// Only the new diagram shall be selected afterwards
		unselectAll();
		// END KGU#624 2018-12-23
		this.addDiagram(root, form, null);
	}

	/**
	 * @param root - the {@link Root} element of the diagram to be added
	 * @param position - the proposed position
	 */
	public void addDiagram(Root root, Point position)
	// START KGU#110 2015-12-20: Enhancement #62 - we want to be able to use predefined positions
	{
		this.addDiagram(root, null, position);
	}    

	/**
	 * Places the passed-in diagram {@code root} in the drawing area if it hadn't already been
	 * residing here. If a {@link Mainform} {@code form} was given, then it is registered with
	 * the {@code root} (unless there is already another {@link Mainform} associated) and
	 * {@code root} will automatically be pinned.
	 * If {@code point} is given then the diagram will be placed to that position, otherwise a free
	 * area is looked for.
	 * @param root - the {@link Root} element of the diagram to be added
	 * @param form - the sender of the diagram if it was pushed here from a Structorizer instance
	 * @param point - the proposed position
	 */
	public void addDiagram(Root root, Mainform form, Point point)
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
			// START KGU#499 2018-02-22: New packing strategy (silhouette approach)
			//Rect rect = getDrawingRect();
			LinkedList<Point> silhouette = new LinkedList<Point>();
			Rect rect = getDrawingRect(silhouette);
			// END KGU#499 2018-02-22

			int top = DEFAULT_GAP;
			int left = DEFAULT_GAP;

			top  = Math.max(rect.top, top);
			left = Math.max(rect.right + DEFAULT_GAP, left);

			// START KGU#497 2018-02-17: Enh. #512 - zooming must be considered
			//if (left > this.getWidth())
			if (left > this.getWidth() * this.zoomFactor)
				// END KGU#497 2018-02-17
			{
				// FIXME (KGU 2015-11-19) This isn't really sensible - might find a free space by means of a quadtree?
				top = rect.bottom + DEFAULT_GAP;
				left = rect.left;
			}
			// START KGU#110 2015-12-20
			//Point point = new Point(left,top);
			boolean pointGiven = point != null;
			if (!pointGiven)
			{
				point = new Point(left,top);
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
			// START KGU 2015-11-30
			// START KGU#136 2016-03-01: Bugfix #97 - here we need the actual position
			//Rectangle rec = root.getRect().getRectangle();
			//rec.setLocation(left, top);
			Rect rec = root.getRect(point);
			// END KGU#136 2016-03-01
			if (rec.right == rec.left) rec.right += DEFAULT_WIDTH;
			if (rec.bottom == rec.top) rec.bottom += DEFAULT_HEIGHT;
			// START KGU#499 2018-02-21: Enh. #515 - better area management
			// Improve the placement if possible (for more compact arrangement)
			if (!pointGiven) {
				point = findPreferredLocation(silhouette, rec.getRectangle());
				diagram.point = point;
				rec = root.getRect(point);
			}
			// END KGU#499 2018-02-21
			// START KGU#85 2015-11-18
			adaptLayout();
			// END KGU#85 2015-11-18
			// START KGU#497 2018-12-23: Bugfix for enh. #512
			rec = rec.scale(1/this.zoomFactor);
			// END KGU#497 2018-12-23
			this.scrollRectToVisible(rec.getRectangle());
			// START KGU#88 2015-12-20: It ought to be pinned if form wasn't null
			if (form != null)
			{
				diagram.isPinned = true;
			}
			// END KGU#88 2015-12-20
			// START KGU#278 2016-10-11: Enh. #267
			notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
			// END KGU#278 2016-10-11
			// END KGU 2015-11-30
			// START KGU#624 2018-12-21: Enh. #655 Simply add the new diagram to the selection
			//// START KGU 2016-12-12: First unselect the selected diagram (if any)
			//if (mouseSelected != null && mouseSelected.root != null)
			//{
			//	mouseSelected.root.setSelected(false);
			//}
			//mouseSelected = diagram;
			diagramsSelected.add(diagram);
			diagram.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
			// END KGU 2016-12-12
			// START KGU#624 2018-12-21: Enh. #655
			notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
			// END KGU#624 2018-12-21
			repaint();
			//getDrawingRect();	// Desperate but ineffective approach to force scroll area update
			// START KGU#2 2015-11-19
		}
		// START KGU#119 2016-01-02: Bugfix #78 - if a position is given then move the found diagram
		else if (point != null)
		{
			diagram.point = point;
			// START KGU 2016-12-12: First unselect the selected diagram (if any)
			// START KGU#624 2018-12-21: Enh. #655 Multiple selection - just add the diagram
			//if (mouseSelected != null && mouseSelected.root != null)
			//{
			//	mouseSelected.root.setSelected(false);
			//}
			//mouseSelected = diagram;
			diagramsSelected.add(diagram);
			diagram.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
			// START KGU#624 2018-12-21: Enh. #655
			notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
			// END KGU#624 2018-12-21
			// END KGU 2016-12-12
			repaint();
			//getDrawingRect();	// Desperate but ineffective approach to force scroll area update
		}
		// END KGU#119 2016-01-02
		if (form != null)
		{
			// START KGU#125 2016-01-07: We allow adoption but only for orphaned diagrams
			//diagram.mainform = form;
			if (diagram.mainform == null)
			{
				diagram.mainform = form;
				// START KGU#305 2016-12-12: Enh.#305 / 2016-12-16 no longer neeeded
				//form.updateArrangerIndex();
				// END KGU#305 2016-12-12
			}
			// END KGU#125 2016-01-07
			root.addUpdater(this);
		}
		// END KGU#2 2015-11-19
	}

	// START KGU#499 2018-02-21: Enh. #515 - More intelligent area management
	/**
	 * Scans the given silhouette line given by the {@link Point} list {@code silhouette}
	 * for the uppermost breach wide enough to accomodate a diagram of width {@code rec.width}. 
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
			if (leap.x > this.getWidth() * this.zoomFactor) {
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
			// Here a better entry might start
			if (optimum == null || leap.y < optimum.y) {
				candidates.add(new Point(leap));
			}
		}
		// If we didn't find anything better, then we will just adhere to the bounds approach result
		// But first have a look whether some incompletely analysed breaches (those remaining open at
		// end) are wide enough to be accepted. We will allow a diagram if fits at least by half.
		float windowWidth = this.getWidth() * this.zoomFactor - rec.width/2; 
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
	public void removeDiagram(Root _root) {
		// START KGU#312 2016-12-29: Enh. #315: More meticulous equality check
		//Diagram diagr = findDiagram(_root, true);
		Diagram diagr = findDiagram(_root, 1);	// Test for object identity
		// END KGU#312 2016-12-29
		if (diagr != null) {
			removeDiagram(diagr);
		}
	}

	private void removeDiagram(Diagram diagr)
	{
		boolean ask = true;
		Mainform form = diagr.mainform;
		// START KGU#194 2016-05-09: Bugfix #185 - on importing unsaved roots may linger here
		if (diagr.root.hasChanged())
		{
			if (form == null || form.getRoot() != diagr.root)
			{
				// Create a temporary Mainform
				form = new Mainform(false);
				form.setRoot(diagr.root);
				// Let the user decide to save it or not, or to cancel this removal 
				boolean goOn = form.diagram.saveNSD(true);
				// Destroy the temporary Mainform
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
		diagrams.remove(diagr);
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
					xmlgen.generateCode(this.diagramsSelected.iterator().next().root,"\t")
					);
			// END KGU#624 2018-12-21
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(toClip, this);					
		}
		return okay;
	}

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
			this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED | IRoutinePoolListener.RPC_SELECTION_CHANGED);
			// END KGU#318 2017-01-05
		}
		else
		{
			JOptionPane.showMessageDialog(frame, msgCoverageError.getText(),
					"Error", JOptionPane.ERROR_MESSAGE, null);   		
		}
	}

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
				}
			}
		}
		return canDo;
		// END KGU#642 2018-12-21
	}
	// END KGU#117 2016-03-09

	/** Creates new form Surface */
	public Surface()
	{
		initComponents();
		create();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	//@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

//		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
//		this.setLayout(layout);
//		layout.setHorizontalGroup(
//			layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//				.add(0, 400, Short.MAX_VALUE)
//			);
//		layout.setVerticalGroup(
//			layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//				.add(0, 300, Short.MAX_VALUE)
//		);
		this.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		this.setPreferredSize(new Dimension(400, 300));
		// START KGU#497 2018-02-17: Enh. #512
		try {
			this.zoomFactor = Float.parseFloat(Ini.getInstance().getProperty("arrangerZoom", "2.0f"));
		}
		catch (NumberFormatException ex) {
			logger.log(Level.WARNING, "Corrupt zoom factor in ini", ex);
		}
		// END KGU#497 2018-02-17
	}// </editor-fold>//GEN-END:initComponents

	/**
	 * @return the vector of {@link Diagram}s
	 */
	@Deprecated
	protected Vector<Diagram> getDiagrams()
	{
		return diagrams;
	}

	/**
	 * @param diagrams - the vector of {@link Diagram}s to set
	 */
	@Deprecated
	protected void setDiagrams(Vector<Diagram> diagrams)
	{
		this.diagrams = diagrams;
	}
	
	// START KGU#624 2018-12-25: Enh. #655
	/**
	 * @return the number of currently held {@link Diagram}s
	 */
	public int getDiagramCount()
	{
		return this.diagrams.size();
	}
	// END KGU#624 2018-12-25

	// START KGU#49 2015-10-18: When the window is going to be closed we have to give the diagrams a chance to store their stuff
	// FIXME (KGU): Quick-and-dirty version. More convenient should be a list view with all unsaved diagrams for checkbox selection
	/**
	 * Loops over all administered diagrams and has their respective Mainform (if still alive) saved them in case they are dirty
	 * @return Whether saving is complete (or confirmed though being incomplete) 
	 */
	// START KGU#177 2016-04-14: Enh. #158 - report all diagram (file) names without saving possibility
	//public void saveDiagrams()
	public boolean saveDiagrams()
	{
		return saveDiagrams(false, false);
	}

	/**
	 * Loops over all administered dirty diagrams and has their respective Mainform (if still alive)
	 * saved them. Otherwise uses a temporary Mainform.
	 * @param goingToClose - whether the application is going to close
	 * @param dontAsk - if questions are to be suppressed
	 * @return
	 */
	public boolean saveDiagrams(boolean goingToClose, boolean dontAsk)
	// END KGU#177 2016-04-14
	{
		// START KGU#177 2016-04-14: Enh. #158 - a pasted diagram may not have been saved, so warn
		boolean allDone = true;
		StringList unsaved = new StringList();
		// END KGU#177 2016-04-14
		if (this.diagrams != null)
		{
			// START KGU#320 2017-01-04: Bugfix #321
			HashSet<Root> handledRoots = new HashSet<Root>();
			HashSet<Mainform> mainforms = new HashSet<Mainform>();
			// END KGU#320 2017-01-04
			// START KGU#534 2018-06-27: Enh. #552
			lu.fisch.structorizer.gui.Diagram.startSerialMode();
			try {
				// END KGU#534 2018-06-27
				Iterator<Diagram> iter = this.diagrams.iterator();
				while (iter.hasNext())
				{
					Diagram diagram = iter.next();
					Mainform form = diagram.mainform;
					if (form != null)
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
						mainforms.add(form);
						handledRoots.add(diagram.root);
						// END KGU#320 2017-01-04
					}
					// START KGU#177 2016-04-14: Enh. #158 - a pasted diagram may not have been saved, so warn
					else if (diagram.root.filename == null || diagram.root.filename.isEmpty())
					{
						unsaved.add("( " + diagram.root.proposeFileName() + " ?)");
						allDone = false;
					}
					else if (diagram.root.hasChanged())
					{
						unsaved.add(diagram.root.filename);
						allDone = false;
					}
					// END KGU#177 2016-04-14
				}
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
				lu.fisch.structorizer.gui.Diagram.endSerialMode();
			}
			// END KGU#534 2018-06-29
		}
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

	// START KGU#534 2018-06-27: Enh. #552 - new opportunity to clear the entire Arranger
	/**
	 * Removes all diagrams from the Arranger surface after having asked for changes to
	 * be saved.
	 * @return whether this action was complete.
	 */
	public boolean removeAllDiagrams()
	{
		boolean allDone = false;
		if (saveDiagrams()) {
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
			}
			finally {
				adaptLayout();
				repaint();
				notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED | IRoutinePoolListener.RPC_SELECTION_CHANGED);
			}
			allDone = true;
		}
		return allDone;
	}
	// END KGU#534 2018-06-27

	public void mouseClicked(MouseEvent e)
	{
		Diagram diagr1 = null;
		if (this.diagramsSelected.size() == 1) {
			diagr1 = this.diagramsSelected.iterator().next();
		}
		if (e.getClickCount()==2)
		{
			if (diagr1 == null) {
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
			int mouseX = Math.round(e.getX() * this.zoomFactor);
			int mouseY = Math.round(e.getY() * this.zoomFactor);
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
			this.notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
			repaint();			
		}
	}

	public void mousePressed(MouseEvent e)
	{
		/* The interesting things happen in mouseClicked() and mouseDragged() */
		// START KGU#626 2018-12-23: Enh. #657
		showPopupMenu(e);
		// END KGU#626 2018-12-23
	}

	public void mouseReleased(MouseEvent e)
	{
		// We must reset the last mousePoint lest mouseDragged() runs havoc
		mousePoint = null;
		if (dragArea != null) {
			// TODO Select all contained diagrams
			Set<Diagram> foundDiagrams = this.getContainedDiagrams(dragArea);
			if (! e.isShiftDown()) {
				this.unselectAll();
			}
			this.selectSet(foundDiagrams);
		}
		dragArea = null;
		// START KGU#626 2018-12-23: Enh. #657
		showPopupMenu(e);
		// END KGU#626 2018-12-23
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mouseDragged(MouseEvent e)
	{
		int mouseX = e.getX();
		int mouseY = e.getY();
		if (mouseX > 0 && mouseY > 0)	// Don't let drag beyond the scrollable area
		{
			// Make sure the viewport moves with the mouse
			Rectangle rect = new Rectangle(mouseX, mouseY, 1, 1);
			scrollRectToVisible(rect);

			// Now we need the virtual coordinates
			mouseX = Math.round(mouseX * zoomFactor);
			mouseY = Math.round(mouseY * zoomFactor);
			
			Diagram diagram = getHitDiagram(mouseX, mouseY);
			Point oldMousePoint = this.mousePoint;
			
			// Don't drag diagrams if the mouse isn't above one of the selected diagrams
			if (dragArea == null && (oldMousePoint != null || diagram != null && (this.diagramsSelected.isEmpty() || this.diagramsSelected.contains(diagram)))) {
				this.mousePoint = new Point(mouseX, mouseY);
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
			else if (this.mousePoint == null) {
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
			diagr.point.setLocation(newX, newY);
		}
		adaptLayout();
		repaint();
	}

	public void mouseMoved(MouseEvent e)
	{
	}
	
	// START KGU#626 2018-12-23: Prepared for enh. #657
	private void showPopupMenu(MouseEvent e) 
	{
		if (e.isPopupTrigger()) 
		{
			Diagram diagr = this.getHitDiagram(Math.round(e.getX() * zoomFactor), Math.round(e.getY() * zoomFactor));
			// START KGU#318 2017-01-05: Enh. #319
			//popup.show(e.getComponent(), e.getX(), e.getY());
			if (diagr != null) {
				// TODO
				//popupMenu.show(e.getComponent(), e.getX(), e.getY());					
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
	 */
	private List<Diagram> getHitDiagrams(int trueX, int trueY)
	{
		List<Diagram> hitDiagrams = new LinkedList<Diagram>();
		for (int d = diagrams.size()-1; d >= 0; d--)
		{
			Diagram diagram = diagrams.get(d);
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
		this.diagramsSelected.clear();
		for (Diagram diagr: this.diagrams) {
			if (diagr.root != null) {
				diagr.root.setSelected(false, Element.DrawingContext.DC_ARRANGER);
			}
		}
		repaint();
	}
	
	/**
	 * Selects all available diagrams and repaints
	 */
	public void selectAll() {
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
	 * @param diagrSet - set of diagram to be added to the selection
	 */
	public void selectSet(Set<Diagram> diagrSet) {
		this.diagramsSelected.addAll(diagrSet);
		for (Diagram diagr: diagrSet) {
			if (diagr.root != null) {
				diagr.root.setSelected(true, Element.DrawingContext.DC_ARRANGER);
			}
		}
		repaint();
	}
	// END KGU#624 2018-12-21

	/**
	 * Repaints the given {@link Root} (which reported to have been subject to changes).
	 * Also notifies all registered {@link IRoutinePoolListener}s.
	 * @param source - the structogram that reported to have been modified
	 */
	public void update(Root source)
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
				String newRootName = source.getMethodName();
				if (!oldRootName.equals(newRootName)) {
					removeFromNameMap(oldRootName, diagr);
					addToNameMap(newRootName, diagr);
				}
				this.notifyChangeListeners(IRoutinePoolListener.RPC_POOL_CHANGED);
			}
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
	 * @param root - the Nassi-Shneiderman diagram we look for
	 * @param equalityLevel - the level of resemblance - see {@link Root#compareTo(Root)}
	 * @return the first {@link Diagram} that holds a matching {@link Root}.
	 * @see #findDiagram(Root, int, boolean)
	 * @see #findDiagramsByName(String)
	 * @see #findIncludesByName(String)
	 */
	private Diagram findDiagram(Root root, int equalityLevel)
	{
		return findDiagram(root, equalityLevel, false);
	}
	/**
	 * Search among the held {@link Diagram} objects for one the associated {@link Root} of which
	 * closely enough resembles the given {@link Root} {@code root}.
	 * @param root - the Nassi-Shneiderman diagram we look for
	 * @param equalityLevel - the level of resemblance - see {@link Root#compareTo(Root)}
	 * @param warnLevel2AndAbove - if true then a warning will be raised if the obtained equality level is not 1 
	 * @return the first {@link Diagram} that holds a matching {@link Root}.
	 * @see #findDiagram(Root, int)
	 * @see #findDiagramsByName(String)
	 * @see #findIncludesByName(String)
	 */
	private Diagram findDiagram(Root root, int equalityLevel, boolean warnLevel2andAbove)
	// END KGU#312 2016-12-29
	// END KGU#119 2016-01-02
	{
		Diagram owner = null;
		Vector<Diagram> diagramList = this.diagrams;
		// START KGU#624 2018-12-26: Enh. #655 - Attempt to make search faster
		if (equalityLevel == 1) {
			return this.rootMap.get(root);
		}
		else if (equalityLevel == 2 || equalityLevel == 5) {
			// We try to reduce the number of diagrams to be searched  
			diagramList = this.nameMap.get(root.getMethodName());
		}
		// END KGU#624 2018-12-26
		if (diagramList != null) {
			for(int d = 0; owner == null && d < diagramList.size(); d++)
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
						String message = msgInsertionConflict[resemblance-3].getText().
								replace("%1", root.getSignatureString(false)).
								replace("%2", fName);
						JOptionPane.showMessageDialog(this.getParent(), message,
								this.titleDiagramConflict.getText(),
								JOptionPane.WARNING_MESSAGE);			
					}
					if (resemblance <= equalityLevel)
					{
						owner = diagram;	// Will leave the loop
					}
				}
				// END KGU#312 2016-12-29
			}
		}
		return owner;
	}
	// END KGU#2 2015-11-19

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
			// START KGU#85 2015-11-18
			adaptLayout();
			// END KGU#85 2015-11-18
			this.repaint();
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
	public Vector<Root> findIncludesByName(String rootName)
	{
		Vector<Root> incls = new Vector<Root>();
		for (Root root: this.findDiagramsByName(rootName)) {
			if (root.isInclude()) {
				incls.add(root);
			}
		}
		return incls;
	}
	// END KGU#376 2017-04-11

	// START KGU#2 2015-11-24
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.executor.IRoutinePool#findRoutinesBySignature(java.lang.String, int)
	 */
	@Override
	public Vector<Root> findRoutinesBySignature(String rootName, int argCount)
	{
		Vector<Root> functionsAny = findDiagramsByName(rootName);
		Vector<Root> functions = new Vector<Root>();
		for (int i = 0; i < functionsAny.size(); i++)
		{
			Root root = functionsAny.get(i);
			if (root.isSubroutine() && root.getParameterNames().count() == argCount)
			{
				functions.add(root);
			}
		}
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
	 */
	protected StringList listSelectedRoots(boolean allIfEmpty, boolean withPath) {
		Vector<Root> selectedRoots = new Vector<Root>(this.diagramsSelected.size());
		for (Diagram diagr: this.diagramsSelected) {
			if (diagr.root != null) {
				selectedRoots.add(diagr.root);
			}
		}
		if (selectedRoots.isEmpty() && allIfEmpty) {
			selectedRoots.addAll(this.getAllRoots());
		}
		Collections.sort(selectedRoots, Root.SIGNATURE_ORDER);
		StringList signatures = new StringList();
		for (Root root: selectedRoots) {
			signatures.add(root.getSignatureString(withPath));
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

	// Windows listener for the mainform
	// I need this to unregister the updater
	public void windowOpened(WindowEvent e)
	{
	}

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

	public void windowClosed(WindowEvent e)
	{
		// START KGU#305 2016-12-16
		if (e.getSource() instanceof Mainform) { 
			removeChangeListener((Mainform)e.getSource());
		}
		// END KGU#305 2016-12-16
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub

	}

	// START KGU#305 2016-12-12: Enh. #305
	/**
	 * Scrolls to the given Root if found and selects it. If setAtTop is true then the diagram
	 * will be raised to the top drawing level.
	 * @param aRoot - the diagram to be focused
	 * @param setAtTop - whether the diagram is to be drawn on top of all
	 */
	public void scrollToDiagram(Root aRoot, boolean setAtTop) {
		// FIXME: Apply zoomFactor!
		// START KGU#312 2016-12-29: Enh. #315 - adaptation to modified signature 
		//Diagram diagr = this.findDiagram(aRoot, true);
		Diagram diagr = this.findDiagram(aRoot, 1);	// Check for identity here
		// ED KGU#312 2016-12-29
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
			rect = rect.scale(1/this.zoomFactor);
			// END KGU#497 2018-02-17
			this.scrollRectToVisible(rect.getRectangle());
		}
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
			rect = rect.scale(1/zoomFactor);
		}
		if (rect.right > rect.left && rect.bottom > rect.top) {
			bounds = rect.getRectangle();
		}
		return bounds;
	}
	// END KGU#624 2018-12-24
	
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
	Set<Root> getSelected() {
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
		LinkedList<Root> rootQueue = new LinkedList<Root>(selectedRoots);
		Set<Diagram> addedDiagrams = new HashSet<Diagram>();
		while (!rootQueue.isEmpty()) {
			Root root = rootQueue.removeFirst();
			// First look for called routines
			Vector<Call> calls = root.collectCalls();
			for (Call call: calls) {
				if (call.isDisabled()) {
					continue;
				}
				Function fct = call.getCalledRoutine();
				if (fct != null) {
					// TODO in future: compare group membership
					Vector<Root> candidates = this.findRoutinesBySignature(fct.getName(), fct.paramCount());
					// For now we will add all of them to be on the safe side
					for (Root sub: candidates) {
						if (selectedRoots.add(sub)) {
							rootQueue.addLast(sub);
							addedDiagrams.add(rootMap.get(sub));	// Should of course be in the map
							sub.setSelected(true, Element.DrawingContext.DC_ARRANGER);
						}
					}
					if (candidates.isEmpty()) {
						missingSignatures.add(fct.getSignatureString());
					}
					else if (candidates.size() > 1) {
						duplicateSignatures.add(fct.getSignatureString());
					}
				}
			}
			// Then look for referenced includables
			if (root.includeList != null) {
				for (int i = 0; i < root.includeList.count(); i++) {
					String inclName = root.includeList.get(i);
					Vector<Root> candidates = this.findIncludesByName(inclName);
					// For now we will add all of them to be on the safe side
					for (Root incl: candidates) {
						if (selectedRoots.add(incl)) {
							rootQueue.addLast(incl);
							addedDiagrams.add(rootMap.get(incl));	// Should of course be in the map
							incl.setSelected(true, Element.DrawingContext.DC_ARRANGER);
						}
					}
					if (candidates.isEmpty()) {
						missingSignatures.add(inclName);
					}
					else if (candidates.size() > 1) {
						duplicateSignatures.add(inclName);
					}
				}
			}
		}
		diagramsSelected.addAll(addedDiagrams);
		notifyChangeListeners(IRoutinePoolListener.RPC_SELECTION_CHANGED);
		repaint();
		return addedDiagrams.size();
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
		for (IRoutinePoolListener listener: listeners) {
			// START KGU#624 2018-12-21: Enh. #655
			//listener.routinePoolChanged(this);
			listener.routinePoolChanged(this, _flags);
			// END KGU#624 2018-12-21
		}
	}
	// END KGU#305 2016-12-16

	// START KGU#497 2018-02-17: Enh. #512
	/**
	 * Raises or reduces the current zoom factor according to the argument.
	 * @param zoomIn - if true then z´the zoom factor is reduced otherwise it is raised
	 */
	public void zoom(boolean zoomIn) {
		if (zoomIn) {
			this.zoomFactor = Math.max(this.zoomFactor * 0.9f, 1);
		}
		else {
			this.zoomFactor /= 0.9f;
		}
		repaint();
		// START KGU#624 2018-12-21: Enh. #655
		this.dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
		// END KGU#624 2018-12-21
	}

	/**
	 * Return the current zoom factor
	 * @return zoom factor (1 = 100%, 2 = 50%, 3 = 33% etc.)
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
			if (Element.E_WHEEL_REVERSE_ZOOM) {
				rotation *= -1;
			}
			if (rotation >= 1) {
				mwEvt.consume();
				this.zoom(false);
			}
			else if (rotation <= -1) {
				mwEvt.consume();
				this.zoom(true);
			}
		}
	}
	// END KGU#503 2018-03-13
	
	// START KGU#624 2018-12-26: Enh. #655
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

}

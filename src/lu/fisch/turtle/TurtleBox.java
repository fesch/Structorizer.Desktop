/*
    Turtlebox

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

package lu.fisch.turtle;

/******************************************************************************************************
 *
 *      Author:         Robert Fisch
 *
 *      Description:    TurtleBox - a Turtle graphics window with an interface usable e.g. by Structorizer
 *                      but also (with an appropriate adapter) by arbitrary Java applications
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2015-12-10      Inheritance change for enhancement request #48
 *      Kay Gürtzig     2016-10-16      Enh. #272: exact internal position (double coordinates)
 *      Kay Gürtzig     2016-12-02      Enh. #302 Additional methods to set the pen and background color
 *      Kay Gürtzig     2017-06-29/30   Enh. #424: Inheritance extension to FunctionProvidingDiagramControl
 *                                      function map introduced, functions getX(), getY(), getOrientation() added
 *      Kay Gürtzig     2017-10-28      Enh. #443: interface FunctionProvidingDiagramControl now integrated,
 *                                      structure of function map modified, procedure map added, execution
 *                                      mechanism fundamentally revised
 *                                      Concurrency issue fixed (KGU#449).
 *      Kay Gürtzig     2018-01-16      Enh. #490: Class decomposed to allow a mere API use without realising the GUI
 *      Kay Gürtzig     2018-07-30      Enh. #576: New procedure clear() added to the API
 *      Kay Gürtzig     2018-10-12      Issue #622: Modification apparently helping to overcome drawing contention
 *      Kay Gürtzig     2019-03-02      Issue #366: New methods isFocused() and requestFocus() in analogy to Window
 *      Kay Gürtzig     2020-12-11      Enh. #704: Scrollbars, status bar, and popup menu added
 *                                      Enh. #443: Deprecated execute methods disabled
 *      Kay Gürtzig     2020-12-16      Enh. #704/#880: Zoom and export functions accomplished
 *      Kay Gürtzig     2020-12-17/20   Enh. #890 Seven improvements to the GUI implemented
 *      Kay Gürtzig     2020-12-21      Enh. #893 (measuring), bugfix #894 (correct picture displacement)
 *      Kay Gürtzig     2020-12-22      Enh. #890: Snapping for measure line; CR KGU#895: no Move objects
 *                                      created anymore, 2nd (higher-resolution) turtle image
 *      Kay Gürtzig     2020-12-23      Bugfix #897: Numerical and endless loop risk on rl()/getOrientation()
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      To start the Turtleizer in an application, the following steps are recommended:
 *      	{@code TurtleBox turtleBox = new TurtleBox(<width>, <height>);}
 *			{@code turtleBox.setVisible(true);}
 *			{@code turtleBox.setAnimationDelay(0, true);}
 *      The API for employing applications is retrievable via {@link TurtleBox#getFunctionMap()} and
 *      {@link TurtleBox#getProcedureMap}.
 *
 ******************************************************************************************************///

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
//import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import lu.fisch.diagrcontrol.*;
import lu.fisch.turtle.io.CSVFilter;
import lu.fisch.turtle.io.ExtFileFilter;
import lu.fisch.turtle.io.PNGFilter;
import lu.fisch.turtle.io.SVGFilter;
import lu.fisch.turtle.elements.Element;
import lu.fisch.turtle.elements.Line;
import lu.fisch.turtle.elements.Move;

/**
 * TurtleBox - a Turtle controller providing an interface usable e.g. by Structorizer
 * but also (with an appropriate adapter) by arbitrary Java applications<br/>.
 * On parameterized initialization or call of any of the offered routines a graphics
 * window with a Turtle is realized.
 * To start the Turtleizer in an application, the following steps are recommended:<br/>
 * {@code TurtleBox turtleBox = new TurtleBox(<width>, <height>);}<br/>
 * {@code turtleBox.setVisible(true);}<br/>
 * {@code turtleBox.setAnimationDelay(0, true);}<br/>
 * The API for employing applications is retrievable via {@link TurtleBox#getFunctionMap()}
 * and {@link TurtleBox#getProcedureMap()}.<br/>
 * In order just to retrieve the available API without bringing up the GUI a light-weight
 * instance is obtained by
 * {@code TurtleBox turtleBox = new TurtleBox();}
 * @author robertfisch
 * @author Kay Gürtzig
 */
// START KGU#97 2015-12-10: Inheritance change for enhancement request #48
//public class TurtleBox extends JFrame implements DiagramController
@SuppressWarnings("serial")
// START KGU#480 2018-01-16: Inheritance change for enh. #490
//public class TurtleBox extends JFrame implements DelayableDiagramController
public class TurtleBox implements DelayableDiagramController
// END KGU#480 2018-01-16
// END KGU#97 2015-12-10
{
//	// START KGU#417 2017-06-29: Enh. #424 Function capability map
//	private static final HashMap<String, Class<?>[]> definedFunctions = new HashMap<String, Class<?>[]>();
//	static {
//		definedFunctions.put("getx", new Class<?>[]{Double.class});
//		definedFunctions.put("gety", new Class<?>[]{Double.class});
//		definedFunctions.put("getorientation", new Class<?>[]{Double.class});
//	}
//	// END KGU#417 2017-06-29
	// START KGU#597 2018-10-12: Issue #622 Analysis of drawing contention on some Macbook
	//public static final Logger logger = Logger.getLogger(TurtleBox.class.getName());
	// END KGU#597 2018-10-12
	// START KGU#417/KGU#448 2017-10-28: Enh. #424, #443 Function capability map
	private static final HashMap<String, Method> definedProcedures = new HashMap<String, Method>();
	static {
		try {
			definedProcedures.put("forward#1", TurtleBox.class.getMethod("forward", new Class<?>[]{Double.class}));
			definedProcedures.put("forward#2", TurtleBox.class.getMethod("forward", new Class<?>[]{Double.class, Color.class}));
			definedProcedures.put("backward#1", TurtleBox.class.getMethod("backward", new Class<?>[]{Double.class}));
			definedProcedures.put("backward#2", TurtleBox.class.getMethod("backward", new Class<?>[]{Double.class, Color.class}));
			definedProcedures.put("fd#1", TurtleBox.class.getMethod("fd", new Class<?>[]{Integer.class}));
			definedProcedures.put("fd#2", TurtleBox.class.getMethod("fd", new Class<?>[]{Integer.class, Color.class}));
			definedProcedures.put("bk#1", TurtleBox.class.getMethod("bk", new Class<?>[]{Integer.class}));
			definedProcedures.put("bk#2", TurtleBox.class.getMethod("bk", new Class<?>[]{Integer.class, Color.class}));
			definedProcedures.put("left#1", TurtleBox.class.getMethod("left", new Class<?>[]{Double.class}));
			definedProcedures.put("right#1", TurtleBox.class.getMethod("right", new Class<?>[]{Double.class}));
			definedProcedures.put("rl#1", TurtleBox.class.getMethod("left", new Class<?>[]{Double.class}));
			definedProcedures.put("rr#1", TurtleBox.class.getMethod("right", new Class<?>[]{Double.class}));
			definedProcedures.put("gotoxy#2", TurtleBox.class.getMethod("gotoXY", new Class<?>[]{Integer.class, Integer.class}));
			definedProcedures.put("gotox#1", TurtleBox.class.getMethod("gotoX", new Class<?>[]{Integer.class}));
			definedProcedures.put("gotoy#1", TurtleBox.class.getMethod("gotoY", new Class<?>[]{Integer.class}));
			definedProcedures.put("penup#0", TurtleBox.class.getMethod("penUp", (Class<?>[])null));
			definedProcedures.put("pendown#0", TurtleBox.class.getMethod("penDown", (Class<?>[])null));
			definedProcedures.put("up#0", TurtleBox.class.getMethod("penUp", (Class<?>[])null));
			definedProcedures.put("down#0", TurtleBox.class.getMethod("penDown", (Class<?>[])null));
			definedProcedures.put("hideturtle#0", TurtleBox.class.getMethod("hideTurtle", (Class<?>[])null));
			definedProcedures.put("showturtle#0", TurtleBox.class.getMethod("showTurtle", (Class<?>[])null));
			definedProcedures.put("setpencolor#3", TurtleBox.class.getMethod("setPenColor", new Class<?>[]{Integer.class, Integer.class, Integer.class}));
			definedProcedures.put("setbackground#3", TurtleBox.class.getMethod("setBackgroundColor", new Class<?>[]{Integer.class, Integer.class, Integer.class}));
			// START KGU#566 2018-07-30: Enh. #576
			definedProcedures.put("clear#0", TurtleBox.class.getMethod("clear", (Class<?>[])null));
			// END KGU#566 2018-07-30
		} catch (NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		}
	}
	private static final HashMap<String, Method> definedFunctions = new HashMap<String, Method>();
	static {
		try {
			definedFunctions.put("getx#0", TurtleBox.class.getMethod("getX", (Class<?>[])null));
			definedFunctions.put("gety#0", TurtleBox.class.getMethod("getY", (Class<?>[])null));
			definedFunctions.put("getorientation#0", TurtleBox.class.getMethod("getOrientation", (Class<?>[])null));
		} catch (NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		}
	}
	// END KGU#417/KGU#448 2017-10-28
	// START KGU#885 2020-12-12: Enh. #704
	//// START KGU#480 2018-01-16: Enh. #490 - frame now as attribute
	///** The GUI frame - while null, it hasn't been materialized (light-weight instance) */
	//private JFrame frame = null;
	//// END KGU#480 2018-01-16
	
	// START KGU 2020-12-23
	public static final Logger logger = Logger.getLogger(TurtleBox.class.getName());
	private static final String HELP_URL = "https://help.structorizer.fisch.lu/index.php?%";
	private static final String GUI_LINK = "menu=93&page=#turtleizer_gui";
	private static final String KEY_LINK = "menu=118&page=#keys_turtleizer";
	// END KGU 2020-12-23
	
	public static final class TurtleFrame extends JFrame implements KeyListener, WindowFocusListener
	{
		public final class TurtlePanel extends JPanel
		{
			@Override
			public void paint(Graphics graphics)
			{
				paint(graphics, false);
			}
			public void paint(Graphics graphics, boolean compensateZoom)
			{
				synchronized(zoomMutex) {
					Graphics2D g = (Graphics2D) graphics;
					// set anti-aliasing rendering
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

					// clear background
					// START KGU#303 2016-12-02: Enh. #302
					//g.setColor(Color.WHITE);
					g.setColor(owner.backgroundColor);
					// END KGU#303 2016-12-02
					// START KGU#685 2020-12-11: Enh. #704
					//g.fillRect(0, 0, this.getWidth(), this.getHeight());
					float zoom = zoomFactor;
					if (compensateZoom) {
						Dimension prefDim = getPreferredSize();
						g.fillRect(0, 0,
								Math.round(prefDim.width / zoom),
								Math.round(prefDim.height / zoom));
						zoom = 1f;
					}
					else {
						g.fillRect(0, 0, this.getWidth(), this.getHeight());
						g.scale(zoomFactor, zoomFactor);
					}
					// START KGU#893 2020-12-21: Bugfix #894
					Point offset = displacement;	// We cache it to avoid concurrency trouble
					if (offset != null) {
						g.translate(offset.x, offset.y);
					}
					// END KGU#893 2020-12-21
					// END KGU#685 2020-12-11

					// START KGU#685 2020-12-14: Enh. #704
					if (offset != null && popupShowOrigin.isSelected()) {
						// Paint the axes of coordinates
						int x1 = owner.bounds.x + owner.bounds.width;
						int y1 = owner.bounds.y + owner.bounds.height;
						Rectangle visibleRect = g.getClipBounds();
						// Make sure the lines run through the entire visible area
						if (visibleRect != null) {
							x1 = Math.max(x1, visibleRect.x + visibleRect.width);
							y1 = Math.max(y1, visibleRect.y + visibleRect.height);
						}
						g.setColor(Color.decode("0xffcccc"));
						java.awt.Stroke strk = g.getStroke();
						g.setStroke(new java.awt.BasicStroke(1f/zoom,
								java.awt.BasicStroke.CAP_ROUND,
								java.awt.BasicStroke.JOIN_ROUND, 1f,
								new float[] {2f/zoom, 2f/zoom}, 0f));
						g.drawLine(0, owner.bounds.y, 0, y1);
						g.drawLine(owner.bounds.x, 0, x1, 0);
						g.setStroke(strk);
					}
					// END KGU#685 2020-12-14
					
					// START KGU#303 2016-12-03: Enh. #302
					//g.setColor(Color.BLACK);
					g.setColor(owner.defaultPenColor);
					// END KGU#303 2016-12-03

					// draw all elements
					// START KGU#449 2017-10-28: The use of iterators may lead to lots of
					//java.util.ConcurrentModificationException errors slowing down all.
					// So we better avoid the iterator and loop against a snapshot size
					// (which is safe because the elements Vector can't shrink during execution).
					//for (Element ele : elements)
					//{
					//    ele.draw(g);
					//}
					int nElements = owner.elements.size();
					// START KGU#597 2018-10-12: Issue #622 - Monitoring drawing detention underMac
					//logger.config("Painting " + nElements + " elements...");
					// END KGU#597 2018-10-12
					for (int i = 0; i < nElements; i++) {
						owner.elements.get(i).draw(g);
					}
					// END KGU#449 2017-10-28

					// START KGU#889 2020-12-21: Enh. #890/8 measuring line
					if (dragStart != null) {
						int offsetX = 0, offsetY = 0;
						if (offset != null) {
							offsetX = offset.x;
							offsetY = offset.y;
						}
						int x1 = (int)(mouseX / zoom) - offsetX;
						int y1 = (int)(mouseY / zoom) - offsetY;
						g.setColor(Color.decode("0xccccff"));
						java.awt.Stroke strk = g.getStroke();
						g.setStroke(new java.awt.BasicStroke(1f/zoom,
								java.awt.BasicStroke.CAP_ROUND,
								java.awt.BasicStroke.JOIN_ROUND, 1f,
								new float[] {4f/zoom, 4f/zoom}, 0f));
						g.drawLine(dragStart.x, dragStart.y, x1, y1);
						g.setStroke(strk);
					}
					// END KGU#889 2020-12-21

					if (!owner.turtleHidden)
					{
						Image turtleImg = owner.image;
						int x = owner.pos.x;
						int y = owner.pos.y;
						// START KGU#889 2020-12-22: Enh. #890 Provide a higher resolution
						if (zoom > 1.25f) {
							turtleImg = owner.image2;
							if (offset != null) {
								g.translate(-offset.x, -offset.y);
							}
							// CAUTION! BE AWARE THAT zoom GETS TEMPORARILY MODIFIED HERE!
							zoom = zoom/2;
							g.scale(0.5, 0.5);
							if (offset != null) {
								g.translate(offset.x, offset.y);
							}
							x *= 2;
							y *= 2;
						}
						// END KGU#889 2020-12-22
						// apply rotation
						g.rotate((270-owner.angle)/180*Math.PI, x, y);
						// fix drawing point
						x -= turtleImg.getWidth(this)/2;
						y -= turtleImg.getHeight(this)/2;
						// draw the turtle
						g.drawImage(turtleImg, x, y, this);
						
					}
					// CAUTION: Make sure nothing gets drawn here (zoom possibly modified!)
					
					// START KGU#893 2020-12-21: Bugfix #894
					if (offset != null) {
						g.translate(-offset.x, -offset.y);
					}
					// END KGU#893 2020-12-21
					// START KGU#685 2020-12-11: Enh. #704
					if (!compensateZoom) {
						g.scale(1/zoom, 1/zoom);
					}
					// END KGU#685 2020-12-11
				}

				// START KGU#685 2020-12-16: Enh. #704
				this.updatePreferredSize(!compensateZoom);
				// END KGU#685 2020-12-16
			}
			
			// START KGU#685 2020-12-16: Enh. #704
			public void updatePreferredSize(boolean useZoom)
			{
				Rectangle bounds = new Rectangle(owner.bounds);
				// START KGU#893 2020-12-21: Bugfix #894
				if (displacement != null) {
					bounds.x += displacement.x;
					bounds.y += displacement.y;
				}
				// END KGU#893 2020-12-21
				// Make sure the bounds comprise the turtle position - visible or not
				bounds.add(new Rectangle(owner.pos.x, owner.pos.y, 1, 1));
				// We need the maximum distances from top and left border
				Dimension dim = new Dimension(
						// The drawing could lie completely in the negative range
						Math.max(bounds.x + bounds.width, 0),
						Math.max(bounds.y + bounds.height, 0)
						);
				// Add some pixels for the scrollbars
				dim.width += MARGIN;
				dim.height += MARGIN;
				if (useZoom) {
					dim.width = Math.round(Math.min(dim.width, Short.MAX_VALUE) * zoomFactor);
					dim.height = Math.round(Math.min(dim.height, Short.MAX_VALUE) * zoomFactor);
				}

				this.setPreferredSize(dim);
				this.revalidate();
			}
			// END KGU#685 2020-12-16
		}
		private TurtlePanel panel;
		// START KGU#685 2020-12-11: Enh. #704
		private JScrollPane scrollarea;
		private JPanel statusbar;
		protected JLabel statusHome;
		protected JLabel statusTurtle;
		protected JLabel statusSize;
		protected JLabel statusViewport;
		protected JLabel statusZoom;
		//protected JLabel statusSelection;
		private float zoomFactor = 1.0f;
		protected javax.swing.JPopupMenu popupMenu;
		protected javax.swing.JMenuItem popupGotoCoord;
		protected javax.swing.JMenuItem popupGotoTurtle;
		protected javax.swing.JMenuItem popupGotoHome;
		// START KGU#889 2020-12-18: Enh. #890
		protected javax.swing.JMenuItem popupGotoOrigin;
		// END KGU#889 2020-12-18
		protected javax.swing.JMenuItem popupExtendCanvas;
		protected javax.swing.JMenuItem popupZoom100;
		protected javax.swing.JMenuItem popupZoomBounds;
		protected javax.swing.JCheckBoxMenuItem popupShowOrigin;
		protected javax.swing.JCheckBoxMenuItem popupShowTurtle;
		protected javax.swing.JCheckBoxMenuItem popupShowStatus;
		protected javax.swing.JCheckBoxMenuItem popupShowCoords;
		// START KGU#889 2020-12-23: Enh. #890/8
		protected javax.swing.JCheckBoxMenuItem popupSnapLines;
		protected javax.swing.JMenuItem popupSnapRadius;
		// END KGU#889 2020-12-23
		protected javax.swing.JMenuItem popupExportCSV;
		protected javax.swing.JMenu popupExportImage;
		protected javax.swing.JMenuItem popupExportPNG;
		protected javax.swing.JMenuItem popupExportSVG;
		protected javax.swing.JMenuItem popupBackground;
		// START KGU#889 2020-12-20: Enh. #890/7
		// popup for coordinates
		private JLabel lblPop = new JLabel("", SwingConstants.CENTER);
		private JPopupMenu pop = new JPopupMenu();
		// END KGU#889
		
		// The following JLabel objects are mere text holders for localisation purposes
		protected JLabel msgOverwrite = new JLabel("File exists. Sure to overwrite?");
		protected JLabel msgBrowseFailed = new JLabel("Failed to show \"%\" in browser");
		protected JLabel msgHelp = new JLabel("Turtleizer help");
		// START KGU#889 2020-12-18: Issue #890
		protected JLabel lblScale = new JLabel("Scale factor:");
		private static final char[] SEPARATORS = new char[] {
				',', ';', '\t', ' ', ':'
		};
		private static final String[] CSV_COL_HEADERS = new String[] {
				"xFrom", "yFrom", "xTo", "yTo", "color"
		};
		protected JLabel lblSeparator = new JLabel("Separator");
		protected javax.swing.JRadioButton[] rbSeparators = new javax.swing.JRadioButton[] {
				new javax.swing.JRadioButton("Comma"),
				new javax.swing.JRadioButton("Semicolon"),
				new javax.swing.JRadioButton("Tabulator"),
				new javax.swing.JRadioButton("Blank"),
				new javax.swing.JRadioButton("Colon")
		};
		protected javax.swing.ButtonGroup bg = null;
		protected javax.swing.JColorChooser colChooser = null;
		// END KGU#889 2020-12-18
		
		private File currentDirectory = null;
		private int[] lastAskedCoords = null;	// last explicitly asked coordinates
		private int lastAskedScale = 1;	// last explicitly asked SVG scale
		/** Coordinate offset (in turtle world units), or {@code null} */
		private Point displacement = null;		// Origin displacement after moving the drawing
		private Object zoomMutex = new Object();	// Sequentialization within the EventQueue
		// START KGU#889 2020-12-20: Enh. #890/7
		private boolean showCoordinates = true;
		/** Most recent mouse coordinates (in window units) */
		private int mouseX = 0, mouseY = 0;
		// END KGU#889 2020-12-20
		// START KGU#889 2020-12-20/23: Enh. #890/8
		/** Dragging start point in turtle world coordinates, or {@code null} */
		private Point dragStart = null;
		/** Snap distance for the measuring function, in turtle world units */
		private int snapRadius = 5;
		private boolean snapLines = true;
		// END KGU#889 2020-12-20/23

		private TurtleBox owner;

		public TurtleFrame(TurtleBox owner)
		{
			super();
			this.owner = owner;
			initComponents();
		}

		private void initComponents()
		{
			panel = new TurtlePanel();

			panel.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger() && popupMenu != null) {
						pop.setVisible(false);
						popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger() && popupMenu != null) {
						pop.setVisible(false);
						popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
					pop.setVisible(false);
				}});

			scrollarea = new JScrollPane(panel);
			scrollarea.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
			scrollarea.setWheelScrollingEnabled(true);
			scrollarea.setDoubleBuffered(true);
			scrollarea.setBorder(BorderFactory.createEmptyBorder());
			scrollarea.addMouseWheelListener(new MouseWheelListener() {
				@Override
				public void mouseWheelMoved(MouseWheelEvent mwEvt) {
					if (mwEvt.isControlDown()) {
						int rotation = mwEvt.getWheelRotation();
						if (Math.abs(rotation) >= 1) {
							if (owner.reverseZoomWheel) {
								rotation *= -1;
							}
							mwEvt.consume();
							zoom(rotation < 0);
						}
					}
					else {
						pop.setVisible(false);
					}
						
				}});
			getContentPane().add(scrollarea, java.awt.BorderLayout.CENTER);
			panel.setAutoscrolls(true);
			statusbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
			//statusbar.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.LineBorder(java.awt.Color.DARK_GRAY),
			//		new javax.swing.border.EmptyBorder(0, 4, 0, 4)));
			statusHome = new JLabel();
			statusTurtle = new JLabel();
			statusSize = new JLabel();
			statusViewport = new JLabel();
			statusZoom = new JLabel();
			//statusSelection = new JLabel("0");
			statusHome.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
					javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
			statusTurtle.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
					javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
			statusSize.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
					javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
			statusViewport.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
					javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
			statusZoom.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
					javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4)));
			//statusSelection.setBorder(new javax.swing.border.CompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED),
			//		new javax.swing.border.EmptyBorder(0, 4, 0, 4)));
			statusHome.setToolTipText("Current home position");
			statusTurtle.setToolTipText("Current turtle position");
			statusSize.setToolTipText("Extent of the drawn area");
			statusViewport.setToolTipText("Current scrolling viewport");
			statusZoom.setToolTipText("Zoom factor");
			//statusSelection.setToolTipText("Number of selected segments");
			statusbar.add(statusHome);
			statusbar.add(statusTurtle);
			statusbar.add(statusSize);
			statusbar.add(statusViewport);
			statusbar.add(statusZoom);
			//statusbar.add(statusSelection);
			statusbar.setFocusable(false);
			getContentPane().add(statusbar, java.awt.BorderLayout.SOUTH);

			initPopupMenu();

			scrollarea.getViewport().addChangeListener(new javax.swing.event.ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent evt) {
					updateStatus();
				}
			});

			// START KGU#889 2020-12-20: Enh. #890/7
			// popup for current coordinates
			JPanel jp = new JPanel();
			jp.setOpaque(false);
			lblPop.setPreferredSize(new Dimension(30,12));
			jp.add(lblPop);
			pop.add(jp);
			this.addWindowFocusListener(this);
			panel.addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseDragged(MouseEvent evt) {
					Point mousePt = evt.getPoint();
					Point offset = new Point();
					if (displacement != null) {
						offset = displacement;
					}
					Point truePt = new Point(
							Math.round(mousePt.x/zoomFactor) - offset.x,
							Math.round(mousePt.y/zoomFactor) - offset.y
							);
					// START KGU#889 2020-12-22: Enh. #890/8 snapping to lines or points
					double nearestDist = Double.MAX_VALUE;
					Point nearestPt = null;
					double dist = truePt.distance(owner.pos);
					if (dist <= snapRadius) {
						nearestPt = owner.pos;
						nearestDist = dist;
					}
					int nElements = owner.elements.size();
					// Remark: consider a k-d-tree or a quad tree if linear search tends be too slow
					for (int i = 0; i < nElements; i++) {
						Point npt = owner.elements.get(i).getNearestPoint(truePt, snapLines);
						dist = npt.distance(truePt);
						if (dist <= snapRadius && dist < nearestDist) {
							nearestPt = npt;
							nearestDist = dist;
							if (dist < 0.5) {
								break;
							}
						}
					}
					if (nearestPt != null) {
						truePt = nearestPt;
						mousePt = new Point(
								Math.round((nearestPt.x + offset.x) * zoomFactor),
								Math.round((nearestPt.y + offset.y) * zoomFactor)
								);
					}
					// END KGU#889 2020-12-22
					if (dragStart == null) {
						Cursor cursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
						setCursor(cursor);
						dragStart = truePt;
					}
					mouseX = mousePt.x;
					mouseY = mousePt.y;
					Rectangle rect = new Rectangle(mouseX, mouseY, 1, 1);
					panel.scrollRectToVisible(rect);
					Point loc = ((JComponent) evt.getSource()).getLocationOnScreen().getLocation();
					int diffX = (truePt.x - dragStart.x);
					int diffY = (truePt.y - dragStart.y);
					if (diffX != 0 || diffY != 0) {
						if (evt.isShiftDown()) {
							if (Math.abs(diffY) < Math.abs(diffX)) {
								mouseY = Math.round((dragStart.y + offset.y) * zoomFactor);
								diffY = 0;
							}
							else {
								mouseX = Math.round((dragStart.x + offset.x) * zoomFactor);
								diffX = 0;
							}
						}
						double length = Math.sqrt(diffX*diffX + diffY*diffY);
						double orient = Math.toDegrees(Math.atan2(diffX, -diffY));
						String coordTip = String.format("%.2f (%d, %d) %.2f°",
								length, diffX, diffY, orient);
						lblPop.setText(coordTip);
						lblPop.setPreferredSize(
								new Dimension(
										8 + lblPop.getFontMetrics(lblPop.getFont()).stringWidth(coordTip),
										lblPop.getFontMetrics(lblPop.getFont()).getHeight()
										)
								);
						pop.setLocation(loc.x + mouseX, loc.y + mouseY+16);
						pop.setVisible(true);
						panel.repaint();
					}
				}

				@Override
				public void mouseMoved(MouseEvent evt) {
					if (dragStart != null) {
						Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
						setCursor(cursor);
						dragStart = null;
						panel.repaint();
					}
					if (pop.isVisible() != showCoordinates) {
						pop.setVisible(showCoordinates);
					}
					if (showCoordinates) {
						mouseX = evt.getX();
						mouseY = evt.getY();
						updateCoordinatePopup();
						Point loc = ((JComponent) evt.getSource()).getLocationOnScreen().getLocation();
						pop.setLocation(loc.x + mouseX, loc.y + mouseY+16);
						//pop.setVisible(true);
					}
				}
			});
			// END KGU#889 2020-12-20
			
			this.addKeyListener(this);
		}

		/**
		 * Updates all status information in the status bar and controls
		 * the menu item accessibility.
		 */
		private void updateStatus() {
			if (statusbar.isVisible()) {
				statusHome.setText("(" + owner.home.x + ", " + owner.home.y + ")");
				//int turtleX = owner.pos.x;
				//int turtleY = owner.pos.y;
				// START KGU#893 2020-12-21: Bugfix #894
				//if (displacement != null) {
				//	turtleX -= displacement.x;
				//	turtleY -= displacement.y;
				//}
				//statusTurtle.setText(
				//		String.format("(%d, %d) %.2f°",
				//				turtleX, turtleY, owner.getOrientation()));
				statusTurtle.setText(
						String.format("(%d, %d) %.2f°",
								owner.pos.x, owner.pos.y, owner.getOrientation()));
				// END KGU#893 2020-12-21
				Rectangle size = new Rectangle(owner.bounds);
				size.add(owner.pos);
				statusSize.setText(
						// rightmost coordinate
						Math.max(size.x + size.width, 0)
						+ " x "
						// bottom coordinate
						+ Math.max(size.y + size.height, 0)
						);
				synchronized(zoomMutex) {
					Rectangle vRect = scrollarea.getViewport().getViewRect();
					vRect.x /= zoomFactor;
					vRect.y /= zoomFactor;
					vRect.width /= zoomFactor;
					vRect.height /= zoomFactor;
					// START KGU#889 2020-12-20: Issue #890/7
					if (displacement != null) {
						vRect.x -= displacement.x;
						vRect.y -= displacement.y;
					}
					// END KGU#889 2020-12-20
					statusViewport.setText(String.format("%d .. %d : %d .. %d",
							vRect.x, vRect.x + vRect.width,
							vRect.y, vRect.y + vRect.height));
					statusZoom.setText(String.format("%.1f %%", 100 * zoomFactor));
				}
			}
			popupShowTurtle.setSelected(!owner.turtleHidden);
			popupShowOrigin.setEnabled(displacement != null);
			if (displacement == null) {
				popupShowOrigin.setSelected(false);
			}
			popupExtendCanvas.setEnabled(owner.bounds.x < 0 || owner.bounds.y < 0);
			popupZoom100.setEnabled(zoomFactor != 1.0f);
			// START KGU#889 2020-12-17: issue #890
			popupExportCSV.setEnabled(!owner.elements.isEmpty());
			popupExportSVG.setEnabled(owner.bounds.width > 0 || owner.bounds.height > 0);
			// END KGU#889 2020-12-17
			// START KGU#889 2020-12-20: Enh. #890/7
			popupShowCoords.setSelected(showCoordinates);
			// END KGU#889 2020-12-20
		}

		// START KGU#685 2020-12-11: Enh. #704
		/**
		 * Creates the popup menu
		 */
		private void initPopupMenu()
		{
			popupMenu = new javax.swing.JPopupMenu();
			popupGotoCoord = new javax.swing.JMenuItem("Scroll to coordinate ...");
			popupGotoTurtle = new javax.swing.JMenuItem("Scroll to turtle position");
			popupGotoHome = new javax.swing.JMenuItem("Scroll to home position");
			// START KGU#889 2020-12-18: Enh. #890
			popupGotoOrigin = new javax.swing.JMenuItem("Scroll to origin (0,0)");
			// END KGU#889 2020-12-18
			popupExtendCanvas = new javax.swing.JMenuItem("Make all drawing visible");
			popupZoom100 = new javax.swing.JMenuItem("Reset zoom to 100%");
			popupZoomBounds = new javax.swing.JMenuItem("Zoom to the bounds");
			popupShowOrigin = new javax.swing.JCheckBoxMenuItem("Show axes of coordinates");
			popupShowTurtle = new javax.swing.JCheckBoxMenuItem("Show turtle");
			popupShowStatus = new javax.swing.JCheckBoxMenuItem("Show status bar");
			popupShowCoords = new javax.swing.JCheckBoxMenuItem("Pop up coordinates");
			// START KGU#889 2020-12-23: Enh. #890/8
			popupSnapLines = new javax.swing.JCheckBoxMenuItem("Snap lines (else: points only)");
			popupSnapRadius = new javax.swing.JMenuItem("Set measuring snap radius ...");
			// END KGU#889 2020-12-23
			popupExportCSV = new javax.swing.JMenuItem("Export drawing intems as CSV ...");
			popupExportImage = new javax.swing.JMenu("Export as image");
			popupExportPNG = new javax.swing.JMenuItem("to PNG ...");
			popupExportSVG = new javax.swing.JMenuItem("to SVG ...");
			popupBackground = new javax.swing.JMenuItem("Set background color ...");

			popupGotoCoord.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gotoCoordinate();
				}});
			popupMenu.add(popupGotoCoord);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupGotoCoord.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));

			popupGotoTurtle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gotoTurtle();
				}});
			popupMenu.add(popupGotoTurtle);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupGotoTurtle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));

			popupGotoHome.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gotoHome();
				}});
			popupMenu.add(popupGotoHome);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupGotoHome.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));

			// START KGU#889 2020-12-18: Enh. #890
			popupGotoOrigin.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gotoOrigin();
				}});
			popupMenu.add(popupGotoOrigin);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupGotoOrigin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0));

			// END KGU#889 2020-12-18

			popupMenu.addSeparator();
			
			popupZoom100.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zoom(1.0f);
				}
			});
			popupMenu.add(popupZoom100);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupZoom100.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));

			popupZoomBounds.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zoomToBounds();
				}

			});
			popupMenu.add(popupZoomBounds);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupZoomBounds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0));

			popupMenu.addSeparator();
			
			popupExtendCanvas.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fixDisplacement();
				}

			});
			popupMenu.add(popupExtendCanvas);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			// START KGU#889 2020-12-21: Issue #890/7
			//popupMoveIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
			popupExtendCanvas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
			// END KGU#889 2020-12-21
			
			popupShowOrigin.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					repaint();
				}});
			popupMenu.add(popupShowOrigin);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupShowOrigin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0));
			popupShowOrigin.setEnabled(displacement != null);
			
			popupMenu.addSeparator();			

			popupShowTurtle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					owner.turtleHidden = !popupShowTurtle.isSelected();
					repaint();
				}});
			popupMenu.add(popupShowTurtle);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupShowTurtle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));

			popupBackground.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setBackground();
				}});
			popupMenu.add(popupBackground);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupBackground.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0));

			popupMenu.addSeparator();

			popupShowStatus.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					statusbar.setVisible(popupShowStatus.isSelected());
					updateStatus();
				}});
			popupMenu.add(popupShowStatus);
			popupShowStatus.setSelected(true);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupShowStatus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));

			// START KGU#889 2020-12-20: Enh. #890/7
			popupShowCoords.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showCoordinates = popupShowCoords.isSelected();
					// Just rapidly switch off, otherwise the next mouse motion will pop it up
					if (!showCoordinates) {
						pop.setVisible(false);
					}
				}});
			popupMenu.add(popupShowCoords);
			popupShowCoords.setSelected(true);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupShowCoords.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
			// END KGU#889 2020-12-20

			// START KGU#889 2020-12-23: Enh. #890/8
			popupSnapLines.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					snapLines = popupSnapLines.isSelected();
				}});
			popupMenu.add(popupSnapLines);
			popupSnapLines.setSelected(snapLines);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupSnapLines.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));

			popupSnapRadius.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					configureMeasuring();
				}});
			popupMenu.add(popupSnapRadius);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupSnapRadius.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
			// END KGU#889 2020-12-23

			popupMenu.addSeparator();

			popupExportCSV.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exportCSV();
				}});
			popupMenu.add(popupExportCSV);

			popupMenu.add(popupExportImage);
			
			popupExportPNG.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exportPNG();
				}});
			popupExportImage.add(popupExportPNG);
			// This doesn't work directly but shows the key binding handled via keyPressed()
			popupExportPNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
			
			popupExportSVG.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exportSVG();
				}});
			popupExportImage.add(popupExportSVG);
			//popupExportImage.setEnabled(false);
		}
		
		/**
		 * Adjusts the zoom factor to the next higher or lower step
		 * @param zoomIn - {@code true} to zoom in, {@code false} to zoom out
		 */
		private void zoom(boolean zoomIn)
		{
			if (zoomIn) {
				zoom(this.zoomFactor / ZOOM_RATE);
			}
			else {
				zoom(this.zoomFactor * ZOOM_RATE);
			}
		}
		
		/**
		 * Sets the zoom factor to given value {@code newFactor} if it doesn't
		 * exceed certain limits
		 * @param newFactor - proposed zoom factor
		 */
		private void zoom(float newFactor)
		{
			Point center = null;
			synchronized(zoomMutex) {
				Rectangle vRect = scrollarea.getViewport().getViewRect();
				// Try to maintain centre coordinate
				center = new Point(
						Math.round((vRect.x + vRect.width/2) / zoomFactor),
						Math.round((vRect.y + vRect.height/2) / zoomFactor)
						);
				this.zoomFactor = Math.max(MIN_ZOOM, Math.min(newFactor, MAX_ZOOM));
				panel.updatePreferredSize(true);
			}
			// Try to maintain centre coordinate
			gotoCoordinate(center);
			// Is this necessary?
			//this.dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED));
			repaintAll();
			if (pop.isVisible()) {
				updateCoordinatePopup();
			}
		}
		
		/**
		 * Zooms such that the entire drawing fits into the viewport
		 */
		private void zoomToBounds() {
			Rectangle bounds = new Rectangle(owner.bounds);
			if (!owner.turtleHidden) {
				bounds.add(owner.pos);
			}
			// START KGU#893 2002-12-21: Bugfix #894
			if (displacement != null) {
				bounds.x += displacement.x;
				bounds.y += displacement.y;
			}
			// END KGU#893 2020-12-21
			// Restrict the considered bounds to the visible part
			if (bounds.x < 0) {
				bounds.width += bounds.x;
				if (bounds.width < 1) bounds.width = 1;
				bounds.x = 0;
			}
			if (bounds.y < 0) {
				bounds.height += bounds.y;
				if (bounds.height < 1) bounds.height = 1;
				bounds.y = 0;
			}
			// This is the actual viewport size
			Rectangle view = scrollarea.getViewport().getViewRect();
			float zoomH = 1.0f * view.width / (bounds.width + MARGIN);
			float zoomV = 1.0f * view.height / (bounds.height + MARGIN);
			zoom(Math.min(zoomH, zoomV));
			pop.setVisible(false);
			gotoCoordinate(new Point(bounds.x + bounds.width/2, bounds.y + bounds.height/2));
		}

		/**
		 * Asks the user for a target coordinate and returns the entered coordinate
		 * as {@link Point} or its nearest point within the occupied area (if the
		 * coordinate lie outside).
		 * @return the {@link Point} representing the user input or {@code null} if
		 * cancelled.
		 */
		protected Point askForCoordinate() {
			Point point = null;
			JPanel pnlCoords = new JPanel();
			pnlCoords.setLayout(new java.awt.GridBagLayout());
			java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 2;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.anchor = java.awt.GridBagConstraints.LINE_START;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc.insets = new java.awt.Insets(1, 2, 2, 1);
			javax.swing.JLabel lblPrompt = new javax.swing.JLabel("Target coordinate");
			pnlCoords.add(lblPrompt, gbc);
			javax.swing.JTextField[] fields = new javax.swing.JTextField[2];

			gbc.gridwidth = 1;
			for (int row = 0; row < 2; row++) {
				gbc.gridy++;
				gbc.gridx = 0;
				gbc.weightx = 0;
				gbc.fill = java.awt.GridBagConstraints.NONE;
				pnlCoords.add(new javax.swing.JLabel((char)('x' + row) + ""), gbc);
				gbc.gridx++;
				gbc.weightx = 1;
				gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
				fields[row] = new javax.swing.JTextField();
				pnlCoords.add(fields[row], gbc);
				if (lastAskedCoords != null) {
					fields[row].setText(Integer.toString(lastAskedCoords[row]));
				}
			}

			do {
				boolean committed = JOptionPane.showConfirmDialog(this, pnlCoords,
						popupGotoCoord.getText(),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
				if (!committed) {
					return null;
				}
				point = new Point();
				for (int row = 0; row < fields.length; row++) {
					try {
						int coord = Integer.parseInt(fields[row].getText());
						if (row == 0) {
							point.x = coord;
						}
						else {
							point.y = coord;
						}
						fields[row].setForeground(null);
						if (lastAskedCoords == null) {
							lastAskedCoords = new int[] {0, 0};
						}
						lastAskedCoords[row] = coord;
					}
					catch (NumberFormatException ex) {
						fields[row].setForeground(Color.RED);
						point = null;
						break;
					}
				}
			} while (point == null);
			return point;
		}
		
		/**
		 * Opens a colour chooser dialog allowing to specify a colour with the
		 * given {@code oldColor} as initial setting
		 * @param oldColor - colour to be presented (white will be the default)
		 * @return the chosen colour or {@code null}
		 */
		protected Color chooseBackground(Color oldColor) {
			if (colChooser == null) {
				colChooser = new javax.swing.JColorChooser();
			}
			Color bgColor = null;
			if (oldColor != null) {
				colChooser.setColor(oldColor.getRGB());
			}
			if (JOptionPane.showConfirmDialog(this, colChooser,
					popupBackground.getText(),
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				bgColor = colChooser.getColor();
			}
			return bgColor;
		}

		// START KGU#889 2020-12-23: Enh. #890/8
		/**
		 * Opens a dialog to allow the snap configuration for the measuring
		 * function
		 */
		protected void configureMeasuring() {
			// We need a spinner
			JPanel pnl = new JPanel();
			pnl.setLayout(new javax.swing.BoxLayout(pnl, javax.swing.BoxLayout.X_AXIS));
			pnl.add(new javax.swing.JLabel(popupSnapRadius.getText().replace("...", "")));
			javax.swing.JSpinner spnScale = new javax.swing.JSpinner();
			SpinnerModel spnModel = new SpinnerNumberModel(snapRadius, 5, 100, 1);
			spnScale.setModel(spnModel);
			pnl.add(spnScale);
			JPanel content = new JPanel();
			content.add(pnl);
			if (JOptionPane.showConfirmDialog(this, content,
					popupSnapRadius.getText(),
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				snapRadius = (int)spnScale.getValue();
			}
		}
		// END KGU#889 2020-12-23

		/**
		 * Exports the drawing elements and moves to a CSV file. Will open a
		 * file chooser dialog first. May pop up a message box if something goes wrong.
		 */
		private void exportCSV()
		{
			JFileChooser fc = new JFileChooser(currentDirectory);
			fc.setDialogTitle(popupExportCSV.getText());
			ExtFileFilter filter = new CSVFilter();
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			// START KGU#889 2020-12-18: Issue #890 - choosable separator
			addCSVAccessory(fc);
			// END KGU#889 2020-12-18
			int decision = fc.showSaveDialog(this);
			if (decision == JFileChooser.APPROVE_OPTION) {
				File chosen = fc.getSelectedFile();
				if (chosen != null) {
					// START KGU#889 2020-12-18: Issue #890 - choosable separator
					String separator = getSeparatorCSV();
					// END KGU#889 2020-12-18
					// Ensure acceptable file extension
					if (!filter.accept(chosen)) {
						// START KGU#889 2020-12-17: Issue #890
						//chosen = new File(chosen.getPath() + ".txt");
						chosen = new File(chosen.getPath() + "." + filter.getAcceptedExtensions()[0]);
						// END KGU#889 2020-12-17
					}
					Path path = chosen.toPath().toAbsolutePath();
					// Check for overriding
					if (Files.exists(path)) {
						decision = JOptionPane.showConfirmDialog(this,
								msgOverwrite.getText(),
								popupExportCSV.getText(), JOptionPane.OK_CANCEL_OPTION);
						if (decision != JOptionPane.OK_OPTION) {
							return;
						}
					}
					try (BufferedWriter bw = Files.newBufferedWriter(path)) {
						// Write header
						// START KGU#889 2020-12-18: Issue #890 - choosable separator
						//bw.append("xFrom,yFrom,xTo,yTo,color\n");
						for (int i = 0; i < CSV_COL_HEADERS.length; i++) {
							if (i > 0) {
								bw.append(separator);
							}
							bw.append(CSV_COL_HEADERS[i]);
						}
						bw.newLine();
						// END KGU#889 2020-12-18
						// Write lines
						int nElements = owner.elements.size();
						for (int i = 0; i < nElements; i++) {
							// START KGU#889 2020-12-18: Issue #890 - Moves are redundant
//							bw.append(owner.elements.get(i).toCSV(null));
//							bw.newLine();
							Element el = owner.elements.get(i);
							if (!(el instanceof Move)) {
								bw.append(el.toCSV(separator));
								bw.newLine();
							}
							// END KGU#889 2020-12-18
						}
					} catch (IOException exc) {
						String message = exc.getMessage();
						if (message == null || message.isEmpty()) {
							message = exc.toString();
						}
						JOptionPane.showMessageDialog(this, message,
								popupExportCSV.getName(),
								JOptionPane.ERROR_MESSAGE);
					}
					if (Files.exists(path) && !chosen.isDirectory()) {
						currentDirectory = chosen.getParentFile();
					}
				}
			}
		}

		// START KGU#889 2020-12-18: Issue #890 - choosable separator
		/**
		 * Adds an accessory panel for CSV export to the given file chooser {@code fc}
		 * @param fc - a {@link JFileChooser}
		 */
		private void addCSVAccessory(JFileChooser fc) {
			JPanel pnl = new JPanel();
			pnl.setLayout(new java.awt.GridLayout(0, 1));
			pnl.add(lblSeparator);
			int nSepa = Math.min(rbSeparators.length, SEPARATORS.length);
			if (bg == null) {
				// Construct the radio button group for the separator choice
				bg = new javax.swing.ButtonGroup();
				if (nSepa > 0) {
					rbSeparators[0].setSelected(true);
				}
				for (int i = 0; i < nSepa; i++) {
					bg.add(rbSeparators[i]);
				}
			}
			for (int i = 0; i < nSepa; i++) {
				pnl.add(rbSeparators[i]);
			}
			JPanel accessory = new JPanel();
			accessory.add(pnl);
			fc.setAccessory(accessory);
		}

		/**
		 * Identifies and returns the chosen CSV separator string
		 * @return the separator as string (or null)
		 */
		private String getSeparatorCSV() {
			String separator = null;
			if (bg != null) {
				int nSepa = Math.min(rbSeparators.length, SEPARATORS.length);
				for (int i = 0; i < nSepa; i++) {
					if (rbSeparators[i].isSelected()) {
						separator = Character.toString(SEPARATORS[i]);
						break;
					}
				}
			}
			return separator;
		}
		// END KGU#889 2020-12-18

		/**
		 * Exports the drawing to an SVG file. Will open a file chooser dialog first
		 * and ask for a scaling factor.
		 * May pop up a message box if something goes wrong.
		 */
		private void exportSVG()
		{
			final int MAX_POINTS_PER_PATH = 800;
			JFileChooser fc = new JFileChooser(currentDirectory);
			fc.setDialogTitle(
					(popupExportImage.getText() + " " + popupExportSVG.getText())
					.replace("...", ""));
			ExtFileFilter filter = new SVGFilter();
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			// START KGU#685 2020-12-17: Issue #704 replaces posterior option query
			SpinnerModel spnModel = addScaleAccessory(fc);
			int scale = 0;
			// END KGU#685 2020-12-17
			int decision = fc.showSaveDialog(this);
			if (decision == JFileChooser.APPROVE_OPTION && (scale = (int)spnModel.getValue()) >= 1) {
				File chosen = fc.getSelectedFile();
				if (chosen != null) {
					// Ensure acceptable file extension
					if (!filter.accept(chosen)) {
						// START KGU#889 2020-12-17: Issue #704
						//chosen = new File(chosen.getPath() + ".svg");
						chosen = new File(chosen.getPath() + "." + filter.getAcceptedExtensions()[0]);
						// END KGU#889 2020-12-17
					}
					Path path = chosen.toPath().toAbsolutePath();
					// Check for overriding
					if (Files.exists(path)) {
						decision = JOptionPane.showConfirmDialog(this,
								msgOverwrite.getText(),
								popupExportCSV.getText(), JOptionPane.OK_CANCEL_OPTION);
						if (decision != JOptionPane.OK_OPTION) {
							return;
						}
					}
					int offsetX = -owner.bounds.x, offsetY = -owner.bounds.y;
					// START KGU#685 2020-12-17: Issue #704 replaces posterior option query
					this.lastAskedScale = scale;
					// END KGU#685 2020-12-17
					try (BufferedWriter bw = Files.newBufferedWriter(path)) {
						bw.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
						bw.append("<!-- Created with " + owner.getClass().getName()
								+ " (https://structorizer.fisch.lu) -->\n");
						bw.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\""
								+ owner.bounds.width * scale + "\" height=\""
								+ owner.bounds.height * scale + "\">\n");
						String title = chosen.getName();
						if (title.contains(".")) {
							title = title.substring(0, title.lastIndexOf('.')-1);
						}
						bw.append("  <title>" + title + "</title>\n");
						
						/* Draw the background:
						 * The fill colour must not be given as hex code, otherwise the rectangle
						 * will always be black! */
						bw.append(String.format("    <rect style=\"fill:rgb(%d,%d,%d);fill-opacity:1\" ",
								owner.backgroundColor.getRed(),
								owner.backgroundColor.getGreen(),
								owner.backgroundColor.getBlue()));
						bw.append(String.format(" x=\"0\" y=\"0\" width=\"%d\" height=\"%d\" ",
								Math.round(owner.bounds.width * scale),
								Math.round(owner.bounds.height * scale)));
						bw.append("id=\"background\"/>\n");
						
						// Now export the elements
						bw.append("  <g id=\"elements\" style=\"fill:none;stroke-width:"
								+ Math.round(scale) + "px;stroke-opacity:1:stroke-linejoin:miter\">\n");
						Point lastPt = null;
						Color lastCol = null;
						int nPoints = 0;
						int nElements = owner.elements.size();
						for (int i = 0; i < nElements; i++) {
							Element el = owner.elements.get(i);
							if (el instanceof Line) {
								Point from = el.getFrom();
								Point to = el.getTo();
								Color col = el.getColor();
								if (lastPt == null || !lastPt.equals(from)
										|| lastCol == null || !lastCol.equals(col)
										|| nPoints >= MAX_POINTS_PER_PATH) {
									if (i > 0) {
										bw.append("\" />\n");
									}
									bw.append("    <path\n");
									bw.append("      style=\"stroke:#"
									+ Integer.toHexString(col.getRGB()).substring(2)
									+ "\"\n");
									bw.append(String.format("      id=\"path%1$05d\"\n", i));
									bw.append(
											String.format(
													Locale.ENGLISH,
													"      d=\"m %d,%d ",
													(from.x + offsetX) * scale,
													(from.y + offsetY) * scale));
								}
								bw.append(String.format(
												Locale.ENGLISH,
												"%d,%d ",
												(to.x - from.x) * scale,
												(to.y - from.y) * scale));
								lastPt = to;
								lastCol = col;
								nPoints++;
							}
						}
						if (lastPt != null) {
							bw.append("\" />\n");
						}
						bw.append("  </g>\n");
						bw.append("</svg>\n");
					} catch (IOException exc) {
						String message = exc.getMessage();
						if (message == null || message.isEmpty()) {
							message = exc.toString();
						}
						JOptionPane.showMessageDialog(this, message,
								fc.getDialogTitle(),
								JOptionPane.ERROR_MESSAGE);
					}
					if (Files.exists(path) && !chosen.isDirectory()) {
						currentDirectory = chosen.getParentFile();
					}
				}
			}
		}
		
		/**
		 * Adds some text and an integer spinner to the given file chooser
		 * {@code fc}.
		 * @param fc - a file chooser
		 * @return the spinner model of the added number spinner to allow to
		 * obtain the value
		 */
		private SpinnerModel addScaleAccessory(JFileChooser fc) {
			JPanel pnl = new JPanel();
			pnl.setLayout(new javax.swing.BoxLayout(pnl, javax.swing.BoxLayout.Y_AXIS));
			pnl.add(new javax.swing.JLabel(statusSize.getToolTipText() + ":"));
			pnl.add(new javax.swing.JLabel(String.format("%d x %d pixel", owner.bounds.width, owner.bounds.height)));
			pnl.add(new javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL));
			pnl.add(lblScale);
			javax.swing.JSpinner spnScale = new javax.swing.JSpinner();
			SpinnerModel spnModel = new SpinnerNumberModel(lastAskedScale, 1, 10, 1);
			spnScale.setModel(spnModel);
			pnl.add(spnScale);
			JPanel accessory = new JPanel();
			accessory.add(pnl);
			fc.setAccessory(accessory);
			return spnModel;
		}

		/**
		 * Exports the drawing as PNG file. Will open a file chooser dialog first.
		 * May pop up a message box if something goes wrong.
		 */
		private void exportPNG()
		{
			JFileChooser dlgSave = new JFileChooser(currentDirectory);
			ExtFileFilter filter = new PNGFilter();
			dlgSave.addChoosableFileFilter(filter);
			dlgSave.setFileFilter(filter);
			dlgSave.setDialogTitle(
					(popupExportImage.getText() + " " + popupExportPNG.getText())
					.replace("...", ""));
			int decision = dlgSave.showSaveDialog(this);
			if (decision == JFileChooser.APPROVE_OPTION)
			{
				currentDirectory = dlgSave.getCurrentDirectory();
				while (currentDirectory != null && !currentDirectory.isDirectory())
				{
					currentDirectory = currentDirectory.getParentFile();
				}
				// correct the filename, if necessary
				File chosen = dlgSave.getSelectedFile();
				if (!filter.accept(chosen)) {
					// START KGU#889 2020-12-17: Issue #704
					//chosen = new File(chosen.getPath() + ".png");
					chosen = new File(chosen.getPath() + "." + filter.getAcceptedExtensions()[0]);
					// END KGU#889 2020-12-17
				}
				// Check for overriding
				Path path = chosen.toPath().toAbsolutePath();
				if (Files.exists(path)) {
					decision = JOptionPane.showConfirmDialog(this,
							msgOverwrite.getText(),
							popupExportCSV.getText(), JOptionPane.OK_CANCEL_OPTION);
					if (decision != JOptionPane.OK_OPTION) {
						return;
					}
				}
				synchronized(zoomMutex) {
					int offsetX = 0, offsetY = 0;
					Dimension dim = panel.getPreferredSize();
					int width = dim.width - Math.round(offsetX * zoomFactor);
					int height = dim.height - Math.round(offsetY * zoomFactor);
					BufferedImage bi = new BufferedImage(
							Math.round(width / this.zoomFactor),
							Math.round(height / this.zoomFactor),
							BufferedImage.TYPE_4BYTE_ABGR);
					try
					{
						panel.paint(bi.getGraphics(), true);
						ImageIO.write(bi, "png", chosen);
					}
					catch(Exception e)
					{
						String message = e.getMessage();
						if (message == null || message.trim().isEmpty()) {
							message = e.toString();
						}
						JOptionPane.showMessageDialog(this,
								message,
								dlgSave.getDialogTitle(),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}

		/**
		 * Repaints the turtle drawing and updates the status bar
		 */
		public void repaintAll()
		{
			panel.repaint();
			updateStatus();
		}

		@Override
		public void keyTyped(KeyEvent ev) {
			// Nothing to do here
		}

		@Override
		public void keyPressed(KeyEvent ev) {
			if (ev.getSource() == this) {
				if (dragStart != null && ev.getKeyCode() != KeyEvent.VK_SHIFT) {
					dragStart = null;
					Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
					setCursor(cursor);
					panel.repaint();
				}
				if (!ev.isAltDown() && !ev.isAltGraphDown()) {
					switch (ev.getKeyCode()) {
					case KeyEvent.VK_HOME:
						gotoHome();
						break;
					case KeyEvent.VK_END:
						gotoTurtle();
						break;
					case KeyEvent.VK_G:
						gotoCoordinate();
						break;
					case KeyEvent.VK_T:
						owner.turtleHidden = !owner.turtleHidden;
						popupShowTurtle.setSelected(!owner.turtleHidden);
						repaint();
						break;
					case KeyEvent.VK_B:
						setBackground();
						break;
					case KeyEvent.VK_S:
						if (ev.isControlDown()) {
							exportPNG();
						}
						else {
							statusbar.setVisible(!statusbar.isVisible());
							popupShowStatus.setSelected(statusbar.isVisible());
							updateStatus();
						}
						break;
						// START KGU#889 2020-12-20: Enh. #890/7
					case KeyEvent.VK_C:
						showCoordinates = !showCoordinates;
						popupShowCoords.setSelected(showCoordinates);
						if (!showCoordinates) {
							pop.setVisible(false);
						}
						break;
						// END KGU#889 2020-12-20
						// START KGU#889 2020-12-23: Enh. #890/8
					case KeyEvent.VK_L:
						snapLines = !snapLines;
						popupSnapLines.setSelected(snapLines);
						break;
					case KeyEvent.VK_R:
						configureMeasuring();
						break;
						// END KGU#889 2020-12-23
					case KeyEvent.VK_UP:
						handleCursorKey(-1, false, ev.isShiftDown() ? 10 : 1);
						break;
					case KeyEvent.VK_DOWN:
						handleCursorKey(+1, false, ev.isShiftDown() ? 10 : 1);
						break;
					case KeyEvent.VK_LEFT:
						handleCursorKey(-1, true, ev.isShiftDown() ? 10 : 1);
						break;
					case KeyEvent.VK_RIGHT:
						handleCursorKey(+1, true, ev.isShiftDown() ? 10 : 1);
						break;
					case KeyEvent.VK_PAGE_UP:
						handlePageKey(-1, ev.isShiftDown());
						break;
					case KeyEvent.VK_PAGE_DOWN:
						handlePageKey(1, ev.isShiftDown());
						break;
					case KeyEvent.VK_ADD:
						zoom(true);
						break;
					case KeyEvent.VK_SUBTRACT:
						zoom(false);
						break;
					case KeyEvent.VK_1:
						zoom(1.0f);
						break;
					case KeyEvent.VK_Z:
						zoomToBounds();
						break;
						// START KGU#889 2020-12-21: Issue #890/7
						//case KeyEvent.VK_M:
					case KeyEvent.VK_A:
						// END KGU#889 2020-12-21
						if (owner.bounds.x < 0 || owner.bounds.y < 0) {
							fixDisplacement();
						}
						break;
					case KeyEvent.VK_O:
						if (displacement != null) {
							popupShowOrigin.doClick();
						}
						break;
						// START KGU#889 2020-12-18: Enh. #890
					case KeyEvent.VK_0:
						gotoOrigin();
						break;
						// END KGU#889 2020-12-18
					case KeyEvent.VK_F1:
						this.helpTurtleizer(false);
						break;
					}
				}
				else if (ev.isAltDown() && ev.getKeyCode() == KeyEvent.VK_F1) {
					this.helpTurtleizer(true);
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent ev) {
			// Nothing to do here
		}
		
		private void gotoHome()
		{
			Point home = new Point(owner.home);
			pop.setVisible(false);
			if (displacement != null) {
				home.x += displacement.x;
				home.y += displacement.y;
			}
			gotoCoordinate(home);
		}
		
		private void gotoTurtle()
		{
			pop.setVisible(false);
			// START KGU#893 2002-12-21: Bugfix #894
			//gotoCoordinate(owner.pos);
			Point turtle = new Point(owner.pos);
			if (displacement != null) {
				turtle.x += displacement.x;
				turtle.y += displacement.y;
			}
			gotoCoordinate(turtle);
			// END KGU#893 2020-12-21
		}
		
		private void gotoCoordinate()
		{
			Point coord = askForCoordinate();
			if (coord != null) {
				pop.setVisible(false);
				// START KGU#893 2002-12-21: Bugfix #894
				if (displacement != null) {
					coord.x += displacement.x;
					coord.y += displacement.y;
				}
				// END KGU#893 2020-12-21
				gotoCoordinate(coord);
			}
		}
		
		// START KGU#889 2020-12-18: Enh. #890
		private void gotoOrigin()
		{
			pop.setVisible(false);
			if (displacement != null) {
				gotoCoordinate(displacement);
			}
			else {
				gotoCoordinate(new Point());
			}
		}
		// END KGU#889 2020-12-18

		/**
		 * Scrolls to the given point {@code coord}, trying to centre the
		 * viewport around it (which will not of course work if the point
		 * is too near to the canvas border.
		 * @param coord - the target coordinate
		 */
		private void gotoCoordinate(Point coord) {
			Rectangle vRect = null;
			synchronized(zoomMutex) {
				vRect = scrollarea.getViewport().getViewRect();
				int marginH = vRect.width/2;	// horizontal margin
				int marginV = vRect.height/2;	// vertical margin
				// Estimate the position in the zoomed panel
				int posX = Math.round(coord.x * zoomFactor);
				int posY = Math.round(coord.y * zoomFactor);
				vRect.x = Math.max(posX - marginH, 0);
				vRect.y = Math.max(posY - marginV, 0);
			}
			if (vRect != null) {
				panel.scrollRectToVisible(vRect);
			}
			repaint();
		}
		
		/**
		 * Sets the {@link #displacement} such that all elements fit into the
		 * visible canvas (i.e. the positive quadrant)
		 */
		private void fixDisplacement()
		{
			if (owner.bounds.x < 0 || owner.bounds.y < 0) {
				// START KGU#893 2020-12-21: Bugfix #894
				// We must prevent any repaint activity during this action
				//synchronized (this.getTreeLock()) {
				//	Dimension shift = new Dimension(
				//			Math.max(-owner.bounds.x, 0),
				//			Math.max(-owner.bounds.y, 0));
				//	int nElements = owner.elements.size();
				//	for (int i = 0; i < nElements; i++) {
				//		owner.elements.get(i).move(shift);
				//	}
				//	// Move all important points as well (except home)
				//	owner.pos.x += shift.width; owner.pos.y += shift.height;
				//	owner.posX += shift.width; owner.posY += shift.height;
				//	owner.bounds.x += shift.width; owner.bounds.y += shift.height;
				//	displacement = new Point(shift.width, shift.height);
				//}
				displacement = new Point(
						Math.max(-owner.bounds.x, 0),
						Math.max(-owner.bounds.y, 0)
						);
				// END KGU#893 2020-12-21
				repaintAll();
			}
		}
		
		private void setBackground()
		{
			Color bgColor = chooseBackground(owner.backgroundColor);
			if (bgColor != null) {
				owner.setBackgroundColor(bgColor);
			}
		}
		
		private void handleCursorKey(int dir, boolean horizontal, int factor)
		{
			javax.swing.JScrollBar bar;
			if (horizontal) {
				bar = scrollarea.getHorizontalScrollBar();
			}
			else {
				bar = scrollarea.getVerticalScrollBar();
			}
			int units = bar.getUnitIncrement(dir) * factor;
			int oldVal = bar.getValue();
			bar.setValue(Math.max(oldVal + dir * units, 0));
			if (pop.isVisible()) {
				int incr = bar.getValue() - oldVal;
				if (horizontal) {
					mouseX += incr;
				}
				else {
					mouseY += incr;
				}
				updateCoordinatePopup();
			}
		}

		private void handlePageKey(int dir, boolean horizontal)
		{
			javax.swing.JScrollBar bar;
			if (horizontal) {
				bar = scrollarea.getHorizontalScrollBar();
			}
			else {
				bar = scrollarea.getVerticalScrollBar();
			}
			int oldVal = bar.getValue();
			bar.setValue(oldVal + dir * bar.getBlockIncrement(dir));
			if (pop.isVisible()) {
				int incr = bar.getValue() - oldVal;
				if (horizontal) {
					mouseX += incr;
				}
				else {
					mouseY += incr;
				}
				updateCoordinatePopup();
			}
		}

		/**
		 * Tries to open the online User Guide with the Arranger page in the browser
		 * @param keyBindings TODO
		 */
		public void helpTurtleizer(boolean keyBindings)
		{
			String query = keyBindings ? KEY_LINK : GUI_LINK;
			String help = HELP_URL.replace("%", query);
			boolean isLaunched = false;
			try {
				isLaunched = lu.fisch.utils.Desktop.browse(new URI(help));
			} catch (URISyntaxException ex) {
				logger.log(Level.WARNING, "Can't browse Turtleizer help URL.", ex);
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
				msgHelp.getText(),
				JOptionPane.ERROR_MESSAGE);
			}
		}
		
		
		// START KGU#889 2020-12-20: Enh. #890/7 coordinate tooltip
		@Override
		public void windowGainedFocus(WindowEvent e) {
			// Nothing to do here
		}

		@Override
		public void windowLostFocus(WindowEvent e) {
			pop.setVisible(false);
		}
		// END KGU#889 2020-12-20

		/**
		 * Writes the current turtle world coordinates into the coordinate popup
		 * and ensures appropriate size.
		 */
		private void updateCoordinatePopup() {
			int x = (int)(mouseX / zoomFactor);
			int y = (int)(mouseY / zoomFactor);
			if (displacement != null) {
				x -= displacement.x;
				y -= displacement.y;
			}
			String coordTip = String.format("(%d, %d)", x, y);
			lblPop.setText(coordTip);
			lblPop.setPreferredSize(
					new Dimension(
							8 + lblPop.getFontMetrics(lblPop.getFont()).stringWidth(coordTip),
							lblPop.getFontMetrics(lblPop.getFont()).getHeight()
							)
					);
		}
	}

	/** The GUI frame - while null, it hasn't been materialized (light-weight instance) */
	private TurtleFrame frame = null;
	/**
	 * @return the used frame (e.g. for localization purposes), may be {@code null}.
	 */
	public JFrame getFrame()
	{
		return frame;
	}
	
	/**
	 * Adapt the GUI components to the current look and feel
	 */
	public void updateLookAndFeel()
	{
		if (frame != null) {
			try {
				javax.swing.SwingUtilities.updateComponentTreeUI(frame);
				javax.swing.SwingUtilities.updateComponentTreeUI(frame.popupMenu);
			}
			catch (Exception ex) {}
		}
	}
	// END KGU#685 2020-12-11

    private static final String TITLE = "Turtleizer";
    
    // START KGU#685 2020-12-11: Enh. #704
    /** Width and height margin for the drawn area (regarding scrollbars) */
    private static final int MARGIN = 20;
    /** Maximum zoom factor */
    private static final float MAX_ZOOM = 2.0f;
    /** Minimum zoom factor */
    private static final float MIN_ZOOM = 0.01f;
    /** Zoom change factor */
    private static final float ZOOM_RATE = 0.9f;
    /** Flag to specify reverse zoom effect of mouse wheel */
    private boolean reverseZoomWheel = false;
    public void setReverseZoomWheel(boolean isReverse) {
        reverseZoomWheel = isReverse;
    }
    // END KGU#685 2020-12-11

    /** Rounded turtle position (okay for drawing but bad for consecutive drawing accuracy) */
    private Point pos;
    // START KGU#282 2016-10-16: Enh. #272
    /** Exact turtle position coordinates */
    private double posX, posY;
    // END KGU#282 2016-10-16
    /** Start coordinate for the drawing (turtle home) */
    private Point home;
    /** Internal turtle orientation in degrees (0° = right-bound, -90° = upwards) */
    private double angle = -90;
    /** The turtle image in standard size (about 36 x 36 pixel) */
    private Image image = (new ImageIcon(this.getClass().getResource("turtle.png"))).getImage();
    /** A higher resolution turtle image (about 72 x 72 pixel) */
    private Image image2 = (new ImageIcon(this.getClass().getResource("turtle2.png"))).getImage();
    private boolean isPenDown = true;
    // START KGU#303 2016-12-02: Enh. #302
    //private Color penColor = Color.BLACK;
    private Color defaultPenColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;
    /** Current pen colour */
    private Color penColor = defaultPenColor;
    // END KGU#303 2016-12-02
    private boolean turtleHidden = false;
    private int delay = 10;
    private Vector<Element> elements = new Vector<Element>();
    // START KGU#685 2020-12-14: Enh. #704
    /** bounding box of all visible elements, to be maintained via {@link #addLine(Point,Point,Color)} */
    private Rectangle bounds = new Rectangle();
    // END KGU#685 2020-12-14

    /**
     * This constructor does NOT realize a GUI, it just creates a light-weight instance
     * for API retrieval. A call of {@link #setVisible(boolean)} or the first use of an
     * API routine from {@link #definedProcedures} or {@link #definedFunctions} will establish
     * the graphics window, though.
     */
    public TurtleBox()
    {
        // START KGU#480 2018-01-16: Enh. #490 - This constructor no longer builds the GUI
        //init(300,300);
        home = new Point();
        reinit();
        // END KGU#480 2018-01-16
    }

    /**
     * Establishes a window frame with the graphics canvas for the turtle movements.
     * The turtle will be placed in the centre of he canvas.
     * @param width - the initial width of the frame in pixels
     * @param height - the initial height of the frame in pixels.
     */
    public TurtleBox(int width, int height)
    {
        init(width,height);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {}
    
    /* (non-Javadoc)
     * @see java.awt.Component#getName()
     */
    @Override
    public String getName()
    {
        return TITLE;
    }
    
    /**
     * Returns the "opposite" of the internal orientation of the turtle in degrees.
     * @return degrees (90° = North, positive sense = clockwise)
     * @see #getOrientation()
     */
    public double getAngle()
    {
        return 180 + angle;
    }
    
    // START KGU#417 2017-06-29: Enh. #424
    /**
     * API function returning the "external" turtle orientation in degrees
     * in the range -180 .. 180 where
     * <ul>
     * <li>0 is upwards/North (initial orientation),</li>
     * <li>positive sense is clockwise (right/East),</li>
     * <li>negative sense is counter-clockwise (left/West).</li>
     * </ul>
     * @return orientation in degrees.
     * @see #getAngle()
     */
    public double getOrientation() {
        double orient = angle + 90.0;
        // KGU#897 2020-12-23 Bugfix #897 Without the fix in rl() this bore the risk of eternal loops!
        while (orient > 180) { orient -= 360; }
        while (orient < -180) { orient += 360; }
        return orient == 0.0 ? orient : -orient;
    }
    // END KGU#417 2017-06-29
    
    // START #272 2016-10-16 (KGU)
    private void setPos(Point newPos)
    {
        pos = newPos;
        posX = pos.getX();
        posY = pos.getY();
    }
    
    private void setPos(double x, double y)
    {
        posX = x;
        posY = y;
        pos = new Point((int)Math.round(x), (int)Math.round(y));
    }
    // END #272 2016-10-16

    /**
     * Initialises this instance establishing the window with the graphics canvas
     * and places the Turtle in the centre.
     * @param width - initial width of the frame in pixels
     * @param height - initial height of the frame in pixels
     */
    private void init(int width, int height)
    {
        // START KGU#480 2018-01-16: Enh. #490 - care for the existence of a frame
        if (frame == null) {
            // START KGU#685 2020-12-12
            //frame = new JFrame();
            frame = new TurtleFrame(this);
            // END KGU#685 2020-12-12
        }
        // END KGU#480 2018-01-16
        frame.setTitle(TITLE);
        //frame.setIconImage((new ImageIcon(this.getClass().getResource("turtle.png"))).getImage());
        frame.setIconImage(image);

        //this.setDefaultCloseOperation(TurtleBox.EXIT_ON_CLOSE);
        //this.setDefaultCloseOperation(TurtleBox.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setBounds(0, 0, width, height);
        // START KGU#685 2020-12-11: Enh. #704
        //frame.getContentPane().add(panel);
        //this.setVisible(true);
        //setPos(new Point(panel.getWidth()/2,panel.getHeight()/2));
        //home = new Point(panel.getWidth()/2,panel.getHeight()/2);
        //panel.setDoubleBuffered(true);
        //panel.repaint();
        
        setPos(new Point(frame.scrollarea.getWidth()/2,
                frame.scrollarea.getHeight()/2));	// FIXME!
        home = new Point(pos.x, pos.y);

        frame.repaintAll();
        // END KGU#685 2020-12-11
    }

    //@Override
    /**
     * Realizes the window on the screen , brings it to front and fetches the window focus.
     * @param visible
     * @see JFrame#setVisible(boolean)
     */
    public void setVisible(boolean visible)
    {
        // START KGU#480 2018-01-16: Enh. #490 - lazy initialization
        if (visible && frame == null) {
            init(300, 300);
        }
        // END KGU#480 2018-01-16
        frame.setVisible(visible);
// START KGU#303 2016-12-03: Issue #302 - replaced by reinit() call below
//        elements.clear();
//        angle=-90;
//        // START KGU#303 2016-12-02: Enh. #302
//        backgroundColor = Color.WHITE;
//        defaultPenColor = Color.BLACK;
//        turtleHidden = false;
//        // END KGU#3032016-12-02
//        setPos(new Point(panel.getWidth()/2,panel.getHeight()/2));
// END KGU#303 2016-12-03
        if (visible) {
            // START KGU #685 2020-12-11: Enh. #704
            //home = new Point(panel.getWidth()/2, panel.getHeight()/2);
            home = new Point(Math.round(frame.scrollarea.getWidth()/2 / frame.zoomFactor),
                    Math.round(frame.scrollarea.getHeight()/2 / frame.zoomFactor));
            // END KGU#685 2020-12-11
            // START KGU#303 2016-12-03: Issue #302 - replaces disabled code above
            reinit();
            // END KGU#303 2016-12-03
            frame.paint(frame.getGraphics());
            // START KGU#685 2020-12-11: Enh. #704
            frame.updateStatus();
            // END KGU#685 2020-12-11
        }
    }
    
    // START KGU#303 2016-12-03: Issue #302
    /**
     * Undoes all possible impacts of a previous main diagram execution  
     */
    private void reinit()
    {
        elements.clear();
        angle = -90;
        backgroundColor = Color.WHITE;
        defaultPenColor = Color.BLACK;
        turtleHidden = false;
        // START KGU#685 2020-12-14: Enh. #704
        bounds = new Rectangle();
        bounds.width = -1;
        bounds.height = -1;
        if (frame != null) {
            frame.displacement = null;
        }
        // END KGU#685 2020-12-14
        setPos(home.getLocation());
        penDown();
    }
    // END KGU#303 2016-12-03
    
    private void delay()
    {
        //panel.repaint();
        // force repaint (not recommended!)
        // START KGU#480 2018-01-16: Enh. #490 - lazy initialization
        if (frame == null) {
            init(300, 300);
        }
        // END KGU#480 2018-01-16
        // START KGU#685 2020-12-11: Enh. #704
        //panel.repaint();
        frame.repaintAll();
        // END KGU#685 2020-12-11
        if (delay != 0)
        {
            try { Thread.sleep(delay); }
            catch (InterruptedException e) {
                System.err.println(e.getMessage());
                logger.log(Level.CONFIG, e.getMessage());
            }
        }
    }
    
    // START KGU#685 2020-12-14: Enh. #704
    /** Method to ensure incremental bounds adjustment */
    private void addLine(Point from, Point to, Color color)
    {
        Line line = new Line(from, to, color);
        elements.add(line);
        bounds.add(line.getBounds());
    }
    // END KGU#685 2020-12-14

    public void fd(Integer pixels)
    {
        Point newPos = new Point(pos.x-(int) Math.round(Math.cos(angle/180*Math.PI)*pixels),
                                 pos.y+(int) Math.round(Math.sin(angle/180*Math.PI)*pixels));
        if (isPenDown)
        {
            addLine(pos, newPos, penColor);
        }
        // START KGU 2020-12-22: Avoided for performance reasons and redundancy
        //else
        //{
        //    elements.add(new Move(pos,newPos));	// This is rather redundant
        //}
        // END KGU 2020-12-22
        //System.out.println("from: ("+pos.x+","+pos.y+") => to: ("+newPos.x+","+newPos.y+")");
        setPos(newPos);
        delay();
    }

    // START #272 2016-10-16 (KGU)
//    public void forward(int pixels)
//    {
//        fd(pixels);
//    }
    public void forward(Double pixels)
    {
        double newX = posX - Math.cos(angle/180*Math.PI) * pixels;
        double newY = posY + Math.sin(angle/180*Math.PI) * pixels;
        Point newPos = new Point((int)Math.round(newX), (int)Math.round(newY));
        if (isPenDown)
        {
            addLine(pos, newPos, penColor);
        }
        // START KGU 2020-12-22: Avoided for performance reasons and redundancy
        //else
        //{
        //    elements.add(new Move(pos, newPos));	// This is rater redundant
        //}
        // END KGU 2020-12-22
        //System.out.println("from: ("+pos.x+","+pos.y+") => to: ("+newPos.x+","+newPos.y+")");
        setPos(newX, newY);
        delay();
    }
    // END #272 2016-10-16

    public void bk(Integer pixels)
    {
        fd(-pixels);
    }

    public void backward(Double pixels)
    {
        forward(-pixels);
    }
    
    // START KGU#448 2017-10-28: Enh. #443 Wrappers with color argument
    public void fd(Integer pixels, Color color)
    {
        this.setColorNonWhite(color);
        this.fd(pixels);
    }
    public void bk(Integer pixels, Color color)
    {
        this.setColorNonWhite(color);
        this.fd(-pixels);
    }
    public void forward(Double pixels, Color color)
    {
        this.setColorNonWhite(color);
        this.forward(pixels);
    }
    public void backward(Double pixels, Color color)
    {
        this.setColorNonWhite(color);
        this.forward(-pixels);
    }
    // END KGU#448 2017-10-28

    private void rl(Double degrees)
    {
        // START KGU#897 2020-12-23: Bugfix #897 Mathematic risk fixed, e.g. on rl(1E30); rl(-1E30)
        //this.angle += degrees;
        /* Restrict both values to the range -360 ... 360 lest the result should
         * get corrupted by too large an order difference */
        double multiples = Math.signum(degrees) * Math.floor(Math.abs(degrees)/360.0);
        if (multiples != 0) {
            degrees -= multiples * 360.0;
        }
        this.angle += degrees;
        if (this.angle > 360.0) {
            this.angle -= 360.0;
        }
        else if (this.angle < -360.0) {
            this.angle += 360.0;
        }
        // END KGU#897 2020-12-23
        delay();
    }

    public void left(Double degrees)
    {
        rl(degrees);
    }

    private void rr(Double degrees)
    {
        rl(-degrees);
    }

    public void right(Double degrees)
    {
        rr(degrees);
    }
    
    // START KGU#303 2016-12-02: Enh. #302
    /** Non-retarded method to set the background colour directly
     * @param bgColor - the new background colour
     * @see #setBackgroundColor(Integer, Integer, Integer)
     */
    public void setBackgroundColor(Color bgColor)
    {
        // START KGU#480 2018-01-16: Enh. #490 - lazy initialization
        if (frame == null) {
            init(300, 300);
        }
        // END KGU#480 2018-01-16
        backgroundColor = bgColor;
        frame.panel.repaint();
    }

    /** Delayed API method to set the background colour from RGB values
     * @param red
     * @param green
     * @param blue
     * @see #setBackgroundColor(Color)
     */
    public void setBackgroundColor(Integer red, Integer green, Integer blue)
    {
        backgroundColor = new Color(Math.min(255, red), Math.min(255, green), Math.min(255, blue));
        delay();
    }

    /**
     * Non-retarded method to set the default pen colour directly
     * This colour is used whenever a move is carried out with colour WHITE
     * @param bgColor - the new foreground colour
     */
    public void setPenColor(Color penColor)
    {
        // START KGU#480 2018-01-16: Enh. #490 - lazy initialization
        if (frame == null) {
            init(300, 300);
        }
        // END KGU#480 2018-01-16
        defaultPenColor = penColor;
        frame.panel.repaint();
    }

    /**
     * Delayed API method to set the default pen colour from RGB values
     * This colour is used whenever a move is carried out with colour WHITE
     * @param red
     * @param green
     * @param blue
     */
    public void setPenColor(Integer red, Integer green, Integer blue)
    {
        defaultPenColor = new Color(Math.min(255, red), Math.min(255, green), Math.min(255, blue));
        delay();
    }
    // END KGU#303 2016-12-02

    // KGU: Where is this method used?
    /**
     * @return the angle from the turtle home position to its current position
     * in integral (!?) degrees
     */
    @Deprecated
    public double getAngleToHome()
    {
        // START #272 2016-10-16 (KGU)
        //double hypo = Math.sqrt(Math.pow((pos.x-home.x), 2)+Math.pow((pos.y-home.y),2));
        //double base = (home.x-pos.x);
        //double haut = (home.y-pos.y);
        double base = (home.x-posX);
        double haut = (home.y-posY);
        double hypo = Math.sqrt(base*base + haut*haut);
        // END #272 2016-10-16

        double sinAlpha = haut/hypo;
        double cosAlpha = base/hypo;

        double alpha = Math.asin(sinAlpha);

        alpha = alpha/Math.PI*180;

        // rounding (FIXME: what for?)
        alpha = Math.round(alpha*100)/100;

        if (cosAlpha < 0) // Q2 & Q3
        {
            alpha = 180-alpha;
        }
        alpha =- alpha;
        alpha -= getAngle();

        while (alpha < 0)
        {
            alpha += 360;
        }
        
        while (alpha>=360)
        {
            alpha -= 360;
        }

        return alpha;
    }

    /**
     * Raises the pen, i.e. subsequent moves won't leave a trace
     */
    public void penUp()
    {
        isPenDown=false;
    }

    /**
     * Pus the pen down, i.e. subsequent moves will leave a trace
     */
    public void penDown()
    {
        isPenDown=true;
    }

    /**
     * Direct positioning (without trace, without changing orientation)
     * @param x - new horizontal pixel coordinate (from left window edge)
     * @param y - new vertical pixel coordinate (from top window edge downwards)
     */
    public void gotoXY(Integer x, Integer y)
    {
        Point newPos = new Point(x,y);
        // START KGU 2020-12-22: Disabled for performance reasons - Moves are redundant
        //elements.add(new Move(pos, newPos));	// This is rather redundant
        // END KGU 2020-12-22
        setPos(newPos);
        delay();
   }

    /**
     * Direct vertical positioning (without trace, without changing orientation,
     * horizontal position remains unchanged)
     * @param y - new vertical pixel coordinate (from top window edge downwards)
     */
    public void gotoY(Integer y)
    {
        gotoXY(pos.x, y);
    }

    /**
     * Direct horizontal positioning (without trace, without changing orientation,
     * vertical position remains unchanged)
     * @param x - new horizontal pixel coordinate (from left window edge)
     */
    public void gotoX(Integer x)
    {
        gotoXY(x, pos.y);
    }

    /** The turtle icon will no longer be shown, has no impact on the pen */
    public void hideTurtle()
    {
        turtleHidden = true;
        delay();
    }

    /** The turtle icon will be shown again, has no impact on the pen */
    public void showTurtle()
    {
        turtleHidden = false;
        delay();
    }

    /**
     * Directly set the current pen colour (without delay), not part of the API, does not
     * modify the default pen colour, does not avert colour WHITE.
     * @param color - the new pen colour
     * @see #setColorNonWhite(Color)
     * @see #setPenColor(Color)
     * @see #setBackground(Color)
     */
    public void setColor(Color color)
    {
        this.penColor = color;
    }

    // START KGU#448 2017-10-28: Enh. #443
    //public void setAnimationDelay(int delay)
    /* (non-Javadoc)
     * @see lu.fisch.diagrcontrol.DelayableDiagramController#setAnimationDelay(int, boolean)
     */
    public void setAnimationDelay(int delay, boolean forceReinit)
    // END KGU#448 2017-10-28
    {
        if (forceReinit) {
            reinit();
        }
        this.delay = delay;
    }

    // START KGU#356 2019-03-02: Issue #366 - Allow focus control of he DiagramController copes with it
    /**
     * @return whether this TurtleBox window is focused
     */
    @Override
    public boolean isFocused()
    {
        return this.frame != null && this.frame.isFocused();
    }

    /**
     * Requests the focus for this TurtleBox window.
     */
    @Override
    public void requestFocus()
    {
        if (this.frame != null) {
            this.frame.requestFocus();
        }
    }
    // END KGU#356 2019-03-02
    

// START KGU#448/KGU#673 2020-12-11: Enh. #443 deprecated stuff disabled
//    @Deprecated
//    private String parseFunctionName(String str)
//    {
//        if (str.trim().indexOf("(")!=-1)
//            return str.trim().substring(0,str.trim().indexOf("(")).trim().toLowerCase();
//        else
//            return null;
//    }
//
//    @Deprecated
//    private String parseFunctionParam(String str, int count)
//    {
//    	String res = null;
//    	int posParen1 = (str = str.trim()).indexOf("(");
//    	if (posParen1 > -1)
//    	{
//    		String params = str.substring(posParen1+1, str.indexOf(")")).trim();
//    		if (!params.isEmpty())
//    		{
//    			String[] args = params.split(",");
//    			if (count < args.length) {
//    				res = args[count];
//    			}
//    		}
//    	}
//    	return res;
//    }
//
//    @Deprecated
//    private Double parseFunctionParamDouble(String str, int count)
//    {
//        String res = parseFunctionParam(str, count);
//        if( res == null || res.isEmpty() ) { return 0.0; }
//        return Double.valueOf(res);
//    }
//
//    @Deprecated
//    public String execute(String message, Color color)
//    {
//        setColorNonWhite(color);
//        return execute(message);
//    }
// END KGU#448/KGU#673 2020-12-11

    /**
     * Sets the given color except in case of WHITE where the {@link #defaultPenColor} is used instead.
     * Does not influence the default pen colour.
     * @param color - the specified colour (where WHITE means default)
     * @see #setColor(Color)
     * @see #setPenColor(Color)
     * @see #setBackground(Color)
     */
    private void setColorNonWhite(Color color) {
        if(color.equals(Color.WHITE))
        {
            // START KGU#303 2016-12-03: Enh. #302
            //this.setColor(Color.BLACK);
            this.setColor(defaultPenColor);
            // END KGU#303 2016-12-03
        }
        else this.setColor(color);
    }

// START KGU#448/KGU#673 2020-12-11: Enh. #443 deprecated stuff disabled
//    @Deprecated
//    public String execute(String message)
//    {
//        String name = parseFunctionName(message);
//        double param1 = parseFunctionParamDouble(message,0);
//        double param2 = parseFunctionParamDouble(message,1);
//        // START KGU#303 2016-12-02: Enh. #302
//        double param3 = parseFunctionParamDouble(message,2);
//        // END KGU#303 2016-12-02
//        String res = new String();
//        if(name!=null)
//        {
//            if (name.equals("init")) {
//// START KGU#303 2016-12-03: Issue #302 - replaced by reinit() call
////                elements.clear();
////                angle=-90;
////                // START KGU#303 2016-12-02: Enh. #302
////                backgroundColor = Color.WHITE;
////                // END KGU#3032016-12-02
////                // START KGU#303 2016-12-03: Enh. #302
////                defaultPenColor = Color.BLACK;
////                turtleHidden = false;
////                // END KGU#3032016-12-03
////                setPos(home.getLocation());
////                penDown();
////                reinit();
//// END KGU#303 2016-12-03
//                setAnimationDelay((int) param1, true);
//            }
//            // START #272 2016-10-16 (KGU): Now different types (to allow to study rounding behaviour)
//            //else if (name.equals("forward") || name.equals("fd")) { forward((int) param1); }
//            //else if (name.equals("backward") || name.equals("bk")) { backward((int) param1); }
//            else if (name.equals("forward")) { forward(param1); }
//            else if (name.equals("backward")) { backward(param1); }
//            else if (name.equals("fd")) { fd((int)param1); }
//            else if (name.equals("bk")) { bk((int)param1); }
//            // END #272 2016-10-16
//            // START KGU 20141007: Wrong type casting mended (led to rotation biases)
//            //else if (name.equals("left") || name.equals("rl")) { left((int) param1); }
//            //else if (name.equals("right") || name.equals("rr")) { right((int) param1); }
//            else if (name.equals("left") || name.equals("rl")) { left(param1); }
//            else if (name.equals("right") || name.equals("rr")) { right(param1); }
//            // END KGU 20141007
//            else if (name.equals("penup") || name.equals("up")) { penUp(); }
//            else if (name.equals("pendown") || name.equals("down")) { penDown(); }
//            else if (name.equals("gotoxy")) { gotoXY((int) param1, (int) param2); }
//            else if (name.equals("gotox")) { gotoX((int) param1); }
//            else if (name.equals("gotoy")) { gotoY((int) param1); }
//            else if (name.equals("hideturtle")) { hideTurtle(); }
//            else if (name.equals("showturtle")) { showTurtle(); }
//            // START KGU#303 2016-12-02: Enh. #302 - A procedure to set the backgroud colour was requested
//            else if (name.equals("setbackground")) { setBackgroundColor((int)Math.abs(param1),(int)Math.abs(param2),(int)Math.abs(param3)); }
//            else if (name.equals("setpencolor")) { setPenColor((int)Math.abs(param1),(int)Math.abs(param2),(int)Math.abs(param3)); }
//            // END KGU#303 2016-12-02
//            else { res="Procedure <"+name+"> not implemented!"; }
//        }
//        
//        return res;
//    }
// END KGU#448/KGU#673 2020-12-11

    // START KGU#448 2017-10-28: Enh. #443
    /* (non-Javadoc)
     * @see lu.fisch.diagrcontrol.DiagramController#getProcedureMap()
     */
    @Override
    public HashMap<String, Method> getProcedureMap() {
    	return definedProcedures;
    }
    // END KGU#448 2017-10-28

    // START KGU#417 2017-06-29: Enh. #424, #443 - Allow function getX(), getY(), getOrientation();
    /* (non-Javadoc)
     * @see lu.fisch.diagrcontrol.DiagramController#getFunctionMap()
     */
    public HashMap<String, Method> getFunctionMap()
    {
    	return definedFunctions;
    }
    
    // START KGU#448 2017-10-28: Enh. #417, #443 - support the generic execute method
    /**
     * Returns the current horizontal pixel coordinate.
     * @return the precise result of preceding moves, i.e. as double value
     */
    public double getX()
    {
    	return this.posX;
    }
    /**
     * Returns the current vertical pixel coordinate (from top downwards).
     * @return the precise result of preceding moves, i.e. as double value
     */
    public double getY()
    {
    	return this.posY;
    }
    // END KGU#448 2017-10-28

    // START KGU#566 2018-07-30: Enh. #576 API procedure allowing the user algorithm to wipe the box
    /**
     * Delayed API function to wipe the TurtleBox from all content
     */
    public void clear()
    {
    	this.elements.clear();
    	// START KGU#685 2020-12-14: Enh. #704
    	this.bounds = new Rectangle();
    	this.bounds.width = -1;
    	this.bounds.height = -1;
    	frame.displacement = null;
    	// END KGU#685 2020-12-14
    	this.delay();
    }
    // END KGU#566 2018-07-30
}

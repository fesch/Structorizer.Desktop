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
 *      Author:         Bob Fisch
 *
 *      Description:    This is the main application form.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-11      First Issue
 *      Kay Gürtzig     2015-10-18      Methods getRoot(), setRoot() introduced to ease Arranger handling (KGU#48)
 *      Kay Gürtzig     2015-10-30      Issue #6 fixed properly (see comment)
 *      Kay Gürtzig     2015-11-03      check_14 property added (For loop enhancement, #10 = KGU#3)
 *      Kay Gürtzig     2015-11-10      Issues #6 and #16 fixed by appropriate default window behaviour
 *      Kay Gürtzig     2015-11-14      Yet another improved approach to #6 / #16: see comment
 *      Kay Gürtzig     2015-11-24      KGU#88: The decision according to #6 / #16 is now returned on setRoot()
 *      Kay Gürtzig     2015-11-28      KGU#2/KGU#78/KGU#47: New checks 15, 16, and 17 registered for loading
 *      Kay Gürtzig     2015-12-04      KGU#95: Bugfix #42 - wrong default current directory mended
 *      Kay Gürtzig     2016-01-04      KGU#123: Bugfix #65 / Enh. #87 - New Ini property: mouse wheel mode
 *      Kay Gürtzig     2016-03-16      KGU#157: Bugfix #132 - Don't allow to close without having stopped Executor
 *      Kay Gürtzig     2016-03-18      KGU#89: Localization of Executor Control supported 
 *      Kay Gürtzig     2016-07-03      KGU#202: Localization of Arranger Surface supported
 *      Kay Gürtzig     2016-07-25      Issues #201, #202: Look-and-Feel propagation to Arranger and Executor
 *      Kay Gürtzig     2016-08-01      Enh. #128: new mode "Comments plus text" associated to Ini file
 *      Kay Gürtzig     2016-08-08      Issues #220, #224: Look-and Feel updates for Executor and Translator
 *      Kay Gürtzig     2016-09-09      Locales backwards compatibility precaution for release 3.25 in loadFromIni()
 *      Kay Gürtzig     2016-10-11      Enh. #267: New method updateAnalysis() introduced
 *      Kay Gürtzig     2016-11-01      Issue #81: Scale factor from Ini also applied to fonts
 *      Kay Gürtzig     2016-11-09      Issue #81: Scale factor no longer rounded except for icons, ensured to be >= 1
 *      Kay Gürtzig     2016-12-02      Enh. #300: Notification of disabled version retrieval or new versions
 *      Kay Gürtzig     2016-12-12      Enh. #305: API enhanced to support the Arranger Root index view
 *      Kay Gürtzig     2016-12-15      Enh. #310: New options for saving diagrams added
 *      Kay Gürtzig     2017-01-04      KGU#49: Closing a stand-alone instance now effectively warns Arranger
 *      Kay Gürtzig     2017-01-06      Issue #312: Measure against lost focus on start.
 *      Kay Gürtzig     2017-01-07      Enh. #101: Modified title string for dependent instances
 *      Kay Gürtzig     2017-01-15      Enh. #333: New potential preference "unicodeCompOps" added to Ini
 *      Kay Gürtzig     2017-02-03      Issue #340: Redundant calls of setLocale dropped
 *      Kay Gürtzig     2017-03-15      Enh. #300: turned retrieveVersion to static
 *      Kay Gürtzig     2017-10-06      Enh. #430: InputBox.FONT_SIZE now addressed in loadFromIni(), saveToIni()
 *      Kay Gürtzig     2017-11-05      Issue #452: Differentiated initial setting for Analyser preferences
 *      Kay Gürtzig     2017-11-06      Issue #455: Drastic measures against races on startup.
 *      Kay Gürtzig     2017-11-14      Bugfix #465: invokeAndWait must be suppressed if not standalone
 *      Kay Gürtzig     2018-01-21      Enh. #490: DiagramController aliases saved and loaded to/from Ini
 *                                      Issue #455: Multiple redrawing of diagram avoided in loadFromIni().
 *      Kay Gürtzig     2018-02-09      Bugfix #507 had revealed an event queue issue in loadFromIni() on
 *                                      loading  preferences from explicitly chosen ini file. This is fixed now
 *      Kay Gürtzig     2018-06-25      Issue #551.1: The msgUpdateInfoHint shouldn't be given on webstart
 *      Kay Gürtzig     2018-07-09      Bugfix #555: Failing restoration of the previous comment popup status
 *      Kay Gürtzig     2018-09-10      Issue #508: New option Element.E_PADDING_FIX in load/save INI
 *      Kay Gürtzig     2018-10-06      Issue #552: No need for serial action on closing if Arranger
 *                                      doesn't hold dirty diagrams
 *      Kay Gürtzig     2018-10-28      Enh. #419: loadFromIni() decomposed (diagram-related parts delegated)
 *      Kay Gürtzig     2018-12-21      Enh. #655 signature and semantics of method routinePoolChanged adapted 
 *      Kay Gürtzig     2019-01-17      Issue #664: Workaround for ambiguous canceling in AUTO_SAVE_ON_CLOSE mode
 *      Kay Gürtzig     2019-02-16      Enh. #682: Extended welcome menu with language choice
 *      Kay Gürtzig     2019-02-20      Issue #686: Improved the detection of the current Look and Feel
 *      Kay Gürtzig     2019-03-21      Enh. #707: Configurations for filename proposals
 *      Kay Gürtzig     2019-03-27      Enh. #717: Loading/saving of Element.E_WHEEL_SCROLL_UNIT
 *      Kay Gürtzig     2019-07-28      Issue KGU#715: isWebStart renamed to isAutoUpdating
 *      Kay Gürtzig     2019-08-03      Issue #733 Selective property export mechanism implemented.
 *      Bob Fisch       2019-08-04      Issue #537: OSXAdapter stuff introduced
 *      Kay Gürtzig     2019-09-10      Bugfix #744: OSX file handler hadn't been configured
 *      Kay Gürtzig     2019-09-16      #744 workaround: file open queue on startup for OS X
 *      Kay Gürtzig     2019-09-19      Bugfix #744: OSX configuration order changed: file handler first
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      2017-11-06 Drastic measures against races on start
 *      - If opened stand-alone (i.e. unless being opened from Arranger), races on startup of caused lots
 *        of NullPointerExceptions in the background and a diagram initially to be loaded looking contorted
 *        and bizarrely prolonged. So the steps on creation where put in invokeAndWait blocks.
 *      2015-11-14 New approach to solve the Window Closing problem (Kay Gürtzig, #6 = KGU#49 / #16 = KGU#66)
 *      - A new boolean field isStandalone (addressed by a new parameterized constructor) is introduced in
 *        order to decide whether to exit or only to dispose on Window Closing event. So if the Mainform is
 *        opened as a dependent frame, it should be opened as new Mainform(false) from now on. In this case
 *        it will only dispose when closing, otherwise it will exit. 
 *      2015-11-10 Window Closing problem (Kay Gürtzig, KGU#49/KGU#66)
 *      - Issues #6/#16 hadn't been solved in the intended way since the default action had still been
 *        EXIT_ON_CLOSE instead of just disposing.
 *      2015-10-30 (Kay Gürtzig)
 *      - if on closing the window the user cancels an option dialog asking him or her whether or not to save
 *        the diagram changes then the Mainform is to be prevented from closing. If the Mainform runs as
 *        a thread of the Arranger, however, this might not help a lot because it is going to be killed
 *        anyway if the Arranger was pushing the event.
 *
 ******************************************************************************************************///

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.locales.Translator;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.turtle.TurtleBox;
import lu.fisch.diagrcontrol.DiagramController;
import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.archivar.IRoutinePoolListener;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.locales.LangFrame;
import lu.fisch.structorizer.locales.Locales;

@SuppressWarnings("serial")
public class Mainform  extends LangFrame implements NSDController, IRoutinePoolListener
{
	// START KGU#484 2018-03-22: Issue #463
	public static final Logger logger = Logger.getLogger(Mainform.class.getName());
	// END KGU#484 2018-03-22
	public Diagram diagram = null;
	private Menu menu = null;
	private Editor editor = null;
	
	private String laf = null;
	// START KGU#300 2016-12-02: Enh. #300
	// The version for which information about update retrieval was last suppressed
	private String suppressUpdateHint = "";
	// END KGU#300 2016-12-02
	// START KGU#287 2017-01-11: Issue #81/#330
	private String preselectedScaleFactor = null;
	// END KGU#287 2017-01-11
	
	// START KGU#49/KGU#66 2015-11-14: This decides whether to exit or just to dispose when being closed
	private boolean isStandalone = true;	// The default is to exit...
	// END KGU#49/KGU#66 2015-11-14
	// START KGU#461/KGU#491 2018-02-09: Bugfix #455/#465/#507: We got into trouble on reloading the preferences
	private boolean isStartingUp = true;
	// END KGU#461/KGU#491 2018-02-09
	// START KGU#724 2019-09-16: Bugfix #744 Open file event queue for OS X
	public LinkedList<String> filesToOpen = null;
	// END KGU#724 2019-09-16
	
	// START KGU 2016-01-10: Enhancement #101: Show version number and stand-alone status in title
	private String titleString = "Structorizer " + Element.E_VERSION;
	// END KGU 2016-01-10
	// START KGU#326 2017-01-07: Enh. #101 - count the instances (for the title string)
	private static int instanceCount = 0;
	private int instanceNo;
	// END KGU#326 #2017-01-07
	// START KGU#456 2017-11-05: Enh. #452
	/** Indicates whether Structorizer may have been started the first time */
	boolean isNew = false;
	// END KGU#456 2017-11-05
	// START KGU#532 2018-06-25: To be able to suppress version hints on webstart (doesn't make sense)
	public boolean isAutoUpdating = false;
	// END KGU#532 2018-06-25
	
	// START KGU#655 2019-02-16: Enhanced welcome menu
	private JOptionPane panWelcome = null;
	private JTextArea txtWelcome1 = null, txtWelcome2 = null;
	private JToggleButton[] btnLangs = null;
	// END KGU#655 2019-02-16
		
	/******************************
 	 * Setup the Mainform
	 ******************************/
	private void create()
	{
		// START KGU#456 2017-11-05: Enh. #452
		//Ini.getInstance();
		isNew = Ini.getInstance().wasFirstStart();
		// END KGU#456 2017-11-05
		/*
		try {
			ClassPathHacker.addFile("Structorizer.app/Contents/Resources/Java/quaqua-filechooser-only.jar");
			UIManager.setLookAndFeel(
					"ch.randelshofer.quaqua.QuaquaLookAndFeel"
					);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}/**/

		/******************************
		 * Load values from INI
		 ******************************/
		// This is required at an early moment e.g. to ensure correct scaling etc.
		// But it doesn't reach components like Editor, Menu and Diagram, which are
		// created later
		loadFromINI();

		/******************************
		 * Some JFrame specific things
		 ******************************/
		// set window title
		// START KGU 2016-01-10: Enhancement #101 - show version number and standalone status
		//setTitle("Structorizer");
		// START KGU#326 2017-01-07: Enh. #101 improved title information
		//if (!this.isStandalone) titleString = "(" + titleString + ")";
		if (!this.isStandalone) titleString = "[" + this.instanceNo + "] " + titleString;
		// END KGU#326 2017-01-07
		setTitle(titleString);
		// END KGU 2016-01-10
		// set layout (OS default)
		setLayout(null);
		// set windows size
		//setSize(550, 550);
		// show form
		setVisible(true);
		// set action to perform if closed
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set icon depending on OS ;-)
		String os = System.getProperty("os.name").toLowerCase();
		setIconImage(IconLoader.getIcon(0).getImage());
		if (os.contains("windows")) 
		{
			setIconImage(IconLoader.getIcon(0).getImage());
		} 
		else if (os.contains("mac")) 
		{
			setIconImage(IconLoader.icoNSD.getImage());
		}

		/******************************
		 * Setup the editor
		 ******************************/
		//System.out.println("* Setup the editor ...");
		if (this.isStandalone) {	// KGU#461 2017-11-14: Bugfix #455/#465
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						editor = new Editor(Mainform.this);
					}
				});
			} catch (InvocationTargetException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.SEVERE, "Editor creation thread failed.", e1);
				// END KGU#484 2018-04-05
			} catch (InterruptedException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.SEVERE, "Editor creation thread failed.", e1);
				// END KGU#484 2018-04-05
			}
		}
		else {
			// Already in an event dispatcher thread
			editor = new Editor(Mainform.this);
		}
		//System.out.println("* editor done.");
		// get reference to the diagram
		diagram = getEditor().diagram;
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(getEditor(),BorderLayout.CENTER);

		/******************************
		 * Setup the menu
		 ******************************/
		//System.out.println("* Setup the menu ...");
		if (this.isStandalone) {	// KGU#461 2017-11-14: Bugfix #455/#465
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						menu = new Menu(diagram, Mainform.this);
					}
				});
			} catch (InvocationTargetException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.SEVERE, "Menu creation thread failed.", e1);
				// END KGU#484 2018-04-05
			} catch (InterruptedException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.SEVERE, "Menu creation thread failed.", e1);
				// END KGU#484 2018-04-05
			}
		}
		else {
			menu = new Menu(diagram, Mainform.this);
		}
		//System.out.println("* menu done.");
		setJMenuBar(menu);		

		/******************************
		 * Update the buttons and menu
		 ******************************/
		//System.out.println("* Update the buttons and menu ...");
		if (this.isStandalone) {	// KGU#461 2017-11-14: Bugfix #455/#465
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						doButtons();
					}
				});
			} catch (InvocationTargetException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.WARNING, "Button update failed.", e1);
				// END KGU#484 2018-04-05
			} catch (InterruptedException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.WARNING, "Button update failed.", e1);
				// END KGU#484 2018-04-05
			}
		}
		else {
			doButtons();
		}
		//System.out.println("* Buttons and menu done.");

		/******************************
		 * Set onClose event
		 ******************************/
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				// START KGU#157 2016-03-16: Bugfix #131 Never just close with a running executor!
				if (diagram.getRoot() != null && diagram.getRoot().executed && !isStandalone)
				{
					// This will pop up a dialog to stop the execution
					// By first argument set to null we avoid reopening the Executor Control
					Executor.getInstance(null, null);
					// Since the executor is a concurrent thread and we don't know the decision of
					// the user, we can neither wait nor proceed here. So we just leave.
				}
				else {
					// END KGU#157 2016-03-16
					// START KGU#634 2019-01-17: Issue #664 - diagram and user must be able to decide whether it's crucial
					try {
						diagram.isGoingToClose = true;
						// END KGU#634 2019-01-17
						if (diagram.saveNSD(!Element.E_AUTO_SAVE_ON_CLOSE))
						{
							// START KGU#287 2017-01-11: Issue #81/#330
							if (isStandalone) {
								if (Element.E_NEXT_SCALE_FACTOR <= 0) {	// pathologic value?
									Element.E_NEXT_SCALE_FACTOR = 1.0;
								}
								preselectedScaleFactor = Double.toString(Element.E_NEXT_SCALE_FACTOR);
							}
							// END KGU#287 2017-01-11
							saveToINI();
							// START KGU#49/KGU#66 (#6/#16) 2015-11-14: only EXIT if there are no owners
							if (isStandalone) {
								boolean vetoed = false;
								// START KGU#49 2017-01-04 Care for potential Arranger dependents
								if (Arranger.hasInstance()) {
									Diagram.startSerialMode();
									vetoed = !Arranger.getInstance().windowClosingVetoable(e);
									Diagram.endSerialMode();
								}
								// END KGU#49 2017-01-04
								if (!vetoed) {
									// START KGU#484 2018-03-22: Issue #463
									logger.info("Structorizer " + instanceNo + " shutting down.");
									// END KGU#484 2018-03-22
									System.exit(0);	// This kills all related frames and threads as well!
								}
							}
							else {
								// START KGU#484 2018-03-22: Issue #463
								logger.info("Structorizer " + instanceNo + " going to dispose.");
								// END KGU#484 2018-03-22
								dispose();
							}
							// END KGU#49/KGU#66 (#6/#16) 2015-11-14
						}
						// START KGU#634 2019-01-17
					}
					finally {
						diagram.isGoingToClose = false;
					}
					// END KGU#634 2019-01-17
					// START KGU#157 2016-03-16: Bugfix #131 part 2
				}
				// END KGU#157 2016-03-16
			}

			@Override
			public void windowOpened(WindowEvent e) 
			{  
				//editor.componentResized(null);
				//editor.revalidate();
				//repaint();
			}

			@Override
			public void windowActivated(WindowEvent e)
			{
				//editor.componentResized(null);
				//editor.revalidate();
				//repaint();
			}

			@Override
			public void windowGainedFocus(WindowEvent e) 
			{
				//editor.componentResized(null);
				//editor.revalidate();
				//repaint();
			}
		}); 

		/******************************
		 * Load values from INI
		 ******************************/
		// This has to be done a second time now, after all components were put in place
		//System.out.println("* Load from Ini ...");
		loadFromINI();
		//System.out.println("* Load from Ini done.");
		// START KGU#337 2017-02-03: Issue #340 - setLocale has already been done by loadFromIni()
		//Locales.getInstance().setLocale(Locales.getInstance().getLoadedLocaleName());
		// END KGU#337 2017-02-03

		/******************************
		 * Resize the toolbar
		 ******************************/
		//editor.componentResized(null);
		//System.out.println("* Revalidate editor ...");
		if (this.isStandalone) {	// KGU#461 2017-11-14: Bugfix #455/#465
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						getEditor().revalidate();
					}
				});
			} catch (InvocationTargetException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.WARNING, "Editor revaluation thread failed.", e1);
				// END KGU#484 2018-04-05
			} catch (InterruptedException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.WARNING, "Editor revalidation thread failed.", e1);
				// END KGU#484 2018-04-05
			}
		}
		else {
			getEditor().revalidate();
		}
		//System.out.println("* Repaint ...");
		repaint();
		//System.out.println("* Redraw ...");
		//System.out.println("* Revalidate editor ...");
		if (this.isStandalone) {	// KGU#461 2017-11-14: Bugfix #455/#465
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						diagram.setInitialized();
					}
				});
			} catch (InvocationTargetException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.WARNING, "Diagram initialization failed.", e1);
				// END KGU#484 2018-04-05
			} catch (InterruptedException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.WARNING, "Diagram initialization failed.", e1);
				// END KGU#484 2018-04-05
			}
		}
		else {
			diagram.setInitialized();
		}
		// START KGU#305 2016-12-16
		//System.out.println("* Update Arranger index ...");
		if (this.isStandalone) {	// KGU#461 2017-11-14: Bugfix #455/#465
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						// START KGU#626 2019-01-01: Enh. #657
						//getEditor().updateArrangerIndex(Arranger.getSortedRoots());
						getEditor().updateArrangerIndex(Arranger.getSortedGroups());
						// END KGU#626 2019-01-01
					}
				});
			} catch (InvocationTargetException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.WARNING, "Arranger index update failed.", e1);
				// END KGU#484 2018-04-05
			} catch (InterruptedException e1) {
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				logger.log(Level.WARNING, "Arranger index update failed.", e1);
				// END KGU#484 2018-04-05
			}
		}
		else {
			// START KGU#626 2019-01-01: Enh. #657
			//getEditor().updateArrangerIndex(Arranger.getSortedRoots());
			getEditor().updateArrangerIndex(Arranger.getSortedGroups());
			// END KGU#626 2019-01-01
		}
		//System.out.println("* Arranger index done.");
		// END KGU#305 2016-12-16
		// START KGU#325 2017-01-06: Issue #312 ensure the work area getting initial focus
		//System.out.println("* scrollarea.requestFocus ...");
		getEditor().scrollarea.requestFocusInWindow();
		// END KGU#325 2017-01-06

		// START KGU#461/KGU#491 2018-02-09: Bugfix #455/#465/#507: We got into trouble on reloading the preferences
		isStartingUp = false;
		// END KGU#461/KGU#491 2018-02-09
	}
	
	/******************************
	 * Load & save INI-file
	 ******************************/
	// START KGU#466 2019-08-02: Issue #733 - selective preferences export
	/**
	 * Returns the preference keys used in the ini file for the given {@code category}
	 * (if class {@code Mainform} is responsible for the saving and loading of the properties
	 * of this category. Currently, the following categories are supported here:
	 * <ul>
	 * <li>"diagram": Settings from the "Diagram" menu</li>
	 * <li>"saving": File saving options</li>
	 * </ul>
	 * @param category
	 * @return a String array containing the relevant keys for the ini file
	 * @see Element#getPreferenceKeys(String)
	 * @see Root#getPreferenceKeys()
	 * @see CodeParser#getPreferenceKeys()
	 */
	public static String[] getPreferenceKeys(String category)
	{
		if (category.equals("saving")) {
			return new String[] {"autoSaveOnExecute", "autoSaveOnClose", "makeBackups", "filenameWithArgNos",
					"filenameSigSeparator", "arrangerRelCoords"};
		}
		else if (category.equals("diagram")) {
			return new String[] {"showComments", "commentsPlusText", "switchTextComments", "varHightlight",
					"DIN", "hideDeclarations", "index", };
		}
		else if (category.equals("wheel")) {
			return new String[] {"wheel*"};
		}
		else if (category.equals("update")) {
			return new String[] {"retrieveVersion", "suppressUpdateHint"};
		}
		return new String[]{};
	}
	// END KGU#466 2019-08-02
	
	public void loadFromINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			ini.load();	// FIXME This seems to be repeated in order to buy time for the GUI (?)

			// ======================== GUI scaling ==========================
			double scaleFactor = Double.parseDouble(ini.getProperty("scaleFactor","1"));
			// START KGU#287 2017-01-09 
			if (scaleFactor <= 0.5) scaleFactor = 1.0;	// Pathologic value...
			//IconLoader.setScaleFactor(scaleFactor.intValue());
			IconLoader.setScaleFactor(scaleFactor);	// The IconLoader doesn't scale down anyway
			Element.E_NEXT_SCALE_FACTOR = scaleFactor;
			// END KGU#287 2017-01-09
			
			// ======================= position ==============================
			// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
			int defaultWidth = Double.valueOf(750 * scaleFactor).intValue();
			int defaultHeight = Double.valueOf(550 * scaleFactor).intValue();
			// END KGU#287 2016-11-01
			// position
			int top = Integer.parseInt(ini.getProperty("Top","0"));
			int left = Integer.parseInt(ini.getProperty("Left","0"));
			// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
			//int width = Integer.parseInt(ini.getProperty("Width","750"));
			//int height = Integer.valueOf(ini.getProperty("Height","550"));
			int width = Integer.parseInt(ini.getProperty("Width", ""+defaultWidth));
			int height = Integer.parseInt(ini.getProperty("Height", ""+defaultHeight));
			// END KGU#287 2016-11-01

			// reset to defaults if wrong values
			if (top<0) top=0;
			if (left<0) left=0;
			// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
			//if (width<=0) width=750;
			//if (height<=0) height=550;
			if (width <= 0) width = defaultWidth;
			if (height <= 0) height = defaultHeight;
			// END KGU#287 2016-11-01
			
			// START KGU#300 2016-12-02: Enh. #300
			suppressUpdateHint = ini.getProperty("suppressUpdateHint", "");
			// END KGU#300 2016-12-02
			
			// language	
			String localeFileName = ini.getProperty("Lang","en");
			// START temporary backwards compatibility precaution towards release 3.25
			if (localeFileName.equals("chs.txt")) localeFileName = "zh-cn.txt";
			else if (localeFileName.equalsIgnoreCase("cht.txt")) localeFileName = "zh-tw.txt";
			// END temporary backwards compatibility precaution towards release 3.25
			//System.out.println("* setLocale(" + localeFileName + ")");
			Locales.getInstance().setLocale(localeFileName);
			//System.out.println("* Locale is set.");
			// START KGU#479 2017-12-15: Enh. #492 - preparation for configurable element names
			Locales.getInstance().setLocale(ElementNames.getInstance());
			ElementNames.getFromIni(ini);
			// END KGU#479 2017-12-15
			
			// colors
			Element.loadFromINI();
			updateColors();

			// parser
			CodeParser.loadFromINI();

			// look & feel
			laf = ini.getProperty("laf","Mac OS X");
			//System.out.println("* setLookAndFeel(" + laf + ")");
			setLookAndFeel(laf);
			//System.out.println("* LookAndFeel is set.");
			// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
			//System.out.println("* scaleDefaultFontSize(" + scaleFactor + ")");
			GUIScaler.scaleDefaultFontSize(scaleFactor);
			//System.out.println("* defaultFontSize is scaled.");
			// END KGU#287 2016-11-01
			
			// size
			setPreferredSize(new Dimension(width,height));
			setSize(width,height);
			setLocation(new Point(top,left));
			validate();

			// START KGU#123 2018-03-14: Enh. #87, Bugfix #65
			Element.E_WHEELCOLLAPSE = ini.getProperty("wheelToCollapse", "0").equals("1");
			// END KGU#123 2018-03-14
			// START KGU#503 2018-03-14: Enh. #519
			Element.E_WHEEL_REVERSE_ZOOM = ini.getProperty("wheelCtrlReverse", "0").equals("1");
			// END KGU#503 2018-03-14
			// START KGU#699 2019-03-27: Enh. #717
			Element.E_WHEEL_SCROLL_UNIT = Integer.parseInt(ini.getProperty("wheelScrollUnit", "0"));
			// END KGU#699 2019-03-27
			// START KGU#494 2018-09-10: Issue #508
			Element.E_PADDING_FIX = ini.getProperty("fixPadding", "0").equals("1");
			// END KGU#494 2018-09-10

			// START KGU#300 2016-12-02: Enh. #300
			Diagram.retrieveVersion = ini.getProperty("retrieveVersion", "false").equals("true");
			// END KGU#300 2016-12-02

			
			// Analyser (see also Root.saveToIni())
			// START KGU#239 2016-08-12: Code redesign
			for (int i = 1; i <= Root.numberOfChecks(); i++)
			{
				// START KGU#456 2017-11-05: Issue #452 - use initial Root.analyserChecks as defaults 
				//Root.setCheck(i, ini.getProperty("check" + i, "1").equals("1"));
				Root.setCheck(i, ini.getProperty("check" + i, Root.check(i) ? "1" : "0").equals("1"));
				// END KGU#456 2017-11-05
			}
			// END KGU#2/KGU#78 2016-08-12
			
			if (diagram != null) 
			{
				// ======= current directories, recent files, find settings ======
				// START KGU#602 2018-10-28: Let diagram fetch its properties itself
				diagram.fetchIniProperties(ini);
				// END KGU#602 2018-10-28
				
				// ==================== diagram menu settings ====================
				// DIN 66261
				if (ini.getProperty("DIN","0").equals("1")) // default = 0
				{
					//diagram.setDIN();
					Element.E_DIN = true;
				}
				// comments
				if (ini.getProperty("showComments","1").equals("0")) // default = 1
				{
					//diagram.setComments(false);
					// START KGU#549 2018-07-09: Bugfix #555 - the mode of the last session wasn't restored anymore 
					//Element.E_SHOWCOMMENTS = true;
					Element.E_SHOWCOMMENTS = false;
					// END KGU#549 2018-07-09
				}
				// START KGU#227 2016-08-01: Enh. #128
				//diagram.setCommentsPlusText(ini.getProperty("commentsPlusText","0").equals("1"));	// default = 0
				Element.E_COMMENTSPLUSTEXT = ini.getProperty("commentsPlusText","0").equals("1");	// default = 0
				// END KGU#227 2016-08-01
				if (ini.getProperty("switchTextComments","0").equals("1")) // default = 0
				{
					//diagram.setToggleTC(true);
					Element.E_TOGGLETC = true;
				}
				// syntax highlighting
				if (ini.getProperty("varHightlight","1").equals("1")) // default = 0
				{
					//diagram.setHightlightVars(true);
					Element.E_VARHIGHLIGHT = true;	// this is now directly used for drawing
					diagram.resetDrawingInfo();
				}
				// START KGU#477 2017-12-06: Enh. #487
				//diagram.setHideDeclarations(ini.getProperty("hideDeclarations","0").equals("1"));	// default = 0
				Element.E_HIDE_DECL = ini.getProperty("hideDeclarations","0").equals("1");	// default = 0
				// END KGU#227 2017-12-06
				// analyser
				// KGU 2016-07-27: Analyser should by default be switched on. See Issue #207
				/*
				if (ini.getProperty("analyser","0").equals("0")) // default = 0
				{
					diagram.setAnalyser(false);
				}
				/**/
				
				// START KGU#480 2018-01-21: Enh. #490
				if (Element.controllerName2Alias.isEmpty()) {
					for (DiagramController controller: diagram.getDiagramControllers()) {
						if (controller == null) {
							controller = new TurtleBox();
						}
						String className = controller.getClass().getName();
						for (Entry<String, java.lang.reflect.Method> entry: controller.getProcedureMap().entrySet()) {
							String sign = entry.getKey();
							String name = entry.getValue().getName();
							String[] parts = sign.split("#");
							if (!name.equalsIgnoreCase(parts[0])) {
								name = parts[0];
							}
							String alias = ini.getProperty(className + "." + sign, "").trim();
							if (!alias.isEmpty()) {
								Element.controllerName2Alias.put(sign, alias);
								Element.controllerAlias2Name.put(alias.toLowerCase() + "#" + parts[1], name);
							}
						}
					}
					if (ini.getProperty("applyAliases", "0").equals("1")) // default = 0
					{
						//diagram.setApplyAliases(true);
						Element.E_APPLY_ALIASES = true;
					}
				}
				// END KGU#480 2018-01-18
				// START KGU#305 2016-12-14: Enh. #305
				//System.out.println("* setArrangerIndex() ...");
				diagram.setArrangerIndex(ini.getProperty("index", "1").equals("1"));	// default = 1
				//System.out.println("* ArrangerIndex is set.");
				// END KGU#305 2016-12-14
				// START KGU#456 2017-11-05: Issue #452
				diagram.setSimplifiedGUI(ini.getProperty("userSkillLevel", "1").equals("0"));
				// END KGU#452 2017-11-05
				
				// START KGU#461/KGU#491 2018-02-09: Bugfix #455/#465/#507: We got into trouble on reloading the preferences
				//if (this.isStandalone) {	// KGU#461 2017-11-14: Bugfix #455/#465
				if (this.isStandalone && this.isStartingUp) {
				// END KGU#461/KGU#491 2018-02-09
					try {
						EventQueue.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								doButtons();
								diagram.analyse();
								diagram.resetDrawingInfo();
								diagram.redraw();
							}
						});
					} catch (InvocationTargetException e1) {
						// START KGU#484 2018-04-05: Issue #463
						//e1.printStackTrace();
						logger.log(Level.WARNING, "Diagram update failed.", e1);
						// END KGU#484 2018-04-05
					} catch (InterruptedException e1) {
						// START KGU#484 2018-04-05: Issue #463
						//e1.printStackTrace();
						logger.log(Level.WARNING, "Diagram index update failed.", e1);
						// END KGU#484 2018-04-05
					}
				}
				else {
					// Already in an event dispatcher thread
					this.doButtons();
					diagram.analyse();
					diagram.resetDrawingInfo();
					diagram.redraw();
				}
			}

			// START KGU#309 2016-12-15: Enh. #310 new saving options
			Element.E_AUTO_SAVE_ON_EXECUTE = ini.getProperty("autoSaveOnExecute", "0").equals("1");
			Element.E_AUTO_SAVE_ON_CLOSE = ini.getProperty("autoSaveOnClose", "0").equals("1");
			Element.E_MAKE_BACKUPS = ini.getProperty("makeBackups", "1").equals("1");
			// END KGU#309 20161-12-15
			// START KGU#690 2019-03-21: Issue #707 - new saving options
			Element.E_FILENAME_WITH_ARGNUMBERS = !ini.getProperty("filenameWithArgNos", "1").equals("0");
			String filenameSepa = ini.getProperty("filenameSigSeparator", "-");
			Element.E_FILENAME_SIG_SEPARATOR = filenameSepa.isEmpty() ? '-' : filenameSepa.charAt(0);
			// END KGU#690 2019-03-21
			
			// START KGU#630 2019-01-13: Enh. #662/4
			Arranger.A_STORE_RELATIVE_COORDS = ini.getProperty("arrangerRelCoords", "0").equals("1");
			// END KGU#630 2019-01-13
			
			// START KGU#331 2017-01-15: Enh. #333 Comparison operator display
			Element.E_SHOW_UNICODE_OPERATORS = ini.getProperty("unicodeCompOps", "1").equals("1");
			// END KGU#331 2017-01-15
			
			// START KGU#428 2017-10-06: Enh. #430
			InputBox.FONT_SIZE = Float.parseFloat(ini.getProperty("editorFontSize", "0"));
			// END KGU#428 2017-10-06
			
			// KGU#602 2018-10-28: Fetching of recent file paths outsourced to Diagram.fetchIniProperties()
			
			doButtons();
		}
		catch (Exception e) 
		{
			logger.log(Level.WARNING, "Ini", e);

			setPreferredSize(new Dimension(500,500));
			setSize(500,500);
			setLocation(new Point(0,0));
			validate();
		}
	}


	public void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();

			// ======================= position ==============================
			ini.setProperty("Top",Integer.toString(getLocationOnScreen().x));
			ini.setProperty("Left",Integer.toString(getLocationOnScreen().y));
			ini.setProperty("Width",Integer.toString(getWidth()));
			ini.setProperty("Height",Integer.toString(getHeight()));

			// ======= current directories, recent files, find settings ======
			if (diagram != null)
			{
				// START KGU#324 2017-06-16: Enh. #415 Let diagram cache it itself
//				if(diagram.currentDirectory!=null)
//				{
//					ini.setProperty("currentDirectory", diagram.currentDirectory.getAbsolutePath());
//					// START KGU#354 2071-04-26: Enh. #354 Also retain the other directories
//					ini.setProperty("lastExportDirectory", diagram.lastCodeExportDir.getAbsolutePath());
//					ini.setProperty("lastImportDirectory", diagram.lastCodeImportDir.getAbsolutePath());
//					ini.setProperty("lastImportFilter", diagram.lastImportFilter);
//					// END KGU#354 2017-04-26
//				}
//				// START KGU#305 2016-12-15: Enh. #305
//				ini.setProperty("index", (diagram.showArrangerIndex() ? "1" : "0"));
//				// END KGU#305 2016-12-15
				diagram.cacheIniProperties(ini);
				// END KGU#324 2017-06-16
			}
			
			// ======================= update control ========================
			// START KGU#300 2016-12-02: Enh. #300
			ini.setProperty("retrieveVersion", Boolean.toString(Diagram.retrieveVersion));
			// END KGU#300 2016-12-02
			// START KGU#300 2016-12-02: Enh. #300
			// Update hint suppression
			ini.setProperty("suppressUpdateHint", this.suppressUpdateHint);
			// END KGU#300 2016-12-02
		
			// =========================== language ==========================
			ini.setProperty("Lang",Locales.getInstance().getLoadedLocaleFilename());
			
			// ==================== diagram menu settings ====================
			// DIN, comments
			ini.setProperty("DIN", (Element.E_DIN ? "1" : "0"));
			ini.setProperty("showComments", (Element.E_SHOWCOMMENTS ? "1" : "0"));
			// START KGU#227 2016-08-01: Enh. #128
			ini.setProperty("commentsPlusText", Element.E_COMMENTSPLUSTEXT ? "1" : "0");
			// END KGU#227 2016-08-01
			ini.setProperty("switchTextComments", (Element.E_TOGGLETC ? "1" : "0"));
			ini.setProperty("varHightlight", (Element.E_VARHIGHLIGHT ? "1" : "0"));
			// START KGU#477 2017-12-06: Enh. #487
			ini.setProperty("hideDeclarations", Element.E_HIDE_DECL ? "1" : "0");
			// END KGU#227 2016-12-06
			// KGU 2016-07-27: Analyser should by default be switched on. See Issue #207
			//ini.setProperty("analyser", (Element.E_ANALYSER ? "1" : "0"));
			// START KGU#456 2017-11-05: Issue #452
			ini.setProperty("userSkillLevel", (Element.E_REDUCED_TOOLBARS ? "0" : "1"));
			// END KGU#456 2017-11-05
			
			// ====================== controller aliases =====================
			// START KGU#480 2018-01-21: Enh. #490
			ini.setProperty("applyAliases", Element.E_APPLY_ALIASES ? "1" : "0");
			// END KGU#480 2018-01-21
			
			// ========================== wheel ==============================
			// START KGU#123 2016-01-04: Enh. #87
			ini.setProperty("wheelToCollapse", (Element.E_WHEELCOLLAPSE ? "1" : "0"));
			// END KGU#123 2016-01-04
			// START KGU#503 2018-03-14: Enh. #519
			ini.setProperty("wheelCtrlReverse", (Element.E_WHEEL_REVERSE_ZOOM ? "1" : "0"));
			// END KGU#503 2018-03-14
			// START KGU#699 2019-03-27: Enh. #717
			ini.setProperty("wheelScrollUnit", Integer.toString(Element.E_WHEEL_SCROLL_UNIT));
			// END KGU#699 2019-03-27

			// ========================== fonts ==============================
			// START KGU#494 2018-09-10: Issue #508
			ini.setProperty("fixPadding", (Element.E_PADDING_FIX ? "1" : "0"));
			// END KGU#494 2018-09-10
			// START KGU#331 2017-01-15: Enh. #333 Comparison operator display
			ini.setProperty("unicodeCompOps", (Element.E_SHOW_UNICODE_OPERATORS ? "1" : "0"));
			// END KGU#331 2017-01-15
			// START KGU#428 2017-10-06: Enh. #430
			if (InputBox.FONT_SIZE > 0) {
				ini.setProperty("editorFontSize", Float.toString(InputBox.FONT_SIZE));
			}
			// END KGU#428 2017-10-06
			
			// ======================= saving options ========================
			// START KGU#309 2016-12-15: Enh. #310 new saving options
			ini.setProperty("autoSaveOnExecute", (Element.E_AUTO_SAVE_ON_EXECUTE ? "1" : "0"));
			ini.setProperty("autoSaveOnClose", (Element.E_AUTO_SAVE_ON_CLOSE ? "1" : "0"));
			ini.setProperty("makeBackups", (Element.E_MAKE_BACKUPS ? "1" : "0"));
			// END KGU#309 20161-12-15
			// START KGU#690 2019-03-21: Issue #707 - new saving options
			ini.setProperty("filenameWithArgNos", (Element.E_FILENAME_WITH_ARGNUMBERS ? "1" : "0"));
			ini.setProperty("filenameSigSeparator", Character.toString(Element.E_FILENAME_SIG_SEPARATOR));
			// END KGU#690 2019-03-21

			// ======================= look and feel =========================
			if (laf != null)
			{
				ini.setProperty("laf", laf);
			}
			
			// ======================== GUI scaling ==========================
			// START KGU#287 2017-01-11: Issue #81/#330
			if (this.preselectedScaleFactor != null) {
				ini.setProperty("scaleFactor", this.preselectedScaleFactor);
			}
			// END KGU#287 2017-01-11
			


			// START KGU#324 2017-06-16: Enh. #415: Now done by diagram.cacheIniProperties(ini) above
//			// recent files
//			if (diagram!=null)
//			{
//				if (diagram.recentFiles.size()!=0)
//				{
//					for(int i=0;i<diagram.recentFiles.size();i++)
//					{
//						//System.out.println(i);
//						ini.setProperty("recent"+String.valueOf(i),(String) diagram.recentFiles.get(i));
//					} 
//				}
//			}
			// END KGU#324 2017-06-16
			
			// START KGU#479 2017-12-15: Enh. #492
			ElementNames.putToIni(ini);
			// END KGU#479 2017-12-15
			
			// START KGU#497 2018-02-17: Enh. #512
			// Give the Arranger a chance to add its stuff to the properties to be saved 
			if (Arranger.hasInstance()) {
				Arranger.getInstance().updateProperties(ini);
			}
			// END KGU#497 2018-02-17
			
			ini.save();
		}
		catch (Exception e) 
		{
			logger.log(Level.WARNING, "Ini", e);
		}
	}
	
	/******************************
	 * This method dispatches the
	 * command to all sublisteners
	 ******************************/
	public void doButtons()
	{
		if(menu!=null)
		{
			menu.doButtonsLocal();
		}
		
		if (getEditor()!=null)
		{
			getEditor().doButtonsLocal();
		}
		
		doButtonsLocal();
	}
	
	public String getLookAndFeel()
	{
		return laf;
	}
	
	public void setLookAndFeel(String _laf)
	{
		if (_laf != null)
		{
			//System.out.println("Setting: "+_laf);
			// START KGU#661 2019-02-20: Issue #686 Detect current L&F (if _laf fails)
			//laf=_laf;
			LookAndFeel currentLaf = UIManager.getLookAndFeel();
			String currentLafName = currentLaf.getName();
			// END KGU#661 2019-02-20
			
			UIManager.LookAndFeelInfo plafs[] = UIManager.getInstalledLookAndFeels();
			for(int j = 0; j < plafs.length; ++j)
			{
				//System.out.println("Listing: "+plafs[j].getName());
				if (_laf.equals(plafs[j].getName()))
				{
					//System.out.println("Found: "+plafs[j].getName());
					try
					{
						UIManager.setLookAndFeel(plafs[j].getClassName());
						// START KGU#661 2019-02-20: Issue #686
						laf = _laf;
						// END KGU#661 2019-02-20
						SwingUtilities.updateComponentTreeUI(this);
						// START KGU#211/KGU#646 2016-07-25/2019-02-05: Issue #202, #674 - Propagation to Arranger
						Arranger.updateLookAndFeel();
						// END KGU#211/KGU#646 2016-07-25/2019-02-05
						// START KGU#210 2016-08-08: Issue #201 - Propagation to Executor Control
						Executor.updateLookAndFeel();
						// END KGU#210 2016-08-08
						// START KGU#233 2016-08-08: Issue #220
						Translator.updateLookAndFeel();
						// END KGU#233 2016-08-08
						// START #324 2017-06-16: Enh. #415 - we let diagram update the find&replace dialog
						if (diagram != null) {
							diagram.updateLookAndFeel();
						}
						// END KGU #324 2017-06-16
						// START KGU#661 2019-02-20: Issue #686
						return;
						// END KGU#661 2019-02-20
					}
					catch (Exception e)
					{
						// show error
						JOptionPane.showOptionDialog(null,e.getMessage(),
													 "Error ...",
													 JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,null,null,null);
					}
				}
				// START KGU#661 2019-02-20: Issue #686
				else if (plafs[j].getClassName().equals(currentLaf.getClass().getName())) {
					currentLafName = plafs[j].getName();
				}
				// END KGU#661 2019-02-20
			}
			// START KGU#661 2019-02-20: Issue #686
			laf = currentLafName;
			// END KGU#661 2019-02-20
		}
	}
	
	@Override
	public void savePreferences()
	{
		//System.out.println("Saving");
		saveToINI();
		CodeParser.saveToINI();
		Element.saveToINI();
		Root.saveToINI();
	}

	/******************************
	 * Local listener (empty)
	 ******************************/
	@Override
	public void doButtonsLocal()
	{
		boolean done=false;
		if (diagram != null)
		{
			Root root = diagram.getRoot();
			if (root != null && root.filename != null && !root.filename.isEmpty())
			{
				File f = new File(root.filename);
				// START KGU 2016-01-10: Enhancement #101 - involve version number and stand-alone status
				//this.setTitle("Structorizer - " + f.getName());
				this.setTitle(this.titleString + " - " + f.getName());
				// END KGU 2016-01-10
				done = true;
			}
		}
		// START KGU 2016-01-10: Enhancement #101 - involve version number and stand-alone status
		//if (!done) this.setTitle("Structorizer");
		if (!done) this.setTitle(this.titleString);
		// END KGU 2016-01-10
	}

	@Override
	public void updateColors() 
	{
		if(editor!=null)
			editor.updateColors();
	}
		
	/******************************
	 * Constructor
	 ******************************/
	// START KGU#49/KGU#66 2015-11-14: We want to distinguish whether we may exit on close
//	public Mainform()
//	{
//		super();
//		create();
//	}

	public Mainform()
	{
		this(true);
	}

	public Mainform(boolean standalone)
	{
		super();
		// START KGU#326 2017-01-07: Enh. #101 improved title information
		this.instanceNo = ++instanceCount;
		// END KGU#326 2017-01-07
		this.isStandalone = standalone;
		// START KGU#484 2018-03-22: Issue #463
		logger.info("Structorizer " + this.instanceNo + " starting up.");
		// END KGU#484 2018-03-22
		// START KGU#305 2016-12-16: Code revision
		Arranger.addToChangeListeners(this);
		// END KGU#305 2016-12-16
		create();
	}
	// END KGU#49/KGU#66

	/**
	 * @return the editor
	 */
	public Editor getEditor()
	{
		return editor;
	}

	public JFrame getFrame()
	{
		return this;
	}

	public Root getRoot()
	{
		if (this.diagram == null)
			return null;

		return this.diagram.getRoot();
	}

	public boolean setRoot(Root root)
	{
		boolean done = false;
		if (this.diagram != null)	// May look somewhat paranoid, but diagram is public...
		{
			// KGU#88: We now reflect if the user refuses to override the former diagram
			done = this.diagram.setRootIfNotRunning(root);
		}
		return done;
	}

	// START KGU#278 2016-10-11: Enh. #267 - Allows updates from Subroutine pools
	public void updateAnalysis()
	{
		if (this.diagram != null)
		{
			this.diagram.analyse();
		}
	}
	// END KGU#278 2016-10-11

	// START KGU#300 2016-12-02: Enh. #300 (KGU#456 2017-11-06: renamed for enh. #452)
	public void popupWelcomePane()
	{
		// START KGU#456 2017-11-06: Enh. #452
		//if (!Ini.getInstance().getProperty("retrieveVersion", "false").equals("true")) {
		if (this.isNew) {
			// START KGU#655 2019-02-16: Enh. #682 Introduction of a language choice mechanism
//			int chosen = JOptionPane.showOptionDialog(this,
//					Menu.msgWelcomeMessage.getText().replace("%", AnalyserPreferences.getCheckTabAndDescription(26)[1]),
//					Menu.lblHint.getText(),
//					JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
//					IconLoader.getIcon(24),
//					new String[]{Menu.lblReduced.getText(), Menu.lblNormal.getText()}, Menu.lblNormal.getText());
//			if (chosen == JOptionPane.OK_OPTION) {
			Box outerBox = new Box(BoxLayout.Y_AXIS);
			Box innerBox = new Box(BoxLayout.X_AXIS);
			String[] menuPath = {"menuFile", "menuFileTranslator"};
			String[] defaultNames = {"File", "Translator"};
			String[] repl = Menu.getLocalizedMenuPath(menuPath, defaultNames);
			txtWelcome1 = new JTextArea(Menu.msgWelcomeMessage1.getText().replace("%1", repl[0]).replace("%2", repl[1]));
			outerBox.add(txtWelcome1);
			btnLangs = new JToggleButton[Locales.LOCALES_LIST.length];
			String currLocale = Locales.getInstance().getLoadedLocaleName();
			for (int iLoc = 0; iLoc < Locales.LOCALES_LIST.length; iLoc++)
			{
				final String locName = Locales.LOCALES_LIST[iLoc][0];
				String locDescription = Locales.LOCALES_LIST[iLoc][1];
				if (locDescription != null)
				{
					ImageIcon icon = IconLoader.getLocaleIconImage(locName);
					btnLangs[iLoc] = new JToggleButton(icon);
					btnLangs[iLoc].setToolTipText(locDescription);
					if (locName.equals(currLocale)) {
						btnLangs[iLoc].setSelected(true);
					}
					btnLangs[iLoc].addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) {
						Object btn = event.getSource();
						if (btn instanceof JToggleButton && !((JToggleButton) btn).isSelected()) {
							System.out.println("unselected");
							return;
						}
						for (int i = 0; i < btnLangs.length; i++) {
							if (btnLangs[i] != null && btnLangs[i] != btn) {
								btnLangs[i].setSelected(false);
							}
						}
						chooseLang(locName); 
					}
					} );
					innerBox.add(btnLangs[iLoc]);
				}
			}
			outerBox.add(innerBox);
			outerBox.add(Box.createVerticalStrut((int)(10 * Element.E_NEXT_SCALE_FACTOR)));
			txtWelcome2 = new JTextArea(Menu.msgWelcomeMessage2.getText().replace("%", AnalyserPreferences.getCheckTabAndDescription(26)[1]));
			outerBox.add(txtWelcome2);
			txtWelcome1.setEditable(false);
			txtWelcome2.setEditable(false);
			String[] options = new String[]{Menu.lblReduced.getText(), Menu.lblNormal.getText()};
			panWelcome = new JOptionPane(outerBox,
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
					IconLoader.getIcon(24),
					options,
					options[1]);
			JDialog dialog = panWelcome.createDialog(this, Menu.lblHint.getText());
			dialog.setVisible(true);
			Object chosen = panWelcome.getValue();
			// Options may have been replaced by language change!
			Object[] objects = panWelcome.getOptions();
			if (objects[0].equals(chosen)) {
			// END KGU#655 2019-02-16
				Root.setCheck(26, true);
				Root.setCheck(25, true);
				if (diagram != null) {
					diagram.setSimplifiedGUI(true);
				}
				else {
					// The essence of diagram.setSimplifiedGUI() but without immediate visibility switch
					Element.E_REDUCED_TOOLBARS = true;
				}
			}
		}
		else if (!Ini.getInstance().getProperty("retrieveVersion", "false").equals("true")) {
			// END KGU#456 2017-11-06
			// START KGU#532 2018-06-25: In a webstart environment the message doesn't make sense
			//if (!Element.E_VERSION.equals(this.suppressUpdateHint)) {
			if (!isAutoUpdating && !Element.E_VERSION.equals(this.suppressUpdateHint)) {    	    		
				// END KGU#532 2018-06-25
				int chosen = JOptionPane.showOptionDialog(this,
						Menu.msgUpdateInfoHint.getText().replace("%1", this.menu.menuPreferences.getText()).replace("%2", this.menu.menuPreferencesNotifyUpdate.getText()),
						Menu.lblHint.getText(),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
						null,
						new String[]{Menu.lblOk.getText(), Menu.lblSuppressUpdateHint.getText()}, Menu.lblOk.getText());
				if (chosen != JOptionPane.OK_OPTION) {
					this.suppressUpdateHint = Element.E_VERSION;
				}
			}
		}
		else if (diagram != null) {
			diagram.updateNSD(false);
		}
		// START KGU#459 2017-11-20: Enh. #459-1
		if (diagram != null) {
			diagram.updateTutorialQueues();
			if (diagram.getRoot().startNextTutorial(false) > -1) {
				diagram.showTutorialHint();
			}
		}
		// END KGU#459 2017-11-20
	}
	// END KGU#300 2016-12-02

	// START KGU#655 2019-02-16: Enh. #682
	public void chooseLang(String localeName)
	{
		Locales.getInstance().setLocale((Component)ElementNames.getInstance(), localeName);
		// Better reset the use of personally configured names on changing the language
		ElementNames.useConfiguredNames = false;
		Locales.getInstance().setLocale(localeName);
		// Unfortunately the JOptionPane cannot be forced in any way to adapt its outer size to the new
		// textArea bounds. So we will have to tune the welcome texts such that the text bounds won't
		// change.
		String[] menuPath = {"menuFile", "menuFileTranslator"};
		String[] defaultNames = {"File", "Translator"};
		String[] repl = Menu.getLocalizedMenuPath(menuPath, defaultNames);
		txtWelcome1.setText(Menu.msgWelcomeMessage1.getText().replace("%1", repl[0]).replace("%2", repl[1]));
		txtWelcome2.setText(Menu.msgWelcomeMessage2.getText().replace("%", AnalyserPreferences.getCheckTabAndDescription(26)[1]));
		Component comp = txtWelcome1;
		while (comp != null) {
			if (comp instanceof JOptionPane) {
				String[] options = new String[]{Menu.lblReduced.getText(), Menu.lblNormal.getText()};
				((JOptionPane)comp).setOptions(options);
				((JOptionPane)comp).setInitialValue(options[1]);
				comp = null;
			}
			else {
				comp = comp.getParent();
			}
		}
		if (diagram != null) {
			diagram.analyse();
		}
		doButtons();	// This ensures the correct language menu item i the preferences menu gets selected
	}
	// END KGU#655 2019-02-16

	public boolean isStandalone()
	{
		return this.isStandalone;
	}

	// START KGU#305 2016-12-16: Code revision
	@Override
	public void routinePoolChanged(IRoutinePool _source, int _flags) {
		if (_source instanceof Arranger && this.editor != null) {
			if ((_flags & IRoutinePoolListener.RPC_POOL_CHANGED) != 0) {
				// START KGU#626 2019-01-01: Enh. #657
				//this.editor.updateArrangerIndex(Arranger.getSortedRoots());
				this.editor.updateArrangerIndex(Arranger.getSortedGroups());
				// END KGU#626 2019-01-01
			} else if ((_flags & (IRoutinePoolListener.RPC_POSITIONS_CHANGED | IRoutinePoolListener.RPC_GROUP_COLOR_CHANGED)) != 0) {
				this.editor.repaintArrangerIndex();
			}
			// START KGU#701 2019-03-30: Issue #718
			diagram.invalidateAndRedraw();
			// END KGU#701 2019-03-30
		}
		updateAnalysis();
	}
	// END KGU#305 2016-12-16

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

	// START KGU#679 2019-03-12: Enh. #698 - we needed a way for Arranger to inform about recently loaded or saved arrangement files
	/**
	 * Registers the given {@code file} as recently used. Interface for other modules like
	 * {@link Arranger} to inform about used files, e.g. arrangements.
	 * @param file - a file interpretable for Structorizer and just loaded or saved or otherwise used
	 */
	public void addRecentFile(File file) {
		if (diagram != null) {
			diagram.addRecentFile(file.getAbsolutePath());
		}
	}
	// END KGU#679 2019-03-12

	public void doOSX() {
		try {
			// Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
			// use as delegates for various com.apple.eawt.ApplicationListener methods
			
			// Issue #744: The file handler must be the first handler to be established! Otherwise the
			// event of the double-clicked file that led to launching Structorizer might slip through!
			OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadFile", new Class[]{String.class}));
			OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
			OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
			OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
			OSXAdapter.setDockIconImage(getIconImage());

			logger.info("OS X handlers established.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "Failed to establish OS X handlers", e);
		}
	}

	// General file handler; fed to the OSXAdapter as the method to call when 
	// a file associated to Structorizer is double-clicked or dragged onto it:
	public void loadFile(String filePath) {
		// START KGU#724 2019-09-16: Issue #744 (workaround for hazards on startup, may no longer be necessary)
		if (filePath == null || filePath.isEmpty()) {
			return;
		}
		if (diagram == null || this.isStartingUp) {
			// Lazy initialization
			logger.info("openFile event with path \"" + filePath + "\" postponed.");
			if (this.filesToOpen == null) {
				this.filesToOpen = new LinkedList<String>();
			}
			filesToOpen.addLast(filePath);	// push the file path to the queue
			// Nothing more to do here at the moment
			return;
		}
		// If files had already been queued then first try to load these
		else if (this.filesToOpen != null) {
			String lastExt = "";
			while (!this.filesToOpen.isEmpty()) {
				String queuedPath = this.filesToOpen.removeFirst();
				if (lastExt.equals("nsd") && queuedPath.toLowerCase().endsWith(".nsd")) {
					diagram.arrangeNSD();
				}
				logger.info("Handling postponed openFile event with path \"" + filePath + "\" ...");
				try {
					lastExt = diagram.openNsdOrArr(queuedPath);
				}
				catch (Exception ex) {
					logger.log(Level.WARNING, "Failed to load file \"" + queuedPath + "\":", ex);
				}
			}
			// Prepare the current event after having loaded at least one postponed file
			if (lastExt.equals("nsd") && filePath.toLowerCase().endsWith(".nsd")) {
				diagram.arrangeNSD();
			}
		}
		// END KGU#724 2019-09-16
		// Eventually, load the given file
		try {
			diagram.openNsdOrArr(filePath);
		}
		catch (Exception ex) {
			logger.log(Level.WARNING, "Failed to load file \"" + filePath + "\":", ex);
		}
	}

	// General info dialog; fed to the OSXAdapter as the method to call when 
	// "About OSXAdapter" is selected from the application menu
	public void about() {
		if (diagram != null) diagram.aboutNSD();
	}

	// General preferences dialog; fed to the OSXAdapter as the method to call when
	// "Preferences..." is selected from the application menu
	public void preferences() {
		if (diagram != null) diagram.preferencesNSD();
	}

	// General quit handler; fed to the OSXAdapter as the method to call when a system quit event occurs
	// A quit event is triggered by Cmd-Q, selecting Quit from the application or Dock menu, or logging out
	public boolean quit() { 
		int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.YES_OPTION)
		{
			getFrame().dispatchEvent(new WindowEvent(getFrame(), WindowEvent.WINDOW_CLOSING));
			return true;
		}
		else
		{
			return false;
		}
	}					

}

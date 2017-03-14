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
 *      Author:         Bob Fisch
 *
 *      Description:    This is the main application form.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.11      First Issue
 *      Kay Gürtzig     2015.10.18      Methods getRoot(), setRoot() introduced to ease Arranger handling (KGU#48)
 *      Kay Gürtzig     2015.10.30      Issue #6 fixed properly (see comment)
 *      Kay Gürtzig     2015.11.03      check_14 property added (For loop enhancement, #10 = KGU#3)
 *      Kay Gürtzig     2015.11.10      Issues #6 and #16 fixed by appropriate default window behaviour
 *      Kay Gürtzig     2015.11.14      Yet another improved approach to #6 / #16: see comment
 *      Kay Gürtzig     2015.11.24      KGU#88: The decision according to #6 / #16 is now returned on setRoot()
 *      Kay Gürtzig     2015.11.28      KGU#2/KGU#78/KGU#47: New checks 15, 16, and 17 registered for loading
 *      Kay Gürtzig     2015.12.04      KGU#95: Bugfix #42 - wrong default current directory mended
 *      Kay Gürtzig     2016.01.04      KGU#123: Bugfix #65 / Enh. #87 - New Ini property: mouse wheel mode
 *      Kay Gürtzig     2016.03.16      KGU#157: Bugfix #132 - Don't allow to close without having stopped Executor
 *      Kay Gürtzig     2016.03.18      KGU#89: Localization of Executor Control supported 
 *      Kay Gürtzig     2016.07.03      KGU#202: Localization of Arranger Surface supported
 *      Kay Gürtzig     2016.07.25      Issues #201, #202: Look-and-Feel propagation to Arranger and Executor
 *      Kay Gürtzig     2016.08.01      Enh. #128: new mode "Comments plus text" associated to Ini file
 *      Kay Gürtzig     2016.08.08      Issues #220, #224: Look-and Feel updates for Executor and Translator
 *      Kay Gürtzig     2016.09.09      Locales backwards compatibility precaution for release 3.25 in loadFromIni()
 *      Kay Gürtzig     2016.10.11      Enh. #267: New method updateAnalysis() introduced
 *      Kay Gürtzig     2016.11.01      Issue #81: Scale factor from Ini also applied to fonts
 *      Kay Gürtzig     2016.11.09      Issue #81: Scale factor no longer rounded except for icons, ensured to be >= 1
 *      Kay Gürtzig     2016.12.02      Enh. #300: Notification of disabled version retrieval or new versions
 *      Kay Gürtzig     2016.12.12      Enh. #305: API enhanced to support the Arranger Root index view
 *      Kay Gürtzig     2016.12.15      Enh. #310: New options for saving diagrams added
 *      Kay Gürtzig     2017.01.04      KGU#49: Closing a stand-alone instance now effectively warns Arranger
 *      Kay Gürtzig     2017.01.06      Issue #312: Measure against lost focus on start.
 *      Kay Gürtzig     2017.01.07      Enh. #101: Modified title string for dependent instances
 *      Kay Gürtzig     2017.01.15      Enh. #333: New potential preference "unicodeCompOps" added to Ini
 *      Kay Gürtzig     2017.02.03      Issue #340: Redundant calls of setLocale dropped
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      2015.11.14 New approach to solve the Window Closing problem (Kay Gürtzig, #6 = KGU#49 / #16 = KGU#66)
 *      - A new boolean field isStandalone (addressed by a new parameterized constructor) is introduced in
 *        order to decide whether to exit or only to dispose on Window Closing event. So if the Mainform is
 *        opened as a dependent frame, it should be opened as new Mainform(false) from now on. In this case
 *        it will only dispose when closing, otherwise it will exit. 
 *      2015.11.10 Window Closing problem (Kay Gürtzig, KGU#49/KGU#66)
 *      - Issues #6/#16 hadn't been solved in the intended way since the default action had still been
 *        EXIT_ON_CLOSE instead of just disposing.
 *      2015.10.30 (Kay Gürtzig)
 *      - if on closing the window the user cancels an option dialog asking him or her whether or not to save
 *        the diagram changes then the Mainform is to be prevented from closing. If the Mainform runs as
 *        a thread of the Arranger, however, this might not help a lot because it is going to be killed
 *        anyway if the Arranger was pushing the event.
 *
 ******************************************************************************************************///

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.locales.Translator;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.executor.IRoutinePool;
import lu.fisch.structorizer.executor.IRoutinePoolListener;
import lu.fisch.structorizer.locales.LangFrame;
import lu.fisch.structorizer.locales.Locales;

@SuppressWarnings("serial")
public class Mainform  extends LangFrame implements NSDController, IRoutinePoolListener
{
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
	
	// START KGU 2016-01-10: Enhancement #101: Show version number and stand-alone status in title
	private String titleString = "Structorizer " + Element.E_VERSION;
	// END KGU 2016-01-10
	// START KGU#326 2017-01-07: Enh. #101 - count the instances (for the title string)
	private static int instanceCount = 0;
	private int instanceNo;
	// END KGU#326 #2017-01-07
		
	/******************************
 	 * Setup the Mainform
	 ******************************/
	private void create()
	{
            Ini.getInstance();
            /*
            try {
                    ClassPathHacker.addFile("Structorizer.app/Contents/Resources/Java/quaqua-filechooser-only.jar");
                    UIManager.setLookAndFeel(
                                                                     "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                                                                     );
            } catch (Exception e) {
                    System.out.println(e.getMessage());
            }*/

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
            setIconImage(IconLoader.ico074.getImage());
            if (os.contains("windows")) 
            {
                setIconImage(IconLoader.ico074.getImage());
            } 
            else if (os.contains("mac")) 
            {
                setIconImage(IconLoader.icoNSD.getImage());
            }

            /******************************
             * Setup the editor
             ******************************/
            editor = new Editor(this);
            // get reference to the diagram
            diagram = getEditor().diagram;
            Container container = getContentPane();
            container.setLayout(new BorderLayout());
            container.add(getEditor(),BorderLayout.CENTER);

            /******************************
             * Setup the menu
             ******************************/
            menu = new Menu(diagram,this);
            setJMenuBar(menu);		

            /******************************
             * Update the buttons and menu
             ******************************/
            doButtons();

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
                            else
                            // END KGU#157
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
                                            // START KGU#49 2017-01-04 Care for potential Arranger dependants
                                            if (Arranger.hasInstance()) {
                                                Arranger.getInstance().windowClosing(e);
                                            }
                                            // END KGU#49 2017-01-04
                                            System.exit(0);	// This kills all related frames and threads as well!
                                    }
                                    else
                                            dispose();
                                    // END KGU#49/KGU#66 (#6/#16) 2015-11-14
                            }
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
            loadFromINI();
            // START KGU#337 2017-02-03: Issue #340 - setLocale has already been done by loadFromIni()
            //Locales.getInstance().setLocale(Locales.getInstance().getLoadedLocaleName());
            // END KGU#337 2017-02-03

            /******************************
             * Resize the toolbar
             ******************************/
            //editor.componentResized(null);
            getEditor().revalidate();
            repaint();
            diagram.redraw();
            // START KGU#305 2016-12-16
            getEditor().updateArrangerIndex(Arranger.getSortedRoots());
            // END KGU#305 2016-12-16
            // START KGU#325 2017-01-06: Issue #312 ensure the work area getting initial focus
            getEditor().scrollarea.requestFocusInWindow();
            // END KGU#325 2017-01-06
	}
	

	/******************************
	 * Load & save INI-file
	 ******************************/
	public void loadFromINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			ini.load();	// FIXME This seems to be repeated in order to buy time for the GUI

			double scaleFactor = Double.valueOf(ini.getProperty("scaleFactor","1"));
			// START KGU#287 2017-01-09 
			if (scaleFactor <= 0.5) scaleFactor = 1.0;	// Pathologic value...
			//IconLoader.setScaleFactor(scaleFactor.intValue());
			IconLoader.setScaleFactor(scaleFactor);	// The IconLoader doesn't scale down anyway
			Element.E_NEXT_SCALE_FACTOR = scaleFactor;
			// END KGU#287 2017-01-09
			
			// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
			int defaultWidth = new Double(750 * scaleFactor).intValue();
			int defaultHeight = new Double (550 * scaleFactor).intValue();
			// END KGU#287 2016-11-01
			// position
			int top = Integer.valueOf(ini.getProperty("Top","0"));
			int left = Integer.parseInt(ini.getProperty("Left","0"));
			// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
			//int width = Integer.parseInt(ini.getProperty("Width","750"));
			//int height = Integer.valueOf(ini.getProperty("Height","550"));
			int width = Integer.parseInt(ini.getProperty("Width", ""+defaultWidth));
			int height = Integer.valueOf(ini.getProperty("Height",""+defaultHeight));
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
			Locales.getInstance().setLocale(localeFileName);
			
			// colors
			Element.loadFromINI();
			updateColors();

			// parser
			D7Parser.loadFromINI();

			// look & feel
			laf=ini.getProperty("laf","Mac OS X");
			setLookAndFeel(laf);
			// START KGU#287 2016-11-01: Issue #81 (DPI awareness)
			GUIScaler.scaleDefaultFontSize(scaleFactor);
			// END KGU#287 2016-11-01
			
			// size
			setPreferredSize(new Dimension(width,height));
			setSize(width,height);
			setLocation(new Point(top,left));
			validate();

			if (diagram!=null) 
			{
				// current directory
				// START KGU#95 2015-12-04: Fix #42 Don't propose the System root but the user home
				//diagram.currentDirectory = new File(ini.getProperty("currentDirectory", System.getProperty("file.separator")));
				diagram.currentDirectory = new File(ini.getProperty("currentDirectory", System.getProperty("user.home")));
				// END KGU#95 2015-12-04
				// START KGU#300 2016-12-02: Enh. #300
				diagram.retrieveVersion = ini.getProperty("retrieveVersion", "false").equals("true");
				// END KGU#300 2016-12-02
				
				// DIN 66261
				if (ini.getProperty("DIN","0").equals("1")) // default = 0
				{
					diagram.setDIN();
				}
				// comments
				if (ini.getProperty("showComments","1").equals("0")) // default = 1
				{
					diagram.setComments(false);
				}
				// START KGU#227 2016-08-01: Enh. #128
				diagram.setCommentsPlusText(ini.getProperty("commentsPlusText","0").equals("1"));	// default = 0
				// END KGU#227 2016-08-01
				if (ini.getProperty("switchTextComments","0").equals("1")) // default = 0
				{
					diagram.setToggleTC(true);
				}
				// comments
				if (ini.getProperty("varHightlight","1").equals("1")) // default = 0
				{
					diagram.setHightlightVars(true);
				}
				// analyser
				// KGU 2016-07-27: Why has this been commented out once (before version 3.17)? See Issue #207
                /*
				if (ini.getProperty("analyser","0").equals("0")) // default = 0
				{
					diagram.setAnalyser(false);
				}
                 * */
				// START KGU#305 2016-12-14: Enh. #305
				diagram.setArrangerIndex(ini.getProperty("index", "1").equals("1"));	// default = 1
				// END KGU#305 2016-12-14
				// START KGU#123 2016-01-04: Enh. #87, Bugfix #65
				diagram.setWheelCollapses(ini.getProperty("wheelToCollapse", "0").equals("1"));
				// END KGU#123 2016-01-04
			}

			// START KGU#309 2016-12-15: Enh. #310 new saving options
			Element.E_AUTO_SAVE_ON_EXECUTE = ini.getProperty("autoSaveOnExecute", "0").equals("1");
			Element.E_AUTO_SAVE_ON_CLOSE = ini.getProperty("autoSaveOnClose", "0").equals("1");
			Element.E_MAKE_BACKUPS = ini.getProperty("makeBackups", "1").equals("1");
		    // END KGU#309 20161-12-15
			
			// START KGU#331 2017-01-15: Enh. #333 Comparison operator display
			Element.E_SHOW_UNICODE_OPERATORS = ini.getProperty("unicodeCompOps", "1").equals("1");
			// END KGU#331 2017-01-15
			
			// recent files
			try
			{	
				if (diagram != null)
				{
					for(int i=9;i>=0;i--)
					{
						if(ini.keySet().contains("recent"+i))
						{
							if(!ini.getProperty("recent"+i,"").trim().equals(""))
							{
								diagram.addRecentFile(ini.getProperty("recent"+i,""),false);
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
			
			// analyser (see also Root.saveToIni())
			// START KGU#239 2016-08-12: Code redesign
			for (int i = 1; i <= Root.numberOfChecks(); i++)
			{
				Root.setCheck(i, ini.getProperty("check" + i, "1").equals("1"));
			}
			// END KGU#2/KGU#78 2016-08-12
			
			doButtons();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println(e);

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

			// position
			ini.setProperty("Top",Integer.toString(getLocationOnScreen().x));
			ini.setProperty("Left",Integer.toString(getLocationOnScreen().y));
			ini.setProperty("Width",Integer.toString(getWidth()));
			ini.setProperty("Height",Integer.toString(getHeight()));

			// current directory, version retrieval
			if(diagram!=null)
			{
				if(diagram.currentDirectory!=null)
				{
					ini.setProperty("currentDirectory",diagram.currentDirectory.getAbsolutePath());
				}
				// START KGU#300 2016-12-02: Enh. #300
				ini.setProperty("retrieveVersion", Boolean.toString(diagram.retrieveVersion));
				// END KGU#300 2016-12-02
				// START KGU#305 2016-12-15: Enh. #305
				ini.setProperty("index", (diagram.showArrangerIndex() ? "1" : "0"));
				// END KGU#305 2016-12-15
			}
			
			// language
			ini.setProperty("Lang",Locales.getInstance().getLoadedLocaleFilename());
			
			// DIN, comments
			ini.setProperty("DIN", (Element.E_DIN ? "1" : "0"));
			ini.setProperty("showComments", (Element.E_SHOWCOMMENTS ? "1" : "0"));
			// START KGU#227 2016-08-01: Enh. #128
			ini.setProperty("commentsPlusText", Element.E_COMMENTSPLUSTEXT ? "1" : "0");
			// END KGU#227 2016-08-01
			ini.setProperty("switchTextComments", (Element.E_TOGGLETC ? "1" : "0"));
			ini.setProperty("varHightlight", (Element.E_VARHIGHLIGHT ? "1" : "0"));
			// KGU 2016-07-27: Why has this been commented out once (before version 3.17)? See Issue #207
			//ini.setProperty("analyser", (Element.E_ANALYSER ? "1" : "0"));
			// START KGU#123 2016-01-04: Enh. #87
			ini.setProperty("wheelToCollapse", (Element.E_WHEELCOLLAPSE ? "1" : "0"));
			// END KGU#123 2016-01-04
			
		    // START KGU#309 2016-12-15: Enh. #310 new saving options
		    ini.setProperty("autoSaveOnExecute", (Element.E_AUTO_SAVE_ON_EXECUTE ? "1" : "0"));
		    ini.setProperty("autoSaveOnClose", (Element.E_AUTO_SAVE_ON_CLOSE ? "1" : "0"));
		    ini.setProperty("makeBackups", (Element.E_MAKE_BACKUPS ? "1" : "0"));
		    // END KGU#309 20161-12-15

			// START KGU#331 2017-01-15: Enh. #333 Comparison operator display
		    ini.setProperty("unicodeCompOps", (Element.E_SHOW_UNICODE_OPERATORS ? "1" : "0"));
			// END KGU#331 2017-01-15

			// look and feel
			if(laf!=null)
			{
				ini.setProperty("laf", laf);
			}
			
			// START KGU#287 2017-01-11: Issue #81/#330
			if (this.preselectedScaleFactor != null) {
				ini.setProperty("scaleFactor", this.preselectedScaleFactor);
			}
			// END KGU#287 2017-01-11
			
			// START KGU#300 2016-12-02: Enh. #300
			// Update hint suppression
			ini.setProperty("suppressUpdateHint", this.suppressUpdateHint);
			// END KGU#300 2016-12-02
			
			// recent files
			if(diagram!=null)
			{
				if(diagram.recentFiles.size()!=0)
				{
					for(int i=0;i<diagram.recentFiles.size();i++)
					{
						//System.out.println(i);
						ini.setProperty("recent"+String.valueOf(i),(String) diagram.recentFiles.get(i));
					} 
				}
			}
			
			ini.save();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
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
		if(_laf!=null)
		{
			//System.out.println("Setting: "+_laf);
			laf=_laf;
			
			UIManager.LookAndFeelInfo plafs[] = UIManager.getInstalledLookAndFeels();
			for(int j = 0; j < plafs.length; ++j)
			{
				//System.out.println("Listing: "+plafs[j].getName());
				if(_laf.equals(plafs[j].getName()))
				{
					//System.out.println("Found: "+plafs[j].getName());
					try
					{
						UIManager.setLookAndFeel(plafs[j].getClassName());
//						// START KGU#287 2017-01-09: Bugfix #330 on switching back to "Nimbus", several font sizes get lost - DOESN'T WORK
//						if (_laf.equalsIgnoreCase("nimbus")) {
//							double scaleFactor = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
//							GUIScaler.scaleDefaultFontSize(scaleFactor);
//						}
//						// END KGU#287 2017-01-09
						SwingUtilities.updateComponentTreeUI(this);
						// START KGU#211 2016-07-25: Issue #202 - Propagation to Arranger
						if (Arranger.hasInstance())
						{
							SwingUtilities.updateComponentTreeUI(Arranger.getInstance());
						}
						// END KGU#211 2016-07-25
						// START KGU#210 2016-08-08: Issue #201 - Propagation to Executor Control
						Executor.updateLookAndFeel();
						// END KGU#210 2016-08-08
						// START KGU#233 2016-08-08: Issue #220
						Translator.updateLookAndFeel();
						// END KGU#233 2016-08-08
					}
					catch (Exception e)
					{
						// show error
						JOptionPane.showOptionDialog(null,e.getMessage(),
													 "Error ...",
													 JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
					}
				}
			}
		}
	}
    
    @Override
    public void savePreferences()
    {
            //System.out.println("Saving");
            saveToINI();
            D7Parser.saveToINI();
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
//    public Mainform()
//    {
//        super();
//        create();
//    }

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
    
    // START KGU#300 2016-12-02: Enh. #300
    public void notifyNewerVersion()
    {
    	if (!Ini.getInstance().getProperty("retrieveVersion", "false").equals("true")) {
    		if (!Element.E_VERSION.equals(this.suppressUpdateHint)) {
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
    }
    // END KGU#300 2016-12-02
    
    public boolean isStandalone()
    {
    	return this.isStandalone;
    }

    // START KGU#305 2016-12-16: Code revision
	@Override
	public void routinePoolChanged(IRoutinePool _source) {
		if (_source instanceof Arranger && this.editor != null) {
			this.editor.updateArrangerIndex(Arranger.getSortedRoots());
		}
		updateAnalysis();
	}
	// END KGU#305 2016-12-16
	

}

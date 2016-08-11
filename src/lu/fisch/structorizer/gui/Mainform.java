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
 *      Kay Gürtzig     2016.08.08      Issues #220, #224: Loak-and Feel updates for Executor and Translator
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
import lu.fisch.structorizer.locales.LangFrame;
import lu.fisch.structorizer.locales.Locales;

@SuppressWarnings("serial")
public class Mainform  extends LangFrame implements NSDController
{
	public Diagram diagram = null;
	private Menu menu = null;
	private Editor editor = null;
	
	private String laf = null;
	
	// START KGU#49/KGU#66 2015-11-14: This decides whether to exit or just to dispose when being closed
	private boolean isStandalone = true;	// The default is to exit...
	// END KGU#49/KGU#66 2015-11-14
	
	// START KGU 2016-01-10: Enhancement #101: Show version number and stand-alone status in title
	private String titleString = "Structorizer " + Element.E_VERSION;
	// END KGU 2016-01-10
		
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
            loadFromINI();

            /******************************
             * Some JFrame specific things
             ******************************/
            // set window title
            // START KGU 2016-01-10: Enhancement #101 - show version number and standalone status
            //setTitle("Structorizer");
            if (!this.isStandalone) titleString = "(" + titleString + ")";
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
            // get reference tot he diagram
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
                            if (diagram.saveNSD(true))
                            {
                                    saveToINI();
                                    // START KGU#49/KGU#66 (#6/#16) 2015-11-14: only EXIT if there are no owners
                                    if (isStandalone)
                                            System.exit(0);	// This kills all related frames and threads as well!
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
            loadFromINI();
            Locales.getInstance().setLocale(Locales.getInstance().getLoadedLocaleName());

            /******************************
             * Resize the toolbar
             ******************************/
            //editor.componentResized(null);
            getEditor().revalidate();
            repaint();
            getEditor().diagram.redraw();
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
			ini.load();

			double scaleFactor = Double.valueOf(ini.getProperty("scaleFactor","1")).intValue();
                        IconLoader.setScaleFactor(scaleFactor);
			
                        // position
			int top = Integer.valueOf(ini.getProperty("Top","0"));
			int left = Integer.parseInt(ini.getProperty("Left","0"));
			int width = Integer.parseInt(ini.getProperty("Width","750"));
			int height = Integer.valueOf(ini.getProperty("Height","550"));

                        // reset to defaults if wrong values
                        if (top<0) top=0;
                        if (left<0) left=0;
                        if (width<=0) width=750;
                        if (height<=0) height=550;

			// language	
                        Locales.getInstance().setLocale(ini.getProperty("Lang","en"));
                        
                        // colors
                        Element.loadFromINI();
                        updateColors();
                        
                        // parser
                        D7Parser.loadFromINI();

			// look & feel
			laf=ini.getProperty("laf","Mac OS X");
			setLookAndFeel(laf);
			
			// size
			setPreferredSize(new Dimension(width,height));
			setSize(width,height);
			setLocation(new Point(top,left));
			validate();

			if(diagram!=null) 
			{
				// current directory
				// START KGU#95 2015-12-04: Fix #42 Don't propose the System root but the user home
				//diagram.currentDirectory = new File(ini.getProperty("currentDirectory", System.getProperty("file.separator")));
				diagram.currentDirectory = new File(ini.getProperty("currentDirectory", System.getProperty("user.home")));
				// END KGU#95 2015-12-04
				
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
				// START KGU#123 2016-01-04: Enh. #87, Bugfix #65
				diagram.setWheelCollapses(ini.getProperty("wheelToCollapse", "0").equals("1"));
				// END KGU#123 2016-01-04
			}
			
			// recent files
			try
			{	
				if(diagram!=null)
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
			Root.check1 = ini.getProperty("check1","1").equals("1");
			Root.check2 = ini.getProperty("check2","1").equals("1");
			Root.check3 = ini.getProperty("check3","1").equals("1");
			Root.check4 = ini.getProperty("check4","1").equals("1");
			Root.check5 = ini.getProperty("check5","1").equals("1");
			Root.check6 = ini.getProperty("check6","1").equals("1");
			Root.check7 = ini.getProperty("check7","1").equals("1");
			Root.check8 = ini.getProperty("check8","1").equals("1");
			Root.check9 = ini.getProperty("check9","1").equals("1");
			Root.check10 = ini.getProperty("check10","1").equals("1");
			Root.check11 = ini.getProperty("check11","1").equals("1");
			Root.check12 = ini.getProperty("check12","1").equals("1");
			Root.check13 = ini.getProperty("check13","1").equals("1");
			// START KGU#3 2015-11-03: New check for enhanced FOR loops
			Root.check14 = ini.getProperty("check14","1").equals("1");
			// END KGU#3 2015-11-03
			// START KGU#2/KGU#78 2015-11-28: New checks for CALL and JUMP elements
			Root.check15 = ini.getProperty("check15","1").equals("1");
			Root.check16 = ini.getProperty("check16","1").equals("1");
			Root.check17 = ini.getProperty("check17","1").equals("1");
			// END KGU#2/KGU#78 2015-11-28

			
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

			// current directory
			if(diagram!=null)
			{
				if(diagram.currentDirectory!=null)
				{
					ini.setProperty("currentDirectory",diagram.currentDirectory.getAbsolutePath());
				}
			}
			
			// language
			ini.setProperty("Lang",Locales.getInstance().getLoadedLocaleFilename());
			
			// DIN, comments
			ini.setProperty("DIN",(Element.E_DIN?"1":"0"));
			ini.setProperty("showComments",(Element.E_SHOWCOMMENTS?"1":"0"));
			// START KGU#227 2016-08-01: Enh. #128
			ini.setProperty("commentsPlusText", Element.E_COMMENTSPLUSTEXT ? "1" : "0");
			// END KGU#227 2016-08-01
			ini.setProperty("switchTextComments",(Element.E_TOGGLETC?"1":"0"));
			ini.setProperty("varHightlight",(Element.E_VARHIGHLIGHT?"1":"0"));
			// KGU 2016-07-27: Why has this been commented out once (before version 3.17)? See Issue #207
			//ini.setProperty("analyser",(Element.E_ANALYSER?"1":"0"));
			// START KGU#123 2016-01-04: Enh. #87
			ini.setProperty("wheelToCollapse",(Element.E_WHEELCOLLAPSE?"1":"0"));
			// END KGU#123 2016-01-04
			
			// look and feel
			if(laf!=null)
			{
				ini.setProperty("laf", laf);
			}
			
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
        this.isStandalone = standalone;
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
    		done = this.diagram.setRoot(root);
    	}
    	return done;
    }
	
}

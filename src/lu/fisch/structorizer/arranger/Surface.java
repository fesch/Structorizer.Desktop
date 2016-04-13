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
 *      Author          Date			Description
 *      ------          ----			-----------
 *      Bob Fisch       2009.08.18		First Issue
 *      Kay Gürtzig     2015.10.18		Several enhancements to improve Arranger usability (see comments)
 *      Kay Gürtzig     2015.11.14      Parameterized creation of dependent Mainforms (to solve issues #6, #16)
 *      Kay Gürtzig     2015.11.18      Several changes to get scrollbars working (issue #35 = KGU#85)
 *                                      removal mechanism added, selection mechanisms revised
 *      Kay Gürtzig     2015.12.17      Bugfix KGU#111 for Enh. #63, preparations for Enh. #62 (KGU#110)
 *      Kay Gürtzig     2015.12.20      Enh. #62 (KGU#110) 1st approach: Load / save as mere file list.
 *                                      Enh. #35 (KGU#88) Usability improvement (automatic pinning)
 *      Kay Gürtzig     2016.01.02      Bugfix #78 (KGU#119): Avoid reloading of structurally equivalent diagrams 
 *      Kay Gürtzig     2016.01.15      Enh. #110: File open dialog now selects the NSD filter
 *      Kay Gürtzig     2016.03.02      Bugfix #97 (KGU#136): Modifications for stable selection
 *      Kay Gürtzig     2016.03.03      Bugfix #121 (KGU#153): Successful file dropping must not pop up an error message
 *      Kay Gürtzig     2016-03-08		Bugfix #97: Method for drawing info invalidation added (KGU#155) 
 *      Kay Gürtzig     2016.03.09      Enh. #77 (KGU#117): Methods clearExecutionStatus and setCovered added
 *      Kay Gürtzig     2016.03.12      Enh. #124 (KGU#156): Generalized runtime data visualisation (refactoring)
 *      Kay Gürtzig     2016.03.14      Enh. #62 update: currentDirectory adopted from first added diagram.
 *      Kay Gürtzig     2016.03.16      Bugfix #132: Precautions against stale Mainform references (KGU#158) 
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2016.03.16 (Kay Gürtzig)
 *      - It still happened that double-clicking on a diagram seemed to have no effect. In these cases
 *        actually there was a stale Mainform reference. The reason was that the windowClosing() trigger
 *        used to compare the Roots instead of the Mainform itself. So wrong reference may have been
 *        removed, e.g. if the Mainform contained some called subroutine it wasn't associated with.
 *        On the other hand, a handling for such a case was missing in the mouseClicked() trigger. 
 *      2016.03.08 (Kay Gürtzig)
 *      - Enh. #77: For Test Coverage Tracking, Arranger in its function of a subroutine pool had to
 *        be enabled to set oder clear coverage flags 
 *      2016.01.02 (Kay Gürtzig)
 *      - Bug #78: On (re)placing diagrams from a Structorizer Mainframe, an identity check had already
 *        duplicate diagram presence, but on file dropping and reloading a saved arrangement (Enhancement #62),
 *        an identity check didn't help, of course. So for these cases, a structural equivalence check had
 *        to be used instead - the bugfix realises this by new method Root.equals(). 
 *      2015.11.18 (Kay Gürtzig)
 *      - In order to achieve scrollability, autoscroll mode (on dragging) had to be enabled, used area has
 *        to be communicated (nothing better than resetting the layout found - see adapt_layout())
 *      - Method removeDiagram() added,
 *      - selection consistency improved (never select or unselect a diagram element other than root, don't
 *        select eclipsed diagrams, don't leave selection flag on diagrams no longer selected).
 *      2015.11.14 (Kay Gürtzig)
 *      - The creation of dependant Mainforms is now done via a parameterized constructor in order to
 *        inform the Mainform that it must not exit on closing but may only dispose.
 *      2015.10.18 (KGU)
 *      - New interface method replaced() implemented that allows to keep track of NSD replacement in a
 *        related Mainform (KGU#48)
 *      - New interface method findSourcesByName() to prepare subroutine execution in a future effort (KGU#2)
 *      - Method saveDiagrams() added, enabling the Mainforms to save dirty diagrams before exit (KGU#49)
 *
 ******************************************************************************************************/

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import lu.fisch.graphics.Rect;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Updater;
import lu.fisch.structorizer.executor.IRoutinePool;
import lu.fisch.structorizer.gui.Mainform;
import lu.fisch.structorizer.io.ArrFilter;
import lu.fisch.structorizer.io.PNGFilter;
import lu.fisch.structorizer.parsers.NSDParser;
import lu.fisch.utils.StringList;
import net.iharder.dnd.FileDrop;

/**
 *
 * @author robertfisch
 */
@SuppressWarnings("serial")
public class Surface extends javax.swing.JPanel implements MouseListener, MouseMotionListener, WindowListener, Updater, IRoutinePool {

    private Vector<Diagram> diagrams = new Vector<Diagram>();

    private Point mousePoint = null;
    private Point mouseRelativePoint = null;
    private boolean mousePressed = false;
    private Diagram mouseSelected = null;
    // START KGU#88 2015-11-24: We may often need the pin icon
    public static Image pinIcon = null;
    // END KGU#88 2015-11-24
    // START KGU#110 2015-12-21: Enh. #62, also supports PNG export
    public File currentDirectory = new File(System.getProperty("user.home"));
    // END KGU#110 2015-12-21

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        if(diagrams!=null)
        {
            for(int d=0; d<diagrams.size(); d++)
            {
                Diagram diagram = diagrams.get(d);
                Root root = diagram.root;
                Point point = diagram.point;
                
                // START KGU#88 2015-11-24
                //root.draw(g, point, this);
                Rect rect = root.draw(g, point, this);
                if (diagram.isPinned)
                {
                	if (pinIcon == null)
                	{
                		pinIcon = new ImageIcon(getClass().getResource("/lu/fisch/structorizer/gui/icons/pin_blue_14x20.png")).getImage();
                	}
                	int x = rect.right - pinIcon.getWidth(null)*3/4;
                	int y = rect.top - pinIcon.getHeight(null)/4;
                	
                	((Graphics2D)g).drawImage(pinIcon, x, y, null);
                	//((Graphics2D)g).drawOval(rect.right - 15, rect.top + 5, 10, 10);
                }
                // END KGU#88 2015_11-24
            }
        }
    }

    private void create()
    {
    	new  FileDrop(this, new FileDrop.Listener()
    	{
    		public void  filesDropped( java.io.File[] files )
    		{
    			//boolean found = false;
// START KGU#111 2015-12-17: Bugfix #63: We must now handle a possible exception
//    			for (int i = 0; i < files.length; i++)
//    			{
//    				String filename = files[i].toString();
//    				if(filename.substring(filename.length()-4).toLowerCase().equals(".nsd"))
//    				{
//    					// open an existing file
//    					NSDParser parser = new NSDParser();
//    					File f = new File(filename);
//        				Root root = parser.parse(f.toURI().toString());
//    					
//        				root.filename=filename;
//        				addDiagram(root);
//    				}
//    			}
    			loadFiles(files);
// END KGU#111 2015-12-17
    		}
    	});

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        // START KGU#85 2015-11-18
        this.setAutoscrolls(true);
        // END KGU#85 2015-11-18

    }
    
    // START KGU#110 2015-12-17: Enh. #62 - offer an opportunity to save / load an arrangement
    public int loadFiles(java.io.File[] files)
    {
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
    			troubles += "\"" + filename + "\": " + errorMessage;
    			System.err.println("Arranger failed to load \"" + filename + "\": " + troubles);	
    		}
    		else
    		{
    			nLoaded++;
    		}
    		// END KGU#153 2016-03-03
    	}
    	if (!troubles.isEmpty())
    	{
			JOptionPane.showMessageDialog(this, troubles, "File Load Error", JOptionPane.ERROR_MESSAGE);
    	}
    	return nLoaded;
    }
    
    private String loadFile(String filename)
    {
    	return loadFile(filename, null);
    }
    
    private String loadFile(String filename, Point point)
    {
    	String errorMessage = "";
		if(filename.substring(filename.length()-4).toLowerCase().equals(".nsd"))
		{
			// open an existing file
			NSDParser parser = new NSDParser();
			File f = new File(filename);	// FIXME (KGU) Why don't we just use files[i]?
			// START KGU#111 2015-12-17: Bugfix #63: We must now handle a possible exception
			try {
			// END KGU#111 2015-12-17
				Root root = parser.parse(f.toURI().toString());

				root.filename = filename;
				addDiagram(root, point);
   			// START KGU#111 2015-12-17: Bugfix #63: We must now handle a possible exception
			}
			catch (Exception ex) {
				errorMessage = ex.getLocalizedMessage();
			}
			// END KGU#111 2015-12-17
		}
    	return errorMessage;
    }
    
    /**
     * Stores the current diagram arrangement to a file.
     * In this first approach this will only be a list of points and filenames
     * Hence, the file won't be portable (unless all listed files were copied accordingly.
     * 
     * The final version might produce a packed archive containing the list file as well
     * as the referenced NSD files such that it can be ported to a different location and
     * extracted.
     *  
     * @param frame - the commanding GUI component
     * @return status flag (true iff the saving succeeded without error) 
     */
    public boolean saveArrangement(Frame frame)
    {
    	boolean done = false;
    	// Ensure the diagrams themselves are saved
    	this.saveDiagrams();
    	// Let's select path and name for the list / archive file
        JFileChooser dlgSave = new JFileChooser("Save arranged set of diagrams ...");
        dlgSave.addChoosableFileFilter(new ArrFilter());
        dlgSave.setCurrentDirectory(currentDirectory);
        int result = dlgSave.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            currentDirectory = dlgSave.getCurrentDirectory();
            // correct the filename if necessary
            String filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
            if (!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".arr"))
            {
                    filename+=".arr";
            }
            done = saveArrangement(frame, filename + "");
        }
        return done;
    }
    
    /**
     * Stores the current diagram arrangement to a file.
     * In this first approach this will only be a list of points and filenames
     * Hence, the file won't be portable (unless all listed files were copied accordingly.
     * 
     * The final version might produce a packed archive containing the list file as well
     * as the referenced NSD files such that it can be ported to a different location and
     * extracted.
     *  
     * @param frame - the commanding GUI component
     * @param filename - the name of the selected file
     * @return status flag (true iff the saving succeeded without error) 
     */
    public boolean saveArrangement(Frame frame, String filename)
    {
    	String[] EnvVariablesToCheck = { "TEMP", "TMP", "TMPDIR", "HOME", "HOMEPATH" };
    	boolean done = false;
    	// Ensure the diagrams themselves are saved
    	String outFilename = filename + "";		// Name of the actually written file
    	try
    	{
    		// set up the file
    		File file = new File(outFilename);
    		boolean fileExisted = file.exists(); 
    		if (fileExisted)
    		{
    			// Find a suited temporary directory to store the output file
    			String tempDir = "";
    			for (int i = 0; (tempDir == null || tempDir.isEmpty()) && i < EnvVariablesToCheck.length; i++)
    			{
    				tempDir = System.getenv(EnvVariablesToCheck[i]);
    			}
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
    			outFilename = tempDir + File.separator + "Arranger.tmp";
    		}
    		FileOutputStream fos = new FileOutputStream(outFilename);
    		Writer out = new OutputStreamWriter(fos, "UTF8");
    		for (int d = 0; d < this.diagrams.size(); d++)
    		{
    			Diagram diagr = this.diagrams.get(d);
    			out.write(Integer.toString(diagr.point.x) + ",");
    			out.write(Integer.toString(diagr.point.y) + ",");
    			StringList entry = new StringList();
    			entry.add(diagr.root.getPath());
    			out.write(entry.getCommaText()+'\n');
    		}

    		out.close();

    		// If the Arr file had existed then replace it by the output file after having created a backup
    		if (fileExisted)
    		{
    			File backUp = new File(filename + ".bak");
    			if (backUp.exists())
    			{
    				backUp.delete();
    			}
    			file.renameTo(backUp);
    			file = new File(filename);
    			File tmpFile = new File(outFilename);
    			tmpFile.renameTo(file);
    		}
    		// END KGU#94 2015.12.04

    		done = true;
    	}
    	catch (Exception ex)
    	{
    		JOptionPane.showMessageDialog(frame, "Error on saving the arrangement:" + ex.getMessage() + "!",
    				"Error", JOptionPane.ERROR_MESSAGE, null);
    	}
    	return done;
    }
    
    public boolean loadArrangement(Frame frame)
    {
    	boolean done = false;
    	// Ensure the previous diagrams themselves are saved
    	this.saveDiagrams();
    	// Let's select path and name for the list / archive file
        JFileChooser dlgOpen = new JFileChooser("Reload a stored arrangement of diagrams ...");
        // START KGU 2016-01-15: Enh. #110 - select the provided filter
        //dlgOpen.addChoosableFileFilter(new ArrFilter());
        ArrFilter filter = new ArrFilter();
        dlgOpen.addChoosableFileFilter(filter);
        dlgOpen.setFileFilter(filter);
        // END KGU 2016-01-15: Enh. #110

        dlgOpen.setCurrentDirectory(currentDirectory);
        
        int result = dlgOpen.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            currentDirectory = dlgOpen.getCurrentDirectory();
            // correct the filename if necessary
            String filename = dlgOpen.getSelectedFile().getAbsoluteFile().toString();
            done = loadArrangement(frame, filename);
        }
   	
    	return done;
    }

    public boolean loadArrangement(Frame frame, String filename)
    {
    	boolean done = false;
    	
    	String errorMessage = null;
    	try
    	{
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
    			String trouble = loadFile(nsdFileName, point);
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
    			}
    		}

    		in.close();

    		done = true;
    	}
    	catch (Exception ex)
    	{
    		errorMessage = ex.getLocalizedMessage();
    		if (errorMessage == null)
    		{
    			errorMessage = ex.toString();
    		}
    	}
    	if (errorMessage != null)
    	{
    		JOptionPane.showMessageDialog(frame, "Error on loading the arrangement: " + errorMessage + "!",
    				"Error", JOptionPane.ERROR_MESSAGE, null);   		
    	}
    	return done;
    }
    // END KGU#110 2015-12-17
    
    public void exportPNG(Frame frame)
    {
        JFileChooser dlgSave = new JFileChooser("Export diagram as PNG ...");
        // propose name
        //String uniName = directoryName.substring(directoryName.lastIndexOf('/')+1).trim();
        //dlgSave.setSelectedFile(new File(uniName));

        dlgSave.addChoosableFileFilter(new PNGFilter());
        dlgSave.setCurrentDirectory(currentDirectory);
        int result = dlgSave.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            currentDirectory = dlgSave.getCurrentDirectory();
            // correct the filename, if necessary
            String filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
            if(!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".png"))
            {
                    filename+=".png";
            }

            // deselect any diagram
            if(diagrams!=null)
            {
                for(int d=0; d<diagrams.size(); d++)
                {
                    diagrams.get(d).root.setSelected(false);
                }
                repaint();
            }

            // set up the file
            File file = new File(filename);
            // create the image
            BufferedImage bi = new BufferedImage(this.getWidth(), this.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
            paint(bi.getGraphics());
            // save the file
            try
            {
                ImageIO.write(bi, "png", file);
            }
            catch(Exception e)
            {
                JOptionPane.showOptionDialog(frame,"Error while saving the image!","Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
            }
        }
    }

    public Rect getDrawingRect()
    {
        Rect r = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);


        if(diagrams!=null)
        {
        	//System.out.println("--------getDrawingRect()---------");
            if(diagrams.size()>0)
            for(int d=0; d<diagrams.size(); d++)
            {
                Diagram diagram = diagrams.get(d);
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
                int width = Math.max(rect.right, 80);
                // empirical minimum height of an empty diagram 
                //int height = Math.max(rect.bottom - rect.top, 118);
                int height = Math.max(rect.bottom, 118);
                // END KGU#136 2016-03-01
                //System.out.println(root.getMethodName() + ": (" + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom +")");
                r.left = Math.min(diagram.point.x, r.left);
                r.top = Math.min(diagram.point.y, r.top);
                r.right = Math.max(diagram.point.x + width, r.right);
                r.bottom = Math.max(diagram.point.y + height, r.bottom);
                //END KGU#85 2015-11-18
            }
            else  r = new Rect(0,0,0,0);
        }
        else r = new Rect(0,0,0,0);

        //System.out.println("drawingRect: (" + r.left + ", " + r.top + ", " + r.right + ", " + r.bottom +")");

        return r;
    }
    
    private Rect adaptLayout()
    {
    	Rect rect = getDrawingRect();
    	// Didn't find anything else to effectively inform the scrollbars about current extension
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
        		layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        		.add(0, rect.right, Short.MAX_VALUE)
        		);
        layout.setVerticalGroup(
        		layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        		.add(0, rect.bottom, Short.MAX_VALUE)
        		);
    	return rect;	// Just in case someone might need it
    }

    public void addDiagram(Root root)
    // START KGU#2 2015-11-19: Needed a possibility to register a related Mainform
    {
    	addDiagram(root, null, null);
    }
    /**
     * Places the passed-in diagram root in the drawing area if it hadn't already been
     * residing here. If a Mainform form was given, then it is registered with the root
     * (unless there is already another Mainform associated) and root will automatically
     * be pinned.
     * @param root - a diagram to be placed here
     * @param form - the sender of the diagram if it was pushed here from a Structorizer instance
     */
    public void addDiagram(Root root, Mainform form)
    // START KGU#110 2015-12-20: Enhancement #62 -we want to be able to use predefined positions
    {
    	this.addDiagram(root, form, null);
    }
    
    /**
     * @param root - the root element of the diagram to be added
     * @param position - the proposed position
     */
    public void addDiagram(Root root, Point position)
    // START KGU#110 2015-12-20: Enhancement #62 - we want to be able to use predefined positions
    {
    	this.addDiagram(root, null, position);
    }    
    
    /**
     * Places the passed-in diagram root in the drawing area if it hadn't already been
     * residing here. If a Mainform form was given, then it is registered with the root
     * (unless there is already another Mainform associated) and root will automatically
     * be pinned.
     * If point is given then the diaram will be place to that position, otherwise a free
     * area is looked for.
     * @param root - the root element of the diagram to be added
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
    	Diagram diagram = findDiagram(root, form != null);	// If the Mainform is given, then it's not from file
    	// END KGU#1119 2016-01-02
    	if (diagram == null) {
    	// END KGU#2 2015-11-19
    		Rect rect = getDrawingRect();

    		int top = 10;
    		int left = 10;

    		top  = Math.max(rect.top, top);
    		left = Math.max(rect.right+10, left);

    		if (left>this.getWidth())
    		{
    			// FIXME (KGU 2015-11-19) This isn't really sensible - might find a free space by means of a quadtree?
    			top = rect.bottom+10;
    			left = rect.left;
    		}
    		// START KGU#110 2015-12-20
    		//Point point = new Point(left,top);
    		if (point == null)
    		{
    			point = new Point(left,top);
    		}
    		// END KGU#110 2015-12-20
    		// START KGU 2016-03-14: Enh. #62
    		// If it's the first diagram then adopt the crrent directory if possible
    		if (diagrams.isEmpty() && root.filename != null && !root.filename.isEmpty())
    		{
    			this.currentDirectory = new File(root.filename);
    		}
    		// END KGU 2016-03-14
    		/*Diagram*/ diagram = new Diagram(root,point);
    		diagrams.add(diagram);
    		// START KGU#85 2015-11-18
    		adaptLayout();
    		// END KGU#85 2015-11-18
    		// START KGU 2015-11-30
    		// START KGU#136 2016-03-01: Bugfix #97 - here we need the actual position
    		//Rectangle rec = root.getRect().getRectangle();
    		//rec.setLocation(left, top);
    		Rectangle rec = root.getRect(point).getRectangle();
    		// END KGU#136 2016-03-01
    		if (rec.width == 0)	rec.width = 120;
    		if (rec.height == 0) rec.height = 150;
    		this.scrollRectToVisible(rec);
    		// START KGU#88 2015-12-20: It ought to be pinned if form wasn't null
    		if (form != null)
    		{
    			diagram.isPinned = true;
    		}
    		// END KGU#88 2015-12-20
    		// END KGU 2015-11-30
    		repaint();
    		getDrawingRect();
    	// START KGU#2 2015-11-19
    	}
    	// START KGU#119 2016-01-02: Bugfix #78 - if a position is given then move the found diagram
    	else if (point != null)
    	{
    		diagram.point = point;
    		repaint();
    		getDrawingRect();    		
    	}
    	// END KGU#119 2016-01-02
    	if (form != null)
    	{
        	// START KGU#125 2016-01-07: We allow adoption but only for orphaned diagrams
    		//diagram.mainform = form;
        	if (diagram.mainform == null)
        	{
        		diagram.mainform = form;
        	}
        	// END KGU#125 2016-01-07
    		root.addUpdater(this);
    	}
    	// END KGU#2 2015-11-19
    }

    // START KGU#85 2015-11-17
    public void removeDiagram()
    {
    	if (this.mouseSelected != null)
    	{
    		this.mouseSelected.root.removeUpdater(this);
    		diagrams.remove(this.mouseSelected);
    		this.mouseSelected = null;
    		adaptLayout();
            repaint();
    	}
    }
    // END KGU#85 2015-11-17

    // START KGU#88 2015-11-24: Provide a possibility to protect diagrams against replacement
    public void togglePinned()
    {
    	if (this.mouseSelected != null)
    	{
    		this.mouseSelected.isPinned = !this.mouseSelected.isPinned;
    		repaint();
    	}
    }
    // END KGU#88 2015-11-24

    // START KGU#117 2016-03-09: Provide a possibility to mark diagrams as test-covered
    public void setCovered(Frame frame)
    {
    	if (this.maySetCovered())
    	{
    		if (this.mouseSelected.root.deeplyCovered)
    		{
    			if (JOptionPane.showConfirmDialog(frame, "Routine is already marked as test-covered! Reset coverage mark?") == JOptionPane.OK_OPTION)
    			{
    				this.mouseSelected.root.deeplyCovered = false;
    			}
    		}
    		else
    		{
    			this.mouseSelected.root.deeplyCovered = true;
    			this.mouseSelected.root.setSelected(false);
    			this.mouseSelected = null;
    		}
    		repaint();
    	}
    	else
    	{
    		JOptionPane.showMessageDialog(frame, "No suitable routine diagram selected, cannot mark anything as covered!",
    				"Error", JOptionPane.ERROR_MESSAGE, null);   		
    	}
    }
    
    public boolean maySetCovered()
    {
    	return Element.E_COLLECTRUNTIMEDATA &&
    			this.mouseSelected != null &&
    			this.mouseSelected.root != null &&
    			!this.mouseSelected.root.isProgram;
    }
    // END KGU#88 2015-11-24

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @return the diagrams
     */
    public Vector<Diagram> getDiagrams()
    {
        return diagrams;
    }

    /**
     * @param diagrams the diagrams to set
     */
    public void setDiagrams(Vector<Diagram> diagrams)
    {
        this.diagrams = diagrams;
    }
    
    // START KGU#49 2015-10-18: When the window is going to be closed we have to give the diagrams a chance to store their stuff
    // FIXME (KGU): Quick-and-dirty version. More convenient should be a list view with all unsaved diagrams for checkbox selection
    /**
     * Loops over all administered diagrams and has their respective Mainform (if still alive) saved them in case they are dirty 
     */
    public void saveDiagrams()
    {
    	if (this.diagrams != null)
    	{
    		Iterator<Diagram> iter = this.diagrams.iterator();
    		while (iter.hasNext())
    		{
    			Diagram diagram = iter.next();
    			Mainform form = diagram.mainform;
    			if (form != null)
    			{
    				form.diagram.saveNSD(true);
    			}
    		}
    	}
    }
    // END KGU#49 2015-10-18
    

    public void mouseClicked(MouseEvent e)
    {
        mousePressed(e);
        // Double click?
        if (e.getClickCount()==2 && mouseSelected!=null)
        {
            // create editor
            Mainform form = mouseSelected.mainform;
            // START KGU#158 2016-03-16: Bugfix #132 - Precaution against stale Mainform
            int nAttempts = 0;
            do {
            	try
            	{
            // END KGU#158 2016-03-16 (part 1)
            		// START KGU#88 2015-11-24: An atteched Mainform might refuse to re-adopt the root
            		//if(form==null)
            		if(form==null || !form.setRoot(mouseSelected.root))
            			// END KGU#88 2015-11-24
            		{
            			// START KGU#49/KGU#66 2015-11-14: Start a dependent Mainform not willing to kill us
            			//form=new Mainform();
            			form=new Mainform(false);
            			// END KGU#49/KGU#66 2015-11-14
            			form.addWindowListener(this);
            			// With a new Mainform, refusal is not possible 
            			form.setRoot(mouseSelected.root);
            		}

            		// change the default closing behaviour
            		// START KGU#49/KGU#66 2015-11-14 Now already achieved by constructor argument false  
            		//form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            		// END KGU#49/#66 2015-11-14

            		// store mainform in diagram
            		mouseSelected.mainform=form;

            		// register this as "updater"
            		mouseSelected.root.addUpdater(this);

            		// attach the new diagram to the editor
            		// START KGU#88 2015-11-24: Now already done above
            		//form.setRoot(mouseSelected.root);
            		// END KGU#88 2015-11-24
            		form.setVisible(true);
            // START KGU#158 2016-03-16: Bugfix #132 (part 2)
            	}
            	catch (Exception ex)
            	{
            		// Seems the Mainform was stale (closed without having been cleared)
            		form = null;
            	}
            } while (form == null && nAttempts++ < 2);
            // END KGU#158 2016-03-16 (part 2)

            mouseSelected=null;
            mousePressed=false;
            // START KGU#85 2015-11-18
            adaptLayout();
            // END KGU#85 2015-11-18
            this.repaint();
        }
    }

    public void mousePressed(MouseEvent e)
    {
        mousePoint = e.getPoint();
        mousePressed = true;
        // START KGU 2015-11-18: First unselect the selected diagram (if any)
        if (mouseSelected != null && mouseSelected.root != null)
        {
        	mouseSelected.root.setSelected(false);
        	mouseSelected = null;
        }
        // END KGU 2015-11-18
        for (int d=0; d<diagrams.size(); d++)
        {
            Diagram diagram = diagrams.get(d);
            Root root = diagram.root;
   
            // START KGU 2015-11-18 No need to select something (may have side-effects!) 
            //Element ele = root.selectElementByCoord(mousePoint.x-diagram.point.x,
            //                                        mousePoint.y-diagram.point.y);
            Element ele = root.getElementByCoord(mousePoint.x-diagram.point.x,
                                                 mousePoint.y-diagram.point.y);
            // END KGU 2015-11-18
            if (ele != null)
            {
                // START KGU 2015-11-18: Avoid the impression of multiple selections
                if (mouseSelected != null && mouseSelected.root != null)
                {
                	mouseSelected.root.setSelected(false);
                }
                // END KGU 2015-11-18
                mouseSelected = diagram;
                mouseRelativePoint = new Point(mousePoint.x-mouseSelected.point.x,
                                               mousePoint.y-mouseSelected.point.y);
                // START KGU 2015-11-18: We didn't select anything, so there is nothing to unselect 
                //root.selectElementByCoord(-1, -1);
                // END KGU 2015-11-18
                root.setSelected(true);
            }

        }
        repaint();
    }

    public void mouseReleased(MouseEvent e)
    {
        mousePressed = false;
        // START KGU 2015-11-18: For consistency reasons, the selected diagram has to be unselected, too
        if (mouseSelected != null && mouseSelected.root != null)
        {
        	mouseSelected.root.setSelected(false);
        }
        // END KGU 2015-11-18
        mouseSelected = null;
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mouseDragged(MouseEvent e)
    {
    	// START KGU#85 2015-11-18
        Rectangle rect = new Rectangle(e.getX(), e.getY(), 1, 1);
        if (e.getX() > 0 && e.getY() > 0)	// Don't let drag beyond the scrollable area
        {
        	scrollRectToVisible(rect);
        // END KGU#85 2015-11-18

        	if (mousePressed == true)
        	{
        		if (mouseSelected!=null)
        		{
        			mouseSelected.point.setLocation(e.getPoint().x-mouseRelativePoint.x,
        					e.getPoint().y-mouseRelativePoint.y);
        			// START KGU#85 2015-11-18
        			adaptLayout();
        			// END KGU#85 2015-11-18
        			repaint();
        		}
        	}
        // START KGU#85 2015-11-18
        }
        // END KGU#85 2015-11-18
    }

    public void mouseMoved(MouseEvent e)
    {
    }

    public void update(Root source)
    {
        // START KGU#85 2015-11-18
        adaptLayout();
        // END KGU#85 2015-11-18
        this.repaint();
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
    private Diagram findDiagram(Root root, boolean identityCheck)
    // END KGU#119 2016-01-02
    {
    	Diagram owner = null;
    	if (this.diagrams != null) {
    		for(int d = 0; owner == null && d < this.diagrams.size(); d++)
    		{
    			Diagram diagram = this.diagrams.get(d);
    			// START KGU#119 2016-01-02: Bugfix #78 When loading diagrams we ought to check for equality only
    			//if (diagram.root == root)
    			if (identityCheck && diagram.root == root ||
    					!identityCheck && diagram.root.equals(root))
       			// END KGU#119 2016-01-02
    			{
    				owner = diagram;	// Will leave the loop
    			}
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
    	// START KGU#119 2016-01-02: Bugfix #78 - we only check for identity here, not for structural equality
    	//Diagram owner = findDiagram(oldRoot);
    	Diagram owner = findDiagram(oldRoot, true);
    	// END KGU#119 2016-01-02
    	if (owner != null) {
    		oldRoot.removeUpdater(this);
    	// START KGU#88 2015-11-24: Protect the Root if diagram is pinned
    	}
    	if (owner != null && !owner.isPinned)
    	{
    	// END KGU#88 2015-11-24
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
    	}
    }
    // END KGU#48 2015-10-17
    
    // START KGU#2 2015-10-17: Prepares the execution of a registered NSD as subroutine
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.executor.IRoutinePool#findRoutinesByName(java.lang.String)
     */
    @Override
    public Vector<Root> findRoutinesByName(String rootName)
    {
    	Vector<Root> functions = new Vector<Root>();
    	if (this.diagrams != null) {
    		for (int d = 0; d < this.diagrams.size(); d++)
    		{
    			Diagram diagram = this.diagrams.get(d);
    			if (rootName.equals(diagram.root.getMethodName()))
    			{
    				functions.add(diagram.root);
    			}
    		}
    	}
    	return functions;
    }
    // END KGU#2 2015-10-17
    
    // START KGU#2 2015-11-24
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.executor.IRoutinePool#findRoutinesBySignature(java.lang.String, int)
     */
    @Override
    public Vector<Root> findRoutinesBySignature(String rootName, int argCount)
    {
    	Vector<Root> functionsAny = findRoutinesByName(rootName);
    	Vector<Root> functions = new Vector<Root>();
    	for (int i = 0; i < functionsAny.size(); i++)
    	{
    		Root root = functionsAny.get(i);
   			if (!root.isProgram && root.getParameterNames().count() == argCount)
   			{
   				functions.add(root);
    		}
    	}
    	return functions;
    }
    // END KGU#2 2015-11-24

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



    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}

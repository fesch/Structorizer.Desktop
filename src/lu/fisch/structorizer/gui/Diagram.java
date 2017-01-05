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

/*
 ******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents the visual diagram itself.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Kay Gürtzig     2015.10.09      Colour setting will now duly be registered as diagram modification
 *                      2015.10.11      Comment popping repaired by proper subclassing of getElementByCoord
 *                                      Listener method MouseExited now enabled to drop the sticky comment popup
 *      Kay Gürtzig     2015.11.08      Parser preferences for FOR loops enhanced (KGU#3)
 *      Kay Gürtzig     2015.11.22      Selection of Subqueue subsequences or entire Subqueues enabled
 *                                      thus allowing collective operations like delete/cut/copy/paste (KGU#87).
 *      Kay Gürtzig     2015.11.24      Method setRoot() may now refuse the replacement (e.g. on cancelling
 *                                      the request to save recent changes)
 *      Kay Gürtzig     2015.11.29      New check options added to analyserNSD()
 *      Kay Gürtzig     2015.12.02      Bugfix #39 (KGU#91)
 *      Kay Gürtzig     2015.12.04      Bugfix #40 (KGU#94): With an error on saving, the recent file was destroyed
 *      Kay Gürtzig     2015.12.16      Bugfix #63 (KGU#111): Error message on loading failure
 *      Kay Gürtzig     2016.01.02      Bugfix #85 (KGU#120): Root changes are also subject to undoing/redoing
 *      Kay Gürtzig     2016.01.03      Issue #65 (KGU#123): Collapsing/expanding from menu, autoscroll enabled 
 *      Kay Gürtzig     2016.01.11      Bugfix #102 (KGU#138): clear selection on delete, undo, redo 
 *      Kay Gürtzig     2016.01.15      Enh. #110: File open dialog now selects the NSD filter
 *      Kay Gürtzig     2016.01.21      Bugfix #114: Editing restrictions during execution, breakpoint menu item
 *      Kay Gürtzig     2016.02.03      Bugfix #117: Title and button update on root replacement (KGU#149)
 *      Kay Gürtzig     2016.03.02      Bugfix #97: Reliable selection mechanism on dragging (KGU#136)
 *      Kay Gürtzig     2016.03.08      Bugfix #97: Drawing info invalidation now involves Arranger (KGU#155)
 *      Kay Gürtzig     2016.03.16      Bugfix #131: Precautions against replacement of Root under execution (KGU#158)
 *      Kay Gürtzig     2016.03.21      Enh. #84: FOR-IN loops considered in editing and parser preferences (KGU#61)
 *      Kay Gürtzig     2016-04-01      Issue #143 (comment popup off on editing etc.), Issue #144 (preferred code generator)
 *      Kay Gürtzig     2016-04-04      Enh. #149: Characterset configuration for export supported
 *      Kay Gürtzig     2016-04-05      Bugfix #155: Selection must be cleared in newNSD()
 *      Kay Gürtzig     2016.04.07      Enh. #158: Moving selection as cursor key actions (KGU#177)
 *      Kay Gürtzig     2016-04-14      Enh. #158: moveSelection() now updates the scroll view (KGU#177)
 *      Kay Gürtzig     2016-04-19      Issue #164 (no selection heir on deletion) and #165 (inconsistent unselection)
 *      Kay Gürtzig     2016-04-23      Issue #168 (no selection heir on cut) and #169 (no selection on start/undo/redo)
 *      Kay Gürtzig     2016-04-24      Bugfixes for issue #158 (KGU#177): Leaving the body of Parallel, Forever etc. downwards,
 *                                      button state update was missing.
 *      Kay Gürtzig     2016-04-24      Issue #169 accomplished: selection on start / after export
 *      Kay Gürtzig     2016-05-02      Bugfix #184: Imported root must be set changed.
 *      Kay Gürtzig     2016-05-08      Issue #185: Import of multiple roots per file (collected in Arranger, KGU#194)
 *      Kay Gürtzig     2016.07.06      Enh. #188: New method transmuteNSD() for element conversion (KGU#199)
 *      Kay Gürtzig     2016.07.19      Enh. #192: File name proposals slightly modified (KGU#205)
 *      Kay Gürtzig     2016.07.20      Enh. #160: New export option genExportSubroutines integrated (KGU#178)
 *      Kay Gürtzig     2016.07.21      Enh. #197: Selection may be expanded by Shift-Up and Shift-Down (KGU#206)
 *      Kay Gürtzig     2016.07.25      Enh. #158 / KGU#214: selection traversal accomplished for un-boxed Roots,
 *                                      and FOREVER / non-DIN FOR loops
 *      Kay Gürtzig     2016.07.26      Bugfix #204: Modified ExportOptionDialoge API (for correct sizing)
 *      Kay Gürtzig     2016.07.28      Bugfix #208: Modification in setFunction(), setProgram(), and exportPNG()
 *                                      Bugfix #209: exportPNGmulti() corrected
 *      Kay Gürtzig     2016.07.31      Issue #158 Changes from 2016.07.25 partially withdrawn, additional restrictions
 *      Kay Gürtzig     2016.08.01      Issue #213: FOR loop transmutation implemented
 *                                      Enh. #215: Breakpoint trigger counters added (KGU#213)
 *      Kay Gürtzig     2016.08.12      Enh. #231: Analyser checks rorganised to arrays for easier maintenance
 *      Kay Gürtzig     2016.09.09      Issue #213: preWhile and postWhile keywords involved in FOR loop transmutation
 *      Kay Gürtzig     2016.09.11      Issue #213: Resulting selection wasn't highlighted
 *      Kay Gürtzig     2016.09.13      Bugfix #241: Modification in showInputBox()
 *      Kay Gürtzig     2016.09.15      Issue #243: Forgotten message box texts included in localization,
 *                                      Bugfix #244: Flaws in the save logic mended
 *      Kay Gürtzig     2016.09.17      Issue #245: Message box for failing browser call in updateNSD() added.
 *      Kay Gürtzig     2016.09.21      Issue #248: Workaround for legacy Java versions (< 1.8) in editBreakTrigger()
 *      Kay Gürtzig     2016.09.24      Enh. #250: Several modifications around showInputBox()
 *      Kay Gürtzig     2016.09.25      Enh. #253: D7Parser.keywordMap refactoring done, importOptions() added.
 *      Kay Gürtzig     2016.09.26      Enh. #253: Full support for diagram refactoring implemented.
 *      Kay Gürtzig     2016.10.03      Enh. #257: CASE element transmutation (KGU#267), enh. #253 revised
 *      Kay Gürtzig     2016.10.06      Minor improvements in FOR and CALL transmutations (enh. #213/#257)
 *      Kay Gürtzig     2016.10.06      Bugfix #262: Selection and dragging problems after insertion, undo, and redo
 *      Kay Gürtzig     2016.10.07      Bugfix #263: "Save as" now updates the current directory
 *      Kay Gürtzig     2016.10.11      KGU#280: field isArrangerOpen replaced by a method (due to volatility)
 *      Kay Gürtzig     2016.10.13      Enh. #270: Functionality for the disabling of elements
 *      Kay Gürtzig     2016.11.06      Issue #279: All references to method HashMap.getOrDefault() replaced
 *      Kay Gürtzig     2016.11.09      Issue #81: Scale factor no longer rounded, Update font only scaled if factor > 1
 *      Kay Gürtzig     2016.11.15      Enh. #290: Opportunities to load arrangements via openNSD() and FilesDrop
 *      Kay Gürtzig     2016.11.16      Bugfix #291: upward cursor traversal ended in REPEAT loops
 *      Kay Gürtzig     2016.11.17      Bugfix #114: Prerequisites for editing and transmutation during execution revised
 *      Kay Gürtzig     2016.11.18/19   Issue #269: Scroll to the element associated to a selected Analyser error
 *      Kay Gürtzig     2016.11.21      Issue #269: Focus alignment improved for large elements
 *      Kay Gürtzig     2016.12.02      Enh. #300: Update notification mechanism
 *      Kay Gürtzig     2016.12.12      Enh, #305: Infrastructure for Arranger root list
 *      Kay Gürtzig     2016.12.28      Enh. #318: Backsaving of unzipped diagrams to arrz file
 *      Kay Gürtzig     2017.01.04      Bugfix #321: Signatures of saveNSD(), doSaveNSD(), saveAsNSD() and zipToArrz() enhanced
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      
 *      2016.07.31 (Kay Gürtzig, #158)
 *      - It turned out that circular horizontal selection move is not sensible. It compromises usability
 *        rather than it helps. With active horizontal mouse scrolling the respective diagram margin is
 *        so quickly reached that a breathtaking rotation evolves - no positioning is possible. Even with
 *        cursor keys you fall too rapidly into the margin trap, just to be kicked to a totally different
 *        place. This makes navigation rather hazardous. Selection chain will end at the left or right
 *        margin now, giving pause for consideration.
 *        Moving inwards the diagram from the selected Root will still work.
 *
 ******************************************************************************************************
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.datatransfer.*;

import net.iharder.dnd.*; //http://iharder.sourceforge.net/current/java/filedrop/

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.*;
import javax.imageio.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.freehep.graphicsio.emf.*;
import org.freehep.graphicsio.pdf.*;
import org.freehep.graphicsio.swf.*;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.generators.*;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.turtle.TurtleBox;

import org.freehep.graphicsio.svg.SVGGraphics2D;

@SuppressWarnings("serial")
public class Diagram extends JPanel implements MouseMotionListener, MouseListener, Printable, MouseWheelListener, ClipboardOwner, ListSelectionListener
{

	// START KGU#48 2015-10-18: We must be capable of preserving consistency when root is replaced by the Arranger
    //public Root root = new Root();
    private Root root = new Root();
    // END KGU 2015-10-18
    private TurtleBox turtle = null; //

    private Element selected = null;

    private boolean mouseMove = false;
    private int mouseX = -1;
    private int mouseY = -1;
    private Element selectedDown = null;
    private Element selectedUp = null;
    private Element selectedMoved = null;
    private int selX = -1;
    private int selY = -1;
    private int mX = -1;
    private int mY = -1;
    
    private NSDController NSDControl = null;
    
    // START KGU#2 2015-11-24 - KGU#280 2016-10-11 replaced by method consulting the Arranger class
    // Dependent Structorizer instances may otherwise be ignorant of the Arranger availability
    //public boolean isArrangerOpen = false;
    static public boolean isArrangerOpen()
    {
    	return Arranger.hasInstance();
    }
    // END KGU#2 2015-11-24

    private JList<DetectedError> errorlist = null;
    private JList<Root> diagramIndex = null;

    private Element eCopy = null;

    public File currentDirectory = new File(System.getProperty("user.home"));
    public File lastExportDir = null;
    // START KGU#170 2016-04-01: Enh. #144 maintain a favourite export generator
    private String prefGeneratorName = "";
    // END KGU#170 2016-04-01
    
    // START KGU#300 2016-12-02: Enh. #300 - update notification settings
    public boolean retrieveVersion = false;
    // END KGU#300 2016-12-02
	// START KGU#305 2016-12-12: Enh. #305
	private boolean show_ARRANGER_INDEX = false;	// Arranger index visible?
	// END KGU#305 2016-12-12

    // recently opened files
    protected Vector<String> recentFiles = new Vector<String>();

    // popup for comment
    private JLabel lblPop = new JLabel("",SwingConstants.CENTER);
    private JPopupMenu pop = new JPopupMenu();

    // toolbar management
    public Vector<MyToolbar> toolbars = new Vector<MyToolbar>();
    
	/*****************************************
	 * CONSTRUCTOR
     *****************************************/
    public Diagram(Editor _editor, String _string)
    {
        super(true);
        this.setDoubleBuffered(true);	// we don't need double buffering, because the drawing
                                                                        // itself does it allready!
        this.setBackground(Color.LIGHT_GRAY);

        if(_editor!=null)
        {
            errorlist=_editor.errorlist;
            diagramIndex = _editor.diagramIndex;
            NSDControl = _editor;
        }
        create(_string);
    }


    // START KGU#48,KGU#49 2015-10-19: Make sure that replacing root by Arranger doesn't harm anything or risks losses
	/**
	 * @return the currently managed Root
	 */
	public Root getRoot() {
		return root;
	}

	/**
	 * @param root the Root to set
	 * @return false if the user refuses to adopt the Root or the Root is being executed
	 */
	public boolean setRootIfNotRunning(Root root) {
		// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
		if (!this.checkRunning()) return false;	// Don't proceed if the root is being executed
		// END KGU#157 2016-03-16

		return setRoot(root, true);
	}
	
	public boolean setRoot(Root root, boolean askToSave) {
		if (root != null)
		{
			// Save if something has been changed
			if (!saveNSD(askToSave))
			{
				// Abort this if the user cancels the save request
				return false;
			}
			this.unselectAll();

			boolean hil = this.root.hightlightVars;
			this.root = root;
			root.hightlightVars = hil;
			//System.out.println(root.getFullText().getText());
			//root.getVarNames();
			//root.hasChanged = true;
			// START KGU#183 2016-04-23: Issue #169
			selected = root.findSelected();
			if (selected == null)
			{
				selected = root;
				root.setSelected(true);
			}
			// END KGU#183 2016-04-23
			redraw();
			analyse();
			// START KGU#149 2016-02-03: Bugfix #117
			doButtons();
			// END KGU#149 2016-02-03
		}
		return true;
	}
	// END KGU#48,KGU#49 2015-10-18

    /*
	// START KGU#2 2015-11-24: Allows the Executor to localize the Control frame
	public String getLang()
	{
		return NSDControl.getLang();
	}
	// END KGU#2 2015-11-24
    */    
	
    public boolean getAnalyser()
    {
        return Element.E_ANALYSER;
    }

    private void create(String _string)
    {
		// load different things from INI-file
		Element.loadFromINI();
		D7Parser.loadFromINI();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		// START KGU#123 2016-01-04: Enh. #87, Bugfix #65
		//this.addMouseWheelListener(this);
		if (Element.E_WHEELCOLLAPSE)
		{
			this.addMouseWheelListener(this);
		}
		// END KGU#123 2016-01-04

		new FileDrop( this, new FileDrop.Listener()
			{
				public void  filesDropped( java.io.File[] files )
				{
					// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
					if (!checkRunning()) return;	// Don't proceed if the root is being executed
					// END KGU#157 2016-03-16
					//boolean found = false;
					for (int i = 0; i < files.length; i++)
					{
						String filename = files[i].toString();
						String filenameLower = filename.toLowerCase();

						if(filenameLower.endsWith(".nsd"))
						{
							/*
							// only save if something has been changed
							saveNSD(true);

							// load the new file
							NSDParser parser = new NSDParser();
							root = parser.parse(filename);
							root.filename=filename;
							currentDirectory = new File(filename);
							redraw();*/
							openNSD(filename);
						}
						// START KGU#289 2016-11-15: Enh. #290 (Arranger file support)
						else if (filenameLower.endsWith(".arr")
								||
								filenameLower.endsWith(".arrz"))
						{
							loadArrangement(files[i]);
						}
						// END KGU#289 2016-11-15
						else if (
								filenameLower.endsWith(".mod")
								||
								filenameLower.endsWith(".pas")
								||
								filenameLower.endsWith(".dpr")
								||
								filenameLower.endsWith(".lpr")
								)
						{
							// save (only if something has been changed)
							saveNSD(true);
							// load and parse source-code
							D7Parser d7 = new D7Parser("D7Grammar.cgt");
							Root rootNew = d7.parse(filename);
							if (d7.error.equals(""))
							{
								setRootIfNotRunning(rootNew);
								currentDirectory = new File(filename);
								//System.out.println(root.getFullText().getText());
							}
							else
							{
								// show error
								JOptionPane.showMessageDialog(null,
										d7.error,
										Menu.msgTitleParserError.getText(),
										JOptionPane.ERROR_MESSAGE);
							}

							redraw();
						}


					}
				}
			}
				);

		root.setText(StringList.getNew(_string));

        // START KGU#123 2016-01-04: Issue #65
        this.setAutoscrolls(true);
        // END KGU#123 2016--01-04

        // popup for comment
		JPanel jp = new JPanel();
		jp.setOpaque(true);
		lblPop.setPreferredSize(new Dimension(30,12));
		jp.add(lblPop);
		pop.add(jp);
		
		// START KGU#182 2016-04-24: Issue #169
		selected = root;
		root.setSelected(true);
		// END KGU#182 2016-04-24
		
		// Attempt to find out what provokes the NullPointerExceptions on start
		//System.out.println("**** " + this + ".create() ready!");
	}

	public void hideComments()
	{
		pop.setVisible(false);
	}

	public void mouseMoved(MouseEvent e)
	{
		//System.out.println("MouseMoved at (" + e.getX() + ", " + e.getY() + ")");
		// KGU#91 2015-12-04: Bugfix #39 - Disabled
        //if(Element.E_TOGGLETC) root.setSwitchTextAndComments(true);
		if(e.getSource()==this && NSDControl!=null)
		{
        	boolean popVisible = false;
        	if (Element.E_SHOWCOMMENTS==true && ((Editor) NSDControl).popup.isVisible()==false)
        	{
				//System.out.println("=================== MOUSE MOVED (" + e.getX()+ ", " +e.getY()+ ")======================");
        		// START KGU#25 2015-10-11: Method merged with selectElementByCoord
        		//Element selEle = root.getElementByCoord(e.getX(),e.getY());
        		Element selEle = root.getElementByCoord(e.getX(), e.getY(), false);
        		// END KGU#25 2015-10-11
				//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE MOVED >>>>> " + selEle + " <<<<<<<<<<<<<<<<<<<<<");

        		if (selEle != null &&
        				!selEle.getComment(false).getText().trim().isEmpty())
        		{
        			// START KGU#199 2016-07-07: Enh. #188 - we must cope with combined comments now
        			//StringList comment = selEle.getComment(false);
        			StringList comment = StringList.explode(selEle.getComment(false), "\n");
        			comment.removeAll("");	// Don't include empty lines here
        			// END KGU#199 2016-07-07
        			String htmlComment = "<html>"+BString.replace(BString.encodeToHtml(comment.getText()),"\n","<br>")+"</html>";
        			if(!lblPop.getText().equals(htmlComment))
        			{
        				lblPop.setText(htmlComment);
        			}
        			int maxWidth = 0;
        			int si = 0;
        			for (int i = 0; i < comment.count(); i++)
        			{
        				if (maxWidth < comment.get(i).length())
        				{
        					maxWidth = comment.get(i).length();
        					si=i;
        				}
        			}
        			lblPop.setPreferredSize(
        					new Dimension(
        							8 + lblPop.getFontMetrics(lblPop.getFont()).
        							stringWidth(comment.get(si)),
        							comment.count()*16
        							)
        					);

        			int x = ((JComponent) e.getSource()).getLocationOnScreen().getLocation().x;
        			int y = ((JComponent) e.getSource()).getLocationOnScreen().getLocation().y;
        			pop.setLocation(x+e.getX(),
        					y+e.getY()+16);
        			popVisible = true;
        		}
        	}
        	pop.setVisible(popVisible);
		}
		// KGU#91 2015-12-04: Bugfix #39 - Disabled
        //if(Element.E_TOGGLETC) root.setSwitchTextAndComments(false);
	}

	public void mouseDragged(MouseEvent e)
	{
		if(e.getSource()==this)
		{
			// START KGU#123 2016-01-04: Issue #65 - added for autoscroll behaviour
			Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
	        ((JPanel)e.getSource()).scrollRectToVisible(r);
	        // END KGU#123 2016-01-04

	        // START KGU#25 2015-10-11: Method merged with getElementByCoord(int,int)
			//Element bSome = root.selectElementByCoord(e.getX(),e.getY());
			Element bSome = root.getElementByCoord(e.getX(), e.getY(), true);
			// END KGU#25 2015-10-11

			if (bSome != null)
			{
				mX = e.getX();
				mY = e.getY();
				//System.out.println("DRAGGED "+mX+" "+mY);
				/*System.out.println("DRAGGED ("+e.getX()+", "+e.getY()+") >> " +
						bSome + " >> " + selectedDown);
						/**/

				bSome.setSelected(true);
				//System.out.println("selected = " + bSome);
				//System.out.println("selectedDown = " + selectedDown);
				//System.out.println("selectedUp = " + selectedUp);
				if (selectedDown != null) selectedDown.setSelected(true);

				boolean doRedraw = false;
                
				if ((selectedDown!=null) && (e.getX()!=mouseX) && (e.getY()!=mouseY) && (selectedMoved!=bSome))
				{
					mouseMove=true;
					if(selectedDown.getClass().getSimpleName().equals("Root") ||
					   selectedDown.getClass().getSimpleName().equals("Subqueue") ||
					   bSome.getClass().getSimpleName().equals("Root") ||
					   //root.checkChild(bSome, selectedDown))
					   bSome.isDescendantOf(selectedDown))
					{
						Element.E_DRAWCOLOR=Color.RED;
					}
					else
					{
						Element.E_DRAWCOLOR=Color.GREEN;
					}
					/*
					 selectedDown.draw(new Canvas((Graphics2D)this.getGraphics()), selectedDown.rect);
					 if(bSome!=null)
					 {
					 bSome.draw(new Canvas((Graphics2D)this.getGraphics()), bSome.rect);

					 }
					 */
					doRedraw= true;
				}

				if (selX != -1 && selY != -1)
				{
					doRedraw = true;
				}

				if (doRedraw)
					redraw();

			}
			selectedMoved = bSome;
		}
	}

	public void mousePressed(MouseEvent e)
	{
		if(e.getSource()==this)
		{
			//System.out.println("Pressed");
			mouseX = e.getX();
			mouseY = e.getY();

			Element.E_DRAWCOLOR = Color.YELLOW;
			// START KGU#25 2015-10-11: Method merged with getElementByCoord(int,int)
			//Element ele = root.selectElementByCoord(e.getX(),e.getY());
			Element ele = root.getElementByCoord(e.getX(), e.getY(), true);
			// END KGU#25 2015-10-11

			// KGU#87: Maintain a selected sequence on right mouse button click 
			if (e.getButton() == MouseEvent.BUTTON3 && selected instanceof IElementSequence &&
					(ele == null || ((IElementSequence)selected).getIndexOf(ele) >= 0))
			{
				// Restore the selection flags (which have been reduced to ele by root.getElementByCoord(...))
				// START KGU 2016-01-09: Bugfix #97 (possibly) - ele may be null here!
				//ele.setSelected(false);
				if (ele != null) ele.setSelected(false);
				// END KGU 2016-01-09
				selected.setSelected(true);
				redraw();
			}
			else if (ele != null)
			{
				// START KGU#136 2016-03-02: Bugfix #97 - Selection wasn't reliable
				ele.setSelected(true);
				// END KGU#136 2016-03-02
				mX = mouseX;
				mY = mouseY;
				// START KGU#136 2016-03-02: Bugfix #97 - we must get the element corner
				//selX = mouseX-ele.getRect().left;
				//selY = mouseY-ele.getRect().top;
				Rect topLeft = ele.getRectOffDrawPoint();
				selX = mouseX - topLeft.left;
				selY = mouseY - topLeft.top;
				// END KGU#136 2016-03-02

				// KGU#87: Expansion to entire subqueue (induced by Alt key held down)?
				if (e.isAltDown() && ele.parent instanceof Subqueue &&
						((Subqueue)ele.parent).getSize() > 1)
				{
					((Subqueue)ele.parent).setSelected(true);
					selected = ele.parent;
					// In case someone wants to drag then let it just be done for the single element
					// (we don't allow dynamically to move a sequence - the user may better cut and paste)
					selectedDown = ele;
					selectedUp = ele;
					redraw();						
				}
				else if (ele != selected)
				{
					// START KGU#87 2015-11-23: If an entire Subqueue had been selected, reset the flags 
					if (selected instanceof Subqueue)
					{
						selected.setSelected(false);
					}
					if (e.isShiftDown() && selected != null &&
							ele.parent instanceof Subqueue &&
							ele.parent == selected.parent)
					{
						// Select the subrange
						//System.out.println("Selected range of " + ele.parent + " " +
						//((Subqueue)ele.parent).getIndexOf(ele) + " - " +
						//((Subqueue)ele.parent).getIndexOf(selected));
						selected.setSelected(false);
						selected = new SelectedSequence(selected, ele);
						selected.setSelected(true);
						redraw();
						selectedDown = ele;
						selectedUp = ele;
					}
					else
					{
					// END KGU#87 2015-11-23
						ele.setSelected(true);
						// START KGU#87 2015-11-23: Ensure a redrawing after a Subqueue had been selected 
						//selected=ele;
						//if(selectedDown!=ele) 
						if (selectedDown != ele || selected instanceof IElementSequence)
						// END KGU#87 2015-11-23
						{
							redraw();
						}
						selected = ele;
						selectedDown = ele;
						selectedUp = ele;
					// START KGU#87 2015-11-23: Original code just part of the else branch
					}
					// END KGU#87 2015-11-23
				}
				//redraw();
			}
			// START KGU#180 2016-04-15: Bugfix #165 - detection didn't work properly
			else /* ele == null */
			{
				selected = null;
				// FIXME: May selectedDown and selectedUp still hold a former selection? 
				redraw();
			}
			// END KGU#180 2016-04-15

			if (selected != null)
			{
				if ( !selected.getClass().getSimpleName().equals("Subqueue") &&
					!selected.getClass().getSimpleName().equals("Root") )
				{
					mouseMove = false;
				}
			}

			if (NSDControl != null) NSDControl.doButtons();
		}
    }

    public void mouseReleased(MouseEvent e)
	{
    	if (e.getSource()==this)
    	{
    		//System.out.println("Released");
    		boolean doDraw = false;

    		if(selX!=-1 && selY!=-1 && selectedDown!=null)
    		{
    			selX = -1;
    			selY = -1;
    			doDraw=true;
    		}

    		if ((mouseMove==true) && (selectedDown!=null))
			{
				Element.E_DRAWCOLOR=Color.YELLOW;
				if ( !selectedDown.getClass().getSimpleName().equals("Subqueue") &&
						!selectedDown.getClass().getSimpleName().equals("Root"))
				{
					//System.out.println("=================== MOUSE RELEASED 1 (" + e.getX()+ ", " +e.getY()+ ")======================");
					// START KGU#25 2015-10-11: Method merged with getElementByCoord(int,int)
					//selectedUp = root.selectElementByCoord(e.getX(),e.getY());
					selectedUp = root.getElementByCoord(e.getX(), e.getY(), true);
					// END KGU#25 2015-10-11
					//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE RELEASED 1 >>>>>>> " + selectedUp + " <<<<<<<<<<<<<<<<<<<<<<");
					if (selectedUp != null)
					{
						selectedUp.setSelected(false);
						if ( !selectedUp.getClass().getSimpleName().equals("Root") &&
						   selectedUp != selectedDown &&
						   //root.checkChild(selectedUp,selectedDown)==false
						   !selectedUp.isDescendantOf(selectedDown)
						   )
						{
							root.addUndo();
							NSDControl.doButtons();

							// START KGU#87 2015-11-22: Subqueues should never be moved but better prevent...
							//root.removeElement(selectedDown);
							if (!(selectedDown instanceof Subqueue))
							{
								root.removeElement(selectedDown);
							}
							// END KGU#87 2015-11-22
							selectedUp.setSelected(false);
							root.addAfter(selectedUp, selectedDown);
							// START KGU'87 2015-11-22: See above
							//selectedDown.setSelected(true);
							if (!(selectedDown instanceof Subqueue))
							{
								selectedDown.setSelected(true);
							}
							else
							{
								((Subqueue)selectedDown).clear();
								selectedDown.setSelected(false);
							}
							// END KGU#87 2015-11-22
							doDraw=true;
						}
						else
						{
							selectedUp.setSelected(false);
							selectedDown.setSelected(true);
							doDraw=true;
						}
					}
				}
				else
				{
					//System.out.println("=================== MOUSE RELEASED 2 (" + e.getX()+ ", " +e.getY()+ ")======================");
					// START KGU#25 2015-10-11: Method merged with getElementByCoord(int,int)
					//selectedUp = root.selectElementByCoord(e.getX(),e.getY());
					selectedUp = root.getElementByCoord(e.getX(), e.getY(), true);
					// END KGU#25 2015-10-11
					//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE RELEASED 2 >>>>>>> " + selectedUp + " <<<<<<<<<<<<<<<<<<<<<<");
					if (selectedUp!=null) selectedUp.setSelected(false);
					doDraw=true;
				}
			}

			mouseMove=false;

			if(doDraw==true)
			{
				redraw();
				analyse();
			}

			if (NSDControl!=null) NSDControl.doButtons();
		}
	}

    public void mouseEntered(MouseEvent e)
	{
	}

    public void mouseExited(MouseEvent e)
	{
    	// START KGU#1 2015-10-11: We ought to get rid of that sticky popped comment!
    	this.hideComments();
    	// END KGU#1 2015-10-11
	}

    public void mouseClicked(MouseEvent e)
	{
    	// select the element
		if (e.getClickCount() == 1)
		{
			if (e.getSource()==this)
			{
                                //System.out.println("Clicked");
                                // KGU 2015-10-11: In case of reactivation replace the following by ...root.getElementByCoord(e.getX(),e.getY(),true); !
                                /*Element selly = root.selectElementByCoord(e.getX(),e.getY());
                                if(selly!=selected && selected!=null) 
                                {
                                    selected.setSelected(false);
                                }
                                selected=selly;

                                // redraw the diagram
                                //redraw();
                                // do the button thing
                                if(NSDControl!=null) NSDControl.doButtons();
/**/
                               /*
                                // select the element
				Element selly = root.selectElementByCoord(e.getX(),e.getY());
				if(selected!=selly)
				{
                                        selected=selly;
                                        if(selected!=null)
                                        {
        					selected.setSelected(true);
                                        }
                                        // redra the diagram
                                        redraw();
                                        // do the button thing
                                        if(NSDControl!=null) NSDControl.doButtons();
				}*/
			}
			// START KGU#305 2016-12-15: Enh. #312 - content moved to method valueChanged(e) 
//			else if (e.getSource() == errorlist)
//			{
//				// an error list entry has been selected
//				if(errorlist.getSelectedIndex()!=-1)
//				{
//					// get the selected error
//					DetectedError err = root.errors.get(errorlist.getSelectedIndex()); 
//					Element ele = err.getElement();
//					if(ele!=null)
//					{
//						// deselect the previously selected element (if any)
//						if (selected!=null) {selected.setSelected(false);}
//						// select the new one
//						selected = ele;
//						ele.setSelected(true);
//						
//						// redraw the diagram
//						// START KGU#276 2016-11-18: Issue #269 - ensure the associated element be visible
//						//redraw();
//						redraw(ele);
//						// END KGU#276 2016-11-18
//						
//						// do the button thing
//						if(NSDControl!=null) NSDControl.doButtons();
//					}
//					// START KGU#220 2016-07-27: Draft for Enh. #207, but withdrawn
//					//else if (err.getError().equals(Menu.warning_1.getText()))
//					//{
//					//	this.toggleTextComments();
//					//}
//					// END KGU#200 2016-07-27
//				}
//			}
			// END KGU305 2016-12-15
			// START KGU#305 2016-12-12: Enh. #305
			else if (e.getSource() == diagramIndex)
			{
				Arranger.scrollToDiagram(diagramIndex.getSelectedValue(), true);
			}
			// END KGU#305 2016-12-12
		}
                // edit the element
		else if ((e.getClickCount() == 2))
		{
			if(e.getSource()==this)
			{
				// selected the right element
				//selected = root.selectElementByCoord(e.getX(),e.getY());
				// START KGU#87 2015-11-22: Don't edit non-empty Subqueues, reselect single element
				//if (selected != null)
//				if ((selected instanceof Subqueue) && ((Subqueue)selected).getSize() > 0)
//				{
//					selected = root.selectElementByCoord(e.getX(), e.getY());	// Is of little effect - often subqueues don't detect properly
//					redraw();
//					//System.out.println("Re-selected on double-click: " + selected + ((selected instanceof Subqueue) ? ((Subqueue)selected).getSize() : ""));
//				}
				// START KGU#143 2016-11-17: Issue #114 - don't edit elements under execution
				//if (selected != null && !((selected instanceof Subqueue) && ((Subqueue)selected).getSize() > 0))
				if (canEdit())
				// END KGU#143 2016-11-17
				// END KGU#87 2015-11-22
				{
					// edit it
					editNSD();
					selected.setSelected(true);
					// START KGU#276 2016-10-11: Issue #269 Attempt to focus the associated element - failed!
					//redraw();
					redraw(selected);	// Doesn't work properly
					// END KGU#276 2016-10-11
					// do the button thing
					if(NSDControl!=null) NSDControl.doButtons();
				}
			}
			else if (e.getSource() == errorlist)
			{
				// the error list has been clicked
				if(errorlist.getSelectedIndex()!=-1)
				{
					// select the right element
					selected = (root.errors.get(errorlist.getSelectedIndex())).getElement();
					// edit it
					editNSD();
					// do the button things
					if(NSDControl!=null) NSDControl.doButtons();
				}
			}
			// START KGU#305 2016-12-12: Enh. #305
			else if (e.getSource() == diagramIndex)
			{
				Root selectedRoot = diagramIndex.getSelectedValue();
				if (selectedRoot != null && selectedRoot != this.root) {
					this.setRootIfNotRunning(selectedRoot);
				}
				this.getParent().getParent().requestFocusInWindow();
			}
			// END KGU#305 2016-12-12
		}
	}

    // START KGU#143 2016-01-21: Bugfix #114 - We need a possibility to update buttons from execution status
    public void doButtons()
    {
    	if(NSDControl!=null) NSDControl.doButtons();
    }
    // END KGU#143 2016-01-21

    // START KGU#276 2016-10-09: Issue #269
    /**
     * Scroll to the given element and redraw the current diagram
     * @param element - the element to gain the focus
     */
    public void redraw(Element element)
    {
    	Rectangle rect = element.getRectOffDrawPoint().getRectangle();
    	Rectangle visibleRect = new Rectangle();
    	this.computeVisibleRect(visibleRect);
    	// START KGU#276 2016-11-19: Issue #269 Ensure wide elements be shown left-bound
    	if (rect.width > visibleRect.width &&
    			!(element instanceof Alternative || element instanceof Case))
    	{
    		rect.width = visibleRect.width;
    	}
    	// END KGU#276 2016-11-19
    	// START KGU#276 2016-11-21: Issue #269 Ensure high elements be shown top-bound
    	if (rect.height > visibleRect.height &&
    			!(element instanceof Instruction || element instanceof Parallel || element instanceof Forever))
    	{
    		// ... except for REPEAT loops, which are to be shown bottom-aligned
    		if (element instanceof Repeat)	{
    			rect.y += rect.height - visibleRect.height;
    		}
    		rect.height = visibleRect.height;
    	}
    	// END KGU#276 2016-11-21
    	scrollRectToVisible(rect);
    	redraw();	// This is to make sure the drawing rectangles are correct
    }
    // END KGU#276 2016-10-09
    
    public void redraw()
    {
    	if (root.hightlightVars==true)
    	{
    		root.getVarNames();
    	}

    	Rect rect = root.prepareDraw(this.getGraphics());
    	Dimension d = new Dimension(rect.right-rect.left,rect.bottom-rect.top);
    	this.setPreferredSize(d);
    	//this.setSize(d);
    	this.setMaximumSize(d);
    	this.setMinimumSize(d);
    	//this.setSize(new Dimension(rect.right-rect.left,rect.bottom-rect.top));
    	//this.validate();

    	((JViewport) this.getParent()).revalidate();

    	//redraw(this.getGraphics());
    	this.repaint();
    }

	public void redraw(Graphics _g)
	{
		// KGU#91 2015-12-04: Bugfix #39 - Disabled
        //if (Element.E_TOGGLETC) root.setSwitchTextAndComments(true);
		root.draw(_g);
        
		lu.fisch.graphics.Canvas canvas = new lu.fisch.graphics.Canvas((Graphics2D) _g);
		Rect rect;
		// FIXME: This "background filling" isn't necessary, at least not under windows
//		rect = new Rect(root.width+1,0,this.getWidth(),this.getHeight());
//		canvas.setColor(Color.LIGHT_GRAY);
//		canvas.fillRect(rect);
//		rect = new Rect(0,root.height+1,this.getWidth(),this.getHeight());
//		canvas.setColor(Color.LIGHT_GRAY);
//		canvas.fillRect(rect);
		// START KGU 2016-02-27: This area has already been filled twice
//		rect = new Rect(root.width+1,root.height+1,this.getWidth(),this.getHeight());
//		canvas.setColor(Color.LIGHT_GRAY);
//		canvas.fillRect(rect);
		// END KGU 2016-02-27
        
		// draw dragged element
		if (selX != -1 && selY != -1 && selectedDown!=null && mX!=mouseX && mY!=mouseY)
		{
			_g.setColor(Color.BLACK);
			// START KGU#136 2016-03-02: Bugfix #97 - It must not play any role where the diagram was drawn before
			//rect = selectedDown.getRect();
			//Rect copyRect = rect.copy();
			rect = selectedDown.getRectOffDrawPoint();
			// END KGU#136 2016-03-02
			int w = rect.right-rect.left;
			int h = rect.bottom-rect.top;
			rect.left = mX - selX;
			rect.top  = mY - selY;
			rect.right  = rect.left + w;
			rect.bottom = rect.top + h;
			((Graphics2D)_g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			selectedDown.draw(canvas, rect);
			((Graphics2D)_g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			// START KGU#136 2016-03-01: Bugfix #97 - this is no longer necessary
			//selectedDown.rect = copyRect;
			// END KGU#136 2016-03-01
			//System.out.println(selectedDown.getClass().getSimpleName()+"("+selectedDown.getText().getLongString()+
			//		") repositioned to ("+copyRect.left+", "+copyRect.top+")");
			//_g.drawRect(mX-selX, mY-selY, w, h);
		}/**/

		// KGU#91 2015-12-04: Bugfix #39 - Disabled
		//if (Element.E_TOGGLETC) root.setSwitchTextAndComments(false);
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if(root!=null)
		{
			redraw(g);
		}
	}
        
    // START KGU#155 2016-03-08: Some additional fixing for bugfix #97
	/**
	 * Invalidates the cached prepareDraw info of the current diagram (Root)
	 * (to be called on events with global impact on the size or shape of Elements)
	 * @param _all are Roots parked in the Arranger to be invalidated, too?
	 */
	public void resetDrawingInfo(boolean _all)
	{
		root.resetDrawingInfoDown();
		if (isArrangerOpen())
		{
			Arranger.getInstance().resetDrawingInfo(this.hashCode());
		}
	}
	// END KGU#155 2016-03-08

	public Element getSelected()
	{
		return selected;
	}
	
	// START KGU#87 2015-11-22: 
	public boolean selectedIsMultiple()
	{
		return (selected instanceof IElementSequence && ((IElementSequence)selected).getSize() > 0);
	}
	// END KGU#87 2015-11-22
	
    // START KGU#41 2015-10-11: Unselecting, e.g. before export, had left the diagram status inconsistent:
    // Though the selected status of the elements was unset, the references of the formerly selected
    // elements invisibly remained in the respective diagram attributes, possibly causing unwanted effects.
    // So this new method was introduced to replace the selectElementByCoord(-1,-1) calls.
    public void unselectAll()
    {
    	if (root != null)
    	{
    		root.selectElementByCoord(-1, -1);
    	}
    	selected = selectedUp = selectedDown = selectedMoved = null;
    	redraw();
    }
	// END KGU#41 2015-10-11

	/**
	* Method: print <p>
	*
	* This class is responsible for rendering a page using
	* the provided parameters. The result will be a grid
	* where each cell will be half an inch by half an inch.
	*
	* @param g a value of type Graphics
	* @param pageFormat a value of type PageFormat
	* @param page a value of type int
	* @return a value of type int
	*/
	public int print(Graphics g, PageFormat pageFormat, int page)
	{  
		if (page == 0)
		{
			Graphics2D g2d = (Graphics2D) g;

			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

			/*if (pageFormat.getOrientation() != PageFormat.PORTRAIT)
			{*/
				double sX = (pageFormat.getImageableWidth()-1)/root.width;
				double sY = (pageFormat.getImageableHeight()-1)/root.height;
				double sca = Math.min(sX,sY);
				if (sca>1) {sca=1;}
				g2d.scale(sca,sca);
			/*}
			else
			{
				double sX = (pageFormat.getImageableWidth()-1)/root.width;
				double sY = (pageFormat.getImageableHeight()-1)/root.height;
				double sca = Math.min(sX,sY);
				//if (sca>1) {sca=1;}
				g2d.scale(sca,sca);
			}*/

			root.draw(g);

			return (PAGE_EXISTS);
		}
		else
		{
			return (NO_SUCH_PAGE);
		}
	}

	/*****************************************
	 * New method
	 *****************************************/
	public void newNSD()
	{
		// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
		if (!this.checkRunning()) return;	// Don't proceed if the root is being executed
		// END KGU#157 2016-03-16

		// START KGU#48 2015-10-17: Arranger support
		Root oldRoot = root;
		// END KGU#48 2015-10-17
		// only save if something has been changed
		saveNSD(true);

		// create an empty diagram
		boolean HV = root.hightlightVars;
		root = new Root();
		root.hightlightVars=HV;
		// START KGU 2015-10-29: This didn't actually make sense
		//root.hasChanged=true;
		// END KGU 2015-10-29
		// START KGU#183 2016-04-23: Bugfix #155, Issue #169
		// We must not forget to clear a previous selection
		//this.selected = this.selectedDown = this.selectedUp = null;
		this.selectedDown = this.selectedUp = null;
		this.selected = root;
		root.setSelected(true);
		// END KGU#183 2016-04-23
		redraw();
		analyse();
		// START KGU#48 2015-10-17: Arranger support
		if (oldRoot != null)
		{
			oldRoot.notifyReplaced(root);
		}
		// END KGU#48 2015-10-17
	}


	/*****************************************
	 * Open method
	 *****************************************/
	public void openNSD()
	{
		// START KGU 2015-10-17: This will be done by openNSD(String) anyway - once is enough!
		// only save if something has been changed
		//saveNSD(true);
		// END KGU 2015-10-17

		// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
		if (!this.checkRunning()) return;	// Don't proceed if the root is being executed
		// END KGU#157 2016-03-16

		// open an existing file
		// create dialog
		JFileChooser dlgOpen = new JFileChooser();
		dlgOpen.setDialogTitle(Menu.msgTitleOpen.getText());
		// set directory
		if(root.getFile()!=null)
		{
			dlgOpen.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgOpen.setCurrentDirectory(currentDirectory);
		}
		// config dialogue
		// START KGU 2016-01-15: Enh. #110 - select the provided filter
		//dlgOpen.addChoosableFileFilter(new StructogramFilter());
		StructogramFilter filter = new StructogramFilter();
		dlgOpen.addChoosableFileFilter(filter);
		// START KGU#289 2016-11-15: Enh. #290 (allow arrangement files to be selected)
		dlgOpen.addChoosableFileFilter(new ArrFilter());
		dlgOpen.addChoosableFileFilter(new ArrZipFilter());
		// END KGU#289 2016-11-15
		dlgOpen.setFileFilter(filter);
		// END KGU 2016-01-15
		// show & get result
		int result = dlgOpen.showOpenDialog(this);
		// react on result
		if (result == JFileChooser.APPROVE_OPTION)
		{
			/*
			NSDParser parser = new NSDParser();
			root = parser.parse(dlgOpen.getSelectedFile().toURI().toString());
			root.filename=dlgOpen.getSelectedFile().getAbsoluteFile().toString();
			currentDirectory = new File(root.filename);
			redraw();
			*/
			// START KGU#289/KGU#316 2016-11-15/2016-12-28: Enh. #290/#318 (Arranger file support)
			//openNSD(dlgOpen.getSelectedFile().getAbsoluteFile().toString());
			openNsdOrArr(dlgOpen.getSelectedFile().getAbsoluteFile().toString());
			// END KGU#289/KGU#316 2016-11-15/2016-12-28
		}
	}
	
	// START KGU#289/KGU#316 2016-11-15/2016-12-28: Enh. #290/#318: Better support for Arranger files
	public void openNsdOrArr(String _filepath)
	{
		if (StructogramFilter.getExtension(_filepath).equals("nsd")) {
			this.openNSD(_filepath);
		}
		else {
			loadArrangement(new File(_filepath));
		}
	}
	// END KGU#316 2016-12-28

	public void openNSD(String _filename)
	{
		// START KGU#48 2015-10-17: Arranger support
		Root oldRoot = this.root;
		// END KGU#48 2015-10-17
		// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
		String errorMessage = Menu.msgErrorNoFile.getText();
		// END KGU#111 2015-12-16
		try
		{
			File f = new File(_filename);
			//System.out.println(f.toURI().toString());
			if(f.exists()==true)
			{
				// save current diagram (only if something has been changed)
				saveNSD(true);

				// open an existing file
				NSDParser parser = new NSDParser();
				boolean hil = root.hightlightVars;
				root = parser.parse(f.toURI().toString());
				root.hightlightVars=hil;
				root.filename=_filename;
				currentDirectory = new File(root.filename);
				addRecentFile(root.filename);
				// START KGU#183 2016-04-23: Issue #169
				selected = root;
				root.setSelected(true);
				// END KGU#183 2016-04-23
				redraw();
				analyse();
				// START KGU#48 2015-10-17: Arranger support
				if (oldRoot != null)
				{
					oldRoot.notifyReplaced(root);
				}
				// END KGU#48 2015-10-17
				// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
				errorMessage = null;
				// END KGU#111 2015-12-16
			}
		}
		catch (Exception e)
		{
			// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
			//System.out.println(e.getMessage());
			errorMessage = e.getLocalizedMessage();
			if (errorMessage == null) errorMessage = e.getMessage();
			System.err.println("openNSD(\"" + _filename + "\"): " + (errorMessage != null ? errorMessage : e));
			if (e instanceof java.util.ConcurrentModificationException) {
				e.printStackTrace(System.err);
			}
			// END KGU#111 2015-12-16
		}
		// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
		if (errorMessage != null)
		{
			JOptionPane.showMessageDialog(this, "\"" + _filename + "\": " + errorMessage, 
					Menu.msgTitleLoadingError.getText(),
					JOptionPane.ERROR_MESSAGE);
		}
		// END KGU#111 2015-12-16
	}

	// START KGU#289 2016-11-15: Enh. #290 (Aranger file support
	private void loadArrangement(File arrFile)
	{
		Arranger arr = Arranger.getInstance();
		String errorMsg = arr.loadArrangement((Mainform)NSDControl.getFrame(), arrFile.toString());
		if (!errorMsg.isEmpty()) {
			JOptionPane.showMessageDialog(this, "\"" + arrFile + "\": " + errorMsg, 
					Menu.msgTitleLoadingError.getText(),
					JOptionPane.ERROR_MESSAGE);			
		}
		else {
			arr.setVisible(true);
			// START KGU#316 2016-12-28: Enh. #318
			addRecentFile(arrFile.getAbsolutePath());
			this.currentDirectory = arrFile;
			// END KGU#316 2016-12-28
		}
	}
	// END KGU#289 2016-11-15

	/*****************************************
	 * SaveAs method
	 *****************************************/
	public void saveAsNSD()
	// START KGU#320 2017-01-04: Bugfix #321(?) We need a possibility to save a different root
	{
		saveAsNSD(this.root);
	}
	
	private void saveAsNSD(Root root)
	// END KGU#320 2017-01-04
	{
		JFileChooser dlgSave = new JFileChooser();
		dlgSave.setDialogTitle(Menu.msgTitleSaveAs.getText());
		// set directory
		if(root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(currentDirectory);
		}

		// propose name
		String nsdName = root.proposeFileName();
		dlgSave.setSelectedFile(new File(nsdName));

		dlgSave.addChoosableFileFilter(new StructogramFilter());
		
		// START KGU#248 2016-09-15: Bugfix #244 - allow more than one chance
		//int result = dlgSave.showSaveDialog(this);
		int result = JFileChooser.ERROR_OPTION;
		do {
			result = dlgSave.showSaveDialog(this);
		// END KGU#248 2016-9-15
			if (result == JFileChooser.APPROVE_OPTION)
			{
				String newFilename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
				if(!newFilename.substring(newFilename.length()-4, newFilename.length()).toLowerCase().equals(".nsd"))
				{
					newFilename += ".nsd";
				}

				File f = new File(newFilename);
				boolean writeNow = true;
				if (f.exists())
				{
					writeNow=false;
					int res = JOptionPane.showConfirmDialog(
							this,
							Menu.msgOverwriteFile.getText(),
							Menu.btnConfirmOverwrite.getText(),
							JOptionPane.YES_NO_OPTION);
					if (res == JOptionPane.YES_OPTION) writeNow=true;
				}

				if (!writeNow)
				{
					// START KGU#248 2016-09-15: Bugfix #244 - message no longer needed (due to new loop)
					//JOptionPane.showMessageDialog(this, Menu.msgRepeatSaveAttempt.getText());
					result = JFileChooser.ERROR_OPTION;
					// END KGU#248 2016-09-15
				}
				else
				{
					root.filename = newFilename;
					// START KGU#316 2016-12-28: Enh. #318
					root.shadowFilepath = null;
					// END KGU#316 2016-12-28
					// START KGU#94 2015.12.04: out-sourced to auxiliary method
					// START KGU#320 2017-01-04: Bugfix #321(?) Need a parameter now
					//doSaveNSD();
					doSaveNSD(root);
					// END KGU#320 2017-01-04
					// END KGU#94 2015-12-04
		        	// START KGU#273 2016-10-07: Bugfix #263 - remember the directory as current directory
		        	this.currentDirectory = f;
		        	// END KGU#273 2016-10-07
				}
			}
		// START KGU#248 2016-09-15: Bugfix #244 - allow to leave the new loop
			else
			{
				// User cancelled the file dialog -> leave the loop
				result = JFileChooser.CANCEL_OPTION;
			}
		} while (result == JFileChooser.ERROR_OPTION);
		// END KGU#248 2016-09-15

	}

	/*****************************************
	 * Save method
	 *****************************************/
	
	/**
	 * Stores unsaved changes (if any). If _askToSave is true then the user may confirm or deny saving or cancel the
	 * inducing request. 
	 * @param _askToSave - if true and the current root has unsaved changes then a user dialog will be popped up first
	 * @return true if the user did not cancel the save request
	 */
	public boolean saveNSD(boolean _askToSave)
	// START KGU#320 2017-01-04: Bugfix (#321)
	{
		return saveNSD(this.root, _askToSave);
	}
	
	public boolean saveNSD(Root root, boolean _askToSave)
	// END KGU#320 2017-01-04
	{
		int res = 0;	// Save decision: 0 = do save, 1 = don't save, -1 = cancelled (don't leave)
		// only save if something has been changed
		// START KGU#137 2016-01-11: Use the new method now
		//if(root.hasChanged==true)
		if (!root.isEmpty() && root.hasChanged())
		// END KGU#137 2016-01-11
		{

			if (_askToSave)
			{
				// START KGU#49 2015-10-18: If induced by Arranger then it's less ambiguous seeing the NSD name
				//res = JOptionPane.showOptionDialog(this,
				//		   "Do you want to save the current NSD-File?",
				String filename = root.filename;
				if (filename == null || filename.isEmpty())
				{
					filename = root.proposeFileName();
				}
				res = JOptionPane.showOptionDialog(this,
												   Menu.msgSaveChanges.getText() + "\n\"" + filename + "\"",
				// END KGU#49 2015-10-18
												   Menu.msgTitleQuestion.getText(),
												   JOptionPane.YES_NO_OPTION,
												   JOptionPane.QUESTION_MESSAGE,
												   null,null,null);
			}
			
			if (res==0)
			{
				// Check whether root has already been loaded or saved once
				//boolean saveIt = true;

				//System.out.println(this.currentDirectory.getAbsolutePath());
				
				if (root.filename.equals(""))
				{
					// root has never been saved
// START KGU#248 2016-09-15: Bugfix #244 delegate to saveAsNSD()
//					JFileChooser dlgSave = new JFileChooser();
//					dlgSave.setDialogTitle(Menu.msgTitleSave.getText());
//					// set directory
//					if (root.getFile() != null)
//					{
//						dlgSave.setCurrentDirectory(root.getFile());
//					}
//					else
//					{
//						dlgSave.setCurrentDirectory(currentDirectory);
//					}
//
//					// propose name
//
//					dlgSave.setSelectedFile(new File(root.proposeFileName()));
//
//					dlgSave.addChoosableFileFilter(new StructogramFilter());
//					int result = dlgSave.showSaveDialog(this);
//
//					if (result == JFileChooser.APPROVE_OPTION)
//					{
//						root.filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
//						if(!root.filename.substring(root.filename.length()-4).toLowerCase().equals(".nsd"))
//						{
//							root.filename+=".nsd";
//						}
//					}
//					else
//					{
//						saveIt = false;
//					}
//				}
//
//				if (saveIt == true)
					// START KGU#320 2017-01-04: Bugfix (#321)
					//saveAsNSD();
					saveAsNSD(root);
					// END KGU#320 2017-01-04
				}
				else
// END KGU#248 2016-09-15
				{
					// START KGU#94 2015-12-04: Out-sourced to auxiliary method
					// START KGU#320 2017-01-04: Bugfix (#321) had to parameterize this
					//doSaveNSD();
					doSaveNSD(root);
					// END KGU#320 2017-01-04
					// END KGU#94 2015-12-04
				}
			}
		}
		return res != -1; // true if not cancelled
	}
	
	// START KGU#94 2015-12-04: Common file writing routine (on occasion of bugfix #40)
	// START KGU#320 2017-01-03: Bugfix (#321)
	//private boolean doSaveNSD()
	private boolean doSaveNSD(Root root)
	// END KGU#320 2017-01-03
	{
		//String[] EnvVariablesToCheck = { "TEMP", "TMP", "TMPDIR", "HOME", "HOMEPATH" };
		boolean done = false;
		try
		{
			// START KGU#94 2015.12.04: Bugfix #40 part 1
			// A failed saving attempt should not leave a truncated file!
			//FileOutputStream fos = new FileOutputStream(root.filename);
			String filename = root.filename;
			// START KGU#316 2016-12-28: Enh. #318
			if (root.shadowFilepath != null) {
				filename = root.shadowFilepath;
			}
			// END KGU#316 2016-12-28
			File f = new File(filename);
			boolean fileExisted = f.exists();
			// START KGU#316 2016-12-28: Enh. 318
			//if (fileExisted)
			if (fileExisted && root.shadowFilepath == null)
			// END KGU#316 2016-12-28
			{
				// START KGU#316 2016-12-28: Enh. #318 - temporary file designation simplified  
//        		String tempDir = "";
//        		for (int i = 0; (tempDir == null || tempDir.isEmpty()) && i < EnvVariablesToCheck.length; i++)
//        		{
//        			tempDir = System.getenv(EnvVariablesToCheck[i]);
//        		}
//        		if ((tempDir == null || tempDir.isEmpty()) && this.currentDirectory != null)
//        		{
//        			File dir = this.currentDirectory;
//        			if (dir.isFile())
//        			{
//        				tempDir = dir.getParent();
//        			}
//        			else
//        			{
//        				tempDir = dir.getAbsolutePath();
//        			}
//        		}
//        		filename = tempDir + System.getProperty("file.separator") + "Structorizer.tmp";
				File tmpFile = File.createTempFile("Structorizer", "nsd");
				filename = tmpFile.getAbsolutePath();
				// END KGU#316 2016-12-28
			}
			FileOutputStream fos = new FileOutputStream(filename);
			// END KGU#94 2015-12-04
			Writer out = new OutputStreamWriter(fos, "UTF-8");
			XmlGenerator xmlgen = new XmlGenerator();
			out.write(xmlgen.generateCode(root,"\t"));
			out.close();

			// START KGU#94 2015-12-04: Bugfix #40 part 2
			// If the NSD file had existed then replace it by the output file after having created a backup
			// START KGU#316 2016-12-28: Enh. #318 Let nsd files reside in arrz files
			// if (fileExisted)
			if (root.shadowFilepath != null) {
				// START KGU#320 2017-01-04: Bugfix #321(?)
				//if (!zipToArrz(filename)) {
				if (!zipToArrz(root, filename)) {
				// END KGU#320 2017-01-04
					// If the saving to the original arrz file failed then make the shadow path the actual one
					root.filename = filename;
					root.shadowFilepath = null;
				}
			}
			else if (fileExisted)
			// END KGU#316 201612-28
			{
				File backUp = new File(root.filename + ".bak");
				if (backUp.exists())
				{
					backUp.delete();
				}
				f.renameTo(backUp);
				f = new File(root.filename);
				File tmpFile = new File(filename);
				tmpFile.renameTo(f);
				// START KGU#309 2016-12-15: Issue #310 backup may be opted out
				if (!Element.E_MAKE_BACKUPS && backUp.exists()) {
					backUp.delete();
				}
				// END KGU#309 2016-12-15
			}
			// END KGU#94 2015.12.04

			// START KGU#137 2016-01-11: On successful saving, record the undo stack level
			//root.hasChanged=false;
			root.rememberSaved();
			// END KGU#137 2016-01-11
			// START KGU#316 2016-12-28: Enh. #318: Don't remember a zip-internal file path
			//addRecentFile(root.filename);
			addRecentFile(root.getPath(true));
			// END KGU#316 2016-12-28
			done = true;
		}
		catch(Exception ex)
		{
			String message = ex.getLocalizedMessage();
			if (message == null) message = ex.getMessage();
			JOptionPane.showMessageDialog(this,
					Menu.msgErrorFileSave.getText().replace("%", message),
					Menu.msgTitleError.getText(),
					JOptionPane.ERROR_MESSAGE, null);
		}
		return done;
	}
	// END KGU#94 2015-12-04
	
	// START KGU#316 2016-12-28: Enh. #318
	// START KGU#320 2017-01-04: Bugfix #320 We might be forced to save a different diagram (from Arranger)
	//private boolean zipToArrz(String tmpFilename)
	private boolean zipToArrz(Root root, String tmpFilename)
	// END KGU#320 2017-01-04
	{
		String error = null;
		boolean isDone = false;
		final int BUFSIZE = 2048;
		byte[] buf = new byte[BUFSIZE];
		int len = 0;

		StringList inZipPath = new StringList();
		File arrzFile = new File(root.filename);
		while (arrzFile != null && !arrzFile.isFile()) {
			inZipPath.add(arrzFile.getName());
			arrzFile = arrzFile.getParentFile();
		}
		if (arrzFile == null) {
			int posArrz = root.filename.toLowerCase().indexOf(".arrz");
			error = ((posArrz > 0) ? root.filename.substring(0, posArrz+5) : root.filename) + ": " + Menu.msgErrorNoFile.getText();
		}
		else {
			String localPath = inZipPath.reverse().concatenate(File.separator);

			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(arrzFile);
				File tmpZipFile = File.createTempFile("Structorizer", "zip");
				final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpZipFile));
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				// Copy all but the file to be updated
				while(entries.hasMoreElements()) {
					ZipEntry entryIn = entries.nextElement();
					if (!entryIn.getName().equals(localPath)) {
						zos.putNextEntry(entryIn);
						InputStream is = zipFile.getInputStream(entryIn);
						while((len = is.read(buf)) > 0) {            
							zos.write(buf, 0, len);
						}
						zos.closeEntry();
					}
				}
				// Now add the file to be updated
				zos.putNextEntry(new ZipEntry(localPath));
				FileInputStream fis = new FileInputStream(tmpFilename);
				while ((len = (fis.read(buf))) > 0) {
					zos.write(buf, 0, len);
				}
				zos.closeEntry();
				fis.close();
				zos.close();
				zipFile.close();
				String zipPath = arrzFile.getAbsolutePath();
				File bakFile = new File(zipPath + ".bak");
				if (bakFile.exists()) {
					bakFile.delete();
				}
				boolean bakOk = arrzFile.renameTo(bakFile);
				boolean zipOk = tmpZipFile.renameTo(new File(zipPath));
				if (bakOk && zipOk && !Element.E_MAKE_BACKUPS) {
					bakFile.delete();
				}
				isDone = true;
			} catch (ZipException ex) {
				error = ex.getLocalizedMessage();
			} catch (IOException ex) {
				error = ex.getLocalizedMessage();
			}
		}
		if (error != null) {
			JOptionPane.showMessageDialog(this,
					error,
					Menu.msgTitleError.getText(),
					JOptionPane.ERROR_MESSAGE, null);
		}
		return isDone;
	}
	// END KGU#316 2016-12-28

	/*****************************************
	 * Undo method
	 *****************************************/
	public void undoNSD()
	{
		root.undo();
		// START KGU#138 2016-01-11: Bugfix #102 - All elements will be replaced by copies...
		selected = this.selectedDown = this.selectedMoved = this.selectedUp = null;
		// END KGU#138 2016-01-11
		// START KGU#272 2016-10-06: Bugfix #262: We must unselect root such that it may find a selected descendant
		root.setSelected(false);
		// END KGU#272 2016-10-06
		// START KGU#183 2016-04-24: Issue #169 - Restore previous selection if possible
		selected = root.findSelected();
		// END KGU#183 2016-04-24
		// START KGU#272 2016-10-06: Bugfix #262
		if (selected == null)
		{
			selected = root;
			root.setSelected(true);
		}
		else
		{
			selectedDown = selectedUp = selected;
		}
		// END KGU#272 2016-10-06
		redraw();
		analyse();
	}

	/*****************************************
	 * Redo method
	 *****************************************/
	public void redoNSD()
	{
		root.redo();
		// START KGU#138 2016-01-11: Bugfix #102 All elements will be replaced by copies...
		selected = this.selectedDown = this.selectedMoved = this.selectedUp = null;
		// END KGU#138 2016-01-11
		// START KGU#272 2016-10-06: Bugfix #262: We must unselect root such that it may find a selected descendant
		root.setSelected(false);
		// END KGU#272 2016-10-06
		// START KGU#183 2016-04-24: Issue #169 - Restore previous selection if possible
		selected = root.findSelected();
		// END KGU#183 2016-04-24
		// START KGU#272 2016-10-06: Bugfix #262
		if (selected == null)
		{
			selected = root;
			root.setSelected(true);
		}
		else
		{
			selectedDown = selectedUp = selected;
		}
		// END KGU#272 2016-10-06
		redraw();
		analyse();
	}

	/*****************************************
	 * applicability test methods
	 *****************************************/
	public boolean canPaste()
	{
		boolean cond = (eCopy!=null && selected!=null);
		if (cond)
		{
			// START KGU#143 2016-01-21 Bugfix #114
			// The first condition is for the case the copy is a referenced sequence 
			cond = !eCopy.isExecuted();
			// We must not insert to a subqueue with an element currently executed or with pending execution
			// (usually the exection index would then be on the stack!)
			if (!(selected instanceof Subqueue) && selected.parent != null && selected.parent.isExecuted())
			{
				cond = false;
			}
			// END KGU#143 2016-01-21
			cond = cond && !selected.getClass().getSimpleName().equals("Root");
		}

		return cond;
	}

	// START KGU#143 2016-01-21: Bugfix #114 - elements involved in execution must not be edited...
	//public boolean canCutCopy()
	public boolean canCut()
	{
		// START KGU#177 2016-04-14: Enh. #158 - we want to allow to copy diagrams e.g. to an Arranger of a different JVM
		//return canCopy() && !selected.executed && !selected.waited;
		// START KGU#177 2016-07-06: Enh #158: mere re-formulation (equivalent)
		//return canCopy() && !(selected instanceof Root) && !selected.executed && !selected.waited;
		return canCopyNoRoot() && !selected.isExecuted();
		// END KGU#177 2016-07-06
		// END KGU#177 2016-04-14
	}

	// ... though breakpoints shall still be controllable
	public boolean canCopy()
	// END KGU#143 2016-01-21
	{
		boolean cond = (selected!=null);
		if (cond)
		{
			// START KGU#177 2016-04-14: Enh. #158 - we want to allow to copy diagrams e.g. to an Arranger of a different JVM
			//cond = !selected.getClass().getSimpleName().equals("Root");
			// END KGU#177 2016-04-14
			// START KGU#87 2015-11-22: Allow to copy a non-empty Subqueue
			//cond = cond && !selected.getClass().getSimpleName().equals("Subqueue");
			cond = cond && (!selected.getClass().getSimpleName().equals("Subqueue") || ((Subqueue)selected).getSize() > 0);
			// END KGU#87 2015-11-22
		}

		return cond;
	}
	
	// START KGU#177 2016-07-06: Enh. #158 - accidently breakpoints had become enabled on Root
	public boolean canCopyNoRoot()
	{
		return canCopy() && !(selected instanceof Root);
	}
	// END KGU#177 2016-07-06
	
	// START KGU#143 2016-11-17: Issue #114: Complex condition for editability
	public boolean canEdit()
	{
		return selected != null && !this.selectedIsMultiple() &&
				(!selected.isExecuted(false) || selected instanceof Instruction && !selected.executed);
	}
	// END KGU#143 2016-11-17

	// START KGU#199 2016-07-06: Enh. #188: Element conversions
	public boolean canTransmute()
	{
		boolean isConvertible = false;
		if (selected != null && !selected.isExecuted())
		{
			if (selected instanceof Instruction)
			{
				Instruction instr = (Instruction)selected;
				isConvertible = instr.getText().count() > 1
						|| instr.isJump()
						|| instr.isFunctionCall()
						|| instr.isProcedureCall();
			}
			else if (selected instanceof IElementSequence && ((IElementSequence)selected).getSize() > 1)
			{
				isConvertible = true;
				for (int i = 0; isConvertible && i < ((IElementSequence)selected).getSize(); i++)
				{
					if (!(((IElementSequence)selected).getElement(i) instanceof Instruction))
					{
						isConvertible = false;
					}
				}
			}
			// START KGU#229 2016-08-01: Enh. #213
			else if (selected instanceof For)
			{
				isConvertible = ((For)selected).style == For.ForLoopStyle.COUNTER;
			}
			// END KGU#229 2016-08-01
			// START KGU#267 2016-10-03: Enh. #257
			else if (selected instanceof Case)
			{
				isConvertible = true;
			}
			// END KGU#267 2016-10-03
		}
		return isConvertible;
	}
	// END KGU#199 2016-07-06
	
	/*****************************************
	 * setColor method
	 *****************************************/
	public void setColor(Color _color)
	{
		if(getSelected()!=null)
		{
			// START KGU#38 2016-01-11 Setting of colour wasn't undoable though recorded as change
			root.addUndo();
			// END KGU#38 2016-01-11
			getSelected().setColor(_color);
			//getSelected().setSelected(false);
			//selected=null;
			// START KGU#137 2016-01-11: Already prepared by addUndo()
			//root.hasChanged=true;
			// END KGU#137 2016-01-11
			redraw();
		}
	}


	/*****************************************
	 * Copy method
	 *****************************************/
	public void copyNSD()
	{
		if (selected!=null)
		{
			// START KGU#177 2016-04-14: Enh. #158 - Allow to copy a diagram via clipboard
			//eCopy = selected.copy();
			if (selected instanceof Root)
			{
	        	XmlGenerator xmlgen = new XmlGenerator();
				StringSelection toClip = new StringSelection(xmlgen.generateCode(root,"\t"));
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(toClip, this);
			}
			else
			{
				eCopy = selected.copy();
			}
			// END KGU#177 2016-04-14
		}
	}

	/*****************************************
	 * cut method
	 *****************************************/
	public void cutNSD()
	{
		if (selected!=null)
		{
			eCopy = selected.copy();
			// START KGU#182 2016-04-23: Issue #168	- pass the selection to the "next" element
			Element newSel = getSelectionHeir();
			// END KGU#182 2016-04-23
			eCopy.setSelected(false);
			root.addUndo();
			root.removeElement(selected);
			// START KGU#137 2016-01-11: Already prepared by addUndo()
			//root.hasChanged=true;
			// END KGU#137 2016-01-11
			redraw();
			// START KGU#182 2016-04-23: Issue #168	- pass the selection to the "next" element
			//selected=null;
			this.selected = newSel;
			if (newSel != null)
			{
				newSel.setSelected(true);
			}
			// END KGU#182 2016-04-23
			analyse();
		}
	}

	/*****************************************
	 * paste method
	 *****************************************/
	public void pasteNSD()
	{
		if (selected!=null && eCopy!=null)
		{
			root.addUndo();
			selected.setSelected(false);
			Element nE = eCopy.copy();
			nE.setSelected(true);	// FIXME (KGU#87): Looks fine but is misleading with a pasted Subqueue
			root.addAfter(selected,nE);
			// START KGU#137 2016-01-11: Already prepared by addUndo()
			//root.hasChanged=true;
			// END KGU#137 2016-01-11
			// START KGU#87 2015-11-22: In case of a copied Subqueue the copy shouldn't be selected!
			if (nE instanceof Subqueue)
			{
				// If the target was a Subqueue then it had been empty and contains all nE had contained,
				// hence we may leave it selected, otherwise the minimum risk is to clear the selection
				if (!(selected instanceof Subqueue))
					selected = null;
				((Subqueue)nE).clear();
			}
			else
			// END KGU#87 2015-11-22
				selected=nE;
			redraw();
			analyse();
		}
	}

	/*****************************************
	 * edit method
	 *****************************************/
	public void editNSD()
	{
		Element element = getSelected();
		if(element!=null)
		{
			if (element.getClass().getSimpleName().equals("Subqueue"))
			{
				EditData data = new EditData();
				data.title="Add new instruction ...";

				// START KGU#42 2015-10-14: Enhancement for easier title localisation
				//showInputBox(data);
				showInputBox(data, "Instruction", true);
				// END KGU#42 2015-10-14

				if (data.result==true)
				{
					Element ele = new Instruction(data.text.getText());
					ele.setComment(data.comment.getText());
					// START KGU#43 2015-10-17
					if (data.breakpoint) {
						ele.toggleBreakpoint();
					}
					// END KGU#43 2015-10-17
					// START KGU#277 2016-10-13: Enh. #270
					ele.disabled = data.disabled;
					// END KGU#277 2016-10-13
					// START KGU#213 2016-08-01: Enh. #215 (temprarily disabled again)
					//ele.setBreakTriggerCount(data.breakTriggerCount);
					// END KGU#213 2016-08-01
					root.addUndo();
					// START KGU#137 2016-01-11: Already prepared by addUndo()
					//root.hasChanged=true;
					// END KGU#137 2016-01-11
					((Subqueue) element).addElement(ele);
					// START KGU#136 2016-03-01: Bugfix #97
					// FIXME: Other parts of the diagram might be affected, too
					element.resetDrawingInfoUp();
					// END KGU#136 2016-03-01
					ele.setSelected(true);
					selected=ele;
					redraw();
				}
			}
			else
			{
				EditData data = new EditData();
				data.title="Edit element ...";
				data.text.setText(element.getText().getText());
				data.comment.setText(element.getComment().getText());
				// START KGU#43 2015-10-12
				data.breakpoint = element.isBreakpoint();
				// END KGU#43 2015-10-12
				// START KGU#213 2016-08-01: Enh. #215
				data.breakTriggerCount = element.getBreakTriggerCount();
				// END KGU#213 2016-08-01				
				// START KGU#277 2016-10-13: Enh. #270
				data.disabled = element.disabled;
				// END KGU#277 2016-10-13
			
				// START KGU#3 2015-10-25: Allow more sophisticated For loop editing
				if (element instanceof For)
				{
					// START KGU#61 2016-03-21: Content of the branch outsourced
					preEditFor(data, (For)element);
					// END KGU#61 2016-03-21
				}
				// END KGU#3 2015-10-25

				// START KGU#42 2015-10-14: Enhancement for easier title localisation
				//showInputBox(data);
				showInputBox(data, element.getClass().getSimpleName(), false);
				// END KGU#42 2015-10-14

				if(data.result==true)
				{
					// START KGU#120 2016-01-02: Bugfix #85 - StringList changes of Root are to be undoable, too!
					//if (!element.getClass().getSimpleName().equals("Root"))
					// END KGU#120 2016-01-02
						root.addUndo();
					if (!(element instanceof Forever))
					{
						element.setText(data.text.getText());
					}
					element.setComment(data.comment.getText());
					// START KGU#43 2015-10-12
					if (element.isBreakpoint() != data.breakpoint) 
					{
						element.toggleBreakpoint();
					}
					// END KGU#43 2015-10-12
					// START KGU#213 2016-08-01: Enh. #215
					//element.setBreakTriggerCount(data.breakTriggerCount);
					// END KGU#213 2016-08-01
					// START KGU#277 2016-10-13: Enh. #270
					element.disabled = data.disabled;
					// END KGU#277 2016-10-13
					// START KGU#3 2015-10-25
					if (element instanceof For)
					{
						// START KGU#61 2016-03-21: Content of the branch outsourced
						postEditFor(data, (For)element);
						// END KGU#61 2016-03-21
					}
					// END KGU#3 2015-10-25
					// START KGU#137 2016-01-11: Already prepared by addUndo()
					//root.hasChanged=true;
					// END KGU#137 2016-01-11
					// START KGU#136 2016-03-01: Bugfix #97
					// FIXME: Other parts of the diagram might be affected, too
					element.resetDrawingInfoUp();
					// END KGU#136 2016-03-01
					redraw();
				}
			}

			analyse();
		}
	}
	
	private void preEditFor(EditData _data, For _for)
	{
		// Cache the style - we temporarily modify it to get all information
		For.ForLoopStyle style = _for.style;
		try {
			_for.style = For.ForLoopStyle.COUNTER;
			_data.forParts.add(_for.getCounterVar());
			_data.forParts.add(_for.getStartValue());
			_data.forParts.add(_for.getEndValue());
			_data.forParts.add(Integer.toString(_for.getStepConst()));
		}
		catch (Exception ex) {}
		finally {
			// Ensure the original style is restored
			_data.forLoopStyle = _for.style = style;
		}
		// Now try to get a value list in case it's a FOR-IN loop
		String valueList = _for.getValueList();
		if (valueList != null)
		{
			_data.forParts.add(valueList);
		}
		
	}
	
	private void postEditFor(EditData _data, For _for)
	{
		_for.style = _data.forLoopStyle;

		_for.setCounterVar(_data.forParts.get(0));
		_for.setStartValue(_data.forParts.get(1));
		_for.setEndValue(_data.forParts.get(2));
		_for.setStepConst(_data.forParts.get(3));

		// FOR-IN loop support
		if (_for.style == For.ForLoopStyle.TRAVERSAL)
		{
			// START KGU#61 2016-09-24: Seemed to be nonsense
			//_for.style = For.ForLoopStyle.FREETEXT;
			//_for.setValueList(_for.getValueList());
			//_for.style = For.ForLoopStyle.TRAVERSAL;
			_for.setValueList(_data.forParts.get(4));
			// END KGU#61 2016-09-24
		}
		// START KGU#61 2016-09-24
		else {
			_for.setValueList(null);
		}
		// END KGU#61 2016-09-24		
	}

	/*****************************************
	 * moveUp method
	 *****************************************/
	public void moveUpNSD()
	{
		root.addUndo();
		root.moveUp(getSelected());
		redraw();
		analyse();
	}

	/*****************************************
	 * moveDown method
	 *****************************************/
	public void moveDownNSD()
	{
		root.addUndo();
		root.moveDown(getSelected());
		redraw();
		analyse();
	}

	/*****************************************
	 * delete method
	 *****************************************/
	public void deleteNSD()
	{
		root.addUndo();
		// START KGU#181 2016-04-19: Issue #164	- pass the selection to the "next" element
		Element newSel = getSelectionHeir();
		// END KGU#181 2016-04-19
		root.removeElement(getSelected());
		// START KGU#138 2016-01-11: Bugfix #102 - selection no longer valid
		// START KGU#181 2016-04-19: Issue #164	- pass the selection to the "next" element
		//this.selected = null;
		this.selected = newSel;
		if (newSel != null)
		{
			newSel.setSelected(true);
		}
		// END KGU#181 2016-04-19
		// END KGU#138 2016-01-11
		redraw();
		analyse();
		// START KGU#138 2016-01-11: Bugfix#102 - disable element-based buttons
		this.NSDControl.doButtons();
		// END KGU#138 2016-01-11
	}
	
	// START KGU#181 2016-04-19: Issue #164	- pass the selection to the "next" element
	/**
	 * Returns the element "inheriting" the selection from the doomed currently selected element
	 * This will be (if existent): 1. successor within Subqueue, 2. predecessor within Subqueue
	 * Requires this.selected to be neither Root nor an empty Subqueue.
	 * @return the element next the currently selected one, null if there is no selection
	 */
	private Element getSelectionHeir()
	{
		Element heir = null;
		if (selected != null && !(selected instanceof Root))
		{
			Subqueue sq = (Subqueue)((selected instanceof Subqueue) ? selected : selected.parent);
			int ixHeir = -1; 
			if (selected instanceof SelectedSequence)
			{
				// Last element of the subsequence
				Element last = ((SelectedSequence) selected).getElement(((SelectedSequence) selected).getSize()-1);
				Element frst = ((SelectedSequence) selected).getElement(0);
				int ixLast = sq.getIndexOf(last);	// Actual index of the last element in the Subqueue
				int ixFrst = sq.getIndexOf(frst);	// Actual index of the first element in the Subqueue
				if (ixLast < sq.getSize() - 1)
				{
					ixHeir = ixLast + 1;
				}
				else if (ixFrst > 0)
				{
					ixHeir = ixFrst - 1;
				}
			}
			else if (!(selected instanceof Subqueue)) 
			{
				int ixEle = sq.getIndexOf(selected);
				if (ixEle < sq.getSize() - 1)
				{
					ixHeir = ixEle + 1;
				}
				else
				{
					ixHeir = ixEle - 1;
				}
			}
			if (ixHeir >= 0)
			{
				heir = sq.getElement(ixHeir);
			}
			else
			{
				// Empty Subqueue remnant will take over selection
				heir = sq;
			}
		}
		return heir;
	}
	// END KGU#181 2016-04-19
	
	// START KGU#123 2016-01-03: Issue #65, for new buttons and menu items
	/*****************************************
	 * collapse method
	 *****************************************/
	public void collapseNSD()
	{
		getSelected().setCollapsed(true);
		redraw();
		analyse();
		this.NSDControl.doButtons();
	}
	
	/*****************************************
	 * expand method
	 *****************************************/
	public void expandNSD()
	{
		getSelected().setCollapsed(false);
		redraw();
		analyse();
		this.NSDControl.doButtons();
	}
	// END KGU#123 2016-01-03
	
	// START KGU#277 2016-10-13: Enh. #270
	/*========================================
	 * disable method
	 *=======================================*/
	public void disableNSD()
	{
		boolean allDisabled = true;
		root.addUndo();
		if (getSelected() instanceof IElementSequence)
		{
			IElementSequence elements = (IElementSequence)getSelected();
			for (int i = 0; allDisabled && i < elements.getSize(); i++)
			{
				allDisabled = elements.getElement(i).disabled;
			}
			elements.setDisabled(!allDisabled);
		}
		else {
			getSelected().disabled = !getSelected().disabled;
		}
		
		redraw();
		analyse();
		this.NSDControl.doButtons();
	}
	
	// END KGU#277 2016-10-13

	/*****************************************
	 * add method
	 *****************************************/
	public void addNewElement(Element _ele, String _title, String _pre, boolean _after)
	{
		if (getSelected()!=null)
		{
			EditData data = new EditData();
			data.title=_title;
			data.text.setText(_pre);
			// START KGU 2015-10-14: More information to ease title localisation
			//showInputBox(data);
			showInputBox(data, _ele.getClass().getSimpleName(), true);
			// END KGU 2015-10-14
			if(data.result == true)
			{
				if (!(_ele instanceof Forever))
				{
					_ele.setText(data.text.getText());
				}
				_ele.setComment(data.comment.getText());
				// START KGU 2015-10-17
				if (_ele.isBreakpoint() != data.breakpoint) {
					_ele.toggleBreakpoint();
				}
				// END KGU 2015-10-17
				// START KGU#277 2016-10-13: Enh. #270
				_ele.disabled = data.disabled;
				// END KGU#277 2016-10-13
				// START KGU#213 2016-08-01: Enh. #215
				//_ele.setBreakTriggerCount(data.breakTriggerCount);
				// END KGU#213 2016-08-01
				// START KGU#3 2015-10-25
				if (_ele instanceof For)
				{
					((For)_ele).setCounterVar(data.forParts.get(0));
					((For)_ele).setStartValue(data.forParts.get(1));
					((For)_ele).setEndValue(data.forParts.get(2));
					((For)_ele).setStepConst(data.forParts.get(3));
					// START KGU#61 2016-03-21: Enh. #84 - consider FOR-IN loops as well
					//((For)_ele).isConsistent = ((For)_ele).checkConsistency();
					((For)_ele).setValueList(data.forParts.get(4));
					((For)_ele).style = ((For)_ele).classifyStyle();
					// END KGU#61 2016-03-21
				}
				// END KGU#3 2015-10-25
				root.addUndo();
				if(_after==true)
				{
					root.addAfter(getSelected(),_ele);
				}
				else
				{
					root.addBefore(getSelected(),_ele);
				}
				_ele.setSelected(true);	// FIXME: should already have been done by Root.insertElement()
				selected=_ele;
				// START KGU#272 2016-10-06: Bugfix #262
				selectedDown = selectedUp = selected;
				// END KGU#272 2016-10-06
				redraw();
				analyse();
			}
		}
	}
	
	/*****************************************
	 * transmute method(s)
	 *****************************************/
	// START KGU#199 2016-07-06: Enh. #188 - perform the possible conversion
	public void transmuteNSD()
	{
		Subqueue parent = (Subqueue)selected.parent;
		if (selected instanceof Instruction)
		{
			root.addUndo();
			if (selected.getText().count() > 1)
			{
				transmuteToSequence(parent);
			}
			else
			{
				transmuteToSpecialInstr(parent);
			}
		}
		else if (selected instanceof IElementSequence)
		{
			root.addUndo();
			transmuteToCompoundInstr(parent);
		}
		// START KGU#229 2016-08-01: Enh. #213 - FOR loop decomposition
		else if (selected instanceof For && ((For)selected).style == For.ForLoopStyle.COUNTER)
		{
			root.addUndo();
			decomposeForLoop(parent);
		}
		// END KGU#229 2016-08-01
		// START KGU#267 2016-10-03: Enh. #257 - CASE decomposition
		else if (selected instanceof Case)
		{
			root.addUndo();
			decomposeCase(parent);
		}
		// END KGU#267 2016-10-03
		this.doButtons();
		redraw();
		analyse();
	}
	
	private void transmuteToSequence(Subqueue parent)
	{
		// Comment will be split as follows:
		// If the number of strings of the comment equals the number of instruction
		// lines then the strings are assigned one by one to he resulting instructions
		// (thereby splitting multi-line strings into StringLists),
		// otherwise the first instruction will get all comment.
		int index = parent.getIndexOf(selected);
		StringList comment = selected.getComment();
		int count = selected.getText().count();
		boolean distributeComment = (count == comment.count());
		for (int i = 0; i < count; i++)
		{
			Instruction instr = (Instruction)selected.copy();
			instr.setText(selected.getText().get(i));
			if (distributeComment)
			{
				instr.setComment(StringList.explode(comment.get(i), "\n"));
			}
			else if (i != 0)
			{
				instr.comment.clear();
			}
			parent.insertElementAt(instr, index+i+1);
		}
		parent.removeElement(index);
		selected = new SelectedSequence(parent, index, index+count-1);
		selectedUp = selectedDown = null;
		selected.setSelected(true);
	}
	
	private void transmuteToSpecialInstr(Subqueue parent)
	{
		Instruction instr = (Instruction)selected;
		Element elem = instr;
		if (instr instanceof Call || instr instanceof Jump)
		{
			elem = new Instruction(instr);
		}
		else if (instr.isProcedureCall() || instr.isFunctionCall())
		{
			elem = new Call(instr);
		}
		else if (instr.isJump())
		{
			elem = new Jump(instr);
		}
		int index = parent.getIndexOf(instr);
		parent.insertElementAt(elem, index+1);
		parent.removeElement(index);
		this.selected = elem;
		this.selectedUp = this.selectedDown = this.selected;
	}
	
	private void transmuteToCompoundInstr(Subqueue parent)
	{
		// Comments will be composed as follows:
		// If none of the selected elements had a non-empty comment then the resulting
		// comment will be empty. Otherwise the resulting comment will contain as many
		// strings as elements. Each of them will be the respective element comment,
		// possibly containing several newlines if it was a multi-line comment.
		Instruction instr = (Instruction)((IElementSequence)selected).getElement(0);
		StringList composedComment = StringList.getNew(instr.getComment().getText().trim());
		int nElements = ((IElementSequence)selected).getSize();
		int index = parent.getIndexOf(instr);
		boolean brkpt = instr.isBreakpoint();
		// START KGU#213 2016-08-01: Enh. #215
		int brkCount = instr.getBreakTriggerCount();
		// END KGU#213 2016-08-01
		// Find out whether all elements are of the same kind
		boolean sameKind = true;
		for (int i = 1; sameKind && i < nElements; i++)
		{
			if (((IElementSequence)selected).getElement(i).getClass() != instr.getClass())
			{
				sameKind = false;
			}
		}
		// If so...
		if (sameKind)
		{
			// ... then clone the first element of the sequence as same class
			instr = (Instruction)instr.copy();
		}
		else {
			// ... else clone the first element of the sequence as simple instruction
			instr = new Instruction(instr);
		}
		((IElementSequence)selected).removeElement(0);
		nElements--;
		// And now append the contents of the remaining elements, removing them from the selection
		for (int i = 0; i < nElements; i++)
		{
			Element ele = ((IElementSequence)selected).getElement(0);
			instr.getText().add(ele.getText());
			composedComment.add(ele.getComment().getText().trim());
			if (ele.isBreakpoint())
			{
				brkpt = true;
			}
			// START KGU#213 2016-08-01: Enh. #215
			// Use the earliest breakTriggerCount
			int brkCnt = ele.getBreakTriggerCount();
			if (brkCnt > 0 && brkCnt < brkCount)
			{
				brkCount = brkCnt;
			}
			// END KGU#213 2016-08-01
			((IElementSequence)selected).removeElement(0);			
		}
		// If there was no substantial comment then we must not create one, otherwise
		// the cmment is to consist of as many strings as instruction lines - where
		// each of them may contain newlines for reversibility
		if (!composedComment.concatenate().trim().isEmpty())
		{
			instr.setComment(composedComment);
		}
		else
		{
			instr.getComment().clear();
		}
		// If any of the implicated instructions had a breakpoint then set it here, too
		if (brkpt && !instr.isBreakpoint())
		{
			instr.toggleBreakpoint();
		}
		// START KGU#213 2016-08-01: Enh. #215
		// Use the earliest breakTriggerCount
		instr.setBreakTriggerCount(brkCount);
		// END KGU#213 2016-08-01
		
		instr.setSelected(true);
		parent.insertElementAt(instr, index);
		this.selected = instr;
		this.selectedUp = this.selectedDown = this.selected;
	}
	// END KGU#199 2016-07-06

	// START KGU#229 2016-08-01: Enh. #213 - FOR loop decomposition
	private void decomposeForLoop(Subqueue parent)
	{
		// Comment will be tranferred to the While loop.
		For forLoop = (For)selected;
		String asgmtOpr = " <- ";
		if (forLoop.getText().get(0).contains(":="))
		{
			asgmtOpr = " := ";
		}
		int step = forLoop.getStepConst();
		Element[] elements = new Element[3];
		elements[0] = new Instruction(forLoop.getCounterVar() + asgmtOpr + forLoop.getStartValue());
		// START KGU#229 2016-09-09: Take care of the configured prefix and postfix
		//While whileLoop = new While(forLoop.getCounterVar() + (step < 0 ? " >= " : " <= ") + forLoop.getEndValue());
		String prefix = "", postfix = "";
		if (!D7Parser.getKeyword("preWhile").trim().isEmpty()) {
			prefix = D7Parser.getKeyword("preWhile");
			if (!prefix.endsWith(" ")) prefix += " ";
		}
		if (!D7Parser.getKeyword("postWhile").trim().isEmpty()) {
			postfix = D7Parser.getKeyword("postWhile");
			if (!postfix.startsWith(" ")) postfix = " " + postfix;
		}
		While whileLoop = new While(prefix + forLoop.getCounterVar() + (step < 0 ? " >= " : " <= ") + forLoop.getEndValue() + postfix);
		// END KGU#229 2016-09-09
		elements[1] = whileLoop;
		elements[2] = new Instruction(forLoop.getCounterVar() + asgmtOpr + forLoop.getCounterVar() + (step < 0 ? " - " : " + ") + Math.abs(forLoop.getStepConst()));

		whileLoop.setComment(forLoop.getComment());
		if (forLoop.isBreakpoint())
		{
			whileLoop.toggleBreakpoint();
		}
		whileLoop.setBreakTriggerCount(forLoop.getBreakTriggerCount());
		whileLoop.q = forLoop.getBody();
		whileLoop.q.parent = whileLoop;
		whileLoop.q.addElement(elements[2]);
		whileLoop.setCollapsed(forLoop.isCollapsed());
		for (int i = 0; i < elements.length; i++)
		{
			Element elem = elements[i];
			elem.setColor(forLoop.getColor());
			elem.deeplyCovered = forLoop.deeplyCovered;
			elem.simplyCovered = forLoop.simplyCovered;
		}
		int index = parent.getIndexOf(forLoop);
		for (int i = 0; i < 2; i++)
		{
			parent.insertElementAt(elements[1-i], index+1);
		}
		parent.removeElement(index);
		this.selected = new SelectedSequence(parent, index, index+1);
		// START KGU#229 2016-09-11: selection must be made visible!
		this.selected.setSelected(true);
		// END KGU#229 2016-09-11
		this.selectedUp = this.selectedDown = this.selected;
	}
	// END KGU#229 2016-08-01

	// START KGU#267 2016-10-03: Enh. #257 - CASE structure decomposition
	private void decomposeCase(Subqueue parent)
	{
		// Comment will be tranferred to the first replacing element
		// (discriminator variable assignment or outermost Alternative).
		Case caseElem = (Case)selected;
		// List of replacing nested alternatives
		List<Alternative> alternatives = new LinkedList<Alternative>();
		// Possibly preceding assignment of the selection expression value
		Instruction asgnmt = null;
		// tokenized selection expression
		StringList selTokens = Element.splitLexically(caseElem.getText().get(0), true);
		// Eliminate parser preference keywords
		String[] redundantKeywords = {D7Parser.getKeyword("preCase"), D7Parser.getKeyword("postCase")};
		for (String keyword: redundantKeywords)
		{
			if (!keyword.trim().isEmpty())
			{
				StringList tokenizedKey = Element.splitLexically(keyword, false);
				int pos = -1;
				while ((pos = selTokens.indexOf(tokenizedKey, pos+1, !D7Parser.ignoreCase)) >= 0)
				{
					for (int i = 0; i < tokenizedKey.count(); i++)
					{
						selTokens.delete(pos);
					}
				}
			}
		}
		String discriminator = selTokens.concatenate().trim();
		// If the discriminating expression isn't just a variable then assign its value to an
		// artificial variable first and use this as discriminator further on.
		if (!Function.testIdentifier(discriminator, ""))
		{
			String discrVar = "discr" + caseElem.hashCode();
			asgnmt = new Instruction(discrVar + " <- " + discriminator);
			discriminator = discrVar;
			asgnmt.setColor(caseElem.getColor());
		}
		
		// Take care of the configured prefix and postfix
		String prefix = "", postfix = "";
		if (!D7Parser.getKeyword("preAlt").trim().isEmpty()) {
			prefix = D7Parser.getKeyword("preAlt");
			if (!prefix.endsWith(" ")) prefix += " ";
		}
		if (!D7Parser.getKeyword("postAlt").trim().isEmpty()) {
			postfix = D7Parser.getKeyword("postAlt");
			if (!postfix.startsWith(" ")) postfix = " " + postfix;
		}
		
		int nAlts = 0;	// number of alternatives created so far
		for (int lineNo = 1; lineNo < caseElem.getText().count(); lineNo++)
		{
			String line = caseElem.getText().get(lineNo);
			// Specific handling of the last branch
			if (lineNo == caseElem.getText().count()-1)
			{
				// In case it's a "%", nothing is to be added, otherwise the last
				// branch is to be the else path of the innermost alternative
				if (!line.equals("%"))
				{
					// This should not happen before the first alternative has been created!
					alternatives.get(nAlts-1).qFalse = caseElem.qs.get(lineNo-1);
					alternatives.get(nAlts-1).qFalse.parent = alternatives.get(nAlts-1);
				}
			}
			else 
			{
				String[] selectors = line.split(",");
				String cond = "";
				for (String selConst: selectors)
				{
					cond += " || (" + discriminator + " = " + selConst.trim() + ")";
				}
				// START KGU#288 2016-11-06: Issue #279
				//cond = cond.substring(4).replace("||", D7Parser.getKeywordOrDefault("oprOr", "or"));
				cond = cond.substring(4).replace("||", D7Parser.getKeywordOrDefault("oprOr", "or"));
				// END KGU#288 2016-11-06
				Alternative newAlt = new Alternative(prefix + cond + postfix);
				newAlt.qTrue = caseElem.qs.get(lineNo-1);
				newAlt.qTrue.parent = newAlt;
				alternatives.add(newAlt);
				if (nAlts > 0)
				{
					alternatives.get(nAlts-1).qFalse.addElement(newAlt);
				}
				nAlts++;
			}
		}

		Element firstSubstitutor = (asgnmt != null) ? asgnmt : alternatives.get(0);
		firstSubstitutor.setComment(caseElem.getComment());
		if (caseElem.isBreakpoint())
		{
			firstSubstitutor.toggleBreakpoint();
		}
		firstSubstitutor.setBreakTriggerCount(caseElem.getBreakTriggerCount());
		for (Alternative alt: alternatives)
		{
			alt.setColor(caseElem.getColor());
			alt.deeplyCovered = caseElem.deeplyCovered;
			alt.simplyCovered = caseElem.simplyCovered;
		}
		alternatives.get(0).setCollapsed(caseElem.isCollapsed());

		int index = parent.getIndexOf(caseElem);
		parent.removeElement(index);
		parent.insertElementAt(alternatives.get(0), index);
		if (asgnmt != null)
		{
			parent.insertElementAt(asgnmt, index);
			this.selected = new SelectedSequence(parent, index, index+1);
		}
		else 
		{
			this.selected = parent.getElement(index);
		}
		this.selected.setSelected(true);
		this.selectedUp = this.selectedDown = this.selected;
	}
	// END KGU#267 2016-10-03

	// START KGU#282 2016-10-16: Issue #272 (draft)
	/*=======================================*
	 * Turtleizer precision methods
	 *=======================================*/
	/**
	 * Replaces all Turtleizer fd and bk procedure calls by the more precise
	 * forward and backward instructions (precisionUp = true) or the other way round
	 * in the selected elements 
	 * @param precisionUp
	 */
	public void replaceTurtleizerAPI(boolean precisionUp)
	{
		final class TurtleizerSwitcher implements IElementVisitor 
		{
			private int from;
			// START #272 2016-10-17 (KGU): detect changes (to get rid of void undo entry
			private boolean act = false;
			private int nChanges = 0;
			// END #272 2016-10-17
			private final String[][] functionPairs = { {"fd", "forward"}, {"bk", "backward"}};
			
			public TurtleizerSwitcher(boolean upgrade)
			{
				this.from = upgrade ? 0 : 1;
			}
			
			public boolean visitPreOrder(Element _ele)
			{
				if (_ele.getClass().getSimpleName().equals("Instruction")) {
					for (int i = 0; i < _ele.getText().count(); i++) {
						String line = _ele.getText().get(i);
						if (Instruction.isTurtleizerMove(line)) {
							Function fct = new Function(line);
							for (int j = 0; j < functionPairs.length; j++) {
								String oldName = functionPairs[j][from];
								if (fct.getName().equals(oldName)) {
									// START #272 2016-10-17
									//_ele.getText().set(i, functionPairs[j][1 - from] + line.trim().substring(oldName.length()));
									if (this.act) {
										_ele.getText().set(i, functionPairs[j][1 - from] + line.trim().substring(oldName.length()));
									}
									nChanges++;
									// END #272 2016-10-17
								}
							}
						}
					}
				}
				return true;
			}
			public boolean visitPostOrder(Element _ele)
			{
				return true;
			}
			
			// START #272 2016-10-17 (KGU)
			public void activate()
			{
				this.nChanges = 0;
				this.act = true;
			}
			
			public int getNumberOfReplacements()
			{
				return nChanges;
			}
			// END #272 2016-10-17
			
		}
		
		// START #272 2016-10-17 (KGU): Inform the user and get rid of void undo entry
		//root.addUndo();
		//selected.traverse(new TurtleizerSwitcher(precisionUp));
		// First mere count run
		TurtleizerSwitcher switcher = new TurtleizerSwitcher(precisionUp);
		selected.traverse(switcher);
		int nReplaced = switcher.getNumberOfReplacements();
		if (nReplaced > 0) {
			// There will be substitutions, so get dangerous.
			root.addUndo();
			switcher.activate();
			selected.traverse(switcher);			
		}
		JOptionPane.showMessageDialog(this, Menu.msgReplacementsDone.getText().replace("%", Integer.toString(nReplaced)));
		// END #272 2016-10-17
		
	}
	// END KGU#282 2016-10-16

	// START KGU#43 2015-10-12
	/*****************************************
	 * breakpoint methods
	 *****************************************/
	public void toggleBreakpoint()
	{
		Element ele = getSelected();
		if (ele != null)
		{
			ele.toggleBreakpoint();
			redraw();
		}
	}
	
	// START KGU#213 2016-08-02: Enh. #215
	public void editBreakTrigger() {
		// TODO Auto-generated method stub
		Element ele = getSelected();
		if (ele != null)
		{
			int trigger = ele.getBreakTriggerCount();
			// FIXME: Replace this quick-and-dirty approach by something more functional
			String str = JOptionPane.showInputDialog(this,
					Menu.msgBreakTriggerPrompt.getText(),
					Integer.toString(trigger));
			if (str != null)
			{
				// START KGU#252 2016-09-21: Issue 248 - Linux (Java 1.7) workaround
				boolean isDone = false;
				// END KGU#252 2016-09-21
				try {
					// START KGU#252 2016-09-21: Issue 248 - Linux (Java 1.7) workaround
					//ele.setBreakTriggerCount(Integer.parseUnsignedInt(str));
					isDone = ele.setBreakTriggerCount(Integer.parseInt(str));
					// END KGU#252 2016-09-21
					// We assume the intention to activate the breakpoint with the configuration
					if (!ele.isBreakpoint())
					{
						ele.toggleBreakpoint();
					}
					redraw();
				}
				catch (NumberFormatException ex)
				{
					// START KGU#252 2016-09-21: Issue 248 - Linux (Java 1.7) workaround
					//JOptionPane.showMessageDialog(this,
					//		Menu.msgBreakTriggerIgnored.getText(),
					//		Menu.msgTitleWrongInput.getText(),
					//		JOptionPane.ERROR_MESSAGE);
					// END KGU#252 2016-09-21
				}
				// START KGU#252 2016-09-21: Issue 248 - Linux (Java 1.7) workaround
				if (!isDone) {
					JOptionPane.showMessageDialog(this,
						Menu.msgBreakTriggerIgnored.getText(),
						Menu.msgTitleWrongInput.getText(),
						JOptionPane.ERROR_MESSAGE);
				}
				// END KGU#252 2016-09-21
			}
		}
	}
	// END KGU#213 2016-08-02

	public void clearBreakpoints()
	{
		root.clearBreakpoints();
		redraw();
	}

	public void clearExecutionStatus()
	{
		root.clearExecutionStatus();
		redraw();
	}
	// END KGU#43 2015-10-12

	/*****************************************
	 * print method
	 *****************************************/
	public void printNSD()
	{
		/*
		// printing support
		//--- Create a printerJob object
		PrinterJob printJob = PrinterJob.getPrinterJob ();
		//--- Set the printable class to this one since we
		//--- are implementing the Printable interface
		printJob.setPrintable (this);
		//--- Show a print dialog to the user. If the user
		//--- clicks the print button, then print, otherwise
		//--- cancel the print job
		if (printJob.printDialog())
		{
			try
			{
				printJob.print();
			}
			catch (Exception PrintException)
			{
				PrintException.printStackTrace();
			}
		}
		*/
		//PrintPreview.print(this);
		/*
		PrintPreview pp = new PrintPreview(this,"Print Previwe");
		Point p = getLocationOnScreen();
		pp.setLocation(Math.round(p.x+(getVisibleRect().width-pp.getWidth())/2), Math.round(p.y)+(getVisibleRect().height-pp.getHeight())/2);
		pp.setVisible(true);
		*/
		PrintPreview pp = new PrintPreview(NSDControl.getFrame(),this);
		Point p = getLocationOnScreen();
		pp.setLocation(Math.round(p.x+(getVisibleRect().width-pp.getWidth())/2+this.getVisibleRect().x),
					   Math.round(p.y)+(getVisibleRect().height-pp.getHeight())/2+this.getVisibleRect().y);
		pp.setVisible(true);
	}

	// START KGU #2 2015-11-19
	/*****************************************
	 * arrange method
	 *****************************************/
	public void arrangeNSD()
	{
		//System.out.println("Arranger button pressed!");
		Arranger arr = Arranger.getInstance();
		arr.addToPool(root, NSDControl.getFrame());
		arr.setVisible(true);
		// KGU#280 2016-10-11: Obsolete now
		//isArrangerOpen = true;	// Gives the Executor a hint where to find a subroutine pool
	}
	// END KGU#2 2015-11-19
	
	// START KGU#125 2016-01-06: Possibility to adopt a diagram if it's orphaned
	public void adoptArrangedOrphanNSD(Root root)
	{
		if (isArrangerOpen())
		{
			Arranger arr = Arranger.getInstance();
			arr.addToPool(root, NSDControl.getFrame());			
		}
	}
	// END KGU#125 2016-01-06

	/*****************************************
	 * about method
	 *****************************************/
	public void aboutNSD()
	{
		About about = new About(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		about.setLocation(Math.round(p.x+(getVisibleRect().width-about.getWidth())/2+this.getVisibleRect().x),
						  Math.round(p.y)+(getVisibleRect().height-about.getHeight())/2+this.getVisibleRect().y);
		// START KGU#300 2016-12-02: Enh. #300 - Add info about newer version if enabled
		String newVersion = this.getLatestVersionIfNewer();
		if (newVersion != null) {
			about.lblVersion.setText(about.lblVersion.getText() + " (" + Menu.msgNewerVersionAvail.getText().replace("%", newVersion) + ")");
		}
		// END KGU#300 2016-12-02
		about.setVisible(true);
	}

	/*****************************************
	 * export picture method
	 *****************************************/
	public void exportPNGmulti()
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24
		
		// START KGU#41 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll();
		// END KGU#41 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as Multi-PNG ...");
		// set directory
		if (lastExportDir!=null)
		{
			dlgSave.setCurrentDirectory(lastExportDir);
		}
		else if(root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: There is already a suitable method on root
//		String nsdName = root.getText().get(0);
//		nsdName.replace(':', '_');
//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU#170 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.PNGFilter());
		PNGFilter filter = new PNGFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		pop.setVisible(false);	// Issue #143: Hide the current comment popup if visible
		// END KGU#170 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION)
		{
			lastExportDir=dlgSave.getSelectedFile().getParentFile();
			String filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".png"))
			{
				filename+=".png";
			}

			// START KGU#224 2016-07-28: Issue #209  Test was nonsense since the actual file names will be different
			//File file = new File(filename);
			File file = new File(filename.replace(".png", "-00-00.png"));
			// END KGU#224 2016-07-28
			boolean writeDown = true;
            
			if(file.exists())
			{
				int response = JOptionPane.showConfirmDialog (null,
						Menu.msgOverwriteFiles.getText(),
						Menu.btnConfirmOverwrite.getText(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
				{
					writeDown=false;
				}
			}
			if(writeDown==true)
			{
				// START KGU#218 2016-07-28: Issue #206 Localization efforts
				//int cols = Integer.valueOf(JOptionPane.showInputDialog(null, "Into how many columns do you want to split the output?", "1"));
				//int rows = Integer.valueOf(JOptionPane.showInputDialog(null, "Into how many rows do you want to split the output?", "3"));
				int cols = Integer.valueOf(JOptionPane.showInputDialog(null, Menu.msgDialogExpCols.getText(), "1"));
				int rows = Integer.valueOf(JOptionPane.showInputDialog(null, Menu.msgDialogExpRows.getText(), "3"));
				// END KGU#218 2016-07-28

				BufferedImage image = new BufferedImage(root.width+1,root.height+1,BufferedImage.TYPE_4BYTE_ABGR);
				// START KGU#221 2016-07-28: Issue #208 Need to achieve transparent background
				//printAll(image.getGraphics());
				redraw(image.createGraphics());
				// END KGU#221 2016-07-28
				// source: http://answers.yahoo.com/question/index?qid=20110821001157AAcdXVk
				// source: http://kalanir.blogspot.com/2010/02/how-to-split-image-into-chunks-java.html
				try
				{
					// 1. Load image file into memory
					//File file = new File("mario.png"); // mario.png in the same working directory
					//FileInputStream fis = new FileInputStream(file);
					//BufferedImage image = ImageIO.read(fis);

					// 2. Decide the number of pieces, and calculate the size of each chunk
					//int rows = 4;
					//int cols = 6;
					int chunks = rows * cols;

					int chunkWidth = image.getWidth() / cols;
					int chunkHeight = image.getHeight() / rows;
					// START KGU#223 2016-07-28: Bugfix #209 - identify the integer division defects
					int widthDefect = image.getWidth() % cols;
					int heightDefect = image.getHeight() % rows;
					// END KGU#223 2016-07-28

					// 3. Define an Image array to hold image chunks
					int count = 0;
					BufferedImage imgs[] = new BufferedImage[chunks];

					// 4. Fill the Image array with split image parts
					for (int x = 0; x < rows; x++)
					{
						for (int y = 0; y < cols; y++)
						{
							//Initialize the image array with image chunks
							// START KGU#223 2016-07-28: Bugfix #209
							// We must compensate the rounding defects lest the right and lower borders should be cut 
							//imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());
							int tileWidth = chunkWidth + (y < cols-1 ? 0 : widthDefect);
							int tileHeight = chunkHeight + (x < rows-1 ? 0 : heightDefect);
							imgs[count] = new BufferedImage(tileWidth, tileHeight, image.getType());
							// END KGU#223 2016-07-28
							
							// draws the image chunk
							Graphics2D gr = imgs[count++].createGraphics();
							// START KGU#223 2016-07-28: Bugfix #209
							//gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
							// We need to achieve transparent background
							gr.drawImage(image, 0, 0, tileWidth, tileHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + tileWidth, chunkHeight * x + tileHeight, null);
							// END KGU#223 2016-07-28
							gr.dispose();
						}
					}

					// 5. Save mini images into image files
					// START KGU#224 2016-07-28: Issue #209 - provide the original base name
					file = new File(filename);
					filename = file.getAbsolutePath();
					// END KGU#224 2016-07-28
					for (int i = 0; i < imgs.length; i++)
					{
						// START KGU#224 2016-07-28: Issue #209 - Better file name coding
						//File f = new File(file.getAbsolutePath().replace(".png", "-"+i+".png"));
						File f = new File(filename.replace(".png", String.format("-%1$02d-%2$02d.png", i / cols, i % cols)));
						// END KGU#224 2016-07-28
						ImageIO.write(imgs[i], "png", f);
					}     
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(this,
							Menu.msgErrorImageSave.getText(),
							Menu.msgTitleError.getText(), 
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null)
		{
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}
        
	public void exportPNG()
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU#41 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll();
		// END KGU#41 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as PNG ...");
		// set directory
		if (lastExportDir!=null)
		{
			dlgSave.setCurrentDirectory(lastExportDir);
		}
		else if(root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: There is already a suitable method
//		String nsdName = root.getText().get(0);
//		nsdName.replace(':', '_');
//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.PNGFilter());
		PNGFilter filter = new PNGFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		pop.setVisible(false);	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION)
		{
			lastExportDir=dlgSave.getSelectedFile().getParentFile();
			String filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".png"))
			{
				filename+=".png";
			}

			File file = new File(filename);
			boolean writeDown = true;

			if(file.exists())
			{
				int response = JOptionPane.showConfirmDialog (null,
						Menu.msgOverwriteFile.getText(),
						Menu.btnConfirmOverwrite.getText(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
				{
					writeDown=false;
				}
			}
			if(writeDown==true)
			{
				BufferedImage bi = new BufferedImage(root.width+1,root.height+1,BufferedImage.TYPE_4BYTE_ABGR);
				// START KGU#221 2016-07-28: Issue #208 Need to achieve transparent background
				//printAll(bi.getGraphics());
				redraw(bi.createGraphics());
				// END KGU#221 2016-07-28
				try
				{
					ImageIO.write(bi, "png", file);
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(this,
							Menu.msgErrorImageSave.getText(),
							Menu.msgTitleError.getText(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null)
		{
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}

	public void exportEMF()
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24
		
		// START KGU#41 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll();
		// END KGU#41 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as EMF ...");
		// set directory
		if (lastExportDir!=null)
		{
			dlgSave.setCurrentDirectory(lastExportDir);
		}
		else if(root.getFile() != null)
		{
			dlgSave.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: D.R.Y. - There is already a suitable method
		//		String nsdName = root.getText().get(0);
		//		nsdName.replace(':', '_');
		//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.EMFFilter());
		EMFFilter filter = new EMFFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		pop.setVisible(false);	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION)
		{
			lastExportDir=dlgSave.getSelectedFile().getParentFile();
			String filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".emf"))
			{
				filename+=".emf";
			}

			File file = new File(filename);
			boolean writeDown = true;

			if(file.exists())
			{
				int response = JOptionPane.showConfirmDialog (null,
						Menu.msgOverwriteFile.getText(),
						Menu.btnConfirmOverwrite.getText(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
				{
					writeDown=false;
				}
			}
			if(writeDown==true)
			{
				try
				{
					EMFGraphics2D emf = new EMFGraphics2D(new FileOutputStream(filename),new Dimension(root.width+12, root.height+12)) ;

					emf.startExport();
					lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(emf);
					lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
					myrect.left+=6;
					myrect.top+=6;
					root.draw(c,myrect);
					emf.endExport();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restor old selection
		selected = wasSelected;
		if (selected != null)
		{
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}

	public void exportSVG() // does not work!!
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24
		
		// START KGU#41 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll();
		// END KGU#41 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as SVG ...");
		// set directory
		if (lastExportDir!=null)
		{
			dlgSave.setCurrentDirectory(lastExportDir);
		}
		else if(root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: D.R.Y. - there is already a suitable method
//		String nsdName = root.getText().get(0);
//		nsdName.replace(':', '_');
//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.SVGFilter());
		SVGFilter filter = new SVGFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		pop.setVisible(false);	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION)
		{
			lastExportDir=dlgSave.getSelectedFile().getParentFile();
			String filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".svg"))
			{
				filename+=".svg";
			}

			File file = new File(filename);
			boolean writeDown = true;

			if(file.exists())
			{
				int response = JOptionPane.showConfirmDialog (null,
						Menu.msgOverwriteFile.getText(),
						Menu.btnConfirmOverwrite.getText(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
				{
					writeDown=false;
				}
			}
			if(writeDown==true)
			{
				try
				{
					SVGGraphics2D svg = new SVGGraphics2D(new FileOutputStream(filename),new Dimension(root.width+12, root.height+12)) ;
					svg.startExport();
					lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(svg);
					lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
					myrect.left+=6;
					myrect.top+=6;
					root.draw(c,myrect);
					svg.endExport();

					// re-read the file ...
					StringBuffer buffer = new StringBuffer();
					InputStreamReader isr = new InputStreamReader(new FileInputStream(filename));
					Reader in = new BufferedReader(isr);
					int ch;
					while ((ch = in.read()) > -1)
					{
						buffer.append((char)ch);
					}
					// START KGU 2015-12-04
					in.close();
					// END KGU 2015-12-04

					// ... and encode it UTF-8
					FileOutputStream fos = new FileOutputStream(filename);
					Writer out = new OutputStreamWriter(fos, "UTF-8");
					out.write(buffer.toString());
					out.close();

				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		//unselectAll();
		selected = wasSelected;
		if (selected != null)
		{
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}

	public void exportSWF()
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll();
		// END KGU 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as SWF ...");
		// set directory
		if (lastExportDir!=null)
		{
			dlgSave.setCurrentDirectory(lastExportDir);
		}
		else if(root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: D.R.Y. - there is already a suitable method
//		String nsdName = root.getText().get(0);
//		nsdName.replace(':', '_');
//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.SWFFilter());
		SWFFilter filter = new SWFFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		pop.setVisible(false);	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION)
		{
			lastExportDir=dlgSave.getSelectedFile().getParentFile();
			String filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".swf"))
			{
				filename+=".swf";
			}

			File file = new File(filename);
			boolean writeDown = true;

			if(file.exists())
			{
				int response = JOptionPane.showConfirmDialog (null,
						Menu.msgOverwriteFile.getText(),
						Menu.btnConfirmOverwrite.getText(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
				{
					writeDown=false;
				}
			}
			if(writeDown==true)
			{
				try
				{
					SWFGraphics2D svg = new SWFGraphics2D(new FileOutputStream(filename),new Dimension(root.width+12, root.height+12)) ;

					svg.startExport();
					lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(svg);
					lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
					myrect.left+=6;
					myrect.top+=6;
					root.draw(c,myrect);
					svg.endExport();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null)
		{
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}

	public void exportPDF()
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll();
		// END KGU 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as PDF ...");
		// set directory
		if (lastExportDir!=null)
		{
			dlgSave.setCurrentDirectory(lastExportDir);
		}
		else if(root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: D.R.Y. - there is already a suitable method
//		String nsdName = root.getText().get(0);
//		nsdName.replace(':', '_');
//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.PDFFilter());
		PDFFilter filter = new PDFFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		pop.setVisible(false);	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION)
		{
			lastExportDir=dlgSave.getSelectedFile().getParentFile();
			String filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".pdf"))
			{
				filename+=".pdf";
			}

			File file = new File(filename);
			boolean writeDown = true;

			if(file.exists())
			{
				int response = JOptionPane.showConfirmDialog (null,
						Menu.msgOverwriteFile.getText(),
						Menu.btnConfirmOverwrite.getText(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION)
				{
					writeDown=false;
				}
			}
			if(writeDown==true)
			{
				try
				{
					PDFGraphics2D svg = new PDFGraphics2D(new FileOutputStream(filename),new Dimension(root.width+12, root.height+12)) ;

					svg.startExport();
					lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(svg);
					lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
					myrect.left+=6;
					myrect.top+=6;
					root.draw(c,myrect);
					svg.endExport();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null)
		{
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}


	/*****************************************
	 * export code methods
	 *****************************************/
	public void importPAS()
	{
		// only save if something has been changed
		saveNSD(true);

		String filename = "";

		JFileChooser dlgOpen = new JFileChooser();
		dlgOpen.setDialogTitle(Menu.msgTitleImport.getText().replace("%", "Pascal"));
		// set directory
		if(root.getFile()!=null)
		{
			dlgOpen.setCurrentDirectory(root.getFile());
		}
		else
		{
			dlgOpen.setCurrentDirectory(currentDirectory);
		}

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgOPen.addChoosableFileFilter(new PascalFilter());
		PascalFilter filter = new PascalFilter();
		dlgOpen.addChoosableFileFilter(filter);
		dlgOpen.setFileFilter(filter);
		pop.setVisible(false);	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgOpen.showOpenDialog(NSDControl.getFrame());
		filename=dlgOpen.getSelectedFile().getAbsoluteFile().toString();

		if (result == JFileChooser.APPROVE_OPTION)
		{
			// load and parse source-code
			D7Parser d7 = new D7Parser("D7Grammar.cgt");
			// START KGU#194 2016-05-08: Bugfix #185 - mechanism for multiple roots per file
			//Root rootNew = d7.parse(filename);
			// START KGU#265 2016-09-28: Enh. #253 brought the Charset configuration. So make use of it.
			//List<Root> newRoots = d7.parse(filename, "ISO-8859-1");
			Ini ini = Ini.getInstance();
			List<Root> newRoots = d7.parse(filename, ini.getProperty("impImportCharset", "ISO-8859-1"));
			// END KGU#265 2016-09-28
			// END KGU#194 2016-05-08
			if (d7.error.equals(""))
			{
				boolean hil = root.hightlightVars;
				// START KGU#194 2016-05-08: Bugfix #185 - there may be multiple routines 
				Root firstRoot = null;
				//root = rootNew;
				Iterator<Root> iter = newRoots.iterator();
				if (iter.hasNext()){
					firstRoot = iter.next();
				}
				while (iter.hasNext())
				{
					root = iter.next();
					root.hightlightVars = hil;
					// The Root must be marked for saving
					root.setChanged();
					// ... and be added to the Arranger
					this.arrangeNSD();
				}
				if (firstRoot != null)
				{
					root = firstRoot;
				// END KGU#194 2016-05-08
					root.hightlightVars = hil;
					// START KGU#183 2016-04-24: Enh. #169
					selected = root;
					selected.setSelected(true);
					// END KGU#183 2016-04-24
					// START KGU#192 2016-05-02: #184 - The Root must be marked for saving
					root.setChanged();
					// END KGU#192 2016-05-02
				// START KGU#194 2016-05-08: Bugfix #185 - multiple routines per file
				}
				// END KGU#194 2016-05-08
			}
			else
			{
				// show error
				// START KGU 2016-01-11: Yes and No buttons somewhat strange...
				//JOptionPane.showOptionDialog(null,d7.error,
				//							 "Parser Error",
				//							 JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
				JOptionPane.showMessageDialog(null, d7.error,
						Menu.msgTitleParserError.getText(),
						JOptionPane.ERROR_MESSAGE, null);
				// END KGU 2016-01-11
			}

			redraw();
			analyse();
		}
	} 

	/*****************************************
	 * export code methods
	 *****************************************/
	public void export(String _generatorClassName)
	{
		try
		{
			Class<?> genClass = Class.forName(_generatorClassName);
			Generator gen = (Generator) genClass.newInstance();
			// START KGU#170 2016-04-01: Issue #143
			pop.setVisible(false);	// Hide the current comment popup if visible
			// END KGU#170 2016-04-01
			gen.exportCode(root,currentDirectory,NSDControl.getFrame());
		}
		catch(Exception ex)
		{
			String message = ex.getLocalizedMessage();
			if (message == null) message = ex.getMessage();
			JOptionPane.showMessageDialog(this,
					Menu.msgErrorUsingGenerator.getText().replace("%", _generatorClassName)+"\n" + message,
					Menu.msgTitleError.getText(),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// START KGU#208 2016-07-22: Enh. #199
	/*****************************************
	 * help method
	 *****************************************/

	/**
	 * Tries to open the online User Guide in the browser
	 */
	public void helpNSD()
	{
		// START KGU#250 2016-09-17: Issue #245 (defective Linux integration workaround)
//		try {
//			Desktop.getDesktop().browse(new URI("http://help.structorizer.fisch.lu/index.php"));
//		}
//		catch(Exception ex)
//		{
//			ex.printStackTrace();
//			// We may get here if there is no standard browser or no standard application for web links
//			// configured (as issue #245 proved) - in case of missing network access the browser will
//			// rather show a message itself, though.
//			String message = ex.getLocalizedMessage();
//			if (message == null) message = ex.getMessage();
//			JOptionPane.showMessageDialog(null,
//					message,
//					Menu.msgTitleURLError.getText(),
//					JOptionPane.ERROR_MESSAGE);
//		}
		String help = "http://help.structorizer.fisch.lu/index.php";
		boolean isLaunched = false;
		try {
			isLaunched = lu.fisch.utils.Desktop.browse(new URI("http://help.structorizer.fisch.lu/index.php"));
		} catch (URISyntaxException ex) {
			ex.printStackTrace();
		}
		if (!isLaunched)
		{
			String message = Menu.msgBrowseFailed.getText().replace("%", help);
			JOptionPane.showMessageDialog(null,
			message,
			Menu.msgTitleURLError.getText(),
			JOptionPane.ERROR_MESSAGE);
		}
		// END KGU#250 2016-09-17
	}
	// END KGU#208 2016-07-22
	
	/*****************************************
	 * update method
	 *****************************************/

	public void updateNSD()
	// START KGU#300 2016-12-02: Enh. #300
	{
		updateNSD(true);
	}
	
	public void updateNSD(boolean evenWithoutNewerVersion)
	// END KGU#300 2016-12-02
	{
		// KGU#35 2015-07-29: Bob's code adopted with slight modification (Homepage URL put into a variable) 
		final String home = "http://structorizer.fisch.lu";
		
		// START KGU#300 2016-12-02: Enh. #300
		String latestVersion = getLatestVersionIfNewer();
		if (!evenWithoutNewerVersion && latestVersion == null) {
			return;
		}
		// END KGU#300 2016-12-02
		
		try {
			// START KGU#247 2016-09-17: Issue #243/#245 Translation support for update window content
			//JEditorPane ep = new JEditorPane("text/html","<html><font face=\"Arial\">Goto <a href=\"" + home + "\">" + home + "</a> to look for updates<br>and news about Structorizer.</font></html>");
			String fontAttr = "";
			double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor","1"));
			if (scaleFactor > 1) {
				int fontSize = (int)(3*scaleFactor);
				fontAttr = " size="+fontSize;
			}
			// START KGU#300 2016-12-02: Enh. #300
			String versionInfo = "";
			if (latestVersion != null) {
				versionInfo = Menu.msgNewerVersionAvail.getText().replace("%", latestVersion) + "<br><br>";
			}
			// END KGU#300 2016-12-02
			JEditorPane ep = new JEditorPane("text/html","<html><font face=\"Arial\""+fontAttr+">" +
					// START KGU#300 2016-12-02: Enh. #300
					versionInfo +
					// END KGU#300 2016-12-02
					Menu.msgGotoHomepage.getText().replace("%", "<a href=\"" + home + "\">" + home + "</a>") +
					"</font></html>");
			// END KGU#247 2016-09-17
			ep.addHyperlinkListener(new HyperlinkListener()
			{
				@Override
				public void hyperlinkUpdate(HyperlinkEvent evt)
				{
					if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					{
						// START KGU#250 2016-09-17: Issue #245 (defective Linux integration workaround)
						//try {
						//	Desktop.getDesktop().browse(evt.getURL().toURI());
						//}
						//catch(Exception ex)
						//{
						//	ex.printStackTrace();
						//}
						String errorMessage = null;
						try {
							if (!lu.fisch.utils.Desktop.browse(evt.getURL().toURI()))
							{
								errorMessage = Menu.msgBrowseFailed.getText().replace("%", evt.getURL().toString());
							};
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
							errorMessage = ex.getLocalizedMessage();
							if (errorMessage == null) errorMessage = ex.getMessage();
						}
						if (errorMessage != null)
						{
							JOptionPane.showMessageDialog(null,
									errorMessage,
									Menu.msgTitleURLError.getText(),
									JOptionPane.ERROR_MESSAGE);

						}
						// END KGU#250 2016-09-17
					}
				}
			});
			ep.setEditable(false);
			JLabel label = new JLabel();
			ep.setBackground(label.getBackground());

			JOptionPane.showMessageDialog(this, ep);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	// START KGU#300 2016-12-02 Enh. #300 Support for version retrieval
	private String retrieveLatestVersion()
	{
		final String http_url = "http://structorizer.fisch.lu/version.txt";

		String version = null;
		if (this.retrieveVersion) {
			try {

				URL url = new URL(http_url);
				HttpURLConnection con = (HttpURLConnection)url.openConnection();

				if (con!=null) {

					BufferedReader br = 
							new BufferedReader(
									new InputStreamReader(con.getInputStream()));

					String input;
					while ((input = br.readLine()) != null && version == null){
						if (input.matches("\\d+\\.\\d+([-.][0-9]+)?")) {
							version = input;
						}
					}
					br.close();

				}

			} catch (MalformedURLException e) {
				System.err.println("Diagram.retrievaLatestVersion: " + e.toString());
			} catch (IOException e) {
				System.err.println("Diagram.retrievaLatestVersion: " + e.toString());
			}
		}
		return version;
	}

	// START KGU#300 2016-12-06: Not actually needed
//	private static int[] splitVersionString(String version)
//	{
//		StringList versionParts = StringList.explode(version, "\\.");
//		versionParts = StringList.explode(versionParts, "-");
//		int[] versionNumbers = new int[versionParts.count()];
//		for (int i = 0; i < versionParts.count(); i++) {
//			try {
//				versionNumbers[i] = Integer.parseInt(versionParts.get(i));
//			}
//			catch (NumberFormatException ex) {
//				versionNumbers[i] = 0;
//			}
//		}
//		return versionNumbers;
//	}
	// END KGU#300 2016-12-06
	
	public String getLatestVersionIfNewer()
	{
		int cmp = 0;
		String latestVerStr = retrieveLatestVersion();
		if (latestVerStr != null) {
			// START KGU#300 2016-12-06: The lexicographic comparison is quite perfect here
//			int[] thisVersion = splitVersionString(Element.E_VERSION);
//			int[] currVersion = splitVersionString(latestVerStr);
//			int minLen = Math.min(thisVersion.length, currVersion.length);
//			for (int i = 0; i < minLen && cmp == 0; i++) {
//				if (currVersion[i] < thisVersion[i]) {
//					cmp = -1;
//				}
//				else if (currVersion[i] > thisVersion[i]) {
//					cmp = 1;
//				}
//			}
//			if (cmp == 0 && minLen < currVersion.length) {
//				cmp = 1;
//			}
			cmp = latestVerStr.compareTo(Element.E_VERSION);
			// END KGU#300 2016-12-06
		}
		return (cmp > 0 ? latestVerStr : null);
	}
	
	public void setRetrieveVersion(boolean retrieveVersion)
	{
		this.retrieveVersion = retrieveVersion;
	}
	// END KGU#300 2016-12-02

	/*****************************************
	 * the preferences dialog methods
	 *****************************************/

	public void colorsNSD()
	{
		Colors colors = new Colors(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		colors.setLocation(Math.round(p.x+(getVisibleRect().width-colors.getWidth())/2+this.getVisibleRect().x),
						   Math.round(p.y+(getVisibleRect().height-colors.getHeight())/2+this.getVisibleRect().y));

		// set fields
		colors.color0.setBackground(Element.color0);
		colors.color1.setBackground(Element.color1);
		colors.color2.setBackground(Element.color2);
		colors.color3.setBackground(Element.color3);
		colors.color4.setBackground(Element.color4);
		colors.color5.setBackground(Element.color5);
		colors.color6.setBackground(Element.color6);
		colors.color7.setBackground(Element.color7);
		colors.color8.setBackground(Element.color8);
		colors.color9.setBackground(Element.color9);

		colors.pack();
		colors.setVisible(true);

		// get fields
		Element.color0=colors.color0.getBackground();
		Element.color1=colors.color1.getBackground();
		Element.color2=colors.color2.getBackground();
		Element.color3=colors.color3.getBackground();
		Element.color4=colors.color4.getBackground();
		Element.color5=colors.color5.getBackground();
		Element.color6=colors.color6.getBackground();
		Element.color7=colors.color7.getBackground();
		Element.color8=colors.color8.getBackground();
		Element.color9=colors.color9.getBackground();

		NSDControl.updateColors();

		// save fields to ini-file
		Element.saveToINI();

	}

	public void preferencesNSD()
	{
		Preferences preferences = new Preferences(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		preferences.setLocation(Math.round(p.x+(getVisibleRect().width-preferences.getWidth())/2+this.getVisibleRect().x),
								Math.round(p.y+(getVisibleRect().height-preferences.getHeight())/2+this.getVisibleRect().y));

		// set fields
		preferences.edtAltT.setText(Element.preAltT);
		preferences.edtAltF.setText(Element.preAltF);
		preferences.edtAlt.setText(Element.preAlt);
		preferences.txtCase.setText(Element.preCase);
		preferences.edtFor.setText(Element.preFor);
		preferences.edtWhile.setText(Element.preWhile);
		preferences.edtRepeat.setText(Element.preRepeat);
                
		preferences.altPadRight.setSelected(Element.altPadRight);

		preferences.pack();
		preferences.setVisible(true);

		// get fields
		Element.preAltT=preferences.edtAltT.getText();
		Element.preAltF=preferences.edtAltF.getText();
		Element.preAlt=preferences.edtAlt.getText();
		Element.preCase=preferences.txtCase.getText();
		Element.preFor=preferences.edtFor.getText();
		Element.preWhile=preferences.edtWhile.getText();
		Element.preRepeat=preferences.edtRepeat.getText();
		Element.altPadRight=preferences.altPadRight.isSelected();

		// save fields to ini-file
		Element.saveToINI();
                redraw();
	}

	public void parserNSD()
	{
		ParserPreferences parserPreferences = new ParserPreferences(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		parserPreferences.setLocation(Math.round(p.x+(getVisibleRect().width-parserPreferences.getWidth())/2+this.getVisibleRect().x),
								Math.round(p.y+(getVisibleRect().height-parserPreferences.getHeight())/2+this.getVisibleRect().y));
		
		// set fields
		parserPreferences.edtAltPre.setText(D7Parser.getKeyword("preAlt"));
		parserPreferences.edtAltPost.setText(D7Parser.getKeyword("postAlt"));
		parserPreferences.edtCasePre.setText(D7Parser.getKeyword("preCase"));
		parserPreferences.edtCasePost.setText(D7Parser.getKeyword("postCase"));
		parserPreferences.edtForPre.setText(D7Parser.getKeyword("preFor"));
		parserPreferences.edtForPost.setText(D7Parser.getKeyword("postFor"));
		// START KGU#3 2015-11-08: New configurable separator for FOR loop step const
		parserPreferences.edtForStep.setText(D7Parser.getKeyword("stepFor"));
		// END KGU#3 2015-11-08
		// START KGU#61 2016-03-21: New configurable keywords for FOR-IN loop
		parserPreferences.edtForInPre.setText(D7Parser.getKeyword("preForIn"));
		parserPreferences.edtForInPost.setText(D7Parser.getKeyword("postForIn"));
		// END KGU#61 2016-03-21
		parserPreferences.edtWhilePre.setText(D7Parser.getKeyword("preWhile"));
		parserPreferences.edtWhilePost.setText(D7Parser.getKeyword("postWhile"));
		parserPreferences.edtRepeatPre.setText(D7Parser.getKeyword("preRepeat"));
		parserPreferences.edtRepeatPost.setText(D7Parser.getKeyword("postRepeat"));
		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
		parserPreferences.edtJumpLeave.setText(D7Parser.getKeyword("preLeave"));
		parserPreferences.edtJumpReturn.setText(D7Parser.getKeyword("preReturn"));
		parserPreferences.edtJumpExit.setText(D7Parser.getKeyword("preExit"));
		// END KGU#78 2016-03-25
		parserPreferences.edtInput.setText(D7Parser.getKeyword("input"));
		parserPreferences.edtOutput.setText(D7Parser.getKeyword("output"));
		// START KGU#165 2016-03-25: We need a transparent decision here
		parserPreferences.chkIgnoreCase.setSelected(D7Parser.ignoreCase);
		// END KGU#165 2016-03-25
		
		parserPreferences.pack();
		parserPreferences.setVisible(true);

		if(parserPreferences.OK)
		{
			// START KGU#258 2016-09-26: Enh. #253 - prepare the old settings for a refactoring
			HashMap<String, StringList> oldKeywordMap = null;
			boolean wasCaseIgnored = D7Parser.ignoreCase;
			boolean considerRefactoring = root.children.getSize() > 0
					|| isArrangerOpen() && Arranger.getInstance().getAllRoots().size() > 0;
			if (considerRefactoring)
			{
				oldKeywordMap = new LinkedHashMap<String, StringList>();
				for (String key: D7Parser.keywordSet())
				{
					// START KGU#288 2016-11-06: Issue #279 - method getOrDefault may not be available
					//String keyword = D7Parser.keywordMap.getOrDefault(key, "");
					//if (!keyword.trim().isEmpty())
					String keyword = D7Parser.getKeyword(key);
					if (keyword != null && !keyword.trim().isEmpty())
					// END KGU#288 2016-11-06
					{
						// Complete strings aren't likely to be found in a key, so don't bother
						oldKeywordMap.put(key, Element.splitLexically(keyword,  false));
					}
				}
			}
			// END KGU#258 2016-09-26

			// get fields
			D7Parser.setKeyword("preAlt", parserPreferences.edtAltPre.getText());
			D7Parser.setKeyword("postAlt", parserPreferences.edtAltPost.getText());
			D7Parser.setKeyword("preCase", parserPreferences.edtCasePre.getText());
			D7Parser.setKeyword("postCase", parserPreferences.edtCasePost.getText());
			D7Parser.setKeyword("preFor", parserPreferences.edtForPre.getText());
			D7Parser.setKeyword("postFor", parserPreferences.edtForPost.getText());
			// START KGU#3 2015-11-08: New configurable separator for FOR loop step const
			D7Parser.setKeyword("stepFor", parserPreferences.edtForStep.getText());
			// END KGU#3 2015-11-08
			// START KGU#61 2016-03-21: New configurable keywords for FOR-IN loop
			D7Parser.setKeyword("preForIn", parserPreferences.edtForInPre.getText());
			D7Parser.setKeyword("postForIn", parserPreferences.edtForInPost.getText());
			// END KGU#61 2016-03-21
			D7Parser.setKeyword("preWhile", parserPreferences.edtWhilePre.getText());
			D7Parser.setKeyword("postWhile", parserPreferences.edtWhilePost.getText());
			D7Parser.setKeyword("preRepeat", parserPreferences.edtRepeatPre.getText());
			D7Parser.setKeyword("postRepeat", parserPreferences.edtRepeatPost.getText());
    		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
    		D7Parser.setKeyword("preLeave", parserPreferences.edtJumpLeave.getText());
    		D7Parser.setKeyword("preReturn", parserPreferences.edtJumpReturn.getText());
    		D7Parser.setKeyword("preExit", parserPreferences.edtJumpExit.getText());
    		// END KGU#78 2016-03-25
			D7Parser.setKeyword("input", parserPreferences.edtInput.getText());
			D7Parser.setKeyword("output", parserPreferences.edtOutput.getText());
			// START KGU#165 2016-03-25: We need a transparent decision here
			D7Parser.ignoreCase = parserPreferences.chkIgnoreCase.isSelected();
			// END KGU#165 2016-03-25

			// save fields to ini-file
			D7Parser.saveToINI();
			
			// START KGU#258 2016-09-26: Enh. #253 - now try a refactoring if specified
			boolean redrawn = false;
			if (considerRefactoring && offerRefactoring(oldKeywordMap))
			{
				boolean refactorAll = oldKeywordMap.containsKey("refactorAll");
				redrawn = refactorDiagrams(oldKeywordMap, refactorAll, wasCaseIgnored);
			}
			// END KGU#258 2016-09-26

			// START KGU#136 2016-03-31: Bugfix #97 - cached bounds may have to be invalidated
			if (Element.E_VARHIGHLIGHT && !redrawn)
			{
				// Parser keyword changes may have an impact on the text width ...
				this.resetDrawingInfo(true);
				
				// START KGU#258 2016-09-26: Bugfix #253 ... and Jumps and loops
				analyse();
				// END KGU#258 2016-09-26

				// redraw diagram
				redraw();
			}
			// END KGU#136 2016-03-31
			
		}
	}
	
	// START KGU#258 2016-09-26: Enh. #253: A set of helper methods for refactoring
	/**
	 * (To be called after a preference file has been loaded explicitly on user demand.)
	 * Based on the refactoringData collected before the loading, a difference analysis
	 * between the old and new parser preferences will be done. If changes are detected
	 * and there are non-trivial Roots then a dialog box will be popped up showing
	 * the changes and offering to refactor the current or all diagrams. If the user
	 * agrees then the respective code will be added to the refactoringData and true
	 * will be returned, otherwise false.
	 * @param refactoringData - tokenized previous non-empty parser preferences
	 * @return true if a refactoring makes sense, false otherwise
	 */
	public boolean offerRefactoring(HashMap<String, StringList> refactoringData)
	{
		// Since this method is always called after a preference file has been loaded,
		// we update the preferred export code for the doButtons() call, though it
		// has nothing to do with refactoring
		this.prefGeneratorName = Ini.getInstance().getProperty("genExportPreferred", this.prefGeneratorName);
		
		// No refectoring data was collected then we are done here ...
		if (refactoringData == null) return false;
		
		// Otherwise we look for differences between old and new parser preferences
		StringList replacements = new StringList();
		for (HashMap.Entry<String,StringList> entry: refactoringData.entrySet())
		{
			String oldValue = entry.getValue().concatenate();
			// START KGU#288 2016-11-06: Issue #279 - Method getOrDefault() missing in OpenJDK
			//String newValue = D7Parser.getKeywordOrDefault(entry.getKey(), "");
			String newValue = D7Parser.getKeywordOrDefault(entry.getKey(), "");
			// END KGU#288 2016-11-06
			if (!oldValue.equals(newValue))
			{
				replacements.add("   " + entry.getKey() + ": \"" + oldValue + "\" -> \"" + newValue + "\"");
			}
		}
		// Only offer the question if there are relevant replacements and at least one non-empty or parked Root
		if (replacements.count() > 0 && (root.children.getSize() > 0 || isArrangerOpen() && !Arranger.getInstance().getAllRoots().isEmpty()))
		{
			String[] options = {
					Menu.lblRefactorNone.getText(),
					Menu.lblRefactorCurrent.getText(),
					Menu.lblRefactorAll.getText()
			};
			int answer = JOptionPane.showOptionDialog(this,
					Menu.msgRefactoringOffer.getText().replace("%", "\n" + replacements.getText() + "\n"),
					Menu.msgTitleQuestion.getText(), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options, options[0]);
			if (answer != 0 && answer != JOptionPane.CLOSED_OPTION)
			{
				if (D7Parser.ignoreCase)
				{
					refactoringData.put("ignoreCase", StringList.getNew("true"));
				}
				if (answer == 2)
				{
					refactoringData.put("refactorAll", StringList.getNew("true"));
				}
				return true;
			}
		}
		return false;
	}
	
	public void refactorNSD(HashMap<String, StringList> refactoringData)
	{
		if (refactoringData != null)
		{
			refactorDiagrams(refactoringData,
					refactoringData.containsKey("refactorAll"),
					refactoringData.containsKey("ignoreCase")
					);
		}
	}
	
	private boolean refactorDiagrams(HashMap<String, StringList> oldKeywordMap, boolean refactorAll, boolean wasCaseIgnored)
	{
		boolean redrawn = false;
		if (oldKeywordMap != null && !oldKeywordMap.isEmpty())
		{
			final class Refactorer implements IElementVisitor
			{
				public HashMap<String, StringList> oldMap = null;
				boolean ignoreCase = false;

				@Override
				public boolean visitPreOrder(Element _ele) {
					_ele.refactorKeywords(oldMap, ignoreCase);
					return true;
				}

				@Override
				public boolean visitPostOrder(Element _ele) {
					// FIXME It should be okay to cut off the recursion in  post order...?
					return true;
				}
				Refactorer(HashMap<String, StringList> _keyMap, boolean _caseIndifferent)
				{
					oldMap = _keyMap;
					ignoreCase = _caseIndifferent;
				}
			};
			root.addUndo();
			root.traverse(new Refactorer(oldKeywordMap, wasCaseIgnored));
			if (refactorAll && isArrangerOpen())
			{
				// Well, we hope that the roots won't change the hash code on refactoring...
				for (Root aRoot: Arranger.getInstance().getAllRoots())
				{
					if (root != aRoot) {
						aRoot.addUndo();
						aRoot.traverse(new Refactorer(oldKeywordMap, wasCaseIgnored));
					}
				}
			}
			
			// Parser keyword changes may have an impact on the text width ...
			this.resetDrawingInfo(true);
			
			// START KGU#258 2016-09-26: Bugfix #253 ... and Jumps and loops
			analyse();
			// END KGU#258 2016-09-26

			// FIXME: This doesn't seem to work 
			doButtons();
			
			// redraw diagram
			redraw();
			
			redrawn = true;
		}
		return redrawn;
	}
	// END KGU#258 2016-09-26

	public void analyserNSD()
	{
		AnalyserPreferences analyserPreferences = new AnalyserPreferences(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		analyserPreferences.setLocation(Math.round(p.x+(getVisibleRect().width-analyserPreferences.getWidth())/2+this.getVisibleRect().x),
						   Math.round(p.y+(getVisibleRect().height-analyserPreferences.getHeight())/2+this.getVisibleRect().y));

		// set fields
		// START KGU#239 2016-08-12: Code redesign (2016-09-22: index mapping modified)
		for (int i = 1; i < analyserPreferences.checkboxes.length; i++)
		{
			analyserPreferences.checkboxes[i].setSelected(Root.check(i));
		}
		// END KGU#239 2016-08-12

		analyserPreferences.pack();
		analyserPreferences.setVisible(true);

		// get fields
		// START KGU#239 2016-08-12: Code redesign (2016-09-22: index mapping modified)
		for (int i = 1; i < analyserPreferences.checkboxes.length; i++)
		{
			Root.setCheck(i, analyserPreferences.checkboxes[i].isSelected());
		}
		// END KGU#239 2016-08-12

		// save fields to ini-file
		Root.saveToINI();

		// re-analyse
		root.getVarNames();
		analyse();
	}

    public void exportOptions()
    {
        try
        {
            Ini ini = Ini.getInstance();
            ini.load();
            ExportOptionDialoge eod = new ExportOptionDialoge(NSDControl.getFrame());
            if(ini.getProperty("genExportComments","0").equals("true"))
                eod.commentsCheckBox.setSelected(true);
            else 
                eod.commentsCheckBox.setSelected(false);
            // START KGU#16/KGU#113 2015-12-18: Enh. #66, #67
            eod.bracesCheckBox.setSelected(ini.getProperty("genExportBraces", "0").equals("true"));
            eod.lineNumbersCheckBox.setSelected(ini.getProperty("genExportLineNumbers", "0").equals("true"));
            // END KGU#16/KGU#113 2015-12-18
            // START KGU#178 2016-07-20: Enh. #160
            eod.chkExportSubroutines.setSelected(ini.getProperty("genExportSubroutines", "0").equals("true"));
            // END #178 2016-07-20
            // START KGU#162 2016-03-31: Enh. #144
            eod.noConversionCheckBox.setSelected(ini.getProperty("genExportnoConversion", "0").equals("true"));
            // END KGU#162 2016-03-31
            // START KGU#170 2016-04-01: Enh. #144 Favourite export generator
            eod.cbPrefGenerator.setSelectedItem(ini.getProperty("genExportPreferred", "Java"));
            // END KGU#170 2016-04-01
            // START KGU#168 2016-04-04: Issue #149 Charsets for export
            eod.charsetListChanged(ini.getProperty("genExportCharset", Charset.defaultCharset().name()));
            // END KGU#168 2016-04-04
            eod.setVisible(true);
            
            if(eod.goOn==true)
            {
                ini.setProperty("genExportComments", String.valueOf(eod.commentsCheckBox.isSelected()));
                // START KGU#16/KGU#113 2015-12-18: Enh. #66, #67
                ini.setProperty("genExportBraces", String.valueOf(eod.bracesCheckBox.isSelected()));
                ini.setProperty("genExportLineNumbers", String.valueOf(eod.lineNumbersCheckBox.isSelected()));
                // END KGU#16/KGU#113 2015-12-18
                // START KGU#178 2016-07-20: Enh. #160
                ini.setProperty("genExportSubroutines", String.valueOf(eod.chkExportSubroutines.isSelected()));
                // END #178 2016-07-20                
                // START KGU#162 2016-03-31: Enh. #144
                ini.setProperty("genExportnoConversion", String.valueOf(eod.noConversionCheckBox.isSelected()));
                // END KGU#162 2016-03-31
                // START KGU#170 2016-04-01: Enh. #144 Favourite export generator
                this.prefGeneratorName = (String)eod.cbPrefGenerator.getSelectedItem();
                ini.setProperty("genExportPreferred", this.prefGeneratorName);
                this.NSDControl.doButtons();
                // END KGU#170 2016-04-01
                // START KGU#168 2016-04-04: Issue #149 Charset for export
                ini.setProperty("genExportCharset", (String)eod.cbCharset.getSelectedItem());
                // END KGU#168 2016-04-04
                ini.save();
            }
        } 
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        } 
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    // START KGU#258 2016-09-26: Enh. #253
    public void importOptions()
    {
        try
        {
            Ini ini = Ini.getInstance();
            ini.load();
            ImportOptionDialog iod = new ImportOptionDialog(NSDControl.getFrame());
            iod.chkRefactorOnLoading.setSelected(ini.getProperty("impRefactorOnLoading", "false").equals("true"));
            iod.charsetListChanged(ini.getProperty("impImportCharset", Charset.defaultCharset().name()));
            iod.setVisible(true);
            
            if(iod.goOn==true)
            {
                ini.setProperty("impRefactorOnLoading", String.valueOf(iod.chkRefactorOnLoading.isSelected()));
                ini.setProperty("impImportCharset", (String)iod.cbCharset.getSelectedItem());
                ini.save();
            }
        } 
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        } 
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    // END KGU#258 2016-09-26

    // START KGU#309 2016-12-15: Enh. #310
    public void savingOptions()
    {
    	SaveOptionDialog sod = new SaveOptionDialog(NSDControl.getFrame());
    	sod.chkAutoSaveClose.setSelected(Element.E_AUTO_SAVE_ON_CLOSE);
    	sod.chkAutoSaveExecute.setSelected(Element.E_AUTO_SAVE_ON_EXECUTE);
    	sod.chkBackupFile.setSelected(Element.E_MAKE_BACKUPS);
    	sod.setVisible(true);

    	if(sod.goOn==true)
    	{
    		Element.E_AUTO_SAVE_ON_CLOSE = sod.chkAutoSaveClose.isSelected();
    		Element.E_AUTO_SAVE_ON_EXECUTE = sod.chkAutoSaveExecute.isSelected();
    		Element.E_MAKE_BACKUPS = sod.chkBackupFile.isSelected();
    	}
    }
    // END KGU#258 2016-09-26

	public void fontNSD()
	{
		FontChooser fontChooser = new FontChooser(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		fontChooser.setLocation(Math.round(p.x+(getVisibleRect().width-fontChooser.getWidth())/2+this.getVisibleRect().x),
								Math.round(p.y+(getVisibleRect().height-fontChooser.getHeight())/2+this.getVisibleRect().y));

		// set fields
		fontChooser.setFont(Element.getFont());
		fontChooser.setVisible(true);

		// get fields
		Element.setFont(fontChooser.getCurrentFont());

		// save fields to ini-file
		Element.saveToINI();
		
		// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
		this.resetDrawingInfo(true);
		// END KGU#136 2016-03-02

		// redraw diagram
		redraw();
	}

	public void fontUpNSD()
	{
		// change font size
		Element.setFont(new Font(Element.getFont().getFamily(),Font.PLAIN,Element.getFont().getSize()+2));

		// save size
		Element.saveToINI();

		// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
		this.resetDrawingInfo(true);
		// END KGU#136 2016-03-02

		// redraw diagram
		redraw();
	}

	public void fontDownNSD()
	{
		if(Element.getFont().getSize()-2>=4)
		{
			// change font size
			Element.setFont(new Font(Element.getFont().getFamily(),Font.PLAIN,Element.getFont().getSize()-2));

			// save size
			Element.saveToINI();

			// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
			this.resetDrawingInfo(true);
			// END KGU#136 2016-03-02

			// redraw diagram
			redraw();
		}
	}

	/*****************************************
	 * setter method
	 *****************************************/
	public void toggleDIN()
	{
		Element.E_DIN = !(Element.E_DIN);
		NSDControl.doButtons();
		// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
		this.resetDrawingInfo(true);
		// END KGU#136 2016-03-02		
		redraw();
	}

	public void setDIN()
	{
		Element.E_DIN = true;
		NSDControl.doButtons();
		// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
		this.resetDrawingInfo(true);
		// END KGU#136 2016-03-02
		redraw();
	}

	public boolean getDIN()
	{
		return Element.E_DIN;
	}

	public void setNice(boolean _nice)
	{
		root.isNice=_nice;
		// START KGU#137 2016-01-11: Record this change in addition to the undoable ones
		//root.hasChanged=true;
		root.setChanged();
		// END KGU#137 2016-01-11
    	// START KGU#136 2016-03-01: Bugfix #97
    	root.resetDrawingInfoUp();	// Only affects Root
    	// END KGU#136 2016-03-01
		redraw();
	}

	public boolean isNice()
	{
		return root.isNice;
	}

	public void setFunction()
	{
		// START KGU#221 2016-07-28: Bugfix #208
		if (!root.isNice && root.isProgram)
		{
			root.resetDrawingInfoUp();
		}
		// END KGU#221 2016-07-28
		root.isProgram=false;
		// START KGU#137 2016-01-11: Record this change in addition to the undoable ones
		//root.hasChanged=true;
		root.setChanged();
		// END KGU#137 2016-01-11
		// START KGU#253 2016-09-22: Enh. #249 - (un)check parameter list
		analyse();
		// END KGU#253 2016-09-22
		redraw();
	}

	public void setProgram()
	{
		// START KGU#221 2016-07-28: Bugfix #208
		if (!root.isNice && !root.isProgram)
		{
			root.resetDrawingInfoUp();
		}
		// END KGU#221 2016-07-28
		root.isProgram=true;
		// START KGU#137 2016-01-11: Record this change in addition to the undoable ones
		//root.hasChanged=true;
		root.setChanged();
		// END KGU#137 2016-01-11
		// START KGU#253 2016-09-22: Enh. #249 - (un)check parameter list
		analyse();
		// END KGU#253 2016-09-22
		redraw();
	}

	public boolean isProgram()
	{
		return root.isProgram;
	}


	public void setComments(boolean _comments)
	{
		Element.E_SHOWCOMMENTS=_comments;
		NSDControl.doButtons();
		redraw();
	}

    // START KGU#227 2016-07-31: Enh. #128
    void setCommentsPlusText(boolean _activate)
    {
    	Element.E_COMMENTSPLUSTEXT = _activate;
    	this.resetDrawingInfo(true);
    	analyse();
    	repaint();
    }
    // END KGU#227 2016-07-31

	public void setToggleTC(boolean _tc)
	{
		Element.E_TOGGLETC=_tc;
		// START KGU#136 2016-03-01: Bugfix #97
		this.resetDrawingInfo(true);
		// END KGU#136 2016-03-01
		NSDControl.doButtons();
		redraw();
	}

	public void setHightlightVars(boolean _highlight)
	{
		Element.E_VARHIGHLIGHT=_highlight;	// this isn't used for drawing, actually
		root.hightlightVars=_highlight;
		// START KGU#136 2016-03-01: Bugfix #97
		this.resetDrawingInfo(false);	// Only current root is involved
		// END KGU#136 2016-03-01
		NSDControl.doButtons();
		redraw();
	}

	public void toggleAnalyser()
	{
		setAnalyser(!Element.E_ANALYSER);
		if(Element.E_ANALYSER==true)
		{
			analyse();
		}
	}

	public void setAnalyser(boolean _analyse)
	{
		Element.E_ANALYSER = _analyse;
		NSDControl.doButtons();
	}

	// START KGU#305 2016-12-14: Enh. #305
	public void setArrangerIndex(boolean _showIndex)
	{
		this.show_ARRANGER_INDEX = _showIndex;
		NSDControl.doButtons();
	}
	public boolean showArrangerIndex()
	{
		return this.show_ARRANGER_INDEX;
	}
	// END KGU#305 216-12-14
	
	// START KGU#123 2016-01-04: Enh. #87
	public void toggleWheelMode()
	{
		setWheelCollapses(!Element.E_WHEELCOLLAPSE);
	}
	
	public void setWheelCollapses(boolean _collapse)
	{
		Element.E_WHEELCOLLAPSE = _collapse;
		if (_collapse)
		{
			this.addMouseWheelListener(this);
		}
		else
		{
			this.removeMouseWheelListener(this);
		}
		this.NSDControl.doButtons();
	}
	
	public boolean getWheelCollapses()
	{
		return Element.E_WHEELCOLLAPSE;
	}
	// END KGU#123 2016-01-04
	
	// START KGU#170 2016-04-01: Enh. #144: Maintain a preferred export generator
	public String getPreferredGeneratorName()
	{
		if (this.prefGeneratorName.isEmpty())
		{
	        try
	        {
	            Ini ini = Ini.getInstance();
	            ini.load();
	            this.prefGeneratorName = ini.getProperty("genExportPreferred", "Java");
	        } 
	        catch (FileNotFoundException ex)
	        {
	            ex.printStackTrace();
	        } 
	        catch (IOException ex)
	        {
	            ex.printStackTrace();
	        }
	    }
		return this.prefGeneratorName;
	}
	// END KGU#170 2016-04-01

	/*****************************************
	 * inputbox methods
	 *****************************************/
	// START KGU 2015-10-14: additional parameters for title customisation
	//public void showInputBox(EditData _data)
	public void showInputBox(EditData _data, String _elementType, boolean _isInsertion)
	// END KGU 2015-10-14
	{
		if(NSDControl!=null)
		{
			// START KGU#170 2016-04-01: Issue #143 - on opening the editor a comment popup should vanish
			pop.setVisible(false);
			// END KGU#170 2016-04-01
			// START KGU#3 2015-10-25: Dedicated support for FOR loops
			//InputBox inputbox = new InputBox(NSDControl.getFrame(),true);
			InputBox inputbox = null;
			if (_elementType.equals("For"))
			{
				InputBoxFor ipbFor = new InputBoxFor(NSDControl.getFrame(), true);
				// START #61 2016-09-24: After partial redesign some things work differently, now
				//if (!_isInsertion)
				//{
				if (_isInsertion)
				{
					// Split the default text to find out what style it is
					String[] forFractions = For.splitForClause(_data.text.getLongString());
					for (int i = 0; i < 4; i++)
					{
						_data.forParts.add(forFractions[i]);
					}
					if (forFractions[5] != null)
					{
						_data.forParts.add(forFractions[5]);
					}
				}
				// END KGU#61 2016-09-24
				ipbFor.txtVariable.setText(_data.forParts.get(0));
				ipbFor.txtStartVal.setText(_data.forParts.get(1));
				ipbFor.txtEndVal.setText(_data.forParts.get(2));
				ipbFor.txtIncr.setText(_data.forParts.get(3));
				// START KGU#61 2016-03-21: Enh. #84 - Consider FOR-IN loops
				//ipbFor.chkTextInput.setSelected(!_data.forPartsConsistent);
				//ipbFor.enableTextFields(!_data.forPartsConsistent);
				if (_data.forParts.count() > 4)
				{
					ipbFor.txtValueList.setText(ipbFor.forInValueList = _data.forParts.get(4));
					ipbFor.txtVariableIn.setText(_data.forParts.get(0));
				}
				boolean textMode = _data.forLoopStyle == For.ForLoopStyle.FREETEXT;
				ipbFor.chkTextInput.setSelected(textMode);
				ipbFor.enableTextFields(textMode);
				ipbFor.setIsTraversingLoop(_data.forLoopStyle == For.ForLoopStyle.TRAVERSAL);
				// END KGU#61 2016-03-21
//				}
//				else {
//					ipbFor.enableTextFields(false);
//				}
				inputbox = ipbFor;
			}
			else
			{
				inputbox = new InputBox(NSDControl.getFrame(), true);
			}
			// END KGU#3 2015-10-25
			//Point p = getLocationOnScreen();
			// position inputbox in the middle of this component

			//inputbox.setLocation(Math.round(p.x+(this.getVisibleRect().width-inputbox.getWidth())/2+this.getVisibleRect().x),
			//					 Math.round(p.y+(this.getVisibleRect().height-inputbox.getHeight())/2+this.getVisibleRect().y));

			inputbox.setLocationRelativeTo(NSDControl.getFrame());

			// set title (as default)
			inputbox.setTitle(_data.title);

			// set field
			inputbox.txtText.setText(_data.text.getText());
			inputbox.txtComment.setText(_data.comment.getText());
			// START KGU#43 2015-10-12: Breakpoint support
			boolean notRoot = getSelected() != root;
			inputbox.chkBreakpoint.setVisible(notRoot);
			inputbox.chkBreakpoint.setSelected(_data.breakpoint);
			// END KGU#43 2015-10-12
			// START KGU#213 2016-08-01: Enh. #215
			// START KGU#246 2016-09-13: Bugfix #241)
			//inputbox.lblBreakTrigger.setText(inputbox.lblBreakText.getText().replace("%", Integer.toString(_data.breakTriggerCount)));
			// START KGU#213 2016-10-13: Enh. #215 - Make it invisible if zero
			//inputbox.lblBreakTriggerText.setVisible(notRoot);
			inputbox.lblBreakTriggerText.setVisible(notRoot && _data.breakTriggerCount > 0);
			// END KGU#213 2016-10-13
			inputbox.lblBreakTrigger.setText(Integer.toString(_data.breakTriggerCount));
			// END KGU#246 2016-09-13
			// END KGU#213 2016-08-01
			// START KGU#277 2016-10-13: Enh. #270
			inputbox.chkDisabled.setVisible(notRoot);
			inputbox.chkDisabled.setSelected(_data.disabled);
			// END KGU#277 2016-10-13

			inputbox.OK=false;
			// START KGU#42 2015-10-14: Pass the additional information for title translation control
			if (_elementType.equals("Root") && !this.isProgram())
			{
				_elementType = "Function";
			}
			else if (_elementType.equals("Forever"))
			{
				inputbox.lblText.setVisible(false);
				inputbox.txtText.setVisible(false);
			}
			inputbox.elementType = _elementType;
			inputbox.forInsertion = _isInsertion;
			// END KGU#42 2015-10-14
			// START KGU#91 2015-12-04: Issue #39 - Attempt to set focus - always fails
			//if (Element.E_TOGGLETC || _elementType.equals("Forever"))
			//{
			//	boolean ok = inputbox.txtComment.requestFocusInWindow();
			//	//if (ok) System.out.println("Comment will get focus");
			//}
			//else
			//{
			//	boolean ok = inputbox.txtText.requestFocusInWindow();
			//	//if (ok) System.out.println("Text will get focus");
			//}
			// END KGU KGU#91 2015-12-04
			// START KGU#61 2016-03-21: Give InputBox an opportunity to check and ensure consistency
			inputbox.checkConsistency();
			// END KGU#61 2016-03-21
			inputbox.setVisible(true);

			// get fields
			_data.text.setText(inputbox.txtText.getText());
			_data.comment.setText(inputbox.txtComment.getText());
			// START KGU#43 2015-10-12: Breakpoint support
			_data.breakpoint = inputbox.chkBreakpoint.isSelected();
			// END KGU#43 2015-10-12
			// START KGU#277 2016-10-13: Enh. #270
			_data.disabled = inputbox.chkDisabled.isSelected();
			// END KGU#277 2016-10-13
			// START KGU#213 2016-08-01: Enh. #215 (temporarily disabled again)
//			try{
//				_data.breakTriggerCount = Integer.parseUnsignedInt(inputbox.txtBreakTrigger.getText());
//			}
//			catch (Exception ex)
//			{
//				_data.breakTriggerCount = 0;
//			}
			// END KGU#213 2016-08-01
			// START KGU#3 2015-10-25: Dedicated support for For loops
			if (inputbox instanceof InputBoxFor)
			{
				_data.forParts = new StringList();
				_data.forParts.add(((InputBoxFor)inputbox).txtVariable.getText());
				_data.forParts.add(((InputBoxFor)inputbox).txtStartVal.getText());
				_data.forParts.add(((InputBoxFor)inputbox).txtEndVal.getText());
				_data.forParts.add(((InputBoxFor)inputbox).txtIncr.getText());
				// START KGU#61 2016-03-21: Enh. #84 - consider FOR-IN loops
				//_data.forPartsConsistent = !((InputBoxFor)inputbox).chkTextInput.isSelected();
				// FIXME!
				//if (!((InputBoxFor)inputbox).chkTextInput.isSelected())
				//{
				//	_data.forLoopStyle = For.ForLoopStyle.COUNTER;
				//}
				//else if (((InputBoxFor)inputbox).forInValueList != null)
				//{
				//	_data.forLoopStyle = For.ForLoopStyle.TRAVERSAL;
				//	_data.forParts.add(((InputBoxFor)inputbox).forInValueList);				}
				//else
				//{
				//	_data.forLoopStyle = For.ForLoopStyle.FREETEXT;
				//}
				_data.forLoopStyle = ((InputBoxFor)inputbox).identifyForLoopStyle();
				if (_data.forLoopStyle == For.ForLoopStyle.TRAVERSAL)
				{
					// (InputBoxFor)inputbox).txtVariableIn.getText() should equal (InputBoxFor)inputbox).txtVariable.getText(),
					// such that nothing must be done about it here
					_data.forParts.add(((InputBoxFor)inputbox).forInValueList);
				}
				if (((InputBoxFor)inputbox).chkTextInput.isSelected() && !((InputBoxFor)inputbox).isLoopDataConsistent())
				{
					_data.forLoopStyle = For.ForLoopStyle.FREETEXT;
				}
				// END KGU#61 2016-03-21
				
			}
			// END KGU#3 2015-10-25
			_data.result = inputbox.OK;

			inputbox.dispose();
		}
	}
	
	/*****************************************
	 * CLIPBOARD INTERACTIONS
     *****************************************/
	public void copyToClipboardPNG()
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll();
		// END KGU 2015-10-11

		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//DataFlavor pngFlavor = new DataFlavor("image/png","Portable Network Graphics");

		// get diagram
		BufferedImage image = new BufferedImage(root.width+1,root.height+1, BufferedImage.TYPE_INT_ARGB);
		root.draw(image.getGraphics());

		// put image to clipboard
		ImageSelection imageSelection = new ImageSelection(image);
		systemClipboard.setContents(imageSelection, null);

		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null)
		{
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}

	public void copyToClipboardEMF()
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll();
		// END KGU 2015-10-11

		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		try
		{
			ByteArrayOutputStream myEMF = new ByteArrayOutputStream();
			EMFGraphics2D emf = new EMFGraphics2D(myEMF,new Dimension(root.width+6, root.height+1)) ;
			emf.setFont(Element.getFont());

			emf.startExport();
			lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(emf);
			lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
			root.draw(c,myrect);
			emf.endExport();

			systemClipboard.setContents(new EMFSelection(myEMF),null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null)
		{
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
		//System.out.println("MouseWheelMoved at (" + e.getX() + ", " + e.getY() + ")");
    	//System.out.println("MouseWheelEvent: " + e.getModifiersEx() + " Rotation = " + e.getWheelRotation() + " Type = " + 
    	//		((e.getScrollType() == e.WHEEL_UNIT_SCROLL) ? ("UNIT " + e.getScrollAmount()) : "BLOCK")  );
        if (selected != null)
        {
        	// START KGU#123 2016-01-04: Bugfix #65 - heavy differences between Windows and Linux here:
        	// In Windows, the rotation result may be arbitrarily large whereas the scrollAmount is usually 1.
        	// In Linux, however, the rotation result will usually be -1 or +1, whereas the scroll amount is 3.
        	// So we just multiply both and will get a sensible threshold, we hope.
            //if(e.getWheelRotation()<-1) selected.setCollapsed(true);
            //else if(e.getWheelRotation()>1)  selected.setCollapsed(false);
        	int rotation = e.getWheelRotation();
        	if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
        		rotation *= e.getScrollAmount();
        	}
        	else {
        		rotation *= 2;
        	}
            if (rotation < -1) {
            	selected.setCollapsed(true);
            }
            else if (rotation > 1) {
            	selected.setCollapsed(false);
            }
            // END KGU#123 2016-01-04
            
            redraw();
        }
        // FIXME KGU 2016-01-0: Issue #65
//        // Rough approach to test horizontal scrollability - only works near the left and right
//        // borders, because the last mouseMoved position is used. Seems that we will have to
//        // maintain a virtual scroll position here which is to be used instead of e.getX().
//        if ((e.getModifiersEx() & MouseWheelEvent.SHIFT_DOWN_MASK) != 0)
//        {
//        	int rotation = e.getWheelRotation();
//        	if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
//        		rotation *= e.getScrollAmount();
//        	}
//        	System.out.println("Horizontal scrolling by " + rotation);
//			Rectangle r = new Rectangle(e.getX() + 50 * rotation, e.getY(), 1, 1);
//	        ((JPanel)e.getSource()).scrollRectToVisible(r);
//        	
//        }
    }

    void toggleTextComments() {
    	Element.E_TOGGLETC=!Element.E_TOGGLETC;
    	// START KGU#136 2016-03-01: Bugfix #97
    	this.resetDrawingInfo(true);
    	// END KGU#136 2016-03-01
    	// START KGU#220 2016-07-27: Enh. #207
    	analyse();
    	// END KGU#220 2016-07-27
    	repaint();
    }
    
	// Inner class is used to hold an image while on the clipboard.
	public static class EMFSelection implements Transferable, ClipboardOwner
		{
			public static final DataFlavor emfFlavor = new DataFlavor("image/emf", "Enhanced Meta File");
			// the Image object which will be housed by the ImageSelection
			private ByteArrayOutputStream os;

			static
			{
				try
				{
					SystemFlavorMap sfm = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
					sfm.addUnencodedNativeForFlavor(emfFlavor, "ENHMETAFILE");
				}
				catch(Exception e)
				{
					System.err.println("EMFSelection: " + e.getMessage());
				}
			}

			private static DataFlavor[] supportedFlavors = {emfFlavor};

			public EMFSelection(ByteArrayOutputStream os)
			{
				this.os = os;
			}

			// Returns the supported flavors of our implementation
			public DataFlavor[] getTransferDataFlavors()
			{
				return supportedFlavors;
			}

			// Returns true if flavor is supported
			public boolean isDataFlavorSupported(DataFlavor flavor)
			{
				for(int i=0;i<supportedFlavors.length;i++)
				{
					DataFlavor f = supportedFlavors[i];
					if (f.equals(flavor))
					{
						return true;
					}
				}
				return false;
			}

			// Returns Image object housed by Transferable object
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException
			{
				if (flavor.equals(emfFlavor))
				{
					return (new ByteArrayInputStream(os.toByteArray()));
				}
				else
				{
					System.out.println("Hei !!!");
					throw new UnsupportedFlavorException(flavor);
				}
			}

			public void lostOwnership(Clipboard arg0, Transferable arg1) {}

		}

	// Inner class is used to hold an image while on the clipboard.
	public static class ImageSelection implements Transferable
		{
			// the Image object which will be housed by the ImageSelection
			private Image image;

			public ImageSelection(Image image)
			{
				this.image = image;
			}

			// Returns the supported flavors of our implementation
			public DataFlavor[] getTransferDataFlavors()
			{
				return new DataFlavor[] {DataFlavor.imageFlavor};
			}

			// Returns true if flavor is supported
			public boolean isDataFlavorSupported(DataFlavor flavor)
			{
				return DataFlavor.imageFlavor.equals(flavor);
			}

			// Returns Image object housed by Transferable object
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException
			{
				if (!DataFlavor.imageFlavor.equals(flavor))
				{
					throw new UnsupportedFlavorException(flavor);
				}
				// else return the payload
				return image;
			}
		}

	/*****************************************
	 * ANALYSER
     *****************************************/
	protected void analyse()
	{
		if(Element.E_ANALYSER==true && errorlist!=null)
		{
			//System.out.println("Analysing ...");

			// Olà - The use of a thread to show the errorlist
			// seams not to work, because the list is not always
			// shown. Concurrency problem?

			/*
			 Analyser analyser = new Analyser(root,errorlist);
			 analyser.start();
			 /**/

			//System.out.println("Working ...");
			Vector<DetectedError> vec = root.analyse();
			DefaultListModel<DetectedError> errors = 
					(DefaultListModel<DetectedError>) errorlist.getModel();
			errors.clear();

			for(int i=0;i<vec.size();i++)
			{
				errors.addElement(vec.get(i));
			}

			errorlist.repaint();
			errorlist.validate();
		}
	}

	 /*****************************************
	  * Recently used files
	  *****************************************/
	public void addRecentFile(String _filename)
	{
		addRecentFile(_filename,true);
	}

	public void addRecentFile(String _filename, boolean saveINI)
	{
		if(recentFiles.contains(_filename))
		{
			recentFiles.remove(_filename);
		}
		recentFiles.insertElementAt(_filename,0);
		while(recentFiles.size()>10)	// FIXME (KGU 2014-11-25) hard-coded "magic number"
		{
			recentFiles.removeElementAt(recentFiles.size()-1);
		}
		NSDControl.doButtons();
		if(saveINI==true) {NSDControl.savePreferences();}
	}

    /*****************************************
     * Run
	 *****************************************/
    public void goRun()
    {
    	// Activate he executor (getInstance() is supposed to do that)
    	/*Executor executor =*/ Executor.getInstance(this,null);
    	/*
                String str = JOptionPane.showInputDialog(null, "Please enter the animation delay!", "50");
                if(str!=null)
                {
                    executor.setDelay(Integer.valueOf(str));
                    executor.execute(this.root);
                }
    	 */
    }

    public void goTurtle()
    {
    	if(turtle==null)
    	{
    		turtle= new TurtleBox(500,500);
    	}
    	turtle.setVisible(true);
    	// Activate the executor (getInstance() is supposed to do that)
    	/*Executor executor =*/ Executor.getInstance(this,turtle);
    	/*
                 String str = JOptionPane.showInputDialog(null, "Please enter the animation delay!", "50");
                if(str!=null)
                {
                    executor.setDelay(Integer.valueOf(str));
                    executor.execute(this.root);
                }
    	 */

    }
    
    /**
     * Checks for running status of the Root currently held and suggests the user to stop the
     * execution if it is running
     * @return true if the fostered Root isn't executed (action may proceed), false otherwise
     */
    private boolean checkRunning()
    {
    	if (this.root == null || !this.root.isExecuted()) return true;	// No problem
    	// Give the user the chance to kill the execution but don't do anything for now,
    	// whatever the user may have been decided.
    	Executor.getInstance(null, null);
    	return false;
    }
    
    
    // START KGU#177 2016-04-07: Enh. #158
    /**
     * Tries to shift the selection to the next element in the _direction specified.
     * It turned out that on going down and right it's most intuitive to dive into
     * the substructure of compound elements (rather than jumping to its successor).
     * (For Repeat elements this holds on going up).
     * @param _direction - the cursor key orientation (up, down, left, right)
     */
    public void moveSelection(Editor.CursorMoveDirection _direction)
    {
    	if (selected != null)
    	{
    		Rect selRect = selected.getRectOffDrawPoint();
    		// Get center coordinates
    		int x = (selRect.left + selRect.right) / 2;
    		int y = (selRect.top + selRect.bottom) / 2;
    		switch (_direction)
    		{
    		case CMD_UP:
    			if (selected instanceof Repeat)
    			{
    				// START KGU#292 2016-11-16: Bugfix #291
    				//y = ((Repeat)selected).getRectOffDrawPoint().bottom - 2;
    				y = ((Repeat)selected).getBody().getRectOffDrawPoint().bottom - 2;
    				// END KGU#292 2016-11-16
    			}
    			else if (selected instanceof Root)
    			{
    				y = ((Root)selected).children.getRectOffDrawPoint().bottom - 2;
    			}
    			else
    			{
    				y = selRect.top - 2;
    			}
    			break;
    		case CMD_DOWN:
    			if (selected instanceof ILoop && !(selected instanceof Repeat))
    			{
    				Subqueue body = ((ILoop)selected).getBody();
    				y = body.getRectOffDrawPoint().top + 2;
    			}
    			else if (selected instanceof Alternative)
    			{
    				y = ((Alternative)selected).qTrue.getRectOffDrawPoint().top + 2;
    			}
    			else if (selected instanceof Case)
    			{
    				y = ((Case)selected).qs.get(0).getRectOffDrawPoint().top + 2;
    			}
    			else if (selected instanceof Parallel)
    			{
    				y = ((Parallel)selected).qs.get(0).getRectOffDrawPoint().top + 2;
    			}
    			else if (selected instanceof Root)
    			{
    				y = ((Root)selected).children.getRectOffDrawPoint().top + 2;
    			}
    			else
    			{
    				y = selRect.bottom + 2;
    			}
    			break;
    		case CMD_LEFT:
    			if (selected instanceof Root)
    			{
    				Rect bodyRect =((Root)selected).children.getRectOffDrawPoint(); 
    				// The central element of the subqueue isn't the worst choice because from
    				// here the distances are minimal. The top element, on the other hand,
    				// is directly reachable by cursor down.
    				x = bodyRect.right - 2;
    				y = (bodyRect.top + bodyRect.bottom) / 2;
    			}
    			else
    			{
    				x = selRect.left - 2;
    			}
    			break;
    		case CMD_RIGHT:
    			if (selected instanceof ILoop)
    			{
    				Rect bodyRect = ((ILoop)selected).getBody().getRectOffDrawPoint();
    				x = bodyRect.left + 2;
    				// The central element of the subqueue isn't the worst choice because from
    				// here the distances are minimal. The top element, on the other hand,
    				// is directly reachable by cursor down.
    				y = (bodyRect.top + bodyRect.bottom) / 2;
    			}
    			else if (selected instanceof Root)
    			{
    				Rect bodyRect =((Root)selected).children.getRectOffDrawPoint(); 
    				// The central element of the subqueue isn't the worst choice because from
    				// here the distances are minimal. The top element, on the other hand,
    				// is directly reachable by cursor down.
    				x = bodyRect.left + 2;
    				y = (bodyRect.top + bodyRect.bottom) / 2;
    			}
    			else
    			{
    				x = selRect.right + 2;
    			}
    			break;
    		}
    		Element newSel = root.getElementByCoord(x, y, true);
    		if (newSel != null)
    		{
    			// START KGU#177 2016-04-24: Bugfix - couldn't leave Parallel and Forever elements
    			// Compound elements with a lower bar would catch the selection again when their last
    			// encorporated element is left downwards. So identify such a situation and leap after
    			// the enclosing compound...
    			if (_direction == Editor.CursorMoveDirection.CMD_DOWN &&
    					(newSel instanceof Parallel || newSel instanceof Forever || !Element.E_DIN && newSel instanceof For) &&
    					newSel.getRectOffDrawPoint().top < selRect.top)
    			{
    				newSel = root.getElementByCoord(x, newSel.getRectOffDrawPoint().bottom + 2, true);
    			}
    			// END KGU#177 2016-04-24
    			// START KGU#214 2016-07-25: Improvement of enh. #158
    			else if (_direction == Editor.CursorMoveDirection.CMD_UP &&
    					(newSel instanceof Forever || !Element.E_DIN && newSel instanceof For) &&
    					newSel.getRectOffDrawPoint().bottom < selRect.bottom)
    			{
    				Subqueue body = ((ILoop)newSel).getBody();
    				Element sel = root.getElementByCoord(x, body.getRectOffDrawPoint().bottom - 2, true);
    				if (sel != null)
    				{
    					newSel = sel;
    				}
    			}
    			// END KGU#214 2016-07-25
    			// START KGU#214 2016-07-31: Issue #158
    			else if (newSel instanceof Root && (_direction == Editor.CursorMoveDirection.CMD_LEFT
    					|| _direction == Editor.CursorMoveDirection.CMD_RIGHT))
    			{
    				newSel = selected;	// Stop before the border on boxed diagrams
    			}
    			// END KGU#214 2015-07-31
    			selected = newSel;
    		}
    		// START KGU#214 2016-07-25: Bugfix for enh. #158 - un-boxed Roots didn't catch the selection
    		// This was better than to rush around on horizontal wheel activity! Hence fix withdrawn
//    		else if (_direction != Editor.CursorMoveDirection.CMD_UP && !root.isNice)
//    		{
//    			selected = root;
//    		}
    		// END KGU#214 2016-07-25
    		selected.setSelected(true);
			
    		// START KGU#177 2016-04-14: Enh. #158 - scroll to the selected element
			//redraw();
			redraw(selected);
			// END KGU#177 2016-04-14
			
			// START KGU#177 2016-04-24: Bugfix - buttons haven't been updated 
			this.doButtons();
			// END KGU#177 2016-04-24
    	}
    }
    // END KGU#177 2016-04-07

    // START KGU#206 2016-07-21: Enh. #158 + #197
    /**
     * Tries to expand the selection towards the next element in the _direction
     * specified.
     * This is of course limited to the bounds of the containing Subqueue.
     * @param _direction - the cursor key orientation (up, down)
     */
    public void expandSelection(Editor.SelectionExpandDirection _direction)
    {
    	if (selected != null
    			&& !(selected instanceof Subqueue)
    			&& !(selected instanceof Root))
    	{
    		boolean newSelection = false;
    		Subqueue sq = (Subqueue)selected.parent;
    		Element first = selected;
    		Element last = selected;
    		if (selected instanceof SelectedSequence)
    		{
    			first = ((SelectedSequence)selected).getElement(0);
    			last = ((SelectedSequence)selected).getElement(((SelectedSequence)selected).getSize()-1);
    		}
    		int index0 = sq.getIndexOf(first);
    		int index1 = sq.getIndexOf(last);
    		if (_direction == Editor.SelectionExpandDirection.EXPAND_UP && index0 > 0)
    		{
    			selected = new SelectedSequence(sq, index0-1, index1);
    			newSelection = true;
    		}
    		else if (_direction == Editor.SelectionExpandDirection.EXPAND_DOWN && index1 < sq.getSize()-1)
    		{
    			selected = new SelectedSequence(sq, index0, index1+1);
    			newSelection = true;
    		}
    		if (newSelection)
    		{
        		selected.setSelected(true);
    			redraw(selected);
    			this.doButtons();
    		}
    	}
    }
    // END KGU#206 2016-07-21

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// TODO Auto-generated method stub
	}

	// START KGU#305 2016-12-15: Issues #305, #312
	@Override
	public void valueChanged(ListSelectionEvent ev) {
		if (ev.getSource() == errorlist) {
			// an error list entry has been selected
			if(errorlist.getSelectedIndex()!=-1)
			{
				// get the selected error
				DetectedError err = root.errors.get(errorlist.getSelectedIndex()); 
				Element ele = err.getElement();
				if (ele != null)
				{
					// deselect the previously selected element (if any)
					if (selected!=null) {selected.setSelected(false);}
					// select the new one
					selected = ele;
					ele.setSelected(true);
					
					// redraw the diagram
					// START KGU#276 2016-11-18: Issue #269 - ensure the associated element be visible
					//redraw();
					redraw(ele);
					// END KGU#276 2016-11-18
					
					// do the button thing
					if(NSDControl!=null) NSDControl.doButtons();
					
					errorlist.requestFocusInWindow();
				}
				// START KGU#220 2016-07-27: Draft for Enh. #207, but withdrawn
				//else if (err.getError().equals(Menu.warning_1.getText()))
				//{
				//	this.toggleTextComments();
				//}
				// END KGU#200 2016-07-27
			}
		}
	}
	// END KGU#305

}

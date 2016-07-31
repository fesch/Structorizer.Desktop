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
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.datatransfer.*;

import net.iharder.dnd.*; //http://iharder.sourceforge.net/current/java/filedrop/

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.imageio.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
import lu.fisch.turtle.TurtleBox;

import org.freehep.graphicsio.svg.SVGGraphics2D;

@SuppressWarnings("serial")
public class Diagram extends JPanel implements MouseMotionListener, MouseListener, Printable, MouseWheelListener, ClipboardOwner
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
    
    // START KGU#2 2015-11-24
    public boolean isArrangerOpen = false;
    // END KGU#2 2015-11-24

    private JList<DetectedError> errorlist = null;

    private Element eCopy = null;

    public File currentDirectory = new File(System.getProperty("user.home"));
    public File lastExportDir = null;
    // START KGU#170 2016-04-01: Enh. #144 maintain a favourite export generator
    private String prefGeneratorName = "";
    // END KGU#170 2016-04-01

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
	public boolean setRoot(Root root) {
		// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
		if (!this.checkRunning()) return false;	// Don't proceed if the root is being executed
		// END KGU#157 2016-03-16

		return setRoot(root, true);
	}
	
	public boolean setRoot(Root root, boolean askToSave) {
		if (root != null)
		{
			if(askToSave){
				// Save if something has been changed
				if (!saveNSD(true))
				{
					// Abort this if the user cancels the save request
					return false;
				}
				this.unselectAll();
			}

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

	// START KGU#2 2015-11-24: Allows the Executor to localize the Control frame
	public String getLang()
	{
		return NSDControl.getLang();
	}
	// END KGU#2 2015-11-24
	
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

						if(filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".nsd"))
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
						else if (
								(filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".mod"))
								||
								(filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".pas"))
								||
								(filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".dpr"))
								||
								(filename.substring(filename.length()-4, filename.length()).toLowerCase().equals(".lpr"))
								)
						{
							// save (only if something has been changed)
							saveNSD(true);
							// load and parse source-code
							D7Parser d7 = new D7Parser("D7Grammar.cgt");
							Root rootNew = d7.parse(filename);
							if (d7.error.equals(""))
							{
								setRoot(rootNew);
								currentDirectory = new File(filename);
								//System.out.println(root.getFullText().getText());
							}
							else
							{
								// show error
								JOptionPane.showOptionDialog(null,d7.error,
										"Parser Error",
										JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
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
				//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE MOVED >>>>> " + ((selEle != null) ? selEle : "null") + " <<<<<<<<<<<<<<<<<<<<<");

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
						bSome + " >> " + ((selectedDown != null) ? selectedDown : "null"));
						/**/

				bSome.setSelected(true);
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

			// START KGU#87 2015-11-22: Maintain a selected sequence on right mouse button click 
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
			else
			// END KGU#87 2015-11-23	
			if (ele != null)
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

				// START KGU#87 2015-11-23
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
				else
				// END KGU#87 2015-11-23
				if (ele != selected)
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
			// START KGU#180 2016-04-15: Bugfix #165 - delection didn't work properly
			else /* ele == null */
			{
				selected = null;
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
					//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE RELEASED 1 >>>>>>> " + ((selectedUp != null)?selectedUp:"null")+ " <<<<<<<<<<<<<<<<<<<<<<");
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
					//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE RELEASED 2 >>>>>>> " + ((selectedUp != null)?selectedUp:"null")+ " <<<<<<<<<<<<<<<<<<<<<<");
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
			else
			{
				// an error list entry has been selected
				if(errorlist.getSelectedIndex()!=-1)
				{
					// get the selected error
					DetectedError err = root.errors.get(errorlist.getSelectedIndex()); 
					Element ele = err.getElement();
					if(ele!=null)
					{
						// deselect the previously selected element (if any)
						if (selected!=null) {selected.setSelected(false);}
						// select the new one
						selected = ele;
						ele.setSelected(true);
						// redraw the diagram
						redraw();
						// do the button thing
						if(NSDControl!=null) NSDControl.doButtons();
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
				if (selected != null && !((selected instanceof Subqueue) && ((Subqueue)selected).getSize() > 0))
				// END KGU#87 2015-11-22
				{
					// edit it
					editNSD();
					selected.setSelected(true);
					redraw();
					// do the button thing
					if(NSDControl!=null) NSDControl.doButtons();
				}
			}
			else
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
		}
	}

    // START KGU#143 2016-01-21: Bugfix #114 - We need a possibility to update buttons from execution status
    public void doButtons()
    {
    	if(NSDControl!=null) NSDControl.doButtons();
    }
    // END KGU#143 2016-01-21

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
		if (this.isArrangerOpen)
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
		dlgOpen.setDialogTitle("Open file ...");
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
			openNSD(dlgOpen.getSelectedFile().getAbsoluteFile().toString());
		}
	}

	public void openNSD(String _filename)
	{
		// START KGU#48 2015-10-17: Arranger support
		Root oldRoot = this.root;
		// END KGU#48 2015-10-17
		// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
		String errorMessage = "File not found!";
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
			errorMessage = e.getMessage();
			System.out.println(errorMessage);
			// END KGU#111 2015-12-16
		}
		// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
		if (errorMessage != null)
		{
			JOptionPane.showMessageDialog(this, "\"" + _filename + "\": " + errorMessage, "Loading Error",
					JOptionPane.ERROR_MESSAGE);
		}
		// END KGU#111 2015-12-16
	}


	/*****************************************
	 * SaveAs method
	 *****************************************/
	public void saveAsNSD()
	{
		JFileChooser dlgSave = new JFileChooser();
		dlgSave.setDialogTitle("Save file as ...");
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
		// START KGU 2015-10-16: D.R.Y. - there is already a suitable method
//		String nsdName = root.getText().get(0);
//		nsdName.replace(':', '_');
//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		dlgSave.addChoosableFileFilter(new StructogramFilter());
		int result = dlgSave.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			root.filename=dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if(!root.filename.substring(root.filename.length()-4, root.filename.length()).toLowerCase().equals(".nsd"))
			{
				root.filename+=".nsd";
			}

                        File f = new File(root.filename);
                        boolean writeNow = true;
                        if(f.exists())
                        {
                            writeNow=false;
                            int res = JOptionPane.showConfirmDialog(
                                    this,
                                    Menu.msgOverwriteFile.getText(),
                                    Menu.btnConfirmOverwrite.getText(),
                                    JOptionPane.YES_NO_OPTION);
                            if(res==JOptionPane.YES_OPTION) writeNow=true;
                        }

                        if(writeNow==false)
                        {
                            JOptionPane.showMessageDialog(this,"Your file has not been saved. Please repeat the save operation!");
                        }
                        else
                        {
                        	// START KGU#94 2015.12.04: out-sourced to auxiliary method
//                            try
//                            {
//                            
//                                    FileOutputStream fos = new FileOutputStream(root.filename);
//                                    Writer out = new OutputStreamWriter(fos, "UTF8");
//                                    XmlGenerator xmlgen = new XmlGenerator();
//                                    out.write(xmlgen.generateCode(root,"\t"));
//                                    out.close();
//                                    /*
//                                    BTextfile outp = new BTextfile(root.filename);
//                                    outp.rewrite();
//                                    XmlGenerator xmlgen = new XmlGenerator();
//                                    outp.write(xmlgen.generateCode(root,"\t"));
//                                    //outp.write(diagram.root.getXML());
//                                    outp.close();
//                                    /**/
//
//                                    root.hasChanged=false;
//                                    addRecentFile(root.filename);
//                            }
//                            catch(Exception e)
//                            {
//                                    JOptionPane.showOptionDialog(this,"Error while saving the file!","Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
//                            }
                        	doSaveNSD();
                        	// END KGU#94 2015-12-04
                         }
		}
	}

	/*****************************************
	 * Save method
	 *****************************************/
	
	/**
	 * Stores unsaved changes (if any). If _checkChanges is true then the user may confirms or deny saving or cancel the
	 * inducing request. 
	 * @param _checkChanged - if true and the current root has unsaved changes then a user dialog will be popped up first
	 * @return true if the user cancelled the save request
	 */
	public boolean saveNSD(boolean _checkChanged)
	{
		int res = 0;	// Save decision: 0 = do save, 1 = don't save, -1 = cancelled (don't leave)
		// only save if something has been changed
		// START KGU#137 2016-01-11: Use the new method now
		//if(root.hasChanged==true)
		if (root.hasChanged())
		// END KGU#137 2016-01-11
		{

			if (_checkChanged==true)
			{
				// START KGU#49 2015-10-18: If induced by Arranger then it's less ambiguous to see the NSD name
				//res = JOptionPane.showOptionDialog(this,
				//		   "Do you want to save the current NSD-File?",
				String filename = root.filename;
				if (filename == null || filename.isEmpty())
				{
					filename = root.proposeFileName();
				}
				res = JOptionPane.showOptionDialog(this,
												   "Do you want to save the current NSD-File?\n\"" + filename + "\"",
				// END KGU#49 2015-10-18
												   "Question",
												   JOptionPane.YES_NO_OPTION,
												   JOptionPane.QUESTION_MESSAGE,
												   null,null,null);
			}
			
			if (res==0)
			{
				// if root has not yet been saved
				boolean saveIt = true;

				//System.out.println(this.currentDirectory.getAbsolutePath());
				
				if(root.filename.equals(""))
				{
					JFileChooser dlgSave = new JFileChooser();
					dlgSave.setDialogTitle("Save file ...");
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

					dlgSave.setSelectedFile(new File(root.proposeFileName()));

					dlgSave.addChoosableFileFilter(new StructogramFilter());
					int result = dlgSave.showSaveDialog(this);

					if (result == JFileChooser.APPROVE_OPTION)
					{
						root.filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
						if(!root.filename.substring(root.filename.length()-4).toLowerCase().equals(".nsd"))
						{
							root.filename+=".nsd";
						}
					}
					else
					{
						saveIt = false;
					}
				}

				if (saveIt == true)
				{
					// START KGU#94 2015-12-04: Out-sourced to auxiliary method
//					try
//					{
//                                                FileOutputStream fos = new FileOutputStream(root.filename);
//                                                Writer out = new OutputStreamWriter(fos, "UTF8");
//                                                XmlGenerator xmlgen = new XmlGenerator();
//                                                out.write(xmlgen.generateCode(root,"\t"));
//                                                out.close();
//                                                /*
//                                                BTextfile outp = new BTextfile(root.filename);
//						outp.rewrite();
//						XmlGenerator xmlgen = new XmlGenerator();
//						outp.write(xmlgen.generateCode(root,"\t"));
//						//outp.write(diagram.root.getXML());
//						outp.close();/**/
//
//						root.hasChanged=false;
//						addRecentFile(root.filename);
//					}
//					catch(Exception e)
//					{
//						JOptionPane.showOptionDialog(this,"Error while saving the file!\n"+e.getMessage(),"Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
//					}
					doSaveNSD();
					// END KGU#94 2015-12-04
				}
			}
		}
		return res != -1; // true if not cancelled
	}
	
	// START KGU#94 2015-12-04: Common file writing routine (on occasion of bugfix #40)
	private boolean doSaveNSD()
	{
		String[] EnvVariablesToCheck = { "TEMP", "TMP", "TMPDIR", "HOME", "HOMEPATH" };
		boolean done = false;
        try
        {
        	// START KGU#94 2015.12.04: Bugfix #40 part 1
        	// A failed saving attempt should not leave a truncated file!
        	//FileOutputStream fos = new FileOutputStream(root.filename);
        	String filename = root.filename;
        	File f = new File(filename);
        	boolean fileExisted = f.exists(); 
        	if (fileExisted)
        	{
        		String tempDir = "";
        		for (int i = 0; (tempDir == null || tempDir.isEmpty()) && i < EnvVariablesToCheck.length; i++)
        		{
        			tempDir = System.getenv(EnvVariablesToCheck[i]);
        		}
        		if ((tempDir == null || tempDir.isEmpty()) && this.currentDirectory != null)
        		{
        			File dir = this.currentDirectory;
        			if (dir.isFile())
        			{
        				tempDir = dir.getParent();
        			}
        			else
        			{
        				tempDir = dir.getAbsolutePath();
        			}
        		}
        		filename = tempDir + System.getProperty("file.separator") + "Structorizer.tmp";
        	}
        	FileOutputStream fos = new FileOutputStream(filename);
        	// END KGU#94 2015-12-04
        	Writer out = new OutputStreamWriter(fos, "UTF8");
        	XmlGenerator xmlgen = new XmlGenerator();
        	out.write(xmlgen.generateCode(root,"\t"));
        	out.close();

        	// START KGU#94 2015-12-04: Bugfix #40 part 2
        	// If the NSD file had existed then replace it by the output file after having created a backup
        	if (fileExisted)
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
        	}
        	// END KGU#94 2015.12.04
        	
			// START KGU#137 2016-01-11: On successful saving, record the undo stack level
        	//root.hasChanged=false;
        	root.rememberSaved();
        	// END KGU#137 2016-01-11
        	addRecentFile(root.filename);
        	done = true;
        }
        catch(Exception ex)
        {
        	JOptionPane.showMessageDialog(this, "Error on saving the file:" + ex.getMessage() + "!", "Error",
        			JOptionPane.ERROR_MESSAGE, null);
        }
        return done;
	}
	// END KGU#94 2015-12-04

	/*****************************************
	 * Undo method
	 *****************************************/
	public void undoNSD()
	{
		root.undo();
		// START KGU#138 2016-01-11: Bugfix #102 - All elements will be replaced by copies...
		selected = this.selectedDown = this.selectedMoved = this.selectedUp = null;
		// END KGU#138 2016-01-11
		// START KGU#183 2016-04-24: Issue #169 - Restore previous selection if possible
		selected = root.findSelected();
		// END KGU#183 2016-04-24
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
		// START KGU#183 2016-04-24: Issue #169 - Restore previous selection if possible
		selected = root.findSelected();
		// END KGU#183 2016-04-24
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
		return canCopyNoRoot() && !selected.executed && !selected.waited;
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

	// START KGU#199 2016-07-06: Enh. #188: Element conversions
	public boolean canTransmute()
	{
		boolean convertible = false;
		if (selected != null && !selected.executed && !selected.waited)
		{
			if (selected instanceof Instruction)
			{
				Instruction instr = (Instruction)selected;
				convertible = instr.getText().count() > 1
						|| instr.isJump()
						|| instr.isFunctionCall()
						|| instr.isProcedureCall();
			}
			else if (selected instanceof IElementSequence && ((IElementSequence)selected).getSize() > 1)
			{
				convertible = true;
				for (int i = 0; convertible && i < ((IElementSequence)selected).getSize(); i++)
				{
					if (!(((IElementSequence)selected).getElement(i) instanceof Instruction))
					{
						convertible = false;
					}
				}
			}
		}
		return convertible;
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

				if(data.result==true)
				{
					Element ele = new Instruction(data.text.getText());
					ele.setComment(data.comment.getText());
					// START KGU#43 2015-10-17
					if (data.breakpoint) {
						ele.toggleBreakpoint();
					}
					// END KGU#43 2015-10-17
					root.addUndo();
					// START KGU#137 2016-01-11: Already prepared by addUndo()
					//root.hasChanged=true;
					// END KGU#137 2016-01-11
					((Subqueue) element).addElement(ele);
					// START KGU#136 2016-03-01: Bugfix #97
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
					if (element.isBreakpoint() != data.breakpoint) {
						element.toggleBreakpoint();
					}
					// END KGU#43 2015-10-12
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
		// START KGU#61 2016-03-21: Enh. #84 - consider FOR-IN loops
		//boolean wasConsistent = ((For)element).isConsistent;
		For.ForLoopStyle style = _for.style;
		// END KGU#61 2016-03-12
		// START KGU#3 2015-11-08: We must support backward compatibility
		// For the first display shows the real contents
		// START KGU#61 2016-03-21: Enh. #84 - Now there are three cases
		//((For)element).isConsistent = true;^
		_for.style = For.ForLoopStyle.COUNTER;
		// END KGU#61 2016-03-21
		// END KGU#3 2015-11-08
		_data.forParts.add(_for.getCounterVar());
		_data.forParts.add(_for.getStartValue());
		_data.forParts.add(_for.getEndValue());
		_data.forParts.add(Integer.toString(_for.getStepConst()));
		// START KGU#61 2016-03-21: Enh. #84
		//data.forPartsConsistent = ((For)element).isConsistent = wasConsistent;
		String valueList = _for.getValueList();
		if (valueList != null)
		{
			_data.forParts.add(valueList);
		}
		_data.forLoopStyle = _for.style = style;
		// END KGU#61 2016-03-12
		
	}
	
	private void postEditFor(EditData _data, For _for)
	{
		// START KGU#61 2016-03-21: Enh. #84 - consider FOR-IN loops
		//((For)element).isConsistent = data.forPartsConsistent;
		_for.style = _data.forLoopStyle;
		// END KGU#61 2016-03-21
		_for.setCounterVar(_data.forParts.get(0));
		_for.setStartValue(_data.forParts.get(1));
		_for.setEndValue(_data.forParts.get(2));
		_for.setStepConst(_data.forParts.get(3));
		// START KGU#61 2016-03-21: Enh. #84 - FOR-IN loop support
		if (_for.style == For.ForLoopStyle.TRAVERSAL)
		{
			_for.style = For.ForLoopStyle.FREETEXT;
			_for.setValueList(_for.getValueList());
			_for.style = For.ForLoopStyle.TRAVERSAL;
		}
		// END KGU#61 2016-03-21		
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
				_ele.setSelected(true);
				selected=_ele;
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
		instr.setSelected(true);
		parent.insertElementAt(instr, index);
		this.selected = instr;
		this.selectedUp = this.selectedDown = this.selected;
	}
	// END KGU#199 2016-07-06

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
		pp.setLang(NSDControl.getLang());
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
		// START KGU#202 2016-07-03: Localization of Arranger
		arr.setLangLocal(this.getLang());
		// END KGU#202 2016-07-03
		arr.setVisible(true);
		isArrangerOpen = true;	// Gives the Executor a hint where to find a subroutine pool
	}
	// END KGU#2 2015-11-19
	
	// START KGU#125 2016-01-06: Possibility to adopt a diagram if it's orphaned
	public void adoptArrangedOrphanNSD(Root root)
	{
		if (isArrangerOpen)
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
		about.setLang(NSDControl.getLang());
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
					JOptionPane.showOptionDialog(this,"Error while saving the images!","Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
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
					JOptionPane.showOptionDialog(this,"Error while saving the image!","Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
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
		dlgOpen.setDialogTitle("Import Pascal Code ...");
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
			List<Root> newRoots = d7.parse(filename, "ISO-8859-1");
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
						"Parser Error", JOptionPane.ERROR_MESSAGE, null);
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
		catch(Exception e)
		{
			JOptionPane.showOptionDialog(this,"Error while using generator "+_generatorClassName+"\n"+e.getMessage(),"Error",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
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
		try {
			Desktop.getDesktop().browse(new URI("http://help.structorizer.fisch.lu/index.php"));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			// Usually we won't get here - in case of missing network access the browser will
			// show a message
			JOptionPane.showMessageDialog(null, ex.getMessage(),
					"URL Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	// END KGU#208 2016-07-22
	
	/*****************************************
	 * update method
	 *****************************************/

	public void updateNSD()
	{
		// KGU#35 2015-07-29: Bob's code adopted with slight modification (Homepage URL put into a variable) 
		String home = "http://structorizer.fisch.lu";
		try {
			JEditorPane ep = new JEditorPane("text/html","<html><font face=\"Arial\">Goto <a href=\"" + home + "\">" + home + "</a> to look for updates<br>and news about Structorizer.</font></html>");
			ep.addHyperlinkListener(new HyperlinkListener()
			{
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e)
				{
					if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					{
						try {
							Desktop.getDesktop().browse(e.getURL().toURI());
						}
						catch(Exception ee)
						{
							ee.printStackTrace();
						}
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

		colors.setLang(NSDControl.getLang());
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

		preferences.setLang(NSDControl.getLang());
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
		parserPreferences.edtAltPre.setText(D7Parser.preAlt);
		parserPreferences.edtAltPost.setText(D7Parser.postAlt);
		parserPreferences.edtCasePre.setText(D7Parser.preCase);
		parserPreferences.edtCasePost.setText(D7Parser.postCase);
		parserPreferences.edtForPre.setText(D7Parser.preFor);
		parserPreferences.edtForPost.setText(D7Parser.postFor);
		// START KGU#3 2015-11-08: New configurable separator for FOR loop step const
		parserPreferences.edtForStep.setText(D7Parser.stepFor);
		// END KGU#3 2015-11-08
		// START KGU#61 2016-03-21: New configurable keywords for FOR-IN loop
		parserPreferences.edtForInPre.setText(D7Parser.preForIn);
		parserPreferences.edtForInPost.setText(D7Parser.postForIn);
		// END KGU#61 2016-03-21
		parserPreferences.edtWhilePre.setText(D7Parser.preWhile);
		parserPreferences.edtWhilePost.setText(D7Parser.postWhile);
		parserPreferences.edtRepeatPre.setText(D7Parser.preRepeat);
		parserPreferences.edtRepeatPost.setText(D7Parser.postRepeat);
		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
		parserPreferences.edtJumpLeave.setText(D7Parser.preLeave);
		parserPreferences.edtJumpReturn.setText(D7Parser.preReturn);
		parserPreferences.edtJumpExit.setText(D7Parser.preExit);
		// END KGU#78 2016-03-25
		parserPreferences.edtInput.setText(D7Parser.input);
		parserPreferences.edtOutput.setText(D7Parser.output);
		// START KGU#165 2016-03-25: We need a transparent decision here
		parserPreferences.chkIgnoreCase.setSelected(D7Parser.ignoreCase);
		// END KGU#165 2016-03-25

		parserPreferences.setLang(NSDControl.getLang());
		parserPreferences.pack();
		parserPreferences.setVisible(true);

		if(parserPreferences.OK)
		{

			// get fields
			D7Parser.preAlt=parserPreferences.edtAltPre.getText();
			D7Parser.postAlt=parserPreferences.edtAltPost.getText();
			D7Parser.preCase=parserPreferences.edtCasePre.getText();
			D7Parser.postCase=parserPreferences.edtCasePost.getText();
			D7Parser.preFor=parserPreferences.edtForPre.getText();
			D7Parser.postFor=parserPreferences.edtForPost.getText();
			// START KGU#3 2015-11-08: New configurable separator for FOR loop step const
			D7Parser.stepFor=parserPreferences.edtForStep.getText();
			// END KGU#3 2015-11-08
			// START KGU#61 2016-03-21: New configurable keywords for FOR-IN loop
			D7Parser.preForIn=parserPreferences.edtForInPre.getText();
			D7Parser.postForIn=parserPreferences.edtForInPost.getText();
			// END KGU#61 2016-03-21
			D7Parser.preWhile=parserPreferences.edtWhilePre.getText();
			D7Parser.postWhile=parserPreferences.edtWhilePost.getText();
			D7Parser.preRepeat=parserPreferences.edtRepeatPre.getText();
			D7Parser.postRepeat=parserPreferences.edtRepeatPost.getText();
    		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
    		D7Parser.preLeave=parserPreferences.edtJumpLeave.getText();
    		D7Parser.preReturn=parserPreferences.edtJumpReturn.getText();
    		D7Parser.preExit=parserPreferences.edtJumpExit.getText();
    		// END KGU#78 2016-03-25
			D7Parser.input=parserPreferences.edtInput.getText();
			D7Parser.output=parserPreferences.edtOutput.getText();
			// START KGU#165 2016-03-25: We need a transparent decision here
			D7Parser.ignoreCase = parserPreferences.chkIgnoreCase.isSelected();
			// END KGU#165 2016-03-25

			// save fields to ini-file
			D7Parser.saveToINI();

			// START KGU#136 2016-03-31: Bugfix #97 - cached bounds may have to be invalidated
			if (Element.E_VARHIGHLIGHT)
			{
				// Parser keyword chenges may have an impact on the text width
				this.resetDrawingInfo(true);
				// redraw diagram
				redraw();
			}
			// END KGU#136 2016-03-31
			
		}
	}

	public void analyserNSD()
	{
		AnalyserPreferences analyserPreferences = new AnalyserPreferences(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		analyserPreferences.setLocation(Math.round(p.x+(getVisibleRect().width-analyserPreferences.getWidth())/2+this.getVisibleRect().x),
						   Math.round(p.y+(getVisibleRect().height-analyserPreferences.getHeight())/2+this.getVisibleRect().y));

		// set fields
		analyserPreferences.check1.setSelected(Root.check1);
		analyserPreferences.check2.setSelected(Root.check2);
		analyserPreferences.check3.setSelected(Root.check3);
		analyserPreferences.check4.setSelected(Root.check4);
		analyserPreferences.check5.setSelected(Root.check5);
		analyserPreferences.check6.setSelected(Root.check6);
		analyserPreferences.check7.setSelected(Root.check7);
		analyserPreferences.check8.setSelected(Root.check8);
		analyserPreferences.check9.setSelected(Root.check9);
		analyserPreferences.check10.setSelected(Root.check10);
		analyserPreferences.check11.setSelected(Root.check11);
		analyserPreferences.check12.setSelected(Root.check12);
		analyserPreferences.check13.setSelected(Root.check13);
		// START KGU#3 2015-11-03: New check type for enhanced FOR loops
		analyserPreferences.check14.setSelected(Root.check14);
		// END KGU#3 2015-11-03
		// START KGU#2/KGU#78 2015-11-25: New check type for enabled subroutine calls / JUMP instructions
		analyserPreferences.check15.setSelected(Root.check15);	// KGU#2
		analyserPreferences.check16.setSelected(Root.check16);	// KGU#78
		// END KGU#2/KGU#78 2015-11-25
		// START KGU#47 2015-11-29: New check type for PARALLEL sections
		analyserPreferences.check17.setSelected(Root.check17);	// KGU#47
		// END KGU#47 2015-11-29

		analyserPreferences.setLang(NSDControl.getLang());
		analyserPreferences.pack();
		analyserPreferences.setVisible(true);

		// get fields
		Root.check1=analyserPreferences.check1.isSelected();
		Root.check2=analyserPreferences.check2.isSelected();
		Root.check3=analyserPreferences.check3.isSelected();
		Root.check4=analyserPreferences.check4.isSelected();
		Root.check5=analyserPreferences.check5.isSelected();
		Root.check6=analyserPreferences.check6.isSelected();
		Root.check7=analyserPreferences.check7.isSelected();
		Root.check8=analyserPreferences.check8.isSelected();
		Root.check9=analyserPreferences.check9.isSelected();
		Root.check10=analyserPreferences.check10.isSelected();
		Root.check11=analyserPreferences.check11.isSelected();
		Root.check12=analyserPreferences.check12.isSelected();
		Root.check13=analyserPreferences.check13.isSelected();
		// START KGU#3 2015-11-03: New check type for enhanced FOR loops
		Root.check14=analyserPreferences.check14.isSelected();
		// END KGU#3 2015-11-03
		// START KGU#2/KGU#78 2015-11-25: New check type for enabled subroutine calls / JUMP instructions
		Root.check15=analyserPreferences.check15.isSelected();	// KGU#2
		Root.check16=analyserPreferences.check16.isSelected();	// KGU#78
		// END KGU#2/KGU#78 2015-11-25
		// START KGU#47 2015-11-29: New check type for PARALLEL sections
		Root.check17=analyserPreferences.check17.isSelected();	// KGU#47
		// END KGU#47 2015-11-29

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
            // START KGU#212 2016-07-26: bugfix #204 eod sizing needs the language
            //ExportOptionDialoge eod = new ExportOptionDialoge(NSDControl.getFrame());
            ExportOptionDialoge eod = new ExportOptionDialoge(
            		NSDControl.getFrame(), NSDControl.getLang());
            // END KGU#212 2016-07-26
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

	public void fontNSD()
	{
		FontChooser fontChooser = new FontChooser(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		fontChooser.setLocation(Math.round(p.x+(getVisibleRect().width-fontChooser.getWidth())/2+this.getVisibleRect().x),
								Math.round(p.y+(getVisibleRect().height-fontChooser.getHeight())/2+this.getVisibleRect().y));

		// set fields
		fontChooser.setFont(Element.getFont());
		fontChooser.setLang(NSDControl.getLang());
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

	public boolean drawComments()
	{
		return Element.E_SHOWCOMMENTS;
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
		Element.E_ANALYSER=_analyse;
		NSDControl.doButtons();
	}
	
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
				if (!_isInsertion)
				{
					ipbFor.txtVariable.setText(_data.forParts.get(0));
					ipbFor.txtStartVal.setText(_data.forParts.get(1));
					ipbFor.txtEndVal.setText(_data.forParts.get(2));
					ipbFor.txtIncr.setText(_data.forParts.get(3));
					// START KGU#61 2016-03-21: Enh. #84 - Consider FOR-IN loops
					//ipbFor.chkTextInput.setSelected(!_data.forPartsConsistent);
					//ipbFor.enableTextFields(!_data.forPartsConsistent);
					if (_data.forParts.count() > 4)
					{
						ipbFor.forInValueList = _data.forParts.get(4);
					}
					boolean textMode = _data.forLoopStyle != For.ForLoopStyle.COUNTER;
					ipbFor.chkTextInput.setSelected(textMode);
					ipbFor.enableTextFields(textMode);
					// END KGU#61 2016-03-21
				}
				else {
					ipbFor.enableTextFields(false);
				}
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
			inputbox.chkBreakpoint.setEnabled(getSelected() != root);
			inputbox.chkBreakpoint.setSelected(_data.breakpoint);
			// END KGU#43 2015-10-12

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
			// START KGU#61 2016-03-21: Give InputBox an opportunity to check consistency
			inputbox.checkConsistency();
			// END KGU#61 2016-03-21
			inputbox.setLang(NSDControl.getLang());
			inputbox.setVisible(true);

			// get fields
			_data.text.setText(inputbox.txtText.getText());
			_data.comment.setText(inputbox.txtComment.getText());
			// START KGU#43 2015-10-12: Breakpoint support
			_data.breakpoint = inputbox.chkBreakpoint.isSelected();
			// END KGU#43 2015-10-12
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
				if (!((InputBoxFor)inputbox).chkTextInput.isSelected())
				{
					_data.forLoopStyle = For.ForLoopStyle.COUNTER;
				}
				else if (((InputBoxFor)inputbox).forInValueList != null)
				{
					_data.forLoopStyle = For.ForLoopStyle.TRAVERSAL;
					_data.forParts.add(((InputBoxFor)inputbox).forInValueList);				}
				else
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
					System.err.println(e.getMessage());
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
    				y = ((Repeat)selected).getRectOffDrawPoint().bottom - 2;
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
    			selected = newSel;
    		}
    		// START KGU#214 2016-07-25: Bugfix for enh. #158 - un-boxed Roots didn't catch the selection
    		else if (_direction != Editor.CursorMoveDirection.CMD_UP && !root.isNice)
    		{
    			selected = root;
    		}
    		// END KGU#214 2016-07-25
    		selected.setSelected(true);
			
    		// START KGU#177 2016-04-14: Enh. #158 - scroll to the selected element
			this.scrollRectToVisible(selected.getRectOffDrawPoint().getRectangle());
			// END KGU#177 2016-04-14
			
			redraw();
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
    			this.scrollRectToVisible(selected.getRectOffDrawPoint().getRectangle());
    			redraw();
    			this.doButtons();
    		}
    	}
    }
    // END KGU#206 2016-07-21

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// TODO Auto-generated method stub
		
	}

}

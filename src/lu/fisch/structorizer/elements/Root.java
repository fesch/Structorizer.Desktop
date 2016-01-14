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

package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents the "root" of a diagram or the program/sub itself.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007.12.09      First Issue
 *      Bob Fisch       2008.04.18      Added analyser
 *      Kay Gürtzig     2014.10.18      Var name search unified and false detection of "as" within var names mended
 *      Kay Gürtzig     2015.10.12      new methods toggleBreakpoint() and clearBreakpoints() (KGU#43).
 *      Kay Gürtzig     2015.10.16      getFullText methods redesigned/replaced, changes in getVarNames()
 *      Kay Gürtzig     2015.10.17      improved Arranger support by method notifyReplaced (KGU#48)
 *      Kay Gürtzig     2015.11.03      New error14 field and additions to analyse for FOR loop checks (KGU#3)
 *      Kay Gürtzig     2015.11.13/14   Method copy() accomplished, modifications for subroutine calls (KGU#2 = #9)
 *      Kay Gürtzig     2015.11.22/23   Modifications to support selection of Element sequences (KGU#87),
 *                                      Code revision in Analyser (field Subqueue.children now private).
 *      Kay Gürtzig     2015.11.28      Several additions to analyser (KGU#2 = #9, KGU#47, KGU#78 = #23) and
 *                                      saveToIni()
 *      Kay Gürtzig     2015.12.01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *      Bob Fisch       2015.12.10      Bugfix #50 -> grep parameter types (Method getParams(...))
 *      Kay Gürtzig     2015.12.11      Bugfix #54 (KGU#102) in getVarNames(): keywords within identifiers
 *      Kay Gürtzig     2015.12.20      Bugfix #50 (KGU#112) getResultType() slightly revised
 *      Kay Gürtzig     2016.01.02      Bugfixes #78 (KGU#119, equals()) and #85 (KGU#120, undo() etc.) 
 *      Kay Gürtzig     2016.01.06      Bugfix #89: References to obsolete operator padding (KGU#126) and
 *                                      faulty index condition for variable detection (KGU#98) fixed 
 *      Kay Gürtzig     2016.01.08      Bugfix #50 (KGU#135) postfix result type was split into lines  
 *      Kay Gürtzig     2016.01.11      Issue #103 (KGU#137): "changed" state now dependent on undo/redo
 *                                      stack, see comments below for details
 *      Kay Gürtzig     2016.01.14      Bugfix #103: Saving didn't reset the hasChanged flag anymore (KGU#137)
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      
 *      2016.01.11 (KGU#137)
 *      - When changes are undone back to the moment of last file saving, the hasChanged is to be reset
 *      - Therefore, we now track the undo stack size when saving. As soon as an undo action returns to
 *        the recorded stack size, the hasChanged flag will be reset. Undoing more steps sets the
 *        flag again but keeps the stored stack size for the case of redoing forward to this point again.
 *      - As soon as an undoable editing below the recorded stack level eccurs (wiping the redo stack),
 *        the recorded stack level will be set to an unreachable -1, because the saved state gets lost
 *        internally.
 *
 ******************************************************************************************************///


import java.util.Iterator;
import java.util.Vector;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.io.File;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.image.*;

import javax.swing.JLabel;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.gui.*;

import com.stevesoft.pat.*;

import java.awt.Point;
import java.util.ArrayList;

/**
 * @author kay
 *
 */
public class Root extends Element {
	
	// KGU 2015-10-16: Just for testing purposes
	//private static int fileCounter = 1;

	// some fields
	public boolean isNice = true;
	public boolean isProgram = true;
	// START KGU#137 2016-01-11: Bugfix #103 - More precise tracking of changes
	//public boolean hasChanged = false;
	private boolean hasChanged = false;		// Now only for global, not undoable changes
	private int undoLevelOfLastSave = 0;	// Undo stack level recorded on saving
	// END KGU#137 2016-01-11
	public boolean hightlightVars = false;
	// START KGU#2 (#9) 2015-11-13:
	// Is this routine currently waiting for a called subroutine?
	public boolean isCalling = false;
	// END KG#2 (#9) 2015-11-13
	
	public Subqueue children = new Subqueue();

	public int height = 0;
	public int width = 0;

	private Stack<Subqueue> undoList = new Stack<Subqueue>();
	private Stack<Subqueue> redoList = new Stack<Subqueue>();

	public String filename = "";

	// variables
	public StringList variables = new StringList();
	public Vector<DetectedError> errors = new Vector<DetectedError>();
	private StringList rootVars = new StringList();

	// TODO (KGU 2016-01-04): We ought to think about an array or Vector here.
	// error checks for analyser (see also addError(), saveToIni(), Diagram.analyserNSD() and Mainform.loadFromIni())
	public static boolean check1 = false;
	public static boolean check2 = false;
	public static boolean check3 = false;
	public static boolean check4 = false;
	public static boolean check5 = false;
	public static boolean check6 = false;
	public static boolean check7 = false;
	public static boolean check8 = false;
	public static boolean check9 = false;
	public static boolean check10 = false;
	public static boolean check11 = false;
	public static boolean check12 = false;
	public static boolean check13 = false;
	// START KGU#3 2015-11-03: New check for enhanced FOR loop
	public static boolean check14 = false;
	// END KGU#3 2015-11-03
	// START KGU#2 2015-11-25: New check for subroutine CALL syntax
	public static boolean check15 = false;
	// END KGU#2 2015-11-25
	// START KGU#78 2015-11-25: New check for incorrect JUMP element
	public static boolean check16 = false;
	// END KGU#78 2015-11-25
	// START KGU#78 2015-11-28: New check for races in PARALLEL sections
	public static boolean check17 = false;
	// END KGU#78 2015-11-28

	private Vector<Updater> updaters = new Vector<Updater>();

	// KGU#91 2015-12-04: No longer needed
	//private boolean switchTextAndComments = false;

	public Root()
	{
		super(StringList.getNew("???"));
		setText(StringList.getNew("???"));
		children.parent=this;
	}

	public Root(StringList _strings)
	{
		super(_strings);
		setText(_strings);
		children.parent=this;
	}
	
    public void addUpdater(Updater updater)
    {
    	// START KGU#48 2015-10-17: While this.updaters is only a Vector, we must avoid multiple registration...
        //updaters.add(updater);
    	if (!updaters.contains(updater))
    	{
    		updaters.add(updater);
    	}
    	// END KGU#48 2015-10-17
    }

    public void removeUpdater(Updater updater)
    {
        updaters.remove(updater);
    }
    
    // START KGU#2 (#9) 2015-11-14: We need a way to get the Updaters
    public Iterator<Updater> getUpdateIterator()
    {
    	return updaters.iterator();
    }
    // END KGU#2 (#9) 2015-11-14

    // START KGU#48 2015-10-17: Arranger support on Root replacement (e.g. by loading a new file)
    public void notifyReplaced(Root newRoot)
    {
    	//System.out.println("Trying to notify my replacement to " + updaters.size() + " Updaters..."); // FIXME (KGU) Remove after successful test!
    	Iterator<Updater> iter = updaters.iterator();
    	while (iter.hasNext())
    	{
    		//System.out.println(this.getMethodName() + " notifying an Updater about replacement.");
    		iter.next().replaced(this, newRoot);
    	}
    	updaters.clear();
    }
    // END KGU#48 2015-10-17
    
    // START KGU#137 2016-01-11: Bugfix #103 - Enhanced change tracking, synchronized with undoing/redoing/saving
    /**
     * Sets an additional sticky changed flag for saveable global settings that are not subject
     * of the undo/redo stacks
     */
    public void setChanged()
    {
    	this.hasChanged = true;
    }

    /**
     * Detects if changes (no matter if undoable or not) have been registered since last saving
     * @return true if there have been changes not undone
     */
    public boolean hasChanged()
    {
    	return this.hasChanged || this.undoLevelOfLastSave != this.undoList.size();
    }
    // END KGU#137 2016-01-11

	// START KGU 2015-10-13: This follows a code snippet found in Root.draw(Canvas, Rect), which had been ineffective though
	@Override
	public Color getColor()
	{
		if (isNice)	// KGU 2015-10-13 condition inverted because it hadn't made sense the way it was
		{
			// The surrounding box is obvious - so it can't be mistaken for an instruction
			return Color.WHITE;
		}
		else
		{
			// The grey colour helps to distinguish the header from instructions
			return Color.LIGHT_GRAY;
		}
	}
	// END KGU 2015-10-13
	
	public Rect prepareDraw(Canvas _canvas)
	{
		Rect subrect = new Rect();

		rect.top=0;
		rect.left=0;

		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		Font titleFont = new Font(Element.font.getName(),Font.BOLD,Element.font.getSize());
		_canvas.setFont(titleFont);

		if(isNice==true)
		{
			rect.right=2*E_PADDING;
			for(int i=0;i<getText(false).count();i++)
			{
				int w = getWidthOutVariables(_canvas,getText(false).get(i),this);
				if(rect.right<w+2*E_PADDING)
				{
					rect.right=w+2*E_PADDING;
				}
			}
			rect.bottom=3*E_PADDING+getText(false).count()*fm.getHeight();
		}
		else
		{
			rect.right=2*E_PADDING;
			for(int i=0;i<getText(false).count();i++)
			{
				if(rect.right<getWidthOutVariables(_canvas,getText(false).get(i),this)+2*Math.round(E_PADDING/2))
				{
					rect.right=getWidthOutVariables(_canvas,getText(false).get(i),this)+2*Math.round(E_PADDING/2);
				}
			}
			rect.bottom=2*Math.round(E_PADDING/2)+getText(false).count()*fm.getHeight();
		}

		_canvas.setFont(Element.font);

		subrect=children.prepareDraw(_canvas);

		if(isNice==true)
		{
			rect.right=Math.max(rect.right,subrect.right+2*Element.E_PADDING);
		}
		else
		{
			rect.right=Math.max(rect.right,subrect.right);
		}

		rect.bottom+=subrect.bottom;
		this.width=rect.right-rect.left;
		this.height=rect.bottom-rect.top;

		return rect;
	}

	public void drawBuffered(Canvas _canvas, Rect _top_left)
	{
		// save reference to output canvas
		Canvas origCanvas = _canvas;
		// create a new image (buffer) to draw on
		BufferedImage bufferImg = new BufferedImage(_top_left.right+1,_top_left.bottom+1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bufferGraphics = (Graphics2D) bufferImg.getGraphics();
		_canvas = new Canvas(bufferGraphics);


		draw(_canvas,_top_left);

		// draw buffer to output canvas
		origCanvas.draw(bufferImg,0,0);

		// free up the buffer an clean memory
		bufferImg=null;
		System.gc();
	}

	public void draw(Canvas _canvas, Rect _top_left)
	{
		Rect myrect = new Rect();
		// START KGU 2015-10-13: Encapsulates all fundamental colouring and highlighting strategy
		//Color drawColor = getColor();
		Color drawColor = getFillColor();
		// END KGU 2015-10-13

		if(getText().count()==0)
		{
			getText().add("???");
		}
		else if ( ((String)getText().get(0)).trim().equals("") )
		{
			getText().delete(0);
			getText().insert("???",0);
		}

		rect=_top_left.copy();

		// START KGU 2015-10-13: 
		// Root-specific part put into an override version of getColor()
		// Remaining stuff replaced by new method getFillColor(), which hence comprises both
//		if(isNice==false)
//		{
//			drawColor=Color.WHITE;
//		}
//		else
//		{
//			drawColor=Color.LIGHT_GRAY;
//		}
//
//		drawColor=getColor();
//
//		if(selected==true)
//		{
//                if(waited==true) { drawColor=Element.E_WAITCOLOR; }
//                else { drawColor=Element.E_DRAWCOLOR; }
//		}
		// END KGU 2015-10-13
		
		// draw background
		myrect=_top_left.copy();


		Canvas canvas = _canvas;

		// erase background
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		canvas.fillRect(myrect);

		/*
		 if(isNice=false) then _canvas.pen.Width:=3;
		 _canvas.pen.Color:=clBlack;
		 _canvas.brush.Color:=drawColor;
		 if(isProgram=true) then  _canvas.Rectangle(rect)
		 else if(isNice=false) then
		 begin
		 _canvas.pen.Color:=clNone;
		 _canvas.Rectangle(rect);
		 end
		 else _canvas.RoundRect(rect.Left,rect.Top,rect.Right,rect.Bottom,30,30); //(rect.right-rect.left) div 3,(rect.Bottom-rect.Top) div 3);
		 _canvas.pen.Width:=1;
		 */

		// draw comment
		if(E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
		{
			canvas.setBackground(E_COMMENTCOLOR);
			canvas.setColor(E_COMMENTCOLOR);

			myrect.left+=2;
			myrect.top+=1;
			myrect.right=myrect.left+4;
			myrect.bottom-=2;

			canvas.fillRect(myrect);
		}

		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		Font titleFont = new Font(Element.font.getName(),Font.BOLD,Element.font.getSize());
		canvas.setFont(titleFont);

		// draw text
		if(isNice==true)
		{
			for(int i=0;i<getText(false).count();i++)
			{
				canvas.setColor(Color.BLACK);
				writeOutVariables(canvas,
								  rect.left+E_PADDING,
							      rect.top+(i+1)*fm.getHeight()+E_PADDING,
								  (String) getText(false).get(i)
								  ,this);
			}
		}
		else
		{
			for(int i=0;i<getText(false).count();i++)
			{
				canvas.setColor(Color.BLACK);
				// FIXME (KGU): Why aren't the variables highlighted here? (forgotten?)
				canvas.writeOut(  rect.left+Math.round(E_PADDING/2),
								rect.top+(i+1)*fm.getHeight()+Math.round(E_PADDING/2),
								(String) getText(false).get(i)
								);
			}
		}
		canvas.setFont(Element.font);
		
		int headerHeight = fm.getHeight()*getText(false).count();

		if(isNice==true)
		{
			headerHeight += 2*E_PADDING;
			rect.top = _top_left.top + headerHeight;
			rect.bottom -= E_PADDING;
			rect.left = _top_left.left + E_PADDING;
			rect.right -= E_PADDING;
		}
		else
		{
			headerHeight += 2*(E_PADDING/2);
			rect.top = _top_left.top + headerHeight;
			rect.left = _top_left.left;
		}

		children.draw(_canvas,rect);

		// draw box around
		canvas.setColor(Color.BLACK);
		canvas.drawRect(_top_left);


		// draw thick line
		if(isNice==false)
		{
			rect.top = _top_left.top + headerHeight - 1;
			rect.left = _top_left.left;
			canvas.drawRect(rect);
		}


		if (isProgram==false)
		{
			rect = _top_left.copy();
			canvas.setColor(Color.WHITE);
			canvas.drawRect(rect);
			canvas.setColor(Color.BLACK);
			rect = _top_left.copy();
			canvas.roundRect(rect);
		}

		rect = _top_left.copy();
	}

	// START KGU 2015-10-11: Methods merged into getElementByCoord(int _x, int _y, boolean _forSelection
//    @Override
//    public Element selectElementByCoord(int _x, int _y)
//    {
//            Element selMe = super.selectElementByCoord(_x,_y);
//            Element selCh = children.selectElementByCoord(_x,_y);
//            if(selCh!=null)
//            {
//                    selected=false;
//                    return selCh;
//            }
//            else
//            {
//                    return selMe;
//            }
//    }
//
//    @Override
//    public Element getElementByCoord(int _x, int _y)
//    {
//            Element selMe = super.getElementByCoord(_x,_y);
//            Element selCh = children.getElementByCoord(_x,_y);
//            if(selCh!=null)
//            {
//                    return selCh;
//            }
//            else
//            {
//                    return selMe;
//            }
//    }

    @Override
    public Element getElementByCoord(int _x, int _y, boolean _forSelection)
    {
            Element selMe = super.getElementByCoord(_x, _y, _forSelection);
            Element selCh = children.getElementByCoord(_x, _y, _forSelection);
            if(selCh!=null)
            {
                    if (_forSelection) selected = false;
                    return selCh;
            }
            else
            {
                    return selMe;
            }
    }
    // END KGU 2015-10-11

    /**
     * Checks if _child is a descendant of _parent in the tree
     * @param _child - Element to be verified as descendant of _parent
     * @param _parent - Element to be verified as ancestor of _child
     * @return true iff _cild is a descendant of _parent
     */
    public boolean checkChild(Element _child, Element _parent)
    {
            Element tmp = _child;
            boolean res = false;
            if(tmp != null)
            {
                    while ((tmp.parent!=null) && (res==false))
                    {
                            if(tmp.parent==_parent)
                            {
                                    res = true;
                            }
                            tmp=tmp.parent;
                    }
            }
            return res;
    }

    public void removeElement(Element _ele)
    {
            if(_ele != null)
            {
                    _ele.selected=false;
                    // START KGU#87 2015-11-22: Allow to remove entire non-empty Subqueues
                    //if ( !_ele.getClass().getSimpleName().equals("Subqueue") &&
                    //         !_ele.getClass().getSimpleName().equals("Root"))
                    if ( _ele instanceof IElementSequence)
                    {
                    	// START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                    	//hasChanged = ((IElementSequence)_ele).getSize() > 0;
                    	// END KGU#137 2016-01-11
                    	((IElementSequence)_ele).removeElements();
                    }
                    else if (!_ele.getClass().getSimpleName().equals("Root"))
                    // END KGU#87 2015-11-22
                    {
                            ((Subqueue) _ele.parent).removeElement(_ele);
                        	// START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                            //hasChanged=true;
                        	// END KGU#137 2016-01-11
                    }
            }
    }

    private void insertElement(Element _ele, Element _new, boolean _after)
    {
            if(_ele!=null && _new!=null)
            {
                    if (_ele.getClass().getSimpleName().equals("Subqueue"))
                    {
                            ((Subqueue) _ele).addElement(_new);
                            _ele.selected=false;
                            _new.selected=true;
                        	// START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                            //hasChanged=true;
                        	// END KGU#137 2016-01-11
                    }
                    else if (_ele.parent.getClass().getSimpleName().equals("Subqueue"))
                    {
                            int i = ((Subqueue) _ele.parent).getIndexOf(_ele);
                            if (_after) i++;
                            ((Subqueue) _ele.parent).insertElementAt(_new, i);
                            _ele.selected=false;
                            _new.selected=true;
                        	// START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                            //hasChanged=true;
                        	// END KGU#137 2016-01-11
                    }
                    else
                    {
                            // this case should never happen!
                    }

            }
    }
    
    public void addAfter(Element _ele, Element _new)
    {
    	insertElement(_ele, _new, true);
    }
    
    public void addBefore(Element _ele, Element _new)
    {
    	insertElement(_ele, _new, false);
    }
    
    
    // START KGU#43 2015-10-12: Breakpoint support
    @Override
    public void toggleBreakpoint()
    {
    	// root may never have a breakpoint!
    	breakpoint = false;
    }
    
    public void clearBreakpoints()
    {
            super.clearBreakpoints();
            children.clearBreakpoints();
    }
    // END KGU#43 2015-10-12

	// START KGU#43 2015-10-13
	// Recursively clears all execution flags in this branch
	public void clearExecutionStatus()
	{
		super.clearExecutionStatus();
		children.clearExecutionStatus();
	}
	// END KGU#43 2015-10-13

	// START KGU#64 2015-11-03: Is to improve drawing performance
	/**
	 * Recursively clears all drawing info this subtree down
	 * (To be overridden by structured sub-classes!)
	 */
	@Override
	public void resetDrawingInfoDown()
	{
		this.resetDrawingInfo();
		this.children.resetDrawingInfoDown();
	}
	// END KGU#64 2015-11-03

    public Rect prepareDraw(Graphics _g)
    {
        Canvas canvas = new Canvas((Graphics2D) _g);
        canvas.setFont(Element.getFont()); //?
        return this.prepareDraw(canvas);
    }

    public Rect draw(Graphics _g, Point point, Updater updater)
    {
        setDrawPoint(point);

        /*
        final Updater myUpdater = updater;
        new Thread(
            new Runnable()
            {
                public void run()
                {
                    // inform updaters
                    for(int u=0;u<updaters.size();u++)
                    {
                        if(updaters.get(u)!=myUpdater)
                        {
                            updaters.get(u).update();
                        }
                    }
                }
            }
        ).start();/**/

        // inform updaters
        for(int u=0;u<updaters.size();u++)
        {
            if(updaters.get(u)!=updater)
            {
                updaters.get(u).update(this);
            }
        }

        Canvas canvas = new Canvas((Graphics2D) _g);
        canvas.setFont(Element.getFont()); //?
        Rect myrect = this.prepareDraw(canvas);
        myrect.left+=point.x;
        myrect.top+=point.y;
        myrect.right+=point.x;
        myrect.bottom+=point.y;
        //this.drawBuffered(canvas,myrect);
        this.draw(canvas,myrect);
        //this.drawBuffered(canvas, myrect);

        return myrect;
    }

    public Rect draw(Graphics _g, Point point)
    {
        return draw(_g, point, null);
    }

    public Rect draw(Graphics _g)
    {
        return draw(_g, new Point(0,0), null);

        /*
        // inform updaters
        for(int u=0;u<updaters.size();u++)
        {
            updaters.get(u).update();
        }

        Canvas canvas = new Canvas((Graphics2D) _g);
        canvas.setFont(Element.getFont()); //?
        Rect myrect = this.prepareDraw(canvas);
        //this.drawBuffered(canvas,myrect);
        this.draw(canvas,myrect);

        return myrect;/**/
    }

    public Element copy()
    {
            Root ele = new Root(this.getText().copy());
            ele.setComment(this.getComment().copy());
            ele.setColor(this.getColor());
            ele.isNice=this.isNice;
            ele.isProgram=this.isProgram;
            ele.children=(Subqueue) this.children.copy();
            // START KGU#2 (#9) 2015-11-13: By the above replacement the new children were orphans
            ele.children.parent = ele;
            //ele.updaters = this.updaters;	// FIXME: Risks of this?
            // END KGU#2 (#9) 2015-11-13
            return ele;
    }
    
	// START KGU#119 2016-01-02: Bugfix #78
	/**
	 * Returns true iff _another is of same class, all persistent attributes are equal, and
	 * all substructure of _another recursively equals the substructure of this. 
	 * @param another - the Element to be compared
	 * @return true on recursive structural equality, false else
	 */
	@Override
	public boolean equals(Element _another)
	{
		return super.equals(_another) && this.children.equals(((Root)_another).children);
	}
	// END KGU#119 2016-01-02
	
	public void addUndo()
	{
		boolean test = this.equals(this);
		undoList.add((Subqueue)children.copy());
		// START KGU#120 2016-01-02: Bugfix #85 - park my StringList attributes on the stack top
		undoList.peek().setText(this.text.copy());
		undoList.peek().setComment(this.comment.copy());
		// END KGU#120 2016-01-02
		clearRedo();
		// START KGU#137 2016-01-11: Bugfix #103
		// If stack was lower than when last saved, then related info is going lost
		if (undoList.size() <= this.undoLevelOfLastSave)
		{
			this.undoLevelOfLastSave = -1;
		}
		// END KGU#137 2016-01-11
	}

    public boolean canUndo()
    {
            return (undoList.size()>0);
    }

    public boolean canRedo()
    {
            return (redoList.size()>0);
    }

    public void clearRedo()
    {
            redoList = new Stack<Subqueue>();
    }

    public void clearUndo()
    {
            undoList = new Stack<Subqueue>();
    		// START KGU#137 2016-01-11: Bugfix #103 - Most recently saved state is lost, too
            // FIXME: It might also be an initialisation (in which case = 0 would have been correct)
            this.undoLevelOfLastSave = -1;
    		// END KGU#137 2016-01-11
    }

    public void undo()
    {
            if (undoList.size()>0)
            {
                    // START KGU#137 2016-01-11: Bugfix #103 - rely on undoList level comparison 
                    //this.hasChanged=true;
                    // END KGU#137 2016-01-11
                    redoList.add((Subqueue)children.copy());
            		// START KGU#120 2016-01-02: Bugfix #85 - park my StringList attributes in the stack top
            		redoList.peek().setText(this.text.copy());
            		redoList.peek().setComment(this.comment.copy());
            		// END KGU#120 2016-01-02
                    children = undoList.pop();
                    children.parent=this;
            		// START KGU#120 2016-01-02: Bugfix #85 - restore my StringList attributes from stack
            		this.setText(children.getText().copy());
            		this.setComment(children.getComment().copy());
            		children.text.clear();
            		children.comment.clear();
            		// END KGU#120 2016-01-02
            }
    }

    public void redo()
    {
            if (redoList.size()>0)
            {
                    // START KGU#137 2016-01-11: Bugfix #103 - rely on undoList level comparison 
                    //this.hasChanged=true;
                    // END KGU#137 2016-01-11
                    undoList.add((Subqueue)children.copy());
            		// START KGU#120 2016-01-02: Bugfix #85 - park my StringList attributes on the stack top
            		undoList.peek().setText(this.text.copy());
            		undoList.peek().setComment(this.comment.copy());
            		// END KGU#120 2016-01-02
                    children = redoList.pop();
                    children.parent=this;
            		// START KGU#120 2016-01-02: Bugfix #85 - restore my StringList attributes from the stack
            		this.setText(children.getText().copy());
            		this.setComment(children.getComment().copy());
            		children.text.clear();
            		children.comment.clear();
            		// END KGU#120 2016-01-02
            }
    }

    // START KGU#137 2016-01-11: Bugfix #103 - Synchronize saving with undo / redo stacks
    /**
     * To be called after successful saving the diagram as NSD in order to record
     * the current undoStack size, such that we may know whether or not there are
     * unsaved changes.
     */
    public void rememberSaved()
    {
    	this.undoLevelOfLastSave = this.undoList.size();
    	// START KGU#137 2016-01-14: Bugfix #107
    	this.hasChanged = false;
    	// END KGU#137 2016-01-16
    }
    // END KGU#137 2016-01-11

    public boolean moveDown(Element _ele)
    {
            boolean res = false;
            if(_ele!=null)
            {
                    int i = ((Subqueue) _ele.parent).getIndexOf(_ele);
                    if (!_ele.getClass().getSimpleName().equals("Subqueue") &&
                            !_ele.getClass().getSimpleName().equals("Root") &&
                            ((i+1)<((Subqueue) _ele.parent).getSize()))
                    {
                            ((Subqueue) _ele.parent).removeElement(i);
                            ((Subqueue) _ele.parent).insertElementAt(_ele, i+1);
                        	// START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                            //hasChanged=true;
                        	// END KGU#137 2016-01-11
                            _ele.setSelected(true);
                            res=true;
                    }
            }
            return res;
    }

    public boolean moveUp(Element _ele)
    {
            boolean res = false;
            if(_ele!=null)
            {
                    int i = ((Subqueue) _ele.parent).getIndexOf(_ele);
                    if (!_ele.getClass().getSimpleName().equals("Subqueue") &&
                            !_ele.getClass().getSimpleName().equals("Root") &&
                            ((i-1>=0)))
                    {
                            ((Subqueue) _ele.parent).removeElement(i);
                            ((Subqueue) _ele.parent).insertElementAt(_ele, i-1);
                        	// START KGU#137 2016-01-11: Bugfix 103 - rely on addUndo() 
                            //hasChanged=true;
                        	// END KGU#137 2016-01-11
                            _ele.setSelected(true);
                            res=true;
                    }
            }
            return res;
    }

    public File getFile()
    {
            if(filename.equals(""))
            {
                    return null;
            }
            else
            {
                    return new File(filename);
            }
    }

    public String getPath()
    {
            if (filename.equals(""))
            {
                    return new String();
            }
            else
            {
                    File f = new File(filename);
                    return f.getAbsolutePath();
            }
    }

    /*************************************
     * Extract full text of all Elements
     *************************************/

    // START KGU 2015-10-16
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
     */
    @Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
    	// Whereas a subroutine diagram is likely to hold parameter declarations in the header,
    	// (such that we ought to deliver its header for the variable detection), this doesn't
    	// hold for programs.
    	if (!this.isProgram && !_instructionsOnly)
    	{
    		_lines.add(this.getText());
    	}
    	this.children.addFullText(_lines, _instructionsOnly);
    }
    // END KGU 2015-10-16

    /**
     * @deprecated Use _node.addFullText(_lines, _instructionsOnly) instead, where the argument
     * _instructionsOnly specifies whether only lines potentially introducing new variables are of interest.
     * @param _node - the instruction sequence to be evaluated
     * @param _lines - the StringList to append to
     */
    private void getFullText(Subqueue _node, StringList _lines)
    {
    	for(int i=0; i<_node.getSize(); i++)
    	{
    		_lines.add(((Element)_node.getElement(i)).getText());
    		if(_node.getElement(i).getClass().getSimpleName().equals("While"))
    		{
    			getFullText(((While) _node.getElement(i)).q,_lines);
    		}
    		else if(_node.getElement(i).getClass().getSimpleName().equals("For"))
    		{
    			getFullText(((For) _node.getElement(i)).q,_lines);
    		}
    		else if(_node.getElement(i).getClass().getSimpleName().equals("Repeat"))
    		{
    			getFullText(((Repeat) _node.getElement(i)).q,_lines);
    		}
    		else if(_node.getElement(i).getClass().getSimpleName().equals("Alternative"))
    		{
    			getFullText(((Alternative) _node.getElement(i)).qTrue,_lines);
    			getFullText(((Alternative) _node.getElement(i)).qFalse,_lines);
    		}
    		else if(_node.getElement(i).getClass().getSimpleName().equals("Case"))
    		{
    			Case c = ((Case) _node.getElement(i));
    			for (int j=0;j<c.qs.size();j++)
    			{
    				getFullText((Subqueue) c.qs.get(j),_lines);
    			}
    		}
    	}
    }

    /**
     * @deprecated Use getFullText(_instructionsOnly) instead, where argument _instructionsOnly
     * specifies whether only lines potentially introducing new variables are of interest. 
     * @return
     */
    public StringList getFullText()
    {
            StringList sl = getText().copy();
            getFullText(children,sl);
            return sl;
    }

    /**
     * @deprecated Use _el.getFullText(_instructionsOnly) instead, where argument _instructionsOnly
     * specifies whether only lines potentially introducing new variables are of interest.
     * @param _el
     * @return the composed StringList
     */
    public StringList getFullText(Element _el)
    {
            StringList sl = _el.getText().copy();

            if(_el.getClass().getSimpleName().equals("While"))
            {
                    getFullText(((While) _el).q,sl);
            }
            else if(_el.getClass().getSimpleName().equals("For"))
            {
                    getFullText(((For) _el).q,sl);
            }
            else if(_el.getClass().getSimpleName().equals("Repeat"))
            {
                    getFullText(((Repeat) _el).q,sl);
            }
            else if(_el.getClass().getSimpleName().equals("Alternative"))
            {
                    getFullText(((Alternative)_el).qTrue,sl);
                    getFullText(((Alternative) _el).qFalse,sl);
            }
            else if(_el.getClass().getSimpleName().equals("Case"))
            {
                    Case c = ((Case) _el);
                    for (int j=0;j<c.qs.size();j++)
                    {
                            getFullText((Subqueue) c.qs.get(j),sl);
                    }
            }

            return sl;
    }

    /*************************************
     * Extract full text of all Elements
     *************************************/



    private String cleanup(String _s)
    {
            //System.out.println("IN : "+_s);
            if(_s.indexOf("[")>=0)
            {
                    _s=_s.substring(0,_s.indexOf("["));
            }
            if(_s.indexOf(".")>=0)
            {
                    _s=_s.substring(0,_s.indexOf("."));
            }
            //System.out.println("OUT : "+_s);

            return _s;

    }

    //
    // Extract used variable names from an element.
    // HYP 1: (?) <- <used>
    // HYP 2: (?)[<used>] <- <used>
    // HYP 3: [output] <used>
    //
    private StringList getUsedVarNames(Element _ele)
    {
            return getUsedVarNames(_ele,true,false);
    }

    /**
     * Extract used variable names from an element (and its substructure), where the text lines
     * of the element itself are NOT included if _includeSelf is set false.
     * HYP 1: (?) <- <used>
     * HYP 2: (?)[<used>] <- <used>
     * HYP 3: [output] <used>
     * @param _ele - the element to be searched
     * @param _includeSelf - whether or not the own text lines of _ele are to be included
     * @return The StringList of passively used variable names
     */
    private StringList getUsedVarNames(Element _ele, boolean _includeSelf)
    {
            return getUsedVarNames(_ele,_includeSelf,false);
    }

    /**
     * Gathers the names of all variables that are used by Element _ele in expressions:
     * HYP 1: (?) <- <used>
     * HYP 2: (?)[<used>] <- <used>
     * HYP 3: [output] <used>
     * This works only if _ele is different from this.
     * @param _ele - the element to be searched
     * @param _includeSelf - whether or not the own text of _ele is to be considered (otherwise only substructure)
     * @param _onlyEle - if true then only the text of _ele itself is searched (no substructure)
     * @return StringList of variable names according to the above specification
     */
    public StringList getUsedVarNames(Element _ele, boolean _includeSelf, boolean _onlyEle)
    {
            StringList varNames = new StringList();

            if (_ele!=this)
            {
                    // get body text
                    StringList lines = new StringList();
                    if(_onlyEle==true)
                    {
                            lines.add(_ele.getText());
                    }
                    else
                    {
                            // START KGU#39 2015-10-16: What exactly is expected here?
                            //lines = getFullText(_ele);
                            lines = _ele.getFullText(false);
                            // END KGU#39 2015-10-16
                            if (_includeSelf==false)
                            {
                                    for(int i=0; i<_ele.getText().count(); i++)
                                    {
                                            lines.delete(0);
                                    }
                            }
                    }
                    //System.out.println(lines);

                    for(int i=0; i<lines.count(); i++)
                    {
                            String allText = lines.get(i);
                            // START KGU#23 2015-10-16: We better make sure the line is trimmed (for more precise keyword detection)
                            allText = allText.trim();
                            // END KGU#23 2015-10-16
                            
                            Regex r;

                            // modify "inc" and "dec" function (Pascal)
                            r = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 + $2"); allText=r.replaceAll(allText);
                            r = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)","$1 <- $1 + 1"); allText=r.replaceAll(allText);
                            r = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 - $2"); allText=r.replaceAll(allText);
                            r = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)","$1 <- $1 - 1"); allText=r.replaceAll(allText);

                            // modify mathematically correct assignments
                            // START KGU 2015-11-28: Disambiguate the operator symbols all at once now
                            //r = new Regex("(.*?)[:][=](.*?)","$1 <- $2"); allText=r.replaceAll(allText);     // 1.29
                            allText = Element.unifyOperators(allText);
                            // END KGU 2015-11-28

                            //
                            // Should use PARAMETERS HERE!!!
                            //

                            // kick off text parts
                            allText=allText.replaceAll("(.*?)['](.*?)['](.*?)","$1$3");
                            allText=allText.replaceAll("(.*?)[\"](.*?)[\"](.*?)","$1$3");

                            // input
                            r = new Regex(BString.breakup(D7Parser.input.trim())+"[ ](.*?)",D7Parser.input.trim()+" $1"); allText=r.replaceAll(allText);
                            // output
                            r = new Regex(BString.breakup(D7Parser.output.trim())+"[ ](.*?)",D7Parser.output.trim()+" $1"); allText=r.replaceAll(allText);
                            // START KGU#23 2015-10-16: there must be a gap between the keyword and the variable name!
                            // for
                            //r = new Regex(BString.breakup(D7Parser.preFor.trim())+"(.*?)"+D7Parser.postFor.trim()+"(.*?)",D7Parser.preFor.trim()+"$1"+D7Parser.postFor.trim()+"$2"); allText=r.replaceAll(allText);
                            // while
                            //r = new Regex(BString.breakup(D7Parser.preWhile.trim())+"(.*?)",D7Parser.preWhile.trim()+"$1"); allText=r.replaceAll(allText);
                            // repeat
                            //r = new Regex(BString.breakup(D7Parser.preRepeat.trim())+"(.*?)",D7Parser.preRepeat.trim()); allText=r.replaceAll(allText);
                            // for
                            //if(allText.indexOf(D7Parser.preFor.trim())>=0)
                            //{
                            //        allText=allText.substring(allText.indexOf(D7Parser.preFor.trim())+D7Parser.preFor.trim().length()).trim();
                            //}
                            // REPLACEMENT STARTS HERE:
                            // for
                            if (!D7Parser.preFor.trim().isEmpty()) {
                            	r = new Regex(BString.breakup(D7Parser.preFor.trim())+"[ ](.*?\\W)"+D7Parser.postFor.trim()+"(\\W.*?)",D7Parser.preFor.trim()+" $1 "+D7Parser.postFor.trim()+" $2");
                            }
                            else {
                            	r = new Regex("(.*?\\W)"+D7Parser.postFor.trim()+"(\\W.*?)","$1 "+D7Parser.postFor.trim()+" $2");
                            }
                            allText=r.replaceAll(allText);
                            // while
                            if (!D7Parser.preWhile.trim().isEmpty())
                            {
                            	r = new Regex(BString.breakup(D7Parser.preWhile.trim())+"(\\W.*?)",D7Parser.preWhile.trim()+"$1"); allText=r.replaceAll(allText);
                            }
                            // repeat
                            if (!D7Parser.preRepeat.trim().isEmpty())
                            {
                            	// FIXME (KGU) Why is the expression after the preRepeat keyword dropped here?
                            	r = new Regex(BString.breakup(D7Parser.preRepeat.trim())+"(.*?)",D7Parser.preRepeat.trim()); allText=r.replaceAll(allText);
                            }
                            // for
                            if(allText.indexOf(D7Parser.preFor.trim())==0)	// Must be at the line's very beginning
                            {
                                    allText=allText.substring(D7Parser.preFor.trim().length()).trim();
                            }
                            // END KGU 2015-10-16

                            // get names from assignments
                            // START KGU#126 2016-01-06: We can no longer expect operators to be padded
//                            if(allText.indexOf(" <- ")>=0)
//                            {
//                                    int pos = allText.indexOf(" <- ");
//
//                                    String s = allText.substring(0, pos);
//                                    if(allText.indexOf("[")>=0)
//                                    {
//                                            r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$2");
//                                            s=r.replaceAll(s);
//                                    } else { s=""; }
//
//                                    allText=s+" "+allText.substring(pos + " <- ".length(), allText.length());
//                            }
                            int asgnPos = allText.indexOf("<-");
                            if (asgnPos >= 0)
                            {
                                    String s = allText.substring(0, asgnPos);
                                    // START KGU#98 2016-01-06: This is only interesting within s
                                    //if (allText.indexOf("[") >= 0)
                                    if (s.indexOf("[") >= 0)
                                    // END KGU#98 2016-01-06
                                    {
                                            r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$2");
                                            s = r.replaceAll(s);
                                    } else { s = ""; }

                                    allText=s+" "+allText.substring(asgnPos + "<-".length(), allText.length());
                            }
                            // END KGU#126 2016-01-06

                            // cutoff output keyword
                            // START KGU#23 2015-10-16: Must start at the very beginning 
                            //if(allText.indexOf(D7Parser.output.trim())>=0)
                            //{
                            //    allText=allText.substring(allText.indexOf(D7Parser.output.trim())+D7Parser.output.trim().length()).trim();
                            //}
                            if(allText.indexOf(D7Parser.output.trim()) == 0)
                            {
                            	allText=allText.substring(D7Parser.output.trim().length()).trim();
                            }
                            // END KGU#23 2015-10-16

                            // and constant strings
                            if(allText.indexOf("'")>=0)
                            {
                                    /*r = new Regex("(.*?)['](.*?)['](.*?)","$1$3");
                                    allText=r.replaceAll(allText);*/
                                    allText=allText.replaceAll("(.*?)['](.*?)['](.*?)","$1$3");
                            }
                            if(allText.indexOf("\"")>=0)
                            {
                                    /*r = new Regex("(.*?)[\"](.*?)[\"](.*?)","$1$3");
                                    allText=r.replaceAll(allText);*/
                                    allText=allText.replaceAll("(.*?)[\"](.*?)[\"](.*?)","$1$3");
                            }

                            // parse out array index
                            if(allText.indexOf(D7Parser.input.trim())>=0)
                            {
                                    if(allText.indexOf("[")>=0)
                                    {
                                            r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$2");
                                            allText=r.replaceAll(allText);
                                    }
                                    else
                                    {
                                            allText = "";
                                    }
                            }

                            lines.set(i,allText);
                    }

                    // Analyse for used variables
                    // START KGU#26/KGU#65 2015-11-04: This code was practically identical to that in Element.writeOutVariables
                    // Moreover, we solve the erroneous in-String analysis (i.e. string literals had been scrutinized, too!) 
                    StringList parts = Element.splitLexically(lines.getLongString(), true);
                    // END KGU#26/KGU#65 2015-11-04
                    // START KGU 2015-11-29: Get rid of spaces
                    parts.removeAll(" ");
                    // END KGU 2015-11-29

                    //this.getVarNames(); // needed?  // CHECKITfile://localhost/Users/robertfisch/Desktop/TEST.nsd

                    for(int i=0; i<parts.count(); i++)
                    {
                            String display = parts.get(i);

                            //display = BString.replace(display, "<--","<-");	// No longer necessary, operators already unified
                            //display = BString.replace(display, "<-","\u2190");	// Not needed to identify variables
                            
                            // START KGU 2015-11-29: we would have got a more precise test (testidentifier(display)) but
                            // using it here would disable the analyser to detect incorrect variable names
                            display = display.trim();
                            // END KGU 2015-11-29
                            
                            if (!display.equals(""))
                            {
                                    if(this.variables.contains(display) && !varNames.contains(display))
                                    {
                                            //System.out.println("Adding to used var names: " + display);
                                            varNames.add(display);
                                    }
                            }
                    }

/*                    // FIXME (KGU) Disable this after testing
                    System.out.println("Lines: "+lines.getCommaText());
                    System.out.println("Parts: "+parts.getCommaText());
                    System.out.println("Vars:  "+variables.getCommaText());
                    System.out.println("Used:  "+varNames.getCommaText());
*/                    
            }

            varNames=varNames.reverse();
            //varNames.saveToFile("D:\\SW-Produkte\\Structorizer\\tests\\Variables_" + Root.fileCounter++ + ".txt");
            return varNames;
    }

    // get varnames of a bunch of textlines
    // HYP 1: VARNAME <- (?)
    // HYP 2: [input] VARNAME, VARNAME, VARNAME
    // HYP 3: for VARNAME <- (?) ...
    //
    public StringList getVarnames(StringList lines)
    {
            StringList varNames = new StringList();

            for(int i=0;i<lines.count();i++)
            {
                    String allText = lines.get(i);
                    Regex r;

                    // modify "inc" and "dec" function (Pascal)
                    r = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 + $2"); allText=r.replaceAll(allText);
                    r = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)","$1 <- $1 + 1"); allText=r.replaceAll(allText);
                    r = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 - $2"); allText=r.replaceAll(allText);
                    r = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)","$1 <- $1 - 1"); allText=r.replaceAll(allText);

                    // START KGU 2015-11-28:Disambiguate all operators at once
                    // modify mathematically correct assignments
                    //r = new Regex("(.*?)[:][=](.*?)","$1<-$2"); allText=r.replaceAll(allText);     // 1.29
                    allText = Element.unifyOperators(allText);
                    // END KGU 2015-11-28

                    //
                    // Should use PARAMETERS HERE!!!
                    //

                    // START KGU#102 2015-12-11: Bugfix #55 keyword replacement must not be done within identifiers
                    // FIXME (KGU): Is it necessary to accept the keywords case-independently (well, in Pascal it is)
//                    // input
//                    r = new Regex(BString.breakup(D7Parser.input.trim())+"[ ](.*?)",D7Parser.input.trim()+" $1"); allText=r.replaceAll(allText);
//                    // output
//                    r = new Regex(BString.breakup(D7Parser.output.trim())+"[ ](.*?)",D7Parser.output.trim()+" $1"); allText=r.replaceAll(allText);
//                    // for
//                    r = new Regex(BString.breakup(D7Parser.preFor.trim())+"(.*?)"+D7Parser.postFor.trim()+"(.*?)",D7Parser.preFor.trim()+"$1"+D7Parser.postFor.trim()+"$2"); allText=r.replaceAll(allText);
//                    // while
//                    r = new Regex(BString.breakup(D7Parser.preWhile.trim())+"(.*?)",D7Parser.preWhile.trim()+"$1"); allText=r.replaceAll(allText);
//                    // repeat
//                    r = new Regex(BString.breakup(D7Parser.preRepeat.trim())+"(.*?)",D7Parser.preRepeat.trim()); allText=r.replaceAll(allText);
//                    // for
//                    if(allText.indexOf(D7Parser.preFor.trim())>=0)
//                    {
//                            allText=allText.substring(allText.indexOf(D7Parser.preFor.trim())+D7Parser.preFor.trim().length()).trim();
//                    }
                    // input
                    r = new Regex("^"+BString.breakup(D7Parser.input.trim())+"[ ](.*?)", D7Parser.input.trim()+" $1"); allText=r.replaceAll(allText);
                    // output
                    r = new Regex("^"+BString.breakup(D7Parser.output.trim())+"[ ](.*?)", D7Parser.output.trim()+" $1"); allText=r.replaceAll(allText);
                    // for
                    r = new Regex("(^|[\\W])"+BString.breakup(D7Parser.preFor.trim())+"([ ].*?)"+D7Parser.postFor.trim()+"([ ].*?)", D7Parser.preFor.trim()+"$2"+D7Parser.postFor.trim()+"$3"); allText=r.replaceAll(allText);
                    // while
                    r = new Regex("^"+BString.breakup(D7Parser.preWhile.trim())+"(.*?)", D7Parser.preWhile.trim()+"$1"); allText=r.replaceAll(allText);
                    // repeat
                    r = new Regex("^"+BString.breakup(D7Parser.preRepeat.trim())+"(.*?)", D7Parser.preRepeat.trim()); allText=r.replaceAll(allText);
                    // for
                    if(allText.matches("(^|[\\W])" + D7Parser.preFor.trim() + "[ ](.*)"))
                    {
                            allText=allText.substring(allText.indexOf(D7Parser.preFor.trim()+" ")+D7Parser.preFor.trim().length()).trim();
                    }
                    // END KGU#102 2015-12-11

                    // START KGU#126 2016-01-06: Operators can no longer expected to be padded
//                    if(allText.indexOf(" <- ")>=0)
//                    {
//                            int pos = allText.indexOf(" <- ");
//                            allText = allText.substring(0,pos);
//                            //System.out.println("Adding to initialised var names: " + cleanup(allText.trim()));
//                            varNames.addOrderedIfNew(cleanup(allText.trim()));
//                    }
                    if(allText.indexOf("<-")>=0)
                    {
                            int pos = allText.indexOf("<-");
                            allText = allText.substring(0, pos);
                            // FIXME: In case of typed variables we should only add the last identifier part
                            //System.out.println("Adding to initialised var names: " + cleanup(allText.trim()));
                            varNames.addOrderedIfNew(cleanup(allText.trim()));
                    }
                    // END KGU#126 2016-01-06

                    // get names from read statements
                    if(allText.indexOf(D7Parser.input.trim())>=0)
                    {
                            String sr=allText.substring(allText.indexOf(D7Parser.input.trim())+D7Parser.input.trim().length()).trim();
                            StringList str = StringList.explode(sr,",");
                            if(str.count()>0)
                            {
                                    for(int j=0;j<str.count();j++)
                                    {
                                            String s = str.get(j);
                                            // START KGU#98 2016-01-06: Better don't glue the prefix and suffix without space
                                            //r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$1$3");
                                            r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$1 $3");
                                            // END KGU#98 2016-01-06
                                            s = r.replaceAll(s);

                                            if(!s.trim().equals(""))
                                            {
                                                    //System.out.println("Adding to initialised var names: " + s.trim());
                                                    varNames.addOrderedIfNew(s.trim());
                                            }
                                    }
                            }
                    }


                    lines.set(i,allText);
            }

            return varNames;
    }


    /**
     * Extract all variable names of the entire program.
     * @return list of variable names
     */
    public StringList getVarNames()
    {
            return getVarNames(this, false, false, true);
    }

    /**
     * Extract all variable names of the passed-in element _ele.
     * @return list of variable names
     */
    public StringList getVarNames(Element _ele)
    {
    	// Only the own variables, not recursively
    	return getVarNames(_ele, true, false);
    }

    public StringList getVarNames(Element _ele, boolean _onlyEle)
    {
    	// All variables, not only those from body (sub-structure)
    	return getVarNames(_ele, _onlyEle, false);
    }

    public StringList getVarNames(Element _ele, boolean _onlyEle, boolean _onlyBody)
    {
    	
    	return getVarNames(_ele, _onlyEle, _onlyBody, false);
    }

    private StringList getVarNames(Element _ele, boolean _onlyEle, boolean _onlyBody, boolean _entireProg)
    {

            StringList varNames = new StringList();

            // check root text for variable names
            // !!
            // !! This works only for Pascal-like syntax: functionname (<name>, <name>, ..., <name>:<type>; ...)
            // !! or VBA like syntax: functionname(<name>, <name> as <type>; ...)
            // !!
            // !! This will also detect the functionname itself if the parentheses are missing (bug?)
            // !!
        	// START KGU 2015-11-29: Decomposed -> new method collectParameters
//            try
//            {
//                    if(this.isProgram==false && _ele==this)
//                    {
//                            String rootText = this.getText().getText();
//                            rootText = rootText.replace("var ", "");
//                            if(rootText.indexOf("(")>=0)
//                            {
//                                    rootText=rootText.substring(rootText.indexOf("(")+1).trim();
//                                    rootText=rootText.substring(0,rootText.indexOf(")")).trim();
//                            }
//
//                            StringList params = StringList.explode(rootText,";");
//                            if(params.count()>0)
//                            {
//                                    for(int i=0;i<params.count();i++)
//                                    {
//                                            String S = params.get(i);
//                                            if(S.indexOf(":")>=0)
//                                            {
//                                                    S=S.substring(0,S.indexOf(":")).trim();
//                                            }
//// START KGU#18 2014-10-18 "as" must not be detected if it's a substring of some identifier
////                                            if(S.indexOf("as")>=0)
////                                            {
////                                                    S=S.substring(0,S.indexOf("as")).trim();
////                                            }
//                                            // Actually, a sensible approach should consider any kinds of white space and delimiters...
//                                            if(S.indexOf(" as ")>=0)
//                                            {
//                                                    S=S.substring(0,S.indexOf(" as ")).trim();
//                                            }
//// END KGU#18 2014-10-18                                            
//                                            StringList vars = StringList.explode(S,",");
//                                            for(int j=0;j<vars.count();j++)
//                                            {
//                                                    if(!vars.get(j).trim().equals(""))
//                                                    {
//                                                        //System.out.println("Adding: "+vars.get(j).trim());
//                                                        varNames.add(vars.get(j).trim());
//                                                    }
//                                            }
//                                    }
//                            }
//                    }
//            }
//            catch (Exception e)
//            {
//                    // Don't do anything if this is the entire program
//                    if (!_entireProg) {	
//                            System.out.println(e.getMessage());
//                    }
//            }
            if (this.isProgram==false && _ele==this && !_onlyBody)
            {
            	collectParameters(varNames, null);
            }

            // get body text
            StringList lines;
            if(_onlyEle==true && !_onlyBody)
            {
                    lines = _ele.getText().copy();
            }
            else if(_entireProg)
            {
                    // START KGU#39 2015-10-16: Use object methods now
                    //lines = getFullText();
                    lines = this.getFullText(_onlyBody);
                    // END KGU#39 2015-10-16
            }
            else
            {
                    // START KGU#39 2015-10-16: Use object methods now
                    //lines = getFullText(_ele);
                    lines = _ele.getFullText(true);
                    // START KGU#39 2015-10-16
            }
            
//            // KGU 2015-11-30: Turned out to be too simple: getFullText(true) already omits the
//            // own "header" text unless _ele is this and this is no program
//            if(_onlyBody==true)
//            	
//            {
//                    for(int l=0;l<_ele.getText().count();l++)
//                    {
//                            lines.delete(0);
//                    }
//            }

            varNames.add(getVarnames(lines));

            varNames=varNames.reverse();
            if (_entireProg) {
                    this.variables=varNames;
            }
            //System.out.println(varNames.getCommaText());
            return varNames;
    }
    
    // START BFI 2015-12-10
    public String getReturnType()
    {
        try 
        {
            // stop and return null if this is not a function
            if(this.isProgram) return null;
            // get the root text
            String rootText = this.getText().getText(); 
            // stop if there is no closing parenthesis
            if(rootText.indexOf(")")<0) return null;
            // get part after closing parenthesis
            rootText = rootText.substring(rootText.indexOf(")")+1);
            // replace eventually ":"
            rootText = rootText.replaceAll(":", "");
            
            return rootText.trim();
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    /*
    // test getReturnType()
    public static void main(String[] args)
    {
        StringList sl = new StringList();
        sl.add("test(a,b:integer; c:real): string");
        Root root = new Root(sl);
        root.isProgram=false;

        System.out.println("Starting ...");
        System.out.println(root.getReturnType());
        System.out.println("- end -");
    }
    */
            
    /**
     * Identifies parameter names and types of the routine and returns an array list
     * of Param objects being name-type pairs.
     * This is just a different aggregation of the same results getParameterNames() and
     * getParameterTypes() would provide.
     * @return the list of the declared parameters
     */
    public ArrayList<Param> getParams()
    {
            ArrayList<Param> resultVars = new ArrayList<Param>();

            StringList names = new StringList();
            StringList types = new StringList();
            
            collectParameters(names, types);
            
            for (int i = 0; i < names.count(); i++)
            {
            	resultVars.add(new Param(names.get(i), types.get(i)));
            }
            
            return resultVars;
    }    
    
    /*
    // test getParams()
    public static void main(String[] args)
    {
        StringList sl = new StringList();
        sl.add("a,b:integer; c:real");
        Root root = new Root(sl);
        root.isProgram=false;

        System.out.println("Starting ...");
        ArrayList<Param> vars = root.getParams();
        for(int i=0; i<vars.size(); i++)
        {
           System.out.println(i+") "+vars.get(i).name+" = "+vars.get(i).type);
        }
        System.out.println("- end -");
    }
    /**/
    // END BFI 2015-12-10
    
    private String errorMsg(JLabel _label, String _rep)
    {
            String res = _label.getText();
            res = res.replaceAll("%", _rep);
            return res;
    }

    // START KGU#78 2015-11-25: We additionally supervise return mechanisms
    //private void analyse(Subqueue _node, Vector _errors, StringList _vars, StringList _uncertainVars)
    /**
     * Analyses the subtree, which _node is local root of
     * @param _node - subtree root
     * @param _errors - the collected errors (may be enhanced by the call)
     * @param _vars - names of variables being set within the subtree
     * @param _uncertainVars - names of variables being set in some branch of the subtree 
     * @param _resultFlags - a boolean array: {usesReturn?, usesResult?, usesProcName?}
     */
    private void analyse(Subqueue _node, Vector<DetectedError> _errors, StringList _vars, StringList _uncertainVars, boolean[] _resultFlags)
    {
    	DetectedError error;

    	for(int i=0;i<_node.getSize();i++)
    	{
    		Element ele = _node.getElement(i);
    		
    		// get all set variables from actual instruction (just this level, no substructre)
    		StringList myVars = getVarNames(ele);


    		// CHECK: assignment in condition (#8)
    		// FIXME (KGU 2015-12-16): What about Case elements?
    		if(ele.getClass().getSimpleName().equals("While")
    				||
    				ele.getClass().getSimpleName().equals("Repeat")
    				||
    				ele.getClass().getSimpleName().equals("Alternative"))
    		{
    			String text = ele.getText().getLongString();
    			if ( text.contains("<-") || text.contains(":=") )
    			{
    				//error  = new DetectedError("It is not allowed to make an assignment inside a condition.",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error08,""), ele);
    				addError(_errors,error,8);
    			}
    		}


    		// CHECK: two checks in one loop: (#5) & (#7)
    		for(int j=0;j<myVars.count();j++)
    		{
    			// CHECK: non-uppercase var (#5)
    			if(!myVars.get(j).toUpperCase().equals(myVars.get(j)) && !rootVars.contains(myVars.get(j)))
    			{
    				if(!((myVars.get(j).toLowerCase().equals("result") && this.isProgram==false)))
    				{
    					//error  = new DetectedError("The variable «"+myVars.get(j)+"» must be written in uppercase!",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error05,myVars.get(j)), ele);
    					addError(_errors,error,5);
    				}
    			}

    			// CHECK: correct identifiers (#7)
    			if(testidentifier(myVars.get(j))==false)
    			{
    				//error  = new DetectedError("«"+myVars.get(j)+"» is not a valid name for a variable!",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error07_3,myVars.get(j)), ele);
    				addError(_errors,error,7);
    			}

    			// START KGU#78 2015-11-25
    			if (!this.isProgram && myVars.get(j).toLowerCase().equals("result"))
    			{
    				_resultFlags[1] = true;
    				if (_resultFlags[0] || _resultFlags[2])

    				{
    					//error  = new DetectedError("Your function seems to use several competitive return mechanisms!",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error13_3, myVars.get(j)), ele);
    					addError(_errors,error,13);                                            	
    				}
    			}
    			else if (!this.isProgram && myVars.get(j).equals(getMethodName()))
    			{
    				_resultFlags[2] = true;
    				if (_resultFlags[0] || _resultFlags[1])

    				{
    					//error  = new DetectedError("Your functions seems to use several competitive return mechanisms!",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error13_3, myVars.get(j)), ele);
    					addError(_errors,error,13);                                            	
    				}
    			}
    			// END KGU#78 2015-11-25

    		}

    		// CHECK: two checks with the same input condition
    		if(ele.getClass().getSimpleName().equals("Instruction"))
    		{
    			StringList test = ele.getText();

    			// CHECK: wrong multi-line instruction (#10 - new!)
    			boolean isInput = false;
    			boolean isOutput = false;
    			boolean isAssignment = false;

    			// Check every instruction line...
    			for(int l=0; l<test.count(); l++)
    			{
    				// CHECK: wrong assignment (#11 - new!)
    				String myTest = test.get(l);

    				// START KGU#65/KGU#126 2016-01-06: More precise analysis, though expensive
//    				// FIXME (KGU): Shouldn't we better do a lexical splitting here (see below)? 
//    				// Remove all strings delimited by '
//    				myTest=myTest.replaceAll("(.*?)['](.*?)['](.*?)","$1$3");
//    				// Remove all strings delimited by "
//    				myTest=myTest.replaceAll("(.*?)[\"](.*?)[\"](.*?)","$1$3");
//
//    				//System.out.println(" -- "+myTest);
//
//    				// FIXME (KGU): condition is not sound
//    				String unified = Element.unifyOperators(myTest);
//    				if (unified.contains(" == ") && !unified.contains(" <- "))
//    				{
//    					//error  = new DetectedError("You probably made an assignment error. Please check this instruction!",(Element) _node.getElement(i));
//    					error  = new DetectedError(errorMsg(Menu.error11,""), ele);
//    					addError(_errors,error,11);
//    				}
//
//    				// CHECK: wrong multi-line instruction (#10 - new!)
//    				String myText = test.get(l);
//    				if (myText.contains(D7Parser.input.trim())) {isInput=1;}
//    				if (myText.contains(D7Parser.output.trim())) {isOutput=1;}
//    				if ( myText.contains("<-") || myText.contains(":=") || myText.contains("<--")) {isAssignment=1;}

    				StringList tokens = splitLexically(myTest, true);
    				unifyOperators(tokens, false);
    				if (tokens.contains("<-"))
    				{
    					isAssignment = true;
    				}
    				else if (tokens.contains("=="))
    				{
    				        //error  = new DetectedError("You probably made an assignment error. Please check this instruction!",(Element) _node.getElement(i));
    				        error  = new DetectedError(errorMsg(Menu.error11,""), _node.getElement(i));
    				        addError(_errors,error,11);
    				}
    				
    				// CHECK: wrong multi-line instruction (#10 - new!)
    				if (myTest.startsWith(D7Parser.input.trim())) {isInput = true;}
    				if (myTest.startsWith(D7Parser.output.trim())) {isOutput = true;}
    				// END KGU#65/KGU#126 2016-01-06

    			}
    			// CHECK: wrong multi-line instruction (#10 - new!)
    			if (isInput && isOutput && isAssignment)
    			{
    				//error  = new DetectedError("A single instruction element should not contain input/output instructions and assignments!",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error10_1,""), ele);
    				addError(_errors, error, 10);
    			}
    			else if (isInput && isOutput)
    			{
    				//error  = new DetectedError("A single instruction element should not contain input and output instructions!",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error10_2,""), ele);
    				addError(_errors, error, 10);
    			}
    			else if (isInput && isAssignment)
    			{
    				//error  = new DetectedError("A single instruction element should not contain input instructions and assignments!",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error10_3,""), ele);
    				addError(_errors, error, 10);
    			}
    			else if (isOutput && isAssignment)
    			{
    				//error  = new DetectedError("A single instruction element should not contain ouput instructions and assignments!",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error10_4,""), ele);
    				addError(_errors, error, 10);
    			}
    		}


    		// CHECK: non-initialised var (no REPEAT)  (#3)
    		StringList myUsed = getUsedVarNames(_node.getElement(i),true,true);
    		if(!ele.getClass().getSimpleName().equals("Repeat"))
    		{
    			for(int j=0;j<myUsed.count();j++)
    			{
    				if(!_vars.contains(myUsed.get(j)) && !_uncertainVars.contains(myUsed.get(j)))
    				{
    					//error  = new DetectedError("The variable «"+myUsed.get(j)+"» has not yet been initialized!",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error03_1,myUsed.get(j)), ele);
    					addError(_errors,error,3);
    				}
    				else if(_uncertainVars.contains(myUsed.get(j)))
    				{
    					//error  = new DetectedError("The variable «"+myUsed.get(j)+"» may not have been initialized!",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error03_2,myUsed.get(j)), ele);
    					addError(_errors,error,3);
    				}
    			}
    		}

    		/*////// AHHHHHHHH ////////
                            getUsedVarNames should also parse for new variable names,
                            because any element that uses a variable that has never been
                            assigned, this variable will not be known and thus not
                            detected at all!
    		 */
    		/*
                            if(_node.getElement(i).getClass().getSimpleName().equals("Instruction"))
                            {
                                    System.out.println("----------------------------");
                                    System.out.println(((Element) _node.getElement(i)).getText());
                                    System.out.println("----------------------------");
                                    System.out.println("Vars : "+myVars);
                                    System.out.println("Init : "+_vars);
                                    System.out.println("Used : "+myUsed);
                                    //System.out.println("----------------------------");
                            }
                            /**/

    		// START KGU#2/KGU#78 2015-11-25: New checks for Call and Jump elements
    		// CHECK: Correct syntax of Call elements (#15) New!
    		if (ele instanceof Call)
    		{
    			String text = ele.getText().getLongString();
    			text = Element.unifyOperators(text);
    			// START KGU#126 2016-01-06: We may no longer expect operatos to be padded
//    			if ( text.contains(" <- ") )
//    			{
//    				text = text.substring(text.indexOf(" <- ") + 4);
//    			}
    			if ( text.contains("<-") )	// FIXME: Detection within string literals!?
    			{
    				text = text.substring(text.indexOf("<-") + 2);
    			}
    			// END KGU#126 2016-01-06
    			Function func = new Function(text);
    			if (!func.isFunction())
    			{
    				//error  = new DetectedError("The CALL hasn't got form «[ <var> " + "\u2190" +" ] <routine_name>(<arg_list>)»!",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error15, ""), ele);
    				addError(_errors,error,15);
    			}
    			
    		}
    		// CHECK: Correct usage of Jump, including return (#16) New!
			else if (ele instanceof Jump)
			{
    			StringList sl = _node.getElement(i).getText();
    			String jumpKeywords = "«" + D7Parser.preLeave + "», «" + D7Parser.preReturn +
    					"», «" + D7Parser.preExit + "»";
				String line = sl.get(0).trim().toLowerCase();
				
				// Preparation
				boolean isReturn = line.matches(Matcher.quoteReplacement(D7Parser.preReturn) + "([\\W].*|$)");
				boolean isLeave = line.matches(Matcher.quoteReplacement(D7Parser.preLeave) + "([\\W].*|$)");
				boolean isExit = line.matches(Matcher.quoteReplacement(D7Parser.preExit) + "([\\W].*|$)");
				boolean isJump = isLeave || isExit ||
						line.matches("exit([\\W].*|$)") ||	// Also check hard-coded keywords
						line.matches("break([\\W].*|$)");	// Also check hard-coded keywords
				Element parent = ele.parent;
				// Count the nested loops
				int levelsDown = 0;
				// Routines and Parallel sections cannot be penetrated by leave or break
				while (parent != null && !(parent instanceof Root) && !(parent instanceof Parallel))
				{
					if (parent instanceof While ||
							parent instanceof Repeat ||
							parent instanceof For ||
							parent instanceof Forever)
					{
						levelsDown++;
					}
					parent = parent.parent;
				}
				boolean insideParallel = parent instanceof Parallel;
				
				// CHECK: Incorrect Jump syntax?
				if (sl.count() > 1 || !(isJump || isReturn || line.isEmpty()))
				{
					//error = new DetectedError("A JUMP element must contain exactly one of «exit n», «return <expr>», or «leave [n]»!",(Element) _node.getElement(i));
					error = new DetectedError(errorMsg(Menu.error16_1, jumpKeywords), ele);
					addError(_errors, error, 16);
				}
				// CHECK: Correct usage of return (nearby check result mechanisms) (#13, #16)
				else if (isReturn)
				{
					_resultFlags[0] = true;
					myVars.addIfNew("result");
					// START KGU#78 2015-11-25: Different result mechanisms?
					if (_resultFlags[1] || _resultFlags[2])
					{
						//error = new DetectedError("Your function seems to use several competitive return mechanisms!",(Element) _node.getElement(i));
						error = new DetectedError(errorMsg(Menu.error13_3, D7Parser.preReturn), ele);
						addError(_errors, error, 13);                                            	
					}
					// Check if we are inside a Parallel construct
					if (insideParallel)
					{
						//error = new DetectedError("You must not directly return out of a parallel thread!",(Element) _node.getElement(i));
						error = new DetectedError(errorMsg(Menu.error16_5, ""), ele);
						addError(_errors, error, 16);                                            							
					}
				}
				else if (isLeave)
				{
					int levelsUp = 1;
					if (line.length() > D7Parser.preLeave.length())
					{
						try
						{
							levelsUp = Integer.parseInt(line.substring(D7Parser.preLeave.length()).trim());
						}
						catch (Exception ex)
						{
							//error = new DetectedError("Wrong argument for this kind of JUMP (should be an integer constant)!",(Element) _node.getElement(i));
							error = new DetectedError(errorMsg(Menu.error16_6, ""), ele);
							addError(_errors, error, 16);    					    							
						}
					}
					// Compare the number of nested loops we are in with the requested jump levels
					if (levelsUp < 1 || levelsUp > levelsDown)
					{
						//error = new DetectedError("Cannot leave or break more loop levels than being nested in!",(Element) _node.getElement(i));
						error = new DetectedError(errorMsg(Menu.error16_4, String.valueOf(levelsDown)), ele);
						addError(_errors, error, 16);    								
					}
				}
				else if (isExit && line.length() > D7Parser.preExit.length())
				{
					try
					{
						Integer.parseInt(line.substring(D7Parser.preExit.length()).trim());
					}
					catch (Exception ex)
					{
						//error = new DetectedError("Wrong argument for this kind of JUMP (should be an integer constant)!",(Element) _node.getElement(i));
						error = new DetectedError(errorMsg(Menu.error16_6, ""), ele);
						addError(_errors, error, 16);    					    							
					}
				}
			}
			else if (ele instanceof Instruction)	// May also be a subclass (except Call and Jump)!
    		{
    		// END KGU#78 2015-11-25
    			StringList sl = _node.getElement(i).getText();
    			for(int ls=0; ls<sl.count(); ls++)
    			{
    				String line = sl.get(ls).trim().toLowerCase();
    				// START KGU#78 2015-11-25: Make sure a potential result is following 
    				//if(line.toLowerCase().indexOf("return")==0)
    				boolean isReturn = line.matches(Matcher.quoteReplacement(D7Parser.preReturn) + "([\\W].*|$)");
    				boolean isLeave = line.matches(Matcher.quoteReplacement(D7Parser.preLeave) + "([\\W].*|$)");
    				boolean isExit = line.matches(Matcher.quoteReplacement(D7Parser.preExit) + "([\\W].*|$)");
    				boolean isJump = isLeave || isExit ||
    						line.matches("exit([\\W].*|$)") ||	// Also check hard-coded keywords
							line.matches("break([\\W].*|$)");	// Also check hard-coded keywords
    				if (isReturn && !line.substring(D7Parser.preReturn.length()).isEmpty())
    				// END KGU#78 2015-11-25
    				{
    					_resultFlags[0] = true;
    					myVars.addIfNew("result");
    					// START KGU#78 2015-11-25: Different result mechanisms?
    					if (_resultFlags[1] || _resultFlags[2])
    					{
    						//error = new DetectedError("Your function seems to use several competitive return mechanisms!",(Element) _node.getElement(i));
    						error = new DetectedError(errorMsg(Menu.error13_3, D7Parser.preReturn), ele);
    						addError(_errors, error, 13);                                            	
    					}
    					// END KGU#78 2015-11-25
    				}
    				// START KGU#78 2015-11-25: New test (#16)
    				if (!(ele instanceof Jump) &&
    						(isJump || (isReturn && !(_node.parent instanceof Root &&
    								ls == sl.count()-1 && i == _node.getSize()-1)))
    								)
    				{
    					//error = new DetectedError("An exit, leave or break instruction is only allowed as JUMP element!",(Element) _node.getElement(i));
    					//error = new DetectedError("A return instruction, unless at final position, must form a JUMP element!",(Element) _node.getElement(i));
    					error = new DetectedError(errorMsg((isReturn ? Menu.error16_2 : Menu.error16_3), line), ele);
    					addError(_errors, error, 16);
    				}
    				// END KGU#78 2015-11-25

    			}
    		// START KGU#78 2015-11-25
    		}
    		// END KGU#78 2015-11-25

    		// add detected vars to initialised vars
    		_vars.addIfNew(myVars);


    		// CHECK: endless loop (#2)
    		if(ele.getClass().getSimpleName().equals("While")
    				||
    				ele.getClass().getSimpleName().equals("Repeat"))
    		{
    			// get used variables from inside the loop
    			StringList usedVars = getVarNames(ele, false);
    			// get loop condition variables
    			StringList loopVars = getUsedVarNames(ele, true, true);

    			/*
    			System.out.println(ele.getClass().getSimpleName() + " : " + ele.getText().getLongString());
    			System.out.println("Used : "+usedVars);
    			System.out.println("Loop : "+loopVars);
    			/**/

    			boolean check = false;
    			for(int j=0; j<loopVars.count(); j++)
    			{
    				check = check || usedVars.contains(loopVars.get(j));
    			}
    			if (check==false)
    			{
    				//error  = new DetectedError("No change of the variables in the condition detected. Possible endless loop ...",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error02,""), ele);
    				addError(_errors,error,2);
    			}
    		}

    		// CHECK: loop var modified (#1) and loop parameter consistency (#14 new!)
    		if(ele.getClass().getSimpleName().equals("For"))
    		{
    			// get assigned variables from inside the FOR-loop
    			StringList usedVars = getVarNames(ele, false, true);
    			// get loop variable (that should be only one!!!)
    			StringList loopVars = getVarNames(ele, true);

    			/*
                                    System.out.println("USED : "+usedVars);
                                    System.out.println("LOOP : "+loopVars);
                                    /**/

    			if(loopVars.count()==0)
    			{
    				//error  = new DetectedError("WARNING: No loop variable detected ...",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error01_1,""), ele);
    				addError(_errors,error,1);
    			}
    			else
    			{
    				if(loopVars.count()>1)
    				{
    					//error  = new DetectedError("WARNING: More than one loop variable detected ...",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error01_2,""), ele);
    					addError(_errors,error,1);
    				}

    				if(usedVars.contains(loopVars.get(0)))
    				{
    					//error  = new DetectedError("You are not allowed to modify the loop variable «"+loopVars.get(0)+"» inside the loop!",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error01_3, loopVars.get(0)), ele);
    					addError(_errors,error,1);
    				}
    			}

    			// START KGU#3 2015-11-03: New check for consistency of the loop header
    			For forEle = (For)ele;
    			if (!forEle.checkConsistency()) {
    				//error  = new DetectedError("FOR loop parameters are not consistent to the loop heading text!", elem);
    				error = new DetectedError(errorMsg(Menu.error14_1,""), ele);
    				addError(_errors, error, 14);
    			}
    			String stepStr = forEle.splitForClause()[4];
    			if (!stepStr.isEmpty())
    			{
    				// Just in case...
    				//error  = new DetectedError("FOR loop step parameter «"+stepStr+"» is no legal integer constant!", elem);
    				error = new DetectedError(errorMsg(Menu.error14_2, stepStr), ele);
    				try {
    					int stepVal = Integer.parseInt(stepStr);
    					if (stepVal == 0)
    					{
    						// Two kinds of error at the same time
    						addError(_errors, error, 14);
    						//error  = new DetectedError("No change of the variables in the condition detected. Possible endless loop ...",(Element) _node.getElement(i));
    						error  = new DetectedError(errorMsg(Menu.error02,""), ele);
    						addError(_errors, error, 2);
    					}
    				}
    				catch (NumberFormatException ex)
    				{
    					addError(_errors, error, 14);                                    		
    				}
    			}
    			// END KGU#3 2015-11-03
    		}

    		// CHECK: if with empty T-block (#4)
    		if(ele.getClass().getSimpleName().equals("Alternative"))
    		{
    			if(((Alternative) _node.getElement(i)).qTrue.getSize()==0)
    			{
    				//error  = new DetectedError("You are not allowed to use an IF-statement with an empty TRUE-block!",(Element) _node.getElement(i));
    				error  = new DetectedError(errorMsg(Menu.error04,""), ele);
    				addError(_errors,error,4);
    			}
    		}
    		
    		// CHECK: Inconsistency risk due to concurent variable access by parallel threads (#17) New!
    		if (ele.getClass().getSimpleName().equals("Parallel"))
    		{
    			// These hash tables will contain a binary pattern per variable name indicating
    			// which threads will set or use the respective veriable name. If more than
    			// Integer.SIZE (supposed to be 32) parallel branches exist (pretty unlikely)
    			// than analysis will just give up beyond the Interger.SIZEth thread.
    			Hashtable<String,Integer> myInitVars = new Hashtable<String,Integer>();
    			Hashtable<String,Integer> myUsedVars = new Hashtable<String,Integer>();
    			Iterator<Subqueue> iter = ((Parallel)ele).qs.iterator();
    			int threadNo = 0;
    			while (iter.hasNext() && threadNo < Integer.SIZE)
    			{
    				Subqueue sq = iter.next();
    				// Get all variables initialised or otherwise set within the thread
    				StringList threadSetVars = getVarNames(sq,false,false);
    				// Get all variables used within the thread
    				StringList threadUsedVars = getUsedVarNames(sq,false,false);        				
    				// First register all variables being an assignment target
    				for (int v = 0; v < threadSetVars.count(); v++)
    				{
    					String varName = threadSetVars.get(v);
    					Integer count = myInitVars.putIfAbsent(varName, 1 << threadNo);
    					if (count != null) { myInitVars.put(varName, count.intValue() | (1 << threadNo)); }
    				}
    				// Then register all used variables
    				for (int v = 0; v < threadUsedVars.count(); v++)
    				{
    					String varName = threadSetVars.get(v);
    					Integer count = myUsedVars.putIfAbsent(varName, 1 << threadNo);
    					if (count != null) { myUsedVars.put(varName, count.intValue() | (1 << threadNo)); }
    				}
    				threadNo++;
    			}
    			// walk trough the hashtables and check for conflicts
    			Enumeration<String> keys = myInitVars.keys();
    			while ( keys.hasMoreElements() )
    			{
    				String key = keys.nextElement();
    				int initPattern = myInitVars.get(key);
   					// Trouble may arize if several branches access the same variable (races,
   					// inconsistency). So we must report these cases.
    				Integer usedPattern = myUsedVars.get(key);
    				// Do other threads than those setting the variable access it?
    				boolean isConflict = usedPattern != null && (usedPattern.intValue() | initPattern) != initPattern;
    				// Do several threads assign values to variable key?
    				if (!isConflict)
    				{
    					int count = 0;
    					for (int bit = 0; bit < Integer.SIZE && count < 2; bit++)
    					{
    						if ((initPattern & 1) != 0) count++;
    						initPattern >>= 1;
    					}
    					isConflict = count > 1;
    				}
    				// Do several threads access the variable assigned in some of them?
    				if (!isConflict && usedPattern != null)
    				{
    					int count = 0;
    					for (int bit = 0; bit < Integer.SIZE && count < 2; bit++)
    					{
    						if ((usedPattern.intValue() & 1) != 0) count++;
    						usedPattern >>= 1;
    					}
    					isConflict = count > 1;
    				}
   					if (isConflict)
   					{
   						//error  = new DetectedError("Consistency risk due to concurrent access to variable «%» by several parallel threads!",(Element) _node.getElement(i));
   						error  = new DetectedError(errorMsg(Menu.error17, key), ele);
   						addError(_errors, error, 17);
   					}
    			}
    			
    		}

    		// continue analysis for subelements
    		if(ele.getClass().getSimpleName().equals("While"))
    		{
    			analyse(((While) ele).q,_errors,_vars,_uncertainVars, _resultFlags);
    		}
    		else if(ele.getClass().getSimpleName().equals("For"))
    		{
    			analyse(((For) ele).q,_errors,_vars,_uncertainVars, _resultFlags);
    		}
    		else if(ele.getClass().getSimpleName().equals("Repeat"))
    		{
    			analyse(((Repeat) ele).q,_errors,_vars,_uncertainVars, _resultFlags);

    			// CHECK: non init var (REPEAT only, because it must be analysed _after_ the body!)  (#3)
    			/*
                                    System.out.println("----------------------------");
                                    System.out.println("Init : "+_vars);
                                    System.out.println("Used : "+myUsed);
    			 */

    			//myUsed = getUsedVarNames((Element) _node.getElement(i),true,true);
    			for(int j=0;j<myUsed.count();j++)
    			{
    				if(!_vars.contains(myUsed.get(j)) && !_uncertainVars.contains(myUsed.get(j)))
    				{
    					//error  = new DetectedError("The variable «"+myUsed.get(j)+"» has not yet been initialized!",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error03_1,myUsed.get(j)), ele);
    					addError(_errors,error,3);
    				}
    				else if(_uncertainVars.contains(myUsed.get(j)))
    				{
    					//error  = new DetectedError("The variable «"+myUsed.get(j)+"» may not have been initialized!",(Element) _node.getElement(i));
    					error  = new DetectedError(errorMsg(Menu.error03_2,myUsed.get(j)), ele);
    					addError(_errors,error,3);
    				}
    			}

    		}
    		// START KGU#90 2015-11-28: Analysis ignored the internals of Forever loops and Parallel elements
    		else if (ele.getClass().getSimpleName().equals("Forever"))
    		{
    			analyse(((Forever) ele).q, _errors, _vars, _uncertainVars, _resultFlags);    			
    		}
    		else if (ele.getClass().getSimpleName().equals("Parallel"))
    		{
    			StringList initialVars = _vars.copy();
    			Iterator<Subqueue> iter = ((Parallel)ele).qs.iterator();
    			while (iter.hasNext())
    			{
    				// For the thread, propagate only variables known before the parallel section
    				StringList threadVars = initialVars.copy();
    				analyse(iter.next(), _errors, threadVars, _uncertainVars, _resultFlags);
    				// Any variable introduced by one of the threads will be known after all threads have terminated
    				_vars.addIfNew(threadVars);
    			}
    		}
    		// END KGU#90 2015-11-28
    		else if(ele.getClass().getSimpleName().equals("Alternative"))
    		{
    			StringList tVars = _vars.copy();
    			StringList fVars = _vars.copy();

    			analyse(((Alternative) ele).qTrue,_errors,tVars,_uncertainVars, _resultFlags);
    			analyse(((Alternative) ele).qFalse,_errors,fVars,_uncertainVars, _resultFlags);

    			for(int v = 0; v < tVars.count(); v++)
    			{
    				String varName = tVars.get(v);
    				if (fVars.contains(varName)) { _vars.addIfNew(varName); }
    				else if (!_vars.contains(varName)) { _uncertainVars.add(varName); }
    			}
    			for(int v = 0; v < fVars.count(); v++)
    			{
    				String varName = fVars.get(v);
    				if (tVars.contains(varName)) { _vars.addIfNew(varName); }
    				else if (!_vars.contains(varName)) { _uncertainVars.addIfNew(varName); }
    			}

    			// if a variable is not being initialised on both of the lists,
    			// it could be considered as not always being initialised
    			//
    			// => use a second list with variable that "may not have been initialised"
    		}
    		else if(ele.getClass().getSimpleName().equals("Case"))
    		{
    			Case caseEle = ((Case) ele);
    			StringList initialVars = _vars.copy();
    			// This Hashtable will contain strings composed of as many '1' characters as
    			// branches initialise the respective new variable - so in the end we can see
    			// which variables aren't always initialised.
    			Hashtable<String, String> myInitVars = new Hashtable<String, String>();
    			for (int j=0; j < caseEle.qs.size(); j++)
    			{
    				StringList caseVars = initialVars.copy();
    				analyse((Subqueue) caseEle.qs.get(j),_errors,caseVars,_uncertainVars,_resultFlags);
    				for(int v = 0; v<caseVars.count(); v++)
    				{
    					String varName = caseVars.get(v);
    					if(myInitVars.containsKey(varName))
    					{
    						myInitVars.put(varName, myInitVars.get(varName) + "1");
    					}
    					else
    					{
    						myInitVars.put(varName, "1");
    					}
    				}
    				//_vars.addIfNew(caseVars);
    			}
    			//System.out.println(myInitVars);
    			// walk trought the hashtable and check
    			Enumeration<String> keys = myInitVars.keys();
    			while ( keys.hasMoreElements() )
    			{
    				String key = keys.nextElement();
    				String value = myInitVars.get(key);

    				int si = caseEle.qs.size();	// Number of branches
    				// adapt size if no "default"
    				if ( caseEle.getText().get(caseEle.getText().count()-1).equals("%") )
    				{
    					si--;
    				}
    				//System.out.println("SI = "+si+" = "+c.text.get(c.text.count()-1));

    				if(value.length()==si)
    				{
    					_vars.addIfNew(key);
    				}
    				else
    				{
    					if(!_vars.contains(key))
    					{
    						_uncertainVars.addIfNew(key);
    					}
    				}
    			}
    			// look at the comment for the IF-structure
    		}
    	} // for(int i=0; i < _node.size(); i++)...
    }

    private boolean testidentifier(String _str)
    {
            boolean result = true;
            _str=_str.trim();
            if(_str.equals(""))
            {
                    result=false;
            }
            else
            {
            	if(
            			('a'<=_str.toLowerCase().charAt(0) && _str.toLowerCase().charAt(0)<='z')
            			||
            			(_str.toLowerCase().charAt(0)=='_')
            			)
            	{
            		if (_str.length()>1)
            		{
            			// START KGU 2015-11-25: This loop and condition were obviously wrong
            			//for(int i=0;i<_str.length();i++)
            			//{
            			//	if(!(
            			//			('a'<=_str.toLowerCase().charAt(0) && _str.toLowerCase().charAt(0)<='z')
            			//			||
            			//			('0'<=_str.charAt(0) && _str.charAt(0)<='9')
            			//			||
            			//			(_str.charAt(0)=='_')
            			//			))
            			String strLower = _str.toLowerCase();
            			for (int i = 1; i < _str.length(); i++)
            			{
            				if (!(
            						('a' <= strLower.charAt(i) && strLower.charAt(i) <= 'z')
            						||
            						('0' <= strLower.charAt(i) && strLower.charAt(i) <= '9')
            						||
            						(strLower.charAt(i) == '_')
            						))
            			// END KGU 2015-11-25
            				{
            					result = false;
            				}
            			}
            		}
            	}
            	else
            	{
            		result = false;
            	}

            }
            return result;
    }

    private void addError(Vector<DetectedError> errors, DetectedError error, int errorNo)
    {
            switch (errorNo)
            {
                    case 1:
                            if (Root.check1) errors.add(error);
                            break;
                    case 2:
                            if (Root.check2) errors.add(error);
                            break;
                    case 3:
                            if (Root.check3) errors.add(error);
                            break;
                    case 4:
                            if (Root.check4) errors.add(error);
                            break;
                    case 5:
                            if (Root.check5) errors.add(error);
                            break;
                    case 6:
                            if (Root.check6) errors.add(error);
                            break;
                    case 7:
                            if (Root.check7) errors.add(error);
                            break;
                    case 8:
                            if (Root.check8) errors.add(error);
                            break;
                    case 9:
                            if (Root.check9) errors.add(error);
                            break;
                    case 10:
                            if (Root.check10) errors.add(error);
                            break;
                    case 11:
                            if (Root.check11) errors.add(error);
                            break;
                    case 12:
                            if (Root.check12) errors.add(error);
                            break;
                    case 13:
                            if (Root.check13) errors.add(error);
                            break;
                    // START KGU#3 2015-11-03: New checks for enhanced FOR loop        
                    case 14:
                            if (Root.check14) errors.add(error);
                            break;
                    // END KGU#3 2015-11-03
                    // START KGU#2 2015-11-25: New checks for subroutine CALLs        
                    case 15:
                            if (Root.check15) errors.add(error);
                            break;
                    // END KGU#2 2015-11-25
                    // START KGU#78 2015-11-25: New checks for JUMP
                    case 16:
                            if (Root.check16) errors.add(error);
                            break;
                    // END KGU#78 2015-11-25
                    // START KGU#47 2015-11-28: New checks for PARALLEL
                    case 17:
                           if (Root.check17) errors.add(error);
                           break;
                    // END KGU#47 2015-11-28
                    default:
                            errors.add(error);
                            break;
            }
    }

    public StringList getParameterNames()
    {
    	// this.getVarNames();
    	// START KGU#2 2015-11-29
        //StringList vars = getVarNames(this,true,false);
        StringList vars = new StringList();
    	collectParameters(vars, null);
    	return vars;
    	// END KGU#2 2015-11-29 
    }

    // START KGU 2015-11-29
    public StringList getParameterTypes()
    {
    	StringList types = new StringList();
    	collectParameters(null, types);
    	return types;
    }
    // END KGU 2015-11-29
    
    public String getMethodName()
    {
    	String rootText = getText().getLongString();
    	int pos;

    	pos = rootText.indexOf("(");
    	if (pos!=-1) rootText=rootText.substring(0,pos);
    	pos = rootText.indexOf("[");	// FIXME: this might be part of a return type specification!
    	if (pos!=-1) rootText=rootText.substring(0,pos);
    	pos = rootText.indexOf(":");	// Omitted argument list?
    	if (pos!=-1) rootText=rootText.substring(0,pos);

    	String programName = rootText.trim();

    	// START KGU#2 2015-11-25: Type-specific handling:
    	// In case of a function, the last identifier will be the name, preceding ones may be type specifiers
    	// With a program, we just concatenate the strings by underscores
    	if (!isProgram)
    	{
    		String[] tokens = rootText.split(" ");
    		// It won't be that many strings, so we just go forward and keep the last acceptable one
    		for (int i = 0; i < tokens.length; i++)
    		{
    			if (testidentifier(tokens[i]))
    			{
    				programName = tokens[i];
    			}
    		}
    	}
    	// END KGU#2 2015-11-25
    	// START KGU 2015-10-16: Just in case...
    	programName = programName.replace(' ', '_');
    	// END KGU 2015-10-16

    	return programName;
    }
    
    // START KGU#78 2015-11-25: Extracted from analyse() and rewritten
    /**
     * Returns a string representing a detected result type if this is a subroutine diagram. 
     * @return null or a string possibly representing some datatype
     */
    public String getResultType()
    {
        // FIXME: This is not consistent to getMethodName()!
    	String resultType = null;
    	if (!this.isProgram)	// KGU 2015-12-20: Types more rigorously discarded if this is a program
    	{
    		String rootText = getText().getLongString();
    		StringList tokens = Element.splitLexically(rootText, true);
    		tokens.removeAll(" ");
    		int posOpenParenth = tokens.indexOf("(");
    		int posCloseParenth = tokens.indexOf(")");
    		int posColon = tokens.indexOf(":");
    		if (posOpenParenth >= 0 && posOpenParenth < posCloseParenth)
    		{
    			// First attempt: Something after parameter list and "as" or ":"
    			if (tokens.count() > posCloseParenth + 1 &&
    					(tokens.get(posCloseParenth + 1).toLowerCase().equals("as")) ||
    					(tokens.get(posCloseParenth + 1).equals(":"))
    					)
    			{
    				// START KGU#135 2016-01-08: It was not meant to be split to several lines.
    				//resultType = tokens.getText(posCloseParenth + 2);
    				resultType = tokens.concatenate(" ", posCloseParenth + 2);
    				// END KGU#135 2016-01-06
    			}
    			// Second attempt: A keyword sequence preceding the routine name
    			else if (posOpenParenth > 1 && testidentifier(tokens.get(posOpenParenth-1)))
    			{
    				// We assume that the last token is the procedure name, the previous strings
    				// may be the type
    				resultType = tokens.concatenate(" ", 0, posOpenParenth - 1);
    			}
    		}
    		else if (posColon != -1)
    		{
    			// Third attempt: In case of an omitted parenthesis, the part behind the colon may be the type 
    			resultType = tokens.concatenate(" ", posColon+1);
    		}
    	}
    	return resultType;
    }

    // Extracts parameter names and types from the parenthesis content of the Root text
    // and adds them synchronously to paramNames and paramTypes (if not null).
    public void collectParameters(StringList paramNames, StringList paramTypes)
    {
        if (!this.isProgram)
        {
        	try
        	{
        		String rootText = this.getText().getText();
        		rootText = rootText.replace("var ", "");
        		if(rootText.indexOf("(")>=0)
        		{
        			rootText=rootText.substring(rootText.indexOf("(")+1).trim();
        			rootText=rootText.substring(0,rootText.indexOf(")")).trim();
        		}

        		StringList params = StringList.explode(rootText,";");
        		for(int i = 0; i < params.count(); i++)
        		{
        			// common type for parameter group
        			String type = null;
        			String decl = params.get(i);
        			int posColon = decl.indexOf(":");
        			if (posColon >= 0)
        			{
        				type = decl.substring(posColon + 1).trim();
        				decl = decl.substring(0, posColon).trim();
        			}
        			else if ((posColon = decl.indexOf(" as ")) >= 0)
        			{
        				type = decl.substring(posColon + " as ".length()).trim();
        				decl = decl.substring(0, posColon).trim();
        			}
        			StringList vars = StringList.explode(decl,",");
        			for (int j=0; j < vars.count(); j++)
        			{
        				String varName = vars.get(j).trim();
        				if (!varName.isEmpty())
        				{
        					//System.out.println("Adding parameter: " + vars.get(j).trim());
        					if (paramNames != null)	paramNames.add(varName);
        					if (paramTypes != null)	paramTypes.add(type);
        				}
        			}
        		}
        	}
        	catch (Exception ex)
        	{
        		System.out.println(ex.getMessage());
        	}
        }
    	
    }
    // END KGU#78 2015-11-25

    public Vector<DetectedError> analyse()
    {
            this.getVarNames();

            Vector<DetectedError> errors = new Vector<DetectedError>();
            StringList vars = getVarNames(this,true,false);
            rootVars = vars.copy();
            StringList uncertainVars = new StringList();

            // START KGU 2015-11-25: This was practically what getMethodName() does
            //String rootText = getText().getLongString();
            //Regex r;
            
            //int pos;
            //
            //pos = rootText.indexOf("(");
            //if (pos!=-1) rootText=rootText.substring(0,pos);
            //pos = rootText.indexOf("[");
            //if (pos!=-1) rootText=rootText.substring(0,pos);
            //pos = rootText.indexOf(":");
            //if (pos!=-1) rootText=rootText.substring(0,pos);
            //
            //String programName = rootText.trim();
            String programName = getMethodName();
            // ENDGU 2015-11-25

            DetectedError error;

            // CHECK: uppercase for programname (#6)
            if(!programName.toUpperCase().equals(programName))
            {
                    //error  = new DetectedError("The programname «"+programName+"» must be written in uppercase!",this);
                    error  = new DetectedError(errorMsg(Menu.error06,programName),this);
                    addError(errors,error,6);
            }

            // CHECK: correct identifier for programname (#7)
            if(testidentifier(programName)==false)
            {
                    //error  = new DetectedError("«"+programName+"» is not a valid name for a program or function!",this);
                    error  = new DetectedError(errorMsg(Menu.error07_1,programName),this);
                    addError(errors,error,7);
            }

            // CHECK: two checks in one loop: (#12 - new!) & (#7)
            for(int j=0;j<vars.count();j++)
            {
                    // CHECK: non-conform parameter name (#12 - new!)
                    if( !(vars.get(j).charAt(0)=='p' && vars.get(j).substring(1).toUpperCase().equals(vars.get(j).substring(1))) )
                    {
                            //error  = new DetectedError("The parameter «"+vars.get(j)+"» must start with the letter \"p\" followed by only uppercase letters!",this);
                            error  = new DetectedError(errorMsg(Menu.error12,vars.get(j)),this);
                            addError(errors,error,12);
                    }

                    // CHECK: correct identifiers (#7)
                    if(testidentifier(vars.get(j))==false)
                    {
                            //error  = new DetectedError("«"+vars.get(j)+"» is not a valid name for a parameter!",this);
                            error  = new DetectedError(errorMsg(Menu.error07_2,vars.get(j)),this);
                            addError(errors,error,7);
                    }
            }


            // CHECK: the content of the diagram
            boolean[] resultFlags = {false, false, false};
            analyse(this.children,errors,vars,uncertainVars, resultFlags);

            // Test if we have a function (return value) or not
            // START KGU#78 2015-11-25: Delegated to a more general function
            //String first = this.getText().get(0).trim();
            //boolean haveFunction = first.toLowerCase().contains(") as ") || first.contains(") :") || first.contains("):");
            boolean haveFunction = getResultType() != null;
            // END KGU#78 2015-11-25

            // CHECK: var = programname (#9)
            if (!haveFunction && variables.contains(programName))
            {
                    //error  = new DetectedError("Your program («"+programName+"») may not have the same name as a variable!",this);
                    error  = new DetectedError(errorMsg(Menu.error09,programName),this);
                    addError(errors,error,9);
            }

            // CHECK: sub does not return any result (#13 - new!)
            // pre-requirement: we have a sub that returns something ...  FUNCTIONNAME () <return type>
            // check to see if
            // _ EITHER _
            // the name of the sub (proposed filename) is contained in the name of the assigned variablename
            // _ OR _
            // the list of initialized variables contains one of "RESULT", "Result", or "Result"
            // _ OR _
            // every path through the algorithm end with a return instruction (with expression!)
            if (haveFunction==true)
            {
            	// START KGU#78 2015-11-25: Let's first gather all necessary information
            	boolean setsResultCi = vars.contains("result", false);
//            	boolean setsResultLc = false, setsResultUc = false, setsResultWc = false;
//            	if (setsResultCi)
//            	{
//            		setsResultLc = vars.contains("result", true);
//            		setsResultUc = vars.contains("RESULT", true);
//            		setsResultWc = vars.contains("Result", true);
//            	}
            	boolean setsProcNameCi = vars.contains(programName,false);	// Why case-independent?
            	boolean maySetResultCi = uncertainVars.contains("result", false);
//            	boolean maySetResultLc = false, maySetResultUc = false, maySetResultWc = false;
//            	if (maySetResultCi)
//            	{
//            		maySetResultLc = uncertainVars.contains("result", true);
//            		maySetResultUc = uncertainVars.contains("RESULT", true);
//            		maySetResultWc = uncertainVars.contains("Result", true);
//            	}
            	boolean maySetProcNameCi = uncertainVars.contains(programName,false);	// Why case-independent?
            	// END KHU#78 2015-11-25
            	
            	if (!setsResultCi && !setsProcNameCi &&
            			!maySetResultCi && !setsProcNameCi)
            	{
            		//error  = new DetectedError("Your function does not return any result!",this);
            		error  = new DetectedError(errorMsg(Menu.error13_1,""),this);
            		addError(errors,error,13);
            	}
            	else if (!setsResultCi && !setsProcNameCi &&
            			(maySetResultCi || setsProcNameCi))
            	{
            		//error  = new DetectedError("Your function may not return a result!",this);
            		error  = new DetectedError(errorMsg(Menu.error13_2,""),this);
            		addError(errors,error,13);
            	}
            	// START KGU#78 2015-11-25: Check competitive approaches
            	else if (maySetResultCi && maySetProcNameCi)
            	{
            		//error  = new DetectedError("Your functions seems to use several competitive return mechanisms!",this);
            		error  = new DetectedError(errorMsg(Menu.error13_3,"RESULT <-> " + programName),this);
            		addError(errors,error,13);            		
            	}
            	// END KGU#78 2015-11-25
            }

            /*
            for(int i=0;i<errors.size();i++)
            {
                    System.out.println((DetectedError) errors.get(i));
            }
            /**/

            this.errors=errors;
            return errors;
    }

    public static void saveToINI()
    {
            try
            {
                    Ini ini = Ini.getInstance();
                    ini.load();
                    // analyser (see also Mainform.loadFromIni(), Diagram.analyserNSD()) 
                    ini.setProperty("check1",(check1?"1":"0"));
                    ini.setProperty("check2",(check2?"1":"0"));
                    ini.setProperty("check3",(check3?"1":"0"));
                    ini.setProperty("check4",(check4?"1":"0"));
                    ini.setProperty("check5",(check5?"1":"0"));
                    ini.setProperty("check6",(check6?"1":"0"));
                    ini.setProperty("check7",(check7?"1":"0"));
                    ini.setProperty("check8",(check8?"1":"0"));
                    ini.setProperty("check9",(check9?"1":"0"));
                    ini.setProperty("check10",(check10?"1":"0"));
                    ini.setProperty("check11",(check11?"1":"0"));
                    ini.setProperty("check12",(check12?"1":"0"));
                    ini.setProperty("check13",(check13?"1":"0"));
                    // START KGU#3 2015-11-03: New check for enhanced FOR loop
                    ini.setProperty("check14",(check14?"1":"0"));
                    // END KGU#3 2015-11-03
        			// START KGU#2/KGU#78 2015-11-28: New checks for CALL and JUMP elements
                    ini.setProperty("check15",(check15?"1":"0"));
                    ini.setProperty("check16",(check16?"1":"0"));
                    ini.setProperty("check17",(check17?"1":"0"));
        			// END KGU#2/KGU#78 2015-11-28

                    ini.save();
            }
            catch (Exception e)
            {
                    System.out.println(e);
            }
    }


    public boolean isSwitchTextAndComments() {
// START KGU#91 2015-12-04: Bugfix #39 drawing has directly to follow the set mode
//      return switchTextAndComments;
  	return Element.E_TOGGLETC;
// END KGU#91 2015-12-04
  }

// START KGU#91 2015-12-04: No longer needed
//  public void setSwitchTextAndComments(boolean switchTextAndComments) {
//      this.switchTextAndComments = switchTextAndComments;
//  }

	/**
	 * Returns the content of the text field unless _alwaysTrueText is false and
	 * mode isSwitchedTextAndComment is active, in which case the comment field
	 * is returned instead, if it is not empty.
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
    @Override
	public StringList getText(boolean _alwaysTrueText)
	{
		StringList textToShow = super.getText(_alwaysTrueText);
		if (textToShow.getText().trim().isEmpty())
		{
			textToShow = text;
		}
		return textToShow;
	}
// END KGU#91 2015-12-04
  
}

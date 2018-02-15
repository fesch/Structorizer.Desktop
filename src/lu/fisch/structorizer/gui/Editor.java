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
 *      Description:    This class represents the basic diagram editor.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------          ----            -----------
 *      Bob Fisch       2007.12.28      First Issue
 *      Kay Gürtzig     2015.10.12      control elements for breakpoint handling added (KGU#43). 
 *      Kay Gürtzig     2015.11.22      Adaptations for handling selected non-empty Subqueues (KGU#87)
 *      Kay Gürtzig     2016.01.04      Enh. #87: New buttons and menu items for collapsing/expanding elements
 *      Kay Gürtzig     2016.01.11      Enh. #103: Save button disabled while Root is unchanged (KGU#137)
 *      Kay Gürtzig     2016.01.21      Bugfix #114: Editing restrictions during execution (KGU#143)
 *      Kay Gürtzig     2016.01.22      Bugfix for Enh. #38 (addressing moveUp/moveDown, KGU#143 + KGU#144).
 *      Kay Gürtzig     2016.04.06      Enh. #158: Key bindings for cursor keys added (KGU#177)
 *      Kay Gürtzig     2016.04.14      Enh. #158: Key bindings for page keys added (KGU#177)
 *      Kay Gürtzig     2016.07.06      Enh. #188: New button and menu item for element conversion (KGU#199)
 *      Kay Gürtzig     2016.07.21      Enh. #197: Selection may be expanded by Shift-Up and Shift-Down (KGU#206)
 *      Kay Gürtzig     2016.08.02      Enh. #215: popupBreakTrigger added
 *      Kay Gürtzig     2016.10.13      Enh. #277: New toolbar button (+ context menu item) for disabling elements
 *      Kay Gürtzig     2016.11.17      Bugfix #114: Prerequisites for editing and transmutation during execution revised
 *      Kay Gürtzig     2016.11.22      Enh. #284: Key bindings for font resizing added (KGU#294)
 *      Kay Gürtzig     2016.12.12      Enh. #305: Scrollable list view of Roots in Arranger added
 *      Kay Gürtzig     2016.12.17      Enh. #305: Key binding <del> added to Arranger index list.
 *      Kay Gürtzig     2017.01.05      Enh. #319: Context menu for Arranger index
 *      Kay Gürtzig     2017.01.13      Bugfix #233: F6 and F8 had got kidnapped by the JSplitPanes sp and sp305
 *      Kay Gürtzig     2017.02.09      Enh. #344: Ctrl-Y as additional redo key binding
 *      Kay Gürtzig     2017.03.27      Enh. #380: New button/popup menu item to convert a sequence in a subroutine
 *      Kay Gürtzig     2017.03.28      Enh. #387: New "Save All" button
 *      Kay Gürtzig     2017.05.16      Enh. #389: Button for third diagram type (includable)
 *      Kay Gürtzig     2017.06.15      Enh. #415, #199: Toolbar additions for find & replace as well as help
 *      Kay Gürtzig     2017.11.05      Issue #452: Mechanisms for simplified toolbar (beginners' mode)
 *      Kay Gürtzig     2017.11.19      Bugfix: #468: action helpNSD had been associated to wrong toolbar button
 *      Kay Gürtzig     2018.02.12      Issues #4, #510: element toolbars merged, icon numbers modified
 *      Kay Gürtzig     2018.02.13      Issue #510: All "arrowed" element icons replaced by pure element icons
 *      Kay Gürtzig     2018.02.14      Issue #510: btnNice and its solitary toolbar disabled.
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import com.kobrix.notebook.gui.AKDockLayout;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.locales.LangPanel;

@SuppressWarnings("serial")
public class Editor extends LangPanel implements NSDController, ComponentListener
{
    // Controller
    NSDController NSDControll = null;

//    // Toolbars
//    protected MyToolbar toolbar = null;
	
    // Splitpanes
    JSplitPane sp;
    // START KGU#305 2016-12-12: Enh. #305 - add a diagram index for Arranger
    JSplitPane sp305;
    // END KGU#305 2016-12-12

    // lists
    DefaultListModel<DetectedError> errors = new DefaultListModel<DetectedError>();
    protected final JList<DetectedError> errorlist = new JList<DetectedError>(errors);
    // START KGU#305 2016-12-12: Enh. #305 - add a diagram index for Arranger
    DefaultListModel<Root> diagrams = new DefaultListModel<Root>();
    protected final JList<Root> diagramIndex = new JList<Root>(diagrams);
    // END KGU#305 2016-12-12

    // Panels
    public Diagram diagram = new Diagram(this, "???");
	
    // scrollpanes
    protected final JScrollPane scrollarea = new JScrollPane(diagram);
    protected final JScrollPane scrolllist = new JScrollPane(errorlist);
    // START KGU#305 2016-12-12: Enh. #305 - add a diagram index for Arranger
    protected final JScrollPane scrollIndex = new JScrollPane(diagramIndex);
    // END KGU#305 2016-12-12

    // Buttons
    // I/O
    protected final JButton btnNew = new JButton(IconLoader.getIcon(1)); 
    protected final JButton btnOpen = new JButton(IconLoader.getIcon(2)); 
    protected final JButton btnSave = new JButton(IconLoader.getIcon(3));
    // START KGU#373 2017-03-28: Enh. #387
    protected final JButton btnSaveAll = new JButton(IconLoader.getIcon(69));
    // END KGU#373 2017-03-38
    // START KGU493 2018-02-12: Issue #510 - toolbars Before and After merged
//    // InsertBefore
//    protected final JButton btnBeforeInst = new JButton(IconLoader.getIcon(7)); 
//    protected final JButton btnBeforeAlt = new JButton(IconLoader.getIcon(8)); 
//    protected final JButton btnBeforeFor = new JButton(IconLoader.getIcon(9)); 
//    protected final JButton btnBeforeWhile = new JButton(IconLoader.getIcon(10)); 
//    protected final JButton btnBeforeRepeat = new JButton(IconLoader.getIcon(11)); 
//    protected final JButton btnBeforeCall = new JButton(IconLoader.getIcon(49)); 
//    protected final JButton btnBeforeJump = new JButton(IconLoader.getIcon(56)); 
//    protected final JButton btnBeforeCase = new JButton(IconLoader.getIcon(47)); 
//    protected final JButton btnBeforeForever = new JButton(IconLoader.getIcon(9));
//    protected final JButton btnBeforePara = new JButton(IconLoader.getIcon(90));
    // InsertAfter
//    protected final JButton btnAfterInst = new JButton(IconLoader.getIcon(12)); 
//    protected final JButton btnAfterAlt = new JButton(IconLoader.getIcon(13)); 
//    protected final JButton btnAfterFor = new JButton(IconLoader.getIcon(14)); 
//    protected final JButton btnAfterWhile = new JButton(IconLoader.getIcon(15)); 
//    protected final JButton btnAfterRepeat = new JButton(IconLoader.getIcon(16)); 
//    protected final JButton btnAfterCall = new JButton(IconLoader.getIcon(50)); 
//    protected final JButton btnAfterJump = new JButton(IconLoader.getIcon(55)); 
//    protected final JButton btnAfterCase = new JButton(IconLoader.getIcon(48)); 
//    protected final JButton btnAfterForever = new JButton(IconLoader.getIcon(14));
//    protected final JButton btnAfterPara = new JButton(IconLoader.getIcon(89));
    protected final JButton btnAfterInst = new JButton(IconLoader.getIcon(57)); 
    protected final JButton btnAfterAlt = new JButton(IconLoader.getIcon(60)); 
    protected final JButton btnAfterFor = new JButton(IconLoader.getIcon(74)); 
    protected final JButton btnAfterWhile = new JButton(IconLoader.getIcon(62)); 
    protected final JButton btnAfterRepeat = new JButton(IconLoader.getIcon(63)); 
    protected final JButton btnAfterCall = new JButton(IconLoader.getIcon(58)); 
    protected final JButton btnAfterJump = new JButton(IconLoader.getIcon(59)); 
    protected final JButton btnAfterCase = new JButton(IconLoader.getIcon(64)); 
    protected final JButton btnAfterForever = new JButton(IconLoader.getIcon(61));
    protected final JButton btnAfterPara = new JButton(IconLoader.getIcon(91));
    // END KGU#493 2018-02-12
	// undo & redo
    protected final JButton btnUndo = new JButton(IconLoader.getIcon(39)); 
	protected final JButton btnRedo = new JButton(IconLoader.getIcon(38));
	// find & replace (KGU#324 2017-06-14: Enh. #415)
	protected final JButton btnFindReplace = new JButton(IconLoader.getIcon(73));
	// copy & paste
	protected final JButton btnCut = new JButton(IconLoader.getIcon(44)); 
    protected final JButton btnCopy = new JButton(IconLoader.getIcon(42)); 
    protected final JButton btnPaste = new JButton(IconLoader.getIcon(43));
	// style / type
	// START KGU#493 2018-02-14: Issue #510 Disabled as of little use here 
    //protected final JToggleButton btnNice = new JToggleButton(IconLoader.getIcon(40));
    // END KGU#493 2018-02-14
    protected final JToggleButton btnFunction = new JToggleButton(IconLoader.getIcon(21));
    protected final JToggleButton btnProgram = new JToggleButton(IconLoader.getIcon(22));
    // START KGU#376 2017-05-16: Enh. #389
    protected final JToggleButton btnInclude = new JToggleButton(IconLoader.getIcon(71));
    // END KGU#376 2017-05-16
	// editing
    protected final JButton btnEdit = new JButton(IconLoader.getIcon(6)); 
    protected final JButton btnDelete = new JButton(IconLoader.getIcon(5)); 
    protected final JButton btnMoveUp = new JButton(IconLoader.getIcon(19)); 
    protected final JButton btnMoveDown = new JButton(IconLoader.getIcon(20));
	// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
	protected final JButton btnTransmute = new JButton(IconLoader.getIcon(109));
	// END KGU#199 2016-07-06
	// START KGU#365 2017-03-27: Enh. #380 - conversion of sequence in a subroutine
	protected final JButton btnOutsource = new JButton(IconLoader.getIcon(68));
	// END KGU#365 2017-03-27
    // collapsing & expanding + disabling
    // START KGU#123 2016-01-04: Enh. #87 - Preparations for Fix #65
    protected final JButton btnCollapse = new JButton(IconLoader.getIcon(106)); 
    protected final JButton btnExpand = new JButton(IconLoader.getIcon(107));    
    // END KGU#123 2016-01-04
    // START KG#277 2016-10-13: Enh. #270
    protected final JButton btnDisable = new JButton(IconLoader.getIcon(26));
    // END KGU#277 2016-10-13
	// printing
    protected final JButton btnPrint = new JButton(IconLoader.getIcon(41));
    // START KGU#2 2015-11-19: Arranger launch added
    protected final JButton btnArrange = new JButton(IconLoader.getIcon(105));
    // END KGU#2 2015-11-19
	// font
    protected final JButton btnFontUp = new JButton(IconLoader.getIcon(33)); 
    protected final JButton btnFontDown = new JButton(IconLoader.getIcon(34));
	// copyright / help
    protected final JButton btnAbout = new JButton(IconLoader.getIcon(17));
	// START KGU#414 2017-06-14: Enh. #199
	protected final JButton btnHelp = new JButton(IconLoader.getIcon(110));
	// END KGU#414 2017-06-14
    // executing / testing
    protected final JButton btnMake = new JButton(IconLoader.getIcon(4));
    // START KGU#486 2018-02-06: Issue #4
    //protected final JButton btnTurtle = new JButton(IconLoader.turtle);
    protected final JButton btnTurtle = new JButton(IconLoader.getIcon(54));
    // END KGU#486 2018-02-06
    // START KGU 2015-10-12: Breakpoint wiping
    protected final JButton btnDropBrk = new JButton(IconLoader.getIcon(104));
    // END KGU 2015-10-12
	// colors
    protected ColorButton btnColor0 = new ColorButton(Element.color0);
    protected ColorButton btnColor1 = new ColorButton(Element.color1);
    protected ColorButton btnColor2 = new ColorButton(Element.color2);
    protected ColorButton btnColor3 = new ColorButton(Element.color3);
    protected ColorButton btnColor4 = new ColorButton(Element.color4);
    protected ColorButton btnColor5 = new ColorButton(Element.color5);
    protected ColorButton btnColor6 = new ColorButton(Element.color6);
    protected ColorButton btnColor7 = new ColorButton(Element.color7);
    protected ColorButton btnColor8 = new ColorButton(Element.color8);
    protected ColorButton btnColor9 = new ColorButton(Element.color9);
	
	
    // Popup menu
    protected final JPopupMenu popup = new JPopupMenu();
    protected final JMenuItem popupCut = new JMenuItem("Cut",IconLoader.getIcon(44));
    protected final JMenuItem popupCopy = new JMenuItem("Copy",IconLoader.getIcon(42));
    protected final JMenuItem popupPaste = new JMenuItem("Paste",IconLoader.getIcon(43));
    protected final JMenu popupAdd = new JMenu("Add");
    // Submenu of "Add"
    protected final JMenu popupAddBefore = new JMenu("Before");
    // Submenus of "Add -> Before"
    protected final JMenuItem popupAddBeforeInst = new JMenuItem("Instruction",IconLoader.getIcon(/*7*/57));
    protected final JMenuItem popupAddBeforeAlt = new JMenuItem("IF statement",IconLoader.getIcon(/*8*/60));
    protected final JMenuItem popupAddBeforeCase = new JMenuItem("CASE statement",IconLoader.getIcon(/*47*/64));
    // START KGU#493 2018-02-12: Issue #4 - distinguishable FOR symbol
    //protected final JMenuItem popupAddBeforeFor = new JMenuItem("FOR loop",IconLoader.getIcon(9));
    protected final JMenuItem popupAddBeforeFor = new JMenuItem("FOR loop",IconLoader.getIcon(/*95*/74));
    // END KGU#493 2018-002-12
    protected final JMenuItem popupAddBeforeWhile = new JMenuItem("WHILE loop",IconLoader.getIcon(/*10*/62));
    protected final JMenuItem popupAddBeforeRepeat = new JMenuItem("REPEAT loop",IconLoader.getIcon(/*11*/63));
    protected final JMenuItem popupAddBeforeForever = new JMenuItem("ENDLESS loop",IconLoader.getIcon(/*9*/61));
    protected final JMenuItem popupAddBeforeCall = new JMenuItem("Call",IconLoader.getIcon(/*49*/58));
    protected final JMenuItem popupAddBeforeJump = new JMenuItem("Jump",IconLoader.getIcon(/*56*/59));
    protected final JMenuItem popupAddBeforePara = new JMenuItem("Parallel",IconLoader.getIcon(/*90*/91));

    protected final JMenu popupAddAfter = new JMenu("After");
    // Submenus of "Add -> After"
    protected final JMenuItem popupAddAfterInst = new JMenuItem("Instruction",IconLoader.getIcon(/*12*/57));
    protected final JMenuItem popupAddAfterAlt = new JMenuItem("IF statement",IconLoader.getIcon(/*13*/60));
    protected final JMenuItem popupAddAfterCase = new JMenuItem("CASE statement",IconLoader.getIcon(/*48*/64));
    // START KGU#493 2018-02-12: Issue #4 - distinguishable FOR symbol
    //protected final JMenuItem popupAddAfterFor = new JMenuItem("FOR loop",IconLoader.getIcon(14));
    protected final JMenuItem popupAddAfterFor = new JMenuItem("FOR loop",IconLoader.getIcon(/*97*/74));
    // END KGU#493 2018-002-12
    protected final JMenuItem popupAddAfterWhile = new JMenuItem("WHILE loop",IconLoader.getIcon(/*15*/62));
    protected final JMenuItem popupAddAfterRepeat = new JMenuItem("REPEAT loop",IconLoader.getIcon(/*16*/63));
    protected final JMenuItem popupAddAfterCall = new JMenuItem("Call",IconLoader.getIcon(/*50*/58));
    protected final JMenuItem popupAddAfterJump = new JMenuItem("Jump",IconLoader.getIcon(/*55*/59));
    protected final JMenuItem popupAddAfterForever = new JMenuItem("ENDLESS loop",IconLoader.getIcon(/*14*/61));
    protected final JMenuItem popupAddAfterPara = new JMenuItem("Parallel",IconLoader.getIcon(/*89*/91));

    protected final JMenuItem popupEdit = new JMenuItem("Edit",IconLoader.getIcon(6));
    protected final JMenuItem popupDelete = new JMenuItem("Delete",IconLoader.getIcon(5));
    protected final JMenuItem popupMoveUp = new JMenuItem("Move up",IconLoader.getIcon(19));
    protected final JMenuItem popupMoveDown = new JMenuItem("Move down",IconLoader.getIcon(20));
	// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
	protected final JMenuItem popupTransmute = new JMenuItem("Transmute", IconLoader.getIcon(109));
	// END KGU#199 2016-07-06
	// START KGU#365 2017-03-23: Enh. #380 - conversion of sequence in a subroutine
	protected final JMenuItem popupOutsource = new JMenuItem("Outsource", IconLoader.getIcon(68));
	// END KGU#365 2017-03-23
    // START KGU#123 2016-01-04: Enh. #87 - Preparations for Fix #65
    protected final JMenuItem popupCollapse = new JMenuItem("Collapse", IconLoader.getIcon(106)); 
    protected final JMenuItem popupExpand = new JMenuItem("Expand", IconLoader.getIcon(107));    
    // END KGU#123 2016-01-04
    // START KG#277 2016-10-13: Enh. #270
    protected final JMenuItem popupDisable = new JMenuItem("Disable", IconLoader.getIcon(26));
    // END KGU#277 2016-10-13
    // START KGU#43 2015-10-12: Breakpoint toggle
    protected final JMenuItem popupBreakpoint = new JMenuItem("Toggle Breakpoint", IconLoader.getIcon(103));
    // END KGU#43 2015-10-12
	// START KGU#213 2016-08-02: Enh. #215
	protected final JMenuItem popupBreakTrigger = new JMenuItem("Specify break trigger...", IconLoader.getIcon(112));
	// END KGU#143 2016-08-02
	
	// START KGU#318 2017-01-05: Enh. #319 - context menu for the Arranger index
    protected final JPopupMenu popupIndex = new JPopupMenu();
    protected final JMenuItem popupIndexGet = new JMenuItem("Get diagram", IconLoader.getIcon(0));
    protected final JMenuItem popupIndexSave = new JMenuItem("Save changes", IconLoader.getIcon(3));
    protected final JMenuItem popupIndexRemove = new JMenuItem("Remove", IconLoader.getIcon(45));
    protected final JMenuItem popupIndexCovered = new JMenuItem("Test-covered on/off", IconLoader.getIcon(46));
	// END KGU#318 2017-01-05
    
    // START KGU#177 2016-04-06: Enh. #158
    // Action names
    public enum CursorMoveDirection { CMD_UP, CMD_DOWN, CMD_LEFT, CMD_RIGHT };
    private class SelectionMoveAction extends AbstractAction
    {
    	Diagram diagram;	// The object responsible for executing the action
    	
    	SelectionMoveAction(Diagram _diagram, CursorMoveDirection _dir)
    	{
    		super(_dir.name());
    		diagram = _diagram;
    	}
    	
		@Override
		public void actionPerformed(ActionEvent ev) {
			diagram.moveSelection(CursorMoveDirection.valueOf(getValue(AbstractAction.NAME).toString()));
		}
    	
    }
    // END KGU#177 2016-04-06
    // START KGU#206 2016-07-21: Enh. #158, #197
    public enum SelectionExpandDirection { EXPAND_UP, EXPAND_DOWN };
    private class SelectionExpandAction extends AbstractAction
    {
    	Diagram diagram;	// The object responsible for executing the action
    	
    	SelectionExpandAction(Diagram _diagram, SelectionExpandDirection _dir)
    	{
    		super(_dir.name());
    		diagram = _diagram;
    	}
    	
		@Override
		public void actionPerformed(ActionEvent ev) {
			diagram.expandSelection(SelectionExpandDirection.valueOf(getValue(AbstractAction.NAME).toString()));
		}
    	
    }
    // END KGU#206 2016-07-21
    // START KGU#177 2016-04-14: Enh. #158
    private class PageScrollAction extends AbstractAction
    {
    	private JScrollBar vScrollBar;
    	private boolean up;
    	
    	PageScrollAction(JScrollBar vScrBar, boolean pageUp, String key)
    	{
    		super(key);
    		vScrollBar = vScrBar;
    		up = pageUp; 		
    	}

		@Override
		public void actionPerformed(ActionEvent ev) {
			// TODO Auto-generated method stub
			int value = vScrollBar.getValue();
			int incr = vScrollBar.getBlockIncrement(up ? -1 : 1);
			vScrollBar.setValue(value + (up ? -incr : incr));
		}
    	
    }
    // END KGU#177 2016-04-14
    // START KGU#294 2016-11-22: Issue #284 Unification of font resizing key bindings
    private class FontResizeAction extends AbstractAction
    {
    	Diagram diagram;	// The object responsible for executing the action
    	
    	FontResizeAction(Diagram _diagram, String _key)
    	{
    		super(_key);
    		diagram = _diagram;
    	}
    	
		@Override
		public void actionPerformed(ActionEvent ev) {
			if (getValue(AbstractAction.NAME).equals("FONT_UP")) {
				diagram.fontUpNSD();
			}
			else {
				diagram.fontDownNSD();	
			}
		}
    }
    // END KGU#294 2016-11-22
    // START KGU#305 2016-12-15: Enh. #305 - diagramIndex should react to keys
    private class ArrangerIndexAction extends AbstractAction
    {
    	
    	ArrangerIndexAction(boolean isDoubleClick)
    	{
    		super(isDoubleClick ? "DOUBLE_CLICK" : "SINGLE_CLCK");
    	}
    	
		// START KGU#305 2016-12-17: Also allow to remove a diagram from Arranger
    	ArrangerIndexAction(String keyString)
    	{
    		super(keyString);
    	}
		// END KGU#305 2016-12-17
    	
		@Override
		public void actionPerformed(ActionEvent ev) {
			if (getValue(AbstractAction.NAME).equals("SINGLE_CLICK")) {
				Arranger.scrollToDiagram(diagramIndex.getSelectedValue(), true);
			}
			// START KGU#305 2016-12-17: Also allow to remove a diagram from Arranger
			//else {
			else if (getValue(AbstractAction.NAME).equals("DOUBLE_CLICK")) {
			// END KGU#305 2016-12-17
				arrangerIndexGet();
			}
			// START KGU#305 2016-12-17: Also allow to remove a diagram from Arranger
			else if (getValue(AbstractAction.NAME).equals("DELETE") && Arranger.hasInstance()) {
				arrangerIndexRemove();
			}
			// END KGU#305 2016-12-17
		}
    }    
    // END KGGU#305 2016-12-15
    
    private MyToolbar newToolBar(String name, boolean indispensable)
    {
        MyToolbar toolbar = new MyToolbar();
        toolbar.setName(name);
        diagram.toolbars.add(toolbar);
        // START KGU#456 2017-11-05: Enh. #452
        if (!indispensable) {
        	diagram.expertToolbars.add(toolbar);
        	if (Element.E_REDUCED_TOOLBARS) {
        		toolbar.setVisible(false);
        	}
        }
        // END KGU#456 2017-11-05
        this.add(toolbar, AKDockLayout.NORTH);
        toolbar.setFloatable(true);
        toolbar.setRollover(true);
        //toolbar.addSeparator();
        return toolbar;
    }

    private void create()
    {
    	MyToolbar toolbar = null;
    	
        // Setting up "this" ;-)
        addComponentListener(this);
        this.setDoubleBuffered(false);

        // Setting up the popup-menu with all submenus and shortcuts and actions
        popup.add(popupCut);
        popupCut.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.cutNSD(); doButtons(); } } );

        popup.add(popupCopy);
        popupCopy.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyNSD(); doButtons(); } } );

        popup.add(popupPaste);
        popupPaste.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.pasteNSD(); doButtons(); } } );

        popup.addSeparator();

        popup.add(popupAdd);
        popupAdd.setIcon(IconLoader.getIcon(18));

        popupAdd.add(popupAddBefore);
        popupAddBefore.setIcon(IconLoader.getIcon(19));

        popupAddBefore.add(popupAddBeforeInst);
        popupAddBeforeInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeAlt);
        popupAddBeforeAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeCase);
        popupAddBeforeCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeFor);
        popupAddBeforeFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeWhile);
        popupAddBeforeWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeRepeat);
        popupAddBeforeRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeForever);
        popupAddBeforeForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeCall);
        popupAddBeforeCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeJump);
        popupAddBeforeJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforePara);
        popupAddBeforePara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",false); doButtons(); } } );

        popupAdd.add(popupAddAfter);
        popupAddAfter.setIcon(IconLoader.getIcon(20));

        popupAddAfter.add(popupAddAfterInst);
        popupAddAfterInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterAlt);
        popupAddAfterAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterCase);
        popupAddAfterCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterFor);
        popupAddAfterFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterWhile);
        popupAddAfterWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterRepeat);
        popupAddAfterRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterForever);
        popupAddAfterForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterCall);
        popupAddAfterCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterJump);
        popupAddAfterJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterPara);
        popupAddAfterPara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",true); doButtons(); } } );

        popup.add(popupEdit);
        popupEdit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editNSD(); doButtons(); } } );

        popup.add(popupDelete);
        popupDelete.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.deleteNSD(); doButtons(); } } );

        popup.addSeparator();

        popup.add(popupMoveUp);
        popupMoveUp.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.moveUpNSD(); doButtons(); } } );

        popup.add(popupMoveDown);
        popupMoveDown.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.moveDownNSD(); doButtons(); } } );
        
		// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
		popup.add(popupTransmute);
		popupTransmute.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.transmuteNSD(); doButtons(); } } );
		// END KGU#199 2016-07-06

		// START KGU#365 2017-03-27: Enh. #380 - conversion of sequence in a subroutine
		popup.add(popupOutsource);
		popupOutsource.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.outsourceNSD(); doButtons(); } } );;
		// END KGU#365 2017-03-27

        // START KGU#123 2016-01-03: Enh. #87 - New menu items (addressing Bug #65)
		popup.addSeparator();

		popup.add(popupCollapse);
		popupCollapse.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.collapseNSD(); doButtons(); } } );

		popup.add(popupExpand);
		popupExpand.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.expandNSD(); doButtons(); } } );
		// END KGU#123 2016-01-03

		// START KGU#277 2016-10-13: Enh. #270
		popup.add(popupDisable);
		popupDisable.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.disableNSD(); doButtons(); } } );
		// END KGU#123 2016-10-13

		// START KGU#43 2015-10-12 Add a possibility to set or unset a checkpoint on the selected Element
        popup.addSeparator();

        popup.add(popupBreakpoint);
        popupBreakpoint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleBreakpoint(); doButtons(); } }); 
        // END KGU#43 2015-10-12

		// START KGU#213 2016-08-02: Enh. #215 - new breakpoint feature
		popup.add(popupBreakTrigger);
        popupBreakTrigger.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editBreakTrigger(); doButtons(); } }); 
		// END KGU#213 2016-08-02

    	// START KGU#318 2017-01-05: Enh. #319 - context menu for the Arranger index
        popupIndex.add(popupIndexGet);
        popupIndexGet.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) {	arrangerIndexGet();	} });
        
        popupIndex.add(popupIndexSave);
        popupIndexSave.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexSave(); } });

        popupIndex.add(popupIndexRemove);
        popupIndexRemove.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexRemove(); } });

        popupIndex.add(popupIndexCovered);
        popupIndexCovered.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { arrangerIndexToggleCovered(); } });
        // END KGU#318 2017-01-05

        // add toolbars
        //toolbar.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        this.setLayout(new AKDockLayout());
        // FIXME (KGU): Why is the static variable holding MyToolbar overwritten?
        toolbar = newToolBar("New, open, save", true);
		
        // Setting up the toolbar with all buttons and actions
		// I/O
		//toolbar.addSeparator();
        toolbar.add(btnNew);
		btnNew.setFocusable(false);
		btnNew.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.newNSD(); doButtons(); } } );
        toolbar.add(btnOpen);
		btnOpen.setFocusable(false);
		btnOpen.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.openNSD(); doButtons(); } } );
        toolbar.add(btnSave);
		btnSave.setFocusable(false);
		btnSave.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.saveNSD(false); doButtons(); } } );
	    // START KGU#373 2017-03-28: Enh. #387
        toolbar.add(btnSaveAll, false);
		btnSaveAll.setFocusable(false);
		btnSaveAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.saveAllNSD(); doButtons(); } } );
	    // END KGU#373 2017-03-38
		
		toolbar = newToolBar("Print", true);

		// printing
		//toolbar.addSeparator();
        toolbar.add(btnPrint);
		btnPrint.setFocusable(false);
		btnPrint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.printNSD(); doButtons(); } } );
		// START KGU#2 2015-11-24: Arranger launcher (now action correctly attached)
        toolbar.add(btnArrange, false);
        btnArrange.setToolTipText("Add the diagram to the Arranger panel");
		btnArrange.setFocusable(false);
		btnArrange.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.arrangeNSD(); doButtons(); } } );
		// END KGU#2 2015-11-24
		
		toolbar = newToolBar("Undo, redo", true);

		// undo & redo
		//toolbar.addSeparator();
        toolbar.add(btnUndo);
		btnUndo.setFocusable(false);
		btnUndo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.undoNSD(); doButtons(); } } );
        toolbar.add(btnRedo);
		btnRedo.setFocusable(false);
		btnRedo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.redoNSD(); doButtons(); } } );
		
        // START KGU#324 2017-06-14: Enh. #415
		toolbar = newToolBar("Find & Replace", false);
		
		// find & replace
		toolbar.add(btnFindReplace);
		btnFindReplace.setFocusable(false);
		btnFindReplace.addActionListener(new ActionListener() {	public void actionPerformed(ActionEvent arg0) { diagram.findAndReplaceNSD(); } });
		// END KGU#324 2017-06-14
		
		toolbar = newToolBar("Copy, cut, paste", true);

		// copy & paste
		//toolbar.addSeparator();
        toolbar.add(btnCut);
		btnCut.setFocusable(false);
		btnCut.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.cutNSD(); doButtons(); } } );
        toolbar.add(btnCopy);
		btnCopy.setFocusable(false);
		btnCopy.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyNSD(); doButtons(); } } );
        toolbar.add(btnPaste);
		btnPaste.setFocusable(false);
		btnPaste.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.pasteNSD(); doButtons(); } } );
		
		toolbar = newToolBar("Edit, delete, move", true);

		// editing
		//toolbar.addSeparator();
        toolbar.add(btnEdit);
		btnEdit.setFocusable(false);
		btnEdit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editNSD(); doButtons(); } } );
        toolbar.add(btnDelete);
		btnDelete.setFocusable(false);
		btnDelete.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.deleteNSD(); doButtons(); } } );
        toolbar.add(btnMoveUp);
		btnMoveUp.setFocusable(false);
		btnMoveUp.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.moveUpNSD(); doButtons(); } } );
        toolbar.add(btnMoveDown);
		btnMoveDown.setFocusable(false);
		btnMoveDown.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.moveDownNSD(); doButtons(); } } );
		// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
        toolbar.add(btnTransmute, false);
        btnTransmute.setFocusable(false);
		btnTransmute.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.transmuteNSD(); doButtons(); } } ); 
		// END KGU#199 2016-07-06
		// START KGU#365 2017-03-27: Enh. #380 - We allow subroutine generation
        toolbar.add(btnOutsource, false);
        btnOutsource.setFocusable(false);
		btnOutsource.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.outsourceNSD(); doButtons(); } } ); 
		// END KGU#199 2016-07-06
		
		toolbar = newToolBar("Method, program", false);

		// style
		//toolbar.addSeparator();
        toolbar.add(btnProgram);
		btnProgram.setFocusable(false);
		btnProgram.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setProgram(); doButtons(); } } );
        toolbar.add(btnFunction);
		btnFunction.setFocusable(false);
		btnFunction.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setFunction(); doButtons(); } } );
	    // START KGU#376 2017-05-16: Enh. #389
        toolbar.add(btnInclude);
		btnInclude.setFocusable(false);
		btnInclude.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setInclude(); doButtons(); } } );
	    // END KGU#376 2017-05-16

		// START KGU#493 2018-02-14: Issue #510 Disabled as of little use here 
//		toolbar = newToolBar("Nice", false);
//		
//		//toolbar.addSeparator();
//        toolbar.add(btnNice);
//		btnNice.setFocusable(false);
//		btnNice.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setNice(btnNice.isSelected()); doButtons(); } } );
		// END KGU#493 2018-02-14

		// START KGU#493 2018-02-12: Issue #510 - toolbars Before + After merged
//		toolbar = newToolBar("Add before ...", false);
//		//toolbar.setOrientation(JToolBar.VERTICAL);
//		//this.add(toolbar,BorderLayout.WEST);
//		
//		// IsertBefore
//		//toolbar.addSeparator();
//        toolbar.add(btnBeforeInst);
//		btnBeforeInst.setFocusable(false);
//		btnBeforeInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",false); doButtons(); } } );
//        toolbar.add(btnBeforeAlt);
//		btnBeforeAlt.setFocusable(false);
//		btnBeforeAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,false); doButtons(); } } );
//        toolbar.add(btnBeforeCase);
//		btnBeforeCase.setFocusable(false);
//		btnBeforeCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,false); doButtons(); } } );
//        toolbar.add(btnBeforeFor);
//		btnBeforeFor.setFocusable(false);
//		btnBeforeFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,false); doButtons(); } } );
//        toolbar.add(btnBeforeWhile);
//		btnBeforeWhile.setFocusable(false);
//		btnBeforeWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,false); doButtons(); } } );
//        toolbar.add(btnBeforeRepeat);
//		btnBeforeRepeat.setFocusable(false);
//		btnBeforeRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,false); doButtons(); } } );
//        toolbar.add(btnBeforeForever, false);
//		btnBeforeForever.setFocusable(false);
//		btnBeforeForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",false); doButtons(); } } );
//        toolbar.add(btnBeforeCall);
//		btnBeforeCall.setFocusable(false);
//		btnBeforeCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",false); doButtons(); } } );
//        toolbar.add(btnBeforeJump, false);
//		btnBeforeJump.setFocusable(false);
//		btnBeforeJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",false); doButtons(); } } );
//        toolbar.add(btnBeforePara, false);
//		btnBeforePara.setFocusable(false);
//		btnBeforePara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",false); doButtons(); } } );
		// END KGU#493 2018-02-12

		toolbar = newToolBar("Add after ...", true);
		//toolbar.setOrientation(JToolBar.VERTICAL);
		//this.add(toolbar,BorderLayout.WEST);

		// IsertAfter
		//toolbar.addSeparator();
        toolbar.add(btnAfterInst);
		btnAfterInst.setFocusable(false);
		btnAfterInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","", (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0 ); doButtons(); } } );
        toolbar.add(btnAfterAlt);
		btnAfterAlt.setFocusable(false);
		btnAfterAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt, (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
        toolbar.add(btnAfterCase);
		btnAfterCase.setFocusable(false);
		btnAfterCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase, (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
        toolbar.add(btnAfterFor);
		btnAfterFor.setFocusable(false);
		btnAfterFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor, (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
        toolbar.add(btnAfterWhile);
		btnAfterWhile.setFocusable(false);
		btnAfterWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile, (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
        toolbar.add(btnAfterRepeat);
		btnAfterRepeat.setFocusable(false);
		btnAfterRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat, (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
        toolbar.add(btnAfterForever, false);
		btnAfterForever.setFocusable(false);
		btnAfterForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","", (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
        toolbar.add(btnAfterCall);
		btnAfterCall.setFocusable(false);
		btnAfterCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","", (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
        toolbar.add(btnAfterJump, false);
		btnAfterJump.setFocusable(false);
		btnAfterJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","", (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
        toolbar.add(btnAfterPara, false);
		btnAfterPara.setFocusable(false);
		btnAfterPara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","", (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
		
		toolbar = newToolBar("Colors ...", false);

		// Colors
		//toolbar.addSeparator();
        toolbar.add(btnColor0);
        toolbar.add(btnColor1);
        toolbar.add(btnColor2);
        toolbar.add(btnColor3);
        toolbar.add(btnColor4);
        toolbar.add(btnColor5);
        toolbar.add(btnColor6);
        toolbar.add(btnColor7);
        toolbar.add(btnColor8);
        toolbar.add(btnColor9);
		btnColor0.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor0.getColor()); doButtons(); } } );
		btnColor1.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor1.getColor()); doButtons(); } } );
		btnColor2.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor2.getColor()); doButtons(); } } );
		btnColor3.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor3.getColor()); doButtons(); } } );
		btnColor4.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor4.getColor()); doButtons(); } } );
		btnColor5.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor5.getColor()); doButtons(); } } );
		btnColor6.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor6.getColor()); doButtons(); } } );
		btnColor7.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor7.getColor()); doButtons(); } } );
		btnColor8.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor8.getColor()); doButtons(); } } );
		btnColor9.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setColor(btnColor9.getColor()); doButtons(); } } );
		btnColor0.setFocusable(false);
		btnColor1.setFocusable(false);
		btnColor2.setFocusable(false);
		btnColor3.setFocusable(false);
		btnColor4.setFocusable(false);
		btnColor5.setFocusable(false);
		btnColor6.setFocusable(false);
		btnColor7.setFocusable(false);
		btnColor8.setFocusable(false);
		btnColor9.setFocusable(false);

		// START KGU#123 2016-01-04: Enh. #87 - Preparation for fix #65
		toolbar = newToolBar("Collapsing", false);

		// Collapse & Expand
		//toolbar.addSeparator();
        toolbar.add(btnCollapse);
		btnCollapse.setFocusable(false);
		btnCollapse.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.collapseNSD(); } } );
        toolbar.add(btnExpand);
		btnExpand.setFocusable(false);
		btnExpand.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.expandNSD(); } } );
		// END KGU#123 2016-01-04
		// START KGU#277 2016-10-13: Enh. #270 / KGU#310 2016-12-14: moved to "Turtle, interprete" toolbar
//        toolbar.add(btnDisable);
//		btnDisable.setFocusable(false);
//		btnDisable.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.disableNSD(); } } );
		// END KGU#123 2016-01-04

		toolbar = newToolBar("Debug", true);

		// Turtle
		//toolbar.addSeparator();
        toolbar.add(btnTurtle);
		btnTurtle.setFocusable(false);
		btnTurtle.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.goTurtle(); } } );
        toolbar.add(btnMake);
		btnMake.setFocusable(false);
		btnMake.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.goRun(); } } );
        toolbar.add(btnDropBrk, false);
		btnDropBrk.setFocusable(false);
		btnDropBrk.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.clearBreakpoints(); } } );
		// START KGU#310 2016-12-14: Moved hitherto from toolbar "Collapsing"
        toolbar.add(btnDisable, false);
		btnDisable.setFocusable(false);
		btnDisable.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.disableNSD(); } } );
		// END KGU#310 2016-12-14

		toolbar = newToolBar("Font ...", true);

		// Font
		//toolbar.addSeparator();
        toolbar.add(btnFontUp);
		btnFontUp.setFocusable(false);
		btnFontUp.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.fontUpNSD(); doButtons(); } } );
        toolbar.add(btnFontDown);
		btnFontDown.setFocusable(false);
		btnFontDown.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.fontDownNSD(); doButtons(); } } );
		
		toolbar = newToolBar("About, help", true);

		// About
		//toolbar.addSeparator();
        toolbar.add(btnAbout);
		btnAbout.setFocusable(false);
		btnAbout.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.aboutNSD(); } } );
		// START KGU#414 2017-06-14: Enh. #199
		toolbar.add(btnHelp);
		btnHelp.setFocusable(false);
		// START KGU#462 2017-11-19: Issues #199, #468
		//btnAbout.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.helpNSD(); } } );
		btnHelp.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.helpNSD(); } } );
		// END KGU#462 2017-11-19
		// END KGU#414 2017-06-14

		sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.add(sp,AKDockLayout.CENTER);
		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.setResizeWeight(0.99);
		sp.setDividerSize(5);
		
		// add panels
		// get container object
		//Container container = this;
        //container.add(scrollarea,AKDockLayout.CENTER);
		
		// START KGU#305 2016-12-12: Enh, #305
		//sp.add(scrollarea);
		sp305 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		sp.add(sp305);
		sp305.setBorder(BorderFactory.createEmptyBorder());
		sp305.setResizeWeight(0.99);
		sp305.setDividerSize(5);
		sp305.add(scrollarea);
		// END KGU#305 2016-12-12
		
        scrollarea.setBackground(Color.LIGHT_GRAY);
		scrollarea.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
		scrollarea.setWheelScrollingEnabled(true);
		scrollarea.setDoubleBuffered(true);
		scrollarea.setBorder(BorderFactory.createEmptyBorder());
		scrollarea.setViewportView(diagram);
		scrollarea.setFocusable(true);
		// START KGU#177 2016-04-06 Enh. #158 Moving selection by cursor keys
		InputMap inpMap = scrollarea.getInputMap(WHEN_FOCUSED);
		ActionMap actMap = scrollarea.getActionMap();
		// Add key bindings to the Diagram
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), CursorMoveDirection.CMD_UP);
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), CursorMoveDirection.CMD_DOWN);
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), CursorMoveDirection.CMD_LEFT);
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), CursorMoveDirection.CMD_RIGHT);
		// START KGU#206 2016-07-21: Enh. #197
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK ), SelectionExpandDirection.EXPAND_UP);
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK ), SelectionExpandDirection.EXPAND_DOWN);
		// END KGU#206 2016-07-21
	    // START KGU#177 2016-04-14: Enh. #158
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "PAGE_DOWN");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "PAGE_UP");
		// END KGU#177 2016-04-16
	    // START KGU#294 2016-11-22: Enh. #284
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK), "FONT_UP");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK), "FONT_DOWN");
		// END KGU#294 2016-11-22
		// START KGU#347 2017-02-09: Enh. #344 additional key binding for redo
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "REDO");
		// END KGU#347 2017-02-09
		actMap.put(CursorMoveDirection.CMD_UP, new SelectionMoveAction(diagram, CursorMoveDirection.CMD_UP));
		actMap.put(CursorMoveDirection.CMD_DOWN, new SelectionMoveAction(diagram, CursorMoveDirection.CMD_DOWN));
		actMap.put(CursorMoveDirection.CMD_LEFT, new SelectionMoveAction(diagram, CursorMoveDirection.CMD_LEFT));
		actMap.put(CursorMoveDirection.CMD_RIGHT, new SelectionMoveAction(diagram, CursorMoveDirection.CMD_RIGHT));
		// END KGU#177 2016-04.-06
		// START KGU#206 2016-07-21: Enh. #197
		actMap.put(SelectionExpandDirection.EXPAND_UP, new SelectionExpandAction(diagram, SelectionExpandDirection.EXPAND_UP));
		actMap.put(SelectionExpandDirection.EXPAND_DOWN, new SelectionExpandAction(diagram, SelectionExpandDirection.EXPAND_DOWN));
		// END KGU#206 2016-07-21
	    // START KGU#177 2016-04-14: Enh. #158
		actMap.put("PAGE_DOWN", new PageScrollAction(scrollarea.getVerticalScrollBar(), false, "PAGE_DOWN"));
		actMap.put("PAGE_UP", new PageScrollAction(scrollarea.getVerticalScrollBar(), true, "PAGE_UP"));
		// END KGU#177 2016-04-16
		// START KGU#294 2016-11-22: Enh. #284
		actMap.put("FONT_DOWN", new FontResizeAction(diagram, "FONT_DOWN"));
		actMap.put("FONT_UP", new FontResizeAction(diagram, "FONT_UP"));
		// END KGU#294 2016-11-22
		// START KGU#347 2017-02-09: Enh. #344 additional key binding for redo
		actMap.put("REDO", new AbstractAction("REDO") { public void actionPerformed(ActionEvent event) { diagram.redoNSD(); doButtons(); }});
		// END KGU#347 2017-02-09
		//scrollarea.getViewport().setBackingStoreEnabled(true);
		
		// START KGU#239 2017-01-13: Bugfix #233 SplitPanes had snatched away accelerator keys F6 and F8
		inpMap = sp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "none");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, java.awt.event.InputEvent.CTRL_DOWN_MASK), inpMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0)));
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "none");
		inpMap = sp305.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "none");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, java.awt.event.InputEvent.CTRL_DOWN_MASK), inpMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0)));
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "none");
		// END KGU#239 2017-01-13
		
		// START KGU#305 2016-12-12: Enh. #305
		sp305.add(scrollIndex);
		scrollIndex.setWheelScrollingEnabled(true);
		scrollIndex.setDoubleBuffered(true);
		scrollIndex.setBorder(BorderFactory.createEmptyBorder());
		scrollIndex.setViewportView(diagramIndex);
		//scrollIndex.setPreferredSize(new Dimension(50,0));

		//diagramIndex.setFocusable(false);
		diagramIndex.setCellRenderer(new RootListCellRenderer());
		diagramIndex.setLayoutOrientation(JList.VERTICAL);
		diagramIndex.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		diagramIndex.addMouseListener(diagram);
		diagramIndex.addMouseListener(new PopupListener());
		// END KGU#305 2016-12-12
		// START KGU#305 2016-12-15: Enh. #305 - react to space and enter
		inpMap = diagramIndex.getInputMap(WHEN_FOCUSED);
		actMap = diagramIndex.getActionMap();
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "SPACE");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE");
		actMap.put("SPACE", new ArrangerIndexAction(false));
		actMap.put("ENTER", new ArrangerIndexAction(true));
		actMap.put("DELETE",new ArrangerIndexAction("DELETE"));
		// END KGU#305 2016-12-15

		
        //container.add(scrolllist,AKDockLayout.SOUTH);
		sp.add(scrolllist);
		scrolllist.setWheelScrollingEnabled(true);
		scrolllist.setDoubleBuffered(true);
		scrolllist.setBorder(BorderFactory.createEmptyBorder());
		scrolllist.setViewportView(errorlist);
		scrolllist.setPreferredSize(new Dimension(0,50));

		// START KGU#305 2016-12-15: Issue #312 show focus
		errorlist.setCellRenderer(new DefaultListCellRenderer() {
		    private final Color selectedBackgroundNimbus = new Color(57,105,138);
		    
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object obj, int index, boolean isSelected,
					boolean cellHasFocus) {
				super.getListCellRendererComponent(list, obj, index, isSelected, cellHasFocus);
		        if (isSelected) {
		    		if (UIManager.getLookAndFeel().getName().equals("Nimbus"))
		    		{
		    			// Again, a specific handling for Nimbus was necessary in order to show any difference at all.
		    			if (list.isFocusOwner()) {
		    				setBackground(selectedBackgroundNimbus);
		    				setForeground(Color.WHITE);
		    			}
		    			else {
		    				// Invert the selection colours
		    				setBackground(Color.WHITE);	
		    				setForeground(selectedBackgroundNimbus);
		    			}
		    		}
		    		else {
		    			if (list.isFocusOwner()) {
		    				setBackground(list.getSelectionBackground());
		    				setForeground(list.getSelectionForeground());
		    			}
		    			else {
		    				// Invert the selection colours
		                    setBackground(list.getSelectionForeground());
		                    setForeground(list.getSelectionBackground());
		    			}
		    		}
		        }
				return this;
			}

		});
		// END KGU#305 2016-12-15
		errorlist.setLayoutOrientation(JList.VERTICAL);
		errorlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		errorlist.addMouseListener(diagram);
		errorlist.addListSelectionListener(diagram);

		//diagram.setOpaque(true);
		diagram.addMouseListener(new PopupListener());
		
        // START KGU#287 2017-01-09: Issues #81/#330 GUI scaling
        GUIScaler.rescaleComponents(this);
//		if (this.getFrame() != null) {
//			SwingUtilities.updateComponentTreeUI(this.getFrame());
//		}
        // END KGU#287 2017-01-09

        // Attempt to find out what provokes the NullPointerExceptions on start
		//System.out.println("**** " + this + ".create() ready!");
		
		//doButtons();
		//container.validate();
	}
	
	public void doButtons()
	{
		if(NSDControll!=null)
		{
			NSDControll.doButtons();
		}
	}	
	
	public void doButtonsLocal()
	{
		//scrollarea.setViewportView(diagram);

		// conditions
		// START KGU#143 2016-01-21: Bugfix #114 - elements involved in execution must not be edited
		//boolean conditionAny =  diagram.getSelected() != null;
		Element selected = diagram.getSelected();
		boolean conditionAny =  selected != null && !selected.isExecuted();
		// END KGU#143 2016-01-21
		boolean condition =  conditionAny && diagram.getSelected()!=diagram.getRoot();
		// START KGU#87 2015-11-22: For most operations, multiple selections are not supported
		boolean conditionNoMult = condition && !diagram.selectedIsMultiple();
		// END KGU#87 2015-11-22
		//int i = -1;
		boolean conditionCanMoveUp = false;
		boolean conditionCanMoveDown = false;
		if (conditionAny)
		{
			// START KGU#144 2016-01-22: Bugfix for #38 - Leave the decision to the selected element
			//if(diagram.getSelected().parent!=null)
			//{
			//	// make sure parent is a subqueue, which is not the case if somebody clicks on a subqueue!
			//	if (diagram.getSelected().parent.getClass().getSimpleName().equals("Subqueue"))
			//	{
			//		i = ((Subqueue) diagram.getSelected().parent).getIndexOf(diagram.getSelected());
			//		conditionCanMoveUp = (i-1>=0);
			//		conditionCanMoveDown = (i+1<((Subqueue) diagram.getSelected().parent).getSize());
			//	}
			//}
			conditionCanMoveUp = diagram.getSelected().canMoveUp();
			conditionCanMoveDown = diagram.getSelected().canMoveDown();
			// END KGU#144 2016-01-22
		}
		
		// START KGU#137 2016-01-11: Bugfix #103 - Reflect the "saveworthyness" of the diagram
		// save
		btnSave.setEnabled(diagram.canSave(false));
		// END KGU#137 2016-01-11
	    // START KGU#373 2017-03-28: Enh. #387
		btnSaveAll.setEnabled(diagram.canSave(true));
	    // END KGU#373 2017-03-38
		
		// undo & redo
		btnUndo.setEnabled(diagram.getRoot().canUndo());
		btnRedo.setEnabled(diagram.getRoot().canRedo());
		
		// elements
		// START KGU#493 2018-02-12: Issue #510 Toolbars Before + After merged
		//btnBeforeInst.setEnabled(condition);
		//btnBeforeAlt.setEnabled(condition);
		//btnBeforeCase.setEnabled(condition);
		//btnBeforeFor.setEnabled(condition);
		//btnBeforeWhile.setEnabled(condition);
		//btnBeforeRepeat.setEnabled(condition);
		//btnBeforeForever.setEnabled(condition);
		//btnBeforeCall.setEnabled(condition);
		//btnBeforeJump.setEnabled(condition);
		//btnBeforePara.setEnabled(condition);
		// END KGU#493 2018-02-12

		btnAfterInst.setEnabled(condition);
		btnAfterAlt.setEnabled(condition);
		btnAfterCase.setEnabled(condition);
		btnAfterFor.setEnabled(condition);
		btnAfterWhile.setEnabled(condition);
		btnAfterRepeat.setEnabled(condition);
		btnAfterForever.setEnabled(condition);
		btnAfterCall.setEnabled(condition);
		btnAfterJump.setEnabled(condition);
		btnAfterPara.setEnabled(condition);

		// START KGU#87 2015-11-22: Why enable the main entry if no action is enabled?
		popupAdd.setEnabled(condition);
		// END KGU#87 2015-11-22
		popupAddBeforeInst.setEnabled(condition);
		popupAddBeforeAlt.setEnabled(condition);
		popupAddBeforeCase.setEnabled(condition);
		popupAddBeforeFor.setEnabled(condition);
		popupAddBeforeWhile.setEnabled(condition);
		popupAddBeforeRepeat.setEnabled(condition);
		popupAddBeforeForever.setEnabled(condition);
		popupAddBeforeCall.setEnabled(condition);
		popupAddBeforeJump.setEnabled(condition);
		popupAddBeforePara.setEnabled(condition);

		popupAddAfterInst.setEnabled(condition);
		popupAddAfterAlt.setEnabled(condition);
		popupAddAfterCase.setEnabled(condition);
		popupAddAfterFor.setEnabled(condition);
		popupAddAfterWhile.setEnabled(condition);
		popupAddAfterRepeat.setEnabled(condition);
		popupAddAfterForever.setEnabled(condition);
		popupAddAfterCall.setEnabled(condition);
		popupAddAfterJump.setEnabled(condition);
		popupAddAfterPara.setEnabled(condition);
		
		// colors
		btnColor0.setEnabled(condition);
		btnColor1.setEnabled(condition);
		btnColor2.setEnabled(condition);
		btnColor3.setEnabled(condition);
		btnColor4.setEnabled(condition);
		btnColor5.setEnabled(condition);
		btnColor6.setEnabled(condition);
		btnColor7.setEnabled(condition);
		btnColor8.setEnabled(condition);
		btnColor9.setEnabled(condition);
		
		// START KGU#123 2016-01-03: Enh. #87 - We allow multiple selection for collapsing
		// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
		btnCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());
		btnExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());			
		// END KGU#123 2016-01-03
		// START KGU#277 2016-10-13: Enh. #270
		btnDisable.setEnabled(condition && !(selected instanceof Subqueue) || diagram.selectedIsMultiple());
		// END KGU#277 2016-01-13

		// editing
		// START KGU#87 2015-11-22: Don't allow editing if multiple elements are selected
		//btnEdit.setEnabled(conditionAny);
		// START KGU#143 2016-11-17: Bugfix #114 - unstructured elements may be edited if parent is waiting
		//btnEdit.setEnabled(conditionAny && !diagram.selectedIsMultiple());
		btnEdit.setEnabled(diagram.canEdit());
		// END KGU#143 2016-11-17
		// END KGU#87 2015-11-22
		// START KGU#143 2016-01-21: Bugfix #114 - we must differentiate among cut and copy
		//btnDelete.setEnabled(diagram.canCutCopy());
		btnDelete.setEnabled(diagram.canCut());
		// END KGU#143 2016-01-21
		btnMoveUp.setEnabled(conditionCanMoveUp);
		btnMoveDown.setEnabled(conditionCanMoveDown);
		// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
		btnTransmute.setEnabled(diagram.canTransmute());
		// END KGU#199 2016-07-06
		// START KGU#365 2017-03-27: Enh. #380 - We allow subroutine generation
		btnOutsource.setEnabled(diagram.canCut());
		// END KGU#365 2017-03-27
		// START KGU#87 2015-11-22: Don't allow editing if multiple elements are selected
		//popupEdit.setEnabled(conditionAny);
		// START KGU#143 2016-11-17: Bugfix #114 - unstructured elements may be edited if parent is waiting
		//popupEdit.setEnabled(conditionAny && !diagram.selectedIsMultiple());
		popupEdit.setEnabled(diagram.canEdit());
		// END KGU#143 2016-11-17
		// END KGU#87 2015-11-22
		// START KGU#143 2016-01-21: Bugfix #114 - we must differentiate among cut and copy
		//popupDelete.setEnabled(condition);
		popupDelete.setEnabled(diagram.canCut());
		// END KGU#143 2016-01-21
		popupMoveUp.setEnabled(conditionCanMoveUp);
		popupMoveDown.setEnabled(conditionCanMoveDown);
		// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
		popupTransmute.setEnabled(diagram.canTransmute());
		// END KGU#199 2016-07-06
		// START KGU#365 2017-03-27: Enh. #380 - We allow subroutine generation
		popupOutsource.setEnabled(diagram.canCut());
		// END KGU#365 2017-03-27
		
		// START KGU#123 2016-01-03: Enh. #87 - We allow multiple selection for collapsing
		// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
		popupCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());
		popupExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());			
		// END KGU#123 2016-01-03
		// START KGU#277 2016-10-13: Enh. #270
		popupDisable.setEnabled(condition && !(selected instanceof Subqueue) || diagram.selectedIsMultiple());
		// END KGU#277 2016-01-13

		// executor
		// START KGU#143 2016-01-21: Bugfix #114 - breakpoints need a more generous enabling policy
		//popupBreakpoint.setEnabled(diagram.canCutCopy());	// KGU 2015-10-12: added
		// START KGU#177 2016-07-06: Enh. #158 - Collateral damage mended
		//popupBreakpoint.setEnabled(diagram.canCopy());
		popupBreakpoint.setEnabled(diagram.canCopyNoRoot());
		// END KGU#177 2016-07-06
		// END KGU#143 2016-01-21
		// START KGU#213 2016-08-02: Enh. #215 - breakpoint control enhanced
		popupBreakTrigger.setEnabled(diagram.canCopyNoRoot() && !diagram.selectedIsMultiple());
		// END KGU#213 2016-08-02
		
		// copy & paste
		// START KGU#143 2016-01-21: Bugfix #114 - we must differentiate among cut and copy
		//btnCopy.setEnabled(diagram.canCutCopy());
		//btnCut.setEnabled(diagram.canCutCopy());
		btnCopy.setEnabled(diagram.canCopy());
		btnCut.setEnabled(diagram.canCut());
		// END KGU#143 2016-01-21
		btnPaste.setEnabled(diagram.canPaste());
		// START KGU#143 2016-01-21: Bugfix #114 - we must differentiate among cut and copy
		//popupCopy.setEnabled(diagram.canCutCopy());
		//popupCut.setEnabled(diagram.canCutCopy());
		popupCopy.setEnabled(diagram.canCopy());
		popupCut.setEnabled(diagram.canCut());
		popupPaste.setEnabled(diagram.canPaste());
		// END KGU#143 2016-01-21
		
		// style
		// START KGU#493 2018-02-14: Issue #510 Disabled as of little use here 
		//btnNice.setSelected(diagram.isNice());
		// END KGU#493 2018-02-14
		btnFunction.setSelected(diagram.isSubroutine());
		btnProgram.setSelected(diagram.isProgram());
		btnInclude.setSelected(diagram.isInclude());

		// DIN
		if (Element.E_DIN)
		{
			// START KGU493 2018-02-12: Issues #4, #510
			//btnBeforeFor.setIcon(IconLoader.getIcon(10));
			//btnAfterFor.setIcon(IconLoader.getIcon(15));
			//btnAfterFor.setIcon(IconLoader.getIcon(62));
			//popupAddBeforeFor.setIcon(IconLoader.getIcon(10));
			//popupAddAfterFor.setIcon(IconLoader.getIcon(15));
			btnAfterFor.setIcon(IconLoader.getIcon(74));
			popupAddBeforeFor.setIcon(IconLoader.getIcon(/*95*/74));
			popupAddAfterFor.setIcon(IconLoader.getIcon(/*97*/74));
			// END KGU#493 2018-02-12
		}
		else
		{
			// START KGU493 2018-02-12: Issues #4, #510
			//btnBeforeFor.setIcon(IconLoader.getIcon(9));
			////btnAfterFor.setIcon(IconLoader.getIcon(14));
			//btnAfterFor.setIcon(IconLoader.getIcon(61));
			//popupAddBeforeFor.setIcon(IconLoader.getIcon(9));
			//popupAddAfterFor.setIcon(IconLoader.getIcon(14));
			btnAfterFor.setIcon(IconLoader.getIcon(53));
			popupAddBeforeFor.setIcon(IconLoader.getIcon(/*94*/53));
			popupAddAfterFor.setIcon(IconLoader.getIcon(/*96*/53));
			// END KGU#493 2018-02-12
		}
		
		
		//scrollarea.revalidate();
		//scrollarea.setViewportView(diagram);
		
		// analyser
		
		if(Element.E_ANALYSER==true)
		{
			if (sp.getDividerSize()==0)
			{
				sp.remove(scrolllist);
				sp.add(scrolllist);
			}
			scrolllist.setVisible(true);
			scrolllist.setViewportView(errorlist);
			sp.setDividerSize(5);
			scrolllist.revalidate();
		}
		else
		{
			sp.remove(scrolllist);
			sp.setDividerSize(0);
		}
		
		// START KGU#305 2016-12-12: Enh. 305
		// arranger
		
		if (diagram.showArrangerIndex() && !diagrams.isEmpty())
		{
			if (sp305.getDividerSize()==0)
			{
				sp305.remove(scrollIndex);
				sp305.add(scrollIndex);
			}
			scrollIndex.setVisible(true);
			scrollIndex.setViewportView(diagramIndex);
			sp305.setDividerSize(5);
			scrollIndex.revalidate();
		}
		else
		{
			scrollIndex.setVisible(false);
			sp305.remove(scrollIndex);
			sp305.setDividerSize(0);
		}
		// END KGU#305 2016-12-12
		
		// START KGU#318 2017-01-05: Enh. #319
		boolean indexSelected = scrollIndex.isVisible() && !diagramIndex.isSelectionEmpty();
		popupIndexGet.setEnabled(indexSelected && diagramIndex.getSelectedValue() != diagram.getRoot());
		popupIndexSave.setEnabled(indexSelected && diagramIndex.getSelectedValue().hasChanged());
		popupIndexRemove.setEnabled(indexSelected);
		popupIndexCovered.setEnabled(indexSelected && Element.E_COLLECTRUNTIMEDATA && !diagramIndex.getSelectedValue().isProgram());
		// END KGU#318 2017-01-05
           
                //
                /*
                for(int j=0;j<diagram.toolbars.size();j++)
                {
                  MyToolbar tb = diagram.toolbars.get(j);

                  this.remove(tb);
                  if(tb.isVisible()) this.add(tb,AKDockLayout.NORTH);
                  //else this.remove(tb);
                }
                 */

	}
	
	
    @Override
    public void updateColors() 
    {	
            btnColor0.setColor(Element.color0);
            btnColor1.setColor(Element.color1);
            btnColor2.setColor(Element.color2);
            btnColor3.setColor(Element.color3);
            btnColor4.setColor(Element.color4);
            btnColor5.setColor(Element.color5);
            btnColor6.setColor(Element.color6);
            btnColor7.setColor(Element.color7);
            btnColor8.setColor(Element.color8);
            btnColor9.setColor(Element.color9);
    }

    public Editor()
    {
            super();
            this.NSDControll=null;
            create();
    }

    public Editor(NSDController _NSDControll)
    {
            super();
            this.NSDControll=_NSDControll;
            create();
    }

    public JFrame getFrame()
    {
    	if (NSDControll != null) {
    		return NSDControll.getFrame();
    	}
    	return null;
    }

    public void loadFromINI()
    {
        // nothing to do here
    }

	// PopupListener Methods
	class PopupListener extends MouseAdapter 
	{
        @Override
		public void mousePressed(MouseEvent e) 
		{
			showPopup(e);
		}
		
        @Override
		public void mouseReleased(MouseEvent e) 
		{
			showPopup(e);
		}
		
		private void showPopup(MouseEvent e) 
		{
			if (e.isPopupTrigger()) 
			{
				// START KGU#318 2017-01-05: Enh. #319
				//popup.show(e.getComponent(), e.getX(), e.getY());
				if (e.getComponent() == diagram) {
					popup.show(e.getComponent(), e.getX(), e.getY());					
				}
				else if (e.getComponent() == diagramIndex) {
					doButtonsLocal();
					diagramIndex.requestFocusInWindow();
					popupIndex.show(e.getComponent(), e.getX(), e.getY());
				}
				// END KGU#318 2017-01-05
			}
		}
	}
		
	// ComponentListener Methods
	public void componentHidden(ComponentEvent e) 
	{
    }
	
    public void componentMoved(ComponentEvent e) 
	{
		//componentResized(e);
    }
	
    public void componentResized(ComponentEvent e) 
	{
		/*
		int maxWidth = 0;
		for(int i=0;i<toolbar.getComponentCount();i++)
		{
			maxWidth+=toolbar.getComponent(i).getWidth();
		}
		
		if(toolbar.getWidth()!=0)
		{
			int prefI = maxWidth/toolbar.getWidth();
			if (maxWidth % toolbar.getWidth() > 0 )
			{
				prefI++;
			}
			
			//System.out.println(prefI);
			
			toolbar.setPreferredSize(new Dimension(toolbar.getWidth(), Math.round(prefI)*26+6 ));
		}
		toolbar.validate();
		/**/
	}
	
    public void componentShown(ComponentEvent e) 
	{
		//componentResized(e);
    }
	
	public void setLookAndFeel(String _laf) {}
	public String getLookAndFeel() { return null;}

	public void savePreferences() 
	{
		NSDControll.savePreferences();
	}

    // START KGU#305 2016-12-12: Enh. #305 - Pass diagram list of Arranger to editor
	public void updateArrangerIndex(Vector<Root> _diagrams)
	{
		boolean wasEmpty = this.diagrams.isEmpty();
		this.diagrams.clear();
		if (_diagrams != null) {
			for (int i = 0; i < _diagrams.size(); i++) {
				Root aRoot = _diagrams.get(i);
				this.diagrams.addElement(aRoot);
			}
		}
		if (this.diagrams.isEmpty()) {
			this.scrollIndex.setVisible(false);
		}
		else if (wasEmpty) {
			this.scrollIndex.setVisible(true);
		}
		this.scrollIndex.repaint();
		this.scrollIndex.validate();
		this.doButtonsLocal();
	}
    // END KGU#305 2016-12-12
	
	// START KGU#305/KGU#318 2017-01-05: Enh. #305/#319 Arranger index action methods concentrated here
	public void arrangerIndexGet()
	{
		Root selectedRoot = diagramIndex.getSelectedValue();
		if (selectedRoot != null && selectedRoot != diagram.getRoot()) {
			diagram.setRootIfNotRunning(selectedRoot);
			scrollarea.requestFocusInWindow();
		}		
	}

	public void arrangerIndexSave()
	{
		Root selectedRoot = diagramIndex.getSelectedValue();
		if (selectedRoot != null) {
			diagram.saveNSD(selectedRoot, false);
		}		
	}

	public void arrangerIndexRemove()
	{
		int index = diagramIndex.getSelectedIndex();
		Root selectedRoot = diagramIndex.getSelectedValue();
		if (selectedRoot != null) {
			Arranger.getInstance().removeDiagram(selectedRoot);
		}
		if (index < diagramIndex.getModel().getSize()) {
			diagramIndex.setSelectedIndex(index);
		}
		else if (index > 0) {
			diagramIndex.setSelectedIndex(index-1);
		}
		if (diagramIndex.getModel().getSize() > 0) {
			diagramIndex.requestFocusInWindow();
		}
		else {
			scrollarea.requestFocusInWindow();
		}		
	}
	
	public void arrangerIndexToggleCovered()
	{
		Root selectedRoot = diagramIndex.getSelectedValue();
		if (selectedRoot != null && Element.E_COLLECTRUNTIMEDATA) {
			selectedRoot.deeplyCovered = !selectedRoot.deeplyCovered;
			// We must update/refresh both Structorizer and Arranger
			if (selectedRoot == diagram.getRoot()) {
				diagram.redraw();
			}
			else {
				Arranger.getInstance().redraw();
			}
			diagramIndex.repaint();
		}		
		
	}
	// END KGU#305/KGU#318 2017-01-05

}

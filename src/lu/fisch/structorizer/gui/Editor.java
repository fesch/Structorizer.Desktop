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
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import com.kobrix.notebook.gui.AKDockLayout;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.locales.LangPanel;

@SuppressWarnings("serial")
public class Editor extends LangPanel implements NSDController, ComponentListener
{
    // Controller
    NSDController NSDControll = null;

    // Toolbars
    protected MyToolbar toolbar = null;
	
    // Splitpane
    JSplitPane sp;

    // list
    DefaultListModel<DetectedError> errors = new DefaultListModel<DetectedError>();
    protected final JList<DetectedError> errorlist = new JList<DetectedError>(errors);

    // Panels
    public Diagram diagram = new Diagram(this, "???");
	
    // scrollarea
    protected final JScrollPane scrollarea = new JScrollPane(diagram);
    protected final JScrollPane scrolllist = new JScrollPane(errorlist);

    // Buttons
    // I/O
    protected final JButton btnNew = new JButton(IconLoader.ico001); 
    protected final JButton btnOpen = new JButton(IconLoader.ico002); 
    protected final JButton btnSave = new JButton(IconLoader.ico003); 
    // InsertBefore
    protected final JButton btnBeforeInst = new JButton(IconLoader.ico007); 
    protected final JButton btnBeforeAlt = new JButton(IconLoader.ico008); 
    protected final JButton btnBeforeFor = new JButton(IconLoader.ico009); 
    protected final JButton btnBeforeWhile = new JButton(IconLoader.ico010); 
    protected final JButton btnBeforeRepeat = new JButton(IconLoader.ico011); 
    protected final JButton btnBeforeCall = new JButton(IconLoader.ico049); 
    protected final JButton btnBeforeJump = new JButton(IconLoader.ico056); 
    protected final JButton btnBeforeCase = new JButton(IconLoader.ico047); 
    protected final JButton btnBeforeForever = new JButton(IconLoader.ico009);
    protected final JButton btnBeforePara = new JButton(IconLoader.ico090);
    // InsertAfter
    protected final JButton btnAfterInst = new JButton(IconLoader.ico012); 
    protected final JButton btnAfterAlt = new JButton(IconLoader.ico013); 
    protected final JButton btnAfterFor = new JButton(IconLoader.ico014); 
    protected final JButton btnAfterWhile = new JButton(IconLoader.ico015); 
    protected final JButton btnAfterRepeat = new JButton(IconLoader.ico016); 
    protected final JButton btnAfterCall = new JButton(IconLoader.ico050); 
    protected final JButton btnAfterJump = new JButton(IconLoader.ico055); 
    protected final JButton btnAfterCase = new JButton(IconLoader.ico048); 
    protected final JButton btnAfterForever = new JButton(IconLoader.ico014);
    protected final JButton btnAfterPara = new JButton(IconLoader.ico089);
	// undo & redo
    protected final JButton btnUndo = new JButton(IconLoader.ico039); 
	protected final JButton btnRedo = new JButton(IconLoader.ico038); 
	// copy & paste
	protected final JButton btnCut = new JButton(IconLoader.ico044); 
    protected final JButton btnCopy = new JButton(IconLoader.ico042); 
    protected final JButton btnPaste = new JButton(IconLoader.ico043);
	// style 
    protected final JToggleButton btnNice = new JToggleButton(IconLoader.ico040);
    protected final JToggleButton btnFunction = new JToggleButton(IconLoader.ico021);
    protected final JToggleButton btnProgram = new JToggleButton(IconLoader.ico022);
	// editing
    protected final JButton btnEdit = new JButton(IconLoader.ico006); 
    protected final JButton btnDelete = new JButton(IconLoader.ico005); 
    protected final JButton btnMoveUp = new JButton(IconLoader.ico019); 
    protected final JButton btnMoveDown = new JButton(IconLoader.ico020);
	// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
	protected final JButton btnTransmute = new JButton(IconLoader.ico109);
	// END KGU#199 2016-07-06
    // collapsing & expanding
    // START KGU#123 2016-01-04: Enh. #87 - Preparations for Fix #65
    protected final JButton btnCollapse = new JButton(IconLoader.ico106); 
    protected final JButton btnExpand = new JButton(IconLoader.ico107);    
    // END KGU#123 2016-01-04
	// printing
    protected final JButton btnPrint = new JButton(IconLoader.ico041);
    // START KGU#2 2015-11-19: Arranger launch added
    protected final JButton btnArrange = new JButton(IconLoader.ico105);
    // END KGU#2 2015-11-19
	// font
    protected final JButton btnFontUp = new JButton(IconLoader.ico033); 
    protected final JButton btnFontDown = new JButton(IconLoader.ico034);
	// copyright 
    protected final JButton btnAbout = new JButton(IconLoader.ico017);
    // executing / testing
    protected final JButton btnMake = new JButton(IconLoader.ico004);
    protected final JButton btnTurtle = new JButton(IconLoader.turtle);
    // START KGU 2015-10-12: Breakpoint wiping
    protected final JButton btnDropBrk = new JButton(IconLoader.ico104);
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
    protected final JMenuItem popupCut = new JMenuItem("Cut",IconLoader.ico044);
    protected final JMenuItem popupCopy = new JMenuItem("Copy",IconLoader.ico042);
    protected final JMenuItem popupPaste = new JMenuItem("Paste",IconLoader.ico043);
    protected final JMenu popupAdd = new JMenu("Add");
    // Submenu of "Add"
    protected final JMenu popupAddBefore = new JMenu("Before");
    // Submenus of "Add -> Before"
    protected final JMenuItem popupAddBeforeInst = new JMenuItem("Instruction",IconLoader.ico007);
    protected final JMenuItem popupAddBeforeAlt = new JMenuItem("IF statement",IconLoader.ico008);
    protected final JMenuItem popupAddBeforeCase = new JMenuItem("CASE statement",IconLoader.ico047);
    protected final JMenuItem popupAddBeforeFor = new JMenuItem("FOR loop",IconLoader.ico009);
    protected final JMenuItem popupAddBeforeWhile = new JMenuItem("WHILE loop",IconLoader.ico010);
    protected final JMenuItem popupAddBeforeRepeat = new JMenuItem("REPEAT loop",IconLoader.ico011);
    protected final JMenuItem popupAddBeforeForever = new JMenuItem("ENDLESS loop",IconLoader.ico009);
    protected final JMenuItem popupAddBeforeCall = new JMenuItem("Call",IconLoader.ico049);
    protected final JMenuItem popupAddBeforeJump = new JMenuItem("Jump",IconLoader.ico056);
    protected final JMenuItem popupAddBeforePara = new JMenuItem("Parallel",IconLoader.ico090);

    protected final JMenu popupAddAfter = new JMenu("After");
    // Submenus of "Add -> After"
    protected final JMenuItem popupAddAfterInst = new JMenuItem("Instruction",IconLoader.ico012);
    protected final JMenuItem popupAddAfterAlt = new JMenuItem("IF statement",IconLoader.ico013);
    protected final JMenuItem popupAddAfterCase = new JMenuItem("CASE statement",IconLoader.ico048);
    protected final JMenuItem popupAddAfterFor = new JMenuItem("FOR loop",IconLoader.ico014);
    protected final JMenuItem popupAddAfterWhile = new JMenuItem("WHILE loop",IconLoader.ico015);
    protected final JMenuItem popupAddAfterRepeat = new JMenuItem("REPEAT loop",IconLoader.ico016);
    protected final JMenuItem popupAddAfterCall = new JMenuItem("Call",IconLoader.ico050);
    protected final JMenuItem popupAddAfterJump = new JMenuItem("Jump",IconLoader.ico055);
    protected final JMenuItem popupAddAfterForever = new JMenuItem("ENDLESS loop",IconLoader.ico014);
    protected final JMenuItem popupAddAfterPara = new JMenuItem("Parallel",IconLoader.ico089);

    protected final JMenuItem popupEdit = new JMenuItem("Edit",IconLoader.ico006);
    protected final JMenuItem popupDelete = new JMenuItem("Delete",IconLoader.ico005);
    protected final JMenuItem popupMoveUp = new JMenuItem("Move up",IconLoader.ico019);
    protected final JMenuItem popupMoveDown = new JMenuItem("Move down",IconLoader.ico020);
	// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
	protected final JMenuItem popupTransmute = new JMenuItem("Transmute", IconLoader.ico109);
	// END KGU#199 2016-07-06
    // START KGU#123 2016-01-04: Enh. #87 - Preparations for Fix #65
    protected final JMenuItem popupCollapse = new JMenuItem("Collapse", IconLoader.ico106); 
    protected final JMenuItem popupExpand = new JMenuItem("Expand", IconLoader.ico107);    
    // END KGU#123 2016-01-04
    // START KGU#43 2015-10-12: Breakpoint toggle
    protected final JMenuItem popupBreakpoint = new JMenuItem("Toggle Breakpoint", IconLoader.ico103);
    // END KGU#43 2015-10-12
	// START KGU#213 2016-08-02: Enh. #215
	protected final JMenuItem popupBreakTrigger = new JMenuItem("Specify break trigger...", IconLoader.ico112);
	// END KGU#143 2016-08-02
    
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
    
    private MyToolbar newToolBar(String name)
    {
        toolbar = new MyToolbar();
        toolbar.setName(name);
        diagram.toolbars.add(toolbar);
        this.add(toolbar,AKDockLayout.NORTH);
        toolbar.setFloatable(true);
        toolbar.setRollover(true);
        //toolbar.addSeparator();
        return toolbar;
    }

    private void create()
    {
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
        popupAdd.setIcon(IconLoader.ico018);

        popupAdd.add(popupAddBefore);
        popupAddBefore.setIcon(IconLoader.ico019);

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

        popupAddBefore.add(popupAddBeforeForever);
        popupAddBeforeForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeRepeat);
        popupAddBeforeRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeCall);
        popupAddBeforeCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforeJump);
        popupAddBeforeJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",false); doButtons(); } } );

        popupAddBefore.add(popupAddBeforePara);
        popupAddBeforePara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",false); doButtons(); } } );

        popupAdd.add(popupAddAfter);
        popupAddAfter.setIcon(IconLoader.ico020);

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

        popupAddAfter.add(popupAddAfterForever);
        popupAddAfterForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",true); doButtons(); } } );

        popupAddAfter.add(popupAddAfterRepeat);
        popupAddAfterRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,true); doButtons(); } } );

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

        // START KGU#123 2016-01-03: Enh. #87 - New menu items (addressing Bug #65)
		popup.addSeparator();

		popup.add(popupCollapse);
		popupCollapse.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.collapseNSD(); doButtons(); } } );

		popup.add(popupExpand);
		popupExpand.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.expandNSD(); doButtons(); } } );
		// END KGU#123 2016-01-03

		// START KGU#43 2015-10-12 Add a possibility to set or unset a checkpoint on the selected Element
        popup.addSeparator();

        popup.add(popupBreakpoint);
        popupBreakpoint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleBreakpoint(); doButtons(); } }); 
        // END KGU#43 2015-10-12

		// START KGU#213 2016-08-02: Enh. #215 - new breakpoint feature
		popup.add(popupBreakTrigger);
        popupBreakTrigger.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editBreakTrigger(); doButtons(); } }); 
		// END KGU#213 2016-08-02

        // add toolbars
        //toolbar.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        this.setLayout(new AKDockLayout());
        toolbar=newToolBar("New, open, save");
		
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
		
		toolbar=newToolBar("Print");

		// printing
		//toolbar.addSeparator();
        toolbar.add(btnPrint);
		btnPrint.setFocusable(false);
		btnPrint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.printNSD(); doButtons(); } } );
		// START KGU#2 2015-11-24: Arranger launcher (now action correctly attached)
        toolbar.add(btnArrange);
        btnArrange.setToolTipText("Add the diagram to the Arranger panel");
		btnArrange.setFocusable(false);
		btnArrange.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.arrangeNSD(); doButtons(); } } );
		// END KGU#2 2015-11-24
		
		toolbar=newToolBar("Undo, redo");

		// undo & redo
		//toolbar.addSeparator();
        toolbar.add(btnUndo);
		btnUndo.setFocusable(false);
		btnUndo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.undoNSD(); doButtons(); } } );
        toolbar.add(btnRedo);
		btnRedo.setFocusable(false);
		btnRedo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.redoNSD(); doButtons(); } } );
		
		toolbar=newToolBar("Copy, cut, paste");

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
		
		toolbar=newToolBar("Edit, delete, move");

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
        toolbar.add(btnTransmute);
        btnTransmute.setFocusable(false);
		btnTransmute.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.transmuteNSD(); doButtons(); } } ); 
		// END KGU#199 2016-07-06
		
		toolbar=newToolBar("Method, program");

		// style
		//toolbar.addSeparator();
        toolbar.add(btnFunction);
		btnFunction.setFocusable(false);
		btnFunction.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setFunction(); doButtons(); } } );
        toolbar.add(btnProgram);
		btnProgram.setFocusable(false);
		btnProgram.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setProgram(); doButtons(); } } );

		toolbar=newToolBar("Nice");
		
		//toolbar.addSeparator();
        toolbar.add(btnNice);
		btnNice.setFocusable(false);
		btnNice.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setNice(btnNice.isSelected()); doButtons(); } } );
		
		toolbar=newToolBar("Add before ...");
		//toolbar.setOrientation(JToolBar.VERTICAL);
		//this.add(toolbar,BorderLayout.WEST);
		
		// IsertBefore
		//toolbar.addSeparator();
        toolbar.add(btnBeforeInst);
		btnBeforeInst.setFocusable(false);
		btnBeforeInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",false); doButtons(); } } );
        toolbar.add(btnBeforeAlt);
		btnBeforeAlt.setFocusable(false);
		btnBeforeAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,false); doButtons(); } } );
        toolbar.add(btnBeforeCase);
		btnBeforeCase.setFocusable(false);
		btnBeforeCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,false); doButtons(); } } );
        toolbar.add(btnBeforeFor);
		btnBeforeFor.setFocusable(false);
		btnBeforeFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,false); doButtons(); } } );
        toolbar.add(btnBeforeWhile);
		btnBeforeWhile.setFocusable(false);
		btnBeforeWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,false); doButtons(); } } );
        toolbar.add(btnBeforeRepeat);
		btnBeforeRepeat.setFocusable(false);
		btnBeforeRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,false); doButtons(); } } );
        toolbar.add(btnBeforeForever);
		btnBeforeForever.setFocusable(false);
		btnBeforeForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",false); doButtons(); } } );
        toolbar.add(btnBeforeCall);
		btnBeforeCall.setFocusable(false);
		btnBeforeCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",false); doButtons(); } } );
        toolbar.add(btnBeforeJump);
		btnBeforeJump.setFocusable(false);
		btnBeforeJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",false); doButtons(); } } );
        toolbar.add(btnBeforePara);
		btnBeforePara.setFocusable(false);
		btnBeforePara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",false); doButtons(); } } );

		toolbar=newToolBar("Add after ...");
		//toolbar.setOrientation(JToolBar.VERTICAL);
		//this.add(toolbar,BorderLayout.WEST);

		// IsertAfter
		//toolbar.addSeparator();
        toolbar.add(btnAfterInst);
		btnAfterInst.setFocusable(false);
		btnAfterInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",true); doButtons(); } } );
        toolbar.add(btnAfterAlt);
		btnAfterAlt.setFocusable(false);
		btnAfterAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,true); doButtons(); } } );
        toolbar.add(btnAfterCase);
		btnAfterCase.setFocusable(false);
		btnAfterCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,true); doButtons(); } } );
        toolbar.add(btnAfterFor);
		btnAfterFor.setFocusable(false);
		btnAfterFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,true); doButtons(); } } );
        toolbar.add(btnAfterWhile);
		btnAfterWhile.setFocusable(false);
		btnAfterWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,true); doButtons(); } } );
        toolbar.add(btnAfterRepeat);
		btnAfterRepeat.setFocusable(false);
		btnAfterRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,true); doButtons(); } } );
        toolbar.add(btnAfterForever);
		btnAfterForever.setFocusable(false);
		btnAfterForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",true); doButtons(); } } );
        toolbar.add(btnAfterCall);
		btnAfterCall.setFocusable(false);
		btnAfterCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",true); doButtons(); } } );
        toolbar.add(btnAfterJump);
		btnAfterJump.setFocusable(false);
		btnAfterJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",true); doButtons(); } } );
        toolbar.add(btnAfterPara);
		btnAfterPara.setFocusable(false);
		btnAfterPara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",true); doButtons(); } } );
		
		toolbar=newToolBar("Colors ...");

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
		toolbar=newToolBar("Collapsing");

		// Collapse & Expand
		//toolbar.addSeparator();
        toolbar.add(btnCollapse);
		btnCollapse.setFocusable(false);
		btnCollapse.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.collapseNSD(); } } );
        toolbar.add(btnExpand);
		btnExpand.setFocusable(false);
		btnExpand.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.expandNSD(); } } );
		// END KGU#123 2016-01-04

		toolbar=newToolBar("About");

		// About
		//toolbar.addSeparator();
        toolbar.add(btnAbout);
		btnAbout.setFocusable(false);
		btnAbout.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.aboutNSD(); } } );

		toolbar=newToolBar("Turtle, interprete");

		// Turtle
		//toolbar.addSeparator();
        toolbar.add(btnTurtle);
		btnTurtle.setFocusable(false);
		btnTurtle.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.goTurtle(); } } );
        toolbar.add(btnMake);
		btnMake.setFocusable(false);
		btnMake.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.goRun(); } } );
        toolbar.add(btnDropBrk);
		btnDropBrk.setFocusable(false);
		btnDropBrk.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.clearBreakpoints(); } } );

		toolbar=newToolBar("Font ...");

		// Font
		//toolbar.addSeparator();
        toolbar.add(btnFontUp);
		btnFontUp.setFocusable(false);
		btnFontUp.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.fontUpNSD(); doButtons(); } } );
        toolbar.add(btnFontDown);
		btnFontDown.setFocusable(false);
		btnFontDown.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.fontDownNSD(); doButtons(); } } );
		
		
		sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.add(sp,AKDockLayout.CENTER);
		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.setResizeWeight(0.99);
		sp.setDividerSize(5);
		
		// add panels
		// get container object
		//Container container = this;
        //container.add(scrollarea,AKDockLayout.CENTER);
		
		sp.add(scrollarea);
                scrollarea.setBackground(Color.LIGHT_GRAY);
		scrollarea.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
		scrollarea.setWheelScrollingEnabled(true);
		scrollarea.setDoubleBuffered(true);
		scrollarea.setBorder(BorderFactory.createEmptyBorder());
		scrollarea.setViewportView(diagram);
		// START KGU#177 2016-04-06 Enh. #158 Moving selection by cursor keys
		InputMap inpMap = scrollarea.getInputMap(WHEN_IN_FOCUSED_WINDOW);
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
		//scrollarea.getViewport().setBackingStoreEnabled(true);
				
        //container.add(scrolllist,AKDockLayout.SOUTH);
		sp.add(scrolllist);
		scrolllist.setWheelScrollingEnabled(true);
		scrolllist.setDoubleBuffered(true);
		scrolllist.setBorder(BorderFactory.createEmptyBorder());
		scrolllist.setViewportView(errorlist);
		scrolllist.setPreferredSize(new Dimension(0,50));

		errorlist.setLayoutOrientation(JList.VERTICAL);
		errorlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		errorlist.addMouseListener(diagram);

		//diagram.setOpaque(true);
		diagram.addMouseListener(new PopupListener());
		
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
		btnSave.setEnabled(diagram.getRoot().hasChanged());
		// END KGU#137 2016-01-11
		
		// undo & redo
		btnUndo.setEnabled(diagram.getRoot().canUndo());
		btnRedo.setEnabled(diagram.getRoot().canRedo());
		
		// elements
		btnBeforeInst.setEnabled(conditionNoMult);
		btnBeforeAlt.setEnabled(conditionNoMult);
		btnBeforeCase.setEnabled(conditionNoMult);
		btnBeforeFor.setEnabled(conditionNoMult);
		btnBeforeWhile.setEnabled(conditionNoMult);
		btnBeforeRepeat.setEnabled(conditionNoMult);
		btnBeforeForever.setEnabled(conditionNoMult);
		btnBeforeCall.setEnabled(conditionNoMult);
		btnBeforeJump.setEnabled(conditionNoMult);
		btnBeforePara.setEnabled(conditionNoMult);

		btnAfterInst.setEnabled(conditionNoMult);
		btnAfterAlt.setEnabled(conditionNoMult);
		btnAfterCase.setEnabled(conditionNoMult);
		btnAfterFor.setEnabled(conditionNoMult);
		btnAfterWhile.setEnabled(conditionNoMult);
		btnAfterRepeat.setEnabled(conditionNoMult);
		btnAfterForever.setEnabled(conditionNoMult);
		btnAfterCall.setEnabled(conditionNoMult);
		btnAfterJump.setEnabled(conditionNoMult);
		btnAfterPara.setEnabled(conditionNoMult);

		// START KGU#87 2015-11-22: Why enable the main entry if no action is enabled?
		popupAdd.setEnabled(conditionNoMult);
		// END KGU#87 2015-11-22
		popupAddBeforeInst.setEnabled(conditionNoMult);
		popupAddBeforeAlt.setEnabled(conditionNoMult);
		popupAddBeforeCase.setEnabled(conditionNoMult);
		popupAddBeforeFor.setEnabled(conditionNoMult);
		popupAddBeforeWhile.setEnabled(conditionNoMult);
		popupAddBeforeRepeat.setEnabled(conditionNoMult);
		popupAddBeforeForever.setEnabled(conditionNoMult);
		popupAddBeforeCall.setEnabled(conditionNoMult);
		popupAddBeforeJump.setEnabled(conditionNoMult);
		popupAddBeforePara.setEnabled(conditionNoMult);

		popupAddAfterInst.setEnabled(conditionNoMult);
		popupAddAfterAlt.setEnabled(conditionNoMult);
		popupAddAfterCase.setEnabled(conditionNoMult);
		popupAddAfterFor.setEnabled(conditionNoMult);
		popupAddAfterWhile.setEnabled(conditionNoMult);
		popupAddAfterRepeat.setEnabled(conditionNoMult);
		popupAddAfterForever.setEnabled(conditionNoMult);
		popupAddAfterCall.setEnabled(conditionNoMult);
		popupAddAfterJump.setEnabled(conditionNoMult);
		popupAddAfterPara.setEnabled(conditionNoMult);
		
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
		btnCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());
		btnExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());			
		// END KGU#123 2016-01-03

		// editing
		// START KGU#87 2015-11-22: Don't allow editing if multiple elements are selected
		//btnEdit.setEnabled(conditionAny);
		btnEdit.setEnabled(conditionAny && !diagram.selectedIsMultiple());
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
		// START KGU#87 2015-11-22: Don't allow editing if multiple elements are selected
		//popuEdit.setEnabled(conditionAny);
		popupEdit.setEnabled(conditionAny && !diagram.selectedIsMultiple());
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
		
		// START KGU#123 2016-01-03: Enh. #87 - We allow multiple selection for collapsing
		// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
		popupCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());
		popupExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());			
		// END KGU#123 2016-01-03

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
		btnNice.setSelected(diagram.isNice());
		btnFunction.setSelected(!diagram.isProgram());
		btnProgram.setSelected(diagram.isProgram());

		// DIN
		if(Element.E_DIN==true)
		{
			btnBeforeFor.setIcon(IconLoader.ico010);
			btnAfterFor.setIcon(IconLoader.ico015);
			popupAddBeforeFor.setIcon(IconLoader.ico010);
			popupAddAfterFor.setIcon(IconLoader.ico015);
		}
		else
		{
			btnBeforeFor.setIcon(IconLoader.ico009);
			btnAfterFor.setIcon(IconLoader.ico014);
			popupAddBeforeFor.setIcon(IconLoader.ico009);
			popupAddAfterFor.setIcon(IconLoader.ico014);
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
                return NSDControll.getFrame();
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
				popup.show(e.getComponent(), e.getX(), e.getY());
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

}

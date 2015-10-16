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
 *      ------			----			-----------
 *      Bob Fisch       2007.12.28      First Issue
 *      Kay Gürtzig     2015.10.12      control elements for breakpoint handling added. 
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

//import sun.awt.image.codec.JPEGImageEncoderImpl;

import lu.fisch.structorizer.elements.*;

public class Editor extends JPanel implements NSDController, ComponentListener
{
    // Controller
    NSDController NSDControll = null;

    // Toolbars
    protected MyToolbar toolbar = null;
	
    // Splitpane
    JSplitPane sp;

    // list
    DefaultListModel errors = new DefaultListModel();
    protected JList errorlist = new JList(errors);

    // Panels
    public Diagram diagram = new Diagram(this, "???");
	
    // scrollarea
    protected JScrollPane scrollarea = new JScrollPane(diagram);
    protected JScrollPane scrolllist = new JScrollPane(errorlist);

    // Buttons
    // I/O
    protected JButton btnNew = new JButton(IconLoader.ico001); 
    protected JButton btnOpen = new JButton(IconLoader.ico002); 
    protected JButton btnSave = new JButton(IconLoader.ico003); 
    // InsertBefore
    protected JButton btnBeforeInst = new JButton(IconLoader.ico007); 
    protected JButton btnBeforeAlt = new JButton(IconLoader.ico008); 
    protected JButton btnBeforeFor = new JButton(IconLoader.ico009); 
    protected JButton btnBeforeWhile = new JButton(IconLoader.ico010); 
    protected JButton btnBeforeRepeat = new JButton(IconLoader.ico011); 
    protected JButton btnBeforeCall = new JButton(IconLoader.ico049); 
    protected JButton btnBeforeJump = new JButton(IconLoader.ico056); 
    protected JButton btnBeforeCase = new JButton(IconLoader.ico047); 
    protected JButton btnBeforeForever = new JButton(IconLoader.ico009);
    protected JButton btnBeforePara = new JButton(IconLoader.ico090);
    // InsertAfter
    protected JButton btnAfterInst = new JButton(IconLoader.ico012); 
    protected JButton btnAfterAlt = new JButton(IconLoader.ico013); 
    protected JButton btnAfterFor = new JButton(IconLoader.ico014); 
    protected JButton btnAfterWhile = new JButton(IconLoader.ico015); 
    protected JButton btnAfterRepeat = new JButton(IconLoader.ico016); 
    protected JButton btnAfterCall = new JButton(IconLoader.ico050); 
    protected JButton btnAfterJump = new JButton(IconLoader.ico055); 
    protected JButton btnAfterCase = new JButton(IconLoader.ico048); 
    protected JButton btnAfterForever = new JButton(IconLoader.ico014);
    protected JButton btnAfterPara = new JButton(IconLoader.ico089);
	// undo & redo
    protected JButton btnUndo = new JButton(IconLoader.ico039); 
	protected JButton btnRedo = new JButton(IconLoader.ico038); 
	// copy & paste
	protected JButton btnCut = new JButton(IconLoader.ico044); 
    protected JButton btnCopy = new JButton(IconLoader.ico042); 
    protected JButton btnPaste = new JButton(IconLoader.ico043);
	// style 
    protected JToggleButton btnNice = new JToggleButton(IconLoader.ico040);
    protected JToggleButton btnFunction = new JToggleButton(IconLoader.ico021);
    protected JToggleButton btnProgram = new JToggleButton(IconLoader.ico022);
	// editing
    protected JButton btnEdit = new JButton(IconLoader.ico006); 
    protected JButton btnDelete = new JButton(IconLoader.ico005); 
    protected JButton btnMoveUp = new JButton(IconLoader.ico019); 
    protected JButton btnMoveDown = new JButton(IconLoader.ico020); 
	// printing
    protected JButton btnPrint = new JButton(IconLoader.ico041); 
	// font
    protected JButton btnFontUp = new JButton(IconLoader.ico033); 
    protected JButton btnFontDown = new JButton(IconLoader.ico034);
	// copyright 
    protected JButton btnAbout = new JButton(IconLoader.ico017);
    // executing / testing
    protected JButton btnMake = new JButton(IconLoader.ico004);
    protected JButton btnTurtle = new JButton(IconLoader.turtle);
    // START KGU 2015-10-12: Breakpoint wiping
    protected JButton btnDropBrk = new JButton(IconLoader.ico104);
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
    protected JPopupMenu popup = new JPopupMenu();
    protected JMenuItem popupCut = new JMenuItem("Cut",IconLoader.ico044);
    protected JMenuItem popupCopy = new JMenuItem("Copy",IconLoader.ico042);
    protected JMenuItem popupPaste = new JMenuItem("Paste",IconLoader.ico043);
    protected JMenu popupAdd = new JMenu("Add");
    // Submenu of "Add"
    protected JMenu popupAddBefore = new JMenu("Before");
    // Submenus of "Add -> Before"
    protected JMenuItem popupAddBeforeInst = new JMenuItem("Instruction",IconLoader.ico007);
    protected JMenuItem popupAddBeforeAlt = new JMenuItem("IF statement",IconLoader.ico008);
    protected JMenuItem popupAddBeforeCase = new JMenuItem("CASE statement",IconLoader.ico047);
    protected JMenuItem popupAddBeforeFor = new JMenuItem("FOR loop",IconLoader.ico009);
    protected JMenuItem popupAddBeforeWhile = new JMenuItem("WHILE loop",IconLoader.ico010);
    protected JMenuItem popupAddBeforeRepeat = new JMenuItem("REPEAT loop",IconLoader.ico011);
    protected JMenuItem popupAddBeforeForever = new JMenuItem("ENDLESS loop",IconLoader.ico009);
    protected JMenuItem popupAddBeforeCall = new JMenuItem("Call",IconLoader.ico049);
    protected JMenuItem popupAddBeforeJump = new JMenuItem("Jump",IconLoader.ico056);
    protected JMenuItem popupAddBeforePara = new JMenuItem("Parallel",IconLoader.ico090);

    protected JMenu popupAddAfter = new JMenu("After");
    // Submenus of "Add -> After"
    protected JMenuItem popupAddAfterInst = new JMenuItem("Instruction",IconLoader.ico012);
    protected JMenuItem popupAddAfterAlt = new JMenuItem("IF statement",IconLoader.ico013);
    protected JMenuItem popupAddAfterCase = new JMenuItem("CASE statement",IconLoader.ico048);
    protected JMenuItem popupAddAfterFor = new JMenuItem("FOR loop",IconLoader.ico014);
    protected JMenuItem popupAddAfterWhile = new JMenuItem("WHILE loop",IconLoader.ico015);
    protected JMenuItem popupAddAfterRepeat = new JMenuItem("REPEAT loop",IconLoader.ico016);
    protected JMenuItem popupAddAfterCall = new JMenuItem("Call",IconLoader.ico050);
    protected JMenuItem popupAddAfterJump = new JMenuItem("Jump",IconLoader.ico055);
    protected JMenuItem popupAddAfterForever = new JMenuItem("ENDLESS loop",IconLoader.ico014);
    protected JMenuItem popupAddAfterPara = new JMenuItem("Parallel",IconLoader.ico089);

    protected JMenuItem popupEdit = new JMenuItem("Edit",IconLoader.ico006);
    protected JMenuItem popupDelete = new JMenuItem("Delete",IconLoader.ico005);
    protected JMenuItem popupMoveUp = new JMenuItem("Move up",IconLoader.ico019);
    protected JMenuItem popupMoveDown = new JMenuItem("Move down",IconLoader.ico020);
    // START KGU 2015-10-12: Breakpoint toggle
    protected JMenuItem popupBreakpoint = new JMenuItem("Toggle Breakpoint", IconLoader.ico103);
    // END KGU 2015-10-12

    
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
        
        // START KGU 2015-10-12 Add a possibility to set or unset a checkpoint on the selected Element
        popup.addSeparator();

        popup.add(popupBreakpoint);
        popupBreakpoint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleBreakpoint(); doButtons(); } }); 
        // END KGU 2015-10-12

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
		boolean conditionAny =  diagram.getSelected() != null;
		boolean condition =  conditionAny && diagram.getSelected() != diagram.root;
		int i = -1;
		boolean conditionCanMoveUp = false;
		boolean conditionCanMoveDown = false;
		if (conditionAny)
		{
			if (diagram.getSelected().parent!=null)
			{
				// make sure parent is a subqueue, which is not the case if somebody clicks on a subqueue!
				if (diagram.getSelected().parent.getClass().getSimpleName().equals("Subqueue"))
				{
					i = ((Subqueue) diagram.getSelected().parent).getIndexOf(diagram.getSelected());
					conditionCanMoveUp = (i-1>=0);
					conditionCanMoveDown = (i+1<((Subqueue) diagram.getSelected().parent).getSize());
				}
			}
		}
		
		// undo & redo
		btnUndo.setEnabled(diagram.root.canUndo());
		btnRedo.setEnabled(diagram.root.canRedo());
		
		// elements
		btnBeforeInst.setEnabled(condition);
		btnBeforeAlt.setEnabled(condition);
		btnBeforeCase.setEnabled(condition);
		btnBeforeFor.setEnabled(condition);
		btnBeforeWhile.setEnabled(condition);
		btnBeforeRepeat.setEnabled(condition);
		btnBeforeForever.setEnabled(condition);
		btnBeforeCall.setEnabled(condition);
		btnBeforeJump.setEnabled(condition);
		btnBeforePara.setEnabled(condition);

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
		
		// editing
		btnEdit.setEnabled(conditionAny);
		btnDelete.setEnabled(diagram.canCutCopy());
		btnMoveUp.setEnabled(conditionCanMoveUp);
		btnMoveDown.setEnabled(conditionCanMoveDown);
		popupEdit.setEnabled(conditionAny);
		popupDelete.setEnabled(condition);
		popupMoveUp.setEnabled(conditionCanMoveUp);
		popupMoveDown.setEnabled(conditionCanMoveDown);
		
		// executor
		popupBreakpoint.setEnabled(diagram.canCutCopy());	// KGU 2015-10-12: added
		
		// copy & paste
		btnCopy.setEnabled(diagram.canCutCopy());
		btnCut.setEnabled(diagram.canCutCopy());
		btnPaste.setEnabled(diagram.canPaste());
		popupCopy.setEnabled(diagram.canCutCopy());
		popupCut.setEnabled(diagram.canCutCopy());
		popupPaste.setEnabled(diagram.canPaste());
		
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
	
	public void setLangLocal(String _langfile)
	{
		LangDialog.setLang(this,NSDControll.getLang());
	}
	
	public void setLang(String _langfile)
	{
		NSDControll.setLang(_langfile);
	}
	
	public String getLang()
	{
		return NSDControll.getLang();
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

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
 *      Description:    This class represents the basic diagram editor.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-28      First Issue
 *      Kay Gürtzig     2015-10-12      control elements for breakpoint handling added (KGU#43). 
 *      Kay Gürtzig     2015-11-22      Adaptations for handling selected non-empty Subqueues (KGU#87)
 *      Kay Gürtzig     2016-01-04      Enh. #87: New buttons and menu items for collapsing/expanding elements
 *      Kay Gürtzig     2016-01-11      Enh. #103: Save button disabled while Root is unchanged (KGU#137)
 *      Kay Gürtzig     2016-01-21      Bugfix #114: Editing restrictions during execution (KGU#143)
 *      Kay Gürtzig     2016-01-22      Bugfix for Enh. #38 (addressing moveUp/moveDown, KGU#143 + KGU#144).
 *      Kay Gürtzig     2016-04-06      Enh. #158: Key bindings for cursor keys added (KGU#177)
 *      Kay Gürtzig     2016-04-14      Enh. #158: Key bindings for page keys added (KGU#177)
 *      Kay Gürtzig     2016-07-06      Enh. #188: New button and menu item for element conversion (KGU#199)
 *      Kay Gürtzig     2016-07-21      Enh. #197: Selection may be expanded by Shift-Up and Shift-Down (KGU#206)
 *      Kay Gürtzig     2016-08-02      Enh. #215: popupBreakTrigger added
 *      Kay Gürtzig     2016-10-13      Enh. #277: New toolbar button (+ context menu item) for disabling elements
 *      Kay Gürtzig     2016-11-17      Bugfix #114: Prerequisites for editing and transmutation during execution revised
 *      Kay Gürtzig     2016-11-22      Enh. #284: Key bindings for font resizing added (KGU#294)
 *      Kay Gürtzig     2016-12-12      Enh. #305: Scrollable list view of Roots in Arranger added
 *      Kay Gürtzig     2016-12-17      Enh. #305: Key binding <del> added to Arranger index list.
 *      Kay Gürtzig     2017-01-05      Enh. #319: Context menu for Arranger index
 *      Kay Gürtzig     2017-01-13      Bugfix #233: F6 and F8 had got kidnapped by the JSplitPanes sp and sp305
 *      Kay Gürtzig     2017-02-09      Enh. #344: Ctrl-Y as additional redo key binding
 *      Kay Gürtzig     2017-03-27      Enh. #380: New button/popup menu item to convert a sequence in a subroutine
 *      Kay Gürtzig     2017-03-28      Enh. #387: New "Save All" button
 *      Kay Gürtzig     2017-05-16      Enh. #389: Button for third diagram type (includable)
 *      Kay Gürtzig     2017-06-15      Enh. #415, #199: Toolbar additions for find & replace as well as help
 *      Kay Gürtzig     2017-11-05      Issue #452: Mechanisms for simplified toolbar (beginners' mode)
 *      Kay Gürtzig     2017-11-19      Bugfix: #468: action helpNSD had been associated to wrong toolbar button
 *      Kay Gürtzig     2018-02-12      Issues #4, #510: element toolbars merged, icon numbers modified
 *      Kay Gürtzig     2018-02-13      Issue #510: All "arrowed" element icons replaced by pure element icons
 *      Kay Gürtzig     2018-02-14      Issue #510: btnUnboxed and its solitary toolbar disabled.
 *      Kay Gürtzig     2018-07-02      KGU#245: color buttons converted into an array
 *      Kay Gürtzig     2018-07-27      Bugfix #568: Action name for space key binding in arranger list corrected
 *      Kay Gürtzig     2018-09-13      Enh. #590: New entry "Inspect attributes..." in the Arranger Index popup menu
 *      Kay Gürtzig     2018-10-02      Enh. #616: Additional key bindings Ctrl-Ins, Shift-Del, and Shift-Ins
 *      Kay Gürtzig     2018-12-30      Enh. #158, #655: Key bindings for shift+page keys, home, end
 *      Kay Gürtzig     2019-01-01      Enh. #657: JList diagramIndex replaced by JTree arrangerIndex
 *      Kay Gürtzig     2019-01-05/06   Enh. #657: Arranger index popup menu item "diagram info" added
 *      Kay Gürtzig     2019-01-07/08   Enh. #622: Group info box redesigned
 *      Kay Gürtzig     2019-01-12      Enh. #662: Arranger index stuff moved to a new class ArrangerIndex
 *      Kay Gürtzig     2019-02-05      Bugfix #674: L&F update of popup menu ensured
 *      Kay Gürtzig     2019-02-26      Enh. #689: New menu item to edit the sub diagram referred by a CALL
 *      Kay Gürtzig     2019-03-15/17   Issue #56: new menu items for Try elements, breakpoint items disabled
 *                                      for Forever and Try elements.
 *      Kay Gürtzig     2019-03-22      Enh. #452: Several popup menu items made invisible on simplified mode
 *      Kay Gürtzig     2019-09-23      Enh. #738: First code preview implementation approach
 *      Kay Gürtzig     2019-09-26      Enh. #738: Popup menu for code review accomplished.
 *      Kay Gürtzig     2019-09-27      Enh. #738: Click in code preview now selects element and highlights code,
 *                                      double-click opens element editor
 *      Kay Gürtzig     2019-10-02      Enh. #738 code preview font control via Ctrl-Numpad-+/-
 *      Kay Gürtzig     2020-05-02      Issue #866: Modified key bindings for expanding / reducing selection
 *      Bob Fisch       2020-05-25      New "restricted" mode to suppress code export/import
 *      Kay Gürtzig     2020-06-03      Bugfix #868: Suppression of export / import now works without side-effect
 *      Kay Gürtzig     2020-06-06      Bugfix #868/#870: Suppression of group export had been forgotten.
 *      Kay Gürtzig     2020-10-17      Enh. #872: New toolbar with 2 display mode indicators
 *      Kay Gürtzig     2020-12-11      Bugfix #885: Display mode indicator visibility mended
 *      Kay Gürtzig     2020-10-15      Bugfix #885 enabling rule for the mode display was still flawed
 *      Kay Gürtzig     2021-01-01      Enh. #903: C operator display mode indicator visibility modified
 *      Kay Gürtzig     2021-01-13      Icon for "About ..." menu item replaced
 *      Kay Gürtzig     2021-01-27      Enh. #917: popupEditSub enhanced to work also for included diagrams
 *      Kay Gürtzig     2021-01-28      Accelerator associations for the context menu items set.
 *      Kay Gürtzig     2021-03-03      Issue #954: Modified behaviour of "btnDropBrk" button
 *      Kay Gürtzig     2021-03-09      Issue #966: Precaution for dark look & feel themes
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import com.kobrix.notebook.gui.AKDockLayout;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import lu.fisch.structorizer.arranger.ArrangerIndex;
import lu.fisch.structorizer.arranger.Group;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.locales.LangEvent;
import lu.fisch.structorizer.locales.LangEventListener;
import lu.fisch.structorizer.locales.LangPanel;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.parsers.GENParser;

@SuppressWarnings("serial")
public class Editor extends LangPanel implements NSDController, ComponentListener
{
	// Controller
	NSDController NSDControll = null;

//	// Toolbars
//	protected MyToolbar toolbar = null;
	
	// Splitpanes
	JSplitPane sp;
	// START KGU#305 2016-12-12: Enh. #305 - add a diagram index for Arranger
	JSplitPane sp305;
	// END KGU#305 2016-12-12
	
	// START KGU#964 2021-03-09: Issue #966 Bad preview readability with dark themes
	/** Standard background colour of the code preview */
	private static final Color CODE_PREVIEW_BACKGROUND = new Color(237, 237, 255);
	// END KGU#964 2021-03-09

	// lists
	DefaultListModel<DetectedError> errors = new DefaultListModel<DetectedError>();
	protected final JList<DetectedError> errorlist = new JList<DetectedError>(errors);

	// START KGU#705 2019-09-23: Enh. #738 Code preview
	protected final JTextArea txtCode = new JTextArea();
	// END KGU#705 2019-09-23
	// Panels
	public Diagram diagram = new Diagram(this, "???");
	// START KGU#305 2016-12-12: Enh. #305 - add a diagram index for Arranger
	// START KGU#630 2019-01-12: Replaces all former arranger index stuff strewn here
	protected final ArrangerIndex arrangerIndex = new ArrangerIndex(diagram);
	// END KGU#630 2019-01-12
	// END KGU#305 2016-12-12
	
	// scrollpanes
	protected final JScrollPane scrollarea = new JScrollPane(diagram);
	protected final JScrollPane scrolllist = new JScrollPane(errorlist);
	// START KGU#305 2016-12-12: Enh. #305 - add a diagram index for Arranger
	protected final JScrollPane scrollIndex = new JScrollPane(arrangerIndex);
	// END KGU#305 2016-12-12
	// START KGU#705 2019-09-23: Enh. #738 Code preview
	public final JTabbedPane pnlTabbed = new JTabbedPane();
	protected final JScrollPane scrollCode = new JScrollPane(txtCode);
	// END KGU#705 2019-09-23

	// Buttons
	// I/O
	protected final JButton btnNew = new JButton(IconLoader.getIcon(1)); 
	protected final JButton btnOpen = new JButton(IconLoader.getIcon(2)); 
	protected final JButton btnSave = new JButton(IconLoader.getIcon(3));
	// START KGU#373 2017-03-28: Enh. #387
	protected final JButton btnSaveAll = new JButton(IconLoader.getIcon(69));
	// END KGU#373 2017-03-38
	// START KGU493 2018-02-12: Issue #510 - toolbars Before and After merged
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
	// START KGU#686 2019-03-16: Enh. #56
	protected final JButton btnAfterTry = new JButton(IconLoader.getIcon(120));
	// END KGU#686 2019-03-16
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
	protected final JToggleButton btnUnboxed = new JToggleButton(IconLoader.getIcon(40));	// KGU#494 2018-02-14 #510 not used
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
	// START KGU 2021-01-13: Icon replaced
	//protected final JButton btnAbout = new JButton(IconLoader.getIcon(17));
	protected final JButton btnAbout = new JButton(IconLoader.getIcon(126));
	// END KGU 2021-01-13
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
	// START KGU#952 2021-03-03: Issue #954 modified functionality
	//protected final JButton btnDropBrk = new JButton(IconLoader.getIcon(104));
	protected final JToggleButton btnDropBrk = new JToggleButton(IconLoader.getIcon(104));
	// END KGU#952 2021-03-03
	// END KGU 2015-10-12
	// colors
	// START KGU#245 2018-07-02: Individual color buttons converted to an array
	protected ColorButton[] btnColors = new ColorButton[Element.colors.length];
	// END KGU#245 2018-07-02	
	// START KGU#872 2020-10-17: Enh. #872
	// display modes
	protected final JLabel lblSwitchComments = new JLabel(IconLoader.getIcon(102));
	protected final JLabel lblOperatorsC = new JLabel(IconLoader.getIcon(124));
	private static final String[] pathSwitchComments = {"menuDiagram", "menuDiagramSwitchComments"};
	private static final String[] pathOperatorsC = {"menuDiagram", "menuDiagramOperatorsC"};
	protected static final LangTextHolder ttSwitchComments = new LangTextHolder("Text and comments are exchanged!");
	protected static final LangTextHolder ttOperatorsC = new LangTextHolder("Operators are shown in C style!");
	protected static final LangTextHolder msgInactiveMode = new LangTextHolder("(Inactive display mode)");
	protected static final LangTextHolder msgMenuHint = new LangTextHolder("Menu item");
	// END KGU#872 2020-10-17
	
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
	// START KGU#686 2019-03-16: Enh. #56
	protected final JMenuItem popupAddBeforeTry = new JMenuItem("Try-Catch",IconLoader.getIcon(120));
	// END KGU#686 2019-03-16

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
	// START KGU#686 2019-03-16: Enh. #56
	protected final JMenuItem popupAddAfterTry = new JMenuItem("Try-Catch",IconLoader.getIcon(120));
	// END KGU#686 2019-03-16

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
	// START KGU#667 2019-02-26: Enh. #689 - summon the called subroutine for editing
	protected final JMenuItem popupEditSub = new JMenuItem("Edit subroutine ...", IconLoader.getIcon(21));
	// END KGU#667 2019-02-26
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
	
	// START KGU#705 2019-09-26: Enh. #738
	protected final JPopupMenu popupCode = new JPopupMenu();
	protected final JMenuItem popupCodeExport = new JMenuItem("Export ...", IconLoader.getIcon(32));
	protected final JMenuItem popupCodeHide = new JMenuItem("Hide code preview");
	protected final LangTextHolder ttPopupCodePreview = new LangTextHolder("Switches the code preview to % and sets it as favourite export language.");
	// END KGU#705 2019-09-26

	// START BOB 2020-05-25: restricted mode (suppresses code export / import)
	// START KGU#868 2020-06-03: Renamed for more clarity
	//private boolean restricted = false;
	/** suppresses code preview if true */
	private boolean noExportImport = false;
	// END KGU#868 2020-06-03
	// END BOB 2020-05-25
	
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
	// START KGU#866 2020-05-02: Issue #866
	//public enum SelectionExpandDirection { EXPAND_UP, EXPAND_DOWN };
	public enum SelectionExpandDirection { EXPAND_UP, EXPAND_DOWN, EXPAND_TOP, EXPAND_BOTTOM };
	// END KGU#866 2020-05-02
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
		private JScrollBar scrollBar;
		private boolean up;
		
		PageScrollAction(JScrollBar scrBar, boolean pageUp, String key)
		{
			super(key);
			scrollBar = scrBar;
			up = pageUp; 		
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			int value = scrollBar.getValue();
			int incr = scrollBar.getBlockIncrement(up ? -1 : 1);
			scrollBar.setValue(value + (up ? -incr : incr));
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
		// START KGU#245 2018-07-02: Serial buttons converted to array
		for (int i = 0; i < Element.colors.length; i++) {
			btnColors[i] = new ColorButton(Element.colors[i]);
		}
		// END KGU#245 2018-07-02

		// Setting up "this" ;-)
		addComponentListener(this);
		this.setDoubleBuffered(false);

		// Setting up the popup-menu with all submenus and shortcuts and actions
		createPopupMenu();

		// add toolbars
		//toolbar.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		this.setLayout(new AKDockLayout());
		createToolbars();

		sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.add(sp, AKDockLayout.CENTER);
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
		
		createDiagramArea();

		// START KGU#705 2019-09-23: Enh. #738
		pnlTabbed.setTabPlacement(SwingConstants.BOTTOM);
		pnlTabbed.add("Arrangerindex", scrollIndex);
		
		try {
			txtCode.setTabSize(4);
			// START KGU#964 2021-03-09: Issue #966 problem with dark themes
			//txtCode.setBackground(new Color(237, 237, 255));
			Color fg = txtCode.getForeground();
			// Text brighter than GRAY would be bad on CODE_PREVIEW_BACKGROUND, so check brightness
			float[] hsb = new float[3];
			Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), hsb);
			if (hsb[2] >= 0.5) {
				txtCode.setForeground(Color.DARK_GRAY);
			}
			txtCode.setBackground(CODE_PREVIEW_BACKGROUND);
			// END KGU#964 2021-03-09
			txtCode.getDocument().insertString(0, "", null);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		txtCode.setEditable(false);
		txtCode.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_ADD || e.getKeyCode() == KeyEvent.VK_SUBTRACT) && (e.isControlDown())) {
					controlCodeFont(e.getKeyCode() == KeyEvent.VK_ADD);
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {}
		});
		
		pnlTabbed.add("Code preview", scrollCode);
		// END KGU#705 2019-09-23
		
		// START KGU#305 2016-12-12: Enh. #305
		//createArrangerIndex();	// KGU#630 2019-01-12: Now done by ArrangerIndex itself
		// START KGU#705 2019-09-23: Enh. #738 Arranger index now tabbed together with Code preview
		//sp305.add(scrollIndex);
		sp305.add(pnlTabbed);
		// END KGU#705 2019-09-23
		// END KGU#305 2016-12-12

		// START KGU#705 2019-09-26: Enh. #738
		this.createPreviewPopupMenu();
		// END KGU#705 2019-09-26

		//container.add(scrolllist,AKDockLayout.SOUTH);
		createErrorList();
		sp.add(scrolllist);

		//diagram.setOpaque(true);
		diagram.addMouseListener(new PopupListener());
		txtCode.addMouseListener(new PopupListener());

		
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
		// START KGU#964 2021-03-09: Issue #966
		UIManager.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("lookAndFeel".equals(evt.getPropertyName())) {
					// It takes its time until the UI will be updated, so better ask for the default
					Color fg = UIManager.getColor("TextArea.foreground");
					if (fg == null) {
						// Only if that fails fall back to the current setting (may be obsolete)
						fg = Editor.this.txtCode.getForeground();
					}
					// Get the foreground (= text) brightness (in range 0.0 ... 0.1)
					float[] hsb = new float[3];
					Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), hsb);
					if (hsb[2] >= 0.5) {
						// Make the text dark enough for good contrast
						fg = Color.DARK_GRAY;
					}
					txtCode.setForeground(fg);
					// We insist on the chosen background, for highlighting consistency
					txtCode.setBackground(CODE_PREVIEW_BACKGROUND);
				}
			}
			
		});
		// END KGU#964 2021-03-09
	}

	// START KGU#705 2019-10-02: Enh. #738
	/**
	 * Incrementally enlarges or diminishes the code preview font.
	 * @param fontUp - {@code true} to enlarge the font, {@code false} to diminish it 
	 */
	protected void controlCodeFont(boolean fontUp) {
		Font font = txtCode.getFont();
		float increment = 2.0f;
		if (!fontUp) {
			increment = font.getSize() > 8 ? -2.0f : 0.0f;
		}
		Font newFont = font.deriveFont(font.getSize()+increment);
		txtCode.setFont(newFont);
	}
	// END KGU#705 2019-10-02

	/**
	 * Sets up the diagram editing area in fields {@link #scrollarea}, {@link #diagram}.
	 */
	private void createDiagramArea() {
		scrollarea.setBackground(Color.LIGHT_GRAY);
		scrollarea.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
		scrollarea.setWheelScrollingEnabled(true);
		scrollarea.setDoubleBuffered(true);
		scrollarea.setBorder(BorderFactory.createEmptyBorder());
		scrollarea.setViewportView(diagram);
		scrollarea.setFocusable(true);
		// START KGU#503 2018-03-13: Enh. #519 - for the zooming diagram needs permanent wheel notification 
		scrollarea.addMouseWheelListener(diagram);
		// END KGU#503 2018-03-13
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
		// START KGU#866 2020-05-02: Issue #866
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK ), SelectionExpandDirection.EXPAND_TOP);
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK ), SelectionExpandDirection.EXPAND_BOTTOM);
		// END KGU#866 2020-05-02
		// START KGU#177 2016-04-14: Enh. #158
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "PAGE_DOWN");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "PAGE_UP");
		// END KGU#177 2016-04-16
		// START KGU#177/KGU#629 2018-12-30: Enh. #158, #655
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_DOWN_MASK), "PAGE_RIGHT");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_DOWN_MASK), "PAGE_LEFT");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "HOME");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "END");
		// END KGU#177/KGU#629 2018-12-30
		// START KGU#294 2016-11-22: Enh. #284
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK), "FONT_UP");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK), "FONT_DOWN");
		// END KGU#294 2016-11-22
		// START KGU#347 2017-02-09: Enh. #344 additional key binding for redo
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "REDO");
		// END KGU#347 2017-02-09
		// START KGU#591 2018-10-02: Enh. #616
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK), "COPY");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK), "CUT");
		inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK), "PASTE");
		// END KGU#591 2018-10-02
		actMap.put(CursorMoveDirection.CMD_UP, new SelectionMoveAction(diagram, CursorMoveDirection.CMD_UP));
		actMap.put(CursorMoveDirection.CMD_DOWN, new SelectionMoveAction(diagram, CursorMoveDirection.CMD_DOWN));
		actMap.put(CursorMoveDirection.CMD_LEFT, new SelectionMoveAction(diagram, CursorMoveDirection.CMD_LEFT));
		actMap.put(CursorMoveDirection.CMD_RIGHT, new SelectionMoveAction(diagram, CursorMoveDirection.CMD_RIGHT));
		// END KGU#177 2016-04.-06
		// START KGU#206 2016-07-21: Enh. #197
		actMap.put(SelectionExpandDirection.EXPAND_UP, new SelectionExpandAction(diagram, SelectionExpandDirection.EXPAND_UP));
		actMap.put(SelectionExpandDirection.EXPAND_DOWN, new SelectionExpandAction(diagram, SelectionExpandDirection.EXPAND_DOWN));
		// END KGU#206 2016-07-21
		// START KGU#866 2020-05-02: Issue #866
		actMap.put(SelectionExpandDirection.EXPAND_TOP, new SelectionExpandAction(diagram, SelectionExpandDirection.EXPAND_TOP));
		actMap.put(SelectionExpandDirection.EXPAND_BOTTOM, new SelectionExpandAction(diagram, SelectionExpandDirection.EXPAND_BOTTOM));
		// END KGU#866 2020-05-02
		// START KGU#177 2016-04-14: Enh. #158
		actMap.put("PAGE_DOWN", new PageScrollAction(scrollarea.getVerticalScrollBar(), false, "PAGE_DOWN"));
		actMap.put("PAGE_UP", new PageScrollAction(scrollarea.getVerticalScrollBar(), true, "PAGE_UP"));
		// END KGU#177 2016-04-16
		// START KGU#177/KGU#629 2018-12-30: Enh. #158, #655
		actMap.put("PAGE_RIGHT", new PageScrollAction(scrollarea.getHorizontalScrollBar(), false, "PAGE_RIGHT"));
		actMap.put("PAGE_LEFT", new PageScrollAction(scrollarea.getHorizontalScrollBar(), true, "PAGE_LEFT"));
		actMap.put("HOME", new AbstractAction("HOME") {
			@Override
			public void actionPerformed(ActionEvent event) {
				scrollarea.getVerticalScrollBar().setValue(0);
				scrollarea.getHorizontalScrollBar().setValue(0);
				}
			});
		actMap.put("END", new AbstractAction("END") {
			@Override
			public void actionPerformed(ActionEvent event) {
				scrollarea.getVerticalScrollBar().setValue(diagram.getHeight());
				scrollarea.getHorizontalScrollBar().setValue(diagram.getWidth());
				}
			});
		// END KGU#177/KGU#629 2018-12-30
		// START KGU#294 2016-11-22: Enh. #284
		actMap.put("FONT_DOWN", new FontResizeAction(diagram, "FONT_DOWN"));
		actMap.put("FONT_UP", new FontResizeAction(diagram, "FONT_UP"));
		// END KGU#294 2016-11-22
		// START KGU#347 2017-02-09: Enh. #344 additional key binding for redo
		actMap.put("REDO", new AbstractAction("REDO") { public void actionPerformed(ActionEvent event) { diagram.redoNSD(); doButtons(); }});
		// END KGU#347 2017-02-09
		// START KGU#591 2018-10-02: Enh. #616
		actMap.put("COPY", new AbstractAction("COPY") { public void actionPerformed(ActionEvent event) { if (diagram.canCopy()) {diagram.copyNSD(); doButtons(); }}});
		actMap.put("CUT", new AbstractAction("CUT") { public void actionPerformed(ActionEvent event) { if (diagram.canCut()) {diagram.cutNSD(); doButtons(); }}});
		actMap.put("PASTE", new AbstractAction("PASTE") { public void actionPerformed(ActionEvent event) { if (diagram.canPaste()) {diagram.pasteNSD(); doButtons(); }}});
		// END KGU#591 2018-10-02
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
	}

	/**
	 * Sets up the toolbars with all buttons and actions
	 */
	private void createToolbars() {
		MyToolbar toolbar = newToolBar("New, open, save", true);
		
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
//		toolbar.add(btnUnboxed);
//		btnUnboxed.setFocusable(false);
//		btnUnboxed.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setUnboxed(btnUnboxed.isSelected()); doButtons(); } } );
		// END KGU#493 2018-02-14

		// START KGU#493 2018-02-12: Issue #510 - toolbars Before + After merged
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
		// END KGU#493 2018-02-12
		// START KGU#686 2019-03-16: Enh. #56
		toolbar.add(btnAfterTry, false);
		btnAfterTry.setFocusable(false);
		btnAfterTry.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Try(),"Add new try ...","", (event.getModifiers() & ActionEvent.SHIFT_MASK) == 0); doButtons(); } } );
		// END KGU#686 2019-03-16
		
		toolbar = newToolBar("Colors ...", false);

		// Colors
		//toolbar.addSeparator();
		// START KGU#245 2018-07-02: Serial buttons converted to array
		ActionListener colorButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object button = e.getSource(); 
				if (button instanceof ColorButton) {
					diagram.setColor(((ColorButton)button).getColor());
					doButtons();
				}
			}
		};
		for (int i = 0; i < Element.colors.length; i++) {
			toolbar.add(btnColors[i]);
			{
				btnColors[i].addActionListener(colorButtonListener);
			}
			btnColors[i].setFocusable(false);
		}
		// END KGU#245 2018-07-02

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
//		toolbar.add(btnDisable);
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
		// START KGU#952 2021-03-03: Issue #954 modified functionality
		//btnDropBrk.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.clearBreakpoints(); } } );
		btnDropBrk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				diagram.disableBreakpoints(((JToggleButton)event.getSource()).isSelected()); 
			} 
		});
		// END KGU#952 2021-03-03
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

		// Display mode indicators
		// START KGU#872 2020-10-17: Enh. 872
		toolbar = newToolBar("Display mode", true);
		
		toolbar.add(lblSwitchComments);
		toolbar.add(new JLabel(" "));	// FIXME Better way to ensure a sensible gap, e.g. insets?
		toolbar.add(lblOperatorsC);
		lblSwitchComments.setFocusable(false);
		lblOperatorsC.setFocusable(false);
		// END KGU#872 2020-10-17

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
	}

	/**
	 * Sets up the pop-up menus with all submenus and shortcuts and actions
	 */
	private void createPopupMenu() {
		// FIXME: This method becomes deprecated with Java 10! Use getMenuShortcutKeyMaskEx() instead in future.
		// OS-dependent key mask for menu shortcuts
		int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		popup.add(popupCut);
		popupCut.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.cutNSD(); doButtons(); } } );
		popupCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,menuShortcutKeyMask));

		popup.add(popupCopy);
		popupCopy.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyNSD(); doButtons(); } } );
		popupCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,menuShortcutKeyMask));

		popup.add(popupPaste);
		popupPaste.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.pasteNSD(); doButtons(); } } );
		popupPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,menuShortcutKeyMask));

		popup.addSeparator();

		popup.add(popupAdd);
		popupAdd.setIcon(IconLoader.getIcon(18));

		popupAdd.add(popupAddBefore);
		popupAddBefore.setIcon(IconLoader.getIcon(19));

		popupAddBefore.add(popupAddBeforeInst);
		popupAddBeforeInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",false); doButtons(); } } );
		popupAddBeforeInst.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforeAlt);
		popupAddBeforeAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,false); doButtons(); } } );
		popupAddBeforeAlt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforeCase);
		popupAddBeforeCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,false); doButtons(); } } );
		popupAddBeforeCase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforeFor);
		popupAddBeforeFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,false); doButtons(); } } );
		popupAddBeforeFor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforeWhile);
		popupAddBeforeWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,false); doButtons(); } } );
		popupAddBeforeWhile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforeRepeat);
		popupAddBeforeRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,false); doButtons(); } } );
		popupAddBeforeRepeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforeForever);
		popupAddBeforeForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",false); doButtons(); } } );
		popupAddBeforeForever.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, menuShortcutKeyMask | KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforeCall);
		popupAddBeforeCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",false); doButtons(); } } );
		popupAddBeforeCall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforeJump);
		popupAddBeforeJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",false); doButtons(); } } );
		popupAddBeforeJump.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, KeyEvent.SHIFT_DOWN_MASK));

		popupAddBefore.add(popupAddBeforePara);
		popupAddBeforePara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",false); doButtons(); } } );
		popupAddBeforePara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, menuShortcutKeyMask | KeyEvent.SHIFT_DOWN_MASK));

		// START KGU#686 2019-03-16: Enh. #56
		popupAddBefore.add(popupAddBeforeTry);
		popupAddBeforeTry.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Try(),"Add new try-catch ...","",false); doButtons(); } } );
		popupAddBeforeTry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, menuShortcutKeyMask | KeyEvent.SHIFT_DOWN_MASK));
		// END KGU#686 2019-03-16

		popupAdd.add(popupAddAfter);
		popupAddAfter.setIcon(IconLoader.getIcon(20));

		popupAddAfter.add(popupAddAfterInst);
		popupAddAfterInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",true); doButtons(); } } );
		popupAddAfterInst.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

		popupAddAfter.add(popupAddAfterAlt);
		popupAddAfterAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,true); doButtons(); } } );
		popupAddAfterAlt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

		popupAddAfter.add(popupAddAfterCase);
		popupAddAfterCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,true); doButtons(); } } );
		popupAddAfterCase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));

		popupAddAfter.add(popupAddAfterFor);
		popupAddAfterFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,true); doButtons(); } } );
		popupAddAfterFor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

		popupAddAfter.add(popupAddAfterWhile);
		popupAddAfterWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,true); doButtons(); } } );
		popupAddAfterWhile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

		popupAddAfter.add(popupAddAfterRepeat);
		popupAddAfterRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,true); doButtons(); } } );
		popupAddAfterRepeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));

		popupAddAfter.add(popupAddAfterForever);
		popupAddAfterForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",true); doButtons(); } } );
		popupAddAfterForever.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, menuShortcutKeyMask));

		popupAddAfter.add(popupAddAfterCall);
		popupAddAfterCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",true); doButtons(); } } );
		popupAddAfterCall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));

		popupAddAfter.add(popupAddAfterJump);
		popupAddAfterJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",true); doButtons(); } } );
		popupAddAfterJump.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

		popupAddAfter.add(popupAddAfterPara);
		popupAddAfterPara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",true); doButtons(); } } );
		popupAddAfterPara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, menuShortcutKeyMask));

		// START KGU#686 2019-03-16: Enh. #56
		popupAddAfter.add(popupAddAfterTry);
		popupAddAfterTry.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Try(),"Add new try-catch ...","",true); doButtons(); } } );
		popupAddAfterTry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, menuShortcutKeyMask));
		// END KGU#686 2019-03-16

		popup.add(popupEdit);
		popupEdit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editNSD(); doButtons(); } } );
		popupEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

		popup.add(popupDelete);
		popupDelete.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.deleteNSD(); doButtons(); } } );
		popupDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

		popup.addSeparator();

		popup.add(popupMoveUp);
		popupMoveUp.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.moveUpNSD(); doButtons(); } } );
		popupMoveUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, menuShortcutKeyMask));

		popup.add(popupMoveDown);
		popupMoveDown.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.moveDownNSD(); doButtons(); } } );
		popupMoveDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, menuShortcutKeyMask));
		
		// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
		popup.add(popupTransmute);
		popupTransmute.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.transmuteNSD(); doButtons(); } } );
		popupTransmute.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, menuShortcutKeyMask));
		// END KGU#199 2016-07-06

		// START KGU#365 2017-03-27: Enh. #380 - conversion of sequence in a subroutine
		popup.add(popupOutsource);
		popupOutsource.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.outsourceNSD(); doButtons(); } } );
		popupOutsource.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, menuShortcutKeyMask));
		// END KGU#365 2017-03-27

		// START KGU#667 2019-02-26: Enh. #689 - summons the called subroutine for editing
		popup.add(popupEditSub);
		popupEditSub.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editSubNSD(); doButtons(); } } );
		popupEditSub.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, menuShortcutKeyMask));
		// END KGU#667 2019-02-26

		// START KGU#123 2016-01-03: Enh. #87 - New menu items (addressing Bug #65)
		popup.addSeparator();

		popup.add(popupCollapse);
		popupCollapse.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.collapseNSD(); doButtons(); } } );
		popupCollapse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0));

		popup.add(popupExpand);
		popupExpand.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.expandNSD(); doButtons(); } } );
		popupExpand.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0));
		// END KGU#123 2016-01-03

		// START KGU#277 2016-10-13: Enh. #270
		popup.add(popupDisable);
		popupDisable.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.disableNSD(); doButtons(); } } );
		popupDisable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, menuShortcutKeyMask));
		// END KGU#123 2016-10-13

		// START KGU#43 2015-10-12 Add a possibility to set or unset a checkpoint on the selected Element
		popup.addSeparator();

		popup.add(popupBreakpoint);
		popupBreakpoint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleBreakpoint(); doButtons(); } }); 
		popupBreakpoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, menuShortcutKeyMask | KeyEvent.SHIFT_DOWN_MASK));
		// END KGU#43 2015-10-12

		// START KGU#213 2016-08-02: Enh. #215 - new breakpoint feature
		popup.add(popupBreakTrigger);
		popupBreakTrigger.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editBreakTrigger(); doButtons(); } }); 
		popupBreakTrigger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, menuShortcutKeyMask | KeyEvent.ALT_DOWN_MASK));
		// END KGU#213 2016-08-02

	}
	
	// START KGU#705 2019-09-26: Enh. #738
	private void createPreviewPopupMenu()
	{
		Vector<GENPlugin> plugins = Menu.generatorPlugins;
		if (plugins.isEmpty()) {
			// Editor must retrieve the plugins itself, obvioulsy
			String fileName = "generators.xml";
			BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream(fileName));
			GENParser genp = new GENParser();
			plugins = genp.parse(buff);
			try { buff.close();	} catch (IOException e) {}
		}
		ImageIcon defaultIcon = IconLoader.getIcon(87);
		String tooltip = ttPopupCodePreview.getText();
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object item = e.getSource();
				if (item instanceof JMenuItem) {
					diagram.setPreferredGeneratorName(((JMenuItem)item).getText());
				}
			}
		};
		for (GENPlugin plugin: plugins)
		{
			ImageIcon icon = defaultIcon;	// The default icon
			if (plugin.icon != null && !plugin.icon.isEmpty()) {
				try {
					URL iconFile = this.getClass().getResource(plugin.icon);
					if (iconFile != null) {
						icon = IconLoader.getIconImage(this.getClass().getResource(plugin.icon));
					}
				}
				catch (Exception ex) {}
			}
			JCheckBoxMenuItem pluginItem = new JCheckBoxMenuItem(plugin.title, icon);
			pluginItem.setName("code_" + plugin.title);
			popupCode.add(pluginItem);
			pluginItem.setToolTipText(tooltip.replace("%", plugin.title));
			pluginItem.addActionListener(listener);
		}
		this.ttPopupCodePreview.addLangEventListener(new LangEventListener() {
			@Override
			public void LangChanged(LangEvent evt) {
				String tooltip = ttPopupCodePreview.getText();
				for (Component comp: popupCode.getComponents()) {
					String name = comp.getName();
					if (comp instanceof JCheckBoxMenuItem) {
						((JMenuItem)comp).setToolTipText(tooltip.replace("%", name.substring(5)));						
					}
				}
			}});
		
		popupCode.addSeparator();
		
		popupCodeExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,(java.awt.event.InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
		popupCodeExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String language = diagram.getPreferredGeneratorName();
				for (GENPlugin plugin: Menu.generatorPlugins) {
					if (plugin.title.equals(language)) {
						diagram.export(plugin.className, plugin.options);
						break;
					}
				}
			}
		});
		
		popupCodeHide.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		popupCodeHide.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				diagram.setCodePreview(false);
			}
		});
		
		popupCode.add(popupCodeExport);
		popupCode.addSeparator();
		popupCode.add(popupCodeHide);
	}
	// END KGU#705 2019-09-26

	/**
	 * Sets up the error list in fields {@link #scrolllist}, {@link #errorlist}.
	 */
	private void createErrorList() {
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
	}

	public void doButtons()
	{
		if(NSDControll!=null)
		{
			NSDControll.doButtons();
		}
	}	
	
	/**
	 * Checks and updates the visibility / usability of all Editor-specific buttons, menu items
	 * and other controls
	 */
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
		// START KGU#686 2019-03-16: Enh. #56
		btnAfterTry.setEnabled(condition);
		// END KGU#686 2019-03-16

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
		// START KGU#686 2019-03-16: Enh. #56
		popupAddBeforeTry.setEnabled(condition);
		// END KGU#686 2019-03-16

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
		// START KGU#686 2019-03-16: Enh. #56
		popupAddAfterTry.setEnabled(condition);
		// END KGU#686 2019-03-16
		
		popupAddBeforeForever.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupAddBeforeJump.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupAddBeforePara.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupAddAfterForever.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupAddAfterJump.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupAddAfterPara.setVisible(!Element.E_REDUCED_TOOLBARS);

		
		// colors
		// START KGU#245 2018-07-02: Serial buttons converted to array
		for (int i = 0; i < Element.colors.length; i++) {
			btnColors[i].setEnabled(condition);
		}
		// END KGU#245 2018-07-02

		// START KGU#123 2016-01-03: Enh. #87 - We allow multiple selection for collapsing
		// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
		btnCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());
		btnExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());			
		// END KGU#123 2016-01-03

		// executor
		// START KGU#277 2016-10-13: Enh. #270
		btnDisable.setEnabled(condition && !(selected instanceof Subqueue) || diagram.selectedIsMultiple());
		// END KGU#277 2016-01-13
		// START KGU#952 2021-03-03: Issue #954 New breakpoint functionality
		this.btnDropBrk.setSelected(!Element.E_BREAKPOINTS_ENABLED);
		// END KGU#952 2021-03-03

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
		// START KGU#667 2019-02-26 Enh.#689
		popupEditSub.setEnabled(diagram.canEditSub());
		// END KGU#667 2019-02-26
		// START KGU#770 2021-01-27: Enh. #917
		if (diagram.canEditSub()) {
			if (selected != null && selected instanceof Root) {
				popupEditSub.setText(Menu.msgEditIncludable.getText());
				popupEditSub.setIcon(IconLoader.getIcon(71));
			}
			else {
				popupEditSub.setText(Menu.msgEditSubroutine.getText());
				popupEditSub.setIcon(IconLoader.getIcon(21));
			}
		}
		// END KGU#770 2021-01-27
		popupTransmute.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupOutsource.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupEditSub.setVisible(!Element.E_REDUCED_TOOLBARS);
		
		// START KGU#123 2016-01-03: Enh. #87 - We allow multiple selection for collapsing
		// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
		popupCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());
		popupExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());			
		// END KGU#123 2016-01-03
		// START KGU#277 2016-10-13: Enh. #270
		popupDisable.setEnabled(condition && !(selected instanceof Subqueue) || diagram.selectedIsMultiple());
		// END KGU#277 2016-01-13
		popupCollapse.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupExpand.setVisible(!Element.E_REDUCED_TOOLBARS);
		popupDisable.setVisible(!Element.E_REDUCED_TOOLBARS);

		// executor
		// START KGU#143 2016-01-21: Bugfix #114 - breakpoints need a more generous enabling policy
		//popupBreakpoint.setEnabled(diagram.canCutCopy());	// KGU 2015-10-12: added
		// START KGU#177 2016-07-06: Enh. #158 - Collateral damage mended
		//popupBreakpoint.setEnabled(diagram.canCopy());
		// START KGU#686 2019-03-17: Enh. #56 It doesn't make sense to place breakpoints on endless loops or try elements
		//popupBreakpoint.setEnabled(diagram.canCopyNoRoot());
		popupBreakpoint.setEnabled(diagram.canSetBreakpoint());
		// END KGU#686 2019-03-17
		// END KGU#177 2016-07-06
		// END KGU#143 2016-01-21
		// START KGU#213 2016-08-02: Enh. #215 - breakpoint control enhanced
		// START KGU#686 2019-03-17: Enh. #56 It doesn't make sense to place breakpoints on endless loops or try elements
		//popupBreakTrigger.setEnabled(diagram.canCopyNoRoot() && !diagram.selectedIsMultiple());
		popupBreakTrigger.setEnabled(diagram.canSetBreakpoint() && !diagram.selectedIsMultiple());
		// END KGU#686 2019-03-17
		// END KGU#213 2016-08-02
		popupBreakTrigger.setVisible(!Element.E_REDUCED_TOOLBARS);
		
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
		//btnUnboxed.setSelected(diagram.isUnboxed());
		// END KGU#493 2018-02-14
		btnFunction.setSelected(diagram.isSubroutine());
		btnProgram.setSelected(diagram.isProgram());
		btnInclude.setSelected(diagram.isInclude());
		
		// START KGU#872 2020-10-17: Enh. #872
		// START KGU#887 2020-12-11: Bugfix #885
		//lblSwitchComments.setEnabled(Element.E_TOGGLETC);
		lblSwitchComments.setEnabled(Element.isSwitchTextCommentMode());
		// END KGU#887 2020-12-11
		lblSwitchComments.setToolTipText(buildMessageWithMenuRef(
				lblSwitchComments.isEnabled() ? ttSwitchComments : msgInactiveMode,
				Menu.getLocalizedMenuPath(pathSwitchComments, null)));
		// START KGU#887 2020-12-11: Bugfix #885
		//lblOperatorsC.setEnabled(Element.E_SHOW_C_OPERATORS && !Element.E_TOGGLETC);
		// START KGU#902 2021-01-01: Enh. #903 May now also work in SwitchTextComment mode
		//lblOperatorsC.setEnabled(Element.E_SHOW_C_OPERATORS 
		//		&& (!Element.isSwitchTextCommentMode()));
		lblOperatorsC.setEnabled(Element.E_SHOW_C_OPERATORS && Element.E_VARHIGHLIGHT);
		// END KGU#902 2021-01-01
		// END KGU#887 2020-12-11
		lblOperatorsC.setToolTipText(buildMessageWithMenuRef(
				lblOperatorsC.isEnabled() ? ttOperatorsC : msgInactiveMode,
				Menu.getLocalizedMenuPath(pathOperatorsC, null)));
		// END KGU#872 2020-10-17

		// DIN
		ImageIcon iconFor = IconLoader.getIcon(Element.E_DIN ? 74 : 53);
		btnAfterFor.setIcon(iconFor);
		popupAddBeforeFor.setIcon(iconFor);
		popupAddAfterFor.setIcon(iconFor);
		
		for (Component comp: popupCode.getComponents()) {
			if (comp instanceof JCheckBoxMenuItem) {
				((JCheckBoxMenuItem)comp).setSelected(comp.getName().equals("code_" + diagram.getPreferredGeneratorName()));
			}
		}
		
		//scrollarea.revalidate();
		//scrollarea.setViewportView(diagram);
		
		// analyser
		
		if (Element.E_ANALYSER == true)
		{
			if (sp.getDividerSize() == 0)
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
		
		// START KGU#305 2016-12-12: Enh. 305, KGU#705 2019-09-23: Enh. #738
		// arranger index and code preview
		doButtonsTabbedPane();
		// END KGU#305 2016-12-12

                // Intends to ensure the original toolbar order after redocking?
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

	/**
	 * Checks and updates the visibility / usability of the tabbed pane including all Arranger-
	 * Index-specific buttons, menu items, and other controls as well as the code preview page 
	 */
	private void doButtonsTabbedPane() {
		// START KGU#626 2019-01-01: Enh. #657
		//if (diagram.showingArrangerIndex() && !diagrams.isEmpty())
		// START KGU#705 2019-09-23: Enh. #738
		//if (diagram.showingArrangerIndex() && !arrangerIndex.isEmpty())
		boolean showIndex = diagram.showingArrangerIndex() && !arrangerIndex.isEmpty();
		boolean showPreview = diagram.showingCodePreview(); 
		if (showIndex || showPreview)
		// END KGU#705 2019-09-23
		// END KGU#626 2019-01-01
		{
			if (sp305.getDividerSize()==0)
			{
				// START KGU#705 2019-09-23: Enh. #738
				//sp305.remove(scrollIndex);
				//sp305.add(scrollIndex);
				sp305.remove(pnlTabbed);
				sp305.add(pnlTabbed);
				// END KGU#705 2019-09-23
			}
			//START KGU#705 2019-09-23: Enh. #738
			//scrollIndex.setVisible(true);
			//scrollIndex.setViewportView(arrangerIndex);
			boolean wasEnabledIndex = pnlTabbed.isEnabledAt(0);
			boolean wasEnabledPreview = pnlTabbed.isEnabledAt(1);
			pnlTabbed.setEnabledAt(0, showIndex);
			if (showIndex) {
				scrollIndex.setViewportView(arrangerIndex);
			}
			pnlTabbed.setEnabledAt(1, showPreview);
			if (!wasEnabledIndex && showIndex || !showPreview) {
				pnlTabbed.setSelectedIndex(0);
			}
			else if (!wasEnabledPreview && showPreview || !showIndex) {
				pnlTabbed.setSelectedIndex(1);
			}
			// END KGU#705 2019-09-23
			sp305.setDividerSize(5);
			if (showIndex) {
				scrollIndex.revalidate();
			}
		}
		else
		{
			// START KGU#705 2019-09-23: Enh. #738
			//scrollIndex.setVisible(false);
			//sp305.remove(scrollIndex);
			pnlTabbed.setEnabledAt(0, showIndex);
			pnlTabbed.setEnabledAt(1, showPreview);
			sp305.remove(pnlTabbed);
			// END KGU#705 2019-09-23
			sp305.setDividerSize(0);
		}
		// END KGU#305 2016-12-12
		
		// START KGU#318 2017-01-05: Enh. #319
		arrangerIndex.doButtonsLocal();
		// END KGU#626 2019-01-03
	}


	@Override
	public void updateColors() 
	{	
		// START KGU#245 2018-07-02: Serial buttons converted to array
		for (int i = 0; i < Element.colors.length; i++) {
			btnColors[i].setColor(Element.colors[i]);
		}
		// END KGU#245 2018-07-02
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
			Element closest = null;
			if (e.getSource() == txtCode) {
				try {
					int line = txtCode.getLineOfOffset(txtCode.getCaretPosition());
					closest = diagram.identifyElementForCodeLine(line);
				} catch (BadLocationException ex) {}
			}
			if (closest != null && e.getButton() == MouseEvent.BUTTON1) {
				diagram.selectElement(closest);
				if (e.getClickCount() == 2) {
					diagram.editNSD();
					doButtons();
				}
			}
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
				Component comp = e.getComponent();
				if (comp == diagram) {
					popup.show(comp, e.getX(), e.getY());
				}
				// END KGU#318 2017-01-05
				// START KGU#705 2019-09-26: Enh. #738
				// START KGU#868 2020-06-03: Bugfix #868
				//else if (comp == txtCode) {
				else if (comp == txtCode && !noExportImport) {
				// END KGU#868 2020-06-03
					popupCode.show(comp, e.getX(), e.getY());
				}
				// END KGU#705 2019-09-26
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
	// START KGU#626 2019-01-01: Enh. #657 - Now we pass a Group collection and rebuild the tree
//	public void updateArrangerIndex(Vector<Root> _diagrams)
//	{
//		boolean wasEmpty = this.diagrams.isEmpty();
//		this.diagrams.clear();
//		if (_diagrams != null) {
//			for (int i = 0; i < _diagrams.size(); i++) {
//				Root aRoot = _diagrams.get(i);
//				this.diagrams.addElement(aRoot);
//			}
//		}
//		if (this.diagrams.isEmpty()) {
//			this.scrollIndex.setVisible(false);
//		}
//		else if (wasEmpty) {
//			this.scrollIndex.setVisible(true);
//		}
//		this.scrollIndex.repaint();
//		this.scrollIndex.validate();
//		this.doButtonsLocal();
//	}
	/**
	 * Rebuilds the Arranger index from scratch according to the group information
	 * given with {@code _groups}.
	 * @param _groups - sorted list of all currently held {@link Group} objects
	 */
	public void updateArrangerIndex(Vector<Group> _groups)
	{
		boolean wasEmpty = arrangerIndex.isEmpty();
		arrangerIndex.update(_groups);
		if (arrangerIndex.isEmpty()) {
			scrollIndex.setVisible(false);
		}
		else if (wasEmpty) {
			scrollIndex.setVisible(true);
		}
		this.scrollIndex.repaint();
		this.scrollIndex.validate();
		this.doButtonsLocal();
	}
	// END KGU#626 2019-01-01

	// START KGU#626 2019-01-04: Enh. #657
	/**
	 * Lightweight update of the Arranger Index (just some modified markers to be refreshed)
	 */
	public void repaintArrangerIndex() {
		this.scrollIndex.repaint();
		this.scrollIndex.validate();
		this.doButtonsTabbedPane();
	}
	// END KGU#626 2019-01-04
	// END KGU#305 2016-12-12

	// START KGU#646 2019-02-05: Bugfix #674 - Update popup menu L&F (isn't member of the component tree)
	@Override
	public void updateUI()
	{
		super.updateUI();
		// Cater for the look and feel update of the popup menu.
		// START KGU#705 209-09-26: Enh. #738
		//for (Component comp: new Component[] {this.popup}) {
		for (Component comp: new Component[] {this.popup, this.popupCode}) {
		// END KGU#708 2019-09-26
			if (comp != null) {
				try {
					javax.swing.SwingUtilities.updateComponentTreeUI(comp);
				}
				catch (Exception ex) {
					System.err.println("L&F problem with " + comp);
				}
			}
		}
	}
	// END KGU#646 2019-02-05

	// START BOB 2020-05-25
	@Override
	public boolean isRestricted() {
		return noExportImport;
	}

	/**
	 * Controls whether GUI elements providing code import or export be
	 * suppressed
	 * @param restricted - true to disable menu items offering export
	 */
	// START KGU#868 2020-06-03: Bugfix #868
//	public void setRestricted(boolean restricted) {
//		this.restricted = restricted;
//		popupCode.setVisible(!restricted);
//	}
	public void hideExportImport()
	{
		noExportImport = true;
		popupCode.setVisible(false);
		diagram.setCodePreview(false);
		// START KGU#870 2020-06-06: Bugfix #868, #870 group export had been forgotten
		arrangerIndex.hideExportImport();
		// END KGU#870 2020-06-06
	}
	// END KGU#868 2020-06-03
	// END BOB 2020-05-25
	
	// START KGU#872 2020-10-17: Enh. #872
	/**
	 * Builds a message with with a menu path hint from a {@link LangTextHolder} and the
	 * given caption array.
	 * @param _label - the {@link LangTextHolder} providing the translated text template
	 * @param _menuPath - array of translated menu captions to be appended as path
	 * @return the composed string
	 */
	private String buildMessageWithMenuRef(LangTextHolder _label, String[] _menuPath)
	{
		StringBuilder msg = new StringBuilder(_label.getText());
		msg.append(" → ");
		msg.append(msgMenuHint.getText());
		msg.append(" \"");
		for (int i = 0; i < _menuPath.length; i++)
		{
			if (i > 0) {
				msg.append(" ► ");
			}
			msg.append(_menuPath[i]);
		}
		msg.append('\"');
		return msg.toString();
	}
	// END KGU#239 2016-08-12


}

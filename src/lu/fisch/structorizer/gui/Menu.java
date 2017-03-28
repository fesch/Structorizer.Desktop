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
 *      Description:    This class is responsible for setting up the entire menubar.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007.12.30      First Issue
 *      Bob Fisch       2008.04.12      Adapted for Generator plugin
 *      Kay Gürtzig     2015.11.03      Additions for FOR loop enhancement (KGU#3)
 *      Kay Gürtzig     2015.11.22      Adaptations for handling selected non-empty Subqueues (KGU#87)
 *      Kay Gürtzig     2015.11.25      Error labels error13_3 (KGU#78), error15 (KGU#2), and error_16_x added
 *      Kay Gürtzig     2015.11.26      New error label error14_3 (KGU#3) added
 *      Kay Gürtzig     2015.11.28      New error label error17 (KGU#47) added
 *      Kay Gürtzig     2016.01.03/04   Enh. #87: New menu items and buttons for collapsing/expanding 
 *      Kay Gürtzig     2016.01.21      Bugfix #114: Editing restrictions during execution, breakpoint menu item
 *      Kay Gürtzig     2016.01.22      Bugfix for Enh. #38 (addressing moveUp/moveDown, KGU#143 + KGU#144).
 *      Kay Gürtzig     2016.04.01      Issue #144: Favourite code export menu item, #142 accelerator keys added
 *      Kay Gürtzig     2016.04.06      Enh. #158: Key bindings for editNSD, moveUpNSD, moveDownNSD
 *      Kay Gürtzig     2016.04.12      Enh. #137: New message error16_7 introduced.
 *      Kay Gürtzig     2016.04.24      Fix #173: Mnemonics for menus Diagram and Help had been compromised
 *      Kay Gürtzig     2016.07.07      Enh. #188: New menu item "wand" for element conversion (KGU#199)
 *      Kay Gürtzig     2016.07.22      Enh. #199: New help menu item "user guide" for element conversion (KGU#208)
 *      Kay Gürtzig     2016.07.28      Enh. #206: New Dialog message text holders
 *      Kay Gürtzig     2016.07.31      Enh. #128: New Diagram menu item "Comments + text"
 *      Kay Gürtzig     2016.08.02      Enh. #215: menuDiagramBreakTrigger added, new message text holders
 *      Kay Gürtzig     2016.08.03      Enh. #222: New possibility to load translations from a text file
 *      Kay Gürtzig     2016.08.04      Most persistent attributes set to final
 *      Bob Fisch       2016.08.08      Redesign of the Language choice mechanisms (#225 fixed by Kay Gürtzig)
 *      Kay Gürtzig     2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions) 
 *      Kay Gürtzig     2016.08.12      Enh. #231: Analyser checks re-organised to arrays for easier maintenance
 *                                      two new checks introduced (variable name collisions)
 *      Kay Gürtzig     2016.09.01      Bugfix #233: CASE insertion by F10 had been averted by menu bar
 *      Kay Gürtzig     2016.09.04      Structural redesign for menuPreferencesLanguage
 *      Kay Gürtzig     2016.09.15      Issue #243: Additional text holders for forgotten message box texts
 *      Kay Gürtzig     2016.09.22      New text holder / messages for Analyser
 *      Kay Gürtzig     2016.09.26/03   Enh. #253: Refactoring support
 *      Kay Gürtzig     2016.10.11      Enh. #267: error15 renamed to error15_1, new error15_2
 *      Kay Gürtzig     2016.10.13      Enh. #270: Menu items for the disabling of elements
 *      Kay Gürtzig     2016.10.16      Enh. #272: Menu items for the replacement of Turtleizer command sets
 *      Kay Gürtzig     2016.11.17      Bugfix #114: Prerequisites for editing during execution revised
 *      Kay Gürtzig     2016.12.02      Enh. #300: New menu entry to enable online update retrieval
 *      Kay Gürtzig     2016.12.14      Enh. #305: New menu entry to enable/disable Arranger index
 *                                      KGU#310: New Debug menu
 *      Kay Gürtzig     2016.12.17      Enh. #267: New Analyser error15_3
 *      Kay Gürtzig     2017.01.07      Enh. #329: New Analyser error21
 *      Kay Gürtzig     2017.03.15      Enh. #354: All code import merged to a single menu item
 *      Kay Gürtzig     2017.03.23      Enh. #380: New menu entry to convert a sequence in a subroutine
 *      Kay Gürtzig     2017.03.28      Enh. #387: New menu entry "Save All"
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.helpers.*;
import lu.fisch.structorizer.io.INIFilter;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangMenuBar;
import lu.fisch.structorizer.locales.Locales;
import lu.fisch.structorizer.locales.Translator;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.utils.StringList;

@SuppressWarnings("serial")
public class Menu extends LangMenuBar implements NSDController
{
	private Diagram diagram = null;
	private NSDController NSDControl = null;

	// Menu "File"
	protected final JMenu menuFile = new JMenu("File");
	// Submenus of "File"
	protected final JMenuItem menuFileNew = new JMenuItem("New", IconLoader.ico001);
	protected final JMenuItem menuFileSave = new JMenuItem("Save", IconLoader.ico003);
	protected final JMenuItem menuFileSaveAs = new JMenuItem("Save As ...", IconLoader.ico003);
	// START KGU#373 2017-03-28: Enh. #387
	protected final JMenuItem menuFileSaveAll = new JMenuItem("Save All", IconLoader.ico069);
	//  END KGU#373 2017-03-28
	protected final JMenuItem menuFileOpen = new JMenuItem("Open ...", IconLoader.ico002);
	protected final JMenuItem menuFileOpenRecent = new JMenu("Open Recent File");
	protected final JMenu menuFileExport = new JMenu("Export");
	// Submenu of "File -> Export"
	protected final JMenu menuFileExportPicture = new JMenu("Picture");
	protected final JMenuItem menuFileExportPicturePNG = new JMenuItem("PNG ...",IconLoader.ico032);
	protected final JMenuItem menuFileExportPicturePNGmulti = new JMenuItem("PNG (multiple) ...",IconLoader.ico032);
	protected final JMenuItem menuFileExportPictureEMF = new JMenuItem("EMF ...",IconLoader.ico032);
	protected final JMenuItem menuFileExportPictureSWF = new JMenuItem("SWF ...",IconLoader.ico032);
	protected final JMenuItem menuFileExportPicturePDF = new JMenuItem("PDF ...",IconLoader.ico032);
	protected final JMenuItem menuFileExportPictureSVG = new JMenuItem("SVG ...",IconLoader.ico032);
	protected final JMenu menuFileExportCode = new JMenu("Code");
	// START KGU#171 2016-04-01: Enh. #144 - new menu item for Favourite Code Export
	protected static final LangTextHolder lbFileExportCodeFavorite = new LangTextHolder("Export as % Code");	// Label template for translation
	protected final JMenuItem menuFileExportCodeFavorite = new JMenuItem("Export Fav. Code", IconLoader.ico004);
	// END KGU#171 2016-04-01
	protected final JMenu menuFileImport = new JMenu("Import");
	// Submenu of "File -> Import"
	// START KGU#354 2017-03-14: Enh. #354 We use one unified menu item for all code import now
	//protected final JMenuItem menuFileImportPascal = new JMenuItem("Pascal Code ...",IconLoader.ico004);
	//protected final JMenuItem menuFileImportC = new JMenuItem("ANSI-C Code ...",IconLoader.ico004);
	protected final JMenuItem menuFileImportCode = new JMenuItem("Source Code ...", IconLoader.ico004);
	// END KGU#354 2017-03-14

	// START KGU#2 2015-11-19: New menu item to have the Arranger present the diagram
	protected final JMenuItem menuFileArrange = new JMenuItem("Arrange", IconLoader.ico105);
	// END KGU#2 2015-11-19
	protected final JMenuItem menuFilePrint = new JMenuItem("Print ...",IconLoader.ico041);
    // START BOB 2016-08-02
	protected final JMenuItem menuFileTranslator = new JMenuItem("Translator", IconLoader.ico113);
    // END BOB 2016-08-02
	protected final JMenuItem menuFileQuit = new JMenuItem("Quit");

	// Menu "Edit"
	protected final JMenu menuEdit = new JMenu("Edit");
	// Submenu of "Edit"
	protected final JMenuItem menuEditUndo = new JMenuItem("Undo",IconLoader.ico039);
	protected final JMenuItem menuEditRedo = new JMenuItem("Redo",IconLoader.ico038);
	protected final JMenuItem menuEditCut = new JMenuItem("Cut",IconLoader.ico044);
	protected final JMenuItem menuEditCopy = new JMenuItem("Copy",IconLoader.ico042);
	protected final JMenuItem menuEditPaste = new JMenuItem("Paste",IconLoader.ico043);
	protected final JMenuItem menuEditCopyDiagramPNG = new JMenuItem("Copy bitmap diagram to clipboard",IconLoader.ico032);
	protected final JMenuItem menuEditCopyDiagramEMF = new JMenuItem("Copy vector diagram to clipboard",IconLoader.ico032);
	// START KGU#282 2016-10-16: Issue #272: Options to upgrade or downgrade graphics
	protected final JMenuItem menuEditUpgradeTurtle = new JMenuItem("To fine graphics",IconLoader.ico027);
	protected final JMenuItem menuEditDowngradeTurtle = new JMenuItem("To integer graphics",IconLoader.ico028);
	// END KGU#282 2016-10-16

	protected final JMenu menuView = new JMenu("View");

        // Menu "Diagram"
	protected final JMenu menuDiagram = new JMenu("Diagram");
	// Submenus of "Diagram"
	protected final JMenu menuDiagramAdd = new JMenu("Add");
	// Submenu "Diagram -> Add -> Before"
	protected final JMenu menuDiagramAddBefore = new JMenu("Before");
	// Submenus for adding Elements "Before"
	protected final JMenuItem menuDiagramAddBeforeInst = new JMenuItem("Instruction",IconLoader.ico007);
	protected final JMenuItem menuDiagramAddBeforeAlt = new JMenuItem("IF statement",IconLoader.ico008);
	protected final JMenuItem menuDiagramAddBeforeCase = new JMenuItem("CASE statement",IconLoader.ico047);
	protected final JMenuItem menuDiagramAddBeforeFor = new JMenuItem("FOR loop",IconLoader.ico009);
	protected final JMenuItem menuDiagramAddBeforeWhile = new JMenuItem("WHILE loop",IconLoader.ico010);
	protected final JMenuItem menuDiagramAddBeforeRepeat = new JMenuItem("REPEAT loop",IconLoader.ico011);
	protected final JMenuItem menuDiagramAddBeforeForever = new JMenuItem("ENDLESS loop",IconLoader.ico009);
	protected final JMenuItem menuDiagramAddBeforeCall = new JMenuItem("Call",IconLoader.ico049);
	protected final JMenuItem menuDiagramAddBeforeJump = new JMenuItem("Jump",IconLoader.ico056);
	protected final JMenuItem menuDiagramAddBeforePara = new JMenuItem("Parallel",IconLoader.ico090);

	// Submenu "Diagram -> Add -> After"
	protected final JMenu menuDiagramAddAfter = new JMenu("After");
	// Submenus for adding Elements "After"
	protected final JMenuItem menuDiagramAddAfterInst = new JMenuItem("Instruction",IconLoader.ico012);
	protected final JMenuItem menuDiagramAddAfterAlt = new JMenuItem("IF statement",IconLoader.ico013);
	protected final JMenuItem menuDiagramAddAfterCase = new JMenuItem("CASE statement",IconLoader.ico048);
	protected final JMenuItem menuDiagramAddAfterFor = new JMenuItem("FOR loop",IconLoader.ico014);
	protected final JMenuItem menuDiagramAddAfterWhile = new JMenuItem("WHILE loop",IconLoader.ico015);
	protected final JMenuItem menuDiagramAddAfterRepeat = new JMenuItem("REPEAT loop",IconLoader.ico016);
	protected final JMenuItem menuDiagramAddAfterForever = new JMenuItem("ENDLESS loop",IconLoader.ico014);
	protected final JMenuItem menuDiagramAddAfterCall = new JMenuItem("Call",IconLoader.ico050);
	protected final JMenuItem menuDiagramAddAfterJump = new JMenuItem("Jump",IconLoader.ico055);
	protected final JMenuItem menuDiagramAddAfterPara = new JMenuItem("Parallel",IconLoader.ico089);

	protected final JMenuItem menuDiagramEdit = new JMenuItem("Edit",IconLoader.ico006);
	protected final JMenuItem menuDiagramDelete = new JMenuItem("Delete",IconLoader.ico005);
	protected final JMenuItem menuDiagramMoveUp = new JMenuItem("Move up",IconLoader.ico019);
	protected final JMenuItem menuDiagramMoveDown = new JMenuItem("Move down",IconLoader.ico020);
	// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
	protected final JMenuItem menuDiagramTransmute = new JMenuItem("Transmute", IconLoader.ico109);
	// END KGU#199 2016-07-06
	// START KGU#365 2017-03-23: Enh. #380 - conversion of sequence in a subroutine
	protected final JMenuItem menuDiagramOutsource = new JMenuItem("Outsource", IconLoader.ico068);
	// END KGU#365 2017-03-23
	// START KGU#123 2016-01-03: New menu items for collapsing/expanding (addresses #65)
	protected final JMenuItem menuDiagramCollapse = new JMenuItem("Collapse", IconLoader.ico106);
	protected final JMenuItem menuDiagramExpand = new JMenuItem("Expand", IconLoader.ico107);
	// END KGU#123 2016-01-03
	// START KGU#310 2016-12-14: Renamed and moved to menu "Debug"
//	// START KGU#277 2016-10-13: Enh. #270: Disabling of elements
//	protected final JMenuItem menuDiagramDisable = new JMenuItem("Disable", IconLoader.ico026);
//	// END KGU#277 2016-10-13
//	// START KGU#143 2016-01-21: Bugfix #114 - Compensate editing restriction by accelerator4
//	protected final JMenuItem menuDiagramBreakpoint = new JMenuItem("Toggle Breakpoint", IconLoader.ico103);
//	// END KGU#143 2016-01-21
//	// START KGU#213 2016-08-02: Enh. #215
//	protected final JMenuItem menuDiagramBreakTrigger = new JMenuItem("Specify break trigger...", IconLoader.ico112);
//	// END KGU#143 2016-08-02
	// END KGU#310 2016-12-14

	protected final JMenu menuDiagramType = new JMenu("Type");
	protected final JCheckBoxMenuItem menuDiagramTypeProgram = new JCheckBoxMenuItem("Main",IconLoader.ico022);
	protected final JCheckBoxMenuItem menuDiagramTypeFunction = new JCheckBoxMenuItem("Sub",IconLoader.ico021);
	protected final JCheckBoxMenuItem menuDiagramNice = new JCheckBoxMenuItem("Boxed diagram?",IconLoader.ico040);
	protected final JCheckBoxMenuItem menuDiagramComment = new JCheckBoxMenuItem("Show comments?",IconLoader.ico077);
	protected final JCheckBoxMenuItem menuDiagramMarker = new JCheckBoxMenuItem("Highlight variables?",IconLoader.ico079);
	protected final JCheckBoxMenuItem menuDiagramDIN = new JCheckBoxMenuItem("DIN 66261?",IconLoader.ico082);
	protected final JCheckBoxMenuItem menuDiagramAnalyser = new JCheckBoxMenuItem("Analyse structogram?",IconLoader.ico083);
	protected final JCheckBoxMenuItem menuDiagramSwitchComments = new JCheckBoxMenuItem("Switch text/comments?",IconLoader.ico102);
	// START KGU#123 2016-01-04: Enh. #87
	protected final JCheckBoxMenuItem menuDiagramWheel = new JCheckBoxMenuItem("Mouse wheel for collapsing?",IconLoader.ico108);
	// END KGU#123 2016-01-04
	// START KGU#227 2016-07-31: Enh. #128
	protected final JCheckBoxMenuItem menuDiagramCommentsPlusText = new JCheckBoxMenuItem("Comments plus texts?",IconLoader.ico111);
	// END KGU#227 2016-07-31
	// START KGU#305 2016-12-14: Enh. #305
	protected final JCheckBoxMenuItem menuDiagramIndex = new JCheckBoxMenuItem("Show Arranger index?",IconLoader.ico029);
	// END KGU#305 2016-12-14

	// Menu "Preferences"
	protected final JMenu menuPreferences = new JMenu("Preferences");
	// Submenu of "Preferences"
	// START KGU#300 2016-12-02: Enh. #300
	protected final JCheckBoxMenuItem menuPreferencesNotifyUpdate = new JCheckBoxMenuItem("Notify of new versions?",IconLoader.ico052);
	// END KGU#2016-12-02
	protected final JMenuItem menuPreferencesFont = new JMenuItem("Font ...",IconLoader.ico023);
	protected final JMenuItem menuPreferencesColors = new JMenuItem("Colors ...",IconLoader.ico031);
	protected final JMenuItem menuPreferencesOptions = new JMenuItem("Structures ...",IconLoader.ico040);
	protected final JMenuItem menuPreferencesParser = new JMenuItem("Parser ...",IconLoader.ico004);
	protected final JMenuItem menuPreferencesAnalyser = new JMenuItem("Analyser ...",IconLoader.ico083);
	// START KGU#309 2016-12-15: Enh. #310 - new options for saving diagrams
	protected final JMenuItem menuPreferencesSaving = new JMenuItem("Saving ...",IconLoader.ico003);
	// END KGU#309 2016-12-15
	protected final JMenuItem menuPreferencesExport = new JMenuItem("Export ...",IconLoader.ico032);
	protected final JMenuItem menuPreferencesImport = new JMenuItem("Import ...",IconLoader.ico025);
	protected final JMenu menuPreferencesLanguage = new JMenu("Language");
	// START KGU#242 2016-09-04: Structural redesign - generic generation of language menu items
	protected final Hashtable<String, JCheckBoxMenuItem> menuPreferencesLanguageItems = new Hashtable<String, JCheckBoxMenuItem>(Locales.LOCALES_LIST.length);
	// END KGU#242 2016-09-04
	// START KGU#232 2016-08-03/2016-09-06: Enh. #222
	protected final JMenuItem menuPreferencesLanguageFromFile = new JCheckBoxMenuItem("From file ...",IconLoader.getLocaleIconImage("empty"));
	// END KGU#232 2016-08-03/2016-09-06
	protected final JMenu menuPreferencesLookAndFeel = new JMenu("Look & Feel");
	// START KGU#287 2017-01-11: Issue #81/#330
	protected final JMenuItem menuPreferencesScalePreset = new JMenuItem("GUI Scaling ...", IconLoader.ico051);
	// END KGU#287 2017-01-11
	protected final JMenu menuPreferencesSave = new JMenu("All preferences ...");
	protected final JMenuItem menuPreferencesSaveAll = new JMenuItem("Save");
	protected final JMenuItem menuPreferencesSaveLoad = new JMenuItem("Load from file ...");
	protected final JMenuItem menuPreferencesSaveDump = new JMenuItem("Save to file ...");

	// START KGU#310 2016-12-14
	// Menu "Debug"
	protected final JMenu menuDebug = new JMenu("Debug");
	// Submenu of "Debug"
    protected final JMenuItem menuDebugTurtle = new JMenuItem("Turtleizer ...", IconLoader.turtle);
    protected final JMenuItem menuDebugExecute = new JMenuItem("Executor ...", IconLoader.ico004);
	protected final JMenuItem menuDebugBreakpoint = new JMenuItem("Toggle breakpoint", IconLoader.ico103);
	protected final JMenuItem menuDebugBreakTrigger = new JMenuItem("Specify break trigger ...", IconLoader.ico112);
	protected final JMenuItem menuDebugDropBrkpts = new JMenuItem("Clear breakpoints", IconLoader.ico104);
	protected final JMenuItem menuDebugDisable = new JMenuItem("Disable", IconLoader.ico026);
	// END KGU#310 2016-12-14

	// Menu "Help"
	protected final JMenu menuHelp = new JMenu("Help");
	// Submenu of "Help"
	// START KGU#208 2016-07-22: Enh. #199
	protected final JMenuItem menuHelpOnline = new JMenuItem("User Guide",IconLoader.ico110);
	// END KGU#208 2016-07-22
	protected final JMenuItem menuHelpAbout = new JMenuItem("About ...",IconLoader.ico017);
	protected final JMenuItem menuHelpUpdate = new JMenuItem("Update ...",IconLoader.ico052);

	// START KGU#239 2016-08-12: Enh. #231
	// Generator plugins accessible for Analyser
	public static Vector<GENPlugin> generatorPlugins = new Vector<GENPlugin>();
	// END KGU#239 2016-08-12
	// START KGU#354 2017-03-04: Enh. #354
	public static Vector<GENPlugin> parserPlugins = new Vector<GENPlugin>();
	// END KGU#354 2017-03-04
	// Error messages for Analyser
	// START KGU#220 2016-07-27: Enh. as proposed in issue #207
	public static final LangTextHolder warning_1 = new LangTextHolder("WARNING: TEXTS AND COMMENTS ARE EXCHANGED IN DISPLAY! ---> \"Diagram > Switch text/comments\".");
	// END KGU#220 2016-07-27
	public static final LangTextHolder error01_1 = new LangTextHolder("WARNING: No loop variable detected ...");
	public static final LangTextHolder error01_2 = new LangTextHolder("WARNING: More than one loop variable detected: «%»");
	public static final LangTextHolder error01_3 = new LangTextHolder("You are not allowed to modify the loop variable «%» inside the loop!");
	public static final LangTextHolder error02 = new LangTextHolder("No change of the variables in the condition detected. Possible endless loop ...");
	public static final LangTextHolder error03_1= new LangTextHolder("The variable «%» has not yet been initialized!");
	public static final LangTextHolder error03_2 = new LangTextHolder("The variable «%» may not have been initialized!");
	public static final LangTextHolder error04 = new LangTextHolder("You are not allowed to use an IF-statement with an empty TRUE-block!");
	public static final LangTextHolder error05 = new LangTextHolder("The variable «%» must be written in uppercase!");
	public static final LangTextHolder error06 = new LangTextHolder("The programname «%» must be written in uppercase!");
	public static final LangTextHolder error07_1 = new LangTextHolder("«%» is not a valid name for a program or function!");
	public static final LangTextHolder error07_2 = new LangTextHolder("«%» is not a valid name for a parameter!");
	public static final LangTextHolder error07_3 = new LangTextHolder("«%» is not a valid name for a variable!");
	public static final LangTextHolder error08 = new LangTextHolder("It is not allowed to make an assignment inside a condition.");
	public static final LangTextHolder error09 = new LangTextHolder("Your program («%») cannot have the same name as a variable or parameter!");
	public static final LangTextHolder error10_1 = new LangTextHolder("A single instruction element should not contain input/output instructions and assignments!");
	public static final LangTextHolder error10_2 = new LangTextHolder("A single instruction element should not contain input and output instructions!");
	public static final LangTextHolder error10_3 = new LangTextHolder("A single instruction element should not contain input instructions and assignments!");
	public static final LangTextHolder error10_4 = new LangTextHolder("A single instruction element should not contain ouput instructions and assignments!");
	public static final LangTextHolder error11 = new LangTextHolder("You probably made an assignment error. Please check this instruction!");
	public static final LangTextHolder error12 = new LangTextHolder("The parameter «%» must start with the letter \"p\" followed by only uppercase letters!");
	public static final LangTextHolder error13_1 = new LangTextHolder("Your function does not return any result!");
	public static final LangTextHolder error13_2 = new LangTextHolder("Your function may not return a result!");
	// START KGU#78 (#23) 2015-11-25: Check for competitive return mechanisms
	public static final LangTextHolder error13_3 = new LangTextHolder("Your functions seems to use several competitive return mechanisms: «%»!");
	// END KGU#78 (#23) 2015-11-25
	// START KGU#3 2015-11-03: New checks for the enhanced For loop
	public static final LangTextHolder error14_1 = new LangTextHolder("The FOR loop parameters are not consistent to the loop heading text!");
	public static final LangTextHolder error14_2 = new LangTextHolder("The FOR loop step value («%») is not a legal integer constant!");
	// START KGU#3 2015-11-26: More clarity if e.g. a counter variable is named "step" and so is the stepFor parser preference
	public static final LangTextHolder error14_3 = new LangTextHolder("Variable name «%» may collide with one of the configured FOR loop heading keywords!");
	// END KGU#3 2015-11-26
	// END KGU#3 2015-11-03
	// START KGU#2 2015-11-25: New check for Call element syntax and Jump consistency
	// START KGU#278 2016-10-11: Enh. #267: Check for subroutine availability
	//public static final LangTextHolder error15 = new LangTextHolder("The CALL hasn't got form «[ <var> " + "\u2190" +" ] <routine_name>(<arg_list>)»!");
	public static final LangTextHolder error15_1 = new LangTextHolder("The CALL hasn't got form «[ <var> " + "\u2190" +" ] <routine_name>(<arg_list>)»!");
	public static final LangTextHolder error15_2 = new LangTextHolder("The called subroutine «%» is currently not available.");
	// END KGU#278 2016-10-11
	// START KGU 2016-12-17
	public static final LangTextHolder error15_3 = new LangTextHolder("There are several matching subroutines for «%».");
	// END KGU 2016-12-17
	public static final LangTextHolder error16_1 = new LangTextHolder("A JUMP element may be empty or start with one of %, possibly followed by an argument!");	
	public static final LangTextHolder error16_2 = new LangTextHolder("A return instruction, unless at final position, must form a JUMP element!");
	public static final LangTextHolder error16_3 = new LangTextHolder("An exit, leave or break instruction is only allowed as JUMP element!");
	public static final LangTextHolder error16_4 = new LangTextHolder("Cannot leave or break more loop levels than being nested in («%»)!");
	public static final LangTextHolder error16_5 = new LangTextHolder("You must not directly return out of a parallel thread!");
	public static final LangTextHolder error16_6 = new LangTextHolder("Wrong argument for this kind of JUMP (should be an integer constant)!");
	public static final LangTextHolder error16_7 = new LangTextHolder("Instruction isn't reachable after a JUMP!");
	// END KGU#2 2015-11-25
	// START KGU#47 2015-11-28: New check for concurrency problems
	public static final LangTextHolder error17 = new LangTextHolder("Consistency risk due to concurrent access to variable «%» by several parallel threads!");
	// END KGU#47 2015-11-28
	// START KGU#239 2016-08-12: Enh. #231 - New checks for variable name collisions
	public static final LangTextHolder error18 = new LangTextHolder("Variable name «%1» may be confused with variable(s) «%2» in some case-indifferent languages!");
	public static final LangTextHolder error19 = new LangTextHolder("Variable name «%1» may collide with reserved names in languages like %2!");
	// END KGU#239 2016-08-12
	// START KGU#253 2016-09-21: Enh. #249 - New check for subroutine syntax.
	public static final LangTextHolder error20 = new LangTextHolder("A subroutine header must have a (possibly empty) parameter list within parentheses.");
	// END KGU#253 2016-09-21
	// START KGU#327 2017-01-07: Enh. #329 - New check for hardly distinguishable variable names.
	public static final LangTextHolder error21 = new LangTextHolder("Variable names I (upper-case i), l (lower-case L), and O (upper-case o) are hard to distinguish from each other, 1, or 0.");
	// END KGU#253 2016-09-21

	// START KGU#218 2016-07-28: Issue #206 - enhanced localization
	// Dialog messages
	public static final LangTextHolder msgDialogExpCols = new LangTextHolder("Into how many columns do you want to split the output?");
	public static final LangTextHolder msgDialogExpRows = new LangTextHolder("Into how many rows do you want to split the output?");
	public static final LangTextHolder msgOverwriteFile = new LangTextHolder("Overwrite existing file?");
	public static final LangTextHolder msgOverwriteFiles = new LangTextHolder("Existing file(s) detected. Overwrite?");
	public static final LangTextHolder btnConfirmOverwrite = new LangTextHolder("Confirm Overwrite");
	public static final LangTextHolder msgRepeatSaveAttempt = new LangTextHolder("Your file has not been saved. Please repeat the save operation!");
	// END KGU#218 2016-07-28
	// START KGU#227 2016-07-31: Enh. #128
	public static final LangTextHolder menuDiagramSwitchTCTooltip = new LangTextHolder("Unselect \"%1\" to enable this item");
	// END KGU#227 2016-07-31
	// START KGU#213 2016-08-02: Enh. #215
	public static final LangTextHolder msgBreakTriggerPrompt = new LangTextHolder("Specify an execution count triggering a break (0 = always).");
	public static final LangTextHolder msgBreakTriggerIgnored = new LangTextHolder("Input ignored - must be a cardinal number.");
	public static final LangTextHolder msgErrorFileSave = new LangTextHolder("Error on saving the file: %!");
	// END KGU#213 2016-08-02
	// START KGU#232 2016-08-02: Enh. #222
	public static final LangTextHolder msgOpenLangFile = new LangTextHolder("Open language file");
	public static final LangTextHolder msgLangFile = new LangTextHolder("Structorizer language file");
	// END KGU#232 2016-08-02
	// START KGU#247 2016-09-15: Issue #243: Forgotten message box translations
	public static final LangTextHolder msgTitleError = new LangTextHolder("Error");
	public static final LangTextHolder msgTitleLoadingError = new LangTextHolder("Loading Error");
	public static final LangTextHolder msgTitleParserError = new LangTextHolder("Parser Error");
	public static final LangTextHolder msgTitleURLError = new LangTextHolder("URL Error");
	public static final LangTextHolder msgTitleQuestion = new LangTextHolder("Question");
	public static final LangTextHolder msgTitleWrongInput = new LangTextHolder("Wrong Input");
	public static final LangTextHolder msgTitleOpen = new LangTextHolder("Open file ...");
	public static final LangTextHolder msgTitleSave = new LangTextHolder("Save file ...");
	public static final LangTextHolder msgTitleSaveAs = new LangTextHolder("Save file as ...");
	public static final LangTextHolder msgTitleImport = new LangTextHolder("Code import - choose a file filter ...");
	public static final LangTextHolder msgSaveChanges = new LangTextHolder("Do you want to save the current NSD file?");
	public static final LangTextHolder msgErrorImageSave = new LangTextHolder("Error on saving the image(s)!");
	public static final LangTextHolder msgErrorUsingGenerator = new LangTextHolder("Error while using % generator");
	// END KGU#247 2016-09-15
	// START KGU#354 2017-03-04
	public static final LangTextHolder msgErrorUsingParser = new LangTextHolder("Error while using % parser");
	// END KGU#354 2017-03-04
	// START KGU#247 2016-09-17: Issue #243: Forgotten message box translation
	public static final LangTextHolder msgGotoHomepage = new LangTextHolder("Go to % to look for updates and news about Structorizer.");
	public static final LangTextHolder msgErrorNoFile = new LangTextHolder("File not found!");
	public static final LangTextHolder msgBrowseFailed = new LangTextHolder("Failed to show % in browser");
	// END KGU#247 2016-09-17
	// START KGU#258 2016-10-03: Enh. #253: Diagram keyword refactoring
	public static final LangTextHolder msgRefactoringOffer = new LangTextHolder("Keywords configured in the Parser Preferences were replaced:%Are loaded diagrams to be refactored accordingly?");
	public static final LangTextHolder lblRefactorNone = new LangTextHolder("no");
	public static final LangTextHolder lblRefactorCurrent = new LangTextHolder("current diagram");
	public static final LangTextHolder lblRefactorAll = new LangTextHolder("all diagrams");
	// END KGU#258 2016-10-03
	// START KGU#362 2017-03-28: Enh. #370
	public static final LangTextHolder msgDiscardParserPrefs = new LangTextHolder("Sure to discard new parser preferences?");
	public static final LangTextHolder msgAdaptStructPrefs = new LangTextHolder("Adapt Structure preferences to Parser preferences?");
	public static final LangTextHolder msgKeywordsDiffer = new LangTextHolder("This is a diagram, the original keyword context of which differs from current Parser Preferences:%1\n"
			+ "You have opted against automatic refactoring on loading.\n\n"
			+ "Now you have the following opportunities:\n%2");
	public static final LangTextHolder msgRefactorNow = new LangTextHolder("The diagram may be refactored now (recommended)");
	public static final LangTextHolder msgAdoptPreferences = new LangTextHolder("You may adopt the keywords loaded with the diagram as new Parser Preferences\n"
			+ "   (which would induce refactoring of all other open diagrams!)");
	public static final LangTextHolder msgLeaveAsIs = new LangTextHolder("You may leave the diagram as is, which may prevent its\n"
			+ "   debugging and cause defective export");
	public static final LangTextHolder msgAllowChanges = new LangTextHolder("Allow the modification, thus losing the original keyword information");
	public static final LangTextHolder lblRefactorNow = new LangTextHolder("Refactor diagram");
	public static final LangTextHolder lblAdoptPreferences = new LangTextHolder("Adopt keywords");
	public static final LangTextHolder lblLeaveAsIs = new LangTextHolder("Leave diagram as is");
	public static final LangTextHolder lblAllowChanges = new LangTextHolder("Allow to change now");
	// END KGU#362 2017-03-28
	// START KGU#282 2016-10-17: Enh. #272
	public static final LangTextHolder msgReplacementsDone = new LangTextHolder("% instructions replaced.");	
	// END KGU#282 2016-10-17
	// START KGU#300 2016-12-02: Enh. #300
	public static final LangTextHolder msgNewerVersionAvail = new LangTextHolder("A newer version % is available for download.");
	public static final LangTextHolder msgUpdateInfoHint = new LangTextHolder("If you want to get notified of available new versions\nyou may enable update retrieval from Structorizer homepage\nvia menu item \"%1\" > \"%2\".");
	public static final LangTextHolder lblOk = new LangTextHolder("OK");
	public static final LangTextHolder lblSuppressUpdateHint = new LangTextHolder("Don't show this window again");
	public static final LangTextHolder lblHint = new LangTextHolder("Hint");
	// END KGU#300 2016-12-02
	// START KGU#354 2017-03-04: Enh. #354 Now generic import menu
	//public static final LangTextHolder lblImportCode = new LangTextHolder("% Code ...");
	public static final LangTextHolder lblCopyToClipBoard = new LangTextHolder("OK + Copy to Clipboard");
	public static final LangTextHolder msgSelectParser = new LangTextHolder("The source file type of \"%2\" is ambiguous. Please select an import language:%1\nEnter the most appropriate index please.");
	public static final LangTextHolder msgImportCancelled = new LangTextHolder("Code import for file \"%\" cancelled.");
	// END KGU#354 2017-03-04
	// START KGU#365 2017-03-27: Enh. #380
	public static final LangTextHolder msgSubroutineName = new LangTextHolder("Name of the subroutine");
	// END KGU#365 2017-03-27

	public void create()
	{
		JMenuBar menubar = this;

		// START KGU#240 2016-09-01: Bugfix #233 - Configured key binding F10 for CASE insertion wasn't effective
		menubar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "none");
		// END KGU#240 2016-09-01

		// Setting up Menu "File" with all submenus and shortcuts and actions
		menubar.add(menuFile);
		menuFile.setMnemonic(KeyEvent.VK_F);

		menuFile.add(menuFileNew);
		menuFileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileNew.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.newNSD(); doButtons(); } } );

		menuFile.add(menuFileSave);
		menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileSave.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.saveNSD(false); doButtons(); } } );

		menuFile.add(menuFileSaveAs);
		menuFileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuFileSaveAs.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.saveAsNSD(); doButtons(); } } );

		// START KGU#373 2017-03-28: Enh. #387
		menuFile.add(menuFileSaveAll);
		menuFileSaveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, (java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK)));
		menuFileSaveAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.saveAllNSD(); doButtons(); } } );
		//  END KGU#373 2017-03-28

		menuFile.add(menuFileOpen);
		menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileOpen.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.openNSD(); doButtons(); } } );

		menuFile.add(menuFileOpenRecent);
		menuFileOpenRecent.setIcon(IconLoader.ico002);

		menuFile.addSeparator();

		menuFile.add(menuFileImport);

		// START KGU#354 2017-03-04: Enh. #354 / KGU#354 2017-03-14 Dropped again - one menu item for all now
		//menuFileImport.add(menuFileImportPascal);
		//menuFileImportPascal.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importPAS(); doButtons(); } } );
//		// Read parsers from configuration file and add them to the menu
//		BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream("parsers.xml"));
//		GENParser genp = new GENParser();
//		parserPlugins = genp.parse(buff);
//		for (int i=0; i < parserPlugins.size(); i++)
//		// END KGU#239 2016-08-12
//		{
//			// START KGU#239 2016-08-12: Enh. #231
//			//GENPlugin plugin = (GENPlugin) plugins.get(i);
//			GENPlugin plugin = parserPlugins.get(i);
//			// END KGU#239 2016-08-12
//			JMenuItem pluginItem = new JMenuItem(lblImportCode.getText().replace("%", plugin.title), IconLoader.ico004);
//			menuFileImport.add(pluginItem);
//			final String className = plugin.className;
//			pluginItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importCode(className); doButtons(); } } );
//		}
//		try {
//			buff.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// END KGU#354 2017-03-04
		// START KGU#354 2017-03-14: Enh. #354 We turn back to a single menu entry and leave selection to the FileChooser
		menuFileImport.add(menuFileImportCode);
		menuFileImportCode.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importCode(); } });
		menuFileImportCode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,(java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		// END KGU#354 2017-03-14

		menuFile.add(menuFileExport);

		menuFileExport.add(menuFileExportPicture);
		menuFileExportPicture.setIcon(IconLoader.ico032);

		menuFileExportPicture.add(menuFileExportPicturePNG);
		menuFileExportPicturePNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileExportPicturePNG.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.exportPNG(); doButtons(); } } );

		menuFileExportPicture.add(menuFileExportPicturePNGmulti);
		menuFileExportPicturePNGmulti.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.exportPNGmulti(); doButtons(); } } );

		menuFileExportPicture.add(menuFileExportPictureEMF);
		menuFileExportPictureEMF.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.exportEMF(); doButtons(); } } );

		menuFileExportPicture.add(menuFileExportPictureSWF);
		menuFileExportPictureSWF.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.exportSWF(); doButtons(); } } );

		menuFileExportPicture.add(menuFileExportPicturePDF);
		menuFileExportPicturePDF.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.exportPDF(); doButtons(); } } );

		menuFileExportPicture.add(menuFileExportPictureSVG);
		menuFileExportPictureSVG.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.exportSVG(); doButtons(); } } );

		menuFileExport.add(menuFileExportCode);
		menuFileExportCode.setIcon(IconLoader.ico004);

		// read generators from file
		// and add them to the menu
		BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream("generators.xml"));
		GENParser genp = new GENParser();
		// START KGU#239 2016-08-12: Enh. #231
		//Vector<GENPlugin> plugins = genp.parse(buff);
		//for(int i=0;i<plugins.size();i++)
		generatorPlugins = genp.parse(buff);
		for (int i=0; i < generatorPlugins.size(); i++)
		// END KGU#239 2016-08-12
		{
			// START KGU#239 2016-08-12: Enh. #231
			//GENPlugin plugin = (GENPlugin) plugins.get(i);
			GENPlugin plugin = generatorPlugins.get(i);
			// END KGU#239 2016-08-12
			JMenuItem pluginItem = new JMenuItem(plugin.title, IconLoader.ico004);
			menuFileExportCode.add(pluginItem);
			final String className = plugin.className;
			pluginItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.export(className); doButtons(); } } );
		}
		
		// START KGU#171 2016-04-01: Enh. #144 - accelerated export to favourite target language
		menuFile.add(menuFileExportCodeFavorite);
		menuFileExportCodeFavorite.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,(java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuFileExportCodeFavorite.setToolTipText("You may alter the favourite target language in the export preferences.");
		menuFileExportCodeFavorite.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event)
					{
						boolean done = false;
						String generatorName = diagram.getPreferredGeneratorName();
						for (int pos = 0; !done && pos < menuFileExportCode.getItemCount(); pos++)
						{
							JMenuItem pluginItem = menuFileExportCode.getItem(pos);
							if (pluginItem.getText().equals(generatorName))
							{
								pluginItem.getActionListeners()[0].actionPerformed(event);
								done = true;
							}
						}
					}
				});
		// END KGU#171 2016-04-01

		menuFile.addSeparator();

		menuFile.add(menuFilePrint);
		menuFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFilePrint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.printNSD(); doButtons(); } } );

		// START KGU#2 2015-11-19
		menuFile.add(menuFileArrange);
		//menuFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileArrange.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.arrangeNSD(); doButtons(); } } );
		// END KGU#2 2015-11-19
                
        menuFile.addSeparator();

        // START BOB 2016-08-02
		menuFile.add(menuFileTranslator);
		menuFileTranslator.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { Translator.launch(NSDControl); } } );
		// END BOB 2016-08-02

		menuFile.addSeparator();

		menuFile.add(menuFileQuit);
		menuFileQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		// START KGU#66 2015-11-05: hard exiting here fails to induce the file save dialog in case of unsaved changes and will kill a related Arranger!
		//menuFileQuit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { System.exit(0); } } );
		menuFileQuit.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event)
					{
						// Simulate the [x] button - this is the best we can do, Mainform will handle it properly
						getFrame().dispatchEvent(new WindowEvent(getFrame(), WindowEvent.WINDOW_CLOSING));
					}
				} );
		// END KGU#66 2015-11-05

		// Setting up Menu "Edit" with all submenus and shortcuts and actions
		menubar.add(menuEdit);
		menuEdit.setMnemonic(KeyEvent.VK_E);

		menuEdit.add(menuEditUndo);
		menuEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuEditUndo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.undoNSD(); doButtons(); } } );

		menuEdit.add(menuEditRedo);
		menuEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		menuEditRedo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.redoNSD(); doButtons(); } } );

		menuEdit.addSeparator();

		menuEdit.add(menuEditCut);
		menuEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuEditCut.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.cutNSD(); doButtons(); } } );

		menuEdit.add(menuEditCopy);
		//Toolkit.getDefaultToolkit().get
		//MenuShortcut ms = new MenuShortcut
		menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuEditCopy.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyNSD(); doButtons(); } } );

		menuEdit.add(menuEditPaste);
		menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuEditPaste.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.pasteNSD(); doButtons(); } } );

		menuEdit.addSeparator();

		// START KGU#282 2016-10-16: Issue #272: Options to upgrade or downgrade graphics
		menuEdit.add(menuEditUpgradeTurtle);
		menuEditUpgradeTurtle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, java.awt.event.InputEvent.SHIFT_MASK));
		menuEditUpgradeTurtle.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.replaceTurtleizerAPI(true); doButtons(); } } );

		menuEdit.add(menuEditDowngradeTurtle);
		menuEditDowngradeTurtle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuEditDowngradeTurtle.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.replaceTurtleizerAPI(false); doButtons(); } } );

		menuEdit.addSeparator();
		// END KGU#282 2016-10-16

		menuEdit.add(menuEditCopyDiagramPNG);
		menuEditCopyDiagramPNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuEditCopyDiagramPNG.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyToClipboardPNG();; doButtons(); } } );

		if(!System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
		{
			menuEdit.add(menuEditCopyDiagramEMF);
			menuEditCopyDiagramEMF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			menuEditCopyDiagramEMF.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyToClipboardEMF(); doButtons(); } } );
		}

		// Setting up Menu "View" with all submenus and shortcuts and actions
		//menubar.add(menuView);

		// Setting up Menu "Diagram" with all submenus and shortcuts and actions
		menubar.add(menuDiagram);
		menuDiagram.setMnemonic(KeyEvent.VK_D);

		menuDiagram.add(menuDiagramAdd);
		menuDiagramAdd.setIcon(IconLoader.ico018);

		menuDiagramAdd.add(menuDiagramAddBefore);
		menuDiagramAddBefore.setIcon(IconLoader.ico019);

		// START KGU#169 2016-04-01: Enh. #142 (accelerator keys added in analogy to the insert after items)
		menuDiagramAddBefore.add(menuDiagramAddBeforeInst);
		menuDiagramAddBeforeInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",false); doButtons(); } } );
		menuDiagramAddBeforeInst.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, java.awt.event.InputEvent.SHIFT_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeAlt);
		menuDiagramAddBeforeAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,false); doButtons(); } } );
		menuDiagramAddBeforeAlt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, java.awt.event.InputEvent.SHIFT_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeCase);
		menuDiagramAddBeforeCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,false); doButtons(); } } );
		menuDiagramAddBeforeCase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, java.awt.event.InputEvent.SHIFT_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeFor);
		menuDiagramAddBeforeFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,false); doButtons(); } } );
		menuDiagramAddBeforeFor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, java.awt.event.InputEvent.SHIFT_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeWhile);
		menuDiagramAddBeforeWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,false); doButtons(); } } );
		menuDiagramAddBeforeWhile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, java.awt.event.InputEvent.SHIFT_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeRepeat);
		menuDiagramAddBeforeRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,false); doButtons(); } } );
		menuDiagramAddBeforeRepeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, java.awt.event.InputEvent.SHIFT_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeForever);
		menuDiagramAddBeforeForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",false); doButtons(); } } );

		menuDiagramAddBefore.add(menuDiagramAddBeforeCall);
		menuDiagramAddBeforeCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",false); doButtons(); } } );
		menuDiagramAddBeforeCall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, java.awt.event.InputEvent.SHIFT_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeJump);
		menuDiagramAddBeforeJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",false); doButtons(); } } );
		menuDiagramAddBeforeJump.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, java.awt.event.InputEvent.SHIFT_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforePara);
		menuDiagramAddBeforePara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",false); doButtons(); } } );
		menuDiagramAddBeforePara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F13, java.awt.event.InputEvent.SHIFT_MASK));
		// END KGU#169 2016-04-01

		menuDiagramAdd.add(menuDiagramAddAfter);
		menuDiagramAddAfter.setIcon(IconLoader.ico020);

		menuDiagramAddAfter.add(menuDiagramAddAfterInst);
		menuDiagramAddAfterInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",true); doButtons(); } } );
		menuDiagramAddAfterInst.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterAlt);
		menuDiagramAddAfterAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,true); doButtons(); } } );
		menuDiagramAddAfterAlt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterCase);
		menuDiagramAddAfterCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,true); doButtons(); } } );
		menuDiagramAddAfterCase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterFor);
		menuDiagramAddAfterFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,true); doButtons(); } } );
		menuDiagramAddAfterFor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterWhile);
		menuDiagramAddAfterWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,true); doButtons(); } } );
		menuDiagramAddAfterWhile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterRepeat);
		menuDiagramAddAfterRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,true); doButtons(); } } );
		menuDiagramAddAfterRepeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterForever);
		menuDiagramAddAfterForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",true); doButtons(); } } );

		menuDiagramAddAfter.add(menuDiagramAddAfterCall);
		menuDiagramAddAfterCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",true); doButtons(); } } );
		menuDiagramAddAfterCall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterJump);
		menuDiagramAddAfterJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",true); doButtons(); } } );
		menuDiagramAddAfterJump.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterPara);
		menuDiagramAddAfterPara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",true); doButtons(); } } );
		menuDiagramAddAfterPara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F13,0));

		menuDiagram.add(menuDiagramEdit);
		// START KGU#177 2016-04-06: Enh. #158
		menuDiagramEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
		// END KGU#177 2016-04-06
		menuDiagramEdit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editNSD(); doButtons(); } } );

		menuDiagram.add(menuDiagramDelete);
		menuDiagramDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
		menuDiagramDelete.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.deleteNSD(); doButtons(); } } );

		menuDiagram.addSeparator();

		menuDiagram.add(menuDiagramMoveUp);
		// START KGU#177 2016-04-06: Enh. #158
		menuDiagramMoveUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK));
		// END KGU#177 2016-04-06
		menuDiagramMoveUp.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.moveUpNSD(); doButtons(); } } );

		menuDiagram.add(menuDiagramMoveDown);
		// START KGU#177 2016-04-06: Enh. #158
		menuDiagramMoveDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK));
		// END KGU#177 2016-04-06
		menuDiagramMoveDown.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.moveDownNSD(); doButtons(); } } );

		// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
		menuDiagram.add(menuDiagramTransmute);
		menuDiagramTransmute.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuDiagramTransmute.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.transmuteNSD(); doButtons(); } } );;
		// END KGU#199 2016-07-06
		// START KGU#365 2017-03-23: Enh. #380 - conversion of sequence in a subroutine
		menuDiagram.add(menuDiagramOutsource);
		menuDiagramOutsource.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.CTRL_DOWN_MASK));
		menuDiagramOutsource.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.outsourceNSD(); doButtons(); } } );;
		// END KGU#365 2017-03-23
		
		menuDiagram.addSeparator();

		// START KGU#123 2016-01-03: New menu items (addressing #65)
		menuDiagram.add(menuDiagramCollapse);
		menuDiagramCollapse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0));
		menuDiagramCollapse.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.collapseNSD(); doButtons(); } } );

		menuDiagram.add(menuDiagramExpand);
		menuDiagramExpand.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0));
		menuDiagramExpand.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.expandNSD(); doButtons(); } } );

		// START KGU#310 2016-12-14: Moved to menu Debug
//		// START KGU#277 2016-10-13: Enh. #270
//		menuDiagram.add(menuDebugDisable);
//		menuDebugDisable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//		menuDebugDisable.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.disableNSD(); doButtons(); } } );
//		// END KGU#277 2016-10-13
		// END KGU#310 2016-12-14

		menuDiagram.addSeparator();
		// END KGU#123 2016-01-03

		// START KGU#310 2016-12-14: Moved to menu "Debug" and renamed
//		// START KGU#143 2016-01-21: Bugfix #114 - Compensate editing restriction by accelerator
//		menuDiagram.add(menuDebugBreakpoint);
//		menuDebugBreakpoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
//		menuDebugBreakpoint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleBreakpoint(); doButtons(); } }); 
//
//		// START KGU#213 2016-08-02: Enh. #215 - new breakpoint feature
//		menuDiagram.add(menuDebugBreakTrigger);
//		menuDebugBreakTrigger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
//		menuDebugBreakTrigger.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editBreakTrigger(); doButtons(); } }); 
//		// END KGU#213 2016-08-02
//
//        menuDiagram.addSeparator();
//		// END KGU#143 2016-01-21
        // END KGU#310 2016-12-14
        

		menuDiagram.add(menuDiagramType);

		menuDiagramType.add(menuDiagramTypeProgram);
		menuDiagramTypeProgram.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setProgram(); doButtons(); } } );

		menuDiagramType.add(menuDiagramTypeFunction);
		menuDiagramTypeFunction.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setFunction(); doButtons(); } } );

		menuDiagram.add(menuDiagramNice);
		menuDiagramNice.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setNice(menuDiagramNice.isSelected()); doButtons(); } } );

		menuDiagram.add(menuDiagramComment);
		menuDiagramComment.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setComments(menuDiagramComment.isSelected()); doButtons(); } } );

		// START KGU#227 2016-07-31: Enh. #128
		menuDiagram.add(menuDiagramCommentsPlusText);
		menuDiagramCommentsPlusText.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setCommentsPlusText(menuDiagramCommentsPlusText.isSelected()); doButtons(); } } );
		// END KGU#227 2016-07-31

		menuDiagram.add(menuDiagramSwitchComments);
		menuDiagramSwitchComments.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleTextComments(); doButtons(); } } );
		// START KGU#169 2016-04-01: Enh. #142 (accelerator key added)
		menuDiagramSwitchComments.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, (java.awt.event.InputEvent.ALT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
		// START KGU#169 2016-04-01

		menuDiagram.add(menuDiagramMarker);
		menuDiagramMarker.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setHightlightVars(menuDiagramMarker.isSelected()); doButtons(); } } );
		menuDiagramMarker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));

		menuDiagram.add(menuDiagramDIN);
		menuDiagramDIN.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleDIN(); doButtons(); } } );

		menuDiagram.add(menuDiagramAnalyser);
		menuDiagramAnalyser.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleAnalyser(); doButtons(); } } );
		menuDiagramAnalyser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		
		// START KGU#305 2016-12-14: Enh. #305
		menuDiagram.add(menuDiagramIndex);
		menuDiagramIndex.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setArrangerIndex(menuDiagramIndex.isSelected()); } } );
		menuDiagramIndex.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, java.awt.event.InputEvent.SHIFT_MASK));
		// END KGU#305 2016-12-14

		// START KGU#123 2016-01-04: Enh. #87
		menuDiagram.add(menuDiagramWheel);
		menuDiagramWheel.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleWheelMode(); doButtons(); } } );
		// END KGU#123 2016-01-04

		// Setting up Menu "Preferences" with all submenus and shortcuts and actions
		menubar.add(menuPreferences);
		menuPreferences.setMnemonic(KeyEvent.VK_P);

		// START KGU#300 2016-12-02: Enh. #300
		menuPreferences.add(menuPreferencesNotifyUpdate);
		menuPreferencesNotifyUpdate.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setRetrieveVersion(menuPreferencesNotifyUpdate.isSelected()); } } );
		menuPreferencesNotifyUpdate.setToolTipText("Allow Structorizer to retrieve version info from Structorizer homepage and to inform about new releases.");
		// END KGU#2016-12-02

		menuPreferences.add(menuPreferencesFont);
		menuPreferencesFont.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.fontNSD(); doButtons(); } } );

		menuPreferences.add(menuPreferencesColors);
		menuPreferencesColors.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.colorsNSD(); doButtons(); } } );

		menuPreferences.add(menuPreferencesOptions);
		menuPreferencesOptions.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.preferencesNSD(); doButtons(); } } );

		menuPreferences.add(menuPreferencesParser);
		menuPreferencesParser.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.parserNSD(); doButtons(); } } );

		menuPreferences.add(menuPreferencesAnalyser);
		menuPreferencesAnalyser.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.analyserNSD(); doButtons(); } } );

		// START KGU#309 2016-12-15: Enh. #310
		menuPreferences.add(menuPreferencesSaving);
		menuPreferencesSaving.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.savingOptions(); doButtons(); } } );
		// END KGU#309 2016-12-15

		menuPreferences.add(menuPreferencesExport);
		menuPreferencesExport.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.exportOptions(); doButtons(); } } );

		// START KGU#258 2016-09-25: Enh. #253
		menuPreferences.add(menuPreferencesImport);
		menuPreferencesImport.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importOptions(); doButtons(); } } );
		// END KGU#258 2016-09-25

		menuPreferences.add(menuPreferencesLanguage);
		menuPreferencesLanguage.setIcon(IconLoader.ico081);

		// START KGU#242 2016-09-04: Redesign of the language menu item mechanism
		for (int iLoc = 0; iLoc < Locales.LOCALES_LIST.length; iLoc++)
		{
			final String locName = Locales.LOCALES_LIST[iLoc][0];
			String locDescription = Locales.LOCALES_LIST[iLoc][1];
			if (locDescription != null)
			{
				String caption = locDescription;
				ImageIcon icon = IconLoader.getLocaleIconImage(locName);
				JCheckBoxMenuItem item = new JCheckBoxMenuItem(caption, icon);
				item.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang(locName); } } );
				menuPreferencesLanguage.add(item);
				menuPreferencesLanguageItems.put(locName, item);
			}
		}
		// END KGU#242 2016-09-04

		// START KGU#232 206-08-03: Enh. #222
		menuPreferencesLanguage.addSeparator();
		menuPreferencesLanguage.add(menuPreferencesLanguageFromFile);
		menuPreferencesLanguageFromFile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLangFile(); } } );
		menuPreferencesLanguageFromFile.setToolTipText("You may create translation files with the 'Translator' tool in the File menu.");
                
		// create Look & Feel Menu
		menuPreferences.add(menuPreferencesLookAndFeel);
		menuPreferencesLookAndFeel.setIcon(IconLoader.ico078);
		UIManager.LookAndFeelInfo plafs[] = UIManager.getInstalledLookAndFeels();
		for(int j = 0; j < plafs.length; ++j)
		{
			JCheckBoxMenuItem mi = new JCheckBoxMenuItem(plafs[j].getName());
			mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { NSDControl.setLookAndFeel((((JCheckBoxMenuItem) event.getSource()).getText())); doButtons(); } } );
			menuPreferencesLookAndFeel.add(mi);

			if(mi.getText().equals(UIManager.getLookAndFeel().getName()))
			{
				mi.setSelected(true);
			}
		}

		// START KGU#287 2017-01-11: Issue #81/#330
		menuPreferences.add(menuPreferencesScalePreset);
		menuPreferencesScalePreset.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { new GUIScaleChooser().setVisible(true); } } );
		// END KGU#287 2017-01-11

		menuPreferences.addSeparator();

		menuPreferences.add(menuPreferencesSave);
		menuPreferencesSave.add(menuPreferencesSaveAll);
		menuPreferencesSaveAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { NSDControl.savePreferences(); } } );
		menuPreferencesSave.add(menuPreferencesSaveDump);
		menuPreferencesSaveDump.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) 
			{ 
				NSDControl.savePreferences(); 
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new INIFilter());
				if(fc.showSaveDialog(NSDControl.getFrame())==JFileChooser.APPROVE_OPTION)
				{
					// save some data from the INI file
					Ini ini = Ini.getInstance();
					try
					{
						ini.load();
						String fn = fc.getSelectedFile().toString();
						if(fn.toLowerCase().indexOf(".ini")==-1) fn+=".ini";
						ini.save(fn);
					}
					catch (Exception ex)
					{
						System.err.println("Error saving the configuration file ...");
						ex.printStackTrace();
					}
				}
			}
		} );
		menuPreferencesSave.add(menuPreferencesSaveLoad);
		menuPreferencesSaveLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) 
			{ 
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new INIFilter());
				if(fc.showOpenDialog(NSDControl.getFrame())==JFileChooser.APPROVE_OPTION)
				{
					try
					{
						// load some data from the INI file
						Ini ini = Ini.getInstance();

						// START KGU#258 2016-09-26: Enh. #253
						HashMap<String, StringList> refactoringData = new LinkedHashMap<String, StringList>();
						for (String key: CodeParser.keywordSet())
						{
							// START KGU#288 2016-11-06: Issue #279 - getOrDefault() may not be available
							//String keyword = CodeParser.keywordMap.getOrDefault(key, "");
							String keyword = CodeParser.getKeywordOrDefault(key, "");
							// END KGU#288 2016-11-06
							if (!keyword.trim().isEmpty())
							{
								// Complete strings aren't likely to be found in a key, so don't bother
								refactoringData.put(key, Element.splitLexically(keyword,  false));
							}
							// An empty preForIn keyword is a synonym for the preFor keyword
							else if (key.equals("preForIn"))
							{
								refactoringData.put(key, refactoringData.get("preFor"));
							}
						}
						// END KGU#258 2016-09-26

						ini.load(fc.getSelectedFile().toString());
						ini.save();
						NSDControl.loadFromINI();

						// START KGU#258 2016-09-26: Enh. #253
						if (diagram.offerRefactoring(refactoringData))
						{
							diagram.refactorNSD(refactoringData);
						}
						// END KGU#258 2016-09-26
					}
					catch (Exception ex)
					{
						System.err.println("Error loading the configuration file ...");
						ex.printStackTrace();
					}
				}
				NSDControl.savePreferences(); 
			}
		} );

        // START KGU#310 2016-12-14: New Debug menu
		menubar.add(menuDebug);
		menuDebug.setMnemonic(KeyEvent.VK_B);
		
		menuDebug.add(menuDebugTurtle);
		menuDebugTurtle.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.goTurtle(); } } );

		menuDebug.add(menuDebugExecute);
		menuDebugExecute.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.goRun(); } } );

		menuDebug.add(menuDebugDropBrkpts);
		menuDebugDropBrkpts.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.clearBreakpoints(); } } );

		menuDebug.addSeparator();

		menuDebug.add(menuDebugBreakpoint);
		menuDebugBreakpoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		menuDebugBreakpoint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleBreakpoint(); doButtons(); } }); 

		menuDebug.add(menuDebugBreakTrigger);
		menuDebugBreakTrigger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		menuDebugBreakTrigger.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editBreakTrigger(); doButtons(); } }); 

		menuDebug.add(menuDebugDisable);
		menuDebugDisable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuDebugDisable.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.disableNSD(); doButtons(); } } );
		// END KGU#310 2016-12-14

		// Setting up Menu "Help" with all submenus and shortcuts and actions
		menubar.add(menuHelp);
		// START KGU#184 2016-04-24: Bugfix #173: This overwrote the Diagram mnemonics
		//menuDiagram.setMnemonic(KeyEvent.VK_A);
		menuHelp.setMnemonic(KeyEvent.VK_H);
		// END KGU#184 2016-04-24

		// START KGU#208 2016-07-22: Enh. #199
		menuHelp.add(menuHelpOnline);
		menuHelpOnline.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) {diagram.helpNSD(); } } );
		menuHelpOnline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		// END KGU#208 2016-07-22

		menuHelp.add(menuHelpAbout);
		menuHelpAbout.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) {diagram.aboutNSD(); } } );
		// START KGU#208 2016-07-22: Enh. #199 - F1 accelerator re-decicated to User Guide
		//menuHelpAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menuHelpAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_DOWN_MASK));
		// END KGU#208 2016-07-22

		menuHelp.add(menuHelpUpdate);
		menuHelpUpdate.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) {diagram.updateNSD(); } } );
		menuHelpUpdate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        // START KGU#287 2017-01-09: Issues #81/#330 GUI scaling
        GUIScaler.rescaleComponents(this);
//        if (this.getFrame() != null) {
//        	SwingUtilities.updateComponentTreeUI(this.getFrame());
//        }
        // END KGU#287 2017-01-09

        // Attempt to find out what provokes the NullPointerExceptions on start
		//System.out.println("**** " + this + ".create() ready!");
	}

	@Override
	public void setLookAndFeel(String _laf) {}

	@Override
	public String getLookAndFeel() { return null;}

	@Override
	public void doButtons()
	{
		if(NSDControl!=null)
		{
			NSDControl.doButtons();
		}
	}

	@Override
	public void doButtonsLocal()
	{
		if (diagram!=null)
		{
                        /*
                        // remove all submenus from "view"
                        menuView.removeAll();
                        // add submenus to "view"
                        for(int i=0;i<diagram.toolbars.size();i++)
                        {
                          final MyToolbar tb = diagram.toolbars.get(i);

                          JCheckBoxMenuItem menuToolbar = new JCheckBoxMenuItem(tb.getName(),IconLoader.ico023);
                	  menuToolbar.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { tb.setVisible(!tb.isVisible()); doButtons(); } } );

                          if (tb.isVisible())
                          {
                                menuToolbar.setSelected(true);
                          }
                          menuView.add(menuToolbar);
                          //System.out.println(entry.getKey() + "-->" + entry.getValue());
                        }
                        */

			// conditions
			// START KGU#143 2016-01-21: Bugfix #114 - elements involved in execution must not be edited
			//boolean conditionAny =  diagram.getSelected()!=null;
			Element selected = diagram.getSelected();
			boolean conditionAny =  selected != null && !selected.isExecuted();
			// END KGU#143 2016-01-21
			boolean condition =  conditionAny && diagram.getSelected()!=diagram.getRoot();
			// START KGU#87 2015-11-22: For most operations, multiple selections are not suported
			boolean conditionNoMult = condition && !diagram.selectedIsMultiple();
			// END KGU#87 2015-11-22
			int i = -1;
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
			menuFileSave.setEnabled(diagram.canSave(false));
			// END KGU#137 2016-01-11
		    // START KGU#373 2017-03-28: Enh. #387
			menuFileSaveAll.setEnabled(diagram.canSave(true));
		    // END KGU#373 2017-03-38
			// START KGU#170 2016-04-01: Enh. #144 - update the favourite export item text
			String itemText = lbFileExportCodeFavorite.getText().replace("%", diagram.getPreferredGeneratorName());
			this.menuFileExportCodeFavorite.setText(itemText);
			// END KGU#170 2016-04-01
			
			// undo & redo
			menuEditUndo.setEnabled(diagram.getRoot().canUndo());
			menuEditRedo.setEnabled(diagram.getRoot().canRedo());

			// graphics up/downgrade
			// START KGU#282 2016-10-16: Issue #272
			menuEditUpgradeTurtle.setEnabled(conditionAny);
			menuEditDowngradeTurtle.setEnabled(conditionAny);
			// END KGU#282 2016-10-16

			// style
			menuDiagramTypeFunction.setSelected(!diagram.isProgram());
			menuDiagramTypeProgram.setSelected(diagram.isProgram());
			menuDiagramNice.setSelected(diagram.getRoot().isNice);
			menuDiagramAnalyser.setSelected(Element.E_ANALYSER);
			// START KGU#305 2016-12-14: Enh. #305
			menuDiagramIndex.setSelected(diagram.showArrangerIndex());
			// END KGU#305 2016-12-14

			// elements
			// START KGU#87 2015-11-22: Why enable the main entry if no action is enabled?
			menuDiagramAdd.setEnabled(conditionNoMult);
			// END KGU#87 2015-11-22
			menuDiagramAddBeforeInst.setEnabled(conditionNoMult);
			menuDiagramAddBeforeAlt.setEnabled(conditionNoMult);
			menuDiagramAddBeforeCase.setEnabled(conditionNoMult);
			menuDiagramAddBeforeFor.setEnabled(conditionNoMult);
			menuDiagramAddBeforeWhile.setEnabled(conditionNoMult);
			menuDiagramAddBeforeRepeat.setEnabled(conditionNoMult);
			menuDiagramAddBeforeForever.setEnabled(conditionNoMult);
			menuDiagramAddBeforeCall.setEnabled(conditionNoMult);
			menuDiagramAddBeforeJump.setEnabled(conditionNoMult);
			menuDiagramAddBeforePara.setEnabled(conditionNoMult);

			menuDiagramAddAfterInst.setEnabled(conditionNoMult);
			menuDiagramAddAfterAlt.setEnabled(conditionNoMult);
			menuDiagramAddAfterCase.setEnabled(conditionNoMult);
			menuDiagramAddAfterFor.setEnabled(conditionNoMult);
			menuDiagramAddAfterWhile.setEnabled(conditionNoMult);
			menuDiagramAddAfterRepeat.setEnabled(conditionNoMult);
			menuDiagramAddAfterForever.setEnabled(conditionNoMult);
			menuDiagramAddAfterCall.setEnabled(conditionNoMult);
			menuDiagramAddAfterJump.setEnabled(conditionNoMult);
			menuDiagramAddAfterPara.setEnabled(conditionNoMult);


			// editing
			// START KGU#87 2015-11-22: Don't allow editing if multiple elements are selected
			//menuDiagramEdit.setEnabled(conditionAny);
			// START KGU#143 2016-11-17: Bugfix #114 - unstructured elements may be edited if parent is waiting
			//menuDiagramEdit.setEnabled(conditionAny && !diagram.selectedIsMultiple());
			menuDiagramEdit.setEnabled(diagram.canEdit());
			// END KGU#143 2016-11-7
			// END KGU#87 2015-11-22
			// START KGU#143 2016-01-21: Bugfix #114 - we must differentiate among cut and copy
			//menuDiagramDelete.setEnabled(diagram.canCutCopy());
			menuDiagramDelete.setEnabled(diagram.canCut());
			// END KGU#143 2016-01-21
			menuDiagramMoveUp.setEnabled(conditionCanMoveUp);
			menuDiagramMoveDown.setEnabled(conditionCanMoveDown);
			// START KGU#199 2016-07-07: Enh. #188 - We allow instruction conversion
			menuDiagramTransmute.setEnabled(diagram.canTransmute());
			// END KGU#199 2016-07-07
			// START KGU#365 2017-03-26: Enh. #380 - We allow subroutine generation
			menuDiagramOutsource.setEnabled(diagram.canCut());
			// END KGU#365 2017-03-26
			
			
			// START KGU#123 2016-01-03: We allow multiple selection for collapsing
			// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
			menuDiagramCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());
			menuDiagramExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());			
			// END KGU#123 2016-01-03
			// START KGU#277 2016-10-13: Enh. #270
			menuDebugDisable.setEnabled(condition && !(selected instanceof Subqueue) || diagram.selectedIsMultiple());
			// END KGU#277 2016-01-13

			// START KGU#143 2016-01-21: Bugfix #114 - breakpoint control now also here
			// START KGU#177 2016-07-06: Enh. #158 - Collateral damage mended
			//menuDiagramBreakpoint.setEnabled(diagram.canCopy());
			menuDebugBreakpoint.setEnabled(diagram.canCopyNoRoot());
			// END KGU#177 2016-07-06
			// END KGU#143 2016-01-21
			// START KGU#213 2016-08-02: Enh. #215 - breakpoint control enhanced
			menuDebugBreakTrigger.setEnabled(diagram.canCopyNoRoot() && !diagram.selectedIsMultiple());
			// END KGU#213 2016-08-02

			// copy & paste
			// START KGU#143 2016-01-21: Bugfix #114 - we must differentiate among cut and copy
			//menuEditCopy.setEnabled(diagram.canCutCopy());
			//menuEditCut.setEnabled(diagram.canCutCopy());
			menuEditCopy.setEnabled(diagram.canCopy());
			menuEditCut.setEnabled(diagram.canCut());
			// END KGU#143 2016-01-21
			menuEditPaste.setEnabled(diagram.canPaste());

			// nice
			menuDiagramNice.setSelected(diagram.isNice());

			// variable highlighting
			menuDiagramMarker.setSelected(diagram.getRoot().hightlightVars);

			// show comments?
			menuDiagramComment.setSelected(Element.E_SHOWCOMMENTS);

			// START KGU#227 2016-07-31: Enh. #128
			// draw elements with both comments and diagram?
			menuDiagramCommentsPlusText.setSelected(Element.E_COMMENTSPLUSTEXT);
			menuDiagramSwitchComments.setEnabled(!Element.E_COMMENTSPLUSTEXT);
			if (Element.E_COMMENTSPLUSTEXT)
			{
				menuDiagramSwitchComments.setToolTipText(menuDiagramSwitchTCTooltip.getText().replace("%1", menuDiagramCommentsPlusText.getText()));
			}
			else
			{
				menuDiagramSwitchComments.setToolTipText(null);
			}
			// END KGU#227 2016-07-31

			// swap texts against comments?
			menuDiagramSwitchComments.setSelected(Element.E_TOGGLETC);

			// DIN 66261
			menuDiagramDIN.setSelected(Element.E_DIN);
			if(Element.E_DIN==true)
			{
				menuDiagramAddBeforeFor.setIcon(IconLoader.ico010);
				menuDiagramAddAfterFor.setIcon(IconLoader.ico015);
			}
			else
			{
				menuDiagramAddBeforeFor.setIcon(IconLoader.ico009);
				menuDiagramAddAfterFor.setIcon(IconLoader.ico014);
			}
			
			// START KGU#123 2016-01-04: Enh. #87
			// control the collapsing by mouse wheel?
			menuDiagramWheel.setSelected(diagram.getWheelCollapses());
			// END KGU#123 2016-01-04
			
			// START KGU#300 2016-12-02: Enh. #300
			menuPreferencesNotifyUpdate.setSelected(Ini.getInstance().getProperty("retrieveVersion", "false").equals("true"));
			// END KGU#300 2016-12-02

			// Look and Feel submenu
			//System.out.println("Having: "+UIManager.getLookAndFeel().getName());
			for(i=0;i<menuPreferencesLookAndFeel.getMenuComponentCount();i++)
			{
				JCheckBoxMenuItem mi =(JCheckBoxMenuItem) menuPreferencesLookAndFeel.getMenuComponent(i);

				//System.out.println("Listing: "+mi.getText());
				if (mi.getText().equals(NSDControl.getLookAndFeel()))
				{
					mi.setSelected(true);
					//System.out.println("Found: "+mi.getText());
				}
				else
				{
					mi.setSelected(false);
				}
			}

			// Languages
			String locName = Locales.getInstance().getLoadedLocaleName();
			// START KGU#242 2016-09-04: Structural redesign
			for (String key: menuPreferencesLanguageItems.keySet())
			{
				menuPreferencesLanguageItems.get(key).setSelected(locName.equals(key));
			}
			// END KGU#242 2016-09-04
			menuPreferencesLanguageFromFile.setSelected(locName.equals("external"));

			// Recent file
			// START KGU#287 2017-01-11: Issue #81/#330 Assimilate the dynamic menu items in font
			Font menuItemFont = UIManager.getFont("MenuItem.font");
			// END KGU#287 2017-01-11
			menuFileOpenRecent.removeAll();
			for(int j = 0; j < diagram.recentFiles.size(); ++j)
			{
				final String nextFile = (String) diagram.recentFiles.get(j);
				JMenuItem mi = new JMenuItem(nextFile, IconLoader.ico074);
				// START KGU#316 2016-12-28: Enh. #290/#318
				//mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.openNSD(nextFile); doButtons(); } } );
				mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.openNsdOrArr(nextFile); doButtons(); } } );
				// END KGU#316 2016-12-28
				// START KGU#287 2017-01-11: Issue #81/#330 Assimilate the dynamic menu items in font
				if (menuItemFont != null) mi.setFont(menuItemFont);
				// END KGU#287 2017-01-11
				menuFileOpenRecent.add(mi);
			}

		}
	}

	@Override
	public void updateColors() {}


	public Menu(Diagram _diagram, NSDController _NSDController)
	{
		super();
		diagram=_diagram;
		NSDControl=_NSDController;
		create();
	}

	
	@Override
	public void savePreferences() {};

    @Override
    public JFrame getFrame()
    {
        return NSDControl.getFrame();
    }

    @Override
    public void loadFromINI()
    {
    }

	// START KGU#235 2016-08-09: Bugfix #225
    public void chooseLang(String localeName)
    {
    	Locales.getInstance().setLocale(localeName);
    	doButtons();
    	diagram.analyse();
    }
	// END KGU#235 2016-08-09
	
    // START KGU#232 2016-08-03: Enh. #222
    public void chooseLangFile() {
        JFileChooser dlgOpen = new JFileChooser();
        dlgOpen.setDialogTitle(msgOpenLangFile.getText());
        // set directory
        dlgOpen.setCurrentDirectory(new File(System.getProperty("user.home")));
        // config dialogue
        FileNameExtensionFilter filter = new FileNameExtensionFilter(msgLangFile.getText(), "txt");
        dlgOpen.addChoosableFileFilter(filter);
        dlgOpen.setFileFilter(filter);
        // show & get result
        int result = dlgOpen.showOpenDialog(this);
        // react on result
        if (result == JFileChooser.APPROVE_OPTION) {
            // create a new StringList
            StringList sl = new StringList();
            // load the selected file into it
            String filename = dlgOpen.getSelectedFile().getAbsoluteFile().toString();
            sl.loadFromFile(filename);
            // paste it's content to the "external" locale
            Locales.getInstance().setExternal(sl,filename);
        }
        // START KGU#235 2016-08-09: Bugfix #225
        doButtons();
        diagram.analyse();
        // END KGU#235 2016-08-09
    }
    // END KGU#232 2016-08-03
}

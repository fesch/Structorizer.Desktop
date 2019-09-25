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
 *      Description:    This class is responsible for setting up the entire menubar.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-30      First Issue
 *      Bob Fisch       2008-04-12      Adapted for Generator plugin
 *      Kay Gürtzig     2015-11-03      Additions for FOR loop enhancement (KGU#3)
 *      Kay Gürtzig     2015-11-22      Adaptations for handling selected non-empty Subqueues (KGU#87)
 *      Kay Gürtzig     2015-11-25      Error labels error13_3 (KGU#78), error15 (KGU#2), and error_16_x added
 *      Kay Gürtzig     2015-11-26      New error label error14_3 (KGU#3) added
 *      Kay Gürtzig     2015-11-28      New error label error17 (KGU#47) added
 *      Kay Gürtzig     2016-01-03/04   Enh. #87: New menu items and buttons for collapsing/expanding 
 *      Kay Gürtzig     2016-01-21      Bugfix #114: Editing restrictions during execution, breakpoint menu item
 *      Kay Gürtzig     2016-01-22      Bugfix for Enh. #38 (addressing moveUp/moveDown, KGU#143 + KGU#144).
 *      Kay Gürtzig     2016-04-01      Issue #144: Favourite code export menu item, #142 accelerator keys added
 *      Kay Gürtzig     2016-04-06      Enh. #158: Key bindings for editNSD, moveUpNSD, moveDownNSD
 *      Kay Gürtzig     2016-04-12      Enh. #137: New message error16_7 introduced.
 *      Kay Gürtzig     2016-04-24      Fix #173: Mnemonics for menus Diagram and Help had been compromised
 *      Kay Gürtzig     2016-07-07      Enh. #188: New menu item "wand" for element conversion (KGU#199)
 *      Kay Gürtzig     2016-07-22      Enh. #199: New help menu item "user guide" for element conversion (KGU#208)
 *      Kay Gürtzig     2016-07-28      Enh. #206: New Dialog message text holders
 *      Kay Gürtzig     2016-07-31      Enh. #128: New Diagram menu item "Comments + text"
 *      Kay Gürtzig     2016-08-02      Enh. #215: menuDiagramBreakTrigger added, new message text holders
 *      Kay Gürtzig     2016-08-03      Enh. #222: New possibility to load translations from a text file
 *      Kay Gürtzig     2016-08-04      Most persistent attributes set to final
 *      Bob Fisch       2016-08-08      Redesign of the Language choice mechanisms (#225 fixed by Kay Gürtzig)
 *      Kay Gürtzig     2016-08-12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions) 
 *      Kay Gürtzig     2016-08-12      Enh. #231: Analyser checks re-organised to arrays for easier maintenance
 *                                      two new checks introduced (variable name collisions)
 *      Kay Gürtzig     2016-09-01      Bugfix #233: CASE insertion by F10 had been averted by menu bar
 *      Kay Gürtzig     2016-09-04      Structural redesign for menuPreferencesLanguage
 *      Kay Gürtzig     2016-09-15      Issue #243: Additional text holders for forgotten message box texts
 *      Kay Gürtzig     2016-09-22      New text holder / messages for Analyser
 *      Kay Gürtzig     2016-09-26/03   Enh. #253: Refactoring support
 *      Kay Gürtzig     2016-10-11      Enh. #267: error15 renamed to error15_1, new error15_2
 *      Kay Gürtzig     2016-10-13      Enh. #270: Menu items for the disabling of elements
 *      Kay Gürtzig     2016-10-16      Enh. #272: Menu items for the replacement of Turtleizer command sets
 *      Kay Gürtzig     2016-11-17      Bugfix #114: Prerequisites for editing during execution revised
 *      Kay Gürtzig     2016-12-02      Enh. #300: New menu entry to enable online update retrieval
 *      Kay Gürtzig     2016-12-14      Enh. #305: New menu entry to enable/disable Arranger index
 *                                      KGU#310: New Debug menu
 *      Kay Gürtzig     2016-12-17      Enh. #267: New Analyser error15_3
 *      Kay Gürtzig     2017-01-07      Enh. #329: New Analyser error21
 *      Kay Gürtzig     2017-03-15      Enh. #354: All code import merged to a single menu item
 *      Kay Gürtzig     2017-03-23      Enh. #380: New menu entry to convert a sequence in a subroutine
 *      Kay Gürtzig     2017-03-28      Enh. #387: New menu entry "Save All"
 *      Kay Gürtzig     2017-04-04      Enh. #388: New Analyser error for constant definitions (no. 22)
 *      Kay Gürtzig     2017-04-11      Enh. #389: Additional messages for analysis of import calls
 *      Kay Gürtzig     2017-04-20      Enh. #388: Second error (error22_2) for constant analysis
 *      Kay Gürtzig     2017-04-26/28   Enh. KGU#386: Method for plugin menu items, diagram file import
 *      Kay Gürtzig     2017-05-16      Enh. #389: Third diagram type ("includable") added
 *      Kay Gürtzig     2017-05-21      Enh. #372: New menu entry and accelerator for AttribeInspector
 *      Kay Gürtzig     2017-06-13      Enh. #415: Find&Replace menu item
 *      Kay Gürtzig     2017-11-05      Enh. #452: Preference "simplified toolbars" introduced
 *      Kay Gürtzig     2017-11-09      Enh. #415: New accelerator key for menuEditCopyDiagramEMF
 *      Kay Gürtzig     2017-11-20      Enh. #452/#459: Revisions for guided tours, enh. #469: Accelerators for debug menu
 *      Kay Gürtzig     2017-12-06      Enh. #487: New menu items for hiding of declaration sequences
 *      Kay Gürtzig     2017-12-14/15   Enh. #492: Configuration of external element names added
 *      Kay Gürtzig     2018-01-18      Issue #4: Icon association modified
 *      Kay Gürtzig     2018-01-18/19   Enh. #490: New preferences menu item added (DiagramController aliases)
 *      Kay Gürtzig     2018-02-07      Enh. #4, #81: Icon retrieval updated, scaling for plugin icons
 *      Kay Gürtzig     2018-02-12      Issue #4: Separate icons for FOR loops introduced
 *      Kay Gürtzig     2018-02-13      Issue #510: All "arrowed" element icons replaced by conv. element icons
 *      Kay Gürtzig     2018-03-14      Enh. #519: New ctrl+wheel preference together with old menuDiagramWheel in Preferences menu
 *      Kay Gürtzig     2018-03-15      Bugfix #522: New messages for subroutine outsourcing 
 *      Kay Gürtzig     2018-10-26      Enh. #619: New menu entries and messages for line breaking
 *      Kay Gürtzig     2018-12-24      Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() calls concentrated
 *      Kay Gürtzig     2019-01-04      Enh. #657: Key bindings Ctrl-G and Ctrl-Shift-G withdrawn (too rarely used)
 *      Kay Gürtzig     2919-02-20      Issue #686: Improved the detection of the current Look and Feel
 *      Kay Gürtzig     2019-02-26      Enh. #689: New menu item to edit the sub diagram referred by a CALL
 *      Kay Gürtzig     2019-03-07      Enh. #385: New message error20_2, error20 renamed in error20_1
 *      Kay Gürtzig     2019-03-16      Enh. #56: New menu items to add TRY-CATCH elements *
 *      Kay Gürtzig     2019-03-17      Issue #56: breakpoint items disabled for Forever and Try elements.
 *      Kay Gürtzig     2019-03-22      Enh. #452: Several popup menu items made invisible on simplified mode
 *      Kay Gürtzig     2019-03-27      Enh. #717: New menu entry menuPreferencesWheelUnit
 *      Kay Gürtzig     2019-08-02/03   Issue #733 Selective property export mechanism implemented.
 *      Kay Gürtzig     2019-08-06      Enh. #740: Backup mechanism for the explicit loading of INI files
 *      Kay Gürtzig     2019-09-13      Enh. #746: Re-translation of import plugin menu items (LangEventListener)
 *      Kay Gürtzig     2019-09-17      Issues #747/#748: Menu items for Try elements, shortcuts for Try, Parallel, Forever
 *      Kay Gürtzig     2019-09-24      Enh. #738: New menu items for code preview
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.helpers.*;
import lu.fisch.structorizer.io.INIFilter;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.locales.LangEvent;
import lu.fisch.structorizer.locales.LangEventListener;
import lu.fisch.structorizer.locales.LangMenuBar;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.locales.Locale;
import lu.fisch.structorizer.locales.Locales;
import lu.fisch.structorizer.locales.Translator;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.utils.StringList;

@SuppressWarnings("serial")
// START KGU#725 2019-09-13: Enh. #746
//public class Menu extends LangMenuBar implements NSDController
public class Menu extends LangMenuBar implements NSDController, LangEventListener
// END KGU#725 2019-09-13
{
	public enum PluginType { GENERATOR, PARSER, IMPORTER, CONTROLLER };
	
	// START KGU#484 2018-03-22: Issue #463
	public static final Logger logger = Logger.getLogger(Menu.class.getName());
	// END KGU#484 2018-03-22
	private Diagram diagram = null;
	private NSDController NSDControl = null;

	// Menu "File"
	protected final JMenu menuFile = new JMenu("File");
	// Submenus of "File"
	protected final JMenuItem menuFileNew = new JMenuItem("New", IconLoader.getIcon(1));
	protected final JMenuItem menuFileSave = new JMenuItem("Save", IconLoader.getIcon(3));
	protected final JMenuItem menuFileSaveAs = new JMenuItem("Save As ...", IconLoader.getIcon(92));
	// START KGU#373 2017-03-28: Enh. #387
	protected final JMenuItem menuFileSaveAll = new JMenuItem("Save All", IconLoader.getIcon(69));
	//  END KGU#373 2017-03-28
	protected final JMenuItem menuFileOpen = new JMenuItem("Open ...", IconLoader.getIcon(2));
	protected final JMenuItem menuFileOpenRecent = new JMenu("Open Recent File");
	protected final JMenu menuFileExport = new JMenu("Export");
	// Submenu of "File -> Export"
	protected final JMenu menuFileExportPicture = new JMenu("Picture");
	// START KGU#486 2018-01-18: Issue #4 icon redesign
//	protected final JMenuItem menuFileExportPicturePNG = new JMenuItem("PNG ...",IconLoader.getIcon(32));
//	protected final JMenuItem menuFileExportPicturePNGmulti = new JMenuItem("PNG (multiple) ...",IconLoader.getIcon(32));
//	protected final JMenuItem menuFileExportPictureEMF = new JMenuItem("EMF ...",IconLoader.getIcon(32));
//	protected final JMenuItem menuFileExportPictureSWF = new JMenuItem("SWF ...",IconLoader.getIcon(32));
//	protected final JMenuItem menuFileExportPicturePDF = new JMenuItem("PDF ...",IconLoader.getIcon(32));
//	protected final JMenuItem menuFileExportPictureSVG = new JMenuItem("SVG ...",IconLoader.getIcon(32));
	protected final JMenuItem menuFileExportPicturePNG = new JMenuItem("PNG ...",IconLoader.getIcon(88));
	protected final JMenuItem menuFileExportPicturePNGmulti = new JMenuItem("PNG (multiple) ...",IconLoader.getIcon(88));
	protected final JMenuItem menuFileExportPictureEMF = new JMenuItem("EMF ...",IconLoader.getIcon(88));
	protected final JMenuItem menuFileExportPictureSWF = new JMenuItem("SWF ...",IconLoader.getIcon(88));
	protected final JMenuItem menuFileExportPicturePDF = new JMenuItem("PDF ...",IconLoader.getIcon(88));
	protected final JMenuItem menuFileExportPictureSVG = new JMenuItem("SVG ...",IconLoader.getIcon(88));
	// END KGU#486 2018-01-18
	protected final JMenu menuFileExportCode = new JMenu("Code");
	// START KGU#171 2016-04-01: Enh. #144 - new menu item for Favourite Code Export
	protected static final LangTextHolder lbFileExportCodeFavorite = new LangTextHolder("Export as % Code");	// Label template for translation
	// START KGU#486 2018-01-18: Issue #4 icon redesign
	//protected final JMenuItem menuFileExportCodeFavorite = new JMenuItem("Export Fav. Code", IconLoader.getIcon(4));
	protected final JMenuItem menuFileExportCodeFavorite = new JMenuItem("Export Fav. Code", IconLoader.getIcon(87));
	// END KGU#486 2018-01-18
	// END KGU#171 2016-04-01
	protected final JMenu menuFileImport = new JMenu("Import");
	// Submenu of "File -> Import"
	// START KGU#354 2017-03-14: Enh. #354 We use one unified menu item for all code import now
	//protected final JMenuItem menuFileImportPascal = new JMenuItem("Pascal Code ...",IconLoader.getIcon(4));
	protected final JMenuItem menuFileImportCode = new JMenuItem("Source Code ...", IconLoader.getIcon(87));
	// END KGU#354 2017-03-14
	//protected final JMenuItem menuFileImportNSDEd = new JMenuItem("Foreign NSD editor file ...", IconLoader.getIcon(74));

	
	// START KGU#363 2017-05-19: Enh. #372
	protected final JMenuItem menuFileAttributes = new JMenuItem("Inspect attributes ...", IconLoader.getIcon(86));
	// END KGU#363 2017-05-19
	// START KGU#2 2015-11-19: New menu item to have the Arranger present the diagram
	protected final JMenuItem menuFileArrange = new JMenuItem("Arrange", IconLoader.getIcon(105));
	// END KGU#2 2015-11-19
	protected final JMenuItem menuFilePrint = new JMenuItem("Print ...",IconLoader.getIcon(41));
    // START BOB 2016-08-02
	protected final JMenuItem menuFileTranslator = new JMenuItem("Translator", IconLoader.getIcon(113));
    // END BOB 2016-08-02
	protected final JMenuItem menuFileQuit = new JMenuItem("Quit", IconLoader.getIcon(122));

	// Menu "Edit"
	protected final JMenu menuEdit = new JMenu("Edit");
	// Submenu of "Edit"
	protected final JMenuItem menuEditUndo = new JMenuItem("Undo",IconLoader.getIcon(39));
	protected final JMenuItem menuEditRedo = new JMenuItem("Redo",IconLoader.getIcon(38));
	protected final JMenuItem menuEditCut = new JMenuItem("Cut",IconLoader.getIcon(44));
	protected final JMenuItem menuEditCopy = new JMenuItem("Copy",IconLoader.getIcon(42));
	protected final JMenuItem menuEditPaste = new JMenuItem("Paste",IconLoader.getIcon(43));
	// START KGU#324 2017-05-30: Enh. #415
	protected final JMenuItem menuEditFindReplace = new JMenuItem("Find/Replace", IconLoader.getIcon(73));
	// END KGU#324 2017-05-30
	protected final JMenuItem menuEditCopyDiagramPNG = new JMenuItem("Copy bitmap diagram to clipboard",IconLoader.getIcon(32));
	protected final JMenuItem menuEditCopyDiagramEMF = new JMenuItem("Copy vector diagram to clipboard",IconLoader.getIcon(32));
	// START KGU#282 2016-10-16: Issue #272: Options to upgrade or downgrade graphics
	protected final JMenuItem menuEditUpgradeTurtle = new JMenuItem("To fine graphics",IconLoader.getIcon(27));
	protected final JMenuItem menuEditDowngradeTurtle = new JMenuItem("To integer graphics",IconLoader.getIcon(28));
	// END KGU#282 2016-10-16
	// START KGU#602 2018-10-26: enh. #619
	protected final JMenuItem menuEditBreakLines = new JMenuItem("(Re-)break text lines ...", IconLoader.getIcon(56));
	// END KGU#602 2018-10-26
	// START KGU#667 2019-02-26: Enh. #689 - summon the called subroutine for editing
	protected final JMenuItem menuEditSummonSub = new JMenuItem("Edit subroutine ...", IconLoader.getIcon(21));
	// END KGU#667 2019-02-26

	protected final JMenu menuView = new JMenu("View");

	// Menu "Diagram"
	protected final JMenu menuDiagram = new JMenu("Diagram");
	// Submenus of "Diagram"
	protected final JMenu menuDiagramAdd = new JMenu("Add");
	// Submenu "Diagram -> Add -> Before"
	protected final JMenu menuDiagramAddBefore = new JMenu("Before");
	// Submenus for adding Elements "Before"
	protected final JMenuItem menuDiagramAddBeforeInst = new JMenuItem("Instruction",IconLoader.getIcon(/*7*/57));
	protected final JMenuItem menuDiagramAddBeforeAlt = new JMenuItem("IF statement",IconLoader.getIcon(/*8*/60));
	protected final JMenuItem menuDiagramAddBeforeCase = new JMenuItem("CASE statement",IconLoader.getIcon(/*47*/64));
	// START KGU#493 2018-02-12: Issue #4
	//protected final JMenuItem menuDiagramAddBeforeFor = new JMenuItem("FOR loop",IconLoader.getIcon(9));
	protected final JMenuItem menuDiagramAddBeforeFor = new JMenuItem("FOR loop",IconLoader.getIcon(/*95*/74));
	// END KGU#493 2018-02-12
	protected final JMenuItem menuDiagramAddBeforeWhile = new JMenuItem("WHILE loop",IconLoader.getIcon(/*10*/62));
	protected final JMenuItem menuDiagramAddBeforeRepeat = new JMenuItem("REPEAT loop",IconLoader.getIcon(/*11*/63));
	protected final JMenuItem menuDiagramAddBeforeForever = new JMenuItem("ENDLESS loop",IconLoader.getIcon(/*9*/61));
	protected final JMenuItem menuDiagramAddBeforeCall = new JMenuItem("Call",IconLoader.getIcon(/*49*/58));
	protected final JMenuItem menuDiagramAddBeforeJump = new JMenuItem("Jump",IconLoader.getIcon(/*56*/59));
	protected final JMenuItem menuDiagramAddBeforePara = new JMenuItem("Parallel",IconLoader.getIcon(/*90*/91));
	// START KGU#686 2019-03-16: Enh. #56
	protected final JMenuItem menuDiagramAddBeforeTry = new JMenuItem("Try-Catch",IconLoader.getIcon(120));
	// END KGU#686 2019-03-16

	// Submenu "Diagram -> Add -> After"
	protected final JMenu menuDiagramAddAfter = new JMenu("After");
	// Submenus for adding Elements "After"
	protected final JMenuItem menuDiagramAddAfterInst = new JMenuItem("Instruction",IconLoader.getIcon(/*12*/57));
	protected final JMenuItem menuDiagramAddAfterAlt = new JMenuItem("IF statement",IconLoader.getIcon(/*13*/60));
	protected final JMenuItem menuDiagramAddAfterCase = new JMenuItem("CASE statement",IconLoader.getIcon(/*48*/64));
	// START KGU#493 2018-02-12: Issue #4
	//protected final JMenuItem menuDiagramAddAfterFor = new JMenuItem("FOR loop",IconLoader.getIcon(14));
	protected final JMenuItem menuDiagramAddAfterFor = new JMenuItem("FOR loop",IconLoader.getIcon(/*97*/74));
	// END KGU#493 2018-02-12
	protected final JMenuItem menuDiagramAddAfterWhile = new JMenuItem("WHILE loop",IconLoader.getIcon(/*15*/62));
	protected final JMenuItem menuDiagramAddAfterRepeat = new JMenuItem("REPEAT loop",IconLoader.getIcon(/*16*/63));
	protected final JMenuItem menuDiagramAddAfterForever = new JMenuItem("ENDLESS loop",IconLoader.getIcon(/*14*/61));
	protected final JMenuItem menuDiagramAddAfterCall = new JMenuItem("Call",IconLoader.getIcon(/*50*/58));
	protected final JMenuItem menuDiagramAddAfterJump = new JMenuItem("Jump",IconLoader.getIcon(/*55*/59));
	protected final JMenuItem menuDiagramAddAfterPara = new JMenuItem("Parallel",IconLoader.getIcon(/*89*/91));
	// START KGU#686 2019-03-16: Enh. #56
	protected final JMenuItem menuDiagramAddAfterTry = new JMenuItem("Try-Catch",IconLoader.getIcon(120));
	// END KGU#686 2019-03-16

	protected final JMenuItem menuDiagramEdit = new JMenuItem("Edit",IconLoader.getIcon(6));
	protected final JMenuItem menuDiagramDelete = new JMenuItem("Delete",IconLoader.getIcon(5));
	protected final JMenuItem menuDiagramMoveUp = new JMenuItem("Move up",IconLoader.getIcon(19));
	protected final JMenuItem menuDiagramMoveDown = new JMenuItem("Move down",IconLoader.getIcon(20));
	// START KGU#199 2016-07-06: Enh. #188 - We allow instruction conversion
	protected final JMenuItem menuDiagramTransmute = new JMenuItem("Transmute", IconLoader.getIcon(109));
	// END KGU#199 2016-07-06
	// START KGU#365 2017-03-23: Enh. #380 - conversion of sequence in a subroutine
	protected final JMenuItem menuDiagramOutsource = new JMenuItem("Outsource", IconLoader.getIcon(68));
	// END KGU#365 2017-03-23
	// START KGU#123 2016-01-03: New menu items for collapsing/expanding (addresses #65)
	protected final JMenuItem menuDiagramCollapse = new JMenuItem("Collapse", IconLoader.getIcon(106));
	protected final JMenuItem menuDiagramExpand = new JMenuItem("Expand", IconLoader.getIcon(107));
	// END KGU#123 2016-01-03
	// START KGU#310 2016-12-14: Renamed and moved to menu "Debug"
//	// START KGU#277 2016-10-13: Enh. #270: Disabling of elements
//	protected final JMenuItem menuDiagramDisable = new JMenuItem("Disable", IconLoader.getIcon(26));
//	// END KGU#277 2016-10-13
//	// START KGU#143 2016-01-21: Bugfix #114 - Compensate editing restriction by accelerator4
//	protected final JMenuItem menuDiagramBreakpoint = new JMenuItem("Toggle Breakpoint", IconLoader.getIcon(103));
//	// END KGU#143 2016-01-21
//	// START KGU#213 2016-08-02: Enh. #215
//	protected final JMenuItem menuDiagramBreakTrigger = new JMenuItem("Specify break trigger...", IconLoader.getIcon(112));
//	// END KGU#143 2016-08-02
	// END KGU#310 2016-12-14

	protected final JMenu menuDiagramType = new JMenu("Type");
	protected final JCheckBoxMenuItem menuDiagramTypeProgram = new JCheckBoxMenuItem("Main", IconLoader.getIcon(22));
	protected final JCheckBoxMenuItem menuDiagramTypeFunction = new JCheckBoxMenuItem("Sub", IconLoader.getIcon(21));
	//START KGU#376 2017-05-16: Enh. #389
	protected final JCheckBoxMenuItem menuDiagramTypeInclude = new JCheckBoxMenuItem("Includable", IconLoader.getIcon(71));
	//END KGU#376 2017-05-16
	protected final JCheckBoxMenuItem menuDiagramUnboxed = new JCheckBoxMenuItem("Unframed diagram?", IconLoader.getIcon(40));
	protected final JCheckBoxMenuItem menuDiagramComment = new JCheckBoxMenuItem("Show comments?", IconLoader.getIcon(77));
	protected final JCheckBoxMenuItem menuDiagramMarker = new JCheckBoxMenuItem("Highlight variables?", IconLoader.getIcon(79));
	protected final JCheckBoxMenuItem menuDiagramDIN = new JCheckBoxMenuItem("DIN 66261?", IconLoader.getIcon(82));
	protected final JCheckBoxMenuItem menuDiagramAnalyser = new JCheckBoxMenuItem("Analyse structogram?", IconLoader.getIcon(83));
	protected final JCheckBoxMenuItem menuDiagramSwitchComments = new JCheckBoxMenuItem("Switch text/comments?", IconLoader.getIcon(102));
	// START KGU#227 2016-07-31: Enh. #128
	protected final JCheckBoxMenuItem menuDiagramCommentsPlusText = new JCheckBoxMenuItem("Comments plus texts?", IconLoader.getIcon(111));
	// END KGU#227 2016-07-31
	// START KGU#477 2017-12-06: Enh. #487
	protected final JCheckBoxMenuItem menuDiagramHideDeclarations = new JCheckBoxMenuItem("Hide declarations?", IconLoader.getIcon(85));
	// END KGU#477 2017-12-06
	// START KGU#305 2016-12-14: Enh. #305
	protected final JCheckBoxMenuItem menuDiagramIndex = new JCheckBoxMenuItem("Show Arranger index?", IconLoader.getIcon(29));
	// END KGU#305 2016-12-14
	// START KGU#705 2019-09-23: Enh. #738
	protected final JCheckBoxMenuItem menuDiagramPreview = new JCheckBoxMenuItem("Show Code preview?", IconLoader.getIcon(87));
	// END KGU#705 2019-09-23

	// Menu "Preferences"
	// START KGU#466 2019-08-02: Issue #733 - prepare a selective preferences export, lazy initialisation
	private static final HashMap<String, String[]> preferenceKeys = new LinkedHashMap<String, String[]>();
	// END KGU#466 2019-08-02
	
	protected final JMenu menuPreferences = new JMenu("Preferences");
	// Submenu of "Preferences"
	// START KGU#300 2016-12-02: Enh. #300
	protected final JCheckBoxMenuItem menuPreferencesNotifyUpdate = new JCheckBoxMenuItem("Notify of new versions?",IconLoader.getIcon(52));
	// END KGU#2016-12-02
	// START KGU#456 2017-11-05: Enh. #452
	protected final JCheckBoxMenuItem menuPreferencesSimplified = new JCheckBoxMenuItem("Simplified toolbars?",IconLoader.getIcon(75));
	// END KGU#456 2017-11-05
	protected final JMenuItem menuPreferencesFont = new JMenuItem("Font ...",IconLoader.getIcon(23));
	protected final JMenuItem menuPreferencesColors = new JMenuItem("Colors ...",IconLoader.getIcon(31));
	protected final JMenuItem menuPreferencesOptions = new JMenuItem("Structures ...",IconLoader.getIcon(40));
	protected final JMenuItem menuPreferencesParser = new JMenuItem("Parser ...",IconLoader.getIcon(4));
	protected final JMenuItem menuPreferencesAnalyser = new JMenuItem("Analyser ...",IconLoader.getIcon(83));
	// START KGU#309 2016-12-15: Enh. #310 - new options for saving diagrams
	protected final JMenuItem menuPreferencesSaving = new JMenuItem("Saving ...",IconLoader.getIcon(3));
	// END KGU#309 2016-12-15
	protected final JMenuItem menuPreferencesExport = new JMenuItem("Export ...",IconLoader.getIcon(32));
	protected final JMenuItem menuPreferencesImport = new JMenuItem("Import ...",IconLoader.getIcon(25));
	protected final JMenu menuPreferencesLanguage = new JMenu("Language");
	// START KGU#242 2016-09-04: Structural redesign - generic generation of language menu items
	protected final Hashtable<String, JCheckBoxMenuItem> menuPreferencesLanguageItems = new Hashtable<String, JCheckBoxMenuItem>(Locales.LOCALES_LIST.length);
	// END KGU#242 2016-09-04
	// START KGU#232 2016-08-03/2016-09-06: Enh. #222
	protected final JMenuItem menuPreferencesLanguageFromFile = new JCheckBoxMenuItem("From file ...",IconLoader.getLocaleIconImage("empty"));
	// END KGU#232 2016-08-03/2016-09-06
	// START KGU#479 2017-12-14: Enh. #492
	protected final JMenuItem menuPreferencesElements = new JMenuItem("Element names ...", IconLoader.getIcon(57));
	// END KGU#479 2017-12-14
	// START KGU#480 2018-01-18: Enh. #490 - Aliases for controller API
	// START KGU#486 2018-02-06: Issue #4
	//protected final JMenuItem menuPreferencesCtrlAliases = new JMenuItem("Controller aliases ...", IconLoader.turtle);
	protected final JMenuItem menuPreferencesCtrlAliases = new JMenuItem("Controller aliases ...", IconLoader.getIcon(54));
	// END KGU#486 2018-02-06
	// END KGU#480 2018-01-18
	protected final JMenu menuPreferencesLookAndFeel = new JMenu("Look & Feel");
	// START KGU#503 2018-03-14: Enh. #519
	protected final JMenu menuPreferencesWheel = new JMenu("Mouse Wheel");
	// START KGU#123 2016-01-04: Enh. #87
	protected final JCheckBoxMenuItem menuPreferencesWheelCollapse = new JCheckBoxMenuItem("Mouse wheel for collapsing?", IconLoader.getIcon(108));
	// END KGU#123 2016-01-04
	protected final JCheckBoxMenuItem menuPreferencesWheelZoom = new JCheckBoxMenuItem("Reverse zoom with ctr + wheel", IconLoader.getIcon(7));
	// END KGU#503 2018-03-14
	// START KGU#699 2019-03-27: Issue #717
	protected final JMenuItem menuPreferencesWheelUnit = new JMenuItem("Mouse wheel scrolling unit ...", IconLoader.getIcon(9));
	// END KGU#699 2019-03-27
	// START KGU#287 2017-01-11: Issue #81/#330
	protected final JMenuItem menuPreferencesScalePreset = new JMenuItem("GUI Scaling ...", IconLoader.getIcon(51));
	// END KGU#287 2017-01-11
	protected final JMenu menuPreferencesSave = new JMenu("Save or load preferences ...");
	protected final JMenuItem menuPreferencesSaveAll = new JMenuItem("Save now");
	protected final JMenuItem menuPreferencesSaveLoad = new JMenuItem("Load from file ...");
	protected final JMenuItem menuPreferencesSaveDump = new JMenuItem("Save to file ...");
	// START KGU#721 2019-08-06: Enh. #740
	protected final JMenuItem menuPreferencesSaveRestore = new JMenuItem("Restore last backup");
	// END KGU#721 2019-08-06

	// START KGU#310 2016-12-14
	// Menu "Debug"
	protected final JMenu menuDebug = new JMenu("Debug");
	// Submenu of "Debug"
	// START KGU#486 2018-02-06: Issue #4
	//protected final JMenuItem menuDebugTurtle = new JMenuItem("Turtleizer ...", IconLoader.turtle);
	protected final JMenuItem menuDebugTurtle = new JMenuItem("Turtleizer ...", IconLoader.getIcon(54));
	// END KGU#486 2018-02-06
	protected final JMenuItem menuDebugExecute = new JMenuItem("Executor ...", IconLoader.getIcon(4));
	protected final JMenuItem menuDebugBreakpoint = new JMenuItem("Toggle breakpoint", IconLoader.getIcon(103));
	protected final JMenuItem menuDebugBreakTrigger = new JMenuItem("Specify break trigger ...", IconLoader.getIcon(112));
	protected final JMenuItem menuDebugDropBrkpts = new JMenuItem("Clear breakpoints", IconLoader.getIcon(104));
	protected final JMenuItem menuDebugDisable = new JMenuItem("Disable", IconLoader.getIcon(26));
	// END KGU#310 2016-12-14

	// Menu "Help"
	protected final JMenu menuHelp = new JMenu("Help");
	// Submenu of "Help"
	// START KGU#208 2016-07-22: Enh. #199
	protected final JMenuItem menuHelpOnline = new JMenuItem("User Guide",IconLoader.getIcon(110));
	// END KGU#208 2016-07-22
	protected final JMenuItem menuHelpAbout = new JMenuItem("About ...",IconLoader.getIcon(17));
	protected final JMenuItem menuHelpUpdate = new JMenuItem("Update ...",IconLoader.getIcon(52));

	// START KGU#239 2016-08-12: Enh. #231
	/** Generator plugins accessible for Analyser, {@link Diagram}, {@link ExportOptionDialoge} etc. */
	public static Vector<GENPlugin> generatorPlugins = new Vector<GENPlugin>();
	// END KGU#239 2016-08-12
	// START KGU#448/KGU#480 2018-01-08: Enh. #443, #490
	/** {@link DiagramController} plugins accessible for {@link Diagram}, {@link DiagramControllerAliases} etc. */
	public static Vector<GENPlugin> controllerPlugins = new Vector<GENPlugin>();
	// END KGU#448/KGU#480 2018-01-08
	// Error messages for Analyser
	// START KGU#220 2016-07-27: Enh. as proposed in issue #207
	public static final LangTextHolder warning_1 = new LangTextHolder("WARNING: TEXTS AND COMMENTS ARE EXCHANGED IN DISPLAY! → Menu \"%1 ► %2\".");
	// END KGU#220 2016-07-27
	// START KGU#456 2017-11-05: Enh. #452
	public static final LangTextHolder warning_2 = new LangTextHolder("NOTE: «%4» is active. To switch it off → Menu \"%1 ► %2 ► %3\".");	
	// END KGU#456 2017-11-05
	public static final LangTextHolder error01_1 = new LangTextHolder("WARNING: No loop variable detected ...");
	public static final LangTextHolder error01_2 = new LangTextHolder("WARNING: More than one loop variable detected: «%»");
	public static final LangTextHolder error01_3 = new LangTextHolder("You are not allowed to modify the loop variable «%» inside the loop!");
	public static final LangTextHolder error02 = new LangTextHolder("No change of the variables in the condition detected. Possible endless loop ...");
	// START KGU#375 2017-04-05: Enh. #390 Mor precise informaton for multi-line instructions
	//public static final LangTextHolder error03_1= new LangTextHolder("The variable «%» has not yet been initialized!");
	//public static final LangTextHolder error03_2 = new LangTextHolder("The variable «%» may not have been initialized!");
	public static final LangTextHolder error03_1= new LangTextHolder("The variable «%1» has not yet been initialized%2!");
	public static final LangTextHolder error03_2 = new LangTextHolder("The variable «%1» may not have been initialized%2!");
	// END KGU#375 2017-04-05
	public static final LangTextHolder error04 = new LangTextHolder("You are not allowed to use an IF-statement with an empty TRUE-block!");
	public static final LangTextHolder error05 = new LangTextHolder("The variable «%» must be written in uppercase!");
	public static final LangTextHolder error06 = new LangTextHolder("The programname «%» must be written in uppercase!");
	public static final LangTextHolder error07_1 = new LangTextHolder("«%» is not a valid name for a program or function!");
	public static final LangTextHolder error07_2 = new LangTextHolder("«%» is not a valid name for a parameter!");
	public static final LangTextHolder error07_3 = new LangTextHolder("«%» is not a valid name for a variable!");
	public static final LangTextHolder error07_4 = new LangTextHolder("Program names should not contain spaces, better put underscores between the words: «%».");
	// START KGU#456 2017-11-04: Enh. #452 - Be more friendly to newbees
	public static final LangTextHolder hint07_1 = new LangTextHolder("What is your algorithm to do? Replace «%» with a good name for it!");
	// END KGU#456 2017-11-01
	public static final LangTextHolder error08 = new LangTextHolder("It is not allowed to make an assignment inside a condition.");
	public static final LangTextHolder error09 = new LangTextHolder("Your program («%») cannot have the same name as a variable or parameter!");
	public static final LangTextHolder error10_1 = new LangTextHolder("A single instruction element should not contain input/output instructions and assignments!");
	public static final LangTextHolder error10_2 = new LangTextHolder("A single instruction element should not contain input and output instructions!");
	public static final LangTextHolder error10_3 = new LangTextHolder("A single instruction element should not contain input instructions and assignments!");
	public static final LangTextHolder error10_4 = new LangTextHolder("A single instruction element should not contain ouput instructions and assignments!");
	// START KGU#375 2017-04-05: Enh. #388
	public static final LangTextHolder error10_5 = new LangTextHolder("A single instruction element should not mix constant definitions with other instructions!");
	// END KGU#375 2017-04-05
	// START KGU#388 2017-09-13: Enh. #423
	public static final LangTextHolder error10_6 = new LangTextHolder("A single instruction element should not mix type definitions with other instructions!");
	// END KGU#388 2017-09-13
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
	public static final LangTextHolder error15_2 = new LangTextHolder("The called diagram «%» is currently not available.");
	// END KGU#278 2016-10-11
	// START KGU 2016-12-17
	public static final LangTextHolder error15_3 = new LangTextHolder("There are several diagrams matching signature «%».");
	// END KGU 2016-12-17
	public static final LangTextHolder error16_1 = new LangTextHolder("A JUMP element may be empty or start with one of %, possibly followed by an argument!");	
	public static final LangTextHolder error16_2 = new LangTextHolder("A % instruction, unless at final position, must form a JUMP element!");
	public static final LangTextHolder error16_3 = new LangTextHolder("A %1 or %2 instruction is only allowed as JUMP element!");
	public static final LangTextHolder error16_4 = new LangTextHolder("Cannot leave or break more loop levels than being nested in («%»)!");
	public static final LangTextHolder error16_5 = new LangTextHolder("You must not directly jump (%1, %2) out of a parallel thread!");
	public static final LangTextHolder error16_6 = new LangTextHolder("Illegal argument for a «%» JUMP (must be an integer constant)!");
	public static final LangTextHolder error16_7 = new LangTextHolder("Instruction isn't reachable after a JUMP!");
	public static final LangTextHolder error16_8 = new LangTextHolder("The argument for this «%» JUMP might be unsuited (should be an integer value).");
	// END KGU#2 2015-11-25
	// START KGU#47 2015-11-28: New check for concurrency problems
	public static final LangTextHolder error17 = new LangTextHolder("Consistency risk due to concurrent access to variable «%» by several parallel threads!");
	// END KGU#47 2015-11-28
	// START KGU#239 2016-08-12: Enh. #231 - New checks for variable name collisions
	public static final LangTextHolder error18 = new LangTextHolder("Variable name «%1» may be confused with variable(s) «%2» in some case-indifferent languages!");
	public static final LangTextHolder error19_1 = new LangTextHolder("Variable name «%1» may collide with reserved names in languages like %2!");
	public static final LangTextHolder error19_2 = new LangTextHolder("Variable name «%» is reserved in Structorizer and is likely to cause trouble on execution!");
	// END KGU#239 2016-08-12
	// START KGU#253 2016-09-21: Enh. #249 - New check for subroutine syntax.
	// START KGU#371 2019-03-07: Enh. #385 - default parameter check added
	//public static final LangTextHolder error20 = new LangTextHolder("A subroutine header must have a (possibly empty) parameter list within parentheses.");
	public static final LangTextHolder error20_1 = new LangTextHolder("A subroutine header must have a (possibly empty) parameter list within parentheses.");
	public static final LangTextHolder error20_2 = new LangTextHolder("Parameters with default must be placed contiguously at the parameter list end.");
	// END KGU#371 2019-03-07
	// END KGU#253 2016-09-21
	// START KGU#327 2017-01-07: Enh. #329 - New check for hardly distinguishable variable names.
	public static final LangTextHolder error21 = new LangTextHolder("Variable names I (upper-case i), l (lower-case L), and O (upper-case o) are hard to distinguish from each other, 1, or 0.");
	// END KGU#327 2017-01-07
	// START KGU#375 2017-04-04: Enh. #388 - New check for constants depending on non-constant values or be redefined.
	public static final LangTextHolder error22_1 = new LangTextHolder("Constant «%1» depends on apparently non-constant value(s) %2.");
	public static final LangTextHolder error22_2 = new LangTextHolder("Attempt to modify the value of constant «%»!");
	public static final LangTextHolder errorLineReference = new LangTextHolder(" (line %)");
	// END KGU#375 2017-04-04
	// START KGU#376 2017-04-11/21: Enh. #389 
	public static final LangTextHolder error23_1 = new LangTextHolder("Diagram «%» is rather unsuited to be included as it makes use of return.");
	public static final LangTextHolder error23_2 = new LangTextHolder("Import of diagram «%» is recursive!");
	public static final LangTextHolder error23_3 = new LangTextHolder("Import of diagram «%1» will be ignored here because it had already been imported: %2");
	public static final LangTextHolder error23_4 = new LangTextHolder("Name conflict between local and imported variable or constant «%»!");
	public static final LangTextHolder error23_5 = new LangTextHolder("An includable diagram «%» is currently not available!");
	public static final LangTextHolder error23_6 = new LangTextHolder("More than one includable diagrams with name «%» found!");
	// END KGU#376 2017-04-11/21
	// START KGU#388 2017-09-17: Enh. #423: imported record type definitions
	public static final LangTextHolder error23_7 = new LangTextHolder("There is a name conflict between local and imported type definition «%»!");
	// END KGU#388 2017-09-17
	// START KGU#388 2017-09-13: Enh. #423
	public static final LangTextHolder error24_1 = new LangTextHolder("Type definition in (composed) line % is malformed.");
	public static final LangTextHolder error24_2 = new LangTextHolder("Type name «%» is illegal or colliding with another identifier.");
	public static final LangTextHolder error24_3 = new LangTextHolder("Component name «%» is illegal or duplicate.");
	public static final LangTextHolder error24_4 = new LangTextHolder("Component type «%» is undefined or unknown.");
	public static final LangTextHolder error24_5 = new LangTextHolder("There is no defined record type «%»!");
	public static final LangTextHolder error24_6 = new LangTextHolder("Record component «%» will not be modified/initialized!");
	public static final LangTextHolder error24_7 = new LangTextHolder("Record type «%1» hasn't got a component «%2»!");
	public static final LangTextHolder error24_8 = new LangTextHolder("Variable «%1» hasn't got a component «%2»!");
	// END KGU#388 2017-09-13
	// START KGU#456 2017-11-04: Enh. #452 - Be more helpful to newbees
	public static final LangTextHolder hint25_1 = new LangTextHolder("Select the diagram centre and place a first element, e.g. an input instruction like «%1 %2»");
	public static final LangTextHolder hint25_2 = new LangTextHolder("You might want to input data, e.g. with an instruction like «%1 %2». → Menu \"%3\"");
	public static final LangTextHolder hint25_3 = new LangTextHolder("You might want to print results, e.g. with an instruction like «%1 %2». → Menu \"%3\"");
	public static final LangTextHolder hint25_4 = new LangTextHolder("Select the diagram centre and place a first element, e.g. an Instruction.");
	public static final LangTextHolder hint25_5 = new LangTextHolder("Select the diagram centre and place e.g. an Instruction element with a type or constant definition.");
	public static final LangTextHolder hint25_6 = new LangTextHolder("You might want to place some processing instruction like «%1» between input and output. → Menu \"%2\"");
	public static final LangTextHolder[] hint26 = {
			new LangTextHolder("Select the diagram centre, double-click, press Enter or F5, and put in the instruction text «% \"Hello world!\"»"),
			new LangTextHolder("Now you may e.g. test your diagram: → Menu \"%1 ► %2\""),
			new LangTextHolder("Now you may e.g. save your diagram: → Menu \"%1 ► %2\""),
			new LangTextHolder("Now you may e.g. export your diagram to some programming language: → Menu \"%1 ► %2 ► %3\""),
			new LangTextHolder("Now you may e.g. export your diagram as graphics file: → Menu \"%1 ► %2 ► %3\"")
	};
	// END KGU#456 2017-11-01
	// START KGU#459 2017-11-14: Enh. #459
	public static final LangTextHolder msgGuidedTours = new LangTextHolder("You activated guided tours.\n\nWatch out for recommendations\nor instructions\nin the bottom text pane\n(Analyser report list).");
	public static final LangTextHolder msgGuidedTourDone = new LangTextHolder("Congratulations - you finished the tutorial «%».");
	public static final LangTextHolder msgGuidedTourNext = new LangTextHolder("%1\nTutorial «%2» is going to start now.\nYou may want to clear the diagram first via menu \"%3\"");
	public static final LangTextHolder ttlGuidedTours = new LangTextHolder("Guided Tours");
	// END KGU#459 2017-11-14

	// START KGU#218 2016-07-28: Issue #206 - enhanced localization
	// Dialog messages
	public static final LangTextHolder msgDialogExpCols = new LangTextHolder("Into how many columns do you want to split the output?");
	public static final LangTextHolder msgDialogExpRows = new LangTextHolder("Into how many rows do you want to split the output?");
	public static final LangTextHolder msgOverwriteFile = new LangTextHolder("Overwrite existing file?");
	public static final LangTextHolder msgOverwriteFiles = new LangTextHolder("Existing file(s) detected. Overwrite?");
	// START KGU#553 2018-07-10: Issue #557
	public static final LangTextHolder msgOverwriteFile1 = new LangTextHolder("Overwrite existing file \"%\"?");
	public static final LangTextHolder msgCancelAll = new LangTextHolder("Cancel the saving of all remaining files?");
	public static final LangTextHolder lblAcceptProposedNames = new LangTextHolder("Accept proposed names for all files");
	// END KGU#553 2018-07-10
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
	// START KGU#509 2018-03-20: Bugfix #526
	public static final LangTextHolder msgErrorFileRename = new LangTextHolder("Error(s) on renaming the saved file:\n%1Look for file \"%2\" and move/rename it yourself."); 
	// END KGU#509 2018-03-20
	// START KGU#232 2016-08-02: Enh. #222
	public static final LangTextHolder msgOpenLangFile = new LangTextHolder("Open language file");
	public static final LangTextHolder msgLangFile = new LangTextHolder("Structorizer language file");
	// END KGU#232 2016-08-02
	// START KGU#247 2016-09-15: Issue #243: Forgotten message box translations
	public static final LangTextHolder msgTitleError = new LangTextHolder("Error");
	public static final LangTextHolder msgTitleWarning = new LangTextHolder("Warning");
	public static final LangTextHolder msgTitleLoadingError = new LangTextHolder("Loading Error");
	public static final LangTextHolder msgTitleParserError = new LangTextHolder("Parser Error");
	public static final LangTextHolder msgTitleURLError = new LangTextHolder("URL Error");
	public static final LangTextHolder msgTitleQuestion = new LangTextHolder("Question");
	public static final LangTextHolder msgTitleWrongInput = new LangTextHolder("Wrong Input");
	public static final LangTextHolder msgTitleOpen = new LangTextHolder("Open file ...");
	public static final LangTextHolder msgTitleSave = new LangTextHolder("Save file ...");
	public static final LangTextHolder msgTitleSaveAs = new LangTextHolder("Save file as ...");
	public static final LangTextHolder msgTitleImport = new LangTextHolder("Code import - choose source file (mind the file filter!) ...");
	public static final LangTextHolder msgTitleNSDImport = new LangTextHolder("Import a diagram file from % ...");
	public static final LangTextHolder msgSaveChanges = new LangTextHolder("Do you want to save the current NSD file?");
	public static final LangTextHolder msgErrorImageSave = new LangTextHolder("Error on saving the image(s)!");
	public static final LangTextHolder msgErrorUsingGenerator = new LangTextHolder("Error while using % generator");
	// END KGU#247 2016-09-15
	// START KGU#634 2019-01-17: Issue #664
	public static final LangTextHolder msgVetoClose = new LangTextHolder("Structorizer is going to close. Veto?");
	// END KGU#634 2019-01-17
	// START KGU#354 2017-03-04
	public static final LangTextHolder msgErrorUsingParser = new LangTextHolder("Error while using % parser");
	// END KGU#354 2017-03-04
	// START KGU#247 2016-09-17: Issue #243: Forgotten message box translation
	public static final LangTextHolder msgGotoHomepage = new LangTextHolder("Go to % to look for updates<br/>and news about Structorizer.");
	public static final LangTextHolder msgErrorNoFile = new LangTextHolder("File not found!");
	public static final LangTextHolder msgBrowseFailed = new LangTextHolder("Failed to show % in browser");
	// END KGU#247 2016-09-17
	// START KGU#258 2016-10-03: Enh. #253: Diagram keyword refactoring
	// START KGU#718 2019-08-01: Modified layout for the refactoring proposal
	//public static final LangTextHolder msgRefactoringOffer = new LangTextHolder("Keywords configured in the Parser Preferences were replaced:%Are loaded diagrams to be refactored accordingly?");
	public static final LangTextHolder[] hdrRefactoringTable = new LangTextHolder[] {
			new LangTextHolder("Reference"),
			new LangTextHolder("Old keyword"),
			new LangTextHolder("New keyword")
			};
	public static final LangTextHolder msgRefactoringOffer1 = new LangTextHolder("Keywords configured in the Parser Preferences were replaced:");
	public static final LangTextHolder msgRefactoringOffer2 = new LangTextHolder("Are loaded diagrams to be refactored accordingly?");
	// START KGU#718 2019-08-01
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
	// START KGU#456 2017-11-05: Enh. #452 (KGU#655 2019-02-15: split into two texts 
	//public static final LangTextHolder msgWelcomeMessage = new LangTextHolder("Welcome to Structorizer, your Nassi-Shneiderman diagram editor.\n"
	//		+ "With this tool you may design, test, analyse, export algorithms and many things more.\n"
	//		+ "It was designed for easy handling but has already gained a lot of functions.\n\n"
	//		+ "If you are an absolute beginner then you may start with a reduced toolbar and a \"%\".\n"
	//		+ "Do you want to start in the simplified and guided mode? (You can always switch to full mode.)");
	public static final LangTextHolder msgWelcomeMessage1 = new LangTextHolder(
			"Welcome to Structorizer, your easy Nassi-Shneiderman diagram editor.\n"
			+ "With this tool you may design, test, analyse, export algorithms and many things more.\n\n"
			+ "Please choose your initial dialog language (you may always change this later via the menu):");
	public static final LangTextHolder msgWelcomeMessage2 = new LangTextHolder(
			"Structorizer was designed for intuitive handling but has already gained a lot of extras.\n\n"
			+ "If you are an absolute beginner then you may start with a reduced toolbar and a \"%\".\n"
			+ "Do you want to start in the simplified and guided mode? (You can always switch to full mode.)");
	public static final LangTextHolder lblReduced = new LangTextHolder("Yes, reduced mode");
	public static final LangTextHolder lblNormal = new LangTextHolder("No, normal mode");
	// END KGU#456 2017-11-05
	// START KGU#354 2017-03-04: Enh. #354 Now generic import menu
	//public static final LangTextHolder lblImportCode = new LangTextHolder("% Code ...");
	public static final LangTextHolder lblCopyToClipBoard = new LangTextHolder("OK + Copy to Clipboard");
	public static final LangTextHolder ttlCodeImport = new LangTextHolder("Source code import");
	public static final LangTextHolder msgSelectParser = new LangTextHolder("The source file type of \"%\" is ambiguous.\nPlease select an import language/parser:");
	public static final LangTextHolder msgImportCancelled = new LangTextHolder("Code import for file \"%\" cancelled.");
	public static final LangTextHolder msgImportFileReadError = new LangTextHolder("File \"%\" does not exist or cannot be read.");
	// END KGU#354 2017-03-04
	// START KGU#392 2017-05-07: Enh. #354, #399
	public static final LangTextHolder msgUnsupportedFileFormat = new LangTextHolder("These files couldn't be loaded because their format is not known or not supported:\n%");
	// END KGU#392 2017-05-07
	// START KGU#553 2018-07-10: Workaround #557
	public static final LangTextHolder msgTooManyDiagrams = new LangTextHolder("The number of created diagrams exceeds the configured threshold (%) for direct rendering.\nSave all obtained diagrams as files just now?");
	// END KGU#553 2018-07-10
	// START KGU#365 2017-03-27: Enh. #380
	public static final LangTextHolder msgSubroutineName = new LangTextHolder("Name of the subroutine");
	public static final LangTextHolder msgJumpsOutwardsScope = new LangTextHolder(
			"There are JUMP instructions among the selected elements targeting outwards.\n"
			+ "To outsource them would compromise the logic of the program or result in defective code!\n"
			+ "The respective JUMPs are listed below and shown RED in the diagram:\n"
			+ "%\n\n"
			+ "Do you really still insist on continuing?");
	public static final LangTextHolder lblContinue = new LangTextHolder("Yes, continue");
	public static final LangTextHolder lblCancel = new LangTextHolder("No, cancel");
	// END KGU#365 2017-03-27
	// START KGU#534 2018-06-29: Enh. #552
	public static final LangTextHolder lblYes = new LangTextHolder("Yes");
	public static final LangTextHolder lblNo = new LangTextHolder("No");
	public static final LangTextHolder lblSkip = new LangTextHolder("No, skip");
	public static final LangTextHolder lblModify = new LangTextHolder("No, modify");
	// END KGU#534 2018-06-29
	// START KGU#534 2018-06-27: Enh. #552
	public static final LangTextHolder lblYesToAll = new LangTextHolder("Yes to all");
	public static final LangTextHolder lblNoToAll = new LangTextHolder("No to all");
	// END KGU#534 2018-06-27
	// START KGU#506 2018-03-14: Issue #522 (for enh. #380)
	public static final LangTextHolder msgIncludableName = new LangTextHolder("Name of a (new) includable diagram to move shared types to");
	public static final LangTextHolder msgMustBeIdentifier = new LangTextHolder("Your chosen name was not suited as identifier!");
	// END KGU#506 2018-03-14
	// START KGU#386 2017-04-28
	public static final LangTextHolder msgImportTooltip = new LangTextHolder("Diagram files generated by the Nassi-Shneiderman editor from %");
	// END KGU#386 2017-04-28
	// START KGU#480 2018-01-19: Enh. #490 - table header strings for DiagramControllerAliases
	public static final LangTextHolder msgTabHdrSignature = new LangTextHolder("Signature");
	public static final LangTextHolder msgTabHdrAlias = new LangTextHolder("Alias");
	// END KGU#480 2018-01-19
	// START KGU#602 2018-10-26: Enh. #619 - Opportunity to re-break the text lines in elements
	public static final LangTextHolder ttlBreakTextLines = new LangTextHolder("Limit the line length by word wrapping");
	public static final LangTextHolder msgMaxLineLengthSelected = new LangTextHolder(    "Max. line length in selected element(s): %");
	public static final LangTextHolder msgMaxLineLengthSubstructure = new LangTextHolder("Max. line lengths including substructure: %");
	public static final LangTextHolder lblNewMaxLineLength = new LangTextHolder("New maximum line length:");
	public static final LangTextHolder lblInvolveSubtree = new LangTextHolder("Apply also to substructure");
	public static final LangTextHolder lblPreserveContinuators = new LangTextHolder("Preserve existing soft line breaks");
	// END KGU#602 2018-10-26
	// START KGU#654 2019-02-15: Enh. #681
	public static final LangTextHolder msgSetAsPreferredGenerator = new LangTextHolder("You exported the last %2 times to %1 code.\nDo you want to set %1 as your favourite code export language in the File menu?");
	// END KGU#654 2019-02-15
	// START KGU#667 2019-02-26: Enh. #689
	public static final LangTextHolder msgChooseSubroutine = new LangTextHolder("Choose the subroutine to be edited:");
	public static final LangTextHolder msgCreateSubroutine = new LangTextHolder("Create a new subroutine «%»?");
	// END KGU#667 2019-02-26
	// START KGU#699 2019-03-27: Issue #717
	public static final LangTextHolder ttlMouseScrollUnit = new LangTextHolder("Mouse wheel scrolling unit");
	// END KGU#699 2019-03-27
	// START KGU#466 2019-08-03: Issue #733
	public static final LangTextHolder msgSelectPreferences = new LangTextHolder("Preference Categories To Be Exported");
	public static final LangTextHolder msgAllPreferences = new LangTextHolder("All preferences");
	public static final LangTextHolder msgInvertSelection = new LangTextHolder("Invert selection");
	public static final LangTextHolder ttDiagramMenuSettings = new LangTextHolder("Settings from menu \"%\"");
	public static final LangTextHolder prefsArranger = new LangTextHolder("Arranger");
	// END KGU#466 2019-08-03
	// START KGU#721 2019-08-06: Enh. #740
	protected static final LangTextHolder msgIniBackupFailed = new LangTextHolder("The creation of a backup of the current preferences failed.\nDo you still want to load \"%\"?");
	protected static final LangTextHolder msgIniRestoreFailed = new LangTextHolder("Could not restore the last preferences backup%");
	// END KGU#721 2019-08-06
	// START KGU#725 2019-09-13: Enh. #746 - for later re-translation if necessary
	private Map<JMenuItem, String> importpluginItems = new HashMap<JMenuItem, String>();
	// END KGU#725 2019-09-13

	public void create()
	{
		JMenuBar menubar = this;
		
		// FIXME: This method becomes deprecated with Java 10! Use getMenuShortcutKeyMaskEx() instead in future.
		// OS-dependent key mask for menu shortcuts
		int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		// START KGU#240 2016-09-01: Bugfix #233 - Configured key binding F10 for CASE insertion wasn't effective
		menubar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "none");
		// END KGU#240 2016-09-01

		// Setting up Menu "File" with all submenus and shortcuts and actions
		menubar.add(menuFile);
		menuFile.setMnemonic(KeyEvent.VK_F);

		menuFile.add(menuFileNew);
		menuFileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,menuShortcutKeyMask));
		menuFileNew.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.newNSD(); doButtons(); } } );

		menuFile.add(menuFileSave);
		menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,menuShortcutKeyMask));
		menuFileSave.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.saveNSD(false); doButtons(); } } );

		menuFile.add(menuFileSaveAs);
		menuFileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, (java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK)));
		menuFileSaveAs.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.saveAsNSD(); doButtons(); } } );

		// START KGU#373 2017-03-28: Enh. #387
		menuFile.add(menuFileSaveAll);
		menuFileSaveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, (java.awt.event.InputEvent.SHIFT_DOWN_MASK | menuShortcutKeyMask)));
		menuFileSaveAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.saveAllNSD(); doButtons(); } } );
		//  END KGU#373 2017-03-28

		menuFile.add(menuFileOpen);
		menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,menuShortcutKeyMask));
		menuFileOpen.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.openNSD(); doButtons(); } } );

		menuFile.add(menuFileOpenRecent);
		menuFileOpenRecent.setIcon(IconLoader.getIcon(2));

		menuFile.addSeparator();

		menuFile.add(menuFileImport);

		// START KGU#354 2017-03-04: Enh. #354 / KGU#354 2017-03-14 Dropped again - one menu item for all now
		//menuFileImport.add(menuFileImportPascal);
		//menuFileImportPascal.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importPAS(); doButtons(); } } );
//		// Read parsers from configuration file and add them to the menu
		//addPluginMenuItems(menuFileImportCode, "parsers.xml", IconLoader.getIcon(4), PluginType.PARSER);
		// END KGU#354 2017-03-04
		// START KGU#354 2017-03-14: Enh. #354 We turn back to a single menu entry and leave selection to the FileChooser
		menuFileImport.add(menuFileImportCode);
		// START KGU#486 2018-01-18: Issue #4
		menuFileImport.setIcon(IconLoader.getIcon(25));
		// END KGU#486 2018-01-18
		menuFileImportCode.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importCode(); } });
		menuFileImportCode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,(java.awt.event.InputEvent.SHIFT_DOWN_MASK | menuShortcutKeyMask)));
		// END KGU#354 2017-03-14

		// START KGU#386 2017-04-26
		addPluginMenuItems(menuFileImport, PluginType.IMPORTER, IconLoader.getIcon(0), this.importpluginItems);
		// END KGU#386 2017-04-26
		// START KGU#725 2019-09-13: Enh. #746 - for later re-translation if necessary
		this.msgImportTooltip.addLangEventListener(this);
		// END KGU#725 2019-09-13

		menuFile.add(menuFileExport);
		// START KGU#486 2018-01-18: Issue #4
		menuFileExport.setIcon(IconLoader.getIcon(32));
		// END KGU#486 2018-01-18

		menuFileExport.add(menuFileExportPicture);
		// START KGU#486 2018-01-18: Issue #4
		//menuFileExportPicture.setIcon(IconLoader.getIcon(32));
		menuFileExportPicture.setIcon(IconLoader.getIcon(88));
		// END KGU#486 2018-01-18

		menuFileExportPicture.add(menuFileExportPicturePNG);
		menuFileExportPicturePNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,menuShortcutKeyMask));
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
		// START KGU#486 2018-01-18: Issue #4
		//menuFileExportCode.setIcon(IconLoader.getIcon(4));
		menuFileExportCode.setIcon(IconLoader.getIcon(87));
		// END KGU#486 2018-01-18
		// START KGU#386 2017-04-26: Plugin evaluation outsourced
//		// read generators from file
//		// and add them to the menu
//		BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream("generators.xml"));
//		GENParser genp = new GENParser();
//		// START KGU#239 2016-08-12: Enh. #231
//		//Vector<GENPlugin> plugins = genp.parse(buff);
//		//for(int i=0;i<plugins.size();i++)
//		generatorPlugins = genp.parse(buff);
//		for (int i=0; i < generatorPlugins.size(); i++)
//		// END KGU#239 2016-08-12
//		{
//			// START KGU#239 2016-08-12: Enh. #231
//			//GENPlugin plugin = (GENPlugin) plugins.get(i);
//			GENPlugin plugin = generatorPlugins.get(i);
//			// END KGU#239 2016-08-12
//			// START KGU 2017-04-23
//			//JMenuItem pluginItem = new JMenuItem(plugin.title, IconLoader.getIcon(4));
//			ImageIcon icon = IconLoader.getIcon(4);	// The default icon
//			if (plugin.icon != null && !plugin.icon.isEmpty()) {
//				try {
//					URL iconFile = this.getClass().getResource(plugin.icon);
//					if (iconFile != null) {
//						icon = new ImageIcon(this.getClass().getResource(plugin.icon));
//					}
//				}
//				catch (Exception ex) {}
//			}
//			JMenuItem pluginItem = new JMenuItem(plugin.title, icon);
//			// END KGU 2017-04-23
//			menuFileExportCode.add(pluginItem);
//			final String className = plugin.className;
//			pluginItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.export(className); doButtons(); } } );
//		}
		// START KGU#486 2018-01-18: Issue #4 - Icon redesign
		//generatorPlugins = this.addPluginMenuItems(menuFileExportCode, PluginType.GENERATOR, IconLoader.getIcon(4));
		generatorPlugins = this.addPluginMenuItems(menuFileExportCode, PluginType.GENERATOR, IconLoader.getIcon(87), null);
		// END KGU#486 2018-01-18
		
		// START KGU#171 2016-04-01: Enh. #144 - accelerated export to favourite target language
		menuFile.add(menuFileExportCodeFavorite);
		menuFileExportCodeFavorite.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,(java.awt.event.InputEvent.SHIFT_DOWN_MASK | menuShortcutKeyMask)));
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
		menuFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,menuShortcutKeyMask));
		menuFilePrint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.printNSD(); doButtons(); } } );

		// START KGU#2 2015-11-19
		menuFile.add(menuFileArrange);
		//menuFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,menuShortcutKeyMask));
		menuFileArrange.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.arrangeNSD(); doButtons(); } } );
		// END KGU#2 2015-11-19

		menuFile.addSeparator();

		// START KGU#363 2017-05-19: Enh. #372
		menuFile.add(menuFileAttributes);
		menuFileAttributes.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.attributesNSD(); doButtons(); } } );
		menuFileAttributes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, (java.awt.event.InputEvent.ALT_DOWN_MASK /*| menuShortcutKeyMask*/)));

		menuFile.addSeparator();
		// END KGU#363 2017-05-19

		// START BOB 2016-08-02
		menuFile.add(menuFileTranslator);
		menuFileTranslator.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { Translator.launch(NSDControl); } } );
		// END BOB 2016-08-02

		menuFile.addSeparator();

		menuFile.add(menuFileQuit);
		menuFileQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,menuShortcutKeyMask));
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
		menuEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,menuShortcutKeyMask));
		menuEditUndo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.undoNSD(); doButtons(); } } );

		menuEdit.add(menuEditRedo);
		menuEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, (java.awt.event.InputEvent.SHIFT_DOWN_MASK | menuShortcutKeyMask)));
		menuEditRedo.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.redoNSD(); doButtons(); } } );

		menuEdit.addSeparator();

		menuEdit.add(menuEditCut);
		menuEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,menuShortcutKeyMask));
		menuEditCut.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.cutNSD(); doButtons(); } } );

		menuEdit.add(menuEditCopy);
		//Toolkit.getDefaultToolkit().get
		//MenuShortcut ms = new MenuShortcut
		menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,menuShortcutKeyMask));
		menuEditCopy.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyNSD(); doButtons(); } } );

		menuEdit.add(menuEditPaste);
		menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,menuShortcutKeyMask));
		menuEditPaste.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.pasteNSD(); doButtons(); } } );

		menuEdit.addSeparator();
		
		// START KGU#324 2017-05-30: Enh. #415
		menuEdit.add(menuEditSummonSub);
		menuEditSummonSub.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, menuShortcutKeyMask));
		menuEditSummonSub.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editSubNSD(); doButtons(); } } );
		menuEdit.addSeparator();
		// END KGU#324 2017-05-30

		// START KGU#324 2017-05-30: Enh. #415
		menuEdit.add(menuEditFindReplace);
		menuEditFindReplace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,menuShortcutKeyMask));
		menuEditFindReplace.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.findAndReplaceNSD(); doButtons(); } } );
		menuEdit.addSeparator();
		// END KGU#324 2017-05-30

		// START KGU#282 2016-10-16: Issue #272: Options to upgrade or downgrade graphics
		menuEdit.add(menuEditUpgradeTurtle);
		// START KGU#626 2019-01-04: Enh. #657 - Accelerator withdrawn, we need ctrl-g etc. now for the grouping mechanism
		//menuEditUpgradeTurtle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		// END KGU#626 2019-01-04
		menuEditUpgradeTurtle.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.replaceTurtleizerAPI(true); doButtons(); } } );

		menuEdit.add(menuEditDowngradeTurtle);
		// START KGU#626 2019-01-04: Enh. #657 - Accelerator withdrawn, we need ctrl-g etc. now for the grouping mechanism
		//menuEditDowngradeTurtle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, menuShortcutKeyMask));
		// END KGU#626 2019-01-04
		menuEditDowngradeTurtle.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.replaceTurtleizerAPI(false); doButtons(); } } );

		menuEdit.addSeparator();
		// END KGU#282 2016-10-16

		menuEdit.add(menuEditBreakLines);
		menuEditBreakLines.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.rebreakLines(); doButtons(); } } );

		menuEdit.addSeparator();
		
		menuEdit.add(menuEditCopyDiagramPNG);
		menuEditCopyDiagramPNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,menuShortcutKeyMask));
		menuEditCopyDiagramPNG.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyToClipboardPNG();; doButtons(); } } );

		if(!System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
		{
			menuEdit.add(menuEditCopyDiagramEMF);
			// START KGU#324 2017-11-09: Enh. #415 Ctrl-F now needed for Find & Replace
			//menuEditCopyDiagramEMF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, menuShortcutKeyMask));
			menuEditCopyDiagramEMF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, (java.awt.event.InputEvent.SHIFT_DOWN_MASK | menuShortcutKeyMask)));
			// END KGU#324 2017-11-09
			menuEditCopyDiagramEMF.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyToClipboardEMF(); doButtons(); } } );
		}

		// Setting up Menu "View" with all submenus and shortcuts and actions
		//menubar.add(menuView);

		// Setting up Menu "Diagram" with all submenus and shortcuts and actions
		menubar.add(menuDiagram);
		menuDiagram.setMnemonic(KeyEvent.VK_D);

		menuDiagram.add(menuDiagramAdd);
		menuDiagramAdd.setIcon(IconLoader.getIcon(18));

		menuDiagramAdd.add(menuDiagramAddBefore);
		menuDiagramAddBefore.setIcon(IconLoader.getIcon(19));

		// START KGU#169 2016-04-01: Enh. #142 (accelerator keys added in analogy to the insert after items)
		menuDiagramAddBefore.add(menuDiagramAddBeforeInst);
		menuDiagramAddBeforeInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",false); doButtons(); } } );
		menuDiagramAddBeforeInst.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, java.awt.event.InputEvent.SHIFT_DOWN_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeAlt);
		menuDiagramAddBeforeAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,false); doButtons(); } } );
		menuDiagramAddBeforeAlt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, java.awt.event.InputEvent.SHIFT_DOWN_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeCase);
		menuDiagramAddBeforeCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,false); doButtons(); } } );
		menuDiagramAddBeforeCase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, java.awt.event.InputEvent.SHIFT_DOWN_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeFor);
		menuDiagramAddBeforeFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,false); doButtons(); } } );
		menuDiagramAddBeforeFor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, java.awt.event.InputEvent.SHIFT_DOWN_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeWhile);
		menuDiagramAddBeforeWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,false); doButtons(); } } );
		menuDiagramAddBeforeWhile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, java.awt.event.InputEvent.SHIFT_DOWN_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeRepeat);
		menuDiagramAddBeforeRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,false); doButtons(); } } );
		menuDiagramAddBeforeRepeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, java.awt.event.InputEvent.SHIFT_DOWN_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeForever);
		menuDiagramAddBeforeForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",false); doButtons(); } } );
		// START KGU#725 2019-09-17: Issue #747
		menuDiagramAddBeforeForever.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		// END KGU#725 2019-09-17

		menuDiagramAddBefore.add(menuDiagramAddBeforeCall);
		menuDiagramAddBeforeCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",false); doButtons(); } } );
		menuDiagramAddBeforeCall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, java.awt.event.InputEvent.SHIFT_DOWN_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforeJump);
		menuDiagramAddBeforeJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",false); doButtons(); } } );
		menuDiagramAddBeforeJump.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, java.awt.event.InputEvent.SHIFT_DOWN_MASK));

		menuDiagramAddBefore.add(menuDiagramAddBeforePara);
		menuDiagramAddBeforePara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",false); doButtons(); } } );
		// START KGU#725 2019-09-17: Issue #747
		//menuDiagramAddBeforePara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F13, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		menuDiagramAddBeforePara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		// END KGU#725 2019-09-17
		// END KGU#169 2016-04-01

		// START KGU#686 2019-03-16: Enh. #56
		menuDiagramAddBefore.add(menuDiagramAddBeforeTry);
		menuDiagramAddBeforeTry.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Try(),"Add new try-catch ...","",true); doButtons(); } } );
		// START KGU#725 2019-09-17: Issue #747
		menuDiagramAddBeforeTry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		// END KGU#725 2019-09-17
		// END KGU#686 2019-03-16

		menuDiagramAdd.add(menuDiagramAddAfter);
		menuDiagramAddAfter.setIcon(IconLoader.getIcon(20));

		menuDiagramAddAfter.add(menuDiagramAddAfterInst);
		menuDiagramAddAfterInst.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Instruction(),"Add new instruction ...","",true); doButtons(); } } );
		menuDiagramAddAfterInst.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterAlt);
		menuDiagramAddAfterAlt.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Alternative(),"Add new IF statement ...",Element.preAlt,true); doButtons(); } } );
		menuDiagramAddAfterAlt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0));

		menuDiagramAddAfter.add(menuDiagramAddAfterCase);
		menuDiagramAddAfterCase.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Case(),"Add new CASE statement ...",Element.preCase,true); doButtons(); } } );
		menuDiagramAddAfterCase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));

		menuDiagramAddAfter.add(menuDiagramAddAfterFor);
		menuDiagramAddAfterFor.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new For(),"Add new FOR loop ...",Element.preFor,true); doButtons(); } } );
		menuDiagramAddAfterFor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

		menuDiagramAddAfter.add(menuDiagramAddAfterWhile);
		menuDiagramAddAfterWhile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new While(),"Add new WHILE loop ...",Element.preWhile,true); doButtons(); } } );
		menuDiagramAddAfterWhile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

		menuDiagramAddAfter.add(menuDiagramAddAfterRepeat);
		menuDiagramAddAfterRepeat.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Repeat(),"Add new REPEAT loop ...",Element.preRepeat,true); doButtons(); } } );
		menuDiagramAddAfterRepeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));

		menuDiagramAddAfter.add(menuDiagramAddAfterForever);
		menuDiagramAddAfterForever.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Forever(),"Add new ENDLESS loop ...","",true); doButtons(); } } );
		// START KGU#725 2019-09-17: Issue #747
		menuDiagramAddAfterForever.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		// END KGU#725 2019-09-17

		menuDiagramAddAfter.add(menuDiagramAddAfterCall);
		menuDiagramAddAfterCall.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Call(),"Add new call ...","",true); doButtons(); } } );
		menuDiagramAddAfterCall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));

		menuDiagramAddAfter.add(menuDiagramAddAfterJump);
		menuDiagramAddAfterJump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Jump(),"Add new jump ...","",true); doButtons(); } } );
		menuDiagramAddAfterJump.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

		menuDiagramAddAfter.add(menuDiagramAddAfterPara);
		menuDiagramAddAfterPara.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Parallel(),"Add new parallel ...","",true); doButtons(); } } );
		// START KGU#725 2019-09-17: Issue #747
		//menuDiagramAddAfterPara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F13, 0));
		menuDiagramAddAfterPara.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		// END KGU#725 2019-09-17

		// START KGU#686 2019-03-16: Enh. #56
		menuDiagramAddAfter.add(menuDiagramAddAfterTry);
		menuDiagramAddAfterTry.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.addNewElement(new Try(),"Add new try-catch ...","",true); doButtons(); } } );
		// START KGU#725 2019-09-17: Issue #747
		menuDiagramAddAfterTry.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		// END KGU#725 2019-09-17
		// END KGU#686 2019-03-16

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
		menuDiagramTransmute.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, menuShortcutKeyMask));
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
//		menuDebugDisable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, menuShortcutKeyMask));
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
//		menuDiagram.addSeparator();
//		// END KGU#143 2016-01-21
		// END KGU#310 2016-12-14


		menuDiagram.add(menuDiagramType);

		menuDiagramType.add(menuDiagramTypeProgram);
		menuDiagramTypeProgram.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setProgram(); doButtons(); } } );

		menuDiagramType.add(menuDiagramTypeFunction);
		menuDiagramTypeFunction.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setFunction(); doButtons(); } } );

		//START KGU#376 2017-05-16: Enh. #389
		menuDiagramType.add(menuDiagramTypeInclude);
		menuDiagramTypeInclude.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setInclude(); doButtons(); } } );
		// END KGU#376 2017-05-16

		menuDiagram.add(menuDiagramUnboxed);
		menuDiagramUnboxed.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setUnboxed(menuDiagramUnboxed.isSelected()); doButtons(); } } );

		menuDiagram.addSeparator();

		menuDiagram.add(menuDiagramComment);
		menuDiagramComment.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setComments(menuDiagramComment.isSelected()); doButtons(); } } );

		// START KGU#227 2016-07-31: Enh. #128
		menuDiagram.add(menuDiagramCommentsPlusText);
		menuDiagramCommentsPlusText.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setCommentsPlusText(menuDiagramCommentsPlusText.isSelected()); doButtons(); } } );
		// END KGU#227 2016-07-31

		menuDiagram.add(menuDiagramSwitchComments);
		menuDiagramSwitchComments.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleTextComments(); doButtons(); } } );
		// START KGU#169 2016-04-01: Enh. #142 (accelerator key added)
		menuDiagramSwitchComments.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, (java.awt.event.InputEvent.ALT_DOWN_MASK | menuShortcutKeyMask)));
		// START KGU#169 2016-04-01

		// START KGU#477 2017-12-06: Enh. #487
		menuDiagram.add(menuDiagramHideDeclarations);
		menuDiagramHideDeclarations.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setHideDeclarations(menuDiagramHideDeclarations.isSelected()); doButtons(); } } );
		// END KGU#477 2016-12-06

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
		menuDiagramIndex.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		// END KGU#305 2016-12-14

		// START KGU#705 2019-09-23: Enh. #738
		menuDiagram.add(menuDiagramPreview);
		menuDiagramPreview.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setCodePreview(menuDiagramPreview.isSelected()); } } );
		menuDiagramPreview.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		// END KGU#305 2016-12-14

		// Setting up Menu "Preferences" with all submenus and shortcuts and actions
		menubar.add(menuPreferences);
		menuPreferences.setMnemonic(KeyEvent.VK_P);
		
		menuPreferences.add(menuPreferencesLanguage);
		menuPreferencesLanguage.setIcon(IconLoader.getIcon(81));

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

		// START KGU#232 2016-08-03: Enh. #222
		menuPreferencesLanguage.addSeparator();
		menuPreferencesLanguage.add(menuPreferencesLanguageFromFile);
		menuPreferencesLanguageFromFile.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLangFile(); } } );
		menuPreferencesLanguageFromFile.setToolTipText("You may create translation files with the 'Translator' tool in the File menu.");
		// END KGU#232 2016-08-03
		
		// START KGU#300 2016-12-02: Enh. #300
		menuPreferences.add(menuPreferencesNotifyUpdate);
		menuPreferencesNotifyUpdate.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setRetrieveVersion(menuPreferencesNotifyUpdate.isSelected()); } } );
		menuPreferencesNotifyUpdate.setToolTipText("Allow Structorizer to retrieve version info from Structorizer homepage and to inform about new releases.");
		// END KGU#2016-12-02
		
		// START KGU#456 2017-11-05. Issue #452
		menuPreferences.add(menuPreferencesSimplified);
		menuPreferencesSimplified.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.setSimplifiedGUI(menuPreferencesSimplified.isSelected()); doButtons();} } );
		// END KGU#456 2017-11-05

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

		// START KGU#479 2017-12-14: Enh. #492
		menuPreferences.add(menuPreferencesElements);
		menuPreferencesElements.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.elementNamesNSD(); doButtons(); } } );;
		// END KGU#479 2017-12-14

		// START KGU#480 2018-01-18: Enh. #490 - Aliases for controller API
		menuPreferences.add(menuPreferencesCtrlAliases);
		menuPreferencesCtrlAliases.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.controllerAliasesNSD(controllerPlugins); } } );;
		// END KGU#480 2018-01-18

		// create Look & Feel Menu
		menuPreferences.add(menuPreferencesLookAndFeel);
		menuPreferencesLookAndFeel.setIcon(IconLoader.getIcon(78));
		LookAndFeel thisLaF = UIManager.getLookAndFeel();
		UIManager.LookAndFeelInfo plafs[] = UIManager.getInstalledLookAndFeels();
		for(int j = 0; j < plafs.length; ++j)
		{
			JCheckBoxMenuItem mi = new JCheckBoxMenuItem(plafs[j].getName());
			mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { NSDControl.setLookAndFeel((((JCheckBoxMenuItem) event.getSource()).getText())); doButtons(); } } );
			menuPreferencesLookAndFeel.add(mi);

			// START KGU#661 2019-02-20 - The name comparison will not always work, particularly not with "GTK+"
			//if(mi.getText().equals(UIManager.getLookAndFeel().getName()))
			if (mi.getText().equals(thisLaF.getName()) ||
					thisLaF.getClass().getName().equals(plafs[j].getClassName()))
			// END KGU#661 2019-02-29
			{
				mi.setSelected(true);
			}
		}
		
		// START KGU#503 2018-03-14: Enh. #519 (+ enh. #87)
		menuPreferences.add(menuPreferencesWheel);
		menuPreferencesWheel.setIcon(IconLoader.getIcon(9));
		// START KGU#123 2016-01-04: Enh. #87 
		menuPreferencesWheel.add(menuPreferencesWheelCollapse);
		menuPreferencesWheelCollapse.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleWheelMode(); doButtons(); } } );
		// END KGU#123 2016-01-04
		menuPreferencesWheel.add(menuPreferencesWheelZoom);
		menuPreferencesWheelZoom.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleCtrlWheelMode(); doButtons(); } });
		// END KGU#503 2018-03-14

		// START KGU#699 2019-03-27: Issue #717
		menuPreferencesWheel.add(menuPreferencesWheelUnit);
		menuPreferencesWheelUnit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.configureWheelUnit(); doButtons(); } });
		// END KGU#699 2019-03-27
		
		// START KGU#287 2017-01-11: Issue #81/#330
		menuPreferences.add(menuPreferencesScalePreset);
		menuPreferencesScalePreset.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { new GUIScaleChooser().setVisible(true); } } );
		// END KGU#287 2017-01-11

		menuPreferences.addSeparator();
		
		// START KGU#448 2018-01-04: Enh. #443 - checkbox menu items prepared for additional diagram controllers
		controllerPlugins = this.addPluginMenuItems(menuDebug, PluginType.CONTROLLER, IconLoader.getIcon(4), null);
		// END KGU#448 2018-01-04

		// START KGU#466 2019-08.02: Issue #733 - allows selective preferences export
		if (preferenceKeys.isEmpty()) {
			Vector<GENPlugin> parserPlugins = null;
			try (BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream("parsers.xml"))) {
				GENParser genp = new GENParser();
				parserPlugins = genp.parse(buff);
			} catch (IOException e) {}
			if (parserPlugins == null) {
				parserPlugins = new Vector<GENPlugin>();
			}
			preferenceKeys.put("menuDiagram", Mainform.getPreferenceKeys("diagram"));
			preferenceKeys.put("menuPreferencesLanguage", new String[] {"Lang"});
			preferenceKeys.put("menuPreferencesNotifyUpdate", Mainform.getPreferenceKeys("update"));
			preferenceKeys.put("menuPreferencesSimplified", new String[] {"userSkillLevel"});
			preferenceKeys.put("menuPreferencesFont", new String[] {"Font", "Size", "editorFontSize", "fixPadding", "unicodeCompOps"});
			preferenceKeys.put("menuPreferencesColors", Element.getPreferenceKeys("color"));
			preferenceKeys.put("menuPreferencesOptions", Element.getPreferenceKeys("structure"));
			preferenceKeys.put("menuPreferencesParser", CodeParser.getPreferenceKeys());
			preferenceKeys.put("menuPreferencesAnalyser", Root.getPreferenceKeys());
			preferenceKeys.put("menuPreferencesSaving", Mainform.getPreferenceKeys("saving"));
			String[] exportKeys = new String[generatorPlugins.size()+1];
			exportKeys[0] = "genExport*";
			for (int i = 0; i < generatorPlugins.size(); i++) {
				GENPlugin plugin = generatorPlugins.get(i);
				exportKeys[i+1] = plugin.getKey() + ".*";
			}
			preferenceKeys.put("menuPreferencesExport", exportKeys);
			String[] importKeys = new String[parserPlugins == null ? 1 : parserPlugins.size()+1];
			importKeys[0] = "imp*";
			for (int i = 0; i < parserPlugins.size(); i++) {
				GENPlugin plugin = parserPlugins.get(i);
				importKeys[i+1] = plugin.getKey() + ".*";
			}
			preferenceKeys.put("menuPreferencesImport", importKeys);
			preferenceKeys.put("menuPreferencesElements", ElementNames.getPreferenceKeys());
			String[] controllerKeys = new String[controllerPlugins.size()+1];
			controllerKeys[0] = "applyAliases";
			for (int i = 0; i < controllerPlugins.size(); i++) {
				controllerKeys[i+1] = controllerPlugins.get(i).className + ".*";
			}
			preferenceKeys.put("menuPreferencesCtrlAliases", controllerKeys);
			preferenceKeys.put("menuPreferencesLookAndFeel", new String[] {"laf"});
			preferenceKeys.put("menuPreferencesWheel", Mainform.getPreferenceKeys("wheel"));
			preferenceKeys.put("menuPreferencesScalePreset", new String[] {"scaleFactor"});
			preferenceKeys.put("prefsArranger", new String[] {"arranger*"});
			preferenceKeys.put("menuEditFindReplace", new String[] {"find*", "search*"});
		}
		// END KGU#466 2019-08-02

		menuPreferences.add(menuPreferencesSave);
		menuPreferencesSave.add(menuPreferencesSaveAll);
		menuPreferencesSaveAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { NSDControl.savePreferences(); } } );
		menuPreferencesSave.add(menuPreferencesSaveDump);
		menuPreferencesSaveDump.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) 
			{ 
				// START KGU#466 2019-08-03: Issue #733 - this was obviously misplaced (would be overridden by ini.load()
				//NSDControl.savePreferences();
				Set<String> prefPatterns = diagram.selectPreferencesToExport(msgSelectPreferences.getText(), preferenceKeys);
				if (prefPatterns == null) {
					// Cancelled
					return;
				}
				// END KGU#466 2019-08-03
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new INIFilter());
				if (fc.showSaveDialog(NSDControl.getFrame()) == JFileChooser.APPROVE_OPTION)
				{
					// save some data from the INI file
					Ini ini = Ini.getInstance();
					try
					{
						ini.load();
						String fn = fc.getSelectedFile().toString();
						if (!fn.toLowerCase().endsWith(".ini")) fn += ".ini";
						// START KGU#466 2019-08-03: Issue #733 - Update the ini properties from the cached settings
						//ini.save(fn);
						NSDControl.savePreferences();
						if (prefPatterns.isEmpty()) {
							ini.save(fn);
						}
						else {
							ini.save(fn, prefPatterns);
						}
						// END KGU#466 2019-08-03
					}
					catch (Exception ex)
					{
						logger.log(Level.WARNING, "Error saving the configuration file ...", ex);
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
				if (fc.showOpenDialog(NSDControl.getFrame())==JFileChooser.APPROVE_OPTION)
				{
					try
					{
						// load some data from the INI file
						Ini ini = Ini.getInstance();

						// START KGU#258 2016-09-26: Enh. #253
						HashMap<String, StringList> refactoringData = fetchRefactoringData();
						// END KGU#258 2016-09-26

						// START KGU#721 2019-08-06: Enh. #740 produce a backup to keep safe
						if (!ini.backup()) {
							if (JOptionPane.showConfirmDialog(NSDControl.getFrame(),
									msgIniBackupFailed.getText().replace("%", fc.getSelectedFile().getPath()), 
									menuPreferencesSaveLoad.getText(),
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION)
							{
								return;
							}
						}
						// END KGU#721b2019-08-06
						ini.load(fc.getSelectedFile().getPath());
						ini.save();
						NSDControl.loadFromINI();

						// START KGU#258 2016-09-26: Enh. #253
						if (diagram.offerRefactoring(refactoringData))
						{
							// (Refactoring involves redrawing)
							diagram.refactorNSD(refactoringData);
						}
						// END KGU#258 2016-09-26
					}
					catch (Exception ex)
					{
						logger.log(Level.WARNING, "Error loading the configuration file ...", ex);
					}
				}
				NSDControl.savePreferences(); // FIXME: This sensible here?
			}

		} );
		// START KGU#721 2019-08-06: Enh. #740
		menuPreferencesSave.add(menuPreferencesSaveRestore);
		menuPreferencesSaveRestore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				boolean done = false;
				String trouble = null;
				try {
					Ini ini = Ini.getInstance();
					HashMap<String, StringList> refactoringData = fetchRefactoringData();
					done = ini.restore();
					ini.save();
					NSDControl.loadFromINI();
					if (diagram.offerRefactoring(refactoringData))
					{
						// (Refactoring involves redrawing)
						diagram.refactorNSD(refactoringData);
					}
				} catch (Exception ex) {
					logger.log(Level.WARNING, "Error restoring the configuration backup ...", ex);
					trouble = ex.getMessage();
				}
				if (!done) {
					JOptionPane.showMessageDialog(NSDControl.getFrame(),
						msgIniRestoreFailed.getText().replace("%", trouble == null ? "!" : ": " + trouble),
						menuPreferencesSaveRestore.getText(),
						JOptionPane.ERROR_MESSAGE);
				}
			}});
		// END KGU#721 2019-08-06

		
		// START KGU#310 2016-12-14: New Debug menu
		menubar.add(menuDebug);
		menuDebug.setMnemonic(KeyEvent.VK_B);
		
		menuDebug.add(menuDebugTurtle);
		menuDebugTurtle.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.goTurtle(); } } );
		// START KGU#463 2017-11-20: Enh. #469 (accelerator key added)
		menuDebugTurtle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, (java.awt.event.InputEvent.SHIFT_DOWN_MASK | menuShortcutKeyMask)));
		// START KGU#463 2017-11-2

		menuDebug.add(menuDebugExecute);
		menuDebugExecute.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.goRun(); } } );
		// START KGU#463 2017-11-20: Enh. #469 (accelerator key added)
		menuDebugExecute.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, menuShortcutKeyMask));
		// START KGU#463 2017-11-2
		
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
		menuDebugDisable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, menuShortcutKeyMask));
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
		menuHelpUpdate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,menuShortcutKeyMask));

		// START KGU#287 2017-01-09: Issues #81/#330 GUI scaling
		GUIScaler.rescaleComponents(this);
//		if (this.getFrame() != null) {
//			SwingUtilities.updateComponentTreeUI(this.getFrame());
//		}
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

				JCheckBoxMenuItem menuToolbar = new JCheckBoxMenuItem(tb.getName(),IconLoader.getIcon(23));
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

			menuFileArrange.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuFileAttributes.setVisible(!Element.E_REDUCED_TOOLBARS);			
			menuFileTranslator.setVisible(!Element.E_REDUCED_TOOLBARS);			
			
			// undo & redo
			menuEditUndo.setEnabled(diagram.getRoot().canUndo());
			menuEditRedo.setEnabled(diagram.getRoot().canRedo());

			// graphics up/downgrade
			// START KGU#282 2016-10-16: Issue #272
			menuEditUpgradeTurtle.setEnabled(conditionAny);
			menuEditDowngradeTurtle.setEnabled(conditionAny);
			menuEditUpgradeTurtle.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuEditDowngradeTurtle.setVisible(!Element.E_REDUCED_TOOLBARS);			
			// END KGU#282 2016-10-16
			
			// START KGU#602 2018-10-26: Enh. #619
			menuEditBreakLines.setEnabled(conditionAny);
			menuEditBreakLines.setVisible(!Element.E_REDUCED_TOOLBARS);			
			// END KGU#602 2018-10-16

			// START KGU#667 2019-02-26 Enh.#689
			menuEditSummonSub.setEnabled(diagram.canEditSub());
			menuEditSummonSub.setVisible(!Element.E_REDUCED_TOOLBARS);			
			// END KGU#667 2019-02-26
			
			// style / type
			menuDiagramTypeFunction.setSelected(diagram.isSubroutine());
			menuDiagramTypeProgram.setSelected(diagram.isProgram());
			menuDiagramTypeInclude.setSelected(diagram.isInclude());
			menuDiagramUnboxed.setSelected(!diagram.getRoot().isBoxed);
			menuDiagramAnalyser.setSelected(Element.E_ANALYSER);
			// START KGU#305 2016-12-14: Enh. #305
			menuDiagramIndex.setSelected(diagram.showingArrangerIndex());
			// END KGU#305 2016-12-14
			// START KGU#705 2019-09-24: Enh. #738
			menuDiagramPreview.setSelected(diagram.showingCodePreview());
			// END KGU#705 2019-09-24

			// elements
			// START KGU#87 2015-11-22: Why enable the main entry if no action is enabled?
			menuDiagramAdd.setEnabled(condition);
			// END KGU#87 2015-11-22
			menuDiagramAddBeforeInst.setEnabled(condition);
			menuDiagramAddBeforeAlt.setEnabled(condition);
			menuDiagramAddBeforeCase.setEnabled(condition);
			menuDiagramAddBeforeFor.setEnabled(condition);
			menuDiagramAddBeforeWhile.setEnabled(condition);
			menuDiagramAddBeforeRepeat.setEnabled(condition);
			menuDiagramAddBeforeForever.setEnabled(condition);
			menuDiagramAddBeforeCall.setEnabled(condition);
			menuDiagramAddBeforeJump.setEnabled(condition);
			menuDiagramAddBeforePara.setEnabled(condition);
			// START KGU#686 2019-03-16: Enh. #56
			menuDiagramAddBeforeTry.setEnabled(condition);
			// END KGU#686 2019-03-16

			menuDiagramAddAfterInst.setEnabled(condition);
			menuDiagramAddAfterAlt.setEnabled(condition);
			menuDiagramAddAfterCase.setEnabled(condition);
			menuDiagramAddAfterFor.setEnabled(condition);
			menuDiagramAddAfterWhile.setEnabled(condition);
			menuDiagramAddAfterRepeat.setEnabled(condition);
			menuDiagramAddAfterForever.setEnabled(condition);
			menuDiagramAddAfterCall.setEnabled(condition);
			menuDiagramAddAfterJump.setEnabled(condition);
			menuDiagramAddAfterPara.setEnabled(condition);
			// START KGU#686 2019-03-16: Enh. #56
			menuDiagramAddAfterTry.setEnabled(condition);
			// END KGU#686 2019-03-16
			
			menuDiagramAddBeforeForever.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuDiagramAddBeforeJump.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuDiagramAddBeforePara.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuDiagramAddBeforeTry.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuDiagramAddAfterForever.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuDiagramAddAfterJump.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuDiagramAddAfterPara.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuDiagramAddAfterTry.setVisible(!Element.E_REDUCED_TOOLBARS);

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
			menuDiagramTransmute.setVisible(!Element.E_REDUCED_TOOLBARS);
			// END KGU#199 2016-07-07
			// START KGU#365 2017-03-26: Enh. #380 - We allow subroutine generation
			menuDiagramOutsource.setEnabled(diagram.canCut());
			menuDiagramOutsource.setVisible(!Element.E_REDUCED_TOOLBARS);
			// END KGU#365 2017-03-26
			
			
			// START KGU#123 2016-01-03: We allow multiple selection for collapsing
			// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
			menuDiagramCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());
			menuDiagramExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed(false) || condition && diagram.selectedIsMultiple());			
			menuDiagramCollapse.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuDiagramExpand.setVisible(!Element.E_REDUCED_TOOLBARS);

			menuDiagramHideDeclarations.setVisible(!Element.E_REDUCED_TOOLBARS);
			
			menuPreferencesColors.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuPreferencesElements.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuPreferencesCtrlAliases.setVisible(!Element.E_REDUCED_TOOLBARS);
			menuPreferencesWheel.setVisible(!Element.E_REDUCED_TOOLBARS);

			
			// END KGU#123 2016-01-03
			// START KGU#277 2016-10-13: Enh. #270
			menuDebugDisable.setEnabled(condition && !(selected instanceof Subqueue) || diagram.selectedIsMultiple());
			menuDebugDisable.setVisible(!Element.E_REDUCED_TOOLBARS);
			// END KGU#277 2016-01-13
			
			// START KGU#143 2016-01-21: Bugfix #114 - breakpoint control now also here
			// START KGU#177 2016-07-06: Enh. #158 - Collateral damage mended
			//menuDiagramBreakpoint.setEnabled(diagram.canCopy());
			// START KGU#686 2019-03-17: Enh. #56 It doesn't make sense to place breakpoints on endless loops or try elements
			//menuDebugBreakpoint.setEnabled(diagram.canCopyNoRoot());
			menuDebugBreakpoint.setEnabled(diagram.canSetBreakpoint());
			// END KGU#686 2019-03-17
			// END KGU#177 2016-07-06
			// END KGU#143 2016-01-21
			// START KGU#213 2016-08-02: Enh. #215 - breakpoint control enhanced
			// START KGU#686 2019-03-17: Enh. #56 It doesn't make sense to place breakpoints on endless loops or try elements
			//menuDebugBreakTrigger.setEnabled(diagram.canCopyNoRoot() && !diagram.selectedIsMultiple());
			menuDebugBreakTrigger.setEnabled(diagram.canSetBreakpoint() && !diagram.selectedIsMultiple());
			// END KGU#686 2019-03-17
			menuDebugBreakTrigger.setVisible(!Element.E_REDUCED_TOOLBARS);
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
			menuDiagramUnboxed.setSelected(diagram.isUnboxed());

			// variable highlighting
			menuDiagramMarker.setSelected(Element.E_VARHIGHLIGHT);

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

			// START KGU#477 2017-12-11: Enh. #487
			menuDiagramHideDeclarations.setSelected(Element.E_HIDE_DECL);
			// END KGU#477 2017-12-11

			// DIN 66261
			menuDiagramDIN.setSelected(Element.E_DIN);
			ImageIcon iconFor = IconLoader.getIcon(Element.E_DIN ? 74 : 53);
			menuDiagramAddBeforeFor.setIcon(iconFor);
			menuDiagramAddAfterFor.setIcon(iconFor);
			
			// START KGU#123 2016-01-04: Enh. #87
			// control the collapsing by mouse wheel?
			menuPreferencesWheelCollapse.setSelected(Element.E_WHEELCOLLAPSE);
			// END KGU#123 2016-01-04
			
			// START KGU#300 2016-12-02: Enh. #300
			menuPreferencesNotifyUpdate.setSelected(Ini.getInstance().getProperty("retrieveVersion", "false").equals("true"));
			// END KGU#300 2016-12-02
			
			// START KGU#456 2017-11-06. Enh. #452
			this.menuPreferencesSimplified.setSelected(Element.E_REDUCED_TOOLBARS);
			// END KGU#456 2017-11-06

			// Look and Feel submenu
			//System.out.println("Having: "+UIManager.getLookAndFeel().getName());
			String lafName = NSDControl.getLookAndFeel();
			for (i = 0; i < menuPreferencesLookAndFeel.getMenuComponentCount(); i++)
			{
				JCheckBoxMenuItem mi = (JCheckBoxMenuItem)menuPreferencesLookAndFeel.getMenuComponent(i);

				//System.out.println("Listing: "+mi.getText());
				if (mi.getText().equals(lafName))
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

			// START KGU#721 2019-08-06: Enh. #740
			menuPreferencesSaveRestore.setEnabled(Ini.getInstance().hasBackup());
			
			// Recent file
			// START KGU#287 2017-01-11: Issue #81/#330 Assimilate the dynamic menu items in font
			Font menuItemFont = UIManager.getFont("MenuItem.font");
			// END KGU#287 2017-01-11
			menuFileOpenRecent.removeAll();
			for(int j = 0; j < diagram.recentFiles.size(); ++j)
			{
				final String nextFile = (String) diagram.recentFiles.get(j);
				// START KGU#489 2018-02-07: 
				//JMenuItem mi = new JMenuItem(nextFile, IconLoader.getIcon(0));
				JMenuItem mi = new JMenuItem(nextFile, 
						((nextFile.endsWith(".arr") || nextFile.endsWith(".arrz")) ? IconLoader.getIcon(105) : IconLoader.getIcon(0)));
				// END KGU#489 2018-02-07
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

	// START KGU#456 2017-11-05: Enh. #452 Support for auto-documentation
	/**
	 * Tries to retrieve the current captions for the menus and items named by {@code menuItemKeys}
	 * and returns the localized captions as string array.<br/>
	 * If {@code defaultStrings} are given (should have same length as {@code menuItemKeys} then the
	 * respective default string will be used if retrieval fails for some of the given keys. If not
	 * given or too short then the respective caption from the default locale is retrieved instead. 
	 * @param menuItemKeys - an array of names
	 * @param defaultStrings - an array of default names for the requested captions or null
	 * @return the array of retrieved captions
	 */
	public static String[] getLocalizedMenuPath(String[] menuItemKeys, String[] defaultStrings)
	{
		String[] names = new String[menuItemKeys.length];
		String localeName = Locales.getInstance().getLoadedLocaleName();
		Locale locale = Locales.getInstance().getLocale(localeName);
		if (locale != null) {
			for (int i = 0; i < menuItemKeys.length; i++) {
				String text = locale.getValue("Structorizer", "Menu." + menuItemKeys[i] + ".text");
				if (text.isEmpty()) {
					if (defaultStrings != null && i < defaultStrings.length) {
						text = defaultStrings[i];
					}
					else {
						text = Locales.getInstance().getDefaultLocale().getValue("Structorizer", "Menu." + menuItemKeys[i] + ".text");
					}
				}
				names[i] = text;
			}
		}
		return names;
	}
	// END KGU#456

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
		// START KGU#479 2017-12-15: Enh. #492
		Locales.getInstance().setLocale((Component)ElementNames.getInstance(), localeName);
		// Better reset the use of personally configured names on changing the language
		ElementNames.useConfiguredNames = false;
		// END KGU#479 2017-12-15
		Locales.getInstance().setLocale(localeName);
		doButtons();
		diagram.analyse();
		// START KGU#705 2019-09-24: Enh. #738
		diagram.setCodePreviewTooltip();
		// END KGU#705 2019-09-24

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

	// START KGU#386 2017-04-26
	private Vector<GENPlugin> addPluginMenuItems(JMenu _menu, PluginType _type, ImageIcon _defaultIcon, Map<JMenuItem, String> _itemMap)
	{
		// read generators from file
		String fileName = "void.xml";
		String tooltip = "%";
		switch (_type) {
		case GENERATOR:
			fileName = "generators.xml";
			break;
		case PARSER:	// This isn't used anymore - we still leave it in the code for regularity
			fileName = "parsers.xml";
			break;
		case IMPORTER:
			fileName = "importers.xml";
			tooltip = msgImportTooltip.getText();
			break;
		case CONTROLLER:
			fileName = "controllers.xml";
		}
		// and add them to the menu
		BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream(fileName));
		GENParser genp = new GENParser();
		Vector<GENPlugin> plugins = genp.parse(buff);
		try { buff.close();	} catch (IOException e) {}
		for (int i = 0; i < plugins.size(); i++)
		{
			GENPlugin plugin = plugins.get(i);
			ImageIcon icon = _defaultIcon;	// The default icon
			if (plugin.icon != null && !plugin.icon.isEmpty()) {
				try {
					URL iconFile = this.getClass().getResource(plugin.icon);
					if (iconFile != null) {
						// START KGU#287 2018-02-07: Enh. #81 
						//icon = new ImageIcon(this.getClass().getResource(plugin.icon));
						icon = IconLoader.getIconImage(this.getClass().getResource(plugin.icon));
						// END KGU#287 2018-02-07
					}
				}
				catch (Exception ex) {}
			}
			JMenuItem pluginItem;
			if (_type == PluginType.CONTROLLER) {
				if (plugin.className.equals("lu.fisch.turtle.TurtleBox")) {
					continue;
				}
				pluginItem = new JCheckBoxMenuItem(plugin.title, icon);
			}
			else {
				pluginItem = new JMenuItem(plugin.title, icon);
			}
			_menu.add(pluginItem);
			if (plugin.info != null) {
				pluginItem.setToolTipText(tooltip.replace("%", plugin.info));
				// START KGU#725 2019-09-13: Enh. #746 - for later re-translation if necessary
				if (_itemMap != null) {
					_itemMap.put(pluginItem, plugin.info);
				}
				// END KGU#725 2019-09-13
			}
			final String className = plugin.className;
			// START KGU#354/KGU#395 2017-05-11: Enh. #354 - prepares plugin-specific option
			final Vector<HashMap<String, String>> options = plugin.options;
			// END KGU#354/KGU#395 2017-05-11
			
			ActionListener listener = null;
			switch (_type) {
			case GENERATOR:
				// START KGU#354/KGU#395 2017-05-11: Enh. #354 - prepares plugin-specific option
				//listener = new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.export(className); doButtons(); } };
				listener = new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.export(className, options); doButtons(); } };
				// END KGU#354/KGU#395 2017-05-11
				break;
			case PARSER:	// This isn't used anymore - we still leave in the code for regularity
				listener = new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importCode(/*className*/); doButtons(); } };
				break;
			case IMPORTER:
				// START KGU#354/KGU#395 2017-05-11: Enh. #354 - prepares plugin-specific option
				//listener = new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importNSD(className); doButtons(); } };
				listener = new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importNSD(className, options); doButtons(); } };
				// END KGU#354/KGU#395 2017-05-11
				break;
				// START KGU#448 2018-01-05: Enh. #443
			case CONTROLLER:	// This isn't used anymore - we still leave in the code for regularity
				listener = new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.enableController(className, ((JCheckBoxMenuItem)pluginItem).isSelected()); } };
				break;
				// END KGU#448 2018-01-05
			}
			if (listener != null) {
				pluginItem.addActionListener(listener);
			}
		}
		
		return plugins;
	}
	// END KGU#386 2017-04-26

	// START KGU#258 2016-09-26: Enh. #253 (KGU#721 2019-08-06: Enh. #740 - extracted as method)
	/**
	 * Collects the current parser preferences in lexically split form for comparison
	 * with modified parser preferences
	 * @return a {@link HashMap} associating parser tags with lexically split keywords
	 */
	private HashMap<String, StringList> fetchRefactoringData() {
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
		return refactoringData;
	}
	// END KGU#258 2016-09-26 (KGU#721 2019-08-06)

	// START KGU#725 2019-09-13: Enh. #746 - The importer tooltips hadn't been retranslated
	@Override
	public void LangChanged(LangEvent evt) {
		if (evt.getSource() == msgImportTooltip) {
			String tooltip = msgImportTooltip.getText();
			for (Map.Entry<JMenuItem, String> entry: this.importpluginItems.entrySet()) {
				entry.getKey().setToolTipText(tooltip.replace("%", entry.getValue()));
			}
		}
	}
	// END KGU#725 2019-09-13

}

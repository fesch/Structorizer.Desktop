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
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///


import lu.fisch.structorizer.locales.LangDialog;
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
	protected final JMenuItem menuFileImportPascal = new JMenuItem("Pascal Code ...",IconLoader.ico004);

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
	// START KGU#123 2016-01-03: New menu items for collapsing/expanding (addresses #65)
	protected final JMenuItem menuDiagramCollapse = new JMenuItem("Collapse", IconLoader.ico106);
	protected final JMenuItem menuDiagramExpand = new JMenuItem("Expand", IconLoader.ico107);
	// END KGU#123 2016-01-03
	// START KGU#143 2016-01-21: Bugfix #114 - Compensate editing restriction by accelerator4
	protected final JMenuItem menuDiagramBreakpoint = new JMenuItem("Toggle Breakpoint", IconLoader.ico103);
	// END KGU#143 2016-01-21
	// START KGU#213 2016-08-02: Enh. #215
	protected final JMenuItem menuDiagramBreakTrigger = new JMenuItem("Specify break trigger...", IconLoader.ico112);
	// END KGU#143 2016-08-02

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

	// Menu "Help"
	protected final JMenu menuPreferences = new JMenu("Preferences");
	// Submenu of "Help"
	protected final JMenuItem menuPreferencesFont = new JMenuItem("Font ...",IconLoader.ico023);
	protected final JMenuItem menuPreferencesColors = new JMenuItem("Colors ...",IconLoader.ico031);
	protected final JMenuItem menuPreferencesOptions = new JMenuItem("Structures ...",IconLoader.ico040);
	protected final JMenuItem menuPreferencesParser = new JMenuItem("Parser ...",IconLoader.ico004);
	protected final JMenuItem menuPreferencesAnalyser = new JMenuItem("Analyser ...",IconLoader.ico083);
	protected final JMenuItem menuPreferencesExport = new JMenuItem("Export ...",IconLoader.ico032);
	protected final JMenu menuPreferencesLanguage = new JMenu("Language");
	protected final JMenuItem menuPreferencesLanguageEnglish = new JCheckBoxMenuItem("English",IconLoader.ico046);
	protected final JMenuItem menuPreferencesLanguageGerman = new JCheckBoxMenuItem("German",IconLoader.ico080);
	protected final JMenuItem menuPreferencesLanguageFrench = new JCheckBoxMenuItem("French",IconLoader.ico045);
	protected final JMenuItem menuPreferencesLanguageDutch = new JCheckBoxMenuItem("Dutch",IconLoader.ico051);
	protected final JMenuItem menuPreferencesLanguageLuxemburgish = new JCheckBoxMenuItem("Luxemburgish",IconLoader.ico075);
	protected final JMenuItem menuPreferencesLanguageSpanish = new JCheckBoxMenuItem("Spanish",IconLoader.ico084);
	protected final JMenuItem menuPreferencesLanguagePortugalBrazil = new JCheckBoxMenuItem("Brazilian portuguese",IconLoader.ico085);
	protected final JMenuItem menuPreferencesLanguageItalian = new JCheckBoxMenuItem("Italian",IconLoader.ico086);
	protected final JMenuItem menuPreferencesLanguageSimplifiedChinese = new JCheckBoxMenuItem("Chinese (simplified)",IconLoader.ico087);
	protected final JMenuItem menuPreferencesLanguageTraditionalChinese = new JCheckBoxMenuItem("Chinese (traditional)",IconLoader.ico094);
	protected final JMenuItem menuPreferencesLanguageCzech = new JCheckBoxMenuItem("Czech",IconLoader.ico088);
	protected final JMenuItem menuPreferencesLanguageRussian = new JCheckBoxMenuItem("Russian",IconLoader.ico092);
	protected final JMenuItem menuPreferencesLanguagePolish = new JCheckBoxMenuItem("Polish",IconLoader.ico093);
	// START KGU#232 2016-08-03: Enh. #222
	protected final JMenuItem menuPreferencesLanguageFromFile = new JCheckBoxMenuItem("From file ...",IconLoader.ico114);
	// END KGU#232 2016-08-03
	protected final JMenu menuPreferencesLookAndFeel = new JMenu("Look & Feel");
	protected final JMenu menuPreferencesSave = new JMenu("All preferences ...");
	protected final JMenuItem menuPreferencesSaveAll = new JMenuItem("Save");
	protected final JMenuItem menuPreferencesSaveLoad = new JMenuItem("Load from file ...");
	protected final JMenuItem menuPreferencesSaveDump = new JMenuItem("Save to file ...");

	// Menu "Help"
	protected final JMenu menuHelp = new JMenu("Help");
	// Submenu of "Help"
	// START KGU#208 2016-07-22: Enh. #199
	protected final JMenuItem menuHelpOnline = new JMenuItem("User Guide",IconLoader.ico110);
	// END KGU#208 2016-07-22
	protected final JMenuItem menuHelpAbout = new JMenuItem("About ...",IconLoader.ico017);
	protected final JMenuItem menuHelpUpdate = new JMenuItem("Update ...",IconLoader.ico052);

	// Error messages for Analyser
	// START KGU#220 2016-07-27: Enh. as proposed in issue #207
	public static final LangTextHolder warning_1 = new LangTextHolder("WARNING: TEXTS AND COMMENTS ARE EXCHANGED IN DISPLAY! ---> \"Diagram > Switch text/comments\".");
	// END KGU#220 2016-07-27
	public static final LangTextHolder error01_1 = new LangTextHolder("WARNING: No loop variable detected ...");
	public static final LangTextHolder error01_2 = new LangTextHolder("WARNING: More than one loop variable detected ...");
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
	public static final LangTextHolder error15 = new LangTextHolder("The CALL hasn't got form «[ <var> " + "\u2190" +" ] <routine_name>(<arg_list>)»!");
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

	public void create()
	{
		JMenuBar menubar = this;

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

		menuFile.add(menuFileOpen);
		menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFileOpen.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.openNSD(); doButtons(); } } );

		menuFile.add(menuFileOpenRecent);
		menuFileOpenRecent.setIcon(IconLoader.ico002);

		menuFile.addSeparator();

		menuFile.add(menuFileImport);

		menuFileImport.add(menuFileImportPascal);
		menuFileImportPascal.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.importPAS(); doButtons(); } } );

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
		Vector<GENPlugin> plugins = genp.parse(buff);
		for(int i=0;i<plugins.size();i++)
		{
			GENPlugin plugin = (GENPlugin) plugins.get(i);
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

		menuEdit.add(menuEditCopyDiagramPNG);
		menuEditCopyDiagramPNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuEditCopyDiagramPNG.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.copyToClipboardPNG(); doButtons(); } } );

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
		
		menuDiagram.addSeparator();

		// START KGU#123 2016-01-03: New menu items (addressing #65)
		menuDiagram.add(menuDiagramCollapse);
		menuDiagramCollapse.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0));
		menuDiagramCollapse.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.collapseNSD(); doButtons(); } } );

		menuDiagram.add(menuDiagramExpand);
		menuDiagramExpand.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0));
		menuDiagramExpand.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.expandNSD(); doButtons(); } } );

		menuDiagram.addSeparator();
		// END KGU#123 2016-01-03
		
		// START KGU#143 2016-01-21: Bugfix #114 - Compensate editing restriction by accelerator
		menuDiagram.add(menuDiagramBreakpoint);
    	menuDiagramBreakpoint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        menuDiagramBreakpoint.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleBreakpoint(); doButtons(); } }); 

		// START KGU#213 2016-08-02: Enh. #215 - new breakpoint feature
		menuDiagram.add(menuDiagramBreakTrigger);
    	menuDiagramBreakTrigger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
        menuDiagramBreakTrigger.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.editBreakTrigger(); doButtons(); } }); 
		// END KGU#213 2016-08-02

        menuDiagram.addSeparator();
		// END KGU#143 2016-01-21

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
		
		// START KGU#123 2016-01-04: Enh. #87
		menuDiagram.add(menuDiagramWheel);
		menuDiagramWheel.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.toggleWheelMode(); doButtons(); } } );
		// END KGU#123 2016-01-04

		// Setting up Menu "Preferences" with all submenus and shortcuts and actions
		menubar.add(menuPreferences);
		menuPreferences.setMnemonic(KeyEvent.VK_P);

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

		menuPreferences.add(menuPreferencesExport);
		menuPreferencesExport.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.exportOptions(); doButtons(); } } );

		menuPreferences.add(menuPreferencesLanguage);
		menuPreferencesLanguage.setIcon(IconLoader.ico081);

		menuPreferencesLanguage.add(menuPreferencesLanguageEnglish);
		menuPreferencesLanguageEnglish.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("en"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageGerman);
		menuPreferencesLanguageGerman.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("de"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageFrench);
		(menuPreferencesLanguageFrench).addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("fr"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageDutch);
		menuPreferencesLanguageDutch.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("nl"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageLuxemburgish);
		menuPreferencesLanguageLuxemburgish.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("lu"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageSpanish);
		menuPreferencesLanguageSpanish.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("es"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguagePortugalBrazil);
		menuPreferencesLanguagePortugalBrazil.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("pt_br"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageItalian);
		menuPreferencesLanguageItalian.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("it"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageSimplifiedChinese);
		menuPreferencesLanguageSimplifiedChinese.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("chs"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageTraditionalChinese);
		menuPreferencesLanguageTraditionalChinese.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("cht"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageCzech);
		menuPreferencesLanguageCzech.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("cz"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageRussian);
		menuPreferencesLanguageRussian.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("ru"); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguagePolish);
		menuPreferencesLanguagePolish.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { chooseLang("pl"); } } );

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

		menuPreferences.addSeparator();

		menuPreferences.add(menuPreferencesSave);
                menuPreferencesSave.add(menuPreferencesSaveAll);
		menuPreferencesSaveAll.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { NSDControl.savePreferences(); } } );
                menuPreferencesSave.add(menuPreferencesSaveDump);
		menuPreferencesSaveDump.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) 
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
                } } );
                menuPreferencesSave.add(menuPreferencesSaveLoad);
                menuPreferencesSaveLoad.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) 
                { 
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new INIFilter());
                    if(fc.showOpenDialog(NSDControl.getFrame())==JFileChooser.APPROVE_OPTION)
                    {
                        try
                        {
                            // load some data from the INI file
                            Ini ini = Ini.getInstance();
                            ini.load(fc.getSelectedFile().toString());
                            ini.save();
                            NSDControl.loadFromINI();
                        }
                        catch (Exception ex)
                        {
                            System.err.println("Error loading the configuration file ...");
                            ex.printStackTrace();
                        }
                    }
                    NSDControl.savePreferences(); 
                } } );


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
			menuFileSave.setEnabled(diagram.getRoot().hasChanged());
			// END KGU#137 2016-01-11
			// START KGU#170 2016-04-01: Enh. #144 - update the favourite export item text
			String itemText = lbFileExportCodeFavorite.getText().replace("%", diagram.getPreferredGeneratorName());
			this.menuFileExportCodeFavorite.setText(itemText);
			// END KGU#170 2016-04-01
			
			// undo & redo
			menuEditUndo.setEnabled(diagram.getRoot().canUndo());
			menuEditRedo.setEnabled(diagram.getRoot().canRedo());

			// style
			menuDiagramTypeFunction.setSelected(!diagram.isProgram());
			menuDiagramTypeProgram.setSelected(diagram.isProgram());
			menuDiagramNice.setSelected(diagram.getRoot().isNice);
			menuDiagramComment.setSelected(Element.E_SHOWCOMMENTS);
			menuDiagramAnalyser.setSelected(Element.E_ANALYSER);

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
			menuDiagramEdit.setEnabled(conditionAny && !diagram.selectedIsMultiple());
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
			
			
			// START KGU#123 2016-01-03: We allow multiple selection for collapsing
			// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
			menuDiagramCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());
			menuDiagramExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());			
			// END KGU#123 2016-01-03

			// START KGU#143 2016-01-21: Bugfix #114 - breakpoint control now also here
			// START KGU#177 2016-07-06: Enh. #158 - Collateral damage mended
			//menuDiagramBreakpoint.setEnabled(diagram.canCopy());
			menuDiagramBreakpoint.setEnabled(diagram.canCopyNoRoot());
			// END KGU#177 2016-07-06
			// END KGU#143 2016-01-21
			// START KGU#213 2016-08-02: Enh. #215 - breakpoint control enhanced
			menuDiagramBreakTrigger.setEnabled(diagram.canCopyNoRoot() && !diagram.selectedIsMultiple());
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

			// show comments?
			menuDiagramComment.setSelected(diagram.drawComments());

			// variable highlighting
			menuDiagramMarker.setSelected(diagram.getRoot().hightlightVars);

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
			menuPreferencesLanguageEnglish.setSelected(Locales.getInstance().getLoadedLocaleName().equals("en"));
			menuPreferencesLanguageGerman.setSelected(Locales.getInstance().getLoadedLocaleName().equals("de"));
			menuPreferencesLanguageFrench.setSelected(Locales.getInstance().getLoadedLocaleName().equals("fr"));
			menuPreferencesLanguageDutch.setSelected(Locales.getInstance().getLoadedLocaleName().equals("nl"));
			menuPreferencesLanguageLuxemburgish.setSelected(Locales.getInstance().getLoadedLocaleName().equals("lu"));
			menuPreferencesLanguageSpanish.setSelected(Locales.getInstance().getLoadedLocaleName().equals("es"));
			menuPreferencesLanguagePortugalBrazil.setSelected(Locales.getInstance().getLoadedLocaleName().equals("pt_br"));
			menuPreferencesLanguageItalian.setSelected(Locales.getInstance().getLoadedLocaleName().equals("it"));
			menuPreferencesLanguageSimplifiedChinese.setSelected(Locales.getInstance().getLoadedLocaleName().equals("chs"));
			menuPreferencesLanguageTraditionalChinese.setSelected(Locales.getInstance().getLoadedLocaleName().equals("cht"));
			menuPreferencesLanguageCzech.setSelected(Locales.getInstance().getLoadedLocaleName().equals("cz"));
			menuPreferencesLanguageRussian.setSelected(Locales.getInstance().getLoadedLocaleName().equals("ru"));
			menuPreferencesLanguagePolish.setSelected(Locales.getInstance().getLoadedLocaleName().equals("pl"));
			menuPreferencesLanguageFromFile.setSelected(Locales.getInstance().getLoadedLocaleName().equals("external"));

			// Recent file
			menuFileOpenRecent.removeAll();
			for(int j = 0; j < diagram.recentFiles.size(); ++j)
			{
				JMenuItem mi = new JMenuItem((String) diagram.recentFiles.get(j),IconLoader.ico074);
				final String nextFile = (String) diagram.recentFiles.get(j);
				mi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { diagram.openNSD(nextFile); doButtons(); } } );
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
            // paste it's content to the "preview" locale
            Locales.getInstance().setExternal(sl,filename);

            //Locales.getInstance().setLocale(dlgOpen.getSelectedFile().getAbsoluteFile().toString());
            //setLang(dlgOpen.getSelectedFile().getAbsoluteFile().toString());
        }
        // START KGU#235 2016-08-09: Bugfix #225
        doButtons();
        diagram.analyse();
        // END KGU#235 2016-08-09
    }
    // END KGU#232 2016-08-03
}

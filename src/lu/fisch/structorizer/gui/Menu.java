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

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.gui.LangTextHolder;
import lu.fisch.structorizer.helpers.*;
import lu.fisch.structorizer.io.INIFilter;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.parsers.*;

@SuppressWarnings("serial")
public class Menu extends JMenuBar implements NSDController
{
	private Diagram diagram = null;
	private NSDController NSDControl = null;

	// Menu "File"
	protected JMenu menuFile = new JMenu("File");
	// Submenus of "File"
	protected JMenuItem menuFileNew = new JMenuItem("New", IconLoader.ico001);
	protected JMenuItem menuFileSave = new JMenuItem("Save", IconLoader.ico003);
	protected JMenuItem menuFileSaveAs = new JMenuItem("Save As ...", IconLoader.ico003);
	protected JMenuItem menuFileOpen = new JMenuItem("Open ...", IconLoader.ico002);
	protected JMenuItem menuFileOpenRecent = new JMenu("Open Recent File");
	protected JMenu menuFileExport = new JMenu("Export");
	// Submenu of "File -> Export"
	protected JMenu menuFileExportPicture = new JMenu("Picture");
	protected JMenuItem menuFileExportPicturePNG = new JMenuItem("PNG ...",IconLoader.ico032);
	protected JMenuItem menuFileExportPicturePNGmulti = new JMenuItem("PNG (multiple) ...",IconLoader.ico032);
	protected JMenuItem menuFileExportPictureEMF = new JMenuItem("EMF ...",IconLoader.ico032);
	protected JMenuItem menuFileExportPictureSWF = new JMenuItem("SWF ...",IconLoader.ico032);
	protected JMenuItem menuFileExportPicturePDF = new JMenuItem("PDF ...",IconLoader.ico032);
	protected JMenuItem menuFileExportPictureSVG = new JMenuItem("SVG ...",IconLoader.ico032);
	protected JMenu menuFileExportCode = new JMenu("Code");
	// START KGU#171 2016-04-01: Enh. #144 - new menu item for Favourite Code Export
	protected static LangTextHolder lbFileExportCodeFavorite = new LangTextHolder("Export as % Code");	// Label template for translation
	protected JMenuItem menuFileExportCodeFavorite = new JMenuItem("Export Fav. Code", IconLoader.ico004);
	// END KGU#171 2016-04-01
	protected JMenu menuFileImport = new JMenu("Import");
	// Submenu of "File -> Import"
	protected JMenuItem menuFileImportPascal = new JMenuItem("Pascal Code ...",IconLoader.ico004);

	// START KGU#2 2015-11-19: New menu item to have the Arranger present the diagram
	protected JMenuItem menuFileArrange = new JMenuItem("Arrange", IconLoader.ico105);
	// END KGU#2 2015-11-19
	protected JMenuItem menuFilePrint = new JMenuItem("Print ...",IconLoader.ico041);
	protected JMenuItem menuFileQuit = new JMenuItem("Quit");

	// Menu "Edit"
	protected JMenu menuEdit = new JMenu("Edit");
	// Submenu of "Edit"
	protected JMenuItem menuEditUndo = new JMenuItem("Undo",IconLoader.ico039);
	protected JMenuItem menuEditRedo = new JMenuItem("Redo",IconLoader.ico038);
	protected JMenuItem menuEditCut = new JMenuItem("Cut",IconLoader.ico044);
	protected JMenuItem menuEditCopy = new JMenuItem("Copy",IconLoader.ico042);
	protected JMenuItem menuEditPaste = new JMenuItem("Paste",IconLoader.ico043);
	protected JMenuItem menuEditCopyDiagramPNG = new JMenuItem("Copy bitmap diagram to clipboard",IconLoader.ico032);
	protected JMenuItem menuEditCopyDiagramEMF = new JMenuItem("Copy vector diagram to clipboard",IconLoader.ico032);

	protected JMenu menuView = new JMenu("View");

        // Menu "Diagram"
	protected JMenu menuDiagram = new JMenu("Diagram");
	// Submenus of "Diagram"
	protected JMenu menuDiagramAdd = new JMenu("Add");
	// Submenu "Diagram -> Add -> Before"
	protected JMenu menuDiagramAddBefore = new JMenu("Before");
	// Submenus for adding Elements "Before"
	protected JMenuItem menuDiagramAddBeforeInst = new JMenuItem("Instruction",IconLoader.ico007);
	protected JMenuItem menuDiagramAddBeforeAlt = new JMenuItem("IF statement",IconLoader.ico008);
	protected JMenuItem menuDiagramAddBeforeCase = new JMenuItem("CASE statement",IconLoader.ico047);
	protected JMenuItem menuDiagramAddBeforeFor = new JMenuItem("FOR loop",IconLoader.ico009);
	protected JMenuItem menuDiagramAddBeforeWhile = new JMenuItem("WHILE loop",IconLoader.ico010);
	protected JMenuItem menuDiagramAddBeforeRepeat = new JMenuItem("REPEAT loop",IconLoader.ico011);
	protected JMenuItem menuDiagramAddBeforeForever = new JMenuItem("ENDLESS loop",IconLoader.ico009);
	protected JMenuItem menuDiagramAddBeforeCall = new JMenuItem("Call",IconLoader.ico049);
	protected JMenuItem menuDiagramAddBeforeJump = new JMenuItem("Jump",IconLoader.ico056);
	protected JMenuItem menuDiagramAddBeforePara = new JMenuItem("Parallel",IconLoader.ico090);

	// Submenu "Diagram -> Add -> After"
	protected JMenu menuDiagramAddAfter = new JMenu("After");
	// Submenus for adding Elements "After"
	protected JMenuItem menuDiagramAddAfterInst = new JMenuItem("Instruction",IconLoader.ico012);
	protected JMenuItem menuDiagramAddAfterAlt = new JMenuItem("IF statement",IconLoader.ico013);
	protected JMenuItem menuDiagramAddAfterCase = new JMenuItem("CASE statement",IconLoader.ico048);
	protected JMenuItem menuDiagramAddAfterFor = new JMenuItem("FOR loop",IconLoader.ico014);
	protected JMenuItem menuDiagramAddAfterWhile = new JMenuItem("WHILE loop",IconLoader.ico015);
	protected JMenuItem menuDiagramAddAfterRepeat = new JMenuItem("REPEAT loop",IconLoader.ico016);
	protected JMenuItem menuDiagramAddAfterForever = new JMenuItem("ENDLESS loop",IconLoader.ico014);
	protected JMenuItem menuDiagramAddAfterCall = new JMenuItem("Call",IconLoader.ico050);
	protected JMenuItem menuDiagramAddAfterJump = new JMenuItem("Jump",IconLoader.ico055);
	protected JMenuItem menuDiagramAddAfterPara = new JMenuItem("Parallel",IconLoader.ico089);

	protected JMenuItem menuDiagramEdit = new JMenuItem("Edit",IconLoader.ico006);
	protected JMenuItem menuDiagramDelete = new JMenuItem("Delete",IconLoader.ico005);
	protected JMenuItem menuDiagramMoveUp = new JMenuItem("Move up",IconLoader.ico019);
	protected JMenuItem menuDiagramMoveDown = new JMenuItem("Move down",IconLoader.ico020);
	// START KGU#123 2016-01-03: New menu items for collapsing/expanding (addresses #65)
	protected JMenuItem menuDiagramCollapse = new JMenuItem("Collapse", IconLoader.ico106);
	protected JMenuItem menuDiagramExpand = new JMenuItem("Expand", IconLoader.ico107);
	// END KGU#123 2016-01-03
	// START KGU#143 2016-01-21: Bugfix #114 - Compensate editing restriction by accelerator4
	protected JMenuItem menuDiagramBreakpoint = new JMenuItem("Toggle Breakpoint", IconLoader.ico103);
	// END KGU#143 2016-01-21

	protected JMenu menuDiagramType = new JMenu("Type");
	protected JCheckBoxMenuItem menuDiagramTypeProgram = new JCheckBoxMenuItem("Main",IconLoader.ico022);
	protected JCheckBoxMenuItem menuDiagramTypeFunction = new JCheckBoxMenuItem("Sub",IconLoader.ico021);
	protected JCheckBoxMenuItem menuDiagramNice = new JCheckBoxMenuItem("Boxed diagram?",IconLoader.ico040);
	protected JCheckBoxMenuItem menuDiagramComment = new JCheckBoxMenuItem("Show comments?",IconLoader.ico077);
	protected JCheckBoxMenuItem menuDiagramMarker = new JCheckBoxMenuItem("Highlight variables?",IconLoader.ico079);
	protected JCheckBoxMenuItem menuDiagramDIN = new JCheckBoxMenuItem("DIN 66261?",IconLoader.ico082);
	protected JCheckBoxMenuItem menuDiagramAnalyser = new JCheckBoxMenuItem("Analyse structogram?",IconLoader.ico083);
	protected JCheckBoxMenuItem menuDiagramSwitchComments = new JCheckBoxMenuItem("Switch text/comments?",IconLoader.ico102);
	// START KGU#123 2016-01-04: Enh. #87
	protected JCheckBoxMenuItem menuDiagramWheel = new JCheckBoxMenuItem("Mouse wheel for collapsing?",IconLoader.ico108);
	// END KGU#123 2016-01-04

	// Menu "Help"
	protected JMenu menuPreferences = new JMenu("Preferences");
	// Submenu of "Help"
	protected JMenuItem menuPreferencesFont = new JMenuItem("Font ...",IconLoader.ico023);
	protected JMenuItem menuPreferencesColors = new JMenuItem("Colors ...",IconLoader.ico031);
	protected JMenuItem menuPreferencesOptions = new JMenuItem("Structures ...",IconLoader.ico040);
	protected JMenuItem menuPreferencesParser = new JMenuItem("Parser ...",IconLoader.ico004);
	protected JMenuItem menuPreferencesAnalyser = new JMenuItem("Analyser ...",IconLoader.ico083);
	protected JMenuItem menuPreferencesExport = new JMenuItem("Export ...",IconLoader.ico032);
	protected JMenu menuPreferencesLanguage = new JMenu("Language");
	protected JMenuItem menuPreferencesLanguageEnglish = new JCheckBoxMenuItem("English",IconLoader.ico046);
	protected JMenuItem menuPreferencesLanguageGerman = new JCheckBoxMenuItem("German",IconLoader.ico080);
	protected JMenuItem menuPreferencesLanguageFrench = new JCheckBoxMenuItem("French",IconLoader.ico045);
	protected JMenuItem menuPreferencesLanguageDutch = new JCheckBoxMenuItem("Dutch",IconLoader.ico051);
	protected JMenuItem menuPreferencesLanguageLuxemburgish = new JCheckBoxMenuItem("Luxemburgish",IconLoader.ico075);
	protected JMenuItem menuPreferencesLanguageSpanish = new JCheckBoxMenuItem("Spanish",IconLoader.ico084);
	protected JMenuItem menuPreferencesLanguagePortugalBrazil = new JCheckBoxMenuItem("Brazilian portuguese",IconLoader.ico085);
	protected JMenuItem menuPreferencesLanguageItalian = new JCheckBoxMenuItem("Italian",IconLoader.ico086);
	protected JMenuItem menuPreferencesLanguageSimplifiedChinese = new JCheckBoxMenuItem("Chinese (simplified)",IconLoader.ico087);
	protected JMenuItem menuPreferencesLanguageTraditionalChinese = new JCheckBoxMenuItem("Chinese (traditional)",IconLoader.ico094);
	protected JMenuItem menuPreferencesLanguageCzech = new JCheckBoxMenuItem("Czech",IconLoader.ico088);
	protected JMenuItem menuPreferencesLanguageRussian = new JCheckBoxMenuItem("Russian",IconLoader.ico092);
	protected JMenuItem menuPreferencesLanguagePolish = new JCheckBoxMenuItem("Polish",IconLoader.ico093);
	protected JMenu menuPreferencesLookAndFeel = new JMenu("Look & Feel");
	protected JMenu menuPreferencesSave = new JMenu("All preferences ...");
	protected JMenuItem menuPreferencesSaveAll = new JMenuItem("Save");
	protected JMenuItem menuPreferencesSaveLoad = new JMenuItem("Load from file ...");
	protected JMenuItem menuPreferencesSaveDump = new JMenuItem("Save to file ...");

	// Menu "Help"
	protected JMenu menuHelp = new JMenu("Help");
	// Submenu of "Help"
	protected JMenuItem menuHelpAbout = new JMenuItem("About ...",IconLoader.ico017);
	protected JMenuItem menuHelpUpdate = new JMenuItem("Update ...",IconLoader.ico052);

	// Error messages for analyser
	public static LangTextHolder error01_1 = new LangTextHolder("WARNING: No loop varLangTextHolderdetected ...");
	public static LangTextHolder error01_2 = new LangTextHolder("WARNING: More than one loop variable detected ...");
	public static LangTextHolder error01_3 = new LangTextHolder("You are not allowed to modify the loop variable «%» inside the loop!");
	public static LangTextHolder error02 = new LangTextHolder("No change of the variables in the condition detected. Possible endless loop ...");
	public static LangTextHolder error03_1= new LangTextHolder("The variable «%» has not yet been initialized!");
	public static LangTextHolder error03_2 = new LangTextHolder("The variable «%» may not have been initialized!");
	public static LangTextHolder error04 = new LangTextHolder("You are not allowed to use an IF-statement with an empty TRUE-block!");
	public static LangTextHolder error05 = new LangTextHolder("The variable «%» must be written in uppercase!");
	public static LangTextHolder error06 = new LangTextHolder("The programname «%» must be written in uppercase!");
	public static LangTextHolder error07_1 = new LangTextHolder("«%» is not a valid name for a program or function!");
	public static LangTextHolder error07_2 = new LangTextHolder("«%» is not a valid name for a parameter!");
	public static LangTextHolder error07_3 = new LangTextHolder("«%» is not a valid name for a variable!");
	public static LangTextHolder error08 = new LangTextHolder("It is not allowed to make an assignment inside a condition.");
	public static LangTextHolder error09 = new LangTextHolder("Your program («%») cannot have the same name as a variable or parameter!");
	public static LangTextHolder error10_1 = new LangTextHolder("A single instruction element should not contain input/output instructions and assignments!");
	public static LangTextHolder error10_2 = new LangTextHolder("A single instruction element should not contain input and output instructions!");
	public static LangTextHolder error10_3 = new LangTextHolder("A single instruction element should not contain input instructions and assignments!");
	public static LangTextHolder error10_4 = new LangTextHolder("A single instruction element should not contain ouput instructions and assignments!");
	public static LangTextHolder error11 = new LangTextHolder("You probably made an assignment error. Please check this instruction!");
	public static LangTextHolder error12 = new LangTextHolder("The parameter «%» must start with the letter \"p\" followed by only uppercase letters!");
	public static LangTextHolder error13_1 = new LangTextHolder("Your function does not return any result!");
	public static LangTextHolder error13_2 = new LangTextHolder("Your function may not return a result!");
	// START KGU#78 (#23) 2015-11-25: Check for competitive return mechanisms
	public static LangTextHolder error13_3 = new LangTextHolder("Your functions seems to use several competitive return mechanisms: «%»!");
	// END KGU#78 (#23) 2015-11-25
	// START KGU#3 2015-11-03: New checks for the enhanced For loop
	public static LangTextHolder error14_1 = new LangTextHolder("The FOR loop parameters are not consistent to the loop heading text!");
	public static LangTextHolder error14_2 = new LangTextHolder("The FOR loop step value («%») is not a legal integer constant!");
	// START KGU#3 2015-11-26: More clarity if e.g. a counter variable is named "step" and so is the stepFor parser preference
	public static LangTextHolder error14_3 = new LangTextHolder("Variable name «%» may collide with one of the configured FOR loop heading keywords!");
	// END KGU#3 2015-11-26
	// END KGU#3 2015-11-03
	// START KGU#2 2015-11-25: New check for Call element syntax and Jump consistency 
	public static LangTextHolder error15 = new LangTextHolder("The CALL hasn't got form «[ <var> " + "\u2190" +" ] <routine_name>(<arg_list>)»!");
	public static LangTextHolder error16_1 = new LangTextHolder("A JUMP element may be empty or start with one of %, possibly followed by an argument!");	
	public static LangTextHolder error16_2 = new LangTextHolder("A return instruction, unless at final position, must form a JUMP element!");
	public static LangTextHolder error16_3 = new LangTextHolder("An exit, leave or break instruction is only allowed as JUMP element!");
	public static LangTextHolder error16_4 = new LangTextHolder("Cannot leave or break more loop levels than being nested in («%»)!");
	public static LangTextHolder error16_5 = new LangTextHolder("You must not directly return out of a parallel thread!");
	public static LangTextHolder error16_6 = new LangTextHolder("Wrong argument for this kind of JUMP (should be an integer constant)!");
	public static LangTextHolder error16_7 = new LangTextHolder("Instruction isn't reachable after a JUMP!");
	// END KGU#2 2015-11-25
	// START KGU#47 2015-11-28: New check for concurrency problems
	public static LangTextHolder error17 = new LangTextHolder("Consistency risk due to concurrent access to variable «%» by several parallel threads!");
	// END KGU#47 2015-11-28


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
		menuPreferencesLanguageEnglish.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("en.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageGerman);
		menuPreferencesLanguageGerman.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("de.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageFrench);
		(menuPreferencesLanguageFrench).addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("fr.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageDutch);
		menuPreferencesLanguageDutch.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("nl.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageLuxemburgish);
		menuPreferencesLanguageLuxemburgish.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("lu.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageSpanish);
		menuPreferencesLanguageSpanish.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("es.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguagePortugalBrazil);
		menuPreferencesLanguagePortugalBrazil.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("pt_br.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageItalian);
		menuPreferencesLanguageItalian.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("it.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageSimplifiedChinese);
		menuPreferencesLanguageSimplifiedChinese.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("chs.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageTraditionalChinese);
		menuPreferencesLanguageTraditionalChinese.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("cht.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageCzech);
		menuPreferencesLanguageCzech.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("cz.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguageRussian);
		menuPreferencesLanguageRussian.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("ru.txt"); doButtons(); } } );

		menuPreferencesLanguage.add(menuPreferencesLanguagePolish);
		menuPreferencesLanguagePolish.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) { setLang("pl.txt"); doButtons(); } } );
                
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

		menuHelp.add(menuHelpAbout);
		menuHelpAbout.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) {diagram.aboutNSD(); } } );
		menuHelpAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));

		menuHelp.add(menuHelpUpdate);
		menuHelpUpdate.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent event) {diagram.updateNSD(); } } );
		menuHelpUpdate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

	}

	public void setLookAndFeel(String _laf) {}
	public String getLookAndFeel() { return null;}

	public void doButtons()
	{
		if(NSDControl!=null)
		{
			NSDControl.doButtons();
		}
	}

	public void setLangLocal(String _langfile)
	{
		LangDialog.setLang(this,NSDControl.getLang());
		// START KGU#170 2016-04-01: Enh. #144 - update the favourite export item text
		if (diagram != null)
		{
			String itemText = lbFileExportCodeFavorite.getText().replace("%", diagram.getPreferredGeneratorName());
			this.menuFileExportCodeFavorite.setText(itemText);
		}
		// END KGU#170 2016-04-01
		diagram.analyse();
	}

	public void setLang(String _langfile)
	{
		NSDControl.setLang(_langfile);
	}

	public String getLang()
	{
		return NSDControl.getLang();
	}

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
			
			// START KGU#123 2016-01-03: We allow multiple selection for collapsing
			// collapse & expand - for multiple selection always allowed, otherwise only if a change would occur
			menuDiagramCollapse.setEnabled(conditionNoMult && !diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());
			menuDiagramExpand.setEnabled(conditionNoMult && diagram.getSelected().isCollapsed() || condition && diagram.selectedIsMultiple());			
			// END KGU#123 2016-01-03

			// START KGU#143 2016-01-21: Bugfix #114 - breakpoint control now also here
			menuDiagramBreakpoint.setEnabled(diagram.canCopy());
			// END KGU#143 2016-01-21

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

			// Langauges
			menuPreferencesLanguageEnglish.setSelected(getLang().equals("en.txt"));
			menuPreferencesLanguageGerman.setSelected(getLang().equals("de.txt"));
			menuPreferencesLanguageFrench.setSelected(getLang().equals("fr.txt"));
			menuPreferencesLanguageDutch.setSelected(getLang().equals("nl.txt"));
			menuPreferencesLanguageLuxemburgish.setSelected(getLang().equals("lu.txt"));
			menuPreferencesLanguageSpanish.setSelected(getLang().equals("es.txt"));
			menuPreferencesLanguagePortugalBrazil.setSelected(getLang().equals("pt_br.txt"));
			menuPreferencesLanguageItalian.setSelected(getLang().equals("it.txt"));
			menuPreferencesLanguageSimplifiedChinese.setSelected(getLang().equals("chs.txt"));
			menuPreferencesLanguageTraditionalChinese.setSelected(getLang().equals("cht.txt"));
			menuPreferencesLanguageCzech.setSelected(getLang().equals("cz.txt"));
			menuPreferencesLanguageRussian.setSelected(getLang().equals("ru.txt"));
			menuPreferencesLanguagePolish.setSelected(getLang().equals("pl.txt"));

			// Recentl file
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

	public void updateColors() {}


	public Menu(Diagram _diagram, NSDController _NSDController)
	{
		super();
		diagram=_diagram;
		NSDControl=_NSDController;
		create();
	}

	public void savePreferences() {};

    public JFrame getFrame()
    {
        return NSDControl.getFrame();
    }

    public void loadFromINI()
    {
    }

}

/*
    Structorizer
    A little tool which you can use to create Nassi-Shneiderman Diagrams (NSD)

    Copyright (C) 2009, 2020  Bob Fisch

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

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Structorizer class (main entry point for interactive and batch mode)
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-27      First Issue
 *      Kay Gürtzig     2015-12-16      Bugfix #63 - no open attempt without need
 *      Kay Gürtzig     2016-04-28      First draft for enh. #179 - batch generator mode (KGU#187)
 *      Kay Gürtzig     2016-05-03      Prototype for enh. #179 - incl. batch parser and help (KGU#187)
 *      Kay Gürtzig     2016-05-08      Issue #185: Capability of multi-routine import per file (KGU#194)
 *      Kay Gürtzig     2016-12-02      Enh. #300: Information about updates on start in interactive mode
 *                                      Modification in command-line concatenation
 *      Kay Gürtzig     2016-12-12      Issue #306: multiple arguments in simple command line are now
 *                                      interpreted as several files to be opened in series.
 *      Kay Gürtzig     2017-01-27      Issue #306 + #290: Support for Arranger files in command line
 *      Kay Gürtzig     2017-03-04      Enh. #354: Configurable set of import parsers supported now
 *      Kay Gürtzig     2017-04-27      Enh. #354: Verbose option (-v with log directory) for batch import
 *      Kay Gürtzig     2017-07-02      Enh. #354: Parser-specific options retrieved from Ini, parser cloned.
 *      Kay Gürtzig     2017-11-06      Issue #455: Loading of argument files put in a sequential thread to overcome races
 *      Kay Gürtzig     2018-03-21      Issue #463: Logging configuration via file logging.properties
 *      Kay Gürtzig     2018-06-07      Issue #463: Logging configuration mechanism revised (to support WebStart)
 *      Kay Gürtzig     2018-06-08      Issue #536: Precaution against command line argument trouble
 *      Kay Gürtzig     2018-06-12      Issue #536: Experimental workaround for Direct3D trouble
 *      Kay Gürtzig     2018-06-25      Issue #551: No message informing about version check option on WebStart
 *      Kay Gürtzig     2018-07-01      Bugfix #554: Parser selection and instantiation for batch parsing was defective.
 *      Kay Gürtzig     2018-07-03      Bugfix #554: Now a specified parser will override the automatic search.
 *      Kay Gürtzig     2018-08-17      Help text for parser updated (now list is from parsers.xml).
 *      Kay Gürtzig     2018-08-18      Bugfix #581: Loading of a list of .nsd/.arr/.arrz files as command line argument
 *      Kay Gürtzig     2018-09-14      Issue #537: Apple-specific code revised such that build configuration can handle it
 *      Kay Gürtzig     2018-09-19      Bugfix #484/#603: logging setup extracted to method, ini dir existence ensured
 *      Kay Gürtzig     2018-09-27      Slight modification to verbose option (-v may also be used without argument)
 *      Kay Gürtzig     2018-10-08      Bugfix #620: Logging path setup revised
 *      Kay Gürtzig     2018-10-25      Enh. #416: New option -l maxlen for command line parsing, signatures of
 *                                      export(...) and parse(...) modified.
 *      Kay Gürtzig     2019-03-05      Deprecated method clazz.newInstance() replaced by clazz.getDeclaredConstructor().newInstance()
 *      Kay Gürtzig     2019-03-13      Enh. #696: Batch code export for arrangement files (.arr/.arrz) implemented
 *      Kay Gürtzig     2019-03-26      Enh. #697: Batch code parsing now produces arrangements for multi-routine sources
 *                                      (file overwriting bug fixed on this occasion);
 *                                      Bugfix #715: disambiguateParser() had only worked once in the loop
 *      Kay Gürtzig     2019-07-28      Issue #551 / KGU#715: No hint about version check option on Windows installer either
 *      Kay Gürtzig     2019-08-01      Issues #551, #733 - corrected directory retrieval
 *      Bob Fisch       2019-08-04      Issue #537: ApplicationFactory replaced by OSXAdapter stuff
 *      Kay Gürtzig     2019-08-05      Enh. #737: Possibility of providing a settings file for batch export
 *      Kay Gürtzig     2019-08-07      Enh. #741: Option -s now also respected for interactive mode,
 *                                      Bugfix #742
 *      Kay Gürtzig     2019-09-16      #744 workaround: file open queue on startup for OS X
 *      Kay Gürtzig     2020-03-23      Issues #828, #836: Slight unification of arrangement exports with
 *                                      group export: Without specified entry points all contained diagrams
 *                                      will be qualified for export. Remaining difference: We still first
 *                                      check for contained main diagrams as potential tree roots.
 *      Kay Gürtzig     2020-04-22      Bugfix #853: If both the arr file path and the contained nsd file paths
 *                                      are relative then the batch export failed
 *                                      Issue #828/#836 - The fallback to all roots hadn't worked for arr files
 *      Bob Fisch       2020-05-25      New command line option "-restricted" to suppress code export and import
 *      Kay Gürtzig     2020-06-03      Bugfix #868: mends implementation defects in Bob's most recent change
 *      Kay Gürtzig     2020-06-06      Issue #870: Command line option "-restricted" withdrawn
 *      Kay Gürtzig     2021-06-08      Issue #67, #953: retrieval mechanism for plugin-specific options from ini
 *      Kay Gürtzig     2022-08-01      Enh. #1047: Batch export option -k for keeping the source files apart
 *      Kay Gürtzig     2022-08-11/12   Enh. #1047: Modified effect of -o option (also in combination with -k)
 *      Kay Gürtzig     2023-08-17      Bugfix #1083: Undue error message on using "-p Pascal ..." eliminated
 *      Kay Gürtzig     2023-10-29      Bugfix #1100: Precaution against insufficient Java version.
 *
 ******************************************************************************************************
 *
 *      Comment:
 *
 ******************************************************************************************************///

import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

//import lu.fisch.structorizer.application.ApplicationFactory;
import lu.fisch.structorizer.archivar.Archivar;
import lu.fisch.structorizer.archivar.Archivar.ArchiveIndex;
import lu.fisch.structorizer.archivar.Archivar.ArchiveIndexEntry;
import lu.fisch.structorizer.archivar.ArchivarException;
import lu.fisch.structorizer.archivar.ArchivePool;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.generators.Generator;
import lu.fisch.structorizer.generators.XmlGenerator;
import lu.fisch.structorizer.gui.Mainform;
import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.io.ArrFilter;
import lu.fisch.structorizer.io.ArrZipFilter;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.io.StructogramFilter;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.structorizer.parsers.GENParser;
import lu.fisch.structorizer.parsers.NSDParser;
import lu.fisch.utils.StringList;

public class Structorizer
{
	// START KGU#1095 2023-10-29: Issue #1100 report insufficient JRE
	private static final int REQUIRED_JAVA_VERSION = 11;
	// END KGU#1095 2023-10-29

	// entry point
	public static void main(String args[])
	{
		// START KGU#484 2018-03-21: Issue #463 - Configurability of the logging system ensured
		setupLogging();
		// END KGU#484 2018-03-21
		Logger.getLogger(Structorizer.class.getName()).log(Level.INFO, "Command line: " + new StringList(args).getLongString());
		// START KGU#187 2016-04-28: Enh. #179
		Vector<String> fileNames = new Vector<String>();
		String generator = null;
		String parser = null;
		StringList switches = new StringList();
		//String outFileName = null;
		//String charSet = "UTF-8";
		// START KGU#354 2017-04-27: Enh. #354
		//String logDir = null;
		// END KGU#354 2017-04-27
		// START KGU#538 2018-07-01: issue #554
		//String settingsFile = null;
		// END KGU#538 2018-07-01
		HashMap<String, String> options = new HashMap<String, String>();
		//System.out.println("arg 0: " + args[0]);
		if (args.length == 1 && args[0].equals("-h"))
		{
			printHelp();
			return;
		}
		// START KGU#722 2019-08-06: Enh. #741
		File settings = null;
		boolean openFound = false;	// switch "-open" found?
		// END KGU#722 2019-08-06
		for (int i = 0; i < args.length; i++)
		{
			//System.out.println("arg " + i + ": " + args[i]);
			if (i == 0 && args[i].equals("-x") && args.length > 1)
			{
				generator = args[++i];
			}
			else if (i == 0 && args[i].equals("-p") && args.length > 1)
			{
				parser = "*";
				// START KGU#538 2018-07-01: Bugfix #554 - was nonsense and had to be replaced
				// In order to disambiguate alternative parsers for the same file type, there
				// might be a given parser or language name again (3.28-05).
				// START KGU#1072 2023-08-17: Bugfix #1083 (moreover, option "pas" is no longer accepted)
				//if (i+2 < args.length) {
				//	// Legacy support - parsers will now be derived from the file extensions 
				//	if (args[i+1].equalsIgnoreCase("pas") || args[i+1].equalsIgnoreCase("pascal")) {
				//		parser = "D7Parser";
				//	}
				//	else if (!args[i+1].startsWith("-")) {
				//		// doesn't seem to be an option, so it might be a default parser name...
				//		parser = args[i+1];
				//	}
				//}
				if ((i+2 < args.length) && !args[i+1].startsWith("-") && !args[i+1].contains(".")) {
					// doesn't seem to be an option, so it might be a default parser name...
					parser = args[i+1];
				}
				// END KGU#1072 2023-08-17
				// END KGU#538 2018-07-01
			}
			// START KGU#722 2019-08-07: Enh. #741
			else if (i == 0 && args[i].equals("-open")) {
				openFound = true;
			}
			// END KGU#722 2019-08-07
			// START KGU#538 2018-07-01: Bugfix #554 - was nonsense and had to be replaced 
			// Legacy support - parsers will now be derived from the file extensions 
			//else if (i > 0 && (parser != null) && (args[i].equalsIgnoreCase("pas") || args[i].equalsIgnoreCase("pascal"))
			//		&& !parser.endsWith("pas")) {
			//	parser += "pas";
			//}
			// END KGU#538 2018-07-01
			else if (args[i].equals("-o") && i+1 < args.length)
			{
				// Output file name
				// START KGU#722 2019-08-07: Enh. #741
				if (openFound || generator == null && parser == null) {
					// Mark this as an illegal option
					switches.add(args[i]);
				}
				// END KGU#722 2019-08-07
				//outFileName = args[++i];
				options.put("outFileName", args[++i]);
			}
			else if (args[i].equals("-e") && i+1 < args.length)
			{
				// Encoding
				// START KGU#722 2019-08-07: Enh. #741
				if (openFound || generator == null && parser == null) {
					// Mark this as an illegal option
					switches.add(args[i]);
				}
				// END KGU#722 2019-08-07
				//charSet = args[++i];
				options.put("charSet", args[++i]);
			}
			// START KGU#354 2017-04-27: Enh. #354 verbose mode?
			else if (args[i].equals("-v") && i+1 < args.length)
			{
				// START KGU#722 2019-08-07: Enh. #741
				if (openFound || generator == null && parser == null) {
					// Mark this as an illegal option
					switches.add(args[i]);
				}
				// END KGU#722 2019-08-07
				// START KGU#354 2018-09-27: More tolerance spent
				//logDir = args[++i];
				String dirName = args[i+1]; 
				if (dirName.startsWith("-") || !(new File(dirName)).isDirectory()) {
					// No valid path given, so use "."
					//logDir = ".";
					options.put("logDir", ".");
				}
				else {
					//logDir = args[++i];
					// START KGU#723 2019-08-07: Bugfix #742
					//options.put("logDir", "args[++i]");
					options.put("logDir", args[++i]);
					// END KGU#723 2019-08-07
				}
				// END KGU#354 2018-09-27
			}
			// END KGU#354 2017-04-27
			// START KGU#538 2018-07-01: Issue #554 - new option for a settings file
			else if (args[i].equals("-s") && i+1 < args.length)
			{
				//settingsFile = args[++i];
				// START KGU#722 2019-08-06: Enh. #741
				//options.put("settingsFile", args[++i]);
				settings = new File(args[++i]);
				try {
					if (settings.canRead() || settings.createNewFile()) {
						if (settings.canRead()) {
							// FIXME check whether this resolves the path correctly
							options.put("settingsFile", settings.getAbsolutePath());
						}
					}
				} catch (IOException ex) {
					System.err.println("*** Failure on ensuring specified settings file: " + ex.getMessage());
					Logger.getLogger(Structorizer.class.getName()).log(Level.WARNING, "Option -s " + settings.getPath(), ex);
					settings = null;
				}
				// END KGU#722 2019-08-06
			}
			// END KGU#538 2018-07-01
			// START KGU#602 2018-10-25: Enh. #416 - line length constraint
			else if (args[i].equals("-l") && parser != null && i+1 < args.length) {
				// START KGU#722 2019-08-07: Enh. #741
				if (openFound || generator == null && parser == null) {
					// Mark this as an illegal option
					switches.add(args[i]);
				}
				// END KGU#722 2019-08-07
				try {
					short maxLen = Short.parseShort(args[i+1]);
					if (maxLen == 0 || maxLen >= 20) {
						options.put("maxLineLength", args[++i]);
					}
				}
				catch (NumberFormatException ex) {}
			}
			// END KGU#602 2018-10-25
			// Target standard output?
			else if (args[i].equals("-")) {
				switches.add("-");
			}
			// Other options
			// START KGU#722 2019-08-07: Enh. #741
			else if (args[i].equals("-open")) {
				openFound = true;
			}
			// END KGU#722 2019-08-07
			// Other switches
			else if (args[i].startsWith("-")) {
				// append only the letter (without hyphen)
				switches.add(args[i].substring(1));
			}
			else
			{
				fileNames.add(args[i]);
			}
		}
		
		// START KGU#1095 2023-10-29: Workaround #1100 Try to warn if we are run with an obsolete JRE
		String javaVer = System.getProperty("java.version");
		if (javaVer != null) {
			String[] parts = javaVer.split("\\.", 2);
			if (parts.length > 0) {
				int mainVer = 0;
				try {
					mainVer = Integer.parseInt(parts[0]);
				}
				catch (NumberFormatException ex) {}
				if (mainVer < REQUIRED_JAVA_VERSION) {
					String verMsg = "*** Java runtime environment " + javaVer + " not suited to start Structorizer! We need Java "
							+ REQUIRED_JAVA_VERSION + " at least.";
					System.err.println(verMsg);
					Logger.getLogger(Structorizer.class.getName()).log(Level.SEVERE, verMsg);
					if (generator == null && parser == null) {
						JOptionPane.showMessageDialog(null,
								verMsg, 
								"Java version error",
								JOptionPane.ERROR_MESSAGE);
					}
					System.exit(1);;
				}
			}
			
		}
		// END KGU#1095 2023-10-29
		
		// START KGU#722 2019-08-06: Enh. #741
		if (settings != null) {
			if (generator != null || parser != null) {
				try {
					Ini.getInstance().redirect(settings.getAbsolutePath(), generator != null || parser != null);
				} catch (IOException ex) {
					System.err.println("*** Failing to redirect settings file: " + ex.getMessage());
					Logger.getLogger(Structorizer.class.getName()).log(Level.WARNING, "Option -s " + settings.getPath(), ex);
					options.remove("settingsFile");
				}
			}
			else if (!Ini.setIniPath(settings.getAbsolutePath())) {
				options.remove("settingsFile");
			}
		}
		// END KGU#722 2019-08-06
		if (generator != null)
		{
			//Structorizer.export(generator, fileNames, outFileName, switches, charSet, null);
			Structorizer.export(generator, fileNames, options, switches.concatenate());
			return;
		}
		else if (parser != null)
		{
			// START KGU#354 2017-04-27: Enh. #354 verbose mode
			//Structorizer.parse(parser, fileNames, outFileName, options, charSet);
			//Structorizer.parse(parser, fileNames, outFileName, switches, charSet, settingsFile, logDir);
			Structorizer.parse(parser, fileNames, options, switches.concatenate());
			// END KGU#354 2017-04-27
			return;
		}
		// END KGU#187 2016-04-28
				
		// START KGU#521 2018-06-12: Workaround for #536 (corrupted rendering on certain machines) 
		System.setProperty("sun.java2d.noddraw", "true");
		// END KGU#521 2018-06-12

		// try to load the system Look & Feel
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			//System.out.println("Error setting native LAF: " + e);
		}

		// load the mainform
		final Mainform mainform = new Mainform();
		
		// START KGU#532 2018-06-25: Issue #551 Suppress version notification option hint
		File appDir = Ini.getInstallDirectory();
		// START KGU#715 2019-07-28: 
		//mainform.isAutoUpdating = appPath.endsWith("webstart");
		File uplaFile = new File(appDir.getAbsolutePath() + File.separator + "upla.jar");
		mainform.isAutoUpdating = getApplicationPath().endsWith("webstart") || uplaFile.exists();
		// END KGU#715 2019-07-28
		// END KGU#532 2018-06-25
		// START KGU#440 2017-11-06: Issue #455 Decisive measure against races on loading an drawing
		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
		// END KGU#440 2017-11-06
					//String s = new String();
					// START KGU#722 219-08-07: Enh. #741 - we know the potential file arguments already...
					//int start = 0;
					//if (args.length > 0 && args[0].equals("-open")) {
					//	start = 1;
					//}
					// END KGU#722 2019-08-07
					
					// START KGU#724 2019-09-16: Issue #744 - consider postponed openFile events on OS X
					if (mainform.filesToOpen != null) {
						fileNames.addAll(mainform.filesToOpen);
						mainform.filesToOpen.clear();
					}
					// END KGU#724 2019-09-16

					// If there are several .nsd, .arr, or .arrz files as arguments, then try to load
					// them all ...
					String lastExt = "";	// Last file extension
					// START KGU#722 219-08-07: Enh. #741 - we know the potential file arguments already...
					//for (int i = start; i < args.length; i++)
					for (int i = 0; i < fileNames.size(); i++)
					// END KGU#722 2019-08-07
					{
						// START KGU#306 2016-12-12/2017-01-27: This seemed to address file names with blanks...
						//s += args[i];
						// START KGU#722 2019-08-07: Enh. #741 - we have the filenames already
						//String s = args[i].trim();
						String s = fileNames.get(i).trim();
						// END KGU#722 219-08-07
						if (!s.isEmpty())
						{
							if (lastExt.equals("nsd") && !mainform.diagram.getRoot().isEmpty()) {
								// Push the previously loaded diagram to Arranger
								mainform.diagram.arrangeNSD();
							}
							lastExt = mainform.diagram.openNsdOrArr(s);
							// START KGU#521 2018-06-08: Bugfix #536 (try)
							if (lastExt == "") {
								String msg = "Unsuited or misplaced command line argument \"" + s + "\" ignored.";
								Logger.getLogger(Structorizer.class.getName()).log(Level.WARNING, msg);
								JOptionPane.showMessageDialog(mainform, msg,
										"Command line", JOptionPane.WARNING_MESSAGE);
							}
							// END KGU#521 2018-06-08
						}
						// END KGU#306 2016-12-12/2017-01-27
					}
					// START KGU#722 2019-08-07: Enh. #741
					if (!switches.isEmpty()) {
						StringBuilder opts = new StringBuilder();
						for (int i = 0; i < switches.count(); i++) {
							String swtch = switches.get(i);
							opts.append(" -");
							if (!swtch.equals("-")) {
								opts.append(swtch);
							}
							String msg = "Unsupported command line options " + opts.toString().trim() + " ignored.";
							Logger.getLogger(Structorizer.class.getName()).log(Level.WARNING, msg);
							JOptionPane.showMessageDialog(mainform, msg,
									"Command line", JOptionPane.WARNING_MESSAGE);
						}
					}
					// END KGU#722 2019-08-07
		// START KGU#440 2017-11-06: Issue #455 Decisive measure against races on loading an drawing
				}
				// START KGU#306 2016-12-12: Enh. #306 - Replaced with the stuff in the loop above
//			s = s.trim();
//			// START KGU#111 2015-12-16: Bugfix #63 - no open attempt without need
//			//mainform.diagram.openNSD(s);
//			if (!s.isEmpty())
//			{
//				mainform.diagram.openNSD(s);
//			}
//			// END KGU#111 2015-12-16
				// END KGU#306 2016-12-12
			});
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// END KGU#440 2017-11-06
		mainform.diagram.redraw();

		if(System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("apple.awt.graphics.UseQuartz", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Structorizer");

			mainform.doOSX();
		}
		
		// Without this, the toolbar had often wrong status when started from a diagram 
		mainform.doButtons();
		// START KGU#300 2016-12-02
		mainform.popupWelcomePane();
		// END KGU#300 2016-12-02
	}

	// START KGU#579 2018-09-19: Logging setup extracted on occasion of a #463 fix
	/**
	 * Initializes the logging system and tries to ensure a product-specific
	 * default logging configuration in the anticipated Structorizer ini directory
	 */
	private static void setupLogging() {
		// The logging configuration (for java.util.logging) is expected next to the jar file
		// (or in the project directory while debugged from the IDE).
		File iniDir = Ini.getIniDirectory();
		File configFile = new File(iniDir.getAbsolutePath(), "logging.properties");
		// If the file doesn't exist then we'll copy it from the resource
		if (!configFile.exists()) {
			InputStream configStr = Structorizer.class.getResourceAsStream("/lu/fisch/structorizer/logging.properties");
			if (configStr != null) {
				// START KGU#579 2018-09-19: Bugfix #603 On the very first use of Structorizer, iniDir won't exist (Ini() hasn't run yet)
				//copyStream(configStr, configFile);
				try {
					// START KGU#595 2018-10-07: Bugfix #620 - We ought to check the success 
					//if (!iniDir.exists())
					//{
					//	iniDir.mkdir();
					//}
					//copyStream(configStr, configFile);
					if (!iniDir.exists() &&	!iniDir.mkdirs()) {
						System.err.println("*** Creation of folder \"" + iniDir + "\" failed!");
						iniDir = new File(System.getProperty("user.home"));
						configFile = new File(iniDir.getAbsolutePath(), "logging.properties");
					}
					copyLogProperties(configStr, configFile);
					// END KGU#595 2018-10-07
				}
				catch (Exception ex) {
					ex.printStackTrace();
					try {
						configStr.close();
					} catch (IOException e) {}
				}
				// END KGU#579 2018-09-19
			}
		}
		if (configFile.exists()) {
			System.setProperty("java.util.logging.config.file", configFile.getAbsolutePath());
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (SecurityException | IOException e) {
				// Just write the trace to System.err
				e.printStackTrace();
			}
		}
		// START KGU#484 2018-04-05: Issue #463 - If the copy attempt failed too, try to leave a note..,
		else {
			File logLogFile = new File(iniDir.getAbsolutePath(), "Structorizer.log");
			try {
				// Better check twice, we may not know at what point the first try block failed...
				if (!iniDir.exists())
				{
					iniDir.mkdir();
				}
				OutputStreamWriter logLog =	new OutputStreamWriter(new FileOutputStream(logLogFile), "UTF-8");
				logLog.write("No logging config file in dir " + iniDir + " - using Java logging standard.");
				logLog.close();
			} catch (IOException e) {
				// Just write the trace to System.err
				e.printStackTrace();
			}
		}
		// END KGU#484 2018-04-05
		//System.out.println(System.getProperty("java.util.logging.config.file"));
	}
	// END KGU#579 2018-09-19
	
	// START KGU#187 2016-05-02: Enh. #179
	private static final String[] synopsis = {
		"Structorizer [-s SETTINGSFILE] [-open] [NSDFILE|ARRFILE|ARRZFILE]...",
		"Structorizer -x GENERATOR [-a] [-b] [-c] [-f] [-k] [-l] [-t] [-e CHARSET] [-s SETTINGSFILE] [-] [-o OUTFILE] (NSDFILE|ARRSPEC|ARRZSPEC)...",
		"Structorizer -p [PARSER] [-f] [-z] [-v [LOGPATH]] [-l MAXLINELEN] [-e CHARSET] [-s SETTINGSFILE] [-o OUTFILE] SOURCEFILE...",
		"Structorizer -h",
		"(See " + Element.E_HELP_PAGE + "?menu=96 or " + Element.E_HELP_PAGE + "?menu=136 for details.)"
	};
	// END KGU#187 2016-05-02
	
	// START KGU#187 2016-04-28: Enh. #179
	/*****************************************
	 * batch code export method
	 * @param _generatorName - name of the target language or generator class
	 * @param _nsdOrArrNames - vector of the diagram and/or archive file names
	 * @param _options - map of non-binary command line options
	 * @param _switches - set of switches (on / off)
	 *****************************************/
	public static void export(String _generatorName, Vector<String> _nsdOrArrNames, HashMap<String, String> _options, String _switches)
	{
		Vector<Root> roots = new Vector<Root>();
		// START KGU#679 2019-03-13: Enh. #696 - allow to export archives
		HashMap<ArchivePool, Vector<Root>> pools = new LinkedHashMap<ArchivePool, Vector<Root>>();
		Archivar archivar = new Archivar();
		StringList poolFileNames = new StringList();
		// END KGU#679 2019-02-13
		String outFileName = _options.get("outFileName");
		String codeFileName = outFileName;
		// START KGU#1051 2022-08-11: Issue #1047 handle output folder
		File outFile = null;
		File outFolder = null;
		if (outFileName != null) {
			outFolder = outFile = new File(outFileName);
			if (outFolder.isDirectory()) {
				outFile = null;
				codeFileName = null;	// not suited as code file name - derive it
			}
			else /* doesn't exist or isn't a directory */ {
				// Check if at least the parent is an existing folder
				outFolder = outFolder.getAbsoluteFile().getParentFile();
				if (!outFolder.isDirectory()) {
					System.err.println("*** Output folder \"" + outFolder.getAbsolutePath()
					+ "\" does not exist, -o will be ignored.");
					outFolder = outFile = null;
					// Path isn't suited as code file - derive one
					codeFileName = outFileName = null;
				}
			}
		}
		// END KGU#1051 2022-08-11
		// the encoding to be used. 
		String charSet = _options.getOrDefault("charSet", "UTF-8");
		// START KGU#720 2019-08-07: Enh. #737
		// path of a property file to be preferred over structorizer.ini
		boolean settingsGiven = _options.containsKey("settingsFile");
		// END KGU#720 2019-08-07
		// START KGU#1040 2022-08-01: Issue #1047 separate nsd export and scissor fixing
		boolean toStdOut = _switches.indexOf('-') >= 0;
		// END KGU#1040 2022-08-01
		for (String fName : _nsdOrArrNames)
		{
			try
			{
				// Test the existence of the current NSD or arrangement file
				// START KGU#679 2019-03-13: Enh. #696 - allow to export archives
				//File f = new File(fName);
				//if (f.exists())
				StringList arrSpec = StringList.explode(fName, "!");
				File f = new File(arrSpec.get(0));
				boolean isArrz = false;
				if (f.exists() && StructogramFilter.isNSD(fName))
				// END KGU#679 2019-02-13
				{
					// open an existing file and gather the Root
					NSDParser parser = new NSDParser();
					// START KGU#363 2017-05-21: Issue #372 API change
					//root = parser.parse(f.toURI().toString());
					Root root = parser.parse(f);
					// END KGU#363 2017-05-21
					root.filename = fName;
					roots.add(root);
					// If no output file name is given then derive one from the first NSD file
					if (codeFileName == null && !toStdOut)
					{
						codeFileName = f.getCanonicalPath();
						// START KGU#1051 2022-08-11: Issue #1047 handle output folder
						if (outFolder != null) {
							codeFileName = Path.of(outFolder.getPath(), f.getName()).toString();
						}
						// END KGU#1051 2022-08-11
					}
				}
				// START KGU#679 2019-03-13: Enh. #696 - allow to export archives
				else if (f.exists() && (ArrFilter.isArr(arrSpec.get(0)) || (isArrz = ArrZipFilter.isArr(arrSpec.get(0))))) {
					arrSpec.remove(0);
					// START KGU#851 2020-04-22: Bugfix #853 archivar must be able to derive the parent directory
					if (!isArrz && !f.isAbsolute()) {
						f = f.getAbsoluteFile();
					}
					// END KGU#851 2020-04-22
					if (!addExportPool(pools, archivar, arrSpec, f, isArrz)) {
						System.err.println("*** No starting diagrams in arrangement \"" + f.getAbsolutePath() + "\" found. Skipped.");
					}
					else 
					{
						// START KGU#1051 2022-08-11: Issue #1047 handle output folder
						//String outFilePath = outFileName;
						// If no output file name is given then derive one from the arrangement file
						//if (outFilePath == null && !toStdOut) {
						//	outFilePath = f.getCanonicalPath();
						//}
						String outFilePath = f.getCanonicalPath();
						if (outFileName == null && toStdOut) {
							outFilePath = null;
						}
						else if (outFolder != null) {
							/*
							 * If outFolder is given then it depends on -k whether the
							 * target file base name is derived from the specified
							 * outFileName (if it's not a folder) or from the arrangement
							 * file name.
							 */
							String baseName = f.getName();
							// Compose the out file path from the outFolder and the basename
							outFilePath = Path.of(outFolder.getAbsolutePath(), baseName).toString();
						}
						// END KGU#1051 2022-08-11
						poolFileNames.add(outFilePath);
					}
				}
				// END KGU#679 2019-02-13
				else
				{
					System.err.println("*** File \"" + fName + "\" not found or inappropriate. Skipped.");
				}
			}
			catch (Exception e)
			{
				System.err.println("*** Error while trying to load " + fName + ": " + e.getMessage());
			}
		}
		// START KGU#1051 2022-08-12: Issue #1047
		// Special case of given file name and only a single arrangement in the list
		if (roots.isEmpty() && pools.size() == 1 && outFile != null) {
			poolFileNames.set(0, outFile.getAbsolutePath());
		}
		// END KGU#1051 2022-08-12
		
		String genClassName = null;
		// START KGU#679 2019-03-13: Enh. #696 - allow to export archives
		//if (!roots.isEmpty())
		if (!roots.isEmpty() || !pools.isEmpty())
		// END KGU#679 2019-02-13
		{
			String usage = "Usage: " + synopsis[1] + "\nwith GENERATOR =";
			// We just (ab)use some class residing in package gui to fetch the plugin configuration 
			BufferedInputStream buff = new BufferedInputStream(lu.fisch.structorizer.gui.EditData.class.getResourceAsStream("generators.xml"));
			GENParser genp = new GENParser();
			Vector<GENPlugin> plugins = genp.parse(buff);
			// START KGU#977 2021-06-08: Issues #667, #953 We may have to fetch plugin-specific options
			Vector<HashMap<String, String>> pluginOptions = null;
			// END KGU#977 2021-06-08
			try { buff.close();	} catch (IOException e) {}
			for (int i=0; genClassName == null && i < plugins.size(); i++)
			{
				GENPlugin plugin = (GENPlugin) plugins.get(i);
				StringList names = StringList.explode(plugin.title, "/");
				String className = plugin.getKey();
				usage += (i>0 ? " |" : "") + "\n\t" + className;
				if (className.equalsIgnoreCase(_generatorName))
				{
					genClassName = plugin.className;
					// START KGU#977 2021-06-08: Issues #667, #953 We may have to fetch plugin-specific options
					pluginOptions = plugin.options;
					// END KGU#977 2021-06-08
				}
				else
				{
					for (int j = 0; j < names.count(); j++)
					{
						if (names.get(j).trim().equalsIgnoreCase(_generatorName)) {
							genClassName = plugin.className;
							// START KGU#977 2021-06-08: Issues #667, #953 We may have to fetch plugin-specific options
							pluginOptions = plugin.options;
							// END KGU#977 2021-06-08
						}
						usage += " | " + names.get(j).trim();
					}
				}
			}

			if (genClassName == null)
			{
				System.err.println("*** Unknown code generator \"" + _generatorName + "\"");
				System.err.println(usage);
				System.exit(1);
			}
			
			try
			{
				Class<?> genClass = Class.forName(genClassName);
				Generator gen = (Generator) genClass.getDeclaredConstructor().newInstance();
				// START KGU#977 2021-06-08: Issues #67, #953 Provide plugin-specific options in advance
				if (settingsGiven && pluginOptions != null) {
					StringList problems = gen.setPluginOptionsFromIni(pluginOptions);
					for (int i = 0; i < problems.count(); i++) {
						System.err.println(problems.get(i));
					}
				}
				// Report the retrieved options
				if (!pluginOptions.isEmpty()) {
					System.out.println("Retrieved generator-specific options:");
					for (HashMap<String, String> option: pluginOptions) {
						String optionName = option.get("name");
						Object optionValue = gen.getPluginOption(optionName, null);
						if (optionValue != null) {
							System.out.println(" + " + optionName + " = " + optionValue);
						}
					}
					System.out.println();
				}
				// END KGU#977 2021-06-08
				// START KGU#679 2019-03-13: Enh. #696 - allow to export archives
				//if (!roots.isEmpty())
				//gen.exportCode(roots, codeFileName, _switches, charSet);
				// ======= Export nsd files ======
				if (!roots.isEmpty()) {
					// START KGU#1040 2022-08-01: Issue #1047: Allow isolated export
					//gen.exportCode(roots, codeFileName, _switches, charSet, settingsGiven, null);
					if (_switches.indexOf('k') >= 0) {
						// Export into separate (isolated) code files
						for (Root root: roots) {
							File f = new File(root.filename);
							Vector<Root> oneRoot = new Vector<Root>();
							oneRoot.add(root);
							// START KGU#1051 2022-08-11: Issue #1047 handle output folder
							if (outFolder != null) {
								f = new File(Path.of(outFolder.getAbsolutePath(), f.getName()).toString());
							}
							// END KGU#1051 2022-08-11
							gen.exportCode(oneRoot, f.getAbsolutePath(), _switches, charSet, settingsGiven, null);
							if (toStdOut) {
								System.out.println();
							}
						}
					}
					else {
						// Export into a single amalgamated code file with scissor lines
						// START KGU#720 2019-08-06: Enh. #737 - now with specific option file
						//gen.exportCode(roots, codeFileName, _switches, charSet, null);
						gen.exportCode(roots, codeFileName, _switches, charSet, settingsGiven, null);
						// END KGU#720 2019-08-06
						// Ensure a newline after the last line of code
						if (toStdOut) {
							System.out.println();
						}
					}
					// END KGU#1040 2022-08-01
				}
				// ======= Export arr/arrz files ======
				int i = 0;
				for (Entry<ArchivePool, Vector<Root>> poolEntry: pools.entrySet()) {
					// START KGU#1040 2022-08-01: Issue #1047: Ensure scissor lines on stdout
					//gen.exportCode(poolEntry.getValue(), poolFileNames.get(i++), _switches, charSet, settingsGiven, poolEntry.getKey());
					if (toStdOut
							&& (!roots.isEmpty() || pools.size() > 1)) {
						String poolName = poolEntry.getKey().getName();
						System.out.println(Generator.prepareScissorLine(true,
								gen.deriveCodeFileName(poolName, false)));
					}
					// START KGU#720 2019-08-05: Enh. #737 - now with specific option file
					//gen.exportCode(poolEntry.getValue(), poolFileNames.get(i++), _switches, charSet, poolEntry.getKey(), null);
					gen.exportCode(poolEntry.getValue(), poolFileNames.get(i++), _switches, charSet, settingsGiven, poolEntry.getKey());
					// END KGU#720 2019-08-05
					// Ensure a newline after the last line of code
					if (toStdOut) {
						System.out.println();
					}
					// END KGU#1040 2022-08-01
				}
				// END KGU#679 2019-02-13
			}
			catch(java.lang.ClassNotFoundException ex)
			{
				System.err.println("*** Generator class " + ex.getMessage() + " not found!");
				System.exit(3);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
				System.err.println("*** Error on instantiating " + _generatorName + "\n" + ex.getMessage());
				ex.printStackTrace();
				System.exit(3);
			}
			catch(Exception e)
			{
				System.err.println("*** Error on using " + _generatorName + "\n" + e.getMessage());
				e.printStackTrace();
				System.exit(4);
			}
		}
		else
		{
			System.err.println("*** No NSD files for code generation.");
			System.exit(2);
		}
	}
	// END KGU#187 2016-04-28

	/**
	 * Tries to form an {@link ArchivePool} from arrangement file {@code aFile} and
	 * to identify the pool roots for export from the signature list {@code arrSpec}
	 * or (if empty) all program roots form the pool.
	 * @param pools - a map from {@link ArchivePool}s to start diagram sets - this is where the
	 * {@link ArchivePool} derived from {@code aFile} will be added to.
	 * @param archivar - the employed {@link Archivar}
	 * @param arrSpec - the arrangement specification
	 * @param f - the arrangement file (may be an archive - .arrz- or just a list - .arr -)
	 * @param isArrz - indicates whether file {@code f} is a compressed arrangement archive
	 * @throws Exception - if something goes wrong
	 */
	private static boolean addExportPool(HashMap<ArchivePool, Vector<Root>> pools, Archivar archivar, StringList arrSpec,
			File f, boolean isArrz) throws Exception {
		boolean done = false;
		ArchiveIndex index = null;
		if (isArrz) {
			//index = archivar.getArrangementArchiveContent(f, null);
			index = archivar.unzipArrangementArchive(f, null);
		}
		else {
			index = archivar.makeNewIndexFor(f);
		}
		ArchivePool pool = new ArchivePool(index);
		// Now collect the starting roots within the pool
		Vector<Root> poolRoots = new Vector<Root>();
		if (!arrSpec.isEmpty()) {
			// Starting roots explicitly given
			for (Iterator<ArchiveIndexEntry> iter = index.iterator(); iter.hasNext();)
			{
				ArchiveIndexEntry entry = iter.next();
				if (entry.name == null) {
					entry.getRoot(archivar);
				}
				String signature = entry.getSignature();
				Root root = null;
				if (arrSpec.contains(signature)) {
					root = entry.getRoot(archivar);
					if (root != null) {
						poolRoots.add(root);
					}
					arrSpec.removeAll(signature);
				}
			}
			for (int i = 0; i < arrSpec.count(); i++) {
				System.err.println("*** No diagram " + arrSpec.get(i) + " in arrangement " + f.getAbsolutePath() + ". Skipped.");
			}
		}
		else {
			// START KGU#815/KGU#824 2020-03-23: Enh. #828, issue #836 - Collect all Roots from the archive as fallback
			Vector<Root> allRoots = new Vector<Root>();
			// END KGU#815/KGU#824 2020-03-23
			// Identify and collect all main diagrams of the pool
			for (Iterator<ArchiveIndexEntry> iter = index.iterator(); iter.hasNext();)
			{
				ArchiveIndexEntry entry = iter.next();
				Root root = null;
				if (entry.name == null) {
					entry.getRoot(archivar);	// This may set entry.name!
				}
				// START KGU#815/KGU#824 2020-04-22: Enh. #828, issue #836 - Collect all Roots from the archive as fallback
				//if (entry.name != null && entry.minArgs == -1) {
				if (entry.name != null) {
				// END KGU#815/KGU#824 2020-04-22
					root = entry.getRoot(archivar);
					// START KGU#815/KGU#824 2020-03-23: Enh. #828, issue #836 - Collect all Roots from the archive as fallback
					//if (root != null && root.isProgram()) {
					//	poolRoots.add(root);
					//}
					if (root != null) {
						allRoots.add(root);
						if (root.isProgram()) {
							poolRoots.add(root);
						}
					}
					// END KGU#815/KGU#824 2020-03-23
				}
			}
			// START KGU#815/KGU#824 2020-03-23: Enh. #828, issue #836 cautious unification with group export:
			// If no suitable program Root is found then we will simply add all diagrams
			if (poolRoots.isEmpty()) {
				poolRoots = allRoots;
			}
			// END KGU#815/KGU#824 2020-03-23
		}
		if (!poolRoots.isEmpty()) {
			pools.put(pool, poolRoots);
			done = true;
		}
		return done;
	}
	
	// START KGU#187 2016-04-29: Enh. #179 - for symmetry reasons also allow a parsing in batch mode, 2019-03-05 made public
	/*****************************************
	 * batch code import method
	 * @param _parserName - name of a preferred default parser (just for the case of ambiguity)
	 * @param _filenames - names of the files to be imported
	 * @param _options - map of non-binary command line parameters
	 * @param _switches - set of switches (either on or off)
	 *****************************************/
	public static void parse(String _parserName, Vector<String> _filenames, HashMap<String, String> _options, String _switches)
	{
		
		String usage = "Usage: " + synopsis[2] + "\nAccepted file extensions:";
		// base name of the nsd file(s) to be created
		String outFile = _options.get("outFileName"); 
		// the encoding to be assumed or used 
		String charSet = _options.getOrDefault("charSet", "UTF-8");
		// START KGU#722 2019-08-07: Enh. #741 - no longer needed, ini redirection already done
		// path of a property file to be preferred against structorizer.ini
		//String settingsFileName = _options.get("settingsFile");
		// END KGU#722 2019-08-07
		// Path of the target folder for the parser log
		String _logDir = _options.get("logDir");

		Vector<GENPlugin> plugins = null;
		// START KGU#354 2017-03-10: Enh. #354 configurable parser plugins
		// Initialize the mapping file extensions -> CodeParser
		// We just (ab)use some class residing in package gui to fetch the plugin configuration 
		BufferedInputStream buff = new BufferedInputStream(lu.fisch.structorizer.gui.EditData.class.getResourceAsStream("parsers.xml"));
		try {
			GENParser genp = new GENParser();
			plugins = genp.parse(buff);
		}
		finally {
			try { buff.close();	} catch (IOException e) {}
		}
		HashMap<CodeParser, GENPlugin> parsers = new HashMap<CodeParser, GENPlugin>();
		//String parsClassName = null;
		CodeParser specifiedParser = null;
		for (int i=0; i < plugins.size(); i++)
		{
			GENPlugin plugin = (GENPlugin) plugins.get(i);
			String className = plugin.className;
			try
			{
				Class<?> parsClass = Class.forName(className);
				CodeParser parser = (CodeParser) parsClass.getDeclaredConstructor().newInstance();
				parsers.put(parser, plugin);
				usage += "\n\t";
				for (String ext: parser.getFileExtensions()) {
					usage += ext + ", ";
				}
				// Get rid of last ", "
				if (usage.endsWith(", ")) {
					usage = usage.substring(0, usage.length()-2) + " for " + parser.getDialogTitle();
				}
				// START KGU#538 2018-07-01: Bugfix #554
				if (_parserName.equalsIgnoreCase(plugin.getKey()) 
						|| _parserName.equalsIgnoreCase(parser.getDialogTitle())
						|| _parserName.equalsIgnoreCase(plugin.title)) {
					specifiedParser = parser;
				}
				// END KGU#538 2018-07-01
			}
			catch(java.lang.ClassNotFoundException ex)
			{
				System.err.println("*** Parser class " + ex.getMessage() + " not found!");
			}
			catch(Exception e)
			{
				// START KGU#538 2018-07-01: Bugfix #554 
				//System.err.println("*** Error on creating " + _parserName);
				System.err.println("*** Error on creating " + className);
				// END KGU#538 2018-07-01
				e.printStackTrace();
			}
		}
		// END KGU#354 2017-03-10
		// START KGU#538 2018-07-03: Bugfix #554
		if (!_parserName.equals("*") && specifiedParser == null) {
			System.err.println("*** No parser \"" + _parserName+ "\" found! Trying standard parsers.");
		}
		// END KGU#538 2018-07-03
		
		// START KGU#193 2016-05-09: Output file name specification was ignored, option f had to be tuned.
		boolean overwrite = _switches.indexOf("f") >= 0 && 
				!(outFile != null && !outFile.isEmpty() && _filenames.size() > 1);
		// END KGU#193 2016-05-09
		// START KGU#678 2019-03-26: Enh. #697 - Allow to put results in arrangement archives
		boolean asArchive = _switches.indexOf("z") >= 0;
		// END KGU#678 2019-03-26
		
		// START KGU#354 2017-03-03: Enh. #354 - support several parser plugins
		// While there was only one input language candidate, a single Parser instance had been enough
		//D7Parser d7 = new D7Parser("D7Grammar.cgt");
		// END KGU#354 2017-03-04

		// START KGU#696 2019-03-26: Bugfix #715 - standard input stream had accidently been closed
		Scanner scnr = new Scanner(System.in);
		// END KGU#696 2019-03-26
		// START KGU#538 2018-07-01: Bugfix #554 - for the case there are alternatives
		Vector<CodeParser> suitedParsers = new Vector<CodeParser>();
		// END KGU#538 2018-07-01
		// START KGU#1072 2023-08-17: Bugfix #1083 Make sure that only the first "filename" is checked against the parser name
		boolean filename1st = true;
		// END KGU#1072 2023-08-17
		for (String filename : _filenames)
		{
			// START KGU#538 2018-07-04: Bugfix #554 - the 1st "filename" might be the parser name
			// START KGU#1072 2023-08-17: Bugfix #1083 Make sure that only the first "filename" is skipped
			//if (specifiedParser != null && filename.equals(_parserName)) {
			//	continue;
			//}
			if (specifiedParser != null && filename1st && filename.equals(_parserName)) {
				filename1st = false;
				continue;
			}
			filename1st = false;
			// END KGU#1072 2023-08-17
			// END KGU#538 2018-07-04
			// START KGU#194 2016-05-08: Bugfix #185 - face more contained roots
			//Root rootNew = null;
			List<Root> newRoots = new LinkedList<Root>();
			// END KGU#194 2016-05-08
			// START KGU#354 2017-03-04: Enh. #354
			//if (fileExt.equals("pas"))
			File importFile = new File(filename);
			// START KGU#538 2018-07-01: Bugfix #554
			suitedParsers.clear();
			// END KGU#538 2018-07-01
			CodeParser parser = specifiedParser;
			// If the parser wasn't specified explicitly then search for suited parsers
			if (parser == null) {
				// START KGU#416 2017-07-02: Enh. #354, #409 Parser retrieval combined with option retrieval
				for (Entry<CodeParser, GENPlugin> entry: parsers.entrySet()) {
					if (entry.getKey().accept(importFile)) {
						// START KGU#538 2018-07-01: Bugfix #554
						//parser = cloneWithPluginOptions(entry.getValue());
						//break;
						// If the preferred parser is among the suited ones, select it
						if (entry.getKey() == specifiedParser) {
							parser = specifiedParser;
							break;
						}
						suitedParsers.add(entry.getKey());
						// END KGU#538 2018-07-01
					}
				}
				// START KGU#538 2018-07-01: Bugfix #554
				if (parser == null) {
					// START KGU#696 2019-03-26: Bugfix #715 - standard input scanner shall not be closed
					//parser = disambiguateParser(suitedParsers, filename);
					parser = disambiguateParser(suitedParsers, filename, scnr);
					// END KGU#696 2019-03-26
				}
				// END KGU#538 2018-07-01
				// END KGU#416 2017-07-02
				if (parser == null) {
					System.out.println("--- File \"" + filename + "\" skipped (not accepted by any parser)!");
					continue;
				}
				// END KGU#354 2017-03-09
			}
			// START KGU#538 2018-07-01: Bugfix #554
			System.out.println("--- Processing file \"" + filename + "\" with " + parser.getClass().getSimpleName() + " ...");
			// Unfortunately, CodeParsers aren't reusable, so we better create a new instance in any case.
			// START KGU#722 2019-08-07: Enh. #741 - now ini will already have been associated with a differing settings file
			//parser = cloneWithPluginOptions(parsers.get(parser), settingsFileName);
			parser = cloneWithPluginOptions(parsers.get(parser), null);
			// END KGU#722 2019-08-07
			// START KGU#678 2019-03-26: fileExt had always remained null
			StringList fileExts = new StringList(parser.getFileExtensions());
			// END KGU#678 2019-03-26
			// END KGU#538 2018-07-01
			// START KGU#602 2018-10-25: Issue #416
			if (_options.containsKey("maxLineLength")) {
				try {
					short maxLineLength = Short.parseShort(_options.get("maxLineLength"));
					if (maxLineLength >= 0) {
						parser.optionMaxLineLength = maxLineLength;
					}
				}
				catch (NumberFormatException ex) {}		
			}
			// END KGU#602 2018-10-25
			// START KGU#194 2016-05-04: Bugfix for 3.24-11 - encoding wasn't passed
			// START KGU#354 2017-04-27: Enh. #354 pass in the log directory path
			//newRoots = parser.parse(filename, _charSet);
			newRoots = parser.parse(filename, charSet, _logDir);
			// END KGU#354 2017-04-27
			// END KGU#194 2016-05-04
			if (!parser.error.isEmpty())
			{
				System.err.println("*** Parser error in file \"" + filename + "\":\n" + parser.error);
				continue;
			}

			// Now save the roots as NSD files. Derive the target file names from the source file name
			// if _outFile isn't given.
			// START KGU#193 2016-05-09: Output file name specification was ignred, optio f had to be tuned.
			if (outFile != null && !outFile.isEmpty())
			{
				filename = outFile;
			}
			// END KGU#193 2016-05-09
			// START KGU#678 2019-03-26: Enh. #697 Create an arrangement archive for multiple roots
			// Moreover, the feedback of the overwrite variable seems to have been a refactoring defect
			//overwrite = writeRootsToFiles(newRoots, filename, fileExt, overwrite);
			if (newRoots.size() > 1 && asArchive) {
				writeRootsToArchive(newRoots, filename, fileExts, overwrite);
			}
			else {
				writeRootsToFiles(newRoots, filename, fileExts, overwrite);
			}
			/* If there are several source files and an out file name was given then we may
			 * not of course allow that subsequent results overwrite the former ones.
			 */
			if (outFile != null && !outFile.isEmpty()) {
				overwrite = false;
			}
			// END KGU#678 2019-03 26
		}
		// START KGU#696 2019-03-26: Bugfix #715 - Now the input scanner may be closed
		scnr.close();
		// END KGU#696 2019-03-26
	}
	// END KGU#187 2016-04-29

	// START KGU#538 2018-07-01: Bugfix #554
	/**
	 * Generates the nsd files from the given list of {@link Root}s
	 * 
	 * @param newRoots - list of generated {@link Root}s
	 * @param filename - the base file name for the resulting nsd files (may be
	 *     the source file name or a specified out file name)
	 * @param fileExts - file name extensions to be replaced (typical extensions of the source file).
	 * @param overwrite - whether existing files are to be overwritten - otherwise a number
	 *     will be appended to avoid name clashes.
	 */
	private static void writeRootsToFiles(List<Root> newRoots, String filename, StringList fileExts, boolean overwrite)
	{
		// START KGU#194 2016-05-08: Bugfix #185 - face more contained roots
		//if (rootNew != null)
		boolean multipleRoots = newRoots.size() > 1;
		// START KGU#678 2019-03-26: Enh #697
		Archivar.ArchiveIndex index = null;
		if (multipleRoots) {
			index = (new Archivar()).makeEmptyIndex();
		}
		// END KGU#678 2019-03-26
		for (Root rootNew : newRoots)
		// END KGU#194 2016-05-08
		{
			StringList nameParts = ensureFileExtension(filename, fileExts, "nsd");
			// In case of multiple roots (subroutines) insert the routine's proposed file name
			if (multipleRoots && !rootNew.isProgram())
			{
				nameParts.insert(rootNew.proposeFileName(), nameParts.count()-1);
			}
			//System.out.println("File name raw: " + nameParts);
			if (!overwrite)
			{
				makeUniqueFilename(nameParts);
			}
			String filenameToUse = nameParts.concatenate(".");
			//System.out.println("Writing to " + filename);
			try {
				FileOutputStream fos = new FileOutputStream(filenameToUse);
				Writer out = null;
				out = new OutputStreamWriter(fos, "UTF8");
				try {
					XmlGenerator xmlgen = new XmlGenerator();
					out.write(xmlgen.generateCode(rootNew,"\t", false));
				}
				finally {
					out.close();
				}
				// START KGU#678 2019-03-26: Enh #697
				if (index != null) {
					rootNew.filename = filenameToUse;
					index.addEntryFor(rootNew, null);
				}
				// END KGU#678 2019-03-26
			}
			catch (IOException e) {
				System.err.println("*** " + e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
		// START KGU#678 2019-03-26: Enh. #697 - write an arrangement list for multiple results
		if (index != null) {
			StringList lines = index.deriveArrangementList(false);
			StringList nameParts = ensureFileExtension(filename, fileExts, "arr");
			if (!overwrite)
			{
				makeUniqueFilename(nameParts);
			}
			try {
				FileOutputStream fos = new FileOutputStream(nameParts.concatenate("."));
				Writer out = null;
				out = new OutputStreamWriter(fos, "UTF8");
				try {
					out.write(lines.getText());
					out.write('\n');	// Issue #706
				}
				finally {
					out.close();
				}
			}
			catch (IOException e) {
				System.err.println("*** " + e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
		// END KGU#678 2019-03-26
	}

	// START KGU#678 2019-03-26: Enh. #697
	private static void writeRootsToArchive(List<Root> roots, String filename, StringList fileExts, boolean overwrite) {
		StringList nameParts = ensureFileExtension(filename, fileExts, "arrz");
		if (!overwrite)
		{
			makeUniqueFilename(nameParts);
		}
		Archivar archivar = new Archivar();
		filename = nameParts.concatenate(".");
		StringList troubles = new StringList();
		try {
			archivar.zipArrangement(new File(filename), roots, false, troubles);
			for (int i = 0; i < troubles.count(); i++) {
				System.err.println("*** " + troubles.get(i));
			}
		} catch (ArchivarException e) {
			System.err.println("*** Error on creating archive " + filename + ": " + e.toString());
		}
	}

	/**
	 * @param nameParts
	 */
	private static void makeUniqueFilename(StringList nameParts) {
		boolean fileNameUnique = false;
		int count = 0;
		do {
			File file = new File(nameParts.concatenate("."));
			if (file.exists())
			{
				if (count == 0) {
					nameParts.insert(Integer.toString(count), nameParts.count()-1);
				}
				else {
					nameParts.set(nameParts.count()-2, Integer.toString(count));
				}
				count++;
			}
			else
			{
				fileNameUnique = true;
			}
		} while (!fileNameUnique);
	}
	
	/**
	 * Split the given file path at all dots and ensure a file name extension
	 * {@code newExt} (as last name part of the resulting {@link StringList}.
	 * If {@code filename} had ended with one of the old file extensions given
	 * in {@code oldFileExts} then this extension will be eliminated before. 
	 * @param filename - the file path or name to be prepared.
	 * @param oldExts - a list of unwanted file name extensions (without dots!)
	 * @param newExt - the new file name extension to be ensured (without dot!)
	 * @return the split file name (should be concatenated with "." separator).
	 * Last part will always be {@code newExt}.
	 */
	private static StringList ensureFileExtension(String filename, StringList oldExts, String newExt) {
		StringList nameParts = StringList.explode(filename, "[.]");
		String ext = nameParts.get(nameParts.count()-1).toLowerCase();
		// START KGU#687 2019-03-26: Extended to an array of file extensions
		//if (ext.equals(fileExt))
		if (oldExts.contains(ext))
		// END KGU#687 2019-03-26
		{
			// Replace the given source code file name extension by the extension newExt
			nameParts.set(nameParts.count()-1, newExt);
		}
		else if (!ext.equals(newExt))
		{
			// Otherwise append extension newExt if this hasn't already been the extension
			nameParts.add(newExt);
		}
		return nameParts;
	}
	// END KGU#678 2019-03-26

	/**
	 * Chooses the parser to be used among the {@code suitedParsers}. If there are many
	 * then the user will be asked interactively.<br/>
	 * (Later there might be a change to get it from a configuration file.)
	 * @param suitedParsers - a vector of parsers accepting the file extension
	 * @param filename - name of the file to be parsed (for dialog purposes)
	 * @param scnr - the {@link Scanner} instance to be used for input
	 * @return a {@link CodeParser} instance if there was a valid choice or null 
	 */
	private static CodeParser disambiguateParser(Vector<CodeParser> suitedParsers, String filename, Scanner scnr)
	{
		CodeParser parser = null;
		if (suitedParsers.size() == 1) {
			parser = suitedParsers.get(0);
		}
		else if (suitedParsers.size() > 1) {
			System.out.println("Several suited parsers found for file \"" + filename + "\":");
			for (int i = 0; i < suitedParsers.size(); i++) {
				System.out.println((i+1) + ": " + suitedParsers.get(i).getDialogTitle());
			}
			int chosen = -1;
			// START KGU#696 2019-03-26: Bugfix #715 We used the wrong method - didn't wait for user input
			//Scanner scnr = new Scanner(System.in);
			// END KGU#696 2019-03-26
			try {
				while (chosen < 0) {
					System.out.print("Please select the number of your favourite (0 = skip file): ");
					String input = scnr.nextLine();
					try {
						chosen = Integer.parseInt(input);
						if (chosen < 0 || chosen > suitedParsers.size()) {
							System.err.println("*** Value out of range!");
							chosen = -1;
						}
					}
					catch (Exception ex) {
						System.err.println("*** Wrong number format!");
					}
				}
			}
			// START KGU#696 2019-03-26: Bugfix #715 We must not close input stream here but catch errors
			//finally {
			//	scnr.close();
			//}
			catch (Exception ex) {
				ex.printStackTrace();
				chosen = 1;
				System.err.println("*** Option 1 set.");				
			}
			// END KGU#696 2019-03-26
			if (chosen != 0) {
				parser = suitedParsers.get(chosen-1);
			}
		}
		return parser;
	}
	// END KGU#538 2018-07-01
	
	// START KGU#416 2017-07-03: Enh. #354, #409
	private static CodeParser cloneWithPluginOptions(GENPlugin plugin, String _settingsFile) {
		CodeParser parser;
		try {
			// START KGU#538 2018-07-01: Bugfix #554 Instantiation failed (missing path)
			//parser = (CodeParser)Class.forName(plugin.getKey()).newInstance();
			parser = (CodeParser)Class.forName(plugin.className).getDeclaredConstructor().newInstance();
			// END KGU#538 2018-07-01
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			System.err.println("Structorizer.CloneWithPluginSpecificOptions("
					+ plugin.getKey()
					+ "): " + ex.toString() + " on creating \"" + plugin.getKey()
					+ "\"");
			return null;
		}
		Ini ini = Ini.getInstance();
		if (!plugin.options.isEmpty()) {
			System.out.println("Retrieved parser-specific options:");
			try {
				// START KGU#538 2018-07-01: Issue #554 - a different properties file might be requested
				//ini.load();
				if (_settingsFile != null && (new File(_settingsFile)).canRead()) {
					ini.load(_settingsFile);
				}
				else {
					ini.load();
				}
				// END KGU#538 2018-07-01
				// START KGU#977 2021-06-08: Delegated to the IPluginClass (on occasion of enh. #953)
				StringList problems = parser.setPluginOptionsFromIni(plugin.options);
				for (int i = 0; i < problems.count(); i++) {
					System.err.println(problems.get(i));
				}
				for (HashMap<String, String> option: plugin.options) {
					String optionName = option.get("name");
					Object optionValue = parser.getPluginOption(optionName, null);
					if (optionValue != null) {
						System.out.println(" + " + optionName + " = " + optionValue);
					}
				}
				System.out.println();
				// END KGU#977 2021-06-08
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// START KGU#977 2021-06-08: Delegated to the IPluginClass (on occasion of enh. #953)
//		for (HashMap<String, String> optionSpec: plugin.options) {
//			String optionKey = optionSpec.get("name");
//			String valueStr = ini.getProperty(plugin.getKey() + "." + optionKey, "");
//			Object value = null;
//			String type = optionSpec.get("type");
//			String items = optionSpec.get("items");
//			// Now convert the option into the specified type
//			if (!valueStr.isEmpty() && type != null || items != null) {
//				// Better we fail with just a single option than with the entire method
//				try {
//					if (items != null) {
//						value = valueStr;
//					}
//					else if (type.equalsIgnoreCase("character")) {
//						value = valueStr.charAt(0);
//					}
//					else if (type.equalsIgnoreCase("boolean")) {
//						value = Boolean.parseBoolean(valueStr);
//					}
//					else if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer")) {
//						value = Integer.parseInt(valueStr);
//					}
//					else if (type.equalsIgnoreCase("unsiged")) {
//						value = Integer.parseUnsignedInt(valueStr);
//					}
//					else if (type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")) {
//						value = Double.parseDouble(valueStr);
//					}
//					else if (type.equalsIgnoreCase("string")) {
//						value = valueStr;
//					}
//				}
//				catch (NumberFormatException ex) {
//					System.err.println("*** Structorizer.CloneWithPluginSpecificOptions("
//							+ plugin.getKey()
//							+ "): " + ex.getMessage() + " on converting \""
//							+ valueStr + "\" to " + type + " for " + optionKey);
//				}
//			}
//			System.out.println(" + " + optionKey + " = " + value);
//			if (value != null) {
//				parser.setPluginOption(optionKey, value);
//			}
//		}
//		if (!plugin.options.isEmpty()) {
//			System.out.println("");
//		}
		// END KGU#977 2021-06-08
		return parser;
	}
	// END KGU#416 2017-07-02

	// START KGU#187 2016-05-02: Enh. #179 - help might be sensible
	private static void printHelp()
	{
		System.out.print("Usage:\n");
		for (int i = 0; i < synopsis.length; i++)
		{
			System.out.println(synopsis[i]);
		}
		System.out.println("with");
		System.out.print("\tGENERATOR = ");
		// We just (ab)use some class residing in package gui to fetch the plugin configuration 
		BufferedInputStream buff = new BufferedInputStream(lu.fisch.structorizer.gui.EditData.class.getResourceAsStream("generators.xml"));
		GENParser genp = new GENParser();
		Vector<GENPlugin> plugins = genp.parse(buff);
		try { buff.close();	} catch (IOException e) {}
		for (int i=0; i < plugins.size(); i++)
		{
			GENPlugin plugin = (GENPlugin) plugins.get(i);
			StringList names = StringList.explode(plugin.title, "/");
			String className = plugin.getKey();
			System.out.print( (i>0 ? " |" : "") + "\n\t\t" + className );
			for (int j = 0; j < names.count(); j++)
			{
				System.out.print(" | " + names.get(j).trim());
			}
		}
		System.out.println("\n\tARRSPEC = (ARRFILE|ARRZFILE)!SIGNATURE...");
		System.out.print("\n\tPARSER = ");
		// Again we (ab)use some class residing in package gui to fetch the plugin configuration 
		buff = new BufferedInputStream(lu.fisch.structorizer.gui.EditData.class.getResourceAsStream("parsers.xml"));
		genp = new GENParser();
		plugins = genp.parse(buff);
		try { buff.close();	} catch (IOException e) {}
		for (int i=0; i < plugins.size(); i++)
		{
			GENPlugin plugin = (GENPlugin) plugins.get(i);
			StringList names = StringList.explode(plugin.title, "/");
			String className = plugin.getKey();
			System.out.print( (i>0 ? " |" : "") + "\n\t\t" + className );
			for (int j = 0; j < names.count(); j++)
			{
				System.out.print(" | " + names.get(j).trim());
			}
		}
		System.out.println("");
	}
	// END KGU#187 2016-05-02
	
	/** @return the installation path of Structorizer (for webstart test) */
	public static String getApplicationPath()
	{
		// FIXME Can all this be replaced by Ini.getInstallDirectory().getParent(); ?
		CodeSource codeSource = Structorizer.class.getProtectionDomain().getCodeSource();
		File rootPath = null;
		try {
			rootPath = new File(codeSource.getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return "";
		}
		return rootPath.getParentFile().getPath();
	}
		
// START KGU#595 2018-10-07: Bugfix #620 - we must adapt the log pattern
//	/**
//	 * Performs a bytewise copy of {@code sourceStrm} to {@code targetFile}. Closes
//	 * {@code sourceStrm} afterwards.
//	 * @param sourceStrm - opened source {@link InputStream}
//	 * @param targetFile - {@link File} object for the copy target
//	 * @return a string describing occurred errors or an empty string.
//	 */
//	private static String copyStream(InputStream sourceStrm, File targetFile) {
//		String problems = "";
//		final int BLOCKSIZE = 512;
//		byte[] buffer = new byte[BLOCKSIZE];
//		FileOutputStream fos = null;
//		try {
//			fos = new FileOutputStream(targetFile.getAbsolutePath());
//			int readBytes = 0;
//			do {
//				readBytes = sourceStrm.read(buffer);
//				if (readBytes > 0) {
//					fos.write(buffer, 0, readBytes);
//				}
//			} while (readBytes > 0);
//		} catch (FileNotFoundException e) {
//			problems += e + "\n";
//		} catch (IOException e) {
//			problems += e + "\n";
//		}
//		finally {
//			if (fos != null) {
//				try {
//					fos.close();
//				} catch (IOException e) {}
//			}
//			try {
//				sourceStrm.close();
//			} catch (IOException e) {}
//		}
//		return problems;
//	}
	
	/**
	 * Performs a linewise filtered copy of {@code sourceStrm} to {@code targetFile}. Closes
	 * {@code sourceStrm} afterwards.
	 * @param sourceStrm - opened source {@link InputStream}
	 * @param targetFile - {@link File} object for the copy target
	 * @return a string describing occurred errors or an empty string.
	 */
	private static String copyLogProperties(InputStream configStr, File configFile) {
		
		final String pattern = "java.util.logging.FileHandler.pattern = ";
		String problems = "";
		File configDir = configFile.getParentFile();
		File homeDir = new File(System.getProperty("user.home"));
		StringList pathParts = new StringList();
		while (configDir != null && homeDir != null) {
			if (homeDir.equals(configDir)) {
				pathParts.add("%h");
				homeDir = null;
			}
			else if (configDir.getName().isEmpty()) {
				pathParts.add(configDir.toString().replace(":\\", ":"));
			}
			else {
				pathParts.add(configDir.getName());
			}
			configDir = configDir.getParentFile();
		}
		String logPath = pathParts.reverse().concatenate("/") + "/structorizer%u.log";
		DataInputStream in = new DataInputStream(configStr);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(configFile);
			DataOutputStream out = new DataOutputStream(fos);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					if (line.startsWith(pattern)) {
						line = pattern + logPath;
					}
					bw.write(line + "\n");
				}
			} catch (IOException e) {
				problems += e + "\n";
				e.printStackTrace();
			}
			finally {
				if (fos != null) {
					bw.close();
				}
			}
		} catch (IOException e1) {
			problems += e1 + "\n";
			e1.printStackTrace();
		}
		finally {
			try {
				br.close();
			} catch (IOException e) {}
		}
		return problems;
	}
// END KGU#595 2018-10-07


}

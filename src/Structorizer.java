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
 *      Author          Date			Description
 *      ------          ----			-----------
 *      Bob Fisch       2007.12.27		First Issue
 *      Kay Gürtzig     2015.12.16      Bugfix #63 - no open attempt without need
 *      Kay Gürtzig     2016.04.28      First draft for enh. #179 - batch generator mode (KGU#187)
 *      Kay Gürtzig     2016.05.03      Prototype for enh. #179 - incl. batch parser and help (KGU#187)
 *      Kay Gürtzig     2016.05.08      Issue #185: Capability of multi-routine import per file (KGU#194)
 *      Kay Gürtzig     2016.12.02      Enh. #300: Information about updates on start in interactive mode
 *                                      Modification in command-line concatenation
 *      Kay Gürtzig     2016.12.12      Issue #306: multiple arguments in simple command line are now
 *                                      interpreted as several files to be opened in series.
 *      Kay Gürtzig     2017.01.27      Issue #306 + #290: Support for Arranger files in command line
 *      Kay Gürtzig     2017.03.04      Enh. #354: Configurable set of import parsers supported now
 *      Kay Gürtzig     2017.04.27      Enh. #354: Verbose option (-v with log directory) for batch import
 *
 ******************************************************************************************************
 *
 *      Comment:		
 *      
 ******************************************************************************************************///


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.UIManager;

import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.generators.Generator;
import lu.fisch.structorizer.generators.XmlGenerator;
import lu.fisch.structorizer.gui.Mainform;
import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.structorizer.parsers.GENParser;
import lu.fisch.structorizer.parsers.NSDParser;
import lu.fisch.utils.StringList;

public class Structorizer
{

	// entry point
	public static void main(String args[])
	{
		// START KGU#187 2016-04-28: Enh. #179
		Vector<String> fileNames = new Vector<String>();
		String generator = null;
		String parser = null;
		String options = "";
		String outFileName = null;
		String charSet = "UTF-8";
		// START KGU#354 2017-04-27:Enh. #354
		String logDir = null;
		// END KGU#354 2017-04-27
		//System.out.println("arg 0: " + args[0]);
		if (args.length == 1 && args[0].equals("-h"))
		{
			printHelp();
			return;
		}
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
			}
			// Legacy support - parsers will now be derived from the file extensions 
			else if (i > 0 && (parser != null) && (args[i].equalsIgnoreCase("pas") || args[i].equalsIgnoreCase("pascal"))
					&& !parser.endsWith("pas")) {
				parser += "pas";
			}
			else if (args[i].equals("-o") && i+1 < args.length)
			{
				outFileName = args[++i];
			}
			else if (args[i].equals("-e") && i+1 < args.length)
			{
				charSet = args[++i];
			}
			// START KGU#354 2017-04-27: Enh. #354 verbose mode?
			else if (args[i].equals("-v") && i+1 < args.length)
			{
				logDir = args[++i];
			}
			// END KGU#354 2017-04-27
			// Target standard output?
			else if (args[i].equals("-"))
			{
				options += "-";
			}
			// Other options
			else if (args[i].startsWith("-"))
			{
				options += args[i].substring(1);
			}
			else
			{
				fileNames.add(args[i]);
			}
		}
		if (generator != null)
		{
			Structorizer.export(generator, fileNames, outFileName, options, charSet);
			return;
		}
		else if (parser != null)
		{
			// START KGU#354 2017-04-27: Enh. #354 verbose mode
			//Structorizer.parse(parser, fileNames, outFileName, options, charSet);
			Structorizer.parse(parser, fileNames, outFileName, options, charSet, logDir);
			// END KGU#354 2017-04-27
			return;
		}
		// END KGU#187 2016-04-28
		
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

		try
		{
			//String s = new String();
			int start = 0;
			if (args.length > 0 && args[0].equals("-open")) {
				start = 1;
			}
			// FIXME (KGU 2016-12-12): This concatenation still doesn't make sense...
			// (If the file name contained blanks then the OS should have quoted it,
			// if the command line contained several file names, on the other hand, then
			// they would have to be loaded separately - this could be done by moving
			// the previously loaded one to the Arranger on each consecutive load.)
			for (int i=start; i<args.length; i++)
			{
				// START KGU#306 2016-12-12/2017-01-27: This seemed to address file names with blanks...
				//s += args[i];
				String s = args[i].trim();
				String lastExt = "";	// Last file extension
				if (!s.isEmpty())
				{
					if (lastExt.equals("nsd") && !mainform.diagram.getRoot().isEmpty()) {
						// Push the previously loaded diagram to Arranger
						mainform.diagram.arrangeNSD();
					}
					lastExt = mainform.diagram.openNsdOrArr(s);
				}
				// END KGU#306 2016-12-12/2017-01-27
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
			mainform.diagram.redraw();
		}
		catch (Exception e)
		{
		}


		if(System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
		{

			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("apple.awt.graphics.UseQuartz", "true");

			com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();

			try
			{
				application.setEnabledPreferencesMenu(true);
				application.addApplicationListener(new com.apple.eawt.ApplicationAdapter() {
					public void handleAbout(com.apple.eawt.ApplicationEvent e) {
						mainform.diagram.aboutNSD();
						e.setHandled(true);
					}
					public void handleOpenApplication(com.apple.eawt.ApplicationEvent e) {
					}
					public void handleOpenFile(com.apple.eawt.ApplicationEvent e) {
						if(e.getFilename()!=null)
						{
							mainform.diagram.openNSD(e.getFilename());
						}
					}
					public void handlePreferences(com.apple.eawt.ApplicationEvent e) {
						mainform.diagram.preferencesNSD();
					}
					public void handlePrintFile(com.apple.eawt.ApplicationEvent e) {
						mainform.diagram.printNSD();
					}
					public void handleQuit(com.apple.eawt.ApplicationEvent e) {
						mainform.saveToINI();
						mainform.dispose();
					}
				});
			}
			catch (Exception e)
			{
			}
		}/**/

		// START KGU#300 2016-12-02
		mainform.notifyNewerVersion();
		// END KGU#300 2016-12-02
	}
	
	// START KGU#187 2016-05-02: Enh. #179
	private static String[] synopsis = {
		"Structorizer [NSDFILE]",
		"Structorizer -x GENERATOR [-b] [-c] [-f] [-l] [-t] [-e CHARSET] [-] [-o OUTFILE] NSDFILE...",
		"Structorizer -p [pas|pascal] [-f] [-v LOGPATH] [-e CHARSET] [-o OUTFILE] SOURCEFILE...",
		"Structorizer -h"
	};
	// END KGU#187 2016-05-02
	
	// START KGU#187 2016-04-28: Enh. #179
	/*****************************************
	 * batch code export methods
	 *****************************************/
	public static void export(String _generatorName, Vector<String> _nsdFileNames, String _codeFileName, String _options, String _charSet)
	{
		Vector<Root> roots = new Vector<Root>();
		for (String fName : _nsdFileNames)
		{
			Root root = null;
			try
			{
				// Test the existence of the current NSD file
				File f = new File(fName);
				if (f.exists())
				{
					// open an existing file
					NSDParser parser = new NSDParser();
					root = parser.parse(f.toURI().toString());
					root.filename = fName;
					roots.add(root);
					// If no output file name is given then derive one from the first NSD file
					if (_codeFileName == null && _options.indexOf('-') < 0)
					{
						_codeFileName = f.getCanonicalPath();
					}
				}
				else
				{
					System.err.println("*** File " + fName + " not found. Skipped.");
				}
			}
			catch (Exception e)
			{
				System.err.println("*** Error while trying to load " + fName + ": " + e.getMessage());
			}
		}
		
		String genClassName = null;
		if (!roots.isEmpty())
		{
			String usage = "Usage: " + synopsis[1] + "\nwith GENERATOR =";
			// We just (ab)use some class residing in package gui to fetch the plugin configuration 
			BufferedInputStream buff = new BufferedInputStream(lu.fisch.structorizer.gui.EditData.class.getResourceAsStream("generators.xml"));
			GENParser genp = new GENParser();
			Vector<GENPlugin> plugins = genp.parse(buff);
			for (int i=0; genClassName == null && i < plugins.size(); i++)
			{
				GENPlugin plugin = (GENPlugin) plugins.get(i);
				StringList names = StringList.explode(plugin.title, "/");
				String className = plugin.className.substring(plugin.className.lastIndexOf(".")+1);
				usage += (i>0 ? " |" : "") + "\n\t" + className;
				if (className.equalsIgnoreCase(_generatorName))
				{
					genClassName = plugin.className;
				}
				else
				{
					for (int j = 0; j < names.count(); j++)
					{
						if (names.get(j).trim().equalsIgnoreCase(_generatorName))
							genClassName = plugin.className;
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
				Generator gen = (Generator) genClass.newInstance();
				gen.exportCode(roots, _codeFileName, _options, _charSet);
			}
			catch(java.lang.ClassNotFoundException ex)
			{
				System.err.println("*** Generator class " + ex.getMessage() + " not found!");
				System.exit(3);
			}
			catch(Exception e)
			{
				System.err.println("*** Error while using " + _generatorName + "\n" + e.getMessage());
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
	
	// START KGU#187 2016-04-29: Enh. #179 - for symmetry reasons also allow a parsing in batch mode
	/*****************************************
	 * batch code import methods
	 * @param _logDir TODO
	 *****************************************/
	private static void parse(String _parserName, Vector<String> _filenames, String _outFile, String _options, String _charSet, String _logDir)
	{
		
		String usage = "Usage: " + synopsis[2] + "\nAccepted file extensions:";

		String fileExt = null;
		// START KGU#354 2017-03-10: Enh. #354 configurable parser plugins
		// Initialize the mapping file extensions -> CodeParser
		// We just (ab)use some class residing in package gui to fetch the plugin configuration 
		BufferedInputStream buff = new BufferedInputStream(lu.fisch.structorizer.gui.EditData.class.getResourceAsStream("parsers.xml"));
		GENParser genp = new GENParser();
		Vector<GENPlugin> plugins = genp.parse(buff);
		Vector<CodeParser> parsers = new Vector<CodeParser>();
		//String parsClassName = null;
		CodeParser parser = null;
		for (int i=0; i < plugins.size(); i++)
		{
			GENPlugin plugin = (GENPlugin) plugins.get(i);
			String className = plugin.className;
			try
			{
				Class<?> parsClass = Class.forName(className);
				parser = (CodeParser) parsClass.newInstance();
				parsers.add(parser);
				usage += "\n\t";
				for (String ext: parser.getFileExtensions()) {
					usage += ext + ", ";
				}
				// Get rid of last ", " 
				if (usage.endsWith(", ")) {
					usage = usage.substring(0, usage.length()-2) + " for " + parser.getDialogTitle();
				}
			}
			catch(java.lang.ClassNotFoundException ex)
			{
				System.err.println("*** Parser class " + ex.getMessage() + " not found!");
			}
			catch(Exception e)
			{
				System.err.println("*** Error on creating " + _parserName + "\n" + e.getMessage());
				e.printStackTrace();
			}
		}

		// END KGU#354 2017-03-10
		
		// START KGU#193 2016-05-09: Output file name specification was ignored, option f had to be tuned.
		boolean overwrite = _options.indexOf("f") >= 0 && 
				!(_outFile != null && !_outFile.isEmpty() && _filenames.size() > 1);
		// END KGU#193 2016-05-09
		
		// START KGU#354 2017-03-03: Enh. #354 - support several parser plugins
		// While there was only one input language candidate, a single Parser instance had been enough
		//D7Parser d7 = new D7Parser("D7Grammar.cgt");
		// END KGU#354 2017-03-04
		
		for (String filename : _filenames)
		{
			// START KGU#194 2016-05-08: Bugfix #185 - face more contained roots
			//Root rootNew = null;
			List<Root> newRoots = new LinkedList<Root>();
			// END KGU#194 2016-05-08
			// START KGU#354 2017-03-04: Enh. #354
			//if (fileExt.equals("pas"))
			File importFile = new File(filename);
			parser = null;
			for (int p = 0; parser == null && p < parsers.size(); p++) {
				if (parsers.get(p).accept(importFile)) {
					parser = parsers.get(p);
				}
			}
			if (parser != null)
			// END KGU#354 2017-03-09
			{
				//D7Parser d7 = new D7Parser("D7Grammar.cgt");
				// START KGU#194 2016-05-04: Bugfix for 3.24-11 - encoding wasn't passed
				//rootNew = d7.parse(filename);
				// START KGU#354 2017-04-27: Enh. #354 pass in the log directory path
				//newRoots = parser.parse(filename, _charSet);
				newRoots = parser.parse(filename, _charSet, _logDir);
				// END KGU#354 2017-04-27
				// END KGU#194 2016-05-04
				if (!parser.error.isEmpty())
				{
					System.err.println("Parser error in file " + filename + "\n" + parser.error);
					continue;
				}
			}
			else {
				System.err.println("File " + filename + " not accpeted by any parser!");
			}
		
			// Now save the roots as NSD files. Derive the target file names from the source file name
			// if _outFile isn't given.
			// START KGU#193 2016-05-09: Output file name specification was ignred, optio f had to be tuned.
			if (_outFile != null && !_outFile.isEmpty())
			{
				filename = _outFile;
			}
			// END KGU#193 2016-05-09
			// START KGU#194 2016-05-08: Bugfix #185 - face more contained roots
			//if (rootNew != null)
			boolean multipleRoots = newRoots.size() > 1;
			for (Root rootNew : newRoots)
			// END KGU#194 2016-05-08
			{
				StringList nameParts = StringList.explode(filename, "[.]");
				String ext = nameParts.get(nameParts.count()-1).toLowerCase();
				if (ext.equals(fileExt))
				{
					nameParts.set(nameParts.count()-1, "nsd");
				}
				else if (!ext.equals("nsd"))
				{
					nameParts.add("nsd");
				}
				// In case of multiple roots (subroutines) insert the routine's proposed file name
				if (multipleRoots && !rootNew.isProgram())
				{
					nameParts.insert(rootNew.proposeFileName(), nameParts.count()-1);
				}
				//System.out.println("File name raw: " + nameParts);
				if (!overwrite)
				{
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
							overwrite = true;
						}
					} while (!overwrite);
				}
				String filenameToUse = nameParts.concatenate(".");
				//System.out.println("Writing to " + filename);
				try {
					FileOutputStream fos = new FileOutputStream(filenameToUse);
					Writer out = null;
					out = new OutputStreamWriter(fos, "UTF8");
					XmlGenerator xmlgen = new XmlGenerator();
					out.write(xmlgen.generateCode(rootNew,"\t"));
					out.close();
				}
				catch (UnsupportedEncodingException e) {
					System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
				catch (IOException e) {
					System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}
	}
	// END KGU#187 2016-04-29
	
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
		for (int i=0; i < plugins.size(); i++)
		{
			GENPlugin plugin = (GENPlugin) plugins.get(i);
			StringList names = StringList.explode(plugin.title, "/");
			String className = plugin.className.substring(plugin.className.lastIndexOf(".")+1);
			System.out.print( (i>0 ? " |" : "") + "\n\t\t" + className );
			for (int j = 0; j < names.count(); j++)
			{
				System.out.print(" | " + names.get(j).trim());
			}
		}
		System.out.println("\n\tPARSER = ");
		String[] names = {"pas", "pascal"};
		for (int j = 0; j < names.length; j++)
		{
			System.out.print((j > 0 ? " | " : "\t\t") + names[j]);
		}
		System.out.println("");
	}
	// END KGU#187 2016-05-02
		
}

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
package lu.fisch.structorizer.parsers;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Abstract Parser class for all code import (except Pascal/Delphi).
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.03.04      First Issue
 *      Kay Gürtzig     2017.03.25      Fix #357: Precaution against failed file preparation
 *      Kay Gürtzig     2017.03.30      Standard colours for declarations, constant definitions and global stuff
 *      Kay Gürtzig     2017.04.11      Mechanism to revert file preparator replacements in the syntax error display
 *      Kay Gürtzig     2017.04.16      New hook method postProcess(String textToParse) for sub classes
 *      Kay Gürtzig     2017.04.27      File logging mechanism added (former debug prints) 
 *      Kay Gürtzig     2017.05.22      Enh. #372: Generic support for "origin" attribute
 *      Simon Sobisch   2017.05.23      Hard line break in the parser error context display introduced 
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *
 ******************************************************************************************************///

import java.awt.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import com.creativewidgetworks.goldparser.engine.ParserException;
import com.creativewidgetworks.goldparser.engine.Position;
import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Symbol;
import com.creativewidgetworks.goldparser.engine.SymbolList;
import com.creativewidgetworks.goldparser.engine.Token;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.utils.StringList;


/**
 * Abstract base class for all code importing classes using the GOLDParser to
 * parse the source code based on a compiled grammar.
 * A compiled grammar file (version 1.0, with extension cgt) given, the respectie
 * subclass can be generated with the GOLDprog.exe tool using ParserTemplate.pgt
 * as template file, e.g.:
 * {@code GOLDprog.exe Ada.cgt ParserTemplate.pgt AdaParser.java} 
 * The generated subclass woud be able to parse code but must manually be accomplished
 * in order to build a structogram from the obtained parse tree. Override the methods
 * {@link #buildNSD_R(Reduction, Subqueue)} and {@link #getContent_R(Reduction, String)}
 * for that purpose.
 * This is where the
 * real challenge is lurking...
 * @author Kay Gürtzig
 */
public abstract class CodeParser extends javax.swing.filechooser.FileFilter
{
	/************ Common fields *************/
	
	/**
	 * String field holding the message of error occurred during parsing or build phase
	 * for later evaluation (empty if there was no error) 
	 */
	public String error;
	// Used width for displaying the error in a dialog
	final int DLG_STR_WIDTH = 100;

	/**
	 * The generic LALR(1) parser providing the parse tree
	 */
	protected AuParser parser;
	/**
	 *  Currently built diagram Root
	 */
	protected Root root = null;
	/**
	 * List of the Roots of (all) imported diagrams - we may obtain a collection
	 * of Roots (unit or program with subroutines)!
	 */
	protected List<Root> subRoots = new LinkedList<Root>();
	// START KGU#358 2017-03-06: Enh. #354, #368 - new import options
	/**
	 * Value of the import option to import mere variable declarations
	 * @see #optionSaveParseTree()
	 */
	protected boolean optionImportVarDecl = false;
	/**
	 * Returns the value of the import option to save the obtained parse tree
	 * @return true iff the parse tree is to be saved as text file
	 * @see #optionImportVarDecl
	 */
	protected boolean optionSaveParseTree()
	{
		return Ini.getInstance().getProperty("impSaveParseTree", "false").equals("true");
	}
	// END KGU#358 2017-03-06

	// START KGU#395 2017-05-26: Enh. #357 - parser-specific options
	private final HashMap<String, Object> optionMap = new HashMap<String, Object>();

	/**
	 * Returns a Generator-specific option value if available (otherwise null)
	 * @param _optionName - option key, to be combined with the parser class name
	 * @param _defaultValue TODO
	 * @return an Object (of the type specified in the plugin) or null
	 */
	protected Object getPluginOption(String _optionName, Object _defaultValue) {
		Object optionVal = _defaultValue;
		String fullKey = this.getClass().getSimpleName()+"."+_optionName;
		if (this.optionMap.containsKey(fullKey)) {
			optionVal = this.optionMap.get(fullKey);
		}
		return optionVal;
	}

	/**
	 * Allows to set a plugin-specified option before the code parsing starts
	 * @param _optionName - a key string
	 * @param _value - an object according to the type specified in the plugin
	 */
	public void setPluginOption(String _optionName, Object _value)
	{
		String fullKey = this.getClass().getSimpleName()+"."+_optionName;
		this.optionMap.put(fullKey, _value);
	}
	// END KGU#395 2017-05-26
	
	// START KGU#354 2017-04-27
	/**
	 * An open log file for verbose parsing and building if not null
	 * @see #log(String, boolean)
	 */
	private OutputStreamWriter logFile = null;
	// END KGU#354 2017-04-27
	
	/**
	 * Standard element colour for imported constant definitions
	 * @see #colorDecl
	 * @see #colorGlobal
	 * @see #colorMisc
	 */
	protected static final Color colorConst = Color.decode("0xFFE0FF");
	/**
	 * Standard element colour for imported variable declarations (without initialization)
	 * @see #colorConst
	 * @see #colorGlobal
	 * @see #colorMisc
	 */
	protected static final Color colorDecl = Color.decode("0xE0FFE0");
	/**
	 * Standard element colour for imported global declarations or definitions
	 * @see #colorConst
	 * @see #colorDecl
	 * @see #colorMisc
	 */
	protected static final Color colorGlobal = Color.decode("0xE0FFFF");
	/**
	 * Standard element colour for miscellaneous mark-ups
	 * @see #colorConst
	 * @see #colorDecl
	 * @see #colorGlobal
	 */
	protected static final Color colorMisc = Color.decode("0xFFFFE0");
	
	// START KGU 2017-04-11
	/**
	 * Identifier replacement map to be filled by the file preparation method if identifiers
	 * had to be replaced by other symbols or generic identifiers in order to allow the source
	 * file to pass the parsing.
	 * Must map the substitutes to the original identifiers such that the replacements may be
	 * reverted on error display.
	 * @see #prepareTextfile(String, String)   
	 */
	protected HashMap<String, String> replacedIds = new HashMap<String, String>();
	// END KGU 2017-04-11

	/************ Abstract Methods *************/
	
	/**
	 * Is to provide the file name of the compiled grammar the parser class is made for
	 * @return a grammar file name retrievable as resource (a cgt or egt file).
	 */
	protected abstract String getCompiledGrammar();
	
	/**
	 * Is to return the internal name of the grammar table as given in the grammar file
	 * parameters
	 * @return Name string as specified inthe grammar file header
	 */
	protected abstract String getGrammarTableName();
	
	/**
	 * Is to return a replacement for the FileChooser title. Ideally its just
	 * the name of the source language imported by this parser.
	 * @return Source language name to be inserted in the file open dialog title. 
	 */
	public abstract String getDialogTitle();
	
	/**
	 * Is to return a short description of the source file type, ideally in English.
	 * @see #getFileExtensions()
	 * @return File type description, e.g. "Ada source files"
	 */
	protected abstract String getFileDescription();
	
	/**
	 * Return a string array with file name extensions to be recognized and accepted
	 * as source files of the input language of this parser.<br>
	 * The extensions must not start with a dot!
	 * Correct: { "cpp", "cc" }, WRONG: { ".cpp", ".cc" } 
	 * @see #getFileDescription()
	 * @return the array of associated file name extensions
	 */
	public abstract String[] getFileExtensions();
	
	/**
	 * Parses the ANSI-C source code from file _textToParse, which is supposed to be encoded
	 * with the charset _encoding, and returns a list of structograms - one for each function
	 * or program contained in the source file.
	 * Field `error' will either contain an empty string or an error message afterwards.
	 * @param _textToParse - file name of the C source.
	 * @param _encoding - name of the charset to be used for decoding
	 * @param _logDir - null or a directory path to direct the parsing and building log to.
	 * @return A list containing composed diagrams (if successful, otherwise field error will contain an error description)
	 * @throws Exception 
	 * @see #prepareTextfile(String, String)
	 * @see #initializeBuildNSD()
	 * @see #buildNSD_R(Reduction, Subqueue)
	 * @see #getContent_R(Reduction, String)
	 * @see #subclassUpdateRoot(Root, String)
	 * @see #subclassPostProcess(String) 
	 */
	public List<Root> parse(String _textToParse, String _encoding, String _logDir)
	{
		if (_logDir != null) {
			File logDir = new File(_logDir);
			if (logDir.isDirectory()) {
				try {
					File log = new File(_textToParse);
					logFile = new OutputStreamWriter(new FileOutputStream(new File(logDir, log.getName() + ".log")), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		// AuParser is a Structorizer subclass of GOLDParser (Au = gold)
 		parser = new AuParser(
 				getClass().getResourceAsStream(getCompiledGrammar()),
 				getGrammarTableName(),
 				// START KGU#354 2017-04-27: Enh. #354
 				//true);
 				true,
 				logFile);
 				// END KGU#354 2017-04-27

		// Controls whether or not a parse tree is returned or the program executed.
 		parser.setGenerateTree(optionSaveParseTree());

		// create new root
		root = new Root();
		error = "";
		
		// START KGU#370 2017-03-25: Fix #357 - precaution against preparation failure
		//File intermediate = prepareTextfile(textToParse, _encoding);
		File intermediate = null;
		if (logFile != null) {
			try {
				logFile.write("STARTING FILE PREPARATION...\n\n");
			} catch (IOException e) {
			}
		}
		try {
			intermediate = prepareTextfile(_textToParse, _encoding);
		}
		catch (Exception ex) {
			if ((error = ex.getMessage()) == null) {
				error = ex.toString();
			}
			error = ":\n" + error;
		}
		if (intermediate == null) {
			error = "**FILE PREPARATION ERROR** on file \"" + _textToParse + "\"" + error;
			if (logFile != null) {
				try {
					logFile.close();
					logFile = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return subRoots;	// It doesn't make sense to continue here (BTW subRoots is supposed to be empty)
		}
		// END KGU#370 2017-03-25
		else if (logFile != null) {
			try {
				logFile.write("\nFILE PREPARATION COMPLETE -> \"" + intermediate.getAbsolutePath() + "\"\n\n");
			} catch (IOException e) {}
		}
		
		String sourceCode = null;
		
		boolean isSyntaxError = false;
		
        try {
			sourceCode = loadSourceFile(intermediate.getAbsolutePath(), _encoding);
            // Parse the source statements to see if it is syntactically correct
            boolean parsedWithoutError = parser.parseSourceStatements(sourceCode);

            // Holds the parse tree if setGenerateTree(true) was called
            //tree = parser.getParseTree();
            
            // Either execute the code or print any error message
            if (parsedWithoutError) {
				// ************************************** log file
				System.out.println("Parsing complete.");
				log("\nParsing complete.\n\n", false);
				// ************************************** end log
				if (this.optionSaveParseTree()) {
					try {
					String tree = parser.getParseTree();
					File treeLog = new File(_textToParse + ".parsetree.txt");
					String encTree = Ini.getInstance().getProperty("genExportCharset", "UTF-8");
					OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(treeLog), encTree);
					ow.write(tree);
					//System.out.println("==> "+filterNonAscii(pasCode.trim()+"\n"));
					ow.close();
					}
					catch (Exception ex) {
						System.err.println(ex.getMessage());
					}
				}
            	buildNSD(parser.getCurrentReduction());
            } else {
            	isSyntaxError = true;
                error = parser.getErrorMessage() + " in file \"" + _textToParse + "\"";
            }
        }
        catch (ParserException e) {
            error = "**PARSER ERROR** with file \"" + _textToParse + "\":\n" + e.getMessage();
            e.printStackTrace();
        }
		catch (IOException e1) {
            error = "**IO ERROR** on importing file \"" + _textToParse + "\":\n" + e1.getMessage();
			e1.printStackTrace();
		}
        catch (Exception e2) {
        	error = "**Severe error on importing file \"" + _textToParse + "\":\n" + e2.toString();
        	e2.printStackTrace();
        }

		// START KGU#191 2016-04-30: Issue #182 - In error case append the context 
		if (isSyntaxError && intermediate != null)
		{
			Position pos = parser.getCurrentPosition();
			error += "\n\nPreceding source context:";
			int lineNo = pos.getLine() - 1;
			int colNo = pos.getColumn() - 1;
			int start = (lineNo > 10) ? lineNo -10 : 0;
			StringList sourceLines = StringList.explode(sourceCode, "\n");
			// Unfortunately, the lineNo obtained from the parser is not correct, i.e.
			// it doesn't count empty lines. So there are two options for us here:
			// a) we remove all empty lines by sourceLines.removeAll(""); and may
			//    navigate quicker to the start but irritate the user
			// b) We loop over all entries not counting empty lines. This way, line
			//    numbering keeps half-way consistent with the user's counting.
			// We decide for a), since some preprocessor lines have been dropped
			// anyway.
			sourceLines.removeAll("");
			for (int i = start; i < lineNo; i++) {
				addLineToErrorString(i+1, undoIdReplacements(sourceLines.get(i).replace("\t", "    ")));
			}
			String line = sourceLines.get(lineNo);
			if (line.length() >= colNo) {
				line = undoIdReplacements(line.substring(0, colNo) + "» " + line.substring(colNo));
			}
//			if (line.length() < colNo && lineNo+1 < sourceLines.count()) {
//				error += String.format("\n%4d:   %s", lineNo+2, sourceLines.get(lineNo+1).replaceFirst("(^\\s*)(\\S.*)", "$1»$2").replace("\t", "    "));
//			}
			addLineToErrorString(lineNo+1, line.replace("\t", "    "));
			SymbolList sl = parser.getExpectedSymbols();
			Token token = parser.getCurrentToken();
			final String tokVal = token.toString();
			error += "\n\nFound token " + tokVal;
			String tokStr = token.asString().trim();
			// START KGU 2017-05-23: The token might be a generic surrogate for preprocessing, show the original id  
			tokStr = this.undoIdReplacements(tokStr);
			// END KGU 2017-05-23
			if (!tokVal.equals(tokStr) && !tokVal.equals("'" + tokStr + "'")) {
				error += " (" + tokStr + ")";
			}
			error += "\n\nExpected: ";
			String sepa = "";
			String exp = "";
			for (Symbol sym: sl) {
				exp += sepa + sym.toString();
				sepa = " | ";
				if (exp.length() > DLG_STR_WIDTH) {
					error += exp;
					exp = "";
					sepa = "\n        | ";
				}
			}
			error += exp;
			// ************************************** log file
			System.out.println("Parsing failed.");
			log("\n" + error + "\n\n", true);
			// ************************************** end log
		}
		// END KGU#191 2016-04-30

		// remove the temporary file
		intermediate.delete();
		
		// START KGU#194 2016-07-07: Enh. #185/#188 - Try to convert calls to Call elements
		StringList signatures = new StringList();
		for (Root subroutine : subRoots)
		{
			// START KGU#354 2017-03-10: Hook for subclass postprocessing
			this.subclassUpdateRoot(subroutine, _textToParse);
			// END KGU#354 2017-03-10
			if (subroutine.isSubroutine())
			{
				signatures.add(subroutine.getMethodName() + "#" + subroutine.getParameterNames().count());
			}
		}
		// END KGU#194 2016-07-07
		// START KGU#354 2017-03-10: Hook for subclass postprocessing
		if (!subRoots.contains(root)) {
			this.subclassUpdateRoot(root, _textToParse);
		}
		// END KGU#354 2017-03-10
		
		// START KGU#194 2016-05-08: Bugfix #185 - face an empty program or unit vessel
		//return root;
		if (subRoots.isEmpty() || root.children.getSize() > 0)
		{
			subRoots.add(0, root);
		}
		// START KGU#194 2016-07-07: Enh. #185/#188 - Try to convert calls to Call elements
		for (Root aRoot : subRoots)
		{
			aRoot.convertToCalls(signatures);
			// START KGU#363 2017-05-22: Enh. #372
			aRoot.origin += " / " + this.getClass().getSimpleName() + ": \"" + _textToParse + "\""; 
			// END KGU#363 2017-05-22
		}
		// END KGU#194 2016-07-07
		
		// Sub-classable postprocessing
		try {
			subclassPostProcess(_textToParse);
		}
		catch (Exception ex) {
			if ((error = ex.getMessage()) == null) {
				error = ex.toString();
			}
			error = ":\n" + error;
		}

		log("\nBUILD PHASE COMPLETE.\n", true);
		if (subRoots.size() >= 1 && subRoots.get(0).children.getSize() > 0) {
			log(subRoots.size() + " diagram(s) built.\n", true);
		}
		else {
			log("No diagrams built.\n", true);
		}
		
		if (logFile != null) {
			try {
				logFile.close();
				logFile = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return subRoots;
	}

	/**
	 * Adds a source line to the public error String, adds line breaks where necessary
	 * @param lineNum number of line for output
	 * @param content of source line, output is done with DLG_STR_WIDTH
	 */
	private void addLineToErrorString(int lineNum, String line) {
		error += String.format("\n%5d:   %."+DLG_STR_WIDTH+"s", lineNum, line);
		
		// simple approach to insert line break by width, better approach would be
		// breaking at word boundary
		final int strLen = line.length();
		for (int j = DLG_STR_WIDTH; j < strLen; j += DLG_STR_WIDTH) {
			error += String.format("\n%5s+   %."+DLG_STR_WIDTH+"s", "", line.substring(j));
		}
		
	}

	/**
	 * Writes the given _logContent to the current opened file if there is one.
	 * Oherwise and if {@code _toSystemOutInstead} is true the content will be
	 * written to the console output.
	 * @param _logContent - the message to be logged
	 * @param _toSystemOutInstead - whether the message is to be reported to System.out
	 */
	protected void log(String _logContent, boolean _toSystemOutInstead)
	{
		boolean done = false;
		if (logFile != null)
		try {
			logFile.write(_logContent);
			done = true;
		} catch (IOException e) {
			System.err.println(this.getClass().getSimpleName() + ".log(): " + e.toString());
		}
		if (!done && _toSystemOutInstead) {
			System.out.print(_logContent);
		}
	}

	// START KGU 2017-04-11
	/**
	 * Replaces all strings being keys in this.replacedIds by their respective mapped
	 * strings in the given line (i.e. actually tries to revert all performed substitutions).
	 * (Method is called in situations when the original text is required).
	 * @param line a source line or content string possibly with identifiers replaced by the file preparer
	 * @return line with reverted identifier substitutions
	 */
	protected String undoIdReplacements(String line) {
		for (Entry<String,String> entry: this.replacedIds.entrySet()) {
			String pattern = "(^|.*?\\W)" + entry.getKey() + "(\\W.*?|$)";
			if (line.matches(pattern)) {
				line = line.replaceAll(pattern, "$1" + Matcher.quoteReplacement(entry.getValue()) + "$2");
			}
		}
		return line;
	}
	// END KGU 2017-04-11

	/**
	 * Performs some necessary preprocessing for the text file. Must return a
	 * {@link java.io.File} object associated to a temporary (and possibly modified) copy of
	 * the file _textToParse. The copy is to be in a fix encoding.
	 * Typically opens the file, filters it and writes a new temporary file,
	 * which may then actually be parsed, to a suited directory.
	 * The preprocessed file will always be saved with UTF-8 encoding.
	 * @param _textToParse - name (path) of the source file
	 * @param _encoding - the expected encoding of the source file.
	 * @return A temporary {@link java.io.File} object for the created intermediate file, null
	 * if something went wrong.
	 * @see #replacedIds
	 */
	protected abstract File prepareTextfile(String _textToParse, String _encoding);
	
	/**
	 * Called after the build for every created Root and allows thus to do some 
	 * @param root
	 * @param sourceFileName
	 */
	protected abstract void subclassUpdateRoot(Root root, String sourceFileName);

	/**
	 * Allows subclasses to do some finishing work after all general stuff after parsing and
	 * NSD synthesis is practically done.
	 * @param textToParse - path of the parsed source file
	 */
	protected void subclassPostProcess(String textToParse)
	{
	}

	/******* FileFilter Extension *********/
	
	/**
	 * Internal check for acceptable input files. The default implementation just
	 * compares the filename extension with the extensions configured in and
	 * provided by {@link #getFileExtensions()}. Helper method for method 
	 * {@link #accept(File)}.
	 * @param _filename
	 * @return true if the import file is formally welcome. 
	 */
	protected final boolean isOK(String _filename)
	{
		boolean res = false;
		String ext = getExtension(_filename); 
		if (ext != null)
		{
			for (int i =0; i<getFileExtensions().length; i++)
			{
				res = res || (ext.equalsIgnoreCase(getFileExtensions()[i]));
			}
		}
		return res;
	}
	
	private static final String getExtension(String s) 
	{
		String ext = null;
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	private static final String getExtension(File f) 
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		
		return ext;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public final String getDescription() 
	{
        return getFileDescription();
    }
	
	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
    public final boolean accept(File f) 
	{
        if (f.isDirectory()) 
		{
            return true;
        }
		
        String extension = getExtension(f);
        if (extension != null) 
		{
            return isOK(f.getName());
		}
		
        return false;
    }
	
	
    /**
     * Load a source file to be interpreted by the engine.  
     * @param filename of a source file
     * @return source code to be interpreted
     * @throws IOException 
     */
    public String loadSourceFile(String filename, String encoding) throws IOException {
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(filename);
        byte[] buf = new byte[(int)file.length()];
        fis.read(buf);
        fis.close();
        return new String(buf);
    }
   
	/******* Diagram Synthesis *********/
	
	/**
	 * This is the entry point for the Nassi-Shneiderman diagram construction
	 * from the successfully established parse tree.
	 * Retrieves the import options, sets the initial diagram type (to program)
	 * and calls {@link #buildNSD_R(Reduction, Subqueue)}.
	 * NOTE: If your subclass needs to to some specific initialization then
	 * override {@link #initializeBuildNSD()}.
	 * @see #buildNSD_R(Reduction, Subqueue)
	 * @see #getContent_R(Reduction, String)
	 * @param _reduction the top Reduction of the parse tree.
	 */
	protected final void buildNSD(Reduction _reduction)
	{
		// START KGU#358 2017-03-06: Enh. #368 - consider import options!
		this.optionImportVarDecl = Ini.getInstance().getProperty("impVarDeclarations", "false").equals("true");
		// END KGU#358 2017-03-06
		root.setProgram(true);
		// Allow subclasses to adjust things before the recursive build process is going off.
		this.initializeBuildNSD();
		buildNSD_R(_reduction, root.children);
	}
	
	/**
	 * Recursively constructs the Nassi-Shneiderman diagram into the _parentNode
	 * from the given reduction subtree 
	 * @param _reduction - the current reduction subtree to be converted
	 * @param _parentNode - the Subqueue the emerging elements are to be added to.
	 */
	protected abstract void buildNSD_R(Reduction _reduction, Subqueue _parentNode);
	
	/**
	 * Composes the parsed non-terminal _reduction to a Structorizer-compatible
	 * terminal string, combines it with the given _content string and returns the
	 * result
	 * @param _reduction - a reduction sub-tree
	 * @param _content - partial translation result to be combined with the _reduction
	 * @return the combined translated string
	 */
	protected abstract String getContent_R(Reduction _reduction, String _content);
	
	/**
	 * Overridable method to do target-language-specific initialization before
	 * the recursive method {@link #buildNSD_R(Reduction, Subqueue)} will be called.
	 * Method is called in {@link #buildNSD(Reduction)}.
	 */
	protected void initializeBuildNSD()
	{
	}
	
	/************************
	 * static things
	 ************************/
	
	// START KGU#165 2016-03-25: Once and for all: It should be a transparent choice, ...
	/**
	 * whether or not the keywords are to be handled in a case-independent way
	 */
	public static boolean ignoreCase = true;
	// END KGU#165 2016-03-25
	
	// START KGU#288 2016-11-06: Issue #279: Access limited to private, compensated by new methods
	//public static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
	private static final HashMap<String, String> keywordMap = new LinkedHashMap<String, String>();
	// END KGU#288 2016-11-06
	static {
		keywordMap.put("preAlt",     "");
		keywordMap.put("postAlt",    "");
		keywordMap.put("preCase",    "");
		keywordMap.put("postCase",   "");
		keywordMap.put("preFor",     "for");
		keywordMap.put("postFor",    "to");
		keywordMap.put("stepFor",    "by");
		keywordMap.put("preForIn",   "foreach");
		keywordMap.put("postForIn",  "in");
		keywordMap.put("preWhile",   "while");
		keywordMap.put("postWhile",  "");
		keywordMap.put("preRepeat",  "until");
		keywordMap.put("postRepeat", "");
		keywordMap.put("preLeave",   "leave");
		keywordMap.put("preReturn",  "return");
		keywordMap.put("preExit",    "exit");
		// START KGU#376 017-04-11: Enh. #389
		keywordMap.put("preImport",  "include");
		// END KGU#376 2017-04-11
		keywordMap.put("input",      "INPUT");
		keywordMap.put("output",     "OUTPUT");
	}
	
	public static void loadFromINI()
	{
		final HashMap<String, String> defaultKeys = new HashMap<String, String>();
		// START KGU 2017-01-06: Issue #327: Defaults changed to English
		defaultKeys.put("ParserPreFor", "for");
		defaultKeys.put("ParserPostFor", "to");
		defaultKeys.put("ParserStepFor", "by");
		defaultKeys.put("ParserPreForIn", "foreach");
		defaultKeys.put("ParserPostForIn", "in");
		defaultKeys.put("ParserPreWhile", "while ");
		defaultKeys.put("ParserPreRepeat", "until ");
		defaultKeys.put("ParserPreLeave", "leave");
		defaultKeys.put("ParserPreReturn", "return");
		defaultKeys.put("ParserPreExit", "exit");
		defaultKeys.put("ParserInput", "INPUT");
		defaultKeys.put("ParserOutput", "OUTPUT");
		// END KGU 2017-01-06 #327
		// START KGU#376 017-04-11: Enh. #389
		defaultKeys.put("ParserPreImport", "include");
		// END KGU#376 2017-04-11
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();

			for (String key: keywordMap.keySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
                                if(defaultKeys.containsKey(propertyName))
                                {
                                    keywordMap.put(key, ini.getProperty(propertyName, defaultKeys.get(propertyName)));
                                }
                                else
                                {
                                    keywordMap.put(key, ini.getProperty(propertyName, ""));
                                }
			}
			
			// START KGU#165 2016-03-25: Enhancement configurable case awareness
			ignoreCase = ini.getProperty("ParserIgnoreCase", "true").equalsIgnoreCase("true");
			// END KGU#3 2016-03-25
			
		}
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}
	
	public static void saveToINI()
	{
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();			// elements
			for (Map.Entry<String, String> entry: getPropertyMap(true).entrySet())
			{
				String propertyName = "Parser" + Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);
				ini.setProperty(propertyName, entry.getValue());
			}
			
			ini.save();
		}
		catch (Exception e) 
		{
			System.out.println(e);
		}
	}
	
	// START KGU#163 2016-03-25: For syntax analysis purposes
	/**
	 * Returns the complete set of configurable parser keywords for Elements 
	 * @return array of current keyword strings
	 */
	public static String[] getAllProperties()
	{
		String[] props = new String[]{};
		return keywordMap.values().toArray(props);
	}
	// END KGU#163 2016-03-25
	
	// START KGU#258 2016-09-25: Enh. #253 (temporary workaround for the needed Hashmap)
	/**
	 * Returns a Hashmap mapping parser preference labels like "preAlt" to the
	 * configured parser preference keywords.
	 * @param includeAuxiliary - whether or not non-keyword settings (like "ignoreCase") are to be included
	 * @return the hash table with the current settings
	 */
	public static final HashMap<String, String> getPropertyMap(boolean includeAuxiliary)
	{
		HashMap<String, String> keywords = keywordMap;
		if (includeAuxiliary)
		{
			keywords = new HashMap<String,String>(keywordMap);
			// The following information may be important for a correct search
			keywords.put("ignoreCase",  Boolean.toString(ignoreCase));
		}
		return keywords;
	}
	// END KGU#258 2016-09-25
	
	// START KGU#288 2016-11-06: New methods to facilitate bugfix #278, #279
	/**
	 * Returns the set of the parser preference names
	 * @return
	 */
	public static Set<String> keywordSet()
	{
		return keywordMap.keySet();
	}
	
	/**
	 * Returns the cached keyword for parser preference _key or null
	 * @param _key - the name of the requested parser preference
	 * @return the cached keyword or null
	 */
	public static String getKeyword(String _key)
	{
		return keywordMap.get(_key);
	}
	
	/**
	 * Returns the cached keyword for parser preference _key or the given _defaultVal if no
	 * entry or only an empty entry is found for _key.
	 * @param _key - the name of the requested parser preference
	 * @param _defaultVal - a default keyword to be returned if there is no non-empty cached value
	 * @return the cached or default keyword
	 */
	public static String getKeywordOrDefault(String _key, String _defaultVal)
	{
		// This method circumvents the use of the Java 8 method:
		//return keywordMap.getOrDefault(_key, _defaultVal);
		String keyword = keywordMap.get(_key);
		if (keyword == null || keyword.isEmpty()) {
			keyword = _defaultVal;
		}
		return keyword;
	}
	
	/**
	 * Replaces the cached parser preference _key with the new keyword _keyword for this session.
	 * Note:
	 * 1. This does NOT influence the Ini file!
	 * 2. Only for existing keys a new mapping may be set 
	 * @param _key - name of the parser preference
	 * @param _keyword - new value of the parser preference or null
	 */
	public static void setKeyword(String _key, String _keyword)
	{
		if (_keyword == null) {
			_keyword = "";
		}
		// Bugfix #281/#282
		if (keywordMap.containsKey(_key)) {
			keywordMap.put(_key, _keyword);
		}
	}
	// END KGU#288 2016-11-06
	
}

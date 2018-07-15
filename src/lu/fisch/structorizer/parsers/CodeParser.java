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
 *      Simon Sobisch   2017.06.07      Precautions for non-printable characters in the log stream 
 *      Kay Gürtzig     2017.06.22      Enh. #420: Infrastructure for comment import
 *      Kay Gürtzig     2017.09.30      Enh. #420: Cleaning mechanism for the retrieved comments implemented
 *      Kay Gürtzig     2018.04.12      Issue #489: Fault tolerance improved.
 *      Kay Gürtzig     2018.06.29      Enh. #553: Listener management added
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.swing.SwingWorker;

import com.creativewidgetworks.goldparser.engine.ParserException;
import com.creativewidgetworks.goldparser.engine.Position;
import com.creativewidgetworks.goldparser.engine.Reduction;
import com.creativewidgetworks.goldparser.engine.Symbol;
import com.creativewidgetworks.goldparser.engine.SymbolList;
import com.creativewidgetworks.goldparser.engine.Token;
import com.creativewidgetworks.goldparser.engine.enums.SymbolType;

import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.helpers.IPluginClass;
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
public abstract class CodeParser extends javax.swing.filechooser.FileFilter implements IPluginClass
{
	/************ Common fields *************/
	
	// START KGU#484 2018-03-22 Issue #463
	private Logger logger;
	/** @return the standard Java logger for this class */
	protected Logger getLogger()
	{
		if (this.logger == null) {
			this.logger = Logger.getLogger(getClass().getName());
		}
		return this.logger;
	}
	// END KGU#484 2018-03-22
	/**
	 * String field holding the message of error occurred during parsing or build phase
	 * for later evaluation (empty if there was no error) 
	 */
	public String error;
	/**
	 * Maximum width for displaying parsing errors in a dialog (used for
	 * line wrapping)
	 */
	protected final int DLG_STR_WIDTH = 100;

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
	private List<Root> subRoots = new LinkedList<Root>();
	// START KGU#407 2017-06-22: Enh. #420 Optional comment import
	/**
	 * Value of the import option to import source code comments
	 * The option is supported by method {@link #retrieveComment(Reduction)}
	 * @see #optionSaveParseTree()
	 * @see #optionImportVarDecl
	 * @see #retrieveComment(Reduction)
	 */
	protected boolean optionImportComments = false;
	// END KGU#407 2017-06-22
	// START KGU#358 2017-03-06: Enh. #354, #368 - new import options
	/**
	 * Value of the import option to import mere variable declarations
	 * @see #optionImportComments
	 * @see #optionSaveParseTree()
	 */
	protected boolean optionImportVarDecl = false;
	/**
	 * Returns the value of the import option to save the obtained parse tree
	 * @return true iff the parse tree is to be saved as text file
	 * @see #optionImportComments
	 * @see #optionImportVarDecl
	 */
	protected boolean optionSaveParseTree()
	{
		return Ini.getInstance().getProperty("impSaveParseTree", "false").equals("true");
	}
	// END KGU#358 2017-03-06
	
	// START KGU#537 2018-07-01: Enh. #533 - for progress notification we need control
	/**
	 * Adds the given {@link Root} to {@link #subRoots} and notifies about the new
	 * Root count. There is no check for duplicity!
	 * @param newRoot - the {@link Root} to be added.
	 * @see #addAllRoots(Collection)
	 * @see #getSubRootCount()
	 */
	protected void addRoot(Root newRoot)
	{
		int oldCount = this.subRoots.size();
		this.subRoots.add(newRoot);
		this.firePropertyChange("root_count", oldCount, this.subRoots.size());
	}
	/**
	 * Adds all the given {@link Root}s to {@link #subRoots} and notifies about the new
	 * Root count. There is no check for duplicity!
	 * @param roots - a collection of {@link Root}s to be added.
	 * @see #addRoot(Root)
	 * @see #getSubRootCount()
	 */
	protected void addAllRoots(Collection<Root> roots)
	{
		int oldCount = this.subRoots.size();
		this.subRoots.addAll(roots);
		this.firePropertyChange("root_count", oldCount, this.subRoots.size());
	}
	/**
	 * @return - the current number of created sub-{@link Root}s
	 * @see #addRoot(Root)
	 * @see #addAllRoots(Collection)
	 */
	protected int getSubRootCount()
	{
		return this.subRoots.size();
	}
	// END KGU#537 3018-07-01

	// START KGU#395 2017-05-26: Enh. #357 - parser-specific options
	private final HashMap<String, Object> optionMap = new HashMap<String, Object>();

	/**
	 * Returns a Generator-specific option value if available (otherwise null)
	 * @param _optionName - option key, to be combined with the parser class name
	 * @param _defaultValue TODO
	 * @return an Object (of the type specified in the plugin) or null
	 */
	@Override
	public Object getPluginOption(String _optionName, Object _defaultValue) {
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
	@Override
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
	
	// START KGU#407 2017-06-22: Enh. #420: Comment import
	/**
	 * Maps Reductions back their respective "owning" Tokens if the latter is directly
	 * associated with a comment, just for making sure we won't miss an entry on comment
	 * retrieval.
	 */
	private HashMap<Reduction, Token> commentMap = new HashMap<Reduction, Token>();
	/**
	 * Set of rule ids for statement detection in comment retrieval (stopper). To be filled with
	 * {@link #registerStatementRuleIds(int[])}
	 */
	private Set<Integer> statementRuleIds = new HashSet<Integer>();
	// END KGU#407 2017-06-22
	
	// START KGU#537 2018-06-29: Enh. #553
	/**
	 * Internal exception to force termination in case of a cancelled thread
	 */
	@SuppressWarnings("serial")
	public class ParserCancelled extends Exception
	{
	}
	/**
	 * Holds the controlling {@link SwingWorker} in case of an interactive background execution.
	 * @see CodeParser#setSwingWorker(SwingWorker)
	 */
	private SwingWorker<?, ?> worker = null;
	/** Giving the parser access to the worker that is performing it allows the parser to
	 * inform about status or progress via property change notifications.
	 * @param _worker - a {@link SwingWorker} representing the worker thread
	 */
	public void setSwingWorker(SwingWorker<?, ?> _worker)
	{
		this.worker = _worker;
	}
	/**
	 * Returns true if the parser is running under the control of a cancelled {@link SwingWorker}
	 * and hence is to stop as soon as possible.<br/>
	 * Subclasses should test this method frequently in order to be able to end their actions if
	 * true is returned.
	 * @return true if there is a worker and te worker was stopped
	 * @see #doStandardCancelActionIfReqested()
	 */
	public boolean isCancelled()
	{
		return this.worker != null && (this.worker.isCancelled() || Thread.interrupted());
	}
	/**
	 * Checks whether the parser is running under the control of a cancelled {@link SwingWorker}
	 * and if so, sets a cancelled message into the error field, does all necessary disposal of
	 * general resources (e.g. log file) and returns true - such that the parser
	 * may be left immediately.
	 * @throws ParserCancelled 
	 * @see #isCancelled()
	 */
	protected void checkCancelled() throws ParserCancelled
	{
		if (this.isCancelled()) {
			error = this.getClass().getSimpleName() + " CANCELLED!";
			log(error, false);
			closeLog();
			getLogger().log(Level.WARNING, error);
			//System.err.println("+++ ParserCancelled thrown!");
			throw new ParserCancelled();
		}
	}
	/**
	 * If this runs under the control of a non-terminated {@link SwingWorker} then has it report the
	 * given bound property update to any registered listeners. In this case the result will be true,
	 * otherwise false.
	 * The {@link SwingWorker} will not fire an event if old and new are equal and non-null. 
	 * @param propertyName - the programmatic name of the property that was changed
	 * @param oldValue - the old value of the property
	 * @param newValue - the new value of the property
	 * @return true iff the property change could be propagated to an active worker.
	 */
	protected boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		boolean done = false;
		if (this.worker != null && !this.worker.isDone()) {
			this.worker.firePropertyChange(propertyName, oldValue, newValue);
			done = true;
		}
		return done;
	}
	// END KGU#537 2018-06-29

	/************ Abstract Methods *************/
	
	/**
	 * Is to provide the file name of the compiled grammar the parser class is made for
	 * @return a grammar file name retrievable as resource (a cgt or egt file).
	 */
	protected abstract String getCompiledGrammar();
	
	/**
	 * Is to return the internal name of the grammar table as given in the grammar file
	 * parameters
	 * @return Name string as specified in the grammar file header
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
	 * Parses the source code from file _textToParse, which is supposed to be encoded
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
		// START KGU#537 2018-07-01: Enh. #553
		try {
		// END KGU#537 2018-07-01
			Random random = new Random();	// FIXME: for testing only
			if (_logDir != null) {
				File logDir = new File(_logDir);
				if (logDir.isDirectory()) {
					try {
						File log = new File(_textToParse);
						logFile = new OutputStreamWriter(new FileOutputStream(new File(logDir, log.getName() + ".log")), "UTF-8");
					}
					// START KGU#484 2018-04-05: Issue #463
					//catch (UnsupportedEncodingException e) {
					//	e.printStackTrace();
					//}
					//catch (FileNotFoundException e) {
					//	e.printStackTrace();
					//}
					catch (Exception e) {
						getLogger().log(Level.SEVERE, "Creation of parser log file failed.", e);
					}
					// END KGU#484 2018-04-05
				}
			}
			// START KGU#537 2018-06-30: Enh. #553
			this.checkCancelled();
			// END KGU#537 2018-06-30
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

			// START KGU#537 2018-06-30: Enh. #553
			this.checkCancelled();
			// END KGU#537 2018-06-30

			// START KGU#370 2017-03-25: Fix #357 - precaution against preparation failure
			//File intermediate = prepareTextfile(textToParse, _encoding);
			File intermediate = null;
			log("STARTING FILE PREPARATION...\n\n", false);
			// START KGU#537 2018-06-30: Enh. #553
			this.firePropertyChange("phase_start", -1, 0);
			//// DEBUG Sleep for up to one second.
			//try {
			//	Thread.sleep(random.nextInt(1000));
			//} catch (InterruptedException ignore) {}
			// END KGU#537 2018-06-30
			try {
				intermediate = prepareTextfile(_textToParse, _encoding);
				// START KGU#537 2018-06-30: Enh. #553
				//// DEBUG Sleep for up to one second.
				//try {
				//	Thread.sleep(random.nextInt(1000));
				//} catch (InterruptedException ignore) {}
				this.firePropertyChange("progress", 0, 100);
				// END KGU#537 2018-06-30
			}
			catch (ParserCancelled ex) {
				throw ex;
			}
			catch (Exception ex) {
				String errText = ex.getMessage();
				if (errText == null) {
					errText = ex.toString();
				}
				error = ":\n" + errText + (error.isEmpty() ? "" : (":\n" + error));
			}

			// START KGU#537 2018-06-30: Enh. #553
			this.checkCancelled();
			// END KGU#537 2018-06-30

			if (intermediate == null) {
				error = "**FILE PREPARATION ERROR** on file \"" + _textToParse + "\"" + (error.isEmpty() ? "" : (":\n" + error));
				log(error, false);
				closeLog();
				// START KGU#537 2018-07-01: Enh. #553
				this.firePropertyChange("error", "", error);
				// END KGU#537 2018-07-01
				return subRoots;	// It doesn't make sense to continue here (BTW subRoots is supposed to be empty)
			}
			// END KGU#370 2017-03-25
			else {
				log("\nFILE PREPARATION COMPLETE -> \"" + intermediate.getAbsolutePath() + "\"\n\n", false);
			}

			String sourceCode = null;

			boolean isSyntaxError = false;

			// START KGU#537 2018-06-30: Enh. #553
			this.checkCancelled();
			// END KGU#537 2018-06-30

			try {
				// START KGU#537 2018-06-30: Enh. #553
				this.firePropertyChange("phase_start", 0, 1);
				//// DEBUG Sleep for up to one second.
				//try {
				//	Thread.sleep(random.nextInt(1000));
				//} catch (InterruptedException ignore) {}
				// END KGU#537 2018-06-30
				sourceCode = loadSourceFile(intermediate.getAbsolutePath(), _encoding);
				// START KGU#537 2018-06-30: Enh. #553
				this.checkCancelled();
				// END KGU#537 2018-06-30
				// Parse the source statements to see if it is syntactically correct
				boolean parsedWithoutError = parser.parseSourceStatements(sourceCode);

				// Holds the parse tree if setGenerateTree(true) was called
				//tree = parser.getParseTree();

				// Either execute the code or print any error message
				if (parsedWithoutError) {
					// ************************************** log file
					getLogger().info("Parsing complete.");	// System logging
					log("\nParsing complete.\n\n", false);
					// ************************************** end log
					// START KGU#537 2018-06-30: Enh. #553
					//// DEBUG Sleep for up to one second.
					//try {
					//	Thread.sleep(random.nextInt(1000));
					//} catch (InterruptedException ignore) {}
					this.firePropertyChange("progress", 0, 100);
					// END KGU#537 2018-06-30
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
							getLogger().log(Level.WARNING, "Saving .parsetree.txt failed: {0}", ex.getMessage());
						}
					}
					// START KGU#537 2018-06-30: Enh. #553
					this.firePropertyChange("phase_start", 1, 2);
					//// DEBUG Sleep for up to one second.
					//try {
					//	Thread.sleep(random.nextInt(1000));
					//} catch (InterruptedException ignore) {}
					// END KGU#537 2018-06-30
					if (this.optionImportComments) {
						// Prepare the comment map for reductions
						for (Token commentedToken: parser.commentMap.keySet()) {
							if (commentedToken.getType() == SymbolType.NON_TERMINAL) {
								this.commentMap.put(commentedToken.asReduction(), commentedToken);
							}
						}
					}
					buildNSD(parser.getCurrentReduction());
					// START KGU#537 2018-06-30: Enh. #553
					//// DEBUG Sleep for up to one second.
					//try {
					//	Thread.sleep(random.nextInt(1000));
					//} catch (InterruptedException ignore) {}
					this.firePropertyChange("progress", 0, 100);
					// END KGU#537 2018-06-30
				} else {
					isSyntaxError = true;
					error = parser.getErrorMessage() + " in file \"" + _textToParse + "\"";
				}
			}
			catch (ParserException e) {
				error = "**PARSER ERROR** with file \"" + _textToParse + "\":\n" + e.getMessage();
				// START KGU#484 2018-04-05: Issue #463
				//e.printStackTrace();
				getLogger().log(Level.WARNING, error, e);
				// END KGU#484 2018-04-05
			}
			catch (IOException e1) {
				error = "**IO ERROR** on importing file \"" + _textToParse + "\":\n" + e1.getMessage();
				// START KGU#484 2018-04-05: Issue #463
				//e1.printStackTrace();
				getLogger().log(Level.WARNING, error, e1);
				// END KGU#484 2018-04-05
			}
			catch (Exception e2) {
				error = "**Severe error on importing file \"" + _textToParse + "\":\n" + e2.toString();
				// START KGU#484 2018-04-05: Issue #463
				//e2.printStackTrace();
				getLogger().log(Level.WARNING, error, e2);
				// END KGU#484 2018-04-05
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
				// Note: position may not be correct if preprocessor dropped / added lines
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
				// START KGU#511 2018-04-12: Issue #489
				//final String tokVal = token.toString();
				final String tokVal = (token == null) ? "ε (END OF TEXT)" : token.toString();
				// END KGU#511 2018-04-12
				error += "\n\nFound token " + tokVal;
				if (token != null) {
					String tokStr = token.asString().trim();
					// START KGU 2017-05-23: The token might be a generic surrogate for preprocessing, show the original id  
					tokStr = this.undoIdReplacements(tokStr);
					// END KGU 2017-05-23
					if (!tokVal.equals(tokStr) && !tokVal.equals("'" + tokStr + "'")) {
						error += " (" + tokStr + ")";
					}
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
				getLogger().warning("Parsing failed.");	// System logging
				log("\n" + error + "\n\n", true);
				// ************************************** end log
			}
			// END KGU#191 2016-04-30

			// START KGU#537 2018-06-30: Enh. #553
			if (!error.isEmpty()) {
				this.firePropertyChange("error", "", error);
				return this.subRoots;
			} else {
				// remove the temporary file
				//intermediate.delete();
			}
			
			this.firePropertyChange("phase_start", 2, 3);
			//// DEBUG Sleep for up to one second.
			//try {
			//	Thread.sleep(random.nextInt(1000));
			//} catch (InterruptedException ignore) {}
			// END KGU#537 2018-06-30
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
				// START KGU#537 2018-07-01: Enh. #533
				this.firePropertyChange("root_count", subRoots.size()-1, this.subRoots.size());
				// END KGU#537 2017-07-01
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
				// START KGU#537 2018-06-30: Enh. #553
				//// DEBUG Sleep for up to one second.
				//try {
				//	Thread.sleep(random.nextInt(1000));
				//} catch (InterruptedException ignore) {}
				this.firePropertyChange("progress", 0, 100);
				// END KGU#537 2018-06-30
			}
			// START KGU#537 2018-07-01: Enh. #553 We must not swallow this here
			catch (ParserCancelled ex) {
				throw ex;
			}
			// END KGU#537 2018-07-01
			catch (Exception ex) {
				if ((error = ex.getMessage()) == null) {
					error = ex.toString();
				}
				error = "Problems in postprocess:\n" + error;
			}

			log("\nBUILD PHASE COMPLETE.\n", true);
			if (subRoots.size() >= 1 && subRoots.get(0).children.getSize() > 0) {
				log(subRoots.size() + " diagram(s) built.\n", true);
			}
			else {
				log("No diagrams built.\n", true);
			}
			closeLog();
			this.firePropertyChange("root_count", -1, subRoots.size());
		// START KGU#537 2018-07-01: Enh. #553
		}
		catch (ParserCancelled ex) {}
		finally {
			closeLog();
		}
		if (!error.isEmpty()) {
			this.firePropertyChange("error", "", error);
		}
		// END KGU#537 2018-07-01
		return subRoots;
	}

	/**
	 * Closes the specific parser log (if it had been set up)
	 */
	private void closeLog() {
		if (logFile != null) {
			try {
				logFile.close();
				logFile = null;
			} catch (IOException e) {
				// START KGU#484 2018-04-05: Issue #463
				//e.printStackTrace();
				getLogger().log(Level.WARNING, "Failed to close parser log.", e);
				// END KGU#484 2018-04-05
			}
		}
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
	 * Otherwise and if {@code _toSystemOutInstead} is true the content will be
	 * written to the console output.
	 * @param _logContent - the message to be logged
	 * @param _toSystemOutInstead - whether the message is to be reported to System.out
	 */
	protected void log(String _logContent, boolean _toSystemOutInstead)
	{
		boolean done = false;
		
		// START SSO 2017-06-07
		// Hack for replacing non-printable characters that we may have to log,
		// for example from StreamTokenizer value of '\0'
		StringBuilder printableString = new StringBuilder(_logContent.length());
		for (int offset = 0; offset < _logContent.length();)
		{
			int codePoint = _logContent.codePointAt(offset);
			char lcChar = _logContent.charAt(offset);

			switch (lcChar) {
				// include some standard characters
				case '\n':
				case '\r':
				case '\t':
					printableString.append(lcChar);
					break;
				default:
					// Replace invisible control characters and unused code points
					switch (Character.getType(codePoint))
					{
						case Character.CONTROL:     // \p{Cc}
						case Character.FORMAT:      // \p{Cf}
						case Character.PRIVATE_USE: // \p{Co}
						case Character.SURROGATE:   // \p{Cs}
						case Character.UNASSIGNED:  // \p{Cn}
							printableString.append(String.format ("\\u%04x", (int)lcChar));
							break;
						default:
							printableString.append(Character.toChars(codePoint));
							break;
					}
					break;
			}
			offset += Character.charCount(codePoint);
		}
		_logContent = printableString.toString();
		// END SSO 2017-06-07

		if (logFile != null)
		try {
			logFile.write(_logContent);
			done = true;
		} catch (IOException e) {
			getLogger().log(Level.WARNING, "Failed to write user log entry.", e);
		}
		if (!done && _toSystemOutInstead) {
			// START KGU#484 2018-03-22: Issue #463 
			//System.out.print(_logContent);
			getLogger().info(_logContent);
			// END KGU#484 2018-03-22
		}
	}
	
	// START KGU#407 2017-06-22: Enh. #420 allow subclasses a comment retrieval
	/**
	 * Adds all rule ids given by the array to the registered rule ids for statement
	 * detection in comment retrieval.
	 * @param _ruleIds - production table indices indicating a statement in the source language
	 * @see #registerStatementRuleId(int)
	 * @see #retrieveComment(Reduction) 
	 */
	protected void registerStatementRuleIds(int[] _ruleIds)
	{
		for (int id: _ruleIds) {
			this.registerStatementRuleId(id);
		}
	}
	/**
	 * Add the given rule id  to the registered rule ids for statement detection
	 * in comment retrieval.
	 * @param _ruleIds - production table indices indicating a statement in the source language
	 * @see #registerStatementRuleIds(int[])
	 * @see #retrieveComment(Reduction) 
	 */
	protected void registerStatementRuleId(int _ruleId)
	{
		this.statementRuleIds.add(_ruleId);
	}
	/**
	 * Retrieves comments associated to Tokens in the subtree rooted by the given
	 * Reduction {@code _reduction} if {@link #optionImportComments} is set, otherwise
	 * or there hasn't been a comment in the code returns null<br/>
	 * NOTE: For a sensible limitation of the search depth the set of statement rule ids
	 * must have been configured before. Otherwise the comments of substructure statements
	 * might also be concatenated to the enclosing structured statement's comment.
	 * @param _reduction
	 * @return strung comprising the collected comment lines
	 * @throws ParserCancelled 
	 * @see #registerStatementRuleId(int)
	 * @see #registerStatementRuleIds(int[])
	 * @see #equipWithSourceComment(Element, Reduction)
	 */
	protected String retrieveComment(Reduction _reduction) throws ParserCancelled
	{
		// START KGU#537 2018-06-30: Enh. #553
        this.checkCancelled();
		// END KGU#537 2018-06-30		
		if (this.optionImportComments) {
			//System.out.println("START SEARCH FOR " + _reduction);
			StringBuilder comment = new StringBuilder();
			// First we look if the reduction's token itself might have a comment 
			Token topToken = this.commentMap.get(_reduction);
			if (topToken != null) {
				comment.append("\n" + parser.commentMap.get(topToken));
			}
			// Then we check the subtree recursively
			retrieveComment_R(_reduction, comment);
			if (comment.length() > 0) {
				return cleanComment(comment.toString().substring(1));	// Remove the first newline
			}
		}
		return null;
	}
	private void retrieveComment_R(Reduction _reduction, StringBuilder _comment)
	{
		for (int i = 0; i < _reduction.size(); i++) {
			Token token = _reduction.get(i);
			// Now the following cases are to be distinguished:
			// 1. The token is a non-terminal
			//    1.1 It represents a statement on its own: skip it
			//    1.2 Something else: check for comment and descend
			// 2. The token is a terminal: check for comment
			if (token.getType() == SymbolType.NON_TERMINAL) {
				Reduction red = token.asReduction();
				//System.out.print(Integer.toString(i) + ". ("+ red.getParent().getTableIndex() +") " + red);
				if (!statementRuleIds.contains(red.getParent().getTableIndex())) {
					String comment = parser.commentMap.get(token);
					//System.out.println(" ==> " + comment);
					if (comment != null) {
						_comment.append("\n" + comment);
					}
					retrieveComment_R(red, _comment);
				}
				else {
					//System.out.println(" STOP!");
				}
			}
			else  {
				String comment = parser.commentMap.get(token);
				//System.out.print(Integer.toString(i) + ". ending at " + token + " (" + token.hashCode() + ")");
				if (comment != null) {
					//System.out.println(" ==> " + comment.substring(0, Math.min(comment.length(), 30)));
					_comment.append("\n" + comment);
				}
				//else {
				//	System.out.println(" %%% ");
				//}
			}
		}
	}
	/**
	 * Convenience method that retrieves the source comment for the given Reduction
	 * {@code _reduction} and places this (if any) in Element {@code _ele}, if the
	 * option to import comments ({@link #optionImportComments}) is set. Does nothing
	 * otherwise.<br/>
	 * NOTE:<br/>
	 * 1. In order to work correctly this method requires an initial parser-specific
	 * statement rule registration via {@link #registerStatementRuleIds(int[])} once
	 * been done.<br/>
	 * 2. This method will overwrite any comment that might have been placed on
	 * {@code _ele} before. So make sure to insert or append some additional comments
	 * related to e.g. parsing or building issues afterwards.
	 * @param _ele - The element built from Reduction {@code _reduction}
	 * @param _reduction - The reduction, which led to the creation of {@code _ele}
	 * @return The same element, but possibly with comment.
	 * @throws ParserCancelled 
	 * @see #optionImportComments
	 * @see #retrieveComment(Reduction)
	 * @see #registerStatementRuleIds(int[])
	 */
	protected Element equipWithSourceComment(Element _ele, Reduction _reduction) throws ParserCancelled
	{
		String comment = this.retrieveComment(_reduction);
		if (comment != null) {
			_ele.setComment(comment);
		}
		return _ele;
	}
	// END KGU#407 2017-06-22
	// START KGU#407 2017-09-30: Enh. #420
	private String cleanComment(String _rawComment) {
		StringList lines = StringList.explode(_rawComment, "\n");
		String endDelim = null;
		String[][] delims = this.getCommentDelimiters();
		for (int i = 0; i < lines.count(); i++) {
			String line = lines.get(i);
			if (endDelim == null) {
				line = line.trim();
				for (String[] pair: delims) {
					if (line.startsWith(pair[0])) {
						line = line.substring(pair[0].length());
						lines.set(i, line);
						if (pair.length > 1) {
							endDelim = pair[1];
						}
						break;
					}
				}
			}
			if (endDelim != null && line.endsWith(endDelim)) {
				line = line.substring(0, line.length() - endDelim.length());
				lines.set(i, line);
				endDelim = null;
			}
		}
		return lines.getText();
	}
	/**
	 * Returns an array of String arrays. Each element String array must consist of:<br/>
	 * 1. a single String representing a line comment delimiter, or<br/>
	 * 2. a pair of comment delimiters ([0] the left and [1] the right one)
	 * <p>
	 * This method is to be implemented by the subclasses. 
	 * @return the array of single delimiters and delimiter pairs
	 */
	abstract protected String[][] getCommentDelimiters();
	// END KGU#407 2017-09-30


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
	 * @throws ParserCancelled TODO
	 * @see #replacedIds
	 */
	protected abstract File prepareTextfile(String _textToParse, String _encoding) throws ParserCancelled;
	
	/**
	 * Called after the build for every created Root and allows thus to do some
	 * postprocessing for individual created Roots.
	 * @param root - one of the build diagrams
	 * @param sourceFileName - the name of the originating source file 
	 * @throws ParserCancelled TODO
	 */
	protected abstract void subclassUpdateRoot(Root root, String sourceFileName) throws ParserCancelled;

	/**
	 * Allows subclasses to do some finishing work after all general stuff after parsing and
	 * NSD synthesis is practically done.
	 * @param textToParse - path of the parsed source file
	 * @throws ParserCancelled TODO
	 */
	protected void subclassPostProcess(String textToParse) throws ParserCancelled
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
	 * @throws ParserCancelled 
	 */
	protected final void buildNSD(Reduction _reduction) throws ParserCancelled
	{
		// START KGU#358 2017-03-06: Enh. #368 - consider import options!
		this.optionImportVarDecl = Ini.getInstance().getProperty("impVarDeclarations", "false").equals("true");
		// END KGU#358 2017-03-06
		// START KGU#407 2017-06-22
		this.optionImportComments = Ini.getInstance().getProperty("impComments", "false").equals("true");
		// END KGU#407 2017-06-22
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
	 * @throws ParserCancelled TODO
	 */
	protected abstract void buildNSD_R(Reduction _reduction, Subqueue _parentNode) throws ParserCancelled;
	
	/**
	 * Composes the parsed non-terminal _reduction to a Structorizer-compatible
	 * terminal string, combines it with the given _content string and returns the
	 * result
	 * @param _reduction - a reduction sub-tree
	 * @param _content - partial translation result to be combined with the _reduction
	 * @return the combined translated string
	 * @throws ParserCancelled TODO
	 */
	protected abstract String getContent_R(Reduction _reduction, String _content) throws ParserCancelled;
	
	/**
	 * Overridable method to do target-language-specific initialization before
	 * the recursive method {@link #buildNSD_R(Reduction, Subqueue)} will be called.
	 * Method is called in {@link #buildNSD(Reduction)}.<br/>
	 * The subclass method should call {@link #registerStatementRuleIds(int[])} here.
	 * @throws ParserCancelled TODO
	 */
	protected void initializeBuildNSD() throws ParserCancelled
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
			Logger.getLogger(CodeParser.class.getName()).log(Level.WARNING, "Ini", e);
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
			Logger.getLogger(CodeParser.class.getName()).log(Level.WARNING, "Ini", e);
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

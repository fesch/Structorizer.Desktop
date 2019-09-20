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

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Abstract class for any code generator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------          ----			-----------
 *      Bob Fisch       2007-12-27		First Issue
 *      Bob Fisch       2008-04-12		Plugin Interface
 *      Kay Gürtzig     2014-11-16		comment generation revised (see comment below)
 *      Kay Gürtzig     2015-10-18		File name proposal in exportCode(Root, File, Frame) delegated to Root
 *      Kay Gürtzig     2015-11-01		transform methods re-organised (KGU#18/KGU23) using subclassing
 *      Kay Gürtzig     2015-11-30		General pre-processing for generateCode(Root, String) (KGU#47)
 *      Bob Fisch       2015-12-10		Bugfix #51: when input identifier is alone, it was not converted
 *      Kay Gürtzig     2015-12-18		Enh #66, #67: New export options
 *      Kay Gürtzig     2015-12-21      Bugfix #41/#68/#69 (= KGU#93) avoid padding and string literal impact
 *      Kay Gürtzig     2015-12-22		Slight performance improvement in transform()
 *      Kay Gürtzig     2016-01-16      KGU#141: New generic method lValueToTypeNameIndex introduced for Issue #112
 *      Kay Gürtzig     2016-03-22      KGU#61/KGU#129: varNames now basic field for all subclasses
 *      Kay Gürtzig     2016-03-31      Enh. #144 - content conversion may be switched off
 *      Kay Gürtzig     2016-04-01      Enh. #110 - export file filter now pre-selected
 *      Kay Gürtzig     2016-04-04      Issues #149, #151 - Configurable charset / useless ExportOptionDialogs
 *      Kay Gürtzig     2016-04-28      Draft for enh. #179 - batch mode (KGU#187)
 *      Kay Gürtzig     2016-04-29      Bugfix KGU#189 for issue #61/#107 (mutilated array access)
 *      Kay Gürtzig     2016-07-19      Enh. #192: File name proposal slightly modified (KGU#205)
 *      Kay Gürtzig     2016-07-20      Enh. #160: Support for export of involved subroutines (KGU#178)
 *      Kay Gürtzig     2016-08-10      Issue #227: information gathering pass introduced to control optional
 *                                      code expressions
 *                                      Bugfix #228: Unnecessary error message exporting recursive routines
 *      Kay Gürtzig     2016-09-25      Enh. #253: CodeParser.kewordMap refactoring done
 *      Kay Gürtzig     2016-10-13      Enh. #270: Basic functionality for disabled elements (addCode()))
 *      Kay Gürtzig     2016-10-15      Enh. #271: transformInput() and signature of getOutputReplacer() modified
 *      Kay Gürtzig     2016-10-16      Bugfix #275: Defective subroutine registration for topological sort mended
 *      Kay Gürtzig     2016-12-01      Bugfix #301: New method boolean isParenthesized(String)
 *      Kay Gürtzig     2016-12-22      Enh. #314: Support for Structorizer File API, improvements for #227
 *      Kay Gürtzig     2017-01-20      Bugfix #336: variable list for declaration section (loop vars in, parameters out)
 *      Kay Gürtzig     2017-01-26      Enh. #259: Type info is now gathered for declarations support
 *      Kay Gürtzig     2017-01-30      Bugfix #337: Mutilation of lvalues with nested index access
 *      Kay Gürtzig     2017-02-19      KGU#348: Additions to support PythonGenerator in generating Parallel code
 *      Kay Gürtzig     2017-02-20      Bugfix #349: Export missed to generate recursive subroutines and their callers
 *      Kay Gürtzig     2017-02-26      Enh. #346 (mechanism to add user-configured file includes) 
 *      Kay Gürtzig     2017-02-27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig     2017-03-05      Issue #365: Support for posterior insertion of global definitions
 *      Kay Gürtzig     2017-03-10      Issue #368: New method getExportCharset
 *      Kay Gürtzig     2017-04-14      Bugfix #394: Jump map generation revised
 *      Kay Gürtzig     2017-04-18      Bugfix #386 required to lift he "final" from generateCode(Subqueue...)
 *      Kay Gürtzig     2017-04-26      Signature of method exportCode() modified to return the used directory
 *      Kay Gürtzig     2017-05-16      Enh. #372: New method insertCopyright()
 *      Kay Gürtzig     2017-09-20      Enh. #389: Mechanism for include retrieval (analogous to #160 for subroutines)
 *      Kay Gürtzig     2017-09-20      Enh. #388/#423: comment mapping for declarations introduced
 *      Kay Gürtzig     2017-09-26      Enh. #389/#423: Supporting code parts from PasGenerator adopted
 *      Kay Gürtzig     2018-02-22      Bugfix #517: Infrastructure for correct handling of decl./init. from includables
 *      Kay Gürtzig     2018-03-13      Modifications for bugfix #521, transformOutput() revised
 *      Kay Gürtzig     2018-10-30      New field generatorIncludes and method insertGeneratorIncludes() to
 *                                      avoid duplicate include/import/using entries system <-> user 
 *      Kay Gürtzig     2019-02-14      Enh. #680: Support for input instructions with several variables
 *      Kay Gürtzig     2019-02-16      Enh. #681: method exportCode() now returns null if export was cancelled.
 *      Kay Gürtzig     2019-03-13      Enh. #696: All references to Arranger replaced by routinePool,
 *                                      subroutine retrieval enabled in the batch version of exportCode
 *      Kay Gürtzig     2019-03-17      Enh. #56: Basic method generateCode(Try, String) added.
 *      Kay Gürtzig     2019-03-21      Issue #706: A newline symbol was to be appended to the last text file line
 *      Kay Gürtzig     2019-03-21      Issue #707: Modifications to the file name proposal (see comment)
 *      Kay Gürtzig     2019-03-28      Enh. #657: Retrieval for subroutines now with group filter
 *      Kay Gürtzig     2019-08-05      Enh. #737: Possibility of providing a settings file for batch export
 *      Kay Gürtzig     2019-08-07      Enh. #741: Modified API for batch export (different ini path mechanism)
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      2019-03-21 - Issue #707 (elemhsb / Kay Gürtzig)
 *      - It was requested that the proposed export file name should primarily follow the nsd file name if
 *        that has already existed.
 *      - For Python export in particular there shouldn't be hyphens in the file name. So if the user decides
 *        to export a subroutine diagram to some language wth file name peculiarities, the inheriting generator
 *        now gets a chance to intervene (i.e. to modify the file name proposal).
 *      2016-12-22 - Enhancement #314: Structorizer file API support.
 *      - This is in the most cases done by copying a set of implementing functions for the target language
 *        into the resulting file. Generator provides two methods insertFileAPI() for this purpose.
 *      - Generator supports this by an extended information scanning to decide whether the file API is used.
 *      2016-10-15 - Enhancement #271: Input instruction with integrated prompt string
 *      - For input instructions with prompt string (enh. #271), different inputReplacer patterns are needed
 *        (they must e.g. derive some input instruction). Therefore an API modification for generators to
 *        plug in became necessary: getInputReplacer() now requires a boolean argument to provide the appropriate
 *        pattern. Method transformInput() must distinguish and handle the input instruction flavours therefore.
 *      	
 *      2016-07-20 - Enhancement #160 - option to include called subroutines
 *      - there is no sufficient way to export a called subroutine when its call is generated, because
 *        duplicate exports must be avoided and usually a topological sorting is necessary.
 *        For a topologically sorted duplication-free export, however, all called subroutines must be known
 *        in advance. Therefore, we must analyse the subroutines as well in advance 
 *      	
 *      2015-11-30 - Decomposition of generateRoot() and diverse pre-processing provided for subclasses
 *      - method mapJumps fills hashTable jumpTable mapping (Jump and Loop elements to connecting codes)
 *      - parameter names and types as well as function name and type are preprocessed
 *      - result mechanisms are also analysed
 *
 *      2014-11-16 - Enhancement
 *      - method insertComment renamed to insertAsComment (as it inserts the instruction text!)
 *      - overloaded method insertComment added to export the actual element comment
 *      
 ******************************************************************************************************///

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.elements.Alternative;
import lu.fisch.structorizer.elements.Call;
import lu.fisch.structorizer.elements.Case;
import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.For;
import lu.fisch.structorizer.elements.Forever;
import lu.fisch.structorizer.elements.IElementVisitor;
import lu.fisch.structorizer.elements.ILoop;
import lu.fisch.structorizer.elements.Instruction;
import lu.fisch.structorizer.elements.Jump;
import lu.fisch.structorizer.elements.Parallel;
import lu.fisch.structorizer.elements.Repeat;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.structorizer.elements.Subqueue;
import lu.fisch.structorizer.elements.Try;
import lu.fisch.structorizer.elements.TypeMapEntry;
import lu.fisch.structorizer.elements.While;
import lu.fisch.structorizer.executor.Control;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.helpers.IPluginClass;
import lu.fisch.structorizer.io.Ini;
import lu.fisch.structorizer.parsers.CodeParser;
import lu.fisch.utils.BString;
import lu.fisch.utils.BTextfile;
import lu.fisch.utils.StringList;


public abstract class Generator extends javax.swing.filechooser.FileFilter implements IPluginClass
{
	// START KGU#371 2019-03-07: Enh. #385 - Support for default subroutine arguments
	/**
	 * Classifies a language w.r.t. the capability of overloading subroutine signatures. There are three levels
	 * distinguished here:<br/>
	 * 0. No overloading allowed: subroutines may not share the same name, not even if their argument lists differ in length.<br/>
	 * 1. Overloading is legitimate but default arguments can only be achieved by delegating the call to another subroutine
	 * with more arguments.<br/>
	 * 2. Overloading and default arguments are available, such that a single definition may declare several signatures.<br/>
	 * Note: In programming languages overloading usually also means distinction by argument types. Since declarations of variables
	 * aren't mandatory in Structorizer, tyoe inference is weak and vague at most. So it isn't actually possible to distinguish
	 * parameter lists by argument types in Structorizer. Hence executable diagrams won't make use of it. So, export should be
	 * relatively safe.
	 * @author Kay Gürtzig
	 */
	public enum OverloadingLevel {OL_NO_OVERLOADING, OL_DELEGATION, OL_DEFAULT_ARGUMENTS};
	// END KGU#371 2019-03-07
	
	// START KGU#686 2019-03-18: Enh. #56
	/**
	 * Type represents possible levels of exception support, i.e. whether or not
	 * and which parts of a try-catch-finally block are supported.(It is assumed
	 * that a {@code throw} mechanism is available if try/catch is supported.)
	 * <ul>
	 * <li>{@link #TC_NO_TRY} means no {@code try/catch/throw} at all;</li>
	 * <li>{@link #TC_TRY_CATCH} means {@code try/catch} and {@code throw} but no {@code finally} clause.</li>
	 * <li>{@link #TC_TRY_CATCH_FINALLY} means full exception support, {@code finally} included.</li>
	 * </ul>
	 * @see #getTryCatchLevel()
	 * @author Kay Gürtzig
	 */
	public enum TryCatchSupportLevel {TC_NO_TRY, TC_TRY_CATCH, TC_TRY_CATCH_FINALLY};
	// END KGU#686 2019-03-18

	
	/************ Fields ***********************/
	// START KGU#484 2018-03-22: Issue #463
	private Logger logger = null;
	// END KGU#484 2018-03-22
	// START KGU#162 2016-03-31: Enh. #144
	protected boolean suppressTransformation = false;
	// END KGU#162 2016-03-31
	private boolean exportAsComments = false;
	private boolean startBlockNextLine = false;
	private boolean generateLineNumbers = false;
	private String exportCharset = Charset.defaultCharset().name();
	// START KGU#178 2016-07-19: Enh. #160
	private boolean exportSubroutines = false;
	// END KGU#178 2016-07-19
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	private String includeFiles = "";
	// END KGU#351 2017-02-26
	// START KGU#363 2017-05-11: Enh. #372 - license and author info ought to be exportable as well
	private boolean exportAuthorLicense = false;
	// END KGU#363 2017-05-11
	// START KGU#395 2017-05-11: Enh. #357 - generator-specific options
	private final HashMap<String, Object> optionMap = new HashMap<String, Object>();
	// END KGU#395 2017-05-11
	// START KGU#676 2019-03-13: Enh. #696 Explicit routine pool instead of direct Arranger access
	protected IRoutinePool routinePool = null;
	// END KGU#676 2019-03-13

	protected StringList code = new StringList();
	
	// START KGU#194 2016-05-07: Bugfix #185 - subclasses might need filename access
	protected String pureFilename = "";
	// END KGU#194 2016-05-07

	// START KGU#74 2015-11-29: Sound handling of Jumps requires some tracking
	protected boolean returns = false; // Explicit return instructions occurred?
	protected boolean alwaysReturns = false; // Do all paths involve a return instruction?
	protected boolean isResultSet = false; // Assignment to variable named "result"?
	protected boolean isFunctionNameSet = false; // Assignment to variable named like function?
	protected int labelCount = 0; // unique count for generated labels
	protected String labelBaseName = "StructorizerLabel_";
	/**
	 * maps loops and Jump elements to label counts (neg. number means illegal jump target)
	 * such that goto mechanisms mght be used to circumvent missing leave instructions in
	 * the target language. Automatically filled before actual code export starts.
	 */
	protected Hashtable<Element, Integer> jumpTable = new Hashtable<Element, Integer>();
	// END KGU#74 2015-11-29
	// START KGU#178 2016-07-19: Enh. #160 Subroutines for export integration
	/** Recursive usage map of called subroutines */
	protected Hashtable<Root, SubTopoSortEntry> subroutines = new Hashtable<Root, SubTopoSortEntry>();
	/** Line number where to insert subroutine definitions */
	protected int subroutineInsertionLine = 0;
	/** Indentation level (string) for subroutine definitions to be inserted */
	protected String subroutineIndent = "";
	/** Signatures of missing routines (routines called but not found) */
	protected StringList missingSubroutines = new StringList();
	/** Is the currently processed Root the top of the tree (the one the job was started for)? */
	protected boolean topLevel = true;
	// END KGU#178 2016-07-19
	// START KGU#376 2017-09-20: Enh. #389
	/** Recursive usage map of diagram includes */
	protected Hashtable<Root, SubTopoSortEntry> includeMap = new Hashtable<Root, SubTopoSortEntry>();
	/** Topologically sorted Queue of all diagrams recursively included by the Roots to be exported. */
	protected Queue<Root> includedRoots = new LinkedList<Root>();
	// END KGU#376 2017-09-20
	// START KGU#376/KGU#388 2017-09-26: Enh. #389, #423
	protected HashMap<Root, StringList> structuredInitialisations = new HashMap<Root, StringList>();
	/** Maps diagram signatures to the respective lists of names of declared constants, types and variables */
	private HashMap<String, StringList> declaredStuff = new HashMap<String, StringList>(); 
	// END KGU#376/KGU#388 2017-09-26
    
	// START KGU#236 2016-08-10: Issue #227: Find out whether there are I/O operations
	// START KGU#236 2016-12-22: Issue #227: root-specific analysis needed
//	protected boolean hasOutput = false;
//	protected boolean hasInput = false;
//	protected boolean hasEmptyInput = false;
	private boolean hasOutput = false;
	private boolean hasInput = false;
	private boolean hasEmptyInput = false;
	private Set<Root> rootsWithOutput = new HashSet<Root>();
	private Set<Root> rootsWithInput = new HashSet<Root>();
	private Set<Root> rootsWithEmptyInput = new HashSet<Root>();
	// END KGU#236 2016-12-22
	// END KGU#236 2016-08-10
	// START KGU#311 2016-12-22: Enh. #314 - File API support
	/** Flag to indicate whether routines of the Structorizer File API are used */
	protected boolean usesFileAPI = false;
	// END KGU#311 2016-12-22
	// START KGU#348 2017-02-19: Support for translation of Parallel elements
	/** Flag to indicate whether the diagram contains Parallel elements */
	protected boolean hasParallels = false;
	// END KGU#348 2017-02-19
	// START KGU#686 2019-03-21: Enh. #56
	protected boolean hasTryBlocks = false;
	// END KGU#686 2019-03-21
	// START KGU#424 2017-09-25: We introduce a source mapping for declaration comments
	/** Maps declared names (variable, constants, types) per Root to originating Elements */
	protected HashMap<Root, HashMap<String, Instruction>> declarationCommentMap = new HashMap<Root, HashMap<String, Instruction>>();
	/** holds the Instruction the previous declaration comment was taken from */
	protected Instruction lastDeclSource = null;
	// END KGU#424 2017-09-25

	// START KGU#129/KGU#61 2016-03-22: Bugfix #96 / Enh. #84 Now important for most generators
	/**
	 * List of the names of (initialized) variables in the exported diagram
	 * Some generators must prefix variables, for some generators it's important e.g. for FOR-IN loops
	 */
	protected StringList varNames = new StringList();
	// END KGU#129/KGU#61 2015-01-22
	// START KGU 2016-03-29: For keyword detection improvement
	private Vector<StringList> splitKeywords = new Vector<StringList>();
	// END KGU 2016-03-29
	// START KGU#446 2017-10-27: Enh. #441
	/** Flag to remember whether Turtleizer routine calls are in the code (to prepare support if possible) */
	protected boolean usesTurtleizer = false;
	protected int includeInsertionLine = -1;
	// END KGU#446 2017-10-27
	// START KGU#501 2018-02-22: Bugfix #517
	private boolean includeInitialisation;
	protected boolean isInitializingIncludes() {
		return this.includeInitialisation;
	}
	// END KGU#501 2018-02-22
	// START KGU#607 2018-10-30: Enh. 346
	/** A list of generator-induced includes to be intersected or united with the configured user includes */
	protected StringList generatorIncludes = new StringList();
	// END KGU#607 2018-10-30
	
	/************ Abstract Methods *************/
	// START KGU#484 2018-03-22: Issue #463
	protected Logger getLogger()
	{
		if (this.logger == null) {
			this.logger = Logger.getLogger(getClass().getName());
		}
		return this.logger;
	}
	// END KGU#484 2018-03-22
	/**
	 * Should provide a string to be used as title for the export FileChooser
	 * dialog, e.g. something like "Export Pascal Code ..."
	 * @see #getFileDescription()
	 * @see #getFileExtensions()
	 * @return Title string for FilChooser dialog
	 */
	protected abstract String getDialogTitle();
	
	/**
	 * Is to return a short describing text of the source file type (for the
	 * FileChooser).
	 * @see #getDialogTitle()
	 * @see #getFileExtensions()
	 * @return Short file type description
	 */
	protected abstract String getFileDescription();
	
	/**
	 * Returns an array of fle name extensions typical for source files of the
	 * target language, used for the file filter (FileChooser).
	 * @return Array of extensions (without dot!)
	 */
	protected abstract String[] getFileExtensions();

	/**
	 * Returns a string representing one indentation unit for the target code
	 * @return code-style conform indentation string (on level) 
	 */
	protected abstract String getIndent();
		
//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//	/**
//	 * Returns a list of the most important reserved words in the target language.
//	 * These aren't relevant for he code export itself but fo the Analyser, if it
//	 * is to advise against the use of them for naming variables.
//	 * @see #isCaseSignificant()
//	 * @return collection of key strings
//	 */
//	@Deprecated
//	public abstract String[] getReservedWords();
//	
//	/**
//	 * Indicates whether case is significant in parsing of reserved words and
//	 * identifiers.
//	 * @see #getReservedWords()
//	 * @return true if case matters
//	 */
//	public abstract boolean isCaseSignificant();
//	// END KGU 2016-08-12
	
	// START KGU 2015-10-18: It seemed sensible to store the comment specification permanently
	/**
	 * Left delimiter of a both-end delimited or line comment
	 * @see #commentSymbolRight()
	 * @return left comment delimiter, e.g. "/*", "//", "(*", or "{"
	 */
	protected abstract String commentSymbolLeft();
	/**
	 * Right delimiter of a both-end delimited comment. In case commentSymbolLeft()
	 * returns a line-comment symbol, then here should the epty string be returned
	 * (the default).
	 * @see #commentSymbolLeft()
	 * @return right comment delimiter if required, e.g. "*\/", "}", "*)"
	 */
	protected String commentSymbolRight() { return ""; }
	// END KGU 2015-10-18
	
	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/**
	 * A pattern how to embed the variable (right-hand side of an input instruction)
	 * into the target code. Depending on the argument value, either a pattern for
	 * prompt output plus variable input or just for the latter is to be returned.<br/>
	 * In case {@code withPrompt} is false, placeholder $1 reserves the position for the
	 * variable name.<br/>
	 * In case {@code withPrompt} is true, placeholder $1 reserves the position for the
	 * prompt string and $2 is the placeholder for the variable name.
	 * @see #getOutputReplacer()
	 * @param withPrompt - is a prompt string to be considered?
	 * @return a regex replacement pattern, e.g. {@code "$1 = (new Scanner(System.in)).nextLine();"}
	 */
	// START KGU#281 2016-10-15: Enh. #271
	//protected abstract String getInputReplacer();
	protected abstract String getInputReplacer(boolean withPrompt);
	// END KGU#281 2016-10-15

	/**
	 * A pattern how to embed the expression (right-hand side of an output instruction)
	 * into the target code. The placeholder $1 marks the position where to insert the
	 * expression list (assumed to be separated with comma).
	 * @see #getInputReplacer(boolean)
	 * @return a string similar to a regex replacement pattern (but without duplicated
	 * backslashes!), e.g. {@code "System.out.println($1);"}
	 */
	protected abstract String getOutputReplacer();
	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/**
	 * Specifies whether an instruction to leave the innermost enclosing loop (like "break;" in C)
	 * is available in the target language AND ALSO BREAKS CASE SELECTIONS (switch constructs).
	 * 
	 * @return true if and only if there is such an instruction
	 */
	protected abstract boolean breakMatchesCase();
	// END KGU#78 2015-12-18
	
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * Returns the pattern of the target-language specific include/import/uses phrase
	 * for user-specific file includes with a "%" character as placeholder for the
	 * single file name or the substring "%%" if a comma-separated list is allowed.
	 * @return String to be inserted in order to load a user-specific include file or null
	 */
	protected abstract String getIncludePattern();
	// END KGU#351 2017-02-26
	
	// START KGU#371 2019-03-07: Enh. #385
	/**
	 * @return The level of subroutine overloading support in the target language
	 */
	protected abstract OverloadingLevel getOverloadingLevel();
	// END KGU#371 2019-03-07
	
	// START KGU#686 2019-03-18: Enh. #56
	/**
	 * Specifies the degree of availability of a try-catch-finally construction
	 * and the corresponding throw mechanism in the target language.
	 * @return either {@link TryCatchSupportLevel#TC_NO_TRY} or {@link TryCatchSupportLevel#TC_TRY_CATCH},
	 * or {@link TryCatchSupportLevel#TC_TRY_CATCH_FINALLY}
	 */
	protected abstract TryCatchSupportLevel getTryCatchLevel();
	// END KGU#686 2019-0318

	// START KGU#366 2017-03-10: Bugfix #378: Allow annotations of the charset
	/**
	 * Returns the currently configured character set name for the file export
	 * @return name of the character set
	 */
	protected String getExportCharset() {
		return this.exportCharset;
	}
	// END KGU#366 2017-03-10
	
	/************ Code Generation **************/
	
	// START KGU#16 2015-12-18: Enh. #66 - Code style option for opening brace placement
	/**
	 * Returns the negated value of the export option "Put block-opening braces on same
	 * line", which is relevant for all C-like languages.
	 * @return true if the left block brace should be placed in the NEXT line. 
	 */
	protected boolean optionBlockBraceNextLine() {
		// START KGU 2016-04-04: Issue #151 - Get rid of the inflationary eod threads
		//return (!eod.bracesCheckBox.isSelected());
		return (this.startBlockNextLine);
		// END KGU 2016-04-04
	}
	// END KGU#16 2015-12-18	
	
	// START KGU#113 2015-12-18: Enh. #67 - Line numbering for BASIC export
	/**
	 * Returns the value of the export option for languages BASIC (and possibly COBOL)
	 * whether to generate line numbers at the beginning of every single line. The way
	 * how to generate these numbers is completely the task of the inheriting generator.
	 * @return true if lines are to start with numbers.
	 */
	protected boolean optionCodeLineNumbering() {
		// START KGU 2016-04-04: Issue #151 - Get rid of the inflationary eod threads
		//return (eod.lineNumbersCheckBox.isSelected());
		return this.generateLineNumbers;
		// END KGU 2016-04-04
	}
	// END KGU#113 2015-12-18	
	
	// START KGU#178 2016-07-19: Enh. #160 - recursive implication of subroutines
	/**
	 * Returns the value of the export option recursively to involve all available
	 * subroutines (i.e. other diagrams) called by the diagrams to be exported. 
	 * @return true if subroutines are to be implicated.
	 */
	protected boolean optionExportSubroutines() {
		return this.exportSubroutines;
	}
	// END KGU#178 2016-07-19	
	
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * Returns the (usually comma-separated) list of configured header, library,
	 * module, or unit names, for which language-specific include / import / uses
	 * directives are to be inserted into the exported source files. It's up to the
	 * inheriting generator class to provide a sensible include pattern if is to make
	 * use of the default insertion routine insertUserIncludes(String).
	 * @see #getIncludePattern()
	 * @see #insertUserIncludes(String) 
	 * @return String with configured include items (empty string if nothing specified)
	 */
	protected String optionIncludeFiles()
	{
		return this.includeFiles;
	}
	// END KGU#351 2017-02-26

	// START KGU#395 2017-05-11: Enh. #357 - source format option for COBOL export
	/**
	 * Returns the value of the export option whether free source file format may
	 * be used (chiefly for COBOL).
	 * @return true if free file format is to be used.
	 */
	protected boolean optionExportLicenseInfo() {
		return this.exportAuthorLicense;
	}
	// END KGU#395 2017-05-11
	
	/**
	 * Returns a Generator-specific option value if available (otherwise null)
	 * @param _optionName - option key, to be combined with the generator class name
	 * @param _defaultValue - a (situative) default value 
	 * @return an Object (of the type specified in the plugin) or _defaultValue
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
	 * Allows to set a plugin-specified option before the code generation starts
	 * @param _optionName - a key string
	 * @param _value - an object according to the type specified in the plugin
	 */
	@Override
	public void setPluginOption(String _optionName, Object _value)
	{
		String fullKey = this.getClass().getSimpleName()+"."+_optionName;
		this.optionMap.put(fullKey, _value);
	}
	
	// START KGU#236 2016-12-22: Issue #227: root-specific analysis needed
	/**
	 * Returns true if in the diagram referred by _root there are output instructions
	 * (this is automatically detected before the actual export begins).
	 * This can e.g. be used to insert special include directives if needed.
	 * @see #hasOutput()
	 * @see #getOutputReplacer()
	 * @see #hasInput(Root)
	 * @see #hasEmptyInput(Root)
	 * @param _root - the interesting diagram
	 * @return true iff output instructions are contained
	 */
	protected boolean hasOutput(Root _root)
	{
		return rootsWithOutput.contains(_root);
	}
	
	/**
	 * Returns true if in the diagram referred by _root there are input instructions
	 * (this is automatically detected before the actual export begins).
	 * This can e.g. be used to insert special include directives if needed.
	 * @see #hasInput()
	 * @see #getInputReplacer()
	 * @see #hasEmptyInput(Root)
	 * @see #hasOutput(Root)
	 * @param _root - the interesting diagram
	 * @return true iff input instructions are contained
	 */	
	protected boolean hasInput(Root _root)
	{
		return rootsWithInput.contains(_root);
	}
	/**
	 * Returns true if in the diagram referenced by _root there are empty
	 * input instructions, i.e. instructions merely waiting for user acknowledge
	 * (this is automatically detected before the actual export begins).
	 * This can e.g. be used to insert special include directives if needed.
	 * @see #hasEmptyInput()
	 * @see #getInputReplacer()
	 * @see #hasInput(Root)
	 * @see #hasOutput(Root)
	 * @param _root - the interesting diagram
	 * @return true iff empty input instructions are contained
	 */	
	protected boolean hasEmptyInput(Root _root)
	{
		return rootsWithEmptyInput.contains(_root);
	}
	/**
	 * Returns true if in any of the diagrams to be exported there are output
	 * instructions (this is automatically detected before the actual export
	 * begins).
	 * This can e.g. be used to insert special include directives if needed.
	 * @see #hasOutput(Root)
	 * @see #getOutputReplacer()
	 * @see #hasInput()
	 * @see #hasEmptyInput()
	 * @return true iff output instructions are contained
	 */
	protected boolean hasOutput()
	{
		return !rootsWithOutput.isEmpty();
	}
	/**
	 * Returns true if in any of the diagrams to be exported there are input
	 * instructions (this is automatically detected before the actual export
	 * begins).
	 * This can e.g. be used to insert special include directives if needed.
	 * @see #hasInput(Root)
	 * @see #getInputReplacer()
	 * @see #hasEmptyInput()
	 * @see #hasOutput()
	 * @return true iff input instructions are contained
	 */
	protected boolean hasInput()
	{
		return !rootsWithInput.isEmpty();
	}
	/**
	 * Returns true if in any of the diagrams to be exported there are empty
	 * instructions, i.e. those without variable, just intended to wait for
	 * the user's attention (this is automatically detected before the actual
	 * export begins).
	 * This can e.g. be used to insert special include directives if needed.
	 * @see #hasEmptyInput(Root)
	 * @see #getInputReplacer()
	 * @see #hasInput()
	 * @see #hasOutput()
	 * @return true iff input instructions are contained
	 */
	protected boolean hasEmptyInput()
	{
		return !rootsWithEmptyInput.isEmpty();
	}
	// END KGU#236 2016-12-22

	// START KGU#376 2017-09-26: Enh. #389 - with includable diagrams, there might be several references
	/**
	 * Checks whether the given {@code _id} has already been defined by one of the diagrams
	 * included by {@code _root} or this diagram itself.
	 * If not and {@code _setDefindIfNot} is true then registers the {@code _id} with {@code _root}
	 * in {@link #declaredStuff}.
	 * @param _root - the currently exported Root
	 * @param _id - the name of a constant, variable, or type (in the latter case prefixed with ':')
	 * @param _setDefinedIfNot - whether the name is to be registered for {@code _root} now if not
	 * @return true if there had already been a definition before
	 */
	protected boolean wasDefHandled(Root _root, String _id, boolean _setDefinedIfNot)
	{
		String signature = _root.getSignatureString(false);
		StringList definedIds = this.declaredStuff.get(signature);
		boolean handled = definedIds != null && definedIds.contains(_id);
		if (_root.includeList != null) {
			for (int i = 0; !handled && i < _root.includeList.count(); i++) {
				String inclName = _root.includeList.get(i);
				if ((definedIds  = this.declaredStuff.get(inclName)) != null) {
					handled = definedIds.contains(_id);
				}
			}
		}
		// The topLevel restriction for includables is here because only definitions of includables
		// introduced at top level may be regarded as overall available. Usually, the declarations
		// of all includables are inserted at top level.
		if (!handled && (topLevel || !_root.isInclude()) && _setDefinedIfNot) {
			setDefHandled(signature, _id);
		}
		return handled;
	}
	// END KGU#376 2017-09-26

	/**
	 * Registers the declaration of entity {@code _id} as handled in the code for the {@link Root}
	 * with signature {@code _signature}. Returns whether the 
	 * @param _signature
	 * @param _id
	 */
	protected void setDefHandled(String _signature, String _id) {
		StringList definedIds;
		if ((definedIds = this.declaredStuff.get(_signature)) != null) {
			definedIds.addIfNew(_id);
		}
		else {
			this.declaredStuff.put(_signature, StringList.getNew(_id));
		}
	}

	// KGU 2014-11-16: Method renamed (formerly: insertComment)
	// START KGU 2015-11-18: Method parameter list reduced by a comment symbol configuration
	/**
	 * Inserts the text of {@code _element} as comments into the code, using delimiters
	 * {@link #commentSymbolLeft()} and {@link #commentSymbolRight()} (if given) to enclose
	 * the comment lines, with indentation {@code _indent}.
	 * @see #insertComment(Element, String)
	 * @see #insertComment(String, String)
	 * @see #insertComment(StringList, String)
	 * @see #insertBlockComment(StringList, String, String, String, String)
	 * @see #addCode(String, String, boolean) 
	 * @param _element current NSD element
	 * @param _indent indentation string
	 */
	protected boolean insertAsComment(Element _element, String _indent)
	{
		// START KGU#173 2016-04-04: Issue #151 - Get rid of the inflationary ExportOptionDialoge threads
		//if(eod.commentsCheckBox.isSelected()) {
		if (this.exportAsComments) {
		// END KGU#173 2016-04-04
			insertComment(_element.getText(), _indent);
			return true;
		}
		return false;
	}

	/**
	 * Inserts the comment part of _element into the code, using delimiters this.commentSymbolLeft
	 * and this.commentSymbolRight (if given) to enclose the comment lines, with indentation _indent
	 * @see #insertAsComment(Element, String)
	 * @see #insertComment(String, String)
	 * @see #insertComment(StringList, String)
	 * @see #insertBlockComment(StringList, String, String, String, String)
	 * @see #addCode(String, String, boolean) 
	 * @param _element current NSD element
	 * @param _indent indentation string
	 */
	protected void insertComment(Element _element, String _indent)
	{
		this.insertComment(_element.getComment(), _indent);
	}

	/**
	 * Inserts the given String as single comment line to the exported code
	 * @see #insertComment(Element, String)
	 * @see #insertAsComment(Element, String)
	 * @see #insertComment(StringList, String)
	 * @see #insertBlockComment(StringList, String, String, String, String)
	 * @see #addCode(String, String, boolean) 
	 * @param _text - the text to be inserted as comment
	 * @param _indent - indentation string
	 */
	protected void insertComment(String _text, String _indent)
	{
		String[] lines = _text.split("\n");
		for (int i = 0; i < lines.length; i++)
		{
			code.add(_indent + commentSymbolLeft() + " " + lines[i] + " " + commentSymbolRight());
		}
	}

	/**
	 * Inserts all lines of the given StringList as a series of single comment lines to the exported code
	 * @see #insertComment(Element, String)
	 * @see #insertAsComment(Element, String)
	 * @see #insertComment(String, String)
	 * @see #insertBlockComment(StringList, String, String, String, String)
	 * @see #addCode(String, String, boolean) 
	 * @param _sl - the text to be inserted as comment
	 * @param _indent - indentation string
	 */
	protected void insertComment(StringList _sl, String _indent)
	{
		for (int i = 0; i < _sl.count(); i++)
		{
			// The following splitting is just to avoid empty comment lines and broken
			// comment lines (though the latter shouldn't be possible here)
			String commentLine = _sl.get(i);
			// Skip an initial empty comment line
			if (i > 0 || !commentLine.isEmpty()) {
				insertComment(commentLine, _indent);
			}
		}
	}
	
	/**
	 * Inserts a multi-line comment with configurable comment delimiters for the starting line, the
	 * continuation lines, and the trailing line.
	 * @see #insertComment(Element, String)
	 * @see #insertAsComment(Element, String)
	 * @see #insertComment(String, String)
	 * @see #insertComment(StringList, String)
	 * @see #addCode(String, String, boolean) 
	 * @param _sl - the StringList to be written as commment
	 * @param _indent - the basic indentation 
	 * @param _start - comment symbol for the leading comment line (e.g. "/**"; omitted if being null)
	 * @param _cont - comment symbol for the continuation lines (e.g. " *")
	 * @param _end - comment symbol for trailing line (e.g. " *"+"/"; if null then no trailing line is generated)
	 */
	protected void insertBlockComment(StringList _sl, String _indent, String _start, String _cont, String _end)
	{
		// START KGU#199 2016-07-07: Precaution against enh. #188 (multi-line StringList elements)
		_sl = StringList.explode(_sl,  "\n");
		// END KGU#199 2016-07-07
		if (_start != null)
		{
			code.add(_indent + _start);
		}
		for (int i = 0; i < _sl.count(); i++)
		{
			// The following splitting is just to avoid empty comment lines and broken
			// comment lines (though the latter shouldn't be possible here)
			String commentLine = _sl.get(i);
			// Skip an initial empty comment line
			if (i > 0 || !commentLine.isEmpty()) {
				code.add(_indent + _cont + commentLine);
			}
		}
		if (_end != null)
		{
			code.add(_indent + _end);
		}
	}
	// END KGU 2015-10-18

	// START KGU#607 2018-10-30: (Issue #346)
	/**
	 * This is a service method inheriting generators may call at the appropriate
	 * position in order to insert include (or import or uses etc.) directives
	 * the generator regards as necessary and had enqueued in {@link #generatorIncludes}.<br/>
	 * If user-configured include items have already been inserted in the code then
	 * argument {@code skipUserIncludes} should be set true in oder to skip them here.
	 * Otherwise the argument should be set false lest items of the itersection of both
	 * sets should be omitted by both this method and {@link #insertUserIncludes(String)}.<br/>
	 * The method calls a subclassable method {@link #prepareIncludeItem(String)}
	 * (empty at {@link Generator} level) for every configured item before the
	 * insertion takes place - if some pre-processing of the items is necessary
	 * then the generator subclass ought to override the preparation method.<br/>
	 * @param _indent - current indentation string
	 * @param skipUserIncludes - if user includes have already been added to the code
	 * and are not to be repeated inadvertently here.
	 * @return number of inserted lines
	 * @see #getIncludePattern()
	 * @see #prepareIncludeItem(String)
	 * @see #optionIncludeFiles()
	 * @see #insertUserIncludes(String, boolean)
	 * @see #generatorIncludes
	 */
	protected int insertGeneratorIncludes(String _indent, boolean skipUserIncludes)
	{
		int nInserted = 0;
		String pattern = this.getIncludePattern();
		HashSet<String> userIncludes = new HashSet<String>();
		if (skipUserIncludes) {
			for (String item: this.optionIncludeFiles().split(",")) {
				userIncludes.add(item.trim());
			}
		}
		for (int i = 0; i < this.generatorIncludes.count(); i++) {
			String incl = this.generatorIncludes.get(i);
			if (!userIncludes.contains(incl)) {
				code.add(_indent + pattern.replace("%", prepareIncludeItem(incl)));
				nInserted++;
			}
		}
		return nInserted;
	}
	// END KGU#607 2018-10-30
	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * This is a service method inheriting generators may call at the appropriate
	 * position in order to insert include (or import or uses etc.) directives
	 * for the include items configured in the export options for the respective
	 * language.<br/>
	 * Include items that have already been enqueued for code insertion by the
	 * generator itself in {@link #generatorIncludes} will be spared here.<br/>
	 * The method calls a subclassable method {@link #prepareIncludeItem(String)}
	 * (empty at {@link CodeGnerator} level) for every item configured before the
	 * insertion takes place - if some pre-processing of the items is necessary
	 * then the generator subclass may override this method.<br/>
	 * The configured list of include items may also be retrieved directly via
	 * method {@link #optionIncludeFiles()} and then be processed individually.
	 * @param _indent - indentation for the directives
	 * @return number of inserted lines
	 * @see #getIncludePattern()
	 * @see #prepareIncludeItem(String)
	 * @see #optionIncludeFiles()
	 * @see #insertGeneratorIncludes(String, boolean)
	 * @see #generatorIncludes
	 */
	protected int insertUserIncludes(String _indent)
	{
		int nInserted = 0;
		String pattern = this.getIncludePattern();
		String includes = this.optionIncludeFiles().trim();
		if (pattern != null && includes != null && !includes.isEmpty()) {
			// START KGU#607 2018-10-30: Issue #346 - Avoid duplicate includes
			StringList items = new StringList(includes.split(","));
			for (int i = items.count()-1; i >= 0; i--) {
				String item = items.get(i).trim();
				if (this.generatorIncludes.contains(item)) {
					items.remove(i);
				}
			}
			// END KGU#607 2018-10-30
			// Collective (enumerative) include phrase available?
			if (pattern.contains("%%")) {
				// START KGU#607 2018-10-30: Issue #346
				//code.add(_indent + pattern.replace("%%", includes));
				code.add(_indent + pattern.replace("%%", items.concatenate(",")));
				nInserted++;
				// END KGU#607 2018-10-30
			}
			// .. otherwise produce a single line for every item
			else if (pattern.contains("%")) {
				for (int i = 0; i < items.count(); i++) {
					String item = items.get(i).trim();
					if (!item.isEmpty()) {
						code.add(_indent + pattern.replace("%", prepareIncludeItem(item)));
						nInserted++;
					}
				}
			}
		}
		return nInserted;
	}
	/**
	 * Method may pre-process an include file or module name for the import / use
	 * clause. This version is called by {@link #insertUserIncludes(String)} and does nothing but
	 * may be overridden. 
	 * @see #getIncludePattern()
	 * @see #optionIncludeFiles()
	 * @see #insertUserIncludes(String)
	 * @param _includeFileName a string from the user include configuration
	 * @return the preprocessed string as to be actually inserted
	 */
	protected String prepareIncludeItem(String _includeFileName)
	{
		return _includeFileName;
	}
	// END KGU#351 2017-02-26

	// START KGU#277 2016-10-13: Enh. #270
	/**
	 * Depending on {@code asComment}, adds the given text either as comment or as active
	 * source code to the code lines.
	 * This is a convenient wrapper for {@code this.code.add(String)}.
	 * @see #insertComment(Element, String)
	 * @see #insertAsComment(Element, String)
	 * @see #insertComment(String, String)
	 * @see #insertComment(StringList, String)
	 * @see #insertBlockComment(StringList, String, String, String, String)
	 * @param text - the prepared (transformed and composed) line of code
	 * @param _indent - current indentation
	 * @param asComment - whether or not the code is to be commented out.
	 */
	protected void addCode(String text, String _indent, boolean asComment)
	{
		if (asComment)
		{
			// Indentation is intentionally put inside the comment (comment encloses entire line)
			insertComment(_indent + text, "");
		}
		else
		{
			code.add(_indent + text);
		}
	}
	// END KGU#277 2016-10-13

	// START KGU#376/KGU#388 2017-09-25: Enh. #389, #423
	/**
	 * Tries to find the defining instruction for identifier {@code _id} within
	 * the given Root {@code _root} or one of the identified includables and
	 * appends the element comment at the current position in this case.
	 * @param _root - the currently generated Root
	 * @param _indent - the current indentation as String
	 * @param _id - the declared identifier (const, var or type)
	 */
	protected void insertDeclComment(Root _root, String _indent, String _id) {
		if (this.declarationCommentMap.containsKey(_root)) {
			Instruction srcElement = this.declarationCommentMap.get(_root).get(_id);
			if (srcElement != null && srcElement != this.lastDeclSource) {
				insertComment(srcElement, _indent);
				// One comment is enough
				this.lastDeclSource = srcElement;
				return;
			}
		}
		for (Root incl: this.includedRoots.toArray(new Root[]{})) {
			if (_root.includeList != null
					&& _root.includeList.contains(incl.getMethodName()) 
					&& this.declarationCommentMap.containsKey(incl)) {
				Instruction srcElement = this.declarationCommentMap.get(incl).get(_id);
				if (srcElement != null && srcElement != this.lastDeclSource) {
					insertComment(srcElement, _indent);
					// One comment is enough
					this.lastDeclSource = srcElement;
					return;
				}
			}
		}
	}
	// END KGU#376/KGU#388 2017-09-25

	/**
	 * Overridable general text transformation routine, performing the following steps:<br/>
	 * 1. Eliminates parser preference keywords listed below and unifies all operators
	 *    (see {@link lu.fisch.Structorizer.elements.Element#unifyOperators(java.lang.String)}).<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;"preAlt", "preCase", "preWhile", "preRepeat",<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;"postAlt", "postCase", "postWhile", "postRepeat";<br/>
	 * 2. Tokenizes the result, processes the tokens by an overridable method
	 *    {@link #transformTokens(StringList)}, and re-concatenates the result;<br/>
	 * 3. Transforms Input and Output lines according to regular replacement expressions defined
	 *    by {@link #getInputReplacer(boolean)} and {@link #getOutputReplacer()}, respectively. This is done by overridable
	 *    methods {@link #transformInput(String)} and {@link #transformOutput(String)}, respectively.
	 *    This is only done if _input starts with one of the configured Input and Output keywords<br/>
	 * Note: Of course steps 1 through 3 will only be done if the overriding method calls
	 * this parent method at some suited point.
	 * @see #transform(String, boolean)
	 * @see #transformTokens(StringList)   
	 * @see #transformInput(String)
	 * @see #transformOutput(String)
	 * @see #transformType(String, String)
	 * @see #suppressTransformation
	 * @param _input a line or the concatenated lines of an Element's text
	 * @return the transformed line (target language line)
	 */
	protected String transform(String _input)
	{
		return transform(_input, true);
	}

	/**
	 * Overridable general text transformation routine, performing the following steps:<br/>
	 * 1. Eliminates parser preference keywords listed below and unifies all operators.
	 *         "preAlt", "preCase", "preWhile", "preRepeat",
	 *         "postAlt", "postCase", "postWhile", "postRepeat";<br/>
	 * 2. Tokenizes the result, processes the tokens by an overridable method
	 *    {@link #transformTokens(StringList)}, and re-concatenates the result;<br/>
	 * 3. Transforms Input and Output lines if {@code _doInputOutput} is true.
	 *    This is only done if {@code _input} starts with one of the configured Input
	 *    and Output keywords.
	 * @see #transform(String)
	 * @see #transformTokens(StringList)   
	 * @see #transformInput(String)
	 * @see #transformOutput(String)
	 * @see #transformType(String, String)
	 * @see #suppressTransformation
	 * @see lu.fisch.Structorizer.elements.Element#unifyOperators(java.lang.String)
	 * @param _input - a line or the concatenated lines of an Element's text
	 * @param _doInputOutput - whether the third transformations are to be performed
	 * @return the transformed line (target language line)
	 */
	protected String transform(String _input, boolean _doInputOutput)
	{

		// General operator unification and dummy keyword elimination
		// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//		_input = Element.transformIntermediate(_input);
//
//		// assignment transformation
//		_input = transformAssignment(_input);
		
		// START KGU#162 2016-03-31: Enh. #144
		//StringList tokens = Element.transformIntermediate(_input);
		StringList tokens = null;
		if (this.suppressTransformation)
		{
			// Suppress all syntax changes, just split to tokens.
			tokens = Element.splitLexically(_input, true);
			Element.cutOutRedundantMarkers(tokens);
		}
		else
		{
			// convert to tokens into a common intermediate language
			tokens = Element.transformIntermediate(_input);
		}
		// END KGU#162 2016-03-31
		
		// START KGU 2016-03-29: Unify all parser keywords
		// This is somewhat redundant because most of the keywords have already been cut out
		// but it's still needed for the meaningful ones.
		String[] keywords = CodeParser.getAllProperties();
		for (int kw = 0; kw < keywords.length; kw++)
		{    				
			if (keywords[kw].trim().length() > 0)
			{
				StringList keyTokens = this.splitKeywords.elementAt(kw);
				int keyLength = keyTokens.count();
				int pos = -1;
				while ((pos = tokens.indexOf(keyTokens, pos + 1, !CodeParser.ignoreCase)) >= 0)
				{
					// Replace the first token of the keyword by the entire keyword
					tokens.set(pos, keywords[kw]);
					// Remove the remaining tokens of the split keyword
					for (int j=1; j < keyLength; j++)
					{
						tokens.delete(pos+1);
					}
				}
			}
		}
		// END KGU 2016-03-29
		// START KGU#162 2016-03-31: Enh. #144
		//String transformed = transformTokens(tokens);
		String transformed = "";
		if (this.suppressTransformation) {
			// Just re-concatenate the tokens if no conversion is wanted
			transformed = tokens.concatenate();
		}
		else {
			transformed = transformTokens(tokens);
		}
		// END KGU#162 2016-03-31
		// END KGU#93 2015-12-21

		if (_doInputOutput)
		{
			// START KGU 2015-12-22: Avoid unnecessary transformation attempts
			//// input instruction transformation
			//transformed = transformInput(transformed);
			//// output instruction transformation
			//transformed = transformOutput(transformed);
			if (transformed.indexOf(CodeParser.getKeyword("input").trim()) >= 0)
			{
				transformed = transformInput(transformed);
			}
			else if (transformed.indexOf(CodeParser.getKeyword("output").trim()) >= 0)
			{
				transformed = transformOutput(transformed);
			}
			// END KGU 2015-12-22
		}

		return transformed.trim();
	}
	
	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
	/**
	 * Transforms operators and other tokens from the given intermediate
	 * language into tokens of the target language and returns the result
	 * as string.<br/>
	 * OVERRIDE this! (Method just returns the re-concatenated tokens)
	 * This method is called by {@link #transform(String, boolean)} but may
	 * also be used elsewhere for a specific token list.
	 * @see #transform(String, boolean)
	 * @see #transformInput(String)
	 * @see #transformOutput(String)
	 * @see #transformType(String, String)
	 * @see #suppressTransformation
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed string
	 */
	protected String transformTokens(StringList tokens)
	{
		return tokens.concatenate();
	}
	
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * OVERRIDE this! (Method just returns _interm without changes)
//	 * @param _interm - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm)
//	{
//		return _interm;
//	}
	// END KGU#93 2015-12-21
	
	// START KGU#16 2015-11-30
	/**
	 * Transforms type identifier into the target language (as far as possible).
	 * Is to be overridden by the Generator subclasses if typing is an issue.
	 * Method is called e.g. by {@link #getTransformedTypes(TypeMapEntry, boolean)} and
	 * in other contexts.
	 * Note: This method does not perform a type map retrieval!
	 * @see #getTransformedTypes(TypeMapEntry, boolean)
	 * @see #transform(String, boolean)
	 * @see #transformTokens(StringList)
	 * @see #transformInput(String)
	 * @see #transformOutput(String)
	 * @see #suppressTransformation
	 * @param _type - a string potentially meaning a datatype (or null)
	 * @param _default - a default string returned if _type happens to be null
	 * @return a type identifier (or the unchanged _type value if matching failed)
	 */
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		return _type;
	}
	// END KGU#16 2015-11-30
	
	// START KGU#388 2017-09-26: Enh. #423
	/**
	 * Creates a type description for the target language from the given
	 * TypeMapEntry {@code typeInfo}.
	 * For the case a special treatment might be necessary within a nested type
	 * definition, the enclosing TypeInfo may be given as {@code definingType}
	 * @param typeInfo - the defining or derived TypeMapInfo of the type 
	 * @param definingType - the enclosing type just being defined or null
	 * @return a String suited as type description in declarations etc. of the target language 
	 */
	protected String transformTypeFromEntry(TypeMapEntry typeInfo, TypeMapEntry definingType) {
		// Just a dummy, to be overridden by subclasses
		return typeInfo.getCanonicalType(true, true);
	}
	// END KGU#388 2017-09-26
	
	// START KGU#261 2017-01-26: Enh. #259/#335
	protected StringList getTransformedTypes(TypeMapEntry typeEntry, boolean preferName)
	{
		// START KGU#388 2017-09-19: Enh. #423
		//StringList types = typeEntry.getTypes();
		StringList types;
		if (preferName && typeEntry.isNamed()) {
			types = StringList.getNew(typeEntry.typeName);
		}
		else {
			types = typeEntry.getTypes();
		}
		// END KGU#388 2017-09-19
		StringList transTypes = new StringList();
		for (int i = 0; i < types.count(); i++) {
			String type = types.get(i);
			int posLastAt = type.lastIndexOf('@')+1;
			type = type.substring(0, posLastAt) + transformType(type.substring(posLastAt), "???");
			transTypes.addIfNew(type);
		}
		// Get rid of completely undefined types
		transTypes.removeAll("???");
		return transTypes;
	}
	// END KGU#261 2017-01-26
	
	// START KGU#653 2019-02-14: Enh. #680
	/**
	 * Subclassable method possibly to obtain a suited transformed argument list string for the given series of
	 * input items (i.e. expressions designating an input target variable each) to be inserted in the input replacer
	 * returned by {@link #getInputReplacer(boolean)}, this allowing to generate a single input instruction only.<br/>
	 * This dummy implementation returns just null, meaning there is no such conversion in general (such that several
	 * input instructions must be generated. Subclasses the target language of which allows multi-variable input
	 * instructions should override this.
	 * @param _inputVarItems - {@link StringList} of variable descriptions for input
	 * @return either a syntactically converted combined string with suited operator or separator symbols, or null.
	 */
	protected String composeInputItems(StringList _inputVarItems)
	{
		return null;
	}
	// END KGU#653 2019-02-14
	
	/**
	 * Detects whether the given code line starts with the configured input keystring
	 * and if so replaces it according to the regex pattern provided by
	 * {@link #getInputReplacer(boolean)}.
	 * @see #getInputReplacer(boolean)
	 * @see #transformOutput(String)
	 * @see #transform(String, boolean)
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed input instruction or _interm unchanged
	 */
	protected String transformInput(String _interm)
	{
		// START KGU#281 2016-10-15: for enh. #271 (input with prompt)
		//String subst = getInputReplacer();
		// END KGU#281 2016-10-15
		// Between the input keyword and the variable name there MUST be some blank...
		String keyword = CodeParser.getKeyword("input").trim();
		// START KGU#399 2017-05-16: bugfix #403
		//if (!keyword.isEmpty() && _interm.startsWith(keyword))
		String gap = (!keyword.isEmpty() && Character.isJavaIdentifierPart(keyword.charAt(keyword.length()-1)) ? "[\\W]" : "");
		String pattern = "^" + getKeywordPattern(keyword) + "(" + gap + ".*|$)";
		if (!keyword.isEmpty() && _interm.matches(pattern))
		// END KGU#399 2017-05-16
		{
			// START KGU#281 2016-10-15: for enh. #271 (input with prompt)
			String quotes = "";
			// START KGU#399 2017-05-16: bugfix #403
			//String tail = _interm.substring(keyword.length()).trim();
			String tail = _interm.replaceFirst(pattern, "$1").trim();
			// END KGU#399 2017-05-16
			if (tail.startsWith("\"")) {
				quotes = "\"";
			}
			else if (tail.startsWith("'")) {
				quotes = "'";
			}
			// END KGU#281 2016-10-15
			// START KGU#399 2017-05-16: Bugfix #403
			//String matcher = Matcher.quoteReplacement(keyword);
			//if (Character.isJavaIdentifierPart(keyword.charAt(keyword.length()-1)))
			//{
			//	matcher = matcher + "[ ]";
			//}
			//
			// Start - BFI (#51 - Allow empty input instructions)
			//if(!_interm.matches("^" + matcher + "(.*)"))
			//{
			//	_interm += " ";
			//}
			// End - BFI (#51)
			// END KGU#399 2017-05-16
			
			// START KGU#281 2016-10-15: Enh. #271 (input instructions with prompt
			//_interm = _interm.replaceFirst("^" + matcher + "(.*)", subst);
			if (quotes.isEmpty()) {
				String subst = getInputReplacer(false);
				// START KGU#399 2017-05-16: bugfix #51, #403
				//_interm = _interm.replaceFirst("^" + matcher + "[ ]*(.*)", subst);
				// START KGU#653 2019-02-14: Enh. #680
				//_interm = _interm.replaceFirst(pattern, subst);
				_interm = subst.replace("$1", tail);
				// END KGU#653 3019-02-14
				// END KGU#399 2017-05-16
			}
			else {
				String subst = getInputReplacer(true);
				// START KGU#399 2017-05-16: bugfix #51, #403				
				//_interm = _interm.replaceFirst("^" + matcher + "\\h*("+quotes+".*"+quotes+")[, ]*(.*)", subst);
				// START KGU#653 2019-02-14: Enh. #680
				//pattern = "^" + getKeywordPattern(keyword) + "\\h*("+quotes+".*"+quotes+")[,]?\\s*(.*)";
				//_interm = _interm.replaceFirst(pattern, subst);
				pattern = "^" + "\\h*("+quotes+".*"+quotes+")[,]?\\s*(.*)";
				_interm = tail.replaceFirst(pattern, subst);
				// END KGU#653 2019-02-14
				// END KGU#399 2017-05-16
			}
			// END KGU#281 2016-10-15
		}
		return _interm;
	}

	/**
	 * Detects whether the given code line starts with the configured output keystring
	 * and if so replaces it according to the regex pattern provided by
	 * {@link #getOutputReplacer()}.
	 * @see #getOutputReplacer()
	 * @see #transformInput(String)
	 * @see #transform(String, boolean)
	 * @param _interm - a code line in intermediate syntax
	 * @return transformed output instruction or _interm unchanged
	 */
	protected String transformOutput(String _interm)
	{
		String subst = getOutputReplacer();
		String keyword = CodeParser.getKeyword("output").trim();
		// START KGU#399 2017-05-16: bugfix #403
		//if (!keyword.isEmpty() && _interm.startsWith(keyword))
		// Between the input keyword and a variable name there must be some blank unless the keyword itself ends
		// with a blank or some non-identifier character. On the other hand, the expression might start with an
		// operator symbol or a parenthesis... We try to approach this uncertainty with the gap variable.
		// As a result, however, a superfluous blank may remain in front of the first expression.
		String gap = (!keyword.isEmpty() && Character.isJavaIdentifierPart(keyword.charAt(keyword.length()-1)) ? "[\\W]" : "");
		// START KGU#505 2018-03-13: The substitution mechanism introduced with #403 left a leading blank in the expression
		//String pattern = "^" + getKeywordPattern(keyword) + "\\s*(" + gap + ".*|$)";
		//if (!keyword.isEmpty() && _interm.matches(pattern))
		Matcher matcher = Pattern.compile("^" + getKeywordPattern(keyword) + "\\s*(" + gap + ".*|$)").matcher(_interm);
		if (!keyword.isEmpty() && matcher.matches())
		// END KGU#505 2018-03-13
		// END KGU#399 2017-05-16
		{
			// START KGU#399 201-05-16: bugfix #51, #403
			//String matcher = Matcher.quoteReplacement(keyword);
			//if (Character.isJavaIdentifierPart(keyword.charAt(keyword.length()-1)))
			//{
			//	matcher = matcher + "[ ]";
			//}
			//
			// Start - BFI (#51 - Allow empty output instructions)
			//if(!_interm.matches("^" + matcher + "(.*)"))
			//{
			//	_interm += " ";
			//}
			// End - BFI (#51)
			//
			//_interm = _interm.replaceFirst("^" + matcher + "(.*)", subst);
			// START KGU#505 2018-03-13: trim the expressions before insertion
			//_interm = _interm.replaceFirst(pattern, subst);
			_interm = subst.replace("$1", matcher.group(1).trim());
			// END KGU#505 2018-03-13
			// END KGU#399 2017-05-16
		}
		return _interm;
	}
	// END KGU#18/KGU#23 2015-11-01
	
	// START KGU#165 2016-04-03: Support keyword case sensitivity
	/**
	 * Returns an appropriate match string for the given parser preference string
	 * (where {@link CodeParser#ignoreCase} is paid attention to)
	 * @see lu.fisch.structorizer.parsers.CodeParser#getKeyword(String)
	 * @param keyword - parser preference string
	 * @return match pattern
	 */
	protected static String getKeywordPattern(String keyword)
	{
		String pattern = Matcher.quoteReplacement(keyword);
		if (CodeParser.ignoreCase)
		{
			pattern = BString.breakup(pattern);
		}
		return pattern;
	}
	// END KGU#165 2016-04-03

	// START KGU#74 2015-11-30
	/**
	 * We do a recursive analysis for loops, returns and jumps of type "leave"
	 * to be able to place equivalent goto instructions and their target labels
	 * on demand.
	 * Maps Jump instructions and Loops (as potential jump targets) to unique
	 * numbers used for the creation of unambiguous goto or break labels.
	 * 
	 * The mapping is gathered in {@link #jumpTable}.
	 * If a return instruction with value is encountered, this.returns will be set true
	 * @see #breakMatchesCase()
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @param _squeue - instruction sequence to be analysed 
	 * @return true iff there is no execution path without a value returned.
	 */
	protected boolean mapJumps(Subqueue _squeue)
	{
		boolean surelyReturns = false;
//		String preLeave  = CodeParser.getKeywordOrDefault("preLeave", "leave");
		String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
//		String preExit   = CodeParser.getKeywordOrDefault("preExit", "exit");
//		String patternLeave = getKeywordPattern(preLeave) + "([\\W].*|$)";
		String patternReturn = getKeywordPattern(preReturn) + "([\\W].*|$)";
//		String patternExit = getKeywordPattern(preExit) + "([\\W].*|$)";
		Iterator<Element> iter = _squeue.getIterator();
		while (iter.hasNext() && !surelyReturns)
		{
			Element elem = iter.next();
			// If we detect a Jump element of type leave then we detect its target
			// and label both
			if (elem instanceof Jump && !elem.isDisabled())
			{
				// START KGU#413 2017-06-09: Enh. #416: There might be line continuation
				//String jumpText = elem.getText().getLongString().trim();
				String jumpText = elem.getUnbrokenText().getLongString();
				// END KGU#413 2017-06-09
				// START KGU#380 2017-04-14: Bugfix #394 Code revision, simplification
				//if (jumpText.matches(patternReturn))
				
				Jump jump = (Jump)elem;
				if (jump.isReturn())
				// END KGU#380 2017-04-14
				{
					boolean hasResult = !jumpText.substring(preReturn.length()).trim().isEmpty();
					if (hasResult) this.returns = true;
					// Further investigation would be done in vain - the remaining sequence is redundant
					return hasResult;
				}
				// START KGU#380 2017-04-14: Bugfix #394 - Code revision, simplification
				//else if (jumpText.matches(patternExit))
				else if (jump.isExit())
				// END KGU#380 2017-04-14
				{
					// Doesn't return a regular result but we won't get to the end, so a default return is
					// not required, we handle this as if a result would have been returned.
					//surelyReturns = true;
					return true;
				}
// START KGU#380 2017-04-14: Bugfix #394 - Code revision, simplification
//				// Get the number of requested exit levels
//				int levelsUp = 0;
//				if (jumpText.isEmpty())
//				{
//					levelsUp = 1;
//				}
//				else if (jumpText.matches(patternLeave))
//				{
//					levelsUp = 1;
//					if (jumpText.length() > preLeave.length()) {
//						try {
//							levelsUp = Integer.parseInt(jumpText.substring(preLeave.length()).trim());
//						}
//						catch (NumberFormatException ex)
//						{
//							System.out.println("Unsuited leave argument in Element \"" + jumpText + "\"");
//						}
//					}
//				}
//				// Try to find the target loop
//				// START KGU#78 2015-12-18: Enh. #23 specific handling only required if there is a break instruction
//				//boolean simpleBreak = levelsUp == 1;	// For special handling of Case context
//				// Simple break instructions usually require special handling of Case context
//				boolean simpleBreak = levelsUp == 1 && this.breakMatchesCase();
//				// END KGU#78 2015-12-18
//				Element parent = elem.parent;
//				while (parent != null && !(parent instanceof Parallel) && levelsUp > 0)
//				{
//					if (parent instanceof ILoop)
//					{
//						if (--levelsUp == 0 && !simpleBreak)	// Target reached?
//						{
//							// Is target loop already associated with a label?
//							Integer label = this.jumpTable.get(parent);
//							if (label == null)
//							{
//								// If not then associate it with a label
//								label = this.labelCount++;
//								this.jumpTable.put(parent, label);
//							}
//							this.jumpTable.put(elem, label);
//						}
//					}
//					else if (parent instanceof Case)
//					{
//						// If we were within a selection (switch) then we must use "goto" to get out
//						simpleBreak = false;
//					}
//					parent = parent.parent;
//				}
//				if (levelsUp > 0)
				else if (jump.isLeave()) {
					Element targetLoop = jump.getLeftLoop(null);
					if (targetLoop != null) {
						List<Element> leftStructures = jump.getLeftStructures(null, this.breakMatchesCase(), false);
						boolean simpleBreak = leftStructures.size() == 1 && this.breakMatchesCase();
						if (!simpleBreak) {
							// Is target loop already associated with a label?
							Integer label = this.jumpTable.get(targetLoop);
							if (label == null)
							{
								// If not then associate it with a label
								label = this.labelCount++;
								this.jumpTable.put(targetLoop, label);
							}
							this.jumpTable.put(elem, label);
						}
					}
					else {
						// Target couldn't be found, so mark the jump with an error marker
						this.jumpTable.put(elem, -1);						
					}
					// After an unconditional leave further instructions at this level are redundant  
					return surelyReturns;
				}
				// START KGU#686 2019-03-18: Enh. #56 support for try / catch /throw
				else if (jump.isThrow() && this.getTryCatchLevel() != TryCatchSupportLevel.TC_NO_TRY) {
					// Doesn't return a regular result but we won't get to the end, so a default return is
					// not required, we handle this as if a result would have been returned.
					return true;
				}
				// END KGU#686 2019-03-18
				else	// No recognized jump type
// END KGU#380 2017-04-14
				{
					// Target couldn't be found, so mark the jump with an error marker
					this.jumpTable.put(elem, -1);
				}
// START KGU#380 2017-04-14: Bugfix #394 - No longer needed
//				else {
//					// After an unconditional jump, the remaining instructions are redundant
//					return surelyReturns;
//				}
// END KGU#380 2017-04-14
			}
			// No jump: then only recursively descend
			else if (elem instanceof Alternative)
			{
				boolean willReturnT = mapJumps(((Alternative)elem).qTrue);
				boolean willReturnF = mapJumps(((Alternative)elem).qFalse);
				surelyReturns = willReturnT && willReturnF;
			}
			else if (elem instanceof Case)
			{
				boolean willReturn = false;
				for (int i = 0; i < ((Case)elem).qs.size(); i++)
				{
					boolean caseReturns = mapJumps(((Case)elem).qs.elementAt(i));
					willReturn = willReturn && caseReturns;
				}
				if (willReturn) surelyReturns = true;
			}
			else if (elem instanceof ILoop)	// While, Repeat, For, Forever
			{
				if (mapJumps(((ILoop)elem).getBody())) surelyReturns = true;
			}
			else if (elem instanceof Parallel)
			{
				// There is no regular return out of a parallel thread...
				for (int i = 0; i < ((Parallel)elem).qs.size(); i++)
				{
					mapJumps(((Parallel)elem).qs.elementAt(i));
				}
			}
			else if (elem instanceof Instruction)
			{
				// START KGU#413 2017-06-09: Enh. #416
				//StringList text = elem.getText();
				StringList text = elem.getUnbrokenText();
				// END KGU#413 2017-06-09
				for (int i = 0; i < text.count(); i++)
				{
					String line = text.get(i);
					if (line.matches(patternReturn))
					{
						boolean hasResult = !line.substring(preReturn.length()).trim().isEmpty();
						if (hasResult)
						{
							this.returns = true;
							// Further investigation would be done in vain - the remaining sequence is redundant
							surelyReturns = true;
						}
					}
				}
				
			}
		}
		return surelyReturns;
	}
	
	
	// START KGU#109/KGU#141 2016-01-16: New for ease of fixing #61 and #112
	/**
	 * Decomposes the left-hand side of an assignment passed in as _lval
	 * into four strings:<br/>
	 * [0] - type specification (a sequence of tokens, may be empty)<br/>
	 * [1] - variable name (a single token supposed to be the identifier)<br/>
	 * [2] - index expression (if _lval is an indexed variable, else empty)<br/>
	 * [3] - component path (if _lval is a record component of an indexed variable, else empty)
	 * @param _lval a string found on the left-hand side of an assignment operator
	 * @return String array of [0] type, [1] name, [2] index, [3] component path; all but [1] may be empty
	 */
	protected String[] lValueToTypeNameIndexComp(String _lval)
	{
		// Avoid too much nonsense on indexed variables
		// START KGU#334 2017-01-30: Bugfix #337 - lvalue was mutilated with nested index access
		//Regex r = new Regex("(.*?)\\[(.*?)\\](.*?)","$1 $3");
		// END KGU#334 2017-01-30
		String type = "";
		String name = null;
		String index = "";
		String comp = "";
		String before = _lval;
		String after = "";
		int posL = _lval.indexOf("[");
		int posR = _lval.lastIndexOf("]");
		if (posL >= 0 && posR > posL) {
			index = _lval.substring(posL + 1, posR);
			before = _lval.substring(0, posL);
			after = _lval.substring(posR + 1);
		}
		if (after.startsWith(".") && Function.testIdentifier(after.substring(1), ".")) {
			comp = after;
			name = before;
		}
		else {
			name = (before + " " + after).trim();	// This is somewhat strange in general
		}
		// END KGU#388 2017-09-27
		// Check Pascal and BASIC style of type specifications
		int subPos = name.indexOf(":");
		if (subPos > 0)
		{
			type = name.substring(subPos + 1).trim() + " ";
			name = name.substring(0, subPos).trim();
		}
		else if ((subPos = name.toLowerCase().indexOf(" as ")) > 0)
		{
			type = name.substring(subPos + " as ".length()).trim() + " ";
			name = name.substring(0, subPos).trim();
		}
		// Now split the assumed name to check C-style type specifications
		StringList nameParts = StringList.explode(name, " ");
		if (type.isEmpty() || nameParts.count() > 1)
		{
			type = nameParts.concatenate(" ", 0, nameParts.count()-1).trim() + " ";
		}
		name = nameParts.get(nameParts.count()-1);
		//r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$2");
		// START KGU#388 2017-09-27: Enh. #423 Didn't work, since often appended a "tail"
		//String index = "";
		//if ((subPos = _lval.indexOf('[')) >= 0 && _lval.indexOf(']', subPos+1) >= 0)
		//{
		//	// START KGU#189 2016-04-29: Bugfix #337 for multidimensional array expressions
		//	// lvalues like a[i][j] <- ... had been transformed to a[ij] <- ...
		//	// Now index would become "i][j" in such a case which at least preserves syntax
		//	index = _lval.replaceAll("(.*?)[\\[](.*?)[\\]](.*?)","$2").trim();
		//	// END KGU#189 2016-04-29
		//}
		//String[] typeNameIndex = {type, name, index};
		//return typeNameIndex;
		String[] typeNameIndexPath = {type, name, index, comp};
		return typeNameIndexPath;
		// END KGU#388 2017-09-27
	}
	// END KGU#109/KGU#141 2016-01-16
	
	// START KGU#61 2016-03-23: Enh. #84 (FOR-IN loop infrastructure)
	/**
	 * In case of a FOR-IN loop tries to extract the value list items if explicitly
	 * given in the loop text (literal syntax).<br/>
	 * If the value list is represented by a variable then null will be returned instead.<br/>
	 * Utility routine that may be used in {@link #generateCode(For, String)}.
	 * @param _for - the for loop of FOR-IN style to be analysed
	 * @return a StringList where every element contains one item (as string) or null
	 */
	protected StringList extractForInListItems(For _for)
	{
		String valueList = _for.getValueList();
		StringList items = null;
		boolean isComplexObject = Function.isFunction(valueList) || this.varNames.contains(valueList);
		if (valueList.startsWith("{") && valueList.endsWith("}"))
		{
			items = Element.splitExpressionList(valueList.substring(1, valueList.length()-1), ",");
		}
		else if (valueList.contains(","))
		{
			items = Element.splitExpressionList(valueList, ",");
		}
		else if (!isComplexObject && valueList.contains(" "))
		{
			items = Element.splitExpressionList(valueList, " ");
		}
		return items;
	}
	// END KGU#61 2016-03-23
	
	// START KGU#178 2016-07-19: Enh. #160
	/**
	 * Establishes a mapping among the Root referred to be {@code _called} and
	 * the {@code _caller} Root in field {@link #subroutines}.
	 * @param _call - a CALL element found in Root {@code _caller}
	 * @param _caller - the Root containing {@code _call}
	 * @return the called Root if being available and not having been mapped before 
	 */
	protected Root registerCalled(Call _call, Root _caller)
	{
		Root newSub = null;
		Function called = _call.getCalledRoutine();
		// START KGU#349 2017-02-20: Bugfix #349 - don't register directly recursive calls
		//if (called != null && Arranger.hasInstance())
		// START KGU#371 2019-03-08: Enh. #385 cope with optional parameters
		//if (called != null && !_caller.getSignatureString(false).equals(called.getSignatureString()) && Arranger.hasInstance())
		// START KGU#676 2019-03-13: Enh. #696 Routine pool may now stem from an archive 
		//if (called != null && !(_caller.getMethodName().equals(called.getName()) && _caller.acceptsArgCount(called.paramCount()) >= 0) && Arranger.hasInstance())
		if (called != null && !(_caller.getMethodName().equals(called.getName()) && _caller.acceptsArgCount(called.paramCount()) >= 0) && routinePool != null)
		// END KGU#676 2019-03-13
		// END KGU#371 2019-03-07
		// END KGU#349 2017-02-20
		{
			// START KGU#676 2019-03-13: Enh. #696 Routine pool may now stem from an archive 
			//Vector<Root> foundRoots = Arranger.getInstance().
			//		findRoutinesBySignature(called.getName(), called.paramCount());
			Vector<Root> foundRoots = routinePool.
					findRoutinesBySignature(called.getName(), called.paramCount(), _caller);
			// END KGU#676 2019-03-13
			// FIXME: How to select among Roots with compatible signature?
			if (!foundRoots.isEmpty())
			{
				newSub = putRootsToMap(foundRoots.firstElement(), _caller, subroutines);
			}
			// START KGU#237 2016-08-10: bugfix #228
			else if ((newSub = getAmongSubroutines(called)) != null)
			{
				subroutines.get(newSub).callers.add(_caller);
				// If we got here, then it's probably the top-level routine itself
				// So better be cautious with reference counting here (lest the
				// calling routine would be suppressed on printing)
				newSub = null;	// ...and it's not a new subroutine, of course
			}
			// END KU#237 2016-08-10
			else
			{
				missingSubroutines.addIfNew(_call.getSignatureString());
			}
		}
		return newSub;
	}

	/**
	 * Establishes a mapping between Roots {@code _referred} and {@code _caller} in the given
	 * {@code _referenceMap} for later topological sorting.
	 * @param _referred - the Root being referred to by _caller
	 * @param _caller - the Root referring to {@code _referred}
	 * @param _referenceMap - the map from referred Roots to referring Roots
	 * @return the {@code _referred} Root if it hadn't been in {@code _referenceMap} before 
	 */
	private Root putRootsToMap(Root _referred, Root _caller, Hashtable<Root, SubTopoSortEntry> _referenceMap) {
		// Is there already an entry for this root?
		Root newSub = null;
		SubTopoSortEntry entry = _referenceMap.get(_referred);
		boolean toBeCounted = false;
		if (entry == null)
		{
			// No - create a new entry
			_referenceMap.put(_referred, new SubTopoSortEntry(_caller));
			newSub = _referred;
			toBeCounted = true;
		}
		else
		{
			// Yes: add the calling routine to the set of roots to be informed
			// (if not already registered)
			toBeCounted = entry.callers.add(_caller);
		}
		// Now count the call at the callers entry (if there is one)
		if (toBeCounted && (entry = _referenceMap.get(_caller)) != null)
		{
			entry.nReferingTo++;
		}
		return newSub;
	}

	private void registerCalledSubroutines(Root _root)
	{
		// START KGU#238 2016-08-11: Code revision
		//Vector<Call> calls = new Vector<Call>();
		//collectCalls(_root.children, calls);
		// START KGU#624 2018-12-26: Enh. #655 - method moved to Root
		//Vector<Call> calls = collectCalls(_root);
		Vector<Call> calls = _root.collectCalls();
		// END KGU#624 2018-12-26
		// END KGU#238 2016-08-11
		for (Call call: calls)
		{
			Root registered = null;
			// Identify and register the called routine
			if ((registered = registerCalled(call, _root)) != null)
			{
				// If it hadn't been registered before, analyse it as well
				registerCalledSubroutines(registered);
			}
		}
	}
	
	// START KGU#237 2016-08-10: Bugfix #228
	/**
	 * Tries to find a Root in {@link #subroutines} the signature of which
	 * matches that of the given {@link lu.fisch.structorizer.parser.Function}
	 * {@code fct}.
	 * @param fct - object holding a parsed subroutine call
	 * @return a matching {@link lu.fisch.structorizer.elements.Root} object if available, otherwise null 
	 */
	private Root getAmongSubroutines(Function fct)
	{
		for (Root sub: subroutines.keySet())
		{
			if (sub.getMethodName().equals(fct.getName())
					// START KGU#371 2019-03-08: Enh. #385 allow optional parameters
					//&& sub.getParameterNames().count() == fct.paramCount())
					&& sub.acceptsArgCount(fct.paramCount()) >= 0)
					// END KGU#371 2019-03-08
			{
				return sub;
			}
		}
		return null;
	}
	// END KGU#237 2016-08-10
	
	// START KGU#376 2017-09-20: Enh. #389
	private void registerIncludedRoots(Root _root, Hashtable<Root, SubTopoSortEntry> _includedRoots)
	{
		if (_root.includeList != null && (routinePool != null)) {
			for (int i = 0; i < _root.includeList.count(); i++)
			{
				Root newIncl = null;
				String includeName = _root.includeList.get(i);
				Vector<Root> candidates = routinePool.findIncludesByName(includeName, _root);
				if (!candidates.isEmpty()) {
					newIncl = putRootsToMap(candidates.firstElement(), _root, _includedRoots);
				}
				else if ((newIncl = getAmongExportedRoots(includeName, _includedRoots)) != null)
				{
					_includedRoots.get(newIncl).callers.add(_root);
					// If we got here, then it's probably the top-level diagram itself
					// So better be cautious with reference counting here (lest the
					// including diagram would be suppressed on printing)
					newIncl = null;	// ...and it's not a new subroutine, of course
				}
				if (newIncl != null) {
					// Now do the recursion (the Includable itself may include others)
					registerIncludedRoots(newIncl, _includedRoots);
				}
			}
		}
	}

	private Root getAmongExportedRoots(String includeName, Hashtable<Root, SubTopoSortEntry> _includeMap) {
		for (Root included: _includeMap.keySet()) {
			if (includeName.equals(included.getMethodName())) {
				return included;
			}
		}
		for (Root included: subroutines.keySet()) {
			if (included.isInclude() && includeName.equals(included.getMethodName())) {
				return included;
			}
		}
		return null;
	}
	// END KGU#376 2017-09-20

	// START KGU#236/KGU#311 2016-12-22: Issue #227, enh. #314 - we may need this more root-specificly
	private final void gatherElementInformationRoot(Root _root)
	{
		hasOutput = hasInput = hasEmptyInput = false;
		// START KGU#424 2017-09-25: Care for correct comment positioning
		this.declarationCommentMap.put(_root, new HashMap<String, Instruction>());
		// END KGU#4242 2017-09-25
		gatherElementInformation(_root);
		if (hasOutput) rootsWithOutput.add(_root);
		if (hasInput) rootsWithInput.add(_root);
		if (hasEmptyInput) rootsWithEmptyInput.add(_root);
		// START KGU#376 2017-09-25: Enh. #389
		this.registerIncludedRoots(_root, includeMap);
		// END KGU#376 2017-09-25
	}
	// END KGU#236/KGU#311 2016-12-22
	// START KGU#236 2016-08-10: Issue #227
	// Recursive scanning routine to gather certain information via
	// subclassable method checkElementInformation();
	private final void gatherElementInformation(Element _ele)
	{
		// START KGU#238 2016-08-11: Code revision
		_ele.traverse(new IElementVisitor() {
			@Override
			public boolean visitPreOrder(Element _ele) {
				return checkElementInformation(_ele);
			}
			@Override
			public boolean visitPostOrder(Element _ele) {
				return true;
			}
			
		});
		// END KGU#238 2016-08-11
	}
	
	/**
	 * Generic and subclassable method to check for certain information
	 * on any kind of element. Is guaranteed to be called on every single
	 * element of the diagram before code export is started.
	 * Must not be recursive! 
	 * @param _ele - the currently inpected element
	 * @return whether the traversal is to be continued or not
	 */
	protected boolean checkElementInformation(Element _ele)
	{
		if (_ele instanceof Instruction)
		{
			Instruction instr = (Instruction)_ele;
			if (instr.isInput()) {
				hasInput = true;
				if (instr.isEmptyInput()) hasEmptyInput = true;
			}
			if (instr.isOutput()) hasOutput = true;	
			// START KGU#424 2017-09-25: We must build a comment map for declarations
			Root owner = Element.getRoot(instr);
			StringList declNames = owner.getVarNames(instr);
			StringList text = instr.getUnbrokenText();
			for (int i = 0; i < text.count(); i++) {
				String line = text.get(i);
				if (line.startsWith("type ") && line.contains("=")) {
					declNames.add(":" + line.substring(4, line.indexOf("=")).trim());
				}
			}
			HashMap<String, Instruction> commentMap = this.declarationCommentMap.get(owner);
			for (int i = 0; i < declNames.count(); i++) {
				commentMap.put(declNames.get(i), instr);
			}
			// END KGU#424 2017-09-25
		}
		// START KGU#348 2017-02-19: Support for translation of Parallel elements
		else if (_ele instanceof Parallel)
		{
			hasParallels = true;
		}
		// END KGU#348 2017-02-19
		// START KGU#686 2019-03-21: Enh. #56 - For Perl, try-catch is an extra module
		else if (_ele instanceof Try) {
			hasTryBlocks = true;
		}
		// END KGU#686 2019-03-21
		// START KGU#311 2016-12-22: Enh. #314 - check for file API support
		if (!usesFileAPI && _ele.getText().getText().contains("file"))
		{
			// Now we check more precisely
			String text = _ele.getText().getText();
			for (int i = 0; !usesFileAPI && i < Executor.fileAPI_names.length; i++) {
				if (text.contains(Executor.fileAPI_names[i]))
					usesFileAPI = true;
			}
		}
		// END KGU#311 2016-12-22
		return true;
	}
	// END KGU#236 2016-08-10
 	
	/**
	 * This method is responsible for generating the code of an {@code Instruction} element.<br/>
	 * This dummy version is to be overridden by each inheriting generator class.
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #addCode(String, String, boolean)
	 * @see #insertAsComment(Element, String)
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _inst - the {@link lu.fisch.structorizer.elements.Instruction}
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Instruction _inst, String _indent)
	{
		//
	}
	
	/**
	 * This method is responsible for generating the code of an {@code Alternative}
	 * element i.e. an IF construction.<br/>
	 * This dummy version is to be overridden by each inheriting generator class
	 * (you may have a look at its code to see how the recursive descending is done).
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _alt - the {@link lu.fisch.structorizer.elements.Alernative} element to be exported
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Alternative _alt, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_alt.qTrue,_indent+this.getIndent());
		// code.add(_indent+"");
		generateCode(_alt.qFalse,_indent+this.getIndent());
		// code.add(_indent+"");
	}

	/**
	 * This method is responsible for generating the code of a {@code Case}
	 * element i.e. a multiple selection.<br/>
	 * This dummy version is to be overridden by each inheriting generator class
	 * (you may have a look at its code to see how the recursive descending is done).
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _inst - the {@link lu.fisch.structorizer.elements.Instruction} element to be exported
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Case _case, String _indent)
	{
		// code.add(_indent+"");
		for(int i=0; i < _case.qs.size(); i++)
		{
			// code.add(_indent+"");
			generateCode((Subqueue) _case.qs.get(i), _indent+this.getIndent());
			// code.add(_indent+"");
		}
		// code.add(_indent+"");
	}

	/**
	 * This method is responsible for generating the code of a {@code For} loop
	 * element, either of counting or enumerating style.<br/>
	 * This dummy version is to be overridden by each inheriting generator class
	 * (you may have a look at its code to see how the recursive descending is done).
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _for - the {@link lu.fisch.structorizer.elements.For} element to be exported
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(For _for, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_for.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	/**
	 * This method is responsible for generating the code of a {@code While} loop
	 * element.<br/>
	 * This dummy version is to be overridden by each inheriting generator class
	 * (you may have a look at its code to see how the recursive descending is done).
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _while - the {@link lu.fisch.structorizer.elements.While} element to be exported
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(While _while, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_while.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	/**
	 * This method is responsible for generating the code of a {@code Repeat} loop
	 * element.<br/>
	 * This dummy version is to be overridden by each inheriting generator class
	 * (you may have a look at its code to see how the recursive descending is done).
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _repeat - the {@link lu.fisch.structorizer.elements.Repeat} element to be exported
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Repeat _repeat, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_repeat.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}

	/**
	 * This method is responsible for generating the code of a {@code Forever} loop
	 * element.<br/>
	 * This dummy version is to be overridden by each inheriting generator class
	 * (you may have a look at its code to see how the recursive descending is done).
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _while - the {@link lu.fisch.structorizer.elements.While} element to be exported
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Forever _forever, String _indent)
	{
		// code.add(_indent+"");
		generateCode(_forever.q, _indent + this.getIndent());
		// code.add(_indent+"");
	}
	
	/**
	 * This method is responsible for generating the code of a {@code Call} element.<br/>
	 * This dummy version is to be overridden by each inheriting generator class.
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @param _inst - the {@link lu.fisch.structorizer.elements.Instruction}
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Call _call, String _indent)
	{
		// code.add(_indent+"");
	}

	/**
	 * This method is responsible for generating the code of an {@code Instruction} element.<br/>
	 * This dummy version is to be overridden by each inheriting generator class.
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @param _inst - the {@link lu.fisch.structorizer.elements.Instruction}
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Jump _jump, String _indent)
	{
		// code.add(_indent+"");
	}

	/**
	 * This method is responsible for generating the code of a {@code Parallel} section
	 * element.<br/>
	 * This dummy version just concatenates the threads sequentially and should therefore
	 * be overridden by each inheriting generator class that knows to orchestrate
	 * parallelism
	 * (you may have a look at its code to see how the recursive descending is done).
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Try, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _para - the {@link lu.fisch.structorizer.elements.Parallel} element to be exported
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Parallel _para, String _indent)
	{
		// code.add(_indent+"");
		for(int i = 0; i < _para.qs.size(); i++)
		{
			// code.add(_indent+"");
			generateCode((Subqueue) _para.qs.get(i), _indent+this.getIndent());
			// code.add(_indent+"");
		}
		// code.add(_indent+"");
	}
	
	// START KGU#686 2019-03-17: Enh. #56 try Element introduced
	/**
	 * This method is responsible for generating the code of an {@code Instruction} element.
	 * This dummy version is to be overridden by each inheriting generator class.
	 * It should make use of available helper methods {@link #transform(String)} etc. and
	 * be aware of the several export options.
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @param _try - the {@link lu.fisch.structorizer.elements.Try}
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected void generateCode(Try _try, String _indent)
	{
		insertComment("try (FIXME!)", _indent);
		generateCode(_try.qTry, _indent + this.getIndent());
		insertComment(("catch " + _try.getExceptionVarName()).trim() + " (FIXME!)", _indent);
		generateCode(_try.qCatch, _indent + this.getIndent());
		insertComment("fimally (FIXME!)", _indent);
		generateCode(_try.qFinally, _indent + this.getIndent());
		insertComment("end try (FIXME!)", _indent);
	}
	// END KGU#686 2019-03-17

	/**
	 * This method does not generate anything itself, it is just a formal
	 * entry point for the abstract Element base class in order to distribute
	 * the call to the subclass-specific overloaded methods.
	 * It is NOT to be overridden by subclasses!
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Root, String)
	 * @see #getIndent()
	 * @see #optionCodeLineNumbering()
	 * @see #optionBlockBraceNextLine()
	 * @param _ele - the {@link lu.fisch.structorizer.elements.Element}
	 * @param _indent - the indentation string valid for the given Instruction
	 */
	protected final void generateCode(Element _ele, String _indent)
	{
		if(_ele.getClass().getSimpleName().equals("Instruction"))
		{
			generateCode((Instruction) _ele, _indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Alternative"))
		{
			generateCode((Alternative) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Case"))
		{
			generateCode((Case) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Parallel"))
		{
			generateCode((Parallel) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("For"))
		{
			generateCode((For) _ele, _indent);
		}
		else if(_ele.getClass().getSimpleName().equals("While"))
		{
			generateCode((While) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Repeat"))
		{
			generateCode((Repeat) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Forever"))
		{
			generateCode((Forever) _ele,_indent);
		}
		// START KGU#686 2019-03-17: Enh. #56
		else if(_ele.getClass().getSimpleName().equals("Try"))
		{
			generateCode((Try) _ele,_indent);
		}
		// END KGU#686 2019-03-17
		else if(_ele.getClass().getSimpleName().equals("Call"))
		{
			generateCode((Call) _ele,_indent);
		}
		else if(_ele.getClass().getSimpleName().equals("Jump"))
		{
			generateCode((Jump) _ele,_indent);
		}
	}
	
	/**
	 * This method does not generate anything itself, it just delegates
	 * the job to the methods for the contained elements.<br/>
	 * Should NOT be overridden by subclasses except if inevitable. (Then
	 * super ought to be called before or after the specific enhancements.)
	 * @see #generateCode(Instruction, String)
	 * @see #generateCode(Alternative, String)
	 * @see #generateCode(Case, String)
	 * @see #generateCode(For, String)
	 * @see #generateCode(While, String)
	 * @see #generateCode(Repeat, String)
	 * @see #generateCode(Forever, String)
	 * @see #generateCode(Call, String)
	 * @see #generateCode(Jump, String)
	 * @see #generateCode(Parallel, String)
	 * @see #generateCode(Root, String)
	 * @param _subqueue - the {@link lu.fisch.structorizer.elements.Subqueue}
	 * @param _indent - the indentation string valid for the given element's level
	 */
	// START KGU#383 2017-04-18: Bugfix #386: For an elegant fixing 'final' restriction lifted
	//protected final void generateCode(Subqueue _subqueue, String _indent)
	protected void generateCode(Subqueue _subqueue, String _indent)
	// END KGU#383 2017-04-18
	{
		// code.add(_indent+"");
		for(int i=0; i<_subqueue.getSize(); i++)
		{
			generateCode(_subqueue.getElement(i),_indent);
		}
		// code.add(_indent+"");
	}

	/******** Public Methods *************/

	/**
	 * This method builds the outer code framework for the algorithm
	 * (i.e. the program, procedure or function definition), usually
	 * consisting of the header, a "preamble" (containing e.g. variable
	 * declarations), the implementation part, the result compilation,
	 * and a footer. See {@link Generator#generateCode(Root, String)} for the
	 * general template. Now you have two options:<br/>
	 * a)	Either you may override {@link #generateCode(Root, String)} as a
	 * 		whole if the substructure template doesn't suit your
	 * 		target language needs,<br/>
	 * b)	or you may leave the base method as is and override the
	 * 		submethods (see their Java doc and the examples you may
	 * 		find in various Generator subclasses):<br/>
	 * 		{@link #generateHeader(Root, String, String, StringList, StringList, String)}<br/>
	 * 		{@link #generatePreamble(Root, String, StringList)}<br/>
	 *		{@link #generateResult(Root, String, boolean, StringList)}<br/>
	 *		{@link #generateFooter(Root, String)}.<br/>
	 * @param _root - the diagram to be exported
	 * @param _indent - the indentation for this diagram
	 * @return the entire code for this Root as one string (with newlines)
	 */
	public String generateCode(Root _root, String _indent)
	{
		// START KGU#74 2015-11-30: General pre-processing phase 1
		// Code analysis and Header analysis
		String procName = _root.getMethodName();
		boolean alwaysReturns = mapJumps(_root.children);
		StringList paramNames = new StringList();
		StringList paramTypes = new StringList();
		_root.collectParameters(paramNames, paramTypes, null);
		String resultType = _root.getResultType();
		// START KGU#61/KGU#129 2016-03-22: Now common field for all generator classes
		//StringList varNames = _root.getVarNames(_root, false, true);	// FOR loop vars are missing
		// START KGU#333 2017-01-20: Bugfix #336 - Correct way to include loop variables and exclude parameters
		//this.varNames = _root.getVarNames(_root, false, true);	// FOR loop vars are missing
		// START KGU#691 2019-03-21: Bugfix - the variable highlighting and detection was inflicted
		//this.varNames = _root.getVarNames();
		this.varNames = _root.retrieveVarNames().copy();
		// END KGU#691 2019-03-21
		for (int p = 0; p < paramNames.count(); p++) {
			this.varNames.removeAll(paramNames.get(p));
		}
		// END KGU#333 2017-01-20
		// END KGU#61/KGU#129 2016-03-22
		this.isResultSet = varNames.contains("result", false);
		this.isFunctionNameSet = varNames.contains(procName);
		
		String preaIndent = generateHeader(_root, _indent, procName, paramNames, paramTypes, resultType);
		String bodyIndent = generatePreamble(_root, preaIndent, varNames);
		// END KGU#74 2015-11-30
		
		// code.add("");
		generateCode(_root.children, bodyIndent);
		// code.add("");
		
		// START KGU#74 2015-11-30: Result preprocessing
		generateResult(_root, preaIndent, alwaysReturns, varNames);
		generateFooter(_root, _indent);
		// END KGU#74 2015-11-30

		return code.getText();
	}
	
	// Just dummy implementations to be overridden by subclasses
	/**
	 * Composes the heading for the program or function according to the
	 * syntactic rules of the target language and adds it to this.code.
	 * @see #generatePreamble(Root, String, StringList)
	 * @see #generateResult(Root, String, boolean, StringList)
	 * @see #generateFooter(Root, String)
	 * @param _root - The diagram root element
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param _paramNames - list of the argument names
	 * @param _paramTypes - list of corresponding type names (possibly null) 
	 * @param _resultType - result type name (possibly null)
	 * @return the default indentation string for the preamble stuff following
	 */
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		return _indent + this.getIndent();
	}
	/**
	 * Generates some preamble (i.e. comments, language declaration section etc.)
	 * and adds it to this.code.
	 * @see #generateHeader(Root, String, String, StringList, StringList, String)
	 * @see #generateResult(Root, String, boolean, StringList)
	 * @see #generateFooter(Root, String)
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param _varNames - list of variable names introduced inside the body
	 * @return the default indentation string for the main implementation part
	 */
	protected String generatePreamble(Root _root, String _indent, StringList _varNames)
	{
		return _indent;
	}
	/**
	 * Creates the appropriate code for returning a required result and adds it
	 * (after the algorithm code of the body) to this.code)
	 * @see #generateHeader(Root, String, String, StringList, StringList, String)
	 * @see #generatePreamble(Root, String, StringList)
	 * @see #generateResult(Root, String, boolean, StringList)
	 * @see #generateFooter(Root, String)
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param _alwaysReturns - whether all paths of the body already force a return
	 * @param _varNames - names of all assigned variables
	 * @return the default indentation string for the following footer
	 */
	protected String generateResult(Root _root, String _indent, boolean _alwaysReturns, StringList _varNames)
	{
		return _indent;
	}
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close an open block. 
	 * @see #generateHeader(Root, String, String, StringList, StringList, String)
	 * @see #generatePreamble(Root, String, StringList)
	 * @see #generateResult(Root, String, boolean, StringList)
	 * @param _root - the diagram root element 
	 * @param _indent - the current indentation string
	 */
	protected void generateFooter(Root _root, String _indent)
	{
		
	}
	// END KGU#74 2015-11-30
	
	// START KGU#376 2017-09-28: Enh. #389 - insert the initialization code of the includables
	/**
	 * Inserts the definitions and declarations of all includable diagrams recursively required by
	 * the roots to be exported in topological order
	 * @param _root - the currently exported Root (supposed to be the hierarchy top)
	 * @param _indent - the current indentation
	 * @param _force - Whether the insertion is to be forced no regard of declaration policy
	 */
	protected void insertGlobalDefinitions(Root _root, String _indent, boolean _force) {
		boolean thisDone = false;
		code.add("");
		for (Root incl: this.includedRoots.toArray(new Root[]{})) {
			insertDefinitions(incl, _indent, incl.retrieveVarNames(), _force);
			if (incl == _root) {
				thisDone = true;
			}
		}
		if (_root.isInclude() && !thisDone) {
			insertDefinitions(_root, _indent, this.varNames, true);				
		}
	}
	
	/**
	 * Inserts constant, type, and variable definitions for the passed-in {@link Root} {@code _root} 
	 * @param _root - the diagram the declarations and definitions of are to be inserted
	 * @param _indent - the proper indentation as String
	 * @param _varNames - optionally the StringList of the variable names to be declared (my be null)
	 * @param _force - true means that the insertion is forced even if option {@link #isInternalDeclarationAllowed()} is set 
	 */
	protected void insertDefinitions(Root _root, String _indent, StringList _varNames, boolean _force) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Generates the (initialisation) code of all includable diagrams recursively required by
	 * the roots to be exported in topological order 
	 * @param _indent - current indentation string
	 */
	protected void insertGlobalInitialisations(String _indent) {
		if (topLevel) {
			int startLine = code.count();
			for (Root incl: this.includedRoots.toArray(new Root[]{})) {
				insertComment("BEGIN initialization for \"" + incl.getMethodName() + "\"", _indent);
				// START KGU#501 2018-02-22: Bugfix #517
				this.includeInitialisation = true;
				// END KGU#501 2018-02-22
				generateCode(incl.children, _indent);
				// START KGU#501 2018-02-22: Bugfix #517
				this.includeInitialisation = false;
				// END KGU#501 2018-02-22
				insertComment("END initialization for \"" + incl.getMethodName() + "\"", _indent);
			}
			if (code.count() > startLine) {
				code.add(_indent);
			}
		}
	}
	// END KGU#376 2017-09-28

	
	// START KGU#363 2017-05-16: Enh. #372 - more ease for subclasses to place the license information
	/**
	 * Inserts the copyright information (author name, license name and text) if the respective option
	 * is enabled. 
	 * @param _root - the Root object holding the relevant attributes
	 * @param _indent - the current indentation string
	 * @param _fullText - whether the full license text is to be inserted, too (may be lengthy!)
	 */
	protected void insertCopyright(Root _root, String _indent, boolean _fullText) {
		if (this.optionExportLicenseInfo()) {
			this.insertComment("", _indent);
			this.insertComment("Copyright (C) " + _root.getCreatedString() + " " + _root.getAuthor(), _indent);
			if (_root.licenseName != null) {
				this.insertComment("License: " + _root.licenseName, _indent);
			}
			if (_fullText && _root.licenseText != null) {
				this.insertComment(StringList.explode(_root.licenseText, "\n"), _indent);
			}
			this.insertComment("", _indent);
		}
	}
	
	/**
	 * Entry point for interactively commanded code export. Retrieves export options,
	 * opens a file selection dialog, and effectuates the actual code export.
	 * @param _root - program or top-level routine diagram (call hierarchy root)
	 * @param _proposedDirectory - last export or current Structorizer directory (as managed by Diagram)
	 * @param _frame - the GUI Frame object responsible for this action
	 * @param _routinePool TODO
	 * @return the chosen target directory if the export hadn't been cancelled, otherwise null
	 * @see #exportCode(Vector, String, String, String, String, IRoutinePool)
	 */
	// START KGU 2017-04-26
	//public void exportCode(Root _root, File _currentDirectory, Frame _frame)
	// START KGU#676 2019-03-13: Enh. #696 Allow to specify the routine pool to be used
	public File exportCode(Root _root, File _proposedDirectory, Frame _frame, IRoutinePool _routinePool)
	// END KGU#676 2019-03-13
	// END KGU 2017-04-26
	{
		// START KGU 2017-04-26
		File exportDir = _proposedDirectory;
		// END KGU 2017-04-26
		// START KGU#676 2019-03-13: Enh. #696 Allow to specify the routine pool to be used
		routinePool = _routinePool;
		// END KGU#676 2019-03-13
		//=============== Get export options ======================
		try
		{
			Ini ini = Ini.getInstance();
			ini.load();
			// START KGU#173 2016-04-04: Issue #151 - get rid of all the hidden ExportOptionDialoge produced here
//			eod = new ExportOptionDialoge(frame);	// FIXME (KGU) What do we need this hidden dialog for?
//			if(ini.getProperty("genExportComments","0").equals("true"))
//				eod.commentsCheckBox.setSelected(true);
//			else 
//				eod.commentsCheckBox.setSelected(false);
//			// START KGU#16/KGU#113 2015-12-18: Enh. #66, #67
//			eod.bracesCheckBox.setSelected(ini.getProperty("genExportBraces", "0").equals("true"));
//			eod.lineNumbersCheckBox.setSelected(ini.getProperty("genExportLineNumbers", "0").equals("true"));
//			// END KGU#16/KGU#113 2015-12-18
//			// START KGU#162 2016-03-31: Enh. #144
//			eod.noConversionCheckBox.setSelected(ini.getProperty("genExportnoConversion", "0").equals("true"));
//			this.suppressTransformation = eod.noConversionCheckBox.isSelected(); 
//			// END KGU#16/KGU#113 2015-12-18
			exportAsComments = ini.getProperty("genExportComments","0").equals("true");
			startBlockNextLine = !ini.getProperty("genExportBraces", "0").equals("true");
			generateLineNumbers = ini.getProperty("genExportLineNumbers", "0").equals("true");
			exportCharset = ini.getProperty("genExportCharset", Charset.defaultCharset().name());
			suppressTransformation = ini.getProperty("genExportnoConversion", "0").equals("true");
			// END KGU#173 2016-04-04
			// START KGU#178 2016-07-19: Enh. #160
			exportSubroutines = ini.getProperty("genExportSubroutines", "0").equals("true");
			// END KGU#178 2016-07-19
			// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
			includeFiles = ini.getProperty("genExportIncl" + this.getClass().getSimpleName(), "");
			// END KGU#351 2017-02-26
			// START KGU#363 2017-05-11: Enh. #372 - license and author info ought to be exportable as well
			exportAuthorLicense = ini.getProperty("genExportLicenseInfo", "0").equals("true");
			// END KGU#363 2017-05-11

		} 
		catch (FileNotFoundException ex)
		{
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			this.getLogger().log(Level.WARNING, "Trouble getting export options.", ex);
			// END KGU#484 2018-04-05
		} 
		catch (IOException ex)
		{
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			this.getLogger().log(Level.WARNING, "Trouble getting export options.", ex);
			// END KGU#484 2018-04-05
		}

		//=============== Request output file path (interactively) ======================
		JFileChooser dlgSave = new JFileChooser();
		dlgSave.setDialogTitle(getDialogTitle());

		// set directory
		if(_root.getFile()!=null)
		{
			dlgSave.setCurrentDirectory(_root.getFile());
		}
		else
		{
			dlgSave.setCurrentDirectory(_proposedDirectory);
		}

		// propose name
		// START KGU 2015-10-18: Root has got a mechanism for this!
		//		String nsdName = _root.getText().get(0);
		//		nsdName.replace(':', '_');
		//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		// START KGU#690 2019-03-21: Issue #707 We prefer the base name of the diagram file if already saved
		//String nsdName = _root.proposeFileName();
		String nsdName = _root.getPath();
		if (nsdName.isEmpty()) {
			nsdName = _root.proposeFileName();
		}
		else {
			nsdName = (new File(nsdName)).getName();
			int dotPos = nsdName.lastIndexOf('.');
			if (dotPos > 1) {
				nsdName = nsdName.substring(0, dotPos);
			}
		}
		// Now the subclass gets a chance to modify the proposal if there are some  - according to #707 - hyphens in python file names are nasty
		nsdName = this.ensureFilenameConformity(nsdName);
		// END KGU#690 2019-03-21
		// END KGU 2015-10-18
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		dlgSave.addChoosableFileFilter((javax.swing.filechooser.FileFilter) this);
		dlgSave.setFileFilter((javax.swing.filechooser.FileFilter) this);
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(_frame);

		/***** file_exists check here!
		 if(file.exists())
		 {
		 JOptionPane.showMessageDialog(null,file);
		 int response = JOptionPane.showConfirmDialog (null,
		 "Overwrite existing file?","Confirm Overwrite",
		 JOptionPane.OK_CANCEL_OPTION,
		 JOptionPane.QUESTION_MESSAGE);
		 if (response == JOptionPane.CANCEL_OPTION)
		 {
		 return;
		 }
		 else
		 */
		String filename = new String();

		File file = null;

		if (result == JFileChooser.APPROVE_OPTION) 
		{
			filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if (!isOK(filename))
			{
				filename += "."+getFileExtensions()[0];
			}
			file = new File(filename);
		}

		//System.out.println(filename);

		if (file != null && file.exists())
		{
			int response = JOptionPane.showConfirmDialog (null,
					"Overwrite existing file?","Confirm Overwrite",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION)
			{
				file = null;	// We might as well return here
			}
		}
			
		//=============== Actual code generation ======================
		if (file != null)
		{
			// START KGU 2017-04-26
			exportDir = file.getParentFile();
			// END KGU 2017-04-26
			// START KGU#194 2016-05-07: Bugfix #185 - the subclass may need the filename
			pureFilename = file.getName();
			int dotPos = pureFilename.indexOf(".");
			if (dotPos >= 0)
			{
				pureFilename = pureFilename.substring(0, dotPos);
			}
			// END KGU#194 2016-05-07

			// START KGU 2016-03-29: Pre-processed match patterns for better identification of complicated keywords
			this.splitKeywords.clear();
			String[] keywords = CodeParser.getAllProperties();
			for (int k = 0; k < keywords.length; k++)
			{
				this.splitKeywords.add(Element.splitLexically(keywords[k], false));
			}
			// END KGU 2016-03-29

			try
			{
				// START KGU#178 2016-07-20: Enh. #160 - register all subroutine calls
				if (this.optionExportSubroutines())
				{
					// START KGU#237 2016-08-10: Bugfix #228 - precaution for recursive top-level routine
					if (!_root.isProgram())
					{
						subroutines.put(_root, new SubTopoSortEntry(null));
					}
					// END KGU#237 2016-08-10
					registerCalledSubroutines(_root);
					// START KGU#237 2016-08-10: Bugfix #228
					if (!_root.isProgram())
					{
						subroutines.remove(_root);
					}
					// END KGU#237 2016-08-10
				}
				// END KGU#178 2016-07-20
				
				// START KGU#236 2016-08-10: Issue #227: General information gathering pass
				// START KGU#311 2016-12-22: Issue #227, Enh. #314
				//gatherElementInformation(_root);
				gatherElementInformationRoot(_root);
				// END KGU#311 2016-12-22
				
				if (this.optionExportSubroutines())
				{
					for (Root sub: subroutines.keySet())
					{
						// START KGU#311 2016-12-22: Issue #227, Enh. #314
						//gatherElementInformation(sub);
						gatherElementInformationRoot(sub);
						// END KGU#311 2016-12-22
					}		
				}
				// END KGU#236 2016-08-10
				
				// START KGU#376 2017-09-25: Enh. #389 Set up the topologically sorted include list
				includedRoots = sortTopologically(includeMap);
				// END KGU#376 2017-09-25
				// START KGU#424 2017-09-25: Care for the mapping of appropriate comments
				for (Root incl: includedRoots.toArray(new Root[]{})) {
					gatherElementInformationRoot(incl);
				}
				// END KGU#424 2017-09-25

				// START KGU 2015-10-18: This didn't make much sense: Why first insert characters that will be replaced afterwards?
				// (And with them possibly any such characters that had not been there for indentation!)
				//    String code = BString.replace(generateCode(_root,"\t"),"\t",getIndent());
				String code = generateCode(_root, "");
				// END KGU 2015-10-18

				// START KGU#178 2016-07-20: #160 - Sort and export required subroutines
				if (this.optionExportSubroutines())
				{
					code = generateSubroutineCode(_root);
				}
				// END KGU#178 2016-07-20
				
//				for (String charsetName : Charset.availableCharsets().keySet())
//				{
//					System.out.println(charsetName);
//				}
//				System.out.println("Default: " + Charset.defaultCharset().name());
				
				BTextfile outp = new BTextfile(filename);
				// START KGU#168 2016-04-04: Issue #149 - allow to select the charset
				//outp.rewrite();
				outp.rewrite(exportCharset);
				// END KGU#168 2016-04-04
				outp.write(code);
				// START KGU#689 2019-03-21: Issue #706 - a non-empty text file should end with a newline
				if (!code.isEmpty()) {
					outp.write("\n");
				}
				// END KGU#689 2019-03-21
				outp.close();
				
				if (this.usesFileAPI) {
					copyFileAPIResources(filename);
				}
			}
			catch (Exception e)
			{
				String message = e.getMessage();
				// START KGU#484 2018-04-05: Issue #463
				//e.printStackTrace();
				getLogger().log(Level.WARNING, "Error on saving file!", e);
				// END KGU#484 2018-04-05
				if (message == null) {
					message = e.getClass().getSimpleName();
				}
				JOptionPane.showMessageDialog(null,
						"Error while saving the file!\n" + message,
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			// START KGU#178 2016-07-20: Enh. #160
			if (this.optionExportSubroutines() && missingSubroutines.count() > 0)
			{
				JOptionPane.showMessageDialog(null,
						"Export defective. Some subroutines weren't found:\n\n" + missingSubroutines.getText(),
						"Warning", JOptionPane.WARNING_MESSAGE);		    		
			}
			// END KGU#178 2016-07-20
		} // if (file != null)
		// START KGU#654 2019-02-16: Enh. #681 - we want to inform the caller if the export failed
		else {
			exportDir = null;
		}
		// END KGU#654 2019-02-16
		// START KGU 2017-04-26
		return exportDir;
		// END KGU 2017-04-26
	}
	
	// START KGU#690 2019-03-121: Enh. #707
	/**
	 * This method allows the subclass to modify the automatically generated file name proposal
	 * to ensure conformity with file name conventions of the target language.
	 * This should concentrate on the base name rather than the extension (which will typically
	 * not be included in the argument string).<br/>
	 * The base method just passes the argument through.
	 * @param proposedFilename - a base fle name according to the proposal rules for NSD file names.
	 * @return the possibly modified name
	 */
	protected String ensureFilenameConformity(String proposedFilename) {
		return proposedFilename;
	}
	// END KGU#690 2019-03-21
	
	// START KGU#178 2016-07-20: Enh. #160 - Specific code for subroutine export
	/**
	 * Routine is called from {@link #exportCode(Root, File, Frame, IRoutinePool)} after
	 * the top-level diagram code has been created and generates and inserts the code
	 * sequences of the called subroutines of {@code _root}in topologically sorted order.
	 * @param _root - the top-level diagram root.
	 * @return the entire code for this {@code Root} including the subroutine diagrams as one string (with newlines)
	 * @see #sortTopologically(Hashtable)
	 */
	protected final String generateSubroutineCode(Root _root)
	{
		StringList outerCodeTail = code.subSequence(this.subroutineInsertionLine, code.count());
		code = code.subSequence(0, this.subroutineInsertionLine);
		topLevel = false;
		Queue<Root> roots = sortTopologically(subroutines);
		while (!roots.isEmpty())
		{
			Root sub = roots.remove();	// get the next routine
			generateCode(sub, subroutineIndent);	// add its code
		}

		code.add(outerCodeTail);
		
		topLevel = true;
		
		return code.getText();
	}

	/**
	 * Performs a topological sorting of the Roots in the {@code _dependencyMap}
	 * and returns the result as queue.<br/>
	 * ATTENTION: This routine consumes (i.e. destroys) the passed-in {@code _dependencyMap}!
	 * @param _dependencyMap - the dependency graph of the routines as map - will be emptied!
	 * @return queue of the sorted diagrams (independent first, dependent ones following)
	 */
	protected Queue<Root> sortTopologically(Hashtable<Root, SubTopoSortEntry> _dependencyMap) {
		Queue<Root> sortedRoots = new LinkedList<Root>();
		Queue<Root> roots = new LinkedList<Root>();
		// START KGU#349 2017-02-20: Bugfix #349 - precaution against indirect recursion, we must export all routines
		int minRefCount = 0;
		while (!_dependencyMap.isEmpty()) {
		// END KGU#349 2017-02-20
			// Initial queue filling - this is a classical topological sorting algorithm
			for (Root sub: _dependencyMap.keySet())
			{
				SubTopoSortEntry entry = _dependencyMap.get(sub);
				// If this routine refers to no other one, then enlist it
				if (entry.nReferingTo == minRefCount)
				{
					roots.add(sub);
				}
			}
			// Now we have an initial queue of independent routines,
			// so export them and enlist those dependents
			// the prerequisites of which are thereby fulfilled.
			while (!roots.isEmpty())
			{
				Root sub = roots.remove();	// get the next routine
				sortedRoots.add(sub);	// ... and add it to the result queue

				// look for dependent routines and decrement their dependency counter
				// (the entry for sub isn't needed any longer now)
				for (Root caller: _dependencyMap.remove(sub).callers)
				{
					SubTopoSortEntry entry = _dependencyMap.get(caller);
					// Last dependency? Then enlist the caller (if it's not already listed - in case of indirect recursion)
					if (entry != null && --entry.nReferingTo <= 0 && !roots.contains(caller))
					{
						roots.add(caller);
						// when we could add a due subroutine then there is no need anymore to tolerate routines in need of others
						minRefCount = 0;
					}
				}
			}
			// START KGU#349 2017-02-20: Bugfix #349
			// An indirect recursion might block the queuing of routines, so raise reference toleration level
			minRefCount++;
			// END KGU#349
		}
		return sortedRoots;
	}
	// END KGU#178 2016-07-20
	
	// START KGU#311 2016-12-22: Enh. #314
	/**
	 * Inserts all marked sections of resource file "FileAPI.&lt;_language&gt;.txt"
	 * at line {@link #subroutineInsertionLine} into the resulting code.
	 * Increases {@link #subroutineInsertionLine} by the number of lines copied
	 * such that subroutines (which are inserted later) will be inserted after the
	 * FileAPI stuff, because they might rely on some FileAPI routines.
	 * @see #insertFileAPI(String, int)
	 * @see #insertFileAPI(String, int, String)
	 * @see #insertFileAPI(String, int, String, int)
	 * @param _language - name or file name extension of an export language
	 */
	protected void insertFileAPI(String _language)
	{
		insertFileAPI(_language, 0);	
	}
	
	/**
	 * Inserts marked section {@code _sectionCount} (1, 2, ...) or all sections (_sectionCount = 0) of
	 * resource file "FileAPI.&lt;_language&gt;.txt" at line {@link #subroutineInsertionLine} into
	 * the resulting code.
	 * Increases {@link #subroutineInsertionLine} by the number of lines copied
	 * such that subroutines (which are inserted later) will be inserted after the
	 * FileAPI stuff, because they might rely on some FileAPI routines.
	 * @see #insertFileAPI(String)
	 * @see #insertFileAPI(String, int, String)
	 * @see #insertFileAPI(String, int, String, int)
	 * @param _language - name or file name extension of an export language
	 * @param _sectionCount - number of the marked section to be copied (0 for all)
	 */
	protected void insertFileAPI(String _language, int _sectionCount)
	{
		this.subroutineInsertionLine = insertFileAPI(_language, this.subroutineInsertionLine, this.subroutineIndent, _sectionCount);	
	}
	
	/**
	 * Inserts marked section _sectionCount (1, 2, ...) or all sections ({@code _sectionCount = 0}) of
	 * resource file "FileAPI.&lt;_language&gt;.txt" at line {@code _atLine} with given {@_indentation} into
	 * the resulting code 
	 * @see #insertFileAPI(String)
	 * @see #insertFileAPI(String, int)
	 * @see #insertFileAPI(String, int, String)
	 * @param _language - name or file name extension of an export language
	 * @param _atLine - target line where the file section is to be copied to
	 * @param _indentation - indentation string (to precede every line of the copied section) 
	 * @param _sectionCount - number of the marked section to be copied (0 for all)
	 * @return line number at the end of the inserted code lines
	 */
	protected int insertFileAPI(String _language, int _atLine, String _indentation, int _sectionCount)
	{
		boolean isDone = false;
		int sectNo = 0;
		String error = "";
		try {
			java.io.InputStream fis = this.getClass().getResourceAsStream("FileAPI." + _language + ".txt");
			java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis, "UTF-8"));
			String line = null;
			boolean doInsert = false;
			while ((line = reader.readLine()) != null) {
				if (line.contains("===== STRUCTORIZER FILE API START =====")){
					sectNo++;
					doInsert = _sectionCount == 0 || _sectionCount == sectNo;
					code.insert(_indentation, _atLine++);
				}
				if (doInsert) {
					// Unify indentation and replace dummy messages by localized ones
					line = line.replace("\t", this.getIndent());
					line = line.replace("§INVALID_HANDLE_READ§", Control.msgInvalidFileNumberRead.getText());
					line = line.replace("§INVALID_HANDLE_WRITE§", Control.msgInvalidFileNumberWrite.getText());
					line = line.replace("§NO_INT_ON_FILE§", Control.msgNoIntLiteralOnFile.getText());
					line = line.replace("§NO_DOUBLE_ON_FILE§", Control.msgNoDoubleLiteralOnFile.getText());
					line = line.replace("§END_OF_FILE§", Control.msgEndOfFile.getText());
					code.insert(_indentation + line, _atLine++);
				}
				if (line.contains("===== STRUCTORIZER FILE API END =====")){
					doInsert = false;
					code.insert(_indentation, _atLine++);
				}
			}
			reader.close();
			isDone = true;
		} catch (IOException e) {
			error = e.getLocalizedMessage();
		}
		if (!isDone) {
			getLogger().log(Level.WARNING, "insertFileAPI({0}, ...): {1}", new Object[]{_language, error});
		}
		return _atLine;
	}
	
	/**
	 * Routine stub that may be overridden by subclasses to command the creation of (modified) copies
	 * of some resource files for the used FileAPI. Typically, this method is called just once after
	 * the (recursive) code export has been mostly done.
	 * @see #copyFileAPIResource(String, String, String)
	 * @see #insertFileAPI(String)
	 * @see #insertFileAPI(String, int)
	 * @see #insertFileAPI(String, int, String)
	 * @see #insertFileAPI(String, int, String, int)
	 * @param _filePath - path of the target directory or of some file within it  
	 * @return flag whether the copy has worked
	 */
	protected boolean copyFileAPIResources(String _filePath)
	{
		return true;
	}
	
	/**
	 * Creates a (modified) copy of resource file "FileAPI.&lt;_language&gt;.txt" in the _targetPath
	 * directory with the given _targetFilename. If _targetFilename is null then the file name will
	 * be "FileAPI"&lt;_language&gt;.
	 * @see #copyFileAPIResources(String)
	 * @see #insertFileAPI(String)
	 * @see #insertFileAPI(String, int)
	 * @see #insertFileAPI(String, int, String)
	 * @see #insertFileAPI(String, int, String, int)
	 * @param _language - a language-specific filename extension
	 * @param _targetFilename - the proposed filename for the copy (should not contain path elements!)
	 * @param _targetPath - path of the target directory or of a file within it 
	 * @return
	 */
	protected boolean copyFileAPIResource(String _language, String _targetFilename, String _targetPath)
	{
		boolean isDone = false;
		String error = "";
		if (_targetFilename == null) {
			_targetFilename = "FileAPI." + _language;
		}
		File target = new File(_targetFilename);
		if (!target.isAbsolute()) {
			java.io.File targetDir = new java.io.File(_targetPath);
			if (!targetDir.isDirectory()) {
				targetDir = targetDir.getParentFile();
			}
			target = new File(targetDir.getAbsolutePath() + File.separator + _targetFilename);
		}
		// Don't overwrite the file if it already exists in the target directory
		if (!target.exists()) {
			InputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = this.getClass().getResourceAsStream("FileAPI." + _language + ".txt");
				fos = new FileOutputStream(target);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					// Suppress insertion markers and replace dummy messages by localized ones
					if (!line.contains("===== STRUCTORIZER FILE API")){
						line = line.replace("\t", this.getIndent());
						line = line.replace("§INVALID_HANDLE_READ§", Control.msgInvalidFileNumberRead.getText());
						line = line.replace("§INVALID_HANDLE_WRITE§", Control.msgInvalidFileNumberWrite.getText());
						line = line.replace("§NO_INT_ON_FILE§", Control.msgNoIntLiteralOnFile.getText());
						line = line.replace("§NO_DOUBLE_ON_FILE§", Control.msgNoDoubleLiteralOnFile.getText());
						line = line.replace("§END_OF_FILE§", Control.msgEndOfFile.getText());
						// TODO copy the file, replacing the messages
						writer.write(line); writer.newLine();
					}
				}
				writer.close();
				reader.close();
				isDone = true;
			} catch (IOException e) {
				error = e.getLocalizedMessage();
			}
			finally {
				if (fis != null) {
					try { fis.close(); } catch (IOException e) {}
				}
				if (fos != null) {
					try { fos.close(); } catch (IOException e) {}
				}
			}
			if (!isDone) {
				getLogger().log(Level.WARNING, "copyFileAPIResource({0}, {1}, ...): {2}", new Object[]{_language, _targetFilename, error});
			}
		}
		return isDone;
	}
	// END KGU#311 2016-12-22
	
	// START KGU#301 2016-12-01: Bugfix #301
	/**
	 * Helper method to detect exactly whether the given {@code expression} is enclosed in parentheses.
	 * Simply check whether it starts with "(" and ends with ")" is NOT sufficient because the expression
	 * might look like this: {@code (4 + 8) * sqrt(3.5)}, which starts and ends with a parenthesis without
	 * being parenthesized.  
	 * @param expression - the expression to be analysed as string
	 * @return true if the expression is properly parenthesized. (Which is to be ensured e.g for conditions
	 * in C and derived languages.
	 */
	protected static boolean isParenthesized(String expression)
	{
		return Element.isParenthesized(expression);
	}
	// END KGU#301 2017-09-19
	/**
	 * Helper method to detect exactly whether the expression represented by {@code tokens} is enclosed in
	 * parentheses.<br/>
	 * Simply check whether it starts with "(" and ends with ")" is NOT sufficient because the expression
	 * might look like this: {@code (4 + 8) * sqrt(3.5)}, which starts and ends with a parenthesis without
	 * being parenthesized.  
	 * @param tokens - the tokenized expression to be analysed as StringList
	 * @return true if the expression is properly parenthesized. (Which is to be ensured e.g for conditions
	 * in C and derived languages.
	 */
	protected static boolean isParenthesized(StringList tokens)
	{
		return Element.isParenthesized(tokens);
	}
	// END KGU#301 2017-09-19

	// START KGU#187 2016-04-28: Enh. 179 batch mode
	/*****************************************
	 * batch code export methods
	 *****************************************/

	/**
	 * Exports the diagrams given by _roots into a text file with path _targetFile.<br/>
	 * Note: This method is intended for batch export.
	 * @param _roots - vector of diagram Roots to be exported (in this order).
	 * @param _targetFile - path of the target text file for the code export.
	 * @param _options - String containing code letters for export options ('b','c','f','l','t','-') 
	 * @param _charSet - name of the character set to be used.
	 * @param _settingsFromFile - whether a (partial) ini file for alternative option retrieval was given
	 * @param _routinePool - the routine pool to be used if referenced subroutines are to be exported
	 * @see #exportCode(Root, File, Frame, IRoutinePool)
	 */
	// START KGU#676 2019-03-13: Enh. #696 allow explicitly to specify the routine pool to use
	//public void exportCode(Vector<Root> _roots, String _targetFile, String _options, String _charSet)
	// START KGU#720 2019-08-05: Enh. #737 - allow to load settings from a configuration file
	public void exportCode(Vector<Root> _roots, String _targetFile, String _options, String _charSet, boolean _settingsFromFile, IRoutinePool _routinePool)
	// END KGU#720 2019-08-05
	// END KGU#676 2019-03-13
	{
		// START KGU#676 2019-03-13: Enh. #696
		routinePool = _routinePool;
		this.exportSubroutines = _routinePool != null;
		this.pureFilename = "";
		this.hasParallels = false;
		this.usesFileAPI = false;
		this.hasInput = false;
		this.hasEmptyInput = false;
		this.hasOutput = false;
		this.code.clear();
		// END KGU#676 2019-03-13
		// START KGU#311 2016-12-27: Enh. #314
		boolean someRootUsesFileAPI = false;
		// END KGU#311 2016-12-27
		
		if (Charset.isSupported(_charSet))
		{
			exportCharset = _charSet;
		}
		else
		{
			getLogger().log(Level.WARNING, "*** Charset {0} not available; {1} used.", new Object[]{_charSet, exportCharset});
		}
		
		//=============== Get export options ======================
		// START KGU#720/KGU#722 2019-08-07: Enh. #737, #741 - option to load settings from extra settings file
		if (_settingsFromFile) {
			try
			{
				Ini ini = Ini.getInstance();
				ini.load();
				exportAsComments = ini.getProperty("genExportComments","0").equals("true");
				startBlockNextLine = !ini.getProperty("genExportBraces", "0").equals("true");
				generateLineNumbers = ini.getProperty("genExportLineNumbers", "0").equals("true");
				exportCharset = ini.getProperty("genExportCharset", Charset.defaultCharset().name());
				suppressTransformation = ini.getProperty("genExportnoConversion", "0").equals("true");
				includeFiles = ini.getProperty("genExportIncl" + this.getClass().getSimpleName(), "");
				exportAuthorLicense = ini.getProperty("genExportLicenseInfo", "0").equals("true");
			} 
			catch (IOException ex)
			{
				this.getLogger().log(Level.WARNING, "Trouble getting export options.", ex);
			}
		}
		// END KGU#720/KGU#722 2019-08-07

		boolean overwrite = false;
		if (_options != null)
		{
			for (int i = 0; i < _options.length(); i++)
			{
				char ch = _options.charAt(i);
				switch (ch)
				{
				// START KGU#363 2017-05-11: Enh. #372
				//case 'A':
				case 'a':
					exportAuthorLicense = true;
					break;
				// END KGU#363 2017-05-11
				//case 'C':
				case 'c':
					exportAsComments = true;
					break;
				//case 'B':
				case 'b':
					startBlockNextLine = true;
					break;
				//case 'F':
				case 'f':
					overwrite = true;
					break;
				//case 'L':
				case 'l':
					generateLineNumbers = true;
					break;
				//case 'T':
				case 't':
					suppressTransformation = true;
					break;
				case '-':	// Handled separately
					break;
				default:
					// START KGU#484 2018-03-22: Issue #463
					//System.err.println("*** Unknown generator option -" + ch + " ignored.");
					getLogger().log(Level.WARNING, "Unknown generator option -{0} ignored.", ch);
				}
			}
		}

		if (_targetFile != null)
		{
			if (!isOK(_targetFile))			
			{
				int posDot = _targetFile.lastIndexOf(".");
				int posSep = _targetFile.lastIndexOf(System.getProperty("file.separator"));
				if (posDot > posSep)
				{
					_targetFile = _targetFile.substring(0, posDot);
				}
				_targetFile += "." + getFileExtensions()[0];
			}

			StringList nameParts = StringList.explode(_targetFile, "[.]");
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
			_targetFile = nameParts.concatenate(".");
		}

		CodeParser.loadFromINI();
		this.splitKeywords.clear();
		String[] keywords = CodeParser.getAllProperties();
		for (int k = 0; k < keywords.length; k++)
		{
			this.splitKeywords.add(Element.splitLexically(keywords[k], false));
		}

		boolean firstExport = true;
		// START KGU#676 2019-03-13: Enh. #696 Now that we can export archives we must consider subroutines and includes here to
		this.subroutines.clear();
		this.includedRoots.clear();
		this.includeMap.clear();
		if (this.optionExportSubroutines()) {
			if (routinePool.getName() != null) {
				this.pureFilename = routinePool.getName();	// used e.g. for Pascal/Oberon UNIT/MODULE naming
			}
		}
		// END KGU#676 2019-03-13
		for (Root root : _roots)
		{
			// START KGU#676 2019-03-31: Isse #696
			root.specialRoutinePool = routinePool;
			// END KGU#676 2019-03-31
			if (firstExport || routinePool != null)
			{
				firstExport = false;
			}
			else
			{
				code.add("");
				this.insertComment("=======8<=====================================================", "");
				code.add("");
			}

			// START KGU#676 2019-03-13: Enh. #696 Now that we can export archives we must consider subroutines and includes here to
			if (this.optionExportSubroutines()) {
				boolean wasAdded = false;
				// Precaution for recursive top-level routine
				if (!root.isProgram() && !subroutines.containsKey(root))
				{
					subroutines.put(root, new SubTopoSortEntry(null));
					wasAdded = true;
				}
				registerCalledSubroutines(root);
				if (wasAdded)
				{
					subroutines.remove(root);
				}
				if (this.usesFileAPI) { someRootUsesFileAPI = true; }
			}
			else {
			// END KGU#676 2019-03-13
				// START KGU#348 2017-09-25: Reset the need for thread libraries before each export
				this.hasParallels = false;
				// START KGU#348 2017-09-25
				// START KGU#311 2016-12-27: Enh. #314 ensure I/O-specific additions per using root
				this.usesFileAPI = false;
			// START KGU#676 2019-03-13: Enh. #696 Now that we can export archives we must consider subroutines and includes here to
			}
			// END KGU#676 2019-03-13
			gatherElementInformationRoot(root);
			if (this.usesFileAPI) { someRootUsesFileAPI = true; }
			if (this.pureFilename.isEmpty()) {
				this.pureFilename = root.getMethodName();	// used e.g. for Pascal/Oberon UNIT/MODULE naming
			}
			// END KGU#311 2016-12-27

			// START KGU#676 2019-03-13: Enh. #696 Postpone this until we have all subroutine information
			//generateCode(root, "");
			if (!this.optionExportSubroutines()) {
				generateCode(root, "");
			}
			// END KGU#678 2019-03-13
			
		}
		// START KGU#676 2019-03-13: Enh. #696 Now that we can export archives we must consider subroutines and includes here to
		if (this.optionExportSubroutines()) {
			for (Root sub: subroutines.keySet())
			{
				gatherElementInformationRoot(sub);
			}
			includedRoots = sortTopologically(includeMap);
			for (Root incl: includedRoots.toArray(new Root[]{})) {
				gatherElementInformationRoot(incl);
			}
			int subroutineLine = code.count();
			firstExport = true;
			for (Root root: _roots) {
				if (!subroutines.containsKey(root) && !includedRoots.contains(root)) {
					if (!firstExport) {
						code.add("");
						this.insertComment("=======8<=====================================================", "");
						code.add("");
					}
					generateCode(root, "");
					if (firstExport) {
						subroutineLine = this.subroutineInsertionLine;
						firstExport = false;
					}
				}
			}
			this.subroutineInsertionLine = subroutineLine;
			if (!firstExport && !subroutines.isEmpty()) {
				code.insert("", subroutineLine);
				code.insert(this.commentSymbolLeft() + " = = 8< = = = = = = = = = = = = = = = = = = = = = = = = = = = = =" + this.commentSymbolRight(), subroutineLine);
				code.insert("", subroutineLine);
			}
			generateSubroutineCode(null);
		}
		// END KGU#676 2019-03-13

		// Did the user want the code directed to standard output?
		if (_options.indexOf('-') >= 0)
		{
			exportToStdOut();
		}
		// Normal file export
		if (_targetFile != null)
		try
		{
			BTextfile outp = new BTextfile(_targetFile);

			outp.rewrite(exportCharset);

			outp.write(code.getText());
			// START KGU#689 2019-03-21: Issue #706 - a non-empty text file should end with a newline
			if (!code.isEmpty()) {
				outp.write("\n");
			}
			// END KGU#689 2019-03-21
			outp.close();
			
			// START KGU#311 2016-12-27: Enh. #314 Allow the subclass to copy necessary resource files
			if (someRootUsesFileAPI) {
				copyFileAPIResources(_targetFile);
			}
			// END KGU#311 2016-12-27
		}
		catch(Exception e)
		{
			getLogger().log(Level.WARNING, "*** Error while saving the file \"{0}\"!\n{1}", new Object[]{_targetFile, e.getMessage()});
		}
	} 
	
	/**
	 * Subroutine for batch mode - writes the generated code to the console
	 * (for redirection purposes)
	 * Expects the target charset in field exportCharset and the code to be
	 * exported in field code.
	 */
	private void exportToStdOut()
	{
		OutputStreamWriter outp = null;
		try {
			outp = new OutputStreamWriter(System.out, exportCharset);
		} catch (UnsupportedEncodingException e) {
			// This should never happen since we have checked the Charset before...
			getLogger().log(Level.WARNING, "*** Unsupported Encoding: {0}", e.getMessage());
			outp = new OutputStreamWriter(System.out, Charset.defaultCharset());
		}
		try {
			BufferedWriter writer = new BufferedWriter(outp);
			writer.write(code.getText());
			writer.close();		// May we do this at all with an underlying System.out?
		} catch (IOException e) {
			getLogger().log(Level.WARNING, "*** Error on writing to stdout: {0}", e.getMessage());
		}
	}
	
	/******* FileFilter Extension *********/
	protected boolean isOK(String _filename)
	{
		boolean res = false;
		// START KGU 2016-01-16: Didn't work for mixed-case extensions like ".Mod" - and it was inefficient
//		if(getExtension(_filename)!=null)
//		{
//			for(int i =0; i<getFileExtensions().length; i++)
//			{
//				res = res || (getExtension(_filename).equals(getFileExtensions()[i]));
//			}
//		}
		String ext = getExtension(_filename); 
		if (ext != null)
		{
			for (int i =0; i<getFileExtensions().length; i++)
			{
				res = res || (ext.equalsIgnoreCase(getFileExtensions()[i]));
			}
		}
		// END KGU 2016-01-16
		return res;
	}
	
	private static String getExtension(String s) 
	{
		String ext = null;
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) 
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	private static String getExtension(File f) 
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
	
	public String getDescription() 
	{
		return getFileDescription();
	}

	public boolean accept(File f) 
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


	/******* Constructor ******************/

	public Generator()
	{
	}

}

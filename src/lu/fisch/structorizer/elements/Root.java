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

package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class represents the "root" of a diagram or the program/sub itself.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-09      First Issue
 *      Bob Fisch       2008-04-18      Added analyser
 *      Kay Gürtzig     2014-10-18      Var name search unified and false detection of "as" within var names mended
 *      Kay Gürtzig     2015-10-12      new methods toggleBreakpoint() and clearBreakpoints() (KGU#43).
 *      Kay Gürtzig     2015-10-16      getFullText methods redesigned/replaced, changes in getVarNames()
 *      Kay Gürtzig     2015-10-17      improved Arranger support by method notifyReplaced (KGU#48)
 *      Kay Gürtzig     2015-11-03      New error14 field and additions to analyse for FOR loop checks (KGU#3)
 *      Kay Gürtzig     2015-11-13/14   Method copy() accomplished, modifications for subroutine calls (KGU#2 = #9)
 *      Kay Gürtzig     2015-11-22/23   Modifications to support selection of Element sequences (KGU#87),
 *                                      Code revision in Analyser (field Subqueue.children now private).
 *      Kay Gürtzig     2015-11-28      Several additions to analyser (KGU#2 = #9, KGU#47, KGU#78 = #23) and
 *                                      saveToIni()
 *      Kay Gürtzig     2015-12-01      Bugfix #39 (KGU#91) -> getText(false) on drawing
 *      Bob Fisch       2015-12-10      Bugfix #50 -> grep parameter types (Method getParams(...))
 *      Kay Gürtzig     2015-12-11      Bugfix #54 (KGU#102) in getVarNames(): keywords within identifiers
 *      Kay Gürtzig     2015-12-20      Bugfix #50 (KGU#112) getResultType() slightly revised
 *      Kay Gürtzig     2016-01-02      Bugfixes #78 (KGU#119, equals()) and #85 (KGU#120, undo() etc.) 
 *      Kay Gürtzig     2016-01-06      Bugfix #89: References to obsolete operator padding (KGU#126) and
 *                                      faulty index condition for variable detection (KGU#98) fixed 
 *      Kay Gürtzig     2016-01-08      Bugfix #50 (KGU#135) postfix result type was split into lines  
 *      Kay Gürtzig     2016-01-11      Issue #103 (KGU#137): "changed" state now dependent on undo/redo
 *                                      stack, see comments below for details
 *      Kay Gürtzig     2016-01-14      Bugfix #103/#109: Saving didn't reset the hasChanged flag anymore (KGU#137)
 *      Kay Gürtzig     2016-01-16      Bugfix #112: Processing of indexed variables mended (KGU#141)
 *      Kay Gürtzig     2016-01-21      Bugfix #114: Editing restrictions during execution, breakpoint menu item
 *      Kay Gürtzig     2016-01-22      Bugfix for issue #38: moveUp/moveDown for selected sequences (KGU#144)
 *      Kay Gürtzig     2016-02-25      Bugfix #97 (= KGU#136): field rect replaced by rect0 in prepareDraw()
 *      Kay Gürtzig     2016-03-02      Bugfix #97 (= KGU#136) accomplished -> translation-independent selection
 *      Kay Gürtzig     2016-03-12      Enh. #124 (KGU#156): Generalized runtime data visualisation
 *      Kay Gürtzig     2016-03-21      Enh. #84 (KGU#61): For-In loops in variable detection and Analyser
 *      Kay Gürtzig     2016-03-25      Bugfix #135 (KGU#163) Method analyse(.,.,.,.,.) decomposed and corrected
 *      Kay Gürtzig     2016-03-29      Methods getUsedVarNames() completely rewritten.
 *      Kay Gürtzig     2016-04-05      Bugfix #154 (KGU#176) analyse_17() peeked in a wrong collection (Parallel)
 *      Kay Gürtzig     2016-04-12      Enh. #161 (KGU#179) analyse_13_16() extended (new error16_7)
 *      Kay Gürtzig     2016-04-24      Issue #169: Method findSelected() introduced, copy() modified (KGU#183)
 *      Kay Gürtzig     2016-07-07      Enh. #185 + #188: Mechanism to convert Instructions to Calls
 *      Kay Gürtzig     2016-07-19      Enh. #192: New method proposeFileName() involving the argument count (KGU#205)
 *      Kay Gürtzig     2016-07-22      Bugfix KGU#209 (Enh. #77): The display of the coverage marker didn't work
 *      Kay Gürtzig     2016-07-25      Bugfix #205: Variable higlighting worked only in boxed Roots (KGU#216)
 *      Kay Gürtzig     2016-07-27      Issue #207: New Analyser warning in switch text/comments mode (KGU#220)
 *      Kay Gürtzig     2016-07-28      Bugfix #208: Filling of subroutine diagrams no longer exceeds border
 *                                      Bugfix KGU#222 in collectParameters()
 *      Kay Gürtzig     2016-08-12      Enh. #231: New analyser checks 18, 19; checks reorganised to arrays
 *                                      for easier maintenance
 *      Kay Gürtzig     2016-09-21      Enh. #249: New analyser check 20 (function header syntax) implemented
 *      Kay Gürtzig     2016-09-25      Enh. #255: More informative analyser warning error_01_2. Dead code dropped.
 *                                      Enh. #253: CodeParser.keywordMap refactored
 *      Kay Gürtzig     2016-10-11      Enh. #267: New analyser check for error15_2 (unavailable subroutines)
 *      Kay Gürtzig     2016-10-12      Issue #271: user-defined prompt strings in input instructions
 *      Kay Gürtzig     2016-10-13      Enh. #270: Analyser checks for disabled elements averted.
 *      Kay Gürtzig     2016-11-22      Bugfix #295: Spurious error11 in return statements with equality comparison
 *      Kay Gürtzig     2016-12-12      Enh. #306: New method isEmpty() for a Root without text, children, and undo entries
 *                                      Enh. #305: Method getSignatureString() and Comparator SIGNATUR_ORDER added.
 *      Kay Gürtzig     2016-12-16      Bugfix #305: Comparator SIGNATURE_ORDER corrected
 *      Kay Gürtzig     2016-12-28      Enh. #318: Support for re-saving to an arrz file (2017.01.03: getFile() fixed)
 *      Kay Gürtzig     2016-12-29      Enh. #315: New comparison method distinguishing different equality levels
 *      Kay Gürtzig     2017-01-07      Enh. #329: New Analyser check 21 (analyse_18_19 renamed to analyse_18_19_21)
 *      Kay Gürtzig     2017-01-13      Enh. #305: Notification of arranger index listeners ensured on saving (KGU#330)
 *      Kay Gürtzig     2017-01-17      Enh. #335: Toleration of Pascal variable declarations in getUsedVarNames()
 *      Kay Gürtzig     2017-01-30      Enh. #335: Type info mechanism established
 *      Kay Gürtzig     2017-01-31      Bugfix in getParameterTypes() and getResultType() on occasion of issue #113
 *      Kay Gürtzig     2017-02-01      Enh. #259/#335: Parameters added to typeMap
 *      Kay Gürtzig     2017-02-07      KGU#343: Result analysis mechanism revised
 *      Kay Gürtzig     2017-03-06      Issue #368: Declarations are not to cause "uninitialized" warnings any longer
 *      Kay Gürtzig     2017-03-10      KGU#363: Enh. #372 (Simon Sobisch) new attributes author etc.
 *      Kay Gürtzig     2017-03-10/14   KGU#363: Enh. #372 (Simon Sobisch) new license attributes
 *      Kay Gürtzig     2017-03-14/26   Enh. #380: Method outsourceToSubroutine() supports automatic derival of subroutines
 *      Kay Gürtzig     2017-03-30      Enh. #388: const retrieval (method collectParameters() modified)
 *      Kay Gürtzig     2017-04-04      Enh. #388: New Analyser check for constant definitions (no. 22),
 *                                      method getUsedVarNames decomposed, check no. 10 enhanced.
 *      Kay Gürtzig     2017-04-05      Issue #390: Improved initialization check for multi-line instructions
 *      Kay Gürtzig     2017-04-11      Enh. #389: Analyser additions for import calls implemented
 *      Kay Gürtzig     2017-04-13      Enh. #380: Method outsourceToSubroutine() improved
 *      Kay Gürtzig     2017-04-14      Issues #23, #380, #394: analyse_13_16_jump() radically revised
 *      Kay Gürtzig     2017-04-21      Enh. #389: import checks re-organized to a new check group 23
 *      Kay Gürtzig     2017-05-06      Bugfix #397: Wrong insertion position with SelectedSequence as target
 *      Kay Gürtzig     2017-05-09      Enh. #372: Statistics method supporting the AttributeInspector
 *      Kay Gürtzig     2017-05-16      Enh. #389: Third diagram type introduced.
 *      Kay Gürtzig     2017-05-21      Enh. #372: additional attributes included in undo/redo mechanism
 *      Kay Gürtzig     2017-05-22      Enh. #272: New attribute "origin"
 *      Kay Gürtzig     2017-06-30      Enh. #389: New attribute "includeList"
 *      Kay Gürtzig     2017-07-02      Enh. #389: Analyser and execution mechanisms adapted to new include design
 *      Kay Gürtzig     2017-09-18      Enh. #423: Type retrieval and Analyser enhancement for record types
 *      Kay Gürtzig     2017-10-09      Enh. #423: Adjustments for Analyser check 24.
 *      Kay Gürtzig     2017-10-26      Enh. #423: Wrong type map reference in analyse_22_24() corrected
 *      Kay Gürtzig     2017-11-04      Enh. #452: More tutoring in Analyser, method getMethodName(boolean) introduced
 *      Kay Gürtzig     2017-11-05      Issue #454: logic of getMethodName() modified
 *      Kay Gürtzig     2018-03-12      Bugfix #518: Distinction between uninitialized and empty typeMap
 *      Kay Gürtzig     2018-03-15      Bugfix #522: makeTypedescription (for outsourcing) now considers record types,
 *                                      Bugfix #523: Defective undo and redo of include_list changes mended
 *                                      KGU#505: Analyser now copes better with lists of record access expressions
 *      Kay Gürtzig     2018-04-03      Bugfix #528: Record component access analysis mended and applied to all elements
 *      Kay Gürtzig     2018-04-04      Issue #529: Critical section in prepareDraw() reduced.
 *      Kay Gürtzig     2018-07-17      Issue #561: getElementCounts() modified for AttributeInspector update
 *      Kay Gürtzig     2018-07-20      Enh. #563: Analyser accepts simplified record initializers
 *      Kay Gürtzig     2018-07-25      Dropped field highlightVars (Element.E_VARHIGHLIGHT works directly now)
 *      Kay Gürtzig     2018-09-12      Refinement to #372: More file meta data used as workaround for missing author attributes 
 *      Kay Gürtzig     2018-09-17      Issue #594 Last remnants of com.stevesoft.pat.Regex replaced
 *      Kay Gürtzig     2018-09-24      Bugfix #605: Defective argument list parsing mended
 *      Kay Gürtzig     2018-09-28      Issue #613: New methods removeFromIncludeList(...)
 *      Kay Gürtzig     2018-10-04      Bugfix #618: Function names shouldn't be reported as used variables
 *      Kay Gürtzig     2018-10-25      Enh. #419: New methods breakElementTextLines(...), getMaxLineLength(...)
 *      Kay Gürtzig     2018-12-18      Bugfix #649: New method getElementCount(), use of cached variable names on redrawing
 *      Kay Gürtzig     2018-12-19      Bugfix #652: Drawing preparation and actual drawing were inconsistent
 *                                      w.r.t. the "Included Diagrams" box, such that ugly discrepancies appeared.
 *      Kay Gürtzig     2018-12-26      Method collectCalls(Element) moved hitherto from class Generator
 *      Kay Gürtzig     2019-02-16      Enh. #680: getUsedVarNames() fixed for multi-item INPUT (nearby fixed a bug with indexed variables)
 *      Kay Gürtzig     2019-03-07      Enh. #385: Support for default values in argument lists
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-03-20      Bugfix #706: analyse_15 hardened against inconsistent Call contents
 *      Kay Gürtzig     2019-03-21      Enh. #707: Configuration for file name proposals
 *      Kay Gürtzig     2019-03-28      Enh. #657: Retrieval for subroutines now with group filter
 *      Kay Gürtzig     2019-03-30      Issues #699, #718, #720 Handling of includeList and cached info
 *      Kay Gürtzig     2019-03-31      Issue #696 - field specialRoutinePool added, type retrieval may use it
 *      Kay Gürtzig     2019-08-02      Issue #733: New method getPreferenceKeys() for partial preference export
 *      Kay Gürtzig     2019-10-13/15   Bugfix #763: Test for stale file association in getFile(), new method copyWithFilepaths()
 *      Kay Gürtzig     2019-11-08      Enh. #770: New analyser checks 27, 28 (CASE elements)
 *      Kay Gürtzig     2019-11-13      New method getMereDeclarationNames() for bugfix #776
 *      Kay Gürtzig     2019-11-17      Enh. #739: Support for enum type definitions
 *      Kay Gürtzig     2019-11-21      Enh. #739: Bug in extractEnumerationConstants() fixed
 *      Kay Gürtzig     2020-02-21      Bugfix #825: The subsections of TRY elements hadn't been analysed
 *      Kay Gürtzig     2020-03-29      Bugfix #841: Analyser check for missing or misplaced parameter list didn't work
 *      Kay Gürtzig     2020-04-22      Bugfix #854: typeMap made a LinkedHashMap to ensure topological order on code export
 *      Kay Gürtzig     2020-10-16      Issue #874: Identifier check in Analyser modified with respect to non-ascii letters
 *      Kay Gürtzig     2020-10-19      Issue #875: New public method for the retrieval of potential arguments
 *      Kay Gürtzig     2021-01-02/06   Enh. #905: Mechanism to draw a warning symbol on related DetectedError
 *      Kay Gürtzig     2021-01-10      Enh. #910: Adaptations for new DiagramController approach
 *      Kay Gürtzig     2021-01-30      Bugfix #921: Outsourcing repaired w.r.t. enumerators
 *      Kay Gürtzig     2021-02-01      Bugfix #923: Analyser did not consider FOR loop variable types on record check
 *      Kay Gürtzig     2021-02-03      Bugfix #924: Record component names falsely detected as uninitialized variables
 *                                      Issue #920: `Infinity' introduced as literal
 *      Kay Gürtzig     2021-02-04      Bugfix #925: Type entry production for const parameters mended
 *      Kay Gürtzig     2021-02-08      Enh. #928: New Analyser check 29 (structured CASE discriminator)
 *      Kay Gürtzig     2021-02-22      Enh. #410: New field "namespace" to reflect e.g. class context
 *      Kay Gürtzig     2021-02-26      Enh. #410: Method getMethodName and getParameterDeclarations decomposed
 *                                      to static methods also usable by e.g. Call elements representing method
 *                                      declarations
 *      Kay Gürtzig     2021-02-28      Issue #947: Enhanced cyclic inclusion detection implemented
 *      Kay Gürtzig     2021-10-02      Bugfix #990: Fields returnsValue and alwaysReturns introduced to support export
 *      Kay Gürtzig     2021-10-03      Issue #991: Inconsistent Analyser check for result mechanism (case-ignorant)
 *      Kay Gürtzig     2021-10-05      Enh. #992: New Analyser check 30 against bracket faults
 *      Kay Gürtzig     2021-10-07      Bugfix #995: False Analyser accusations about insecure initialization status
 *      Kay Gürtzig     2021-11-12/14   Enh. #967: New plugin-specific Analyser syntax checks
 *      Kay Gürtzig     2021-12-05      Bugfix #1024: Malformed record initializer killed Analyser in check 24
 *      Kay Gürtzig     2021-12-13      Bugfix #1025: Lacking evaluation of binary, octal and hexadecimal literals in check 27
 *      Kay Gürtzig     2022-01-04      Bugfix #1026: Analyser defect on broken lines in Jump or Instruction elements
 *      
 ******************************************************************************************************
 *
 *      Comment:		/
 *      
 *      2019-03-07 (KGU#371)
 *      - Parameter list analysis has become still more complex, so it seemed sensible to cache the
 *        parameter list while the text doesn't change.
 *      2016-03-25 (KGU#163)
 *      - Detection of un-initialised variables (analyser check #3) only worked for variables with
 *        initialisation after use. Variables nowhere initialised weren't found at all! This was now
 *        eventually mended.
 *      2016-01-11 (KGU#137)
 *      - When changes are undone back to the moment of last file saving, the hasChanged is to be reset
 *      - Therefore, we now track the undo stack size when saving. As soon as an undo action returns to
 *        the recorded stack size, the hasChanged flag will be reset. Undoing more steps sets the
 *        flag again but keeps the stored stack size for the case of redoing forward to this point again.
 *      - As soon as an undoable editing below the recorded stack level occurs (wiping the redo stack),
 *        the recorded stack level will be set to an unreachable -1, because the saved state gets lost
 *        internally.
 *
 ******************************************************************************************************///

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.xml.sax.Attributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.image.*;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EmptyStackException;

import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.locales.LangTextHolder;
import lu.fisch.structorizer.locales.Locales;
import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.structorizer.generators.GeneratorSyntaxChecker;
import lu.fisch.structorizer.gui.*;

/**
 * This class represents the "root" of a diagram or the program/sub itself.
 * It is responsible for the behaviour of the entire diagram linked to it. 
 * It also represents the Analyser (performing all syntactic and style checks
 * over the diagram elements).
 * @author Bob Fisch
 */
public class Root extends Element {
	
	// KGU 2015-10-16: Just for testing purposes
	//private static int fileCounter = 1;

	// START KGU#305 2016-12-12: Enh. #305 / 2016-12-16: Case-independent comparison
	public static final Comparator<Root> SIGNATURE_ORDER =
			new Comparator<Root>() {
		public int compare(Root root1, Root root2)
		{
			String prefix1 = Integer.toString(root1.diagrType.ordinal());
			String prefix2 = Integer.toString(root2.diagrType.ordinal());
			// START KGU#408 2021-02-22: Enh. #410 Consider the qualified names
			//int result = (prefix1 + root1.getSignatureString(false)).compareToIgnoreCase(prefix2 + root2.getSignatureString(false));
			int result = (prefix1 + root1.getSignatureString(false, true)).compareToIgnoreCase(prefix2 + root2.getSignatureString(false, true));
			// END KGU#408 2021-02-22
			if (result == 0) {
				result = ("" + root1.getPath()).compareToIgnoreCase("" + root2.getPath());
			}
			return result;
		}
	};
	// END KGU#305 2016-12-12
	
	// START KGU#575 2018-09-17: Issue #594 - we delegate the replacement to a static method on Element 
	//private final static java.util.regex.Pattern INC_PATTERN1 = java.util.regex.Pattern.compile(BString.breakup("inc")+"[(](.*?)[)](.*?)");
	//private final static java.util.regex.Pattern INC_PATTERN2 = java.util.regex.Pattern.compile(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)");
	//private final static java.util.regex.Pattern DEC_PATTERN1 = java.util.regex.Pattern.compile(BString.breakup("dec")+"[(](.*?)[)](.*?)");
	//private final static java.util.regex.Pattern DEC_PATTERN2 = java.util.regex.Pattern.compile(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)");
	//private final static java.util.regex.Pattern INDEX_PATTERN = java.util.regex.Pattern.compile("(.*?)[\\[](.*?)[\\]](.*?)");
	private final static Pattern INDEX_PATTERN = Pattern.compile("(.*?)[\\[](.*?)[\\]](.*?)");
	private final static Pattern INDEX_PATTERN_GREEDY = java.util.regex.Pattern.compile("(.*?)[\\[](.*)[\\]](.*?)");
	// END KGU#575 2018-09-17
	// START KGU#580 2018-09-24: Bugfix #605
	private final static Pattern VAR_PATTERN = Pattern.compile("(^|.*?\\W)var\\s(.*?)");
	// END KGU#580 2018-09-24
	
	// START KGU#376 2017-05-16: Enh. #389 - we introduce a third diagram type now
	public static final int R_CORNER = 15;

	// START KGU#911 2021-01-10: Enh. #910 sepecial sub type of Includable (no editing, no saving)
	//public enum DiagramType {DT_MAIN, DT_SUB, DT_INCL};
	public enum DiagramType {DT_MAIN, DT_SUB, DT_INCL, DT_INCL_DIAGRCTRL};
	// END KGU#911 2021-01-10
	private DiagramType diagrType = DiagramType.DT_MAIN;
	// END KGU#376 2017-05-16

	// some fields
	public boolean isBoxed = true;
	//private boolean isProgram = true;
	// START KGU#137 2016-01-11: Bugfix #103 - More precise tracking of changes
	//public boolean hasChanged = false;
	private boolean hasChanged = false;		// Now only for global, not undoable changes
	private int undoLevelOfLastSave = 0;	// Undo stack level recorded on saving
	// END KGU#137 2016-01-11
	//public boolean highlightVars = false;
	// START KGU#2 (#9) 2015-11-13:
	/** Executor: Is this routine currently waiting for a called subroutine? */
	public boolean isCalling = false;
	// END KG#2 (#9) 2015-11-13
	// START KGU#376 2017-07-02: Enh. #389 - we want to show execution in inlcudeList
	public boolean isIncluding = false;
	// END KGU#376 2017-07-02
	// START KGU#408 2021-02-22: Enh. #410 Introduced on occasion of Java import
	/** A qualifier usable e.g. to reflect package (in case of a class) or class membership */
	private String namespace = null;
	// END KGU#408 2021-02-22
	
	// START KGU#624 2018-12-22: Enh. #655
	/** selection flags for different drawing contexts. Index 0 is unused (instead field {@link #selected} is used) */ 
	private boolean[] contextSelections = new boolean[DrawingContext.values().length];
	// END KGU#624 2018-12-22
	
	// START KGU#676 2019-03-31: Enh. #696 - We need pool access for batch export
	/** A routine pool to be used for retrieval of includables and subroutines instead of Arranger if not null */
	public IRoutinePool specialRoutinePool = null;
	// END KGU#676 2019-03-31
	
	public Subqueue children = new Subqueue();

	public int height = 0;
	public int width = 0;
	
	// START KGU#136 2016-03-01: Bugfix #97 - sensibly, we cache the subqueue extensions
	private Point pt0Sub = new Point(0,0);
	// END KGU#136 2016-03-01

	private Stack<Subqueue> undoList = new Stack<Subqueue>();
	private Stack<Subqueue> redoList = new Stack<Subqueue>();

	public String filename = "";
	// START KGU#316 2016-12-28: Enh. #318 Consider unzipped arrz-files
	public String shadowFilepath = null;	// temp file path of an unzipped file
	// END KGU#316 2016-12-28
	// START KGU#362 2017-03-28: Issue #370 - retain original keywords if not refactored - makes Root readonly!
	public HashMap<String, StringList> storedParserPrefs = null;
	// END KGU#362 2017-03-28
	// START KGU#363 2017-03-10: Enh. #372
	private String author = null;
	private String modifiedby = null;
	private Date created = null;
	private Date modified = null;
	// START KGU#363 2018-09-12: Enh. #372 - for undo/redo we need to cache some original meta info
	public String modifiedby0 = null;		// Original entry for modifiedby
	// END KGU#363 2018-09-12	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public String licenseName = null;
	public String licenseText = null;
	public String origin = "Structorizer " + E_VERSION;
	
	// START KGU#990 2021-10-02: Bugfix #990
	public Boolean returnsValue = null;
	// END KGU#990 2021-10-02
	
	// START KGU#371 2019-03-07: Enh. #385 for performance reasons, we cache the parameter list here
	/** Cached parameter list, each entry is composed of name, type, and default, each as Strings (defaults may be null) */
	private ArrayList<Param> parameterList = null;
	// END KGU#371 2019-03-07
	
	// START KGU#376 2017-06-30: Enh. #389: Includable diagrams now managed directly by Root
	/** List of the names of the diagrams to be included by this Root (may be null!) */
	public StringList includeList = null;
	/**
	 * Checks, whether {@code aRoot} is includable and if so, ensures that its name
	 * becomes member of this' include list.
	 * @param aRoot - a diagram to be added to the include list of this
	 * @return true if {@code aRoot} is includable and new to the include list
	 * @see #addToIncludeList(String)
	 * @see #removeFromIncludeList(Root)
	 */
	public boolean addToIncludeList(Root aRoot)
	{
		boolean added = false;
		if (aRoot.isInclude()) {
			added = addToIncludeList(aRoot.getMethodName());
		}
		return added;
	}
	/**
	 * Ensures that the given {@code rootName} (which is assumed to be the name of an
	 * includable diagram, but not verified) is member of this' include list. If the
	 * {@link #includeList} was null, then it will be created.
	 * @param rootName - assumed name of an includable Root
	 * @return true if {@code rootName} was new
	 * @see #addToIncludeList(Root)
	 * @see #removeFromIncludeList(String)
	 */
	public boolean addToIncludeList(String rootName)
	{
		if (this.includeList == null) {
			this.includeList = new StringList();
		}
		return this.includeList.addIfNew(rootName);
	}
	// END KGU#376 2017-06-30
	// START KGU#586 2018-09-28: Introduced on occasion of #613
	/**
	 * Ensures that {@link Root} {@code aRoot} is not member of the {@link #includeList}
	 * @param aRoot - an includable {@link Root}
	 * @return true if {@code aRoot} had been included before
	 * @see #removeFromIncludeList(String)
	 * @see #addToIncludeList(Root)
	 */
	public boolean removeFromIncludeList(Root aRoot)
	{
		return aRoot.isInclude() && this.removeFromIncludeList(aRoot.getMethodName());
	}
	/**
	 * Ensures that the given {@code rootName} (which is assumed to be the name of an
	 * includable diagram, but not verified) is NOT member of this' include list.
	 * @param rootName - assumed name of an includable {@link Root}
	 * @return true if the assumed includable had been member of the include list
	 * @see #removeFromIncludeList(Root)
	 * @see #addToIncludeList(String)
	 */
	public boolean removeFromIncludeList(String rootName)
	{
		boolean done = false;
		if (this.includeList != null) {
			done = this.includeList.removeAll(rootName) > 0;
		}
		return done;
	}
	// END KGU#586 2018-09-28
	
	// START KGU#408 2021-02-22: Enh. #410 getter and setter for new field namespace
	/**
	 * Sets the given name prefix {@code _qualifier} as {@link #namespace} attribute
	 * if it is an identifier sequence, separated by dots like e.g. a Java package name.
	 * An empty string or {@code null} will nullify the namespace.
	 * Does not modify this object otherwise.
	 * @param _qualifier - the proposed name prefix or {@code null}.
	 * @return {@code true} if the proposed _qualifier met the syntax requirements
	 * @see #getNamespace()
	 * @see #getQualifiedName()
	 * @see #getSignatureString(boolean, boolean)
	 */
	public boolean setNamespace(String _qualifier)
	{
		boolean done = _qualifier == null || (_qualifier =_qualifier.trim()).isEmpty() ||
				Function.testIdentifier(_qualifier, true, ".");
		if (done) {
			namespace = _qualifier;
			if (namespace != null && namespace.isEmpty()) {
				namespace = null;
			}
		}
		return done;
	}
	
	/**
	 * @return the specified name qualifier (e.g. a package name or class access path)
	 * or {@code null}.
	 * @see #setNamespace(String)
	 * @see #getQualifiedName()
	 * @see #getSignatureString(boolean, boolean)
	 */
	public String getNamespace()
	{
		return namespace;
	}
	// END KGU#408 2021-02-22
	
	/**
	 * @return true if and only if the diagram type is "main program"
	 * @see #isSubroutine()
	 * @see #isInclude()
	 * @see #setProgram(boolean)
	 * @see #setInclude(boolean)
	 */
	public boolean isProgram() {
		return diagrType == DiagramType.DT_MAIN;
	}
	/**
	 * Sets the diagram type to "main program" or "subroutine"
	 * @param isProgram - if the diagram type is to be "main program" (true) or "subroutine"
	 * (false)
	 * @see #isProgram()
	 * @see #isSubroutine()
	 * @see #isInclude()
	 * @see #setInclude(boolean)
	 */
	public void setProgram(boolean isProgram) {
		if (isProgram) {
			diagrType = DiagramType.DT_MAIN;
		} else {
			diagrType = DiagramType.DT_SUB;
		}
	}
	/**
	 * @return true if and only if the diagram type is subroutine
	 * @see #isProgram()
	 * @see #isInclude()
	 * @see #setProgram(boolean)
	 * @see #setInclude(boolean)
	 */
	public boolean isSubroutine() {
		return diagrType == DiagramType.DT_SUB;
	}
	/**
	 * @return true if and only if the diagram type is "includable"
	 * @see #isProgram()
	 * @see #isSubroutine()
	 * @see #setInclude(boolean)
	 * @see #setProgram(boolean)
	 */
	public boolean isInclude() {
		// START KGU#911 2021-01-10: Enh. #910 New subtype for DiagramControllers
		//return diagrType == DiagramType.DT_INCL;
		return diagrType == DiagramType.DT_INCL || diagrType == DiagramType.DT_INCL_DIAGRCTRL;
		// END KGU#911 2021-01-10
	}
	/**
	 * Sets the diagram type to "includable" (ordinary)
	 * @see #isInclude()
	 * @see #isProgram()
	 * @see #isSubroutine()
	 * @see #setProgram(boolean)
	 */
	public void setInclude()
	// START KGU#911 2021-01-10: Enh. #910
	//{
	//	diagrType = DiagramType.DT_INCL;
	//}
	{
		setInclude(true);
	}
	/**
	 * Sets the diagram type to "includable" (ordinary)
	 * @param usual - {@code true} for usual (user-defined) Includable,
	 *       {@code false} for an immutable, not storable {@link DiagramController}
	 *       representative
	 * @see #isInclude()
	 * @see #isProgram()
	 * @see #isSubroutine()
	 * @see #setProgram(boolean)
	 */
	public void setInclude(boolean usual)
	{
		diagrType = usual ? DiagramType.DT_INCL : DiagramType.DT_INCL_DIAGRCTRL;
	}
	/**
	 * @return {@code true} iff this is an Includable representing a {@link DiagramController}
	 * @see #isInclude()
	 * @see #setInclude(boolean)
	 */
	public boolean isRepresentingDiagramController()
	{
		return diagrType == DiagramType.DT_INCL_DIAGRCTRL;
	}
	// END KGU#911 2021-01-10
	public String getAuthor() {
		return this.author;
	}
	public void setAuthor(String authorName) {
		this.author = authorName;
	}
	public String getModifiedBy() {
		return this.modifiedby;
	}
	public Date getCreated() {
		return this.created;
	}
	public String getCreatedString() {
		if (this.created == null) {
			return "";
		}
		return dateFormat.format(this.created);
	}
	public Date getModified() {
		return this.modified;
	}
	public String getModifiedString() {
		if (this.modified == null) {
			return "";
		}
		return dateFormat.format(this.modified);
	}
	/**
	 * Retrieves the author attributes from the XML node {@code attributes}
	 * @param attributes - The {@link Attributes} of the originating XML node
	 * @see #fetchAuthorDates(File,File)
	 */
	public void fetchAuthorDates(Attributes attributes)
	{
		if(attributes.getIndex("author")!=-1)  {
			this.author = attributes.getValue("author");
		}
		if(attributes.getIndex("created")!=-1)  {
			try {
				this.created = this.dateFormat.parse(attributes.getValue("created"));
			} catch (ParseException e) {}
		}
		if(attributes.getIndex("changedby")!=-1)  {
			this.modifiedby = attributes.getValue("changedby") ; 
		}
		if(attributes.getIndex("changed")!=-1)  {
			try {
				this.modified = this.dateFormat.parse(attributes.getValue("changed"));
			} catch (ParseException e) {} 
		}
	}
	// END KGU#363 2017-03-10
	// START KGU#363 2017-05-21: Enh. #372
	/**
	 * Tries to extract sensible file attribute values and to use them to initialize the
	 * author attributes before {@link #fetchAuthorDates(Attributes)} is called.<br/>
	 * Be aware that the diagram meta data are meant to override the file meta data, the
	 * use of which is only a workaround for legacy nsd files that didn't store own
	 * meta data.  
	 * @param _file - the file from which this diagram was loaded
	 * @param _arrzFile - the arrz file, which {@code _file} was extracted from, or null
	 * @see #fetchAuthorDates(Attributes)
	 */
	public void fetchAuthorDates(File _file, File _arrzFile) {
		// Override the constructor settings if the Root is loaded from file
		this.created = null;
		this.author = "???";
		// START KGU#363 2018-09-11: Improvement to retrieve suited file attributes as defaults
		Path path = _file.toPath();
		Path ownerPath = path;
		if (_arrzFile != null) {
			ownerPath = _arrzFile.toPath();
		}
		try {
			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
			long createTime = attrs.creationTime().toMillis();
			// If the file was unzipped then the creation time of the arrz file is more sensible
			if (_arrzFile != null) {
				createTime = Files.readAttributes(ownerPath, BasicFileAttributes.class).creationTime().toMillis();
			}
			Date created = null;
			// Check if the OS supports creation time attributes at all
			if (createTime > 0) {
				created = new Date(createTime);
			}
			this.modified = new Date(attrs.lastModifiedTime().toMillis());
			FileOwnerAttributeView view = Files.getFileAttributeView(ownerPath, FileOwnerAttributeView.class);
			// Ignore creation times later than the last modification (usually this means that the
			// file has been copied after its last modification - creation time has a rather physical
			// meaning, it does not refer to the content)
			if (created.before(this.modified)) {
				this.created = created;
				// The owner might be the author
				this.author = view.getOwner().getName();
			}
			else {
				// This may be disputed but the owner is more likely the last modifier
				this.modifiedby = view.getOwner().getName();
			}
		} catch (IOException e) {}
		// END KGU#363 2018-09-11
		this.licenseName = null;
		if (_file.canRead()) {
			long modTime = _file.lastModified();
			if (modTime != 0L && (this.modified == null || this.modified.getTime() < modTime) &&
					(this.created == null || this.created.getTime() < modTime)) {
				this.modified = new Date(modTime);
			}
		}
	}
	// END KGU#363 2017-05-21

	/**
	 * Names of variables defined within this diagram (may be null after changes,
	 * lazy initialization!)
	 * @see #getCachedVarNames()
	 */
	// START KGU#444/KGU#618 2018-12-18 - Issues #417, #649 We want to distinguish empty from invalid
	//public StringList variables = new StringList();
	// START KGU#701 2019-03-30: Issue #718 - This should better not be accessible directly - we need to be aware of changes
	//public StringList variables = null;
	private StringList variables = null;
	// END KGU#701 2ß19-03-30
	
	/**
	 * @return Cached names of the variables defined within this diagram (may be empty after changes).
	 * Will not retrieve variables.
	 * @see #variables
	 * @see #retrieveVarNames()
	 * @see #getVarNames()
	 */
	public StringList getCachedVarNames()
	{
		if (variables != null) {
			return variables;
		}
		return new StringList();
	}
	// END KGU#444/KGU#618 2018-12-18
	
	// START KGU#686 2019-03-16: Enh. #56 (introduction of Try blocks)
	@Override
	protected Set<String> getVariableSetFor(Element _element)
	{
		Set<String> varNames = new HashSet<String>();
		// START KGU#701 2019-03-30: Issue #718 - we must react to changes
		//StringList vars = getCachedVarNames();
		StringList vars = getVarNames();
		// END KGU#701 2019-03-30
		for (int i = 0; i < vars.count(); i++) {
			varNames.add(vars.get(i));
		}
		return varNames;
	}
	// END KGU#686 2019-03-16
	
	// START KGU#375 2017-03-31: Enh. #388
	/**
	 * Names and cached value expressions of detected constants among the {@link #variables}.
	 * For enumeration type constants, the actual value is prefixed with ":" + typename + "€".
	 * @see #getConstValueString(String) 
	 */
	public LinkedHashMap<String, String> constants = new LinkedHashMap<String, String>();
	// END KGU#375 2017-03-31
	/**
	 * Vector containing Element-related Analyser complaints
	 */
	public Vector<DetectedError> errors = new Vector<DetectedError>();
	
	private StringList rootVars = new StringList();
	// START KGU#261 2017-01-19: Enh. #259 (type map: (var name | type name) -> type info)
	// START KGU#502 2018-03-12: Bugfix #518 - distinguish between uninitialized and resulting empty map 
	//private HashMap<String, TypeMapEntry> typeMap = new HashMap<String, TypeMapEntry>();
	private HashMap<String, TypeMapEntry> typeMap = null;
	// END KGU#502 2018-03-12
	// END KGU#261 2017-01-19
	// START KGU#163 2016-03-25: Added to solve the complete detection of unknown/uninitialised identifiers
	/** Pre-processed parser preference keywords to match them against tokenized strings */
	private static Vector<StringList> splitKeywords = new Vector<StringList>();
	// START KGU#920 2021-02-02: Issue #920 Infinity allowed as literal
	/** Specific names not to be mistaken as uninitialized variables in unified texts */
	//private String[] operatorsAndLiterals = {"false", "true", "div"};
	private String[] operatorsAndLiterals = {"false", "true", "Infinity", "div"};
	// END KGU#920 2021-02-03
	// END KGU#163 2016-03-25

	// error checks for analyser (see also addError(), saveToIni(), Diagram.analyserNSD() and Mainform.loadFromIni())
	// START KGU#239 2016-08-12: Inh. #231 + Partial redesign
	// KGU#456 2017-11-05: Now used as initial defaults
	private static boolean[] analyserChecks = {
		true,	true,	true,	true,	false,	// 1 .. 5
		false,	true,	true,	true,	true,	// 6 .. 10
		true,	false,	true,	true,	true,	// 11 .. 15
		true,	true,	true,	true,	true,	// 16 .. 20
		true,	true,	true,	true,	false,	// 21 .. 25
		false,	true,	true,	true,	true	// 26 .. 30
		// Add another element for every new check...
		// and DON'T FORGET to append its description to
		// AnalyserPreferences.checkCaptions
	};
	public static int numberOfChecks()
	{
		return analyserChecks.length;
	}
	/**
	 * Returns whether the Analyser check #{@code checkNo} is enabled
	 * @param checkNo - an official Analyser check number 
	 * @return true if the check is enabled, false otherwise
	 */
	public static boolean check(int checkNo)
	{
		// enable all unknown checks by default
		return checkNo < 1 || checkNo > analyserChecks.length
				|| analyserChecks[checkNo-1];
	}
	public static void setCheck(int checkNo, boolean enable)
	{
		if (checkNo >= 1 && checkNo <= analyserChecks.length)
		{
			analyserChecks[checkNo-1] = enable;
		}
	}
	// END KGU#239 2016-08-12
	
	// START KGU#1012 2021-11-12: Enh. #967 New plugin-specific checks
	/**
	 * Maps plugin-specific syntax checker descriptions to their
	 * respective activation status
	 * 
	 * @see lu.fisch.structorizer.helpers.GENPlugin#syntaxCheck
	 * @see lu.fisch.structorizer.generators.GeneratorSyntaxChecker#checkSyntax(String, Element, int)
	 */
	private static final HashMap<String, Boolean> pluginChecks = new HashMap<String, Boolean>();
	/**
	 * Returns whether the plugin-specific syntax check {@code pluginCheck} is
	 * configured and enabled
	 * @param pluginCheck - the plugin check specifier (class name : check type code)
	 * @return true if the check is enabled, false otherwise
	 */
	public static boolean check(String pluginCheck)
	{
		return pluginChecks.containsKey(pluginCheck) && pluginChecks.get(pluginCheck);
	}
	public static void setCheck(String pluginCheck, boolean enable)
	{
		pluginChecks.put(pluginCheck, enable);
	}
	private static HashMap<String, GeneratorSyntaxChecker> pluginSyntaxCheckers = null;
	// END KGU#1012 2021-11-12
	
	// START KGU#456 2017-11-05: Issue #452
	/** Current state of analyser guides */
	private Queue<Integer> tutorialQueue = new LinkedList<Integer>();
	/**
	 * Step number of the current tutorial (-1 = not begun)
	 * @see #getTutorialState(int)
	 * @see #startNextTutorial(boolean)
	 * @see #advanceTutorialState(int, Root)
	 */
	private int tutorialState = -1;
	public int getCurrentTutorial()
	{
		Integer checkNo = tutorialQueue.peek();
		while (checkNo != null && !check(checkNo)) {
			tutorialQueue.remove();
			checkNo = tutorialQueue.peek();
		}
		if (checkNo == null) {
			checkNo = -1;
		}
		return checkNo;
	}
	public int getTutorialState(int checkNo) {
		int state = -1;
		if (check(checkNo) && tutorialQueue.peek() == checkNo) {
			state = tutorialState;
		}
		return state;
	}
	/**
	 * Review the {@code tutorialQueue} according to the given ordered array
	 * of available tutorial numbers.
	 * @param tutorials - numbers of available tutorials in didactic order
	 */
	public void updateTutorialQueue(int[] tutorials)
	{
		// First check whether the first guide in the queue has begun and is still valid
		Integer started = tutorialQueue.poll();
		if (started != null && (!check(started) || tutorialState < 0)) {
			// obsolete, so drop it
			started = null;
		}
		tutorialQueue.clear();
		if (started != null) {
			// Re-insert the started guide
			tutorialQueue.add(started);
		}
		else {
			started = -1;
			tutorialState = -1;
		}
		// Now add all currently activated tutorials
		for (int guideCode : tutorials) {
			if (guideCode != started && check(guideCode)) {
				tutorialQueue.add(guideCode);
			}
		}
		if (started == -1) {
			this.startNextTutorial(false);
		}
	}
	/**
	 * Starts the next tutorial in the queue that is not disabled (disabled
	 * tutorial numbers will be dropped from the queue)
	 * @param _disableCurrent - if true then the current guide will be switched off and a success message is popped up
	 * @return - the number of the started tutorial
	 */
	public int startNextTutorial(boolean _disableCurrent)
	{
		Integer checkNo = tutorialQueue.peek();
		String message = null;
		if (checkNo != null && _disableCurrent) {
			setCheck(checkNo, false);
			String[] checkDescr = AnalyserPreferences.getCheckTabAndDescription(checkNo);
			message = Menu.msgGuidedTourDone.getText().replace("%", checkDescr[1]);
		}
		while ((checkNo = tutorialQueue.peek()) != null && !check(checkNo)) {
			tutorialQueue.remove();
		}
		if (checkNo != null) {
			tutorialState = 0;
			if (_disableCurrent) {
				String[] checkDescr = AnalyserPreferences.getCheckTabAndDescription(checkNo);
				StringList menuNew = new StringList(Menu.getLocalizedMenuPath(new String[]{"menuFile", "menuFileNew"}, new String[]{"File", "New"}));
				message = Menu.msgGuidedTourNext.getText().
						replace("%1", message).
						replace("%2", checkDescr[1]).
						replace("%3", menuNew.concatenate(" \u25BA "));
			}
		}
		else {
			checkNo = -1;
			tutorialState = -1;
		}
		if (message != null) {
			JOptionPane.showMessageDialog(null, 
					message, 
					Menu.ttlGuidedTours.getText(), 
					JOptionPane.INFORMATION_MESSAGE,
					IconLoader.getIcon(24));
		}
		return checkNo;
	}
	/**
	 * Checks status for tutorial {@code CheckNo} on diagram {@code root} and
	 * advances the {@link #tutorialState} if it's due.
	 * @param checkNo - the Analyser code for the tutorial
	 * @param root - the affected diagram
	 * @return {@code true} if the next step of the tutorial was granted
	 */
	public boolean advanceTutorialState(int checkNo, Root root)
	{
		if (!check(checkNo) || tutorialQueue.isEmpty() || tutorialQueue.peek() != checkNo) {
			return false;
		}
		if	(root == null ||
				root != null && root.isTutorialReadyForStep(checkNo, tutorialState+1)) {
			tutorialState++;
			return true;
		}
		return false;
	}
	// END KGU#456 2017-11-05
	// Mapping keyword -> generator titles
	private static Hashtable<String, StringList> caseAwareKeywords = null;
	private static Hashtable<String, StringList> caseUnawareKeywords = null;
	// END KGU#239 2016-08-12
	// START KGU#239 2017-04-11: Some structorizer-internal keywords are also to be checked against
	private static Set<String> structorizerKeywords = new HashSet<String>();
	// END KGU#239 2017-04-11

	private Vector<Updater> updaters = new Vector<Updater>();

	// KGU#91 2015-12-04: No longer needed
	//private boolean switchTextAndComments = false;

	public Root()
	{
		super(StringList.getNew("???"));
		setText(StringList.getNew("???"));	// This looked redundant
		children.parent=this;
		// START KGU#363 2017-03-10: Enh. #372 - Author and date fields
		author = Ini.getInstance().getProperty("authorName", System.getProperty("user.name"));
		if (author.isEmpty()) {
			author = System.getProperty("user.name");
		}
		created = new Date();
		// END KGU#363 2017-03-10
		// START KGU#363 2017-03-14: Enh. #372 - License fields
		licenseName = Ini.getInstance().getProperty("licenseName", "");
		// END KGU#363 2017-03-14
		// START KGU#624 2018-12-22: Enh. #655 - Independent selection markers for certain contexts
		for (int i = 0; i < this.contextSelections.length; i++) {
			this.contextSelections[i] = false;
		}
		// END KGU#624 2018-12-22
		//this(StringList.getNew("???"));
	}

	public Root(StringList _header)
	{
		super(_header);
		setText(_header);	// This looked redundant
		children.parent=this;
		// START KGU#363 2017-03-10: Enh. #372 - Author and date fields
		author = Ini.getInstance().getProperty("authorName", System.getProperty("user.name"));
		if (author.isEmpty()) {
			author = System.getProperty("user.name");
		}
		created = new Date();
		// END KGU#363 2017-03-10
		// START KGU#363 2017-03-14: Enh. #372 - License fields
		licenseName = Ini.getInstance().getProperty("licenseName", "");
		// END KGU#363 2017-03-14
	}
	
	// START KGU#371 2019-03-07: Enh. #385 - clear parameter list cache
	@Override
	public void setText(String _text)
	{
		text.setText(_text);
		parameterList = null;
	}

	@Override
	public void setText(StringList _text)
	{
		text = _text;
		parameterList = null;
	}
	// END KGU#371 2019-03-07

    public void addUpdater(Updater updater)
    {
    	// START KGU#48 2015-10-17: While this.updaters is only a Vector, we must avoid multiple registration...
    	//updaters.add(updater);
    	if (!updaters.contains(updater))
    	{
    		updaters.add(updater);
    	}
    	// END KGU#48 2015-10-17
    }

    public void removeUpdater(Updater updater)
    {
        updaters.remove(updater);
    }
    
    // START KGU#2 (#9) 2015-11-14: We need a way to get the Updaters
    public Iterator<Updater> getUpdateIterator()
    {
    	return updaters.iterator();
    }
    // END KGU#2 (#9) 2015-11-14

    // START KGU#48 2015-10-17: Arranger support on Root replacement (e.g. by loading a new file)
    public void notifyReplaced(Root newRoot)
    {
    	// FIXME: Something here may provoke a java.util.ConcurrentModificationException
    	//System.out.println("Trying to notify my replacement to " + updaters.size() + " Updaters..."); // FIXME (KGU) Remove after successful test!
    	Iterator<Updater> iter = updaters.iterator();
    	while (iter.hasNext())
    	{
    		//System.out.println(this.getMethodName() + " notifying an Updater about replacement.");
    		iter.next().replaced(this, newRoot);
    	}
    	updaters.clear();
    }
    // END KGU#48 2015-10-17
    
    // START KGU#137 2016-01-11: Bugfix #103 - Enhanced change tracking, synchronized with undoing/redoing/saving
    /**
     * Sets an additional sticky changed flag for saveable global settings that are not subject
     * of the undo/redo stacks
     * @param setModifiedAttrs - if true then attributes {@link #modified} and {@link #modifiedby} will be set (not undoable!) 
     */
    // START KGU#363 2018-09-12: Fixes #372 - not always it is sensible to set modified attributes
    public void setChanged(boolean setModifiedAttrs)
    {
    	this.hasChanged = true;
    	// START KGU#363 2017-03-10: Enh. #372, KGU#363 2018-09-12 made dependent on argument
    	if (setModifiedAttrs) {
    		// END KGU#363 2018-09-12
    		this.modifiedby = Ini.getInstance().getProperty("authorName", System.getProperty("user.name"));
    		if (modifiedby.trim().isEmpty()) {
    			modifiedby = System.getProperty("user.name");
    		}
    		this.modified = new Date();
    	// END KGU#363 2017-03-10
    	// START KGU#363 2017-03-10: Enh. #372, KGU#363 2018-09-12 made dependent on argument
    	}
		// END KGU#363 2018-09-12
    }

    /**
     * Detects if changes (no matter if undoable or not) have been registered since last saving
     * @return true if there have been changes not undone
     */
    public boolean hasChanged()
    {
    	return this.hasChanged || this.undoLevelOfLastSave != this.undoList.size();
    }
    // END KGU#137 2016-01-11

	// START KGU 2015-10-13: This follows a code snippet found in Root.draw(Canvas, Rect), which had been ineffective though
	@Override
	public Color getColor()
	{
		if (isBoxed)
		{
			// The surrounding box is obvious - so it can't be mistaken for an instruction
			return Color.WHITE;
		}
		else
		{
			// The grey colour helps to distinguish the header from instructions
			return Color.LIGHT_GRAY;
		}
	}
	// END KGU 2015-10-13
	
	// START KGU#306 2016-12-12: Enh. #306
	public boolean isEmpty()
	{
		String txt = this.text.concatenate().trim();
		boolean isEmpty = 
				(txt.isEmpty() || txt.equals("???")) &&
				this.comment.concatenate().trim().isEmpty() &&
				this.children.getSize() == 0 &&
				this.undoList.isEmpty() &&
				this.redoList.isEmpty();
		return isEmpty;
	}
	// END KGU#306 2016-12-12
	
	public Rect prepareDraw(Canvas _canvas)
	{
		// START KGU#136 2016-03-01: Bugfix #97 (prepared)
		if (this.isRect0UpToDate) return rect0.copy();
		// START KGU#516 2018-04-04: Directly to work on field rect0 was not so good an idea for re-entrance
		//pt0Sub.x = 0;
		// END KGU#516 2018-04-04
		// END KGU#136 2016-03-01
		
		//  KGU#136 2016-02-25: Bugfix #97 - all rect references replaced by rect0
		// START KGU#516 2018-04-04: Issue #529 - Directly to work on field rect0 was not so good an idea for re-entrance
		//rect0.top = 0;
		//rect0.left = 0;
		Rect rect0 = new Rect();
		Rect subrect0 = new Rect();
		Point pt0Sub = new Point();
		// END KGU#516 2018-04-04

		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		Font titleFont = new Font(Element.font.getName(),Font.BOLD,Element.font.getSize());
		_canvas.setFont(titleFont);

		// Compute width (dependent on diagram style and text properties)
		int padding = 2*(E_PADDING/2);
		if (isBoxed)
		{
			padding = 2 * E_PADDING;
			pt0Sub.x = E_PADDING;
		}
		rect0.right = 2 * E_PADDING;
		for (int i=0; i<getText(false).count(); i++)
		{
			int width = getWidthOutVariables(_canvas,getText(false).get(i),this) + padding;
			if (rect0.right < width)
			{
				rect0.right = width;
			}
		}
		
		// Compute height (depends on diagram style and number of text lines)
		int vPadding = isBoxed ? 3 * E_PADDING : padding;
		rect0.bottom = vPadding + getText(false).count() * fm.getHeight();
		
		// START KGU#227 2016-07-31: Enhancement #128
		Rect commentRect = new Rect();
		if (Element.E_COMMENTSPLUSTEXT)
		{
			commentRect = this.writeOutCommentLines(_canvas, 0, 0, false);
			commentRect.right += padding;
			if (rect0.right < commentRect.right)
			{
				rect0.right = commentRect.right;
			}
			rect0.bottom += commentRect.bottom;
		}
		// END KGU#227 2016-07-31
		
		pt0Sub.y = rect0.bottom;
		if (isBoxed)	pt0Sub.y -= E_PADDING;

		_canvas.setFont(Element.font);

		subrect0 = children.prepareDraw(_canvas);
		if (isBoxed)
		{
			rect0.right = Math.max(rect0.right, subrect0.right + 2*Element.E_PADDING);
		}
		else
		{
			rect0.right = Math.max(rect0.right, subrect0.right);
		}

		rect0.bottom += subrect0.bottom;
		// START KGU#221 2016-07-28: Bugfix #208 - partial boxing for un-boxed subroutine or includable
		if (!isBoxed && !isProgram()) rect0.bottom += E_PADDING/2;
		// END KGU#221 2016-07-28

		// START KGU#376 2017-07-01: Enh. #389 - determine the required size for the import list
		// KGU#621 2018-12-19: Bugfix #652 - children size exploration has to be done before!
		if (this.includeList != null) {
			// KGU#621 2018-12-19: Bugfix #652 - Padding assumptions corrected
			Rect includesBox = this.writeOutImports(_canvas, 0, 0, rect0.right - padding, false);
			int inclHeight = includesBox.bottom - includesBox.top + E_PADDING/2;
			rect0.bottom += inclHeight;
			rect0.right = Math.max(rect0.right, includesBox.right - includesBox.left + padding);
			pt0Sub.y += inclHeight;
		}
		// END KGU#376 2017-07-01
		
		this.width = rect0.right - rect0.left;
		this.height = rect0.bottom - rect0.top;
		
		// START KGU#516 2018-04-04: Issue #529 - reduced critical section
		this.rect0 = rect0;
		this.pt0Sub = pt0Sub;
		// END KGU#516 2018-04-04
		// START KGU#136 2016-03-01: Bugfix #97
		isRect0UpToDate = true;
		// END KGU#136 2016-03-01
		return rect0.copy();
	}

	public void drawBuffered(Canvas _canvas, Rect _top_left)
	{
		// save reference to output canvas
		Canvas origCanvas = _canvas;
		// create a new image (buffer) to draw on
		BufferedImage bufferImg = new BufferedImage(_top_left.right+1, _top_left.bottom+1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bufferGraphics = (Graphics2D) bufferImg.getGraphics();
		_canvas = new Canvas(bufferGraphics);


		draw(_canvas, _top_left, null, false);

		// draw buffer to output canvas
		origCanvas.draw(bufferImg,0,0);

		// free up the buffer and clean memory
		bufferImg = null;
		System.gc();
	}

	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention)
	{
		draw(_canvas, _top_left, _viewport, _inContention, DrawingContext.DC_STRUCTORIZER);
	}
	
	public void draw(Canvas _canvas, Rect _top_left, Rectangle _viewport, boolean _inContention, DrawingContext _context)
	{
		// START KGU#502/KGU#524/KGU#553 2019-03-13: New approach to reduce drawing contention
		if (!checkVisibility(_viewport, _top_left)) { return; }
		// END KGU#502/KGU#524/KGU#553 2019-03-13

		// START KGU 2015-10-13: Encapsulates all fundamental colouring and highlighting strategy
		//Color drawColor = getColor();
		Color drawColor = getFillColor(_context);
		// END KGU 2015-10-13

		// FIXME: Drawing shouldn't modify the element
		if (getText().isEmpty())
		{
			text.add("???");
		}
		else if ( ((String)getText().get(0)).trim().equals("") )
		{
			text.delete(0);
			text.insert("???",0);
		}

		Rect myRect = _top_left.copy();

		// draw background

		Canvas canvas = _canvas;

		// erase background
		canvas.setBackground(drawColor);
		canvas.setColor(drawColor);
		// START KGU#221 2016-07-27: Bugfix #208, KGU#376 2017-05-16: third type
		//canvas.fillRect(_top_left);
		int bevel = isBoxed ? R_CORNER : E_PADDING/2;
		switch (diagrType) {
		case DT_SUB:
			canvas.fillRoundRect(_top_left, R_CORNER);
			break;
		case DT_INCL:
		// START KGU#911 2021-01-10: Enh. #910
		case DT_INCL_DIAGRCTRL:
		// END KGU#911 2021-01-10
			canvas.fillPoly(this.makeBevelledRect(_top_left, bevel));
			break;
		default:
			canvas.fillRect(_top_left);
		}
		// END KGU#221 2016-07-27

		// draw comment
		if (E_SHOWCOMMENTS==true && !getComment(false).getText().trim().equals(""))
		{
			// START KGU#221 2016-07-27: Bugfix #208
			//this.drawCommentMark(_canvas, _top_left);
			Rect commRect = _top_left.copy();
			if (isSubroutine())
			{
				commRect.top += E_PADDING/2;
				commRect.bottom -= E_PADDING/2;
			}
			else if (isInclude())
			{
				commRect.top += bevel;
			}
			this.drawCommentMark(_canvas, commRect);
			// END KGU#221 2016-07-27
		}

		int textPadding = isBoxed ? E_PADDING : E_PADDING/2;

		// START KGU#227 2016-07-31: Enh. #128
		int commentHeight = 0;
		if (Element.E_COMMENTSPLUSTEXT)
		{
			Rect commentRect = this.writeOutCommentLines(_canvas,
					_top_left.left + textPadding,
					_top_left.top + textPadding,
					true);
			commentHeight += commentRect.bottom - commentRect.top;
		}
		// END KGU#227 2016-07-31
		
		FontMetrics fm = _canvas.getFontMetrics(Element.font);
		Font titleFont = new Font(Element.font.getName(),Font.BOLD,Element.font.getSize());
		canvas.setFont(titleFont);

		// START KGU#376 2017-07-01: Enh. #389 - determine the required size for the import list
		if (this.includeList != null) {
			Rect includesBox = this.writeOutImports(_canvas,
					_top_left.left + textPadding,
					_top_left.top + textPadding + commentHeight,
					width - 2 * textPadding, true);
			commentHeight += includesBox.bottom - includesBox.top + E_PADDING/2;
		}
		// END KGU#376 2017-07-01

		// draw text
		// START KGU#216 2016-07-25: Bug #205 - Except the padding the differences here had been wrong
		for (int i = 0; i < getText(false).count(); i++)
		{
			canvas.setColor(Color.BLACK);
			writeOutVariables(canvas,
							  myRect.left + textPadding,
							  // START KGU#227 2016-07-31: Enh. #128
							  //rect.top + (i+1)*fm.getHeight() + textPadding,
							  myRect.top + (i+1)*fm.getHeight() + textPadding + commentHeight,
							  // END KGU#227 2016-07-31
							  (String)getText(false).get(i),
							  this,
							  _inContention);
		}
		// write the run-time info if enabled (Enh. #124)
		this.writeOutRuntimeInfo(canvas, myRect.right - textPadding, myRect.top);
		// END KGU#216 2016-07-25
		
		// START KGU#906 2021-01-02: Enh. #905
		this.drawWarningSignOnError(canvas, myRect);
		// END KGU#906 2021-01-02

		canvas.setFont(Element.font);
		
		// Draw the frame around the body
		// START #227 2016-07-31: Enh. #128 + Code revision
		Rect bodyRect = _top_left.copy();
		bodyRect.left += pt0Sub.x;
		bodyRect.top += pt0Sub.y;
		bodyRect.right -= pt0Sub.x;	// Positioning is symmetric!
		if (isBoxed)
		{
			bodyRect.bottom -= E_PADDING;
		}
		else if (!isProgram())
		{
			bodyRect.bottom -= E_PADDING/2;
		}
		
		children.draw(_canvas, bodyRect, _viewport, _inContention);
		// END KGU#227 2016-07-31

		// draw box around
		canvas.setColor(Color.BLACK);
		// START KGU#221 2016-07-27: Bugfix #208
		//canvas.drawRect(_top_left);
		if (isProgram())
		{
			canvas.drawRect(_top_left);
		}
		// END KGU##221 2016-07-27


		// draw thick line
		if (isBoxed == false)
		{
			Rect sepRect = bodyRect.copy();
			sepRect.bottom = sepRect.top--;
			//rect.left = _top_left.left;
			canvas.drawRect(sepRect);
			// START KGU#221 2016-07-28: Bugfix #208
			if (!isProgram())
			{
				sepRect.top = bodyRect.bottom;
				sepRect.bottom = sepRect.top + 1;
				canvas.drawRect(sepRect);
			}
			// END KGU#221 2016-07-28
		}


		if (!isProgram())
		{
			//rect = _top_left.copy();
			// START KGU#221 2016-07-27: Bugfix #208
			//canvas.setColor(Color.WHITE);
			//canvas.drawRect(rect);
//			if (!isNice)
//			{
//				canvas.setColor(Color.WHITE);
//				canvas.drawRect(rect);
//			}
			// END KGU#221 2016-07-27
			canvas.setColor(Color.BLACK);
			myRect = _top_left.copy();
			// START KGU#376 2017-05-16: Enh. #389
			//canvas.roundRect(rect);
			if (isSubroutine()) {
				canvas.roundRect(myRect, R_CORNER);
			}
			else {
				canvas.drawPoly(this.makeBevelledRect(myRect, bevel));
			}
			// END KGU#376 2017-05-16
		}

		// START KGU#136 2016-03-01: Bugfix #97 - store rect in 0-bound (relocatable) way
		//rect = _top_left.copy();
		rect = new Rect(0, 0, 
				_top_left.right - _top_left.left, _top_left.bottom - _top_left.top);
		this.topLeft.x = _top_left.left - this.drawPoint.x;
		this.topLeft.y = _top_left.top - this.drawPoint.y;
		// END KGU#136 2016-03-01
		// START KGU#502/KGU#524/KGU#553 2019-03-14: Bugfix #518,#544,#557
		wasDrawn = true;
		// END KGU#502/KGU#524/KGU#553 2019-03-14
	}
	
	// START KGU#376 2017-07-01: Enh. #389
	/**
	 * Draws (or calculates) a box with the names of the diagrams to be included.<br/>
	 * NOTE: Should only be called if includeList isn't empty.
	 * @param _canvas - the current drawing surface
	 * @param _x - left margin coordinate
	 * @param _y - upper margin coordinate
	 * @param maxWidth - maximum width of the box
	 * @param _actuallyDraw - draw (true) or only calculate size (false)
	 * @return The resulting shape of the box
	 */
	protected Rect writeOutImports(Canvas _canvas, int _x, int _y, int _maxWidth, boolean _actuallyDraw)
	{
		int height = 0;				// Pure total text height (without padding)
		int width = 0;				// Pure maximum text width (without padding)
		int padding = E_PADDING/2;	// Box-internal padding
		// smaller font
		int smallFontSize = Element.font.getSize() * 2 / 3;
		Font smallFont = new Font(Element.font.getName(), Font.PLAIN, smallFontSize);
		Font smallBoldFont = new Font(Element.font.getName(), Font.BOLD, smallFontSize);
		// backup the original font
		Font backupFont = _canvas.getFont();
		_canvas.setFont(smallFont);
		_canvas.setColor(Color.BLACK);
		FontMetrics fm = _canvas.getFontMetrics(smallFont);
		int fontHeight = fm.getHeight();
		String caption = Element.preImport.trim();
		int captionX = _x + padding;
		int captionY = _y;
		if (!caption.isEmpty()) {
			if (!caption.endsWith(":")) {
				caption += ":";
			}
			if (this.includeList.count() > 0) {
				height += fontHeight/4 + fontHeight;	// upper padding + string height
				width = _canvas.stringWidth(caption);
				// START KGU#621 2018-12-19: Bugfix #652 For the further comparison we may have to enlarge _maxWidth
				if (!_actuallyDraw) {
					_maxWidth = Math.max(width + 2 * padding, _maxWidth);
				}
				// END KGU#621 2018-12-19
				captionY = _y + height;
			}
		}
		fm = _canvas.getFontMetrics(smallBoldFont);
		fontHeight = fm.getHeight();
		_canvas.setFont(smallBoldFont);
		String line = "";
		StringList includeLines = new StringList();
		// START KGU#621 2018-12-19: Bugfix #652
		// In preparation phase we must ensure a width that corresponds at least to the longest name
		if (!_actuallyDraw) {
			for (int i = 0; i < this.includeList.count(); i++) {
				_maxWidth = Math.max(_maxWidth, _canvas.stringWidth(this.includeList.get(i)) + 2 * padding);
			}
		}
		// END KGU#621 2018-12-19
		for (int i = 0; i < this.includeList.count(); i++) {
			String name = this.includeList.get(i);
			// Since the leading ", " will be cut off we don't have to add ", " for the test
			// In theory 2 * padding would have to be added for comparison but this turned out too large
			if (line.isEmpty() || 2 * padding + _canvas.stringWidth(line + name) < _maxWidth)
			{
				// The space is wide enough to append name, or we have no choice
				line += ", " + name;
			}
			else {
				// Stash the current line (cannot be extended)
				height += fontHeight;
				line = line.substring(2);	// Cut off the leading ", " from the old line
				width = Math.max(width, _canvas.stringWidth(line));
				includeLines.add(line);
				// Start a new line
				line = ", " + name;			// Place the name to the new line
			}
		}
		// Is there a begun line? Then stash it
		if (!line.isEmpty())
		{
			height += fontHeight;
			line = line.substring(2);	// Cut off the leading ", " from the last line
			width = Math.max(width, _canvas.stringWidth(line));
			includeLines.add(line);
		}
		
		Rect inclBox = new Rect(_x, _y, _x + Math.max(_maxWidth, width + 2 * padding), _y + height);
		if (height > 0)
		{
			inclBox.bottom += fontHeight/2;
			if (_actuallyDraw) {
				Polygon bevelled = this.makeBevelledRect(inclBox, fontHeight/2);
				if (this.isIncluding) {
					_canvas.setColor(Element.E_RUNNINGCOLOR);
					_canvas.fillPoly(bevelled);
				}
				_canvas.setColor(Color.GRAY);
				_canvas.drawPoly(bevelled);
			}
		}
		if (_actuallyDraw) {
			_canvas.setColor(Color.BLACK);
			if (!caption.isEmpty()) {
				_canvas.setFont(smallFont);
				if (this.includeList.count() > 0) {
					_canvas.writeOut(captionX, captionY, caption);
				}
				_canvas.setFont(smallBoldFont);
				for (int i = 0; i < includeLines.count(); i++) {
					captionY += fontHeight;
					_canvas.writeOut(captionX, captionY, includeLines.get(i));
				}
			}
		}
		_canvas.setFont(backupFont);
		return inclBox;
	}


	// START KGU#376 2017-05-16: Enh. #389
    private Polygon makeBevelledRect(Rect _rect, int _bevel) {
    	// We start with the lower left corner in clockwise direction, the upper left and the
    	// lower right corners will be bevelled with _bevel argument
    	int[] xCoords = new int[] {
    			_rect.left,
    			_rect.left,				// left edge
    			_rect.left + _bevel,	// upper left bevel
    			_rect.right,			// top edge
    			_rect.right,			// right edge
    			_rect.right - _bevel,	// lower right bevel
    			//_rect.left			// closes automatically
    	};
    	int[] yCoords = new int[] {
    			_rect.bottom,
    			_rect.top + _bevel,		// left edge
    			_rect.top,				// upper left bevel
    			_rect.top,				// top edge
    			_rect.bottom - _bevel,	// right edge
    			_rect.bottom,			// lower right bevel
    			//_rect.bottom			// closes automatically
    	}; 
    	return  new Polygon(xCoords, yCoords, xCoords.length);
    }
    // END KGU#376 2017-05-16
    
    // START KGU#324 2017-06-16: Enh. #415 we need an icon for the find result tree
    /**
     * @return a type-specific image icon e.g. to be used in the {@link FindAndReplace} result
     * tree. 
     */
    @Override
    public ImageIcon getIcon()
    {
    	switch (this.diagrType) {
    	case DT_INCL:
    	// START KGU#911 2021-01-10: Enh. #910
    	case DT_INCL_DIAGRCTRL:
    	// END KGU#911 2021-01-10
    		return IconLoader.getIcon(71);
    	case DT_SUB:
    		return IconLoader.getIcon(21);
    	case DT_MAIN:
    		return IconLoader.getIcon(22);
    	}
    	return super.getIcon();
    }
    // END KGU#324 2017-06-16

    // START KGU#535 2018-06-28
    /**
     * @return the (somewhat smaller) element-type-specific icon image intended to be used in
     * {@link FindAndReplace} dialog.
     * @see #getIcon()
     */
    @Override
    public ImageIcon getMiniIcon()
    {
    	return this.getIcon();
    }
    // END KGU#535 2018-06-28

    @Override
    public Element getElementByCoord(int _x, int _y, boolean _forSelection)
    {
            // START KGU#136 2016-03-01: Bugfix #97 - now we relativate cursor position rather than rectangles
//            Point pt = getDrawPoint();
//            _x -= pt.x;
//            _y -= pt.y;
            // END KGU#136 2016-03-01

            Element selMe = super.getElementByCoord(_x, _y, _forSelection);
            // START KGU#136 2016-03-01: Bugfix #97
            //Element selCh = children.getElementByCoord(_x, _y, _forSelection);
            Element selCh = children.getElementByCoord(_x - pt0Sub.x, _y - pt0Sub.y, _forSelection);
            // END KGU#136 2016-03-01
            if(selCh!=null)
            {
                    if (_forSelection) selected = false;
                    return selCh;
            }
            else
            {
                    return selMe;
            }
    }
    // END KGU 2015-10-11

	// START KGU#183 2016-04-24: Issue #169 
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#findSelected()
	 */
	public Element findSelected()
	{
		Element sel = selected ? this : null;
		if (sel == null)
		{
			sel = children.findSelected();
		}
		return sel;
	}
	// END KGU#183 2016-04-24
	
	// START KGU 2019-03-14 Helps to place imported Roots more sensibly in Arranger
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRect()
	 */
	@Override
	public Rect getRect()
	{
		if (!wasDrawn && isRect0UpToDate) {
			return rect0.copy();
		}
		return super.getRect();
	}

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#getRect(java.awt.Point)
	 */
	@Override
	public Rect getRect(Point relativeTo)
	{
		Rect myRect = rect;
		if (!wasDrawn && isRect0UpToDate) {
			myRect = rect0;
		}
		return new Rect(myRect.left + relativeTo.x, myRect.top + relativeTo.y,
				myRect.right + relativeTo.x, myRect.bottom + relativeTo.y);		
	}
	// END KGU 2019-03-14

    public void removeElement(Element _ele)
    {
            if(_ele != null)
            {
                    _ele.selected=false;
                    // START KGU#87 2015-11-22: Allow to remove entire non-empty Subqueues
                    //if ( !_ele.getClass().getSimpleName().equals("Subqueue") &&
                    //         !_ele.getClass().getSimpleName().equals("Root"))
                    if ( _ele instanceof IElementSequence)
                    {
                    	// START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                    	//hasChanged = ((IElementSequence)_ele).getSize() > 0;
                    	// END KGU#137 2016-01-11
                    	((IElementSequence)_ele).removeElements();
                    	// START KGU#136 2016-03-01: Bugfix #97
                    	_ele.resetDrawingInfoUp();
                    	// END KGU#136 2016-03-01
                    }
                    else if (!_ele.getClass().getSimpleName().equals("Root"))
                    // END KGU#87 2015-11-22
                    {
                            ((Subqueue) _ele.parent).removeElement(_ele);
                            // START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                            //hasChanged=true;
                            // END KGU#137 2016-01-11
                            // START KGU#136 2016-03-01: Bugfix #97
                            _ele.parent.resetDrawingInfoUp();
                            // END KGU#136 2016-03-01
                    }
            }
    }

    private void insertElement(Element _ele, Element _new, boolean _after)
    {
            if (_ele != null && _new != null)
            {
                    if (_ele.getClass().getSimpleName().equals("Subqueue"))
                    {
                            ((Subqueue) _ele).addElement(_new);
                            _ele.selected = false;
                            _new.selected = true;
                            // START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                            //hasChanged=true;
                            // END KGU#137 2016-01-11
                            // START KGU#136 2016-07-07: Bugfix #97 - now delegated to Subqueue
                            //_ele.resetDrawingInfoUp();
                            // END KGU#136 2016-07-07
                    }
                    else if (_ele.parent.getClass().getSimpleName().equals("Subqueue"))
                    {
                            // START KGU#389 2017-05-06: Bugfix #397 - wrong placement if _ele was a SelectedSequence
                            //int i = ((Subqueue) _ele.parent).getIndexOf(_ele);
                            Element target = _ele;
                            if (_ele instanceof IElementSequence) {
                            	int elIndex = 0;
                            	if (_after) {
                            		elIndex = ((IElementSequence)_ele).getSize()-1;
                            	}
                            	target = ((IElementSequence)_ele).getElement(elIndex);
                            }
                            int i = ((Subqueue) _ele.parent).getIndexOf(target);
                            // END KGU#389 2017-05-06
                            if (_after) i++;
                            ((Subqueue) _ele.parent).insertElementAt(_new, i);
                            _ele.selected = false;
                            _new.selected = true;
                            // START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                            //hasChanged=true;
                            // END KGU#137 2016-01-11
                            // START KGU#136 2016-03-01: Bugfix #97 - now delegated to Subqueue
                            //_ele.parent.resetDrawingInfoUp();
                            // END KGU#136 2016-03-01
                    }
                    else
                    {
                            // this case should never happen!
                    }

            }
            // START KGU#477 2017-12-06: Enh. #487
            _ele.resetDrawingInfoUp();
            // END KGU#477 2017-12-06
    }
    
    public void addAfter(Element _ele, Element _new)
    {
    	insertElement(_ele, _new, true);
    }
    
    public void addBefore(Element _ele, Element _new)
    {
    	insertElement(_ele, _new, false);
    }
    
    
    // START KGU#43 2015-10-12: Breakpoint support
    @Override
    public void toggleBreakpoint()
    {
    	// root may never have a breakpoint!
    	breakpoint = false;
    }
    	
	// START KGU#117 2016-03-06: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#isTestCovered(boolean)
	 */
	public boolean isTestCovered(boolean _deeply)
	{
		// START KGU#209 2016-07-22 If Root is marked as deeply covered then it is to report it
		//return this.children.isTestCovered(_deeply);
		return this.deeplyCovered || this.children.isTestCovered(_deeply);
		// END KGU#209 2016-07-22
	}
	// END KGU#117 2016-03-06

    public Rect prepareDraw(Graphics _g)
    {
        Canvas canvas = new Canvas((Graphics2D) _g);
        canvas.setFont(Element.getFont()); //?
        return this.prepareDraw(canvas);
    }
    
    /**
     * Draws this diagram at anchor position {@code _point} (upper left corner) on {@link Graphics}
     * {@code _g}, where {@link Updater} {@code _prohibitedUpdater} is NOT allowed to refresh (in order to avoid
     * stack overflow due to endless recursion).
     * @param _g - the target graphics environment
     * @param _point - the target position
     * @param _viewport - visible area of the viewport
     * @param _prohibitedUpdater - if given an updater not to be informed
     * @param _drawingContext - the context e.g. for selection highlighting 
     * @param _inContention TODO
     * @return the area occupied by this diagram as {@link Rect}
     */
    public Rect draw(Graphics _g, Point _point, Rectangle _viewport, Updater _prohibitedUpdater, DrawingContext _drawingContext, boolean _inContention)
    {
        setDrawPoint(_point);

        /*
        final Updater myUpdater = updater;
        new Thread(
            new Runnable()
            {
                public void run()
                {
                    // inform updaters
                    for(int u=0;u<updaters.size();u++)
                    {
                        if(updaters.get(u)!=myUpdater)
                        {
                            updaters.get(u).update();
                        }
                    }
                }
            }
        ).start();/**/

        // inform updaters
        for (int u = 0; u < updaters.size(); u++)
        {
            if (updaters.get(u) != _prohibitedUpdater)
            {
                updaters.get(u).update(this);
            }
        }

        Canvas canvas = new Canvas((Graphics2D) _g);
        // START KGU#906 2021-01-02: Enh. #905
        if (_drawingContext == DrawingContext.DC_STRUCTORIZER) {
            canvas.setFlag(CANVAS_FLAGNO_ERROR_CHECK);
        }
        // END KGU#906 2021-01-02
        canvas.setFont(Element.getFont()); //?
        Rect myrect = this.prepareDraw(canvas);
        myrect.left += _point.x;
        myrect.top += _point.y;
        myrect.right += _point.x;
        myrect.bottom += _point.y;
        this.draw(canvas, myrect, _viewport, _inContention, _drawingContext);

        return myrect;
    }

//    public Rect draw(Graphics _g, Point _point)
//    {
//        return draw(_g, _point, null, null, DrawingContext.DC_STRUCTORIZER);
//    }

    public Rect draw(Graphics _g, Rectangle _viewport)
    {
        return draw(_g, new Point(0,0), _viewport, null, DrawingContext.DC_STRUCTORIZER, false);

        /*
        // inform updaters
        for(int u=0;u<updaters.size();u++)
        {
            updaters.get(u).update();
        }

        Canvas canvas = new Canvas((Graphics2D) _g);
        canvas.setFont(Element.getFont()); //?
        Rect myrect = this.prepareDraw(canvas);
        //this.drawBuffered(canvas,myrect);
        this.draw(canvas,myrect);

        return myrect;/**/
    }
    
    // START KGU#906 2021-01-06: Enh. #905
    public Rect draw(Graphics _g, Rectangle _viewport, DrawingContext _context)
    {
        return draw(_g, new Point(0,0), _viewport, null, _context, false);
    }
    // END KGU#906 2021-01-06
    
    public boolean getSelected(DrawingContext _drawingContext)
    {
    	if (_drawingContext == DrawingContext.DC_STRUCTORIZER) {
    		return getSelected();
    	}
    	return this.contextSelections[_drawingContext.ordinal()];
    }

	public Element setSelected(boolean _sel, DrawingContext _drawingContext)
	{
		if (_drawingContext == DrawingContext.DC_STRUCTORIZER) {
			return setSelected(_sel);
		}
		this.contextSelections[_drawingContext.ordinal()] = _sel;
		return _sel ? this : null;
	}

	// START KGU#749 2019-10-15: Issue #763 - we must compensate the changes in Diagram.saveNSD(Root, boolean)
	/**
	 * Like {@link #copy()} but also copies the file paths (particularly important
	 * for recursive execution to avoid eternal saving requests).
	 * 
	 * @return the copied {@link Root}
	 */
	public Root copyWithFilepaths()
	{
		Root cpy = (Root)this.copy();
		cpy.filename = this.filename;
		cpy.shadowFilepath = this.shadowFilepath;
	}
	// END KGU#749 2019-10-15

    @Override
    public Element copy()
    {
            Root ele = new Root(this.getText().copy());
            copyDetails(ele, false);
            ele.isBoxed=this.isBoxed;
            ele.diagrType = this.diagrType;
            ele.children=(Subqueue) this.children.copy();
            // START KGU#2 (#9) 2015-11-13: By the above replacement the new children were orphans
            ele.children.parent = ele;
            //ele.updaters = this.updaters;	// FIXME: Risks of this?
            // END KGU#2 (#9) 2015-11-13
            // START KGU#704 2019-03-30: Bugfix #699 - we must not forget to clone the includeList
            if (this.includeList != null) {
                ele.includeList = this.includeList.copy();
            }
            // END KGU#704 2019-03-30
            // START KGU#363 2017-03-10: Enh. #372
            ele.author = this.author;
            ele.created = this.created;
            this.modifiedby = Ini.getInstance().getProperty("authorName", System.getProperty("user.name"));
            if (modifiedby.trim().isEmpty()) {
            	modifiedby = System.getProperty("user.name");
            }
            ele.modified = new Date();
            // END KGU#363 2017-03-10
            // START KGU#408 2021-02-22: Enh. #410
            ele.namespace = this.namespace;
            // END KGU#408 2021-02-22
            return ele;
    }
    
	// START KGU#119 2016-01-02: Bugfix #78
	/**
	 * Returns true iff _another is of same class, all persistent attributes are equal, and
	 * all substructure of _another recursively equals the substructure of this. 
	 * @param another - the Element to be compared
	 * @return true on recursive structural equality, false else
	 */
	@Override
	public boolean equals(Element _another)
	{
		// START KGU#408 2021-02-24: Enh. #410 Involve namespace
		//return super.equals(_another) && this.children.equals(((Root)_another).children);
		return super.equals(_another)
				&& (this.namespace == null && ((Root)_another).namespace == null
					|| this.namespace != null && ((Root)_another).namespace != null
					&& this.namespace.equals(((Root)_another).namespace))
				&& this.children.equals(((Root)_another).children);
		// END KGU#408 2021-02-24
	}
	// END KGU#119 2016-01-02
	
	// START KGU#312 2016-12-29: Enh. #315
	/**
	 * Equivalence check returning one of the following similarity "levels":<br/>
	 * 0: no resemblance<br/>
	 * 1: Identity (i. e. the Java Root elements are identical);<br/>
	 * 2: Exact equality (i. e. objects aren't identical but all attributes
	 *    and structure are recursively equal AND the file paths are equal
	 *    AND there are no unsaved changes in both diagrams);<br/>
	 * 3: Equal file path but unsaved changes in one or both diagrams (this
	 *    can occur if several Structorizer instances in the same application
	 *    loaded the same file independently);<br/>
	 * 4: Equal contents but different file paths (may occur if a file copy
	 *    is loaded or if a Structorizer instance just copied the diagram
	 *    with "Save as");<br/>
	 * 5: Equal signature (i. e. type, name and argument number) but different
	 *    content or structure.
	 * @param another - the Root to compare with
	 * @return a resemblance code according to the description above
	 */
	public int compareTo(Root another)
	{
		int resemblance = 0;
		if (this == another) {
			resemblance = 1;
		}
		else if (this.equals(another)) {
			if (this.getPath().equals(another.getPath())) {
				if (this.hasChanged() || another.hasChanged()) {
					resemblance = 3;
				}
				else {
					resemblance = 2;
				}
			}
			else {
				resemblance = 4;
			}
		}
		else if (this.getSignatureString(false, false).equals(another.getSignatureString(false, false))) {
			resemblance = 5;
		}
		return resemblance;
	}
	// END KGU#312 2016-12-29
	
	// START KGU#117 2016-03-07: Enh. #77
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#combineCoverage(lu.fisch.structorizer.elements.Element)
	 */
	@Override
	public boolean combineRuntimeData(Element _cloneOfMine)
	{
		return super.combineRuntimeData(_cloneOfMine) && this.children.combineRuntimeData(((Root)_cloneOfMine).children);
	}
	// END KGU#117 2016-03-07
	
	// START KGU#117 2016-03-12: Enh. #77
	protected String getRuntimeInfoString()
	{
		return this.getExecCount() + " / (" + this.getExecStepCount(true) + ")";
	}
	// END KGU#117 2016-03-12
	
	// START KGU#376 2017-07-02: Enh. #389
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#clearExecutionStatus()
	 */
	public void clearExecutionStatus()
	{
		super.clearExecutionStatus();
		this.isIncluding = false;
	}
	// END KGU#376 2017-07-02

	/**
	 * Adds a new entry to the undo stack and clears the redo stack.
	 * Supposed to be called before undoable changes to this diagram are applied. 
	 */
	public void addUndo()
	{
		addUndo(false);
	}
	
	/**
	 * Adds a new entry to the undo stack, including a snapshot of the editable Root attributes (like author name,
	 * license data etc.) if {@code _cacheAttributes} is true. Only to be called before changes to the attributes
	 * are expected. Otherwise {@link #addUndo()} should be used.<br/>
	 * Clears the redo stack.
	 * @param _cacheAttributes - specifies whether diagram attributes are also to be cached.
	 * @see #addUndo()
	 */
	public void addUndo(boolean _cacheAttributes)
	{

		Subqueue oldChildren = (Subqueue)children.copy(); 
		// START KGU#120 2016-01-02: Bugfix #85 - park my StringList attributes on the stack top
		oldChildren.setText(this.text.copy());
		oldChildren.setComment(this.comment.copy());
		// END KGU#120 2016-01-02
		// START KGU#363 2017-05-21: Enh. #372: Care for the new attributes
		if (_cacheAttributes) oldChildren.rootAttributes = new RootAttributes(this);
		// END KGU#363 3017-05-21
		// START KGU#363 2018-09-12: Enh. #372: Some meta data must always be cached
		oldChildren.modified = this.modified;
		if (undoList.empty()) {
			this.modifiedby0 = this.modifiedby;
		}
		// END KGU#363 3018-09-12
		// START KGU#990 2021-10-02: Bugfix #990 New fields for export facilitation
		this.returnsValue = null;
		// END KGU#990 2021-10-02
		// START KGU#376 2017-07-01: Enh. #389
		if (this.includeList != null) {
			oldChildren.diagramRefs = this.includeList.concatenate(",");
		}
		// END KGU#376 2017-07-01
		undoList.add(oldChildren);
		clearRedo();
		// START KGU#137 2016-01-11: Bugfix #103
		// If stack was lower than when last saved, then related info is going lost
		if (undoList.size() <= this.undoLevelOfLastSave)
		{
			this.undoLevelOfLastSave = -1;
		}
		// END KGU#137 2016-01-11
		// START KGU#261/KGU#444/KGU#618/KGU#701 2019-03-30: Issues #259, #417, #649, #718
		this.clearVarAndTypeInfo(true);
		// END KGU#261/KGU#444/KGU#618/KGU#701 2018-12-18
		// START KGU#117 2016-03-07: Enh. #77: On a substantial change, invalidate test coverage
		this.clearRuntimeData();
		// END KGU#117 2016-03-07
		// START KGU#701/KGU#703 2019-03-30: Issue #718
		if (this.isInclude() && Arranger.hasInstance()) {
			for (Root ref: Arranger.getInstance().findIncludingRoots(this.getMethodName(), true)) {
				ref.clearVarAndTypeInfo(false);
			}
		}
		// END KGU#701/KGU#703 2019-03-30
		// START KGU#363 2017-03-10: Enh. #372
		this.modifiedby = Ini.getInstance().getProperty("authorName", System.getProperty("user.name"));
		if (modifiedby.trim().isEmpty()) {
			modifiedby = System.getProperty("user.name");
		}
		this.modified = new Date();
		// END KGU#363 2017-03-10
	}

	/**
	 * Checks whether there are stacked undoable changes
	 * @return true if there are entries on the undo stack and diagram is not being executed
	 * @see #undo()
	 * @see #addUndo()
	 * @see #clearUndo()
	 * @see #canRedo()
	 */
	public boolean canUndo()
	{
		// START KGU#143 2016-01-21: Bugfix #114 - we cannot allow a redo while an execution is pending
		//return (undoList.size()>0);
		return (undoList.size() > 0) && !this.waited;
		// END KGU#143 2016-01-21
	}

	/**
	 * Checks whether there are stacked redoable changes
	 * @return true if there are entries on the redo stack and diagram is not being executed
	 * @see #redo()
	 * @see #undo()
	 * @see #clearRedo()
	 * @see #canUndo()
	 */
	public boolean canRedo()
	{
		// START KGU#143 2016-01-21: Bugfix #114 - we cannot allow a redo while an execution is pending
		//return (redoList.size()>0);
		return (redoList.size() > 0) && !this.waited;
		// END KGU#143 2016-01-21
	}

    /**
     * Removes all entries from the redo stack
     * @see #undo()
     * @see #redo()
     * @see #canRedo()
     */
    public void clearRedo()
    {
        redoList = new Stack<Subqueue>();
    }

    /**
     * Removes all entries from the undo stack
     * @see #undo()
     * @see #canUndo()
     * @see #addUndo()
     */
    public void clearUndo()
    {
        undoList = new Stack<Subqueue>();
        // START KGU#137 2016-01-11: Bugfix #103 - Most recently saved state is lost, too
        // FIXME: It might also be an initialisation (in which case = 0 would have been correct)
        this.undoLevelOfLastSave = -1;
        // END KGU#137 2016-01-11
    }

    /**
     * Takes the top entry from the undo stack, reverts the associated changes and adds
     * a respective entry to the redo stack.
     * @see #addUndo()
     * @see #canUndo()
     * @see #undo(boolean)
     */
    public void undo()
    {
    // START KGU#365 2017-03-19: Enh. #380 we need an undo without redo
        undo(true);
    }
    
    /**
     * Takes the top entry from the undo stack and reverts the associated changes. If argument {@code redoable}
     * is true then adds a corresponding entry to the redo stack otherwise the undone action won't be redoable
     * (This should only be set false in order to clean the undo stack of entries that are part of a larger
     * transaction also involving other diagrams and cannot consistently be redone therefore, e.g. on outsourcing
     * subroutines; normally {@link #undo()} is to be used instead.)
     * @see #undo()
     * @see #canUndo()
     * @see #addUndo()
     */
    public void undo(boolean redoable)
    {
    // END KGU#365 2017-03-19
        if (undoList.size()>0)
        {
            // START KGU#137 2016-01-11: Bugfix #103 - rely on undoList level comparison 
            //this.hasChanged=true;
            // END KGU#137 2016-01-11
            // START KGU#365 2017-03-19: Enh. #380
            if (redoable) {
            // END KGU#365 2017-03-19
                redoList.add((Subqueue)children.copy());
                // START KGU#120 2016-01-02: Bugfix #85 - park my StringList attributes in the stack top
                redoList.peek().setText(this.text.copy());
                redoList.peek().setComment(this.comment.copy());
                // END KGU#120 2016-01-02
                // START KGU#507 2018-03-15: Bugfix #523
                if (this.includeList != null) {
                    redoList.peek().diagramRefs = this.includeList.concatenate(",");
                }
                // END KGU#507 2018-03-15
                // START KGU#363 2018-09-12 
                redoList.peek().modified = this.modified;
                // END KGU#363 2018-09-12
            // START KGU#365 2017-03-19: Enh. #380
            }
            // END KGU#365 2017-03-19
            children = undoList.pop();
            children.parent = this;
            // START KGU#120 2016-01-02: Bugfix #85 - restore my StringList attributes from stack
            this.setText(children.getText().copy());
            this.setComment(children.getComment().copy());
            children.text.clear();
            children.comment.clear();
            // END KGU#120 2016-01-02
            // START KGU#363 2017-05-21: Enh. #372
            // If the undone action involves Root attributes then we must
            // cache the current attributes on the redo stack accordingly
            // and restore the attributes from the undo stack
            if (children.rootAttributes != null) {
                if (redoable) {
                    redoList.peek().rootAttributes = new RootAttributes(this);
                }
                this.adoptAttributes(children.rootAttributes);
                children.rootAttributes = null;
            }
            // END KGU#363 2017-05-21
            // START KGU#363 2018-09-12
            this.modified = children.modified;	// Restore the former modification date
            children.modified = null;
            // Special action if all changes have been undone.
            if (undoList.empty()) {
            	this.modifiedby = this.modifiedby0;
            }
            // END KGU#363 2018-09-12
            // START KGU#376 2017-07-01: Enh. #389
            if (children.diagramRefs != null) {
                this.includeList = StringList.explode(children.diagramRefs, ",");
                children.diagramRefs = null;
            }
            // END KGU#376 2017-07-01
            // START KGU507 2018-03-15: bugfix #523
            else {
                this.includeList = null;
            }
            // END KGU507 2018-03-15
            // START KGU#136/KGU#261/KGU#444/KGU#618/KGU#701 2019-03-30: Issues #97, #259, #417, #649, #718
            this.clearVarAndTypeInfo(true);
            // END KGU#136/KGU#261/KGU#444/KGU#618/KGU#701 2019-03-30
            // START KGU#703 2019-03-30: Issue #718
            if (this.isInclude() && Arranger.hasInstance()) {
                for (Root ref: Arranger.getInstance().findIncludingRoots(this.getMethodName(), true)) {
                    ref.clearVarAndTypeInfo(false);
                }
            }
            // END KGU#703 2019-03-30
        }
    }

	// START KGU#363 2017-05-21: Enh. #372
    public void adoptAttributes(RootAttributes attributes) {
    	if (attributes != null) {
    		this.author = attributes.authorName;
    		this.licenseName = attributes.licenseName;
    		this.licenseText = attributes.licenseText;
    		this.origin = attributes.origin;
    		// START KGU#408 2021-02-22: Enh. #410
    		this.namespace = attributes.namespace;
    		// END KGU#408 2021-02-22
    	}
    }
    // KGU#363 2017-05-21
    
    /**
     * Takes the top entry from the redo stack and restores the results before the associated undo action.
     * @see #undo()
     * @see #canRedo()
     * @see #clearRedo()
     */
    public void redo()
    {
        if (redoList.size()>0)
        {
            // START KGU#137 2016-01-11: Bugfix #103 - rely on undoList level comparison 
            //this.hasChanged=true;
            // END KGU#137 2016-01-11
            undoList.add((Subqueue)children.copy());
            // START KGU#120 2016-01-02: Bugfix #85 - park my StringList attributes on the stack top
            undoList.peek().setText(this.text.copy());
            undoList.peek().setComment(this.comment.copy());
            // END KGU#120 2016-01-02
            // START KGU#507 2018-03-15: Bugfix #523
            if (this.includeList != null) {
                undoList.peek().diagramRefs = this.includeList.concatenate(",");
            }
            // END KGU#507 2018-03-15
            // START KGU#363 2018-09-12: Enh. #372
            undoList.peek().modified = this.modified;	// Save the current modification date
            // END KGU#363 2018-09-12
            children = redoList.pop();
            children.parent = this;
            // START KGU#120 2016-01-02: Bugfix #85 - restore my StringList attributes from the stack
            this.setText(children.getText().copy());
            this.setComment(children.getComment().copy());
            children.text.clear();
            children.comment.clear();
            // END KGU#120 2016-01-02
            // START KGU#363 2017-05-21: Enh. #372
            if (children.rootAttributes != null) {
                undoList.peek().rootAttributes = new RootAttributes(this);
                this.adoptAttributes(children.rootAttributes);
                children.rootAttributes = null;
            }
            // END KGU#363 2017-05-21
            // START KGU#363 2018-09-12: Enh. #372
            this.modified = children.modified;
            children.modified = null;
            // END KGU#363 2018-09-12
            // START KGU#507 2018-03-15: Bugfix #523
            if (children.diagramRefs != null) {
                this.includeList = StringList.explode(children.diagramRefs, ",");
                children.diagramRefs = null;
            }
            else {
                this.includeList = null;
            }
            // END KGU#507 2018-03-15
            // START KGU#136/KGU#261/KGU#701 2019-03-30: Bugfix #97, enh. #259, #718
            this.clearVarAndTypeInfo(true);
            // END KGU#136/KGU#261 2019-03-20
            // START KGU#703 2019-03-30: Issue #720
            if (this.isInclude() && Arranger.hasInstance()) {
                for (Root ref: Arranger.getInstance().findIncludingRoots(this.getMethodName(), true)) {
                    ref.clearVarAndTypeInfo(false);
                }
            }
            // END KGU#703 2019-03-30
        }
    }

    // START KGU#137 2016-01-11: Bugfix #103 - Synchronize saving with undo / redo stacks
    /**
     * To be called after successful saving the diagram as NSD in order to record
     * the current undoStack size, such that we may know whether or not there are
     * unsaved changes.
     */
    public void rememberSaved()
    {
        this.undoLevelOfLastSave = this.undoList.size();
        // START KGU#137 2016-01-14: Bugfix #107
        this.hasChanged = false;
        // END KGU#137 2016-01-16
        // START KGU#330 2017-01-13: Enh. #305 Notify arranger index listeners
        // inform updaters
        for(int u = 0; u < updaters.size(); u++)
        {
            updaters.get(u).update(this);
        }
        // END KGU#330 2017-01-13
    }
    // END KGU#137 2016-01-11

    public boolean moveDown(Element _ele)
    {
        boolean res = false;
        if(_ele!=null)
        {
            // START KGU#144 2016-01-22: Bugfix #38 - multiple selection wasn't properly considered
            if (_ele instanceof SelectedSequence)
            {
                res = ((SelectedSequence)_ele).moveDown();
            }
            else
            {
            // END KGU#144 2016-01-22
                int i = ((Subqueue) _ele.parent).getIndexOf(_ele);
                if (!_ele.getClass().getSimpleName().equals("Subqueue") &&
                        !_ele.getClass().getSimpleName().equals("Root") &&
                        ((i+1)<((Subqueue) _ele.parent).getSize()))
                {
                    // START KGU#136 2016-03-02: Bugfix #97
                    //((Subqueue) _ele.parent).removeElement(i);
                    //((Subqueue) _ele.parent).insertElementAt(_ele, i+1);
                    ((Subqueue) _ele.parent).moveElement(i, i+1);
                    // END KGU#136 2016-03-02: Bugfix #97
                    // START KGU#137 2016-01-11: Bugfix #103 - rely on addUndo() 
                    //hasChanged=true;
                    // END KGU#137 2016-01-11
                    _ele.setSelected(true);
                    res=true;
                }
            // START KGU#144 2016-01-22: Bugfix #38 (continued)
            }
            // END KGU#144 2016-01-22
        }
        return res;
    }

    public boolean moveUp(Element _ele)
    {
        boolean res = false;
        if(_ele!=null)
        {
            // START KGU#144 2016-01-22: Bugfix #38 - multiple selection wasn't properly considered
            if (_ele instanceof SelectedSequence)
            {
                res = ((SelectedSequence)_ele).moveUp();
            }
            else
            {
                // END KGU#144 2016-01-22
                int i = ((Subqueue) _ele.parent).getIndexOf(_ele);
                if (!_ele.getClass().getSimpleName().equals("Subqueue") &&
                        !_ele.getClass().getSimpleName().equals("Root") &&
                        ((i-1>=0)))
                {
                    // START KGU#136 2016-03-02: Bugfix #97
                    //((Subqueue) _ele.parent).removeElement(i);
                    //((Subqueue) _ele.parent).insertElementAt(_ele, i-1);
                    ((Subqueue) _ele.parent).moveElement(i, i-1);
                    // END KGU#136 2016-03-02: Bugfix #97
                    // START KGU#137 2016-01-11: Bugfix 103 - rely on addUndo() 
                    //hasChanged=true;
                    // END KGU#137 2016-01-11
                    _ele.setSelected(true);
                    res=true;
                }
            // START KGU#144 2016-01-22: Bugfix #38 (continued)
            }
            // END KGU#144 2016-01-22
        }
        return res;
    }

    /**
     * Returns a File object representing the existing file this diagram is stored within
     * or proceeding from. In case this is an extracted file, it will represent the path
     * of the containing archive. If this is not associated to a file (e.g. never saved) or
     * the origin file cannot be located anymore then the result will be null.<br/>
     * (The result is equivalent to {@code new File(this.getPath(true))}, if this is
     * associated to a file at all.)
     * @return a File object representing the existing source or archive file or null
     * @see #getPath()
     * @see #getPath(boolean)
     */
    public File getFile()
    {
    	if (filename.equals(""))
    	{
    		return null;
    	}
    	else
    	{
    		// START KGU#316 2017-01-03: Issue #318 - we must not return a virtual path
    		//return new File(filename);
    		File myFile = new File(filename);
    		//File myFile = new File("N:\\doof\\dumm\\duemmer.nsd");
    		while (myFile != null && !myFile.exists()) {
    			myFile = myFile.getParentFile();
    		}
    		// START KGU#749 2019-10-13: Bugfix #763
    		if (myFile != null && myFile.isDirectory()) {
    			myFile = null;
    		}
    		// END KGU#749 2019-10-13
    		return myFile;
    		// END KGU#316 2017-01-03
    	}
    }

    /**
     * Returns the absolute file path as string if this diagram is associated to a file or an empty string
     * otherwise.<br/>
     * If the file resides within an arrz archive, the path will be a symbolic path into the arrz archive.
     * (This method is equivalent to {@code getPath(false)}.)
     * @return the absolute path of the nsd file (may be symbolic)
     * @see #getPath(boolean)
     * @see #getFile()
     */
    public String getPath()
    // START KGU#316 2016-12-28: Enh. #318 Consider unzipped file
    {
    	return getPath(false);
    }
    
    /**
     * Returns the absolute file path as string if this diagram is associated to a file,
     * or an empty string otherwise.<br/>
     * If the file resides within an arrz archive, the path may be a symbolic path into
     * the arrz archive unless {@code pathOfOrigin} is true, in which case it will be the
     * archive path itself instead.<br/>
     * {@code getPath(true)} is equivalent to {@code getFile().getAbsolutePath()} if this
     * is associated to a file at all.
     * @param pathOfOrigin - if true and the diagram resides within an arrz archive the
     * path of the mere archive will be returned.
     * @return the absolute path of the nsd file (may be symbolic) or of the housing arrz file.
     * @see #getFile()
     */
    public String getPath(boolean pathOfOrigin)
    // END KGU#316 2016-12-28
    {
    	if (filename.equals(""))
    	{
    		return filename;
    	}
    	else
    	{
    		File f = new File(filename);
    		// START KGU#316 2016-12-28: Enh. #318 Consider unzipped file
    		if (pathOfOrigin && this.shadowFilepath != null) {
    			while (f != null && !f.isFile()) {
    				f = f.getParentFile();
    			}
    			// No Zip file found?
    			if (f == null) {
    				f = new File(filename);
    			}
    		}
    		// END KGU#316 2016-12-28
    		return f.getAbsolutePath();
    	}
    }
    
    /*************************************
     * Extract full text of all Elements
     *************************************/

    // START KGU 2015-10-16
    /* (non-Javadoc)
     * @see lu.fisch.structorizer.elements.Element#addFullText(lu.fisch.utils.StringList, boolean)
     */
    @Override
    protected void addFullText(StringList _lines, boolean _instructionsOnly)
    {
    	// Whereas a subroutine diagram is likely to hold parameter declarations in the header,
    	// (such that we ought to deliver its header for the variable detection), this doesn't
    	// hold for programs and includable diagrams.
    	// START KGU#376 2017-07-02: Enh. #389 - beware of cyclic recursion
    	//if (!this.isProgram && !_instructionsOnly)
    	//{
    	//	_lines.add(this.getText());
    	//}
    	//this.children.addFullText(_lines, _instructionsOnly);
    	HashSet<Root> implicatedRoots = new HashSet<Root>();
    	this.addFullText(_lines, _instructionsOnly, implicatedRoots);
    	// END KGU#376 2017-07-02
    }
    // END KGU 2015-10-16
    
    // START KGU#376 2017-07-02: Enh. #389
    protected void addFullText(StringList _lines, boolean _instructionsOnly, HashSet<Root> _implicatedRoots)
    {
    	if (!_implicatedRoots.contains(this)) {
    		// START KGU#676 2019-03-31: Enh. #696
    		//if (this.includeList != null && Arranger.hasInstance()) {
    		IRoutinePool pool = specialRoutinePool;
    		if (this.includeList != null && (pool != null || Arranger.hasInstance())) {
    			if (pool == null) {
    				pool = Arranger.getInstance();
    			}
    		// END KGU#676 2019-03-31
    			_implicatedRoots.add(this);
    			for (int i = 0; i < this.includeList.count(); i++) {
    				String name = this.includeList.get(i);
    				// START KGU#676 2019-03-31: Enh. #696
    				//Vector<Root> roots = Arranger.getInstance().findIncludesByName(name, this);
    				Vector<Root> roots = pool.findIncludesByName(name, this, false);
    				// END KGU#676 2019-03-31
    				if (roots.size() == 1) {
    					roots.get(0).addFullText(_lines, _instructionsOnly, _implicatedRoots);
    				}
    			}		
    		}
    		if (this.isSubroutine() && !_instructionsOnly)
    		{
    			_lines.add(this.getText());
    		}
    		this.children.addFullText(_lines, _instructionsOnly);
    	}
    }
    // END KGU#376 2017-07-02

    /**
     * Extracts the variable name out of a more complex string possibly also
     * containing index brackets, component access operator or type specifications
     * @param _s the raw lvalue string
     * @return the pure variable name
     */
    private String extractVarName(String _s)
    {
        //System.out.println("IN : "+_s);
        // START KGU#141 2016-01-16: Bugfix #112
//            if(_s.indexOf("[")>=0)
//            {
//                    _s=_s.substring(0,_s.indexOf("["));
//            }
        while (_s.startsWith("(") && _s.endsWith(")"))
        {
        	_s = _s.substring(1,  _s.length()-1).trim();
        }
        // START KGU#575 2018-09-17: Issue #594 - Get rid of an obsolete 3rd-party Regex library
        // START KGU 2016-03-29: Bugfix - nested index expressions were defectively split (a bracket remained)
        //Regex r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$1 $3");
        //Regex r = new Regex("(.*?)[\\[](.*)[\\]](.*?)","$1 $3");
        // END KGU 2016-03-29
        //_s = r.replaceAll(_s);
        _s = INDEX_PATTERN_GREEDY.matcher(_s).replaceAll("$1 $3");
        // END KGU#575 2018-09-17
        // START KGU#141 2016-01-16: Bugfix #112 Cut off component and method names
        if (_s.indexOf(".") >= 0)
        {
        	_s = _s.substring(0, _s.indexOf("."));
        }
        // START KGU#109/KGU#141 2016-01-16: Bugfix #61/#107/#112
        // In case of Pascal-typed variables we should only use the part before the separator
        int colonPos = _s.indexOf(':');	// Check Pascal and BASIC style as well
        if (colonPos > 0 || (colonPos = _s.indexOf(" as ")) > 0)
        {
        	_s = _s.substring(0, colonPos).trim();
        }
        // In case of C-typed variables we should only use the last word (identifier)
        String[] tokens = _s.split(" ");
        if (tokens.length > 0) _s = tokens[tokens.length-1];
        // END KGU#109/KGU#141 2016-01-16
        //System.out.println("OUT : "+_s);

        return _s;

    }

    // KGU 2016-03-29: Completely rewritten
    /**
     * Gathers the names of all variables that are used by Element _ele in expressions:<br/>
     * HYP 1: (?) &lt;- (?) &lt;used&gt; (?)<br/>
     * HYP 2: (?)'['&lt;used&gt;']' &lt;- (?) &lt;used&gt; (?)<br/>
     * HYP 3: output (?) &lt;used&gt; (?)<br/>
     * Note: This works only if _ele is different from this.<br/>
     * @param _ele - the element to be searched
     * @param _includeSelf - whether or not the own text of _ele is to be considered (otherwise only substructure)
     * @param _onlyEle - if true then only the text of _ele itself is searched (no substructure)
     * @return StringList of variable names according to the above specification
     */
    public StringList getUsedVarNames(Element _ele, boolean _includeSelf, boolean _onlyEle)
    {
    	//if (_ele instanceof Repeat) System.out.println("getUsedVarNames(" + _ele + ", " + _includeSelf + ", " + _onlyEle + ")");
    	StringList varNames = new StringList();

    	if (_ele != this)
    	{
    		// get body text
    		StringList lines = new StringList();
    		if (_onlyEle)
    		{
    			// START KGU#413 2017-09-13: Enh. #416 cope with user-defined line breaks 
    			//lines.add(_ele.getText());
    			StringList unbrokenLines = _ele.getUnbrokenText();
    			if (_ele instanceof Instruction) {
    				for (int i = 0; i < unbrokenLines.count(); i++) {
    					String line = unbrokenLines.get(i);
    					if (!Instruction.isTypeDefinition(line, null)) {
    						lines.add(line);
    					}
    				}
    			}
    			// START KGU#686 2019-03-16: Enh. #56 the text of the catch clause is kind of declaration, not use
    			//else {
    			else if (!(_ele instanceof Try)) {
    			// END KGU#686 2019-03-16
    				lines.add(unbrokenLines);
    			}
    			// END KGU#413 2017-09-13
    			// START KGU#163 2016-03-25: In case of Case the default line must be removed
    			if (_ele instanceof Case && lines.count() > 1)
    			{
    				lines.delete(lines.count()-1);;
    			}
    			// END KGU#163 2016-03-25
    		}
    		else
    		{
    			// START KGU#39 2015-10-16: What exactly is expected here?
    			//lines = getFullText(_ele);
    			lines = _ele.getFullText(false);
    			// END KGU#39 2015-10-16
    			if (!_includeSelf)
    			{
    				for(int i=0; i<_ele.getUnbrokenText().count(); i++)
    				{
    					lines.delete(0);
    				}
    			}
    			// START KGU#163 2016-03-25: The Case default line must be deleted
    			else if (_ele instanceof Case && _ele.getText().count() > 1)
    			{
    				// Remove the last line of the CASE element
    				lines.delete(_ele.getText().count() - 1);
    			}
    			// END KGU#163 2016-03-25
    		}
    		//System.out.println(lines);

    		String[] keywords = CodeParser.getAllProperties();
    		StringList parts = new StringList();
    		
    		for(int i=0; i<lines.count(); i++)
    		{
    			// START KGU#375 2017-04-04: Enh. #388 method decomposed
    			parts.addIfNew(getUsedVarNames(lines.get(i).trim(), keywords));
    			// END KGU#375 2017-04-04
    		}
    		
    		// START KGU#375 2017-04-04: Enh. #388 - now already done by getUsedVarName(String, String[])
    		varNames.addIfNew(parts);
    		// END KGU#375 2017-04-04

    	}

    	varNames = varNames.reverse();	// Why is it reversed?
    	//varNames.saveToFile("D:\\SW-Produkte\\Structorizer\\tests\\Variables_" + Root.fileCounter++ + ".txt");
    	return varNames;
    }

	// START KGU#375 2017-04-04: Enh. #388 getUsedVarNames decomposed on occasion of analyse_22_24
	/**
	 * Gathers the names of all variables that are used in text line _line in expressions:<br/>
	 * HYP 1: (?) &lt;- (?) &lt;used&gt; (?)<br/>
	 * HYP 2: (?)'['&lt;used&gt;']' &lt;- (?) &lt;used&gt; (?)<br/>
	 * HYP 3: output (?) &lt;used&gt; (?)<br/>
	 * HYP 4: input (?)'['&lt;used&gt;']'
	 * @param _line - the element text line to be analysed
	 * @param _keywords the set of parser keywords (if available)
	 * @return StringList of used variable names according to the above specification
	 */
	private StringList getUsedVarNames(String _line, String[] _keywords)
	{
		if (_keywords == null) {
			_keywords = CodeParser.getAllProperties();
		}
//		Regex r;

		// modify "inc" and "dec" function (Pascal)
//		r = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 + $2"); _line = r.replaceAll(_line);
//		r = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)","$1 <- $1 + 1"); _line = r.replaceAll(_line);
//		r = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 - $2"); _line = r.replaceAll(_line);
//		r = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)","$1 <- $1 - 1"); _line = r.replaceAll(_line);
		// START KGU#575 2018-09-17: Issue #594 - we may simply use the equivalent matchers inherited from Element
		//_line = INC_PATTERN2.matcher(_line).replaceAll("$1 <- $1 + $2");
		//_line = INC_PATTERN1.matcher(_line).replaceAll("$1 <- $1 + 1");
		//_line = DEC_PATTERN2.matcher(_line).replaceAll("$1 <- $1 - $2");
		//_line = DEC_PATTERN1.matcher(_line).replaceAll("$1 <- $1 - 1");
		_line = transform_inc_dec(_line);
		// END KGU#575 2018-09-17

		StringList tokens = Element.splitLexically(_line.trim(), true);

		Element.unifyOperators(tokens, false);

		// Replace all split keywords by the respective configured strings
		// This replacement will be aware of the case sensitivity preference
		for (int kw = 0; kw < _keywords.length; kw++)
		{
			if (_keywords[kw].trim().length() > 0)
			{
				StringList keyTokens = splitKeywords.elementAt(kw);
				int keyLength = keyTokens.count();
				int pos = -1;
				while ((pos = tokens.indexOf(keyTokens, pos + 1, !CodeParser.ignoreCase)) >= 0)
				{
					tokens.set(pos, _keywords[kw]);
					for (int j=1; j < keyLength; j++)
					{
						tokens.delete(pos+1);
					}
				}
			}
		}

		// Unify FOR-IN loops and FOR loops for the purpose of variable analysis
		if (!CodeParser.getKeyword("postForIn").trim().isEmpty())
		{
			tokens.replaceAll(CodeParser.getKeyword("postForIn"), "<-");
		}
		
		// Here all the unification, alignment, reduction is done, now the actual analysis begins

		String token0 = "";
		if (tokens.count() > 0) token0 = tokens.get(0);
		int asgnPos = tokens.indexOf("<-");
		if (asgnPos >= 0)
		{
			// Look for indexed variable as assignment target, get the indices in this case
			String s = tokens.subSequence(0, asgnPos).concatenate();
			// START KGU#924 2021-02-03: Bugfix #924 component ids sometimes blamed
			//if (s.indexOf("[") >= 0)
			int posLBrack = s.indexOf("[");
			if (posLBrack >= 0 && s.indexOf("]", posLBrack+1) >= 0)
			// END KGU#924 2021-02-03
			{
				// START KGU#924 2021-02-03: Bugfix #924 We must cut off the tail
				s = s.substring(posLBrack, s.lastIndexOf("]")+1);
				// END KGU#924 2021-02-03
				//r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$2");
				//s = r.replaceAll(s);
				s = INDEX_PATTERN.matcher(s).replaceAll("$2");
			} else { s = ""; }

			StringList indices = Element.splitLexically(s, true);
			indices.add(tokens.subSequence(asgnPos+1, tokens.count()));
			tokens = indices;
		}
		// START KGU#332/KGU#375 2017-01-17: Enh. #335 - ignore the content of uninitialized declarations
		else if (token0.equalsIgnoreCase("var") || token0.equalsIgnoreCase("dim") || token0.equalsIgnoreCase("const")) {
			// START KGU#358 2017-03-06: Issue #368 - declarations are no longer variable usages
			// (though an uninitialized const is a syntax error)
			tokens.clear();
			// END KGU#358 2017-03-06
		}
		// END KGU#332/KGU#375 2017-01-17

		// cutoff output keyword
		else if (token0.equals(CodeParser.getKeyword("output")))	// Must be at the line's very beginning
		{
			tokens.delete(0);
		}

		// parse out array index
		else if (token0.equals(CodeParser.getKeyword("input")))
		{
			// START KGU#653 2019-02-16: Enh. #680 - with multiple variables we must decompose the list
			StringList items = Instruction.getInputItems(_line);
			tokens.clear();
			for (int j = 1; j < items.count(); j++) {
				StringList itemTokens = Element.splitLexically(items.get(j), true);
				String s = "";
				// START KGU#924 2021-02-03: Bugfix #924 component ids sometimes blamed
				//if (itemTokens.indexOf("[", 1) >= 0)
				int posLBrack = itemTokens.indexOf("[", 1);
				if (posLBrack >= 0 && itemTokens.indexOf("]", posLBrack+1) >= 0)
				// END KGU#924 2021-02-03
				{
					//System.out.print("Reducing \"" + s);
					//r = new Regex("(.*?)[\\[](.*?)[\\]](.*?)","$2");
					//s = r.replaceAll(s);
					// START KGU#924 2021-02-03: Bugfix #924 We must cut off the tail
					//s = INDEX_PATTERN.matcher(itemTokens.subSequence(1, itemTokens.count()).concatenate()).replaceAll("$2");
					s = INDEX_PATTERN.matcher(
							itemTokens.concatenate(null, posLBrack, itemTokens.lastIndexOf("]")+1))
							.replaceAll("$2");
					// END KGU#924 2021-02-03
					//System.out.println("\" to \"" + s + "\"");
				}
				// Only the indices are relevant here
				itemTokens = Element.splitLexically(s, true);
				tokens.addIfNew(itemTokens);
			}
			// END KGU#653 2019-02-16
		}

		tokens.removeAll(" ");
		// Eliminate all keywords
		for (int kw = 0; kw < _keywords.length; kw++)
		{
			tokens.removeAll(_keywords[kw]);
		}
		for (int kw = 0; kw < this.operatorsAndLiterals.length; kw++)
		{
			int pos = -1;
			while ((pos = tokens.indexOf(operatorsAndLiterals[kw], false)) >= 0)
			{
				tokens.delete(pos);
			}
		}
		// START KGU#388 2017-09-17: Enh. #423 Cut off all irrelevant stuff of record initializers
		skimRecordInitializers(tokens);
		// END KGU#388 2017-09-17
		int i = 0;
		while(i < tokens.count())
		{
			String token = tokens.get(i);
			// START KGU#588 2018-10-04: Bugfix #618 Function names shouldn't be gathered here
			//if((Function.testIdentifier(token, null)
			//		&& (i == tokens.count() - 1 || !tokens.get(i+1).equals("("))
			//		|| this.variables.contains(token)))
			if((Function.testIdentifier(token, false, null) || this.getCachedVarNames().contains(token))
					&& (i == tokens.count() - 1 || !tokens.get(i+1).equals("(")))
			// END KGU#588 2018-10-04
			{
				// keep the id
				//System.out.println("Adding to used var names: " + token);
				i++;
			}
			// START KGU#388 2017-09-17: Enh. #423 Record support - don't complain component names!
			else if (token.equals(".") && i+1 < tokens.count() && Function.testIdentifier(tokens.get(i+1), false, null)) {
				// Drop the dot together with the following component name
				tokens.remove(i, i+2);
			}
			// END KGU#388 2017-09-17
			else {
				// no id or variable name, so drop it
				tokens.remove(i);
			}
		}
		return tokens;
	}
	// END KGU#375 2017-04-04
	// START KGU#388 2017-10-09: Enh. #423
	/**
	 * Recursively cuts off all irrelevant stuff of record initializers for {@link #getUsedVarNames(String, String[])}
	 * @param tokens - the skimmed tokens
	 */
	private void skimRecordInitializers(StringList tokens) {
		int posBrace = 0;
		while ((posBrace = tokens.indexOf("{", posBrace+1)) > 0) {
			if (Function.testIdentifier(tokens.get(posBrace-1), false, null)) {
				HashMap<String, String> components = Element.splitRecordInitializer(tokens.concatenate("", posBrace-1), null, false);
				if (components != null) {
					// Remove all tokens from the type name on (they are in the HashMap now)
					tokens.remove(posBrace-1, tokens.count());
					// Append all the value strings for the components but not the component names
					for (Entry<String, String> comp: components.entrySet()) {
						if (!comp.getKey().startsWith("§")) {
							StringList subTokens = Element.splitLexically(comp.getValue(), true);
							skimRecordInitializers(subTokens);
							tokens.add(subTokens);
						}
					}
					// If there was further text beyond the initializer then tokenize and append it
					if (components.containsKey("§TAIL§")) {
						StringList subTokens = Element.splitLexically(components.get("§TAIL§"), true);
						skimRecordInitializers(subTokens);
						tokens.add(subTokens);
					}
				}
			}
		}
	}
	// END KGU#388 2017-10-09
	
	// START KGU#542 2019-11-17: Enh. #739 Support for enumeration type values
	/**
	 * Returns the value string (cached literal or expression) associated to constant
	 * {@code constName} (if it was defined) or null (otherwise). In case of enumeration
	 * constants, wipes off the typename prefix ({@code ":" + <typename> + "€"}).
	 * @param constName - name of the constant
	 * @return value string for the constant (cleaned in case of an enumerator) or null
	 * @see #constants
	 */
	public String getConstValueString(String constName)
	{
		String valString = this.constants.get(constName);
		if (valString != null && valString.startsWith(":") && valString.contains("€")) {
			// Skim off the enumerator type name
			valString = valString.substring(valString.indexOf('€')+1);
		}
		return valString;
	}
	// END KGU#542 2019-11-17

    // KGU 2016-03-29 Rewritten based on tokens
    /**
     * Get the names of defined variables out of a bunch of text lines.<br/>
     * Note: We DON'T force identifier syntax, the variables found here may contain
     * language-specific characters etc.<br/>
     * HYP 1: [const] [&lt;type&gt;] VARNAME &lt;- (?)<br/>
     * HYP 2: [input] VARNAME, VARNAME, VARNAME<br/>
     * HYP 3: for VARNAME &lt;- (?) ...<br/>
     * HYP 4: foreach VARNAME in (?)<br/>
     * In case of HYP 1 also registers the name as constant definition in constantDefs
     * if the name hadn't been occurred earlier as variable or constant.
     * 
     * @param lines - the text lines extracted from one or more elements 
     * @param constantDefs - a map of constant definitions
     * @return - the StringList of identified variable names
     * @see #constants
     */
    public StringList getVarNames(StringList lines, HashMap<String, String> constantDefs)
    {
    	StringList varNames = new StringList();

    	// START KGU#163 2016-03-25: Pre-processed match patterns for identifier search
    	splitKeywords.clear();
    	String[] keywords = CodeParser.getAllProperties();
    	for (int k = 0; k < keywords.length; k++)
    	{
    		splitKeywords.add(Element.splitLexically(keywords[k], false));
    	}
    	// END KGU#163 2016-03-25

    	for(int i=0; i<lines.count(); i++)
    	{
    		String allText = lines.get(i);
    		// modify "inc" and "dec" function (Pascal)
    		// START KGU#575 2018-09-17: Issue #594 - replace obsolete 3rd-party Regex library
    		//Regex r;
    		//r = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 + $2"); allText=r.replaceAll(allText);
    		//r = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)","$1 <- $1 + 1"); allText=r.replaceAll(allText);
    		//r = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)","$1 <- $1 - $2"); allText=r.replaceAll(allText);
    		//r = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)","$1 <- $1 - 1"); allText=r.replaceAll(allText);
    		allText = transform_inc_dec(allText);
    		// END KGU#575 2018-09-17

    		StringList tokens = Element.splitLexically(allText, true);

    		Element.unifyOperators(tokens, false);

    		// Replace all split keywords by the respective configured strings
    		// This replacement will be aware of the case sensitivity preference
    		for (int kw = 0; kw < keywords.length; kw++)
    		{
    			if (keywords[kw].trim().length() > 0)
    			{
    				StringList keyTokens = splitKeywords.elementAt(kw);
    				int keyLength = keyTokens.count();
    				int pos = -1;
    				while ((pos = tokens.indexOf(keyTokens, pos + 1, !CodeParser.ignoreCase)) >= 0)
    				{
    					tokens.set(pos, keywords[kw]);
    					tokens.remove(pos + 1, pos + keyLength);
    				}
    			}
    		}

    		// Unify FOR-IN loops and FOR loops for the purpose of variable analysis
    		if (!CodeParser.getKeyword("postForIn").trim().isEmpty())
    		{
    			tokens.replaceAll(CodeParser.getKeyword("postForIn"), "<-");
    		}

    		// Here all the unification, alignment, reduction is done, now the actual analysis begins

    		int asgnPos = tokens.indexOf("<-");
    		if (asgnPos > 0)
    		{
    			String s = tokens.subSequence(0, asgnPos).concatenate();
    			// (KGU#141 2016-01-16: type elimination moved to extractVarName())
    			//System.out.println("Adding to initialised var names: " + extractVarName(allText.trim()));
    			String varName = extractVarName(s.trim());
    			boolean wasNew = varNames.addOrderedIfNew(varName);
    			// START KGU#375 2017-03-31: Enh. #388 collect constant definitions
    			// Register it as constant if marked as such and not having been declared before
    			if (tokens.get(0).equals("const") && wasNew && !constantDefs.containsKey(varName)) {
    				constantDefs.put(varName, tokens.subSequence(asgnPos+1, tokens.count()).concatenate().trim());
    			}
    		}


    		// get names from read statements
    		int inpPos = tokens.indexOf(CodeParser.getKeyword("input"));
    		if (inpPos >= 0)
    		{
    			// START KGU#281 2016-10-12: Issue #271 - there may be a prompt string literal to be skipped
    			//String s = tokens.subSequence(inpPos + 1, tokens.count()).concatenate().trim();
    			inpPos++;
    			// START KGU#281 2016-12-23: Enh. #271 - allow comma between prompt and variable name
    			//while (inpPos < tokens.count() && (tokens.get(inpPos).trim().isEmpty() || tokens.get(inpPos).matches("^[\"\'].*[\"\']$")))
    			while (inpPos < tokens.count() && (tokens.get(inpPos).trim().isEmpty() || tokens.get(inpPos).trim().equals(",") || tokens.get(inpPos).matches("^[\"\'].*[\"\']$")))
    			// END KGU#281 2016-12-23
    			{
    				inpPos++;
    			}
    			//String s = tokens.subSequence(inpPos, tokens.count()).concatenate().trim();
    			// END KGU#281 2016-10-12
    			// A mere splitting by comma would spoil function calls as indices etc.
    			StringList parts = Element.splitExpressionList(tokens.subSequence(inpPos, tokens.count()), ",", false);
    			for (int p = 0; p < parts.count(); p++)
    			{
    				varNames.addOrderedIfNew(extractVarName(parts.get(p).trim()));
    			}
    		}


    		//lines.set(i, allText);
    	}

    	return varNames;
    }

    /**
     * Extracts all variable names of the entire program and stores them in
     * {@link #variables}, ignoring previously cached results.
     * @return list of variable names
     * @see #getVarNames()
     */
    public StringList retrieveVarNames()
    {
    	//System.out.println("getVarNames() called...");
    	return getVarNames(this, false, false, true);
    }

    /**
     * Provides all variable names of the entire program if cached, otherwise retrieves and
     * stores them in {@link #variables}.
     * @return list of variable names
     * @see #retrieveVarNames()
     * @see #getMereDeclarationNames(boolean)
     */
    public StringList getVarNames() {
    	//System.out.println("getVarNames() called...");
    	if (this.variables != null) {
    		return this.variables;
    	}
    	// This is the same as retrieveVarNames()
    	return getVarNames(this, false, false, true);
    }
    
    /**
     * Extracts the names of all variables assigned or introduced within passed-in element
     * {@code _ele} and its possible substructure.
     * @param _ele - the element to be scanned for introduced variables
     * @return list of variable names
     * @see #getVarNames(Element, boolean)
     */
    public StringList getVarNames(Element _ele)
    {
    	// Only the own variables, not recursively
    	return getVarNames(_ele, true, false);
    }

    /**
     * Extracts the names of all variables assigned or introduced within passed-in element
     * {@code _ele}, possibly ignoring its substructure or not.
     * @param _ele - the element to be scanned for introduced variables
     * @param _onlyEle - whether possible substructure is to be ignored
     * @return list of variable names
     * @see #getVarNames(Element, boolean, boolean)
     */
    public StringList getVarNames(Element _ele, boolean _onlyEle)
    {
        // All variables, not only those from body (i.e. sub-structure)
        return getVarNames(_ele, _onlyEle, false);
    }

    /**
     * Extracts the names of all variables assigned or introduced within passed-in element
     * {@code _ele}, possibly either ignoring its substructure or only regarding its
     * substructure, e.g. in case of loops.<br/>
     * <b>Note that {@code _onlyEle} and {@code _onlyBody} may not both be {@code true}!</b>
     * @param _ele - the element to be scanned for introduced variables
     * @param _onlyEle - whether possible substructure of {@code _ele} is to be ignored
     * @param _onlyBody - whether only the substructure of {@code _ele} is to be scanned
     * @return list of variable names
     * @see #getVarNames(Element, boolean, boolean, boolean)
     */
    public StringList getVarNames(Element _ele, boolean _onlyEle, boolean _onlyBody)
    {
        return getVarNames(_ele, _onlyEle, _onlyBody, false);
    }

    /**
     * Extracts the names of all variables assigned or introduced within passed-in element
     * {@code _ele}, possibly either ignoring its substructure or only regarding its
     * substructure, e.g. in case of loops.<br/>
     * <b>Note that {@code _onlyEle} and {@code _onlyBody} may not both be {@code true}!</b>
     * @param _ele - the element to be scanned for introduced variables
     * @param _onlyEle - whether possible substructure of {@code _ele} is to be ignored
     * @param _onlyBody - whether only the substructure of {@code _ele} is to be scanned
     * @param _entireProg - relevant for the case {@code _ele} is {@code this} and
     *     {@code _onlyEle <> true} or {@code _onlyBody = true}
     * @return list of variable names
     */
    private StringList getVarNames(Element _ele, boolean _onlyEle, boolean _onlyBody, boolean _entireProg)
    {

            StringList varNames = new StringList();
            StringList argTypes = new StringList();

            // check root text for variable names
            // !!
            // !! This works only for Pascal-like syntax: functionname (<name>, <name>, ..., <name>:<type>; ...)
            // !! or VBA like syntax: functionname(<name>, <name> as <type>; ...)
            // !!
            // KGU 2015-11-29: Decomposed -> new method collectParameters
            if (this.isSubroutine() && _ele==this && !_onlyBody)
            {
            	collectParameters(varNames, argTypes, null);
            	for (int i = 0; i < varNames.count(); i++) {
            		String type = argTypes.get(i); 
            		if (type != null && (type.trim() + " ").startsWith("const ")) {
            			this.constants.put(varNames.get(i), null);
            		}
            	}
            }

            // get relevant text
            StringList lines;
            if (_onlyEle && !_onlyBody)
            {
                // START KGU#388/KGU#413 2017-09-13: Enh. #416, #423
                //lines = _ele.getText().copy();
                lines = _ele.getUnbrokenText();
                if (_ele instanceof Instruction) {
                	// Get rid of lines that may not introduce variables
                	int i = 0;
                	while (i < lines.count()) {
                		if (Instruction.isTypeDefinition(lines.get(i), null)) {
                			// START KGU#542 2019-11-17: Enh. #739 We must extract enumerators here
                			HashMap<String, String> constVals = this.extractEnumerationConstants(lines.get(i));
                			if (constVals != null) {
                				// We simply generate singular constant definition lines
                				for (Entry<String, String> enumItem: constVals.entrySet()) {
                					lines.insert("const " + enumItem.getKey() + " <- " + enumItem.getValue(), i++);
                				}
                			}
                			// END KGU#542 2019-11-17
                			lines.remove(i);
                		}
                		else {
                			i++;
                		}
                	}
                }
                // END KGU#388/KGU#413 2017-09-13: Enh. #416, #423
            }
            else if (_entireProg)
            {
                // START KGU#39 2015-10-16: Use object methods now
                //lines = getFullText();
                lines = this.getFullText(_onlyBody);
                // END KGU#39 2015-10-16
            }
            else
            {
                // START KGU#39 2015-10-16: Use object methods now
                //lines = getFullText(_ele);
                lines = _ele.getFullText(true);
                // START KGU#39 2015-10-16
            }
            
            varNames.add(getVarNames(lines, this.constants));

            varNames = varNames.reverse();	// FIXME (KGU): What is intended by reversing?
            if (_entireProg) {
                    this.variables = varNames;
            }
            //System.out.println(varNames.getCommaText());
            return varNames;
    }
    
    /**
     * Retrieves all constants from the given type definition list if it is an
     * enumeration type list and returns their name-value map.<br/>
     * The associated values will be prefixed with {@code ":" + <typename> + "€"} and may be
     * constant expressions, particularly of kind {@code <something>+<number>}.
     * @param _typeDefLine - the (unbroken) line of the enum type definition
     * @returns a map of the enumeration constants to their prefixed values strings or null
     */
    public LinkedHashMap<String,String> extractEnumerationConstants(String _typeDefLine)
    {
    	LinkedHashMap<String,String> enumConstants = null;
    	StringList tokens = Element.splitLexically(_typeDefLine, true);
    	tokens.removeAll(" ");
    	if (!tokens.get(3).equalsIgnoreCase("enum")) {
    		return null;
    	}
    	String typename = tokens.get(1);
    	String typeSpec = tokens.concatenate(null, 3, tokens.count()).trim();
    	// Confirm that the syntax is okay
    	if (TypeMapEntry.MATCHER_ENUM.reset(typeSpec).matches()) {
    		enumConstants = new LinkedHashMap<String, String>();
    		int val = 0;
    		String valStr = "";
    		int posBrace = typeSpec.indexOf('{');
    		StringList items = StringList.explode(typeSpec.substring(posBrace+1, typeSpec.length()-1), ",");
    		for (int i = 0; i < items.count(); i++, val++) {
    			String item = items.get(i);
    			int posEq = item.indexOf('=');
    			if (posEq >= 0) {
    				// Get the value string
    				valStr = item.substring(posEq+1).trim();
    				val = 0;
    				// Reduce item to the pure constant name
    				item = item.substring(0, posEq);
    				// FIXME: We should be able to evaluate constant expressions, e.g. "MONDAY+1"!
    				if (this.constants.containsKey(valStr)) {
    					// Is an already defined constant, get the associated value string
    					valStr = this.getConstValueString(valStr);
    				}
    				else if (valStr.contains("+")) {
    					int posPlus = valStr.lastIndexOf('+');
    					try {
    						int offset = Integer.parseInt(valStr.substring(posPlus+1));
    						valStr = valStr.substring(0, posPlus).trim();
    						val = offset;
    					}
    					catch (NumberFormatException ex) {}
    				}
    				try {
    					int val0 = Integer.parseInt(valStr);
    					// Don't accept negative values
    					if (val0 >= 0) {
    						val = val0;
    						valStr = "";
    					}
    				}
    				catch (NumberFormatException ex) {
    					// Just ignore the explicit value and use the standard
    				}
    			}
    			enumConstants.put(item, ":" + typename + "€" + valStr + (!valStr.isEmpty() ? "+" : "") + val);
    		}
    	}
    	return enumConstants;
    }

    // START KGU#672 2019-11-13: Introduced for Bugfix #776
    /**
     * @param allowMethodName if true then the method name will be included if among the keys in {@link #getTypeInfo()}, with false it will be suppressed
     * @return A list of the names of uninitialized, i.e. merely declared, variables
     * (of this diagram and all included diagrams).
     * @see #getVarNames()
     * @see #getTypeInfo()
     */
    public StringList getMereDeclarationNames(boolean allowMethodName)
    {
    	StringList declNames = new StringList();	// Result
    	StringList varNames = this.getVarNames();	// Names of initialized variables
    	String methName = this.getMethodName();
    	for (String name: this.getTypeInfo().keySet()) {
    		// Ignore type names and omit initialized variables (which should also include constants)
    		if (!name.startsWith(":") && !varNames.contains(name) && (allowMethodName || !methName.equals(name))) {
    			declNames.add(name);
    		}
    	}
    	return declNames;
    }
    // END KGU#672 2019-11-13
    
    // START KGU#261 2017-01-20: Enh. #259
    /**
     * Creates (if not already cached), caches, and returns the static overall type map
     * for this diagram and its included definition providers (if having been available
     * on the first creation).<br/>
     * Every change to this diagram clears the cache and hence leads to an info refresh.  
     * @return the type table mapping prefixed type names and variable names to their
     * respective defined or declared TypeMapEntries with structural information.
     */
    public HashMap<String, TypeMapEntry> getTypeInfo()
    // START KGU#678 2019-03-30: Enh. #696: For batch export mode, we must provide an alternative pool for includes
    {
        return getTypeInfo(null);
    }
    
    /**
     * Creates (if not already cached), caches, and returns the static overall type map
     * for this diagram and its included definition providers retrieved from {@code routinePool}
     * if given, otherwise from {@link Arranger} if available.<br/>
     * Every change to this diagram clears the cache and hence leads to an info refresh.
     * @param routinePool - a diagram pool to search for includables (default: {@link Arranger} instance)
     * @return the type table mapping prefixed type names and variable names to their
     * respective defined or declared TypeMapEntries with structural information.
     */
    public HashMap<String, TypeMapEntry> getTypeInfo(IRoutinePool routinePool)    
    // END KGU#678 2019-03-30
    {
    	// START KGU#502 2018-03-12: Bugfix #518 - Avoid repeated traversal in case of lacking type and var info
    	//if (this.typeMap.isEmpty()) {
    	if (this.typeMap == null) {
    		// START KGU#852 2020-04-22: Bugfix #854 - we must ensure topological order on export
    		//this.typeMap = new HashMap<String, TypeMapEntry>();
    		this.typeMap = new LinkedHashMap<String, TypeMapEntry>();
    		// END KGU#852 2020-04-22
    	// END KGU#502 2018-03-12
    		// START KGU#388 2017-09-18: Enh. #423 adopt all type info from included diagrams first
    		// FIXME: The import info can easily get obsolete unnoticedly!
    		if (this.includeList != null) {
    			for (int i = 0; i < this.includeList.count(); i++) {
    				String inclName = this.includeList.get(i);
    				// START KGU#66 2019-03-30: Enh.#696 consider alternative routine pool
    				//if (Arranger.hasInstance()) {
    				//	for (Root incl: Arranger.getInstance().findIncludesByName(inclName, this)) {
    				//		typeMap.putAll(incl.getTypeInfo());
    				//	}
    				//}
    				IRoutinePool pool = routinePool;
    				if (pool == null && (pool = specialRoutinePool) == null && Arranger.hasInstance()) {
    					pool = Arranger.getInstance();
    				}
    				if (pool != null) {
    					// START KGU#408 2021-02-24: Enh. #410 Filter by namespace if available
    					//for (Root incl: pool.findIncludesByName(inclName, this)) {
    					for (Root incl: pool.findIncludesByName(inclName, this, true)) {
    					// END KGU#408 2021-02-24
    						typeMap.putAll(incl.getTypeInfo());
    					}
    				}
    				// END KGU#676 2019-03-30
    			}
    		}
    		// END KGU#388 2017-09-18
    		IElementVisitor collector = new IElementVisitor() {

    			@Override
    			public boolean visitPreOrder(Element _ele) {
    				if (!_ele.isDisabled(true)) {	// FIXME shouldn't this be isDisabled(false)?
    					_ele.updateTypeMap(typeMap);
    				}
    				return true;
    			}

    			@Override
    			public boolean visitPostOrder(Element _ele) {
    				return true;
    			}
    			
    		};
    		this.traverse(collector);
    	}
    	return this.typeMap;
    }
    
    /** Also consider clearVarAndTypeInfo() */
    private void clearTypeInfo()
    {
    	// START KGU#502 2018-03-12: Bugfix #518
    	//this.typeMap.clear();
    	this.typeMap = null;
    	// END KGU#502 2018-03-12
    }
    // END KGU#261 2017-01-20

	// START KGU#701/KGU#703 2019-03-30: Issue #718, #720
	/**
	 * Clears (invalidates) all cached variable names and type information.
	 * Also resets the drawing information recursively if {@link Element#E_VARHIGHLIGHT}
	 * is true.
	 * @param clearDrawInfo - if true then drawing info will also be reset (otherwise only
	 * in case of {@link Element#E_VARHIGHLIGHT}).
	 */
	public void clearVarAndTypeInfo(boolean clearDrawInfo)
	{
		this.variables = null;
		this.constants.clear();
		this.clearTypeInfo();
		// START KGU#990 2021-10-02: Bugfix #990 - new fields to facilitate export
		this.returnsValue = null;
		// END KGU#990 2021-10-02
		if (clearDrawInfo || E_VARHIGHLIGHT) {
			this.resetDrawingInfoDown();
		}
	}
	// END KGU#701/KGU#703 2019-03-30

	// START KGU#261/KGU#332 2017-02-01: Enh. #259/#335
	/**
	 * Adds all parameter declarations to the given map (varname -> typeinfo).
	 * @param typeMap
	 */
	public void updateTypeMap(HashMap<String, TypeMapEntry> typeMap)
	{
		ArrayList<Param> parameters = getParams();
		String typeSpec = null;
		for (Param par: parameters) {
			// START KGU#925 2021-02-04: Bugfix #925 - can't need prefixes like "const" here
			//if ((typeSpec = par.getType()) != null) {
			if ((typeSpec = par.getType(true)) != null) {
			// END KGU#925 2021-02-04
				this.addToTypeMap(typeMap, par.getName(), typeSpec, 0, true, true);
			}
		}
		if (this.isSubroutine()) {
			typeSpec = this.getResultType();
			if (typeSpec != null) {
				// START KGU#593 2018-10-05: Issue #619 - missing declarations on C++ export
				// This is somewhat tricky here: The result type is an explicit return variable declaration for
				// Pascal, but it's not for C++, Java etc. So, for code export consistency we must take into
				// consideration where we check whether an explicit variable declaration will come (mostly C++,
				// C#, Java) we drive better if we don't set the "explicitly" flag here.
				//this.addToTypeMap(typeMap, this.getMethodName(), typeSpec, 0, false, true, false);
				this.addToTypeMap(typeMap, this.getMethodName(), typeSpec, 0, false, false);
				// END KGU#593 2018-10-05
			}
		}
	}
	// END KGU#261/KGU#332 2017-02-01

	// START BFI 2015-12-10
	/**
	 * Identifies parameter names, types, and default values of the routine and returns an array list
	 * of Param objects being name-type-default triples.
	 * This is just a different aggregation of the same results {@link #getParameterNames()},
	 * {@link #getParameterTypes()}, and {@link #getParameterDefaults()} would provide.
	 * @return the list of the declared parameters
	 */
	public ArrayList<Param> getParams()
	{
		// START KGU#371 2019-03-07: Enh. #385 Now we cache this list for beter performance
		//ArrayList<Param> resultVars = new ArrayList<Param>();

		//StringList names = new StringList();
		//StringList types = new StringList();
		//collectParameters(names, types);
		//for (int i = 0; i < names.count(); i++)
		//{
		//    resultVars.add(new Param(names.get(i), types.get(i)));
		//}

		//return resultVars;
		if (parameterList == null) {
			// Method fills parameterList
			collectParameters(null, null, null);
		}
		if (parameterList == null) {
			return new ArrayList<Param>();
		}
		return parameterList;
		// END KGU#371 2019-03-07
	}
	// END BFI 2015-12-10
	
    // START KGU 2016-03-25: JLabel replaced by new class LangTextHolder
    //private String errorMsg(JLabel _label, String _rep)
    private String errorMsg(LangTextHolder _label, String _subst)
    // END KGU 2016-03-25
    {
            String res = _label.getText();
            res = res.replace("%", _subst);
            return res;
    }
    
    // START KGU#239 2016-08-12: New opportunity to insert more than one information
    private String errorMsg(LangTextHolder _label, String[] _substs)
    {
        String msg = _label.getText();
        for (int i = 0; i < _substs.length; i++)
        {
        	msg = msg.replace("%"+(i+1), _substs[i]);
        }
        return msg;
    	
    }
    // END KGU#239 2016-08-12

    // START KGU#78 2015-11-25: We additionally supervise return mechanisms
    //private void analyse(Subqueue _node, Vector _errors, StringList _vars, StringList _uncertainVars)
    /**
     * Analyses the subtree, which _node is local root of
     * @param _node - subtree root
     * @param _errors - the collected errors (may be enhanced by the call)
     * @param _vars - names of variables being set within the subtree
     * @param _uncertainVars - names of variables being set in some branch of the subtree 
     * @param _constants - constants defined hitherto
     * @param _resultFlags - a boolean array: {usesReturn?, usesResult?, usesProcName?}
     * @param _types - the type definitions and declarations encountered so far
     */
    private void analyse(Subqueue _node, Vector<DetectedError> _errors, StringList _vars, StringList _uncertainVars, HashMap<String, String> _constants, boolean[] _resultFlags, HashMap<String, TypeMapEntry> _types)
    {
    	for (int i = 0; i < _node.getSize(); i++)
    	{
    		Element ele = _node.getElement(i);
    		// START KGU#277 2016-10-13: Enh. #270 - disabled elements are to be handled as if they wouldn't exist
    		if (ele.isDisabled(true)) continue;
    		// END KGU#277 2016-10-13
    		String eleClassName = ele.getClass().getSimpleName();
    		
    		// get all set variables from actual instruction (just this level, no substructre)
    		StringList myVars = getVarNames(ele);

    		// START KGU#1012 2021-11-14: Enh. #967
    		if (pluginSyntaxCheckers != null && !eleClassName.equals("Root") && !eleClassName.equals("Parallel")) {
    			for (Map.Entry<String, GeneratorSyntaxChecker> chkEntry: pluginSyntaxCheckers.entrySet()) {
    				if (pluginChecks.get(chkEntry.getKey()) == true) {
    					GeneratorSyntaxChecker checker = chkEntry.getValue();
    					StringList lines = ele.getUnbrokenText();
    					GENPlugin.SyntaxCheck.Source testType = 
    							GENPlugin.SyntaxCheck.Source.valueOf(chkEntry.getKey().split(":")[1]);
    					switch (testType) {
    					case STRING:
    						for (int l = 0; l < lines.count(); l++) {
    							String line = lines.get(l);
    							String error = checker.checkSyntax(line, ele, l);
    							if (error != null) {
    								// FIXME: Fetch the plugin-configured message
    								addError(_errors, new DetectedError(error.replace("error.syntax", "ARM syntax violation"), ele), -2);
    							}
    						}
    						break;
    					case TREE:
    						// FIXME not implemented yet: iterate over parsed lines
    						break;
    					}
    				}
    			}
    		}
    		// END KGU#1012 2021-11-14
    		
    		// CHECK: assignment in condition (#8)
    		if (eleClassName.equals("While")
    				|| eleClassName.equals("Repeat")
    				|| eleClassName.equals("Alternative"))
    		{
    			analyse_8(ele, _errors);
    		}
    		
    		// CHECK  #5: non-uppercase var
    		// CHECK  #7: correct identifiers
    		// CHECK #13: Competitive return mechanisms
    		analyse_5_7_13(ele, _errors, myVars, _resultFlags);
    		
    		// START KGU#239/KGU#327 2016-08-12: Enh. #231 / # 329
    		// CHECK #18: Variable names only differing in case
    		// CHECK #19: Possible name collisions with reserved words
    		// CHECK #21: Mistakable variable names I, l, O
    		analyse_18_19_21(ele, _errors, _vars, _uncertainVars, myVars);
    		// END KGU#239/KGU#327 2016-08-12

    		// CHECK #10: wrong multi-line instruction
    		// CHECK #11: wrong assignment (comparison operator in assignment)
    		// CHECK #22: constant depending on non-constants or constant redefinition
    		// CHECK #24: type definitions
    		if (eleClassName.equals("Instruction"))
    		{
    			analyse_10_11(ele, _errors);
    			// START KGU#375 2017-04-04: Enh. #388
    			// START KGU#388 2017-09-16: Enh. #423 record analysis
    			//analyse_22((Instruction)ele, _errors, _vars, _uncertainVars, _constants);
    			analyse_22_24((Instruction)ele, _errors, _vars, _uncertainVars, _constants, _types);
    			// END KGU#388 2017-09-16
    			// END KGU#375 2017-04-04
    		}

    		// START KGU#992 2021-10-05: Enh. #992
    		// CHECK #30: Bracket balancing
    		analyse_30(ele, _errors);
    		// END KGU#992 2021-10-05

    		// CHECK: non-initialised var (except REPEAT)  (#3)
    		// START KGU#375 2017-04-05: Enh. #388 linewise analysis for Instruction elements
//    		StringList myUsed = getUsedVarNames(ele, true, true);
//    		if (!eleClassName.equals("Repeat"))
//    		{
//    			// FIXME: linewise test for Instruction elements needed
//    			analyse_3(ele, _errors, _vars, _uncertainVars, myUsed);
//    		}
    		StringList myUsed = new StringList();
    		if (eleClassName.equals("Instruction"))
    		{
    			@SuppressWarnings("unchecked")
    			HashMap<String, String> constantDefs = (HashMap<String, String>)_constants.clone();
    			String[] keywords = CodeParser.getAllProperties();
    			StringList initVars = _vars.copy();
    			// START KGU#423 2017-09-13: Enh. #416 - cope with user-defined line breaks
    			//for (int j = 0; j < ele.getText().count(); j++) {
    			StringList unbrokenText = ele.getUnbrokenText();
    			for (int j = 0; j < unbrokenText.count(); j++) {
    			// END KGU#423 2017-09-13
    				String line = unbrokenText.get(j);
    				// START KGU#388 2017-09-13: Enh. #423
    				if (!Instruction.isTypeDefinition(line, _types)) {
    				// END KGU#388 2017-09-13
    					myUsed = getUsedVarNames(line, keywords);
    					// START KGU#985 2021-10-07: Bugfix #995 - Pass line number, respect "healing"
    					//analyse_3(ele, _errors, initVars, _uncertainVars, myUsed, -1);
    					//initVars.add(this.getVarNames(StringList.getNew(line), constantDefs));
    					analyse_3(ele, _errors, initVars, _uncertainVars, myUsed, j);
    					StringList asgdVars = this.getVarNames(StringList.getNew(line), constantDefs);
    					initVars.add(asgdVars);
    					for (int k = 0; k < asgdVars.count(); k++) {
    						_uncertainVars.removeAll(asgdVars.get(k));
    					}
    					// END KGU#985 2021-10-07
    				// START KGU#388 2017-09-13: Enh. #423
    				}
    				// END KGU#388 2017-09-13
    			}
    		}
    		else {
    			myUsed = getUsedVarNames(ele, true, true);
    			if (!eleClassName.equals("Repeat"))
    			{
    				analyse_3(ele, _errors, _vars, _uncertainVars, myUsed, -1);
    			}
    		}
    		// END KGU#375 2017-04-05

    		/*////// AHHHHHHHH ////////
                            getUsedVarNames should also parse for new variable names,
                            because any element that uses a variable that has never been
                            assigned, this variable will not be known and thus not
                            detected at all!
                            KGU#163 2016-03-25: Solved
    		 */
    		/*
    		if(_node.getElement(i).getClass().getSimpleName().equals("Instruction"))
    		{
    			System.out.println("----------------------------");
    			System.out.println(((Element) _node.getElement(i)).getText());
    			System.out.println("----------------------------");
    			System.out.println("Vars : "+myVars);
    			System.out.println("Init : "+_vars);
    			System.out.println("Used : "+myUsed);
    			//System.out.println("----------------------------");
    		}
    		/**/

    		// START KGU#2/KGU#78 2015-11-25: New checks for Call and Jump elements
    		// CHECK: Correct syntax of Call elements (#15) New!
    		if (ele instanceof Call)
    		{
    			analyse_15((Call)ele, _errors);
    		}
    		// CHECK: Correct usage of Jump, including return (#16) New!
    		// + CHECK #13: Competitive return mechanisms
    		else if (ele instanceof Jump)
    		{
    			analyse_13_16_jump((Jump)ele, _errors, myVars, _resultFlags);
    		}
    		else if (ele instanceof Instruction)	// May also be a subclass (except Call and Jump)!
    		{
    		// END KGU#78 2015-11-25
				analyse_13_16_instr((Instruction)ele, _errors, i == _node.getSize()-1, myVars, _resultFlags);
    		// START KGU#78 2015-11-25
    		}
    		// END KGU#78 2015-11-25

    		// add detected vars to initialised vars
//    		// START KGU#376 2017-04-11: Enh. #389 - withdrawn 2017-04-20
    		_vars.addIfNew(myVars);
//    		if (!(ele instanceof Call && ((Call)ele).isImportCall())) {
//    			_vars.addIfNew(myVars);
//    		}
//    		// END KGU#376 2017-04-20

    		// CHECK: endless loop (#2)
    		if (eleClassName.equals("While")
    				|| eleClassName.equals("Repeat"))
    		{
    			analyse_2(ele, _errors);
    		}

    		// CHECK: loop var modified (#1) and loop parameter consistency (#14 new!)
    		if (eleClassName.equals("For"))
    		{
    			// START KGU#923 2021-02-01: Bugfix #923 FOR loops introduce variables!
    			if (ele instanceof For) {
    				ele.updateTypeMap(_types);
    			}
    			// END KGU#923 2021-02-01
    			analyse_1_2_14((For)ele, _errors);
    		}

    		// CHECK: if with empty T-block (#4)
    		if (eleClassName.equals("Alternative"))
    		{
    			if(((Alternative)ele).qTrue.getSize()==0)
    			{
    				//error  = new DetectedError("You are not allowed to use an IF-statement with an empty TRUE-block!",(Element) _node.getElement(i));
    				addError(_errors, new DetectedError(errorMsg(Menu.error04,""), ele), 4);
    			}
    		}
    		
    		// CHECK: Inconsistency risk due to concurrent variable access by parallel threads (#17) New!
    		if (eleClassName.equals("Parallel"))
    		{
    			analyse_17((Parallel) ele, _errors);
    		}
    		// START KGU#514 2018-04-03: Bugfix #528 (for Instructions, it has already been done above)
    		else if (check(24) && !eleClassName.equals("Instruction")) {
    			analyse_24(ele, _errors, _types);
    		}
    		// END KGU#514 2018-04-03


    		// continue analysis for subelements
    		if (ele instanceof ILoop)
    		{
    			analyse(((ILoop) ele).getBody(), _errors, _vars, _uncertainVars, _constants, _resultFlags, _types);
    		
    			if (ele instanceof Repeat)
    			{
    				analyse_3(ele, _errors, _vars, _uncertainVars, myUsed, -1);
    			}
    		}
    		else if (eleClassName.equals("Parallel"))
    		{
    			StringList initialVars = _vars.copy();
    			Iterator<Subqueue> iter = ((Parallel)ele).qs.iterator();
    			while (iter.hasNext())
    			{
    				// For the thread, propagate only variables known before the parallel section
    				StringList threadVars = initialVars.copy();
    				analyse(iter.next(), _errors, threadVars, _uncertainVars, _constants, _resultFlags, _types);
    				// Any variable introduced by one of the threads will be known after all threads have terminated
    				_vars.addIfNew(threadVars);
    			}
    		}
    		else if(eleClassName.equals("Alternative"))
    		{
    			StringList tVars = _vars.copy();
    			StringList fVars = _vars.copy();

    			analyse(((Alternative)ele).qTrue, _errors, tVars, _uncertainVars, _constants, _resultFlags, _types);
    			analyse(((Alternative)ele).qFalse, _errors, fVars, _uncertainVars, _constants, _resultFlags, _types);

    			for(int v = 0; v < tVars.count(); v++)
    			{
    				String varName = tVars.get(v);
    				if (fVars.contains(varName)) { _vars.addIfNew(varName); }
    				else if (!_vars.contains(varName)) { _uncertainVars.add(varName); }
    			}
    			for(int v = 0; v < fVars.count(); v++)
    			{
    				String varName = fVars.get(v);
    				if (!_vars.contains(varName)) { _uncertainVars.addIfNew(varName); }
    			}

    			// if a variable is not being initialised on both of the lists,
    			// it could be considered as not always being initialised
    			//
    			// => use a second list with variable that "may not have been initialised"
    		}
    		else if(eleClassName.equals("Case"))
    		{
    			Case caseEle = ((Case) ele);
				int si = caseEle.qs.size();	// Number of branches
    			StringList initialVars = _vars.copy();
    			// START KGU#758 2019-11-08: Enh. #770
    			analyse_27_28(caseEle, _errors);
    			// END KGU#758 2019-11-08
    			// START KGU#928 2021-02-08: Enh. #928 - discriminator type check added
    			analyse_29(caseEle, _errors, _types);
    			// END KGU#928 2021-02-08
    			    			 
    			// This Hashtable will contain strings composed of as many '1' characters as
    			// branches initialise the respective new variable - so in the end we can see
    			// which variables aren't always initialised.
    			Hashtable<String, String> myInitVars = new Hashtable<String, String>();
    			for (int j=0; j < si; j++)
    			{
    				StringList caseVars = initialVars.copy();
    				analyse((Subqueue) caseEle.qs.get(j),_errors, caseVars, _uncertainVars, _constants, _resultFlags, _types);
    				for(int v = 0; v < caseVars.count(); v++)
    				{
    					String varName = caseVars.get(v);
    					if(myInitVars.containsKey(varName))
    					{
    						myInitVars.put(varName, myInitVars.get(varName) + "1");
    					}
    					else
    					{
    						myInitVars.put(varName, "1");
    					}
    				}
    				//_vars.addIfNew(caseVars);
    			}
    			//System.out.println(myInitVars);
    			// walk trough the hash table and check
    			Enumeration<String> keys = myInitVars.keys();
    			// adapt size if no "default"
    			if ( caseEle.getText().get(caseEle.getText().count()-1).equals("%") )
    			{
    				si--;
    			}
    			//System.out.println("SI = "+si+" = "+c.text.get(c.text.count()-1));
    			while ( keys.hasMoreElements() )
    			{
    				String key = keys.nextElement();
    				String value = myInitVars.get(key);

    				if(value.length()==si)
    				{
    					_vars.addIfNew(key);
    				}
    				else
    				{
    					if(!_vars.contains(key))
    					{
    						_uncertainVars.addIfNew(key);
    					}
    				}
    			}
    			// look at the comment for the IF-structure
    		}
    		// START KGU#812 2020-02-21: Bugfix #825 forgotten to descend recursively
    		else if (eleClassName.equals("Try")) {
    			StringList tVars = _vars.copy();
    			StringList cVars = _vars.copy();
    			StringList fVars = _vars.copy();

    			cVars.addIfNew(((Try)ele).getText().trim());
    			
    			analyse(((Try)ele).qTry, _errors, tVars, _uncertainVars, _constants, _resultFlags, _types);
    			analyse(((Try)ele).qCatch, _errors, cVars, _uncertainVars, _constants, _resultFlags, _types);

    			for(int v = 0; v < tVars.count(); v++)
    			{
    				String varName = tVars.get(v);
    				if (!_vars.contains(varName)) { _uncertainVars.add(varName); }
    			}
    			for(int v = 0; v < cVars.count(); v++)
    			{
    				String varName = cVars.get(v);
    				if (!_vars.contains(varName)) { _uncertainVars.add(varName); }
    			}
    			
    			analyse(((Try)ele).qFinally, _errors, fVars, _uncertainVars, _constants, _resultFlags, _types);

    			for(int v = 0; v < fVars.count(); v++)
    			{
    				String varName = fVars.get(v);
    				_vars.addIfNew(varName);
    			}

    		}
    		// END KGU#812 2020-02-21
    		// START KGU#376 2017-04-11: Enh. #389 - revised 2017-04-20 - disabled 2017-07-01
    		//else if ((ele instanceof Call && ((Call)ele).isImportCall())) {
    		//	analyse_23((Call)ele, _errors, _vars, _uncertainVars, _constants);
    		//}
    		// END KGU#376 2017-04-11
    		
    	} // for(int i=0; i < _node.size(); i++)...
    }
    
    // START KGU 2016-03-24: Decomposed analyser methods
    
    /**
     * CHECK  #1: loop var modified<br/>
     * CHECK #14: loop parameter consistency
     * @param ele - For element to be analysed
     * @param _errors - global list of errors
     */
	private void analyse_1_2_14(For ele, Vector<DetectedError> _errors)
	{
		// get assigned variables from inside the FOR-loop
		StringList modifiedVars = getVarNames(ele, false, true);
		// get loop variable (that should be only one!!!)
		StringList loopVars = getVarNames(ele, true);
		// START KGU#61 2016-03-21: Enh. #84 - ensure FOR-IN variables aren't forgotten
		String counterVar = ele.getCounterVar();
		if (counterVar != null && !counterVar.isEmpty())
		{
			loopVars.addIfNew(counterVar);
		}
		// END KGU#61 2016-03-21

		/*
		System.out.println("MODIFIED : "+modifiedVars);
		System.out.println("LOOP     : "+loopVars);
		/**/

		if (loopVars.isEmpty())
		{
			//error  = new DetectedError("WARNING: No loop variable detected ...",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error01_1,""), ele), 1);
		}
		else
		{
			if (loopVars.count() > 1)
			{
				//error  = new DetectedError("WARNING: More than one loop variable detected ...",(Element) _node.getElement(i));
				// START KGU#260 2016-09-25: It seems sensible to show the variable names (particularly to identify malfunction) 
				//addError(_errors, new DetectedError(errorMsg(Menu.error01_2,""), ele), 1);
				addError(_errors, new DetectedError(errorMsg(Menu.error01_2, loopVars.concatenate("», «")), ele), 1);
				// END KGU#260 2016-09-25
			}

			// START KGU#923 2021-02-01: Bugfix #923 This check does not make sense in FOR-IN loops
			//if (modifiedVars.contains(loopVars.get(0)))
			if (modifiedVars.contains(loopVars.get(0)) && !ele.isForInLoop())
			// END KGU#923 201-02-01
			{
				//error  = new DetectedError("You are not allowed to modify the loop variable «"+loopVars.get(0)+"» inside the loop!",(Element) _node.getElement(i));
				addError(_errors, new DetectedError(errorMsg(Menu.error01_3, loopVars.get(0)), ele), 1);
			}
		}

		// START KGU#3 2015-11-03: New check for consistency of the loop header
		if (!ele.checkConsistency()) {
			//error  = new DetectedError("FOR loop parameters are not consistent to the loop heading text!", elem);
			addError(_errors, new DetectedError(errorMsg(Menu.error14_1,""), ele), 14);
		}
		
		String stepStr = ele.splitForClause()[4];
		// The following test automatically excludes FOR-IN loops as well
		if (!stepStr.isEmpty())
		{
			// Just in case...
			//error  = new DetectedError("FOR loop step parameter «"+stepStr+"» is no legal integer constant!", elem);
			DetectedError error = new DetectedError(errorMsg(Menu.error14_2, stepStr), ele);
			try {
				int stepVal = Integer.parseInt(stepStr);
				if (stepVal == 0)
				{
					// Two kinds of error at the same time
					addError(_errors, error, 14);
					//error  = new DetectedError("No change of the variables in the condition detected. Possible endless loop ...",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error02,""), ele), 2);
				}
			}
			catch (NumberFormatException ex)
			{
				addError(_errors, error, 14);                                    		
			}
		}
		// END KGU#3 2015-11-03
	}

	/**
	 * CHECK #2: Endless loop
	 * @param ele - Loop element to be analysed
	 * @param _errors - global error list
	 */
	private void analyse_2(Element ele, Vector<DetectedError> _errors)
	{
		// get modified and introduced variables from inside the loop
		StringList modifiedVars = getVarNames(ele, false);
		// get loop condition variables
		StringList loopVars = getUsedVarNames(ele, true, true);

		/*
		System.out.println(eleClassName + " : " + ele.getText().getLongString());
		System.out.println("Used : "+usedVars);
		System.out.println("Loop : "+loopVars);
		/**/

		boolean check = false;
		for(int j=0; j<loopVars.count(); j++)
		{
			check = check || modifiedVars.contains(loopVars.get(j));
		}
		if (check==false)
		{
			//error  = new DetectedError("No change of the variables in the condition detected. Possible endless loop ...",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error02,""), ele), 2);
		}
	}

	/**
	 * CHECK #3: non-initialised variables (except REPEAT)
	 * @param _ele - Element to be analysed
	 * @param _errors - global error list
	 * @param _vars - variables with certain initialisation 
	 * @param _uncertainVars - variables with uncertain initialisation (e.g. in a branch)
	 * @param _myUsedVars - variables used but not defined by this element
	 * @param _lineNo - number of the originating code line
	 */
	private void analyse_3(Element _ele, Vector<DetectedError> _errors, StringList _vars, StringList _uncertainVars, StringList _myUsedVars, int _lineNo)
	{
			for (int j=0; j<_myUsedVars.count(); j++)
			{
				String myUsed = _myUsedVars.get(j);
				// START KGU#343 2017-02-07: Ignore pseudo-variables (markers)
				if (myUsed.startsWith("§ANALYSER§")) {
					continue;
				}
				// END KGU#343 2017-02-07
				// START KGU##375 2017-04-05: Enh. #388
				String lineRef = "";
				if (_lineNo >= 0) {
					lineRef = Menu.errorLineReference.getText().replace("%", Integer.toString(_lineNo+1));
				}
				// END KGU#375 2017-04-05
				if (!_vars.contains(myUsed) && !_uncertainVars.contains(myUsed))
				{
					//error  = new DetectedError("The variable «"+myUsed.get(j)+"» has not yet been initialized!",(Element) _node.getElement(i));
					// START KGU##375 2017-04-05: Enh. #388
					//addError(_errors, new DetectedError(errorMsg(Menu.error03_1, myUsed), ele), 3);
					addError(_errors, new DetectedError(errorMsg(Menu.error03_1, new String[]{myUsed, lineRef}), _ele), 3);
					// END KGU#375 2017-04-05
				}
				else if (_uncertainVars.contains(myUsed))
				{
					//error  = new DetectedError("The variable «"+myUsed.get(j)+"» may not have been initialized!",(Element) _node.getElement(i));
					// START KGU##375 2017-04-05: Enh. #388
					//addError(_errors, new DetectedError(errorMsg(Menu.error03_2, myUsed), ele), 3);
					addError(_errors, new DetectedError(errorMsg(Menu.error03_2, new String[]{myUsed, lineRef}), _ele), 3);
					// END KGU#375 2017-04-05
				}
			}
	}


	/**
	 * Three checks in one loop:<br/>
	 * CHECK  #5: non-uppercase var<br/>
	 * CHECK  #7: correct identifiers<br/>
	 * CHECK #13: Competitive return mechanisms
	 * @param ele - the element to be checked
	 * @param _errors - the global error list
	 * @param _myVars - the variables detected so far
	 * @param _resultFlags - 3 flags for (0) return, (1) result, and (2) function name
	 */
	private void analyse_5_7_13(Element ele, Vector<DetectedError> _errors, StringList _myVars, boolean[] _resultFlags)
	{
		// START KGU#812 2020-02-21: Bugfix #825 We check the error variable of a TRY block too
		if (ele instanceof Try) {
			_myVars = _myVars.copy();
			_myVars.addIfNew(((Try)ele).getExceptionVarName());			
		}
		// END KGU#812 2020-02-21
		for (int j=0; j<_myVars.count(); j++)
		{
			String myVar = _myVars.get(j);
			// START KGU#343 2017-02-07: Ignore pseudo-variables (markers)
			if (myVar.startsWith("§ANALYSER§")) {
				continue;
			}
			// END KGU#343 2017-02-07

			// CHECK: non-uppercase var (#5)
			if(!myVar.toUpperCase().equals(myVar) && !rootVars.contains(myVar))
			{
				if(!((myVar.toLowerCase().equals("result") && this.isSubroutine())))
				{
					//error  = new DetectedError("The variable «"+myVars.get(j)+"» must be written in uppercase!",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error05, myVar), ele), 5);
				}
			}

			// CHECK: correct identifiers (#7)
			// START KGU#61 2016-03-22: Method outsourced
			//if(testidentifier(myVars.get(j))==false)
			if (!Function.testIdentifier(myVar, false, null))
			// END KGU#61 2016-03-22
			{
				//error  = new DetectedError("«"+myVars.get(j)+"» is not a valid name for a variable!",(Element) _node.getElement(i));
				addError(_errors, new DetectedError(errorMsg(Menu.error07_3, myVar), ele), 7);
			}
			// START KGU#877 2020-10-16: Bugfix #874
			else if (!Function.testIdentifier(myVar, true, null))
			{
				//error  = new DetectedError(Identifier "«"+myVar+"» contains non-ascii letters, this should be avoided.",(Element) _node.getElement(i));
				addError(_errors, new DetectedError(errorMsg(Menu.error07_5, myVar), ele), 7);
			}
			// END KGU#877 2020-10-16

			// START KGU#78 2015-11-25
			// CHECK: Competitive return mechanisms (#13)
			if (this.isSubroutine() && myVar.toLowerCase().equals("result"))
			{
				_resultFlags[1] = true;
				if (_resultFlags[0] || _resultFlags[2])

				{
					//error  = new DetectedError("Your function seems to use several competitive return mechanisms!",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error13_3, myVar), ele), 13);
				}
			}
			else if (this.isSubroutine() && myVar.equals(getMethodName()))
			{
				_resultFlags[2] = true;
				if (_resultFlags[0] || _resultFlags[1])

				{
					//error  = new DetectedError("Your functions seems to use several competitive return mechanisms!",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error13_3, myVar), ele), 13);
				}
			}
			// END KGU#78 2015-11-25
		}

	}

	/**
	 * CHECK #8: assignment in condition
	 * @param ele - the element to be checked
	 */
	private void analyse_8(Element ele, Vector<DetectedError> _errors)
	{
		String condition = ele.getText().getLongString();
		if ( condition.contains("<-") || condition.contains(":=") )
		{
			//error  = new DetectedError("It is not allowed to make an assignment inside a condition.",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error08,""), ele), 8);
		}
	}

	/**
	 * Two checks:<br/>
	 * CHECK #10: wrong multi-line instruction<br/>
	 * CHECK #11: wrong assignment (comparison operator in assignment)
	 * @param ele - Element to be analysed
	 * @param _errors - global error list
	 */
	private void analyse_10_11(Element ele, Vector<DetectedError> _errors)
	{
		// START KGU#413 2017-09-13: Enh. #416 cope with user-defined line breaks
		//StringList test = ele.getText();
		StringList test = ele.getUnbrokenText();
		// END KGU#413 2017-09-13

		// CHECK: wrong multi-line instruction (#10)
		boolean isInput = false;
		boolean isOutput = false;
		boolean isAssignment = false;
		// START KGU#375 2017-04-05: Enh. #388
		boolean isConstant = false;
		// END KGU#375 2017-04-05
		// START KGU#388 2017-09-13: Enh. #423
		boolean isTypedef = false;
		// END KGU#388 2017-09-13
		StringList inputTokens = Element.splitLexically(CodeParser.getKeyword("input"), false);
		StringList outputTokens = Element.splitLexically(CodeParser.getKeyword("output"), false);
		// START KGU#297 2016-11-22: Issue #295 - Instructions starting with the return keyword must be handled separately
		StringList returnTokens = Element.splitLexically(CodeParser.getKeyword("preReturn"), false);
		// END KGU#297 2016-11-22

		// Check every instruction line...
		for(int lnr = 0; lnr < test.count(); lnr++)
		{
			// CHECK: wrong assignment (#11 - new!)
			//String myTest = test.get(l);

			// START KGU#65/KGU#126 2016-01-06: More precise analysis, though expensive
			StringList tokens = splitLexically(test.get(lnr).trim(), true);
			unifyOperators(tokens, false);
			// START KGU#297 2016-11-22: Issue #295 - Instructions starting with the return keyword must be handled separately
			//if (tokens.contains("<-"))
			boolean isReturn = tokens.indexOf(returnTokens, 0, !CodeParser.ignoreCase) == 0;
			// END KGU#297 2016-11-22
			if (tokens.contains("<-") && !isReturn)
			{
				// START KGU#375 2017-04-05: Enh. #388
				//isAssignment = true;
				if (tokens.get(0).equals("const")) {
					isConstant = true;
				}
				else {
					isAssignment = true;
				}
				// END KGU#375 2017-04-05
			}
			// START KGU#388 2017-09-13: Enh. #423
			else if (tokens.indexOf("type", 0, !CodeParser.ignoreCase) == 0) {
				isTypedef = true;
			}
			// END KGU#388 2017-09-13
			// START KGU#297 2016-11-22: Issue #295: Instructions starting with the return keyword must be handled separately
			//else if (tokens.contains("=="))
			else if (!isReturn && tokens.contains("==") || isReturn && tokens.contains("<-"))
			// END KGU#297 2016-11-22
			{
				//error  = new DetectedError("You probably made an assignment error. Please check this instruction!",(Element) _node.getElement(i));
				addError(_errors, new DetectedError(errorMsg(Menu.error11,""), ele), 11);
			}
			
			// CHECK: wrong multi-line instruction (#10 - new!)	
			if (tokens.indexOf(inputTokens, 0, !CodeParser.ignoreCase) == 0)
			{
				isInput = true;
			}
			if (tokens.indexOf(outputTokens, 0, !CodeParser.ignoreCase) == 0)
			{
				isOutput = true;
			}
			// END KGU#65/KGU#126 2016-01-06

		}
		// CHECK: wrong multi-line instruction (#10 - new!)
		// START KGU#375 2017-04-05: Enh. #388
		//if (isInput && isOutput && isAssignment) {
		// START KGU#388 2017-09-13: Enh. #423
		//if (isConstant && (isInput || isOutput || isAssignment)) {
		if (isTypedef && (isConstant || isInput || isOutput || isAssignment)) {
			//error  = new DetectedError("A single element should not mix type definitions with other instructions!",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error10_6,""), ele), 10);
		}
		else if (isConstant && (isInput || isOutput || isAssignment)) {
		// END KGU#388 2017-09-13
			//error  = new DetectedError("A single element should not mix constant type definitions with other instructions!",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error10_5,""), ele), 10);
		}
		else if (isInput && isOutput && isAssignment)
		// END KGU#375 2017-04-05
		{
			//error  = new DetectedError("A single instruction element should not contain input/output instructions and assignments!",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error10_1,""), ele), 10);
		}
		else if (isInput && isOutput)
		{
			//error  = new DetectedError("A single instruction element should not contain input and output instructions!",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error10_2,""), ele), 10);
		}
		else if (isInput && isAssignment)
		{
			//error  = new DetectedError("A single instruction element should not contain input instructions and assignments!",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error10_3,""), ele), 10);
		}
		else if (isOutput && isAssignment)
		{
			//error  = new DetectedError("A single instruction element should not contain ouput instructions and assignments!",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error10_4,""), ele), 10);
		}
	}

	/**
	 * CHECK #15: Correct syntax of Call elements
	 * @param ele - CALL Element to be analysed
	 * @param _errors - global error list
	 */
	private void analyse_15(Call ele, Vector<DetectedError> _errors)
	{
		// START KGU#376 2017-04-11: Enh. #389 - new call type / undone 2017-07-01
		if (!ele.isProcedureCall(false) && !ele.isFunctionCall(false))
		//if (!ele.isProcedureCall() && !ele.isFunctionCall() && !ele.isImportCall())
		// END KGU#376 2017-04-11
		{
			//error  = new DetectedError("The CALL hasn't got form «[ <var> " + "\u2190" +" ] <routine_name>(<arg_list>)»!",(Element) _node.getElement(i));
			// START KGU#278 2016-10-11: Enh. #267
			//addError(_errors, new DetectedError(errorMsg(Menu.error15, ""), ele), 15);
			addError(_errors, new DetectedError(errorMsg(Menu.error15_1, ""), ele), 15);
			// END KGU#278 2016-10-11
		}
		// START KGU#278 2016-10-11: Enh. #267
		// START KGU#376 2017-04-11: Enh. #389 - undone 2017-07-01
//		else if (ele.isImportCall()) {
//			String name = ele.getSignatureString();
//			int count = 0;	// Number of matching routines
//			if (Arranger.hasInstance()) {
//				count = Arranger.getInstance().findIncludesByName(name).size();
//			}
//			if (count == 0) {
//				//error  = new DetectedError("The called subroutine «<routine_name>(<arg_count>)» is currently not available.",(Element) _node.getElement(i));
//				addError(_errors, new DetectedError(errorMsg(Menu.error15_2, name), ele), 15);
//			}
//			else if (count > 1) {
//				//error  = new DetectedError("There are several matching subroutines for «<routine_name>(<arg_count>)».",(Element) _node.getElement(i));
//				addError(_errors, new DetectedError(errorMsg(Menu.error15_3, name), ele), 15);					
//			}
//		}
		// END KGU#376 2017-04-11
		else {
			// START KGU 2017-04-11: We have methods of higher level here!
			//String text = ele.getText().get(0);
			//StringList tokens = Element.splitLexically(text, true);
			//Element.unifyOperators(tokens, true);
			//int asgnPos = tokens.indexOf("<-");
			//if (asgnPos > 0)
			//{
			//	// This looks somewhat misleading. But we do a mere syntax check
			//	text = tokens.concatenate("", asgnPos+1);
			//}
			//Function subroutine = new Function(text);
			Function subroutine = ele.getCalledRoutine();
			// END KGU 2017-04-11
			// START KGU#689 2019-03-20: Bugfix #706 - subroutine may be null here
			if (subroutine == null) {
				addError(_errors, new DetectedError(errorMsg(Menu.error15_1, ""), ele), 15);
				return;
			}
			// END KGU#689 2019-03-20
			String subName = subroutine.getName();
			int subArgCount = subroutine.paramCount();
			if ((!this.getMethodName().equals(subName) || subArgCount != this.getParameterNames().count()))
			{
				int count = 0;	// Number of matching routines
				if (Arranger.hasInstance()) {
					// START KGU#408 2021-02-25: Enh. #410 more precise filtering
					//count = Arranger.getInstance().findRoutinesBySignature(subName, subArgCount, this).size();
					count = Arranger.getInstance().findRoutinesBySignature(subName, subArgCount, this, true).size();
					// END KGU#408 2021-02-25
				}
				if (count == 0) {
					//error  = new DetectedError("The called subroutine «<routine_name>(<arg_count>)» is currently not available.",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error15_2, subName + "(" + subArgCount + ")"), ele), 15);
					// START KGU#877 2020-10-16: Issue #874 warn in case of a non-ascii name before the subroutine gets derived
					if (!Function.testIdentifier(subName, true, null)) {
						addError(_errors, new DetectedError(errorMsg(Menu.error07_5, subName), ele), 7);
					}
					// END KGU#877 2020-10-16
				}
				else if (count > 1) {
					//error  = new DetectedError("There are several matching subroutines for «<routine_name>(<arg_count>)».",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error15_3, subName + "(" + subArgCount + ")"), ele), 15);					
				}
			}
		}
		// END KGU#278 2016-10-11
	}

	/**
	 * CHECK #16: Correct usage of Jump, including return<br/>
	 * CHECK #13: Competitive return mechanisms
	 * @param ele - JUMP element to be analysed
	 * @param _errors - global error list
	 * @param _myVars - all variables defined or modified by this element (should be empty so far, might be extended) 
	 * @param _resultFlags - 3 flags for (0) return, (1) result, and (2) function name
	 */
	private void analyse_13_16_jump(Jump ele, Vector<DetectedError> _errors, StringList _myVars, boolean[] _resultFlags)
	{
		// START KGU#1023 2022-01-04: Bugfix #1026 line continuation wasn't recognised
		//StringList sl = ele.getText();
		StringList sl = ele.getUnbrokenText();
		// END KGU#1023 2022-01-04
		String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return");
		String preLeave = CodeParser.getKeywordOrDefault("preLeave", "leave");
		String preExit = CodeParser.getKeywordOrDefault("preExit", "exit");
		// START KGU#686 2019-03-18: Enh. #56
		//String jumpKeywords = "«" + preLeave + "», «" + preReturn +	"», «" + preExit + "»";
		String preThrow = CodeParser.getKeywordOrDefault("preThrow", "throw");
		String jumpKeywords = "«" + preLeave + "», «" + preReturn +	"», «" + preExit + "», «" + preThrow + "»";
		// END KGU#686 2019-03-18
		String line = sl.get(0).trim();
		String lineComp = line;

		// Preparation
		if (CodeParser.ignoreCase) {
			preReturn = preReturn.toLowerCase();
			preLeave = preLeave.toLowerCase();
			preExit = preExit.toLowerCase();
			//String jumpKeywords = "«" + preLeave + "», «" + preReturn +	"», «" + preExit + "»";
			preThrow = preThrow.toLowerCase();
			// END KGU#686 2019-03-18
			lineComp = line.toLowerCase();
		}
		boolean isReturn = ele.isReturn();
		boolean isLeave = ele.isLeave();
		boolean isExit = ele.isExit();
		//String jumpKeywords = "«" + preLeave + "», «" + preReturn +	"», «" + preExit + "»";
		boolean isThrow = ele.isThrow();
		// END KGU#686 2019-03-18
		boolean isJump = isLeave || isExit ||
				lineComp.matches("exit([\\W].*|$)") ||	// Also check hard-coded keywords
				lineComp.matches("break([\\W].*|$)");	// Also check hard-coded keywords
		Element parent = ele.parent;
		// START KGU#179 2022-01-04: Issue #161 similar check in multi-line case
		if (sl.count() > 1
				&& ((isReturn = Jump.isReturn(line))
						|| (isLeave = Jump.isLeave(line))
						|| (isExit = Jump.isExit(line))
						|| (isThrow = Jump.isThrow(line)))
				&& !sl.concatenate("", 1).trim().isEmpty()) {
			//error = new DetectedError("Instruction isn't reachable after a JUMP!",((Subqueue)parent).getElement(pos+1)));
			addError(_errors, new DetectedError(errorMsg(Menu.error16_7, ""), ele), 16);	
		}
		// END KGU#179 2022-01-04
		// START KGU#179 2016-04-12: Enh. #161 New check for unreachable instructions
		int pos = -1;
		if (parent instanceof Subqueue && (pos = ((Subqueue)parent).getIndexOf(ele)) < ((Subqueue)parent).getSize()-1)
		{
			//error = new DetectedError("Instruction isn't reachable after a JUMP!",((Subqueue)parent).getElement(pos+1)));
			addError(_errors, new DetectedError(errorMsg(Menu.error16_7, ""), ((Subqueue)parent).getElement(pos+1)), 16);	
		}
		// END KGU#179 2016-04-12

		// START KGU#78/KGU#365 2017-04-14: Enh. #23, #380 - completely rewritten
		
		// Routines and Parallel sections cannot be penetrated by leave or break
		boolean insideParallel = false;
		
		// CHECK: Incorrect Jump syntax?
		if (sl.count() > 1 || !(isJump || isReturn || isThrow || line.isEmpty()))
		{
			//error = new DetectedError("A JUMP element must contain exactly one of «exit n», «return <expr>», or «leave [n]»!",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error16_1, jumpKeywords), ele), 16);
		}
		// CHECK: Correct usage of return (nearby check result mechanisms) (#13, #16)
		if (isReturn)
		{
			// START KGU#343 2017-02-07: Disabled to suppress the warnings - may there be side-effects?
			//_resultFlags[0] = true;
			//_myVars.addIfNew("result");	// FIXME: This caused warnings if e.g. "Result" is used somewhere else
			if (!line.substring(preReturn.length()).trim().isEmpty()) {
				_resultFlags[0] = true;
				_myVars.addIfNew("§ANALYSER§RETURNS");
				// START KGU#78 2015-11-25: Different result mechanisms?
				if (_resultFlags[1] || _resultFlags[2])
				{
					//error = new DetectedError("Your function seems to use several competitive return mechanisms!",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error13_3, preReturn), ele), 13);
				}
				// END KGU#78 2015-11-25
			}
			// END KGU#343 2017-02-07
			// Check if we are inside a Parallel construct
			while (parent != null && !(parent instanceof Root) && !(parent instanceof Parallel))
			{
				parent = parent.parent;
			}
			insideParallel = (parent != null && parent instanceof Parallel);
		}
		// CHECK: Leave levels feasible (#16) 
		else if (isLeave)
		{
			int levelsUp = ele.getLevelsUp();
			List<Element> loopsToLeave = ele.getLeftStructures(null, false, true);
			if (levelsUp < 0 || loopsToLeave == null)
			{
				//error = new DetectedError("Wrong argument for this kind of JUMP (should be an integer constant)!",(Element) _node.getElement(i));
				addError(_errors, new DetectedError(errorMsg(Menu.error16_6, preLeave), ele), 16);    					    							
			}
			else {
				int levelsDown = loopsToLeave.size();
				if (levelsDown > 0 && loopsToLeave.get(levelsDown-1) instanceof Parallel) {
					insideParallel = true;
					levelsDown--;
				}
				// Compare the number of nested loops we are in with the requested jump levels
				if (levelsUp < 1 || levelsUp > levelsDown)
				{
					//error = new DetectedError("Cannot leave or break more loop levels than being nested in!",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error16_4, String.valueOf(levelsDown)), ele), 16);    								
				}
			}
		}
		// CHECK: Exit argument ok?
		else if (isExit && line.length() > preExit.length())
		{
			// START KGU 2017-04-14: Syntactical restriction loosened
			//try
			//{
			//	Integer.parseInt(line.substring(preExit.length()).trim());
			//}
			//catch (Exception ex)
			//{
			//	//error = new DetectedError("Wrong argument for this kind of JUMP (should be an integer constant)!",(Element) _node.getElement(i));
			//	addError(_errors, new DetectedError(errorMsg(Menu.error16_6, ""), ele), 16);    					    							
			//}
			String expr = line.substring(preExit.length()).trim();
			String exprType = Element.identifyExprType(this.getTypeInfo(), expr, true);
			if (!exprType.equalsIgnoreCase("int")) {
				// error = new DetectedError("Wrong argument for this kind of JUMP (should be an integer constant)!",(Element) _node.getElement(i));
				addError(_errors, new DetectedError(errorMsg(Menu.error16_8, preExit), ele), 16);    					    							
				
			}
			// END KGU 2017-04-14
		}
		
		if (insideParallel)
		{
			// error = new DetectedError("You must not directly return out of a parallel thread!",(Element) _node.getElement(i));
			addError(_errors, new DetectedError(errorMsg(Menu.error16_5, new String[]{preReturn, preLeave}), ele), 16);                                            							
		}
		
		// END KGU#78/KGU#365 2017-04-14
		
	}

	/**
	 * CHECK #16: Correct usage of return (suspecting hidden Jump)<br/>
	 * CHECK #13: Competitive return mechanisms
	 * @param ele - Instruction to be analysed
	 * @param _errors - global error list
	 * @param _index - position of this element within the owning Subqueue
	 * @param _myVars - all variables defined or modified by this element (should be empty so far, might be extended) 
	 * @param _resultFlags - 3 flags for (0) return, (1) result, and (2) function name
	 */
	private void analyse_13_16_instr(Instruction ele, Vector<DetectedError> _errors, boolean _isLastElement, StringList _myVars, boolean[] _resultFlags)
	{
		// START KGU#1023 2022-01-04: Bugfix #1026 line continuation wasn't recognised
		//StringList sl = ele.getText();
		StringList sl = ele.getUnbrokenText();
		// END KGU#1023 2022-01-04
		String preReturn =  CodeParser.getKeywordOrDefault("preReturn", "return").trim();
		String preLeave = CodeParser.getKeywordOrDefault("preLeave", "leave").trim();
		String preExit = CodeParser.getKeywordOrDefault("preExit", "exit").trim();
		String patternReturn = Matcher.quoteReplacement(CodeParser.ignoreCase ? preReturn.toLowerCase() : preReturn);
		String patternLeave = Matcher.quoteReplacement(CodeParser.ignoreCase ? preLeave.toLowerCase() : preLeave);
		String patternExit = Matcher.quoteReplacement(CodeParser.ignoreCase ? preExit.toLowerCase() : preExit);

		for (int ls=0; ls < sl.count(); ls++)
		{
			String line = sl.get(ls).trim().toLowerCase();
			// START KGU#78 2015-11-25: Make sure a potential result is following a return keyword
			//if(line.toLowerCase().indexOf("return")==0)
			if (CodeParser.ignoreCase) line = line.toLowerCase();
			boolean isReturn = line.matches(Matcher.quoteReplacement(patternReturn) + "([\\W].*|$)");
			boolean isLeave = line.matches(Matcher.quoteReplacement(patternLeave) + "([\\W].*|$)");
			boolean isExit = line.matches(Matcher.quoteReplacement(patternExit) + "([\\W].*|$)");
			boolean isJump = isLeave || isExit ||
					line.matches("exit([\\W].*|$)") ||	// Also check hard-coded keywords
					line.matches("break([\\W].*|$)");	// Also check hard-coded keywords
			if (isReturn && !line.substring(CodeParser.getKeywordOrDefault("preReturn", "return").length()).isEmpty())
				// END KGU#78 2015-11-25
			{
				_resultFlags[0] = true;
				// START KGU#343 2017-02-07: This could cause case-related warnings if e.g. "Result" is used somewhere 
				//_myVars.addIfNew("result");
				_myVars.addIfNew("§ANALYSER§RETURNS");
				// END KGU#343 2017-02-07
				// START KGU#78 2015-11-25: Different result mechanisms?
				if (_resultFlags[1] || _resultFlags[2])
				{
					//error = new DetectedError("Your function seems to use several competitive return mechanisms!",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error13_3, CodeParser.getKeywordOrDefault("preReturn", "return")), ele), 13);
				}
				// END KGU#78 2015-11-25
			}
			// START KGU#78 2015-11-25: New test (#16)
			// A return from an ordinary instruction is only accepted if it is nor nested and the
			// very last instruction of the program or routine
			if (!(ele instanceof Jump) && // Well, this is more or less clear...
					(isJump || (isReturn && !(ele.parent.parent instanceof Root &&
							ls == sl.count()-1 && _isLastElement)))
					)
			{
				if (isReturn) {
					//error = new DetectedError("A return instruction, unless at final position, must form a JUMP element!",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error16_2, preReturn), ele), 16);
				}
				else {
					//error = new DetectedError("An exit, leave, or break instruction is only allowed as JUMP element!",(Element) _node.getElement(i));
					addError(_errors, new DetectedError(errorMsg(Menu.error16_3, new String[]{preLeave, preExit}), ele), 16);
				}
			}
			// END KGU#78 2015-11-25
		}
	}

	/**
	 * CHECK #17: Inconsistency risk due to concurrent variable access by parallel threads
	 * @param ele - Parallel element to be analysed
	 * @param _errors - global error list
	 */
	private void analyse_17(Parallel ele, Vector<DetectedError> _errors)
	{
		// These hash tables will contain a binary pattern per variable name indicating
		// which threads will modify or use the respective variable. If more than
		// Integer.SIZE (supposed to be 32) parallel branches exist (pretty unlikely)
		// then analysis will just give up beyond the Integer.SIZEth thread.
		Hashtable<String,Integer> myInitVars = new Hashtable<String,Integer>();
		Hashtable<String,Integer> myUsedVars = new Hashtable<String,Integer>();
		Iterator<Subqueue> iter = ele.qs.iterator();
		int threadNo = 0;
		while (iter.hasNext() && threadNo < Integer.SIZE)
		{
			Subqueue sq = iter.next();
			// Get all variables initialised or otherwise set within the thread
			StringList threadSetVars = getVarNames(sq,false,false);
			// Get all variables used within the thread
			StringList threadUsedVars = getUsedVarNames(sq,false,false);        				
			// First register all variables being an assignment target
			for (int v = 0; v < threadSetVars.count(); v++)
			{
				String varName = threadSetVars.get(v);
				// START KGU#343 2017-02-07: Ignore pseudo-variables (markers)
				if (varName.startsWith("§ANALYSER§")) {
					continue;
				}
				// END KGU#343 2017-02-07
				Integer count = myInitVars.putIfAbsent(varName, 1 << threadNo);
				if (count != null) { myInitVars.put(varName, count.intValue() | (1 << threadNo)); }
			}
			// Then register all used variables
			for (int v = 0; v < threadUsedVars.count(); v++)
			{
				// START KGU#176 2016-04-05: Bugfix #154 Wrong collection used
				//String varName = threadSetVars.get(v);
				String varName = threadUsedVars.get(v);
				// END KGU#176 2016-04-05
				Integer count = myUsedVars.putIfAbsent(varName, 1 << threadNo);
				if (count != null) { myUsedVars.put(varName, count.intValue() | (1 << threadNo)); }
			}
			threadNo++;
		}
		// walk trough the hashtables and check for conflicts
		Enumeration<String> keys = myInitVars.keys();
		while ( keys.hasMoreElements() )
		{
			String key = keys.nextElement();
			int initPattern = myInitVars.get(key);
			// Trouble may arize if several branches access the same variable (races,
			// inconsistency). So we must report these cases.
			Integer usedPattern = myUsedVars.get(key);
			// Do other threads than those setting the variable access it?
			boolean isConflict = usedPattern != null && (usedPattern.intValue() | initPattern) != initPattern;
			// Do several threads assign values to variable key?
			if (!isConflict)
			{
				int count = 0;
				for (int bit = 0; bit < Integer.SIZE && count < 2; bit++)
				{
					if ((initPattern & 1) != 0) count++;
					initPattern >>= 1;
				}
				isConflict = count > 1;
			}
			// Do several threads access the variable assigned in some of them?
			if (!isConflict && usedPattern != null)
			{
				int count = 0;
				for (int bit = 0; bit < Integer.SIZE && count < 2; bit++)
				{
					if ((usedPattern.intValue() & 1) != 0) count++;
					usedPattern >>= 1;
				}
				isConflict = count > 1;
			}
			if (isConflict)
			{
				//error  = new DetectedError("Consistency risk due to concurrent access to variable «%» by several parallel threads!", ele);
				addError(_errors, new DetectedError(errorMsg(Menu.error17, key), ele), 17);
			}
		}
	}
	
	// END KGU 2016-03-24#
	
	// START KGU#239 2016-08-12: Enh. #23?
	/**
	 * CHECK #18: Variable names only differing in case<br/>
	 * CHECK #19: Possible name collisions with reserved words<br/>
	 * CHECK #21: Discourage use of variable names 'I', 'l', and 'O'
	 * @param _ele - the element to be checked
	 * @param _errors - the global error list
	 * @param _vars - variables definitely introduced so far
	 * @param _uncertainVars - variables detected but not certainly initialized so far
	 * @param _myVars - the variables introduced by _ele
	 */
	private void analyse_18_19_21(Element _ele, Vector<DetectedError> _errors, StringList _vars, StringList _uncertainVars, StringList _myVars)
	{
		StringList[] varSets = {_vars, _uncertainVars, _myVars};
		for (int i = 0; i < _myVars.count(); i++)
		{
			StringList collidingVars = new StringList();
			String varName = _myVars.get(i);
			// START KGU#343 2017-02-07: Ignore pseudo-variables (markers)
			if (varName.startsWith("§ANALYSER§")) {
				continue;
			}
			// END KGU#343 2017-02-07
			for (int s = 0; s < varSets.length; s++)
			{
				int j = -1;
				while ((j = varSets[s].indexOf(varName, j+1, false)) >= 0)
				{
					if (!varName.equals(varSets[s].get(j)))
					{
						collidingVars.addIfNew(varSets[s].get(j));
					}
				}
			}
			if (collidingVars.count() > 0)
			{
				String[] substitutions = {varName, collidingVars.concatenate("», «")};
				// warning "Variable name «%1» may collide with variable(s) «%2» in some case-indifferent languages!"
				addError(_errors, new DetectedError(errorMsg(Menu.error18, substitutions), _ele), 18);			
			}
			// START KGU#327 2017-01-07: Enh. #329 discourage use of 'I', 'l', and 'O'
			if (varName.equals("I") || varName.equals("l") || varName.equals("O")) {
				// warning "Variable names I (upper-case i), l (lower-case L), and O (upper-case o) are hard to distinguish from each other, 1, or 0."
				addError(_errors, new DetectedError(errorMsg(Menu.error21, ""), _ele), 21);
			}
			// END KGU#327 2017-01-07
		}
		
		if (check(19))	// This check will cost some time
		{
			for (int i = 0; i < _myVars.count(); i++)
			{
				StringList languages = new StringList();
				String varName = _myVars.get(i);
				// START KGU#343 2017-02-07: Ignore pseudo-variables (markers)
				if (varName.startsWith("§ANALYSER§")) {
					continue;
				}
				// END KGU#343 2017-02-07
				StringList languages1 = caseAwareKeywords.get(varName);
				StringList languages2 = caseUnawareKeywords.get(varName.toLowerCase());
				if (languages1 != null) languages.add(languages1);
				if (languages2 != null) languages.add(languages2);
				if (languages.count() > 0)
				{
					String[] substitutions = {varName, languages.concatenate(", ")};
					// warning "Variable name «%1» may collide with reserved names in languages like %2!"
					addError(_errors, new DetectedError(errorMsg(Menu.error19_1, substitutions), _ele), 19);								
				}
				// START KGU#239 2017-04-11
				if (structorizerKeywords.contains(varName)) {
					addError(_errors, new DetectedError(errorMsg(Menu.error19_2, varName), _ele), 19);
				}
				// END KGU#239 2017-04-11
			}
		}
	}

	// START KGU#253 2016-09-22: Enh. #249
	/**
	 * CHECK #20: Function signature check (name, parentheses)
	 * @param _errors - the global error list
	 */
	private void analyse_20(Vector<DetectedError> _errors)
	{
		StringList paramNames = new StringList();
		// START KGU#371 2019-03-07: Enh. #385 - check default value contiguousness
		//StringList paramTypes = new StringList();
		//if (this.isSubroutine() && !this.collectParameters(paramNames, paramTypes, null))
		StringList paramDefaults = new StringList();
		if (this.isSubroutine() && !this.collectParameters(paramNames, null, paramDefaults))
		// END KGU#371 2019-03-07
		{
			// warning "A subroutine header must have a (possibly empty) parameter list within parentheses."
			addError(_errors, new DetectedError(errorMsg(Menu.error20_1, ""), this), 20);								
		}
		// START KGU#836 2020-03-29: Issue #841 In case of a non-subroutine a parameter list should also be wrong
		else if (!this.isSubroutine() && this.getText().getText().indexOf("(") >= 0) {
			String key1 = "ElementNames.localizedNames." + (this.isProgram() ? 13 : 15) + ".text";
			String key2 = "ElementNames.localizedNames.14.text";
			String elName = Locales.getValue("Elements", key1, true);
			String subName = Locales.getValue("Elements", key2, true);
			addError(_errors, new DetectedError(errorMsg(Menu.error20_3, new String[] {elName, subName}), this), 20);
		}
		// END KGU#836 2020-03-29
		boolean hasDefaults = false;
		for (int i = 0; i < paramDefaults.count(); i++) {
			String deflt = paramDefaults.get(i);
			if (!hasDefaults && deflt != null) {
				hasDefaults = true;
			}
			else if (hasDefaults && deflt == null) {
				// error "Parameters with default must be placed contiguously at the parameter list end."
				addError(_errors, new DetectedError(errorMsg(Menu.error20_2, ""), this), 20);								
			}
		}
	}
	// END KGU#253 2016-09-22
    
	/**
	 * CHECK #22: constants depending on non-constants and constant modifications<br/>
	 * CHECK #24: type definitions
	 * @param _instr - Instruction element to be analysed
	 * @param _errors - global error list
	 * @param _vars - variables with certain initialisation
	 * @param _uncertainVars - variables with uncertain initialisation (e.g. in a branch)
	 * @param _definedConsts - constant definitions registered so far
	 * @param _types - type definitions (key starting with ":") and declarations so far
	 * @see #analyse_24(Element, Vector, HashMap)
	 * @see #analyse_24_tokens(Element, Vector, HashMap, StringList)
	 */
	private void analyse_22_24(Instruction _instr, Vector<DetectedError> _errors, StringList _vars, StringList _uncertainVars, HashMap<String, String> _definedConsts, HashMap<String, TypeMapEntry> _types)
	{
		StringList knownVars = _vars.copy();
		String[] keywords = CodeParser.getAllProperties();
		// START KGU#413 2017-09-17: Enh. #416 cope with user-inserted line breaks
		//for (int i = 0; i < _instr.getText().count(); i++) {
		//	String line = _instr.getText().get(i);
		StringList unbrText = _instr.getUnbrokenText();
		for (int i = 0; i < unbrText.count(); i++) {
			String line = unbrText.get(i);
		// END KGU#413 2017-09-17
			// START KGU#388 2017-09-13: Enh. #423: Type checks
			boolean isTypedef = Instruction.isTypeDefinition(line, _types);
			// END KGU#413 2017-09-13
			if (line.startsWith("const ")) {
				StringList myUsedVars = getUsedVarNames(line.substring("const ".length()), keywords);
				StringList nonConst = new StringList();
				for (int j = 0; j < myUsedVars.count(); j++)
				{
					String myUsed = myUsedVars.get(j);
					if (!knownVars.contains(myUsed) && !_uncertainVars.contains(myUsed) || !_definedConsts.containsKey(myUsed)) {
						nonConst.add(myUsed);
					}
				}
				StringList myDefs = getVarNames(StringList.getNew(line), _definedConsts);
				if (myDefs.count() > 0 && nonConst.count() > 0) {
					//error  = new DetectedError("The constant «"+myDefs.get(0)+"» depends on non-constant values: "+"!", _instr);
					addError(_errors, new DetectedError(errorMsg(Menu.error22_1, new String[]{myDefs.get(0), nonConst.concatenate("», «")}), _instr), 22);
					// It's an insecure constant, so drop it from the analysis map
					_definedConsts.remove(myDefs.get(0));
				}
				knownVars.add(myDefs);
			}
			// START KGU#388 2017-09-13: Enh. #423 - check type definitions
			else if (line.startsWith("type ") || isTypedef) {
				if (!isTypedef) {
					//error  = new DetectedError("Type definition in line"+i+"is malformed!", _instr);
					addError(_errors, new DetectedError(errorMsg(Menu.error24_1, String.valueOf(i)), _instr), 24);
				}
				else {
					StringList tokens = splitLexically(line, true);
					int posAsgnmt = tokens.indexOf("=");
					String typename = tokens.concatenate("", 1, posAsgnmt).trim();
					String typeSpec = tokens.concatenate("", posAsgnmt + 1, tokens.count()).trim();
					int posBrace = typeSpec.indexOf("{");
					// START KGU#543 2018-07-05 We have to face indirections now.
					if (posBrace >= 0) {
					// END KGU#543 2018-07-05
						StringList compNames = new StringList();
						StringList compTypes = new StringList();
						// We test here against type-associated variable names and an existing type name
						if (!Function.testIdentifier(typename, false, null) || _types.containsKey(typename) || _types.containsKey(":" + typename)) {
							//error  = new DetectedError("Type name «" + typename + "» is illegal or colliding with another identifier.", _instr);
							addError(_errors, new DetectedError(errorMsg(Menu.error24_2, typename), _instr), 24);					
						}
						// START KGU#542 2019-11-17: Enh. #739 support enum types now
						String tag = typeSpec.substring(0, posBrace).toLowerCase();
						if (tag.equals("enum")) {
							HashMap<String,String> enumDefs = this.extractEnumerationConstants(line);
							if (enumDefs == null) {
								//error  = new DetectedError("Type definition in line"+i+"is malformed!", _instr);
								addError(_errors, new DetectedError(errorMsg(Menu.error24_1, String.valueOf(i)), _instr), 24);					
							}
							else {
								for (Entry<String, String> enumItem: enumDefs.entrySet()) {
									String constName = enumItem.getKey();
									String enumValue = enumItem.getValue();
									String oldVal = _definedConsts.put(constName, enumValue);
									if (oldVal != null && !oldVal.equals(enumValue)) {
										//error  = new DetectedError("Attempt to modify the value of constant «"+varName+"»!", _instr);
										addError(_errors, new DetectedError(errorMsg(Menu.error22_2, constName), _instr), 22);						
									}
								}
							}
						}
						else {	// tag assumed to be "record" or "struct"
						// END KGU#542 2019-11-17
							extractDeclarationsFromList(typeSpec.substring(posBrace+1, typeSpec.length()-1), compNames, compTypes, null);
							for (int j = 0; j < compNames.count(); j++) {
								String compName = compNames.get(j);
								if (!Function.testIdentifier(compName, false, null) || compNames.subSequence(0, j-1).contains(compName)) {
									//error  = new DetectedError("Component name «" + compName + "» is illegal or duplicate.", _instr);
									addError(_errors, new DetectedError(errorMsg(Menu.error24_3, compName), _instr), 24);					
								}
								String type = compTypes.get(j);
								// Clear off array specifiers, but the check is still too restrictive...
								if (type != null) {
									type = type.trim();
									String typeLower;
									if (type.endsWith("]") && type.contains("[")) {
										type = type.substring(0, type.indexOf("[")).trim();
									}
									else if ((typeLower = type.toLowerCase()).startsWith("array") && typeLower.contains("of ")) {
										type = type.substring(typeLower.lastIndexOf("of ")+3).trim();
									}
									if (!TypeMapEntry.isStandardType(type) && !_types.containsKey(":" + type) && !type.equals(typename)) {
										//error  = new DetectedError("Type name «" + type + "» is illegal or unknown.", _instr);
										addError(_errors, new DetectedError(errorMsg(Menu.error24_4, type), _instr), 24);								
									}
								}
							}
						// START KGU#542 2019-11-17: Enh. #739 support enum types now
						}
						// END KGU#542 2019-11-17
					// START KGU#543 2018-07-05 - check if it is a valid type reference
					}
					else if (Function.testIdentifier(typeSpec, false, null) && !_types.containsKey(":" + typeSpec)) {
						//error  = new DetectedError("Type name «" + type + "» is illegal or unknown.", _instr);
						addError(_errors, new DetectedError(errorMsg(Menu.error24_4, typeSpec), _instr), 24);														
					}
					// END KGU#543 2018-07-05
				}
			}
			// END KGU#388 2017-09-13
			else {
				// START KGU#375 2017-04-20: Enh. #388 - Warning on attempts to redefine constants
				//knownVars.add(getVarNames(StringList.getNew(line), _definedConsts));
				Set<String> formerConstants = new HashSet<String>();
				formerConstants.addAll(_definedConsts.keySet());
				StringList myDefs = getVarNames(StringList.getNew(line), _definedConsts);
				for (int j = 0; j < myDefs.count(); j++) {
					String varName = myDefs.get(j);
					if (formerConstants.contains(varName)) {
						//error  = new DetectedError("Attempt to modify the value of constant «"+varName+"»!", _instr);
						addError(_errors, new DetectedError(errorMsg(Menu.error22_2, varName), _instr), 22);						
					}
				}
				// END KGU#375 2017-04-20
				// START KGU#388 2017-09-17: Enh. #423 Check the definition of type names and components
				if (check(24)) {
					StringList tokens = Element.splitLexically(line, true);
					//int nTokens = tokens.count();
					int posBrace = 0;
					String typeName = "";
					while ((posBrace = tokens.indexOf("{", posBrace + 1)) > 1 && Function.testIdentifier((typeName = tokens.get(posBrace-1)), false, null)) {
						TypeMapEntry recType = _types.get(":"+typeName);
						if (recType == null || !recType.isRecord()) {
							//error  = new DetectedError("There is no defined record type «"+typeName+"»!", _instr);
							addError(_errors, new DetectedError(errorMsg(Menu.error24_5, typeName), _instr), 24);												
						}
						else {
							// START KGU#559 2018-07-20: Enh. #563  more intelligent initializer evaluation
							//HashMap<String, String> components = Element.splitRecordInitializer(tokens.concatenate("", posBrace));
							HashMap<String, String> components = Element.splitRecordInitializer(tokens.concatenate("", posBrace), recType, false);
							// END KGU#559 2018-07-20
							// START KGU#1021 2021-12-05: Bugfix #1024 components may be null!
							if (components == null) {
								addError(_errors, new DetectedError(errorMsg(Menu.error24_1, Integer.toString(i+1)), _instr), 24);
							}
							else {
							// END KGU#1021 2021-12-05
								Set<String> compNames = recType.getComponentInfo(true).keySet();
								for (String compName: compNames) {
									if (!compName.startsWith("§") && !components.containsKey(compName)) {
										//error  = new DetectedError("Record component «"+compName+"» will not be modified/initialized!", _instr);
										addError(_errors, new DetectedError(errorMsg(Menu.error24_6, compName), _instr), 24);																					
									}
								}
								for (String compName: components.keySet()) {
									if (!compName.startsWith("§") && !compNames.contains(compName)) {
										//error  = new DetectedError("Record type «"+typeName+"» hasn't got a component «"+compName+"»!", _instr);
										addError(_errors, new DetectedError(errorMsg(Menu.error24_7, new String[]{typeName, compName}), _instr), 24);																					
									}
								}
							// START KGU#1021 2021-12-05: Bugfix #1024
							}
							// END KGU#1021 2021-12-05
						}
					}
					// START KGU#388 2017-09-29: Enh. #423 (KGU#514 2018-04-03: extracted for bugfix #528)
					analyse_24_tokens(_instr, _errors, _types, tokens);
					// END KGU#388 2017-09-29

				}
				// END KGU#388 2017-09-17
			}
		}
		// START KGU#388 2017-09-17: Enh. #423 record analysis support
		_instr.updateTypeMap(_types);
		// END KGU#388 2017-09-17
	}

	/**
	 * CHECK 24: does the detailed record component access analysis for the given token sequence
	 * @param _ele - the originating element
	 * @param _errors - global error list
	 * @param _types - type definitions (key starting with ":") and declarations so far
	 * @param _tokens - tokens of the current line, ideally without any instruction keywords
	 * @see #analyse_22_24(Instruction, Vector, StringList, StringList, HashMap, HashMap)
	 * @see #analyse_24(Element, Vector, HashMap)
	 */
	private void analyse_24_tokens(Element _ele, Vector<DetectedError> _errors,
			HashMap<String, TypeMapEntry> _types, StringList _tokens) {
		int nTokens = _tokens.count();
		int posDot = -1;
		String path = "";
		TypeMapEntry varType = null;
		while ((posDot = _tokens.indexOf(".", posDot + 1)) > 0 && posDot < nTokens - 1) {
			String before = _tokens.get(posDot - 1);
			// Jump in front of an index access
			// FIXME: This is just a rough heuristics producing nonsense in case of nested expressions with indices
			int posBrack = -1;
			if (before.equals("]") && (posBrack = _tokens.lastIndexOf("[", posDot)) > 0) {
				before = _tokens.get(posBrack - 1);
			}
			String after = _tokens.get(posDot+1);
			if (!Function.testIdentifier(after, false, null) || !Function.testIdentifier(before, false, null)) {
				path = "";
				varType = null;
				continue;
			}
			// START KGU#507 2018-03-15 - nonsense from expressions like OUTPUT otherDay.day, ".", otherDay.month, ".", otherDay.year 
			else if (!path.endsWith("]") && !path.endsWith(before)) {
				path = "";
				varType = null;
			}
			// END KGU#507 2018-03-15
			if ((path.isEmpty() || varType == null) && Function.testIdentifier(before, false, null)) {
				if (path.isEmpty()) {
					path = before;
				}
				varType = _types.get(path);
			}
			if (varType != null && posBrack > 0 && varType.isArray()) {
				String arrTypeStr = varType.getCanonicalType(true, false);
				if (arrTypeStr != null && arrTypeStr.startsWith("@")) {
					// Try to get the element type
					// START KGU#502 2018-02-12: Bugfix #518 - seemed inconsistent to check the field typeMap here
					//varType = typeMap.get(":" + arrTypeStr.substring(1));
					varType = _types.get(":" + arrTypeStr.substring(1));
					// END KGU#502 2018-02-12
				}
			}
			if (varType == null || !varType.isRecord() || !varType.getComponentInfo(false).containsKey(after)) {
				if (posBrack > 0) {
					path += _tokens.concatenate("", posBrack, posDot);
				}
				//error  = new DetectedError("Variable «"+varName+"» hasn't got a component «"+compName+"»!", _instr);
				addError(_errors, new DetectedError(errorMsg(Menu.error24_8, new String[]{path, after}), _ele), 24);
				varType = null;
				path += "." + after;
			}
			// START KGU#514 2018-04-03: Bugfix #528 - went wrong for e.g. rec.arr[idx].comp
			//else if (posDot + 2 < nTokens && tokens.get(posDot+2).equals(".")) {
			else if (posDot + 2 < nTokens && (_tokens.get(posDot+2).equals(".") || _tokens.get(posDot+2).equals("["))) {
			// END KGU#514 2018-04-03
				path += "." + after;
				varType = varType.getComponentInfo(false).get(after);
			}
			else {
				varType = null;
				path = "";
			}
		}
	}
	
	/**
	 * CHECK #23: Diagram includes
	 * @param _errors - global error list
	 * @param _vars - variables with certain initialisation
	 * @param _uncertainVars - variables with uncertain initialisation (e.g. in a branch)
	 * @param _constants - incremental constant definition map
	 * @param _importStack - names of imported includables
	 * @param _analysedImports - 
	 * @param _callerSet - in case of indirect cycles via routine call, the set of calling roots,
	 *        otherwise {@code null}
	 * @param _types - type definitions and declarations
	 * @see #analyse_23_inCalledRoutines(Vector, Root, StringList, HashMap, HashMap, HashSet)
	 */
	private void analyse_23(Vector<DetectedError> _errors,
			StringList _vars, StringList _uncertainVars, HashMap<String, String> _constants,
			StringList _importStack, HashMap<String, StringList> _analysedImports,
			// START KGU#946 2021-02-28: Bugfix #947
			HashSet<Root> _callerSet,
			// END KGU#946 2021-02-28
			HashMap<String, TypeMapEntry> _types)
	{
		// START KGU#376 2017-07-01: Enh. #389 - obsolete
//		Subqueue node = (Subqueue)_call.parent;
//		// Check correct placement (must be at the beginning of the diagram
//		boolean misplaced = !(node.parent instanceof Root);
//		if (!misplaced) {
//			for (int j = 0; !misplaced && j < node.getIndexOf(_call); j++) {
//				Element el0 = node.getElement(j);
//				if (!el0.isDisabled() && (!(el0 instanceof Call) || !((Call)el0).isImportCall())) {
//					misplaced = true;
//				}
//			}
//		}
//		if (misplaced) {
//			//error  = new DetectedError("Import calls () must be placed at the very beginning of the diagram!", ele);
//			String[] data = new String[]{_call.getText().getLongString(), getRoot(_call).getMethodName()};
//			addError(_errors, new DetectedError(errorMsg(Menu.error23_5, data), _call), 23);
//		}
//		String name = (_call).getSignatureString();
		if (this.includeList == null) {
			// START KGU#946 2021-02-28: Bugfix #947 drill down if we follow a call chain...
			if (check(23) &&
					(_callerSet != null && !_callerSet.contains(this) /* in this case Arranger must have an instance */
					|| (this.isInclude() || this.isSubroutine()) && Arranger.hasInstance() && !this.collectCalls().isEmpty())) {
				if (this.isInclude()) {
					String name = this.getMethodName();
					if(_importStack.contains(name)) {
						//error  = new DetectedError("Import of diagram «%1» is recursive! (Recursion path: %1 <- %2)");
						addError(_errors, new DetectedError(errorMsg(Menu.error23_2, name), this), 23);
						return;
					}
					_importStack.add(name);
				}
				if (_callerSet == null) {
					_callerSet = new HashSet<Root>();
				}
				this.analyse_23_inCalledRoutines(_errors, this, _importStack, _analysedImports, _types, _callerSet);
			}
			// END KGU#946 2021-02-28
			return;
		}
		for (int i = 0; i < includeList.count(); i++) {
			String name = includeList.get(i);
			int count = 0;	// Number of matching routines
			if (Arranger.hasInstance()) {
				count = Arranger.getInstance().findIncludesByName(name, this, false).size();
			}
			if (count == 0) {
				//error  = new DetectedError("An includable diagram «<diagram_name>» is currently not available.", this);
				addError(_errors, new DetectedError(errorMsg(Menu.error23_5, name), this), 23);
			}
			else if (count > 1) {
				//error  = new DetectedError("There are several diagrams matching signature «<diagram_name>».", this);
				addError(_errors, new DetectedError(errorMsg(Menu.error23_6, name), this), 23);					
			}
		// END KGU#376 2017-07-01
			// Is this Root cyclically included?
			if (this.isInclude() && name.equals(this.getMethodName())
					|| _importStack.contains(name)) {
				//error  = new DetectedError("Import of diagram «%1» is recursive! (Recursion path: %1 <- %2)");
				addError(_errors, new DetectedError(errorMsg(Menu.error23_2, name), this), 23);    				

			}
			else if (_analysedImports.containsKey(name)) {
				//error  = new DetectedError("Import of diagram «%1» will be ignored here because it had already been imported: %2");
				StringList path = _analysedImports.get(name);
				addError(_errors, new DetectedError(errorMsg(Menu.error23_3, new String[]{name, path.concatenate("<-")}), this), 23);    									
			}
			else if (Arranger.hasInstance()) {
				Vector<Root> roots = Arranger.getInstance().findIncludesByName(name, this, false);
				if (roots.size() == 1) {
					Root importedRoot = roots.get(0);
					Vector<DetectedError> impErrors = new Vector<DetectedError>();
					boolean[] subResultFlags = new boolean[]{false, false, false};
					// We are not interested in internal errors but in the imported variables and constants
					// START KGU#376 2017-04-20: Enh. #389 - new semantic approach: no access to this context
					//analyse(roots.get(0).children, new Vector<DetectedError>(), _vars, _uncertainVars, _constants, subResultFlags);
					StringList importedVars = new StringList();
					StringList importedUncVars = new StringList();
					// START KGU#388 2017-09-17: Enh. #423
					HashMap<String, TypeMapEntry> importedTypes = new HashMap<String, TypeMapEntry>(); 
					// END KGU#388 2017-09-17
					if (this.isInclude()) {
						_importStack.add(this.getMethodName());
					}
					importedRoot.analyse_23(impErrors, importedVars, importedUncVars, _constants,
							_importStack, _analysedImports, _callerSet, importedTypes);
					// START KGU#946 2021-02-28: Bugfix #947: Check cyclic inclusions via Calls
					//analyse(importedRoot.children, impErrors, importedVars, importedUncVars, _constants, subResultFlags, importedTypes);
					/* The identification of an inclusion loop via Calls is to be done with care,
					 * lest we should pay the benefit bitterly with performance degrading and even
					 * getting caught in an eternal recursion here!
					 */
					HashSet<Root> callers = _callerSet;
					if (callers == null) {
						/* Don't analyse this Includable fully in case of an indirect call (then
						 * only cyclic inclusion is to be tested)!
						 */
						analyse(importedRoot.children, impErrors, importedVars, importedUncVars, _constants,
								subResultFlags, importedTypes);
						callers = new HashSet<Root>();
					}
					if (check(23)) {
						analyse_23_inCalledRoutines(_errors, importedRoot, _importStack, _analysedImports, _types, callers);
					}
					// END KGU#946 2021-02-028
					_analysedImports.put(name, _importStack.copy());
					if (this.isInclude()) {
						_importStack.remove(_importStack.count()-1);
					}
					// END KGU#376 2017-04-20
					if (subResultFlags[0]) {
						//error  = new DetectedError("Diagram «%» is rather unsuited to be included as it makes use of return.",(Element) _node.getElement(i));
						addError(_errors, new DetectedError(errorMsg(Menu.error23_1, name), this), 23);
					}
					// Now associate all sub-analysis results with the Call element
					for (DetectedError err: impErrors) {
						// Unfortunately the error object doesn't know its category, so we relaunch it under category 23
						addError(_errors, new DetectedError(name + ": " + err.getMessage(), this), 23);
					}
					// Add analysis for name conflicts and uncertain variables - might still occur among includes!
					// START KGU#388 2017-09-17: Enh. #423
					for (String key: importedTypes.keySet()) {
						if (key.startsWith(":") && _types.containsKey(key)) {
							//error  = new DetectedError("There is a name conflict between local and imported type definition «%»!",(Element) _node.getElement(i));
							addError(_errors, new DetectedError(errorMsg(Menu.error23_7, key.substring(1)), this), 23);
						}
						else {
							_types.put(key, importedTypes.get(key));
						}
					}
					// END KGU#388 2017-09-17
					for (int j = 0; j < importedVars.count(); j++) {
						String varName = importedVars.get(j);
						if (!_vars.addIfNew(varName) || _uncertainVars.contains(varName)) {
							//error  = new DetectedError("There is a name conflict between local and imported variable «%»!",(Element) _node.getElement(i));
							addError(_errors, new DetectedError(errorMsg(Menu.error23_4, varName), this), 23);
						}
					}
					for (int j = 0; j < importedUncVars.count(); j++) {
						String varName = importedUncVars.get(j);
						//error  = new DetectedError("The variable «%1» may not have been initialized%2!",(Element) _node.getElement(i));
						addError(_errors, new DetectedError(errorMsg(Menu.error03_2, new String[]{varName, ""}), this), 3);    						    					
						if (_vars.contains(varName) || !_uncertainVars.addIfNew(varName)) {
							//error  = new DetectedError("There is a name conflict between local and imported variable «%»!",(Element) _node.getElement(i));
							addError(_errors, new DetectedError(errorMsg(Menu.error23_4, varName), this), 23);
						}
					}
					for (Entry<String, String> constEntry: importedRoot.constants.entrySet()) {
						if (!_constants.containsKey(constEntry.getKey())) {
							_constants.put(constEntry.getKey(), constEntry.getValue());
							if (!this.constants.containsKey(constEntry.getKey())) {
								this.constants.put(constEntry.getKey(), constEntry.getValue());
							}
						}
					}
				}
			}
		// START KGU#376 2017-07-01
		}
		// END KGU#376 2017-07-01

	}
	/**
	 * CHECK #23: Helper method to check indirect cyclic includes (via subroutine calls)
	 * for {@link #analyse_23(Vector, StringList, StringList, HashMap, StringList, HashMap, HashSet, HashMap)}
	 * @param _errors - global error list
	 * @param _root - the calling {@link Root}
	 * @param _importStack - names of already imported includables
	 * @param _analysedImports - map from Includables to the corresponding _importStack
	 * @param _callerSet - the set of calling roots to avoid eternal recursion
	 * @param _types - type definitions and declarations
	 * @param _callers - set of direct or indirect callers of routine {@code _root}
	 * @see #analyse_23(Vector, StringList, StringList, HashMap, StringList, HashMap, HashSet, HashMap)
	 */
	private void analyse_23_inCalledRoutines(Vector<DetectedError> _errors, Root _root, StringList _importStack,
			HashMap<String, StringList> _analysedImports, HashMap<String, TypeMapEntry> _types,
			HashSet<Root> _callers) {
		// If _root is an includable then it would be detected via the _importStack in case of a cycle
		if (!_root.isInclude()) {
			_callers.add(_root);
		}
		for (Call call: _root.collectCalls()) {
			Function called = call.getCalledRoutine();
			if (called != null && called.isFunction()) {
				Vector<Root> callCands = Arranger.getInstance().findRoutinesBySignature(
						called.getName(), called.paramCount(), _root, true);
				// Should we check all candidates?
				if (callCands.size() == 1 && !_callers.contains(callCands.get(0))) {
					// Don't let the variables etc. have an impact here
					callCands.get(0).analyse_23(_errors, new StringList(), new StringList(), new HashMap<String, String>(),
							_importStack, _analysedImports, _callers, _types);
				}
			}
		}
	}
	
	// START KGU#514 2018-04-03: Bugfix #528
	/**
	 * CHECK #24: correct record access
	 * @param _ele - element to be analysed
	 * @param _errors - global error list
	 * @param _types - type definitions (key starting with ":") and declarations so far
	 */
	private void analyse_24(Element _ele, Vector<DetectedError> _errors, HashMap<String, TypeMapEntry> _types)
	{
		StringList unbrText = _ele.getUnbrokenText();
		for (int i = 0; i < unbrText.count(); i++) {
			StringList tokens = Element.splitLexically(unbrText.get(i), true);
			Element.cutOutRedundantMarkers(tokens);
			analyse_24_tokens(_ele, _errors, _types, tokens);
		}
	}
	// END KGU#514 2018-04-03
	
	// START KGU#758 2019-11-08: Enh. #770 - check CASE elements
	/**
	 * CHECK 27: CASE selectors be integer constants<br/>
	 * CHECK 28: duplicate CASE selectors
	 * @param _case -  Case element to be analysed
	 * @param _errors - global error list
	 */
	private void analyse_27_28(Case _case, Vector<DetectedError> _errors)
	{
		HashSet<String> selectors = new HashSet<String>();
		HashSet<Integer> values = new HashSet<Integer>();
		StringList text = _case.getUnbrokenText();
		StringList duplicates = new StringList();
		String aNonNumber = null;
		// cf. checkValues
		for (int i = 1; i < text.count(); i++) {
			StringList items = Element.splitExpressionList(text.get(i), ",");
			for (int j = 0; j < items.count(); j++) {
				int val = 0;
				String item = items.get(j);
				// Check for duplicates (including the default label)
				if (!selectors.add(item)) {
					duplicates.addIfNew(item);
				}
				// Check for non-integers and non-characters (without the default branch label)
				if (i < text.count()-1) {
					String constVal = this.getConstValueString(item);
					if (constVal == null) {
						constVal = item;
					}
					// Check if it is not a character literal
					if (!constVal.endsWith("'") || !(constVal.startsWith("'\\") && constVal.length() > 3 || constVal.startsWith("'") && constVal.length() == 3)) {
						try {
							// START KGU#1022 2021-12-13: Bugfix #1025
							//val = Integer.parseInt(constVal);
							if (constVal.startsWith("0b")) {
								val = Integer.parseInt(constVal.substring(2), 2);
							}
							else if (constVal.startsWith("0x")) {
								val = Integer.parseInt(constVal.substring(2), 16);
							}
							else if (constVal.startsWith("0")) {
								val = Integer.parseInt(constVal, 8);
							}
							else {
								val = Integer.parseInt(constVal);
							}
							// END KGU#1022 2021-12-13
							if (!values.add(val)) {
								duplicates.addIfNew(constVal);
							}
						}
						catch (NumberFormatException ex) {
							aNonNumber = item;
						}
					}
				}
			}
		}
		if (aNonNumber != null) {
			//error  = new DetectedError("Some selector item seems not to be an integer constant.", _case);
			addError(_errors, new DetectedError(errorMsg(Menu.error27, aNonNumber), _case), 27);
		}
		if (!duplicates.isEmpty()) {
			//error  = new DetectedError("There are multiple (conflicting) selector items (%) in the CASE element!", _case);
			addError(_errors, new DetectedError(errorMsg(Menu.error28, duplicates.concatenate(", ")), _case), 28);
		}
	}
	// END KGU#758 2019-11-08
	
	// START KGU#928 2021-02-08: Enh. #928 - check for structured types
	/**
	 * CHECK 29: CASE discriminators of structured types
	 * @param _case - Case element to be analysed
	 * @param _errors - global error list
	 * @param _types - type definitions
	 */
	private void analyse_29(Case _case, Vector<DetectedError> _errors, HashMap<String, TypeMapEntry> _types)
	{
		StringList text = _case.getUnbrokenText();
		String typeStr = Root.identifyExprType(_types, text.get(0), true);
		boolean isStructured = typeStr.startsWith("@") || typeStr.startsWith("$");
		if (!isStructured && Function.testIdentifier(typeStr, false, null)) {
			// Do another test
			TypeMapEntry type = _types.get(":" + typeStr);
			isStructured = type != null && (type.isArray() || type.isRecord());
		}
		if (isStructured) {
			//error  = new DetectedError("The discriminator (%) is structured - which is unsuited for CASE!", _case);
			addError(_errors, new DetectedError(errorMsg(Menu.error29, text.get(0)), _case), 29);
		}
	}
	// END KGU#928 2021-02-08
	
	// START KGU#992 2021-10-05: Enh. #992
	/**
	 * CHECK 30: Unbalanced or badly nested parentheses, brackets etc.
	 * @param _ele - element to be analysed
	 * @param _errors - global error list
	 */
	private void analyse_30(Element _ele, Vector<DetectedError> _errors) {
		if (!check(30)) {
			return;
		}
		StringList unbrokenLines = _ele.getUnbrokenText();
		for (int i = 0; i < unbrokenLines.count(); i++) {
			StringList tokens = Element.splitLexically(unbrokenLines.get(i), true);
			Stack<Character> brackets = new Stack<Character>();
			for (int j = 0; j < tokens.count(); j++) {
				String token = tokens.get(j);
				if (token.equals("(")) {
					brackets.push(')');
				}
				else if (token.equals("[")) {
					brackets.push(']');
				}
				else if (token.equals("{")) {
					brackets.push('}');
				}
				else if (token.equals(")") || token.equals("]") || token.equals("}")) {
					try {
						char top = brackets.pop();
						if (top != token.charAt(0)) {
							//error  = new DetectedError("There is a closing '%1' where '%3' is expected in line %2!", _case);
							addError(_errors, new DetectedError(errorMsg(Menu.error30_3, new String[] {token, Integer.toString(i+1), Character.toString(top)}), _ele), 30);
						}
					}
					catch (EmptyStackException ex) {
						//error  = new DetectedError("There is at least one more closing '%1' than opening brackets in line %2!", _case);
						addError(_errors, new DetectedError(errorMsg(Menu.error30_2, new String[] {token, Integer.toString(i+1)}), _ele), 30);
					}
				}
			}
			if (!brackets.isEmpty()) {
				//error  = new DetectedError("There are %1 more opening than closing brackets in line %2, '%3' was expected next!", _ele);
				addError(_errors, new DetectedError(errorMsg(Menu.error30_1, new String[] {Integer.toString(brackets.size()), Integer.toString(i+1), Character.toString(brackets.pop())}), _ele), 30);
			}
		}
	}
	// END KGU#992 2021-10-05
	
	// START KGU#456 2017-11-06: Enh. #452
	/**
	 * Reports the active guide and performs the specified checks and steps
	 * @param _errors - global error list to be appended to
	 * @param _isNameValid - flag indicating an acceptable name, potential trigger for next step
	 */
	private void analyseGuides(Vector<DetectedError> _errors, boolean _isNameValid)
	{
		final String[] menuPath = Menu.getLocalizedMenuPath(
				new String[]{"menuPreferences", "menuPreferencesAnalyser"},
				new String[]{"Preferences", "Analyser ..."});
		int code = getCurrentTutorial();
		if (code >= 0) {
			String[] analyserCaptions = AnalyserPreferences.getCheckTabAndDescription(code);
			StringList strings = new StringList(menuPath);
			strings.add(new StringList(analyserCaptions));
			addError(_errors, new DetectedError(errorMsg(Menu.warning_2, strings.toArray()), null), 0);
			// Define the actual guide actions here 
			// START KGU#456 2017-11-04: Enh. #452 - charm initiative
			if (_isNameValid && this.children.getSize() == 0) {
				String text = null;
				switch (code) {
				case 26: // hello world tour 
					text = errorMsg(Menu.hint26[0], CodeParser.getKeywordOrDefault("output", "OUTPUT"));
					// START KGU#906 2021-01-06: Enh. #905 distinguish hints from warnings
					//addError(_errors, new DetectedError(text, this.children), 26);
					addError(_errors, new DetectedError(text, this.children, false), 26);
					// END KGU#906 2021-01-06
					break;
				case 25: // first IPO guide 
					switch (this.diagrType) {
					case DT_INCL:
						text = errorMsg(Menu.hint25_5, "");
						break;
					case DT_MAIN:
						text = errorMsg(Menu.hint25_1, new String[]{CodeParser.getKeyword("input"), (check(5) ? "X" : "x")});
						//startNextTutorial(true);
						break;
					case DT_SUB:
						text = errorMsg(Menu.hint25_4, "");
						break;
					default:
						break;
					}
					if (text != null) {
						// START KGU#906 2021-01-06: Enh. #905 distinguish hints from warnings
						//addError(_errors, new DetectedError(text, this.children), 25);
						addError(_errors, new DetectedError(text, this.children, false), 25);
						// END KGU#906 2021-01-06
					}
					break;
				}
			}
			switch (code) {
			case 25:
				guide_25(_errors);
				break;
			case 26:
				guide_26(_errors);
				break;
			}
		}
	}
	// END KGU#456 2017-11-06
	
	// START KGU#456 2017-11-04: Enh. #452 - charm initiative
	/**
	 * CHECK #25: General diagram construction hints according to the IPO paradigm
	 * @param _errors - global error list to be appended to
	 */
	private void guide_25(Vector<DetectedError> _errors)
	{
		class detectorIO implements IElementVisitor
		{
			private boolean[] m_flags;
			
			/**
			 * Fills the {@code elementFlags} (must contain at least three elements!) by traversing
			 * the diagram tree as follows.<br/>
			 * [0] true if there is at least one input instruction
			 * [1] true if there is at least one output instruction
			 * [2] true if there is any element other than input and output
			 * @param elementFlags - at least three boolean flags for input instruction, output instruction, other elements
			 */
			public detectorIO(boolean[] elementFlags)
			{
				m_flags = elementFlags;
			}

			@Override
			public boolean visitPreOrder(Element _ele) {
				if (_ele instanceof Instruction) {
					Instruction instr = (Instruction)_ele;
					if (instr.isInput()) {
						m_flags[0] = true;
					}
					else if (instr.isOutput()) {
						m_flags[1] = true;
					}
					else if (instr.isAssignment()) {
						m_flags[2] = true;
					}
				}
				// Go on unless elements of all kinds are found
				return !m_flags[0] || !m_flags[1] || !m_flags[2];
			}

			@Override
			public boolean visitPostOrder(Element _ele) {
				// Go on unless elements of all kinds are found
				return !m_flags[0] || !m_flags[1] || !m_flags[2];
			}
			
		}
		// empty body had already been handled in analyseGuides()
		if (this.isProgram() && children.getSize() > 0) {
			final boolean[] hasIO = {false, false, false};
			String varName1 = "x";
			String varName2 = "y";
			if (check(5)) {
				varName1 = varName1.toUpperCase();
				varName2 = varName2.toUpperCase();
				}
			String asgnmt = varName2 + " <- 15.5 * " + varName1 + " + 7.9";
			this.traverse(new detectorIO(hasIO));
			if (!hasIO[0]) {
				StringList menuPath = new StringList(Menu.getLocalizedMenuPath(
						new String[]{"menuDiagram","menuDiagramAdd", "menuDiagramAddBefore", "menuDiagramAddBeforeInst"},
						new String[]{"Diagram", "Add", "Before", "Instruction"}));
				addError(_errors, 
						new DetectedError(
								errorMsg(Menu.hint25_2, new String[]{CodeParser.getKeywordOrDefault("input", "INPUT"), varName1, menuPath.concatenate(" \u25BA ")}),
								// START KGU#906 2021-01-06: Enh. #905 Distinguish hints from warnings
								//this.children.getElement(0)
								this.children.getElement(0),
								false
								// END KGU#906 2021-01-06
								),
						25);
			}
			else if (!hasIO[1]) {
				StringList menuPath = new StringList(Menu.getLocalizedMenuPath(
						new String[]{"menuDiagram","menuDiagramAdd", "menuDiagramAddAfter", "menuDiagramAddAfterInst"},
						new String[]{"Diagram", "Add", "After", "Instruction"}));
				addError(_errors,
						new DetectedError(
								errorMsg(Menu.hint25_3, new String[]{CodeParser.getKeywordOrDefault("output", "OUTPUT"), varName2, menuPath.concatenate(" \u25BA ")}),
								// START KGU#906 2021-01-06: Enh. #905 Distinguish hints from warnings
								//this.children.getElement(this.children.getSize()-1)
								this.children.getElement(this.children.getSize()-1),
								false
								// END KGU#906 2021-01-06
								),
						25);
			}
			else if (!hasIO[2]) {
				StringList menuPath = new StringList(Menu.getLocalizedMenuPath(
						new String[]{"menuDiagram","menuDiagramAdd", "menuDiagramAddAfter", "menuDiagramAddAfterInst"},
						new String[]{"Diagram", "Add", "After", "Instruction"}));
				addError(_errors,
						new DetectedError(
								errorMsg(Menu.hint25_6, new String[]{asgnmt, menuPath.concatenate(" \u25BA ")}),
								// START KGU#906 2021-01-06: Enh. #905 Distinguish hints from warnings
								//this.children.getElement(0)
								this.children.getElement(0),
								false
								// END KGU#906 2021-01-06
								),
						25);
			}
			else {
				startNextTutorial(true);
			}
		}
		else if (!this.isProgram() && children.getSize() > 0) {
			startNextTutorial(true);
		}
	}
	
	/**
	 * GUIDED TOUR #26: Hello world
	 * @param _errors - global error list to be appended to
	 */
	private void guide_26(Vector<DetectedError> _errors)
	{
		final String[][] menuSpecs = {
				{"menuDebug", "menuDebugExecute"},
				{"menuFile", "menuFileSave"},
				{"menuFile", "menuFileExport", "menuFileExportCode"},
				{"menuFile", "menuFileExport", "menuFileExportPicture"}
		};
		final String[][] menuDefaults = {
				{"Debug", "Executor ..."},
				{"File", "Save"},
				{"File", "Export", "Code ..."},
				{"File", "Export", "Picture ..."}
		};
		int state = getTutorialState(26);
		if (state == 0 && advanceTutorialState(26, this)) {
			state++;
		}
		if (state > 0) {
			if (state <= menuSpecs.length) {
				String[] menuNames = Menu.getLocalizedMenuPath(menuSpecs[state-1], menuDefaults[state-1]);
				// START KGU#906 2021-01-06: Enh. #905 Distinguish hints from warnings
				//addError(_errors, new DetectedError(errorMsg(Menu.hint26[state], menuNames), this), 26);
				addError(_errors, new DetectedError(errorMsg(Menu.hint26[state], menuNames), this, false), 26);
				// END KGU#906 2021-01-06
			}
			else {
				startNextTutorial(true);
			}
		}
		
	}
	
	/**
	 * Checks whether the prerequisites for stage {@code _step} of guide {@code checkNo} are
	 * fulfilled. Will return false if not or if the check isn't active or hasn't been started.
	 * @param _checkNo - code of the tutorial guide (among the Analyser checks)
	 * @param _step - the interesting step next to be gone to
	 * @return true if step {@code state} can be gone to.
	 */
	public boolean isTutorialReadyForStep(int _checkNo, int _step)
	{
		boolean isOk = false;
		int prevState = getTutorialState(_checkNo); 
		if (prevState < 0) {
			// Not even started
			return false;
		}
		switch (_checkNo) {
		case 26:	// hello world tour 
		{
			Element elem = null;
			isOk = _step == 0 || children.getSize() == 1 && (elem = children.getElement(0)) instanceof Instruction && ((Instruction)elem).isOutput();
		}
			break;
			// default is always false
		}
		return isOk;
	}
	// END KGU#456 2017-11-04

    /**
     * Checks whether Analyser errors of category {@code errorNo} are enabled and if so adds
     * the passed-in problem entry {@code error} to the Analyser report list {@code errors}
     * @param errors - the list of element-specific or general Analyser reports
     * @param error - a report list entry with description and {@link Element} reference
     * @param errorNo - coded category of errors, warnings, or hints
     */
    private void addError(Vector<DetectedError> errors, DetectedError error, int errorNo)
    {
        // START KGU#239 2016-08-12: Enh. #231 + Code revision
        if (Root.check(errorNo))
        {
            errors.addElement(error);
        }
        // END KGU#239 2016-08-12
    }

    /**
     * Retrieves the names of the declared parameters in case this is a routine diagram,
     * otherwise an empty {@link StringList}
     * 
     * @return the list of parameter names in order of declaration (may be empty)
     * 
     * @see #getParameterTypes()
     * @see #getParameterDefaults()
     * @see #collectParameters(StringList, StringList, StringList)
     * @see #getMethodName()
     * @see #getResultType()
     */
    public StringList getParameterNames()
    {
    	// this.getVarNames();
    	// START KGU#2 2015-11-29
    	//StringList vars = getVarNames(this,true,false);
    	StringList vars = new StringList();
    	collectParameters(vars, null, null);
    	return vars;
    	// END KGU#2 2015-11-29 
    }

    // START KGU 2015-11-29
    /**
     * Retrieves the names of the types associated to the declared parameters in 
     * ase this is a routine diagram, otherwise an empty {@link StringList}.
     * 
     * @return the list of parameter type names in order of parameter declaration
     * (may be empty)
     * 
     * @see #getParameterNames()
     * @see #getParameterDefaults()
     * @see #collectParameters(StringList, StringList, StringList)
     * @see #getMethodName()
     * @see #getResultType()
     */
    public StringList getParameterTypes()
    {
    	StringList types = new StringList();
    	collectParameters(null, types, null);
    	return types;
    }
    // END KGU 2015-11-29
    
    // START KGU#371 2019-03-07: Enh. #385 - Allow parameter defaults
    /**
     * Retrieves the default value literals for the declared parameters (in case
     * this is a routine diagram with parameter defaults), otherwise an empty
     * {@link StringList}
     * 
     * @return the list of parameter default literals in order of definition.
     * <b>Note:</b> the list contains a {@code null} element where the respective
     * parameter hasn't got a default value!
     * 
     * @see #getParameterNames()
     * @see #getParameterTypes()
     * @see #collectParameters(StringList, StringList, StringList)
     * @see #getMethodName()
     * @see #getResultType()
     */
    public StringList getParameterDefaults()
    {
    	StringList defaults = new StringList();
    	collectParameters(null, null, defaults);
    	return defaults;
    }
    
    /**
     * Counts the number of mandatory parameters (i.e. parameters without default
     * value) where optional parameters are only detected as far they are either
     * the last parameter or only followed by optional parameters.
     * 
     * @return the minimum number of arguments this subroutine must obtain to be called.
     * 
     * @see #getParameterDefaults()
     * @see #acceptsArgCount(int)
     */
    public int getMinParameterCount()
    {
    	StringList params = new StringList();
    	StringList defaults = new StringList();
    	collectParameters(params, null, defaults);
    	int minParams = params.count();
    	while (minParams > 0) {
    		if (defaults.get(minParams-1) != null) {
    			minParams--;
    		}
    		else {
    			break;
    		}
    	}
    	return minParams;
    }
    
    /**
     * Checks whether a call with {@code nArgs} arguments may use this routine diagram. If so,
     * returns the number of default values needed to fill all parameters (0 in case o an exact
     * match), otherwise returns a negative number.
     * 
     * @param nArgs - the number of given argument values
     * @return number of available defaults to be used in order to satisfy all parameters, or a
     * negative number
     * 
     * @see #getMinParameterCount()
     * @see #getParameterNames()
     */
    public int acceptsArgCount(int nArgs)
    {
    	StringList params = new StringList();
    	StringList defaults = new StringList();
    	collectParameters(params, null, defaults);
    	int nDefaults = params.count() - nArgs;
    	for (int i = nArgs; nDefaults > 0 && i < defaults.count(); i++) {
    		if (defaults.get(i) != null) {
    			nDefaults--;
    		}
    		else {
    			nDefaults = -1; // leaves the loop
    		}
    	}
    	return nDefaults;
    }
    // END KGU 2019-03-07
    
    /**
     * Extracts the diagram name from the Root text. Contained blanks are replaced with underscores.
     * 
     * @return the program/subroutine name
     * 
     * @see #getMethodName(boolean)
     * @see #getQualifiedName()
     * @see #getSignatureString(boolean, boolean)
     * @see #getParameterNames()
     * @see #getParameterTypes()
     * @see #getParameterDefaults()
     * @see #getResultType()
     * @see #isProgram()
     * @see #isSubroutine()
     * @see #isInclude()
     */
    public String getMethodName()
    // START KGU#456 2017-11-04
    {
    	return getMethodName(true);
    }
    
    /**
     * Extracts the diagram name from the Root text.
     * 
     * @param _replaceBlanks - specifies whether contained blanks are to be replaced with underscores.
     * @return the program/subroutine name
     * 
     * @see #getMethodName()
     * @see #getMethodName(String, DiagramType, boolean)
     * @see #getQualifiedName(boolean)
     * @see #getSignatureString(boolean, boolean)
     * @see #getParameterNames()
     * @see #getParameterTypes()
     * @see #getParameterDefaults()
     * @see #getResultType()
     * @see #isProgram()
     * @see #isSubroutine()
     * @see #isInclude()
     */
    public String getMethodName(boolean _replaceBlanks)
    // END KGU#456 2017-11-04
    {
    	return getMethodName(this.getText().getLongString(), this.diagrType, _replaceBlanks);
    }
    
    /**
     * Extracts the diagram name from the given Root text {@code rootText}, regarding
     * the assumed {@link DiagramType} {@code rootType}.
     * 
     * @param rootText - the header of a diagram as long (unbroken) text
     * @param rootType - the assumed diagram type (main, subroutine, includable etc.)
     * @param _replaceBlanks - specifies whether contained blanks are to be replaced with underscores.
     * @return the program/subroutine name
     * 
     * @see #extractMethodParamDecls(String, StringList, StringList, StringList)
     */
    public static String getMethodName(String rootText, DiagramType rootType, boolean _replaceBlanks)
    // END KGU#456 2017-11-04
    {
    	int pos;
    	boolean isSubroutine = rootType == DiagramType.DT_SUB;

    	// START KGU#457 2017-11-05: Issue #454 We should check for Pascal-style result type in advance
    	boolean returnTypeFollows = false;
    	if ((pos = rootText.lastIndexOf(')')) > 0 && rootText.indexOf(':', pos+1) > 0) {
    		returnTypeFollows = true;
    	}
    	// END KGU#457 2017-11-05
    	if ((pos = rootText.indexOf('(')) > -1) rootText = rootText.substring(0, pos);
    	// START KGU#457 2017-11-05: Issue #454
    	if (isSubroutine && !returnTypeFollows && (pos = rootText.indexOf(']')) > 0) {
    		// This seems to be part of a return type specification
    		rootText = rootText.substring(pos+1);
    	}
    	// END KGU#457 2017-11-05
    	// Whatever this may mean here now...
    	if ((pos = rootText.indexOf('[')) > -1) rootText=rootText.substring(0,pos);
    	// Omitted argument list?
    	if ((pos = rootText.indexOf(':')) > -1) {
    		// START KGU#457 2017-11-05: Issue #454
    		if (pos < rootText.length() - 1) {
    			returnTypeFollows = true;
    		}
    		// END KGU#457 2017-11-05
    		rootText=rootText.substring(0, pos);
    	}

    	String programName = rootText.trim();

    	// START KGU#2 2015-11-25: Type-specific handling:
    	// In case of a function, the last identifier will be the name, preceding ones may be type specifiers
    	// unless the return type specification follows the argument list
    	// With a program or include, we just concatenate the strings by underscores
    	// START KGU#457 2017-11-05: Issue #454
    	//if (isSubroutine())
    	if (isSubroutine && !returnTypeFollows)
    	// END KGU#457 2017-11-05
    	{
    		String[] tokens = rootText.split(" ");
    		// It won't be that many strings, so we just go forward and keep the last acceptable one
    		for (int i = 0; i < tokens.length; i++)
    		{
    			// START KGU#61 2016-03-22: Method outsourced
    			//if (testidentifier(tokens[i]))
    			// START KGU#911 2021-01-10: Enh. #910 - DiagramController names start with "$"
    			//if (Function.testIdentifier(tokens[i], false, null))
    			if (Function.testIdentifier(tokens[i], false, null)
    					|| rootType == DiagramType.DT_INCL_DIAGRCTRL
    					&& tokens[i].startsWith("$")
    					&& Function.testIdentifier(tokens[i].substring(1), false, null))
    			// END KGU#911 2021-01-10
    			// END KGU#61 2016-03-22
    			{
    				programName = tokens[i];
    			}
    		}
    	}
    	// END KGU#2 2015-11-25
    	// START KGU 2015-10-16: Just in case...
    	// START KGU#457 2017-11-04: Issue #454
		//programName = programName.replace(' ', '_');
    	if (_replaceBlanks) {
    		programName = programName.replace(' ', '_');
    	}
    	// END KGU#457 2017-11-04
    	// END KGU 2015-10-16

    	return programName;
    }
    
    // START KGU#408 2021-02-22: Enh. #410 New convenience methods to support class import
    /**
     * Extracts the diagram name from the Root text and applies the {@link #namespace} as
     * prefix if specified. Contained blanks are replaced with underscores.
     * 
     * @return the (possibly qualified) program/subroutine name
     * 
     * @see #getMethodName()
     * @see #getQualifiedName(boolean)
     * @see #getSignatureString(boolean, boolean)
     * @see #getParameterNames()
     * @see #getParameterTypes()
     * @see #getParameterDefaults()
     * @see #getResultType()
     * @see #isProgram()
     * @see #isSubroutine()
     * @see #isInclude()
     */
    public String getQualifiedName()
    {
    	return getQualifiedName(true);
    }
    
    /**
     * Extracts the diagram name from the Root text and applies the {@link #namespace} as
     * prefix if specified.
     * @param _replaceBlanks - specifies whether contained blanks are to be replaced with underscores.
     * @return the (possibly qualified) program/subroutine name
     * @see #getMethodName(boolean)
     * @see #getQualifiedName()
     * @see #getSignatureString(boolean, boolean)
     * @see #getParameterNames()
     * @see #getParameterTypes()
     * @see #getParameterDefaults()
     * @see #getResultType()
     * @see #isProgram()
     * @see #isSubroutine()
     * @see #isInclude()
     */
    public String getQualifiedName(boolean _replaceBlanks)
    {
    	String name = getMethodName(_replaceBlanks);
    	if (namespace != null && !namespace.trim().isEmpty()) {
    		name = namespace.trim() + "." + name;
    	}
    	return name;
    }
    // END KGU#408 2021-02-22
    
    // START KGU#78 2015-11-25: Extracted from analyse() and rewritten
    /**
     * Returns a string representing a detected result type if this is a subroutine diagram.
     * 
     * @return null or a string possibly representing some data type
     * 
     * @see #getParameterNames()
     * @see #getParameterTypes()
     * @see #getParameterDefaults()
     * @see #getMethodName()
     * @see #isSubroutine()
     */
    public String getResultType()
    {
        // FIXME: This is not consistent to getMethodName()!
    	String resultType = null;
    	if (this.isSubroutine())	// KGU 2015-12-20: Types more rigorously discarded if this is a program
    	{
    		String rootText = getText().getLongString();
    		StringList tokens = Element.splitLexically(rootText, true);
    		//tokens.removeAll(" ");
    		int posOpenParenth = tokens.indexOf("(");
    		int posCloseParenth = tokens.indexOf(")");
    		int posColon = tokens.indexOf(":");
    		if (posOpenParenth >= 0 && posOpenParenth < posCloseParenth)
    		{
    			// First attempt: Something after parameter list and "as" or ":"
    			if (tokens.count() > posCloseParenth + 1) {
    				StringList right = tokens.subSequence(posCloseParenth + 1, tokens.count());
    				right.removeAll(" ");
    				if (right.count() > 0 && (right.get(0).toLowerCase().equals("as") || right.get(0).equals(":"))) {
    					// START KGU#135 2016-01-08: It was not meant to be split to several lines.
    					//resultType = tokens.getText(posCloseParenth + 2);
    					resultType = tokens.concatenate("", tokens.indexOf(right.get(0), posCloseParenth + 1)+1).trim();
    					// END KGU#135 2016-01-06
    				}
    			}
    			// Second attempt: A keyword sequence preceding the routine name
    			// START KGU#61 2016-03-22: Method outsourced
    			//else if (posOpenParenth > 1 && testidentifier(tokens.get(posOpenParenth-1)))
    			if ((resultType == null || resultType.isEmpty()) && posOpenParenth > 1) {
    				StringList left = tokens.subSequence(0, posOpenParenth);
    				left.removeAll(" ");
    				if (left.count() > 1 && Function.testIdentifier(left.get(left.count()-1), false, null))
    			// END KGU#61 2016-03-22
    				{
    					// We assume that the last token is the procedure name, the previous strings
    					// may be the type
    					resultType = left.concatenate(" ", 0, left.count()-1);
    				}	
    			}
    		}
    		else if (posColon != -1)
    		{
    			// Third attempt: In case of an omitted parenthesis, the part behind the colon may be the type 
    			resultType = tokens.concatenate(null, posColon+1).trim();
    		}
    	}
    	
    	return resultType;
    }

    /**
     * Extracts parameter names and types from the parenthesis content of the Root text
     * and adds them synchronously to {@code paramNames} and {@code paramTypes} (if not null).
     * 
     * @param paramNames - {@link StringList} to be expanded by the found parameter names
     * @param paramTypes - {@link StringList} to be expanded by the found parameter types, or null
     * @param paramDefaults - {@link StringList} to be expanded by possible default literals, or null
     * @return {@code true} iff the text contains a parameter list at all
     * 
     * @see #getParameterNames()
     * @see #getParameterTypes()
     * @see #getParameterDefaults()
     * @see #getResultType()
     * @see #getMethodName()
     * @see #getSignatureString(boolean, boolean)
     * @see #isSubroutine()
     */
    // START KGU#253 2016-09-22: Enh. #249 - Find out whether there is a parameter list
    //public void collectParameters(StringList paramNames, StringList paramTypes)
    // START KGU#371 2019-03-07: Enh. #381 - Allow default parameters
    //public boolean collectParameters(StringList paramNames, StringList paramTypes)
    public boolean collectParameters(StringList paramNames, StringList paramTypes, StringList paramDefaults)
    // END KGU#371 2019-03-07
    // END KGU#253 2016-09-22
    {
        // START KGU#253 2016-09-22: Enh. #249 - is there a parameter list?
        boolean hasParamList = false;
        // END KGU#253 2016-09-22
        if (this.isSubroutine())
        {
        	// START KGU#371 2019-03-07: Enh. #385 Use cached values if available, otherwise fill cache
        	if (parameterList != null) {
        		synchronized(this) {
        			for (Param param: parameterList) {
        				if (paramNames != null) {
        					paramNames.add(param.name);
        				}
        				if (paramTypes != null) {
        					paramTypes.add(param.type);
        				}
        				if (paramDefaults != null) {
        					paramDefaults.add(param.defaultValue);
        				}
        			}
        		}
        		// Nothing more to do here
        		// START KGU#836 2020-03-29: Bugfix #841
        		//return true;
        		String rootText = this.getText().getLongString();
        		int posPar1 = rootText.indexOf("(");
        		return posPar1 >= 0 && rootText.indexOf(")", posPar1+1) >= 0;
        		// END KGU#836 2020-03-29
        	}
        	// Compute the parameter list from scratch, make sure all three lists exist
        	if (paramNames == null) {
        		paramNames = new StringList();
        	}
        	if (paramTypes == null) {
        		paramTypes = new StringList();
        	}
        	if (paramDefaults == null) {
        		paramDefaults = new StringList();
        	}
        	// END KGU#371 2019-03-07
        	try
        	{
        		String rootText = this.getText().getText();
        		hasParamList = extractMethodParamDecls(rootText, paramNames, paramTypes, paramDefaults);
        		// START KGU#371 2019-03-07: Enh. #395 Use cached values if available, otherwise fill cache
        		parameterList = new ArrayList<Param>(paramNames.count());
        		synchronized(this) {
        			for (int i = 0; i < paramNames.count(); i++) {
        				parameterList.add(new Param(paramNames.get(i), paramTypes.get(i), paramDefaults.get(i)));
        			}
        		}
        		// END KGU#371 2019-03-07
        	}
        	catch (Exception ex)
        	{
        		logger.logp(Level.WARNING, getClass().getName(), "collectParameters", ex.getMessage());
        	}
        }
        // START KGU#253 2016-09-22: Enh. #249 - is there a parameter list?
        return hasParamList;
        // START KGU#253 2016-09-22
    }
    // END KGU#78 2015-11-25

	// START KGU#408 2021-02-26: Enh. #410 Facilitate the task for Calls representing method declarations
	/**
	 * Extracts the parameter declarations from the given routine declaration text
	 * {@code rootText} and puts them to the passed {@link StringList}s as fa as not
	 * being {@code null}.
	 * 
	 * @param rootText - the unbroken text of the method declaration
	 * @param paramNames - list to add the names of the parameters (in order of occurrence),
	 *        or {@code null}
	 * @param paramTypes - list to add the types of the parameters (in order of occurrence),
	 *        or {@code null}
	 * @param paramDefaults - list to add the literals of the parameter defaults (in oder of
	 *        occurrence where some elements may be {@code null}), or {@code null}
	 * @return {@code true} iff the text contains a parameter list at all
	 * 
	 * @see #getMethodName(String, DiagramType, boolean)
	 */
	protected static boolean extractMethodParamDecls(String rootText, StringList paramNames, StringList paramTypes,
			StringList paramDefaults) {
		boolean hasParamList = false;
		// START KGU#580 2018-09-24: Bugfix #605 we must not mutilate identifiers ending with "var".
		//rootText = rootText.replace("var ", "");
		Matcher varMatcher = VAR_PATTERN.matcher(rootText);
		if (varMatcher.matches()) {
			rootText = varMatcher.replaceAll("$1$2");
		}
		// END KGU#580
		if (rootText.indexOf("(") >= 0)
		{
			// FIXME: This is getting too simple now!
			rootText = rootText.substring(rootText.indexOf("(")+1).trim();
			rootText = rootText.substring(0,rootText.lastIndexOf(")")).trim();
			// START KGU#253 2016-09-22: Enh. #249 - seems to be a parameter list
			hasParamList = true;
			// END KGU#253 2016-09-22
		}
		// START KGU#222 2016-07-28: If there is no parenthesis then we shouldn't add anything...
		else
		{
			rootText = "";
		}
		// END KGU#222 2016-07-28

		extractDeclarationsFromList(rootText, paramNames, paramTypes, paramDefaults);
		return hasParamList;
	}
	// END KGU#408 2021-02-26
    
    // START KGU#305 2016-12-12: Enh. #305 - representaton fo a Root list
    /**
     * Returns a string of the form &lt;method_name&gt;[(&lt;n_args&gt;)][: &lt;file_path&gt;].
     * The parenthesized argument number (&lt;n_args&gt;) is only included if this is not a program,
     * the file path appendix is only added if _addPath is true
     * 
     * @param _addPath - whether or not the file path is to be aded t the signature string
     * @param _qualified - if {@code true} and the {@link #namespace} attribute is set then
     * the qualified name ({@code <namespace>.<name>}) is used in the result.
     * @return the composed string
     * 
     * @see #getMethodName()
     * @see #getParameterNames()
     * @see #getParameterTypes()
     * @see #getParameterDefaults()
     * @see #getResultType()
     * @see #getMinParameterCount()
     */
    public String getSignatureString(boolean _addPath, boolean _qualified) {
    	String presentation = this.getMethodName();
    	// START KGU#408 2021-02-22: Enh. #410 consider possible qualification
    	if (_qualified && namespace != null && !namespace.isEmpty()) {
    		presentation = namespace + "." + presentation;
    	}
    	// END KGU#408 2021-02-22
    	if (this.isSubroutine()) {
    		// START KGU#371 2019-03-07: Enh. #385: Default parameter values supported
    		//presentation += "(" + this.getParameterNames().count() + ")";
    		int maxArgs = this.getParameterNames().count();
    		int minArgs = this.getMinParameterCount();
    		presentation += "(" + ((minArgs < maxArgs) ? minArgs + "-" : "" ) + maxArgs + ")";
    		// END KGU#371 2019-03-07
    	}
    	if (_addPath) {
    		// START KGU 2016-12-29: Show changed status
    		if (this.hasChanged()) {
    			presentation = "*" + presentation;
    		}
    		// END KGU 2016-12-29
    		presentation += ": " + this.getPath();
    	}
    	return presentation;
    }
    // END KGU#305 2016-12-12
	
    // START KGU#205 2016-07-19: Enh. #192 The proposed file name of subroutines should contain the argument number
    /**
     * Returns a String composed of the diagram name (actually the routine name)
     * and (if the diagram is a function diagram) the number of arguments, separated
     * by a hyphen, e.g. if the diagram header is DEMO and the type is program then
     * the result will also be "DEMO". If the diagram is a function diagram, however,
     * and the text contains "func(x, name)" or "int func(double x, String name)" or
     * "func(x: REAL; name: STRING): INTEGER" then the result would be "func-2".
     * 
     * @return a file base name (i.e. without path and extension)
     * 
     * @see #getSignatureString(boolean, boolean)
     * @see #getMethodName()
     */
    public String proposeFileName()
    {
    	String fname = this.getMethodName();
    	// START KGU#690 2019-03-21: Issue #707 - signature suffix ought to be configurable
    	//if (this.isSubroutine())
    	if (E_FILENAME_WITH_ARGNUMBERS && this.isSubroutine())
    	// END KGU#690 2019-03-21
    	{
    		// START KGU#371 2019-03-07: Enh. #385 argument count range
    		//fname += "-" + this.getParameterNames().count();
    		int minArgs = this.getMinParameterCount();
    		int maxArgs = this.getParameterNames().count();
    		// START KGU#690 2019-03-21: Issue #707 - signature suffix ought to be configurable
    		//fname += "-" + minArgs;
    		fname += Character.toString(E_FILENAME_SIG_SEPARATOR) + minArgs;
    		// END KGU#690 2019-03-21
    		if (maxArgs > minArgs) {
    			// START KGU#690 2019-03-21: Issue #707 - signature suffix ought to be configurable
    			//fname += "-" + maxArgs;
    			fname += Character.toString(E_FILENAME_SIG_SEPARATOR) + maxArgs;
    			// END KGU#690 2019-03-21
    		}
    		// END KGU#371 2019-03-07
    	}
    	return fname;
    }
    // END KGU#205 2016-07-19
    
    /**
     * Main Analyser method - controls and performs all static analysis for this diagram.
     * 
     * @return a vector of detected errors
     * 
     * @see #check(int)
     * @see #check(String)
     */
    public Vector<DetectedError> analyse()
    {
        structorizerKeywords.clear();
        structorizerKeywords.add("global");
        // START KGU#920 2021-02-04: Enh. #920 Infinity is now a literal
        structorizerKeywords.add("Infinity");
        structorizerKeywords.add("true");
        structorizerKeywords.add("false");
        // END KGU#920 2021-02-04
        for (String keyword: CodeParser.getAllProperties()) {
            structorizerKeywords.add(keyword);
        }
        // START KGU#1012 2021-11-14: Enh. #967 plugin-specific syntax checks
        if (pluginSyntaxCheckers == null && !pluginChecks.isEmpty()) {
            pluginSyntaxCheckers = new HashMap<String, GeneratorSyntaxChecker>();
            for (String checkSpec: pluginChecks.keySet()) {
                String[] parts = checkSpec.split(":");
                if (parts.length >= 1) {
                    try {
                        Class<?> chkClass = Class.forName(parts[0]);
                        GeneratorSyntaxChecker checker = 
                                (GeneratorSyntaxChecker) chkClass.getDeclaredConstructor().newInstance();
                        pluginSyntaxCheckers.put(checkSpec, checker);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    };
                }
            }
        }
        // END KGU#1012 2021-11-14

        this.retrieveVarNames();	// also fills this.constants if not already done
        //System.out.println(this.variables);

        Vector<DetectedError> errors = new Vector<DetectedError>();
        // Retrieve the parameter names (in the effect)
//        StringList vars = getVarNames(this,true,false);
//        rootVars = vars.copy();
        rootVars = getVarNames(this, true, false);
        StringList vars = new StringList();
        StringList uncertainVars = new StringList();
//        HashMap<String, String> definedConsts = new LinkedHashMap<String, String>();
//        for (int v = 0; v < vars.count(); v++) {
//        	String para = vars.get(v);
//        	if (this.constants.containsKey(para)) {
//        		definedConsts.put(para, this.constants.get(para));
//        	}
//        }
        // START KGU#388 2017-09-17: Enh. #423
        HashMap<String, TypeMapEntry> typeDefinitions = new HashMap<String, TypeMapEntry>(); 
        // END KGU#388 2017-09-17

        // START KGU#456 2017-11-04: For anaysis purposes we shouldn't mend spaces
        String programName = getMethodName(false);
        // END KGU#456 2017-11-04

        DetectedError error;

        // START KGU#220 2016-07-27: Enh. #207
        // Warn in case of switched text/comments as first report
        if (isSwitchTextCommentMode())
        {
            String[] menuPath = {"menuDiagram", "menuDiagramSwitchComments"};
            String[] defaultNames = {"Diagram", "Switch text/comments?"};
            // This is a general warning without associated element - put at top
            error = new DetectedError(errorMsg(Menu.warning_1, Menu.getLocalizedMenuPath(menuPath, defaultNames)), null);
            // Category 0 is not restricted to configuration (cannot be switched off)
            addError(errors, error, 0);
        }
        // END KGU#220 2016-07-27
        
        // START KGU#376 2017-04-20: Enh. #389 - alternative implementation approach
//		for (String rootName: this.importedRoots) {
//			// Get all lines of the imported root
//			if (Arranger.hasInstance()) {
//				Vector<Root> roots = Arranger.getInstance().findProgramsByName(rootName);
//				if (roots.size() == 1) {
//					boolean[] subResultFlags = new boolean[]{false, false, false};
//					// We are not interested in internal errors but in the imported variables and constants
//	    			analyse(roots.get(0).children, new Vector<DetectedError>(), vars, uncertainVars, constants, subResultFlags);
//				}
//				else {
//					// The diagram «%» to be imported is currently not available.
//					addError(errors, new DetectedError(errorMsg(Menu.error15_4, rootName), this), 15);					
//				}
//			}		
//		}
        // END KGU#376 2017-04-11


        // START KGU#376 2017-07-01: Enh. #389 - Now includes are a Root property (again)
        LinkedHashMap<String, String> importedConstants = new LinkedHashMap<String, String>();
        this.analyse_23(errors, vars, uncertainVars, importedConstants, new StringList(), new HashMap<String,StringList>(), null, typeDefinitions);
        // END KGU#376 2017-07-01

        vars.add(rootVars);
        HashMap<String, String> definedConsts = new LinkedHashMap<String, String>();
        for (int v = 0; v < vars.count(); v++) {
            String para = vars.get(v);
            if (this.constants.containsKey(para)) {
                definedConsts.put(para, this.constants.get(para));
            }
        }

        // START KGU#239 2016-08-12: Enh. #231 - prepare variable name collision check
        // CHECK 19: identifier collision with reserved words
        if (check(19) && (caseAwareKeywords == null || caseUnawareKeywords == null))
        {
            initialiseKeyTables();
        }
        // END KGU#239 2016-08-12

        // CHECK: upper-case for program name (#6)
        if (!programName.toUpperCase().equals(programName))
        {
            //error  = new DetectedError("The programname «"+programName+"» must be written in uppercase!",this);
            error  = new DetectedError(errorMsg(Menu.error06,programName),this);
            addError(errors,error,6);
        }

        // CHECK: correct identifier for program name (#7)
        // START KGU#61 2016-03-22: Method outsourced
        //if(testidentifier(programName)==false)
        boolean hasValidName = true;
        // START KGU#911 2021-01-10: Enh. 910 Tolerate a name starting with '$' for a DiagramController
        //if (!Function.testIdentifier(programName, false, null))
        if (!Function.testIdentifier(programName, false, null)
            && !(this.isRepresentingDiagramController()
                    && programName.startsWith("$")
                    && Function.testIdentifier(getMethodName(true), false, null)))
        // END KGU#61 2016-03-22
        {
            //error  = new DetectedError("«"+programName+"» is not a valid name for a program or function!",this);
            // START KGU#456/KGU#457 2017-11-04: Enh. #452, #454 - charm initiative
            //error  = new DetectedError(errorMsg(Menu.error07_1,programName),this);
            if (programName.equals("???") && this.children.getSize() == 0) {
                // "What is your algorithm to do? Replace «???» with a good name for it!"
                error  = new DetectedError(errorMsg(Menu.hint07_1, programName), this);
            }
            else if (programName.contains(" ") && Function.testIdentifier(programName.replace(' ', '_'), false, null)) {
                // "Program names should not contain spaces, better place underscores between the words:"
                error  = new DetectedError(errorMsg(Menu.error07_4, programName.replace(' ', '_')), this);
            }
            else {
                // "«"+programName+"» is not a valid name for a program or function!"
                error  = new DetectedError(errorMsg(Menu.error07_1, programName), this);
            }
            hasValidName = false;
            // END KGU#456/KGU#457 2017-11-04
            addError(errors, error, 7);
        }
        // START KGU#877 2020-10-16: Issue #874 tolerate non-ascii letters, but warn
        else if (!Function.testIdentifier(programName, true, null)) {
            // Identifier "«"+programName+"» contains non-ascii letters; this should be avoided."
            addError(errors, new DetectedError(errorMsg(Menu.error07_5, programName), this), 7);
        }
        // END KGU#877 2020-10-16
        
        // START KGU#456 2017-11-04: Enh. #452 - charm initiative
        analyseGuides(errors, hasValidName);
        // END KGU#456 2017-11-04

        // START KGU#253 2016-09-22: Enh. #249: subroutine header syntax
        // CHECK: subroutine header syntax (#20 - new!)
        analyse_20(errors);
        // END KGU#253 2016-09-22
        
        // START KGU#388 2017-09-1: Enh. #423
        this.updateTypeMap(typeDefinitions);
        // END KGU#388 2017-09-17

        // START KGU#239 2016-08-12: Enh. #231: Test for name collisions
        // If check 23 is enabled then the below check will already have produced check 19 results for
        // imported variables, otherwise these will have been suppressed, so we check all imported variables, too
       	analyse_18_19_21(this, errors, vars, new StringList(), (check(23) ? rootVars : vars));
        // END KGU#239 2016-08-12

        // CHECK: two checks in one loop: (#12 - new!) & (#7)
        for (int j=0; j < rootVars.count(); j++)
        {
            String para = rootVars.get(j);
            // CHECK: non-conform parameter name (#12 - new!)
            if( !(para.charAt(0)=='p' && para.substring(1).toUpperCase().equals(para.substring(1))) )
            {
                //error  = new DetectedError("The parameter «"+vars.get(j)+"» must start with the letter \"p\" followed by only uppercase letters!",this);
                error  = new DetectedError(errorMsg(Menu.error12,para),this);
                addError(errors,error,12);
            }

            // CHECK: correct identifiers (#7)
            // START KGU#61 2016-03-22: Method outsourced
            //if(testidentifier(vars.get(j))==false)
            if (!Function.testIdentifier(para, false, null))
            // END KGU#61 2016-03-22
            {
                //error  = new DetectedError("«"+vars.get(j)+"» is not a valid name for a parameter!",this);
                error = new DetectedError(errorMsg(Menu.error07_2, para), this);
                addError(errors, error, 7);
            }
            // START KGU#877 2020-10-16: Issue #874 tolerate non-ascii lettes but warn
            if (!Function.testIdentifier(para, true, null))
            {
                //error  = new DetectedError("Identifier «"+para+"» contains non-asscii letters; this should be avoided.", this);
                addError(errors, new DetectedError(errorMsg(Menu.error07_5, para), this), 7);
            }
        }


        // CHECK: the content of the diagram
        boolean[] resultFlags = {false, false, false};
        analyse(this.children, errors, vars, uncertainVars, definedConsts, resultFlags, typeDefinitions);

        // Test if we have a function (return value) or not
        // START KGU#78 2015-11-25: Delegated to a more general function
        //String first = this.getText().get(0).trim();
        //boolean isFunction = first.toLowerCase().contains(") as ") || first.contains(") :") || first.contains("):");
        boolean isFunction = getResultType() != null;
        // END KGU#78 2015-11-25

        // CHECK: var = programname (#9)
        if (!isFunction && getCachedVarNames().contains(programName))
        {
            //error  = new DetectedError("Your program («"+programName+"») may not have the same name as a variable!",this);
            error  = new DetectedError(errorMsg(Menu.error09,programName),this);
            addError(errors,error,9);
        }

        // CHECK: sub does not return any result (#13 - new!)
        // pre-requirement: we have a sub that returns something ...  FUNCTIONNAME () <return type>
        // check to see if
        // _ EITHER _
        // the name of the sub (proposed filename) is contained in the names of the assigned variables
        // _ OR _
        // the list of initialized variables contains one of "RESULT", "Result", or "Result"
        // _ OR _
        // every path through the algorithm ends with a return instruction (with expression!)
        if (isFunction)
        {
            // START KGU#78 2015-11-25: Let's first gather all necessary information
            boolean setsResultCi = vars.contains("result", false);
//            boolean setsResultLc = false, setsResultUc = false, setsResultWc = false;
//            if (setsResultCi)
//            {
//                setsResultLc = vars.contains("result", true);
//                setsResultUc = vars.contains("RESULT", true);
//                setsResultWc = vars.contains("Result", true);
//            }
            // START KGU#991 2021-10-03: Issue #991 case-aware check needed.
            //boolean setsProcNameCi = vars.contains(programName,false);	// Why case-independent?
            boolean setsProcName = vars.contains(programName);
            // END KGU#991 2021-10-03
            boolean maySetResultCi = uncertainVars.contains("result", false);
//            boolean maySetResultLc = false, maySetResultUc = false, maySetResultWc = false;
//            if (maySetResultCi)
//            {
//            	maySetResultLc = uncertainVars.contains("result", true);
//            	maySetResultUc = uncertainVars.contains("RESULT", true);
//            	maySetResultWc = uncertainVars.contains("Result", true);
//            }
            // START KGU#991 2021-10-03: Issue #991 case-aware check needed.
            //boolean maySetProcNameCi = uncertainVars.contains(programName,false);	// Why case-independent?
            boolean maySetProcName = uncertainVars.contains(programName);	// Why case-independent?
            // END KGU#991 2021-10-03
            // END KGU#78 2015-11-25
            // START KGU#343 2017-02-07: Ignore pseudo-variables (markers)
            boolean doesReturn = vars.contains("§ANALYSER§RETURNS");
            boolean mayReturn = resultFlags[0];
            // END KGU#343 2017-02-07
            // START KGU#990 2021-10-02: Bugfix #990 new flag fields to facilitate export
            returnsValue = mayReturn;
            // END KGU#990 2021-10-02
            
            // START KGU#991 2021-10-03: Issue #991 case-aware check needed.
            //if (!setsResultCi && !setsProcNameCi && !doesReturn &&
            //		!maySetResultCi && !maySetProcNameCi && !mayReturn)
            if (!setsResultCi && !setsProcName && !doesReturn &&
            		!maySetResultCi && !maySetProcName && !mayReturn)
            // END KGU#991 2021-10-03
            {
            	//error  = new DetectedError("Your function does not return any result!",this);
            	error = new DetectedError(errorMsg(Menu.error13_1,""),this);
            	addError(errors,error,13);
            }
            // START KGU#991 2021-10-03: Issue #991 case-aware check needed.
            //else if (!setsResultCi && !setsProcNameCi && !doesReturn &&
            //		(maySetResultCi || maySetProcNameCi || mayReturn))
            else if (!setsResultCi && !setsProcName && !doesReturn &&
            		(maySetResultCi || maySetProcName || mayReturn))
            // END KGU#991 2021-10-03
            {
            	//error  = new DetectedError("Your function may not return a result!",this);
            	error = new DetectedError(errorMsg(Menu.error13_2,""),this);
            	addError(errors,error,13);
            }
            // START KGU#78 2015-11-25: Check competitive approaches
            //else if (maySetResultCi && maySetProcNameCi)
            else if (maySetResultCi && maySetProcName)
            {
            	//error  = new DetectedError("Your functions seems to use several competitive return mechanisms!",this);
            	error = new DetectedError(errorMsg(Menu.error13_3,"RESULT <-> " + programName),this);
            	addError(errors,error,13);            		
            }
            // END KGU#78 2015-11-25
        }

        /*
        for(int i=0;i<errors.size();i++)
        {
            System.out.println((DetectedError) errors.get(i));
        }
        /**/

        this.errors = errors;
        return errors;
    }

	// START KGU#239 2016-08-12: Enh. #231
	/**
	 * Initializes the lookup tables for the identifier check 19 of analyser 
	 */
	private static final void initialiseKeyTables()
	{
		// Establish the primary lookup tables
		caseAwareKeywords = new Hashtable<String, StringList>();
		caseUnawareKeywords = new Hashtable<String, StringList>();
		// Now add the table entries for every generator
		for (GENPlugin plugin: Menu.generatorPlugins)
		{
			// The reserved words the generator will provide
			// START KGU#239 2017-04-23: Enh. #231 Alternatively configurable in the plugin
			//String[] reserved = null;
			//boolean distinguishCase = true;
			String[] reserved = plugin.reservedWords/**/;
			// Case relevance the generator will provide
			boolean distinguishCase = plugin.caseMatters;
			if (reserved != null)
			{
				Hashtable<String, StringList> table =
						distinguishCase ? caseAwareKeywords : caseUnawareKeywords;
				for (int i = 0; i < reserved.length; i++)
				{
					String key = reserved[i];
					if (!distinguishCase)
					{
						key = key.toLowerCase();	// normalise key for the primary lookup
					}
					// Ensure an entry in the respective primary lookup
					StringList users = table.get(key);
					if (users == null)
					{
						// First occurrence of thís key word
						users = StringList.getNew(plugin.title);
						table.put(key, users);
					}
					else
					{
						// Other generators have already exposed this keyword
						users.add(plugin.title);
					}
				}
			}
		}
		// Now buy the GUI some time to accomplish its initialisation
//		try {
//			Thread.sleep(500);
//		} catch(InterruptedException ex) {
//			System.out.println("Root.initialiseKeyTables(): sleep failed.");
//			Thread.currentThread().interrupt();
//		}
	}
	// END KGU#239 2016-06-12
	
	// START KGU#466 2019-08-02: Issue #733
	public static String[] getPreferenceKeys()
	{
		// START KGU#906 2021-01-02: Enh. #905
		//String[] prefKeys = new String[analyserChecks.length];
		// START KGU#1012 2021-11-14: Enh. #967 inclde plugin-specific keys
		//String[] prefKeys = new String[analyserChecks.length + 1];
		String[] prefKeys = new String[analyserChecks.length + 1 + pluginChecks.size()];
		int ix = analyserChecks.length + 1;
		for (String prefKey: pluginChecks.keySet()) {
			prefKeys[ix++] = prefKey;
		}
		// END KGU#1012 2021-11-14
		// END KGU#906 2021-01-02
		for (int i = 0; i < analyserChecks.length; i++) {
			prefKeys[i] = "check" + (i+1);
		}
		// START KGU#906 2021-01-02: Enh. #905
		prefKeys[analyserChecks.length] = "drawAnalyserMarks";
		// END KGU#906 2021-01-02
		return prefKeys;
	}
	// END KGU#466 2019-08-02

    public static void saveToINI()
    {
        try
        {
            Ini ini = Ini.getInstance();
            ini.load();
            // analyser (see also Mainform.loadFromIni(), Diagram.analyserNSD()) 
            // START KGU#239 2016-08-12: Enh. #231 + Code revision
            for (int i = 0; i < analyserChecks.length; i++)
            {
                ini.setProperty("check" + (i+1), (check(i+1) ? "1" : "0"));
            }
            // END KGU#239 2016-08-12
            // START KGU#1012 2021-11-12: Enh. #967 additional check category
            for (Entry<String, Boolean> entry: pluginChecks.entrySet()) {
                ini.setProperty(entry.getKey(), (entry.getValue() ? "1" : "0"));
            }
            // END KGU#1012 2021-11-12
            // START KGU#906 2021-01-02: Enh. #905
            ini.setProperty("drawAnalyserMarks", E_ANALYSER_MARKER ? "1" : "0");
            // END KGU#906 2021-01-02
            
            ini.save();
        }
        catch (Exception e)
        {
        	logger.logp(Level.SEVERE, Root.class.getName(), "saveToINI()", "", e);
        }
    }

// START KGU#91 2015-12-04: No longer needed
//  public void setSwitchTextAndComments(boolean switchTextAndComments) {
//      this.switchTextAndComments = switchTextAndComments;
//  }

	/**
	 * Returns the content of the text field unless _alwaysTrueText is false and
	 * mode isSwitchedTextAndComment is active, in which case the comment field
	 * is returned instead, if it is not empty.
	 * @param _alwaysTrueText - if true then mode isSwitchTextAndComment is ignored
	 * @return either the text or the comment
	 */
	@Override
	public StringList getText(boolean _alwaysTrueText)
	{
		StringList textToShow = super.getText(_alwaysTrueText);
		if (textToShow.getText().trim().isEmpty())
		{
			textToShow = text;
		}
		return textToShow;
	}
// END KGU#91 2015-12-04

	// START KGU#199 2016-07-07: Enh. #188 - ensure Call elements for known subroutines
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#convertToCalls(lu.fisch.utils.StringList)
	 */
	@Override
	public void convertToCalls(StringList _signatures) {
		this.children.convertToCalls(_signatures);
	}
	// END KGU#199 2016-07-07

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.elements.Element#traverse(lu.fisch.structorizer.elements.IElementVisitor)
	 */
	@Override
	public boolean traverse(IElementVisitor _visitor) {
		boolean proceed = _visitor.visitPreOrder(this);
		if (proceed)
		{
			proceed = children.traverse(_visitor);
		}
		if (proceed)
		{
			proceed = _visitor.visitPostOrder(this);
		}
		return proceed;
	}
	@Override
	protected String[] getRelevantParserKeys() {
		// There no relevant parser keys here
		return null;
	}
	
	// START KGU#365 2017-03-14: Enh. #380: Mechanism to derive subroutines from diagram-snippets
	/**
	 * Replaces the given {@code elements} from this diagram by a subroutine call and
	 * returns the new subroutine formed out of these elements.
	 * Tries to derive the necessary arguments and places them in the parameter lists
	 * of both the subroutine and the call.
	 * The heuristic retrieval of necessary results is incomplete (it still cannot detect
	 * mere updates of variables that had existed before). In case several result values
	 * are detected, then an array of them will be returned. This may cause trouble for export
	 * languages like C (where array can't be returned that easily) or strongly typed languages
	 * (where a record/struct might have been necessary instead).
	 * @param elements - an element sequence from this Root
	 * @param name - the name for the new subroutine
	 * @param result - name of the result variable (if any) or null 
	 * @return a new Root formed from the given elements or null if {@code elements} was
	 * empty or didn't belong to this Root. 
	 */
	public Root outsourceToSubroutine(IElementSequence elements, String name, String result)
	{ 
		// FIXME: The result value mechanism is the most complex and vague part here:
		// We cannot assume that a single (and possibly scalar) return value is
		// sufficient. At least the following cases have to be considered:
		// 1. Variables set within elements (and still used afterwards outside);
		// 2. Non-scalar variables passed to some subroutine call within elements;
		// 3. Array variables with element assignments to them within elements.
		// It would be fine if the caller (Diagram) could do this analysis in advance
		// and pass the committed result variable names in.
		// In any case, if the number of committed result items is larger than 1,
		// then we will have to gather them into a data structure (i.e. an array)
		// in order to return all off them. After the call, the content of the
		// returned data structure (array) will have to be scattered again.
		
		Root subroutine = null;
		int nElements = elements.getSize();
		if (nElements > 0) {
			HashMap<String, TypeMapEntry> types = this.getTypeInfo();
			// Identify uninitialized variables before the outsourcing (for comparison)
			StringList uninitializedVars0 = new StringList();
			this.getUninitializedVars(this.children, new StringList(), uninitializedVars0);
			if (Element.getRoot(elements.getSubqueue()) != this) {
				return null;
			}
			// Create the new subroutine and move the elements to it
			subroutine = new Root();
			subroutine.setProgram(false);
			for (int i = 0; i < nElements; i++) {
				subroutine.children.addElement(elements.getElement(0));
				elements.removeElement(0);
			}
			// Uninitialized variables within the subroutine are potential arguments...
			StringList args = new StringList();
			subroutine.getUninitializedVars(subroutine.children, new StringList(), args);
			// Identify uninitialized variables in root after the outsourcing
			StringList uninitializedVars1 = new StringList();
			this.getUninitializedVars(this.children, new StringList(), uninitializedVars1);
			// New uninitialized variables in root are certain results (which is not sufficient!)
			StringList results = new StringList();
			for (int i = 0; i < uninitializedVars1.count(); i++) {
				String varName = uninitializedVars1.get(i);
				if (!uninitializedVars0.contains(varName)) {
					results.add(varName);
				}
			}
			if (results.isEmpty() && result != null) {
				results.add(result);
			}
			else if (results.count() > 1 && result == null) {
				// START KGU#921 2021-01-30: Bugfix #921 Minus signs broke id syntax
				// The hexstring conversion avoids a syntax error because of negative hash code
				//result = "arr" + subroutine.hashCode();
				result = "arr" + Integer.toHexString(subroutine.hashCode());
				// END KGU#921 2021-01-30
			}
			// FIXME: There should be a hook for interactive argument reordering
			// Compose the subroutine signature
			StringList argSpecs = new StringList();
			// START KGU#921 2021-01-30: Bugfix #921 we must skip enum constants
			StringList realArgs = new StringList();
			// END KGU#921 2021-01-30
			boolean argTypesFound = false;
			for (int i = 0; i < args.count(); i++) {
				String argSpec = args.get(i);
				// START KGU#921 2021-01-30: Bugfix #921 we must skip enum constants
				// FIXME actually any primitive constants should be skipped, but their def is to be included
				String constVal = this.constants.get(argSpec);
				if (constVal != null && constVal.startsWith(":") && constVal.contains("€")) {
					continue;
				}
				realArgs.add(argSpec);
				// END KGU#921 2021-01-30
				String typeDecl = makeTypeDeclaration(argSpec, types);
				if (!typeDecl.isEmpty()) {
					argSpec += typeDecl;
					argTypesFound = true;
				}
				argSpecs.add(argSpec);
			}
			// START KGU#921 2021-01-30: Bugfix #921 we must skip enum constants
			args = realArgs;
			// END KGU#921 2021-01-30
			String signature = name + "("
					+ argSpecs.concatenate(argTypesFound ? "; " : ", ")
					+ ")";
			
			// Result composition (both within the subroutine and for the call)
			String resAsgnmt = null;
			if (results.count() == 1 && (result == null || result.equals(results.get(0)))) {
				result = results.get(0);
				signature += makeTypeDeclaration(result, types);
				if (!result.equals(name) && !result.equalsIgnoreCase("result")) {
					resAsgnmt = name + " <- " + result;
				}
			}
			else if (results.count() > 0) {
				signature += ": array";
				resAsgnmt = name + " <- {" + results.concatenate(", ") + "}";
			}
			subroutine.setText(signature);
			//subroutine.setChanged();		// This was not helpful for code import
			if (resAsgnmt != null) {
				subroutine.children.addElement(new Instruction(resAsgnmt));
			}
			Call call = new Call((result != null ? result + " <- " : "" ) + name + "(" + args.concatenate(", ") + ")");
			elements.addElement(call);
			if (results.count() > 1 || results.count() == 1 && !result.equals(results.get(0))) {
				StringList asgnmts = new StringList();
				for (int i = 0; i < results.count(); i++) {
					asgnmts.add(results.get(i) + " <- " + result + "[" + i + "]");
				}
				elements.addElement(new Instruction(asgnmts));
			}
		}
		return subroutine;
	}
	
	// Tries to derive a type association in Pascal syntax for varName
	private String makeTypeDeclaration(String varName, HashMap<String, TypeMapEntry> types) {
		String typeDecl = "";
		TypeMapEntry entry = types.get(varName);
		if (entry != null && entry.isConflictFree()) {
			String prefix = "";
			String type = entry.getTypes().get(0);
			if (!type.equals("???")) {
				// START KGU#506 2018-03-14: Issue #522
				// START KGU#921 2021-01-30: Bugfix #921
				//if (entry.isRecord()) {
				if (entry.isRecord() || entry.isEnum()) {
				// END KGU#921 2021-01-30
					type = entry.typeName;
				}
				// END KGU#506 2018-03-14
				while (type.startsWith("@")) {
					prefix += "array of ";
					type = type.substring(1);
				}
				typeDecl += ": " + prefix + type;
			}
			// FIXME: Handle record types more sensibly (how?)
			// We would rather have to replace the sequence by the type name instead
			typeDecl.replaceAll("(.*?)[$]\\w+(\\{.*?)", "$1record\\{");
		}
		return typeDecl;
	}
	/**
	 * This is practically a very lean version of the {@link #analyse(StringList)} method. We simply don't create
	 * Analyser warnings but collect variable names which are somewhere used without (unconditioned)
	 * initialization. These are candidates for parameters.
	 * @param _node - The Subqueue recursively to be scrutinized for variables
	 * @param _vars - Names of variables, which had been introduced before (will be enhanced here)
	 * @param _args - collects the names of not always initialized variables (potential arguments) 
	 */
	private void getUninitializedVars(Subqueue _node, StringList _vars, StringList _args) {
		
		for (int i=0; i<_node.getSize(); i++)
		{
			Element ele = _node.getElement(i);
			if (ele.isDisabled(true)) continue;
			
			String eleClassName = ele.getClass().getSimpleName();

			// get all set variables from actual instruction (just this level, no substructure)
			StringList myVars = getVarNames(ele);
			// Find uninitialized variables (except REPEAT)
			StringList myUsedVars = getUsedVarNames(_node.getElement(i),true,true);

			if (!eleClassName.equals("Repeat"))
			{
				for (int j=0; j<myUsedVars.count(); j++)
				{
					String myUsed = myUsedVars.get(j);
					// START KGU#343 2017-02-07: Ignore pseudo-variables (markers)
					if (myUsed.startsWith("§ANALYSER§")) {
						continue;
					}
					// END KGU#343 2017-02-07
					if (!_vars.contains(myUsed))
					{
						_args.addIfNew(myUsed);
					}
				}
			}

			// add detected vars to initialised vars
			_vars.addIfNew(myVars);

			// continue analysis for subelements
			if (ele instanceof ILoop)
			{
				getUninitializedVars(((ILoop) ele).getBody(), _vars, _args);
			}
			else if (eleClassName.equals("Parallel"))
			{
				StringList initialVars = _vars.copy();
				Iterator<Subqueue> iter = ((Parallel)ele).qs.iterator();
				while (iter.hasNext())
				{
					// For the thread, propagate only variables known before the parallel section
					StringList threadVars = initialVars.copy();
					getUninitializedVars(iter.next(), threadVars, _args);
					// Any variable introduced by one of the threads will be known after all threads have terminated
					_vars.addIfNew(threadVars);
				}
			}
			else if(eleClassName.equals("Alternative"))
			{
				StringList tVars = _vars.copy();
				StringList fVars = _vars.copy();

				getUninitializedVars(((Alternative)ele).qTrue, tVars, _args);
				getUninitializedVars(((Alternative)ele).qFalse, fVars, _args);

				for(int v = 0; v < tVars.count(); v++)
				{
					String varName = tVars.get(v);
					if (fVars.contains(varName)) { _vars.addIfNew(varName); }
				}
			}
			else if(eleClassName.equals("Case"))
			{
				Case caseEle = ((Case) ele);
				int nBranches = caseEle.qs.size();	// Number of branches
				StringList initialVars = _vars.copy();
				// An entry of this Hashtable will contain the number of the branches
				// having initialized the variable represeneted by the key - so in the
				// end we can see which variables aren't always initialized.
				Hashtable<String, Integer> myInitVars = new Hashtable<String, Integer>();
				// adapt size if there is no "default" branch
				// START KGU#1023 2022-01-04: Bugfix #1026 line continuation vulnerability
				//if ( caseEle.getText().get(caseEle.getText().count()-1).equals("%") )
				StringList lines = caseEle.getUnbrokenText();
				if ( lines.get(lines.count()-1).equals("%") )
				// END KGU#1023 2022-01-04
				{
					nBranches--;
				}
				for (int j = 0; j < nBranches; j++)
				{
					StringList caseVars = initialVars.copy();
					getUninitializedVars((Subqueue) caseEle.qs.get(j), caseVars, _args);
					for(int v = 0; v < caseVars.count(); v++)
					{
						String varName = caseVars.get(v);
						if(myInitVars.containsKey(varName))
						{
							myInitVars.put(varName, myInitVars.get(varName) + 1);
						}
						else
						{
							myInitVars.put(varName, 1);
						}
					}
					//_vars.addIfNew(caseVars);
				}
				//System.out.println(myInitVars);
				// walk trough the hash table and check
				Enumeration<String> keys = myInitVars.keys();
				//System.out.println("SI = "+si+" = "+c.text.get(c.text.count()-1));
				while ( keys.hasMoreElements() )
				{
					String key = keys.nextElement();
					int value = myInitVars.get(key);

					if(value >= nBranches)
					{
						_vars.addIfNew(key);
					}
					else
					{
						_args.addIfNew(key);
					}
				}
			}


		} // for(int i=0; i < _node.size(); i++)...
	}
	// END KGU#365 2017-03-14
	
	// START KGU#874 2020-10-19: Issue #875 a public wrapper for the method before
	/**
	 * Tries to identify all used variables that are neither imported nor initialized
	 * @param _pool - a potential routine pool for the retrieval of included Includables
	 * @return a {@link StringList} of potentially uninitialised variables
	 */
	public StringList getUninitializedVars(IRoutinePool _pool)
	{
		StringList obtainedVars = new StringList();
		StringList uninitialized = new StringList();
		if (this.includeList != null && _pool != null) {
			for (int i = 0; i < this.includeList.count(); i++) {
				Vector<Root> includes = _pool.findIncludesByName(this.includeList.get(i), this, false);
				for (Root include: includes) {
					obtainedVars.addIfNew(include.getVarNames());
				}
			}
		}
		this.getUninitializedVars(this.children, obtainedVars, uninitialized);
		return uninitialized;
	}
	// END KGU#874 2020-10-19

	// START KGU#363 2017-05-08: Enh. #372 - some statistics
	// KGU#556 2018-07-17: Issue #561 (loops differentiated, array enlarged)
	/**
	 * Retrieves the counts of contained elements per category:<br/>
	 * 0. Instructions<br/>
	 * 1. Alternatives<br/>
	 * 2. Selections<br/>
	 * 3. FOR loops<br/>
	 * 4. WHILE loops<br/>
	 * 5. REPEAT loops<br/>
	 * 6. FOREVER loops<br/>
	 * 7. Calls<br/>
	 * 8. Jumps<br/>
	 * 9. Parallel sections<br/>
	 * 10. Try blocks<br/>
	 * @return an integer array with element counts according to the index map above 
	 * @see #getElementCount()
	 */
	public Integer[] getElementCounts()
	{
		// START KGU#686 2019-03-24: Enh. #56
		//final Integer[] counts = new Integer[]{0,0,0, 0,0,0, 0,0,0, 0};
		final Integer[] counts = new Integer[]{0,0,0, 0,0,0, 0,0,0, 0,0};
		// END KGU#686 2019-03-24
		
		IElementVisitor counter = new IElementVisitor() {

			@Override
			public boolean visitPreOrder(Element _ele) {
				// Since Call and Jump extend Instruction, they must be tested first
				if (_ele instanceof Call) {
					counts[7]++;
				}
				else if (_ele instanceof Jump) {
					counts[8]++;
				}
				else if (_ele instanceof Instruction) {
					counts[0]++;
				}
				if (_ele instanceof Alternative) {
					counts[1]++;
				}
				else if (_ele instanceof Case) {
					counts[2]++;
				}
				else if (_ele instanceof For) {
					counts[3]++;
				}
				else if (_ele instanceof While) {
					counts[4]++;
				}
				else if (_ele instanceof Repeat) {
					counts[5]++;
				}
				else if (_ele instanceof Forever) {
					counts[6]++;
				}
				else if (_ele instanceof Parallel) {
					counts[9]++;
				}
				// START KGU#686 2019-03-24: Enh. #56
				else if (_ele instanceof Try) {
					counts[10]++;
				}
				// END KGU#686 2019-03-24
				return true;
			}

			@Override
			public boolean visitPostOrder(Element _ele) {
				return true;
			}
			
		};
		this.traverse(counter);
		
		return counts;
	}
	// END KGU#363 2017-05-08
	
	// START KGU#444/KGU#618 2018-12-18: Issue #417, #649 Auxiliary method to get the total element count
	/**
	 * @return the total number of elements comprised by this Root including itself
	 * (and except Subqueues). Uses {@link #getElementCounts()}.
	 * @see #getElementCounts()
	 */
	public int getElementCount()
	{
		Integer[] counts = getElementCounts();
		int nElements = 0;
		for (Integer count: counts) {
			nElements += count;
		}
		return nElements + 1;
	}
	// END KGU#444/KGU#618 2018-12-18
	
	// START KGU#324 2017-05-30: Enh. #373, #415
	/**
	 * Provides a tree iterator for forward (pre-order top-down) or backward (post-order bottom-up)
	 * traversal.
	 * @return An Iterator instance responding to hasNext(), next(), hasPrevious(), previous() method
	 */
	public IElementSequence.Iterator iterator()
	{
		return this.children.iterator(true);
	}
	// END KGU#363 2017-05-30

	// START KGU#602 2018-10-25: Issue #419 - Tools to break very long lines is requested
	/**
	 * Breaks down all text lines longer than {@code maxLineLength} of all elements
	 * along the tokens into continuated lines (i.e. broken lines end with backslash).
	 * Already placed line breaks are preserved unless {@code rebreak} is true, in which
	 * case broken lines are first concatenated in order to be broken according to
	 * {@code maxLineLength}.<br/>
	 * If a token is longer than {@code maxLineLength} (might be a string literal) then
	 * it will not be broken or decomposed in any way such that the line length limit
	 * may not always hold.<br/>
	 * If this method led to a different text layout then the drawing info is invalidated
	 * up-tree.
	 * @param maxLineLength - the number of characters a line should not exceed
	 * @param rebreak - if true then existing line breaks (end-standing backslashes) or not preserved
	 * @return true if any text was effectively modified, false otherwise
	 * @see Element#breakTextLines(int, boolean)
	 */
	public boolean breakElementTextLines(int maxLineLength, boolean rebreak)
	{
		boolean changed = false;
		IElementSequence.Iterator iter = this.iterator();
		while (iter.hasNext()) {
			if (iter.next().breakTextLines(maxLineLength, rebreak)) {
				changed = true;
			}
		}
		return changed;
	};
	
	/**
	 * Detects the maximum text line length either on this very element 
	 * @param _includeSubstructure - whether (in case of a complex element) the substructure
	 * is to be involved
	 * @return the maximum line length
	 */
	public int getMaxLineLength(boolean _includeSubstructure)
	{
		int maxLen = super.getMaxLineLength(false);
		if (_includeSubstructure) {
			maxLen = Math.max(maxLen, this.children.getMaxLineLength(true));
		}
		return maxLen;
	}
	// END KGU#602 2018-10-25
	
	// START KGU#178/KGU#624 2018-12-26: Issues #160, #655; moved hitherto from class Generator
	/**
	 * Gathers all {@code Call} elements contained in this Root and not being disabled
	 * @return the vector of all contained {@code Call} elements
	 */
	public Vector<Call> collectCalls()
	{
		return collectCalls(this);
	}
	
	/**
	 * Recursively gathers all {@code Call} elements in {@code _ele} and it's substructure
	 * unless they are disabled.
	 * @param _ele - an {@link Element}
	 * @return a vector of found {@code Call} elements
	 */
	private Vector<Call> collectCalls(Element _ele)
	{
		final class CallCollector implements IElementVisitor
		{
			public Vector<Call> calls = new Vector<Call>();
			
			@Override
			public boolean visitPreOrder(Element _ele) {
				// START KGU#632 2019-01-08: Nobody needs a disabled Call ...
				//if (_ele instanceof Call) {
				if (_ele instanceof Call && !_ele.isDisabled(false)) {
				// END KGU#632 2019-01-08
					calls.add((Call)_ele);
				}
				return true;
			}
			@Override
			public boolean visitPostOrder(Element _ele) {
				return true;
			}
		};
		CallCollector visitor = new CallCollector();
		_ele.traverse(visitor);
		return visitor.calls;
	}
	// END KGU#178/KGU#624 2018-12-26
	
}

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

package lu.fisch.structorizer.executor;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class controls the execution of a diagram.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date			Description
 *      ------			----			-----------
 *      Bob Fisch                       First Issue
 *      Kay Gürtzig     2015-10-11      Method execute() now ensures that all elements get unselected
 *      Kay Gürtzig     2015-10-13      Method step decomposed into separate subroutines, missing
 *                                      support for Forever loops and Parallel sections added;
 *                                      delay mechanism reorganised in order to integrate breakpoint
 *                                      handling in a sound way
 *      Kay Gürtzig     2015-10-15      stepParallel() revised (see comment)
 *      Kay Gürtzig     2015-10-17/18   First preparations for a subroutine retrieval via Arranger
 *      Kay Gürtzig     2015-10-21      Support for multiple constants per CASE branch added
 *      Kay Gürtzig     2015-10-26/27   Language conversion and FOR loop parameter analysis delegated to the elements
 *      Kay Gürtzig     2015-11-04      Bugfix in stepInstruction() w.r.t. input/output (KGU#65)
 *      Kay Gürtzig     2015-11-05      Enhancement allowing to adopt edited values from Control (KGU#68)
 *      Kay Gürtzig     2015-11-08      Array assignments and variable setting deeply revised (KGU#69)
 *      Kay Gürtzig     2015-11-09      Bugfix: div operator had gone, wrong exit condition in stepRepeat (KGU#70),
 *                                      wrong equality operator in stepCase().
 *      Kay Gürtzig     2015-11-11      Issue #21 KGU#77 fixed: return instructions didn't terminate the execution.
 *      Kay Gürtzig     2015-11-12      Bugfix KGU#79: WHILE condition wasn't effectively converted.
 *      Kay Gürtzig     2015-11-13/14   Enhancement #9 (KGU#2) to allow the execution of subroutine calls
 *      Kay Gürtzig     2015-11-20      Bugfix KGU#86: Interpreter was improperly set up for functions sqr, sqrt;
 *                                      Message types for output and return value information corrected
 *      Kay Gürtzig     2015-11-23      Enhancement #36 (KGU#84) allowing to pause from input and output dialogs.
 *      Kay Gürtzig     2015-11-24/25   Enhancement #9 (KGU#2) enabling the execution of calls accomplished.
 *      Kay Gürtzig     2015-11-25/27   Enhancement #23 (KGU#78) to handle Jump elements properly.
 *      Kay Gürtzig     2015-12-10      Bugfix #49 (KGU#99): wrapper objects in variables obstructed comparison,
 *                                      ER #48 (KGU#97) w.r.t. delay control of diagramControllers
 *      Kay Gürtzig     2015-12-11      Enhancement #54 (KGU#101): List of output expressions
 *      Kay Gürtzig     2015-12-13      Enhancement #51 (KGU#107): Handling of empty input and output
 *      Kay Gürtzig     2015-12-15/26   Bugfix #61 (KGU#109): Precautions against type specifiers
 *      Kay Gürtzig     2016-01-05      Bugfix #90 (KGU#125): Arranger updating for executed subroutines fixed
 *      Kay Gürtzig     2016-01-07      Bugfix #91 (KGU#126): Reliable execution of empty Jump elements,
 *                                      Bugfix #92 (KGU#128): Function names were replaced within string literals
 *      Kay Gürtzig     2016-01-08      Bugfix #95 (KGU#130): div operator conversion accidently dropped
 *      Kay Gürtzig     2016-01-09      KGU#133: Quick fix to show returned arrays in a list view rather than a message box
 *      Kay Gürtzig     2016-01-14      KGU#100: Array initialisation in assignments enabled (Enh. #84)
 *      Kay Gürtzig     2016-01-15      KGU#109: More precaution against typed variables (issues #61, #107)
 *      Kay Gürtzig     2016-01-16      Bugfix #112: Several flaws in index evaluation mended (KGU#141)
 *      Kay Gürtzig     2016-01-29      Bugfix #115, enh. #84: Result arrays now always presented as list
 *                                      (with "Pause" button if not already in step mode; KGU#133, KGU#147).
 *      Kay Gürtzig     2016-03-13      Enh. #77, #124: runtime data collection implemented (KGU#117, KGU#156)
 *      Kay Gürtzig     2016-03-16      Bugfix #131: Precautions against reopening, take-over, and loss of control (KGU#157)
 *      Kay Gürtzig     2016-03-17      Enh. #133: Stacktrace now permanently maintained, not only on errors (KGU#159)
 *      Kay Gürtzig     2016-03-18      KGU#89: Language localization support slightly improved
 *      Kay Gürtzig     2016-03-21      Enh. #84 (KGU#61): Support for FOR-IN loops
 *      Kay Gürtzig     2016-03-29      Bugfix #139 (KGU#166) in getIndexValue() - nested index access failed
 *      Kay Gürtzig     2016-04-03      KGU#150: Support for Pascal functions chr and ord
 *                                      KGU#165: Case awareness consistency for keywords improved.
 *      Kay Gürtzig     2016-04-12      Enh. #137 (KGU#160): Additional or exclusive output to text window
 *      Kay Gürtzig     2016-04-25      Issue #30 (KGU#76): String comparison substantially improved,
 *                                      Enh. #174 (KGU#184): Input now accepts array initialisation expressions
 *      Kay Gürtzig     2016-04-26      KGU#150: ord implementation revised,
 *                                      Enh. #137 (KGU#160): Arguments and results added to text window output
 *      Kay Gürtzig     2016-05-05      KGU#197: Further (forgotten) texts put under language support
 *      Kay Gürtzig     2016-05-25      KGU#198: top-level function results weren't logged in the window output
 *      Kay Gürtzig     2016-06-07      KGU#200: While loops showed wrong colour if their body raised an error
 *      Kay Gürtzig     2016-07-25      Issue #201: Look-and-Feel update, Strack trace level numbers (KGU#210)
 *      Kay Gürtzig     2016-07-27      KGU#197: Further (chiefly error) messages put under language support
 *                                      Enh. #137: Error messages now also written to text window output
 *      Kay Gürtzig     2016-09-17      Bugfix #246 (Boolean expressions) and issue #243 (more translations)
 *      Kay Gürtzig     2016-09-22      Issue #248: Workaround for Java 7 in Linux systems (parseUnsignedInt)
 *      Kay Gürtzig     2016-09-25      Bugfix #251: Console window wasn't involved in look and feel update
 *      Kay Gürtzig     2016-09-25      Bugfix #254: parser keywords for CASE elements had been ignored
 *                                      Enh. #253: CodeParser.keywordMap refactoring done
 *      Kay Gürtzig     2016-10-06      Bugfix #261: Stop didn't work immediately within multi-line instructions
 *      Kay Gürtzig     2016-10-07      Some synchronized sections added to reduce inconsistency exception likelihood
 *      Kay Gürtzig     2016-10-09      Bugfix #266: Built-in Pascal functions copy, delete, insert defectively implemented;
 *                                      Issue #269: Attempts to scroll the diagram to currently executed elements (ineffective)
 *      Kay Gürtzig     2016-10-12      Issue #271: Systematic support for user-defined input prompts
 *      Kay Gürtzig     2016-10-13      Enh. #270: Elements may be disabled for execution ("outcommented")
 *      Kay Gürtzig     2016-10-16      Enh. #273: Input strings "true" and "false" now accepted as boolean values
 *                                      Bugfix #276: Raw string conversion and string display mended, undue replacements
 *                                      of ' into " in method convert() eliminated
 *      Kay Gürtzig     2016-11-19      Issue #269: Scrolling problem eventually solved. 
 *      Kay Gürtzig     2016-11-22      Bugfix #293: input and output boxes no longer popped up at odd places on screen.
 *      Kay Gürtzig     2016-11-22/25   Issue #294: Test coverage rules for CASE elements without default branch refined
 *      Kay Gürtzig     2016-12-12      Issue #307: Attempts to manipulate FOR loop variables now cause an error
 *      Kay Gürtzig     2016-12-22      Enh. #314: Support for File API
 *      Kay Gürtzig     2016-12-29      Enh. #267/#315 (KGU#317): Execution abort on ambiguous CALLs
 *      Kay Gürtzig     2017-01-06      Bugfix #324: Trouble with replacing an array by a scalar value on input
 *                                      Enh. #325: built-in type test functions added.
 *      Kay Gürtzig     2017-01-17      Enh. #335: Toleration of Pascal variable declarations in stepInstruction()
 *      Kay Gürtzig     2017-01-27      Enh. #335: Toleration of BASIC variable declarations in stepInstruction()
 *      Kay Gürtzig     2017-02-08      Issue #343: Unescaped internal string delimiters escaped on string literal conversion
 *      Kay Gürtzig     2017-02-17      KGU#159: Stacktrace now also shows the arguments of top-level subroutine calls
 *      Kay Gürtzig     2017-03-06      Bugfix #369: Interpretation of C-style array initializations (decl.) fixed.
 *      Kay Gürtzig     2017-03-27      Issue #356: Sensible reaction to the close button ('X') implemented
 *      Kay Gürtzig     2017-03-30      Enh. #388: Concept of constants implemented
 *      Kay Gürtzig     2017-04-11      Enh. #389: Implementation of import calls (without context change)
 *      Kay Gürtzig     2017-04-12      Bugfix #391: Control button activation fixed for step mode
 *      Kay Gürtzig     2017-04-14      Issue #380/#394: Jump execution code revised on occasion of these bugfixes
 *      Kay Gürtzig     2017-04-22      Code revision KGU#384: execution context bundled into Executor.context
 *      Kay Gürtzig     2017-05-07      Enh. #398: New built-in functions sgn (int result) and signum (float result)
 *      Kay Gürtzig     2017-05-22      Issue #354: converts binary literals ("0b[01]+") into decimal literals 
 *      Kay Gürtzig     2017-05-23      Bugfix #411: converts certain unicode escape sequences to octal ones
 *      Kay Gürtzig     2017-05-24      Enh. #413: New function split(string, string) built in
 *      Kay Gürtzig     2017-06-09      Enh. #416: Support for execution line continuation by trailing backslash
 *      Kay Gürtzig     2017-06-30      Enh. #424: Turtleizer functions enabled (evaluateDiagramControllerFunctions())
 *      Kay Gürtzig     2017-07-01      Enh. #413: Special check for built-in split function in stepForIn()
 *      Kay Gürtzig     2017-07-02      Enh. #389: Include (import) mechanism redesigned (no longer CALL-based)
 *      Kay Gürtzig     2017-09-09      Bugfix #411 revised (issue #426)
 *      Kay Gürtzig     2017-09-17      Enh. #423: First draft implementation of records.
 *      Kay Gürtzig     2017-09-18/27   Enh. #423: Corrections on handling typed constants and for-in loops with records
 *      Kay Gürtzig     2017-09-30      Bugfix #429: Initializer evaluation made available in return statements
 *      Kay Gürtzig     2017-10-02      Some regex stuff revised to gain performance
 *      Kay Gürtzig     2017-10-08      Enh. #423: Recursive array and record initializer evaluation,
 *                                      Array element assignment in record components fixed.
 *      Kay Gürtzig     2017-10-10      Bugfix #433: Ghost results for procedure diagrams named like Java classes
 *      Kay Gürtzig     2017-10-11      Bugfix #434: The condition pre-compilation in loops must not include string comparison
 *      Kay Gürtzig     2017-10-12      Issue #432: Attempt to improve performance by reducing redraw() calls on delay 0
 *      Kay Gürtzig     2017-10-14      Issues #436, #437: Arrays now represented as ArrayList; adoptVarChanges() returns error messages
 *      Kay Gürtzig     2017-10-16      Enh. #439: prepareForDisplay() made static, showArray() generalized to showCompoundValue()
 *      Kay Gürtzig     2017-10-28      Enh. #443: First adaptations for multiple DiagramControllers
 *      Kay Gürtzig     2017-10-29      Enh. #423: Workaround for evaluation error on converted actual object field access
 *      Kay Gürtzig     2017-10-31      Enh. #439: showCompoundValue() now more comfortable with ValuePresenter
 *      Kay Gürtzig     2017-11-01      Bugfix #447: Issue with line continuation backslashes in stepAlternative() fixed
 *      Kay Gürtzig     2017-12-10/11   Enh. #487: New display mode "Hide declarations" supported in execution counting
 *      Kay Gürtzig     2018-01-23      Bugfix #498: stepRepeat no longer checks the loop condition in advance
 *      Kay Gürtzig     2018-02-07/08   Bugfix #503: Defective preprocessing of string comparisons led to wrong results
 *      Kay Gürtzig     2018-02-11      Bugfix #509: Built-in function copyArray had a defective definition
 *      Kay Gürtzig     2018-03-19      Bugfix #525: Cloning and special run data treatment of recursive calls reestablished
 *                                      Enh. #389: class ExecutionStackEntry renamed in ExecutionContext
 *      Kay Gürtzig     2018-03-20      Issue #527: More expressive error messages on index trouble in arrays 
 *      Kay Gürtzig     2018-04-03      KGU#515: Fixed a bug in stepRepeat() (erroneous condition evaluation after a failed body)
 *      Kay Gürtzig     2018-07-02      KGU#539: Fixed the operation step counting for CALL elements 
 *      Kay Gürtzig     2018-07-20      Enh. #563 - support for simplified record initializers
 *      Kay Gürtzig     2018-07-27      Issue #432: Deficient redrawing in step mode with delay 0 fixed
 *      Kay Gürtzig     2018-08-01      Enh. #423/#563: Effort to preserve component order for record display
 *      Kay Gürtzig     2018-08-03      Enh. #577: Meta information to output console now conditioned
 *      Kay Gürtzig     2018-08-06      Some prevention against running status lock on occasion of Issue #577
 *      Kay Gürtzig     2018-09-17      Issue #594: Last remnants of com.stevesoft.pat.Regex replaced
 *      Kay Gürtzig     2018-09-24      Bugfix #605: Handling of const arguments on top level fixed
 *      Kay Gürtzig     2018-10-02/04   Bugfix #617: evaluateDiagramControllerFunctions used to fail when
 *                                      several controller functions occurred in an expression or raised an
 *                                      NullPointerException if a controller function was called with wrong arg number
 *      Kay Gürtzig     2018-12-12      Bugfix #642: Unreliable splitting of comparison expressions
 *      Kay Gürtzig     2018-12-16      Bugfix #644 in tryAssignment()
 *      Kay Gürtzig     2018-12-17      Bugfix #646 in tryOutput()
 *      Kay Gürtzig     2019-02-13      Issue #527: Error message improvement in evaluateExpression()
 *      Kay Gürtzig     2019-02-14      Enh. #680: INPUT instructions with multiple variables supported
 *      Kay Gürtzig     2019-02-17      Issues #51,#137: Write prompts of empty input instructions to output window
 *      Kay Gürtzig     2019-02-26      Bugfix #687: Breakpoint behaviour was flawed for Repeat loops
 *      Kay Gürtzig     2019-03-02      Issue #366: Return the focus to a DiagramController that had it before tryInput()
 *      Kay Gürtzig     2019-03-04      KGU#675 Initial delay with wait removed from stepRoot()
 *      Kay Gürttig     2019-03-07      Enh. #385 - support for optional routine arguments
 *      Kay Gürtzig     2019-03-09      Issue #527 - Refinement of index range error detection (for array copies)
 *      Kay Gürtzig     2019-03-14      Issue #366 - Mainform, Arranger, and Control now also under focus watch
 *      Kay Gürtzig     2019-03-17/18   Enh. #56 - Implementation of try/catch/finally and throw
 *      Kay Gürtzig     2019-03-28      Enh. #657 - Retrieval for subroutines now with group filter
 *      Kay Gürtzig     2019-09-24      Enh. #738 - Reflection of the executed element in code preview
 *      Kay Gürtzig     2019-10-04      Precaution against ConcurrentModificationException (from Arranger)
 *      Kay Gürtzig     2019-10-15      Issue #763 - precautions against collateral effects of widened save check.
 *      Kay Gürtzig     2019-11-08      Bugfix #769 - CASE selector list splitting was too simple for string literals
 *      Kay Gürtzig     2019-11-09      Bugfix #771 - Unhandled errors deep from the interpreter
 *      Kay Gürtzig     2019-11-17      Enh. #739 - Support for enum type definitions
 *      Kay Gürtzig     2019-11-20/21   Enh. #739 - Several fixes and improvements for enh. #739 (enum types)
 *      Kay Gürtzig     2019-11-28      Bugfix #773: component access within index expressions caused trouble in some cases
 *      Kay Gürtzig     2020-02-20      Bugfix #820: Try hadn't respected an exit and didn't reset isErrorReported
 *                                      issue #822: Empty instruction lines are now ignored, missing exit args too.
 *                                      Fixed #823 (defective execution of assignments in some cases)
 *      Kay Gürtzig     2020-02-21      Issue #826: Raw input is to cope with backslashes as in Windows file paths
 *      Kay Gürtzig     2020-04-04      Issue #829: Control should not automatically close after debugging [mawa290669]
 *      Kay Gürtzig     2020-04-13      Bugfix #848: On updating the context of includables mere declarations had been forgotten
 *      Kay Gürtzig     2020-04-23      Bugfix #858: split function in FOR-IN loop was not correctly handled
 *      Kay Gürtzig     2020-04-28      Issue #822: Empty CALL lines should cause more sensible error messages
 *      Kay Gürtzig     2020-10-19      Issue #879: Inappropriate handling of input looking like initializers
 *      Kay Gürtzig     2020-12-14      Issue #829 revoked (Control will by default close after execution)
 *      Kay Gürtzig     2020-12-25      Bugfix #898: Results of substituted Turtleizer functions must be put in parentheses 
 *
 ******************************************************************************************************
 *
 *      Comment:
 *
 *      2017-10-28 Issue #443
 *      - Executor might potentially have to work with several DiagramControllers. So it is important
 *        efficiently to find out, what diagram controller routines are available and whether there are
 *        potential signature conflicts.
 *      - Therefore the field diagramController was replaced by a list diagramContrllers and several
 *        retrieval mechanism had to be revised.
 *      - The Interface DiagramController was also completely redesigned.  
 *      2017-10-13/14 Issue #436
 *      - The internal representations of Structorizer arrays as Object[] caused inconsistent behaviour when
 *        used as parameters: subroutines obtain a reference and replacements of elements are thus effective
 *        for the calling diagram, but as soon as additional elements was appended to the array the reference
 *        got broken (because the Object[] was replaced by a larger one) and all further manipulations weren't
 *        visible to the calling level but performed on a detached copy.
 *        This is now solved by representing arrays as ArrayLists internally (synchronisation is not an issue
 *        here, so Vectors aren't necessary). This enlarges of course the overhead, both in space and time,
 *        expressions require more syntactical conversion before they can be passed to the interpreter.
 *      2017-09-17/2017-10-08
 *      - Type definitions (in Instruction elements) were introduced, particularly for record/struct types:
 *            type &lt;type_name&gt; = record{&lt;component_declaration&gt; ;...}
 *            type &lt;type_name&gt; = struct{&lt;component_declaration&gt; ;...}
 *      - Record initializer expressions were introduced:
 *            &lt;type_name&gt;{&lt;component_name&gt;: &lt;component_value&gt; ,...}
 *      - record values are supported in form of HashMaps, which requires conversion of qualified
 *        names and rather complicated tests on assignments {@see #setVar(String, Object)}
 *      2017-04-22 Code revision (KGU#384)
 *      - The execution context as to be pushed to call stack had been distributed over numerous attributes
 *        and was bundled to an ExecutionStackEntry held in attribute context (the class ExecutionStackEntry
 *        is likely to be renamed to ExecutionContext). This was to simplify the call mechanisms and regain
 *        overview and control.
 *      2016-03-17 Enh. #133 (KGU#159)
 *      - Previously, a Call stack trace was only shown in case of an execution error or manual abort.
 *        Now a Call stack trace may always be requested while execution hasn't ended. Only prerequisite
 *        is that the execution be paused. Then a click on the button "Stacktrace" will do.
 *        Moreover, the stacktrace will always be presented as list view (before, a simple message box had
 *        been used unless the number of call levels exceeded 10).   
 *      2016-03-16/18 Bugfix #131 (KGU#157)
 *      - When the "run" (or "make") button was pressed while an execution was already running or stood
 *        paused then the Executor CALL stack, the event queues, and the connection between Diagram and
 *        Root often got compromised or even corrupted. The same used to happen when a Structorizer
 *        instance replaced its Root while this was involved in some execution. So these actions had to
 *        be prevented if an execution is going on. Hence, the parameterized version of getInstance() 
 *        now checks the running flag and raises a user dialog in order either to ignore the interfering
 *        action or to abort the running execution.
 *      2016-03-06 / 2016-03-12 Enhancements #77, #124 (KGU#117/KGU#156)
 *      - According to an ER by [elemhsb], first a mechanism optionally to visualise code coverage (for
 *        white-box test completeness) was implemented. A green background colour was proposed and used
 *        to highlight covered Element. It soon became clear that with respect to subroutines a dis-
 *        tinction among loose (shallow) and strict (deep) coverage was necessary, particularly when
 *        recursion comes in. So the coverage tracking could be switched between shallow mode (where
 *        subroutines were automatically regarded as proven to have been covered previously, such the
 *        first CALL to a routine it was automatically marked as covered as well) and deep mode where
 *        a CALL was only marked after the subroutine (regarded as brand-new and never analyzed) had
 *        fully been covered at runtime.
 *      - When this obviously worked, I wanted to get more out of the new mechanism. Instead of
 *        deciding first which coverage tracking to do and having to do another run to see the effect
 *        of the complementary option, always both kinds of analysis were done at once, and the user
 *        could arbitrarily switch between the two possible coverage results.
 *      - And then I had a really great idea: Why not add some more runtime data collection, once data
 *        are collected? And so I added an execution counter for every very element, such that after
 *        a run one might easily see, how often a certain operation was executed. And a kind of
 *        histographic analysis seemed also sensible, i.e. to show how the load is distributed over
 *        the elements (particularly the structured ones) and how many instruction steps were needed
 *        in total to run the algorithm for certain data. This is practically an empirical abstract
 *        time estimation. Both count numbers (execution counter / instruction load) are now written
 *        to the upper right corner of any element, and additionally a scaled colouring from deep
 *        blue to hot red is used to visualize the hot spots and the lost places.
 *      2015.12.10 (KGU#97, KGU#99)
 *          Bug/ER #48: An attached diagramController (usually the TurtleBox) had not immediately been
 *            informed about a delay change, such that e.g. the Turtleizer still crept in slow motion
 *            while the Executor had no delay anymore. Now a suitable diagramController will be informed.
 *          Bug 49: Equality test had failed between variables, particularly between array elements,
 *            because they presented Wrapper objects (e. g. Integer) rather than primitive values. 
 *            For scalar variables, values are now assigned as primitive type if possible (via
 *            interpreter.eval()). For array elements, in contrast, the comparison expression  will be
 *            converted, such that == and != will be replaced by .equals() calls.
 *      2015.11.23 (KGU#84) Pausing from input and output dialogs enabled (Enhancement issue #36)
 *          On cancelling input now first a warning box opens and after having quit the execution is in pause
 *          mode such that the user may edit values, abort or continue in either run oder step mode.
 *          Output and result message dialogs now provide a Pause button to allow to pause mode (see above).
 *      2015.11.13 (KGU#2) Subroutine call mechanisms introduced
 *          Recursively callable submethod of execute(Root) added plus new call-handling method executeCall()
 *          Error handling in some subroutine level still neither prepared nor tested
 *      2015.11.04 (KGU#65) Input/output execution mended
 *          The configured input / output parser settings triggered input or output action also if found
 *          deep in a line, even within a string literal. This was mended.
 *      2015.10.26/27 (KGU#3) Language conversion (in method convert) partially delegated to Element
 *          The aim was to share this functionality with generators
 *          Analysis of FOR loop parameters also delegated to the For class instance.
 *      2015.10.21 (KGU#15) Common branch for multiple constants in Case structure enabled
 *          A modification in stepCase() now allows to test against a comma-separated list of case constants
 *          (though it would fail with complex expressions, accidently containing commas but this would anyway
 *          produce nonsense on code export)
 *      2015.10.17/18 (KGU#2) Two successful (though somewhat makeshift) subroutine retrieval attempts
 *          in stepInstruction() via Arranger and by means of Bob's Function class.
 *          We can be glad that Executor is already a Singleton - on the one hand...
 *          Towards an actually working approach several challenges must therefore be addressed:
 *          1. a Stack with tuples of root, variable values, return value, and the like.
 *          2. Reentrance of the Elements or replication of entire Element hierarchies.
 *          3. Recursion on the user algorithm level (see above) - if deep copies of the diagrams are
 *             temporarily created and pushed into the Arranger then either an additional "busy" flag
 *             will be necessary on Root or a second, volatile diagram vector (not be searched!) on
 *             Surface. By design, volatile subroutine copies should never be associated with a Mainform,
 *             not even on double-clicking! By design, they should partially overlap on the Surface
 *             (in the stack order i.e. top on top).
 *          4. The trouble is going to get really nasty with Parallel elements involved, particularly if
 *             their threads use identical subroutines.   
 *      2015.10.15 (KGU#47) Improved simulation of Parallel execution
 *          Instead of running entire "threads" of the parallel section in just random order, the "threads"
 *          will now only progress by one instruction when randomly chosen, so they alternate in an
 *          unpredictable way)
 *         
 ******************************************************************************************************///

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lu.fisch.diagrcontrol.*;
import lu.fisch.diagrcontrol.DiagramController.FunctionException;
import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.gui.Diagram;
import lu.fisch.structorizer.gui.IconLoader;
import lu.fisch.structorizer.parsers.CodeParser;
//import lu.fisch.structorizer.syntax.ExprParser;
import lu.fisch.utils.BString;
import lu.fisch.utils.StringList;
import bsh.EvalError;
import bsh.Interpreter;

/**
 * Singleton class controlling the execution of a Nassi-Shneiderman diagram.
 * Method sed as runnable thread.
 * @author robertfisch
 */
public class Executor implements Runnable
{
	// START KGU 2018-03-21
	public static final Logger logger = Logger.getLogger(Executor.class.getName());
	// END KGU 2018-03-21

	// START KGU#376 2017-04-20: Enh. #389
	/**
	 * Context record for an imported Root in order to fetch the defined constants
	 * and global/static variables as well as defined or declared types.
	 * This prototype version just contains the BeanShell interpreter used at the
	 * initial execution and hence bearing values etc. and a list of variable names.
	 * Supports enhancement #389
	 * @author Kay Gürtzig
	 * @see Executor#importMap
	 */
	private class ImportInfo {
		public final Interpreter interpreter;
		public final StringList variableNames;
		// START KGU#388 2017-09-18: Enh. 423
		public final HashMap<String, TypeMapEntry> typeDefinitions;
		// END KGU#388 2017-09-18
		// START KGU#388 2017-09-18: Enh. 423
		//public ImportInfo(Interpreter _interpr, StringList _varNames) {
		public ImportInfo(Interpreter _interpr, StringList _varNames, HashMap<String, TypeMapEntry> _typeMap) {
			interpreter = _interpr;
			variableNames = _varNames;
			// START KGU#388 2017-09-18: Enh. 423
			typeDefinitions = new HashMap<String, TypeMapEntry>(_typeMap);
			// END KGU#388 2017-09-18
		}
		// END KGU#388 2017-09-18
	};
	// END KGU#376 2017-04-20

	private static Executor mySelf = null;
	// START KGU#311 2016-12-22: Enh. #314 - fileAPI index
	public static final String[] fileAPI_names = {
		"fileOpen", "fileCreate", "fileAppend",
		"fileClose",
		"fileRead", "fileReadChar", "fileReadInt", "fileReadDouble", "fileReadLine",
		"fileEOF",
		"fileWrite", "fileWriteLine"
	};
	// END KGU#311 2016-12-22

	private static final String[] builtInFunctions = new String[] {
			"public int random(int max) { return (int) (Math.random()*max); }",
			"public void randomize() {  }",
			// START KGU#391 2017-05-07: Enh. #398 - we need a sign function to ease the rounding support for COBOL import
			"public int sgn(int i) { return (i == 0 ? 0 : (i > 0 ? 1 : -1)); }",
			"public int sgn(double d) { return (d == 0 ? 0 : (d > 0 ? 1 : -1)); }",
			// END KGU#391 2017-05-07
			// square
			"public double sqr(double d) { return d * d; }",
			// square root
			"public double sqrt(double d) { return Math.sqrt(d); }",
			// length of a string
			"public int length(String s) { return s.length(); }",
			// position of a substring inside another string
			"public int pos(String subs, String s) { return s.indexOf(subs)+1; }",
			"public int pos(Character subs, String s) { return s.indexOf(subs)+1; }",
			// return a substring of a string
			// START KGU#275 2016-10-09: Bugfix #266: length tolerance of copy function had to be considered
			//"public String copy(String s, int start, int count) { return s.substring(start-1,start-1+count); }",
			"public String copy(String s, int start, int count) { int end = Math.min(start-1+count, s.length()); return s.substring(start-1,end); }",
			// END KGU#275 2016-10-09
			// delete a part of a string
			"public String delete(String s, int start, int count) { return s.substring(0,start-1)+s.substring(start+count-1); }",
			// insert a string into another one
			"public String insert(String what, String s, int start) { return s.substring(0,start-1)+what+s.substring(start-1); }",
			// string transformation
			"public String lowercase(String s) { return s.toLowerCase(); }",
			"public String uppercase(String s) { return s.toUpperCase(); }",
			"public String trim(String s) { return s.trim(); }",
			// START KGU#410 2017-05-24: Enh. #413: Introduced to facilitate COBOL import but generally useful
			// If we passed the result of String.split() directly then we would obtain a String[] object the 
			// Executor cannot display.
			"public ArrayList split(String s, String p)"
					+ "{ p = java.util.regex.Pattern.quote(p);"
					+ " String[] parts = s.split(p, -1);"
					+ "ArrayList results = new ArrayList(parts.length);"
					+ " for (int i = 0; i < parts.length; i++) {"
					+ "		results.add(parts[i]);"
					+ "}"
					+ "return results; }",
			"public ArrayList split(String s, char c)"
					+ "{ return split(s, \"\" + c); }",
			// END KGU#410 2017-05-24
			// START KGU#651 2019-02-13: Issue #678 C function facilitating code import
			"public int strcmp(String s1, String s2)"
					+ "{ return s1.compareTo(s2); }",
			// END KGU#651 2019-02-13
			// START KGU#57 2015-11-07: More interoperability for characters and Strings
			// char transformation
			"public Character lowercase(Character ch) { return (Character)Character.toLowerCase(ch); }",
			"public Character uppercase(Character ch) { return (Character)Character.toUpperCase(ch); }",
			// START KGU#150 2016-04-03
			"public int ord(Character ch) { return (int)ch; }",
			// START KGU 2016-04-26: It is conform to many languages just to use the first character
			//"public int ord(String s) throws Exception { if (s.length() == 1) return (int)s.charAt(0); else throw new Exception(); }",
			"public int ord(String s) { return (int)s.charAt(0); }",
			// END KGU 2016-04-26
			"public char chr(int code) { return (char)code; }",
			// END KGU#150 2016-04-03
			// END KGU#57 2015-11-07
			// START KGU#322 2017-01-06: Enh. #325 - reflection functions
			"public boolean isArray(Object obj) { return (obj instanceof ArrayList); }",
			"public boolean isString(Object obj) { return (obj instanceof String); }",
			"public boolean isChar(Object obj) { return (obj instanceof Character); }",
			"public boolean isBool(Object obj) { return (obj instanceof Boolean); }",
			"public boolean isNumber(Object obj) { return (obj instanceof Integer) || (obj instanceof Double); }",
			// START KGU#439 2017-10-13: Issue #436
			//"public int length(Object[] arr) { return arr.length; }",
			"public int length(ArrayList arr) { return arr.size(); }",
			// END KGU#439 2017-10-13
			// END KGU#322 2017-01-06
			// START KGU 2016-12-18: #314: Support for simple text file API
			"public int fileOpen(String filePath) { "
					+ "int fileNo = 0; "
					+ "java.io.File file = new java.io.File(filePath); "
					+ "if (!file.isAbsolute()) { "
					+ "file = new java.io.File(executorCurrentDirectory + java.io.File.separator + filePath); "
					+ "} "
					+ "try { java.io.FileInputStream fis = new java.io.FileInputStream(file); "
					+ "java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis, \"UTF-8\")); "
					+ "fileNo = executorFileMap.size() + 1; "
					+ "executorFileMap.add(new java.util.Scanner(reader)); "
					+ "} "
					+ "catch (SecurityException e) { fileNo = -3; } "
					+ "catch (java.io.FileNotFoundException e) { fileNo = -2; } "
					+ "catch (java.io.IOException e) { fileNo = -1; } "
					+ "return fileNo; }",

			"public int fileCreate(String filePath) { "
					+ "int fileNo = 0; "
					+ "java.io.File file = new java.io.File(filePath); "
					+ "if (!file.isAbsolute()) { "
					+ "file = new java.io.File(executorCurrentDirectory + java.io.File.separator + filePath); "
					+ "} "
					+ "try { java.io.FileOutputStream fos = new java.io.FileOutputStream(file); "
					+ "java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, \"UTF-8\")); "
					+ "fileNo = executorFileMap.size() + 1; "
					+ "executorFileMap.add(writer); "
					+ "} "
					+ "catch (SecurityException e) { fileNo = -3; } "
					+ "catch (java.io.FileNotFoundException e) { fileNo = -2; } "
					+ "catch (java.io.IOException e) { fileNo = -1; } "
					+ "return fileNo; }",

			"public int fileAppend(String filePath) { "
					+ "int fileNo = 0; "
					+ "java.io.File file = new java.io.File(filePath); "
					+ "if (!file.isAbsolute()) { "
					+ "file = new java.io.File(executorCurrentDirectory + java.io.File.separator + filePath); "
					+ "} "
					+ "try { java.io.FileOutputStream fos = new java.io.FileOutputStream(file, true); "
					+ "java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, \"UTF-8\")); "
					+ "fileNo = executorFileMap.size() + 1; "
					+ "executorFileMap.add(writer); "
					+ "} "
					+ "catch (SecurityException e) { fileNo = -3; } "
					+ "catch (java.io.FileNotFoundException e) { fileNo = -2; } "
					+ "catch (java.io.IOException e) { fileNo = -1; } "
					+ "return fileNo; "
					+ "}",

			"public void fileClose(int fileNo) { "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable file = executorFileMap.get(fileNo - 1); "
					+ "if (file != null) { "
					+ "try { file.close(); } "
					+ "catch (java.io.IOException e) {} "
					+ "executorFileMap.set(fileNo - 1, null); } "
					+ "}"
					+ "}",

			"public boolean fileEOF(int fileNo) {"
					+ "	boolean isEOF = true; "
					+ "	if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "		java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "		if (reader instanceof java.util.Scanner) { "
					+ "			try { "
					+ "				isEOF = !((java.util.Scanner)reader).hasNext();"
					+ "			} catch (IOException e) {}"
					+ "		}"
					+ "	}"
					+ "	else { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "	return isEOF;"
					+"}",
			// The following is just a helper method...
			"public Object structorizerGetScannedObject(java.util.Scanner sc) {"
					+ "Object result = null; "
					+ "sc.useLocale(java.util.Locale.UK); "
					+ "if (sc.hasNextInt()) { result = sc.nextInt(); } "
					+ "else if (sc.hasNextDouble()) { result = sc.nextDouble(); } "
					+ "else if (sc.hasNext(\"\\\\\\\".*?\\\\\\\"\")) { "
					+ "String str = sc.next(\"\\\\\\\".*?\\\\\\\"\"); "
					+ "result = str.substring(1, str.length() - 1); "
					+ "} "
					+ "else if (sc.hasNext(\"'.*?'\")) { "
					+ "String str = sc.next(\"'.*?'\"); "
					+ "result = str.substring(1, str.length() - 1); "
					+ "} "
					+ "else if (sc.hasNext(\"\\\\{.*?\\\\}\")) { "
					+ "String token = sc.next(); "
					+ "result = new Object[]{token.substring(1, token.length()-1)}; "
					+ "} " 
					+ "else if (sc.hasNext(\"\\\\\\\".*\")) { "
					+ "String str = sc.next(); "
					+ "while (sc.hasNext() && !sc.hasNext(\".*\\\\\\\"\")) { "
					+ "str += \" \" + sc.next(); "
					+ "} "
					+ "if (sc.hasNext()) { str += \" \" + sc.next(); } "
					+ "result = str.substring(1, str.length() - 1); "
					+ "} "
					+ "else if (sc.hasNext(\"'.*\")) { "
					+ "String str = sc.next(); "
					+ "while (sc.hasNext() && !sc.hasNext(\".*'\")) { "
					+ "str += \" \" + sc.next(); "
					+ "} "
					+ "if (sc.hasNext()) { str += \" \" + sc.next(); } "
					+ "result = str.substring(1, str.length() - 1); "
					+ "} "
					+ "else if (sc.hasNext(\"\\\\{.*\")) { "
					+ "java.util.regex.Pattern oldDelim = sc.delimiter(); "
					+ "sc.useDelimiter(\"\\\\}\"); "
					+ "String content = sc.next().trim().substring(1); "
					+ "sc.useDelimiter(oldDelim); "
					+ "if (sc.hasNext(\"\\\\}\")) { sc.next(); } "
					+ "String[] elements = {}; "
					+ "if (!content.isEmpty()) { "
					+ "elements = content.split(\"\\\\p{javaWhitespace}*,\\\\p{javaWhitespace}*\"); "
					+ "} "
					+ "Object[] objects = new Object[elements.length]; "
					+ "for (int i = 0; i < elements.length; i++) { "
					+ "java.util.Scanner sc0 = new java.util.Scanner(elements[i]); "
					+ "objects[i] = structorizerGetScannedObject(sc0); "
					+ "sc0.close(); "
					+ "} "
					+ "result = objects;"
					+ "}"
					+ "else { result = sc.next(); } "
					+ "return result; }",
			"public Object fileRead(int fileNo) { "
					+ "Object result = null; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "result = structorizerGetScannedObject((java.util.Scanner)reader); "
					+ "ok = true;"
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return result; }",
			"public Character fileReadChar(int fileNo) { "
					+ "Character result = '\0'; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "java.util.Scanner sc = (java.util.Scanner)reader; "
					+ "java.util.regex.Pattern oldDelim = sc.delimiter(); "
					+ "sc.useDelimiter(\"\"); "
					+ "try { "
					+ "if (!sc.hasNext(\".\") && sc.hasNextLine()) { sc.nextLine(); result = '\\n'; }"
					+ "else { result = sc.next(\".\").charAt(0); } "
					+ "}"
					+ "finally { sc.useDelimiter(oldDelim); } "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return result; }",
			"public Integer fileReadInt(int fileNo) { "
					+ "Integer result = null; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "result = ((java.util.Scanner)reader).nextInt(); "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return result; }",
			"public Double fileReadDouble(int fileNo) { "
					+ "Double result = null; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "result = ((java.util.Scanner)reader).nextDouble(); "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return result; }",
			"public String fileReadLine(int fileNo) { "
					+ "String line = null; "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable reader = executorFileMap.get(fileNo - 1); "
					+ "if (reader instanceof java.util.Scanner) { "
					+ "line = ((java.util.Scanner)reader).nextLine(); "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberRead.getText() + "\"); } "
					+ "return line;	}",
			"public void fileWrite(int fileNo, java.lang.Object data) { "
					+ "	boolean ok = false; "
					+ "	if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "		java.io.Closeable writer = executorFileMap.get(fileNo - 1); "
					+ "		if (writer instanceof java.io.BufferedWriter) { "
					+ "			((java.io.BufferedWriter)writer).write(data.toString()); "
					+ "		ok = true;"
					+ "	}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberWrite.getText() + "\"); } "
					+ "}",
			"public void fileWriteLine(int fileNo, java.lang.Object data) { "
					+ "boolean ok = false; "
					+ "if (fileNo > 0 && fileNo <= executorFileMap.size()) { "
					+ "java.io.Closeable file = executorFileMap.get(fileNo - 1); "
					+ "if (file instanceof java.io.BufferedWriter) { "
					+ "((java.io.BufferedWriter)file).write(data.toString()); "
					+ "((java.io.BufferedWriter)file).newLine(); "
					+ "ok = true; "
					+ "}"
					+ "}"
					+ "if (!ok) { throw new java.io.IOException(\"" + Control.msgInvalidFileNumberWrite.getText() + "\"); } "
					+ "}",
			// END KGU 2016-12-18
			// START KGU#439 2017-10-13: Issue #436 Array representation changed from Object[] to ArrayList<Object>
			//"public ArrayList copyArray(Object[] sourceArray) {"
			//		+ "ArrayList targetArray = new ArrayList(sourceArray.length);"
			//		+ "for (int i = 0; i < sourceArray.length; i++) {"
			//		+ "targetArray.add(sourceArray[i]);"
			//		+ "}"
			//		+ "return targetArray;"
			//		+ "}",
			"public ArrayList copyArray(ArrayList sourceArray) {"
					// START KGU#492 2018-02-11: Bugfix #509 - wrong use of arguments
					//+ "return new ArrayList(targetArray);"
					+ "return new ArrayList(sourceArray);"
					// END KGU#492 2018-02-11
					+ "}",
			// END KGU#439 2017-10-13
			// START KGU#388 2017-09-13: Enh. #423 Workaround for missing support of HashMap<?,?>.clone() in bsh-2.0b4.jar
			"public HashMap copyRecord(HashMap sourceRecord) {"
					+ "return new HashMap(sourceRecord);"
					+ "}"
	};
	
	/**
	 * Returns the singleton instance IF THERE IS ONE. Does NOT create an instance!
	 * @return the existing instance or null.
	 * @see #getInstance(Diagram, DiagramController)
	 */
	public static Executor getInstance()
	{
		return mySelf;
	}

	// START KGU#448 2017-10-28: Enh. #443
	///**
	// * Ensures there is a (singleton) instance and returns it
	// * @param diagram - the Diagram instance requesting the instance (also used for conflict detection)
	// * @param diagramController - facade of an additionally controllable module or device 
	// * @return the sole instance of this class.
	// */
	//public static Executor getInstance(Diagram diagram,
	//		DiagramController diagramController)
	/**
	 * Ensures there is a (singleton) instance and returns it
	 * @param diagram - the Diagram instance requesting the instance (also used for conflict detection)
	 * @param diagramControllers - façades of additionally controllable modules or devices 
	 * @return the sole instance of this class.
	 */
	public static Executor getInstance(Diagram diagram,
			DiagramController[] diagramControllers)
	// END KGU#448 2017-10-28
	{
		// START KGU#443 2017-10-16: It is annoying if control refuses to keep its place on restart
		boolean setControlLocation = false;
		// END KGU#443 2017-10-16
		if (mySelf == null)
		{
			mySelf = new Executor(diagram);
			// START KGU#448 2017-10-28: Enh. #443
			// END KGU#448 2017-10-28
			// START KGU#443 2017-10-16: It is annoying if control refuses to keep its place on restart
			setControlLocation = true;
			// END KGU#443 2017-10-16
		}
		// START KGU#448 2017-10-28: Enh. #443 This must not of course be done while Executor is running!
		//if (diagramController != null)
		//{
		//	mySelf.diagramController = diagramController;
		//}
		// END KGU#448 2017-10-28
		// START KGU#157 2016-03-16: Bugfix #131 - Don't init if there is a running thread
		//if (diagram != null)
		//{
		//	mySelf.diagram = diagram;
		//}
		//mySelf.control.init();
		//mySelf.control.setLocationRelativeTo(diagram);
		boolean doInitialise = true;
		mySelf.reopenFor = null;
		if (mySelf.diagram != null && mySelf.running)
		{
			doInitialise = false;
			Root root = mySelf.diagram.getRoot();
			String errText = mySelf.control.lbStopRunningProc.getText();
			errText = errText.replace("\\n", "\n");
			if (root != null)
			{
				errText = errText.replace("?", " (\"" + root.getMethodName() + "\")?");
			}
			int res = JOptionPane.showOptionDialog(diagram,
					errText,
					mySelf.control.msgTitleQuestion.getText(),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,null,null);
			if (res == 0)
			{
				// START KGU 2018-08-08: If it had already been stopped make sure new start will be possible
				if (mySelf.stop) {
					mySelf.running = false;
					mySelf.control.init();
				}
				// END KGU 2018-08-08
				mySelf.setStop(true);
				mySelf.reopenFor = diagram;
			}
		}
		if (doInitialise)
		{
			// START KGU#448 2017-10-28: Enh. #443
			if (diagramControllers != null)
			{
				// Now configure the API look-up tables
				mySelf.configureControllerLookUps(diagramControllers);
			}
			// END KGU#448 2017-10-28
			if (diagram != null)
			{
				mySelf.diagram = diagram;
			}
			mySelf.control.init();
			// START KGU#443 2017-10-16: It is annoying if control refuses to keep its place on restart
			//mySelf.control.setLocationRelativeTo(diagram);
			if (setControlLocation) {
				mySelf.control.setLocationRelativeTo(diagram);
			}
			// END KGU#443 2017-10-16
		}
		// END KGU#157 2016-03-16: Bugfix #131
		mySelf.control.validate();
		mySelf.control.setVisible(true);
		mySelf.control.repaint();

		return mySelf;
	}

	// START KGU#448 2017-10-28: Enh. #443
	/**
	 * Sets up the DiagramController API tables for faster retrieval
	 */
	private void configureControllerLookUps(DiagramController[] controllers) {
		this.diagramControllers = controllers;
		this.controllerFunctions.clear();
		this.controllerProcedures.clear();
		this.controllerFunctionNames.clear();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < controllers.length; i++) {
			DiagramController controller = controllers[i];
			for (String key: controller.getFunctionMap().keySet()) {
				String name = key.substring(0, key.indexOf('#'));
				DiagramController conflicting = null;
				if ((conflicting = this.controllerFunctions.put(key, controller)) != null) {
					sb.append(Control.msgFunctionConflict.getText().
							replace("%1", name).
							replace("%2", key.substring(key.indexOf('#')+1)).
							replace("%3", conflicting.getName()).
							replace("%4", controller.getName()));
				}
				this.controllerFunctionNames.add(name);
			}
			for (String key: controller.getProcedureMap().keySet()) {
				String name = key.substring(0, key.indexOf('#'));
				DiagramController conflicting = null;
				if ((conflicting = this.controllerProcedures.put(key, controller)) != null) {
					sb.append(Control.msgFunctionConflict.getText().
							replace("%1", name).
							replace("%2", key.substring(key.indexOf('#')+1)).
							replace("%3", conflicting.getName()).
							replace("%4", controller.getName()));
				}
			}				
		}
		if (sb.length() > 0) {
			JOptionPane.showMessageDialog(null, 
					Control.msgSignatureConflicts.getText().replace("%", sb.toString()),
					Control.class.getSimpleName(), JOptionPane.WARNING_MESSAGE);
		}
	}
	// END KGU#448 2017-10-28

	private Control control = new Control();

	// START KGU#160 2016-04-12: Enh. #137 - Option for text window output
	private OutputConsole console = new OutputConsole();
	private boolean isConsoleEnabled = false; 
	// END KGU#160 2016-04-12

	private int delay = 50;

	private Diagram diagram = null;
	
	// START KGU#376 2017-04-20: Enh. #389 - we need info about all imported Roots
	/**
	 * Maps all Roots ever called as import during current execution to their
	 * execution results, represented by an ImportInfo object, such that whenever
	 * the same Root will be requested for import again, we may just retrieve its
	 * results here.
	 * @see ExecutionContext#importList 
	 */
	private final HashMap<Root, ImportInfo> importMap = new HashMap<Root, ImportInfo>();
	//private StringList importList = new StringList();	// KGU#384 2017-04-22: -> context
	// END KGU#376 2017-04-20
	// START KGU#384 2017-04-22: Redesign of the execution context
	/**
	 * Execution context cartridge containing all context to be pushed to callers stack on calls
	 */
	private ExecutionContext context;
	// END KGU#376 2017-04-20
	// START KGU#2 (#9) 2015-11-13: We need a stack of calling parents
	private Stack<ExecutionContext> callers = new Stack<ExecutionContext>();
	//private Object returnedValue = null;	// KGU#384 2017-04-22 -> context
	private Vector<IRoutinePool> routinePools = new Vector<IRoutinePool>();
	// END KGU#2 (#9) 2015-11-13
	// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
	//private StringList forLoopVars = new StringList();	// KGU#384 2017-04-22 -> context
	// END KGU#307 2016-12-12

	// START KGU#448 2017-10-28: Enh.#443
	//private DiagramController diagramController = null;
	private DiagramController[] diagramControllers = new DiagramController[]{};
	private HashMap<String, DiagramController> controllerFunctions = new HashMap<String, DiagramController>();
	private HashMap<String, DiagramController> controllerProcedures = new HashMap<String, DiagramController>();
	private Set<String> controllerFunctionNames = new HashSet<String>(); 
	// END KGU#448 2017-10-28
	// START KGU#384 2017-04-22: Context redesign -> this.context
	//private Interpreter interpreter;
	//private boolean returned = false;
	// END KGU#384 2017-04-22

	private boolean paus = false;
	private boolean running = false;
	private boolean step = false;
	private boolean stop = false;
	// START KGU#78 2015-11-25: JUMP enhancement (#35)
	//private int loopDepth = 0;	// Level of nested loops KGU#384 207-04-22 -> context
	/** Number of loop levels to unwind */
	private int leave = 0;
	// END KGU#78 2015-11-25
	//private StringList variables = new StringList();	// KGU#384 2017-04-22 -> context
	// START KGU#375 2017-03-30: Enh. #388 Support the concept of variables
	//private HashMap<String, Object> constants = new HashMap<String, Object>();	// KGU#384 2017-04-22 -> context
	// END KGU#375 2017-03-30
	// START KGU#2 2015-11-24: It is crucial to know whether an error had been reported on a lower level
	/** Had a lower call level already reported an error? */
	private boolean isErrorReported = false;
	// START KGU#686 2019-03-17: Enh. #56 implementation of a try element
	/** This is to convey an error message from a subroutine if called within a try block */
	private String subroutineTrouble = null;
	/** Indicates whether the current execution is done within a try block */
	private boolean withinTryBlock = false;
	/** Specific message allowing a catch block to recognise a throw without arguments */
	private static final String RETHROW_MESSAGE = "unspecified throw";
	/** Is set on an exit instruction, which is not supposed to be catchable */
	private boolean isExited = false;
	// END KGU#686 2019-03-17
	private StringList stackTrace = new StringList();
	// END KGU#2 2015-11-22
	// START KGU#157 2016-03-16: Bugfix #131 - Precaution against reopen attempts by different Structorizer instances
	/** A Structorizer instance that tried to open Control while still running */
	private Diagram reopenFor = null;
	// END KGU#2 2016-03-16
	// START KGU 2016-12-18: Enh. #314: Stream table for Simple file API
	private final Vector<Closeable> openFiles = new Vector<Closeable>();
	// END KGU 2016-12-18
	// START KGU#477 2017-12-10: Enh. #487
	/** The first element of a currently executed mere declaration sequence */
	private Instruction lastDeclarationSurrogate = null;
	// END KGU#477 2017-12-10
	
	// Constant set of matchers for unicode literals that cause harm in interpreter
	// (Concurrent execution of the using method is rather unlikely, so we dare to reuse the Matchers) 
	private static final Matcher[] MTCHs_BAD_UNICODE = new Matcher[]{
			Pattern.compile("(.*)\\\\u000[aA](.*)").matcher(""),
			Pattern.compile("(.*?)\\\\u000[dD](.*?)").matcher(""),
			Pattern.compile("(.*?)\\\\u0022(.*?)").matcher(""),
			Pattern.compile("(.*?)\\\\u005[cC](.*?)").matcher("")
	};
	// Replacement patterns for the unicode literals associated with the matchers above
	private static final String[] RPLCs_BAD_UNICODE = new String[]{
			"$1\\\\012$2",
			"$1\\\\015$2",
			"$1\\\\042$2",
			"$1\\\\134$2"
	};
	/** Matcher for binary integer literals, which the interpreter doesn't cope with */
	private static final Matcher MTCH_BIN_LITERAL = Pattern.compile("0b[01]+").matcher("");
	/** Matcher for certain interpreter error messages related to array assignment */
	// FIXME: Might have to be adapted with a newer version of the bean shell interpreter some day ...
	private static final Matcher MTCH_EVAL_ERROR_ARRAY = Pattern.compile(".*Can't assign.*to java\\.lang\\.Object \\[\\].*").matcher("");
	/** Matcher for split function */
	//private static final Matcher MTCH_SPLIT = Pattern.compile("^split\\(.*?[,].*?\\)$").matcher("");
	// Replacer Regex objects for syntax conversion - if Regex re-use shouldn't work then we may replace it by java.util.regex stuff
	// START KGU#575 2018-09-17: Issue #594 - we replace it anyway now
	//private static final Regex RPLC_DELETE_PROC = new Regex("delete\\((.*),(.*),(.*)\\)", "$1 <- delete($1,$2,$3)");
	//private static final Regex RPLC_INSERT_PROC = new Regex("insert\\((.*),(.*),(.*)\\)", "$2 <- insert($1,$2,$3)");
	//private static final Regex RPLC_INC2_PROC = new Regex(BString.breakup("inc")+"[(](.*?)[,](.*?)[)](.*?)", "$1 <- $1 + $2");
	//private static final Regex RPLC_INC1_PROC = new Regex(BString.breakup("inc")+"[(](.*?)[)](.*?)", "$1 <- $1 + 1");
	//private static final Regex RPLC_DEC2_PROC = new Regex(BString.breakup("dec")+"[(](.*?)[,](.*?)[)](.*?)", "$1 <- $1 - $2");
	//private static final Regex RPLC_DEC1_PROC = new Regex(BString.breakup("dec")+"[(](.*?)[)](.*?)", "$1 <- $1 - 1");
	private static final Matcher DELETE_PROC_MATCHER = java.util.regex.Pattern.compile("delete\\((.*),(.*),(.*)\\)").matcher("");
	private static final Matcher INSERT_PROC_MATCHER = java.util.regex.Pattern.compile("insert\\((.*),(.*),(.*)\\)").matcher("");
	private static final String DELETE_PROC_SUBST = "$1 <- delete($1,$2,$3)";
	private static final String INSERT_PROC_SUBST = "$2 <- insert($1,$2,$3)";
	// END KGU#575 2018-09-17
	
	private static final StringList OBJECT_ARRAY = StringList.explode("Object,[,]", ",");
	
	// START KGU#388 2017-10-29: Enh. #423 This EvalError message indicates that the record qualifier conversion may have overdone  
	private static final String ERROR423MESSAGE = "Error in method invocation: Method get( java.lang.String ) not found in class";
	private static final Matcher ERROR423MATCHER = Pattern.compile(".*inline evaluation of: ``(.*?\\.)get\\(\\\"(\\w+)\\\"\\)(.*?)'' : Error in method.*").matcher("");
	// END KGU#388 2017-10-29
	// START KGU#510 2018-03-20: Issue #527 Possible pattern for index problem
	// START KGU#677 2019-03-09: In case of Arrays being the result of a function (e.g. copyArray()), the message looks different
	//private static final Matcher ERROR527MATCHER = Pattern.compile(".*inline evaluation of: ``(.*?\\.)get\\((.*?)\\)(.*?)'' : Method Invocation (\\w+)\\.)get").matcher("");
	private static final Matcher ERROR527MATCHER = Pattern.compile(".*inline evaluation of: ``(.*?)\\.get\\((.*)\\)(.*?)'' : Method Invocation ((\\w+)\\.)?get").matcher("");
	// END KGU#677 2019-03-09
	// END KGU#510 2018-03-20
	private static final int MAX_STACK_INDENT = 40;
	
	// START KGU#448 2017-10-28: Enh. #443 - second argument will be initialized in getInstance() anyway
	//private Executor(Diagram diagram, DiagramController diagramController)
	private Executor(Diagram diagram)
	// END KGU#448 2017-10-28
	{
		this.diagram = diagram;
		
		// START KGU#448 2017-10-28: Enh. #443
		//this.diagramController = diagramController;
		// END KGU#448 2017-10-28
		// START KGU#372 2017-03-27: Enh. #356: Show at least an information on closing attempt
		this.control.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent evt) {}

			@Override
			public void windowClosed(WindowEvent evt) {}

			@Override
			public void windowClosing(WindowEvent evt) {
				if (evt.getSource() == control) {	// should be the only possible source but...
					if (running) {
						JOptionPane.showMessageDialog(null, Control.msgUseStopButton.getText(),
								mySelf.getClass().getSimpleName() + ": " + mySelf.diagram.getRoot().getSignatureString(false),
								JOptionPane.WARNING_MESSAGE);
					}
					else {
						control.clickStopButton();
					}
				}
			}

			@Override
			public void windowDeactivated(WindowEvent evt) {}

			@Override
			public void windowDeiconified(WindowEvent evt) {}

			@Override
			public void windowIconified(WindowEvent evt) {}

			@Override
			public void windowOpened(WindowEvent evt) {}
			
		});
		// END KGU#372 2017-03-27
	}

	// START KGU#210/KGU#234 2016-08-08: Issue #201 - Ensure GUI consistency
	public static void updateLookAndFeel()
	{
		if (mySelf != null)
		{
			mySelf.control.updateLookAndFeel();
			// START KGU#255 2016-09-25: Bugfix #251
			SwingUtilities.updateComponentTreeUI(mySelf.console);
			// END KGU#255 2016-09-25
		}
	}
	// END KGU#210/KGU#234 2016-08-08
	
	// METHOD MODIFIED BY GENNARO DONNARUMMA

	/**
	 * Unifies the operators, replaces math functions and certain built-in routines and
	 * converts string comparisons.<br/>
	 * NOTE: This method should NOT be called if {@code s} contains an entire instruction line
	 * rather than just an expression - use {@code convert(s, false)} in such cases instead
	 * and make sure the {@link #convertStringComparison(String)} is called for the mere
	 * expression part later on. 
	 * @param s - the expression or instruction line to be pre-processed
	 * @param convertComparisons - whether string comparisons are to be detected and rewritten
	 * @return the converted string
	 * @see #convert(String, boolean)
	 * @see #convertStringComparison(String)
	 */
	private String convert(String s)
	{
		return convert(s, true);
	}
	
	/**
	 * Unifies the operators, replaces math functions and certain built-in routines and
	 * converts string comparisons if {@code convertComparisons} is true.<br/>
	 * NOTE: Argument {@code convertComparisons} should not be true if {@code s} contains
	 * an entire instruction line rather than just an expression! 
	 * @param s - the expression or instruction line to be pre-processed
	 * @param convertComparisons - whether string comparisons are to be detected and rewritten
	 * @return the converted string
	 */
	private String convert(String s, boolean convertComparisons)
	{
		// START KGU#128 2016-01-07: Bugfix #92 - Effort via tokens to avoid replacements within string literals
		StringList tokens = Element.splitLexically(s, true);
		Element.unifyOperators(tokens, false);
		// START KGU#130 2015-01-08: Bugfix #95 - Conversion of div operator had been forgotten...
		tokens.replaceAll("div", "/");		// FIXME: Operands should better be coerced to integer...
		// END KGU#130 2015-01-08
		// START KGU#285 2016-10-16: Bugfix #276
		// pascal: quotes
		for (int i = 0; i < tokens.count(); i++)
		{
			String token = tokens.get(i);
			// START KGU#342 2017-01-08: Issue #343 We must also escape all internal quotes
			//if (token.length() != 3 && token.startsWith("'") && token.endsWith("'"))
			//{
			//	tokens.set(i, "\"" + token.substring(1, token.length()-1) + "\"");
			//}
			int tokenLen = token.length();
			if (tokenLen >= 2 && (token.startsWith("'") && token.endsWith("'") || token.startsWith("\"") && token.endsWith("\"")))
			{
				char delim = token.charAt(0);
				String internal = token.substring(1, tokenLen-1);
				// Escape all unescaped double quotes
				int pos = -1;
				while ((pos = internal.indexOf("\"", pos+1)) >= 0) {
					if (pos == 0 || internal.charAt(pos-1) != '\\') {
						internal = internal.substring(0, pos) + "\\" + internal.substring(pos);
						pos++;
					}
				}
				// START KGU 2017-04-22 unescaping of double single quotes - no, doesn't make sense
				//if (token.startsWith("'") && internal.length() > 2) {
				//	int intLen = internal.length();
				//	internal = internal.replace("''", "'");
				//	tokenLen -= (intLen - internal.length());
				//}
				// END KGU 2017-04-22
				// START KGU#406/KGU#420 2017-05-23/2017-09-09: Bugfix #411, #426 (arose with COBOL import)
				// The interpreter doesn't cope with unicode escape sequences "\\u000a", "\\u000d", "\\u0022", and "\\u005c"
				//internal = internal.replaceAll("(.*)\\\\u000[aA](.*)", "$1\\\\012$2").
				//		replaceAll("(.*?)\\\\u000[dD](.*?)", "$1\\\\015$2").
				//		replaceAll("(.*?)\\\\u0022(.*?)", "$1\\\\042$2").
				//		replaceAll("(.*?)\\\\u005[cC](.*?)", "$1\\\\134$2");
				for (int mtch = 0; mtch < MTCHs_BAD_UNICODE.length; mtch++) {
					internal = MTCHs_BAD_UNICODE[mtch].reset(internal).replaceAll(RPLCs_BAD_UNICODE[mtch]);
				}
				// END KGU#406/KGU#420 2017-05-23/2017-09-09
				if (!(tokenLen == 3 || tokenLen == 4 && token.charAt(1) == '\\')) {
					delim = '\"';
				}
				tokens.set(i, delim + internal + delim);
			}
			// END KGU#342 2017-01-08
			// START KGU#354 2017-05-22: Unfortunately the interpreter doesn't cope with binary integer literals, so convert them
			else if (MTCH_BIN_LITERAL.reset(token).matches()) {
				tokens.set(i, "" + Integer.parseInt(token.substring(2), 2));
			}
			// END KGU#354 2017-05-22
		}
		// END KGU#285 2016-10-16
		// Function names to be prefixed with "Math."
		final String[] mathFunctions = {
				// START KGU#391 2017-05-07: Enh. #398 We needed a sign function for facilitating COBOL rounding import
				"signum",
				// END KGU#391 2017-05-07
				"cos", "sin", "tan", "acos", "asin", "atan", "toRadians", "toDegrees",
				"abs", "round", "min", "max", "ceil", "floor", "exp", "log", "sqrt", "pow"
				};
		StringList fn = new StringList();
		fn.add("DUMMY");
		fn.add("(");
		for (int f = 0; f < mathFunctions.length; f++)
		{
			int pos = 0;
			fn.set(0, mathFunctions[f]);
			while ((pos = tokens.indexOf(fn, pos, true)) >= 0)
			{
				tokens.set(pos, "Math." + mathFunctions[f]);
			}
		}
		s = tokens.concatenate();
		// END KGU#128 2016-01-07

		// pascal notation to access a character inside a string
		//r = new Regex("(.*)\\[(.*)\\](.*)", "$1.charAt($2-1)$3");
		//r = new Regex("(.*)\\[(.*)\\](.*)", "$1.substring($2-1,$2)$3");
		// MODIFIED BY GENNARO DONNARUMMA, NEXT LINE COMMENTED -->
		// NO REPLACE ANY MORE! CHARAT AND SUBSTRING MUST BE CALLED MANUALLY
		// s = r.replaceAll(s);
		// START KGU#575 2018-09-17: Issue #594 - replacing obsolete 3rd-party Regex library
		//s = RPLC_DELETE_PROC.replaceAll(s);
		//s = RPLC_INSERT_PROC.replaceAll(s);
		// pascal: delete
		s = DELETE_PROC_MATCHER.reset(s).replaceAll(DELETE_PROC_SUBST);
		// pascal: insert
		s = INSERT_PROC_MATCHER.reset(s).replaceAll(INSERT_PROC_SUBST);
		// END KGU#575 2018-09-17
		// START KGU#285 2016-10-16: Bugfix #276 - this spoiled apostrophes because misplaced here
//		// pascal: quotes
//		r = new Regex("([^']*?)'(([^']|'')*)'", "$1\"$2\"");
//		//r = new Regex("([^']*?)'(([^']|''){2,})'", "$1\"$2\"");
//		s = r.replaceAll(s);
		// END KGU#285 2016-10-16
		// START KGU 2015-11-29: Adopted from Root.getVarNames() - can hardly be done in initInterpreter() 
        // pascal: convert "inc" and "dec" procedures
		//s = RPLC_INC2_PROC.replaceAll(s);
		//s = RPLC_INC1_PROC.replaceAll(s);
		//s = RPLC_DEC2_PROC.replaceAll(s);
		//s = RPLC_DEC1_PROC.replaceAll(s);
		s = Element.transform_inc_dec(s);
        // END KGU 2015-11-29
		
        // START KGU 2017-04-22: now done above in the string token conversion
		//s = s.replace("''", "'");	// (KGU 2015-11-29): Looks like an unwanted relic!
        // END KGU 2017-04-22
		// pascal: randomize
		s = s.replace("randomize()", "randomize");
		s = s.replace("randomize", "randomize()");

		// clean up ... if needed
		s = s.replace("Math.Math.", "Math.");

		if (convertComparisons)
		{
			// This should only be applied to an expression in s, not to an entire instruction line!
			// KGU#490 / bugfix #503: this is now to be ensured by the caller of convert()
			s = convertStringComparison(s);
		}

		// System.out.println(s);
		return s;
	}
	
	// START KGU#57 2015-11-07
	private String convertStringComparison(String str)
	{
//		Character chA = 'a';
//		Character chB = 'a';
//		System.out.println("Zeichen sind " + ((chA == chB) ? "" : "NICHT ") + "identisch!");
//		System.out.println("Zeichen sind " + ((chA.equals(chB)) ? "" : "NICHT ") + "gleich!");
		// Is there any equality test at all?
		// START KGU#76 2016-04-25: Issue #30 - convert all string comparisons
		//if (str.indexOf(" == ") >= 0 || str.indexOf(" != ") >= 0)
		String[] compOps = {"==", "!=", "<=", ">=", "<", ">"};
		boolean containsComparison = false;
		for (int op = 0; !containsComparison && op < compOps.length; op++)
		{
			containsComparison = str.indexOf(compOps[op]) >= 0;
		}
		if (containsComparison)
		// END KGU#76 2016-04-25
		{
			// START KGU#612 2018-12-12: Bugfix #642 - operator symbols weren't reliably detected
			//// We are looking for || operators and split the expression by them (if present)
			//// START KGU#490 2018-02-07: Bugfix #503 - the regex precaution was wrong here
			////StringList exprs = StringList.explodeWithDelimiter(str, " \\|\\| ");	// '|' is a regex metasymbol!
			//StringList exprs = StringList.explodeWithDelimiter(str, " || ");	// The delimiter is no regex here!!
			//// END KGU#490 2018-02-07
			//// Now we do the same with && operators
			//exprs = StringList.explodeWithDelimiter(exprs, " && ");
			StringList allTokens = Element.splitLexically(str, true);
			StringList exprs = new StringList();
			int lastI = 0;
			for (int i = 0; i < allTokens.count(); i++) {
				String token = allTokens.get(i);
				if (token.equals("||") || token.equals("&&")) {
					exprs.add(allTokens.subSequence(lastI, i).concatenate());
					exprs.add(token);
					lastI = i+1;
				}
			}
			exprs.add(allTokens.subSequence(lastI, allTokens.count()).concatenate());
			// END KGU#612 2018-12-12
			// Now we should have some "atomic" assertions, among them comparisons
			boolean replaced = false;
			for (int i = 0; i < exprs.count(); i++)
			{
				String s = exprs.get(i);
				// START KGU#76 2016-04-25: Issue #30 - convert all string comparisons
				//String[] eqOps = {"==", "!="};
				//for (int op = 0; op < eqOps.length; op++)
				StringList tokens = Element.splitLexically(s.trim(), true);
				for (int op = 0; op < compOps.length; op++)
				// END KGU#76 2016-04-25
				{
					// START KGU#76 2016-04-25: Issue #30
					//Regex r = null;
					// We can no longer expect operators to be padded, better use tokens
					//if (!s.equals(" " + eqOps[op] + " ") && s.indexOf(eqOps[op]) >= 0)
					int opPos = -1;		// Operator position
					if ((opPos = tokens.indexOf(compOps[op])) >= 0)
					{
						String leftParenth = "";
						String rightParenth = "";
						// Get the left operand expression
						// START KGU#76 2016-04-25: Issue #30
						//r = new Regex("(.*)"+eqOps[op]+"(.*)", "$1");
						//String left = r.replaceAll(s).trim();	// All? Really? And what is the result supposed to be then?
						String left = tokens.concatenate("", 0, opPos).trim();
						// END KGU#76 2016-04-25
						// Re-balance parentheses
						while (Function.countChar(left, '(') > Function.countChar(left, ')') &&
								left.startsWith("("))
						{
							leftParenth = leftParenth + "(";
							left = left.substring(1).trim();
						}
						// Get the right operand expression
						// START KGU#76 2016-04-25: Issue #30
						//r = new Regex("(.*)"+eqOps[op]+"(.*)", "$2");
						//String right = r.replaceAll(s).trim();
						String right = tokens.concatenate("", opPos+1).trim();
						// END KGU#76 2016-04-25
						// Re-balance parentheses
						while (Function.countChar(right, ')') > Function.countChar(right, '(') &&
								right.endsWith(")"))
						{
							rightParenth = rightParenth + ")";
							right = right.substring(0, right.length()-1).trim();
						}
						// ---- thanks to autoboxing, we can always use the "equals" method
						// ---- to compare things ...
						// addendum: sorry, doesn't always work.
						try
						{
							int pos = -1;	// some character position
							Object leftO = this.evaluateExpression(left, false, false);
							Object rightO = this.evaluateExpression(right, false, false);
							String neg = (op > 0) ? "!" : "";
							// First the obvious case: two String expressions
							if ((leftO instanceof String) && (rightO instanceof String))
							{
								// START KGU#76 2016-04-25: Issue #30 support all string comparison
								//exprs.set(i, leftParenth + neg + left + ".equals(" + right + ")" + rightParenth);
								exprs.set(i, leftParenth + left + ".compareTo(" + right + ") " + compOps[op] + " 0" + rightParenth);
								// END KGU#76 2016-04-25
								replaced = true;
							}
							// We must make single-char strings comparable with characters, since it
							// doesn't work automatically and several conversions have been performed 
							else if ((leftO instanceof String) && (rightO instanceof Character))
							{
								// START KGU#76 2016-04-25: Issue #30 support all string comparison
								//exprs.set(i, leftParenth + neg + left + ".equals(\"" + (Character)rightO + "\")" + rightParenth);
								// START KGU#342 2017-02-09: Bugfix #343 - be aware of characters to be escaped
								//exprs.set(i, leftParenth + left + ".compareTo(\"" + (Character)rightO + "\") " + compOps[op] + " 0" + rightParenth);
								exprs.set(i, leftParenth + left + ".compareTo(\"" + this.literalFromChar((Character)rightO) + "\") " + compOps[op] + " 0" + rightParenth);
								// END KGU#342 2017-02-09
								// END KGU#76 2016-04-25
								replaced = true;								
							}
							else if ((leftO instanceof Character) && (rightO instanceof String))
							{
								// START KGU#76 2016-04-25: Issue #30 support all string comparison
								//exprs.set(i, leftParenth + neg + right + ".equals(\"" + (Character)leftO + "\")" + rightParenth);
								// START KGU#342 2017-02-09: Bugfix #343 - be aware of characters to be escaped
								//exprs.set(i, leftParenth + "\"" + (Character)leftO + "\".compareTo(" + right + ") " + compOps[op] + " 0" + rightParenth);
								exprs.set(i, leftParenth + "\"" + this.literalFromChar((Character)leftO) + "\".compareTo(" + right + ") " + compOps[op] + " 0" + rightParenth);
								// END KGU#342 2017-02-09
								// END KGU#76 2016-04-25
								replaced = true;								
							}
							// START KGU#99 2015-12-10: Bugfix #49 (also replace if both operands are array elements (objects!)
							// START KGU#76 2016-04-25: Issue #30 - this makes only sense for "==" and "!="
							//else if ((pos = left.indexOf('[')) > -1 && left.indexOf(']', pos) > -1 && 
							else if (op < 2 &&
									(pos = left.indexOf('[')) > -1 && left.indexOf(']', pos) > -1 && 
							// END KGU#76 2016-04-25
									(pos = right.indexOf('[')) > -1 && right.indexOf(']', pos) > -1)
							{
								exprs.set(i, leftParenth + neg + left + ".equals(" + right + ")" + rightParenth);
								replaced = true;								
							}
							// END KGU#99 2015-12-10
						}
						catch (EvalError ex)
						{
							logger.log(Level.WARNING, "convertStringComparison(\"{0}\"): {1}", new Object[]{str, ex.getMessage()});
						}
						catch (Exception ex)
						{
							logger.log(Level.WARNING, "convertStringComparison(\"{0}\"): {1}", new Object[]{str, ex.getMessage()});
						}
					} // if (!s.equals(" " + eqOps[op] + " ") && (s.indexOf(eqOps[op]) >= 0))
				} // for (int op = 0; op < eqOps.length; op++)
				if (replaced)
				{
					// START KGU#490 2018-02-07: Bugfix #503 - the regex escaping was wrong (see above)
					//// Compose the partial expressions and undo the regex escaping for the initial split
					//str = exprs.getLongString().replace(" \\|\\| ", " || ");
					str = exprs.getLongString();
					// END KGU#490 2018-02-07
					str.replace("  ", " ");	// Get rid of multiple spaces
				}
			}
		}
		return str;
	}
	// END KGU#57 2015-11-07
	
	// START KGU#342 2017-02-09: Bugfix #343
	private String literalFromChar(char ch) {
		String literal = Character.toString(ch);
		if ("\"\'\\\b\f\n\r\t".indexOf(ch) >= 0) {
			literal = "\\" + literal;
		}
		return literal;
	}
	// END KGU#342 2017-02-09

	private void delay()
	{
		if (delay != 0)
		{
			diagram.redraw();
			try
			{
				Thread.sleep(delay);
			} catch (InterruptedException e)
			{
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
		waitForNext();
	}

	/**
	 * @param aStep
	 *            the step to set
	 */
	public void doStep()
	{
		synchronized (this)
		{
			paus = false;
			step = true;
			this.notify();
		}
	}
	
	// START KGU#117 2016-03-08: Enh. #77
	/**
	 * Clears the execution status of all routines held by known
	 * subroutine pools  
	 */
	public void clearPoolExecutionStatus()
	{
		Iterator<IRoutinePool> iter = this.routinePools.iterator();
		while (iter.hasNext())
		{
			iter.next().clearExecutionStatus();
		}
		this.diagram.clearExecutionStatus();
		// START KGU#156 2016-03-10: Enh. #124
		if (!Element.E_COLLECTRUNTIMEDATA)
		{
			Element.resetMaxExecCount();
		}
		// END KGU#156 2016-03-10
	}
	// END KGU#117 2016-03-08

	// METHOD MODIFIED BY GENNARO DONNARUMMA

	public void execute()
	// START KGU#2 (#9) 2015-11-13: We need a recursively applicable version
	{
		Root root = this.diagram.getRoot();
		this.callers.clear();
		this.stackTrace.clear();
		this.routinePools.clear();
		// START KGU#376 2017-04-22: Enh. #389
		this.importMap.clear();
		// END KGU#376 2017-04-22
		// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
		//this.forLoopVars.clear();	// KGU#384 2017-04-22 -> new context
		// END KGU#307 2016-12-12
		// START KGU#375 2017-03-30: Enh. #388: Keep track of constants
		//this.constants.clear();	// KGU#384 2017-04-22 -> new context
		// END KGU#375 2017-03-30
		// START KGU#311 2016-12-18: Enh. #314
		for (Closeable file: this.openFiles) {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					logger.log(Level.WARNING, "openFiles -> {0}", e.getLocalizedMessage());
				}
			}
		}
		this.openFiles.clear();
		// END KGU#311 2016-12-18

		if (Arranger.hasInstance())
		{
			this.routinePools.addElement(Arranger.getInstance());
			// START KGU#117 2016-03-08: Enh. #77
			Arranger.getInstance().clearExecutionStatus();
			// END KGU#117 2016-03-08
		}
		this.isErrorReported = false;
		root.isCalling = false;
		// START KGU#686 2019-03-17: Enh. #56
		this.withinTryBlock = false;
		this.subroutineTrouble = null;
		this.isExited = false;
		// END KGU#686 2019-03-17
		// START KGU#160 2016-04-12: Enh. #137 - Address the console window
		// START KGU#569 2018-08-08: Issue #577: Replace the console if it has become inconsistent
		//this.console.clear();
		try {
			this.console.clear();			
		}
		catch (NullPointerException ex) {
			this.console.setVisible(false);
			this.console.dispose();
			this.console = new OutputConsole();
		}
		// END KGU#569 2018-08-08
		SimpleDateFormat sdf = new SimpleDateFormat();
		if (this.console.logMeta()) {
			this.console.writeln("*** STARTED \"" + root.getText().getLongString() +
					"\" at " + sdf.format(System.currentTimeMillis()) + " ***", Color.GRAY);
		}
		if (this.isConsoleEnabled) this.console.setVisible(true);
		// END KGU#160 2016-04-12
		// START KGU#384 2017-04-22
		this.context = new ExecutionContext(root);
		initInterpreter();
		// END KGU#384 2017-04-22
		/////////////////////////////////////////////////////////
		this.execute(null);	// The actual top-level execution
		/////////////////////////////////////////////////////////
		this.callers.clear();
		this.stackTrace.clear();
		// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
		this.context.forLoopVars.clear();
		// END KGU#307 2016-12-12
		// START KGU 2016-12-18: Enh. #314
		for (Closeable file: this.openFiles) {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					logger.log(Level.WARNING, "openFiles -> {0}", e.getLocalizedMessage());
				}
			}
		}
		this.openFiles.clear();
		// END KGU 2016-12-18
		// START KGU#160 2016-04-12: Enh. #137 - Address the console window 
		if (this.console.logMeta()) {
			this.console.writeln("*** TERMINATED \"" + root.getText().getLongString() +
					"\" at " + sdf.format(System.currentTimeMillis()) + " ***", Color.GRAY);
		}
		if (this.isConsoleEnabled) this.console.setVisible(true);
		// END KGU#160 2016-04-12
		// START KGU#705 2019-09-24: Enh. #738
		diagram.updateCodePreview();
		// END KGU#705 2019-09-24
		//System.out.println("stackTrace size: " + stackTrace.count());
	}
	
	/**
	 * Executes the current diagram held by this.diagram, applicable for main or sub routines.<br/>
	 * If called within a Try execution (@link #withinTryBlock} then a possible error message will
	 * be put into {@link #subroutineTrouble}, otherwise the error message will be popped up as
	 * message box. Flag {@link #isErrorReported} will be set in both cases.
	 * @param arguments - list of interpreted argument values or null (if main program)
	 * @return whether the call was successful
	 */
	private boolean execute(Object[] arguments)
	{
		boolean successful = true;
	// END KGU#2 (#9) 2015-11-13
		
		// START KGU#384 2017-04-22: execution context redesign
		//Root root = diagram.getRoot();
		Root root = context.root;
		// END 2017-04-22

		// START KGU#159 2016-03-17: Now we permanently maintain the stacktrace, not only in case of error
		//addToStackTrace(root, arguments);	// KGU 2017-02-17 moved downwards, after the argument request
		// END KGU#159 2016-03-17
		
		// START KGU#2 (#9) 2015-11-14
		Iterator<Updater> iter = root.getUpdateIterator();
		while (iter.hasNext())
		{
			Updater pool = iter.next();
			if (pool instanceof IRoutinePool && !this.routinePools.contains((IRoutinePool)pool))
			{
				this.routinePools.addElement((IRoutinePool)pool);
			}
		}
		// END KGU#2 (#9) 2015-11-14

		boolean analyserState = diagram.getAnalyser();
		diagram.setAnalyser(false);
		// START KGU 2015-10-11/13:
		// Unselect all elements before start!
		//diagram.unselectAll();	// KGU 2016-03-08: There is no need anymore
		// Reset all execution state remnants (just for sure)
		// START KGU#430 2017-10-12: Issue #432 reduce redraw() calls at least if delay = 0
		//diagram.clearExecutionStatus();
		if (delay > 0) {
			diagram.clearExecutionStatus();
		}
		else {
			root.clearExecutionStatus(); // Avoid refresh
		}
		// END KGU#430 2017-10-12
		// END KGU 2015-10-11/13
		// START KGU#376 2017-04-11: Enh. #389 - Must no longer be done here but in execute() and executeCall()
		//initInterpreter();
		// END KGU#376 2017-04-11
		String trouble = "";
		// START KGU#384 2017-04-22: Holding all execution context in this.context now
		//returned = false;
		// START KGU#78 2015-11-25
		//loopDepth = 0;
		leave = 0;
		// END KGU#78 2015-11-25
		// END KGU#384 207-04-22
		
		// START KGU#376 2017-07-01: Enh. #389 - perform all specified includes
		trouble = importSpecifiedIncludables(root);
		// END KGU#376 2017-07-01

		// START KGU#39 2015-10-16 (1/2): It made absolutely no sense to look for parameters if root is a program
		if (root.isSubroutine() && trouble.isEmpty())
		{
		// END KGU#39 2015-10-16 (1/2)
			// START KGU#371 2019-03-07: Enh. #385 be aware of possible default values
			//StringList params = root.getParameterNames();
			//System.out.println("Having: "+params.getCommaText());
			// START KGU#375 2017-03-30: Enh. #388 - support a constant concept
			//StringList pTypes = root.getParameterTypes();
			// END KGU#375 2017-03-30
			StringList params = new StringList();
			StringList pTypes = new StringList();
			StringList pDefaults = new StringList();
			root.collectParameters(params, pTypes, pDefaults);
			// END KGU#371 2019-03-07
			// START KGU#2 2015-12-05: New mechanism of getParameterNames() made reverting wrong
			//params=params.reverse();
			// END KGU#2 2015-12-05
			//System.out.println("Having: "+params.getCommaText());
			// START KGU#2 2015-11-24
			boolean noArguments = arguments == null;
			if (noArguments) arguments = new Object[params.count()];
			// END KGU#2 2015-11-24
			for (int i = 0; i < params.count(); i++)
			{
				String in = params.get(i);
				// START KGU#375 2017-03-30: Enh. #388 - support a constant concept
				String type = pTypes.get(i);
				boolean isConstant = type != null && (type.toLowerCase() + " ").startsWith("const ");
				// END KGU#375 2017-03-30
				// START KGU#388 2017-09-18: Enh. #423 Track at least record types
				if (type != null) {
					StringList typeTokens = Element.splitLexically(type, true);
					typeTokens.removeAll(" ");
					if (isConstant) {
						typeTokens.remove(0);
					}
					if (typeTokens.count() == 1 && context.dynTypeMap.containsKey(":" + (type = typeTokens.get(0)))) {
						context.dynTypeMap.put(in, context.dynTypeMap.get(":" + type));
					}
				}
				// END KGU#388 2017-09-18
				
				// START KGU#2 (#9) 2015-11-13: If root was not called then ask the user for values
				if (noArguments)
				{
				// END KGU#2 (#9) 2015-11-13
					// START KGU#89 2016-03-18: More language support 
					//String str = JOptionPane.showInputDialog(null,
					//		"Please enter a value for <" + in + ">", null);
					String msg = control.lbInputValue.getText();
					msg = msg.replace("%", in);
					// START KGU#371 2019-03-07: Enh. #385 - offer a default value if available
					//String str = JOptionPane.showInputDialog(diagram.getParent(), msg, null);
					String str = JOptionPane.showInputDialog(diagram.getParent(), msg, pDefaults.get(i));
					// END KGU#371 2019-03-07
					// END KGU#89 2016-03-18
					if (str == null)
					{
						//i = params.count();	// leave the loop
						// START KGU#197 2016-07-27: Enhanced localization
						//trouble = "Manual break!";
						trouble = control.msgManualBreak.getText();
						// END KGU#197 2016-07-27
						// START KGU#371 2019-03-07: Enh. #385
						str = pDefaults.get(i);
						// END KGU#371 2019-03-07
						break;
					}
					try
					{
						// START KGU#69 2015-11-08 What we got here is to be regarded as raw input
						// START KGU#375 2017-03-30: Enh. 388: Support a constant concept (KGU#580 2018-09-24 corrected)
						String varName = setVarRaw(in, str);
						if (isConstant) {
							this.context.constants.put(varName, this.context.interpreter.get(varName));
							this.updateVariableDisplay();
						}
						// END KGU#375 2017-03-30
						// END KGU#69 2015-11-08
						// START KGU#2 2015-11-24: We might need the values for a stacktrace
						arguments[i] = context.interpreter.get(in);
						// END KGU#2 2015-11-24
						// START KGU#160 2016-04-26: Issue #137 - document the arguments
						if (this.console.logMeta()) {
							this.console.writeln("*** Argument <" + in + "> = " + prepareValueForDisplay(arguments[i], context.dynTypeMap), Color.CYAN);
						}
						// END KGU#160 2016-04-26
					} catch (EvalError ex)
					{
						trouble = ex.getLocalizedMessage();
						if (trouble == null) trouble = ex.getMessage();
						break;
					}
				// START KGU#2 (#9) 2015-11-13: If root was called then just assign the arguments
				}
				else
				{
					try
					{
						// START KGU#375 2017-03-30: Enh. 388: Support a constant concept
						//setVar(in, arguments[i]);
						// START KGU#371 2019-03-07: Enh. #385: Cope with default values
						//if (isConstant) {
						//	setVar("const " + in, arguments[i]);
						//}
						//else {
						//	setVar(in, arguments[i]);
						//}
						if (isConstant) {
							in = "const " + in;
						}
						if (i < arguments.length) {
							setVar(in, arguments[i]);
						}
						else {
							setVarRaw(in, pDefaults.get(i));
						}
						// END KGU#371 2019-03-07
						// END KGU#375 2017-03-30
					}
					catch (EvalError ex)
					{
						trouble = ex.getLocalizedMessage();
						if (trouble == null) trouble = ex.getMessage();
						break;
					}
				}
				// END KGU#2 (#9) 2015-11-13
			}
		// START KGU#39 2015-10-16
		}
		// END KGU#39 2015-10-16
		// START KGU#376 2017-04-22: Enh. #389 - without arguments, we must also show the new context 
		try {
			this.updateVariableDisplay();
		} catch (EvalError ex) {}
		// END KGU#376 2017-04-22

		// START KGU#159 2017-02-17: Now we permanently maintain the stacktrace, not only in case of error
		addToStackTrace(root, arguments);
		// END KGU#159 2017-03-17
	
		if (trouble.equals(""))
		{
			/////////////////////////////////////////////////////
			// Actual start of execution 
			/////////////////////////////////////////////////////
			trouble = step(root);
			
			if (trouble.equals("") && (stop == true))
			{
				// START KGU#197 2016-07-27: Enhanced localization
				//trouble = "Manual break!";
				trouble = control.msgManualBreak.getText();
				// END KGU#197 2016-07-27
			}
		}

		// START KGU#430 2017-10-12: Issue #432 reduce redraw() calls with delay 0 (KGU#558: unless we are in step mode)
		//diagram.redraw();
		if (delay > 0 || step) {
			diagram.redraw();
		}
		// END KGU#430 2017-10-12
		if (!trouble.equals(""))
		{
			// START KGU#2 (#9) 2015-11-13
			successful = false;
			// END KGU#2 (#9) 2015-11-13
			
			// MODIFIED BY GENNARO DONNARUMMA, ADDED ARRAY ERROR MSG
			
			String modifiedResult = trouble;
			/* FIXME (KGU): If the interpreter happens to provide localized messages then this won't work anymore!
			 * ... and after having replaced actual arrays by ArrayLists we may no longer obtain this type of message */
			if (trouble.contains("Not an array"))
			{
				modifiedResult = modifiedResult.concat(" or the index "
						+ modifiedResult.substring(
								modifiedResult.indexOf("[") + 1,
								modifiedResult.indexOf("]"))
						+ " is out of bounds (invalid index)");
				trouble = modifiedResult;
			}

			// START KGU#2 2015-11-22: If we are on a subroutine level, then we must stop the show
			//JOptionPane.showMessageDialog(diagram, trouble, "Error",
			//		JOptionPane.ERROR_MESSAGE);

			// START KGU#686 2019-03-17: Enh. #56: don't panic if we are within a try block
			if (!this.withinTryBlock && !stop) {
			// END KGU#686 2019-03-17
		
				if (!isErrorReported)
				{
					JOptionPane.showMessageDialog(diagram.getParent(), trouble, control.msgTitleError.getText(),
							JOptionPane.ERROR_MESSAGE);
					// START KGU#160 2016-07-27: Issue #137 - also log the trouble to the console
					this.console.writeln("*** " + trouble, Color.RED);
					// END KGU#160 2016-07-27
					isErrorReported = true;
				}
				if (!this.callers.isEmpty())
				{
					stop = true;
					paus = false;
					step = false;
				}
				else if (isErrorReported && stackTrace.count() > 1)
				{
					// START KGU#159 2016-03-17: Now we permanently maintain the stacktrace, so there is no need anymore
					//addToStackTrace(root, arguments);
					// END KGU#159 2016-03-17
					showStackTrace();
				}
			
			// START KGU#686 2019-03-17: Enh. #56: don't panic if we are within a try block
			}
			else {
				if (!this.isErrorReported && this.console.logMeta()) {
					this.console.writeln("*** " + Control.msgErrorInSubroutine.getText().
							replace("%1", this.stackTrace.get(this.stackTrace.count()-1)).
							replace("%2", Integer.toString(this.stackTrace.count()-1)).
							replace("%3", trouble), Color.RED);
				}
				this.subroutineTrouble = trouble;
				this.isErrorReported = true;
			}
			// END KGU#686 2019-03-17
			
			// END KGU#2 2015-11-24
		} else
		{
			if (root.isSubroutine() && (context.returned == false))
			{
				// Possible result variable names
				StringList posres = new StringList();
				// START KGU#434 2017-10-10: Bugfix #433 The name must have existed as variable in this context, too
				// It happened that e.g. a Java object like java.awt.Polygon was "found" as result for diagram "Polygon"
				//posres.add(root.getMethodName());
				//posres.add("result");
				//posres.add("RESULT");
				//posres.add("Result");
				for (String resCand: new String[]{root.getMethodName(), "result", "RESULT", "Result"}) {
					if (context.variables.contains(resCand)) {
						posres.add(resCand);
					}
				}
				// END KGU#434 2017-10-10

				try
				{
					int i = 0;
					while ((i < posres.count()) && (!context.returned))
					{
						Object resObj = context.interpreter.get(posres.get(i));
						if (resObj != null)
						{
							// START KGU#2 (#9) 2015-11-13: Only tell the user if this wasn't called
							//JOptionPane.showMessageDialog(diagram, n,
							//		"Returned result", 0);
							context.returnedValue = resObj;
							if (this.callers.isEmpty())
							{
								// START KGU#197 2016-05-25: Translate the headline!
								String header = control.lbReturnedResult.getText();
								// END KGU#197 2016-05-25
								// START KGU#133 2016-01-09: Show large arrays in a listview
								//JOptionPane.showMessageDialog(diagram, n,
								//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
								// KGU#133 2016-01-29: Arrays now always shown as listview (independent of size)
								// START KGU#439 2017-10-13: Issue #436
								//if (resObj instanceof Object[] /*&& ((Object[])resObj).length > 20*/)
								//{
								//	// START KGU#147 2016-01-29: Enh. #84 - interface changed for more flexibility
								//	//showArray((Object[])resObj, "Returned result");
								//	showArray((Object[])resObj, header, !step);
								//	// END KGU#147 2016-01-29
								//}
								if (resObj instanceof ArrayList<?> || resObj instanceof HashMap<?,?>)
								{
									showCompoundValue(resObj, header, !step);
								}
								// END KGU#439 2017-10-13
								// START KGU#84 2015-11-23: Enhancement to give a chance to pause (though of little use here)
								//else
								//{
									//JOptionPane.showMessageDialog(diagram, resObj,
									//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
								//}
								else if (step)
								{
									// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
									if (this.console.logMeta()) {
										this.console.writeln("*** " + header + ": " + prepareValueForDisplay(resObj, context.dynTypeMap), Color.CYAN);
									}
									// END KGU#160 2016-04-26
									JOptionPane.showMessageDialog(diagram.getParent(), resObj,
											header, JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									// START KGU#198 2016-05-25: Issue #137 - also log the result to the console
									if (this.console.logMeta()) {
										this.console.writeln("*** " + header + ": " + prepareValueForDisplay(resObj, context.dynTypeMap), Color.CYAN);
									}
									// END KGU#198 2016-05-25
									Object[] options = {
											Control.lbOk.getText(),
											Control.lbPause.getText()
											};
									int pressed = JOptionPane.showOptionDialog(diagram.getParent(), resObj, header,
											JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
									if (pressed == 1)
									{
										paus = true;
										step = true;
										// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
										//control.setButtonsForPause();
										control.setButtonsForPause(false);	// This avoids interference with the pause button
										// END KGU#379 2017-04-12
									}
								}
								// END KGU#84 2015-11-23
								// END KGU#133 2016-01-09
							}
							// START KGU#148 2016-01-29: Pause now here, particularly for subroutines
							delay();
							// END KGU#148 2016-01-29							
							// END KGU#2 (#9) 2015-11-13
							context.returned = true;
						}
						i++;
					}
				} catch (EvalError ex)
				{
					Logger.getLogger(Executor.class.getName()).log(
							Level.SEVERE, null, ex);
				}

			}
			// START KGU#299 2016-11-23: Enh. #297 In step mode, this offers a last pause to inspect variables etc.
			if (this.callers.isEmpty() && !context.returned) {
				delay();
			}
			// END KGU 2016-11-23

		}
		// START KGU 2015-10-13: Unsets all execution flags in the diagram
		// START KGU#430 2017-10-12: Issue #432 Reduce redraw() calls at least if delay = 0
		//diagram.clearExecutionStatus();
		if (delay > 0) {
			diagram.clearExecutionStatus();
		}
		else {
			context.root.clearExecutionStatus();
		}
		// END KGU#430 2017-10-12
		// END KGU 2015-10-13
		diagram.setAnalyser(analyserState);

		// START KGU#686 2019-03-17: Enh. #56 - do the stack unwinding also in case of a tried execution
		//if (successful)
		if (successful || this.withinTryBlock)
		// END KGU#686 2019-03-56
		{
			dropFromStackTrace();
		}
		
		// START KGU#2 (#9) 2015-11-13: Need the status
		return successful;
		// END KGU# (#9) 2015-11-13
	}
	
	// START KGU#376 2017-07-01: Enh. #389 - perform all specified includes
	private String importSpecifiedIncludables(Root root) {
		String errorString = "";
		if (root.includeList != null) {
			root.waited = true;
			root.isIncluding = true;
			for (int i = 0; errorString.isEmpty() && i < root.includeList.count(); i++) {
				delay();
				Root imp = null;
				String diagrName = root.includeList.get(i);
				try {
					imp = this.findIncludableWithName(diagrName);
				}
				catch (ConcurrentModificationException ex) {
					ex.printStackTrace();
					return ex.toString();
				}
				catch (Exception ex) {
					// Likely to be an ambiguous call, but might be something else
					String msg = ex.getMessage();
					if (msg == null) {
						msg = ex.toString();
					}
					return msg;
				}
				if (imp != null)
				{
					// START KGU#376 2017-04-21: Enh. #389
					// Has this import already been executed -then just adopt the results
					if (this.importMap.containsKey(imp)) {
						ImportInfo impInfo = this.importMap.get(imp);
						this.copyInterpreterContents(impInfo.interpreter, context.interpreter,
								// START KGU#843 2020-04-13: Bugfix #848 Merely declared variables must also be considered
								//imp.getCachedVarNames(), imp.constants.keySet(), false);
								impInfo.variableNames, imp.constants.keySet(), false);
								// END KGU#843 2020-04-13
						// START KGU#388 2017-09-18: Enh. #423
						// Adopt the imported typedefs if any
						for (Entry<String, TypeMapEntry> typeEntry: impInfo.typeDefinitions.entrySet()) {
							TypeMapEntry oldEntry = context.dynTypeMap.putIfAbsent(typeEntry.getKey(), typeEntry.getValue());
							if (oldEntry != null) {
								logger.log(Level.INFO, "Conflicting type entry {0} from Includable {1}", new Object[]{typeEntry.getKey(), diagrName});
							}
						}
						// END KGU#388 2017-09-18
						context.variables.addIfNew(impInfo.variableNames);
						for (String constName: imp.constants.keySet()) {
							// FIXME: Is it okay just to ignore conflicting constants?
							if (!context.constants.containsKey(constName)) {
								try {
									context.constants.put(constName, impInfo.interpreter.get(constName));
								} catch (EvalError e) {
									if (!errorString.isEmpty()) {
										errorString += "\n";
									}
									errorString += e.getMessage();
								}
							}
						}
						try 
						{
							updateVariableDisplay();
						}
						catch (EvalError ex) {}
					}
					else {
						// END KGU#376 2017-04-21
						executeCall(imp, null, null);
						// START KGU#686 2019-03-17: Enh. #56 might have caused a caught trouble
						if (subroutineTrouble != null) {
							errorString = subroutineTrouble;
							subroutineTrouble = null;
						}
						// END KGU#686 2019-03-17
					}
					context.importList.addIfNew(diagrName);
				}
				else
				{
					// START KGU#197 2016-07-27: Now translatable message
					//trouble = "A subroutine diagram " + f.getName() + " (" + f.paramCount() + 
					//		" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";
					errorString = control.msgNoInclDiagram.getText().
							replace("%", diagrName);
					// END KGU#197 2016-07-27
				}
			}
			if (errorString.isEmpty()) {
				root.waited = false;
				root.isIncluding = false;
			}
		}
		return errorString;
	}
	// END KGU#376 2017-07-01

	// START KGU#133 2016-01-09: New method for presenting result arrays as scrollable list
	// START KGU#147 2016-01-29: Enh. #84 - interface enhanced, pause button added
	//private void showArray(Object[] _array, String _title)
//	@Deprecated
//	private void showArray(Object[] _array, String _title, boolean withPauseButton)
//	// END KGU#147 2016-01-29
//	{	
//		JDialog arrayView = new JDialog();
//		arrayView.setTitle(_title);
//		arrayView.setIconImage(IconLoader.ico004.getImage());
//		arrayView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		// START KGU#147 2016-01-29: Enh. #84 (continued)
//		JButton btnPause = new JButton("Pause");
//		btnPause.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) 
//			{
//				step = true; paus = true; control.setButtonsForPause(true);
//				if (event.getSource() instanceof JButton)
//				{
//					Container parent = ((JButton)(event.getSource())).getParent();
//					while (parent != null && !(parent instanceof JDialog))
//					{
//						parent = parent.getParent();
//					}
//					if (parent != null) {
//						((JDialog)parent).dispose();
//					}
//				}
//			}
//		});
//		arrayView.getContentPane().add(btnPause, BorderLayout.NORTH);
//		btnPause.setVisible(withPauseButton);
//		// END KGU#147 2016-01-29
//		// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
//		this.console.writeln("*** " + _title + ":", Color.CYAN);
//		// END KGU#160 2016-04-26
//		List arrayContent = new List(10);
//		for (int i = 0; i < _array.length; i++)
//		{
//			// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
//			String valLine = "[" + i + "]  " + prepareValueForDisplay(_array[i]);
//			this.console.writeln("\t" + valLine, Color.CYAN);
//			// END KGU#160 2016-04-26
//			arrayContent.add(valLine);
//		}
//		arrayView.getContentPane().add(arrayContent, BorderLayout.CENTER);
//		arrayView.setSize(300, 300);
//		arrayView.setLocationRelativeTo(control);
//		arrayView.setModalityType(ModalityType.APPLICATION_MODAL);
//		arrayView.setVisible(true);
//	}
//	// END KGU#133 2016-01-09

	// START KGU#439 2017-10-13: Enh. #436
	private void showCompoundValue(Object _arrayOrRecord, String _title, boolean withPauseButton)
	{	
//		JDialog arrayView = new JDialog();
//		arrayView.setTitle(_title);
//		arrayView.setIconImage(IconLoader.ico004.getImage());
//		arrayView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// START KGU#147 2016-01-29: Enh. #84 (continued)
		//JButton btnPause = new JButton("Pause");
		JButton btnPause = null;
		if (withPauseButton) {
			btnPause = new JButton();
			btnPause.setIcon(IconLoader.getIconImage(getClass().getResource("/lu/fisch/structorizer/executor/pause.png"))); // NOI18N
			btnPause.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) 
				{
					step = true; paus = true; control.setButtonsForPause(true);
					if (event.getSource() instanceof JButton)
					{
						Container parent = ((JButton)(event.getSource())).getParent();
						while (parent != null && !(parent instanceof JDialog))
						{
							parent = parent.getParent();
						}
						if (parent != null) {
							((JDialog)parent).dispose();
						}
					}
				}
			});
		}
//		arrayView.getContentPane().add(btnPause, BorderLayout.NORTH);
//		btnPause.setVisible(withPauseButton);
		
		// END KGU#147 2016-01-29
		// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
		if (this.console.logMeta()) {
			this.console.writeln("*** " + _title + ":", Color.CYAN);
		}
		// END KGU#160 2016-04-26
		List arrayContent = new List(10);
		if (_arrayOrRecord instanceof ArrayList<?>) {
			@SuppressWarnings("unchecked")
			ArrayList<Object> array = (ArrayList<Object>)_arrayOrRecord;
			for (int i = 0; i < array.size(); i++)
			{
				String valLine = "[" + i + "]  " + prepareValueForDisplay(array.get(i), context.dynTypeMap);
				// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
				if (this.console.logMeta()) {
					this.console.writeln("\t" + valLine, Color.CYAN);
				}
				// END KGU#160 2016-04-26
				arrayContent.add(valLine);
			}
		}
		else if (_arrayOrRecord instanceof HashMap<?,?>) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> record = (HashMap<String, Object>)_arrayOrRecord;
			if (record.containsKey("§TYPENAME§")) {
				String valLine = "== " + record.get("§TYPENAME§") + " ==";
				if (this.console.logMeta()) {
					this.console.writeln("\t" + valLine, Color.CYAN);
				}
				arrayContent.add(valLine);				
			}
			for (Entry<String, Object> entry: record.entrySet())
			{
				if (!entry.getKey().startsWith("§")) {
					String valLine = entry.getKey() + ":  " + prepareValueForDisplay(entry.getValue(), context.dynTypeMap);
					if (this.console.logMeta()) {
						this.console.writeln("\t" + valLine, Color.CYAN);
					}
					arrayContent.add(valLine);
				}
			}
		}
		else {
			String valLine = prepareValueForDisplay(_arrayOrRecord, context.dynTypeMap);
			if (this.console.logMeta()) {
				this.console.writeln("\t" + valLine, Color.CYAN);
			}
			arrayContent.add(valLine);
		}
//		arrayView.getContentPane().add(arrayContent, BorderLayout.CENTER);
//		arrayView.setSize(300, 300);
		ValuePresenter arrayView = new ValuePresenter(_title, _arrayOrRecord, false, btnPause);
		arrayView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		arrayView.setLocationRelativeTo(control);
		arrayView.setModalityType(ModalityType.APPLICATION_MODAL);
		arrayView.setVisible(true);
	}
	// END KGU#439 2017-10-13
	
	// START KGU#2 (#9) 2015-11-13: New method to execute a called subroutine
	// START KGU#156 2016-03-12: Enh. #124 - signature enhanced to overcome some nasty hacks
	// KGU#376 2017-07-01: Enh.#389 - caller may now be null if an include is performed
	//private Object executeCall(Root subRoot, Object[] arguments)
	private Object executeCall(Root subRoot, Object[] arguments, Call caller)
	// END KGU#156 2016-03-12
	{
		boolean cloned = false;
		Root root = subRoot;
		Object resultObject = null;
		// START KGU#384 2017-04-22: Replaced by the ExecutionContext cartridge
//		Root oldRoot = this.diagram.getRoot();
//		ExecutionStackEntry entry = new ExecutionStackEntry(
//				oldRoot,
//				this.variables, 
//				this.interpreter,
//				// START KGU#78 2015-11-25
//				this.loopDepth,
//				// END KGU#78 2015-11-25
//				// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
//				this.forLoopVars,
//				// END KGU#307 2016-12-12
//				// START KGU#375/KGU#376 2017-04-21: Enh. #388, #389
//				this.constants,
//				this.importList
//				// END KGU#375, KGU#376 2017-04-21
//				);
		// START KGU#508 2018-03-19: Bugfix #525 - This had been forgotten on replacing the ExecutionStackEntry (#389)
		this.context.root.isCalling = true;
		// END KGU#508 2018-03-19
		this.callers.push(this.context);
		// START KGU#2 2015-10-18: cross-NSD subroutine execution?
		// END KGU#384 2017-04-22
		// START KGU#376 2017-04-21: Update all current imports before sub execution
		for (int i = 0; i < context.importList.count(); i++) {
			String impName = context.importList.get(i);
			// FIXME This retrieval is a little awkward - maybe the importList should be a set of Root
			for (Root impRoot: this.importMap.keySet()) {
				if (impRoot.getMethodName().equals(impName)) {
					ImportInfo info = this.importMap.get(impRoot);
					this.copyInterpreterContents(context.interpreter, info.interpreter, info.variableNames, impRoot.constants.keySet(), true);
				}
			}
		}
		// START KGU#384 2017-04-22 Is done below now, when setting up the new context
//		if (!subRoot.isProgram) {
//			// It's not an import, so start with a new importList 
//			this.importList = new StringList();
//		}
		// END KGU#384 2017-04-22
		// END KGU#376 2017-04-21
		// START KGU#384 2017-04-22: Now delegated to execute(Object[])
//		this.initInterpreter();
//		this.variables = new StringList();	// FIXME -> Map<String, Set<Interpreter>>
//		// START KGU#375 2017-04-21: Enh. #388: Need also a new constants enviroment
//		this.constants = new HashMap<String, Object>();
//		// END KGU#375 2017-04-21
//		// START KGU#307 2016-12-12: Issue #307: Keep track of FOR loop variables
//		this.forLoopVars = new StringList(); 
//		// END KGU#307 2016-12-12
//		// loopDepth will be set 0 by the execut(arguments) call below
		// END KGU#384 2017-04-22
		
		// If the found subroutine is already an active caller, then we need a new instance of it
		if (root.isCalling)
		{
			// START KGU#749 2019-10-15: Issue #763 - we must compensate the changes in Diagram.saveNSD(Root, boolean)
			//root = (Root)root.copy();
			root = root.copyWithFilepaths();
			// END KGU#749 2019-10-15
			root.isCalling = false;
			// Remaining initialisations will be done by this.execute(...).
			cloned = true;
		}
		// START KGU#384 2017-04-22: Execution context redesign
		if (root.isInclude()) {
			// For an import Call continue the importList recursively
			this.context = new ExecutionContext(root, this.context.importList);
		}
		else {
			// For a subroutine call, start with a new import list
			this.context = new ExecutionContext(root);
		}
		initInterpreter();
		// END KGU#384 2017-04-22
		
		// START KGU#430 2017-10-12: Issue #432 reduce redraw() calls on delay 0
		//this.diagram.setRoot(root, !Element.E_AUTO_SAVE_ON_EXECUTE);
		this.diagram.setRoot(root, !Element.E_AUTO_SAVE_ON_EXECUTE, delay > 0);
		// END KGU#430 2017-10-12
		
		// START KGU#156 2016-03-11: Enh. #124 - detect execution counter diff.
		int countBefore = root.getExecStepCount(true);
		// END KGU#156 2016-03-11
		
		/////////////////////////////////////////////////////////
		boolean ok = this.execute(arguments);	// Actual execution of the subroutine or import
		/////////////////////////////////////////////////////////
		
		// START KGU#156 2016-03-11: Enh. #124 / KGU#376 2017-07-01: Enh. #389 - caller may be null
		if (caller != null) {
			// START KGU#539 2018-07-02 Bugfix - the call itself is also to be counted as an operation
			//caller.addToExecTotalCount(root.getExecStepCount(true) - countBefore, true);
			caller.addToExecTotalCount(root.getExecStepCount(true) - countBefore + 1, true);
			// END KGU#539 2018-07-02
			// START KGU#686 2019-03-17: Enh. #56 could be in a try context, so don't honour unsuccessful execution 
			//if (cloned || root.isTestCovered(true))	
			if (ok && (cloned || root.isTestCovered(true)))	
			// END KGU#686 2019-03-17
			{
				caller.deeplyCovered = true;
			}
		}
		// END KGU#156 2016-03-11 / KGU#376 2017-07-01

		// START KGU#2 2015-11-24
//		if (!done || stop)
//		{
//			addToStackTrace(root, arguments);
//		}
		// END KGU#2 2015-11-24
		
		//---------------------------------------------------------
		// Integrate the called context into the caller context
		//---------------------------------------------------------
		
		// START KGU#117 2016-03-07: Enh. #77
		// For recursive calls the coverage must be combined
		if (cloned && Element.E_COLLECTRUNTIMEDATA)
		{
			subRoot.combineRuntimeData(root);
		}
		// END KG#117 2016-03-07
		
		ExecutionContext entry = this.callers.pop();	// former context
		
//		// START KGU#376 2017-04-21: Enh. #389 don't restore after an import call
		// FIXME: Restore but cache the Interpreter with all variables and copy contents before
		if (subRoot.isInclude()) {
			// It was an import Call, so we have to import the definitions and values 
			// FIXME: Derive a sensible type StringList from subRoot.getTypeInfo() KGU 2017-09-18: what for?
			this.copyInterpreterContents(context.interpreter, entry.interpreter,
					this.context.variables, entry.root.constants.keySet(), false);
			// START KGU#388 2017-09-18: Enh. #423
			// Adopt the imported typedefs if any
			for (Entry<String, TypeMapEntry> typeEntry: context.dynTypeMap.entrySet()) {
				TypeMapEntry oldEntry = entry.dynTypeMap.putIfAbsent(typeEntry.getKey(), typeEntry.getValue());
				if (oldEntry != null) {
					logger.log(Level.INFO, "Conflicting type entry {0} from Includable {1}", new Object[]{typeEntry.getKey(), subRoot.getMethodName()});
				}
			}
			// END KGU#388 2017-09-18
			entry.variables.addIfNew(context.variables);
			for (Entry<String, Object> constEntry: context.constants.entrySet()) {
				if (!entry.constants.containsKey(constEntry.getKey())) {
					entry.constants.put(constEntry.getKey(), constEntry.getValue());
				}
			}	
			this.importMap.put(subRoot, new ImportInfo(this.context.interpreter, this.context.variables, this.context.dynTypeMap));
			context.importList.addIfNew(subRoot.getMethodName());
			// TODO: Check this for necessity and soundness!
			for (Entry<String, String> constEntry: subRoot.constants.entrySet()) {
				if (!entry.root.constants.containsKey(constEntry.getKey())) {
					entry.root.constants.put(constEntry.getKey(), constEntry.getValue());
				}
			}
		}
		else {
			// Subroutines may have updated definitions from import diagrams - we must get aware of these changes 
			for (int i = 0; i < context.importList.count(); i++) {
				String impName = context.importList.get(i);
				// FIXME This retrieval is a little awkward - maybe the importList should be a set of Root
				for (Root impRoot: this.importMap.keySet()) {
					if (impRoot.getMethodName().equals(impName)) {
						ImportInfo info = this.importMap.get(impRoot);
						if (this.copyInterpreterContents(context.interpreter, info.interpreter, info.variableNames, impRoot.constants.keySet(), true)
								&& entry.importList.contains(impName)) {
							this.copyInterpreterContents(info.interpreter, entry.interpreter, info.variableNames, impRoot.constants.keySet(), true);
						}
					}
				}
			}
		}
//		// END KGU#376 2017-04-21
		// START KGU#384 2017-04-22: Now done at once with the entire context cartridge
//		this.variables = entry.variables;
//		// START KGU#375 2017-04-21: Enh. #388: Need also a new constants enviroment
//		this.constants = entry.constants;
//		// END KGU#375 2017-04-21
//		this.interpreter = entry.interpreter;
//		// START KGU#78 2015-11-25
//		this.loopDepth = entry.loopDepth;
//		// END KGU#78 2015-11-25
//		this.forLoopVars = entry.forLoopVars;
		// END KGU#384 2017-08-22
		
		//---------------------------------------------------------
		// Restore the caller now
		//---------------------------------------------------------
		
		// START KGU#430 2017-10-12: Issue #432 reduce redraw() calls on delay 0
		//this.diagram.setRoot(entry.root, !Element.E_AUTO_SAVE_ON_EXECUTE);
		this.diagram.setRoot(entry.root, !Element.E_AUTO_SAVE_ON_EXECUTE, delay > 0);
		// END KGU#430 2017-10-12
		entry.root.isCalling = false;

		// START KGU#686 2019-03-17: Enh. #56 Don't fetch the result if failed
		if (ok) {
		// END KGU#686 2019-03-17
			// START KGU#376 2017-04-21: Enh. #389
			// The called subroutine will certainly have returned a value...
			resultObject = this.context.returnedValue;
			// ... but definitively not THIS calling routine!
			// FIXME: Shouldn't we have cached the previous values in entry?
		// START KGU#686 2019-03-17: Enh. #56 Don't fetch the result if failed
		}
		// END KGU#686 2019-03-17
		
		
		// START KGU#384 2017-04-22: Now done at once with the entire context cartridge
		//this.returned = false; 
		//this.returnedValue = null;
		this.context = entry;
		// END KGU#384 2017-08-22
		
		try 
		{
			updateVariableDisplay();
		}
		catch (EvalError ex) {}
		
		return resultObject;
	}
	
	// START KGU#2 2015-11-24: Stack trace support for execution errors
	private void addToStackTrace(Root _root, Object[] _arguments)
	{
		String argumentString = "";
		if (_arguments != null)
		{
			for (int i = 0; i < _arguments.length; i++)
			{
				argumentString = argumentString + (i>0 ? ", " : "") + prepareValueForDisplay(_arguments[i], context.dynTypeMap);					
			}
			argumentString = "(" + argumentString + ")";
		}
		this.stackTrace.add(_root.getMethodName() + argumentString);
		// START KGU#569 2018-08-03: Enh. #577 - optional call trace in console window
		if (this.console.logCalls()) {
			int depth = this.stackTrace.count() - 1;
			for (int i = 0; i < Math.min(MAX_STACK_INDENT, depth); i++) {
				this.console.write("  ");
			}
			if (depth > MAX_STACK_INDENT) {
				this.console.write("[" + depth + "]", Color.GRAY);
			}
			this.console.writeln(">>> " + this.stackTrace.get(depth), Color.GRAY);
		}
		// END KGU#569 2018-08-03
	}
	
	// START KGU#159 2016-03-17: #133 Stacktrace should always be available on demand, not only on error
	private void dropFromStackTrace()
	{
		int size = this.stackTrace.count();
		if (size > 0)
		{
			size--;
			// START KGU#569 2018-08-03: Enh. #577 - optional call trace in console window
			if (this.console.logCalls()) {
				for (int i = 0; i < Math.min(MAX_STACK_INDENT, size); i++) {
					this.console.write("  ");
				}
				if (size > MAX_STACK_INDENT) {
					this.console.write("[" + size + "]", Color.GRAY);
				}
				this.console.writeln("<<< " + this.stackTrace.get(size), Color.GRAY);
			}
			// END KGU#569 2018-08-03
			this.stackTrace.delete(size);
		}
	}
	// END KGU#159 2016-03-17

	/**
	 * Pops up a dialog displaying the call trace with argument values
	 */
	public void showStackTrace()
	{
// START KGU#159 2016-03-17: A listview is always the better choice
// (Think of large arrays as arguments!)
//		if (stackTrace.count() <= 20)
//		{
//			// Okay, keep it simple
//			JOptionPane.showMessageDialog(diagram, this.stackTrace.getText(), "Stack trace",
//					JOptionPane.INFORMATION_MESSAGE);
//		}
//		else
//		{
// END KGU#159 2016-03-17
			JDialog stackView = new JDialog();
			stackView.setTitle("Stack trace");
			//stackView.setIconImage(IconLoader.ico004.getImage());
			stackView.setIconImage(IconLoader.getIcon(4).getImage());
			List stackContent = new List(10);
			int depth = stackTrace.count();
			for (int i = 0; i < depth; i++)
			{
				// START KGU#201 2016-07-25: Issue #201 - level indices added
				//stackContent.add(stackTrace.get(depth - i - 1));
				stackContent.add(depth-i-1 + ": " + stackTrace.get(depth - i - 1));
				// END KGU#201 2016-07-25
			}
			stackView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			stackView.getContentPane().add(stackContent, BorderLayout.CENTER);
			stackView.setSize(300, 300);
			stackView.setLocationRelativeTo(control);
			stackView.setModalityType(ModalityType.APPLICATION_MODAL);
			stackView.setVisible(true);
// START KGU#159 2016-03-17: A listview is always the better choice
//		}		
// END KGU#159 2016-03-17
	}
	// END KGU#2 2015-11-24
	
	/**
	 * Searches all known pools for a unique includable diagram with given name 
	 * @param name - diagram name
	 * @return a Root of type INCLUDABLE with given name if uniquely found, null otherwise
	 * @throws Exception
	 */
	public Root findIncludableWithName(String name) throws Exception
	{
		return findDiagramWithSignature(name, -2);
	}
	
    /**
     * Searches all known pools for subroutines with a signature compatible to name(arg1, arg2, ..., arg_nArgs) 
     * @param name - function name
     * @param nArgs - number of parameters of the requested function
     * @return a Root that matches the specification if uniquely found, null otherwise
     * @throws Exception 
     */
    public Root findSubroutineWithSignature(String name, int nArgs) throws Exception
    {
    	Root subroutine = null;
    	// First test whether the current root calls itself recursively
    	Root root = diagram.getRoot();
    	if (name.equals(root.getMethodName()) && nArgs == root.getParameterNames().count())
    	{
    		subroutine = root;
    	}
    	if (subroutine == null) {
    		subroutine = findDiagramWithSignature(name, nArgs);
    	}
    	return subroutine;
    }
    
    /**
     * Searches all known pools for either routine diagrams with a signature compatible to {@code name(arg1, arg2, ..., arg_nArgs)}
     * or for includable diagrams with name {@code name}
     * @param name - diagram name
     * @param nArgs - number of parameters of the requested function (negative for Includable)
     * @return a Root that matches the specification if uniquely found, null otherwise
     * @throws Exception if there are differing matching diagrams
     */
    private Root findDiagramWithSignature(String name, int nArgs) throws Exception
    {
    	Root diagr = null;
    	Iterator<IRoutinePool> iter = this.routinePools.iterator();
    	while (diagr == null && iter.hasNext())
    	{
    		IRoutinePool pool = iter.next();
    		Vector<Root> candidates = null;
    		if (nArgs >= 0) {
    			candidates = pool.findRoutinesBySignature(name, nArgs, context.root);
    		}
    		else {
    			// Why the heck this circumvention? 
//    			candidates = new Vector<Root>();
//    			for (Root cand: pool.findIncludesByName(name, context.root)) {
//    				candidates.add(cand);
//    			}
    			candidates = pool.findIncludesByName(name, context.root);
    		}
    		// START KGU#317 2016-12-29: Now the execution will be aborted on ambiguous calls
    		//for (int c = 0; subroutine == null && c < candidates.size(); c++)
    		for (int c = 0; c < candidates.size(); c++)
    		// END KGU#317 2016-12-29
    		{
    			// START KGU#317 2016-12-29: Check for ambiguity (multiple matches) and raise e.g. an exception in that case
    			//subroutine = candidates.get(c);
    			Root cand = candidates.get(c);
    			if (diagr == null) {
    				diagr = cand;
    			}
    			else {
    				int similarity = diagr.compareTo(cand); 
    				if (similarity > 2 && similarity != 4) {
    					// 3: Equal file path but unsaved changes in one or both diagrams;
    					// 5: Equal signature (i. e. type, name and argument number) but different content or structure.
    					throw new Exception(control.msgAmbiguousCall.getText().replace("%1", name).replace("%2", (nArgs < 0 ? "--" : Integer.toString(nArgs))));
    				}
    			}
    			// END KGU#317 2016-12-29
    			// START KGU#125 2016-01-05: Is to force updating of the diagram status
    			if (pool instanceof Updater)
    			{
    				diagr.addUpdater((Updater)pool);
    			}
    			diagram.adoptArrangedOrphanNSD(diagr);
    			// END KGU#125 2016-01-05
    		}
    	}
    	return diagr;
    }
	// END KGU#2 (#9) 2015-11-13

	// KGU#448 2017-10-28: Replaced former method getExec(String) in the only remained reference 
	public String initRootExecDelay()
	{
		String trouble = "";
		// START KGU#448 2017-10-28: Enh. #443
		//if (diagramController != null && diagramController instanceof DelayableDiagramController)
		//{
		//	((DelayableDiagramController)diagramController).setAnimationDelay(delay, true);
		//}
		//else
		//boolean delayed = false;
		if (diagramControllers != null) {
			for (DiagramController controller: diagramControllers) {
				if (controller instanceof DelayableDiagramController) {
					((DelayableDiagramController)controller).setAnimationDelay(delay, true);
					//delayed = true;
				}
			}
		}
		// START KGU#675 2019-03-04: This seemed to be a relic from times when execution paused after the current element
//		if (!delayed)
//		// END KGU#448 2017-10-28
//		{
//			delay();
//		}
		// END KGU#675 2019-03-04
		if (delay != 0)
		{
			diagram.redraw();
			try
			{
				Thread.sleep(delay);
			} catch (InterruptedException e)
			{
				logger.log(Level.SEVERE, "sleep(): {0}", e.getMessage());
			}
		}
		return trouble;
	}

//	// Replaced by getExec(DiagramController, String, Object[])
//    @Deprecated
//	public String getExec(String cmd, Color color)
//	{
//		String trouble = "";
//		if (diagramController != null)
//		{
//			trouble = diagramController.execute(cmd, color);
//		} else
//		{
//			delay();
//		}
//		if (delay != 0)
//		{
//			diagram.redraw();
//			try
//			{
//				Thread.sleep(delay);
//			} catch (InterruptedException e)
//			{
//				System.err.println("Executor.getExec(\"" + cmd + "\", " + color + "): " + e.getMessage());
//			}
//		}
//		return trouble;
//	}

	// START KGU#448 2017-10-28: Enh. #443 replaces getExec(String) and getExec(String, Color)
	/**
	 * Executes the procedure {@code procName} with arguments {@code arguments} on
	 * the given {@link DiagramController} {@code controller}.
	 * As the operation is regarded as a diagram step, the specified delay is applied.
	 * @param controller - the facade for the controlled device
	 * @param procName - the name of the operation
	 * @param arguments - the arguments for the operation
	 * @return
	 */
	public String getExec(DiagramController controller, String procName, Object[] arguments)
	{
		String trouble = "";
		try {
			// We don't expect results here
			controller.execute(procName, arguments);
		}
		catch (FunctionException ex) {
			trouble = ex.getMessage();
		}
		if (delay != 0)
		{
			// Don't do a duplicate delay
			if (!(controller instanceof DelayableDiagramController)) {
				delay();
			}
			diagram.redraw();
			try
			{
				Thread.sleep(delay);
			} catch (InterruptedException e)
			{
				StringList args = new StringList();
				for (int i = 0; i < arguments.length; i++) {
					args.add(arguments[i].toString());
				}
				logger.log(Level.SEVERE, "getExec({0}, \"{1}\", {2}): {3}",
						new Object[]{
								controller, procName,
								args.concatenate(", "), e.getMessage()
						});
			}
		}
		return trouble;
	}
	// END KGU#448 2017-10-28

	public boolean getPaus()
	{
		synchronized (this)
		{
			return paus;
		}
	}
	
	private void initInterpreter()
	{
		try
		{
			// STRT KGU#384 2017-04-22: Redesign of execution context
			//interpreter = new Interpreter();
			Interpreter interpreter = this.context.interpreter;
			// END KGU#384 2017-04-22

			// START KGU 2016-12-18: #314: Support for simple text file API
			interpreter.set("executorFileMap", this.openFiles);
			interpreter.set("executorCurrentDirectory", 
					(diagram.currentDirectory.isDirectory() ? diagram.currentDirectory : diagram.currentDirectory.getParentFile()).getAbsolutePath());
			// END KGU 2016-12-18

			for (int i = 0; i < builtInFunctions.length; i++) {
				interpreter.eval(builtInFunctions[i]);
			}
			
		} catch (EvalError ex)
		{
			//java.io.IOException
			logger.log(Level.SEVERE, ex.getMessage());
		}
	}
	
	// Test for Interpreter routines
//	public Object structorizerGetScannedObject(java.util.Scanner sc) {
//		Object result = null; 
//		sc.useLocale(java.util.Locale.UK); 
//		if (sc.hasNextInt()) { result = sc.nextInt(); } 
//		else if (sc.hasNextDouble()) { result = sc.nextDouble(); } 
//		else if (sc.hasNext("\\\".*?\\\"")) { result = sc.next("\\\".*?\\\""); } 
//		else if (sc.hasNext("\\{.*?\\}")) {
//			String token = sc.next();
//			result = new Object[]{token.substring(1, token.length()-1)};
//		} 
//		else if (sc.hasNext("\\\".*")) { 
//			String str = sc.next(); 
//			while (sc.hasNext() && !sc.hasNext(".*\\\"")) { 
//				str += " " + sc.next();
//			}
//			if (sc.hasNext()) { str += " " + sc.next(); }
//			result = str;
//		}
//		else if (sc.hasNext("\\{.*")) { 
//			java.util.regex.Pattern oldDelim = sc.delimiter();
//			//sc.useDelimiter("(\\p{javaWhitespace}*,\\p{javaWhitespace}*|\\})");
//			sc.useDelimiter("\\}");
//			String expr = sc.next().trim().substring(1);
//			sc.useDelimiter(oldDelim);
//			String[] elements = {};
//			if (!expr.isEmpty()) {
//				elements = expr.split("\\p{javaWhitespace}*,\\p{javaWhitespace}*");
//			}
//			if (sc.hasNext("\\}")) { sc.next(); }
//			Object[] objects = new Object[elements.length];
//			for (int i = 0; i < elements.length; i++) { 
//				java.util.Scanner sc0 = new java.util.Scanner(elements[i]);
//				objects[i] = structorizerGetScannedObject(sc0);
//				sc0.close();
//			}
//			result = objects;
//		}
//		else { result = sc.next(); }
//		return result;
//	}

//	public int fileOpen(String filePath)
//	{
//		int fileNo = 0; 
//		java.io.File file = new java.io.File(filePath);
//		try {
//			java.io.FileInputStream fis = new java.io.FileInputStream(file);
//			java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis, "UTF-8"));
//			fileNo = this.openFiles.size() + 1;
//			this.openFiles.add(new java.util.Scanner(reader));
//		}
//		catch (SecurityException e) { fileNo = -3; }
//		catch (java.io.FileNotFoundException e) { fileNo = -2; }
//		catch (java.io.IOException e) { fileNo = -1; }
//		return fileNo;
//	}

//	public int fileAppend(String filePath)
//	{
//		int fileNo = 0;
//		java.io.File file = new java.io.File(filePath);
//		if (!file.isAbsolute()) {
//			file = diagram.currentDirectory;
//			if (!file.isDirectory()) { file = file.getParentFile(); }
//			file = new java.io.File(file.getAbsolutePath() + java.io.File.separator + filePath);
//			filePath = file.getAbsolutePath();
//		}
//		java.io.BufferedWriter writer = null;
//		System.out.println(file.getName());
//		try {
//			if (file.exists()) {
//				java.io.File tmpFile = java.io.File.createTempFile("structorizer_"+file.getName(), null);
//				if (tmpFile.exists()) { tmpFile.delete(); }
//				if (file.renameTo(tmpFile)) {
//					java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath); 
//					java.io.FileInputStream fis = new java.io.FileInputStream(tmpFile.getAbsolutePath()); 
//					writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, "UTF-8")); 
//					java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis, "UTF-8"));
//					String line = null; 
//					while ((line = reader.readLine()) != null) {
//						writer.write(line); writer.newLine();
//					} 
//					reader.close();
//					tmpFile.delete();
//				}
//				else {
//					fileNo = -4;
//				}
//			} 
//			else { 
//				java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath); 
//				writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(fos, "UTF-8")); 				
//			} 
//			fileNo = this.openFiles.size() + 1;
//			this.openFiles.add(writer);  
//		} 
//		catch (SecurityException e) { fileNo = -3; } 
//		catch (java.io.FileNotFoundException e) { fileNo = -2; }
//		catch (java.io.IOException e) { fileNo = -1; }
//		return fileNo;
//	}
	
//	public void fileClose(int fileNo) throws java.io.IOException
//	{
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable file = this.openFiles.get(fileNo - 1);
//			if (file != null) {
//				try { file.close(); }
//				catch (java.io.IOException e) {}
//				this.openFiles.set(fileNo - 1, null); }
//		}
//		else { throw new java.io.IOException("fileClose: §INVALID_HANDLE_READ§"); }
//	}

//	public boolean fileEOF(int fileNo) throws java.io.IOException
//	{
//		boolean isEOF = true;
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable reader = this.openFiles.get(fileNo - 1);
//			if (reader instanceof java.util.Scanner) {
//				//try {
//					isEOF = !((java.util.Scanner)reader).hasNext();
//				//} catch (java.io.IOException e) {}
//			}
//		}
//		else { throw new java.io.IOException("fileEOF: §INVALID_HANDLE_READ§"); }
//		return isEOF;
//	}

//	public Object fileRead(int fileNo) throws java.io.IOException
//	{
//		Object result = null;
//		boolean ok = false;
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable reader = this.openFiles.get(fileNo - 1);
//			if (reader instanceof java.util.Scanner) {
//				result = structorizerGetScannedObject((java.util.Scanner)reader);
//				ok = true;
//			}
//		}
//		if (!ok) { throw new java.io.IOException("fileRead: §INVALID_HANDLE_READ§"); }
//		return result;
//	}

//	public String fileReadLine(int fileNo) throws java.io.IOException
//	{
//		String line = null;
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable file = this.openFiles.get(fileNo - 1);
//			if (file instanceof java.io.BufferedReader) {
//				line = ((java.io.BufferedReader)file).readLine();
//			}
//		}
//		return line;
//	}

//	public void fileWrite(int fileNo, String line)
//	{
//		if (fileNo > 0 && fileNo <= this.openFiles.size()) {
//			java.io.Closeable file = this.openFiles.get(fileNo - 1);
//			if (file instanceof java.io.BufferedWriter) {
//				((java.io.BufferedWriter)file).write(line);
//				((java.io.BufferedWriter)file).newLine();
//			}
//		}
//	}

	// START KGU#376 2017-04-20: Enh. #389 - we need to copy interpreter contents 
	/**
	 * Copies the constants specified by <code>_constNames</code> and the values of the variables
	 * specified by <code>_varNames</code> from the <code>_source</code> interpreter context to the
	 * <code>_target</code> interpreter context.
	 * @param _source - the source interpreter
	 * @param _target - the target interpreter
	 * @param _varNames - names of the variables to be considered
	 * @param _constNames - names of the constants to be included
	 * @param _overwrite - whereas defined constants are never overwritten, for variables this argument
	 * may allow to update the values of already existing values (default is false)
	 * @return true if there was at least one copied entity
	 */
	private boolean copyInterpreterContents(Interpreter _source, Interpreter _target, StringList _varNames, Set<String> _constNames, boolean _overwrite)
	{
		boolean somethingCopied = false;
		for (int i = 0; i < _varNames.count(); i++) {
			String varName = _varNames.get(i);
			try {
				if (!_constNames.contains(varName) && _overwrite || _target.get(varName) == null) {
					Object val = _source.get(_varNames.get(i));
					/* Here we try to avoid hat all primitive values are boxed to
					 * Object.*/
					if (val instanceof Boolean) {
						_target.set(varName, ((Boolean)val).booleanValue());
						somethingCopied = true;
					}
					else if (val instanceof Integer) {
						_target.set(varName, ((Integer)val).intValue());
						somethingCopied = true;
					}
					else if (val instanceof Long) {
						_target.set(varName, ((Long)val).longValue());
						somethingCopied = true;
					}
					else if (val instanceof Float) {
						_target.set(varName, ((Float)val).floatValue());
						somethingCopied = true;
					}
					else if (val instanceof Double) {
						_target.set(varName, ((Double)val).doubleValue());
						somethingCopied = true;
					}
					else {
						_target.set(varName, val);
						somethingCopied = true;
					}
				}
			} catch (EvalError e) {
				// START KGU#484 2018-04-05: Issue #463
				//e.printStackTrace();
				logger.log(Level.WARNING, "Execution context change for variable " + varName, e);
				// END KGU#484 2018-04-05
			}
		}
		return somethingCopied;
	}
	// END KGU#376 2017-04-20

	public boolean isNumeric(String input)
	{
		try
		{
			Double.parseDouble(input);
			return true;
		} catch (Exception e)
		{
			return false;
		}
	}
	
	public boolean isRunning()
	{
		return running;
	}

	public void run()
	{
		execute();
		running = false;
		// START KGU#117/KGU#156 2016-03-13: Enh. #77 + #124
		// It is utterly annoying when in run data mode the control always 
		// closes after execution.
		control.setVisible(false);
		// START KGU#157 2016-03-16: Bugfix #131 - postponed Control start?
		// START KGU#817 2020-04-04: Issue #829, revoked on 2020-12-14
		boolean reopen = false;
		//boolean reopen = true;
		// END KGU#817 2020-04-04
		if (this.reopenFor != null)
		{
			this.diagram = this.reopenFor;
			this.reopenFor = null;
			reopen = true;
		}
		// START KGU#117/KGU#156 2016-03-13: Enh. #77 + #124
		// It is utterly annoying when in run data mode the control always 
		// closes after execution.
		if (reopen || Element.E_COLLECTRUNTIMEDATA)
		{
			control.init();
			control.validate();
			control.setVisible(true);
			control.repaint();
		}
		// END KGU#117/KGU#156
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(int aDelay)
	{
		// START KGU#97 2015-12-20: Enh.Req. #48: Only inform if it's worth
		boolean delayChanged = aDelay != delay;
		// END KGU#97 2015-12-10
		delay = aDelay;
		// START KGU#97 2015-12-10: Enh.Req. #48: Inform delay-aware DiaramControllers A.S.A.P.
		// START KGU#448 2017-10-28: Enh. #443 Revised to cope with several controllers
		if (delayChanged && diagramControllers != null) {
			for (DiagramController controller: diagramControllers) {
				if (controller instanceof DelayableDiagramController)
				{
					((DelayableDiagramController) controller).setAnimationDelay(aDelay, false);
				}
			}
		}
		// END KGU#448 2017-10-28
		// END KGU#97 2015-12-20
	}

	/*
	 * ORIGINAL VERSION, NOT MODIFIED BY gdonnarumma
	 */

	/*
	 * private void setVar(String name, Object content) throws EvalError {
	 * //interpreter.set(name,content);
	 * 
	 * if(content instanceof String) { if(!isNumeric((String) content)) {
	 * content = "\""+ ((String) content) + "\""; } }
	 * 
	 * interpreter.set(name,content); interpreter.eval(name+" = "+content);
	 * variables.addIfNew(name);
	 * 
	 * if(delay!=0) { Vector<Vector> vars = new Vector<Vector>(); for(int
	 * i=0;i<variables.count();i++) { Vector myVar = new Vector();
	 * myVar.add(variables.get(i));
	 * myVar.add(interpreter.get(variables.get(i))); vars.add(myVar); }
	 * control.updateVars(vars); }
	 * 
	 * }
	 */

	/**
	 * @param aPaus
	 *            the step to set
	 */
	public void setPaus(boolean aPaus)
	{
		// START KGU 2015-10-13: In "turbo" mode, too, we want to see were the algorithm is hovering.
		if (delay == 0)
		{
			diagram.redraw();
 			try {
				updateVariableDisplay();
			}
			catch (EvalError ex)
			{
				logger.log(Level.SEVERE, "Sync Error in updateVariableDisplay(): {0}", ex.toString());
			}
		}
		// END KGU 2015-10-13
		synchronized (this)
		{
			paus = aPaus;
			if (paus == false)
			{
				step = false;
			}
			this.notify();
		}
	}

	/**
	 * @param aStop
	 *            the stop to set
	 */
	public void setStop(boolean aStop)
	{
		diagram.clearExecutionStatus();
		synchronized (this)
		{
			stop = aStop;
			paus = false;
			step = false;
			this.notify();
		}
	}

	
	// START KGU#67/KGU#68/KGU#69 2015-11-08: We must distinguish between raw input and evaluated objects
	/**
	 * Interprets and evaluates the user input string {@code rawInput} and assigns the result to the given
	 * variable extracted from the "lvalue" {@code target} via {@link #setVar(String, Object)}.
	 * @param target - an assignment lvalue, may contain modifiers, type info and access specifiers
	 * @param rawInput - the raw input string to be interpreted
	 * @return base name of the assigned variable (or constant)
	 * @throws EvalError if the interpretation of {@code rawInput} fails, if the {@code target} or the resulting
	 * value is inappropriate, if both don't match or if a loop variable violation is detected.
	 * @see #setVar(String, Object) 
	 */
	private String setVarRaw(String target, String rawInput) throws EvalError
	{
		// START KGU#580 2018-09-24: Issue #605
		String varName = target;
		// END KGU#580 2018-09-24
		// first add as string (lest we should end with nothing at all...)
		// START KGU#109 2015-12-15: Bugfix #61: Previously declared (typed) variables caused errors here
		//setVar(name, rawInput);
		EvalError finalError = null;
		try {
			varName = setVar(target, rawInput);
		}
		catch (EvalError ex)
		{
			finalError = ex;	// Remember this error for the case all other attempts will fail
		}
		// END KGU#109 2015-12-15
		// Try some refinement if possible
		if (rawInput != null && !isNumeric(rawInput) )
		{
			try
			{
				String strInput = rawInput.trim();
				// Maybe the string or character is already quoted, then get the content
				if (strInput.startsWith("\"") && strInput.endsWith("\"") ||
						strInput.startsWith("'") && strInput.endsWith("'"))
				{
					// START KGU#813 2020-02-21: Bugfix #826 - Strings with backslashes used to cause eval errors
					//this.evaluateExpression(target + " = " + rawInput, false, false);
					//varName = setVar(target, context.interpreter.get(target));
					if (strInput.startsWith("'") && strInput.length() > 3 && strInput.charAt(1) != '\\') {
						strInput = "\"" + strInput.substring(1, strInput.length()-1) + "\"";
					}
					varName = evaluateRawString(target, strInput);
					// END KGU#813 2020-02-21
				}
				// START KGU#285 2016-10-16: Bugfix #276
				else if (rawInput.contains("\\"))
				{
					// Obviously it isn't enclosed by quotes (otherwise the previous test would have caught it
					// START KGU#813 2020-02-21: Bugfix #826 - Strings with backslashes used to cause eval errors
					//this.evaluateExpression(target + " = \"" + rawInput + "\"", false, false);
					evaluateRawString(target, "\"" + rawInput + "\"");
					// END KGU#813 2020-02-21
					varName = setVar(target, context.interpreter.get(target));					
				}
				// END KGU#285 2016-10-16
				// try adding as char (only if it's not a digit)
				else if (rawInput.length() == 1)
				{
					Character charInput = rawInput.charAt(0);
					varName = setVar(target, charInput);
				}
				// START KGU#184 2016-04-25: Enh. #174 - accept array initialisations on input
//				else if (strInput.startsWith("{") && rawInput.endsWith("}"))
//				{
//					String asgnmt = "Object[] " + target + " = " + rawInput;
//					// Nested initializers won't work here!
//					this.evaluateExpression(asgnmt, false);
//					setVar(target, context.interpreter.get(target));
//				}
//				// END KGU#184 2016-04-25
//				// START KGU#388 2017-09-18: Enh. #423
//				else if (strInput.indexOf("{") > 0 && strInput.endsWith("}")
//						&& Function.testIdentifier(strInput.substring(0, strInput.indexOf("{")), null)) {
//					String asgnmt = "HashMap " + target + " = new HashMap()";
//					this.evaluateExpression(asgnmt, false);
//					HashMap<String, String> components = Element.splitRecordInitializer(strInput);
//					for (Entry<String, String> comp: components.entrySet()) {
//						String value = comp.getValue();
//						if (comp.getKey().startsWith("§")) {
//							value = "\"" + value + "\"";
//						}
//						asgnmt = target + ".put(\"" + comp.getKey() + "\", " + value + ")";
//						this.evaluateExpression(asgnmt, false);
//					}
//					setVar(target, context.interpreter.get(target));
//				}
				// END KGU#388 2017-09-18
				else if (strInput.endsWith("}") && (strInput.startsWith("{") ||
						strInput.indexOf("{") > 0 && Function.testIdentifier(strInput.substring(0, strInput.indexOf("{")), false, null))) {
					// START KGU#879 2020-10-19: Issue #879 - we should not invalidate a successfully set content
					//varName = setVar(target, this.evaluateExpression(strInput, true, false));
					Object evaluated = this.evaluateExpression(strInput, true, false);
					// If there is no sensible evaluation result then leave the value as is
					if (evaluated != null) {
						varName = setVar(target, evaluated);
					}
					// END KGU#879 2020-10-19
				}
				// START KGU#283 2016-10-16: Enh. #273
				else if (strInput.equals("true") || strInput.equals("false"))
				{
					varName = setVar(target, Boolean.valueOf(strInput));
				}
				// END KGU#283 2016-10-16
			}
			catch (Exception ex)
			{
				logger.log(Level.INFO, "\"{0}\" as string/char: {1}", new Object[]{rawInput, ex.getMessage()});
				// START KGU#388 2017-09-18: These explicit errors should get raised
				throw ex;
				// END KGU#388 2017-09-18
			}
			// If all went well until here, then it's fine
			finalError = null;
		}
		// try adding as double
		try
		{
			double dblInput = Double.parseDouble(rawInput);	// may cause an exception 
			varName = setVar(target, dblInput);
			finalError = null;
		} catch (Exception ex)
		{
			//System.out.println(rawInput + " as double: " + ex.getMessage());
			if (ex instanceof EvalError) {
				// In this case the error came from the interpreter, not from parsing attempts
				finalError = (EvalError)ex;
			}
		}
		// finally try adding as integer
		try
		{
			int intInput = Integer.parseInt(rawInput);	// may cause an exception
			varName = setVar(target, intInput);
			finalError = null;
		} catch (Exception ex)
		{
			//System.out.println(rawInput + " as int: " + ex.getMessage());
			if (ex instanceof EvalError) {
				// In this case the error came from the interpreter, not from parsing attempts
				finalError = (EvalError)ex;
			}
		}
		if (finalError != null) {
			throw finalError;
		}
		return varName;
	}

	// START KGU#813 2020-02-21: Auxiliary method introduced for bugfix #826
	/**
	 * Tries to evaluate the given string quoted raw string (looks like a string literal
	 * but might contain "sharp" escape characters, i.e. sequences like \n but also direct \.
	 * If some illegal backslash sequence is detected then evaluation with doubled backslashes
	 * is attempted.
	 * @param target - string specifying the assignment target (possibly with index or component access)
	 * @param rawInput - a quoted raw string from input
	 * @return the base variable name of the assignment target if evaluation succeeded
	 * @throws EvalError if evaluation failed.
	 */
	private String evaluateRawString(String target, String rawInput) throws EvalError {
		try {
			this.evaluateExpression(target + " = " + rawInput, false, false);
		}
		catch (EvalError ex) {
			String msg = ex.getMessage();
			int idx = -1;
			if (msg != null && (idx = msg.indexOf("Lexical error ")) >= 0 && msg.substring(idx).contains("\\")) {
				// Apparently the backslash(es) weren't meant to be escape characters.
				this.evaluateExpression(target + " = " + rawInput.replace("\\", "\\\\"), false, false);
			}
			else {
				throw ex;
			}
		}
		return setVar(target, context.interpreter.get(target));					
	}

	// METHOD MODIFIED BY GENNARO DONNARUMMA and revised by Kay Gürtzig
	/**
	 * Assigns the computed value {@code content} to the given variable extracted from the "lvalue"
	 * {@code target}. Analyses and handles possibly given extra information in order to register and
	 * declare the target variable or constant.<br/>
	 * Also ensures that no loop variable manipulation is performed (the entire loop stack is checked,
	 * so use {@link #setVar(String, Object, int)} for a regular loop variable update).<br/>
	 * There are the following sensible cases w.r.t. {@code target} here (unquoted brackets enclose optional parts):<br/>
	 * a) {@code [const] <id>}<br/>
	 * b) {@code <id>'['<expr>']'}<br/>
	 * c) {@code [const] <typespec1> <id>}<br/>
	 * d) {@code [const] <typespec1> <id>'['[<expr>]']'}  - implicit C-style array declaration (questionable)<br/>
	 * e) {@code [const|var] <id> : <typespec2>}<br/>
	 * f) {@code [const|dim] <id> as <typespec2>}<br/>
	 * g) {@code <id>(.<id>['['<expr>']'])+}<br/>
	 * h) {@code <id>'['<expr>']'(.<id>)+}<br/>
	 * ILLEGAL (NOT supported here):<br/>
	 * w) {@code const <id>'['<expr>']'} - single elements can't be const<br/>
	 * x) {@code [const] <id>'['']'}  - C-style array declaration: redundant if array value is assigned, wrong otherwise<br/>
	 * y) {@code <id>'['<expr>']'('['<expr>']')}+<br/>
	 * Meta symbol legend (as far as not obvious):<br/>
	 * {@code <typespec1> ::=}<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;<code>{modifier} &lt;typeid&gt; |</code><br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;<code>{modifier} &lt;typeid&gt; ('['']')+</code> - Java-style array type (questionable)<br/>
	 * {@code <typespec2> ::=}<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@code <typeid> |}<br/>
	 * &nbsp;&nbsp;&nbsp;&nbsp;{@code array ['['<range>']'] of <typespec>}<br/>
	 * {@code <range> ::= <id> | <intliteral> .. <intliteral>}<br/>
	 * @param target - an assignment lvalue, may contain modifiers, type info and access specifiers
	 * @param content - the value to be assigned
	 * @return base name of the assigned variable (or constant)
	 * @throws EvalError if the {@code target} or the {@code content} is inappropriate or if both aren't compatible
	 * or if a loop variable violation is detected.
	 * @see #setVarRaw(String, Object)
	 * @see #setVar(String, Object, int) 
	 */
	private String setVar(String target, Object content) throws EvalError
	// START KGU#307 2016-12-12: Enh. #307 - check FOR loop variable manipulation
	{
		return setVar(target, content, context.forLoopVars.count()-1);
	}

	/**
	 * Assigns the computed value {@code content} to the given variable extracted from the "lvalue"
	 * {@code target}. Analyses and handles possibly given extra information in order to register and
	 * declare the target variable or constant.<br/>
	 * Also ensures that no loop variable manipulation is performed. 
	 * @param target - an assignment lvalue, may contain modifiers, type info and access specifiers
	 * @param content - the value to be assigned
	 * @param ignoreLoopStackLevel - the loop nesting level beyond which loop variables aren't critical.
	 * @return base name of the assigned variable (or constant)
	 * @throws EvalError if the {@code target} or the {@code content} is inappropriate or if both don't
	 * match or if a loop variable violation is detected.
	 * @see #setVarRaw(String, Object)
	 * @see #setVar(String, Object)
	 */
	@SuppressWarnings("unchecked")
	private String setVar(String target, Object content, int ignoreLoopStackLevel) throws EvalError
	// END KGU#307 2016-12-12
	{
		// START KGU#375 2017-03-30: Enh. #388 - Perform a clear case analysis instead of some heuristic poking
		// We refer to the cases listed in the javadoc of method setVar(target, content).
		boolean isConstant = false;
		String recordName = null;
		TypeMapEntry compType = null;
		StringList typeDescr = null;
		String indexStr = null;

		// ======== PHASE 1: Analysis of the target structure ===========

		StringList tokens = Element.splitLexically(target, true);
		tokens.removeAll(" ");
		int nTokens = tokens.count();
		String token0 = tokens.get(0).toLowerCase();
		if ((isConstant = token0.equals("const")) || token0.equals("var") || token0.equals("dim")) {
			// a), c), d), e), f) ?
			tokens.remove(0);
			// Extract type information
			int posColon = tokens.indexOf(":");
			if (posColon < 0 && !token0.equals("var")) posColon = tokens.indexOf("as", false);
			if (posColon >= 0) {
				typeDescr = tokens.subSequence(posColon+1, nTokens);
				tokens = tokens.subSequence(0, posColon);
				// In case of an explicit and Pascal- or BASIC-style variable declaration the target must be an unqualified identifier
				if (tokens.contains(".")) {
					throw new EvalError(control.msgConstantRecordComponent.getText().replace("%", target), null, null);
				}
				if (tokens.contains("[")) {
					throw new EvalError(control.msgConstantArrayElement.getText().replace("%", target), null, null);
				}
			}
			nTokens = tokens.count();
			target = tokens.get(nTokens-1);
			// START KGU#388 2017-09-18: Enh. #423 - Register the declared type
			associateType(target, typeDescr);
			// END KGU#388 2017-09-18
		}
		// Now it must be some C or Java style declaration or just a plain variable (possibly indexed or qualified or both)
		// START KGU#388 2017-09-14: Enh. #423 - We try recursively to track cases g) and h) down
//		else if (tokens.get(nTokens-1).equals("]")) {
//			// b) indexed variable or d) a C-style array declaration or g) or h)?
//			int posLBrack = tokens.indexOf("[");
//			if (posLBrack < 1) {
//				throw new EvalError(control.msgInvalidExpr.getText().replace("%1", tokens.concatenate(" ")), null, null);
//			}
//			else {
//				name = tokens.get(posLBrack-1);
//				if (posLBrack == 1) {
//					indexStr = tokens.concatenate(" ");
//					if (isConstant) {
//						throw new EvalError(control.msgConstantArrayElement.getText().replace("%", indexStr), null, null);
//					}
//				}
//			}
//		}
		// qualified or indexed or both?
		else if (tokens.indexOf(".") == 1 || tokens.get(nTokens-1).equals("]")) {
			// FIXME: Face a mixed encapsulation of arrays and records
			// In case of a record component access there must not be modifiers
			if (tokens.indexOf(".") == 1) {
				TypeMapEntry recordType = null;
				// The base variable name should be the last identifier in the series
				target = tokens.get(0);
				recordType = this.identifyRecordType(target, false);	// This will only differ from null if it's a record type
				recordName = target;
				// Now check recursively for record component names 
				while (recordType != null && nTokens >= 3 && tokens.get(1).equals(".") && Function.testIdentifier(tokens.get(2), false, null)) {
					LinkedHashMap<String, TypeMapEntry> comps = recordType.getComponentInfo(false);
					String compName = tokens.get(2);
					if (comps.containsKey(compName)) {
						// If this is in turn a record type, it may be going on recursively...
						target += "." + compName;
						compType = comps.get(compName);
						if (compType.isRecord()) {
							recordType = compType;
						}
						else {
							recordType = null;
						}
						tokens.set(0, target);
						tokens.remove(1, 3);
						nTokens -= 2;
					}
					else {
						throw new EvalError(control.msgInvalidExpr.getText().replace("%1", target + "." + compName), null, null);
					}
				}
				if (isConstant) {
					throw new EvalError(control.msgConstantRecordComponent.getText().replace("%", target), null, null);
				}
				if (this.isConstant(recordName)) {
					throw new EvalError(control.msgConstantRedefinition.getText().replace("%", recordName), null, null);
				}
			}
			if (tokens.get(nTokens-1).equals("]")) {
				// b) indexed variable or d) a C-style array declaration?
				int posLBrack = tokens.indexOf("[");
				if (posLBrack < 1 || recordName != null && posLBrack > 1) {
					throw new EvalError(control.msgInvalidExpr.getText().replace("%1", tokens.concatenate(" ")), null, null);
				}
				else {
					target = tokens.get(posLBrack-1);
					if (posLBrack == 1) {
						// START KGU#773 2019-11-28: Bugfix #786 - To insert spaces wasn't helpful
						//indexStr = tokens.concatenate(" ");
						indexStr = tokens.concatenate(null);
						// END KGU#773 2019-11-28
						// START KGU#490 2018-02-08: Bugfix #503 - we must apply string comparison conversion after decomposition
						// A string comparison in the index string  may be unlikely but not impossible
						indexStr = this.convertStringComparison(indexStr);
						// END KGU#490 2018-02-08
						if (isConstant) {
							throw new EvalError(control.msgConstantArrayElement.getText().replace("%", indexStr), null, null);
						}
					}
				}
			}
		}
		// END KGU#388 2017-09-14
		else {
			// The standard case: a) or c)
			// START KGU#388 2017-09-18: Register the declared type if it's a defined type name
			if (nTokens == 2) {
				typeDescr = tokens.subSequence(0, nTokens - 1);
				associateType(tokens.get(1), typeDescr);
			}
			// END KGU#388 2017-09-18
			target = tokens.get(nTokens-1);
		}
		
		// ======== PHASE 2: Check of loop variable violations ===========
		
		// FIXME: target may still contain type and other modifiers, so this check might fail!
		// START KGU#307 2016-12-12: Enh. #307 - check FOR loop variable manipulation
		if (context.forLoopVars.lastIndexOf(target, ignoreLoopStackLevel) >= 0)
		{
			throw new EvalError(control.msgForLoopManipulation.getText().replace("%", target), null, null);
		}
		// END KGU#307 2016-12-12
		
		// ======== PHASE 3: Precautions against violation of constants ===========
		// START KGU#375 2017-03-30: Enh. #388 - check redefinition of constant
		if (this.isConstant(target) || recordName != null && this.isConstant(recordName)) {
			throw new EvalError(control.msgConstantRedefinition.getText().replace("%", target), null, null);
		}
		
		// Avoid sharing an array if the target is a constant (while the source may not be) 
		if (isConstant && content instanceof Object[]) {
			// START KGU#439 2017-10-13: Enh. #436
			//content = ((Object[])content).clone();
			ArrayList<Object> newContent = new ArrayList<Object>(((Object[])content).length);
			for (Object elem: (Object[])content) {
				newContent.add(elem);
			}
			content = newContent;
			// END KGU#439 2017-10-13
		}
		// END KGU#375 2017-03-30
		// START KGU#439 2017-10-13: Enh. #436
		else if (isConstant && content instanceof ArrayList<?>) {
			// FIXME: This is only a shallow copy, we might have to clone all values as well
			content = new ArrayList<Object>((ArrayList<?>)content);
		}
		// END KGU#439 2017-10-13
		// START KGU#388 2017-09-14: Enh. #423
		else if (isConstant && content instanceof HashMap<?,?>) {
			// FIXME: This is only a shallow copy, we might have to clone all values as well
			// START KGU#526 2018-08-01: Preserve component order (if it had actually been a LinkedHashMap all the better)
			content = new LinkedHashMap<String, Object>((HashMap<String, Object>)content);
			// END KGU#526 2018-08-01
		}
		// END KGU#388 2017-09-14
		
		// MODIFIED BY GENNARO DONNARUMMA, ARRAY SUPPORT ADDED
		// Fundamentally revised by Kay Gürtzig 2015-11-08

		// START KGU#375 2017-03-30: Enh. #388 - replaced by preparing code above
//		String arrayname = null;
//		if ((name.contains("[")) && (name.contains("]")))
//		{
//			arrayname = name.substring(0, name.indexOf("["));
//			// START KGU#109 2015-12-16: Bugfix #61: Several strings suggest type specifiers
//			String[] nameParts = arrayname.split(" ");
//			arrayname = nameParts[nameParts.length-1];
//			// END KGU#109 2015-12-15
//		// START KGU#359 2017-03-06: Bugfix #369 for typed array initialisation like int a[3] <- {4, 9, 2}
//			if (nameParts.length > 1) {
//				// This is rather a C-style array declaration (initialized) than an array
//				// element assignment. The important question is now, whether the
//				// expression represents an array. Then we would drop the "index"
//				// (which is indeed a size) or check it against the array size.
//				name = arrayname;
//				arrayname = null;
//			}
//		}
//		if (arrayname != null) {
//		// Now all is fine here...
//		// END KGU#359 2017-03-06 
//			boolean arrayFound = this.variables.contains(arrayname);
//			int index = this.getIndexValue(name);
		
		// ======== PHASE 4: Structure-aware value assignment ===========

		// -------- Step 4 a: Array element assignment ----------------------- 
		if (indexStr != null) {
		// END KGU#375 2017-03-30
			boolean arrayFound = context.variables.contains(target);
			boolean componentArrayFound = compType != null && context.variables.contains(recordName) && compType.isArray();
			int index = this.getIndexValue(indexStr);
			ArrayList<Object> objectArray = null;
			Object record = null;
			HashMap<String, Object> parentRecord = null;
			int oldSize = 0;
			if (arrayFound)
			{
				// START KGU#439 2017-10-13: Issue #436
//				try {
//					// If it hasn't been an array then we'll get an error here
//					//objectArray = (Object[]) this.interpreter.get(arrayname);
//					objectArray = (Object[]) context.interpreter.get(target);
//					oldSize = objectArray.length;
//				}
//				catch (Exception ex)
//				{
//					// Produce a meaningful EvalError instead
//					//this.interpreter.eval(arrayname + "[" + index + "] = " + prepareValueForDisplay(content));
//					this.evaluateExpression(target + "[" + index + "] = " + prepareValueForDisplay(content), false);
//				}
				Object targetObject = this.context.interpreter.get(target);
				if (targetObject == null && context.dynTypeMap.containsKey(target) && context.dynTypeMap.get(target).isArray()) {
					// KGU#432: The variable had been declared as array but not initialized - so be generous here
					objectArray = new ArrayList<Object>();
				}
				else if (targetObject instanceof ArrayList) {
					objectArray = (ArrayList<Object>)targetObject;
					oldSize = objectArray.size();
				}
				else {
					// FIXME: Produce a more meaningful EvalError
					this.evaluateExpression(target + "[" + index + "] = " + prepareValueForDisplay(content, context.dynTypeMap), false, true);
				}
				// END KGU#439 2017-10-13
			}
			else if (componentArrayFound)
			{
				// Now get the original array component
				StringList path = StringList.explode(target, "\\.");
				record = context.interpreter.get(path.get(0));	// base record
				if (record == null) {
					record = this.createEmptyRecord(path, 0);
				}
				Object comp = record;
				for (int i = 1; i < path.count(); i++) {
					parentRecord = (HashMap<String,Object>)comp;
					comp = parentRecord.get(path.get(i));
					if (comp == null && i < path.count()-1) {
						comp = this.createEmptyRecord(path, i);
						parentRecord.put(path.get(i), comp);
					}
				}
				if (comp == null) {
					objectArray = new ArrayList<Object>();
				}
				else if (comp instanceof ArrayList<?>) {
					objectArray = (ArrayList<Object>)comp;
					oldSize = objectArray.size();
				}
				else {
					String valueType = Instruction.identifyExprType(context.dynTypeMap, prepareValueForDisplay(comp, null), true);
					throw new EvalError(control.msgTypeMismatch.getText().
							replace("%1", valueType).
									replace("%2", compType.getCanonicalType(true, true)).
									replace("%3", target), null, null);
				}
			}
			if (index > oldSize - 1) // This includes the case of oldSize = 0
			{
				// START KGU#439 2017-10-13: Issue #436
//				Object[] oldObjectArray = objectArray;
//				objectArray = new Object[index + 1];
//				for (int i = 0; i < oldSize; i++)
//				{
//					objectArray[i] = oldObjectArray[i];
//				}
//				for (int i = oldSize; i < index; i++)
//				{
//					objectArray[i] = new Integer(0);
//				}
				if (objectArray == null) {
					objectArray = new ArrayList<Object>(index+1);
				}
				// This adds dummy elements until inclusively index
				for (int i = oldSize; i <= index; i++) {
					objectArray.add(0);
				}
				// END KGU#439 2017-10-13
			}
			//objectArray[index] = content;
			objectArray.set(index, content);
			//this.interpreter.set(arrayname, objectArray);
			//this.variables.addIfNew(arrayname);
			if (componentArrayFound) {
				//try {
					StringList path = StringList.explode(target, "\\.");
					parentRecord.put(path.get(path.count()-1), objectArray);
					context.interpreter.set(recordName, record);
				//}
				//catch (Exception ex)
				//{
				//	// Produce a meaningful EvalError instead
				//	//this.interpreter.eval(arrayname + "[" + index + "] = " + prepareValueForDisplay(content));
				//	this.evaluateExpression(target + "[" + index + "] = " + prepareValueForDisplay(content), false);
				//}
				
			}
			else {
				context.interpreter.set(target, objectArray);
				context.variables.addIfNew(target);
			}
		}
		// START KGU#388 2017-09-14: Enh. #423 Special treatment for record components
		// -------- Step 4 b: Record component assignment -------------------- 
		else if (recordName != null) {
			StringList path = StringList.explode(target, "\\.");
			try {
				Object record = context.interpreter.get(recordName);
				if (record == null && path.count() == 2) {
					record = createEmptyRecord(path, 0);
				}
				// START KGU#568 2018-08-01: Avoid a dull NullPointerException
				else if (record == null || !(record instanceof HashMap)) {
					throw new EvalError(control.msgInvalidRecord.getText().replace("%1", recordName).replace("%2", String.valueOf(record)), null, null);
				}
				// END KGU#568 2018-08-01
				Object comp = record;
				for (int i = 1; i < path.count()-1; i++) {
					Object subComp = ((HashMap<?, ?>)comp).get(path.get(i));
					if (subComp == null && i == path.count()-2) {
						// We tolerate that the penultimate level is unset...
						subComp = this.createEmptyRecord(path, i);
						((HashMap<String, Object>)comp).put(path.get(i), subComp);
					}
					else if (!(subComp instanceof HashMap<?,?>)) {
						throw new EvalError(control.msgInvalidComponent.getText().replace("%1", path.get(i-1)).replace("%2", path.concatenate(".",0,i-1)), null, null);
					}
					comp = subComp;
				}
				((HashMap<String, Object>)comp).put(path.get(path.count()-1), content);
				context.interpreter.set(recordName, record);
				// START KGU#580 2018-09-24
				target = recordName;	// this is the variable name to be returned
				// END KGU#580 2018-09-24
			}
			catch (EvalError ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new EvalError(ex.toString(), null, null);
			}
		}
		// END KGU#388 2017-09-14
		// -------- Step 4 c: assignment to a plain variable ----------------------- 
		else // indexString == null && recordName == null
		{
			// START KGU#375 2017-03-30: Enh. #388 - this has all been done already now
//			// START KGU#109 2015-12-16: Bugfix #61: Several strings suggest type specifiers
//			// START KGU#109 2016-01-15: Bugfix #61,#107: There might also be a colon...
//			int colonPos = name.indexOf(":");	// Check Pascal and BASIC style as well
//			if (colonPos > 0 || (colonPos = name.indexOf(" as ")) > 0)
//			{
//				name = name.substring(0, colonPos).trim();
//			}
//			// END KGU#109 2016-01-15
//			String[] nameParts = name.split(" ");
//			name = nameParts[nameParts.length-1];
//			// END KGU#109 2015-12-15
			// END KGU#375 2017-03-30

			// START KGU#388 2017-09-14: Enh. #423
			// Throw an error if a record is assigned to a used but undeclared, non-record or wrong-record-type variable
			// or vice versa 
			if (content instanceof HashMap<?,?>) {
				String typeName = ((HashMap<?, ?>)content).get("§TYPENAME§").toString();
				if ((context.variables.contains(target) || typeDescr != null)
						&& (!context.dynTypeMap.containsKey(target) || (compType = context.dynTypeMap.get(target)) == null || !compType.isRecord()
						|| !compType.typeName.equals(typeName))) {
					String compTypeStr = "???";
					if (compType != null) {
						compTypeStr = compType.getCanonicalType(true, true).replace("@", "array of ");
					}
					throw new EvalError(control.msgTypeMismatch.getText().
							replace("%1", ((HashMap<?, ?>)content).get("§TYPENAME§").toString()).
							replace("%2", compTypeStr).
							replace("%3", target), null, null);
				}
			}
			else if (content != null && (context.dynTypeMap.containsKey(target) && (compType = context.dynTypeMap.get(target)) != null
				|| typeDescr != null && typeDescr.count() == 1 && (compType = context.dynTypeMap.get("%" + typeDescr.get(0))) != null)
					&& compType.isRecord() ) {
				throw new EvalError(control.msgTypeMismatch.getText().
						replace("%1", content.toString()).
						replace("%2", compType.typeName).
						replace("%3", target), null, null);
			}

			// START KGU#322 2017-01-06: Bugfix #324 - an array assigned on input hindered scalar re-assignment
			//this.interpreter.set(name, content);
			try {
				context.interpreter.set(target, content);
			}
			catch (EvalError ex) {
				if (MTCH_EVAL_ERROR_ARRAY.reset(ex.getMessage()).matches()) {
					// Stored array type is an obstacle for re-assignment, so drop it
					context.interpreter.unset(target);
					// Now try again
					context.interpreter.set(target, content);
				}
				else {
					// Something different, so rethrow
					throw ex;
				}
			}
			// END KGU#322 2017-01-06
			
			// MODIFIED BY GENNARO DONNARUMMA
			// PREVENTING DAMAGED STRING AND CHARS
			// FIXME (KGU): Seems superfluous or even dangerous (Addendum 2015-12-10: Now the aim became clear by issue #49)
//			if (content != null)
//			{
//				if (content instanceof String)
//				{
//					content = ((String) content).replaceAll("\"\"", "\"");
//				}
//				else if (content instanceof Character)
//				{
//					content = new String("'" + content + "'");
//				}
//			}
//			this.interpreter.eval(name + " = " + content);	// What the heck is this good for, now?
			// START KGU#99 2015-12-10: Bugfix #49 - for later comparison etc. we try to replace wrapper objects by simple values
			// FIXME: Why is String also excluded here?
			if (! (content instanceof String || content instanceof Character || content instanceof ArrayList<?> || content instanceof HashMap<?,?>))
			{
				try {
					this.evaluateExpression(target + " = " + content, false, false);	// Avoid the variable content to be an object
				}
				catch (EvalError ex)	// Just ignore an error (if we may rely on the previously set content to survive)
				{}
			}
			// END KGU#99 2015-12-10
			context.variables.addIfNew(target);
			// START KGU#375 2017-03-30: Enh. #388
			if (isConstant) {
				context.constants.put(target, context.interpreter.get(target));
			}
			// END KGU#375 2017-03-30
		}
		
		// START KGU#20 2015-10-13: In step mode, variable display should be updated even if delay is set to 0
//		if (this.delay != 0)
//		{
//			Vector<Vector> vars = new Vector();
//			for (int i = 0; i < this.variables.count(); i++)
//
//			{
//				Vector myVar = new Vector();
//				myVar.add(this.variables.get(i));
//				myVar.add(this.interpreter.get(this.variables.get(i)));
//				vars.add(myVar);
//			}
//			this.control.updateVars(vars);
//		}
		
		if (this.delay != 0 || step)
		{
			updateVariableDisplay();
		}
		// END KGU#20 2015-10-13
		// START KGU#580 2018-09-24: Bugfix #605
		return target;	// Base name of the assigned variable or constant
		// END KGU#580 2018-09-24
	}

	/**
	 * Detects whether the StringList {@code _typeDescr} specifies a defined type and if so
	 * associates the latter to the given variable or constant name {@code target} in {@code this.context.dynTypeMap}.
	 * @param target - a variable or constant identifier
	 * @param typeDescr - a {@link StringList} comprising a found type description
	 */
	private void associateType(String target, StringList typeDescr) {
		String typeName = null;
		if (typeDescr != null && typeDescr.count() == 1 && Function.testIdentifier(typeName = typeDescr.get(0), false, null)
				&& context.dynTypeMap.containsKey(":" + typeName)) {
			context.dynTypeMap.put(target, context.dynTypeMap.get(":" + typeName));
		}
		// In other cases we cannot create a new TypeMapEntry because we are lacking element and line information here.
		// So it is up to the calling method...
	}
	
	private HashMap<String, Object> createEmptyRecord(StringList path, int depth) {
		TypeMapEntry recordType = this.identifyRecordType(path.get(0), false);
		for (int i = 1; i <= depth; i++) {
			recordType = recordType.getComponentInfo(true).get(path.get(i));
		}
		return createEmptyRecord(recordType);
	}
	private HashMap<String, Object> createEmptyRecord(TypeMapEntry recordType) {
		// START KGU#526 2018-08-01: Preserve component order
		//HashMap<String, Object> record = new HashMap<String, Object>();
		HashMap<String, Object> record = new LinkedHashMap<String, Object>();
		// END KGU#526 2018-08-01
		for (String compName: recordType.getComponentInfo(true).keySet()) {
			record.put(compName, null);
		}
		record.put("§TYPENAME§", recordType.typeName);
		return record;
	}

	/**
	 * Checks if the name described by {@code typeOrVarName} represents a record and if so
	 * returns the respective TypeMapEntry, otherwise null.
	 * @param typeOrVarName - a string sequence of modifiers, ids, and possible selectors 
	 * @param isTypeName - must be true for a type name and false for a var/const name.
	 * @return a TypeMapEntry for a record type or null
	 */
	private TypeMapEntry identifyRecordType(String typeOrVarName, boolean isTypeName)
	{
		TypeMapEntry recordType = context.dynTypeMap.get((isTypeName ? ":" : "") + typeOrVarName);
		
		if (recordType != null && !recordType.isRecord()) {
				recordType = null;
		}
		
		return recordType;
	}

	// START KGU#20 2015-10-13: Code from above moved hitherto and formed to a method
	/**
	 * Prepares an editable variable table and has the Control update the display
	 * of variables with it
	 */
	private void updateVariableDisplay() throws EvalError
	{
		Vector<String[]> vars = new Vector<String[]>();
		for (int i = 0; i < context.variables.count(); i++)
		{
			String varName = context.variables.get(i);
			// START KGU#67 2015-11-08: We had to find a solution for displaying arrays in a sensible way
			//myVar.add(this.interpreter.get(this.variables.get(i)));
			Object val = context.interpreter.get(varName);
			String valStr = prepareValueForDisplay(val, context.dynTypeMap);
			// START KGU#542 2019-11-20: Enh. #739 - support enumeration types
			TypeMapEntry varType = context.dynTypeMap.get(varName);
			if (varType != null && varType.isEnum() && val instanceof Integer) {
				int testVal = ((Integer)val).intValue();
				String enumStr = decodeEnumValue(testVal, varType);
				if (enumStr != null) {
					if (enumStr.equals(varName)) {
						// This is the enumerator itself (a constant), so append the type name
						valStr += " (" + varType.typeName + ")";
					}
					else {
						// For variables just holding the enumarator value, just show the name instead
						valStr = enumStr;
					}
				}
			}
			// END KGU#542 2019-11-20
			// END KGU#67 2015-11-08
			vars.add(new String[]{varName, valStr});
		}
		this.control.updateVars(vars);
		// START KGU#2 (#9) 2015-11-14
		this.control.updateCallLevel(this.callers.size());
		// END#2 (#9) KGU 2015-11-14
	}
	// END KGU#20 2015-10-13
	
	// START KGU#67/KGU#68 2015-11-08: We have to present values in an editable way (recursively!)
	// START KGU#526 2018-08-01: Enh. #423 - new optional argument to improve record presentation
	//protected static String prepareValueForDisplay(Object val, HashMap<String)
	protected static String prepareValueForDisplay(Object val, HashMap<String, TypeMapEntry> typeMap)
	// END KGU#526 2018-08-01
	{
		String valStr = "";
		if (val != null)
		{
			valStr = val.toString();
			if (val instanceof ArrayList)
			{
				valStr = "{";
				ArrayList<?> valArray = (ArrayList<?>)val;
				for (int j = 0; j < valArray.size(); j++)
				{
					String elementStr = prepareValueForDisplay(valArray.get(j), typeMap);
					valStr = valStr + ((j > 0) ? ", " : "") + elementStr;
				}
				valStr = valStr + "}";
			}
			// START KGU#388 2017-09-14: Enh. #423
			// START KGU#526 2018-08-01: Enh. #423
			//if (val.getClass().getSimpleName().equals("HashMap")) {
			if (val instanceof HashMap) {
			// END KGU#526 2018-08-01
				// In case we have access to a type map provide the declared component order.
				HashMap<?, ?> hmVal = (HashMap<?, ?>)val;
				String typeName = String.valueOf(hmVal.get("§TYPENAME§"));
				valStr = typeName + "{";
				// START KGU#526 2018-08-01: Enh. #423 - Try to preserve component order
				TypeMapEntry typeInfo = null;
				int j = 0;
				if (typeMap != null && (typeInfo = typeMap.get(":"+typeName)) != null && typeInfo.isRecord()) {
					for (String compName: typeInfo.getComponentInfo(true).keySet()) {
						if (hmVal.containsKey(compName)) {
							String elementStr = prepareValueForDisplay(hmVal.get(compName), typeMap);
							valStr += ((j++ > 0) ? ", " : "") + compName + ": " + elementStr;
						}
					}
				}
				else {
				// END KGU#526 2018-08-01
					for (Entry<?, ?> entry: hmVal.entrySet())
					{
						if (entry.getKey() instanceof String) {
							String key = (String)entry.getKey();
							if (!key.startsWith("§")) {
								String elementStr = prepareValueForDisplay(entry.getValue(), typeMap);
								valStr += ((j++ > 0) ? ", " : "") + key + ": " + elementStr;
							}
						}
					}
				// START KGU#526 2018-08-01: Enh. #423 (continuation)
				}
				// END KGU#526 2018-08-01
				valStr = valStr + "}";
			}
			// END KGU#388 2017-09-14
			else if (val instanceof String)
			{
				// START KGU#285 2016-10-16: Bugfix #276
				valStr = valStr.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
				// END KGU#285 2016-10-16
				valStr = "\"" + valStr + "\"";
			}
			else if (val instanceof Character)
			{
				// START KGU#285 2016-10-16: Bugfix #276
				valStr = valStr.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
				// END KGU#285 2016-10-16
				valStr = "'" + valStr + "'";
			}
		}
		return valStr;
	}
	// END KGU#67/KGU#68 2015-11-08
	
	// START KGU#68 2015-11-06 - modified 2016-10-07 for improved thread-safety
	/**
	 * Tries to commit the variable manipulations prepared in the Control display
	 * and reports the manipulations to the Output Console Window.
	 * If errors occur, then the returned {@link StringList} will contain the respective
	 * messages.
	 * @param newValues - a {@link HashMap} containing the names and new value strings
	 * of manipulated variables.
	 * @return a {@link StringList} containing obtained evluation errors.
	 */
	@SuppressWarnings("unchecked")
	public StringList adoptVarChanges(HashMap<String,Object> newValues)
	{
		StringList errors = new StringList();
		String tmplManuallySet = control.lbManuallySet.getText();	// The message template
		for (HashMap.Entry<String, Object> entry: newValues.entrySet())
		{
			String varName = entry.getKey();
			try {
				TypeMapEntry type = context.dynTypeMap.get(varName);
				Object oldValue = context.interpreter.get(varName);
				Object newValue = entry.getValue();
				// START KGU#443 2017-10-29: Issue #439 Precaution against unnecessary value overwriting
				String oldValStr = prepareValueForDisplay(oldValue, context.dynTypeMap);
				if (oldValStr.equals(newValue.toString())) {
					// If there are no visible changes then we avoid reconstruction of the value
					// from string because this might lead to broken references without need.
					continue;
				}
				// END KGU#443 2017-10-29
				// START KGU#160 2016-04-12: Enh. #137 - text window output
				// START KGU#197 2016-05-05: Language support extended
				//this.console.writeln("*** Manually set: " + varName + " <- " + newValues[i] + " ***", Color.RED);
				if (this.console.logMeta()) {				
					this.console.writeln(tmplManuallySet.replace("%1", varName).replace("%2", newValue.toString()), Color.RED);
				}
				// END KGU#197 2016-05-05
				if (isConsoleEnabled)
				{
					this.console.setVisible(true);
				}
				// END KGU#160 2016-04-12

				if (oldValue != null && oldValue instanceof ArrayList<?>)
				{
					// In this case an initialisation expression ("{ ..., ..., ...}") is expected
					int oldSize = ((ArrayList<Object>)oldValue).size();
					//String asgnmt = "Object[] " + tmpVarName + " = " + newValue;
					//this.evaluateExpression(asgnmt, true, false);
					Object newObject = this.evaluateExpression(newValue.toString(), true, false);
					//context.interpreter.unset(tmpVarName);
					if (newObject instanceof ArrayList<?>) {
						int newSize = ((ArrayList<Object>)newObject).size();
						// First replace existing elements
						for (int i = 0; i < Math.min(oldSize, newSize); i++) {
							((ArrayList<Object>)oldValue).set(i, ((ArrayList<Object>)newObject).get(i));
						}
						// If the array has shrunk then get rid of obsolete elements
						for (int i = oldSize-1; i >= newSize; i--) {
							((ArrayList<Object>)oldValue).remove(i);
						}
						// If the element has grown then add the additional values
						for (int i = oldSize; i < newSize; i++) {
							((ArrayList<Object>)oldValue).add(((ArrayList<Object>)newObject).get(i));
						}
					}
					else {
						// FIXME check if the variable had explicitly been declared as Array - in this case refuse
						context.interpreter.set(varName, newObject);
					}
//					// Okay, but now we have to sort out some un-boxed strings
//					Object[] objectArray = (Object[]) interpreter.get(varName);
//					for (int j = 0; j < objectArray.length; j++)
//					{
//						Object content = objectArray[j];
//						if (content != null)
//						{
//							System.out.println("Updating " + varName + "[" + j + "] = " + content.toString());
//							this.interpreter.set("structorizer_temp", content);
//							this.interpreter.eval(varName + "[" + j + "] = structorizer_temp");
//						}
//					}
				}
				// START KGU#388 2017-10-08: Enh. #423
				else if (type != null && type.isRecord()) {
					// START KGU#439 2017-10-13: Issue #436 We must not break references
					//context.interpreter.set(varName, evaluateExpression((String)newValue, true));
					Object newObject = evaluateExpression((String)newValue, true, false);
					if (oldValue instanceof HashMap && newObject instanceof HashMap) {
						for (String key: type.getComponentInfo(true).keySet()) {
							if (!key.startsWith("§")) {
								if (((HashMap<String, Object>)newObject).containsKey(key)) {
									((HashMap<String, Object>)oldValue).put(key, ((HashMap<String, Object>)newObject).get(key));
								}
								else {
									((HashMap<String, Object>)oldValue).remove(key);
								}
							}
						}
					}
					else if (oldValue == null) {
						context.interpreter.set(varName, newObject);
					}
					// END KGU#439 2017-10-13
				}
				// START KGU#542 2019-11-21: Enh. #739 - support enum types
				else if (type != null && type.isEnum() && context.constants.containsKey(newValue)) {
					Object constVal = context.constants.get(newValue);
					if (constVal instanceof Integer && newValue.equals(this.decodeEnumValue((Integer)constVal, type))) {
						context.interpreter.set(varName, constVal);
					}
					else {
						setVarRaw(varName, (String)newValue);
					}
				}
				// END KGU#542 2019-11-21
				else
				{
					setVarRaw(varName, (String)newValue);
				}
				// END KGU#388 2017-10-08
			}
			catch (EvalError err) {
				// START KGU#441 2017-10-13: Enh. #437
				errors.add(varName + ": " + err.getMessage());
				// END KGU#441 2017-10-13
				logger.log(Level.WARNING, "adoptVarChanges({}) on {0}: {1}", new Object[]{newValues, varName, err.getMessage()});
			}
		}
		return errors;
	}
	// END KGU#68 2015-11-06
	
	// START KGU#542 2019-11-21: Enh. #739 Support for enumeration types
	private String decodeEnumValue(int testVal, TypeMapEntry varType) {
		int itemVal = 0;
		StringList enumInfo = varType.getEnumerationInfo();
		for (int j = 0; j < enumInfo.count(); j++) {
			String[] enumItem = enumInfo.get(j).split("\\s*=\\s*", 2);
			if (enumItem.length > 1) {
				try {
					Object e1 = context.interpreter.eval(enumItem[1]);
					if (e1 instanceof Integer && (Integer)e1 >= 0) {
						itemVal = ((Integer)e1).intValue();
					}
				}
				catch (EvalError ex) {}
			}
			if (testVal == itemVal) {
				return enumItem[0];
			}
			itemVal++;
		}
		return null;
	}
	// END KGU#542 2019-11-21
	
	// START KGU#375 2017-03-30: Auxiliary callback for Control
	public boolean isConstant(String varName)
	{
		return context.constants.containsKey(varName.trim());
	}
	// END KGU#375 2017-03-30
	
	// START KGU#542 2019-11-21: Enh. #739 support for enumerator types
	public boolean isEnumerator(String varName)
	{
		TypeMapEntry type = context.dynTypeMap.get(varName);
		return type != null && type.isEnum();
	}
	
	public StringList getEnumeratorValuesFor(String varName)
	{
		TypeMapEntry type = context.dynTypeMap.get(varName);
		if (type == null || !type.isEnum()) {
			return null;
		}
		StringList names = type.getEnumerationInfo();
		for (int i = 0; i < names.count(); i++) {
			int posEqu = names.get(i).indexOf('=');
			if (posEqu >= 0) {
				names.set(i,  names.get(i).substring(0, posEqu).trim());
			}
		}
		return names;
	}
	// END KGU#542 2019-11-21

	public void start(boolean useSteps)
	{
		paus = useSteps;
		step = useSteps;
		stop = false;

		control.updateVars(new Vector<String[]>());
		
		running = true;
		Thread runner = new Thread(this, "Player");
		runner.start();
	}
	
	// START KGU#43 2015-10-12 New method for breakpoint support
	private boolean checkBreakpoint(Element element)
	{
		// START KGU#213 2016-08-01: Enh. #215
		//boolean atBreakpoint = element.isBreakpoint();
		boolean atBreakpoint = element.triggersBreakNow();
		// END KGU#213 2016-08-01
		// START KGU#276 2016-11-19: Issue #267: in paused mode we should move the focus to the current element
		if (delay > 0 || step || atBreakpoint) {
			diagram.redraw(element);
		}
		// END KGU#276 2016-11-19
		if (atBreakpoint) {
			// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
			//control.setButtonsForPause();
			control.setButtonsForPause(false);	// This avoids interference with the pause button
			// END KGU#379 2017-04-12
			this.setPaus(true);
		}
		return atBreakpoint;
	}
	// END KGU#43 2015-10-12

	// START KGU 2015-10-13: Decomposed this "monster" method into Element-type-specific subroutines
	private String step(Element element)
	{
		String trouble = new String();
		// START KGU#277 2016-10-13: Enh. #270: skip the element if disabled
		if (element.disabled) {
			return trouble;
		}
		// END KGU#277 2016-10-13
		
		element.executed = true;
		// START KGU#143 2016-01-21: Bugfix #114 - make sure no compromising editing is done
		diagram.doButtons();
		// END KGU#143 2016-01-21
		// START KGU#43 2015-10-12: If there is a breakpoint switch to step mode before delay
		// START KGU#665 2019-02-26: Bugfix #687 a breakpointed Repeat loop must not pause when entered
		//checkBreakpoint(element);
		if (!(element instanceof Repeat)) {
			checkBreakpoint(element);
		}
		// END KGU#665 2019-02-26
		// END KGU#43 2015-10-12

		// START KGU#477 2017-12-10: Enh. #487 - check continuation of 
		if (!(element instanceof Instruction) || !((Instruction)element).isMereDeclaratory()) {
			this.lastDeclarationSurrogate = null;
		}
		else if (this.lastDeclarationSurrogate == null) {
			this.lastDeclarationSurrogate = (Instruction)element;
		}
		else if (Element.E_HIDE_DECL) {
			this.lastDeclarationSurrogate.executed = true;
		}
		// END KGU#477 2017-12-10
		// The Root element, REPEAT loop, and TRY block won't be delayed or halted in the beginning except by their members
		if (element instanceof Root)
		{
			trouble = stepRoot((Root)element);
		} else if (element instanceof Repeat)
		{
			trouble = stepRepeat((Repeat)element);
		}
		// START KGU#686 2019-03-16: Enh. #56
		else if (element instanceof Try) {
			trouble = stepTry((Try)element);
		}
		// END KGU#686 2019-03-16
		else 
		{
			// Delay or wait (in case of step mode or breakpoint) before
			delay();	// does the delaying or waits in case of step mode or breakpoint
			
			// START KGU#2 2015-11-14: Separate execution for CALL elements to keep things clearer
			//if (element instanceof Instruction)
			if (element instanceof Call)
			{
				trouble = stepCall((Call)element);
			}
			// START KGU#78 2015-11-25: Separate handling of JUMP instructions
			else if (element instanceof Jump)
			{
				trouble = stepJump((Jump)element);
			}
			// END KGU#78 2015-11-25
			else if (element instanceof Instruction)
			// END KGU#2 2015-11-14
			{
				trouble = stepInstruction((Instruction)element);
			} else if (element instanceof Case)
			{
				trouble = stepCase((Case)element);
			} else if (element instanceof Alternative)
			{
				trouble = stepAlternative((Alternative)element);
			} else if (element instanceof While)
			{
				trouble = stepWhile(element, false);
			} else if (element instanceof For)
			{
				trouble = stepFor((For)element);
			}
			// START KGU#44/KGU#47 2015-10-13: Obviously, Forever loops and Parallel sections had been forgotten
			else if (element instanceof Forever)
			{
				trouble = stepWhile(element, true);
			}
			else if (element instanceof Parallel)
			{
				trouble = stepParallel((Parallel)element);
			}
			// END KGU#44/KGU#47 2015-10-13
		}
		if (trouble.equals("")) {
			element.executed = false;
			// START KGU#117 2016-03-07: Enh. #77
			element.checkTestCoverage(false);
			// END KGU#117 2016-03-07
			// START KGU#156 2016-03-11: Enh. #124
			// Increment the execution counters
			element.countExecution();
			// END KGU#156 2016-03-11
		}
		return trouble;
	}

	private String stepRoot(Root element)
	{
		// KGU 2015-11-25: Was very annoying to wait here in step mode
		// and we MUST NOT re-initialize the diagramControllers on a subroutine!
		if ((diagramControllers != null || !step) && callers.isEmpty())
		{
			// START KGU#448 2017-10-28: Enh. #443 use the internal interface not the diagram API
			//getExec("init(" + delay + ")");
			initRootExecDelay();
			// END KGU#448 2017-10-28
		}

		element.waited = true;

		// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
		String trouble = stepSubqueue(element.children, false);
		// END KGU#117 2016-03-07

		return trouble;
	}

	private String stepInstruction(Instruction element)
	{
		String trouble = new String();

		// START KGU#413 2017-06-09: Enh. #416 allow user-defined line concatenation
		//StringList sl = element.getText();
		StringList sl = element.getUnbrokenText();
		// END KGU#413 2017-06-09
		// START KGU#477 2017-12-10: Enh. #487 - special treatment for declaration sequences
		int initialStepCount = element.getExecStepCount(false);
		// END KGU#477 2017-12-10
		int i = 0;

		// START KGU#569 2018-08-06: Issue #577 - circumvent GUI trouble on window output
		boolean repeated = false;
		// END KGU#569 2018-08-06
		// START KGU#77/KGU#78 2015-11-25: Leave if some kind of leave statement has been executed
		//while ((i < sl.count()) && trouble.equals("") && (stop == false))
		while ((i < sl.count()) && trouble.equals("") && (stop == false) &&
				!context.returned && leave == 0)
		// END KGU#77/KGU#78 2015-11-25
		{
			String cmd = sl.get(i).trim();
			// START KGU#388 2017-09-13: Enh. #423 We shouldn't do this for type definitions
			//cmd = convert(cmd).trim();
			// END KGU#388 2017-09-13
			// START KGU#569 2018-08-06: Issue #577 - circumvent GUI trouble on window output
			boolean isOutput = false;
			boolean outputDone = false;
			// END KGU#569 2018-08-06
			try
			{
				// START KGU#508 2018-03-19: Bugfix #525 operation count for all non-typedefs
				boolean isTypeDef = false;
				// END KGU#508 2018-03-19
				
				// START KGU#809 2020-02-20: Issue #822 (derived from #819) Just skip empty lines
				if (cmd.trim().isEmpty()) {
					i++;
					continue;
				}
				// END KGU#809 2020-02-20
				
				// START KGU#490 2018-02-07: Bugfix #503 - we should first rule out input, output instructions, and JUMPs
				//if (!Instruction.isTypeDefinition(cmd, context.dynTypeMap)) {
				//	cmd = convert(cmd).trim();
				// Input (keyword should only trigger this if positioned at line start)
				if (cmd.matches(
						this.getKeywordPattern(CodeParser.getKeyword("input")) + "([\\W].*|$)"))
				{
					trouble = tryInput(cmd);
				}
				// output (keyword should only trigger this if positioned at line start)
				else if (cmd.matches(
						this.getKeywordPattern(CodeParser.getKeyword("output")) + "([\\W].*|$)"))
				{
					// START KGU#569 2018-08-06: Issue #577 - circumvent GUI trouble on window output
					isOutput = true;
					// END KGU#569 2018-08-06
					trouble = tryOutput(cmd);
					// START KGU#569 2018-08-06: Issue #577 - circumvent GUI trouble on window output
					outputDone = true;
					// END KGU#569 2018-08-06
				}
				// return statement
				// The "return" keyword ought to be the first word of the instruction,
				// comparison should not be case-sensitive while CodeParser.preReturn isn't fully configurable,
				// but a separator would be fine...
				else if (cmd.matches(
						this.getKeywordPattern(CodeParser.getKeywordOrDefault("preReturn", "return")) + "([\\W].*|$)"))
				{		 
					trouble = tryReturn(cmd.trim());
				}
				else 
				// START KGU#388 2017-09-13: Enh. #423 We shouldn't do this for type definitions
				if (!Instruction.isTypeDefinition(cmd, context.dynTypeMap)) {
					cmd = convert(cmd, false).trim();	// Do the string comparison analysis after decomposition!
				// END KGU#388 2017-09-13
				// END KGU#490 2018-02-07

					// START KGU#417 2017-06-30: Enh. #424 (Turtleizer functions introduced)
					// FIXME (KGU#490): Is this too early?
					cmd = this.evaluateDiagramControllerFunctions(cmd);
					// END KGU#417 2017-06-30

					// assignment?
					// START KGU#377 2017-03-30: Bugfix
					//if (cmd.indexOf("<-") >= 0)
					if (Element.splitLexically(cmd, true).contains("<-"))
					// END KGU#377 2017-03-30: Bugfix
					{
						trouble = tryAssignment(cmd, element, i);
					}
					// START KGU#332 2017-01-17/19: Enh. #335 - tolerate a Pascal variable declaration
					else if (cmd.matches("^var.*:.*")) {
						// START KGU#388 2017-09-14: Enh. #423
						element.updateTypeMapFromLine(this.context.dynTypeMap, cmd, i);
						// END KGU#388 2017-09-14
						StringList varNames = StringList.explode(cmd.substring("var".length(), cmd.indexOf(":")), ",");
						for (int j = 0; j < varNames.count(); j++) {
							setVar(varNames.get(j).trim(), null);
						}
					}
					else if (cmd.matches("^dim.* as .*")) {
						// START KGU#388 2017-09-14: Enh. #423
						element.updateTypeMapFromLine(this.context.dynTypeMap, cmd, i);
						// END KGU#388 2017-09-14
						StringList varNames = StringList.explode(cmd.substring("dim".length(), cmd.indexOf(" as ")), ",");
						for (int j = 0; j < varNames.count(); j++) {
							setVar(varNames.get(j), null);
						}
					}
					// END KGU#332 2017-01-17/19
					else
					{
						// START KGU#490 2018-02-08: Bugfix #503 - we haven't converted string comp any longer before
						cmd = this.convertStringComparison(cmd);
						// END KGU#490 2018-02-08
						trouble = trySubroutine(cmd, element);
					}
					
				// START KGU#388 2017-09-13: Enh. #423
				}
				else {
					// START KGU#508 2018-03-19: Bugfix #525 operation count for non-typedefs
					// We don't increment the total execution count here - this is regarded as a non-operation
					isTypeDef = true;
					// END KGU#508 2018-03-19
					element.updateTypeMapFromLine(this.context.dynTypeMap, cmd, i);
					// START KGU#542 2019-11-17: Enh. #739 - In case of an enum type definition we have to assign the constants
					String typeDescr = cmd.substring(cmd.indexOf('=')+1).trim();
					if (TypeMapEntry.MATCHER_ENUM.reset(typeDescr).matches()) {
						isTypeDef = false;	// Is to be counted as an ordinary instruction (costs even more)
						HashMap<String, String> enumItems = context.root.extractEnumerationConstants(cmd);
						if (enumItems == null) {
							trouble = Control.msgInvalidEnumDefinition.getText().replace("%", typeDescr);
						}
						else {
							for (Entry<String,String> enumItem: enumItems.entrySet()) {
								String constName = enumItem.getKey();
								// This is the prefixed value
								String enumValue = enumItem.getValue();
								// Check whether the constant may be set or confirmed
								String oldVal = context.root.constants.put(constName, enumValue);
								if (oldVal != null && !enumValue.equals(oldVal) || context.constants.containsKey(constName)) {
									// There had been a differing value before
									trouble = control.msgConstantRedefinition.getText().replace("%", constName);
									break;
								}
								else {
									// This is the pure value (ought to be an integral literal)
									Object trueValue = context.interpreter.eval(context.root.getConstValueString(constName));
									// Now establish the value in the interpreter and the variable display
									setVar("const " + constName, trueValue);
								}
							}
						}
					}
					// END KGU#542 2019-11-17
				}
				// END KGU#388 2017-09-13
				// START KGU#156/KGU#508 2018-03-19: Enh. #124, bugfix #525 - this has to be done for all non-typedefs
				if (!isTypeDef) {
					element.addToExecTotalCount(1, true);	// For the instruction line
				}
				// END KGU#156/KGU#508 2018-03-19
				// START KGU#271: 2016-10-06: Bugfix #261: Allow to step and stop within an instruction block (but no breakpoint here!) 
				if ((i+1 < sl.count()) && trouble.equals("") && (stop == false)
						&& !context.returned && leave == 0)
				{
					delay();
				}
				// END KGU#271 2016-10-06
				// START KGU#569 2018-08-06: Issue #577 - circumvent GUI trouble on window output
				repeated = false;
				// END KGU#569 2018-08-06
			} catch (EvalError ex)
			{
				trouble = ex.getLocalizedMessage();
				if (trouble == null) trouble = ex.getMessage();
				if (trouble.endsWith("TargetError")) {
					String errorText = ex.getErrorText();
					int leftParPos = errorText.indexOf('(');
					int rightParPos = errorText.lastIndexOf(')');
					if (errorText.startsWith("throw") && leftParPos >= 0 && rightParPos > leftParPos) {
						errorText = errorText.substring(leftParPos+1,  rightParPos).trim();
					}
					trouble = trouble.replace("TargetError", errorText);
				}
			}
			catch (Exception ex)
			{
				// START KGU#569 2018-08-06: Issue #577 - Might be some trouble resulting from the output console...
				// (Sometimes there occurred asynchronous NullPointerExceptions from the OutputConsole textPane here)
				//trouble = ex.getLocalizedMessage();
				//if (trouble == null || trouble.length() < 5) trouble = ex.getMessage();
				//if (trouble == null || trouble.length() < 5) trouble = ex.toString();
				logger.log(Level.WARNING, "Unspecific error during execution of " + element.toString(), ex);
				if (trouble.isEmpty() && isOutput && !repeated && JOptionPane.showConfirmDialog(
						this.control, Control.msgGUISyncFault.getText().replace("%", cmd),
						control.msgTitleError.getText(),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					if (!outputDone) {
						// Try to repeat it once
						i--;
						repeated = true;
					}
					// Otherwise ignore it since execution was done.
				}
				else {
					trouble = ex.getLocalizedMessage();
					if (trouble == null || trouble.length() < 5) trouble = ex.getMessage();
					if (trouble == null || trouble.length() < 5) trouble = ex.toString();
				}
				// END KGU#569 2018-08-06
			}
			i++;
		} // while ((i < sl.count()) && trouble.equals("") ...)
		if (trouble.equals(""))
		{
			element.executed = false;
			// START KGU#477 2017-12-10: Enh. #487 - special treatment for declaration sequences
			if (this.lastDeclarationSurrogate != null && this.lastDeclarationSurrogate != element) {
				this.lastDeclarationSurrogate.executed = false;
				this.lastDeclarationSurrogate.addToExecTotalCount(element.getExecStepCount(false) - initialStepCount, false);
			}
			// END KGU#477 2017-12-10
		}
		return trouble;
	}
	
	// START KGU#2 2015-11-14: Separate dedicated implementation for "foreign calls"
	private String stepCall(Call element)
	{
		String trouble = new String();

		// START KGU#413 2017-06-09: Enh. #416 allow user-defined line concatenation
		//StringList sl = element.getText();
		StringList sl = element.getUnbrokenText();
		// END KGU#413 2017-06-09
		int i = 0;

		// START KGU#117 2016-03-10: Enh. #77
		boolean wasSimplyCovered = element.simplyCovered;
		boolean wasDeeplyCovered = element.deeplyCovered;
		boolean allSubroutinesCovered = true;
		// END KGU#117 2016-03-10

		// START KGU#77 2015-11-11: Leave if a return statement has been executed
		//while ((i < sl.count()) && trouble.equals("") && (stop == false))
		while ((i < sl.count()) && trouble.equals("") && (stop == false) && !context.returned)
		// END KGU#77 2015-11-11
		{
			String cmd = sl.get(i);
			// START KGU#809 2020-04-28: Issue #822 Sensible error messages on empty lines
			if (cmd.trim().isEmpty()) {
				trouble = Control.msgIllegalEmptyLine.getText();
				break;
			}
			// END KGU#809 2020-04-28
			// cmd=cmd.replace(":=", "<-");
			// START KGU#490 2018-02-08: Bugfix #503 - postpone string comparison conversion 
			//cmd = convert(cmd);
			cmd = convert(cmd, !Instruction.isAssignment(cmd));
			// END KGU#490 2018-02-08

			try
			{
				// START KGU#117 2016-03-08: Enh. #77
				element.deeplyCovered = false;
				// END KGU#117 2016-03-08

				// START KGU 2015-10-12: Allow to step within an instruction block (but no breakpoint here!) 
				if (i > 0)
				{
					delay();
				}
				// END KGU 2015-10-12

				// START KGU#417 2017-06-30: Enh. #424
				cmd = this.evaluateDiagramControllerFunctions(cmd);
				// END KGU#417 2017-06-30

				// assignment?
				// START KGU#377 2017-03-30: Bugfix
				//if (cmd.indexOf("<-") >= 0)
				if (Element.splitLexically(cmd, true).contains("<-"))
				// END KGU#377 2017-03-30: Bugfix
				{
					trouble = tryAssignment(cmd, element, i);
				}
				else
				{
					trouble = trySubroutine(cmd, element);
				}
				
				// START KGU#117 2016-03-08: Enh. #77
				allSubroutinesCovered = allSubroutinesCovered && element.deeplyCovered;
				// END KGU#117 2016-03-08
			} catch (EvalError ex)
			{
				trouble = ex.getLocalizedMessage();
				if (trouble == null) trouble = ex.getMessage();
			}

			i++;
			// Among the lines of a single instruction element there is no further breakpoint check!
		}
		if (trouble.equals(""))
		{
			element.executed = false;
			// START KGU#117 2016-03-08: Enh. #77
			element.simplyCovered = true;	// (Should already have been set)
			element.deeplyCovered = wasDeeplyCovered || allSubroutinesCovered;
			if (!wasDeeplyCovered && allSubroutinesCovered ||
					!wasSimplyCovered)
			{
				element.checkTestCoverage(true);
			}
			// END KGU#117 2016-03-08
		}
		return trouble;
	}
	// END KGU#2 2015-11-14

	// START KGU#78 2015-11-25: Separate dedicated implementation for JUMPs
	private String stepJump(Jump element)
	{
		String trouble = new String();

		// START KGU#413 2017-06-09: Enh. #416 allow user-defined line concatenation
		//StringList sl = element.getText();
		StringList sl = element.getUnbrokenText();
		// END KGU#413 2017-06-09
		boolean done = false;

		// START KGU#380 2017-04-14: #394 Radically rewritten and simplified (no multi-line evaluation anymore)
		// Leave?
		if (element.isLeave()) {
			int nLevels = element.getLevelsUp();
			if (nLevels < 1) {
				String argument = sl.get(0).trim().substring(CodeParser.getKeyword("preLeave").length()).trim();
				trouble = control.msgIllegalLeave.getText().replace("%1", argument);				
			}
			else {
				this.leave += nLevels;
				done = true;
			}
		}
		// Unstructured return?
		else if (element.isReturn()) {
			try {
				// START KGU#417 2017-06-30: Enh. #424
				//trouble = tryReturn(convert(sl.get(0)));
				String cmd = convert(sl.get(0));
				cmd = this.evaluateDiagramControllerFunctions(cmd);
				trouble = tryReturn(cmd);
				// END KGU#417 2017-06-30
				done = true;			
			}
			catch (Exception ex)
			{
				trouble = ex.getLocalizedMessage();
				if (trouble == null) trouble = ex.getMessage();
			}
		}
		// Exit from entire program?
		else if (element.isExit()) {
			StringList tokens = Element.splitLexically(sl.get(0).trim(), true);
			// START KGU#365/KGU#380 2017-04-14: Issues #380, #394 Allow arbitrary integer expressions now
			//tokens.removeAll("");
			tokens.remove(0);	// Get rid of the keyword...
			String expr = tokens.concatenate().trim();
			// END KGU#380 2017-04-14
			// Get exit value
			int exitValue = 0;
			if (!expr.isEmpty()) {	// KGU 2020-02-20 issue #   we tolerate omitted exit value (defaults to 0)
				try {
					// START KGU 2017-04-14: #394 Allow arbitrary integer expressions now
					//Object n = interpreter.eval(tokens.get(1));
					// START KGU#417 2017-06-30: Enh. #424
					expr = this.evaluateDiagramControllerFunctions(expr);
					// END KGU#417 2017-06-30
					Object n = this.evaluateExpression(expr, false, false);
					// END KGU 2017-04-14
					if (n instanceof Integer)
					{
						exitValue = ((Integer) n).intValue();
					}
					else
					{
						// START KGU#197 2016-07-27: More localization support
						//trouble = "Inappropriate exit value: <" + (n == null ? tokens.get(1) : n.toString()) + ">";
						trouble = control.msgWrongExit.getText().replace("%1",
								"<" + (n == null ? expr : n.toString()) + ">");
						// END KGU#197 2016-07-27
						// START KGU#686 2019-03-18: Enh. #56 must not be caught
						// END KGU#686 2019-03-18
					}
				}
				catch (EvalError ex)
				{
					// START KGU#197 2016-07-27: More localization support (Updated 32016-09-17)
					//trouble = "Wrong exit value: " + ex.getMessage();
					String exMessage = ex.getLocalizedMessage();
					if (exMessage == null) exMessage = ex.getMessage();
					trouble = control.msgWrongExit.getText().replace("%1", exMessage);
					// END KGU#197 2016-07-27
				}
			}
			if (trouble.isEmpty())
			{
				// START KGU#197 2016-07-27: More localization support
				//trouble = "Program exited with code " + exitValue + "!";
				trouble = control.msgExitCode.getText().replace("%1",
						Integer.toString(exitValue));
				// END KGU#197 2016-07-27
				// START KGU#117 2016-03-07: Enh. #77
				element.checkTestCoverage(true);
				// END KGU#117 2016-03-07
				// START KGU#808 2020-02-20: Bugfix #820 this flag must be set lest a try block should catch it
				this.isExited = true;
				// END KGU#808 2020-02-20
			}
			done = true;
		}
		// START KGU#686 2019-03-18: Enh. #56 throw instructions introduced
		else if (element.isThrow()) {
			try {
				String expr = sl.get(0).trim().substring(CodeParser.getKeyword("preThrow").length()).trim();
				if (expr.isEmpty()) {
					trouble = RETHROW_MESSAGE;
				}
				else {
					Object argVal = null;
					try {
						String temp = this.evaluateDiagramControllerFunctions(convert(expr));
						argVal = this.evaluateExpression(temp, temp.contains("{"), false);
					}
					catch (Exception ex) {}
					if (argVal != null) {
						trouble = argVal.toString();
					}
					else {
						trouble = expr;
					}
				}
				if (console.logMeta()) {
					console.writeln("*** " + Control.msgThrown.getText().
							replace("%1", this.stackTrace.get(this.stackTrace.count()-1)).
							replace("%2", Integer.toString(this.stackTrace.count()-1)).
							replace("%3", trouble), Color.RED);
				}
			}
			catch (Exception ex)
			{
				if ((trouble = ex.getLocalizedMessage()) == null && (trouble = ex.getMessage()) == null || trouble.trim().isEmpty()) {
					trouble = ex.toString();
				}
			}
		}
		// END KGU#686 2019-03-18
		// Anything else is an error
		else
		{
			// START KGU#197 2016-07-27: More localization support
			//trouble = "Illegal content of a Jump (i.e. exit) instruction: <" + cmd + ">!";
			trouble = control.msgIllegalJump.getText().replace("%1", sl.concatenate(" <nl> "));
			// END KGU#197 2016-07-27
		}
		// END KGU#380 2017-04-14
			
		if (done && leave > context.loopDepth)
		{
			// START KGU#197 2016-07-27: More localization support
			trouble = "Too many levels to leave (actual depth: " + context.loopDepth + " / specified: " + leave + ")!";
			trouble = control.msgTooManyLevels.getText().
					replace("%1", Integer.toString(context.loopDepth)).
					replace("%2", Integer.toString(leave));
			// END KGU#197 2016-07-27
		}			
		if (trouble.equals(""))
		{
			// START KGU#156 2016-03-11: Enh. #124
			element.addToExecTotalCount(1, true);	// For the jump
			//END KGU#156 2016-03-11
			element.executed = false;
		}
		return trouble;
	}
	// END KGU#78 2015-11-25
	
	// START KGU#417 2017-06-29: Enh. #424 New mechanism to pre-evaluate Turtleizer functions
	private String evaluateDiagramControllerFunctions(String expression) throws EvalError
	{
		if (diagramControllers != null) {
			// Now, several ones of the functions offered by diagramController might
			// occur at different nesting depths in the expression. So we must find
			// and evaluate them from innermost to outermost.
			// We advance from right to left, this way we will evaluate deeper nested
			// functions first.
			// Begin with collecting all possible occurrence positions
			StringList tokens = Element.splitLexically(expression, true);
			LinkedList<Integer> positions = new LinkedList<Integer>();
			int pos = tokens.count();
			while ((pos = tokens.lastIndexOf("(", pos-1)) >= 0) {
				String token = null;
				while (pos > 0 && (token = tokens.get(--pos).trim()).isEmpty());
				if (pos >= 0 && token != null && this.controllerFunctionNames.contains(token.toLowerCase())) {
					// START KGU#592 2018-10-02: Bugfix #617 The evaluation is to be done in reverse order as well
					//positions.addFirst(pos);
					positions.addLast(pos);
					// END KGU#591 2018-10-02
				}
			}
			Iterator<Integer> iter = positions.iterator();
			try {
				while (iter.hasNext()) {
					pos = iter.next();
					String fName = tokens.get(pos).toLowerCase();
					StringList exprTail = tokens.subSequence(tokens.indexOf("(", pos+1)+1, tokens.count());
					StringList args = Element.splitExpressionList(exprTail, ",", false);
					int nArgs = args.count();
					String fSign = fName + "#" + nArgs;
					DiagramController controller = this.controllerFunctions.get(fSign);
					// START KGU#592 2018-10-04 - Bugfix #617 If the signature doesn't match exactly then skip
					if (controller != null) {
					// END KGU#592 2018-10-04
						//Method function = controller.getFunctionMap().get(fSign);
						// Now we must know what is beyond the function call (the tail)
						String tail = "";
						StringList parts = Element.splitExpressionList(exprTail, ",", true);
						if (parts.count() > nArgs) {
							tail = parts.get(parts.count()-1).trim();
						}
						Object argVals[] = new Object[nArgs];
						for (int i = 0; i < nArgs; i++) {
							// While the known controller functions haven't got (complex) arguments we may neglect initializers
							argVals[i] = this.evaluateExpression(args.get(i), false, false);
						}
						// Passed till here, we try to execute the function - this may throw a FunctionException
						Object result = controller.execute(fName, argVals);
						tokens.remove(pos, tokens.count());
						//tokens.add(controller.castArgument(result, function.getReturnType()).toString());
						// START KGU#898 2020-12-25: Bugfix #898 - we must put the results in parentheses
						//tokens.add(result.toString());
						tokens.add("(");
						tokens.add(result.toString());
						tokens.add(")");
						// END KGU#898 2020-12-25
						if (!tail.isEmpty()) {
							tokens.add(Element.splitLexically(tail.substring(1), true));
						}
					// START KGU#592 2018-10-04 - Bugfix #617 (continued)
					}
					// END KGU#592 2018-10-04
				}
			}
			catch (EvalError ex) {
				throw ex;
			}
			catch (Exception ex) {
				// Convert other errors into EvalError
				EvalError err = new EvalError(ex.toString(), null, null);
				err.setStackTrace(ex.getStackTrace());
				throw err;
			}

			expression = tokens.concatenate();
		}
		return expression;
	}
	// END KGU#417 2017-06-29
	
	// START KGU 2015-11-11: Equivalent decomposition of method stepInstruction
	/**
	 * Submethod of {@link #stepInstruction(Instruction)}, handling an assignment.
	 * Also updates the dynamic type map. 
	 * @param cmd - the (assignment) instruction line, may also contain declarative parts
	 * @param instr - the Instruction element
	 * @param lineNo - the line number of the current assignment (for the type resgistration)
	 * @return a possible error message (for errors not thrown as EvalError)
	 * @throws EvalError
	 */
	private String tryAssignment(String cmd, Instruction instr, int lineNo) throws EvalError
	{
		String trouble = "";
		Object value = null;
		// KGU#2: In case of a Call element, we allow an assignment with just the subroutine call on the
		// right-hand side. This makes it relatively easy to detect and prepare the very subroutine call,
		// in contrast to possible occurrences of such foreign function calls at arbitrary expression depths,
		// combined, nested etc.
		// START KGU#375 2017-03-30: Enh. #388 - be constant-aware (clone constant arrays in the expression)
//		String varName = cmd.substring(0, cmd.indexOf("<-")).trim();
//		String expression = cmd.substring(
//				cmd.indexOf("<-") + 2, cmd.length()).trim();
		StringList tokens = Element.splitLexically(cmd, true);
		int posAsgnOpr = tokens.indexOf("<-");
		String leftSide = tokens.subSequence(0, posAsgnOpr).concatenate().trim();
		tokens.remove(0, posAsgnOpr+1);
		// START KGU#490 2018-02-08: Bugfix #503 - we must apply string comparison conversion after decomposition#
		// FIXME: this repeated tokenization is pretty ugly - we need a syntax tree...
		// DEBUG
		//StringBuilder problems = new StringBuilder();
		//ExprParser.getInstance().parse(tokens.concatenate(), problems);
		tokens = Element.splitLexically(this.convertStringComparison(tokens.concatenate().trim()), true);
		// END KGU#490 2018-02-08
		// START KGU#388 2017-09-13: Enh. #423 support records
		tokens.removeAll(" ");
		// END KGU#388 2017-09-13
		// Watch out for constant arrays or records
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			Object constVal = context.constants.get(token);
			if (constVal instanceof ArrayList<?>) {
				// Let a constant array be replaced by its clone, so we avoid structure
				// sharing, which would break the assurance of constancy.
				tokens.set(i, "copyArray(" + token + ")");
			}
			// START KGU#388 2017-09-13: Enh. #423 support records, too
			else if (constVal instanceof HashMap<?, ?>) {
				// Let a constant record be replaced by its clone, so we avoid structure
				// sharing, which would break the assurance of constancy.
				tokens.set(i, "copyRecord(" + token + ")");
			}
			// END KGU#388 2017-09-13
		}
		// START KGU#810 2020-02-20 Bugfix #823 - necessary gaps could vanish here
		//String expression = tokens.concatenate().trim();
		String expression = tokens.concatenate(null).trim();
		// END KGU#81ß0 2020-02-20
		// END KGU#375 2017-03-30
		if (instr instanceof Call)
		{
			Function f = new Function(expression);
			if (f.isFunction())
			{
				//System.out.println("Looking for SUBROUTINE NSD:");
				//System.out.println("--> " + f.getName() + " (" + f.paramCount() + " parameters)");
				// START KGU#317 2016-12-29
				//Root sub = this.findSubroutineWithSignature(f.getName(), f.paramCount());
				Root sub = null;
				try {
					sub = this.findSubroutineWithSignature(f.getName(), f.paramCount());
				} catch (Exception ex) {
					// Ambiguous call!
					if ((trouble = ex.getMessage()) == null) {
						trouble = ex.toString();
					}
					return trouble;
				}
				// END KGU#317 2016-12-29
				if (sub != null)
				{
					Object[] args = new Object[f.paramCount()];
					for (int p = 0; p < f.paramCount(); p++)
					{
						// START KGU#615 2018-12-16: Bugfix #644 - initializers as arguments caused errors
						//args[p] = this.evaluateExpression(f.getParam(p), false, false);
						args[p] = this.evaluateExpression(f.getParam(p), true, false);
						// END KGU#615 2018-12-16
					}
					value = executeCall(sub, args, (Call)instr);
					// START KGU#117 2016-03-10: Enh. #77
					// START KGU#686 2019-03-17: Enh. #56 might have caused a caught trouble
					//if (Element.E_COLLECTRUNTIMEDATA)
					if (subroutineTrouble != null) {
						trouble = subroutineTrouble;
						subroutineTrouble = null;
					}
					else if (Element.E_COLLECTRUNTIMEDATA)
					// END KGU#686 2019-03-17
					{
						instr.simplyCovered = true;
					}
					// END KGU#117 2016-03-10
				}
				else
				{
					// START KGU#197 2016-07-27: Now translatable
					//trouble = "A function diagram " + f.getName() + " (" + f.paramCount() + 
					//		" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";
					trouble = control.msgNoSubroutine.getText().
							replace("%1", f.getName()).
							replace("%2", Integer.toString(f.paramCount())).
							replace("\\n", "\n");
					// END KGU#197 2016-07-27
				}
			}
			else
			{
				// START KGU#197 2016-07-27: Now translatable
				//trouble = "<" + expression + "> is not a correct function!";
				trouble = control.msgIllFunction.getText().replace("%1", expression);
				// END KGU#197 2016-07-27
			}
		}
		// END KGU#2 2015-10-17
		// Now evaluate the expression
		// START KGU#426 2017-09-30: Enh. #48, #423, bugfix #429 we need this code e.g in tryReturn, too
		else
		{
			value = this.evaluateExpression(expression, true, false);
		}
		// END KGU#426 2017-09-30
		
		if (value != null)
		{
			// Assign the value and handle provided declaration
			setVar(leftSide, value);
			// START KGU#388 2017-09-14. Enh. #423
			// FIXME: This is poorly done, particularly we must handle cases of record assignment 
			//instr.updateTypeMapFromLine(context.dynTypeMap, cmd, lineNo);
			if (!leftSide.contains(".") && !leftSide.contains("[")) {
				TypeMapEntry oldEntry = null;
				String target = Instruction.getAssignedVarname(Element.splitLexically(leftSide, true), false) + "";
				if (!context.dynTypeMap.containsKey(target) || !(oldEntry = context.dynTypeMap.get(target)).isDeclared) {
					String typeDescr = Instruction.identifyExprType(context.dynTypeMap, expression, true);
					if (oldEntry == null) {
						TypeMapEntry typeEntry = null;
						if (typeDescr != null && (typeEntry = context.dynTypeMap.get(":" + typeDescr)) == null) {
							typeEntry = new TypeMapEntry(typeDescr, null, null, instr, lineNo, true, false);
						}
						context.dynTypeMap.put(target, typeEntry);
					}
					else {
						oldEntry.addDeclaration(typeDescr, instr, lineNo, true);
					}
				}
			}
			// END KGU#388 2017-09-14
		}
		// START KGU#2 2015-11-24: In case of an already detected problem don't fill the trouble
		//else if (trouble.isEmpty())
		else if (trouble.isEmpty() && !stop)
		// END KGU#2 2015-11-24
		{
			// START KGU#197 2016-07-27: Localization support
			//trouble = "<"
			//		+ expression
			//		+ "> is not a correct or existing expression.";
			trouble = control.msgInvalidExpr.getText().replace("%1", expression);
			// END KGU#197 2016-07-27
		}

		return trouble;
		
	}
	
	/**
	 * Submethod of {@link #stepInstruction(Instruction)}, handling an input instruction
	 * @param cmd - the instruction line expressing an input operation
	 * @return a possible error string
	 * @throws EvalError
	 */
	private String tryInput(String cmd) throws EvalError
	{
		String trouble = "";
		// START KGU#356 2019-03-02: Issue #366
		DiagramController focusedController = null;
		for (DiagramController dc: this.diagramControllers) {
			if (dc.isFocused()) {
				focusedController = dc;
				break;	// There can hardly be more than one focused controller
			}
		}
		// END KGU#356 2019-03-02
		// START KGU#356 2019-03-14: Enh. #366
		JFrame focusedFrame = null;
		if (focusedController == null) {
			if (diagram.getFrame().isFocused()) {
				focusedFrame = diagram.getFrame();
			}
			else if (control.isFocused()) {
				focusedFrame = control;
			}
			else if (Arranger.hasInstance() && Arranger.getInstance().isFocused()) {
				focusedFrame = Arranger.getInstance();
			}
		}
		// END KGU#356 2019-03-14
		// START KGU#653 2019-02-14: Enh. #680 - revision
		StringList inputItems = Instruction.getInputItems(cmd);
		String prompt = inputItems.get(0);
		if (!prompt.isEmpty()) {
			// Wipe off delimiters
			prompt = prompt.substring(1, prompt.length()-1);
			// START KGU#285 2016-10-16: Bugfix #276 - We should interpret contained escape sequences...
			try {
				String dummyVar = "prompt" + this.hashCode();
				this.evaluateExpression(dummyVar + "=\"" + prompt + "\"", false, false);
				Object res = context.interpreter.get(dummyVar);
				if (res != null) {
					prompt = res.toString();
				}
				context.interpreter.unset(dummyVar);
			}
			catch (EvalError ex) {}
			// END KGU#285 2016-10-16
		}
		// Empty input instruction?
		if (inputItems.count() == 1)
		// END KGU#653 219-02-14
		{
			// In run mode, give the user a chance to intervene
			Object[] options = {
					Control.lbOk.getText(),
					Control.lbPause.getText()
			};
			if (prompt.isEmpty()) {
				prompt = control.lbAcknowledge.getText();
			}
			// START KGU#160 2019-02-17: Enh. #51, #137 - an explicit prompt should be passed to text window
			else {
				this.console.writeln(prompt, Color.YELLOW);
			}
			// END KGU#160 2019-02-17
			int pressed = JOptionPane.showOptionDialog(diagram.getParent(), prompt, control.lbInput.getText(),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
			if (pressed == 1)
			{
				synchronized(this)
				{
					paus = true;
					step = true;
				}
				// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
				//control.setButtonsForPause();
				control.setButtonsForPause(false);	// This avoids interference with the pause button
				// END KGU#379 2017-04-12
			}
		}
		else
		{
		// END KGU#107 2015-12-13
			inputItems.remove(0);
			for (int i = 0; i < inputItems.count() && trouble.equals("") && (stop == false); i++) {
				String var = inputItems.get(i).trim();
				// START KGU#141 2016-01-16: Bugfix #112 - setVar won't eliminate enclosing parantheses anymore
				while (var.startsWith("(") && var.endsWith(")"))
				{
					var = var.substring(1, var.length()-1).trim();
				}
				// END KGU#141 2016-01-16
				// START KGU#33 2014-12-05: We ought to show the index value
				// if the variable is indeed an array element
				if (var.contains("[") && var.contains("]")) {
					try {
						// Try to replace the index expression by its current value
						int index = getIndexValue(var);
						var = var.substring(0, var.indexOf('[')+1) + index
								+ var.substring(var.indexOf(']'));
					}
					catch (Exception e)
					{
						// START KGU#141 2016-01-16: We MUST raise the error here.
						trouble = e.getLocalizedMessage();
						if (trouble == null) trouble = e.getMessage();
						// END KGU#141 2016-01-16
					}
				}
				// END KGU#33 2014-12-05
				// START KGU#375 2017-03-30: Enh. #388 - support of constants
				if (this.isConstant(var)) {
					trouble = control.msgConstantRedefinition.getText().replaceAll("%", var);
				}
				// END KGU#375 2017-03-30
				// START KGU#141 2016-01-16: Bugfix #112 - nothing more to do than exiting
				if (!trouble.isEmpty())
				{
					return trouble;
				}
				// END KGU#141 2016-01-16
				inputItems.set(i, var);
			}
			// START KGU#89 2016-03-18: More language support 
			//String str = JOptionPane.showInputDialog(null,
			//		"Please enter a value for <" + in + ">", null);
			// START KGU#281 2016-10-12: Enh. #271
			//String msg = control.lbInputValue.getText();
			//msg = msg.replace("%", in);
			if (prompt.isEmpty()) {
				prompt = control.lbInputValue.getText();				
				prompt = prompt.replace("%", inputItems.concatenate(", "));
			}
			// END KGU#281 2016-10-12
			// START KGU#160 2016-04-12: Enh. #137 - text window output
			this.console.write(prompt + (prompt.trim().endsWith(":") ? " " : ": "), Color.YELLOW);
			if (isConsoleEnabled)
			{
				this.console.setVisible(true);
			}
			// END KGU#160 2016-04-12
			//String str = JOptionPane.showInputDialog(diagram.getParent(), prompt, null);
			// END KGU#89 2016-03-18
			String[] values = new String[inputItems.count()];
			boolean goOn = true;
			if (values.length == 1) {
				values[0] = JOptionPane.showInputDialog(diagram.getParent(), prompt, null);
				goOn = values[0] != null;
			}
			else {
				goOn = showMultipleInputDialog(diagram.getParent(), prompt, inputItems, values);
			}

			// START KGU#84 2015-11-23: ER #36 - Allow a controlled continuation on cancelled input
			//setVarRaw(in, str);
			
			if (!goOn)
			{
				// Switch to step mode such that the user may enter the variable in the display and go on
				// START KGU#197 2016-05-05: Issue #89
				//JOptionPane.showMessageDialog(diagram, "Execution paused - you may enter the value in the variable display.",
				//		"Input cancelled", JOptionPane.WARNING_MESSAGE);
				JOptionPane.showMessageDialog(control, control.lbInputPaused.getText(),
						control.lbInputCancelled.getText(), JOptionPane.WARNING_MESSAGE);
				// START KGU#197 2016-05-05
				synchronized(this)
				{
					paus = true;
					step = true;
				}
				// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
				//control.setButtonsForPause();
				control.setButtonsForPause(false);	// This avoids interference with the pause button
				// END KGU#379 2017-04-12
				for (int i = 0; i < inputItems.count(); i++) {
					String var = inputItems.get(i);
					if (!context.variables.contains(var))
					{
						// If the variable hasn't been used before, we must create it now
						setVar(var, null);
					}
				}
			}
			else // goOn
			{
				// START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
				this.console.writeln((new StringList(values)).concatenate(", "), Color.GREEN);
				if (isConsoleEnabled)
				{
					this.console.setVisible(true);
				}
				// END KGU#160 2016-04-12
				// START KGU#69 2015-11-08: Use specific method for raw input
				for (int i = 0; i < inputItems.count(); i++) {
					setVarRaw(inputItems.get(i), values[i]);
				}
				// END KGU#69 2015-11-08
				// START KGU#356 2019-03-02: Issue #366
				if (focusedController != null) {
					focusedController.requestFocus();
				}
				// END KGU#356 2019-03-02
				// START KGU#356 2019-03-14: Enh. #366
				else if (focusedFrame != null) {
					focusedFrame.requestFocus();
				}
				// END KGU#356 2019-03-14
			}
			// END KGU#84 2015-11-23
			// START KGU#107 2015-12-13: Enh./bug #51 part 2
		}
		// END KGU#107 2015-12-13

		return trouble;

	}
	
	// START KGU#653 2019-02-13: Enh. #653 - Comfortable handling of multi-variable input
	/**
	 * Opens a dialog with input fields for all input items given as {@code targets} and gathers the
	 * values in array {@code values}.
	 * @param parent - the {@link Container} this modal dialog is placed relatively to
	 * @param prompt - the common input prompt string 
	 * @param targets - the descriptions of the variables to be filled
	 * @param values - array for the raw input strings per variable 
	 * @return true if the results were committed, false otherwise (meaning to pause execution)
	 */
	private boolean showMultipleInputDialog(Container parent, String prompt, StringList targets, String[] values) {
		javax.swing.JPanel pnl = new javax.swing.JPanel();
		pnl.setLayout(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = java.awt.GridBagConstraints.LINE_START;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new java.awt.Insets(1, 2, 2, 1);
		javax.swing.JLabel lblPrompt = new javax.swing.JLabel(prompt);
		pnl.add(lblPrompt, gbc);
		javax.swing.JTextField[] fields = new javax.swing.JTextField[values.length];
		
		gbc.gridwidth = 1;
		for (int row = 0; row < targets.count(); row++) {
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.weightx = 0;
			gbc.fill = java.awt.GridBagConstraints.NONE;
			pnl.add(new javax.swing.JLabel(targets.get(row)), gbc);
			gbc.gridx++;
			gbc.weightx = 1;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			fields[row] = new javax.swing.JTextField();
			pnl.add(fields[row], gbc);
		}
		
		boolean committed = JOptionPane.showConfirmDialog(diagram.getParent(), pnl,
				CodeParser.getKeywordOrDefault("input", "INPUT"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
		if (committed) {
			for (int row = 0; row < targets.count(); row++) {
				values[row] = fields[row].getText();
			}
		}
		
		return committed;
	}
	// END KGU#653 2019-02-14

	/**
	 * Submethod of {@link #stepInstruction(Instruction)}, handling an output instruction
	 * @param cmd - the output instruction line
	 * @return a possible error string
	 * @throws EvalError
	 */
	private String tryOutput(String cmd) throws EvalError
	{
		String trouble = "";
		// KGU 2015-12-11: Instruction is supposed to start with the output keyword!
		String out = cmd.substring(/*cmd.indexOf(CodeParser.output) +*/
						CodeParser.getKeyword("output").trim().length()).trim();
		// START KGU#490 2018-02-07: Bugfix #503 
		out = this.evaluateDiagramControllerFunctions(convert(out).trim());
		// END KGU#490 2018-02-07
		String str = "";
		// START KGU#107 2015-12-13: Enh-/bug #51: Handle empty output instruction
		if (!out.isEmpty())
		{
		// END KGU#107 2015-12-13
		// START KGU#101 2015-12-11: Fix #54 - Allow several expressions to be output in a line
			StringList outExpressions = Element.splitExpressionList(out, ",");
			for (int i = 0; i < outExpressions.count() && trouble.isEmpty(); i++)
			{
				out = outExpressions.get(i);
		// END KGU#101 2015-12-11
				Object n = this.evaluateExpression(out, false, false);
				if (n == null)
				{
					trouble = control.msgInvalidExpr.getText().replace("%1", out);
				} else
				{
		// START KGU#101 2015-12-11: Fix #54 (continued)
					//	String s = unconvert(n.toString());
					str += n.toString();
				}
			}
		// START KGU#107 2015-12-13: Enh-/bug #51: Handle empty output instruction
		}
//		else {
//			str = "(empty line)";
//		}
		// END KGU#107 2015-12-13
		if (trouble.isEmpty())
		{
			// START KGU#616 2018-12-17: Bugfix #646 Undue trimming and obsolete "unconverting"
			//String s = unconvert(str.trim());	// FIXME (KGU): What the heck is this good for?
			String s = str;
			// END KGU#616 2018-12-17
		// END KGU#101 2015-12-11
			// START KGU#84 2015-11-23: Enhancement #36 to give a chance to pause
			//JOptionPane.showMessageDialog(diagram, s, "Output",
			//		0);
			//System.out.println("running/step/paus/stop: " +
			//		running + " / " + step + " / " + paus + " / " + " / " + stop);

			// START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
			//if (step)
			this.console.writeln(s);
			// START KGU#107 2016-05-05: For the message dialog we must show something
			if (s.isEmpty())
			{
				s = "(" + control.lbEmptyLine.getText() + ")";
			}
			// END KGU#107 2016-05-05
			if (isConsoleEnabled)
			{
				this.console.setVisible(true);
			}
			else if (step)
			// END KGU#160 2016-04-12
			{
				// In step mode, there is no use to offer pausing
				// diagram is a bad anchor component since its extension is the Root rectangle (may be huge!)
				JOptionPane.showMessageDialog(diagram.getParent(), s, control.lbOutput.getText(),
						JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				// In run mode, give the user a chance to intervene
				Object[] options = {
						Control.lbOk.getText(),
						Control.lbPause.getText()
				};
				// diagram is a bad anchor component since its extension is the Root rectangle (may be huge!)
				int pressed = JOptionPane.showOptionDialog(diagram.getParent(), s, control.lbOutput.getText(),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
				if (pressed == 1)
				{
					synchronized(this)
					{
						paus = true;
						step = true;
					}
					// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
					//control.setButtonsForPause();
					control.setButtonsForPause(false);	// This avoids interference with the pause button
					// END KGU#379 2017-04-12
				}
			}
			// END KGU#84 2015-11-23
		}
		return trouble;
	}

	// Submethod of stepInstruction(Instruction element), handling a return instruction
	private String tryReturn(String cmd) throws EvalError
	{
		String trouble = "";
		String header = control.lbReturnedResult.getText();
		String out = cmd.substring(CodeParser.getKeywordOrDefault("preReturn", "return").length()).trim();
		// START KGU#77 (#21) 2015-11-13: We ought to allow an empty return
		//Object n = interpreter.eval(out);
		//if (n == null)
		//{
		//	trouble = "<"
		//			+ out
		//			+ "> is not a correct or existing expression.";
		//} else
		//{
		//	String s = unconvert(n.toString());
		//	JOptionPane.showMessageDialog(diagram, s,
		//			"Returned trouble", 0);
		//}
		// START KGU#490 2018-02-07: Bugfix #503 
		out = this.evaluateDiagramControllerFunctions(convert(out).trim());
		// END KGU#490 2018-02-07
		Object resObj = null;
		if (!out.isEmpty())
		{
			// START KGU#426 2017-09-30: Bugfix #429
			//resObj = this.evaluateExpression(out);
			resObj = this.evaluateExpression(out, true, false);
			// END KGU#426 2017-09-30
			// If this diagram is executed at top level then show the return value
			if (this.callers.empty())
			{
				if (resObj == null)	{
					trouble = control.msgInvalidExpr.getText().replace("%1", out);
				} 
				// START KGU#133 2016-01-29: Arrays should be presented as scrollable list
				// START KGU#439 2017-10-13: Issue 436 - Structorizer arrays now implemented as ArrayLists rather than Object[] 
				//else if (resObj instanceof Object[]) {
				//	showArray((Object[])resObj, header, !step);
				//}
				else if (resObj instanceof ArrayList<?> || resObj instanceof HashMap<?,?>) {
					showCompoundValue(resObj, header, !step);
				}
				// END KGU#439 2017-10-13
				else if (step) {
					// START KGU#160 2016-04-26: Issue #137 - also log the result to the console
					if (this.console.logMeta()) {
						this.console.writeln("*** " + header + ": " + prepareValueForDisplay(resObj, context.dynTypeMap), Color.CYAN);
					}
					// END KGU#160 2016-04-26
					// START KGU#147 2016-01-29: This "unconverting" copied from tryOutput() didn't make sense...
					//String s = unconvert(resObj.toString());
					//JOptionPane.showMessageDialog(diagram, s,
					//		"Returned result", JOptionPane.INFORMATION_MESSAGE);
					JOptionPane.showMessageDialog(diagram.getParent(), resObj,
							header, JOptionPane.INFORMATION_MESSAGE);
					// END KGU#147 2016-01-29					
				// END KGU#133 2016-01-29
				}
				else {
					// START KGU#198 2016-05-25: Issue #137 - also log the result to the console
					if (this.console.logMeta()) {
						this.console.writeln("*** " + header + ": " + prepareValueForDisplay(resObj, context.dynTypeMap), Color.CYAN);
					}
					// END KGU#198 2016-05-25
					// START KGU#84 2015-11-23: Enhancement to give a chance to pause (though of little use here)
					Object[] options = {
							Control.lbOk.getText(),
							Control.lbPause.getText()
					};
					int pressed = JOptionPane.showOptionDialog(diagram.getParent(), resObj, header,
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
					if (pressed == 1)
					{
						synchronized(this) {
							paus = true;
							step = true;
						}
						// START KGU#379 2017-04-12: Bugfix #391 moved to waitForNext()
						//control.setButtonsForPause();
						control.setButtonsForPause(false);	// This avoids interference with the pause button
						// END KGU#379 2017-04-12
					}
					// END KGU#84 2015-11-23
				}
			}
		}
		
		context.returnedValue = resObj;
		// END KGU#77 (#21) 2015-11-13
		context.returned = true;
		return trouble;
	}

	// Submethod of stepInstruction(Instruction element), handling a function call
	private String trySubroutine(String cmd, Instruction element) throws EvalError
	{
		String trouble = "";
		Function f = new Function(cmd);
		if (f.isFunction())
		{
			String procName = f.getName();
			//String params = new String();	// List of evaluated arguments
			Object[] args = new Object[f.paramCount()];
			for (int p = 0; p < f.paramCount(); p++)
			{
				try
				{
					// START KGU#615 2018-12-16: Bugfix #644 - Allow initializers as subroutine arguments
					//args[p] = this.evaluateExpression(f.getParam(p), false, false);
					args[p] = this.evaluateExpression(f.getParam(p), true, false);
					// END KGU#615 2018-12-16
					if (args[p] == null)
					{
						if (!trouble.isEmpty())
						{
							trouble = trouble + "\n";
						}
						trouble += "PARAM " + (p+1) + ": "
								+ control.msgInvalidExpr.getText().replace("%1", f.getParam(p));
					}
//					else
//					{
//						params += "," + args[p].toString();
//					}
				} catch (EvalError ex)
				{
					String exMessage = ex.getLocalizedMessage();
					if (exMessage == null) exMessage = ex.getMessage();
					trouble += (!trouble.isEmpty() ? "\n" : "") +
							"PARAM " + (p+1) + ": " + exMessage;
				}
			}
			// If this element is of class Call and the extracted function name
			// corresponds to one of the NSD diagrams currently opened then try
			// a sub-execution of that diagram.
			// START KGU#2 2015-10-17: Check foreign call
			if (trouble.isEmpty() && element instanceof Call)
			{
				// FIXME: Disable the output instructions for the release version
				//System.out.println("Looking for SUBROUTINE NSD:");
				//System.out.println("--> " + f.getName() + " (" + f.paramCount() + " parameters)");
				// START KGU#317 2016-12-29: Abort execution on ambiguous calls
				//Root sub = this.findSubroutineWithSignature(f.getName(), f.paramCount());
				Root sub = null;
				try {
					sub = this.findSubroutineWithSignature(procName, f.paramCount());
				} catch (Exception ex) {
					return ex.getMessage();	// Ambiguous call!
				}
				// END KGU#317 2016-12-29
				if (sub != null)
				{
					executeCall(sub, args, (Call)element);
					// START KGU#117 2016-03-10: Enh. #77
					// START KGU#686 2019-03-17: Enh. #56 might have caused a caught trouble
					//if (Element.E_COLLECTRUNTIMEDATA)
					if (subroutineTrouble != null) {
						trouble = subroutineTrouble;
						subroutineTrouble = null;
					}
					else if (Element.E_COLLECTRUNTIMEDATA)
					// END KGU#686 2019-03-17
					{
						element.simplyCovered = true;
					}
					// END KGU#117 2016-03-10
				}
				else
				{
					// START KGU#197 2016-07-27: Now translatable message
					//trouble = "A subroutine diagram " + f.getName() + " (" + f.paramCount() + 
					//		" parameters) could not be found!\nConsider starting the Arranger and place needed subroutine diagrams there first.";
					trouble = control.msgNoSubroutine.getText().
							replace("%1", procName).
							replace("%2", Integer.toString(f.paramCount())).
							replace("\\n", "\n");
					// END KGU#197 2016-07-27
				}
			}
			// END KGU#2 2015-10-17
			else if (trouble.isEmpty())
			{
				// START KGU#448 2017-10-28: Enh. #443
//				if (diagramController != null)
//				{
//					if (f.paramCount() > 0)
//					{
//						// Cut off the leading comma from the list of evaluated arguments
//						params = params.substring(1);
//					}
//					cmd = procName.toLowerCase() + "(" + params + ")";
//					trouble = getExec(cmd, element.getColor());
//				} 
				procName = procName.toLowerCase();
				String pSign = procName + "#" + args.length;
				DiagramController controller = this.controllerProcedures.get(pSign);
				if (controller != null) { 
					HashMap<String, Method> procMap = controller.getProcedureMap(); 
					// Check if the controller accepts a method with additional color argument, too
					Method colMethod = procMap.get(procName + "#" + (args.length + 1));
					if (colMethod != null && colMethod.getParameterTypes()[args.length] == Color.class) {
						Object[] argsColor = new Object[args.length+1];
						for (int i = 0; i < args.length; i++) {
							argsColor[i] = args[i];
						}
						argsColor[args.length] = element.getColor();
						args = argsColor;
					}
					trouble = getExec(controller, procName, args);
				}
				// END KGU#448 2017-20-28
				else	
				{
					// Try as built-in subroutine as is
					this.evaluateExpression(cmd, false, false);
				}
			}
		}
		else {
			// START KGU#197 2017-06-06: Now localizable
			//trouble = "<" + cmd + "> is not a correct function!";
			trouble = control.msgIllFunction.getText().replace("%1", cmd);
			// END KGU#197 2017-06-06
		}
		return trouble;
	}
	// END KGU 2015-11-11

	private String stepCase(Case element)
	{
		// START KGU 2016-09-25: Bugfix #254
		String[] parserKeys = new String[]{
				CodeParser.getKeyword("preCase"),
				CodeParser.getKeyword("postCase")
				};
		// END KGU 2016-09-25
		String trouble = new String();
		try
		{
			// START KGU#453 2017-11-02: Issue #447 - face line continuation
			//StringList text = element.getText();
			StringList text = element.getUnbrokenText();
			// START KGU#453 2017-11-02
			// START KGU#259 2016-09-25: Bugfix #254
			//String expression = text.get(0) + " = ";
			StringList tokens = Element.splitLexically(text.get(0), true);
			for (String key : parserKeys)
			{
				if (!key.trim().isEmpty())
				{
					tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
				}		
			}
			// START KGU#417 2017-06-30: Enh. #424
			//String expression = tokens.concatenate() + " = ";
			String expression = this.evaluateDiagramControllerFunctions(tokens.concatenate()) + " = ";
			// END KGU#417 2017-06-30
			// END KGU#259 2016-09-25
			boolean done = false;
			int last = text.count() - 1;
			boolean hasDefaultBranch = !text.get(last).trim().equals("%");
			if (!hasDefaultBranch)
			{
				last--;
			}
			// START KGU#156 2016-03-11: Enh. #124
			element.addToExecTotalCount(1, true);	// For the condition test (as if were just one comparison)
			//END KGU#156 2016-03-11
			
			for (int q = 1; (q <= last) && (done == false); q++)
			{
				// START KGU#15 2015-10-21: Support for multiple constants per branch
				//String test = convert(expression + text.get(q));
				// START KGU#755 2019-11-08: Bugfix #769 - string literals might contain commas
				//String[] constants = text.get(q).split(",");
				String[] constants = Element.splitExpressionList(text.get(q), ",").toArray();
				// END KGU#755 2019-11-08
				// END KGU#15 2015-10-21
				boolean go = false;
				if ((q == last) && hasDefaultBranch)
				{
					// default branch
					go = true;
				}
				if (go == false)
				{
					// START KGU#15 2015-10-21: Test against a list of constants now
					//Object n = interpreter.eval(test);
					//go = n.toString().equals("true");
					for (int c = 0; !go && c < constants.length; c++)
					{
						// START KGU#259 2016-09-25: Bugfix #254
						//String test = convert(expression + constants[c]);
						tokens = Element.splitLexically(constants[c], true);
						for (String key : parserKeys)
						{
							if (!key.trim().isEmpty())
							{
								tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
							}		
						}
						String test = convert(expression + tokens.concatenate());
						// END KGU#259 2016-09-25
						Object n = this.evaluateExpression(test, false, false);
						go = n.toString().equals("true");
					}
					// END KGU#15 2015-10-21
				}
				if (go)
				{
					done = true;
					element.waited = true;
					if (trouble.isEmpty())
					{
						trouble = stepSubqueue(element.qs.get(q - 1), false);
					}
					if (trouble.equals(""))
					{
						element.waited = false;
					}
				}
			}
			if (trouble.equals(""))
			{
				// START KGU#296 2016-11-25: Issue #294 - special coverage treatment for default-less CASE
				if (!done && !hasDefaultBranch) {
					// In run data tracking mode it is required that the suppressed default branch
					// has been passed at least once to achieve deep test coverage
					element.qs.get(last).deeplyCovered = true;
				}
				// END KGU#296 2016-11-25
				element.executed = false;
				element.waited = false;
			}
		} catch (EvalError ex)
		{
			trouble = ex.getLocalizedMessage();
			if (trouble == null) trouble = ex.getMessage();
		}
		
		return trouble;
	}
	
	private String stepAlternative(Alternative element)
	{
		String trouble = new String();
		try
		{
			// START KGU#453 2017-11-01: Bugfix #447 - get rid of possible line continuator backslashes
			//String s = element.getText().getText();
			String s = element.getUnbrokenText().getLongString();
			// END KGU#453 2017-11-01
			// START KGU#150 2016-04-03: More precise processing
//			if (!CodeParser.preAlt.equals(""))
//			{
//				// FIXME: might damage variable names
//				s = BString.replace(s, CodeParser.preAlt, "");
//			}
//			if (!CodeParser.postAlt.equals(""))
//			{
//				// FIXME: might damage variable names
//				s = BString.replace(s, CodeParser.postAlt, "");
//			}
//
//			s = convert(s);
			StringList tokens = Element.splitLexically(s, true);
			for (String key : new String[]{
					CodeParser.getKeyword("preAlt"),
					CodeParser.getKeyword("postAlt")})
			{
				if (!key.trim().isEmpty())
				{
					tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
				}		
			}
			s = convert(tokens.concatenate());
			// END KGU#150 2016-04-03

			// START KGU#417 2017-06-30: Enh. #424
			s = this.evaluateDiagramControllerFunctions(s);
			// END KGU#417 2017-06-30

			//System.out.println("C=  " + interpreter.get("C"));
			//System.out.println("IF: " + s);
			Object cond = this.evaluateExpression(s, false, false);
			//System.out.println("Res= " + n);
			if (cond == null || !(cond instanceof Boolean))
			{
				// START KGU#197 2016-07-27: Localization support
				//trouble = "<" + s
				//		+ "> is not a correct or existing expression.";
				trouble = control.msgInvalidBool.getText().replace("%1", s);
				// END KGU#197 2016-07-27
			}
			// if(getExec(s).equals("OK"))
			else 
			{
				// START KGU#156 2016-03-11: Enh. #124
				element.addToExecTotalCount(1, true);	// For the condition test
				//END KGU#156 2016-03-11
				
				Subqueue branch;
				if (cond.toString().equals("true"))
				{
					branch = element.qTrue;
				}
				else
				{
					branch = element.qFalse;
				}
				element.executed = false;
				element.waited = true;

				// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
//				int i = 0;
//				// START KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
//				//while ((i < branch.children.size())
//				//		&& trouble.equals("") && (stop == false))
//				while ((i < branch.getSize())
//						&& trouble.equals("") && (stop == false) && !returned && leave == 0)
//				// END KGU#78 2015-11-25
//				{
//					trouble = step(branch.getElement(i));
//					i++;
//				}
				if (trouble.isEmpty())
				{
					trouble = stepSubqueue(branch, true);
				}
				// END KGU#117 2016-03-07
				if (trouble.equals(""))
				{
					element.waited = false;
				}
			}
			if (trouble.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
		} catch (EvalError ex)
		{
			trouble = ex.getLocalizedMessage();
			if (trouble == null) trouble = ex.getMessage();
		}
		return trouble;
	}
	
	// This executes While and Forever loops
	private String stepWhile(Element element, boolean eternal)
	{
		String trouble = new String();
		try
		{
			String condStr = "true";	// Condition expression
			if (!eternal) {
				// START KGU#413 2017-06-09: Enh. #416: Cope with user-inserted line breaks
				//condStr = element.getText().getText();
				condStr = element.getUnbrokenText().getLongString();
				// END KGU#413 2017-06-09
				// START KGU#150 2016-04-03: More precise processing
//				if (!CodeParser.preWhile.equals(""))
//				{
//					// FIXME: might damage variable names
//					condStr = BString.replace(condStr, CodeParser.preWhile, "");
//				}
//				if (!CodeParser.postWhile.equals(""))
//				{
//					// FIXME: might damage variable names
//					condStr = BString.replace(condStr, CodeParser.postWhile, "");
//				}
//				// START KGU#79 2015-11-12: Forgotten to write back the trouble!
//				//convert(condStr, false);
//				condStr = convert(condStr, false);
//				// END KGU#79 2015-11-12
//				// System.out.println("WHILE: "+condStr);
				StringList tokens = Element.splitLexically(condStr, true);
				for (String key : new String[]{
						CodeParser.getKeyword("preWhile"),
						CodeParser.getKeyword("postWhile")})
				{
					if (!key.trim().isEmpty())
					{
						tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
					}		
				}
				// START KGU#433 2017-10-11: Bugfix #434 Don't try to be too clever here - variables might change type within the loop..
				//condStr = convert(tokens.concatenate());
				condStr = convert(tokens.concatenate(), false);
				// END KGU#433 2017-10-11
				// END KGU#150 2016-04-03
			}

			//int cw = 0;
			// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
			//Object cond = context.interpreter.eval(convertStringComparison(condStr));
			String tempCondStr = this.evaluateDiagramControllerFunctions(condStr);
			Object cond = this.evaluateExpression(convertStringComparison(tempCondStr), false, false);
			// END KGU#417 2017-06-30

			if (cond == null || !(cond instanceof Boolean))
			{
				// START KGU#197 2016-07-27: Localization support
				//trouble = "<" + condStr
				//		+ "> is not a correct or existing expression.";
				trouble = control.msgInvalidBool.getText().replace("%1", condStr);
				// END KGU#197 2016-07-27
			} else
			{
				// START KGU#156 2016-03-11: Enh. #124
				element.addToExecTotalCount(1, true);	// For the condition evaluation
				//END KGU#156 2016-03-11
				
				// START KGU#77/KGU#78 2015-11-25: Leave if any kind of Jump statement has been executed
				//while (cond.toString().equals("true") && trouble.equals("")
				//		&& (stop == false))
				context.loopDepth++;
				while (cond.toString().equals("true") && trouble.equals("")
						&& (stop == false) && !context.returned && leave == 0)
				// END KGU#77/KGU#78 2015-11-25
				{

					element.executed = false;
					element.waited = true;

					// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
					if (trouble.isEmpty())
					{
						trouble = stepSubqueue(((ILoop)element).getBody(), true);						
					}
					// END KGU#117 2016-03-07

					// START KGU#200 2016-06-07: Body is only done if there was no error
					//element.executed = true;
					//element.waited = false;
					// END KGU#200 2016-06-07
					if (trouble.equals(""))
					{
						// START KGU#200 2016-06-07: If body is done without error then show loop as active again
						element.executed = true;
						element.waited = false;
						// END KGU#200 2016-06-07
						//cw++;
						// START KGU 2015-10-13: Symbolizes the loop condition check 
						checkBreakpoint(element);
						delay();
						// END KGU 2015-10-13
					}
					// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
					//cond = context.interpreter.eval(convertStringComparison(condStr));
					tempCondStr = this.evaluateDiagramControllerFunctions(condStr);
					cond = this.evaluateExpression(convertStringComparison(tempCondStr), false, false);
					// END KGU#417 2017-06-30
					if (cond == null)
					{
						// START KGU#197 2016-07-27: Localization support
						//trouble = "<"
						//		+ condStr
						//		+ "> is not a correct or existing expression.";
						trouble = control.msgInvalidExpr.getText().replace("%1", condStr);
						// END KGU#197 2016-07-27
					}
					// START KGU#156 2016-03-11: Enh. #124
					else
					{
						element.addToExecTotalCount(1, true);	// For the condition evaluation
					}
					//END KGU#156 2016-03-11			
						
				}
				// START KGU#78 2015-11-25: If there are open leave requests then nibble one off
				if (leave > 0)
				{
					leave--;
				}
				context.loopDepth--;
				// END KGU#78 2015-11-25
			}
			if (trouble.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
			/*
			 * if (cw > 1000000) { element.selected = true; trouble =
			 * "Your loop ran a million times. I think there is a problem!";
			 * }
			 */
		} catch (EvalError ex)
		{
			trouble = ex.getMessage();
		}
		return trouble;
	}
	
	private String stepRepeat(Repeat element)
	{
		String trouble = new String();
		try
		{
			element.waited = true;
			if (delay != 0 || step)
			{
				diagram.redraw();
			}

			// The exit condition is converted and parsed once in advance!
			// Hence, syntactic errors will be reported before the loop has been started at all.
			// And, of course, variables only introduced within the loop won't be recognised--
			// which is sound with scope rules in C or Java.
			// START KGU#413 2017-06-09: Enh. #416: Cope with user-inserted line breaks
			//String condStr = element.getText().getText();
			String condStr = element.getUnbrokenText().getLongString();
			// END KGU#413 2017-06-09
			// START KGU#150 2016-04-03: More precise processing
//			if (!CodeParser.preRepeat.equals(""))
//			{
//				// FIXME: might damage variable names
//				condStr = BString.replace(condStr, CodeParser.preRepeat, "");
//			}
//			if (!CodeParser.postRepeat.equals(""))
//			{
//				// FIXME: might damage variable names
//				condStr = BString.replace(condStr, CodeParser.postRepeat, "");
//			}
//			condStr = convert(condStr, false);
			StringList tokens = Element.splitLexically(condStr, true);
			for (String key : new String[]{
					CodeParser.getKeyword("preRepeat"),
					CodeParser.getKeyword("postRepeat")})
			{
				if (!key.trim().isEmpty())
				{
					tokens.removeAll(Element.splitLexically(key, false), !CodeParser.ignoreCase);
				}		
			}
			// START KGU#433 2017-10-11: Bugfix #434 Don't try to be too clever here - variables might change type within the loop...
			//condStr = convert(tokens.concatenate());
			condStr = convert(tokens.concatenate(), false);
			// END KGU#433 2017-10-11
			// END KGU#150 2016-04-03

			//int cw = 0;
			// START KGU#487 2018-01-23: Bugfix #498 - preliminary condition test dropped altogether. 
			// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
			//Object cond = context.interpreter.eval(convertStringComparison(condStr));
			//String tempCondStr = this.evaluateDiagramControllerFunctions(condStr);
			//Object cond = this.evaluateExpression(convertStringComparison(tempCondStr), false, false);
			// END KGU#417 2017-06-30
			//if (cond == null)
			//{
			//	// START KGU#197 2016-07-27: Localization support
			//	//trouble = "<" + condStr
			//	//		+ "> is not a correct or existing expression.";
			//	trouble = control.msgInvalidExpr.getText().replace("%1", condStr);
			//	// END KGU#197 2016-07-27
			//} else
			Object cond = null;
			// END KGU#487 2018-01-23
			{
				// START KGU#78 2015-11-25: In order to handle exits we must know the nesting depth
				context.loopDepth++;
				// END KGU#78
				do
				{
					// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
					if (trouble.isEmpty())
					{
						trouble = stepSubqueue(element.getBody(), true);
					}
					// END KGU#117 2016-03-07

					if (trouble.equals(""))
					{
						//cw++;
						element.executed = true;
						// START KGU#515 2018-04-03: The following must not be done if the body failed (had erroneously resided after this if)
						// START KGU#665 2019-02-26: Bugfix #687  breakpoint check had been misplaced (was behind the cond evaluation)
						// delay this element
						checkBreakpoint(element);
						element.waited = false;
						delay();	// Symbolizes the loop condition check time
						// END KGU#665 2019-02-26
						// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
						String tempCondStr = this.evaluateDiagramControllerFunctions(condStr);
						cond = this.evaluateExpression(convertStringComparison(tempCondStr), false, false);
						// END KGU#417 2017-06-30
						if (cond == null || !(cond instanceof Boolean))
						{
							// START KGU#197 2016-07-27: Localization support
							trouble = control.msgInvalidBool.getText().replace("%1", condStr);
							// END KGU#197 2016-07-27
						}

						element.waited = true;

						// START KGU#156 2016-03-11: Enh. #124
						element.addToExecTotalCount(1, true);		// For the condition evaluation
						// END KGU#156 2016-03-11
						// END KGU#515 2018-04-03
					}
					
				// START KGU#70 2015-11-09: Condition logically incorrect - execution often got stuck here 
				//} while (!(n.toString().equals("true") && trouble.equals("") && (stop == false)));
				// START KGU#77/KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
				//} while (!(n.toString().equals("true")) && trouble.equals("") && (stop == false))
				} while (cond != null && !(cond.toString().equals("true")) && trouble.equals("") && (stop == false) &&
						!context.returned && leave == 0);
				// END KGU#77/KGU#78 2015-11-25
				// END KGU#70 2015-11-09
				// START KGU#78 2015-11-25: If there are open leave requests then nibble one off
				if (leave > 0)
				{
					leave--;
				}
				context.loopDepth--;
				// END KGU#78 2015-11-25
			}

			if (trouble.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
			/*
			 * if (cw > 100) { element.selected = true; trouble = "Problem!";
			 * }
			 */
		} catch (EvalError ex)
		{
			trouble = ex.getMessage();
		}
		return trouble;
	}
	
	private String stepFor(For element)
	{
		// START KGU#61 2016-03-21: Enh. #84
		if (element.isForInLoop())
		{
			return stepForIn(element);
		}
		// END KGU#61 2016-03-21
		String trouble = new String();
		// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
		int forLoopLevel = context.forLoopVars.count();
		// END KGU#307 2016-12-12
		try
		{
			int sval = element.getStepConst();
			String counter = element.getCounterVar();
			
			// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
			context.forLoopVars.add(counter);
			// END KGU#307 2016-12-12

			String s = element.getStartValue(); 

			s = convert(s);
			// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated
			s = this.evaluateDiagramControllerFunctions(s);
			// END KGU#417 2017-06-30
			Object n = this.evaluateExpression(s, false, false);
			if (n == null)
			{
				// START KGU#197 2016-07-27: Localization support
				//trouble = "<"+s+"> is not a correct or existing expression.";
				trouble = control.msgInvalidExpr.getText().replace("%1", s);
				// END KGU#197 2016-07-27
			}
			int ival = 0;
			if (n instanceof Integer)
			{
				ival = (Integer) n;
			}
			else if (n instanceof Long)
			{
				ival = ((Long) n).intValue();
			}
			else if (n instanceof Float)
			{
				ival = ((Float) n).intValue();
			}
			else if (n instanceof Double)
			{
				ival = ((Double) n).intValue();
			}

			s = element.getEndValue();
			s = convert(s);
			// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated
			s = this.evaluateDiagramControllerFunctions(s);
			// END KGU#417 2017-06-30
			n = this.evaluateExpression(s, false, false);
			if (n == null)
			{
				// START KGU#197 2016-07-27: Localization support
				//trouble = "<"+s+"> is not a correct or existing expression.";
				trouble = control.msgInvalidExpr.getText().replace("%1", s);
				// END KGU#197 2016-07-27
			}
			int fval = 0;
			if (n instanceof Integer)
			{
				fval = (Integer) n;
			}
			else if (n instanceof Long)
			{
				fval = ((Long) n).intValue();
			}
			else if (n instanceof Float)
			{
				fval = ((Float) n).intValue();
			}
			else if (n instanceof Double)
			{
				fval = ((Double) n).intValue();
			}

			// START KGU#156 2016-03-11: Enh. #124
			element.addToExecTotalCount(1, true);	// For the initialisation and first test
			//END KGU#156 2016-03-11
			
			int cw = ival;
			// START KGU#77/KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
			//while (((sval >= 0) ? (cw <= fval) : (cw >= fval)) && trouble.equals("") && (stop == false))
			context.loopDepth++;
			while (((sval >= 0) ? (cw <= fval) : (cw >= fval)) && trouble.equals("") &&
					(stop == false) && !context.returned && leave == 0)
			// END KGU#77/KGU#78 2015-11-25
			{
				// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
				//setVar(counter, cw);
				setVar(counter, cw, forLoopLevel-1);
				// END KGU#307 2016-12-12
				element.waited = true;


				// START KGU#117 2016-03-07: Enh. #77 - consistent subqueue handling
				if (trouble.isEmpty())
				{
					trouble = stepSubqueue(element.getBody(), true);
				}
				// END KGU#117 2016-03-07

				// START KGU#156 2016-03-11: Enh. #124
				element.addToExecTotalCount(1, true);	// For the condition test and increment
				//END KGU#156 2016-03-11
				
				// At this point, we symbolize the time for the incrementing and condition checking
				element.waited = false;
				element.executed = true;
				if (delay != 0 || step)
				{
					diagram.redraw();
				}
				checkBreakpoint(element);
				delay();
				element.executed = false;
				element.waited = true;
				
				// START KGU 2015-10-13: The step value is now calculated in advance
				cw += sval;
				// END KGU 2015-10-13
			}
			// START KGU#78 2015-11-25
			if (leave > 0)
			{
				leave--;
			}
			context.loopDepth--;
			// END KGU#78 2015-11-25
			if (trouble.equals(""))
			{
				element.executed = false;
				element.waited = false;
			}
		} catch (EvalError ex)
		{
			trouble = ex.getMessage();
		}
		// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
		while (forLoopLevel < context.forLoopVars.count()) {
			context.forLoopVars.remove(forLoopLevel);
		}
		// END KGU#307 2016-12-12
		return trouble;
	}
	
	// START KGU#61 2016-03-21: Enh. #84
	// This executes FOR-IN loops
	private String stepForIn(For element)
	{
		String trouble = new String();
		// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
		int forLoopLevel = context.forLoopVars.count();
		// END KGU#307 2016-12-12
		String valueListString = element.getValueList();
		String iterVar = element.getCounterVar();
		Object[] valueList = null;
		String problem = "";	// Gathers exception descriptions for analysis purposes
		Object value = null;
		boolean valueNoArray = false;
		// START KGU#417 2017-06-30: Enh. #424 - Turtleizer functions must be evaluated each time
		try {
			valueListString = this.evaluateDiagramControllerFunctions(valueListString).trim();
		}
		catch (EvalError ex)
		{
			problem += "\n" + ex.getMessage();
		}
		// END KGU#417 2017-06-30
		if (valueListString.startsWith("{") && valueListString.endsWith("}"))
		{
			try
			{
				// START KGU#439 2017-10-13: Issue #436
				//this.evaluateExpression("Object[] tmp20160321kgu = " + valueListString, false, false);
				//value = context.interpreter.get("tmp20160321kgu");
				//context.interpreter.unset("tmp20160321kgu");
				value = this.evaluateExpression(valueListString, true, false);
				// END KGU#439 2017-10-13
			}
			catch (EvalError ex)
			{
				problem += "\n" + ex.getMessage();
			}
		}
		
		// External function calls are not allowed at this position but there of course some
		// functions that yield an array (split) or a string (copy, insert, delete, uppercase,
		// lowercase. The latter ones might even be concatenated. Nevertheless it's relatively
		// safe to evaluate this as content of an array initializer. If the comma was what we
		// assumed (separator of an item enumeration) then the resulting array (i.e. ArrayList)
		// MUST contain more than one element. (If the comma IS an argument separator of a
		// function call then either the function will be an element of the value list or
		// we obtain a single element - or some syntax trouble.)
		if (value == null && valueListString.contains(","))
		{
			try
			{
				// START KGU#439 2017-10-13: Issue #436
				//this.evaluateExpression("Object[] tmp20160321kgu = {" + valueListString + "}", false, false);
				//value = context.interpreter.get("tmp20160321kgu");
				//context.interpreter.unset("tmp20160321kgu");
				value = this.evaluateExpression("{" + valueListString + "}", true, false);
				// END KGU#439 2017-10-13
				// START KGU#856 2020-04-23: Bugfix #858 - there ARE functions returning an array or string
				if (value instanceof ArrayList && ((ArrayList<?>)value).size() == 1) {
					/* If the array contains only a single element then we must have
					 * misinterpreted the comma (may have been part of a string literal
					 * or separator in a function parameter list. So the element is certainly
					 * the array or string we need
					 */
					value = ((ArrayList<?>)value).get(0);
				}
				// END KGU#856 2020-04-23
			}
			catch (EvalError ex)
			{
				problem += "\n" + ex.getMessage();
			}
		}
		// Might be a function or variable otherwise evaluable
		if (value == null)
		{
			try
			{
				value = this.evaluateExpression(valueListString, false, false);
				// START KGU#429 2017-10-08
				// In case it was a variable or function, it MUST contain or return an array to be acceptable
				if (value != null && /*!(value instanceof Object[]) &&*/ !(value instanceof ArrayList<?>) && !(value instanceof String)) {
					valueNoArray = true;
					problem += valueListString + " = " + prepareValueForDisplay(value, context.dynTypeMap);
				}
				// END KGU#429 2017-10-08
			}
			catch (EvalError ex)
			{
				problem += "\n" + ex.getMessage();
			}
		}
		if (value == null && valueListString.contains(" "))
		{
			// Rather desperate attempt to compose an array from loose strings (like in shell scripts)
			StringList tokens = Element.splitExpressionList(valueListString, " ");
			try
			{
				// START KGU#439 2017-10-13: Issue #436
				//this.evaluateExpression("Object[] tmp20160321kgu = {" + tokens.concatenate(",") + "}", false, false);
				//value = context.interpreter.get("tmp20160321kgu");
				//context.interpreter.unset("tmp20160321kgu");
				value = this.evaluateExpression("{" + tokens.concatenate(",") + "}", true, false);
				// END KGU#439 2017-10-13
			}
			catch (EvalError ex)
			{
				problem += "\n" + ex.getMessage();
			}
		}
		if (value != null)
		{
			// Shouldn't occur anymore
			if (value instanceof Object[])
			{
				valueList = (Object[]) value;
			}
			else if (value instanceof ArrayList) {
				valueList = ((ArrayList<?>)value).toArray();
			}
			// START KGU#429 2017-10-08
			else if (value instanceof String) {
				char[] chars = ((String)value).toCharArray(); 
				valueList = new Character[chars.length];
				for (int i = 0; i < chars.length; i++) {
					valueList[i] = chars[i];
				}
			}
			// END KGU#429 2017-10-08
			else if (!valueNoArray)
			{
				valueList = new Object[1];
				valueList[0] = value;
			}
		}

		if (valueList == null)
		{
			trouble = control.msgBadValueList.getText().replace("%", valueListString);
			// START KGU 2016-07-06: Privide the gathered information
			if (!problem.isEmpty())
			{
				trouble += "\n" + control.msgBadValueListDetails.getText().replace("%", problem);
			}
			// END KGU 2016-07-06
		}
		else
		{
				element.addToExecTotalCount(1, true);	// For the condition evaluation
				// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
				context.forLoopVars.add(iterVar);
				// END KGU#307 2016-12-12

				// Leave if any kind of Jump statement has been executed
				context.loopDepth++;
				int cw = 0;

				while (cw < valueList.length && trouble.equals("")
						&& (stop == false) && !context.returned && leave == 0)
				{
					try
					{
						Object iterVal = valueList[cw];
						// START KGU#388 2017-09-27: Enh. #423 declare or un-declare the loop variable dynamically
						TypeMapEntry iterType = null;
						if (iterVal instanceof HashMap<?,?>) {
							Object typeName = ((HashMap<?, ?>)iterVal).get("§TYPENAME§");
							if (typeName instanceof String && (iterType = context.dynTypeMap.get(":" + typeName)) != null) {
								context.dynTypeMap.put(iterVar, iterType);
							}
						}
						else if (iterVal != null && (iterType = context.dynTypeMap.get(iterVar)) != null && iterType.isRecord()) {
							context.dynTypeMap.remove(iterVar);
						}
						// END KGU#388 2017-09-27
						// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
						//setVar(iterVar, valueList[cw]);
						setVar(iterVar, iterVal, forLoopLevel-1);
						// END KGU#307 2016-12-12
						element.executed = false;
						element.waited = true;

						if (trouble.isEmpty())
						{
							trouble = stepSubqueue(((ILoop)element).getBody(), true);						
						}

						element.executed = true;
						element.waited = false;
						if (trouble.equals(""))
						{
							cw++;
							// Symbolizes the loop condition check 
							checkBreakpoint(element);
							delay();
						}
						element.addToExecTotalCount(1, true);	// For the condition evaluation
					} catch (EvalError ex)
					{
						trouble = ex.getMessage();
					}
				}
				// If there are open leave requests then nibble one off
				if (leave > 0)
				{
					leave--;
				}
				context.loopDepth--;
				// START KGU#307 2016-12-12: Issue #307 - prepare warnings on loop variable manipulations
				while (forLoopLevel < context.forLoopVars.count()) {
					context.forLoopVars.remove(forLoopLevel);
				}
				// END KGU#307 2016-12-12
		}
		if (trouble.equals(""))
		{
			element.executed = false;
			element.waited = false;
		}
		/*
		 * if (cw > 1000000) { element.selected = true; trouble =
		 * "Your loop ran a million times. I think there is a problem!";
		 * }
		 */
		return trouble;
	}
	// END KGU#61 2016-03-21
	
	private String stepParallel(Parallel element)
	{
		String trouble = new String();
		try
		{
			int outerLoopDepth = context.loopDepth;
			int nThreads = element.qs.size();
			// For each of the parallel "threads" fetch a subqueue's Element iterator...
			Vector<Iterator<Element> > undoneThreads = new Vector<Iterator<Element>>();
			for (int thr = 0; thr < nThreads; thr++)
			{
				undoneThreads.add(element.qs.get(thr).getIterator());
			}

			element.waited = true;
			// Since we can hardly really execute this in parallel here,
			// the workaround is to run all the "threads" in a randomly chosen order...
			Random rdmGenerator = new Random(System.currentTimeMillis());

			// The first condition holds if there is at least one unexhausted "thread"
			// START KGU#77/KGU#78 2015-11-25: Leave if some kind of Jump statement has been executed
			//while (!undoneThreads.isEmpty() && trouble.equals("") && (stop == false))
			context.loopDepth = 0;	// Loop exits may not penetrate the Parallel section
			while (!undoneThreads.isEmpty() && trouble.equals("") && (stop == false) &&
					!context.returned && leave == 0)
			// END KGU#77/KGU#78 2015-11-25
			{
				// Pick one of the "threads" by chance
				int threadNr = rdmGenerator.nextInt(undoneThreads.size());
				Iterator<Element> iter = undoneThreads.get(threadNr);
				if (!iter.hasNext())
				{
					// Thread is exhausted - drop it
					undoneThreads.remove(threadNr);
				}
				else 
				{
					// Run the next instruction of the chosen thread
					Element instr = iter.next();
					int oldExecCount = instr.getExecStepCount(true);
					// END KGU#156 2016-03-11
					trouble = step(instr);
					// START KGU#117 2016-03-12: Enh. #77
					element.addToExecTotalCount(instr.getExecStepCount(true) - oldExecCount, false);
					// END KGU#117 2016-03-12
					// In order to allow better tracking we put the executed instructions into `waited´ state...
					instr.waited = true;
					// START KGU#78 2015-11-25: Parallel sections are impermeable for leave requests!
					if (trouble == "" && leave > 0)
					{
						// This should never happen (the leave instruction should have failed already)
						// At least we will kill the causing thread...
						undoneThreads.remove(threadNr);
						// ...and then of course wipe the remaining requested levels
						leave = 0;
						// As it is not only a user syntax error but also a flaw in the Structorizer mechanisms we better report it
						// START KGU#247 2016-09-17: Issue #243
						//JOptionPane.showMessageDialog(diagram, "Uncaught attempt to jump out of a parallel thread:\n\n" + 
						//		instr.getText().getText().replace("\n",  "\n\t") + "\n\nThread killed!",
						//		"Parallel Execution Problem", JOptionPane.WARNING_MESSAGE);
						JOptionPane.showMessageDialog(diagram.getParent(), control.msgJumpOutParallel.getText().replace("%", "\n\n" + 
								instr.getText().getText().replace("\n",  "\n\t") + "\n\n"),
								control.msgTitleParallel.getText(), JOptionPane.WARNING_MESSAGE);
						// END KGU#247 2016-09-17
					}
					// END KGU#78 2015-11-25
				}
			}
			context.loopDepth = outerLoopDepth;	// Restore the original context
			if (trouble.equals(""))
			{
				// Recursively reset all `waited´ flags of the subqueues now finished
				element.clearExecutionStatus();
			}
		} catch (Error ex)
		{
			trouble = ex.getMessage();
		}
		return trouble;
	}

	// START KGU#686 2019-03-16: Enh. #56 Introdcution of TRY CATCH FINALLY
	private String stepTry(Try element)
	{
		String trouble = new String();
		element.executed = false;
		element.waited = true;
		
		// Start executing the try block
		
		boolean wasWithinTry = this.withinTryBlock;
		this.withinTryBlock = true;
		
		trouble = stepSubqueue(element.qTry, true);
		
		this.withinTryBlock = wasWithinTry;
		
		// In case of trouble (other than exit) exceute the catch block
		if (!trouble.isEmpty() && !isExited) {
			String origTrouble = trouble;	// For the case of a rethrow
			try {
				this.updateVariableDisplay();
				String varName = element.getExceptionVarName();
				Object priorValue = null;
				boolean hadVariable = false;
				if (varName != null) {
					if ((hadVariable = context.variables.contains(varName))) {
					priorValue = context.interpreter.get(varName);
					}
					setVar(varName, trouble);
				}
				// START KGU#806 2020-02-20: Bugfix #820 From now on new errors may occur
				this.isErrorReported = false;
				// END KGU#806 2020-02-20
				/* Normally the catch block will clear the trouble, but if it causes
				 * trouble itself (e.g. by rethrowing) than this will be the new
				 * trouble */
				trouble = stepSubqueue(element.qCatch, true);
				
				element.qTry.clearExecutionStatus();
				if (hadVariable) {
					setVar(varName, priorValue);
				}
				else if (varName != null) {
					context.interpreter.unset(varName);
					context.variables.removeAll(varName);
				}
				if (trouble.equals(RETHROW_MESSAGE)) {
					// Obviously a rethrow, so restore the original error message
					trouble = origTrouble;
				}
			} catch (EvalError e) {
				trouble = e.toString();
			}
			// FIXME: We should eliminate all variables introduced within the catch block!
		}
		
		// Execute the finally block (even in case of exit - but don't overwrite the exit text then)
		try {
			this.updateVariableDisplay();
			// Execute the finally block
			String finalTrouble = stepSubqueue(element.qFinally, true);
			if (!finalTrouble.isEmpty() && !isExited) {
				// An error out of the finally block adds to a possible previous trouble
				if (!trouble.isEmpty()) {
					trouble = "1. " + trouble + "\n2. " + finalTrouble;
				}
				else {
					trouble = finalTrouble;
				}
			}
		} catch (EvalError ex) {
			if (!isExited && (trouble = ex.getLocalizedMessage()) == null && (trouble = ex.getMessage()) == null || trouble.isEmpty()) {
				trouble = ex.toString();
			}
		}
		
		if (trouble.isEmpty()) {
			element.waited = false;
			element.executed = false;
		}
		
		return trouble;
	}
	// END KGU#686 2019-03-16
	
	// START KGU#117 2016-03-07: Enh. #77 - to track test coverage a consistent subqueue handling is necessary
	/**
	 * 
	 * @param sq
	 * @param checkLeave
	 * @return
	 */
	String stepSubqueue(Subqueue sq, boolean checkLeave)
	{
		String trouble = "";
		
		int i = 0;
		while ((i < sq.getSize())
				&& trouble.equals("") && (stop == false) && !context.returned
				&& (!checkLeave || leave == 0))
		{
			// START KGU#156 2016-03-11: Enh. #124
			//trouble = step(sq.getElement(i));
			Element ele = sq.getElement(i);
			int oldExecCount = ele.getExecStepCount(true);
			trouble = step(ele);
			sq.parent.addToExecTotalCount(ele.getExecStepCount(true) - oldExecCount, false);
			// END KGU#156 2016-03-11
			i++;
		}
		if (sq.getSize() == 0)
		{
			sq.deeplyCovered = sq.simplyCovered = true;
			// START KGU#156 2016-03-11: Enh. #124
			sq.countExecution();
			//END KGU#156 2016-03-11
		}
		return trouble;
	}
	
	// START KGU#388 2017-09-16: Enh. #423 We must prepare expressions with record component access
	/**
	 * Resolves qualified names (record access) where contained and - if allowed by setting
	 * {@code _withInitializers} - array or record initializers and has the interpreter evaluate
	 * the prepared expression.<br/>
	 * This preparation work might perhaps also have been done by the convert function but requires
	 * current evaluation context. So it was rather located here.<br/>
	 * Note: Argument {@code _withInitializers} (and the associated mechanism) was added via
	 * refactoring afterwards with a default value of {@code false} in order to avoid unwanted
	 * impact. If there happens to be some place in code where it seems helpful to activate this
	 * mechanism just go ahead and try.   
	 * @param _expr -the converted expression to be evaluated
	 * @param _withInitializers - whether an array or record initializer is to be managed here
	 * @param _preserveBrackets - if true then brackets won't be substituted
	 * @return the evaluated result if successful 
	 * @throws EvalError an exception if something went wrong (may be raised by the interpreter
	 * or this method itself)
	 */
	protected Object evaluateExpression(String _expr, boolean _withInitializers, boolean _preserveBrackets) throws EvalError
	{
		Object value = null;
		StringList tokens = Element.splitLexically(_expr, true);
		// START KGU#773 2019-11-28: Bugfix #786 Blanks are not tolerated by the susequent mechanisms like index evaluation
		tokens.removeAll(" ");
		// END KGU#773 2019-11-28
		// START KGU#439 2017-10-13: Enh. #436 Arrays now represented by ArrayLists
		if (!_preserveBrackets) {
			if (tokens.indexOf(OBJECT_ARRAY, 0, true) == 0) {
				tokens.set(0, "Object[]");
				tokens.remove(1,3);
			}
			tokens.replaceAll("[", ".get(");
			tokens.replaceAll("]", ")");
		}
		// END KGU#439 2017-10-13
		// Special treatment for inc() and dec() functions? - no need if convert was applied before
		int i = 0;
		while ((i = tokens.indexOf(".", i+1)) > 0) {
			// FIXME: We should check for either declared type or actual object type of what's on the left of the dot.
			// The trouble is that we would have to analyse the expression on the left of the dot in order to find out
			// whether it is a record. But where does it begin? It could be a function call (e.g. copyRecord()) or an
			// indexed access to an array element... An how can we make sure its evaluation hasn't got irreversible side
			// effects?
			// At least the check against following parenthesis will help to avoid the spoiling of Java method calls.
			if (i+1 < tokens.count() && Function.testIdentifier(tokens.get(i+1), false, null) && (i+2 == tokens.count() || !tokens.get(i+2).equals("("))) {
				tokens.set(i, ".get(\"" + tokens.get(i+1) + "\")");
				tokens.remove(i+1);
			}
		}
		// START KGU#100/KGU#388 2017-09-29: Enh. #84, #423 TODO Make this available at more places
		if (tokens.get(tokens.count()-1).equals("}") && _withInitializers) {
			TypeMapEntry recordType = null;
			// START KGU#100 2016-01-14: Enh. #84 - accept array assignments with syntax array <- {val1, val2, ..., valN}
			if (tokens.get(0).equals("{")) {			
				value = evaluateArrayInitializer(_expr, tokens);
			}
			// END KGU#100 2016-01-14
			// START KGU#388 2017-09-13: Enh. #423 - accept record assignments with syntax recordVar <- typename{comp1: val1, comp2: val2, ..., compN: valN}
			else if (tokens.get(1).equals("{") && (recordType = identifyRecordType(tokens.get(0), true)) != null) {
				value = evaluateRecordInitializer(_expr, tokens, recordType);
			}
			// END KGU#388 2017-09-13
		}
		// END KGU#100/KGU#388 2017-09-29
		else
		{
			// Possibly our resolution of qualified names went too far. For this case give it some more tries
			// with partially undone conversions. This should not noticeably slow down the evaluation in case
			// no error occurs.
			boolean error423 = false;
			// START KGU#773 2019-11-28: Bugfix #786 Since blanks have been eliminated now, we must be cautious on concatenation
			//String expr = tokens.concatenate();
			String expr = tokens.concatenate(null);
			// END KGU#773 2019-11-28
			boolean messageAugmented = false;
			do {
				error423 = false;
				try {
					value = context.interpreter.eval(expr);
				}
				catch (EvalError err) {
					String error423message = err.getMessage(); 
					if (error423message.contains(ERROR423MESSAGE)) {
						if (ERROR423MATCHER.reset(error423message).matches()) {
							// Restore the assumed original attribute access and try again
							// (this will at least induce a less confusing message)
							// Could still be improved as we obtain in the end of the message the very name
							expr = ERROR423MATCHER.group(1) + ERROR423MATCHER.group(2) + ERROR423MATCHER.group(3);
							error423 = true;
						}
					}
					// START KGU#510 2018-03-20: Issue #527 - index range problem detection for more helpful message
					else if (ERROR527MATCHER.reset(error423message).matches()) {
						try {
							// START KGU#677 2019-03-09: Bugfix #527
							//Object potArray = context.interpreter.eval(ERROR527MATCHER.group(4));
							//Object potIndex = context.interpreter.eval(ERROR527MATCHER.group(2));
							String arrayName = ERROR527MATCHER.group(5);
							Object potArray = null;
							if (arrayName == null && (arrayName = ERROR527MATCHER.group(1)).contains("copyArray(")) {
								arrayName = arrayName.replaceFirst("^copyArray\\((.*)\\)$", "$1");
							}
							if (arrayName != null) {
								potArray = context.interpreter.eval(arrayName);
							}
							String indexExpr = ERROR527MATCHER.group(2);
							if (indexExpr != null) {
								indexExpr = Element.splitExpressionList(indexExpr, ",").get(0);
							}
							Object potIndex = context.interpreter.eval(indexExpr);
							// END KGU#677 2019-03-09
							if (potArray instanceof ArrayList && potIndex instanceof Integer) {
								int index = ((Integer)potIndex).intValue();
								if (index < 0 || index >= ((ArrayList<?>)potArray).size()) {
									err.setMessage(control.msgIndexOutOfBounds.getText().
											// START KGU#677 2019-03-09: Bugfix #527
											//replace("%1", ERROR527MATCHER.group(2)).
											replace("%1", indexExpr).
											// END KGU#677 2019-03-09
											replace("%2", Integer.toString(index)).
											// START KGU#677 2019-03-09: Bugfix #527
											//replace("%3", ERROR527MATCHER.group(4))
											replace("%3", arrayName)
											// END KGU#677 2019-03-09
											);
								}
								// START KGU#510 2019-02-13: Improvement for issue #527
								// In more complex expressions it may not be the first index that caused the trouble, so look for other causes
								else {
									messageAugmented = addCauseDescription(_expr, err);
								}
								// END KGU#510 2019-02-13
							}
						}
						catch (EvalError err1) {
							//System.out.println(err1);
						}
					}
					// END KGU#510 2018-03-20
					// START KGU#615 2018-12-16: Just a simple workaround for #644 (single level initializer arguments)
					else if (error423message.contains("Encountered \"( {\"")) {
						throw new EvalError(error423message + "\n" + control.msgInitializerAsArgument.getText(), null, null);
					}
					// END KGU#615 2018-12-16
					if (!error423) {
						// START KGU#677 2019-03-09: This shouldn't harm, anyway
						if (!messageAugmented) {
							addCauseDescription(_expr, err);
						}
						// END KGU#677 2019-03-09
						throw err;
					}
				}
				// START KGU#756 2019-11-08: Internal interpreter errors may e.g. occur if a type name "Char" is evaluated
				catch (Error ex) {
					if (ex.getClass().getName().equals("bsh.Parser$LookaheadSuccess")) {
						throw new EvalError("Syntax error in expression «" + expr + "» - possibly a misplaced type name.", null, null);
					}
					else {
						throw ex;
					}
				}
				// END KGU#756 2019-11-08
			} while (error423);
		}
		return value;
	}
	// END KGU#388 2017-09-16

	// START KGU#677 2019-03-09: Issue #527 (revision)
	/**
	 * Replaces the top-level EvalError message with the given expression
	 * and a description of the originating problem, which is often more
	 * expressive than the message of {@code err}.
	 * @param _expr - The original expression or element text
	 * @param err - an {@link EvalError}
	 * @return true if the message of {@code err} had been augmented or replaced.
	 */
	private boolean addCauseDescription(String _expr, EvalError err) {
		Throwable ex = err;
		String msg = null;
		while (ex.getCause() != null) {
			ex = ex.getCause();
			if (ex.getMessage() != null) {
				msg = ex.getMessage();
			}
		}
		if (msg != null) {
			err.setMessage(_expr + ":\n" + msg);
		}
		return msg != null;
	}
	// END KGU#677 2019-03-09

	// START KGU#100 2017-10-08: Enh. #84 - accept array assignments with syntax array <- {val1, val2, ..., valN}
	/**
	 * Recursively pre-evaluates array initializer expressions
	 * @param _expr - the initializer as String (just for a possible error message)
	 * @param tokens - the initializer in precomputed tokenized form
	 * @return object that should be a ArrayList<Object>
	 * @throws EvalError
	 */
	private Object evaluateArrayInitializer(String _expr, StringList tokens) throws EvalError {
		// We have to evaluate those element values in advance, which are initializers themselves...
//		this.evaluateExpression("Object[] tmp20160114kgu = " + tokens.concatenate(), false);
//		value = context.interpreter.get("tmp20160114kgu");
//		context.interpreter.unset("tmp20160114kgu");
		StringList elementExprs = Element.splitExpressionList(tokens.subSequence(1, tokens.count()-1), ",", true);
		int nElements = elementExprs.count();
		if (!elementExprs.get(nElements-1).isEmpty()) {
			throw new EvalError(control.msgInvalidExpr.getText().replace("%1", _expr), null, null);				
		}
		elementExprs.remove(--nElements);
		ArrayList<Object> valueArray = new ArrayList<Object>(nElements);
		for (int i = 0; i < nElements; i++) {
			valueArray.add(evaluateExpression(elementExprs.get(i), true, false));
		}
		return valueArray;
	}
	// END KGU#100 2016-01-14
	// START KGU#388 2017-09-13: Enh. #423 - accept record assignments with syntax recordVar <- typename{comp1: val1, comp2: val2, ..., compN: valN}
	/**
	 * Recursively pre-evaluates record initializer expressions
	 * @param _expr - the expression
	 * @param tokens - the splitting result
	 * @param recordType - the identified record type entry
	 * @return the filled {@link HashMap} of component name - value pairs
	 * @throws EvalError
	 */
	private Object evaluateRecordInitializer(String _expr, StringList tokens, TypeMapEntry recordType) throws EvalError {
//		this.evaluateExpression("HashMap tmp20170913kgu = new HashMap()", false);
		// START KGU#559 2018-07-20: Enh. #563 - simplified record initializers (smarter interpretation)
		//HashMap<String, String> components = Element.splitRecordInitializer(tokens.concatenate(null));
		HashMap<String, String> components = Element.splitRecordInitializer(tokens.concatenate(null), recordType, false);
		// END KGU#559 2018-07-20
		if (components == null || components.containsKey("§TAIL§")) {
			throw new EvalError(control.msgInvalidExpr.getText().replace("%1", _expr), null, null);
		}
		HashMap<String, Object> valueRecord = new LinkedHashMap<String, Object>();
		valueRecord.put("§TYPENAME§", components.remove("§TYPENAME§"));
		LinkedHashMap<String, TypeMapEntry> compDefs = recordType.getComponentInfo(false);
		for (Entry<String, String> comp: components.entrySet()) {
			if (compDefs.containsKey(comp.getKey())) {
				// We have to evaluate the component value in advance if it is an initializer itself...
				//context.interpreter.eval("tmp20170913kgu.put(\"" + comp.getKey() + "\", " + comp.getValue() + ");");
				valueRecord.put(comp.getKey(), this.evaluateExpression(comp.getValue(), true, false));
			}
			else {
				throw new EvalError(control.msgInvalidComponent.getText().replace("%1", comp.getKey()).replace("%2", recordType.typeName), null, null);
			}
		}
//		value = context.interpreter.get("tmp20170913kgu");
//		if (value instanceof HashMap<?,?>) {
//			((HashMap<String, Object>)value).put("§TYPENAME§", recordType.typeName);
//		}
//		context.interpreter.unset("tmp20170913kgu");
		return valueRecord;
	}
	// END KGU#388 2017-09-13
	
	private void waitForNext()
	{
		// START KGU#379 2017-04-12: Bugfix #391: This is the proper place to prepare the buttons for pause mode
		// Well, maybe it is better put into the synchronized block?
		if (getPaus()) {
			control.setButtonsForPause(true);
		}
		// END KGU#379 2017-04-12
		synchronized (this)
		{
			while (paus == true)
			{
				try
				{
					wait();
				} catch (Exception e)
				{
					logger.log(Level.SEVERE, "wait()", e);
				}
			}
		}
		/*
		 * int i = 0; while(paus==true) { System.out.println(i);
		 * 
		 * try { Thread.sleep(100); } catch (InterruptedException e) {
		 * System.out.println(e.getMessage());} i++; }
		 */

		if (step == true)
		{
			paus = true;
		}
	}
	
	// START KGU#33/KGU#34 2014-12-05
	// Method tries to extract the index value from an expression formed like
	// an array element access, i.e. "<arrayname>[<expression>]"
	private int getIndexValue(String varname) throws EvalError
	{
		// START KGU#141 2016-01-16: Bugfix #112
		String message = "Illegal (negative) index";
		// END KGU#141 2016-01-16
		String ind = varname.substring(varname.indexOf("[") + 1,
				// START KGU#166 2016-03-29: Bugfix #139 (nested index expressions failed)
				//varname.indexOf("]"));
				varname.lastIndexOf("]"));
				// END KGU#166 2016-03-29

		int index = -1;

		try
		{
			//index = Integer.parseInt(ind);	// KGU: This was nonsense - usually no literal here
			index = (Integer) this.evaluateExpression(ind, false, false);
		}
		catch (Exception e)
		{
			//index = (Integer) this.interpreter.get(ind);	// KGU: This didn't work for expressions
			// START KGU#141 2016-01-16: Bugfix #112 - this led to silent errors and incapacitation of executor
			//System.out.println(e.getMessage() + " on " + varname + " in Executor.getIndexValue()");
			message = e.getMessage();	// We will rethrow it later
			// END KGU#141 2016-01-16
		}
		// START KGU#141 2016-01-16: Bugfix #112 - We may not allow negative indices
		if (index < 0)
		{
			throw new EvalError(message + " on index evaluation in: " + varname, null, null);
		}
		// END KGU#141 2016-01-16
		return index;
	}
	// END KGU#33/KGU#34 2014-12-05
	
	// START KGU#165 2016-04-03: Support keyword case sensitivity
	/**
	 * Returns an appropriate match string for the given parser preference string
	 * (where CodeParser.ignoreCase is paid attention to)
	 * @param keyword - parser preference string
	 * @return match pattern
	 */
	private String getKeywordPattern(String keyword)
	{
		String pattern = Matcher.quoteReplacement(keyword);
		if (CodeParser.ignoreCase)
		{
			pattern = BString.breakup(pattern, true);
		}
		return pattern;
	}
	// END KGU#165 2016-04-03
	
	// START KGU#156 2016-03-10: An interface for an external update trigger was needed
	public void redraw()
	{
		diagram.repaint();
	}
	// END KGU#156 2016-03-10
	
    // START KGU#160 2016-04-12: Enh. #137 - Checkbox for text window output
	public void setOutputWindowEnabled(boolean _enabled)
	{
		this.isConsoleEnabled = _enabled;
		// START KGU#569 2018-08-09: Issue #577 - console window might have got corrupted
		//this.console.setVisible(_enabled);
		try {
			this.console.setVisible(_enabled);
		}
		catch (NullPointerException ex) {
			this.console.dispose();
			this.console = new OutputConsole();
			this.console.setVisible(_enabled);
		}
	}
	// END KGU#160 2016-04-12

}

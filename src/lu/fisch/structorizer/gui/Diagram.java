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
 *      Description:    This class represents the visual diagram itself.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-09      First Issue
 *      Kay Gürtzig     2015-10-09      Colour setting will now duly be registered as diagram modification
 *                      2015-10-11      Comment popping repaired by proper subclassing of getElementByCoord
 *                                      Listener method MouseExited now enabled to drop the sticky comment popup
 *      Kay Gürtzig     2015-11-08      Parser preferences for FOR loops enhanced (KGU#3)
 *      Kay Gürtzig     2015-11-22      Selection of Subqueue subsequences or entire Subqueues enabled
 *                                      thus allowing collective operations like delete/cut/copy/paste (KGU#87).
 *      Kay Gürtzig     2015-11-24      Method setRoot() may now refuse the replacement (e.g. on cancelling
 *                                      the request to save recent changes)
 *      Kay Gürtzig     2015-11-29      New check options added to analyserNSD()
 *      Kay Gürtzig     2015-12-02      Bugfix #39 (KGU#91)
 *      Kay Gürtzig     2015-12-04      Bugfix #40 (KGU#94): With an error on saving, the recent file was destroyed
 *      Kay Gürtzig     2015-12-16      Bugfix #63 (KGU#111): Error message on loading failure
 *      Kay Gürtzig     2016-01-02      Bugfix #85 (KGU#120): Root changes are also subject to undoing/redoing
 *      Kay Gürtzig     2016-01-03      Issue #65 (KGU#123): Collapsing/expanding from menu, autoscroll enabled 
 *      Kay Gürtzig     2016-01-11      Bugfix #102 (KGU#138): clear selection on delete, undo, redo 
 *      Kay Gürtzig     2016-01-15      Enh. #110: File open dialog now selects the NSD filter
 *      Kay Gürtzig     2016-01-21      Bugfix #114: Editing restrictions during execution, breakpoint menu item
 *      Kay Gürtzig     2016-02-03      Bugfix #117: Title and button update on root replacement (KGU#149)
 *      Kay Gürtzig     2016-03-02      Bugfix #97: Reliable selection mechanism on dragging (KGU#136)
 *      Kay Gürtzig     2016-03-08      Bugfix #97: Drawing info invalidation now involves Arranger (KGU#155)
 *      Kay Gürtzig     2016-03-16      Bugfix #131: Precautions against replacement of Root under execution (KGU#158)
 *      Kay Gürtzig     2016-03-21      Enh. #84: FOR-IN loops considered in editing and parser preferences (KGU#61)
 *      Kay Gürtzig     2016-04-01      Issue #143 (comment popup off on editing etc.), Issue #144 (preferred code generator)
 *      Kay Gürtzig     2016-04-04      Enh. #149: Characterset configuration for export supported
 *      Kay Gürtzig     2016-04-05      Bugfix #155: Selection must be cleared in newNSD()
 *      Kay Gürtzig     2016-04-07      Enh. #158: Moving selection as cursor key actions (KGU#177)
 *      Kay Gürtzig     2016-04-14      Enh. #158: moveSelection() now updates the scroll view (KGU#177)
 *      Kay Gürtzig     2016-04-19      Issue #164 (no selection heir on deletion) and #165 (inconsistent unselection)
 *      Kay Gürtzig     2016-04-23      Issue #168 (no selection heir on cut) and #169 (no selection on start/undo/redo)
 *      Kay Gürtzig     2016-04-24      Bugfixes for issue #158 (KGU#177): Leaving the body of Parallel, Forever etc. downwards,
 *                                      button state update was missing.
 *      Kay Gürtzig     2016-04-24      Issue #169 accomplished: selection on start / after export
 *      Kay Gürtzig     2016-05-02      Bugfix #184: Imported root must be set changed.
 *      Kay Gürtzig     2016-05-08      Issue #185: Import of multiple roots per file (collected in Arranger, KGU#194)
 *      Kay Gürtzig     2016-07-06      Enh. #188: New method transmuteNSD() for element conversion (KGU#199)
 *      Kay Gürtzig     2016-07-19      Enh. #192: File name proposals slightly modified (KGU#205)
 *      Kay Gürtzig     2016-07-20      Enh. #160: New export option genExportSubroutines integrated (KGU#178)
 *      Kay Gürtzig     2016-07-21      Enh. #197: Selection may be expanded by Shift-Up and Shift-Down (KGU#206)
 *      Kay Gürtzig     2016-07-25      Enh. #158 / KGU#214: selection traversal accomplished for un-boxed Roots,
 *                                      and FOREVER / non-DIN FOR loops
 *      Kay Gürtzig     2016-07-26      Bugfix #204: Modified ExportOptionDialoge API (for correct sizing)
 *      Kay Gürtzig     2016-07-28      Bugfix #208: Modification in setFunction(), setProgram(), and exportPNG()
 *                                      Bugfix #209: exportPNGmulti() corrected
 *      Kay Gürtzig     2016-07-31      Issue #158 Changes from 2016.07.25 partially withdrawn, additional restrictions
 *      Kay Gürtzig     2016-08-01      Issue #213: FOR loop transmutation implemented
 *                                      Enh. #215: Breakpoint trigger counters added (KGU#213)
 *      Kay Gürtzig     2016-08-12      Enh. #231: Analyser checks rorganised to arrays for easier maintenance
 *      Kay Gürtzig     2016-09-09      Issue #213: preWhile and postWhile keywords involved in FOR loop transmutation
 *      Kay Gürtzig     2016-09-11      Issue #213: Resulting selection wasn't highlighted
 *      Kay Gürtzig     2016-09-13      Bugfix #241: Modification in showInputBox()
 *      Kay Gürtzig     2016-09-15      Issue #243: Forgotten message box texts included in localization,
 *                                      Bugfix #244: Flaws in the save logic mended
 *      Kay Gürtzig     2016-09-17      Issue #245: Message box for failing browser call in updateNSD() added.
 *      Kay Gürtzig     2016-09-21      Issue #248: Workaround for legacy Java versions (< 1.8) in editBreakTrigger()
 *      Kay Gürtzig     2016-09-24      Enh. #250: Several modifications around showInputBox()
 *      Kay Gürtzig     2016-09-25      Enh. #253: D7Parser.keywordMap refactoring done, importOptions() added.
 *      Kay Gürtzig     2016-09-26      Enh. #253: Full support for diagram refactoring implemented.
 *      Kay Gürtzig     2016-10-03      Enh. #257: CASE element transmutation (KGU#267), enh. #253 revised
 *      Kay Gürtzig     2016-10-06      Minor improvements in FOR and CALL transmutations (enh. #213/#257)
 *      Kay Gürtzig     2016-10-06      Bugfix #262: Selection and dragging problems after insertion, undo, and redo
 *      Kay Gürtzig     2016-10-07      Bugfix #263: "Save as" now updates the current directory
 *      Kay Gürtzig     2016-10-11      KGU#280: field isArrangerOpen replaced by a method (due to volatility)
 *      Kay Gürtzig     2016-10-13      Enh. #270: Functionality for the disabling of elements
 *      Kay Gürtzig     2016-11-06      Issue #279: All references to method HashMap.getOrDefault() replaced
 *      Kay Gürtzig     2016-11-09      Issue #81: Scale factor no longer rounded, Update font only scaled if factor > 1
 *      Kay Gürtzig     2016-11-15      Enh. #290: Opportunities to load arrangements via openNSD() and FilesDrop
 *      Kay Gürtzig     2016-11-16      Bugfix #291: upward cursor traversal ended in REPEAT loops
 *      Kay Gürtzig     2016-11-17      Bugfix #114: Prerequisites for editing and transmutation during execution revised
 *      Kay Gürtzig     2016-11-18/19   Issue #269: Scroll to the element associated to a selected Analyser error
 *      Kay Gürtzig     2016-11-21      Issue #269: Focus alignment improved for large elements
 *      Kay Gürtzig     2016-12-02      Enh. #300: Update notification mechanism
 *      Kay Gürtzig     2016-12-12      Enh, #305: Infrastructure for Arranger root list
 *      Kay Gürtzig     2016-12-28      Enh. #318: Backsaving of unzipped diagrams to arrz file
 *      Kay Gürtzig     2017-01-04      Bugfix #321: Signatures of saveNSD(), doSaveNSD(), saveAsNSD() and zipToArrz() enhanced
 *      Kay Gürtzig     2017-01-09      Bugfix #330: Scaling of FileChooser for Nimbus L&F solved
 *      Kay Gürtzig     2017-01-27      Issues #290/#306: Signature and logic of openNsdOrArr slightly modified
 *      Kay Gürtzig     2017-02-08      Bugfix #198: Cursor navigation for Alternatives and CASE elements fixed
 *      Kay Gürtzig     2017-02-27      Enh. #346: Export option dialog changes for user-specific include directives
 *      Kay Gürtzig     2017-03-04      Enh. #354: Code import generalized
 *      Kay Gürtzig     2017-03-06      Enh. #368: New import option: code import of variable declarations
 *      Kay Gürtzig     2017-03-08      Enh. #354: file dropping generalized, new import option to save parseTree
 *      Kay Gürtzig     2017-03-10      Enh. #367: IF transmutation added: Swapping of the branches
 *      Kay Gürtzig     2017-03-12      Enh. #372: Author name configurable in save options
 *      Kay Gürtzig     2017-03-14      Enh. #372: Author name and license info editable now
 *      Kay Gürtzig     2017-03-15      Enh. #354: New menu strategy for code import - selection by FileChooser
 *      Kay Gürtzig     2017-03-19/27   Enh. #380: New function to outsource subsequences to routines
 *      Kay Gürtzig     2017-03-28      Issue #370: Improved dialog strategies for refactoring (parser preferences)
 *      Kay Gürtzig     2017-04-27      Enh. #354: New Import option log directory
 *      Kay Gürtzig     2017-05-07      Enh. #399: Message on dropping files of unsupported type.
 *      Kay Gürtzig     2017-05-09      Issue #400: Proper check whether preference changes were committed
 *      Kay Gürtzig     2017-05-11      Enh. #357: Mechanism to retrieve plugin-specified generator options
 *      Kay Gürtzig     2017-05-16      Enh. #389: Support for third diagram type (include/import)
 *      Kay Gürtzig     2017-05-18      Issue #405: New preference for width shrinking of CASE elements 
 *      Kay Gürtzig     2017-05-21      Enh. #372: AttributeInspector integrated, undo mechanism adapted
 *      Kay Gürtzig     2017-05-23      Enh. #354: On multiple-root code import now all roots go to Arranger
 *      Kay Gürtzig     2017-06-20      Enh. #354,#357: GUI Support for configuration of plugin-specific options
 *      Kay Gürtzig     2017-07-01      Enh. #389: Include mechanism transferred from CALL to ROOT
 *      Kay Gürtzig     2017-07-02      Enh. #357: plugin-specific option retrieval for code import
 *      Kay Gürtzig     2017-09-12      Enh. #415: Find&Replace dialog properly re-packed after L&F change
 *      Kay Gürtzig     2017-10-10      Issue #432: Workaround for nasty synch problem in redraw()
 *      Kay Gürtzig     2017-10-12      Issue #432: redrawing made optional in two methods 
 *      Kay Gürtzig     2017-10-23      Positioning of sub-dialogs no longer depends on diagram size
 *                                      Issue #417: scroll units adapted to Root size to reduce time complexity
 *      Kay Gürtzig     2017-10-28      Enh. #443: Slight adaption for multiple DiagramControllers
 *      Kay Gürtzig     2017-11-03      Bugfix #417: division by zero exception in scroll unit adaptation averted
 *      Kay Gürtzig     2017-12-06      Enh. #487: Support for hiding declaration sequences (still defective)
 *      Kay Gürtzig     2017-12-12      Issue #471: Option to copy error message to clipboard in importCode()
 *      Kay Gürtzig     2017-12-15      Issue #492: Element type name configuration
 *      Kay Gürtzig     2018-01-03      Enh. #415: Ensured that the Find&Replace dialog regains focus when selected
 *      Kay Gürtzig     2018-01-21      Enh. #490: New DiagramController alias preferences integrated
 *      Kay Gürtzig     2018-01-22      Post-processing of For elements after insertion and modification unified
 *      Kay Gürtzig     2018-02-09      Bugfix #507: Must force a complete redrawing on changing IF branch labels
 *      Kay Gürtzig     2018-02-15      Bugfix #511: Cursor key navigation was caught in collapsed loops. 
 *      Kay Gürtzig     2018-02-18      Bugfix #511: Collapsed CASE and PARALLEL elements also caught down key.
 *      Kay Gürtzig     2018-03-13      Enh. #519: "Zooming" via controlling font size with Ctrl + mouse wheel 
 *      Kay Gürtzig     2018-03-15      Bugfix #522: Outsourcing now considers record types and includes
 *      Kay Gürtzig     2018-03-20      Bugfix #526: Workaround for failed renaming of temporarily saved file
 *      Kay Gürtzig     2018-04-03      KGU#514: analyse() call on mere mouse clicking avoided
 *      Kay Gürtzig     2018-06-08      Issue #536: Precaution against command line argument trouble in openNsdOrArr()
 *      Kay Gürtzig     2018-06-11      Issue #143: Comment popup off on opening print preview
 *      Kay Gürtzig     2018-06-27      Enh. #552: Mechanism for global decisions on serial actions (save, overwrite)
 *                                      Usability of the parser choice dialog for code import improved.
 *      Kay Gürtzig     2018-07-02      KGU#245: color preferences modified to work with arrays
 *      Kay Gürtzig     2018-07-09      KGU#548: The import option dialog now retains the selected plugin for specific options
 *      Kay Gürtzig     2018-07-27      Bugfix #569: Report list didn't react to mouse clicks on a selected line
 *      Kay Gürtzig     2018-09-10      Issue #508: New option to continue with fix paddings in fontNSD()
 *      Kay Gürtzig     2018-09-13      Enh. #590: method attributesNSD() parameterized for Arranger Index use.
 *      Kay Gürtzig     2018-10-01      Bugfix #367: After IF branch swapping the drawing invalidation had wrong direction
 *      Kay Gürtzig     2018-10-26/28   Enh. #419: New import option impMaxLineLength, new method rebreakLines()
 *      Kay Gürtzig     2018-10-29      Enh. #627: Clipboard copy of a code import error will now contain stack trace if available
 *      Kay Gürtzig     2018-12-18      Bugfix #648, #649 - safe import from Struktogrammeditor, scrolling performance
 *      Kay Gürtzig     2019-01-06      Enh. #657: Outsourcing with group context
 *      Kay Gürtzig     2019-01-13      Enh. #662/4: Support for new saving option to store relative coordinates in arr files
 *      Kay Gürtzig     2019-01-17      Issue #664: Workaround for ambiguous canceling in AUTO_SAVE_ON_CLOSE mode
 *      Kay Gürtzig     2019-01-20      Issue #668: Group behaviour on outsourcing subdiagrams improved.
 *      Kay Gürtzig     2019-02-15/16   Enh. #681 - mechanism to propose new favourite generator after repeated use
 *      Kay Gürtzig     2019-02-26      Bugfix #688: canTransmute() should always return true for Call and Jump elements
 *      Kay Gürtzig     2019-02-26      Enh. #689: Mechanism to edit the referred routine of a selected Call introduced
 *      Kay Gürtzig     2019-03-01      Bugfix #693: Missing existence check on loading recent arrangement files added
 *      Kay Gürtzig     2019-03-13      Issues #518, #544, #557: Element drawing now restricted to visible rect.
 *      Kay Gürtzig     2019-03-25      Issue #685: Workaround for exception stack traces on copying to windows clipboard 
 *      Kay Gürtzig     2019-03-27      Enh. #717: Configuration of scroll increment (Element.E_WHEEL_SCROLL_UNIT)
 *      Kay Gürtzig     2019-03-28      Enh. #657: Retrieval for subroutines now with group filter
 *      Kay Gürtzig     2019-03-29      Issues #518, #544, #557 drawing speed improved by redraw area reduction
 *      Kay Gürtzig     2019-03-20      Bugfix #720: Proper reflection of includable changes ensured
 *      Kay Gürtzig     2019-06-13      Bugfix #728: Wipe the result tree in an opened F&R dialog on editing.
 *      Kay Gürtzig     2019-07-31      Issue #526: File renaming workaround reorganised on occasion of bugfix #731
 *      Kay Gürtzig     2019-08-01      KGU#719: Refactoring dialog redesigned to show a JTable of key pairs
 *      Kay Gürtzig     2019-08-03      Issue #733: Selective property export mechanism implemented.
 *      Kay Gürtzig     2019-09-24      Bugfix #751: Cursor traversal didn't reach Try element internals
 *      Kay Gürtzig     2019-09-23      Enh. #738: First code preview implementation approaches
 *      Kay Gürtzig     2019-09-27      Enh. #738: Methods for code preview popup menu reaction
 *      Kay Gürtzig     2019-09-28      Javadoc completions, fine-tuning for #738
 *      Kay Gürtzig     2019-09-29      Issue #753: Unnecessary structure preference synchronization offers suppressed.
 *      Kay Gürtzig     2019-10-05      Issues #758 (Edit subroutine) and KGU#743 (root type change) fixed
 *      Kay Gürtzig     2019-10-07      Error message fallback for cases of empty exception text ensured (KGU#747)
 *      Kay Gürtzig     2019-10-13/15   Bugfix #763: Stale file also triggers save request in saveNSD()
 *      Bob Fisch       2019-11-24      New method setRootForce() introduced as interface for Unimozer (c)
 *      Kay Gürtzig     2019-11-29      Bugfix #777: Concurrent favourite export language modification now properly handled
 *      Kay Gürtzig     2020-01-20      Enh. #801 - Offline help added, exception handling flaw in helpNSD() fixed
 *      Kay Gürtzig     2020-02-04      Bugfix #805: Several volatile preferences cached to the Ini instance when modified
 *      Kay Gürtzig     2020-02-16      Issue #815: Combined file filter (StructorizerFilter) preferred in openNSD()
 *      Kay Gürtzig     2020-03-03      Enh. #440: New method to support PapDesigner export
 *      Kay Gürtzig     2020-03-16/17   Enh. #828: New method to export an arrangement group
 *      Kay Gürtzig     2020-04-22      Enh. #855: New export options for array size / string length defaults
 *      Kay Gürtzig     2020-04-23      Bugfix #856: Selective preference saving to file didn't work properly
 *      Kay Gürtzig     2020-04-28      Bugfix #865: On subroutine generation arguments true and false weren't recognised
 *      Kay Gürtzig     2020-05-02      Issue #866: Selection expansion / reduction mechanisms revised
 *      Kay Gürtzig     2020-06-03      Issue #868: Code import via files drop had to be disabled in restricted mode
 *      Kay Gürtzig     2020-10-17      Enh. #872: New display mode for operators (in C style)
 *      Kay Gürtzig     2020-10-18      Issue #875: Direct diagram saving into an archive, group check in canSave(true)
 *      Kay Gürtzig     2020-10-20/22   Issue #801: Ensured that the User Guide download is done in a background thread
 *      Kay Gürtzig     2020-12-10      Bugfix #884: Flaws of header inference for virgin diagrams mended
 *      Kay Gürtzig     2020-12-12      Enh. #704: Adaptations to Turtleizer enhancements
 *      Kay Gürtzig     2020-12-14      Bugfix #887: TurtleBox must be shared
 *      Kay Gürtzig     2020-12-20      Bugfix #892: "Save as" and double-click trouble with arranged diagrams
 *      Kay Gürtzig     2020-12-25      Enh. #896: Cursor shape changes when element dragging is permissible,
 *                                      dragging elements above the target position enabled via the Shift key
 *      Kay Gürtzig     2020-12-29      Issue #901: Time-consuming actions set WAIT_CURSOR now
 *      Kay Gürtzig     2020-12-30      Issue #901: WAIT_CURSOR now also applied to saveAllNSD()
 *      Kay Gürtzig     2021-01-01      Enh. #903: Syntax highlighting in popup, popup adaption on L&F change
 *      Kay Gürtzig     2021-01-06      Enh. 905: New Analyser markers suppressed on image export and printing
 *                                      Bugfix #907: Duplicate code in goRun() led to a skipped tutorial step,
 *                                      Issue #569: Diagram scrolling on errorlist selection improved
 *      Kay Gürtzig     2021-01-10      Enh. #910: Effective support for actual DiagramControllers
 *      Kay Gürtzig     2021-01-23/25   Enh. #915: Special editor for Case elements (InputBoxCase) supported
 *      Kay Gürtzig     2021-01-27      Enh. #917: editSubNSD() (#689) now also applies to referred Includables
 *      Kay Gürtzig     2021-01-30      Bugfix #921: recursive type retrieval for outsizing, handling of enum types
 *      Kay Gürtzig     2021-02-04      Enh. #926: Element selection now scrolls to the related Analyser warnings
 *      Kay Gürtzig     2021-02-12      Bugfix #936 in exportGroup() - failed on a group never having been saved
 *      Kay Gürtzig     2021-02-24      Bugfix #419: rebreakLines() did not redraw though it induces reshaping
 *      Kay Gürtzig     2021-02-28      Issue #905: Faulty redrawing policy after AnalyserPreference changes fixed
 *      Kay Gürtzig     2021-03-01      Bugfix #950: Arranger notifications were accidently switched off on code import
 *      Kay Gürtzig     2021-03-02      Bugfix #951: On FilesDrop for source files the language-specific options weren't used
 *      Kay Gürtzig     2021-03-03      Issue #954: Modified behaviour of "Clear all Breakpoints" button
 *      Kay Gürtzig     2021-04-14      Bugfix #969: Precaution against relative paths in currentDirectory
 *      Kay Gürtzig     2021-04-16      Enh. #967: ARM code export options retrieved from Ini instead from menu item
 *      Kay Gürtzig     2021-06-03      Bugfix KGU#975: Signature of setPluginSpecificOptions() refactored
 *      Kay Gürtzig     2021-06-08      Enh. #953: Modifications for ExportOptionDialog (line numbering option)
 *      Kay Gürtzig     2021-06-09      Bugfix #977: Attempt of a workaround for a code preview problem
 *      Kay Gürtzig     2021-06-10/11   Enh. #926, #979: Analyser report tooltip on the Analyser marker driehoekje (#905)
 *      Kay Gürtzig     2021-09-18      Bugfix #983: Summoning a subroutine to an editor unduly turned it 'changed'
 *      Kay Gürtzig     2021-10-29      Issue #1004: Export/import option dialogs now respect plugin-specific option defaults
 *      Kay Gürtzig     2021-11-14      Enh. #967: Analyser preferences enhanced by plugin-specific checks
 *      Kay Gürtzig     2022-05-08      Bugfix #1033: Diagram import left a stale Analyser report list.
 *      Kay Gürtzig     2022-06-24      Bugfix #1038: Additional argument for setRoot() to suppress recursive saving requests
 *      Kay Gürtzig     2022-08-18      Enh. #1066: text auto-completion mechanism in showInputBox()
 *      Kay Gürtzig     2022-08-25      Enh. #1066: Infinity literal added to auto-complete words.
 *      Kay Gürtzig     2023-09-12      Bugfix #1086: Defective arrangement on source import with two routines
 *      Kay Gürtzig     2023-11-09      Enh. #1114: Place the InputBox caret at the first question mark in
 *                                      the default text for new Elements
 *      Kay Gürtzig     2024-03-07      Issue #1129: Restrict the number of lines to show in a warning popup
 *      Kay Gürtzig     2024-03-15      Bugfix #1140: Transmutation conditions were too strict for method calls
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *      
 *      2021-04-16 (Kay Gürtzig, #967 = ARM code export)
 *      - Alessandro Simonetta had added a Dialog menu item to switch among two ARM code syntax versions
 *        (GNU/KEIL). As this is not a diagram property, the mechanism was replaced by a plugin-specific
 *        Boolean export option "gnuCode". On this occasion the option retrieval code had to be fixed.
 *      2016-07-31 (Kay Gürtzig, #158)
 *      - It turned out that circular horizontal selection move is not sensible. It compromises usability
 *        rather than it helps. With active horizontal mouse scrolling the respective diagram margin is
 *        so quickly reached that a breathtaking rotation evolves - no positioning is possible. Even with
 *        cursor keys you fall too rapidly into the margin trap, just to be kicked to a totally different
 *        place. This makes navigation rather hazardous. Selection chain will end at the left or right
 *        margin now, giving pause for consideration.
 *        Moving inwards the diagram from the selected Root will still work.
 *
 ******************************************************************************************************///

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.print.*;
import java.awt.datatransfer.*;

import net.iharder.dnd.*; //http://iharder.sourceforge.net/current/java/filedrop/

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.imageio.*;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.freehep.graphicsio.emf.*;
import org.freehep.graphicsio.pdf.*;
import org.freehep.graphicsio.swf.*;

import lu.fisch.diagrcontrol.DiagramController;
import lu.fisch.graphics.*;
import lu.fisch.utils.*;
import lu.fisch.utils.Desktop;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.io.*;
import lu.fisch.structorizer.locales.Locales;
import lu.fisch.structorizer.generators.*;
import lu.fisch.structorizer.helpers.GENPlugin;
import lu.fisch.structorizer.helpers.IPluginClass;
import lu.fisch.structorizer.archivar.Archivar;
import lu.fisch.structorizer.archivar.IRoutinePool;
import lu.fisch.structorizer.arranger.Arranger;
import lu.fisch.structorizer.arranger.Group;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.elements.Element.DrawingContext;
import lu.fisch.structorizer.executor.Control;
import lu.fisch.structorizer.executor.Executor;
import lu.fisch.structorizer.executor.Function;
import lu.fisch.turtle.TurtleBox;

import org.freehep.graphicsio.svg.SVGGraphics2D;

/**
 * Represents the working area of the Structorizer. Holds the current
 * Nassi-Shneiderman diagram and manages all editing ativities as well as
 * loading, saving, import, export etc.
 *
 * @author Robert Fisch
 * @author Kay Gürtzig
 */
@SuppressWarnings("serial")

public class Diagram extends JPanel implements MouseMotionListener, MouseListener, Printable, MouseWheelListener, ClipboardOwner, ListSelectionListener {

	// START KGU#363 2017-03-28: Enh. #370 - allow to break a Root-modifying activity
	/**
	 * Exception may be raised if a Root-modifying action was denied
	 *
	 * @author Kay Gürtzig
	 */
	public class CancelledException extends Exception
	{
		public CancelledException() {
			super("Cancelled");
		}
	}
	// END KGU#363 2017-03-28

	// START KGU#484 2018-03-22: Info #463
	public static final Logger logger = Logger.getLogger(Diagram.class.getName());
	// END KGU#484 2018-03-22

	/**
	 * Fixed size limitation for the file history
	 */
	private static final int MAX_RECENT_FILES = 10;

	// START KGU#48 2015-10-18: We must be capable of preserving consistency when root is replaced by the Arranger
	/**
	 * The current Nassi-Shneiderman diagram
	 *
	 * @see #getRoot()
	 * @see #setRoot(Root, boolean, boolean, boolean)
	 * @see #setIf
	 */
	//public Root root = new Root();
	private Root root = new Root();
	// END KGU#48 2015-10-18
	// START KGU#873 2020-12-14: Bugfix #887 All diagrams must share the same Turtleizer
	//private TurtleBox turtle = null;
	private static TurtleBox turtle = null;
	// END KGU#873 2020-12-14

	private Element selected = null;

	private boolean mouseMove = false;
	private int mouseX = -1;
	private int mouseY = -1;
	/**
	 * Selected element with mouse button down (i.e. element eligible for
	 * dragging)
	 */
	private Element selectedDown = null;
	/**
	 * On dragging elements, the element being moved
	 */
	private Element selectedMoved = null;
	private int selX = -1;
	private int selY = -1;
	private int mX = -1;
	private int mY = -1;

	private NSDController NSDControl = null;

	// START KGU#534 2018-06-27: Enh. #552
	/*========================================
	 * Serial action support
	 *========================================*/
	/**
	 * Nesting depth of serial actions
	 */
	private static short serialActionDepth = 0;

	public enum SerialDecisionStatus {
		INDIVIDUAL, YES_TO_ALL, NO_TO_ALL
	};

	public enum SerialDecisionAspect {
		SERIAL_SAVE, SERIAL_OVERWRITE, SERIAL_GROUP_SAVE
	};
	private static final SerialDecisionStatus[] serialDecisions = {
			SerialDecisionStatus.INDIVIDUAL,	// SERIAL_SAVE
			SerialDecisionStatus.INDIVIDUAL,	// SERIAL_OVERWRITE
			SerialDecisionStatus.INDIVIDUAL,	// SERIAL_GROUP_SAVE
	}; 
	/**
	 * Enters a serial action - thus allowing general decisions to certain
	 * aspects of a serial action. Starts with INDIVIDIAL decision for all
	 * aspects.<br/>
	 * Make sure to call {@link #endSerialMode()} on terminating the serial
	 * action.<br/>
	 * Is nesting-aware.
	 *
	 * @see #endSerialMode()
	 * @see #isInSerialMode()
	 * @see #setSerialDecision(SerialDecisionAspect, boolean)
	 * @see #getSerialDecision(SerialDecisionAspect)
	 */
	public static void startSerialMode() {
		if (serialActionDepth <= 0) {
			for (int i = 0; i < serialDecisions.length; i++) {
				serialDecisions[i] = SerialDecisionStatus.INDIVIDUAL;
			}
			serialActionDepth = 1;
		} else {
			serialActionDepth++;
		}
	}

	/**
	 * Leaves a serial action (i.e. the current nesting level). On ending the
	 * outermost level, all serial decisions are cleared.
	 *
	 * @see #startSerialMode()
	 * @see #isInSerialMode()
	 * @see #setSerialDecision(SerialDecisionAspect, boolean)
	 * @see #getSerialDecision(SerialDecisionAspect)
	 */
	public static void endSerialMode() {
		if (serialActionDepth == 1) {
			for (int i = 0; i < serialDecisions.length; i++) {
				serialDecisions[i] = SerialDecisionStatus.INDIVIDUAL;
			}
		}
		if (serialActionDepth > 0) {
			serialActionDepth--;
		}
	}

	/**
	 * @return true if a serial action is going on such that serial decisions
	 * are relevant.
	 * @see #startSerialMode()
	 * @see #endSerialMode()
	 * @see #getSerialDecision(SerialDecisionAspect)
	 * @see #setSerialDecision(SerialDecisionAspect, boolean)
	 */
	public static boolean isInSerialMode()
	{
		return serialActionDepth > 0;
	}

	/**
	 * Returns the valid decision for the given {@code aspect} of the current
	 * serial action (INDIVIDUAL if there is no serial action context).
	 *
	 * @param aspect - one of the supported serial decision aspects
	 * @return the decision value
	 * @see #setSerialDecision(SerialDecisionAspect, boolean)
	 */
	public static SerialDecisionStatus getSerialDecision(SerialDecisionAspect aspect) {
		return serialDecisions[aspect.ordinal()];
	}

	/**
	 * Sets a general decision for all remaining files or other subjects for the
	 * given {@code aspect} of the current serial action (note that there is no
	 * way back to INDIVIDUAL here). Is ignored if there is no serial action
	 * context.
	 *
	 * @param aspect - one of the supported serial decision aspects
	 * @param statusAll - yes to all (true) or no to all (false)
	 * @see #getSerialDecision(SerialDecisionAspect)
	 */
	public static void setSerialDecision(SerialDecisionAspect aspect, boolean statusAll) {
		if (serialActionDepth > 0) {
			serialDecisions[aspect.ordinal()] = (statusAll ? SerialDecisionStatus.YES_TO_ALL : SerialDecisionStatus.NO_TO_ALL);
		}
	}
	// END KGU#534 2018-06-27

	// START KGU#2 2015-11-24 - KGU#280 2016-10-11 replaced by method consulting the Arranger class
	// Dependent Structorizer instances may otherwise be ignorant of the Arranger availability
	//public boolean isArrangerOpen = false;
	static public boolean isArrangerOpen() {
		return Arranger.hasInstance();
	}
	// END KGU#2 2015-11-24

	private JList<DetectedError> errorlist = null;
	// START KGU#705 2019-09-23: Enh. #738
	private JTextArea codePreview = null;
	// END KGU#705 2019-09-23

	// START KGU#368 2017-03-10: Enh. #376 - Allow copy and paste among Structorizer instances
	//private Element eCopy = null;
	static private Element eCopy = null;
	// END KGU#368 2017-03-10

	public File currentDirectory = new File(System.getProperty("user.home"));
	public File lastExportDir = null;
	// START KGU#354 2017-04-26: Enh. #354 also remember the last import folder
	public File lastCodeExportDir = null;
	public File lastCodeImportDir = null;
	public String lastImportFilter = "";
	// END KGU#354 2017-04-26
	// START KGU#602 2018-10-28: Enh. #619 - last used line length limitation (0 = never used)
	/**
	 * Last used value for interactive line length limitation (word wrapping)
	 */
	public int lastWordWrapLimit = 0;
	// END KGU#602 2018-10-28
	// START KGU#170 2016-04-01: Enh. #144 maintain a favourite export generator
	private String prefGeneratorName = "";
	// END KGU#170 2016-04-01
	// START KGU#354 2017-03-15: Enh. #354 CodeParser cache
	/**
	 * Cache of class instances implementing interface {@link CodeParser}
	 */
	private static Vector<CodeParser> parsers = null;
	/**
	 * The {@link GENPlugin}s held here provide parser-specific option
	 * specifications
	 */
	private static Vector<GENPlugin> parserPlugins = null;
	// END KGU#354 2017-03-15
	// START KGU#448 2018-01-05: Enh. #443
	// START KGU#911 2021-01-10: Enh. #910 We associate to all DiagramControllers an Includable
	/**
	 * Available {@link DiagramController}-implementing instances (including
	 * Turtleizer) combined with a representing Includable (except Turtleizer)
	 */
	//private static ArrayList<DiagramController> diagramControllers = null;
	private static LinkedHashMap<DiagramController, Root> diagramControllers = null;
	///** Bitset of enabled {@link DiagramController} instances */ 
	//private long enabledDiagramControllers = 0;
	// END KGU#911 2021-01-10
	// END KGU#448 2018-01-05

	// START KGU#300 2016-12-02: Enh. #300 - update notification settings
	// KGU#300 2017-03-15: turned static
	public static boolean retrieveVersion = false;
	// END KGU#300 2016-12-02
	// START KGU#305 2016-12-12: Enh. #305
	/**
	 * Indicates whether Arranger index is visible (diagram setting)
	 */
	private boolean show_ARRANGER_INDEX = false;	// Arranger index visible?
	// END KGU#305 2016-12-12
	// START KGU#705 2019-09-23: Enh. #738
	/**
	 * Indicates whether code preview is enabled (diagram setting)
	 */
	private boolean show_CODE_PREVIEW = false;		// Code Preview visible?
	/**
	 * Maps Elements to line number intervals of the current code preview
	 */
	private HashMap<Element, int[]> codePreviewMap = null;
	/**
	 * Highlight painter for the {@link #codePreview}
	 */
	private final HighlightPainter codeHighlightPainter =
			new DefaultHighlighter.DefaultHighlightPainter(Element.E_DRAWCOLOR);
	/**
	 * Highlight painter for executed elements in the {@link #codePreview}
	 */
	private final HighlightPainter execHighlightPainter =
			new DefaultHighlighter.DefaultHighlightPainter(Element.E_RUNNINGCOLOR);
	/**
	 * Caches the highlight manager for {@link #codePreview}
	 */
	private Highlighter codeHighlighter = null;
	// END KGU#705 2019-09-23

	// recently opened files
	protected Vector<String> recentFiles = new Vector<String>();

	// popup for comment
	/** The Label the comment popup consists of */
	private JLabel lblPop = new JLabel("",SwingConstants.CENTER);
	/** The popup for the comment */
	private JPopupMenu pop = new JPopupMenu();
	// START KGU#902 2021-01-01: Enh. #903
	/** The Element that most recently fed the {@link #lblPop} */
	private Element poppedElement = null;
	// END KGU#902 2021-01-01

	// toolbar management
	public Vector<MyToolbar> toolbars = new Vector<MyToolbar>();
	/** Toolbars that are to be disabled in simplified mode */
	public Vector<MyToolbar> expertToolbars = new Vector<MyToolbar>();

	private FindAndReplace findDialog = null;

	// START KGU#440 2017-11-06: Bugfix #455 - allow to suppress drawing on initialisation
	private boolean isInitialized = false;
	// END KGU#440 2017-11-06

	// START KGU#634 2019-01-17: Issue #664 - we need this distinction for saveAsNSD() in mode AUTO_SAVE_ON_CLOSE
	/**
	 * Flag allowing the saving methods to decide whether the application is
	 * going to close
	 */
	protected boolean isGoingToClose;
	// END KGU#634 2019-01-17

	// START KGU#654 2019-02-15: Enh. #681 - proposal mechanism for favourite generator
	protected int generatorProposalTrigger = 0;
	private String lastGeneratorName = null;
	private int generatorUseCount = 0;
	// END KGU#654 2019-02-15

	// STRT KGU#667 2019-02-26: Enh. #689
	/**
	 * Additional {@link Mainform} for editing of subroutines
	 */
	private Mainform subForm = null;
	// END KGU#667 2019-02-26

	/*========================================
	 * CONSTRUCTOR
	 *========================================*/
	/**
	 * Creates the Working area implementing most of the associated actions
	 *
	 * @param _editor - the associated {@link Editor} instance providing
	 * toolbar, popup menu, and more
	 * @param _name - name of the initial diagram to be presented
	 */
	public Diagram(Editor _editor, String _name) {
		super(true);
		this.setDoubleBuffered(true);	// we don't need double buffering, because the drawing
		// itself does it allready!
		this.setBackground(Color.LIGHT_GRAY);

		if (_editor != null) {
			errorlist = _editor.errorlist;
			// START KGU#705 2019-09-23: Enh. #738
			codePreview = _editor.txtCode;
			// END KGU#705 2019-09-23
			NSDControl = _editor;
		}
		create(_name);
	}

	// START KGU#48,KGU#49 2015-10-19: Make sure that replacing root by Arranger doesn't harm anything or risks losses
	/**
	 * @return the currently managed Root
	 */
	public Root getRoot() {
		return root;
	}

	/**
	 * Replaces the current diagram ({@link #root}) by the given {@code root}
	 * unless {@link Executor} is running or the user cancels the action on
	 * occasion of a requested decision about unsaved changes of the recently
	 * held {@link #root}.
	 *
	 * @param root - the {@link Root} to set
	 * @return false if the user refuses to adopt {@code root} or the current
	 * {@link Root} is being executed
	 */
	public boolean setRootIfNotRunning(Root root) {
		// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
		if (!this.checkRunning()) {
			return false;	// Don't proceed if the root is being executed
		}
		// END KGU#157 2016-03-16

		this.getParent().getParent().requestFocusInWindow();	// It's the JScrollPane (Editor.scrollaraea)
		return setRoot(root, true, true, true);
	}

	// START BOB 2019-11-24: Unimozer needs this method
	/**
	 * Replaces the current {@link Root} by the given {@code root} (if not null)
	 * and updates all depending GUI elements.<br/>
	 * CAUTION: Special interface for embedded use in Unimozer. Does not protect
	 * unsaved changes of the recently held {@link Root}!
	 *
	 * @param root - The new {@link Root} object (NSD) to be shown.
	 * @return true (no matter what happened)
	 * @see #setRoot(Root, boolean, boolean, boolean)
	 * @see #setRootIfNotRunning(Root)
	 */
	public boolean setRootForce(Root root) {
		if (root != null) {
			this.root = root;
			selected = root.findSelected();
			if (selected == null) {
				selected = root;
				root.setSelected(true);
			}
			redraw();
			analyse();
			this.updateCodePreview();
			doButtons();
			adaptScrollUnits();
		}
		return true;
	}
	// END BOB 2019-11-24

	// START KGU#430 2017-10-12: Issue #432 allow to set the root without immediate redrawing
	/**
	 * Replaces the current {@link Root} by the given {@code root} and updates
	 * all depending GUI elements.<br/>
	 * Should not be used while Executor is running the current Root - consider
	 * using {@link #setRootIfNotRunning(Root)} instead, which does a
	 * preliminary check.
	 *
	 * @param root - the new diagram root
	 * @param allowToSave - whether saving attempts for the recent {@link Root}
	 *     are allowed at all (should be by default - this is to avoid recursive
	 *     or repeated requests on recursive execution
	 * @param askToSave - in case the recent {@link Root} has unsaved changes
	 *     and {@code allowToSave} is {@code true}, ask to save it (or otherwise
	 *     simply save it without confirmation)?
	 * @param draw - If true then the work area will be redrawn
	 * @return {@code true} if {@code root} wasn't {@code null} and has properly replaced the
	 *     current diagram
	 *
	 * @see #setRootIfNotRunning(Root)
	 */
	//public boolean setRoot(Root root, boolean askToSave)
	public boolean setRoot(Root root, boolean allowToSave, boolean askToSave, boolean draw)
	// END KGU#430 2017-10-12
	{
		if (root != null) {
			// Save if something has been changed
			// START KGU#874 2020-10-18: Issue #875 Don't pester the user if root is arranged
			//if (!saveNSD(askToSave))
			// START KGU#874/KGU#893 2020-12-10: Bugfix #892 (#875 implementation mistake)
			//boolean isArranged = Arranger.hasInstance()
			//		&& Arranger.getInstance().getAllRoots().contains(root);
			boolean isArranged = Arranger.hasInstance()
					&& Arranger.getInstance().getAllRoots().contains(this.root);
			// END KGU#874/KGU#893 2020-12-10
			// START KGU#1032 2022-06-22: Bugfix #1038: Avoid recursive saving questions
			//if ((!askToSave || !isArranged) && !saveNSD(askToSave))
			if (allowToSave && (!askToSave || !isArranged) && !saveNSD(askToSave))
			// END KGU#1032 2022-06-22
			// END KGU#874 2020-10-18
			{
				// Abort this if the user cancels the save request
				return false;
			}
			this.unselectAll(draw);

			//boolean hil = root.highlightVars;
			this.root = root;
			//root.highlightVars = hil;
			//System.out.println(root.getFullText().getText());
			//root.getVarNames();
			//root.hasChanged = true;
			// START KGU#183 2016-04-23: Issue #169
			selected = root.findSelected();
			if (selected == null) {
				selected = root;
				root.setSelected(true);
			}
			// END KGU#183 2016-04-23
			if (draw) {
				redraw();
				analyse();
			}
			// START KGU#705 2019-09-23: Enh. #738
			this.updateCodePreview();
			// END KGU#705 2019-09-23
			// START KGU#149 2016-02-03: Bugfix #117
			doButtons();
			// END KGU#149 2016-02-03
			// START KGU#444 2017-10-23: Issue #417
			adaptScrollUnits();
			// END KGU#44 2017-10-23
		}
		return root != null;
	}
	// END KGU#48,KGU#49 2015-10-18

	/*
	// START KGU#2 2015-11-24: Allows the Executor to localize the Control frame
	public String getLang()
	{
		return NSDControl.getLang();
	}
	// END KGU#2 2015-11-24
	 */
	/**
	 * @return true if the Analyser is currently enabled, false otherwise
	 */
	public boolean getAnalyser() {
		return Element.E_ANALYSER;
	}

	private void create(String _string) {
		// load different things from INI-file
		Element.loadFromINI();
		CodeParser.loadFromINI();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		new FileDrop(this, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
				if (!checkRunning()) {
					return;	// Don't proceed if the root is being executed
				}
				// END KGU#157 2016-03-16
				// START KGU#392 2017-05-07: Enh. #399
				String unsuitedFiles = "";
				// END KGU#392 2017-05-07
				//boolean found = false;
				for (int i = 0; i < files.length; i++) {
					String filename = files[i].toString();
					// START KGU#671 2019-03-01: Issue #693 We can use the equivalent mechanism of openNsdOrArr() instead
					//String filenameLower = filename.toLowerCase();
					//if(filenameLower.endsWith(".nsd"))
					//{
					//	openNSD(filename);
					//}
					//// START KGU#289 2016-11-15: Enh. #290 (Arranger file support)
					//else if (filenameLower.endsWith(".arr")
					//		||
					//		filenameLower.endsWith(".arrz"))
					//{
					//	loadArrangement(files[i]);
					//}
					//// END KGU#289 2016-11-15
					//else {
					// If openNsdOrArr() doesn't recognise the file type then it returns an empty extension string
					// START KGU#868 2020-06-03: Bugfix #868 - filesdrop import is to be suppressed, too
					//if (openNsdOrArr(filename).isEmpty()) {
					if (openNsdOrArr(filename).isEmpty() && !NSDControl.isRestricted()) {
					// END KGU#868 2020-06-03
					// END KGU#671 2019-03-01
						Ini ini = Ini.getInstance();
						String charSet = ini.getProperty("impImportCharset", Charset.defaultCharset().name());
						// START KGU#354 2017-04-27: Enh. #354
						boolean isVerbose = ini.getProperty("impLogToDir", "false").equals("true");
						String logPath = null;
						if (isVerbose) {
							logPath = ini.getProperty("impLogDir", "");
						}
						// END KGU#354 2017-04-27
						// START KGU#354 2017-03-08: go over all the parser plugins
						CodeParser parser = null;
						File theFile = new File(filename);
						parser = findParserForFileExtension(theFile);
						if (parser != null) {
							// save (only if something has been changed)
							saveNSD(true);
							// START KGU#354 2017-04-27: Enh. #354
							if (isVerbose) {
								if (logPath.isEmpty()) {
									logPath = theFile.getParent();
								} else if (logPath.equals(".")) {
									if (currentDirectory != null) {
										if (!currentDirectory.isDirectory()) {
											logPath = currentDirectory.getParent();
										} else {
											logPath = currentDirectory.getPath();
										}
									}
								}
							}
							// END KGU#354 2017-04-27				
							// load and parse source-code
							// START KGU#354 2017-05-03: Enh. #354 - we needed more safety here
							String parserError = null;
							try {
							// END KGU#354 2017-05-03
								// START KGU#354 2017-05-12: Enh. #354 - we better use a new instance instead of statically sharing it
								parser = parser.getClass().getDeclaredConstructor().newInstance();
								// END KGU#354 2017-05-12
								// START KGU#395 2017-07-02: Enh. #357
								String pluginKey = parser.getClass().getSimpleName();
								for (int j = 0; j < parserPlugins.size(); j++) {
									// START KGU#948 2021-03-02: Bugfix #951 wrong index used
									//GENPlugin plug = parserPlugins.get(i);
									GENPlugin plug = parserPlugins.get(j);
									// END KGU#948 2021-03-02
									if (plug.getKey().equals(pluginKey)) {
										setPluginSpecificOptions(parser, plug.options);
										break;
									}
								}
								// END KGU#395 2017-07-02
								List<Root> newRoots = parser.parse(filename, charSet, logPath);
								if (parser.error.equals("")) {
									boolean arrange = false;
									// START KGU#1076 2023-09-12: Bugfix #1086 - for functionality a main should reside in work area
									Root newMain = null;
									// END KGU#1076 2023-09-12
									for (Root rootNew : newRoots) {
										if (arrange) {
											// START KGU#1076 2023-09-12: Bugfix #1086 - the group should be named after the file
											//arrangeNSD();
											arrangeNSD(theFile.getName());
											// END KGU#1076 2023-09-12
										}
										// START KGU#1076 2023-09-12: Bugfix #1086 - for functionality a main should reside in work area
										if (newMain == null && rootNew.isProgram()) {
											newMain = rootNew;
										}
										// END KGU#1076 2023-09-12
										// FIXME: Consider temporary shut-off of Arranger index notifications, see importCode()
										setRootIfNotRunning(rootNew);
										currentDirectory = new File(filename);
										arrange = true;
										//System.out.println(root.getFullText().getText());
									}
									// START KGU#354 2017-05-23: Enh.#354 - with many roots it's better to push the principal root to the Arranger, too
									// START KGU#1076 2023-09-12: Bugfix #1086 - This must already be done if it's more than ONE root.
									//if (newRoots.size() > 2 || !root.isProgram()) {
									//	arrangeNSD();
									//}
									if (newRoots.size() >= 2) {
										arrangeNSD(theFile.getName());
									}
									// END KGU#1076 2023-09-12
									// END KGU#354 2017-05-23
									for (Root rootNew : newRoots) {
										rootNew.setChanged(false);
									}
									// START KGU#1076 2023-09-12: Bugfix #1086 - for functionality a main should reside in work area
									if (newMain != null && !root.isProgram()) {
										setRootIfNotRunning(newMain);
									}
									// END KGU#1076 2023-09-12
																	} else {
							// START KGU#354 2017-05-03: Enh #354 Safety addition part 2
									parserError = parser.error;
								}
							} catch (Exception ex) {
								parserError = ex.toString();
								// START KGU#484 2018-04-05: Issue #463
								//ex.printStackTrace();
								logger.log(Level.WARNING, "Use of parser " + parser + " failed.", ex);
								// END KGU#484 2018-04-05
							}
							if (parserError != null)
							// END KGU#354 2017-05-03
							{
								// show error
								// START KGU#364 2017-03-09: Issues #182, #354 - Allow to copy the content
								//JOptionPane.showMessageDialog(null,
								//		parser.error,
								//		Menu.msgTitleParserError.getText(),
								//		JOptionPane.ERROR_MESSAGE);
								String[] options = {
										Menu.lblOk.getText(),
										Menu.lblCopyToClipBoard.getText()
								};
								int chosen = JOptionPane.showOptionDialog(null,
										// START KGU#354 2017-05-03: Enh. #354 - Safety addition part 3
										//parser.error,
										parserError,
										// END KGU#354 2017-05-03
										Menu.msgTitleParserError.getText(),
										JOptionPane.ERROR_MESSAGE,
										JOptionPane.YES_NO_OPTION,
										null, options, 0);
								if (chosen == 1) {
									Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
									StringSelection toClip = new StringSelection(parser.error);
									clipboard.setContents(toClip, null);
								}
							}
							redraw();
							// START KGU#354 2017-05-02: Enh. #354 file buttons hadn't been enabled properly  
							doButtons();
							// END KGU#354 2017-05-02

							Container cont = getParent();
							while (cont != null && !(cont instanceof JFrame)) {
								cont = cont.getParent();
							}
							if (cont != null) {
								((JFrame) cont).toFront();
							}
						}
						// START KGU#392 2017-05-07: Enh. #399: Gather unsuited files
						else {
							unsuitedFiles += "\n\u2022 " + filename;
						}
						// END KGU#392 2017-05-07

					}
					// END KGU#354 2017-03-08
				} // for (int i = 0; i < files.length; i++)
				// START KGU#392 2017-05-07: Enh. #399 Inform about unsuited files
				if (!unsuitedFiles.isEmpty()) {
					JOptionPane.showMessageDialog(null,
							Menu.msgUnsupportedFileFormat.getText().replace("%", unsuitedFiles),
							Menu.msgTitleLoadingError.getText(),
							JOptionPane.INFORMATION_MESSAGE);
				}
				// END KGU#392 2017-05-07
			}
		}
				);

		root.setText(StringList.getNew(_string));

		// START KGU#123 2016-01-04: Issue #65
		this.setAutoscrolls(true);
		// END KGU#123 2016--01-04

		// popup for comment
		JPanel jp = new JPanel();
		jp.setOpaque(true);
		lblPop.setPreferredSize(new Dimension(30, 12));
		jp.add(lblPop);
		pop.add(jp);

		// START KGU#182 2016-04-24: Issue #169
		selected = root;
		root.setSelected(true);
		// END KGU#182 2016-04-24

		// Attempt to find out what provokes the NullPointerExceptions on start
		//System.out.println("**** " + this + ".create() ready!");
	}

	// START KGU#354 2017-03-08: go over all the parser plugins
	private CodeParser findParserForFileExtension(File file) {
		CodeParser parser = null;
		this.retrieveParsers();
		for (int i = 0; i < parsers.size() && parser == null; i++) {
			if (parsers.get(i).accept(file)) {
				parser = parsers.get(i);
			}
		}

		return parser;
	}
	// END KGU#354 2017-03-08

	public void hideComments() {
		pop.setVisible(false);
		// START KGU#902 2021-01-01: Enh. #903 Make sure the pop info gets refreshed
		poppedElement = null;
		// END KGU#902 2021-01-01
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		//System.out.println("MouseMoved at (" + e.getX() + ", " + e.getY() + ")");
		// KGU#91 2015-12-04: Bugfix #39 - Disabled
		//if(Element.E_TOGGLETC) root.setSwitchTextAndComments(true);
		if (e.getSource() == this && NSDControl != null) {
			boolean popVisible = false;
			// START KGU#979 2021-06-10: Enh. #926, #979
			//if (Element.E_SHOWCOMMENTS && !((Editor) NSDControl).popup.isVisible())
			Element selEle = null;
			boolean popupDone = false;
			// Don't show an Analyser tooltip while the context menu (NSDControl.popup) is open
			if (Element.E_ANALYSER && Element.E_ANALYSER_MARKER && !((Editor) NSDControl).popup.isVisible()) {
				// Analyser tooltip has priority over comment tooltip
				if ((selEle = root.getElementByCoord(e.getX(), e.getY(), false)) != null) {
					// Check if the analyser marker region is hit
					Rect rectEl = selEle.getRectOffDrawPoint();
					Rect rectMk = selEle.getAnalyserMarkerBounds(rectEl, true);
					if (rectMk.contains(e.getPoint())) {
						// Now check whether the element has associated warnings
						HashMap<Element, Vector<DetectedError>> errorMap = selEle.getRelatedErrors(true);
						if (!errorMap.isEmpty()) {
							FontMetrics fm = lblPop.getFontMetrics(lblPop.getFont());
							int width = 0;
							int lines = 0;
							StringBuilder sb = new StringBuilder();
							sb.append("<html>");
							String text = "";
							for (Entry<Element, Vector<DetectedError>> entry: errorMap.entrySet()) {
								Element errEle = entry.getKey();
								// START KGU#1116 2024-03-07: Issue #1129: Restrict the popup lines
								//if (errorMap.size() > 1 || errEle != selEle) {
								if ((errorMap.size() > 1 || errEle != selEle) && lines < Element.E_ANALYSER_MAX_POPUP_LINES) {
								// END KGU#1116 2024-03-07
									// This is a collapsed element, i.e., potentially represents several elements
									text = ElementNames.getElementName(errEle, false, null);
									StringList elText = errEle.getText();
									String elText1 = "";
									if (elText.count() > 0) {
										elText1 = elText.get(0);
										if (elText.count() > 1) {
											elText1 += " ...";
										}
									}
									text += " (" + elText1 + ")";
									sb.append(BString.encodeToHtml(text));
									sb.append(":<br/>");
									width = Math.max(width, fm.stringWidth(text));
									lines++;
								}
								for (DetectedError err: entry.getValue()) {
									// START KGU#1116 2024-03-07: Issue #1129: Restrict the popup lines
									if (lines < Element.E_ANALYSER_MAX_POPUP_LINES) {
									// END KGU#1116 2024-03-07
										text = err.getMessage();
										if (err.isWarning()) {
											sb.append("<span style=\"color: #FF0000;\">");
										}
										else {
											sb.append("<span style=\"color: #0000FF;\">");
										}
										sb.append(BString.encodeToHtml(text));
										sb.append("</span><br/>");
										width = Math.max(width, fm.stringWidth(text));
									// START KGU#1116 2024-03-07: Issue #1129: Restrict the popup lines
									}
									// END KGU#1116 2024-03-07
									lines++;
								}
							}
							// START KGU#1116 2024-03-07: Issue #1129: Restrict the popup lines
							if (lines > Element.E_ANALYSER_MAX_POPUP_LINES) {
								sb.append("... (+ " + (lines - Element.E_ANALYSER_MAX_POPUP_LINES) + ")<br/>");
								lines = Element.E_ANALYSER_MAX_POPUP_LINES + 1;
							}
							// END KGU#1116 2024-03-07
							sb.append("</html>");
							lblPop.setText(sb.toString());
							lblPop.setPreferredSize(
									new Dimension(
											8 + width,
											lines * fm.getHeight()
											)
									);
							// Ensure that the comment tooltip isn't suppressed when the marker gets left
							poppedElement = null;
							int x = ((JComponent) e.getSource()).getLocationOnScreen().getLocation().x;
							int y = ((JComponent) e.getSource()).getLocationOnScreen().getLocation().y;
							pop.setLocation(x+e.getX(),
									y+e.getY()+16);
							popVisible = true;
							popupDone = true;
						}
					}
				}
				else {
					// No element hit, hence nothing else to do.
					popupDone = true;
				}
			}
			// Don't show a comment tooltip while the context menu (NSDControl.popup) is open
			if (!popupDone && Element.E_SHOWCOMMENTS && !((Editor) NSDControl).popup.isVisible())
			// END KGU#979 2021-06-10
			{
				//System.out.println("=================== MOUSE MOVED (" + e.getX()+ ", " +e.getY()+ ")======================");
				// START KGU#25 2015-10-11: Method merged with selectElementByCoord
				//Element selEle = root.getElementByCoord(e.getX(),e.getY());
				// START KGU#979 2021-06-11: Enh. #979 no need to retrieve twice
				//Element selEle = root.getElementByCoord(e.getX(), e.getY(), false);
				if (selEle == null) {
					selEle = root.getElementByCoord(e.getX(), e.getY(), false);
				}
				// END KGU#979 2021-06-11
				// END KGU#25 2015-10-11
				//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE MOVED >>>>> " + selEle + " <<<<<<<<<<<<<<<<<<<<<");

				if (selEle != null
						&& !selEle.getComment(false).getText().trim().isEmpty()) {
					// START KGU#902 2021-01-01: Enh. #903
					//// START KGU#199 2016-07-07: Enh. #188 - we must cope with combined comments now
					////StringList comment = selEle.getComment(false);
					//StringList comment = StringList.explode(selEle.getComment(false), "\n");
					//comment.removeAll("");	// Don't include empty lines here
					//// END KGU#199 2016-07-07
					//String htmlComment = "<html>" + BString.encodeToHtml(comment.getText()).replace("\n", "<br/>") + "</html>";
					//if(!lblPop.getText().equals(htmlComment))
					//{
					//	lblPop.setText(htmlComment);
					//}
					if (selEle != poppedElement) {
						StringBuilder sb = new StringBuilder();
						StringList comment = selEle.appendHtmlComment(sb);
						lblPop.setText(sb.toString());
						int maxWidth = 0;
						FontMetrics fm = lblPop.getFontMetrics(lblPop.getFont()); 
						for (int i = 0; i < comment.count(); i++)
						{
							String line = comment.get(i);
							maxWidth = Math.max(maxWidth, fm.stringWidth(line));
						}
						if (lblPop.getText().contains("<strong>")) {
							maxWidth *= 1.2;
						}
						lblPop.setPreferredSize(
								new Dimension(
										8 + maxWidth,
										comment.count() * lblPop.getFontMetrics(lblPop.getFont()).getHeight()
										)
								);
						poppedElement = selEle;
					}
					// END KGU#902 2021-01-01

					int x = ((JComponent) e.getSource()).getLocationOnScreen().getLocation().x;
					int y = ((JComponent) e.getSource()).getLocationOnScreen().getLocation().y;
					pop.setLocation(x + e.getX(),
							y + e.getY() + 16);
					popVisible = true;
				}
			}
			pop.setVisible(popVisible);
		}
		// KGU#91 2015-12-04: Bugfix #39 - Disabled
		//if(Element.E_TOGGLETC) root.setSwitchTextAndComments(false);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (e.getSource() == this) {
			// START KGU#123 2016-01-04: Issue #65 - added for autoscroll behaviour
			Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
			this.scrollRectToVisible(r);
			// END KGU#123 2016-01-04

			// START KGU#25 2015-10-11: Method merged with getElementByCoord(int,int)
			//Element bSome = root.selectElementByCoord(e.getX(),e.getY());
			Element bSome = root.getElementByCoord(e.getX(), e.getY(), true);
			// END KGU#25 2015-10-11

			if (bSome != null) {
				mX = e.getX();
				mY = e.getY();
				//System.out.println("DRAGGED "+mX+" "+mY);
				/*System.out.println("DRAGGED ("+e.getX()+", "+e.getY()+") >> " +
						bSome + " >> " + selectedDown);
						/**/
				// START KGU#896 2020-12-25: Enh. 896
				if (getCursor().getType() != Cursor.MOVE_CURSOR) {
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
				}
				// END KGU#896 2020-12-25
				bSome.setSelected(true);
				//System.out.println("selected = " + bSome);
				//System.out.println("selectedDown = " + selectedDown);
				//System.out.println("selectedUp = " + selectedUp);
				if (selectedDown != null) {
					selectedDown.setSelected(true);
				}

				boolean doRedraw = false;

				if ((selectedDown != null) && (e.getX() != mouseX) && (e.getY() != mouseY) && (selectedMoved != bSome)) {
					mouseMove = true;
					if (selectedDown.getClass().getSimpleName().equals("Root")
							|| selectedDown.getClass().getSimpleName().equals("Subqueue")
							|| bSome.getClass().getSimpleName().equals("Root")
							// START KGU#911 2021-01-10: Enh. #910
							|| selectedDown.isImmutable() || bSome.isImmutable()
							// END KGU#911 2021-01-10
							//root.checkChild(bSome, selectedDown))
							|| bSome.isDescendantOf(selectedDown)) {
						Element.E_DRAWCOLOR = Color.RED;
					} else {
						Element.E_DRAWCOLOR = Color.GREEN;
					}
					/*
					 selectedDown.draw(new Canvas((Graphics2D)this.getGraphics()), selectedDown.rect);
					 if(bSome!=null)
					 {
					 bSome.draw(new Canvas((Graphics2D)this.getGraphics()), bSome.rect);

					 }
					 */
					doRedraw = true;
				}

				if (selX != -1 && selY != -1) {
					doRedraw = true;
				}

				if (doRedraw) {
					redraw();
				}

			}
			selectedMoved = bSome;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//System.out.println("MousePressed at (" + e.getX() + ", " + e.getY() + ")");
		if (e.getSource() == this) {
			// START KGU#705 2019-09-26: Enh. #738: The focus wasn't automatically gained on clicking into the diagram
			this.getParent().getParent().requestFocus();
			// END KGU#705 2019-09-26
			//System.out.println("Pressed");
			mouseX = e.getX();
			mouseY = e.getY();

			Element.E_DRAWCOLOR = Color.YELLOW;
			// START KGU#25 2015-10-11: Method merged with getElementByCoord(int,int)
			//Element ele = root.selectElementByCoord(e.getX(),e.getY());
			Element ele = root.getElementByCoord(e.getX(), e.getY(), true);
			// END KGU#25 2015-10-11

			// START KGU#896 2020-12-25: Enh. #896
			boolean setMoveCursor = false;
			// END KGU#896 2020-12-25

			// KGU#87: Maintain a selected sequence on right mouse button click 
			if (e.getButton() == MouseEvent.BUTTON3 && selected instanceof IElementSequence
					&& (ele == null || ((IElementSequence) selected).getIndexOf(ele) >= 0)) {
				// Restore the selection flags (which have been reduced to ele by root.getElementByCoord(...))
				// START KGU 2016-01-09: Bugfix #97 (possibly) - ele may be null here!
				//ele.setSelected(false);
				if (ele != null) {
					ele.setSelected(false);
				}
				// END KGU 2016-01-09
				selected = selected.setSelected(true);
				redraw();
			} else if (ele != null) {
				// START KGU#896 2020-12-25: Enh. #896
				setMoveCursor = ele != null && getCursor().getType() != Cursor.MOVE_CURSOR;
				// END KGU#896 2020-12-25
				// START KGU#136 2016-03-02: Bugfix #97 - Selection wasn't reliable
				ele = ele.setSelected(true);
				// END KGU#136 2016-03-02
				mX = mouseX;
				mY = mouseY;
				// START KGU#136 2016-03-02: Bugfix #97 - we must get the element corner
				//selX = mouseX-ele.getRect().left;
				//selY = mouseY-ele.getRect().top;
				Rect topLeft = ele.getRectOffDrawPoint();
				selX = mouseX - topLeft.left;
				selY = mouseY - topLeft.top;
				// END KGU#136 2016-03-02

				// KGU#87: Expansion to entire subqueue (induced by Alt key held down)?
				if (e.isAltDown() && ele.parent instanceof Subqueue
						&& ((Subqueue) ele.parent).getSize() > 1) {
					((Subqueue) ele.parent).setSelected(true);
					selected = ele.parent;
					// In case someone wants to drag then let it just be done for the single element
					// (we don't allow dynamically to move a sequence - the user may better cut and paste)
					selectedDown = ele;
					// START KGU#896 2020-12-25: Enh. #896
					setMoveCursor = false;	// So we will not show the MOVE cursor here
					// END KGU#896 2020-12-25
					redraw();
				} else if (ele != selected) {
					// START KGU#87 2015-11-23: If an entire Subqueue had been selected, reset the flags 
					if (selected instanceof Subqueue) {
						selected.setSelected(false);
					}
					if (e.isShiftDown() && selected != null
							&& ele.parent instanceof Subqueue
							&& ele.parent == selected.parent) {
						// Select the subrange
						//System.out.println("Selected range of " + ele.parent + " " +
						//((Subqueue)ele.parent).getIndexOf(ele) + " - " +
						//((Subqueue)ele.parent).getIndexOf(selected));
						selected.setSelected(false);
						selected = new SelectedSequence(selected, ele);
						// START KGU#866 2020-05-02: Issue #866 span may get reduced now
						if (((SelectedSequence) selected).getSize() == 1) {
							// Replace the span by its only member
							selected = ((SelectedSequence) selected).getElement(0);
						}
						// END KGU#866 2020-05-02
						// START KGU#896 2020-12-25: Enh. #896
						else {
							setMoveCursor = false;
						}
						// END KGU#896 2020-12-25
						selected.setSelected(true);
						redraw();
						selectedDown = ele;
					} else {
					// END KGU#87 2015-11-23
						ele.setSelected(true);
						// START KGU#87 2015-11-23: Ensure a redrawing after a Subqueue had been selected 
						//selected=ele;
						//if(selectedDown!=ele) 
						if (selectedDown != ele || selected instanceof IElementSequence)
						// END KGU#87 2015-11-23
						{
							redraw();
						}
						selected = ele;
						selectedDown = ele;
					// START KGU#87 2015-11-23: Original code just part of the else branch
					}
					// END KGU#87 2015-11-23
				}
				//redraw();
				// START KGU#926 2021-02-04: Enh. #926
				scrollErrorListToSelected();
				// END KGU#926 2021-02-04
			}
			// START KGU#180 2016-04-15: Bugfix #165 - detection didn't work properly
			else /* ele == null */ {
				selected = null;
				// FIXME: May selectedDown and selectedUp still hold a former selection? 
				redraw();
			}
			// END KGU#180 2016-04-15

			if (selected != null) {
				if (!selected.getClass().getSimpleName().equals("Subqueue")
						&& !selected.getClass().getSimpleName().equals("Root")) {
					mouseMove = false;
					// START KGU#896 2020-12-25: Enh. #896
					if (selected != ele) {
						setMoveCursor = false;
					}
					// END KGU#896 2020-12-25
				}
				// START KGU#896 2020-12-25: Enh. #896
				else {
					// Never show the MOVE cursor on a Root or Subqueue
					setMoveCursor = false;
				}
				// END KGU#896 2020-12-25
				// START KGU#911 2021-01-10: Enh. #910
				if (selected.isImmutable()) {
					setMoveCursor = false;
				}
				// END KGU#911 2021-01-10
			}
			// START KGU#896 2020-12-25: Enh. #896
			if (setMoveCursor) {
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}
			// END KGU#896 2020-12-25

			// START KGU#705 2019-09-24: Enh. #738
			highlightCodeForSelection();
			// END KGU#705 2019-09-24
			if (NSDControl != null) {
				NSDControl.doButtons();
			}
		}
	}

	// START KGU#926 2021-02-04: Enh. #926
	/**
	 * Scrolls the errorlist to its first entry that is related to an element of
	 * the selection set, if {@link #selected} is not {@code null} and Analyser
	 * mode is active
	 */
	private void scrollErrorListToSelected() {
		if (selected != null && Element.E_ANALYSER) {
			HashMap<Element, Vector<DetectedError>> errorMap
			= selected.getRelatedErrors(false);
			// The errorMap will not contain more than one DetectedError object
			for (Vector<DetectedError> relatedErrors : errorMap.values()) {
				DetectedError err = relatedErrors.firstElement();
				int ix = root.errors.indexOf(err);
				if (ix >= 0) {
					errorlist.ensureIndexIsVisible(ix);
				}
			}
		}
	}
	// END KGU#926 2021-02-04

	@Override
	public void mouseReleased(MouseEvent e) {
		// FIXME: What about hidden declarations?
		if (e.getSource() == this) {
			//System.out.println("Released");
			boolean doDraw = false;
			// START KGU#514 2018-04-03: Superfluous Analyser calls reduced on occasion of bugfix #528 
			boolean doReanalyse = false;
			// END KGU#514 2018-04-03
			boolean isShiftDown = e.isShiftDown();

			if ((selX != -1) && (selY != -1) && (selectedDown != null)) {
				selX = -1;
				selY = -1;
				doDraw = true;
			}

			if ((mouseMove == true) && (selectedDown != null)) {
				Element.E_DRAWCOLOR = Color.YELLOW;
				if (!selectedDown.getClass().getSimpleName().equals("Subqueue")
						&& !selectedDown.getClass().getSimpleName().equals("Root")) {
					//System.out.println("=================== MOUSE RELEASED 1 (" + e.getX()+ ", " +e.getY()+ ")======================");
					// START KGU#25 2015-10-11: Method merged with getElementByCoord(int,int)
					//selectedUp = root.selectElementByCoord(e.getX(),e.getY());
					Element selectedUp = root.getElementByCoord(e.getX(), e.getY(), true);
					// END KGU#25 2015-10-11
					//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE RELEASED 1 >>>>>>> " + selectedUp + " <<<<<<<<<<<<<<<<<<<<<<");
					if (selectedUp != null) {
						selectedUp.setSelected(false);
						if (!selectedUp.getClass().getSimpleName().equals("Root")
								&& selectedUp != selectedDown
								// START KGU#911 2021-01-10: Enh. #910
								&& !selectedUp.isImmutable() && !selectedDown.isImmutable()
								// END KGU#911 2021-01-10
								//root.checkChild(selectedUp,selectedDown)==false
								&& !selectedUp.isDescendantOf(selectedDown)) {
							//root.addUndo();
							try {
								addUndoNSD(false);
							} catch (CancelledException ex) {
								return;
							}
							NSDControl.doButtons();
							// START KGU#514 2018-04-03: Superfluous Analyser calls reduced on occasion of bugfix #528 
							doReanalyse = true;
							// END KGU#514 2018-04-03

							// START KGU401 2017-05-17: Issue #405
							selectedDown.resetDrawingInfoDown();
							// END KGU#401 2017-05-17

							// START KGU#87 2015-11-22: Subqueues should never be moved but better prevent...
							//root.removeElement(selectedDown);
							if (!(selectedDown instanceof Subqueue)) {
								root.removeElement(selectedDown);
							}
							// END KGU#87 2015-11-22
							selectedUp.setSelected(false);

							// START KGU#896 2020-12-25: Enh. #896 - eventually allow to drop before the target
							//root.addAfter(selectedUp, selectedDown);
							if (isShiftDown) {
								root.addBefore(selectedUp, selectedDown);
							} else {
								root.addAfter(selectedUp, selectedDown);
							}
							// END KGU#896 2020-12-25

							// START KGU'87 2015-11-22: See above
							//selectedDown.setSelected(true);
							if (!(selectedDown instanceof Subqueue)) {
								selectedDown.setSelected(true);
							} else {
								((Subqueue) selectedDown).clear();
								selectedDown.setSelected(false);
							}
							// END KGU#87 2015-11-22
							doDraw = true;
							// START KGU#705 2019-09-30: Enh. #738
							updateCodePreview();
							// END KGU#705 2019-09-30
						} else {
							selectedUp.setSelected(false);
							selectedDown.setSelected(true);
							doDraw = true;
						}
					}
				} else {
					//System.out.println("=================== MOUSE RELEASED 2 (" + e.getX()+ ", " +e.getY()+ ")======================");
					// START KGU#25 2015-10-11: Method merged with getElementByCoord(int,int)
					//selectedUp = root.selectElementByCoord(e.getX(),e.getY());
					Element selectedUp = root.getElementByCoord(e.getX(), e.getY(), true);
					// END KGU#25 2015-10-11
					//System.out.println(">>>>>>>>>>>>>>>>>>> MOUSE RELEASED 2 >>>>>>> " + selectedUp + " <<<<<<<<<<<<<<<<<<<<<<");
					if (selectedUp != null) {
						selectedUp.setSelected(false);
					}
					doDraw = true;
				}
			}

			mouseMove = false;
			// START KGU#896 2020-12-25: Enh. #986
			if (getCursor().getType() != Cursor.DEFAULT_CURSOR) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			// END KGU#896 2020-12-25

			if (doDraw) {
				redraw();
				// START KGU#514 2018-04-03: Superfluous Analyser calls reduced on occasion of bugfix #528
				//analyse();
				if (doReanalyse) {
					analyse();
				}
				// END KGU#514 2018-04-03
			}

			if (NSDControl != null) {
				NSDControl.doButtons();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// START KGU#1 2015-10-11: We ought to get rid of that sticky popped comment!
		this.hideComments();
		// END KGU#1 2015-10-11
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//System.out.println("MouseClicked at (" + e.getX() + ", " + e.getY() + ")");
		// select the element
		if (e.getClickCount() == 1) {
			// START KGU#565 2018-07-27: Bugfix #569 We must react to a click in the errorlist if it contains only a single entry 
			//if (e.getSource()==this)
			//{
			//}
			if (e.getSource() == errorlist) {
				this.handleErrorListSelection();
			}
			// END KGU#565 2018-07-27
			// START KGU#305 2016-12-12: Enh. #305
			// START KGU#626 2019-01-01: Enh. #657
			//else if (e.getSource() == diagramIndex)
			//{
			//	Arranger.scrollToDiagram(diagramIndex.getSelectedValue(), true);
			//}
			// END KGU#626 2019-01-01
			// END KGU#305 2016-12-12
		} // edit the element
		else if ((e.getClickCount() == 2)) {
			if (e.getSource() == this) {
				// selected the right element
				//selected = root.selectElementByCoord(e.getX(),e.getY());
				// START KGU#87 2015-11-22: Don't edit non-empty Subqueues, reselect single element
				//if (selected != null)
//				if ((selected instanceof Subqueue) && ((Subqueue)selected).getSize() > 0)
//				{
//					selected = root.selectElementByCoord(e.getX(), e.getY());	// Is of little effect - often subqueues don't detect properly
//					redraw();
//					//System.out.println("Re-selected on double-click: " + selected + ((selected instanceof Subqueue) ? ((Subqueue)selected).getSize() : ""));
//				}
				// START KGU#143 2016-11-17: Issue #114 - don't edit elements under execution
				//if (selected != null && !((selected instanceof Subqueue) && ((Subqueue)selected).getSize() > 0))
				if (canEdit())
				// END KGU#143 2016-11-17
				// END KGU#87 2015-11-22
				{
					// edit it
					editNSD();
					selected.setSelected(true);
					// START KGU#276 2016-10-11: Issue #269 Attempt to focus the associated element - failed!
					//redraw();
					redraw(selected);	// Doesn't work properly
					// END KGU#276 2016-10-11
					// do the button thing
					if (NSDControl != null) {
						NSDControl.doButtons();
					}
					// START KGU#705 2019-09-24: Enh. #738
					highlightCodeForSelection();
					// END KGU#705 2019-09-24
				}
			} else if (e.getSource() == errorlist) {
				// the error list has been clicked
				if (errorlist.getSelectedIndex() >= 0) {
					// select the concerned element
					// START KGU#565 2021-01-06: Bugfix #569 - improvement
					//// START KGU#565 2018-07-27: Bugfix #569 - We must first unselect the previous selection
					//selected = (root.errors.get(errorlist.getSelectedIndex())).getElement();
					//Element errElem = (root.errors.get(errorlist.getSelectedIndex())).getElement();
					//if (selected != null && errElem != selected) {
					//	selected.setSelected(false);
					//	selected = errElem.setSelected(true);
					//	// START KGU#705 2019-09-24: Enh. #738
					//	highlightCodeForSelection();
					//	// END KGU#705 2019-09-24
					//}
					//// END KGU#565 2018-07-27
					//// edit it
					//editNSD();
					if (this.handleErrorListSelection()) {
						editNSD();
					}
					// END KGU#565 2021-01-06
					// do the button things
					if (NSDControl != null) {
						NSDControl.doButtons();
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// START KGU#699 2019-03-27: Enh. #717 This is for convenience in configureWheelUnit()
		if (e.getSource() instanceof JSpinner) {
			SpinnerNumberModel model = (SpinnerNumberModel) ((JSpinner) e.getSource()).getModel();
			int rotation = e.getWheelRotation();
			Object value = null;
			if (rotation < 0 && (value = model.getNextValue()) != null) {
				model.setValue(value);
			} else if (rotation > 0 && (value = model.getPreviousValue()) != null) {
				model.setValue(value);
			}
			return;
		}
		// END KGU#699 2019-03-27
		//System.out.println("MouseWheelMoved at (" + e.getX() + ", " + e.getY() + ")");
		//System.out.println("MouseWheelEvent: " + e.getModifiers() + " Rotation = " + e.getWheelRotation() + " Type = " + 
		//		((e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) ? ("UNIT " + e.getScrollAmount()) : "BLOCK")  );
		// START KGU#503 2018-03-13: Enh. #519 - The mouse wheel got a new function and is permanently listened to
		//if (selected != null)
		if ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) != 0) {
			// Ctrl + mouse wheel is now to raise or shrink the font (thus to kind of zoom) 
			int rotation = e.getWheelRotation();
			int fontSize = Element.getFont().getSize();
			if (Element.E_WHEEL_REVERSE_ZOOM) {
				rotation *= -1;
			}
			if (rotation >= 1 && fontSize - 1 >= 4) {
				// reduce font size
				Element.setFont(new Font(Element.getFont().getFamily(), Font.PLAIN, fontSize - 1));
				root.resetDrawingInfoDown();
				redraw();
				e.consume();
			} else if (rotation <= -1) {
				// enlarge font size
				Element.setFont(new Font(Element.getFont().getFamily(), Font.PLAIN, fontSize + 1));
				root.resetDrawingInfoDown();
				redraw();
				e.consume();
			}
		} else if (Element.E_WHEELCOLLAPSE && selected != null)
		// END KGU#503 2018-03-13
		{
			// START KGU#123 2016-01-04: Bugfix #65 - heavy differences between Windows and Linux here:
			// In Windows, the rotation result may be arbitrarily large whereas the scrollAmount is usually 1.
			// In Linux, however, the rotation result will usually be -1 or +1, whereas the scroll amount is 3.
			// So we just multiply both and will get a sensible threshold, we hope.
			//if(e.getWheelRotation()<-1) selected.setCollapsed(true);
			//else if(e.getWheelRotation()>1)  selected.setCollapsed(false);
			int rotation = e.getWheelRotation();
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				rotation *= e.getScrollAmount();
			} else {
				rotation *= 2;
			}
			if (rotation < -1) {
				selected.setCollapsed(true);
			} else if (rotation > 1) {
				selected.setCollapsed(false);
			}
			// END KGU#123 2016-01-04
			// START KGU#503 2018-03-13: Enh. #519 - may not work (depends on the order of listeners)
			e.consume();
			// END KGU#503 2018-03-13
			redraw();
		}
		// FIXME KGU 2016-01-0: Issue #65
//		// Rough approach to test horizontal scrollability - only works near the left and right
//		// borders, because the last mouseMoved position is used. Seems that we will have to
//		// maintain a virtual scroll position here which is to be used instead of e.getX().
//		if ((e.getModifiersEx() & MouseWheelEvent.SHIFT_DOWN_MASK) != 0)
//		{
//			int rotation = e.getWheelRotation();
//			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
//				rotation *= e.getScrollAmount();
//			}
//			System.out.println("Horizontal scrolling by " + rotation);
//			Rectangle r = new Rectangle(e.getX() + 50 * rotation, e.getY(), 1, 1);
//			((JPanel)e.getSource()).scrollRectToVisible(r);
//		}
	}

	// START KGU#143 2016-01-21: Bugfix #114 - We need a possibility to update buttons from execution status
	public void doButtons() {
		if (NSDControl != null) {
			NSDControl.doButtons();
		}
	}
	// END KGU#143 2016-01-21

	// START KGU#276 2016-10-09: Issue #269
	/**
	 * Scroll to the given element and redraw the current diagram
	 *
	 * @param element - the element to gain the focus
	 */
	public void redraw(Element element) {
		Rectangle rect = element.getRectOffDrawPoint().getRectangle();
		Rectangle visibleRect = new Rectangle();
		this.computeVisibleRect(visibleRect);
		// START KGU#276 2016-11-19: Issue #269 Ensure wide elements be shown left-bound
		if (rect.width > visibleRect.width
				&& !(element instanceof Alternative || element instanceof Case)) {
			rect.width = visibleRect.width;
		}
		// END KGU#276 2016-11-19
		// START KGU#276 2016-11-21: Issue #269 Ensure high elements be shown top-bound
		if (rect.height > visibleRect.height
				&& !(element instanceof Instruction || element instanceof Parallel || element instanceof Forever)) {
			// ... except for REPEAT loops, which are to be shown bottom-aligned
			if (element instanceof Repeat) {
				rect.y += rect.height - visibleRect.height;
			}
			rect.height = visibleRect.height;
		}
		// END KGU#276 2016-11-21
		try {
			scrollRectToVisible(rect);
		} catch (Exception ex) {
			logger.warning(ex.toString());
		}
		redraw();	// This is to make sure the drawing rectangles are correct
		// START KGU#705 2019-09-24: Enh. 738
		if (show_CODE_PREVIEW && codeHighlighter != null && element.executed) {
			// START KGU#978 2021-06-09: Workaround for mysterious bug #977
			try {
			// END KGU#978 2021-06-09
				codeHighlighter.removeAllHighlights();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						highlightCodeForElement(element, true);
					}
				});
			// START KGU#978 2021-06-09: Workaround for mysterious bug #977
			}
			catch (NullPointerException ex) {
				logger.log(Level.CONFIG, "Strange error #977 in code preview", ex);
			}
			// END KGU#978 2021-06-09
		}
		// END KGU#705 2019-09-24
	}
	// END KGU#276 2016-10-09

	public void redraw() {
		// START KGU#440 2017-11-06: Bugfix #455 - suppress drawing unless Structorizer is fully initialized
		if (!this.isInitialized) {
			return;
		}
		// END KGU#440 2017-11-06
		boolean wasHighLight = Element.E_VARHIGHLIGHT;
		if (wasHighLight) {
			// START KGU#430 2017-10-10: Issue #432
			//root.getVarNames();
			try {
				// START KGU#444/KGU#618 2018-12-18: Issue #417, #649
				//root.getVarNames();
				root.getVarNames();
				// END KGU#444/KGU#618 2018-12-18
			} catch (Exception ex) {
				logger.log(Level.WARNING, "*** Possible sync problem:", ex);
				// Avoid trouble (highlighting would require variable retrieval)
				Element.E_VARHIGHLIGHT = false;
			}
			// END KGU#430 2017-10-10
		}

		Rect rect = root.prepareDraw(this.getGraphics());
		Dimension d = new Dimension(rect.right - rect.left, rect.bottom - rect.top);
		this.setPreferredSize(d);
		//this.setSize(d);
		this.setMaximumSize(d);
		this.setMinimumSize(d);
		//this.setSize(new Dimension(rect.right-rect.left,rect.bottom-rect.top));
		//this.validate();

		((JViewport) this.getParent()).revalidate();

		//redraw(this.getGraphics());
		this.repaint();

		// START KGU#430 2017-10-10: Issue #432
		Element.E_VARHIGHLIGHT = wasHighLight;
		// END KGU#430 2017-10-10
	}

	// START KGU#703 219-03-30: Issue #718, #720
	/**
	 * Resets cached variable and type information, the drawing information
	 * including the highlight cache after routine pool changes and redraws the
	 * managed diagram. The clearing of variables, types etc. is not done if the
	 * {@link Root} is under execution.
	 */
	public void invalidateAndRedraw() {
		// During execution it is no good idea to reset variable, constants, and type information.
		// Anyway the pool changes will usually only be a pseudo addition in order to get ownership
		// of executed subroutines, so better ignore it.
		// Otherwise, of course, we should react to a possible insertion or removal of some referred
		// includable, and even subroutines becoming available may have an impact on derived types
		// (result types), even recursively.
		if (!root.isExecuted()) {
			root.clearVarAndTypeInfo(false);
		}
		// START KGU#874 2020-10-18: Issue #875 Particularly the save buttons must be updated
		this.doButtons();
		// END KGU#874 2020-10-18
		redraw();
	}
	// END KGU#703 2019-03-30

	public void redraw(Graphics _g) {
	// START KGU#906 2021-01-06: Enh. #905 - we needed to distinguish work area from export
		redraw(_g, DrawingContext.DC_STRUCTORIZER);
	}

	public void redraw(Graphics _g, DrawingContext _context)
	// END KGU#906 2021-01-06
	{
		// KGU#91 2015-12-04: Bugfix #39 - Disabled
		//if (Element.E_TOGGLETC) root.setSwitchTextAndComments(true);
		// START KGU#502/KGU#524/KGU#553: 2019-03-29: Issues #518, #544, #557 drawing speed
		//root.draw(_g, ((JViewport)this.getParent()).getViewRect());
		Rectangle clipRect = _g.getClipBounds();
		// START KGU#906 2021-01-06: Enh. #905
		//root.draw(_g, clipRect);
		root.draw(_g, clipRect, _context);
		// END KGU#906 2021-01-06
		// END KGU#502/KGU#524/KGU#553

		lu.fisch.graphics.Canvas canvas = new lu.fisch.graphics.Canvas((Graphics2D) _g);
		Rect rect;
		// draw dragged element
		if ((selX != -1) && (selY != -1) && (selectedDown != null) && (mX != mouseX) && (mY != mouseY)) {
			_g.setColor(Color.BLACK);
			// START KGU#136 2016-03-02: Bugfix #97 - It must not play any role where the diagram was drawn before
			//rect = selectedDown.getRect();
			//Rect copyRect = rect.copy();
			rect = selectedDown.getRectOffDrawPoint();
			// END KGU#136 2016-03-02
			int w = rect.right - rect.left;
			int h = rect.bottom - rect.top;
			rect.left = mX - selX;
			rect.top = mY - selY;
			rect.right = rect.left + w;
			rect.bottom = rect.top + h;
			((Graphics2D) _g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			// START KGU#502/KGU#524/KGU#553: 2019-03-29: Issues #518, #544, #557 drawing speed
			//selectedDown.draw(canvas, rect, ((JViewport)this.getParent()).getViewRect(), false);
			selectedDown.draw(canvas, rect, clipRect, false);
			// START KGU#502/KGU#524/KGU#553: 2019-03-29: Issues #518, #544, #557
			((Graphics2D) _g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			// START KGU#136 2016-03-01: Bugfix #97 - this is no longer necessary
			//selectedDown.rect = copyRect;
			// END KGU#136 2016-03-01
			//System.out.println(selectedDown.getClass().getSimpleName()+"("+selectedDown.getText().getLongString()+
			//		") repositioned to ("+copyRect.left+", "+copyRect.top+")");
			//_g.drawRect(mX-selX, mY-selY, w, h);
		}/**/

		// KGU#91 2015-12-04: Bugfix #39 - Disabled
		//if (Element.E_TOGGLETC) root.setSwitchTextAndComments(false);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (root != null) {
			//logger.debug("Diagram: " + System.currentTimeMillis());
			redraw(g, DrawingContext.DC_STRUCTORIZER);
		}
	}

	// START KGU#444 2017-10-23: Issue #417 - polynomial scrolling time complexity 
	/**
	 * Adapts the scroll units according to the size of the current
	 * {@link Root}. With standard scroll unit of 1, large diagrams would take
	 * an eternity to get scrolled over because their redrawing time also
	 * increases with the number of elements, of course, such that it's
	 * polynomial (at least square) time growth...
	 */
	protected void adaptScrollUnits() {
		Container parent = this.getParent();
		if (parent != null && (parent = parent.getParent()) instanceof javax.swing.JScrollPane) {
			javax.swing.JScrollPane scroll = (javax.swing.JScrollPane) parent;
			// START KGU#444 2017-11-03: Bugfix #417 - in rare cases a division by 0 exception could occur
			//int heightFactor = root.getRect().bottom / scroll.getHeight() + 1;
			//int widthFactor = root.getRect().right / scroll.getWidth() + 1;
			int heightFactor = 1;
			int widthFactor = 1;
			Rect drawRect = root.getRect();
			//System.out.println("rect : " + drawRect);
			// START KGU#444/KGU#618 2018-12-18: Issue #417, #649 - scrolling too slow
			// For new Roots, the drawing may not have had time to compute the size, so try an
			// estimate from the total number of elements
			if (drawRect.bottom < 10) {
				int nElements = root.getElementCount();
				drawRect = new Rect(0, 0,
						(int) (Math.sqrt(nElements) * Element.getPadding()),
						nElements * 3 * Element.getPadding()
						);
			}
			// END KGU#444/KGU#618 2018-12-18
			if (scroll.getHeight() > 0) {
				heightFactor = drawRect.bottom / scroll.getHeight() + 1;
			}
			if (scroll.getWidth() > 0) {
				widthFactor = drawRect.right / scroll.getWidth() + 1;
			}
			// END KGU#444 2017-11-03
			//System.out.println("unit factors: " + widthFactor + " / " + heightFactor);
			// START KGU#699 2019-03-27: Issue #717
			//scroll.getHorizontalScrollBar().setUnitIncrement(widthFactor);
			//scroll.getVerticalScrollBar().setUnitIncrement(heightFactor);
			if (Element.E_WHEEL_SCROLL_UNIT <= 0) {
				// The very first time Structorizer is used, we fetch the original unit increment
				Element.E_WHEEL_SCROLL_UNIT = scroll.getVerticalScrollBar().getUnitIncrement();
			}
			scroll.getHorizontalScrollBar().setUnitIncrement(Element.E_WHEEL_SCROLL_UNIT + widthFactor - 1);
			scroll.getVerticalScrollBar().setUnitIncrement(Element.E_WHEEL_SCROLL_UNIT + heightFactor - 1);
			// END KGU#699 2019-03-27
		}
	}
	// END KGU#444 2017-10-23

	// START KGU#155 2016-03-08: Some additional fixing for bugfix #97
	/**
	 * Invalidates the cached prepareDraw info of the current diagram (Root) (to
	 * be called on events with global impact on the size or shape of Elements)
	 */
	public void resetDrawingInfo() {
		root.resetDrawingInfoDown();
		// START KGU#902 2021-01-01: Enh. #903
		poppedElement = null;
		// END KGU#902 2021-01-01
		if (isArrangerOpen()) {
			Arranger.getInstance().resetDrawingInfo(this.hashCode());
		}
	}
	// END KGU#155 2016-03-08

	public Element getSelected() {
		return selected;
	}

	// START KGU#477 2017-12-07: Enh. #487
	public Element getFirstSelected() {
		if (selected instanceof IElementSequence && ((IElementSequence) selected).getSize() > 0) {
			return ((IElementSequence) selected).getElement(0);
		}
		return selected;
	}

	public Element getLastSelected() {
		if (selected instanceof IElementSequence && ((IElementSequence) selected).getSize() > 0) {
			return ((IElementSequence) selected).getElement(((IElementSequence) selected).getSize() - 1);
		}
		return selected;
	}
	// END KGU#477 2017-12-07

	// START KGU#87 2015-11-22: 
	public boolean selectedIsMultiple() {
		return (selected instanceof IElementSequence && ((IElementSequence) selected).getSize() > 0);
	}
	// END KGU#87 2015-11-22

	// START KGU#41 2015-10-11: Unselecting, e.g. before export, had left the diagram status inconsistent:
	// Though the selected status of the elements was unset, the references of the formerly selected
	// elements invisibly remained in the respective diagram attributes, possibly causing unwanted effects.
	// So this new method was introduced to replace the selectElementByCoord(-1,-1) calls.
	/**
	 * Resets the selected state on all elements of the current {@link Root} and
	 * redraws the diagram.
	 *
	 * @see #unselectAll(boolean)
	 */
	public void unselectAll() // START KGU#430 2017-10-12: Issue #432 allow to suppress redrawing
	{
		unselectAll(true);
	}

	/**
	 * Resets the selected state on all elements of the current {@link Root} and
	 * redraws the diagram if {@code refresh} is true.
	 */
	public void unselectAll(boolean refresh)
	// END KGU#430 2017-10-12
	{
		if (root != null) {
			root.selectElementByCoord(-1, -1);
		}
		selected = selectedDown = selectedMoved = null;
		if (refresh) {
			redraw();
		}
		// START KGU#705 2019-09-24: Enh. #738
		highlightCodeForSelection();
		// END KGU#705 2019-09-24
	}
	// END KGU#41 2015-10-11

	// START KGU#705 2019-09-24: Enh. #738
	/**
	 * Highlights the code regions corresponding to the current selection in the
	 * preview area.
	 */
	private void highlightCodeForSelection() {
		if (show_CODE_PREVIEW && codePreviewMap != null) {
			int pos = -1;
			if (codeHighlighter != null) {
				// START KGU#978 2021-06-09: Workaround for mysterious bug #977
				//codeHighlighter.removeAllHighlights();
				try {
					codeHighlighter.removeAllHighlights();
				}
				catch (NullPointerException ex) {
					logger.log(Level.CONFIG, "Strange error #977 in code preview", ex);
				}
				// END KGU#978 2021-06-09
			}
			if (this.selected != null) {
				if (this.selected instanceof IElementSequence) {
					IElementSequence.Iterator iter = ((IElementSequence) this.selected).iterator(false);
					while (iter.hasNext()) {
						int p = highlightCodeForElement(iter.next(), false);
						if (pos < 0) {
							pos = p;
						}
					}
				} else {
					pos = highlightCodeForElement(this.selected, false);
				}
			}
			if (pos >= 0) {
				try {
					Rectangle2D viewRect = codePreview.modelToView2D(pos);
					// Scroll to make the rectangle visible
					codePreview.scrollRectToVisible(viewRect.getBounds());
				} catch (BadLocationException e) {
					// FIXME DEBUG (should not occur)
					e.printStackTrace();
				} catch (NullPointerException ex) {
					// Nothing we could do here, symptom for racing hazard
				}
			}
		}
	}

	/**
	 * Highlights the code regions corresponding to the given element
	 * {@code ele} in the preview area .
	 *
	 * @param ele - the {@link Element} the code for which is to be highlighted
	 * @param scrollTo TODO
	 * @return the start position of the first line of the element code
	 */
	private int highlightCodeForElement(Element ele, boolean scrollTo) {
		int pos = -1;
		int[] interval = codePreviewMap.get(ele);
		if (interval != null) {
			if (codeHighlighter == null) {
				codeHighlighter = codePreview.getHighlighter();
			}
			for (int line = interval[0]; line < interval[1]; line++) {
				try {
					pos = codePreview.getLineStartOffset(line);
					int p0 = pos + interval[2];
					int p1 = codePreview.getLineEndOffset(line);
					if (p0 < p1) {
						// START KGU#978 2021-06-09: Workaround for mysterious error #977
						//codeHighlighter.addHighlight(p0, p1, ele.executed ? execHighlightPainter : codeHighlightPainter);
						try {
							// Possibly a GUI initialisation problem...
							HighlightPainter hlPainter = ele.executed ? execHighlightPainter : codeHighlightPainter;
							if (hlPainter == null) {
								hlPainter = new DefaultHighlighter.DefaultHighlightPainter(
										ele.executed ? Element.E_RUNNINGCOLOR : Element.E_DRAWCOLOR);
							}
							codeHighlighter.addHighlight(p0, p1, hlPainter);
						}
						catch (NullPointerException ex) {
							logger.log(Level.CONFIG, "Strange error #977 in code preview", ex);
						}
						// END KGU#978 2021-06-09
						if (scrollTo) {
							Rectangle2D viewRect = codePreview.modelToView2D(pos);
							// Scroll to make the rectangle visible
							codePreview.scrollRectToVisible(viewRect.getBounds());
						}
					}
				} catch (BadLocationException e) {
					// Just ignore errors
					logger.warning("Bad code preview location for element " + ele);
				}
			}
		}
		return pos;
	}
	// END KGU#705 2019-09-24

	/**
	 * Tries to identify the {@link Element} that is responsible for the code
	 * line with given number {@code lineNo} in the {@link #codePreview}.
	 *
	 * @param lineNo - number of a line in the {@link #codePreview}.
	 * @return the closest corresponding {@link Element}.
	 */
	public Element identifyElementForCodeLine(int lineNo) {
		int lineRangeSize = codePreview.getLineCount();
		Element closest = null;
		for (Entry<Element, int[]> entry : codePreviewMap.entrySet()) {
			int[] range = entry.getValue();
			if (range[0] <= lineNo && range[1] > lineNo) {
				if (range[1] - range[0] < lineRangeSize || closest == null) {
					closest = entry.getKey();
					lineRangeSize = range[1] - range[0];
				}
			}
		}
		return closest;
	}

	// START KGU#705 2019-09-26: Enh. #738
	/**
	 * Entry point for external components to get the given {@code element}
	 * consistently selected in the diagram.
	 *
	 * @param element - the {@link Element} to be selected
	 */
	public void selectElement(Element element) {
		if (element != null) {
			Element sel = root.findSelected();
			if (sel != null) {
				sel.setSelected(false);
			}
			element.setSelected(true);
			selected = element;
			selectedDown = selected;
			this.redraw(element);
			if (codeHighlighter != null) {
				// START KGU#978 2021-06-09: Workaround for mysterious bug #977
				//codeHighlighter.removeAllHighlights();
				try {
					codeHighlighter.removeAllHighlights();
				}
				catch (NullPointerException ex) {
					logger.log(Level.CONFIG, "Strange error #977 in code preview", ex);
				}
				// END KGU#978 2021-06-09
			}
			this.highlightCodeForElement(element, false);
		}
	}
	// END KGU#705 2019-09-26

	/**
	 * This method is responsible for rendering a page using the provided
	 * parameters. The result will be a grid where each cell will be half an
	 * inch by half an inch.
	 *
	 * @param g - a value of type Graphics
	 * @param pageFormat - a value of type PageFormat
	 * @param page - a value of type int
	 * @return an int code for the status (either {@link java.awt.print.Printable#PAGE_EXISTS}
	 * if the page could be printed or {@link java.awt.print.Printable#NO_SUCH_PAGE} if it failed.
	 */
	public int print(Graphics g, PageFormat pageFormat, int page) {
		if (page == 0) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

			double sX = (pageFormat.getImageableWidth() - 1) / root.width;
			double sY = (pageFormat.getImageableHeight() - 1) / root.height;
			double sca = Math.min(sX, sY);
			if (sca > 1) {
				sca = 1;
			}
			g2d.scale(sca, sca);

			// START KGU#906 2021-01-06: Enh. #905 We don't want the triangles in the print
			//root.draw(g, null);
			root.draw(g, null, DrawingContext.DC_IMAGE_EXPORT);
			// END KGU#906 2021-01-06

			return (PAGE_EXISTS);
		} else {
			return (NO_SUCH_PAGE);
		}
	}

	/*========================================
	 * New method
	 *========================================*/
	/**
	 * Replaces the current {@link #root} by a new empty {@link Root} unless the
	 * current {@link #root} is being executed or the user refuses to make a
	 * decision about unsaved changes.
	 */
	public void newNSD() {
		// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
		if (!this.checkRunning()) {
			return;	// Don't proceed if the root is being executed
		}
		// END KGU#157 2016-03-16

		// START KGU#48 2015-10-17: Arranger support
		Root oldRoot = root;
		// END KGU#48 2015-10-17
		// only save if something has been changed
		// START KGU#534 2018-06-27: Bugfix #552 We should not proceed if the user canceled the saving
		//saveNSD(true);
		if (!saveNSD(true)) {
			return;
		}
		// END KGU#534 2018-06-27

		// create an empty diagram
		root = new Root();
		// START KGU#183 2016-04-23: Bugfix #155, Issue #169
		// We must not forget to clear a previous selection
		//this.selected = this.selectedDown = this.selectedUp = null;
		this.selectedDown = null;
		this.selected = root;
		root.setSelected(true);
		// END KGU#183 2016-04-23
		// START KGU#456 2017-11-20: Issue #452
		root.updateTutorialQueue(AnalyserPreferences.getOrderedGuideCodes());
		// END KGU#456 2017-11-20
		redraw();
		analyse();
		// START KGU#48 2015-10-17: Arranger support
		if (oldRoot != null) {
			oldRoot.notifyReplaced(root);
		}
		// END KGU#48 2015-10-17
		// START KGU#705 2019-09-23: Enh. #738
		this.updateCodePreview();
		// END KGU#705 2019-09-23
	}


	/*========================================
	 * Open method
	 *========================================*/
	/**
	 * Action method to have the user select an NSD or arrangement file to be
	 * loaded (which is going to replace the current {@link #root}. Does nothing
	 * if there is a pending execution.
	 *
	 * @see #openNSD(String)
	 * @see #openNsdOrArr(String)
	 */
	public void openNSD() {
		// START KGU 2015-10-17: This will be done by openNSD(String) anyway - once is enough!
		// only save if something has been changed
		//saveNSD(true);
		// END KGU 2015-10-17

		// START KGU#157 2016-03-16: Bugfix #131 - Precaution against replacement if under execution
		if (!this.checkRunning()) {
			return;	// Don't proceed if the root is being executed
		}
		// END KGU#157 2016-03-16

		// open an existing file
		// create dialog
		JFileChooser dlgOpen = new JFileChooser();
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgOpen);
		// END KGU#287 2017-01-09
		dlgOpen.setDialogTitle(Menu.msgTitleOpen.getText());
		// set directory
		if (root.getFile() != null) {
			dlgOpen.setCurrentDirectory(root.getFile());
		} else {
			dlgOpen.setCurrentDirectory(currentDirectory);
		}
		// config dialogue
		// START KGU 2016-01-15: Enh. #110 - select the provided filter
		//dlgOpen.addChoosableFileFilter(new StructogramFilter());
		// START KGU#802 2020-02-16: Issue #815
		//StructogramFilter filter = new StructogramFilter();
		//dlgOpen.addChoosableFileFilter(filter);
		StructorizerFilter filter = new StructorizerFilter();
		dlgOpen.addChoosableFileFilter(filter);
		dlgOpen.addChoosableFileFilter(new StructogramFilter());
		// END KGU#802 2020-02-16
		// START KGU#289 2016-11-15: Enh. #290 (allow arrangement files to be selected)
		dlgOpen.addChoosableFileFilter(new ArrFilter());
		dlgOpen.addChoosableFileFilter(new ArrZipFilter());
		// END KGU#289 2016-11-15
		dlgOpen.setFileFilter(filter);
		// END KGU 2016-01-15
		// show & get result
		int result = dlgOpen.showOpenDialog(this.getFrame());
		// react on result
		if (result == JFileChooser.APPROVE_OPTION) {
			// START KGU#289/KGU#316 2016-11-15/2016-12-28: Enh. #290/#318 (Arranger file support)
			//openNSD(dlgOpen.getSelectedFile().getAbsoluteFile().toString());
			openNsdOrArr(dlgOpen.getSelectedFile().getAbsoluteFile().toString());
			// END KGU#289/KGU#316 2016-11-15/2016-12-28
		}
	}

	// START KGU#289/KGU#316 2016-11-15/2016-12-28: Enh. #290/#318: Better support for Arranger files
	/**
	 * Attempts to open (load) the file specified by {@code _filepath} as .nsd,
	 * .arr, or .arrz file.<br/>
	 * If none of the expected file extensions match then an empty string is
	 * returned.
	 *
	 * @param _filepath - the path of the file to be loaded
	 * @return - the file extension
	 * @see #openNSD()
	 * @see #openNSD(String)
	 */
	public String openNsdOrArr(String _filepath) {
		String ext = ExtFileFilter.getExtension(_filepath);
		if (ext.equals("arr") || ext.equals("arrz")) {
			loadArrangement(new File(_filepath));
		}
		// START KGU#521 2018-06-08: Bugfix #536
		//else {
		else if (ext.equals("nsd")) {
		// END KGU#521 2018-06-08
			this.openNSD(_filepath);
		}
		// START KGU#521 2018-06-08: Bugfix #536
		else {
			ext = "";
		}
		// END KGU#521 2018-06-08
		return ext;
	}
	// END KGU#316 2016-12-28

	/**
	 * Method is to open an NSD file (not an arrangement file!) the path of
	 * which is given by {@code _filename}.<br/>
	 * If arrangement file are also to be accepted then use
	 * {@link #openNsdOrArr(String)} instead.
	 *
	 * @param _filename - file path of an NSD file
	 * @see #openNsdOrArr(String)
	 */
	public void openNSD(String _filename) {
		// START KGU#48 2015-10-17: Arranger support
		Root oldRoot = this.root;
		// END KGU#48 2015-10-17
		// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
		String errorMessage = Menu.msgErrorNoFile.getText();
		// END KGU#111 2015-12-16
		// START KGU#901 2021-01-22: Issue #901 WAIT_CURSOR on time-consuming actions
		Cursor origCursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		// END KGU#901 2021-01-22
		try {
			File f = new File(_filename);
			//System.out.println(f.toURI().toString());
			if (f.exists()) {
				// START KGU#901 2021-01-22: Issue #901 WAIT_CURSOR on time-consuming actions
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				// END KGU#901 2021-01-22
				// save current diagram (only if something has been changed)
				saveNSD(true);

				// open an existing file
				NSDParser parser = new NSDParser();
				//boolean hil = root.highlightVars;
				// START KGU#363 2017-05-21: Issue #372 API change
				//root = parser.parse(f.toURI().toString());
				root = parser.parse(f);
				// END KGU#363 2017-05-21
				//root.highlightVars = hil;
				if (Element.E_VARHIGHLIGHT) {
					root.retrieveVarNames();	// Initialise the variable table, otherwise the highlighting won't work
				}
				root.filename = _filename;
				// START KGU#969 2021-04-14: Bugfix #969 - precaution against relative paths
				//currentDirectory = new File(root.filename);
				//addRecentFile(root.filename);
				currentDirectory = f.getAbsoluteFile();
				addRecentFile(f.getAbsolutePath());
				// END KGU#969 2021-04-14

				// START KGU#183 2016-04-23: Issue #169
				selected = root;
				root.setSelected(true);
				// END KGU#183 2016-04-23
				redraw();
				analyse();
				// START KGU#456 2017-11-20: Issue #452
				root.updateTutorialQueue(AnalyserPreferences.getOrderedGuideCodes());
				// END KGU#456 2017-11-20
				// START KGU#48 2015-10-17: Arranger support
				if (oldRoot != null) {
					oldRoot.notifyReplaced(root);
				}
				// END KGU#48 2015-10-17
				// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
				errorMessage = null;
				// END KGU#111 2015-12-16

				// START KGU#362 2017-03-28: Issue #370
				if (root.storedParserPrefs != null) {
					this.handleKeywordDifferences(false);
				}
				// END KGU#362 2017-03-28
				// START KGU#705 2019-09-23: Enh. #738
				this.updateCodePreview();
				// END KGU#705 2019-09-23
			}
		} catch (Exception e) {
			//e.printStackTrace();
			// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
			//System.out.println(e.getMessage());
			errorMessage = e.getLocalizedMessage();
			if (errorMessage == null) {
				errorMessage = e.getMessage();
			}
			if (errorMessage == null || errorMessage.isEmpty()) {
				errorMessage = e.toString();
			}
			Level level = Level.SEVERE;
			if (e instanceof java.util.ConcurrentModificationException) {
				level = Level.WARNING;
			}
			logger.log(level, "openNSD(\"" + _filename + "\"): ", e);
			// END KGU#111 2015-12-16
		}
		// START KGU#901 2021-01-22: Issue #901 WAIT_CURSOR on time-consuming actions
		finally {
			setCursor(origCursor);
		}
		// END KGU#901 2021-01-22
		// START KGU#111 2015-12-16: Bugfix #63: No error messages on failed load
		if (errorMessage != null) {
			JOptionPane.showMessageDialog(this.getFrame(), "\"" + _filename + "\": " + errorMessage,
					Menu.msgTitleLoadingError.getText(),
					JOptionPane.ERROR_MESSAGE);
		}
		// END KGU#111 2015-12-16
		// START KGU#444/KGU#618 2018-12-18: Issue #417, #649
		this.adaptScrollUnits();
		// END KGU#444/KGU#618 2018-12-18
	}

	// START KGU#362 2017-03-28: Issue #370
	private boolean handleKeywordDifferences(boolean isChangeRequest) {
		StringList ignoreCaseInfo = root.storedParserPrefs.get("ignoreCase");
		boolean wasCaseIgnored = ignoreCaseInfo != null && ignoreCaseInfo.getText().equals("true");
		StringList replacements = new StringList();
		for (HashMap.Entry<String, StringList> entry : root.storedParserPrefs.entrySet()) {
			String storedValue = entry.getValue().concatenate();
			// START KGU#288 2016-11-06: Issue #279 - Method getOrDefault() missing in OpenJDK
			//String newValue = CodeParser.getKeywordOrDefault(entry.getKey(), "");
			String currentValue = (entry.getKey().equals("ignoreCase"))
					? Boolean.toString(CodeParser.ignoreCase)
							: CodeParser.getKeywordOrDefault(entry.getKey(), "");
			// END KGU#288 2016-11-06
			if (!storedValue.equals(currentValue)) {
				replacements.add("   " + entry.getKey() + ": \"" + storedValue + "\"  ≠  \"" + currentValue + "\"");
			}
		}
		String[] options = {
				Menu.lblRefactorNow.getText(),
				(isChangeRequest ? Menu.lblAllowChanges : Menu.lblAdoptPreferences).getText(),
				Menu.lblLeaveAsIs.getText()
		};
		String[] optionTexts = {
				Menu.msgRefactorNow.getText(),
				(isChangeRequest ? Menu.msgAllowChanges : Menu.msgAdoptPreferences).getText(),
				Menu.msgLeaveAsIs.getText()
		};
		String menuText = "";
		for (int i = 0; i < optionTexts.length; i++) {
			menuText += (char) ('a' + i) + ") " + optionTexts[i] + (i + 1 < optionTexts.length ? "," : ".") + "\n";
		}
		int answer = JOptionPane.showOptionDialog(this.getFrame(),
				Menu.msgKeywordsDiffer.getText().replace("%1", "\n" + replacements.getText() + "\n").replace("%2", menuText),
				Menu.msgTitleQuestion.getText(), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options, options[0]);
		boolean goAhead = false;
		switch (answer) {
		case 0: // Refactor the current diagram
		{
			HashMap<String, StringList> storedParserPrefs = root.storedParserPrefs;
			root.storedParserPrefs = null;
			refactorDiagrams(storedParserPrefs, false, wasCaseIgnored);
			goAhead = true;
		}
		break;
		case 1:
			if (isChangeRequest) {
				// drop the old keyword information
				root.storedParserPrefs = null;
			} else {
				// Refactor all the other diagrams
				// Cache the current parser preferences
				HashMap<String, StringList> splitPrefs = new HashMap<String, StringList>();
				// and adopt the stored preferences of the diagram
				for (String key : CodeParser.keywordSet()) {
					splitPrefs.put(key, Element.splitLexically(CodeParser.getKeywordOrDefault(key, ""), false));
					StringList stored = root.storedParserPrefs.get(key);
					if (stored != null) {
						CodeParser.setKeyword(key, stored.concatenate());
					}
				}
				boolean tmpIgnoreCase = CodeParser.ignoreCase;
				CodeParser.ignoreCase = wasCaseIgnored;
				try {
					Ini.getInstance().save();
				} catch (Exception ex) {
					logger.log(Level.SEVERE, "Ini.getInstance().save()", ex);
				}
				// Refactor the diagrams
				refactorDiagrams(splitPrefs, true, tmpIgnoreCase);
				root.storedParserPrefs = null;
				if (Arranger.hasInstance()) {
					Arranger.getInstance().redraw();
				}

				offerStructPrefAdaptation(splitPrefs);
			}
			goAhead = true;
			break;
		case 2:
			if (!isChangeRequest) {
				goAhead = true;
			}
			break;
		}
		return goAhead;
	}
	// END KGU#362 2017-03-28

	// START KGU#289 2016-11-15: Enh. #290 (Aranger file support
	private void loadArrangement(File arrFile) {
		Arranger arr = Arranger.getInstance();
		// START KGU#671 2019-03-01: Bugfix #693 - common existence check
		//String errorMsg = arr.loadArrangement((Mainform)NSDControl.getFrame(), arrFile.toString());
		String errorMsg = "";
		if (!arrFile.exists()) {
			errorMsg = Menu.msgErrorNoFile.getText();
		} else {
			// START KGU#901 2020-12-29: Issue #901 WAIT_CURSOR on time-consuming actions
			//errorMsg = arr.loadArrangement((Mainform)this.getFrame(), arrFile);
			Cursor origCursor = getCursor();
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));	// Possibly this should have done Surface?
				errorMsg = arr.loadArrangement((Mainform) this.getFrame(), arrFile);
			} finally {
				setCursor(origCursor);
			}
			// END KGU#901 2020-12-29
		}
		// END KGU#671 2019-03-01
		if (!errorMsg.isEmpty()) {
			JOptionPane.showMessageDialog(this.getFrame(), "\"" + arrFile + "\": " + errorMsg,
					Menu.msgTitleLoadingError.getText(),
					JOptionPane.ERROR_MESSAGE);
		} else {
			arr.setVisible(true);
			// START KGU#316 2016-12-28: Enh. #318
			addRecentFile(arrFile.getAbsolutePath());
			// START KGU#969 2021-04-14: Bugfix #969
			//this.currentDirectory = arrFile;
			this.currentDirectory = arrFile.getAbsoluteFile();
			// END KGU#969 2021-04-14
			// END KGU#316 2016-12-28
		}
	}
	// END KGU#289 2016-11-15

	/*========================================
	 * SaveAll method
	 *========================================*/
	/**
	 * Saves all NSD (and arrangement) files without file association or with
	 * unsaved changes in a serial action.
	 */
	public void saveAllNSD()
	{
		startSerialMode();
		// START KGU#901 2020-12-30: Issue #901
		Cursor origCursor = getCursor();
		// END KGU#901 2020-12-30
		try {
			if ((saveNSD(false)
					|| JOptionPane.showConfirmDialog(this.getFrame(),
							Menu.msgCancelAll.getText(),
							Menu.msgTitleSave.getText(),
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					&& Arranger.hasInstance()) {
				// START KGU#901 2020-12-30: Issue #901
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				// END KGU#901 2020-12-30
				Arranger.getInstance().saveAll(this.getFrame());
			}
		} finally {
			// START KGU#901 2020-12-30: Issue #901
			setCursor(origCursor);
			// END KGU#901 2020-12-30
			endSerialMode();
		}
	}

	/*========================================
	 * SaveAs method
	 *========================================*/
	/**
	 * Tries to save the current {@link Root} under a new path. Opens a
	 * FileChooser for this purpose
	 *
	 * @return true if the diagram was saved, false otherwise.
	 */
	public boolean saveAsNSD()
	// START KGU#320 2017-01-04: Bugfix #321(?) We need a possibility to save a different root
	{
		// START KGU#893 2020-12-20: Bugfix #892 - Arrangment group members must be cloned!
		//return saveAsNSD(this.root);
		Root rootToSave = this.root;
		// Check membership in named group: if there is any, clone the Root
		if (Arranger.hasInstance()
				&& !Arranger.getInstance().getGroupsFromRoot(root, true).isEmpty()) {
			rootToSave = (Root) root.copy();
		}
		boolean done = saveAsNSD(rootToSave);
		if (done && rootToSave != this.root) {
			JOptionPane.showMessageDialog(
					this.getFrame(),
					Menu.msgRootCloned.getText().replace("%1", this.root.getSignatureString(false, false))
					.replace("%2", rootToSave.getSignatureString(true, false)));
			this.setRoot(rootToSave, true, true, true);
		}
		return done;
		// END KGU#893 2020-12-20
	}

	/**
	 * Tries to save the {@link Root} given as {@code root} under a new path.
	 * Opens a FileChooser for this purpose.
	 *
	 * @param root - the diagram to be saved.
	 * @return true if the diagram was saved, false otherwise
	 */
	private boolean saveAsNSD(Root root)
	// END KGU#320 2017-01-04
	{
		// START KGU#911 2021-01-10: Enh. #910 suppress saving
		if (root.isRepresentingDiagramController()) {
			return true;	// Fake success
		}
		// END KGU#911 2021-01-10
		// propose name
		String nsdName = root.proposeFileName();

		// START KGU#534/KGU#553 2018-07-10: Enh. #552, issue #557 - special treatment for mass serial save
		File dir = this.currentDirectory;
		if (dir != null && isInSerialMode() && getSerialDecision(SerialDecisionAspect.SERIAL_SAVE) == SerialDecisionStatus.YES_TO_ALL) {
			// We have a target directory and the user has already confirmed to save all with proposed names 
			if (!dir.isDirectory()) {
				// A file name had been stored as current directory, so reduce it to its directory
				dir = dir.getParentFile();
			}
			// Accomplish the proposed file name...
			File f = new File(dir.getAbsolutePath() + File.separator + nsdName + ".nsd");
			// ... check whether a file with this name has existed
			int answer = this.checkOverwrite(f, true);
			if (answer == 0) {
				// Okay, we are entitled to overwrite
				root.filename = f.getAbsolutePath();
				root.shadowFilepath = null;
				return doSaveNSD(root);
			} else if (answer < 0) {
				// User wants to cancel the serial saving
				return false;
			} else if (answer == 2) {
				// Skip this file here, no further attempt
				return true;
			}
		}
		// END KGU#534/KGU#553 2018-07-10

		// Now we are either not in serial mode or a name conflict is to be solved via file chooser
		JFileChooser dlgSave = new JFileChooser();

		// START KGU#553 2018-07-13: Issue #557
		// Add a checkbox to adhere to the proposed names for all remaining roots if we are in serial mode
		JCheckBox chkAcceptProposals = addSerialAccessory(dlgSave);
		// END KGU#553 2018-07-13
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgSave);
		// END KGU#287 2017-01-09
		dlgSave.setDialogTitle(Menu.msgTitleSaveAs.getText());
		File rootFile = root.getFile();
		// set directory
		if (rootFile != null) {
			dlgSave.setCurrentDirectory(rootFile);
		} else {
			dlgSave.setCurrentDirectory(currentDirectory);
		}

		dlgSave.setSelectedFile(new File(nsdName));
		dlgSave.addChoosableFileFilter(new StructogramFilter());

		// START KGU#248 2016-09-15: Bugfix #244 - allow more than one chance
		//int result = dlgSave.showSaveDialog(this);
		int result = JFileChooser.ERROR_OPTION;
		do {
			result = dlgSave.showSaveDialog(this.getFrame());
		// END KGU#248 2016-9-15
			if (result == JFileChooser.APPROVE_OPTION) {
				String newFilename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
				if (!newFilename.substring(newFilename.length() - 4, newFilename.length()).toLowerCase().equals(".nsd")) {
					newFilename += ".nsd";
				}
				if (chkAcceptProposals != null && chkAcceptProposals.isSelected()) {
					setSerialDecision(SerialDecisionAspect.SERIAL_SAVE, true);
				}

				File f = new File(newFilename);
				int writePerm = checkOverwrite(f, false);

				if (writePerm < 0) {
					// Cancelled all
					return false;
				} else if (writePerm == 2) {
					// No further attempt
					return true;
				} else if (writePerm != 0) {
					// START KGU#248 2016-09-15: Bugfix #244 - message no longer needed (due to new loop)
					//JOptionPane.showMessageDialog(this, Menu.msgRepeatSaveAttempt.getText());
					result = JFileChooser.ERROR_OPTION;
					// END KGU#248 2016-09-15
				} else {
					root.filename = newFilename;
					// START KGU#316 2016-12-28: Enh. #318
					root.shadowFilepath = null;
					// END KGU#316 2016-12-28
					// START KGU#874 2020-10-19: Enh. #875 set the signature from file name for dummies
					replaceDummyHeader(root, f);
					// END KGU#874 2020-10-19
					// START KGU#94 2015.12.04: out-sourced to auxiliary method
					// START KGU#320 2017-01-04: Bugfix #321(?) Need a parameter now
					//doSaveNSD();
					doSaveNSD(root);
					// END KGU#320 2017-01-04
					// END KGU#94 2015-12-04
					// START KGU#273 2016-10-07: Bugfix #263 - remember the directory as current directory
					this.currentDirectory = f;
					// END KGU#273 2016-10-07
				}
			}
		// START KGU#248 2016-09-15: Bugfix #244 - allow to leave the new loop
		//	else
		//	{
		//	// User cancelled the file dialog -> leave the loop
		//		result = JFileChooser.CANCEL_OPTION;
		//	}
		} while (result == JFileChooser.ERROR_OPTION);
		// END KGU#248 2016-09-15

		return result != JFileChooser.CANCEL_OPTION;
	}

	// START KGU#874 2020-10-19: Issue #875 - try to make sense from the filename
	/**
	 * IN case {@code _root} has a dummy header (empty or "???"), we try to
	 * derive a header from the name of the chosen target file {@code _file}
	 *
	 * @param _root - the {@link Root} to be saved
	 * @param _file - the chosen target file
	 */
	private void replaceDummyHeader(Root _root, File _file) {
		String header = _root.getMethodName().trim();
		if (header.isEmpty() || header.equals("???")) {
			header = _file.getName();
			// Remove the ".nsd" extension
			header = header.substring(0, header.length() - 4);
			String argList = "";
			if (_root.isSubroutine()) {
				header = header.split(Matcher.quoteReplacement("" + Element.E_FILENAME_SIG_SEPARATOR), -1)[0];
				// Try to infer arguments and result type
				IRoutinePool pool = null;
				if (Arranger.hasInstance()) {
					pool = Arranger.getInstance();
				}
				StringList vars = _root.getUninitializedVars(pool);
				// TODO: Infer the argument types and the result type
				argList = "(" + vars.concatenate(", ") + ")";
				vars = _root.getVarNames();
				// START KGU#886 2020-12-10: Issue #884
				//if (vars.contains(header) || vars.contains("result", false)) {
				IElementVisitor returnFinder = new IElementVisitor() {
					private int retLen = CodeParser.getKeyword("preReturn").length();

					@Override
					public boolean visitPreOrder(Element _ele) {
						if (_ele instanceof Jump) {
							StringList lines = _ele.getUnbrokenText();
							for (int i = 0; i < lines.count(); i++) {
								String line = lines.get(i);
								if (Jump.isReturn(line)) {
									// Stops and returns false if a value is returned
									// TODO Try to identify the result type
									return line.substring(retLen).trim().isEmpty();
								}
							}
						}
						return true;
					}

					@Override
					public boolean visitPostOrder(Element _ele) {
						return true;
					}

				};
				boolean lastElReturnsVal = false;
				if (root.children.getSize() > 0) {
					Element lastEl = root.children.getElement(root.children.getSize() - 1);
					if (lastEl instanceof Instruction) {
						int retLen = CodeParser.getKeyword("preReturn").length();
						StringList lines = lastEl.getUnbrokenText();
						for (int i = 0; i < lines.count(); i++) {
							String line = lines.get(i);
							if (Jump.isReturn(line)) {
								// Stops and detects if a value is returned
								lastElReturnsVal = !line.substring(retLen).trim().isEmpty();
								break;
							}
						}
					}
				}
				if (vars.contains(header) || vars.contains("result", false)
						|| lastElReturnsVal
						|| !_root.children.traverse(returnFinder)) {
				// END KGU#886 2020-12-10: Issue #884
					// TODO try to identify the type
					argList += ": ???";
				}
			}
			if (Function.testIdentifier(header, false, null)) {
				// START KGU#886 2020-12-10: Bugfix #884
				//root.setText(header + argList);
				root.addUndo();
				root.setText(header + argList);
				this.analyse();
				// END KGU#886 2020-12-10
				this.invalidateAndRedraw();
			}
		}

	}
	// END KGU#874 2020-10-19

	/**
	 * In case of serial mode adds a checkbox to {@code fileChooser}
	 *
	 * @param fileChooser - the {@link JFileChooser} to be decorated if in
	 * serial mode.
	 * @return the checkbox if it was created
	 */
	private JCheckBox addSerialAccessory(JFileChooser fileChooser) {
		JCheckBox chkAcceptProposals = null;
		if (isInSerialMode() && getSerialDecision(SerialDecisionAspect.SERIAL_SAVE) == SerialDecisionStatus.INDIVIDUAL) {
			// Unfortunateley, the accessory is usally placed right of the file view.
			// So we split the caption for the checkbox to be added into words and
			// and "verticalize" the text by distributing the words over as many
			// vertically boxed labels as needed.
			JPanel pnlAccept = new JPanel();
			pnlAccept.setLayout(new BoxLayout(pnlAccept, BoxLayout.PAGE_AXIS));
			String[] words = Menu.lblAcceptProposedNames.getText().split("\\s+");
			chkAcceptProposals = new JCheckBox(words[0]);
			// Find out the maximum word length such that we may combine shorter words
			int maxWordLen = words[0].length() + 5;
			for (int i = 1; i < words.length; i++) {
				maxWordLen = Math.max(maxWordLen, words[i].length());
			}
			pnlAccept.add(chkAcceptProposals);
			int i = 1;
			while (i < words.length) {
				String word = words[i++];
				while (i < words.length && word.length() + 1 + words[i].length() <= maxWordLen) {
					word += " " + words[i++];
				}
				JLabel lbl = new JLabel(word);
				lbl.setBorder(new EmptyBorder(0, 5, 2, 0));
				pnlAccept.add(lbl);
			}
			fileChooser.setAccessory(pnlAccept);
		}
		return chkAcceptProposals;
	}

	/**
	 * Checks if a file {@code _file} already exists and requests overwrite
	 * permission in this case.
	 *
	 * @param f - The proposed file path
	 * @param showFilename - whether the file name is to be presented in the
	 * message
	 * @return 0 = writing is permitted, 1 = modification requested, 2 = skip
	 * (don't write), -1 - cancel all
	 */
	private int checkOverwrite(File f, boolean showFilename) {
		int writeNow = 0;
		if (f.exists()) {
			writeNow = 1;
			// START KGU#534 2018-06-27: Enh. #552
			if (isInSerialMode()) {
				switch (getSerialDecision(SerialDecisionAspect.SERIAL_OVERWRITE)) {
				case INDIVIDUAL: {
					String[] options = {
							Menu.lblContinue.getText(),
							Menu.lblModify.getText(),
							Menu.lblYesToAll.getText(),
							Menu.lblSkip.getText()
					};
					String initialValue = options[0];
					String message = Menu.msgOverwriteFile.getText();
					if (showFilename) {
						message = Menu.msgOverwriteFile1.getText().replaceAll("%", f.getAbsolutePath());
					}
					int res = JOptionPane.showOptionDialog(
							this.getFrame(),
							message,
							Menu.btnConfirmOverwrite.getText(),
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							initialValue);
					if (res < 0) {
						writeNow = -1;
					} else if (res > 2) {
						writeNow = 2;
					} else if (res == 2) {
						setSerialDecision(SerialDecisionAspect.SERIAL_OVERWRITE, true);
					}
					if (res == 0 || res == 2) {
						writeNow = 0;
					}
				}
				break;
				case YES_TO_ALL:
					writeNow = 0;
					break;
				default: ;
				}
			} else {
			// END KGU#534 2018-06-27
				int res = JOptionPane.showConfirmDialog(
						this.getFrame(),
						Menu.msgOverwriteFile.getText(),
						Menu.btnConfirmOverwrite.getText(),
						JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					writeNow = 0;
				}
			// START KGU#534 2018-06-27: Enh. #552
			}
			// END KGU#534 2018-06-27
		}
		return writeNow;
	}

	/*========================================
	 * Save methods
	 *========================================*/
	/**
	 * Stores unsaved changes (if any) of the current {@link Root} . If
	 * {@code _askToSave} is {@code true} then the user may confirm or deny saving
	 * or cancel the inducing request. Otherwise unsaved changes will silently be
	 * stored.
	 *
	 * @param _askToSave - if true and the current root has unsaved changes then
	 *     a user dialog will pop up first
	 * @return {@code true} if the user did not cancel the save request
	 * 
	 * @see #saveNSD(Root, boolean)
	 */
	public boolean saveNSD(boolean _askToSave)
	// START KGU#320 2017-01-04: Bugfix #321 - we need an opportunity to save another Root
	{
		// START KGU#456 2017-11-05: Enh. #452
		//return saveNSD(this.root, _askToSave);
		boolean needsSave = !root.isEmpty() && root.hasChanged();
		if (saveNSD(this.root, _askToSave)) {
			if (needsSave && root.advanceTutorialState(26, root)) {
				analyse();
			}
			return true;
		}
		return false;
		// END KGU#456 2017-11-05
	}

	/**
	 * Stores unsaved changes (if any) of the given {@link Root} {@code root}.
	 * If {@code _askToSave} is {@code true} then the user may confirm or deny
	 * saving or cancel the inducing request. Otherwise unsaved changes will
	 * silently be stored.
	 *
	 * @param root - {@link Root} to be saved
	 * @param _askToSave - if {@code true} and the given {@code root} has
	 *     unsaved changes then a user dialog will pop up first.
	 * @return {@code true} if the user did not cancel the save request
	 */
	public boolean saveNSD(Root root, boolean _askToSave)
	// END KGU#320 2017-01-04
	{
		// START KGU#911 2021-01-10: Enh. #910 suppress saving
		if (root.isRepresentingDiagramController()) {
			return true;	// Fake success
		}
		// END KGU#911 2021-01-10
		int res = 0;	// Save decision: 0 = do save, 1 = don't save, -1 = cancelled (don't leave)
		// only save if something has been changed
		// START KGU#137 2016-01-11: Use the new method now
		//if(root.hasChanged==true)
		// START KGU#749 2019-10-13: Bugfix #763 - also save in case of a stale file
		//if (!root.isEmpty() && root.hasChanged())
		boolean hasValidFile = root.getFile() != null;
		if (!hasValidFile && root.shadowFilepath != null) {
			File shadow = new File(root.shadowFilepath);
			if (shadow.canRead()) {
				root.filename = shadow.getAbsolutePath();
				root.shadowFilepath = null;	// FIXME: This may require refreshing / updating - is it ensured?
				hasValidFile = true;
			}
		}
		if (!root.isEmpty() && (root.hasChanged() || !hasValidFile))
		// END KGU#749 2019-10-13
		// END KGU#137 2016-01-11
		{
			String message = null;
			if (_askToSave) {
				String filename = root.filename;
				if (filename == null || filename.isEmpty()) {
					filename = root.proposeFileName();
				}
				message = Menu.msgSaveChanges.getText() + "\n\"" + filename + "\"";
			}
			// START BOB 2019-10-16: Unimozer crashed with a NullPointerException
			if (this.NSDControl == null) {
				return false;
			}
			// END BOB 2019-10-16
			res = requestSaveDecision(message, this.getFrame(), SerialDecisionAspect.SERIAL_SAVE);

			// START KGU#534 2018-06-27: Enh. #552
			//if (res==0)
			if (res == 0 || res == 2)
			// END KGU#534 2018-06-27
			{
				// Check whether root has already been loaded or saved once
				//boolean saveIt = true;

				//System.out.println(this.currentDirectory.getAbsolutePath());
				// START KGU#749 2019-10-13: Bugfix #763 - Also save in case of a stale file
				//if (root.filename.equals(""))
				// START KGU#874 2020-10-18: Issue #875 Special handling for virgin Roots in archive groups
				boolean fileFaked = false;
				if (!hasValidFile && isArrangerOpen()) {
					Collection<Group> owners = Arranger.getInstance().getGroupsFromRoot(root, false);
					/* If there is exactly one owning group except the default group
					 * and this group resides in an archive file then we will prepare
					 * the desired file paths for integration of the virgin diagram
					 */
					if (owners.size() == 1) {
						File arrzFile = null;
						for (Group owner : owners) {
							if (!owner.isDefaultGroup()) {
								arrzFile = owner.getArrzFile(true);
							}
						}
						if (arrzFile != null) {
							String fileName = root.proposeFileName();
							// We won't accept a nonsense file name
							if (!fileName.isEmpty() && !fileName.equals("???")) {
								try {
									/* Create a temporary file path */
									File tempFile = File.createTempFile("Structorizer", ".nsd");
									root.shadowFilepath = tempFile.getAbsolutePath();
									/* Build the virtual file path (within the archive,
									 * we hope there won't be a name collision)
									 */
									root.filename = arrzFile.getAbsolutePath() + File.separator + fileName + ".nsd";
									fileFaked = true;
									/* Remove the temporary file lest it should be regarded as update */
									if (tempFile.exists()) {
										tempFile.delete();
									}
									// Only if all preparations worked we will fake file validity
									hasValidFile = true;
								} catch (IOException exc) {
									logger.log(Level.FINE, "No temporary file creatable", exc);
								}
							}
						}
					}
				}
				// END KGU#874 2020-10-18
				if (!hasValidFile)
				// END KGU#749 2019-10-13
				{
					// root has never been saved
// START KGU#248 2016-09-15: Bugfix #244 delegate to saveAsNSD()
//					JFileChooser dlgSave = new JFileChooser();
//					dlgSave.setDialogTitle(Menu.msgTitleSave.getText());
//					// set directory
//					if (root.getFile() != null)
//					{
//						dlgSave.setCurrentDirectory(root.getFile());
//					}
//					else
//					{
//						dlgSave.setCurrentDirectory(currentDirectory);
//					}
//
//					// propose name
//
//					dlgSave.setSelectedFile(new File(root.proposeFileName()));
//
//					dlgSave.addChoosableFileFilter(new StructogramFilter());
//					int result = dlgSave.showSaveDialog(this);
//
//					if (result == JFileChooser.APPROVE_OPTION)
//					{
//						root.filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
//						if(!root.filename.substring(root.filename.length()-4).toLowerCase().equals(".nsd"))
//						{
//							root.filename+=".nsd";
//						}
//					}
//					else
//					{
//						saveIt = false;
//					}
//				}
//
//				if (saveIt == true)
//					// START KGU#320 2017-01-04: Bugfix (#321)
					//saveAsNSD();
					if (!saveAsNSD(root)) {
						// START KGU#634 2019-01-17: Issue #664 - in mode AUTO_SAVE_ON_CLOSE, this answer my be ambiguous
						//res = -1;	// Cancel all
						if (!Element.E_AUTO_SAVE_ON_CLOSE
								|| !isGoingToClose
								|| JOptionPane.showConfirmDialog(this.getFrame(),
										Menu.msgVetoClose.getText(),
										Menu.msgTitleWarning.getText(),
										JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
							res = -1;
						} else {
							res = 1;
						}
						// END KGU#634 2019-01-17
					}
					// END KGU#320 2017-01-04
				} else
// END KGU#248 2016-09-15
				{
					// START KGU#94 2015-12-04: Out-sourced to auxiliary method
					// START KGU#320 2017-01-04: Bugfix (#321) had to parameterize this
					//doSaveNSD();
					// START KGU#874 2020-10-18: Issue #875
					//doSaveNSD(root);
					if (!doSaveNSD(root) && fileFaked) {
						root.filename = "";
						root.shadowFilepath = null;
					}
					// END KGU#874 2020-10-18
					// END KGU#320 2017-01-04
					// END KGU#94 2015-12-04
				}
			}
		}
		return res != -1; // true if not cancelled
	}

	/**
	 * Service method for a decision about saving a file in a potential serial
	 * context.
	 *
	 * @param _messageText - the text of the offered question if an interactive
	 * dialog is wanted at all, null otherwise
	 * @param _initiator - an owning component for the modal message or question
	 * boxes
	 * @param _aspect - the current serial action mode (of type
	 * {@link SerialDecisionAspect})
	 * @return 0 for approval, 1 for disapproval, 2 for "yes to all", 3 for "no
	 * to all", -1 for cancel
	 */
	public static int requestSaveDecision(String _messageText, Component _initiator, SerialDecisionAspect _aspect) {
		int res = 0;
		// START KGU#534 2018-06-27: Enh. #552
		if (_messageText != null && isInSerialMode()) {
			switch (getSerialDecision(_aspect)) {
			case NO_TO_ALL:
				res = 1;
				// NO break here!
			case YES_TO_ALL:
				_messageText = null;
				break;
			default:;
			}
		}
		// END KGU#534 2018-06-27
		if (_messageText != null) {
			// START KGU#49 2015-10-18: If induced by Arranger then it's less ambiguous seeing the NSD name
			//res = JOptionPane.showOptionDialog(this,
			//		   "Do you want to save the current NSD-File?",
			String[] options = null;
			if (isInSerialMode()) {
				options = new String[]{
						Menu.lblContinue.getText(),
						Menu.lblSkip.getText(),
						Menu.lblYesToAll.getText(),
						Menu.lblNoToAll.getText() // Well, this is less sensible...
				};
			} else {
				options = new String[]{
						Menu.lblYes.getText(),
						Menu.lblNo.getText()
				};
			}
			Object initialValue = options[0];
			res = JOptionPane.showOptionDialog(_initiator,
					_messageText,
			// END KGU#49 2015-10-18
					Menu.msgTitleQuestion.getText(),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					// START KGU#534 2018-06-27: Enh. #552
					//null,null,null
					null,
					options,
					initialValue
					// END KGU#534 2018-06-27
					);
		}
		if (res >= 2) {
			setSerialDecision(_aspect, res == 2);
		}
		return res;
	}

	// START KGU#94 2015-12-04: Common file writing routine (on occasion of bugfix #40)
	// START KGU#320 2017-01-03: Bugfix (#321)
	//private boolean doSaveNSD()
	private boolean doSaveNSD(Root root) // END KGU#320 2017-01-03
	{
		//String[] EnvVariablesToCheck = { "TEMP", "TMP", "TMPDIR", "HOME", "HOMEPATH" };
		boolean done = false;
		try {
			// START KGU#94 2015.12.04: Bugfix #40 part 1
			// A failed saving attempt should not leave a truncated file!
			//FileOutputStream fos = new FileOutputStream(root.filename);
			String filename = root.filename;
			// START KGU#316 2016-12-28: Enh. #318
			if (root.shadowFilepath != null) {
				filename = root.shadowFilepath;
			}
			// END KGU#316 2016-12-28
			File f = new File(filename);
			boolean fileExisted = f.exists();
			// START KGU#316 2016-12-28: Enh. 318
			//if (fileExisted)
			if (fileExisted && root.shadowFilepath == null)
			// END KGU#316 2016-12-28
			{
				File tmpFile = File.createTempFile("Structorizer", ".nsd");
				filename = tmpFile.getAbsolutePath();
			}
			FileOutputStream fos = new FileOutputStream(filename);
			// END KGU#94 2015-12-04
			Writer out = new OutputStreamWriter(fos, "UTF-8");
			XmlGenerator xmlgen = new XmlGenerator();
			out.write(xmlgen.generateCode(root, "\t", false));
			out.close();

			// START KGU#94 2015-12-04: Bugfix #40 part 2
			// If the NSD file had existed then replace it by the output file after having created a backup
			// START KGU#316 2016-12-28: Enh. #318 Let nsd files reside in arrz files
			// if (fileExisted)
			if (root.shadowFilepath != null) {
				// START KGU#320 2017-01-04: Bugfix #321(?)
				//if (!zipToArrz(filename)) {
				if (!zipToArrz(root, filename)) {
				// END KGU#320 2017-01-04
					// If the saving to the original arrz file failed then make the shadow path the actual one
					root.filename = filename;
					root.shadowFilepath = null;
				}
			} else if (fileExisted)
			// END KGU#316 2016-12-28
			{
				File backUp = new File(root.filename + ".bak");
				if (backUp.exists()) {
					backUp.delete();
				}
				// START KGU#717 2019-07-31: Bugfix #526, #731
				//f.renameTo(backUp);
				boolean moved = Archivar.renameTo(f, backUp);
				// END KGU#717 2019-07-31
				f = new File(root.filename);
				File tmpFile = new File(filename);
				// START KGU#717 2019-07-31: Bugfix #526, #731
				//tmpFile.renameTo(f);
				moved = moved && Archivar.renameTo(tmpFile, f);
				// END KGU#717 2019-07-31
				// START KGU#509 2018-03-20: Bugfix #526 renameTo may have failed, so better check
				if (!moved || !f.exists() && tmpFile.canRead()) {
					logger.log(Level.WARNING, "Failed to rename \"{0}\" to \"{1}\"; trying a workaround...",
							new Object[]{filename, f.getAbsolutePath()});
					String errors = Archivar.copyFile(tmpFile, f, true);
					if (!errors.isEmpty()) {
						JOptionPane.showMessageDialog(this.getFrame(),
								Menu.msgErrorFileRename.getText().replace("%1", errors).replace("%2", tmpFile.getAbsolutePath()),
								Menu.msgTitleError.getText(),
								JOptionPane.ERROR_MESSAGE, null);
					}
				}
				// END KGU#509 2018-03-20
				// START KGU#309 2016-12-15: Issue #310 backup may be opted out
				if (!Element.E_MAKE_BACKUPS && backUp.exists()) {
					backUp.delete();
				}
				// END KGU#309 2016-12-15
			}
			// END KGU#94 2015.12.04

			// START KGU#137 2016-01-11: On successful saving, record the undo stack level
			//root.hasChanged=false;
			root.rememberSaved();
			// END KGU#137 2016-01-11
			// START KGU#316 2016-12-28: Enh. #318: Don't remember a zip-internal file path
			//addRecentFile(root.filename);
			addRecentFile(root.getPath(true));
			// END KGU#316 2016-12-28
			done = true;
		} catch (Exception ex) {
			String message = ex.getLocalizedMessage();
			if (message == null) {
				message = ex.getMessage();
			}
			if (message == null || message.isEmpty()) {
				message = ex.toString();
			}
			JOptionPane.showMessageDialog(this.getFrame(),
					Menu.msgErrorFileSave.getText().replace("%", message),
					Menu.msgTitleError.getText(),
					JOptionPane.ERROR_MESSAGE, null);
		}
		return done;
	}

	// END KGU#94 2015-12-04
	// START KGU#316 2016-12-28: Enh. #318
	// START KGU#320 2017-01-04: Bugfix #320 We might be forced to save a different diagram (from Arranger)
	//private boolean zipToArrz(String tmpFilename)
	private boolean zipToArrz(Root root, String tmpFilename)
	// END KGU#320 2017-01-04
	{
		String error = null;
		boolean isDone = false;
		final int BUFSIZE = 2048;
		byte[] buf = new byte[BUFSIZE];
		int len = 0;

		StringList inZipPath = new StringList();
		File arrzFile = new File(root.filename);
		while (arrzFile != null && !arrzFile.isFile()) {
			inZipPath.add(arrzFile.getName());
			arrzFile = arrzFile.getParentFile();
		}
		if (arrzFile == null) {
			int posArrz = root.filename.toLowerCase().indexOf(".arrz");
			error = ((posArrz > 0) ? root.filename.substring(0, posArrz + 5) : root.filename) + ": " + Menu.msgErrorNoFile.getText();
		} else {
			String localPath = inZipPath.reverse().concatenate(File.separator);

			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(arrzFile);
				File tmpZipFile = File.createTempFile("Structorizer", "zip");
				final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpZipFile));
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				// Copy all but the file to be updated
				while (entries.hasMoreElements()) {
					ZipEntry entryIn = entries.nextElement();
					if (!entryIn.getName().equals(localPath)) {
						zos.putNextEntry(entryIn);
						InputStream is = zipFile.getInputStream(entryIn);
						while ((len = is.read(buf)) > 0) {
							zos.write(buf, 0, len);
						}
						zos.closeEntry();
					}
				}
				// Now add the file to be updated
				zos.putNextEntry(new ZipEntry(localPath));
				FileInputStream fis = new FileInputStream(tmpFilename);
				while ((len = (fis.read(buf))) > 0) {
					zos.write(buf, 0, len);
				}
				zos.closeEntry();
				fis.close();
				zos.close();
				zipFile.close();
				String zipPath = arrzFile.getAbsolutePath();
				File bakFile = new File(zipPath + ".bak");
				if (bakFile.exists()) {
					bakFile.delete();
				}
				// START KGU#717 2019-07-31: Bugfix #526/#731
				//boolean bakOk = arrzFile.renameTo(bakFile);
				//boolean zipOk = tmpZipFile.renameTo(new File(zipPath));
				boolean bakOk = Archivar.renameTo(arrzFile, bakFile);
				boolean zipOk = Archivar.renameTo(tmpZipFile, new File(zipPath));
				// END KGU#717 2019-07-31
				if (bakOk && zipOk && !Element.E_MAKE_BACKUPS) {
					bakFile.delete();
				}
				isDone = true;
			} catch (ZipException ex) {
				error = ex.getLocalizedMessage();
			} catch (IOException ex) {
				error = ex.getLocalizedMessage();
			}
		}
		if (error != null) {
			JOptionPane.showMessageDialog(this.getFrame(),
					error,
					Menu.msgTitleError.getText(),
					JOptionPane.ERROR_MESSAGE, null);
		}
		return isDone;
	}
	// END KGU#316 2016-12-28

	/*========================================
	 * addUndo method
	 *========================================*/
	/**
	 * Creates an undo entry on .root, if the action wasn't cancelled.
	 * (Otherwise a CancelledException is thrown.)
	 *
	 * @param _isRoot - if the Root itself is to be changed (such that
	 * attributes are to be cached)
	 * @throws CancelledException
	 */
	public void addUndoNSD(boolean _isRoot) throws CancelledException {
		if (!_isRoot && root.storedParserPrefs != null) {
			// This is an un-refactored Root!
			// So care for consistency
			if (!this.handleKeywordDifferences(true)) {
				throw new CancelledException();
			}
		}
		root.addUndo(_isRoot);
		// START KGU#684 2019-06-13: Bugfix #728
		if (this.findDialog != null) {
			this.findDialog.resetResults();
		}
		// END KGU#684 2019-06-13
	}

	/*========================================
	 * Undo method
	 *========================================*/
	/**
	 * Reverts the last action from the undo stack and updates the environment.
	 */
	public void undoNSD() {
		// START KGU#684 2019-06-13: Bugfix #728
		if (this.findDialog != null) {
			this.findDialog.resetResults();
		}
		// END KGU#684 2019-06-13
		root.undo();
		// START KGU#138 2016-01-11: Bugfix #102 - All elements will be replaced by copies...
		selected = this.selectedDown = this.selectedMoved = null;
		// END KGU#138 2016-01-11
		// START KGU#272 2016-10-06: Bugfix #262: We must unselect root such that it may find a selected descendant
		root.setSelected(false);
		// END KGU#272 2016-10-06
		// START KGU#183 2016-04-24: Issue #169 - Restore previous selection if possible
		selected = root.findSelected();
		// END KGU#183 2016-04-24
		// START KGU#272 2016-10-06: Bugfix #262
		if (selected == null) {
			selected = root;
			root.setSelected(true);
		} else {
			selectedDown = selected;
		}
		// END KGU#272 2016-10-06
		redraw();
		analyse();
		// START KGU#705 2019-09-23: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-09-23
	}

	/*========================================
	 * Redo method
	 *========================================*/
	/**
	 * Redoes the last undone action (if possible) and updates the environment.
	 */
	public void redoNSD() {
		// START KGU#684 2019-06-13: Bugfix #728
		if (this.findDialog != null) {
			this.findDialog.resetResults();
		}
		// END KGU#684 2019-06-13
		root.redo();
		// START KGU#138 2016-01-11: Bugfix #102 All elements will be replaced by copies...
		selected = this.selectedDown = this.selectedMoved = null;
		// END KGU#138 2016-01-11
		// START KGU#272 2016-10-06: Bugfix #262: We must unselect root such that it may find a selected descendant
		root.setSelected(false);
		// END KGU#272 2016-10-06
		// START KGU#183 2016-04-24: Issue #169 - Restore previous selection if possible
		selected = root.findSelected();
		// END KGU#183 2016-04-24
		// START KGU#272 2016-10-06: Bugfix #262
		if (selected == null) {
			selected = root;
			root.setSelected(true);
		} else {
			selectedDown = selected;
		}
		// END KGU#272 2016-10-06
		redraw();
		analyse();
		// START KGU#705 2019-09-23: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-09-23
	}

	/*========================================
	 * applicability test methods
	 *========================================*/
	/**
	 * Checks whether the prerequisites to paste elements are fulfilled.
	 *
	 * @return true if there is something to paste and pasting is allowed.
	 */
	public boolean canPaste() {
		boolean cond = (eCopy != null && selected != null);
		if (cond) {
			// START KGU#143 2016-01-21 Bugfix #114
			// The first condition is for the case the copy is a referenced sequence 
			cond = !eCopy.isExecuted();
			// We must not insert to a subqueue with an element currently executed or with pending execution
			// (usually the exection index would then be on the stack!)
			if (!(selected instanceof Subqueue) && selected.parent != null && selected.parent.isExecuted()) {
				cond = false;
			}
			// END KGU#143 2016-01-21
			cond = cond && !selected.getClass().getSimpleName().equals("Root");
		}

		return cond;
	}

	// START KGU#143 2016-01-21: Bugfix #114 - elements involved in execution must not be edited...
	/**
	 * Checks whether the prerequisites to cut selected elements are fulfilled.
	 *
	 * @return true if there is something selected and may be cut now.
	 * @see #canCopy()
	 */
	//public boolean canCutCopy()
	public boolean canCut() {
		// START KGU#177 2016-04-14: Enh. #158 - we want to allow to copy diagrams e.g. to an Arranger of a different JVM
		//return canCopy() && !selected.executed && !selected.waited;
		// START KGU#177 2016-07-06: Enh #158: mere re-formulation (equivalent)
		//return canCopy() && !(selected instanceof Root) && !selected.executed && !selected.waited;
		// START KGU#911 2021-01-10: Enh. #910
		//return canCopyNoRoot() && !selected.isExecuted();
		return canCopyNoRoot() && !selected.isExecuted() && !selected.isImmutable();
		// END KGU#911 2021-01-10
		// END KGU#177 2016-07-06
		// END KGU#177 2016-04-14
	}

	// ... though breakpoints shall still be controllable
	/**
	 * Checks whether the prerequisites to copy selected elements are fulfilled.
	 *
	 * @return true if there is something selected and may be copied now.
	 * @see #canCut()
	 * @see #canCopyNoRoot()
	 */
	public boolean canCopy()
	// END KGU#143 2016-01-21
	{
		boolean cond = (selected != null);
		if (cond) {
			// START KGU#177 2016-04-14: Enh. #158 - we want to allow to copy diagrams e.g. to an Arranger of a different JVM
			//cond = !selected.getClass().getSimpleName().equals("Root");
			// END KGU#177 2016-04-14
			// START KGU#87 2015-11-22: Allow to copy a non-empty Subqueue
			//cond = cond && !selected.getClass().getSimpleName().equals("Subqueue");
			cond = cond && (!selected.getClass().getSimpleName().equals("Subqueue") || ((Subqueue) selected).getSize() > 0);
			// END KGU#87 2015-11-22
		}

		return cond;
	}

	// START KGU#177 2016-07-06: Enh. #158 - accidently breakpoints had become enabled on Root
	/**
	 * Restricted check wheher the selectd element(s) may be copied - will
	 * return false if the selected element is a {@link Root}.
	 *
	 * @return true if there is something except a {@link Root} selected and may
	 * be copied now.
	 * @see #canCopy()
	 * @see #canCut()
	 */
	public boolean canCopyNoRoot() {
		return canCopy() && !(selected instanceof Root);
	}
	// END KGU#177 2016-07-06

	// START KGU#143 2016-11-17: Issue #114: Complex condition for editability
	/**
	 * Checks whether the prerequisites to edit the selected element are
	 * fulfilled.
	 *
	 * @return true if there is something selected and may be edited now.
	 */
	public boolean canEdit() {
		return selected != null && !this.selectedIsMultiple()
				&& (!selected.isExecuted(false) || selected instanceof Instruction && !selected.executed);
	}
	// END KGU#143 2016-11-17

	// START KGU#686 2019-03-17: Enh. #56
	public boolean canSetBreakpoint() {
		return canCopyNoRoot() && !(selected instanceof Forever || selected instanceof Try);
	}
	// END KGU#686 2019-03-17

	// START KGU#199 2016-07-06: Enh. #188: Element conversions
	public boolean canTransmute() {
		boolean isConvertible = false;
		// START KGU#911 2021-01-10: Enh. #910
		//if (selected != null && !selected.isExecuted())
		if (selected != null && !selected.isExecuted() && !selected.isImmutable())
		// END KGU#911 2021-01-10
		{
			// START KGU#666 2019-02-26: Bugfix #688 - it should always be offered to convert Calls and Jumps (to Instructions)
			//if (selected instanceof Instruction)
			if (selected instanceof Call || selected instanceof Jump) {
				isConvertible = true;
			} else if (selected instanceof Instruction)
			// END KGU#666 2019-02-26
			{
				Instruction instr = (Instruction) selected;
				isConvertible = instr.getUnbrokenText().count() > 1
						|| instr.isJump()
						|| instr.isFunctionCall(true)
						|| instr.isProcedureCall(true);
			} else if (selected instanceof IElementSequence && ((IElementSequence) selected).getSize() > 1) {
				isConvertible = true;
				for (int i = 0; isConvertible && i < ((IElementSequence) selected).getSize(); i++) {
					if (!(((IElementSequence) selected).getElement(i) instanceof Instruction)) {
						isConvertible = false;
					}
				}
			}
			// START KGU#229 2016-08-01: Enh. #213
			else if (selected instanceof For) {
				isConvertible = ((For) selected).style == For.ForLoopStyle.COUNTER;
			}
			// END KGU#229 2016-08-01
			// START KGU#267 2016-10-03: Enh. #257
			else if (selected instanceof Case) {
				isConvertible = true;
			}
			// END KGU#267 2016-10-03
			// START KGU#357 2017-03-10: Enh. #367
			else if (selected instanceof Alternative && ((Alternative) selected).qFalse.getSize() > 0) {
				isConvertible = true;
			}
			// END KGU#357 2017-03-10
		}
		return isConvertible;
	}
	// END KGU#199 2016-07-06

	// START KGU#373 2017-03-28: Enh. #387
	public boolean canSave(boolean any) {
		boolean cond = this.root.hasChanged();
		if (!cond && any && Arranger.hasInstance()) {
			Set<Root> roots = Arranger.getInstance().getAllRoots();
			for (Root aRoot : roots) {
				if (aRoot.hasChanged()) {
					cond = true;
					break;
				}
			}
			// START KGU#874 2020-10-18: Issue #875 We must also check groups!
			if (!cond) {
				for (Group group : Arranger.getSortedGroups()) {
					if (!group.isDefaultGroup() && group.hasChanged()) {
						cond = true;
						break;
					}
				}
			}
			// END KGU#874 2020-10-18
		}
		return cond;
	}
	// END KGU#373 2017-03-28

	/*========================================
	 * setColor method
	 *========================================*/
	/**
	 * Sets the background colour of the selected element(s) to {@code _color}
	 * (undoable).
	 *
	 * @param _color - the colour to be applied.
	 */
	public void setColor(Color _color) {
		// START KGU#911 2021-01-10: Enh. #910
		//if (getSelected() != null)
		if (selected != null && !selected.isImmutable())
		// END KGU#911 2021-01-10
		{
			// START KGU#38 2016-01-11 Setting of colour wasn't undoable though recorded as change
			//root.addUndo();
			try {
				addUndoNSD(false);
			} catch (CancelledException e) {
				return;
			}
			// END KGU#38 2016-01-11
			selected.setColor(_color);
			//getSelected().setSelected(false);
			//selected=null;
			// START KGU#137 2016-01-11: Already prepared by addUndo()
			//root.hasChanged=true;
			// END KGU#137 2016-01-11
			redraw();
		}
	}


	/*========================================
	 * Copy method
	 *========================================*/
	/**
	 * Copies the selected element(s) either to the system clipboard (if a
	 * {@link Root} is selected or to an internal cache (otherwise).
	 *
	 * @see #canCopy()
	 * @see #cutNSD()
	 * @see #pasteNSD()
	 */
	public void copyNSD() {
		if (selected != null) {
			// START KGU#177 2016-04-14: Enh. #158 - Allow to copy a diagram via clipboard
			//eCopy = selected.copy();
			if (selected instanceof Root) {
				XmlGenerator xmlgen = new XmlGenerator();
				StringSelection toClip = new StringSelection(xmlgen.generateCode(root, "\t", false));
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(toClip, this);
			} else {
				eCopy = selected.copy();
			}
			// END KGU#177 2016-04-14
		}
	}

	/*========================================
	 * cut method
	 *========================================*/
	/**
	 * Copies the selected element(s) to an internal cache and then removes them
	 * from the current diagram (undoable). Does nothing if {@link #root} is
	 * selected.
	 *
	 * @see #canCut()
	 * @see #copyNSD()
	 * @see #pasteNSD()
	 */
	public void cutNSD() {
		// START KGU#911 2021-01-10: Enh. #910
		//if (selected != null && selected != root)
		if (selected != null && selected != root && !selected.isImmutable())
		// END KGU#911 2021-01-10
		{
			eCopy = selected.copy();
			// START KGU#182 2016-04-23: Issue #168	- pass the selection to the "next" element
			Element newSel = getSelectionHeir();
			// END KGU#182 2016-04-23
			eCopy.setSelected(false);
			//root.addUndo();
			try {
				addUndoNSD(false);
			} catch (CancelledException e) {
				return;
			}
			root.removeElement(selected);
			// START KGU#137 2016-01-11: Already prepared by addUndo()
			//root.hasChanged=true;
			// END KGU#137 2016-01-11
			redraw();
			// START KGU#182 2016-04-23: Issue #168	- pass the selection to the "next" element
			//selected=null;
			this.selected = newSel;
			if (newSel != null) {
				// START KGU#477 2017-12-06: Enh. #487 - consider hidden declaration sequences
				//newSel.setSelected(true);
				this.selected = newSel.setSelected(true);
				// END KGU#477 2017-12-06
			}
			// END KGU#182 2016-04-23
			analyse();
			// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
			adaptScrollUnits();
			// END KGU#444 2017-10-23
			// START KGU#705 2019-09-24: Enh. #738
			updateCodePreview();
			// END KGU#705 2019-09-24
		}
	}

	/*========================================
	 * paste method
	 *========================================*/
	/**
	 * Pastes the elements from the internal copy cache to to the next position
	 * after the currently selected element(s). Does nothing if the copy cache
	 * is empty or nor target elements are selected.
	 *
	 * @see #canPaste()
	 * @see #copyNSD()
	 * @see #cutNSD()
	 */
	public void pasteNSD() {
		if (selected != null && eCopy != null) {
			//root.addUndo();
			try {
				addUndoNSD(false);
			} catch (CancelledException e) {
				return;
			}
			// START KGU#477 2017-12-06: Enh, #487 - declaration stuff might be collapsed
			selected = selected.setSelected(true);
			// END KGU#477 2017-12-06
			selected.setSelected(false);
			Element nE = eCopy.copy();
			nE.setSelected(true);	// FIXME (KGU#87): Looks fine but is misleading with a pasted Subqueue
			// START KGU#477 2017-12-06: Enh, #487 - declaration stuff might be collapsed
			//root.addAfter(selected, nE);
			root.addAfter(getLastSelected(), nE);
			// END KGU#477 2017-12-06
			// START KGU#137 2016-01-11: Already prepared by addUndo()
			//root.hasChanged=true;
			// END KGU#137 2016-01-11
			// START KGU#87 2015-11-22: In case of a copied Subqueue the copy shouldn't be selected!
			//selected=nE;
			if (nE instanceof Subqueue) {
				// If the target was a Subqueue then it had been empty and contains all nE had contained,
				// hence we may leave it selected, otherwise the minimum risk is to clear the selection
				if (!(selected instanceof Subqueue)) {
					selected = null;
				}
				((Subqueue) nE).clear();
			} else {
				selected = nE;
			}
			// END KGU#87 2015-11-22
			redraw();
			analyse();
			// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
			adaptScrollUnits();
			// END KGU#444 2017-10-23
			// START KGU#705 2019-09-24: Enh. #738
			updateCodePreview();
			// END KGU#705 2019-09-24
		}
	}

	/*========================================
	 * edit method
	 *========================================*/
	/**
	 * Opens the editor for the currently selected element and applies the
	 * committed changes (undoable).
	 *
	 * @return true if the element was changed, false otherwise
	 * @see #canEdit()
	 */
	public boolean editNSD() {
		boolean modified = false;
		Element element = getSelected();
		if (element != null) {
			// START KGU#911 2021-01-11: Enh. #910 Avert changes on immutable diagrams
			boolean mayCommit = !element.isImmutable() && !element.isExecuted();
			// END KGU#911 2021-01-11
			if (element.getClass().getSimpleName().equals("Subqueue")) {
				// START KGU#911 2021-01-11: Enh. #910 Avert changes on immutable diagrams
				if (!mayCommit) {
					return modified;
				}
				// END KGU#911 2021-01-11

				EditData data = new EditData();
				data.title = "Add new instruction ...";

				// START KGU#42 2015-10-14: Enhancement for easier title localisation
				//showInputBox(data);
				showInputBox(data, "Instruction", true, true);
				// END KGU#42 2015-10-14

				if (data.result == true) {
					// START KGU#480 2018-01-21: Enh. #490 we have to replace DiagramController aliases by the original names
					//Element ele = new Instruction(data.text.getText());
					String text = Element.replaceControllerAliases(data.text.getText(), false, false);
					Element ele = new Instruction(text);
					// END KGU#480 2018-01-21
					ele.setComment(data.comment.getText());
					// START KGU#43 2015-10-17
					if (data.breakpoint) {
						ele.toggleBreakpoint();
					}
					// END KGU#43 2015-10-17
					// START KGU#277 2016-10-13: Enh. #270
					ele.setDisabled(data.disabled);
					// END KGU#277 2016-10-13
					// START KGU#213 2016-08-01: Enh. #215 (temporarily disabled again)
					//ele.setBreakTriggerCount(data.breakTriggerCount);
					// END KGU#213 2016-08-01
					//root.addUndo();
					try {
						addUndoNSD(false);
					} catch (CancelledException e) {
						return false;
					}
					// START KGU#137 2016-01-11: Already prepared by addUndo()
					//root.hasChanged=true;
					// END KGU#137 2016-01-11
					((Subqueue) element).addElement(ele);
					// START KGU#136 2016-03-01: Bugfix #97
					element.resetDrawingInfoUp();
					// END KGU#136 2016-03-01
					selected = ele.setSelected(true);
					modified = true;
					redraw();
				}
			} else {
				EditData data = new EditData();
				data.title = "Edit element ...";
				// START KGU#480 2018-01-21: Enh. #490 we have to replace DiagramController aliases by the original names
				//data.text.setText(element.getText().getText());
				data.text.setText(element.getAliasText().getText());
				// END KGU#480 2018-01-21
				data.comment.setText(element.getComment().getText());
				// START KGU#43 2015-10-12
				data.breakpoint = element.isBreakpoint();
				// END KGU#43 2015-10-12
				// START KGU#213 2016-08-01: Enh. #215
				data.breakTriggerCount = element.getBreakTriggerCount();
				// END KGU#213 2016-08-01				
				// START KGU#277 2016-10-13: Enh. #270
				data.disabled = element.isDisabled(true);
				// END KGU#277 2016-10-13

				// START KGU#3 2015-10-25: Allow more sophisticated For loop editing
				if (element instanceof For) {
					// START KGU#61 2016-03-21: Content of the branch outsourced
					preEditFor(data, (For) element);
					// END KGU#61 2016-03-21
				}
				// END KGU#3 2015-10-25
				// START KGU#363 2017-03-14: Enh. #372
				else if (element instanceof Root) {
					//data.authorName = ((Root)element).getAuthor();
					//data.licenseName = ((Root)element).licenseName;
					//data.licenseText = ((Root)element).licenseText;
					data.licInfo = new RootAttributes((Root) element);
					// START KGU#376 2017-07-01: Enh. #389
					data.diagramRefs = ((Root) element).includeList;
					// END KGU#376 2017-07-01
				}
				// END KGU#363 2017-03-14
				// START KGU#695 2021-01-22: Enh. #714
				else if (element instanceof Try) {
					data.showFinally = ((Try) element).isEmptyFinallyVisible();
				}
				// END KGU#695 2021-01-22
				// START KGU#927 2021-02-07: Enh. #915 Which branches contain elements?
				else if (element instanceof Case) {
					data.branchOrder = new int[((Case) element).qs.size()];
					for (int i = 0; i < data.branchOrder.length; i++) {
						if (((Case) element).qs.get(i).getSize() > 0) {
							data.branchOrder[i] = i + 1;
						} else {
							data.branchOrder[i] = 0;
						}
					}
				}
				// END KGU#927 2021-02-07

				// START KGU#42 2015-10-14: Enhancement for easier title localisation
				//showInputBox(data);
				// START KGU#946 2021-02-28: We could get wrong Root type information when induced from errorlist
				//showInputBox(data, element.getClass().getSimpleName(), false, mayCommit);
				String elemType = element.getClass().getSimpleName();
				if (element instanceof Root) {
					if (((Root) element).isSubroutine()) {
						elemType = "Function";
					} else if (((Root) element).isInclude()) {
						elemType = "Includable";
					}
				}
				showInputBox(data, elemType, false, mayCommit);
				// END KGU#946 2021-02-28
				// END KGU#42 2015-10-14

				if (data.result == true) {
					// START KGU#120 2016-01-02: Bugfix #85 - StringList changes of Root are to be undoable, too!
					//if (!element.getClass().getSimpleName().equals("Root"))
					// END KGU#120 2016-01-02
					// START KGU#363 2017-05-21: Enh. #372:
					// Also cache root attributes if the edited element is a Root
					//root.addUndo();
					// START KGU#684 2019-06-13: Bugfix #728 - we have to check more here
					//root.addUndo(element instanceof Root);
					try {
						this.addUndoNSD(element instanceof Root);
					} catch (CancelledException e) {
						return false;
					}
					// END KGU#684 2019-06-13
					// END KGU#363 2017-05-21
					// START KGU#916 2021-01-24: Enh. #915 We may preserve branch associations now
					// This must be done before the text is updated!
					if (element instanceof Case) {
						((Case) element).reorderBranches(data.branchOrder);
					}
					// END KGU#916 2021-01-24
					if (!(element instanceof Forever)) {
						// START KGU#480 2018-01-21: Enh. #490 we have to replace DiagramController aliases by the original names
						//element.setText(data.text.getText());
						element.setAliasText(data.text.getText());
						// END KGU#480 2018-01-21
					}
					element.setComment(data.comment.getText());
					// START KGU#43 2015-10-12
					if (element.isBreakpoint() != data.breakpoint) {
						element.toggleBreakpoint();
					}
					// END KGU#43 2015-10-12
					// START KGU#213 2016-08-01: Enh. #215
					//element.setBreakTriggerCount(data.breakTriggerCount);
					// END KGU#213 2016-08-01
					// START KGU#277 2016-10-13: Enh. #270
					element.setDisabled(data.disabled);
					// END KGU#277 2016-10-13
					// START KGU#3 2015-10-25
					if (element instanceof For) {
						// START KGU#61 2016-03-21: Content of the branch outsourced
						postEditFor(data, (For) element);
						// END KGU#61 2016-03-21
					}
					// END KGU#3 2015-10-25
					// START KGU#363 2017-03-14: Enh. #372
					else if (element instanceof Root) {
						((Root) element).adoptAttributes(data.licInfo);
						// START KGU#376 2017-07-01: Enh. #389
						((Root) element).includeList = data.diagramRefs;
						// END KGU#376 2017-07-01
					}
					// END KGU#363 2017-03-14
					// START KGU#695 2021-01-22: Enh. #714
					else if (element instanceof Try) {
						((Try) element).setEmptyFinallyVisible(data.showFinally);
					}
					// END KGU#695 2021-01-22
					// START KGU#137 2016-01-11: Already prepared by addUndo()
					//root.hasChanged=true;
					// END KGU#137 2016-01-11
					// START KGU#136 2016-03-01: Bugfix #97
					element.resetDrawingInfoUp();
					// END KGU#136 2016-03-01
					modified = true;
					redraw();
				}
			}

			analyse();
			// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
			adaptScrollUnits();
			// END KGU#444 2017-10-23
			// START KGU#705 2019-09-23: Enh. #738
			if (modified) {
				this.updateCodePreview();
			}
			// END KGU#705 2019-09-23
		}
		return modified;
	}

	private void preEditFor(EditData _data, For _for) {
		// Cache the style - we temporarily modify it to get all information
		For.ForLoopStyle style = _for.style;
		try {
			_for.style = For.ForLoopStyle.COUNTER;
			_data.forParts.add(_for.getCounterVar());
			// START KGU#480 2018-01-22: Enh. #490 - maybe aliases are to be put in
			//_data.forParts.add(_for.getStartValue());
			//_data.forParts.add(_for.getEndValue());
			_data.forParts.add(Element.replaceControllerAliases(_for.getStartValue(), true, false));
			_data.forParts.add(Element.replaceControllerAliases(_for.getEndValue(), true, false));
			// END KGU#480 2018-01-22
			_data.forParts.add(Integer.toString(_for.getStepConst()));
		} catch (Exception ex) {
		} finally {
			// Ensure the original style is restored
			_data.forLoopStyle = _for.style = style;
		}
		// Now try to get a value list in case it's a FOR-IN loop
		String valueList = _for.getValueList();
		if (valueList != null) {
			// START KGU#480 2018-01-22: Enh. #490 - maybe aliases are to be put in
			valueList = Element.replaceControllerAliases(valueList, true, false);
			// END KGU#480 2018-01-22
			_data.forParts.add(valueList);
		}
	}

	private void postEditFor(EditData _data, For _for) {
		_for.style = _data.forLoopStyle;

		_for.setCounterVar(_data.forParts.get(0));
		// START KGU#480 2018-01-22: Enh. #490 - maybe aliases are to be replaced
		//_for.setStartValue(_data.forParts.get(1));
		//_for.setEndValue(_data.forParts.get(2));
		_for.setStartValue(Element.replaceControllerAliases(_data.forParts.get(1), false, false));
		_for.setEndValue(Element.replaceControllerAliases(_data.forParts.get(2), false, false));
		// END KGU#480 2018-01-22
		_for.setStepConst(_data.forParts.get(3));

		// FOR-IN loop support
		if (_for.style == For.ForLoopStyle.TRAVERSAL) {
			// START KGU#61 2016-09-24: Seemed to be nonsense
			//_for.style = For.ForLoopStyle.FREETEXT;
			//_for.setValueList(_for.getValueList());
			//_for.style = For.ForLoopStyle.TRAVERSAL;
			// START KGU#480 2018-01-22: Enh. #490 - maybe aliases are to be replaced
			//_for.setValueList(_data.forParts.get(4));
			_for.setValueList(Element.replaceControllerAliases(_data.forParts.get(4), false, false));
			// END KGU#480 2018-01-22
			// END KGU#61 2016-09-24
		}
		// START KGU#61 2016-09-24
		else {
			_for.setValueList(null);
		}
		// END KGU#61 2016-09-24
		/* START KGU 2018-01-22: This code differed from that in addNewElement
		 * with respect to the following statement missing here so far, which
		 * seemed to make sense in case of inconsistency, though. So it was added
		 * as part of the unification - it forces FREETEXT flavour in due cases.
		 */
		_for.style = _for.classifyStyle();
		/* END KGU 2018-01-22 */

	}

	/*========================================
	 * moveUp method
	 *========================================*/
	/**
	 * Moves the selected element(s) one position up in the current diagram
	 * (undoable).
	 *
	 * @see Element#canMoveUp()
	 */
	public void moveUpNSD() {
		//root.addUndo();
		try {
			addUndoNSD(false);
		} catch (CancelledException e) {
			return;
		}
		root.moveUp(getSelected());
		redraw();
		analyse();
		// START KGU#705 2019-09-23: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-09-23
	}

	/*========================================
	 * moveDown method
	 *========================================*/
	/**
	 * Moves the selected element(s) one position down in the current diagram
	 * (undoable).
	 *
	 * @see Element#canMoveDown()
	 */
	public void moveDownNSD() {
		//root.addUndo();
		try {
			addUndoNSD(false);
		} catch (CancelledException e) {
			return;
		}
		root.moveDown(getSelected());
		redraw();
		analyse();
		// START KGU#705 2019-09-23: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-09-23
	}

	/*========================================
	 * delete method
	 *========================================*/
	/**
	 * Removes the selected element(s) fro the current diagram (undoable).
	 *
	 * @see #canCut()
	 */
	public void deleteNSD() {
		//root.addUndo();
		try {
			addUndoNSD(false);
		} catch (CancelledException e) {
			return;
		}
		// START KGU#181 2016-04-19: Issue #164	- pass the selection to the "next" element
		Element newSel = getSelectionHeir();
		// END KGU#181 2016-04-19
		root.removeElement(getSelected());
		// START KGU#138 2016-01-11: Bugfix #102 - selection no longer valid
		// START KGU#181 2016-04-19: Issue #164	- pass the selection to the "next" element
		//this.selected = null;
		this.selected = newSel;
		if (newSel != null) {
			// START KGU#477 2017-12-06: Enh. #487 - consider hidden declaration sequences
			//newSel.setSelected(true);
			this.selected = newSel.setSelected(true);
			// END KGU#477 2017-12-06
		}
		// END KGU#181 2016-04-19
		// END KGU#138 2016-01-11
		redraw();
		analyse();
		// START KGU#138 2016-01-11: Bugfix#102 - disable element-based buttons
		this.NSDControl.doButtons();
		// END KGU#138 2016-01-11
		// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
		adaptScrollUnits();
		// END KGU#444 2017-10-23
		// START KGU#705 2019-09-23: Enh. #738
		this.updateCodePreview();
		// END KGU#705 2019-09-23
	}

	// START KGU#181 2016-04-19: Issue #164	- pass the selection to the "next" element
	/**
	 * Returns the element "inheriting" the selection from the doomed currently
	 * selected element This will be (if existent): 1. successor within
	 * Subqueue, 2. predecessor within Subqueue Requires this.selected to be
	 * neither Root nor an empty Subqueue.
	 *
	 * @return the element next the currently selected one, null if there is no
	 * selection
	 */
	private Element getSelectionHeir() {
		Element heir = null;
		if (selected != null && !(selected instanceof Root)) {
			Subqueue sq = (Subqueue) ((selected instanceof Subqueue) ? selected : selected.parent);
			int ixHeir = -1;
			if (selected instanceof SelectedSequence) {
				// Last element of the subsequence
				Element last = ((SelectedSequence) selected).getElement(((SelectedSequence) selected).getSize() - 1);
				Element frst = ((SelectedSequence) selected).getElement(0);
				int ixLast = sq.getIndexOf(last);	// Actual index of the last element in the Subqueue
				int ixFrst = sq.getIndexOf(frst);	// Actual index of the first element in the Subqueue
				if (ixLast < sq.getSize() - 1) {
					ixHeir = ixLast + 1;
				} else if (ixFrst > 0) {
					ixHeir = ixFrst - 1;
				}
			} else if (!(selected instanceof Subqueue)) {
				int ixEle = sq.getIndexOf(selected);
				if (ixEle < sq.getSize() - 1) {
					ixHeir = ixEle + 1;
				} else {
					ixHeir = ixEle - 1;
				}
			}
			if (ixHeir >= 0) {
				heir = sq.getElement(ixHeir);
			} else {
				// Empty Subqueue remnant will take over selection
				heir = sq;
			}
		}
		return heir;
	}
	// END KGU#181 2016-04-19

	// START KGU#123 2016-01-03: Issue #65, for new buttons and menu items
	/*========================================
	 * collapse method
	 *========================================*/
	/**
	 * Marks the selected element(s) as collapsed such that they are represented
	 * by a surrogate the size of a simple instruction on drawing.
	 *
	 * @see #expandNSD()
	 */
	public void collapseNSD() {
		getSelected().setCollapsed(true);
		redraw();
		analyse();
		this.NSDControl.doButtons();
		// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
		adaptScrollUnits();
		// END KGU#444 2017-10-23
	}

	/*========================================
	 * expand method
	 *========================================*/
	/**
	 * Removes the collapsed flag from the selected elements such that they get
	 * represented in their usual shape again on drawing the diagram.
	 *
	 * @see #collapseNSD()
	 */
	public void expandNSD() {
		getSelected().setCollapsed(false);
		redraw();
		analyse();
		this.NSDControl.doButtons();
		// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
		adaptScrollUnits();
		// END KGU#444 2017-10-23
	}
	// END KGU#123 2016-01-03

	// START KGU#277 2016-10-13: Enh. #270
	/*========================================
	 * disable method
	 *=======================================*/
	/**
	 * Flips the disabled flag on the selected element(s). If several elements
	 * are selected, some of which are disabled while others are not, then
	 * disables all of them. To disable an element means that it gets handled
	 * like a comment on execution and export (undoable).
	 */
	public void disableNSD() {
		// START KGU#911 2021-01-10: Enh. 910
		if (selected != null && selected.isImmutable()) {
			return;
		}
		// END KGU#911 2021-01-10
		boolean allDisabled = true;
		//root.addUndo();
		try {
			addUndoNSD(false);
		} catch (CancelledException e) {
			return;
		}
		if (getSelected() instanceof IElementSequence) {
			IElementSequence elements = (IElementSequence) getSelected();
			for (int i = 0; allDisabled && i < elements.getSize(); i++) {
				allDisabled = elements.getElement(i).isDisabled(true);
			}
			elements.setDisabled(!allDisabled);
		} else {
			getSelected().setDisabled(!getSelected().isDisabled(true));
		}

		redraw();
		analyse();
		this.NSDControl.doButtons();
		// START KGU#705 2019-09-23: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-09-23
	}
	// END KGU#277 2016-10-13

	/*========================================
	 * add method
	 *========================================*/
	/**
	 * Opens the element editor for the new given {@link Element} {@code _ele}
	 * and inserts the edited element after (if {@code _after} is true) or
	 * before (otherwise) the selected element(s) unless the user cancels the
	 * action.
	 *
	 * @param _ele - the new {@link Element} to be processed
	 * @param _title - proposed title for the editor (may be overridden by the
	 * localisation mechanism
	 * @param _pre - the default prefix text as configured in the Structure
	 * Preferences
	 * @param _after - whether {@code _ele} is to be inserted after (true) or
	 * before (false) the selected element.
	 */
	public void addNewElement(Element _ele, String _title, String _pre, boolean _after) {
		if (getSelected() != null) {
			EditData data = new EditData();
			data.title = _title;
			data.text.setText(_pre);
			// START KGU 2015-10-14: More information to ease title localisation
			//showInputBox(data);
			showInputBox(data, _ele.getClass().getSimpleName(), true, true);
			// END KGU 2015-10-14
			if (data.result == true) {
				if (!(_ele instanceof Forever)) {
					// START KGU#480 2018-01-21: Enh. #490 we have to replace DiagramController aliases by the original names
					//_ele.setText(data.text.getText());
					_ele.setAliasText(data.text.getText());
					// END KGU#480 2018-01-21
				}
				_ele.setComment(data.comment.getText());
				// START KGU 2015-10-17
				if (_ele.isBreakpoint() != data.breakpoint) {
					_ele.toggleBreakpoint();
				}
				// END KGU 2015-10-17
				// START KGU#277 2016-10-13: Enh. #270
				_ele.setDisabled(data.disabled);
				// END KGU#277 2016-10-13
				// START KGU#213 2016-08-01: Enh. #215
				//_ele.setBreakTriggerCount(data.breakTriggerCount);
				// END KGU#213 2016-08-01
				// START KGU#3 2015-10-25
				if (_ele instanceof For) {
/* START KGU 2018-01-22: The only difference of this code to postEditorFor(_data, (For)_ele)
 * was the way the style information and the value list were set - it was difficult to say
 * which way was the better one.
 */
//					((For)_ele).setCounterVar(data.forParts.get(0));
//					// START KGU#480 2018-01-22: Enh. #490 we have to replace DiagramController aliases by the original names
//					//((For)_ele).setStartValue(data.forParts.get(1));
//					//((For)_ele).setEndValue(data.forParts.get(2));
//					((For)_ele).setStartValue(Element.replaceControllerAliases(
//							data.forParts.get(1), false, false));
//					((For)_ele).setEndValue(Element.replaceControllerAliases(
//							data.forParts.get(2), false, false));
//					// END KGU#480 2018-01-22
//					((For)_ele).setStepConst(data.forParts.get(3));
//					// START KGU#61 2016-03-21: Enh. #84 - consider FOR-IN loops as well
//					//((For)_ele).isConsistent = ((For)_ele).checkConsistency();
//					// START KGU#480 2018-01-22: Enh. #490 we have to replace DiagramController aliases by the original names
//					//((For)_ele).setValueList(data.forParts.get(4));
//					((For)_ele).setValueList(Element.replaceControllerAliases(
//							data.forParts.get(4), false, false));
//					// END KGU#480 2018-01-22
//					((For)_ele).style = ((For)_ele).classifyStyle();
//					// END KGU#61 2016-03-21
					this.postEditFor(data, (For) _ele);
/* END KGU 2018-01-22 */
				}
				// END KGU#3 2015-10-25
				// START KGU#695 2021-01-22: Enh. #714
				else if (_ele instanceof Try) {
					((Try) _ele).setEmptyFinallyVisible(data.showFinally);
				}
				// END KGU#695 2021-01-22
				//root.addUndo();
				try {
					addUndoNSD(false);
				} catch (CancelledException e) {
					return;
				}
				selected.setSelected(false);
				if (_after == true) {
					// START KGU#477 2017-12-06: Enh. #487
					//root.addAfter(getSelected(),_ele);
					root.addAfter(getLastSelected(), _ele);
					// END KGU#477 2017-12-06
				} else {
					// START KGU#477 2017-12-06: Enh. #487
					//root.addBefore(getSelected(),_ele);
					root.addBefore(getFirstSelected(), _ele);
					// END KGU#477 2017-12-06
				}
				selected = _ele.setSelected(true);
				// START KGU#272 2016-10-06: Bugfix #262
				selectedDown = selected;
				// END KGU#272 2016-10-06
				redraw();
				analyse();
				// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
				adaptScrollUnits();
				// END KGU#444 2017-10-23
				// START KGU#705 2019-09-23: Enh. #738
				updateCodePreview();
				// END KGU#705 2019-09-23
			}
		}
	}

	/*========================================
	 * subroutine derivation method(s)
	 *========================================*/
	// START KGU#365 2017-03-19: Enh. #380 - perform the possible conversion
	/**
	 * Extracts the select element(s) from the diagram, moves them to an
	 * interactively specified new subroutine diagram and replaces them by a
	 * {@link Call} for the latter (undoable).
	 */
	public void outsourceNSD() {
		if (this.selected != null) {
			IElementSequence elements = null;
			if (!this.selectedIsMultiple()) {
				elements = new SelectedSequence(this.selected, this.selected);
			} else {
				elements = (IElementSequence) this.selected;
			}
			// START KGU#365 2017-04-14: We must at least warn if return or unmatched leave instructions are contained
			List<Jump> jumps = findUnsatisfiedJumps(elements);
			if (!jumps.isEmpty()) {
				String jumpTexts = "";
				for (Jump jmp : jumps) {
					String jumpLine = jmp.getUnbrokenText().getLongString().trim();
					if (jumpLine.isEmpty()) {
						jumpLine = "(" + CodeParser.getKeywordOrDefault("preLeave", "leave") + ")";
					}
					jumpTexts += "\n \u25CF " + jumpLine;
				}
				Element.troubleMakers.addAll(jumps);
				int answer = JOptionPane.YES_OPTION;
				try {
					redraw();
					String[] options = new String[]{Menu.lblContinue.getText(), Menu.lblCancel.getText()};
					answer = JOptionPane.showOptionDialog(this.getFrame(),
							Menu.msgJumpsOutwardsScope.getText().replace("%", jumpTexts),
							Menu.msgTitleWarning.getText(),
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
							null,
							options,
							options[1]
							);
				} finally {
					Element.troubleMakers.clear();
					redraw();
				}
				if (answer != JOptionPane.YES_OPTION) {
					return;
				}
			}
			// END KGU#365 2017-04-14
			String hint = Menu.msgMustBeIdentifier.getText();
			String prompt1 = Menu.msgSubroutineName.getText() + ": ";
			String prompt = prompt1;
			String subroutineName = null;
			do {
				subroutineName = JOptionPane.showInputDialog(prompt);
				prompt = hint + "\n" + prompt1;
			} while (subroutineName != null && !Function.testIdentifier(subroutineName, false, null));
			if (subroutineName != null) {
				try {
					addUndoNSD(false);
				} catch (CancelledException e) {
					return;
				}
				selected.setSelected(false);
				// START KGU#506 2018-03-14: issue #522 - we need to check for record types
				HashMap<String, TypeMapEntry> parentTypes = root.getTypeInfo();
				// END KGU#506 2018-03-14
				// START KGU#626 2019-01-06: Enh. #657
				// Detect all groups root is member of such that we can associate the subroutine to them
				Collection<Group> groups = null;
				if (Arranger.hasInstance()) {
					groups = Arranger.getInstance().getGroupsFromRoot(root, true);
				}
				// END KGU#626 2019-01-06
				// START KGU#638 2019-01-20: Issue #668 - If root is not member of a group then push it to Arranger
				String targetGroupName = null;
				if (groups == null || groups.isEmpty() && Arranger.getInstance().getGroupsFromRoot(root, false).isEmpty()) {
					// If the diagram is a program then create an exclusive group named after the main diagram 
					if (root.isProgram()) {
						targetGroupName = root.getMethodName(true);
						Arranger.getInstance().addToPool(root, this.getFrame(), targetGroupName);
						groups = Arranger.getInstance().getGroupsFromRoot(root, true);
					} else {
						Arranger.getInstance().addToPool(root, this.getFrame());
					}
				} else if (Arranger.getInstance().getGroupsFromRoot(root, false).size() == groups.size()) {
					// Parent diagram is arranged but not member of the default group - then its children shouldn't be either
					targetGroupName = groups.iterator().next().getName();
				}
				// END KGU#638 2019-01-20
				// FIXME May we involve the user in argument and result value identification?
				Root sub = root.outsourceToSubroutine(elements, subroutineName, null);
				if (sub != null) {
					// adopt presentation properties from root
					//sub.highlightVars = Element.E_VARHIGHLIGHT;
					sub.isBoxed = root.isBoxed;
					// START KGU#506 2018-03-14: issue #522 - we need to check for record types
					//sub.getVarNames();	// just to prepare proper drawing.
					StringList subVars = sub.retrieveVarNames();
					prepareArgTypesForSub(parentTypes, groups, targetGroupName, sub, subVars);
					// END KGU#506 2018-03-14
					sub.setChanged(false);	// The argument false does NOT mean to reset the changed flag!
					Arranger arr = Arranger.getInstance();
					// START KGU#638 2019-01-20: Issue #668 - Improved group association behaviour
					//arr.addToPool(sub, NSDControl.getFrame());
					arr.addToPool(sub, NSDControl.getFrame(), targetGroupName);
					// END KGU#638 3019-01-20
					// START KGU#626 2019-01-06: Enh. #657
					// Associate the subroutine to all groups root is member of
					if (groups != null) {
						for (Group group : groups) {
							Arranger.getInstance().attachRootToGroup(group, sub, null, this.getFrame());
						}
					}
					// END KGU#626 2019-01-06
					arr.setVisible(true);
				} else {
					// Something failed, so undo the temporary changes without redo option
					root.undo(false);
				}
				selected.setSelected(true);
				redraw();
				analyse();
				// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
				adaptScrollUnits();
				// END KGU#444 2017-10-23
				// START KGU#705 2019-09-23: Enh. #738
				updateCodePreview();
				// END KGU#705 2019-09-23
			}
		}
	}

	/**
	 * Retrieves the types for subroutine variables {@code subVars} from the
	 * type map {@code parentTypes} of the calling routine and adopts or
	 * implants required includables.
	 *
	 * @param parentTypes - type map of the calling routine
	 * @param groups - Arranger groups of the calling routine (for Includable
	 * implantation)
	 * @param targetGroupName - name of the target group for the routine
	 * @param sub - the subroutine (its includeList may be modified)
	 * @param subVars - the interesting variables of the routine
	 * @return a StringList of type names in order of {@code subVars}.
	 */
	private StringList prepareArgTypesForSub(HashMap<String, TypeMapEntry> parentTypes,
			Collection<Group> groups, String targetGroupName, Root sub, StringList subVars) {
		// START KGU#921 2021-01-30: Bugfix #921 we must ensure topological ordering
		//HashMap<String, Element> sharedTypesMap = new HashMap<String, Element>();
		HashMap<String, Element> sharedTypesMap = new LinkedHashMap<String, Element>();
		// END KGU#921 2021-01-30
		StringList typeNames = new StringList();
		for (int i = 0; i < subVars.count(); i++) {
			String typeName = "";
			TypeMapEntry varType = null;
			String varName = subVars.get(i);
			if (Function.testIdentifier(varName, false, "")) {
				varType = parentTypes.get(varName);
				if (varType != null) {
					typeName = varType.getCanonicalType(true, true);
				}
				// START KGU#864 2020-04-28: Bugfix #865
				else if (varName.equals("true") || varName.contentEquals("false")) {
					typeName = "boolean";
				}
				// END KGU#864 2020-04-28
			} else {
				typeName = Element.identifyExprType(parentTypes, varName, true);
				if (!typeName.isEmpty()) {
					varType = parentTypes.get(":" + typeName);
				}
			}
			// START KGU#921 2021-01-30: Bugfix #921 Had to be recursive!
			//if (varType != null && varType.isRecord()) {
			//	Element defining = varType.getDeclaringElement();
			//	if (defining != null) {
			//		Root typeSource = Element.getRoot(defining); 
			//		if (typeSource == root) {
			//			sharedTypesMap.putIfAbsent(varType.typeName, defining);
			//		}
			//		else if (typeSource != null) {
			//			sub.addToIncludeList(typeSource);
			//		}
			//	}
			//}
			gatherSharedTypes(sub, sharedTypesMap, varType, parentTypes);
			// END KGU#921 2021-01-30
			typeNames.add(typeName);
		}
		if (!sharedTypesMap.isEmpty()) {
			// FIXME: We might also offer a combo box containing the already included diagrams of root
			String hint = Menu.msgMustBeIdentifier.getText() + "\n";
			String prompt1 = Menu.msgIncludableName.getText() + ": ";
			String prompt = prompt1;
			String includableName = null;
			do {
				includableName = JOptionPane.showInputDialog(prompt);
				prompt = hint + prompt1;
			} while (includableName == null || !Function.testIdentifier(includableName, false, null));
			Root incl = null;
			if (Arranger.hasInstance()) {
				Vector<Root> includes = Arranger.getInstance().findIncludesByName(includableName, root, false);
				if (!includes.isEmpty()) {
					incl = includes.firstElement();
					incl.addUndo();
				}
			}
			boolean isNewIncl = incl == null;
			if (isNewIncl) {
				incl = new Root();
				incl.setText(includableName);
				incl.setInclude(true);
				// adopt presentation properties from root
				//incl.highlightVars = Element.E_VARHIGHLIGHT;
				incl.isBoxed = root.isBoxed;
			}
			for (Element source : sharedTypesMap.values()) {
				((Subqueue) source.parent).removeElement(source);
				incl.children.addElement(source);
			}
			incl.setChanged(false);	// The argument false does NOT mean to reset the changed flag!
			if (isNewIncl) {
				// START KGU#638 2019-01-20: Issue #668 - Improved group association behaviour
				//Arranger.getInstance().addToPool(incl, NSDControl.getFrame());
				Arranger.getInstance().addToPool(incl, NSDControl.getFrame(), targetGroupName);
				// END KGU#638 3019-01-20
			}
			// START KGU#626 2019-01-06: Enh. #657
			// Associate the includable to all groups root is member of
			if (groups != null) {
				for (Group group : groups) {
					Arranger.getInstance().attachRootToGroup(group, incl, null, this.getFrame());
				}
			}
			// END KGU#626 2019-01-06
			root.addToIncludeList(includableName);
			sub.addToIncludeList(includableName);
		}
		return typeNames;
	}
	// END KGU#365 2017-03-19

	// START KGU#921 2021-01-30: Bugfix #921
	/**
	 * Recursively gathers the underlying complex (i.e. definition-mandatory)
	 * types the subroutine {@code sub} depends on together with their defining
	 * elements if retrievable.
	 *
	 * @param sub - a new subroutine diagram
	 * @param sharedTypesMap - the map of types assumed necessarily to be
	 * shared, may be enhanced here
	 * @param varType - a definitely referred type
	 * @param parentTypeMap - the type map of the calling diagram,
	 */
	private void gatherSharedTypes(Root sub, HashMap<String, Element> sharedTypesMap, TypeMapEntry varType, HashMap<String, TypeMapEntry> parentTypeMap) {
		if (varType != null) {
			if (varType.isRecord() || varType.isEnum()) {
				// Ensure a topological order of types by post-order traversal
				if (varType.isRecord()) {
					for (TypeMapEntry subType : varType.getComponentInfo(true).values()) {
						gatherSharedTypes(sub, sharedTypesMap, subType, parentTypeMap);
					}
				}
				Element defining = varType.getDeclaringElement();
				if (defining != null) {
					Root typeSource = Element.getRoot(defining);
					if (typeSource == root) {
						sharedTypesMap.putIfAbsent(varType.typeName, defining);
					} else if (typeSource != null) {
						sub.addToIncludeList(typeSource);
					}
				}
			} else if (varType.isArray()) {
				// Try to fetch the element type
				String typeDescr = varType.getCanonicalType(true, false);
				int i = 0;
				while (i < typeDescr.length() && typeDescr.charAt(i) == '@') {
					i++;
				}
				typeDescr = typeDescr.substring(i);
				if (Function.testIdentifier(typeDescr, false, null)
						&& (varType = parentTypeMap.get(":" + typeDescr)) != null) {
					gatherSharedTypes(sub, sharedTypesMap, varType, parentTypeMap);
				}
			}
		}
	}
	// END KGU#921 2021-01-30

	// START KGU#365 2017-04-14: Enh. #380
	/**
	 * Retrieves all {@link Jump} elements within the span of {@code elements}
	 * trying to leave outside the span.
	 */
	private List<Jump> findUnsatisfiedJumps(IElementSequence elements) {
		final class JumpFinder implements IElementVisitor {

			private Subqueue scope = null;
			private List<Jump> foundJumps = new LinkedList<Jump>();

			public JumpFinder(Subqueue scope) {
				this.scope = scope;
			}

			public List<Jump> getJumps() {
				return foundJumps;
			}

			@Override
			public boolean visitPreOrder(Element _ele) {
				if (_ele instanceof Jump) {
					Jump jmp = (Jump) _ele;
					if (jmp.isReturn() || jmp.isLeave() && jmp.getLeftLoop(scope) == null) {
						this.foundJumps.add(jmp);
					}
				}
				return true;
			}

			@Override
			public boolean visitPostOrder(Element _ele) {
				return true;
			}

		}

		Subqueue scope = elements.getSubqueue();
		JumpFinder finder = new JumpFinder(scope);
		scope.traverse(finder);
		return finder.getJumps();

	}
	// END KGU#65 2017-04-14

	// START KGU#667 2019-02-26: Enh. #689
	/**
	 * @return true if the selected element is a {@link Call} and a called
	 * routine signature can be extracted or if the selected element is a
	 * {@link Root} and its include list is not empty.
	 * @see #editSubNSD()
	 */
	public boolean canEditSub() {
		boolean canEdit = false;
		if (selected != null && selected instanceof Call) {
			Function called = ((Call) selected).getCalledRoutine();
			// We don't want to open an editor in case of a recursive call.
			canEdit = (called != null && !(called.getSignatureString().equals(root.getSignatureString(false, false))));
		}
		// START KGU#770 2021-01-27: Enh. #917 Also support Includables
		else if (selected != null && selected instanceof Root) {
			canEdit = ((Root) selected).includeList != null
					&& !((Root) selected).includeList.isEmpty();
		}
		// END KGU#770 2021-01-27
		return canEdit;
	}

	/**
	 * Summons the called subroutine of the selected {@link Call} into a
	 * {@link Mainfom} instance, possibly opens a new one. May instead offer a
	 * choice list of Includable names if the selected element is {@link Root}
	 * with non-empty include list an then summon the selected Includable in the
	 * same way.
	 *
	 * @see #canEditSub()
	 */
	public void editSubNSD() {
		// START KGU#981 2021-09-18: Bugfix #983 Undue modifications of already existing diagrams
		boolean isNew = false;
		// END KGU#981 2021-09-18
		// START KGU#770 2021-01-27: Enh. #917
		Root referredRoot = null;
		String targetGroupName = null;	// This will be relevant for a new diagram
		Collection<Group> myGroups = null;
		// END KGU#770 2021-01-27
		
		// 1. Take care of subroutine calls
		if (selected instanceof Call && this.canEditSub()) {
			Call call = (Call) selected;
			Function called = call.getCalledRoutine();
			// START KGU#770 2021-01-27: Enh. #917
			//Root referredRoot = null;
			// END KGU#770 2021-01-27
			// Try to find the subroutine in Arranger
			if (Arranger.hasInstance()) {
				Vector<Root> candidates = Arranger.getInstance()
						.findRoutinesBySignature(called.getName(), called.paramCount(), root, false);
				// Open a choice list if the group approach alone wasn't successful
				referredRoot = chooseReferredRoot(candidates, Menu.msgChooseSubroutine.getText());
			}
			// START KGU#770 2021-01-27: Enh. #917
			//String targetGroupName = null;	// This will be relevant for a new subroutine
			// END KGU#770 2021-01-27
			// Create new subroutine root if we haven't been able to select an existing one
			if (referredRoot == null) {
				if (JOptionPane.showConfirmDialog(getFrame(),
						Menu.msgCreateSubroutine.getText().replace("%", called.getSignatureString()),
						Menu.msgTitleQuestion.getText(),
						JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
					return;
				}
				referredRoot = new Root();
				myGroups = Arranger.getInstance().getGroupsFromRoot(root, true);
				StringList params = new StringList();
				for (int i = 0; i < called.paramCount(); i++) {
					String param = called.getParam(i);
					params.add(param);
				}
				// START KGU#744 2019-10-05: Issue #758 - retrieve argument types and care for shared types 
				StringList argTypes = this.prepareArgTypesForSub(root.getTypeInfo(), myGroups, targetGroupName, referredRoot, params);
				String paramSeparator = ", ";
				for (int i = 0; i < params.count(); i++) {
					String typeName = argTypes.get(i).replace("@", "array of ");
					// START KGU#864 2020-04-28: Bugfix #865
					//if (!Function.testIdentifier(params.get(i), "")) {
					String param = params.get(i);
					if (!Function.testIdentifier(param, false, "") || param.equals("true") || param.equals("false")) {
					// END KGU#864 2020-04-28
						params.set(i, param = ("param" + (i + 1)));
					}
					if (!typeName.isEmpty() && !typeName.equals("???")) {
						params.set(i, param + ": " + typeName);
						paramSeparator = "; ";
					}
				}
				// END KGU#744 2019-10-05
				String result = "";
				if (((Call) selected).isFunctionCall(false)) {
					StringList lineTokens = Element.splitLexically(call.getUnbrokenText().get(0), true);
					lineTokens.removeAll(" ");
					String var = Call.getAssignedVarname(lineTokens, true);
					if (Function.testIdentifier(var, false, null)) {
						TypeMapEntry typeEntry = root.getTypeInfo().get(var);
						result = typeEntry.getCanonicalType(true, true).replace("@", "array of ");
						if (result == null) {
							result = "";
						}
					}
					if (!result.trim().isEmpty()) {
						result = ": " + result.trim();
					} else {
						result = ": ???";
					}
				}
				referredRoot.setText(called.getName() + "(" + params.concatenate(paramSeparator) + ")" + result);
				referredRoot.setProgram(false);
				// START KGU#981 2021-09-18: Bugfix #983 Undue modifications of already existing diagrams
				isNew = true;
				// END KGU#981 2021-09-18
			}
		// START KGU#770 2021-01-27: Enh. #917
		}
		// 2. Take care of referenced includable diagrams
		else if (selected instanceof Root && this.canEditSub()) {
			StringList includeNames = ((Root)selected).includeList;
			if (root.isInclude() && includeNames.contains(root.getMethodName())) {
				root.addUndo();
				includeNames.removeAll(root.getMethodName());
				if (includeNames.isEmpty()) {
					JOptionPane.showMessageDialog(getFrame(),
							Menu.msgCyclicInclusion.getText(),
							Menu.msgTitleWarning.getText(),
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
			String inclName = null;
			if (includeNames.count() > 1) {
				inclName = (String) JOptionPane.showInputDialog(getFrame(),
						Menu.msgChooseIncludable.getText(),
						Menu.msgTitleQuestion.getText(),
						JOptionPane.QUESTION_MESSAGE, null, // Use default icon
						includeNames.toArray(), // Array of choices
						includeNames.get(0));	// Initial choice
				if (inclName == null) {
					return;
				}
			} else {
				inclName = includeNames.get(0);
			}
			// Try to find the Includable in Arranger
			Vector<Root> candidates = Arranger.getInstance()
					.findIncludesByName(inclName, (Root) selected, false);
			// Prevent cyclic inclusion
			candidates.remove(root);
			// Open a choice list if the group approach alone wasn't successful
			referredRoot = chooseReferredRoot(candidates, Menu.msgChooseIncludable.getText());
			// Create new subroutine root if we haven't been able to select an existing one
			if (referredRoot == null) {
				if (JOptionPane.showConfirmDialog(getFrame(),
						Menu.msgCreateIncludable.getText().replace("%", inclName),
						Menu.msgTitleQuestion.getText(),
						JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
					return;
				}
				referredRoot = new Root();
				referredRoot.setText(inclName);
				referredRoot.setInclude();
				// START KGU#981 2021-09-18: Bugfix #983 Undue modifications of already existing diagrams
				isNew = true;
				// END KGU#981 2021-09-18
			}
		}
		
		// 3. Now care about group context
		myGroups = Arranger.getInstance().getGroupsFromRoot(root, true);
		// START KGU#981 2021-09-18: Bugfix #983 Undue modifications of already existing diagrams
		//if (referredRoot != null) {
		if (isNew) {
		// END KGU#981 2021-09-18
			referredRoot.setChanged(false);
			// Now care for the group context. If the parent diagram hadn't been in Arranger then put it there now
			if (myGroups.isEmpty() && Arranger.getInstance().getGroupsFromRoot(root, false).isEmpty()) {
				// If the diagram is a program then create an exclusive group named after the main diagram 
				if (root.isProgram()) {
					targetGroupName = root.getMethodName(true);
					Arranger.getInstance().addToPool(root, this.getFrame(), targetGroupName);
					myGroups = Arranger.getInstance().getGroupsFromRoot(root, true);
				} else {
					Arranger.getInstance().addToPool(root, this.getFrame());
				}
			} else if (Arranger.getInstance().getGroupsFromRoot(root, false).size() == myGroups.size()) {
				// Parent diagram is arranged but not member of the default group - then its children shouldn't be either
				targetGroupName = myGroups.iterator().next().getName();
			}
		// END KGU#770 2021-01-27
		// START KGU#981 2021-09-18: Bugfix #983 Undue modifications of already existing diagrams
		}
		// END KGU#981 2021-09-18
		// START KGU#744 2019-10-05: Issue #758 - In case the connected subForm already handles the subroutine don't force to save it
		//if (subForm == null || subForm.diagram == null || !subForm.diagram.saveNSD(true) || !subForm.setRoot(subroutine)) {
		if (subForm == null
				|| subForm.diagram == null
				|| subForm.diagram.getRoot() != referredRoot
				&& (!subForm.diagram.saveNSD(true) || !subForm.setRoot(referredRoot))) {
		// END KGU#744 2019-10-05
			subForm = new Mainform(false);
			subForm.addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {
				}

				@Override
				public void windowClosing(WindowEvent e) {
					// A dead Mainform causes enormous trouble if we try to work with it.
					if (subForm == e.getSource()) {
						subForm = null;
					}
				}
				public void windowClosed(WindowEvent e) {
					((Mainform)(e.getSource())).removeWindowListener(this);
					// START KGU#744 2019-10-05: Bugfix #758 We are not always informed on windowClosing
					if (subForm == e.getSource()) {
						subForm = null;
					}
				}
				@Override
				public void windowIconified(WindowEvent e) {}
				@Override
				public void windowDeiconified(WindowEvent e) {}
				@Override
				public void windowActivated(WindowEvent e) {}
				@Override
				public void windowDeactivated(WindowEvent e) {}

			});
		}
		if (subForm.diagram.getRoot() != referredRoot) {
			subForm.setRoot(referredRoot);
		}
		// Associate the arranged diagram to the subForm
		if (targetGroupName != null) {
			Arranger.getInstance().addToPool(referredRoot, subForm, targetGroupName);
		}
		// START KGU#744 2019-10-05: Bugfix #758 - The subroutine has always to be associated
		else {
			Arranger.getInstance().addToPool(referredRoot, subForm);
		}
		Arranger.getInstance().setVisible(true);
		// END KGU#744 2019-10-05
		if (!subForm.isVisible()) {
			subForm.setVisible(true);
		}
		// START KGU#744 2019-10-05: Bugfix #758
		int state = subForm.getExtendedState();
		if ((state & Frame.ICONIFIED) != 0) {
			subForm.setExtendedState(state & ~Frame.ICONIFIED);
		}
		// END KGU#744 2019-10-05
		Point loc = NSDControl.getFrame().getLocation();
		Point locSub = subForm.getLocation();
		if (loc.equals(locSub)) {
			subForm.setLocation(loc.x + 20, loc.y + 20);
		}
		// START KGU#770 2021-01-27: Enh. #689, #917
		// We must of course give the focus to the opened editor
		subForm.requestFocus();
		// END KGU#770 2021-01-27
		// START KGU#981 2021-09-18: Bugfix #983 Undue modifications of already existing diagrams
		//}
		// END KGU#981 2021-09-18
	}

	/**
	 * Disambiguates the referenced {@link Root} among the {@code candidates}
	 * with user assistance if necessary.
	 *
	 * @param candidates - the vector of candidate {@link Root}s
	 * @param rootType - localised name of the rout type
	 * @return either the selected {@link Root} or {@code null}
	 */
	private Root chooseReferredRoot(Vector<Root> candidates, String rootType) {
		Root referredRoot = null;
		// If the finding is unambiguous, get it
		if (candidates.size() == 1) {
			referredRoot = candidates.get(0);
		} else if (candidates.size() > 1) {
			// Open a choice list with full paths and let the user decide
			String[] choices = new String[candidates.size()];
			int i = 0;
			for (Root cand : candidates) {
				choices[i++] = cand.getSignatureString(true, false);
			}
			String input = (String) JOptionPane.showInputDialog(getFrame(),
					Menu.msgChooseSubroutine.getText().replace("%", rootType),
					Menu.msgTitleQuestion.getText(),
					JOptionPane.QUESTION_MESSAGE, null, // Use default icon
					choices, // Array of choices
					choices[0]); // Initial choice
			if (input != null && !input.trim().isEmpty()) {
				for (i = 0; i < choices.length && referredRoot != null; i++) {
					if (input.equals(choices[i])) {
						referredRoot = candidates.get(i);
					}
				}
			}
		}
		return referredRoot;
	}
	// END KGU#667 2019-02-26

	/*========================================
	 * transmute method(s)
	 *========================================*/
	// START KGU#199 2016-07-06: Enh. #188 - perform the possible conversion
	/**
	 * Converts the selected element(s) into some substitute according to the
	 * specified transmutation rules (undoable).
	 *
	 * @see #canTransmute()
	 */
	public void transmuteNSD() {
		Subqueue parent = (Subqueue) selected.parent;
		if (selected instanceof Instruction) {
			//root.addUndo();
			try {
				addUndoNSD(false);
			} catch (CancelledException e) {
				return;
			}
			if (selected.getUnbrokenText().count() > 1) {
				transmuteToSequence(parent);
			} else {
				transmuteToSpecialInstr(parent);
			}
		} else if (selected instanceof IElementSequence) {
			root.addUndo();
			transmuteToCompoundInstr(parent);
		}
		// START KGU#229 2016-08-01: Enh. #213 - FOR loop decomposition
		else if (selected instanceof For && ((For) selected).style == For.ForLoopStyle.COUNTER) {
			root.addUndo();
			decomposeForLoop(parent);
		}
		// END KGU#229 2016-08-01
		// START KGU#267 2016-10-03: Enh. #257 - CASE decomposition
		else if (selected instanceof Case) {
			root.addUndo();
			decomposeCase(parent);
		}
		// END KGU#267 2016-10-03
		// START KGU#357 2017-03-10: Enh. #367: swapping of sides
		else if (selected instanceof Alternative && ((Alternative) selected).qFalse.getSize() > 0) {
			root.addUndo();
			swapBranches((Alternative) selected);
		}
		// END KGU#357 2017-03-10
		this.doButtons();
		redraw();
		analyse();
		// START KGU#444 2017-10-23: Issue #417 - reduce scrolling complexity
		adaptScrollUnits();
		// END KGU#444 2017-10-23
		// START KGU#705 2019-09-23: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-09-23
	}

	// START KGU#357 2017-03-10: Enh. #367
	/**
	 * Helper method of {@link #transmuteNSD()}
	 */
	private void swapBranches(Alternative _alt) {
		String condition = _alt.getText().getText();
		String negCondition = Element.negateCondition(condition);
		_alt.setText(negCondition);
		Subqueue temp = _alt.qFalse;
		_alt.qFalse = _alt.qTrue;
		_alt.qTrue = temp;
		// The width of the condition is likely to have changed
		_alt.resetDrawingInfoUp();	// KGU#590 2018-10-01: Corrected Down -> Up
		redraw();
		analyse();
	}
	// END KGU#357 2017-03-10

	/**
	 * Helper method of {@link #transmuteNSD()}
	 */
	private void transmuteToSequence(Subqueue parent) {
		// Comment will be split as follows:
		// If the number of strings of the comment equals the number of instruction
		// lines then the strings are assigned one by one to he resulting instructions
		// (thereby splitting multi-line strings into StringLists),
		// otherwise the first instruction will get all comment.
		int index = parent.getIndexOf(selected);
		StringList comment = selected.getComment();
		StringList text = selected.getBrokenText();
		int count = text.count();
		boolean distributeComment = (count == comment.count());
		for (int i = 0; i < count; i++) {
			Instruction instr = (Instruction) selected.copy();
			instr.setText(StringList.explode(text.get(i), "\n"));
			if (distributeComment) {
				instr.setComment(StringList.explode(comment.get(i), "\n"));
			} else if (i != 0) {
				instr.comment.clear();
			}
			parent.insertElementAt(instr, index + i + 1);
		}
		parent.removeElement(index);
		selected = new SelectedSequence(parent, index, index + count - 1);
		selectedDown = null;
		selected.setSelected(true);
	}

	/**
	 * Helper method of {@link #transmuteNSD()}
	 */
	private void transmuteToSpecialInstr(Subqueue parent) {
		Instruction instr = (Instruction) selected;
		Element elem = instr;
		if (instr instanceof Call || instr instanceof Jump) {
			elem = new Instruction(instr);
		} else if (instr.isProcedureCall(true) || instr.isFunctionCall(true)) {
			elem = new Call(instr);
		} else if (instr.isJump()) {
			elem = new Jump(instr);
		}
		int index = parent.getIndexOf(instr);
		parent.insertElementAt(elem, index + 1);
		parent.removeElement(index);
		this.selected = elem;
		this.selectedDown = this.selected;
	}

	/**
	 * Helper subroutine of {@link #transmuteNSD()}
	 */
	private void transmuteToCompoundInstr(Subqueue parent) {
		// Comments will be composed as follows:
		// If none of the selected elements had a non-empty comment then the resulting
		// comment will be empty. Otherwise the resulting comment will contain as many
		// strings as elements. Each of them will be the respective element comment,
		// possibly containing several newlines if it was a multi-line comment.
		Instruction instr = (Instruction) ((IElementSequence) selected).getElement(0);
		StringList composedComment = StringList.getNew(instr.getComment().getText().trim());
		int nElements = ((IElementSequence) selected).getSize();
		int index = parent.getIndexOf(instr);
		boolean brkpt = instr.isBreakpoint();
		// START KGU#213 2016-08-01: Enh. #215
		int brkCount = instr.getBreakTriggerCount();
		// END KGU#213 2016-08-01
		// Find out whether all elements are of the same kind
		boolean sameKind = true;
		for (int i = 1; sameKind && i < nElements; i++) {
			if (((IElementSequence) selected).getElement(i).getClass() != instr.getClass()) {
				sameKind = false;
			}
		}
		// If so...
		if (sameKind) {
			// ... then clone the first element of the sequence as same class
			instr = (Instruction) instr.copy();
		} else {
			// ... else clone the first element of the sequence as simple instruction
			instr = new Instruction(instr);
		}
		((IElementSequence) selected).removeElement(0);
		nElements--;
		// And now append the contents of the remaining elements, removing them from the selection
		for (int i = 0; i < nElements; i++) {
			Element ele = ((IElementSequence) selected).getElement(0);
			instr.getText().add(ele.getText());
			composedComment.add(ele.getComment().getText().trim());
			if (ele.isBreakpoint()) {
				brkpt = true;
			}
			// START KGU#213 2016-08-01: Enh. #215
			// Use the earliest breakTriggerCount
			int brkCnt = ele.getBreakTriggerCount();
			if (brkCnt > 0 && brkCnt < brkCount) {
				brkCount = brkCnt;
			}
			// END KGU#213 2016-08-01
			((IElementSequence) selected).removeElement(0);
		}
		// If there was no substantial comment then we must not create one, otherwise
		// the cmment is to consist of as many strings as instruction lines - where
		// each of them may contain newlines for reversibility
		if (!composedComment.concatenate().trim().isEmpty()) {
			instr.setComment(composedComment);
		} else {
			instr.getComment().clear();
		}
		// If any of the implicated instructions had a breakpoint then set it here, too
		if (brkpt && !instr.isBreakpoint()) {
			instr.toggleBreakpoint();
		}
		// START KGU#213 2016-08-01: Enh. #215
		// Use the earliest breakTriggerCount
		instr.setBreakTriggerCount(brkCount);
		// END KGU#213 2016-08-01

		instr.setSelected(true);
		parent.insertElementAt(instr, index);
		this.selected = instr;
		this.selectedDown = this.selected;
	}
	// END KGU#199 2016-07-06

	// START KGU#229 2016-08-01: Enh. #213 - FOR loop decomposition
	/**
	 * Helper method of {@link #transmuteNSD()}
	 */
	private void decomposeForLoop(Subqueue parent) {
		// Comment will be tranferred to the While loop.
		For forLoop = (For) selected;
		String asgmtOpr = " <- ";
		if (forLoop.getText().get(0).contains(":=")) {
			asgmtOpr = " := ";
		}
		int step = forLoop.getStepConst();
		Element[] elements = new Element[3];
		elements[0] = new Instruction(forLoop.getCounterVar() + asgmtOpr + forLoop.getStartValue());
		// START KGU#229 2016-09-09: Take care of the configured prefix and postfix
		//While whileLoop = new While(forLoop.getCounterVar() + (step < 0 ? " >= " : " <= ") + forLoop.getEndValue());
		String prefix = "", postfix = "";
		if (!CodeParser.getKeyword("preWhile").trim().isEmpty()) {
			prefix = CodeParser.getKeyword("preWhile");
			if (!prefix.endsWith(" ")) {
				prefix += " ";
			}
		}
		if (!CodeParser.getKeyword("postWhile").trim().isEmpty()) {
			postfix = CodeParser.getKeyword("postWhile");
			if (!postfix.startsWith(" ")) {
				postfix = " " + postfix;
			}
		}
		While whileLoop = new While(prefix + forLoop.getCounterVar() + (step < 0 ? " >= " : " <= ") + forLoop.getEndValue() + postfix);
		// END KGU#229 2016-09-09
		elements[1] = whileLoop;
		elements[2] = new Instruction(forLoop.getCounterVar() + asgmtOpr + forLoop.getCounterVar() + (step < 0 ? " - " : " + ") + Math.abs(forLoop.getStepConst()));

		whileLoop.setComment(forLoop.getComment());
		if (forLoop.isBreakpoint()) {
			whileLoop.toggleBreakpoint();
		}
		whileLoop.setBreakTriggerCount(forLoop.getBreakTriggerCount());
		whileLoop.q = forLoop.getBody();
		whileLoop.q.parent = whileLoop;
		whileLoop.q.addElement(elements[2]);
		whileLoop.setCollapsed(forLoop.isCollapsed(true));
		for (int i = 0; i < elements.length; i++) {
			Element elem = elements[i];
			elem.setColor(forLoop.getColor());
			elem.deeplyCovered = forLoop.deeplyCovered;
			elem.simplyCovered = forLoop.simplyCovered;
		}
		int index = parent.getIndexOf(forLoop);
		for (int i = 0; i < 2; i++) {
			parent.insertElementAt(elements[1 - i], index + 1);
		}
		parent.removeElement(index);
		this.selected = new SelectedSequence(parent, index, index + 1);
		// START KGU#229 2016-09-11: selection must be made visible!
		this.selected.setSelected(true);
		// END KGU#229 2016-09-11
		this.selectedDown = this.selected;
	}
	// END KGU#229 2016-08-01

	// START KGU#267 2016-10-03: Enh. #257 - CASE structure decomposition
	/**
	 * Helper method of {@link #transmuteNSD()}
	 */
	private void decomposeCase(Subqueue parent) {
		// Comment will be transferred to the first replacing element
		// (discriminator variable assignment or outermost Alternative).
		Case caseElem = (Case) selected;
		// List of replacing nested alternatives
		List<Alternative> alternatives = new LinkedList<Alternative>();
		// Possibly preceding assignment of the selection expression value
		Instruction asgnmt = null;
		// tokenized selection expression
		StringList selTokens = Element.splitLexically(caseElem.getText().get(0), true);
		// Eliminate parser preference keywords
		String[] redundantKeywords = {CodeParser.getKeyword("preCase"), CodeParser.getKeyword("postCase")};
		for (String keyword : redundantKeywords) {
			if (!keyword.trim().isEmpty()) {
				StringList tokenizedKey = Element.splitLexically(keyword, false);
				int pos = -1;
				while ((pos = selTokens.indexOf(tokenizedKey, pos + 1, !CodeParser.ignoreCase)) >= 0) {
					for (int i = 0; i < tokenizedKey.count(); i++) {
						selTokens.delete(pos);
					}
				}
			}
		}
		String discriminator = selTokens.concatenate().trim();
		// If the discriminating expression isn't just a variable then assign its value to an
		// artificial variable first and use this as discriminator further on.
		if (!Function.testIdentifier(discriminator, false, "")) {
			String discrVar = "discr" + caseElem.hashCode();
			asgnmt = new Instruction(discrVar + " <- " + discriminator);
			discriminator = discrVar;
			asgnmt.setColor(caseElem.getColor());
		}

		// Take care of the configured prefix and postfix
		String prefix = "", postfix = "";
		if (!CodeParser.getKeyword("preAlt").trim().isEmpty()) {
			prefix = CodeParser.getKeyword("preAlt");
			if (!prefix.endsWith(" ")) {
				prefix += " ";
			}
		}
		if (!CodeParser.getKeyword("postAlt").trim().isEmpty()) {
			postfix = CodeParser.getKeyword("postAlt");
			if (!postfix.startsWith(" ")) {
				postfix = " " + postfix;
			}
		}

		int nAlts = 0;	// number of alternatives created so far
		for (int lineNo = 1; lineNo < caseElem.getText().count(); lineNo++) {
			String line = caseElem.getText().get(lineNo);
			// Specific handling of the last branch
			if (lineNo == caseElem.getText().count() - 1) {
				// In case it's a "%", nothing is to be added, otherwise the last
				// branch is to be the else path of the innermost alternative
				if (!line.equals("%")) {
					// This should not happen before the first alternative has been created!
					alternatives.get(nAlts - 1).qFalse = caseElem.qs.get(lineNo - 1);
					alternatives.get(nAlts - 1).qFalse.parent = alternatives.get(nAlts - 1);
				}
			} else {
				String[] selectors = line.split(",");
				String cond = "";
				for (String selConst : selectors) {
					cond += " || (" + discriminator + " = " + selConst.trim() + ")";
				}
				// START KGU#288 2016-11-06: Issue #279
				//cond = cond.substring(4).replace("||", CodeParser.getKeywordOrDefault("oprOr", "or"));
				cond = cond.substring(4).replace("||", CodeParser.getKeywordOrDefault("oprOr", "or"));
				// END KGU#288 2016-11-06
				Alternative newAlt = new Alternative(prefix + cond + postfix);
				newAlt.qTrue = caseElem.qs.get(lineNo - 1);
				newAlt.qTrue.parent = newAlt;
				alternatives.add(newAlt);
				if (nAlts > 0) {
					alternatives.get(nAlts - 1).qFalse.addElement(newAlt);
				}
				nAlts++;
			}
		}

		Element firstSubstitutor = (asgnmt != null) ? asgnmt : alternatives.get(0);
		firstSubstitutor.setComment(caseElem.getComment());
		if (caseElem.isBreakpoint()) {
			firstSubstitutor.toggleBreakpoint();
		}
		firstSubstitutor.setBreakTriggerCount(caseElem.getBreakTriggerCount());
		for (Alternative alt : alternatives) {
			alt.setColor(caseElem.getColor());
			alt.deeplyCovered = caseElem.deeplyCovered;
			alt.simplyCovered = caseElem.simplyCovered;
		}
		alternatives.get(0).setCollapsed(caseElem.isCollapsed(true));

		int index = parent.getIndexOf(caseElem);
		parent.removeElement(index);
		parent.insertElementAt(alternatives.get(0), index);
		if (asgnmt != null) {
			parent.insertElementAt(asgnmt, index);
			this.selected = new SelectedSequence(parent, index, index + 1);
		} else {
			this.selected = parent.getElement(index);
		}
		this.selected.setSelected(true);
		this.selectedDown = this.selected;
	}
	// END KGU#267 2016-10-03

	// START KGU#282 2016-10-16: Issue #272 (draft)
	/*=======================================*
	 * Turtleizer precision methods
	 *=======================================*/
	/**
	 * Replaces all Turtleizer {@code fd} and {@code bk} procedure calls by the
	 * more precise {@code forward} and {@code backward} instructions
	 * ({@code precisionUp} = true) or the other way round ({@code precisionUp}
	 * = false) in the selected elements
	 *
	 * @param precisionUp - whether to convert to the more precise or to the
	 * less precise versions
	 */
	public void replaceTurtleizerAPI(boolean precisionUp) {
		final class TurtleizerSwitcher implements IElementVisitor {

			private int from;
			// START #272 2016-10-17 (KGU): detect changes (to get rid of void undo entry
			private boolean act = false;
			private int nChanges = 0;
			// END #272 2016-10-17
			private final String[][] functionPairs = {{"fd", "forward"}, {"bk", "backward"}};

			public TurtleizerSwitcher(boolean upgrade) {
				this.from = upgrade ? 0 : 1;
			}

			public boolean visitPreOrder(Element _ele) {
				if (_ele.getClass().getSimpleName().equals("Instruction")) {
					for (int i = 0; i < _ele.getText().count(); i++) {
						String line = _ele.getText().get(i);
						if (Instruction.isTurtleizerMove(line)) {
							Function fct = new Function(line);
							for (int j = 0; j < functionPairs.length; j++) {
								String oldName = functionPairs[j][from];
								if (fct.getName().equals(oldName)) {
									// START #272 2016-10-17
									//_ele.getText().set(i, functionPairs[j][1 - from] + line.trim().substring(oldName.length()));
									if (this.act) {
										_ele.getText().set(i, functionPairs[j][1 - from] + line.trim().substring(oldName.length()));
									}
									nChanges++;
									// END #272 2016-10-17
								}
							}
						}
					}
				}
				return true;
			}

			public boolean visitPostOrder(Element _ele) {
				return true;
			}

			// START #272 2016-10-17 (KGU)
			public void activate() {
				this.nChanges = 0;
				this.act = true;
			}

			public int getNumberOfReplacements() {
				return nChanges;
			}
			// END #272 2016-10-17

		}

		// START #272 2016-10-17 (KGU): Inform the user and get rid of void undo entry
		//root.addUndo();
		//selected.traverse(new TurtleizerSwitcher(precisionUp));
		// First mere count run
		TurtleizerSwitcher switcher = new TurtleizerSwitcher(precisionUp);
		selected.traverse(switcher);
		int nReplaced = switcher.getNumberOfReplacements();
		if (nReplaced > 0) {
			// There will be substitutions, so get dangerous.
			//root.addUndo();
			try {
				addUndoNSD(false);
			} catch (CancelledException e) {
				return;
			}
			switcher.activate();
			selected.traverse(switcher);
		}
		JOptionPane.showMessageDialog(this.getFrame(),
				Menu.msgReplacementsDone.getText().replace("%", Integer.toString(nReplaced)));
		// END #272 2016-10-17
		// START KGU#705 2019-09-23: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-09-23
	}
	// END KGU#282 2016-10-16

	// START KGU#602 2018-10-26: Enh. #419
	/**
	 * Adjusts line breaking for the selected element(s). Requests the details
	 * interactively from the user.
	 */
	public void rebreakLines() {
		if (this.selected != null) {
			// The current maximum line lengths of the selected element(s)
			Element selectedEls = this.selected;
			int flatMax = selectedEls.getMaxLineLength(false);
			int deepMax = selectedEls.getMaxLineLength(true);
			int lastLineLimit = this.lastWordWrapLimit <= 0 ? deepMax : this.lastWordWrapLimit;
			JPanel pnl = new JPanel();
			JLabel lblMaxLenSel = new JLabel(Menu.msgMaxLineLengthSelected.getText().
					replace("%", Integer.toString(flatMax)));
			JLabel lblMaxLenSub = new JLabel(Menu.msgMaxLineLengthSubstructure.getText().
					replace("%", Integer.toString(deepMax)));
			JLabel lblNewLen = new JLabel(Menu.lblNewMaxLineLength.getText());
			JSpinner spnNewLen = new JSpinner();
			SpinnerModel spnModelLen = new SpinnerNumberModel(0, 0, 255, 5);
			spnNewLen.setModel(spnModelLen);
			spnNewLen.setValue(lastLineLimit);
			JCheckBox cbRecursive = new JCheckBox(Menu.lblInvolveSubtree.getText());
			JCheckBox cbPreserve = new JCheckBox(Menu.lblPreserveContinuators.getText());
			cbRecursive.setSelected(!(selectedEls instanceof Instruction));
			cbPreserve.setSelected(lastLineLimit < (cbRecursive.isSelected() ? deepMax : flatMax));

			GridBagConstraints gbcLimits = new GridBagConstraints();
			gbcLimits.insets = new Insets(0, 5, 5, 5);
			gbcLimits.weightx = 1.0;
			gbcLimits.anchor = GridBagConstraints.LINE_START;

			pnl.setLayout(new GridBagLayout());

			gbcLimits.gridx = 0;
			gbcLimits.gridy = 0;
			gbcLimits.gridwidth = GridBagConstraints.REMAINDER;
			pnl.add(lblMaxLenSel, gbcLimits);

			gbcLimits.gridy++;
			pnl.add(lblMaxLenSub, gbcLimits);

			gbcLimits.gridy++;
			gbcLimits.gridwidth = 1;
			pnl.add(lblNewLen, gbcLimits);

			gbcLimits.gridx++;
			gbcLimits.gridwidth = GridBagConstraints.REMAINDER;
			gbcLimits.fill = GridBagConstraints.HORIZONTAL;
			pnl.add(spnNewLen, gbcLimits);

			gbcLimits.gridx = 0;
			gbcLimits.gridy++;
			gbcLimits.fill = GridBagConstraints.NONE;
			pnl.add(cbRecursive, gbcLimits);
			gbcLimits.gridy++;
			pnl.add(cbPreserve, gbcLimits);

			ChangeListener changeListener = new ChangeListener() {

				private final JSpinner spnLen = spnNewLen;
				private final JCheckBox chkRec = cbRecursive, chkPres = cbPreserve;
				private final int flatLen = flatMax, deepLen = deepMax;

				@Override
				public void stateChanged(ChangeEvent e) {
					int limit = (int) spnLen.getValue();
					if (chkRec.isSelected() && limit > deepLen || !chkRec.isSelected() && limit > flatLen) {
						chkPres.setSelected(false);
					}
				}

			};

			// Without this hack, directly entering a number in the spinner's text field wouldn't fire the ChangeEvent
			DefaultFormatter formatter = (DefaultFormatter) ((JSpinner.DefaultEditor) spnNewLen.getEditor()).getTextField().getFormatter();
			formatter.setCommitsOnValidEdit(true);

			spnNewLen.addChangeListener(changeListener);
			cbRecursive.addChangeListener(changeListener);

			int answer = JOptionPane.showConfirmDialog(
					this.getFrame(), pnl,
					Menu.ttlBreakTextLines.getText(),
					JOptionPane.OK_CANCEL_OPTION);

			if (answer == JOptionPane.OK_OPTION) {
				root.addUndo();
				boolean recursive = cbRecursive.isSelected();
				boolean preserve = cbPreserve.isSelected();
				this.lastWordWrapLimit = (short) (int) spnNewLen.getValue();
				boolean changed = false;
				if (selectedEls instanceof Root) {
					changed = selectedEls.breakTextLines(this.lastWordWrapLimit, !preserve);
					if (recursive && ((Root) selectedEls).breakElementTextLines(this.lastWordWrapLimit, !preserve)) {
						changed = true;
					}
				} else if (!recursive && !(selectedEls instanceof IElementSequence)) {
					changed = selectedEls.breakTextLines(this.lastWordWrapLimit, !preserve);
				} else {
					if (!(selectedEls instanceof IElementSequence)) {
						selectedEls = new SelectedSequence(selectedEls, selectedEls);
					}
					IElementSequence.Iterator iter = ((IElementSequence) selectedEls).iterator(recursive);
					while (iter.hasNext()) {
						if (iter.next().breakTextLines(this.lastWordWrapLimit, !preserve)) {
							changed = true;
						}
					}
				}
				if (!changed) {
					root.undo(false);
				}
				// START KGU#940 2021-02-24: Bugfix #419 - Drawing update had been missing
				redraw();
				// END KGU#940 2021-02-24
				// START KGU#705 2019-09-23: Enh. #738
				updateCodePreview();
				// END KGU#705 2019-09-23
			}
		}
	}
	// END KGU#602 2016-10-26

	// START KGU#43 2015-10-12
	/*========================================
	 * breakpoint methods
	 *========================================*/
	/**
	 * Enables or disables the breakpoint on the selected element(s) depending
	 * on the previous state. Does not affect a possibly configured break
	 * trigger.
	 *
	 * @see Element#toggleBreakpoint()
	 * @see #editBreakTrigger()
	 * @see #clearBreakpoints()
	 */
	public void toggleBreakpoint() {
		Element ele = getSelected();
		if (ele != null) {
			ele.toggleBreakpoint();
			redraw();
		}
	}

	// START KGU#213 2016-08-02: Enh. #215
	/**
	 * Opens a dialog that allows to set the break trigger value (i.e. the
	 * execution count value that triggers a breakpoint if enabled.
	 *
	 * @see #toggleBreakpoint()
	 * @see #clearBreakpoints()
	 */
	public void editBreakTrigger() {
		// TODO Auto-generated method stub
		Element ele = getSelected();
		if (ele != null) {
			int trigger = ele.getBreakTriggerCount();
			// FIXME: Replace this quick-and-dirty approach by something more functional
			String str = JOptionPane.showInputDialog(this.getFrame(),
					Menu.msgBreakTriggerPrompt.getText(),
					Integer.toString(trigger));
			if (str != null) {
				// START KGU#252 2016-09-21: Issue 248 - Linux (Java 1.7) workaround
				boolean isDone = false;
				// END KGU#252 2016-09-21
				try {
					// START KGU#252 2016-09-21: Issue 248 - Linux (Java 1.7) workaround
					//ele.setBreakTriggerCount(Integer.parseUnsignedInt(str));
					isDone = ele.setBreakTriggerCount(Integer.parseInt(str));
					// END KGU#252 2016-09-21
					// We assume the intention to activate the breakpoint with the configuration
					if (!ele.isBreakpoint()) {
						// FIXME This might not work properly with recursive algorithms (i.e. on stack unwinding)
						ele.toggleBreakpoint();
					}
					redraw();
				} catch (NumberFormatException ex) {
					// START KGU#252 2016-09-21: Issue 248 - Linux (Java 1.7) workaround
					//JOptionPane.showMessageDialog(this,
					//		Menu.msgBreakTriggerIgnored.getText(),
					//		Menu.msgTitleWrongInput.getText(),
					//		JOptionPane.ERROR_MESSAGE);
					// END KGU#252 2016-09-21
				}
				// START KGU#252 2016-09-21: Issue 248 - Linux (Java 1.7) workaround
				if (!isDone) {
					JOptionPane.showMessageDialog(this.getFrame(),
							Menu.msgBreakTriggerIgnored.getText(),
							Menu.msgTitleWrongInput.getText(),
							JOptionPane.ERROR_MESSAGE);
				}
				// END KGU#252 2016-09-21
			}
		}
	}
	// END KGU#213 2016-08-02

	/**
	 * Unsets all enabled breakpoints throughout the current diagram.
	 *
	 * @see #toggleBreakpoint()
	 * @see #editBreakTrigger()
	 */
	public void clearBreakpoints() {
		// FIXME (Issue #954): All clones in the Executor call stack must also be cleared!
		root.clearBreakpoints();
		redraw();
	}

	// START KGU#952 2021-03-03: Issue #954
	/**
	 * Disables (or enables) the supervision of Breakpoints by {@link Executor}
	 *
	 * @param disable - if {@code true} then all breakpoints in all diagrams
	 * will be disabled, otherwise they will be activated again.
	 */
	public void disableBreakpoints(boolean disable) {
		Element.E_BREAKPOINTS_ENABLED = !disable;
		if (disable) {
			Element.E_BREAKPOINTCOLOR = Element.E_COMMENTCOLOR;
		} else {
			Element.E_BREAKPOINTCOLOR = Color.RED;
		}
		redraw();
		doButtons();
	}

	/**
	 * Clears all execution flags and counts throughout the entire diagram held
	 * as {@link #root}.
	 */
	public void clearExecutionStatus() {
		root.clearExecutionStatus();
		redraw();
	}
	// END KGU#43 2015-10-12

	/*========================================
	 * print method
	 *========================================*/
	/**
	 * Opens the print preview for the current {@link #root} from which it can
	 * be printed.
	 *
	 * @see #exportPDF()
	 */
	public void printNSD() {
		/*
		// printing support
		//--- Create a printerJob object
		PrinterJob printJob = PrinterJob.getPrinterJob ();
		//--- Set the printable class to this one since we
		//--- are implementing the Printable interface
		printJob.setPrintable (this);
		//--- Show a print dialog to the user. If the user
		//--- clicks the print button, then print, otherwise
		//--- cancel the print job
		if (printJob.printDialog())
		{
			try
			{
				printJob.print();
			}
			catch (Exception PrintException)
			{
				PrintException.printStackTrace();
			}
		}
		 */
		//PrintPreview.print(this);
		/*
		PrintPreview pp = new PrintPreview(this,"Print Previwe");
		Point p = getLocationOnScreen();
		pp.setLocation(Math.round(p.x+(getVisibleRect().width-pp.getWidth())/2), Math.round(p.y)+(getVisibleRect().height-pp.getHeight())/2);
		pp.setVisible(true);
		 */
		// START KGU#170 2018-06-11: Issue #143 - on opening the print preview a comment popup should vanish
		hideComments();
		// END KGU#170 2018-06-11
		PrintPreview pp = new PrintPreview(NSDControl.getFrame(), this);
		Point p = getLocationOnScreen();
		pp.setLocation(Math.round(p.x + (getVisibleRect().width - pp.getWidth()) / 2 + this.getVisibleRect().x),
				Math.round(p.y) + (getVisibleRect().height - pp.getHeight()) / 2 + this.getVisibleRect().y);
		pp.setVisible(true);
	}

	// START KGU#2 2015-11-19
	/*========================================
	 * arrange method
	 *========================================*/
	/**
	 * Push the current root to the Arranger and pin it there. If Arranger
	 * wasn't visible then it will be (re-)opened.
	 */
	public void arrangeNSD()
	// START KGU#626 2018-12-28: Enh. #657
	{
		arrangeNSD(null);
	}

	/**
	 * Push the current root to the Arranger and pin it there. If Arranger
	 * wasn't visible then it will be (re-)opened.
	 *
	 * @param sourceFilename - the base name of a code file the diagram was
	 * imported from if so, null otherwise
	 */
	public void arrangeNSD(String sourceFilename)
	// END KGU#626 2018-12-28
	{
		//System.out.println("Arranger button pressed!");
		Arranger arr = Arranger.getInstance();
		arr.addToPool(root, NSDControl.getFrame(), sourceFilename);
		arr.setVisible(true);
		// KGU#280 2016-10-11: Obsolete now
		//isArrangerOpen = true;	// Gives the Executor a hint where to find a subroutine pool
	}
	// END KGU#2 2015-11-19

	// START KGU#125 2016-01-06: Possibility to adopt a diagram if it's orphaned
	public void adoptArrangedOrphanNSD(Root root) {
		if (isArrangerOpen()) {
			Arranger arr = Arranger.getInstance();
			// START KGU#742 2019-10-04: This caused ConcurrentModificationExceptions
			//arr.addToPool(root, frame);
			arr.adoptRootIfOrphaned(root, (Mainform) NSDControl.getFrame());
			//END KGU#741 2019-10-04
		}
	}
	// END KGU#125 2016-01-06

	/*========================================
	 * about method
	 *========================================*/
	/**
	 * Opens the About window with version info, authors tab, license text,
	 * change log tab, and paths tab.
	 */
	public void aboutNSD() {
		About about = new About(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		about.setLocation(Math.round(p.x + (getVisibleRect().width - about.getWidth()) / 2 + this.getVisibleRect().x),
				Math.round(p.y) + (getVisibleRect().height - about.getHeight()) / 2 + this.getVisibleRect().y);
		// START KGU#300 2016-12-02: Enh. #300 - Add info about newer version if enabled
		String newVersion = this.getLatestVersionIfNewer();
		if (newVersion != null) {
			about.lblVersion.setText(about.lblVersion.getText() + " (" + Menu.msgNewerVersionAvail.getText().replace("%", newVersion) + ")");
		}
		// END KGU#300 2016-12-02
		about.setVisible(true);
	}

	/*========================================
	 * export picture method
	 *========================================*/
	/**
	 * Opens a dialog for the configuration of a multi-tile PNG export and a
	 * {@link FileChooser} and performs the respective image export.
	 *
	 * @see #exportPNG()
	 * @see #exportSVG()
	 * @see #exportEMF()
	 * @see #exportPDF()
	 * @see #exportSWF()
	 */
	public void exportPNGmulti() {
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU#41 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll(true);
		// END KGU#41 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as Multi-PNG ...");
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgSave);
		// END KGU#287 2017-01-09
		// set directory
		if (lastExportDir != null) {
			dlgSave.setCurrentDirectory(lastExportDir);
		} else if (root.getFile() != null) {
			dlgSave.setCurrentDirectory(root.getFile());
		} else {
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: There is already a suitable method on root
		//		String nsdName = root.getText().get(0);
		//		nsdName.replace(':', '_');
		//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU#170 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.PNGFilter());
		PNGFilter filter = new PNGFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		hideComments();	// Issue #143: Hide the current comment popup if visible
		// END KGU#170 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			lastExportDir = dlgSave.getSelectedFile().getParentFile();
			String filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if (!filename.substring(filename.length() - 4, filename.length()).toLowerCase().equals(".png")) {
				filename += ".png";
			}

			// START KGU#224 2016-07-28: Issue #209  Test was nonsense since the actual file names will be different
			//File file = new File(filename);
			File file = new File(filename.replace(".png", "-00-00.png"));
			// END KGU#224 2016-07-28
			boolean writeDown = true;

			if (file.exists()) {
				int response = JOptionPane.showConfirmDialog(this.getFrame(),
						Menu.msgOverwriteFiles.getText(),
						Menu.btnConfirmOverwrite.getText(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION) {
					writeDown = false;
				}
			}
			if (writeDown == true) {
				// START KGU#218 2016-07-28: Issue #206 Localization efforts
				//int cols = Integer.valueOf(JOptionPane.showInputDialog(null, "Into how many columns do you want to split the output?", "1"));
				//int rows = Integer.valueOf(JOptionPane.showInputDialog(null, "Into how many rows do you want to split the output?", "3"));
				int cols = Integer.valueOf(JOptionPane.showInputDialog(null, Menu.msgDialogExpCols.getText(), "1"));
				int rows = Integer.valueOf(JOptionPane.showInputDialog(null, Menu.msgDialogExpRows.getText(), "3"));
				// END KGU#218 2016-07-28

				BufferedImage image = new BufferedImage(root.width + 1, root.height + 1, BufferedImage.TYPE_4BYTE_ABGR);
				// START KGU#221 2016-07-28: Issue #208 Need to achieve transparent background
				//printAll(image.getGraphics());
				// START KGU#906 2021-01-06: Enh. #905
				//redraw(image.createGraphics());
				redraw(image.createGraphics(), DrawingContext.DC_IMAGE_EXPORT);
				// END KGU#906 2021-01-06
				// END KGU#221 2016-07-28
				// source: http://answers.yahoo.com/question/index?qid=20110821001157AAcdXVk
				// source: http://kalanir.blogspot.com/2010/02/how-to-split-image-into-chunks-java.html
				try {
					// 1. Load image file into memory
					//File file = new File("mario.png"); // mario.png in the same working directory
					//FileInputStream fis = new FileInputStream(file);
					//BufferedImage image = ImageIO.read(fis);

					// 2. Decide the number of pieces, and calculate the size of each chunk
					//int rows = 4;
					//int cols = 6;
					int chunks = rows * cols;

					int chunkWidth = image.getWidth() / cols;
					int chunkHeight = image.getHeight() / rows;
					// START KGU#223 2016-07-28: Bugfix #209 - identify the integer division defects
					int widthDefect = image.getWidth() % cols;
					int heightDefect = image.getHeight() % rows;
					// END KGU#223 2016-07-28

					// 3. Define an Image array to hold image chunks
					int count = 0;
					BufferedImage imgs[] = new BufferedImage[chunks];

					// 4. Fill the Image array with split image parts
					for (int x = 0; x < rows; x++) {
						for (int y = 0; y < cols; y++) {
							//Initialize the image array with image chunks
							// START KGU#223 2016-07-28: Bugfix #209
							// We must compensate the rounding defects lest the right and lower borders should be cut 
							//imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());
							int tileWidth = chunkWidth + (y < cols - 1 ? 0 : widthDefect);
							int tileHeight = chunkHeight + (x < rows - 1 ? 0 : heightDefect);
							imgs[count] = new BufferedImage(tileWidth, tileHeight, image.getType());
							// END KGU#223 2016-07-28

							// draws the image chunk
							Graphics2D gr = imgs[count++].createGraphics();
							// START KGU#223 2016-07-28: Bugfix #209
							//gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
							// We need to achieve transparent background
							gr.drawImage(image, 0, 0, tileWidth, tileHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + tileWidth, chunkHeight * x + tileHeight, null);
							// END KGU#223 2016-07-28
							gr.dispose();
						}
					}

					// 5. Save mini images into image files
					// START KGU#224 2016-07-28: Issue #209 - provide the original base name
					file = new File(filename);
					filename = file.getAbsolutePath();
					// END KGU#224 2016-07-28
					for (int i = 0; i < imgs.length; i++) {
						// START KGU#224 2016-07-28: Issue #209 - Better file name coding
						//File f = new File(file.getAbsolutePath().replace(".png", "-"+i+".png"));
						File f = new File(filename.replace(".png", String.format("-%1$02d-%2$02d.png", i / cols, i % cols)));
						// END KGU#224 2016-07-28
						ImageIO.write(imgs[i], "png", f);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this.getFrame(),
							Menu.msgErrorImageSave.getText(),
							Menu.msgTitleError.getText(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null) {
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
		// START KGU#456 2017-11-05: Enh. #452
		if (root.advanceTutorialState(26, root)) {
			analyse();
		}
		// END KGU#456 2017-11-05
	}

	/**
	 * Opens a {@link FileChooser} and performs the image export as PNG file.
	 *
	 * @see #exportPNGmulti()
	 * @see #exportSVG()
	 * @see #exportEMF()
	 * @see #exportPDF()
	 * @see #exportSWF()
	 */
	public void exportPNG() {
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU#41 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll(true);
		// END KGU#41 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as PNG ...");
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgSave);
		// END KGU#287 2017-01-09
		// set directory
		if (lastExportDir != null) {
			dlgSave.setCurrentDirectory(lastExportDir);
		} else if (root.getFile() != null) {
			dlgSave.setCurrentDirectory(root.getFile());
		} else {
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: There is already a suitable method
		//String nsdName = root.getText().get(0);
		//nsdName.replace(':', '_');
		//if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.PNGFilter());
		PNGFilter filter = new PNGFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		hideComments();	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			lastExportDir = dlgSave.getSelectedFile().getParentFile();
			String filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if (!filename.substring(filename.length() - 4, filename.length()).toLowerCase().equals(".png")) {
				filename += ".png";
			}

			File file = new File(filename);
			if (checkOverwrite(file, false) == 0) {
				BufferedImage bi = new BufferedImage(root.width + 1, root.height + 1, BufferedImage.TYPE_4BYTE_ABGR);
				// START KGU#221 2016-07-28: Issue #208 Need to achieve transparent background
				//printAll(bi.getGraphics());
				// START KGU#906 2021-01-06: Enh. #905
				//redraw(bi.createGraphics());
				redraw(bi.createGraphics(), DrawingContext.DC_IMAGE_EXPORT);
				// END KGU#906 2021-01-06
				// END KGU#221 2016-07-28
				try {
					ImageIO.write(bi, "png", file);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this.getFrame(),
							Menu.msgErrorImageSave.getText(),
							Menu.msgTitleError.getText(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null) {
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
		// START KGU#456 2017-11-05: Enh. #452
		if (root.advanceTutorialState(26, root)) {
			analyse();
		}
		// END KGU#456 2017-11-05
	}

	/**
	 * Opens a {@link FileChooser} and performs the image export as EMF file.
	 *
	 * @see #exportPNG()
	 * @see #exportPNGmulti()
	 * @see #exportSVG()
	 * @see #exportPDF()
	 * @see #exportSWF()
	 */
	public void exportEMF() {
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU#41 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll(true);
		// END KGU#41 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as EMF ...");
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgSave);
		// END KGU#287 2017-01-09
		// set directory
		if (lastExportDir != null) {
			dlgSave.setCurrentDirectory(lastExportDir);
		} else if (root.getFile() != null) {
			dlgSave.setCurrentDirectory(root.getFile());
		} else {
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: D.R.Y. - There is already a suitable method
		//		String nsdName = root.getText().get(0);
		//		nsdName.replace(':', '_');
		//		if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//		if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.EMFFilter());
		EMFFilter filter = new EMFFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		hideComments();	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			lastExportDir = dlgSave.getSelectedFile().getParentFile();
			String filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if (!filename.substring(filename.length() - 4, filename.length()).toLowerCase().equals(".emf")) {
				filename += ".emf";
			}

			File file = new File(filename);
			if (checkOverwrite(file, false) == 0) {
				try {
					EMFGraphics2D emf = new EMFGraphics2D(new FileOutputStream(filename),
							new Dimension(root.width + 12, root.height + 12));

					emf.startExport();
					lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(emf);
					lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
					myrect.left += 6;
					myrect.top += 6;
					root.draw(c, myrect, null, false);
					emf.endExport();
				} catch (Exception e) {
					// START KGU#484 2018-04-05: Issue #463
					//e.printStackTrace();
					logger.log(Level.WARNING, "Trouble exporting as image.", e);
					// END KGU#484 2018-04-05
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restor old selection
		selected = wasSelected;
		if (selected != null) {
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
		// START KGU#456 2017-11-05: Enh. #452
		if (root.advanceTutorialState(26, root)) {
			analyse();
		}
		// END KGU#456 2017-11-05
	}

	/**
	 * Opens a {@link FileChooser} and performs the image export as SVG file.
	 *
	 * @see #exportPNG()
	 * @see #exportPNGmulti()
	 * @see #exportEMF()
	 * @see #exportPDF()
	 * @see #exportSWF()
	 */
	public void exportSVG() // does not work!!
	{
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU#41 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll(true);
		// END KGU#41 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as SVG ...");
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgSave);
		// END KGU#287 2017-01-09
		// set directory
		if (lastExportDir != null) {
			dlgSave.setCurrentDirectory(lastExportDir);
		} else if (root.getFile() != null) {
			dlgSave.setCurrentDirectory(root.getFile());
		} else {
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: D.R.Y. - there is already a suitable method
		//String nsdName = root.getText().get(0);
		//nsdName.replace(':', '_');
		//if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.SVGFilter());
		SVGFilter filter = new SVGFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		hideComments();	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			lastExportDir = dlgSave.getSelectedFile().getParentFile();
			String filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if (!filename.substring(filename.length() - 4, filename.length()).toLowerCase().equals(".svg")) {
				filename += ".svg";
			}

			File file = new File(filename);
			if (checkOverwrite(file, false) == 0) {
				try {
					SVGGraphics2D svg = new SVGGraphics2D(new FileOutputStream(filename), new Dimension(root.width + 12, root.height + 12));
					svg.startExport();
					lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(svg);
					lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
					myrect.left += 6;
					myrect.top += 6;
					root.draw(c, myrect, null, false);
					svg.endExport();

					// re-read the file ...
					StringBuffer buffer = new StringBuffer();
					InputStreamReader isr = new InputStreamReader(new FileInputStream(filename));
					Reader in = new BufferedReader(isr);
					int ch;
					while ((ch = in.read()) > -1) {
						buffer.append((char) ch);
					}
					// START KGU 2015-12-04
					in.close();
					// END KGU 2015-12-04

					// ... and encode it UTF-8
					FileOutputStream fos = new FileOutputStream(filename);
					Writer out = new OutputStreamWriter(fos, "UTF-8");
					out.write(buffer.toString());
					out.close();

				} catch (Exception e) {
					// START KGU#484 2018-04-05: Issue #463
					//e.printStackTrace();
					logger.log(Level.WARNING, "Trouble exporting as image.", e);
					// END KGU#484 2018-04-05
				}
			}
		}

		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		//unselectAll();
		selected = wasSelected;
		if (selected != null) {
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
		// START KGU#456 2017-11-05: Enh. #452
		if (root.advanceTutorialState(26, root)) {
			analyse();
		}
		// END KGU#456 2017-11-05
	}

	/**
	 * Opens a {@link FileChooser} and performs the image export as SWF file.
	 *
	 * @see #exportPNG()
	 * @see #exportPNGmulti()
	 * @see #exportSVG()
	 * @see #exportPDF()
	 * @see #exportEMF()
	 */
	public void exportSWF() {
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll(true);
		// END KGU 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as SWF ...");
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgSave);
		// END KGU#287 2017-01-09
		// set directory
		if (lastExportDir != null) {
			dlgSave.setCurrentDirectory(lastExportDir);
		} else if (root.getFile() != null) {
			dlgSave.setCurrentDirectory(root.getFile());
		} else {
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: D.R.Y. - there is already a suitable method
		//String nsdName = root.getText().get(0);
		//nsdName.replace(':', '_');
		//if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.SWFFilter());
		SWFFilter filter = new SWFFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		hideComments();	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			lastExportDir = dlgSave.getSelectedFile().getParentFile();
			String filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if (!filename.substring(filename.length() - 4, filename.length()).toLowerCase().equals(".swf")) {
				filename += ".swf";
			}

			File file = new File(filename);
			if (checkOverwrite(file, false) == 0) {
				try {
					SWFGraphics2D svg = new SWFGraphics2D(new FileOutputStream(filename), new Dimension(root.width + 12, root.height + 12));

					svg.startExport();
					lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(svg);
					lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
					myrect.left += 6;
					myrect.top += 6;
					root.draw(c, myrect, null, false);
					svg.endExport();
				} catch (Exception e) {
					// START KGU#484 2018-04-05: Issue #463
					//e.printStackTrace();
					logger.log(Level.WARNING, "Trouble exporting as image.", e);
					// END KGU#484 2018-04-05
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null) {
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
		// START KGU#456 2017-11-05: Enh. #452
		if (root.advanceTutorialState(26, root)) {
			analyse();
		}
		// END KGU#456 2017-11-05
	}

	/**
	 * Opens a {@link FileChooser} and performs the image export as PDF file.
	 *
	 * @see #exportPNG()
	 * @see #exportPNGmulti()
	 * @see #exportSVG()
	 * @see #exportEMF()
	 * @see #exportSWF()
	 * @see #printNSD()
	 */
	public void exportPDF() {
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll(true);
		// END KGU 2015-10-11

		JFileChooser dlgSave = new JFileChooser("Export diagram as PDF ...");
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgSave);
		// END KGU#287 2017-01-09
		// set directory
		if (lastExportDir != null) {
			dlgSave.setCurrentDirectory(lastExportDir);
		} else if (root.getFile() != null) {
			dlgSave.setCurrentDirectory(root.getFile());
		} else {
			dlgSave.setCurrentDirectory(currentDirectory);
		}
		// propose name
		// START KGU 2015-10-16: D.R.Y. - there is already a suitable method
		//String nsdName = root.getText().get(0);
		//nsdName.replace(':', '_');
		//if(nsdName.indexOf(" (")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf(" ("));}
		//if(nsdName.indexOf("(")>=0) {nsdName=nsdName.substring(0,nsdName.indexOf("("));}
		String nsdName = root.proposeFileName();
		// END KGU 2015-10-16
		dlgSave.setSelectedFile(new File(nsdName));

		// START KGU 2016-04-01: Enh. #110 - select the provided filter
		//dlgSave.addChoosableFileFilter(new lu.fisch.structorizer.io.PDFFilter());
		PDFFilter filter = new PDFFilter();
		dlgSave.addChoosableFileFilter(filter);
		dlgSave.setFileFilter(filter);
		hideComments();	// Issue #143: Hide the current comment popup if visible
		// END KGU 2016-04-01
		int result = dlgSave.showSaveDialog(NSDControl.getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			lastExportDir = dlgSave.getSelectedFile().getParentFile();
			String filename = dlgSave.getSelectedFile().getAbsoluteFile().toString();
			if (!filename.substring(filename.length() - 4, filename.length()).toLowerCase().equals(".pdf")) {
				filename += ".pdf";
			}

			File file = new File(filename);
			if (checkOverwrite(file, false) == 0) {
				try {
					PDFGraphics2D pdf = new PDFGraphics2D(new FileOutputStream(filename), new Dimension(root.width + 12, root.height + 12));

					pdf.startExport();
					lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(pdf);
					lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
					myrect.left += 6;
					myrect.top += 6;
					root.draw(c, myrect, null, false);
					pdf.endExport();
				} catch (Exception e) {
					// START KGU#484 2018-04-05: Issue #463
					//e.printStackTrace();
					logger.log(Level.WARNING, "Trouble exporting as image.", e);
					// END KGU#484 2018-04-05
				}
			}
		}
		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null) {
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
		// START KGU#456 2017-11-05: Enh. #452
		if (root.advanceTutorialState(26, root)) {
			analyse();
		}
		// END KGU#456 2017-11-05
	}

	// START KGU#396 2020-03-03: Enh. #440 Specific export interface for PapDesigner
	/**
	 * Exports the current diagram (possibly with all referenced subdiagrams) as
	 * PAP flowcharts compatible with PapDesigner.
	 *
	 * @param din66001_1982 - whether the newer DIN 66001 (from 1982) is to be
	 * applied (otherwise the obsolete standard version from 1966 will be
	 * adhered to)
	 * @see #exportPap(Root, boolean)
	 */
	public void exportPap(boolean din66001_1982) {
		exportPap(root, din66001_1982);
	}

	/**
	 * Exports the given diagram {@code _root} (possibly with all referenced
	 * subdiagrams) as PAP flowchart compatible with PapDesigner
	 *
	 * @param _root - the top level {@link Root} to be exported
	 * @param din66001_1982 - whether the newer DIN 66001 (from 1982) is to be
	 * applied (otherwise the obsolete standard version from 1966 will be
	 * adhered to) #see {@link #exportPap(boolean)}
	 */
	public void exportPap(Root _root, boolean din66001_1982) {
		try {
			Generator gen = new PapGenerator();
			gen.setPluginOption("din66001_1982", din66001_1982);
			hideComments();	// Hide the current comment popup if visible
			File exportDir
			= gen.exportCode(_root,
					(lastCodeExportDir != null ? lastCodeExportDir : currentDirectory),
					NSDControl.getFrame(),
					(Arranger.hasInstance() ? Arranger.getInstance() : null));
			if (exportDir != null) {
				this.lastCodeExportDir = exportDir;
			}
		} catch (Exception ex) {
			String message = ex.getLocalizedMessage();
			if (message == null) {
				message = ex.getMessage();
			}
			if (message == null || message.isEmpty()) {
				message = ex.toString();
			}
			logger.log(Level.CONFIG, message, ex);
			JOptionPane.showMessageDialog(this.getFrame(),
					Menu.msgErrorUsingGenerator.getText().replace("%", PapGenerator.class.getSimpleName()) + "\n" + message,
					Menu.msgTitleError.getText(),
					JOptionPane.ERROR_MESSAGE);
		}
	}
	// END KGU#396 2020-03-03


	/*========================================
	 * Import method for foreign diagrams
	 *========================================*/
	// START KGU#386 2017-04-25: version 3.26-06 - new import sources
	/**
	 * Imports diagrams from alien file formats (e.g. from Struktogrammeditor)
	 *
	 * @param _className - Name of the appropriate importer class (to be
	 * configured via plugins)
	 * @param _specificOptions - importer-specific key-value pairs
	 */
	public void importNSD(String _className, Vector<HashMap<String, String>> _specificOptions) {
		// only save if something has been changed
		// START KGU#1028 2022-05-08: Bugfix #1033 now integrated in setRoot()
		//saveNSD(true);
		// END KGU#1028 2022-05-08

		if (!this.checkRunning()) {
			return;	// Don't proceed if the root is being executed
		}
		// open an existing file
		// create dialog
		JFileChooser dlgOpen = new JFileChooser();
		// Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgOpen);
		INSDImporter parser = null;
		try {
			// FIXME: For future Java versions we may need a factory here
			Class<?> impClass = Class.forName(_className);
			parser = (INSDImporter) impClass.getDeclaredConstructor().newInstance();

			dlgOpen.setDialogTitle(Menu.msgTitleNSDImport.getText().replace("%", parser.getDialogTitle()));
			// set directory
			dlgOpen.setCurrentDirectory(currentDirectory);
			// config dialogue
			FileFilter filter = parser.getFileFilter();
			dlgOpen.addChoosableFileFilter(filter);
			dlgOpen.setFileFilter(filter);
			// show & get result
			int result = dlgOpen.showOpenDialog(this);
			// react to result
			if (result == JFileChooser.APPROVE_OPTION) {
				//boolean hil = root.highlightVars;
				// FIXME: Replace this with a generalized version of openNSD(String)
				// START KGU#1028 2022-05-08: Bugfix #1033 - Report list got stale
				//root = parser.parse(dlgOpen.getSelectedFile().toURI().toString());
				Root root = parser.parse(dlgOpen.getSelectedFile().toURI().toString());
				if (root != null) {
				// END KGU#1028 2022-05-08
					//root.highlightVars = hil;
					if (Element.E_VARHIGHLIGHT) {
						root.retrieveVarNames();	// Initialise the variable table, otherwise the highlighting won't work
					}
					currentDirectory = dlgOpen.getSelectedFile();
				// START KGU#1028 2022-05-08: Bugfix #1033 - Report list got stale
				//redraw();
					this.setRoot(root, true, true, true);
				}
				// END KGU#1028 2022-05-08
			}
		} catch (Exception ex) {
			String message = ex.getLocalizedMessage();
			if (message == null) {
				message = ex.getMessage();
			}
			if (message == null || message.isEmpty()) {
				message = ex.toString();
			}
			JOptionPane.showMessageDialog(this.getFrame(), message,
					Menu.msgTitleError.getText(), JOptionPane.ERROR_MESSAGE);
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			logger.log(Level.WARNING, "Using parser " + _className + " failed.", ex);
			// END KGU#484 2018-04-05
		}
		// START KGU#444/KGU#618 2018-12-18: Issue #417, #649
		this.adaptScrollUnits();
		// END KGU#444/KGU#618 2018-12-18
	}
	// END KGU#386 2017-04-25

	/*========================================
	 * import methods for code
	 *========================================*/
	// START KGU#537 2018-06-29: Enh. #553
	/**
	 * Internal helper class for the background parsing of code to be imported.
	 *
	 * @author Kay Gürtzig
	 */
	private class ImportWorker extends SwingWorker<List<Root>, Integer> {

		private CodeParser parser;
		private File file;
		private Ini ini;
		private String logPath;

		public ImportWorker(CodeParser _parser, File _file, Ini _ini, String _logPath) {
			this.parser = _parser;
			this.file = _file;
			this.ini = _ini;
			this.logPath = _logPath;
		}

		@Override
		protected List<Root> doInBackground() throws Exception {
			//System.out.println("*** " + this.getClass().getSimpleName()+" going to work!");
			this.parser.setSwingWorker(this);
			List<Root> roots = null;
			roots = parser.parse(file.getAbsolutePath(),
					ini.getProperty("impImportCharset", "ISO-8859-1"),
					// START KGU#354 2017-04-27: Enh. #354
					logPath
					// END KGU#354 2017-04-27
					);
			return roots;
		}

	}
	// END KGU#537 2018-06-30

	/**
	 * Gets an instance of the given parser class, interactively selects a
	 * source file for the chosen language parses the file and tries to build a
	 * structogram from it in a background thread.
	 *
	 * @param options
	 */
	public void importCode(/*String _parserClassName,*/) {
		// only save if something has been changed
		saveNSD(true);

		CodeParser parser = null;

		// START KGU#354 2017-03-14: Enh. #354
		this.retrieveParsers();
		// END KGU#354 2017-03-14

		JFileChooser dlgOpen = new JFileChooser();
		// START KGU#287 2017-01-09: Bugfix #330 Ensure Label items etc. be scaled for L&F "Nimbus"
		GUIScaler.rescaleComponents(dlgOpen);
		// END KGU#287 2017-01-09
		dlgOpen.setDialogTitle(Menu.msgTitleImport.getText());
		// set directory
		// START KGU#354 2017-04-26: Enh. #354
		//if(root.getFile()!=null)
		//{
		//	dlgOpen.setCurrentDirectory(root.getFile());
		//}
		File importDir = this.lastCodeImportDir;
		if (importDir != null || (importDir = root.getFile()) != null) {
			dlgOpen.setCurrentDirectory(importDir);
		}
		// END KGU#354 2017-04-26
		else {
			dlgOpen.setCurrentDirectory(currentDirectory);
		}

		for (CodeParser psr : parsers) {
			dlgOpen.addChoosableFileFilter(psr);
			// START KGU#354 2017-04-26: Enh. #354 GUI improvement 
			if (psr.getDialogTitle().equals(this.lastImportFilter)) {
				dlgOpen.setFileFilter(psr);
			}
		}
		//dlgOpen.setFileFilter(parser);

		hideComments();	// Issue #143: Hide the current comment popup if visible
		int result = dlgOpen.showOpenDialog(NSDControl.getFrame());

		if (result == JFileChooser.APPROVE_OPTION) {
			File file = dlgOpen.getSelectedFile().getAbsoluteFile();

			if (!file.canRead()) {
				JOptionPane.showMessageDialog(this.getFrame(),
						Menu.msgImportFileReadError.getText().replace("%", file.getPath()));
				return;
			}

			// Identify a suited or the selected parser
			javax.swing.filechooser.FileFilter filter = dlgOpen.getFileFilter();

			parser = identifyParser(file, filter);

			if (parser == null) {
				JOptionPane.showMessageDialog(this.getFrame(),
						Menu.msgImportCancelled.getText().replace("%", file.getPath()));
				return;
			}

			// START KGU#354 2017-04-26: Enh. #354
			this.lastImportFilter = parser.getDialogTitle();
			this.lastCodeImportDir = file.getParentFile();
			// END KGU#354 2017-04-26

			Cursor origCursor = getCursor();
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));

				// load and parse source-code
				//CParser cp = new CParser("C-ANSI.cgt");
				// START KGU#194 2016-05-08: Bugfix #185 - mechanism for multiple roots per file
				//Root rootNew = d7.parse(filename);
				// START KGU#265 2016-09-28: Enh. #253 brought the Charset configuration. So make use of it.
				//List<Root> newRoots = d7.parse(filename, "ISO-8859-1");
				Ini ini = Ini.getInstance();
				// START KGU#354 2017-04-27: Enh. #354
				boolean isVerbose = ini.getProperty("impLogToDir", "false").equals("true");
				String logPath = null;
				if (isVerbose) {
					logPath = ini.getProperty("impLogDir", "");
					if (logPath.isEmpty()) {
						logPath = file.getParent();
					} else if (logPath.equals(".")) {
						if (currentDirectory != null) {
							if (!currentDirectory.isDirectory()) {
								logPath = currentDirectory.getParent();
							} else {
								logPath = currentDirectory.getPath();
							}
						}
					}
				}
				// END KGU#354 2017-04-27
				// START KGU#354 2017-05-11: Enh. #354 - we better use a new instance instead of statically sharing it
				parser = parser.getClass().getDeclaredConstructor().newInstance();
				// END KGU#354 2017-05-11
				// START KGU#395 2017-07-02: Enh. #357
				String pluginKey = parser.getClass().getSimpleName();
				for (int i = 0; i < parserPlugins.size(); i++) {
					GENPlugin plug = parserPlugins.get(i);
					if (plug.getKey().equals(pluginKey)) {
						this.setPluginSpecificOptions(parser, plug.options);
					}
				}
				// END KGU#395 2017-07-02
				// START KGU#537 2018-06-30: Enh. #553
				//List<Root> newRoots = parser.parse(file.getAbsolutePath(),
				//		ini.getProperty("impImportCharset", "ISO-8859-1"),
				//		// START KGU#354 2017-04-27: Enh. #354
				//		logPath
				//		// END KGU#354 2017-04-27
				//		);
				ImportWorker worker = new ImportWorker(parser, file, ini, logPath);
				// Pop up the progress monitor (it will be closed via the OK buttons).
				new CodeImportMonitor(this.getFrame(), worker, parser.getDialogTitle());
				List<Root> newRoots = worker.get();
				// END KGU#537 2018-06-30
				// END KGU#265 2016-09-28
				// END KGU#194 2016-05-08
				if (parser.error.equals("") && !worker.isCancelled()) {
					//boolean hil = root.highlightVars;
					// START KGU#194 2016-05-08: Bugfix #185 - there may be multiple routines 
					Root firstRoot = null;
					//root = rootNew;
					Iterator<Root> iter = newRoots.iterator();
					if (iter.hasNext()) {
						firstRoot = iter.next();
					}
					// START KGU#553 2018-07-10: In case of too many diagrams Structorizer would go zombie
					int nRoots = newRoots.size();
					int maxRoots = Integer.parseInt(ini.getProperty("impMaxRootsForDisplay", "20"));
					if (nRoots > maxRoots) {
						String[] options = {Menu.lblContinue.getText(), Menu.lblCancel.getText()};
						int chosen = JOptionPane.showOptionDialog(this.getFrame(),
								Menu.msgTooManyDiagrams.getText().replace("%", Integer.toString(maxRoots)),
								Menu.ttlCodeImport.getText(),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null,
								options, 0);
						if (chosen != JOptionPane.OK_OPTION) {
							newRoots.clear();
							iter = newRoots.iterator();
						}
						startSerialMode();
						try {
							while (iter.hasNext() && getSerialDecision(SerialDecisionAspect.SERIAL_SAVE) != SerialDecisionStatus.NO_TO_ALL) {
								Root nextRoot = iter.next();
								//nextRoot.highlightVars = hil;
								nextRoot.setChanged(false);
								// If the saving attempt fails, ask whether the saving loop is to be cancelled 
								if (!this.saveNSD(nextRoot, false)) {
									if (JOptionPane.showConfirmDialog(
											this.getFrame(),
											Menu.msgCancelAll.getText(),
											Menu.ttlCodeImport.getText(),
											JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
										// User decided not to save further diagrams.
										setSerialDecision(SerialDecisionAspect.SERIAL_SAVE, false);
									}
									// Saving failed, but no abort, so go on with next file (don't change status)
								}
							}
						} finally {
							endSerialMode();
							// Now we must prevent Structorizer from loading the diagrams nevertheless
							newRoots.clear();
							iter = newRoots.iterator();
							nRoots = 1;
						}
					}
					// END KGU#553 2018-07-10
					// START KGU#1076 2023-09-12: Bugfix #1086 incomplete establishment of arrangement group on C99 import
					Root newMain = null;
					// END KGU#1076 2023-09-12
					while (iter.hasNext()) {
						root = iter.next();
						// START KGU#1076 2023-09-12: Bugfix #1086
						if (newMain == null && root.isProgram()) {
							newMain = root;
						}
						// END KGU#1076 2023-09-12
						//root.highlightVars = hil;
						if (Element.E_VARHIGHLIGHT) {
							root.retrieveVarNames();	// Initialise the variable table, otherwise the highlighting won't work
						}
						// The Root must be marked for saving
						root.setChanged(false);
						// ... and be added to the Arranger
						// START KGU#626 2018-12-28 Enh. #657 - group management introduced
						//this.arrangeNSD();
						this.arrangeNSD(file.getName());
						// END KGU#626 2018-12-28
						Arranger.getInstance().enableNotification(false);
					}
					if (firstRoot != null) {
						root = firstRoot;
					// END KGU#194 2016-05-08
						//root.highlightVars = hil;
						if (Element.E_VARHIGHLIGHT) {
							root.retrieveVarNames();	// Initialise the variable table, otherwise the highlighting won't work
						}
						// START KGU#183 2016-04-24: Enh. #169
						selected = root;
						selected.setSelected(true);
						// END KGU#183 2016-04-24
						// START KGU#192 2016-05-02: #184 - The Root must be marked for saving
						root.setChanged(false);
						// END KGU#192 2016-05-02
						// START KGU#354 2017-05-23: Enh.#354 - with many roots it's better to push the principal root to the Arranger, too
						// START KGU#626 2018-12-28: Enh. #657 - with groups, push the main diagram, too, also in case of a program
						//if (nRoots > 2 || !root.isProgram()) {
						//	this.arrangeNSD();
						//}
						// START KGU#1076 2023-09-12: Bugfix #1086 - obvious dyscalculia...
						//if (nRoots > 2) {
						if (nRoots >= 2) {
						// END KGU#1076 2023-09-12
							this.arrangeNSD(file.getName());
						}
						// END KGU#626 2018-12-28
						// END KGU#354 2017-05-23
						// START KGU#1076 2023-09-12: Bugfix #1086
						if (!firstRoot.isProgram() && newMain != null) {
							this.setRoot(newMain, false, false, true);
						}
						// END KGU#1076 2023-09-12
					// START KGU#194 2016-05-08: Bugfix #185 - multiple routines per file
					}
					// END KGU#194 2016-05-08
				} else {
					// show error
					// START KGU 2016-01-11: Yes and No buttons somewhat strange...
					//JOptionPane.showOptionDialog(null,d7.error,
					//							 "Parser Error",
					//							 JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);
					// START KGU#364 2017-12-12: Issue #471 - Allow to copy the content
					//JOptionPane.showMessageDialog(this.NSDControl.getFrame(),
					//		parser.error,
					//		Menu.msgTitleParserError.getText(),
					//		JOptionPane.ERROR_MESSAGE, null);
					String[] options = {Menu.lblOk.getText(), Menu.lblCopyToClipBoard.getText()};
					int chosen = JOptionPane.showOptionDialog(this.getFrame(),
							parser.error,
							Menu.msgTitleParserError.getText(),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE, null,
							options, 0);
					if (chosen == 1) {
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						// START KGU#604 2018-10-29: Enh. #627 - Append a stacktrace if available 
						//StringSelection toClip = new StringSelection(parser.error);
						String errorString = parser.error;
						if (parser.exception != null) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							parser.exception.printStackTrace(new PrintStream(baos));
							errorString += "\n\nSTACK TRACE\n(A more detailed trace will be in the structorizer log file):\n\n" + baos.toString();
						}
						StringSelection toClip = new StringSelection(errorString);
						// END KGU#604 2018-10-29
						clipboard.setContents(toClip, null);
					}
					// END KGU#364 2017-12-12
					// END KGU 2016-01-11
				}
			} catch (java.util.concurrent.CancellationException ex) {
				JOptionPane.showMessageDialog(this.getFrame(),
						Menu.msgImportCancelled.getText().replace("%", file.getPath()));
			} catch (Exception ex) {
				String message = ex.getLocalizedMessage();
				if (message == null) {
					message = ex.getMessage();
					// START KGU#484 2018-04-05: Issue #463
					//ex.printStackTrace();
					logger.log(Level.WARNING, "", ex);
					// END KGU#484 2018-04-05
				}
				if (message == null || message.isEmpty()) {
					message = ex.toString();
				}
				JOptionPane.showMessageDialog(this.getFrame(),
						Menu.msgErrorUsingParser.getText().replace("%", parser.getDialogTitle()) + "\n" + message,
						Menu.msgTitleError.getText(),
						JOptionPane.ERROR_MESSAGE);
			} finally {
				doButtons();
				redraw();
				analyse();
				// START KGU#444/KGU#618 2018-12-18: Issue #417, #649 - We may have obtained huge diagrams...
				this.adaptScrollUnits();
				// END KGU#444/KGU#618 2018-12-18
				// START KGU#705 2019-09-24: Enh. #738
				updateCodePreview();
				// END KGU#705 2019-09-24
				setCursor(origCursor);
				if (Arranger.hasInstance()) {
					// KGU#947 2021-03-01 Bugfix #950 Bad implementation caused permanent loss of notifications
					Arranger.getInstance().enableNotification(true);
				}
			}
		}
	}

	// START KGU#354 2017-03-15: Enh. #354 - auxiliary methods
	// Tries to disambiguate the parser for the given file
	private CodeParser identifyParser(File file, FileFilter usedFilter) {
		CodeParser parser = null;

		Vector<CodeParser> candidates = new Vector<CodeParser>();
		String[] choice = new String[parsers.size()];
		Vector<String> candStrings = new Vector<String>();
		// We are better prepared for the ambiguous case...
		int nr0 = 1, nr = 1;
		final String format = "%2d: %s";
		for (CodeParser psr : parsers) {
			String descr = psr.getDescription();
			choice[nr0 - 1] = String.format(format, nr0, descr);
			nr0++;
			if (usedFilter == psr) {
				// The user had explicitly chosen this filter, so we are ready
				parser = psr;
				break;
			} else if (psr.accept(file)) {
				candidates.add(psr);
				candStrings.add(String.format(format, nr++, descr));
			}
		}

		if (parser == null) {
			if (candidates.size() == 1) {
				parser = candidates.get(0);
			} else {
				if (!candidates.isEmpty()) {
					choice = candStrings.toArray(new String[candStrings.size()]);
				} else {
					candidates = parsers;
				}
				JComboBox<String> cbParsers = new JComboBox<String>(choice);
				String prompt = Menu.msgSelectParser.getText().replace("%", file.getName());
				int resp = JOptionPane.showConfirmDialog(null,
						new Object[]{prompt, cbParsers},
						Menu.ttlCodeImport.getText(),
						JOptionPane.OK_CANCEL_OPTION);
				if (resp == JOptionPane.OK_OPTION) {
					int index = cbParsers.getSelectedIndex();
					// Well this test is of course mere paranoia...
					if (index >= 0 && index < candidates.size()) {
						parser = candidates.get(index);
					}
				}
			}
		}
		return parser;
	}

	/**
	 * Lazy initialization method for static field {@link #parsers}
	 */
	private void retrieveParsers() {
		if (parsers != null) {
			return;
		}
		parsers = new Vector<CodeParser>();
		String errors = "";
		try ( BufferedInputStream buff = new BufferedInputStream(getClass().getResourceAsStream("parsers.xml"))) {
			GENParser genp = new GENParser();
			parserPlugins = genp.parse(buff);
		} catch (IOException e) {
			// START KGU#484 2018-04-05: Issue #463
			//e.printStackTrace();
			logger.log(Level.WARNING, "Couldn't close parser plugin definition file.", e);
			// END KGU#484 2018-04-05
		}
		for (int i = 0; parserPlugins != null && i < parserPlugins.size(); i++) {
			GENPlugin plugin = parserPlugins.get(i);
			final String className = plugin.className;
			try {
				Class<?> genClass = Class.forName(className);
				parsers.add((CodeParser) genClass.getDeclaredConstructor().newInstance());
			} catch (Exception ex) {
				errors += "\n" + plugin.title + ": " + ex.getLocalizedMessage();
			}
		}
		if (!errors.isEmpty()) {
			errors = Menu.msgTitleLoadingError.getText() + errors;
			JOptionPane.showMessageDialog(this.getFrame(), errors,
					Menu.msgTitleParserError.getText(), JOptionPane.ERROR_MESSAGE);
		}
	}
	// END KGU#354 2017-03-15

	/*========================================
	 * export code methods
	 *========================================*/
	/**
	 * Export the current diagram to the programming language associated to the
	 * generator {@code _generatorClassName}
	 *
	 * @param _generatorClassName - class name of he generator to be used
	 * @param _specificOptions - generator-specific options
	 */
	public void export(String _generatorClassName, Vector<HashMap<String, String>> _specificOptions) {
		// START KGU#815 2020-02-20: Enh. 828 - We offer not only the export of groups but also of diagrams
		// (Code moved to export(Root, String, Vector<HashMap<String, String>))
		export(root, _generatorClassName, _specificOptions);
		// END KGU#815 2020-02-20
	}

	// START KGU#815 2020-03-16: Enh. #828
	/**
	 * Export the given diagram {@code _root} to the programming language
	 * associated to the generator {@code _generatorClassName}.
	 *
	 * @param _generatorClassName - class name of the generator to be used
	 * @param _specificOptions - generator-specific options
	 */
	public void export(Root _root, String _generatorClassName, Vector<HashMap<String, String>> _specificOptions) {
		// START KGU#901 2020-12-29: Issue #901 apply WAIT_CURSOR during time-consuming actions
		Cursor origCursor = getCursor();
		// END KGU#901 2020-12-29
		try {
			Class<?> genClass = Class.forName(_generatorClassName);
			Generator gen = (Generator) genClass.getDeclaredConstructor().newInstance();
			// START KGU#170 2016-04-01: Issue #143
			hideComments();	// Hide the current comment popup if visible
			// END KGU#170 2016-04-01
			// START KGU#815 2020-03-30: Enh. #828 If called from ArrangerIndex, options will be null
			if (_specificOptions == null) {
				for (GENPlugin plugin : Menu.generatorPlugins) {
					if (plugin.className.equals(_generatorClassName)) {
						_specificOptions = plugin.options;
						break;
					}
				}
				if (_specificOptions == null) {
					_specificOptions = new Vector<HashMap<String, String>>();
				}
			}
			// END KGU#815 2020-03-20
			// START KGU#395 2017-05-11: Enh. #357
			this.setPluginSpecificOptions(gen, _specificOptions);
			// END KGU#395 2017-05-11
			// START KGU#901 2020-12-29: Issue #901 applay WAIT_CURSOR for time-consuming actions
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			// END KGU#901 2020-12-29
			// START KGU 2017-04-26: Remember the export directory
			//gen.exportCode(root, currentDirectory, NSDControl.getFrame());
			// START KGU#654 2019-02-16: Enh. #681 Don't overwrite the last export dir in case the export failed or was cancelled
			//this.lastCodeExportDir =
			File exportDir =
			// END KGU#654 2019-02-16
					gen.exportCode(_root,
							(lastCodeExportDir != null ? lastCodeExportDir : currentDirectory),
							// START KGU#676/KGU#679 2019-03-13: Enh. #696,#698 Specify the routine pool expicitly
							//NSDControl.getFrame());
							NSDControl.getFrame(),
							(Arranger.hasInstance() ? Arranger.getInstance() : null));
							// END KGU#676 2019-03-13
			// START KGU#654 2019-02-16: Enh. #681
			// START KGU#456 2017-11-05: Enh. #452
			if (_root == root && root.advanceTutorialState(26, root)) {
				analyse();
			}
			// END KGU#456 2017-11-05
			// START KGU#654 2019-02-15/16: Enh. #681 - count the successful exports to the target language
			if (exportDir != null) {
				this.lastCodeExportDir = exportDir;

				String prefGenName = this.getPreferredGeneratorName();
				String thisGenName = null;
				for (GENPlugin plugin : Menu.generatorPlugins) {
					if (plugin.className.equals(_generatorClassName)) {
						thisGenName = plugin.title;
						break;
					}
				}
				if (thisGenName.equals(this.lastGeneratorName)) {
					if (++this.generatorUseCount == this.generatorProposalTrigger && this.generatorProposalTrigger > 0
							&& !prefGenName.equals(this.lastGeneratorName)) {
						// START KGU#901 2020-12-29: Issue #901 applay WAIT_CURSOR for time-consuming actions
						setCursor(origCursor);
						// END KGU#901 2020-12-29
						if (JOptionPane.showConfirmDialog(this.getFrame(),
								Menu.msgSetAsPreferredGenerator.getText().replace("%1", thisGenName).replaceAll("%2", Integer.toString(this.generatorUseCount)),
								Menu.lbFileExportCodeFavorite.getText().replace("%", thisGenName),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							this.prefGeneratorName = thisGenName;
							Ini.getInstance().setProperty("genExportPreferred", thisGenName);
							Ini.getInstance().save();
							// START KGU#705 2019-09-23: Enh. #738
							this.updateCodePreview();
							// END KGU#705 2019-09-23
							// doButtons() is assumed to be performed after his method had been called, anyway
						}
					}
				} else {
					this.lastGeneratorName = thisGenName;
					this.generatorUseCount = 1;
				}
			}
			// END KGU#654 2019-02-15/16
		} catch (Exception ex) {
			// START KGU#901 2020-12-29: Issue #901 applay WAIT_CURSOR for time-consuming actions
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			// END KGU#901 2020-12-29
			String message = ex.getLocalizedMessage();
			if (message == null) {
				message = ex.getMessage();
			}
			if (message == null || message.isEmpty()) {
				message = ex.toString();
			}
			logger.log(Level.CONFIG, message, ex);
			JOptionPane.showMessageDialog(this.getFrame(),
					Menu.msgErrorUsingGenerator.getText().replace("%", _generatorClassName) + "\n" + message,
					Menu.msgTitleError.getText(),
					JOptionPane.ERROR_MESSAGE);
		}
		// START KGU#901 2020-12-29: Issue #901 apply WAIT_CURSOR during time-consuming actions
		finally {
			setCursor(origCursor);
		}
		// END KGU#901 2020-12-29
	}

	/**
	 * Export the group represented by the programming language associated to
	 * the generator {@code _generatorClassName}
	 *
	 * @param group - The {@link Group} to be exported
	 * @param generatorName - class name of the generator to be used
	 * @param extraOptions - a possible extra option map (handled like plugin
	 * options) or null
	 */
	public void exportGroup(Group group, String generatorName, Map<String, Object> extraOptions) {
		hideComments();	// Hide the current comment popup if visible (issue #143)
		File groupFile = group.getFile();
		File targetDir = lastCodeExportDir;
		// START KGU#935 2021-02-12: Bugfix #936
		//if ((targetDir == null || Ini.getInstance().getProperty("", "true").equals("true")) && groupFile.exists()) {
		if ((targetDir == null || Ini.getInstance().getProperty("", "true").equals("true"))
				&& groupFile != null && groupFile.exists()) {
		// END KGU#935 2021-02-12
			targetDir = groupFile.getParentFile();
		}
		if (targetDir == null || !targetDir.exists()) {
			targetDir = currentDirectory;
		}
		String groupName = group.proposeFileName().replace(".", "_");

		// START KGU#901 2020-12-29: Issue #901 applay WAIT_CURSOR for time-consuming actions
		Cursor origCursor = getCursor();
		// END KGU#901 2020-12-29
		try {
			Class<?> genClass = Class.forName(generatorName);
			Generator gen = (Generator) genClass.getDeclaredConstructor().newInstance();
			Vector<HashMap<String, String>> options = null;
			for (GENPlugin plugin : Menu.generatorPlugins) {
				if (plugin.className.equals(generatorName)) {
					options = plugin.options;
					break;
				}
			}
			if (options == null) {
				options = new Vector<HashMap<String, String>>();
			}
			this.setPluginSpecificOptions(gen, options);
			// START KGU#396 2020-04-01: Temporary extra mechanism for #440
			if (extraOptions != null) {
				for (Map.Entry<String, Object> option : extraOptions.entrySet()) {
					gen.setPluginOption(option.getKey(), option.getValue());
				}
			}
			// END KGU#396 2020-04-01

			// START KGU#901 2020-12-29: Issue #901 applay WAIT_CURSOR for time-consuming actions
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			// END KGU#901 2020-12-29
			File exportDir = gen.exportCode(group.getSortedRoots(), groupName,
					targetDir,
					NSDControl.getFrame(),
					(Arranger.hasInstance() ? Arranger.getInstance() : null));

			if (exportDir != null) {
				this.lastCodeExportDir = exportDir;
			}
		} catch (Exception ex) {
			// START KGU#901 2020-12-29: Issue #901 applay WAIT_CURSOR for time-consuming actions
			setCursor(origCursor);
			// END KGU#901 2020-12-29
			String message = ex.getLocalizedMessage();
			if (message == null) {
				message = ex.getMessage();
			}
			if (message == null || message.isEmpty()) {
				message = ex.toString();
			}
			logger.log(Level.CONFIG, message, ex);
			JOptionPane.showMessageDialog(this.getFrame(),
					Menu.msgErrorUsingGenerator.getText().replace("%", generatorName) + "\n" + message,
					Menu.msgTitleError.getText(),
					JOptionPane.ERROR_MESSAGE);
		}
		// START KGU#901 2020-12-29: Issue #901 applay WAIT_CURSOR for time-consuming actions
		finally {
			setCursor(origCursor);
		}
		// END KGU#901 2020-12-29
	}
	// END KGU#815 2020-03-16

	// START KGU#705 2019-09-23: Enh. #738: Code preview support
	/**
	 * Place a code preview for the current diagram to the currrent favourite
	 * programming language. Also fills the {@link #codePreviewMap} with
	 * associations between {@link Element}s and line intervals.
	 *
	 * @param _specificOptions - generator-specific options
	 */
	public void updateCodePreview() {
		if (this.show_CODE_PREVIEW && this.codePreview != null) {
			String generatorName = this.getPreferredGeneratorName();
			try {
				codePreviewMap = new HashMap<Element, int[]>();
				Generator gen = null;
				Arranger arranger = null;
				if (Arranger.hasInstance()) {
					arranger = Arranger.getInstance();
				}
				for (GENPlugin plugin : Menu.generatorPlugins) {
					if (plugin.title.equals(generatorName)) {
						Class<?> genClass = Class.forName(plugin.className);
						gen = (Generator) genClass.getDeclaredConstructor().newInstance();
						setPluginSpecificOptions(gen, plugin.options);
						String code = gen.deriveCode(root,
								NSDControl.getFrame(),
								arranger,
								codePreviewMap);
						codePreview.setText(code);
						break;
					}
				}
				setCodePreviewTooltip();
			} catch (Exception ex) {
				String message = ex.getLocalizedMessage();
				if (message == null) {
					message = ex.getMessage();
				}
				if (message == null || message.isEmpty()) {
					message = ex.toString();
				}
				logger.log(Level.CONFIG, message, ex);
				JOptionPane.showMessageDialog(this.getFrame(),
						Menu.msgErrorUsingGenerator.getText().replace("%", generatorName) + "\n" + message,
						Menu.msgTitleError.getText(),
						JOptionPane.ERROR_MESSAGE);
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				highlightCodeForSelection();
			}
		});
		//highlightCodeForSelection();
	}
	// END KGU#705 2019-09-23

	// START KGU#395 2017-05-11: Enh. #357 / Revised KGU#416 2017-06-20, KGU#975 2021-06-03
	/**
	 * Retrieves plugin-specific options for the plugin-related class instance
	 * {@code _pluginInstance} (e.g. a generator or parser) from Ini and fills
	 * the option map of {@code _pluginInstance}.
	 * @param _pluginInstance - instance of a plugin-related class
	 * @param _specificOptions - vector of the plugin-specific option
	 * specifications (as key-value maps with keys "name", "type", "title",
	 * "help")
	 */
	private void setPluginSpecificOptions(IPluginClass _pluginInstance,
			Vector<HashMap<String, String>> _specificOptions)
	{
//		// END KGU#975 2021-06-03
//		for (HashMap<String, String> optionSpec: _specificOptions) {
//			String optionKey = optionSpec.get("name");
//			String valueStr = ini.getProperty(className + "." + optionKey, "");
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
//					else if (type.equalsIgnoreCase("unsigned")) {
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
//					String message = ex.getMessage();
//					if (message == null || message.isEmpty()) message = ex.toString();
//					logger.log(Level.SEVERE,"{0}: {1} on converting \"{2}\" to {3} for {4}",
//							new Object[]{
//									className,
//									message,
//									valueStr,
//									type,
//									optionKey});
//				}
//			}
//			if (value != null) {
//				_pluginInstance.setPluginOption(optionKey, value);
//			}
//		}
		StringList errors = _pluginInstance.setPluginOptionsFromIni(_specificOptions);
		for (int i = 0; i < errors.count(); i++) {
			logger.log(Level.SEVERE, errors.get(i));
		}
		// END KGU#977 2021-06-08
	}
	// END KGU#395 2017-05-11

	// START KGU#208 2016-07-22: Enh. #199
	/*========================================
	 * help method
	 *========================================*/
	/**
	 * Tries to open the online User Guide in the browser
	 */
	public void helpNSD() {
		// START KGU#563 2018-07-26: Issue #566
		//String help = "http://help.structorizer.fisch.lu/index.php";
		String help = Element.E_HELP_PAGE;
		// END KGU#563 2018-07-26
		boolean isLaunched = false;
		try {
			isLaunched = lu.fisch.utils.Desktop.browse(new URI(help));
		} catch (URISyntaxException ex) {
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			logger.log(Level.WARNING, "Can't browse help URL.", ex);
			// END KGU#484 2018-04-05
		}
		// START KGU 2018-12-24
		// The isLaunched mechanism above does not signal an unavailable help page.
		// With the following code we can find out whether the help page was available...
		// TODO In this case we might offer to download the PDF for offline use,
		// otherwise we could try to open a possibly previously downloaded PDF ...
		URL url;
		HttpsURLConnection con = null;
		try {
			isLaunched = false;
			url = new URL(help);
			con = (HttpsURLConnection) url.openConnection();
			if (con != null) {
				con.connect();
			}
			isLaunched = true;
		} catch (SocketTimeoutException ex) {
			logger.log(Level.WARNING, "Timeout connecting to " + help, ex);
		} catch (MalformedURLException e1) {
			logger.log(Level.SEVERE, "Malformed URL " + help, e1);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed Access to " + help, e);
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		// END KGU 2018-12-24
		if (!isLaunched) {
			String message = Menu.msgBrowseFailed.getText().replace("%", help);
			boolean offlineShown = this.showHelpPDF();
			if (offlineShown) {
				message += "\n\n" + Menu.msgShowingOfflineGuide.getText();
			}
			JOptionPane.showMessageDialog(null,
					message,
					Menu.msgTitleURLError.getText(),
					offlineShown ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
			// TODO We might look for a downloaded PDF version and offer to open this instead...

		}
		else {
			// Download the current PDF version if there hasn't been any by now.
			this.downloadHelpPDF(false, null);
		}
	}
	// END KGU#208 2016-07-22

	// START KGU#791 2010-10-20: Issue #801 - we need a background thread for explicit download
	private boolean helpDownloadCancelled = false;

	/**
	 * Tries to download the most recent user guide as PDF in a backround thread
	 * with progress bar. Will override a possibly existing file.
	 *
	 * @param title - the menu item caption to be used as window title
	 */
	public void downloadHelpPDF(String title) {
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

			@Override
			protected Boolean doInBackground() throws Exception {
				return downloadHelpPDF(true, this);
			}

			public void done() {
				if (isCancelled()) {
					// We must tell method downloadHelpPDF that the task was aborted
					// (The possibly incompletely transferred file must be deleted.)
					helpDownloadCancelled = true;
				}
			}

		};
		new DownloadMonitor(getFrame(), worker, title, Element.E_HELP_FILE_SIZE);
	}
	// END KGU#791 2020-10-20

	// START KGU#791 2020-01-20: Enh. #801 support offline help
	/**
	 * Tries to download the PDF version of the user guide to the ini directory
	 *
	 * @param overrideExisting - if an existing user guide file is to be
	 * overriden by the newest one
	 * @param worker - if given then the transfer chunks are chosen smaller and
	 * a regular progress message will be sent
	 * @return true if the download was done and successful.
	 */
	public boolean downloadHelpPDF(boolean overrideExisting, SwingWorker<Boolean, Void> worker) {
		/* See https://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
		 * for technical discussion 
		 */
		// KGU#791 2020-10-20 Method revised to allow running in a backround thread
		helpDownloadCancelled = false;
		String helpFileName = Element.E_HELP_FILE;
		File helpDir = Ini.getIniDirectory(true);
		File helpFile = new File(helpDir.getAbsolutePath() + File.separator + helpFileName);
		String helpFileURI = Element.E_DOWNLOAD_PAGE + "?file=" + helpFileName;
		boolean overwritten = false;
		long copiedTotal = 0;
		long chunk = (worker == null) ? Integer.MAX_VALUE : 1 << 16;
		try {
			URL website = new URL(helpFileURI);
			if (!helpFile.exists() || overrideExisting) {
				try (InputStream inputStream = website.openStream();
						ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
						FileOutputStream fileOutputStream = new FileOutputStream(helpFile)) {
					overwritten = true;
					long copied = 0;
					do {
						copied = fileOutputStream.getChannel().
								transferFrom(readableByteChannel, copiedTotal, chunk);
						if (worker != null) {
							worker.firePropertyChange("progress", copiedTotal, copiedTotal + copied);
						}
						copiedTotal += copied;
					} while (copied > 0);
				} catch (IOException ex) {
					logger.log(Level.INFO, "Failed to download help file!", ex);
					if (overrideExisting) {
						String error = ex.getMessage();
						if (error == null) {
							error = ex.toString();
						} else if (ex instanceof UnknownHostException) {
							error = Menu.msgHostNotAvailable.getText().replace("%", error);
						}
						JOptionPane.showMessageDialog(null,
								Menu.msgDownloadFailed.getText().replace("%", error),
								Menu.msgTitleURLError.getText(),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} catch (MalformedURLException ex) {
			logger.log(Level.CONFIG, helpFileURI, ex);
		}
		if (helpDownloadCancelled && overwritten && helpFile.exists()) {
			helpFile.delete();	// File is likely to be defective
			copiedTotal = 0;
		}
		//System.out.println("Leaving downloadHelpPDF()");
		return copiedTotal > 0;
	}

	/**
	 * Tries to present a downloaded PDF version of the user guide from the ini
	 * directory.
	 *
	 * @return true if a user guide file is present and could be shown.
	 */
	private boolean showHelpPDF() {
		String helpFileName = Element.E_HELP_FILE;
		File helpDir = Ini.getIniDirectory(true);
		File helpFile = new File(helpDir.getAbsolutePath() + File.separator + helpFileName);
		if (helpFile.canRead()) {
			return Desktop.open(helpFile);
		}
		return false;
	}
	// END KGU#791 2020-01-20

	/*========================================
	 * update method
	 *========================================*/
	/**
	 * Shows an info box with the link to the download page of Structorizer and
	 * informs whether there is a newer version available.
	 *
	 * @see #updateNSD(boolean)
	 */
	public void updateNSD() // START KGU#300 2016-12-02: Enh. #300
	{
		updateNSD(true);
	}

	/**
	 * Checks the availability of a newer version on the download page and shows
	 * an info box with the link to the download page of Structorizer if a new
	 * version is available or {@code evenWithoutNewerVersion} is true.
	 *
	 * @param evenWithoutNewerVersion - whether the infor box is always to be
	 * popped up.
	 * @see #updateNSD()
	 */
	public void updateNSD(boolean evenWithoutNewerVersion)
	// END KGU#300 2016-12-02
	{
		// KGU#35 2015-07-29: Bob's code adopted with slight modification (Homepage URL put into a variable)
		// START KGU#563 2018-07-26: Issue #566
		//final String home = "https://structorizer.fisch.lu";
		final String home = Element.E_HOME_PAGE;
		// END KGU#563 2018-07-26

		// START KGU#300 2016-12-02: Enh. #300
		String latestVersion = getLatestVersionIfNewer();
		if (!evenWithoutNewerVersion && latestVersion == null) {
			return;
		}
		// END KGU#300 2016-12-02

		try {
			// START KGU#247 2016-09-17: Issue #243/#245 Translation support for update window content
			//JEditorPane ep = new JEditorPane("text/html","<html><font face=\"Arial\">Goto <a href=\"" + home + "\">" + home + "</a> to look for updates<br>and news about Structorizer.</font></html>");
			String fontAttr = "";
			double scaleFactor = Double.valueOf(Ini.getInstance().getProperty("scaleFactor", "1"));
			if (scaleFactor > 1) {
				int fontSize = (int) (3 * scaleFactor);
				fontAttr = " size=" + fontSize;
			}
			// START KGU#300 2016-12-02: Enh. #300
			String versionInfo = "";
			if (latestVersion != null) {
				versionInfo = Menu.msgNewerVersionAvail.getText().replace("%", latestVersion) + "<br><br>";
			}
			// END KGU#300 2016-12-02
			JEditorPane ep = new JEditorPane("text/html", "<html><font face=\"Arial\"" + fontAttr + ">"
					// START KGU#300 2016-12-02: Enh. #300
					+ versionInfo
					// END KGU#300 2016-12-02
					+ Menu.msgGotoHomepage.getText().replace("%", "<a href=\"" + home + "\">" + home + "</a>")
					+ "</font></html>");
			// END KGU#247 2016-09-17
			ep.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent evt) {
					if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
						// START KGU#250 2016-09-17: Issue #245 (defective Linux integration workaround)
						//try {
						//	Desktop.getDesktop().browse(evt.getURL().toURI());
						//}
						//catch(Exception ex)
						//{
						//	ex.printStackTrace();
						//}
						String errorMessage = null;
						try {
							if (!lu.fisch.utils.Desktop.browse(evt.getURL().toURI())) {
								errorMessage = Menu.msgBrowseFailed.getText().replace("%", evt.getURL().toString());
							};
						} catch (Exception ex) {
							// START KGU#484 2018-04-05: Issue #463
							//ex.printStackTrace();
							logger.log(Level.WARNING, "Defective homepage link.", ex);
							// END KGU#484 2018-04-05
							errorMessage = ex.getLocalizedMessage();
							if (errorMessage == null) {
								errorMessage = ex.getMessage();
							}
							if (errorMessage == null || errorMessage.isEmpty()) {
								errorMessage = ex.toString();
							}
						}
						if (errorMessage != null) {
							JOptionPane.showMessageDialog(null,
									errorMessage,
									Menu.msgTitleURLError.getText(),
									JOptionPane.ERROR_MESSAGE);
						}
						// END KGU#250 2016-09-17
					}
				}
			});
			ep.setEditable(false);
			JLabel label = new JLabel();
			ep.setBackground(label.getBackground());

			JOptionPane.showMessageDialog(this.getFrame(), ep);
		} catch (Exception e) {
			// START KGU#484 2018-04-05: Issue #463
			//e.printStackTrace();
			logger.log(Level.WARNING, "Trouble accessing homepage.", e);
			// END KGU#484 2018-04-05
		}
	}

	// START KGU#300 2016-12-02 Enh. #300 Support for version retrieval
	/**
	 * Helper method for {@link #updateNSD()}
	 *
	 * @return the version string, e.g. "3.29-14", of the latest version
	 * available or null, depending on whether online version retrieval is
	 * enabled by {@link #retrieveVersion}.
	 */
	private String retrieveLatestVersion() {
		// START KGU#563 2018-07-26: Issue #566
		//final String http_url = "https://structorizer.fisch.lu/version.txt";
		final String http_url = Element.E_HOME_PAGE + "/version.txt";
		// END KGU#563 2018-07-26

		String version = null;
		if (retrieveVersion) {
			try {

				URL url = new URL(http_url);
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

				if (con != null) {

					BufferedReader br
					= new BufferedReader(
							new InputStreamReader(con.getInputStream()));

					String input;
					while ((input = br.readLine()) != null && version == null) {
						if (input.matches("\\d+\\.\\d+([-.][0-9]+)?")) {
							version = input;
						}
					}
					br.close();

				}

			} catch (MalformedURLException e) {
				logger.severe(e.toString());
			} catch (IOException e) {
				logger.warning(e.toString());
			}
		}
		return version;
	}

// START KGU#300 2016-12-06: Not actually needed
//	private static int[] splitVersionString(String version)
//	{
//		StringList versionParts = StringList.explode(version, "\\.");
//		versionParts = StringList.explode(versionParts, "-");
//		int[] versionNumbers = new int[versionParts.count()];
//		for (int i = 0; i < versionParts.count(); i++) {
//			try {
//				versionNumbers[i] = Integer.parseInt(versionParts.get(i));
//			}
//			catch (NumberFormatException ex) {
//				versionNumbers[i] = 0;
//			}
//		}
//		return versionNumbers;
//	}
// END KGU#300 2016-12-06

	/**
	 * @return the version string, e.g. "3.30-05", of the latest version more
	 * adavanced than the currently running version if online version retrieval
	 * is enabled ({@link #retrieveVersion}) and a newer version is available;
	 * null otherwise.
	 */
	public String getLatestVersionIfNewer() {
		int cmp = 0;
		String latestVerStr = retrieveLatestVersion();
		if (latestVerStr != null) {
			// START KGU#300 2016-12-06: The lexicographic comparison is quite perfect here
			//int[] thisVersion = splitVersionString(Element.E_VERSION);
			//int[] currVersion = splitVersionString(latestVerStr);
			//int minLen = Math.min(thisVersion.length, currVersion.length);
			//for (int i = 0; i < minLen && cmp == 0; i++) {
			//	if (currVersion[i] < thisVersion[i]) {
			//		cmp = -1;
			//	}
			//	else if (currVersion[i] > thisVersion[i]) {
			//		cmp = 1;
			//	}
			//}
			//if (cmp == 0 && minLen < currVersion.length) {
			//	cmp = 1;
			//}
			cmp = latestVerStr.compareTo(Element.E_VERSION);
			// END KGU#300 2016-12-06
		}
		return (cmp > 0 ? latestVerStr : null);
	}

	public void setRetrieveVersion(boolean _retrieveVersion) {
		retrieveVersion = _retrieveVersion;
		// START KGU#792 2020-02-04: Bugfix #805
		Ini.getInstance().setProperty("retrieveVersion", Boolean.toString(Diagram.retrieveVersion));
		// END KGU#792 2020-02-04
	}
	// END KGU#300 2016-12-02

	/*========================================
	 * the preferences dialog methods
	 *========================================*/
	/**
	 * Opens the colour configuration dialog and processes configuration
	 * changes.
	 */
	public void colorsNSD() {
		Colors colors = new Colors(NSDControl.getFrame(), Element.colors.length);
		Point p = getLocationOnScreen();
		colors.setLocation(
				Math.round(p.x + (getVisibleRect().width - colors.getWidth()) / 2 + this.getVisibleRect().x),
				Math.round(p.y + (getVisibleRect().height - colors.getHeight()) / 2 + this.getVisibleRect().y)
		);

		// set fields
		for (int i = 0; i < Element.colors.length; i++) {
			colors.colors[i].setBackground(Element.colors[i]);
		}

		colors.pack();
		colors.setVisible(true);

		// START KGU#393 2017-05-09: Issue #400 - check whether changes were committed
		if (colors.OK) {
		// END KGU#393 2017-05-09		
			// get fields
			for (int i = 0; i < Element.colors.length; i++) {
				Element.colors[i] = colors.colors[i].getBackground();
			}

			NSDControl.updateColors();

			// save fields to ini-file
			Element.saveToINI();
		// START KGU#393 2017-05-09: Issue #400
		}
		// END KGU#393 2017-05-09		

	}

	/**
	 * Opens the structure prefereneces dialog and processes configuration
	 * changes.
	 */
	public void preferencesNSD() {
		Preferences preferences = new Preferences(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		preferences.setLocation(
				Math.round(p.x + (getVisibleRect().width - preferences.getWidth()) / 2 + this.getVisibleRect().x),
				Math.round(p.y + (getVisibleRect().height - preferences.getHeight()) / 2 + this.getVisibleRect().y)
		);

		// set fields
		preferences.edtAltT.setText(Element.preAltT);
		preferences.edtAltF.setText(Element.preAltF);
		preferences.edtAlt.setText(Element.preAlt);
		preferences.txtCase.setText(Element.preCase);
		preferences.edtFor.setText(Element.preFor);
		preferences.edtWhile.setText(Element.preWhile);
		preferences.edtRepeat.setText(Element.preRepeat);

		preferences.altPadRight.setSelected(Element.altPadRight);

		// START KGU#401 2017-05-18: Issue #405 - allow to reduce CASE width by branch element rotation
		preferences.spnCaseRot.setValue(Element.caseShrinkByRot);
		// END KGU#401 2017-05-18
		// START KGU#376 2017-07-02: Enh. #389
		preferences.edtRoot.setText(Element.preImport);
		// END KGU#376 2017-07-02
		// START KGU#916 2021-01-25: Enh. #915
		preferences.chkCaseEditor.setSelected(Element.useInputBoxCase);
		// END KGU#916 2021-01-25

		// START KGU#686 2019-03-22: Enh. #56
		preferences.edtTry.setText(Element.preTry);
		preferences.edtCatch.setText(Element.preCatch);
		preferences.edtFinal.setText(Element.preFinally);
		// END KGU#686 2019-03-22

		preferences.pack();
		preferences.setVisible(true);

		// START KGU#393 2017-05-09: Issue #400 - check whether changes were committed
		if (preferences.OK) {
		// END KGU#393 2017-05-09
			// START KGU#491 2018-02-09: Bugfix #507 - if branch labels change we force reshaping
			boolean mustInvalidateAlt =
					!Element.preAltT.equals(preferences.edtAltT.getText()) ||
					!Element.preAltF.equals(preferences.edtAltF.getText());
			// END KGU#491 2018-02-09
			// get fields
			Element.preAltT     = preferences.edtAltT.getText();
			Element.preAltF     = preferences.edtAltF.getText();
			Element.preAlt      = preferences.edtAlt.getText();
			Element.preCase     = preferences.txtCase.getText();
			Element.preFor      = preferences.edtFor.getText();
			Element.preWhile    = preferences.edtWhile.getText();
			Element.preRepeat   = preferences.edtRepeat.getText();
			Element.altPadRight = preferences.altPadRight.isSelected();
			// START KGU#686 2019-03-22: Enh. #56
			Element.preTry      = preferences.edtTry.getText();
			Element.preCatch    = preferences.edtCatch.getText();
			Element.preFinally  = preferences.edtFinal.getText();
			// END KGU#686 2019-03-22
			// START KGU#376 2017-07-02: Enh. #389
			String newImportCaption = preferences.edtRoot.getText();
			// END KGU#376 2017-07-02
			// START KGU#401 2017-05-18: Issue #405 - allow to reduce CASE width by branch element rotation
			int newShrinkThreshold = (Integer) preferences.spnCaseRot.getModel().getValue();
			//if (newShrinkThreshold != Element.caseShrinkByRot) {
			if (newShrinkThreshold != Element.caseShrinkByRot
					// START KGU#491 2019-02-09: Bugfix #507
					|| mustInvalidateAlt
					// END KGU#491 2019-02-09
					|| !newImportCaption.equals(Element.preImport)) {
				root.resetDrawingInfoDown();
			}
			Element.caseShrinkByRot = newShrinkThreshold;
			// END KGU#401 2017-05-18
			// START KGU#916 2021-01-25: Enh. #915
			Element.useInputBoxCase = preferences.chkCaseEditor.isSelected();
			// END KGU#916 2021-01-25
			// START KGU#376 2017-07-02: Enh. #389
			Element.preImport = preferences.edtRoot.getText();
			// END KGU#376 2017-07-02

			// save fields to ini-file
			Element.saveToINI();
			redraw();
		// START KGU#393 2017-05-09: Issue #400
		}
		// END KGU#393 2017-05-09		
	}

	/**
	 * Opens the parser preferences dialog and processes configuration changes.
	 */
	public void parserNSD() {
		ParserPreferences parserPreferences = new ParserPreferences(NSDControl.getFrame());
		Point p = getLocationOnScreen();
		parserPreferences.setLocation(Math.round(p.x + (getVisibleRect().width - parserPreferences.getWidth()) / 2 + this.getVisibleRect().x),
				Math.round(p.y + (getVisibleRect().height - parserPreferences.getHeight()) / 2 + this.getVisibleRect().y));

		// set fields
		parserPreferences.edtAltPre.setText(CodeParser.getKeyword("preAlt"));
		parserPreferences.edtAltPost.setText(CodeParser.getKeyword("postAlt"));
		parserPreferences.edtCasePre.setText(CodeParser.getKeyword("preCase"));
		parserPreferences.edtCasePost.setText(CodeParser.getKeyword("postCase"));
		parserPreferences.edtForPre.setText(CodeParser.getKeyword("preFor"));
		parserPreferences.edtForPost.setText(CodeParser.getKeyword("postFor"));
		// START KGU#3 2015-11-08: New configurable separator for FOR loop step const
		parserPreferences.edtForStep.setText(CodeParser.getKeyword("stepFor"));
		// END KGU#3 2015-11-08
		// START KGU#61 2016-03-21: New configurable keywords for FOR-IN loop
		parserPreferences.edtForInPre.setText(CodeParser.getKeyword("preForIn"));
		parserPreferences.edtForInPost.setText(CodeParser.getKeyword("postForIn"));
		// END KGU#61 2016-03-21
		parserPreferences.edtWhilePre.setText(CodeParser.getKeyword("preWhile"));
		parserPreferences.edtWhilePost.setText(CodeParser.getKeyword("postWhile"));
		parserPreferences.edtRepeatPre.setText(CodeParser.getKeyword("preRepeat"));
		parserPreferences.edtRepeatPost.setText(CodeParser.getKeyword("postRepeat"));
		// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
		parserPreferences.edtJumpLeave.setText(CodeParser.getKeyword("preLeave"));
		parserPreferences.edtJumpReturn.setText(CodeParser.getKeyword("preReturn"));
		parserPreferences.edtJumpExit.setText(CodeParser.getKeyword("preExit"));
		// END KGU#78 2016-03-25
		// START KGU#686 2019-03-18: Enh. #56 - Try / Carch / Throw mechanism implemented
		parserPreferences.edtJumpThrow.setText(CodeParser.getKeyword("preThrow"));
		// END KGU#686 2019-03-18
		parserPreferences.edtInput.setText(CodeParser.getKeyword("input"));
		parserPreferences.edtOutput.setText(CodeParser.getKeyword("output"));
		// START KGU#165 2016-03-25: We need a transparent decision here
		parserPreferences.chkIgnoreCase.setSelected(CodeParser.ignoreCase);
		// END KGU#165 2016-03-25

		parserPreferences.pack();
		parserPreferences.setVisible(true);

		if (parserPreferences.OK) {
			// START KGU#258 2016-09-26: Enh. #253 - prepare the old settings for a refactoring
			HashMap<String, StringList> oldKeywordMap = null;
			boolean wasCaseIgnored = CodeParser.ignoreCase;
			boolean considerRefactoring = root.children.getSize() > 0
					|| isArrangerOpen() && Arranger.getInstance().getAllRoots().size() > 0;
			//if (considerRefactoring)
			//{
				oldKeywordMap = new LinkedHashMap<String, StringList>();
				for (String key : CodeParser.keywordSet()) {
					String keyword = CodeParser.getKeyword(key);
					if (keyword != null && !keyword.trim().isEmpty())
					{
						// Complete strings aren't likely to be found in a key, so don't bother
						oldKeywordMap.put(key, Element.splitLexically(keyword, false));
					}
				}
			//}
			// END KGU#258 2016-09-26

			// get fields
			CodeParser.setKeyword("preAlt", parserPreferences.edtAltPre.getText());
			CodeParser.setKeyword("postAlt", parserPreferences.edtAltPost.getText());
			CodeParser.setKeyword("preCase", parserPreferences.edtCasePre.getText());
			CodeParser.setKeyword("postCase", parserPreferences.edtCasePost.getText());
			CodeParser.setKeyword("preFor", parserPreferences.edtForPre.getText());
			CodeParser.setKeyword("postFor", parserPreferences.edtForPost.getText());
			// START KGU#3 2015-11-08: New configurable separator for FOR loop step const
			CodeParser.setKeyword("stepFor", parserPreferences.edtForStep.getText());
			// END KGU#3 2015-11-08
			// START KGU#61 2016-03-21: New configurable keywords for FOR-IN loop
			CodeParser.setKeyword("preForIn", parserPreferences.edtForInPre.getText());
			CodeParser.setKeyword("postForIn", parserPreferences.edtForInPost.getText());
			// END KGU#61 2016-03-21
			CodeParser.setKeyword("preWhile", parserPreferences.edtWhilePre.getText());
			CodeParser.setKeyword("postWhile", parserPreferences.edtWhilePost.getText());
			CodeParser.setKeyword("preRepeat", parserPreferences.edtRepeatPre.getText());
			CodeParser.setKeyword("postRepeat", parserPreferences.edtRepeatPost.getText());
			// START KGU#78 2016-03-25: Enh. #23 - Jump configurability introduced
			CodeParser.setKeyword("preLeave", parserPreferences.edtJumpLeave.getText());
			CodeParser.setKeyword("preReturn", parserPreferences.edtJumpReturn.getText());
			CodeParser.setKeyword("preExit", parserPreferences.edtJumpExit.getText());
			// END KGU#78 2016-03-25
			// START KGU#686 2019-03-18: Enh. #56 - Try / Carch / Throw mechanism implemented
			CodeParser.setKeyword("preThrow", parserPreferences.edtJumpThrow.getText());
			// END KGU#686 2019-03-18
			CodeParser.setKeyword("input", parserPreferences.edtInput.getText());
			CodeParser.setKeyword("output", parserPreferences.edtOutput.getText());
			// START KGU#165 2016-03-25: We need a transparent decision here
			CodeParser.ignoreCase = parserPreferences.chkIgnoreCase.isSelected();
			// END KGU#165 2016-03-25

			// save fields to ini-file
			CodeParser.saveToINI();

			// START KGU#258 2016-09-26: Enh. #253 - now try a refactoring if specified
			boolean redrawn = false;
			if (considerRefactoring && offerRefactoring(oldKeywordMap)) {
				boolean refactorAll = oldKeywordMap.containsKey("refactorAll");
				redrawn = refactorDiagrams(oldKeywordMap, refactorAll, wasCaseIgnored);
			}
			// END KGU#258 2016-09-26

			// START KGU#362 2017-03-28: Issue #370
			offerStructPrefAdaptation(oldKeywordMap);
			// END KGU#362 2017-03-28

			// START KGU#136 2016-03-31: Bugfix #97 - cached bounds may have to be invalidated
			if (Element.E_VARHIGHLIGHT && !redrawn) {
				// Parser keyword changes may have an impact on the text width ...
				this.resetDrawingInfo();
				// START KGU#258 2016-09-26: Bugfix #253 ... and Jumps and loops
				analyse();
				// END KGU#258 2016-09-26

				// redraw diagram
				redraw();
			}
			// END KGU#136 2016-03-31

			// START KGU#705 2019-09-29: Enh. #738
			updateCodePreview();
			// END KGU#705 2019-09-29
		}
	}

	// START KGU#258 2016-09-26: Enh. #253: A set of helper methods for refactoring
	/**
	 * (To be called after a preference file has been loaded explicitly on user
	 * demand.) Based on the refactoringData collected before the loading, a
	 * difference analysis between the old and new parser preferences will be
	 * done. If changes are detected and there are non-trivial Roots then a
	 * dialog box will be popped up showing the changes and offering to refactor
	 * the current or all diagrams. If the user agrees then the respective code
	 * will be added to the refactoringData and true will be returned, otherwise
	 * false.<br/>
	 * If the user cancels then the original parser preferences will be restored
	 * and false will be returned.
	 *
	 * @param refactoringData - tokenized previous non-empty parser preferences
	 * @return true if a refactoring makes sense, false otherwise
	 */
	public boolean offerRefactoring(HashMap<String, StringList> refactoringData) {
		// Since this method is always called after a preference file has been loaded,
		// we update the preferred export code for the doButtons() call, though it
		// has nothing to do with refactoring
		this.prefGeneratorName = Ini.getInstance().getProperty("genExportPreferred", this.prefGeneratorName);

		// No refectoring data was collected then we are done here ...
		if (refactoringData == null) {
			return false;
		}

		// Otherwise we look for differences between old and new parser preferences
		// START KGU#719 2019-08-01: New layout for the refactoring dialog
		//StringList replacements = new StringList();
		List<String[]> replacements = new LinkedList<String[]>();
		// END KGU#719 2019-08-1
		for (HashMap.Entry<String, StringList> entry : refactoringData.entrySet()) {
			String oldValue = entry.getValue().concatenate();
			// START KGU#288 2016-11-06: Issue #279 - Method getOrDefault() missing in OpenJDK
			//String newValue = CodeParser.getKeywordOrDefault(entry.getKey(), "");
			String newValue = CodeParser.getKeywordOrDefault(entry.getKey(), "");
			// END KGU#288 2016-11-06
			if (!oldValue.equals(newValue)) {
				// START KGU#719 2019-08-01: New layout for the refactoring dialog
				//replacements.add("   " + entry.getKey() + ": \"" + oldValue + "\" -> \"" + newValue + "\"");
				replacements.add(new String[]{entry.getKey(), "\"" + oldValue + "\"", "\"" + newValue + "\""});
				// END KGU#719 2019-08-01
			}
		}
		// Only offer the question if there are relevant replacements and at least one non-empty or parked Root
		// START KGU#719 2019-08-01
		if (!replacements.isEmpty() && (root.children.getSize() > 0
			|| isArrangerOpen() && !Arranger.getInstance().getAllRoots().isEmpty()))
		// END KGU#719 2019-08-01
		{
			String[] options = {
					Menu.lblRefactorNone.getText(),
					Menu.lblRefactorCurrent.getText(),
					Menu.lblRefactorAll.getText()
			};
			// START KGU#719 2019-08-01: New layout
			JTable replTable = new JTable(0, 3);
			for (String[] tupel : replacements) {
				((DefaultTableModel) replTable.getModel()).addRow(tupel);
			}
			for (int col = 0; col < Math.min(replTable.getColumnCount(), Menu.hdrRefactoringTable.length); col++) {
				replTable.getColumnModel().getColumn(col).setHeaderValue(Menu.hdrRefactoringTable[col].getText());
			}
			Box box = Box.createVerticalBox();
			Box box1 = Box.createHorizontalBox();
			Box box2 = Box.createHorizontalBox();
			box1.add(new JLabel(Menu.msgRefactoringOffer1.getText()));
			box1.add(Box.createHorizontalGlue());
			box2.add(new JLabel(Menu.msgRefactoringOffer2.getText()));
			box2.add(Box.createHorizontalGlue());
			box.add(box1);
			box.add(Box.createVerticalStrut(5));
			box.add(replTable.getTableHeader());
			box.add(replTable);
			box.add(Box.createVerticalStrut(10));
			box.add(box2);
			replTable.setEnabled(false);
			replTable.setRowHeight((int) (replTable.getRowHeight() * Double.valueOf(Ini.getInstance().getProperty("scaleFactor", "1"))));
			// END KGU#719 2019-08-01
			// START KGU#362 2017-03-28: Issue #370: Restore old settings if user backed off
			//int answer = JOptionPane.showOptionDialog(this,
			//		Menu.msgRefactoringOffer.getText().replace("%", "\n" + replacements.getText() + "\n"),
			//		Menu.msgTitleQuestion.getText(), JOptionPane.OK_CANCEL_OPTION,
			//		JOptionPane.QUESTION_MESSAGE,
			//		null,
			//		options, options[0]);
			//if (answer != 0 && answer != JOptionPane.CLOSED_OPTION)
			int answer = JOptionPane.CLOSED_OPTION;
			do {
				answer = JOptionPane.showOptionDialog(this.getFrame(),
						// START KGU#719 2019-08-01
						//Menu.msgRefactoringOffer.getText().replace("%", "\n" + replacements.getText() + "\n"),
						box,
						// END KGU#719 2019-08-01
						Menu.msgTitleQuestion.getText(), JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options, options[2]);
				if (answer == JOptionPane.CLOSED_OPTION && JOptionPane.showConfirmDialog(this.getFrame(),
						Menu.msgDiscardParserPrefs.getText()) == JOptionPane.OK_OPTION) {
					// Revert the changes
					for (Map.Entry<String, StringList> refEntry : refactoringData.entrySet()) {
						CodeParser.setKeyword(refEntry.getKey(), refEntry.getValue().concatenate());
					}
					answer = 2;
				}
			} while (answer == JOptionPane.CLOSED_OPTION);
			if (answer != 0)
			// END KGU#362 2017-03-28
			{
				if (CodeParser.ignoreCase) {
					refactoringData.put("ignoreCase", StringList.getNew("true"));
				}
				if (answer == 2) {
					refactoringData.put("refactorAll", StringList.getNew("true"));
				}
				return true;
			}
		}
		return false;
	}

	// START KGU#362 2017-03-28: Issue #370 - helper methods for preference consistency 
	private void offerStructPrefAdaptation(HashMap<String, StringList> refactoringData) {
		// START KGU#735 2019-09-29: Issue #753 - first do a check to avoid puzzling questions
		//if (JOptionPane.showConfirmDialog(this.NSDControl.getFrame(),
		//		Menu.msgAdaptStructPrefs.getText(), Menu.msgTitleQuestion.getText(),
		//		JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
		String updateNeed = null;
		if (((updateNeed = checkPref(Element.preAlt, refactoringData, "preAlt", "postAlt")) != null
				|| (updateNeed = checkPref(Element.preWhile, refactoringData, "preWhile", "postWhile")) != null
				|| (updateNeed = checkPref(Element.preRepeat, refactoringData, "preRepeat", "postRepeat")) != null
				|| (updateNeed = checkPrefCase(Element.preCase, refactoringData)) != null
				|| (updateNeed = checkPrefFor(Element.preFor, refactoringData)) != null)
				&& JOptionPane.showConfirmDialog(this.getFrame(),
						Menu.msgAdaptStructPrefs.getText().replace("%", updateNeed), Menu.msgTitleQuestion.getText(),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
		// END KGU#735 2019-09-29
			Element.preAlt = replacePref(Element.preAlt,
					refactoringData, "preAlt", "postAlt");
			Element.preWhile = replacePref(Element.preWhile,
					refactoringData, "preWhile", "postWhile");
			Element.preRepeat = replacePref(Element.preRepeat,
					refactoringData, "preRepeat", "postRepeat");
			Element.preCase = replacePrefCase(Element.preCase,
					refactoringData);
			Element.preFor = replacePrefFor(Element.preFor,
					refactoringData);
		}
	}

	private String replacePref(String structPref, HashMap<String, StringList> refactoringData,
			String prefixKey, String postfixKey) {
		StringList old = refactoringData.get(prefixKey);
		int startPos = 0;
		if (old != null) {
			String oldPrefix = old.concatenate();
			String newPrefix = CodeParser.getKeywordOrDefault(prefixKey, "");
			if (!oldPrefix.trim().isEmpty() && structPref.startsWith(oldPrefix)) {
				structPref = newPrefix + structPref.substring(oldPrefix.length());
				startPos = newPrefix.length();
			}
		}
		old = refactoringData.get(postfixKey);
		if (old != null) {
			String oldPostfix = old.concatenate();
			String newPostfix = CodeParser.getKeywordOrDefault(postfixKey, "");
			if (!oldPostfix.trim().isEmpty() && structPref.substring(startPos).endsWith(oldPostfix)) {
				structPref = structPref.substring(0, structPref.length() - oldPostfix.length()) + newPostfix;
			}
		}
		return structPref;
	}

	private String replacePrefCase(String preCase, HashMap<String, StringList> refactoringData) {
		StringList structPrefLines = StringList.explode(preCase, "\n");
		String oldPrefix = "";
		String oldPostfix = "";
		String newPrefix = CodeParser.getKeywordOrDefault("preCase", "");
		String newPostfix = CodeParser.getKeywordOrDefault("postCase", "");
		StringList old = refactoringData.get("preCase");
		if (old != null) {
			oldPrefix = old.concatenate();
		}
		old = refactoringData.get("postCase");
		if (old != null) {
			oldPostfix = old.concatenate();
		}
		for (int i = 0; i < structPrefLines.count() - 1; i++) {
			String structPref = structPrefLines.get(i);
			if (!oldPrefix.trim().isEmpty() && structPref.startsWith(oldPrefix)) {
				structPref = newPrefix + structPref.substring(oldPrefix.length());
			}
			if (!oldPostfix.trim().isEmpty() && structPref.endsWith(oldPostfix)) {
				structPref = structPref.trim().substring(0, structPref.length() - oldPostfix.length()) + newPostfix;
			}
			structPrefLines.set(i, structPref);
		}
		return structPrefLines.getText();
	}

	private String replacePrefFor(String structPref, HashMap<String, StringList> refactoringData) {
		String oldPrefix1 = "";
		String oldPrefix2 = "";
		String oldInfix1 = "";
		String oldInfix1a = "";
		String oldInfix2 = "";
		String newPrefix1 = CodeParser.getKeywordOrDefault("preFor", "");
		String newPrefix2 = CodeParser.getKeywordOrDefault("preForIn", "");
		String newInfix1 = CodeParser.getKeywordOrDefault("postFor", "");
		String newInfix1a = CodeParser.getKeywordOrDefault("stepFor", "");
		String newInfix2 = CodeParser.getKeywordOrDefault("postForIn", "");
		StringList old = null;
		if ((old = refactoringData.get("preFor")) != null) {
			oldPrefix1 = old.concatenate();
		}
		if ((old = refactoringData.get("preForIn")) != null) {
			oldPrefix2 = old.concatenate();
		}
		if ((old = refactoringData.get("postFor")) != null) {
			oldInfix1 = old.concatenate();
		}
		if ((old = refactoringData.get("stepFor")) != null) {
			oldInfix1a = old.concatenate();
		}
		if ((old = refactoringData.get("postForIn")) != null) {
			oldInfix2 = old.concatenate();
		}
		String tail = "";
		if (!oldPrefix1.trim().isEmpty() && !oldInfix1.trim().isEmpty()
				&& structPref.startsWith(oldPrefix1)
				&& (tail = structPref.substring(oldPrefix1.length())).contains(oldInfix1)) {
			if (tail.matches(".*?\\W+" + oldInfix1 + "\\W+.*?")) {
				tail = tail.replaceFirst("(.*?\\W+)" + oldInfix1 + "(\\W+.*?)",
						"$1" + Matcher.quoteReplacement(newInfix1) + "$2");
			}
			if (tail.matches(".*?\\W+" + oldInfix1a + "\\W+.*?")) {
				tail = tail.replaceFirst("(.*?\\W+)" + oldInfix1a + "(\\W+.*?)",
						"$1" + Matcher.quoteReplacement(newInfix1a) + "$2");
			}
			structPref = newPrefix1 + tail;
		} else if (!oldPrefix2.trim().isEmpty() && !oldInfix2.trim().isEmpty()
				&& structPref.startsWith(oldPrefix2)
				&& (tail = structPref.substring(oldPrefix2.length())).contains(oldInfix2)) {
			if (tail.matches(".*?\\W+" + oldInfix2 + "\\W+.*?")) {
				tail = tail.replaceFirst("(.*?\\W+)" + oldInfix2 + "(\\W+.*?)",
						"$1" + Matcher.quoteReplacement(newInfix2) + "$2");
			}
			structPref = newPrefix2 + tail;
		}
		return structPref;
	}
	// END KGU#362 2017-03-28

	// START KGU#735 2019-09-29: Issue #753 - check methods for preference consistency 
	private String checkPref(String structPref, HashMap<String, StringList> refactoringData,
			String prefixKey, String postfixKey) {
		String newPref = replacePref(structPref, refactoringData, prefixKey, postfixKey);
		if (!newPref.equals(structPref)) {
			return structPref + " --> " + newPref;
		}
		return null;
	}

	private String checkPrefCase(String structPref, HashMap<String, StringList> refactoringData) {
		String newPref = replacePrefCase(structPref, refactoringData);
		if (!newPref.equals(structPref)) {
			return structPref + " --> " + newPref;
		}
		return null;
	}

	private String checkPrefFor(String structPref, HashMap<String, StringList> refactoringData) {
		String newPref = replacePrefFor(structPref, refactoringData);
		if (!newPref.equals(structPref)) {
			return structPref + " --> " + newPref;
		}
		return null;
	}
	// END KGU#735 2019-09-29

	/**
	 * Replaces used parser keywords in the specified diagrams by the keywords
	 * associated to them in the keyword map {@code refactoringData}, which also
	 * contains the specification whether all open diagrams are to be refactored
	 * in this way or just {@link #root}.
	 *
	 * @param refactoringData - maps old keywords to new keywords and may
	 * contain keys "refactorAll" and "ignoreCase" as mere flags.
	 */
	public void refactorNSD(HashMap<String, StringList> refactoringData) {
		if (refactoringData != null) {
			refactorDiagrams(refactoringData,
					refactoringData.containsKey("refactorAll"),
					refactoringData.containsKey("ignoreCase")
					);
		}
	}

	private boolean refactorDiagrams(HashMap<String, StringList> oldKeywordMap, boolean refactorAll, boolean wasCaseIgnored) {
		boolean redrawn = false;
		if (oldKeywordMap != null && !oldKeywordMap.isEmpty()) {
			final class Refactorer implements IElementVisitor {

				public HashMap<String, StringList> oldMap = null;
				boolean ignoreCase = false;

				@Override
				public boolean visitPreOrder(Element _ele) {
					_ele.refactorKeywords(oldMap, ignoreCase);
					return true;
				}

				@Override
				public boolean visitPostOrder(Element _ele) {
					return true;
				}

				Refactorer(HashMap<String, StringList> _keyMap, boolean _caseIndifferent) {
					oldMap = _keyMap;
					ignoreCase = _caseIndifferent;
				}
			};
			// START KGU#362 2017-03-28: Issue #370 avoid frozen diagrams
			//root.addUndo();
			//root.traverse(new Refactorer(oldKeywordMap, wasCaseIgnored));
			if (root.storedParserPrefs == null) {
				root.addUndo();
				root.traverse(new Refactorer(oldKeywordMap, wasCaseIgnored));
			}
			// END KGU#362 2017-03-28
			if (refactorAll && isArrangerOpen()) {
				// Well, we hope that the roots won't change the hash code on refactoring...
				for (Root aRoot : Arranger.getInstance().getAllRoots()) {
					// START KGU#362 2017-03-28: Issue #370 avoid frozen diagrams
					//if (root != aRoot) {
					if (root != aRoot && aRoot.storedParserPrefs == null) {
					// END KGU#362 2017-03-28
						aRoot.addUndo();
						aRoot.traverse(new Refactorer(oldKeywordMap, wasCaseIgnored));
					}
				}
			}

			// Parser keyword changes may have an impact on the text width ...
			this.resetDrawingInfo();

			// START KGU#258 2016-09-26: Bugfix #253 ... and Jumps and loops
			analyse();
			// END KGU#258 2016-09-26

			doButtons();

			// redraw diagram
			redraw();

			redrawn = true;
		}
		return redrawn;
	}
	// END KGU#258 2016-09-26

	/**
	 * Opens the Arranger Preferences dialog and processes configuration changes
	 */
	public void analyserNSD() {
		// START KGU#1012 2021-11-14: Enh. #967
		//AnalyserPreferences analyserPreferences = new AnalyserPreferences(NSDControl.getFrame());
		StringList pluginCheckKeys = new StringList();
		StringList pluginCheckTitles = new StringList();
		for (GENPlugin plugin: Menu.generatorPlugins) {
			if (plugin.syntaxChecks != null) {
				for (int i = 0; i < plugin.syntaxChecks.size(); i++) {
					GENPlugin.SyntaxCheck spec = plugin.syntaxChecks.get(i);
					String key = spec.className + ":" + spec.source.name();
					String title = spec.title;
					if (title == null || title.isEmpty()) {
						title = "Syntax check for " + plugin.getKey();
					}
					pluginCheckKeys.add(key);
					pluginCheckTitles.add(title);
				}
			}
		}
		AnalyserPreferences analyserPreferences = new AnalyserPreferences(
				NSDControl.getFrame(), 
				pluginCheckTitles.toArray());
		// END KGU#1012 2021-11-14
		Point p = getLocationOnScreen();
		analyserPreferences.setLocation(Math.round(p.x + (getVisibleRect().width - analyserPreferences.getWidth()) / 2 + this.getVisibleRect().x),
				Math.round(p.y + (getVisibleRect().height - analyserPreferences.getHeight()) / 2 + this.getVisibleRect().y));

		// set fields
		// START KGU#239 2016-08-12: Code redesign (2016-09-22: index mapping modified)
		for (int i = 1; i < analyserPreferences.checkboxes.length; i++) {
			analyserPreferences.checkboxes[i].setSelected(Root.check(i));
		}
		// END KGU#239 2016-08-12
		// START KGU#1012 2021-11-14: Issue #967
		for (int i = 0; i < pluginCheckKeys.count(); i++) {
			analyserPreferences.pluginCheckboxes[i].setSelected(
					Root.check(pluginCheckKeys.get(i)) == true);
		}
		// END KGU#1012 2021-11-14
		// START KGU#906 2021-01-02: Enh. #905
		analyserPreferences.chkDrawWarningSign.setSelected(Element.E_ANALYSER_MARKER);
		// END KGU#906 2021-01-02
		// START KGU#459 2017-11-15: Enh. #459-1
		boolean hadActiveTutorials = false;
		for (int code : AnalyserPreferences.getOrderedGuideCodes()) {
			if (hadActiveTutorials = Root.check(code)) {
				break;
			}
		}
		// END KGU#459 2017-11-15

		analyserPreferences.pack();
		analyserPreferences.setVisible(true);

		// get fields
		// START KGU#393 2017-05-09: Issue #400 - check whether changes were actually committed
		if (analyserPreferences.OK) {
		// END KGU#393 2017-05-09
			// START KGU#239 2016-08-12: Code redesign (2016-09-22: index mapping modified)
			for (int i = 1; i < analyserPreferences.checkboxes.length; i++) {
				Root.setCheck(i, analyserPreferences.checkboxes[i].isSelected());
			}
			// END KGU#239 2016-08-12
			// START KGU#1012 2021-11-14: Issue #967
			for (int i = 0; i < pluginCheckKeys.count(); i++) {
				Root.setCheck(
						pluginCheckKeys.get(i),
						analyserPreferences.pluginCheckboxes[i].isSelected());
			}
			// END KGU#1012 2021-11-14
			// START KGU#906 2021-01-02: Enh. #905
			boolean markersWereOn = Element.E_ANALYSER_MARKER;
			Element.E_ANALYSER_MARKER = analyserPreferences.chkDrawWarningSign.isSelected();
			// END KGU#906 2021-01-02

			// save fields to ini-file
			Root.saveToINI();

			// START KGU#456/KGU#459 2017-11-15: Enh. #452, #459-1
			updateTutorialQueues();
			if (!hadActiveTutorials) {
				for (int code : AnalyserPreferences.getOrderedGuideCodes()) {
					if (Root.check(code)) {
						showTutorialHint();
						break;
					}
				}
			}
			// END KGU#456 2017-11-15
			// re-analyse
			//root.getVarNames();	// Is done by root.analyse() itself
			analyse();
			// START KGU#906 2021-01-02: Enh. #905
			// START KGU#906 2021-02-28: Issue #905 We have to redraw on test set modifications, too
			//if (markersWereOn != Element.E_ANALYSER_MARKER) {
			if (markersWereOn || (markersWereOn != Element.E_ANALYSER_MARKER)) {
			// END KGU#906 2021-02-28
				redraw();
			}
			// END KGU#906 2021-01-02
		// START KGU#393 2017-05-09: Issue #400
		}
		// END KGU#393 2017-05-09		
	}

	// START KGU#456 2017-11-15: Enh. #452
	protected void updateTutorialQueues() {
		int[] guideCodes = AnalyserPreferences.getOrderedGuideCodes();
		root.updateTutorialQueue(guideCodes);
		if (Arranger.hasInstance()) {
			for (Root aRoot : Arranger.getInstance().getAllRoots()) {
				aRoot.updateTutorialQueue(guideCodes);
			}
		}
	}
	// END KGU#456 2017-11-15

	/**
	 * Opens the export options dialog and processes configuration changes.
	 */
	public void exportOptions()
	{
		try {
			Ini ini = Ini.getInstance();
			ini.load();
			ExportOptionDialoge eod = new ExportOptionDialoge(NSDControl.getFrame(), Menu.generatorPlugins);
			if (ini.getProperty("genExportComments", "false").equals("true")) {
				eod.commentsCheckBox.setSelected(true);
			} else {
				eod.commentsCheckBox.setSelected(false);
			}
			// START KGU#16/KGU#113 2015-12-18: Enh. #66, #67
			eod.bracesCheckBox.setSelected(ini.getProperty("genExportBraces", "false").equals("true"));
			// START KGU#113 2021-06-07: Issue #67 now plugin-specific
			//eod.lineNumbersCheckBox.setSelected(ini.getProperty("genExportLineNumbers", "false").equals("true"));
			// END KGU#113 2021-06-07
			// END KGU#16/KGU#113 2015-12-18
			// START KGU#178 2016-07-20: Enh. #160
			eod.chkExportSubroutines.setSelected(ini.getProperty("genExportSubroutines", "false").equals("true"));
			// END #178 2016-07-20
			// START KGU#162 2016-03-31: Enh. #144
			eod.noConversionCheckBox.setSelected(ini.getProperty("genExportnoConversion", "false").equals("true"));
			// END KGU#162 2016-03-31
			// START KGU#363/KGU#395 2017-05-11: Enh. #372, #357
			eod.chkExportLicenseInfo.setSelected(ini.getProperty("genExportLicenseInfo", "false").equals("true"));
			// END KGU#363/KGU#395 2017-05-11
			// START KGU#816 2020-03-17: Enh. #837
			eod.chkDirectoryFromNsd.setSelected(ini.getProperty("genExportDirFromNsd", "true").equals("true"));
			// END KGU#816 2020-03-17
			// START KGU#854 2020-04-22: Enh. #855
			eod.chkArraySize.setSelected(ini.getProperty("genExportUseArraySize", "false").equals("true"));
			eod.chkStringLen.setSelected(ini.getProperty("genExportUseStringLen", "false").equals("true"));
			eod.spnArraySize.setValue(Integer.parseUnsignedInt(ini.getProperty("genExportArraySizeDefault", "100")));
			eod.spnStringLen.setValue(Integer.parseUnsignedInt(ini.getProperty("genExportStringLenDefault", "256")));
			// END KGU#854 2020-04-22
			// START KGU#170 2016-04-01: Enh. #144 Favourite export generator
			eod.cbPrefGenerator.setSelectedItem(ini.getProperty("genExportPreferred", "Java"));
			// END KGU#170 2016-04-01
			// START KGU#654 2019-02-15: Enh #681 Trigger for proposing recent generator as new favourite
			eod.spnPrefGenTrigger.setValue(generatorProposalTrigger);
			// END KGU#654 2019-02-15
			// START KGU#168 2016-04-04: Issue #149 Charsets for export
			eod.charsetListChanged(ini.getProperty("genExportCharset", Charset.defaultCharset().name()));
			// END KGU#168 2016-04-04 
			// START KGU#975 2021-06-07: Enh. #953 Restore the last selected plugin choice
			eod.cbOptionPlugins.setSelectedItem(ini.getProperty("expPluginChoice", ""));
			// END KGU#975 2021-06-07
			// START KGU#351 2017-02-26: Enh. #346 / KGU#416 2017-06-20 Revised
			for (int i = 0; i < Menu.generatorPlugins.size(); i++) {
				GENPlugin plugin = Menu.generatorPlugins.get(i);
				String propertyName = "genExportIncl" + plugin.getKey();
				eod.includeLists[i].setText(ini.getProperty(propertyName, ""));
				// START KGU#416 2017-06-20: Enh. #354,#357
				HashMap<String, String> optionValues = new HashMap<String, String>();
				for (HashMap<String, String> optionSpec : plugin.options) {
					String optKey = optionSpec.get("name");
					propertyName = plugin.getKey() + "." + optKey;
					// START KGU#1000 2021-10-29: Issue #1004 - support plugin-defined default values
					//optionValues.put(optKey, ini.getProperty(propertyName, ""));
					String dflt = "";
					if (optionSpec.containsKey("default")) {
						dflt = optionSpec.get("default");
					}
					optionValues.put(optKey, ini.getProperty(propertyName, dflt));
					// END KGU#1000 2021-10-29
				}
				eod.generatorOptions.add(optionValues);
				// END KGU#416 2017-06-20
			}
			// END KGU#351 2017-02-26

			eod.setVisible(true);

			if (eod.goOn == true) {
				ini.setProperty("genExportComments", String.valueOf(eod.commentsCheckBox.isSelected()));
				// START KGU#16/KGU#113 2015-12-18: Enh. #66, #67
				ini.setProperty("genExportBraces", String.valueOf(eod.bracesCheckBox.isSelected()));
				// START KGU#113 2021-06-07: Issue #67 now plugin-specific
				//ini.setProperty("genExportLineNumbers", String.valueOf(eod.lineNumbersCheckBox.isSelected()));
				// END KGU#113 2021-06-07
				// END KGU#16/KGU#113 2015-12-18
				// START KGU#178 2016-07-20: Enh. #160
				ini.setProperty("genExportSubroutines", String.valueOf(eod.chkExportSubroutines.isSelected()));
				// END #178 2016-07-20                
				// START KGU#162 2016-03-31: Enh. #144
				ini.setProperty("genExportnoConversion", String.valueOf(eod.noConversionCheckBox.isSelected()));
				// END KGU#162 2016-03-31
				// START KGU#363/KGU#395 2017-05-11: Enh. #372, #357
				ini.setProperty("genExportLicenseInfo", String.valueOf(eod.chkExportLicenseInfo.isSelected()));
				// END KGU#363/KGU#395 2017-05-11
				// START KGU#816 2020-03-17: Enh. #837
				ini.setProperty("genExportDirFromNsd", String.valueOf(eod.chkDirectoryFromNsd.isSelected()));
				// END KGU#816 2020-03-17
				// START KGU#854 2020-04-22: Enh. #855
				ini.setProperty("genExportUseArraySize", String.valueOf(eod.chkArraySize.isSelected()));
				ini.setProperty("genExportUseStringLen", String.valueOf(eod.chkStringLen.isSelected()));
				ini.setProperty("genExportArraySizeDefault", String.valueOf(eod.spnArraySize.getValue()));
				ini.setProperty("genExportStringLenDefault", String.valueOf(eod.spnStringLen.getValue()));
				// END KGU#854 2020-04-22
				// START KGU#170 2016-04-01: Enh. #144 Favourite export generator
				String prefGenName = (String) eod.cbPrefGenerator.getSelectedItem();
				// START KGU#654 2019-02-15: Enh #681 Trigger for proposing recent generator as new favourite
				if (!prefGenName.equals(this.prefGeneratorName)) {
					// If the preferred generator was changed then start new use counting 
					this.generatorUseCount = 0;
				}
				// END KGU#654 2019-02-15
				this.prefGeneratorName = prefGenName;
				ini.setProperty("genExportPreferred", this.prefGeneratorName);
				this.NSDControl.doButtons();
				// END KGU#170 2016-04-01
				// START KGU#654 2019-02-15: Enh #681 Trigger for proposing recent generator as new favourite
				int generatorUseTrigger = (int) eod.spnPrefGenTrigger.getValue();
				if (generatorUseTrigger != this.generatorProposalTrigger) {
					this.generatorUseCount = 0;
				}
				this.generatorProposalTrigger = generatorUseTrigger;
				ini.setProperty("genExportPrefTrigger", this.prefGeneratorName);
				// END KGU#654 2019-02-15
				// START KGU#168 2016-04-04: Issue #149 Charset for export
				ini.setProperty("genExportCharset", (String) eod.cbCharset.getSelectedItem());
				// END KGU#168 2016-04-04
				// START KGU#351 2017-02-26: Enh. #346 / KGU#416 2017-06-20 Revised
				for (int i = 0; i < Menu.generatorPlugins.size(); i++) {
					GENPlugin plugin = Menu.generatorPlugins.get(i);
					String propertyName = "genExportIncl" + plugin.getKey();
					ini.setProperty(propertyName, eod.includeLists[i].getText().trim());
					// START KGU#416 2017-06-20: Enh. #354,#357
					for (Map.Entry<String, String> entry : eod.generatorOptions.get(i).entrySet()) {
						propertyName = plugin.getKey() + "." + entry.getKey();
						ini.setProperty(propertyName, entry.getValue());
					}
					// END KGU#416 2017-06-20
				}
				// END KGU#351 2017-02-26
				// START KGU#975 2021-06-07: Enh. #953 Restore the last selected plugin choice
				ini.setProperty("expPluginChoice", (String)eod.cbOptionPlugins.getSelectedItem());
				// END KGU#975 2021-06-07
				ini.save();
				// START KGU#705 2019-09-23: Enh. #738
				this.updateCodePreview();
				// END KGU#705 2019-09-23
			}
		} catch (IOException ex) {
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			logger.log(Level.WARNING, "Trouble saving preferences.", ex);
			// END KGU#484 2018-04-05
		}
	}

	// START KGU#705 2019-09-24: Enh. #738
	/**
	 * Sets a tooltip to the codePreview tab showing the language name plus (if
	 * retrievable) the tooltip of Menu.menuFileExportCodeFavorite.
	 */
	protected void setCodePreviewTooltip() {
		Component comp = codePreview;
		while (comp != null && !(comp instanceof JTabbedPane)) {
			comp = comp.getParent();
		}
		if (comp instanceof JTabbedPane) {
			//prefGenName + Menu.
			String tt = Locales.getValue("Structorizer", "Menu.menuFileExportCodeFavorite.tooltip", true);
			((JTabbedPane) comp).setToolTipTextAt(1, prefGeneratorName + " - " + tt);
		}
	}
	// END KGU#705 2019-09-24
	
	// START AS 2021-03-25: Enh. #967 (mode for ARM code export) - KGU 2021-04-15 replaced by export option
	///**
	// * Sets the Element.ARM_VISUAL based on the menu choice 
	// * 
	// * @param _arm - true to switch in arm GNU mode (false for KEIL syntax compiler)
	// */  
	//public void setOperationArmVisual(boolean _arm) {
	//	Element.ARM_GNU = _arm;
	//	this.resetDrawingInfo();
	//	NSDControl.doButtons();
	//	updateCodePreview();
	//	redraw();
	//}
	//END AS 2021-03-25
	
	// START KGU#258 2016-09-26: Enh. #253
	/**
	 * Opens the import options dialog and processes configuration changes.
	 */
	public void importOptions() {
		try {
			Ini ini = Ini.getInstance();
			ini.load();
			// START KGU#416 2017-06-20: Enh. #354,#357
			//ImportOptionDialog iod = new ImportOptionDialog(NSDControl.getFrame());
			this.retrieveParsers();
			ImportOptionDialog iod = new ImportOptionDialog(NSDControl.getFrame(), parserPlugins);
			// END KGU#416 2017-06-20
			// START KGU#362 2017-03-28: Issue #370 - default turned to true
			//iod.chkRefactorOnLoading.setSelected(ini.getProperty("impRefactorOnLoading", "false").equals("true"));
			iod.chkRefactorOnLoading.setSelected(!ini.getProperty("impRefactorOnLoading", "true").equals("false"));
			// END KGU#362 2017-03-28
			iod.charsetListChanged(ini.getProperty("impImportCharset", Charset.defaultCharset().name()));
			// START KGU#358 2017-03-06: Enh. #368
			iod.chkVarDeclarations.setSelected(ini.getProperty("impVarDeclarations", "false").equals("true"));
			// END KGU#358 2017-03-06
			// START KGU#407 2017-06-22: Enh. #420
			iod.chkCommentImport.setSelected(ini.getProperty("impComments", "false").equals("true"));
			// END KGU#407 2017-06-22
			// START KGU#821 2020-03-09: Issue #833
			iod.chkInsertOptKeywords.setSelected(ini.getProperty("impOptKeywords", "false").equals("true"));
			// END KGU#821 2020-03-09
			// START KGU#354 2017-03-08: Enh. #354 - new option to save the parse tree
			iod.chkSaveParseTree.setSelected(ini.getProperty("impSaveParseTree", "false").equals("true"));
			// END KGU#354 2017-03-08
			// START KGU#553 2018-07-13: Issue #557 - KGU#701 2019-03-29: Issue #718 raised from 20 to 50
			iod.spnLimit.setValue(Integer.parseUnsignedInt(ini.getProperty("impMaxRootsForDisplay", "50")));
			// END KGU#354 2018-07-13
			// START KGU#602 2018-10-25: Issue #419
			iod.spnMaxLen.setValue(Integer.parseUnsignedInt(ini.getProperty("impMaxLineLength", "0")));
			// END KGU#602 2018-10-25
			// START KGU#354 2017-04-27: Enh. #354 - new option to log to a specified directory
			iod.chkLogDir.setSelected(ini.getProperty("impLogToDir", "false").equals("true"));
			iod.txtLogDir.setText(ini.getProperty("impLogDir", ""));
			// START KGU#416 2017-06-20: Enh. #354,#357
			if (parserPlugins != null) {
				// START KGU#548 2018-07-09: Restore the last selected plugin choice
				iod.cbOptionPlugins.setSelectedItem(ini.getProperty("impPluginChoice", ""));
				// END KGU#548 2018-07-09
				for (int i = 0; i < parserPlugins.size(); i++) {
					GENPlugin plugin = parserPlugins.get(i);
					HashMap<String, String> optionValues = new HashMap<String, String>();
					for (HashMap<String, String> optionSpec : plugin.options) {
						String optKey = optionSpec.get("name");
						String propertyName = plugin.getKey() + "." + optKey;
						// START KGU#1000 2021-10-29: Issue #1004 - support plugin-defined default values
						//optionValues.put(optKey, ini.getProperty(propertyName, ""));
						String dflt = "";
						if (optionSpec.containsKey("default")) {
							dflt = optionSpec.get("default");
						}
						optionValues.put(optKey, ini.getProperty(propertyName, dflt));
						// END KGU#1000 2021-10-29
					}
					iod.parserOptions.add(optionValues);
				}
			}
			// END KGU#416 2017-06-20
			iod.doLogButtons();
			// END KGU#354 2017-04-27

			iod.setVisible(true);

			if (iod.goOn == true) {
				ini.setProperty("impRefactorOnLoading", String.valueOf(iod.chkRefactorOnLoading.isSelected()));
				ini.setProperty("impImportCharset", (String) iod.cbCharset.getSelectedItem());
				// START KGU#358 2017-03-06: Enh. #368
				ini.setProperty("impVarDeclarations", String.valueOf(iod.chkVarDeclarations.isSelected()));
				// END KGU#358 2017-03-06
				// START KGU#407 2017-06-22: Enh. #420
				ini.setProperty("impComments", String.valueOf(iod.chkCommentImport.isSelected()));
				// END KGU#407 2017-06-22
				// START KGU#821 2020-03-09: Issue #833
				ini.setProperty("impOptKeywords", String.valueOf(iod.chkInsertOptKeywords.isSelected()));
				// END KGU#821 2020-03-09
				// START KGU#354 2017-03-08: Enh. #354 - new option to save the parse tree
				ini.setProperty("impSaveParseTree", String.valueOf(iod.chkSaveParseTree.isSelected()));
				// END KGU#354 2017-03-08
				// START KGU#354 2017-04-27: Enh. #354 - new option to log to a specified directory
				ini.setProperty("impLogToDir", String.valueOf(iod.chkLogDir.isSelected()));
				ini.setProperty("impLogDir", iod.txtLogDir.getText());
				// END KGU#354 2017-04-27
				// START KGU#553 2018-07-13: Issue #557
				ini.setProperty("impMaxRootsForDisplay", String.valueOf(iod.spnLimit.getValue()));
				// END KGU#553 2018-07-13
				// START KGU#602 2018-10-25: Issue #419
				ini.setProperty("impMaxLineLength", String.valueOf(iod.spnMaxLen.getValue()));
				// END KGU#602 2018-10-25
				// START KGU#416 2017-02-26: Enh. #354, #357
				for (int i = 0; i < parserPlugins.size(); i++) {
					GENPlugin plugin = parserPlugins.get(i);
					for (Map.Entry<String, String> entry : iod.parserOptions.get(i).entrySet()) {
						String propertyName = plugin.getKey() + "." + entry.getKey();
						ini.setProperty(propertyName, entry.getValue());
					}
				}
				// END KGU#416 2017-06-20
				// START KGU#548 2018-07-09: Restore the last selected plugin choice
				ini.setProperty("impPluginChoice", (String) iod.cbOptionPlugins.getSelectedItem());
				// END KGU#548 2018-07-09
				ini.save();
			}
		} catch (FileNotFoundException ex) {
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			logger.log(Level.WARNING, "Trouble accessing import preferences.", ex);
			// END KGU#484 2018-04-05
		} catch (IOException ex) {
			// START KGU#484 2018-04-05: Issue #463
			//ex.printStackTrace();
			logger.log(Level.WARNING, "Trouble accessing import preferences.", ex);
			// END KGU#484 2018-04-05
		}
	}
	// END KGU#258 2016-09-26

	// START KGU#309 2016-12-15: Enh. #310
	/**
	 * Opens the file saveing options dialog and processes configuration
	 * changes.
	 */
	public void savingOptions() {
		try {
			SaveOptionDialog sod = new SaveOptionDialog(NSDControl.getFrame());
			Ini ini = Ini.getInstance();
			sod.chkAutoSaveClose.setSelected(Element.E_AUTO_SAVE_ON_CLOSE);
			sod.chkAutoSaveExecute.setSelected(Element.E_AUTO_SAVE_ON_EXECUTE);
			sod.chkBackupFile.setSelected(Element.E_MAKE_BACKUPS);
			// START KGU#363 2017-03-12: Enh. #372 Allow user-defined author string
			sod.txtAuthorName.setText(ini.getProperty("authorName", System.getProperty("user.name")));
			sod.cbLicenseFile.setSelectedItem(ini.getProperty("licenseName", ""));
			// END KGU#363 2017-03-12
			// START KGU#630 2019-01-13: Enh. #662/4
			sod.chkRelativeCoordinates.setSelected(Arranger.A_STORE_RELATIVE_COORDS);
			// END KGU#630 2019-01-13
			// START KGU#690 2019-03-21: Enh. #707
			sod.chkArgNumbers.setSelected(Element.E_FILENAME_WITH_ARGNUMBERS);
			sod.cbSeparator.setSelectedItem(Element.E_FILENAME_SIG_SEPARATOR);
			// END KGU#690 2019-03-21
			sod.setVisible(true);

			if (sod.goOn == true) {
				Element.E_AUTO_SAVE_ON_CLOSE = sod.chkAutoSaveClose.isSelected();
				Element.E_AUTO_SAVE_ON_EXECUTE = sod.chkAutoSaveExecute.isSelected();
				Element.E_MAKE_BACKUPS = sod.chkBackupFile.isSelected();
				// START KGU#630 2019-01-13: Enh. #662/4
				Arranger.A_STORE_RELATIVE_COORDS = sod.chkRelativeCoordinates.isSelected();
				// END KGU#630 2019-01-13
				// START KGU#690 2019-03-21: Enh. #707
				Element.E_FILENAME_WITH_ARGNUMBERS = sod.chkArgNumbers.isSelected();
				Element.E_FILENAME_SIG_SEPARATOR = (Character) sod.cbSeparator.getSelectedItem();
				// END KGU#690 2019-03-21
				// START KGU#363 2017-03-12: Enh. #372 Allow user-defined author string
				ini.setProperty("authorName", sod.txtAuthorName.getText());
				String licName = (String) sod.cbLicenseFile.getSelectedItem();
				if (licName == null) {
					ini.setProperty("licenseName", "");
				} else {
					ini.setProperty("licenseName", licName);
				}
				// END KGU#363 2017-03-12
				ini.save();
			}
		} catch (FileNotFoundException e) {
			// START KGU#484 2018-04-05: Issue #463
			//e.printStackTrace();
			logger.log(Level.WARNING, "Trouble accessing preferences.", e);
			// END KGU#484 2018-04-05
		} catch (IOException e) {
			// START KGU#484 2018-04-05: Issue #463
			//e.printStackTrace();
			logger.log(Level.WARNING, "Trouble accessing saving preferences.", e);
			// END KGU#484 2018-04-05
		}
	}
	// END KGU#258 2016-09-26

	/*========================================
	 * font control
	 *========================================*/
	/**
	 * Opens the font configration dialog and processes configuration changes.
	 */
	public void fontNSD() {
		// START KGU#494 2018-09-10: Issue #508 - support option among fix / proportional padding
		//FontChooser fontChooser = new FontChooser(NSDControl.getFrame());
		FontChooser fontChooser = new FontChooser(NSDControl.getFrame(), true);
		// END KGU#494 2018-09-10
		Point p = getLocationOnScreen();
		fontChooser.setLocation(Math.round(p.x + (getVisibleRect().width - fontChooser.getWidth()) / 2 + this.getVisibleRect().x),
				Math.round(p.y + (getVisibleRect().height - fontChooser.getHeight()) / 2 + this.getVisibleRect().y));

		// set fields
		// START KGU#494 2018-09-10: Issue #508
		fontChooser.setFixPadding(Element.E_PADDING_FIX);
		// END KGU#494 2018-09-10
		fontChooser.setFont(Element.getFont());
		fontChooser.setVisible(true);

		// START KGU#393 2017-05-09: Issue #400 - make sure the changes were committed
		if (fontChooser.OK) {
		// END KGU#393 2017-05-09
			// get fields
			// START KGU#494 2018-09-10: Issue #508
			Element.E_PADDING_FIX = fontChooser.getFixPadding();
			// END KGU#494 2018-09-10
			Element.setFont(fontChooser.getCurrentFont());

			// save fields to ini-file
			Element.saveToINI();

			// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
			this.resetDrawingInfo();
			// END KGU#136 2016-03-02

			// redraw diagram
			redraw();
			// START KGU#444/KGU#618 2018-12-18: Issue #417, #649
			this.adaptScrollUnits();	// May be too early to get new draw rectangle but try at least
			// END KGU#444/KGU#618 2018-12-18
		// START KGU#393 2017-05-09: Issue #400
		}
		// END KGU#393 2017-05-09		

	}

	/**
	 * Enlarges the diagram font by two points
	 */
	public void fontUpNSD() {
		// change font size
		Element.setFont(new Font(Element.getFont().getFamily(), Font.PLAIN, Element.getFont().getSize() + 2));

		// save size
		Element.saveToINI();

		// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
		this.resetDrawingInfo();
		// END KGU#136 2016-03-02

		// redraw diagram
		redraw();
		// START KGU#444/KGU#618 2018-12-18: Issue #417, #649
		this.adaptScrollUnits();	// May be too early to get new draw rectangle but try at least
		// END KGU#444/KGU#618 2018-12-18
	}

	/**
	 * Diminishes the diagram font by two points.
	 */
	public void fontDownNSD() {
		if (Element.getFont().getSize() - 2 >= 4) {
			// change font size
			Element.setFont(new Font(Element.getFont().getFamily(), Font.PLAIN, Element.getFont().getSize() - 2));

			// save size
			Element.saveToINI();

			// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
			this.resetDrawingInfo();
			// END KGU#136 2016-03-02

			// redraw diagram
			redraw();
		}
		// START KGU#444/KGU#618 2018-12-18: Issue #417, #649
		this.adaptScrollUnits();	// May be too early to get new draw rectangle but try at least
		// END KGU#444/KGU#618 2018-12-18
	}

	/*========================================
	 * DIN conformity
	 *========================================*/
	/**
	 * Flips the graphical representation of FOR loops between DIN 66261 and
	 * ENDLESS design
	 *
	 * @see #setDIN()
	 * @see #getDIN()
	 */
	public void toggleDIN() {
		Element.E_DIN = !(Element.E_DIN);
		NSDControl.doButtons();
		// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
		this.resetDrawingInfo();
		// END KGU#136 2016-03-02		
		redraw();
	}

	/**
	 * Switches the graphical representation of FOR loops to DIN 66261 design
	 *
	 * @see #toggleDIN()
	 * @see #getDIN()
	 */
	public void setDIN() {
		Element.E_DIN = true;
		NSDControl.doButtons();
		// START KGU#136 2016-03-02: Bugfix #97 - cached bounds must be invalidated
		this.resetDrawingInfo();
		// END KGU#136 2016-03-02
		redraw();
	}

	/**
	 * @return true if the representation of FOR loops is currently conform to
	 * DIN-66261.
	 * @see #toggleDIN()
	 * @see #setDIN()
	 */
	public boolean getDIN() {
		return Element.E_DIN;
	}

	/*========================================
	 * diagram type and shape
	 *========================================*/
	/**
	 * Reduces the frame of the current diagram to a very frugal shape if
	 * {@code _unboxed} is true or switches it to a complete box otherwise.
	 *
	 * @see #isUnboxed()
	 */
	public void setUnboxed(boolean _unboxed) {
		root.isBoxed = !_unboxed;
		// START KGU#137 2016-01-11: Record this change in addition to the undoable ones
		//root.hasChanged=true;
		root.setChanged(true);
		// END KGU#137 2016-01-11
		// START KGU#136 2016-03-01: Bugfix #97
		root.resetDrawingInfoUp();	// Only affects Root
		// END KGU#136 2016-03-01
		redraw();
	}

	/**
	 * @return true if the frame of this diagram is reduced to a frugal shape.
	 * @see #setUnboxed(boolean)
	 */
	public boolean isUnboxed() {
		return !root.isBoxed;
	}

	/**
	 * Changes the type of {@link #root} to a Subroutine diagram.
	 *
	 * @see #setProgram()
	 * @see #setInclude()
	 * @see #isSubroutine()
	 */
	public void setFunction() {
		// Syntax highlighting must be renewed, outer dimensions may change for unboxed diagrams
		root.resetDrawingInfoDown();
		root.setProgram(false);
		// START KGU#902 2021-01-01: Enh. #903
		poppedElement = null;
		// END KGU#902 2021-01-01
		// START KGU#137 2016-01-11: Record this change in addition to the undoable ones
		//root.hasChanged=true;
		root.setChanged(true);
		// END KGU#137 2016-01-11
		// START KGU#253 2016-09-22: Enh. #249 - (un)check parameter list
		analyse();
		// END KGU#253 2016-09-22
		redraw();
		// START KGU#705 2019-10-01: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-10-01
	}

	/**
	 * Changes the type of {@link #root} to Main program.
	 *
	 * @see #setFunction()
	 * @see #setInclude()
	 * @see #isProgram()
	 */
	public void setProgram() {
		// Syntax highlighting must be renewed, outer dimensions may change for unboxed diagrams
		root.resetDrawingInfoDown();
		// START KGU#703 2019-03-30: Issue #720
		// START KGU#902 2021-01-01: Enh. #903
		poppedElement = null;
		// END KGU#902 2021-01-01
		boolean poolModified = false;
		if (root.isInclude() && Arranger.hasInstance()) {
			for (Root root : Arranger.getInstance().findIncludingRoots(root.getMethodName(), true)) {
				root.clearVarAndTypeInfo(false);
				poolModified = true;
			}
		}
		// END KGU#703 2019-03-30
		root.setProgram(true);
		// START KGU#137 2016-01-11: Record this change in addition to the undoable ones
		//root.hasChanged=true;
		root.setChanged(true);
		// END KGU#137 2016-01-11
		// START KGU#253 2016-09-22: Enh. #249 - (un)check parameter list
		analyse();
		// END KGU#253 2016-09-22
		redraw();
		// START KGU#701 2019-03-30: Issue #720
		if (poolModified) {
			Arranger.getInstance().redraw();
		}
		// END KGU#701 2019-03-30
		// START KGU#705 2019-10-01: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-10-01
	}

	// START KGU#376 2017-05-16: Enh. #389
	/**
	 * Changes the type of {@link #root} to Includable.
	 *
	 * @see #setFunction()
	 * @see #setProgram()
	 * @see #isInclude()
	 */
	public void setInclude() {
		// Syntax highlighting must be renewed, outer dimensions may change for unboxed diagrams
		root.resetDrawingInfoDown();
		root.setInclude(true);
		// START KGU#902 2021-01-01: Enh. #903
		poppedElement = null;
		// END KGU#902 2021-01-01
		// START KGU#703 2019-03-30: Issue #720
		boolean poolModified = false;
		if (Arranger.hasInstance()) {
			for (Root root : Arranger.getInstance().findIncludingRoots(root.getMethodName(), true)) {
				root.clearVarAndTypeInfo(false);
				poolModified = true;
			}
		}
		// END KGU#703 2019-03-30
		// Record this change in addition to the undoable ones
		root.setChanged(true);
		// check absense of parameter list
		analyse();
		redraw();
		// START KGU#701 2019-03-30: Issue #720
		if (poolModified) {
			Arranger.getInstance().redraw();
		}
		// END KGU#701 2019-03-30
		// START KGU#705 2019-10-01: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-10-01
	}
	// END KGU #376 2017-05-16

	/**
	 * @return true if the diagram is of type Main program
	 * @see #isSubroutine()
	 * @see #isInclude()
	 * @see #setProgram()
	 */
	public boolean isProgram() {
		return root.isProgram();
	}

	/**
	 * @return true if the diagram is of type Function
	 * @see #isProgram()
	 * @see #isInclude()
	 * @see #setFunction()
	 */
	public boolean isSubroutine() {
		return root.isSubroutine();
	}

	/**
	 * @return true if the diagram is of type Includable
	 * @see #isSubroutine()
	 * @see #isProgram()
	 * @see #setInclude()
	 */
	public boolean isInclude() {
		return root.isInclude();
	}

	/*========================================
	 * comment modes
	 *========================================*/
	public void setComments(boolean _comments) {
		Element.E_SHOWCOMMENTS = _comments;
		NSDControl.doButtons();
		redraw();
	}

	// START KGU#227 2016-07-31: Enh. #128
	void setCommentsPlusText(boolean _activate) {
		Element.E_COMMENTSPLUSTEXT = _activate;
		this.resetDrawingInfo();
		analyse();
		// START KGU#904 2021-01-01: Repaint allone did not adjust the scroll area
		//repaint();
		redraw();
		// END KGU#904 2021-01-01
	}
	// END KGU#227 2016-07-31

	public void setToggleTC(boolean _tc) {
		Element.E_TOGGLETC = _tc;
		// START KGU#136 2016-03-01: Bugfix #97
		this.resetDrawingInfo();
		// END KGU#136 2016-03-01
		NSDControl.doButtons();
		redraw();
	}

	void toggleTextComments() {
		Element.E_TOGGLETC = !Element.E_TOGGLETC;
		// START KGU#136 2016-03-01: Bugfix #97
		this.resetDrawingInfo();
		// END KGU#136 2016-03-01
		// START KGU#220 2016-07-27: Enh. #207
		analyse();
		// END KGU#220 2016-07-27
		repaint();
	}

	/*========================================
	 * further settings
	 *========================================*/
	/**
	 * Enables or disables the syntax higighting in the elements for all
	 * diagrams
	 *
	 * @param _highlight - true to switch syntax markup on, false to disable it
	 */
	public void setHightlightVars(boolean _highlight) {
		Element.E_VARHIGHLIGHT = _highlight;	// this is now directly used for drawing
		//root.highlightVars = _highlight;
		// START KGU#136 2016-03-01: Bugfix #97
		this.resetDrawingInfo();
		// END KGU#136 2016-03-01
		NSDControl.doButtons();
		redraw();
	}

	// START KGU#872 2020-10-17: Enh. #872 new display mode
	/**
	 * Enables or disables mode to display operators in C style
	 *
	 * @param _operatorsC - true to switch operator display mode to C style,
	 * false to standard
	 */
	public void setOperatorDisplayC(boolean _operatorsC) {
		Element.E_SHOW_C_OPERATORS = _operatorsC;
		if (_operatorsC) {
			Element.E_VARHIGHLIGHT = true;
		}
		this.resetDrawingInfo();
		NSDControl.doButtons();
		redraw();
	}
	// END KGU#872 2020-10-17

	/**
	 * Toggles the activation of the Analyser component (and the visibility of
	 * the Report list).
	 *
	 * @see #setAnalyser(boolean)
	 */
	public void toggleAnalyser() {
		setAnalyser(!Element.E_ANALYSER);
		if (Element.E_ANALYSER == true) {
			analyse();
		}
	}

	/**
	 * Enables or disables the Analyser, according to the value of
	 * {@code _analyse}.
	 *
	 * @param _analyse - the new status of Analyser (true to enable, obviously)
	 * @see #toggleAnalyser()
	 */
	public void setAnalyser(boolean _analyse) {
		Element.E_ANALYSER = _analyse;
		NSDControl.doButtons();
	}

	// START KGU#305 2016-12-14: Enh. #305
	/**
	 * Enables or disables the Arranger Index, according to the value of
	 * {@code _showIndex}.
	 *
	 * @param _showIndex - whether to enable (true) or disable (false) the index
	 * @see #showingArrangerIndex()
	 */
	public void setArrangerIndex(boolean _showIndex) {
		this.show_ARRANGER_INDEX = _showIndex;
		NSDControl.doButtons();
	}

	/**
	 * @return true if the Arranger index is currently enabled (shown), false
	 * otherwise
	 * @see #setArrangerIndex(boolean)
	 */
	public boolean showingArrangerIndex() {
		return this.show_ARRANGER_INDEX;
	}
	// END KGU#305 2016-12-14

	// START KGU#705 2019-09-23: Enh. #738
	/**
	 * Enables or disables the Code Preview, according to the value of
	 * {@code _showPreview}.
	 *
	 * @param _showPreview - whether to enable (true) or disable (false) the
	 * code preview
	 * @see #showingCodePreview()
	 */
	public void setCodePreview(boolean _showPreview) {
		this.show_CODE_PREVIEW = _showPreview;
		if (_showPreview) {
			this.updateCodePreview();
		}
		NSDControl.doButtons();
	}

	/**
	 * @return true if the Code Preview is currently enabled (shown), false
	 * otherwise
	 * @see #setCodePreview(boolean)
	 */
	public boolean showingCodePreview() {
		return this.show_CODE_PREVIEW;
	}
	// END KGU#305 216-12-14

	// START KGU#123 2016-01-04: Enh. #87
	/**
	 * Toggles the use of the mouse wheel for collapsing/expanding elements
	 *
	 * @see #toggleCtrlWheelMode()
	 * @see #configureWheelUnit()
	 */
	public void toggleWheelMode() {
		Element.E_WHEELCOLLAPSE = !Element.E_WHEELCOLLAPSE;
		// START KGU#792 2020-02-04: Bugfix #805
		Ini.getInstance().setProperty("wheelToCollapse", (Element.E_WHEELCOLLAPSE ? "1" : "0"));
		// END KGU#792 2020-02-04
	}
	// END KGU#123 2016-01-04

	// START KGU#503 2018-03-14: Enh. #519
	/**
	 * Toggles the effect of the mouse wheel (with Ctrl key pressed) for
	 * Zooming: in or out.
	 *
	 * @see #toggleWheelMode()
	 * @see #configureWheelUnit()
	 */
	public void toggleCtrlWheelMode() {
		Element.E_WHEEL_REVERSE_ZOOM = !Element.E_WHEEL_REVERSE_ZOOM;
		// START KGU#792 2020-02-04: Bugfix #805
		Ini.getInstance().setProperty("wheelCtrlReverse", (Element.E_WHEEL_REVERSE_ZOOM ? "1" : "0"));
		// END KGU#792 2020-02-04
		// START KGU#685 2020-12-12: Enh. #704
		// Behaviour of DiagramControllers should be consistent ...
		if (turtle != null) {
			turtle.setReverseZoomWheel(Element.E_WHEEL_REVERSE_ZOOM);
		}
		// END KGU#685 2020-12-12
	}
	// END KGU#503 2018-03-14

	// START KGU#699 2019-03-27: Issue #717 scrolling "speed" ought to be configurable
	/**
	 * Opens a little dialog offering to configure the default scrolling
	 * increment for the mouse wheel via a spinner.
	 *
	 * @see #toggleWheelMode()
	 * @see #toggleCtrlWheelMode()
	 */
	public void configureWheelUnit() {
		JSpinner spnUnit = new JSpinner();
		spnUnit.setModel(new SpinnerNumberModel(Math.max(1, Element.E_WHEEL_SCROLL_UNIT), 1, 20, 1));
		spnUnit.addMouseWheelListener(this);
		if (JOptionPane.showConfirmDialog(this.getFrame(),
				spnUnit,
				Menu.ttlMouseScrollUnit.getText(),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				IconLoader.getIcon(9)) == JOptionPane.OK_OPTION) {
			Element.E_WHEEL_SCROLL_UNIT = (Integer) spnUnit.getModel().getValue();
			this.adaptScrollUnits();
			if (Arranger.hasInstance()) {
				Arranger.getInstance().adaptScrollUnits();
			}
			// START KGU#792 2020-02-04: Bugfix #805
			Ini.getInstance().setProperty("wheelScrollUnit", Integer.toString(Element.E_WHEEL_SCROLL_UNIT));
			// END KGU#792 2020-02-04
		}
	}
	// END KGU#699 2019-03-27

	// START KGU#170 2016-04-01: Enh. #144: Maintain a preferred export generator
	/**
	 * Retrieves (and caches) the configured favourite code generator (also
	 * relevant for the code preview)
	 *
	 * @return Language title of the preferred generator
	 * @see #setPreferredGeneratorName(String)
	 */
	public String getPreferredGeneratorName() {
		if (this.prefGeneratorName.isEmpty()) {
			try {
				Ini ini = Ini.getInstance();
				ini.load();
				this.prefGeneratorName = ini.getProperty("genExportPreferred", "Java");
			} catch (IOException ex) {
				// START KGU#484 2018-04-05: Issue #463
				//ex.printStackTrace();
				logger.log(Level.WARNING, "Trouble accessing preferences.", ex);
				// END KGU#484 2018-04-05
			}
		}
		return this.prefGeneratorName;
	}
	// END KGU#170 2016-04-01

	// START KGU#705 2019-09-24: Enh. #738
	/**
	 * Retrieves (and caches) the configured favourite code generator (also
	 * relevant for the code preview).
	 *
	 * @param genName - Language title for the preferred generator
	 * @see #getPreferredGeneratorName()
	 */
	public void setPreferredGeneratorName(String genName) {
		if (genName == null || genName.trim().isEmpty()) {
			return;
		}
		try {
			Ini ini = Ini.getInstance();
			for (GENPlugin plugin : Menu.generatorPlugins) {
				if (genName.equalsIgnoreCase(plugin.title)) {
					ini.load();
					// START KGU#764 2019-11-29: Issue #777 - Another Sructorizer instance may have changed the favourite language
					//if (!genName.equalsIgnoreCase(this.prefGeneratorName)) {
					//	this.generatorUseCount = 1;
					//}
					//this.prefGeneratorName = plugin.title;
					//if (!this.prefGeneratorName.equals(ini.getProperty("genExportPreferred", "Java"))) {
					String iniGeneratorName = ini.getProperty("genExportPreferred", this.prefGeneratorName);
					boolean modified = !genName.equalsIgnoreCase(this.prefGeneratorName) || !genName.equalsIgnoreCase(iniGeneratorName);
					if (modified) {
						this.prefGeneratorName = plugin.title;
					// END KGU#764 2019-11-29
						ini.setProperty("genExportPreferred", plugin.title);
						ini.save();
						updateCodePreview();
						NSDControl.doButtons();
					}
					break;
				}
			}
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Trouble accessing preferences.", ex);
		}
	}
	// END KGU#705 2ß19-09-24

	/*========================================
	 * inputbox methods
	 *========================================*/
	// START KGU 2015-10-14: additional parameters for title customisation
	//public void showInputBox(EditData _data)
	/**
	 * Opens the appropriate element editor version for element type
	 * {@code _elementType} and gathers the editing results in {@code _data}.
	 *
	 * @param _data - container for the content transfer between the element and
	 * the InputBox
	 * @param _elementType - Class name of the {@link Element} we offer editing
	 * for
	 * @param _isInsertion - Indicates whether or not the element is a new
	 * object
	 * @param _allowCommit - Whether the OK button is enabled (not for immutable
	 * elements)
	 */
	public void showInputBox(EditData _data, String _elementType, boolean _isInsertion, boolean _allowCommit) // END KGU 2015-10-14
	{
		if (NSDControl != null) {
			// START KGU#946 2021-02-28: Bugfix #947: Now the type is distinguished before calling this
			boolean isRoot = _elementType.equals("Root")
					|| _elementType.equals("Function")
					|| _elementType.equals("Includable");
			// END KGU#946 2021-02-28
			// START KGU#170 2016-04-01: Issue #143 - on opening the editor a comment popup should vanish
			hideComments();
			// END KGU#170 2016-04-01
			// START KGU#3 2015-10-25: Dedicated support for FOR loops
			//InputBox inputbox = new InputBox(NSDControl.getFrame(),true);
			InputBox inputbox = null;
			if (_elementType.equals("For")) {
				InputBoxFor ipbFor = new InputBoxFor(NSDControl.getFrame(), true);
				// START #61 2016-09-24: After partial redesign some things work differently, now
				//if (!_isInsertion)
				//{
				if (_isInsertion) {
					// Split the default text to find out what style it is
					String[] forFractions = For.splitForClause(_data.text.getLongString());
					for (int i = 0; i < 4; i++) {
						_data.forParts.add(forFractions[i]);
					}
					if (forFractions[5] != null) {
						_data.forParts.add(forFractions[5]);
					}
				}
				// END KGU#61 2016-09-24
				ipbFor.txtVariable.setText(_data.forParts.get(0));
				ipbFor.txtStartVal.setText(_data.forParts.get(1));
				ipbFor.txtEndVal.setText(_data.forParts.get(2));
				ipbFor.txtIncr.setText(_data.forParts.get(3));
				// START KGU#61 2016-03-21: Enh. #84 - Consider FOR-IN loops
				//ipbFor.chkTextInput.setSelected(!_data.forPartsConsistent);
				//ipbFor.enableTextFields(!_data.forPartsConsistent);
				if (_data.forParts.count() > 4) {
					ipbFor.txtValueList.setText(ipbFor.forInValueList = _data.forParts.get(4));
					ipbFor.txtVariableIn.setText(_data.forParts.get(0));
				}
				boolean textMode = _data.forLoopStyle == For.ForLoopStyle.FREETEXT;
				ipbFor.chkTextInput.setSelected(textMode);
				ipbFor.enableTextFields(textMode);
				ipbFor.setIsTraversingLoop(_data.forLoopStyle == For.ForLoopStyle.TRAVERSAL);
				// END KGU#61 2016-03-21
				inputbox = ipbFor;
			}
			// START KGU#363 2017-03-13: Enh. #372
			// START KGU#946 2021-02-28: Bugfix #947: Now the type is distinguished before calling this
			//else if (_elementType.equals("Root")) {
			else if (isRoot) {
			// END KGU#946 2021-02-28
				InputBoxRoot ipbRt = new InputBoxRoot(getFrame(), true);
				ipbRt.licenseInfo = _data.licInfo;
				// START KGU#376 2017-07-01: Enh. #389
				ipbRt.setIncludeList(_data.diagramRefs);
				// END KGU#376 2017-07-01
				inputbox = ipbRt;
			}
			// END KGU#363 2017-03-13
			// START KGU#916 2021-01-24: Enh. #915
			else if (_elementType.equals("Case") && Element.useInputBoxCase) {
				// START KGU#927 2021-02-06: Enh. #915
				//inputbox = new InputBoxCase(getFrame(), true);
				inputbox = new InputBoxCase(getFrame(), true, new CaseEditHelper(root));
				((InputBoxCase) inputbox).branchOrder = _data.branchOrder;
				// END KGU#927 2021-02-06
				inputbox.txtText.setVisible(false);
			}
			// END KGU#916 2021-01-24
			else {
				inputbox = new InputBox(getFrame(), true);
			}
			// END KGU#3 2015-10-25
			inputbox.setLocationRelativeTo(getFrame());

			// set title (as default)
			inputbox.setTitle(_data.title);

			// set field
			inputbox.txtText.setText(_data.text.getText());
			inputbox.txtComment.setText(_data.comment.getText());
			// START KGU#43 2015-10-12: Breakpoint support
			// START KGU#946 2021-02-28: Bugfix #947 - it might be a different Root we summon here...
			//boolean notRoot = getSelected() != root;
			//inputbox.chkBreakpoint.setVisible(notRoot);
			inputbox.chkBreakpoint.setVisible(!isRoot);
			// END KGU#946 2021-02-28
			inputbox.chkBreakpoint.setSelected(_data.breakpoint);
			// START KGU#686 2019-03-17: Enh. #56 - Introduction of Try
			if (_elementType.equals("Try") || _elementType.equals("Forever")) {
				inputbox.chkBreakpoint.setEnabled(false);
				inputbox.chkBreakpoint.setSelected(false);
			}
			// END KGU#686 2019-03-17
			// END KGU#43 2015-10-12
			// START KGU#695 2021-01-22: Enh. #714: Special checkbox for Try elements
			if (_elementType.equals("Try")) {
				inputbox.chkShowFinally.setVisible(true);
				inputbox.chkShowFinally.setSelected(_data.showFinally);
			}
			// END KGU#695 2021-01-22
			// START KGU#213 2016-08-01: Enh. #215
			// START KGU#246 2016-09-13: Bugfix #241)
			//inputbox.lblBreakTrigger.setText(inputbox.lblBreakText.getText().replace("%", Integer.toString(_data.breakTriggerCount)));
			// START KGU#213 2016-10-13: Enh. #215 - Make it invisible if zero
			//inputbox.lblBreakTriggerText.setVisible(notRoot);
			inputbox.lblBreakTriggerText.setVisible(!isRoot && _data.breakTriggerCount > 0);
			// END KGU#213 2016-10-13
			inputbox.lblBreakTrigger.setText(Integer.toString(_data.breakTriggerCount));
			// END KGU#246 2016-09-13
			// END KGU#213 2016-08-01
			// START KGU#277 2016-10-13: Enh. #270
			inputbox.chkDisabled.setVisible(!isRoot);
			inputbox.chkDisabled.setSelected(_data.disabled);
			// END KGU#277 2016-10-13
			
			// START KGU#1057 2022-08-18: Enh. #1066 Support for autocompletions
			if (!isRoot && !_elementType.equals("Parallel") && !_elementType.equals("Try")) {
				ArrayList<String> words = new ArrayList<String>();
				// START KGU#1062 2022-08-25 Enh. #1066
				words.add("Infinity");
				// END KGU#1062 2022-08-25
				StringList varNames = root.getVarNames();
				for (int i = 0; i < varNames.count(); i++) {
					words.add(varNames.get(i));
				}
				// For calls, we add the known routine signatures
				if (_elementType.equals("Call") && Arranger.hasInstance()) {
					Set<Root> roots = Arranger.getInstance().getAllRoots();
					for (Root rt: roots) {
						if (rt.isSubroutine()) {
							words.add(rt.getSignatureString(false, true));
						}
					}
				}
				Collections.sort(words, String.CASE_INSENSITIVE_ORDER);
				inputbox.words = words;
				inputbox.typeMap = root.getTypeInfo();
				inputbox.pnlSuggest.setVisible(true);
			}
			// END KGU#1057 2022-08-18

			inputbox.OK = false;
			// START KGU#42 2015-10-14: Pass the additional information for title translation control
			// START KGU#42 2019-03-05: Adapted to new type set
			//if (_elementType.equals("Root") && !this.isProgram())
			//{
			//	_elementType = "Function";
			//}
			if (_elementType.equals("Root")) {
				if (this.isSubroutine()) {
					_elementType = "Function";
				} else if (this.isInclude()) {
					_elementType = "Includable";
				}
			}
			// END KGU#42 2019-03-05
			else if (_elementType.equals("Forever")) {
				inputbox.lblText.setVisible(false);
				inputbox.txtText.setVisible(false);
			}
			inputbox.elementType = _elementType;
			inputbox.forInsertion = _isInsertion;
			// END KGU#42 2015-10-14
			// START KGU#61 2016-03-21: Give InputBox an opportunity to check and ensure consistency
			inputbox.checkConsistency();
			// END KGU#61 2016-03-21
			// START KGU#911 2021-01-10: Enh. #910
			inputbox.btnOK.setEnabled(_allowCommit);
			// END KGU#911 2021-01-10
			// START KGU#1104 2023-11-09: Enh. #1114 Place the caret to the first question mark for new elements
			if (_isInsertion) {
				// Typically, the default texts contain a question mark where the condition is to be inserted
				int posQM = inputbox.txtText.getText().indexOf('?');
				if (posQM > 0) {
					inputbox.txtText.setCaretPosition(posQM);
					inputbox.txtText.setSelectionEnd(posQM+1);
				}
			}
			// END KGU#1104 2023-11-09
			inputbox.setVisible(true);

			// -------------------------------------------------------------------------------------
			// START KGU#927 2021-02-07: Enh. #915 Avoid unnecessary Case branch reordering
			_data.branchOrder = null;
			// END KGU#927 2021-02-07
			// get fields
			_data.text.setText(inputbox.txtText.getText());
			_data.comment.setText(inputbox.txtComment.getText());
			// START KGU#43 2015-10-12: Breakpoint support
			_data.breakpoint = inputbox.chkBreakpoint.isSelected();
			// END KGU#43 2015-10-12
			// START KGU#277 2016-10-13: Enh. #270
			_data.disabled = inputbox.chkDisabled.isSelected();
			// END KGU#277 2016-10-13
// START KGU#213 2016-08-01: Enh. #215 (temporarily disabled again)
//			try{
//				_data.breakTriggerCount = Integer.parseUnsignedInt(inputbox.txtBreakTrigger.getText());
//			}
//			catch (Exception ex)
//			{
//				_data.breakTriggerCount = 0;
//			}
// END KGU#213 2016-08-01
			// START KGU#695 2021-01-22: Enh. #714
			_data.showFinally = inputbox.chkShowFinally.isSelected();
			// END KGU#695 2021-01-22
			// START KGU#3 2015-10-25: Dedicated support for For loops
			if (inputbox instanceof InputBoxFor) {
				_data.forParts = new StringList();
				_data.forParts.add(((InputBoxFor) inputbox).txtVariable.getText());
				_data.forParts.add(((InputBoxFor) inputbox).txtStartVal.getText());
				_data.forParts.add(((InputBoxFor) inputbox).txtEndVal.getText());
				_data.forParts.add(((InputBoxFor) inputbox).txtIncr.getText());
				// START KGU#61 2016-03-21: Enh. #84 - consider FOR-IN loops
				_data.forLoopStyle = ((InputBoxFor) inputbox).identifyForLoopStyle();
				if (_data.forLoopStyle == For.ForLoopStyle.TRAVERSAL) {
					// (InputBoxFor)inputbox).txtVariableIn.getText() should equal (InputBoxFor)inputbox).txtVariable.getText(),
					// such that nothing must be done about it here
					_data.forParts.add(((InputBoxFor) inputbox).forInValueList);
				}
				if (((InputBoxFor) inputbox).chkTextInput.isSelected() && !((InputBoxFor) inputbox).isLoopDataConsistent()) {
					_data.forLoopStyle = For.ForLoopStyle.FREETEXT;
				}
				// END KGU#61 2016-03-21

			}
			// END KGU#3 2015-10-25
			// START KGU#363 2017-03-13: Enh. 372
			else if (inputbox instanceof InputBoxRoot) {
				// START KGU#363 2017-05-20
				//_data.authorName = ((InputBoxRoot)inputbox).txtAuthorName.getText();
				//_data.licenseName = ((InputBoxRoot)inputbox).licenseInfo.licenseName;
				//_data.licenseText = ((InputBoxRoot)inputbox).licenseInfo.licenseText;
				_data.licInfo = ((InputBoxRoot) inputbox).licenseInfo;
				// END KGU#363 2017-05-20
				// START KGU#376 2017-07-01: Enh. #389
				_data.diagramRefs = ((InputBoxRoot) inputbox).getIncludeList();
				// END KGU#376 2017-07-01
			}
			// END KGU#363 2017-03-13
			// START KGU#916 2021-01-24: Enh. #915 additional functionality for Case elements
			else if (inputbox instanceof InputBoxCase) {
				_data.branchOrder = ((InputBoxCase) inputbox).branchOrder;
			}
			// END KGU#916 2021-01-24
			_data.result = inputbox.OK;

			inputbox.dispose();
		}
	}

	/*========================================
	 * CLIPBOARD INTERACTIONS
	 *========================================*/
	/**
	 * Copies the diagram as a PNG picture to the system clipboard.<br/>
	 * NOTE: For some platforms, the clipboard content may yet be a JPEG image
	 * rather than PNG.
	 *
	 * @see #copyToClipboardEMF()
	 */
	public void copyToClipboardPNG() {
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll(true);
		// END KGU 2015-10-11

		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//DataFlavor pngFlavor = new DataFlavor("image/png","Portable Network Graphics");

		// get diagram
		// START KGU#660 2019-03-25: Issue #685
		//BufferedImage image = new BufferedImage(root.width+1,root.height+1, BufferedImage.TYPE_INT_ARGB);
		int imageType = BufferedImage.TYPE_INT_ARGB;
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			/* Windows always converts to JPEG instead of PNG, so it doesn't cope
			 * with alpha channel. In Java 11, this will produce exception stack
			 * traces, so we just circumvent it now. 
			 */
			imageType = BufferedImage.TYPE_INT_RGB;
		}
		BufferedImage image = new BufferedImage(root.width + 1, root.height + 1, imageType);
		// END KGU#660 2019-03-25
		// START KGU#906 2021-01-06: Enh. #905 - we don't want the triangles in the clipboard
		//root.draw(image.getGraphics(), null);
		root.draw(image.getGraphics(), null, DrawingContext.DC_IMAGE_EXPORT);
		// END KGU#906 2021-01-06

		// put image to clipboard
		ImageSelection imageSelection = new ImageSelection(image);
		systemClipboard.setContents(imageSelection, null);

		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null) {
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}

	/**
	 * Copies the diagram as a EMF picture to the system clipboard.
	 *
	 * @see #copyToClipboardPNG()
	 */
	public void copyToClipboardEMF() {
		// START KGU#183 2016-04-24: Issue #169 - retain old selection
		Element wasSelected = selected;
		// END KGU#183 2016-04-24

		// START KGU 2015-10-11
		//root.selectElementByCoord(-1,-1);	// Unselect all elements
		//redraw();
		unselectAll(true);
		// END KGU 2015-10-11

		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		try {
			ByteArrayOutputStream myEMF = new ByteArrayOutputStream();
			EMFGraphics2D emf = new EMFGraphics2D(myEMF, new Dimension(root.width + 6, root.height + 1));
			emf.setFont(Element.getFont());

			emf.startExport();
			lu.fisch.graphics.Canvas c = new lu.fisch.graphics.Canvas(emf);
			lu.fisch.graphics.Rect myrect = root.prepareDraw(c);
			root.draw(c, myrect, null, false);
			emf.endExport();

			systemClipboard.setContents(new EMFSelection(myEMF), null);
		} catch (Exception e) {
			// START KGU#484 2018-04-05: Issue #463
			//e.printStackTrace();
			logger.log(Level.WARNING, "Clipboad action failed.", e);
			// END KGU#484 2018-04-05
		}

		// START KGU#183 2016-04-24: Issue #169 - restore old selection
		selected = wasSelected;
		if (selected != null) {
			selected.setSelected(true);
		}
		redraw();
		// END KGU#183 2016-04-24
	}

	// Inner class is used to hold an image while on the clipboard.
	public static class EMFSelection implements Transferable, ClipboardOwner {

		public static final DataFlavor emfFlavor = new DataFlavor("image/emf", "Enhanced Meta File");
		// the Image object which will be housed by the ImageSelection
		private ByteArrayOutputStream os;

		static {
			try {
				SystemFlavorMap sfm = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
				sfm.addUnencodedNativeForFlavor(emfFlavor, "ENHMETAFILE");
			} catch (Exception e) {
				String message = e.getMessage();
				if (message == null || message.isEmpty()) {
					message = e.toString();
				}
				logger.logp(Level.SEVERE, "EMFSelection", "static init", message);
			}
		}

		private static DataFlavor[] supportedFlavors = {emfFlavor};

		public EMFSelection(ByteArrayOutputStream os) {
			this.os = os;
		}

		// Returns the supported flavors of our implementation
		public DataFlavor[] getTransferDataFlavors() {
			return supportedFlavors;
		}

		// Returns true if flavor is supported
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			for (int i = 0; i < supportedFlavors.length; i++) {
				DataFlavor f = supportedFlavors[i];
				if (f.equals(flavor)) {
					return true;
				}
			}
			return false;
		}

		// Returns Image object housed by Transferable object
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor.equals(emfFlavor)) {
				return (new ByteArrayInputStream(os.toByteArray()));
			} else {
				//System.out.println("Hei !!!");
				throw new UnsupportedFlavorException(flavor);
			}
		}

		public void lostOwnership(Clipboard arg0, Transferable arg1) {
		}

	}

	// Inner class is used to hold an image while on the clipboard.
	public static class ImageSelection implements Transferable {
		// the Image object which will be housed by the ImageSelection

		private Image image;

		public ImageSelection(Image image) {
			this.image = image;
		}

		// Returns the supported flavors of our implementation
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}

		// Returns true if flavor is supported
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		// Returns Image object housed by Transferable object
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!DataFlavor.imageFlavor.equals(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			// else return the payload
			return image;
		}
	}

	/*========================================
	 * ANALYSER
	 *========================================*/
	/**
	 * Instigates the analysis of the current diagram held in {@link #root}
	 */
	protected void analyse() {
		if (Element.E_ANALYSER && errorlist != null && isInitialized) {
			//System.out.println("Analysing ...");

			// Olà - The use of a thread to show the errorlist
			// seams not to work, because the list is not always
			// shown. Concurrency problem?

			/*
			 Analyser analyser = new Analyser(root,errorlist);
			 analyser.start();
			 /**/
			//System.out.println("Working ...");
			Vector<DetectedError> vec = root.analyse();
			DefaultListModel<DetectedError> errors =
					(DefaultListModel<DetectedError>) errorlist.getModel();
			errors.clear();

			for (int i = 0; i < vec.size(); i++) {
				errors.addElement(vec.get(i));
			}

			errorlist.repaint();
			errorlist.validate();
		}
	}

	/*========================================
	 * Recently used files
	 *========================================*/
	/**
	 * Adds the given path {@code _filename} to the list of recently used files.
	 *
	 * @param _filename - the path of the file most recently used (loaded/saved)
	 * @see #addRecentFile(String, boolean)
	 */
	public void addRecentFile(String _filename) {
		addRecentFile(_filename, true);
	}

	/**
	 * Adds the given {@code _filename} to the list of recently used files. If
	 * this would exceed {@link #MAX_RECENT_FILES} then the oldest file name
	 * gets dropped from the list.
	 *
	 * @param _filename - path of a file most recently used
	 * @param saveINI - if true then we will immediately save the list to the
	 * ini file and have all button visibility checked immediately.
	 * @see #addRecentFile(String)
	 */
	public void addRecentFile(String _filename, boolean saveINI) {
		if (!recentFiles.isEmpty() && recentFiles.get(0).equals(_filename)) {
			return;	// nothing to do
		}
		if (recentFiles.contains(_filename)) {
			recentFiles.remove(_filename);
		}
		recentFiles.insertElementAt(_filename, 0);
		while (recentFiles.size() > MAX_RECENT_FILES) {
			recentFiles.removeElementAt(recentFiles.size() - 1);
		}
		// START KGU#602 2018-10-28 - saveINI = false is typically called on startup, so postpone the button stuff too 
		//NSDControl.doButtons();
		//if (saveINI==true) {NSDControl.savePreferences();}
		if (saveINI) {
			NSDControl.doButtons();
			NSDControl.savePreferences();
		}
		// END KGU#602 2018-10-28
	}

	/*========================================
	 * Run
	 *========================================*/
	/**
	 * Activates the {@link Executor} forcing open the debug {@link Control}.
	 *
	 * @see #goTurtle()
	 */
	public void goRun() {
		// START KGU#448 2018-01-05: Enh. #443 - generalized DiagramController activation
		//Executor.getInstance(this,null);
		Executor.getInstance(this, this.getEnabledControllers());
		// END KGU#448 2018-01-05
		if (root.advanceTutorialState(26, this.root)) {
			analyse();
		}
		// KGU#908 2021-01-06: Bugfix #907 - the previous three lins were duplicate here
	}

	/**
	 * Ensures the {@link TurtleBox} being open and activates the
	 * {@link Executor} forcing open the debug {@link Control}.
	 *
	 * @see #goRun()
	 */
	public void goTurtle() {
		if (turtle == null) {
			// START KGU#889 2020-12-28: Enh. #890 statusbar needs slightly more width
			//turtle = new TurtleBox(500,500);
			turtle = new TurtleBox(512, 560);
			// END KGU#889 2020-12-28
			// START KGU#685 2020-12-12: Enh. #704
			Locales.getInstance().register(turtle.getFrame(), true);
			turtle.setReverseZoomWheel(Element.E_WHEEL_REVERSE_ZOOM);
			// END KGU#685 2020-12-12
			// START KGU#894 2020-12-21: Issue #895 Wasn't correctly scaled (with "Nimbus")
			turtle.updateLookAndFeel();
			GUIScaler.rescaleComponents(turtle.getFrame());
			// END KGU#894 2020-12-21
		}
		turtle.setVisible(true);
		// Activate the executor (getInstance() is supposed to do that)
		// START KGU#448 2018-01-05: Enh. #443: Cope with potentially several controllers
		//Executor.getInstance(this,turtle);
		this.enableController(turtle.getClass().getName(), true);
		goRun();
		// END KGU#448 2018-01-05

	}

	/**
	 * Checks for running status of the Root currently held and suggests the
	 * user to stop the execution if it is running
	 *
	 * @return true if the fostered Root isn't executed (action may proceed),
	 * false otherwise
	 */
	private boolean checkRunning() {
		if (this.root == null || !this.root.isExecuted()) {
			return true;	// No problem
		}		// Give the user the chance to kill the execution but don't do anything for now,
		// whatever the user may have been decided.
		Executor.getInstance(null, null);
		return false;
	}

	// START KGU#448 2018-01-05: Enh. #443
	/**
	 * Lazy initialization method for static field {@link #diagramControllers}
	 *
	 * @return the initialized list of {@link DiagramController} instances; the
	 * first element (reserved for a {@link TurtleBox)} instance) may be null.
	 */
	// START KGU#911 2021-01-10: Enh. #910 Result type changed
	//protected ArrayList<DiagramController> getDiagramControllers() {
	protected LinkedHashMap<DiagramController, Root> getDiagramControllers() {
	// END KGU#911 2021-01-10
		if (diagramControllers != null) {
			return diagramControllers;
		}
		// START KGU#911 2021-01-10: Enh. #910 data structure changed
		//diagramControllers = new ArrayList<DiagramController>();
		// Turtleizer is always added as first entry (no matter whether initialized or not)
		//diagramControllers.add(turtle);
		diagramControllers = new LinkedHashMap<DiagramController, Root>();
		// We try to add Turtleizer as first entry
		if (turtle != null) {
			diagramControllers.put(turtle, null);
		}
		// END KGU#911 2021-01-10
		String errors = "";
		Vector<GENPlugin> plugins = Menu.controllerPlugins;
		if (plugins.isEmpty()) {
			BufferedInputStream buff = null;
			try {
				buff = new BufferedInputStream(getClass().getResourceAsStream("controllers.xml"));
				GENParser genp = new GENParser();
				plugins = genp.parse(buff);
			} catch (Exception ex) {
				errors = ex.toString();
				// START KGU#484 2018-04-05: Issue #463
				//ex.printStackTrace();
				logger.log(Level.WARNING, "Trouble accessing controller plugin definitions.", ex);
				// END KGU#484 2018-04-05
			}
			if (buff != null) {
				try {
					buff.close();
				} catch (IOException ex) {
					// START KGU#484 2018-04-05: Issue #463
					//ex.printStackTrace();
					logger.log(Level.WARNING, "Couldn't close the controller plugin definition file.", ex);
					// END KGU#484 2018-04-05
				}
			}
		}
		for (int i = 0; i < plugins.size(); i++) {
			GENPlugin plugin = plugins.get(i);
			final String className = plugin.className;
			// If it's not Turtleizer then add it to the available controllers
			// START KGU#911 2021-01-10: Bugfix on behalf of #910
			//if (!className.equals("TurtleBox")) {
			if (!className.equals("lu.fisch.turtle.TurtleBox")) {
			// END KGU#911 2021-01-10
				try {
					Class<?> genClass = Class.forName(className);
					// START KGU#911 2021-01-10: Enh. #910 data structure changed
					//diagramControllers.add((DiagramController) genClass.getDeclaredConstructor().newInstance());
					DiagramController ctrlr = (DiagramController) genClass.getDeclaredConstructor().newInstance();
					// Try to set the name according to the plugin title (does not necessarily work)
					ctrlr.setName(plugin.title);
					Root incl = constructDiagrContrIncludable(ctrlr);
					diagramControllers.put(ctrlr, incl);
					// END KGU#911 2021-01-10
				} catch (Exception ex) {
					errors += "\n" + plugin.title + ": " + ex.getLocalizedMessage();
				}
			}
		}
		if (!errors.isEmpty()) {
			errors = Menu.msgTitleLoadingError.getText() + errors;
			JOptionPane.showMessageDialog(this.getFrame(), errors,
					Menu.msgTitleParserError.getText(), JOptionPane.ERROR_MESSAGE);
		}
		return diagramControllers;
	}

	// START KGU#911 2021-01-10: Enh. #910 - We represent DiagramControllers by includables
	/**
	 * Constructs a special Includable diagram for the given DiagramController
	 * {@code controller} listing all provided routines in the comment and
	 * defining specified data type (particularly enumeration types)
	 *
	 * @param controller - a {@link DiagramController} implementor instance
	 * @return a special immutable Includable
	 */
	private Root constructDiagrContrIncludable(DiagramController controller) {
		Root incl = new Root(StringList.getNew("$" + controller.getName().replace(" ", "_")));
		incl.setInclude(false);
		StringList comment = new StringList();
		comment.add("Represents Diagram Controller \"" + controller.getName() + "\"");
		comment.add("");
		comment.add("Provided procedures:");
		int count = addRoutineSignatures(controller.getProcedureMap(), comment);
		if (count == 0) {
			comment.add("\t-");
		}
		comment.add("Provided Functions:");
		count = addRoutineSignatures(controller.getFunctionMap(), comment);
		if (count == 0) {
			comment.add("\t-");
		}
		String[] enumDefs = controller.getEnumerators();
		if (enumDefs != null) {
			comment.add("Provided Enumeration Types:");
			count = 0;
			for (int j = 0; j < enumDefs.length; j++) {
				String enumDef = enumDefs[j];
				int posBrace = enumDef.indexOf("{");
				if (posBrace > 0) {
					String typeName = null;
					if (enumDef.startsWith("enum ")) {
						// Seems to be in Java syntax, convert it to Structorizer syntax
						typeName = enumDef.substring(5, posBrace).trim();
						enumDef = ("type " + typeName + " = enum" + enumDef.substring(posBrace)).trim();
						if (enumDef.endsWith(";")) {
							enumDef = enumDef.substring(0, enumDef.length() - 1);
						}
					} else if (enumDef.startsWith("type ")
							&& (posBrace = enumDef.substring(0, posBrace).indexOf("=")) > 0) {
						typeName = enumDef.substring(5, posBrace).trim();
					}
					if (typeName != null) {
						comment.add(String.format("%4d. %s", ++count, typeName));
						Instruction typedef = new Instruction(enumDef);
						incl.children.addElement(typedef);
					}
				}
			}
			if (count == 0) {
				comment.add("\t-");
			}
		}
		incl.setComment(comment);
		incl.children.addElement(new Instruction("restart()"));
		incl.setDisabled(true);
		return incl;
	}

	/**
	 * Retrieves the signatures of the given API routines and adds them to the
	 * StringList {@code comment}.
	 *
	 * @param routines - the procedure or function map of the DiagramController
	 * @param comment - the {@link StringList} the routine descriptions are to
	 * be added to
	 * @return number of routines
	 */
	public int addRoutineSignatures(HashMap<String, Method> routines, StringList comment) {
		int count = 0;
		for (Map.Entry<String, Method> entry : routines.entrySet()) {
			String[] parts = entry.getKey().split("#", -1);
			Method meth = entry.getValue();
			if (meth.getName().equalsIgnoreCase(parts[0])) {
				// prefer the true name
				parts[0] = meth.getName();
			}
			Class<?>[] argTypes = meth.getParameterTypes();
			Class<?> resType = meth.getReturnType();
			StringList typeNames = new StringList();
			String resTypeName = "";
			if (resType != null && !resType.getName().equals("void")) {
				resTypeName = ": " + resType.getSimpleName();
			}
			for (int i = 0; i < argTypes.length; i++) {
				typeNames.add(argTypes[i].getSimpleName());
			}

			comment.add(String.format("%4d. %s(%s)%s", ++count,
					parts[0], typeNames.concatenate(", "), resTypeName));
		}
		return count;
	}
	// END KGU#911 2021-01-10

	/**
	 * @return an array of {@link DiagramController} instances enabled for
	 * execution
	 * @see #isControllerEnabled(String)
	 * @see #enableController(String, boolean)
	 */
	private DiagramController[] getEnabledControllers() {
		this.getDiagramControllers();
		LinkedList<DiagramController> controllers = new LinkedList<DiagramController>();
		// START KGU#911 2021-01-09: Enh. #910 status now coded in the Includables
		//long mask = 1;
		//for (DiagramController contr: diagramControllers) {
		//	if (contr != null && (this.enabledDiagramControllers & mask) != 0) {
		//		controllers.add(contr);
		//	}
		//	mask <<= 1;
		//}
		for (Map.Entry<DiagramController, Root> entry : diagramControllers.entrySet()) {
			if (entry.getValue() == null || !entry.getValue().isDisabled(false)) {
				controllers.add(entry.getKey());
			}
		}
		// END KGU#911 2021-01-09
		return controllers.toArray(new DiagramController[controllers.size()]);
	}
	// END KGU#448 2018-01-08

	// START KGU#177 2016-04-07: Enh. #158
	/**
	 * Tries to shift the selection to the next element in the _direction
	 * specified. It turned out that on going down and right it's most intuitive
	 * to dive into the substructure of compound elements (rather than jumping
	 * to its successor). (For Repeat elements this holds on going up).
	 *
	 * @param _direction - the cursor key orientation (up, down, left, right)
	 */
	public void moveSelection(Editor.CursorMoveDirection _direction) {
		if (selected != null) {
			Rect selRect = selected.getRectOffDrawPoint();
			// Get center coordinates
			int x = (selRect.left + selRect.right) / 2;
			int y = (selRect.top + selRect.bottom) / 2;
			switch (_direction) {
			case CMD_UP:
				// START KGU#495 2018-02-15: Bugfix #511 - we must never dive into collapsed loops!
				//if (selected instanceof Repeat)
				if (selected instanceof Repeat && !selected.isCollapsed(false)) // END KGU#495 2018-02-15
				{
					// START KGU#292 2016-11-16: Bugfix #291
					//y = ((Repeat)selected).getRectOffDrawPoint().bottom - 2;
					y = ((Repeat) selected).getBody().getRectOffDrawPoint().bottom - 2;
					// END KGU#292 2016-11-16
				} else if (selected instanceof Root) {
					y = ((Root) selected).children.getRectOffDrawPoint().bottom - 2;
				} else {
					y = selRect.top - 2;
				}
				break;
			case CMD_DOWN:
				// START KGU#495 2018-02-15: Bugfix #511 - we must never dive into collapsed loops!
				//if (selected instanceof ILoop && !(selected instanceof Repeat))
				if (selected instanceof ILoop && !selected.isCollapsed(false) && !(selected instanceof Repeat))
				// END KGU#495 2018-02-15
				{
					Subqueue body = ((ILoop) selected).getBody();
					y = body.getRectOffDrawPoint().top + 2;
				}
				// START KGU#346 2017-02-08: Issue #198 - Unification of forking elements
				//else if (selected instanceof Alternative)
				//{
				//	y = ((Alternative)selected).qTrue.getRectOffDrawPoint().top + 2;
				//}
				//else if (selected instanceof Case)
				//{
				//	y = ((Case)selected).qs.get(0).getRectOffDrawPoint().top + 2;
				//}
				// START KGU#498 2018-02-18: Bugfix #511 - cursor was caught when collapsed
				//else if (selected instanceof IFork)
				else if (selected instanceof IFork && !selected.isCollapsed(false))
				// END KGU#498 2018-02-18
				{
					y = selRect.top + ((IFork) selected).getHeadRect().bottom + 2;
				}
				// END KGU#346 2017-02-08
				// START KGU#498 2018-02-18: Bugfix #511 - cursor was caught when collapsed
				//else if (selected instanceof Parallel)
				else if (selected instanceof Parallel && !selected.isCollapsed(false))
				// END KGU#498 2018-02-18
				{
					y = ((Parallel) selected).qs.get(0).getRectOffDrawPoint().top + 2;
				} else if (selected instanceof Root) {
					y = ((Root) selected).children.getRectOffDrawPoint().top + 2;
				}
				// START KGU#729 2019-09-24: Bugfix #751
				else if (selected instanceof Try) {
					y = ((Try) selected).qTry.getRectOffDrawPoint().top + 2;
				}
				// END KGU#729 2019-09-24
				else {
					y = selRect.bottom + 2;
				}
				break;
			case CMD_LEFT:
				if (selected instanceof Root) {
					Rect bodyRect = ((Root) selected).children.getRectOffDrawPoint();
					// The central element of the subqueue isn't the worst choice because from
					// here the distances are minimal. The top element, on the other hand,
					// is directly reachable by cursor down.
					x = bodyRect.right - 2;
					y = (bodyRect.top + bodyRect.bottom) / 2;
				} else {
					x = selRect.left - 2;
					// START KGU#346 2017-02-08: Bugfix #198: It's more intuitive to stay at header y level
					if (selected instanceof IFork) {
						y = selRect.top + ((IFork) selected).getHeadRect().bottom / 2;
					}
					// END KGU#346 2017-02-08
				}
				break;
			case CMD_RIGHT:
				// START KGU#495 2018-02-15: Bugfix #511 - we must never dive into collapsed loops!
				//if (selected instanceof ILoop)
				if (selected instanceof ILoop && !selected.isCollapsed(false))
				// END KGU#495 2018-02-15
				{
					Rect bodyRect = ((ILoop) selected).getBody().getRectOffDrawPoint();
					x = bodyRect.left + 2;
					// The central element of the subqueue isn't the worst choice because from
					// here the distances are minimal. The top element, on the other hand,
					// is directly reachable by cursor down.
					y = (bodyRect.top + bodyRect.bottom) / 2;
				} else if (selected instanceof Root) {
					Rect bodyRect = ((Root) selected).children.getRectOffDrawPoint();
					// The central element of the subqueue isn't the worst choice because from
					// here the distances are minimal. The top element, on the other hand,
					// is directly reachable by cursor down.
					x = bodyRect.left + 2;
					y = (bodyRect.top + bodyRect.bottom) / 2;
				}
				// START KGU#729 2019-09-24: Bugfix #751
				else if (selected instanceof Try) {
					Rect catchRect = ((Try) selected).qCatch.getRectOffDrawPoint();
					x = catchRect.left + 2;
					y = catchRect.top + 2;
				}
				// END KGU#729 2019-09-24
				else {
					x = selRect.right + 2;
					// START KGU#346 2017-02-08: Bugfix #198: It's more intuitive to stay at header y level
					if (selected instanceof IFork) {
						y = selRect.top + ((IFork) selected).getHeadRect().bottom / 2;
					}
					// END KGU#346 2017-02-08
				}
				break;
			}
			Element newSel = root.getElementByCoord(x, y, true);
			if (newSel != null) {
				// START KGU#177 2016-04-24: Bugfix - couldn't leave Parallel and Forever elements
				// Compound elements with a lower bar would catch the selection again when their last
				// encorporated element is left downwards. So identify such a situation and leap after
				// the enclosing compound...
				if (_direction == Editor.CursorMoveDirection.CMD_DOWN
						&& (newSel instanceof Parallel || newSel instanceof Forever || !Element.E_DIN && newSel instanceof For)
						&& newSel.getRectOffDrawPoint().top < selRect.top) {
					newSel = root.getElementByCoord(x, newSel.getRectOffDrawPoint().bottom + 2, true);
				}
				// END KGU#177 2016-04-24
				// START KGU#214 2016-07-25: Improvement of enh. #158
				else if (_direction == Editor.CursorMoveDirection.CMD_UP
						&& (newSel instanceof Forever || !Element.E_DIN && newSel instanceof For)
						&& newSel.getRectOffDrawPoint().bottom < selRect.bottom) {
					Subqueue body = ((ILoop) newSel).getBody();
					Element sel = root.getElementByCoord(x, body.getRectOffDrawPoint().bottom - 2, true);
					if (sel != null) {
						newSel = sel;
					}
				}
				// END KGU#214 2016-07-25
				// START KGU#214 2016-07-31: Issue #158
				else if (newSel instanceof Root && (_direction == Editor.CursorMoveDirection.CMD_LEFT
						|| _direction == Editor.CursorMoveDirection.CMD_RIGHT)) {
					newSel = selected;	// Stop before the border on boxed diagrams
				}
				// END KGU#214 2015-07-31
				// START KGU#729 2019-09-24: Bugfix #751
				else if (newSel instanceof Try) {
					Element sel = null;
					if (_direction == Editor.CursorMoveDirection.CMD_UP) {
						// From finally go to catch, from catch to try, from outside to finally
						if (selected == ((Try) newSel).qFinally || selected.isDescendantOf(((Try) newSel).qFinally)) {
							sel = root.getElementByCoord(x, ((Try) newSel).qCatch.getRectOffDrawPoint().bottom - 2, true);
						} else if (selected == ((Try) newSel).qCatch || selected.isDescendantOf(((Try) newSel).qCatch)) {
							sel = root.getElementByCoord(x, ((Try) newSel).qTry.getRectOffDrawPoint().bottom - 2, true);
						} else if (!selected.isDescendantOf(newSel)) {
							sel = root.getElementByCoord(x, ((Try) newSel).qFinally.getRectOffDrawPoint().bottom - 2, true);
						}
					} else if (_direction == Editor.CursorMoveDirection.CMD_DOWN) {
						// From try go to catch, from catch to finally, from finally to subsequent element
						if (selected == ((Try) newSel).qTry || selected.isDescendantOf(((Try) newSel).qTry)) {
							sel = root.getElementByCoord(x, ((Try) newSel).qCatch.getRectOffDrawPoint().top + 2, true);
						} else if (selected == ((Try) newSel).qCatch || selected.isDescendantOf(((Try) newSel).qCatch)) {
							sel = root.getElementByCoord(x, ((Try) newSel).qFinally.getRectOffDrawPoint().top + 2, true);
						} else if (selected == ((Try) newSel).qFinally || selected.isDescendantOf(((Try) newSel).qFinally)) {
							sel = root.getElementByCoord(x, newSel.getRectOffDrawPoint().bottom + 2, true);
						}
					}
					if (sel != null) {
						newSel = sel;
					}
				}
				// END KGU#729 2019-09-24
				selected = newSel;
			}
			// START KGU#214 2016-07-25: Bugfix for enh. #158 - un-boxed Roots didn't catch the selection
			// This was better than to rush around on horizontal wheel activity! Hence fix withdrawn
			//else if (_direction != Editor.CursorMoveDirection.CMD_UP && !root.isNice)
			//{
			//	selected = root;
			//}
			// END KGU#214 2016-07-25
			selected = selected.setSelected(true);

			// START KGU#177 2016-04-14: Enh. #158 - scroll to the selected element
			//redraw();
			redraw(selected);
			// END KGU#177 2016-04-14

			// START KGU#926 2021-02-04: Enh. #926
			this.scrollErrorListToSelected();
			// END KGU#926 2021-02-04
			// START KGU#705 2019-09-24: Enh. #738
			highlightCodeForSelection();
			// END KGU#705 2019-09-24
			// START KGU#177 2016-04-24: Bugfix - buttons haven't been updated 
			this.doButtons();
			// END KGU#177 2016-04-24
		}
	}
	// END KGU#177 2016-04-07

	// START KGU#206 2016-07-21: Enh. #158 + #197
	/**
	 * Tries to expand the selection towards the next element in the _direction
	 * specified. This is of course limited to the bounds of the containing
	 * Subqueue.
	 *
	 * @param _direction - the cursor key orientation (up, down)
	 */
	public void expandSelection(Editor.SelectionExpandDirection _direction) {
		if (selected != null
				&& !(selected instanceof Subqueue)
				&& !(selected instanceof Root)) {
			boolean newSelection = false;
			Subqueue sq = (Subqueue) selected.parent;
			Element first = selected;
			Element last = selected;
			// START KGU#866 2020-05-02: Issue #866 improved expansion / reduction strategy
			int anchorOffset = 0;
			boolean atUpperEnd = true;	// Is the selection to be modified at upper end?
			boolean atLowerEnd = true;	// Is the selection to be modified at lower end?
			// END KGU#866 2020-05-02
			if (selected instanceof SelectedSequence) {
				SelectedSequence sel = (SelectedSequence) selected;
				first = sel.getElement(0);
				last = sel.getElement(sel.getSize() - 1);
				// START KGU#866 2020-05-02: Issue #866 improved expansion / reduction strategy
				anchorOffset = sel.getAnchorOffset();
				atUpperEnd = !sel.wasModifiedBelowAnchor();	// FIXME offset of anchor?
				atLowerEnd = sel.wasModifiedBelowAnchor();
				// END KGU#866 2020-05-02
			}
			int index0 = sq.getIndexOf(first);
			int index1 = sq.getIndexOf(last);
			// START KGU#866 2020-05-02: Issue #866 improved expansion / reduction strategy
			//if (_direction == Editor.SelectionExpandDirection.EXPAND_UP && index0 > 0)
			if (index0 > 0
					&& (_direction == Editor.SelectionExpandDirection.EXPAND_TOP
					|| _direction == Editor.SelectionExpandDirection.EXPAND_UP && atUpperEnd))
			// END KGU#866 2020-05-02
			{
				// START KGU#866 2020-05-02: Issue #866 improved expansion / reduction strategy
				//selected = new SelectedSequence(sq, index0-1, index1);
				selected = new SelectedSequence(sq, index0 - 1, index1, anchorOffset + 1, false);
				// END KGU#866 2020-05-02
				newSelection = true;
			}
			// START KGU#866 2020-05-02: Issue #866 improved expansion / reduction strategy
			//else if (_direction == Editor.SelectionExpandDirection.EXPAND_DOWN && index1 < sq.getSize()-1)
			else if (index1 < sq.getSize() - 1
					&& (_direction == Editor.SelectionExpandDirection.EXPAND_BOTTOM
					|| _direction == Editor.SelectionExpandDirection.EXPAND_DOWN && atLowerEnd))
			// END KGU#866 2020-05-02
			{
				// START KGU#866 2020-05-02: Issue #866 improved expansion / reduction strategy
				//selected = new SelectedSequence(sq, index0, index1+1, _index1, true);
				selected = new SelectedSequence(sq, index0, index1 + 1, anchorOffset, true);
				// END KGU#866 2020-05-02
				newSelection = true;
			}
			// START KGU#866 2020-05-02: Issue #866 improved expansion / reduction strategy
			else if (_direction == Editor.SelectionExpandDirection.EXPAND_UP && atLowerEnd) {
				// Reduce at end
				selected.setSelected(false);
				redraw(selected);
				if (index0 + 1 >= index1) {
					// Selected sequence collapses to a single element
					selected = first;
				} else {
					selected = new SelectedSequence(sq, index0, index1 - 1,
							(anchorOffset == index1 - index0 ? anchorOffset - 1 : anchorOffset),
							true);
				}
				newSelection = true;
			} else if (_direction == Editor.SelectionExpandDirection.EXPAND_DOWN && atUpperEnd) {
				// Reduce at start
				selected.setSelected(false);
				redraw(selected);
				if (index0 + 1 >= index1) {
					// Selected sequence collapses to a single element
					selected = last;
				} else {
					selected = new SelectedSequence(sq, index0 + 1, index1,
							(anchorOffset == 0 ? 0 : anchorOffset - 1),
							false);
				}
				newSelection = true;
			}
			// END KGU#866 2020-05-02
			if (newSelection) {
				selected.setSelected(true);
				redraw(selected);
				this.doButtons();
			}
			// START KGU#705 2019-09-24: Enh. #738
			highlightCodeForSelection();
			// END KGU#705 2019-09-24
		}
	}
	// END KGU#206 2016-07-21

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// Nothing to do here
	}

	// START KGU#305 2016-12-15: Issues #305, #312
	@Override
	public void valueChanged(ListSelectionEvent ev) {
		if (ev.getSource() == errorlist) {
			// an error list entry has been selected
			// START KGU#565 2018-07-27: Bugfix #569 - content outsourced (as it may also be needed in mouseClicked())
			handleErrorListSelection();
			// END KGU#565 2018-07-27
		}
	}
	// END KGU#305

	// START KGU#565 2018-07-27: Bugfix #569 - errorlist selection without index change wasn't recognised
	/**
	 * Handles a single-click selection in the {@link #errorlist} (ensures the
	 * corresponding diagram element gets selected)
	 *
	 * @return {@code true} if an {@link Element} was associated with the
	 * selected error entry (which will then be the new {@link #selected} value)
	 */
	// START KGU#565 2021-01-06: More consistent selection/scroll handling
	//private void handleErrorListSelection() {
	private boolean handleErrorListSelection() {
	// END KGU#565 2021-01-06
		boolean hadElement = false;
		if (errorlist.getSelectedIndex() >= 0) {
			// get the selected error
			DetectedError err = root.errors.get(errorlist.getSelectedIndex());
			Element ele = err.getElement();
			// START KGU 2021-01-06: The scrolling should also be done for an already selected element
			//if (ele != null && ele != selected)
			if (ele != null)
			// END KGU 2021-01-06
			{
				// START KGU 2021-01-06: see above
				if (ele != selected) {
				// END KGU 2021-01-06
					// deselect the previously selected element (if any)
					if (selected != null) {
						selected.setSelected(false);
					}
					// select the new one
					selected = ele.setSelected(true);
				// START KGU 2021-01-06: See above
				}
				hadElement = true;
				// END KGU 2021-01-06

				// redraw the diagram
				// START KGU#276 2016-11-18: Issue #269 - ensure the associated element be visible
				//redraw();
				redraw(ele);
				// END KGU#276 2016-11-18

				// do the button thing
				if (NSDControl != null) {
					NSDControl.doButtons();
				}

				// START KGU#705 2019-09-24: Enh. #738
				highlightCodeForSelection();
				// END KGU#705 2019-09-24
				errorlist.requestFocusInWindow();
			}
		}
		// START KGU 2021-01-06: See above
		return hadElement;
		// END KGU 2021-01-06
	}

	// START KGU#363 2017-05-19: Enh. #372
	/**
	 * Opens the {@link AttributeInspector} for the current {@link Root}.
	 *
	 * @see #inspectAttributes(Root)
	 */
	public void attributesNSD() {
		inspectAttributes(root);
	}

	/**
	 * Opens the {@link AttributeInspector} for the specified {@code _root}.
	 *
	 * @param _root - a {@link Root} the attributes of which are to be presented
	 * @see #attributesNSD()
	 */
	public void inspectAttributes(Root _root) {
		RootAttributes licInfo = new RootAttributes(_root);
		AttributeInspector attrInsp = new AttributeInspector(
				this.getFrame(), licInfo);
		hideComments();	// Issue #143: Hide the current comment popup if visible
		// START KGU#911 2021-01-10: Enh. #910: We may not allow any change
		if (_root.isRepresentingDiagramController()) {
			attrInsp.btnOk.setEnabled(false);
		}
		// END KGU#911 2021-01-10
		attrInsp.setVisible(true);
		if (attrInsp.isCommitted()) {
			_root.addUndo(true);
			_root.adoptAttributes(attrInsp.licenseInfo);
		}
	}
	// END KGU#363 2017-05-17

	// START KGU#324 2017-05-30: Enh. #415
	public void findAndReplaceNSD() {
		if (this.findDialog == null) {
			findDialog = new FindAndReplace(this);
		}
		hideComments();
		// Even if the Find&Replace dialog had been visible it has now to regain focus
		findDialog.setVisible(true);
	}

	/**
	 * This only cares for the look and feel update of the Find&Replace dialog
	 * (if it is open) and of the Turtleizer.
	 */
	protected void updateLookAndFeel() {
		// START KGU#902 2021-01-01: Enh. #903
		try {
			javax.swing.SwingUtilities.updateComponentTreeUI(this.pop);
		} catch (Exception ex) {
		}
		// END KGU#902 2021-01-01
		if (this.findDialog != null) {
			try {
				javax.swing.SwingUtilities.updateComponentTreeUI(this.findDialog);
				// Restore sub-component listeners which might have got lost by the previous operation.
				this.findDialog.adaptToNewLaF();
			} catch (Exception ex) {
			}
		}
		// START KGU#685 2020-12-12: Enh. #704
		if (turtle != null) {
			turtle.updateLookAndFeel();
		}
		// END KGU#685 2020-12-12
		if (this.codeHighlighter != null) {
			this.codeHighlighter = codePreview.getHighlighter();
			this.updateCodePreview();
		}
	}
	// END KGU#324 2017-05-30

	// START KGU#324 2017-06-16: Enh. #415 Extracted from Mainform
	/**
	 * Caches several settings held in fields to the given {@link Ini} instance
	 *
	 * @param ini - the {@link Ini} instance (a singleton)
	 * @see #fetchIniProperties(Ini)
	 */
	public void cacheIniProperties(Ini ini) {
		if (this.currentDirectory != null) {
			ini.setProperty("currentDirectory", this.currentDirectory.getAbsolutePath());
			// START KGU#354 2071-04-26: Enh. #354 Also retain the other directories
			ini.setProperty("lastExportDirectory", this.lastCodeExportDir.getAbsolutePath());
			ini.setProperty("lastImportDirectory", this.lastCodeImportDir.getAbsolutePath());
			ini.setProperty("lastImportFilter", this.lastImportFilter);
			// END KGU#354 2017-04-26
		}
		// START KGU#305 2016-12-15: Enh. #305
		ini.setProperty("index", (this.showingArrangerIndex() ? "1" : "0"));
		// END KGU#305 2016-12-15
		// START KGU#705 2019-09-24: Enh. #738
		ini.setProperty("codePreview", (this.showingCodePreview() ? "1" : "0"));
		// END KGU#705 2019-09-14
		if (this.recentFiles.size() != 0) {
			for (int i = 0; i < this.recentFiles.size(); i++) {
				//System.out.println(i);
				ini.setProperty("recent" + String.valueOf(i), (String) this.recentFiles.get(i));
			}
		}
		// START KGU#602 2018-10-28: Enh. #419
		ini.setProperty("wordWrapLimit", Integer.toString(this.lastWordWrapLimit));
		// END KGU#602 2018-10-28
		// START KGU#654 2019-02-15: Enh. #681
		ini.setProperty("genExportPrefTrigger", Integer.toString(this.generatorProposalTrigger));
		// END KGU#654 2019-02-15

		if (this.findDialog != null) {
			this.findDialog.cacheToIni(ini);
		}
	}
	// END KGU#324 2017-06-16

	// START KGU#602 2018-10-28: Extracted from Mainform.loadFromIni()
	/**
	 * Adopts several settings held in fields from the given {@link Ini}
	 * instance
	 *
	 * @param ini - the {@link Ini} instance (a singleton)
	 * @see #cacheIniProperties(Ini)
	 */
	public void fetchIniProperties(Ini ini) {
		// current directory
		// START KGU#95 2015-12-04: Fix #42 Don't propose the System root but the user home
		//diagram.currentDirectory = new File(ini.getProperty("currentDirectory", System.getProperty("file.separator")));
		this.currentDirectory = new File(ini.getProperty("currentDirectory", System.getProperty("user.home")));
		// END KGU#95 2015-12-04
		// START KGU#354 2071-04-26: Enh. #354 Also retain the other directories
		this.lastCodeExportDir = new File(ini.getProperty("lastExportDirectory", System.getProperty("user.home")));
		this.lastCodeImportDir = new File(ini.getProperty("lastImportDirectory", System.getProperty("user.home")));
		this.lastImportFilter = ini.getProperty("lastImportDirectory", "");
		// END KGU#354 2017-04-26
		// START KGU#602 2018-10-28: Enh. #419
		try {
			this.lastWordWrapLimit = Integer.parseInt(ini.getProperty("wordWrapLimit", "0"));
			// START KGU#654 2019-02-15: Enh. #681
			this.generatorProposalTrigger = Integer.parseInt(ini.getProperty("genExportPrefTrigger", "5"));
			// END KGU#654 2019-02-15
		} catch (NumberFormatException ex) {
		}
		// END KGU#602 2018-10-28

		// recent files
		try {
			for (int i = MAX_RECENT_FILES - 1; i >= 0; i--) {
				if (ini.keySet().contains("recent" + i)) {
					if (!ini.getProperty("recent" + i, "").trim().isEmpty()) {
						this.addRecentFile(ini.getProperty("recent" + i, ""), false);
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Ini", e);
		}
		NSDControl.doButtons();
	}
	// END KGU#602 2018-10-28

	public void setSimplifiedGUI(boolean _simplified) {
		if (Element.E_REDUCED_TOOLBARS != _simplified) {
			Element.E_REDUCED_TOOLBARS = _simplified;
			for (MyToolbar toolbar : toolbars) {
				if (expertToolbars.contains(toolbar)) {
					// The toolbar is to be hidden completely
					toolbar.setVisible(!_simplified);
				} else {
					// Some speed buttons of the toolbar may have to be hidden
					toolbar.setExpertVisibility(!_simplified);
				}
			}
			Element.cacheToIni();
		}
	}

	/**
	 * Sets this instance initialized and has it redraw all.
	 */
	public void setInitialized() {
		this.isInitialized = true;
		redraw();
		analyse();
	}

	// START KGU#459 2017-11-14: Enh. #459-1
	public void showTutorialHint() {
		JOptionPane.showMessageDialog(this.getFrame(),
				Menu.msgGuidedTours.getText(),
				Menu.ttlGuidedTours.getText(),
				JOptionPane.INFORMATION_MESSAGE,
				IconLoader.getIconImage(getClass().getResource("icons/AnalyserHint.png")));
		analyse();
		repaint();
	}
	// END KGU#459 2017-11-14

	// START KGU#477 2017-12-06: Enh. #487
	/**
	 * Sets the display mode for hiding of mere declarartory element sequences
	 * according to the argument.
	 *
	 * @param _activate - whether to enable or disable the hiding mode.
	 */
	public void setHideDeclarations(boolean _activate) {
		Element selectedElement = this.selected;
		Element.E_HIDE_DECL = _activate;
		this.resetDrawingInfo();
		analyse();
		repaint();
		if (selectedElement != null) {
			if (selectedElement instanceof Instruction) {
				selectedElement.setSelected(false);
				selected = selectedElement = ((Instruction) selectedElement).getDrawingSurrogate(false);
				selectedElement.setSelected(true);
			}
			redraw(selectedElement);
		} else {
			redraw();
		}
		// START KGU#705 2019-09-24: Enh. #738
		updateCodePreview();
		// END KGU#705 2019-09-24
		// FIXME: The diagram will not always have been scrolled to the selected element by now...
	}
	// END KGU#477 2017-12-06

	// START KGU#479 2017-12-14: Enh. #492
	/**
	 * Opens an element designation configurator - this is to allow to discouple
	 * element names from localization.
	 */
	public void elementNamesNSD() {
		ElementNamePreferences namePrefs = new ElementNamePreferences(this.getFrame());
		for (int i = 0; i < namePrefs.txtElements.length; i++) {
			namePrefs.txtElements[i].setText(ElementNames.configuredNames[i]);
		}
		namePrefs.chkUseConfNames.setSelected(ElementNames.useConfiguredNames);
		namePrefs.setVisible(true);
		if (namePrefs.OK) {
			for (int i = 0; i < namePrefs.txtElements.length; i++) {
				ElementNames.configuredNames[i] = namePrefs.txtElements[i].getText();
			}
			ElementNames.useConfiguredNames = namePrefs.chkUseConfNames.isSelected();
			ElementNames.saveToINI();
			Locales.getInstance().setLocale(Locales.getInstance().getLoadedLocaleName());
		}
	}
	// END KGU#479 2017-12-14

	// START KGU#448 2018-01-05: Enh. #443
	/**
	 * Ensures field {@link #diagramControllers} being initialized and enables
	 * or disables the {@link DiagramController} with class name
	 * {@code className} according to the value of {@code selected}.
	 *
	 * @param className - full class name of a {@link DiagramController}
	 * subclass
	 * @param selected - if true enables, otherwise disables the specified
	 * controller
	 * @return true if the specified controller class was found.
	 * @see #getEnabledControllers()
	 * @see #isControllerEnabled(String)
	 */
	public boolean enableController(String className, boolean selected) {
		// Ensure diagramControllers is initialised 
		this.getDiagramControllers();
		// START KGU#911 2021-01-10: Enh. #910 Status now held in associated Includables
		//long mask = 1;
		//for (DiagramController controller: diagramControllers) {
		//	// The initial position is reserved for the TurtleBox instance, which may not have been created 
		//	if (controller == null && mask == 1) {
		//		diagramControllers.set(0, turtle);
		//		controller = turtle;
		//	}
		//	if (controller != null && controller.getClass().getName().equalsIgnoreCase(className)) {
		//		if (selected) {
		//			this.enabledDiagramControllers |= mask;
		//		}
		//		else {
		//			this.enabledDiagramControllers &= ~mask;
		//		}
		//		// START KGU#911 2021-01-09: Enh. #910 We must ensure the possible enumerators
		//		analyse();
		//		redraw();
		//		// END KGU#911 2021-01-09
		//		return true;
		//	}
		//	mask <<= 1;
		//}
		if (turtle != null && !diagramControllers.containsKey(turtle)) {
			diagramControllers.put(turtle, null);
		}
		for (Map.Entry<DiagramController, Root> entry : diagramControllers.entrySet()) {
			if (entry.getKey().getClass().getName().equals(className)) {
				Root incl = entry.getValue();
				// Turtleizer (incl == null) cannot be disabled
				if (incl != null) {
					boolean statusChanged = incl.isDisabled(true) == selected;
					incl.setDisabled(!selected);
					if (selected && !Arranger.getInstance().getAllRoots().contains(incl)) {
						Arranger.getInstance().addToPool(incl, this.getFrame(),
								Arranger.DIAGRAM_CONTROLLER_GROUP_NAME);
						// Ensure invisibility of the group and hence the diagram in Arranger
						for (Group group : Arranger.getInstance().getGroupsFromRoot(incl, true)) {
							if (group.getName().equals(Arranger.DIAGRAM_CONTROLLER_GROUP_NAME)) {
								group.setVisible(false);
								break;
							}
						}
					} else if (!selected && Arranger.hasInstance()) {
						Arranger.getInstance().removeDiagram(incl);
					}
					// We must ensure the possible enumerators are visible
					if (statusChanged) {
						this.resetDrawingInfo();
						analyse();
						redraw();
					}
				}
				return true;
			}
		}
		// END KGU#911 2021-01-10
		return false;
	}
	// END KGU#448 2018-01-14

	// START KGU#911 2021-01-10: Enh. #910 Added for menu "button" control
	public boolean isControllerEnabled(String className) {
		for (Map.Entry<DiagramController, Root> entry : diagramControllers.entrySet()) {
			if (entry.getKey().getClass().getName().equals(className)) {
				return entry.getValue() == null || !entry.getValue().isDisabled(true);
			}
		}
		return false;
	}
	// END KGU#911 2021-01-10

	// START KGU#480 2018-01-18: Enh. #490
	/**
	 * Opens a dialog allowing to configure alias names for
	 * {@link DiagramController} API methods (e.g. for {@link TurtleBox}).
	 *
	 * @param controllerPlugins - the plugin objects for the available
	 * {@link DiagramController}s
	 */
	public void controllerAliasesNSD(Vector<GENPlugin> controllerPlugins) {
		DiagramControllerAliases dialog = new DiagramControllerAliases(this.getFrame(), controllerPlugins);
		dialog.setVisible(true);
		// FIXME: Just temporary - mind Element.controllerName2Alias and Element.controllerAlias2Name
		if (dialog.OK) {
			try {
				Ini.getInstance().save();
			} catch (IOException ex) {
				// START KGU#484 2018-04-05: Issue #463
				//ex.printStackTrace();
				logger.log(Level.WARNING, "Trouble saving preferences.", ex);
				// END KGU#484 2018-04-05
			}
			setApplyAliases(dialog.chkApplyAliases.isSelected());
		}
	}

	/**
	 * Switches the replacement of {@link DiagramController} routine names with
	 * aliases on or off
	 *
	 * @param apply - new status value
	 */
	public void setApplyAliases(boolean apply) {
		Element.E_APPLY_ALIASES = apply;
		this.resetDrawingInfo();
		redraw();
		// START KGU#792 2020-02-04: Bugfix #805
		Ini.getInstance().setProperty("applyAliases", apply ? "1" : "0");
		// END KGU#792 2020-02-04
	}
	// END KGU#480 2018-01-18

	// START KGU#356 2019-03-14: Issue #366
	/**
	 * @return the owning @{@link JFrame} (actually the {@link Mainform}) or
	 * null
	 */
	public JFrame getFrame() {
		// START KGU 2019-11-24: Make sure this doesn't cause a NullPointerException
		if (this.NSDControl == null) {
			return null;
		}
		// END KGU 2019-11-24
		return this.NSDControl.getFrame();
	}
	// END KGU#356 2019-03-14

	// START KGU#466 2019-08-03: Issue #733 - Selective preferences export
	/**
	 * Caches the last used selection pattern in the preference category dialog
	 */
	private static Vector<Boolean> prefCategorySelection = new Vector<Boolean>();

	/**
	 * Opens a dialog allowing to elect preference categories for saving.
	 * Composes a set of ini property keys to be stored from the user selection.
	 * If the user opts for complete export then the returns set will be empty,
	 * if the user cancels then the result will be null.
	 *
	 * @param title - dialog title
	 * @param preferenceKeys - maps preference menu item names to arrays of key
	 * patterns
	 * @return the set of key patterns for filtering the preference export. may
	 * be empty or {@code null}.
	 */
	public Set<String> selectPreferencesToExport(String title, HashMap<String, String[]> preferenceKeys) {
		double scale = Double.parseDouble(Ini.getInstance().getProperty("scaleFactor", "1"));
		// Fill the selection vector to the necessary size
		for (int j = prefCategorySelection.size(); j < preferenceKeys.size(); j++) {
			prefCategorySelection.add(false);
		}
		Set<String> keys = null;
		JPanel panel = new JPanel();
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		panel1.setLayout(new GridLayout(0, 1));
		panel2.setLayout(new GridLayout(0, 2, (int) (5 * scale), 0));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JCheckBox chkAll = new JCheckBox(Menu.msgAllPreferences.getText(), true);
		JCheckBox[] chkCategories = new JCheckBox[preferenceKeys.size()];
		JButton btnInvert = new JButton(Menu.msgInvertSelection.getText());
		int i = 0;
		for (String category : preferenceKeys.keySet()) {
			String msgKey = "Menu." + category + ".text";
			String caption = Locales.getValue("Structorizer", msgKey, true);
			int posEllipse = caption.indexOf("...");
			if (posEllipse > 0) {
				caption = caption.substring(0, posEllipse).trim();
			}
			if (caption.endsWith("?")) {
				caption = caption.substring((caption.startsWith("¿") ? 1 : 0), caption.length() - 1);
			}
			JCheckBox chk = new JCheckBox(caption, prefCategorySelection.get(i));
			(chkCategories[i++] = chk).setEnabled(false);
			if (category.equals("menuDiagram")) {
				chk.setToolTipText(Menu.ttDiagramMenuSettings.getText().replace("%", caption));
			}
		}
		btnInvert.setEnabled(false);
		chkAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				boolean sel = chkAll.isSelected();
				for (int i = 0; i < chkCategories.length; i++) {
					chkCategories[i].setEnabled(!sel);
				}
				btnInvert.setEnabled(!sel);
			}
		});
		btnInvert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < chkCategories.length; i++) {
					chkCategories[i].setSelected(!chkCategories[i].isSelected());
				}
			}
		});
		panel1.add(chkAll);
		//for (JCheckBox chk: chkCategories) {
		//	panel2.add(chk);
		//}
		int offset = (chkCategories.length + 1) / 2;
		for (int j = 0; j < offset; j++) {
			panel2.add(chkCategories[j]);
			if (j + offset < chkCategories.length) {
				panel2.add(chkCategories[j + offset]);
			}
		}
		panel2.add(btnInvert);
		panel.add(panel1);
		panel.add(new JSeparator(SwingConstants.HORIZONTAL));
		panel.add(panel2);
		GUIScaler.rescaleComponents(panel);
		if (JOptionPane.showConfirmDialog(this.getFrame(), panel, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			keys = new HashSet<String>();
			if (!chkAll.isSelected()) {
				i = 0;
				for (String[] patterns : preferenceKeys.values()) {
					// START KGU#855 2020-04-23: Bugfix #856 didn't collect the correct items
					//if (prefCategorySelection.set(i, chkCategories[i].isSelected())) {
					boolean isSelected = chkCategories[i].isSelected();
					prefCategorySelection.set(i, isSelected);
					if (isSelected) {
					// END KGU#855 2020-04-23
						for (String pattern : patterns) {
							keys.add(pattern);
						}
					}
					i++;
				}
				if (keys.isEmpty()) {
					// If nothing  is selected then it doesn't make sense to save anything
					// (and to return an empty here set would mean to save all)
					keys = null;
				}
			}
		}
		return keys;
	}
	// END KGU#466 2019-08-03

}
